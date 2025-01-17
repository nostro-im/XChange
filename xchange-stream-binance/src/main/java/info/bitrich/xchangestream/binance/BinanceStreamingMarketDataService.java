package info.bitrich.xchangestream.binance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import info.bitrich.xchangestream.binance.dto.*;
import info.bitrich.xchangestream.binance.exceptions.UpFrontSubscriptionRequiredException;
import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.functions.Consumer;
import org.knowm.xchange.binance.BinanceAdapters;
import org.knowm.xchange.binance.BinanceErrorAdapter;
import org.knowm.xchange.binance.dto.BinanceException;
import org.knowm.xchange.binance.dto.marketdata.BinanceOrderbook;
import org.knowm.xchange.binance.dto.marketdata.BinanceTicker24h;
import org.knowm.xchange.binance.service.BinanceMarketDataService;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.OrderBookUpdate;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.exceptions.RateLimitExceededException;
import org.knowm.xchange.instrument.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static info.bitrich.xchangestream.service.netty.StreamingObjectMapperHelper.getObjectMapper;

public class BinanceStreamingMarketDataService implements StreamingMarketDataService {
  private static final Logger LOG =
      LoggerFactory.getLogger(BinanceStreamingMarketDataService.class);

  private static final JavaType TICKER_TYPE = getTickerType();
  private static final JavaType TRADE_TYPE = getTradeType();
  protected static final JavaType DEPTH_TYPE = getDepthType();

  protected final BinanceStreamingService service;
  private final String orderBookUpdateFrequencyParameter;

  private final Map<CurrencyPair, Flowable<BinanceTicker24h>> tickerSubscriptions;
  private final Map<CurrencyPair, Flowable<OrderBook>> orderbookSubscriptions;
  private final Map<CurrencyPair, Flowable<BinanceRawTrade>> tradeSubscriptions;
  private final Map<CurrencyPair, Flowable<OrderBookUpdate>> orderBookUpdatesSubscriptions;
  protected final Map<CurrencyPair, Flowable<DepthBinanceWebSocketTransaction>>
      orderBookRawUpdatesSubscriptions;

  private final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();
  protected final BinanceMarketDataService marketDataService;
  protected final Runnable onApiCall;

  protected final AtomicBoolean fallenBack = new AtomicBoolean();
  protected final AtomicReference<Runnable> fallbackOnApiCall = new AtomicReference<>(() -> {});

  public BinanceStreamingMarketDataService(
      BinanceStreamingService service,
      BinanceMarketDataService marketDataService,
      Runnable onApiCall,
      final String orderBookUpdateFrequencyParameter) {
    this.service = service;
    this.orderBookUpdateFrequencyParameter = orderBookUpdateFrequencyParameter;
    this.marketDataService = marketDataService;
    this.onApiCall = onApiCall;
    this.tickerSubscriptions = new ConcurrentHashMap<>();
    this.orderbookSubscriptions = new ConcurrentHashMap<>();
    this.tradeSubscriptions = new ConcurrentHashMap<>();
    this.orderBookUpdatesSubscriptions = new ConcurrentHashMap<>();
    this.orderBookRawUpdatesSubscriptions = new ConcurrentHashMap<>();
  }

  @Override
  public Flowable<OrderBook> getOrderBook(CurrencyPair currencyPair, Object... args) {
    if (!service.isLiveSubscriptionEnabled() && !service.getProductSubscription().getOrderBook().contains(currencyPair)) {
      throw new UpFrontSubscriptionRequiredException();
    }
    return orderbookSubscriptions.computeIfAbsent(currencyPair, this::initOrderBookIfAbsent);
  }

  private Flowable<OrderBook> initOrderBookIfAbsent(CurrencyPair currencyPair) {
    orderBookRawUpdatesSubscriptions.computeIfAbsent(currencyPair, s -> triggerFlowableBody(rawOrderBookUpdates(currencyPair)));
    return createOrderBookFlowable(currencyPair);
  }

  public Flowable<BinanceTicker24h> getRawTicker(CurrencyPair currencyPair, Object... args) {
    if (!service.isLiveSubscriptionEnabled() && !service.getProductSubscription().getTicker().contains(currencyPair)) {
      throw new UpFrontSubscriptionRequiredException();
    }
    return tickerSubscriptions.computeIfAbsent(currencyPair, s -> triggerFlowableBody(rawTickerStream(currencyPair)).publish(1).refCount());
  }

  public Flowable<BinanceRawTrade> getRawTrades(CurrencyPair currencyPair, Object... args) {
    if (!service.isLiveSubscriptionEnabled() && !service.getProductSubscription().getTrades().contains(currencyPair)) {
      throw new UpFrontSubscriptionRequiredException();
    }
    return tradeSubscriptions.computeIfAbsent(currencyPair, s -> triggerFlowableBody(rawTradeStream(currencyPair)).publish(1).refCount());
  }

  /**
   * Api to get binance incremental order book updates. As binance websocket provides only api to
   * get incremental updates {@link #getOrderBook(CurrencyPair, Object...)} have to build book from
   * rest and websocket which leads to delay before the order book will be received by subscriber.
   * This api provides the ability to start receiving updates immediately. It is allowed to
   * subscribe to this api and {@link #getOrderBook(CurrencyPair, Object...)} at the same time.
   */
  public Flowable<OrderBookUpdate> getOrderBookUpdates(
      CurrencyPair currencyPair, Object... args) {
    if (!service.isLiveSubscriptionEnabled() && !service.getProductSubscription().getOrderBook().contains(currencyPair)) {
      throw new UpFrontSubscriptionRequiredException();
    }
    return orderBookUpdatesSubscriptions.computeIfAbsent(
        currencyPair, this::initOrderBookUpdateIfAbsent);
  }

  private Flowable<OrderBookUpdate> initOrderBookUpdateIfAbsent(CurrencyPair currencyPair) {
    orderBookRawUpdatesSubscriptions.computeIfAbsent(currencyPair, s -> triggerFlowableBody(rawOrderBookUpdates(currencyPair)));
    return createOrderBookUpdatesFlowable(currencyPair);
  }

  @Override
  public Flowable<Ticker> getTicker(CurrencyPair currencyPair, Object... args) {
    return getRawTicker(currencyPair).map(BinanceTicker24h::toTicker);
  }

  @Override
  public Flowable<Trade> getTrades(CurrencyPair currencyPair, Object... args) {
    return getRawTrades(currencyPair, args)
        .map(
            rawTrade ->
                new Trade.Builder()
                    .type(BinanceAdapters.convertType(rawTrade.isBuyerMarketMaker()))
                    .originalAmount(rawTrade.getQuantity())
                    .instrument(currencyPair)
                    .price(rawTrade.getPrice())
                    .timestamp(new Date(rawTrade.getTimestamp()))
                    .id(String.valueOf(rawTrade.getTradeId()))
                    .build());
  }

  private Flowable<OrderBookUpdate> createOrderBookUpdatesFlowable(CurrencyPair currencyPair) {
    return orderBookRawUpdatesSubscriptions
        .get(currencyPair)
        .flatMap(
            depthTransaction ->
                FlowableFromStream(extractOrderBookUpdates(currencyPair, depthTransaction)))
        .publish(1).refCount();
  }

  protected String channelFromCurrency(CurrencyPair currencyPair, String subscriptionType) {
    String currency = String.join("", currencyPair.toString().split("/")).toLowerCase();
    String currencyChannel = currency + "@" + subscriptionType;

    if (BinanceSubscriptionType.DEPTH.getType().equals(subscriptionType)) {
      return currencyChannel + orderBookUpdateFrequencyParameter;
    } else {
      return currencyChannel;
    }
  }

  /**
   * Registers subsriptions with the streaming service for the given products.
   *
   * <p>As we receive messages as soon as the connection is open, we need to register subscribers to
   * handle these before the first messages arrive.
   */
  public void openSubscriptions(ProductSubscription productSubscription) {
    productSubscription.getTicker().forEach(this::initTickerSubscription);
    productSubscription.getOrderBook().forEach(this::initRawOrderBookUpdatesSubscription);
    productSubscription.getTrades().forEach(this::initTradeSubscription);
  }

  /**
   * Live Unsubscription that should be called in the Observable.doOnDispose(). This is required to stop receiving data from the stream.
   * This method also clear the subscription from the appropriate map.
   */
  public void unsubscribe(CurrencyPair currencyPair, BinanceSubscriptionType subscriptionType) {

    if (!service.isLiveSubscriptionEnabled()) {
      throw new UnsupportedOperationException("Unsubscribe not supported for Binance when live Subscription/Unsubscription is disabled. " +
          "Call BinanceStreamingExchange.enableLiveSubscription() to active it");
    }
    final String channelId = String.join("", currencyPair.toString().split("/")).toLowerCase()
        + "@" + subscriptionType.getType();
    this.service.unsubscribeChannel(channelId);

    switch (subscriptionType) {
      case DEPTH:
        orderbookSubscriptions.remove(currencyPair);
        orderBookUpdatesSubscriptions.remove(currencyPair);
        orderBookRawUpdatesSubscriptions.remove(currencyPair);
        break;
      case TRADE:
        tradeSubscriptions.remove(currencyPair);
        break;
      case TICKER:
        tickerSubscriptions.remove(currencyPair);
        break;
      default:
        throw new RuntimeException("Subscription type not supported to unsubscribe from stream");
    }
  }

  private void initTradeSubscription(CurrencyPair currencyPair) {
    tradeSubscriptions.put(
        currencyPair, triggerFlowableBody(rawTradeStream(currencyPair)).publish(1).refCount());
  }

  private void initTickerSubscription(CurrencyPair currencyPair) {
    tickerSubscriptions.put(
        currencyPair, triggerFlowableBody(rawTickerStream(currencyPair)).publish(1).refCount());
  }

  private void initRawOrderBookUpdatesSubscription(CurrencyPair currencyPair) {
    orderBookRawUpdatesSubscriptions.put(
        currencyPair, triggerFlowableBody(rawOrderBookUpdates(currencyPair)));
  }

  private Flowable<BinanceTicker24h> rawTickerStream(CurrencyPair currencyPair) {
    return service
        .subscribeChannel(channelFromCurrency(currencyPair, BinanceSubscriptionType.TICKER.getType()))
        .map(
            it ->
                this.<TickerBinanceWebsocketTransaction>readTransaction(it, TICKER_TYPE, "ticker"))
        .filter(transaction -> transaction.getData().getCurrencyPair().equals(currencyPair))
        .map(transaction -> transaction.getData().getTicker());
  }

  private final class OrderbookSubscription {
    final Flowable<DepthBinanceWebSocketTransaction> stream;
    final AtomicLong lastUpdateId = new AtomicLong();
    final AtomicLong snapshotLastUpdateId = new AtomicLong();
    OrderBook orderBook;

    private OrderbookSubscription(Flowable<DepthBinanceWebSocketTransaction> stream) {
      this.stream = stream;
    }

    void invalidateSnapshot() {
      snapshotLastUpdateId.set(0);
    }

    void initSnapshotIfInvalid(CurrencyPair currencyPair) {
      if (snapshotLastUpdateId.get() != 0) return;
      try {
        LOG.info("Fetching initial orderbook snapshot for {} ", currencyPair);
        onApiCall.run();
        fallbackOnApiCall.get().run();
        BinanceOrderbook book = fetchBinanceOrderBook(currencyPair);
        snapshotLastUpdateId.set(book.lastUpdateId);
        lastUpdateId.set(book.lastUpdateId);
        orderBook = BinanceMarketDataService.convertOrderBook(book, currencyPair);
      } catch (Exception e) {
        LOG.error("Failed to fetch initial order book for " + currencyPair, e);
        snapshotLastUpdateId.set(0);
        lastUpdateId.set(0);
        orderBook = null;
      }
    }

    private BinanceOrderbook fetchBinanceOrderBook(CurrencyPair currencyPair)
        throws IOException, InterruptedException {
      try {
        return marketDataService.getBinanceOrderbook(currencyPair, 1000);
      } catch (BinanceException e) {
        if (BinanceErrorAdapter.adapt(e) instanceof RateLimitExceededException) {
          if (fallenBack.compareAndSet(false, true)) {
            LOG.error(
                "API Rate limit was hit when fetching Binance order book snapshot. Provide a \n"
                    + "rate limiter. Apache Commons and Google Guava provide the TimedSemaphore\n"
                    + "and RateLimiter classes which are effective for this purpose. Example:\n"
                    + "\n"
                    + "  exchangeSpecification.setExchangeSpecificParametersItem(\n"
                    + "      info.bitrich.xchangestream.util.Events.BEFORE_API_CALL_HANDLER,\n"
                    + "      () -> rateLimiter.acquire())\n"
                    + "\n"
                    + "Pausing for 15sec and falling back to one call per three seconds, but you\n"
                    + "will get more optimal performance by handling your own rate limiting.");
            RateLimiter rateLimiter = RateLimiter.create(0.333);
            fallbackOnApiCall.set(rateLimiter::acquire);
            Thread.sleep(15000);
          }
        }
        throw e;
      }
    }
  }

  protected Flowable<DepthBinanceWebSocketTransaction> rawOrderBookUpdates(
      CurrencyPair currencyPair) {
    return service
        .subscribeChannel(channelFromCurrency(currencyPair,  BinanceSubscriptionType.DEPTH.getType()))
        .map(
            it ->
                this.<DepthBinanceWebSocketTransaction>readTransaction(
                    it, DEPTH_TYPE, "order book"))
        .map(BinanceWebsocketTransaction::getData)
        .filter(data -> data.getCurrencyPair().equals(currencyPair));
  }

  protected Flowable<OrderBook> createOrderBookFlowable(CurrencyPair currencyPair) {
    // 1. Open a stream to wss://stream.binance.com:9443/ws/bnbbtc@depth
    // 2. Buffer the events you receive from the stream.
    OrderbookSubscription subscription =
        new OrderbookSubscription(orderBookRawUpdatesSubscriptions.get(currencyPair));

    return subscription
        .stream

        // 3. Get a depth snapshot from
        // https://www.binance.com/api/v1/depth?symbol=BNBBTC&limit=1000
        // (we do this if we don't already have one or we've invalidated a previous one)
        .doOnNext(transaction -> subscription.initSnapshotIfInvalid(currencyPair))

        // If we failed, don't return anything. Just keep trying until it works
        .filter(transaction -> subscription.snapshotLastUpdateId.get() > 0L)

        // 4. Drop any event where u is <= lastUpdateId in the snapshot
        .filter(depth -> depth.getLastUpdateId() > subscription.snapshotLastUpdateId.get())

        // 5. The first processed should have U <= lastUpdateId+1 AND u >= lastUpdateId+1, and
        // subsequent events would
        // normally have u == lastUpdateId + 1 which is stricter version of the above - let's be
        // more relaxed
        // each update has absolute numbers so even if there's an overlap it does no harm
        .filter(
            depth -> {
              long lastUpdateId = subscription.lastUpdateId.get();
              boolean result;
              if (lastUpdateId == 0L) {
                result = true;
              } else {
                result =
                    depth.getFirstUpdateId() <= lastUpdateId + 1
                        && depth.getLastUpdateId() >= lastUpdateId + 1;
              }
              if (result) {
                subscription.lastUpdateId.set(depth.getLastUpdateId());
              } else {
                // If not, we re-sync.  This will commonly occur a few times when starting up, since
                // given update ids 1,2,3,4,5,6,7,8,9, Binance may sometimes return a snapshot
                // as of 5, but update events covering 1-3, 4-6 and 7-9.  We can't apply the 4-6
                // update event without double-counting 5, and we can't apply the 7-9 update without
                // missing 6.  The only thing we can do is to keep requesting a fresh snapshot until
                // we get to a situation where the snapshot and an update event precisely line up.
                LOG.info(
                    "Orderbook snapshot for {} out of date (last={}, U={}, u={}). This is normal. Re-syncing.",
                    currencyPair,
                    lastUpdateId,
                    depth.getFirstUpdateId(),
                    depth.getLastUpdateId());
                subscription.invalidateSnapshot();
              }
              return result;
            })

        // 7. The data in each event is the absolute quantity for a price level
        // 8. If the quantity is 0, remove the price level
        // 9. Receiving an event that removes a price level that is not in your local order book can
        // happen and is normal.
        .map(
            depth -> {
              extractOrderBookUpdates(currencyPair, depth)
                  .forEach(it -> subscription.orderBook.update(it));
              return subscription.orderBook;
            })
        .publish(1).refCount();
  }

  private Flowable<BinanceRawTrade> rawTradeStream(CurrencyPair currencyPair) {
    return service
        .subscribeChannel(channelFromCurrency(currencyPair, BinanceSubscriptionType.TRADE.getType()))
        .map(it -> this.<TradeBinanceWebsocketTransaction>readTransaction(it, TRADE_TYPE, "trade"))
        .filter(transaction -> transaction.getData().getCurrencyPair().equals(currencyPair))
        .map(transaction -> transaction.getData().getRawTrade());
  }

  /**
   * Force Flowable to execute its body, this way we get `BinanceStreamingService` to register the
   * Flowables emitter ready for our message arrivals.
   */
  private <T> Flowable<T> triggerFlowableBody(Flowable<T> Flowable) {
    Consumer<T> NOOP = whatever -> {};
    Flowable.subscribe(NOOP);
    return Flowable;
  }

  protected <T> BinanceWebsocketTransaction<T> readTransaction(
          JsonNode node, JavaType type, String transactionType) {
    try {
      return mapper.readValue(mapper.treeAsTokens(node), type);
    } catch (IOException e) {
      throw new ExchangeException(
          String.format("Unable to parse %s transaction", transactionType), e);
    }
  }

  protected Stream<OrderBookUpdate> extractOrderBookUpdates(
          Instrument instrument, DepthBinanceWebSocketTransaction depthTransaction) {
    BinanceOrderbook orderBookDiff = depthTransaction.getOrderBook();

    Stream<OrderBookUpdate> bidStream =
        orderBookDiff.bids.entrySet().stream()
            .map(
                entry ->
                    new OrderBookUpdate(
                        OrderType.BID,
                        entry.getValue(),
                        instrument,
                        entry.getKey(),
                        depthTransaction.getEventTime(),
                        entry.getValue()));

    Stream<OrderBookUpdate> askStream =
        orderBookDiff.asks.entrySet().stream()
            .map(
                entry ->
                    new OrderBookUpdate(
                        OrderType.ASK,
                        entry.getValue(),
                        instrument,
                        entry.getKey(),
                        depthTransaction.getEventTime(),
                        entry.getValue()));

    return Stream.concat(bidStream, askStream);
  }

  private <T> Flowable<T> FlowableFromStream(Stream<T> stream) {
    return Flowable.create(
        emitter -> {
          stream.forEach(emitter::onNext);
          emitter.onComplete();
        }, BackpressureStrategy.LATEST);
  }

  private static JavaType getTickerType() {
    return getObjectMapper()
        .getTypeFactory()
        .constructType(
            new TypeReference<BinanceWebsocketTransaction<TickerBinanceWebsocketTransaction>>() {});
  }

  private static JavaType getTradeType() {
    return getObjectMapper()
        .getTypeFactory()
        .constructType(
            new TypeReference<BinanceWebsocketTransaction<TradeBinanceWebsocketTransaction>>() {});
  }

  private static JavaType getDepthType() {
    return getObjectMapper()
        .getTypeFactory()
        .constructType(
            new TypeReference<BinanceWebsocketTransaction<DepthBinanceWebSocketTransaction>>() {});
  }
}

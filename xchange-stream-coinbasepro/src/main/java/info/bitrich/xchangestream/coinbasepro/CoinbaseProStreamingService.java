package info.bitrich.xchangestream.coinbasepro;

import static io.netty.util.internal.StringUtil.isNullOrEmpty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.coinbasepro.cache.OpenOrdersCache;
import info.bitrich.xchangestream.coinbasepro.dto.CoinbaseProWebSocketSubscriptionMessage;
import info.bitrich.xchangestream.coinbasepro.dto.CoinbaseProWebSocketTransaction;
import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.service.netty.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import io.reactivex.rxjava3.disposables.Disposable;
import org.knowm.xchange.coinbasepro.dto.account.CoinbaseProWebsocketAuthData;
import org.knowm.xchange.coinbasepro.service.CoinbaseProMarketDataService;
import org.knowm.xchange.coinbasepro.service.CoinbaseProTradeService;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoinbaseProStreamingService extends JsonNettyStreamingService {
  private static final Logger LOG = LoggerFactory.getLogger(CoinbaseProStreamingService.class);
  private static final String SUBSCRIBE = "subscribe";
  private static final String UNSUBSCRIBE = "unsubscribe";
  private static final String SHARE_CHANNEL_NAME = "ALL";
  private final Map<String, Flowable<JsonNode>> subscriptions = new ConcurrentHashMap<>();
  private ProductSubscription product = null;
  private final Supplier<CoinbaseProWebsocketAuthData> authData;
  private final boolean subscribeL3Orderbook;

  private WebSocketClientHandler.WebSocketMessageHandler channelInactiveHandler = null;

  private OpenOrdersCache openOrdersCache;
  private Disposable disposable;

  public CoinbaseProStreamingService(
      CoinbaseProMarketDataService marketDataService,
      CoinbaseProTradeService tradeService,
      String apiUrl,
          Supplier<CoinbaseProWebsocketAuthData> authData,
          boolean subscribeL3Orderbook) {
    super(apiUrl, Integer.MAX_VALUE);
    this.authData = authData;
    this.subscribeL3Orderbook = subscribeL3Orderbook;
    openOrdersCache = new OpenOrdersCache(marketDataService, tradeService);
  }

  public CoinbaseProStreamingService(
      CoinbaseProMarketDataService marketDataService,
      CoinbaseProTradeService tradeService,
      String apiUrl,
      int maxFramePayloadLength,
      Duration connectionTimeout,
      Duration retryDuration,
      int idleTimeoutSeconds,
      Supplier<CoinbaseProWebsocketAuthData> authData,
      boolean subscribeL3Orderbook) {
    super(apiUrl, maxFramePayloadLength, connectionTimeout, retryDuration, idleTimeoutSeconds);
    this.authData = authData;
    this.subscribeL3Orderbook = subscribeL3Orderbook;
    openOrdersCache = new OpenOrdersCache(marketDataService, tradeService);
  }

  public OpenOrdersCache getCache() {
    return openOrdersCache;
  }

  public ProductSubscription getProduct() {
    return product;
  }

  @Override
  public String getSubscriptionUniqueId(String channelName, Object... args) {
    return SHARE_CHANNEL_NAME;
  }

  /**
   * Subscribes to the provided channel name, maintains a cache of subscriptions, in order not to
   * subscribe more than once to the same channel.
   *
   * @param channelName the name of the requested channel.
   * @return an Flowable of json objects coming from the exchange.
   */
  @Override
  public Flowable<JsonNode> subscribeChannel(String channelName, Object... args) {
    channelName = SHARE_CHANNEL_NAME;

    if (!channels.containsKey(channelName) && !subscriptions.containsKey(channelName)) {
      subscriptions.put(channelName, super.subscribeChannel(channelName, args));
    }

    return subscriptions.get(channelName);
  }

  @Override
  public Completable connect() {
    return super.connect().doOnComplete(this::initCache);
  }

  /**
   * Subscribes to web socket transactions related to the specified currency, in their raw format.
   *
   * @param currencyPair The currency pair.
   * @return The stream.
   */
  public Flowable<CoinbaseProWebSocketTransaction> getRawWebSocketTransactions(
      CurrencyPair currencyPair, boolean filterChannelName) {
    String channelName = currencyPair.base.toString() + "-" + currencyPair.counter.toString();
    final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();
    return subscribeChannel(channelName)
        .map(s -> mapper.readValue(s.toString(), CoinbaseProWebSocketTransaction.class))
        .filter(t -> channelName.equals(t.getProductId()))
        .filter(t -> !isNullOrEmpty(t.getType()));
  }

  public void initCache() {
    final ObjectMapper mapper = StreamingObjectMapperHelper.getObjectMapper();
    disposable = subscribeChannel("")
            .map(s -> mapper.readValue(s.toString(), CoinbaseProWebSocketTransaction.class))
            .doOnError(e -> LOG.error("Error on Coinbase Pro websocket", e))
            .retry()
            .subscribe(t -> openOrdersCache.processWebSocketTransaction(t),
                    e -> LOG.error("Error caught on orders cache subscriber:\n", e));
  }

  @Override
  public Completable disconnect() {
    if (disposable != null) {
      disposable.dispose();
    }
    return super.disconnect();
  }
  
  boolean isAuthenticated() {
    return authData.get() != null;
  }

  @Override
  protected String getChannelNameFromMessage(JsonNode message) {
    return SHARE_CHANNEL_NAME;
  }

  @Override
  public String getSubscribeMessage(String channelName, Object... args) throws IOException {
    CoinbaseProWebSocketSubscriptionMessage subscribeMessage =
        new CoinbaseProWebSocketSubscriptionMessage(
            SUBSCRIBE, product, subscribeL3Orderbook, authData.get());
    return objectMapper.writeValueAsString(subscribeMessage);
  }

  @Override
  public String getUnsubscribeMessage(String channelName) throws IOException {
    CoinbaseProWebSocketSubscriptionMessage subscribeMessage =
        new CoinbaseProWebSocketSubscriptionMessage(
            UNSUBSCRIBE, new String[] {"level2", "matches", "ticker", "full"}, authData.get());
    return objectMapper.writeValueAsString(subscribeMessage);
  }

  @Override
  protected WebSocketClientExtensionHandler getWebSocketClientExtensionHandler() {
    return WebSocketClientCompressionAllowClientNoContextAndServerNoContextHandler.INSTANCE;
  }

  @Override
  protected WebSocketClientHandler getWebSocketClientHandler(
      WebSocketClientHandshaker handshaker,
      WebSocketClientHandler.WebSocketMessageHandler handler) {
    LOG.info("Registering CoinbaseProWebSocketClientHandler");
    return new CoinbaseProWebSocketClientHandler(handshaker, handler);
  }

  public void setChannelInactiveHandler(
      WebSocketClientHandler.WebSocketMessageHandler channelInactiveHandler) {
    this.channelInactiveHandler = channelInactiveHandler;
  }

  public void subscribeMultipleCurrencyPairs(ProductSubscription... products) {
    this.product = products[0];
  }

  @Override
  protected void handleChannelMessage(String channel, JsonNode message) {
    if (SHARE_CHANNEL_NAME.equals(channel)) {
      channels.forEach((k, v) -> v.getEmitter().onNext(message));

    } else {
      super.handleChannelMessage(channel, message);
    }
  }

  /**
   * Custom client handler in order to execute an external, user-provided handler on channel events.
   * This is useful because it seems CoinbasePro unexpectedly closes the web socket connection.
   */
  class CoinbaseProWebSocketClientHandler extends NettyWebSocketClientHandler {

    public CoinbaseProWebSocketClientHandler(
        WebSocketClientHandshaker handshaker, WebSocketMessageHandler handler) {
      super(handshaker, handler);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
      super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
      super.channelInactive(ctx);
      if (channelInactiveHandler != null) {
        channelInactiveHandler.onMessage("WebSocket Client disconnected!");
      }
    }
  }
}

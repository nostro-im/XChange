package info.bitrich.xchangestream.ftx;

import com.google.common.collect.Lists;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import io.reactivex.rxjava3.core.Flowable;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.ftx.FtxAdapters;
import org.knowm.xchange.instrument.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtxStreamingMarketDataService implements StreamingMarketDataService {

  private static final Logger LOG = LoggerFactory.getLogger(FtxStreamingMarketDataService.class);

  private final FtxStreamingService service;

  public FtxStreamingMarketDataService(FtxStreamingService service) {
    this.service = service;
  }

  @Override
  public Flowable<OrderBook> getOrderBook(Instrument instrument, Object... args) {
    OrderBook orderBook = new OrderBook(null, Lists.newArrayList(), Lists.newArrayList());
    String channelName = "orderbook:" + FtxAdapters.adaptInstrumentToFtxMarket(instrument);

    return service
        .subscribeChannel(channelName)
        .map(
            res -> {
              try {
                return FtxStreamingAdapters.adaptOrderbookMessage(orderBook, instrument, res);
              } catch (IllegalStateException e) {
                LOG.warn(
                    "Resubscribing {} channel after adapter error {}",
                     instrument,
                    e.getMessage());
                orderBook.getBids().clear();
                orderBook.getAsks().clear();
                // Resubscribe to the channel
                this.service.sendMessage(service.getUnsubscribeMessage(channelName));
                this.service.sendMessage(service.getSubscribeMessage(channelName, args));
                return new OrderBook(null, Lists.newArrayList(), Lists.newArrayList(), false);
              }
            })
        .filter(ob -> ob.getBids().size() > 0 && ob.getAsks().size() > 0);
  }

  @Override
  public Flowable<Ticker> getTicker(Instrument instrument, Object... args) {
    return service
        .subscribeChannel("ticker:" + FtxAdapters.adaptInstrumentToFtxMarket(instrument))
        .map(res -> FtxStreamingAdapters.adaptTickerMessage(instrument, res))
        .filter(ticker -> ticker != FtxStreamingAdapters.NULL_TICKER); // lets not send these backs
  }

  @Override
  public Flowable<Trade> getTrades(Instrument instrument, Object... args) {
    return service
        .subscribeChannel("trades:" + FtxAdapters.adaptInstrumentToFtxMarket(instrument))
        .flatMapIterable(res -> FtxStreamingAdapters.adaptTradesMessage(instrument, res));
  }
}

package info.bitrich.xchangestream.binance;

import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import io.reactivex.rxjava3.disposables.Disposable;

import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.CandleStick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This class test the Live Subscription/Unsubscription feature of the Binance Api.
 * See https://github.com/binance/binance-spot-api-docs/blob/master/web-socket-streams.md#live-subscribingunsubscribing-to-streams
 *
 * Before this addon, the subscription of the currency pairs required to be at the connection time, so if we wanted to add new
 * currencies to the stream, it was required to disconnect from the stream and reconnect with the new ProductSubscription instance
 * that contains all currency pairs.
 * With the new addon, we can subscribe to new currencies live without disconnecting the stream.
 */
public class BinanceLiveSubscriptionExample {

    private static final Logger LOG = LoggerFactory.getLogger(BinanceLiveSubscriptionExample.class);

    public static void main(String[] args) throws InterruptedException {

        ExchangeSpecification spec = StreamingExchangeFactory.INSTANCE.createExchange(BinanceStreamingExchange.class)
                .getDefaultExchangeSpecification();
        BinanceStreamingExchange exchange =
            (BinanceStreamingExchange) StreamingExchangeFactory.INSTANCE.createExchange(spec);
        Set<Disposable> disposables = new HashSet<>();
        Disposable disp = null;
        
        // First, we subscribe only for one currency pair at connection time (minimum requirement)
        ProductSubscription subscription =
            ProductSubscription.create()
            	.addTrades(CurrencyPair.BTC_USDT)
            	.addOrderbook(CurrencyPair.BTC_USDT)
        		.addCandleSticks(CurrencyPair.BTC_USDT, 1)
        		.build();
        // Note: at connection time, the live subscription is disabled
        exchange.connect(subscription).blockingAwait();
        
        disp = exchange.getStreamingMarketDataService()
            .getTrades(CurrencyPair.BTC_USDT)
            .doOnCancel(
                () -> exchange.getStreamingMarketDataService().unsubscribe(CurrencyPair.BTC_USDT, BinanceSubscriptionType.TRADE))
            .subscribe(
                trade -> { LOG.info("Trade: {}", trade); });
        disposables.add(disp);
        
        disp = exchange.getStreamingMarketDataService()
            .getOrderBook(CurrencyPair.BTC_USDT)
            .doOnCancel(
                () -> exchange.getStreamingMarketDataService().unsubscribe(CurrencyPair.BTC_USDT, BinanceSubscriptionType.DEPTH))
            .subscribe(
                orderBook -> { LOG.info("Order book: {}", orderBook); });
        disposables.add(disp);
        
        disp = exchange.getStreamingMarketDataService()
	        .getCandleSticks(CurrencyPair.BTC_USDT, 1)
	        .filter(CandleStick::isClosed)
	        .doOnCancel(
	            () -> exchange.getStreamingMarketDataService().unsubscribe(CurrencyPair.BTC_USDT, BinanceSubscriptionType.KLINE_1s))
	        .subscribe(
	            candle -> { LOG.info("CandleStick: {}", candle); });
        disposables.add(disp);
        
        Thread.sleep(10000);
        
        // Now we enable the live subscription/unsubscription to add new currencies to the streams
        LOG.info("Enable live subscription/unsubscription");
        exchange.enableLiveSubscription();
        disposables.forEach(d -> d.dispose());
        
        // We subscribe to 3 new currency pairs for trade (live subscription)
        // IMPORTANT!! Binance has a websocket limit of 5 incoming messages per second. If you bypass this limit, the websocket will be disconnected.
        // (See https://github.com/binance/binance-spot-api-docs/blob/master/web-socket-streams.md#websocket-limits for more details)
        // If you plan to subscribe/unsubscribe more than 5 currency pairs at a time, use a rate limiter or keep the live subscription
        // feature disabled and connect your pairs at connection time only (default value).
        final List<CurrencyPair> currencyPairs = Arrays.asList(CurrencyPair.ETH_USDT, CurrencyPair.LTC_USDT, CurrencyPair.XRP_USDT);
        for (final CurrencyPair currencyPair : currencyPairs) {
            disp = exchange.getStreamingMarketDataService()
                .getTrades(currencyPair)
                .doOnCancel(
                    () -> exchange.getStreamingMarketDataService().unsubscribe(currencyPair, BinanceSubscriptionType.TRADE))
                .subscribe(
                    trade -> { LOG.info("Trade: {}", trade); });
            disposables.add(disp);
        }
        Thread.sleep(5000);
        
        // Now we unsubscribe BTC/USDT from the stream (TRADE and DEPTH) and also the another currency pairs (TRADE 3x)
        // Note: we are ok with live unsubscription because we not bypass the limit of 5 messages per second.
        
        LOG.info("Now all symbols are live unsubscribed (BTC, ETH, LTC & XRP). We will live subscribe to XML/USDT and EOS/BTC...");
        Thread.sleep(5000);

        exchange.getStreamingMarketDataService()
            .getTrades(CurrencyPair.XLM_USDT)
            .doOnCancel(
                () -> exchange.getStreamingMarketDataService().unsubscribe(CurrencyPair.XLM_USDT, BinanceSubscriptionType.TRADE))
            .subscribe(
                trade -> {  });
        exchange.getStreamingMarketDataService()
            .getTrades(CurrencyPair.EOS_BTC)
            .doOnCancel(
                () -> exchange.getStreamingMarketDataService().unsubscribe(CurrencyPair.EOS_BTC, BinanceSubscriptionType.TRADE))
            .subscribe(
                trade -> {  });

        Thread.sleep(5000);
        LOG.info("Test finished, we unsubscribe XML/USDT and EOS/BTC from the streams.");

        exchange.disconnect().blockingAwait();
        LOG.info("Done!");
    }
}

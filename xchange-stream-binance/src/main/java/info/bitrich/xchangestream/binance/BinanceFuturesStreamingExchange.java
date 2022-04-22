package info.bitrich.xchangestream.binance;

import com.google.common.base.MoreObjects;
import info.bitrich.xchangestream.binance.futures.*;
import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingAccountService;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingTradeService;
import info.bitrich.xchangestream.service.netty.ConnectionStateModel;
import info.bitrich.xchangestream.util.Events;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import org.knowm.xchange.ExchangeSharedParameters;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceAuthenticated;
import org.knowm.xchange.binance.futures.BinanceFuturesAdapter;
import org.knowm.xchange.binance.futures.BinanceFuturesAuthenticated;
import org.knowm.xchange.binance.BinanceFuturesExchange;
import org.knowm.xchange.binance.service.BinanceMarketDataService;
import org.knowm.xchange.client.ExchangeRestProxyBuilder;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BinanceFuturesStreamingExchange extends BinanceFuturesExchange implements StreamingExchange {
    private static final Logger LOG = LoggerFactory.getLogger(BinanceFuturesStreamingExchange.class);
    protected static final String USE_HIGHER_UPDATE_FREQUENCY =
            "Binance_Orderbook_Use_Higher_Frequency";

    private BinanceStreamingService streamingService;
    protected BinanceUserDataStreamingService userDataStreamingService;

    private BinanceFuturesStreamingMarketDataService streamingMarketDataService;
    private BinanceFuturesStreamingAccountService streamingAccountService;
    private BinanceFuturesStreamingTradeService streamingTradeService;

    private BinanceUserDataChannel userDataChannel;
    private Runnable onApiCall;
    private String orderBookUpdateFrequencyParameter = "";

    @Override
    protected void initServices() {
        super.initServices();
        this.onApiCall = Events.onApiCall(exchangeSpecification);
        Boolean userHigherFrequency =
                MoreObjects.firstNonNull(
                        (Boolean)
                                exchangeSpecification.getExchangeSpecificParametersItem(
                                        USE_HIGHER_UPDATE_FREQUENCY),
                        Boolean.FALSE);

        if (userHigherFrequency) {
            orderBookUpdateFrequencyParameter = "@100ms";
        }
    }

    @Override
    public ExchangeSpecification getDefaultExchangeSpecification() {
        ExchangeSpecification exchangeSpecification = super.getDefaultExchangeSpecification();
        exchangeSpecification.setStreamingUri("wss://fstream.binance.com");
        exchangeSpecification.setExchangeSpecificParametersItem(ExchangeSharedParameters.PARAM_SANDBOX_STREAMING_URI, "wss://stream.binancefuture.com");
        return exchangeSpecification;
    }

    /**
     * Binance streaming API expects connections to multiple channels to be defined at connection
     * time. To define the channels for this connection pass a `ProductSubscription` in at connection
     * time.
     *
     * @param args A single `ProductSubscription` to define the subscriptions required to be available
     *     during this connection.
     * @return A completable which fulfils once connection is complete.
     */
    @Override
    public Completable connect(ProductSubscription... args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Subscriptions must be made at connection time");
        }
        if (streamingService != null) {
            throw new UnsupportedOperationException(
                    "Exchange only handles a single connection - disconnect the current connection.");
        }

        final String apiUri = exchangeSpecification.getStreamingUri();

        ProductSubscription subscriptions = args[0];
        streamingService = createStreamingService(apiUri, subscriptions);

        List<Completable> completables = new ArrayList<>();

        if (subscriptions.hasUnauthenticated()) {
            completables.add(streamingService.connect());
        }

        if (subscriptions.hasAuthenticated()) {
            if (exchangeSpecification.getApiKey() == null) {
                throw new IllegalArgumentException("API key required for authenticated streams");
            }

            LOG.info("Connecting to authenticated web socket");
            BinanceAuthenticated binance =
                    ExchangeRestProxyBuilder.forInterface(
                                    BinanceFuturesAuthenticated.class, getExchangeSpecification())
                            .build();
            userDataChannel =
                    new BinanceUserDataChannel(binance, exchangeSpecification.getApiKey(), onApiCall);
            try {
                completables.add(createAndConnectUserDataService(apiUri, userDataChannel.getListenKey()));
            } catch (BinanceUserDataChannel.NoActiveChannelException e) {
                throw new IllegalStateException("Failed to establish user data channel", e);
            }
        }

        streamingMarketDataService =
                new BinanceFuturesStreamingMarketDataService(
                        streamingService,
                        (BinanceMarketDataService) marketDataService,
                        onApiCall,
                        orderBookUpdateFrequencyParameter);
        streamingAccountService = new BinanceFuturesStreamingAccountService(userDataStreamingService);
        streamingTradeService = new BinanceFuturesStreamingTradeService(this, userDataStreamingService);

        return Completable.concat(completables)
                .doOnComplete(() -> streamingMarketDataService.openSubscriptions(subscriptions))
                .doOnComplete(() -> streamingAccountService.openSubscriptions())
                .doOnComplete(() -> streamingTradeService.openSubscriptions());
    }

    private Completable createAndConnectUserDataService(String apiUri, String listenKey) {
        userDataStreamingService = BinanceFuturesUserDataStreamingService.create(apiUri, listenKey);
        return userDataStreamingService
                .connect()
                .doOnComplete(
                        () -> {
                            LOG.info("Connected to authenticated web socket");
                            userDataChannel.onChangeListenKey(
                                    newListenKey -> {
                                        userDataStreamingService
                                                .disconnect()
                                                .doOnComplete(
                                                        () -> {
                                                            createAndConnectUserDataService(apiUri, newListenKey)
                                                                    .doOnComplete(
                                                                            () -> {
                                                                                streamingAccountService.setUserDataStreamingService(
                                                                                        userDataStreamingService);
                                                                                streamingTradeService.setUserDataStreamingService(
                                                                                        userDataStreamingService);
                                                                            });
                                                        });
                                    });
                        });
    }

    @Override
    public Completable disconnect() {
        List<Completable> completables = new ArrayList<>();
        completables.add(streamingService.disconnect());
        streamingService = null;
        if (userDataStreamingService != null) {
            completables.add(userDataStreamingService.disconnect());
            userDataStreamingService = null;
        }
        if (userDataChannel != null) {
            userDataChannel.close();
            userDataChannel = null;
        }
        streamingMarketDataService = null;
        return Completable.concat(completables);
    }

    @Override
    public boolean isAlive() {
        return streamingService != null && streamingService.isSocketOpen();
    }

    @Override
    public Flowable<Throwable> reconnectFailure() {
        return streamingService.subscribeReconnectFailure();
    }

    @Override
    public Flowable<Object> connectionSuccess() {
        return streamingService.subscribeConnectionSuccess();
    }

    @Override
    public Flowable<ConnectionStateModel.State> connectionStateFlowable() {
        return streamingService.subscribeConnectionState();
    }

    @Override
    public BinanceFuturesStreamingMarketDataService getStreamingMarketDataService() {
        return streamingMarketDataService;
    }

    @Override
    public StreamingAccountService getStreamingAccountService() {
        return streamingAccountService;
    }

    @Override
    public StreamingTradeService getStreamingTradeService() {
        return streamingTradeService;
    }

    private BinanceStreamingService createStreamingService(String apiUri, ProductSubscription subscription) {
        return new BinanceFuturesStreamingService(apiUri + "/stream?streams=" + buildSubscriptionStreams(subscription), subscription);
    }

    public String buildSubscriptionStreams(ProductSubscription subscription) {
        return Stream.of(
                        buildSubscriptionStrings(subscription.getTicker(), BinanceSubscriptionType.TICKER.getType()),
                        buildSubscriptionStrings(subscription.getOrderBook(),  BinanceSubscriptionType.DEPTH.getType()),
                        buildSubscriptionStrings(subscription.getTrades(), BinanceSubscriptionType.TRADE.getType()))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("/"));
    }

    private String buildSubscriptionStrings(
            List<CurrencyPair> currencyPairs, String subscriptionType) {
        if (BinanceSubscriptionType.DEPTH.getType().equals(subscriptionType)) {
            return subscriptionStrings(currencyPairs)
                    .map(s -> s + "@" + subscriptionType + orderBookUpdateFrequencyParameter)
                    .collect(Collectors.joining("/"));
        } else {
            return subscriptionStrings(currencyPairs)
                    .map(s -> s + "@" + subscriptionType)
                    .collect(Collectors.joining("/"));
        }
    }

    private static Stream<String> subscriptionStrings(List<CurrencyPair> currencyPairs) {
        return currencyPairs.stream()
                .map(pair -> String.join("", pair.toString().split("/")).toLowerCase());
    }

    @Override
    public void useCompressedMessages(boolean compressedMessages) {
        streamingService.useCompressedMessages(compressedMessages);
    }

    public void enableLiveSubscription() {
        if (this.streamingService == null) {
            throw new UnsupportedOperationException("You must connect to streams before enabling live subscription.");
        }
        this.streamingService.enableLiveSubscription();
    }

    public void disableLiveSubscription() {
        if (this.streamingService != null) this.streamingService.disableLiveSubscription();
    }
}

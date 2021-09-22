package info.bitrich.xchangestream.coinbasepro.cache;

import info.bitrich.xchangestream.coinbasepro.dto.CoinbaseProChannelProducts;
import info.bitrich.xchangestream.coinbasepro.dto.CoinbaseProWebSocketTransaction;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import io.reactivex.rxjava3.processors.PublishProcessor;
import org.knowm.xchange.coinbasepro.CoinbaseProAdapters;
import org.knowm.xchange.coinbasepro.dto.trade.CoinbaseProOrder;
import org.knowm.xchange.coinbasepro.service.CoinbaseProMarketDataService;
import org.knowm.xchange.coinbasepro.service.CoinbaseProTradeService;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OpenOrdersCache {
    private static final Logger LOG = LoggerFactory.getLogger(OpenOrdersCache.class);
    private Map<String, ProductOpenOrders> products;
    private final CoinbaseProMarketDataService marketDataService;
    private final CoinbaseProTradeService tradeService;
    private boolean inited;
    private final FlowableProcessor<CoinbaseProOrder> orderUpdatePublisher;

    public OpenOrdersCache(CoinbaseProMarketDataService marketDataService, CoinbaseProTradeService tradeService) {
        this.marketDataService = marketDataService;
        this.tradeService = tradeService;
        products = new HashMap<>();
        inited = false;
        orderUpdatePublisher = PublishProcessor.<CoinbaseProOrder>create().toSerialized();
    }

    private void init(CoinbaseProChannelProducts[] channels) {
        this.products = new HashMap<>();
        for (CoinbaseProChannelProducts channel : channels) {
            if ("user".equals(channel.getName())) {
                for (String product_id : channel.getProduct_ids()) {
                    this.products.put(product_id, new ProductOpenOrders(product_id, marketDataService, tradeService));
                }
                break;
            }
        }
        inited = true;
    }

    public Flowable<CoinbaseProOrder> getOrderChanges() {
        return orderUpdatePublisher;
    }

    public synchronized void processWebSocketTransaction(CoinbaseProWebSocketTransaction transaction) {
        try {
            if ("subscriptions".equals(transaction.getType()) ||
                    "heartbeat".equals(transaction.getType()) ||
                    "received".equals(transaction.getType()) ||
                    "open".equals(transaction.getType()) ||
                    "done".equals(transaction.getType()) ||
                    "match".equals(transaction.getType()) && transaction.getUserId() != null) {

                if ("subscriptions".equals(transaction.getType())) {
                    init(transaction.getChannels());
                } else {
                    ProductOpenOrders cache = products.get(transaction.getProductId());
                    if (cache != null) {
                        cache.processWebSocketTransaction(transaction, orderUpdatePublisher);
                    }
                }
            }
        }catch(Exception e) {
            LOG.error("Exception during transaction - " + transaction.toString(), e);
        }
    }

    public synchronized boolean isInited() {
        if (!inited) {
            return false;
        }
        for (Map.Entry<String, ProductOpenOrders> p : products.entrySet()) {
            if (!p.getValue().isInited()) {
                return false;
            }
        }
        return true;
    }

    public synchronized OpenOrders getOpenOrders() {
        return CoinbaseProAdapters.adaptOpenOrders(
                products.values().stream()
                        .map(ProductOpenOrders::getCoinbaseProOpenOrders)
                        .toArray(CoinbaseProOrder[]::new));
    }

    public synchronized Order getOrder(String orderId) {
        return products.values().stream()
                .map(p -> p.getOrder(orderId))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }
}
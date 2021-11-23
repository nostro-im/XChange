package nostro.xchange.binance.sync;

import nostro.xchange.persistence.TransactionFactory;
import nostro.xchange.utils.NostroStreamingPublisher;
import org.knowm.xchange.binance.dto.account.BinanceAccountInformation;
import org.knowm.xchange.binance.dto.trade.BinanceOrder;
import org.knowm.xchange.binance.dto.trade.BinanceTrade;
import org.knowm.xchange.binance.service.BinanceAccountService;
import org.knowm.xchange.binance.service.BinanceTradeService;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

public class BinanceSyncService {
    private static final Logger LOG = LoggerFactory.getLogger(BinanceSyncService.class);
    
    private final long syncDelay;
    
    final TransactionFactory txFactory;
    final NostroStreamingPublisher publisher;

    private final BinanceAccountService accountService;
    private final BinanceTradeService tradeService;
    private final ScheduledExecutorService executor;

    private ScheduledFuture<?> scheduled = null;

    public BinanceSyncService(TransactionFactory txFactory,
                              NostroStreamingPublisher publisher,
                              BinanceAccountService accountService,
                              BinanceTradeService tradeService,
                              long syncDelay) {
        this.txFactory = txFactory;
        this.publisher = publisher;
        this.accountService = accountService;
        this.tradeService = tradeService;
        this.syncDelay = syncDelay;
        this.executor = Executors.newScheduledThreadPool(1);

        ((ScheduledThreadPoolExecutor) executor).setRemoveOnCancelPolicy(true);
    }

    public synchronized void init() throws Exception {
        LOG.info("BinanceSyncService starting initialization");
        doSync();
        
        if (scheduled == null) {
            scheduled = executor.scheduleWithFixedDelay(this::doSync2, syncDelay, syncDelay, TimeUnit.SECONDS);
            LOG.info("BinanceSyncService initialized, sync task scheduled with delay={} sec", syncDelay);
        }
    }

    public synchronized void connectionStateChanged(boolean connected) {
        if (connected) {
            if (scheduled == null) {
                scheduled = executor.scheduleWithFixedDelay(this::doSync2, 0, syncDelay, TimeUnit.SECONDS);
                LOG.info("BinanceSyncService connected, sync task scheduled");
            }
        } else {
            if (scheduled != null) {
                scheduled.cancel(false);
                scheduled = null;
                LOG.info("BinanceSyncService disconnected, sync task canceled");
            }
        }
    }
    
    private void doSync() throws Exception {
        try {
            new SyncAllTask(this).call();
        } catch (Throwable th) {
            LOG.error("Sync failed", th);
            throw th;
        }
    }

    private void doSync2() {
        try {
            doSync();
        } catch (Exception e) {
            // do nothing, error logged inside "executeSync"
        }
    }

    // BinanceAccountService API used in sync tasks
    BinanceAccountInformation getAccountInformation() throws IOException {
        try {
            return accountService.account();
        } catch (Throwable th) {
            LOG.error("Error while querying account information", th);
            throw th;
        }
    }
    
    // BinanceTradeService API used in sync tasks
    List<BinanceOrder> getOpenOrders(CurrencyPair pair) throws IOException {
        try {
            return tradeService.openOrders(pair);
        } catch (Throwable th) {
            LOG.error("Error while querying open orders", th);
            throw th;
        }
    }
    
    BinanceOrder getLastOrder(CurrencyPair pair) throws IOException {
        try {
            List<BinanceOrder> orders = tradeService.allOrders(pair, null, 1);
            return !orders.isEmpty() ? orders.get(0) : null;
        } catch (Throwable th) {
            LOG.error("Error while querying orders", th);
            throw th;
        }
    }
    
    BinanceTrade getFirstTrade(CurrencyPair pair, Long startTime) throws IOException {
        try {
            List<BinanceTrade> trades = tradeService.myTrades(pair, 1, startTime, null, null);
            return !trades.isEmpty() ? trades.get(0) : null;
        } catch (Throwable th) {
            LOG.error("Error while querying trades", th);
            throw th;
        }
    }

    BinanceOrder getOrder(CurrencyPair pair, long binanceId) throws IOException {
        try {
            return tradeService.orderStatus(pair, binanceId, null);
        } catch (Throwable th) {
            LOG.error("Error while querying order status", th);
            throw th;
        }
    }

    BinanceOrder getOrder(CurrencyPair pair, String id) throws IOException {
        try {
            return tradeService.orderStatus(pair, null, id);
        } catch (Throwable th) {
            String msg = th.getMessage();
            if (msg != null && msg.contains("Order does not exist")) {
                return null;
            }
            LOG.error("Error while querying order status", th);
            throw th;
        }
    }

    List<BinanceOrder> getOrders(CurrencyPair pair, long fromId, int limit) throws IOException {
        try {
            List<BinanceOrder> orders = tradeService.allOrders(pair, fromId, limit);
            LOG.info("Service returned {} orders", orders.size());
            return orders;
        } catch (Throwable th) {
            LOG.error("Error while querying orders", th);
            throw th;
        }
    }

    List<BinanceTrade> getTrades(CurrencyPair pair, long fromId, int limit) throws IOException {
        try {
            List<BinanceTrade> trades = tradeService.myTrades(pair, limit, null, null, fromId);
            LOG.info("Service returned {} trades", trades.size());
            return trades;
        } catch (Throwable th) {
            LOG.error("Error while querying trades", th);
            throw th;
        }
    }
}
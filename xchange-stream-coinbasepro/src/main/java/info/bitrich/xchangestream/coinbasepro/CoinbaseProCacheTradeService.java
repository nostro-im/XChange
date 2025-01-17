package info.bitrich.xchangestream.coinbasepro;

import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.*;
import org.knowm.xchange.exceptions.FundsExceededException;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.CancelOrderParams;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;
import org.knowm.xchange.service.trade.params.orders.OrderQueryParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class CoinbaseProCacheTradeService implements TradeService {
    private final TradeService tradeService;
    private final CoinbaseProStreamingService streamingService;

    public CoinbaseProCacheTradeService(TradeService tradeService, CoinbaseProStreamingService streamingService) {
        this.tradeService = tradeService;
        this.streamingService = streamingService;
    }

    @Override
    public OpenOrders getOpenOrders() throws IOException {
        if (this.streamingService.getCache().isInitiated()) {
            return this.streamingService.getCache().getOpenOrders();
        }
        return tradeService.getOpenOrders();
    }

    @Override
    public OpenOrdersParams createOpenOrdersParams() {
        return tradeService.createOpenOrdersParams();
    }

    @Override
    public OpenOrders getOpenOrders(OpenOrdersParams params) throws IOException {
        if (this.streamingService.getCache().isInitiated()) {
            return this.streamingService.getCache().getOpenOrders();
        }
        return tradeService.getOpenOrders(params);
    }

    @Override
    public String placeMarketOrder(MarketOrder marketOrder) throws IOException {
        String orderId = tradeService.placeMarketOrder(marketOrder);
        streamingService.getCache().addClientOrderId(orderId, marketOrder.getUserReference());
        return orderId;
    }

    @Override
    public String placeLimitOrder(LimitOrder limitOrder) throws IOException, FundsExceededException {
        String orderId = tradeService.placeLimitOrder(limitOrder);
        streamingService.getCache().addClientOrderId(orderId, limitOrder.getUserReference());
        return orderId;
    }

    @Override
    public String placeStopOrder(StopOrder stopOrder) throws IOException, FundsExceededException {
        String orderId = tradeService.placeStopOrder(stopOrder);
        streamingService.getCache().addClientOrderId(orderId, stopOrder.getUserReference());
        return orderId;
    }

    @Override
    public boolean cancelOrder(String orderId) throws IOException {
        return tradeService.cancelOrder(orderId);
    }

    @Override
    public boolean cancelOrder(CancelOrderParams orderParams) throws IOException {
        return tradeService.cancelOrder(orderParams);
    }

    @Override
    public UserTrades getTradeHistory(TradeHistoryParams params) throws IOException {
        return tradeService.getTradeHistory(params);
    }

    @Override
    public TradeHistoryParams createTradeHistoryParams() {
        return tradeService.createTradeHistoryParams();
    }

    @Override
    public Collection<Order> getOrder(String... orderIds) throws IOException {
        if (this.streamingService.getCache().isInitiated()) {
            Collection<Order> orders = new ArrayList<>(orderIds.length);

            for (String orderId : orderIds) {
                Order o = this.streamingService.getCache().getOrder(orderId);
                if (o != null) {
                    orders.add(o);
                } else {
                    orders.addAll(tradeService.getOrder(orderId));
                }
            }
            return orders;
        }
        return tradeService.getOrder(orderIds);
    }

    @Override
    public Collection<Order> getOrder(OrderQueryParams... orderQueryParams) throws IOException {
        return tradeService.getOrder(orderQueryParams);
    }
}

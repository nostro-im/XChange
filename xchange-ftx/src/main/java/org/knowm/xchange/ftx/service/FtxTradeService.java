package org.knowm.xchange.ftx.service;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.client.PlaceOrderLimiter;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.OpenPositions;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.ftx.FtxAdapters;
import org.knowm.xchange.ftx.FtxAuthenticated;
import org.knowm.xchange.ftx.dto.account.FtxAccountDto;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.CancelOrderParams;
import org.knowm.xchange.service.trade.params.DefaultTradeHistoryParamInstrument;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamInstrument;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

public class FtxTradeService extends FtxTradeServiceRaw implements TradeService {
  private static final Logger LOG = LoggerFactory.getLogger(FtxTradeService.class);
  
  private static final int DEF_PLACE_LIMIT = 2;
  private static final long DEF_PLACE_SLEEP = 200;
  private static final long DEF_PLACE_MAX_SLEEP = 10_000;
  
  private final PlaceOrderLimiter placeOrderLimiter;

  public FtxTradeService(Exchange exchange, FtxAuthenticated ftx) {
    super(exchange, ftx);
    
    this.placeOrderLimiter = PlaceOrderLimiter.fromSpecificParams(
            exchange.getExchangeSpecification().getExchangeSpecificParameters(),
            DEF_PLACE_LIMIT,
            DEF_PLACE_SLEEP,
            DEF_PLACE_MAX_SLEEP);
    
    LOG.info("Created {}", placeOrderLimiter);
  }

  @Override
  public String placeMarketOrder(MarketOrder marketOrder) throws IOException {
    return placeOrderLimiter.executePlace(
        () ->
            placeMarketOrderForSubaccount(
                exchange.getExchangeSpecification().getUserName(), marketOrder));
  }

  @Override
  public String placeLimitOrder(LimitOrder limitOrder) throws IOException {
    return placeOrderLimiter.executePlace(
        () ->
            placeLimitOrderForSubaccount(
                exchange.getExchangeSpecification().getUserName(), limitOrder));
  }

  @Override
  public UserTrades getTradeHistory(TradeHistoryParams params) throws IOException {
    return getTradeHistoryForSubaccount(exchange.getExchangeSpecification().getUserName(), params);
  }

  @Override
  public TradeHistoryParams createTradeHistoryParams() {
    return new DefaultTradeHistoryParamInstrument();
  }

  @Override
  public boolean cancelOrder(String orderId) throws IOException {
    return cancelOrderForSubaccount(exchange.getExchangeSpecification().getUserName(), orderId);
  }

  @Override
  public boolean cancelOrder(CancelOrderParams orderParams) throws IOException {
    return cancelOrderForSubaccount(exchange.getExchangeSpecification().getUserName(), orderParams);
  }

  @Override
  public Collection<Order> getOrder(String... orderIds) throws IOException {
    return getOrderFromSubaccount(exchange.getExchangeSpecification().getUserName(), orderIds);
  }

  @Override
  public OpenOrders getOpenOrders(OpenOrdersParams params) throws IOException {
    return getOpenOrdersForSubaccount(exchange.getExchangeSpecification().getUserName(), params);
  }

  @Override
  public OpenOrdersParams createOpenOrdersParams() {
    return new DefaultOpenOrdersParamInstrument();
  }

  @Override
  public OpenOrders getOpenOrders() throws IOException {
    return getOpenOrdersForSubaccount(exchange.getExchangeSpecification().getUserName());
  }

  @Override
  public OpenPositions getOpenPositions() throws IOException {
    String subaccount = exchange.getExchangeSpecification().getUserName();
    FtxAccountDto accountDto = ((FtxAccountService) exchange.getAccountService())
            .getFtxAccountInformation(subaccount)
            .getResult();
    return getOpenPositionsForSubaccount(subaccount, accountDto, false);
  }

  @Override
  public String changeOrder(LimitOrder limitOrder) throws IOException {
    if (limitOrder.getUserReference() != null) {
      return modifyFtxOrderByClientId(
              exchange.getExchangeSpecification().getUserName(),
              limitOrder.getUserReference(),
              FtxAdapters.adaptModifyOrderToFtxOrderPayload(limitOrder))
          .getResult()
          .getClientId();
    } else {
      return modifyFtxOrder(
              exchange.getExchangeSpecification().getUserName(),
              limitOrder.getId(),
              FtxAdapters.adaptModifyOrderToFtxOrderPayload(limitOrder))
          .getResult()
          .getId();
    }
  }
}

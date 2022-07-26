package org.knowm.xchange.ftx.service;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.client.PlaceOrderLimiter;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.OpenPositions;
import org.knowm.xchange.dto.trade.*;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.ftx.FtxAdapters;
import org.knowm.xchange.ftx.FtxAuthenticated;
import org.knowm.xchange.ftx.dto.FtxResponse;
import org.knowm.xchange.ftx.dto.account.FtxAccountDto;
import org.knowm.xchange.ftx.dto.trade.FtxConditionalOrderDto;
import org.knowm.xchange.ftx.dto.trade.FtxOrderDto;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.CancelOrderParams;
import org.knowm.xchange.service.trade.params.DefaultOrderHistoryParamsInstrumentSpan;
import org.knowm.xchange.service.trade.params.InstrumentParam;
import org.knowm.xchange.service.trade.params.OrderHistoryParams;
import org.knowm.xchange.service.trade.params.OrderHistoryParamsTimeSpan;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamInstrument;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;
import org.knowm.xchange.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
  public String placeStopOrder(StopOrder stopOrder) throws IOException {
    return placeStopOrderForSubAccount(exchange.getExchangeSpecification().getUserName(), stopOrder);
  }

  @Override
  public UserTrades getTradeHistory(TradeHistoryParams params) throws IOException {
    return getTradeHistoryForSubaccount(exchange.getExchangeSpecification().getUserName(), params);
  }

  @Override
  public TradeHistoryParams createTradeHistoryParams() {
    return new FtxTradeHistoryParams();
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
  
  @Override
  public List<Order> getOrderHistory(OrderHistoryParams params) throws IOException {
    Assert.isTrue(params instanceof InstrumentParam,"You need to provide instrument to get order history");
    Instrument instrument = ((InstrumentParam) params).getInstrument();
    if (instrument == null) {
      throw new ExchangeException("You need to provide instrument to get order history");
    }
    String market = FtxAdapters.adaptInstrumentToFtxMarket(instrument);

    Integer startTime = null, endTime = null;
    if (params instanceof OrderHistoryParamsTimeSpan) {
      startTime = FtxAdapters.adaptDate(((OrderHistoryParamsTimeSpan) params).getStartTime());
      endTime = FtxAdapters.adaptDate(((OrderHistoryParamsTimeSpan) params).getEndTime());
    }

    String subaccount = exchange.getExchangeSpecification().getUserName();
    FtxResponse<List<FtxOrderDto>> orderResponse = getFtxOrderHistory(subaccount, market, startTime, endTime);
    FtxResponse<List<FtxConditionalOrderDto>> conditionalResponse = getFtxConditionalOrderHistory(subaccount, market, startTime, endTime);

    List<FtxOrderDto> orders = orderResponse.getResult();
    List<FtxConditionalOrderDto> conditionalOrders = conditionalResponse.getResult();

    if (!orders.isEmpty() && !conditionalOrders.isEmpty()) { // both order types have elements
      if (orderResponse.isHasMoreData() || conditionalResponse.isHasMoreData()) { // at least one has paging active
        
        // history is in desc order 
        Date d1 = orders.get(orders.size() - 1).getCreatedAt(); // last order created date
        Date d2 = conditionalOrders.get(conditionalOrders.size() - 1).getCreatedAt(); // last conditional order created date
        
        if (d2.before(d1) && orderResponse.isHasMoreData()) {
          // cut conditional history until d1
          conditionalOrders = conditionalOrders.stream()
                  .filter(o -> !d1.before(o.getCreatedAt()))
                  .collect(Collectors.toList());
          
        } else if (d1.before(d2) && conditionalResponse.isHasMoreData()) {
          // cut order history until d2
          orders = orders.stream()
                  .filter(o -> !d2.before(o.getCreatedAt()))
                  .collect(Collectors.toList());
        }
      }
    }
    
    return FtxAdapters.adaptOrderHistory(orders, conditionalOrders);
  }
  
  @Override
  public OrderHistoryParams createOrderHistoryParams() {
  	return new DefaultOrderHistoryParamsInstrumentSpan();
  }
}

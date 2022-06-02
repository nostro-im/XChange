package org.knowm.xchange.ftx.service;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.OpenPositions;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.ftx.FtxAdapters;
import org.knowm.xchange.ftx.FtxAuthenticated;
import org.knowm.xchange.ftx.FtxException;
import org.knowm.xchange.ftx.dto.FtxResponse;
import org.knowm.xchange.ftx.dto.account.FtxAccountDto;
import org.knowm.xchange.ftx.dto.account.FtxPositionDto;
import org.knowm.xchange.ftx.dto.trade.*;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.service.trade.params.*;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParams;
import org.knowm.xchange.utils.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class FtxTradeServiceRaw extends FtxBaseService {

  public FtxTradeServiceRaw(Exchange exchange, FtxAuthenticated ftx) {
    super(exchange, ftx);
  }

  public String placeMarketOrderForSubaccount(String subaccount, MarketOrder marketOrder)
      throws IOException {
    return placeNewFtxOrder(subaccount, FtxAdapters.adaptMarketOrderToFtxOrderPayload(marketOrder))
        .getResult()
        .getId();
  }

  public String placeLimitOrderForSubaccount(String subaccount, LimitOrder limitOrder)
      throws IOException {
    return placeNewFtxOrder(subaccount, FtxAdapters.adaptLimitOrderToFtxOrderPayload(limitOrder))
        .getResult()
        .getId();
  }

  public FtxResponse<FtxOrderDto> placeNewFtxOrder(
      String subaccount, FtxOrderRequestPayload payload) throws FtxException, IOException {
    try {
      return ftx.placeOrder(
          exchange.getExchangeSpecification().getApiKey(),
          exchange.getNonceFactory().createValue(),
          signatureCreator,
          subaccount,
          payload);
    } catch (FtxException e) {
      throw new FtxException(e.getMessage());
    }
  }

  public FtxResponse<FtxOrderDto> modifyFtxOrder(
      String subaccount, String orderId, FtxModifyOrderRequestPayload payload)
      throws FtxException, IOException {

    return ftx.modifyOrder(
          exchange.getExchangeSpecification().getApiKey(),
          exchange.getNonceFactory().createValue(),
          signatureCreator,
          subaccount,
          orderId,
          payload);
  }

  public FtxResponse<FtxOrderDto> modifyFtxOrderByClientId(
      String subaccount, String clientId, FtxModifyOrderRequestPayload payload)
      throws FtxException, IOException {

    return ftx.modifyOrder(
          exchange.getExchangeSpecification().getApiKey(),
          exchange.getNonceFactory().createValue(),
          signatureCreator,
          subaccount,
          clientId,
          payload);
  }

  public boolean cancelOrderForSubaccount(String subaccount, String orderId) throws IOException {
    return cancelFtxOrder(subaccount, orderId);
  }

  public boolean cancelFtxOrder(String subaccount, String orderId)
      throws FtxException, IOException {
    try {
      return ftx.cancelOrder(
              exchange.getExchangeSpecification().getApiKey(),
              exchange.getNonceFactory().createValue(),
              signatureCreator,
              subaccount,
              orderId)
          .isSuccess();
    } catch (FtxException e) {
      throw new FtxException(e.getMessage());
    }
  }

  public boolean cancelFtxByClientId(String subaccount, String clientId)
      throws FtxException, IOException {
    try {
      return ftx.cancelOrderByClientId(
              exchange.getExchangeSpecification().getApiKey(),
              exchange.getNonceFactory().createValue(),
              signatureCreator,
              subaccount,
              clientId)
          .isSuccess();
    } catch (FtxException e) {
      throw new FtxException(e.getMessage());
    }
  }

  public boolean cancelOrderForSubaccount(String subaccount, CancelOrderParams orderParams)
      throws IOException {
    if (orderParams instanceof CancelOrderByCurrencyPair) {
      return cancelAllFtxOrders(
          subaccount,
          new CancelAllFtxOrdersParams(
              FtxAdapters.adaptInstrumentToFtxMarket(
                  ((CancelOrderByCurrencyPair) orderParams).getCurrencyPair())));
    } else if (orderParams instanceof CancelOrderByUserReferenceParams) {
      return cancelFtxByClientId(
          subaccount, ((CancelOrderByUserReferenceParams) orderParams).getUserReference());
    } else {
      throw new IOException(
          "CancelOrderParams must implement CancelOrderByCurrencyPair interface.");
    }
  }

  public boolean cancelAllFtxOrders(String subaccount, CancelAllFtxOrdersParams payLoad)
      throws FtxException, IOException {
    try {
      return ftx.cancelAllOrders(
              exchange.getExchangeSpecification().getApiKey(),
              exchange.getNonceFactory().createValue(),
              signatureCreator,
              subaccount,
              payLoad)
          .isSuccess();
    } catch (FtxException e) {
      throw new FtxException(e.getMessage());
    }
  }

  public Collection<Order> getOrderFromSubaccount(String subaccount, String... orderIds)
      throws IOException {
    List<Order> orderList = new ArrayList<>();
    for (String orderId : orderIds) {
      Order order = FtxAdapters.adaptOrder(getFtxOrderStatus(subaccount, orderId).getResult());
      orderList.add(order);
    }
    return orderList;
  }

  public FtxResponse<List<FtxOrderDto>> getFtxOpenOrders(String subaccount, String market)
      throws FtxException, IOException {
    try {
      return ftx.openOrders(
          exchange.getExchangeSpecification().getApiKey(),
          exchange.getNonceFactory().createValue(),
          signatureCreator,
          subaccount,
          market);
    } catch (FtxException e) {
      throw new FtxException(e.getMessage());
    }
  }

  public UserTrades getTradeHistoryForSubaccount(String subaccount, TradeHistoryParams params)
      throws IOException {
    Instrument instrument;
    if (params instanceof CurrencyPairParam) {
      instrument = ((CurrencyPairParam) params).getCurrencyPair();
    } else if (params instanceof InstrumentParam) {
      instrument = ((InstrumentParam) params).getInstrument();
    } else {
      throw new IOException(
          "TradeHistoryParams must implement TradeHistoryParamCurrencyPair or TradeHistoryParamInstrument interface.");
    }

    Date startTime = null, endTime = null;
    if (params instanceof TradeHistoryParamsTimeSpan) {
      startTime = ((TradeHistoryParamsTimeSpan) params).getStartTime();
      endTime = ((TradeHistoryParamsTimeSpan) params).getEndTime();
    }

    String orderId = null;
    if (params instanceof TradeHistoryParamOrder) {
      orderId =((TradeHistoryParamOrder) params).getOrderId();
    }

    return FtxAdapters.adaptUserTradesFromTrades(
            getFtxTradeHistory(subaccount,
                    FtxAdapters.adaptInstrumentToFtxMarket(instrument), 
                    FtxAdapters.adaptDate(startTime),
                    FtxAdapters.adaptDate(endTime),
                    orderId).getResult());
  }

  public FtxResponse<List<FtxOrderDto>> getFtxOrderHistory(String subaccount, String market, Integer startTime, Integer endTime)
      throws FtxException, IOException {
    try {
      return ftx.orderHistory(
          exchange.getExchangeSpecification().getApiKey(),
          exchange.getNonceFactory().createValue(),
          signatureCreator,
          subaccount,
          market,
          startTime,
          endTime);
    } catch (FtxException e) {
      throw new FtxException(e.getMessage());
    }
  }

  public FtxResponse<List<FtxUserTradeDto>> getFtxTradeHistory(String subaccount, String market, Integer startTime, Integer endTime, String orderId)
          throws FtxException, IOException {
    try {
      return ftx.fills(
              exchange.getExchangeSpecification().getApiKey(),
              exchange.getNonceFactory().createValue(),
              signatureCreator,
              subaccount,
              market,
              startTime,
              endTime,
              "asc",
              orderId);
    } catch (FtxException e) {
      throw new FtxException(e.getMessage());
    }
  }

  public OpenOrders getOpenOrdersForSubaccount(String subaccount) throws IOException {
    return FtxAdapters.adaptOpenOrders(getFtxAllOpenOrdersForSubaccount(subaccount));
  }

  public OpenOrders getOpenOrdersForSubaccount(String subaccount, OpenOrdersParams params)
      throws IOException {
    Instrument instrument;
    if (params instanceof CurrencyPairParam) {
      instrument = ((CurrencyPairParam) params).getCurrencyPair();
    } else if (params instanceof InstrumentParam) {
      instrument = ((InstrumentParam) params).getInstrument();
    } else {
      throw new IOException(
          "OpenOrdersParams must implement CurrencyPairParam or OpenOrdersParamInstrument interface.");
    }
    return FtxAdapters.adaptOpenOrders(
        getFtxOpenOrders(subaccount, FtxAdapters.adaptInstrumentToFtxMarket(instrument)));
  }

  public FtxResponse<List<FtxOrderDto>> getFtxAllOpenOrdersForSubaccount(String subaccount)
      throws FtxException, IOException {
    return getFtxOpenOrders(subaccount, null);
  }

  public FtxResponse<FtxOrderDto> getFtxOrderStatus(String subaccount, String orderId)
      throws FtxException, IOException {
    try {
      return ftx.getOrderStatus(
          exchange.getExchangeSpecification().getApiKey(),
          exchange.getNonceFactory().createValue(),
          signatureCreator,
          subaccount,
          orderId);
    } catch (FtxException e) {
      throw new FtxException(e.getMessage());
    }
  }

  public FtxResponse<FtxOrderDto> getFtxOrderStatusByClientId(String subaccount, String clientId)
          throws FtxException, IOException {
    try {
      return ftx.getOrderStatusByClientId(
              exchange.getExchangeSpecification().getApiKey(),
              exchange.getNonceFactory().createValue(),
              signatureCreator,
              subaccount,
              clientId);
    } catch (FtxException e) {
      throw new FtxException(e.getMessage());
    }
  }

  public OpenPositions getOpenPositionsForSubaccount(String subaccount, FtxAccountDto accountDto, boolean showAvgPrice) throws IOException {
    List<FtxPositionDto> positionDtos = getFtxPositions(subaccount, showAvgPrice).getResult();
    return FtxAdapters.adaptOpenPositions(accountDto, positionDtos);
  }

  public FtxResponse<List<FtxPositionDto>> getFtxPositions(String subaccount, boolean showAvgPrice)
      throws FtxException, IOException {
    try {
      return ftx.getFtxPositions(
          exchange.getExchangeSpecification().getApiKey(),
          exchange.getNonceFactory().createValue(),
          signatureCreator,
          subaccount,
          showAvgPrice);
    } catch (FtxException e) {
      throw new FtxException(e.getMessage());
    }
  }
}

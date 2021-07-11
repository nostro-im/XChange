package org.knowm.xchange.deribit.v2.service;

import java.io.IOException;

import org.knowm.xchange.deribit.v2.DeribitAdapters;
import org.knowm.xchange.deribit.v2.DeribitExchange;
import org.knowm.xchange.deribit.v2.dto.trade.Order;
import org.knowm.xchange.deribit.v2.dto.trade.OrderState;
import org.knowm.xchange.derivative.OptionContract;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.knowm.xchange.service.trade.TradeService;

public class DeribitTradeService extends DeribitTradeServiceRaw implements TradeService {

  public DeribitTradeService(DeribitExchange exchange) {
    super(exchange);
  }

  @Override
  public boolean cancelOrder(String orderId) throws IOException {
    Order cancel = super.cancel(orderId);
    return cancel.getOrderState() == OrderState.cancelled;
  }

  @Override
  public String placeMarketOrder(MarketOrder marketOrder) throws IOException {
    if (marketOrder.getType() == OrderType.BID) {
      return buy(DeribitAdapters.adaptInstrumentName((OptionContract) marketOrder.getInstrument()),
              marketOrder.getOriginalAmount(),
              org.knowm.xchange.deribit.v2.dto.trade.OrderType.market,
              marketOrder.getUserReference(),
              null, // market
              null,
              null,
              null,
              null,
              null,
              null,
              null
      ).getOrder().getOrderId();
    } else if (marketOrder.getType() == OrderType.ASK) {
      return sell(DeribitAdapters.adaptInstrumentName((OptionContract) marketOrder.getInstrument()),
              marketOrder.getOriginalAmount(),
              org.knowm.xchange.deribit.v2.dto.trade.OrderType.market,
              marketOrder.getUserReference(),
              null, // market
              null,
              null,
              null,
              null,
              null,
              null,
              null
      ).getOrder().getOrderId();
    } else {
      throw new NotYetImplementedForExchangeException(
              "placeMarketOrder not implemented for OrderType: " + marketOrder.getType());
    }
  }
}

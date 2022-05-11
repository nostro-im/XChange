package org.knowm.xchange.service.trade.params;

import org.knowm.xchange.instrument.Instrument;

public class DefaultTradeHistoryParamInstrument implements TradeHistoryParamInstrument, TradeHistoryParamOrder {

  private Instrument instrument;
  private String orderId;

  public DefaultTradeHistoryParamInstrument() {}

  public DefaultTradeHistoryParamInstrument(Instrument instrument) {
    this.instrument = instrument;
  }

  @Override
  public Instrument getInstrument() {
    return instrument;
  }

  @Override
  public void setInstrument(final Instrument instrument) {
    this.instrument = instrument;
  }

  @Override
  public String getOrderId() {
    return orderId;
  }

  @Override
  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }
}

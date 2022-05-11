package org.knowm.xchange.service.trade.params;

import java.util.Collection;
import java.util.Collections;
import org.knowm.xchange.instrument.Instrument;

public class DefaultTradeHistoryParamMultiInstrument implements TradeHistoryParamMultiInstrument, TradeHistoryParamOrder {

  private Collection<Instrument> instruments = Collections.emptySet();
  private String orderId;

  public DefaultTradeHistoryParamMultiInstrument() {}

  public DefaultTradeHistoryParamMultiInstrument(final Collection<Instrument> instruments) {
    this.instruments = instruments;
  }

  @Override
  public Collection<Instrument> getInstruments() {
    return instruments;
  }

  @Override
  public void setInstruments(final Collection<Instrument> instruments) {
    this.instruments = instruments;
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

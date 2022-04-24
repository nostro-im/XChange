package org.knowm.xchange.service.trade.params;

import java.util.Collection;
import java.util.Collections;
import org.knowm.xchange.instrument.Instrument;

public class DefaultTradeHistoryParamMultiInstrument implements TradeHistoryParamMultiInstrument, TradeHistoryParamOrder {

  private Collection<Instrument> instruments = Collections.emptySet();
  private String id;

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
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }
}

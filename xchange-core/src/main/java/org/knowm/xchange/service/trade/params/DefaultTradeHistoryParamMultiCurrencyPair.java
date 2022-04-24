package org.knowm.xchange.service.trade.params;

import java.util.Collection;
import java.util.Collections;
import org.knowm.xchange.currency.CurrencyPair;

public class DefaultTradeHistoryParamMultiCurrencyPair
    implements TradeHistoryParamMultiCurrencyPair, TradeHistoryParamOrder {

  private Collection<CurrencyPair> pairs = Collections.emptySet();
  private String id;

  @Override
  public Collection<CurrencyPair> getCurrencyPairs() {
    return pairs;
  }

  @Override
  public void setCurrencyPairs(Collection<CurrencyPair> value) {
    pairs = value;
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

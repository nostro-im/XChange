package org.knowm.xchange.service.trade.params;

import org.knowm.xchange.currency.CurrencyPair;

public class DefaultTradeHistoryParamCurrencyPair implements TradeHistoryParamCurrencyPair, TradeHistoryParamOrder {

  private CurrencyPair pair;
  private String id;

  public DefaultTradeHistoryParamCurrencyPair() {}

  public DefaultTradeHistoryParamCurrencyPair(CurrencyPair pair) {
    this.pair = pair;
  }

  @Override
  public CurrencyPair getCurrencyPair() {

    return pair;
  }

  @Override
  public void setCurrencyPair(CurrencyPair pair) {

    this.pair = pair;
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

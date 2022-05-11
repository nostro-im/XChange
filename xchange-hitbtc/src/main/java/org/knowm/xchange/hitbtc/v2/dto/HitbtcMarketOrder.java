package org.knowm.xchange.hitbtc.v2.dto;

import java.math.BigDecimal;
import java.util.Date;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.trade.MarketOrder;

public class HitbtcMarketOrder extends MarketOrder {
  public final String clientOrderId;

  public HitbtcMarketOrder(
      OrderType type,
      BigDecimal originalAmount,
      CurrencyPair currencyPair,
      String id,
      Date timestamp,
      BigDecimal averagePrice,
      BigDecimal cumulativeAmount,
      BigDecimal fee,
      OrderStatus status,
      String clientOrderId, Currency feeCurrency) {
    super(
        type,
        originalAmount,
        currencyPair,
        id,
        timestamp,
        averagePrice,
        cumulativeAmount,
        fee,
        status,
        feeCurrency);
    this.clientOrderId = clientOrderId;
  }

  public HitbtcMarketOrder(
      OrderType type,
      BigDecimal originalAmount,
      CurrencyPair currencyPair,
      String id,
      Date timestamp,
      String clientOrderId) {
    super(type, originalAmount, currencyPair, id, timestamp);
    this.clientOrderId = clientOrderId;
  }

  public HitbtcMarketOrder(
      OrderType type,
      BigDecimal originalAmount,
      CurrencyPair currencyPair,
      Date timestamp,
      String clientOrderId) {
    super(type, originalAmount, currencyPair, timestamp);
    this.clientOrderId = clientOrderId;
  }

  public HitbtcMarketOrder(
      OrderType type, BigDecimal originalAmount, CurrencyPair currencyPair, String clientOrderId) {
    super(type, originalAmount, currencyPair);
    this.clientOrderId = clientOrderId;
  }

  public String getClientOrderId() {
    return clientOrderId;
  }
}

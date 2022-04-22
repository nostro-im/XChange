package info.bitrich.xchangestream.binance.futures.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import info.bitrich.xchangestream.binance.dto.BaseBinanceWebSocketTransaction;
import org.knowm.xchange.binance.BinanceAdapters;
import org.knowm.xchange.binance.futures.BinanceFuturesAdapter;
import org.knowm.xchange.binance.futures.dto.trade.BinanceFuturesOrder;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.UserTrade;

public class OrderTradeUpdateBinanceUserTransaction extends BaseBinanceWebSocketTransaction {

  protected final long transactionTime;
  protected final OrderTradeUpdate orderTradeUpdate;

  public OrderTradeUpdateBinanceUserTransaction(
      @JsonProperty("e") String eventType,
      @JsonProperty("E") String eventTime,
      @JsonProperty("T") long transactionTime,
      @JsonProperty("o") OrderTradeUpdate orderTradeUpdate) {
    super(eventType, eventTime);
    this.transactionTime = transactionTime;
    this.orderTradeUpdate = orderTradeUpdate;
  }

  public OrderTradeUpdate getOrderTradeUpdate() {
    return orderTradeUpdate;
  }

  public long getTransactionTime() {
    return transactionTime;
  }
}

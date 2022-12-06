package info.bitrich.xchangestream.binance.dto;

import java.math.BigDecimal;

import org.knowm.xchange.binance.BinanceAdapters;
import org.knowm.xchange.binance.dto.marketdata.BinanceKline;
import org.knowm.xchange.binance.dto.marketdata.KlineInterval;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BinanceWebSocketKline {
  private final BinanceKline kline;
  
  public BinanceWebSocketKline(
      @JsonProperty("t") long startTime,
      @JsonProperty("T") long closeTime,
      @JsonProperty("s") String symbol,
      @JsonProperty("i") String interval,
      @JsonProperty("f") long firstTradeId,
      @JsonProperty("L") long lastTradeId,
      @JsonProperty("o") BigDecimal open,
      @JsonProperty("c") BigDecimal close,
      @JsonProperty("h") BigDecimal high,
      @JsonProperty("l") BigDecimal low,
      @JsonProperty("v") BigDecimal volume,
      @JsonProperty("n") long numTrades,
      @JsonProperty("x") boolean isClose,
      @JsonProperty("q") BigDecimal quoteVolume,
      @JsonProperty("V") BigDecimal takerVolume,
      @JsonProperty("Q") BigDecimal takerQuoteVolume,
      @JsonProperty("B") String ignore) {
    kline = new BinanceKline(
	BinanceAdapters.adaptSymbol(symbol),
        KlineInterval.getFromCode(interval),
        startTime,
        open,
        high,
        low,
        close,
        volume,
        closeTime,
        null, // updated timestamp to be assigned by CandleStickBinanceWebSocketTransaction
        isClose,
        quoteVolume,
        numTrades,
        takerVolume,
        takerQuoteVolume);
  }
  
  public BinanceKline getKline() {
    return kline;
  }
}
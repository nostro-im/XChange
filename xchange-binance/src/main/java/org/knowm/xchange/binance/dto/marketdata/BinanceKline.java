package org.knowm.xchange.binance.dto.marketdata;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import org.knowm.xchange.currency.CurrencyPair;

public final class BinanceKline {

  private final CurrencyPair pair;
  private final KlineInterval interval;
  private final long openTime;
  private final BigDecimal open;
  private final BigDecimal high;
  private final BigDecimal low;
  private final BigDecimal close;
  private final BigDecimal volume;
  private final long closeTime;
  private final Long lastUpdated;
  private final boolean isClosed;
  private final BigDecimal quoteAssetVolume;
  private final long numberOfTrades;
  private final BigDecimal takerBuyBaseAssetVolume;
  private final BigDecimal takerBuyQuoteAssetVolume;
  
  /**
   * C'tor for REST API
   * @param pair
   * @param interval
   * @param obj
   */
  public BinanceKline(CurrencyPair pair, KlineInterval interval, Object[] obj) {
    this.pair = pair;
    this.interval = interval;
    this.openTime = Long.valueOf(obj[0].toString());
    this.open = new BigDecimal(obj[1].toString());
    this.high = new BigDecimal(obj[2].toString());
    this.low = new BigDecimal(obj[3].toString());
    this.close = new BigDecimal(obj[4].toString());
    this.volume = new BigDecimal(obj[5].toString());
    this.closeTime = Long.valueOf(obj[6].toString());
    this.lastUpdated = this.closeTime;
    this.isClosed = true;
    this.quoteAssetVolume = new BigDecimal(obj[7].toString());
    this.numberOfTrades = Long.valueOf(obj[8].toString());
    this.takerBuyBaseAssetVolume = new BigDecimal(obj[9].toString());
    this.takerBuyQuoteAssetVolume = new BigDecimal(obj[10].toString());
  }
  
  /**
   * C'tor for WebSocket (i.e., supports continuous candlesticks)
   * @param pair
   * @param interval
   * @param openTime
   * @param open
   * @param high
   * @param low
   * @param close
   * @param volume
   * @param closeTime
   * @param lastUpdated
   * @param isClosed
   * @param quoteVolume
   * @param numTrades
   * @param takerVolume
   * @param takerQuoteVolume
   */
  public BinanceKline(
      CurrencyPair pair,
      KlineInterval interval,
      long openTime,
      BigDecimal open,
      BigDecimal high,
      BigDecimal low,
      BigDecimal close,
      BigDecimal volume,
      long closeTime,
      Long lastUpdated,
      boolean isClosed,
      BigDecimal quoteVolume,
      long numTrades,
      BigDecimal takerVolume,
      BigDecimal takerQuoteVolume) {
    this.pair = pair;
    this.interval = interval;
    this.openTime = openTime;
    this.open = open;
    this.high = high;
    this.low = low;
    this.close = close;
    this.volume = volume;
    this.closeTime = closeTime;
    this.lastUpdated = lastUpdated;
    this.isClosed = isClosed;
    this.quoteAssetVolume = quoteVolume;
    this.numberOfTrades = numTrades;
    this.takerBuyBaseAssetVolume = takerVolume;
    this.takerBuyQuoteAssetVolume = takerQuoteVolume;
  }
  

  public CurrencyPair getCurrencyPair() {
    return pair;
  }

  public KlineInterval getInterval() {
    return interval;
  }

  public long getOpenTime() {
    return openTime;
  }

  public BigDecimal getOpenPrice() {
    return open;
  }

  public BigDecimal getHighPrice() {
    return high;
  }

  public BigDecimal getLowPrice() {
    return low;
  }

  public BigDecimal getAveragePrice() {
    return low.add(high).divide(new BigDecimal("2"));
  }

  public BigDecimal getClosePrice() {
    return close;
  }

  public BigDecimal getVolume() {
    return volume;
  }

  public long getCloseTime() {
    return closeTime;
  }

  public Long getLastUpdated() {
    return lastUpdated;
  }
  
  public boolean isClosed() {
    return isClosed;
  }
  
  public BigDecimal getQuoteAssetVolume() {
    return quoteAssetVolume;
  }

  public long getNumberOfTrades() {
    return numberOfTrades;
  }

  public BigDecimal getTakerBuyBaseAssetVolume() {
    return takerBuyBaseAssetVolume;
  }

  public BigDecimal getTakerBuyQuoteAssetVolume() {
    return takerBuyQuoteAssetVolume;
  }

  public String toString() {
    String tstamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(openTime);
    return String.format(
        "[%s] %s %s O:%.6f A:%.6f C:%.6f", pair, tstamp, interval, open, getAveragePrice(), close);
  }
}

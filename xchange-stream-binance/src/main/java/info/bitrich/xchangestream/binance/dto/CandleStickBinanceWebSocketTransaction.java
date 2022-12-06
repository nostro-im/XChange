package info.bitrich.xchangestream.binance.dto;

import org.knowm.xchange.binance.dto.marketdata.BinanceKline;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CandleStickBinanceWebSocketTransaction extends ProductBinanceWebSocketTransaction {

  private final BinanceKline kline;

  public CandleStickBinanceWebSocketTransaction(
      @JsonProperty("e") String eventType,
      @JsonProperty("E") String eventTime,
      @JsonProperty("s") String symbol,
      @JsonProperty("k") BinanceWebSocketKline kline) {
    super(eventType, eventTime, symbol);
    this.kline = new BinanceKline(
    		kline.getKline().getCurrencyPair(),
    		kline.getKline().getInterval(),
    		kline.getKline().getOpenTime(),
    		kline.getKline().getOpenPrice(),
    		kline.getKline().getHighPrice(),
    		kline.getKline().getLowPrice(),
    		kline.getKline().getClosePrice(),
    		kline.getKline().getVolume(),
    		kline.getKline().getCloseTime(),
    		Long.parseLong(eventTime), // Fill the only missing field
    		kline.getKline().isClosed(),
    		kline.getKline().getQuoteAssetVolume(),
    		kline.getKline().getNumberOfTrades(),
    		kline.getKline().getTakerBuyBaseAssetVolume(),
    		kline.getKline().getTakerBuyQuoteAssetVolume()
	);
  }
  
  public BinanceKline getKline() {
    return kline;
  }
}
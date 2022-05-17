package org.knowm.xchange.binance.dto.meta;

import java.math.BigDecimal;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.meta.FeeTier;

/** @author ujjwal on 26/02/18. */
public class BinanceCurrencyPairMetaData extends CurrencyPairMetaData {
  private final BigDecimal minNotional;

  /**
   * Constructor
   *
   * @param makerFee Maker fee
   * @param takerFee Taker fee
   * @param minimumAmount Minimum trade amount
   * @param maximumAmount Maximum trade amount
   * @param priceScale Price scale
   */
  public BinanceCurrencyPairMetaData(
      BigDecimal makerFee,
      BigDecimal takerFee,
      BigDecimal minimumAmount,
      BigDecimal maximumAmount,
      Integer priceScale,
      BigDecimal minNotional,
      FeeTier[] feeTiers) {
    super(makerFee, takerFee, minimumAmount, maximumAmount, priceScale, feeTiers);
    this.minNotional = minNotional;
  }

  public BigDecimal getMinNotional() {
    return minNotional;
  }

  @Override
  public String toString() {
    return "BinanceCurrencyPairMetaData{" + "minNotional=" + minNotional + "} " + super.toString();
  }
}

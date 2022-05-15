package org.knowm.xchange.dto.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.Fee;

public class CurrencyPairMetaData implements Serializable {

  private static final long serialVersionUID = 4749144540694704221L;

  /** maker fee */
  @JsonProperty("maker_fee")
  private final BigDecimal makerFee;

  /** taker fee */
  @JsonProperty("take_fee")
  private final BigDecimal takerFee;

  /** Trading fee tiers by volume (fraction). Sorted in ascending order by quantity */
  @JsonProperty("fee_tiers")
  private final FeeTier[] feeTiers;

  /** Minimum trade amount */
  @JsonProperty("min_amount")
  private final BigDecimal minimumAmount;

  /** Maximum trade amount */
  @JsonProperty("max_amount")
  private final BigDecimal maximumAmount;

  /** Minimum trade amount */
  @JsonProperty("counter_min_amount")
  private final BigDecimal counterMinimumAmount;

  /** Maximum trade amount */
  @JsonProperty("counter_max_amount")
  private final BigDecimal counterMaximumAmount;

  /** Decimal places for base amount */
  @JsonProperty("base_scale")
  private final Integer baseScale;

  /** Decimal places for counter amount */
  @JsonProperty("price_scale")
  private final Integer priceScale;

  /** Decimal places for volume amount */
  @JsonProperty("volume_scale")
  private final Integer volumeScale;

  /** Amount step size. If set, any amounts must be a multiple of this */
  @JsonProperty("amount_step_size")
  private final BigDecimal amountStepSize;

  /** Currency that will be used to change for this trade. */
  private final Currency tradingFeeCurrency;

  /** Is market order type allowed on this pair. */
  private final boolean marketOrderEnabled;

  /**
   * Constructor
   *
   * @param makerFee Maker fee
   * @param takerFee Taker fee
   * @param minimumAmount Minimum trade amount
   * @param maximumAmount Maximum trade amount
   * @param priceScale Price scale
   */
  public CurrencyPairMetaData(
      BigDecimal makerFee,
      BigDecimal takerFee,
      BigDecimal minimumAmount,
      BigDecimal maximumAmount,
      Integer priceScale,
      FeeTier[] feeTiers) {
    this(
        makerFee,
        takerFee,
        minimumAmount,
        maximumAmount,
        null,
        null,
        null,
        priceScale,
        null,
        feeTiers,
        null,
        null,
        true);
  }

  /**
   * Constructor
   *
   * @param makerFee Maker fee
   * @param takerFee Taker fee
   * @param minimumAmount Minimum trade amount
   * @param maximumAmount Maximum trade amount
   * @param priceScale Price scale
   * @param amountStepSize Amounts must be a multiple of this amount if set.
   */
  public CurrencyPairMetaData(
      BigDecimal makerFee,
      BigDecimal takerFee,
      BigDecimal minimumAmount,
      BigDecimal maximumAmount,
      Integer priceScale,
      FeeTier[] feeTiers,
      BigDecimal amountStepSize) {
    this(
        makerFee,
        takerFee,
        minimumAmount,
        maximumAmount,
        null,
        null,
        null,
        priceScale,
        null,
        feeTiers,
        amountStepSize,
        null,
        true);
  }

  public CurrencyPairMetaData(
      BigDecimal makerFee,
      BigDecimal takerFee,
      BigDecimal minimumAmount,
      BigDecimal maximumAmount,
      Integer priceScale,
      Integer volumeScale,
      FeeTier[] feeTiers,
      Currency tradingFeeCurrency) {
    this(
        makerFee,
        takerFee,
        minimumAmount,
        maximumAmount,
        null,
        null,
        null,
        priceScale,
        volumeScale,
        feeTiers,
        null,
        tradingFeeCurrency,
        true);
  }

  /**
   * Constructor
   *
   * @param makerFee Maker fee
   * @param takerFee Taker fee
   * @param minimumAmount Minimum trade amount
   * @param maximumAmount Maximum trade amount
   * @param priceScale Price scale
   * @param amountStepSize Amounts must be a multiple of this amount if set.
   */
  public CurrencyPairMetaData(
      @JsonProperty("maker_fee") BigDecimal makerFee,
      @JsonProperty("take_fee") BigDecimal takerFee,
      @JsonProperty("min_amount") BigDecimal minimumAmount,
      @JsonProperty("max_amount") BigDecimal maximumAmount,
      @JsonProperty("counter_min_amount") BigDecimal counterMinimumAmount,
      @JsonProperty("counter_max_amount") BigDecimal counterMaximumAmount,
      @JsonProperty("base_scale") Integer baseScale,
      @JsonProperty("price_scale") Integer priceScale,
      @JsonProperty("volume_scale") Integer volumeScale,
      @JsonProperty("fee_tiers") FeeTier[] feeTiers,
      @JsonProperty("amount_step_size") BigDecimal amountStepSize,
      @JsonProperty("trading_fee_currency") Currency tradingFeeCurrency,
      @JsonProperty("market_order_enabled") boolean marketOrderEnabled) {

    this.makerFee = makerFee;
    this.takerFee = takerFee;
    this.minimumAmount = minimumAmount;
    this.maximumAmount = maximumAmount;
    this.counterMinimumAmount = counterMinimumAmount;
    this.counterMaximumAmount = counterMaximumAmount;
    this.baseScale = baseScale;
    this.priceScale = priceScale;
    this.volumeScale = volumeScale;
    if (feeTiers != null) {
      Arrays.sort(feeTiers);
    }
    this.feeTiers = feeTiers;
    this.amountStepSize = amountStepSize;
    this.tradingFeeCurrency = tradingFeeCurrency;
    this.marketOrderEnabled = marketOrderEnabled;
  }

  public BigDecimal getMakerFee() {
    return makerFee;
  }

  public BigDecimal getTakerFee() {
    return takerFee;
  }

  public Fee getTradingFee() {
    return new Fee(makerFee, takerFee);
  }

  public BigDecimal getMinimumAmount() {

    return minimumAmount;
  }

  public BigDecimal getMaximumAmount() {

    return maximumAmount;
  }

  public Integer getBaseScale() {
    return baseScale;
  }

  public Integer getPriceScale() {
    return priceScale;
  }

  public Integer getVolumeScale() {
    return volumeScale;
  }

  public FeeTier[] getFeeTiers() {

    return feeTiers;
  }

  public BigDecimal getAmountStepSize() {

    return amountStepSize;
  }

  public Currency getTradingFeeCurrency() {
    return tradingFeeCurrency;
  }

  public BigDecimal getCounterMinimumAmount() {
    return counterMinimumAmount;
  }

  public BigDecimal getCounterMaximumAmount() {
    return counterMaximumAmount;
  }

  public boolean isMarketOrderEnabled() {
    return marketOrderEnabled;
  }

  public static class Builder {

    private BigDecimal makerFee;
    private BigDecimal takerFee;
    private FeeTier[] feeTiers;
    private BigDecimal minimumAmount;
    private BigDecimal maximumAmount;
    private BigDecimal counterMaximumAmount;
    private BigDecimal counterMinimumAmount;
    private Integer baseScale;
    private Integer priceScale;
    private Integer volumeScale;
    private BigDecimal amountStepSize;
    private Currency tradingFeeCurrency;
    private boolean marketOrderEnabled;

    public static Builder from(CurrencyPairMetaData metaData) {
      return new Builder()
          .takerFee(metaData.takerFee)
          .makerFee(metaData.makerFee)
          .feeTiers(metaData.getFeeTiers())
          .minimumAmount(metaData.getMinimumAmount())
          .maximumAmount(metaData.getMaximumAmount())
          .counterMinimumAmount(metaData.getCounterMinimumAmount())
          .counterMaximumAmount(metaData.getCounterMaximumAmount())
          .baseScale(metaData.getBaseScale())
          .priceScale(metaData.getPriceScale())
          .volumeScale(metaData.getVolumeScale())
          .amountStepSize(metaData.getAmountStepSize())
          .tradingFeeCurrency(metaData.getTradingFeeCurrency());
    }

    public Builder makerFee(BigDecimal val) {
      makerFee = val;
      return this;
    }

    public Builder takerFee(BigDecimal val) {
      takerFee = val;
      return this;
    }

    public Builder feeTiers(FeeTier[] feeTiers) {
      this.feeTiers = feeTiers;
      return this;
    }

    public Builder minimumAmount(BigDecimal minimumAmount) {
      this.minimumAmount = minimumAmount;
      return this;
    }

    public Builder maximumAmount(BigDecimal maximumAmount) {
      this.maximumAmount = maximumAmount;
      return this;
    }

    public Builder counterMinimumAmount(BigDecimal counterMinimumAmount) {
      this.counterMinimumAmount = counterMinimumAmount;
      return this;
    }

    public Builder counterMaximumAmount(BigDecimal counterMaximumAmount) {
      this.counterMaximumAmount = counterMaximumAmount;
      return this;
    }

    public Builder baseScale(Integer baseScale) {
      this.baseScale = baseScale;
      return this;
    }

    public Builder priceScale(Integer priceScale) {
      this.priceScale = priceScale;
      return this;
    }

    public Builder volumeScale(Integer volumeScale) {
      this.volumeScale = volumeScale;
      return this;
    }

    public Builder amountStepSize(BigDecimal amountStepSize) {
      this.amountStepSize = amountStepSize;
      return this;
    }

    public Builder tradingFeeCurrency(Currency tradingFeeCurrency) {
      this.tradingFeeCurrency = tradingFeeCurrency;
      return this;
    }

    public Builder marketOrderEnabled(boolean marketOrderEnabled) {
      this.marketOrderEnabled = marketOrderEnabled;
      return this;
    }

    public CurrencyPairMetaData build() {
      return new CurrencyPairMetaData(
          makerFee,
          takerFee,
          minimumAmount,
          maximumAmount,
          counterMinimumAmount,
          counterMaximumAmount,
          baseScale,
          priceScale,
          volumeScale,
          feeTiers,
          amountStepSize,
          tradingFeeCurrency,
          marketOrderEnabled);
    }
  }

  @Override
  public String toString() {

    return "CurrencyPairMetaData [makerFee="
        + makerFee
        + ", takerFee="
        + takerFee
        + ", minimumAmount="
        + minimumAmount
        + ", maximumAmount="
        + maximumAmount
        + ", baseScale="
        + baseScale
        + ", priceScale="
        + priceScale
        + ", volumeScale="
        + volumeScale
        + ", amountStepSize="
        + amountStepSize
        + ", tradingFeeCurrency="
        + tradingFeeCurrency
        + "]";
  }
}

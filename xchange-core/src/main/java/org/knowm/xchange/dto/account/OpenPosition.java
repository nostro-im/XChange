package org.knowm.xchange.dto.account;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.knowm.xchange.instrument.Instrument;

@JsonDeserialize(builder = OpenPosition.Builder.class)
public class OpenPosition implements Serializable {
  /** The instrument */
  private final Instrument instrument;
  /** Is this a long or a short position */
  private final Type type;
  /** The size of the position */
  private final BigDecimal size;
  /** The average entry price for the position */
  private final BigDecimal price;
  /** Current mark price for position's instrument */
  private final BigDecimal markPrice;
  /** Liquidation price */
  private final BigDecimal liquidationPrice;
  /** Current initial leverage */
  private final BigDecimal leverage;
  /** Maintenance Margin / Margin Balance. Position will be liquidated once Margin Ratio reaches 1 */
  private final BigDecimal marginRatio;
  /** unrealized profit */
  private final BigDecimal unrealizedProfit;
  /** The time the position was valid on the exchange server */
  private final Date timestamp;

  public OpenPosition(Instrument instrument, Type type, BigDecimal size, BigDecimal price) {
    this(instrument, type, size, price, null, null, null, null, null, null);
  }

  private OpenPosition(
          Instrument instrument,
          Type type,
          BigDecimal size,
          BigDecimal price,
          BigDecimal markPrice,
          BigDecimal liquidationPrice,
          BigDecimal leverage,
          BigDecimal marginRatio,
          BigDecimal unrealizedProfit,
          Date timestamp) {
    this.instrument = instrument;
    this.type = type;
    this.size = size;
    this.price = price;
    this.markPrice = markPrice;
    this.liquidationPrice = liquidationPrice;
    this.leverage = leverage;
    this.marginRatio = marginRatio;
    this.unrealizedProfit = unrealizedProfit;
    this.timestamp = timestamp;
  }

  public Instrument getInstrument() {
    return instrument;
  }

  public Type getType() {
    return type;
  }

  public BigDecimal getSize() {
    return size;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public BigDecimal getMarkPrice() {
    return markPrice;
  }

  public BigDecimal getLiquidationPrice() {
    return liquidationPrice;
  }

  public BigDecimal getLeverage() {
    return leverage;
  }

  public BigDecimal getMarginRatio() {
    return marginRatio;
  }

  public BigDecimal getUnrealizedProfit() {
    return unrealizedProfit;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OpenPosition position = (OpenPosition) o;
    return Objects.equals(instrument, position.instrument) &&
            type == position.type &&
            Objects.equals(size, position.size) &&
            Objects.equals(price, position.price) &&
            Objects.equals(markPrice, position.markPrice) &&
            Objects.equals(liquidationPrice, position.liquidationPrice) &&
            Objects.equals(leverage, position.leverage) &&
            Objects.equals(marginRatio, position.marginRatio) &&
            Objects.equals(unrealizedProfit, position.unrealizedProfit) &&
            Objects.equals(timestamp, position.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(instrument, type, size, price, markPrice, liquidationPrice, leverage, marginRatio, unrealizedProfit, timestamp);
  }

  @Override
  public String toString() {
    return "OpenPosition{" +
            "instrument=" + instrument +
            ", type=" + type +
            ", size=" + size +
            ", price=" + price +
            ", markPrice=" + markPrice +
            ", liquidationPrice=" + liquidationPrice +
            ", leverage=" + leverage +
            ", marginRatio=" + marginRatio +
            ", unrealizedProfit=" + unrealizedProfit +
            ", timestamp=" + timestamp +
            '}';
  }

  public enum Type {
    LONG,
    SHORT
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static class Builder {
    private Instrument instrument;
    private Type type;
    private BigDecimal size;
    private BigDecimal price;
    private BigDecimal markPrice;
    private BigDecimal liquidationPrice;
    private BigDecimal leverage;
    private BigDecimal marginRatio;
    private BigDecimal unrealizedProfit;
    private Date timestamp;

    public static Builder from(OpenPosition openPosition) {
      return new Builder()
              .instrument(openPosition.getInstrument())
              .type(openPosition.getType())
              .size(openPosition.getSize())
              .price(openPosition.getPrice())
              .markPrice(openPosition.getMarkPrice())
              .liquidationPrice(openPosition.getLiquidationPrice())
              .leverage(openPosition.getLeverage())
              .marginRatio(openPosition.getMarginRatio())
              .unrealizedProfit(openPosition.getUnrealizedProfit())
              .timestamp(openPosition.getTimestamp());
    }

    public Builder instrument(final Instrument instrument) {
      this.instrument = instrument;
      return this;
    }

    public Builder type(final Type type) {
      this.type = type;
      return this;
    }

    public Builder size(final BigDecimal size) {
      this.size = size;
      return this;
    }

    public Builder price(final BigDecimal price) {
      this.price = price;
      return this;
    }

    public Builder markPrice(final BigDecimal markPrice) {
      this.markPrice = markPrice;
      return this;
    }

    public Builder liquidationPrice(final BigDecimal liquidationPrice) {
      this.liquidationPrice = liquidationPrice;
      return this;
    }

    public Builder leverage(final BigDecimal leverage) {
      this.leverage = leverage;
      return this;
    }

    public Builder marginRatio(final BigDecimal marginRatio) {
      this.marginRatio = marginRatio;
      return this;
    }

    public Builder unrealizedProfit(final BigDecimal unrealizedProfit) {
      this.unrealizedProfit = unrealizedProfit;
      return this;
    }

    public Builder timestamp(final Date timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public OpenPosition build() {
      return new OpenPosition(instrument, type, size, price, markPrice, liquidationPrice, leverage, marginRatio, unrealizedProfit, timestamp);
    }
  }
}

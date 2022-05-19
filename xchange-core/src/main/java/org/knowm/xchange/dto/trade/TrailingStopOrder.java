package org.knowm.xchange.dto.trade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.apache.commons.lang3.ObjectUtils;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.instrument.Instrument;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * DTO representing a limit order
 *
 * <p>A limit order lets you set a minimum or maximum price before your trade will be treated by the
 * exchange as a {@link MarketOrder}. There is no guarantee that your conditions will be met on the
 * exchange, so your order may not be executed. However, until you become very experienced, almost
 * all orders should be limit orders to protect yourself.
 */
@JsonDeserialize(builder = TrailingStopOrder.Builder.class)
public class TrailingStopOrder extends Order implements Comparable<TrailingStopOrder> {

  private static final long serialVersionUID = -4078839748471177713L;

  public enum TriggerType {
    /**
     * The latest transaction price the contract was traded at, the last trade in the trading history
     */
    LAST_PRICE,
    /**
     * An estimated fair value of a contract, it's computed with different formulas for different exchanges/markets 
     */
    MARK_PRICE
  }

  /**
   * Trigger price to the {@link TrailingStopOrder}. When {@code null}, it's triggered immediately.
   */
  protected final BigDecimal triggerPrice;

  /**
   * Trailing Stop ratio (e.g 0.03 for trail long with 3% drawdown)
   */
  protected final BigDecimal trailingRatio;

  /**
   * Some exchanges allow setting MARK_PRICE or LAST_PRICE as order trigger
   */
  protected final TriggerType triggerType;

  /**
   * @param type Either BID (buying) or ASK (selling)
   * @param originalAmount The amount to trade
   * @param instrument The identifier (e.g. BTC/USD)
   * @param id An id (usually provided by the exchange)
   * @param timestamp a Date object representing the order's timestamp according to the exchange's
   *     server, null if not provided
   * @param triggerPrice Trigger price. When {@code null}, order triggered immediately.
   * @param trailingRatio Trailing drawdown percentage
   * @param averagePrice the weighted average price of any fills belonging to the order
   * @param cumulativeAmount the amount that has been filled
   * @param status the status of the order at the exchange or broker
   * @param userReference An id provided by the user
   */
  public TrailingStopOrder(
      OrderType type,
      BigDecimal originalAmount,
      Instrument instrument,
      String id,
      Date timestamp,
      BigDecimal triggerPrice,
      BigDecimal trailingRatio,
      TriggerType triggerType,
      BigDecimal averagePrice,
      BigDecimal cumulativeAmount,
      BigDecimal fee,
      OrderStatus status,
      String userReference) {

    super(
        type,
        originalAmount,
        instrument,
        id,
        timestamp,
        averagePrice,
        cumulativeAmount,
        fee,
        status,
        userReference);
    this.triggerPrice = triggerPrice;
    this.trailingRatio = trailingRatio;
    this.triggerType = triggerType;
  }

  public BigDecimal getTriggerPrice() {
    return triggerPrice;
  }

  public BigDecimal getTrailingRatio() {
    return trailingRatio;
  }

  public TriggerType getTriggerType() {
    return triggerType;
  }

  @Override
  public String toString() {
    return "TrailingStopOrder [triggerPrice=" + triggerPrice + ", trailingRatio=" + trailingRatio + ", triggerType=" + triggerType + ", " + super.toString() + "]";
  }

  @Override
  public int compareTo(TrailingStopOrder trailingStopOrder) {

    if (this.getType() != trailingStopOrder.getType()) {
      return this.getType() == OrderType.BID ? -1 : 1;
    }
    int res = ObjectUtils.compare(this.triggerPrice, trailingStopOrder.triggerPrice);
    if (res != 0) {
      return res;
    }
    return ObjectUtils.compare(this.trailingRatio, trailingStopOrder.trailingRatio);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TrailingStopOrder)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }

    TrailingStopOrder trailingStopOrder = (TrailingStopOrder) obj;

    if (!Objects.equals(triggerPrice, trailingStopOrder.triggerPrice)) {
      return false;
    }
    if (!Objects.equals(trailingRatio, trailingStopOrder.trailingRatio)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (triggerPrice != null ? triggerPrice.hashCode() : 0);
    result = 31 * result + (trailingRatio != null ? trailingRatio.hashCode() : 0);
    return result;
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static class Builder extends Order.Builder {

    protected BigDecimal triggerPrice;
    protected BigDecimal trailingRatio;
    protected TriggerType triggerType;

    @JsonCreator
    public Builder(
        @JsonProperty("orderType") OrderType orderType,
        @JsonProperty("instrument") Instrument instrument) {

      super(orderType, instrument);
    }

    public static Builder from(Order order) {

      Builder builder =
          new Builder(order.getType(), order.getInstrument())
              .originalAmount(order.getOriginalAmount())
              .cumulativeAmount(order.getCumulativeAmount())
              .timestamp(order.getTimestamp())
              .id(order.getId())
              .flags(order.getOrderFlags())
              .orderStatus(order.getStatus())
              .fee(order.getFee())
              .averagePrice(order.getAveragePrice())
              .userReference(order.getUserReference());
      if (order instanceof TrailingStopOrder) {
        TrailingStopOrder trailingStopOrder = (TrailingStopOrder) order;
        builder.triggerPrice(trailingStopOrder.getTriggerPrice());
        builder.trailingRatio(trailingStopOrder.getTrailingRatio());
        builder.triggerType(trailingStopOrder.getTriggerType());        
      }
      return builder;
    }

    @Override
    public Builder orderType(OrderType orderType) {

      return (Builder) super.orderType(orderType);
    }

    @Override
    public Builder originalAmount(BigDecimal originalAmount) {

      return (Builder) super.originalAmount(originalAmount);
    }

    @Override
    public Builder cumulativeAmount(BigDecimal originalAmount) {

      return (Builder) super.cumulativeAmount(originalAmount);
    }

    @Override
    public Builder remainingAmount(BigDecimal remainingAmount) {

      return (Builder) super.remainingAmount(remainingAmount);
    }

    @Override
    @Deprecated
    public Builder currencyPair(CurrencyPair currencyPair) {

      return (Builder) super.currencyPair(currencyPair);
    }

    @Override
    public Builder instrument(Instrument instrument) {

      return (Builder) super.instrument(instrument);
    }

    @Override
    public Builder id(String id) {

      return (Builder) super.id(id);
    }

    @Override
    public Builder userReference(String userReference) {

      return (Builder) super.userReference(userReference);
    }

    @Override
    public Builder timestamp(Date timestamp) {

      return (Builder) super.timestamp(timestamp);
    }
    
    @Override
    public Builder completedTimestamp(Date timestamp) {

    	return (Builder) super.completedTimestamp(timestamp);
    }

    @Override
    public Builder orderStatus(OrderStatus status) {

      return (Builder) super.orderStatus(status);
    }

    @Override
    public Builder averagePrice(BigDecimal averagePrice) {

      return (Builder) super.averagePrice(averagePrice);
    }

    @Override
    public Builder flag(IOrderFlags flag) {

      return (Builder) super.flag(flag);
    }

    @Override
    public Builder flags(Set<IOrderFlags> flags) {

      return (Builder) super.flags(flags);
    }

    @Override
    public Builder fee(BigDecimal fee) {
      return (Builder) super.fee(fee);
    }

    public Builder triggerPrice(BigDecimal triggerPrice) {

      this.triggerPrice = triggerPrice;
      return this;
    }

    public Builder trailingRatio(BigDecimal trailingRatio) {

      this.trailingRatio = trailingRatio;
      return this;
    }

    public Builder triggerType(TriggerType triggerType) {

      this.triggerType = triggerType;
      return this;
    }
    
    @Override
    public TrailingStopOrder build() {

      TrailingStopOrder order =
          new TrailingStopOrder(
              orderType,
              originalAmount,
              instrument,
              id,
              timestamp,
              triggerPrice,
              trailingRatio,
              triggerType,
              averagePrice,
              originalAmount == null || remainingAmount == null
                  ? cumulativeAmount
                  : originalAmount.subtract(remainingAmount),
              fee,
              status,
              userReference);
      order.setOrderFlags(flags);
      order.setLeverage(leverage);
      return order;
    }
  }
}

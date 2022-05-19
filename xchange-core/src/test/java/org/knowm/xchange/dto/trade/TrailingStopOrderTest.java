package org.knowm.xchange.dto.trade;

import org.junit.Test;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.utils.ObjectMapperHelper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class TrailingStopOrderTest {
  @Test
  public void testBuilder() {
    final Order.OrderType type = Order.OrderType.BID;
    final BigDecimal originalAmount = new BigDecimal("99.401");
    final BigDecimal averagePrice = new BigDecimal("255.00");
    final BigDecimal cumulativeAmount = new BigDecimal("0.00");
    final CurrencyPair currencyPair = CurrencyPair.LTC_BTC;
    final BigDecimal triggerPrice = new BigDecimal("251.64");
    final BigDecimal trailingRatio = new BigDecimal("0.02");
    final BigDecimal fee = new BigDecimal("22.2");
    final Date timestamp = new Date();
    final String id = "id";
    final Order.OrderStatus status = Order.OrderStatus.FILLED;
    final String reference = "reference";
    final Currency feeCurrency = Currency.DX;
    final TrailingStopOrder.TriggerType triggerType = TrailingStopOrder.TriggerType.LAST_PRICE;

    final TrailingStopOrder copy =
        new TrailingStopOrder.Builder(type, currencyPair)
            .originalAmount(originalAmount)
            .averagePrice(averagePrice)
            .cumulativeAmount(cumulativeAmount)
            .triggerPrice(triggerPrice)
            .trailingRatio(trailingRatio)
            .orderStatus(status)
            .timestamp(timestamp)
            .id(id)
            .flag(TestFlags.TEST1)
            .fee(fee)
            .userReference(reference)
            .feeCurrency(feeCurrency)
            .triggerType(triggerType)
            .build();

    assertThat(copy.getType()).isEqualTo(type);
    assertThat(copy.getOriginalAmount()).isEqualTo(originalAmount);
    assertThat(copy.getAveragePrice()).isEqualTo(averagePrice);
    assertThat(copy.getCumulativeAmount()).isEqualTo(cumulativeAmount);
    assertThat(copy.getCurrencyPair()).isEqualTo(currencyPair);
    assertThat(copy.getTriggerPrice()).isEqualTo(triggerPrice);
    assertThat(copy.getTrailingRatio()).isEqualTo(trailingRatio);
    assertThat(copy.getTimestamp()).isEqualTo(timestamp);
    assertThat(copy.getId()).isEqualTo(id);
    assertThat(copy.getOrderFlags()).hasSize(1);
    assertThat(copy.getOrderFlags()).containsExactly(TestFlags.TEST1);
    assertThat(copy.hasFlag(TestFlags.TEST1));
    assertThat(copy.getStatus()).isEqualTo(status);
    assertThat(copy.getFee()).isEqualTo(fee);
    assertThat(copy.getUserReference()).isEqualTo(reference);
    assertThat(copy.getFeeCurrency()).isEqualTo(feeCurrency);
    assertThat(copy.getTriggerType()).isEqualTo(triggerType);
  }

  @Test
  public void testBuilderFrom() throws IOException {
    final Order.OrderType type = Order.OrderType.ASK;
    final BigDecimal originalAmount = new BigDecimal("100.501");
    final CurrencyPair currencyPair = CurrencyPair.BTC_USD;
    final BigDecimal averagePrice = new BigDecimal("255.00");
    final BigDecimal cumulativeAmount = new BigDecimal("0.00");
    final BigDecimal triggerPrice = new BigDecimal("250.34");
    final BigDecimal trailingRatio = new BigDecimal("0.01");
    final BigDecimal fee = new BigDecimal("22.2");
    final Date timestamp = new Date();
    final String id = "id";
    final Order.OrderStatus status = Order.OrderStatus.FILLED;
    final String reference = "reference";
    final Currency feeCurrency = Currency.DX;
    final TrailingStopOrder.TriggerType triggerType = TrailingStopOrder.TriggerType.MARK_PRICE;

    final TrailingStopOrder original =
        new TrailingStopOrder(
            type,
            originalAmount,
            currencyPair,
            id,
            timestamp,
            triggerPrice,
            trailingRatio,
            triggerType,
            averagePrice,
            cumulativeAmount,
            fee,
            status,
            reference,
            feeCurrency);
    original.addOrderFlag(TestFlags.TEST1);
    original.addOrderFlag(TestFlags.TEST3);
    final TrailingStopOrder copy = TrailingStopOrder.Builder.from(original).build();

    assertThat(copy).isEqualToComparingFieldByField(original);
  }

  @Test
  public void testSerializeDeserialize() throws IOException {
    final Order.OrderType type = Order.OrderType.ASK;
    final BigDecimal originalAmount = new BigDecimal("100.501");
    final CurrencyPair currencyPair = CurrencyPair.BTC_USD;
    final BigDecimal averagePrice = new BigDecimal("255.00");
    final BigDecimal cumulativeAmount = new BigDecimal("0.00");
    final BigDecimal triggerPrice = new BigDecimal("250.34");
    final BigDecimal trailingRatio = new BigDecimal("0.01");
    final BigDecimal fee = new BigDecimal("22.2");
    final Date timestamp = new Date();
    final String id = "id";
    final Order.OrderStatus status = Order.OrderStatus.FILLED;
    final String reference = "reference";
    final Currency feeCurrency = Currency.DX;
    final TrailingStopOrder.TriggerType triggerType = TrailingStopOrder.TriggerType.LAST_PRICE;

    final TrailingStopOrder original =
        new TrailingStopOrder(
            type,
            originalAmount,
            currencyPair,
            id,
            timestamp,
            triggerPrice,
            trailingRatio,
            triggerType,
            averagePrice,
            cumulativeAmount,
            fee,
            status,
            reference,
            feeCurrency);
    original.addOrderFlag(TestFlags.TEST1);
    original.addOrderFlag(TestFlags.TEST3);

    TrailingStopOrder jsonCopy = ObjectMapperHelper.viaJSON(original);
    assertThat(jsonCopy).isEqualToIgnoringGivenFields(original, "cumulativeAmount");
    assertTrue(jsonCopy.getCumulativeAmount().compareTo(original.getCumulativeAmount()) == 0);
  }

  private enum TestFlags implements Order.IOrderFlags {
    TEST1,
    TEST2,
    TEST3
  }
}

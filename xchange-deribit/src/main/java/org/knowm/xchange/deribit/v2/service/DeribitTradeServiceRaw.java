package org.knowm.xchange.deribit.v2.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.deribit.v2.DeribitExchange;
import org.knowm.xchange.deribit.v2.dto.Kind;
import org.knowm.xchange.deribit.v2.dto.trade.AdvancedOptions;
import org.knowm.xchange.deribit.v2.dto.trade.Order;
import org.knowm.xchange.deribit.v2.dto.trade.OrderPlacement;
import org.knowm.xchange.deribit.v2.dto.trade.OrderType;
import org.knowm.xchange.deribit.v2.dto.trade.SettlementType;
import org.knowm.xchange.deribit.v2.dto.trade.TimeInForce;
import org.knowm.xchange.deribit.v2.dto.trade.Trigger;
import org.knowm.xchange.deribit.v2.dto.trade.UserSettlements;
import org.knowm.xchange.deribit.v2.dto.trade.UserTrades;

import static org.knowm.xchange.deribit.v2.DeribitResilience.PRIVATE_REST_ENDPOINT_RATE_LIMITER;

public class DeribitTradeServiceRaw extends DeribitBaseService {

  public DeribitTradeServiceRaw(DeribitExchange exchange,
                                ResilienceRegistries resilienceRegistries) {
    super(exchange, resilienceRegistries);
  }

  public OrderPlacement buy(
      String instrumentName,
      BigDecimal amount,
      OrderType type,
      String label,
      BigDecimal price,
      TimeInForce timeInForce,
      BigDecimal maxShow,
      Boolean postOnly,
      Boolean rejectPostOnly,
      Boolean reduceOnly,
      BigDecimal triggerPrice,
      Trigger trigger,
      AdvancedOptions advanced,
      Boolean mmp)
      throws IOException {
    return decorateApiCall(() -> deribitAuthenticated
        .buy(
            instrumentName,
            amount,
            type,
            label,
            price,
            timeInForce,
            maxShow,
            postOnly,
            rejectPostOnly,
            reduceOnly,
            triggerPrice,
            trigger,
            advanced,
            mmp,
            deribitAuth)
        .getResult())
            .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public OrderPlacement sell(
      String instrumentName,
      BigDecimal amount,
      OrderType type,
      String label,
      BigDecimal price,
      TimeInForce timeInForce,
      BigDecimal maxShow,
      Boolean postOnly,
      Boolean rejectPostOnly,
      Boolean reduceOnly,
      BigDecimal triggerPrice,
      Trigger trigger,
      AdvancedOptions advanced,
      Boolean mmp)
      throws IOException {
    return decorateApiCall(() -> deribitAuthenticated
        .sell(
            instrumentName,
            amount,
            type,
            label,
            price,
            timeInForce,
            maxShow,
            postOnly,
            rejectPostOnly,
            reduceOnly,
            triggerPrice,
            trigger,
            advanced,
            mmp,
            deribitAuth)
        .getResult())
            .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public OrderPlacement edit(
      String orderId,
      BigDecimal amount,
      BigDecimal price,
      Boolean postOnly,
      Boolean rejectPostOnly,
      Boolean reduceOnly,
      BigDecimal triggerPrice,
      AdvancedOptions advanced,
      Boolean mmp)
      throws IOException {
    return decorateApiCall(() -> deribitAuthenticated
        .edit(
            orderId,
            amount,
            price,
            postOnly,
            rejectPostOnly,
            reduceOnly,
            triggerPrice,
            advanced,
            mmp,
            deribitAuth)
        .getResult())
            .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public Order cancel(String orderId) throws IOException {
    return decorateApiCall(() -> deribitAuthenticated.cancel(orderId, deribitAuth).getResult())
            .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public Integer cancelByLabel(String label) throws IOException {
    return decorateApiCall(() -> deribitAuthenticated.cancelByLabel(label, deribitAuth).getResult())
            .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public List<Order> getOpenOrdersByCurrency(String currency, Kind kind, String type)
      throws IOException {
    return decorateApiCall(() -> deribitAuthenticated
        .getOpenOrdersByCurrency(currency, kind, type, deribitAuth)
        .getResult())
            .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public List<Order> getOpenOrdersByInstrument(String instrumentName, String type)
      throws IOException {
    return decorateApiCall(() -> deribitAuthenticated
        .getOpenOrdersByInstrument(instrumentName, type, deribitAuth)
        .getResult())
            .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public UserTrades getUserTradesByCurrency(
      String currency,
      Kind kind,
      String startId,
      String endId,
      Integer count,
      Boolean includeOld,
      String sorting)
      throws IOException {
    return decorateApiCall(() -> deribitAuthenticated
        .getUserTradesByCurrency(
            currency, kind, startId, endId, count, includeOld, sorting, deribitAuth)
        .getResult())
            .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public UserTrades getUserTradesByCurrencyAndTime(
      String currency,
      Kind kind,
      Date startTimestamp,
      Date endTimestamp,
      Integer count,
      Boolean includeOld,
      String sorting)
      throws IOException {
    return decorateApiCall(() -> deribitAuthenticated
        .getUserTradesByCurrencyAndTime(
            currency,
            kind,
            startTimestamp.getTime(),
            endTimestamp.getTime(),
            count,
            includeOld,
            sorting,
            deribitAuth)
        .getResult())
            .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public UserTrades getUserTradesByInstrument(
      String instrumentName,
      Integer startSeq,
      Integer endSeq,
      Integer count,
      Boolean includeOld,
      String sorting)
      throws IOException {
    return decorateApiCall(() -> deribitAuthenticated
        .getUserTradesByInstrument(
            instrumentName, startSeq, endSeq, count, includeOld, sorting, deribitAuth)
        .getResult())
            .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public UserTrades getUserTradesByInstrumentAndTime(
      String instrumentName,
      Date startTimestamp,
      Date endTimestamp,
      Integer count,
      Boolean includeOld,
      String sorting)
      throws IOException {
    return decorateApiCall(() -> deribitAuthenticated
        .getUserTradesByInstrumentAndTime(
            instrumentName,
            startTimestamp.getTime(),
            endTimestamp.getTime(),
            count,
            includeOld,
            sorting,
            deribitAuth)
        .getResult())
            .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public UserSettlements getUserSettlementsByInstrument(
      String instrumentName, SettlementType type, Integer count, String continuation)
      throws IOException {
    return decorateApiCall(() -> deribitAuthenticated
        .getSettlementHistoryByInstrument(instrumentName, type, count, continuation, deribitAuth)
        .getResult())
            .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public List<Order> getOrderHistoryByCurrency(
      String currency,
      Kind kind,
      Integer count,
      Integer offset,
      Boolean includeOld,
      Boolean includeUnfilled)
      throws IOException {
    return decorateApiCall(() -> deribitAuthenticated
        .getOrderHistoryByCurrency(
            currency, kind, count, offset, includeOld, includeUnfilled, deribitAuth)
        .getResult())
            .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public List<Order> getOrderHistoryByInstrument(
      String instrumentName,
      Integer count,
      Integer offset,
      Boolean includeOld,
      Boolean includeUnfilled)
      throws IOException {
    return decorateApiCall(() -> deribitAuthenticated
        .getOrderHistoryByInstrument(
            instrumentName, count, offset, includeOld, includeUnfilled, deribitAuth)
        .getResult())
            .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public Order getOrderState(String orderId) throws IOException {
    return decorateApiCall(() -> deribitAuthenticated.getOrderState(orderId, deribitAuth).getResult())
            .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }
}

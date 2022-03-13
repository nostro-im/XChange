package org.knowm.xchange.deribit.v2.service;

import java.io.IOException;
import java.util.List;

import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.deribit.v2.DeribitExchange;
import org.knowm.xchange.deribit.v2.dto.Kind;
import org.knowm.xchange.deribit.v2.dto.marketdata.DeribitCurrency;
import org.knowm.xchange.deribit.v2.dto.marketdata.DeribitInstrument;
import org.knowm.xchange.deribit.v2.dto.marketdata.DeribitOrderBook;
import org.knowm.xchange.deribit.v2.dto.marketdata.DeribitSummary;
import org.knowm.xchange.deribit.v2.dto.marketdata.DeribitTicker;
import org.knowm.xchange.deribit.v2.dto.marketdata.DeribitTrades;

import static org.knowm.xchange.deribit.v2.DeribitResilience.PUBLIC_REST_ENDPOINT_RATE_LIMITER;

/**
 * Implementation of the market data service for Deribit
 *
 * <ul>
 *   <li>Provides access to various market data values
 * </ul>
 */
public class DeribitMarketDataServiceRaw extends DeribitBaseService {

  public DeribitMarketDataServiceRaw(DeribitExchange exchange,
                                     ResilienceRegistries resilienceRegistries) {
    super(exchange, resilienceRegistries);
  }

  public List<DeribitInstrument> getDeribitInstruments(String currency, Kind kind, Boolean expired)
      throws IOException {
    return decorateApiCall(() -> deribit.getInstruments(currency, kind, expired).getResult())
            .withRateLimiter(rateLimiter(PUBLIC_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public List<DeribitCurrency> getDeribitCurrencies() throws IOException {
    return decorateApiCall(() -> deribit.getCurrencies().getResult())
            .withRateLimiter(rateLimiter(PUBLIC_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public DeribitOrderBook getDeribitOrderBook(String instrumentName, Integer depth)
      throws IOException {
    return decorateApiCall(() -> deribit.getOrderBook(instrumentName, depth).getResult())
            .withRateLimiter(rateLimiter(PUBLIC_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public DeribitTrades getLastTradesByInstrument(
      String instrumentName,
      Integer startSeq,
      Integer endSeq,
      Integer count,
      Boolean includeOld,
      String sorting)
      throws IOException {
    return decorateApiCall(() -> deribit
        .getLastTradesByInstrument(instrumentName, startSeq, endSeq, count, includeOld, sorting)
        .getResult())
            .withRateLimiter(rateLimiter(PUBLIC_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public List<DeribitSummary> getSummaryByInstrument(String instrumentName) throws IOException {
    return decorateApiCall(() -> deribit.getSummaryByInstrument(instrumentName).getResult())
            .withRateLimiter(rateLimiter(PUBLIC_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public DeribitTicker getDeribitTicker(String instrumentName) throws IOException {
    return decorateApiCall(() -> deribit.getTicker(instrumentName).getResult())
            .withRateLimiter(rateLimiter(PUBLIC_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }
}

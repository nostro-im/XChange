package org.knowm.xchange.deribit.v2.service;

import java.io.IOException;
import java.util.List;

import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.deribit.v2.DeribitExchange;
import org.knowm.xchange.deribit.v2.dto.Kind;
import org.knowm.xchange.deribit.v2.dto.account.AccountSummary;
import org.knowm.xchange.deribit.v2.dto.account.Position;

import static org.knowm.xchange.deribit.v2.DeribitResilience.PRIVATE_REST_ENDPOINT_RATE_LIMITER;

public class DeribitAccountServiceRaw extends DeribitBaseService {

  public DeribitAccountServiceRaw(DeribitExchange exchange,
                                  ResilienceRegistries resilienceRegistries) {
    super(exchange, resilienceRegistries);
  }

  public AccountSummary getAccountSummary(String currency, Boolean extended) throws IOException {
    return decorateApiCall(() -> deribitAuthenticated.getAccountSummary(currency, extended, deribitAuth).getResult())
            .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }

  public List<Position> getPositions(String currency, Kind kind) throws IOException {
    return decorateApiCall(() -> deribitAuthenticated.getPositions(currency, kind, deribitAuth).getResult())
            .withRateLimiter(rateLimiter(PRIVATE_REST_ENDPOINT_RATE_LIMITER))
            .call();
  }
}

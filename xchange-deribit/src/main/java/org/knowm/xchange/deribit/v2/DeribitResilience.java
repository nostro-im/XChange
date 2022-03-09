package org.knowm.xchange.deribit.v2;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.client.ResilienceUtils;

import java.time.Duration;

import static javax.ws.rs.core.Response.Status.TOO_MANY_REQUESTS;

public class DeribitResilience {

    public static final String PUBLIC_REST_ENDPOINT_RATE_LIMITER = "publicEndpointLimit";

    public static final String PRIVATE_REST_ENDPOINT_RATE_LIMITER = "privateEndpointLimit";

    public static ResilienceRegistries createRegistries() {
        final ResilienceRegistries registries = new ResilienceRegistries();

        registries
                .rateLimiters()
                .rateLimiter(
                        PUBLIC_REST_ENDPOINT_RATE_LIMITER,
                        RateLimiterConfig.from(registries.rateLimiters().getDefaultConfig())
                                .limitRefreshPeriod(Duration.ofSeconds(1))
                                .limitForPeriod(20)
                                .drainPermissionsOnResult(e -> ResilienceUtils.matchesHttpCode(e, TOO_MANY_REQUESTS))
                                .build());

        registries
                .rateLimiters()
                .rateLimiter(
                        PRIVATE_REST_ENDPOINT_RATE_LIMITER,
                        RateLimiterConfig.from(registries.rateLimiters().getDefaultConfig())
                                .limitRefreshPeriod(Duration.ofSeconds(1))
                                .limitForPeriod(30)
                                .drainPermissionsOnResult(e -> ResilienceUtils.matchesHttpCode(e, TOO_MANY_REQUESTS))
                                .build());

        return registries;
    }
}

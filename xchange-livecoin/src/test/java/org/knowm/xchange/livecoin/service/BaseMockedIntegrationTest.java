package org.knowm.xchange.livecoin.service;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.livecoin.LivecoinExchange;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/** @author walec51 */
public class BaseMockedIntegrationTest {

  @Rule public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort().dynamicHttpsPort());

  public Exchange createExchange() {
    Exchange exchange =
        ExchangeFactory.INSTANCE.createExchangeWithoutSpecification(LivecoinExchange.class);
    ExchangeSpecification specification = exchange.getDefaultExchangeSpecification();
    specification.setHost("localhost");
    specification.setSslUri("http://localhost:" + wireMockRule.port());
    specification.setPort(wireMockRule.port());
    specification.setShouldLoadRemoteMetaData(false);
    exchange.applySpecification(specification);
    return exchange;
  }
}

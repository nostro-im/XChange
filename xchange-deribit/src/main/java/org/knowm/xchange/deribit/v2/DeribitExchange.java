package org.knowm.xchange.deribit.v2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSharedParameters;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.deribit.v2.dto.Kind;
import org.knowm.xchange.deribit.v2.dto.marketdata.DeribitCurrency;
import org.knowm.xchange.deribit.v2.dto.marketdata.DeribitInstrument;
import org.knowm.xchange.deribit.v2.service.DeribitAccountService;
import org.knowm.xchange.deribit.v2.service.DeribitMarketDataService;
import org.knowm.xchange.deribit.v2.service.DeribitMarketDataServiceRaw;
import org.knowm.xchange.deribit.v2.service.DeribitTradeService;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.derivative.OptionsContract;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.dto.meta.DerivativeMetaData;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.instrument.Instrument;

public class DeribitExchange extends BaseExchange implements Exchange {

  private static ResilienceRegistries RESILIENCE_REGISTRIES;

  @Override
  public void applySpecification(ExchangeSpecification exchangeSpecification) {

    super.applySpecification(exchangeSpecification);
  }

  @Override
  protected void initServices() {
    this.marketDataService = new DeribitMarketDataService(this, getResilienceRegistries());
    this.accountService = new DeribitAccountService(this, getResilienceRegistries());
    this.tradeService = new DeribitTradeService(this, getResilienceRegistries());
  }

  @Override
  public ExchangeSpecification getDefaultExchangeSpecification() {

    ExchangeSpecification exchangeSpecification = new ExchangeSpecification(this.getClass());
    exchangeSpecification.setSslUri("https://www.deribit.com");
    exchangeSpecification.setHost("deribit.com");
    //    exchangeSpecification.setPort(80);
    exchangeSpecification.setExchangeName("Deribit");
    exchangeSpecification.setExchangeDescription("Deribit is a Bitcoin futures exchange");
    exchangeSpecification.getResilience().setRateLimiterEnabled(true);
    exchangeSpecification.setExchangeSpecificParametersItem(ExchangeSharedParameters.PARAM_USE_SANDBOX, false);
    exchangeSpecification.setExchangeSpecificParametersItem(ExchangeSharedParameters.PARAM_SANDBOX_SSL_URI, "https://test.deribit.com");
    return exchangeSpecification;
  }

  public ExchangeSpecification getSandboxExchangeSpecification() {

    ExchangeSpecification exchangeSpecification = getDefaultExchangeSpecification();
    exchangeSpecification.setExchangeSpecificParametersItem(ExchangeSharedParameters.PARAM_USE_SANDBOX, true);
    exchangeSpecification.setHost("test.deribit.com");
    exchangeSpecification.getResilience().setRateLimiterEnabled(true);
    //    exchangeSpecification.setPort(80);
    return exchangeSpecification;
  }

  @Override
  public void remoteInit() throws IOException {
    updateExchangeMetaData();
  }

  public void updateExchangeMetaData() throws IOException {

    Map<Currency, CurrencyMetaData> currencies = new HashMap<>();
    Map<FuturesContract, DerivativeMetaData> futures = new HashMap<>();
    Map<OptionsContract, DerivativeMetaData> options = new HashMap<>();

    List<DeribitCurrency> activeDeribitCurrencies =
        ((DeribitMarketDataServiceRaw) marketDataService).getDeribitCurrencies();

    for (DeribitCurrency deribitCurrency : activeDeribitCurrencies) {
      currencies.put(
          new Currency(deribitCurrency.getCurrency()), DeribitAdapters.adaptMeta(deribitCurrency));

      List<DeribitInstrument> deribitInstruments =
          ((DeribitMarketDataServiceRaw) marketDataService)
              .getDeribitInstruments(deribitCurrency.getCurrency(), null, null);

      for (DeribitInstrument deribitInstrument : deribitInstruments) {
        if (deribitInstrument.getKind() == Kind.future) {
          futures.put(
              DeribitAdapters.adaptFuturesContract(deribitInstrument),
              DeribitAdapters.adaptMeta(deribitInstrument));
        } else {
          options.put(
              DeribitAdapters.adaptOptionsContract(deribitInstrument),
              DeribitAdapters.adaptMeta(deribitInstrument));
        }
      }
    }
    
    exchangeMetaData = new ExchangeMetaData(
            exchangeMetaData.getCurrencyPairs(),
            currencies,
            futures,
            options,
            exchangeMetaData.getPublicRateLimits(),
            exchangeMetaData.getPrivateRateLimits(),
            exchangeMetaData.isShareRateLimits()
    );
  }

  @Override
  public List<Instrument> getExchangeInstruments() {
    ArrayList<Instrument> instruments = new ArrayList<>();
    instruments.addAll(getExchangeMetaData().getFutures().keySet());
    instruments.addAll(getExchangeMetaData().getOptions().keySet());
    return instruments;
  }

  @Override
  public ResilienceRegistries getResilienceRegistries() {
    if (RESILIENCE_REGISTRIES == null) {
      RESILIENCE_REGISTRIES = DeribitResilience.createRegistries();
    }
    return RESILIENCE_REGISTRIES;
  }
}

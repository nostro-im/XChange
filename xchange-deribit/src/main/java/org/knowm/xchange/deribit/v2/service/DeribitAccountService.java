package org.knowm.xchange.deribit.v2.service;

import java.io.IOException;
import java.util.*;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.deribit.v2.DeribitAdapters;
import org.knowm.xchange.deribit.v2.DeribitExchange;
import org.knowm.xchange.deribit.v2.dto.account.AccountSummary;
import org.knowm.xchange.dto.account.*;
import org.knowm.xchange.service.account.AccountService;

public class DeribitAccountService extends DeribitAccountServiceRaw implements AccountService {

  public DeribitAccountService(DeribitExchange exchange) {
    super(exchange);
  }

  @Override
  public AccountInfo getAccountInfo() throws IOException {
    Collection<Currency> currencies = currencies();

    List<Balance> balances = new ArrayList<>();
    Set<AccountMargin> margin = new HashSet<>();
    for (Currency c : currencies) {
      AccountSummary accountSummary = getAccountSummary(c.getCurrencyCode(), false);
      balances.add(DeribitAdapters.adapt(accountSummary));
      margin.add(DeribitAdapters.adapt(c, accountSummary));
    }

    Wallet wallet = Wallet.Builder.from(balances).build();

    return AccountInfo.Builder.from(Collections.singleton(wallet))
            .openPositions(openPositions(currencies))
            .margins(margin)
            .build();
  }

  List<OpenPosition> openPositions() throws IOException {
    return openPositions(currencies());
  }

  private List<OpenPosition> openPositions(Collection<Currency> currencies) throws IOException {
    List<OpenPosition> openPositions = new ArrayList<>();
    for (Currency c : currencies) {
      super.getPositions(c.getCurrencyCode(), null).stream()
          .map(DeribitAdapters::adapt)
          .forEach(openPositions::add);
    }
    return openPositions;
  }

  Collection<Currency> currencies() {
    return exchange.getExchangeMetaData().getCurrencies().keySet();
  }
}

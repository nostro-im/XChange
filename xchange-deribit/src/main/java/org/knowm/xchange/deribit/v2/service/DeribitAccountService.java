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
    Map<Currency, AccountSummary> accountSummaries = new HashMap<>();
    for (Currency c : currencies) {
      AccountSummary accountSummary = getAccountSummary(c.getCurrencyCode(), false);
      accountSummaries.put(c, accountSummary);
      balances.add(DeribitAdapters.adapt(accountSummary));
      margin.add(DeribitAdapters.adapt(c, accountSummary));
    }

    Wallet wallet = Wallet.Builder.from(balances).build();

    return AccountInfo.Builder.from(Collections.singleton(wallet))
            .openPositions(openPositions(currencies, accountSummaries))
            .margins(margin)
            .build();
  }

  List<OpenPosition> openPositions() throws IOException {
    Map<Currency, AccountSummary> accountSummaries = new HashMap<>();
    Collection<Currency> currencies = currencies();
    for (Currency c : currencies) {
      accountSummaries.put(c, getAccountSummary(c.getCurrencyCode(), false));
    }
    return openPositions(currencies, accountSummaries);
  }

  private List<OpenPosition> openPositions(Collection<Currency> currencies, Map<Currency, AccountSummary> accountSummaries) throws IOException {
    List<OpenPosition> openPositions = new ArrayList<>();
    for (Currency c : currencies) {
      super.getPositions(c.getCurrencyCode(), null).stream()
          .map(position -> DeribitAdapters.adapt(position, accountSummaries.get(c)))
          .forEach(openPositions::add);
    }
    return openPositions;
  }

  Collection<Currency> currencies() {
    return exchange.getExchangeMetaData().getCurrencies().keySet();
  }
}

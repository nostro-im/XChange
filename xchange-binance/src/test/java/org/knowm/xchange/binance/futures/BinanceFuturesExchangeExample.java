package org.knowm.xchange.binance.futures;

import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSharedParameters;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceFuturesExchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.AccountInfo;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.stream.Collectors;

public class BinanceFuturesExchangeExample {

	public static final boolean SANDBOX = true;
	public static final String API_KEY = "key";
	public static final String API_SECRET = "secret";

	public static void main(String[] args) throws InterruptedException {
		// Far safer than temporarily adding these to code that might get committed to VCS

		ExchangeSpecification spec =
				ExchangeFactory.INSTANCE
						.createExchange(BinanceFuturesExchange.class)
						.getDefaultExchangeSpecification();

		spec.setApiKey(API_KEY);
		spec.setSecretKey(API_SECRET);
		spec.setShouldLoadRemoteMetaData(!SANDBOX);
		spec.setExchangeSpecificParametersItem(ExchangeSharedParameters.PARAM_USE_SANDBOX, SANDBOX);
		BinanceFuturesExchange exchange =
				(BinanceFuturesExchange) ExchangeFactory.INSTANCE.createExchange(spec);

		System.out.println("exchange spec: " + exchange.getExchangeSpecification());

		try {
			AccountInfo accountInfo = exchange.getAccountService().getAccountInfo();
			System.out.println("account info: " + accountInfo);
			System.out.println("open positions: " + accountInfo.getOpenPositions().stream().filter(position -> position.getSize().compareTo(BigDecimal.ZERO) != 0).collect(Collectors.toList()));
			System.out.println("account margin: " + accountInfo.getAccountMargin());
			System.out.println("account balances: " + accountInfo.getWallet().getBalances().values().stream().filter(b -> b.getTotal().compareTo(BigDecimal.ZERO) != 0).collect(Collectors.toList()));
		} catch (IOException e) {
			System.out.println("error getting account" + e);
		}
	}
}

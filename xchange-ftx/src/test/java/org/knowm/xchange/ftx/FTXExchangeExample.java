package org.knowm.xchange.ftx;

import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSharedParameters;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.AccountInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FTXExchangeExample {

	public static final boolean SANDBOX = false;
	public static final String API_KEY = "key";
	public static final String API_SECRET = "secret";
	public static final String USER_NAME = "name";

	public static void main(String[] args) throws InterruptedException {
		// Far safer than temporarily adding these to code that might get committed to VCS

		ExchangeSpecification spec =
				ExchangeFactory.INSTANCE
						.createExchange(FtxExchange.class)
						.getDefaultExchangeSpecification();

		spec.setApiKey(API_KEY);
		spec.setSecretKey(API_SECRET);
		spec.setUserName(USER_NAME);
		spec.setShouldLoadRemoteMetaData(!SANDBOX);
		spec.setExchangeSpecificParametersItem(ExchangeSharedParameters.PARAM_USE_SANDBOX, SANDBOX);
		FtxExchange exchange =
				(FtxExchange) ExchangeFactory.INSTANCE.createExchange(spec);

		System.out.println("exchange spec: " + exchange.getExchangeSpecification());

		try {
			AccountInfo accountInfo = exchange.getAccountService().getAccountInfo();
			System.out.println("account info: {}" + accountInfo);
			System.out.println("open positions: {}" + accountInfo.getOpenPositions());
			System.out.println("account margin: {}" + accountInfo.getAccountMargin());
			System.out.println("account balances: {}" + accountInfo.getWallet("margin").getBalance(Currency.USDT));
			System.out.println("account balances: {}" + accountInfo.getWallet("spot").getBalance(Currency.USDT));
		} catch (IOException e) {
			System.out.println("error getting account" + e);
		}
	}
}

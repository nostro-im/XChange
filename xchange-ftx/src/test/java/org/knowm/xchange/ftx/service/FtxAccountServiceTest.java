package org.knowm.xchange.ftx.service;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.derivative.OptionsContract;
import org.knowm.xchange.dto.account.Fee;
import org.knowm.xchange.ftx.FtxAuthenticated;
import org.knowm.xchange.ftx.FtxExchange;
import org.knowm.xchange.ftx.dto.FtxResponse;
import org.knowm.xchange.ftx.dto.account.FtxAccountDto;
import org.knowm.xchange.instrument.Instrument;
import si.mazi.rescu.LongValueFactory;
import si.mazi.rescu.ParamsDigest;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class FtxAccountServiceTest {

	private FtxExchange exchange;
	private FtxAccountService accountService;
	private FtxAuthenticated ftx;

	@Before
	public void setUp() {
		ExchangeSpecification spec = mock(ExchangeSpecification.class);
		given(spec.getApiKey()).willReturn("api-key");
		given(spec.getSecretKey()).willReturn("secret-key");
		given(spec.getUserName()).willReturn("main");

		exchange = mock(FtxExchange.class);
		given(exchange.getExchangeSpecification()).willReturn(spec);
		given(exchange.getNonceFactory()).willReturn(new LongValueFactory());

		ftx = mock(FtxAuthenticated.class);

		Retry retry = mock(Retry.class);
		RetryRegistry retryRegistry = mock(RetryRegistry.class);
		given(retryRegistry.retry(anyString())).willReturn(retry);

		RateLimiterRegistry rateLimiterRegistry = mock(RateLimiterRegistry.class);
		given(rateLimiterRegistry.rateLimiter(anyString())).willReturn(mock(RateLimiter.class));

		ResilienceRegistries resilienceRegistries = mock(ResilienceRegistries.class);
		given(resilienceRegistries.retries()).willReturn(retryRegistry);
		given(resilienceRegistries.rateLimiters()).willReturn(rateLimiterRegistry);

		accountService = new FtxAccountService(exchange, ftx);
	}


	@Test
	public void testGetDynamicTradingFee() {
		Fee expectedTradingFee = new Fee(new BigDecimal("0.001"), new BigDecimal("0.0005"));
		FtxAccountDto account = mock(FtxAccountDto.class);
		when(account.getMakerFee()).thenReturn(expectedTradingFee.getMakerFee());
		when(account.getTakerFee()).thenReturn(expectedTradingFee.getTakerFee());

		Fee tradingFee = accountService.getDynamicTradingFee(account);

		assertThat(tradingFee).isEqualTo(expectedTradingFee);
	}

	@Test
	public void testGetTradingFees_missingInstrument() throws IOException {
		assertThatThrownBy(() -> accountService.getDynamicTradingFees(Collections.singleton(new FuturesContract(CurrencyPair.ADA_BNB, null))))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Exchange meta data does not contain requested instrument");
	}

	@Test
	public void testGetTradingFees_missingInstrument2() throws IOException {
		assertThatThrownBy(() -> accountService.getDynamicTradingFees(Collections.singleton(CurrencyPair.ADA_BNB)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Exchange meta data does not contain requested instrument");
	}

	@Test
	public void testGetTradingFees_missingInstrument3() throws IOException {
		assertThatThrownBy(() -> accountService.getDynamicTradingFees(Collections.singleton(new FinancialInstrument())))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Instrument is not supported");
	}

	@Test
	public void testGetTradingFees_unsupportedInstrument() throws IOException {
		assertThatThrownBy(() -> accountService.getDynamicTradingFees(Collections.singleton(new CurrencyPair(new Currency("XYZ"), Currency.USDT))))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Exchange meta data does not contain requested instrument");
	}

	@Test
	public void testGetTradingFees() throws IOException {
		List<CurrencyPair> pairs = Collections.singletonList(CurrencyPair.ETH_USDT);
		List<Instrument> futures = Collections.singletonList(new FuturesContract(CurrencyPair.ETH_USDT, null));
		List<Instrument> options = Collections.singletonList(new OptionsContract("ETH/USD/210719/34000/C"));
		List<Instrument> instruments = new ArrayList<>();
		instruments.addAll(pairs);
		instruments.addAll(futures);
		instruments.addAll(options);
		given(exchange.getExchangeSymbols()).willReturn(pairs);
		given(exchange.getExchangeFutures()).willReturn(futures);
		given(exchange.getExchangeOptions()).willReturn(options);

		Fee expectedTradingFee = new Fee(new BigDecimal("0.001"), new BigDecimal("0.0005"));
		FtxAccountDto account = mock(FtxAccountDto.class);
		given(account.getMakerFee()).willReturn(expectedTradingFee.getMakerFee());
		given(account.getTakerFee()).willReturn(expectedTradingFee.getTakerFee());
		given(ftx.getAccountInformation(anyString(), nullable(Long.class), any(ParamsDigest.class), anyString())).willReturn(new FtxResponse<>(true, account, false));

		Fee tradingFee = accountService.getDynamicTradingFee(account);
		Map<Instrument, Fee> tradingFees = accountService.getDynamicTradingFees(new HashSet<>(instruments));

		assertThat(tradingFees.size()).isEqualTo(instruments.size());
		tradingFees.forEach((instrument, fee) -> assertThat(fee).isEqualTo(tradingFee));
	}

	private static class FinancialInstrument extends Instrument {}
}
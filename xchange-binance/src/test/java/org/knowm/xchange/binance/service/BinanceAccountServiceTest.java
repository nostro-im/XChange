package org.knowm.xchange.binance.service;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceAuthenticated;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.account.BinanceAccountInformation;
import org.knowm.xchange.binance.dto.account.BinanceFutureTransferType;
import org.knowm.xchange.binance.dto.account.FutureTransferResponse;
import org.knowm.xchange.binance.service.account.params.BinanceFuturesAccountFundsTransferParams;
import org.knowm.xchange.client.ResilienceRegistries;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.dto.account.Fee;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.service.account.params.AccountFundsTransferParams;
import si.mazi.rescu.ParamsDigest;
import si.mazi.rescu.SynchronizedValueFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class BinanceAccountServiceTest {

    private BinanceAccountService accountService;
    private BinanceAuthenticated binanceAuthenticated;
    private BinanceExchange exchange;

    @Before
    public void setUp() {
        ExchangeSpecification.ResilienceSpecification resilienceSpecification = mock(ExchangeSpecification.ResilienceSpecification.class);
        given(resilienceSpecification.isRetryEnabled()).willReturn(false);

        ExchangeSpecification spec = mock(ExchangeSpecification.class);
        given(spec.getApiKey()).willReturn("api-key");
        given(spec.getSecretKey()).willReturn("secret-key");
        given(spec.getResilience()).willReturn(resilienceSpecification);

        exchange = mock(BinanceExchange.class);
        given(exchange.getExchangeSpecification()).willReturn(spec);

        binanceAuthenticated = mock(BinanceAuthenticated.class);

        Retry retry = mock(Retry.class);
        RetryRegistry retryRegistry = mock(RetryRegistry.class);
        given(retryRegistry.retry(anyString())).willReturn(retry);

        RateLimiterRegistry rateLimiterRegistry = mock(RateLimiterRegistry.class);
        given(rateLimiterRegistry.rateLimiter(anyString())).willReturn(mock(RateLimiter.class));

        ResilienceRegistries resilienceRegistries = mock(ResilienceRegistries.class);
        given(resilienceRegistries.retries()).willReturn(retryRegistry);
        given(resilienceRegistries.rateLimiters()).willReturn(rateLimiterRegistry);

        accountService = new BinanceAccountService(exchange, binanceAuthenticated, resilienceRegistries);
    }

    @Test
    public void testTransferThrows_params_not_provided() {
        assertThatThrownBy(() -> accountService.internalFundsTransfer(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testTransferThrows_params_wrong_type() {
        assertThatThrownBy(() -> accountService.internalFundsTransfer(new AccountFundsTransferParams() {
            @Override
            public Currency getCurrency() {
                return null;
            }

            @Override
            public BigDecimal getAmount() {
                return null;
            }
        })).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testTransfer_ok() throws IOException {
        BinanceFuturesAccountFundsTransferParams params = new BinanceFuturesAccountFundsTransferParams(Currency.USDT, BigDecimal.ONE, BinanceFutureTransferType.USDT_FUTURES_TO_SPOT);
        FutureTransferResponse response = new FutureTransferResponse(21);
        given(binanceAuthenticated.futuresTransfer(
                anyString(),
                any(BigDecimal.class),
                anyInt(),
                nullable(Long.class),
                nullable(SynchronizedValueFactory.class),
                anyString(),
                any(ParamsDigest.class))
        ).willReturn(response);

        // when
        String tranId = accountService.internalFundsTransfer(params);

        // then
        assertThat(tranId).isEqualTo("21");
        verify(binanceAuthenticated, times(1)).futuresTransfer(
                eq("USDT"),
                eq(BigDecimal.ONE),
                eq(2),
                nullable(Long.class),
                nullable(SynchronizedValueFactory.class),
                anyString(),
                any(ParamsDigest.class));
    }

    @Test
    public void testGetTradingFee() {
        BinanceAccountInformation account = mock(BinanceAccountInformation.class);
        when(account.getMakerCommission()).thenReturn(BigDecimal.TEN);
        when(account.getTakerCommission()).thenReturn(new BigDecimal(5));

        Fee expectedTradingFee = new Fee(new BigDecimal("0.001"), new BigDecimal("0.0005"));
        Fee tradingFee = accountService.getTradingFee(account);

        assertThat(tradingFee).isEqualTo(expectedTradingFee);
    }

    @Test
    public void testGetTradingFees_missingInstrument() throws IOException {
        assertThatThrownBy(() -> accountService.getDynamicTradingFees(Collections.singleton(new FuturesContract(CurrencyPair.ADA_BNB, null))))
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
        List<CurrencyPair> instruments = Arrays.asList(CurrencyPair.ETH_USDT, CurrencyPair.BTC_AUD);
        given(exchange.getExchangeSymbols()).willReturn(instruments);

        BinanceAccountInformation account = mock(BinanceAccountInformation.class);
        given(account.getMakerCommission()).willReturn(BigDecimal.TEN);
        given(account.getTakerCommission()).willReturn(BigDecimal.TEN);
        given(binanceAuthenticated.account(
                nullable(Long.class),
                nullable(SynchronizedValueFactory.class),
                anyString(),
                any(ParamsDigest.class))
        ).willReturn(account);

        Fee tradingFee = accountService.getTradingFee(account);
        Map<Instrument, Fee> tradingFees = accountService.getDynamicTradingFees(new HashSet<>(instruments));

        assertThat(tradingFees.size()).isEqualTo(instruments.size());
        tradingFees.forEach((instrument, fee) -> assertThat(fee).isEqualTo(tradingFee));
    }
}
package org.knowm.xchange.service.fee;

import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.derivative.OptionsContract;
import org.knowm.xchange.dto.account.Fee;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.meta.DerivativeMetaData;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.instrument.Instrument;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetaDataFeeProviderTest {

	private static final BigDecimal TRADING_FEE = BigDecimal.TEN;
	public static final Fee FEE = new Fee(TRADING_FEE, TRADING_FEE);
	private static final CurrencyPair PAIR = CurrencyPair.ADA_BNB;
	private static final FuturesContract FUTURES_CONTRACT = new FuturesContract(PAIR, null);
	private static final OptionsContract OPTIONS_CONTRACT = new OptionsContract("ETH/USD/210719/34000/C");
	public static final Fee DEFAULT_FEE = new Fee(BaseFeeProvider.defaultTradingFee, BaseFeeProvider.defaultTradingFee);
	private Exchange exchange;
	private MetaDataFeeProvider provider;

	@Before
	public void setUp() throws Exception {
		exchange = mock(Exchange.class);

		Map<CurrencyPair, CurrencyPairMetaData> pairs = Collections.singletonMap(PAIR, new CurrencyPairMetaData(TRADING_FEE, null, null, null, null));
		Map<Currency, CurrencyMetaData> currencies = Collections.emptyMap();
		Map<FuturesContract, DerivativeMetaData> futures = Collections.singletonMap(FUTURES_CONTRACT, new DerivativeMetaData(TRADING_FEE, null, null, null, null, null, null, null, null, null, null));
		Map<OptionsContract, DerivativeMetaData> options = Collections.singletonMap(OPTIONS_CONTRACT, new DerivativeMetaData(TRADING_FEE, null, null, null, null, null, null, null, null, null, null));
		ExchangeMetaData metadata = new ExchangeMetaData(pairs, currencies, futures, options, null, null, false);
		when(exchange.getExchangeMetaData()).thenReturn(metadata);

		provider = new MetaDataFeeProvider(exchange);
	}

	@Test
	public void testPair_existing() {
		assertThat(provider.getTradingFee(PAIR)).isEqualTo(FEE);
	}

	@Test
	public void testPair_not_existing() {
		assertThat(provider.getTradingFee(CurrencyPair.XBT_H18))
				.isEqualTo(DEFAULT_FEE);
	}

	@Test
	public void testContract_existing() {
		assertThat(provider.getTradingFee(FUTURES_CONTRACT)).isEqualTo(FEE);
	}

	@Test
	public void testContract_not_existing() {
		assertThat(provider.getTradingFee(new FuturesContract(CurrencyPair.XBT_H18, null)))
				.isEqualTo(DEFAULT_FEE);
	}

	@Test
	public void testOption_existing() {
		assertThat(provider.getTradingFee(OPTIONS_CONTRACT)).isEqualTo(FEE);
	}

	@Test
	public void testOption_not_existing() {
		assertThat(provider.getTradingFee(new OptionsContract("BTC/USD/210719/34000/C")))
				.isEqualTo(DEFAULT_FEE);
	}

	@Test
	public void testMultiple() {
		List<? extends Instrument> instruments = Arrays.asList(PAIR, OPTIONS_CONTRACT, FUTURES_CONTRACT);
		Map<Instrument, Fee> fees = provider.getTradingFees(new HashSet<>(instruments));
		assertThat(fees.size()).isEqualTo(3);
		assertThat(fees.get(PAIR)).isEqualTo(FEE);
		assertThat(fees.get(OPTIONS_CONTRACT)).isEqualTo(FEE);
		assertThat(fees.get(FUTURES_CONTRACT)).isEqualTo(FEE);
	}
}
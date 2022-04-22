package org.knowm.xchange.service.fee;

import org.junit.Test;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.derivative.OptionsContract;
import org.knowm.xchange.dto.account.Fee;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class ConstantFeeProviderTest {

	@Test
	public void testDifferentInstruments() {
		ConstantFeeProvider provider = new ConstantFeeProvider(BigDecimal.ONE, BigDecimal.TEN);
		assertThat(provider.getTradingFee(new FuturesContract(CurrencyPair.ADA_BNB, null))).isNotNull();
		assertThat(provider.getTradingFee(CurrencyPair.ADA_BNB)).isNotNull();
		assertThat(provider.getTradingFee(new OptionsContract("ETH/USD/210719/34000/C"))).isNotNull();
	}

	@Test
	public void testInstrument() {
		ConstantFeeProvider provider = new ConstantFeeProvider(BigDecimal.ONE, BigDecimal.TEN);
		assertThat(provider.getTradingFee(new FuturesContract(CurrencyPair.ADA_BNB, null)))
				.isEqualTo(provider.getTradingFee(new OptionsContract("ETH/USD/210719/34000/C")));
	}

	@Test
	public void testInstrumentValues() {
		ConstantFeeProvider provider = new ConstantFeeProvider(BigDecimal.ONE, BigDecimal.TEN);
		Fee tradingFee = provider.getTradingFee(new FuturesContract(CurrencyPair.ADA_BNB, null));
		assertThat(tradingFee.getTakerFee()).isEqualByComparingTo(BigDecimal.TEN);
		assertThat(tradingFee.getMakerFee()).isEqualByComparingTo(BigDecimal.ONE);
	}

	@Test
	public void testInstrumentsValues() {
		ConstantFeeProvider provider = new ConstantFeeProvider(BigDecimal.ONE, BigDecimal.TEN);
		Fee tradingFee1 = provider.getTradingFee(new FuturesContract(CurrencyPair.ADA_BNB, null));
		Fee tradingFee2 = provider.getTradingFee(new FuturesContract(CurrencyPair.BTC_USD, null));
		assertThat(tradingFee1).isEqualTo(tradingFee2);
	}
}
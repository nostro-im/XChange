package info.bitrich.xchangestream.binance.futures.dto;

import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountUpdateBinanceWebsocketTransactionTest {

	@Test
	public void testGetAvailable() {
		BigDecimal value = AccountUpdateBinanceWebsocketTransaction.getAvailable(new BinanceFuturesWebsocketBalance("USD",
				BigDecimal.TEN,
				BigDecimal.ONE,
				BigDecimal.ONE.negate()
		));

		assertThat(value).isEqualByComparingTo(new BigDecimal("9"));
	}
}
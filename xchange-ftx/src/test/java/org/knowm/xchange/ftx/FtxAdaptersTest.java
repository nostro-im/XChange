package org.knowm.xchange.ftx;

import org.junit.Test;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.AccountMargin;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.OpenPositions;
import org.knowm.xchange.ftx.dto.FtxResponse;
import org.knowm.xchange.ftx.dto.account.FtxAccountDto;
import org.knowm.xchange.ftx.dto.account.FtxPositionDto;
import org.knowm.xchange.ftx.dto.account.FtxWalletBalanceDto;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FtxAdaptersTest {

    @Test
    public void testGetAccountMargin() {
		BigDecimal leverage = BigDecimal.ONE;
		List<FtxPositionDto> positions = Arrays.asList(
                new FtxPositionDto(null, null, null, "BTC-PERP", null, null, null, null, null, null, null, null, BigDecimal.ZERO, null, null, BigDecimal.TEN, null),
                new FtxPositionDto(null, null, null, "ETH-PERP", null, null, null, null, null, null, null, null, BigDecimal.TEN, null, null, BigDecimal.ONE, null),
                new FtxPositionDto(null, null, null, "XRP-PERP", null, null, null, null, null, null, null, null, BigDecimal.TEN, null, null, BigDecimal.TEN.negate(), null)
        );
		BigDecimal totalAccountValue = new BigDecimal("1000");
		BigDecimal unrealizedProfit = new BigDecimal("9").negate();
		BigDecimal marginBalance = totalAccountValue.add(unrealizedProfit);

		FtxAccountDto ftxAccountDto = new FtxAccountDto(false, null, null, null, null, false, null, null, null, null, null, totalAccountValue, BigDecimal.ONE, null, positions);
        OpenPositions openPositions = FtxAdapters.adaptOpenPositions(positions, leverage);

        AccountMargin margin = FtxAdapters.getAccountMargin(ftxAccountDto, openPositions.getOpenPositions());
        assertThat(margin.getCurrency()).isEqualTo(Currency.USD);
		assertThat(margin.getUnrealizedProfit()).isEqualByComparingTo(unrealizedProfit);
        assertThat(margin.getMarginBalance()).isEqualByComparingTo(marginBalance);
    }

	@Test
	public void testGetAccountInfo_wallet() {
		FtxAccountDto account = new FtxAccountDto(false, null, null, null, null, false, null, null, null, null, null, null, null, null, Collections.emptyList());
		List<FtxWalletBalanceDto> balances = Arrays.asList(
			new FtxWalletBalanceDto(Currency.USD, BigDecimal.ONE, BigDecimal.TEN),
			new FtxWalletBalanceDto(Currency.BTC, BigDecimal.ZERO, BigDecimal.ONE)
		);
		FtxResponse<FtxAccountDto> accountResponse = new FtxResponse<>(true, account, false);
		FtxResponse<List<FtxWalletBalanceDto>> balancesResponse = new FtxResponse<>(true, balances, false);

		AccountInfo accountInfo = FtxAdapters.adaptAccountInfo(accountResponse, balancesResponse, Collections.emptyList());

		assertThat(accountInfo).isNotNull();
		assertThat(accountInfo.getWallets().size()).isEqualTo(1);
		assertThat(accountInfo.getWallet().getBalances().size()).isEqualTo(2);
		assertThat(accountInfo.getWallet().getBalance(Currency.USD)).isNotNull();
		assertThat(accountInfo.getWallet().getBalance(Currency.BTC)).isNotNull();
		assertThat(accountInfo.getWallet().getBalance(Currency.EUR)).isEqualTo(Balance.zero(Currency.EUR));
	}

	@Test
	public void testAdaptBalance() {
		Currency currency = Currency.USD;
		BigDecimal free = new BigDecimal("1992.58580007");
		BigDecimal total = new BigDecimal("1995.35220007");
		Balance balance = FtxAdapters.adaptBalance(new FtxWalletBalanceDto(currency, free, total));
		assertThat(balance.getAvailable()).isEqualByComparingTo(new BigDecimal("1992.58580007"));
		assertThat(balance.getTotal()).isEqualByComparingTo(new BigDecimal("1995.35220007"));
		assertThat(balance.getFrozen()).isEqualByComparingTo(new BigDecimal("2.76640000"));
	}
}
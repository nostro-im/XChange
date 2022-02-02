package org.knowm.xchange.ftx;

import org.junit.Test;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.dto.account.*;
import org.knowm.xchange.ftx.dto.FtxResponse;
import org.knowm.xchange.ftx.dto.account.FtxAccountDto;
import org.knowm.xchange.ftx.dto.account.FtxPositionDto;
import org.knowm.xchange.ftx.dto.account.FtxWalletBalanceDto;
import org.knowm.xchange.ftx.dto.trade.FtxOrderSide;

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
        OpenPositions openPositions = FtxAdapters.adaptOpenPositions(positions, leverage, BigDecimal.ZERO);

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

	@Test
	public void testGetPositionSize_buy() {
		BigDecimal size = BigDecimal.ONE;
		FtxOrderSide side = FtxOrderSide.buy;
		FtxPositionDto dto = new FtxPositionDto(null, null, null, null, null, null, null, null, null, null, null, side, size, null, null, null, null);
		assertThat(FtxAdapters.getPositionSize(dto)).isEqualByComparingTo(size);
	}

	@Test
	public void testGetPositionSize_sell() {
		BigDecimal size = BigDecimal.ONE;
		FtxOrderSide side = FtxOrderSide.sell;
		FtxPositionDto dto = new FtxPositionDto(null, null, null, null, null, null, null, null, null, null, null, side, size, null, null, null, null);
		assertThat(FtxAdapters.getPositionSize(dto)).isEqualByComparingTo(size.negate());
	}


	@Test
	public void testGetPositionType_long() {
		FtxOrderSide side = FtxOrderSide.buy;
		FtxPositionDto dto = new FtxPositionDto(null, null, null, null, null, null, null, null, null, null, null, side, BigDecimal.ONE, null, null, null, null);
		assertThat(FtxAdapters.getPositionType(dto)).isEqualByComparingTo(OpenPosition.Type.LONG);
	}

	@Test
	public void testGetPositionType_short() {
		FtxOrderSide side = FtxOrderSide.sell;
		FtxPositionDto dto = new FtxPositionDto(null, null, null, null, null, null, null, null, null, null, null, side, BigDecimal.ONE, null, null, null, null);
		assertThat(FtxAdapters.getPositionType(dto)).isEqualByComparingTo(OpenPosition.Type.SHORT);
	}

	@Test
	public void testAdaptOpenPosition_instrument() {
		OpenPosition position = FtxAdapters.adaptOpenPosition(FTX_POSITION_DTO, FTX_POSITION_LEVERAGE, FTX_TOTAL_ACCOUNT_VALUE);
		assertThat(position.getInstrument()).isEqualTo(new FuturesContract(CurrencyPair.ETH_USD, null));
	}

	@Test
	public void testGetLiquidationPrice_zero() {
		BigDecimal price = BigDecimal.ZERO;
		FtxPositionDto dto = new FtxPositionDto(null, null, price, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
		assertThat(FtxAdapters.getPositionEstimatedLiquidationPrice(dto)).isNull();
	}

	@Test
	public void testGetLiquidationPrice_null() {
		BigDecimal price = null;
		FtxPositionDto dto = new FtxPositionDto(null, null, price, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
		assertThat(FtxAdapters.getPositionEstimatedLiquidationPrice(dto)).isNull();
	}

	@Test
	public void testGetLiquidationPrice_value() {
		BigDecimal price = BigDecimal.ONE;
		FtxPositionDto dto = new FtxPositionDto(null, null, price, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
		assertThat(FtxAdapters.getPositionEstimatedLiquidationPrice(dto)).isEqualByComparingTo(price);
	}

	@Test
	public void testAdaptOpenPosition_size() {
		OpenPosition position = FtxAdapters.adaptOpenPosition(FTX_POSITION_DTO, FTX_POSITION_LEVERAGE, FTX_TOTAL_ACCOUNT_VALUE);
		assertThat(position.getSize()).isEqualByComparingTo(new BigDecimal("0.001"));
	}

	@Test
	public void testAdaptOpenPosition_type() {
		OpenPosition position = FtxAdapters.adaptOpenPosition(FTX_POSITION_DTO, FTX_POSITION_LEVERAGE, FTX_TOTAL_ACCOUNT_VALUE);
		assertThat(position.getType()).isEqualTo(OpenPosition.Type.LONG);
	}

	@Test
	public void testAdaptOpenPosition_leverage() {
		OpenPosition position = FtxAdapters.adaptOpenPosition(FTX_POSITION_DTO, FTX_POSITION_LEVERAGE, FTX_TOTAL_ACCOUNT_VALUE);
		assertThat(position.getLeverage()).isEqualByComparingTo(FTX_POSITION_LEVERAGE);
	}

	@Test
	public void testAdaptOpenPosition_price() {
		OpenPosition position = FtxAdapters.adaptOpenPosition(FTX_POSITION_DTO, FTX_POSITION_LEVERAGE, FTX_TOTAL_ACCOUNT_VALUE);
		assertThat(position.getPrice()).isEqualByComparingTo(new BigDecimal("2750.3"));
	}

	@Test
	public void testAdaptOpenPosition_markPrice() {
		OpenPosition position = FtxAdapters.adaptOpenPosition(FTX_POSITION_DTO, FTX_POSITION_LEVERAGE, FTX_TOTAL_ACCOUNT_VALUE);
		assertThat(position.getMarkPrice()).isNull();
	}

	@Test
	public void testAdaptOpenPosition_liquidationPrice() {
		OpenPosition position = FtxAdapters.adaptOpenPosition(FTX_POSITION_DTO, FTX_POSITION_LEVERAGE, FTX_TOTAL_ACCOUNT_VALUE);
		assertThat(position.getLiquidationPrice()).isNull();
	}

	@Test
	public void testAdaptOpenPosition_mr() {
		OpenPosition position = FtxAdapters.adaptOpenPosition(FTX_POSITION_DTO, FTX_POSITION_LEVERAGE, FTX_TOTAL_ACCOUNT_VALUE);
		assertThat(position.getMarginRatio()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	public void testAdaptOpenPosition_pnl() {
		OpenPosition position = FtxAdapters.adaptOpenPosition(FTX_POSITION_DTO, FTX_POSITION_LEVERAGE, FTX_TOTAL_ACCOUNT_VALUE);
		assertThat(position.getUnrealizedProfit()).isEqualByComparingTo(new BigDecimal("0.0371"));
	}

	@Test
	public void testAdaptOpenPosition_time() {
		OpenPosition position = FtxAdapters.adaptOpenPosition(FTX_POSITION_DTO, FTX_POSITION_LEVERAGE, FTX_TOTAL_ACCOUNT_VALUE);
		assertThat(position.getTimestamp()).isNull();
	}

	@Test
	public void testGetMarginFraction_position_smaller_then_total() {
		BigDecimal totalValue = new BigDecimal("2000.0");
		BigDecimal positionValue = new BigDecimal("150.0");
		assertThat(FtxAdapters.getMarginFraction(totalValue, positionValue)).isEqualByComparingTo(new BigDecimal("13.3"));
	}

	@Test
	public void testGetMarginFraction_position_greater_then_total() {
		BigDecimal totalValue = new BigDecimal("2000.0");
		BigDecimal positionValue = new BigDecimal("10000.0");
		assertThat(FtxAdapters.getMarginFraction(totalValue, positionValue)).isEqualByComparingTo(new BigDecimal("0.2"));
	}

	@Test
	public void testGetMarginRatio_position_greater_then_total() {
		BigDecimal marginMaintenance = new BigDecimal("0.03"); // 3%
		BigDecimal marginFraction = new BigDecimal("0.2"); // 20%, leverage 5x; total 100$; position 500$
		assertThat(FtxAdapters.getMarginRatio(marginMaintenance, marginFraction)).isEqualByComparingTo(new BigDecimal("0.15"));
	}

	@Test
	public void testGetMarginRatio_position_smaller_then_total() {
		BigDecimal marginMaintenance = new BigDecimal("0.03"); // 3%
		BigDecimal marginFraction = new BigDecimal("13.33"); // 1333%
		assertThat(FtxAdapters.getMarginRatio(marginMaintenance, marginFraction)).isEqualByComparingTo(new BigDecimal("0.0023"));
	}

	private static final BigDecimal FTX_TOTAL_ACCOUNT_VALUE = new BigDecimal("1995.37");
	private static final BigDecimal FTX_POSITION_LEVERAGE = new BigDecimal("1");
	private static final FtxPositionDto FTX_POSITION_DTO = new FtxPositionDto(new BigDecimal("2.7874"),
			new BigDecimal("2787.4"),
			new BigDecimal("0.0"),
			"ETH-PERP",
			new BigDecimal("1.0"),
			new BigDecimal("0.0"),
			new BigDecimal("0.03"),
			new BigDecimal("0.001"),
			new BigDecimal("0.001"),
			new BigDecimal("3.52180901"),
			new BigDecimal("0.0"),
			FtxOrderSide.buy,
			new BigDecimal("0.001"),
			new BigDecimal("0.0"),
			new BigDecimal("2.7874"),
			new BigDecimal("0.0371"),
			new BigDecimal("2750.3"));

/*
	{
		"future": "ETH-PERP",
		"size": 0.001,
		"side": "buy",
		"netSize": 0.001,
		"longOrderSize": 0.0,
		"shortOrderSize": 0.0,
		"cost": 2.7874,
		"entryPrice": 2787.4,
		"unrealizedPnl": 0.0,
		"realizedPnl": 3.52180901,
		"initialMarginRequirement": 1.0,
		"maintenanceMarginRequirement": 0.03,
		"openSize": 0.001,
		"collateralUsed": 2.7874,
		"estimatedLiquidationPrice": 0.0,
		"recentAverageOpenPrice": 2750.3,
		"recentPnl": 0.0371,
		"recentBreakEvenPrice": 2750.3,
		"cumulativeBuySize": 0.001,
		"cumulativeSellSize": 0.0
	}
 */

}
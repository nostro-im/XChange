package org.knowm.xchange.binance.futures;

import org.junit.Test;
import org.knowm.xchange.binance.futures.dto.account.BinanceFuturesAccountInformation;
import org.knowm.xchange.binance.futures.dto.account.BinanceFuturesAsset;
import org.knowm.xchange.binance.futures.dto.account.BinanceFuturesPosition;
import org.knowm.xchange.binance.futures.dto.trade.PositionSide;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.knowm.xchange.binance.futures.BinanceFuturesAdapter.adaptPositionType;

public class BinanceFuturesAdapterTest {

    @Test
    public void testAdaptPositionType1() {
        assertThat(adaptPositionType(PositionSide.LONG, BigDecimal.ONE)).isEqualTo(OpenPosition.Type.LONG);
    }
    @Test
    public void testAdaptPositionType2() {
        assertThat(adaptPositionType(PositionSide.SHORT, BigDecimal.ONE)).isEqualTo(OpenPosition.Type.SHORT);
    }
    @Test
    public void testAdaptPositionType3() {
        assertThat(adaptPositionType(PositionSide.BOTH, BigDecimal.ONE)).isEqualTo(OpenPosition.Type.LONG);
    }
    @Test
    public void testAdaptPositionType4() {
        assertThat(adaptPositionType(PositionSide.BOTH, BigDecimal.ONE.negate())).isEqualTo(OpenPosition.Type.SHORT);
    }
    @Test
    public void testAdaptPositionType5() {
        //noinspection ConstantConditions
        assertThat(adaptPositionType(null, null)).isNull();
    }

    @Test
    public void testAdaptAccountInfo_wallet() {
        BigDecimal mb = BigDecimal.TEN;
        BigDecimal pnl = BigDecimal.ONE;
        AccountInfo accountInfo = BinanceFuturesAdapter.adaptAccountInfo(new BinanceFuturesAccountInformation(0, true, true, true, 0, null, null, null, pnl, mb, null, null, null, null, null, null, null, null));

        assertThat(accountInfo.getAccountMargin()).isPresent();
    }

    @Test
    public void testAdaptAccountInfo_positions_null() {
        AccountInfo accountInfo = BinanceFuturesAdapter.adaptAccountInfo(new BinanceFuturesAccountInformation(0, true, true, true, 0, null, null, null, null, null, null, null, null, null, null, null, null, null));

        assertThat(accountInfo.getOpenPositions().size()).isEqualTo(0);
    }

    @Test
    public void testAdaptAccountInfo_balances_null() {
        AccountInfo accountInfo = BinanceFuturesAdapter.adaptAccountInfo(new BinanceFuturesAccountInformation(0, true, true, true, 0, null, null, null, null, null, null, null, null, null, null, null, null, null));

        assertThat(accountInfo.getWallets().size()).isEqualTo(1);
        Wallet wallet = accountInfo.getWallet();
        assertThat(wallet.getBalances().size()).isEqualTo(0);
    }

    @Test
    public void testGetFreeCollateral_null() {
        BinanceFuturesAccountInformation accountInformation = new BinanceFuturesAccountInformation(0, true, true, true, 0, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertThat(BinanceFuturesAdapter.getFreeCollateral(accountInformation, Currency.USDT)).isNull();
    }

    @Test
    public void testGetFreeCollateral_null2() {
        BigDecimal availableBalance = null;
        BinanceFuturesAsset asset = new BinanceFuturesAsset("USDT", null, null, null, null, null, null, null, null, null, availableBalance, null, false, 0);
        BinanceFuturesAccountInformation accountInformation = new BinanceFuturesAccountInformation(0, true, true, true, 0, null, null, null, null, null, null, null, null, null, null, null, Collections.singletonList(asset), null);
        assertThat(BinanceFuturesAdapter.getFreeCollateral(accountInformation, Currency.USDT)).isNull();
    }

    @Test
    public void testGetFreeCollateral() {
        BigDecimal availableBalance = BigDecimal.TEN;
        BinanceFuturesAsset asset = new BinanceFuturesAsset("USDT", null, null, null, null, null, null, null, null, null, availableBalance, null, false, 0);
        BinanceFuturesAccountInformation accountInformation = new BinanceFuturesAccountInformation(0, true, true, true, 0, null, null, null, null, null, null, null, null, null, null, null, Collections.singletonList(asset), null);
        assertThat(BinanceFuturesAdapter.getFreeCollateral(accountInformation, Currency.USDT)).isEqualByComparingTo(new BigDecimal("10.00000"));
    }

    @Test
    public void testGetCurrentLeverage_null() {
        BinanceFuturesAccountInformation accountInformation = new BinanceFuturesAccountInformation(0, true, true, true, 0, null, null, null, null, null, null, null, null, null, null, null, null, null);
        assertThat(BinanceFuturesAdapter.getCurrentLeverage(accountInformation)).isNull();
    }

    @Test
    public void testGetCurrentLeverage_null2() {
        BigDecimal totalPositionInitialMargin = BigDecimal.ONE;
        BinanceFuturesAccountInformation accountInformation = new BinanceFuturesAccountInformation(0, true, true, true, 0, null, null, null, null, null, totalPositionInitialMargin, null, null, null, null, null, null, null);
        assertThat(BinanceFuturesAdapter.getCurrentLeverage(accountInformation)).isNull();
    }

    @Test
    public void testGetCurrentLeverage_null3() {
        BigDecimal totalPositionInitialMargin = BigDecimal.ONE;
        BigDecimal totalWalletBalance = BigDecimal.ZERO;
        BinanceFuturesAccountInformation accountInformation = new BinanceFuturesAccountInformation(0, true, true, true, 0, null, null, totalWalletBalance, null, null, totalPositionInitialMargin, null, null, null, null, null, null, null);
        assertThat(BinanceFuturesAdapter.getCurrentLeverage(accountInformation)).isNull();
    }

    @Test
    public void testGetCurrentLeverage() {
        BigDecimal totalPositionInitialMargin = BigDecimal.ONE;
        BigDecimal totalWalletBalance = BigDecimal.TEN;
        BinanceFuturesAccountInformation accountInformation = new BinanceFuturesAccountInformation(0, true, true, true, 0, null, null, totalWalletBalance, null, null, totalPositionInitialMargin, null, null, null, null, null, null, null);
        assertThat(BinanceFuturesAdapter.getCurrentLeverage(accountInformation)).isEqualByComparingTo(new BigDecimal("0.1"));
    }

    @Test
    public void testGetCurrentLeverage_zero() {
        BigDecimal totalPositionInitialMargin = BigDecimal.ZERO;
        BigDecimal totalWalletBalance = BigDecimal.TEN;
        BinanceFuturesAccountInformation accountInformation = new BinanceFuturesAccountInformation(0, true, true, true, 0, null, null, totalWalletBalance, null, null, totalPositionInitialMargin, null, null, null, null, null, null, null);
        assertThat(BinanceFuturesAdapter.getCurrentLeverage(accountInformation)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void testGetPositionCurrentLeverage_null() {
        BinanceFuturesAccountInformation accountInformation = new BinanceFuturesAccountInformation(0, true, true, true, 0, null, null, null, null, null, null, null, null, null, null, null, null, null);
        BinanceFuturesPosition position = new BinanceFuturesPosition(null, null, null, null, null, null, null, false, null, null, null, null, null, null, 0);
        assertThat(BinanceFuturesAdapter.getCurrentLeverage(position, accountInformation)).isNull();
    }

    @Test
    public void testGetPositionCurrentLeverage_null2() {
        BigDecimal value = BigDecimal.TEN;
        BinanceFuturesAccountInformation accountInformation = new BinanceFuturesAccountInformation(0, true, true, true, 0, null, null, null, null, null, null, null, null, null, null, null, null, null);
        BinanceFuturesPosition position = new BinanceFuturesPosition(null, null, null, null, null, null, null, false, null, null, null, value, null, null, 0);
        assertThat(BinanceFuturesAdapter.getCurrentLeverage(position, accountInformation)).isNull();
    }

    @Test
    public void testGetPositionCurrentLeverage_null3() {
        BigDecimal value = BigDecimal.TEN;
        BigDecimal total = BigDecimal.ZERO;
        BinanceFuturesAccountInformation accountInformation = new BinanceFuturesAccountInformation(0, true, true, true, 0, null, null, total, null, null, null, null, null, null, null, null, null, null);
        BinanceFuturesPosition position = new BinanceFuturesPosition(null, null, null, null, null, null, null, false, null, null, null, value, null, null, 0);
        assertThat(BinanceFuturesAdapter.getCurrentLeverage(position, accountInformation)).isNull();
    }

    @Test
    public void testGetPositionCurrentLeverage() {
        BigDecimal value = BigDecimal.TEN;
        BigDecimal total = new BigDecimal("25");
        BinanceFuturesAccountInformation accountInformation = new BinanceFuturesAccountInformation(0, true, true, true, 0, null, null, total, null, null, null, null, null, null, null, null, null, null);
        BinanceFuturesPosition position = new BinanceFuturesPosition(null, null, null, null, null, null, null, false, null, null, null, value, null, null, 0);
        assertThat(BinanceFuturesAdapter.getCurrentLeverage(position, accountInformation)).isEqualByComparingTo(new BigDecimal("0.4"));
    }

    @Test
    public void testGetPositionCurrentLeverage_zero() {
        BigDecimal value = BigDecimal.ZERO;
        BigDecimal total = new BigDecimal("25");
        BinanceFuturesAccountInformation accountInformation = new BinanceFuturesAccountInformation(0, true, true, true, 0, null, null, total, null, null, null, null, null, null, null, null, null, null);
        BinanceFuturesPosition position = new BinanceFuturesPosition(null, null, null, null, null, null, null, false, null, null, null, value, null, null, 0);
        assertThat(BinanceFuturesAdapter.getCurrentLeverage(position, accountInformation)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void testGetAccountMargins() {
        BigDecimal mb = BigDecimal.TEN;
        BigDecimal pnl = BigDecimal.ONE;
        BigDecimal totalWalletBalance = new BigDecimal("100");
        BigDecimal totalPositionInitialMargin = BigDecimal.ONE;
        BigDecimal availableBalance = new BigDecimal("10");
        BinanceFuturesAsset asset = new BinanceFuturesAsset("USDT", null, null, null, null, null, null, null, null, null, availableBalance, null, false, 0);
        BinanceFuturesAccountInformation account = new BinanceFuturesAccountInformation(0, true, true, true, 0, null, null, totalWalletBalance, pnl, mb, totalPositionInitialMargin, null, null, null, null, null, Collections.singletonList(asset), null);
        Set<AccountMargin> accountMargins = BinanceFuturesAdapter.getAccountMargins(account);

        assertThat(accountMargins.size()).isOne();
        Optional<AccountMargin> margin = accountMargins.stream().findFirst();
        assertThat(margin).isPresent();

        AccountMargin accountMargin = margin.get();
        assertThat(accountMargin.getCurrency()).isEqualTo(Currency.USDT);
        assertThat(accountMargin.getMarginBalance()).isEqualTo(mb);
        assertThat(accountMargin.getUnrealizedProfit()).isEqualTo(pnl);
        assertThat(accountMargin.getLeverage()).isNull(); // not available for binance account - only on position level
        assertThat(accountMargin.getCurrentLeverage()).isEqualByComparingTo(new BigDecimal("0.010000"));
        assertThat(accountMargin.getFreeCollateral()).isEqualByComparingTo(new BigDecimal("10.0000"));
    }
}

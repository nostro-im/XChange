package org.knowm.xchange.binance.futures;

import org.junit.Test;
import org.knowm.xchange.binance.futures.dto.account.BinanceFuturesAccountInformation;
import org.knowm.xchange.binance.futures.dto.trade.PositionSide;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.AccountMargin;
import org.knowm.xchange.dto.account.OpenPosition;
import org.knowm.xchange.dto.account.Wallet;

import java.math.BigDecimal;
import java.util.Optional;

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

        Optional<AccountMargin> margin = accountInfo.getAccountMargin();
        assertThat(margin).isPresent();
        assertThat(margin.get().getCurrency()).isEqualTo(Currency.USDT);
        assertThat(margin.get().getMarginBalance()).isEqualTo(mb);
        assertThat(margin.get().getUnrealizedProfit()).isEqualTo(pnl);
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
}
package org.knowm.xchange.ftx;

import org.junit.Test;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.AccountMargin;
import org.knowm.xchange.dto.account.OpenPositions;
import org.knowm.xchange.ftx.dto.account.FtxAccountDto;
import org.knowm.xchange.ftx.dto.account.FtxPositionDto;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FtxAdaptersTest {

    @Test
    public void testAccountInfo_wallet() {
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
}
package org.knowm.xchange.binance;

import org.junit.Test;
import org.knowm.xchange.binance.dto.account.AssetDividendResponse;
import org.knowm.xchange.binance.dto.trade.BinanceOrder;
import org.knowm.xchange.binance.service.BinanceTradeService.BinanceOrderFlags;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.service.fee.BaseFeeProvider;
import org.knowm.xchange.service.fee.ConstantFeeProvider;
import org.knowm.xchange.service.fee.FeeProvider;
import org.knowm.xchange.utils.ObjectMapperHelper;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class BinanceAdaptersTest {

  private final FeeProvider feeProvider = new ConstantFeeProvider.Builder()
          .takerFee(BaseFeeProvider.defaultTradingFee)
          .makerFee(BaseFeeProvider.defaultTradingFee)
          .build();

  @Test
  public void testFilledMarketOrder() throws IOException {

    //    InputStream is =
    //        BinanceAdaptersTest.class.getResourceAsStream(
    //            "/org/knowm/xchange/binance/filled-market-order.json");
    //    // Use Jackson to parse it
    //    ObjectMapper mapper = new ObjectMapper();
    //    BinanceOrder binanceOrder = mapper.readValue(is, BinanceOrder.class);

    BinanceAdapters adapter = new BinanceAdapters(feeProvider);
    BinanceOrder binanceOrder =
        ObjectMapperHelper.readValue(
            BinanceAdaptersTest.class.getResource(
                "/org/knowm/xchange/binance/filled-market-order.json"),
            BinanceOrder.class);
    Order order = adapter.adaptOrder(binanceOrder);
    assertThat(order).isInstanceOf(MarketOrder.class);
    MarketOrder marketOrder = (MarketOrder) order;
    assertThat(marketOrder.getStatus()).isEqualByComparingTo(Order.OrderStatus.FILLED);
    assertThat(marketOrder.getOriginalAmount()).isEqualByComparingTo("0.10700000");
    assertThat(marketOrder.getCumulativeAmount()).isEqualByComparingTo("0.10700000");
    assertThat(marketOrder.getRemainingAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(marketOrder.getAveragePrice()).isEqualByComparingTo("0.01858383");
    assertThat(marketOrder.getOrderFlags())
        .contains(BinanceOrderFlags.withClientId("gzcLIkn86ag3FycOCEl6Vi"));

    MarketOrder copy = ObjectMapperHelper.viaJSON(marketOrder);
    assertThat(copy).isEqualToComparingFieldByField(marketOrder);
  }

  @Test
  public void testAssetDividendList() throws Exception {
    AssetDividendResponse assetDividendList =
        ObjectMapperHelper.readValue(
            BinanceAdaptersTest.class.getResource(
                "/org/knowm/xchange/binance/asset-dividend-list.json"),
            AssetDividendResponse.class);

    assertThat(assetDividendList.getTotal()).isEqualByComparingTo(BigDecimal.ONE);

    AssetDividendResponse.AssetDividend assetDividend = assetDividendList.getData().get(0);
    assertThat(assetDividend.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(10L));
    assertThat(assetDividend.getAsset()).isEqualTo("BHFT");
    assertThat(assetDividend.getDivTime()).isEqualByComparingTo(1563189166000L);
    assertThat(assetDividend.getEnInfo()).isEqualTo("BHFT distribution");
    assertThat(assetDividend.getTranId()).isEqualByComparingTo(2968885920L);
  }
}

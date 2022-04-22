package org.knowm.xchange.ftx;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.ftx.dto.FtxResponse;
import org.knowm.xchange.ftx.dto.trade.FtxOrderDto;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.service.fee.ConstantFeeProvider;

public class FtxAdapterTest {

  private FtxAdapters ftxAdapters;

  @Before
  public void setUp() throws Exception {
    ConstantFeeProvider feeProvider = new ConstantFeeProvider.Builder()
            .makerFee(new BigDecimal("0.001"))
            .takerFee(new BigDecimal("0.002"))
            .build();
    ftxAdapters = new FtxAdapters(feeProvider);
  }

  @Test
  public void adaptCurrencyPairToFtxPair() {
    assertPair(new CurrencyPair("BTC/USD"),"BTC/USD");
    assertPair(new FuturesContract("BTC/USD/perpetual"), "BTC-PERP");
    assertPair(new FuturesContract("BTC/USD/210625"), "BTC-0625");
  }

  private void assertPair(Instrument instrument, String expAdapted) {
    assertThat(FtxAdapters.adaptInstrumentToFtxMarket(instrument)).isEqualTo(expAdapted);
  }

  @Test
  public void adaptOpenOrdersTest() throws IOException {
    InputStream is =
        FtxAdapterTest.class.getResourceAsStream("/responses/example-ftxOpenOrders.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();
    FtxResponse<List<FtxOrderDto>> ftxResponse =
        mapper.readValue(is, new TypeReference<FtxResponse<List<FtxOrderDto>>>() {});

    assertThat(ftxAdapters.adaptOpenOrders(ftxResponse).getOpenOrders().size()).isEqualTo(1);
  }

  @Test
  public void adaptUserTradesTest() throws IOException {
    InputStream is =
        FtxAdapterTest.class.getResourceAsStream("/responses/example-ftxOrderHistory.json");

    // Use Jackson to parse it
    ObjectMapper mapper = new ObjectMapper();
    FtxResponse<List<FtxOrderDto>> ftxResponse =
        mapper.readValue(is, new TypeReference<FtxResponse<List<FtxOrderDto>>>() {});

    assertThat(ftxAdapters.adaptOpenOrders(ftxResponse).getOpenOrders().size()).isEqualTo(1);
  }
}

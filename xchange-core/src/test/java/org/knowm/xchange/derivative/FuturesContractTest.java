package org.knowm.xchange.derivative;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDate;

import org.junit.Test;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.utils.ObjectMapperHelper;

public class FuturesContractTest {
  
  @Test
  public void testComparator() {
    FuturesContract contractExpire = new FuturesContract("XBT/USD/200925");
    FuturesContract contractPerpetual = new FuturesContract("XBT/USD/perpetual");

    int c = contractExpire.compareTo(contractPerpetual);
    assertThat(c).isEqualTo(-1);
  }

  @Test
  public void testInit() {
    FuturesContract contractExpire = new FuturesContract("XBT/USD/200925");
    assertThat(contractExpire.getCurrencyPair()).isEqualByComparingTo(CurrencyPair.XBT_USD);
    assertThat(contractExpire.getExpireDate()).isEqualTo(LocalDate.of(2020, 9, 25));
  }

  @Test
  public void testInit_perpetual() {
    FuturesContract contractExpire = new FuturesContract("XBT/USD/perpetual");
    assertThat(contractExpire.getCurrencyPair()).isEqualByComparingTo(CurrencyPair.XBT_USD);
    assertThat(contractExpire.getExpireDate()).isNull();
  }
  @Test
  public void testDeepCompare() throws IOException {
    FuturesContract contractExpire = new FuturesContract("XBT/USD/200925");
    FuturesContract jsonCopy = ObjectMapperHelper.viaJSON(contractExpire);
    assertThat(jsonCopy).isEqualTo(contractExpire);
    assertThat(jsonCopy).isEqualByComparingTo(contractExpire);
    assertThat(jsonCopy.hashCode()).isEqualTo(contractExpire.hashCode());
    assertThat(jsonCopy.toString()).isEqualTo(contractExpire.toString());

    FuturesContract contractPerpetual = new FuturesContract("XBT/USD/perpetual");
    FuturesContract jsonCopy2 = ObjectMapperHelper.viaJSON(contractPerpetual);
    assertThat(jsonCopy2).isEqualTo(contractPerpetual);
    assertThat(jsonCopy2).isEqualByComparingTo(contractPerpetual);
    assertThat(jsonCopy2.hashCode()).isEqualTo(contractPerpetual.hashCode());
    assertThat(jsonCopy2.toString()).isEqualTo(contractPerpetual.toString());
  }
}

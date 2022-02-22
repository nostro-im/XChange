package org.knowm.xchange.derivative;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.junit.Test;
import org.knowm.xchange.utils.ObjectMapperHelper;

public class OptionsContractTest {
  @Test
  public void testDeepCompare() throws IOException {
    OptionsContract contractCall = new OptionsContract("ETH/USD/210719/34000/C");
    OptionsContract jsonCopy = ObjectMapperHelper.viaJSON(contractCall);
    assertThat(contractCall).isEqualTo(jsonCopy);
    assertThat(contractCall).isEqualByComparingTo(jsonCopy);
    assertThat(contractCall.hashCode()).isEqualTo(jsonCopy.hashCode());
    assertThat(contractCall.toString()).isEqualTo(jsonCopy.toString());

    OptionsContract contractPut = new OptionsContract("BTC/USDT/210709/34000/P");
    OptionsContract jsonCopy2 = ObjectMapperHelper.viaJSON(contractPut);
    assertThat(contractPut).isEqualTo(jsonCopy2);
    assertThat(contractPut).isEqualByComparingTo(jsonCopy2);
    assertThat(contractPut.hashCode()).isEqualTo(jsonCopy2.hashCode());
    assertThat(contractPut.toString()).isEqualTo(jsonCopy2.toString());
  }
}

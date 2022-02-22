package org.knowm.xchange.deribit.v2.dto.marketdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.knowm.xchange.deribit.v2.dto.Kind;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class DeribitInstrumentTest {

  @Test
  public void deserializeInstrumentTest() throws Exception {

    // given
    InputStream is =
        DeribitInstrument.class.getResourceAsStream(
            "/org/knowm/xchange/deribit/v2/dto/marketdata/example-instrument.json");

    // when
    ObjectMapper mapper = new ObjectMapper();
    DeribitInstrument instrument = mapper.readValue(is, DeribitInstrument.class);

    // then
    assertThat(instrument).isNotNull();

    long expDateMillis = 1550228400000L;

    assertThat(instrument.getTickSize()).isEqualTo(new BigDecimal("0.01"));
    assertThat(instrument.getSettlementPeriod()).isEqualTo("week");
    assertThat(instrument.getQuoteCurrency()).isEqualTo("USD");
    assertThat(instrument.getMinTradeAmount()).isEqualTo(new BigDecimal("1"));
    assertThat(instrument.getKind()).isEqualTo(Kind.future);
    assertThat(instrument.isActive()).isTrue();
    assertThat(instrument.getInstrumentName()).isEqualTo("BTC-15FEB19");
    assertThat(instrument.getExpirationTimestamp()).isEqualTo(expDateMillis);
    assertThat(instrument.getExpirationLocalDate()).isEqualTo(new Timestamp(expDateMillis).toLocalDateTime().toLocalDate());
    assertThat(instrument.getExpirationDate()).isEqualTo(new Date(expDateMillis));
    assertThat(instrument.getCreationTimestamp().getTime()).isEqualTo(1549537259000L);
    assertThat(instrument.getContractSize()).isEqualTo(10);
    assertThat(instrument.getBaseCurrency()).isEqualTo("BTC");
  }
}

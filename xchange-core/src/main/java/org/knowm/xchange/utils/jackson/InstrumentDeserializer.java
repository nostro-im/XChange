package org.knowm.xchange.utils.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.derivative.OptionsContract;
import org.knowm.xchange.instrument.Instrument;

public class InstrumentDeserializer extends JsonDeserializer<Instrument> {

  public InstrumentDeserializer() {
    this(null);
  }

  public InstrumentDeserializer(Class<?> vc) {
    super();
  }

  @Override
  public Instrument deserialize(JsonParser jsonParser, final DeserializationContext ctxt)
      throws IOException {

    final ObjectCodec oc = jsonParser.getCodec();
    final JsonNode node = oc.readTree(jsonParser);
    return fromString(node.asText());
  }

  public static Instrument fromString(String s) {
    long count = s.chars().filter(ch -> ch == '/').count();
    // CurrencyPair (Base/Counter) i.e. BTC/USD
    if (count == 1) return new CurrencyPair(s);
    // Futures/Swaps (Base/Counter/Prompt) i.e. BTC/USD/200925
    if (count == 2) return new FuturesContract(s);
    // Options (Base/Counter/Prompt/StrikePrice/Put?Call) i.e. BTC/USD/200925/8956.67/P
    if (count == 4) return new OptionsContract(s);

    return null;
  }
}

package org.knowm.xchange.dto.marketdata;

import org.knowm.xchange.instrument.Instrument;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class CandleStickData implements Serializable {
	private static final long serialVersionUID = 4503957311342371202L;
	private final Instrument instrument;
    private final List<CandleStick> candleSticks;

    public CandleStickData(Instrument instrument, List<CandleStick> candleSticks) {
        this.instrument = instrument;
        this.candleSticks = candleSticks;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public List<CandleStick> getCandleSticks() {
        return Collections.unmodifiableList(candleSticks);
    }
}

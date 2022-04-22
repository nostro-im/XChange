package org.knowm.xchange.utils;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.derivative.Derivative;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.derivative.OptionsContract;
import org.knowm.xchange.instrument.Instrument;

public class InstrumentUtils {
    public static CurrencyPair getCurrencyPair(Instrument instrument) {
        if (instrument instanceof CurrencyPair)
            return (CurrencyPair) instrument;
        if (instrument instanceof Derivative)
            return ((Derivative) instrument).getCurrencyPair();
        return null;
    }

    public static boolean isPerpetualFuturesContract(Instrument instrument) {
        return instrument instanceof FuturesContract && ((FuturesContract) instrument).isPerpetual();
    }

    public static boolean isBoundedInstrument(Instrument instrument) {
        return instrument instanceof OptionsContract ||
                (instrument instanceof FuturesContract && !(((FuturesContract) instrument).isPerpetual()));
    }

    public static boolean isNonExpiringInstrument(Instrument instrument) {
        return instrument instanceof CurrencyPair || isPerpetualFuturesContract(instrument);
    }

}

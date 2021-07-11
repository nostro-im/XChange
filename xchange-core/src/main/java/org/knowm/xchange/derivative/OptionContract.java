package org.knowm.xchange.derivative;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.instrument.Instrument;

public class OptionContract extends Instrument
        implements Derivative, Comparable<OptionContract>, Serializable {

    private static final long serialVersionUID = 2349723566699857025L;

    /** The underlying currency being traded */
    private Currency baseCurrency;

    /** The currency in which the instrument prices are quoted */
    private Currency quoteCurrency;

    /** The Date when the OptionContract expires */
    private Date expireDate;

    /** The strike value of the OptionContract */
    private BigDecimal strike;

    /** The option type (Call or Put) */
    private String optionType;

    private CurrencyPair currencyPair;

    private static final Comparator<OptionContract> COMPARATOR =
            Comparator.comparing(OptionContract::getBaseCurrency)
                    .thenComparing(OptionContract::getQuoteCurrency)
                    .thenComparing(OptionContract::getExpireDate)
                    .thenComparing(OptionContract::getStrike)
                    .thenComparing(OptionContract::getOptionType);

    private static final ThreadLocal<DateFormat> DATE_PARSER =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("ddMMMyy"));

    public OptionContract(Currency baseCurrency, Currency quoteCurrency,
                          Date expireDate, BigDecimal strike, String optionType) {
        init(baseCurrency, quoteCurrency, expireDate, strike, optionType);
    }

    @JsonCreator
    public OptionContract(final String str) {
        String[] instrumentArgs = str.split("-");

        int start_index = 1;
        if (instrumentArgs.length == 4) {
            // allow representation where base and quote currency are on string (the same symbol)
            start_index = 0;
        } else if (instrumentArgs.length != 5) {
            throw new IllegalArgumentException("Could not parse option contract from '" + str + "'");
        }

        Currency baseCurrency = Currency.getInstance(instrumentArgs[0]);
        Currency quoteCurrency = Currency.getInstance(instrumentArgs[start_index]);

        try {
            Date expireDate = DATE_PARSER.get().parse(instrumentArgs[start_index+1]);
            init(baseCurrency,
                 quoteCurrency,
                 expireDate,
                 new BigDecimal(instrumentArgs[start_index+2]),
                 instrumentArgs[start_index+3]);
        } catch (ParseException e) {
            throw new IllegalArgumentException(
                    "Could not parse expire date from '"
                            + str
                            + "'. It has to be a 'ddMMMyy' date format");
        }
    }

    private void init(Currency baseCurrency, Currency quoteCurrency, Date expireDate, BigDecimal strike, String optionType) {
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
        this.expireDate = expireDate;
        this.strike = strike;
        this.optionType = optionType;

        this.currencyPair = new CurrencyPair(baseCurrency, quoteCurrency);
    }

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public Currency getQuoteCurrency() {
        return quoteCurrency;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public BigDecimal getStrike() {
        return strike;
    }

    public String getOptionType() {
        return optionType;
    }

    @Override
    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }

    @Override
    public int compareTo(final OptionContract that) {
        return COMPARATOR.compare(this, that);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        OptionContract that = (OptionContract) object;
        return baseCurrency.equals(that.baseCurrency) && quoteCurrency.equals(that.quoteCurrency) && expireDate.equals(that.expireDate) && strike.equals(that.strike) && optionType.equals(that.optionType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), baseCurrency, quoteCurrency, expireDate, strike, optionType);
    }

    @JsonValue
    @Override
    public String toString() {
        return String.join("-",
                baseCurrency.getSymbol(),
                quoteCurrency.getSymbol(),
                DATE_PARSER.get().format(expireDate).toUpperCase(),
                strike.toPlainString(),
                optionType
                );
    }
}

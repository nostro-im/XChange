package org.knowm.xchange.derivative;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.instrument.Instrument;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Objects;

public class OptionsContract extends Instrument
    implements Derivative, Comparable<OptionsContract>, Serializable {

  private static final long serialVersionUID = 4546376909031640294L;

  public enum OptionType {
    CALL("C"),
    PUT("P");

    private final String postfix;

    OptionType(String postfix) {
      this.postfix = postfix;
    }

    public static OptionType fromString(String s) {
      if (CALL.postfix.equalsIgnoreCase(s)) return CALL;
      if (PUT.postfix.equalsIgnoreCase(s)) return PUT;
      throw new IllegalArgumentException("Unknown option type: " + s);
    }
  }

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");

  private static final Comparator<OptionsContract> COMPARATOR =
      Comparator.comparing(OptionsContract::getCurrencyPair)
          .thenComparing(OptionsContract::getExpireDate)
          .thenComparing(OptionsContract::getStrike)
          .thenComparing(OptionsContract::getType);

  private final CurrencyPair currencyPair;

  private final LocalDate expireDate;

  private final BigDecimal strike;

  private final OptionType type;

  private OptionsContract(CurrencyPair currencyPair, LocalDate expireDate, BigDecimal strike, OptionType type) {
    this.currencyPair = currencyPair;
    this.expireDate = expireDate;
    this.strike = strike;
    this.type = type;
  }

  @JsonCreator
  public OptionsContract(final String symbol) {
    String[] parts = symbol.split("/");
    if (parts.length != 5) {
      throw new IllegalArgumentException("Could not parse options contract from '" + symbol + "'");
    }

    String base = parts[0];
    String counter = parts[1];
    String expireDate = parts[2];
    String strike = parts[3];
    String type = parts[4];

    this.currencyPair = new CurrencyPair(base, counter);
    try {
      this.expireDate = LocalDate.parse(expireDate, DATE_FORMAT);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Could not parse expire date from '" + symbol + "'");
    }
    this.strike = new BigDecimal(strike);
    this.type = OptionType.fromString(type);
  }

  @Override
  public CurrencyPair getCurrencyPair() {
    return currencyPair;
  }

  public LocalDate getExpireDate() {
    return expireDate;
  }

  public BigDecimal getStrike() {
    return strike;
  }

  public OptionType getType() {
    return type;
  }

  @Override
  public int compareTo(final OptionsContract that) {
    return COMPARATOR.compare(this, that);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final OptionsContract contract = (OptionsContract) o;
    return Objects.equals(currencyPair, contract.currencyPair)
        && Objects.equals(expireDate, contract.expireDate)
        && Objects.equals(strike, contract.strike)
        && Objects.equals(type, contract.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(currencyPair, expireDate, strike, type);
  }

  public static final class Builder {
    private CurrencyPair currencyPair;
    private LocalDate expireDate;
    private BigDecimal strike;
    private OptionType type;

    public Builder() {}

    public Builder currencyPair(CurrencyPair val) {
      currencyPair = val;
      return this;
    }

    public Builder expireDate(LocalDate val) {
      expireDate = val;
      return this;
    }

    public Builder strike(BigDecimal val) {
      strike = val;
      return this;
    }

    public Builder type(OptionType val) {
      type = val;
      return this;
    }

    public OptionsContract build() {
      return new OptionsContract(currencyPair, expireDate, strike, type);
    }
  }

  @JsonValue
  @Override
  public String toString() {
    return currencyPair.base
        + "/"
        + currencyPair.counter
        + "/"
        + DATE_FORMAT.format(this.expireDate)
        + "/"
        + strike
        + "/"
        + type.postfix;
  }
}

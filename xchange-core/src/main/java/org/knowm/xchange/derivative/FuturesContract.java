package org.knowm.xchange.derivative;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Objects;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.instrument.Instrument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class FuturesContract extends Instrument
    implements Derivative, Comparable<FuturesContract>, Serializable {

  private static final long serialVersionUID = 6876906648149216819L;

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");
  private static final String PERPETUAL = "perpetual";

  private static final Comparator<FuturesContract> COMPARATOR =
      Comparator.comparing(FuturesContract::getCurrencyPair)
          .thenComparing(FuturesContract::getExpireDate, Comparator.nullsLast(Comparator.naturalOrder()));

  /** The CurrencyPair the FuturesContract is based upon */
  private final CurrencyPair currencyPair;

  /** The Date when the FuturesContract expires, when null it is perpetual */
  private final LocalDate expireDate;

  public FuturesContract(CurrencyPair currencyPair, LocalDate expireDate) {
    this.currencyPair = currencyPair;
    this.expireDate = expireDate;
  }

  @JsonCreator
  public FuturesContract(final String symbol) {
    String[] parts = symbol.split("/");
    if (parts.length < 3) {
      throw new IllegalArgumentException("Could not parse futures contract from '" + symbol + "'");
    }

    String base = parts[0];
    String counter = parts[1];
    String expireDate = parts[2];
    this.currencyPair = new CurrencyPair(base, counter);
    if (!PERPETUAL.equalsIgnoreCase(expireDate)) {
      try {
        this.expireDate = LocalDate.parse(expireDate, DATE_FORMAT);
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException(
            "Could not parse expire date from '"
                + symbol
                + "'. It has to be either a 'yyMMdd' date or 'perpetual'");
      }
    } else {
      this.expireDate = null;
    }
  }

  @Override
  public CurrencyPair getCurrencyPair() {
    return currencyPair;
  }

  public LocalDate getExpireDate() {
    return expireDate;
  }

  public boolean isPerpetual() {
    return this.expireDate == null;
  }

  @Override
  public int compareTo(final FuturesContract that) {
    return COMPARATOR.compare(this, that);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final FuturesContract contract = (FuturesContract) o;
    return Objects.equals(currencyPair, contract.currencyPair)
        && Objects.equals(expireDate, contract.expireDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(currencyPair, expireDate);
  }

  @JsonValue
  @Override
  public String toString() {
    return currencyPair.base
        + "/"
        + currencyPair.counter
        + "/"
        + (expireDate == null ? PERPETUAL : DATE_FORMAT.format(expireDate));
  }
}

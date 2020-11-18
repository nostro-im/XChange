package org.knowm.xchange.binancemargin.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import org.knowm.xchange.currency.Currency;

public final class BinancemarginAsset {

  private final Currency currency;
  private final BigDecimal borrowed;
  private final BigDecimal free;
  private final BigDecimal interest;
  private final BigDecimal locked;
  private final BigDecimal netAsset;

  public BinancemarginAsset(
      @JsonProperty("asset") String asset,
      @JsonProperty("borrowed") BigDecimal borrowed,
      @JsonProperty("free") BigDecimal free,
      @JsonProperty("interest") BigDecimal interest,
      @JsonProperty("locked") BigDecimal locked,
      @JsonProperty("netAsset") BigDecimal netAsset) {
    this.currency = Currency.getInstance(asset);
    this.borrowed = borrowed;
    this.free = free;
    this.interest = interest;
    this.locked = locked;
    this.netAsset = netAsset;
  }

  public Currency getCurrency() {
    return currency;
  }

  public BigDecimal getTotal() {
    return free.add(locked);
  }

  public BigDecimal getAvailable() {
    return free;
  }

  public BigDecimal getLocked() {
    return locked;
  }

  public BigDecimal getBorrowed() {
	return borrowed;
  }

  public BigDecimal getInterest() {
	return interest;
  }

  public BigDecimal getNetAsset() {
	return netAsset;
  }

  public String toString() {
    return "[" + currency + ", free=" + free + ", locked=" + locked + "]";
  }
}

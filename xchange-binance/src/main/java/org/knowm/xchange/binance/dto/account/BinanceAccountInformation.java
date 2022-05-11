package org.knowm.xchange.binance.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public final class BinanceAccountInformation {

  final BigDecimal makerCommission;
  final BigDecimal takerCommission;
  final BigDecimal buyerCommission;
  final BigDecimal sellerCommission;
  final boolean canTrade;
  final boolean canWithdraw;
  final boolean canDeposit;
  final long updateTime;
  List<BinanceBalance> balances;
  List<String> permissions;

  public BinanceAccountInformation(
      @JsonProperty("makerCommission") BigDecimal makerCommission,
      @JsonProperty("takerCommission") BigDecimal takerCommission,
      @JsonProperty("buyerCommission") BigDecimal buyerCommission,
      @JsonProperty("sellerCommission") BigDecimal sellerCommission,
      @JsonProperty("canTrade") boolean canTrade,
      @JsonProperty("canWithdraw") boolean canWithdraw,
      @JsonProperty("canDeposit") boolean canDeposit,
      @JsonProperty("updateTime") long updateTime,
      @JsonProperty("balances") List<BinanceBalance> balances,
      @JsonProperty("permissions") List<String> permissions) {
    this.makerCommission = makerCommission;
    this.takerCommission = takerCommission;
    this.buyerCommission = buyerCommission;
    this.sellerCommission = sellerCommission;
    this.canTrade = canTrade;
    this.canWithdraw = canWithdraw;
    this.canDeposit = canDeposit;
    this.updateTime = updateTime;
    this.balances = balances;
    this.permissions = permissions;
  }

  public BigDecimal getMakerCommission() {
    return makerCommission;
  }

  public BigDecimal getTakerCommission() {
    return takerCommission;
  }

  public BigDecimal getBuyerCommission() {
    return buyerCommission;
  }

  public BigDecimal getSellerCommission() {
    return sellerCommission;
  }

  public boolean isCanTrade() {
    return canTrade;
  }

  public boolean isCanWithdraw() {
    return canWithdraw;
  }

  public boolean isCanDeposit() {
    return canDeposit;
  }

  public long getUpdateTime() {
    return updateTime;
  }

  public List<BinanceBalance> getBalances() {
    return balances;
  }

  public List<String> getPermissions() {
    return permissions;
  }
}

package org.knowm.xchange.binancemargin.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.binancemargin.BinanceErrorAdapter;
import org.knowm.xchange.binancemargin.dto.BinanceException;
import org.knowm.xchange.binancemargin.dto.account.AssetDetail;
import org.knowm.xchange.binancemargin.dto.account.BinanceAccountInformation;
import org.knowm.xchange.binancemargin.dto.account.BinancemarginAccountInformation;
import org.knowm.xchange.binancemargin.dto.account.DepositAddress;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.AddressWithTag;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.Fee;
import org.knowm.xchange.dto.account.FundingRecord;
import org.knowm.xchange.dto.account.FundingRecord.Status;
import org.knowm.xchange.dto.account.FundingRecord.Type;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.trade.params.DefaultWithdrawFundsParams;
import org.knowm.xchange.service.trade.params.HistoryParamsFundingType;
import org.knowm.xchange.service.trade.params.RippleWithdrawFundsParams;
import org.knowm.xchange.service.trade.params.TradeHistoryParamCurrency;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.TradeHistoryParamsTimeSpan;
import org.knowm.xchange.service.trade.params.WithdrawFundsParams;

public class BinanceAccountService extends BinanceAccountServiceRaw implements AccountService {

  public BinanceAccountService(Exchange exchange) {
    super(exchange);
  }

  private BinancemarginAccountInformation getBinancemarginAccountInformation() throws IOException {
    Long recvWindow =
        (Long) exchange.getExchangeSpecification().getExchangeSpecificParametersItem("recvWindow");
    return super.marginaccount(recvWindow, getTimestamp());
  }

  @Override
  public AccountInfo getAccountInfo() throws IOException {
    try {
      BinancemarginAccountInformation acc = getBinancemarginAccountInformation();
      List<Balance> balances =
          acc.userAssets.stream()
              .map(b -> new Balance(b.getCurrency(), b.getTotal(), b.getAvailable()))
              .collect(Collectors.toList());
      return new AccountInfo(Wallet.Builder.from(balances).build());
    } catch (BinanceException e) {
      throw BinanceErrorAdapter.adapt(e);
    }
  }

  @Override
  public Map<CurrencyPair, Fee> getDynamicTradingFees() throws IOException {
    Long recvWindow =
        (Long) exchange.getExchangeSpecification().getExchangeSpecificParametersItem("recvWindow");
    BinanceAccountInformation acc = super.account(recvWindow, getTimestamp());
    BigDecimal makerFee =
        acc.makerCommission.divide(new BigDecimal("10000"), 4, RoundingMode.UNNECESSARY);
    BigDecimal takerFee =
        acc.takerCommission.divide(new BigDecimal("10000"), 4, RoundingMode.UNNECESSARY);

    Map<CurrencyPair, Fee> tradingFees = new HashMap<>();
    List<CurrencyPair> pairs = exchange.getExchangeSymbols();

    pairs.forEach(pair -> tradingFees.put(pair, new Fee(makerFee, takerFee)));

    return tradingFees;
  }
}

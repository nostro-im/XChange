package org.knowm.xchange.poloniex.service;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.*;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.poloniex.PoloniexAdapters;
import org.knowm.xchange.poloniex.PoloniexErrorAdapter;
import org.knowm.xchange.poloniex.dto.PoloniexException;
import org.knowm.xchange.poloniex.dto.trade.PoloniexDepositsWithdrawalsResponse;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.trade.params.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/** @author Zach Holmes */
public class PoloniexAccountService extends PoloniexAccountServiceRaw implements AccountService {

  /**
   * Constructor
   *
   * @param exchange
   */
  public PoloniexAccountService(Exchange exchange) {

    super(exchange);
  }

  @Override
  public AccountInfo getAccountInfo() throws IOException {
    try {
      List<Balance> balances = PoloniexAdapters.adaptPoloniexBalances(getExchangeWallet());
      return new AccountInfo(Wallet.Builder.from(balances).build());
    } catch (PoloniexException e) {
      throw PoloniexErrorAdapter.adapt(e);
    }
  }

  @Override
  public String withdrawFunds(Currency currency, BigDecimal amount, String address)
      throws IOException {
    // does not support XRP withdrawals, use RippleWithdrawFundsParams instead
    return withdrawFunds(new DefaultWithdrawFundsParams(address, currency, amount));
  }

  @Override
  public String withdrawFunds(WithdrawFundsParams params) throws IOException {
    try {
      if (params instanceof RippleWithdrawFundsParams) {
        RippleWithdrawFundsParams xrpParams = (RippleWithdrawFundsParams) params;

        return withdraw(
            xrpParams.getCurrency(),
            xrpParams.getAmount(),
            xrpParams.getAddress(),
            xrpParams.getTag());
      }

      if (params instanceof DefaultWithdrawFundsParams) {
        DefaultWithdrawFundsParams defaultParams = (DefaultWithdrawFundsParams) params;

        return withdraw(
            defaultParams.getCurrency(),
            defaultParams.getAmount(),
            defaultParams.getAddress(),
            null);
      }

      throw new IllegalStateException("Don't know how to withdraw: " + params);
    } catch (PoloniexException e) {
      throw PoloniexErrorAdapter.adapt(e);
    }
  }

  @Override
  public String requestDepositAddress(Currency currency, String... args) throws IOException {
    try {
      return getDepositAddress(currency.toString());
    } catch (PoloniexException e) {
      throw PoloniexErrorAdapter.adapt(e);
    }
  }

  @Override
  public TradeHistoryParams createFundingHistoryParams() {
    final DefaultTradeHistoryParamsTimeSpan params = new DefaultTradeHistoryParamsTimeSpan();
    params.setStartTime(
        new Date(System.currentTimeMillis() - 366L * 24 * 60 * 60 * 1000)); // just over one year
    params.setEndTime(new Date());
    return params;
  }

  @Override
  public List<FundingRecord> getFundingHistory(TradeHistoryParams params) throws IOException {
    try {
      Date start = null;
      Date end = null;
      if (params instanceof TradeHistoryParamsTimeSpan) {
        start = ((TradeHistoryParamsTimeSpan) params).getStartTime();
        end = ((TradeHistoryParamsTimeSpan) params).getEndTime();
      }
      final PoloniexDepositsWithdrawalsResponse poloFundings =
          returnDepositsWithdrawals(start, end);
      return PoloniexAdapters.adaptFundingRecords(poloFundings);
    } catch (PoloniexException e) {
      throw PoloniexErrorAdapter.adapt(e);
    }
  }

  @Override
  public Map<CurrencyPair, Fee> getDynamicTradingFees() throws IOException {
    Fee fee = getDynamicTradingFee();
    return exchange.getExchangeSymbols().stream().collect(Collectors.toMap(pair -> pair, pair -> fee));
  }

  @Override
  public Map<Instrument, Fee> getDynamicTradingFees(Set<Instrument> instruments) throws IOException {
    Fee fee = getDynamicTradingFee();
    return instruments.stream().collect(Collectors.toMap(instrument -> instrument, instrument -> fee));
  }

  private Fee getDynamicTradingFee() throws IOException {
    try {
      HashMap<String, String> info = getFeeInfo();
      BigDecimal makerFee = new BigDecimal(info.get("makerFee"));
      BigDecimal takerFee = new BigDecimal(info.get("takerFee"));
      return new Fee(makerFee, takerFee);
    } catch (PoloniexException e) {
      throw PoloniexErrorAdapter.adapt(e);
    }
  }
}

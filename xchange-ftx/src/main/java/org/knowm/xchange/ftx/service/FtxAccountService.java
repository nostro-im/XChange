package org.knowm.xchange.ftx.service;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Fee;
import org.knowm.xchange.ftx.FtxAdapters;
import org.knowm.xchange.ftx.dto.FtxResponse;
import org.knowm.xchange.ftx.dto.account.FtxAccountDto;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.account.params.AccountLeverageParams;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class FtxAccountService extends FtxAccountServiceRaw implements AccountService {

  public FtxAccountService(Exchange exchange) {
    super(exchange);
  }

  @Override
  public AccountInfo getAccountInfo() throws IOException {
    return getSubaccountInfo(exchange.getExchangeSpecification().getUserName());
  }

  public AccountInfo getSubaccountInfo(String subaccount) throws IOException {
    FtxResponse<FtxAccountDto> ftxAccountInformation = getFtxAccountInformation(subaccount);
    FtxAccountDto accountDto = ftxAccountInformation.getResult();
    return FtxAdapters.adaptAccountInfo(
        ftxAccountInformation,
        getFtxWalletBalances(subaccount),
        ((FtxTradeService) exchange.getTradeService())
            .getOpenPositionsForSubaccount(subaccount, accountDto, true)
            .getOpenPositions());
  }

  @Override
  public void setLeverage(AccountLeverageParams params) throws IOException {
    setLeverage(exchange.getExchangeSpecification().getUserName(), params.getLeverage());
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
    String subaccount = exchange.getExchangeSpecification().getUserName();
    FtxResponse<FtxAccountDto> ftxAccountInformation = getFtxAccountInformation(subaccount);
    FtxAccountDto accountDto = ftxAccountInformation.getResult();
    return new Fee(accountDto.getMakerFee(), accountDto.getTakerFee());
  }
}

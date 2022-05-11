package org.knowm.xchange.ftx.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.derivative.OptionsContract;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Fee;
import org.knowm.xchange.ftx.FtxAdapters;
import org.knowm.xchange.ftx.FtxAuthenticated;
import org.knowm.xchange.ftx.dto.FtxResponse;
import org.knowm.xchange.ftx.dto.account.FtxAccountDto;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.account.params.AccountLeverageParams;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class FtxAccountService extends FtxAccountServiceRaw implements AccountService {

  public FtxAccountService(Exchange exchange, FtxAuthenticated ftx) {
    super(exchange, ftx);
  }

  @Override
  public AccountInfo getAccountInfo() throws IOException {
    return getSubaccountInfo(exchange.getExchangeSpecification().getUserName());
  }

  @Override
  public Object getRawAccountInfo() throws IOException {
    String subaccount = exchange.getExchangeSpecification().getUserName();
    FtxAccountDto accountInfo = getFtxAccountInformation(subaccount).getResult();
    return ImmutableMap.of(
            "accountInfo", accountInfo,
            "balances", getFtxWalletBalances(subaccount),
            "openPositions", ((FtxTradeService) exchange.getTradeService())
                    .getOpenPositionsForSubaccount(subaccount, accountInfo, true)
                    .getOpenPositions()
    );
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
    String subaccount = exchange.getExchangeSpecification().getUserName();
    FtxResponse<FtxAccountDto> ftxAccountInformation = getFtxAccountInformation(subaccount);
    FtxAccountDto accountDto = ftxAccountInformation.getResult();
    Fee fee = getDynamicTradingFee(accountDto);
    return exchange.getExchangeSymbols().stream().collect(Collectors.toMap(pair -> pair, pair -> fee));
  }

  @Override
  public Map<Instrument, Fee> getDynamicTradingFees(Set<Instrument> instruments) throws IOException {
    for (Instrument instrument : instruments) {
      if (instrument instanceof CurrencyPair) {
        Preconditions.checkArgument(exchange.getExchangeSymbols().contains(instrument), "Exchange meta data does not contain requested instrument: {}", instrument);
      } else if (instrument instanceof FuturesContract) {
        Preconditions.checkArgument(exchange.getExchangeFutures().contains(instrument), "Exchange meta data does not contain requested instrument: {}", instrument);
      } else if (instrument instanceof OptionsContract) {
        Preconditions.checkArgument(exchange.getExchangeOptions().contains(instrument), "Exchange meta data does not contain requested instrument: {}", instrument);
      } else {
        throw new IllegalArgumentException("Instrument is not supported: " + instrument);
      }
    }

    String subaccount = exchange.getExchangeSpecification().getUserName();
    FtxResponse<FtxAccountDto> ftxAccountInformation = getFtxAccountInformation(subaccount);
    FtxAccountDto accountDto = ftxAccountInformation.getResult();
    Fee fee = getDynamicTradingFee(accountDto);
    return instruments.stream().collect(Collectors.toMap(instrument -> instrument, instrument -> fee));
  }

  Fee getDynamicTradingFee(FtxAccountDto accountDto) {
    return new Fee(accountDto.getMakerFee(), accountDto.getTakerFee());
  }
}

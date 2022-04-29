package org.knowm.xchange.ftx.service;

import com.google.common.collect.ImmutableMap;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.OpenPosition;
import org.knowm.xchange.ftx.FtxAdapters;
import org.knowm.xchange.ftx.dto.FtxResponse;
import org.knowm.xchange.ftx.dto.account.FtxAccountDto;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.account.params.AccountLeverageParams;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class FtxAccountService extends FtxAccountServiceRaw implements AccountService {

  public FtxAccountService(Exchange exchange) {
    super(exchange);
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
}

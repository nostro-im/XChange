package org.knowm.xchange.binancemargin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.binancemargin.BinanceAdapters;
import org.knowm.xchange.binancemargin.dto.BinanceException;
import org.knowm.xchange.binancemargin.dto.account.*;
import org.knowm.xchange.binancemargin.dto.account.DepositList.BinanceDeposit;
import org.knowm.xchange.currency.Currency;

public class BinanceAccountServiceRaw extends BinanceBaseService {

  public BinanceAccountServiceRaw(Exchange exchange) {
    super(exchange);
  }

  public BinancemarginAccountInformation marginaccount(Long recvWindow, long timestamp)
      throws BinanceException, IOException {
    return binance.marginAccount(recvWindow, timestamp, super.apiKey, super.signatureCreator);
  }
  
  public BinanceAccountInformation account(Long recvWindow, long timestamp)
      throws BinanceException, IOException {
    return binance.account(recvWindow, timestamp, super.apiKey, super.signatureCreator);
  }

  public AssetDetailResponse requestAssetDetail() throws IOException {
    Long recvWindow =
        (Long) exchange.getExchangeSpecification().getExchangeSpecificParametersItem("recvWindow");
    return binance.assetDetail(recvWindow, getTimestamp(), apiKey, super.signatureCreator);
  }

  private <T> T checkWapiResponse(WapiResponse<T> result) {
    if (!result.success) {
      BinanceException exception;
      try {
        exception = new ObjectMapper().readValue(result.msg, BinanceException.class);
      } catch (Throwable e) {
        exception = new BinanceException(-1, result.msg);
      }
      throw exception;
    }
    return result.getData();
  }
}

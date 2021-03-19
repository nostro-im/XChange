package org.knowm.xchange.binance;

import org.knowm.xchange.binance.service.BinanceMarginAccountService;
import org.knowm.xchange.binance.service.BinanceTradeService;
import org.knowm.xchange.client.ExchangeRestProxyBuilder;
import org.knowm.xchange.service.trade.TradeService;

public class BinanceMarginExchange extends BinanceExchange {
    private BinanceMarginAuthenticated binanceMargin;

    protected TradeService marginTradeService;
    protected BinanceMarginAccountService marginAccountService;

    @Override
    protected void initServices() {
        super.initServices();
        this.binanceMargin =
                ExchangeRestProxyBuilder.forInterface(
                        BinanceMarginAuthenticated.class, getExchangeSpecification())
                        .build();

        this.marginTradeService = new BinanceTradeService(this, binanceMargin, getResilienceRegistries());
        this.marginAccountService = new BinanceMarginAccountService(this, binanceMargin, getResilienceRegistries());
    }

    public TradeService getMarginTradeService() {
        return marginTradeService;
    }

    public BinanceMarginAccountService getMarginAccountService() {
        return marginAccountService;
    }
}

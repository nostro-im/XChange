package org.knowm.xchange.binance.futures;

import org.knowm.xchange.binance.BinanceAdapters;
import org.knowm.xchange.binance.futures.dto.account.BinanceFuturesAccountInformation;
import org.knowm.xchange.binance.futures.dto.account.BinanceFuturesAsset;
import org.knowm.xchange.binance.futures.dto.account.BinanceFuturesIncomeHistoryRecord;
import org.knowm.xchange.binance.futures.dto.account.BinanceFuturesPosition;
import org.knowm.xchange.binance.futures.dto.trade.BinanceFuturesOrder;
import org.knowm.xchange.binance.futures.dto.trade.BinanceFuturesOrderType;
import org.knowm.xchange.binance.futures.dto.trade.PositionSide;
import org.knowm.xchange.binance.futures.dto.trade.WorkingType;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.*;
import org.knowm.xchange.dto.marketdata.CandleStick;
import org.knowm.xchange.dto.marketdata.CandleStickData;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.StopOrder;
import org.knowm.xchange.dto.trade.TrailingStopOrder;
import org.knowm.xchange.instrument.Instrument;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class BinanceFuturesAdapter {
    private static final int marginRatioPrecision = 4; // 4 digits after decimal point
    private static final int leveragePrecision = 2; // 2 digits after decimal point

    public static AccountInfo adaptAccountInfo(BinanceFuturesAccountInformation account) {
        List<Balance> balances =
                Optional.ofNullable(account.assets).orElse(Collections.emptyList()).stream()
                        .map(BinanceFuturesAdapter::adaptBalance)
                        .collect(Collectors.toList());

        List<OpenPosition> openPositions =
                Optional.ofNullable(account.positions).orElse(Collections.emptyList()).stream()
                        .map(position -> BinanceFuturesAdapter.adaptPosition(position, account))
                        .collect(Collectors.toList());

        return AccountInfo.Builder.from(Collections.singleton(Wallet.Builder.from(balances).build()))
                .openPositions(openPositions)
                .timestamp(account.updateTime != 0 ? new Date(account.updateTime) : null)
                .margins(getAccountMargins(account))
                .build();
    }

    public static Set<AccountMargin> getAccountMargins(BinanceFuturesAccountInformation account) {
        // binance futures work with USDT currency when comparing value of other assets
        Currency currency = Currency.USDT;
        AccountMargin margin = new AccountMargin.Builder()
                .currency(currency)
                .marginBalance(account.totalMarginBalance)
                .unrealizedProfit(account.totalUnrealizedProfit)
                .freeCollateral(getFreeCollateral(account, currency))
                .currentLeverage(getCurrentLeverage(account))
                .build();

        return Collections.singleton(margin);
    }

    private static BigDecimal getMarginRatio(BinanceFuturesPosition p, BinanceFuturesAccountInformation a) {
        return a.totalMarginBalance == null || a.totalMarginBalance.compareTo(BigDecimal.ZERO) == 0
                ? null
                : p.maintMargin.divide(a.totalMarginBalance, marginRatioPrecision, RoundingMode.HALF_DOWN);
    }

    public static OpenPosition adaptPosition(BinanceFuturesPosition p, BinanceFuturesAccountInformation a) {
        BigDecimal currentLeverage = getCurrentLeverage(p, a);
        return new OpenPosition.Builder()
                .instrument(adaptInstrument(p.symbol))
                .type(adaptPositionType(p.positionSide, p.positionAmt))
                .size(p.positionAmt)
                .price(p.entryPrice)
                .currentLeverage(currentLeverage)
                .leverage(p.leverage)
                .marginRatio(getMarginRatio(p, a))
                .unrealizedProfit(p.unrealizedProfit)
                .timestamp(p.updateTime != 0 ? new Date(p.updateTime) : null)
                .build();
    }

    public static Balance adaptBalance(BinanceFuturesAsset a) {
        return new Balance.Builder()
                .currency(Currency.getInstance(a.asset))
                .total(a.walletBalance)
                .available(a.availableBalance)
                .timestamp(a.updateTime != 0 ? new Date(a.updateTime) : null)
                .build();
    }

    public static OpenPosition.Type adaptPositionType(PositionSide positionSide, BigDecimal positionAmt) {
        if (positionSide == null) return null;
        switch (positionSide) {
            case LONG: return OpenPosition.Type.LONG;
            case SHORT: return OpenPosition.Type.SHORT;
            default: return positionAmt.signum() > 0 ? OpenPosition.Type.LONG : OpenPosition.Type.SHORT;
        }
    }

    public static Instrument adaptInstrument(String symbol) {
        return new FuturesContract(BinanceAdapters.adaptSymbol(symbol), null);
    }

    public static BinanceFuturesOrderType adaptOrderType(org.knowm.xchange.binance.dto.trade.OrderType type) {
        switch (type) {
            case LIMIT: return BinanceFuturesOrderType.LIMIT;
            case MARKET: return BinanceFuturesOrderType.MARKET;
            case TAKE_PROFIT_LIMIT: return BinanceFuturesOrderType.TAKE_PROFIT;
            case STOP_LOSS_LIMIT: return BinanceFuturesOrderType.STOP;
            case STOP_LOSS: return BinanceFuturesOrderType.STOP_MARKET;
            case TAKE_PROFIT: return BinanceFuturesOrderType.TAKE_PROFIT_MARKET;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    public static Order adaptOrder(BinanceFuturesOrder order) {
        Order.OrderType type = BinanceAdapters.convert(order.side);
        Instrument instrument = adaptInstrument(order.symbol);
        Order.Builder builder;

        switch (order.origType) {
            case TRAILING_STOP_MARKET:
                builder = new TrailingStopOrder.Builder(type, instrument)
                        .triggerPrice(order.activatePrice)
                        .trailingRatio(order.priceRate.movePointLeft(2))
                        .triggerType(convert(order.workingType));
                break;
                
            case STOP:
            case TAKE_PROFIT:
                builder = new StopOrder.Builder(type, instrument)
                        .stopPrice(order.stopPrice)
                        .limitPrice(order.price);
                break;

            case STOP_MARKET:
            case TAKE_PROFIT_MARKET:
                builder = new StopOrder.Builder(type, instrument)
                        .stopPrice(order.stopPrice);
                break;    
                
            case LIMIT:
                builder = new LimitOrder.Builder(type, instrument)
                        .limitPrice(order.price);
                break;
                
            default:
                builder = new MarketOrder.Builder(type, instrument);
                break;
        }
        
        builder.orderStatus(BinanceAdapters.adaptOrderStatus(order.status))
                .originalAmount(order.origQty)
                .id(Long.toString(order.orderId))
                .timestamp(order.getTime())
                .cumulativeAmount(order.executedQty)
                .averagePrice(order.avgPrice);
        
        if (order.clientOrderId != null) {
            builder.userReference(order.clientOrderId);
        }
        return builder.build();
    }

    public static WorkingType convert(TrailingStopOrder.TriggerType triggerType) {
        if (triggerType == TrailingStopOrder.TriggerType.LAST_PRICE) return WorkingType.CONTRACT_PRICE;
        if (triggerType == TrailingStopOrder.TriggerType.MARK_PRICE) return WorkingType.MARK_PRICE;
        return null;
    }

    public static TrailingStopOrder.TriggerType convert(WorkingType workingType) {
        if (workingType == WorkingType.CONTRACT_PRICE) return TrailingStopOrder.TriggerType.LAST_PRICE;
        if (workingType == WorkingType.MARK_PRICE) return TrailingStopOrder.TriggerType.MARK_PRICE;
        return null;
    }

    public static Ticker replaceInstrument(Ticker ticker, FuturesContract futuresContract) {
        return new Ticker.Builder()
	        .instrument(futuresContract)
	        .open(ticker.getOpen())
	        .last(ticker.getLast())
	        .bid(ticker.getBid())
	        .ask(ticker.getAsk())
	        .high(ticker.getHigh())
	        .low(ticker.getLow())
	        .vwap(ticker.getVwap())
	        .volume(ticker.getVolume())
	        .quoteVolume(ticker.getQuoteVolume())
	        .timestamp(ticker.getTimestamp())
	        .bidSize(ticker.getBidSize())
	        .askSize(ticker.getAskSize())
	        .percentageChange(ticker.getPercentageChange())
	        .build();
    }

    public static OrderBook replaceInstrument(OrderBook orderBook, FuturesContract futuresContract) {
        return new OrderBook(
	        orderBook.getTimeStamp(),
	        orderBook.getAsks().stream()
	            .map(order -> LimitOrder.Builder.from(order).instrument(futuresContract).build()),
	        orderBook.getBids().stream()
	            .map(order -> LimitOrder.Builder.from(order).instrument(futuresContract).build()),
	        false);
    }

    public static Trades replaceInstrument(Trades trades, FuturesContract futuresContract) {
        return new Trades(
	        trades.getTrades().stream()
	            .map(t -> Trade.Builder.from(t).instrument(futuresContract).build())
	            .collect(Collectors.toList()),
	        trades.getTradeSortType());
    }

    public static MarketOrder replaceInstrument(MarketOrder market, CurrencyPair pair) {
        return MarketOrder.Builder.from(market).instrument(pair).build();
    }

    public static LimitOrder replaceInstrument(LimitOrder limit, CurrencyPair pair) {
        return LimitOrder.Builder.from(limit).instrument(pair).build();
    }

    public static StopOrder replaceInstrument(StopOrder stop, CurrencyPair pair) {
        return StopOrder.Builder.from(stop).instrument(pair).build();
    }

    public static TrailingStopOrder replaceInstrument(TrailingStopOrder trailingStop, CurrencyPair pair) {
        return TrailingStopOrder.Builder.from(trailingStop).instrument(pair).build();
    }

    public static CandleStickData replaceInstrument(CandleStickData candles, FuturesContract futuresContract) {
    	return new CandleStickData(
        	futuresContract,
        	candles
        		.getCandleSticks()
        		.stream()
        		.map(candle -> replaceInstrument(candle, futuresContract))
        		.collect(Collectors.toList())
        );
    }
    
    public static CandleStick replaceInstrument(CandleStick candle, FuturesContract futuresContract) {
    	return CandleStick.Builder
        	.from(candle)
        	.instrument(futuresContract)
        	.build();
    }

    // available balance!
    public static BigDecimal getFreeCollateral(BinanceFuturesAccountInformation account, Currency currency) {
        final String symbol = BinanceAdapters.toSymbol(currency);
        return Optional.ofNullable(account.assets).orElse(Collections.emptyList()).stream()
                .filter(asset -> asset.asset.equals(symbol))
                .findFirst()
                .map(binanceFuturesAsset -> binanceFuturesAsset.availableBalance)
                .orElse(null);
    }

    // for position
    public static BigDecimal getCurrentLeverage(BinanceFuturesPosition p, BinanceFuturesAccountInformation a) {
        BigDecimal value = p.notional;
        BigDecimal total = p.isolated == true ? p.isolatedWallet : a.totalWalletBalance;
        if (value == null) return null;
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return null;
        return value.divide(total, leveragePrecision, RoundingMode.HALF_EVEN).abs();
    }

    // for whole account
    public static BigDecimal getCurrentLeverage(BinanceFuturesAccountInformation account) {
        BigDecimal value = account.totalPositionInitialMargin;
        BigDecimal total = account.totalWalletBalance;
        if (value == null) return null;
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return null;
        return value.divide(total, leveragePrecision, RoundingMode.HALF_EVEN).abs();
    }

    public static FundingRecord adaptFundingRecord(BinanceFuturesIncomeHistoryRecord incomeHistory) {
        if (incomeHistory.incomeType != BinanceFuturesIncomeHistoryRecord.Type.FUNDING_FEE) {
            return null;
        }
        
        return new FundingRecord.Builder()
                .setType(incomeHistory.income.signum() > -1 ? FundingRecord.Type.FUNDING_FEE_PROFIT : FundingRecord.Type.FUNDING_FEE_LOSS)
                .setStatus(FundingRecord.Status.COMPLETE)
                .setDate(new Date(incomeHistory.time))
                .setCurrency(Currency.getInstance(incomeHistory.asset))
                .setAmount(incomeHistory.income.abs())
                .setInstrument(adaptInstrument(incomeHistory.symbol))
                .setInternalId(String.valueOf(incomeHistory.tranId))
                .build();
    }
}

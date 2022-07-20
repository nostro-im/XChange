package org.knowm.xchange.ftx;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.*;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.meta.*;
import org.knowm.xchange.dto.trade.*;
import org.knowm.xchange.ftx.dto.FtxResponse;
import org.knowm.xchange.ftx.dto.account.FtxAccountDto;
import org.knowm.xchange.ftx.dto.account.FtxFundingPaymentsDto;
import org.knowm.xchange.ftx.dto.account.FtxPositionDto;
import org.knowm.xchange.ftx.dto.account.FtxWalletBalanceDto;
import org.knowm.xchange.ftx.dto.marketdata.*;
import org.knowm.xchange.ftx.dto.trade.*;
import org.knowm.xchange.instrument.Instrument;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FtxAdapters {
  private static final String IMPLIED_COUNTER = "USD";
  public static final String PERPETUAL = "PERP";

  /**
   * Margin ratio precision of the open positions.
   * This is the number of digits after decimal point.
   */
  private static final int marginRatioPrecision = 4; // 4 digits after decimal point

  private static final int leveragePrecision = 2; // 2 digits after decimal point

  private static final BigDecimal MAX_MAKER_FEE = new BigDecimal("0.0002"); // maker fee at Ftx is 0.02 % for a 1 Tier (max value)
  private static final BigDecimal MAX_TAKER_FEE = new BigDecimal("0.0007"); // taker fee at Ftx is 0.07 % for a 1 Tier (max value)

  public static OrderBook adaptOrderBook(
      FtxResponse<FtxOrderbookDto> ftxOrderbookDto, Instrument instrument) {

    List<LimitOrder> asks = new ArrayList<>();
    List<LimitOrder> bids = new ArrayList<>();

    ftxOrderbookDto
        .getResult()
        .getAsks()
        .forEach(
            ftxAsk ->
                asks.add(
                    adaptOrderbookOrder(
                        ftxAsk.getVolume(), ftxAsk.getPrice(), instrument, Order.OrderType.ASK)));

    ftxOrderbookDto
        .getResult()
        .getBids()
        .forEach(
            ftxBid ->
                bids.add(
                    adaptOrderbookOrder(
                        ftxBid.getVolume(), ftxBid.getPrice(), instrument, Order.OrderType.BID)));

    return new OrderBook(Date.from(Instant.now()), asks, bids);
  }

  public static LimitOrder adaptOrderbookOrder(
      BigDecimal amount, BigDecimal price, Instrument instrument, Order.OrderType orderType) {

    return new LimitOrder(orderType, amount, instrument, "", null, price);
  }

  public static AccountInfo adaptAccountInfo(
      FtxResponse<FtxAccountDto> ftxAccountDto,
      FtxResponse<List<FtxWalletBalanceDto>> ftxBalancesDto,
      Collection<OpenPosition> openPositions) {

    FtxAccountDto accountDto = ftxAccountDto.getResult();

    List<Balance> balances = Optional.ofNullable(ftxBalancesDto.getResult()).orElse(Collections.emptyList()).stream()
            .map(FtxAdapters::adaptBalance)
            .collect(Collectors.toList());

    Wallet wallet = Wallet.Builder.from(balances)
            .maxLeverage(getMaxLeverage(accountDto))
            .currentLeverage(getCurrentLeverage(accountDto))
            .build();

    AccountMargin margin = getAccountMargin(ftxAccountDto.getResult(), openPositions);

    return AccountInfo.Builder.from(Collections.singleton(wallet))
            .username(accountDto.getUsername())
            .tradingFee(getTradingFee(accountDto))
            .openPositions(openPositions)
            .timestamp(Date.from(Instant.now()))
            .margins(Collections.singleton(margin))
            .build();
  }

  // account max leverage set through the UI
  static BigDecimal getMaxLeverage(FtxAccountDto dto) {
    return dto.getLeverage();
  }

  // dynamic calculated leverage
  static BigDecimal getCurrentLeverage(FtxAccountDto dto) {
    BigDecimal value = dto.getTotalPositionSize();
    BigDecimal total = dto.getTotalAccountValue();
    if (value == null) return null;
    if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return null;
    return value.divide(total, leveragePrecision, RoundingMode.HALF_EVEN).abs();
  }

  static BigDecimal getCurrentLeverage(FtxPositionDto p, FtxAccountDto a) {
    BigDecimal value = p.getCost();
    BigDecimal total = a.getTotalAccountValue();
    if (value == null) return null;
    if (total == null || total.compareTo(BigDecimal.ZERO) == 0) return null;
    return value.divide(total, leveragePrecision, RoundingMode.HALF_EVEN).abs();
  }

  static Balance adaptBalance(FtxWalletBalanceDto dto) {
    return new Balance(dto.getCoin(), dto.getTotal(), dto.getFree());
  }

  public static ExchangeMetaData adaptExchangeMetaData(FtxMarketsDto marketsDto) {

    Map<CurrencyPair, CurrencyPairMetaData> currencyPairs = new HashMap<>();
    Map<Currency, CurrencyMetaData> currency = new HashMap<>();
    Map<FuturesContract, DerivativeMetaData> futures = new HashMap<>();

    marketsDto
        .getMarketList()
        .forEach(
            ftxMarketDto -> {
              if ("spot".equals(ftxMarketDto.getType())) {
                CurrencyPair currencyPair =
                    new CurrencyPair(
                        ftxMarketDto.getBaseCurrency(), ftxMarketDto.getQuoteCurrency());

                CurrencyPairMetaData currencyPairMetaData =
                    new CurrencyPairMetaData.Builder()
                        .amountStepSize(ftxMarketDto.getPriceIncrement())
                        .minimumAmount(ftxMarketDto.getSizeIncrement())
                        .priceScale(ftxMarketDto.getPriceIncrement().scale())
                        .baseScale(ftxMarketDto.getSizeIncrement().scale())
                        .makerFee(MAX_MAKER_FEE)
                        .takerFee(MAX_TAKER_FEE)
                        .build();

                currencyPairs.put(currencyPair, currencyPairMetaData);
                if (!currency.containsKey(currencyPair.base)) {
                  currency.put(
                      currencyPair.base,
                      new CurrencyMetaData(
                          ftxMarketDto.getSizeIncrement().scale(), BigDecimal.ZERO));
                }
                if (!currency.containsKey(currencyPair.counter)) {
                  currency.put(
                      currencyPair.counter,
                      new CurrencyMetaData(
                          ftxMarketDto.getPriceIncrement().scale(), BigDecimal.ZERO));
                }
              } else if ("future".equals(ftxMarketDto.getType())) {
                Instrument instrument = adaptFtxMarketToInstrument(ftxMarketDto.getName());
                if (instrument instanceof FuturesContract) {
                  DerivativeMetaData futuresContractMetaData =
                      new DerivativeMetaData.Builder()
                          .minimumAmount(ftxMarketDto.getSizeIncrement())
                          .amountStepSize(ftxMarketDto.getSizeIncrement())
                          .amountScale(ftxMarketDto.getSizeIncrement().scale())
                          .priceStepSize(ftxMarketDto.getPriceIncrement())
                          .priceScale(ftxMarketDto.getPriceIncrement().scale())
                          .makerFee(MAX_MAKER_FEE)
                          .takerFee(MAX_TAKER_FEE)
                          .build();

                  futures.put((FuturesContract) instrument, futuresContractMetaData);
                }
              }
            });

    RateLimit[] rateLimits = {new RateLimit(30, 1, TimeUnit.SECONDS)};

    return new ExchangeMetaData(
        currencyPairs, currency, futures, null, rateLimits, rateLimits, true);
  }

  public static FtxOrderRequestPayload adaptMarketOrderToFtxOrderPayload(MarketOrder marketOrder) {
    return adaptOrderToFtxOrderPayload(FtxOrderType.market, marketOrder, null);
  }

  public static FtxOrderRequestPayload adaptLimitOrderToFtxOrderPayload(LimitOrder limitOrder) {
    return adaptOrderToFtxOrderPayload(FtxOrderType.limit, limitOrder, limitOrder.getLimitPrice());
  }

  public static FtxModifyOrderRequestPayload adaptModifyOrderToFtxOrderPayload(
      LimitOrder limitOrder) {
    return new FtxModifyOrderRequestPayload(
        limitOrder.getLimitPrice(), limitOrder.getOriginalAmount(), limitOrder.getUserReference());
  }

  private static FtxOrderRequestPayload adaptOrderToFtxOrderPayload(
      FtxOrderType type, Order order, BigDecimal price) {
    return new FtxOrderRequestPayload(
        adaptInstrumentToFtxMarket(order.getInstrument()),
        adaptOrderTypeToFtxOrderSide(order.getType()),
        price,
        type,
        order.getOriginalAmount(),
        order.hasFlag(FtxOrderFlags.REDUCE_ONLY),
        order.hasFlag(FtxOrderFlags.IOC),
        order.hasFlag(FtxOrderFlags.POST_ONLY),
        order.getUserReference());
  }

  public static Trades adaptTrades(List<FtxTradeDto> ftxTradeDtos, Instrument instrument) {
    List<Trade> trades = new ArrayList<>();

    ftxTradeDtos.forEach(
        ftxTradeDto ->
            trades.add(
                new Trade.Builder()
                    .id(ftxTradeDto.getId())
                    .instrument(instrument)
                    .originalAmount(ftxTradeDto.getSize())
                    .price(ftxTradeDto.getPrice())
                    .timestamp(ftxTradeDto.getTime())
                    .type(adaptFtxOrderSideToOrderType(ftxTradeDto.getSide()))
                    .build()));

    return new Trades(trades);
  }

  public static UserTrades adaptUserTrades(List<FtxOrderDto> ftxUserTrades) {
    List<UserTrade> userTrades = new ArrayList<>();

    ftxUserTrades.forEach(
        ftxOrderDto -> {
          if (ftxOrderDto.getFilledSize().compareTo(BigDecimal.ZERO) != 0) {
            userTrades.add(
                new UserTrade.Builder()
                    .instrument(adaptFtxMarketToInstrument(ftxOrderDto.getMarket()))
                    .timestamp(ftxOrderDto.getCreatedAt())
                    .id(ftxOrderDto.getId())
                    .orderId(ftxOrderDto.getId())
                    .orderUserReference(ftxOrderDto.getClientId())
                    .originalAmount(ftxOrderDto.getFilledSize())
                    .type(adaptFtxOrderSideToOrderType(ftxOrderDto.getSide()))
                    .price(
                        ftxOrderDto.getAvgFillPrice() == null
                            ? ftxOrderDto.getPrice()
                            : ftxOrderDto.getAvgFillPrice())
                    .build());
          }
        });

    return new UserTrades(userTrades, Trades.TradeSortType.SortByTimestamp);
  }

  public static UserTrade adaptUserTrade(FtxUserTradeDto ftxUserTrade) {
    return new UserTrade.Builder()
            .instrument(adaptFtxMarketToInstrument(ftxUserTrade.getMarket()))
            .timestamp(ftxUserTrade.getTime())
            .id(ftxUserTrade.getId())
            .orderId(ftxUserTrade.getOrderId())
            .originalAmount(ftxUserTrade.getSize())
            .type(adaptFtxOrderSideToOrderType(ftxUserTrade.getSide()))
            .price(ftxUserTrade.getPrice())
            .feeCurrency(Currency.getInstance(ftxUserTrade.getFeeCurrency()))
            .feeAmount(ftxUserTrade.getFee())
            .build();
  }

  public static UserTrades adaptUserTradesFromTrades(List<FtxUserTradeDto> ftxUserTrades) {
    List<UserTrade> userTrades = ftxUserTrades.stream()
            .filter(dto -> dto.getSize().compareTo(BigDecimal.ZERO) != 0)
            .map(FtxAdapters::adaptUserTrade)
            .collect(Collectors.toList());
    return new UserTrades(userTrades, Trades.TradeSortType.SortByTimestamp);
  }

  public static Order adaptOrder(FtxOrderDto ftxOrderDto) {
    Order.OrderType type = adaptFtxOrderSideToOrderType(ftxOrderDto.getSide());
    Instrument instrument = adaptFtxMarketToInstrument(ftxOrderDto.getMarket());
    Order.Builder builder;
    if (ftxOrderDto.getType().equals(FtxOrderType.limit)) {
      builder = new LimitOrder.Builder(type, instrument).limitPrice(ftxOrderDto.getPrice());
    } else {
      builder = new MarketOrder.Builder(type, instrument);
    }
    
    return builder
        .originalAmount(ftxOrderDto.getSize())
        .averagePrice(ftxOrderDto.getAvgFillPrice())
        .userReference(ftxOrderDto.getClientId())
        .timestamp(ftxOrderDto.getCreatedAt())
        .flags(
            Collections.unmodifiableSet(
                new HashSet<>(
                    Arrays.asList(
                        (ftxOrderDto.isIoc() ? FtxOrderFlags.IOC : null),
                        (ftxOrderDto.isPostOnly() ? FtxOrderFlags.POST_ONLY : null),
                        (ftxOrderDto.isReduceOnly() ? FtxOrderFlags.REDUCE_ONLY : null)))))
        .cumulativeAmount(ftxOrderDto.getFilledSize())
        .orderStatus(ftxOrderDto.getStatus())
        .id(ftxOrderDto.getId())
        .build();
  }
  
  public static List<Order> adaptOrderHistoryRespose(FtxResponse<List<FtxOrderDto>> ftxOpenOrdersResponse) {
    return ftxOpenOrdersResponse
            .getResult()
            .stream()
            .map(FtxAdapters::adaptOrder)
            .collect(Collectors.toList());
  }

  public static OpenOrders adaptOpenOrders(FtxResponse<List<FtxOrderDto>> ftxOpenOrdersResponse) {
    List<LimitOrder> limitOrders = new ArrayList<>();
    List<Order> otherOrders = new ArrayList<>();
    ftxOpenOrdersResponse
        .getResult()
        .forEach(ftxOrderDto -> {
          Order order = adaptOrder(ftxOrderDto);
          if (order instanceof LimitOrder) {
            limitOrders.add((LimitOrder) order);
          } else {
            otherOrders.add(order);
          }
        });

    return new OpenOrders(limitOrders, otherOrders);
  }

  public static FtxOrderSide adaptOrderTypeToFtxOrderSide(Order.OrderType orderType) {

    switch (orderType) {
      case ASK:
        return FtxOrderSide.sell;
      case BID:
        return FtxOrderSide.buy;
      case EXIT_ASK:
        return FtxOrderSide.buy;
      case EXIT_BID:
        return FtxOrderSide.sell;
      default:
        return null;
    }
  }

  public static Order.OrderType adaptFtxOrderSideToOrderType(FtxOrderSide ftxOrderSide) {

    return ftxOrderSide == FtxOrderSide.buy ? Order.OrderType.BID : Order.OrderType.ASK;
  }

  public static String adaptInstrumentToFtxMarket(Instrument instrument) {
    if (instrument instanceof FuturesContract) {
      FuturesContract futuresContract = (FuturesContract) instrument;
      String date;
      if (futuresContract.isPerpetual()) {
        date = PERPETUAL; 
      } else {
        Instant instant = futuresContract.getExpireDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
        date = String.format("%02d%02d", zdt.getMonthValue(), zdt.getDayOfMonth());
      }
      return futuresContract.getCurrencyPair().base + "-" + date;
    }
    return instrument.toString();
  }

  public static Instrument adaptFtxMarketToInstrument(String marketName) {
    long count = marketName.chars().filter(ch -> ch == '/').count();
    if (count == 1) {
      return new CurrencyPair(marketName);
    }
    count = marketName.chars().filter(ch -> ch == '-').count();
    if (count == 1) {
      CurrencyPair currencyPair = new CurrencyPair(marketName.split("-")[0], IMPLIED_COUNTER);
      return new FuturesContract(currencyPair, parseFuturesContractDate(marketName));
    }
    return null;
  }

  public static OpenPositions adaptOpenPositions(FtxAccountDto accountDto, List<FtxPositionDto> ftxPositionDtos) {
    List<OpenPosition> positions = ftxPositionDtos.stream()
            .filter(dto -> dto.getSize().compareTo(BigDecimal.ZERO) > 0)
            .map(dto -> adaptOpenPosition(accountDto, dto))
            .collect(Collectors.toList());
    return new OpenPositions(positions);
  }

  static OpenPosition adaptOpenPosition(FtxAccountDto accountDto, FtxPositionDto dto) {
    return new OpenPosition.Builder()
            .instrument(adaptFtxMarketToInstrument(dto.getFuture()))
            .price(getPositionPrice(dto))
            .size(getPositionSize(dto))
            .type(getPositionType(dto))
            .currentLeverage(getCurrentLeverage(dto, accountDto))
            .leverage(getMaxLeverage(accountDto))
            .marginRatio(getMarginRatio(dto.getMaintenanceMarginRequirement(), getMarginFraction(accountDto.getTotalAccountValue(), dto.getCost())))
            .unrealizedProfit(dto.getRecentPnl())
            .liquidationPrice(getPositionEstimatedLiquidationPrice(dto))
            .build();
  }

  public static BigDecimal lendingRounding(BigDecimal value) {
    return value.setScale(4, RoundingMode.DOWN);
  }

  public static Ticker adaptTicker(
      FtxResponse<FtxMarketDto> ftxMarketResp,
      FtxResponse<List<FtxCandleDto>> ftxCandlesResp,
      Instrument instrument) {

    FtxCandleDto lastCandle = ftxCandlesResp.getResult().get(ftxCandlesResp.getResult().size() - 1);

    BigDecimal open = lastCandle.getOpen();
    BigDecimal last = ftxMarketResp.getResult().getLast();
    BigDecimal bid = ftxMarketResp.getResult().getBid();
    BigDecimal ask = ftxMarketResp.getResult().getAsk();
    BigDecimal high = lastCandle.getHigh();
    BigDecimal low = lastCandle.getLow();
    BigDecimal volume = lastCandle.getVolume();
    Date timestamp = lastCandle.getStartTime();

    return new Ticker.Builder()
        .instrument(instrument)
        .open(open)
        .last(last)
        .bid(bid)
        .ask(ask)
        .high(high)
        .low(low)
        .volume(volume)
        .timestamp(timestamp)
        .build();
  }

  static LocalDate parseFuturesContractDate(String name) {
    try {
      String[] split = name.split("-");
      if (PERPETUAL.equals(split[1])) {
        return null;
      }
      int m = Integer.parseInt(split[1].substring(0, 2));
      int d = Integer.parseInt(split[1].substring(2, 4));
      Instant instant =
              Instant.now().atZone(TimeZone.getDefault().toZoneId()).withMonth(m).withDayOfMonth(d).toInstant();
      if (instant.isBefore(Instant.now())) {
        instant = instant.atZone(TimeZone.getDefault().toZoneId()).plus(1, ChronoUnit.YEARS).toInstant();
      }
      return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    } catch (Exception e) {
      throw new IllegalArgumentException(
              "Could not parse futures contract from name '" + name + "'");
    }
  }

  static AccountMargin getAccountMargin(FtxAccountDto accountDto, Collection<OpenPosition> openPositions) {
    BigDecimal unrealizedProfit = openPositions.stream()
            .map(OpenPosition::getUnrealizedProfit)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal marginBalance = Optional.ofNullable(accountDto.getTotalAccountValue()).map(v -> v.add(unrealizedProfit)).orElse(null);

    BigDecimal maxLeverage = getMaxLeverage(accountDto);
    BigDecimal currentLeverage = getCurrentLeverage(accountDto);
    return new AccountMargin.Builder()
            .currency(Currency.USD)
            .marginBalance(marginBalance)
            .unrealizedProfit(unrealizedProfit)
            .freeCollateral(accountDto.getFreeCollateral())
            .leverage(maxLeverage)
            .currentLeverage(currentLeverage)
            .build();
  }

  static BigDecimal getPositionEstimatedLiquidationPrice(FtxPositionDto dto) {
    BigDecimal price = dto.getEstimatedLiquidationPrice();
    if (price != null && price.compareTo(BigDecimal.ZERO) == 0) return null;
    return price;
  }

  static BigDecimal getPositionPrice(FtxPositionDto dto) {
    return dto.getRecentAverageOpenPrice() != null ? dto.getRecentAverageOpenPrice() : dto.getEntryPrice();
  }

  static OpenPosition.Type getPositionType(FtxPositionDto dto) {
    return dto.getSide() == FtxOrderSide.buy ? OpenPosition.Type.LONG : OpenPosition.Type.SHORT;
  }

  static BigDecimal getPositionSize(FtxPositionDto dto) {
    return dto.getSide() == FtxOrderSide.buy ? dto.getSize() : dto.getSize().negate();
  }

  // Margin fraction = Total Account value / position size
  // set scale to 4, when to converted to % will be more clear
  static BigDecimal getMarginFraction(BigDecimal totalValue, BigDecimal positionValue) {
    if (positionValue == null || positionValue.compareTo(BigDecimal.ZERO) == 0) return null;
    return totalValue.divide(positionValue, RoundingMode.HALF_DOWN);
  }

  // Margin Ratio = Maintenance requirement / Margin fraction
  // set scale to 4, when to converted to % will be more clear
  static BigDecimal getMarginRatio(BigDecimal marginMaintenance, BigDecimal marginFraction) {
    if (marginFraction == null || marginFraction.compareTo(BigDecimal.ZERO) == 0) return null;
    return marginMaintenance.divide(marginFraction, marginRatioPrecision, RoundingMode.HALF_DOWN);
  }

  // MR Calculations Example

  // Input
  // Total collateral: US$1,995.37
  // Free collateral: US$1,992.59
  // Max position leverage:1x
  // Margin fraction: 71721.66%
  // Maintenance margin requirement: 3%
  // pos size 0.001 ETH
  // pos notional = 2.782$

  // Calculations
  // Margin fraction = 1995,37 / 2,782 = 717,242990654205607
  // Margin Ratio = 0.03 / 717,242990654205607 = 0,000041826829109
  // MR % = 0,004% - NO RISK

  public static Fee getTradingFee(FtxAccountDto accountDto) {
    return new Fee(accountDto.getMakerFee(), accountDto.getTakerFee());
  }
  
  public static Integer adaptDate(Date date) {
  	if (date == null) {
  		return null;
    }
    return Math.toIntExact(date.getTime()/1000);
  }

  public static FundingRecord adaptFundingRecord(FtxFundingPaymentsDto dto) {
    FuturesContract futuresContract = (FuturesContract) adaptFtxMarketToInstrument(dto.getFuture());
    Currency currency = futuresContract.getCurrencyPair().counter;
    FundingRecord.Type type = dto.getPayment().signum() < 0 ? FundingRecord.Type.FUNDING_FEE_PROFIT : FundingRecord.Type.FUNDING_FEE_LOSS;
    return new FundingRecord.Builder()
            .setInternalId(dto.getId())
            .setDate(dto.getTime())
            .setType(type)
            .setStatus(FundingRecord.Status.COMPLETE)
            .setCurrency(currency)
            .setAmount(dto.getPayment().abs())
            .setInstrument(futuresContract)
            .build();
  }
}


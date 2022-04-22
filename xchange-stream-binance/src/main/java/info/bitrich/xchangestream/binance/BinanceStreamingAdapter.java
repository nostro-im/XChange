package info.bitrich.xchangestream.binance;

import info.bitrich.xchangestream.binance.dto.ExecutionReportBinanceUserTransaction;
import org.knowm.xchange.binance.BinanceAdapters;
import org.knowm.xchange.binance.dto.trade.BinanceOrder;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.UserTrade;

import java.math.BigDecimal;

public class BinanceStreamingAdapter {
	private final BinanceAdapters binanceAdapter;

	public BinanceStreamingAdapter(BinanceAdapters binanceAdapter) {
		this.binanceAdapter = binanceAdapter;
	}

	public UserTrade adaptUserTrade(ExecutionReportBinanceUserTransaction transaction) {
		if (transaction.getExecutionType() != ExecutionReportBinanceUserTransaction.ExecutionType.TRADE) throw new IllegalStateException("Not a trade");
		return new UserTrade.Builder()
				.type(BinanceAdapters.convert(transaction.getSide()))
				.originalAmount(transaction.getLastExecutedQuantity())
				.currencyPair(transaction.getCurrencyPair())
				.price(transaction.getLastExecutedPrice())
				.timestamp(transaction.getEventTime())
				.id(Long.toString(transaction.getTradeId()))
				.orderId(Long.toString(transaction.getOrderId()))
				.feeAmount(transaction.getCommissionAmount())
				.feeCurrency(Currency.getInstance(transaction.getCommissionAsset()))
				.build();
	}

	public Order adaptOrder(ExecutionReportBinanceUserTransaction transaction) {
		return binanceAdapter.adaptOrder(
				new BinanceOrder(
						BinanceAdapters.toSymbol(transaction.getCurrencyPair()),
						transaction.getOrderId(),
						transaction.getClientOrderId(),
						transaction.getOrderPrice(),
						transaction.getOrderQuantity(),
						transaction.getCumulativeFilledQuantity(),
						transaction.getCumulativeQuoteAssetTransactedQuantity(),
						transaction.getCurrentOrderStatus(),
						transaction.getTimeInForce(),
						transaction.getOrderType(),
						transaction.getSide(),
						transaction.getStopPrice(),
						BigDecimal.ZERO,
						transaction.getOrderCreationTime(),
						transaction.getTimestamp()));
	}
}

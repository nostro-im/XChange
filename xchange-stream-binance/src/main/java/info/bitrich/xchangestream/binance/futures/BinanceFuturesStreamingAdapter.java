package info.bitrich.xchangestream.binance.futures;

import info.bitrich.xchangestream.binance.futures.dto.OrderTradeUpdate;
import info.bitrich.xchangestream.binance.futures.dto.OrderTradeUpdateBinanceUserTransaction;
import org.knowm.xchange.binance.BinanceAdapters;
import org.knowm.xchange.binance.futures.BinanceFuturesAdapter;
import org.knowm.xchange.binance.futures.dto.trade.BinanceFuturesOrder;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.UserTrade;

public class BinanceFuturesStreamingAdapter {

	private final BinanceFuturesAdapter binanceFuturesAdapter;

	public BinanceFuturesStreamingAdapter(BinanceFuturesAdapter binanceFuturesAdapter) {
		this.binanceFuturesAdapter = binanceFuturesAdapter;
	}

	public Order adaptOrder(OrderTradeUpdateBinanceUserTransaction transaction) {
		OrderTradeUpdate orderTradeUpdate = transaction.getOrderTradeUpdate();
		return binanceFuturesAdapter.adaptOrder(new BinanceFuturesOrder(
				orderTradeUpdate.getAveragePrice(),
				orderTradeUpdate.getClientOrderId(),
				orderTradeUpdate.getCumulativeFilledQuantity(),
				orderTradeUpdate.getCumulativeFilledQuantity(),
				orderTradeUpdate.getOrderId(),
				orderTradeUpdate.getOrderQuantity(),
				orderTradeUpdate.getOriginalOrderType(),
				orderTradeUpdate.getOrderPrice(),
				orderTradeUpdate.isReduceOnly(),
				orderTradeUpdate.getSide(),
				orderTradeUpdate.getPositionSide(),
				orderTradeUpdate.getCurrentOrderStatus(),
				orderTradeUpdate.getStopPrice(),
				orderTradeUpdate.isCloseAll(),
				orderTradeUpdate.getSymbol(),
				orderTradeUpdate.getTimestamp(),
				orderTradeUpdate.getTimeInForce(),
				orderTradeUpdate.getOrderType(),
				orderTradeUpdate.getActivationPrice(),
				orderTradeUpdate.getCallbackRate(),
				transaction.getTransactionTime(),
				orderTradeUpdate.getStopPriceWorkingType(),
				false));
	}

	public UserTrade adaptUserTrade(OrderTradeUpdateBinanceUserTransaction transaction) {
		OrderTradeUpdate orderTradeUpdate = transaction.getOrderTradeUpdate();
		return new UserTrade.Builder()
				.type(BinanceAdapters.convert(orderTradeUpdate.getSide()))
				.originalAmount(orderTradeUpdate.getLastExecutedQuantity())
				.instrument(new FuturesContract(BinanceAdapters.adaptSymbol(orderTradeUpdate.getSymbol()), null))
				.price(orderTradeUpdate.getLastExecutedPrice())
				.timestamp(transaction.getEventTime())
				.id(Long.toString(orderTradeUpdate.getTradeId()))
				.orderId(Long.toString(orderTradeUpdate.getOrderId()))
				.feeAmount(orderTradeUpdate.getCommissionAmount())
				.feeCurrency(Currency.getInstance(orderTradeUpdate.getCommissionAsset()))
				.orderUserReference(orderTradeUpdate.getClientOrderId())
				.build();
	}
}

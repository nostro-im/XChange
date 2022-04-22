package org.knowm.xchange.service.fee;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.Fee;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.instrument.Instrument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class BaseFeeProvider implements FeeProvider {
	Logger log = LoggerFactory.getLogger(BaseFeeProvider.class);

	/**
	 * Default currency precision (a.k.a., currency scale) in case it isn't provided by
	 * the exchange for either the base or counter currencies.
	 * This is the number of digits after decimal point.
	 */
	static int defaultCurrencyPrecision = 8; // 8 digits after decimal point

	/**
	 * Default trading fee value in case it isn't provided by the exchange.
	 */
	public static BigDecimal defaultTradingFee = new BigDecimal("0.001").setScale(defaultCurrencyPrecision, RoundingMode.UNNECESSARY); // 0.1%

	/**
	 * calculates fee based on input params:
	 * fee = amount*price
	 *
	 * @param amount executed amount
	 * @param price price
	 * @param instrument instrument
	 * @param isMaker maker/taker
	 * @return calculated fee value
	 */
	@Override
	public final BigDecimal calculateFee(BigDecimal amount, BigDecimal price, Instrument instrument, boolean isMaker) {
		Fee fee = getTradingFee(instrument);
		Currency feeCurrency = getFeeCurrency(instrument);
		return amount.multiply(price)
				.multiply(isMaker ? fee.getMakerFee() : fee.getTakerFee())
				.setScale(getFeeCurrencyPrecision(feeCurrency), RoundingMode.UP);
	}

	/**
	 * calculates fee based on {@link Order}:
	 *
	 * @param order order
	 * @return calculated fee value
	 */
	@Override
	public final BigDecimal calculateFee(Order order) {
		return calculateFee(order.getCumulativeAmount(),
				order.getAveragePrice(), order.getInstrument(),
				(order instanceof MarketOrder) == false); // TODO: clarify
	}

	/**
	 * Returns input-currency's precision.
	 * @param currency currency
	 * @return either precision from exchange metadata or {@link BaseFeeProvider#defaultCurrencyPrecision}
	 */
	public int getFeeCurrencyPrecision(Currency currency) { return defaultCurrencyPrecision; }
}

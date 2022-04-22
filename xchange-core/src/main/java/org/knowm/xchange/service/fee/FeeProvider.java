package org.knowm.xchange.service.fee;

import com.google.common.base.Preconditions;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.derivative.OptionsContract;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.Fee;
import org.knowm.xchange.instrument.Instrument;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.knowm.xchange.utils.InstrumentUtils.getCurrencyPair;

public interface FeeProvider {
	/**
	 * Provides either dynamic or static trading fees per each {@link Instrument}
	 * @param instruments set of instruments
	 * @return map of {@link Fee} per {@link Instrument}
	 */
	Map<Instrument, Fee> getTradingFees(Set<Instrument> instruments);

	/**
	 * Provides either dynamic or static trading fee per {@link Instrument}
	 * @param instrument instrument
	 * @return see {@link Fee}
	 */
	default Fee getTradingFee(Instrument instrument) {
		Preconditions.checkNotNull(instrument);

		return getTradingFees(Collections.singleton(instrument)).get(instrument);
	}

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
	BigDecimal calculateFee(BigDecimal amount, BigDecimal price, Instrument instrument, boolean isMaker);

	/**
	 * calculates fee based on {@link Order}:
	 *
	 * @param order order
	 * @return calculated fee value
	 */
	BigDecimal calculateFee(Order order);

	default Currency getFeeCurrency(Instrument instrument) {
		CurrencyPair pair = getCurrencyPair(instrument);
		return instrument instanceof OptionsContract ? pair.base : pair.counter;
	}
}

package org.knowm.xchange.service.fee;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.derivative.FuturesContract;
import org.knowm.xchange.derivative.OptionsContract;
import org.knowm.xchange.dto.account.Fee;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.meta.DerivativeMetaData;
import org.knowm.xchange.instrument.Instrument;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.knowm.xchange.utils.InstrumentUtils.getCurrencyPair;

public class MetaDataFeeProvider extends BaseFeeProvider {
	private Exchange exchange;

	public MetaDataFeeProvider(Exchange exchange) {
		this.exchange = exchange;
	}

	@Override
	public Map<Instrument, Fee> getTradingFees(Set<Instrument> instruments) {
		return instruments.stream().collect(Collectors.toMap(
				instrument -> instrument,
				instrument -> {
					BigDecimal fee = getMetaDataTradingFeeOrDefault(instrument);
					return new Fee(fee, fee);
				}
		));
	}

	/**
	 * Provides fee from the exchange meta data per {@link Instrument} or default value
	 * @param instrument instrument
	 * @return metadata trading fee or default {@link BaseFeeProvider#defaultTradingFee}
	 */
	BigDecimal getMetaDataTradingFeeOrDefault(Instrument instrument) {
		BigDecimal tradingFee = getMetaDataTradingFee(instrument);
		if (tradingFee == null) {
			log.warn("No trading fee provided for instrument={} exchange={}; default trading fee={}", instrument, exchange, defaultTradingFee);
			return defaultTradingFee;
		} else {
			return tradingFee;
		}
	}

	/**
	 * Provides fee from the exchange meta data per {@link Instrument}
	 * @param instrument instrument
	 * @return {@link Fee} or null if not available
	 */
	BigDecimal getMetaDataTradingFee(Instrument instrument) {
		if (instrument instanceof CurrencyPair){
			return Optional.ofNullable(exchange.getExchangeMetaData().getCurrencyPairs().get(getCurrencyPair(instrument)))
					.map(CurrencyPairMetaData::getTradingFee)
					.orElse(null);
		}
		if (instrument instanceof FuturesContract) {
			return Optional.ofNullable(exchange.getExchangeMetaData().getFutures().get(instrument))
					.map(DerivativeMetaData::getTradingFee)
					.orElse(null);
		}
		if (instrument instanceof OptionsContract) {
			return Optional.ofNullable(exchange.getExchangeMetaData().getOptions().get(instrument))
					.map(DerivativeMetaData::getTradingFee)
					.orElse(null);
		}
		return null;
	}

	@Override
	public int getFeeCurrencyPrecision(Currency currency) {
		// Get minimum amount from Exchange static meta-data (i.e., its JSON resource file)
		return Optional.ofNullable(exchange.getExchangeMetaData().getCurrencies().get(currency))
				.map(CurrencyMetaData::getScale)
				.orElse(defaultCurrencyPrecision);
		
	}
}

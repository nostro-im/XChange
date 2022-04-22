package org.knowm.xchange.service.fee;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.Fee;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.instrument.Instrument;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

class DefaultFeeProvider extends BaseFeeProvider {
	private static final int cacheExpirationInMinutes = 60;

	private static final Cache<Instrument, Fee> cache = CacheBuilder.newBuilder()
			.expireAfterWrite(cacheExpirationInMinutes, TimeUnit.MINUTES)
			.build();

	private final Exchange exchange;
	private final FeeProvider constantFeeProvider;

	public DefaultFeeProvider(Exchange exchange, FeeProvider constantFeeProvider) {
		this.exchange = exchange;
		this.constantFeeProvider = constantFeeProvider;
	}

	/**
	 * Invalidates all cache entries.
	 */
	public void clearCache() {
		cache.invalidateAll();
	}

	@Override
	public Map<Instrument, Fee> getTradingFees(Set<Instrument> instruments) {
		log.debug("get trading fees. exchange={} instruments={}", exchange, instruments);

		Preconditions.checkNotNull(exchange);
		Preconditions.checkNotNull(instruments);
		if (instruments.isEmpty()) return Collections.emptyMap();

		// get cached & missing
		Map<Instrument, Fee> items = new HashMap<>();
		HashSet<Instrument> missing = new HashSet<>();
		instruments.forEach(instrument -> {
			Fee fee = getCachedDynamicTradingFee(instrument);
			if (fee != null) {
				items.put(instrument, fee);
			} else {
				missing.add(instrument);
			}
		});

		// initiate fetch and fill MISSING items with dynamic fees
		if (!missing.isEmpty()) {
			try {
				Map<Instrument, Fee> fees = fetchDynamicTradingFees(missing);
				items.putAll(fees);
				missing.removeAll(fees.keySet());
			} catch (IOException e) {
				log.error("failed to fetch dynamic trading fees", e);
			}
		}

		// fill MISSING items with constant values if fetch failed
		if (!missing.isEmpty()) {
			Map<Instrument, Fee> fees = constantFeeProvider.getTradingFees(missing);
			items.putAll(fees);
			missing.removeAll(fees.keySet());
		}

		// assert
		Preconditions.checkArgument(missing.isEmpty());
		Preconditions.checkArgument(items.keySet().equals(instruments));

		log.trace("trading fees. exchange={} fees={}", exchange, items);
		return items;
	}

	@Override
	public int getFeeCurrencyPrecision(Currency currency) {
		// Get minimum amount from Exchange static meta-data (i.e., its JSON resource file)
		CurrencyMetaData currencyMetaData = exchange.getExchangeMetaData().getCurrencies().get(currency);
		if (currencyMetaData == null || currencyMetaData.getScale() == null) {
			log.debug("No currency precision provided for currency={} exchange={}. default precision={}", currency, exchange, defaultCurrencyPrecision);
			return defaultCurrencyPrecision;
		} else {
			return currencyMetaData.getScale();
		}
	}

	private Fee getCachedDynamicTradingFee(Instrument instrument) {
		return cache.getIfPresent(instrument);
	}

	private Map<Instrument, Fee> fetchDynamicTradingFees(Set<Instrument> instruments) throws IOException {
		log.debug("fetch trading fees. exchange={} instruments={}", exchange, instruments);
		Map<Instrument, Fee> fees = Optional.ofNullable(exchange.getAccountService().getDynamicTradingFees(instruments))
				.orElse(Collections.emptyMap());
		fees.forEach(cache::put);
		return fees;
	}
}

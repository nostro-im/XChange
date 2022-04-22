package org.knowm.xchange.service.fee;

import org.knowm.xchange.dto.account.Fee;
import org.knowm.xchange.instrument.Instrument;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ConstantFeeProvider extends BaseFeeProvider {

	final private BigDecimal makerFee;
	final private BigDecimal takerFee;

	ConstantFeeProvider(BigDecimal makerFee, BigDecimal takerFee) {
		this.makerFee = makerFee;
		this.takerFee = takerFee;
	}

	@Override
	public Map<Instrument, Fee> getTradingFees(Set<Instrument> instruments) {
		return instruments.stream().collect(Collectors.toMap(
				instrument -> instrument,
				instrument -> new Fee(makerFee, takerFee)
		));
	}

	public static final class Builder {
		private BigDecimal makerFee;
		private BigDecimal takerFee;

		public Builder() { }

		public Builder makerFee(BigDecimal makerFee) {
			this.makerFee = makerFee;
			return this;
		}

		public Builder takerFee(BigDecimal takerFee) {
			this.takerFee = takerFee;
			return this;
		}

		public ConstantFeeProvider build() {
			return new ConstantFeeProvider(makerFee, takerFee);
		}
	}
}
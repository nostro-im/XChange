package org.knowm.xchange.service.fee;

import org.knowm.xchange.Exchange;

import java.math.BigDecimal;

public class FeeProviderBuilder {
	private final Exchange exchange;
	private BigDecimal defaultMakerFee;
	private BigDecimal defaultTakerFee;

	FeeProviderBuilder(Exchange exchange) {
		this.exchange = exchange;
	}

	public static FeeProviderBuilder from(Exchange exchange) {
		return new FeeProviderBuilder(exchange);
	}

	public FeeProviderBuilder defaultMakerFee(BigDecimal makerFee) {
		this.defaultMakerFee = makerFee;
		return this;
	}

	public FeeProviderBuilder defaultTakerFee(BigDecimal takerFee) {
		this.defaultTakerFee = takerFee;
		return this;
	}

	public FeeProvider build() {
		// fallback fee provider
		FeeProvider constantFeeProvider = new ConstantFeeProvider.Builder()
				.makerFee(defaultMakerFee)
				.takerFee(defaultTakerFee)
				.build();
		return new DefaultFeeProvider(exchange, constantFeeProvider);
	}
}

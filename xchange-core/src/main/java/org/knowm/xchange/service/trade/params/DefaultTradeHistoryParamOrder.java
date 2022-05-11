package org.knowm.xchange.service.trade.params;

public class DefaultTradeHistoryParamOrder implements TradeHistoryParamOrderUserReference {
	private String orderId;
	private String userReference;

	public DefaultTradeHistoryParamOrder() {
	}

	@Override
	public String getOrderId() {
		return orderId;
	}

	@Override
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	@Override
	public String getUserReference() {
		return userReference;
	}

	@Override
	public void setUserReference(String userReference) {
		this.userReference = userReference;
	}
}

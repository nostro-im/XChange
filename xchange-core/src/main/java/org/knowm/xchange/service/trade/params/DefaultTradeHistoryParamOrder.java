package org.knowm.xchange.service.trade.params;

public class DefaultTradeHistoryParamOrder implements TradeHistoryParamOrderUserReference {
	private String id;
	private String userReference;

	public DefaultTradeHistoryParamOrder() {
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
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

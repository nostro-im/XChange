package org.knowm.xchange.ftx.service;

import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.service.trade.params.TradeHistoryParamInstrument;
import org.knowm.xchange.service.trade.params.TradeHistoryParamOrder;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.knowm.xchange.service.trade.params.TradeHistoryParamsTimeSpan;

import java.util.Date;

public class FtxTradeHistoryParams implements TradeHistoryParams, TradeHistoryParamInstrument, TradeHistoryParamsTimeSpan, TradeHistoryParamOrder {
	private Instrument instrument;
	private Date startTime;
	private Date endTime;
	private String orderId;
	
	@Override
	public Instrument getInstrument() {
		return instrument;
	}

	@Override
	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}

	@Override
	public Date getStartTime() {
		return startTime;
	}

	@Override
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	@Override
	public Date getEndTime() {
		return endTime;
	}

	@Override
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@Override
	public String getOrderId() {
		return orderId;
	}

	@Override
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
}

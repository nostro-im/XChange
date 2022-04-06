package org.knowm.xchange.service.trade.params;

import org.knowm.xchange.instrument.Instrument;

import java.util.Date;

public class DefaultOrderHistoryParamsInstrumentSpan implements InstrumentParam, OrderHistoryParamsTimeSpan {
    
    private Instrument instrument;
    private Date startTime;
    private Date endTime;

    public DefaultOrderHistoryParamsInstrumentSpan() {
    }

    public DefaultOrderHistoryParamsInstrumentSpan(Instrument instrument, Date startTime, Date endTime) {
        this.instrument = instrument;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public Instrument getInstrument() {
        return instrument;
    }

    @Override
    public void setInstrument(final Instrument instrument) {
        this.instrument = instrument;
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(Date time) {
        startTime = time;
    }
    
    @Override
    public Date getEndTime() {
        return endTime;
    }

    @Override
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
}

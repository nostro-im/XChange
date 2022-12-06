package org.knowm.xchange.dto.marketdata;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.math.BigDecimal;
import java.util.Date;

import org.knowm.xchange.instrument.Instrument;

/**
 * Data object representing a CandleStick
 */
@JsonDeserialize(builder = CandleStick.Builder.class)
public class CandleStick {

	private final Instrument instrument;
	private final Long interval; // in seconds
	private final Date timestamp;
	private final Date lastUpdated; // in case of continuous updates
	private final Boolean isClosed;
    private final BigDecimal open;
    private final BigDecimal high;
    private final BigDecimal low;
    private final BigDecimal close;
    private final BigDecimal volume;
    private final BigDecimal quoteVolume;
    private final BigDecimal vwap; // these 5 fields can be null if not provided by the exchange
    private final BigDecimal bid;
    private final BigDecimal bidSize;
    private final BigDecimal ask;
    private final BigDecimal askSize;

    public CandleStick(
    		Instrument instrument,
    		Long intervalSeconds,
    		Date timestamp,
    		Date lastUpdated,
    		Boolean isClosed,
    		BigDecimal open,
    		BigDecimal high,
    		BigDecimal low,
    		BigDecimal close,
    		BigDecimal volume,
    		BigDecimal quoteVolume,
    		BigDecimal vwap,
    		BigDecimal bid,
            BigDecimal bidSize,
            BigDecimal ask,
            BigDecimal askSize) {
    	this.instrument = instrument;
    	this.interval = intervalSeconds;
        this.timestamp = timestamp;
        this.lastUpdated = lastUpdated;
        this.isClosed = isClosed;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.quoteVolume = quoteVolume;
        this.vwap = vwap;
        this.bid = bid;
        this.bidSize = bidSize;
        this.ask = ask;
        this.askSize = askSize;
    }

    public Instrument getInstrument() {
    	return instrument;
    }
    
    public Long getInterval() {
        return interval;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public Date getLastUpdated() {
        return lastUpdated;
    }
    
    public Boolean isClosed() {
        return isClosed;
    }
    
    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public BigDecimal getQuoteVolume() {
        return quoteVolume;
    }

    public BigDecimal getVwap() {
        return vwap;
    }

    public BigDecimal getBid() {
        return bid;
    }

    public BigDecimal getBidSize() {
        return bidSize;
    }

    public BigDecimal getAsk() {
        return ask;
    }

    public BigDecimal getAskSize() {
        return askSize;
    }
    
    @Override
    public String toString() {
      return "CandleStick [instrument: "
          + instrument
          + ", interval: "
          + interval
          + ", timestamp: "
          + timestamp
          + ", open: "
          + open
          + ", high: "
          + high
          + ", low: "
          + low
          + ", close: "
          + close
          + ", volume: "
          + volume
          + ", isClosed: "
          + isClosed
          + "]";
    }
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
    	private Instrument instrument;
    	private Long interval;
        private Date timestamp;
        private Date lastUpdated;
    	private Boolean isClosed;
        private BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal close;
        private BigDecimal volume;
        private BigDecimal quoteVolume;
        private BigDecimal vwap;
        private BigDecimal bid;
        private BigDecimal bidSize;
        private BigDecimal ask;
        private BigDecimal askSize;


        public static Builder from(CandleStick candleStick) {
            return new Builder()
                    .instrument(candleStick.getInstrument())
                    .interval(candleStick.getInterval())
                    .timestamp(candleStick.getTimestamp())
                    .lastUpdated(candleStick.getLastUpdated())
                    .isClosed(candleStick.isClosed())
                    .open(candleStick.getOpen())
                    .high(candleStick.getHigh())
                    .low(candleStick.getLow())
                    .close(candleStick.getClose())
                    .volume(candleStick.getVolume())
                    .quoteVolume(candleStick.getQuoteVolume())
                    .vwap(candleStick.getVwap())
                    .bid(candleStick.getBid())
                    .bidSize(candleStick.getBidSize())
                    .ask(candleStick.getAsk())
                    .askSize(candleStick.getAskSize());
        }
        
        public Builder instrument(Instrument instrument) {
            this.instrument = instrument;
            return this;
        }

        public Builder interval(Long intervalSeconds) {
            this.interval = intervalSeconds;
            return this;
        }

        public Builder timestamp(Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder lastUpdated(Date lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }
        
        public Builder isClosed(Boolean isClosed) {
            this.isClosed = isClosed;
            return this;
        }
        
        public Builder open(BigDecimal open) {
            this.open = open;
            return this;
        }

        public Builder high(BigDecimal high) {
            this.high = high;
            return this;
        }

        public Builder low(BigDecimal low) {
            this.low = low;
            return this;
        }

        public Builder close(BigDecimal close) {
            this.close = close;
            return this;
        }

        public Builder volume(BigDecimal volume) {
            this.volume = volume;
            return this;
        }

        public Builder quoteVolume(BigDecimal quoteVolume) {
            this.quoteVolume = quoteVolume;
            return this;
        }

        public Builder vwap(BigDecimal vwap) {
            this.vwap = vwap;
            return this;
        }

        public Builder bid(BigDecimal bid) {
            this.bid = bid;
            return this;
        }

        public Builder bidSize(BigDecimal bidSize) {
            this.bidSize = bidSize;
            return this;
        }

        public Builder ask(BigDecimal ask) {
            this.ask = ask;
            return this;
        }

        public Builder askSize(BigDecimal askSize) {
            this.askSize = askSize;
            return this;
        }

        public CandleStick build() {
            return new CandleStick(
            	instrument,
            	interval,
            	timestamp,
            	lastUpdated,
            	isClosed,
            	open,
            	high,
            	low,
            	close,
            	volume,
            	quoteVolume,
            	vwap,
            	bid,
            	bidSize,
            	ask,
            	askSize);
        }
    }
}

package info.bitrich.xchangestream.binance;

public enum BinanceSubscriptionType {
    DEPTH("depth"),
    TRADE("trade"),
    TICKER("ticker"),
    KLINE_1s("kline_1s"),
    KLINE_1m("kline_1m"),
    KLINE_3m("kline_3m"),
    KLINE_5m("kline_5m"),
    KLINE_15m("kline_15m"),
    KLINE_30m("kline_30m"),
    KLINE_1h("kline_1h"),
    KLINE_2h("kline_2h"),
    KLINE_4h("kline_4h"),
    KLINE_6h("kline_6h"),
    KLINE_8h("kline_8h"),
    KLINE_12h("kline_12h"),
    KLINE_1d("kline_1d"),
    KLINE_3d("kline_3d"),
    KLINE_1w("kline_1w"),
    KLINE_1M("kline_1M");

    private String type;

    BinanceSubscriptionType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
    
    public static BinanceSubscriptionType klineOf(int intervalSeconds) {
    	switch (intervalSeconds) {
    	case 1:
    		return KLINE_1s;
    	case 60:
    		return KLINE_1m;
    	case 3*60:
    		return KLINE_3m;
    	case 5*60:
    		return KLINE_5m;
    	case 15*60:
    		return KLINE_15m;
    	case 30*60:
    		return KLINE_30m;
    	case 60*60:
    		return KLINE_1h;
    	case 2*60*60:
    		return KLINE_2h;
    	case 4*60*60:
    		return KLINE_4h;
    	case 8*60*60:
    		return KLINE_8h;
    	case 12*60*60:
    		return KLINE_12h;
    	case 24*60*60:
    		return KLINE_1d;
    	case 3*24*60*60:
    		return KLINE_3d;
    	case 7*24*60*60:
    		return KLINE_1w;
    	case 30*24*60*60:
    		return KLINE_1M;
    	default:
            throw new RuntimeException("BinanceSubscriptionType not supported for interval: " + intervalSeconds);
    	}
    }
}

package org.knowm.xchange.ftx.dto.trade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Date;

public class FtxUserTradeDto {

    private final String id;
    private final String market;
    private final String future;
    private final String baseCurrency;
    private final String quoteCurrency;
    private final String type;
    private final FtxOrderSide side;
    private final BigDecimal price;
    private final BigDecimal size;
    private final String orderId;
    private final Date time;
    private final String tradeId;
    private final BigDecimal feeRate;
    private final BigDecimal fee;
    private final String feeCurrency;
    private final FtxUserTradeLiquidityType liquidity;

    @JsonCreator
    public FtxUserTradeDto(
            @JsonProperty("id") String id,
            @JsonProperty("market") String market,
            @JsonProperty("future") String future,
            @JsonProperty("baseCurrency") String baseCurrency,
            @JsonProperty("quoteCurrency") String quoteCurrency,
            @JsonProperty("type") String type,
            @JsonProperty("side") FtxOrderSide side,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("size") BigDecimal size,
            @JsonProperty("orderId") String orderId,
            @JsonProperty("time") Date time,
            @JsonProperty("tradeId") String tradeId,
            @JsonProperty("feeRate") BigDecimal feeRate,
            @JsonProperty("fee") BigDecimal fee,
            @JsonProperty("feeCurrency") String feeCurrency,
            @JsonProperty("liquidity") FtxUserTradeLiquidityType liquidity
    ) {
        this.id = id;
        this.market = market;
        this.future = future;
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
        this.type = type;
        this.side = side;
        this.price = price;
        this.size = size;
        this.orderId = orderId;
        this.time = time;
        this.tradeId = tradeId;
        this.feeRate = feeRate;
        this.fee = fee;
        this.feeCurrency = feeCurrency;
        this.liquidity = liquidity;
    }

    public String getId() {
        return id;
    }

    public String getMarket() {
        return market;
    }

    public String getFuture() {
        return future;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public String getType() {
        return type;
    }

    public FtxOrderSide getSide() {
        return side;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getSize() {
        return size;
    }

    public String getOrderId() {
        return orderId;
    }

    public Date getTime() {
        return time;
    }

    public String getTradeId() {
        return tradeId;
    }

    public BigDecimal getFeeRate() {
        return feeRate;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public String getFeeCurrency() {
        return feeCurrency;
    }

    public FtxUserTradeLiquidityType getLiquidity() {
        return liquidity;
    }

    @Override
    public String toString() {
        return "FtxUserTradeDto{" +
                "id='" + id + '\'' +
                ", market='" + market + '\'' +
                ", future='" + future + '\'' +
                ", baseCurrency='" + baseCurrency + '\'' +
                ", quoteCurrency='" + quoteCurrency + '\'' +
                ", type='" + type + '\'' +
                ", side=" + side +
                ", price=" + price +
                ", size=" + size +
                ", orderId='" + orderId + '\'' +
                ", time=" + time +
                ", tradeId='" + tradeId + '\'' +
                ", feeRate=" + feeRate +
                ", fee=" + fee +
                ", feeCurrency='" + feeCurrency + '\'' +
                ", liquidity=" + liquidity +
                '}';
    }
}

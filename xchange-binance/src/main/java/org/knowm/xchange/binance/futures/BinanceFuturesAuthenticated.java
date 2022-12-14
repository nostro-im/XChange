package org.knowm.xchange.binance.futures;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.knowm.xchange.binance.BinanceAuthenticated;
import org.knowm.xchange.binance.dto.BinanceException;
import org.knowm.xchange.binance.dto.account.BinanceMarginPositionSide;
import org.knowm.xchange.binance.dto.account.BinanceMarginType;
import org.knowm.xchange.binance.dto.marketdata.BinanceAggTrades;
import org.knowm.xchange.binance.dto.marketdata.BinanceOrderbook;
import org.knowm.xchange.binance.dto.marketdata.BinancePrice;
import org.knowm.xchange.binance.dto.marketdata.BinancePriceQuantity;
import org.knowm.xchange.binance.dto.marketdata.BinanceTicker24h;
import org.knowm.xchange.binance.dto.meta.BinanceTime;
import org.knowm.xchange.binance.dto.meta.exchangeinfo.BinanceExchangeInfo;
import org.knowm.xchange.binance.dto.trade.BinanceCancelledOrder;
import org.knowm.xchange.binance.dto.trade.BinanceListenKey;
import org.knowm.xchange.binance.dto.trade.OrderSide;
import org.knowm.xchange.binance.dto.trade.TimeInForce;
import org.knowm.xchange.binance.futures.dto.account.BinanceFuturesAccountInformation;
import org.knowm.xchange.binance.futures.dto.account.BinanceFuturesIncomeHistoryRecord;
import org.knowm.xchange.binance.futures.dto.account.BinanceFuturesInitialLeverage;
import org.knowm.xchange.binance.futures.dto.account.BinanceFuturesLeverageBrackets;
import org.knowm.xchange.binance.futures.dto.account.BinanceFuturesPositionInformation;
import org.knowm.xchange.binance.futures.dto.account.BinanceUserCommissionRate;
import org.knowm.xchange.binance.futures.dto.meta.BinanceFuturesExchangeInfo;
import org.knowm.xchange.binance.futures.dto.trade.BinanceFuturesOrder;
import org.knowm.xchange.binance.futures.dto.trade.BinanceFuturesOrderType;
import org.knowm.xchange.binance.futures.dto.trade.BinanceFuturesTrade;
import org.knowm.xchange.binance.futures.dto.trade.NewOrderRespType;
import org.knowm.xchange.binance.futures.dto.trade.PositionSide;
import org.knowm.xchange.binance.futures.dto.trade.WorkingType;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;

import si.mazi.rescu.ParamsDigest;
import si.mazi.rescu.SynchronizedValueFactory;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public interface BinanceFuturesAuthenticated extends BinanceAuthenticated {

	// -------------------- Override Binance interface methods with Futures Url -------------------

	/**
	  * Test connectivity to the Rest API.
	  *
	  * @return
	  * @throws IOException
	  */
	@Override
	@GET
	@Path("fapi/v1/ping")
	Object ping() throws IOException;

	/**
	 * Test connectivity to the Rest API and get the current server time.
	 *
	 * @return
	 * @throws IOException
	 */
	@Override
	@GET
	@Path("fapi/v1/time")
	BinanceTime time() throws IOException;

	/**
	 * Current exchange trading rules and symbol information.
	 *
	 * @return
	 * @throws IOException
	 */
	default BinanceExchangeInfo exchangeInfo() throws IOException {
		throw new NotAvailableFromExchangeException();
	}

	/**
	 * @param symbol
	 * @param limit optional, default 100 max 5000. Valid limits: [5, 10, 20, 50, 100, 500, 1000,
	 *     5000]
	 * @return
	 * @throws IOException
	 * @throws BinanceException
	 */
	@Override
	@GET
	@Path("fapi/v1/depth")
	BinanceOrderbook depth(@QueryParam("symbol") String symbol, @QueryParam("limit") Integer limit)
			throws IOException, BinanceException;
	
	/**
	 * Get compressed, aggregate trades. Trades that fill at the time, from the same order, with the
	 * same price will have the quantity aggregated.<br>
	 * If both startTime and endTime are sent, limit should not be sent AND the distance between
	 * startTime and endTime must be less than 24 hours.<br>
	 * If frondId, startTime, and endTime are not sent, the most recent aggregate trades will be
	 * returned.
	 *
	 * @param symbol
	 * @param fromId optional, ID to get aggregate trades from INCLUSIVE.
	 * @param startTime optional, Timestamp in ms to get aggregate trades from INCLUSIVE.
	 * @param endTime optional, Timestamp in ms to get aggregate trades until INCLUSIVE.
	 * @param limit optional, Default 500; max 500.
	 * @return
	 * @throws IOException
	 * @throws BinanceException
	 */
	@Override
	@GET
	@Path("fapi/v1/aggTrades")
	List<BinanceAggTrades> aggTrades(
	    @QueryParam("symbol") String symbol,
	    @QueryParam("fromId") Long fromId,
	    @QueryParam("startTime") Long startTime,
	    @QueryParam("endTime") Long endTime,
	    @QueryParam("limit") Integer limit)
	    throws IOException, BinanceException;

	/**
	 * Kline/candlestick bars for a symbol. Klines are uniquely identified by their open time.<br>
	 * If startTime and endTime are not sent, the most recent klines are returned.
	 *
	 * @param symbol
	 * @param interval
	 * @param limit optional, default 500; max 500.
	 * @param startTime optional
	 * @param endTime optional
	 * @return
	 * @throws IOException
	 * @throws BinanceException
	 */
	@Override
	@GET
	@Path("fapi/v1/klines")
	List<Object[]> klines(
	    @QueryParam("symbol") String symbol,
	    @QueryParam("interval") String interval,
	    @QueryParam("limit") Integer limit,
	    @QueryParam("startTime") Long startTime,
	    @QueryParam("endTime") Long endTime)
	    throws IOException, BinanceException;

	/**
	 * 24 hour price change statistics for all symbols. - bee careful this api call have a big
	 * weight, only about 4 call per minute can be without ban.
	 *
	 * @return
	 * @throws IOException
	 * @throws BinanceException
	 */
	@Override
	@GET
	@Path("fapi/v1/ticker/24hr")
	List<BinanceTicker24h> ticker24h() throws IOException, BinanceException;
	
	/**
	 * 24 hour price change statistics.
	 *
	 * @param symbol
	 * @return
	 * @throws IOException
	 * @throws BinanceException
	 */
	@Override
	@GET
	@Path("fapi/v1/ticker/24hr")
	BinanceTicker24h ticker24h(@QueryParam("symbol") String symbol)
			throws IOException, BinanceException;

	/**
	 * Latest price for a symbol.
	 *
	 * @return
	 * @throws IOException
	 * @throws BinanceException
	 */
	@Override
	@GET
	@Path("fapi/v1/ticker/price")
	BinancePrice tickerPrice(@QueryParam("symbol") String symbol)
	    throws IOException, BinanceException;

	/**
	 * Latest price for all symbols.
	 *
	 * @return
	 * @throws IOException
	 * @throws BinanceException
	 */
	@Override
	@GET
	@Path("fapi/v1/ticker/price")
	List<BinancePrice> tickerAllPrices() throws IOException, BinanceException;

	/**
	 * Best price/qty on the order book for all symbols.
	 *
	 * @return
	 * @throws IOException
	 * @throws BinanceException
	 */
	@Override
	@GET
	@Path("fapi/v1/ticker/bookTicker")
	List<BinancePriceQuantity> tickerAllBookTickers() throws IOException, BinanceException;
	
	
	
	// -------------------- Extend BinanceAuthenticated API ---------------------------------------
	// Override mutual methods and update to Futures Url
	
    /**
     * Get current account information.
     *
     * @param recvWindow optional
     * @param timestamp
     * @return
     * @throws IOException
     * @throws BinanceException
     */
	@GET
    @Path("fapi/v2/account")
	BinanceFuturesAccountInformation futuresAccount(
            @QueryParam("recvWindow") Long recvWindow,
            @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
            @HeaderParam(X_MBX_APIKEY) String apiKey,
            @QueryParam(SIGNATURE) ParamsDigest signature)
            throws IOException, BinanceException;

    /**
     * @param symbol
     * @param recvWindow optional
     * @param timestamp
     * @param apiKey
     * @param signature
     * @return
     * @throws IOException
     * @throws BinanceException
     */
	@GET
    @Path("fapi/v1/commissionRate")
    BinanceUserCommissionRate userCommissionRate(
            @QueryParam("symbol") String symbol,
            @QueryParam("recvWindow") Long recvWindow,
            @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
            @HeaderParam(X_MBX_APIKEY) String apiKey,
            @QueryParam(SIGNATURE) ParamsDigest signature)
            throws IOException, BinanceException;

    /**
     * Get open orders on a symbol.
     *
     * @param symbol optional
     * @param recvWindow optional
     * @param timestamp
     * @return
     * @throws IOException
     * @throws BinanceException
     */
	@GET
    @Path("fapi/v1/openOrders")
    List<BinanceFuturesOrder> futuresOpenOrders(
            @QueryParam("symbol") String symbol,
            @QueryParam("recvWindow") Long recvWindow,
            @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
            @HeaderParam(X_MBX_APIKEY) String apiKey,
            @QueryParam(SIGNATURE) ParamsDigest signature)
            throws IOException, BinanceException;

    /**
     * Get all account orders; active, canceled, or filled.
     * These orders will not be found:
     * order status is CANCELED or EXPIRED, AND
     * order has NO filled trade, AND
     * created time + 7 days < current time
     *
     * @param symbol required
     * @param orderId optional, If is set, it will get orders >= that orderId. Otherwise most recent orders are returned.
     * @param startTime optional
     * @param endTime optional
     * @param limit optional, default 500; max 1000.
     * @param recvWindow optional
     * @param timestamp required
     *
     * @return
     * @throws IOException
     * @throws BinanceException
     */
	@GET
    @Path("fapi/v1/allOrders")
    List<BinanceFuturesOrder> futuresAllOrders(
            @QueryParam("symbol") String symbol,
            @QueryParam("orderId") Long orderId,
            @QueryParam("startTime") Long startTime,
            @QueryParam("endTime") Long endTime,
            @QueryParam("limit") Integer limit,
            @QueryParam("recvWindow") Long recvWindow,
            @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
            @HeaderParam(X_MBX_APIKEY) String apiKey,
            @QueryParam(SIGNATURE) ParamsDigest signature)
            throws IOException, BinanceException;

    /**
     * Cancel an active order.
     *
     * @param symbol
     * @param orderId optional
     * @param origClientOrderId optional
     * @param newClientOrderId optional, used to uniquely identify this cancel. Automatically
     *     generated by default.
     * @param recvWindow optional
     * @param timestamp
     * @param apiKey
     * @param signature
     * @return
     * @throws IOException
     * @throws BinanceException
     */
	@Override
    @DELETE
    @Path("fapi/v1/order")
    BinanceCancelledOrder cancelOrder(
            @QueryParam("symbol") String symbol,
            @QueryParam("orderId") Long orderId,
            @QueryParam("origClientOrderId") String origClientOrderId,
            @QueryParam("newClientOrderId") String newClientOrderId,
            @QueryParam("recvWindow") Long recvWindow,
            @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
            @HeaderParam(X_MBX_APIKEY) String apiKey,
            @QueryParam(SIGNATURE) ParamsDigest signature)
            throws IOException, BinanceException;

    /**
     * @param symbol
     * @param side
     * @param positionSide optional. Default BOTH for One-way Mode ; LONG or SHORT for Hedge Mode. It must be sent in Hedge Mode.
     * @param type
     * @param timeInForce optional.
     * @param quantity optional. Cannot be sent with closePosition=true(Close-All)
     * @param reduceOnly optional. "true" or "false". default "false". Cannot be sent in Hedge Mode; cannot be sent with closePosition=true
     * @param price optional.
     * @param newClientOrderId optional.
     * @param stopPrice optional. Used with STOP/STOP_MARKET or TAKE_PROFIT/TAKE_PROFIT_MARKET orders.
     * @param closePosition optional. true, false；Close-All，used with STOP_MARKET or TAKE_PROFIT_MARKET.
     * @param activationPrice optional. Used with TRAILING_STOP_MARKET orders, default as the latest price(supporting different workingType)
     * @param callbackRate optional. Used with TRAILING_STOP_MARKET orders, min 0.1, max 5 where 1 for 1%
     * @param workingType optional. stopPrice triggered by: "MARK_PRICE", "CONTRACT_PRICE". Default "CONTRACT_PRICE"
     * @param priceProtect optional. "TRUE" or "FALSE", default "FALSE". Used with STOP/STOP_MARKET or TAKE_PROFIT/TAKE_PROFIT_MARKET orders.
     * @param newOrderRespType optional. "ACK", "RESULT", default "ACK"
     * @param recvWindow optional.
     * @param timestamp
     * @param apiKey
     * @param signature
     * @return
     * @throws IOException
     * @throws BinanceException
     */
	@POST
    @Path("fapi/v1/order")
    BinanceFuturesOrder newFuturesOrder(
            @FormParam("symbol") String symbol,
            @FormParam("side") OrderSide side,
            @FormParam("positionSide") PositionSide positionSide,
            @FormParam("type") BinanceFuturesOrderType type,
            @FormParam("timeInForce") TimeInForce timeInForce,
            @FormParam("quantity") BigDecimal quantity,
            @FormParam("reduceOnly") Boolean reduceOnly,
            @FormParam("price") BigDecimal price,
            @FormParam("newClientOrderId") String newClientOrderId,
            @FormParam("stopPrice") BigDecimal stopPrice,
            @FormParam("closePosition") Boolean closePosition,
            @FormParam("activationPrice") BigDecimal activationPrice,
            @FormParam("callbackRate") BigDecimal callbackRate,
            @FormParam("workingType") WorkingType workingType,
            @FormParam("priceProtect") Boolean priceProtect,
            @FormParam("newOrderRespType") NewOrderRespType newOrderRespType,
            @FormParam("recvWindow") Long recvWindow,
            @FormParam("timestamp") SynchronizedValueFactory<Long> timestamp,
            @HeaderParam(X_MBX_APIKEY) String apiKey,
            @QueryParam(SIGNATURE) ParamsDigest signature)
            throws IOException, BinanceException;

    /**
     * Check an order's status.<br>
     * Either orderId or origClientOrderId must be sent.
     *
     * @param symbol
     * @param orderId optional
     * @param origClientOrderId optional
     * @param recvWindow optional
     * @param timestamp
     * @param apiKey
     * @param signature
     * @return
     * @throws IOException
     * @throws BinanceException
     */
	@GET
    @Path("fapi/v1/order")
    BinanceFuturesOrder futuresOrderStatus(
            @QueryParam("symbol") String symbol,
            @QueryParam("orderId") Long orderId,
            @QueryParam("origClientOrderId") String origClientOrderId,
            @QueryParam("recvWindow") Long recvWindow,
            @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
            @HeaderParam(X_MBX_APIKEY) String apiKey,
            @QueryParam(SIGNATURE) ParamsDigest signature)
            throws IOException, BinanceException;

    /**
     * Get trades for a specific account and symbol.
     *
     * @param symbol
     * @param startTime optional
     * @param endTime optional
     * @param limit optional, default 500; max 1000.
     * @param fromId optional, tradeId to fetch from. Default gets most recent trades.
     * @param recvWindow optional
     * @param timestamp
     * @param apiKey
     * @param signature
     * @return
     * @throws IOException
     * @throws BinanceException
     */
	@GET
    @Path("fapi/v1/userTrades")
    List<BinanceFuturesTrade> myFuturesTrades(
            @QueryParam("symbol") String symbol,
            @QueryParam("limit") Integer limit,
            @QueryParam("startTime") Long startTime,
            @QueryParam("endTime") Long endTime,
            @QueryParam("fromId") Long fromId,
            @QueryParam("recvWindow") Long recvWindow,
            @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
            @HeaderParam(X_MBX_APIKEY) String apiKey,
            @QueryParam(SIGNATURE) ParamsDigest signature)
            throws IOException, BinanceException;

    /**
     * Current exchange trading rules and symbol information.
     *
     * @return
     * @throws IOException
     */
	@GET
    @Path("fapi/v1/exchangeInfo")
    BinanceFuturesExchangeInfo futuresExchangeInfo() throws IOException;

    /**
     * Returns a listen key for websocket login.
     *
     * @param apiKey the api key
     * @return
     * @throws BinanceException
     * @throws IOException
     */
    @POST
    @Path("/fapi/v1/listenKey")
    BinanceListenKey startUserDataStream(@HeaderParam(X_MBX_APIKEY) String apiKey)
            throws IOException, BinanceException;

    /**
     * Keeps the authenticated websocket session alive.
     *
     * @param apiKey the api key
     * @param listenKey the api secret
     * @return
     * @throws BinanceException
     * @throws IOException
     */
    @PUT
    @Path("/fapi/v1/listenKey?listenKey={listenKey}")
    Map<?, ?> keepAliveUserDataStream(
            @HeaderParam(X_MBX_APIKEY) String apiKey, @PathParam("listenKey") String listenKey)
            throws IOException, BinanceException;

    /**
     * Closes the websocket authenticated connection.
     *
     * @param apiKey the api key
     * @param listenKey the api secret
     * @return
     * @throws BinanceException
     * @throws IOException
     */
    @DELETE
    @Path("/fapi/v1/listenKey?listenKey={listenKey}")
    Map<?, ?> closeUserDataStream(
            @HeaderParam(X_MBX_APIKEY) String apiKey, @PathParam("listenKey") String listenKey)
            throws IOException, BinanceException;

    /**
     * Change user's initial leverage of specific symbol market.
     *
     * @param symbol symbol
     * @param leverage leverage setting
     * @throws IOException
     * @throws BinanceException
     */
    @POST
    @Path("/fapi/v1/leverage")
    BinanceFuturesInitialLeverage changeInitialLeverage(
            @FormParam("symbol") String symbol,
            @FormParam("leverage") Integer leverage,
            @FormParam("recvWindow") Long recvWindow,
            @FormParam("timestamp") SynchronizedValueFactory<Long> timestamp,
            @HeaderParam(X_MBX_APIKEY) String apiKey,
            @QueryParam(SIGNATURE) ParamsDigest signature)
            throws IOException, BinanceException;

    /**
     * Change user's initial leverage of specific symbol market.
     *
     * @param symbol symbol
     * @param marginType marginType
     * @throws IOException
     * @throws BinanceException
     */
    @POST
    @Path("/fapi/v1/marginType")
    Map<?, ?> changeMarginType(
            @FormParam("symbol") String symbol,
            @FormParam("marginType") BinanceMarginType marginType,
            @FormParam("recvWindow") Long recvWindow,
            @FormParam("timestamp") SynchronizedValueFactory<Long> timestamp,
            @HeaderParam(X_MBX_APIKEY) String apiKey,
            @QueryParam(SIGNATURE) ParamsDigest signature)
            throws IOException, BinanceException;

    /**
     * Modify isolated position margin
     * Note: Only for isolated symbol
     * @param symbol symbol
     * @param positionSide both/long/short position side
     * @param amount optional
     * @param type add/reduce position margin
     * @throws IOException
     * @throws BinanceException
     */
    @POST
    @Path("/fapi/v1/positionMargin")
    Map<?, ?> modifyIsolatedPositionMargin(
            @FormParam("symbol") String symbol,
            @FormParam("positionSide") BinanceMarginPositionSide positionSide,
            @FormParam("amount") BigDecimal amount,
            @FormParam("type") int type,
            @FormParam("recvWindow") Long recvWindow,
            @FormParam("timestamp") SynchronizedValueFactory<Long> timestamp,
            @HeaderParam(X_MBX_APIKEY) String apiKey,
            @QueryParam(SIGNATURE) ParamsDigest signature)
            throws IOException, BinanceException;

    /**
     * Get Income History
     *
     * If neither startTime nor endTime is sent, the recent 7-day data will be returned.
     * If incomeType is not sent, all kinds of flow will be returned
     * "trandId" is unique in the same incomeType for a user
     *
     * @param symbol optional
     * @param incomeType optional
     * @param startTime optional
     * @param endTime optional
     * @param limit optional, default 100; max 1000.
     * @return
     * @throws IOException
     * @throws BinanceException
     */
    @GET
    @Path("/fapi/v1/income")
    List<BinanceFuturesIncomeHistoryRecord> getIncomeHistory(
            @QueryParam("symbol") String symbol,
            @QueryParam("incomeType") String incomeType,
            @QueryParam("startTime") Long startTime,
            @QueryParam("endTime") Long endTime,
            @QueryParam("limit") Integer limit,
            @QueryParam("recvWindow") Long recvWindow,
            @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
            @HeaderParam(X_MBX_APIKEY) String apiKey,
            @QueryParam(SIGNATURE) ParamsDigest signature)
            throws IOException, BinanceException;

    /**
     * Get leverage brackets
     *
     * @param symbol optional
     *
     * @throws IOException
     * @throws BinanceException
     */
    @GET
    @Path("/fapi/v1/leverageBracket")
    List<BinanceFuturesLeverageBrackets> getLeverageBrackets(
            @QueryParam("symbol") String symbol,
            @QueryParam("recvWindow") Long recvWindow,
            @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
            @HeaderParam(X_MBX_APIKEY) String apiKey,
            @QueryParam(SIGNATURE) ParamsDigest signature)
            throws IOException, BinanceException;

    /**
     * Get current position information.
     *
     * @param symbol optional
     *
     * @throws IOException
     * @throws BinanceException
     */
    @GET
    @Path("/fapi/v1/positionRisk")
    List<BinanceFuturesPositionInformation> getPositionInformation(
            @QueryParam("symbol") String symbol,
            @QueryParam("recvWindow") Long recvWindow,
            @QueryParam("timestamp") SynchronizedValueFactory<Long> timestamp,
            @HeaderParam(X_MBX_APIKEY) String apiKey,
            @QueryParam(SIGNATURE) ParamsDigest signature)
            throws IOException, BinanceException;
}

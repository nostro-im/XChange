package org.knowm.xchange.ftx;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.knowm.xchange.ftx.dto.FtxResponse;
import org.knowm.xchange.ftx.dto.account.*;
import org.knowm.xchange.ftx.dto.account.FtxBorrowingHistoryDto;
import org.knowm.xchange.ftx.dto.trade.*;
import si.mazi.rescu.ParamsDigest;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
public interface FtxAuthenticated extends Ftx {

  @GET
  @Path("/account")
  FtxResponse<FtxAccountDto> getAccountInformation(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount)
      throws IOException, FtxException;

  @GET
  @Path("/wallet/balances")
  FtxResponse<List<FtxWalletBalanceDto>> getWalletBalances(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount)
      throws IOException, FtxException;

  @GET
  @Path("/positions")
  FtxResponse<List<FtxPositionDto>> getFtxPositions(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      @QueryParam("showAvgPrice") boolean showAvgPrice)
      throws IOException, FtxException;

  @DELETE
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/subaccounts")
  FtxResponse deleteSubAccounts(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      FtxSubAccountRequestPOJO payload)
      throws IOException, FtxException;

  @GET
  @Path("/subaccounts")
  FtxResponse<List<FtxSubAccountDto>> getAllSubAccounts(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature)
      throws IOException, FtxException;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/subaccounts")
  FtxResponse<FtxSubAccountDto> createSubAccount(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      FtxSubAccountRequestPOJO payload)
      throws IOException, FtxException;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/subaccounts/update_name")
  FtxResponse changeSubAccountName(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      FtxChangeSubAccountNamePOJO payload)
      throws IOException, FtxException;

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/subaccounts/{nickname}/balances")
  FtxResponse<FtxSubAccountBalanceDto> getSubAccountBalances(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      @PathParam("nickname") String nickname)
      throws IOException, FtxException;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/subaccounts/transfer")
  FtxResponse<FtxSubAccountTranferDto> transferBetweenSubAccounts(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      FtxSubAccountTransferPOJO payload)
      throws IOException, FtxException;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/orders")
  FtxResponse<FtxOrderDto> placeOrder(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      FtxOrderRequestPayload payload)
      throws IOException, FtxException;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/orders/{order_id}/modify")
  FtxResponse<FtxOrderDto> modifyOrder(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      @PathParam("order_id") String orderId,
      FtxModifyOrderRequestPayload payload)
      throws IOException, FtxException;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/orders/by_client_id/{client_order_id}/modify")
  FtxResponse<FtxOrderDto> modifyOrderByClientId(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      @PathParam("client_order_id") String clientId,
      FtxModifyOrderRequestPayload payload)
      throws IOException, FtxException;

  @GET
  @Path("/orders/{order_id}")
  FtxResponse<FtxOrderDto> getOrderStatus(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      @PathParam("order_id") String orderId)
      throws IOException, FtxException;

  @GET
  @Path("/orders/by_client_id/{client_order_id}")
  FtxResponse<FtxOrderDto> getOrderStatusByClientId(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      @PathParam("client_order_id") String clientOrderId)
      throws IOException, FtxException;
  
  @GET
  @Path("/orders")
  FtxResponse<List<FtxOrderDto>> openOrders(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      @QueryParam("market") String market)
      throws IOException, FtxException;

  @GET
  @Path("/orders")
  FtxResponse<List<FtxOrderDto>> openOrdersWithoutMarket(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount)
      throws IOException, FtxException;

  @DELETE
  @Path("/orders/{orderId}")
  FtxResponse<String> cancelOrder(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      @PathParam("orderId") String orderId)
      throws IOException, FtxException;

  @DELETE
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/orders")
  FtxResponse<String> cancelAllOrders(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      CancelAllFtxOrdersParams payLoad)
      throws IOException, FtxException;

  @GET
  @Path("/orders/history")
  FtxResponse<List<FtxOrderDto>> orderHistory(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      @QueryParam("market") String market,
      @QueryParam("start_time") Integer startTime,
      @QueryParam("end_time") Integer endTime)
      throws IOException, FtxException;

  @GET
  @Path("/fills")
  FtxResponse<List<FtxUserTradeDto>> fills(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      @QueryParam("market") String market,
      @QueryParam("start_time") Integer startTime,
      @QueryParam("end_time") Integer endTime,
      @QueryParam("order") String order,
      @QueryParam("orderId") String orderId)
      throws IOException, FtxException;

  @DELETE
  @Path("/orders/by_client_id/{client_order_id}")
  FtxResponse<String> cancelOrderByClientId(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      @PathParam("client_order_id") String clientOrderId)
      throws IOException, FtxException;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/account/leverage")
  FtxResponse<FtxLeverageDto> changeLeverage(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      FtxLeverageDto leverage)
      throws IOException, FtxException;

  @GET
  @Path("/spot_margin/borrow_history")
  FtxResponse<List<FtxBorrowingHistoryDto>> getBorrowHistory(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      @QueryParam("start_time") Long start_time,
      @QueryParam("end_time") Long end_time)
      throws IOException, FtxException;

  @GET
  @Path("/funding_payments")
  FtxResponse<List<FtxFundingPaymentsDto>> getFundingPayments(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      @QueryParam("start_time") Integer startTime,
      @QueryParam("end_time") Integer endTime,
      @QueryParam("future") String future)
      throws IOException, FtxException;

  @GET
  @Path("/spot_margin/lending_info")
  FtxResponse<List<FtxLendingInfoDto>> getLendingInfos(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount)
      throws IOException, FtxException;

  @GET
  @Path("/spot_margin/lending_rates")
  FtxResponse<List<FtxLendingRatesDto>> getLendingRates(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature)
      throws IOException, FtxException;

  @GET
  @Path("/spot_margin/lending_history")
  FtxResponse<List<FtxLendingHistoryDto>> getlendingHistories(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount)
      throws IOException, FtxException;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/spot_margin/offers")
  FtxResponse submitLendingOffer(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      FtxSubmitLendingOfferParams payload)
      throws IOException, FtxException;

  @GET
  @Path("/spot_margin/borrow_rates")
  FtxResponse<List<FtxBorrowingRatesDto>> getBorrowRates(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature)
      throws IOException, FtxException;

  @GET
  @Path("/spot_margin/borrow_info")
  FtxResponse<List<FtxBorrowingInfoDto>> getBorrowingInfos(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount)
      throws IOException, FtxException;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/conditional_orders")
  FtxResponse<FtxConditionalOrderDto> placeConditionalOrder(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      FtxConditionalOrderRequestPayload payload)
      throws IOException, FtxException;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/conditional_orders/{order_id}/modify")
  FtxResponse<FtxConditionalOrderDto> modifyConditionalOrder(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      @PathParam("order_id") String orderId,
      FtxModifyConditionalOrderRequestPayload payload)
      throws IOException, FtxException;

  @DELETE
  @Path("/conditional_orders/{orderId}")
  FtxResponse<String> cancelConditionalOrder(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      @PathParam("orderId") String orderId)
      throws IOException, FtxException;

  @GET
  @Path("/conditional_orders/history")
  FtxResponse<List<FtxConditionalOrderDto>> conditionalOrderHistory(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      @QueryParam("market") String market,
      @QueryParam("start_time") Integer startTime,
      @QueryParam("end_time") Integer endTime)
      throws IOException, FtxException;

  @GET
  @Path("/conditional_orders")
  FtxResponse<List<FtxConditionalOrderDto>> openConditionalOrders(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      @QueryParam("market") String market)
      throws IOException, FtxException;

  @GET
  @Path("/conditional_orders")
  FtxResponse<List<FtxConditionalOrderDto>> openConditionalOrdersWithoutMMarket(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount)
      throws IOException, FtxException;
  
  @GET
  @Path("/conditional_orders/{id}/triggers")
  FtxResponse<List<FtxTriggerDto>> getTriggers(
      @HeaderParam("FTX-KEY") String apiKey,
      @HeaderParam("FTX-TS") Long nonce,
      @HeaderParam("FTX-SIGN") ParamsDigest signature,
      @HeaderParam("FTX-SUBACCOUNT") String subaccount,
      @PathParam("id") String id)
      throws IOException, FtxException;
}

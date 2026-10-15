package org.yht.exps.express.service.invoice;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.invoice.PayMapper;
import org.yht.exps.express.model.invoice.search.Easypay;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.service.CommonService;
import org.yht.util.DateTimeUtil;
import org.yht.util.EncryptionUtils;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

@Slf4j
@Service
//@Transactional
public class PayService {

	@Autowired
	PayMapper payMapper;
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	@Autowired
	CustomProperties customProperties;
	
	@Resource(name = "commonService")
	CommonService commonService;
	
	/**
	 * 정산서 이지페이 거래등록
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public Easypay easypayRegistration(Easypay easypay, HttpServletRequest request) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String shopOrderNo = null;

		/** 비로그인 화면에서는 암호화 처리 */
		if(easypay.getDeviceTypeCode().indexOf("login_") > -1){
			shopOrderNo = easypay.getShopOrderNo();
			easypay.setDeviceTypeCode(easypay.getDeviceTypeCode().replace("login_", ""));
		}else{
			shopOrderNo = EncryptionUtils.decryptAES(easypay.getEncryptShopOrderNo(), Util.null2str(easypay.getEntCode()));
		}
		map.put("entCode", easypay.getEntCode());
		map.put("invoiceNo", shopOrderNo);
		if(shopOrderNo.indexOf("G") > -1){
			map.put("invoiceNoField", "INVOICE_NO_GROUP");
		}else{
			map.put("invoiceNoField", "INVOICE_JOB_NO");
		}
		
		Util.accessControlDatabase(map, "invoiceNoField", false, null, "", null, "");
		Map<String, Object> invoiceMap = payMapper.selectInvoicePaymentData(map);
		if(invoiceMap == null || "S".equals(invoiceMap.get("PAYMENT_STATUS")) || "R".equals(invoiceMap.get("PAYMENT_STATUS"))){
			easypay.setResCd("A11347");	/** 결제 할 수 없는 상태입니다. */
			return easypay;
		}
		
		String url = "";
		
		JSONParser parser = new JSONParser();
		JSONObject data = new JSONObject();
		JSONObject orderInfo = new JSONObject();
		JSONObject customerInfo = new JSONObject();
		JSONObject shopValueInfo = new JSONObject();
		data.put("shopOrderNo", shopOrderNo);
		data.put("mallId", invoiceMap.get("MALL_ID"));
		data.put("payMethodTypeCode", easypay.getPayMethodTypeCode());
		data.put("amount", invoiceMap.get("AMOUNT_TOTAL_LOC"));
		data.put("currency", "00");
		data.put("clientTypeCode", "00");
		//테스트
//		data.put("returnUrl", "http://10.3.1.16:8090/" + easypay.getEntCode() + "/payAuthorization/easypay");
		data.put("returnUrl", customProperties.getEsaypayProtocol() + customProperties.getInterfaceUrl() + "/" + easypay.getEntCode() + "/payAuthorization/easypay");
		data.put("deviceTypeCode", Util.null2str(easypay.getDeviceTypeCode(), "mobile"));
		
		String goodsName = Util.null2str(invoiceMap.get("GOODS_NAME")).replaceAll("\\&|\\||\\,|\\;|\\\\n|\\\\r|\\\\|\\'|\\\"", " ");
		if(goodsName.length() > 50){
			goodsName = goodsName.substring(0, 50);
		}
		customerInfo.put("customerId", invoiceMap.get("ACCOUNT_CODE"));
		
		orderInfo.put("goodsName", goodsName);
		orderInfo.put("customerInfo", customerInfo);
		data.put("orderInfo", orderInfo);
		
		shopValueInfo.put("value1", easypay.getPayMethodTypeCode());
		shopValueInfo.put("value2", easypay.getUserId());
		data.put("shopValueInfo", shopValueInfo);
		
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(customProperties.getEsaypayUrl() + "/api/trades/webpay");
		httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
		httpPost.setEntity(new StringEntity(data.toString(), "UTF-8"));
		HttpResponse httpResponse = client.execute(httpPost);
   	 
		if (httpResponse.getStatusLine().getStatusCode() == 200) {
			ResponseHandler<String> handler = new BasicResponseHandler();
			String body = handler.handleResponse(httpResponse);
			
			data = (JSONObject) parser.parse(body);
			if(Util.isEmpty(data.get("authPageUrl"))){
				easypay.setResMsg(Util.null2str(data.get("resMsg")));
			}else{
				url = Util.null2str(data.get("authPageUrl"));
			}
		} else {
			url = "response is error : " + httpResponse.getStatusLine().getStatusCode();
		}
		easypay.setReturnUrl(url);
		
		return easypay;
	}
	
	/**
	 * 정산서 이지페이 승인요청
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public String easypayApproval(String entCode, HttpServletRequest request) throws Exception {
		Easypay easypay = new Easypay();
		String status = null;
		String shopTransactionId = null;
		String shopOrderNo = request.getParameter("shopOrderNo");
		String invGroupYn = null;
		String errorStr = null;
		String remarks = "";
		
		String encryptShopOrderNo = EncryptionUtils.encryptAES(shopOrderNo, entCode);
		
		//테스트
//		String resultUrl = "redirect:http://10.3.1.16:8080/exps/easypayResult.do?shopOrderNo=" + encryptShopOrderNo;
		String resultUrl = "redirect:http://" + customProperties.getPreUrl() + "/exps/easypayResult.do?shopOrderNo=" + encryptShopOrderNo;
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> stockMap = new HashMap<String, Object>();
		Map<String, Object> invoiceMap = null;
		JSONObject result = new JSONObject();
		try{
			if(!"0000".equals(request.getParameter("resCd"))){	/** 인증요청 오류 */
				errorStr = URLEncoder.encode(request.getParameter("resMsg"), "UTF-8");
			}
			
			if(errorStr == null){
				map.put("entCode", entCode);
				if(shopOrderNo.indexOf("G") > -1){
					map.put("invoiceNoField", "INVOICE_NO_GROUP");
					invGroupYn = "Y";
				}else{
					map.put("invoiceNoField", "INVOICE_JOB_NO");
					invGroupYn = "N";
				}
				map.put("invoiceNo", shopOrderNo);
				Util.accessControlDatabase(map, "invoiceNoField", false, null, "", null, "");
				invoiceMap = payMapper.selectInvoicePaymentData(map);
				if(invoiceMap != null){
					if("S".equals(invoiceMap.get("PAYMENT_STATUS")) || ("R".equals(invoiceMap.get("PAYMENT_STATUS")) && Util.null2int(invoiceMap.get("RE_REQ_CHECK")) < 1)){
						errorStr = "notPossible";
					}
					resultUrl += "&entCode=" + Util.null2str(invoiceMap.get("ENT_MAPPING_CODE"));
				}else{
					errorStr = "nullData";
				}
			}
			
			if(errorStr == null){
				stockMap.put("seqNo", "1");
				stockMap.put("gmtTime", "0");
				stockMap.put("entCode", entCode);
				stockMap.put("entBusinessCode", "");
				if(!Util.null2str(request.getParameter("shopValue2")).equals("")){
					stockMap.put("updateUserId", request.getParameter("shopValue2"));
				}else{
					stockMap.put("updateUserId", invoiceMap.get("ACCOUNT_CODE"));
				}
				stockMap.put("solution", "ECM");
				stockMap.put("stockType", "PAY");
				stockMap.put("stockSign", "PAY");
				stockMap.put("stockSeqLength", 8);
				stockMap.put("dateLength", null);
				stockMap.put("signType", null);
				stockMap.put("stockDateFormat", null);
				stockMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
				stockMap.put("stockRemark", "PAYMENT-JOB NO");
				shopTransactionId = commonMapper.selectNextStockMaxNo(stockMap);
				
				stockMap.put("stockType", "EASY");
				stockMap.put("stockSign", "EASY");
				stockMap.put("stockRemark", "EASY-PAYMENT-JOB NO");
				map.put("payJobNo", commonMapper.selectNextStockMaxNo(stockMap));
				
				if(Util.null2str(shopTransactionId).equals("") || Util.isEmpty(map.get("payJobNo"))){
					errorStr = "shopTransactionId";
				}
			}
			
			if(errorStr != null){
				resultUrl += "&error=" + errorStr;
				return resultUrl;
			}
			
			String encryptShopTransactionId = EncryptionUtils.encryptAES(shopTransactionId, entCode);
			resultUrl += "&shopTransactionId=" + encryptShopTransactionId;
			
			ResponseHandler<String> handler = new BasicResponseHandler();
			JSONParser parser = new JSONParser();
			JSONObject data = new JSONObject();
			data.put("mallId", invoiceMap.get("MALL_ID"));
			data.put("shopTransactionId", shopTransactionId);
			data.put("authorizationId", request.getParameter("authorizationId"));
			data.put("shopOrderNo", request.getParameter("shopOrderNo"));
			data.put("approvalReqDate", DateTimeUtil.getShortDateString());
			
			map.put("shopTransactionId", shopTransactionId);
			map.put("paymentStatus", "R");
			map.put("invoiceNo", shopOrderNo);
			map.put("invGroupYn", invGroupYn);
			map.put("payType", "EASYPAY");
			map.put("approvalReqDate", data.get("approvalReqDate"));
			map.put("amount", invoiceMap.get("AMOUNT_TOTAL_LOC"));
			map.put("payMethodTypeCode", request.getParameter("shopValue1"));
			map.put("inputMethod", "REST");
			map.put("userId", stockMap.get("updateUserId"));
			payMapper.insertPayResult(map);
			
			invoiceMap.put("SHOP_TRANSACTION_ID", shopTransactionId);
			invoiceMap.put("APPROVAL_REQ_DATE", data.get("approvalReqDate"));
			
			HttpClient client = HttpClientBuilder.create().build();
			
			HttpPost httpPost = new HttpPost(Util.null2str(customProperties.getEsaypayUrl() + "/api/trades/approval"));
			/** 응답대기 시간은 최대 30초 동안 응답을 기다리는 것을 권고 합니다. */
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30*1000).setConnectTimeout(30*1000).setConnectionRequestTimeout(30*1000).build();
			httpPost.setConfig(requestConfig);
			httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
			httpPost.setEntity(new StringEntity(data.toString(), "UTF-8"));
			HttpResponse httpResponse = null;
			try{
				httpResponse = client.execute(httpPost);	/** 승인요청 */
			}catch(Exception e){
				remarks += "|승인Exception";
				status = "F";
				/** 대기시간 초과 및 네트워크 오류로 응답을 받지 못한 경우, 거래상태조회 API를 사용하여 PG거래번호를 조회한 후 반드시 취소요청을 해야 합니다. */
				try{
					/** 거래상태조회 */
					JSONObject returnData = this.easypayRetrieveTransaction(easypay, request, invoiceMap);
					if(returnData != null){
						result = new JSONObject(returnData);
					}
					/** 
					 * 이지페이 테스트환경에서 정상건 조회시 statusCode
					 * 신용카드 : TS03(매입요청)
					 * 가상계좌 : TS07(입금대기)
					 * 휴대폰   : TS01(승인)
					 */
					if(returnData != null && ("TS01".equals(returnData.get("statusCode")) || "TS03".equals(returnData.get("statusCode")) || "TS07".equals(returnData.get("statusCode")))){
						remarks += "|거래상태승인";
						/** 가맹점 주문정보의 총 금액과 승인결과 총 금액이 다를 시 반드시 ‘변경, 거래상태조회 API 매뉴얼’을 참고하여 취소 요청을 하시기 바랍니다. */
						if(Util.null2dbl(returnData.get("amount")) != Util.null2dbl(invoiceMap.get("AMOUNT_TOTAL_LOC"))){
							remarks += "|결제금액다름";
							easypay.setResMsg("approved amount is different.");
							status = "F";
							try{
								/** 승인취소 */
								invoiceMap.put("PG_CNO", returnData.get("pgCno"));
								easypay.setUserId(Util.null2str(stockMap.get("updateUserId")));
								/** 메시지인증 암복호화키가 없어서 승인취소 테스트하지 못함  */
								returnData = this.easypayRevise(easypay, request, invoiceMap);
								if(returnData != null){
									result = new JSONObject(returnData);
								}
								
								if(returnData == null || !"0000".equals(returnData.get("resCd"))){	/** 실패 승인취소응답 */
									remarks += "|승인취소실패";
									status = "E";
									if(returnData == null){
										easypay.setResMsg("revise fail error.");
										result.put("resCd", "XXXX");
										result.put("resMsg", easypay.getResMsg());
									}
								}else{
									remarks += "|승인취소성공";
								}
							}catch(Exception ee){
								remarks += "|승인취소Exception";
								status = "E";
								easypay.setResMsg("revise exception error.");
								result.put("resCd", "XXXX");
								result.put("resMsg", easypay.getResMsg());
							}
						}else{
							status = "S";
						}
					}
				}catch(Exception ee){
					remarks += "|거래확인Exception";
					status = "E";
					easypay.setResMsg("approval chkeck error.");
					result.put("resCd", "XXXX");
					result.put("resMsg", easypay.getResMsg());
				}
			}
	   	 
			if (status == null && httpResponse.getStatusLine().getStatusCode() == 200){
				try{
					String body = handler.handleResponse(httpResponse);
					JSONObject returnData = (JSONObject) parser.parse(body);
					result = new JSONObject(returnData);
					if("0000".equals(returnData.get("resCd"))){	/** 성공 승인응답 */
						remarks += "|승인성공";
						status = "S";
						/** 가맹점 주문정보의 총 금액과 승인결과 총 금액이 다를 시 반드시 ‘변경, 거래상태조회 API 매뉴얼’을 참고하여 취소 요청을 하시기 바랍니다. */
						if(Util.null2dbl(returnData.get("amount")) != Util.null2dbl(invoiceMap.get("AMOUNT_TOTAL_LOC"))){
							remarks += "|결제금액다름";
							easypay.setResMsg("Approved Amount Is Different.");
							status = "F";
							try{
								/** 승인취소 */
								easypay.setUserId(Util.null2str(stockMap.get("updateUserId")));
								invoiceMap.put("PG_CNO", returnData.get("pgCno"));
								returnData = this.easypayRevise(easypay, request, invoiceMap);
								if(returnData != null){
									result = new JSONObject(returnData);
								}
								
								if(returnData == null || !"0000".equals(returnData.get("resCd"))){	/** 실패 승인취소응답 */
									remarks += "|승인취소실패";
									status = "E";
									if(returnData == null){
										easypay.setResMsg("Revise Fail Error.");
										result.put("resCd", "XXXX");
										result.put("resMsg", easypay.getResMsg());
									}
								}else{
									remarks += "|승인취소성공";
								}
							}catch(Exception e){
								remarks += "|승인취소Exception";
								status = "E";
								easypay.setResMsg("Revise Exception Error.");
								result.put("resCd", "XXXX");
								result.put("resMsg", easypay.getResMsg());
							}
						}
					}else{
						/** 실패 승인응답 */
						remarks += "|승인실패";
						status = "F";
						easypay.setResCd(Util.null2str(returnData.get("resCd")));
						easypay.setResMsg(Util.null2str(returnData.get("resMsg")));
					}
				}catch(Exception e){
					remarks += "|승인확인Exception";
					status = "E";
					easypay.setResMsg("approval Error.");
					result.put("resCd", "XXXX");
					result.put("resMsg", easypay.getResMsg());
				}
			}else{
				if(httpResponse.getStatusLine().getStatusCode() != 200){
					remarks += "|승인응답코드오류";
					status = "F";
					easypay.setResMsg("http Request Error.");
					result.put("resCd", "XXXX" + httpResponse.getStatusLine().getStatusCode());
					result.put("resMsg", easypay.getResMsg());
				}
			}
		}catch(Exception e){
			remarks += "|시스템오류";
			status = "F";
			easypay.setResMsg("system Error.");
			result.put("resCd", "XXXX");
			result.put("resMsg", easypay.getResMsg());
		}
		
		if("22".equals(request.getParameter("shopValue1")) && "S".equals(status)){
			status = "R";
		}
		result.put("paymentStatus", status);
		result.put("payJobNo", map.get("payJobNo"));
		if(!"".equals(remarks)){
			result.put("remarks", remarks.substring(1));
		}
		this.updatePayResult(result);
		
		return resultUrl;
	}
	
	/**
	 * 정산서 이지페이 거래상태조회
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public JSONObject easypayRetrieveTransaction(Easypay easypay, HttpServletRequest request, Map<String, Object> invoiceMap) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		JSONObject returnData = null;
		if(invoiceMap == null) {
			String shopOrderNo = easypay.getShopOrderNo();
			map.put("entCode", easypay.getEntCode());
			if(shopOrderNo.indexOf("G") > -1){
				map.put("invoiceNoGroup", shopOrderNo);
			}else{
				map.put("invoiceJobNo", shopOrderNo);
			}
			Util.accessControlDatabase(map, "invoiceNoField", false, null, "", null, "");
			invoiceMap = payMapper.selectInvoicePaymentData(map);
		}
		
		if(invoiceMap != null && invoiceMap.size() < 0) {
			ResponseHandler<String> handler = new BasicResponseHandler();
			JSONParser parser = new JSONParser();
			JSONObject data = new JSONObject();
			data.put("mallId", invoiceMap.get("MALL_ID"));
			data.put("shopTransactionId", invoiceMap.get("SHOP_TRANSACTION_ID"));
			data.put("transactionDate", invoiceMap.get("APPROVAL_REQ_DATE"));
			
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost httpPost = new HttpPost(Util.null2str(customProperties.getEsaypayUrl() + "/api/trades/retrieveTransaction"));
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30*1000).setConnectTimeout(30*1000).setConnectionRequestTimeout(30*1000).build();
			httpPost.setConfig(requestConfig);
			httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
			httpPost.setEntity(new StringEntity(data.toString(), "UTF-8"));
			
			HttpResponse httpResponse = client.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == 200){
				String body = handler.handleResponse(httpResponse);
				returnData = (JSONObject) parser.parse(body);
			}
		}
		
		return returnData;
	}
	
	/**
	 * 정산서 이지페이 승인취소요청
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public JSONObject easypayRevise(Easypay easypay, HttpServletRequest request, Map<String, Object> invoiceMap) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		JSONObject returnData = null;
		if(invoiceMap == null) {
			String shopOrderNo = easypay.getShopOrderNo();
			map.put("entCode", easypay.getEntCode());
			if(shopOrderNo.indexOf("G") > -1){
				map.put("invoiceNoGroup", shopOrderNo);
			}else{
				map.put("invoiceJobNo", shopOrderNo);
			}
			Util.accessControlDatabase(map, "invoiceNoField", false, null, "", null, "");
			invoiceMap = payMapper.selectInvoicePaymentData(map);
		}
		
		if(invoiceMap != null && invoiceMap.size() < 0) {
			ResponseHandler<String> handler = new BasicResponseHandler();
			JSONParser parser = new JSONParser();
			JSONObject data = new JSONObject();
			data.put("mallId", invoiceMap.get("MALL_ID"));
			data.put("shopTransactionId", invoiceMap.get("SHOP_TRANSACTION_ID"));
			data.put("pgCno", invoiceMap.get("PG_CNO"));
			data.put("reviseTypeCode", "40");
			data.put("clientIp", Util.getClientIP(request));
			data.put("clientId", easypay.getUserId());
			data.put("msgAuthValue", this.easypayHmacSHA256(Util.null2str(data.get("pgCno")) + "|" + Util.null2str(data.get("shopTransactionId")), Util.null2str(invoiceMap.get("SECRET_KEY"))));
			data.put("cancelReqDate", invoiceMap.get("APPROVAL_REQ_DATE"));
			
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost httpPost = new HttpPost(Util.null2str(customProperties.getEsaypayUrl() + "/api/trades/revise"));
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30*1000).setConnectTimeout(30*1000).setConnectionRequestTimeout(30*1000).build();
			httpPost.setConfig(requestConfig);
			httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
			httpPost.setEntity(new StringEntity(data.toString(), "UTF-8"));
			
			HttpResponse httpResponse = client.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == 200){
				String body = handler.handleResponse(httpResponse);
				returnData = (JSONObject) parser.parse(body);
			}
		}
		
		return returnData;
	}
	
	/**
	 * 정산서 결제 결과정보 저장
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public void updatePayResult(JSONObject data) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("payJobNo", data.get("payJobNo"));
		map.put("resCd", data.get("resCd"));
		map.put("resMsg", data.get("resMsg"));
		map.put("pgCno", data.get("pgCno"));
		if(!Util.isEmpty(data.get("transactionDate"))){
			map.put("transactionDateTime", DateTimeUtil.changeFormat(Util.null2str(data.get("transactionDate")), "yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss"));
		}
		if(!Util.isEmpty(data.get("amount"))){
			map.put("amount", data.get("amount"));
		}
		map.put("statusCode", data.get("statusCode"));
		map.put("statusMessage", data.get("statusMessage"));
		map.put("paymentStatus", data.get("paymentStatus"));
		map.put("remarks", data.get("remarks"));
		
		if(data.get("paymentInfo") != null){
			JSONObject paymentInfo = (JSONObject)data.get("paymentInfo");
			map.put("payMethodTypeCode", paymentInfo.get("payMethodTypeCode"));
			map.put("approvalNo", paymentInfo.get("approvalNo"));
			if(!Util.isEmpty(paymentInfo.get("approvalDate"))){
				map.put("approvalDateTime", DateTimeUtil.changeFormat(Util.null2str(paymentInfo.get("approvalDate")), "yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss"));
			}
			map.put("cpCode", paymentInfo.get("cpCode"));
			if(!Util.isEmpty(paymentInfo.get("multiCardAmount"))){
				map.put("multiCardAmount", paymentInfo.get("multiCardAmount"));
			}
			if(!Util.isEmpty(paymentInfo.get("multiPntAmount"))){
				map.put("multiPntAmount", paymentInfo.get("multiPntAmount"));
			}
			
			if(paymentInfo.get("virtualAccountInfo") != null){	/** 가상계좌 */
				JSONObject virtualAccountInfo = (JSONObject)paymentInfo.get("virtualAccountInfo");
				map.put("bankName", virtualAccountInfo.get("bankName"));
				map.put("accountNo", virtualAccountInfo.get("accountNo"));
				map.put("depositName", virtualAccountInfo.get("depositName"));
				if(!Util.isEmpty(virtualAccountInfo.get("expiryDate"))){
					map.put("expiryDate", DateTimeUtil.changeFormat(Util.null2str(virtualAccountInfo.get("expiryDate")), "yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss"));
				}
			}
		}
		payMapper.updatePayResult(map);
	}
	
	/**
	 * 이지페이 결과정보 조회
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public Easypay getEasypayResult(Easypay easypay) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		String shopOrderNo = Util.null2str(easypay.getShopOrderNo());
		shopOrderNo = EncryptionUtils.decryptAES(shopOrderNo, Util.null2str(easypay.getEntCode()));
		if("noSearch".equals(easypay.getShopTransactionId())){
			map.put("shopTransactionId", easypay.getShopTransactionId());
		}else{
			map.put("shopTransactionId", EncryptionUtils.decryptAES(Util.null2str(easypay.getShopTransactionId()), Util.null2str(easypay.getEntCode())));
		}
		map.put("entCode", easypay.getEntCode());
		map.put("invoiceNo", shopOrderNo);
		if(shopOrderNo.indexOf("G") > -1){
			map.put("invoiceNoField", "INVOICE_NO_GROUP");
		}else{
			map.put("invoiceNoField", "INVOICE_JOB_NO");
		}
		
		Util.accessControlDatabase(map, "invoiceNoField", false, null, "", null, "");
		return payMapper.selectEasypayResult(map);
	}
	
	/**
	 * 이지페이 결제 청구서 조회
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public List<Easypay> getEasypayInvoiceList(Easypay easypay) throws Exception {
		List<Easypay> returnList = null;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("srchBlNo", easypay.getSrchBlNo());
		List<Map<String,Object>> invoiceNoList = payMapper.selectInvoiceList(map);
		if(invoiceNoList != null && invoiceNoList.size() > 0) {
			map.put("invoiceNoList", invoiceNoList);
			returnList = payMapper.selectEasypayInvoice(map);
			if(returnList != null && returnList.size() > 0) {
				for(Easypay data : returnList){
					data.setShopOrderNo(EncryptionUtils.encryptAES(data.getShopOrderNo(), easypay.getEntCode()));
				}
			}
		}
		return returnList;
	}
	
	/**
	 * 이지페이 결과정보 수신(noti)
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public String easypayNoti(HttpServletRequest request) throws Exception {
		try {
			Map<String, String[]> requestMap = request.getParameterMap();
			Iterator<String> iterator = requestMap.keySet().iterator();
			
//			try {
//				log.error("test");
//				log.error(request.getParameter("bank_nm"));
//				log.error(requestMap.get("bank_nm")[0]);
//				request.setCharacterEncoding("UTF-8");
//				log.error(request.getParameter("bank_nm"));
//				log.error(requestMap.get("bank_nm")[0]);
//				request.setCharacterEncoding("EUC-KR");
//				log.error(request.getParameter("bank_nm"));
//				log.error(requestMap.get("bank_nm")[0]);
//			}catch(Exception ee){
//				log.error("testError");
//			}
			
			Map<String, Object> map = new HashMap<String, Object>();
			
		    while (iterator.hasNext()) {
		        String key = (String)iterator.next();
		        map.put(key, requestMap.get(key)[0]);
		    }
			
			Util.setDB(request, "MASTER");
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			tmpMap.put("easypayMallId", map.get("memb_id"));
			String entCode = payMapper.selectEntCode(tmpMap);
			if(entCode != null){
				Util.setDB(request, entCode);
				
				String invoiceNo = Util.null2str(map.get("order_no"));
				String invGroupYn = null;
				String userId = null;
				if(invoiceNo.indexOf("G") > -1){
					invGroupYn = "Y";
				}else{
					invGroupYn = "N";
				}
				
				tmpMap.clear();
				tmpMap.put("seqNo", "1");
				tmpMap.put("gmtTime", "0");
				tmpMap.put("entCode", entCode);
				tmpMap.put("entBusinessCode", "");
				if(!Util.null2str(map.get("user_id")).equals("")){
					userId = Util.null2str(map.get("user_id"));
				}else{
					tmpMap.put("invoiceNo", invoiceNo);
					if("Y".equals(invGroupYn)){
						tmpMap.put("invoiceNoField", "INVOICE_NO_GROUP");
					}else{
						tmpMap.put("invoiceNoField", "INVOICE_JOB_NO");
					}
					Map<String, Object> invoiceMap = payMapper.selectInvoicePaymentData(tmpMap);
					if(invoiceMap != null && invoiceMap.size() > 0){
						userId = Util.null2str(invoiceMap.get("ACCOUNT_CODE"));
					}else{
						userId = "customer";
					}
				}
				tmpMap.put("updateUserId", userId);
				tmpMap.put("solution", "ECM");
				tmpMap.put("stockSeqLength", 8);
				tmpMap.put("dateLength", null);
				tmpMap.put("signType", null);
				tmpMap.put("stockDateFormat", null);
				tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
				tmpMap.put("stockType", "EASY");
				tmpMap.put("stockSign", "EASY");
				tmpMap.put("stockRemark", "EASY-PAYMENT-JOB NO");
				map.put("payJobNo", commonMapper.selectNextStockMaxNo(tmpMap));
				
				String notiType = Util.null2str(map.get("noti_type"));
				tmpMap.clear();
				tmpMap.put("insertType", "easypayNoti");
				tmpMap.put("payJobNo", map.get("payJobNo"));
				tmpMap.put("entCode", entCode);
				tmpMap.put("invoiceNo", invoiceNo);
				tmpMap.put("invGroupYn", invGroupYn);
				tmpMap.put("payType", "EASYPAY");
				tmpMap.put("amount", map.get("amount"));
//				tmpMap.put("shopTransactionId", "");
				tmpMap.put("payMethodTypeCode", map.get("pay_type"));
				if(notiType.equals("10") || notiType.equals("30")){
					tmpMap.put("paymentStatus", "S");
				}else{
					tmpMap.put("paymentStatus", "F");
				}
//				tmpMap.put("approvalReqDate", "");
				tmpMap.put("inputMethod", "NOTI");
				tmpMap.put("resCd", map.get("res_cd"));
				tmpMap.put("resMsg", map.get("res_msg"));
				tmpMap.put("pgCno", map.get("cno"));
				tmpMap.put("notiType", notiType);
				tmpMap.put("statusCode", map.get("stat_cd"));
				tmpMap.put("statusMessage", map.get("stat_msg"));
//				tmpMap.put("approvalNo", "");
//				tmpMap.put("approvalDateTime", "");
				tmpMap.put("cpCode", map.get("cp_cd"));
//				tmpMap.put("multiCardAmount", "");
//				tmpMap.put("multiPntAmount", "");
				tmpMap.put("bankName", map.get("bank_nm"));
				tmpMap.put("accountNo", map.get("account_no"));
				tmpMap.put("depositName", map.get("deposit_nm"));
//				tmpMap.put("expiryDate", "");
//				tmpMap.put("remarks", "");
				tmpMap.put("userId", userId);
				if(!Util.isEmpty(map.get("tran_date"))){
					tmpMap.put("transactionDateTime", DateTimeUtil.changeFormat(Util.null2str(map.get("tran_date")), "yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss"));
				}
				
				payMapper.insertPayResult(tmpMap);
			}
			return "res_cd=0000" + (char)0x1f + "res_msg=SUCCESS";
		}catch(Exception e){
			log.error(Util.getPrintStacTraceString(e));
			return "res_cd=5001" + (char)0x1f + "res_msg=FAIL";
		}
	}
	
	/**
	 * 정산서 이지페이 msgAuthValue 암호화
	 * @param order
	 * @return
	 * @throws Exception
	 */
	public String easypayHmacSHA256(String message, String secretKey) throws Exception {
		try {
			Mac sha256_HMAC = Mac.getInstance( "HmacSHA256" );
			SecretKeySpec secret_key = new SecretKeySpec( secretKey.getBytes(), "HmacSHA256" );
			sha256_HMAC.init( secret_key );

			byte[] hash = sha256_HMAC.doFinal( message.getBytes() ); // hash 값을 HexString 으로 변환하세요.

			//byte array to hexString
			StringBuilder sb = new StringBuilder();
			for(final byte b : hash){
				sb.append(String.format("%02x", b&0xff));
			}
			return sb.toString();
		}catch ( Exception e ) {
			return null;
		}
	}
	
	
}
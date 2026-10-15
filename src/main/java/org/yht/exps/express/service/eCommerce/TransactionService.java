package org.yht.exps.express.service.eCommerce;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.script.DateFieldScript.Parse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.eCommerce.TransactionMapper;
import org.yht.exps.express.model.eCommerce.transaction.TransactionDet;
import org.yht.exps.express.model.eCommerce.transaction.TransactionOdList;
import org.yht.exps.express.model.eCommerce.transaction.TransactionOdItem;
import org.yht.exps.express.model.eCommerce.transaction.TransactionSaveResult;
import org.yht.exps.express.model.eCommerce.transaction.search.SearchTransactionList;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.edi.EdiCommonService;
import org.yht.util.DateTime;
import org.yht.util.Util;

import com.google.gson.Gson;

import ch.qos.logback.core.pattern.parser.Parser;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

@Slf4j
@Service
@Transactional
public class TransactionService {
	@Autowired
	TransactionMapper transactionMapper;

	@Autowired
	StringRedisTemplate redisTemplate;

	@Resource(name = "commonService")
	CommonService commonService;

	@Resource(name="ediCommonService")
	EdiCommonService ediCommonService;

	@Value("${spring.profiles.active}")
	private String profile;
	
	private String reqtTp = "01";
	private String ordNoIclnTp = "02";
	
	/**
	 * Transaction Order List
	 * @param SearchTransactionList
	 * @return
	 * @throws Exception
	 */
	public TransactionOdList selectTransOdList(SearchTransactionList search) throws Exception {
		TransactionOdList transDetList = new TransactionOdList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}

		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}

		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		}else{
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}

		List<TransactionDet> list = transactionMapper.selectTransOdList(search);
		transDetList.setTransDetList(list);

		if(list.size() > 0) transDetList.setTotal(list.get(0).getTotal());
		else transDetList.setTotal(0);

		return transDetList;
	}

	/**
	 * Transaction Detail List
	 * @param SearchTransactionList
	 * @return
	 * @throws Exception
	 */
	public TransactionOdList selectTransDetailList(SearchTransactionList search) throws Exception {
		TransactionOdList transDetList = new TransactionOdList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}

		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}

		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		}else{
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}

		List<TransactionOdItem> list = transactionMapper.selectTransItemList(search);
		transDetList.setTransOdItemList(list);

		if(list.size() > 0) transDetList.setTotal(list.get(0).getTotal());
		else transDetList.setTotal(0);

		return transDetList;
	}

	/**
	 * Delete Transaction Info
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public TransactionSaveResult deleteTransactionInfo(SearchTransactionList search, HttpServletRequest request) throws Exception {
		TransactionSaveResult transactionSaveResult = new TransactionSaveResult();
		Map<String, Object> data = new HashMap<String, Object>();

		for(Map<String,Object> info : search.getTransNoList()) {
			data.put("entCode", search.getEntCode());
			data.put("odJobNo", Util.null2str(info.get("odJobNo")));
			data.put("ecIfSbmtNo", Util.null2str(info.get("ecIfSbmtNo")));
			transactionMapper.deleteTransactionOd(data);
			transactionMapper.deleteTransactionInfo(data);

			// 처리 로그 기록
			ProcessLogs processLogs = new ProcessLogs();
			processLogs.setEntCode(search.getEntCode());
			processLogs.setEntBusinessCode(search.getSrchEntBusinessCode());
			processLogs.setSolution("ECM");
			processLogs.setBound("AI".equals(Util.null2str(search.getSrchBoundType())) ? "AIR" : "SEA");
			processLogs.setBoundType("IMP");
			processLogs.setProcessUserId(search.getUserId());
			processLogs.setLogJobType("DATA");
			processLogs.setJobFuncType("DELETE");
			processLogs.setDescription("DELETE TRANJACTION INFO [OD_JOB_NO : "+ Util.null2str(info.get("odJobNo"))+"]");
			processLogs.setJobLog(new Gson().toJson(info));
			processLogs.setResult("ok");
			processLogs.setJobNo(Util.null2str(info.get("odJobNo")));
			processLogs.setEntBusinessPrintName(search.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(search.getLogUserName());
			processLogs.setProcessUserNameEng(search.getLogUserNameEng());
			commonService.insertLog(processLogs, request);
		}

		return transactionSaveResult;
	}

	/**
	 * Send Transaction Info
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public TransactionSaveResult sendTransInfo(SearchTransactionList search) throws Exception {
		TransactionSaveResult transactionSaveResult = new TransactionSaveResult();

		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> sendMap = new HashMap<String, Object>();
		Map<String, Object> customerInfo = new HashMap<String, Object>();
		List<String> custCodeList = new ArrayList<String>();
		List<String> errFieldList = new ArrayList<String>();
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> odInfoList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> orderInfoList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> productInfoList = new ArrayList<Map<String, Object>>();

		int itemCnt = 0;
		String odJobNo = "";
		String ecIfSbmtNo = "";
		String ediPortalMailboxId = "";
		boolean isValChk = false;

		log.debug("********** sendTransInfo START **********");
		for(Map<String, Object> transInfo : search.getTransNoList()) {
			data = new HashMap<String, Object>();
			sendMap = new HashMap<String, Object>();
			odJobNo = Util.null2str(transInfo.get("odJobNo"));
			ediPortalMailboxId = transactionMapper.selectEdiPortalMailboxId(odJobNo);

			// 주문 정보
			odInfoList = transactionMapper.selectTransOdInfo(odJobNo);

			// 상하위 정보
			custCodeList = transactionMapper.selectCustomerCode(odJobNo);

			if( custCodeList.size() > 0 ) {
				for(String custCode : custCodeList) {
					customerInfo = chkCustomerInfo(search.getEntCode(), custCode);
					if(customerInfo != null) break;
				}
			}

			if(customerInfo == null) {
				isValChk = true;
				errFieldList.add("전자상거래 정보");
				log.debug("[sendTransInfo] Required field value : 전자상거래 정보");
				break;
			}

			// 상품 정보
			if(odInfoList.size() > 0) {
				for(Map<String, Object> orderInfo : odInfoList) {
					productInfoList = transactionMapper.selectTransProductInfoList(odJobNo);
					orderInfo.put("product_info", productInfoList);
					orderInfoList.add(orderInfo);
					itemCnt += productInfoList.size();
	
					errFieldList = chkSendFieldData(orderInfo, productInfoList);
	
					if( errFieldList.size() > 0 ) {
						isValChk = true;
						break;
					}
				}
			}

			if( "".equals(transInfo.get("ecIfSbmtNo")) ) {
				errFieldList.add(0, "거래정보제출번호");
				isValChk = true;
			}

			if( isValChk ) break;

			ecIfSbmtNo = Util.null2str(customerInfo.get("commBusinessCode")) + DateTime.getDate("yyMMdd") + Util.setSeqNo(transactionMapper.selectMaxSeqNoCnt(), 6);
			data.put("txif_sbmt_no", ecIfSbmtNo);												// 거래정보제출번호
			data.put("elcm_ents_sgn", Util.null2str(customerInfo.get("commBusinessCode")));		// 전자상거래업자부호
			data.put("elcm_ents_nm", "");														// 전자상거래업체명
			data.put("order_info", orderInfoList);

//			sendMap.put("ediPortalMailboxId", ediPortalMailboxId);
			sendMap.put("clientId", customerInfo.get("clientId"));								// 관세청에서 발급받은 Client ID
			sendMap.put("clientSecret", customerInfo.get("clientSecret"));						// 관세청에서 발급받은 Client Secret
			sendMap.put("requestData", data);
			dataList.add(sendMap);
			log.debug("[sendTransInfo - {}] order info : {} ", odJobNo, orderInfoList);
		}

		// Validation Item size
//		if( itemCnt > 999 ) {
//			transactionSaveResult.setResultCode("ABMS100008"); // #1#인 경우는 전송 할 수 없습니다.
//			log.debug("[sendTransInfo] Over Item size");
//			return transactionSaveResult;
//		}

		// Validation Required field value
		if( isValChk ) {
			transactionSaveResult.setResultCode("validField");
			log.debug("[sendTransInfo] Required field value : {}", errFieldList);
//			transactionSaveResult.setResultCode("A11460"); // #1#인 경우는 전송 할 수 없습니다.
//			transactionSaveResult.setResultData((Map<String, Object>) new HashMap<String, Object>().put("errData", String.join(", ", errFieldList)));
			return transactionSaveResult;
		}

		String key = "E-COMMERCE_TRANSACTION_INFO_SEND_" + search.getEntCode();
		ListOperations<String, String> listOperations = redisTemplate.opsForList();

		JSONObject json = new JSONObject();
		json.put("entCode", search.getEntCode());
		json.put("entBusinessCode", search.getSrchEntBusinessCode());
		json.put("bound", "AI".equals(Util.null2str(search.getSrchBoundType())) ? "AIR" : "SEA");
		json.put("boundType", "IMP");
		json.put("processUserId", search.getUserId());
		json.put("entBusinessPrintName", search.getLogUserBusinessPrintName());
		json.put("processUserName", search.getLogUserName());
		json.put("processUserNameEng", search.getLogUserNameEng());
		json.put("dataList", dataList);

//		listOperations.rightPush(key, json.toString());
		log.debug("[sendTransInfo] sendData : {} ", json);

//		transactionMapper.updateSendInfo(data);

		log.debug("********** sendTransInfo END **********");
		return transactionSaveResult;
	}

	/** Validation Send */
	private List<String> chkSendFieldData(Map<String, Object> orderInfo, List<Map<String, Object>> productInfoList) {
		List<String> emptyFieldList = new ArrayList<String>();
		boolean isEmptyProdField = false;
		if( "".equals(Util.null2str(orderInfo.get("hbl_no"))) ) emptyFieldList.add("운송장 번호");
		if( "".equals(Util.null2str(orderInfo.get("ord_no"))) ) emptyFieldList.add("주문번호");
		if( "".equals(Util.null2str(orderInfo.get("elcm_pcd"))) ) emptyFieldList.add("전자상거래 판매유형코드");
		if( "".equals(Util.null2str(orderInfo.get("ord_site_url"))) ) emptyFieldList.add("주문 사이트 인터넷 주소");
		if( "".equals(Util.null2str(orderInfo.get("elcm_ents_cnty_cd"))) ) emptyFieldList.add("전자상거래업체 국가코드");
		if( "".equals(Util.null2str(orderInfo.get("ord_cmpl_dttm"))) ) emptyFieldList.add("주문완료일시");
		if( "".equals(Util.null2str(orderInfo.get("ordrr_nm"))) ) emptyFieldList.add("주문자 성명");
		if( "".equals(Util.null2str(orderInfo.get("ordrr_telno"))) ) emptyFieldList.add("주문자 전화번호");
		if( "".equals(Util.null2str(orderInfo.get("cnsi_nm"))) ) emptyFieldList.add("수하인 성명");
		if( "".equals(Util.null2str(orderInfo.get("cnsi_telno"))) ) emptyFieldList.add("수하인 전화번호");
		if( "".equals(Util.null2str(orderInfo.get("cnsi_psno"))) ) emptyFieldList.add("수하인 우편번호");
		if( "".equals(Util.null2str(orderInfo.get("cnsi_bscs_addr"))) ) emptyFieldList.add("수하인 기본주소");
		if( "".equals(Util.null2str(orderInfo.get("cnsi_pers_ecm"))) ) emptyFieldList.add("수하인 개인통관고유부호");
		if( "".equals(Util.null2str(orderInfo.get("tot_stlm_amt"))) ) emptyFieldList.add("총 결제금액");
		if( "".equals(Util.null2str(orderInfo.get("tot_stlm_amt_curr_cd"))) ) emptyFieldList.add("총 결제금액 통화단위");
		if( "".equals(Util.null2str(orderInfo.get("tot_ord_prod_qty"))) ) emptyFieldList.add("총 주문 상품수량");

		if( productInfoList.size() > 0 ) {
			for(Map<String, Object> prodInfo : productInfoList) {
				if( "".equals(Util.null2str(prodInfo.get("prod_srno"))) ) {
					emptyFieldList.add("상품 일련번호");
					isEmptyProdField = true;
				}
				if( "".equals(Util.null2str(prodInfo.get("prod_clsf_nm_stp1"))) ) {
					emptyFieldList.add("상품분류명 1단계");
					isEmptyProdField = true;
				}
				if( "".equals(Util.null2str(prodInfo.get("prod_clsf_nm_stp2"))) ) {
					emptyFieldList.add("상품분류명 2단계");
					isEmptyProdField = true;
				}
				if( "".equals(Util.null2str(prodInfo.get("ord_prod_nm"))) ) {
					emptyFieldList.add("주문상품명");
					isEmptyProdField = true;
				}
				if( "".equals(Util.null2str(prodInfo.get("prod_sale_pge_ur"))) ) {
					emptyFieldList.add("상품판매 페이지 주소");
					isEmptyProdField = true;
				}
				if( "".equals(Util.null2str(prodInfo.get("prod_ord_qty"))) ) {
					emptyFieldList.add("상품주문수량");
					isEmptyProdField = true;
				}
				if( "".equals(Util.null2str(prodInfo.get("prod_unprc"))) ) {
					emptyFieldList.add("상품단가");
					isEmptyProdField = true;
				}
				if( "".equals(Util.null2str(prodInfo.get("prod_unprc_curr_cd"))) ) {
					emptyFieldList.add("상품단가 통화단위");
					isEmptyProdField = true;
				}
				if( "".equals(Util.null2str(prodInfo.get("ord_amt"))) ) {
					emptyFieldList.add("주문금액");
					isEmptyProdField = true;
				}
				if( "".equals(Util.null2str(prodInfo.get("ord_amt_curr_cd"))) ) {
					emptyFieldList.add("주문금액 통화단위");
					isEmptyProdField = true;
				}

				if(isEmptyProdField) break;
			}
		}

		return emptyFieldList;
	}

	/**
	 * Check Transaction Info CustomsId
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public TransactionSaveResult checkTransCustomsId(SearchTransactionList search) throws Exception {
		TransactionSaveResult transactionSaveResult = new TransactionSaveResult();

		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> sendMap = new HashMap<String, Object>();
		Map<String, Object> ordrrInfo = new HashMap<String, Object>();
		Map<String, Object> cmsiInfo = new HashMap<String, Object>();
		Map<String, Object> resData = new HashMap<String, Object>();
		Map<String, Object> customerInfo = new HashMap<String, Object>();
		List<String> errFieldList = new ArrayList<String>();
		List<String> errDataList = new ArrayList<String>();
		List<String> custCodeList = new ArrayList<String>();
		List<Map<String, Object>> odInfoList = new ArrayList<Map<String, Object>>();

		int seqCnt = 0;
		String odJobNo = "";
		String resCode = "";
		String resultCode = "";
		String sendType = "";
		boolean isReValid = "Y".equals(Util.null2str(search.getReCheckYn()));

		log.debug("********** checkTransCustomsId START **********");
		for(Map<String, Object> transInfo : search.getTransNoList()) {
			odJobNo = Util.null2str(transInfo.get("odJobNo"));

			// 주문 정보
			odInfoList = transactionMapper.selectChkTransCustomsInfo(odJobNo);

			// 상하위 정보
			custCodeList = transactionMapper.selectCustomerCode(odJobNo);

			// 상하위정보
			if( custCodeList.size() > 0 ) {
				for(String custCode : custCodeList) {
					customerInfo = chkCustomerInfo(search.getEntCode(), custCode);
					if(customerInfo != null) break;
				}
			}

			if(customerInfo == null) {
				errDataList.add(Util.null2str(odInfoList.get(0).get("hbl_no")));
				log.debug("[checkTransCustomsId] Required field value : 전자상거래 정보");
				continue;
			}

			sendType = isReValid ? "revalidation" : "validation";

			if( odInfoList.size() > 0 ) {
				for(Map<String, Object> odInfo : odInfoList) {
					data = new HashMap<String, Object>();
					sendMap = new HashMap<String, Object>();
					// Validation Required field value
					errFieldList = chkSendFieldData(odInfo);

					if( errFieldList.size() > 0 ) {
						errDataList.add(Util.null2str(odInfo.get("hbl_no")));
						log.debug("[checkTransCustomsId] Required field value : {}", errFieldList);
					}
					if( errDataList.size() > 0 ) break;

					ordrrInfo = new HashMap<String, Object>();
					cmsiInfo = new HashMap<String, Object>();
					
					/* 주문자 정보 */
					ordrrInfo.put("ordrr_fnm", Util.null2str(odInfo.get("odInfo")));
					ordrrInfo.put("ordrr_telno", Util.null2str(odInfo.get("ordrr_telno")));

					/* 수하인 정보 */
					cmsiInfo.put("cnsi_fnm", Util.null2str(odInfo.get("cnsi_fnm")));
					cmsiInfo.put("cnsi_telno", Util.null2str(odInfo.get("cnsi_telno")));
					cmsiInfo.put("dlvrpl_psno", Util.null2str(odInfo.get("dlvrpl_psno")));

					/* 전송 정보 */
					data.put("reqt_tp", reqtTp); 															// 요청구분
					data.put("ordrr_info", ordrrInfo); 														// 주문자 정보
					data.put("cnsi_info", cmsiInfo); 														// 수하인 정보
					data.put("cnsi_pers_ecm", Util.null2str(odInfo.get("cnsi_pers_ecm"))); 					// 수하인 개인통관고유부호
					if(!isReValid) {
						data.put("reqt_no", DateTime.getDate("yyyyMMddHHmm") + Util.setSeqNo(seqCnt, 6));	// 요청번호
						data.put("ord_no_icln_tp", ordNoIclnTp); 											// 주문번호 포함 구분
						data.put("ord_no", Util.null2str(odInfo.get("ord_no"))); 							// 주문번호
					} else {
						data.put("reqt_no", odInfo.get("ecm_gov_req_no"));									// 요청번호
					}

//					sendMap.put("clientId", customerInfo.get("clientId"));									// 관세청에서 발급받은 Client ID
//					sendMap.put("clientSecret", customerInfo.get("clientSecret"));							// 관세청에서 발급받은 Client Secret
//					sendMap.put("requestData", data);

					JSONObject sendData = new JSONObject();
					sendData.put("clientId", customerInfo.get("clientId"));									// 관세청에서 발급받은 Client ID
					sendData.put("clientSecret", customerInfo.get("clientSecret"));							// 관세청에서 발급받은 Client Secret
					sendData.put("requestData", data);
					log.debug("[checkTransCustomsId - {}] sendData : {} ", odJobNo, sendMap);

//					JSONObject jsonObject = ediCommonService.sendEbmsGov(search.getEntCode(), sendType, sendData);
					
//					resultCode = Util.null2str(jsonObject.get("resultCode"));
//					if( "0000".equals(resultCode) ) {
//						resData = (Map<String, Object>) jsonObject.get("customsResponse");
//						resCode = Util.null2str(resData.get("resCode"));
//						data.put("resCode", "NV");
//						data.put("reqNo", data.get("reqt_no"));
//						data.put("customsTempNo", ((Map<String, Object>) ((Map<String, Object>) resData.get("res_data")).get("su_info")).get("su_crtf_no"));
//					} else {
//						data.put("resCode", "N");
//					}
//					data.put("odJobNo", odJobNo);
//
//					transactionMapper.updateCheckCustomsId(data);
//					seqCnt++;
				}
			}
		}

		if( errDataList.size() > 0 ) {
			Map<String, Object> resultData = new HashMap<String, Object>();
			resultData.put("errData", String.join(", ", errDataList.stream().distinct().collect(Collectors.toList())));
			transactionSaveResult.setResultCode("validField");
			transactionSaveResult.setResultData(resultData);
			return transactionSaveResult;
		}

//		JSONObject sendData = new JSONObject();
//		sendData.put("dataList", data);

//		JSONObject jsonObject = ediCommonService.sendEbms(ediPortalMailboxId, "GOVCBRDJR", sendData, "LIST");

//		transactionMapper.updateSendInfo(data);

		log.debug("********** checkTransCustomsId END **********");
		return transactionSaveResult;
	}

	/** Validation Customs Info */
	private Map<String, Object> chkCustomerInfo(String entCode, String custCode) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("entCode", entCode);
		data.put("customerCd", custCode);

		Map<String, Object> customerInfo = transactionMapper.selectCustomerInfo(data);
		if( "".equals(Util.null2str(customerInfo.get("commBusinessCode"))) || "".equals(Util.null2str(customerInfo.get("clientId"))) || "".equals(Util.null2str(customerInfo.get("clientSecret"))) ) {
			return null;
		} else {
			return customerInfo;
		}
	}

	/** Validation CustomsId */
	private List<String> chkSendFieldData(Map<String, Object> orderInfo) {
		List<String> emptyFieldList = new ArrayList<String>();

		if( !"".equals(Util.null2str(orderInfo.get("ordrr_fnm"))) ) emptyFieldList.add("주문자 성명");
		if( "".equals(Util.null2str(orderInfo.get("cnsi_fnm"))) ) emptyFieldList.add("수하인 성명");
		if( "".equals(Util.null2str(orderInfo.get("cnsi_telno"))) ) emptyFieldList.add("수하인 전화번호");
		if( "".equals(Util.null2str(orderInfo.get("dlvrpl_psno"))) ) emptyFieldList.add("수하인 우편번호");
		if( "".equals(Util.null2str(orderInfo.get("cnsi_pers_ecm"))) ) emptyFieldList.add("수하인 개인통관고유부호");
		if( "".equals(Util.null2str(orderInfo.get("ord_no"))) ) emptyFieldList.add("주문번호");

		return emptyFieldList;
	}

	/**
	 * ECM 거래정보 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectTransOdListExcel(SearchTransactionList search) throws Exception {
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}

		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}

		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		}else{
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}

		List<Map<String, Object>> list = transactionMapper.selectTransOdExcelList(search);

		return list;
	}
}
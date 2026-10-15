package org.yht.exps.express.service.eCommerce;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.eCommerce.TrackingOrderMapper;
import org.yht.exps.express.model.eCommerce.pod.TrackingPod;
import org.yht.exps.express.model.eCommerce.pod.search.SearchPodList;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.util.DateTime;
import org.yht.util.EncryptionUtils;
import org.yht.util.Util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Slf4j
@Service
public class TrackingPodInfoSendParser {

	@Autowired
	CommonMapper commonMapper;
	
	@Autowired
	TrackingOrderMapper trackingOrderMapper;
	
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	
	@Autowired
	RestHighLevelClient client;
	
	@Autowired
	PlatformTransactionManager transactionManager;
	
	
	
	/**
	 * 트래킹 진행상태 전송 API
	 * @param entCode
	 * @return
	 */
	public void callTrackingPodInfoSendExecutor(SearchPodList search) {
		log.info("=========================== trackingPodInfoSendExecutor : " + search.getSrchEntCode() + " START==============");
		
		SqlSession sqlSession = null;
		TransactionStatus status = null;
				
		try {
			// ES Index
			String indexName = "ecm-tracking-pod-logs-";
			String index = "ecm-interface-logs-";
			String if_status_info = "";
			
			String entCode = Util.null2str(search.getSrchEntCode());
			String logId = "";
			String[] custList = {"COUPANG-CN","COUPANG"};
			for(String custCode:custList) {
				Map<String, Object> tmpMap = new HashMap<String, Object>();
				DocumentApi documentApi = new DocumentApi(client);
				IndexApi indexApi = new IndexApi(client);
				
				Map<String,Object> trackingSrchMap = new HashMap<String, Object>();
				List<TrackingPod> trackingList = new ArrayList<TrackingPod>();
				trackingSrchMap.put("entCode", entCode);
				trackingSrchMap.put("action", search.getAction());
				trackingSrchMap.put("searchNoList", search.getSearchNoList());
				trackingSrchMap.put("customer", custCode);
				trackingList = trackingOrderMapper.selectTrackingList(trackingSrchMap);

				Map<String, Object> returnMap = null;
				
				if (trackingList != null && trackingList.size() > 0) {
					JSONArray jsonArray = new JSONArray();
					Map<String, Object> podObjMap = new HashMap<String, Object>();
					
					for(TrackingPod trackingPod : trackingList) {
						Map<String, Object> podObj = new LinkedHashMap<String, Object>();
						String podStatus = Util.null2str(trackingPod.getPodStatus());
						String remarks = Util.null2str(trackingPod.getRemarks());
						
						String statusCode = "";
						String statusMessage = "";
						String statusEnglishMessage = "";
						String invoiceType = "DELIVERY";
						String branchName = "";
						String branchTel = "";
						String workerName = "";
						String workerTel = "";
						
						switch(podStatus) {
							case "ECM010":
								statusCode = "01";
								statusMessage = "운송장 등록";
								statusEnglishMessage = "Waybill registered";
								break;
							case "ECM210": case "ECM250":
								statusCode = "11";
								statusMessage = "통관시작";
								statusEnglishMessage = "Customs Start";
								
								branchName = "허브넷통관장";
								branchTel = "02-2662-8851(내선2번)";
								workerName = "허브넷통관장";
								workerTel = "02-2662-8851(내선2번)";
								break;
							case "ECM240": case "ECM280":
								statusCode = "15";
								statusMessage = "통관완료";
								statusEnglishMessage = "Customs Cleared";
								
								branchName = "허브넷통관장";
								branchTel = "02-2662-8851(내선2번)";
								workerName = "허브넷통관장";
								workerTel = "02-2662-8851(내선2번)";
								break;
							case "ECM361":
								statusCode = "16";
								statusMessage = "택배집하";
								statusEnglishMessage = "Delivery Departed";
								
								branchName = Util.null2str(trackingPod.getBranchName());
								branchTel = Util.null2str(trackingPod.getBranchTel());
								workerName = Util.null2str(trackingPod.getWorkerName());
								workerTel = Util.null2str(trackingPod.getWorkerTel());
								break;
							case "ECM370":
								statusCode = "17";
								statusMessage = "간선이동중";
								statusEnglishMessage = "In Transit 1";
								
								branchName = "Hubnet-CJ";
								branchTel = "";
								workerName = "";
								workerTel = "";
								break;
							case "ECM380":
								statusCode = "17";
								statusMessage = "간선이동중";
								statusEnglishMessage = "In Transit 2";
								
								branchName = "Hubnet-CJ";
								branchTel = "";
								workerName = "";
								workerTel = "";
								break;
							case "ECM390":
								statusCode = "19";
								statusMessage = "배송출발";
								statusEnglishMessage = "Delivery Start";
								
								branchName = Util.null2str(trackingPod.getBranchName());
								branchTel = Util.null2str(trackingPod.getBranchTel());
								workerName = Util.null2str(trackingPod.getWorkerName());
								workerTel = Util.null2str(trackingPod.getWorkerTel());
								break;
							case "ECM400":
								statusCode = "92";
								statusMessage = "미배송";
								statusEnglishMessage = "Undelivered";
								break;
							case "ECM420":
								statusCode = "20";
								statusMessage = "배송완료";
								statusEnglishMessage = "Delivery Completed";
								
								branchName = Util.null2str(trackingPod.getBranchName());
								branchTel = Util.null2str(trackingPod.getBranchTel());
								workerName = Util.null2str(trackingPod.getWorkerName());
								workerTel = Util.null2str(trackingPod.getWorkerTel());
								break;
							case "ECM510":
								statusCode = "01";
								statusMessage = "반품지시";
								statusEnglishMessage = "Instruct Transfer";
								invoiceType = "RETURN";
								
								branchName = "Hubnet-CJ";
								branchTel = "";
								workerName = "";
								workerTel = "";
								break;
							case "ECM520":
								statusCode = "16";
								statusMessage = "택배집하_반품";
								statusEnglishMessage = "Pick Up Completed";
								invoiceType = "RETURN";
								
								branchName = Util.null2str(trackingPod.getBranchName());
								branchTel = Util.null2str(trackingPod.getBranchTel());
								workerName = Util.null2str(trackingPod.getWorkerName());
								workerTel = Util.null2str(trackingPod.getWorkerTel());
								break;
							case "ECM530":
								statusCode = "19";
								statusMessage = "배송출발_반품";
								statusEnglishMessage = "Trunk Departed";
								invoiceType = "RETURN";
								
								branchName = Util.null2str(trackingPod.getBranchName());
								branchTel = Util.null2str(trackingPod.getBranchTel());
								workerName = Util.null2str(trackingPod.getWorkerName());
								workerTel = Util.null2str(trackingPod.getWorkerTel());
								break;
							case "ECM540":
								statusCode = "20";
								statusMessage = "배송완료_반품";
								statusEnglishMessage = "Delivery Completed";
								invoiceType = "RETURN";
								
								branchName = Util.null2str(trackingPod.getBranchName());
								branchTel = Util.null2str(trackingPod.getBranchTel());
								workerName = Util.null2str(trackingPod.getWorkerName());
								workerTel = Util.null2str(trackingPod.getWorkerTel());
								break;
							case "ECM550":
								statusCode = "92";
								statusMessage = "미배송_반품";
								statusEnglishMessage = "Undelivered";
								invoiceType = "RETURN";
								break;
							case "ECM065":
								statusCode = "98";
								statusMessage = "통관부호오류";
								statusEnglishMessage = "Wait Customs";
								
								branchName = "허브넷관세사(0264168851)";
								branchTel = "02-6416-8851";
								workerName = "허브넷";
								workerTel = "02-6416-8851";
								break;
						}
						
						//통관시작(11)일때 개인통관부호검증 오류시 전송안함
						if("11".equals(statusCode) && "N".equals(Util.null2str(trackingPod.getCustomsChk()))) {
							continue;
						}
						
						String reasonCode = "";
						String reasonMessage = "";
						if("ECM400".equals(podStatus)) {
							switch(remarks) {
								case "01":
									reasonCode = "01";
									reasonMessage = "Receipt denied (수취거부)";
									break;
								case "05":
									reasonCode = "05";
									reasonMessage = "Incorrect address (수하인 주소 부정확)";
									break;
								case "06":
									reasonCode = "06";
									reasonMessage = "Receipt absence (수하인 부재)";
									break;
								case "07":
									reasonCode = "07";
									reasonMessage = "Customs payment rejected (관세지불 거절)";
									break;
							}
						}else if("ECM420".equals(podStatus)) {
							switch(remarks) {
								case "01":
									reasonCode = "01";
									reasonMessage = "Myself (본인)";
									break;
								case "05":
									reasonCode = "05";
									reasonMessage = "Security Office (경비실)";
									break;
								case "06":
									reasonCode = "06";
									reasonMessage = "Front door (문앞)";
									break;
								case "99":
									reasonCode = "99";
									reasonMessage = "ETC (기타)";
									break;
							}
						}
						
						String invoiceNo = "RETURN".equals(invoiceType) ? Util.null2str(trackingPod.getDeliveryNo()) : Util.null2str(trackingPod.getHblNo());
						//statusAt
						String strDate = trackingPod.getPodDate()+" "+trackingPod.getPodTime();
						SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
						SimpleDateFormat newDtFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
						Date formatDate = dtFormat.parse(strDate);
						String statusAt = newDtFormat.format(formatDate);
						
						podObj.put("courierCode", trackingPod.getCourierCode());
						podObj.put("vendorTypeCode", "COUPANG_GLOBAL");
						podObj.put("invoiceType", invoiceType);
						podObj.put("orderId", trackingPod.getPoNo());
						podObj.put("invoiceNumber", invoiceNo);
						podObj.put("uniqueKey", "RETURN".equals(invoiceType) ? trackingPod.getReturnRequestId() : trackingPod.getShipmentBoxId());
						podObj.put("statusCode", statusCode);
						podObj.put("statusMessage", statusMessage);
						podObj.put("statusEnglishMessage", statusEnglishMessage);
						podObj.put("reasonCode", reasonCode);
						podObj.put("reasonMessage", reasonMessage);
						podObj.put("branchName", branchName);
						podObj.put("branchTel", branchTel);
						podObj.put("workerName", workerName);
						podObj.put("workerTel", workerTel);
						podObj.put("statusAt", statusAt);
						if(!("CN".equals(trackingPod.getOrigCountry()) && "AIR".equals(trackingPod.getBound()))) {//중국항공이 아닐때만 sendAt를 보낸다
							podObj.put("sendAt", DateTime.getDate("yyyy-MM-dd HH:mm:ss"));
						}
						podObj.put("comments", trackingPod.getRemarks());
						if("RETURN".equals(invoiceType)) {//반품일때만 원운송장번호를 보냄
							podObj.put("originalInvoiceNumber", trackingPod.getHblNo());
						}
						podObj.put("providerName", "HUBNET");
						podObj.put("attachmentPath", "");
						jsonArray.add(podObj);
						podObjMap.put(trackingPod.getTrackingJobNo(), podObj);
					}
					
					Map<String,Object> paramMap = new HashMap<String, Object>();
					paramMap.put("entCode", entCode);
					//paramMap.put("deliveryCode", custCode);
					paramMap.put("deliveryCode", "COUPANG-CN");
					Map<String, Object> apiMap = trackingOrderMapper.selectDeliveryApiInfo(paramMap);
					
					if(apiMap != null) {
						String sendUrl = Util.null2str(apiMap.get("RECV_API_URL"));
						String sendPath = Util.null2str(apiMap.get("TRACKING_API_URL"));
						String accessKey = Util.null2str(apiMap.get("RECV_API_ID"));
						String secretKey = Util.null2str(apiMap.get("RECV_API_PW"));
						String signature = HmacGenerator.generate("POST", sendPath, accessKey, secretKey);
						
						log.info("url??"+(sendUrl+sendPath));
						
						BufferedReader in = null;
						URL url = new URL(sendUrl+sendPath);
						HttpURLConnection conn = (HttpURLConnection) url.openConnection();

						// API HEADER
						conn.setRequestProperty("Authorization", signature);
						conn.setRequestProperty("Content-Type", "application/json");
						conn.setRequestMethod("POST");
						conn.setReadTimeout(18000);
						conn.setDoOutput(true);
						conn.setDoInput(true);
						conn.getOutputStream().write(jsonArray.toString().getBytes("UTF-8")); // POST 호출
						
						log.info("[" + Util.getNow("yyyy-MM-dd HH:mm:ss") + "][TrackingParserProcess] signature : " + signature);
						log.info("[" + Util.getNow("yyyy-MM-dd HH:mm:ss") + "][TrackingParserProcess] request : " + jsonArray);

						// 연결 설정
						conn.connect();
						int responseCode = conn.getResponseCode();
						
				        if (responseCode == 200) in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));	// HTTP OK
				        else in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));						// HTTP ERROR

				        String inputLine;
						StringBuilder response = new StringBuilder();
						ObjectMapper jsonToMap = new ObjectMapper(); // json To Map 변환용

						while((inputLine = in.readLine()) != null) { // response 출력
							response.append(inputLine);
						}
						in.close();

						log.debug("check tracking send response -> " + response.toString());
						returnMap = jsonToMap.readValue(Util.null2str(response), Map.class);

						if(responseCode == 200) {
							try {
								sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH);
								status = transactionManager.getTransaction(new DefaultTransactionDefinition());

								TrackingOrderMapper mapper = sqlSession.getMapper(TrackingOrderMapper.class);
								for(int i=0; i<trackingList.size();) {
									TrackingPod tracking = trackingList.get(i++);
									indexName = "ecm-tracking-pod-logs-" + Util.null2str(tracking.getOdJobNo()).toLowerCase();
									if("SUCCESS".equals(Util.null2str(returnMap.get("resultCode")))){
										Map<String,Object> srchMap = new HashMap<String, Object>();
										srchMap.put("trackingJobNo", tracking.getTrackingJobNo());
										srchMap.put("podStatus", tracking.getPodStatus());
										srchMap.put("status", "S");
										mapper.updateTracking(srchMap);

										if (i % 100 == 0 || i == trackingList.size()) {
											sqlSession.flushStatements();
											sqlSession.clearCache();
										}
									}
									
									logId = Util.null2str(tracking.getHblNo());
									
									// 처리결과에 따른 로그 기록
							        tmpMap.clear();
									tmpMap.put("ent_code", entCode); 		 				// 도입사코드
									tmpMap.put("register_user_id", search.getUserId());
									tmpMap.put("register_user_name", search.getUserId());
									tmpMap.put("register_date_time", System.currentTimeMillis());
									tmpMap.put("es_job_no", logId);
									
									List<Map<String, Object>> jobLogList = new ArrayList<Map<String, Object>>();
									Map<String, Object> jobLogMap = new HashMap<String, Object>();
									jobLogMap.put("recv_date", DateTime.getDate("yyyy-MM-dd HH:mm:ss"));
									jobLogMap.put("recv_data", tracking);

									indexApi = new IndexApi(client);
									if (!indexApi.existIndex(indexName))	indexApi.createIndex(indexName);
									documentApi = new DocumentApi(client);
									
									GetResponse getResponse = documentApi.getDocument(indexName, logId);
									
									if(getResponse == null) {
										jobLogList.add(jobLogMap);
										tmpMap.put("file_data", new Gson().toJson(jobLogList));
										documentApi.createDocument(indexName, logId, tmpMap);
									}else {
										tmpMap.clear();
										
										ObjectMapper objectMapper = new ObjectMapper();
										objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
										objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
										objectMapper.setSerializationInclusion(Include.NON_NULL);
										
										ProcessLogs processLogs = objectMapper.convertValue(getResponse.getSourceAsMap(), ProcessLogs.class);
										String jobLog = processLogs.getFileData();
										jobLogList = objectMapper.readValue(jobLog, new TypeReference<List<Map<String, Object>>>() {});
										jobLogList.add(jobLogMap);
							            
										tmpMap.put("file_data", new Gson().toJson(jobLogList));
										tmpMap.put("update_user_id", search.getUserId());
										tmpMap.put("update_user_name", search.getUserId());
										tmpMap.put("update_date_time", System.currentTimeMillis());
										
										documentApi.updateDocument(indexName, logId, tmpMap);
									}
								}
								
								if_status_info = "Send Complete";
								transactionManager.commit(status);
							}catch(Exception e) {
								if_status_info = "Exception";
								if(transactionManager != null)	transactionManager.rollback(status);
								log.error(Util.getPrintStacTraceString(e));
								throw e;
							}
						}else {
							if_status_info = "tracking request fail";
							log.debug("tracking request fail ->" + jsonArray.toString());
						}
						
						if(sqlSession != null)	sqlSession.close();
					}
					
					for(TrackingPod tracking : trackingList) {
						// history 등록 
						tmpMap = new HashMap<String, Object>();
						logId = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
						index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
						String if_status = Util.null2str(returnMap.get("resultCode"), Util.null2str(returnMap.get("code")));
						String if_remark = Util.null2str(returnMap.get("resultMessage"), Util.null2str(returnMap.get("message")));

				    	// 처리결과에 따른 로그 기록
				        tmpMap.clear();
						tmpMap.put("ent_code", entCode); 		 	// 도입사코드
						tmpMap.put("customer_code", "COUPANG"); 		 			// 고객코드
						tmpMap.put("solution", "ECM");      		 				// SOLUTION
						tmpMap.put("bound", tracking.getBound());		 	 		// 바운드 (AIR)
						tmpMap.put("bound_type", tracking.getBoundType());	 		// 바운드타입
						tmpMap.put("hbl_no", tracking.getHblNo());				 	// HBL_NO
						tmpMap.put("if_service_code", tracking.getBound());
						tmpMap.put("if_doc_code", "COUPANG_TRACKING_SEND");
						tmpMap.put("if_doc_name", "쿠팡 Tracking 전송");
						tmpMap.put("if_doc_reference_no", "");	 					// 문서번호 
						tmpMap.put("if_vender", "COUPANG");							// 중개사업자
						tmpMap.put("if_send_recv_type", "S");						// 송수신타입(S:SEND, R:RECV)
						tmpMap.put("if_send_recv_method", "JSON To JSON");			// 송수신방법
						tmpMap.put("if_file_path", "");			 					// 파일경로
						tmpMap.put("if_file_name", "");			 					// 파일명
						tmpMap.put("if_status", if_status);			 				// 처리상태
						tmpMap.put("if_status_info", if_status_info);		 		// 송수신상태_설명
						tmpMap.put("if_sender_id", "COUPANG");							// 송신식별자
						tmpMap.put("if_sender_name", "");		 					// 송신자명
						tmpMap.put("if_receiver_id", "");		 					// 수신식별자
						tmpMap.put("if_receiver_name", "");	     					// 수신자명
						tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));		// DATE
						tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));	// DATE_TIME
						tmpMap.put("if_remark", if_remark);			 				// 비고( 에러로그 등)
						tmpMap.put("register_user_id", search.getUserId());
						tmpMap.put("register_user_name", search.getUserId());
						tmpMap.put("register_date_time", System.currentTimeMillis());
						tmpMap.put("es_job_no", logId);
						tmpMap.put("file_data", new Gson().toJson(podObjMap.get(tracking.getTrackingJobNo())));
						
						indexApi = new IndexApi(client);
						if (!indexApi.existIndex(index))	indexApi.createIndex(index);
						documentApi = new DocumentApi(client);
						documentApi.createDocument(index, logId, tmpMap);
					}
				}
			}
		}catch (Exception e) {
			//예외 메일 전송
			log.error(Util.getPrintStacTraceString(e));
		} finally {
			log.info("=========================== TrackingPodInfoSendExecutor : " + search.getSrchEntCode() + " END==============");
		}
	
	}
	
}


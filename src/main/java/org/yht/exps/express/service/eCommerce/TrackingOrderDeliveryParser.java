package org.yht.exps.express.service.eCommerce;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
//import org.yht.CatchMailSend;
//import org.yht.config.BranchContextHolder;
import org.yht.constant.BranchEnum;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.eCommerce.PcccMapper;
import org.yht.util.DateTime;
import org.yht.util.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class TrackingOrderDeliveryParser {


//	@Value("${custom.elasticsearch-scroll-page}")
//	private int elasticsearchScrollPage_;
//
//	@Value("${custom.unipass-thread-count}")
//	private int unipassThreadCount_;

	@Autowired
	RestHighLevelClient client;

//	@Autowired
//	CatchMailSend catchMailSend;

	@Autowired
	PcccMapper pcccMapper;

	@Autowired
	SqlSessionTemplate sqlSessionTemplate;

	@Autowired
	PlatformTransactionManager transactionManager;
	private int batchInsertSize_ = 80;
	
	/**
	 * 경동택배 트래킹 API
	 * @param dbEntCode 
	 * @param data 
	 */
	public long trackingProcessKD(Map<String, Object> data, String dbEntCode) {
		long chkErr = 0;
		
		// 오더 단건 일때
		// 변경배송사가 있다는건 멀티화물이 1개 생성되면서 멀티화물에 배송사코드가 존재하면 변경배송사로 인식. => 멀티 트래킹으로 진행됨
		
		// 멀티 일때
		// 변경배송사가 있으면 변경배송사로 트래킹.
		if("multi".equals(Util.null2str(data.get("multi_chk")))) {
			data.put("finalHblNo", "".equals(Util.null2str(data.get("deliveryNoChanged"))) ? Util.null2str(data.get("deliveryNo")) : Util.null2str(data.get("deliveryNoChanged")));
		}else {
			data.put("finalHblNo", data.get("hblNo").toString());
		}
		
		String entCode = dbEntCode;
		String hblNo = Util.null2str(data.get("finalHblNo"));
		String multiFlag = Util.null2str(data.get("multi_chk"));
		String poNo = Util.null2str(data.get("poNo"));
		Map<String, Object> deliveryApiMap = new HashMap<String, Object>();
		
		HttpURLConnection conn = null;
		
		try {
			
			List<Map<String,Object>> podList = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> podMultiList = new ArrayList<Map<String,Object>>();
			List<Map<String,Object>> pod4List = new ArrayList<Map<String,Object>>();
			Map<String,Object> deliPodMap = new HashMap<String, Object>();
			boolean isEnd = false;
			boolean deliveryEnd = false;
			BufferedReader in = null;
			String kdUrl = "";
			String deliveryDate = "";
			// 경동 트래킹 API 정보 조회
			Map<String,Object> srchDeliveryApiMap = new HashMap<String, Object>();
			srchDeliveryApiMap.put("entCode", entCode);
			srchDeliveryApiMap.put("deliveryCode", "KD_TRACKING");
			deliveryApiMap = pcccMapper.selectDeliveryApiInfo(srchDeliveryApiMap);

			//세팅정보
			kdUrl = Util.null2str(deliveryApiMap.get("RECV_API_URL"));
			String kdSecretKey = Util.null2str(deliveryApiMap.get("RECV_API_PW"));

			URL url = new URL(kdUrl + "/" + hblNo);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setReadTimeout(18000);
			conn.setConnectTimeout(13000);
			conn.setRequestProperty("X-AUTH-SECRET-KEY", kdSecretKey); // ← Secret Key 헤더 추가
			conn.setRequestProperty("Accept", "application/json");

			int responseCode = conn.getResponseCode();
			if (responseCode == 200) in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			else in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = in.readLine()) != null) {
				response.append(line);
			}
			in.close();
			
			if (responseCode == 200) {
				
				JSONParser jsonParser = null;
				JSONObject cnIdInfo = null;
				List<Map<String, Object>> lastPodlist = new ArrayList<Map<String, Object>>();
				Map<String, Object> resultMap = new HashMap<String, Object>();
				boolean jsonFlag = true;
				
				try {
					jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
					cnIdInfo = (JSONObject) jsonParser.parse(response.toString());
				} catch (ParseException e) {
					jsonFlag = false;
					log.error("[" + hblNo + "] KD ParseException !!!!!!!!" + response.toString());
				}
				
				if (jsonFlag) {
					
					//  result 필드가 "Y"인지 확인 (기존 "success" → "Y"로 변경)
					if ("Y".equals(cnIdInfo.get("result"))) {
						JSONObject dataObj = (JSONObject) cnIdInfo.get("data");
						JSONArray scanList = (JSONArray) dataObj.get("scanList");
						String scanTypePlus = Util.null2str((JSONObject)dataObj.get("scanType"));
						for (int i = 0; i < scanList.size(); i++) {
							String podDate = "";
							String podTime = "";
							JSONObject obj = (JSONObject) scanList.get(i);

							//  infoContent → scanType으로 변경
							String scanType = Util.null2str(obj.get("scanType"));

							//  날짜/시간 파싱: "2026-02-24 10:19:28.949893" 형식
							String[] dateArr = Util.null2str(obj.get("scanDt")).split(" ");
							if (dateArr != null && dateArr.length >= 2) {
								podDate = dateArr[0];
								String[] hourArr = dateArr[1].split(":");
								podTime = hourArr[0] + ":" + hourArr[1];
							}
							
							resultMap.put("podDate", podDate);
							resultMap.put("podTime", podTime);
							resultMap.put("hblno", hblNo);
//							resultMap.put("remarks", remarks);
							resultMap.put("remarks", "99");
							resultMap.put("plusRemark","");
							
							switch (scanType) {
							case "0002": // 발송 ->집하처리
								resultMap.put("podStatus", "ECM361");
								lastPodlist.add(resultMap);
								podMultiList.add(resultMap);
								deliveryEnd = true;//배송완료플래그업데이트(g1은 DELIVERY_STATUS가 전부 바껴야되므로 모든상태에 true)
								pod4List.add(resultMap); // TB_ECM_POD_01
								break;
//							case "0003": // 터미널 -> 집하처리
//								resultMap.put("podStatus", "ECM361");
//								lastPodlist.add(resultMap);
//								pod4List.add(resultMap); // TB_ECM_POD_04
//								break;
//							case "0004": // 노선 -> 간선상차
//								resultMap.put("podStatus", "ECM370");
//								lastPodlist.add(resultMap);
//								pod4List.add(resultMap);
//								break;
//							case "0006": // 도착-> 택배보관함 도착
//								resultMap.put("podStatus", "ECM421");
//								lastPodlist.add(resultMap);
//								pod4List.add(resultMap);
//								if (deliPodMap == null) {
//									deliPodMap = new HashMap(resultMap);
//								}
//								deliveryEnd = true;
//								break;
							case "0008": // 배송중 -> 배송출발
								resultMap.put("podStatus", "ECM390");
								lastPodlist.add(resultMap);
								podMultiList.add(resultMap);
								deliveryEnd = true;//배송완료플래그업데이트(g1은 DELIVERY_STATUS가 전부 바껴야되므로 모든상태에 true)
								pod4List.add(resultMap);
								break;
							case "0007": // 인수자완료 -> 배송완료
									switch (scanTypePlus) {
										case "0001": // 본인
											resultMap.put("remarks","01");
											break;
										case "0004": // 경비실
											resultMap.put("remarks","05");
											break;
										case "0005": // 문앞
											resultMap.put("remarks","06");
											break;
										case "0002": // 가족
										case "0003": // 동료
										case "0006": // 택배보관함
										case "0007": // 영업소출고
										case "0008": // 기타
										case "0009": // 배송완료
										default:
											resultMap.put("remarks","99");
											break;
									}
								resultMap.put("podStatus", "ECM420");
								lastPodlist.add(resultMap);
								podMultiList.add(resultMap);
								pod4List.add(resultMap);
								isEnd = true;
								deliPodMap = new HashMap(resultMap);//배송완료플래그업데이트
								deliveryEnd = true;//배송완료플래그업데이트(g1은 DELIVERY_STATUS가 전부 바껴야되므로 모든상태에 true)
								deliveryDate = resultMap.get("podDate").toString() + " "
										+ resultMap.get("podTime").toString() + ":00.000";//배송완료시간이 있으면 배송완료 플래그도 바뀜
								break;
							}
						}
					} else {
						// result = "N" 인 경우
						log.warn("[" + hblNo + "] KD tracking 조회 결과 없음 : " + cnIdInfo.get("message"));
					}
					
					//멀티일경우는 가장 첫번째데이터를 기준으로 오더에 같이 최신화해줌
					if(multiFlag.equals("multi")){
						processPodList(data, podMultiList,  "TB_ECM_POD_04_M", Arrays.asList("ECM361","ECM390","ECM420"));
						//배송완료일경우 배송완료시간 업데이트(멀티)
						if(isEnd)
						{
							Map<String,Object>multiUpdateMap = new HashMap<>();
							multiUpdateMap.put("odJobNo", data.get("odjobno"));
							multiUpdateMap.put("statusType", "DELIVERY_END");
							multiUpdateMap.put("deliveryNo", hblNo);
							multiUpdateMap.put("podDate",deliveryDate);
							pcccMapper.updateOdMultiDate(multiUpdateMap);
						}
						//멀티의 시퀀스1 -> 오더정보
						if(Util.null2str(data.get("seqNoId")).equals("1")){
							if (deliveryEnd) {
								if (lastPodlist != null && lastPodlist.size() > 0) {
									Map<String, Object> newMap = lastPodlist.get(0);
									newMap.put("odJobNo", data.get("odjobno"));
									newMap.put("saveStatus", Util.null2str(newMap.get("podStatus")));
									if (!Util.null2str(newMap.get("podStatus")).equals("ECM400") && !Util.null2str(newMap.get("podStatus")).equals("ECM420")) {
										newMap.put("notSaveLastStatusYn", "Y");
									}
									pcccMapper.updateOrderLastPodStatus(newMap);
									deliPodMap.put("saveStatus",  Util.null2str(newMap.get("podStatus")));
									deliPodMap.put("deliveryDate", deliveryDate);
									deliPodMap.put("odJobNo", data.get("odjobno"));
									pcccMapper.updateOrderDeliveryInfo(deliPodMap);

									//오더테이블 시간업데이트
									if(isEnd) {
										Map<String, Object> updateOdMap = new HashMap<>();
										updateOdMap.put("odJobNo", data.get("odjobno"));
										updateOdMap.put("statusType", "DELIVERY_END");
										updateOdMap.put("podDate", deliveryDate);
										pcccMapper.updateOdDate(updateOdMap);
									}

								}
							}
							processPodList(data, pod4List, "TB_ECM_POD_04", Arrays.asList("ECM361","ECM390","ECM420"));
						}
					}else {
						if(isEnd) {
							Map<String,Object>multiUpdateMap = new HashMap<>();
							multiUpdateMap.put("odJobNo", data.get("odjobno"));
							multiUpdateMap.put("statusType", "DELIVERY_END");
							multiUpdateMap.put("deliveryNo", hblNo);
							multiUpdateMap.put("chkSeq", "1");
							multiUpdateMap.put("podDate",deliveryDate);
							pcccMapper.updateOdMultiDate(multiUpdateMap);
						}
						//배송완료건 멀티일경우 완료시간 업데이트
						if (lastPodlist != null && lastPodlist.size() > 0) {
							Map<String, Object> newMap = lastPodlist.get(0);
							newMap.put("odJobNo", data.get("odjobno"));
							newMap.put("saveStatus", Util.null2str(newMap.get("podStatus")));
							if (!Util.null2str(newMap.get("podStatus")).equals("ECM400") && !Util.null2str(newMap.get("podStatus")).equals("ECM420")) {
								newMap.put("notSaveLastStatusYn", "Y");
							}
							pcccMapper.updateOrderLastPodStatus(newMap);
							if (deliveryEnd) {
								deliPodMap.put("saveStatus",  Util.null2str(newMap.get("podStatus")));
								deliPodMap.put("deliveryDate", deliveryDate);
								deliPodMap.put("odJobNo", data.get("odjobno"));
								pcccMapper.updateOrderDeliveryInfo(deliPodMap);//배송완료
							}
							processPodList(data, pod4List, "TB_ECM_POD_04", Arrays.asList("ECM361","ECM390","ECM420"));
						}
					}
				}
			} else {
				log.error("KD tracking Request failed.");
			}
		} catch (Exception e) {
			log.error("KD tracking 처리 중 오류 발생 : " + e.getMessage(), e);
		}
		
		return chkErr;
	}


	//대한통운 단건처리 트래킹
	public long trackingProcessKorexPlus(Map<String, Object> data, String dbEntCode) throws Exception {
		
		long chkErr = 0;
		List<Map<String,Object>> podMultiList = new ArrayList<Map<String,Object>>();
		String returnDeliveryNo = Util.null2str(data.get("returnDeliveryNo"));//반품송장번호
		String multiFlag = Util.null2str(data.get("multi_chk"));
		
		String prodCode = "";
		boolean returnFlag = false;//반품건인지 체크하는 플레그
		//반품번호가 존재할경우 원송장번호가 아닌 반품송장번호로 보냄
		if (returnDeliveryNo.equals("")){
			 prodCode =  Util.null2str(data.get("deliveryNo")).equals("") 
					 ? Util.null2str(data.get("hblno")) : Util.null2str(data.get("deliveryNo"));
		}else {
			 prodCode = returnDeliveryNo;
			 returnFlag =true;
		}
		
		String entCode = (String) data.get("entCode");
		String hblNo = Util.null2str(data.get("hblno"));
		String poNo = Util.null2str(data.get("poNo"));
		Map<String, Object> deliveryApiMap = new HashMap<String, Object>();
		Map<String, Object> deliveryApiAllMap = new HashMap<String, Object>();
		log.info("[" + poNo + "] Start Korex Tracking ******************************");

		HttpURLConnection conn = null;
		try {
			
			List<Map<String, Object>> podList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> pod4List = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> pod4ReturnList = new ArrayList<Map<String, Object>>();
			Map<String, Object> deliPodMap = new HashMap<String, Object>();
			boolean isEnd = false;
			boolean deliveryEnd = false;
			BufferedReader in = null;
			String deliveryDate = "";
			//  대한통운 API 접속 정보 조회
			Map<String, Object> srchDeliveryApiMap = new HashMap<String, Object>();
			srchDeliveryApiMap.put("entCode", entCode);
			srchDeliveryApiMap.put("deliveryCode", "KOREX_TRACKING_SINGLE");
			srchDeliveryApiMap.put("srchCustomerCode", Util.null2str(data.get("CUST_CODE_LV1")));//od의 CUST_CODE_LV1
			srchDeliveryApiMap.put("srchBound", Util.null2str(data.get("BOUND")));
			srchDeliveryApiMap.put("srchLogisticsCode", Util.null2str(data.get("DEST_PORT")));
			//두가지사용
			deliveryApiAllMap = pcccMapper.selectDeliveryApiInfoAllMapping(srchDeliveryApiMap);//사업자번호,업체코드
			//없을경우 COMMON적용
			if(deliveryApiAllMap == null) {
				deliveryApiAllMap = pcccMapper.selectDeliveryApiInfoAll(srchDeliveryApiMap);//사업자번호,업체코드
			}
			deliveryApiMap = pcccMapper.selectDeliveryApiInfo(srchDeliveryApiMap);// 중개업체id,url
			
			if(deliveryApiAllMap == null) {
				return chkErr;
			}

			//POST 요청 본문(JSON) 구성
			JSONObject requestBody = new JSONObject();
			JSONObject requestData = new JSONObject();
			requestData.put("TOKEN_NUM", Util.null2str(deliveryApiAllMap.get("KOREX_TOKEN_NUM")));//토큰
			requestData.put("CLNTNUM",   Util.null2str(deliveryApiAllMap.get("KOREX_ID")));// 업체코드
			requestData.put("USER_ID",   Util.null2str(deliveryApiMap.get("RECV_API_ID")));// 중개업체id
			requestData.put("INVC_NO",   prodCode);//송장번호
			requestBody.put("DATA", requestData);
			byte[] postBytes = requestBody.toJSONString().getBytes("UTF-8");

			//HTTP 연결
			URL url = new URL(Util.null2str(deliveryApiMap.get("RECV_API_URL")));
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setReadTimeout(18000);
			conn.setConnectTimeout(13000);
			conn.setDoOutput(true);
			conn.setRequestProperty("CJ-Gateway-APIKey", Util.null2str(deliveryApiAllMap.get("KOREX_TOKEN_NUM")));
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setRequestProperty("Accept",       "application/json");

			try (OutputStream os = conn.getOutputStream()) {
				os.write(postBytes);
			}

			int responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			} else {
				in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
			}

			String line;
			StringBuffer response = new StringBuffer();
			while ((line = in.readLine()) != null) {
				response.append(line);
			}
			in.close();
			
			if (responseCode == 200) {
				
				JSONParser jsonParser = null;
				JSONObject cnIdInfo   = null;
				List<Map<String, Object>> lastPodlist = new ArrayList<Map<String, Object>>();
				boolean jsonFlag = true;

				try {
					jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
					cnIdInfo  = (JSONObject) jsonParser.parse(response.toString());
				} catch (ParseException e) {
					jsonFlag = false;
					log.error("[" + hblNo + "] Korex ParseException !! " + response.toString());
				}
				
				if (jsonFlag) {
					log.info("[" + hblNo + "] Korex tracking Response : " + cnIdInfo);

					// 결과코드 확인:S = 성공
					if ("S".equals(cnIdInfo.get("RESULT_CD"))) {

						JSONArray dataArr = (JSONArray) cnIdInfo.get("DATA");

						for (int i = 0; i < dataArr.size(); i++) {

							Map<String, Object> resultMap = new HashMap<String, Object>();
							JSONObject obj = (JSONObject) dataArr.get(i);

							//필드 파싱
							String crgSt      = Util.null2str(obj.get("CRG_ST"));      // 화물상태 코드
							//String crgStNm    = Util.null2str(obj.get("CRG_ST_NM"));   // 화물상태 명
							String scanYmd    = Util.null2str(obj.get("SCAN_YMD"));    // 스캔 일자 (yyyy-MM-dd)
							String scanHour   = Util.null2str(obj.get("SCAN_HOUR"));   // 스캔 시간 (HH:mm:ss)
							String branchNm   = Util.null2str(obj.get("DEALT_BRAN_NM")); // 처리점소 명
							String courierName =  Util.null2str(obj.get("DEALT_EMP_NM"));//처리사원 명
							String courierTel = Util.null2str(obj.get("DEALT_EMP_NM")); //처리사원 전화
							String acptrNm    = Util.null2str(obj.get("ACPTR_NM"));    // 인수자명

							// 날짜/시간 정제
							String podDate = scanYmd; // "yyyy-MM-dd"
							String podTime = "";
							if (scanHour.length() >= 5) {
								podTime = scanHour.substring(0, 5); // "HH:mm"
							}

							// remarks: 처리점소명 200자 제한
							String remarks = branchNm.length() > 200 ? branchNm.substring(0, 200) : branchNm;
							resultMap.put("blNo",   obj.get("INVC_NO"));
							resultMap.put("remarks",      remarks);
							resultMap.put("podDate",      podDate);
							resultMap.put("podTime",      podTime);
							resultMap.put("deliveryStoreName", branchNm);
							resultMap.put("recipientName",     acptrNm);
							resultMap.put("courierName",       courierName);
							resultMap.put("courierTel",        courierTel);
							resultMap.put("deliveryIrreCode",  "");         //  (단건은 없음)


							// ECM 상태코드 매핑
							// 11: 집하      → ECM361
							// 21: 간선상차  → ECM370
							// 31: 배송출발  → ECM390
							// 82: 미배달    → ECM419 (반송/미배달 보류)
							// 91: 배송완료  → ECM420
							switch (crgSt) {
								case "01": // 집하지시
									if(returnFlag==false) {
										resultMap.put("podStatus", "ECM360");
									}else {
										resultMap.put("podStatus", "ECM510");
									}
									lastPodlist.add(resultMap);
									podMultiList.add(resultMap);
									pod4List.add(resultMap);
									pod4ReturnList.add(resultMap);
									deliveryEnd = true;
									break;
								case "11": // 집하처리
									if(returnFlag==false) {
										resultMap.put("podStatus", "ECM361");
									}else {
										resultMap.put("podStatus", "ECM520");
									}
									lastPodlist.add(resultMap);
									podMultiList.add(resultMap);
									pod4List.add(resultMap);
									pod4ReturnList.add(resultMap);
									deliveryEnd = true;
									break;
								case "12": // 미집하
									resultMap.put("podStatus", "ECM350");
									lastPodlist.add(resultMap);
									podMultiList.add(resultMap);
									pod4List.add(resultMap);
									deliveryEnd = true;
									break;
								case "41": // 간선상차
									resultMap.put("podStatus", "ECM370");
									lastPodlist.add(resultMap);
									podMultiList.add(resultMap);
									pod4List.add(resultMap);
									deliveryEnd = true;
									break;
								case "42": // 간선하차
									resultMap.put("podStatus", "ECM380");
									lastPodlist.add(resultMap);
									podMultiList.add(resultMap);
									pod4List.add(resultMap);
									deliveryEnd = true;
									break;
								case "82": // 배송출발
									if(returnFlag==false){
										resultMap.put("podStatus", "ECM390");
									}else {
										resultMap.put("podStatus", "ECM530");
									}
									lastPodlist.add(resultMap);
									podMultiList.add(resultMap);
									pod4List.add(resultMap);
									pod4ReturnList.add(resultMap);
									deliveryEnd = true;
									break;
								case "84": // 미배달
									if(returnFlag==false){
										resultMap.put("podStatus", "ECM400");
									}else {
										resultMap.put("podStatus", "ECM550");
									}
									lastPodlist.add(resultMap);
									podMultiList.add(resultMap);
									pod4List.add(resultMap);
									pod4ReturnList.add(resultMap);
									deliveryEnd = true;
									break;
								case "91": // 배송완료
									if(returnFlag==false){
											resultMap.put("podStatus", "ECM420");
										}else {
											resultMap.put("podStatus", "ECM540");
										}
									lastPodlist.add(resultMap);
									podMultiList.add(resultMap);
									pod4List.add(resultMap);
									pod4ReturnList.add(resultMap);
									isEnd       = true;
									deliPodMap  = new HashMap<>(resultMap);
									deliveryDate = deliPodMap.get("podDate").toString().replace("-", "")
											+ deliPodMap.get("podTime").toString().replace(":", "");//배송완료시간이 있으면 배송완료 플래그도 바뀜
									deliveryDate = deliPodMap.get("podDate").toString() +" "+deliPodMap.get("podTime").toString();
									deliveryEnd = true;
									break;
								default:
									log.debug("[" + hblNo + "] Korex 미매핑 CRG_ST : " + crgSt);
									break;
							}
						}

					} else {
						// RESULT_CD = "E"
						log.warn("[" + hblNo + "] Korex tracking 조회 실패 : " + cnIdInfo.get("RESULT_DETAIL"));
						
						//조회된 데이터가 없는경우 return처리함
						return chkErr;
					}

					//멀티일경우는 가장 첫번째데이터를 기준으로 오더에 같이 최신화해줌
					if("multi".equals(multiFlag)){
						processPodList(data, podMultiList,  "TB_ECM_POD_04_M", Arrays.asList("ECM060,ECM350", "ECM360", "ECM361", "ECM370", "ECM380", "ECM390", "ECM400", "ECM420", "ECM520", "ECM530", "ECM540"));
						//배송완료일경우 배송완료시간 업데이트(멀티)
						if(isEnd)
						{
							Map<String,Object>multiUpdateMap = new HashMap<>();
							multiUpdateMap.put("odJobNo", data.get("odjobno"));
							multiUpdateMap.put("statusType", returnFlag?"RETURN_END":"DELIVERY_END");//반품완료일경우도 고려
							multiUpdateMap.put("deliveryNo", data.get("deliveryNo"));
							multiUpdateMap.put("podDate",deliveryDate);
							pcccMapper.updateOdMultiDate(multiUpdateMap);
						}
						//멀티의 시퀀스1 -> 오더정보
						if(Util.null2str(data.get("seqNoId")).equals("1")){
							Map<String, Object> newMap = lastPodlist.get(0);
							if (lastPodlist != null && lastPodlist.size() > 0) {
								newMap.put("odJobNo",    data.get("odjobno"));
								newMap.put("saveStatus", Util.null2str(newMap.get("podStatus")));
								if (!Util.null2str(newMap.get("podStatus")).equals("ECM400") && !Util.null2str(newMap.get("podStatus")).equals("ECM420")) {
									newMap.put("notSaveLastStatusYn", "Y");
								}
								pcccMapper.updateOrderLastPodStatus(newMap);
							}
							if (deliveryEnd) {
								deliPodMap.put("saveStatus",  Util.null2str(newMap.get("podStatus")));
								deliPodMap.put("deliveryDate", deliveryDate);
								deliPodMap.put("odJobNo", data.get("odjobno"));
								pcccMapper.updateOrderDeliveryInfo(deliPodMap);
							}
							//오더테이블 시간업데이트
							if(isEnd&& returnFlag) {
								Map<String, Object> updateOdMap = new HashMap<>();
								updateOdMap.put("odJobNo", data.get("odjobno"));
								updateOdMap.put("statusType", "RETURN_END");
								updateOdMap.put("podDate", deliveryDate);
								pcccMapper.updateOdDate(updateOdMap);
							}
								//반품일경우와 구분
							if(returnFlag){
								processPodList(data, pod4ReturnList, "TB_ECM_POD_04", Arrays.asList("ECM540","ECM550","ECM530","ECM520","ECM510"));
							}else {
								processPodList(data, podList,  "TB_ECM_POD_01", Arrays.asList("ECM060"));
								processPodList(data, pod4List, "TB_ECM_POD_04", Arrays.asList("ECM350", "ECM360", "ECM361", "ECM370", "ECM380", "ECM390", "ECM400", "ECM420"));

							}
						}
					}else {

						Map<String, Object> newMap = lastPodlist.get(0);
						if (lastPodlist != null && lastPodlist.size() > 0) {
							newMap.put("odJobNo",    data.get("odjobno"));
							newMap.put("saveStatus", Util.null2str(newMap.get("podStatus")));
							if (!Util.null2str(newMap.get("podStatus")).equals("ECM400") && !Util.null2str(newMap.get("podStatus")).equals("ECM420")) {
								newMap.put("notSaveLastStatusYn", "Y");
							}
							pcccMapper.updateOrderLastPodStatus(newMap);
						}
						if (deliveryEnd) {
							deliPodMap.put("saveStatus",  Util.null2str(newMap.get("podStatus")));
							deliPodMap.put("deliveryDate", deliveryDate);
							deliPodMap.put("odJobNo", data.get("odjobno"));
							pcccMapper.updateOrderDeliveryInfo(deliPodMap);
						}
						//오더테이블 시간업데이트
						if(isEnd&&returnFlag) {
							Map<String, Object> updateOdMap = new HashMap<>();
							updateOdMap.put("odJobNo", data.get("odjobno"));
							updateOdMap.put("statusType", "RETURN_END");
							updateOdMap.put("podDate", deliveryDate);
							pcccMapper.updateOdDate(updateOdMap);
						}
						//반품일경우
						if(returnFlag){
							processPodList(data, pod4ReturnList, "TB_ECM_POD_04", Arrays.asList("ECM540","ECM550","ECM530","ECM520","ECM510"));
						}else{
							processPodList(data, podList,  "TB_ECM_POD_01", Arrays.asList("ECM060"));
							processPodList(data, pod4List, "TB_ECM_POD_04", Arrays.asList("ECM350", "ECM360", "ECM361", "ECM370", "ECM380", "ECM390", "ECM400", "ECM420"));
						}
					}
				}
			} else {
				log.error("Korex tracking Request failed. Code : " + responseCode + ", Response : " + response.toString());
			}
			
		}catch(Exception e) {
			throw e;
		} 
		
		return chkErr;
	}


	/**
	 * 진행상태 INSERT
	 */
	private void processPodList(Map<String, Object> data, List<Map<String, Object>> podList,String podTableName, List<String> podMultiList) throws Exception {

		if (podList == null || podList.isEmpty()) {
			return;
		}

		//멀티가 있는건인지 아닌지 체크 -> 있는건이면 쿠팡에 이중으로 전송되지않도록 처리해야함
		boolean multiFlag = false;
		Map<String,Object>chkMultMap = new HashMap<>();
		chkMultMap.put("odJobNo",data.get("odjobno"));
		if(pcccMapper.countMulti(chkMultMap)>0) {
			multiFlag = true;
		}

		Map<String, Object> searchMap = new HashMap<>();
		searchMap.put("odjobno", data.get("odjobno"));
		searchMap.put("podTableName", podTableName);
		if (podMultiList != null) {
			searchMap.put("podMultiList", podMultiList);
		}
		if(podTableName.equals("TB_ECM_POD_04_M")){
			searchMap.put("multiChk",true);
			searchMap.put("hblno", data.get("blNo"));
		}

		Map<String, Object> returnMap = pcccMapper.selectOrderPodStatus(searchMap);

		String podStatusStr = "";
		if (returnMap != null) {
			podStatusStr = Util.null2str(returnMap.get("podStatus"));
		}

		// DB에서 가져온 POD_STATUS를 List로 변환
		List<String> dbPodStatusList = new ArrayList<>();
		if (!podStatusStr.isEmpty()) {
			dbPodStatusList = Arrays.asList(podStatusStr.split(","));
		}

		// podList의 POD_STATUS를 List로 변환
		List<String> podListStatusList = new ArrayList<>();
		for (Map<String, Object> pod : podList) {
			podListStatusList.add(Util.null2str(pod.get("podStatus")));
		}

		// 정렬 후 비교
		Collections.sort(dbPodStatusList);
		Collections.sort(podListStatusList);
		log.debug(data.get("odjobno")+" : dbPodStatusList-> " + dbPodStatusList);
		log.debug(data.get("odjobno")+" : podListStatusList-> " + podListStatusList);

		if (!dbPodStatusList.equals(podListStatusList))
		{
			log.debug(data.get("odjobno") +" - > start tracking");
			int seq = 1;

			// 쿠팡에 전송
				for (Map<String, Object> pod : podList) {
					if(pod.get("podStatus").equals("ECM361") || pod.get("podStatus").equals("ECM370") || pod.get("podStatus").equals("ECM380") || pod.get("podStatus").equals("ECM390") || pod.get("podStatus").equals("ECM400")
							|| pod.get("podStatus").equals("ECM420")|| pod.get("podStatus").equals("ECM421")
							|| pod.get("podStatus").equals("ECM504")|| pod.get("podStatus").equals("ECM506")|| pod.get("podStatus").equals("ECM510")
							|| pod.get("podStatus").equals("ECM520")|| pod.get("podStatus").equals("ECM530")
							|| pod.get("podStatus").equals("ECM540")|| pod.get("podStatus").equals("ECM550")
					) {
						
						//멀티가 존재하며 pod4에 입력하는 건은 쿠팡에 전송하지 않음
						if(multiFlag && podTableName.equals("TB_ECM_POD_04")){
							continue;
						}
						Map<String,Object> podInfoMap = new HashMap<String, Object>();
						int podStatusCnt = 0;
						podInfoMap.put("entCode", data.get("entCode"));
						podInfoMap.put("odJobNo", data.get("odjobno"));
						podInfoMap.put("hblno", data.get("hblno"));
						podInfoMap.put("podStatus", pod.get("podStatus"));
						podInfoMap.put("podTableName", podTableName);
						podStatusCnt = pcccMapper.selectPodStatusTrackingCnt(podInfoMap);

						if(podStatusCnt == 0 && Util.null2str(data.get("cust_code_lv1")).equals("COUPANG")) {
							//						resultMap = trackingOrderMapper.selectBrosTrackngOrder(podInfoMap);
							podInfoMap.put("entCode", data.get("entCode"));
							podInfoMap.put("trackingJobNo", DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
							podInfoMap.put("odJobNo", data.get("odjobno"));
							podInfoMap.put("hblNo", data.get("hblno"));
							podInfoMap.put("poNo", Util.null2str(data.get("po_no")));
							podInfoMap.put("deliveryCode", Util.null2str(data.get("delivery_code")));
							podInfoMap.put("podStatus", pod.get("podStatus"));
							podInfoMap.put("podDate", pod.get("podDate"));
							podInfoMap.put("podTime", pod.get("podTime"));
							//						podInfoMap.put("podName", Util.null2str(resultMap.get("POD_NAME")));
							podInfoMap.put("remarks", pod.get("remarks"));
							podInfoMap.put("status","R");
							podInfoMap.put("registerUserId","DAEMON");
							pcccMapper.batchInsertCoupangPodStatus(podInfoMap);
						}
					}
			}


			if (returnMap != null) {
				pcccMapper.batchTrackingDeletePod(searchMap);
			}

			for (Map<String, Object> pod : podList) {
				Map<String, Object> podMap = new HashMap<>();
				podMap.put("registerUserId", "DAEMON");
				podMap.put("podTableName", podTableName);
				podMap.put("podStatus", pod.get("podStatus"));
				podMap.put("entCode",  data.get("entCode"));
				podMap.put("odJobNo", data.get("odjobno"));
//				podMap.put("blNo", data.get("hblno"));
				podMap.put("blNo", pod.get("blNo"));
				podMap.put("podDate", pod.get("podDate"));
				podMap.put("podTime", pod.get("podTime"));
				podMap.put("remarks", pod.get("remarks"));
				//추가필드
				podMap.put("deliveryCode",      data.get("delivery_code"));
				podMap.put("deliveryStoreName", pod.get("deliveryStoreName"));
				podMap.put("deliveryIrreCode",  pod.get("deliveryIrreCode"));
				podMap.put("recipientName",     pod.get("recipientName"));
				podMap.put("courierName",       pod.get("courierName"));
				podMap.put("courierTel",        pod.get("courierTel"));
				try {
					podMap.put("podJobNo", DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				} catch (Exception e) {
					log.error("POD_JOB_NO 채번 중 오류 발생 : " + e.getMessage(), e);
				}

				if (podTableName.equals("TB_ECM_POD_04")) {
					podMap.put("deliveryCode", data.get("delivery_code"));
				}
				pcccMapper.batchInsertPodStatus(podMap);
			}
		}else{
			log.debug(data.get("odjobno")+" -> equalData");
		}
	}



}
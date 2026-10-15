package org.yht.exps.express.controller.edi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.edi.SendDelivery;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.service.edi.CustomsService;
import org.yht.exps.express.service.edi.EdiCommonService;
import org.yht.jwt.JwtTokenProvider;
import org.yht.util.DateTime;
import org.yht.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Slf4j
@RestController
@Api(tags = "EDI")
@RequestMapping("/{entCode}/ecm/edi/customs")
public class CustomsController {
	
	@Resource(name = "customsService")
	CustomsService customsService;
	@Resource(name="ediCommonService")
	EdiCommonService ediCommonService;
	
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	private CustomProperties customProperties;
	@Autowired
	JwtTokenProvider jwtTokenProvider;
	
	
	@ApiOperation(value = "배송지변경신고", notes="배송지변경내용을 송신합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendDeliveryModify", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendDeliveryModify(@PathVariable("entCode") String entCode, @RequestBody SendDelivery sendDelivery, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendDelivery, "registerUserId", "registerUserName", null);
		
		Map<String, Object> enterpriseMap = commonMapper.selectEnterpriseInfo(Util.null2str(sendDelivery.getEntCode()));
		String cusSendType = Util.null2str(enterpriseMap.get("CUS_SEND_TYPE"));
		
		OrderSaveResult orderSaveResult = customsService.setSendDeliveryModifyData(sendDelivery, cusSendType);
		if(Util.null2str(orderSaveResult.getResultCode()).equals("")){
			if(cusSendType.equals("REST")){
				if(!ediCommonService.isOnline(customProperties.getEbmsIp(), Util.null2int(customProperties.getEbmsPort()))) {
					orderSaveResult.setResultCode("ebmsRestConnectionError");
					return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
				}
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						try{
							customsService.sendDeliveryModify(sendDelivery, cusSendType, orderSaveResult);
						}catch(Exception e){
							try{
								ediCommonService.asyncErrorSendEmail("CustomerController.sendDeliveryModify", BeanUtils.describe(sendDelivery), e);
							}catch(Exception ee){
								log.error(Util.getPrintStacTraceString(ee));
							}
						}
					}
				});
				thread.start();
				
				orderSaveResult.setResultCode("ebmsRest");
			}else{
				customsService.sendDeliveryModify(sendDelivery, cusSendType, orderSaveResult);
			}
		}
		
		return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
	}
	
	@ApiOperation(value = "별도관리물품 해제 신청서", notes="별도관리물품 해제 신청서를 송신합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendUnmanagementItem", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendUnmanagementItem(@PathVariable("entCode") String entCode, @RequestBody SendDelivery sendDelivery, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendDelivery, "registerUserId", "registerUserName", null);
		Map<String, Object> enterpriseMap = commonMapper.selectEnterpriseInfo(Util.null2str(sendDelivery.getEntCode()));
		String cusSendType = Util.null2str(enterpriseMap.get("CUS_SEND_TYPE"));
		
		OrderSaveResult orderSaveResult = customsService.sendUnmanagementItemData(sendDelivery, cusSendType);
		if(Util.null2str(orderSaveResult.getResultCode()).equals("")){
			if(cusSendType.equals("REST")){
				if(!ediCommonService.isOnline(customProperties.getEbmsIp(), Util.null2int(customProperties.getEbmsPort()))) {
					orderSaveResult.setResultCode("ebmsRestConnectionError");
					return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
				}
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						try{
							customsService.sendUnmanagementItem(sendDelivery, cusSendType, orderSaveResult);
						}catch(Exception e){
							try{
								ediCommonService.asyncErrorSendEmail("CustomsController.sendUnmanagementItem", BeanUtils.describe(sendDelivery), e);
							}catch(Exception ee){
								log.error(Util.getPrintStacTraceString(ee));
							}
						}
					}
				});
				thread.start();
				
				orderSaveResult.setResultCode("ebmsRest");
			}else{
				customsService.sendUnmanagementItem(sendDelivery, cusSendType, orderSaveResult);
			}
		}
		
		return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
	}
	@ApiOperation(value = "하기결과이상보고", notes="하기결과이상보고를 송신합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendAbnormalitiesReport", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendAbnormalitiesReport(@PathVariable("entCode") String entCode, @RequestBody SendDelivery sendDelivery, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendDelivery, "registerUserId", "registerUserName", null);
		return new ResponseEntity<OrderSaveResult>(customsService.sendAbnormalitiesReport(sendDelivery, request), HttpStatus.OK);
	}
	
	@ApiOperation(value = "수출이행신고", notes="수출이행신고를 송신합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendExportFulfillmentNotification", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendExportFulfillmentNotification(@PathVariable("entCode") String entCode, @RequestBody SendDelivery sendDelivery, HttpServletRequest request) throws Exception {
		Util.getSessionUserInfo(request, jwtTokenProvider, sendDelivery, "updateUserId", "registerUserName", null);
		sendDelivery.setRegisterUserId(sendDelivery.getUpdateUserId());
		String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
		OrderSaveResult orderSaveResult = customsService.setSendExportFulfillmentNotificationData(sendDelivery, index);
		if(Util.null2str(orderSaveResult.getResultCode()).equals("")){
			if(!ediCommonService.isOnline(customProperties.getEbmsIp(), Util.null2int(customProperties.getEbmsPort()))) {
				orderSaveResult.setResultCode("ebmsRestConnectionError");
				OrderSaveResult returnObj = new OrderSaveResult();
				returnObj.setResultCode(orderSaveResult.getResultCode());
				if(orderSaveResult.getResultData() != null){
					Map<String, Object> dataMap = new HashMap<String, Object>();
					dataMap.put("errorData", orderSaveResult.getResultData().get("errorData"));
					dataMap.put("processMessage", orderSaveResult.getResultData().get("processMessage"));
					dataMap.put("processCode", orderSaveResult.getResultData().get("processCode"));
					returnObj.setResultData(dataMap);
				}
				return new ResponseEntity<OrderSaveResult>(returnObj, HttpStatus.OK);
			}

			// STTDK, HKLXK 도입사는 대량건 처리 시 세션(30분) 만료로 DataSource 라우팅 키가 사라지는 문제가 있어
			//  백그라운드 스레드에 요청 컨텍스트(db_key=entCode)를 그대로 넘겨준다.
			// dbKeyForThread: entCode가 "STTDK" 또는 "HKLXK"면 그 값을, 아니면 null.
			// null이면 reqAttrs도 null → 아래 스레드에서 기존(세션) 방식 그대로 동작.
			final String dbKeyForThread =
					("STTDK".equalsIgnoreCase(entCode) || "HKLXK".equalsIgnoreCase(entCode)) ? entCode : null;

			// reqAttrs: 백그라운드 스레드에 심어줄 "가짜 요청 컨텍스트".
			// MyRoutingDataSource가 db_key를 물어보면 세션 대신 이 객체가 답한다.
			final RequestAttributes reqAttrs =
					(dbKeyForThread == null) ? null : new RequestAttributes() {
						// db_key 값을 담아둘 자체 저장소. 실제 HttpSession을 안 쓰고 이 Map만 사용 → 세션 만료와 무관.
						private final Map<String, Object> store = new HashMap<String, Object>();
						// 인스턴스 초기화 블록: 이 객체가 만들어질 때 store에 db_key = 도입사코드(STTDK/HKLXK)를 미리 넣어둔다.
						{ store.put("db_key", dbKeyForThread); }
						// 이 한 줄 덕분에 라우팅 키가 항상 해당 도입사코드로 결정된다.
						@Override public Object getAttribute(String name, int scope) { return store.get(name); }
						// 속성 저장 요청 시 store에 넣음.
						@Override public void setAttribute(String name, Object value, int scope) { store.put(name, value); }
						// 속성 삭제 요청 시 store에서 제거. (형식적 구현)
						@Override public void removeAttribute(String name, int scope) { store.remove(name); }
						// 저장된 속성 이름 목록 반환. (형식적 구현)
						@Override public String[] getAttributeNames(int scope) { return store.keySet().toArray(new String[0]); }
						// 객체 소멸 시 실행할 콜백 등록. 쓸 일 없으므로 아무것도 안 함.
						@Override public void registerDestructionCallback(String name, Runnable callback, int scope) { }
						// 특정 키에 대한 참조(예: 실제 request/session 객체) 반환. 우리는 실제 객체가 없으므로 null.
						@Override public Object resolveReference(String key) { return null; }
						// 세션 ID 반환. 실제 세션이 없으니 식별용 임의 문자열("bg-STTDK") 반환.
						@Override public String getSessionId() { return "bg-" + dbKeyForThread; }

						// 세션 동기화용 잠금 객체 반환. 별도 객체가 없으니 자기 자신을 반환.
						@Override public Object getSessionMutex() { return this; }
					};

			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					boolean routingBound = false;
					//STTDK, HKLXK일경우에 타임아웃 문제없는 위 세팅객체를 적용
					if(reqAttrs != null){
						org.springframework.web.context.request.RequestContextHolder.setRequestAttributes(reqAttrs, true);
						routingBound = true;
						log.info("[수출이행신고] 백그라운드 라우팅 컨텍스트 바인딩 - entCode={}, db_key={}", entCode, dbKeyForThread);
					}
					try{
						Map<String, Object> result = orderSaveResult.getResultData();
						List<Map<String, Object>> houseOrderList = (List<Map<String, Object>>)result.get("houseOrderList");
						int totalHouseCnt = (houseOrderList == null) ? 0 : houseOrderList.size();
						log.info("[수출이행신고] 전송 시작 - entCode={}, dbKeyForThread={}, index={}, 전체건수={}",
								entCode, dbKeyForThread, index, totalHouseCnt);

						List<Map<String, Object>> sendHouseOrderList = new ArrayList<Map<String, Object>>();
						String ediPortalMailboxId = "";
						int houseCnt = 0;
						int batchNo = 0;			/** 배치 순번(로그 추적용) */
						int sentCnt = 0;			/** 실제 전송 완료 건수 누적 */
						for(Map<String, Object> houseOrder : houseOrderList){
							if(houseCnt == 500 || !ediPortalMailboxId.equals(houseOrder.get("EDI_PORTAL_MAILBOX_ID"))){	/** 사서함별 500건씩 나눠서 전송 */
								if(houseCnt != 0){
									batchNo++;
									log.info("[수출이행신고] 배치 전송 - entCode={}, 배치번호={}, 사서함ID={}, 건수={}",
											entCode, batchNo, ediPortalMailboxId, houseCnt);
									customsService.sendExportFulfillmentNotification(sendDelivery, sendHouseOrderList, ediPortalMailboxId, index);
									sentCnt += houseCnt;
									log.info("[수출이행신고] 배치 전송 완료 - entCode={}, 배치번호={}, 사서함ID={}, 누적전송={}/{}",
											entCode, batchNo, ediPortalMailboxId, sentCnt, totalHouseCnt);
									Thread.sleep(60000);	/** 관세청에 300건씩 전송시 1분 대기해야함 */
									sendHouseOrderList = new ArrayList<Map<String, Object>>();
									houseCnt = 0;
								}
								ediPortalMailboxId = Util.null2str(houseOrder.get("EDI_PORTAL_MAILBOX_ID"));
							}
							
							sendHouseOrderList.add(houseOrder);
							houseCnt++;
						}
						if(houseCnt != 0){
							batchNo++;
							log.info("[수출이행신고] 배치 전송(마지막) - entCode={}, 배치번호={}, 사서함ID={}, 건수={}",
									entCode, batchNo, ediPortalMailboxId, houseCnt);
							customsService.sendExportFulfillmentNotification(sendDelivery, sendHouseOrderList, ediPortalMailboxId, index);
							sentCnt += houseCnt;
						}
						log.info("[수출이행신고] 전송 완료 - entCode={}, 총배치={}, 총전송건수={}/{}",
								entCode, batchNo, sentCnt, totalHouseCnt);
					}catch(Exception e){
						log.error("[수출이행신고] 전송 실패 - entCode={}, index={}, msg={}", entCode, index, e.getMessage(), e);
						try{
							ediCommonService.asyncErrorSendEmail("CustomerController.sendExportFulfillmentNotification", BeanUtils.describe(sendDelivery), e);
						}catch(Exception ee){
							log.error(Util.getPrintStacTraceString(ee));
						}
					}finally{
						//자원초기화
						if(routingBound){
							org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
						}
					}
				}
			});
			thread.start();
			
			orderSaveResult.setResultCode("ebmsRest");
		}
		
		return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
	}
	
	@ApiOperation(value = "수출이행신고", notes="수출이행신고를 송신합니다.")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "entCode", value = "도입사코드", required = true, dataType = "string", paramType = "path", defaultValue = "YHTXX")
	})
	@RequestMapping(value="/sendExportFulfillmentNotificationOld", method = RequestMethod.POST)
	public ResponseEntity<OrderSaveResult> sendExportFulfillmentNotificationOld(@PathVariable("entCode") String entCode, @RequestBody SendDelivery sendDelivery) throws Exception {
		OrderSaveResult orderSaveResult = customsService.setSendExportFulfillmentNotificationDataOld(sendDelivery);
		if(Util.null2str(orderSaveResult.getResultCode()).equals("")){
			if(!ediCommonService.isOnline(customProperties.getEbmsIp(), Util.null2int(customProperties.getEbmsPort()))) {
				orderSaveResult.setResultCode("ebmsRestConnectionError");
				return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
			}
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try{
						Map<String, Object> resultData = orderSaveResult.getResultData();
						Map<String, Object> restData = (Map)resultData.get("REST");
						int logStartIdx, logEndIdx;
						JSONObject jsonObject;
						List<Map<String, Object>> logList;
						List<JSONArray> jsonArrayList;
						JSONArray jsonArray;
						List<List<String>> odJobNoArrayList;
						List<String> houseOdJobNoList;
						JSONObject sendData;
						Map<String, Object> logMap = (Map)resultData.get("logMap");
						Map<String, Object> logData;
						ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
						BulkData bulkData = null;
						String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
						
						String podStatus = null;
						if("01".equals(sendDelivery.getSendType())){
							podStatus = "ECM121";
						}else if("02".equals(sendDelivery.getSendType())){
							podStatus = "ECM127";
						}else if("03".equals(sendDelivery.getSendType())){
							podStatus = "ECM125";
						}
						Map<String, Object> updateEdiStatusMap = new HashMap<String, Object>();
						updateEdiStatusMap.put("gmtTime", sendDelivery.getGmtTime());
						updateEdiStatusMap.put("sendType", sendDelivery.getSendType());
						updateEdiStatusMap.put("podStatus", podStatus);
						int listSize;
						
						for(String ediPortalMailboxId : restData.keySet()){
							if(ediPortalMailboxId.indexOf("_ORIG_CODE") == -1 && ediPortalMailboxId.indexOf("_LOG_LIST") == -1){
								logStartIdx = 0;
								logEndIdx = 0;
								
								jsonObject = (JSONObject)restData.get(ediPortalMailboxId);
								logList = (List)restData.get(ediPortalMailboxId + "_LOG_LIST");
								jsonArrayList = (List<JSONArray>)jsonObject.get("dataList");
								odJobNoArrayList = (List<List<String>>)jsonObject.get("odJobNoList");
								listSize = jsonArrayList.size();
								for(int i = 0; i < listSize; i++){
									jsonArray = jsonArrayList.get(i);
									houseOdJobNoList = odJobNoArrayList.get(i);
									sendData = new JSONObject();
									sendData.put("dataList", jsonArray);
									customsService.sendExportFulfillmentNotificationMrs(ediPortalMailboxId, sendData, logMap, updateEdiStatusMap, houseOdJobNoList);
									
									logEndIdx = logStartIdx + jsonArray.size();
									for(int j = i; j < logEndIdx; j++){
										logData = logList.get(j);
										logData.putAll(logMap);
										bulkData = new BulkData();
										bulkData.setActionType(BulkData.Type.CREATE);
										bulkData.setIndexName(index);
										bulkData.setId(Util.null2str(logData.get("es_job_no")));
										bulkData.setJsonMap(logData);
										bulkList.add(bulkData);
									}
									logStartIdx = logEndIdx;
									
									if(listSize - 1 != i){
										Thread.sleep(60000);
									}
								}
							}
						}
						
						customsService.sendExportFulfillmentNotificationOld(sendDelivery, orderSaveResult, bulkList, index, podStatus);
					}catch(Exception e){
						try{
							ediCommonService.asyncErrorSendEmail("CustomerController.sendExportFulfillmentNotification", BeanUtils.describe(sendDelivery), e);
						}catch(Exception ee){
							log.error(Util.getPrintStacTraceString(ee));
						}
					}
				}
			});
			thread.start();
			
			orderSaveResult.setResultCode("ebmsRest");
		}
		
		return new ResponseEntity<OrderSaveResult>(orderSaveResult, HttpStatus.OK);
	}
	
}

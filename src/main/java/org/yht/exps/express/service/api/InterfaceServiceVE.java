package org.yht.exps.express.service.api;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import D.P;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.exps.express.mapper.api.InterfaceMapper;
import org.yht.exps.express.mapper.api.InterfaceMapperVE;
import org.yht.exps.express.mapper.eCommerce.DeliveryMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.model.addrLinkApi.Juso;
import org.yht.exps.express.model.addrLinkApi.Results;
import org.yht.exps.express.model.api.CoupangOrderInterface;
import org.yht.exps.express.model.api.CoupangPccc;
import org.yht.exps.express.model.api.CoupangReturn;
import org.yht.exps.express.model.api.HblListVE;
import org.yht.exps.express.model.api.InterfaceReturnDataPcccVE;
import org.yht.exps.express.model.api.InterfaceReturnDataVE;
import org.yht.exps.express.model.api.ItemInfoVE;
import org.yht.exps.express.model.api.ItemListVE;
import org.yht.exps.express.model.eCommerce.delivery.UpdateDelivery;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.logs.InterfaceLogs;
import org.yht.exps.express.service.eCommerce.ZipcodeConvert;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.OrderValidateVE;
import org.yht.util.Util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

@Slf4j
@Service
@Transactional
public class InterfaceServiceVE {
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	
	@Autowired 
	PlatformTransactionManager transactionManager;
	@Autowired
	OrderMapper orderMapper;
	@Autowired
	InterfaceMapper interfaceMapper;
	@Autowired
	DeliveryMapper deliveryMapper;
	@Autowired
	InterfaceMapperVE interfaceMapperVE; 
	
	@Autowired
	ZipcodeConvert zipcodeConvert; 
	
	@Autowired
	RestHighLevelClient client;
	
	@Autowired
	StringRedisTemplate redisTemplate;
	
	@Autowired
	private CustomProperties customProperties;
	
	/**
	 * 쿠팡 오더 생성 API
	 * @param order
	 * @return InterfaceResultVE
	 * @throws Exception
	 */
	public InterfaceReturnDataVE registCoupangOrderInfo(CoupangOrderInterface order, HttpServletRequest req) throws Exception{
//		InterfaceResultVE result = new InterfaceResultVE();
		InterfaceReturnDataVE returnData = new InterfaceReturnDataVE();
		try {
			String format = "yyyyMMdd_HHmmss.SSSSSSS";
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
			String date = now.format(dtf);
			
			HttpSession session = req.getSession(true);
			String accessKey = "";//(Util.null2str(order.getAccessIdentifier().getAccessKey())).toUpperCase();
			String sender = Util.null2str(order.getOriginCode());//(Util.null2str(order.getAccessIdentifier().getSenderIdentifier())).toUpperCase();
			String receiver = (String) session.getAttribute("db_key");//(Util.null2str(order.getAccessIdentifier().getReceiverIdentifier())).toUpperCase();
			
			String returnCode = "";
			String returnMessage = "";
			String hblNo = "";
			String accessFlag = "Y";//"N";
			String path = "";
			String apiPath = customProperties.getInterfaceDir() + "api/" + receiver + "/";
			String infileName = receiver + "_" + sender + "_" + date + UUID.randomUUID().toString().substring(0, 8) + ".json";
			String bound = "";
			String boundEs = "";
			String boundTypeEs = "";
			String logId = ""; // ES 로그용 아이디x
			String index = ""; // ES 로그용 index
			String if_status = "RS"; // 처리상태값
			String if_status_info = "Receive Start"; // 처리상태설명
			
			ObjectMapper jsonToMap = new ObjectMapper(); // json To Map 변환용
			
			Map<String, Object> tmpMap = null;
			IndexApi indexApi = null;
			DocumentApi documentApi = null;
			
			Map<String, Object> map = new HashMap<String, Object>();
			BufferedWriter bw = null;
			FileOutputStream fos = null;
			OutputStreamWriter osw = null;
			
			String courierCode = Util.null2str(order.getCourierCode());
//			if(courierCode.contains("T00017")) {
//				boundEs = "AIR";
//				bound += "AI";
//				boundTypeEs = "IMP";
//			}else if(courierCode.contains("T00025")) {
//				boundEs = "SEA";
//				bound += "OI";
//				boundTypeEs = "IMP";
//			}
			
			String shippingType = Util.null2str(order.getHblList().get(0).getHblProperties().getShippingType());
			if("SEA".equals(shippingType) || "DOMESTIC_SEA".equals(shippingType)) {
				boundEs = "SEA";
				bound += "OI";
				boundTypeEs = "IMP";
			}else if("DOMMESTIC_AIR".equals(shippingType)) {
				boundEs = "AIR";
				bound += "AI";
				boundTypeEs = "IMP";
			}else {
				boundEs = "SEA";
				bound += "OI";
				boundTypeEs = "IMP";
			}
			
			path = apiPath + "COUPANG/RECV/" +bound + "/"; // Interface PATH + 업체코드 경로에 저장
			
			map.put("ReceiverId", receiver);
			map.put("SenderId", sender);
			map.put("UseYN", "Y");
			
//			// Interface key 조회(ENT_CODE(ReceiverId) ,CUSTOMER_CODE(SenderId) ,ACCESS_KEY ,USE_YN) 	
//			Map<String, Object> deliveryInfo = (Map<String, Object>)interfaceMapper.selectInterfaceInfo(map);
//			
//			if(deliveryInfo != null){
//				if(accessKey.equals(Util.null2str(deliveryInfo.get("ACCESS_KEY")))){
//					accessFlag = "Y"; // 요청자가 보낸 값과 동일하면 Y
//				}else{
//					accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
//				}
//			}else{
//				accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
//			}
			
			File pathDir = new File(path);
			if (pathDir.exists() == false)	pathDir.mkdirs();
			File file = new File(path + infileName);
			
	 		CoupangOrderInterface orderInterface = new CoupangOrderInterface();
	 		if("Y".equals(accessFlag)){
        		try{
        			String validateOrder = OrderValidateVE.validateOrderInterface(order);
        			if(!Util.isEmpty(validateOrder)) {
        				accessFlag = "V";
        				returnMessage += validateOrder;
        			}else {
        				orderInterface.setMblNumber(order.getMblNumber());
        				orderInterface.setCourierCode(order.getCourierCode());
        				orderInterface.setOriginCode(order.getOriginCode());
        				orderInterface.setBrokerCode(order.getBrokerCode());
        				orderInterface.setFirstMileCourierCode(order.getFirstMileCourierCode());
        				orderInterface.setDestinationCode(order.getDestinationCode());
        				orderInterface.setFlightNumber(order.getFlightNumber());
        				orderInterface.setNumberOfPackages(order.getNumberOfPackages());
        				orderInterface.setMblProperties(order.getMblProperties());	
        				orderInterface.setSalesTypeCode(order.getSalesTypeCode());
        				orderInterface.setCompanyCode(order.getCompanyCode());
        				orderInterface.setCompanyName(order.getCompanyName());
        				
        				
    					List<HblListVE> hblList = order.getHblList();
    					List<HblListVE> newHblList = new ArrayList<HblListVE>();
    					if(hblList != null && hblList.size() > 0) {
    						for(int m=0; m<hblList.size(); m++) {
    							HblListVE setHblList = hblList.get(m);
    							
    							Map<String, Object> odMap = jsonToMap.readValue(jsonToMap.writeValueAsString(hblList.get(m)).toString(), Map.class);
    							Map<String, Object> customerInfo = jsonToMap.readValue(jsonToMap.writeValueAsString(hblList.get(m).getCustomerInfo()).toString(), Map.class);
    							odMap.putAll(customerInfo);
    							Map<String, Object> hblProperties = jsonToMap.readValue(jsonToMap.writeValueAsString(hblList.get(m).getHblProperties()).toString(), Map.class);
    							odMap.putAll(hblProperties);
    							
    							validateOrder = OrderValidateVE.validateOrderHblInterface(odMap);
    							if(!Util.isEmpty(validateOrder)) {
    								returnMessage += "/hblNo >> " + hblList.get(m).getHblNumber() + "_" +validateOrder;
    								accessFlag = "V";
    								continue;
    							}
    							
    							List<ItemListVE> itemList = hblList.get(m).getItemList();
    							List<ItemListVE> newItemList = new ArrayList<ItemListVE>();
    							if(itemList != null && itemList.size() > 0) {
    								for (int j = 0; j < itemList.size(); j++) {
    									Map<String, Object> odItemMap = jsonToMap.readValue(jsonToMap.writeValueAsString(itemList.get(j)).toString(), Map.class);
    									Map<String, Object> itemProperties = jsonToMap.readValue(jsonToMap.writeValueAsString(itemList.get(j).getItemProperties()).toString(), Map.class);
    									if(itemProperties != null) odItemMap.putAll(itemProperties);
    									
    									validateOrder = OrderValidateVE.validateItemInterface(odItemMap);
    									if(!Util.isEmpty(validateOrder)) {
    										returnMessage += "/hblNo >> " + hblList.get(m).getHblNumber() + "_" +validateOrder;
    										accessFlag = "V";
    										continue;
    									}
    									
    									newItemList.add(itemList.get(j));
    								}
    							}else {
    	    						returnMessage += "/hblNo >> " + hblList.get(m).getHblNumber() + "_/itemList:아이템 정보가 없습니다.";
    	    						accessFlag = "V";
    	    						continue;
    	    					}
    							setHblList.setItemList(newItemList);
    							newHblList.add(setHblList);
    							
    					        // history 등록 
    							tmpMap = new HashMap<String, Object>();
    							logId = Util.null2str(setHblList.getOrderNumber());
    							index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
    							
    					    	// 처리결과에 따른 로그 기록
    					        tmpMap.clear();
    							tmpMap.put("ent_code", receiver); 		 					// 도입사코드
    							tmpMap.put("customer_code", sender); 		 				// 고객코드
    							tmpMap.put("solution", "ECM");      		 				// SOLUTION
    							tmpMap.put("bound", boundEs);		 	 					// 바운드 (AIR)
    							tmpMap.put("bound_type", boundTypeEs);	 					// 바운드타입
    							tmpMap.put("mbl_no", Util.null2str(order.getMblNumber()));	// MBL_NO
    							tmpMap.put("hbl_no", Util.null2str(setHblList.getHblNumber()));	// HBL_NO
    							tmpMap.put("if_service_code", bound);
    							tmpMap.put("if_doc_code", "COUPANG_ORDER_API");
    							tmpMap.put("if_doc_name", "쿠팡오더API");
    							tmpMap.put("if_doc_reference_no", "");	 					// 문서번호 
    							tmpMap.put("if_vender", sender);							// 중개사업자
    							tmpMap.put("if_send_recv_type", "R");						// 송수신타입(S:SEND, R:RECV)
    							tmpMap.put("if_send_recv_method", "JSON To DB");			// 송수신방법
    							tmpMap.put("if_file_path", path);			 				// 파일경로
    							tmpMap.put("if_file_name", infileName);			 			// 파일명
    							tmpMap.put("if_status", if_status);			 				// 처리상태
    							tmpMap.put("if_status_info", if_status_info);		 		// 송수신상태_설명
    							tmpMap.put("if_sender_id", sender);							// 송신식별자
    							tmpMap.put("if_sender_name", "");		 					// 송신자명
    							tmpMap.put("if_receiver_id", "");		 					// 수신식별자
    							tmpMap.put("if_receiver_name", "");	     					// 수신자명
    							tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));		// DATE
    							tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));	// DATE_TIME
    							tmpMap.put("if_remark", returnMessage);			 			// 비고( 에러로그 등)
    							tmpMap.put("register_user_id", "API");
    							tmpMap.put("register_user_name", "API");
    							tmpMap.put("register_date_time", System.currentTimeMillis());
    							tmpMap.put("es_job_no", logId);
    							tmpMap.put("file_data", new Gson().toJson(order));
    							
    							indexApi = new IndexApi(client);
    							if (!indexApi.existIndex(index))	indexApi.createIndex(index);
    							documentApi = new DocumentApi(client);
    							documentApi.createDocument(index, logId, tmpMap);
    						}
    						
    						orderInterface.setHblList(newHblList);
    					}else {
    						returnMessage += "/HblList:하우스 정보가 없습니다.";
    						accessFlag = "V";
    					}
        			}
				} catch(Exception e){
					accessFlag = "E";
					returnMessage = e.toString();
				}
        	}
	 		
	 		if("Y".equals(accessFlag)){
	 			ObjectMapper mapper = new ObjectMapper();
	 			String samString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(orderInterface);
	 			path = apiPath + "COUPANG/SUCC/" + bound + "/";
	 			pathDir = new File(path);
	 			if (pathDir.exists() == false)	pathDir.mkdirs();
	 			fos = new FileOutputStream(path + infileName);
	 			osw = new OutputStreamWriter(fos, "UTF-8");
	 			bw = new BufferedWriter(osw);
	 			bw.write(Util.null2str(samString));
	 			bw.close();
	 			returnCode = "SUCCESS";
	 			returnMessage = "";
	 			if_status = "RECV";
	 			if_status_info = "Receive Complete";
	 			path = "";	//정상건은 배치처리시 파일위치가 변경되기때문에 경로 남기지 않음
 			}else{
	 			path = "";
	 			returnCode = "FAIL";
	 			if_status = "ERROR";
	 			if_status_info = "Receive Fail"; 
	 			if("N".equals(accessFlag)){
		 			file.delete();
		 			infileName = "";	//파일 삭제된 경우 로그에 파일명 남기지 않음
		 			returnMessage = "Wrong AccessKey";
	 			}else if("X".equals(accessFlag)){
		 			file.delete();
		 			infileName = "";	//파일 삭제된 경우 로그에 파일명 남기지 않음
		 			returnMessage = "Not a service provider";
	 			}else if("V".equals(accessFlag)){
		 			ObjectMapper mapper = new ObjectMapper();
		 			String samString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(order);
		 			path = apiPath + "COUPANG/ERROR/" + bound + "/";
		 			pathDir = new File(path);
		 			if (pathDir.exists() == false)	pathDir.mkdirs();
		 			infileName = "ErrorValidation_" + infileName;
		 			fos = new FileOutputStream(path + infileName);
		 			osw = new OutputStreamWriter(fos, "UTF-8");
		 			bw = new BufferedWriter(osw);
		 			bw.write(Util.null2str(samString));
		 			bw.close();
		 			returnMessage = " 에러가 발생한 부분 => " + returnMessage;
	 			}else if("E".equals(accessFlag)){
	 				ObjectMapper mapper = new ObjectMapper();
		 			String samString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(order);
		 			path = apiPath + "COUPANG/ERROR/" + bound + "/";
		 			pathDir = new File(path);
		 			if (pathDir.exists() == false)	pathDir.mkdirs();
		 			infileName = "Error_" + infileName;
		 			fos = new FileOutputStream(path + infileName);
		 			osw = new OutputStreamWriter(fos, "UTF-8");
		 			bw = new BufferedWriter(osw);
		 			bw.write(Util.null2str(samString));
		 			bw.close();
	 			}
 			}
	 		
			returnData.setCode(returnCode);
			returnData.setMessage(returnMessage);
		}catch (Exception e) {
			log.error(Util.getPrintStacTraceString(e));
			returnData.setCode("FAIL");
			returnData.setMessage("System Error.");
		}
		
		return returnData;
	}
	
	/**
	 * 쿠팡 반품처리 API
	 * @param order
	 * @return InterfaceResultVE
	 * @throws Exception
	 */
	public InterfaceReturnDataVE returnCoupangOrderInfo(CoupangReturn order, HttpServletRequest req) throws Exception{
//		InterfaceResultVE result = new InterfaceResultVE();
		InterfaceReturnDataVE returnData = new InterfaceReturnDataVE();
		long procStart = System.currentTimeMillis();
		log.info("[쿠팡반품접수] START - orderId={}, requestId={}, orderInvoiceNumber={}",
				Util.null2str(order.getOrderId()), Util.null2str(order.getRequestId()), Util.null2str(order.getOrderInvoiceNumber()));
		try {
			String format = "yyyyMMdd_HHmmss.SSSSSSS";
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
			String date = now.format(dtf);
			
			HttpSession session = req.getSession(true);
			String accessKey = "";//(Util.null2str(order.getAccessIdentifier().getAccessKey())).toUpperCase();
			String sender = "";//Util.null2str(order.getOriginCode());//(Util.null2str(order.getAccessIdentifier().getSenderIdentifier())).toUpperCase();
			String receiver = (String) session.getAttribute("db_key");//(Util.null2str(order.getAccessIdentifier().getReceiverIdentifier())).toUpperCase();
			
			String returnCode = "";
			String returnMessage = "";
			String hblNo = "";
			String accessFlag = "Y";//"N";
			String bound = "EMS/";
			String boundEs = "";
			String boundTypeEs = "IMP";
			String logId = ""; // ES 로그용 아이디x
			String index = ""; // ES 로그용 index
			String if_status = "RS"; // 처리상태값
			String if_status_info = "Receive Start"; // 처리상태설명
			
			ObjectMapper jsonToMap = new ObjectMapper(); // json To Map 변환용
			
			Map<String, Object> tmpMap = null;
			IndexApi indexApi = null;
			DocumentApi documentApi = null;
			
//			Map<String, Object> map = new HashMap<String, Object>();
//			map.put("ReceiverId", receiver);
//			map.put("SenderId", sender);
//			map.put("UseYN", "Y");
			
//			// Interface key 조회(ENT_CODE(ReceiverId) ,CUSTOMER_CODE(SenderId) ,ACCESS_KEY ,USE_YN) 	
//			Map<String, Object> deliveryInfo = (Map<String, Object>)interfaceMapper.selectInterfaceInfo(map);
//			
//			if(deliveryInfo != null){
//				if(accessKey.equals(Util.null2str(deliveryInfo.get("ACCESS_KEY")))){
//					accessFlag = "Y"; // 요청자가 보낸 값과 동일하면 Y
//				}else{
//					accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
//				}
//			}else{
//				accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
//			}
			
	 		if("Y".equals(accessFlag)){
        		try{
        			String validateOrder = OrderValidateVE.validateReturnOrderInterface(order);
        			if(!Util.isEmpty(validateOrder)) {
        				accessFlag = "V";
        				returnMessage += validateOrder;
        			}else {
						if(order.getSenderAddressInfo() == null) {
							accessFlag = "V";
	        				returnMessage += "/senderAddressInfo: 고객 주소 정보가 없습니다.";
						}
						
						if(order.getReceiverAddressInfo() == null) {
							accessFlag = "V";
	        				returnMessage += "/receiverAddressInfo: 반출지 주소 정보가 없습니다.";
						}
						
						if("Y".equals(accessFlag)){
							String senderAdd = OrderValidateVE.validateReturnAddInfoInterface(order.getSenderAddressInfo());
	        				String receiverAdd = OrderValidateVE.validateReturnAddInfoInterface(order.getReceiverAddressInfo());
	        				
	        				if(!Util.isEmpty(senderAdd)) {
	        					accessFlag = "V";
	            				returnMessage += senderAdd;
	        				}
	        				if(!Util.isEmpty(receiverAdd)) {
	        					accessFlag = "V";
	            				returnMessage += receiverAdd;
	        				}

							if("Y".equals(accessFlag)){
								List<ItemInfoVE> itemList = order.getItemList();
								List<ItemInfoVE> newItemList = new ArrayList<ItemInfoVE>();
								if(itemList != null && itemList.size() > 0) {
									for (int j = 0; j < itemList.size(); j++) {
										String itemValidate = OrderValidateVE.validateReturnItemInfoInterface(itemList.get(j));
										if(!Util.isEmpty(itemValidate)) {
											accessFlag = "V";
											returnMessage += itemValidate;
											continue;
										}
										newItemList.add(itemList.get(j));
									}

									//쿠팡에서는 pono를 주며 그걸가지고 잡넘버와 매핑
									Map<String, Object> orderMap = jsonToMap.readValue(jsonToMap.writeValueAsString(order).toString(), Map.class);
									log.debug("orderMap->"+ orderMap);
									orderMap.put("entCode", receiver);
									orderMap.put("itemList", newItemList);
									Map<String, Object> returnOrderInfo = interfaceMapperVE.selectReturnOrderInfo(orderMap);
									
									//멀티건 추가 및 유효성확인 로직(반품로직은 멀티가 없을경우 멀티를 만들고 진행해야함)
									// (일반적인상황 -> 들어온 잡넘버의 멀티건중에 RETURN_REQUEST_ID와 같은게 있으면 튕김)
									// (예외적인상황 -> 들어온 잡넘버의 멀티건중에 RETURN_REQUEST_ID와 같은게 없으면 해당 잡넘버의 pod4,pod4_m을 지우고 멀티를 추가후 flash)
									boolean chkAdFlag = true;
									Map<String, Object> chkReturnMultiMap = new HashMap<>();
									//멀티추가 고려한 맵
									chkReturnMultiMap.put("odJobNo",returnOrderInfo.get("OD_JOB_NO"));
									chkReturnMultiMap.put("deliveryNo",returnOrderInfo.get("HBL_NO"));
									chkReturnMultiMap.put("deliveryCode",returnOrderInfo.get("DELIVERY_CODE"));
									chkReturnMultiMap.put("customerCode",returnOrderInfo.get("DELIVERY_CODE"));//배송사와같음
									chkReturnMultiMap.put("paymentGubn",returnOrderInfo.get("DELIVERY_PAYMENT_TYPE"));
									chkReturnMultiMap.put("deliveryFee",returnOrderInfo.get("DELIVERY_FEE"));
									chkReturnMultiMap.put("pieces",returnOrderInfo.get("PIECES"));
									chkReturnMultiMap.put("dimWidth",returnOrderInfo.get("DIM_WIDTH"));
									chkReturnMultiMap.put("dimLength",returnOrderInfo.get("DIM_LENGTH"));
									chkReturnMultiMap.put("dimHeight",returnOrderInfo.get("DIM_HEIGHT"));
									chkReturnMultiMap.put("vwt",returnOrderInfo.get("VWT"));
									chkReturnMultiMap.put("recipientName",returnOrderInfo.get("CONSIGNEE_NAME"));
									chkReturnMultiMap.put("remarks","");
									chkReturnMultiMap.put("requestId",orderMap.get("requestId"));
									chkReturnMultiMap.put("itemName",returnOrderInfo.get("COMMODITY_ITEM"));
									chkReturnMultiMap.put("registerUserId","API");
									List<Map<String, Object>> chkReturnMapInfo = interfaceMapperVE.selectReturnChk(chkReturnMultiMap);
									log.info("[쿠팡반품접수] 기존 반품멀티 조회 OD_JOB_NO={}, 기존건수={}, requestId={}",
											returnOrderInfo.get("OD_JOB_NO"), chkReturnMapInfo.size(), orderMap.get("requestId"));
									if(chkReturnMapInfo.size()>0){
										//같은게 1건이라도 있으면 로직이 진행되지 않게하기위해 for문을 돌림
										for(int i=0;i<chkReturnMapInfo.size();i++){
											if(Util.null2str(chkReturnMapInfo.get(i).get("RETURN_REQUEST_ID")).equals(Util.null2str(orderMap.get("requestId")))){
												accessFlag = "V";
												chkAdFlag = false;
											}
										}
										//chkAdFlag가 true면 for문에서 일치하는게 없었다는 뜻이므로 정상로직으로 진행
										if(chkAdFlag==true){
											log.info("[쿠팡반품접수] 동일 requestId 없음 - 기존 POD 초기화 후 멀티 재생성 OD_JOB_NO={}", returnOrderInfo.get("OD_JOB_NO"));
											//1.변경된건을 고려해 반품관련데이터 초기화해야함(pod4,pod4_m을 초기화)
											interfaceMapperVE.deleteChkPod(chkReturnMultiMap);
											//2.멀티를 새로 추가
											interfaceMapperVE.insertReturnMulti(chkReturnMultiMap);
											// 명시적으로 반영 + 캐시 초기화
											sqlSessionTemplate.flushStatements();
											sqlSessionTemplate.clearCache();
										}else {
											log.info("[쿠팡반품접수] 동일 requestId 이미 존재 - 중복 반품으로 차단 OD_JOB_NO={}, requestId={}", returnOrderInfo.get("OD_JOB_NO"), orderMap.get("requestId"));
										}
									}else {
											log.info("[쿠팡반품접수] 기존 반품멀티 없음 - 신규 멀티 생성 OD_JOB_NO={}", returnOrderInfo.get("OD_JOB_NO"));
											//멀티건이 없는경우는 무조건 생성하고 flash하면 됨
											interfaceMapperVE.insertReturnMulti(chkReturnMultiMap);
											// 명시적으로 반영 + 캐시 초기화
											sqlSessionTemplate.flushStatements();
											sqlSessionTemplate.clearCache();
									}

									//대상데이터가 있고 중복반품번호가 아닌경우
									if(returnOrderInfo != null && chkAdFlag) {
										//오더기준값 최신화
										interfaceMapperVE.updateReturnOrder(orderMap);//반품저장
										interfaceMapperVE.updateReturnOrderAd(orderMap);//requestId저장

										//멀티여부 세팅(이거에 따라 조회할 요소가 hbl인지 deliveryNo인지 정해짐)
										UpdateDelivery send = new UpdateDelivery();
										Map<String, Object> multiMap = new HashMap<>();
										log.debug("chk(orderInvoiceNumber) ->"+ Util.null2str(orderMap.get("orderInvoiceNumber")));
										//멀티건은 orderInvoiceNumber을 받아 hbl과 매핑(현재는 무조건 멀티)
										multiMap.put("odJobNo",returnOrderInfo.get("OD_JOB_NO"));
										if(deliveryMapper.countMulti(multiMap)>0){
											log.debug("mulchk->"+ deliveryMapper.countMulti(multiMap));
											send.setMultiChk("multi");
											send.setSrchHblNo(Util.null2str(orderMap.get("orderInvoiceNumber")));
										}

										// 레디스세팅
										send.setRegisterUserId("API");
										send.setRegisterUserName("API");
										send.setGmtTime(9);
										send.setSrchReturnYn("Y");
										send.setCryptographyYn("N");
										send.setBlTypeMh("H");
										Map<String, Object> item = new HashMap<>();
										item.put("odJobNo", returnOrderInfo.get("OD_JOB_NO"));
										List<Map<String, Object>> dataList = new ArrayList<>();
										dataList.add(item);
										send.setDataList(dataList);
										//레디스에 넣을 셀렉 정보(멀티여부는 위에 세팅한 setMultiChk로 동적쿼리셀렉)
										List<Map<String, Object>> resultDelivery = deliveryMapper.selectSendDeliveryListToMultiVE(send);
										Map<String, Object>deliveryMapChk = new HashMap<>();
										String key = "E-COMMERCE_DELIVERY_RETURN_" + receiver;

										// es로그세팅
										String ifStatusRedis = "SEND";
										String errorMsg = "";
										int totalCnt = resultDelivery.size();
										int successedCnt = 0;
										int failedCnt = 0;

										//1건이상 나와야 진행
										if(resultDelivery.size()>0){
											deliveryMapChk = resultDelivery.get(0);
											try {

												//반품정보테이블에 저장
												Map<String, Object> senderInfo = (Map<String, Object>) orderMap.get("senderAddressInfo");
												Map<String, Object> receiverInfo = (Map<String, Object>) orderMap.get("receiverAddressInfo");
												Map<String, Object> insertOrderMap = new HashMap<>();
												insertOrderMap.put("od_job_no",  returnOrderInfo.get("OD_JOB_NO"));
												insertOrderMap.put("requestId",  orderMap.get("requestId"));
												insertOrderMap.put("senderZipcode",       senderInfo.get("zipCode"));
												insertOrderMap.put("senderName",          senderInfo.get("name"));
												insertOrderMap.put("senderTel",           senderInfo.get("contactNumber"));
												insertOrderMap.put("senderHp",            senderInfo.get("cellPhoneNumber"));
												insertOrderMap.put("senderAddress",       senderInfo.get("address"));
												insertOrderMap.put("senderAddressDetail", senderInfo.get("addressDetail"));
												insertOrderMap.put("receiverZipcode",       receiverInfo.get("zipCode"));
												insertOrderMap.put("receiverName",          receiverInfo.get("name"));
												insertOrderMap.put("receiverTel",           receiverInfo.get("tel"));
												insertOrderMap.put("receiverHp",            receiverInfo.get("hp"));
												insertOrderMap.put("receiverAddress",       receiverInfo.get("address"));
												insertOrderMap.put("receiverAddressDetail", receiverInfo.get("addressDetail"));
												insertOrderMap.put("seqNoId", deliveryMapChk.get("SEQ_NO_ID"));
												//삭제하고 인서트하는로직(변경건의 중복키방지)
												interfaceMapperVE.deleteReturninfo(insertOrderMap);
												interfaceMapperVE.insertReturninfo(insertOrderMap);
												interfaceMapperVE.insertResendinfo(insertOrderMap);
												// 멀티 정보 레디스세팅
												JSONObject json = new JSONObject();
												json.put("DELIVERY_PAYMENT_TYPE_MULTI",  deliveryMapChk.get("DELIVERY_PAYMENT_TYPE_MULTI"));
												json.put("commodityItemLocConvertMulti",  deliveryMapChk.get("commodityItemLocConvertMulti"));
												json.put("DIM_WIDTH_MULTI",              deliveryMapChk.get("DIM_WIDTH_MULTI"));
												json.put("DIM_LENGTH_MULTI",             deliveryMapChk.get("DIM_LENGTH_MULTI"));
												json.put("DIM_HEIGHT_MULTI",             deliveryMapChk.get("DIM_HEIGHT_MULTI"));
												json.put("DIM_WIDTH", deliveryMapChk.get("DIM_WIDTH"));
												json.put("DIM_LENGTH", deliveryMapChk.get("DIM_LENGTH"));
												json.put("DIM_HEIGHT", deliveryMapChk.get("DIM_HEIGHT"));
												json.put("GWT_MULTI",                    deliveryMapChk.get("GWT_MULTI"));
												json.put("VWT", deliveryMapChk.get("VWT"));
												json.put("VWT_MULTI", deliveryMapChk.get("VWT_MULTI"));
												json.put("DELIVERY_NO",                  deliveryMapChk.get("DELIVERY_NO"));
												json.put("SEQ_NO_ID",                    deliveryMapChk.get("SEQ_NO_ID"));
												json.put("MULTI_COUNT", deliveryMapChk.get("MULTI_COUNT"));
												json.put("DELIVERY_CODE_MULTI", deliveryMapChk.get("DELIVERY_CODE_MULTI"));

												// POD 관련
												json.put("ECM360_POD_JOB_NO_MULTI",      deliveryMapChk.get("ECM360_POD_JOB_NO_MULTI"));
												json.put("ECM360_POD_JOB_NO",            deliveryMapChk.get("ECM360_POD_JOB_NO"));
												json.put("ECM410_POD_JOB_NO_MULTI",      deliveryMapChk.get("ECM410_POD_JOB_NO_MULTI"));
												json.put("ECM340_POD_JOB_NO_MULTI",      deliveryMapChk.get("ECM340_POD_JOB_NO_MULTI"));
												json.put("ECM340_POD_JOB_NO",            deliveryMapChk.get("ECM340_POD_JOB_NO"));
												json.put("ECM504_POD_JOB_NO_MULTI",      deliveryMapChk.get("ECM504_POD_JOB_NO_MULTI"));
												json.put("ECM504_POD_JOB_NO",            deliveryMapChk.get("ECM504_POD_JOB_NO"));

												// 오더 기본 정보
												json.put("HBL_NO",                       deliveryMapChk.get("HBL_NO"));
												json.put("OD_JOB_NO",                    deliveryMapChk.get("OD_JOB_NO"));
												json.put("HSN",                          deliveryMapChk.get("HSN"));
												json.put("AGENT_CODE",                   deliveryMapChk.get("AGENT_CODE"));
												json.put("DEST_PORT",                    deliveryMapChk.get("DEST_PORT"));
												json.put("LAST_POD_STATUS",              deliveryMapChk.get("LAST_POD_STATUS"));
												json.put("VESSEL_FLIGHT",                deliveryMapChk.get("VESSEL_FLIGHT"));
												json.put("CHK_PO_NO",                    deliveryMapChk.get("CHK_PO_NO"));
												json.put("PO_NO",                        deliveryMapChk.get("PO_NO"));
												json.put("TR_CODE",                      deliveryMapChk.get("TR_CODE"));
												json.put("DELIVERY_CODE",                deliveryMapChk.get("DELIVERY_CODE"));
												json.put("MBL_NO",                       deliveryMapChk.get("MBL_NO"));
												json.put("DELIVERY_PAYMENT_TYPE",        deliveryMapChk.get("DELIVERY_PAYMENT_TYPE"));
												json.put("DELIVERY_NOTE",                deliveryMapChk.get("DELIVERY_NOTE"));
												json.put("BOUND",                        deliveryMapChk.get("BOUND"));
												json.put("BOUND_TYPE",                   deliveryMapChk.get("BOUND_TYPE"));

												// 수하인 정보
												json.put("CONSIGNEE_NAME_LOC",           deliveryMapChk.get("CONSIGNEE_NAME_LOC"));
												json.put("CONSIGNEE_NAME",               deliveryMapChk.get("CONSIGNEE_NAME"));
												json.put("CONSIGNEE_ZIPCODE",            deliveryMapChk.get("CONSIGNEE_ZIPCODE"));
												json.put("CONSIGNEE_ADDRESS",            deliveryMapChk.get("CONSIGNEE_ADDRESS"));
												json.put("CONSIGNEE_ADDRESS_A",          deliveryMapChk.get("CONSIGNEE_ADDRESS_A"));
												json.put("CONSIGNEE_ADDRESS_B",          deliveryMapChk.get("CONSIGNEE_ADDRESS_B"));
												json.put("CONSIGNEE_TEL",                deliveryMapChk.get("CONSIGNEE_TEL"));
												json.put("CONSIGNEE_HP",                 deliveryMapChk.get("CONSIGNEE_HP"));
												json.put("consigneeNameLocConvert",      deliveryMapChk.get("consigneeNameLocConvert"));
												json.put("consigneeZipcodeLocConvert",   deliveryMapChk.get("consigneeZipcodeLocConvert"));
												json.put("consigneeAddressLocConvert",   deliveryMapChk.get("consigneeAddressLocConvert"));
												json.put("consigneeTelConvert",          deliveryMapChk.get("consigneeTelConvert"));
												json.put("consigneeHpConvert",           deliveryMapChk.get("consigneeHpConvert"));

												// 화주 정보
												json.put("shipperNameConvert",           deliveryMapChk.get("shipperNameConvert"));
												json.put("shipperZipcodeConvert",        deliveryMapChk.get("shipperZipcodeConvert"));
												json.put("shipperAddressLocConvert",     deliveryMapChk.get("shipperAddressLocConvert"));
												json.put("shipperTelConvert",            deliveryMapChk.get("shipperTelConvert"));
												json.put("shipperMobileConvert",         deliveryMapChk.get("shipperMobileConvert"));

												// 화물 치수/무게
												json.put("PIECES",                       deliveryMapChk.get("PIECES"));
												json.put("PIECES_REAL",                  deliveryMapChk.get("PIECES_REAL"));
												json.put("CBM",                          deliveryMapChk.get("CBM"));
												json.put("GWT",                          deliveryMapChk.get("GWT"));

												// 금액
												json.put("AMOUNT_CUR",                   deliveryMapChk.get("AMOUNT_CUR"));

												// 품목 정보
												json.put("commodityItemLocConvert",      deliveryMapChk.get("commodityItemLocConvert"));
												json.put("commodityItem1",               deliveryMapChk.get("commodityItem1"));
												json.put("commodityItem2",               deliveryMapChk.get("commodityItem2"));
												json.put("commodityItem3",               deliveryMapChk.get("commodityItem3"));
												json.put("commodityItem4",               deliveryMapChk.get("commodityItem4"));
												json.put("commodityItem5",               deliveryMapChk.get("commodityItem5"));

												// 등록자 정보
												json.put("registerUserId",               deliveryMapChk.get("registerUserId"));
												json.put("registerUserName",             deliveryMapChk.get("registerUserName"));
												json.put("gmtTime",                      deliveryMapChk.get("gmtTime"));

												Long pushResult = redisTemplate.opsForList().rightPush(key, json.toString());
												if (pushResult != null && pushResult > 0) {
													log.info("[쿠팡반품접수] Redis 적재 완료 OD_JOB_NO={}, DELIVERY_NO={}, KEY={}", deliveryMapChk.get("OD_JOB_NO"), deliveryMapChk.get("DELIVERY_NO"), key);

													//반품요청시간 추가
													Map<String, Object> dataMap = new HashMap<String, Object>();
													dataMap.put("deliveryNo", deliveryMapChk.get("DELIVERY_NO"));
													dataMap.put("statusType", "RETURN_DLV_REQ_DATE");
													dataMap.put("podDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
													orderMapper.updateOdMultiDate(dataMap);

													//  대한통운 반품 POD반영
													// 오더건 POD 업데이트 (TB_ECM_POD_04)
													boolean multiChk = "multi".equals(send.getMultiChk());
													String seqNoId = Util.null2str(deliveryMapChk.get("SEQ_NO_ID"));
													//멀티가 아니거나 멀티시퀀스 1번일경우에만 같이 pod반영
													if (!multiChk || "1".equals(seqNoId)) {

														//오더에도 반품요청시간 추가
														Map<String, Object> updateOdMap = new HashMap<>();
														updateOdMap.put("odJobNo", returnOrderInfo.get("OD_JOB_NO"));
														updateOdMap.put("statusType","RETURN_DLV_REQ_DATE");
														updateOdMap.put("podDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
														orderMapper.updateOdDate(updateOdMap);

														List<Map<String, Object>> podList = new ArrayList<>();
														Map<String, Object> podMap = new HashMap<>();
														podMap.put("odJobNo",  returnOrderInfo.get("OD_JOB_NO"));
														podMap.put("hblNo",    deliveryMapChk.get("HBL_NO"));
														podMap.put("podJobNo", deliveryMapChk.get("ECM504_POD_JOB_NO"));
														podList.add(podMap);
														SqlSession sqlSession = null;
														try {
															sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
															Map<String, Object> podBatchMap = new HashMap<>();
															podBatchMap.put("entCode",         receiver);
//														podBatchMap.put("gmtTime",         (int)(System.currentTimeMillis() / 1000));
															podBatchMap.put("gmtTime", 9);
															podBatchMap.put("registerUserId",  "API");
															podBatchMap.put("updateUserId",    "API");
															podBatchMap.put("entBusinessCode", "");
															podBatchMap.put("solution",        "ECM");
															podBatchMap.put("stockType",       "POD");
															podBatchMap.put("stockSign",       "POD");
															podBatchMap.put("stockSeqLength",  "8");
															podBatchMap.put("resultCode",      "FIXED_SIGN_AND_SEQ_NO");
															podBatchMap.put("stockRemark",     "ECM POD JOB NO");
															podBatchMap.put("podStatus",       "ECM504");
															podBatchMap.put("deliveryCode",    "KOREX");
															podBatchMap.put("podTableName",    "TB_ECM_POD_04");
															Util.accessControlDatabase(podBatchMap, "podTableName|gmtTime", false, null, null, null, null);
															batchProcessOrderPod(sqlSession, podBatchMap, podList, "ECM504", String.valueOf(podBatchMap.get("gmtTime")));
															sqlSession.flushStatements();
															sqlSession.clearCache();
															sqlSession.commit();
														} catch (Exception e) {
															throw e;
														} finally {
															if (sqlSession != null) sqlSession.close();
														}
													}
													//  멀티건 POD 업데이트 (TB_ECM_POD_04_M)
													if (multiChk) {
														List<Map<String, Object>> podListMulti = new ArrayList<>();
														Map<String, Object> podMapMulti = new HashMap<>();
														podMapMulti.put("odJobNo",  returnOrderInfo.get("OD_JOB_NO"));
														podMapMulti.put("podJobNo", deliveryMapChk.get("ECM504_POD_JOB_NO_MULTI"));
														podMapMulti.put("hblNo",    deliveryMapChk.get("DELIVERY_NO"));
														podListMulti.add(podMapMulti);
														SqlSession sqlSessionMulti = null;
														try {
															sqlSessionMulti = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
															Map<String, Object> podBatchMapMulti = new HashMap<>();
															podBatchMapMulti.put("entCode",         receiver);
															podBatchMapMulti.put("gmtTime",         9);
															podBatchMapMulti.put("registerUserId",  "API");
															podBatchMapMulti.put("updateUserId",    "API");
															podBatchMapMulti.put("entBusinessCode", "");
															podBatchMapMulti.put("solution",        "ECM");
															podBatchMapMulti.put("stockType",       "POD");
															podBatchMapMulti.put("stockSign",       "POD");
															podBatchMapMulti.put("stockSeqLength",  "8");
															podBatchMapMulti.put("resultCode",      "FIXED_SIGN_AND_SEQ_NO");
															podBatchMapMulti.put("stockRemark",     "ECM POD JOB NO");
															podBatchMapMulti.put("podStatus",       "ECM504");
															podBatchMapMulti.put("deliveryCode",    "Korex");
															podBatchMapMulti.put("podTableName",    "TB_ECM_POD_04_M");
															Util.accessControlDatabase(podBatchMapMulti, "podTableName|gmtTime", false, null, null, null, null);
															batchProcessOrderPod(sqlSessionMulti, podBatchMapMulti, podListMulti, "ECM504", String.valueOf(podBatchMapMulti.get("gmtTime")));
															sqlSessionMulti.flushStatements();
															sqlSessionMulti.clearCache();
															sqlSessionMulti.commit();
														} catch (Exception e) {
															throw e;
														} finally {
															if (sqlSessionMulti != null) sqlSessionMulti.close();
														}

														//멀티건 배송상태(RETURN_DELIVERY_STATUS)를 ECM504(반품요청)로 반영
														Map<String, Object> multiStatusMap = new HashMap<>();
														multiStatusMap.put("odJobNo",   returnOrderInfo.get("OD_JOB_NO"));
														multiStatusMap.put("deliveryNo", deliveryMapChk.get("DELIVERY_NO"));
														multiStatusMap.put("podStatus", "ECM504");
														orderMapper.updateOdMultiStatus(multiStatusMap);
														log.info("[쿠팡반품접수] 멀티 ECM504 반영 OD_JOB_NO={}, DELIVERY_NO={}", returnOrderInfo.get("OD_JOB_NO"), deliveryMapChk.get("DELIVERY_NO"));
													}
													log.debug("Redis save job->"+ Util.null2str(deliveryMapChk.get("OD_JOB_NO")));
													successedCnt++;
//////////

												} else {
													ifStatusRedis = "ERROR";
													errorMsg = "Redis rightPush 반환값 이상 (result=" + pushResult + ")";
													log.error("Redis 저장 실패 OD_JOB_NO={} / KEY={} / {}", deliveryMapChk.get("OD_JOB_NO"), key, errorMsg);

													Map<String, Object> errorMap = new HashMap<>();
													errorMap.put("odJobNo",  returnOrderInfo.get("OD_JOB_NO"));
													errorMap.put("hblNo",    deliveryMapChk.get("HBL_NO"));
													errorMap.put("errorMsg", errorMsg);
													failedCnt++;
												}

											} catch (Exception e) {
												ifStatusRedis = "ERROR";
												errorMsg = "Redis 저장 예외: " + e.getMessage();
												log.error("Redis 저장 예외 OD_JOB_NO={} / ERROR={}", returnOrderInfo.get("OD_JOB_NO"), e.getMessage(), e);

												Map<String, Object> errorMap = new HashMap<>();
												errorMap.put("odJobNo",  returnOrderInfo.get("OD_JOB_NO"));
												errorMap.put("hblNo",    deliveryMapChk.get("HBL_NO"));
												errorMap.put("errorMsg", errorMsg);
												failedCnt++;
											}
											// ES 로그 기록(레디스기록용)
											try {
												logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
												index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");

												tmpMap = new HashMap<>();
												tmpMap.put("ent_code",            receiver);
												tmpMap.put("customer_code",       "");
												tmpMap.put("solution",            "ECM");
												tmpMap.put("bound",               deliveryMapChk.get("BOUND"));
												tmpMap.put("bound_type",          deliveryMapChk.get("BOUND_TYPE"));
												tmpMap.put("hbl_no",              deliveryMapChk.get("HBL_NO"));
												tmpMap.put("if_service_code",     "KOREX");
												tmpMap.put("if_doc_code",         "SEND_RETURN");
												tmpMap.put("if_doc_name",         "대한통운반품");
												tmpMap.put("if_doc_reference_no", deliveryMapChk.get("MBL_NO"));
												tmpMap.put("if_vender",           deliveryMapChk.get("AGENT_CODE"));
												tmpMap.put("if_send_recv_type",   "S");
												tmpMap.put("if_send_recv_method", "redis");
												tmpMap.put("if_file_path",        "");
												tmpMap.put("if_file_name",        "");
												tmpMap.put("if_status",           ifStatusRedis);
												tmpMap.put("if_status_info",      errorMsg);
												tmpMap.put("if_sender_id",        deliveryMapChk.get("AGENT_CODE"));
												tmpMap.put("if_sender_name",      "");
												tmpMap.put("if_receiver_id",      "");
												tmpMap.put("if_receiver_name",    "");
												tmpMap.put("if_date",             DateTime.getDate("yyyy-MM-dd"));
												tmpMap.put("if_date_time",        DateTime.getDate("hh:mm:ss"));
												tmpMap.put("if_remark",           "KOREX");
												tmpMap.put("register_user_id",    deliveryMapChk.get("registerUserId"));
												tmpMap.put("register_user_name",  deliveryMapChk.get("registerUserName"));
												tmpMap.put("register_date_time",  System.currentTimeMillis());
												tmpMap.put("es_job_no",           logId);

												indexApi = new IndexApi(client);
												if (!indexApi.existIndex(index)) indexApi.createIndex(index);

												documentApi = new DocumentApi(client);
												documentApi.createDocument(index, logId, tmpMap);

											} catch (Exception e) {
												log.error("ES 로그 기록 실패 OD_JOB_NO={} / ERROR={}", returnOrderInfo.get("OD_JOB_NO"), e.getMessage(), e);
											}

											log.info("###### 대한통운 반품요청 종료 - 전체:{} / 성공:{} / 실패:{} ##########", totalCnt, successedCnt, failedCnt);
										}else {
											log.debug("존재하지않는 건입니다");
										}
									}
								}else {
									accessFlag = "V";
									returnMessage += "/itemList: 아이템 정보가 없습니다.";
								}
							}

						}
        			}
				} catch(Exception e){
					accessFlag = "E";
					returnMessage = e.toString();
				}
        	}
	 		
	 		if("Y".equals(accessFlag)){
	 			returnCode = "SUCCESS";
	 			returnMessage = "";
	 			if_status = "RECV";
	 			if_status_info = "Receive Complete";
	 			log.info("[쿠팡반품접수] END - 성공(SUCCESS) orderId={}, requestId={}, 소요시간={}ms",
	 					Util.null2str(order.getOrderId()), Util.null2str(order.getRequestId()), (System.currentTimeMillis() - procStart));
 			}else{
	 			returnCode = "FAIL";
	 			if_status = "ERROR";
	 			if_status_info = "Receive Fail"; 
	 			if("N".equals(accessFlag)){
		 			returnMessage = "Wrong AccessKey";
	 			}else if("X".equals(accessFlag)){
		 			returnMessage = "Not a service provider";
	 			}else if("V".equals(accessFlag)){
		 			returnMessage = " 에러가 발생한 부분 => " + returnMessage;
	 			}
	 			log.info("[쿠팡반품접수] END - 실패(accessFlag={}) orderId={}, requestId={}, 사유={}, 소요시간={}ms",
	 					accessFlag, Util.null2str(order.getOrderId()), Util.null2str(order.getRequestId()), returnMessage, (System.currentTimeMillis() - procStart));
 			}
	 		
	 		// history 등록 
			tmpMap = new HashMap<String, Object>();
			logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
			index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
			
	    	// 처리결과에 따른 로그 기록
			tmpMap.clear();
			tmpMap.put("ent_code", receiver); 		 					// 도입사코드
			tmpMap.put("customer_code", sender); 		 				// 고객코드
			tmpMap.put("solution", "ECM");      		 				// SOLUTION
			tmpMap.put("bound", boundEs);		 	 					// 바운드 (AIR)
			tmpMap.put("bound_type", boundTypeEs);	 					// 바운드타입
			tmpMap.put("hbl_no", Util.null2str(hblNo));				 	// HBL_NO
			tmpMap.put("if_service_code", bound);
			tmpMap.put("if_doc_code", "COUPANG_ORDER_API");
			tmpMap.put("if_doc_name", "쿠팡 오더 API");
			tmpMap.put("if_doc_reference_no", "");	 					// 문서번호 
			tmpMap.put("if_vender", sender);							// 중개사업자
			tmpMap.put("if_send_recv_type", "R");						// 송수신타입(S:SEND, R:RECV)
			tmpMap.put("if_send_recv_method", "JSON To DB");			// 송수신방법
			tmpMap.put("if_file_path", "");			 					// 파일경로
			tmpMap.put("if_file_name", "");			 					// 파일명
			tmpMap.put("if_status", if_status);			 				// 처리상태
			tmpMap.put("if_status_info", if_status_info);		 		// 송수신상태_설명
			tmpMap.put("if_sender_id", sender);							// 송신식별자
			tmpMap.put("if_sender_name", "");		 					// 송신자명
			tmpMap.put("if_receiver_id", "");		 					// 수신식별자
			tmpMap.put("if_receiver_name", "");	     					// 수신자명
			tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));		// DATE
			tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));	// DATE_TIME
			tmpMap.put("if_remark", returnMessage);			 			// 비고( 에러로그 등)
			tmpMap.put("register_user_id", "API");
			tmpMap.put("register_user_name", "API");
			tmpMap.put("register_date_time", System.currentTimeMillis());
			tmpMap.put("es_job_no", logId);
			tmpMap.put("job_log", new Gson().toJson(order));
			
			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			documentApi = new DocumentApi(client);
			documentApi.createDocument(index, logId, tmpMap);
			
			returnData.setCode(returnCode);
			returnData.setMessage(returnMessage);
		}catch (Exception e) {
			log.error(Util.getPrintStacTraceString(e));
			returnData.setCode("FAIL");
			returnData.setMessage("System Error.");
		}
		
		return returnData;
	}

	/**
	 * 쿠팡 PCCC API
	 * @param order
	 * @return InterfaceReturnDataPcccVE
	 * @throws Exception
	 */
	public InterfaceReturnDataPcccVE coupangOrderPcccInfo(Map<String, Object> recvPccc, HttpServletRequest req) throws Exception{
		CoupangPccc pccc = (CoupangPccc) recvPccc.get("pccc");
		
//		InterfaceResultVE result = new InterfaceResultVE();
		InterfaceReturnDataPcccVE returnData = new InterfaceReturnDataPcccVE();
		
		Results results = null;
		List<Juso> jusoList = null;
		String consigneeAddress = "";
		
		HttpSession session = req.getSession(true);
		String receiver = (String) session.getAttribute("db_key");//(Util.null2str(order.getAccessIdentifier().getReceiverIdentifier())).toUpperCase();
		
		
		String id = DateTime.getDate("yyyyMMddhhmmssSSS") + "-" + receiver;
		String index = "ecm-customs-verification-result-" + DateTime.getDate("yyyy.MM.dd");
		
		List<JSONObject> redisList = new ArrayList<JSONObject>();
		
		DocumentApi documentApi = new DocumentApi(client);
		IndexApi indexApi = new IndexApi(client);
		
		Map<String, Object> esMap = new HashMap<String, Object>();
		esMap.put("ent_code", receiver);
		esMap.put("id", id);
		esMap.put("order", "");
		esMap.put("status", "wait");
		esMap.put("total_count", 1);
		esMap.put("register_date_time", System.currentTimeMillis());
		esMap.put("register_user_id", "API");
		esMap.put("register_user_name", "API");
		esMap.put("log", new Gson().toJson(pccc));
		
		Map<String, Object> tmpMap = null;
		String logId = "";
		
		String requestId = Util.null2str(pccc.getRequestId());
		String gubun = Util.null2str(recvPccc.get("gubun"));
		String poNo = Util.null2str(pccc.getCoupangOrderId());
		String poDate = Util.null2str(pccc.getOrderedAt());
		String poName = Util.null2str(pccc.getOrdererName());
		String customsId = Util.null2str(pccc.getPccc());
		String consigneeHp = Util.null2str(pccc.getReceiverPhoneNumber());
		String consigneeName = Util.null2str(pccc.getReceiverName());
		String consigneeZipcode = Util.null2str(pccc.getZipcode());
//		String commodityItem = Util.null2str(pccc.getOrdererName());
		String commodityItem = ""; //추후수정예정
		
		if("".equals(requestId) || requestId == null) {
			returnData.setCode("10002");
			returnData.setMessage(" /requestId= 필수입니다.\n");
			returnData.setData("error");
			return returnData;
		}
		
		//수하인 우편번호로 주소변환
		tmpMap = new HashMap<String, Object>();
		if(!"".equals(consigneeZipcode)) {
			tmpMap.put("CONSIGNEE_ADDRESS_LOC", consigneeZipcode);
			results = zipcodeConvert.getAddr_OpenAPI(tmpMap, 0);
			if(results != null) {
				jusoList = results.getJuso();
			}
			if(jusoList != null) {
				consigneeAddress = jusoList.get(0).getRoadAddr();
			}
		}
		
		try {
			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			//중복체크
			Map<String, Object> dupMap = new HashMap<>();
			dupMap.put("requestId", requestId);
			dupMap.put("gubun", gubun);
			int dupCnt = interfaceMapperVE.selectDupReqId(dupMap);
			if(dupCnt > 0 ) {
				esMap.put("fail_count", 1);
				returnData.setCode("10001");
				returnData.setMessage("duplicate requestId");
				returnData.setData("error");
			}else {
				Map<String, Object> params = new HashMap<String, Object>(); 
				String customsIdJobNo = DateTime.getDate("yyyyMMddHHmmss") + Util.getRandomNum(5) + Util.setSeqNo(1);
				String hblNo = DateTime.getDate("yyyyMMdd")+"_"+ Util.getRandomNum(9);
				
				params.put("customsIdJobNo", customsIdJobNo);
				params.put("entCode", receiver);
				params.put("requestId", requestId);
				params.put("poNo", poNo);
				params.put("hblNo", hblNo);
				params.put("customsId", customsId);
				params.put("consigneeHp", consigneeHp);
				params.put("consigneeName", consigneeName);
				params.put("consigneeZipcode", consigneeZipcode);
				params.put("poName", poName);
				params.put("poDate", poDate);
				params.put("commodityItem", commodityItem);
				params.put("consigneeAddress", consigneeAddress);
				params.put("custCodeLv1", "COUPANG-HQ");
				params.put("inputMethod", "COUPANG");
				if("cancel".equals(gubun)) {
					int cancelData = interfaceMapperVE.selectDupReqId(params);
					if(cancelData > 0) {
						interfaceMapperVE.updateCustomsCancelInfo(params);
					}else{
						esMap.put("fail_count", 1);
						returnData.setCode("10002");
						returnData.setMessage("not exist requestId");
						returnData.setData("error");
					}
				}else {
					String pcccValidate = OrderValidateVE.validatePcccInterface(pccc);
					if(!Util.isEmpty(pcccValidate)) {
						esMap.put("fail_count", 1);
						
						returnData.setCode("10002");
						returnData.setMessage(pcccValidate);
						returnData.setData("error");
					}else {
						esMap.put("fail_count", 0);
						interfaceMapperVE.insertCustomsInfo(params);
						JSONObject json = new JSONObject();
						json.put("resultIndex", index);
						json.put("resultId", id);
						json.put("entCode", receiver);
						json.put("entBusinessCode", "");
						json.put("userId", "API");
						json.put("userName", "API");
						json.put("odJobNo", customsIdJobNo);
						json.put("masterOdJobNo", "");
						json.put("mblNo", "");
						json.put("hblNo", hblNo);
						json.put("nameType", "");
						
						json.put("REQUEST_ID", requestId);
						json.put("PO_NO", poNo);
						json.put("CUSTOMS_ID", customsId);
						json.put("CONSIGNEE_HP", consigneeHp);
						json.put("CONSIGNEE_NAME", consigneeName);
						json.put("CONSIGNEE_ZIPCODE", consigneeZipcode);
						json.put("COMMODITY_ITEM", commodityItem);
						json.put("CONSIGNEE_ADDRESS", consigneeAddress);
						json.put("PO_NAME", poName);
						json.put("PO_DATE", poDate);
						json.put("inputMethod", "COUPANG");
						json.put("verifySeq", "1");
						redisList.add(json);
						
						// Batch 작업을 위해 Redis에 저장
						String key = "E-COMMERCE_CUSTOMS_CHECK_" + receiver;
						ListOperations<String, String> listOperations = redisTemplate.opsForList();
						listOperations.rightPush(key, redisList.toString());
						
					}
					returnData.setCode("10000");
					returnData.setMessage("success");
					returnData.setData("success");
				}
			}
			// 결과 조회를 위해 ES에 저장
			documentApi.createDocument(index, id, esMap);
		}catch (Exception e) {
			returnData.setCode("10002");
			returnData.setMessage(e.toString());
			returnData.setData("error");
		}
		
		// history 등록
		tmpMap = new HashMap<String, Object>();
		logId = requestId;
		String intfaceIndex = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");

    	// 처리결과에 따른 로그 기록
        tmpMap.clear();
		tmpMap.put("ent_code", receiver); 		 				// 도입사코드
		tmpMap.put("customer_code", ""); 		 				// 고객코드
		tmpMap.put("solution", "ECM");      		 			// SOLUTION
		tmpMap.put("bound", "COM");		 	 					// 바운드 (AIR)
		tmpMap.put("bound_type", "COM");	 						// 바운드타입
		tmpMap.put("hbl_no", Util.null2str(""));				// HBL_NO
		tmpMap.put("if_service_code", "");
		if("cancel".equals(gubun)) {
			tmpMap.put("if_doc_code", "PCCC_CANCEL_API");
			tmpMap.put("if_doc_name", "PCCC취소수신API");
		}else {
			tmpMap.put("if_doc_code", "PCCC_API");
			tmpMap.put("if_doc_name", "PCCC수신API");
		}
		tmpMap.put("if_doc_reference_no", Util.null2str(poNo));	// 문서번호
		tmpMap.put("if_vender", "");							// 중개사업자
		tmpMap.put("if_send_recv_type", "R");					// 송수신타입(S:SEND, R:RECV)
		tmpMap.put("if_send_recv_method", "JSON To DB");		// 송수신방법
		tmpMap.put("if_file_path", "");			 				// 파일경로
		tmpMap.put("if_file_name", "");			 				// 파일명
		tmpMap.put("if_status", "");			 				// 처리상태
		tmpMap.put("if_status_info", "");		 				// 송수신상태_설명
		tmpMap.put("if_sender_id", "");							// 송신식별자
		tmpMap.put("if_sender_name", "");		 					// 송신자명
		tmpMap.put("if_receiver_id", "");		 					// 수신식별자
		tmpMap.put("if_receiver_name", "");	     					// 수신자명
		tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));		// DATE
		tmpMap.put("if_date_time", DateTime.getDate("HH:mm:ss"));	// DATE_TIME
		tmpMap.put("if_remark", "");			 					// 비고( 에러로그 등)
		tmpMap.put("register_user_id", "API");
		tmpMap.put("register_user_name", "API");
		tmpMap.put("register_date_time", System.currentTimeMillis());
		tmpMap.put("es_job_no", logId);
		
		List<Map<String, Object>> jobLogList = new ArrayList<Map<String, Object>>();
		Map<String, Object> jobLogMap = new HashMap<String, Object>();
		jobLogMap.put("recv_date", DateTime.getDate("yyyy-MM-dd HH:mm:ss"));
		jobLogMap.put("recv_data", pccc);

		indexApi = new IndexApi(client);
		if (!indexApi.existIndex(intfaceIndex))	indexApi.createIndex(intfaceIndex);
		documentApi = new DocumentApi(client);
		
		GetResponse response = documentApi.getDocument(intfaceIndex, logId);
		if(response == null) {
			jobLogList.add(jobLogMap);
			tmpMap.put("file_data", new Gson().toJson(jobLogList));
			documentApi.createDocument(intfaceIndex, logId, tmpMap);
		}else {
			tmpMap.clear();
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.setSerializationInclusion(Include.NON_NULL);
			
			InterfaceLogs interfacelog = mapper.convertValue(response.getSourceAsMap(), InterfaceLogs.class);
			String fileData = interfacelog.getFileData();
			jobLogList = mapper.readValue(fileData, new TypeReference<List<Map<String, Object>>>() {});
			jobLogList.add(jobLogMap);
            
			tmpMap.put("file_data", new Gson().toJson(jobLogList));
			tmpMap.put("update_user_id", "API");
			tmpMap.put("update_user_name", "API");
			tmpMap.put("update_date_time", System.currentTimeMillis());
			
			documentApi.updateDocument(intfaceIndex, logId, tmpMap);
		}
		
		return returnData;
	}

	/**
	 * POD 이력 배치 insert/update 처리(반품처리용)
	 */
	public void batchProcessOrderPod(SqlSession sqlSession, Map<String, Object> tmpMap,
									 List<Map<String, Object>> odJobNoList, String podStatus, String gmtTime) throws Exception {

		OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
		Map<String, Object> dataMap = new HashMap<String, Object>();
		List<Map<String, Object>> insertPodList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> updatePodList = new ArrayList<Map<String, Object>>();
		Map<String, Object> tmpInsertPodMap = null;
		int updateCnt = 1;
		int seq = 1;

		for (Map<String, Object> odJobNoMap : odJobNoList) {
			dataMap.put("odJobNo", odJobNoMap.get("odJobNo"));
			dataMap.put("podStatus", podStatus);
			dataMap.put("gmtTime", 9);
			dataMap.put("noCheckLastPodStatus", "Y");
			orderBatchMapper.updateOrderLastPod(dataMap);
			tmpInsertPodMap = new HashMap<String, Object>();
			if (odJobNoMap.get("podJobNo") != null) {
				tmpInsertPodMap.put("podJobNo", odJobNoMap.get("podJobNo"));
				updatePodList.add(tmpInsertPodMap);
			} else {
				tmpInsertPodMap.put("odJobNo", odJobNoMap.get("odJobNo"));
				tmpInsertPodMap.put("blNo", odJobNoMap.get("hblNo"));
				tmpInsertPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				insertPodList.add(tmpInsertPodMap);
			}

			if ((updateCnt++ % 1000) == 0) {
				sqlSession.flushStatements();
				sqlSession.clearCache();
			}
		}

		updateCnt = 1;
		for (Map<String, Object> insertPodMap : insertPodList) {
			tmpMap.put("odJobNo", insertPodMap.get("odJobNo"));
			tmpMap.put("blNo", insertPodMap.get("blNo"));
			tmpMap.put("podJobNo", insertPodMap.get("podJobNo"));
			orderBatchMapper.insertOrderPod(tmpMap);
			if ((updateCnt++ % 1000) == 0) {
				sqlSession.flushStatements();
				sqlSession.clearCache();
			}
		}

		updateCnt = 1;
		for (Map<String, Object> updatePodMap : updatePodList) {
			tmpMap.put("podJobNo", updatePodMap.get("podJobNo"));
			orderBatchMapper.updateOrderPod(tmpMap);
			if ((updateCnt++ % 1000) == 0) {
				sqlSession.flushStatements();
				sqlSession.clearCache();
			}
		}
	}
}

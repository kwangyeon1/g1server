package org.yht.exps.express.service.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringEscapeUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerMapping;
import org.xml.sax.SAXException;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.api.InterfaceMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.model.api.AccessIdentifier;
import org.yht.exps.express.model.api.AirInterface;
import org.yht.exps.express.model.api.CustomsInfo;
import org.yht.exps.express.model.api.CustomsInfoJsonVE;
import org.yht.exps.express.model.api.CustomsInterface;
import org.yht.exps.express.model.api.EcmRequestBLnterface;
import org.yht.exps.express.model.api.EcmResult;
import org.yht.exps.express.model.api.EcmResult.EcmReturn;
import org.yht.exps.express.model.api.EmsInterface;
import org.yht.exps.express.model.api.EmsRequestBLnterface;
import org.yht.exps.express.model.api.EmsResult;
import org.yht.exps.express.model.api.EmsResult.EmsReturn;
import org.yht.exps.express.model.api.HblInterface;
import org.yht.exps.express.model.api.HblReturnTracking;
import org.yht.exps.express.model.api.HblTrackingInterface;
import org.yht.exps.express.model.api.HblTrackingResult;
import org.yht.exps.express.model.api.InterfaceResult;
import org.yht.exps.express.model.api.InterfaceResultJson;
import org.yht.exps.express.model.api.InterfaceResultJson.InterfaceResultDataJson;
import org.yht.exps.express.model.api.InterfaceReturnData;
import org.yht.exps.express.model.api.InterfaceReturnDataJson;
import org.yht.exps.express.model.api.InterfaceTracking;
import org.yht.exps.express.model.api.InventoryInfo;
import org.yht.exps.express.model.api.InventoryInfoJson;
import org.yht.exps.express.model.api.LxpXrayIntf;
import org.yht.exps.express.model.api.MultiInfo;
import org.yht.exps.express.model.api.MultiInfoJson;
import org.yht.exps.express.model.api.OceanInterface;
import org.yht.exps.express.model.api.OrderInterfaceJson;
import org.yht.exps.express.model.api.OrderInterfaceJsonDKX;
import org.yht.exps.express.model.api.OrderInvoice;
import org.yht.exps.express.model.api.ReturnTrackingData;
import org.yht.exps.express.model.api.ReturnTrackingDataJson;
import org.yht.exps.express.model.api.TrackingInterfaceJson;
import org.yht.exps.express.model.api.TrackingResult;
import org.yht.exps.express.model.api.TrackingResultJson;
import org.yht.exps.express.model.api.Tracking_interface;
import org.yht.exps.express.model.api.hms.Hms;
import org.yht.exps.express.model.api.hms.HmsPackage;
import org.yht.exps.express.model.api.hms.HmsResponse;
import org.yht.exps.express.model.api.hms.HmsResult;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.service.bms.BmsSendUtils;
import org.yht.exps.express.util.HttpUtil;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.FileUtil;
import org.yht.util.Util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.gson.Gson;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.ubireport.viewer.report.preview.UbiViewer;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


@Slf4j
@Service
@Transactional
public class InterfaceService {
		
	@Autowired
	private CustomProperties customProperties;
	
	@Autowired
	InterfaceMapper interfaceMapper;
	@Autowired
	OrderMapper orderMapper;
	@Autowired
	RestHighLevelClient client;
	
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	BmsSendUtils bmsSendUtils;
	
	//로컬,테스트,운영 구분
	@Value("${spring.profiles.active}")
	private String profile;
	@Value("${custom.backup-dir}")
	private String fileBackupDir_;
	@Value("${custom.edi-send-yn}")
	private String ediSendYn;

	/**
	 * ECM 항공(AI/AO) 오더 생성 API
	 * @param order
	 * @return InterfaceResult
	 * @throws Exception
	 */
	public InterfaceResult registAirOrderInfo(AirInterface order, HttpServletRequest req, String entCode) throws Exception {
		
		InterfaceResult result = new InterfaceResult();
		InterfaceReturnData returnData = new InterfaceReturnData();
		try {
			
			log.info("registAirOrderInfo start @@@@@@@@@@@@@@@@@@@@@@@@@@");
			
			String requestUrl = (String)req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE); // URL에 따른 분기처리
//			Calendar cal = Calendar.getInstance();
//			cal.setTime(new Date(System.currentTimeMillis()));
//			String format = "yyyyMMdd_HHmmss.SSS";
//			String date = new SimpleDateFormat(format).format(cal.getTime());
			String format = "yyyyMMdd_HHmmss.SSSSSSS";
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
			String date = now.format(dtf);
			
			// XML 스키마 검증파일(XSD)
			SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			String xsd = "";
			File schemaLocation = null;
			Schema schema = null;
			Validator validator = null;
			FileReader fr = null;
	        BufferedReader br = null;
			String errorValidationLine = "";
			
			
			String accessKey = null;
			String sender = null;
			String receiver = null;
			
			if(entCode == null){
				accessKey = (Util.null2str(order.getAccessIdentifier().getAccessKey())).toUpperCase();
				sender = (Util.null2str(order.getAccessIdentifier().getSenderIdentifier())).toUpperCase();
				receiver = (Util.null2str(order.getAccessIdentifier().getReceiverIdentifier())).toUpperCase();
			}else {	/** UPS 토큰 인증 방식 */
				sender = "";
				receiver = entCode;
			}
			
			if(requestUrl.contains("registUAIOrderInfo")) {
				if("ups2".equals(profile)){
					receiver = "UPSXK";
				}else{
					receiver = "YHTXX";
				}
			}
			
			String returnCode = "";
			String returnMessage = "";
			String hblNo = "";
			String accessFlag = "N";
			String path = "";
			String apiPath = customProperties.getInterfaceDir() + "api/" + receiver + "/";
			String infileName = receiver + "_" + sender + "_" + date + UUID.randomUUID().toString().substring(0, 8) + ".xml";
			String bound = "";
			String boundEs = "AIR";
			String boundTypeEs = "";
			String logId = ""; // ES 로그용 아이디
			String index = ""; // ES 로그용 index
			String if_status = "RS"; // 처리상태값
			String if_status_info = "Receive Start"; // 처리상태설명
			
			Map<String, Object> tmpMap = null;
			IndexApi indexApi = null;
			DocumentApi documentApi = null;
			
			Map<String, Object> map = new HashMap<String, Object>();
			if(requestUrl.contains("registAIOrderInfo")) {
				xsd = customProperties.getXsdDir() + "ai.xsd";
				bound = "AI";
				boundTypeEs = "IMP";
			}else if(requestUrl.contains("registAOOrderInfo")) {
				xsd = customProperties.getXsdDir() + "ao.xsd";
				bound = "AO";
				boundTypeEs = "EXP";
			}else if(requestUrl.contains("registUAIOrderInfo") || requestUrl.contains("registUTAIOrderInfo")) {
				xsd = customProperties.getXsdDir() + "aiUps.xsd";
				bound = "AI";
				boundTypeEs = "IMP";
			}else if(requestUrl.contains("registUAOOrderInfo") || requestUrl.contains("registUTAOOrderInfo")) {
				xsd = customProperties.getXsdDir() + "aoUps.xsd";
				bound = "AO";
				boundTypeEs = "EXP";
			}
			
			path = apiPath + "RECV/" +bound + "/"; // Interface PATH + 업체코드 경로에 저장
			
			map.put("ReceiverId", receiver);
			map.put("SenderId", sender);
			map.put("UseYN", "Y");		
			
			if(entCode == null) {
				// Interface key 조회(ENT_CODE(ReceiverId) ,CUSTOMER_CODE(SenderId) ,ACCESS_KEY ,USE_YN) 	
				Map<String, Object> deliveryInfo = (Map<String, Object>)interfaceMapper.selectInterfaceInfo(map);
				if(deliveryInfo != null){
					if(accessKey.equals(Util.null2str(deliveryInfo.get("ACCESS_KEY")))){
						accessFlag = "Y"; // 요청자가 보낸 값과 동일하면 Y
					}else{
						accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
					}
				}else{
					accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
				}
			}else{	/** UPS는 토큰검증 사용 */
				accessFlag = "Y";
			}
			File pathDir = new File(path);
			if (pathDir.exists() == false)	pathDir.mkdirs();
			File file = new File(path + infileName);
			
	 		StringWriter writer = new StringWriter();
	 		if(order.getOrderManifest() != null && order.getOrderManifest().size() > 0 && order.getOrderManifest().get(0).getOrderInfo() != null){
	 			hblNo = order.getOrderManifest().get(0).getOrderInfo().getHBLNo();
	 		}
	        try{
	        	if("Y".equals(accessFlag)){
	        		
	        		
	        		log.info("registAirOrderInfo parsing start @@@@@@@@@@@@@@@@@@@@@@@@@@");
	        		
	    			// 오더생성 API 사용량 측정을 위해 ORDER_CNT 업데이트 
	        		map.put("Type", "order"); 	// Type : 오더생성 => order, 대한통운 집배코드 (주소정제) => korexConvert 
	//    			updateInterfaceCnt(map);	// 220622 주석처리
	        		
	        		JAXBContext jaxbContext = JAXBContext.newInstance(AirInterface.class);
	                Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
	                
	                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	
	        		jaxbMarshaller.marshal(order, file);
	                jaxbMarshaller.marshal(order, writer);
	                
	    			schemaLocation = new File(xsd);
	    			schema = factory.newSchema(schemaLocation);
	    			validator = schema.newValidator();
	    			
	    			Source source = new StreamSource(file);
	    			validator.validate(source);
	        	}
			} catch(SAXException e){
	//			log.error(Util.getPrintStacTraceString(e));
				String[] errStrArray = e.toString().split("; ");
				if(errStrArray.length > 4) {
					accessFlag = "V";
					String line = null;
					int lineCnt = 0;
					String lineRow = errStrArray[2].split(": ")[1].trim();
					String msg = errStrArray[4].trim();
					
					returnMessage = "LINE (" + lineRow + ") : " + msg;
					
					fr = new FileReader(file);
					br = new BufferedReader(fr);
					
					while((line = br.readLine()) != null){
						lineCnt++;
						if(line.contains("HBLNo")){
							hblNo = line.replace("<HBLNo>", "").replace("</HBLNo>", "").trim();
						}
						if(lineCnt == Integer.parseInt(lineRow)){
							errorValidationLine = line.trim();
							break;
						}
					}
				}else{
					accessFlag = "E";
					returnMessage = e.toString();
				}
			} catch(Exception e){
	//			log.error(Util.getPrintStacTraceString(e));
				accessFlag = "E";
				returnMessage = e.toString();
			}finally{
				if(br != null){try{br.close();}catch(Exception e){log.error(e.getMessage());}}
				if(fr != null){try{fr.close();}catch(Exception e){log.error(e.getMessage());}}
			}
	        
	        if("Y".equals(accessFlag)){
	        	path = apiPath + "SUCC/" + bound + "/";
	        	pathDir = new File(path);
	    		if (pathDir.exists() == false)	pathDir.mkdirs();
	        	File fileToMove = new File(path + infileName);
	 	        file.renameTo(fileToMove);
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
	 	 			path = apiPath + "ERROR/" + bound + "/";
	 	 			pathDir = new File(path);
	 	 			if (pathDir.exists() == false)	pathDir.mkdirs();
	 	 			infileName = "ErrorValidation_" + infileName;
	 	 			File fileToMove = new File(path + infileName);
	 	 	        file.renameTo(fileToMove);
	 	 			returnMessage = returnMessage + " 에러가 발생한 부분 => " + errorValidationLine;
	 	 		}else if("E".equals(accessFlag)){
	 	 			path = apiPath + "ERROR/" + bound + "/";
	 	 			pathDir = new File(path);
	 	 			if (pathDir.exists() == false)	pathDir.mkdirs();
	 	 			infileName = "Error_" + infileName;
	 	 			File fileToMove = new File(path + infileName);
	 	 	        file.renameTo(fileToMove);
	 	 		}
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
			tmpMap.put("if_doc_code", "ORDER_API");
			tmpMap.put("if_doc_name", "오더API");
			tmpMap.put("if_doc_reference_no", "");	 					// 문서번호 
			tmpMap.put("if_vender", sender);							// 중개사업자
			tmpMap.put("if_send_recv_type", "R");						// 송수신타입(S:SEND, R:RECV)
			tmpMap.put("if_send_recv_method", "XML To DB");				// 송수신방법
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
			
			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			documentApi = new DocumentApi(client);
			documentApi.createDocument(index, logId, tmpMap);
	        
			returnData.setReturnCode(returnCode);
			returnData.setReturnMessage(returnMessage);
			
			result.setReturnData(returnData);		
		}catch(Exception e){
			log.error(Util.getPrintStacTraceString(e));
			returnData.setReturnCode("FAIL");
			returnData.setReturnMessage("System Error.");
			result.setReturnData(returnData);
		}
		
		log.info("registAirOrderInfo end @@@@@@@@@@@@@@@@@@@@@@@@@@");
		
		return result;
	}
	
	/**
	 * 인증 방식에 따라 문서 전송자 정보(AccessIdentifier) 유효성 검증 
	 * @param identifier
	 * @param type (U : 사용자 인증 방식 / T : 토큰 인증 방식)
	 * @return
	 */
	public String checkAccessIdentifierUps(AccessIdentifier identifier, String type) {
		Map<String, Object> map = new HashMap<String, Object>();
		String accessKey, sender, receiver = null;
		String errMsg = "";
		/** 사용자 인증 방식 - 문서 전송자 정보 필수값 */
		if(type.equals("U")) {
			if(identifier == null) {
				errMsg = "Please enter the AccessIdentifier.";
			}else {
				sender = (Util.null2str(identifier.getSenderIdentifier())).toUpperCase();
				receiver = (Util.null2str(identifier.getReceiverIdentifier())).toUpperCase();
				accessKey = (Util.null2str(identifier.getAccessKey())).toUpperCase();
				
				if(sender.length() < 1 || sender.length() > 10) {
					errMsg = "Please check the SenderIdentifier.";
				}else if(receiver.length() < 1 || receiver.length() > 5) {
					errMsg = "Please check the ReceiverIdentifier.";
				}else if(accessKey.length() < 1 || accessKey.length() > 64) {
					errMsg = "Please check the AccessKey.";
				}
			}
			
		/** 토큰 인증 방식 - 문서 전송자 정보 입력받지 않음 */
		}else {
			if(identifier != null) {
				errMsg = "You cannot enter the AccessIdentifier.";
			}
		}
		
		return errMsg;
	}

	
	/**
	 * ECM 해상(OI/OO) 오더 생성 API
	 * @param order
	 * @return InterfaceResult
	 * @throws Exception
	 */
	public InterfaceResult registOceanOrderInfo(OceanInterface order, HttpServletRequest req) throws Exception {
		InterfaceResult result = new InterfaceResult();
		InterfaceReturnData returnData = new InterfaceReturnData();
		try {
			String requestUrl = (String)req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE); // URL에 따른 분기처리
//			Calendar cal = Calendar.getInstance();
//			cal.setTime(new Date(System.currentTimeMillis()));
//			String format = "yyyyMMdd_HHmmss.SSS";
//			String date = new SimpleDateFormat(format).format(cal.getTime());
			String format = "yyyyMMdd_HHmmss.SSSSSSS";
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
			String date = now.format(dtf);
			
			// XML 스키마 검증파일(XSD)
			SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			String xsd = "";
			File schemaLocation = null;
			Schema schema = null;
			Validator validator = null;
			FileReader fr = null;
	        BufferedReader br = null;
			String errorValidationLine = "";
			
			String accessKey = (Util.null2str(order.getAccessIdentifier().getAccessKey())).toUpperCase();
			String sender = (Util.null2str(order.getAccessIdentifier().getSenderIdentifier())).toUpperCase();
			String receiver = (Util.null2str(order.getAccessIdentifier().getReceiverIdentifier())).toUpperCase();
			
			String returnCode = "";
			String returnMessage = "";
			String hblNo = "";
			String accessFlag = "N";
			String path = "";
			String apiPath = customProperties.getInterfaceDir() + "api/" + receiver + "/";
			String infileName = receiver + "_" + sender + "_" + date + UUID.randomUUID().toString().substring(0, 8) + ".xml";
			String bound = "";
			String boundEs = "SEA";
			String boundTypeEs = "";
			String logId = ""; // ES 로그용 아이디
			String index = ""; // ES 로그용 index
			String if_status = "RS"; // 처리상태값
			String if_status_info = "Receive Start"; // 처리상태설명		
			
			
			Map<String, Object> map = new HashMap<String, Object>();
			Map<String, Object> tmpMap = null;
			IndexApi indexApi = null;
			DocumentApi documentApi = null;
			
			if(requestUrl.contains("registOIOrderInfo")) {
				xsd = customProperties.getXsdDir() + "oi.xsd";
				bound = "SI";
				boundTypeEs = "IMP";
			}else if(requestUrl.contains("registOOOrderInfo")) {
				xsd = customProperties.getXsdDir() + "oo.xsd";
				bound = "SO";
				boundTypeEs = "EXP";
			}
			
			path = apiPath + "RECV/" +bound + "/"; // Interface PATH + 업체코드 경로에 저장
	
			map.put("ReceiverId", receiver);
			map.put("SenderId", sender);
			map.put("UseYN", "Y");		
			
			 // Interface key 조회(ENT_CODE(ReceiverId) ,CUSTOMER_CODE(SenderId) ,ACCESS_KEY ,USE_YN) 
			Map<String, Object> deliveryInfo = (Map<String, Object>)interfaceMapper.selectInterfaceInfo(map);
			
			if(deliveryInfo != null){
				if(accessKey.equals(Util.null2str(deliveryInfo.get("ACCESS_KEY")))){
					accessFlag = "Y"; // 요청자가 보낸 값과 동일하면 Y
				}
			}else{
				accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
			}		
			
			File pathDir = new File(path);
			if (pathDir.exists() == false)	pathDir.mkdirs();
			File file = new File(path + infileName);
			
	 		StringWriter writer = new StringWriter();
	 		if(order.getOrderManifest_ocean() != null && order.getOrderManifest_ocean().size() > 0 && order.getOrderManifest_ocean().get(0).getOrderInfo_ocean() != null){
	 			hblNo = order.getOrderManifest_ocean().get(0).getOrderInfo_ocean().getHBLNo();
	 		}
	        try{
	        	if("Y".equals(accessFlag)){
	        		
	    			// 오더생성 API 사용량 측정을 위해 ORDER_CNT 업데이트 
	        		map.put("Type", "order"); 	// Type : 오더생성 => order, 대한통운 집배코드 (주소정제) => korexConvert 
	//    			updateInterfaceCnt(map);	// 220622 주석처리
	        		
	        		JAXBContext jaxbContext = JAXBContext.newInstance(OceanInterface.class);
	                Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
	                
	                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	
	        		jaxbMarshaller.marshal(order, file);
	                jaxbMarshaller.marshal(order, writer);
	                
	    			schemaLocation = new File(xsd);
	    			schema = factory.newSchema(schemaLocation);
	    			validator = schema.newValidator();
	    			
	    			Source source = new StreamSource(file);
	    			validator.validate(source);
	        	}
			} catch(SAXException e){
				//log.error(Util.getPrintStacTraceString(e));
				String[] errStrArray = e.toString().split("; ");
				if(errStrArray.length > 4) {
					accessFlag = "V";
					String line = null;
					int lineCnt = 0;
					String lineRow = errStrArray[2].split(": ")[1].trim();
					String msg = errStrArray[4].trim();
					
					returnMessage = "LINE (" + lineRow + ") : " + msg;
					
					fr = new FileReader(file);
					br = new BufferedReader(fr);
					
					while((line = br.readLine()) != null){
						lineCnt++;
						if(line.contains("HBLNo")){
							hblNo = line.replace("<HBLNo>", "").replace("</HBLNo>", "").trim();
						}
						if(lineCnt == Integer.parseInt(lineRow)){
							errorValidationLine = line.trim();
							break;
						}
					}
				}else{
					accessFlag = "E";
					returnMessage = e.toString();
				}
			} catch(Exception e){
				//log.error(Util.getPrintStacTraceString(e));
				accessFlag = "E";
				returnMessage = e.toString();
			}finally{
				if(br != null){try{br.close();}catch(Exception e){log.error(e.getMessage());}}
				if(fr != null){try{fr.close();}catch(Exception e){log.error(e.getMessage());}}
			}
	        
	        if("Y".equals(accessFlag)){
	        	path = apiPath + "SUCC/" + bound + "/";
	        	pathDir = new File(path);
	    		if (pathDir.exists() == false)	pathDir.mkdirs();
	        	File fileToMove = new File(path + infileName);
	 	        file.renameTo(fileToMove);
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
	 	 		}else if("V".equals(accessFlag)){ //  OI 에러 발생 
	 	 			path = apiPath + "ERROR/" + bound + "/";
	 	 			pathDir = new File(path);
	 	 			if (pathDir.exists() == false)	pathDir.mkdirs();
	 	 			infileName = "ErrorValidation_" + infileName;
	 	 			File fileToMove = new File(path + infileName);
	 	 	        file.renameTo(fileToMove);
	 	 			returnMessage = returnMessage + " 에러가 발생한 부분 => [" + errorValidationLine + "]";
	 	 		}else if("E".equals(accessFlag)){
	 	 			path = apiPath + "ERROR/" + bound + "/";
	 	 			pathDir = new File(path);
	 	 			if (pathDir.exists() == false)	pathDir.mkdirs();
	 	 			infileName = "Error_" + infileName;
	 	 			File fileToMove = new File(path + infileName);
	 	 	        file.renameTo(fileToMove);
	 	 		}
	 		}
	        
	        // history 등록 
			tmpMap = new HashMap<String, Object>();
			logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
			index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
			
	    	// 처리결과에 따른 로그 기록
	        tmpMap.clear();
			tmpMap.put("ent_code", receiver); 		 							 // 도입사코드
			tmpMap.put("customer_code", sender); 		 						 // 고객코드
			tmpMap.put("solution", "ECM");      		 						 // SOLUTION
			tmpMap.put("bound", boundEs);		 	 							 // 바운드
			tmpMap.put("bound_type", boundTypeEs);	 							 // 바운드타입
			tmpMap.put("hbl_no", Util.null2str(hblNo));				 							 // HBL_NO
			tmpMap.put("if_service_code", bound);
			tmpMap.put("if_doc_code", "ORDER_API");
			tmpMap.put("if_doc_name", "오더API");
			tmpMap.put("if_doc_reference_no", "");	 							 // 문서번호 
			tmpMap.put("if_vender", sender);									 // 중개사업자
			tmpMap.put("if_send_recv_type", "R");			 					 // 송수신타입(S:SEND, R:RECV)
			tmpMap.put("if_send_recv_method", "XML To DB");	 					 // 송수신방법
			tmpMap.put("if_file_path", path);			 						 // 파일경로
			tmpMap.put("if_file_name", infileName);			 				 	 // 파일명
			tmpMap.put("if_status", if_status);			 						 // 처리상태
			tmpMap.put("if_status_info", if_status_info);		 				 // 송수신상태_설명
			tmpMap.put("if_sender_id", sender);									 // 송신식별자
			tmpMap.put("if_sender_name", "");		 							 // 송신자명
			tmpMap.put("if_receiver_id", "");		 							 // 수신식별자
			tmpMap.put("if_receiver_name", "");	     							 // 수신자명
			tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));				 // DATE
			tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));			 // DATE_TIME
			tmpMap.put("if_remark", returnMessage);			 					 // 비고( 에러로그 등)
			tmpMap.put("register_user_id", "API");
			tmpMap.put("register_user_name", "API");
			tmpMap.put("register_date_time", System.currentTimeMillis());
			tmpMap.put("es_job_no", logId);
			
			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			documentApi = new DocumentApi(client);
			documentApi.createDocument(index, logId, tmpMap);
	        
			returnData.setReturnCode(returnCode);
			returnData.setReturnMessage(returnMessage);
			
			result.setReturnData(returnData);		
		}catch(Exception e){
			log.error(Util.getPrintStacTraceString(e));
			returnData.setReturnCode("FAIL");
			returnData.setReturnMessage("System Error.");
			result.setReturnData(returnData);
		}
		
		return result;
	}

	/**
	 * ECM 수입(항공&해상) 관세사 비용 적용 API(개발보류)
	 * @param customs
	 * @return InterfaceResult
	 * @throws Exception
	 */
	public InterfaceResult registCustomsInfo(CustomsInterface customs, HttpServletRequest req) throws Exception {
		InterfaceResult result = new InterfaceResult();
		InterfaceReturnData returnData = new InterfaceReturnData();
		try {
			String date = null;
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date(System.currentTimeMillis()));
			String format = "yyyyMMdd_HHmmss.SSS";
			date = new SimpleDateFormat(format).format(cal.getTime());
			
			// XML 스키마 검증파일(XSD)
			SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			String xsd = customProperties.getApiCustomsXsd(); 
			File schemaLocation = null;
			Schema schema = null;
			Validator validator = null;
			FileReader fr = null;
	        BufferedReader br = null;
	        String infileName = "";
	        String filePath = "";
			String errorValidationLine = "";
			
	        
			String returnCode = "";
			String returnMessage = "";
			String accessFlag = "N";
			String path = customProperties.getApiCustomsXml(); // 배치개발시 경로 재검토
			String accessKey = (Util.null2str(customs.getAccessIdentifier().getAccessKey())).toUpperCase();
			String sender = (Util.null2str(customs.getAccessIdentifier().getSenderIdentifier())).toUpperCase();
			String receiver = (Util.null2str(customs.getAccessIdentifier().getReceiverIdentifier())).toUpperCase();
			String logId = ""; // ES 로그용 아이디
			String index = ""; // ES 로그용 index
			String if_status = "RS"; // 처리상태값
			String if_status_info = "Receive Start"; // 처리상태설명		
			
			
			IndexApi indexApi = null;
			DocumentApi documentApi = null;
			
			Map<String, Object> tmpMap = null;
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ReceiverId", receiver);
			map.put("SenderId", sender);
			map.put("UseYN", "Y");
	
			// Interface key 조회(ENT_CODE(ReceiverId) ,CUSTOMER_CODE(SenderId) ,ACCESS_KEY ,USE_YN) 	
			Map<String, Object> deliveryInfo = (Map<String, Object>)interfaceMapper.selectInterfaceInfo(map);
			
			if(deliveryInfo != null){
				if(accessKey.equals(Util.null2str(deliveryInfo.get("ACCESS_KEY")))){
					accessFlag = "Y"; // 요청자가 보낸 값과 동일하면 Y
				}
			}else{
				accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
			}
			
			//infileName = path + System.getProperty("file.separator") + receiver + "_" + sender + "_" + date + ".tmp";
			
			infileName = receiver + "_" + sender + "_" + date + ".tmp";
			filePath = path + "/" + infileName;
			File pathDir = new File(path);
			if (pathDir.exists() == false)	pathDir.mkdirs();
			File file = new File(filePath);
			
			// history 등록 
			tmpMap = new HashMap<String, Object>();
			logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
			index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
			
			tmpMap.clear();
			tmpMap.put("ent_code", receiver); 		 							 // 도입사코드
			tmpMap.put("customer_code", ""); 		 							 // 고객코드
			tmpMap.put("solution", "ECM");      		 						 // SOLUTION
			tmpMap.put("bound", "");		 	 								 // 바운드
			tmpMap.put("bound_type", "");	 							 		 // 바운드타입
			tmpMap.put("hbl_no", "");				 							 // HBL_NO
			tmpMap.put("if_service_code", "API");		 						 // 프로세스타입 (EDI, API, KTNET-EBILL-ETB 등)
			tmpMap.put("if_doc_code", "");			 						 // 문서구분 (GOVCBR5IG, AI, AO, OI, OO 등)
			tmpMap.put("if_doc_name", "관세사 비용 계산"); 			 			 // 문서분류명 (AIR ORDER, SEA ORDER)
			tmpMap.put("if_doc_reference_no", "");	 							 // 문서번호 
			tmpMap.put("if_vender", "");										 // 중개사업자
			tmpMap.put("if_send_recv_type", "R");	 							 // 송수신타입(S:SEND, R:RECV)
			tmpMap.put("if_send_recv_method", "XML To DB");		 				 // 송수신방법
			tmpMap.put("if_file_path", path);			 						 // 파일경로
			tmpMap.put("if_file_name", infileName);			 				 	 // 파일명
			tmpMap.put("if_status", if_status);			 						 // 처리상태
			tmpMap.put("if_status_info", if_status_info);		 				 // 송수신상태_설명
			tmpMap.put("if_sender_id", sender);									 // 송신식별자
			tmpMap.put("if_sender_name", "");		 							 // 송신자명
			tmpMap.put("if_receiver_id", "");		 							 // 수신식별자
			tmpMap.put("if_receiver_name", "");	     							 // 수신자명
			tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));				 // DATE
			tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));			 // DATE_TIME
			tmpMap.put("if_remark", returnMessage);			 					 // 비고( 에러로그 등)
			tmpMap.put("register_user_id", "API");
			tmpMap.put("register_user_name", "API");
			tmpMap.put("register_date_time", System.currentTimeMillis());
			tmpMap.put("es_job_no", logId);
			
	
			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			
			documentApi = new DocumentApi(client);
			documentApi.createDocument(index, logId, tmpMap);							
			//
			
	 		StringWriter writer = new StringWriter();
	        try{
	        	if("Y".equals(accessFlag)){
		    		JAXBContext jaxbContext = JAXBContext.newInstance(CustomsInterface.class);
		            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		            
		            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		    		jaxbMarshaller.marshal(customs, file);
		            jaxbMarshaller.marshal(customs, writer);
		            
					schemaLocation = new File(xsd);
					schema = factory.newSchema(schemaLocation);
					validator = schema.newValidator();
					
					Source source = new StreamSource(file);
					validator.validate(source);
	        	}
			} catch(SAXException e){
				String[] errStrArray = e.toString().split(";");
				if(errStrArray.length > 4) {
					accessFlag = "V";
					
					String line = null;
					int lineCnt = 0;
					String lineRow = errStrArray[2].split(":")[1].trim();
					String msg = errStrArray[4].trim();
					returnMessage = "LINE (" + lineRow + ") : " + msg;
					
					fr = new FileReader(file);
					br = new BufferedReader(fr);
					
					while((line = br.readLine()) != null){
						lineCnt++;
						if(lineCnt == Integer.parseInt(lineRow)){
							errorValidationLine = line.trim();
							break;
						}
					}
				}else {
					accessFlag = "E";
					returnMessage = e.toString();
				}
			} catch(Exception e){
				log.error(Util.getPrintStacTraceString(e));
				accessFlag = "E";
				
				returnMessage = e.toString();
			}
	        
	        
	        if("Y".equals(accessFlag)){
	        	File fileToMove = new File(path + System.getProperty("file.separator") + receiver + "_" + sender + "_" + date + ".xml");
	 	        file .renameTo(fileToMove);
	 	         	        
	 	        returnCode = "SUCCESS";
	 			returnMessage = "";
	 			if_status = "RC";
	 			if_status_info = "Receive Complete";
	 		}else if("N".equals(accessFlag)){
	 			file.delete();
				returnCode = "FAIL";
	 			returnMessage = "Wrong AccessKey";
	 			if_status = "RF";
	 			if_status_info = "Receive Fail"; 
	 		}else if("X".equals(accessFlag)){
	 			file.delete();
				returnCode = "FAIL";
	 			returnMessage = "Not a service provider";
	 			if_status = "RF";
	 			if_status_info = "Receive Fail"; 
	 		}else if("V".equals(accessFlag)){
	 			File fileToMove = new File(path + System.getProperty("file.separator") + "error" + System.getProperty("file.separator") + "ErrorValidation_" + receiver + "_" + sender + "_" + date + ".xml");
	 	        file.renameTo(fileToMove);
	 	        if_status = "RF";
				if_status_info = "Receive Fail"; 
				returnMessage = returnMessage + " 에러가 발생한 부분 => " + errorValidationLine;
				returnCode = "FAIL";
	 		}else if("E".equals(accessFlag)){
	 			File fileToMove = new File(path + System.getProperty("file.separator") + "error" + System.getProperty("file.separator") + "Error_" + receiver + "_" + sender + "_" + date + ".xml");
	 	        file.renameTo(fileToMove);
	 	        if_status = "RF";
				if_status_info = "Receive Fail"; 
				returnCode = "FAIL";
	 		}
	        
	        // 처리결과에 따른 로그 기록 history 업데이트
			tmpMap.clear();
			tmpMap.put("if_status", if_status);			 				// 처리상태
			tmpMap.put("if_status_info", if_status_info);		 		// 송수신상태_설명
			tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));		// DATE
			tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));	// DATE_TIME
			tmpMap.put("if_remark", returnMessage);			 			// 비고( 에러로그 등)
			tmpMap.put("register_date_time", System.currentTimeMillis());
			tmpMap.put("es_job_no", logId);
			documentApi.updateDocument(index, logId, tmpMap);						
	        //
			
			returnData.setReturnCode(returnCode);
			returnData.setReturnMessage(returnMessage);
			
			result.setReturnData(returnData);
		}catch(Exception e){
			log.error(Util.getPrintStacTraceString(e));
			returnData.setReturnCode("FAIL");
			returnData.setReturnMessage("System Error.");
			result.setReturnData(returnData);
		}
		return result;
	}
	
	/**
	 * ECM 수입(항공&해상) 트레이킹 조회 API(네이버 굿스플로,스윗트래커 연동)
	 * @param tracking
	 * @return void => 웹화면에 json 출력
	 * @throws Exception
	 */
	public Map<String, Object> selectTrackingInfo(InterfaceTracking tracking, HttpServletRequest req) throws Exception {
		List<Map<String, Object>> mArray = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> detailList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> hblList = null;
		List<String> hblNoList = new ArrayList<String>();
		
		Map<String, Object> rootMap = new HashMap<String, Object>();
		Map<String, Object> hblMap = null;  // 출력용
		Map<String, Object> map = new HashMap<String, Object>(); // 값
		
		
		String stepnm = ""; // 단계이름
		String originMsg = ""; // 공항 or 출발
		String destMsg = ""; // 공항 or 도착
		
		String entCode = tracking.getFwdCode();
		if(!"".equals(Util.null2str(tracking.getHblNo()))) {
			hblNoList.addAll(Arrays.asList(String.valueOf(tracking.getHblNo()).split(",")));
		}
		
		if(hblNoList.size() > 0) {
			map.put("hblNoArr", hblNoList);
			map.put("entCode", entCode);
			if("AI".equals(Util.null2str(tracking.getBound()))) {
				originMsg = "공항";
				destMsg = "공항";
				map.put("bound", "AI");
			}if("OI".equals(Util.null2str(tracking.getBound()))) {
				originMsg = "출발";
				destMsg = "도착";
				map.put("bound", "OI");
			}
			
			// 하우스 컬럼 정리 및 쿼리 작성 
			Map<String, Object> entMap = commonMapper.selectEnterpriseInfo(Util.null2str(entCode).toUpperCase());
			if(entMap != null && entMap.size() > 0){
				map.put("cryptographyYn", entMap.get("ENCRYPTION_YN"));
			}
			hblList = interfaceMapper.selectTrackingHbl(map);

			for(Map<String, Object> tempHblMap : hblList) {
				hblMap = new HashMap<String, Object>();
				this.escapeHtml(tempHblMap);
				hblMap.putAll(tempHblMap);
				
				tempHblMap.put("bound", map.get("bound"));
				
				detailList = interfaceMapper.selectDeliveryTracking(tempHblMap);
				
				if(detailList.size() > 0 ) {
					for(Map<String, Object> detailMap : detailList) {
						
						if("20".equals(Util.null2str(detailMap.get("statusCd")))) { // 출항
							detailMap.put("locationNm", Util.null2str(tempHblMap.get("ORIG_CODE")) + " " + originMsg);
							detailMap.put("memo", Util.null2str(tempHblMap.get("ORIG_CODE")) + "->" + Util.null2str(tempHblMap.get("DEST_CODE"))+"("+Util.null2str(tempHblMap.get("VESSEL_FLIGHT"))+")");
							detailMap.put("csInfo", "-");
						}else if("30".equals(Util.null2str(detailMap.get("statusCd")))) { // 입항
							detailMap.put("locationNm", Util.null2str(tempHblMap.get("DEST_CODE")) + " " + destMsg);
							detailMap.put("memo", "-");
						}else if("70".equals(Util.null2str(detailMap.get("statusCd")))) { // 배달완료
							if("YHTXX".equals(String.valueOf(entCode))) {
								detailMap.put("locationNm", "고객");
							}
						}
						this.escapeHtml(detailMap);
						stepnm = Util.null2str(detailMap.get("statusNm"));
					}
					
					
					hblMap.put("details", detailList);
					
					// 정보 참조하고 삭제 (삭제안하면 해당 내용 응답메시지로 나감)
					hblMap.remove("RefNo");
					hblMap.remove("HBLNo");
					hblMap.remove("OrigCode");
					hblMap.remove("DestCode");
					hblMap.remove("FlightNo");
					hblMap.remove("OrigName");
					//
					
					hblMap.put("stepnm", stepnm); 
					
					
					mArray.add(hblMap);
					
				} // 하우스정보 없을때
			}
				
			Util.escapeNestedDataForXss(mArray, null);
		}
		rootMap.put("mArray", mArray);
		
		return rootMap;

	}
	public void escapeHtml(Map<String, Object> map) throws Exception {
		for(String mapKey : map.keySet()){
			if(map.get(mapKey) instanceof String){
				map.put(mapKey, StringEscapeUtils.escapeHtml4(Util.null2str(map.get(mapKey))));
			}
		}
	}
	
	/**
	 *  ECM 수입(항공(AI/AO), 해상(OI/OO)) 스탁 자동 채번 API 검증
	 * @param paramMap 
	 * @param req
	 * @return JSONObject => 웹화면에 json 출력
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public JSONObject createStockCheck(String body, Map<String, Object> paramMap) throws Exception {

		JSONObject reqJson = null;
		JSONObject returnJson = new JSONObject();
		
		List<String> errMsgList = new ArrayList<String>();
		Map<String, Object> interfaceMap = null;
		Map<String, Object> stockMap = null;
		
		String entCode = null;
		String agentCode = null;
		String customerCode = null;
		String accessKey = null;
		String bound = null;
		String deliveryCode = null;
		
		int stockCount = 0;
		
		try{
			
			if(body != null) {
				reqJson =  new JSONObject().fromObject(body);
				entCode = Util.null2str(reqJson.get("ReceiverIdentifier"));
				agentCode = Util.null2str(reqJson.get("SenderIdentifier"));
				customerCode = Util.null2str(reqJson.get("AgentCode"));
				accessKey = Util.null2str(reqJson.get("AccessKey"));
				bound = Util.null2str(reqJson.get("Bound"));
				deliveryCode = Util.null2str(reqJson.get("DeliveryCode"));
				stockCount = Util.null2int(reqJson.get("StockCount"));
				
			
				// 데이터 검증 시작
				if("".equals(entCode)) {
					errMsgList.add("ReceiverIdentifier 항목은 필수값입니다.");
				}else if(!Pattern.matches("^[a-zA-Z0-9]{5}$", entCode)) {
					errMsgList.add("ReceiverIdentifier 항목에 정합하지 않는 값입니다.");
				}
				
				if("".equals(agentCode)) {
					errMsgList.add("SenderIdentifier 항목은 필수값입니다.");
				}else if(!Pattern.matches("^[a-zA-Z0-9]{1,20}$", agentCode)) {
					errMsgList.add("SenderIdentifier 항목에 정합하지 않는 값입니다.");
				}
				if(!"".equals(customerCode)) {
					if(!Pattern.matches("^[a-zA-Z0-9]{1,20}$", agentCode)) {
						errMsgList.add("AgentCode 항목에 정합하지 않는 값입니다.");
					}
				}
				
				if("".equals(accessKey)) {
					errMsgList.add("AccessKey 항목은 필수값입니다.");
				}else if(!Pattern.matches("^[A-Z0-9]{64}$", accessKey)) {
					errMsgList.add("AccessKey 항목에 정합하지 않는 값입니다.");
				}
				
				if("".equals(bound)) {
					errMsgList.add("Bound 항목은 필수값입니다.");
				}else if(!Pattern.matches("^(AI|AO|OI|OO)$", bound)) {
					errMsgList.add("Bound 항목에 정합하지 않는 값입니다.");
				}
				
				if("".equals(deliveryCode)) {
					errMsgList.add("DeliveryCode 항목은 필수값입니다.");
				}
				
				if(0 == stockCount) {
					errMsgList.add("StockCount 항목은 필수값입니다.");
				}else if(!Pattern.matches("^[0-9]{1,4}$", String.valueOf(stockCount))) {
					errMsgList.add("StockCount 항목에 정합하지 않는 값입니다.(1~9999)");
				}
				// 데이터 검증 완료
				
				// 데이터 검증 오류관련 Exception 발생시켜 리턴
				if(errMsgList.size() > 0){
					throw new Exception("ValidationException");
				}

				// 인증키 검증 시작
				paramMap.put("ReceiverId", entCode);
				paramMap.put("SenderId", agentCode);
				interfaceMap = (Map<String, Object>)interfaceMapper.selectInterfaceInfo(paramMap);
				
				if(interfaceMap != null){
					if(!accessKey.equals(Util.null2str(interfaceMap.get("ACCESS_KEY")))){
						errMsgList.add("AccessKey가 일치하지 않습니다. 관리자에게 문의하시기 바랍니다.");
						throw new Exception("AccessKeyException");
					}
				}else{
					errMsgList.add("AccessKey가 등록되어 있지 않습니다. 관리자에게 문의하시기 바랍니다.");
					throw new Exception("AccessKeyException");
				}
				// 인증키 검증 완료
						
				if("AI".equals(bound)) {
					paramMap.put("Bound", "AIR");
					paramMap.put("BoundType", "IMP");
				} else if("AO".equals(bound)) {
					paramMap.put("Bound", "AIR");
					paramMap.put("BoundType", "EXP");
				} else if("OI".equals(bound)) {
					paramMap.put("Bound", "SEA");
					paramMap.put("BoundType", "IMP");
				} else if("OO".equals(bound)) {
					paramMap.put("Bound", "SEA");
					paramMap.put("BoundType", "EXP");
				}
				
				paramMap.put("TrCode", deliveryCode);
				paramMap.put("agentCode", agentCode);
				paramMap.put("entCode", entCode);
				paramMap.put("customerCode", customerCode);
				paramMap.put("Type", "HBL");
				paramMap.put("HblCnt", stockCount);
				stockMap = (Map<String, Object>)interfaceMapper.selectUnusedStockAPI(paramMap);
				
				if(stockMap != null) { 
					if(stockCount > Util.null2int(stockMap.get("sumUnUsed"))) {
						errMsgList.add("요청한 스탁수("+stockCount+")보다 사용할 수 있는 스탁수("+Util.null2int(stockMap.get("sumUnUsed"))+")가 적어서 사용할 수 없습니다. 관리자에게 문의하시 바랍니다.");
						throw new Exception("StockException");
					}else {
						returnJson.put("Success", true);
					}
				}else {
					errMsgList.add("등록된 스탁정보가 없습니다. 관리자에게 문의하시 바랍니다.");
					throw new Exception("StockException");
				}
				
			}else {
				errMsgList.add("요청한 BODY 정보가 없습니다.");
				throw new Exception("BodyException");
			}

		}catch(JSONException e){
			returnJson.put("Success", false);
			returnJson.put("ErrorMsg", "BODY안에 JSON 문법이 잘못되었습니다.");
		}catch(Exception e){
			if("BodyException".equals(e.getLocalizedMessage())) {
			}else if("ValidationException".equals(e.getLocalizedMessage())) {
			}else if("AccessKeyException".equals(e.getLocalizedMessage())) {
			}else if("StockException".equals(e.getLocalizedMessage())) {
			}else {
				log.error(Util.getPrintStacTraceString(e));
				errMsgList.add(e.getLocalizedMessage());
			}
			
			Util.escapeNestedDataForXss(errMsgList, null);
			returnJson.put("Success", false);
			returnJson.put("ErrorMsg", errMsgList);
		}finally {
			
		}
		
		return returnJson;
		
	}
	
	/**
	 *  ECM 수입(항공(AI/AO), 해상(OI/OO)) 스탁 자동 채번 API
	 * @param req
	 * @return JSONObject => 웹화면에 json 출력
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public JSONObject createStock(HttpServletRequest req) throws Exception {

		JSONObject reqJson = null;
		JSONObject returnJson = new JSONObject();
		
		IndexApi indexApi = null;
		DocumentApi documentApi = null;
		
		List<String> hblNoList = new ArrayList<String>();
		List<String> errMsgList = new ArrayList<String>();
		
		Map<String, Object> interfaceMap = null;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		Map<String, Object> stockMap = null;
		Map<String, Object> stockNoMap = null;
		Map<String, Object> tmpMap = null;
		
		String prefixNo = null;
		String suffixNo = "";
		String startStockNo = null;
		String body = null;
		String fwdCode = null;
		String agentCode = null;
		String accessKey = null;
		String bound = null;
		String deliveryCode = null;
		String stockNo ="";
		String logId = ""; // ES 로그용 아이디
		String index = ""; // ES 로그용 index		
		String if_status = ""; // 처리상태값
		String if_status_info = ""; // 처리상태설명
		String returnMessage = "";
		
		int stockCount = 0;
		
		try{
			
			body = HttpUtil.getBodyDataByRequest(req);
			if(body != null) {
				reqJson =  new JSONObject().fromObject(body);
				fwdCode = Util.null2str(reqJson.get("FwdCode"));
				agentCode = Util.null2str(reqJson.get("AgentCode"));
				accessKey = Util.null2str(reqJson.get("AccessKey"));
				bound = Util.null2str(reqJson.get("Bound"));
				deliveryCode = Util.null2str(reqJson.get("DeliveryCode"));
				stockCount = Util.null2int(reqJson.get("StockCount"));
				
			
				// 데이터 검증 시작
				if("".equals(fwdCode)) {
					errMsgList.add("FwdCode 항목은 필수값입니다.");
				}else if(!Pattern.matches("^[a-zA-Z0-9]{5}$", fwdCode)) {
					errMsgList.add("FwdCode 항목에 정합하지 않는 값입니다.");
				}
				
				if("".equals(agentCode)) {
					errMsgList.add("AgentCode 항목은 필수값입니다.");
				}else if(!Pattern.matches("^[a-zA-Z0-9]{1,20}$", agentCode)) {
					errMsgList.add("AgentCode 항목에 정합하지 않는 값입니다.");
				}
				
				if("".equals(accessKey)) {
					errMsgList.add("AccessKey 항목은 필수값입니다.");
				}else if(!Pattern.matches("^[A-Z0-9]{64}$", accessKey)) {
					errMsgList.add("AccessKey 항목에 정합하지 않는 값입니다.");
				}
				
				if("".equals(bound)) {
					errMsgList.add("Bound 항목은 필수값입니다.");
				}else if(!Pattern.matches("^(AI|AO|OI|OO)$", bound)) {
					errMsgList.add("Bound 항목에 정합하지 않는 값입니다.");
				}
				
				if("".equals(deliveryCode)) {
					errMsgList.add("DeliveryCode 항목은 필수값입니다.");
				}else if(!Pattern.matches("^(KOREX|EMS|SMART_EMS|COPAS_EMS)$", deliveryCode)) {
					errMsgList.add("DeliveryCode 항목에 정합하지 않는 값입니다.");
				}
				
				if(0 == stockCount) {
					errMsgList.add("StockCount 항목은 필수값입니다.");
				}else if(!Pattern.matches("^[0-9]{1,4}$", String.valueOf(stockCount))) {
					errMsgList.add("StockCount 항목에 정합하지 않는 값입니다.(1~9999)");
				}
				// 데이터 검증 완료
				
				// 데이터 검증 오류관련 Exception 발생시켜 리턴
				if(errMsgList.size() > 0){
					throw new Exception("ValidationException");
				}

				// 인증키 검증 시작
				paramMap.put("ReceiverId", fwdCode);
				paramMap.put("SenderId", agentCode);
				interfaceMap = (Map<String, Object>)interfaceMapper.selectInterfaceInfo(paramMap);
				
				if(interfaceMap != null){
					if(!accessKey.equals(Util.null2str(interfaceMap.get("ACCESS_KEY")))){
						errMsgList.add("AccessKey가 일치하지 않습니다. 관리자에게 문의하시기 바랍니다.");
						throw new Exception("AccessKeyException");
					}
				}else{
					errMsgList.add("AccessKey가 등록되어 있지 않습니다. 관리자에게 문의하시기 바랍니다.");
					throw new Exception("AccessKeyException");
				}
				// 인증키 검증 완료
						
				if("AI".equals(bound)) {
					paramMap.put("Bound", "AIR");
					paramMap.put("BoundType", "IMP");
				} else if("AO".equals(bound)) {
					paramMap.put("Bound", "AIR");
					paramMap.put("BoundType", "EXP");
				} else if("OI".equals(bound)) {
					paramMap.put("Bound", "SEA");
					paramMap.put("BoundType", "IMP");
				} else if("OO".equals(bound)) {
					paramMap.put("Bound", "SEA");
					paramMap.put("BoundType", "EXP");
				}
				
				paramMap.put("TrCode", deliveryCode);
				paramMap.put("agentCode", agentCode);
				paramMap.put("Type", "HBL");
				paramMap.put("HblCnt", stockCount);
				stockMap = (Map<String, Object>)interfaceMapper.selectStockAPI(paramMap); /*  EXEC p_CREATE_EXPRESS_STOCK 'AIR', 'IMP', 'HBL', 'CH01' ,'EMS', '10', 'API' */
				
				if(stockMap != null) { 
					if(stockCount > Util.null2int(stockMap.get("unUsed"))) {
						errMsgList.add("요청한 스탁수("+stockCount+")보다 사용할 수 있는 스탁수("+Util.null2int(stockMap.get("unUsed"))+")가 적어서 사용할 수 없습니다. 관리자에게 문의하시 바랍니다.");
						throw new Exception("StockException");
					}else {
						stockNoMap = (Map<String, Object>)interfaceMapper.getCreateStockAPI(paramMap);
						if("0".equals(String.valueOf(stockNoMap.get("result")))) {
							stockMap = (Map<String, Object>)interfaceMapper.selectStockAPI(paramMap);
							errMsgList.add("요청한 스탁수("+stockCount+")보다 사용할 수 있는 스탁수("+Util.null2int(stockMap.get("unUsed"))+")가 적어서 사용할 수 없습니다. 관리자에게 문의하시 바랍니다.");
							throw new Exception("StockException");
						}else {
							startStockNo = String.valueOf(stockNoMap.get("CURRENT_NO"));
							
							if(deliveryCode.contains("KOREX") || deliveryCode.contains("HANJIN")) {
								for(int i=0; i<stockCount; i++) {
									if(deliveryCode.contains("HANJIN")){
										prefixNo = "";
										// stockNo 잘라 냈을때 0으로 시작하는 번호이면 지워지는 버그가 있어서 9자리중 빈자리 0으로 채우는 소스 추가
										stockNo = String.format("%011d",Long.parseLong(startStockNo.substring(0, 11)) + i);
									}else{
										prefixNo = startStockNo.substring(0,2);
										// stockNo 잘라 냈을때 0으로 시작하는 번호이면 지워지는 버그가 있어서 9자리중 빈자리 0으로 채우는 소스 추가
										stockNo = String.format("%09d",Long.parseLong(startStockNo.substring(0, 11)) + i);
									}
									suffixNo = Long.toString(Long.parseLong(stockNo) % 7);
									
									if(suffixNo.length() == 2){
										String doubleDigit = suffixNo.substring(1,2);
										hblNoList.add(prefixNo + stockNo + doubleDigit);
									}else{
										hblNoList.add(prefixNo + stockNo + suffixNo);
									}
								}
							}else {
								for(int i=0; i<stockCount; i++) {
									hblNoList.add(String.valueOf(Long.parseLong(startStockNo)+i));
								}
							}
						}
					}
				}else {
					errMsgList.add("등록된 스탁정보가 없습니다. 관리자에게 문의하시 바랍니다.");
					throw new Exception("StockException");
				}
				
			}else {
				errMsgList.add("요청한 BODY 정보가 없습니다.");
				throw new Exception("BodyException");
			}

			Util.escapeNestedDataForXss(hblNoList, null);
			returnJson.put("Success", true);
			returnJson.put("HBLNos", hblNoList);
			
		}catch(JSONException e){
			returnJson.put("Success", false);
			returnJson.put("ErrorMsg", "BODY안에 JSON 문법이 잘못되었습니다.");
		}catch(Exception e){
			if("BodyException".equals(e.getLocalizedMessage())) {
			}else if("ValidationException".equals(e.getLocalizedMessage())) {
			}else if("AccessKeyException".equals(e.getLocalizedMessage())) {
			}else if("StockException".equals(e.getLocalizedMessage())) {
			}else {
				log.error(Util.getPrintStacTraceString(e));
				errMsgList.add(e.getLocalizedMessage());
			}
			
			Util.escapeNestedDataForXss(errMsgList, null);
			returnJson.put("Success", false);
			returnJson.put("ErrorMsg", errMsgList);
		}finally {

			if((boolean)returnJson.get("Success")) {
				if_status = "SUCCESS";
			}else {
				if_status = "ERROR";
			}
			
			returnMessage = "ReqJSON => [ " + reqJson.toString() + " ] ResJSON => [" + returnJson.toString() + "]";
			
			// 처리 로그 기록 interface history 등록 
			// history 등록 
			tmpMap = new HashMap<String, Object>();
			
			logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
			index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
			
			tmpMap.clear();
			tmpMap.put("ent_code", fwdCode); 		 							 			// 도입사코드
			tmpMap.put("customer_code", ""); 		 							 			// 고객코드
			tmpMap.put("solution", "ECM");      		 									// SOLUTION
			tmpMap.put("bound", Util.null2str(paramMap.get("Bound")));		 	 			// 바운드
			tmpMap.put("bound_type", Util.null2str(paramMap.get("BoundType")));	 			// 바운드타입
			tmpMap.put("hbl_no", "");				 							 			// HBL_NO
			tmpMap.put("if_service_code", "API");		 						 			// 프로세스타입 (EDI, API, KTNET-EBILL-ETB 등)
			tmpMap.put("if_doc_code", "STOCK_API");
			tmpMap.put("if_doc_name",  "스탁API");
			tmpMap.put("if_doc_reference_no", "");	 							 			// 문서번호 
			tmpMap.put("if_vender", agentCode);										 			// 중개사업자
			tmpMap.put("if_send_recv_type", "");			 					 			// 송수신타입(S:SEND, R:RECV)
			tmpMap.put("if_send_recv_method", "");		 				 		 			// 송수신방법
			tmpMap.put("if_status", if_status);			 						 			// 처리상태
			tmpMap.put("if_status_info", if_status_info);		 							// 송수신상태_설명
			tmpMap.put("if_sender_id", agentCode);									 		// 송신식별자
			tmpMap.put("if_sender_name", "");		 							 			// 송신자명
			tmpMap.put("if_receiver_id", "");		 							 			// 수신식별자
			tmpMap.put("if_receiver_name", "");	     							 			// 수신자명
			tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));				 			// DATE
			tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));			 			// DATE_TIME
			tmpMap.put("if_remark", returnMessage);			 					 			// 비고( 에러로그 등)
			tmpMap.put("register_user_id", "API");
			tmpMap.put("register_user_name", "API");
			tmpMap.put("register_date_time", System.currentTimeMillis());
			tmpMap.put("es_job_no", logId);
			

			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			
			documentApi = new DocumentApi(client);
			documentApi.createDocument(index, logId, tmpMap);							
			//								
			
			
		}
		
		return returnJson;
		
	}
	
	/**
	 *  ECM 수입(항공(AI/AO), 해상(OI/OO)) 스탁 자동 채번 API
	 * @param hblNoList
	 * @param req
	 * @return JSONObject => 웹화면에 json 출력
	 * @throws Exception
	 */
	public JSONObject createStock(Map<String, Object> paramMap, List<String> hblNoList) throws Exception {

		JSONObject returnJson = new JSONObject();
		List<String> errMsgList = new ArrayList<String>();
		List<String> stockAgentCode = null;
		Map<String, Object> stockMap = null;
		Map<String, Object> stockNoMap = null;
		String agentCode = Util.null2str(paramMap.get("agentCode"));
		String stockCustCode = null;
		String startStockNo = null;
		int stockCount = 0;
		int custLv = 0;
		try{
			// 거래처코드가 존재하면 거래처 없으면 송신자
			if(!"".equals(Util.null2str(paramMap.get("customerCode")))) {
				stockCustCode = Util.null2str(paramMap.get("customerCode"));
			}else {
				stockCustCode = agentCode;
			}
			paramMap.put("customerCode", stockCustCode);
			stockAgentCode = commonMapper.selectCustCode(paramMap);
			if(stockAgentCode != null && stockAgentCode.size() > 0) {
				custLv = stockAgentCode.size();
				//Lv3이상의 업체는 Lv3으로 stock 생성
				if(custLv > 3) {
					custLv = 3;
				}
			}
		
			stockNoMap = new HashMap<String,Object>();
			for(int i = custLv-1; i >= 0; i--) {
				paramMap.put("agentCode", stockAgentCode.get(i)); 
				stockNoMap = (Map<String, Object>)interfaceMapper.getCreateStockAPI(paramMap);
				if(!"".equals(Util.null2str(stockNoMap.get("CURRENT_NO")))){
					break;
				}
			}
				
			/** stockNo를 생성하지못하면 상위레벨로 다시 생성, custCodeLv0(도입사)로 stockNo생성할수없으면 종료 */
			if("".equals(Util.null2str(stockNoMap.get("CURRENT_NO")))) {
				paramMap.put("agentCode", Util.null2str(paramMap.get("entCode")));
				stockNoMap = (Map<String, Object>)interfaceMapper.getCreateStockAPI(paramMap);
			}
				
			if("0".equals(String.valueOf(stockNoMap.get("result")))) {
				stockMap = (Map<String, Object>)interfaceMapper.selectStockAPI(paramMap);
				errMsgList.add("요청한 스탁수("+stockCount+")보다 사용할 수 있는 스탁수("+Util.null2int(stockMap.get("unUsed"))+")가 적어서 사용할 수 없습니다. 관리자에게 문의하시 바랍니다.");
				throw new Exception("StockException");
			}else {
				startStockNo = String.valueOf(stockNoMap.get("CURRENT_NO"));
				
				hblNoList.add(startStockNo);
			}
				
			Util.escapeNestedDataForXss(hblNoList, null);
			returnJson.put("Success", true);
			
		}catch(JSONException e){
			returnJson.put("Success", false);
			returnJson.put("ErrorMsg", "BODY안에 JSON 문법이 잘못되었습니다.");
		}catch(Exception e){
			if("BodyException".equals(e.getLocalizedMessage())) {
			}else if("ValidationException".equals(e.getLocalizedMessage())) {
			}else if("AccessKeyException".equals(e.getLocalizedMessage())) {
			}else if("StockException".equals(e.getLocalizedMessage())) {
			}else {
				log.error(Util.getPrintStacTraceString(e));
				errMsgList.add(e.getLocalizedMessage());
			}
			
			Util.escapeNestedDataForXss(errMsgList, null);
			returnJson.put("Success", false);
			returnJson.put("ErrorMsg", errMsgList);
		}finally {
			paramMap.put("agentCode", agentCode);
		}
		
		return returnJson;
		
	}
	
	/**
	 *  ECM 수입(항공(AI/AO), 해상(OI/OO)) 스탁 자동 채번 API 검증(EMS)
	 * @param paramMap 
	 * @param req
	 * @return JSONObject => 웹화면에 json 출력
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public JSONObject createStockCheckEms(String body, Map<String, Object> paramMap) throws Exception {

		JSONObject reqJson = null;
		JSONObject returnJson = new JSONObject();
		
		List<String> errMsgList = new ArrayList<String>();
		Map<String, Object> interfaceMap = null;
		Map<String, Object> stockMap = null;
		
		String entCode = null;
		String agentCode = null;
		String accessKey = null;
		String bound = null;
		String deliveryCode = null;
		
		int stockCount = 0;
		
		try{
			
			if(body != null) {
				reqJson =  new JSONObject().fromObject(body);
				entCode = Util.null2str(reqJson.get("ReceiverIdentifier"));
				agentCode = Util.null2str(reqJson.get("SenderIdentifier"));
				accessKey = Util.null2str(reqJson.get("AccessKey"));
				bound = Util.null2str(reqJson.get("Bound"));
				deliveryCode = Util.null2str(reqJson.get("DeliveryCode"));
				stockCount = Util.null2int(reqJson.get("StockCount"));
				
			
				// 데이터 검증 시작
				if("".equals(entCode)) {
					errMsgList.add("ReceiverIdentifier 항목은 필수값입니다.");
				}else if(!Pattern.matches("^[a-zA-Z0-9]{5}$", entCode)) {
					errMsgList.add("ReceiverIdentifier 항목에 정합하지 않는 값입니다.");
				}
				
				if("".equals(agentCode)) {
					errMsgList.add("SenderIdentifier 항목은 필수값입니다.");
				}else if(!Pattern.matches("^[a-zA-Z0-9]{1,20}$", agentCode)) {
					errMsgList.add("SenderIdentifier 항목에 정합하지 않는 값입니다.");
				}
				
				if("".equals(accessKey)) {
					errMsgList.add("AccessKey 항목은 필수값입니다.");
				}else if(!Pattern.matches("^[A-Z0-9]{64}$", accessKey)) {
					errMsgList.add("AccessKey 항목에 정합하지 않는 값입니다.");
				}
				
				if("".equals(bound)) {
					errMsgList.add("Bound 항목은 필수값입니다.");
				}else if(!Pattern.matches("^(AI|AO|OI|OO)$", bound)) {
					errMsgList.add("Bound 항목에 정합하지 않는 값입니다.");
				}
				
				if("".equals(deliveryCode)) {
					errMsgList.add("DeliveryCode 항목은 필수값입니다.");
				}else if(!Pattern.matches("^(EMS-KP|EMS-TS)$", deliveryCode)) {
					errMsgList.add("DeliveryCode 항목에 정합하지 않는 값입니다.");
				}
				
				if(0 == stockCount) {
					errMsgList.add("StockCount 항목은 필수값입니다.");
				}else if(!Pattern.matches("^[0-9]{1,4}$", String.valueOf(stockCount))) {
					errMsgList.add("StockCount 항목에 정합하지 않는 값입니다.(1~9999)");
				}
				// 데이터 검증 완료
				
				// 데이터 검증 오류관련 Exception 발생시켜 리턴
				if(errMsgList.size() > 0){
					throw new Exception("ValidationException");
				}

				// 인증키 검증 시작
				paramMap.put("ReceiverId", entCode);
				paramMap.put("SenderId", agentCode);
				interfaceMap = (Map<String, Object>)interfaceMapper.selectInterfaceInfo(paramMap);
				
				if(interfaceMap != null){
					if(!accessKey.equals(Util.null2str(interfaceMap.get("ACCESS_KEY")))){
						errMsgList.add("AccessKey가 일치하지 않습니다. 관리자에게 문의하시기 바랍니다.");
						throw new Exception("AccessKeyException");
					}
				}else{
					errMsgList.add("AccessKey가 등록되어 있지 않습니다. 관리자에게 문의하시기 바랍니다.");
					throw new Exception("AccessKeyException");
				}
				// 인증키 검증 완료
						
				if("AI".equals(bound)) {
					paramMap.put("Bound", "AIR");
					paramMap.put("BoundType", "IMP");
				} else if("AO".equals(bound)) {
					paramMap.put("Bound", "AIR");
					paramMap.put("BoundType", "EXP");
				} else if("OI".equals(bound)) {
					paramMap.put("Bound", "SEA");
					paramMap.put("BoundType", "IMP");
				} else if("OO".equals(bound)) {
					paramMap.put("Bound", "SEA");
					paramMap.put("BoundType", "EXP");
				}
				
				paramMap.put("TrCode", deliveryCode);
				paramMap.put("agentCode", agentCode);
				paramMap.put("entCode", entCode);
				paramMap.put("Type", "HBL");
				paramMap.put("HblCnt", stockCount);
				stockMap = (Map<String, Object>)interfaceMapper.selectUnusedStockAPI(paramMap);
				
				if(stockMap != null) { 
					if(stockCount > Util.null2int(stockMap.get("sumUnUsed"))) {
						errMsgList.add("요청한 스탁수("+stockCount+")보다 사용할 수 있는 스탁수("+Util.null2int(stockMap.get("sumUnUsed"))+")가 적어서 사용할 수 없습니다. 관리자에게 문의하시 바랍니다.");
						throw new Exception("StockException");
					}else {
						returnJson.put("Success", true);
					}
				}else {
					errMsgList.add("등록된 스탁정보가 없습니다. 관리자에게 문의하시 바랍니다.");
					throw new Exception("StockException");
				}
				
			}else {
				errMsgList.add("요청한 BODY 정보가 없습니다.");
				throw new Exception("BodyException");
			}

		}catch(JSONException e){
			returnJson.put("Success", false);
			returnJson.put("ErrorMsg", "BODY안에 JSON 문법이 잘못되었습니다.");
		}catch(Exception e){
			if("BodyException".equals(e.getLocalizedMessage())) {
			}else if("ValidationException".equals(e.getLocalizedMessage())) {
			}else if("AccessKeyException".equals(e.getLocalizedMessage())) {
			}else if("StockException".equals(e.getLocalizedMessage())) {
			}else {
				log.error(Util.getPrintStacTraceString(e));
				errMsgList.add(e.getLocalizedMessage());
			}
			
			Util.escapeNestedDataForXss(errMsgList, null);
			returnJson.put("Success", false);
			returnJson.put("ErrorMsg", errMsgList);
		}finally {
			
		}
		
		return returnJson;
		
	}
	
	/**
	 *  ECM 수입(항공(AI/AO), 해상(OI/OO)) 스탁 자동 채번 API(EMS)
	 * @param hblNoList 
	 * @param req
	 * @return JSONObject => 웹화면에 json 출력
	 * @throws Exception
	 */
	public JSONObject createStockEms(Map<String, Object> paramMap, List<String> hblNoList) throws Exception {

		JSONObject returnJson = new JSONObject();
		List<String> errMsgList = new ArrayList<String>();
		Map<String, Object> stockMap = null;
		Map<String, Object> stockNoMap = null;
		String startStockNo = null;
		String deliveryCode = Util.null2str(paramMap.get("TrCode"));
		String agentCode = Util.null2str(paramMap.get("agentCode"));
		String entCode = Util.null2str(paramMap.get("entCode"));
		int stockCount = 0;
		
		try{
			
				stockNoMap = new HashMap<String,Object>();
				if("EMS-KP".equals(deliveryCode) || "EMS-TS".equals(deliveryCode)) {
					stockNoMap = (Map<String, Object>)interfaceMapper.getCreateEmsStockAPI(paramMap);
				}else {
					stockNoMap = (Map<String, Object>)interfaceMapper.getCreateStockAPI(paramMap);
				}
				
				/** stockNo를 생성하지못하면 상위레벨로 다시 생성, custCodeLv0(도입사)로 stockNo생성할수없으면 종료 */
				if("".equals(Util.null2str(stockNoMap.get("CURRENT_NO")))) {
					paramMap.put("agentCode", entCode);
					if("EMS-KP".equals(deliveryCode) || "EMS-TS".equals(deliveryCode)) {
						stockNoMap = (Map<String, Object>)interfaceMapper.getCreateEmsStockAPI(paramMap);
					}else {
						stockNoMap = (Map<String, Object>)interfaceMapper.getCreateStockAPI(paramMap);
					}
				}
				
				if("0".equals(String.valueOf(stockNoMap.get("result")))) {
					stockMap = (Map<String, Object>)interfaceMapper.selectStockAPI(paramMap);
					errMsgList.add("요청한 스탁수("+stockCount+")보다 사용할 수 있는 스탁수("+Util.null2int(stockMap.get("unUsed"))+")가 적어서 사용할 수 없습니다. 관리자에게 문의하시 바랍니다.");
					throw new Exception("StockException");
				}else {
					startStockNo = String.valueOf(stockNoMap.get("CURRENT_NO"));
					
					if("EMS-KP".equals(paramMap.get("TrCode"))) {
						startStockNo = "EN"+startStockNo+"KR";
					}else if("EMS-TS".equals(paramMap.get("TrCode"))) {
						startStockNo = "EX"+startStockNo+"KR";
					}
					
					hblNoList.add(startStockNo);
				}
				
			Util.escapeNestedDataForXss(hblNoList, null);
			returnJson.put("Success", true);
			
		}catch(JSONException e){
			returnJson.put("Success", false);
			returnJson.put("ErrorMsg", "BODY안에 JSON 문법이 잘못되었습니다.");
		}catch(Exception e){
			if("BodyException".equals(e.getLocalizedMessage())) {
			}else if("ValidationException".equals(e.getLocalizedMessage())) {
			}else if("AccessKeyException".equals(e.getLocalizedMessage())) {
			}else if("StockException".equals(e.getLocalizedMessage())) {
			}else {
				log.error(Util.getPrintStacTraceString(e));
				errMsgList.add(e.getLocalizedMessage());
			}
			
			Util.escapeNestedDataForXss(errMsgList, null);
			returnJson.put("Success", false);
			returnJson.put("ErrorMsg", errMsgList);
		}finally {
			paramMap.put("agentCode", agentCode);
		}
		
		return returnJson;
		
	}
	
	/**
	 * ECM API KEY 조회
	 * @param paramMap, logMap
	 * @return Map
	 * @throws Exception
	 */
	public Map<String, Object> selectInterfaceInfo(Map<String, Object> paramMap) throws Exception {	
		return interfaceMapper.selectInterfaceInfo(paramMap);
	}
	
	
	/**
	 * ECM 대한통운 집배코드 조회 후 값 리턴 
	 * @param req
	 * @return Map
	 * @throws Exception
	 */
	public Map<String, Object> getKorexAddressCode(Map<String, Object> korexPramMap) throws Exception {
		return interfaceMapper.getKorexAddressCode(korexPramMap); // 프로시저 호출 후 korexPramMap으로 다시 리턴  
	}	

	/**
	 * ECM 대한통운 우편번호로 집배코드 조회 
	 * @param req
	 * @return Map
	 * @throws Exception
	 */ 
	public Map<String, Object> checkKorexPostCode(String zipcode) throws Exception {
		return interfaceMapper.checkKorexPostCode(zipcode); 
	}		
	
	/**
	 * ECM 한진 집배코드 조회
	 * @param hanjinPramMap
	 * @return Map
	 * @throws Exception
	 */
	public Map<String, Object> getHanjinAddressCode(Map<String, Object> hanjinPramMap) throws Exception {	
		return interfaceMapper.getHanjinAddressCode(hanjinPramMap);
	}
	
	/**
	 *  한진 API - 집배코드 조회
	 * @param 
	 * @return Map
	 * @throws Exception
	 */
	public Map<String, Object> connectHanjinApi(JSONObject obj) throws Exception {
				
		BufferedReader in = null;
		String inputLine = null;
		
		String apiKey = Util.null2str(obj.get("api_key"));
		String link = "";
		
		if("Y".equals(ediSendYn)){
			//운영 운송장출력(단건)
//			link = "https://ebbapp.hjt.co.kr/v1/wbl/#{client_id}/print-wbl-sc";
			//권한 오류 있어 개발 url로 세팅함 추후 운영 url로 바꿔야함
			link = "https://ebbapd.hjt.co.kr/v1/wbl/#{client_id}/print-wbl-sc";
		}else {
			//개발 운송장출력(단건)
			link = "https://ebbapd.hjt.co.kr/v1/wbl/#{client_id}/print-wbl-sc";
		}
		link = link.replace("#{client_id}", Util.null2str(obj.get("client_id")));
		
		Map<String, Object> returnMap = null;
		
		URL url = new URL(link);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setReadTimeout(10000);
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("x-api-key", apiKey);
		conn.setDoOutput(true);
		conn.getOutputStream().write(obj.toString().getBytes("UTF-8")); // POST 호출
		
		in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		
		while((inputLine = in.readLine()) != null) { // res 출력
        	if(!"".equals(inputLine)){
        		returnMap = Util.stringToMap(inputLine);
        	}
        }
        
		return returnMap; 
	}	
	
	/**
	 * 롯데 API - 집배코드 조회
	 * @param 
	 * @return Map
	 * @throws Exception
	 */
	public Map<String, Object> connectLotteAPI(JSONObject obj) throws Exception {
		
		BufferedReader in = null;
		String inputLine = null;
		String resultLine = "";
		String apiKey = Util.null2str(obj.get("api_key"));
		String link = "";
		
		if("Y".equals(ediSendYn)){
			link = "http://apigw.llogis.com:10110/api/address/newprint-info";		//운영
		}else {
			link = "http://devapigw.llogis.com:10110/api/address/newprint-info";	//개발
		}

		Map<String, Object> returnMap = null;
		URL url = new URL(link);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
        conn.setReadTimeout(10000);
        conn.setRequestProperty("Content-Type","application/json");
        conn.setRequestProperty("Authorization", "IgtAK " + apiKey);		
        conn.setDoOutput(true);
        conn.getOutputStream().write(obj.toString().getBytes("UTF-8")); // POST 호출
        
        in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
     // res 출력
	    // 롯데택배 API 호출 결과에 줄바꿈 있어 오류나기 때문에 각 라인을 합쳐서 리턴하는 것으로 수정함
        while((inputLine = in.readLine()) != null) {
        	if(!"".equals(inputLine)){
        		resultLine = resultLine + inputLine;
        	}
        }
        
        returnMap = Util.stringToMap(resultLine);

		return returnMap; 
	}	

	/**
	 * API KEY조회
	 * @param 
	 * @return Map
	 * @throws Exception
	 */
	public Map<String, Object> selectDeliveryInterfaceApi(Map<String, Object> map) throws Exception {
		return interfaceMapper.selectDeliveryInterfaceApi(map);
	}
	
	/**
	 * HMS API - packing 저장
	 * @param entCode 
	 * @param 
	 * @return Map
	 * @throws Exception
	 */
	public HmsResult hmsRegistShipmentInfo(Hms hms, String entCode) throws Exception {
		HmsResult result = new HmsResult();
		HmsResponse response = new HmsResponse();
		Map<String, Object> logMap = new HashMap<>();
		String esJobNo = null, errorStr = null, errorCode = null;
		try {
			String ifDate = DateTime.getDate("yyyy-MM-dd HH:mm:ss");
			String ifTime = ifDate.substring(11);
			ifDate = ifDate.substring(0, 10);
			esJobNo = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(1);
			
			//TODO HMS 테스트중
			logMap.put("ent_code", entCode);
//			logMap.put("ent_code", "YHTXX");
			logMap.put("customer_code", "");
			logMap.put("solution", "BMS");
			logMap.put("bound_type", "IMP");
			logMap.put("if_service_code", "");
			logMap.put("if_doc_code", "HMS_PACKAGES");
			logMap.put("if_doc_name", "PACKAGES HMS 수신");
			logMap.put("if_vender", "HMS");
			logMap.put("if_send_recv_type", "R");
			logMap.put("if_send_recv_method", "API");
			logMap.put("if_status_info", "");
			logMap.put("if_sender_id", "");
			logMap.put("if_receiver_id", "");
			logMap.put("if_date", ifDate);
			logMap.put("if_date_time", ifTime);
			logMap.put("register_user_id", "API");
			logMap.put("register_date_time", System.currentTimeMillis());
			logMap.put("es_job_no", esJobNo);
			logMap.put("if_file_path", fileBackupDir_ + "edi/hms/UPSXK/RECV/" + ifDate + "/");
			logMap.put("if_file_name", "PACKAGES_" + esJobNo);
			try{
				log.info("============================== hmsRegistShipmentInfo | " + Util.null2str(hms.getPayload().getShipmentNo()) + " 1111111111");
				FileUtil.fileWrite(new Gson().toJson(hms), Util.null2str(logMap.get("if_file_path")), Util.null2str(logMap.get("if_file_name")));
			}catch(Exception e){
				log.error(e.getMessage());
				FileUtil.fileWrite(hms.toString(), Util.null2str(logMap.get("if_file_path")), Util.null2str(logMap.get("if_file_name")));
			}
			String shipmentNo = null;
			List<HmsPackage> packages = null;
			try{
				log.info("============================== hmsRegistShipmentInfo | " + Util.null2str(hms.getPayload().getShipmentNo()) + " 2222222222");
				shipmentNo = hms.getPayload().getShipmentNo();
				packages = hms.getPayload().getPackages();
			}catch(Exception e){
				log.error(e.getMessage());
				errorStr = "FAIL_A";
				errorCode = "400";
			}
			logMap.put("hbl_no", shipmentNo);
			logMap.put("if_doc_reference_no", shipmentNo);
			
			if(!Util.isEmpty(shipmentNo) && packages != null){
				log.info("============================== hmsRegistShipmentInfo | " + Util.null2str(hms.getPayload().getShipmentNo()) + " 3333333333");
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("hblNo", shipmentNo);
				map = interfaceMapper.selectOrderRegisterDate(map);
				if(map == null){
					map = new HashMap<String, Object>();
					map.put("OD_JOB_NO", shipmentNo);
				}else{
					map.put("odJobNo", map.get("OD_JOB_NO"));
				}
				// 하우스와 PackingNo가 같을 때 저장하지 않음
				for(int i=0; i<packages.size(); i++) {
					if(shipmentNo.equals(packages.get(i).getPackageNo())){
						packages.remove(i);
					}
				}
				log.info("============================== hmsRegistShipmentInfo | " + Util.null2str(hms.getPayload().getShipmentNo()) + " 4444444444");
				logMap.put("bound", Util.null2str(map.get("BOUND"), "COM"));
				map.put("hblNo", shipmentNo);
				map.put("packages", packages);
				interfaceMapper.insertPacking(map);
				
				if(!Util.isEmpty(map.get("BOUND"))){
					commonMapper.updatePackingErrorYn(map);
//					int packingCnt = Util.null2int(map.get("PACKING_CNT"));
//					String packingNo;
//					for(HmsPackage data : packages){
//						packingNo = Util.null2str(data.getPackageNo());
//						if(packingNo.length() == 18 || packingNo.equals(map.get("HBL_NO"))){
//							packingCnt++;
//						}
//					}
//					
//					if(packingCnt != Util.null2int(map.get("PIECES"))){
//						map.put("packingErrorYn", "Y");
//					}else{
//						map.put("packingErrorYn", "N");
//					}
//					map.put("odJobNo", map.get("OD_JOB_NO"));
//					orderMapper.updatePackingErrorYn(map);
				}
				
				response.setReturnCode("SUCCESS");
				response.setReturnMessage("SUCCESS");
				result.setResponse(response);
				logMap.put("if_status", "SUCCESS");
			}else{
				errorStr = "FAIL_B";
				errorCode = "400";
			}
			
		}catch(Exception e){
			log.error(e.getMessage());
			errorStr = "FAIL_C";
			errorCode = "FAIL";
		}
		
		if(errorStr != null){
			response.setReturnCode(errorCode);
			response.setReturnMessage(errorStr);
			result.setResponse(response);
			logMap.put("if_status", "ERROR");
			logMap.put("if_remark", errorCode + " : " + errorStr);
		}
		
		log.info("============================== hmsRegistShipmentInfo | " + Util.null2str(hms.getPayload().getShipmentNo()) + " 5555555555");
		String index = "bms-interface-logs-" + DateTime.getDate("yyyyMMdd");
		IndexApi indexApi = new IndexApi(client);
		DocumentApi documentApi = new DocumentApi(client);
		if (!indexApi.existIndex(index))	indexApi.createIndex(index);
		documentApi.createDocument(index, esJobNo, logMap);
		
		log.info("============================== hmsRegistShipmentInfo | " + Util.null2str(hms.getPayload().getShipmentNo()) + " 6666666666");
		return result;
	}
	
	/**
	 * HMS API - scanDate 저장
	 * @param 
	 * @return Map
	 * @throws Exception
	 */
	public HmsResult hmsRegistPackageScanInfo(Hms hms, String entCode) throws Exception {
		HmsResult result = new HmsResult();
		HmsResponse response = new HmsResponse();
		Map<String, Object> logMap = new HashMap<>();
		String esJobNo = null, errorStr = null, errorCode = null;
		try {
			String ifDate = DateTime.getDate("yyyy-MM-dd HH:mm:ss");
			String ifTime = ifDate.substring(11);
			ifDate = ifDate.substring(0, 10);
			esJobNo = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(1);
			
			//TODO HMS 테스트중
			logMap.put("ent_code", entCode);
//			logMap.put("ent_code", "YHTXX");
			logMap.put("customer_code", "");
			logMap.put("solution", "BMS");
			logMap.put("bound_type", "IMP");
			logMap.put("if_service_code", "");
			logMap.put("if_doc_code", "HMS_SCANDATE");
			logMap.put("if_doc_name", "scanDate HMS 수신");
			logMap.put("if_vender", "HMS");
			logMap.put("if_send_recv_type", "R");
			logMap.put("if_send_recv_method", "API");
			logMap.put("if_status_info", "");
			logMap.put("if_sender_id", "");
			logMap.put("if_receiver_id", "");
			logMap.put("if_date", ifDate);
			logMap.put("if_date_time", ifTime);
			logMap.put("register_user_id", "API");
			logMap.put("register_date_time", System.currentTimeMillis());
			logMap.put("es_job_no", esJobNo);
			logMap.put("if_file_path", fileBackupDir_ + "edi/hms/UPSXK/RECV/" + ifDate + "/");
			logMap.put("if_file_name", "SCANDATE_" + esJobNo);
			try{
				log.info("============================== hmsRegistPackageScanInfo | " + Util.null2str(hms.getPayload().getShipmentNo()) + " 1111111111");
				FileUtil.fileWrite(new Gson().toJson(hms), Util.null2str(logMap.get("if_file_path")), Util.null2str(logMap.get("if_file_name")));
			}catch(Exception e){
				log.error(e.getMessage());
				FileUtil.fileWrite(hms.toString(), Util.null2str(logMap.get("if_file_path")), Util.null2str(logMap.get("if_file_name")));
			}
			String shipmentNo = null;
			String scanDate = null;
			String releaseApprovalCode = null;
			String releaseApprovalNumber = null;
			
			try{
				log.info("============================== hmsRegistPackageScanInfo | " + Util.null2str(hms.getPayload().getShipmentNo()) + " 2222222222");
				shipmentNo = hms.getPayload().getShipmentNo();
				scanDate = hms.getPayload().getScanDate();
				releaseApprovalCode = hms.getPayload().getReleaseApprovalCode();
				releaseApprovalNumber = hms.getPayload().getReleaseApprovalNumber();
			}catch(Exception e){
				log.error(e.getMessage());
				errorStr = "FAIL_A";
				errorCode = "400";
			}
			logMap.put("hbl_no", shipmentNo);
			logMap.put("if_doc_reference_no", shipmentNo);
			
			if(!Util.isEmpty(shipmentNo) && !Util.isEmpty(scanDate) && !Util.isEmpty(DateTime.str2dateDateOnly(scanDate))
					&& !Util.isEmpty(releaseApprovalCode) && !Util.isEmpty(releaseApprovalNumber)){
				log.info("============================== hmsRegistPackageScanInfo | " + Util.null2str(hms.getPayload().getShipmentNo()) + " 3333333333");
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("hblNo", shipmentNo);
				map.put("scanDate", scanDate);
				map.put("outApprovalNo", releaseApprovalNumber);
				map.put("outClass", releaseApprovalCode);
				if(interfaceMapper.updateBmsOutPlanScanDate(map) == 0) {
					errorStr = "shipment not found in db";
					errorCode = "400";
				}else {
					log.info("============================== hmsRegistPackageScanInfo | " + Util.null2str(hms.getPayload().getShipmentNo()) + " 4444444444");
					interfaceMapper.updateBmsOutScanDate(map);
					
					response.setReturnCode("SUCCESS");
					response.setReturnMessage("SUCCESS");
					result.setResponse(response);
					logMap.put("if_status", "SUCCESS");
				}
			}else{
				errorStr = "FAIL_B";
				errorCode = "400";
			}
		}catch(Exception e){
			log.error(e.getMessage());
			errorStr = "FAIL_C";
			errorCode = "FAIL";
		}
		
		if(errorStr != null){
			response.setReturnCode(errorCode);
			response.setReturnMessage(errorStr);
			result.setResponse(response);
			logMap.put("if_status", "ERROR");
			logMap.put("if_remark", errorCode + " : " + errorStr);
		}
		
		log.info("============================== hmsRegistPackageScanInfo | " + Util.null2str(hms.getPayload().getShipmentNo()) + " 5555555555");
		String index = "bms-interface-logs-" + DateTime.getDate("yyyyMMdd");
		IndexApi indexApi = new IndexApi(client);
		DocumentApi documentApi = new DocumentApi(client);
		if (!indexApi.existIndex(index))	indexApi.createIndex(index);
		documentApi.createDocument(index, esJobNo, logMap);
		
		log.info("============================== hmsRegistPackageScanInfo | " + Util.null2str(hms.getPayload().getShipmentNo()) + " 6666666666");
		return result;
	}

	/**
	 * 특송 BL & Pickup 생성 API
	 * @param order
	 * @return InterfaceResult
	 * @throws Exception
	 */
	public InterfaceResult registHblInfo(HblInterface order) throws Exception {
		InterfaceResult result = new InterfaceResult();
		InterfaceReturnData returnData = new InterfaceReturnData();
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date(System.currentTimeMillis()));
			String format = "yyyyMMdd_HHmmss.SSS";
			String date = new SimpleDateFormat(format).format(cal.getTime());
			
			// XML 스키마 검증파일(XSD)
			SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			String xsd = "";
			File schemaLocation = null;
			Schema schema = null;
			Validator validator = null;
			FileReader fr = null;
	        BufferedReader br = null;
			String errorValidationLine = "";
			
			String accessKey = (Util.null2str(order.getAccessIdentifier().getAccessKey())).toUpperCase();
			String sender = (Util.null2str(order.getAccessIdentifier().getSenderIdentifier())).toUpperCase();
			String receiver = (Util.null2str(order.getAccessIdentifier().getReceiverIdentifier())).toUpperCase();
			
			String returnCode = "";
			String returnMessage = "";
			String hblNo = "";
			String accessFlag = "N";
			String path = "";
			String apiPath = customProperties.getInterfaceDir() + "api/" + receiver + "/";
			String infileName = receiver + "_" + sender + "_" + date + UUID.randomUUID().toString().substring(0, 8) + ".xml";
			String bound = "";
			String boundEs = "COM";
			String boundTypeEs = "";
			String logId = ""; // ES 로그용 아이디
			String index = ""; // ES 로그용 index
			String if_status = "RS"; // 처리상태값
			String if_status_info = "Receive Start"; // 처리상태설명
			
			Map<String, Object> tmpMap = null;
			IndexApi indexApi = null;
			DocumentApi documentApi = null;
			
			
			Map<String, Object> map = new HashMap<String, Object>();
			
			xsd = customProperties.getXsdDir() + "obl.xsd";
			bound = "EMS";
			boundTypeEs = "EXP";
			path = apiPath + "RECV/" +bound + "/"; // Interface PATH + 업체코드 경로에 저장
			
			map.put("ReceiverId", receiver);
			map.put("SenderId", sender);
			map.put("UseYN", "Y");		
			
			// Interface key 조회(ENT_CODE(ReceiverId) ,CUSTOMER_CODE(SenderId) ,ACCESS_KEY ,USE_YN) 	
			Map<String, Object> deliveryInfo = (Map<String, Object>)interfaceMapper.selectInterfaceInfo(map);
			
			if(deliveryInfo != null){
				if(accessKey.equals(Util.null2str(deliveryInfo.get("ACCESS_KEY")))){
					accessFlag = "Y"; // 요청자가 보낸 값과 동일하면 Y
				}else{
					accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
				}
			}else{
				accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
			}
			File pathDir = new File(path);
			if (pathDir.exists() == false)	pathDir.mkdirs();
			File file = new File(path + infileName);
			
	 		StringWriter writer = new StringWriter();
	 		if(order.getHblManifest() != null && order.getHblManifest().size() > 0 && order.getHblManifest().get(0).getHblInfo() != null){
	 			hblNo = order.getHblManifest().get(0).getHblInfo().getHBLNo();
	 		}
	        try{
	        	if("Y".equals(accessFlag)){
	        		
	        		JAXBContext jaxbContext = JAXBContext.newInstance(HblInterface.class);
	                Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
	                
	                jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	
	        		jaxbMarshaller.marshal(order, file);
	                jaxbMarshaller.marshal(order, writer);
	                
	    			schemaLocation = new File(xsd);
	    			schema = factory.newSchema(schemaLocation);
	    			validator = schema.newValidator();
	    			
	    			Source source = new StreamSource(file);
	    			validator.validate(source);
	        	}
			} catch(SAXException e){
	//			log.error(Util.getPrintStacTraceString(e));
				String[] errStrArray = e.toString().split("; ");
				if(errStrArray.length > 4) {
					accessFlag = "V";
					String line = null;
					int lineCnt = 0;
					String lineRow = errStrArray[2].split(": ")[1].trim();
					String msg = errStrArray[4].trim();
					
					returnMessage = "LINE (" + lineRow + ") : " + msg;
					
					fr = new FileReader(file);
					br = new BufferedReader(fr);
					
					while((line = br.readLine()) != null){
						lineCnt++;
						if(line.contains("HBLNo")){
							hblNo = line.replace("<HBLNo>", "").replace("</HBLNo>", "").trim();
						}
						if(lineCnt == Integer.parseInt(lineRow)){
							errorValidationLine = line.trim();
							break;
						}
					}
				}else{
					accessFlag = "E";
					returnMessage = e.toString();
				}
			} catch(Exception e){
	//			log.error(Util.getPrintStacTraceString(e));
				accessFlag = "E";
				returnMessage = e.toString();
			}finally{
				if(br != null){try{br.close();}catch(Exception e){log.error(e.getMessage());}}
				if(fr != null){try{fr.close();}catch(Exception e){log.error(e.getMessage());}}
			}
	        
	        if("Y".equals(accessFlag)){
	        	path = apiPath + "SUCC/" + bound + "/";
	        	pathDir = new File(path);
	    		if (pathDir.exists() == false)	pathDir.mkdirs();
	        	File fileToMove = new File(path + infileName);
	 	        file.renameTo(fileToMove);
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
	 	 			path = apiPath + "ERROR/" + bound + "/";
	 	 			pathDir = new File(path);
	 	 			if (pathDir.exists() == false)	pathDir.mkdirs();
	 	 			infileName = "ErrorValidation_" + infileName;
	 	 			File fileToMove = new File(path + infileName);
	 	 	        file.renameTo(fileToMove);
	 	 			returnMessage = returnMessage + " 에러가 발생한 부분 => " + errorValidationLine;
	 	 		}else if("E".equals(accessFlag)){
	 	 			path = apiPath + "ERROR/" + bound + "/";
	 	 			pathDir = new File(path);
	 	 			if (pathDir.exists() == false)	pathDir.mkdirs();
	 	 			infileName = "Error_" + infileName;
	 	 			File fileToMove = new File(path + infileName);
	 	 	        file.renameTo(fileToMove);
	 	 		}
	 		}
	        // history 등록 
			tmpMap = new HashMap<String, Object>();
			logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
			index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
			
	    	// 처리결과에 따른 로그 기록
	        tmpMap.clear();
			tmpMap.put("ent_code", receiver); 		 					// 도입사코드
			tmpMap.put("customer_code", sender); 		 				// 고객코드
			tmpMap.put("solution", "EMS");      		 				// SOLUTION
			tmpMap.put("bound", boundEs);		 	 					// 바운드 (AIR)
			tmpMap.put("bound_type", boundTypeEs);	 					// 바운드타입
			tmpMap.put("hbl_no", Util.null2str(hblNo));				 	// HBL_NO
			tmpMap.put("if_service_code", bound);
			tmpMap.put("if_doc_code", "ORDER_API");
			tmpMap.put("if_doc_name", "오더API");
			tmpMap.put("if_doc_reference_no", "");	 					// 문서번호 
			tmpMap.put("if_vender", sender);							// 중개사업자
			tmpMap.put("if_send_recv_type", "R");						// 송수신타입(S:SEND, R:RECV)
			tmpMap.put("if_send_recv_method", "XML To DB");				// 송수신방법
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
			
			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			documentApi = new DocumentApi(client);
			documentApi.createDocument(index, logId, tmpMap);
	        
			returnData.setReturnCode(returnCode);
			returnData.setReturnMessage(returnMessage);
			
			result.setReturnData(returnData);		
		}catch(Exception e){
			log.error(Util.getPrintStacTraceString(e));
			returnData.setReturnCode("FAIL");
			returnData.setReturnMessage("System Error.");
			result.setReturnData(returnData);
		}
		return result;
	}
	
	/**
	 * 특송 BL & Pickup 트레이킹 조회 API
	 * @param tracking
	 * @return list
	 * @throws Exception
	 */
	public HblTrackingResult hblTracking(HblTrackingInterface tracking, String entCode) throws Exception {
		
		HblTrackingResult hblTrackingResult = new HblTrackingResult();
		
		Map<String, Object> tmpMap = null;
		String logId = ""; 			// ES 로그용 아이디
		String index = ""; 			// ES 로그용 index
		IndexApi indexApi = null;
		DocumentApi documentApi = null;
		
		int odCnt = tracking.getBlTracking().size();
		List<HblReturnTracking> list = new ArrayList<HblReturnTracking>();
		List<HblReturnTracking> tempList = new ArrayList<HblReturnTracking>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		InterfaceReturnData returnData = new InterfaceReturnData();
		
		String errHblNos = "";
		int errHblCnt = 0;
		
		StringWriter writer = new StringWriter();
		JAXBContext jaxbContext = JAXBContext.newInstance(HblTrackingInterface.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.marshal(tracking, writer);
		
        // XML 스키마 검증파일(XSD)
 		SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
 		File schemaLocation = null;
 		Schema schema = null;
 		Validator validator = null;
 		Source source = null;

 		String xsd = customProperties.getXsdDir() + "oblTracking.xsd";
 		String returnCode = "";
 		String returnMessage = "";
 		String receiver = tracking.getAccessIdentifier().getReceiverIdentifier();
 		String searchYN = "Y";
 		String accessKey = tracking.getAccessIdentifier().getAccessKey();
 		String sender = (Util.null2str(tracking.getAccessIdentifier().getSenderIdentifier())).toUpperCase();
 		String accessFlag = "N";
     	
 		paramMap.put("UseYN", "Y");
 		paramMap.put("ReceiverId", receiver);
 		paramMap.put("SenderId", sender);
 		paramMap.put("entCode", entCode);
 		
		Map<String, Object> interfaceMap = (Map<String, Object>)interfaceMapper.selectInterfaceInfo(paramMap);

		if (interfaceMap != null) {
			if(accessKey.equals(Util.null2str(interfaceMap.get("ACCESS_KEY")))) {
				accessFlag = "Y";
			}
		} else {
			accessFlag = "X";
		}
		
		if ("Y".equals(accessFlag)) {
			
			try {
				schemaLocation = new File(xsd);
				schema = factory.newSchema(schemaLocation);
				validator = schema.newValidator();
				source = new StreamSource(new ByteArrayInputStream(writer.toString().getBytes()));
				validator.validate(source);
			} catch (SAXException e) {
				log.error(Util.getPrintStacTraceString(e));
				String[] errStrArray = e.toString().split(";");
				if(errStrArray.length > 3) {
					accessFlag = "V";
					returnMessage = errStrArray[3].split(":")[1].trim();
				}else{
					accessFlag = "E";
					returnMessage = e.toString();
				}
				returnCode = "FAIL";
			} catch (Exception e) {
				log.error(Util.getPrintStacTraceString(e));
				accessFlag = "E";
				returnCode = "FAIL";
				returnMessage = e.toString();
			}
			
			for (int i = 0; i < tracking.getBlTracking().size(); i++) {
				paramMap.put("hblNo", tracking.getBlTracking().get(i).getHblNo());
				tempList = interfaceMapper.selectOblTracking(paramMap);
				if (tempList.size() < 1) {
					searchYN = "N";
					errHblNos += "," + Util.null2str(paramMap.get("hblNo"));	//전달받은 내용중 잘못된 hblNo는 모아서 returnMessage에 표시해줌.
					errHblCnt++;
				}else {
					list.addAll(tempList);
				}
			}

			if ("Y".equals(searchYN)) {
				returnData.setReturnCode("SUCCESS");
				returnData.setReturnMessage("");
//			} else if ("D".equals(searchYN)) {
//				returnData.setReturnCode("FAIL");
//				returnData.setReturnMessage("H.B/L No duplication.Please contact us " + receiver + ".");
			} else {
				if(odCnt == errHblCnt) {
					returnData.setReturnCode("FAIL");
				}else {
					returnData.setReturnCode("PARTIAL SUCCESS");	//부분성공
				}
				returnData.setReturnMessage("data does not exist : " + errHblNos.substring(1));
			}
		} else if ("N".equals(accessFlag)) {
			returnData.setReturnCode("FAIL");
			returnData.setReturnMessage("Wrong AccessKey");
		} else if ("X".equals(accessFlag)) {
			returnData.setReturnCode("FAIL");
			returnData.setReturnMessage("Not a service provider");
		} else {
			returnData.setReturnCode(returnCode);
			returnData.setReturnMessage(returnMessage);
		}
		
		//ES로그 추가 
		tmpMap = new HashMap<String, Object>();
		logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
		index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
		
		// 처리결과에 따른 로그 기록 history
		tmpMap.clear();
		tmpMap.put("ent_code", receiver); 		 							 // 도입사코드
		tmpMap.put("customer_code", sender); 		 						 // 고객코드
		tmpMap.put("solution", "EMS");      		 						 // SOLUTION
		tmpMap.put("bound", "COM");		 	 							 	 // 바운드
		tmpMap.put("bound_type", "COM"); 							 		 // 바운드타입
		tmpMap.put("hbl_no", "");				 							 // HBL_NO
		tmpMap.put("if_service_code", "TRACKING");
		tmpMap.put("if_doc_code", "TRACKING_API");
		tmpMap.put("if_doc_name", "트래킹API"); 			 			 		 // 문서분류명 (AIR ORDER, SEA ORDER)
		tmpMap.put("if_doc_reference_no", "");	 							 // 문서번호 
		tmpMap.put("if_vender", sender);									 // 중개사업자
		tmpMap.put("if_send_recv_type", "");			 					 // 송수신타입(S:SEND, R:RECV)
		tmpMap.put("if_send_recv_method", "");		 				 		 // 송수신방법
		tmpMap.put("if_file_path", "");			 						 	 // 파일경로
		tmpMap.put("if_file_name", "");			 				 	 		 // 파일명
		if("FAIL".equals(returnData.getReturnCode())){
			tmpMap.put("if_status", "ERROR");
		}else if("PARTIAL SUCCESS".equals(returnData.getReturnCode())){
			tmpMap.put("if_status", "SUCCESS");
		}else{
			tmpMap.put("if_status", returnData.getReturnCode());			 	 // 처리상태
		}
		tmpMap.put("if_status_info", returnData.getReturnMessage());		 // 송수신상태_설명
		tmpMap.put("if_sender_id", sender);									 // 송신식별자
		tmpMap.put("if_sender_name", "");		 							 // 송신자명
		tmpMap.put("if_receiver_id", receiver);		 						 // 수신식별자
		tmpMap.put("if_receiver_name", "");	     							 // 수신자명
		tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));				 // DATE
		tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));			 // DATE_TIME
		tmpMap.put("if_remark", returnMessage);			 					 // 비고( 에러로그 등)
		tmpMap.put("order_cnt", odCnt);										 // 트래킹API 건수 -> 처리결과(성공/실패/부분성공)에 상관없이 api로 들어온 hbl건수를 모두 더함.
		tmpMap.put("register_user_id", "API");
		tmpMap.put("register_user_name", "API");
		tmpMap.put("register_date_time", System.currentTimeMillis());
		tmpMap.put("es_job_no", logId);
		
		indexApi = new IndexApi(client);
		if (!indexApi.existIndex(index))	indexApi.createIndex(index);
		
		documentApi = new DocumentApi(client);
		documentApi.createDocument(index, logId, tmpMap);
		
		hblTrackingResult.setReturnData(returnData);
		hblTrackingResult.setBlTracking(list);
		
		writer = new StringWriter();
		jaxbContext = JAXBContext.newInstance(HblTrackingResult.class);
        jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.marshal(hblTrackingResult, writer);
        
		return hblTrackingResult;
	}
	
	/**
	 * ECM 인터페이스 사용량(ORDER_CNT, DELIVERY_CODE_CNT) 업데이트
	 * @param hanjinPramMap
	 * @return Map
	 * @throws Exception
	 */	
	public void updateInterfaceCnt(Map<String, Object> paramMap) throws Exception {
		interfaceMapper.updateInterfaceCnt(paramMap);
	}
	
	public List<Map<String, Object>> selectOrderTrackingList(HttpServletRequest req, Tracking_interface track, String bound, String boundType) throws Exception {
		
		Map<String, Object> tmpMap = null;
		String logId = ""; 			// ES 로그용 아이디
		String index = ""; 			// ES 로그용 index
		IndexApi indexApi = null;
		DocumentApi documentApi = null;
		
		int odCnt = track.getOrderTracking().size();
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String errHblNos = "";
		int errHblCnt = 0;
		
		StringWriter writer = new StringWriter();
		JAXBContext jaxbContext = JAXBContext.newInstance(Tracking_interface.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.marshal(track, writer);
		
        // XML 스키마 검증파일(XSD)
 		SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
 		File schemaLocation = null;
 		Schema schema = null;
 		Validator validator = null;
 		Source source = null;

 		String xsd = customProperties.getXsdDir() + "orderTracking.xsd";
 		String returnCode = "";
 		String returnMessage = "";
 		String receiver = track.getAccessIdentifier().getReceiverIdentifier();
 		String searchYN = "Y";
 		String accessKey = track.getAccessIdentifier().getAccessKey();
 		String sender = (Util.null2str(track.getAccessIdentifier().getSenderIdentifier())).toUpperCase();
 		String accessFlag = "N";
     	
 		Util.setDB(req, receiver);
 		
 		paramMap.put("ReceiverId", receiver);
 		paramMap.put("SenderId", sender);
 		paramMap.put("UseYN", "Y");
 		
		Map<String, Object> interfaceMap = (Map<String, Object>)interfaceMapper.selectInterfaceInfo(paramMap);

		if (interfaceMap != null) {
			if(accessKey.equals(Util.null2str(interfaceMap.get("ACCESS_KEY")))) {
				accessFlag = "Y";
			}
		} else {
			accessFlag = "X";
		}
		
		if ("Y".equals(accessFlag)) {
			
			try {
				schemaLocation = new File(xsd);
				schema = factory.newSchema(schemaLocation);
				validator = schema.newValidator();
				source = new StreamSource(new ByteArrayInputStream(writer.toString().getBytes()));
				validator.validate(source);
			} catch (SAXException e) {
				log.error(Util.getPrintStacTraceString(e));
				String[] errStrArray = e.toString().split(";");
				if(errStrArray.length > 3) {
					accessFlag = "V";
					returnMessage = errStrArray[3].split(":")[1].trim();
				}else{
					accessFlag = "E";
					returnMessage = e.toString();
				}
				returnCode = "FAIL";
			} catch (Exception e) {
				log.error(Util.getPrintStacTraceString(e));
				accessFlag = "E";
				returnCode = "FAIL";
				returnMessage = e.toString();
			}
		}
		
		if("Y".equals(accessFlag)) {
			//수입인경우엔 오더번호나 hbl둘중 하나만 입력해도 매핑되게 동적쿼리구성
			for (int i = 0; i < track.getOrderTracking().size(); i++) {
				String language = "ENG";
				if(!"".equals(Util.null2str(track.getOrderTracking().get(i).getLanguage()))) {
					language = Util.null2str(track.getOrderTracking().get(i).getLanguage());
				}
				paramMap.put("language", language);
				paramMap.put("ordId", track.getOrderTracking().get(i).getOrdId());
				paramMap.put("hblNo", track.getOrderTracking().get(i).getHBLNo());
				paramMap.put("bound", bound);
				paramMap.put("boundType", boundType);
				tempList = interfaceMapper.selectOrderTracking(paramMap);
				if (tempList.size() < 1) {
					searchYN = "N";
					if(!Util.null2str(errHblNos).equals("")) {
						errHblNos += ",";
					}
					errHblNos += Util.null2str(paramMap.get("hblNo"));	//전달받은 내용중 잘못된 hblNo는 모아서 returnMessage에 표시해줌.
					errHblCnt++;
				}else {
					list.addAll(tempList);
				}
				
				if(boundType.equals("IMP")) {
					tempList = interfaceMapper.selectOrderTrackingCustomsInfo(paramMap); 
					if (tempList.size() == 1) {
						list.addAll(tempList);
						tempList = interfaceMapper.selectOrderTrackingMultiInfo(paramMap);
						if(tempList.size() > 0) {
							list.addAll(tempList);
						}
					} else if (tempList.size() > 1) {
						searchYN = "D";
						list.clear();
					}
				}
			}
			
			if ("Y".equals(searchYN)) {
				resultMap.put("ReturnCode", "SUCCESS");
				resultMap.put("ReturnMessage", "");
			} else if ("D".equals(searchYN)) {
				resultMap.put("ReturnCode", "FAIL");
				resultMap.put("ReturnMessage", "H.B/L No duplication.Please contact us " + receiver + ".");
			} else {
				if(odCnt == errHblCnt) {
					resultMap.put("ReturnCode", "FAIL");
				}else {
					resultMap.put("ReturnCode", "PARTIAL SUCCESS");	//부분성공
				}
				resultMap.put("ReturnMessage", "data does not exist : " + errHblNos);
			}
		} else if ("N".equals(accessFlag)) {
			resultMap.put("ReturnCode", "FAIL");
			resultMap.put("ReturnMessage", "Wrong AccessKey");
		} else if ("X".equals(accessFlag)) {
			resultMap.put("ReturnCode", "FAIL");
			resultMap.put("ReturnMessage", "Not a service provider");
		} else {
			resultMap.put("ReturnCode", returnCode);
			resultMap.put("ReturnMessage", returnMessage);
		}
		list.add(resultMap);
		
		//ES로그 추가 
		tmpMap = new HashMap<String, Object>();
		logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
		index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
		
		// 처리결과에 따른 로그 기록 history
		tmpMap.clear();
		tmpMap.put("ent_code", receiver); 		 							 // 도입사코드
		tmpMap.put("customer_code", sender); 		 						 // 고객코드
		tmpMap.put("solution", "ECM");      		 						 // SOLUTION
		tmpMap.put("bound", "");		 	 							 	 // 바운드
		tmpMap.put("bound_type", "");	 							 		 // 바운드타입
		tmpMap.put("hbl_no", "");				 							 // HBL_NO
		tmpMap.put("if_service_code", "TRACKING");
		tmpMap.put("if_doc_code", "TRACKING_API");
		tmpMap.put("if_doc_name", "트래킹API"); 			 			 		 // 문서분류명 (AIR ORDER, SEA ORDER)
		tmpMap.put("if_doc_reference_no", "");	 							 // 문서번호 
		tmpMap.put("if_vender", sender);									 // 중개사업자
		tmpMap.put("if_send_recv_type", "");			 					 // 송수신타입(S:SEND, R:RECV)
		tmpMap.put("if_send_recv_method", "");		 				 		 // 송수신방법
		tmpMap.put("if_file_path", "");			 						 	 // 파일경로
		tmpMap.put("if_file_name", "");			 				 	 		 // 파일명
		if("FAIL".equals(resultMap.get("ReturnCode"))){
			tmpMap.put("if_status", "ERROR");
		}else if("PARTIAL SUCCESS".equals(resultMap.get("ReturnCode"))){
			tmpMap.put("if_status", "SUCCESS");
		}else{
			tmpMap.put("if_status", resultMap.get("ReturnCode"));			 	 // 처리상태
		}
		tmpMap.put("if_status_info", resultMap.get("ReturnMessage"));		 // 송수신상태_설명
		tmpMap.put("if_sender_id", sender);									 // 송신식별자
		tmpMap.put("if_sender_name", "");		 							 // 송신자명
		tmpMap.put("if_receiver_id", receiver);		 						 // 수신식별자
		tmpMap.put("if_receiver_name", "");	     							 // 수신자명
		tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));				 // DATE
		tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));			 // DATE_TIME
		tmpMap.put("if_remark", returnMessage);			 					 // 비고( 에러로그 등)
		tmpMap.put("order_cnt", odCnt);										 // 트래킹API 건수 -> 처리결과(성공/실패/부분성공)에 상관없이 api로 들어온 hbl건수를 모두 더함.
		tmpMap.put("register_user_id", "API");
		tmpMap.put("register_user_name", "API");
		tmpMap.put("register_date_time", System.currentTimeMillis());
		tmpMap.put("es_job_no", logId);
		
		indexApi = new IndexApi(client);
		if (!indexApi.existIndex(index))	indexApi.createIndex(index);
		
		documentApi = new DocumentApi(client);
		documentApi.createDocument(index, logId, tmpMap);
					
		return list;
	}
	
	public TrackingResult convertPodReturnData (List<Map<String, Object>> list) throws JAXBException {
		List<ReturnTrackingData> trackingDataList = new ArrayList<ReturnTrackingData>();
		List<CustomsInfo> cusList = new ArrayList<CustomsInfo>();
		List<MultiInfo> mulList = new ArrayList<MultiInfo>();
		List<InventoryInfo> invList = new ArrayList<InventoryInfo>();
		
		TrackingResult trackingResult = new TrackingResult();
		InterfaceReturnData returnData = new InterfaceReturnData();
		ReturnTrackingData trackingData = null;
		CustomsInfo cus = null;
		MultiInfo mul = null;
		InventoryInfo inv = null;
		
		int index = 0;
		
		for(Map<String, Object> dataMap : list) {
			if(index == (list.size() - 1)){
				returnData.setReturnCode(dataMap.get("ReturnCode").toString());
				returnData.setReturnMessage(dataMap.get("ReturnMessage").toString());
				trackingResult.setReturnData(returnData);
			}
			
			if(dataMap != null){
				if(dataMap.get("POD_STATUS") != null){
					trackingData = new ReturnTrackingData();
					trackingData.setOrdId(Util.null2str(dataMap.get("PO_NO")));
					trackingData.setHBLNo(Util.null2str(dataMap.get("HBL_NO")));
//					trackingData.setTrackingCode(Util.null2str(dataMap.get("ETC16")));
					trackingData.setTrackingCode(Util.null2str(dataMap.get("POD_STATUS")));
					trackingData.setTrackingDateTime(Util.null2str(dataMap.get("TIME")));
					trackingData.setTrackingCodeName(Util.null2str(dataMap.get("TRACKING_CODE_NAME")));
					trackingData.setTrackingRemark(Util.null2str(dataMap.get("REMARKS")));
					trackingDataList.add(trackingData);
					
//					if("".equals(Util.null2str(dataMap.get("ETC16")))){
//						trackingData = new ReturnTrackingData();
//						trackingData.setOrdId(dataMap.get("PO_NO").toString());
//						trackingData.setHBLNo(dataMap.get("HBL_NO").toString());
//						trackingData.setTrackingDateTime(Util.null2str(dataMap.get("TIME")));
//						trackingData.setTrackingRemark("");
//						trackingData.setTrackingCode("TDEP");
//						trackingDataList.add(trackingData);
//					} else if("TARR".equals(trackingData.getTrackingCode().toString())){
//						trackingData = new ReturnTrackingData();
//						trackingData.setOrdId(dataMap.get("PO_NO").toString());
//						trackingData.setHBLNo(dataMap.get("HBL_NO").toString());
//						trackingData.setTrackingDateTime(Util.null2str(dataMap.get("TIME")));
//						trackingData.setTrackingRemark("");
//						trackingData.setTrackingCode("TDLA");
//						trackingDataList.add(trackingData);
//					}
				}
				
				if(dataMap.get("CUSTOMS_CLEARANCE") != null){
					cus = new CustomsInfo();
					cus.setOrdId(Util.null2str(dataMap.get("PO_NO")));
					cus.setHBLNo(Util.null2str(dataMap.get("HBL_NO")));
					cus.setCustomsClearance(Util.null2str(dataMap.get("CUSTOMS_CLEARANCE")));
					cus.setInspection(Util.null2str(dataMap.get("INSPECTION")));
					cus.setWithdrawalCode(Util.null2str(dataMap.get("DROP_CODE")));
					cus.setWithdrawalReason(Util.null2str(dataMap.get("DROP_REASON")));
					cus.setDeliveryCode(Util.null2str(dataMap.get("DELIVERY_CODE")));
					cusList.add(cus);
					
					inv = new InventoryInfo();
					if(Util.null2str(dataMap.get("HOLD_CODE")).equals("")) {
						inv.setHoldYN(Util.null2str(dataMap.get("HOLD_YN")));
					}else {
						inv.setHoldYN(dataMap.get("HOLD_YN") + "|" + Util.null2str(dataMap.get("HOLD_CODE")) + "|" + Util.null2str(dataMap.get("HOLD_REASON")));
					}
					if(Util.null2str(dataMap.get("WH_LOCATION")).equals("")) {
						inv.setLocation(Util.null2str(dataMap.get("WH_LOCATION")));
					}else {
						inv.setLocation(Util.null2str(dataMap.get("WH_LOCATION")) + "|" + Util.null2str(dataMap.get("WH_LOCATION_NAME")));
					}
					inv.setStockDateTime(Util.null2str(dataMap.get("STOCK_DATE_TIME")));
					inv.setStockUserName(Util.null2str(dataMap.get("STOCK_USER_NAME")));
					inv.setCarryoutDate(Util.null2str(dataMap.get("CARRYOUT_DATE")));
					inv.setCarryoutUser(Util.null2str(dataMap.get("CARRYOUT_USER")));
					invList.add(inv);
				}
				
				if(dataMap.get("SEQ_NO_ID") != null) {
					mul = new MultiInfo();
					mul.setDeliveryNo(Util.null2str(dataMap.get("DELIVERY_NO")));
					mul.setChangeDeliveryCode(Util.null2str(dataMap.get("DELIVERY_CODE")));
					mul.setChangeDeliveryNo(Util.null2str(dataMap.get("DELIVERY_NO_CHANGED")));
					mulList.add(mul);
				}
			}
			index++;
		}
		
		trackingResult.setReturnTrackingData(trackingDataList);
		trackingResult.setCustomsInfo(cusList);
		trackingResult.setMultiInfo(mulList);
		trackingResult.setInventoryInfo(invList);
		
		StringWriter writer = new StringWriter();
		JAXBContext jaxbContext1 = JAXBContext.newInstance(TrackingResult.class);
        Marshaller jaxbMarshaller1 = jaxbContext1.createMarshaller();
        jaxbMarshaller1.marshal(trackingResult, writer);
		return trackingResult;
	}
	
	/**
	 * EMS 환적 수입(AI/OI) 오더 생성 API
	 * @param order
	 * @return InterfaceResult
	 * @throws Exception
	 */
	public EmsReturn emsRegistOrderInfo(EmsInterface order, HttpServletRequest req) throws Exception {
		EmsReturn emsReturn = new EmsReturn();
		try {
			String requestUrl = (String)req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE); // URL에 따른 분기처리
//			Calendar cal = Calendar.getInstance();
//			cal.setTime(new Date(System.currentTimeMillis()));
//			String format = "yyyyMMdd_HHmmss.SSS";
//			String date = new SimpleDateFormat(format).format(cal.getTime());
			String format = "yyyyMMdd_HHmmss.SSSSSSS";
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
			String date = now.format(dtf);
			
			String json = "";
			File schemaLocation = null;
			String errorValidationLine = "";
			String errMsg = "";
			
			String accessKey = (Util.null2str(order.getAccessIdentifier().getAccessKey())).toUpperCase();
			String sender = (Util.null2str(order.getAccessIdentifier().getSenderIdentifier())).toUpperCase();
			String receiver = (Util.null2str(order.getAccessIdentifier().getReceiverIdentifier())).toUpperCase();
			
			String returnCode = "";
			String returnMessage = "";
			String hblNo = "";
			String accessFlag = "N";
			String path = "";
			String apiPath = customProperties.getInterfaceDir() + "api/" + receiver + "/";
			String infileName = receiver + "_" + sender + "_" + date + UUID.randomUUID().toString().substring(0, 8) + ".txt";
			String bound = "EMS/";
			String boundEs = "";
			String boundTypeEs = "IMP";
			String logId = ""; // ES 로그용 아이디
			String index = ""; // ES 로그용 index
			String if_status = "RS"; // 처리상태값
			String if_status_info = "Receive Start"; // 처리상태설명
			
			Map<String, Object> tmpMap = null;
			IndexApi indexApi = null;
			DocumentApi documentApi = null;
			
			Map<String, Object> map = new HashMap<String, Object>();
			BufferedWriter bw = null;
			FileOutputStream fos = null;
			OutputStreamWriter osw = null;
			
			if(requestUrl.contains("registAIOrderInfo")) {
				json = customProperties.getXsdDir() + "ai.json";
				boundEs = "AIR";
				bound += "AIR";
			}else if(requestUrl.contains("registOIOrderInfo")) {
				json = customProperties.getXsdDir() + "oi.json";
				boundEs = "SEA";
				bound += "SEA";
			}
			
			path = apiPath + "RECV/" +bound + "/"; // Interface PATH + 업체코드 경로에 저장
			
			map.put("ReceiverId", receiver);
			map.put("SenderId", sender);
			map.put("UseYN", "Y");		
			
			// Interface key 조회(ENT_CODE(ReceiverId) ,CUSTOMER_CODE(SenderId) ,ACCESS_KEY ,USE_YN) 	
			Map<String, Object> deliveryInfo = (Map<String, Object>)interfaceMapper.selectInterfaceInfo(map);
			
			if(deliveryInfo != null){
				if(accessKey.equals(Util.null2str(deliveryInfo.get("ACCESS_KEY")))){
					accessFlag = "Y"; // 요청자가 보낸 값과 동일하면 Y
				}else{
					accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
				}
			}else{
				accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
			}
			
			File pathDir = new File(path);
			if (pathDir.exists() == false)	pathDir.mkdirs();
			File file = new File(path + infileName);
			
	 		StringWriter writer = new StringWriter();
	 		if(order.getOrderManifest() != null && order.getOrderManifest().size() > 0 && order.getOrderManifest().get(0).getOrderInfo() != null){
	 			hblNo = order.getOrderManifest().get(0).getOrderInfo().getHblNo();
	 		}
        	if("Y".equals(accessFlag)){
        		try{
	        		ObjectMapper objectMapper = new ObjectMapper(); 
	        		
	        		errMsg = "";
	        		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909); 
	        		
	        		schemaLocation = new File(json);
					InputStream schemaStream = new FileInputStream(schemaLocation);
	        		
					// 스트림에서 데이터를 읽고 JsonNode에 저장  
					JsonNode jsonNode = objectMapper.valueToTree(order);
					//schemaStream에서 스키마를 가져와 JsonSchema에 저장  
			        com.networknt.schema.JsonSchema schema = factory.getSchema(schemaStream);
			        //유효성 검사 메시지 집합을 생성하고 결과를 저장 
			        Set<ValidationMessage> validationResult  = schema.validate(jsonNode);  
			        
			        if(validationResult.isEmpty()) {
			        	returnMessage = "There is no validation errors";
			        }else {
			        	accessFlag = "V";
			        	int rmidx = 0;
			        	
			        	//오류 메세지
			        	for(ValidationMessage vm : validationResult) {
			        		rmidx = vm.getMessage().indexOf(":");
			        		errorValidationLine = vm.getMessage().substring(0, rmidx).replace("$.", "");
			        		returnMessage = vm.getMessage().substring(rmidx+2);
//			        		System.out.println(errorValidationLine + " " + returnMessage);
		        		}
		        	}
				} catch(Exception e){
					accessFlag = "E";
					returnMessage = e.toString();
				}
        	}
	        
	        if("Y".equals(accessFlag)){
	        	ObjectMapper mapper = new ObjectMapper();
	        	String samString = mapper.writeValueAsString(order);
	        	path = apiPath + "SUCC/" + bound + "/";
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
	 				String samString = mapper.writeValueAsString(order);
	 	 			path = apiPath + "ERROR/" + bound + "/";
	 	 			pathDir = new File(path);
	 	 			if (pathDir.exists() == false)	pathDir.mkdirs();
	 	 			infileName = "ErrorValidation_" + infileName;
	 	 			fos = new FileOutputStream(path + infileName);
	 	 			osw = new OutputStreamWriter(fos, "UTF-8");
	 	 			bw = new BufferedWriter(osw);
	 	 			bw.write(Util.null2str(samString));
	 	 			bw.close();
	 	 			returnMessage = " 에러가 발생한 부분 => " + errorValidationLine + " " + returnMessage;
	 	 		}else if("E".equals(accessFlag)){
	 	 			ObjectMapper mapper = new ObjectMapper();
	 				String samString = mapper.writeValueAsString(order);
	 	 			path = apiPath + "ERROR/" + bound + "/";
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
			tmpMap.put("if_doc_code", "ORDER_API");
			tmpMap.put("if_doc_name", "환적오더API");
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
			
			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			documentApi = new DocumentApi(client);
			documentApi.createDocument(index, logId, tmpMap);
	        
			emsReturn.setReturnCode(returnCode);
			emsReturn.setReturnMessage(returnMessage);
		}catch(Exception e){
			log.error(Util.getPrintStacTraceString(e));
			emsReturn.setReturnCode("FAIL");
			emsReturn.setReturnMessage("System Error.");
		}
		return emsReturn;
	}
	
	/**
	 * EMS 환적 송장 호출 API
	 * @param order
	 * @param req
	 * @throws Exception
	 */
	public EmsResult emsSelectRequestBL(HttpServletRequest req, EmsRequestBLnterface reqBl) throws Exception {
		EmsResult emsResult = new EmsResult();
		EmsReturn emsReturn = new EmsReturn();
		
		try {
			String json = customProperties.getXsdDir() + "emsOrderRequestBL.json";
			File schemaLocation = null;
			String errorValidationLine = "";
			
			String accessKey = (Util.null2str(reqBl.getAccessIdentifier().getAccessKey())).toUpperCase();
			String sender = (Util.null2str(reqBl.getAccessIdentifier().getSenderIdentifier())).toUpperCase();
			String receiver = (Util.null2str(reqBl.getAccessIdentifier().getReceiverIdentifier())).toUpperCase();
			
			String returnMessage = "";
			String hblNo = "";
			String searchYN = "N";
			String accessFlag = "N";
			String logId = ""; // ES 로그용 아이디
			String index = ""; // ES 로그용 index
			String if_status = "RS"; // 처리상태값
			String if_status_info = "Receive Start"; // 처리상태설명
			
			List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
			Map<String, Object> tmpMap = null;
			Map<String, Object> map = new HashMap<String, Object>();
			IndexApi indexApi = null;
			DocumentApi documentApi = null;
			
			map.put("ReceiverId", receiver);
			map.put("SenderId", sender);
			map.put("UseYN", "Y");		
			
			Map<String, Object> deliveryInfo = (Map<String, Object>)interfaceMapper.selectInterfaceInfo(map);
			
			if(deliveryInfo != null){
				if(accessKey.equals(Util.null2str(deliveryInfo.get("ACCESS_KEY")))){
					accessFlag = "Y"; // 요청자가 보낸 값과 동일하면 Y
				}else{
					accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
				}
			}else{
				accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
			}
			
			if("Y".equals(accessFlag)){
				try{
	        		ObjectMapper objectMapper = new ObjectMapper(); 
	        		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909); 
	        		
	        		schemaLocation = new File(json);
					InputStream schemaStream = new FileInputStream(schemaLocation);
	        		
					// 스트림에서 데이터를 읽고 JsonNode에 저장  
					JsonNode jsonNode = objectMapper.valueToTree(reqBl);
					//schemaStream에서 스키마를 가져와 JsonSchema에 저장  
			        com.networknt.schema.JsonSchema schema = factory.getSchema(schemaStream);
			        //유효성 검사 메시지 집합을 생성하고 결과를 저장 
			        Set<ValidationMessage> validationResult  = schema.validate(jsonNode);  
			        
			        if(validationResult.isEmpty()) {
			        	returnMessage = "";
			        }else {
			        	accessFlag = "V";
			        	int rmidx = 0;
			        	
			        	//오류 메세지
			        	for(ValidationMessage vm : validationResult) {
			        		rmidx = vm.getMessage().indexOf(":");
			        		errorValidationLine = vm.getMessage().substring(0, rmidx).replace("$.", "");
			        		returnMessage = errorValidationLine + " " + vm.getMessage().substring(rmidx+2);
		        		}
		        	}
				} catch(Exception e){
					accessFlag = "E";
					returnMessage = e.toString();
				}
			}
			
			if("Y".equals(accessFlag)){
				OrderInvoice orderInvoice = reqBl.getOrderInvoice();
				map.put("ordId", orderInvoice.getOrdId());
				int poNoCnt = interfaceMapper.selectPoNoCnt(map);
				tempList = interfaceMapper.selectEmsOrderRequestBl(map);
				
				// poNo 데이터가 존재하지 않을 때 처리
				if(poNoCnt < 1) {
					searchYN = "N";
					returnMessage = "Order Id data does not exist. (Order Id: " + Util.null2str(map.get("ordId")) + ")";
				}else if(poNoCnt > 1) {
					searchYN = "N";
					returnMessage = "No matching invoice file";
				}else {
					// EMS환적 오더가 아닐 때 처리
					if(!"Y".equals(Util.null2str(tempList.get(0).get("EMS_YN")))) {
						searchYN = "N";
						returnMessage = "Not a ems Data";
					}else if(tempList.get(0).get("MAIL_TYPE") != null) {
						searchYN = "Y";
						hblNo = Util.null2str(tempList.get(0).get("HBL_NO"));
						orderInvoice.setHblNo(hblNo);
						
						String mailType = Util.null2str(tempList.get(0).get("MAIL_TYPE"));
						String printDeliveryCode = "";
						String fileName = Util.null2str(tempList.get(0).get("OD_JOB_NO")) + "_label";
						String poDate = Util.null2str(tempList.get(0).get("PO_DATE"));
						String datePath = poDate.substring(0,4)+"/"+poDate.substring(5,7)+"/"+poDate.substring(8,10)+"/";
						
						if(mailType.equals("E")) {
							printDeliveryCode = "ems-ts";
						}else if(mailType.equals("K")) {
							printDeliveryCode = "ems-kp";
						}
						
						String base64Str = imageToBase64(customProperties.getLabelDir()+printDeliveryCode+"/"+datePath,fileName,"pdf");
						
						if(!"".equals(Util.null2str(base64Str))) {
							orderInvoice.setInvoice(base64Str);
						}
						emsResult.setOrderInvoice(orderInvoice);
					}
				}
				if ("Y".equals(searchYN)) {
					emsReturn.setReturnCode("SUCCESS");
					emsReturn.setReturnMessage("");
				} else {
					emsReturn.setReturnCode("FAIL");
					emsReturn.setReturnMessage(returnMessage);
				} 
	 		} else if ("N".equals(accessFlag)) {
	 			emsReturn.setReturnCode("FAIL");
				emsReturn.setReturnMessage("Wrong AccessKey");
			} else if ("X".equals(accessFlag)) {
				emsReturn.setReturnCode("FAIL");
				emsReturn.setReturnMessage("Not a service provider");
			} else {
				emsReturn.setReturnCode("FAIL");
				emsReturn.setReturnMessage(returnMessage);
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
			tmpMap.put("bound", "");		 	 						// 바운드 (AIR)
			tmpMap.put("bound_type", "IMP");	 						// 바운드타입
			tmpMap.put("hbl_no", Util.null2str(hblNo));					// HBL_NO
			tmpMap.put("if_service_code", "REQUESTBL");
			tmpMap.put("if_doc_code", "REQUESTBL");
			tmpMap.put("if_doc_name", "환적송장호출API");
			tmpMap.put("if_doc_reference_no", "");						// 문서번호 
			tmpMap.put("if_vender", sender);							// 중개사업자
			tmpMap.put("if_send_recv_type", "");						// 송수신타입(S:SEND, R:RECV)
			tmpMap.put("if_send_recv_method", "");						// 송수신방법
			tmpMap.put("if_file_path", "");			 					// 파일경로
			tmpMap.put("if_file_name", "");			 					// 파일명
			if("FAIL".equals(emsReturn.getReturnCode())){				// 처리상태
				tmpMap.put("if_status", "ERROR");
			}else{
				tmpMap.put("if_status", emsReturn.getReturnCode());
			}
			tmpMap.put("if_status_info", emsReturn.getReturnMessage());	// 송수신상태_설명
			tmpMap.put("if_sender_id", sender);							// 송신식별자
			tmpMap.put("if_sender_name", "");		 					// 송신자명
			tmpMap.put("if_receiver_id", receiver);		 				// 수신식별자
			tmpMap.put("if_receiver_name", "");	     					// 수신자명
			tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));		// DATE
			tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));	// DATE_TIME
			tmpMap.put("if_remark", returnMessage);			 			// 비고( 에러로그 등)
			tmpMap.put("register_user_id", "API");
			tmpMap.put("register_user_name", "API");
			tmpMap.put("register_date_time", System.currentTimeMillis());
			tmpMap.put("es_job_no", logId);
			
			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			documentApi = new DocumentApi(client);
			documentApi.createDocument(index, logId, tmpMap);
			
		}catch(Exception e){
			log.error(Util.getPrintStacTraceString(e));
			emsReturn.setReturnCode("FAIL");
			emsReturn.setReturnMessage("System Error.");
		}
		emsResult.setReturn(emsReturn);
		return emsResult;
	}
	
	/**
	 * 파일을 base64 변환하여 리턴한다. 
	 * @param filePath
	 * @param fileName
	 * @param ext
	 * @return
	 * @throws Exception
	 */
	public String imageToBase64(String filePath, String fileName, String ext) throws Exception{
	    String base64Img = "";
	    
	    File f = new File(filePath + fileName + "." + ext);
	    if (f.exists() && f.isFile() && f.length() > 0) {
	        byte[] bt = new byte[(int) f.length()];
	        FileInputStream fis = null;
	        try {
	            fis = new FileInputStream(f);
	            fis.read(bt);
	            base64Img = new String(Base64.encodeBase64(bt));
	        } catch (Exception e) {
	            throw e;
	        } finally {
	            try {
	                if (fis != null) {
	                    fis.close();
	                }
	            } catch (IOException e) {
	            } catch (Exception e) {
	            }
	        }
	    }
	    return base64Img;
	}
	

	/**
	 * STOCK 로그 생성
	 * @param rtnJson
	 * @param body
	 * @param paramMap
	 */
	public void createStockLog(JSONObject rtnJson,String body, Map<String, Object> paramMap) {
		
		JSONObject reqJson = null;
		
		IndexApi indexApi = null;
		DocumentApi documentApi = null;
		
		Map<String, Object> tmpMap = null;
		
		String agentCode = null;
		String logId = ""; // ES 로그용 아이디
		String index = ""; // ES 로그용 index		
		String if_status = ""; // 처리상태값
		String if_status_info = ""; // 처리상태설명
		String returnMessage = "";
		reqJson =  new JSONObject().fromObject(body);
		
		if((boolean)rtnJson.get("Success")) {
			if_status = "SUCCESS";
		}else {
			if_status = "ERROR";
		}
		
		returnMessage = "ReqJSON => [ " + reqJson.toString() + " ] ResJSON => [" + rtnJson.toString() + "]";
		
		// 처리 로그 기록 interface history 등록 
		// history 등록 
		tmpMap = new HashMap<String, Object>();
		
		logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
		index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
		
		tmpMap.clear();
		tmpMap.put("ent_code", Util.null2str(paramMap.get("entCode"))); 		 							 			// 도입사코드
		tmpMap.put("customer_code", ""); 		 							 			// 고객코드
		tmpMap.put("solution", "ECM");      		 									// SOLUTION
		tmpMap.put("bound", Util.null2str(paramMap.get("Bound")));		 	 			// 바운드
		tmpMap.put("bound_type", Util.null2str(paramMap.get("BoundType")));	 			// 바운드타입
		tmpMap.put("hbl_no", "");				 							 			// HBL_NO
		tmpMap.put("if_service_code", "API");		 						 			// 프로세스타입 (EDI, API, KTNET-EBILL-ETB 등)
		tmpMap.put("if_doc_code", "STOCK_API");
		tmpMap.put("if_doc_name",  "스탁API");
		tmpMap.put("if_doc_reference_no", "");	 							 			// 문서번호 
		tmpMap.put("if_vender", Util.null2str(paramMap.get("agentCode")));										 			// 중개사업자
		tmpMap.put("if_send_recv_type", "");			 					 			// 송수신타입(S:SEND, R:RECV)
		tmpMap.put("if_send_recv_method", "");		 				 		 			// 송수신방법
		tmpMap.put("if_status", if_status);			 						 			// 처리상태
		tmpMap.put("if_status_info", if_status_info);		 							// 송수신상태_설명
		tmpMap.put("if_sender_id", agentCode);									 		// 송신식별자
		tmpMap.put("if_sender_name", "");		 							 			// 송신자명
		tmpMap.put("if_receiver_id", "");		 							 			// 수신식별자
		tmpMap.put("if_receiver_name", "");	     							 			// 수신자명
		tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));				 			// DATE
		tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));			 			// DATE_TIME
		tmpMap.put("if_remark", returnMessage);			 					 			// 비고( 에러로그 등)
		tmpMap.put("register_user_id", "API");
		tmpMap.put("register_user_name", "API");
		tmpMap.put("register_date_time", System.currentTimeMillis());
		tmpMap.put("es_job_no", logId);
		

		indexApi = new IndexApi(client);
		if (!indexApi.existIndex(index))	indexApi.createIndex(index);
		
		documentApi = new DocumentApi(client);
		documentApi.createDocument(index, logId, tmpMap);
		
	}
	
	/**
	 * ECM 오더 생성 API
	 * @param order
	 * @return InterfaceResult
	 * @throws Exception
	 */
	public InterfaceResultJson registOrderInfo(OrderInterfaceJson order, HttpServletRequest req) throws Exception {
		InterfaceResultJson orderReturn = new InterfaceResultJson();
		InterfaceResultDataJson resultData = new InterfaceResultDataJson();
		try {
//			String requestUrl = (String)req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE); // URL에 따른 분기처리
			String format = "yyyyMMdd_HHmmss.SSSSSSS";
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
			String date = now.format(dtf);
			
			String json = "";
			File schemaLocation = null;
			String errorValidationLine = "";
			String errMsg = "";
			
			String accessKey = (Util.null2str(order.getAccessIdentifier().getAccessKey())).toUpperCase();
			String sender = (Util.null2str(order.getAccessIdentifier().getSenderIdentifier())).toUpperCase();
			String receiver = (Util.null2str(order.getAccessIdentifier().getReceiverIdentifier())).toUpperCase();
			
			String returnCode = "";
			String returnMessage = "";
			String hblNo = "";
			String errorHblNo = "";
			String accessFlag = "N";
			String path = "";
			String apiPath = customProperties.getInterfaceDir() + "api/" + receiver + "/JSON/";
			String infileName = receiver + "_" + sender + "_" + date + UUID.randomUUID().toString().substring(0, 8) + ".txt";
			String bound = "";
			String boundEs = (Util.null2str(order.getAccessIdentifier().getBound())).toUpperCase();
			String boundTypeEs = (Util.null2str(order.getAccessIdentifier().getBoundType())).toUpperCase();
			String logId = ""; // ES 로그용 아이디
			String index = ""; // ES 로그용 index
			String if_status = "RS"; // 처리상태값
			String if_status_info = "Receive Start"; // 처리상태설명
			
			Map<String, Object> tmpMap = null;
			IndexApi indexApi = null;
			DocumentApi documentApi = null;
			
			Map<String, Object> map = new HashMap<String, Object>();
			BufferedWriter bw = null;
			FileOutputStream fos = null;
			OutputStreamWriter osw = null;
			
			path = apiPath + "RECV/" +bound + "/"; // Interface PATH + 업체코드 경로에 저장
			
			
			map.put("ReceiverId", receiver);
			map.put("SenderId", sender);
			map.put("UseYN", "Y");		
			
			// Interface key 조회(ENT_CODE(ReceiverId) ,CUSTOMER_CODE(SenderId) ,ACCESS_KEY ,USE_YN) 	
			Map<String, Object> deliveryInfo = (Map<String, Object>)interfaceMapper.selectInterfaceInfo(map);
			
			if(deliveryInfo != null){
				if(accessKey.equals(Util.null2str(deliveryInfo.get("ACCESS_KEY")))){
					accessFlag = "Y"; // 요청자가 보낸 값과 동일하면 Y
				}else{
					accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
				}
			}else{
				accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
			}
			
			File pathDir = new File(path);
			if (pathDir.exists() == false)	pathDir.mkdirs();
			File file = new File(path + infileName);
			
	 		if(order.getOrderManifest() != null && order.getOrderManifest().size() > 0 && order.getOrderManifest().get(0).getOrderInfo() != null){
	 			hblNo = order.getOrderManifest().get(0).getOrderInfo().getHblNo();
	 			if("AIR".equals(boundEs) || "SEA".equals(boundEs)) {
	 				if("IMP".equals(boundTypeEs) || "EXP".equals(boundTypeEs)) {
	 					if("IMP".equals(boundTypeEs)) {
	 						json = customProperties.getXsdDir() + "impOrder.json";
	 						if("AIR".equals(boundEs)) {
	 							bound = "AI";
	 						}else {
	 							bound = "SI";
	 						}
	 					}else {
	 						json = customProperties.getXsdDir() + "expOrder.json";
	 						if("AIR".equals(boundEs)) {
	 							bound = "AO";
	 						}else {
	 							bound = "SO";
	 						}
	 					}
	 				}else {
	 					accessFlag = "N";
	 				}
	 			}else {
	 				accessFlag = "N";
	 			}
	 		}
        	if("Y".equals(accessFlag)){
        		try{
	        		ObjectMapper objectMapper = new ObjectMapper(); 
	        		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	        		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE); 

	        		errMsg = "";
	        		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909); 
	        		
	        		schemaLocation = new File(json);
					InputStream schemaStream = new FileInputStream(schemaLocation);
	        		
					// 스트림에서 데이터를 읽고 JsonNode에 저장  
					JsonNode jsonNode = objectMapper.valueToTree(order);
					//schemaStream에서 스키마를 가져와 JsonSchema에 저장  
			        com.networknt.schema.JsonSchema schema = factory.getSchema(schemaStream);
			        //유효성 검사 메시지 집합을 생성하고 결과를 저장 
			        Set<ValidationMessage> validationResult  = schema.validate(jsonNode);  
			        
			        if(validationResult.isEmpty()) {
			        	returnMessage = "There is no validation errors";
			        }else {
			        	accessFlag = "V";
			        	int rmidx = 0;
			        	
			        	Pattern p = Pattern.compile("(\\$\\.OrderManifest\\[\\d+\\])");
			        	//오류 메세지
			        	for(ValidationMessage vm : validationResult) {
			        		String errorPath = vm.getPath(); 
			        		Matcher m = p.matcher(errorPath);

			        		if (m.find()) {
			        		    String basePath = m.group(1);

			        		    String jsonPointer = basePath
			        		        .replace("$", "")
			        		        .replace(".", "/")
			        		        .replaceAll("\\[(\\d+)\\]", "/$1");

			        		    JsonNode baseNode = jsonNode.at(jsonPointer);

			        		    errorHblNo = baseNode.path("OrderInfo").path("HblNo").asText();
			        		}
			        		rmidx = vm.getMessage().indexOf(":");
			        		errorValidationLine = vm.getMessage().substring(0, rmidx).replace("$.", "");
			        		returnMessage = vm.getMessage().substring(rmidx+2);
		        		}
		        	}
				} catch(Exception e){
					accessFlag = "E";
					returnMessage = e.toString();
				}
        	}
	        
	        if("Y".equals(accessFlag)){
	        	ObjectMapper mapper = new ObjectMapper();
	        	 mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	        	String samString = mapper.writeValueAsString(order);
	        	path = apiPath + "SUCC/" + bound + "/";
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
	 				String samString = mapper.writeValueAsString(order);
	 	 			path = apiPath + "ERROR/" + bound + "/";
	 	 			pathDir = new File(path);
	 	 			if (pathDir.exists() == false)	pathDir.mkdirs();
	 	 			infileName = "ErrorValidation_" + infileName;
	 	 			fos = new FileOutputStream(path + infileName);
	 	 			osw = new OutputStreamWriter(fos, "UTF-8");
	 	 			bw = new BufferedWriter(osw);
	 	 			bw.write(Util.null2str(samString));
	 	 			bw.close();
	 	 			returnMessage = " 에러가 발생한 부분 => HblNo : "+ errorHblNo +" "+ errorValidationLine + " " + returnMessage;
	 	 		}else if("E".equals(accessFlag)){
	 	 			ObjectMapper mapper = new ObjectMapper();
	 				String samString = mapper.writeValueAsString(order);
	 	 			path = apiPath + "ERROR/" + bound + "/";
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
			tmpMap.put("if_doc_code", "ORDER_API");
			tmpMap.put("if_doc_name", "오더API");
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
			
			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			documentApi = new DocumentApi(client);
			documentApi.createDocument(index, logId, tmpMap);
	        
			resultData.setReturnCode(returnCode);
			resultData.setReturnMessage(returnMessage);
			orderReturn.setReturnData(resultData);
		}catch(Exception e){
			log.error(Util.getPrintStacTraceString(e));
			resultData.setReturnCode("FAIL");
			resultData.setReturnMessage("System Error.");
			orderReturn.setReturnData(resultData);
		}
		return orderReturn;
	}
	
	/**
	 * ECM 송장 호출 API
	 * @param order
	 * @param req
	 * @throws Exception
	 */
	public EcmResult ecmSelectRequestBL(HttpServletRequest req, EcmRequestBLnterface reqBl) throws Exception {
		EcmResult ecmResult = new EcmResult();
		EcmReturn ecmReturn = new EcmReturn();
		
		try {
			String json = customProperties.getXsdDir() + "ecmOrderRequestBL.json";
			File schemaLocation = null;
			String errorValidationLine = "";
			
			String accessKey = (Util.null2str(reqBl.getAccessIdentifier().getAccessKey())).toUpperCase();
			String sender = (Util.null2str(reqBl.getAccessIdentifier().getSenderIdentifier())).toUpperCase();
			String receiver = (Util.null2str(reqBl.getAccessIdentifier().getReceiverIdentifier())).toUpperCase();
			
			String returnMessage = "";
			String hblNo = "";
			String searchYN = "N";
			String accessFlag = "N";
			String logId = ""; // ES 로그용 아이디
			String index = ""; // ES 로그용 index
			String if_status = "RS"; // 처리상태값
			String if_status_info = "Receive Start"; // 처리상태설명
			String jrf_file_name = ""; //jrf파일이름
			
			List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
			Map<String, Object> tmpMap = null;
			Map<String, Object> map = new HashMap<String, Object>();
			IndexApi indexApi = null;
			DocumentApi documentApi = null;
			
			map.put("ReceiverId", receiver);
			map.put("SenderId", sender);
			map.put("UseYN", "Y");		
			
			Map<String, Object> deliveryInfo = (Map<String, Object>)interfaceMapper.selectInterfaceInfo(map);
			
			if(deliveryInfo != null){
				if(accessKey.equals(Util.null2str(deliveryInfo.get("ACCESS_KEY")))){
					accessFlag = "Y"; // 요청자가 보낸 값과 동일하면 Y
				}else{
					accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
				}
			}else{
				accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
			}
			
			if("Y".equals(accessFlag)){
				try{
	        		ObjectMapper objectMapper = new ObjectMapper(); 
	        		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909); 
	        		
	        		schemaLocation = new File(json);
					InputStream schemaStream = new FileInputStream(schemaLocation);
	        		
					// 스트림에서 데이터를 읽고 JsonNode에 저장  
					JsonNode jsonNode = objectMapper.valueToTree(reqBl);
					//schemaStream에서 스키마를 가져와 JsonSchema에 저장  
			        com.networknt.schema.JsonSchema schema = factory.getSchema(schemaStream);
			        //유효성 검사 메시지 집합을 생성하고 결과를 저장 
			        Set<ValidationMessage> validationResult  = schema.validate(jsonNode);  
			        
			        if(validationResult.isEmpty()) {
			        	returnMessage = "";
			        }else {
			        	accessFlag = "V";
			        	int rmidx = 0;
			        	
			        	//오류 메세지
			        	for(ValidationMessage vm : validationResult) {
			        		rmidx = vm.getMessage().indexOf(":");
			        		errorValidationLine = vm.getMessage().substring(0, rmidx).replace("$.", "");
			        		returnMessage = errorValidationLine + " " + vm.getMessage().substring(rmidx+2);
		        		}
		        	}
				} catch(Exception e){
					accessFlag = "E";
					returnMessage = e.toString();
				}
			}
			
			if("Y".equals(accessFlag)){
				OrderInvoice orderInvoice = reqBl.getOrderInvoice();
				List<Map<String, Object>> orderInvoiceList =new ArrayList<Map<String, Object>>();
				Map<String, Object> orderInvoiceMap = null;
				List<Map<String, Object>> dataList = null;
				Map<String, Object> paramMap = null;
				map.put("ordId", orderInvoice.getOrdId());
				map.put("hblNo", orderInvoice.getHblNo());
				int poNoCnt = interfaceMapper.selectPoNoCnt(map);
				tempList = interfaceMapper.selectEcmOrderRequestBl(map);
				
				// poNo 데이터가 존재하지 않을 때 처리
				if(poNoCnt < 1) {
					searchYN = "N";
					returnMessage = "Order Id data does not exist. (Order Id: " + Util.null2str(map.get("ordId")) + ")";
				}else if(poNoCnt > 1) {
					searchYN = "N";
					returnMessage = "No matching invoice file";
				}else {
						searchYN = "Y";
						map.put("odJobNo", Util.null2str(tempList.get(0).get("odJobNo")));
						if(Util.null2int(tempList.get(0).get("MULTI_CNT")) > 0) {
							if("S".equals(Util.null2str(tempList.get(0).get("MULTI_DATA")))) {
								map.put("multiYn", "Y");
							}else if("N".equals(Util.null2str(tempList.get(0).get("MULTI_DATA")))) {
								searchYN = "N";
								returnMessage = "Multi data does not exist. (Delivery No: " + Util.null2str(map.get("hblNo")) + ")";
								ecmReturn.setReturnCode("FAIL");
								ecmReturn.setReturnMessage(returnMessage);
								ecmResult.setReturn(ecmReturn);
								return ecmResult;
							}
							dataList = interfaceMapper.selectEcmOrderMultiBl(map);
						}else {
							if("N".equals(Util.null2str(tempList.get(0).get("MULTI_DATA")))) {
								searchYN = "N";
								returnMessage = "Data does not exist. (Delivery No: " + Util.null2str(map.get("hblNo")) + ")";
								ecmReturn.setReturnCode("FAIL");
								ecmReturn.setReturnMessage(returnMessage);
								ecmResult.setReturn(ecmReturn);
								return ecmResult;
							}else {
								if("KOREX".equals(tempList.get(0).get("deliveryCode"))) {
									jrf_file_name = "ECM-KOREX-003.jrf";
								}else {
									jrf_file_name = "ECM-EMS-002.jrf";
								}
							}
							hblNo = Util.null2str(tempList.get(0).get("deliveryNo"));
						}
						
						if(dataList != null && dataList.size() > 0) {
							for(int i = 0; i < dataList.size(); i ++) {
								paramMap = dataList.get(i);
								String deliveryCode = Util.null2str(paramMap.get("deliveryCode"));
								if(deliveryCode.contains("KOREX")) {
									paramMap.put("jrfFileName", "ECM-KOREX-003_MULTY.jrf");
								}else if(deliveryCode.contains("EMS")) {
									paramMap.put("jrfFileName", "ECM-EMS-002_MULTI.jrf");
								}
								//송장 출력물 생성(멀티)
								orderInvoiceMap = new HashMap<String, Object>();
								orderInvoiceMap.put(Util.null2str(paramMap.get("deliveryNo")), savePrintInfo(paramMap));
								orderInvoiceList.add(orderInvoiceMap);
							}
						}else {
							//송장 출력물 생성(오더)
							orderInvoiceMap = new HashMap<String, Object>();
							tempList.get(0).put("jrfFileName", jrf_file_name);
							orderInvoiceMap.put(hblNo, savePrintInfo(tempList.get(0)));
							orderInvoiceList.add(orderInvoiceMap);
						}
						orderInvoice.setInvoiceList(orderInvoiceList);
						ecmResult.setOrderInvoice(orderInvoice);
						
				}
				if ("Y".equals(searchYN)) {
					ecmReturn.setReturnCode("SUCCESS");
					ecmReturn.setReturnMessage("");
				} else {
					ecmReturn.setReturnCode("FAIL");
					ecmReturn.setReturnMessage(returnMessage);
				} 
	 		} else if ("N".equals(accessFlag)) {
	 			ecmReturn.setReturnCode("FAIL");
				ecmReturn.setReturnMessage("Wrong AccessKey");
			} else if ("X".equals(accessFlag)) {
				ecmReturn.setReturnCode("FAIL");
				ecmReturn.setReturnMessage("Not a service provider");
			} else {
				ecmReturn.setReturnCode("FAIL");
				ecmReturn.setReturnMessage(returnMessage);
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
			tmpMap.put("bound", "");		 	 						// 바운드 (AIR)
			tmpMap.put("bound_type", "");	 						// 바운드타입
			tmpMap.put("hbl_no", Util.null2str(hblNo));					// HBL_NO
			tmpMap.put("if_service_code", "REQUESTBL");
			tmpMap.put("if_doc_code", "REQUESTBL");
			tmpMap.put("if_doc_name", "송장호출API");
			tmpMap.put("if_doc_reference_no", "");						// 문서번호 
			tmpMap.put("if_vender", sender);							// 중개사업자
			tmpMap.put("if_send_recv_type", "");						// 송수신타입(S:SEND, R:RECV)
			tmpMap.put("if_send_recv_method", "");						// 송수신방법
			tmpMap.put("if_file_path", "");			 					// 파일경로
			tmpMap.put("if_file_name", "");			 					// 파일명
			if("FAIL".equals(ecmReturn.getReturnCode())){				// 처리상태
				tmpMap.put("if_status", "ERROR");
			}else{
				tmpMap.put("if_status", ecmReturn.getReturnCode());
			}
			tmpMap.put("if_status_info", ecmReturn.getReturnMessage());	// 송수신상태_설명
			tmpMap.put("if_sender_id", sender);							// 송신식별자
			tmpMap.put("if_sender_name", "");		 					// 송신자명
			tmpMap.put("if_receiver_id", receiver);		 				// 수신식별자
			tmpMap.put("if_receiver_name", "");	     					// 수신자명
			tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));		// DATE
			tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));	// DATE_TIME
			tmpMap.put("if_remark", returnMessage);			 			// 비고( 에러로그 등)
			tmpMap.put("register_user_id", "API");
			tmpMap.put("register_user_name", "API");
			tmpMap.put("register_date_time", System.currentTimeMillis());
			tmpMap.put("es_job_no", logId);
			
			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			documentApi = new DocumentApi(client);
			documentApi.createDocument(index, logId, tmpMap);
			
		}catch(Exception e){
			log.error(Util.getPrintStacTraceString(e));
			ecmReturn.setReturnCode("FAIL");
			ecmReturn.setReturnMessage("System Error.");
		}
		ecmResult.setReturn(ecmReturn);
		return ecmResult;
	}
	
	/**
	 * 출력물 저장 
	 * @param paramMap
	 */
	public String savePrintInfo(Map<String, Object> paramMap) {
		
			//LocalDate today = LocalDate.now();
	        //String formattedDate = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		
			// 송장 파일 생성 (UBI -> PDF)
			Map<String, Object> ubiParams = new HashMap<String, Object>();
			ubiParams.put("ubi_arg", "odJobNo#"+Util.null2str(paramMap.get("odJobNo"))+"#srchDeliveryCode#"+Util.null2str(paramMap.get("deliveryCode"))+"#srchEntBusinessCode#"+Util.null2str(paramMap.get("entBusinessCode"))+"#srchEntCode#"+Util.null2str(paramMap.get("entCode"))
			+"#srchBlNo#"+Util.null2str(paramMap.get("deliveryNo")) + "#userId#" + Util.null2str(paramMap.get("userId")));
			ubiParams.put("report_FileDir", "ecm");
			ubiParams.put("report_FileName", Util.null2str(paramMap.get("jrfFileName")));
			ubiParams.put("odJobNo", Util.null2str(paramMap.get("odJobNo")));
			ubiParams.put("entCode", Util.null2str(paramMap.get("entCode")));
			ubiParams.put("deliveryCode", Util.null2str(paramMap.get("deliveryCode")));
			ubiParams.put("poDate", Util.null2str(paramMap.get("poDate")));
			
			String base64Pdf = getReportResultFile(ubiParams);

		    return base64Pdf;
	}
	
	/**
	 * UBI 출력물 생성
	 * @param params
	 * @return
	 */
	public String getReportResultFile(Map<String,Object> params){
		Map<String,Object> result = new HashMap<String,Object>();
		String jrf_dir = customProperties.getReportJrfTopLoc(); 			// 리포트 파일 위치.
		String rtnData = null;
		
		log.info("reportFileDir========================"+Util.null2str(params.get("report_FileDir")));
		
		String entCode = Util.null2str(params.get("entCode"));
		String jrfFileName = Util.null2str(params.get("report_FileName"));												// 리포트 파일명.
		//String ds	= "dataSource_yhtxx"; // 테스트 
		//String ds = "jdbc/exps_yhtxx"; //  운영
		String ds = customProperties.getReportDs() + entCode.toLowerCase();														// WAS에 설정된 DataSource 명. ubl/UbiService/ ubiservice.xml
		String file_url = customProperties.getReportUrl();																		// 리포트에서 사용되는 이미지 또는 공통 아이템 정보를 가져오기위한 정보.
		String ubi_serverUrl = customProperties.getReportDomain() 	+ "/UbiServer";												// 리포트에서 사용되는 이미지 또는 공통 아이템 정보를 가져오기위한 정보.
		String servlet_url1 = customProperties.getReportDomain() 	+ "/UbiForm";												//"/UbiForm";							// Form 서블릿 URL.
		String servlet_url2 = customProperties.getReportDomain() 	+ "/UbiData";												//"/UbiData";							// Data 서블릿 URL.
		String args = Util.null2str(params.get("ubi_arg"));					    												// 아규먼트 정보.
		String odJobNo = Util.null2str(params.get("odJobNo"));
		String poDate = Util.null2str(params.get("poDate"));
		String datePath = "/"+poDate.substring(0,4)+"/"+poDate.substring(5,7)+"/"+poDate.substring(8,10)+"/";
		boolean isSuccess = false;
		
//		
		try {
			UbiViewer ubiviewer = new UbiViewer(false, false);	// 객체 생성
			
			log.info("file_url========================"+file_url);
			log.info("ubiServerURL===================="+ubi_serverUrl);
			log.info("jrf_dir========================="+jrf_dir);
			log.info("jrfFileName====================="+jrfFileName);
			log.info("args============================"+args);
			log.info("ds=============================="+ds);
			
			ubiviewer.fileURL 		= file_url;
			ubiviewer.ubiServerURL 	= ubi_serverUrl;
			ubiviewer.isLocalFile	= true;
			ubiviewer.jrfFileDir 	= jrf_dir;
			ubiviewer.jrfFileName 	= jrfFileName;
			ubiviewer.arg 			= args;
			ubiviewer.dataSource 	= ds;
//			ubiviewer.setExportParams("PDF", export_dir + export_file);
			ubiviewer.exectype 		= "TYPE6";
			ubiviewer.fontRevision 	= true;
			ubiviewer.setFontPath(customProperties.getReportFontLoc());		//WAS가 설치된 서버의 OS가 Non-Windows인 경우 폰트 정보를 가져오는 부분을 설정.
			ubiviewer.isDebug 		= true;
			
			Path tempFile = Files.createTempFile("report_" + odJobNo + "_label", ".pdf");
	        
			try {
	            ubiviewer.setExportParams("PDF", tempFile.toString());

	            log.info("[" + odJobNo + "] loadReport Start");
	            isSuccess = ubiviewer.loadReport();
	            log.info("[" + odJobNo + "] loadReport End / isSuccess: " + isSuccess);

	            if(isSuccess) {
	            	byte[] pdfBytes = Files.readAllBytes(tempFile);
	                rtnData= new String(Base64.encodeBase64(pdfBytes));
	            }
	        } finally {
	            Files.deleteIfExists(tempFile); 
	        }
		}catch(Exception e) {
			e.printStackTrace();
			isSuccess = false;
			return rtnData;
		}
		return rtnData;
		
	 }
	
	/**
	 * X-RAY 분배
	 * @param xray
	 * @param req
	 * @return
	 */
	public InterfaceResult imageDisplayInfo(LxpXrayIntf xray, HttpServletRequest req) throws Exception{
		
		InterfaceResult result = new InterfaceResult();
		InterfaceReturnData returnData = new InterfaceReturnData();
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		Map<String, Object> tmpMap = null;
		String hisLogId = ""; 			// ES 로그용 아이디
		String hisIndex = ""; 			// ES 로그용 index
		IndexApi indexApi = null;
		DocumentApi documentApi = null;
		String fileName = "";
		String filePath = "";
		String content = "";
		String msg = "";
		boolean errFlag = false;;
		
		try {
		
		//ES로그 추가
		LocalDate today = LocalDate.now();
		tmpMap = new HashMap<String, Object>();		
		
		hisLogId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
		hisIndex = "bms-interface-logs-" + DateTime.getDate("yyyyMMdd");		
		fileName = hisLogId+".txt";
		filePath = fileBackupDir_ + "api/xrayIntf/"+ Util.null2str(xray.getEntCode()) + "/" + today + "/";
		
		
		if("".equals(Util.null2str(xray.getBagId()))) {
			msg += "The bagId value does not exist\n";
			returnData.setReturnCode("FAIL");
		}else if("".equals(Util.null2str(xray.getBagAlias()))) {
			msg += "The BagAlias value does not exist\n";
			returnData.setReturnCode("FAIL");
		}else if("".equals(Util.null2str(xray.getWorkStationName()))) {
			msg += "The WorkStationName value does not exist\n";
			returnData.setReturnCode("FAIL");
		}else if("".equals(Util.null2str(xray.getMachineName()))) {
			msg += "The MachineName value does not exist\n";
			returnData.setReturnCode("FAIL");
		}else if("".equals(Util.null2str(xray.getTimeStamp()))) {
			msg += "The TimeStamp value does not exist\n";
			returnData.setReturnCode("FAIL");
		}else {
			errFlag = true;
			returnData.setReturnCode("SUCCESS");
		}
		
        
        // 원하는 포맷으로 변환
        String formatted = "";
        
		try {
			// 문자열을 LocalDateTime으로 파싱
			LocalDateTime dateTime = LocalDateTime.parse(xray.getTimeStamp());
			formatted = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
        } catch (DateTimeParseException e) {
        	returnData.setReturnCode("FAIL");
        	msg += "TimeStamp value is not in the correct format\n";
        	errFlag = false;
        }
		
		paramMap.put("bagId", Util.null2str(xray.getBagId()));
		paramMap.put("bagAlias", Util.null2str(xray.getBagAlias()));
		paramMap.put("workStationName", Util.null2str(xray.getWorkStationName()));
		paramMap.put("machineName", Util.null2str(xray.getMachineName()));
		paramMap.put("event", "showing");
		paramMap.put("timeStamp", Util.null2str(xray.getTimeStamp()));
		
		content += paramMap;

		if(errFlag) {
			paramMap.put("timeStamp", formatted);
			interfaceMapper.insertImageDisplayInfo(paramMap);	
		}
		
		}catch(Exception e) {
			log.error("imageDisplayInfo error : " +  e.getMessage());
			errFlag = false;
			returnData.setReturnCode("FAIL");
			msg += "imageDisplayInfo interface error!!\n";
			content += e.getMessage();
		}
		
		tmpMap.put("ent_code", xray.getEntCode()); 		 	 // 도입사코드
		tmpMap.put("customer_code", ""); 		 						 // 고객코드
		tmpMap.put("solution", "BMS");      		 						 // SOLUTION
		tmpMap.put("bound", "");		 	 							 	 // 바운드
		tmpMap.put("bound_type", "IMP");	 							 		 // 바운드타입
		tmpMap.put("hbl_no", "");				 							 // HBL_NO
		tmpMap.put("if_service_code", "XRAY_INTF");
		tmpMap.put("if_doc_code", "XRAY_INTF");
		tmpMap.put("if_doc_name", "X-RAY 분배"); 			 			 		 // 문서분류명 (AIR ORDER, SEA ORDER)
		tmpMap.put("if_doc_reference_no", "");	 							 // 문서번호 
		tmpMap.put("if_vender", "");									 	// 중개사업자
		tmpMap.put("if_send_recv_type", "R");			 					 // 송수신타입(S:SEND, R:RECV)
		tmpMap.put("if_send_recv_method", "API");		 				 		 // 송수신방법
		if("FAIL".equals(returnData.getReturnCode())){
			tmpMap.put("if_status", "ERROR");
		}else{
			tmpMap.put("if_status", "SUCCESS");
		}
		tmpMap.put("if_file_path", filePath);			 						 	 // 파일경로
		tmpMap.put("if_file_name", fileName);			 				 	 		 // 파일명
		tmpMap.put("if_status_info", "");								 	// 송수신상태_설명
		tmpMap.put("if_sender_id", "");									 	// 송신식별자
		tmpMap.put("if_sender_name", "");		 							 // 송신자명
		tmpMap.put("if_receiver_id", "");		 						 // 수신식별자
		tmpMap.put("if_receiver_name", "");	     							 // 수신자명
		tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));				 // DATE
		tmpMap.put("if_date_time", DateTime.getDate("HH:mm:ss"));	
		tmpMap.put("if_remark", msg);			 					 		// 비고( 에러로그 등)
		tmpMap.put("register_user_id", "API");
		tmpMap.put("register_date_time", System.currentTimeMillis());
		tmpMap.put("es_job_no", hisLogId);
		
		indexApi = new IndexApi(client);
		if (!indexApi.existIndex(hisIndex))	indexApi.createIndex(hisIndex);
		
		if(errFlag) {
			msg = "SUCCESS";
		}

		content += "\n" + msg;
		
		try {
			FileUtil.fileWrite(content, filePath, fileName);
			documentApi = new DocumentApi(client);
			documentApi.createDocument(hisIndex, hisLogId, tmpMap);
		}catch(Exception e) {
			log.error("imageDisplayInfo error : "+e.getMessage());
		}
		
		returnData.setReturnMessage(msg);
		result.setReturnData(returnData);
		return result;
	}

	public List<Map<String, Object>> selectOrderTrackingJsonList(HttpServletRequest req, TrackingInterfaceJson track) throws Exception{
		Map<String, Object> tmpMap = null;
		String logId = ""; 			// ES 로그용 아이디
		String index = ""; 			// ES 로그용 index
		IndexApi indexApi = null;
		DocumentApi documentApi = null;
		
		int odCnt = track.getOrderTracking().size();
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String errHblNos = "";
		String errNos = "";
		int errCnt = 0;
		
 		File schemaLocation = null;
 		String errorValidationLine = "";

 		String json = customProperties.getXsdDir() + "orderTracking.json";
 		String returnCode = "";
 		String returnMessage = "";
 		String receiver = track.getAccessIdentifier().getReceiverIdentifier();
 		String searchYN = "Y";
 		String accessKey = track.getAccessIdentifier().getAccessKey();
 		String sender = (Util.null2str(track.getAccessIdentifier().getSenderIdentifier())).toUpperCase();
 		String accessFlag = "N";
     	
 		Util.setDB(req, receiver);
 		
 		paramMap.put("ReceiverId", receiver);
 		paramMap.put("SenderId", sender);
 		paramMap.put("UseYN", "Y");
 		
		Map<String, Object> interfaceMap = (Map<String, Object>)interfaceMapper.selectInterfaceInfo(paramMap);
		if (interfaceMap != null) {
			if(accessKey.equals(Util.null2str(interfaceMap.get("ACCESS_KEY")))) {
				accessFlag = "Y";
			}
		} else {
			accessFlag = "X";
		}
		
		if ("Y".equals(accessFlag)) {
			try{
				ObjectMapper objectMapper = new ObjectMapper(); 
				objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE); 

				JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909); 
				
				schemaLocation = new File(json);
				InputStream schemaStream = new FileInputStream(schemaLocation);
				
				// 스트림에서 데이터를 읽고 JsonNode에 저장  
				JsonNode jsonNode = objectMapper.valueToTree(track);
				//schemaStream에서 스키마를 가져와 JsonSchema에 저장  
				com.networknt.schema.JsonSchema schema = factory.getSchema(schemaStream);
				//유효성 검사 메시지 집합을 생성하고 결과를 저장 
				Set<ValidationMessage> validationResult  = schema.validate(jsonNode);  
				
				if(validationResult.isEmpty()) {
					returnMessage = "";
				}else {
					accessFlag = "V";
					int rmidx = 0;
					
					//오류 메세지
					for(ValidationMessage vm : validationResult) {
						rmidx = vm.getMessage().indexOf(":");
						errorValidationLine = vm.getMessage().substring(0, rmidx).replace("$.", "");
						returnMessage = errorValidationLine + " " + vm.getMessage().substring(rmidx+2);
					}
				}
			} catch(Exception e){
				accessFlag = "E";
				returnMessage = e.toString();
			}
		}
		
		if("Y".equals(accessFlag)){
			for (int i = 0; i < track.getOrderTracking().size(); i++) {
				String language = "ENG"; 
				if(!"".equals(Util.null2str(track.getOrderTracking().get(i).getLanguage()))) {
					language = Util.null2str(track.getOrderTracking().get(i).getLanguage());
				}
				String ordId = Util.null2str(track.getOrderTracking().get(i).getOrdId());
				String hblNo = Util.null2str(track.getOrderTracking().get(i).getHblNo());
				if(!"".equals(ordId) || !"".equals(hblNo)) {
					paramMap.put("ordId", ordId);
					paramMap.put("hblNo", hblNo);
					paramMap.put("language", language);
					tempList = interfaceMapper.selectOrderTrackingVE(paramMap);
					if (tempList.size() < 1) {
						searchYN = "N";
						if(!Util.null2str(errHblNos).equals("")) {
							errHblNos += ",";
						}
						errHblNos += Util.null2str(hblNo, ordId);	//전달받은 내용중 잘못된 hblNo(없으면 ordId)는 모아서 returnMessage에 표시해줌.
						errCnt++;
					}else {
						resultList.addAll(tempList);
					}
					
					String bound = "";
					String boundType = "";
					if(tempList != null && tempList.size() > 0) {
						bound = Util.null2str(tempList.get(0).get("BOUND"));
						boundType = Util.null2str(tempList.get(0).get("BOUND_TYPE"));
					}
					
					if(boundType.equals("IMP")) {
						paramMap.put("bound", bound);
						paramMap.put("boundType", boundType);
						tempList = interfaceMapper.selectOrderTrackingCustomsInfo(paramMap); 
						if (tempList.size() == 1) {
							resultList.addAll(tempList);
							tempList = interfaceMapper.selectOrderTrackingMultiInfo(paramMap);
							if(tempList.size() > 0) {
								resultList.addAll(tempList);
							}
						} else if (tempList.size() > 1) {
							searchYN = "D";
							resultList.clear();
						}
					}
				}else {
					resultList = new ArrayList<Map<String, Object>>();
					searchYN = "N";
					errNos += "data does not exist";
					break;
				}
			}
			if ("Y".equals(searchYN)) {
				resultMap.put("ReturnCode", "SUCCESS");
				resultMap.put("ReturnMessage", "");
			} else if ("D".equals(searchYN)) {
				resultMap.put("ReturnCode", "FAIL");
				resultMap.put("ReturnMessage", "H.B/L No duplication.Please contact us " + receiver + ".");
			} else {
				if(!"".equals(errNos)) {
					resultMap.put("ReturnCode", "FAIL");
					resultMap.put("ReturnMessage", "data does not exist");
				}else {
					if(odCnt == errCnt) {
						resultMap.put("ReturnCode", "FAIL");
					}else {
						resultMap.put("ReturnCode", "PARTIAL SUCCESS");	//부분성공
					}
					resultMap.put("ReturnMessage", "data does not exist : " + errHblNos);
				}
			}
		}else if ("N".equals(accessFlag)) {
			resultMap.put("ReturnCode", "FAIL");
			resultMap.put("ReturnMessage", "Wrong AccessKey");
		} else if ("X".equals(accessFlag)) {
			resultMap.put("ReturnCode", "FAIL");
			resultMap.put("ReturnMessage", "Not a service provider");
		} else {
			resultMap.put("ReturnCode", returnCode);
			resultMap.put("ReturnMessage", returnMessage);
		}
		resultList.add(resultMap);
		
		//ES로그 추가 
		tmpMap = new HashMap<String, Object>();
		logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
		index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
		
		// 처리결과에 따른 로그 기록 history
		tmpMap.clear();
		tmpMap.put("ent_code", receiver); 		 							 // 도입사코드
		tmpMap.put("customer_code", sender); 		 						 // 고객코드
		tmpMap.put("solution", "ECM");      		 						 // SOLUTION
		tmpMap.put("bound", "");		 	 							 	 // 바운드
		tmpMap.put("bound_type", "");	 							 		 // 바운드타입
		tmpMap.put("hbl_no", "");				 							 // HBL_NO
		tmpMap.put("if_service_code", "TRACKING");
		tmpMap.put("if_doc_code", "TRACKING_API");
		tmpMap.put("if_doc_name", "트래킹API"); 			 			 		 // 문서분류명 (AIR ORDER, SEA ORDER)
		tmpMap.put("if_doc_reference_no", "");	 							 // 문서번호 
		tmpMap.put("if_vender", sender);									 // 중개사업자
		tmpMap.put("if_send_recv_type", "");			 					 // 송수신타입(S:SEND, R:RECV)
		tmpMap.put("if_send_recv_method", "");		 				 		 // 송수신방법
		tmpMap.put("if_file_path", "");			 						 	 // 파일경로
		tmpMap.put("if_file_name", "");			 				 	 		 // 파일명
		if("FAIL".equals(resultMap.get("ReturnCode"))){
			tmpMap.put("if_status", "ERROR");
		}else if("PARTIAL SUCCESS".equals(resultMap.get("ReturnCode"))){
			tmpMap.put("if_status", "SUCCESS");
		}else{
			tmpMap.put("if_status", resultMap.get("ReturnCode"));			 	 // 처리상태
		}
		tmpMap.put("if_status_info", resultMap.get("ReturnMessage"));		 // 송수신상태_설명
		tmpMap.put("if_sender_id", sender);									 // 송신식별자
		tmpMap.put("if_sender_name", "");		 							 // 송신자명
		tmpMap.put("if_receiver_id", receiver);		 						 // 수신식별자
		tmpMap.put("if_receiver_name", "");	     							 // 수신자명
		tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));				 // DATE
		tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));			 // DATE_TIME
		tmpMap.put("if_remark", returnMessage);			 					 // 비고( 에러로그 등)
		tmpMap.put("order_cnt", odCnt);										 // 트래킹API 건수 -> 처리결과(성공/실패/부분성공)에 상관없이 api로 들어온 hbl건수를 모두 더함.
		tmpMap.put("register_user_id", "API");
		tmpMap.put("register_user_name", "API");
		tmpMap.put("register_date_time", System.currentTimeMillis());
		tmpMap.put("es_job_no", logId);
		
		indexApi = new IndexApi(client);
		if (!indexApi.existIndex(index))	indexApi.createIndex(index);
		
		documentApi = new DocumentApi(client);
		documentApi.createDocument(index, logId, tmpMap);
					
		return resultList;
	}
	
	public TrackingResultJson convertPodReturnDataJson (List<Map<String, Object>> list) throws JAXBException {
		List<ReturnTrackingDataJson> trackingDataList = new ArrayList<ReturnTrackingDataJson>();
		List<CustomsInfoJsonVE> cusList = new ArrayList<CustomsInfoJsonVE>();
		List<MultiInfoJson> mulList = new ArrayList<MultiInfoJson>();
		List<InventoryInfoJson> invList = new ArrayList<InventoryInfoJson>();
		
		TrackingResultJson trackingResult = new TrackingResultJson();
		InterfaceReturnDataJson returnData = new InterfaceReturnDataJson();
		ReturnTrackingDataJson trackingData = null;
		CustomsInfoJsonVE cus = null;
		MultiInfoJson mul = null;
		InventoryInfoJson inv = null;
		
		int index = 0;
		
		for(Map<String, Object> dataMap : list) {
			if(index == (list.size() - 1)){
				returnData.setReturnCode(dataMap.get("ReturnCode").toString());
				returnData.setReturnMessage(dataMap.get("ReturnMessage").toString());
				trackingResult.setReturnData(returnData);
			}
			
			if(dataMap != null){
				if(dataMap.get("POD_STATUS") != null){
					trackingData = new ReturnTrackingDataJson();
					trackingData.setOrdId(dataMap.get("PO_NO").toString());
					trackingData.setHblNo(dataMap.get("HBL_NO").toString());
					trackingData.setTrackingCode(Util.null2str(dataMap.get("POD_STATUS")));
					trackingData.setTrackingDateTime(Util.null2str(dataMap.get("TIME")));
					trackingData.setTrackingCodeName(Util.null2str(dataMap.get("TRACKING_CODE_NAME")));
					trackingData.setTrackingRemark(Util.null2str(dataMap.get("REMARKS")));
					trackingDataList.add(trackingData);
				}
				
				if(dataMap.get("CUSTOMS_CLEARANCE") != null){
					cus = new CustomsInfoJsonVE();
					cus.setOrdId(dataMap.get("PO_NO").toString());
					cus.setHBLNo(dataMap.get("HBL_NO").toString());
					cus.setCustomsClearance(Util.null2str(dataMap.get("CUSTOMS_CLEARANCE")));
					cus.setInspection(Util.null2str(dataMap.get("INSPECTION")));
					cus.setWithdrawalCode(Util.null2str(dataMap.get("DROP_CODE")));
					cus.setWithdrawalReason(Util.null2str(dataMap.get("DROP_REASON")));
					cus.setDeliveryCode(Util.null2str(dataMap.get("DELIVERY_CODE")));
					cusList.add(cus);
					
					inv = new InventoryInfoJson();
					if(Util.null2str(dataMap.get("HOLD_CODE")).equals("")) {
						inv.setHoldYN(Util.null2str(dataMap.get("HOLD_YN")));
					}else {
						inv.setHoldYN(dataMap.get("HOLD_YN") + "|" + Util.null2str(dataMap.get("HOLD_CODE")) + "|" + Util.null2str(dataMap.get("HOLD_REASON")));
					}
					if(Util.null2str(dataMap.get("WH_LOCATION")).equals("")) {
						inv.setLocation(Util.null2str(dataMap.get("WH_LOCATION")));
					}else {
						inv.setLocation(Util.null2str(dataMap.get("WH_LOCATION")) + "|" + Util.null2str(dataMap.get("WH_LOCATION_NAME")));
					}
					inv.setStockDateTime(Util.null2str(dataMap.get("STOCK_DATE_TIME")));
					inv.setStockUserName(Util.null2str(dataMap.get("STOCK_USER_NAME")));
					inv.setCarryoutDate(Util.null2str(dataMap.get("CARRYOUT_DATE")));
					inv.setCarryoutUser(Util.null2str(dataMap.get("CARRYOUT_USER")));
					invList.add(inv);
				}
				
				if(dataMap.get("SEQ_NO_ID") != null) {
					mul = new MultiInfoJson();
					mul.setDeliveryNo(Util.null2str(dataMap.get("DELIVERY_NO")));
					mul.setChangeDeliveryCode(Util.null2str(dataMap.get("DELIVERY_CODE")));
					mul.setChangeDeliveryNo(Util.null2str(dataMap.get("DELIVERY_NO_CHANGED")));
					mulList.add(mul);
				}
			}
			index++;
		}
		
		trackingResult.setReturnTrackingData(trackingDataList);
		trackingResult.setCustomsInfo(cusList);
		trackingResult.setMultiInfo(mulList);
		trackingResult.setInventoryInfo(invList);
		
		return trackingResult;
	}
	
	/**
	 * ECM 오더(DKX) 생성 API
	 * @param order
	 * @return InterfaceResult
	 * @throws Exception
	 */
	public InterfaceResultJson registOrderInfoDKX(OrderInterfaceJsonDKX order, HttpServletRequest req) throws Exception {
		InterfaceResultJson orderReturn = new InterfaceResultJson();
		InterfaceResultDataJson resultData = new InterfaceResultDataJson();
		try {
//			String requestUrl = (String)req.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE); // URL에 따른 분기처리
			String format = "yyyyMMdd_HHmmss.SSSSSSS";
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
			String date = now.format(dtf);
			
			String json = "";
			File schemaLocation = null;
			String errorValidationLine = "";
			String errMsg = "";
			
			String accessKey = (Util.null2str(order.getAccessIdentifier().getAccessKey())).toUpperCase();
			String sender = (Util.null2str(order.getAccessIdentifier().getSenderIdentifier())).toUpperCase();
			String receiver = (Util.null2str(order.getAccessIdentifier().getReceiverIdentifier())).toUpperCase();
			
			String returnCode = "";
			String returnMessage = "";
			String hblNo = "";
			String errorHblNo = "";
			String accessFlag = "N";
			String path = "";
			String apiPath = customProperties.getInterfaceDir() + "api/" + receiver + "/JSON/";
			String infileName = receiver + "_" + sender + "_" + date + UUID.randomUUID().toString().substring(0, 8) + ".txt";
			String bound = "";
			String boundEs = (Util.null2str(order.getAccessIdentifier().getBound())).toUpperCase();
			String boundTypeEs = (Util.null2str(order.getAccessIdentifier().getBoundType())).toUpperCase();
			String logId = ""; // ES 로그용 아이디
			String index = ""; // ES 로그용 index
			String if_status = "RS"; // 처리상태값
			String if_status_info = "Receive Start"; // 처리상태설명
			
			Map<String, Object> tmpMap = null;
			IndexApi indexApi = null;
			DocumentApi documentApi = null;
			
			Map<String, Object> map = new HashMap<String, Object>();
			BufferedWriter bw = null;
			FileOutputStream fos = null;
			OutputStreamWriter osw = null;
			
			path = apiPath + "RECV/" +bound + "/"; // Interface PATH + 업체코드 경로에 저장
			
			
			map.put("ReceiverId", receiver);
			map.put("SenderId", sender);
			map.put("UseYN", "Y");		
			
			// Interface key 조회(ENT_CODE(ReceiverId) ,CUSTOMER_CODE(SenderId) ,ACCESS_KEY ,USE_YN) 	
			Map<String, Object> deliveryInfo = (Map<String, Object>)interfaceMapper.selectInterfaceInfo(map);
			
			if(deliveryInfo != null){
				if(accessKey.equals(Util.null2str(deliveryInfo.get("ACCESS_KEY")))){
					accessFlag = "Y"; // 요청자가 보낸 값과 동일하면 Y
				}else{
					accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
				}
			}else{
				accessFlag = "X"; // 요청자가 보낸 값과 다르면 X
			}
			
			File pathDir = new File(path);
			if (pathDir.exists() == false)	pathDir.mkdirs();
			File file = new File(path + infileName);
			
	 		if(order.getOrderManifest() != null && order.getOrderManifest().size() > 0 && order.getOrderManifest().get(0).getOrderInfo() != null){
	 			hblNo = order.getOrderManifest().get(0).getOrderInfo().getHblNo();
	 			if("AIR".equals(boundEs) || "SEA".equals(boundEs)) {
	 				if("IMP".equals(boundTypeEs) || "EXP".equals(boundTypeEs)) {
	 					if("IMP".equals(boundTypeEs)) {
	 						json = customProperties.getXsdDir() + "impOrderDKX.json";
	 						if("AIR".equals(boundEs)) {
	 							bound = "AI";
	 						}else {
	 							bound = "SI";
	 						}
	 					}else {
	 						//DKX 수출 추후 수정예정 26.07.02
	 						accessFlag = "N";
//	 						json = customProperties.getXsdDir() + "expOrder.json";
//	 						if("AIR".equals(boundEs)) {
//	 							bound = "AO";
//	 						}else {
//	 							bound = "SO";
//	 						}
	 					}
	 				}else {
	 					accessFlag = "N";
	 				}
	 			}else {
	 				accessFlag = "N";
	 			}
	 		}
        	if("Y".equals(accessFlag)){
        		try{
	        		ObjectMapper objectMapper = new ObjectMapper(); 
	        		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	        		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE); 

	        		errMsg = "";
	        		JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909); 
	        		
	        		schemaLocation = new File(json);
					InputStream schemaStream = new FileInputStream(schemaLocation);
	        		
					// 스트림에서 데이터를 읽고 JsonNode에 저장  
					JsonNode jsonNode = objectMapper.valueToTree(order);
					//schemaStream에서 스키마를 가져와 JsonSchema에 저장  
			        com.networknt.schema.JsonSchema schema = factory.getSchema(schemaStream);
			        //유효성 검사 메시지 집합을 생성하고 결과를 저장 
			        Set<ValidationMessage> validationResult  = schema.validate(jsonNode);  
			        
			        if(validationResult.isEmpty()) {
			        	returnMessage = "There is no validation errors";
			        }else {
			        	accessFlag = "V";
			        	int rmidx = 0;
			        	
			        	Pattern p = Pattern.compile("(\\$\\.OrderManifest\\[\\d+\\])");
			        	//오류 메세지
			        	for(ValidationMessage vm : validationResult) {
			        		String errorPath = vm.getPath(); 
			        		Matcher m = p.matcher(errorPath);

			        		if (m.find()) {
			        		    String basePath = m.group(1);

			        		    String jsonPointer = basePath
			        		        .replace("$", "")
			        		        .replace(".", "/")
			        		        .replaceAll("\\[(\\d+)\\]", "/$1");

			        		    JsonNode baseNode = jsonNode.at(jsonPointer);

			        		    errorHblNo = baseNode.path("OrderInfo").path("HblNo").asText();
			        		}
			        		rmidx = vm.getMessage().indexOf(":");
			        		errorValidationLine = vm.getMessage().substring(0, rmidx).replace("$.", "");
			        		returnMessage = vm.getMessage().substring(rmidx+2);
		        		}
		        	}
				} catch(Exception e){
					accessFlag = "E";
					returnMessage = e.toString();
				}
        	}
	        
	        if("Y".equals(accessFlag)){
	        	ObjectMapper mapper = new ObjectMapper();
	        	 mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	        	String samString = mapper.writeValueAsString(order);
	        	path = apiPath + "SUCC/" + bound + "/";
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
	 				String samString = mapper.writeValueAsString(order);
	 	 			path = apiPath + "ERROR/" + bound + "/";
	 	 			pathDir = new File(path);
	 	 			if (pathDir.exists() == false)	pathDir.mkdirs();
	 	 			infileName = "ErrorValidation_" + infileName;
	 	 			fos = new FileOutputStream(path + infileName);
	 	 			osw = new OutputStreamWriter(fos, "UTF-8");
	 	 			bw = new BufferedWriter(osw);
	 	 			bw.write(Util.null2str(samString));
	 	 			bw.close();
	 	 			returnMessage = " 에러가 발생한 부분 => HblNo : "+ errorHblNo +" "+ errorValidationLine + " " + returnMessage;
	 	 		}else if("E".equals(accessFlag)){
	 	 			ObjectMapper mapper = new ObjectMapper();
	 				String samString = mapper.writeValueAsString(order);
	 	 			path = apiPath + "ERROR/" + bound + "/";
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
			tmpMap.put("if_doc_code", "ORDER_API");
			tmpMap.put("if_doc_name", "오더API(DKX)");
			tmpMap.put("if_doc_reference_no", "");	 					// 문서번호 
			tmpMap.put("if_vender", sender);							// 중개사업자
			tmpMap.put("if_send_recv_type", "R");						// 송수신타입(S:SEND, R:RECV)
			tmpMap.put("if_send_recv_method", "JSON(DKX) To DB");			// 송수신방법
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
			
			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			documentApi = new DocumentApi(client);
			documentApi.createDocument(index, logId, tmpMap);
	        
			resultData.setReturnCode(returnCode);
			resultData.setReturnMessage(returnMessage);
			orderReturn.setReturnData(resultData);
		}catch(Exception e){
			log.error(Util.getPrintStacTraceString(e));
			resultData.setReturnCode("FAIL");
			resultData.setReturnMessage("System Error.");
			orderReturn.setReturnData(resultData);
		}
		return orderReturn;
	}
}
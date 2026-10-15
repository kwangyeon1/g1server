package org.yht.exps.express.service.edi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.SocketFactory;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.yht.MailSmtpSender;
import org.yht.exps.express.com.ftp.FTPConnector;
import org.yht.exps.express.com.ftp.SFTPConnector;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.edi.ManifestMapper;
import org.yht.exps.express.model.bms.unipass.CargoProgressInfo;
import org.yht.exps.express.model.edi.EcmCargoProgressInfo;
import org.yht.exps.express.model.edi.EcmCargoProgressInfo.CargCsclPrgsInfo;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.EncryptionUtils;
import org.yht.util.Util;


import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

@Slf4j
@Service
@Transactional
public class EdiCommonService {
		
	@Autowired
	private CustomProperties customProperties;
	@Autowired
	ManifestMapper manifestMapper;
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	OrderMapper orderMapper;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	
	@Value("${spring.mail.error.host}")
	private String host;
	@Value("${spring.mail.error.port}")
	private int port;
	@Value("${spring.mail.error.username}")
	private String username;
	@Value("${spring.mail.error.password}")
	private String password;
	@Value("${spring.mail.error.rawKey}")
	private String rawKey;
	@Value("${spring.profiles.active}")
	private String profile;
	@Value("${custom.edi-send-yn}")
	private String ediSendYn;
	
    @Value("${custom.unipass-base-url}")
    private String unipassBaseUrl_;
    @Value("${custom.unipass-port}")
    private int unipassPort_;
    @Value("${custom.unipass-cargo-progress-path}")
    private String unipassCargoProgressPath_;
    @Value("${custom.unipass-cargo-progress-key}")
    private String unipassCargoProgressKey_;
	
	/**
	 * 에러 코드 메시지 가져오기
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public String getErrorCodeName(Map<String, Object> map) throws Exception {
		if (!"".equals(Util.null2str(map.get("srchCode")).trim())) {
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(map, "userLanguage|srchEtcField", false, null, null, null, null);
			Map<String, Object> errorCodeMessage = commonMapper.getCodeDetail(map);
			if (errorCodeMessage == null) {
				return "No Message Error Code("+map.get("srchCode")+")";
			} else {
				return Util.null2str(errorCodeMessage.get("CODE_NAME"), "No Message Error Code("+map.get("srchCode")+")");
			}
			
		} else {
			return Util.null2str(map.get("message"));
		}
	}
	
	/**
	 * 에레 메시지 세팅
	 * 
	 * @param referance
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getErrorMessage(String referance, String message, String blNo, String hsn) throws Exception {
		Map<String, Object> errorMessage = new HashMap<String, Object>();
		errorMessage.put("REFERANCE", referance);
		errorMessage.put("ERROR_MESSAGE", message);
		if(blNo != null){
			errorMessage.put("blNo", blNo);
		}
		if(hsn != null){
			errorMessage.put("hsn", hsn);
		}
		errorMessage.put("messageType", "오류");
		return errorMessage;
	}
	public Map<String, Object> getErrorMessage(String referance, String message) throws Exception {
		return getErrorMessage(referance, message, null, null);
	}
	
	/**
	 * 오류검증 메시지 세팅 : 오류인 경우에는 오류내역 창으로 보여주고 리턴
	 * 
	 * @param docSend
	 * @param tempMblData
	 * @param errList
	 * @param errorMessage
	 * @param result
	 * @throws Exception
	 */
	public void setValidateMessage(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> errList, Map<String, Object> result) throws Exception{

		// 항공일 경우 
		if ("AIR".equals(Util.null2str(docSend.get("bound")))) {
			if (Util.null2str(tempMblData.get("MBL_NO")).trim().length() > 3) {
				result.put("mblNo", Util.null2str(tempMblData.get("MBL_NO")).trim().substring(0, 3) + "-" + Util.null2str(tempMblData.get("MBL_NO")).trim().substring(3, Util.null2str(tempMblData.get("MBL_NO")).trim().length()));
			} else {
				result.put("mblNo", Util.null2str(tempMblData.get("MBL_NO")).trim());
			}
			
		// 항공이 아닐 경우
		} else {
			result.put("mblNo", Util.null2str(tempMblData.get("MBL_NO")).trim());
		}

		result.put("odJobNo", Util.null2str(tempMblData.get("OD_JOB_NO")));
		result.put("origCode", Util.null2str(tempMblData.get("ORIG_CODE")));
		result.put("destCode", Util.null2str(tempMblData.get("DEST_CODE")));
		result.put("obDate", Util.null2str(tempMblData.get("OB_DATE")));
		result.put("arDate", Util.null2str(tempMblData.get("AR_DATE")));

		result.put("validateErrorYn", "Y");

		result.put("processCode", "A00001");
		result.put("errorData", errList);
	}
	public void setValidateMessageDel(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> errList, Map<String, Object> result) throws Exception{

		Map<String, Object> tempMap = new HashMap<String, Object>();
		
		// 항공일 경우 
		if ("AIR".equals(Util.null2str(docSend.get("bound")))) {
			if (Util.null2str(tempMblData.get("MBL_NO")).trim().length() > 3) {
				tempMap.put("mblNo", Util.null2str(tempMblData.get("MBL_NO")).trim().substring(0, 3) + "-" + Util.null2str(tempMblData.get("MBL_NO")).trim().substring(3, Util.null2str(tempMblData.get("MBL_NO")).trim().length()));
			} else {
				tempMap.put("mblNo", Util.null2str(tempMblData.get("MBL_NO")).trim());
			}
			
		// 항공이 아닐 경우
		} else {
			tempMap.put("mblNo", Util.null2str(tempMblData.get("MBL_NO")).trim());
		}

		tempMap.put("odJobNo", Util.null2str(tempMblData.get("OD_JOB_NO")));
		tempMap.put("origCode", Util.null2str(tempMblData.get("ORIG_CODE")));
		tempMap.put("destCode", Util.null2str(tempMblData.get("DEST_CODE")));
		tempMap.put("obDate", Util.null2str(tempMblData.get("OB_DATE")));
		tempMap.put("arDate", Util.null2str(tempMblData.get("AR_DATE")));

		tempMap.put("validateErrorYn", "Y");

		tempMap.put("ediDocNo", Util.null2str(tempMblData.get("EDI_DOC_NO")));
		
		tempMap.put("processCode", "A00001");
		tempMap.put("errorData", errList);
		
		result.put(Util.null2str(tempMblData.get("EDI_DOC_NO")), tempMap);
	}
	
	/**
	 * 전송파일 경로 가져오기
	 * 
	 * @param vender
	 * @param interfacePath
	 * @param bizPath
	 * @param docSend
	 * @return
	 */
	public String[] getSendFilePath(String vender, String bizPath, Map<String, Object> docSend) {
		String rVal[] = new String[5];
		
		String entCode = Util.null2str(docSend.get("entCode"));
		if(!Util.null2str(vender).equals("")){
			vender += "/";
		}else {
			vender ="";
		}
		rVal[0] = customProperties.getInterfaceDir() + bizPath + "/" + vender + entCode + "/SEND/";
		rVal[1] = customProperties.getBackupDir() + bizPath + "/" + entCode + "/SEND/" + DateTime.getDate("yyyy-MM") + "/";
		rVal[2] = customProperties.getInterfaceDir() + bizPath + "/" + vender + entCode + "/RECV/";
		rVal[3] = customProperties.getBackupDir() + bizPath + "/" + entCode + "/RECV/" + DateTime.getDate("yyyy-MM") + "/";
		return rVal;
	}
	
	/**
	 * 서버 상태 확인
	 * @param address
	 * @param port
	 * @return
	 */
	public boolean isOnline(String address, int port) {
		boolean result;
		if("Y".equals(ediSendYn)){
			SocketFactory sf = SocketFactory.getDefault();
			try (Socket socket = sf.createSocket()) {
				socket.connect(new InetSocketAddress(address, port), 3000);
				socket.close();
				result = true;
			} catch (IOException e) {
				result = false;
			}
		}else{
			result = true;
		}
		return result;
	}
	
	/** ebMS 관세청 전송 */
	public JSONObject sendEbms(String ediPortalMailboxId, String doucmentCode, JSONObject sendData) throws Exception {
		return sendEbms(ediPortalMailboxId, doucmentCode, sendData, "", null);
	}
	public JSONObject sendEbms(String ediPortalMailboxId, String doucmentCode, JSONObject sendData, String sendType) throws Exception {
		return sendEbms(ediPortalMailboxId, doucmentCode, sendData, sendType, null);
	}
	public JSONObject sendEbms(String ediPortalMailboxId, String doucmentCode, JSONObject sendData, String sendType, String token) throws Exception {
		JSONObject jsonObject = null;
		if("local".equals(profile)){
			ediPortalMailboxId = "VC116814146901";
		}
		log.info("[sendEbms] 진입 - 사서함ID={}, 문서코드={}, sendType={}, ediSendYn={}", ediPortalMailboxId, doucmentCode, sendType, ediSendYn);
		
		if("Y".equals(ediSendYn)){
			if(!isOnline(customProperties.getEbmsIp(), Util.null2int(customProperties.getEbmsPort()))) {
				log.error("[sendEbms] REST 서버 연결 실패");
				jsonObject = new JSONObject();
				jsonObject.put("customsErrCode", "Rest Server Connection Error");
				jsonObject.put("customsErrDesc", "Rest Server Connection Error");
				jsonObject.put("body", "Rest Server Connection Error");
				return jsonObject;
			}
			
			URL url = null;
			HttpURLConnection conn = null;
			BufferedReader in = null;
			String inputLine = null;
			JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
			String ebmsUrl = customProperties.getEbmsUrl();
			String ebmsAccessKey = customProperties.getEbmsAccessKey();
			
			if(token == null){
				try{
					/** 토큰 */
					url = new URL(ebmsUrl + "/security/get/token/" + ediPortalMailboxId);
					log.info("[sendEbms][TOKEN] 토큰 발급 요청 - 사서함ID={}, url={}", ediPortalMailboxId, url);
			
					conn = (HttpURLConnection)url.openConnection();
			        conn.setRequestMethod("GET");
			        conn.setRequestProperty("access-key", ebmsAccessKey);
			        conn.setDoInput(true);
			        conn.setDoOutput(true);
			        
			        in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			    	while((inputLine = in.readLine()) != null) {
			   		 	jsonObject = (JSONObject) parser.parse(inputLine);
			    	}
				}catch(Exception e){
					log.error("[sendEbms][TOKEN] 토큰 발급 실패 - 사서함ID=" + ediPortalMailboxId, e);
					log.error(Util.getPrintStacTraceString(e));
					throw e;
				}finally{
					if(in != null){in.close();}
					if(conn != null){conn.disconnect();}
				}
				token = Util.null2str(jsonObject.get("token"));
				/** 토큰 값 자체는 보안상 로그에 남기지 않고 발급 성공 여부만 기록 */
				log.info("[sendEbms][TOKEN] 토큰 발급 완료 - 사서함ID={}, 발급성공={}", ediPortalMailboxId, !token.isEmpty());
			}
	    	
			jsonObject = null;
			try{
				/** 전송 */
				if(sendType.equals("FILE")){	/** FTP */
					SFTPConnector sftpConnector = null;
					String messageCode;
					
					try {
						sftpConnector = new SFTPConnector();
						messageCode = sftpConnector.connectSFTP(22, customProperties.getEbmsIp(), ediPortalMailboxId, customProperties.getEbmsFtpPw());
						if (messageCode.trim().length() > 0) {
							jsonObject = new JSONObject();
							jsonObject.put("customsErrCode", "Rest FTP Connection Error(" + messageCode + ")");
							jsonObject.put("customsErrDesc", "Rest FTP Connection Error(" + messageCode + ")");
							jsonObject.put("body", "Rest FTP Connection Error(" + messageCode + ")");
							return jsonObject;
						}
								
						JSONArray jsonArray = (JSONArray)sendData.get("dataList");
						JSONObject data;
						for(int i = 0; i < jsonArray.size(); i++){
							data = (JSONObject)jsonArray.get(i);
							try {
								sftpConnector.sftpFileSendForEDI(Util.null2str(data.get("filePath")), Util.null2str(data.get("fileName")), Util.null2str(data.get("fileName")), customProperties.getEbmsFtpPath(), ediPortalMailboxId + "/send", false);
							}catch(Exception ee) {
								jsonObject = new JSONObject();
								jsonObject.put("customsErrCode", "Rest FTP Upload Error");
								jsonObject.put("customsErrDesc", "Rest FTP Upload Error");
								jsonObject.put("body", "Rest FTP Upload Error");
								return jsonObject;
							}
							data.remove("xml");
							data.remove("filePath");
							data.put("docType", doucmentCode);
						}
						
						sendData.put("fileList", jsonArray);
						sendData.remove("dataList", jsonArray);
						ebmsUrl += "/customs/send/file/"  + ediPortalMailboxId + "/list";
										
					}catch(Exception e){
						throw e;
					}finally{
						if (sftpConnector != null && sftpConnector.getConn() != null) {
							sftpConnector.disConnectSFTPForKTNET();
						}
					}
							
				}else{
					ebmsUrl += "/customs/send/data/"  + ediPortalMailboxId + "/" + doucmentCode;
					if(sendType.equals("LIST")){
						ebmsUrl += "/list";
					}
				}
				
				url = new URL(ebmsUrl);
				log.info("[sendEbms][REST] POST 전송 - 사서함ID={}, sendType={}, url={}", ediPortalMailboxId, sendType, ebmsUrl);
				
		    	conn = (HttpURLConnection)url.openConnection();
		        conn.setRequestMethod("POST");
		        conn.setRequestProperty("Content-type","application/json");
		        conn.setRequestProperty("access-key", ebmsAccessKey);
		        conn.setRequestProperty("Authorization", "Bearer " + token);
		        conn.setDoInput(true);
		        conn.setDoOutput(true);
		    	
		        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
		        out.write(sendData.toString());
		        out.close();
		        
		        int responseCode = conn.getResponseCode();
		        log.info("[sendEbms][REST] HTTP 응답코드 - 사서함ID={}, url={}, responseCode={}", ediPortalMailboxId, ebmsUrl, responseCode);

		        in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		        StringBuilder rawResponse = new StringBuilder();
	        	while((inputLine = in.readLine()) != null) {
	        		rawResponse.append(inputLine);
	        		if(sendType.equals("FILE")){
	        			JSONArray jsonArray = (JSONArray) parser.parse(inputLine);	/** [{"docType":"GOVCBR936","customsResponse":{"customsErrCode":"C200","customsErrDesc":"정상처리","body":"ebXML 메시지가 정상 처리 되었습니다.","data":null}}] */
	        			jsonObject = (JSONObject)((JSONObject)jsonArray.get(0)).get("customsResponse");
	        		}else{
	        			jsonObject = (JSONObject) parser.parse(inputLine);
	        		}
	        	}
		        log.info("[sendEbms][REST] 관세청 원문 응답 - 사서함ID={}, response={}", ediPortalMailboxId, rawResponse.toString());

		        String respErrCode = Util.null2str(jsonObject.get("customsErrCode"));
		        String respErrDesc = Util.null2str(jsonObject.get("customsErrDesc"));
		        if("C200".equals(respErrCode)){
		        	log.info("[sendEbms][REST] 관세청 정상 처리 - 사서함ID={}, customsErrCode={}, customsErrDesc={}", ediPortalMailboxId, respErrCode, respErrDesc);
		        }else{
		        	log.error("[sendEbms][REST] 관세청 오류 응답 - 사서함ID={}, customsErrCode={}, customsErrDesc={}, body={}", ediPortalMailboxId, respErrCode, respErrDesc, Util.null2str(jsonObject.get("body")));
		        }
		        jsonObject.put("token", token);
	    	}catch(Exception e){
	    		/** SFTP 업로드 이후 REST POST(전송/응답 파싱) 단계에서 발생한 오류. FILE 모드가 아니어도 여기로 들어온다. */
	    		log.error("[sendEbms][REST] 전송/응답 처리 실패 - 사서함ID=" + ediPortalMailboxId + ", sendType=" + sendType + ", url=" + ebmsUrl, e);
				log.error(Util.getPrintStacTraceString(e));
				jsonObject = new JSONObject();
				jsonObject.put("customsErrCode", "Rest FTP Upload Error!");
				jsonObject.put("customsErrDesc", "Rest FTP Upload Error!");
				jsonObject.put("body", "Rest FTP Upload Error!");
				return jsonObject;
			}finally{
				if(in != null){in.close();}
				if(conn != null){conn.disconnect();}
			}
		}else {
			log.info("[sendEbms] 전송 스킵(ediSendYn != Y) - 사서함ID={}, 문서코드={}", ediPortalMailboxId, doucmentCode);
			jsonObject = new JSONObject();
		}
    	return jsonObject;
	}

	public JSONObject sendEbmsGov(String entCode, String sendType, JSONObject sendData) throws Exception {
		JSONObject jsonObject = null;
		String token = "";
//		if("local".equals(profile)){
//			ediPortalMailboxId = "VC116814146901";
//		}

		if("Y".equals(ediSendYn)){
			if(!isOnline(customProperties.getEbmsIp(), Util.null2int(customProperties.getEbmsPort()))) {
				jsonObject = new JSONObject();
				jsonObject.put("errCode", "0001");
				jsonObject.put("errDesc", "Rest Server Connection Error");
				return jsonObject;
			}

			URL url = null;
			HttpURLConnection conn = null;
			BufferedReader in = null;
			String inputLine = null;
			JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
			String ebmsUrl = customProperties.getEbmsUrl();
			String ebmsAccessKey = customProperties.getEbmsAccessKey();
			ebmsUrl = ebmsUrl.substring(0, ebmsUrl.lastIndexOf("/"));

			try{
				/** 토큰 */
				url = new URL(ebmsUrl + "/ecapi/security/get/token/" + entCode);

				conn = (HttpURLConnection)url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("access-key", ebmsAccessKey);
				conn.setDoInput(true);
				conn.setDoOutput(true);

				in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				while((inputLine = in.readLine()) != null) {
					jsonObject = (JSONObject) parser.parse(inputLine);
				}
			}catch(Exception e){
				log.error(Util.getPrintStacTraceString(e));
				throw e;
			}finally{
				if(in != null){in.close();}
				if(conn != null){conn.disconnect();}
			}
			token = Util.null2str(jsonObject.get("token"));

			jsonObject = null;
			try{
				/** 전송 */
				if(sendType.equals("submit")) {
					ebmsUrl += "/api/ec/transaction/submit";
				} else if(sendType.equals("result")) {
					ebmsUrl += "/api/ec/transaction/result";
				} else if(sendType.equals("validation")) {
					ebmsUrl += "/api/ec/pccc/validation";
				} else if(sendType.equals("revalidation")) {
					ebmsUrl += "/api/ec/pccc/revalidation";
				}

				url = new URL(ebmsUrl);

				conn = (HttpURLConnection)url.openConnection();
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-type","application/json");
				conn.setRequestProperty("access-key", ebmsAccessKey);
				conn.setRequestProperty("Authorization", "Bearer " + token);
				conn.setDoInput(true);
				conn.setDoOutput(true);

				OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
				out.write(sendData.toString());
				out.close();

				in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				while((inputLine = in.readLine()) != null) {
					jsonObject = (JSONObject) parser.parse(inputLine);
//					if(sendType.equals("FILE")){
//						JSONArray jsonArray = (JSONArray) parser.parse(inputLine);	/** [{"docType":"GOVCBR936","customsResponse":{"customsErrCode":"C200","customsErrDesc":"정상처리","body":"ebXML 메시지가 정상 처리 되었습니다.","data":null}}] */
//						jsonObject = (JSONObject)((JSONObject)jsonArray.get(0)).get("customsResponse");
//					}else{
//						jsonObject = (JSONObject) parser.parse(inputLine);
//					}
				}
				jsonObject.put("token", token);
			}catch(Exception e){
				log.error(Util.getPrintStacTraceString(e));
				
				jsonObject = new JSONObject();
				jsonObject.put("errCode", "0001");
				jsonObject.put("errDesc", "Send Data Error");
				return jsonObject;
			}finally{
				if(in != null){in.close();}
				if(conn != null){conn.disconnect();}
			}
		}else {
			jsonObject = new JSONObject();
		}
		return jsonObject;
	}

	public JSONObject sendEbmsTest(String ediPortalMailboxId, String doucmentCode, JSONObject sendData, String sendType, String token) throws Exception {
		JSONObject jsonObject = null;
		if("Y".equals(ediSendYn)){
			if(!isOnline("211.43.13.136", Util.null2int(customProperties.getEbmsPort()))) {
				jsonObject = new JSONObject();
				jsonObject.put("customsErrCode", "Rest Server Connection Error");
				jsonObject.put("customsErrDesc", "Rest Server Connection Error");
				jsonObject.put("body", "Rest Server Connection Error");
				return jsonObject;
			}
			
			URL url = null;
			HttpURLConnection conn = null;
			BufferedReader in = null;
			String inputLine = null;
			JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
			String ebmsUrl = "http://211.43.13.136:38120/ebms";
			String ebmsAccessKey = customProperties.getEbmsAccessKey();
			
			if(token == null){
				try{
					/** 토큰 */
					url = new URL(ebmsUrl + "/security/get/token/" + ediPortalMailboxId);
			
					conn = (HttpURLConnection)url.openConnection();
			        conn.setRequestMethod("GET");
			        conn.setRequestProperty("access-key", ebmsAccessKey);
			        conn.setDoInput(true);
			        conn.setDoOutput(true);
			        
			        in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			    	while((inputLine = in.readLine()) != null) {
			   		 	jsonObject = (JSONObject) parser.parse(inputLine);
			    	}
				}catch(Exception e){
					log.error(Util.getPrintStacTraceString(e));
					throw e;
				}finally{
					if(in != null){in.close();}
					if(conn != null){conn.disconnect();}
				}
				token = Util.null2str(jsonObject.get("token"));
			}
	    	
			jsonObject = null;
			try{
				/** 전송 */
				if(sendType.equals("FILE")){	/** FTP */
					SFTPConnector sftpConnector = null;
					String messageCode;
					
					try {
						sftpConnector = new SFTPConnector();
						messageCode = sftpConnector.connectSFTP(22, "211.43.13.136", ediPortalMailboxId, customProperties.getEbmsFtpPw());
						if (messageCode.trim().length() > 0) {
							jsonObject = new JSONObject();
							jsonObject.put("customsErrCode", "Rest FTP Connection Error(" + messageCode + ")");
							jsonObject.put("customsErrDesc", "Rest FTP Connection Error(" + messageCode + ")");
							jsonObject.put("body", "Rest FTP Connection Error(" + messageCode + ")");
							return jsonObject;
						}
								
						JSONArray jsonArray = (JSONArray)sendData.get("dataList");
						JSONObject data;
						for(int i = 0; i < jsonArray.size(); i++){
							data = (JSONObject)jsonArray.get(i);
							try {
								sftpConnector.sftpFileSendForEDI(Util.null2str(data.get("filePath")), Util.null2str(data.get("fileName")), Util.null2str(data.get("fileName")), customProperties.getEbmsFtpPath(), ediPortalMailboxId + "/send", false);
							}catch(Exception ee) {
								jsonObject = new JSONObject();
								jsonObject.put("customsErrCode", "Rest FTP Upload Error");
								jsonObject.put("customsErrDesc", "Rest FTP Upload Error");
								jsonObject.put("body", "Rest FTP Upload Error");
								return jsonObject;
							}
							data.remove("xml");
							data.remove("filePath");
							data.put("docType", doucmentCode);
						}
						
						sendData.put("fileList", jsonArray);
						sendData.remove("dataList", jsonArray);
						ebmsUrl += "/customs/send/file/"  + ediPortalMailboxId + "/list";
										
					}catch(Exception e){
						throw e;
					}finally{
						if (sftpConnector != null && sftpConnector.getConn() != null) {
							sftpConnector.disConnectSFTPForKTNET();
						}
					}
							
				}else{
					ebmsUrl += "/customs/send/data/"  + ediPortalMailboxId + "/" + doucmentCode;
					if(sendType.equals("LIST")){
						ebmsUrl += "/list";
					}
				}
				
				url = new URL(ebmsUrl);
				
		    	conn = (HttpURLConnection)url.openConnection();
		        conn.setRequestMethod("POST");
		        conn.setRequestProperty("Content-type","application/json");
		        conn.setRequestProperty("access-key", ebmsAccessKey);
		        conn.setRequestProperty("Authorization", "Bearer " + token);
		        conn.setDoInput(true);
		        conn.setDoOutput(true);
		    	
		        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
		        out.write(sendData.toString());
		        out.close();
		        
		        in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	        	while((inputLine = in.readLine()) != null) {
	        		if(sendType.equals("FILE")){
	        			JSONArray jsonArray = (JSONArray) parser.parse(inputLine);	/** [{"docType":"GOVCBR936","customsResponse":{"customsErrCode":"C200","customsErrDesc":"정상처리","body":"ebXML 메시지가 정상 처리 되었습니다.","data":null}}] */
	        			jsonObject = (JSONObject)((JSONObject)jsonArray.get(0)).get("customsResponse");
	        		}else{
	        			jsonObject = (JSONObject) parser.parse(inputLine);
	        		}
	        	}
		        jsonObject.put("token", token);
	    	}catch(Exception e){
	    		log.error(Util.getPrintStacTraceString(e));
				
				jsonObject = new JSONObject();
				jsonObject.put("customsErrCode", "Rest FTP Upload Error!");
				jsonObject.put("customsErrDesc", "Rest FTP Upload Error!");
				jsonObject.put("body", "Rest FTP Upload Error!");
				return jsonObject;
			}finally{
				if(in != null){in.close();}
				if(conn != null){conn.disconnect();}
			}
		}else {
			jsonObject = new JSONObject();
		}
    	return jsonObject;
	}
	/** cs프로그램 관세청전송 */
	public void insertGovSend(CommonMapper mapper, Map map) throws Exception {
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(map, "gmtTime", false, null, null, null, null);
		mapper.insertGovSend(map);
	}
	
	public void asyncErrorSendEmail(String errorMethod, Map params, Exception e) throws Exception {
	    try {
	    	if("Y".equals(ediSendYn)){
	    		MailSmtpSender sender = new MailSmtpSender(host, port, username, EncryptionUtils.decryptAES(password, rawKey));
			    String[] receiver = Util.null2str(customProperties.getManagerEmail()).split(",");
			    
		    	String cause = "";
				String exceptMessage = "";
				try{
					cause = e.getCause().toString();
				}catch(Exception ex){
					cause = e.toString();		
				}
				exceptMessage = Util.getPrintStacTraceString(e);
				
		    	StringBuffer content = new StringBuffer();
				content.append("<b>Method :</b> " + errorMethod + "<br/>");
				content.append("<b>Cause :</b> " + cause + "<br/>");
				content.append("<b>Parameters :</b> " + params.toString() + "<br/>");
				content.append("<b>Exception Message :</b><br/> " + exceptMessage);
				
				String senderName = profile.toUpperCase();
		    	if(senderName.contains("PROD")){
		    		senderName = "G1Express";
		    	}
		    	sender.sendSmtpMail("Expmaster@yht.co.kr", senderName + " REST", receiver, "[" + errorMethod + "] Express Error", content.toString());
	    	}
	    } catch(Exception ex){
	    	log.error(Util.getPrintStacTraceString(ex));
		}
	}
	
	/**
	 * 파일 쓰기
	 * 
	 * @param fileName	- 파일명
	 * @param dirPath	- 폴더명
	 * @return
	 * @throws Exception
	 */
	public BufferedWriter getFileWriterOpen(String fileName, String dirPath[]) throws Exception {
		File file				= null;
		File folder				= null;
		FileWriter writer		= null;
		BufferedWriter bfw		= null;

		try {
			folder = new File(dirPath[0]);

			// 폴더 존재 유무 체크
			if(!folder.isDirectory())	folder.mkdirs();

			file   		= new File(dirPath[0] + fileName);	// 폴더 + 파일명
			writer	= new FileWriter(file);
			bfw			= new BufferedWriter(writer);
			
		} catch(Exception e) {
			if (bfw != null) {
				try{ bfw.close(); } catch(Exception e2){ throw e2; }
			}
			if (writer != null) {
				try{ writer.close(); } catch(Exception e2){ throw e2; }
			}

			throw e;
		}

		return bfw;
	}
	
	/**
	 * 포워더 정보 및 중개사업자 정보 오류 검증
	 * 
	 * @param docSend
	 * @param tempMblData
	 * @param fwSendAccInfo
	 * @param ediHostSendInfo
	 * @param errList
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public boolean getSendHostAndBaseDataValidate(Map<String, Object> docSend, Map<String, Object> tempMblData, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo, List<Map<String, Object>> errList, Map<String, Object> result) throws Exception{

		String message = null;
		String mblNo = null;
		String ifServiceCode = null;
		String ifVender = null;
		String docType = Util.null2str(docSend.get("docType"));
		
		if (!"ISF".equals(docType) 
				&& !"MCUBE-COMXBL".equals(docType) 
				&& !"FNC".equals(docType) 
				&& !"GLOVIS_TRACING".equals(docType)
				&& !"GLOVIS_AI_BL".equals(docType)
				&& !"GLOVIS_SI_BL".equals(docType)
				&& !"GLOVIS_INVOICE".equals(docType)
				&& !"FNC_BL".equals(docType)){
			
			mblNo = Util.null2str(tempMblData.get("MBL_NO")).trim().substring(0, 3) + "-" + Util.null2str(tempMblData.get("MBL_NO")).trim().substring(3, Util.null2str(tempMblData.get("MBL_NO")).trim().length());
		} else {
			mblNo = Util.null2str(tempMblData.get("HBL_NO")).trim();
		}

		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("entCode", Util.null2str(docSend.get("entCode")));
		tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");

		// 0. 사업장의 등록부호 체크
		if ("".equals(Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).trim())) {
			tempSearchData.put("srchCode", "MF-58");	// 세관(관세청) 등록부호를 기재하기 바랍니다.
			message = this.getErrorCodeName(tempSearchData);

			errList.add(this.getErrorMessage("사업장정보", message));
			this.setValidateMessage(docSend, tempMblData, errList, result);

			return false;
		}

		// 1. 중개사업자 값이 없을 경우 에레 코드/메시지 세팅후 종료
		ifVender = Util.null2str(docSend.get("ediVender")).trim();
		if ("".equals(ifVender)) {
			tempSearchData.put("srchCode", "MF-50");	// 해당 M.B/L에 중개사업자가 없습니다.
			message = this.getErrorCodeName(tempSearchData);

			errList.add(this.getErrorMessage(mblNo, message));

			this.setValidateMessage(docSend, tempMblData, errList, result);

			return false;
		}

		// 2. 서비스코드 값이 없을 경우 에레 코드/메시지 세팅후 종료
		ifServiceCode = this.getServiceCode(Util.null2str(docSend.get("solution")), Util.null2str(docSend.get("bound")), Util.null2str(docSend.get("boundType")), docType, ifVender);
		docSend.put("ifServiceCode", ifServiceCode);

		if ("".equals(ifServiceCode)) {
			tempSearchData.put("srchCode", "MF-51");	// 
			message = this.getErrorCodeName(tempSearchData);

			errList.add(this.getErrorMessage("Office", ifVender + "(" + docType + ") : "+ message));

			this.setValidateMessage(docSend, tempMblData, errList, result);

			return false;
		}

		// 3. 중개사업자별 문서 및 송/수신 정보 체크( TB_SMS_ENT_OFFICE_INTERFACE )
		if (ediHostSendInfo == null) {
			tempSearchData.put("srchCode", "MF-52");	// 중개사업자 정보가 등록 되지 않았습니다.
			message = this.getErrorCodeName(tempSearchData);

			errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));

			this.setValidateMessage(docSend, tempMblData, errList, result);

			return false;
		}
		
		// 4. 서비스코드 전송가능 여부 체크 
		if (ediHostSendInfo != null && !"Y".equals(Util.null2str(ediHostSendInfo.get("USE_YN")))) {
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "해당 서비스는 사용 중지 상태 입니다.");
			message = this.getErrorCodeName(tempSearchData);
			
			errList.add(this.getErrorMessage("Office", ifVender + " : " + "'" + ifServiceCode + "' " + message));
			this.setValidateMessage(docSend, tempMblData, errList, result);

			return false;
		}
		
		switch (Util.null2str(docSend.get("ifServiceCode"))) {
			// 적하목록 항공수출 KCNET(특송/포워더)
			case CodeDefinition.KCNET_MFCS_AIR_EXP_FHL:
			case CodeDefinition.KCNET_MFCS_AIR_IMP_FHL:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
	
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
	
				// FTP
				if ("FTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					// PIMA 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_PIMA_CODE")).trim())) {
						tempSearchData.put("srchCode", "MF-01");	// 자사정보에 정보(FHL,FWB PIMA)가 없습니다.
						message = this.getErrorCodeName(tempSearchData);
	
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
	
					// IP 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP")).trim())) {
						tempSearchData.put("srchCode", "MF-53");	// IP 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
	
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
	
					// PORT 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")).trim())) {
						tempSearchData.put("srchCode", "MF-54");	// PORT 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
	
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
	
					// ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
	
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
	
					// PASS 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PASSWORD 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
	
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
	
					// IF_ETC_1
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_1")).trim())) {
						tempSearchData.put("srchCode", "MF-61");	// IF_ETC_1 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
	
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
	
					// IF_ETC_2
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_2")).trim())) {
						tempSearchData.put("srchCode", "MF-62");	// IF_ETC_2 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
	
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				}
				break;
			
			// 적하목록정정, 하기이상신고 KCNET
			case CodeDefinition.KCNET_MFCS_AIR_IMP_WSC:	
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
	
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// WEBSERVICE
				if ("WEBSERVICE".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					
					// IP 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP")).trim())) {
						tempSearchData.put("srchCode", "MF-53");	// IP 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
	
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// PORT 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")).trim())) {
						tempSearchData.put("srchCode", "MF-54");	// PORT 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
	
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
				}
				
				break;
				
			// 적하목록 해운수출 KTNET
			case CodeDefinition.KTNET_MFCS_SEA_EXP_TXT:
			case CodeDefinition.KTNET_MFCS_SEA_IMP_TXT:
			case CodeDefinition.KTNET_MFCS_SEA_EXP_TXT_SFTP:
			case CodeDefinition.KTNET_MFCS_SEA_IMP_TXT_SFTP:
			case CodeDefinition.KLNET_PRISM_SEA_EXP_TXT:
			case CodeDefinition.KLNET_PRISM_SEA_IMP_TXT:
			case CodeDefinition.KLNET_EMS:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
	
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
	
				// FTP
				if ("FTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim()) || "SFTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					// 송신식별자 체크
					if("".equals(Util.null2str(ediHostSendInfo.get("IF_SENDER_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-63");	// 송신식별자 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
	
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
	
					// 수신식별자 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_RECEIVER_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-64");	// 수신식별자 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
	
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
	
					// IP 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP")).trim())) {
						tempSearchData.put("srchCode", "MF-53");	// IP 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
	
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
	
					// PORT 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")).trim())) {
						tempSearchData.put("srchCode", "MF-54");	// PORT 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
	
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
	
					// ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
	
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
	
					// PASS 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PASSWORD 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
	
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				}
				break;
				
			// 적하목록 항공수출 KTNET/FHL(ACPS/AMS)
			case CodeDefinition.KTNET_ACPS_AIR_EXP_FHL:
			case CodeDefinition.KTNET_ACPS_AIR_IMP_FHL:
			case CodeDefinition.KTNET_EMS:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}

				// FTP
				if ("FTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					// PIMA 체크
					// D/O 전송, 취소 시에는 PIMA 체크를 하지 않는다.
					if (!"Y".equals(Util.null2str(ediHostSendInfo.get("DO_YN")).trim())) {
						if ("".equals(Util.null2str(ediHostSendInfo.get("IF_PIMA_CODE")).trim())) {
							tempSearchData.put("srchCode", "");
							tempSearchData.put("message", "자사정보에 FWB PIMA Code를 설정하기 바랍니다.");
							message = this.getErrorCodeName(tempSearchData);
		
							errList.add(this.getErrorMessage("사업장정보", ifVender + " : "+ message));
						}
					}

					// 송신식별자 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SENDER_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-63");	// 송신식별자 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// 수신식별자 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_RECEIVER_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-64");	// 수신식별자 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// IP 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP")).trim())) {
						tempSearchData.put("srchCode", "MF-53");	// IP 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// PORT 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")).trim())) {
						tempSearchData.put("srchCode", "MF-54");	// PORT 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// PASS 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PASSWORD 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				}
				break;
				
			// FWB 목록 항공수출 KTNET
			case CodeDefinition.KTNET_ACPS_AIR_EXP_FWB:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}

				// FTP
				if ("FTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					// 자사상호( 사업장의 영문 상호를 체크 )
					if ("".equals(Util.null2str(fwSendAccInfo.get("ENT_BUSINESS_NAME_ENG")).trim())) {
						tempSearchData.put("srchCode", "");	//
						tempSearchData.put("message", "자사정보에 자사상호를 설정하기 바랍니다.");
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("사업장정보", ifVender + " : "+ message));
					}

					// PIMA 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_PIMA_CODE")).trim())) {
						tempSearchData.put("srchCode", "");
						tempSearchData.put("message", "자사정보에 FWB PIMA Code를 설정하기 바랍니다.");
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("사업장정보", ifVender + " : "+ message));
					}

					// 송신식별자 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SENDER_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-63");	// 송신식별자 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// 수신식별자 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_RECEIVER_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-64");	// 수신식별자 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// IP 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP")).trim())) {
						tempSearchData.put("srchCode", "MF-53");	// IP 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// PORT 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")).trim())) {
						tempSearchData.put("srchCode", "MF-54");	// PORT 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// PASS 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PASSWORD 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				}
				break;

			// FWB 목록 항공수출 KCNET
			case CodeDefinition.KCNET_MFCS_AIR_EXP_FWB:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}

				// FTP
				if ("FTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					// 자사상호( 사업장의 영문 상호를 체크 )
					if ("".equals(Util.null2str(fwSendAccInfo.get("ENT_BUSINESS_NAME_ENG")).trim())) {
						tempSearchData.put("srchCode", "");	//
						tempSearchData.put("message", "자사정보에 자사상호를 설정하기 바랍니다.");
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("사업장정보", ifVender + " : "+ message));
					}

					// PIMA 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_PIMA_CODE")).trim())) {
						tempSearchData.put("srchCode", "");
						tempSearchData.put("message", "자사정보에 FWB PIMA Code를 설정하기 바랍니다.");
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("사업장정보", ifVender + " : "+ message));
					}

//					// 송신식별자 체크
//					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SENDER_ID")).trim())) {
//						tempSearchData.put("srchCode", "MF-63");	// 송신식별자 정보가 입력되지 않았습니다.
//						message = this.getErrorCodeName(tempSearchData);
//
//						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
//					}

//					// 수신식별자 체크
//					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_RECEIVER_ID")).trim())) {
//						tempSearchData.put("srchCode", "MF-64");	// 수신식별자 정보가 입력되지 않았습니다.
//						message = this.getErrorCodeName(tempSearchData);
//
//						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
//					}

					// IP 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP")).trim())) {
						tempSearchData.put("srchCode", "MF-53");	// IP 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// PORT 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")).trim())) {
						tempSearchData.put("srchCode", "MF-54");	// PORT 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// PASS 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PASSWORD 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				}
				break;

			// AMS - 항공수출 KTNET FHL의 AMS와는 다르다
			case CodeDefinition.KTNET_ACPS_AIR_EXP_AMS:
			case CodeDefinition.KTNET_ACPS_AIR_EXP_AMS_SFTP:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}

				// FTP
				if ("FTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim()) || "SFTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					// 송신식별자 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SENDER_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-63");	// 송신식별자 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// 수신식별자 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_RECEIVER_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-64");	// 수신식별자 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// IP 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP")).trim())) {
						tempSearchData.put("srchCode", "MF-53");	// IP 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// PORT 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")).trim())) {
						tempSearchData.put("srchCode", "MF-54");	// PORT 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// PASS 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PASSWORD 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				}
				break;

			// CSI 미세관신고 해운수출 KTNET
			case CodeDefinition.KTNET_MFCS_SEA_EXP_CSI:
			case CodeDefinition.KTNET_MFCS_SEA_EXP_CSI_SFTP:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}

				// FTP
				if ("FTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim()) || "SFTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					// 송신식별자 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SENDER_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-63");	// 송신식별자 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// 수신식별자 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_RECEIVER_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-64");	// 수신식별자 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// IP 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP")).trim())) {
						tempSearchData.put("srchCode", "MF-53");	// IP 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// PORT 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")).trim())) {
						tempSearchData.put("srchCode", "MF-54");	// PORT 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// PASS 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PASSWORD 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				}
				break;

			// AFR 일본관세신고 해운수출 KTNET
			case CodeDefinition.KTNET_MFCS_SEA_EXP_AFR:
			case CodeDefinition.KTNET_MFCS_SEA_EXP_AFR_SFTP:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}

				// FTP
				if ("FTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim()) || "SFTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					// 송신식별자 체크
					if("".equals(Util.null2str(ediHostSendInfo.get("IF_SENDER_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-63");	// 송신식별자 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// 수신식별자 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_RECEIVER_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-64");	// 수신식별자 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// IP 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP")).trim())) {
						tempSearchData.put("srchCode", "MF-53");	// IP 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// PORT 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")).trim())) {
						tempSearchData.put("srchCode", "MF-54");	// PORT 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// PASS 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PASSWORD 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				}
				break;

			// ISF 항공수입
			case CodeDefinition.KGYL_ISF_AIR_IMP:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// IF_ETC_10
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_10")).trim())) {
					tempSearchData.put("srchCode", "IF-36");	// SCAC코드 가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}

				// IF_ETC_3
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_3")).trim())) {
					tempSearchData.put("srchCode", "IF-37");	// AccountNumber 가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// IF_ETC_4
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_4")).trim())) {
					tempSearchData.put("srchCode", "IF-38");	// DepartmentNumber 가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}

				// IF_ETC_5
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_5")).trim())) {
					tempSearchData.put("srchCode", "IF-39");	// UserNumber 가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// IF_ETC_6
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_6")).trim())) {
					tempSearchData.put("srchCode", "IF-40");	// Host Name이 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				break;

			// ISF 해운수입
			case CodeDefinition.KGYL_ISF_SEA_IMP:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// IF_ETC_10
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_10")).trim())) {
					tempSearchData.put("srchCode", "IF-36");	// SCAC코드 가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
		
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
		
				// IF_ETC_3
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_3")).trim())) {
					tempSearchData.put("srchCode", "IF-37");	// AccountNumber 가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
		
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// IF_ETC_4
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_4")).trim())) {
					tempSearchData.put("srchCode", "IF-38");	// DepartmentNumber 가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
		
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
		
				// IF_ETC_5
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_5")).trim())) {
					tempSearchData.put("srchCode", "IF-39");	// UserNumber 가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
		
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// IF_ETC_6
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_6")).trim())) {
					tempSearchData.put("srchCode", "IF-40");	// Host Name이 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
		
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				break;

			// AMS 해운수출				
			case CodeDefinition.KGYL_AMS_SEA_EXP:
				// IF_ETC_3
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_3")).trim())) {
					tempSearchData.put("srchCode", "MF-62");	// AccountNumber 가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Interface", ifVender + " : "+ message));
				}
				
				// IF_ETC_4
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_4")).trim())) {
					tempSearchData.put("srchCode", "MF-61");	// DepartmentNumber 가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Interface", ifVender + " : "+ message));
				}

				// IF_ETC_5
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_5")).trim())) {
					tempSearchData.put("srchCode", "MF-62");	// UserNumber 가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Interface", ifVender + " : "+ message));
				}
				
				// IF_ETC_6
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_6")).trim())) {
					tempSearchData.put("srchCode", "IF-40");	// Host Name이 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
		
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				break;

			// 중국발 항공수출 적하목록 - mCube
			case CodeDefinition.YUHAN_MCUBE_KMSPLUS_AIR_EXP_FHL4:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// WEBSERVICE
				if ("WEBSERVICE".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					// mCube ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID를 입력하지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}

					// mCube PW 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PW를 입력하지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);

						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				} 
				break;

			// 항공수출 파트너용 HAWB 송신(YUHAN/COMXBL) - mCube
			case CodeDefinition.YUHAN_MCUBE_AIR_EXP_XML:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
					
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// WEBSERVICE
				if ("WEBSERVICE".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					// mCube ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID를 입력하지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// mCube PW 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PW를 입력하지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				}
				break;
				
			// FNC S/R 송신(YUHAN/FNC) - mCube
			case CodeDefinition.YUHAN_FNC_SEA_EXP_XML:
				// 송신식별자 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SENDER_ID")).trim())) {
					tempSearchData.put("srchCode", "MF-63");	// 송신식별자 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}

				// 수신식별자 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_RECEIVER_ID")).trim())) {
					tempSearchData.put("srchCode", "MF-64");	// 수신식별자 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
					
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}

				// WEBSERVICE
				if ("WEBSERVICE".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					// mCube ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID를 입력하지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// mCube PW 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PW를 입력하지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				}
				break;

			// FNC 선사 SR 인터페이스 FNC/SR
			case CodeDefinition.FNC_LINE_SEA_EXP_TXT:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
					
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// FTP
				if ("FTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					
					// IP 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP")).trim())) {
						tempSearchData.put("srchCode", "MF-53");	// IP 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// PORT 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")).trim())) {
						tempSearchData.put("srchCode", "MF-54");	// PORT 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// PASS 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PASSWORD 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				}
				break;

			// CFOS KMSPLUS 인터페이스
			case CodeDefinition.CFOS_KMSPLUS_AIR_EXP_TXT:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
					
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// FTP
				if ("FTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					// IP 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP")).trim())) {
						tempSearchData.put("srchCode", "MF-53");	// IP 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// PORT 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")).trim())) {
						tempSearchData.put("srchCode", "MF-54");	// PORT 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// PASS 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PASSWORD 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				}
				break;

			// 글로비스 Tracing 전송
			case CodeDefinition.GLOVIS_TRC_AIR_EXP_TXT:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
					
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// FTP
				if ("FTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					// IP 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP")).trim())) {
						tempSearchData.put("srchCode", "MF-53");	// IP 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// PORT 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")).trim())) {
						tempSearchData.put("srchCode", "MF-54");	// PORT 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// PASS 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PASSWORD 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				}
				break;
				
			// 글로비스 Invoice 전송
			case CodeDefinition.GLOVIS_INVOICE_AR_SAS_TXT:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
					
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// FTP
				if ("FTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					// IP 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP")).trim())) {
						tempSearchData.put("srchCode", "MF-53");	// IP 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// PORT 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")).trim())) {
						tempSearchData.put("srchCode", "MF-54");	// PORT 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// PASS 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PASSWORD 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				}
				break;

			// 글로비스 항공수입 B/L 전송
			case CodeDefinition.GLOVIS_BL_AIR_IMP_TXT:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
					
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// FTP
				if ("FTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					// IP 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP")).trim())) {
						tempSearchData.put("srchCode", "MF-53");	// IP 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// PORT 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")).trim())) {
						tempSearchData.put("srchCode", "MF-54");	// PORT 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// PASS 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PASSWORD 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				}
				break;

			// 글로비스 해상수입 B/L 전송				
			case CodeDefinition.GLOVIS_BL_SEA_IMP_TXT:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
					
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// FTP
				if ("FTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					// IP 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP")).trim())) {
						tempSearchData.put("srchCode", "MF-53");	// IP 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// PORT 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")).trim())) {
						tempSearchData.put("srchCode", "MF-54");	// PORT 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// PASS 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PASSWORD 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				}
				break;
		
			// 금호석유화학 check bl 전송
			case CodeDefinition.KKPC_FNC_BL_SEA_EXP_TXT:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
					
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// FTP
				if ("FTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					// IP 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP")).trim())) {
						tempSearchData.put("srchCode", "MF-53");	// IP 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// PORT 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")).trim())) {
						tempSearchData.put("srchCode", "MF-54");	// PORT 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// PASS 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PASSWORD 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				}
				break;
		
			// 중국 동진선박 B/L 다운로드 추후 FTP로 송신가능성이 있어 미리 추가해논다				
			case CodeDefinition.DONGJIN_BL_SEA_TXT:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
					
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				break;
			
			// 중국 현대상선 Booking(예약) 인터페이스
			case CodeDefinition.HMM_SEA_BKG:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
					
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// FTP
				if ("FTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					
					// IP 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP")).trim())) {
						tempSearchData.put("srchCode", "MF-53");	// IP 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// PORT 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")).trim())) {
						tempSearchData.put("srchCode", "MF-54");	// PORT 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// PASS 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PASSWORD 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				}
				break;
			
			// 중국 현대상선 S/R(확정) 인터페이스
			case CodeDefinition.HMM_SEA_SR:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
					
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// FTP
				if ("FTP".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					// IP 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP")).trim())) {
						tempSearchData.put("srchCode", "MF-53");	// IP 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// PORT 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")).trim())) {
						tempSearchData.put("srchCode", "MF-54");	// PORT 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// ID 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_ID")).trim())) {
						tempSearchData.put("srchCode", "MF-55");	// ID 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
					
					// PASS 체크
					if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SVR_PW")).trim())) {
						tempSearchData.put("srchCode", "MF-56");	// PASSWORD 정보가 입력되지 않았습니다.
						message = this.getErrorCodeName(tempSearchData);
						
						errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
					}
				}
				break;
			
			// 중국 동진선박 B/L 다운로드 추후 FTP로 송신가능성이 있어 미리 추가해논다
			case CodeDefinition.DONGJIN_FR_SEA_TXT:
				// INTERFACE TYPE 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")).trim())) {
					tempSearchData.put("srchCode", "MF-57");	// INTERFACE TYPE 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);
					
					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// 송신식별자 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_SENDER_ID")).trim())) {
					tempSearchData.put("srchCode", "MF-63");	// 송신식별자 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				break;

			// KCNET WEB-SERVICE FWB FHL
			case CodeDefinition.KCNET_WEBSERVICE_AIR_EXP_FHL:
			case CodeDefinition.KCNET_WEBSERVICE_AIR_EXP_FWB:
			case CodeDefinition.KCNET_WEBSERVICE_AIR_IMP_XML:
				// PIMA 체크
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_PIMA_CODE")).trim())) {
					tempSearchData.put("srchCode", "MF-01");	// 자사정보에 정보(FHL,FWB PIMA)가 없습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}

				// IF_ETC_1
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_1")).trim())) {
					tempSearchData.put("srchCode", "MF-61");	// IF_ETC_1 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}

				// IF_ETC_2
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_2")).trim())) {
					tempSearchData.put("srchCode", "MF-62");	// IF_ETC_2 정보가 입력되지 않았습니다.
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Office", ifVender + " : "+ message));
				}
				
				// IF_ETC_3
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_3")).trim())) {
					tempSearchData.put("srchCode", "MF-430");	// Webservice URL
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Interface", ifVender + " : "+ message));
				}
				
				// IF_ETC_4
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_4")).trim())) {
					tempSearchData.put("srchCode", "MF-431");	// KCNET ID
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Interface", ifVender + " : "+ message));
				}

				// IF_ETC_5
				if ("".equals(Util.null2str(ediHostSendInfo.get("IF_ETC_5")).trim())) {
					tempSearchData.put("srchCode", "MF-432");	// KCNET PW
					message = this.getErrorCodeName(tempSearchData);

					errList.add(this.getErrorMessage("Interface", ifVender + " : "+ message));
				}
				break;
				
			default:
				tempSearchData.put("srchCode", "MF-51");	// 현재 지원되지 않는 인터페이스 스펙(서비스코드) 입니다.
				message = this.getErrorCodeName(tempSearchData);

				errList.add(this.getErrorMessage("Office", ifVender + "(" + docType + ") : "+ message));
				break;
		}

		// 오류내역이 있을 경우 Exception 발생후 종료
		if (errList.size() > 0 && !"vali".equals(Util.null2str(docSend.get("action")))) {
			this.setValidateMessage(docSend, tempMblData, errList, result);

			return false;
		}

		return true;
	}
	
	public String getLocation(String str, Map<String, Object> docSend) throws Exception {
		Map<String, Object> tempSearchData = new HashMap<String, Object>();

		// 메시지 가져오기 조건 세팅
		tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
		tempSearchData.put("entCode", Util.null2str(docSend.get("entCode")));
		tempSearchData.put("groupCode", "EDI_BONDED_WAREHOUSE");
		tempSearchData.put("srchCode", str);
		tempSearchData.put("srchEtc2", Util.null2str(docSend.get("bound")));	//쿼리에 조회조건없음?
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(tempSearchData, "userLanguage|srchEtcField", false, null, null, null, null);
		Map<String, Object> tempCodeDetail = commonMapper.getCodeDetail(tempSearchData);
		String tempEtc1 = "";
		String strReturnValue = "";
		
		if(tempCodeDetail != null) {
			tempEtc1 = Util.null2str(tempCodeDetail.get("ETC1")).trim();
			
			if(tempEtc1.length() > 7) {
				tempEtc1 = tempEtc1.substring(tempEtc1.length() - 8, 8);
	        }
	        strReturnValue = this.getEdiNumericSelect(tempEtc1);
		}
		
		return strReturnValue;
	}
	
	/**
	 * M.B/L 오더정보 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getEdiMblOdInfo(Map<String, Object> map) throws Exception {
		return manifestMapper.getEdiMblOdInfo(map);
	}
	
	/**
	 * 포워더 송/수신 계정정보 쿼리
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getFwSendAccInfo(Map<String, Object> map) throws Exception {
		return manifestMapper.getFwSendAccInfo( map);
	}
	
	/**
	 * 적하목록 전송 오류검증 M.B/L 오더정보 조회
	 * 
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getEdiManifestValidatorMblOdInfo(Map<String, Object> map) throws Exception{
		String code = Util.null2str(map.get("ifServiceCode"));
//		// 적하목록 항공수출 KCNET(특송/포워더)
//		if (CodeDefinition.KCNET_MFCS_AIR_EXP_FHL.equals(code) || CodeDefinition.KCNET_WEBSERVICE_AIR_EXP_FHL.equals(code)) {
//			return manifestMapper.FORWARDER_AIR_EXP_ediManifestValidatorMblOdInfo_KCNET_MFCS_AIR_EXP_FHL(map);
//		// 적하목록 해운수출/수입 KTNET
//		}else if(CodeDefinition.KTNET_MFCS_SEA_EXP_TXT.equals(code) || CodeDefinition.KTNET_MFCS_SEA_EXP_TXT_SFTP.equals(code) || CodeDefinition.KLNET_PRISM_SEA_EXP_TXT.equals(code) || CodeDefinition.KTNET_MFCS_SEA_IMP_TXT.equals(code) || CodeDefinition.KTNET_MFCS_SEA_IMP_TXT_SFTP.equals(code) || CodeDefinition.KLNET_PRISM_SEA_IMP_TXT.equals(code)) {	//KLNET의 경우 KTNET에서 헤더만 바뀌므로 같이 사용 / 지서희
//			return manifestMapper.FORWARDER_SEA_ediManifestValidatorMblOdInfo_KTNET_MFCS_SEA_TXT(map);
//		// 적하목록 항공수출 KCNET(특송/포워더)
//		}else if(CodeDefinition.YUHAN_MCUBE_KMSPLUS_AIR_EXP_FHL4.equals(code)) {
//			return manifestMapper.FORWARDER_AIR_EXP_ediManifestValidatorMblOdInfo_KMSPLUS_AIR_EXP_FHL(map);
//		// 적하목록 항공수출 KTNET(FHL4)
//		}else if(CodeDefinition.KTNET_ACPS_AIR_EXP_FHL.equals(code)) {
//			return manifestMapper.FORWARDER_AIR_EXP_ediManifestValidatorMblOdInfo_KTNET_ACPS_AIR_EXP_FHL(map);
//		}else if(CodeDefinition.CFOS_KMSPLUS_AIR_EXP_TXT.equals(code)) {
//			return manifestMapper.FORWARDER_AIR_EXP_ediManifestValidatorMblOdInfo_CFOS_KMSPLUS_AIR_EXP_TXT(map);
//		// EAWB 항공수출 KTNET/FWB(ACPS)
//		}else if(CodeDefinition.KTNET_ACPS_AIR_EXP_FWB.equals(code) || CodeDefinition.KCNET_MFCS_AIR_EXP_FWB.equals(code) || CodeDefinition.KCNET_WEBSERVICE_AIR_EXP_FWB.equals(code)) {
//			return manifestMapper.FORWARDER_AIR_EXP_ediFwbValidatorMblOdInfo_KTNET_ACPS_AIR_EXP_FWB(map);
//		}else{
//			return null;
//		}
		/** 사용안함 */
		if(CodeDefinition.KTNET_ACPS_AIR_EXP_FHL.equals(code) || CodeDefinition.KCNET_MFCS_AIR_EXP_FHL.equals(code) || CodeDefinition.KTNET_ACPS_AIR_EXP_FHL.equals(code) || CodeDefinition.KCNET_MFCS_AIR_IMP_FHL.equals(code) || CodeDefinition.KTNET_MFCS_SEA_EXP_TXT_SFTP.equals(code) || CodeDefinition.KLNET_PRISM_SEA_EXP_TXT.equals(code) || CodeDefinition.KTNET_MFCS_SEA_IMP_TXT_SFTP.equals(code) || CodeDefinition.KLNET_PRISM_SEA_IMP_TXT.equals(code)){
			return manifestMapper.ediManifestValidatorMblOdInfo(map);
		}else{
			return null;
		}
	}
	
	/**
	 * 적하목록 전송 오류검증 H.B/L 오더정보 조회
	 * @param map
	 * @return
	 * @throws java.lang.Exception
	 */
	public List<Map<String, Object>> getEdiManifestValidatorHblOdList(Map<String, Object> map) throws java.lang.Exception {
		String code = Util.null2str(map.get("ifServiceCode"));
//		// 적하목록 항공수출 KCNET(특송/포워더) / 중국발 항공수출 MCUBE
//		if (CodeDefinition.KCNET_MFCS_AIR_EXP_FHL.equals(code) || CodeDefinition.KCNET_WEBSERVICE_AIR_EXP_FHL.equals(code) || CodeDefinition.YUHAN_MCUBE_KMSPLUS_AIR_EXP_FHL4.equals(code)) {
//			return manifestMapper.FORWARDER_AIR_EXP_ediManifestValidatorHblOdList_KCNET_MFCS_AIR_EXP_FHL(map);
//		// 적하목록 해운수출 KTNET
//		} else if (CodeDefinition.KTNET_MFCS_SEA_EXP_TXT.equals(code) || CodeDefinition.KTNET_MFCS_SEA_EXP_TXT_SFTP.equals(code) || CodeDefinition.KLNET_PRISM_SEA_EXP_TXT.equals(code)) {
//			return manifestMapper.FORWARDER_SEA_EXP_ediManifestValidatorHblOdList_KTNET_MFCS_SEA_EXP_TXT(map);
//		// 적하목록 해운수입 KTNET
//		} else if (CodeDefinition.KTNET_MFCS_SEA_IMP_TXT.equals(code) || CodeDefinition.KTNET_MFCS_SEA_IMP_TXT_SFTP.equals(code) || CodeDefinition.KLNET_PRISM_SEA_IMP_TXT.equals(code)) {//KLNET의 경우 KTNET에서 헤더만 바뀌므로 같이 사용 / 지서희
//			return manifestMapper.FORWARDER_SEA_IMP_ediManifestValidatorHblOdList_KTNET_MFCS_SEA_IMP_TXT(map);
//		// 적하목록 항공수출 KTNET(FHL4) 2016-03-30 HIT
//		} else if (CodeDefinition.KTNET_ACPS_AIR_EXP_FHL.equals(code)) {
//			return manifestMapper.FORWARDER_AIR_EXP_ediManifestValidatorHblOdList_KTNET_ACPS_AIR_EXP_FHL(map);
//		// KMSPLUS CFOS
//		} else if (CodeDefinition.CFOS_KMSPLUS_AIR_EXP_TXT.equals(code)) {
//			return manifestMapper.FORWARDER_AIR_EXP_ediManifestValidatorHblOdList_CFOS_KMSPLUS_AIR_EXP_TXT(map);
//		}else {
//			return null;
//		}
		
//		if(CodeDefinition.KTNET_ACPS_AIR_EXP_FHL.equals(code) || CodeDefinition.KCNET_MFCS_AIR_EXP_FHL.equals(code) || CodeDefinition.KTNET_ACPS_AIR_IMP_FHL.equals(code) || CodeDefinition.KCNET_MFCS_AIR_IMP_FHL.equals(code) || CodeDefinition.KTNET_MFCS_SEA_EXP_TXT_SFTP.equals(code) || CodeDefinition.KLNET_PRISM_SEA_EXP_TXT.equals(code) || CodeDefinition.KTNET_MFCS_SEA_IMP_TXT_SFTP.equals(code) || CodeDefinition.KLNET_PRISM_SEA_IMP_TXT.equals(code)){
			return manifestMapper.ediManifestValidatorHblOdInfo(map);
//		}else{
//			return null;
//		}
	}
	
	/**
	 * 적하목록 H.B/L 컨테이너 리스트 가져오기
	 * @param resJsonObject
	 * @return
	 * @throws java.lang.Exception
	 */
	public List<Map<String, Object>> getEdiManifestContainerList(Map<String, Object> map) throws java.lang.Exception {
		String code = Util.null2str(map.get("ifServiceCode"));
//		// 적하목록 해운수출 KTNET
//		if (CodeDefinition.KTNET_MFCS_SEA_EXP_TXT.equals(code) || CodeDefinition.KTNET_MFCS_SEA_EXP_TXT_SFTP.equals(code) || CodeDefinition.KLNET_PRISM_SEA_EXP_TXT.equals(code)) {//KLNET의 경우 KTNET에서 헤더만 바뀌므로 같이 사용 / 지서희
//			return manifestMapper.FORWARDER_SEA_EXP_ediManifestContainerList_KTNET_MFCS_SEA_EXP_TXT(map);
//		}
//		// 적하목록 해운수입 KTNET
//		else if(CodeDefinition.KTNET_MFCS_SEA_IMP_TXT.equals(code) || CodeDefinition.KTNET_MFCS_SEA_IMP_TXT_SFTP.equals(code) || CodeDefinition.KLNET_PRISM_SEA_IMP_TXT.equals(code)) {//KLNET의 경우 KTNET에서 헤더만 바뀌므로 같이 사용 / 지서희
//			return manifestMapper.FORWARDER_SEA_IMP_ediManifestContainerList_KTNET_MFCS_SEA_IMP_TXT(map);
//		// FNC 선사 SR
//		} else if(CodeDefinition.FNC_LINE_SEA_EXP_TXT.equals(code)) {
//			return manifestMapper.FORWARDER_SEA_EXP_ediManifestContainerList_FNC_LINE_SEA_EXP_TXT(map);
//		//현대상선 S/R 인터페이스 
//		} else if(CodeDefinition.HMM_SEA_BKG.equals(code) || CodeDefinition.HMM_SEA_SR.equals(code)){
//			return manifestMapper.FORWARDER_SEA_EXP_ediManifestContainerList_HDMM_SEA_SR(map);
//		}else{
//			return null;
//		}
		
//		if(CodeDefinition.KTNET_MFCS_SEA_EXP_TXT_SFTP.equals(code) || CodeDefinition.KLNET_PRISM_SEA_EXP_TXT.equals(code) || CodeDefinition.KTNET_MFCS_SEA_IMP_TXT_SFTP.equals(code) || CodeDefinition.KLNET_PRISM_SEA_IMP_TXT.equals(code)){
			return manifestMapper.ediManifestContainerList(map);
//		}else{
//			return null;
//		}
	}
	
	/**
	 * 코드값 조회 하기
	 * @param srchCode
	 * @param docSend
	 * @return
	 * @throws Exception
	 */
	public String getPiecesUnit(String srchCode, Map<String, Object> docSend) throws Exception {
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("entCode", Util.null2str(docSend.get("entCode")));
		tempSearchData.put("groupCode", "UNIT_PIECES");
		tempSearchData.put("srchCode", Util.null2str(srchCode));

		String strReturnValue = "";

		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(tempSearchData, "userLanguage|srchEtcField", false, null, null, null, null);
		Map<String, Object> codeDetail = commonMapper.getCodeDetail(tempSearchData);

		if (codeDetail != null)	strReturnValue = Util.null2str(codeDetail.get("ETC1"));

		if(strReturnValue.length() == 0)	strReturnValue = "GT";

		return strReturnValue;
	}
	
	/**
	 * 송신결과 로그기록(송신 원본파일 저장)
	 * @param resJsonObject
	 * @throws java.lang.Exception
	 */
	public void setInterfaceLog(Map<String, Object> docSend, Map<String, Object> ediHostSendInfo, String fileData, String ifInfo) throws java.lang.Exception {
		// 메시지 가져오기 조건 세팅
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
		tempSearchData.put("entCode", Util.null2str(docSend.get("entCode")));
		tempSearchData.put("groupCode", "EDI_DOCUMENT_CODE");
		tempSearchData.put("srchCode", Util.null2str(docSend.get("docType")));
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(docSend, "userLanguage|gmtTime", false, null, null, null, null);
		Map<String, Object> errorCodeMessage = commonMapper.getCodeDetail(tempSearchData);
		Map<String, Object> insertData = new HashMap<String, Object>();
		
		if (errorCodeMessage == null)	errorCodeMessage = new HashMap<String, Object>();
		
		insertData.put("entCode", docSend.get("entCode"));
		insertData.put("entBusinessCode", docSend.get("entBusinessCode"));
		insertData.put("entOfficeCode", docSend.get("entOfficeCode"));
		insertData.put("solution", docSend.get("solution"));
		insertData.put("bound", docSend.get("bound"));
		insertData.put("boundType", docSend.get("boundType"));
		insertData.put("ifSendDocYn", "Y");	// Y:Send/N:Recv 여부
		insertData.put("ifStatus", "");		// ※
		insertData.put("ifStatusInfo", "");	// ※
		insertData.put("ifServiceCode", Util.null2str(ediHostSendInfo.get("IF_SERVICE_CODE")));
		insertData.put("ifInfo", ifInfo);		// ※
		insertData.put("ifVender", Util.null2str(ediHostSendInfo.get("IF_VENDER")));
		insertData.put("ifSRType", Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE")));
		insertData.put("ifDocCode", Util.null2str(docSend.get("docType")));
		insertData.put("ifDocName", Util.null2str(errorCodeMessage.get("CODE_NAME"))); // GROUP_CODE : EDI_DOCUMENT_CODE
		insertData.put("ifDocCodeRef", "");		// ※
		insertData.put("ifSenderId", Util.null2str(ediHostSendInfo.get("IF_SENDER_ID")));
		insertData.put("ifSenderName", "");		// ※
		insertData.put("ifReceiverId", Util.null2str(ediHostSendInfo.get("IF_RECEIVER_ID")));
		insertData.put("ifReceiverName", "");	// ※
		insertData.put("ifMessageId", "");		// ※
		insertData.put("ifMessageName", "");	// ※
		insertData.put("ifMessageNo", "");		// ※
		insertData.put("ifMessageFunctionCode", "");
		insertData.put("ifDocReferenceNo", "");	// ※
		insertData.put("ifDate", Util.null2str(DateTimeUtil.getDate("yyyy-MM-dd")));
		insertData.put("ifDateTime", Util.null2str(DateTimeUtil.getDate("yyyy-MM-dd HH:mm")));
		insertData.put("ifMessageOriginal", fileData);
		insertData.put("ifMessageOriginalAck", "");
		insertData.put("ifMessageOriginalAckInfo", "");
		insertData.put("registerUserId", docSend.get("registerUserId"));
		insertData.put("updateUserId", docSend.get("updateUserId"));
//		insertData.put("format", docSend.get("format"));
//		insertData.put("dbFormat", docSend.get("dbFormat"));
		insertData.put("gmtTime", docSend.get("gmtTime"));
		
//		/** 20210811 pod저장부분에 사용하기위해 추가 */
//		docSend.put("ifDate", insertData.get("ifDate"));
//		docSend.put("ifTime", Util.null2str(insertData.get("ifDateTime")).substring(11));
		// 송신결과 로그기록
		commonMapper.insertInterfaceLog(insertData);
	}
	
	/**
	 * Manifest Status 저장
	 * @param map
	 * @return
	 * @throws java.lang.Exception
	 */
	public void setMfStatus(Map<String, Object> map) throws java.lang.Exception {
		Map<String, Object> statusDetail = new HashMap<String, Object>();
		statusDetail.put("entCode", Util.null2str(map.get("entCode")));
		statusDetail.put("entBusinessCode", Util.null2str(map.get("entBusinessCode")));
		statusDetail.put("odJobNo", Util.null2str(map.get("odJobNo")));
		statusDetail.put("gmtTime", Util.null2str(map.get("gmtTime")));
//		statusDetail.put("format", Util.null2str(map.get("format")));
		statusDetail.put("srchAutoCode", "AUTO_EDI_SEND_DATE");
		statusDetail.put("autoCode", "AUTO_EDI_SEND_DATE");
		statusDetail.put("inputDate", Util.null2str(map.get("ifDate")));
		statusDetail.put("inputTime", Util.null2str(map.get("ifTime")));
		//전자상거래에서 ETC9 사용유무 확인
//		this.updateOdStatusByGroup(statusDetail);
	}
	
	/**
	 * 오더 EDI 상태값 업데이트
	 * 
	 * @param docSend
	 * @param tempMblData
	 * @throws Exception
	 */
	public void setOdEdiStatus(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempHblDataList) throws Exception {
		Map<String, Object> updataMap = new HashMap<String, Object>();
		updataMap.put("gmtTime", Util.null2str(docSend.get("gmtTime")));
		updataMap.put("registerUserId", Util.null2str(docSend.get("registerUserId")));
		updataMap.put("odJobNo", Util.null2str(tempMblData.get("OD_JOB_NO")));
		updataMap.put("houseOdJobNoList", docSend.get("houseOdJobNoList"));
		updataMap.put("lastPodStatus", "ECM100");
		updataMap.put("cdType", Util.null2str(docSend.get("cdType")));
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(updataMap, "gmtTime", false, null, null, null, null);
		manifestMapper.updateOdForEdiStatus(updataMap);
		
		updataMap.clear();
		updataMap.put("userLanguage", docSend.get("userLanguage"));
		updataMap.put("srchGroupCode", "STATUS_ORDER_CARGO");
		updataMap.put("srchCode", "ECM100");
		updataMap.put("entCode", docSend.get("entCode"));
		updataMap.put("entBusinessCode", "");
		updataMap.put("gmtTime", docSend.get("gmtTime"));
		updataMap.put("solution", "ECM");
		updataMap.put("updateUserId", docSend.get("updateUserId"));
		updataMap.put("stockType", "POD");
		updataMap.put("stockSign", "POD");
		updataMap.put("stockSeqLength", 8);
		updataMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		updataMap.put("stockRemark", "ECM POD JOB NO");
		updataMap.put("podStatus", "ECM100");
		updataMap.put("odJobNo", tempMblData.get("OD_JOB_NO"));
		updataMap.put("blNo", tempMblData.get("MBL_NO"));
		updataMap.put("podTableName", 	"TB_ECM_POD_02");
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(updataMap, "podTableName|gmtTime", false, null, null, null, null);
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			
			int seq = 1;
			if(tempMblData.get("ECM100_POD_JOB_NO") == null){
				updataMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				orderBatchMapper.insertOrderPod(updataMap);
			}
			
			int cnt = 1;
			List<Map<String, Object>> updatePodList = new ArrayList<Map<String, Object>>();
			for(Map<String, Object> tempHblData : tempHblDataList){
				if(tempHblData.get("ECM100_POD_JOB_NO") != null){
					updatePodList.add(tempHblData);
				}else{
					updataMap.put("odJobNo", tempHblData.get("OD_JOB_NO"));
					updataMap.put("blNo", tempHblData.get("HBL_NO"));
					updataMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
					orderBatchMapper.insertOrderPod(updataMap);
				}
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			cnt = 1;
			for(Map<String, Object> tempHblData : updatePodList){
				updataMap.put("podJobNo", tempHblData.get("ECM100_POD_JOB_NO"));
				orderBatchMapper.updateOrderPod(updataMap);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
		
	}
	
	/**
	 * 파일복사
	 * 
	 * @param inFileName		- 입력 파일명
	 * @param outFileName		- 출력 파일명
	 * @param inOutFilePath		- inOutFilePath[2]:원본경로, inOutFilePath[3]:temp경로
	 * @throws java.lang.Exception
	 */
	public String moveFile(String inFileName, String outFileName, String inOutFilePath[], boolean addDateFormat) throws java.lang.Exception {
		File outFile = null;
		File folder = null;
		File outFolder = null;
		FileWriter fw = null;

		BufferedReader br = null;
		BufferedWriter bw = null;
		
		try {
			folder = new File(inOutFilePath[2]);
			outFolder = new File(inOutFilePath[3]);

			// 폴더 존재 유무 체크
			if (!folder.isDirectory())		folder.mkdirs();
			if(!outFolder.isDirectory())	outFolder.mkdirs();
			
			if(addDateFormat){
				SimpleDateFormat dateTimeForm = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				outFileName = dateTimeForm.format(new Date(System.currentTimeMillis())) + "_" + outFileName;	// 폴더 + 현재일시 + 파일명
			}
			
			outFile   = new File(inOutFilePath[3] + outFileName);

			br = new BufferedReader(new InputStreamReader(new FileInputStream(inOutFilePath[2] + inFileName), "euc-kr"));

			// BufferedWriter 객체 생성
			fw = new FileWriter(outFile, false);
			bw = new BufferedWriter(fw);

			String s = null;

			// 한줄씩 읽어서 BufferedRaeder에 저장한다.
			while ((s=br.readLine())!=null) {
				// 읽은 데이터(한줄)을 BufferedWriter에 쓴다.
				// 한줄씩 읽으므로, newLine() 메소드로 줄바꿈을 해준다.
				bw.write(s);
				bw.newLine();
			}
			if (br != null) try{ br.close(); }catch(IOException e){log.error(e.getMessage());}
			new File(inOutFilePath[2] + inFileName).delete();
			
		} catch(Exception e) {
			throw e;
		} finally {
			// BufferedWriter FileWriter를 닫아준다.
			if (bw != null) try{ bw.close(); }catch(IOException e){}
			if (fw != null) try{ fw.close(); }catch(IOException e){log.error(e.getMessage());}

			// BufferedReader FileReader를 닫아준다.
			if (br != null) try{ br.close(); }catch(IOException e){log.error(e.getMessage());}
		}
		return outFileName;
	}
	public String moveFile(String inFileName, String outFileName, String inOutFilePath[]) throws java.lang.Exception {
		return moveFile(inFileName, outFileName, inOutFilePath, true);
	}
	
	/**
	 * FTP 파일 전송
	 * 
	 * @param docSend
	 * @param tempMblData
	 * @param fileCreateRetVal
	 * @param ediHostSendInfo
	 * @param errList
	 * @param errorMessage
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public boolean ftpFileSend(Map<String, Object> docSend, Map<String, Object> tempMblData, String fileCreateRetVal[], Map<String, Object> ediHostSendInfo, List<Map<String, Object>> errList, Map<String, Object> result) throws Exception {

		String message = null;
		String messageCode = null;

		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("entCode", Util.null2str(docSend.get("entCode")));
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));

		FTPConnector ftpConnector = null;
		SFTPConnector sftpConnector = null;
		String localFilePath = null;
		String fileName = null;

		String ifSvrIp = null;
		String ifSvrIpPort = null;
		String ifSvrIpPassiveYn = null;
		String ifSvrId = null;
		String ifSvrPw = null;
		String ifSvrRoot = null;
		String ifSvrRootSend = null;
		String ifSvrRootRecv = null;
		String ifSRType = null;

		String ifVender = null;
		String ifServiceCode = null;

		// 중개사업자
		ifVender = Util.null2str(tempMblData.get("EDI_VENDER")).trim();
		
		// 서비스코드
		ifServiceCode = this.getServiceCode(Util.null2str(docSend.get("solution")), Util.null2str(docSend.get("bound")), Util.null2str(docSend.get("boundType")), Util.null2str(docSend.get("docType")), ifVender);
		docSend.put("ifServiceCode", ifServiceCode);

		// 중개사업자별 문서 및 송/수신 정보
		ifSvrIp = Util.null2str(ediHostSendInfo.get("IF_SVR_IP"));
		ifSvrIpPort = Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT"));
		ifSvrIpPassiveYn = Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PASSIVE_YN"));
		ifSvrId = Util.null2str(ediHostSendInfo.get("IF_SVR_ID"));
		ifSvrPw = Util.null2str(ediHostSendInfo.get("IF_SVR_PW"));
		ifSvrRoot = Util.null2str(ediHostSendInfo.get("IF_SVR_ROOT"));
		ifSvrRootSend = Util.null2str(ediHostSendInfo.get("IF_SVR_ROOT_SEND"));
		ifSvrRootRecv = Util.null2str(ediHostSendInfo.get("IF_SVR_ROOT_RECV"));
		ifSRType = Util.null2str(ediHostSendInfo.get("IF_S_R_TYPE"));

		//////////////////////////////////////////
		// FTP 접속 및 파일 전송
		//////////////////////////////////////////
		// 함수로 변경 되야 함.
		if (ifSvrIp.indexOf("//", 0) != -1) {
			ifSvrIp = ifSvrIp.substring(ifSvrIp.indexOf("//", 0) + 2, ifSvrIp.length());
		}
		boolean isFileSend = false;
		try {
			// 함수로 변경 되야 함. 
			ftpConnector = new FTPConnector();
			sftpConnector = new SFTPConnector();
			
			if ("KTNET".equals(ifVender)) {
				if ("SFTP".equals(ifSRType)){
					messageCode = sftpConnector.connectSFTPForKTNET(Util.null2int(ifSvrIpPort), ifSvrIp, ifSvrId, ifSvrPw, "SA32");
				} else {
					messageCode = ftpConnector.connectFTPForKTNET(Util.null2int(ifSvrIpPort), ifSvrIp, ifSvrId, ifSvrPw, ifSvrIpPassiveYn, "FL32");
				}
			} else {
				if ("SFTP".equals(ifSRType)){
					messageCode = sftpConnector.connectSFTP(Util.null2int(ifSvrIpPort), ifSvrIp, ifSvrId, ifSvrPw);
				}else {
					messageCode = ftpConnector.connectFTPForEDI(Util.null2int(ifSvrIpPort), ifSvrIp, ifSvrId, ifSvrPw, ifSvrIpPassiveYn);
				}
			}
	
			if (messageCode.trim().length() > 0) {
				tempSearchData.put("srchCode", messageCode);
				message = this.getErrorCodeName(tempSearchData);
	
				errList.add(this.getErrorMessage("시스템정보", ifVender + " " + message));
	
				this.setValidateMessage(docSend, tempMblData, errList, result);
	
				return false;
			}
	
			fileName	= fileCreateRetVal[0];		// 전송 파일명
			localFilePath = fileCreateRetVal[2];	// 전송 로컬 파일경로
	
			String tempRootPath[] = null;
			String tempSendReceivePath[] = null;
			String serverRootPath = ifSvrRoot;
			String serverSendReceivePath = ifSvrRootSend;
	
			if (!"".equals(Util.null2str(serverRootPath))) {
				tempRootPath = serverRootPath.split("/");
				serverRootPath = "";
	
				for (int i = 0; i < tempRootPath.length; ++i) {
					if(!"".equals(tempRootPath[i])) {
						serverRootPath = serverRootPath + "/" + tempRootPath[i];
					}
				}
			}
			// Ex). [A/B/C => /A/B/C], [A/B/C/ => /A/B/C], [/A/B/C/ => /A/B/C]
			if (!"".equals(Util.null2str(serverSendReceivePath))) {
				tempSendReceivePath = serverSendReceivePath.split("/");
				serverSendReceivePath = "";
	
				for (int i = 0; i < tempSendReceivePath.length; ++i) {
					if (!"".equals(tempSendReceivePath[i])) {
						serverSendReceivePath = serverSendReceivePath + "/" + tempSendReceivePath[i];
					}
				}
			}
	
			///////////////////////////////////////////
			// 생성파일 FTP(웹서비스 등) 송신
			///////////////////////////////////////////
			if (!"SFTP".equals(ifSRType)) {
				if ("KTNET".equals(ifVender)) {
					if (ftpConnector.ftpFileSendForKTnet(localFilePath
							, fileName.substring(fileName.indexOf(".", 0) + 1, fileName.length())	// Ex). 201309-001.upload.XXX.txt -> upload.XXX.txt
							, fileName
							, serverRootPath, serverSendReceivePath)) {
		
						isFileSend = true;
					} else {
						isFileSend = false;
					}
					
				/** FNC, CFOS사용하지않음 */
//				} else if ("FNC".equals(ifVender) || "CFOS".equals(ifVender)) {
//					if (ftpConnector.ftpFileSendForFnc(localFilePath
//							, fileName.substring(fileName.indexOf(".", 0) + 1, fileName.length())	// Ex). 201309-001.upload.XXX.txt -> upload.XXX.txt
//							, fileName
//							, serverRootPath, serverSendReceivePath)) {
//						isFileSend = true;
//					} else {
//						isFileSend = false;
//					}
					
				} else {
					if (ftpConnector.ftpFileSendForEDI(localFilePath
							, fileName.substring(fileName.indexOf(".", 0) + 1, fileName.length())	// Ex). 201309-001.upload.XXX.txt -> upload.XXX.txt
							, fileName
							, serverRootPath, serverSendReceivePath)) {
						isFileSend = true;
					} else {
						isFileSend = false;
					}
				}
				ftpConnector.disconnectFTP();
				
			} else {
				if("KTNET".equals(ifVender)){
					if (sftpConnector.sftpFileSendForKTnet(localFilePath
							, fileName.substring(fileName.indexOf(".", 0) + 1, fileName.length())	// Ex). 201309-001.upload.XXX.txt -> upload.XXX.txt
							, fileName
							, serverRootPath, serverSendReceivePath)) {
			
						isFileSend = true;
					} else {
						isFileSend = false;
					}
				}else{
					if (sftpConnector.sftpFileSendForEDI(localFilePath
							, fileName.substring(fileName.indexOf(".", 0) + 1, fileName.length())	// Ex). 201309-001.upload.XXX.txt -> upload.XXX.txt
							, fileName
							, serverRootPath, serverSendReceivePath)) {
			
						isFileSend = true;
					} else {
						isFileSend = false;
					}
				}
				
			}
		}catch(Exception e){
			throw e;
		}finally{
			if (ftpConnector != null && ftpConnector.getFtpInfo() != null && ftpConnector.getFtpInfo().isConnected()) {
				ftpConnector.disconnectFTP();
			}
			if (sftpConnector != null && sftpConnector.getConn() != null) {
				sftpConnector.disConnectSFTPForKTNET();
			}
		}
		
		if (!isFileSend) {
			tempSearchData.put("srchCode", "");	// ※FTP 파일 전송 실패
			tempSearchData.put("message", "FTP 파일 전송 실패");
			message = this.getErrorCodeName(tempSearchData);

			errList.add(this.getErrorMessage("시스템정보", ifVender + " " + message));

			this.setValidateMessage(docSend, tempMblData, errList, result);

			return false;
		}

		return true;
	}
	
	/**
	 * FTP 파일 전송
	 * 
	 * @param docSend
	 * @param tempMblData
	 * @param fileCreateRetVal
	 * @param ediHostSendInfo
	 * @param errList
	 * @param errorMessage
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public boolean ftpFileSendGw(Map<String, Object> docSend, Map<String, Object> masterOrder, String fileCreateRetVal[], Map<String, Object> kctdiInfo, List<String> errList) throws Exception {

		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("entCode", Util.null2str(docSend.get("entCode")));
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
		
		String message = null;
		String messageCode = null;

		FTPConnector ftpConnector = null;
		String localFilePath = null;
		String fileName = null;

		String ifSvrIp = null;
		String ifSvrIpPort = null;
		String ifSvrIpPassiveYn = null;
		String ifSvrId = null;
		String ifSvrPw = null;
		String ifSvrRoot = null;
		String ifSvrRootSend = "";

		// 중개사업자별 문서 및 송/수신 정보
		ifSvrIp = Util.null2str(kctdiInfo.get("EDI_FTP_IP"));
//		ifSvrIpPort = Util.null2str(kctdiInfo.get("EDI_FTP_PORT"), "21");
		
		ifSvrRoot = Util.null2str(kctdiInfo.get("EDI_FTP_ROOT")); //송신경로
		
		
		/** RUDDK 해상수입 인천항 전송시 패시브모드가 아닐경우 전송실패해서 해상이어도 패시브모드로 전송되도록 수정 */
//		if(Util.null2str(masterOrder.get("BOUND")).equals("SEA")){
//			ifSvrIpPassiveYn = "N";
//		}else{
//			ifSvrIpPassiveYn = "Y";
//			/** 포트구분없이 날짜폴더사용안함 */
////			/** 항공은 인천공항만 사용   인천공항은 설정경로('IN')/날짜(yyyyMMdd)폴더에 전송 */
////			ifSvrRootSend = DateTime.getDate("yyyyMMdd");
//		}
		ifSvrIpPassiveYn = "Y";
		
		
		
		if(Util.null2str(docSend.get("custom")).equals("Y") && Util.null2str(docSend.get("mode")).equals("active")){
			ifSvrIpPassiveYn = "N";
		}
		
		
		
		ifSvrId = Util.null2str(kctdiInfo.get("EDI_FTP_ID"));
		ifSvrPw = Util.null2str(kctdiInfo.get("EDI_FTP_PW"));
		
		//ifSvrRootSend = Util.null2str(kctdiInfo.get("EDI_FTP_ROOT_RECV")); //수신경로 
		

		//////////////////////////////////////////
		// FTP 접속 및 파일 전송 >> /SEND/yyyyMMdd 경로에 파일 전송
		//////////////////////////////////////////
		// 함수로 변경 되야 함.
		if (ifSvrIp.indexOf("//", 0) != -1) {
			ifSvrIp = ifSvrIp.substring(ifSvrIp.indexOf("//", 0) + 2, ifSvrIp.length());
		}
		
		/** EDI_FTP_IP에 구분자 ','로 포트 입력가능 */
		int portIdx = ifSvrIp.indexOf(",");
		if(portIdx != -1){
			ifSvrIpPort = ifSvrIp.substring(portIdx + 1).trim();
			ifSvrIp = ifSvrIp.substring(0, portIdx).trim();
		}
		if(Util.null2str(ifSvrIpPort).equals("")){
			ifSvrIpPort = "21";
		}
		
		boolean isFileSend = false;
		try {
			// 함수로 변경 되야 함. 
			ftpConnector = new FTPConnector();
			messageCode = ftpConnector.connectFTPForEDI(Util.null2int(ifSvrIpPort), ifSvrIp, ifSvrId, ifSvrPw, ifSvrIpPassiveYn, docSend);
	
			if (messageCode.trim().length() > 0) {
				tempSearchData.put("srchCode", messageCode);
				message = this.getErrorCodeName(tempSearchData);
				errList.add("M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + " " + message + "<br />");
				return false;
			}
	
			fileName	= fileCreateRetVal[0];		// 전송 파일명
			localFilePath = fileCreateRetVal[2];	// 전송 로컬 파일경로
	
			String tempRootPath[] = null;
			String tempSendReceivePath[] = null;
			String serverRootPath = ifSvrRoot;
			String serverSendReceivePath = ifSvrRootSend;
	
			if (!"".equals(Util.null2str(serverRootPath))) {
				tempRootPath = serverRootPath.split("/");
				serverRootPath = "";
	
				for (int i = 0; i < tempRootPath.length; ++i) {
					if(!"".equals(tempRootPath[i])) {
						serverRootPath = serverRootPath + "/" + tempRootPath[i];
					}
				}
			}
			// Ex). [A/B/C => /A/B/C], [A/B/C/ => /A/B/C], [/A/B/C/ => /A/B/C]
			if (!"".equals(Util.null2str(serverSendReceivePath))) {
				tempSendReceivePath = serverSendReceivePath.split("/");
				serverSendReceivePath = "";
	
				for (int i = 0; i < tempSendReceivePath.length; ++i) {
					if (!"".equals(tempSendReceivePath[i])) {
						serverSendReceivePath = serverSendReceivePath + "/" + tempSendReceivePath[i];
					}
				}
			}
	
			///////////////////////////////////////////
			// 생성파일 FTP(웹서비스 등) 송신
			///////////////////////////////////////////
			if (ftpConnector.ftpFileSendForEDI(localFilePath, fileName, fileName, serverRootPath, serverSendReceivePath, docSend)) {
				isFileSend = true;
			} else {
				isFileSend = false;
			}
			ftpConnector.disconnectFTP();
				
		}catch(Exception e){
			throw e;
		}finally{
			if (ftpConnector != null && ftpConnector.getFtpInfo() != null && ftpConnector.getFtpInfo().isConnected()) {
				ftpConnector.disconnectFTP();
			}
		}
		
		if (!isFileSend) {
			tempSearchData.put("srchCode", "");	// ※FTP 파일 전송 실패
			tempSearchData.put("message", "FTP 파일 전송 실패");
			message = this.getErrorCodeName(tempSearchData);
			errList.add("M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + " FTP 파일 전송 실패<br />");
			return false;
		}

		return true;
	}
	
	/**
	 * [AS-IS : GetBusinessID] 
	 * 
	 * @param strResidentNo
	 * @return
	 */
	public String getBusinessID(String strResidentNo) {
		try {
            String strBirthDay = "";
            
            if(getSocialSecurityNoCheck(strResidentNo)) {
                switch (strResidentNo.replace("-", "").replace(" ", "").substring(6, 7)) {
                    case "1":
                    case "2":
                    case "5":
                    case "6":
                        strBirthDay = "19" + strResidentNo.substring(0, 6);
                        break;
                    case "3":
                    case "4":
                    case "7":
                    case "8":
                        strBirthDay = "20" + strResidentNo.substring(0, 6);
                        break;
                }
            } else {
                strBirthDay = strResidentNo;
            }

            return strBirthDay;
        } catch(Exception e) {
            return strResidentNo;
        }
	}
	
	/**
	 * 우편번호 유효성 체크
	 *  
	 * @param countryCode
	 * @param zipCode
	 * @return
	 * @throws Exception
	 */
	public boolean checkUserZipCodeDigit(String countryCode, String zipCode, String serviceCode) throws Exception {
		// EAWB 항공수출 KTNET/FWB(ACPS)
		if(CodeDefinition.KTNET_ACPS_AIR_EXP_FWB.equals(Util.null2str(serviceCode).trim())) {
			
		}else if(CodeDefinition.KCNET_MFCS_AIR_EXP_FWB.equals(Util.null2str(serviceCode).trim())) {
			if("US".equals(Util.null2str(countryCode).trim())){
				// 미국 ZipCode - 5number(12345), 5-4number(12345-1234), 9(123456789)
				// Pattern.compile("\\d{5}(([ \\-])?\\d{4})?"); -- 12345 6789 스페이스 허용시
				Pattern pattern  =  Pattern.compile("\\d{5}(([\\-])?\\d{4})?");
				if(!pattern.matcher(zipCode).matches()){
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * 숫자 포멧 형식 지정
	 * 
	 * @param number
	 * @param format
	 * @param newScale
	 * @return
	 */
	public String getNumberFormat(String number, String format, int newScale) {
		NumberFormat numberFormat = new DecimalFormat(format);
		
		// 소수점 자동 반올림 때문에 BigDecimal.ROUND_DOWN 지정 해야됨.
		return numberFormat.format(new BigDecimal(Util.null2str(number, "0")).setScale(newScale, BigDecimal.ROUND_DOWN));
	}
	
	/**
	 * 주코드 체크
	 * 
	 * @param countryCode
	 * @param stateCode
	 * @return
	 * @throws Exception
	 */
	public boolean checkUserStateCode(String countryCode, String stateCode, String serviceCode) throws Exception {
		String tempStateCode = null;
		
		if(Util.null2str(stateCode).contains("-")) {
			tempStateCode = stateCode.substring(stateCode.indexOf("-", 0) + 1, stateCode.length());
		} else {
			tempStateCode = stateCode;
		}
		
		// EAWB 항공수출 KTNET/FWB(ACPS)
		if(CodeDefinition.KTNET_ACPS_AIR_EXP_FWB.equals(Util.null2str(serviceCode).trim())) {
			//CNE의 국가 코드가 US,CA,브라질(BR)과 멕시코(MX) 인 경우 주 코드(State Code) 2자리 필수
			if("US".equals(Util.null2str(countryCode).trim()) || "CA".equals(Util.null2str(countryCode).trim())
					|| "BR".equals(Util.null2str(countryCode).trim()) || "MX".equals(Util.null2str(countryCode).trim())) {
				if("".equals(Util.null2str(tempStateCode).trim())) {
					return false;
		        }
	        }
		} else {
			// State : CNE의 Country Code 가 US, CA 인 경우 필수
			if("US".equals(Util.null2str(countryCode).trim())
					|| "CA".equals(Util.null2str(countryCode).trim())) {
				if(Util.null2str(tempStateCode).trim().length() != 2) {
					return false;
		        }
	        }
		}
		
		return true;
	}
	
	/**
	 * 주민번호체크 - [AS-IS : SocialSecurityNo_Check]
	 * 
	 * @param s
	 * @return
	 */
    public boolean getSocialSecurityNoCheck(String s) {
        String []tempStrArray = {"5", "6", "7", "8"};
    	String sChkDigit;
        int nHap;
        int nChk;
        int nLoop;

        if("".equals(Util.null2str(s))) {
        	return true;
        }

        if(s.charAt(0) == 'F') {
        	return true;
        }

        if(getStrcmp(s.substring(6, 7), tempStrArray)) {
        	return true;
        }
        
        sChkDigit = "234567892345";
        nHap = 0;
        for(nLoop = 0; nLoop < 12; nLoop++) {
        	// compareTo :::: -1은 작다, 0은 같다, 1은 크다
        	if(Character.toString(s.charAt(nLoop)).compareTo("9") > 0
        			|| Character.toString(s.charAt(nLoop)).compareTo("0") < 0) {
        		return false;
        	}
        	nChk = Util.null2int(Character.toString(sChkDigit.charAt(nLoop))) * Util.null2int(Character.toString(s.charAt(nLoop)));
            nHap += nChk;
        }
        
        nHap = nHap % 11;
        
        if (nHap == 1) {
        	nHap = 11;
        }
        if (nHap == 0) {
        	nHap = 10;
        }
        
        if(Util.null2int(Character.toString(s.charAt(12))) == (11 - nHap)) {
        	return true;
        } else {
        	return false;
        }
    }
    
	/**
	 * 문자열을 주어진 문자열들과 비교하여 같은 문자열이 있으면 true - [AS-IS : Strcmp]
	 * 
	 * @param sComp
	 * @param string
	 * @return
	 */
    public boolean getStrcmp(String sComp, String[] sChar) {
        for (int nLoopI = 0; nLoopI < sChar.length; nLoopI++) {
            if (sComp.equals(sChar[nLoopI])) {
            	return true;
            }
        }
        return false;
    }
    
	/**
	 * 품명, 상호명, 주소 데이터에서 적하목록에서 허용하지 않는 특수문자 및 2Byte 문자를 Space로 변환한다. - [AS-IS : GetEdiCharacter]
	 * 
	 * @param str
	 * @return
	 */
	public String getEdiCharacter(String str) {
		str = str.replaceAll("[^a-zA-Z0-9.-.,-,---]", " ");
		
		return str.replace("\r", "").replaceAll("\n", "");
	}
	
	/**
	 * 적하목록관리번호(MRN)의 Check Digit - [AS-IS : VerifyMRNCheckDigitOK]
	 * 
	 * @param str
	 * @return
	 */
	@SuppressWarnings("unlikely-arg-type")
	public boolean verifyMRNCheckDigitOK(String sMRN) {
		String chkDigit = "379379379379379";
		int rtnSum = 0;
		String chkMRN = "";
		int chkMRN2 = 0;

		if (sMRN.length() < 2) {
			return false;
		}

		for (int i = 0; i < 10 ; i++) {
			chkMRN =  Util.null2str(Character.toString(sMRN.charAt(i)));
			chkMRN2 = (int)(chkMRN.charAt(chkMRN.length() - 1)) * (int)(chkDigit.charAt(i));
			
			rtnSum += Integer.toString(chkMRN2).charAt(Integer.toString(chkMRN2).length() - 1); 
		}
		
		if (Character.toString(sMRN.charAt(sMRN.length() - 1)).equals(Integer.toString(rtnSum).charAt(Integer.toString(rtnSum).length() - 1)) ) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * 면장번호 Digit 체크 - [AS-IS : VerifyPermitCheckDigitOK]
	 * 
	 * @param str
	 * @return
	 */
	public boolean getVerifyPermitCheckDigitOK(String strLicenseNo) {
		return this.getVerifyLicenseCheckDigitOK(strLicenseNo);
	}
	
	/**
	 * 면장번호 Digit 체크 - [AS-IS : VerifyLicenseCheckDigitOK]
	 * 
	 * @param str
	 * @return
	 */
	public boolean getVerifyLicenseCheckDigitOK(String strLicenseNo) {
		String chkDigit = "731731731731731731731";

        int rtnSum = 0;
        int nChkLicenseNo = 0;

        int nRusultSum = 0;
        String strRusultSum = "";
        String strAscLicenseNo = "";

        if (strLicenseNo.trim().length() < 2) {
        	return false;
        }
        
        strAscLicenseNo = this.getValLicenseNo(strLicenseNo);

        for(int i = 0; i < strLicenseNo.trim().length() - 1; ++i) {
            nChkLicenseNo = Integer.parseInt(strAscLicenseNo.trim().substring(i, i+1)) * Integer.parseInt(chkDigit.substring(i, i+1));
            rtnSum += nChkLicenseNo;
        }

        nRusultSum = 10 - Integer.parseInt(Integer.toString(rtnSum).substring(Integer.toString(rtnSum).length() - 1, Integer.toString(rtnSum).length()));
        strRusultSum = Integer.toString(nRusultSum).substring(Integer.toString(nRusultSum).length() - 1, Integer.toString(nRusultSum).length());

        if(!strRusultSum.equals(strLicenseNo.substring(strLicenseNo.trim().length() - 1, strLicenseNo.trim().length()))) {
        	return false;
        } else {
            return true;
        }
	}
	
	/**
	 * 면장번호 체크 - [AS-IS : getValLicenseNo]
	 * 
	 * @param strLicenseNo
	 * @return
	 */
	public String getValLicenseNo(String strLicenseNo) {
        StringBuffer strReturnValue = new StringBuffer();

        for (int i = 0; i < strLicenseNo.trim().length() - 1; i++) {
            if (strLicenseNo.trim().charAt(i) != '0' && strLicenseNo.trim().charAt(i) != '1' &&
                strLicenseNo.trim().charAt(i) != '2' && strLicenseNo.trim().charAt(i) != '3' &&
                strLicenseNo.trim().charAt(i) != '4' && strLicenseNo.trim().charAt(i) != '5' &&
                strLicenseNo.trim().charAt(i) != '6' && strLicenseNo.trim().charAt(i) != '7' &&
                strLicenseNo.trim().charAt(i) != '8' && strLicenseNo.trim().charAt(i) != '9')
            {
            	strReturnValue.append("0");
            } else {
            	strReturnValue.append(strLicenseNo.trim().charAt(i));
            }
        }
        
        return strReturnValue.toString();
	}
	
	/**
	 * 변환문서별 EDI특수문자 제거 - [AS-IS : get_EdiCharacterChk]
	 * 정정신고용 특수문자제거 2018.11.22 지서희
	 * 
	 * @param strData
	 * @return
	 */
	public String getEdiModifyCharacterChk(String strData) {
		StringBuffer strReturnValue = new StringBuffer();

        for(int i = 0; i < strData.length(); i++) {
            if ((int)strData.charAt(i) == 123 || (int)strData.charAt(i) == 125 || (int)strData.charAt(i) == 39 || (int)strData.charAt(i) == 63 || (int)strData.charAt(i) == 91 || (int)strData.charAt(i) == 93
                || (int)strData.charAt(i) == 126 || (int)strData.charAt(i) == 96 || (int)strData.charAt(i) == 64 || (int)strData.charAt(i) == 35 || (int)strData.charAt(i) == 36 || (int)strData.charAt(i) == 94 || (int)strData.charAt(i) == 43
                || (int)strData.charAt(i) == 92 || (int)strData.charAt(i) == 124 || (int)strData.charAt(i) == 33 || (int)strData.charAt(i) == 34 || (int)strData.charAt(i) == 10 || (int)strData.charAt(i) == 13 || (int)strData.charAt(i) == 9
                || (int)strData.charAt(i) == 58 || (int)strData.charAt(i) == 160)
            {
                strReturnValue.append(" ");
            } else {
                strReturnValue.append(strData.substring(i, i + 1));
            }
        }
        return strReturnValue.toString();
	}
	
	/**
	 * 숫자, 영문자 유무
	 * 
	 * @param strString
	 * @return
	 */
	public boolean getIsAlphaNumericCheck(String strString) {
        String regType = "^[A-Za-z0-9]*$";
        
		Pattern p = Pattern.compile(regType);
        Matcher m = p.matcher(strString);
        
        return m.matches();
    }
	
	/**
	 * 주코드 가져오기
	 * 
	 * @param stateCode
	 * @return
	 * @throws Exception
	 */
	public String getUserStateCode(String stateCode) throws Exception {
		String tempStateCode = null;
		
		if(Util.null2str(stateCode).contains("-")) {
			tempStateCode = stateCode.substring(stateCode.indexOf("-", 0) + 1, stateCode.length());
		} else {
			tempStateCode = stateCode;
		}
		
		return tempStateCode;
	}
	
	/**
	 * 품명, 상호명, 주소 데이터에서 적하목록에서 허용하지 않는 특수문자 및 2Byte 문자를 Space로 변환한다. - [AS-IS : GetEdiCharacter]
	 * 
	 * @param str
	 * @return
	 */
	public String getEdiCharacterCustom(String str, String checkedVal) {
		str = str.replaceAll(checkedVal, " ");
		
		return str.replace("\r", "").replaceAll("\n", "");
	}
	
	/**
	 * 2byte 문자일 경우 깨지지 않게 자르기
	 * 
	 * @param str
	 * @param start
	 * @param end
	 * @return
	 * @throws Exception
	 */
	public String getSubStringK(String str, int start, int end) throws Exception {
		// 시작위치 >= 끝 또는 문자열 < 시작위치
		if(start >= end || str.length() < start) {
			return "";
		}
		
		return Util.getFileReadData(str, "String", Integer.toString(end - start), start, end);
	}
	
	/**
	 * KCNET 파일전송 삭제 문자 - [AS-IS : DeleteCharString_AIRCIS]
	 * 
	 * @param str
	 * @return
	 */
	public String getDeleteCharStringManifest(String str) {
		String deleteStr = "{}[]:'?~`@#$^+|!,/&%*()_=;<>’";
        String tempStr = str;
        
        tempStr = tempStr.replace("\r\n", " ");
        tempStr = tempStr.replace("\n", " ");
        
        for(int i = 0; i < deleteStr.length(); ++i) {
        	tempStr = tempStr.replace(Character.toString(deleteStr.charAt(i)), " ");
        }
        
        return tempStr.replace("'", "`");
	}
	public String getDeleteCharStringFwb(String str) {
		String deleteStr = "{}[]:'?~`@#$^+|!,/&%*()_=;<>’";
        String tempStr = str;
        
        tempStr = tempStr.replace("\\", " ").replace('"', ' ');
        tempStr = tempStr.replace("\r\n", " ");
        tempStr = tempStr.replace("\n", " ");
        
        for(int i = 0; i < deleteStr.length(); ++i) {
        	tempStr = tempStr.replace(Character.toString(deleteStr.charAt(i)), " ");
        }
        
        return tempStr;
	}
	
	
	public String getRemove2bitChar(String strData, boolean isSpChar)
    {
		String pattern = "[ㄱ-ㅎ가-힣a-zA-Z0-9]";
        int nAsc = 0;
        boolean bResult = false;

        String strResult = "";
        for (int N = 0; N < strData.length(); N++)
        {
            bResult = false;
            nAsc = (int)strData.charAt(N);

            if (Pattern.matches(pattern, strData.substring(N, N + 1)))
            {
                bResult = true;
            }
            else
            {
                bResult = false;
            }

            if (nAsc == 32)
            {
                bResult = true;
            }


            if (!isSpChar)
            {
                if ((nAsc >= 33 && nAsc <= 47) || (nAsc >= 58 && nAsc <= 64) || (nAsc >= 91 && nAsc <= 96) || (nAsc >= 123 && nAsc <= 126))
                {
                    bResult = true;
                }
            }

            if (bResult)
            {
                strResult += strData.substring(N, N + 1);
            }
        }


        return strResult;
    }
	
	public String GetReplaceETCCharToSpace(String strSource, String strType)
    {
		String strResult = "";
        String strETCChar = "";
        String chkStr = "";

        if (strType == "1")
            strETCChar = "$%.,-";  // 허용 특수문자(관우회용)
        else if (strType == "2")
            strETCChar = "()://$%.,_-";  // 허용 특수문자(관우회 전가상거래 주소)
        else if (strType == "3")
            strETCChar = "(,./-_()#~&<>@)";  // 허용 특수문자(EMS)
        else if (strType == "4")
            strETCChar = ".-";         //허용 특수문자(KTNET FWB)
        else if (strType == "5")
            strETCChar = "";         //특수문자 모두제거
        else if (strType == "6")
            strETCChar = "!@#*&():;/$%.,_-";         //자동생성용
        else if (strType == "7")
            strETCChar = "";         //허용하는 특수문자 없음, 영문숫자만 허용
        else if (strType == "8")
            strETCChar = "~!@#$%^&*()_+-=,./?;:`[]{}\r\n";         //허용하는 특수문자 없음, 영문숫자만 허용(FNC용 : 특수문자 '제외한 모두허용)
        else if (strType == "9")
            strETCChar = "~!@#$%^&*()_+-=,./?;:`[]{}";         //어깨점, 엔터, <> 전각문자를 제외한 모든 특수문자 허용
        else if (strType == "10")
            strETCChar = "~!@#$%^&*()_+-=,.?;:`[]{}";         //어깨점, 엔터, / <> 전각문자를 제외한 모든 특수문자 허용

        char[] strSplitChar = strSource.toCharArray();

        for (int i = 0; i < strSplitChar.length; i++)
        {
        	chkStr = Util.null2str(strSplitChar[i]);
        	 int iASCIICode = (int)strSplitChar[i];
            if (chkStr.equals(" ") || iASCIICode == 32)
            {
                strResult += " ";
                continue;
            }

            if (strETCChar.contains(chkStr))
            {
                strResult += chkStr;
                continue;
            }

            if(strSplitChar[i] < 256)
            {
                if ((iASCIICode >= 65 && iASCIICode <= 90) ||  // 알파벳대문자
                (iASCIICode >= 97 && iASCIICode <= 122) ||     // 알파벳소문자
                (iASCIICode >= 48 && iASCIICode <= 57))        // 숫자
                {
                    strResult += chkStr;
                }
                else
                {
                    strResult += " ";
                }
            }
            else
            {
                strResult += " ";
            }
        }

        return strResult;
    }
	
	public String GetReplaceETCCharToSpace_A(String strSource, String strType)
    {
		String strResult = "";
		String strETCChar = "";
		String chkStr = "";

        if (strType == "1")
            strETCChar = ".,=;:()%-*#$@![]{}_";         //.,=;:()%-*#$@![]{}_ 만 허용한다.
        else if (strType == "2")
            strETCChar = "";                            //허용하는 특수문자 없음
            
        char[] strSplitChar = strSource.toCharArray();

        for (int i = 0; i < strSplitChar.length; i++)
        {
        	chkStr = Util.null2str(strSplitChar[i]);
        	
        	int iASCIICode = (int)strSplitChar[i];
            //공백검사 안되는 경우가 있어 아스키코드도 비교 
        	if (chkStr.equals(" ") || iASCIICode == 32)
            {
                strResult += " ";
                continue;
            }

            if (strETCChar.contains(chkStr))
            {
                strResult += chkStr;
                continue;
            }

            if(strSplitChar[i] < 256)
            {
                if ((iASCIICode >= 65 && iASCIICode <= 90) ||  // 알파벳대문자
                (iASCIICode >= 97 && iASCIICode <= 122) ||     // 알파벳소문자
                (iASCIICode >= 48 && iASCIICode <= 57))        // 숫자
                {
                    strResult += chkStr;
                }
                else
                {
                    strResult += "";
                }
            }
            else
            {
                strResult += "";
            }
        }

        return strResult.trim();
    }
    /// <summary>
    /// 문자열에 포함되어 있는 모든 특수문자를 모두 제거한다.
    /// </summary>
    /// <param name="str">문자열</param>
    /// <param name="deleteStr">제거할문자_구분자[|]</param>
    /// <param name="ReplaceChar">대체할문자_구분자[|]</param>
    /// <returns>' 문자가 제거된 문자열 " ~!@#$%^&*()"</returns>
    public String DeleteCharString_Loop(String str, String deleteStr, String ReplaceChar)
    {
        String TempStr = str;
        String[] EmptyChar = ReplaceChar.split("|");
        String[] DeleteChar = deleteStr.split("|");
        char[] DeleteCharArray = null;
        
        for (int i = 0; i < DeleteChar.length; i++){
        	DeleteCharArray = DeleteChar[i].toCharArray();
            for(int j = 0; j < DeleteCharArray.length; j++)
            {
            	char arrChar = DeleteChar[i].toCharArray()[j];
                if (DeleteChar.length != EmptyChar.length)
                    TempStr = TempStr.replace(Util.null2str(arrChar), "");
                else
                    TempStr = TempStr.replace(Util.null2str(arrChar), EmptyChar[i]);
            }
        }
        return TempStr;
    }
    //BL번호에 -Z01 의 순번이 붙는 B/L번호의 경우에는 -Z + 순번을 제거한 B/L번호로 리턴한다.
    public String GetBLNo_ZSeqDelete(String strBLNo)
    {
    	String strReturnValue = "";
        int nIdx = 0;
        if (strBLNo.indexOf("-Z") > 0)
        {
            nIdx = strBLNo.indexOf("-Z");
            strReturnValue = strBLNo.substring(0, nIdx);
        }
        else
            strReturnValue = strBLNo;

        return strReturnValue;
    }
    public String EDI_Numeric_Select(String sString)
    {
    	String sTemp = "";
        String nResult = "";
        String []tempStrArray = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        for (int nLoopI = 0; nLoopI < sString.length(); nLoopI++)
        {
            if (this.getStrcmp(sString.substring(nLoopI, nLoopI + 1), tempStrArray))
                sTemp = sTemp + sString.substring(nLoopI, nLoopI + 1);
        }
        nResult = sTemp;
        return nResult;
    }
    
	/**
	 * H.B/L No. 특수문자 제거
	 * 
	 * @param hBlNo
	 * @return
	 */
	public String getHblDeleteChar(String hBlNo) {
		
		// 공백과 '-' 문자 제거
		return Util.null2str(hBlNo).replace(" ", "").replace("-", "");
	}
	
	/**
	 * 아스키코드 체크후 리턴 - [AS-IS : get_BLCharCHK]
	 * 
	 * @param str
	 * @return
	 */
	public String getBLCharCHK(String xBLNo) {
		int nOneChar = 0;
        StringBuffer strBLCharChk = new StringBuffer();
        
        for (int i = 0; i < xBLNo.length(); i++) {
            nOneChar = xBLNo.charAt(i);
            if ((nOneChar > 47) && (nOneChar < 123)) {
            	strBLCharChk.append(xBLNo.charAt(i));
            }
        }
        return strBLCharChk.toString();
	}
	
	/**
	 * 숫자를 제외한 모든 문자 제거 - [AS-IS : DeleteAlphabetChar]
	 * 
	 * @param str
	 * @return
	 */
	public String getDeleteAlphabetChar(String str) {
		StringBuffer tempStr = new StringBuffer();

		for(int i = 0; i < str.length(); ++i) {
			if(Util.isNumber(Character.toString(str.charAt(i)))) {
				tempStr.append(Character.toString(str.charAt(i)));
			}
		}
		
		return tempStr.toString();
	}
	
	/**
	 * 숫자만 리턴 - [AS-IS : Numeric_Select]
	 * 
	 * @param sString
	 * @return
	 */
	public String getEdiNumericSelect(String sString) {
		// 정규 표현식 숫자가 아니면 "" 빈문자로 치환
		sString = sString.replaceAll("[^0-9]", "");
		
		return sString;
	}
	
	/**
	 * 사업자번호체크 - [AS-IS : BusinessNo_Chk]
	 * 
	 * @param str
	 * @return
	 */
	public boolean getBusinessNoChk(String str) {
		String sChkDigit;
		int nHap;
		int nChk;

		if ("".equals(Util.null2str(str))) {
			return true;
		}

		if(Util.null2str(str).trim().length() != 10 || "".equals(Util.null2str(str).trim())) {
			return false;
		}
		
		sChkDigit = "137137135";
		nHap = 0;
		for (int i = 0; i < 8; ++i) {
			// compareTo :::: -1은 작다, 0은 같다, 1은 크다
			if(Character.toString(str.charAt(i)).compareTo("9") > 0
					|| Character.toString(str.charAt(i)).compareTo("0") < 0) {
				return false;
			}
			nChk = Util.null2int(Character.toString(sChkDigit.charAt(i))) * Util.null2int(Character.toString(str.charAt(i)));
			nHap += nChk % 10;
		}
		
		nChk = Util.null2int(Character.toString(str.charAt(8))) * 5;
		nHap += (nChk / 10) + (nChk % 10);
		
		nHap = nHap % 10;
		if (nHap == 0) {
			nHap = 10;
		}
		
		if(Util.null2int(Character.toString(str.charAt(9))) == (10 - nHap)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 중개사업자, 문서별 서비스 코드 가져오기 
	 * @param solution
	 * @param bound
	 * @param boundType
	 * @param docType
	 * @param vendor
	 * @return
	 */
	public String getServiceCode(String solution, String bound, String boundType, String docType, String vendor) {
		String logVal = "getServiceCode - " + "solution:" + solution  + ", bound:" + bound + ", boundType:" + boundType + ", docType:" + docType + ", vendor:" + vendor;
		log.debug(logVal);
		if("AIR".equals(bound)) {
			if("EXP".equals(boundType)) {
				if("MANIFEST".equals(docType) || "MANIFEST-MODIFY".equals(docType)){	//KTNET, KCNET
					if("KTNET".equals(vendor)) {
						return CodeDefinition.KTNET_ACPS_AIR_EXP_FHL; 
					} else if("KCNET".equals(vendor)) {
						return CodeDefinition.KCNET_MFCS_AIR_EXP_FHL;
					}
				}else if("MANIFEST-ABNORMAL".equals(docType)) {				// 하기이상신고, 항목정정신고
					if("KTNET".equals(vendor)) {
						return CodeDefinition.KTNET_ACPS_AIR_EXP_FHL; 
					} else if("KCNET".equals(vendor)) {
						return CodeDefinition.KCNET_MFCS_AIR_IMP_WSC;
					}
				}
			}else if("IMP".equals(boundType)){
				if("MANIFEST".equals(docType) || "MANIFEST-MODIFY".equals(docType)){
					if("KTNET".equals(vendor)) {
						return CodeDefinition.KTNET_ACPS_AIR_IMP_FHL;
					} else if("KCNET".equals(vendor)) {
						return CodeDefinition.KCNET_MFCS_AIR_IMP_FHL;
					}
				}else if("MANIFEST-ABNORMAL".equals(docType)) {				// 하기이상신고, 항목정정신고
					if("KTNET".equals(vendor)) {
						return CodeDefinition.KTNET_ACPS_AIR_IMP_FHL; 
					} else if("KCNET".equals(vendor)) {
						return CodeDefinition.KCNET_MFCS_AIR_IMP_WSC;
					}
				}else if("MANIFEST-ABNORMAL-PANTOS".equals(docType)) {		// 판토스 하기이상신고, 항목정정신고
					if("KTNET".equals(vendor)) {
						return CodeDefinition.KTNET_ACPS_AIR_IMP_FHL; 
					} else if("KCNET".equals(vendor)) {
						return CodeDefinition.KCNET_ABNORMAL;
					}
				}else if("EMS".equals(docType)){	/** EMS환적 */
					return CodeDefinition.KTNET_EMS;
				}
			}
		}else if("SEA".equals(bound)) {
			if("EXP".equals(boundType)) {
				if("MANIFEST".equals(docType) || "MANIFEST-SFTP".equals(docType) || "MANIFEST-MODIFY".equals(docType) || "MANIFEST-ABNORMAL".equals(docType)) {	//KTNET, KLNET
					if("KTNET".equals(vendor)) {
						return CodeDefinition.KTNET_MFCS_SEA_EXP_TXT_SFTP;
					}else if("KLNET".equals(vendor)){
						/** KLNET 표준/사설여부는 사업장데이터 조회 후 문서생성소스에서 처리 */
						return CodeDefinition.KLNET_PRISM_SEA_EXP_TXT;
					}
				}
			} else if("IMP".equals(boundType)) {
				if("MANIFEST".equals(docType) || "MANIFEST-SFTP".equals(docType) || "MANIFEST-MODIFY".equals(docType) || "MANIFEST-ABNORMAL".equals(docType)) {
					if("KTNET".equals(vendor)) {
						return CodeDefinition.KTNET_MFCS_SEA_IMP_TXT_SFTP;
					}else if("KLNET".equals(vendor)){
						/** KLNET 표준/사설여부는 사업장데이터 조회 후 문서생성소스에서 처리 */
						return CodeDefinition.KLNET_PRISM_SEA_IMP_TXT;
					}
				}else if("EMS".equals(docType)){	/** EMS환적 */
					return CodeDefinition.KLNET_EMS;
				}
			}
		}
		return "";
	}
	
	/**
	 * UNIPASS 화물진행정보 MRNMSN 정보 가져오기
	 * @param mblNo
	 * @param hblNo
	 * @param blYy
	 * @return
	 * @throws Exception
	 */
	public String unipassGetMrnMsn(String mblNo, String hblNo, String blYy) {
		
		URI uri = null;
		RestTemplate restTemplate = new RestTemplate();
		EcmCargoProgressInfo cargoProgressInfo = new EcmCargoProgressInfo();
		String mrnMsn = "";
		
		try {
			UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
			        .scheme("https")
			        .host(unipassBaseUrl_)
			        .port(unipassPort_)
			        .path(unipassCargoProgressPath_)
			        .queryParam("crkyCn", URLDecoder.decode(unipassCargoProgressKey_, "UTF-8"));
	
			if (!"".equals(mblNo))	uriComponentsBuilder.queryParam("mblNo", mblNo.toUpperCase());
			if (!"".equals(hblNo))	uriComponentsBuilder.queryParam("hblNo", hblNo.toUpperCase());
			if (!"".equals(blYy))	uriComponentsBuilder.queryParam("blYy", blYy);
			uri = uriComponentsBuilder.build().encode().toUri();
			cargoProgressInfo = restTemplate.getForObject(uri, EcmCargoProgressInfo.class);
			if (cargoProgressInfo != null) {
				
				CargCsclPrgsInfo cargCsclPrgsInfoDTO = cargoProgressInfo.getCargCsclPrgsInfo();
				 
				if(cargCsclPrgsInfoDTO != null ) {
					String mtNo = Util.null2str(cargCsclPrgsInfoDTO.getCargMtNo());
					if(!"".equals(mtNo)) {
						mrnMsn = mtNo.substring(0,11) + mtNo.substring(11, 15);
					}
				}
				
			}
		}catch(Exception e) {
			log.error("unipassGetMrnMsn error : " + e.getMessage());
		}
		
		return mrnMsn;
	}
}
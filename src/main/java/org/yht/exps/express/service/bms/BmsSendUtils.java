package org.yht.exps.express.service.bms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.bms.EdiBmsSendMapper;
import org.yht.exps.express.model.bms.MrsRet;
import org.yht.exps.express.model.bms.UMrsSendData;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormal;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormalHbl;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.CarryInAbnormalCorr;
import org.yht.exps.express.model.bms.bmsAbnormalCorrection.CarryInAbnormalHblCorr;
import org.yht.exps.express.model.bms.bmsInCorrection.CarryInChange;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOutChange;
import org.yht.exps.express.model.bms.bmsOutNotice.BmsOutNotice;
import org.yht.exps.express.model.bms.disposal.InventoryIn;
import org.yht.exps.express.model.bms.out.BmsOut;
import org.yht.exps.express.model.bms.send.xml.GOVCBR5II.OutListGOVCBR5II;
import org.yht.exps.express.model.bms.send.xml.GOVCBR5LC.InGOVCBR5LC;
import org.yht.exps.express.model.bms.send.xml.GOVCBR5LC.InGOVCBR5LC.Amendment5LC;
import org.yht.exps.express.model.bms.send.xml.GOVCBR5LD.OutGOVCBR5LD;
import org.yht.exps.express.model.bms.send.xml.GOVCBR5LD.OutGOVCBR5LD.Amendment5LD;
import org.yht.exps.express.model.bms.send.xml.GOVCBR632.InDecrGOVCBR632;
import org.yht.exps.express.model.bms.send.xml.GOVCBR6NB.OutDecrGOVCBR6NB;
import org.yht.exps.express.model.bms.send.xml.GOVCBRDKO.DeclGOVCBRDKO;
import org.yht.exps.express.model.bms.send.xml.GOVCBRDKO.DeclGOVCBRDKO.ConsignmentItem;
import org.yht.exps.express.model.bms.send.xml.GOVCBRDKP.DeclGOVCBRDKP;
import org.yht.exps.express.model.bms.send.xml.GOVCBRDKP.DeclGOVCBRDKP.ConsignmentItemDKP;
import org.yht.exps.express.model.bms.unipass.CargoProgressInfo;
import org.yht.exps.express.model.bms.unipass.CargoProgressInfo.CargCsclPrgsInfo;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.service.edi.EdiCommonService;
import org.yht.util.DateTime;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

/**
 * 전송 처리를 위한 Utils 클래스
 */
@Slf4j
@Component
public class BmsSendUtils {

    @Autowired
    private CustomProperties customProperties;

    @Value("${spring.profiles.active}")
    private String profile;

    @Autowired
    CommonMapper commonMapper;

	@Autowired
	EdiBmsSendMapper ediBmsSendMapper;
	
	@Resource(name="ediCommonService")
	EdiCommonService ediCommonService;
	
	@Value("${custom.unipass-url:unipass.customs.go.kr}")
	private String unipassBaseUrl_;

	@Value("${custom.unipass-port:38010}")
	int unipassPort_;

	@Value("${custom.unipass-path:/ext/rest/cargCsclPrgsInfoQry/retrieveCargCsclPrgsInfo}")
	private String unipassCargoProgressPath_;
	
	@Value("${custom.unipass-key:j230k135f190w257u000a090w1}")
	private String unipassCargoProgressKey_;

    public String getEdiPortalMailboxId(String entCode, String entBusinessCode) {
        String ediPortalMailboxId="";
        Map<String, Object> searchMap = new HashMap<>();
        searchMap.put("srchEntCode", entCode); // "YHTXX"
        searchMap.put("srchEntBusinessCode", entBusinessCode); // "YHTXX-KR"
        searchMap.put("srchBound", "BMS");
        searchMap.put("srchBoundType", "IMP");
        searchMap.put("srchEdiCityPort", "ICN");
        Map<String, Object> dataMap = commonMapper.selectBusEdiEcmInfo(searchMap);
        ediPortalMailboxId = (String) dataMap.getOrDefault("EDI_PORTAL_MAILBOX_ID", "TEST_DEFAULT"); // VC116814146901
        return ediPortalMailboxId;
    }
    
    /* mrs 사서함 설정 */
    public String getEdiPortalMailboxIdSrch(String entCode, String entBusinessCode, String bound, String boundType, String ediCityPort) {
        String ediPortalMailboxId="";
        Map<String, Object> searchMap = new HashMap<>();
        searchMap.put("srchEntCode", entCode); // "YHTXX"
        searchMap.put("srchEntBusinessCode", entBusinessCode); // "YHTXX-KR"
        searchMap.put("srchBound", bound);
        searchMap.put("srchBoundType", boundType);
        searchMap.put("srchEdiCityPort", ediCityPort);
        Map<String, Object> dataMap = commonMapper.selectBusEdiEcmInfo(searchMap);
        ediPortalMailboxId = (String) dataMap.getOrDefault("EDI_PORTAL_MAILBOX_ID", "TEST_DEFAULT"); // VC116814146901
        return ediPortalMailboxId;
    }
    
    /***
     * XML Java Instance 를 XML 스트링으로 변경 하고 SchemaLocation및 kcs Header를 추가 한다.
     * @param decl
     * @param jaxbSchemaLocation
     * @param strkcs
     * @return
     */
    public String sendVoToXmlMarshal(Object decl , String jaxbSchemaLocation , String strkcs) {
        String xmlString = "";
        try {
            // JAXBContext 생성
            JAXBContext context = JAXBContext.newInstance(decl.getClass());
            Marshaller marshaller = context.createMarshaller();

            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, jaxbSchemaLocation);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            // XML을 문자열로 변환
            StringWriter writer = new StringWriter();
            marshaller.marshal(decl, writer);
            // 생성된 XML 출력
            xmlString = writer.toString();
            //headeer KCS 테그에 추가 처리
            xmlString = xmlString.replace("<wco:Declaration ", "<wco:Declaration " + strkcs + " ");
        } catch (JAXBException e) {
            xmlString = "";
            log.error(Util.getPrintStacTraceString(e));
        }  finally {
        }
        return xmlString;
    }

    /**
     * MRS 서버 전송후 리턴값 받기
     * security: {"customsErrDesc":"정상처리","customsErrCode":"C200","data":null,"body":"ebXML 메시지가 정상 처리 되었습니다."}
     * @param data
     * @return
     */
    public MrsRet sendMrsServerByHttp(UMrsSendData data)  {
        MrsRet mrsRet = new MrsRet();
        URL url = null;
        HttpURLConnection conn = null;
        BufferedReader in = null;
        OutputStreamWriter out = null;
        String inputLine = null;
        JSONObject jsonObject = null;
        JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
        String ebmsUrl       = customProperties.getEbmsUrl();
        String ebmsAccessKey = customProperties.getEbmsAccessKey();
        JSONObject sendData  = new JSONObject();
        sendData.put("customsReportNo",data.getCustomsReportNo()); // customsReportNo
        sendData.put("xml",data.getXml()); // xml
        // ediPortalMailboxId
        String sendEbmsUrl  = ebmsUrl+"/customs/send/data/"  + data.getEdiPortalMailboxId() + "/" + data.getDoucmentCode();
        try {
            url = new URL(sendEbmsUrl);

            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(120_000); // [추가] MRS 서버 연결 timeout, 120초
            conn.setReadTimeout(120_000);    // [추가] MRS 서버 응답 대기 timeout, 120초
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-type","application/json");
            conn.setRequestProperty("access-key", ebmsAccessKey);
            conn.setRequestProperty("Authorization", "Bearer " + data.getToken());
            conn.setDoInput(true);
            conn.setDoOutput(true);
            out = new OutputStreamWriter(conn.getOutputStream());
            out.write(sendData.toString());

            out.close();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            while((inputLine = in.readLine()) != null) {
                jsonObject = (JSONObject) parser.parse(inputLine);
            }
            //System.out.println("security: "+jsonObject.toJSONString());
            if( null != jsonObject) {
                mrsRet.setCustomsErrCode(jsonObject.getAsString("customsErrCode"));
                mrsRet.setCustomsErrDesc(jsonObject.getAsString("customsErrDesc"));
                mrsRet.setBody(jsonObject.getAsString("body"));
                mrsRet.setData(jsonObject.getAsString("data"));
            }else{
                mrsRet.setCustomsErrCode("C998"); // Send ERROR
                mrsRet.setCustomsErrDesc("XML 전송중에 문제가 발생 하였습니다."); // CONNECT ERROR
                mrsRet.setBody("XML 전송중에 문제가 발생 하였습니다.");
            }
        } catch (java.net.SocketTimeoutException e) {
            mrsRet.setCustomsErrCode("C997");
            mrsRet.setCustomsErrDesc("요청->MRS or MRS->응답 timeout: " + e.getLocalizedMessage());
            mrsRet.setBody(e.getLocalizedMessage());
        } catch (IOException | ParseException e) {
            mrsRet.setCustomsErrCode("C999"); // CONNECT ERROR
            mrsRet.setCustomsErrDesc(e.getLocalizedMessage()); // CONNECT ERROR
            mrsRet.setBody(e.getLocalizedMessage());
        } catch (Exception e) {
            mrsRet.setCustomsErrCode("C999"); // timout 혹은 연결오류 외 오류들을 전부 그냥 C999로 처리
            mrsRet.setCustomsErrDesc(e.getLocalizedMessage());
            mrsRet.setBody(e.getLocalizedMessage());
        }finally {
        	try {
    	        if (out != null) {
    	            out.close();
    	        }

    	        if (in != null) {
    	            in.close();
    	        }

    	    } catch (Exception e) {
    	        throw new RuntimeException("MRS HTTP stream close 실패");
    	    } finally {
    	        if (conn != null) {
    	            conn.disconnect();
    	        }
    	    }
        }
        return  mrsRet;
    }

    /**
     * @param ediPortalMailboxId
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public String getMrsToken(String ediPortalMailboxId )  {
        URL url = null;
        HttpURLConnection conn = null;
        BufferedReader in = null;
        String inputLine = null;
        JSONObject jsonObject = null;
        JSONParser parser    = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
        String ebmsUrl       = customProperties.getEbmsUrl();
        String ebmsAccessKey = customProperties.getEbmsAccessKey();

        String token =null;
        try{
            /** 토큰 */
            url = new URL(ebmsUrl + "/security/get/token/" + ediPortalMailboxId);
            // System.out.println(url.getPath());
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

        }finally{
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {log.error(e.getMessage());  }
            }
            if(conn != null){conn.disconnect();}
        }
        /** 2025-05-13 토큰 정상처리되지 않았을경우 메일발송 후 오류처리  */
//        if(jsonObject != null) {
        try {
        	if(jsonObject == null) return "";
        	token = Util.null2str(jsonObject.get("token"));
        }catch(Exception e){
        	log.error(Util.getPrintStacTraceString(e));
			try{
				ediCommonService.asyncErrorSendEmail("ManifestController.sendCustomsManifestImp", new HashMap<String, Object>(), e);
				throw e;
			}catch(Exception ee){
				log.error(Util.getPrintStacTraceString(ee));
				throw e;
			}
        }
//        }
        return token;
    }
    public boolean isOnline(String address, int port) {
        boolean result;
        if(!profile.contains("local") && !profile.contains("dev")){
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

    /***
     * HMS 토큰생성
     * @param send
     * @param entCode
     * @param docCode
     * @param docName
     * @return
     */
    public String getHmsToken()  {
        URL url = null;
        HttpURLConnection conn = null;
        BufferedReader in = null;
        String inputLine = null;
        JSONObject jsonObject = null;
        JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);

        String token = null;
        try{
	        TrustManager[] trustAllCerts = new TrustManager[] { 
				new X509TrustManager() {
					public X509Certificate[] getAcceptedIssuers() {return null;}
					public void checkClientTrusted(X509Certificate[] certs, String authType) {
						//checkClientTrusted
					}
					public void checkServerTrusted(X509Certificate[] certs, String authType) {
						//checkClientTrusted
					}
			  } 
			};
			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session){
					return true;
				}
			};
			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			
            url = new URL(customProperties.getHmsUrl() + "/auth/oauth2/token");
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write("grant_type=client_credentials&client_id=" + customProperties.getHmsClientId() + "&client_secret=" + customProperties.getHmsClientSecret());
            out.close();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            while((inputLine = in.readLine()) != null) {
                jsonObject = (JSONObject) parser.parse(inputLine);
            }
        }catch(Exception e){
        	log.error(Util.getPrintStacTraceString(e));

        }finally{
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {log.error(e.getMessage());  }
            }
            if(conn != null){conn.disconnect();}
        }
        token = Util.null2str(jsonObject.get("access_token"));
        return token;
    }
    /***
     * 전송 처리 로그 맵 리턴
     * @param send
     * @param entCode
     * @param docCode
     * @param docName
     * @return
     */
    public static Map<String, Object> getSendLogMap(UMrsSendData send, String entCode, String docCode, String docName) {
        Map<String, Object> logMap = new HashMap<String, Object>();
        //logMap.put("ent_code", sendManifest.getEntCode());
        logMap.put("ent_code", entCode);
        logMap.put("customer_code", "");
        logMap.put("solution", "BMS");  //BMS
        logMap.put("bound", "AIR");
        logMap.put("bound_type", "IMP"); // IMP.수입
        logMap.put("hbl_no", "");
        logMap.put("if_service_code", "");
        logMap.put("if_vender", "GOV");
        logMap.put("if_send_recv_type", "S");  //S.SEND ( 송신처리 ), R.RECEIVE ( 수신처리 ) , 수신에서는 R 로 변경 처리
        logMap.put("if_send_recv_method", "");
        logMap.put("if_status_info", "");
        logMap.put("if_sender_id", "");
        logMap.put("if_receiver_id", "");
        logMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));
        logMap.put("if_date_time", DateTime.getDate("HH:mm:ss"));
        logMap.put("register_user_id", send.getRegisterUserId());
        logMap.put("register_date_time", System.currentTimeMillis());
        logMap.put("if_doc_reference_no", send.getCustomsReportNo()); // 신고 번호
        logMap.put("if_doc_code", docCode);
        logMap.put("if_doc_name", docName);
        if("C200".equals(send.getCustomsErrCode())) {
        	logMap.put("if_status", "SUCCESS");
        } else {
        	logMap.put("if_status", "ERROR");
        }
        logMap.put("customs_err_code", send.getCustomsErrCode());
        logMap.put("customs_err_desc", send.getCustomsErrDesc());
        logMap.put("customs_err_desc_additional", send.getCustomsErrDescAdditional());
        return logMap;
    }
    /**
     * GOVCBR632 . 반입신고서 테이블 필드를 XML 변경용 객체와 매핑 처리
     * @param in
     * @return
     */
    public InDecrGOVCBR632 getBmsInToGOVCBR632(InventoryIn in)
    {
        InDecrGOVCBR632 decl = new InDecrGOVCBR632();
        decl.setFunctionCode(in.getFunctionCode());
        decl.setId(in.getCustomsReportNo()); // 신고 번호
        decl.setTypeCode(in.getTypeCode());
        /*declaration.setVersionID("1"); //컬럼 제외 처리  : 04-08 By Kim*/
        // 동적 생성 부분
        if(StringUtils.isNotBlank(in.getXrayInspectionCode())) {
            decl.setExaminationIndicatorCode(in.getXrayInspectionCode()); // X-ray 검사여부
        }
        
        /** 결함보고서_반입_20240805_김현숙.ppt   반입신고서 XML 송신시 XRAY_CHECK 가 있는 경우 추가 해야 함  공백이면 해당라인은 생략  
         *  수기전송때도 추가 함 2026-01-14 yjkim*/
        if(!Util.isEmpty(in.getXrayCheck())){
        	decl.setExaminationIndicatorCode(in.getXrayCheck());
        }
        
        InDecrGOVCBR632.AdditionalCode additionalCode = new InDecrGOVCBR632.AdditionalCode();

        // additionalCode.setCarryInTypeCode("Z");  //컬럼 제외 처리  : 04-08 By Kim*/
        additionalCode.setCarryInTypeCode(in.getInClass());  // 2024-05-20 컬럼 추가 (최정민) */
        additionalCode.setCarryInSplitCode(in.getBunhal());  // A로 고정 처리 , 분할반입구분
        //additionalCode.setCarryInUseCode("P"); //컬럼 제외 처리  : 04-08 By Kim*/

        decl.setAdditionalCode(additionalCode);

        // 동적 생성 부분
        if(StringUtils.isNotBlank(in.getInApprovalNo())) {
            InDecrGOVCBR632.AdditionalDocument additionalDocument = new InDecrGOVCBR632.AdditionalDocument();
            additionalDocument.setId(in.getInApprovalNo()); //반입근거번호
            decl.setAdditionalDocument(additionalDocument);
        }

        InDecrGOVCBR632.Consignment consignment = new InDecrGOVCBR632.Consignment();
        consignment.setGrossVolumeMeasure(String.valueOf(in.getWt()));  // WT
        consignment.setTotalPackageQuantity(String.valueOf(in.getCt())); //CT
        consignment.setAbnormalityTypeCode(in.getAccidentClass());  //ACCIDENT_CLASS  사고유형 코드적고 이상없으면 OKY
        if(!"OKY".equals(in.getAccidentClass())) {
            consignment.setAbnormalityWeightMeasure(String.valueOf(in.getAccidentWt()));  //ACCIDENT_WT
            consignment.setAbnormalityQuantityQuantity(String.valueOf(in.getAccidentCt())); //ACCIDENT_CT
        }
        //InDecrGOVCBR632.Consignor consignor = new InDecrGOVCBR632.Consignor();
        //consignor.setId("1234567890"); //컬럼 제외 처리  : 04-08 By Kim*/
        //consignment.setConsignor(consignor);

        InDecrGOVCBR632.Packaging packaging = new InDecrGOVCBR632.Packaging();
        packaging.setTypeCode(in.getPkgType());  //PKG_TYPE
        consignment.setPackaging(packaging);

        InDecrGOVCBR632.UCR ucr = new InDecrGOVCBR632.UCR();
        ucr.setCustomsAssignedReferenceID(in.getCargoReferenceNo()); // 화물관리번호 19자리
        consignment.setUcr(ucr);

        InDecrGOVCBR632.Warehouse warehouse = new InDecrGOVCBR632.Warehouse();
        String carryInDateTime = in.getCarryinDateTime();
        if(carryInDateTime.length() == 12 ){
            carryInDateTime = carryInDateTime+"00";
        }
        warehouse.setArrivalDateTime(carryInDateTime);  //반입일시를 기재 14자리
        warehouse.setId(in.getLocationPlace());  //장치위치를 기재 10자리 자릿수
        consignment.setWarehouse(warehouse);

        decl.setConsignment(consignment);

        //InDecrGOVCBR632.TransportEquipment transportEquipment = new InDecrGOVCBR632.TransportEquipment();
        //transportEquipment.setId("TK001"); //컬럼 제외 처리  : 04-08 By Kim
        //declaration.setTransportEquipment(transportEquipment);

        return decl;
    }

    /**
     * GOVCBR6NB(반출신고서) 테이블 필드를 XML 변경용 객체와 매핑 처리
     * @param out
     * @return
     */
    public OutDecrGOVCBR6NB getBmsOutToGOVCBR6NB(BmsOut out) {
    	/** 2024-04-26 VersionID(분할반출차수), ID(기간연장근거번호), CarryOutExtensionCode(반출기간연장구분) 엘리먼트 제외 */
    	OutDecrGOVCBR6NB decl = new OutDecrGOVCBR6NB();

    	decl.setFunctionCode(out.getFunctionCode()); /** 전자문서기능(9: 원본, 35: 재전송) */
    	decl.setId(out.getCustomsReportNo()); /** 신고번호(보세구역부호(8)+년도(02)+일련번호(08)) */
    	decl.setTypeCode(out.getTypeCode()); /** 문서형태구분 */
    	decl.setGrossVolumeMeasure(String.valueOf(out.getOutWt())); /** 반출중량 */

    	OutDecrGOVCBR6NB.AdditionalCode additionalCode = new OutDecrGOVCBR6NB.AdditionalCode();
    	additionalCode.setCarryOutTypeCode(out.getOutClass()); /** 화물반출유형 */
    	additionalCode.setCarryOutSplitCode(out.getBunhal()); /** 분할반출구분('A' 고정) */
        decl.setAdditionalCode(additionalCode);

        OutDecrGOVCBR6NB.AdditionalDocument additionalDocument = new OutDecrGOVCBR6NB.AdditionalDocument();
        additionalDocument.setId(out.getOutApprovalNo()); /** 반출근거번호 */
        decl.setAdditionalDocument(additionalDocument);

        OutDecrGOVCBR6NB.Consignment consignment = new OutDecrGOVCBR6NB.Consignment();
        consignment.setTotalPackageQuantity(String.valueOf(out.getOutCt())); /** 반출총개수 */

        OutDecrGOVCBR6NB.UCR ucr = new OutDecrGOVCBR6NB.UCR();
        ucr.setCustomsAssignedReferenceID(out.getCargoReferenceNo()); /** 화물관리번호 */
        consignment.setUcr(ucr);

        OutDecrGOVCBR6NB.Warehouse warehouse = new OutDecrGOVCBR6NB.Warehouse();
        String carryoutDateTime = out.getCarryoutDateTime();
        if(carryoutDateTime.length() == 12) carryoutDateTime = carryoutDateTime + "00";
        warehouse.setDepartureDateTime(carryoutDateTime); /** 반출일시(CCYYMMDDHHMSS) 14자리 */
        consignment.setWarehouse(warehouse);

        decl.setConsignment(consignment);

        return decl;
    }

    /**
     * GOVCBR5II(반출통고 목록보고서) 테이블 필드를 XML 변경용 객체와 매핑 처리
     * @param outNotice
     * @return
     */
    public OutListGOVCBR5II getBmsOutNoticeToGOVCBR5II(BmsOutNotice outNotice) {
    	/** 2024-04-26 BirthDate(화주 생년월일), CountrySubDivisionID(화주 도로명주소), BuildingNumber(화주 건물관리번호) 엘리먼트 제외 */
    	OutListGOVCBR5II decl = new OutListGOVCBR5II();

    	decl.setDeclarationOfficeID(outNotice.getOfficeId()); /** 신고세관/과 */
    	decl.setFunctionCode(outNotice.getFunctionCode()); /** 전자문서기능(9: 원본, 35: 재전송) */
    	decl.setId(outNotice.getCargoReferenceNo()); /** 화물관리번호 */
    	decl.setIssueDateTime(outNotice.getOutNoticeDate()); /** 반출통고일자(CCYYMMDD) */
    	decl.setTotalGrossMassMeasure(String.valueOf(outNotice.getNoticeWt())); /** 반입중량 */
    	decl.setTotalPackageQuantity(String.valueOf(outNotice.getNoticeCt())); /** 반입개수 */
    	decl.setTypeCode(outNotice.getTypeCode()); /** 문서형태구분 */

    	OutListGOVCBR5II.AdditionalInformation additionalInformation = new OutListGOVCBR5II.AdditionalInformation();
    	additionalInformation.setLimitDateTime(outNotice.getPeriodOverDate()); /** 체화예정일자 */
    	decl.setAdditionalInformation(additionalInformation);

    	OutListGOVCBR5II.Consignment consignment = new OutListGOVCBR5II.Consignment();
    	OutListGOVCBR5II.Warehouse warehouse = new OutListGOVCBR5II.Warehouse();
    	warehouse.setId(outNotice.getBmsWhCode()); /** 반입장소 */
    	consignment.setWarehouse(warehouse);
    	decl.setConsignment(consignment);

    	OutListGOVCBR5II.Importer importer = new OutListGOVCBR5II.Importer();
    	importer.setName(outNotice.getCustomerName());

    	OutListGOVCBR5II.Address address = new OutListGOVCBR5II.Address();
    	address.setLine(outNotice.getCustomerAddress2()); /** 화주상세주소 */
    	address.setPostcodeID(outNotice.getCustomerZipcode()); /** 화주우편번호 */
    	address.setDescription(outNotice.getCustomerAddress1()); /** 화주기본주소 */
    	importer.setAddress(address);

    	OutListGOVCBR5II.Communication communication = new OutListGOVCBR5II.Communication();
    	communication.setId(outNotice.getCustomerTel()); /** 화주전화번호 */
    	importer.setCommunication(communication);
    	decl.setImporter(importer);

    	OutListGOVCBR5II.TransportContractDocument transportContractDocument = new OutListGOVCBR5II.TransportContractDocument();
    	transportContractDocument.setId(outNotice.getBlNo()); /** B/L 번호 */
    	decl.setTransportContractDocument(transportContractDocument);

    	return decl;
    }

    /**
     * GOVCBR5LC(반입신고정정신청서) 테이블 필드를 XML 변경용 객체와 매핑 처리
     * @param change
     * @return
     */
    public InGOVCBR5LC getBmsChangeToGOVCBR5LC(CarryInChange change) {
    	InGOVCBR5LC decl = new InGOVCBR5LC();

    	decl.setId(change.getCustomsReportNo()); /** 반입신고번호 */
    	decl.setIssueDateTime(change.getChangeDate()); /** 신청일자(CCYYMMDD) */
    	decl.setTypeCode(change.getTypeCode()); /** 문서형태구분 */
    	decl.setVersionID(change.getChangeCount()); /** 신청차수 */
    	decl.setSubTypeCode(change.getChangeClass()); /** 신청구분 */
    	decl.setReason(String.valueOf(change.getChangeReason())); /** 정정사유 */

    	Amendment5LC amend;
    	List<Amendment5LC> amendments = new ArrayList<Amendment5LC>();

    	/** 반입정정신청항목 조회 */
    	List<CarryInChange> itemList = ediBmsSendMapper.selectCarryInChangeItemList(change.getChangeNo());

    	for(CarryInChange item: itemList) {
    		amend = new Amendment5LC();
    		amend.setStatementDescription(StringUtils.deleteWhitespace(item.getChangeBefore().replaceAll("[:-]", ""))); /** 정정전 */
    		amend.setAdjustmentDescription(StringUtils.deleteWhitespace(item.getChangeAfter().replaceAll("[:-]", ""))); /** 정정후 */

    		InGOVCBR5LC.Pointer pointer = new InGOVCBR5LC.Pointer();
    		pointer.setTagID(item.getChangeCode()); /** 항목번호 */
    		amend.setPointer(pointer);
    		amendments.add(amend);
    	}
    	decl.setAmendments(amendments);

    	InGOVCBR5LC.Submitter submitter = new InGOVCBR5LC.Submitter();
    	submitter.setName(String.valueOf(change.getChangeEmployeeName())); /** 신청자 성명 */
    	decl.setSubmitter(submitter);

    	InGOVCBR5LC.UCR ucr = new InGOVCBR5LC.UCR();
    	ucr.setCustomsAssignedReferenceID(change.getCargoReferenceNo()); /** 화물관리번호 */
    	decl.setUcr(ucr);

    	return decl;
    }

	/**
     * GOVCBR5LD(반출신고정정신청서) 테이블 필드를 XML 변경용 객체와 매핑 처리
     * @param change
     * @return
	 */
	public OutGOVCBR5LD getBmsChangeToGOVCBR5LD(CarryOutChange change) {
		OutGOVCBR5LD decl = new OutGOVCBR5LD();

    	decl.setId(change.getCustomsReportNo()); /** 반입신고번호 */
    	decl.setIssueDateTime(change.getChangeDate()); /** 신청일자(CCYYMMDD) */
    	decl.setTypeCode(change.getTypeCode()); /** 문서형태구분 */
    	decl.setVersionID(change.getChangeCount()); /** 신청차수 */
    	decl.setSubTypeCode(change.getChangeClass()); /** 신청구분 */
    	decl.setReason(String.valueOf(change.getChangeReason())); /** 정정사유 */

    	Amendment5LD amend;
    	List<Amendment5LD> amendments = new ArrayList<Amendment5LD>();

    	/** 반출정정신청항목 조회 */
    	List<CarryOutChange> itemList = ediBmsSendMapper.selectCarryOutChangeItemList(change.getChangeNo());

    	for(CarryOutChange item: itemList) {
    		amend = new Amendment5LD();
    		amend.setStatementDescription(StringUtils.deleteWhitespace(item.getChangeBefore().replaceAll("[:-]", ""))); /** 정정전 */
    		amend.setAdjustmentDescription(StringUtils.deleteWhitespace(item.getChangeAfter().replaceAll("[:-]", ""))); /** 정정후 */

    		OutGOVCBR5LD.Pointer pointer = new OutGOVCBR5LD.Pointer();
    		pointer.setTagID(item.getChangeCode()); /** 항목번호 */
    		amend.setPointer(pointer);
    		amendments.add(amend);
    	}
    	decl.setAmendments(amendments);

    	OutGOVCBR5LD.Submitter submitter = new OutGOVCBR5LD.Submitter();
    	submitter.setName(String.valueOf(change.getChangeEmployeeName())); /** 신청자 성명 */
    	decl.setSubmitter(submitter);

    	OutGOVCBR5LD.UCR ucr = new OutGOVCBR5LD.UCR();
    	ucr.setCustomsAssignedReferenceID(change.getCargoReferenceNo()); /** 화물관리번호 */
    	decl.setUcr(ucr);

        return decl;
	}

	/**
     * GOVCBRDKO(특송화물 반입물품 이상보고서) 테이블 필드를 XML 변경용 객체와 매핑 처리
     * @param abn
     * @return
	 */
	public DeclGOVCBRDKO getBmsAbnormalToGOVCBRDKO(CarryInAbnormal abn) {
		DeclGOVCBRDKO decl = new DeclGOVCBRDKO();

		decl.setDeclarationOfficeID(abn.getOfficeId()); /** 세관/과부호 */
		decl.setId(abn.getCustomsReportNo()); /** 제출번호 */
		decl.setIssueDateTime(abn.getAbnReqDate()); /** 제출일자 */
		decl.setTypeCode(abn.getTypeCode()); /** 문서형태구분 */

		DeclGOVCBRDKO.Consignment consignment = new DeclGOVCBRDKO.Consignment();

		/** HBL 목록 조회 */
		List<CarryInAbnormalHbl> dataList = ediBmsSendMapper.selectEdiBmsAbnormalHblSendList(abn.getAbnNo());

		if(dataList.size() == 1 && dataList.get(0).getHsn().equals("")) {
			/**
			 * MBL:BL = 1:1인데 HSN 값이 없을 경우
			 * BL테이블 조회 결과 값을 MBL 엘리먼트에 맵핑하고 BL 관련 엘리먼트는 제외
			 */
			consignment.setGrossVolumeMeasure(String.valueOf(dataList.get(0).getExWt())); /** 적재화물목록중량 */
			consignment.setTotalPackageQuantity(String.valueOf(dataList.get(0).getExCt())); /** 적재화물목록포장개수 */
			consignment.setValueAmount(String.valueOf(dataList.get(0).getWt())); /** 반입결과중량 */
			consignment.setAbnormalityWeightMeasure(String.valueOf(dataList.get(0).getAccidentWt())); /** 사고중량 */
			consignment.setAbnormalityQuantityQuantity(String.valueOf(dataList.get(0).getAccidentCt())); /** 사고포장개수 */
			consignment.setResultsQuantity(String.valueOf(dataList.get(0).getCt())); /** 반입결과포장개수 */

			DeclGOVCBRDKO.AdditionalInformation additionalInformation = new DeclGOVCBRDKO.AdditionalInformation();
			additionalInformation.setStatementCode(dataList.get(0).getAccidentClass()); /** 사고/상이 내역부호 */
			additionalInformation.setStatementDescription(dataList.get(0).getRejectReason()); /** 이상유형상세내용 */
			consignment.setAdditionalInformation(additionalInformation);
		} else if((dataList.size() == 1 && !dataList.get(0).getHsn().equals("")) || dataList.size() > 1) {
			/**
			 * MBL:BL = 1:1이면서 HSN 값이 있거나 MBL:BL = 1:N일 경우
			 * MBL 관련 엘리먼트는 MBL_NO, MSN만 맵핑하고 BL 관련 엘리먼트는 size()만큼 반복 추가
			 */
			ConsignmentItem item;
			List<ConsignmentItem> consignmentItems = new ArrayList<ConsignmentItem>();

			for(CarryInAbnormalHbl data: dataList) {
				item = new ConsignmentItem();

				DeclGOVCBRDKO.AdditionalInformationHbl additionalInformationHbl = new DeclGOVCBRDKO.AdditionalInformationHbl();
				additionalInformationHbl.setStatementCode(data.getAccidentClass()); /** 사고/상이 내역부호 */
				additionalInformationHbl.setStatementDescription(data.getRejectReason()); /** 이상유형상세내용 */
				item.setAdditionalInformation(additionalInformationHbl);

				DeclGOVCBRDKO.GoodsMeasure goodsMeasure = new DeclGOVCBRDKO.GoodsMeasure();
				goodsMeasure.setGrossMassMeasure(String.valueOf(data.getExWt())); /** 적재화물목록중량 */
				goodsMeasure.setNetNetWeightMeasure(String.valueOf(data.getWt())); /** 반입결과중량 */
				goodsMeasure.setTariffQuantity(String.valueOf(data.getAccidentWt())); /** 사고중량 */
				item.setGoodsMeasure(goodsMeasure);

				DeclGOVCBRDKO.Packaging packaging = new DeclGOVCBRDKO.Packaging();
				packaging.setQuantityQuantity(String.valueOf(data.getExCt())); /** 적재화물목록포장개수 */
				packaging.setMarksNumbersID(String.valueOf(data.getCt())); /** 반입결과포장개수 */
				packaging.setConcurrentPackagingID(String.valueOf(data.getAccidentCt())); /** 사고포장개수 */
				item.setPackaging(packaging);

				DeclGOVCBRDKO.TransportContractDocumentHbl transportContractDocumentHbl = new DeclGOVCBRDKO.TransportContractDocumentHbl();
				transportContractDocumentHbl.setId(data.getBlNo()); /** House BL 번호 */
				transportContractDocumentHbl.setHouseBLSequenceID(data.getHsn()); /** HSN */
				item.setTransportContractDocument(transportContractDocumentHbl);

				consignmentItems.add(item);
			}
			consignment.setConsignmentItems(consignmentItems);
		}

		DeclGOVCBRDKO.TransportContractDocument transportContractDocument = new DeclGOVCBRDKO.TransportContractDocument();
		transportContractDocument.setId(abn.getMblNo()); /** Master BL 번호 */
		transportContractDocument.setMasterBLSequenceID(abn.getMsn()); /** MSN */
		consignment.setTransportContractDocument(transportContractDocument);

		decl.setConsignment(consignment);

		DeclGOVCBRDKO.PreviousDocument previousDocument = new DeclGOVCBRDKO.PreviousDocument();
		previousDocument.setId(abn.getMrn()); /** 적재화물관리번호 */
		previousDocument.setTypeCode(abn.getAirseaGbn()); /** 해상/항공전송구분 */
		decl.setPreviousDocument(previousDocument);

		return decl;
	}

	/**
	 * GOVCBRDKP(특송화물 반입물품 이상보고 정정신청서) 테이블 필드를 XML 변경용 객체와 매핑 처리
	 * @param abnChange
	 * @return
	 */
	public DeclGOVCBRDKP getBmsAbnormalCorrToGOVCBRDKP(CarryInAbnormalCorr abnChange) {
		DeclGOVCBRDKP decl = new DeclGOVCBRDKP();

		decl.setDeclarationOfficeID(abnChange.getOfficeId()); /** 세관/과부호 */
		decl.setId(abnChange.getCustomsReportNo()); /** 제출번호 */
		decl.setIssueDateTime(abnChange.getChangeDate()); /** 제출일자 */
		decl.setTypeCode(abnChange.getTypeCode()); /** 문서형태구분 */
		decl.setVersionID(abnChange.getChangeCount()); /** 정정차수 */

		DeclGOVCBRDKP.Amendment amendment = new DeclGOVCBRDKP.Amendment();
		amendment.setContent((String)abnChange.getChangeReason()); /** 정정사유 */
		decl.setAmendment(amendment);

		DeclGOVCBRDKP.Consignment consignment = new DeclGOVCBRDKP.Consignment();

		/** HBL 목록 조회 */
		List<CarryInAbnormalHblCorr> dataList = ediBmsSendMapper.selectEdiBmsAbnormalHblCorrSendList(abnChange.getAbnChangeNo());

		if(dataList.size() == 1 && dataList.get(0).getHsn().equals("")) {
			/**
			 * MBL:BL = 1:1인데 HSN 값이 없을 경우
			 * BL테이블 조회 결과 값을 MBL 엘리먼트에 맵핑하고 BL 관련 엘리먼트는 제외
			 */
			consignment.setGrossVolumeMeasure(String.valueOf(dataList.get(0).getExWt())); /** 적재화물목록중량 */
			consignment.setTotalPackageQuantity(String.valueOf(dataList.get(0).getExCt())); /** 적재화물목록포장개수 */
			consignment.setValueAmount(String.valueOf(dataList.get(0).getWt())); /** 반입결과중량 */
			consignment.setAbnormalityWeightMeasure(String.valueOf(dataList.get(0).getAccidentWt())); /** 사고중량 */
			consignment.setAbnormalityQuantityQuantity(String.valueOf(dataList.get(0).getAccidentCt())); /** 사고포장개수 */
			consignment.setResultsQuantity(String.valueOf(dataList.get(0).getCt())); /** 반입결과포장개수 */

			DeclGOVCBRDKP.AdditionalInformation additionalInformation = new DeclGOVCBRDKP.AdditionalInformation();
			additionalInformation.setStatementCode(dataList.get(0).getAccidentClass()); /** 사고/상이 내역부호 */
			additionalInformation.setStatementTypeCode(dataList.get(0).getChangeClass()); /** 정정구분 */
			additionalInformation.setStatementDescription((String)dataList.get(0).getRejectReason()); /** 이상유형상세내용 */
			consignment.setAdditionalInformation(additionalInformation);
		} else if((dataList.size() == 1 && !dataList.get(0).getHsn().equals("")) || dataList.size() > 1) {
			/**
			 * MBL:BL = 1:1이면서 HSN 값이 있거나 MBL:BL = 1:N일 경우
			 * MBL 관련 엘리먼트는 MBL_NO, MSN만 맵핑하고 BL 관련 엘리먼트는 size()만큼 반복 추가
			 */
			ConsignmentItemDKP item;
			List<ConsignmentItemDKP> consignmentItems = new ArrayList<ConsignmentItemDKP>();

			for(CarryInAbnormalHblCorr data : dataList) {
				item = new ConsignmentItemDKP();

				DeclGOVCBRDKP.AdditionalInformationHbl additionalInformationHbl = new DeclGOVCBRDKP.AdditionalInformationHbl();
				additionalInformationHbl.setStatementCode(data.getAccidentClass()); /** 사고/상이 내역부호 */
				additionalInformationHbl.setStatementDescription((String)data.getRejectReason()); /** 이상유형상세내용 */
				additionalInformationHbl.setStatementTypeCode(data.getChangeClass()); /** 정정구분 */
				item.setAdditionalInformation(additionalInformationHbl);

				DeclGOVCBRDKP.GoodsMeasure goodsMeasure = new DeclGOVCBRDKP.GoodsMeasure();
				goodsMeasure.setGrossMassMeasure(String.valueOf(data.getExWt())); /** 적재화물목록중량 */
				goodsMeasure.setNetNetWeightMeasure(String.valueOf(data.getWt())); /** 반입결과중량 */
				goodsMeasure.setTariffQuantity(String.valueOf(data.getAccidentWt())); /** 사고중량 */
				item.setGoodsMeasure(goodsMeasure);

				DeclGOVCBRDKP.Packaging packaging = new DeclGOVCBRDKP.Packaging();
				packaging.setQuantityQuantity(String.valueOf(data.getExCt())); /** 적재화물목록포장개수 */
				packaging.setMarksNumbersID(String.valueOf(data.getCt())); /** 반입결과포장개수 */
				packaging.setConcurrentPackagingID(String.valueOf(data.getAccidentCt())); /** 사고포장개수 */
				item.setPackaging(packaging);

				DeclGOVCBRDKP.TransportContractDocumentHbl transportContractDocumentHbl = new DeclGOVCBRDKP.TransportContractDocumentHbl();
				transportContractDocumentHbl.setId(data.getBlNo()); /** House BL 번호 */
				transportContractDocumentHbl.setHouseBLSequenceID(data.getHsn()); /** HSN */
				item.setTransportContractDocument(transportContractDocumentHbl);

				consignmentItems.add(item);
			}
			consignment.setConsignmentItem(consignmentItems);
		}

		DeclGOVCBRDKP.TransportContractDocument transportContractDocument = new DeclGOVCBRDKP.TransportContractDocument();
		transportContractDocument.setId(abnChange.getMblNo()); /** Master BL 번호 */
		transportContractDocument.setMasterBLSequenceID(abnChange.getMsn()); /** MSN */
		consignment.setTransportContractDocument(transportContractDocument);

		decl.setConsignment(consignment);

		DeclGOVCBRDKP.PreviousDocument previousDocument = new DeclGOVCBRDKP.PreviousDocument();
		previousDocument.setId(abnChange.getMrn()); /** 적재화물관리번호 */
		previousDocument.setTypeCode(abnChange.getAirseaGbn()); /** 해상/항공전송구분 */
		decl.setPreviousDocument(previousDocument);

		return decl;
	}
	
	public CargoProgressInfo getCargoProgressInfo(String cargMtNo) throws Exception {
		return this.getCargoProgressInfo(cargMtNo, null, null, null);
	}
	
	// cargMtNo: 화물관리번호, mblNo: MBL번호, hblNo: HBL번호, blYy: BL년도
	public CargoProgressInfo getCargoProgressInfo(String cargMtNo, String mblNo, String hblNo, String blYy) throws Exception {
		CargoProgressInfo cargoProgressInfo = null;

		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance()
				.scheme("https")
				.host(unipassBaseUrl_)
				.port(unipassPort_)
				.path(unipassCargoProgressPath_)
				.queryParam("crkyCn", URLDecoder.decode(unipassCargoProgressKey_, "UTF-8"));

		String _cargMtNo = Util.null2str(cargMtNo);	// 화물관리번호
		String _mblNo = Util.null2str(mblNo);				// MBL No
		String _hblNo = Util.null2str(hblNo);				// HBL No
		String _blYy = Util.null2str(blYy);				// BL 연도

		if (!"".equals(_cargMtNo))	uriComponentsBuilder.queryParam("cargMtNo", _cargMtNo);
		if (!"".equals(_mblNo))		uriComponentsBuilder.queryParam("mblNo", _mblNo);
		if (!"".equals(_hblNo))		uriComponentsBuilder.queryParam("hblNo", _hblNo);
		if (!"".equals(_blYy))		uriComponentsBuilder.queryParam("blYy", _blYy);


		URI uri = uriComponentsBuilder.build().encode().toUri();
		log.info("수입화물진행정보 API uri: {}", uri);

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
		ResponseEntity<String> response = restTemplate.getForEntity(uri,  String.class);

		String xml = response.getBody();

		JAXBContext jaxbContext = JAXBContext.newInstance(CargoProgressInfo.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		cargoProgressInfo = (CargoProgressInfo) jaxbUnmarshaller.unmarshal(new StringReader(xml));

		return cargoProgressInfo;
	}
	
	public CargoProgressInfo getCargoProgressInfoByParam(String cargoReferenceNo) throws Exception {
		return getCargoProgressInfoByParam(cargoReferenceNo, null, null, null);
	}
	public CargoProgressInfo getCargoProgressInfoByParam(String cargoReferenceNo, String arDate, String hblNo) throws Exception {
		return getCargoProgressInfoByParam(cargoReferenceNo, arDate, hblNo, null);
	}
	public CargoProgressInfo getCargoProgressInfoByParam(String cargoReferenceNo, String arDate, String hblNo, String mblNo) throws Exception {
		int thisYear = LocalDate.now().getYear();
		int lastYear = thisYear - 1;

		List<Integer> yearList = new ArrayList<>();
		yearList.add(thisYear);
		yearList.add(lastYear);
		yearList.add(lastYear - 1);

		String arDateStr = Util.null2str(arDate);
		String arYearStr = arDateStr.length() >= 4 ? arDateStr.substring(0, 4) : "";

		if (!"".equals(arYearStr)) {
		    int arYear = Util.null2int(arYearStr);
		    yearList.add(arYear);
		}
        
        String hblMblPattern = "";
        
        CargoProgressInfo cpInfo = null;
        if( !"".equals(Util.null2str(cargoReferenceNo)) ) {
        	log.info("Unipass CARGO_REFERENCE_NO 로 조회");
        	cpInfo = getCargoProgressInfo(cargoReferenceNo);
		}else {
			log.info("Unipass ELSE");
			for(int year : yearList) {
				log.info("Unipass ELSE -> HBL 로 조회");
				cpInfo = this.getCargoProgressInfo(null, null, hblNo, String.valueOf(year));
				if(cpInfo != null) {
					String tCnt = Util.null2str(cpInfo.getTCnt());
					if("".equals(tCnt) || (!"".equals(tCnt) && Integer.parseInt(tCnt) == 0)) {
						continue;
					}else {
						break;
					}
				}
			}
			
			for(int year : yearList) {
				log.info("Unipass ELSE -> MBL_NO 로 조회");
				cpInfo = this.getCargoProgressInfo(null, mblNo, null, String.valueOf(year));
				if(cpInfo != null) {
					String tCnt = Util.null2str(cpInfo.getTCnt());
					if("".equals(tCnt) || (!"".equals(tCnt) && Integer.parseInt(tCnt) == 0)) {
						continue;
					}else {
						log.info("Unipass MBL_NO -> CARGO_REFERENCE_NO 로 조회");
						List<CargCsclPrgsInfo> cargCsclPrgsInfo = cpInfo.getCrgCsclPrgsInfoList();
						if(cargCsclPrgsInfo != null && cargCsclPrgsInfo.size() > 0) {
							cpInfo = this.getCargoProgressInfo(cargCsclPrgsInfo.get(0).getCargMtNo());
							break;
						}
					}
				}
			}
		}
        
        if(cpInfo == null) {
        	log.info("Unipass NULL 조회실패");
			return cpInfo;
        }
        
        String tCnt = Util.null2str(cpInfo.getTCnt());
		if("".equals(tCnt) || (!"".equals(tCnt) && Integer.parseInt(tCnt) == 0)) { // 조회 갯수가 0이면 null로 리턴
			log.info("Unipass tCnt가 null or 0 : {}", cpInfo);
			cpInfo = null;
		}
        
        return cpInfo;
	}

}

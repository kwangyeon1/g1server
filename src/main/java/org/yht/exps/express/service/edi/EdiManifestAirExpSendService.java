package org.yht.exps.express.service.edi;

import java.io.BufferedWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.edi.ManifestMapper;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.service.CommonService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class EdiManifestAirExpSendService {

	@Autowired
	ManifestMapper manifestMapper;
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	private CustomProperties customProperties;
	@Autowired
	RestHighLevelClient client;
	
	@Resource(name="ediCommonService")
	EdiCommonService ediCommonService;
	@Resource(name = "commonService")
	CommonService commonService;
	
	/**
	 * 항공수출 적하목록신고 문서송신
	 * 관세청 4세대 전용
	 * 
	 * @param docSend
	 * @param fwSendAccInfo
	 * @param errList
	 * @param result
	 * @return
	 * @throws java.lang.Exception
	 */
	public boolean setManifestAirExpSend4th(Map<String, Object> docSend, Map<String, Object> tempMblData, Map<String, Object> fwSendAccInfo, List<Map<String, Object>> errList, Map<String, Object> result, HttpServletRequest request) throws java.lang.Exception {
		List<Map<String, Object>> tempHblDataList = null;	// H.B/L 정보 리스트
		Map<String, Object> ediHostSendInfo = null;			// 본지사정보 가져오기( 중계사업자 송신 정보 )
		
		String message = null;
		
		// 에러 메시지 가져오기 조건 세팅
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
		tempSearchData.put("entCode", Util.null2str(docSend.get("entCode")));
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		
		//////////////////////////////////////////
		// 서비스코드 및 중계사업자 체크
		//////////////////////////////////////////
		if ("".equals(Util.null2str(docSend.get("ifServiceCode")))) {
			tempSearchData.put("srchCode", "MF-51");	// 현재 지원되지 않는 인터페이스 스펙(서비스코드) 입니다.
			message = ediCommonService.getErrorCodeName(tempSearchData);
			
			errList.add(ediCommonService.getErrorMessage("본지사정보", Util.null2str(docSend.get("ediVender")) + "(" + Util.null2str(docSend.get("docType")) + ") : "+ message));
			
			ediCommonService.setValidateMessage(docSend, tempMblData, errList, result);
			
			return false;
		}
		
		if ("".equals(Util.null2str(docSend.get("ediVender")))) {
			tempSearchData.put("srchCode", "MF-50");	// 해당 M.B/L에 중개사업자가 없습니다.
			message = ediCommonService.getErrorCodeName(tempSearchData);
			
			errList.add(ediCommonService.getErrorMessage(Util.null2str(docSend.get("mblNo")), "M.B/L : " + message));
			
			ediCommonService.setValidateMessage(docSend, tempMblData, errList, result);
			
			return false;
		}
		
		if("epninf".equals(docSend.get("action")) && !"KTNET".equals(tempMblData.get("EDI_VENDER"))){
			errList.add(ediCommonService.getErrorMessage(Util.null2str(tempMblData.get("MBL_NO")), "수출신고번호간이정보전송은 KTNET만 가능합니다."));
			ediCommonService.setValidateMessage(docSend, tempMblData, errList, result);
			return false;
		}
		
		//////////////////////////////////////////
		// 전송대상 B/L 자료 조회
		//////////////////////////////////////////
//		tempMblData = ediCommonService.getEdiManifestValidatorMblOdInfo(docSend);		// 적하목록 전송 오류검증 M.B/L 오더정보 조회
		
		if ("".equals(Util.null2str(tempMblData.get("MASTER_OD_JOB_NO")).trim())) {
			tempSearchData.put("srchCode", "MF-125");	// SR No. 값이 입력되지 않았습니다.
			message = ediCommonService.getErrorCodeName(tempSearchData);
			
			errList.add(ediCommonService.getErrorMessage("시스템정보", message));
			
			ediCommonService.setValidateMessage(docSend, tempMblData, errList, result);
			
			return false;
		}
		
		tempHblDataList = ediCommonService.getEdiManifestValidatorHblOdList(docSend);	// 적하목록 전송 오류검증 H.B/L 오더정보 조회
		
		//////////////////////////////////////////
		// 본/지사 정보 가져오기( 중개사업자별 문서 및 송/수신 정보, TB_SMS_ENT_OFFICE_INTERFACE )
		//////////////////////////////////////////				
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(docSend, "ifServiceCodeIn", false, null, null, null, null);
		ediHostSendInfo = manifestMapper.getEdiInterfaceInfo(docSend);
		//////////////////////////////////////////
		// 오류검증 및 파일 생성후 전송
		//////////////////////////////////////////
		if("epninf".equals(docSend.get("action"))){
			if (!this.getEpninf(docSend, tempMblData, tempHblDataList, fwSendAccInfo, ediHostSendInfo, errList, result, request)) {
				return false;
			}
		}else {
			if (!this.getManifestAirExpSend4th(docSend, tempMblData, tempHblDataList, fwSendAccInfo, ediHostSendInfo, errList, result, request)) {
				return false;
			}
		}
		
		
		// 정상처리:0000
		result.put("processCode", "0000");
		
		return true;		
	}
	
	/**
	 * 오류검증 및 파일 생성후 전송
	 * 관세청 4세대 전용
	 * 
	 * @param docSend
	 * @param tempMblData
	 * @param tempHblDataList
	 * @param fwSendAccInfo
	 * @param ediHostSendInfo
	 * @param errList
	 * @param result
	 * @return
	 * @throws java.lang.Exception
	 */
	public boolean getManifestAirExpSend4th(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempHblDataList, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo, List<Map<String, Object>> errList, Map<String, Object> result, HttpServletRequest request) throws java.lang.Exception {

		String fileCreateRetVal[] = null;									// 파일 생성후 결과값, [0]:파일명, [1]:로컬파일경로, [2]:파일내용
		//////////////////////////////////////////
		// 오류 검증
		//////////////////////////////////////////
		if (!this.getManifestAirExpSendDataValidate4th(docSend, tempMblData, tempHblDataList, fwSendAccInfo, ediHostSendInfo, errList, result)) {
			return false;
		}
		
		if("errorCheck".equals(docSend.get("action"))){
			return true;
		}
		
		//////////////////////////////////////////
		// 파일 생성
		//////////////////////////////////////////
		fileCreateRetVal = this.getSendDataFileCreate4th(docSend, tempMblData, tempHblDataList, fwSendAccInfo, ediHostSendInfo);
		
		
		//////////////////////////////////////////
		// 파일 전송
		//////////////////////////////////////////
		String fileSendYn = "N";
		if ("Y".equals(customProperties.getEdiSendYn())) {	//20210811 기존g1logis소스  if ("Y".equals(Prop.getMessage(profileCls.getProfileName() + "_" + "AIR_EXP_MANIFEST_SEND"))) {
			
			/** KCNET 웹서비스 사용하지 않음 */
//			if(Util.null2str(docSend.get("ediVender")).equals("KCNET")){
//				try {
//					//테스트 http://121.138.113.7:12101 리얼 http://222.122.233.22:12101
//					String httpPost = Util.null2str(ediHostSendInfo.get("IF_SVR_IP")) + ":" + Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")) + "/WebServices/services/XmlDataService";
////					HttpPost blPost = new HttpPost(httpPost);
////					blPost.setHeader("Content-type", "text/xml;charset=utf-8");
//					HttpClient client = new DefaultHttpClient();
//					
//					HttpPost post = new HttpPost(httpPost);
//					post.setHeader("SOAPAction", "");
//			        post.setEntity(new StringEntity(fileCreateRetVal[1], HTTP.UTF_8));
//			        HttpResponse responseBl = client.execute(post);
//			        fileSendYn = "Y";
//				}catch(Exception e) {
//					throw e;
//				}
//			}else {
				if (ediCommonService.ftpFileSend(docSend, tempMblData, fileCreateRetVal, ediHostSendInfo, errList, result)) {
					fileSendYn = "Y";
				}
//			}
		}else {
			fileSendYn = "Y";
		}
		
		// 원본파일 이동
		String outFileName = ediCommonService.moveFile(fileCreateRetVal[0], fileCreateRetVal[0], fileCreateRetVal);
				
		//////////////////////////////////////////
		// 오더 EDI 상태값 업데이트
		//////////////////////////////////////////
		if(fileSendYn.equals("Y")){
			//적하목록수출 일반건만 변경되게
			docSend.put("cdType", "N");
			ediCommonService.setOdEdiStatus(docSend, tempMblData, tempHblDataList);
			manifestMapper.updateMrnResultType(tempMblData);
		}
		
		//////////////////////////////////////////
		// M.B/L EDI 송신 로그기록
		//////////////////////////////////////////
		//this.setEdiMblLog(docSend, tempMblData);
		
		//////////////////////////////////////////
		// 송신결과 로그기록(송신 원본파일 저장)
		//////////////////////////////////////////
//		ediCommonService.setInterfaceLog(docSend, ediHostSendInfo, fileCreateRetVal[1], "");
		
		/** 처리 로그 기록 */
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		String logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
		String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
		
		tmpMap.clear();
		tmpMap.put("ent_code", docSend.get("entCode"));
		tmpMap.put("customer_code", "");
		tmpMap.put("solution", "ECM");
		tmpMap.put("bound", docSend.get("bound"));
		tmpMap.put("bound_type", docSend.get("boundType"));
		tmpMap.put("hbl_no", "");
		tmpMap.put("if_service_code", docSend.get("ifServiceCode"));
//		tmpMap.put("if_doc_code", fileCreateRetVal[4]);
		tmpMap.put("if_doc_code", "MANIFEST");
		tmpMap.put("if_doc_name", "적하목록");
		if(docSend.get("houseOdJobNoList") == null){
			tmpMap.put("if_doc_reference_no", fileCreateRetVal[5]);
		}else{
			tmpMap.put("if_doc_reference_no", fileCreateRetVal[5] + "(" + ((List)docSend.get("houseOdJobNoList")).size() + ")");
		}
		tmpMap.put("if_vender", docSend.get("ediVender"));
		tmpMap.put("if_send_recv_type", "S");
		tmpMap.put("if_send_recv_method", ediHostSendInfo.get("IF_S_R_TYPE"));
		tmpMap.put("if_file_path", fileCreateRetVal[3]);
		tmpMap.put("if_file_name", outFileName);
		tmpMap.put("if_status", "SEND");
		tmpMap.put("if_status_info", "");
		tmpMap.put("if_sender_id", ediHostSendInfo.get("IF_SENDER_ID"));
		tmpMap.put("if_receiver_id", ediHostSendInfo.get("IF_RECEIVER_ID"));
		tmpMap.put("if_date", DateTimeUtil.getNow("yyyy-MM-dd"));
		tmpMap.put("if_date_time", DateTimeUtil.getNow("HH:mm:ss"));
		tmpMap.put("if_remark", "");
		tmpMap.put("register_user_id", docSend.get("registerUserId"));
		tmpMap.put("register_user_name", docSend.get("registerUserName"));
		tmpMap.put("register_date_time", System.currentTimeMillis());
		tmpMap.put("es_job_no", logId);
			
		IndexApi indexApi = new IndexApi(client);
		if (!indexApi.existIndex(index))	indexApi.createIndex(index);
		
		DocumentApi documentApi = new DocumentApi(client);
		documentApi.createDocument(index, logId, tmpMap);
		
		/** UPS Tracking Send */
		if(fileSendYn.equals("Y")){
			if("YHTXX".equals(docSend.get("entCode")) || "UPSXK".equals(docSend.get("entCode"))){
				index = "ecm-ups-tracking-" + Util.null2str(docSend.get("entCode")).toLowerCase();
				if (!indexApi.existIndex(index))	indexApi.createIndex(index);
				ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
				BulkData bulkData = null;
				Map<String, Object> logMap = null;
				int seq = 1;
				long trackingDateTime = System.currentTimeMillis();
				
				for(Map<String, Object> tracking : tempHblDataList){
					bulkData = new BulkData();
					bulkData.setActionType(BulkData.Type.CREATE);
					bulkData.setIndexName(index);
					bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
					logMap = new HashMap<String, Object>();
					logMap.put("OrdId", tracking.get("PO_NO"));
					logMap.put("MBLNo", tracking.get("MBL_NO"));
					logMap.put("SubmissionNo", tracking.get("SIMPLE_DOC_NO"));
					logMap.put("HBLNo", tracking.get("HBL_NO"));
					logMap.put("HSN", tracking.get("EDI_HSN"));
					logMap.put("TrackingCode", "CMP");
					logMap.put("TrackingDateTime", trackingDateTime);
					logMap.put("TrackingRemark", "");
					logMap.put("ShipperBusinessId", tracking.get("SHIPPER_BUSINESS_ID"));
					logMap.put("BoundType", docSend.get("boundType"));
					bulkData.setJsonMap(logMap);
					bulkList.add(bulkData);
				}
				
				if(bulkList.size() > 0){
					documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
				}
			}
			
//			tmpMap.clear();
//			tmpMap.put("entCode", docSend.get("entCode"));
//			tmpMap.put("entBusinessCode", docSend.get("entBusinessCode"));
//			tmpMap.put("gmtTime", docSend.get("gmtTime"));
//			tmpMap.put("userId", docSend.get("updateUserId"));
//			tmpMap.put("odJobNo", docSend.get("odJobNo"));
//			tmpMap.put("bound", docSend.get("bound"));
//			tmpMap.put("boundType", docSend.get("boundType"));
//			tmpMap.put("blNo", docSend.get("mblNo"));
//			commonService.sendKakaoAlarmGroup(tmpMap, "MANIFEST");
		}
		
		return true;
	}
	
	/**
	 * 전송내역 오류검증
	 * 
	 * //////     AIRCIS 특수 문자
	    //////1.Commodity
	    //////    A.   영문자, 숫자, Space 만 허용(hyphen 및 full stop(.) 불가)
	    //////2.   SHP/CNE 공통
	    //////    A.   Address - Airline Code가 AZ 인 경우 영문자/숫자만 허용    ---> -  .  들어가게함.
	    //////    B.   Telephone -  영문자, 숫자만 허용 (Space 불가)  --->옵션이라 문서에 미포함
	    //////3.   SHP
	    //////    A.   Post No - Airline Code가 AZ 인 경우 필수(영문자/숫자만 허용)
	    //////4.   CNE
	    //////    A.   State : CNE의 Country Code 가 US, CA 인 경우 필수
	    //////    B.   Post No. 
	    //////        i.    CNE의 Country Code 가 CA, DE, AT, US 인 경우 필수
	    //////        ii.   DE, US - 숫자 5자리
	    //////        iii.   AT - 숫자 4자리
	    //////        iv.   Airline Code가 AZ 인 경우 필수(영문자/숫자만 허용)---> -  .  들어가게함
	
	    //////v CNE의 국가 코드가 브라질(BR)과 멕시코(MX) 인 경우 주 코드(State Code) 2자리 필수
	    //////v 국가코드가 캐나다(CA)인 경우 우편번호 체크사항
	    //////    ü 우편번호가 6자리인 경우 
	    //////        - 숫자와 영문 대문자를 혼합한 6자리(특수문자 사용금지) 
	    //////    ü 우편번호가 7자리인 경우 
	    //////        - 3자(알파벳숫자알파벳)+빈칸+3자(숫자알파벳숫자) - ex) V6W 1J8
	
	    //////Cargo IMP에 허용되는 문자는 아래와 같습니다.
	    //////    -    알파벳 A~Z(대문자만)
	    //////    -    숫자 0~9
	    //////    -    특수문자 Space, “.”, “-“, CRLF
	    //////    -    “/”는 구분자
     * 
	 * @param docSend
	 * @param tempMblData
	 * @param tempHblDataList
	 * @param fwSendAccInfo
	 * @param ediHostSendInfo
	 * @param errList
	 * @param result
	 * @return
	 * @throws java.lang.Exception
	 */
	public boolean getManifestAirExpSendDataValidate4th(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempHblDataList, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo, List<Map<String, Object>> errList, Map<String, Object> result) throws java.lang.Exception {
		
		String message = null;
		
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("entCode", Util.null2str(docSend.get("entCode")));
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
		
		///////////////////////////
		// HostInfo And BaseData Check
		///////////////////////////
		if (!ediCommonService.getSendHostAndBaseDataValidate(docSend, tempMblData, fwSendAccInfo, ediHostSendInfo, errList, result)) {
			return false;
		}
					
		///////////////////////////
		// Master B/L Check
		///////////////////////////
        String mblNoStr = Util.null2str(tempMblData.get("MBL_NO")).trim(); //중복 변수 지정 - ditobass!!@
		String mblNo = new StringBuffer(mblNoStr.substring(0, 3)).append("-").append(mblNoStr.substring(3)).toString();
		
		/** mblNo 11자리인지 검증 추가 */
		if (mblNoStr.length() != 11) {
            tempSearchData.put("srchCode", "MF-04");	// MASTER AWB번호가 11자리인지 확인하십시오.
            message = ediCommonService.getErrorCodeName(tempSearchData);

            errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		
		if (!Util.isNumber(mblNoStr)) {
            tempSearchData.put("srchCode", "MF-05");	// Master AWB번호에 영문자가 포함되어 있어습니다.
            message = ediCommonService.getErrorCodeName(tempSearchData);

            errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		
		if ("".equals(Util.null2str(tempMblData.get("LINE_CODE")).trim())) {
            tempSearchData.put("srchCode", "MF-06");	// 항공사를 기재하기 바랍니다.
            message = ediCommonService.getErrorCodeName(tempSearchData);
            
            errList.add(ediCommonService.getErrorMessage(mblNo, "M.B)/L : " + message));
		}
		
		if (Util.null2str(tempMblData.get("VESSEL_FLIGHT")).trim().length() < 3) {
            tempSearchData.put("srchCode", "MF-07");	// 편명은 최소 3자리 이상이어야만 합니다.
            message = ediCommonService.getErrorCodeName(tempSearchData);

            errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
	 
		if ("".equals(Util.null2str(tempMblData.get("OB_DATE")).trim())) {
            tempSearchData.put("srchCode", "MF-08");	// 출항일을 확인하십시오
            message = ediCommonService.getErrorCodeName(tempSearchData);

            errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		
		if ("".equals(Util.null2str(tempMblData.get("ORIG_CODE")).trim())) {
            tempSearchData.put("srchCode", "MF-09");	// Master 출발지를 기재하기 바랍니다.
            message = ediCommonService.getErrorCodeName(tempSearchData);

            errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		
		if ("".equals(Util.null2str(tempMblData.get("DEST_CODE")).trim())) {
            tempSearchData.put("srchCode", "MF-10");	// Master 도착지를 기재하기 바랍니다.
            message = ediCommonService.getErrorCodeName(tempSearchData);

            errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		
		if (Util.null2int(tempMblData.get("PIECES")) <= 0) {
            tempSearchData.put("srchCode", "MF-11");	// Master 수량을 기재하기 바랍니다.
            message = ediCommonService.getErrorCodeName(tempSearchData);

            errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		
		if (new BigDecimal(Util.null2str(tempMblData.get("GWT"), "0")).doubleValue() <= 0) {
            tempSearchData.put("srchCode", "MF-12");	// Master 중량을 기재하기 바랍니다.
            message = ediCommonService.getErrorCodeName(tempSearchData);

            errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		
		///////////////////////////
		//House B/L Check
		///////////////////////////
		List<Map<String, Object>>  customsLicenseList = null;
		Map<String, Object> tempMapSearchData = new HashMap<String, Object>();
		Map<String, Object> tempCustomsLicenseInfo = null;
		Map<String, Object> tempUpdateData = null;
		Map<String, Object> tempHblRow = null;
		
		String hblNo = null;
		String hsn = null;
		String checkHblNo = null;
		String consigneeCountryCodeStr = null; //중복 변수 지정 - ditobass!!@
        String consigneeZipcodeStr = null; //중복 변수 지정 - ditobass!!@
        String codeDetailDestCountryCodeStr = null; //중복 변수 지정 - ditobass!!@
        BigDecimal gwtBD = null; //중복 변수 지정 - ditobass!!@
		
		if (tempHblDataList.size() <= 0) {
            tempSearchData.put("srchCode", "MF-43");	// 해당 M.B/L에 H.B/L이 한 건도 없습니다.
            message = ediCommonService.getErrorCodeName(tempSearchData);

            errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		for (int i = 0; i < tempHblDataList.size(); ++i) {
            tempHblRow = tempHblDataList.get(i);
            hblNo = Util.null2str(tempHblRow.get("HBL_NO")).trim();
            hsn = Util.null2str(tempHblRow.get("EDI_HSN"));
            checkHblNo = ediCommonService.getHblDeleteChar(Util.null2str(tempHblRow.get("HBL_NO")).trim());
            consigneeCountryCodeStr = Util.null2str(tempHblRow.get("CONSIGNEE_COUNTRY_CODE")).trim(); //중복 변수 지정 - ditobass!!@
            consigneeZipcodeStr = Util.null2str(tempHblRow.get("CONSIGNEE_ZIPCODE")).trim(); //중복 변수 지정 - ditobass!!@
            codeDetailDestCountryCodeStr = Util.null2str(tempHblRow.get("CODE_DETAIL_DEST_COUNTRY_CODE")).trim(); //중복 변수 지정 - ditobass!!@
            gwtBD = new BigDecimal(Util.null2str(tempHblRow.get("GWT"), "0")); //중복 변수 지정 - ditobass!!@
            
            if ("".equals(Util.null2str(tempHblRow.get("EDI_ORIG_COUNTRY")).trim())) {
                tempSearchData.put("srchCode", "MF-47");	// House 발송국이 입력되지 않았습니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
            
            if ("".equals(Util.null2str(tempHblRow.get("EDI_DEST_COUNTRY")).trim())) {
                tempSearchData.put("srchCode", "MF-46");	// House 목적국이 입력되지 않았습니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
            
            if ("".equals(checkHblNo)) {
                tempSearchData.put("srchCode", "MF-13");	// House AWB번호를 기재하기 바랍니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
            
            if ("".equals(Util.null2str(tempHblRow.get("EDI_HSN")).trim())) {
                tempSearchData.put("srchCode", "MF-15");	// HSN을 기재하기 바랍니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
            
            if (Util.null2int(tempHblRow.get("PIECES")) <= 0) {
                tempSearchData.put("srchCode", "MF-16");	// House 수량을 기재하기 바랍니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
            
            if (gwtBD.doubleValue() <= 0) {
                tempSearchData.put("srchCode", "MF-17");	// House 중량을 기재하기 바랍니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
            
            if ("".equals(Util.null2str(tempHblRow.get("EDI_DEST_PORT")).trim())) {
                tempSearchData.put("srchCode", "MF-18");	// AMS 목적항을 기재하기 바랍니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
            
            // 대표품목
            if ("".equals(Util.null2str(tempHblRow.get("COMMODITY_ITEM")).trim())) {
                tempSearchData.put("srchCode", "MF-19");	// Commodity 품명을 기재하기 바랍니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
            
            // 상세품목
            if ("".equals(Util.null2str(tempHblRow.get("COMMODITY_ITEM")).trim())) {
                tempSearchData.put("srchCode", "MF-20");	// Item desc 품명을 기재하기 바랍니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
            
            if ("".equals(Util.null2str(tempHblRow.get("SHIPPER_NAME")).trim())) {
                tempSearchData.put("srchCode", "MF-21");	// 송하인 상호를 기재하기 바랍니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
            
            if ("".equals(Util.null2str(tempHblRow.get("SHIPPER_COUNTRY_CODE")).trim())) {
                tempSearchData.put("srchCode", "MF-22");	// 송하인 국적을 기재하기 바랍니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
            
            if ("".equals(Util.null2str(tempHblRow.get("SHIPPER_PLACE")).trim())) {
                tempSearchData.put("srchCode", "MF-23");	// 송하인 Place(도/시명)를 기재하기 바랍니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
            
            if ("".equals(Util.null2str(tempHblRow.get("SHIPPER_STREET")).trim())) {
                tempSearchData.put("srchCode", "MF-24");	// 송하인 Street(상세주소)를 기재하기 바랍니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
		
            // Post No - Airline Code가 AZ 인 경우 필수(영문자/숫자만 허용)
            if ("AZ".equals(Util.null2str(tempHblRow.get("LINE_CODE")).trim())) {
                if ("".equals(Util.null2str(tempHblRow.get("SHIPPER_ZIPCODE")).trim())) {
                    tempSearchData.put("srchCode", "MF-25");	// 송하인 Zip Code(우편번호)를 기재하기 바랍니다.
                    message = ediCommonService.getErrorCodeName(tempSearchData);
                
                    errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                }
            }
            
            if ("".equals(Util.null2str(tempHblRow.get("SHIPPER_ADDRESS")).trim())) {
                tempSearchData.put("srchCode", "MF-86");	// 송하인 주소를 기재하기 바랍니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
            
            if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_NAME")).trim())) {
                tempSearchData.put("srchCode", "MF-26");	// 수하인 상호를 기재하기 바랍니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
            
            if ("".equals(consigneeCountryCodeStr)) {
                tempSearchData.put("srchCode", "MF-27");	// 수하인 국적을 기재하기 바랍니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
            
            //UPS인경우 AMS가 Y일때만 필수 체크한다. **/
            if("UPSXK".equals(Util.null2str(docSend.get("entCode"))) && "M".equals(Util.null2str(tempHblRow.get("AMS_YN")))) {
            	//CNE의 국가 코드가 브라질(BR)과 멕시코(MX) 인 경우 주 코드(State Code) 2자리 필수
            	if ("US".equals(consigneeCountryCodeStr) || "CA".equals(consigneeCountryCodeStr) || "BR".equals(consigneeCountryCodeStr) || "MX".equals(consigneeCountryCodeStr)) {
            		if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_STATE_CODE")).trim())) {
            			tempSearchData.put("srchCode", "MF-28");	// 수하인 State(주)명을 기재하기 바랍니다.
            			message = ediCommonService.getErrorCodeName(tempSearchData);
            			
            			errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            		}
            	}
            	
            	//State : CNE의 Country Code 가 US, CA 인 경우 필수
            	if (!ediCommonService.checkUserStateCode(consigneeCountryCodeStr, Util.null2str(tempHblRow.get("CONSIGNEE_STATE_CODE")).trim(), Util.null2str(docSend.get("ifServiceCode")).trim())) {
            		tempSearchData.put("srchCode", "MF-29");	// 수하인 국가코드가 CA,US인 경우 State(주)명가 2자리어야 합니다.
            		message = ediCommonService.getErrorCodeName(tempSearchData);
            		
            		errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            	}
            }
            
           //UPS인경우 AMS가 Y일때만 필수 체크한다. **/
            if("UPSXK".equals(Util.null2str(docSend.get("entCode"))) && "M".equals(Util.null2str(tempHblRow.get("AMS_YN")))) {
            	//CNE의 Country Code 가 CA, DE, AT, US 인 경우 필수
	            if ("CA".equals(consigneeCountryCodeStr) || "DE".equals(consigneeCountryCodeStr) || "AT".equals(consigneeCountryCodeStr) || "US".equals(consigneeCountryCodeStr)) {
	                if ("".equals(consigneeZipcodeStr)) {
	                    tempSearchData.put("srchCode", "MF-30");	// 수하인 국가코드가 CA,DE,AT,US인 경우 Zip Code(우편번호)를 기재하기 바랍니다.
	                    message = ediCommonService.getErrorCodeName(tempSearchData);
	                
	                    errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
	                }
	            }
	            
	            // DE, US - 숫자 5자리
	            if ("DE".equals(consigneeCountryCodeStr) || "US".equals(consigneeCountryCodeStr)) {
	                if (consigneeZipcodeStr.length() != 5) {
	                    tempSearchData.put("srchCode", "MF-31");	// 수하인 국가코드가 DE,US인 경우 Zip Code(우편번호)가 5자리어야 합니다.
	                    message = ediCommonService.getErrorCodeName(tempSearchData);
	                
	                    errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
	                }
	            }
	            
	            //AT - 숫자 4자리
	            if ("AT".equals(consigneeCountryCodeStr)) {
	                if (consigneeZipcodeStr.length() != 4) {
	                    tempSearchData.put("srchCode", "MF-32");	// 수하인 국가코드가 AT인 경우 Zip Code(우편번호)가 4자리어야 합니다.
	                    message = ediCommonService.getErrorCodeName(tempSearchData);
	                
	                    errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
	                }
	            }
	            
	            //CA - 1.우편번호가 6자리인 경우 - 숫자와 영문 대문자를 혼합한 6자리(특수문자 사용금지)
	            //     2.우편번호가 7자리인 경우 - 3자(알파벳숫자알파벳)+빈칸+3자(숫자알파벳숫자) - ex) V6W 1J8
	            if ("CA".equals(consigneeCountryCodeStr)) {
	                if (!(consigneeZipcodeStr.length() == 6 || consigneeZipcodeStr.length() == 7)) {
	                    tempSearchData.put("srchCode", "MF-33");	// 수하인 국가코드가 CA인 경우 Zip Code(우편번호)가 6자리 혹은 7 자리어야 합니다.
	                    message = ediCommonService.getErrorCodeName(tempSearchData);
	                
	                    errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
	                }
	            }
            }
            
            // Post No - Airline Code가 AZ 인 경우 필수(영문자/숫자만 허용)
            if ("AZ".equals(Util.null2str(tempHblRow.get("LINE_CODE")).trim())) {
                if ("".equals(consigneeZipcodeStr)) {
                    tempSearchData.put("srchCode", "MF-34");	// 수하인 Zip Code(우편번호)를 기재하기 바랍니다.
                    message = ediCommonService.getErrorCodeName(tempSearchData);
                
                    errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                }
            }
            
            if ("ID".equals(codeDetailDestCountryCodeStr)
                && ("CN".equals(Util.null2str(tempHblRow.get("VIA_TO_A_COUNTRY_CODE")).trim())
                    || "CN".equals(Util.null2str(tempHblRow.get("VIA_TO_B_COUNTRY_CODE")).trim())
                    || "CN".equals(Util.null2str(tempHblRow.get("VIA_TO_C_COUNTRY_CODE")).trim()))) {
                tempSearchData.put("srchCode", "");
                tempSearchData.put("message", "경유지가 중국, 도착지가 인도인 경우 확인 바랍니다.");
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
            
            //=============================================================================================================================================
		
            // 중국세관용 FHL 오류 검증 [ START ]===================================================================================================================================
            if ("Y".equals(Util.null2str(tempHblRow.get("CHINA_SPEC_ADD_YN")).trim()))  {
                if ("CN".equals(codeDetailDestCountryCodeStr)
                    || "CN".equals(Util.null2str(tempHblRow.get("VIA_TO_A_COUNTRY_CODE")).trim())
                    || "CN".equals(Util.null2str(tempHblRow.get("VIA_TO_B_COUNTRY_CODE")).trim())
                    || "CN".equals(Util.null2str(tempHblRow.get("VIA_TO_C_COUNTRY_CODE")).trim())) {
                    
                    // SHIPPER 항목 체크
                    if ("".equals(Util.null2str(tempHblRow.get("SHIPPER_TEL")).trim())) {
                        tempSearchData.put("srchCode", "");
                        tempSearchData.put("message", "중국세관용 - Shipper 전화번호를 입력해주세요");
                        message = ediCommonService.getErrorCodeName(tempSearchData);
                        
                        errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                    }
                    
                    // CONSIGNEE 항목 체크
                    if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_TEL")).trim())) {
                        tempSearchData.put("srchCode", "");
                        tempSearchData.put("message", "중국세관용 - Consignee 전화번호를 기재하기 바랍니다.");
                        message = ediCommonService.getErrorCodeName(tempSearchData);
                        
                        errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                    }
                }
            }
            // 중국세관용 FHL 오류 검증 [ E N D ]=============================================================================================================================================
		
            // 인도네시아용 FHL 오류 검증 [ START ]===================================================================================================================================
            if ("Y".equals(Util.null2str(tempHblRow.get("INDONESIA_SPEC_ADD_YN")).trim())) {
                if ("ID".equals(codeDetailDestCountryCodeStr)) {
                    
//                    if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_BUSINESS_ID")).trim())) {
//                        tempSearchData.put("srchCode", "");
//                        tempSearchData.put("message", "인도네시아용 - Consignee 사업자등록번호를 기재하기 바랍니다.");
//                        message = ediCommonService.getErrorCodeName(tempSearchData);
//                        
//                        errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
//                    }
                    
                    if (15 < Util.null2str(tempHblRow.get("CONSIGNEE_BUSINESS_ID")).trim().length()) {
                        tempSearchData.put("srchCode", "");
                        tempSearchData.put("message", "인도네시아용 - Consignee 사업자등록번호는 최대 15자리입니다.");
                        message = ediCommonService.getErrorCodeName(tempSearchData);
                        
                        errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                    }
                }
            }
            // 인도네시아용 FHL 오류 검증 [ E N D ]=============================================================================================================================================
		
            // 방글라데시 FHL 오류 검증 [ START ]===================================================================================================================================
            if ("Y".equals(Util.null2str(tempHblRow.get("BD_SPEC_ADD_YN")).trim())) {
                if ("BD".equals(codeDetailDestCountryCodeStr)/* && !"SS".equals(Util.null2str(tempMblData.get("BL_TYPE_SC")).trim())*/) {
                    if (!Util.isNumber(Util.null2str(tempHblRow.get("DV_CUSTOMS")).trim())) {
                        tempSearchData.put("srchCode", "");
                        tempSearchData.put("message", "방글라데시용 - Declared Value for Customs 금액을 확인해주세요(빈값/글자포함 여부)");
                        message = ediCommonService.getErrorCodeName(tempSearchData);
                        
                        errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                    }
                }
            }
            // 방글라데시 FHL 오류 검증 [ E N D ]=============================================================================================================================================
            if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_PLACE")).trim())) {
                tempSearchData.put("srchCode", "MF-35");	// 수하인 Place(도/시명)를 기재하기 바랍니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
            
            if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_STREET")).trim())) {
                tempSearchData.put("srchCode", "MF-36");	// 수하인 Street(상세주소)를 기재하기 바랍니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
		
            // 폴란드 FHL 오류 검증 [ START ]===================================================================================================================================
            if ("PL".equals(codeDetailDestCountryCodeStr) && "Y".equals(Util.null2str(tempHblRow.get("PL_SPEC_ADD_YN")).trim())) {
                
                // Zip Code 체크
                if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_ZIPCODE")).trim())) {
                    tempSearchData.put("srchCode", "");
                    tempSearchData.put("message", "폴란드용 -  도착지가 폴란드일 경우 Consignee Zip Code(우편번호)를 기재하기 바랍니다.");
                    message = ediCommonService.getErrorCodeName(tempSearchData);
                    
                    errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                    
                } else if (!ediCommonService.checkUserZipCodeDigit(codeDetailDestCountryCodeStr, Util.null2str(tempHblRow.get("CONSIGNEE_ZIPCODE")).trim(), Util.null2str(docSend.get("ifServiceCode")).trim())) {
                    tempSearchData.put("srchCode", "");
                    tempSearchData.put("message", "폴란드용 - Consignee Zip Code 형식은 01-234, 01 234, 01234 기준 입니다.");
                    message = ediCommonService.getErrorCodeName(tempSearchData);
         
                    errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                }
                
                // Zip Code 체크
                if ("".equals(Util.null2str(tempHblRow.get("SHIPPER_ZIPCODE")).trim())) {
                    tempSearchData.put("srchCode", "");
                    tempSearchData.put("message", "폴란드용 -  도착지가 폴란드일 경우 Shipper Zip Code(우편번호)를 기재하기 바랍니다.");
                    message = ediCommonService.getErrorCodeName(tempSearchData);
                    
                    errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                } else if ("KR".equals(Util.null2str(tempHblRow.get("SHIPPER_COUNTRY_CODE")).trim())
                        && !ediCommonService.checkUserZipCodeDigit(Util.null2str(tempHblRow.get("SHIPPER_COUNTRY_CODE")).trim()
                        , Util.null2str(tempHblRow.get("SHIPPER_ZIPCODE")).trim()
                        , Util.null2str(docSend.get("ifServiceCode")).trim())) {
                    tempSearchData.put("srchCode", "");
                    tempSearchData.put("message", "폴란드용 - Shipper Zip Code 형식은 01234 기준 입니다.");
                    message = ediCommonService.getErrorCodeName(tempSearchData);
         
                    errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                }
            }
            // 폴란드 FHL 오류 검증 [ E N D ]=============================================================================================================================================
            
            /** 2026-06-23 AMS여부가 Y이면 방글라데시, 인도 수하인 사업자번호 필수 */
            /** 하우스 조회 쿼리에서 AMS가 Y면 M이고 없거나N이면 I로 나오게 되어있음 */ 
            if("M".equals(Util.null2str(tempHblRow.get("AMS_YN")))) {
            	if("ID".equals(tempMblData.get("DEST_COUNTRY_CODE")) || "BD".equals(tempMblData.get("DEST_COUNTRY_CODE"))){
                	if("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_BUSINESS_ID")))){
                		tempSearchData.put("srchCode", "");
                        tempSearchData.put("message", "도착지가 인도네시아/방글라데시일 경우 Consignee Business ID(수하인 사업자번호)를 기재하기 바랍니다.");
                        message = ediCommonService.getErrorCodeName(tempSearchData);
             
                        errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                	}
                }
            }
		
            //////////////////////
            // 면장정보 조회
            //////////////////////
            tempSearchData.put("odJobNo", Util.null2str(tempHblRow.get("OD_JOB_NO")));
            tempSearchData.put("ifServiceCode", Util.null2str(docSend.get("ifServiceCode")));
            
            customsLicenseList = manifestMapper.ediManifestCustomsLicenseList(tempSearchData);
		
            if (customsLicenseList.size() > 0) {
                for (int n = 0; n < customsLicenseList.size(); ++n) {
                    if ("".equals(Util.null2str(customsLicenseList.get(n).get("LICENSE_NO")).trim())) {
                        tempSearchData.put("srchCode", "MF-37");	// 수출면장번호가 없습니다.
                        message = ediCommonService.getErrorCodeName(tempSearchData);
                
                        errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                    }
                    
                    if (Util.null2int(customsLicenseList.get(n).get("PIECES")) <= 0) {
                        tempSearchData.put("srchCode", "MF-44");	// 수출면장정보에 수량이 없습니다.
                        message = ediCommonService.getErrorCodeName(tempSearchData);
                
                        errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                    }
                    
                    if (new BigDecimal(Util.null2str(customsLicenseList.get(n).get("GWT"), "0")).doubleValue() <= 0) {
                        tempSearchData.put("srchCode", "MF-45");	// 수출면장정보에 중량이 없습니다.
                        message = ediCommonService.getErrorCodeName(tempSearchData);
                
                        errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                    }
                    
                    //4세대 추가 '''@@@ 면장 분할 선적여부 필수항목으로 변경
                    if ("".equals(Util.null2str(customsLicenseList.get(n).get("PARTIAL_YN")).trim())) {
                        tempSearchData.put("srchCode", "MF-137");	// 면장 분할 선적여부가 없습니다.
                        message = ediCommonService.getErrorCodeName(tempSearchData);
                        
                        errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                    }
                }
            } else {
                tempSearchData.put("srchCode", "MF-38");	// 해당 House B/L 에 License정보가 한 건도 없습니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
		
            tempMapSearchData.put("odJobNo", Util.null2str(tempHblRow.get("OD_JOB_NO")));
			tempCustomsLicenseInfo = manifestMapper.ediManifestCustomsLicenseInfo(tempMapSearchData);
		
            if (tempCustomsLicenseInfo != null) {
                if ("".equals(Util.null2str(tempCustomsLicenseInfo.get("COMBINE_TYPE")))) {
                    if (Util.null2int(tempCustomsLicenseInfo.get("L_PIECES")) != Util.null2int(tempHblRow.get("PIECES"))) {
                        tempSearchData.put("srchCode", "MF-39");	// HAWB 의 수량과 면장의 총수량이 일치하지 않습니다.
                        message = ediCommonService.getErrorCodeName(tempSearchData);
                        
                        errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + tempCustomsLicenseInfo.get("LICENSE_NO") + ", " + message, hblNo, hsn));
                    }
                    
                    if (gwtBD.doubleValue() <= 30) {
                        // House B/L 중량이 30kg 이하인 경우 체크하지 않는다.
                    } else if(gwtBD.doubleValue() > 30 && gwtBD.doubleValue() < 100) {
                        
                        //House B/L 중량이 100kg 미만인 경우 수출신고서 중량을 기준으로 50% 이상 차이나면 오류통보
                        if ((gwtBD.multiply(new BigDecimal("0.5"))).doubleValue() < (gwtBD.subtract(new BigDecimal(Util.null2str(tempCustomsLicenseInfo.get("L_GWT"), "0")))).abs().doubleValue()) {
                            tempSearchData.put("srchCode", "MF-40");	// HAWB 의 중량과 면장의 총중량이 일치하지 않습니다.
                            message = ediCommonService.getErrorCodeName(tempSearchData);
                            
                            errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + tempCustomsLicenseInfo.get("LICENSE_NO") + ", " + message, hblNo, hsn));
                        }
                    } else if(gwtBD.doubleValue() >= 100) {
                        //House B/L 중량이 100kg 이상인 경우 수출신고서 중량을 기준으로 30% 이상 차이나면 오류통보
                        if ((gwtBD.multiply(new BigDecimal("0.3"))).doubleValue() < (gwtBD.subtract(new BigDecimal(Util.null2str(tempCustomsLicenseInfo.get("L_GWT"), "0")))).abs().doubleValue()) {
                            tempSearchData.put("srchCode", "MF-40");	// HAWB 의 중량과 면장의 총중량이 일치하지 않습니다.
                            message = ediCommonService.getErrorCodeName(tempSearchData);
                            
                            errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + tempCustomsLicenseInfo.get("LICENSE_NO") + ", " + message, hblNo, hsn));
                        }
                    }
                }
            }
		} // end of for-loop.
		
		// 오류내역이 있을 경우
		if (errList.size() > 0) {
			//////////////////////////////////////////
			// 오더 EDI 상태값 업데이트
			//////////////////////////////////////////
			tempUpdateData = new HashMap<String, Object>();
			tempUpdateData.put("entCode", Util.null2str(docSend.get("entCode")));
			tempUpdateData.put("entBusinessCode", Util.null2str(docSend.get("entBusinessCode")));
			tempUpdateData.put("odJobNo", Util.null2str(tempMblData.get("OD_JOB_NO")));
			
			// FWB:FWB송신완료, FHL-STANDBY : 오류검증만 한경우, FHL-ERROR : 오류검증오류인경우, FHL:적하목록 송신완료
			tempUpdateData.put("ediProcessInfo", "FHL-ERROR");
			//ediCommonDao.setEdiProcessInfoUpdate(tempUpdateData);
			
			ediCommonService.setValidateMessage(docSend, tempMblData, errList, result);
			
			return false;
		} else {
			//////////////////////////////////////////
			// 오더 EDI 상태값 업데이트
			//////////////////////////////////////////
			tempUpdateData = new HashMap<String, Object>();
			tempUpdateData.put("entCode", Util.null2str(docSend.get("entCode")));
			tempUpdateData.put("entBusinessCode", Util.null2str(docSend.get("entBusinessCode")));
			tempUpdateData.put("odJobNo", Util.null2str(tempMblData.get("OD_JOB_NO")));
			
			// FWB:FWB송신완료, FHL-STANDBY : 오류검증만 한경우, FHL-ERROR : 오류검증오류인경우, FHL:적하목록 송신완료
			tempUpdateData.put("ediProcessInfo", "FHL-STANDBY");
			//ediCommonDao.setEdiProcessInfoUpdate(tempUpdateData); //  - 테스트 주석
		}
		
		return true;
	}
	
	/**
	 * 파일생성 
	 * 관세청 4세대 전용
	 * 
	 * @param docSend
	 * @param tempMblData
	 * @param tempHblDataList
	 * @param fwSendAccInfo
	 * @param ediHostSendInfo
	 * @return
	 * @throws java.lang.Exception
	 */
	public String[] getSendDataFileCreate4th(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempHblDataList, Map<String, Object> fwSendAccInfo
			, Map<String, Object> ediHostSendInfo) throws java.lang.Exception {

		List<Map<String, Object>> tempFileCreateHblDataList = tempHblDataList;
		List<Map<String, Object>> customsLicenseList = null;
		List<Map<String, Object>> customsHsList = null;
		
		BufferedWriter bfw		= null;
		
		StringBuffer retWriteData = new StringBuffer();	// 파일 용량이 클경우 내용 저장하면 안됨.
		StringBuffer writeDataHeader = new StringBuffer();
		StringBuffer writeData = new StringBuffer();
		String retData[] = new String[6];
		String lineStr[] = null;
		int bodyLength = 0;
		
		String fileName = new StringBuffer(Util.null2str(docSend.get("registerUserId"))).append(".H").append(Util.null2str(tempMblData.get("MBL_NO")).trim()).append(".txt").toString();	// 전송 파일명
		String dirPath[] = ediCommonService.getSendFilePath(Util.null2str(docSend.get("ediVender")), "edi/manifest", docSend);	// 로컬폴더 경로 가져오기
		
		String cusAddress = null;
		String cusAddress1 = null;
		String cusAddress2 = null;
		String cusAddress3 = null;
		
		String docCode = null;
		String mblNoStr = null; //중복 변수 지정 - ditobass!!@
		// 파일 내용 쓰기'''@@@
		try {
			bfw = ediCommonService.getFileWriterOpen(fileName, dirPath);	// 쓰기 파일 열기
			
			Map<String, Object> tempHblRow = null;
			
			Map<String, Object> tempSearchData = new HashMap<String, Object>();
			
			String strSecondAirLine = null;
			String strForwarderAirline3Code = Util.getFileWriteDataWithRtrim(Util.null2str(ediHostSendInfo.get("IF_ETC_1")), "String", "3");	// 항공사 에서 포워더를 지칭하는 3자리 부포
			String ediVender = Util.null2str(docSend.get("ediVender"));
//			String tempHblNo = "";
            String codeDetailDestCountryCodeStr = null; //중복 변수 지정 - ditobass!!@
            
//            if(ediVender.equals("KCNET")){
//    			writeData.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
////    			writeData.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
//    			writeData.append("<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
//    		    writeData.append("<soapenv:Header xmlns:kcn=\"http://www.kcnet.co.kr/kcn\">");
//    		    writeData.append("<kcn:SenderId>").append(Util.getFileWriteDataWithRtrim(ediCommonService.getDeleteCharStringFwb(Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).trim()), "String", "4")).append("</kcn:SenderId>");
//    		    writeData.append("<kcn:ReceiverId>KCNECP</kcn:ReceiverId>");
//    		    writeData.append("<kcn:DocName>CIMFHL</kcn:DocName>");
//    		    writeData.append("<kcn:DocCode>FHL</kcn:DocCode>");
//    		    writeData.append("<kcn:MessageId>").append(Util.null2str(tempMblData.get("MBL_NO")).trim()).append("</kcn:MessageId>");
//    		    writeData.append("<kcn:VenderId>YHT</kcn:VenderId>");
//    		    writeData.append("<kcn:ID>").append(Util.null2str(ediHostSendInfo.get("IF_WEB_ID"))).append("</kcn:ID>");
//    		    writeData.append("<kcn:Password>").append(Util.null2str(ediHostSendInfo.get("IF_WEB_PWD"))).append("</kcn:Password>");
//    		    writeData.append("</soapenv:Header>");
//    		    writeData.append("<soapenv:Body>");
//    		    writeData.append("<dataArea>");
//    		    
//    		    bfw.write(writeData.toString());
//    			bfw.newLine();	// 엔터값 추가
//    			retWriteData.append(writeData.toString()).append("\r\n");	// DB에 저장할 파일 내용 담기
//            }
            
			for (int i = 0; i < tempFileCreateHblDataList.size(); ++i) {
				tempHblRow = tempFileCreateHblDataList.get(i);
				
                mblNoStr = Util.null2str(tempHblRow.get("MBL_NO")).trim();
                codeDetailDestCountryCodeStr = Util.null2str(tempHblRow.get("CODE_DETAIL_DEST_COUNTRY_CODE")).trim(); //중복 변수 지정 - ditobass!!@
				
				// 항공사 편명 가져오기
				tempSearchData.clear(); //초기화 - ditobass!!@
				tempSearchData.put("entCode", Util.null2str(docSend.get("entCode")));
				tempSearchData.put("srchCode", mblNoStr.substring(0, 3));
				tempSearchData.put("groupCode", "SIGN_AIRLINE");
				tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
				
				strSecondAirLine = Util.null2str(tempMblData.get("LINE_CODE"));	// 마스터 B/L 상의 Flight No 2자리로 변경

				writeData.setLength(0); //초기화 - ditobass!!@
				writeDataHeader.setLength(0);
				bodyLength = 0;

//				"Master 정보"
				//  (ex: MBI/988-12345678ICNTPE/T5k1582.000 줄바꿈)
				writeData.append("MBI/").append(mblNoStr.substring(0, 3)).append("-").append(ediCommonService.getDeleteAlphabetChar(mblNoStr.substring(3)));
				writeData.append(Util.getFileWriteData(Util.null2str(tempMblData.get("ORIG_CODE")), "String", "3"));
				writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("DEST_CODE")), "String", "3"));
				writeData.append("/T");
				
				 //최대 9999999999 // 4세대 10자리로 늘어남
                writeData.append(Util.null2str(tempHblRow.get("M_PIECES"), "0"));
                
                // 중량 소수점  3자리까지
                writeData.append("K");
                writeData.append(ediCommonService.getNumberFormat(Util.null2str(tempHblRow.get("M_GWT"), "0"), "##0.000", 3));
                writeData.append("\r\n");
				
//				"House 정보 메인"
                // (ex: HBS/11561203412354873598/ICNTPE/1/K1582.000/1/ITEMNAME\r\n)
                writeData.append("HBS/");
            	writeData.append(ediCommonService.getHblDeleteChar(Util.null2str(tempHblRow.get("HBL_NO")).trim()));
                writeData.append("/").append(Util.getFileWriteData(Util.null2str(tempMblData.get("ORIG_CODE")), "String", "3"));
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("DEST_CODE")), "String", "3"));
                writeData.append("/");
                
                //최대 9999999999 // 4세대 10자리로 늘어남
                writeData.append(Util.null2str(tempHblRow.get("PIECES"), "0"));
                
                // 중량 소수점  3자리까지
                writeData.append("/K");
                writeData.append(ediCommonService.getNumberFormat(Util.null2str(tempHblRow.get("GWT"), "0"), "##0.000", 3));
                writeData.append("/");
                
               //최대 9999999999 // 4세대 10자리로 늘어남
               // 2015-01-05 상기 H_PIECES_REAL 를 H_PIECES 로 대체한다. 
                if (Util.null2int(tempHblRow.get("PIECES")) >= 1000000000) {
                	writeData.append("999999999");
                } else {
                	writeData.append(Util.null2str(tempHblRow.get("PIECES"), "0"));
                }
                //-------------------------------------------------------------
                writeData.append("/");

                // 대표품목, 상세품목은 하단에서 처리, ※ 품목체크 함수 추가 및 통일 FHL, FWB
                writeData.append(
                		ediCommonService.getEdiCharacter(
                				ediCommonService.getDeleteCharStringManifest(Util.getFileWriteDataWithRtrim(Util.null2str(tempHblRow.get("COMMODITY_ITEM")).replace(".", "").replace("-", ""), "String", "15"))
            			)
            	);
                
                writeData.append("\r\n");
				
                // 2014-12-12 ※오류시 스펙확인 필요!!
                if (!"".equals(Util.null2str(tempHblRow.get("SPECIAL_CARGO")).trim()) || !"".equals(Util.null2str(tempHblRow.get("SPECIAL_CARGO_A")).trim())) {
                	writeData.append("/").append(Util.null2str(tempHblRow.get("SPECIAL_CARGO")).trim().substring(0, 3)).append(Util.null2str(tempHblRow.get("SPECIAL_CARGO_A")).trim().substring(0, 3));
                	writeData.append("\r\n");
                }
				
                

//				"House AMSCommodity"
                writeData.append("TXT/.COM.");
                
                // 세부품목 스펙은 60자리임. 3세대는 25자리밖에 표현을 안함?????  주석에는 60자리인데 왜 25자리밖에???
                // 4세대 200자리
                String tempHblRow1 = ediCommonService.getEdiCharacter(ediCommonService.getDeleteCharStringManifest(ediCommonService.getSubStringK(Util.null2str(tempHblRow.get("COMMODITY_ITEM")).replace(".", "").replace("-", "").trim(), 0, 60)));
                String tempHblRow2 = ediCommonService.getEdiCharacter(ediCommonService.getDeleteCharStringManifest(ediCommonService.getSubStringK(Util.null2str(tempHblRow.get("COMMODITY_ITEM")).replace(".", "").replace("-", "").trim(), 60, 120)));
                String tempHblRow3 = ediCommonService.getEdiCharacter(ediCommonService.getDeleteCharStringManifest(ediCommonService.getSubStringK(Util.null2str(tempHblRow.get("COMMODITY_ITEM")).replace(".", "").replace("-", "").trim(), 120, 180)));
                String tempHblRow4 = ediCommonService.getEdiCharacter(ediCommonService.getDeleteCharStringManifest(ediCommonService.getSubStringK(Util.null2str(tempHblRow.get("COMMODITY_ITEM")).replace(".", "").replace("-", "").trim(), 180, 200)));
                
                if (!"".equals(tempHblRow1.replace(" ","").trim())){
                	 writeData.append(tempHblRow1); 
                	 writeData.append("\r\n");
				}
				if (!"".equals(tempHblRow2.replace(" ","").trim())) {
					 writeData.append("/.COM.");
					 writeData.append(tempHblRow2);
                	 writeData.append("\r\n");
				}
				if (!"".equals(tempHblRow3.replace(" ","").trim())) {
					 writeData.append("/.COM.");
					 writeData.append(tempHblRow3);
                	 writeData.append("\r\n");
				}
				if (!"".equals(tempHblRow4.replace(" ","").trim())) {
					 writeData.append("/.COM.");
					 writeData.append(tempHblRow4);
                	 writeData.append("\r\n");
				}
				
//				"FLT 일자,Deconsol Code, 적재장소,장치장소"
				writeData.append("/.FLT.");
				// OB_DATE 날짜형식( YYYYMMDD )
				writeData.append(Util.null2str(tempHblRow.get("OB_DATE")).trim().replace("-", ""))
                        .append("-").append(Util.null2str(tempHblRow.get("FLIGHT_NO")).trim())
                        .append("-").append(Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).trim())
                        .append("-");
				writeData.append(Util.null2str(tempHblRow.get("LOAD_LOCATION_CODE")).trim())
                        .append("-").append(Util.null2str(tempHblRow.get("EP_LOCATION_CODE")).trim());
                
				writeData.append("\r\n");
				
				// "House Shipper 정보"
				if (!"".equals(ediCommonService.getEdiCharacter(ediCommonService.getDeleteCharStringManifest(ediCommonService.getSubStringK(Util.null2str(tempHblRow.get("SHIPPER_NAME")).trim(), 0, 60))))) {
					writeData.append("/.SNM.").append(ediCommonService.getEdiCharacter(ediCommonService.getDeleteCharStringManifest(ediCommonService.getSubStringK(Util.null2str(tempHblRow.get("SHIPPER_NAME")).trim(), 0, 60))));
					writeData.append("\r\n");
				}
				
				if (!"".equals(ediCommonService.getEdiCharacter(ediCommonService.getDeleteCharStringManifest(ediCommonService.getSubStringK(Util.null2str(tempHblRow.get("SHIPPER_NAME")).trim(), 60, 100))))) {
					writeData.append("/.SNM.").append(ediCommonService.getEdiCharacter(ediCommonService.getDeleteCharStringManifest(ediCommonService.getSubStringK(Util.null2str(tempHblRow.get("SHIPPER_NAME")).trim(), 60, 100))));
					writeData.append("\r\n");
				}
				
				writeData.append("/.SCC.")
                        .append(Util.getFileWriteData(Util.null2str(tempHblRow.get("SHIPPER_COUNTRY_CODE")), "String", "2"))
                        .append(Util.getFileWriteData(Util.null2str(tempMblData.get("ORIG_CODE")), "String", "3"));
				
				writeData.append("\r\n");
				
				cusAddress = ediCommonService.getEdiCharacter(ediCommonService.getDeleteCharStringManifest(Util.null2str(tempHblRow.get("SHIPPER_ADDRESS")).trim()));
				cusAddress1 = ediCommonService.getSubStringK(Util.null2str(cusAddress), 0, 60);
				cusAddress2 = ediCommonService.getSubStringK(Util.null2str(cusAddress), 60, 120);
				cusAddress3 = ediCommonService.getSubStringK(Util.null2str(cusAddress), 120, 150);
				
				if (!"".equals(cusAddress1)) {
					writeData.append("/.SAR.").append(cusAddress1);
					writeData.append("\r\n");
				}
				
				if (!"".equals(cusAddress2)) {
					writeData.append("/.SAR.").append(cusAddress2);
					writeData.append("\r\n");
				}
				
				if (!"".equals(cusAddress3)) {
					writeData.append("/.SAR.").append(cusAddress3);
					writeData.append("\r\n");
				}
				
				if (!"".equals(ediCommonService.getEdiCharacter(ediCommonService.getDeleteCharStringManifest(ediCommonService.getSubStringK(Util.null2str(tempHblRow.get("CONSIGNEE_NAME")).trim(), 0, 60))))) {
					writeData.append("/.CNM.").append(ediCommonService.getEdiCharacter(ediCommonService.getDeleteCharStringManifest(ediCommonService.getSubStringK(Util.null2str(tempHblRow.get("CONSIGNEE_NAME")).trim(), 0, 60))));
					writeData.append("\r\n");			
				}
				
				if (!"".equals(ediCommonService.getEdiCharacter(ediCommonService.getDeleteCharStringManifest(ediCommonService.getSubStringK(Util.null2str(tempHblRow.get("CONSIGNEE_NAME")).trim(), 60, 100))))) {
					writeData.append("/.CNM.").append(ediCommonService.getEdiCharacter(ediCommonService.getDeleteCharStringManifest(ediCommonService.getSubStringK(Util.null2str(tempHblRow.get("CONSIGNEE_NAME")).trim(), 60, 100))));
					writeData.append("\r\n");
				}
				
				writeData.append("/.CCC.")
                        .append(Util.getFileWriteData(Util.null2str(tempHblRow.get("CONSIGNEE_COUNTRY_CODE")), "String", "2"))
                        .append(Util.getFileWriteData(Util.null2str(tempHblRow.get("DEST_CODE")), "String", "3"));	//2021-10-08 기존 ORIG_CODE에서 DEST_CODE로 변경
				writeData.append("\r\n");
				
				cusAddress = ediCommonService.getEdiCharacter(ediCommonService.getDeleteCharStringManifest(Util.null2str(tempHblRow.get("CONSIGNEE_ADDRESS")).trim()));
				cusAddress1 = ediCommonService.getSubStringK(Util.null2str(cusAddress), 0, 60);
				cusAddress2 = ediCommonService.getSubStringK(Util.null2str(cusAddress), 60, 120);
				cusAddress3 = ediCommonService.getSubStringK(Util.null2str(cusAddress), 120, 150);
				
				if (!"".equals(cusAddress1)) {
					writeData.append("/.CAR.").append(cusAddress1);
					writeData.append("\r\n");
				}
				
				if (!"".equals(cusAddress2)) {
					writeData.append("/.CAR.").append(cusAddress2);
					writeData.append("\r\n");
				}
				
				if (!"".equals(cusAddress3)) {
					writeData.append("/.CAR.").append(cusAddress3);
					writeData.append("\r\n");
				}
				
				// HS, 면장정보 조회조건 SET [ START ]
				tempSearchData.put("odJobNo", Util.null2str(tempHblRow.get("OD_JOB_NO")));
				tempSearchData.put("ifServiceCode", Util.null2str(docSend.get("ifServiceCode")));
				// HS, 면장정보 조회조건 SET [ END ]
				
				// 인도네시아용 FHL - OCI 추가 항목 [START]
				if ("Y".equals(Util.null2str(tempHblRow.get("INDONESIA_SPEC_ADD_YN")).trim())) {
					if ("ID".equals(codeDetailDestCountryCodeStr)
						|| "DE".equals(codeDetailDestCountryCodeStr)
						|| "NO".equals(codeDetailDestCountryCodeStr) ) {
						customsHsList = manifestMapper.ediManifestCustomsHsList(tempSearchData);
						if (customsHsList != null && customsHsList.size() > 0) {
							// House 한 건에 최대 9개
							for (int h = 0; h < customsHsList.size(); ++h) {
								if (h == 0) {
									writeData.append("HTS"); 
								}
								writeData.append("/").append(Util.getFileWriteData(Util.null2str(customsHsList.get(h).get("HS_CODE")), "String", "6"));
								writeData.append("\r\n");
							}
						}else{
							writeData.append("HTS"); 
							writeData.append("/").append(Util.getFileWriteData(Util.null2str(tempHblRow.get("EDI_HS_CODE")), "String", "6"));
							writeData.append("\r\n");
						}
					}
				}

//				"House License 정보 OCI"
				//////////////////////
				// 면장정보 조회 KCNET - AMS = N
				//////////////////////
				customsLicenseList = manifestMapper.ediManifestCustomsLicenseList(tempSearchData);
				
				String strCountry = null;
				String strCustomerInfomationIdentifier = null;
				 
				if (customsLicenseList.size() > 0) {
					strCountry = Util.null2str(tempHblRow.get("EDI_ORIG_COUNTRY"));

					writeData.append("OCI");
					
					// 세관신고 여부
					// 4세대는  M(국내신고+해외신고), I(국내신고), E(수출특송건)
					strCustomerInfomationIdentifier = Util.null2str(tempHblRow.get("AMS_YN"));
					
					String strOCIHeader = null;
					//수출화물
                    if ("E".equals(Util.null2str(tempHblRow.get("BL_TYPE_IERT")))) {
                    	
    					//UPS일경우 I => X로 변경
    					if("UPSXK".equals(Util.null2str(docSend.get("entCode")))) {
    						if("I".equals(strCustomerInfomationIdentifier)) {
    							strCustomerInfomationIdentifier = "X";
    						}
    					}
    					
                        if (!"".equals(strCountry)) {
                        	strOCIHeader = "/" + strCountry + "/EXP/" + strCustomerInfomationIdentifier + "/";
                        } else {
                            strOCIHeader = "/KR/EXP/" + strCustomerInfomationIdentifier + "/";
                        }
                        
                        for (int y = 0; y < customsLicenseList.size(); ++y) {
                        	writeData.append(strOCIHeader)
                                    .append("E")
                                    .append(new DecimalFormat("000").format(y + 1))
                                    .append("-")
                                    .append(Util.null2str(customsLicenseList.get(y).get("LICENSE_NO")).trim())
                                    .append("-").append(Util.null2str(customsLicenseList.get(y).get("COMBINE_TYPE_A_Z")).trim())
                                    .append("-").append(("".equals(Util.null2str(customsLicenseList.get(y).get("COMBINE_TYPE_A_Z")).trim()) ? "" : Util.null2str(customsLicenseList.get(y).get("COMBINE_PIECES"), "0")))
                                    .append("-");
                        			if(Util.isEmpty(customsLicenseList.get(y).get("UNIT_PIECES"))) {
                        				writeData.append("CT");
                        			}else if(Util.null2str(customsLicenseList.get(y).get("UNIT_PIECES")).trim().length() > 2){
                        				writeData.append(Util.null2str(customsLicenseList.get(y).get("UNIT_PIECES_EDI")));
                                    }else{
                                    	writeData.append(Util.null2str(customsLicenseList.get(y).get("UNIT_PIECES")).trim());
                                    }
                        	
                        	writeData.append("\r\n");
            				
                        	writeData.append(strOCIHeader)
                                    .append("S")
                                    .append(new DecimalFormat("000").format(y + 1))
                                    .append("-").append(Util.null2str(customsLicenseList.get(y).get("PIECES"), "0"))
                                    .append("-").append(ediCommonService.getNumberFormat(Util.null2str(customsLicenseList.get(y).get("GWT"), "0"), "##0.0", 1))
                                    .append("-").append(("Y".equals(Util.null2str(customsLicenseList.get(y).get("PARTIAL_YN")).trim()) ? "Y" : "N"))
                                    .append("-").append(("Y".equals(Util.null2str(customsLicenseList.get(y).get("PARTIAL_YN")).trim()) ? Util.null2str(customsLicenseList.get(y).get("PARTIAL_SEQ"), "0") : ""));
                        	writeData.append("\r\n");
                        }
                        
                    // 환적화물
                    } else {
                    	
    					//UPS일경우 I => X로 변경
    					if("UPSXK".equals(Util.null2str(docSend.get("entCode")))) {
    						if("I".equals(strCustomerInfomationIdentifier)) {
    							strCustomerInfomationIdentifier = "X";
    						}
    					}
    					
                    	if (!"".equals(strCountry)) {
                    		strOCIHeader = "/" + strCountry + "/EXP/" + strCustomerInfomationIdentifier + "/";
                        } else {
                        	strOCIHeader = "/KR/EXP/" + strCustomerInfomationIdentifier + "/";
                        }
                    	
                    	for (int y = 0; y < customsLicenseList.size(); ++y) {
                        	writeData.append(strOCIHeader)
                                    .append("R")
                                    .append(new DecimalFormat("000").format(y + 1))
                                    .append("-").append(Util.null2str(customsLicenseList.get(y).get("LICENSE_NO")).trim())
                                    .append("-").append(Util.null2str(customsLicenseList.get(y).get("COMBINE_TYPE_A_Z")).trim())
                                    .append("-").append(("".equals(Util.null2str(customsLicenseList.get(y).get("COMBINE_TYPE_A_Z")).trim()) ? "" : Util.null2str(customsLicenseList.get(y).get("COMBINE_PIECES"), "0")))
                                    .append("-");
                			if(Util.isEmpty(customsLicenseList.get(y).get("UNIT_PIECES"))) {
                				writeData.append("CT");
                			}else if(Util.null2str(customsLicenseList.get(y).get("UNIT_PIECES")).trim().length() > 2){
                				writeData.append(Util.null2str(customsLicenseList.get(y).get("UNIT_PIECES_EDI")));
                            }else{
                            	writeData.append(Util.null2str(customsLicenseList.get(y).get("UNIT_PIECES")).trim());
                            }
                        	
                        	writeData.append("\r\n");
            				
            				writeData.append(strOCIHeader)
                                    .append("S")
                                    .append(new DecimalFormat("000").format(y + 1))
                                    .append("-").append(Util.null2str(customsLicenseList.get(y).get("PIECES"), "0"))
                                    .append("-").append(ediCommonService.getNumberFormat(Util.null2str(customsLicenseList.get(y).get("GWT"), "0"), "##0.0", 1))
                                    .append("-").append(("Y".equals(Util.null2str(customsLicenseList.get(y).get("PARTIAL_YN")).trim()) ? "Y" : "N"))
                                    .append("-").append(("Y".equals(Util.null2str(customsLicenseList.get(y).get("PARTIAL_YN")).trim()) ? Util.null2str(customsLicenseList.get(y).get("PARTIAL_SEQ"), "0") : ""));
            				writeData.append("\r\n");
                    	}
                    }
				}
				
				// 중국세관용 KCNET FHL - OCI 추가 항목 [START] - AMS = N
				if ("Y".equals(Util.null2str(tempHblRow.get("CHINA_SPEC_ADD_YN")).trim())) {
					if ("CN".equals(codeDetailDestCountryCodeStr)
						|| "CN".equals(Util.null2str(tempHblRow.get("VIA_TO_A_COUNTRY_CODE")).trim())
						|| "CN".equals(Util.null2str(tempHblRow.get("VIA_TO_B_COUNTRY_CODE")).trim())
						|| "CN".equals(Util.null2str(tempHblRow.get("VIA_TO_C_COUNTRY_CODE")).trim())) {
						
						/* **********************************************************************
						 * [START] KC, U SEGMENT SETTING & 항공사별 OCI 항목 특수문자 제거 SETTING */
						String ediOciSegAttn = "KC";
						String ediOciSegTel	 = "U";
						String ediOciSegCheckChar = "[^a-zA-Z0-9.-.,-,---]";
						String ediCheckAirlineNo = mblNoStr.substring(0, 3);
						
						// 160-CX, 180-KE, 205-NH, 403-PO, 988-OZ, 999-CA, 826-GS, 128-UO, 851-HX, 618-SQ 항공사는 KC, U -> CP, CT로 변경
						if ("160".equals(ediCheckAirlineNo) || "180".equals(ediCheckAirlineNo)
							|| "205".equals(ediCheckAirlineNo) || "403".equals(ediCheckAirlineNo)
							|| "988".equals(ediCheckAirlineNo) || "999".equals(ediCheckAirlineNo)
							|| "128".equals(ediCheckAirlineNo) || "851".equals(ediCheckAirlineNo)
							|| "826".equals(ediCheckAirlineNo) || "618".equals(ediCheckAirlineNo)) {
							ediOciSegAttn	= "CP";
							ediOciSegTel	= "CT";
						}
						
						// 2019.03.11 대한항공 신화물시스템용 검증항목 (대한항공-OCI 항목 특수문자 "- "외 미허용) 체크 - AMS : N
						if ("180".equals(ediCheckAirlineNo) && "Y".equals(Util.null2str(tempHblRow.get("KE_US_SPEC_ADD_YN")))) {
							ediOciSegCheckChar = "[^a-zA-Z0-9,-,---]";
						}
						/* [ END ] KC, U SEGMENT SETTING & 항공사별 OCI 항목 특수문자 제거 SETTING *
						 * **********************************************************************/
						
						// OCI [SHP] - /국가코드/SHP/T/국가인증코드 + 식별번호(사업자번호)
						if (!"".equals(Util.null2str(tempHblRow.get("SHIPPER_BUSINESS_ID")).trim())) {
							writeData.append("/").append(Util.null2str(tempHblRow.get("CODE_DETAIL_ORIG_COUNTRY_CODE")).trim());
							writeData.append("/").append("SHP");
							writeData.append("/").append("T");
							writeData.append("/").append(ediCommonService.getEdiCharacter(
									ediCommonService.getDeleteCharStringManifest(
											Util.getFileWriteDataWithRtrim(Util.null2str(tempHblRow.get("SHP_CN_REG_TYPE")).trim() + Util.null2str(tempHblRow.get("SHIPPER_BUSINESS_ID")).trim(), "String", "35")
											//Util.getFileWriteDataWithRtrim(Util.null2str(tempHblRow.get("SHIPPER_BUSINESS_ID")).trim(), "String", "35")
											))
									);
							writeData.append("\r\n");
						}
						
						// OCI [SHP] - /국가코드/SHP/KC/담당자명
						if (!"".equals(Util.null2str(tempHblRow.get("SHIPPER_ATTN")).trim())) {
							writeData.append("/").append(Util.null2str(tempHblRow.get("CODE_DETAIL_ORIG_COUNTRY_CODE")).trim());
							writeData.append("/").append("SHP");
							writeData.append("/").append(ediOciSegAttn); // KC or CP
							// 2019.03.11 대한항공 신화물시스템용 검증항목 (대한항공-OCI 항목 특수문자 "- "외 미허용) 체크
							writeData.append("/").append(ediCommonService.getEdiCharacterCustom(
									ediCommonService.getDeleteCharStringManifest(
											Util.getFileWriteDataWithRtrim(Util.null2str(tempHblRow.get("SHIPPER_ATTN")).trim(), "String", "35")
											), ediOciSegCheckChar
									));
							writeData.append("\r\n");
						}
						
						// OCI [SHP] - /국가코드/SHP/U/담당자 전화번호
						if (!"".equals(Util.null2str(tempHblRow.get("SHIPPER_ATTN_TEL")).trim())) {
							writeData.append("/").append(Util.null2str(tempHblRow.get("CODE_DETAIL_ORIG_COUNTRY_CODE")).trim());
							writeData.append("/").append("SHP");
							writeData.append("/").append(ediOciSegTel); // U or CT
							// OCI - FAX번호는 TEL과 동일하게 입력 중국발 999-CA(AIR CHINA) 권고사항 - 2018.06.07
							// AIR CHINA 요청으로 TE, FX 문구 제거 - 2018.07.27
							if ("999".equals(mblNoStr.substring(0, 3))) {
								writeData.append("/").append(Util.getFileWriteDataWithRtrim(
										"" + ediCommonService.getBLCharCHK(Util.null2str(tempHblRow.get("SHIPPER_ATTN_TEL")).trim())
										+ " " + ediCommonService.getBLCharCHK(Util.null2str(tempHblRow.get("SHIPPER_ATTN_TEL")).trim())
										, "String", "35"));
								
							} else if ("180".equals(ediCheckAirlineNo) && "Y".equals(Util.null2str(tempHblRow.get("KE_US_SPEC_ADD_YN")))) {
								// 2019.03.11 대한항공 신화물시스템용 검증항목 (대한항공-OCI 항목 특수문자 "- "외 미허용) 체크
								writeData.append("/").append(ediCommonService.getEdiCharacterCustom(
										Util.getFileWriteDataWithRtrim(
												ediCommonService.getBLCharCHK(Util.null2str(tempHblRow.get("SHIPPER_ATTN_TEL")).trim())
												, "String", "35"), ediOciSegCheckChar));
								
							} else {
								writeData.append("/").append(Util.getFileWriteDataWithRtrim(
										ediCommonService.getBLCharCHK(Util.null2str(tempHblRow.get("SHIPPER_ATTN_TEL")).trim())
										, "String", "35"));
							}
							writeData.append("\r\n");
						}
						
						// OCI [CNE] - /국가코드/CNE/T/국가인증코드 + 식별번호(사업자번호)
						if (!"".equals(Util.null2str(tempHblRow.get("CONSIGNEE_BUSINESS_ID")).trim())) {
							writeData.append("/").append(codeDetailDestCountryCodeStr);
							writeData.append("/").append("CNE");
							writeData.append("/").append("T");
							writeData.append("/").append(ediCommonService.getEdiCharacter(
									ediCommonService.getDeleteCharStringManifest(
											Util.getFileWriteDataWithRtrim(Util.null2str(tempHblRow.get("CNE_CN_REG_TYPE")).trim() + Util.null2str(tempHblRow.get("CONSIGNEE_BUSINESS_ID")).trim(), "String", "35")
									))
							);
							writeData.append("\r\n");
						}
						
						// OCI [CNE] - /국가코드/CNE/KC/담당자명
						if (!"".equals(Util.null2str(tempHblRow.get("CONSIGNEE_ATTN")).trim())) {
							writeData.append("/").append(codeDetailDestCountryCodeStr);
							writeData.append("/").append("CNE");
							writeData.append("/").append(ediOciSegAttn);	// KC or CP
							// 2019.03.11 대한항공 신화물시스템용 검증항목 (대한항공-OCI 항목 특수문자 "- "외 미허용) 체크
							writeData.append("/").append(ediCommonService.getEdiCharacterCustom(
									ediCommonService.getDeleteCharStringManifest(
											Util.getFileWriteDataWithRtrim(Util.null2str(tempHblRow.get("CONSIGNEE_ATTN")).trim(), "String", "35")
											), ediOciSegCheckChar));
							writeData.append("\r\n");
						}
						
						// OCI [CNE] - /국가코드/CNE/U/담당자 전화번호
						if (!"".equals(Util.null2str(tempHblRow.get("CONSIGNEE_ATTN_TEL")).trim())) {
							writeData.append("/").append(codeDetailDestCountryCodeStr);
							writeData.append("/").append("CNE");
							writeData.append("/").append(ediOciSegTel);	// U or CT
							// OCI - FAX번호는 TEL과 동일하게 입력 중국발 999-CA(AIR CHINA) 권고사항 - 2018.06.07
							// AIR CHINA 요청으로 TE, FX 문구 제거 - 2018.07.27
							if ("999".equals(mblNoStr.substring(0, 3))) {
								writeData.append("/").append(Util.getFileWriteDataWithRtrim(
										"" + ediCommonService.getBLCharCHK(Util.null2str(tempHblRow.get("CONSIGNEE_ATTN_TEL")).trim())
										+ " " + ediCommonService.getBLCharCHK(Util.null2str(tempHblRow.get("CONSIGNEE_ATTN_TEL")).trim())
										, "String", "35"));
								
							} else if ("180".equals(ediCheckAirlineNo) && "Y".equals(Util.null2str(tempHblRow.get("KE_US_SPEC_ADD_YN")))) {
								// 2019.03.11 대한항공 신화물시스템용 검증항목 (대한항공-OCI 항목 특수문자 "- "외 미허용) 체크
								writeData.append("/").append(ediCommonService.getEdiCharacterCustom(
										Util.getFileWriteDataWithRtrim(
												ediCommonService.getBLCharCHK(Util.null2str(tempHblRow.get("CONSIGNEE_ATTN_TEL")).trim())
												, "String", "35"), ediOciSegCheckChar));
							} else {
								writeData.append("/").append(Util.getFileWriteDataWithRtrim(
										ediCommonService.getBLCharCHK(Util.null2str(tempHblRow.get("CONSIGNEE_ATTN_TEL")).trim())
										, "String", "35"));
							}
							writeData.append("\r\n");
						}
						
						// Notify Check Start
						if (!"".equals(Util.null2str(tempHblRow.get("NOTIFY_COUNTRY_CODE")).trim())) {
							// OCI [NFY] - /국가코드/NFY/T/국가인증코드 + 식별번호(사업자번호)
							if (!"".equals(Util.null2str(tempHblRow.get("NOTIFY_BUSINESS_ID")).trim())) {
								writeData.append("/").append(Util.null2str(tempHblRow.get("NOTIFY_COUNTRY_CODE")).trim());
								writeData.append("/").append("NFY");
								writeData.append("/").append("T");
								writeData.append("/").append(ediCommonService.getEdiCharacter(
										ediCommonService.getDeleteCharStringManifest(
												Util.getFileWriteDataWithRtrim(Util.null2str(tempHblRow.get("NFY_CN_REG_TYPE")).trim() + Util.null2str(tempHblRow.get("NOTIFY_BUSINESS_ID")).trim(), "String", "35")
										))
								);
								writeData.append("\r\n");
							}
							
							// OCI [NFY] - /국가코드/NFY/KC/담당자명
							if (!"".equals(Util.null2str(tempHblRow.get("NOTIFY_ATTN")).trim())) {
								writeData.append("/").append(Util.null2str(tempHblRow.get("NOTIFY_COUNTRY_CODE")).trim());
								writeData.append("/").append("NFY");
								writeData.append("/").append(ediOciSegAttn);	// KC or CP
								// 2019.03.11 대한항공 신화물시스템용 검증항목 (대한항공-OCI 항목 특수문자 "- "외 미허용) 체크
								writeData.append("/").append(ediCommonService.getEdiCharacterCustom(
										ediCommonService.getDeleteCharStringManifest(
												Util.getFileWriteDataWithRtrim(Util.null2str(tempHblRow.get("NOTIFY_ATTN")).trim(), "String", "35")
												), ediOciSegCheckChar));
								writeData.append("\r\n");
							}
							
							// OCI [NFY] - /국가코드/NFY/U/담당자 전화번호
							if (!"".equals(Util.null2str(tempHblRow.get("NOTIFY_ATTN_TEL")).trim())) {
								writeData.append("/").append(Util.null2str(tempHblRow.get("NOTIFY_COUNTRY_CODE")).trim());
								writeData.append("/").append("NFY");
								writeData.append("/").append(ediOciSegTel);	// U or CT
								// OCI - FAX번호는 TEL과 동일하게 입력 중국발 999-CA(AIR CHINA) 권고사항 - 2018.06.07
								// AIR CHINA 요청으로 TE, FX 문구 제거 - 2018.07.27
								if ("999".equals(mblNoStr.substring(0, 3))) {
									writeData.append("/").append(Util.getFileWriteDataWithRtrim(
											"" + ediCommonService.getBLCharCHK(Util.null2str(tempHblRow.get("NOTIFY_ATTN_TEL")).trim())
											+ " " + ediCommonService.getBLCharCHK(Util.null2str(tempHblRow.get("NOTIFY_ATTN_TEL")).trim())
											, "String", "35"));
									
								} else if ("180".equals(ediCheckAirlineNo) && "Y".equals(Util.null2str(tempHblRow.get("KE_US_SPEC_ADD_YN")))) {
									// 2019.03.11 대한항공 신화물시스템용 검증항목 (대한항공-OCI 항목 특수문자 "- "외 미허용) 체크
									writeData.append("/").append(ediCommonService.getEdiCharacterCustom(
											Util.getFileWriteDataWithRtrim(
													ediCommonService.getBLCharCHK(Util.null2str(tempHblRow.get("NOTIFY_ATTN_TEL")).trim())
													, "String", "35"), ediOciSegCheckChar));
								} else {
									writeData.append("/").append(Util.getFileWriteDataWithRtrim(
											ediCommonService.getBLCharCHK(Util.null2str(tempHblRow.get("NOTIFY_ATTN_TEL")).trim())
											, "String", "35"));
								}
								
								writeData.append("\r\n");
							}
						}// NOTIFY Check End
					}
				} // 중국세관용 KCNET FHL - OCI 추가 항목 [ END ] - AMS = N
				
				// 인도네시아용 KCNET FHL - OCI 추가 항목 [START] - AMS = N
				if ("Y".equals(Util.null2str(tempHblRow.get("INDONESIA_SPEC_ADD_YN")).trim())) {
					if ("ID".equals(codeDetailDestCountryCodeStr)) {
						// OCI [CNE] - /국가코드/CNE/T/국가인증코드 + 식별번호(사업자번호)
						writeData.append("/").append(codeDetailDestCountryCodeStr);
						writeData.append("/").append("CNE");
						writeData.append("/").append("T");
						writeData.append("/").append(ediCommonService.getEdiCharacter(
								ediCommonService.getDeleteCharStringManifest(
										Util.null2str(tempHblRow.get("CNE_CN_REG_TYPE")).trim()
										+ Util.getFileWriteDataWithRtrim(Util.null2str(tempHblRow.get("CONSIGNEE_BUSINESS_ID")).trim(), "String", "15")
								))
						);
						writeData.append("\r\n");
					}
				} // 인도네시아용 KCNET FHL - OCI 추가 항목 [ END ] - AMS = N
				
				/** 유럽/방글라데시 수하인 사업자번호 추가 */
				if("210".equals(tempMblData.get("DEST_AREA_CODE")) || "BD".equals(tempMblData.get("DEST_COUNTRY_CODE"))){
					if(!"".equals(Util.null2str(tempHblRow.get("CONSIGNEE_BUSINESS_ID")))) {
						writeData.append("/");
						writeData.append(Util.null2str(tempMblData.get("DEST_COUNTRY_CODE")));
						writeData.append("/NFY/T/");
						writeData.append(ediCommonService.getEdiCharacter(ediCommonService.getDeleteCharStringManifest(Util.null2str(tempHblRow.get("CONSIGNEE_BUSINESS_ID")))));
						writeData.append("\r\n");
					}
				}

//              "House Shipper 정보"
                writeData.append("SHP/").append(ediCommonService.getEdiCharacter(
                		ediCommonService.getDeleteCharStringManifest(Util.getFileWriteDataWithRtrim(Util.null2str(tempHblRow.get("SHIPPER_NAME")).trim(), "String", "35"))
            			));
                writeData.append("\r\n");
				
                writeData.append("/").append(ediCommonService.getEdiCharacter(
                		ediCommonService.getDeleteCharStringManifest(Util.getFileWriteDataWithRtrim(Util.null2str(tempHblRow.get("SHIPPER_STREET")).trim(), "String", "35"))
            			));
                writeData.append("\r\n");
				
                writeData.append("/").append(ediCommonService.getEdiCharacter(
                		ediCommonService.getDeleteCharStringManifest(Util.getFileWriteDataWithRtrim(Util.null2str(tempHblRow.get("SHIPPER_PLACE")).trim(), "String", "17"))
            			));
                
                // 주코드
                if (!"".equals(Util.null2str(tempHblRow.get("SHIPPER_STATE_CODE")).trim())) {
					writeData.append("/").append(Util.getFileWriteDataWithRtrim(ediCommonService.getDeleteCharStringManifest(Util.null2str(tempHblRow.get("SHIPPER_STATE_CODE")).trim()), "String", "9"));
                }
                
                writeData.append("\r\n");
                
				writeData.append("/").append(Util.getFileWriteDataWithRtrim(Util.null2str(tempHblRow.get("SHIPPER_COUNTRY_CODE")).trim(), "String", "2"));
				
                //2011.01.06 EU-ICS 시행으로 화주의 우편번호항목이 권고사항으로 추가됐음.
                if (!"".equals(Util.null2str(tempHblRow.get("SHIPPER_ZIPCODE")).trim())) {
	        	    writeData.append("/").append(Util.null2str(tempHblRow.get("SHIPPER_ZIPCODE")).trim());
                }
                
                // 중국세관용 KCNET FHL - SHP 추가 항목 - AMS = N
				if ("Y".equals(Util.null2str(tempHblRow.get("CHINA_SPEC_ADD_YN")).trim())) {
					if ("CN".equals(codeDetailDestCountryCodeStr)
						|| "CN".equals(Util.null2str(tempHblRow.get("VIA_TO_A_COUNTRY_CODE")).trim())
						|| "CN".equals(Util.null2str(tempHblRow.get("VIA_TO_B_COUNTRY_CODE")).trim())
						|| "CN".equals(Util.null2str(tempHblRow.get("VIA_TO_C_COUNTRY_CODE")).trim())) {
					    
						if ("".equals(Util.null2str(tempHblRow.get("SHIPPER_ZIPCODE")).trim())) {	
						    writeData.append("/");
						}
						writeData.append("/TE/").append(Util.getFileWriteDataWithRtrim(ediCommonService.getBLCharCHK(Util.null2str(tempHblRow.get("SHIPPER_TEL")).trim()), "String", "25"));
						// [SHP] FAX번호는 TEL과 동일하게 입력 중국발 999-CA(AIR CHINA) 권고사항 - 2018.06.07
						if ("999".equals(mblNoStr.substring(0, 3))) {
							writeData.append("/FX/").append(Util.getFileWriteDataWithRtrim(ediCommonService.getBLCharCHK(Util.null2str(tempHblRow.get("SHIPPER_TEL")).trim()), "String", "25"));
						}
					}
				}
                
				writeData.append("\r\n");

//				"House Consignee 정보"
                writeData.append("CNE/").append(ediCommonService.getEdiCharacter(
                		ediCommonService.getDeleteCharStringManifest(Util.getFileWriteDataWithRtrim(Util.null2str(tempHblRow.get("CONSIGNEE_NAME")).trim(), "String", "35"))
            			));
                writeData.append("\r\n");
				
                writeData.append("/").append(ediCommonService.getEdiCharacter(
                		ediCommonService.getDeleteCharStringManifest(Util.getFileWriteDataWithRtrim(Util.null2str(tempHblRow.get("CONSIGNEE_STREET")).trim(), "String", "35"))
            			));
                writeData.append("\r\n");
				
				writeData.append("/").append(ediCommonService.getEdiCharacter(
						ediCommonService.getDeleteCharStringManifest(Util.getFileWriteDataWithRtrim(Util.null2str(tempHblRow.get("CONSIGNEE_PLACE")).trim(), "String", "17"))
            			));
				
				// 주코드
                if (!"".equals(Util.null2str(tempHblRow.get("CONSIGNEE_STATE_CODE")).trim())) {
					writeData.append("/").append(Util.getFileWriteDataWithRtrim(ediCommonService.getDeleteCharStringManifest(ediCommonService.getUserStateCode(Util.null2str(tempHblRow.get("CONSIGNEE_STATE_CODE")).trim())), "String", "9"));
                }
                
                writeData.append("\r\n");
				
				writeData.append("/").append(Util.getFileWriteDataWithRtrim(Util.null2str(tempHblRow.get("CONSIGNEE_COUNTRY_CODE")).trim(), "String", "2"));
                
				//2011.01.06 EU-ICS 시행으로 화주의 우편번호항목이 권고사항으로 추가됐음.
				if (!"".equals(Util.null2str(tempHblRow.get("CONSIGNEE_ZIPCODE")).trim())) {
					if ("PL".equals(codeDetailDestCountryCodeStr) && "Y".equals(Util.null2str(tempHblRow.get("PL_SPEC_ADD_YN")).trim())) {
						StringBuffer sb = new StringBuffer(Util.null2str(tempHblRow.get("CONSIGNEE_ZIPCODE")).trim().replaceAll("[^0-9]", ""));
						writeData.append("/").append(sb.insert(2, "-").toString());
	        	    } else {
	        	    	writeData.append("/").append(Util.null2str(tempHblRow.get("CONSIGNEE_ZIPCODE")).trim());
	        	    }
				}
				
				// 중국세관용 KCNET FWB - CNE 추가 항목 - AMS = N
				if ("Y".equals(Util.null2str(tempHblRow.get("CHINA_SPEC_ADD_YN")).trim())) {
					if ("CN".equals(codeDetailDestCountryCodeStr)
						|| "CN".equals(Util.null2str(tempHblRow.get("VIA_TO_A_COUNTRY_CODE")).trim())
						|| "CN".equals(Util.null2str(tempHblRow.get("VIA_TO_B_COUNTRY_CODE")).trim())
						|| "CN".equals(Util.null2str(tempHblRow.get("VIA_TO_C_COUNTRY_CODE")).trim())) {
					    
						if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_ZIPCODE")).trim())) {	
						    writeData.append("/");
						}
						writeData.append("/TE/").append(Util.getFileWriteDataWithRtrim(ediCommonService.getBLCharCHK(Util.null2str(tempHblRow.get("CONSIGNEE_TEL")).trim()), "String", "25"));
						// [CNE] FAX번호는 TEL과 동일하게 입력 중국발 999-CA(AIR CHINA) 권고사항 - 2018.06.07
						if ("999".equals(mblNoStr.substring(0, 3))) {
							writeData.append("/FX/").append(Util.getFileWriteDataWithRtrim(ediCommonService.getBLCharCHK(Util.null2str(tempHblRow.get("CONSIGNEE_TEL")).trim()), "String", "25"));
						}
					}
				}
				
				writeData.append("\r\n");

//				"House CVD"
//                if (!"".equals(Util.null2str(tempHblRow.get("CURRENCY_CODE")).trim())) {
//                	writeData.append("CVD");
//                	writeData.append("/").append(Util.null2str(tempHblRow.get("CURRENCY_CODE")).trim());
//                	
//                	if ("PP".equals(Util.null2str(tempHblRow.get("FREIGHT_TERM")).trim())) {
//                    	writeData.append("/P");
//                	} else {
//                    	writeData.append("/C");
//                	}
//                	
//                	if ("PP".equals(Util.null2str(tempHblRow.get("OTHER_TERM")).trim())) {
//                    	writeData.append("P");
//                	} else {
//                    	writeData.append("C");
//                	}
//                	
//        		    if ("NVD".equals(Util.null2str(tempHblRow.get("DV_CARRIAGE")).trim().replace(".", "")) || "".equals(Util.null2str(tempHblRow.get("DV_CARRIAGE")).trim())) {
//        		    	writeData.append("/NVD");
//        		    } else {
//        		    	writeData.append("/").append(ediCommonService.getDeleteCharStringManifest(Util.null2str(tempHblRow.get("DV_CARRIAGE")).trim()));
//        		    }
//
//        		    if ("NCV".equals(Util.null2str(tempHblRow.get("DV_CUSTOMS")).trim().replace(".", "")) || "".equals(Util.null2str(tempHblRow.get("DV_CUSTOMS")).trim()) || !Util.isNumber(Util.null2str(tempHblRow.get("DV_CUSTOMS")).trim())) {
//        		    	writeData.append("/NCV");
//        		    } else {
//        		    	writeData.append("/").append(ediCommonService.getDeleteCharStringManifest(Util.null2str(tempHblRow.get("DV_CUSTOMS")).trim()));
//        		    }
//        		    
//        		    if ("NIL".equals(Util.null2str(tempHblRow.get("DV_INSURANCE")).trim().replace(".", "")) || "".equals(Util.null2str(tempHblRow.get("DV_INSURANCE")).trim())) {
//        		    	writeData.append("/XXX");
//        		    } else {
//        		    	writeData.append("/").append(ediCommonService.getDeleteCharStringManifest(Util.null2str(tempHblRow.get("DV_INSURANCE")).trim()));
//        		    }
//
//        		    writeData.append("\r\n");
//                }
                
              //"Header"
				if(ediVender.equals("KCNET")){
//					if(i == 0){
//						writeDataHeader.append("<![CDATA[");
//					}
					writeDataHeader.append("UNB+IATA:1+");
					writeDataHeader.append(Util.null2str(ediHostSendInfo.get("IF_PIMA_CODE")));
					writeDataHeader.append(":PIMA:SEL");
					writeDataHeader.append(Util.getFileWriteDataWithRtrim(strForwarderAirline3Code, "String", "3"));
					writeDataHeader.append("+").append(Util.getFileWriteData(Util.null2str(strSecondAirLine), "String", "2"));

					writeDataHeader.append(":PIMA+");
					writeDataHeader.append(Util.null2str(DateTimeUtil.getDate("yyMMdd:HHmm")));	// yyMMdd:HHmm => Ex). 141015:1443
					writeDataHeader.append("+SEL");
					writeDataHeader.append(Util.getFileWriteDataWithRtrim(strForwarderAirline3Code, "String", "3"));
					writeDataHeader.append("+0'UNH+SEL");
					writeDataHeader.append(Util.getFileWriteDataWithRtrim(strForwarderAirline3Code, "String", "3"));
					writeDataHeader.append("+CIMFHL:4+SEL");
					writeDataHeader.append(Util.getFileWriteDataWithRtrim(strForwarderAirline3Code, "String", "3"));
					writeDataHeader.append("'");
					docCode = "CIMFHL";
				}else if(ediVender.equals("KTNET")){
					writeDataHeader.append("UNB+KECA:4+");
					writeDataHeader.append(Util.null2str(ediHostSendInfo.get("IF_SENDER_ID")).trim());
					writeDataHeader.append(":57+");
					writeDataHeader.append(Util.null2str(ediHostSendInfo.get("IF_RECEIVER_ID")).trim());
					writeDataHeader.append(":57+");
					writeDataHeader.append(Util.null2str(DateTimeUtil.getDate("yyMMdd:HHmm")));	//2021-10-08 기존소스 yyMMdd:HHmm에서 스팩기준으로(YYYYMMDD:HHMM) 수정함
					writeDataHeader.append("+01");
					writeDataHeader.append(mblNoStr);
					writeDataHeader.append("'UNH+1+AIRFHL:2'NAD+CG++");
					writeDataHeader.append(Util.null2str(tempMblData.get("VESSEL_FLIGHT")).substring(0, 2));
					writeDataHeader.append("'BIN+");
					lineStr = writeData.toString().split("\r\n");
					for(String line : lineStr){
						bodyLength += line.length() + 2;
					}
					/** 길이체크 오류 발생해서 주석처리     실제 길이보다 적을경우 문제없다고 함 */
//					bodyLength += 7;	//고정7
					writeDataHeader.append(Util.null2str(bodyLength));
					writeDataHeader.append("+");
					docCode = "AIRFHL";
				}

				writeDataHeader.append("FHL/4");
                
                bfw.write(writeDataHeader.toString());
				bfw.newLine();	// 엔터값 추가
				retWriteData.append(writeDataHeader.toString()).append("\r\n");	// DB에 저장할 파일 내용 담기
				
				bfw.write(writeData.toString());
				retWriteData.append(writeData.toString());	// DB에 저장할 파일 내용 담기

//              "Tail"
				writeData.setLength(0);
                if(ediVender.equals("KCNET")){
                	writeData.append("'UNT+3+SEL")
                    .append(strForwarderAirline3Code.trim())
                    .append("'UNZ+1+SEL")
                    .append(strForwarderAirline3Code.trim())
                    .append("'");
				}else if(ediVender.equals("KTNET")){
					writeData.append("'UNT+4+1'UNZ+1+01");
					writeData.append(mblNoStr);
					writeData.append("'");
				}
                
                bfw.write(writeData.toString());
				bfw.newLine();	// 엔터값 추가
				retWriteData.append(writeData.toString()).append("\r\n");	// DB에 저장할 파일 내용 담기                

                if (tempFileCreateHblDataList.size() > 1) {
                    if ((tempFileCreateHblDataList.size() - i) != 1) {
                    	bfw.newLine();	// 엔터값 추가
                    	retWriteData.append("\r\n");	// DB에 저장할 파일 내용 담기
                    }
                }
			}
			
//			if(ediVender.equals("KCNET")){
//				writeData.setLength(0);
//				writeData.append("]]></dataArea></soapenv:Body></soapenv:Envelope>");
//				bfw.write(writeData.toString());
//				bfw.newLine();	// 엔터값 추가
//				retWriteData.append(writeData.toString()).append("\r\n");	// DB에 저장할 파일 내용 담기
//			}
		} catch(Exception e) {
			throw e;
		} finally {
			if (bfw != null) {
				try{ bfw.close(); }catch(Exception e){log.error(Util.getPrintStacTraceString(e));}
			}
		}
		
		retData[0] = fileName;	// 파일명
		retData[1] = retWriteData.toString();	// 파일 내용
		retData[2] = dirPath[0];	// 원본 로컬 경로
		retData[3] = dirPath[1];	// 백업 로컬 경로
		
		retData[4] = docCode;
		retData[5] = mblNoStr;
		return retData;
	}
	
	/**
	 * 항공수출신고번호간이정보 오류검증 및 파일 생성후 전송
	 * 
	 * @param docSend
	 * @param tempMblData
	 * @param tempHblDataList
	 * @param fwSendAccInfo
	 * @param ediHostSendInfo
	 * @param errList
	 * @param result
	 * @return
	 * @throws java.lang.Exception
	 */
	public boolean getEpninf(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempHblDataList, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo, List<Map<String, Object>> errList, Map<String, Object> result, HttpServletRequest request) throws java.lang.Exception {

		String fileCreateRetVal[] = null;									// 파일 생성후 결과값, [0]:파일명, [1]:로컬파일경로, [2]:파일내용
		//////////////////////////////////////////
		// 오류 검증
		//////////////////////////////////////////
		if (!this.getEpninfSendDataValidate(docSend, tempMblData, tempHblDataList, fwSendAccInfo, ediHostSendInfo, errList, result)) {
			return false;
		}
		
		//////////////////////////////////////////
		// 파일 생성
		//////////////////////////////////////////
		fileCreateRetVal = this.getSendDataFileCreateEpninf(docSend, tempMblData, tempHblDataList, fwSendAccInfo, ediHostSendInfo);
		
		
		//////////////////////////////////////////
		// 파일 전송
		//////////////////////////////////////////
		String fileSendYn = "N";
		if ("Y".equals(customProperties.getEdiSendYn())) {	//20210811 기존g1logis소스  if ("Y".equals(Prop.getMessage(profileCls.getProfileName() + "_" + "AIR_EXP_MANIFEST_SEND"))) {
			if(ediCommonService.ftpFileSend(docSend, tempMblData, fileCreateRetVal, ediHostSendInfo, errList, result)) {
				fileSendYn = "Y";
			}
		}else {
			fileSendYn = "Y";
		}
		
		// 원본파일 이동
		String outFileName = ediCommonService.moveFile(fileCreateRetVal[0], fileCreateRetVal[0], fileCreateRetVal);
				
		//////////////////////////////////////////
		// 오더 EDI 상태값 업데이트
		//////////////////////////////////////////
		if(fileSendYn.equals("Y")){
			
		}
		
		/** 처리 로그 기록 */
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		String logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
		String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
		
		tmpMap.clear();
		tmpMap.put("ent_code", docSend.get("entCode"));
		tmpMap.put("customer_code", "");
		tmpMap.put("solution", "ECM");
		tmpMap.put("bound", docSend.get("bound"));
		tmpMap.put("bound_type", docSend.get("boundType"));
		tmpMap.put("hbl_no", "");
		tmpMap.put("if_service_code", docSend.get("ifServiceCode"));
//		tmpMap.put("if_doc_code", fileCreateRetVal[4]);
		tmpMap.put("if_doc_code", "EPNINF");
		tmpMap.put("if_doc_name", "항공수출신고");
		tmpMap.put("if_doc_reference_no", fileCreateRetVal[5]);
		tmpMap.put("if_vender", docSend.get("ediVender"));
		tmpMap.put("if_send_recv_type", "S");
		tmpMap.put("if_send_recv_method", ediHostSendInfo.get("IF_S_R_TYPE"));
		tmpMap.put("if_file_path", fileCreateRetVal[3]);
		tmpMap.put("if_file_name", outFileName);
		tmpMap.put("if_status", "SEND");
		tmpMap.put("if_status_info", "");
		tmpMap.put("if_sender_id", ediHostSendInfo.get("IF_SENDER_ID"));
		tmpMap.put("if_receiver_id", ediHostSendInfo.get("IF_RECEIVER_ID"));
		tmpMap.put("if_date", DateTimeUtil.getNow("yyyy-MM-dd"));
		tmpMap.put("if_date_time", DateTimeUtil.getNow("HH:mm:ss"));
		tmpMap.put("if_remark", "");
		tmpMap.put("register_user_id", docSend.get("registerUserId"));
		tmpMap.put("register_user_name", docSend.get("registerUserName"));
		tmpMap.put("register_date_time", System.currentTimeMillis());
		tmpMap.put("es_job_no", logId);
			
		IndexApi indexApi = new IndexApi(client);
		if (!indexApi.existIndex(index))	indexApi.createIndex(index);
		
		DocumentApi documentApi = new DocumentApi(client);
		documentApi.createDocument(index, logId, tmpMap);
		
		return true;
	}
	public boolean getEpninfSendDataValidate(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempHblDataList, Map<String, Object> fwSendAccInfo
			, Map<String, Object> ediHostSendInfo, List<Map<String, Object>> errList, Map<String, Object> result) throws java.lang.Exception {
		
		String message = null;
		
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("entCode", Util.null2str(docSend.get("entCode")));
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
		
		///////////////////////////
		// HostInfo And BaseData Check
		///////////////////////////
		if (!ediCommonService.getSendHostAndBaseDataValidate(docSend, tempMblData, fwSendAccInfo, ediHostSendInfo, errList, result)) {
			return false;
		}
					
		///////////////////////////
		// Master B/L Check
		///////////////////////////
        String mblNoStr = Util.null2str(tempMblData.get("MBL_NO")).trim();
		String mblNo = new StringBuffer(mblNoStr.substring(0, 3)).append("-").append(mblNoStr.substring(3)).toString();
		
		if (mblNoStr.length() > 11) {
            errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + "MASTER AWB번호가 11자리를 넘을수 없습니다."));
		}
		if (Util.null2str(tempMblData.get("VESSEL_FLIGHT")).trim().length() < 3) {
            tempSearchData.put("srchCode", "MF-07");	// 편명은 최소 3자리 이상이어야만 합니다.
            message = ediCommonService.getErrorCodeName(tempSearchData);
            errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		if ("".equals(Util.null2str(tempMblData.get("LINE_CODE")).trim())) {
            tempSearchData.put("srchCode", "MF-06");	// 항공사를 기재하기 바랍니다.
            message = ediCommonService.getErrorCodeName(tempSearchData);
            errList.add(ediCommonService.getErrorMessage(mblNo, "M.B)/L : " + message));
		}
		if ("".equals(Util.null2str(tempMblData.get("OB_DATE")).trim())) {
            tempSearchData.put("srchCode", "MF-08");	// 출항일을 확인하십시오
            message = ediCommonService.getErrorCodeName(tempSearchData);
            errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		
		///////////////////////////
		//House B/L Check
		///////////////////////////
		List<Map<String, Object>>  customsLicenseList = null;
		Map<String, Object>  customsLicenseMap = null;
		Map<String, Object> tempHblRow = null;
		
		String hblNo = null;
		String hsn = null;
		
		if (tempHblDataList.size() <= 0) {
            tempSearchData.put("srchCode", "MF-43");	// 해당 M.B/L에 H.B/L이 한 건도 없습니다.
            message = ediCommonService.getErrorCodeName(tempSearchData);

            errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		for (int i = 0; i < tempHblDataList.size(); ++i) {
            tempHblRow = tempHblDataList.get(i);
            hblNo = Util.null2str(tempHblRow.get("HBL_NO")).trim();
            hsn = Util.null2str(tempHblRow.get("EDI_HSN"));
            if(Util.null2int(tempHblRow.get("PIECES")) <= 0){
                tempSearchData.put("srchCode", "MF-16");	// House 수량을 기재하기 바랍니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
            
            tempSearchData.put("odJobNo", Util.null2str(tempHblRow.get("OD_JOB_NO")));
            customsLicenseList = manifestMapper.ediManifestCustomsLicenseList(tempSearchData);
            if(customsLicenseList != null && customsLicenseList.size() > 0){
            	customsLicenseMap = customsLicenseList.get(0);
            	if("".equals(Util.null2str(customsLicenseMap.get("LICENSE_NO")).trim())){
                    tempSearchData.put("srchCode", "MF-37");	// 수출면장번호가 없습니다.
                    message = ediCommonService.getErrorCodeName(tempSearchData);
                    errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                }
            	if("Y".equals(Util.null2str(customsLicenseMap.get("PARTIAL_YN")))){
            		if (Util.null2int(customsLicenseMap.get("COMBINE_PIECES")) <= 0) {
                        errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : 수출면장정보에 동시포장갯수가 없습니다.", hblNo, hsn));
                    }
                }else{
                	if(Util.null2int(customsLicenseMap.get("PIECES")) <= 0){
                        tempSearchData.put("srchCode", "MF-44");	// 수출면장정보에 수량이 없습니다.
                        message = ediCommonService.getErrorCodeName(tempSearchData);
                        errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                    }
                }
            	tempHblRow.put("customsLicenseMap", customsLicenseMap);
            }else{
            	tempSearchData.put("srchCode", "MF-37");	// 수출면장번호가 없습니다.
                message = ediCommonService.getErrorCodeName(tempSearchData);
                errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
		}
		
		// 오류내역이 있을 경우
		if (errList.size() > 0) {
			ediCommonService.setValidateMessage(docSend, tempMblData, errList, result);
			return false;
		}
		
		return true;
	}
	/**
	 * 항공수출신고번호간이정보 파일생성
	 * @param docSend
	 * @param tempMblData
	 * @param tempHblDataList
	 * @param fwSendAccInfo
	 * @param ediHostSendInfo
	 * @return
	 * @throws java.lang.Exception
	 */
	public String[] getSendDataFileCreateEpninf(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempHblDataList, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo) throws java.lang.Exception {

		List<Map<String, Object>> tempFileCreateHblDataList = tempHblDataList;
		
		BufferedWriter bfw		= null;
		StringBuffer retWriteData = new StringBuffer();	// 파일 용량이 클경우 내용 저장하면 안됨.
		StringBuffer writeData = new StringBuffer();
		String retData[] = new String[6];
		
		String fileName = new StringBuffer(Util.null2str(docSend.get("registerUserId"))).append(".S").append(Util.null2str(tempMblData.get("MBL_NO")).trim()).append(".txt").toString();	// 전송 파일명
		String dirPath[] = ediCommonService.getSendFilePath(Util.null2str(docSend.get("ediVender")), "edi/manifest", docSend);	// 로컬폴더 경로 가져오기
		
		String mblNoStr = null;
		try {
			bfw = ediCommonService.getFileWriterOpen(fileName, dirPath);	// 쓰기 파일 열기
			mblNoStr = Util.null2str(tempMblData.get("MBL_NO")).trim();
			Map<String, Object> tempHblRow = null;
			Map<String, Object> customsLicenseMap = new HashMap<String, Object>();
			
            writeData.append("UNB+KECA:1+");
			writeData.append(Util.null2str(ediHostSendInfo.get("IF_SENDER_ID")).trim());
			writeData.append(":57+");
			/** 전달받은 샘플내용으로 수정 */
//			writeData.append(Util.null2str(ediHostSendInfo.get("IF_RECEIVER_ID")).trim());
			writeData.append("AIRB2BEPN");
			writeData.append(":57+");
			writeData.append(Util.null2str(DateTimeUtil.getDate("yyMMdd:HHmm")));
			writeData.append("+001");
			writeData.append(mblNoStr);
			writeData.append("'UNH+1+EPNINF:1.0'");
			bfw.write(writeData.toString());
			retWriteData.append(writeData.toString());
			
			for (int i = 0; i < tempFileCreateHblDataList.size(); ++i) {
				tempHblRow = tempFileCreateHblDataList.get(i);
                
                writeData.setLength(0);
				writeData.append("[10]    ");
				writeData.append(Util.getFileWriteData(mblNoStr, "String", "11"));
				writeData.append("C");
				writeData.append(Util.getFileWriteData(Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")), "String", "4"));
				writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("FLIGHT_NO")).substring(0,2), "String", "2"));
				writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("FLIGHT_NO")).substring(2), "String", "5"));
				writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("OB_DATE")).replace("-", ""), "String", "8"));
				writeData.append(Util.getFileWriteData(Util.null2str(tempMblData.get("M_PIECES")).trim(), "Number", "10"));
				writeData.append(Util.getFileWriteData(Util.null2str(tempMblData.get("HBL_COUNT")).trim(), "Number", "10"));
				writeData.append(Util.getFileWriteData(Util.null2str(tempMblData.get("ORIG_CODE")), "String", "3"));
				writeData.append("'I");
				
				if("R".equals(tempHblRow.get("BL_TYPE_IERT"))){	/** 화물구분 : 환적화물 */
					writeData.append("R");
				}else{	/**  화물구분 : 수출 */
					if("Y".equals(tempHblRow.get("CD_TYPE"))){	/** 목록구분 : 목록 */
						writeData.append("X");
					}else{	/** 목록구분 : 일반/이행 */
						writeData.append("E");
					}
				}
				
				writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("HBL_NO")), "String", "20"));
				writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("PIECES")).trim(), "Number", "10"));
				
				customsLicenseMap = (Map)tempHblRow.get("customsLicenseMap");
				writeData.append(Util.getFileWriteData(Util.null2str(customsLicenseMap.get("LICENSE_NO")), "String", "20"));
				writeData.append(Util.getFileWriteData(Util.null2str(customsLicenseMap.get("PARTIAL_YN")), "String", "1"));
				if("Y".equals(customsLicenseMap.get("PARTIAL_YN"))){
					writeData.append(Util.getFileWriteData(Util.null2str(customsLicenseMap.get("PARTIAL_SEQ")), "String", "1"));
					writeData.append(Util.getFileWriteData(Util.null2str(customsLicenseMap.get("COMBINE_PIECES")).trim(), "Number", "10"));
				}else{
					writeData.append("0");
					writeData.append(Util.getFileWriteData(Util.null2str(customsLicenseMap.get("PIECES")).trim(), "Number", "10"));
				}
				writeData.append("'");
				
				bfw.write(writeData.toString());
				retWriteData.append(writeData.toString());
			}
			
			writeData.setLength(0);
			writeData.append("UNT+");
			writeData.append(Util.null2str(tempFileCreateHblDataList.size() * 2 + 2));
			writeData.append("+1'UNZ+1+001");
			writeData.append(mblNoStr);
			writeData.append("'");
			
			bfw.write(writeData.toString());
			retWriteData.append(writeData.toString());	// DB에 저장할 파일 내용 담기
			
			
		} catch(Exception e) {
			throw e;
		} finally {
			if (bfw != null) {
				try{ bfw.close(); }catch(Exception e){log.error(Util.getPrintStacTraceString(e));}
			}
		}
		
		retData[0] = fileName;	// 파일명
		retData[1] = retWriteData.toString();	// 파일 내용
		retData[2] = dirPath[0];	// 원본 로컬 경로
		retData[3] = dirPath[1];	// 백업 로컬 경로
		
		retData[4] = "EPNINF";
		retData[5] = mblNoStr;
		return retData;
	}
}
package org.yht.exps.express.service.edi;

import java.io.BufferedWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.edi.ManifestMapper;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.service.CommonService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class EdiManifestSeaExpSendService {
		
	@Autowired
	ManifestMapper manifestMapper;
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	RestHighLevelClient client;
	@Autowired
	private CustomProperties customProperties;
	
	@Resource(name="ediCommonService")
	EdiCommonService ediCommonService;
	@Resource(name = "commonService")
	CommonService commonService;
	
	/**
	 * 해운수출 적하목록신고 문서송신
	 * 관세청 4세대 전용
	 * 
	 * @param docSend
	 * @param gridRow
	 * @param fwSendAccInfo
	 * @param errList
	 * @param result
	 * @return
	 * @throws java.lang.Exception
	 */
	public boolean setManifestSeaExpSend4th(Map<String, Object> docSend, Map<String, Object> tempMblData, Map<String, Object> fwSendAccInfo, List<Map<String, Object>> errList, Map<String, Object> result, HttpServletRequest request) throws Exception{
		
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
		String ifServiceCode = Util.null2str(docSend.get("ifServiceCode"));
		if ("".equals(ifServiceCode)) {
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
		
		//////////////////////////////////////////
		// 전송대상 B/L 자료 조회
		//////////////////////////////////////////
//		tempMblData = ediCommonService.getEdiManifestValidatorMblOdInfo(docSend);		// 적하목록 전송 오류검증 M.B/L 오더정보 조회
		
		if ("".equals(Util.null2str(tempMblData.get("MASTER_OD_JOB_NO")).trim())) {
			// 에러 메시지 가져오기 조건 세팅
			tempSearchData.clear();
			tempSearchData.put("entCode", Util.null2str(docSend.get("entCode")));
			tempSearchData.put("srchCode", "MF-125");
			tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage("시스템정보", message));
			ediCommonService.setValidateMessage(docSend, tempMblData, errList, result);
			
			return false;
		}
		tempHblDataList = ediCommonService.getEdiManifestValidatorHblOdList(docSend);	// 적하목록 전송 오류검증 H.B/L 오더정보 조회
		
		//////////////////////////////////////////
		// 본/지사 정보 가져오기( 중개사업자별 문서 및 송/수신 정보, TB_SMS_ENT_OFFICE_INTERFACE )
		//////////////////////////////////////////
		if(ifServiceCode.equals("KLNET-PRISM-SEA-EXP-TXT")){
			List<String> ifServiceCodeList = Arrays.asList( "KLNET-PRISM-SEA-EXP-TXT", "KLNET-PRISM-SEA-EXP-CUS");
			docSend.put("ifServiceCodeIn", ifServiceCodeList);
		}
		ediHostSendInfo = manifestMapper.getEdiInterfaceInfo(docSend);
		if(ediHostSendInfo != null && "KLNET-PRISM-SEA-EXP-CUS".equals(ediHostSendInfo.get("IF_SERVICE_CODE"))){
			docSend.put("klnetSendType", "CUS");
		}
		
		//////////////////////////////////////////
		// 오류검증 및 파일 생성후 전송
		//////////////////////////////////////////
		if (!this.getManifestSeaExpSend4th(docSend, tempMblData, tempHblDataList, fwSendAccInfo, ediHostSendInfo, errList, result, request)) {
			return false;
		}
				
		// 2016.07.06 박현익 : Status 저장
//		if (tempMblData != null) {
//			docSend.put("odJobNo", tempMblData.get("OD_JOB_NO"));
//			ediCommonService.setMfStatus(docSend);
//		}
//		
//		if (tempHblDataList != null) {
//			for (int i = 0; i < tempHblDataList.size(); i++) {
//				docSend.put("odJobNo", tempHblDataList.get(i).get("OD_JOB_NO"));
//				ediCommonService.setMfStatus(docSend);
//			}
//		}
		
		// 정상처리:0000
		result.put("processCode", "0000");
		
		return true;
	}
	
	/**
	 * 해운수출 적하목록 오류검증 및 파일 생성후 전송
	 * 관세청 4세대 전용
	 * 
	 * @param docSend
	 * @param gridRow
	 * @param tempMblData
	 * @param tempHblDataList
	 * @param fwSendAccInfo
	 * @param ediHostSendInfo
	 * @param errList
	 * @param result
	 * @return
	 * @throws Exception
	 */
	public boolean getManifestSeaExpSend4th(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempHblDataList, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo, List<Map<String, Object>> errList, Map<String, Object> result, HttpServletRequest request) throws Exception{
		String fileCreateRetVal[] = null;									// 파일 생성후 결과값, [0]:파일명, [1]:로컬파일경로, [2]:파일내용
		
		//////////////////////////////////////////
		// 오류 검증
		//////////////////////////////////////////
		if (!this.getManifestSeaExpSendDataValidate4th(docSend, tempMblData, tempHblDataList, fwSendAccInfo, ediHostSendInfo, errList, result)) {
			return false;
		}
		
		if("errorCheck".equals(docSend.get("action"))){
			return true;
		}
		
		//////////////////////////////////////////
		// 파일 생성
		//////////////////////////////////////////
		if("CUS".equals(docSend.get("klnetSendType"))){
			fileCreateRetVal = this.getSendDataFileCreate4thKlnetCus(docSend, tempMblData, tempHblDataList, fwSendAccInfo, ediHostSendInfo);
		}else{
			fileCreateRetVal = this.getSendDataFileCreate4th(docSend, tempMblData, tempHblDataList, fwSendAccInfo, ediHostSendInfo);
		}
		
		//////////////////////////////////////////
		// 파일 전송
		//////////////////////////////////////////
		if ("Y".equals(customProperties.getEdiSendYn())) {	//20210811 기존g1logis소스 if ("Y".equals(Prop.getMessage(profileCls.getProfileName() + "_" + "SEA_EXP_MANIFEST_SEND"))) {
			if (!ediCommonService.ftpFileSend(docSend, tempMblData, fileCreateRetVal, ediHostSendInfo, errList, result)) {
				return false;
			}
		}
			
		
		// 원본파일 이동
		String outFileName = ediCommonService.moveFile(fileCreateRetVal[0], fileCreateRetVal[0], fileCreateRetVal);
		
		//////////////////////////////////////////
		// 오더 EDI 상태값 업데이트
		//////////////////////////////////////////
		ediCommonService.setOdEdiStatus(docSend, tempMblData, tempHblDataList);
		manifestMapper.updateMrnResultType(tempMblData);
		
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
		
//		tmpMap.clear();
//		tmpMap.put("entCode", docSend.get("entCode"));
//		tmpMap.put("entBusinessCode", docSend.get("entBusinessCode"));
//		tmpMap.put("gmtTime", docSend.get("gmtTime"));
//		tmpMap.put("userId", docSend.get("updateUserId"));
//		tmpMap.put("odJobNo", docSend.get("odJobNo"));
//		tmpMap.put("bound", docSend.get("bound"));
//		tmpMap.put("boundType", docSend.get("boundType"));
//		tmpMap.put("blNo", docSend.get("mblNo"));
//		commonService.sendKakaoAlarmGroup(tmpMap, "MANIFEST");
		
		return true;
	}
	
	/**
	 * 해운수출 적하목록 전송내역 오류검증
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
	 * @throws Exception
	 */
	public boolean getManifestSeaExpSendDataValidate4th(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempHblDataList
			, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo, List<Map<String, Object>> errList, Map<String, Object> result) throws Exception{
		String message = null;
		
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("entCode", Util.null2str(docSend.get("entCode")));
		tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		
		///////////////////////////
		// HostInfo And BaseData Check
		///////////////////////////
		if (!ediCommonService.getSendHostAndBaseDataValidate(docSend, tempMblData, fwSendAccInfo, ediHostSendInfo, errList, result)) {
			return false;
		}
		
		///////////////////////////
		// Master B/L Check
		///////////////////////////
		String mblNo = Util.null2str(tempMblData.get("MBL_NO")).trim().substring(0, 3) + "-" + Util.null2str(tempMblData.get("MBL_NO")).trim().substring(3, Util.null2str(tempMblData.get("MBL_NO")).trim().length());
		
		if ("".equals(Util.null2str(tempMblData.get("EDI_MRN")).trim())) {
			tempSearchData.put("srchCode", "MF-65");	// 적하목록관리번호(MRN)를 기재 바랍니다.
			message = ediCommonService.getErrorCodeName(tempSearchData);
 
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		} else {
			if (Util.null2str(tempMblData.get("EDI_MRN")).trim().length() != 11) {
				tempSearchData.put("srchCode", "MF-66");	// 적하목록관리번호(MRN)의 자릿수를 확인하기 바랍니다.
				message = ediCommonService.getErrorCodeName(tempSearchData);
	 
				errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
			}else{
				if (!Util.isNumber(Util.null2str(tempMblData.get("EDI_MRN")).substring(0, 2))) {
					tempSearchData.put("srchCode", "MF-67");	// 적하목록관리번호(MRN)의 연도부분을 확인하기 바랍니다.
					message = ediCommonService.getErrorCodeName(tempSearchData);
		 
					errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
				}
				
				if (!ediCommonService.verifyMRNCheckDigitOK(Util.null2str(tempMblData.get("EDI_MRN")).trim())) {
					tempSearchData.put("srchCode", "MF-68");	// 적하목록관리번호(MRN)의 Check Digit를 확인하기 바랍니다.
					message = ediCommonService.getErrorCodeName(tempSearchData);
		 
					errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
				}
			}
			
			if ("".equals(Util.null2str(tempMblData.get("SHIP_LINE_SIGN")).trim())) {
				tempSearchData.put("srchCode", "MF-69");	// 적하목록관리번호(MRN)의 선사부호가 존재하지 않습니다.
				message = ediCommonService.getErrorCodeName(tempSearchData);
	 
				errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
			}
		}

		if ("".equals(Util.null2str(tempMblData.get("MBL_NO")).trim())) {
			tempSearchData.put("srchCode", "MF-70");	// M.B/L 번호를 기재 바랍니다.
			message = ediCommonService.getErrorCodeName(tempSearchData);
 
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		
		if (Util.null2str(tempMblData.get("MBL_NO")).trim().length() > 20) {
			tempSearchData.put("srchCode", "MF-136");	// M.B/L 번호는 20자리를 넘을 수 없습니다.
			message = ediCommonService.getErrorCodeName(tempSearchData);
 
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		
		if ("".equals(Util.null2str(tempMblData.get("LINE_CODE")).trim())) {
			tempSearchData.put("srchCode", "MF-73");	// 선사코드를 기재 바랍니다.
			message = ediCommonService.getErrorCodeName(tempSearchData);
 
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
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
        List<Map<String, Object>>  customsLicenseCheckListWithGroupBy = null;
        List<Map<String, Object>>  customsLicenseCheckList = null;
        List<Map<String, Object>>  customsLicenseCheckListWithCode = null;
        List<Map<String, Object>>  containerList = null;
        
        Map<String, Object> tempMapSearchData = new HashMap<String, Object>();
        Map<String, Object> tempCustomsLicenseInfo = null;
        Map<String, Object> tempUpdateData = null;
        Map<String, Object> tempHblRow = null;
		
        String hblNo = null;
        String hsn = null;
		String checkHblNo = null;
		
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
			
			// 하우스 건수 확인하기 위해 '''@@@ 2016-01-15
			docSend.put("hblchkSize", tempHblDataList.size());
			//////////////////////
			// House 검증
			//////////////////////
			if ("".equals(Util.null2str(tempHblRow.get("MBL_NO")).trim())) {
				tempSearchData.put("srchCode", "MF-70");	// M.B/L 번호를 기재 바랍니다.
				message = ediCommonService.getErrorCodeName(tempSearchData);
	 
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
			} else {
				if(Util.null2str(tempHblRow.get("MBL_NO")).trim().length() > 20) {
					tempSearchData.put("srchCode", "MF-71");	// M.B/L 번호는 16자리 이하여야 합니다.
					message = ediCommonService.getErrorCodeName(tempSearchData);
		 
					errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
				}
			}
			
			if ("".equals(Util.null2str(checkHblNo).trim())) {
	        	tempSearchData.put("srchCode", "MF-74");	// House B/L 번호를 기재하기 바랍니다.
	        	message = ediCommonService.getErrorCodeName(tempSearchData);
	 
	        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
	        } else {
	        	if(Util.null2str(checkHblNo).trim().length() > 20) {
		        	tempSearchData.put("srchCode", "MF-75");	// House B/L 은 16자리를 넘을수 없습니다.
		        	message = ediCommonService.getErrorCodeName(tempSearchData);
		 
		        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
		        }
	        }
			
			if (!"E".equals(Util.null2str(tempHblRow.get("BL_TYPE_IERT")).trim()) && !"R".equals(Util.null2str(tempHblRow.get("BL_TYPE_IERT")).trim())) {
	        	tempSearchData.put("srchCode", "MF-76");	// E 또는 R의 화물 구분을 기재하기 바랍니다.
	        	message = ediCommonService.getErrorCodeName(tempSearchData);
	 
	        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
	        }
			
			if ("".equals(Util.null2str(tempHblRow.get("ORIG_CODE")).trim())) {
	        	tempSearchData.put("srchCode", "MF-77");	// 출발지를 기재하기 바랍니다.
	        	message = ediCommonService.getErrorCodeName(tempSearchData);
	 
	        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
	        } else {
	        	if (Util.null2str(tempHblRow.get("ORIG_CODE")).trim().length() != 5) {
					tempSearchData.put("srchCode", "MF-78");	// 출발지 코드가 5자리인지 확인하기 바랍니다.
		        	message = ediCommonService.getErrorCodeName(tempSearchData);
		 
		        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
		        }
	        }
			
			if ("".equals(Util.null2str(tempHblRow.get("DEST_CODE")).trim())) {
	        	tempSearchData.put("srchCode", "MF-79");	// 도착지를 기재하기 바랍니다.
	        	message = ediCommonService.getErrorCodeName(tempSearchData);
	 
	        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
	        } else {
	        	if(Util.null2str(tempHblRow.get("DEST_CODE")).trim().length() != 5) {
					tempSearchData.put("srchCode", "MF-80");	// 도착지 코드가 5자리인지 확인하기 바랍니다.
		        	message = ediCommonService.getErrorCodeName(tempSearchData);
		 
		        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
		        }
	        }
			
			if (Util.null2int(tempHblRow.get("PIECES")) <= 0) {
	        	tempSearchData.put("srchCode", "MF-16");	//HOUSE 수량을 기재하기 바랍니다.
	        	message = ediCommonService.getErrorCodeName(tempSearchData);
	 
	        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
	        }
			
//			if ("".equals(Util.null2str(tempHblRow.get("UNIT_PIECES")).trim())) {
//	        	tempSearchData.put("srchCode", "MF-81");	// 수량단위를 기재하기 바랍니다.
//	        	message = ediCommonService.getErrorCodeName(tempSearchData);
//	 
//	        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
//	        }
			
			if (new BigDecimal(Util.null2str(tempHblRow.get("GWT"), "0")).doubleValue() <= 0) {
	        	tempSearchData.put("srchCode", "MF-17");	// House 중량을 기재하기 바랍니다.
	        	message = ediCommonService.getErrorCodeName(tempSearchData);
	 
	        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
	        }
			
			if (Util.null2int(tempHblRow.get("COUNT_CONTAINER")) <= 0) {
				tempSearchData.put("srchCode", "MF-83");	// 해당 House 에 컨테이너 내역이 없습니다.
	        	message = ediCommonService.getErrorCodeName(tempSearchData);
	 
	        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
			}
			
			if (Util.null2int(tempHblRow.get("COUNT_LICENSE")) <= 0) {
				tempSearchData.put("srchCode", "MF-84");	// 해당 House 에 수출신고번호 내역이 없습니다.
	        	message = ediCommonService.getErrorCodeName(tempSearchData);
	 
	        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
			}
			
			// 세부품목(해상은 세부 품목으로 신고, 항공은 대표품목으로 신고)
			if ("".equals(Util.null2str(tempHblRow.get("COMMODITY_ITEM")).trim())) {
	        	tempSearchData.put("srchCode", "MF-85");	// 품명을 기재하기 바랍니다.
	        	message = ediCommonService.getErrorCodeName(tempSearchData);
	 
	        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
	        }
			
			if ("".equals(Util.null2str(tempHblRow.get("SHIPPER_NAME")).trim())) {
	        	tempSearchData.put("srchCode", "MF-21");	// 송하인 상호를 기재하기 바랍니다.
	        	message = ediCommonService.getErrorCodeName(tempSearchData);
	 
	        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
	        }
			
			if ("".equals(Util.null2str(tempHblRow.get("SHIPPER_ADDRESS")).trim())) {
	        	tempSearchData.put("srchCode", "MF-86");	// 송하인 주소를 기재하기 바랍니다.
	        	message = ediCommonService.getErrorCodeName(tempSearchData);
	 
	        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
	        }
			
			if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_NAME")).trim())) {
	        	tempSearchData.put("srchCode", "MF-87");	// 수하인 상호를 기재하기 바랍니다.
	        	message = ediCommonService.getErrorCodeName(tempSearchData);
	 
	        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
	        }
			
			if ("".equals(Util.null2str(tempHblRow.get("CONSIGNEE_ADDRESS")).trim())) {
	        	tempSearchData.put("srchCode", "MF-89");	// 수하인 주소를 기재하기 바랍니다.
	        	message = ediCommonService.getErrorCodeName(tempSearchData);
	 
	        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
	        }
			
			//수하인사업자번호가없는경우에오류검증에추가하지만EDI전송에영향을주지는않는다.
            if ("DFDS".equals(Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).trim())
            		|| "YHTX".equals(Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).trim())) {
            	
            	if (ediCommonService.getDeleteAlphabetChar(Util.null2str(tempHblRow.get("SHIPPER_BUSINESS_ID")).trim()).length() != 10
            			&& ediCommonService.getDeleteAlphabetChar(Util.null2str(tempHblRow.get("SHIPPER_BUSINESS_ID")).trim()).length() != 13) {
            		tempSearchData.put("srchCode", "MF-120");	// 송하인 사업자번호를기재바랍니다.
    	        	message = ediCommonService.getErrorCodeName(tempSearchData);
    	 
    	        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            	}
                
            	if (Util.null2str(tempHblRow.get("SHIPPER_BUSINESS_ID")).length() == 10) {
            		//사업자번호가9000000000 인경우에는사업자번호를체크하지않는다. (2012.06.28 - 관세청에서받은번호임)
                    if ("9000000000".equals(Util.null2str(tempHblRow.get("SHIPPER_BUSINESS_ID")).trim())) {
                    	if (ediCommonService.getBusinessNoChk(Util.null2str(tempHblRow.get("SHIPPER_BUSINESS_ID")).trim()) == false) {
                    		tempSearchData.put("srchCode", "MF-121");	// 송하인 사업자등록번호의 'Check Digit' 를 확인 하기 바랍니다.
    			        	message = ediCommonService.getErrorCodeName(tempSearchData);
    			 
    			        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                        }
                    }
            	} else {
            		if (Util.null2str(tempHblRow.get("SHIPPER_BUSINESS_ID")).trim().length() != 0) {
            			tempSearchData.put("srchCode", "MF-123");	// 송하인 사업자등록번호를 확인 하기 바랍니다.
			        	message = ediCommonService.getErrorCodeName(tempSearchData);
			        	
			        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                    }
            	}
            }
			
			//////////////////////
			// 컨테이너 조회
			//////////////////////
			tempSearchData.put("odJobNo", Util.null2str(tempHblRow.get("OD_JOB_NO")));
			tempSearchData.put("ifServiceCode", Util.null2str(docSend.get("ifServiceCode")));
			
			containerList = ediCommonService.getEdiManifestContainerList(tempSearchData);
			
			if (containerList.size() > 0) {
				tempHblRow.put("containerList", containerList);
				for (int n = 0; n < containerList.size(); ++n) {
					if ("".equals(Util.null2str(containerList.get(n).get("CNTR_NO")).trim())) {
						tempSearchData.put("srchCode", "MF-91");	// 컨테이너 번호 를 기재 바랍니다.
			        	message = ediCommonService.getErrorCodeName(tempSearchData);
			 
			        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
					}
					
					if (Util.null2str(Util.null2str(containerList.get(n).get("CNTR_NO"))).trim().length() != 11 && !"IN BULK".equals(Util.null2str(Util.null2str(containerList.get(n).get("CNTR_NO"))).trim())) {
						tempSearchData.put("srchCode", "MF-92");	// 컨테이너 번호가 11자리인지 확인하십시오.
						message = ediCommonService.getErrorCodeName(tempSearchData);
			 
						errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
					}
					
					if (!Util.null2str(containerList.get(n).get("CNTR_NO")).trim().contains("IN BULK")) {
						if (Util.null2str(containerList.get(n).get("CNTR_EDI_CODE")).trim().length() == 0) {
							tempSearchData.put("srchCode", "MF-94");	// 컨테이너 규격을 기재 바랍니다.
				        	message = ediCommonService.getErrorCodeName(tempSearchData);
				 
				        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
						}
						
						if (Util.null2str(Util.null2str(containerList.get(n).get("CNTR_EDI_CODE"))).trim().length() != 4) {
						    tempSearchData.put("srchCode", "MF-95");	// 컨테이너규격이 4자리인지 확인하십시오.
						    message = ediCommonService.getErrorCodeName(tempSearchData);
						    
						    errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
						}
						
						if (!"".equals(Util.null2str(Util.null2str(containerList.get(n).get("CNTR_SEAL_NO_1"))).trim())) {
						    if (Util.null2str(Util.null2str(containerList.get(n).get("CNTR_SEAL_NO_1"))).trim().length() > 20) {
						        tempSearchData.put("srchCode", "");
						        tempSearchData.put("message", "컨테이너 봉인번호1 값이 20자리를 넘을수 없습니다.");
						        message = ediCommonService.getErrorCodeName(tempSearchData);
						        
						        errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
						    }
						}
						
						if (Util.null2str(Util.null2str(containerList.get(n).get("CNTR_SEAL_NO_2"))).trim().length() > 20) {
						    tempSearchData.put("srchCode", "");
						    tempSearchData.put("message", "컨테이너 봉인번호2 값이 20자리를 넘을수 없습니다.");
						    message = ediCommonService.getErrorCodeName(tempSearchData);
						    
						    errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
						}
					}
				}
			} else {
				tempSearchData.put("srchCode", "MF-98");	// 컨테이너 정보을 입력하십시오.
	        	message = ediCommonService.getErrorCodeName(tempSearchData);
	 
	        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
			}
			
			//////////////////////
			// 면장번호 조회
			//////////////////////
			boolean bCombFlag = false;
			docSend.put("bCombFlag", bCombFlag);
			
			int nPieces = 0;
			int nCombPieces = 0;
			int nCombineTypeCount = 0;
			int nLicenseCount = 0;
			//총갯수 누적되어 수정 2016-07-27 HIT
			docSend.put("nPieces", nPieces);
			docSend.put("nCombPieces", nCombPieces);
			docSend.put("nLicenseCount", nLicenseCount);
			
			
			tempSearchData.put("odJobNo", Util.null2str(tempHblRow.get("OD_JOB_NO")));
			tempSearchData.put("ifServiceCode", Util.null2str(docSend.get("ifServiceCode")));
			
			customsLicenseList = manifestMapper.ediManifestCustomsLicenseList(tempSearchData);
			customsLicenseCheckListWithGroupBy = manifestMapper.ediManifestCustomsLicenseCheckListWithGroupBy(tempSearchData);
			
			BigDecimal fWeight = new BigDecimal("0");
			//총중량 누적되어 수정 2016-07-27 HIT
			docSend.put("fWeight", fWeight);
			
			for (int n = 0; n < customsLicenseCheckListWithGroupBy.size(); ++n) {
				// 동시포장이 아닐 경우
				if ((Util.null2str(customsLicenseCheckListWithGroupBy.get(n).get("COMBINE_TYPE_A_Z")).trim().length() == 0)) {
					customsLicenseCheckList = manifestMapper.ediManifestCustomsLicenseCheckList(tempSearchData);

					docSend.put("licenseChkSize", customsLicenseCheckList.size());
					if (customsLicenseCheckList != null && customsLicenseCheckList.size() > 0) {
						for (int y = 0; y < customsLicenseCheckList.size(); ++y) {
							// Manifest_LicenseVerify_Check
							this.getManifestLicenseVerifyCheck(docSend,tempHblRow, customsLicenseCheckList.get(y), tempSearchData, errList);
						}
						nPieces = Util.null2int(docSend.get("nPieces")) + Util.null2int(docSend.get("nCombPieces"));
					}
				// 동시포장 일 경우
				} else {
					tempSearchData.put("combineTypeAZ", Util.null2str(customsLicenseCheckListWithGroupBy.get(n).get("COMBINE_TYPE_A_Z")).trim());
					customsLicenseCheckListWithCode = manifestMapper.ediManifestCustomsLicenseList(tempSearchData);
					//면장개수 체크해서 확인해야함 추가 '''@@@ 2016-01-15
					docSend.put("licenseChkSize", customsLicenseCheckListWithCode.size());
					nLicenseCount = 0;
                    nCombineTypeCount = 0;
                    nCombPieces = 0;
					if (customsLicenseCheckListWithCode.size() > 0) {
						for (int y = 0; y < customsLicenseCheckListWithCode.size(); ++y) {
							++nCombineTypeCount;
							// Manifest_LicenseVerify_Check
							this.getManifestLicenseVerifyCheck(docSend,tempHblRow, customsLicenseCheckListWithCode.get(y),tempSearchData, errList);
						}
						
						if ((Util.null2int(docSend.get("nCombPieces")) > 0) && (Util.null2int(docSend.get("nLicenseCount")) > 0)) {
                            nCombPieces = Util.null2int(docSend.get("nCombPieces")) / Util.null2int(docSend.get("nLicenseCount"));
                        }
                        nPieces = Util.null2int(docSend.get("nPieces")) + nCombPieces;
                        
						if (nCombineTypeCount == 1) {
							tempSearchData.put("srchCode", "MF-124");	// 동시 포장인 경우 동일한 동시포장 부호가 2개 이상 이어야 합니다.
				        	message = ediCommonService.getErrorCodeName(tempSearchData);
				 
				        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
                        }
					}
				}
			}
			
			if (customsLicenseList.size() > 0) {
				tempHblRow.put("licenseList", customsLicenseList);
				for (int n = 0; n < customsLicenseList.size(); ++n) {
					if ("".equals(Util.null2str(customsLicenseList.get(n).get("LICENSE_NO")).trim())) {
						tempSearchData.put("srchCode", "MF-37");	// 수출 면장번호가 없습니다.
			        	message = ediCommonService.getErrorCodeName(tempSearchData);
			 
			        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
					}
					
					if (Util.null2int(customsLicenseList.get(n).get("PIECES")) <= 0) {
						tempSearchData.put("srchCode", "MF-44");	// 수출면장 정보에 수량이 없습니다.
			        	message = ediCommonService.getErrorCodeName(tempSearchData);
			 
			        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
					}
					
					if (Util.null2str(customsLicenseList.get(n).get("PIECES")).trim().length() > 8) {
						tempSearchData.put("srchCode", "MF-101");	// 수출 면장 정보에 수량이 9자리 이상 입니다.
			        	message = ediCommonService.getErrorCodeName(tempSearchData);
			 
			        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
					}
					
					if ("".equals(Util.null2str(customsLicenseList.get(n).get("UNIT_PIECES")).trim())) {
						tempSearchData.put("srchCode", "MF-102");	// 수출 면장 정보에 포장단위가 없습니다.
			        	message = ediCommonService.getErrorCodeName(tempSearchData);
			 
			        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
					}
					
					if (new BigDecimal(Util.null2str(customsLicenseList.get(n).get("GWT"), "0")).doubleValue() <= 0) {
			        	tempSearchData.put("srchCode", "MF-45");	// 수출 면장 정보에 중량이 없습니다.
			        	message = ediCommonService.getErrorCodeName(tempSearchData);
			 
			        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
			        }
					
					if (!"".equals(Util.null2str(customsLicenseList.get(n).get("COMBINE_TYPE_A_Z")).trim())) {
						if (Util.null2int(customsLicenseList.get(n).get("COMBINE_PIECES")) <= 0) {
							tempSearchData.put("srchCode", "MF-103");	// 수출 면장 정보에 동시포장수량이 없습니다.
				        	message = ediCommonService.getErrorCodeName(tempSearchData);
				 
				        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
						}
					}
					
					if ("".equals(Util.null2str(customsLicenseList.get(n).get("PARTIAL_YN")).trim())) {
						tempSearchData.put("srchCode", "MF-104");	// 수출 면장 정보에 분할선적여부가 없습니다.
			        	message = ediCommonService.getErrorCodeName(tempSearchData);
			 
			        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
					}
					
					if ("Y".equals(Util.null2str(customsLicenseList.get(n).get("PARTIAL_YN")).trim())) {
						if (Util.null2int(customsLicenseList.get(n).get("PARTIAL_SEQ")) <= 0) {
							tempSearchData.put("srchCode", "MF-105");	// 수출 면장 정보에 분할차수가 없습니다.
				        	message = ediCommonService.getErrorCodeName(tempSearchData);
				        	
				        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
						}
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
				if (Util.null2int(tempHblRow.get("PIECES")) != nPieces) {
					tempSearchData.put("srchCode", "MF-106");	// H.B/L 의 총개수와 License의 총개수가 일치하지 않습니다.
		        	message = ediCommonService.getErrorCodeName(tempSearchData);
		 
		        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + tempCustomsLicenseInfo.get("LICENSE_NO") + ", " + message, hblNo, hsn));
	            }
				
				bCombFlag = (boolean)docSend.get("bCombFlag");
				
                if (bCombFlag == false) {
                	fWeight = (BigDecimal)docSend.get("fWeight");
                	
                	if (new BigDecimal(Util.null2str(tempHblRow.get("GWT"), "0")).doubleValue() <= 30) {
                    	// House B/L 중량이 30kg 이하인 경우 체크하지 않는다.
                    } else if (new BigDecimal(Util.null2str(tempHblRow.get("GWT"), "0")).doubleValue() > 30
                    		&& new BigDecimal(Util.null2str(tempHblRow.get("GWT"), "0")).doubleValue() < 100) {
                    	
                    	//House B/L 중량이 100kg 미만인 경우 수출신고서 중량을 기준으로 50% 이상 차이나면 오류통보
                        if ((new BigDecimal(Util.null2str(tempHblRow.get("GWT"), "0")).multiply(new BigDecimal("0.5"))).doubleValue() <  (new BigDecimal(Util.null2str(tempHblRow.get("GWT"), "0")).subtract(fWeight)).abs().doubleValue()) {
                        	tempSearchData.put("srchCode", "MF-107");	// H.B/L 의 총중량과 License의 총중량이 일치하지 않습니다.
        		        	message = ediCommonService.getErrorCodeName(tempSearchData);
        		 
        		        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + tempCustomsLicenseInfo.get("LICENSE_NO") + ", " + message, hblNo, hsn));
                        }
                    } else if (new BigDecimal(Util.null2str(tempHblRow.get("GWT"), "0")).doubleValue() >= 100) {
                    	
                    	//House B/L 중량이 100kg 이상인 경우 수출신고서 중량을 기준으로 30% 이상 차이나면 오류통보
                        if ((new BigDecimal(Util.null2str(tempHblRow.get("GWT"), "0")).multiply(new BigDecimal("0.3"))).doubleValue() < (new BigDecimal(Util.null2str(tempHblRow.get("GWT"), "0")).subtract(fWeight)).abs().doubleValue()) {
                        	tempSearchData.put("srchCode", "MF-107");	// H.B/L 의 총중량과 License의 총중량이 일치하지 않습니다.
        		        	message = ediCommonService.getErrorCodeName(tempSearchData);
        		 
        		        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + tempCustomsLicenseInfo.get("LICENSE_NO") + ", " + message, hblNo, hsn));
                        }
                    }
                }
			}
		}	// end of for-loop.
		
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
			//ediCommonDao.setEdiProcessInfoUpdate(tempUpdateData);
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
	public String[] getSendDataFileCreate4th(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempHblDataList, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo) throws java.lang.Exception {
		
		List<Map<String, Object>> tempFileCreateHblDataList = tempHblDataList;
		List<Map<String, Object>> containerList = null;
		List<Map<String, Object>> customsLicenseList = null;
		
		BufferedWriter bfw = null;
		
		StringBuffer retWriteData = new StringBuffer();	// 파일 용량이 클경우 내용 저장하면 안됨.
		StringBuffer writeData = null;
		String retData[] = new String[6];
		
		String fileName = new StringBuffer(Util.null2str(docSend.get("registerUserId"))).append(".H").append(Util.null2str(tempMblData.get("MBL_NO")).trim()).append(".txt").toString();	// 전송 파일명
		String dirPath[] = ediCommonService.getSendFilePath(Util.null2str(docSend.get("ediVender")), "edi/manifest", docSend);	// 로컬폴더 경로 가져오기
		
		String ediVender = Util.null2str(docSend.get("ediVender"));

		// 파일 내용 쓰기
		try {
			bfw = ediCommonService.getFileWriterOpen(fileName, dirPath);	// 쓰기 파일 열기
			
			Map<String, Object> tempHblRow = null;
			Map<String, Object> tempSearchData = null;
			
			String tempHblNo = null;
			
			int nUntCnt = 0;
			
			writeData = new StringBuffer();
            writeData.append("UNB+KECA:1+");
            writeData.append(Util.null2str(ediHostSendInfo.get("IF_SENDER_ID")).trim());
			if ("KTNET".equals(ediVender)) {
				writeData.append(":57+");
			} else {
				writeData.append(":KL+");
			}
            writeData.append(Util.null2str(ediHostSendInfo.get("IF_RECEIVER_ID")).trim());
			if ("KTNET".equals(ediVender)) {
				writeData.append(":57+");
			} else {
				writeData.append(":KL+");
			}
            writeData.append(Util.null2str(DateTimeUtil.getDate("yyMMdd:HHmm")));
            writeData.append("+001"); //UFMS에서는 KTNET, KLNET 모두 003으로 되어있음 / 지서희
            writeData.append(Util.null2str(tempMblData.get("EDI_MRN")).trim());
            writeData.append("'");
            writeData.append("UNH+1+EXHMFS");
            writeData.append(":6.0'");	// 해운 수출 문서 버전 5.1 (4세대 6.0) 으로 변경'''@@@
            ++nUntCnt;
            
            writeData.append("EHDR");
            writeData.append(Util.getFileWriteData(Util.null2str(tempMblData.get("EDI_MRN")).trim(), "String", "11"));
            writeData.append(Util.getFileWriteData(Util.null2str(tempMblData.get("SHIP_LINE_SIGN")).trim(), "String", "4"));
            writeData.append(Util.getFileWriteData(Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).trim(), "String", "4"));
            writeData.append(Util.getFileWriteData(Util.null2str(tempMblData.get("OB_DATE")).replace("-", "").trim(), "String", "8"));
            writeData.append(Util.getFileWriteData(Util.null2str(tempMblData.get("HBL_COUNT")).trim(), "Number", "4"));
            writeData.append("'");
            ++nUntCnt;

            /*///////////////////////////////////////////////////
             * House 생성 시작
             *//////////////////////////////////////////////// */
			for (int i = 0; i < tempFileCreateHblDataList.size(); ++i) {
				tempHblRow = tempFileCreateHblDataList.get(i);
				
				tempSearchData = new HashMap<String, Object>();
				tempSearchData.put("entCode", Util.null2str(docSend.get("entCode")));
				tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
				
				// "House 생성 시작"
                tempHblNo = ediCommonService.getHblDeleteChar(Util.null2str(tempHblRow.get("HBL_NO")).trim());
                
                writeData.append("EIBL");
                writeData.append(Util.getFileWriteData(ediCommonService.getBLCharCHK(Util.null2str(tempMblData.get("MBL_NO")).trim()), "String", "20")); //'''@@@ 4세대 20자리로 변경
                writeData.append(Util.getFileWriteData(ediCommonService.getBLCharCHK(tempHblNo), "String", "20"));//'''@@@ 4세대 20자리로 변경
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("BL_TYPE_IERT")).trim(), "String", "1"));
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("ORIG_CODE")).trim(), "String", "5"));
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("DEST_CODE")).trim(), "String", "5"));
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("DEPARTURE_NOTICE_YN")).trim(), "String", "1"));
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("LC_NO")).trim(), "String", "35"));
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("PIECES")).trim(), "Number", "10"));//'''@@@ 포장개수 10자리로 늘어남
//                writeData.append(Util.getFileWriteData(ediCommonService.getPiecesUnit(Util.null2str(tempHblRow.get("UNIT_PIECES")).trim(), docSend), "String", "2"));
                writeData.append("GT");
                
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("GWT")).trim(), "Number", "17,3")); //'''@@@ 4세대 중량 소수점포함 17자리로 늘어남 소수점은 3자리까지
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("CBM"), "0").trim(), "Number", "13,3")); //'''@@@ 4세대 용적 소수점포함 13자리로 늘어남 소수점은 3자리까지
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("EDI_SPECIAL_CODE")).trim(), "String", "4"));
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("EDI_SPECIAL_CODE_A")).trim(), "String", "4"));
                writeData.append(Util.getFileWriteData("", "String", "2"));
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("COUNT_CONTAINER")).trim(), "Number", "4"));
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("COUNT_LICENSE")).trim(), "Number", "4"));
                writeData.append(Util.getFileWriteData(ediCommonService.getEdiCharacter(Util.null2str(tempHblRow.get("COMMODITY_ITEM")).trim()), "String", "200"));
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("EDI_LOADING_PLACE_CODE")).trim(), "String", "8"));
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("EDI_UCR_NO")).trim(), "String", "35"));
                writeData.append("'");
                ++nUntCnt;
                
                writeData.append("ESHP");
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("SHIPPER_BUSINESS_ID")).trim(), "String", "15"));
                writeData.append(Util.getFileWriteData(ediCommonService.getEdiCharacter(Util.null2str(tempHblRow.get("SHIPPER_NAME")).trim()), "String", "100")); //'''@@@ 4세대 상호명 100자리로 늘어남
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("SHIPPER_COUNTRY_CODE")).trim(), "String", "2"));
                writeData.append(Util.getFileWriteData(ediCommonService.getDeleteAlphabetChar(Util.null2str(tempHblRow.get("SHIPPER_TEL")).trim()), "String", "25"));
                writeData.append(Util.getFileWriteData(ediCommonService.getDeleteAlphabetChar(Util.null2str(tempHblRow.get("SHIPPER_FAX")).trim()), "String", "25"));
                writeData.append(Util.getFileWriteData(ediCommonService.getDeleteAlphabetChar(Util.null2str(tempHblRow.get("SHIPPER_ZIPCODE")).trim()), "String", "10"));
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("SHIPPER_PLACE_CODE")).trim(), "String", "5")); //'''@@@ 4세대 도시코드 추가됨 총 5자리
                writeData.append(Util.getFileWriteData(ediCommonService.getEdiCharacter(Util.null2str(tempHblRow.get("SHIPPER_ADDRESS")).replace("\r\n", "").trim()), "String", "150")); //'''@@@ 4세대 주소 150자리로 변경됨
                writeData.append("'");
                ++nUntCnt;
                
                writeData.append("ECNE");
                writeData.append(Util.getFileWriteData(ediCommonService.getEdiCharacter(Util.null2str(tempHblRow.get("CONSIGNEE_NAME")).trim()), "String", "100")); //'''@@@ 4세대 상호명 100자리로 늘어남
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("CONSIGNEE_COUNTRY_CODE")).trim(), "String", "2"));
                writeData.append(Util.getFileWriteData(ediCommonService.getDeleteAlphabetChar(Util.null2str(tempHblRow.get("CONSIGNEE_TEL")).trim()), "String", "25"));
                writeData.append(Util.getFileWriteData(ediCommonService.getDeleteAlphabetChar(Util.null2str(tempHblRow.get("CONSIGNEE_FAX")).trim()), "String", "25"));
                writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("CONSIGNEE_PLACE_CODE")).trim(), "String", "5")); //'''@@@ 4세대 도시코드 추가됨 총 5자리
                writeData.append(Util.getFileWriteData(ediCommonService.getEdiCharacter(Util.null2str(tempHblRow.get("CONSIGNEE_ADDRESS")).replace("\r\n", "").trim()), "String", "150"));//'''@@@ 4세대 주소 150자리로 변경됨
                writeData.append("'");
                ++nUntCnt;
                
    			if ("KTNET".equals(ediVender)){
    				writeData.append("ENFY");
                    writeData.append(Util.getFileWriteData(ediCommonService.getEdiCharacter(Util.null2str(tempHblRow.get("NOTIFY_NAME")).trim()), "String", "100")); //'''@@@ 4세대 상호명 100자리로 늘어남
                    writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("NOTIFY_COUNTRY_CODE")).trim(), "String", "2"));
                    writeData.append(Util.getFileWriteData(ediCommonService.getDeleteAlphabetChar(Util.null2str(tempHblRow.get("NOTIFY_TEL")).trim()), "String", "25"));
                    writeData.append(Util.getFileWriteData(ediCommonService.getDeleteAlphabetChar(Util.null2str(tempHblRow.get("NOTIFY_FAX")).trim()), "String", "25"));
                    writeData.append(Util.getFileWriteData(ediCommonService.getDeleteAlphabetChar(Util.null2str(tempHblRow.get("NOTIFY_PLACE_CODE")).trim()), "String", "5")); //'''@@@ 4세대 도시코드 추가됨 총 5자리
                    writeData.append(Util.getFileWriteData(ediCommonService.getEdiCharacter(Util.null2str(tempHblRow.get("NOTIFY_ADDRESS")).replace("\r\n", "").trim()), "String", "150"));//'''@@@ 4세대 주소 150자리로 변경됨
                    writeData.append("'");
    			} else {
    				writeData.append("ENFY");
                    writeData.append(Util.getFileWriteData(ediCommonService.getEdiCharacter(Util.null2str(tempHblRow.get("NOTIFY_NAME")).trim()), "String", "100")); //'''@@@ 4세대 상호명 100자리로 늘어남
                    if (!"".equals(Util.null2str(tempHblRow.get("NOTIFY_NAME")).trim())) {
                    	writeData.append(Util.getFileWriteData(Util.null2str(tempHblRow.get("NOTIFY_COUNTRY_CODE")).trim(), "String", "2"));
                    } else {
                    	writeData.append(Util.getFileWriteData("", "String", "2"));
                    }
                    writeData.append(Util.getFileWriteData(ediCommonService.getDeleteAlphabetChar(Util.null2str(tempHblRow.get("NOTIFY_TEL")).trim()), "String", "25"));
                    writeData.append(Util.getFileWriteData(ediCommonService.getDeleteAlphabetChar(Util.null2str(tempHblRow.get("NOTIFY_FAX")).trim()), "String", "25"));
                    if (!"".equals(Util.null2str(tempHblRow.get("NOTIFY_NAME")).trim())) {
                    	if (!"".equals(Util.null2str(tempHblRow.get("NOTIFY_COUNTRY_CODE")).trim()) && !"".equals(Util.null2str(tempHblRow.get("NOTIFY_PLACE_CODE")).trim())) {
                    		writeData.append(Util.getFileWriteData(ediCommonService.getDeleteAlphabetChar(Util.null2str(tempHblRow.get("NOTIFY_PLACE_CODE")).trim()), "String", "5")); //'''@@@ 4세대 도시코드 추가됨 총 5자리	
                    	} else {
                    		writeData.append(Util.getFileWriteData("", "String", "5"));
                    	}
                    } else {
                    	writeData.append(Util.getFileWriteData("", "String", "5"));
                    }
                    writeData.append(Util.getFileWriteData(ediCommonService.getEdiCharacter(Util.null2str(tempHblRow.get("NOTIFY_ADDRESS")).replace("\r\n", "").trim()), "String", "150"));//'''@@@ 4세대 주소 150자리로 변경됨
                    writeData.append("'");
    			}
                ++nUntCnt;
                
                //////////////////////
				// "컨테이너 생성"
				//////////////////////
				tempSearchData.put("odJobNo", Util.null2str(tempHblRow.get("OD_JOB_NO")));
				tempSearchData.put("ifServiceCode", Util.null2str(docSend.get("ifServiceCode")));
				
//				containerList = ediCommonService.getEdiManifestContainerList(tempSearchData);
				containerList = (List)tempHblRow.get("containerList");
				
				if (containerList.size() > 0) {
					for (int n = 0; n < containerList.size(); ++n) {
						
						writeData.append("ECTR");
						writeData.append(Util.getFileWriteData(Util.null2str(containerList.get(n).get("CNTR_NO")).trim(), "String", "11"));
						writeData.append(Util.getFileWriteData(Util.null2str(containerList.get(n).get("CNTR_EDI_CODE")).trim(), "String", "4"));
						writeData.append(Util.getFileWriteData(Util.null2str(containerList.get(n).get("CNTR_SEAL_NO_1")).trim(), "String", "20"));
						writeData.append(Util.getFileWriteData(Util.null2str(containerList.get(n).get("CNTR_SEAL_NO_2")).trim(), "String", "20"));
						writeData.append("'");
						
						++nUntCnt;
					}
				}
                
                //////////////////////
                // "라이센스 건수체크 및 생성"
				//////////////////////
				tempSearchData.put("odJobNo", Util.null2str(tempHblRow.get("OD_JOB_NO")));
				tempSearchData.put("ifServiceCode", Util.null2str(docSend.get("ifServiceCode")));
				
//				customsLicenseList = manifestMapper.ediManifestCustomsLicenseList(tempSearchData);
				customsLicenseList = (List)tempHblRow.get("licenseList");
				
				if (customsLicenseList.size() > 0) {
					for (int y = 0; y < customsLicenseList.size(); ++y) {
	                	if (!"".equals(Util.null2str(customsLicenseList.get(y).get("LICENSE_NO")).trim())) {
	                		writeData.append("EXNO");
							writeData.append(Util.getFileWriteData(Util.null2str(customsLicenseList.get(y).get("LICENSE_NO")).trim(), "String", "19"));//'''@@@ 4세대 수출신고번호 19자리로 늘어남
							writeData.append(Util.getFileWriteData(Util.null2str(customsLicenseList.get(y).get("PIECES")).trim(), "Number", "10"));//'''@@@ 포장개수 10자리로 늘어남
							writeData.append(Util.getFileWriteData(ediCommonService.getPiecesUnit(Util.null2str(customsLicenseList.get(y).get("UNIT_PIECES")).trim(), docSend), "String", "2"));
							writeData.append(Util.getFileWriteData(Util.null2str(customsLicenseList.get(y).get("GWT")).trim(), "Number", "17,3"));//'''@@@ 4세대 중량 소수점포함 17자리로 늘어남 소수점은 3자리까지
							
							if (Util.null2str(customsLicenseList.get(y).get("COMBINE_TYPE_A_Z")).trim().length() > 0) {
								writeData.append(Util.getFileWriteData(Util.null2str(customsLicenseList.get(y).get("COMBINE_TYPE_A_Z")).trim(), "String", "1"));
								writeData.append(Util.getFileWriteData(Util.null2str(customsLicenseList.get(y).get("COMBINE_PIECES")).trim(), "Number", "10"));//'''@@@ 포장개수 10자리로 늘어남

								if ("".equals(Util.null2str(customsLicenseList.get(y).get("COMBINE_UNIT_PIECES")).trim())) {
									writeData.append(Util.getFileWriteData(ediCommonService.getPiecesUnit(Util.null2str(customsLicenseList.get(y).get("UNIT_PIECES")).trim(), docSend), "String", "2"));
								} else {
									writeData.append(Util.getFileWriteData(ediCommonService.getPiecesUnit(Util.null2str(customsLicenseList.get(y).get("COMBINE_UNIT_PIECES")).trim(), docSend), "String", "2"));
								}
							} else {
								writeData.append(Util.getFileWriteData("", "String", "13"));
							}
							
							if ("Y".equals(Util.null2str(customsLicenseList.get(y).get("PARTIAL_YN")).trim())) {
								writeData.append(Util.getFileWriteData("Y", "String", "1"));
								writeData.append(Util.getFileWriteData(Util.null2str(customsLicenseList.get(y).get("PARTIAL_SEQ")).trim(), "String", "1"));
							} else {
								writeData.append(Util.getFileWriteData("N", "String", "1"));
								writeData.append(Util.getFileWriteData("", "String", "1"));
							}
							
							writeData.append("'");
							
							++nUntCnt;
	                	}
					}
				}
			}	// end of for-loop.
			
			++nUntCnt;
			
			writeData.append("UNT+").append(nUntCnt).append("+1'");
			writeData.append("UNZ+1+001").append(Util.null2str(tempMblData.get("EDI_MRN")).trim()).append("'");//UFMS에서는 KTNET, KLNET 모두 003 / 지서희
			
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
		
		retData[4] = "EXHMFS";
		retData[5] = Util.getFileWriteData(ediCommonService.getBLCharCHK(Util.null2str(tempMblData.get("MBL_NO")).trim()), "String", "20");
		return retData;
	}
	public String[] getSendDataFileCreate4thKlnetCus(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempFileCreateHblDataList
			, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo) throws java.lang.Exception {
		
		BufferedWriter bfw = null;
		
		StringBuffer retWriteData = new StringBuffer();	// 파일 용량이 클경우 내용 저장하면 안됨.
		StringBuffer writeData = null;
		String retData[] = new String[6];
		
		String fileName = new StringBuffer(Util.null2str(docSend.get("registerUserId"))).append(".H").append(Util.null2str(tempMblData.get("MBL_NO")).trim()).append(".txt").toString();	// 전송 파일명
		String dirPath[] = ediCommonService.getSendFilePath(Util.null2str(docSend.get("ediVender")), "edi/manifest", docSend);	// 로컬폴더 경로 가져오기
		
		// 파일 내용 쓰기
		try {
			bfw = ediCommonService.getFileWriterOpen(fileName, dirPath);	// 쓰기 파일 열기
			
			Map<String, Object> tempHblRow = null;
			
			String now = DateTimeUtil.getNow("yyyyMMddHHmmss");
			int nUntCnt = 0;
			String addressStr, nameStr, tmpStr;
			
			writeData = new StringBuffer();
			writeData.append("UNB+KECA:1+");
			writeData.append(Util.null2str(ediHostSendInfo.get("IF_SENDER_ID")).trim());
			writeData.append(":KL+");
			writeData.append(Util.null2str(ediHostSendInfo.get("IF_RECEIVER_ID")).trim());
			writeData.append(":KL+");
			writeData.append(Util.null2str(DateTimeUtil.getDate("yyMMdd:HHmm")));
			writeData.append("+001"); //UFMS에서는 KTNET, KLNET 모두 003으로 되어있음 / 지서희
			writeData.append(Util.null2str(tempMblData.get("EDI_MRN")).trim());
			writeData.append("'");
			
			/** CUSREP */
			writeData.append("UNH+1+CUSREP:S:93A:KE'");
			
			writeData.append("BGM+5CC+");
			writeData.append(Util.null2str(tempMblData.get("EDI_MRN")));	/** 적하목록 관리번호 */
			writeData.append("+");
			writeData.append(Util.null2str(tempMblData.get("SEND_TYPE")));	/** 전송구분(9:원본, 35:재전송) */
			writeData.append("'");
			
			writeData.append("DTM+150:" + now + ":204'");	/** 제출일시 */
			
			writeData.append("GIS+SMB:5CH:KCS'");
			
			/** Segment Group 2 */
			writeData.append("LOC+42+");
			writeData.append(Util.null2str(tempMblData.get("CUSTOMS_CODE")));	/** 세관 */
			writeData.append(":113:KCS+");
			writeData.append(Util.null2str(tempMblData.get("CUSTOMS_DPT_CODE")));	/** 과 */
			writeData.append(":5AB:KCS'");
			
			/** Segment Group 5 */
			writeData.append("NAD+CS+");
			writeData.append(Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")));	/** 화물운송주선업자부호 */
			writeData.append(":5CG:KCS'");
			
			writeData.append("NAD+CA+");
			writeData.append(Util.null2str(tempMblData.get("SHIP_LINE_SIGN")));	/** 선박회사 */
			writeData.append(":172:KCS'");
			
			/** Segment Group 8 */
			writeData.append("TDT+20+");
			writeData.append(Util.null2str(tempMblData.get("VOYAGE")));	/** 항차 */
			writeData.append("+++:::");
			writeData.append(Util.null2str(tempMblData.get("VESSEL_FLIGHT")));	/** 선박명 */
			writeData.append("+++");
			writeData.append(Util.null2str(tempMblData.get("VESSEL_SIGN")));	/** 선박호출부호 */
			writeData.append(":103:::");
			writeData.append(Util.null2str(tempMblData.get("DEST_COUNTRY_CODE")));	/** 국가부호(도착지포트의 국가코드) */
			writeData.append("'");
			
			/** Segment Group 9  */
			writeData.append("LOC+9+");
			writeData.append(Util.null2str(tempMblData.get("ORIG_CODE")));	/** 적재항 */
			writeData.append("'");
			
			writeData.append("DTM+136:");
			/** 출항일시 */
			tmpStr = Util.null2str(tempMblData.get("OB_TIME"));
			writeData.append(Util.null2str(tempMblData.get("OB_DATE")).replace("-", ""));
			if(!tmpStr.equals("")){
				writeData.append(tmpStr.replace(":", ""));
			}else{
				writeData.append("0000");
			}
			writeData.append("00:204'");
			
			writeData.append("UNT+11+1'");
			
			/** CUSMAN */
			writeData.append("UNH+2+CUSMAN:S:93A:KE'");
			nUntCnt++;
			
			writeData.append("BGM+5CD+");
			writeData.append(Util.null2str(tempMblData.get("EDI_MRN")));	/** 적하목록 관리번호 */
			writeData.append("+");
			writeData.append(Util.null2str(tempMblData.get("SEND_TYPE")));	/** 전송구분(9:원본, 35:재전송) */
			writeData.append("'");
			nUntCnt++;
			
			writeData.append("GIS+SMB:5CH:KCS'");
			nUntCnt++;
			
			/** Segment Group 3 */
			writeData.append("DMS+");
			writeData.append(Util.null2str(tempMblData.get("MBL_NO")));
			writeData.append("+704'");
			nUntCnt++;
			
			writeData.append("MEA+CT++NMB:");
			writeData.append(Util.null2str(tempMblData.get("HBL_COUNT")));
			writeData.append("'");
			nUntCnt++;
			
			List<Map<String, Object>> tmpList;
			for (int i = 0; i < tempFileCreateHblDataList.size(); ++i) {
				tempHblRow = tempFileCreateHblDataList.get(i);
				/** Segment Group 7 */
				writeData.append("RFF+BH:");
				writeData.append(Util.null2str(tempHblRow.get("HBL_NO")));	/** House B/L번호 */
				writeData.append("'");
				nUntCnt++;
				
				writeData.append("GIS+");
				writeData.append(Util.null2str(tempHblRow.get("BL_TYPE_IERT")));	/** 화물구분부호 */
				writeData.append(":105:KCS'");
				nUntCnt++;
				
				writeData.append("FTX+AAA+++");
				/** 품명 */
				tmpStr = Util.null2str(tempHblRow.get("COMMODITY_ITEM"));
				writeData.append(Util.cutLength(tmpStr, 0, 50));
				writeData.append(":");
				writeData.append(Util.cutLength(tmpStr, 50, 50));
				writeData.append(":");
				writeData.append(Util.cutLength(tmpStr, 100, 50));
				writeData.append(":");
				writeData.append(Util.cutLength(tmpStr, 150, 50));
				writeData.append("'");
				nUntCnt++;
				
				/** 화물고유식별번호   M/C : C 전송안함 */
				/** 적재장소  M/C : C 전송안함 */
				
				writeData.append("MEA+CT++");
				writeData.append(Util.null2str(tempHblRow.get("UNIT_PIECES_EDI"), "CT"));	/** 포장종류 */
				writeData.append(":");
				writeData.append(Util.null2str(tempHblRow.get("PIECES")));	/** 포장개수 */
				writeData.append("'");
				nUntCnt++;
				
				writeData.append("MEA+WT++");
				writeData.append("KG");	/** 중량단위 */
				writeData.append(":");
				writeData.append(Util.null2str(tempHblRow.get("GWT")));	/** 중량 */
				writeData.append("'");
				nUntCnt++;
				
				writeData.append("MEA+VOL++");
				writeData.append("MTQ");	/** 용적단위 */
				writeData.append(":");
				writeData.append(Util.null2str(tempHblRow.get("CBM")));	/** 총용적 */
				writeData.append("'");
				nUntCnt++;
				
				/** Segment Group 8 */
				addressStr = Util.null2str(tempHblRow.get("SHIPPER_ADDRESS"));	/** 송하인주소 */
				nameStr = Util.null2str(tempHblRow.get("SHIPPER_NAME"));	/** 송하인상호 */
				writeData.append("NAD+CZ++");
				writeData.append(Util.cutLength(addressStr, 0, 35));
				writeData.append(":");
				writeData.append(Util.cutLength(addressStr, 35, 35));
				writeData.append(":");
				writeData.append(Util.cutLength(addressStr, 70, 35));
				writeData.append(":");
				writeData.append(Util.cutLength(addressStr, 105, 35));
				writeData.append(":");
				writeData.append(Util.cutLength(addressStr, 140, 10));
				writeData.append("+");
				writeData.append(Util.cutLength(nameStr, 0, 35));
				writeData.append(":");
				writeData.append(Util.cutLength(nameStr, 35, 35));
				writeData.append(":");
				writeData.append(Util.cutLength(nameStr, 70, 30));
				writeData.append("+++");
				writeData.append(Util.null2str(tempMblData.get("ORIG_CODE")));	/** 송하인도시코드 - 출발지포트코드로 입력 */
				writeData.append("++");
				writeData.append(Util.null2str(tempMblData.get("ORIG_COUNTRY_CODE")));	/** 송하인국가코드 - 출발지포트의 국가코드로 입력 */
				writeData.append("'");
				nUntCnt++;
				
				writeData.append("COM+");
				writeData.append(Util.cutLength(ediCommonService.getDeleteAlphabetChar(Util.null2str(tempHblRow.get("SHIPPER_TEL"))), 25));	/** 송하인전화번호 */
				writeData.append(":TE'");
				nUntCnt++;
				
				addressStr = Util.null2str(tempHblRow.get("CONSIGNEE_ADDRESS"));	/** 수하인주소 */
				nameStr = Util.null2str(tempHblRow.get("CONSIGNEE_NAME"));	/** 수하인상호 */
				writeData.append("NAD+CN++");
				writeData.append(Util.cutLength(addressStr, 0, 35));
				writeData.append(":");
				writeData.append(Util.cutLength(addressStr, 35, 35));
				writeData.append(":");
				writeData.append(Util.cutLength(addressStr, 70, 35));
				writeData.append(":");
				writeData.append(Util.cutLength(addressStr, 105, 35));
				writeData.append(":");
				writeData.append(Util.cutLength(addressStr, 140, 10));
				writeData.append("+");
				writeData.append(Util.cutLength(nameStr, 0, 35));
				writeData.append(":");
				writeData.append(Util.cutLength(nameStr, 35, 35));
				writeData.append(":");
				writeData.append(Util.cutLength(nameStr, 70, 30));
				writeData.append("+++");
				writeData.append(Util.null2str(tempMblData.get("DEST_CODE")));	/** 수하인도시코드 - 도착지포트코드로 입력 */
				writeData.append("++");
				writeData.append(Util.null2str(tempMblData.get("DEST_COUNTRY_CODE")));	/** 수하인국가코드 - 도착지포트의 국가코드로 입력 */
				writeData.append("'");
				nUntCnt++;
				
				writeData.append("COM+");
				writeData.append(Util.cutLength(ediCommonService.getDeleteAlphabetChar(Util.null2str(tempHblRow.get("CONSIGNEE_TEL"))), 25));	/** 수하인전화번호 */
				writeData.append(":TE'");
				nUntCnt++;
				
				/** 통지처    상호만 'SAME AS CONSIGNEE' 로 입력 */
				writeData.append("NAD+NI+++SAME AS CONSIGNEE+++'");
				nUntCnt++;
				
				/** Segment Group 9  */
				tmpList = (List)tempHblRow.get("licenseList");
				if(tmpList != null && tmpList.size() > 0) {
					for(Map<String, Object> licenseMap : tmpList){
						writeData.append("DOC+85:::");	/** 수출신고/화물관리번호 구분코드(830 :수출신고 85 :적하목록) */
						writeData.append(Util.null2str(licenseMap.get("PARTIAL_YN")));	/** 분할선적여부 */
						writeData.append("+");
						writeData.append(Util.null2str(licenseMap.get("LICENSE_NO")));	/** 수출신고/화물신고번호 */
						writeData.append("::");
						writeData.append(Util.null2str(licenseMap.get("COMBINE_TYPE_A_Z")));	/** 동시포장그룹부호 */
						writeData.append("'");
						nUntCnt++;
						
						writeData.append("MEA+CT++");
						writeData.append(Util.null2str(licenseMap.get("UNIT_PIECES_EDI"), "CT"));	/** 선적포장종류코드 */
						writeData.append(":");
						writeData.append(Util.null2str(licenseMap.get("PIECES")));	/** 포장개수 */
						writeData.append("'");
						nUntCnt++;
						
						if(!Util.null2str(licenseMap.get("COMBINE_TYPE_A_Z")).equals("")){
							writeData.append("MEA+5AA++");
							writeData.append(Util.null2str(licenseMap.get("UNIT_PIECES_EDI"), "CT"));	/** 동시포장종류코드 */
							writeData.append(":");
							writeData.append(Util.null2str(licenseMap.get("COMBINE_PIECES")));	/** 동시포장개수 */
							writeData.append("'");
							nUntCnt++;
						}
						
						writeData.append("MEA+WT++");
						writeData.append("KG");	/** 중량단위 */
						writeData.append(":");
						writeData.append(Util.null2str(licenseMap.get("GWT")));	/** 중량 */
						writeData.append("'");
						nUntCnt++;
						
						if("Y".equals(licenseMap.get("PARTIAL_YN"))){
							writeData.append("MEA+AAU++SCO:");
							writeData.append(Util.null2str(licenseMap.get("PARTIAL_SEQ")));	/** 분할선적차수 */
							writeData.append("'");
							nUntCnt++;
						}
					}
				}
				
				/** Segment Group 10 */
				tmpList = (List)tempHblRow.get("containerList");
				if(tmpList != null && tmpList.size() > 0) {
					for(Map<String, Object> containerMap : tmpList){
						writeData.append("EQD+CN'");
						nUntCnt++;
						
						writeData.append("EQN+");
						writeData.append(Util.null2str(tempHblRow.get("COUNT_CONTAINER")));	/** 컨테이너 개수(Master B/L 별)     하우스별 컨테이너 개수로 입력 */
						writeData.append("'");
						nUntCnt++;
						
						writeData.append("EQD+CN+");
						writeData.append(Util.null2str(containerMap.get("CNTR_NO")));	/** 컨테이너 번호(M-B/L) */
						writeData.append("+");
						writeData.append(Util.null2str(containerMap.get("CNTR_EDI_CODE")));	/** 컨테이너 규격 */
						writeData.append("'");
						nUntCnt++;
						
						/** 봉인번호 없으면 항목 기재하지 않음 **/
						tmpStr = Util.null2str(containerMap.get("CNTR_SEAL_NO_1"));
						if(!tmpStr.equals("")) {
							writeData.append("SEL+");
							writeData.append(Util.cutLength(tmpStr, 10));	/** 봉인번호 1 */
							if(tmpStr.length() > 10){
								writeData.append("+:::");
								writeData.append(Util.cutLength(tmpStr, 10, 10));	/** 봉인번호 1, 10자리 초과 시 기재 */
							}
							writeData.append("'");
							nUntCnt++;
						}
						
						tmpStr = Util.null2str(containerMap.get("CNTR_SEAL_NO_2"));
						if(!tmpStr.equals("")) {
							writeData.append("SEL+");
							writeData.append(Util.cutLength(tmpStr, 10));	/** 봉인번호 2 */
							if(tmpStr.length() > 10){
								writeData.append("+:::");
								writeData.append(Util.cutLength(tmpStr, 10, 10));	/** 봉인번호 2, 10자리 초과 시 기재 */
							}
							writeData.append("'");
							nUntCnt++;
						}
					}
				}
			}
			
			nUntCnt++;
			writeData.append("UNT+").append(nUntCnt).append("+2'");
			writeData.append("UNZ+1+001").append(Util.null2str(tempMblData.get("EDI_MRN")).trim()).append("'");//UFMS에서는 KTNET, KLNET 모두 003 / 지서희
			
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
		
		retData[4] = "CUSREP_5CC";
		retData[5] =Util.null2str(tempMblData.get("MBL_NO"));
		return retData;
	}
	
	/**
	 * H.B/L 오류검증
	 * 
	 * @param docSend
	 * @param tempHblRow
	 * @param customsLicenseCheckList
	 * @param tempSearchData
	 * @param errList
	 * @throws Exception
	 */
	public void getManifestLicenseVerifyCheck(Map<String, Object> docSend, Map<String, Object> tempHblRow, Map<String, Object> customsLicenseCheckList, Map<String, Object> tempSearchData, List<Map<String, Object>> errList) throws Exception {
		String message = null;
		String hblNo = Util.null2str(tempHblRow.get("HBL_NO")).trim();
		String hsn = Util.null2str(tempHblRow.get("EDI_HSN"));
		
		String strLicenseNo =Util.null2str(customsLicenseCheckList.get("LICENSE_NO")).trim();
		
		boolean bCombFlag = (boolean)docSend.get("bCombFlag");
		
		int nPieces = Util.null2int(docSend.get("nPieces"));
		int nCombPieces = Util.null2int(docSend.get("nCombPieces"));
		int nLicenseCount = Util.null2int(docSend.get("nLicenseCount"));
		
		if (Integer.parseInt(Util.null2str((docSend.get("hblchkSize")))) > 1) {
			if (Integer.parseInt(Util.null2str(docSend.get("licenseChkSize"))) == 1) {
				nPieces = 0;
				nCombPieces = 0;
				nLicenseCount = 0;
			}
		}
		
		BigDecimal fWeight = null;
		if (docSend.get("fWeight") == null) {
			fWeight = new BigDecimal("0");
		} else {
			fWeight = (BigDecimal)docSend.get("fWeight");
			if (Integer.parseInt(Util.null2str((docSend.get("hblchkSize")))) > 1) {
				if (Integer.parseInt(Util.null2str(docSend.get("licenseChkSize"))) == 1) {
					fWeight = new BigDecimal("0");
				}
			}
		}
		
        if (strLicenseNo.length() == 0) {
        	tempSearchData.put("srchCode", "MF-126");	// 수출 신고번호를 기재 바랍니다.
        	message = ediCommonService.getErrorCodeName(tempSearchData);
 
        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
        } 
        
        if ("".equals(Util.null2str(customsLicenseCheckList.get("COMBINE_TYPE_A_Z")).trim())) {
        	nPieces += Util.null2int(customsLicenseCheckList.get("PIECES"));
        	
        	if (new BigDecimal(Util.null2str(customsLicenseCheckList.get("GWT"), "0")).doubleValue() <= 0) {
        		tempSearchData.put("srchCode", "MF-129");	// 수출 신고번호의 중량을 기재 바랍니다.
	        	message = ediCommonService.getErrorCodeName(tempSearchData);
	        	
	        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
        	}
        	
        	fWeight = fWeight.add(new BigDecimal(Util.null2str(customsLicenseCheckList.get("GWT"), "0")));
        } else {
        	bCombFlag = true;
        	
            if (Util.null2int(customsLicenseCheckList.get("COMBINE_PIECES")) <= 0) {
            	tempSearchData.put("srchCode", "MF-130");	// 수출 신고번호의 동시포장 구분이 있는 경우에는 동시포장 개수를 기재바랍니다.
	        	message = ediCommonService.getErrorCodeName(tempSearchData);
	        	
	        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
            }
            
            if (Util.null2int(customsLicenseCheckList.get("COMBINE_PIECES")) == Util.null2int(customsLicenseCheckList.get("PIECES"))) {
                nCombPieces += Util.null2int(customsLicenseCheckList.get("PIECES"));
                nLicenseCount += 1;
            } else {
                nPieces += Util.null2int(customsLicenseCheckList.get("PIECES")) - Util.null2int(customsLicenseCheckList.get("COMBINE_PIECES"));
                nCombPieces += Util.null2int(customsLicenseCheckList.get("COMBINE_PIECES"));
                nLicenseCount += 1;
            }
        }
        
        if ("".equals(Util.null2str(customsLicenseCheckList.get("COMBINE_TYPE_A_Z")).trim()) && (Util.null2int(customsLicenseCheckList.get("COMBINE_PIECES")) > 0)) {
        	tempSearchData.put("srchCode", "MF-131");	// 수출 신고번호의 동시포장 개수는 동시포장 구분이 있는 경우에 만 기재 바랍니다
        	message = ediCommonService.getErrorCodeName(tempSearchData);
        	
        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
        }
        if (("Y".equals(Util.null2str(customsLicenseCheckList.get("PARTIAL_YN")).trim())) && (Util.null2int(customsLicenseCheckList.get("PARTIAL_SEQ")) <= 0)) {
        	tempSearchData.put("srchCode", "MF-132");	// 수출 신고번호의 분할선적 구분이 'Y' 인 경우에는 분할선적 차수를 기재 바랍니다
        	message = ediCommonService.getErrorCodeName(tempSearchData);
        	
        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
        }
        if (("N".equals(Util.null2str(customsLicenseCheckList.get("PARTIAL_YN")).trim())) && (Util.null2int(customsLicenseCheckList.get("PARTIAL_SEQ")) > 0)) {
        	tempSearchData.put("srchCode", "MF-133");	// 분할선적 구분이 'N' 인경우 에는 분할선적 차수를 기재 하지마십시오.
        	message = ediCommonService.getErrorCodeName(tempSearchData);
        	
        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message, hblNo, hsn));
        }
        
		docSend.put("nPieces", nPieces);
		docSend.put("nCombPieces", nCombPieces);
		docSend.put("nLicenseCount", nLicenseCount);
		docSend.put("fWeight", fWeight);
		docSend.put("bCombFlag", bCombFlag);
	}
}
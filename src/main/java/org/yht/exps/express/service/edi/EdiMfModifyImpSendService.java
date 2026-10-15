package org.yht.exps.express.service.edi;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.edi.AbnormalMapper;
import org.yht.exps.express.mapper.edi.ManifestMapper;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.edi.SendManifest;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.service.CommonService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.FileUtil;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class EdiMfModifyImpSendService {
		
	@Autowired
	ManifestMapper manifestMapper;
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	AbnormalMapper abnormalMapper;
	@Autowired
	RestHighLevelClient client;
	@Autowired
	private CustomProperties customProperties;
	
	@Resource(name="ediCommonService")
	EdiCommonService ediCommonService;
	@Resource(name = "commonService")
	CommonService commonService;
	
	/**
	 * 수입 적하목록정정신고 문서송신(4세대)
	 * @param sendManifest
	 * @return
	 * @throws java.lang.Exception
	 */
	public OrderSaveResult sendManifestModifyImp4th(SendManifest sendManifest, HttpServletRequest request) throws java.lang.Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> docSend = new HashMap<String, Object>();
		Map<String, Object> result = new HashMap<String, Object>();
		StringBuffer processMessage = new StringBuffer();
		
		docSend.put("entCode", sendManifest.getEntCode());
		docSend.put("entBusinessCode", sendManifest.getEntBusinessCode());
		docSend.put("solution", sendManifest.getSolution());
		docSend.put("bound", sendManifest.getBound());
		docSend.put("boundType", sendManifest.getBoundType());
		docSend.put("userLanguage", sendManifest.getUserLanguage());
		docSend.put("gmtTime", sendManifest.getGmtTime());
		docSend.put("docType", "MANIFEST-ABNORMAL");
		docSend.put("registerUserId", sendManifest.getRegisterUserId());
		docSend.put("registerUserName", sendManifest.getRegisterUserName());
		docSend.put("contractDate", sendManifest.getContractDate());
		docSend.put("srchEntCode", sendManifest.getEntCode());
		docSend.put("srchEntBusinessCode", sendManifest.getEntBusinessCode());
		docSend.put("srchSolution", sendManifest.getSolution());
		docSend.put("srchBound", sendManifest.getBound());
		docSend.put("srchBoundType", sendManifest.getBoundType());
		docSend.put("cryptographyYn", sendManifest.getCryptographyYn());
        
		// 에러 메시지 가져오기 조건 세팅
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("entCode", Util.null2str(docSend.get("entCode")));
		tempSearchData.put("srchCode", "");
		tempSearchData.put("message", "적하목록 정정신청이 완료 되었습니다.");
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
		String message = ediCommonService.getErrorCodeName(tempSearchData);
		
		List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();
		Map<String, Object> tempMblData = new HashMap<String, Object>();
		Map<String, Object> fwSendAccInfo = null;
		String ifServiceCode = null;
		
		docSend.put("modJobNo", sendManifest.getModJobNo());
		// M.B/L 오더정보 조회
		tempMblData = manifestMapper.getEdiMfModifyValidatorValidatorMblOdInfo(docSend);
				
		if(tempMblData != null && tempMblData.size() > 0) {
			processMessage.append("<li>M.B/L No.");				
			docSend.put("srchMblNo", Util.null2str(tempMblData.get("MBL_NO")));
				
			// 서비스 코드 세팅
			ifServiceCode = ediCommonService.getServiceCode(Util.null2str(docSend.get("solution")), Util.null2str(docSend.get("bound")), Util.null2str(docSend.get("boundType")), Util.null2str(docSend.get("docType")), Util.null2str(tempMblData.get("EDI_VENDER")).trim());
			docSend.put("ifServiceCode", ifServiceCode);
			docSend.put("ediVender", Util.null2str(tempMblData.get("EDI_VENDER")).trim());
			docSend.put("mblJobNo", Util.null2str(tempMblData.get("MOD_JOB_NO")).trim());
			docSend.put("mblNo", Util.null2str(tempMblData.get("MBL_NO")).trim());
				
			// 사업장정보 가져오기( 포워더 송/수신 계정정보, TB_SMS_ENT_BUS_FMS )
			fwSendAccInfo = manifestMapper.getFwSendAccInfo(docSend);
			
			if(!this.setMfModifyAirSeaImpSend4th(docSend, tempMblData, fwSendAccInfo, errList, result, request)) {
				if(result.get("processCode") == null || "".equals(Util.null2str(result.get("processCode")))) {
					result.put("processCode", "A00001");
				}
			}
			processMessage.append("<br>");
			processMessage.append(Util.null2str(tempMblData.get("MBL_NO")));
		}else {
			result.put("processCode", "0000");
			
			tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "M.B/L 자료가 존재하지 않습니다.");
			tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
			tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
			message = ediCommonService.getErrorCodeName(tempSearchData);
		}	
		
		
		processMessage.append("<br><li>" + message + "</li>");
		result.put("processMessage", processMessage.toString());
		
		orderSaveResult.setResultData(result);
		return orderSaveResult;
	}
	
	/**
	 * 수출 적하목록정정신고 문서송신(4세대)
	 * @param docSend
	 * @param tempMblData
	 * @param fwSendAccInfo
	 * @param result
	 * @return
	 * @throws java.lang.Exception
	 */
	public boolean setMfModifyAirSeaImpSend4th(Map<String, Object> docSend, Map<String, Object> tempMblData, Map<String, Object> fwSendAccInfo, List<Map<String, Object>> errList, Map<String, Object> result, HttpServletRequest request) throws java.lang.Exception {
		List<Map<String, Object>> tempHblDataList = null;
		Map<String, Object> ediHostSendInfo = null;
		
		Map<String, Object> tempSearchData = null;
		String message = null;
		
		// 에러 메시지 가져오기 조건 세팅
		tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
		tempSearchData.put("entCode", Util.null2str(docSend.get("entCode")));
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		
		//////////////////////////////////////////
		// 서비스코드 및 중계사업자 체크
		//////////////////////////////////////////
		if("".equals(Util.null2str(docSend.get("ifServiceCode")))) {
			tempSearchData.put("srchCode", "MF-51");	// 현재 지원되지 않는 인터페이스 스펙(서비스코드) 입니다.
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage("본지사정보", Util.null2str(docSend.get("ediVender")) + "(" + Util.null2str(docSend.get("docType")) + ") : "+ message));
			ediCommonService.setValidateMessage(docSend, tempMblData, errList, result);
			return false;
		}
		
		if("".equals(Util.null2str(docSend.get("ediVender")))) {
			tempSearchData.put("srchCode", "MF-50");	// 해당 M.B/L에 중개사업자가 없습니다.
			message = ediCommonService.getErrorCodeName(tempSearchData);
			errList.add(ediCommonService.getErrorMessage(Util.null2str(docSend.get("mblNo")), "M.B/L : " + message));
			ediCommonService.setValidateMessage(docSend, tempMblData, errList, result);
			
			return false;
		}
		
		//////////////////////////////////////////
		// 전송대상 B/L 자료 조회
		//////////////////////////////////////////
		tempHblDataList = manifestMapper.getEdiMfModifyValidatorHblOdList(docSend);
		 
		//////////////////////////////////////////
		// 본/지사 정보 가져오기( 중개사업자별 문서 및 송/수신 정보, TB_SMS_ENT_OFFICE_INTERFACE )
		//////////////////////////////////////////				
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(docSend, "ifServiceCodeIn", false, null, null, null, null);
		ediHostSendInfo = manifestMapper.getEdiInterfaceInfo(docSend);
		
		
		//////////////////////////////////////////
		// 오류검증 및 파일 생성후 전송
		//////////////////////////////////////////
		if(!this.getMfModifyAirSeaImpSend4th(docSend, tempMblData, tempHblDataList, fwSendAccInfo, ediHostSendInfo, errList, result, request)) {
			return false;
		}
				
		result.put("processCode", "0000");
		
		return true;
	}
	
	/**
     * 수출 정정신고 오류검증 및 파일 생성후 전송 '''@@@
     * 관세청 4세대 전용
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
	public boolean getMfModifyAirSeaImpSend4th(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempHblDataList, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo, List<Map<String, Object>> errList, Map<String, Object> result, HttpServletRequest request) throws java.lang.Exception {
		
//		tempMblData		// 정정신고 전송 오류검증 M.B/L 오더정보 조회
//		tempHblDataList	// 정정신고 전송 오류검증 H.B/L 오더정보 조회
//		fwSendAccInfo		// 포워더 송/수신 계정정보
//		ediHostSendInfo	// 중계사업자 송신 정보
		String fileCreateRetVal[] = null;									// 파일 생성후 결과값, [0]:파일명, [1]:로컬파일경로, [2]:파일내용
		String fileSendYn = "N";	// 파일 전송 여부( 'Y':전송, 'N':미전송 )
		
		//////////////////////////////////////////
		// 오류 검증
		//////////////////////////////////////////
		if(!this.getDoAirSeaImpSendDataValidate4th(docSend, tempMblData, tempHblDataList, fwSendAccInfo, ediHostSendInfo, errList, result)) {
			return false;
		}
		
		//////////////////////////////////////////
		// 파일 생성
		//////////////////////////////////////////
		if(Util.null2str(docSend.get("ediVender")).equals("KCNET")){
			fileCreateRetVal = this.getSendDataFileCreate4thKCNET(docSend, tempMblData, tempHblDataList, fwSendAccInfo, ediHostSendInfo);
			
			if ("Y".equals(customProperties.getEdiSendYn())) {
				try {
					
					log.info("getMfModifyAirSeaImpSend4th HttpClient start "+ docSend.get("entCode"));
					
					//테스트 http://121.138.113.7:12101 리얼 http://222.122.233.22:12101
					String httpPost = Util.null2str("http://" + ediHostSendInfo.get("IF_SVR_IP")) + ":" + Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")) + "/WebServices/services/XmlDataService";
		//			HttpPost blPost = new HttpPost(httpPost);
		//			blPost.setHeader("Content-type", "text/xml;charset=utf-8");
					HttpClient client = HttpClientBuilder.create().build();
					
					
					HttpPost post = new HttpPost(httpPost);
					post.setHeader("SOAPAction", "");
			        post.setEntity(new StringEntity(fileCreateRetVal[1], StandardCharsets.UTF_8));
			        HttpResponse responseBl = client.execute(post);
			        
			        int responseCode = 0;
			        if(responseBl != null) {
			        	responseCode = responseBl.getStatusLine().getStatusCode();
			        	
			        	if(responseCode == 200 ) {
			        		fileSendYn = "Y";
			        	}else {
			        		fileSendYn = "N";
			        	}
			        	
			        	HttpEntity entity = responseBl.getEntity();
		                if (entity != null) {
		                   String resultEntity = EntityUtils.toString(entity);
		                   log.info("KCNET 항목정정 전송 결과 Response: " + resultEntity);
		                }
			        }
			        
			        log.info("getMfModifyAirSeaImpSend4th HttpClient end "+ docSend.get("entCode") + " responseCode :" + responseCode);
			        
				}catch(Exception e) {
					log.error(e.getMessage());
					throw e;
				}
				
//				if(ediCommonService.ftpFileSend(docSend, tempMblData, fileCreateRetVal, ediHostSendInfo, errList, result)) {
//					fileSendYn = "Y";
//				}
				
			}else{
				fileSendYn = "Y";
			}
		}else{
			fileCreateRetVal = this.getSendDataFileCreate4th(docSend, tempMblData, tempHblDataList, fwSendAccInfo, ediHostSendInfo);
			//////////////////////////////////////////
			// 파일 전송
			//////////////////////////////////////////
			if ("Y".equals(customProperties.getEdiSendYn())) {
				if(ediCommonService.ftpFileSend(docSend, tempMblData, fileCreateRetVal, ediHostSendInfo, errList, result)) {
					fileSendYn = "Y";
				}
			}else{
				fileSendYn = "Y";
			}
		}


		// 파일전송 결과 세팅
		docSend.put("fileSendYn", fileSendYn);
		
		// 원본파일 이동
		String outFileName = ediCommonService.moveFile(fileCreateRetVal[0], fileCreateRetVal[0], fileCreateRetVal);
		
		//////////////////////////////////////////
		// 정정 M.B/L 상태값 업데이트
		//////////////////////////////////////////
		this.setEdiModifyMblAfterSendUpdate(docSend, tempMblData);
		
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
		tmpMap.put("if_doc_code", "MANIFEST_MODIFY");
		tmpMap.put("if_doc_name", "적하목록정정");
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
		
		
		if("Y".equals(fileSendYn)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
     * 항공-해상수입 정정신고 전송내역 오류검증 '''@@@
     * 관세청 4세대 전용 
     * @param docSend
     * @param gridRow
     * @param tempMblData
     * @param tempHblDataList
     * @param fwSendAccInfo
     * @param ediHostSendInfo
     * @param errList
     * @param result
     * @return
     * @throws java.lang.Exception
     */
	public boolean getDoAirSeaImpSendDataValidate4th(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempHblDataList, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo, List<Map<String, Object>> errList, Map<String, Object> result) throws java.lang.Exception {
		
//		tempMblBaseData	// M.B/L 오더정보 조회
//		tempMblData		// 오류검증 M.B/L 오더정보 조회
//		tempHblDataList	// 오류검증 H.B/L 오더정보 조회
//		fwSendAccInfo		// 포워더 송/수신 계정정보
		
		Map<String, Object> tempUpdateData = null;
		Map<String, Object> tempSearchMapData = new HashMap<String, Object>();
		
		Map<String, Object> tempSearchData = null;	// 임시 조회자료
		
		String message = null;
		
		tempSearchData = new HashMap<String, Object>();
		tempSearchData.put("entCode", Util.null2str(docSend.get("entCode")));
		tempSearchData.put("groupCode", "EDI_VERIFY_ERROR_CODE");
		tempSearchData.put("userLanguage", Util.null2str(docSend.get("userLanguage")));
		
		///////////////////////////
		// HostInfo And BaseData Check
		///////////////////////////
		if(!ediCommonService.getSendHostAndBaseDataValidate(docSend, tempMblData, fwSendAccInfo, ediHostSendInfo, errList, result)) {
			return false;
		}
		
		///////////////////////////
		// BaseData Check
		///////////////////////////
        String mblNoStr = Util.null2str(tempMblData.get("MBL_NO")).trim(); //중복 변수 지정 - ditobass!!@
		String boundStr = Util.null2str(docSend.get("bound")).trim(); //중복 변수 지정 - ditobass!!@
		String boundTypeStr = Util.null2str(docSend.get("boundType")).trim(); //중복 변수 지정 - ditobass!!@
        
		// MRN Check 
        String mblNo = new StringBuffer(mblNoStr.substring(0, 3)).append("-").append(mblNoStr.substring(3)).toString();
        
		// 세관 식별자 체크
        if("AIR".equals(boundStr)) {
        	if("IMP".equals(boundTypeStr)) {
        		if(Util.null2str(tempMblData.get("AIR_IMPORT_RECEIVE_ID")).trim().length() == 0) {
                	tempSearchData.put("srchCode", "");
        			tempSearchData.put("message", "세관 식별자가 기재되지 않았습니다.");
        			message = ediCommonService.getErrorCodeName(tempSearchData);
         
        			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
                }
        	} else {
        		if(Util.null2str(tempMblData.get("AIR_EXPORT_RECEIVE_ID")).trim().length() == 0) {
                	tempSearchData.put("srchCode", "");
        			tempSearchData.put("message", "세관 식별자가 기재되지 않았습니다.");
        			message = ediCommonService.getErrorCodeName(tempSearchData);
         
        			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
                }
        	}
        } else if("SEA".equals(boundStr)) {
        	if("IMP".equals(boundTypeStr)) {
        		if(Util.null2str(tempMblData.get("SEA_IMPORT_RECEIVE_ID")).trim().length() == 0) {
                	tempSearchData.put("srchCode", "");
        			tempSearchData.put("message", "세관 식별자가 기재되지 않았습니다.");
        			message = ediCommonService.getErrorCodeName(tempSearchData);
         
        			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
                }
        	} else {
        		if(Util.null2str(tempMblData.get("SEA_EXPORT_RECEIVE_ID")).trim().length() == 0) {
                	tempSearchData.put("srchCode", "");
        			tempSearchData.put("message", "세관 식별자가 기재되지 않았습니다.");
        			message = ediCommonService.getErrorCodeName(tempSearchData);
         
        			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
                }
        	}
        }
        
    	// 항공사 식별자 체크
      //2021-09-07  항공사정정 개발 보류
//        if("AIR".equals(boundStr) && "IMP".equals(boundTypeStr)) {
//        	// 항공사 신고 여부
//        	if("Y".equals(Util.null2str(tempMblData.get("EDI_SEND_AIRLINE_YN")).trim())) {
//    			if(Util.null2str(tempMblData.get("AIR_LINE_ID")).trim().length() == 0) {
//                	tempSearchData.put("srchCode", "");
//        			tempSearchData.put("message", "항공사 식별자가 기재되지 않았습니다.");
//        			message = ediCommonService.getErrorCodeName(tempSearchData);
//         
//        			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
//                }
//    		}
//        }
        
		///////////////////////////
		// Master B/L Check
		///////////////////////////
        if(Util.null2str(tempMblData.get("EDI_MRN")).trim().length() != 11) {
        	tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "MRN번호가 기재되지 않았거나 11자 미만입니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
 
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
        } else {
            if(ediCommonService.verifyMRNCheckDigitOK(Util.null2str(tempMblData.get("EDI_MRN")).trim()) == false) {
            	tempSearchData.put("srchCode", "");
    			tempSearchData.put("message", "MRN번호의 Check Digit를 확인하기 바랍니다.");
    			message = ediCommonService.getErrorCodeName(tempSearchData);
     
    			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
            }
        }

        // MRN 이외의 필수항목체크
        if(Util.null2str(tempMblData.get("MOD_DECLARE_DATE")).trim().length() == 0) {
        	tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "신청일자를 기재 바랍니다");
			message = ediCommonService.getErrorCodeName(tempSearchData);
 
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
        }

        if(Util.null2str(tempMblData.get("MOD_DECLARE_NO")).trim().length() != 19) {
        	tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "정정신고번호가 기재되지 않았거나 19자리가 아닙니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
 
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
        }

        if(Util.null2str(tempMblData.get("EDI_MSN")).trim().length() != 4) {
        	tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "MSN번호가 기재되지 않았거나 4자 미만입니다.");
			message = ediCommonService.getErrorCodeName(tempSearchData);
 
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
        }

        if(mblNoStr.length() == 0) {
        	tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "번호를 기재 바랍니다");
			message = ediCommonService.getErrorCodeName(tempSearchData);
 
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
        }

        if(Util.null2str(tempMblData.get("CUSTOMS_CODE")).trim().length() == 0) {
        	tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "신고세관을 확인하기 바랍니다");
			message = ediCommonService.getErrorCodeName(tempSearchData);
 
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
        }

        if(Util.null2str(tempMblData.get("CUSTOMS_DPT_CODE")).trim().length() == 0) {
        	tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "신고세관과를 기재 바랍니다");
			message = ediCommonService.getErrorCodeName(tempSearchData);
 
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
        }

        //이미 전송한건중 해당 MRN,ModifySerialNo 가 같은것이 있다면 오류
//        tempSearchMapData = new HashMap<String, Object>();
        tempSearchMapData.clear(); //초기화 - ditobass!!@
        tempSearchMapData.put("modJobNo", Util.null2str(tempMblData.get("MOD_JOB_NO")).trim());
        
        List<Map<String, Object>> tempModDeclareNoList = manifestMapper.getEdiMfModifyMblModDeclareNoList(tempSearchMapData);
        
        if(tempModDeclareNoList != null && tempModDeclareNoList.size() > 0) {
        	tempSearchData.put("srchCode", "");
			tempSearchData.put("message", "이미 사용한 정정등록 일련번호 입니다");
			message = ediCommonService.getErrorCodeName(tempSearchData);
 
			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
        }
		
		///////////////////////////
		// House B/L Check
		///////////////////////////
        List<Map<String, Object>> tempHblSingleList = null;
        
        String hblNo = null;
        String checkHblNo = null;
        String modJobNo = null;
        String odJobNo = null;
        String modType4thStr = null; // 중복 변수 지정 - ditobass!!@
        
        // H.B/L 건수 체크
 		if(tempHblDataList.size() <= 0) {
 			tempSearchData.put("srchCode", "MF-43");	// 해당 M.B/L에 H.B/L이 한 건도 없습니다.
         	message = ediCommonService.getErrorCodeName(tempSearchData);
  
         	errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
 		}
 		
		for(int i = 0; i < tempHblDataList.size(); ++i) {
			
//          "House 검증"
			hblNo = Util.null2str(tempHblDataList.get(i).get("HBL_NO")).trim();
			checkHblNo = ediCommonService.getHblDeleteChar(Util.null2str(tempHblDataList.get(i).get("HBL_NO")).trim());
			modJobNo = Util.null2str(tempHblDataList.get(i).get("MOD_JOB_NO"));
			odJobNo = Util.null2str(tempHblDataList.get(i).get("OD_JOB_NO"));
            modType4thStr = Util.null2str(tempHblDataList.get(i).get("MOD_TYPE_4TH")).trim(); //중복 변수 지정 - ditobass!!@
			
            //하우스 기본정보 체크    
            if(Util.null2str(tempHblDataList.get(i).get("EDI_HSN")).trim().length() != 4) {
            	tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "HSN번호를 기재되지 않았거나 4자 미만입니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
	 
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
            }

            if(Util.null2str(checkHblNo).trim().length() == 0) {
            	tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "House BL번호를 기재 바랍니다");
				message = ediCommonService.getErrorCodeName(tempSearchData);
	 
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
            } else {
                if(!ediCommonService.getIsAlphaNumericCheck(Util.null2str(checkHblNo).trim())) {
                	tempSearchData.put("srchCode", "");
    				tempSearchData.put("message", "House BL번호에 특수문자가 포함되어있습니다.");
    				message = ediCommonService.getErrorCodeName(tempSearchData);
    	 
    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                }
            }
            
            if(Util.null2str(tempHblDataList.get(i).get("MOD_REASON_NAME")).trim().length() == 0) {
            	tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "의 정정사유를 기재 바랍니다");
				message = ediCommonService.getErrorCodeName(tempSearchData);
	 
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
            }

//          "정정구분 체크"
            if(modType4thStr.length() == 0) {
            	tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "의 정정구분를 기재 바랍니다");
				message = ediCommonService.getErrorCodeName(tempSearchData);
	 
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
            }
            
//          "정정구분이 E일 경우(House 정정)"
            else if ("05".equals(modType4thStr)) {	//2021-09-03 house정정일경우 단일항목만 체크

//                tempSearchMapData = new HashMap<String, Object>();
                tempSearchMapData.clear(); //초기화 - ditobass!!@
                tempSearchData.put("odJobNo", Util.null2str(tempHblDataList.get(i).get("OD_JOB_NO")));
                tempSearchData.put("modJobNo", Util.null2str(tempHblDataList.get(i).get("MOD_JOB_NO")));
                tempSearchData.put("entCode", docSend.get("entCode"));
                tempHblSingleList = manifestMapper.getEdiMfModifyHblSingleList(tempSearchData);

                if(tempHblSingleList != null && tempHblSingleList.size() > 0) {
                	for(int nRowCount = 0; nRowCount < tempHblSingleList.size(); ++nRowCount) {
                		if(Util.null2str(tempHblSingleList.get(nRowCount).get("MOD_ITEM_CODE_HBL")).trim().length() == 0) {
                			tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "Single의 정정구분을 기재 바랍니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                        }

                        if(Util.null2str(tempHblSingleList.get(nRowCount).get("MOD_REASON_NAME")).trim().length() == 0) {
                        	tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "Single의 정정사유을 기재 바랍니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                        }
                        // 2024-05-28 정정전내역과 정정후내역 NULL 상태로 전송 가능
//                      if(Util.null2str(tempHblSingleList.get(nRowCount).get("MOD_DESC_BEFORE")).trim().length() == 0) {
//                			tempSearchData.put("srchCode", "");
//            				tempSearchData.put("message", "Single(" + Util.null2str(tempHblSingleList.get(nRowCount).get("MOD_ITEM_CODE_HBL")) + ")의 정정전 내역을 기재 바랍니다.");
//            				message = ediCommonService.getErrorCodeName(tempSearchData);
//            	 
//            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
//                        }
//                		
//                		if(Util.null2str(tempHblSingleList.get(nRowCount).get("MOD_DESC_AFTER")).trim().length() == 0) {
//                			tempSearchData.put("srchCode", "");
//            				tempSearchData.put("message", "Single(" + Util.null2str(tempHblSingleList.get(nRowCount).get("MOD_ITEM_CODE_HBL")) + ")의 정정후 내역을 기재 바랍니다.");
//            				message = ediCommonService.getErrorCodeName(tempSearchData);
//            	 
//            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
//                      }
                        if(Util.null2str(tempHblSingleList.get(nRowCount).get("MOD_DESC_BEFORE")).trim().length() == 0 &&
                        	Util.null2str(tempHblSingleList.get(nRowCount).get("MOD_DESC_AFTER")).trim().length() == 0) {
                        	
                        	tempSearchData.put("srchCode", "");
                        	tempSearchData.put("message", "Single(" + Util.null2str(tempHblSingleList.get(nRowCount).get("MOD_ITEM_CODE_HBL")) + ")의 정정전 내역 또는 정정후 내역을 기재 바랍니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                        }
                        
                        
                    }
                } else if("AIR".equals(boundStr) && "IMP".equals(boundTypeStr)) {
                	tempSearchData.put("srchCode", "");
    				tempSearchData.put("message", "의 정정구분이 정정이면 Single부분을 기재 바랍니다.");
    				message = ediCommonService.getErrorCodeName(tempSearchData);
    	 
    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                }
            }else if("99".equals(modType4thStr)){
            	//container 부분 체크
                if("SEA".equals(boundStr) && "IMP".equals(boundTypeStr)) {
                	Map <String, Object> containerMap = this.getModifyVerifyInContainer4th(odJobNo, modType4thStr, hblNo, boundStr, errList, docSend, modJobNo);
                	tempHblDataList.get(i).put("containerList", containerMap.get("containerList"));
                	tempHblDataList.get(i).put("containerModList", containerMap.get("containerModList"));
                }
            }

//          "정정구분이 06일 경우(House 추가)"
            else if("06".equals(modType4thStr)) {
                if(Util.null2str(tempHblDataList.get(i).get("BL_TYPE_IERT")).trim().length() == 0) {
                	tempSearchData.put("srchCode", "");
    				tempSearchData.put("message", "의 정정구분이 추가이면 화물구분을 기재 바랍니다.");
    				message = ediCommonService.getErrorCodeName(tempSearchData);
    	 
    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                }

                //Bulk 화물인경우 수량단위는 V로시적한다. 벌크화물인경우는 수량은 없기때문에 수량체크는 하지않는다.
                if("SEA".equals(boundStr) && "IMP".equals(boundTypeStr)) {
                    if(Util.null2int(tempHblDataList.get(i).get("PIECES")) == 0 && !"V".equals(ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(i).get("PIECES")).trim(), 0, 1))) {
                    	tempSearchData.put("srchCode", "");
        				tempSearchData.put("message", "의 정정구분이 추가이면 수량을 기재 바랍니다.");
        				message = ediCommonService.getErrorCodeName(tempSearchData);
        	 
        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    }
                } else {
                    if(Util.null2int(tempHblDataList.get(i).get("PIECES")) == 0) {
                    	tempSearchData.put("srchCode", "");
        				tempSearchData.put("message", "의 정정구분이 추가이면 수량을 기재 바랍니다.");
        				message = ediCommonService.getErrorCodeName(tempSearchData);
        	 
        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    }
                }

                if(new BigDecimal(Util.null2str(tempHblDataList.get(i).get("GWT"), "0")).doubleValue() == 0) {
                	tempSearchData.put("srchCode", "");
    				tempSearchData.put("message", "의 정정구분이 추가이면 중량을 기재 바랍니다.");
    				message = ediCommonService.getErrorCodeName(tempSearchData);
    	 
    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                }

//                if(Util.null2str(tempHblDataList.get(i).get("UNIT_PIECES")).trim().length() == 0) {
//                	tempSearchData.put("srchCode", "");
//    				tempSearchData.put("message", "의 정정구분이 추가이면 수량단위를 기재 바랍니다.");
//    				message = ediCommonService.getErrorCodeName(tempSearchData);
//    	 
//    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
//                } else {
//                    if(Util.null2str(tempHblDataList.get(i).get("UNIT_PIECES")).trim().length() > 2) {
//                    	tempSearchData.put("srchCode", "");
//        				tempSearchData.put("message", "수량단위는 2자 이하입니다.");
//        				message = ediCommonService.getErrorCodeName(tempSearchData);
//        	 
//        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
//                    }
//                }
                
                if(Util.null2str(tempHblDataList.get(i).get("COMMODITY_ITEM_DESC")).trim().length() == 0) {
                	tempSearchData.put("srchCode", "");
    				tempSearchData.put("message", "의 정정구분이 추가이면 품명을 기재 바랍니다.");
    				message = ediCommonService.getErrorCodeName(tempSearchData);
    	 
    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                }
                
                // 4세대 스펙에서 배정코드 항목 삭제
                /*if((Util.null2str(tempHblDataList.get(i).get("EDI_ASSIGNMENT_WH_CODE")).trim().length() > 0) && (!"T".equalsIgnoreCase(Util.null2str(tempHblDataList.get(i).get("EDI_ASSIGNMENT_WH_CODE")).trim()))) {
                    if(ediCommonService.getLocation(Util.null2str(tempHblDataList.get(i).get("EDI_ASSIGNMENT_WH_CODE")).trim(), docSend).trim().length() != 8) {
                    	tempSearchData.put("srchCode", "");
        				tempSearchData.put("message", "의 정정구분이 추가이면 배정장소의 식별자(장치장코드)를 기재 바랍니다.");
        				message = ediCommonService.getErrorCodeName(tempSearchData);
        				
        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    }
                }*/

                if(Util.null2str(tempHblDataList.get(i).get("SHIPPER_NAME")).trim().length() == 0) {
                	tempSearchData.put("srchCode", "");
    				tempSearchData.put("message", "의 정정구분이 추가이면 Shipper 상호를 기재 바랍니다..");
    				message = ediCommonService.getErrorCodeName(tempSearchData);
    	 
    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                }

                if(Util.getKor2ByteLength(Util.null2str(tempHblDataList.get(i).get("SHIPPER_NAME")).trim()) > 100) {
                	tempSearchData.put("srchCode", "");
    				tempSearchData.put("message", "Shipper 상호길이가 100자 이상입니다.");
    				message = ediCommonService.getErrorCodeName(tempSearchData);
    	 
    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                }

                if(Util.null2str(tempHblDataList.get(i).get("CONSIGNEE_NAME")).trim().length() == 0) {
                	tempSearchData.put("srchCode", "");
    				tempSearchData.put("message", "의 정정구분이 추가이면 Consignee 상호를 기재 바랍니다.");
    				message = ediCommonService.getErrorCodeName(tempSearchData);
    	 
    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                }

                if(Util.getKor2ByteLength(Util.null2str(tempHblDataList.get(i).get("CONSIGNEE_NAME")).trim()) > 100) {
                	tempSearchData.put("srchCode", "");
    				tempSearchData.put("message", "Consignee 상호길이가 100자 이상입니다.");
    				message = ediCommonService.getErrorCodeName(tempSearchData);
    	 
    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                }
                
                if("SEA".equals(boundStr) && "IMP".equals(boundTypeStr)) {
                    if(new BigDecimal(Util.null2str(tempHblDataList.get(i).get("CBM"), "0")).doubleValue() == 0) {
                    	tempSearchData.put("srchCode", "");
        				tempSearchData.put("message", "의 정정구분이 추가이면 용적을 기재 바랍니다");
        				message = ediCommonService.getErrorCodeName(tempSearchData);
        	 
        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    }
                    
                    if(Util.null2int(tempHblDataList.get(i).get("CNTR_QTY")) == 0) {
                    	tempSearchData.put("srchCode", "");
        				tempSearchData.put("message", "의 정정구분이 추가이면 컨테이너갯수를 기재 바랍니다.");
        				message = ediCommonService.getErrorCodeName(tempSearchData);
        	 
        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    }
                }


                //컨테이너 추가
                if("SEA".equals(boundStr) && "IMP".equals(boundTypeStr)) {
                	Map <String, Object> containerMap = this.getModifyVerifyInContainer4th(odJobNo, modType4thStr, hblNo, boundStr, errList, docSend, modJobNo);
                	tempHblDataList.get(i).put("containerList", containerMap.get("containerList"));
                	tempHblDataList.get(i).put("containerModList", containerMap.get("containerModList"));
                }
            }

//          2021-09-03 "정정구분이 07일 경우(House 삭제) 체크제거"
		}
		
		// 오류내역이 있을 경우
		if(errList.size() > 0) {
			ediCommonService.setValidateMessage(docSend, tempMblData, errList, result);
			return false;
		}
		
		return true;
	}
	
	/**
     * 컨테이너 오류검증 '''@@@
     * 관세청 4세대 전용
     * @param hblJobNo
     * @param modTypeHbl
     * @param hblNo
     * @param bound
     * @param errList
	 * @param modJobNo 
     * @throws Exception
     */
	public Map<String, Object> getModifyVerifyInContainer4th(String jobNo, String modTypeHbl, String hblNo, String bound, List<Map<String, Object>> errList, Map<String, Object> docSend, String modJobNo) throws Exception {
        
		List<Map<String, Object>> tempHblCntrList = null;
		List<Map<String, Object>> tempHblCntrReasonList = null;
		
		Map<String, Object> tempSearchMapData = new HashMap<String, Object>();
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		
		String strCntrNo = null;
		String modTypeCntrStr = null; //중복 변수 지정 - ditobass!!@
		String message = null;
		
		tempSearchMapData.put("odJobNo", Util.null2str(jobNo).trim());
		tempSearchMapData.put("modType", Util.null2str(modTypeHbl).trim());
		tempSearchMapData.put("modJobNo", Util.null2str(modJobNo).trim());
        tempHblCntrList = manifestMapper.ediManifestContainerList(tempSearchMapData);	//ediMfModifyAirSeaSendDao.getEdiMfModifyHblCtnrList(tempSearchMapData);
        
        if(tempHblCntrList != null && tempHblCntrList.size() > 0) {
            for(int nRowCount = 0; nRowCount < tempHblCntrList.size(); ++nRowCount) {
                strCntrNo  = Util.null2str(tempHblCntrList.get(nRowCount).get("CNTR_NO")).trim();
                modTypeCntrStr = Util.null2str(tempHblCntrList.get(nRowCount).get("MOD_TYPE_CNTR")).trim();
                
                if(strCntrNo.length() == 0) {
                	tempSearchData.put("srchCode", "");
    				tempSearchData.put("message", "Container의 컨테이너번호을 기재 바랍니다.");
    				message = ediCommonService.getErrorCodeName(tempSearchData);
    	 
    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
//                } else if ("IN BULK".equalsIgnoreCase(strCntrNo)) {
//                    if (Util.null2str(tempHblCntrList.get(nRowCount).get("CNTR_SEAL_NO_1")).trim().length() == 0) {
//                    	tempSearchData.put("srchCode", "");
//        				tempSearchData.put("message", "[IN BULK] 건 이라도 Container의 봉인번호는 기재 바랍니다. 봉인번호가 없으면 봉인번호에 NO SEAL 기재");
//        				message = ediCommonService.getErrorCodeName(tempSearchData);
//        	 
//        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
//                    }
                }

//              "House 정정구분이 05:정정일경우 체크"
                if ("99".equals(modTypeHbl)) {
                    if (modTypeCntrStr.length() == 0) {
                    	tempSearchData.put("srchCode", "");
        				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] Container의 정정구분를 기재 바랍니다.");
        				message = ediCommonService.getErrorCodeName(tempSearchData);
        	 
        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    }
                    //Container의 정정구분이 I:B/L추가 일경우 체크
                    else if("I".equals(modTypeCntrStr)) {
                    	tempSearchData.put("srchCode", "");
        				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] 의 정정구분이 C이면 정정구분은 A,U,D,X만 기재 바랍니다.");
        				message = ediCommonService.getErrorCodeName(tempSearchData);
        	 
        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));

                        if(!"".equals(Util.null2str(tempHblCntrList.get(nRowCount).get("UNIT_PIECES")).trim())) {
                            if(Util.null2str(tempHblCntrList.get(nRowCount).get("UNIT_PIECES")).trim().length() == 0) {
                            	tempSearchData.put("srchCode", "");
                				tempSearchData.put("message", "수량단위가 있으면 봉인번호를 가재바랍니다. 봉인번호가 없으면 봉인번호에 NO SEAL 기재");
                				message = ediCommonService.getErrorCodeName(tempSearchData);
                	 
                				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                            }
                        }
                    }
                    //Container의 정정구분이 A:추가일경우 체크
                    else if("A".equals(modTypeCntrStr)) {
                        if(Util.null2str(tempHblCntrList.get(nRowCount).get("CNTR_EDI_CODE")).trim().length() == 0) {
                        	tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] Container의 컨테이너종류를 기재 바랍니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                        }

                        if(Util.null2str(tempHblCntrList.get(nRowCount).get("CNTR_SEAL_NO_1")).trim().length() == 0) {
                        	tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] Container의 봉인번호를 기재 바랍니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                        }

                        // 오류검증 체크항목 누락사항 추가 -- 2008.10.28 이하양
                        if(Util.null2int(tempHblCntrList.get(nRowCount).get("PIECES")) == 0) {
                        	tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] Container의 수량을 기재 바랍니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));                        	
                        }

                        if(Util.null2str(tempHblCntrList.get(nRowCount).get("UNIT_PIECES")).trim().length() == 0) {
                        	tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] Container의 포장단위를 기재 바랍니다");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));                        	
                        }
                    }
                    //Container의 정정구분이 U:정정 일경우 체크
                    else if("U".equals(modTypeCntrStr)) {
                    	//2021-09-03 주석
//                        if(Util.null2str(tempHblCntrList.get(nRowCount).get("CNTR_EDI_CODE")).trim().length() != 0) {
//                        	tempSearchData.put("srchCode", "");
//            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] Container의 컨테이너종류를 기재하지 않습니다.");
//            				message = ediCommonService.getErrorCodeName(tempSearchData);
//            	 
//            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));                        	
//                        }
//
//                        if(Util.null2str(tempHblCntrList.get(nRowCount).get("CNTR_SEAL_NO_1")).trim().length() != 0) {
//                        	tempSearchData.put("srchCode", "");
//            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] Container의 봉인번호1를  기재하지 않습니다.");
//            				message = ediCommonService.getErrorCodeName(tempSearchData);
//            	 
//            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));                        	
//                        }
//
//                        if (Util.null2str(tempHblCntrList.get(nRowCount).get("CNTR_SEAL_NO_2")).trim().length() != 0) {
//                        	tempSearchData.put("srchCode", "");
//            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] Container의 봉인번호2를  기재하지 않습니다.");
//            				message = ediCommonService.getErrorCodeName(tempSearchData);
//            	 
//            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));                        	
//                        }
//
//                        // 오류검증 체크항목 누락사항 추가 -- 2008.10.28 이하양
//                        if(Util.null2str(tempHblCntrList.get(nRowCount).get("CNTR_SEAL_NO_3")).trim().length() != 0) {
//                        	tempSearchData.put("srchCode", "");
//            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] Container의 봉인번호3를  기재하지 않습니다.");
//            				message = ediCommonService.getErrorCodeName(tempSearchData);
//            	 
//            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));                        	
//                        }
//
//                        if(Util.null2int(tempHblCntrList.get(nRowCount).get("PIECES")) != 0) {
//                        	tempSearchData.put("srchCode", "");
//            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] Container의 수량을 기재하지 않습니다.");
//            				message = ediCommonService.getErrorCodeName(tempSearchData);
//            	 
//            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));                        	
//                        }
//                        
//                        if(Util.null2str(tempHblCntrList.get(nRowCount).get("UNIT_PIECES")).trim().length() != 0) {
//                        	tempSearchData.put("srchCode", "");
//            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] Container의 포장단위를 기재하지 않습니다.");
//            				message = ediCommonService.getErrorCodeName(tempSearchData);
//            	 
//            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));                        	
//                        }

                    	tempSearchData.put("modJobNo", Util.null2str(tempHblCntrList.get(nRowCount).get("MOD_JOB_NO")).trim());
                    	tempSearchData.put("cntrNo", Util.null2str(tempHblCntrList.get(nRowCount).get("CNTR_NO")).trim());
                    	tempSearchData.put("seqNoId", Util.null2str(tempHblCntrList.get(nRowCount).get("SEQ_NO_ID")).trim());
                        tempSearchData.put("entCode", docSend.get("entCode"));
                        tempHblCntrReasonList = manifestMapper.getEdiMfModifyHblCtnrReasonList(tempSearchData);
                        
                        if(tempHblCntrReasonList.size() > 0){
                        	for (int nSingleRow = 0; nSingleRow < tempHblCntrReasonList.size(); ++nSingleRow) {
                        		if(Util.null2str(tempHblCntrReasonList.get(nSingleRow).get("MOD_ITEM_CODE_CNTR")).trim().length() == 0) {
                        			tempSearchData.put("srchCode", "");
                        			tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] Container의 정정구분이 [U.정정]이면 Container Single 정정항목을 확인 바랍니다.");
                        			message = ediCommonService.getErrorCodeName(tempSearchData);
                        			
                        			errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));                            	
                        		}
                        		
                        		if(Util.null2str(tempHblCntrReasonList.get(nSingleRow).get("MOD_DESC_BEFORE")).trim().length() == 0) {
                        			tempSearchData.put("srchCode", "");
                        			tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] Container의 정정구분이 [U.정정]이면 Container Single(" + Util.null2str(tempHblCntrReasonList.get(nSingleRow).get("MOD_ITEM_CODE_CNTR")) + ") 정정전 내용을 확인 바랍니다.");
                        			message = ediCommonService.getErrorCodeName(tempSearchData);
                        			
                        			errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));                            	
                        		}
                        		
                        		if(Util.null2str(tempHblCntrReasonList.get(nSingleRow).get("MOD_DESC_AFTER")).trim().length() == 0) {
                        			tempSearchData.put("srchCode", "");
                        			tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] Container의 정정구분이 [U.정정]이면 Container Single(" + Util.null2str(tempHblCntrReasonList.get(nSingleRow).get("MOD_ITEM_CODE_CNTR")) + ") 정정후 내용을 확인 바랍니다.");
                        			message = ediCommonService.getErrorCodeName(tempSearchData);
                        			
                        			errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));                            	
                        		}
                        	}
                        }else{
                        	tempSearchData.put("srchCode", "");
                        	tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] Container의 정정구분이 [U.정정]이면 Container Single 정정항목을 기재 바랍니다.");
                        	message = ediCommonService.getErrorCodeName(tempSearchData);
                        	
                        	errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                        }
                    }
                }

//              "House 정정구분이 06:추가일경우 체크"
                else if("06".equalsIgnoreCase(modTypeHbl)) {
                    if(modTypeCntrStr.length() == 0) {
                    	tempSearchData.put("srchCode", "");
        				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] Container의 정정구분를 기재 바랍니다.");
        				message = ediCommonService.getErrorCodeName(tempSearchData);
        	 
        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    }
                    ////Container의 정정구분이 I:B/L추가 일경우 체크
                    else if("I".equals(modTypeCntrStr)) {
                        if(Util.null2int(tempHblCntrList.get(nRowCount).get("PIECES")) > 0 
                        		&& "".equals(Util.null2str(tempHblCntrList.get(nRowCount).get("UNIT_PIECES")).trim())) {
                        	tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] Container의 정정구분의 수량단위를 기재바랍니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                        } else {
                            if(Util.null2str(tempHblCntrList.get(nRowCount).get("UNIT_PIECES")).trim().length() == 0) {
                            	tempSearchData.put("srchCode", "");
                				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] 정정구분이 F이면 Container의 수량단위를 기재 바랍니다.");
                				message = ediCommonService.getErrorCodeName(tempSearchData);
                	 
                				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                            }
                        }
                    }
                    //Container의 정정구분이 A:추가 일경우 체크
                    else if("A".equals(modTypeCntrStr)) {
                    	tempSearchData.put("srchCode", "");
        				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] 정정구분이 F이면 Container의 정정구분은 I만 기재 바랍니다.");
        				message = ediCommonService.getErrorCodeName(tempSearchData);
        	 
        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    }
                    //Container의 정정구분이 U:정정 일경우 체크
                    else if("U".equals(modTypeCntrStr)) {
                    	tempSearchData.put("srchCode", "");
        				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] 정정구분이 F이면 Container의 정정구분은 I만 기재 바랍니다.");
        				message = ediCommonService.getErrorCodeName(tempSearchData);
        	 
        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    }
                    //Container의 정정구분이 D:삭제 일경우 체크
                    else if("D".equals(modTypeCntrStr)) {
                    	tempSearchData.put("srchCode", "");
        				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] 정정구분이 F이면 Container의 정정구분은 I만 기재 바랍니다.");
        				message = ediCommonService.getErrorCodeName(tempSearchData);
        	 
        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    }
                }
            }	// end of for-loop.
        }	// end of if.
        Map<String, Object> returnMap = new HashMap<String, Object>();
        returnMap.put("containerList", tempHblCntrList);
        returnMap.put("containerModList", tempHblCntrReasonList);
        return returnMap;
	}
	
	/**
	 * 파일생성 '''@@@
	 * 관세청 4세대 전용
	 * @param docSend
	 * @param gridRow
	 * @param resJsonObject
	 * @throws java.lang.Exception
	 */
	public String[] getSendDataFileCreate4th(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempHblDataList, Map<String, Object> fwSendAccInfo
			, Map<String, Object> ediHostSendInfo) throws java.lang.Exception {
		
		List<Map<String, Object>> tempHblCntrList = null;
		List<Map<String, Object>> tempHblCntrReasonList = null;
		List<Map<String, Object>> tempHblSingleList = null;
		
		Map<String, Object> tempSearchData = new HashMap<>();
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(docSend, "gmtTime", false, null, null, null, null);
		
//		StringBuffer retWriteData = new StringBuffer();	// 파일 용량이 클경우 내용 저장하면 안됨.
		StringBuffer writeData = null;
		String retData[] = new String[6];
		String fileName = new StringBuffer(Util.null2str(docSend.get("registerUserId"))).append(".H").append(Util.null2str(tempMblData.get("MBL_NO")).trim()).append(".txt").toString();	// 전송 파일명
        String ediVender = Util.null2str(docSend.get("ediVender"));
		String dirPath[] = ediCommonService.getSendFilePath(ediVender, "edi/manifestMod", docSend);	// 로컬폴더 경로 가져오기
		
		/*문서쓰기방식 변경하여 추가*/
		String dirPath0 = dirPath[0];
		
		String strFWDCode			= Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).trim();
		String strDeclarerDate		= Util.null2str(tempMblData.get("MOD_DECLARE_DATE")).replace(" ","").replace("-","").replace(":", "");
        String strCustoms			= Util.null2str(tempMblData.get("CUSTOMS_CODE")).trim();
        String modReqNo = Util.null2str(tempMblData.get("MOD_DECLARE_NO"));
        String strCustomsDepart	= Util.null2str(tempMblData.get("CUSTOMS_DPT_CODE")).trim();
        String strMSN				= Util.null2str(tempMblData.get("EDI_MSN")).trim();
        
		// 파일 내용 쓰기
		try {
			writeData = new StringBuffer();
			
			

            int nSegmentCount = 0;
            
            //전자문서 참조번호
//          "AI_SAVE_SAM_UNH"
            String strFwd_SenderID = Util.null2str(ediHostSendInfo.get("IF_SENDER_ID")).trim();
            String strAirSeaReceiveId = null;
            
            String boundStr = Util.null2str(docSend.get("bound")).trim(); //중복변수 지정 - ditobass!!@
            String boundTypeStr = Util.null2str(docSend.get("boundType")).trim(); //중복변수 지정 - ditobass!!@
            
            // 루프 밖 반복 변수 지정 - ditobass!!@
            String modReasonNameStr = null;
            String commodityItemDescStr = null, commodityItemDescStr50to100 = null, commodityItemDescStr100to150 = null, commodityItemDescStr150to200 = null;
            String shipperCountryCodeStr = null;
            String shipperPlaceCodeStr = null;
            String origPortCodeStr = null;
            String shipperAddressStr = null, shipperAddressStr0to35 = null, shipperAddressStr35to70 = null, shipperAddressStr70to105 = null, shipperAddressStr105to140 = null, shipperAddressStr140to150 = null;
            String shipperNameStr = null, shipperNameStr0to35 = null, shipperNameStr35to70 = null, shipperNameStr70to100 = null;
            String consigneeNameStr = null, consigneeNameStr0to35 = null, consigneeNameStr35to70 = null, consigneeNameStr70to100 = null;
            String consigneeAddressStr = null, consigneeAddressStr0to35 = null, consigneeAddressStr35to70 = null, consigneeAddressStr70to105 = null, consigneeAddressStr105to140 = null, consigneeAddressStr140to150 = null;
            String consigneeBusinessIdStr = null;
            String consigneeCountryCodeStr = null;
            String consigneePlaceCodeStr = null;
            String destPortCodeStr = null;
            String cntrSealNo1Str = null, cntrSealNo1Str0to10 = null, cntrSealNo1Str10to20 = null;
            String cntrSealNo2Str = null, cntrSealNo2Str0to10 = null, cntrSealNo2Str10to20 = null;
            String cntrSealNo3Str = null, cntrSealNo3Str0to10 = null, cntrSealNo3Str10to20 = null;
            String modDescBefore = null, modDescBefore0to50 = null, modDescBefore50to100 = null, modDescBefore100to150 = null, modDescBefore150to200 = null;
            String modDescAfter = null, modDescAfter0to50 = null, modDescAfter50to100 = null, modDescAfter100to150 = null, modDescAfter150to200 = null;
            boolean shtFinishFlag;
            
            //사전적하목록의 경우 수신식별자가 변경된다. 항공수입 : KCS0004AX, 해운수입 :  KCS0004SX
            if("AIR".equals(boundStr)) {
            	if("IMP".equals(boundTypeStr)) {
            		//strAirSeaReceiveId = Util.null2str(tempMblData.get("AIR_IMPORT_RECEIVE_ID")).trim();
            		// KT-NET 식별자
            		strAirSeaReceiveId = "KCS4G002";
            		// 그 외 식별자
            		// strRec = "KCS0004XML";
            	} else {
            		strAirSeaReceiveId = Util.null2str(tempMblData.get("AIR_EXPORT_RECEIVE_ID")).trim();
            	}
            } else if ("SEA".equals(boundStr)) {
            	if("KTNET".equals(ediVender)) {
            		if("IMP".equals(boundTypeStr)) {
                		//strAirSeaReceiveId = Util.null2str(tempMblData.get("SEA_IMPORT_RECEIVE_ID")).trim();
                		// KT-NET 식별자
                		strAirSeaReceiveId = "KCS4G002";
                		// 그 외 식별자
                		// strRec = "KCS0004XML";
                	} else {
                		strAirSeaReceiveId = Util.null2str(tempMblData.get("SEA_EXPORT_RECEIVE_ID")).trim();
                	}
            	}else{//KLNET
            		if("IMP".equals(boundTypeStr)) {
                		strAirSeaReceiveId = "KCS0004XML";
                	} else {
                		strAirSeaReceiveId = Util.null2str(tempMblData.get("SEA_EXPORT_RECEIVE_ID")).trim();
                	}
            	}
            }
            
            // 항공수입 일경우 - 항공사 식별자 세팅
            //2021-09-07  항공사정정 개발 보류
//            if("AIR".equals(boundStr) && "IMP".equals(boundTypeStr)) {
//        		if("Y".equals(Util.null2str(tempMblData.get("EDI_SEND_AIRLINE_YN")).trim())) {
//        			strAirSeaReceiveId = Util.null2str(tempMblData.get("AIR_LINE_ID")).trim();
//        		}
//            }
            
           //테스트 끝나고 삭제해야함 2016-02-16 삭제요망
//            if("IMFCK".equals(Util.null2str(docSend.get("entCode")))){
//            	strAirSeaReceiveId = "KCS4G002";
//            }
            
            // UNB
            if("SEA".equals(boundStr) && "KLNET".equals(ediVender)) {
            	writeData.append("UNB+KECA:4+").append(strFwd_SenderID).append(":KL+").append(Util.null2str(strAirSeaReceiveId).trim()).append(":KC+");
            }else{
            	writeData.append("UNB+KECA:4+").append(strFwd_SenderID).append(":57+").append(Util.null2str(strAirSeaReceiveId).trim()).append(":57+");
            }
            writeData.append(DateTime.getDate("yyyyMMdd:HHmm")).append("+1");
            writeData.append("'");
            nSegmentCount++;
            
            // UNH
            writeData.append("UNH+1+IMFMOD:S:93A:KE");
            writeData.append("'");
            nSegmentCount++;

            //정정신청 제출번호 | 신청인구분구호 , 해상/항공 구분
//          "AI_SAVE_SAM_BGM | AI_SAVE_SAM_GIS"
            // 정정신청 제출번호(MRN + 포워더부호 + 일련번호)
            //BGM+5LI+14KE0E6BGI2PANA0001'
            writeData.append("BGM+5LI+").append(modReqNo);
            writeData.append("'");
            nSegmentCount++;
            
            //신청인구분구호
            //AI_SAVE_SAM_GIS
            //GIS+F:5AG:KCS'
            //'''@@@ 4세대 신청인구분부호 선사:01 항공사:02 운송주선업자:03 으로 변경
            writeData.append("GIS+03:5AG:KCS");
            writeData.append("'");
            nSegmentCount++;
            
            //해상/항공 구분 
            //GIS+40:146:KCS'
            writeData.append("GIS+");
            if("AIR".equals(boundStr)) {
            	writeData.append("40");	// 40:항공
            } else {
            	writeData.append("10");	// 10:해상
            }
            writeData.append(":146:KCS");
            writeData.append("'");
            nSegmentCount++;

            //신고/신청일시 | 신고세관/세관과 | 신청인   
//          "AI_SAVE_SAM_DTM | AI_SAVE_SAM_LOC | AI_SAVE_SAM_NAD

            //신고/신청일시  
            //AI_SAVE_SAM_DTM
            //DTM+141:20141118110400:204'
            writeData.append("DTM+141:").append(strDeclarerDate).append(":204");
            writeData.append("'");
            nSegmentCount++;

            //신고세관/세관과  
            //AI_SAVE_SAM_LOC
            //LOC+41+040:113:KCS+10:5AB:KCS'
            writeData.append("LOC+41+").append(strCustoms).append(":113:KCS+").append(strCustomsDepart).append(":5AB:KCS");
            writeData.append("'");
            nSegmentCount++;

            String entBusinessNameStr = Util.null2str(fwSendAccInfo.get("ENT_BUSINESS_NAME")).trim();
            
            //신청인  
            //AI_SAVE_SAM_NAD
            //NAD+MS++(주) 파나라인'
            //String entBusinessNameStr = Util.null2str(fwSendAccInfo.get("ENT_BUSINESS_NAME")).trim();   //중복 변수 지정 - ditobass!!@
            if(entBusinessNameStr.length() <= 35){
            	writeData.append("NAD+MS++").append(ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(entBusinessNameStr, 0, 35)));
            	writeData.append("'");
            	nSegmentCount++;
            }else if(entBusinessNameStr.length() <= 70){
            	writeData.append("NAD+MS++");
    			writeData.append(ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(entBusinessNameStr, 0, 35)));
    			writeData.append(":").append(ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(entBusinessNameStr, 35, 70)));
            	writeData.append("'");
            	nSegmentCount++;
            }else{
            	writeData.append("NAD+MS++");
				writeData.append(ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(entBusinessNameStr, 0, 35)));
				writeData.append(":").append(ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(entBusinessNameStr, 35, 70)));
				writeData.append(":").append(ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(entBusinessNameStr, 70, 100)));
				writeData.append("'");
            	nSegmentCount++;
            }
            
//        	"하우스 갯수만큼 실행"
            //AI_SAVE_SAM_DMS"
//            int nIMDCNT = 0; //사용 안함 주석 - ditobass!!@
//            int nLINCNT = 0; //사용 안함 주석 - ditobass!!@
            String modType4th = null;
            String modTypeCntr = null;
            StringBuffer correctionContents = new StringBuffer();
            for(int nRowIndex = 0; nRowIndex < tempHblDataList.size(); nRowIndex++) {
            	correctionContents.setLength(0);
            	modType4th = Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_TYPE_4TH")).trim();
//                nIMDCNT++; //사용 안함 주석 - ditobass!!@
                
            	String oldCorrectionContents = Util.null2str(manifestMapper.selectOrderCorrectionContents(tempHblDataList.get(nRowIndex)));
            	if(!oldCorrectionContents.equals("")){
            		correctionContents.append(oldCorrectionContents + "|");
            	}
            	
            	if(modType4th.equals("05")){
            		correctionContents.append("HBL정정:" + Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_REASON_NAME")) + "(");
            	}else if(modType4th.equals("06")){
            		correctionContents.append("HBL추가:" + Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_REASON_NAME")));
            	}else if(modType4th.equals("07")){
            		correctionContents.append("HBL삭제:" + Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_REASON_NAME")));
            	}else if(modType4th.equals("99")){
            		correctionContents.append("컨테이너:" + Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_REASON_NAME")) + "(");
            	}
            	
                //적하목록구분
                //DMS++85'
                writeData.append("DMS++85");
                writeData.append("'");
                nSegmentCount++;
                
                //하우스 정정신청구분
//              "AI_SAVE_SAM_DMS_GIS"

                //신청구분  
                //AI_SAVE_SAM_DMS_GIS
                //GIS+E:5AI:KCS'
                //'''@@@ 4세대 신청구분부호
                // 01: 운항정보정정, 02:M-B/L 정정, 03:M-B/L 추가, 04:M-B/L 삭제, 05:H-B/L 정정, 06:H-B/L 추가, 07:H-B/L 삭제 
                writeData.append("GIS+").append(modType4th.equals("99") ? "05" : modType4th).append(":5AI:KCS");
                writeData.append("'");
                nSegmentCount++;

                // 정정사유 :(01: 운항정보정정, 02:M-B/L 정정, 03:M-B/L 추가, 04:M-B/L 삭제, 05:H-B/L 정정, 06:H-B/L 추가, 07:H-B/L 삭제)
                //FTX+ACD+++기재오류'
                //'''@@@ 4세대 정정사유 100자리
                modReasonNameStr = Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_REASON_NAME")).trim(); //반복 변수 지정 - ditobass!!@
                if(modReasonNameStr.length() <= 50){
            		writeData.append("FTX+ACD+++");
        			writeData.append(ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(modReasonNameStr, 0, 50)));
            		writeData.append("'");
            		nSegmentCount++;
            	}else{
            		writeData.append("FTX+ACD+++"); 
            		writeData.append( ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(modReasonNameStr, 0, 50)));
            		writeData.append(":").append(ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(modReasonNameStr, 50, 100)));
            		writeData.append("'");
            		nSegmentCount++;
            	}
                
                //HOUSE B/L 정정사항   
//              "AI_SAVE_SAM_DMS_GIS_IMD"
                //IMD+++:::0010:001'
                writeData.append("IMD+++:::").append(strMSN).append(":").append(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_HSN")).trim());
                writeData.append("'");
                nSegmentCount++;

//              "하우스 정정 정보"
                //작업구분(E:정정, F:추가, G:삭제)
                //'''@@@ 4세대 (05:정정, 06:추가, 07:삭제)
                if("06".equals(modType4th)) {
                    //Master B/L 번호
                    //RFF+BM:MBLNO'
                	//'''@@@ 4세대 B/L 번호 20자리로 변경
                    writeData.append("RFF+BM:");
                    writeData.append(Util.getFileWriteDataWithRtrim(Util.null2str(tempMblData.get("MBL_NO")).trim(), "String", "20"));
                    writeData.append("'");
                    nSegmentCount++;
                    
                    //House B/L 번호
                    //RFF+BH:HBLNO'
                    //'''@@@ 4세대 B/L 번호 20자리로 변경
                    writeData.append("RFF+BH:");
                    writeData.append(Util.getFileWriteDataWithRtrim(ediCommonService.getHblDeleteChar(Util.null2str(tempHblDataList.get(nRowIndex).get("HBL_NO")).trim()).trim(), "String", "20"));
                    writeData.append("'");
                    nSegmentCount++;
                    
                    /** 하기물품구분코드 */
                    // 해상일 경우 해당 항목이 전송되지 않게 수정 - 2024.06.03 --> KTNET만 하기물품구분코드 전송 - 2024.06.11
                    if("KTNET".equals(ediVender)) {
                    	writeData.append("GIS+");
                    	writeData.append(Util.getFileWriteDataWithRtrim(Util.null2str(tempHblDataList.get(nRowIndex).get("DISC_ITEM_CODE")).trim(), "String", "2")).append(":122:KCS");
                    	writeData.append("'");
                    	nSegmentCount++;
                    }
                    
                    //화물구분
                    //GIS+I:105:KCS'
                    writeData.append("GIS+").append(Util.null2str(tempHblDataList.get(nRowIndex).get("BL_TYPE_IERT")).trim()).append(":105:KCS");
                    writeData.append("'");
                    nSegmentCount++;
                    
                    /**
                     * TO DO 
                     * 17.화물속성
                     * (4세대)화물속성부호를 기재
                     * - 수입화물(I) : 국내 개항간 외항선으로 운송하기 위한 화물 : 1
                     * - 환적화물(T) :
                     *  sea & air 환적화물 : 1
                     * 	수출화물로서 부득이한 사유로 국내 개항에 일시양륙하는 화물 : 2
                     * 	특송물품일경우 : 3
                     *  화물속성은 최초 입항지 기준으로 작성
                     *  
                     *  필요시 컬럼 확인하여 추가하여야함 아래 uFMS 코드
                     */
                    /* 화물속성
	                if (strBound == "OI")
	                {
	                    if (HouseDS.Tables[0].Rows[nRowIndex]["CargoProperty"].ToString().Trim() == "1" || HouseDS.Tables[0].Rows[nRowIndex]["CargoProperty"].ToString().Trim() == "2" || HouseDS.Tables[0].Rows[nRowIndex]["CargoProperty"].ToString().Trim() == "3")
	                    {
	                        strPrint += "GIS+" + HouseDS.Tables[0].Rows[nRowIndex]["CargoProperty"].ToString().Trim() + ":117:KCS'"; nSegmentCount++;
	                    }
	                }
	                else if (strBound == "AI")
	                {
	                    if (HouseDS.Tables[0].Rows[nRowIndex]["EMS_YN"].ToString().Trim() == "Y")
	                    {
	                        strPrint += "GIS+3:117:KCS'"; nSegmentCount++;
	                    }
	                }
	                화물속성 */
                    
                    /* 전자상거래물품 여부 */
                    if("IMP".equals(boundTypeStr)) {
                    	if("Y".equals(Util.null2str(tempHblDataList.get(nRowIndex).get("ECM_YN")).trim())){
                    		writeData.append("GIS+").append("Y").append(":71:KCS");
                    		writeData.append("'");
                    		nSegmentCount++;
                    	}
                    }
                
                    /** 하선하기보세구역부호 */
                    // 해상일 경우 해당 항목이 전송되지 않게 수정 - 2024.06.04 --> KTNET만 하선하기보세구역부호 전송 - 2024.06.11
                    if("KTNET".equals(ediVender) && Util.null2str(tempHblDataList.get(nRowIndex).get("DISC_WH_CODE_ETC1")).trim().length() > 0) {
                    	writeData.append("LOC+104+");
                    	writeData.append(Util.getFileWriteDataWithRtrim(Util.null2str(tempHblDataList.get(nRowIndex).get("DISC_WH_CODE_ETC1")).trim(), "String", "8"));
                    	writeData.append("'");
                    	nSegmentCount++;
                    }
                    
                    /** 배정코드 2024-05-28 추가 */
                    // KTNET만 배정코드 전송 - 2024.06.04
                    if("KTNET".equals(ediVender) && ediCommonService.getLocation(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_ASSIGNMENT_WH_CODE")).trim(), docSend).trim().length() > 0) {
                    	writeData.append("LOC+100+");
                    	writeData.append(Util.getFileWriteData(ediCommonService.getLocation(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_ASSIGNMENT_WH_CODE")).trim(), docSend).trim(), "String", "8"));
                    	writeData.append("'");
                    	nSegmentCount++;
                    }
                                        
                    // 장치예정장소 G-ONE -> 배정장소
                    if("SEA".equals(boundStr) && "IMP".equals(boundTypeStr)) 
                    {
                    	/*
                    	if (get_Location(HouseDS.Tables[0].Rows[nRowIndex]["AssignmentWH"].ToString().Trim()).Length > 0 && HouseDS.Tables[0].Rows[nRowIndex]["AssignmentWH"].ToString().Trim() != "T")
                        {
                            strPrint += "LOC+14+" + get_Location(HouseDS.Tables[0].Rows[nRowIndex]["AssignmentWH"].ToString().Trim()) + ":129:KCS'"; nSegmentCount++;
                        }
                    	 */
                        	if(ediCommonService.getLocation(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_ASSIGNMENT_WH_CODE")).trim(), docSend).trim().length() > 0
                        			/*&& !"T".equals(Util.null2str(tempHblDataList.get(nRowIndex).get("BL_TYPE_IERT")).trim())*/
                        			) {
                        		//LOC+14+배정창고코드:129:KCS'
                        		writeData.append("LOC+14+");
                        		writeData.append(Util.getFileWriteData(ediCommonService.getLocation(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_ASSIGNMENT_WH_CODE")).trim(), docSend).trim(), "String", "8"));
                        		writeData.append(":129:KCS");
        	                    writeData.append("'"); 
        	                    nSegmentCount++;
                        	}
                    }
                    
                    // Bill of Destination
                    if(Util.null2str(tempHblDataList.get(nRowIndex).get("DEST_CODE")).trim().length() > 0) 
                    {
                    	//LOC+20+도착지코드'
                    	writeData.append("LOC+20+");
                    	writeData.append(Util.getFileWriteDataWithRtrim(Util.null2str(tempHblDataList.get(nRowIndex).get("DEST_CODE")).trim(), "String", "5"));
                    	writeData.append("'"); 
                    	nSegmentCount++;
                    }
                    
                    // 포장개수
                    if(Util.null2int(tempHblDataList.get(nRowIndex).get("PIECES")) > 0) 
                    {
                    	writeData.append("PAC+++");
//                    	writeData.append(Util.null2str(tempHblDataList.get(nRowIndex).get("UNIT_PIECES")).trim()).append(":::").append(Util.null2int(tempHblDataList.get(nRowIndex).get("PIECES")));
                    	if("AIR".equals(boundStr)){
                    		writeData.append("CT").append(":::").append(Util.null2int(tempHblDataList.get(nRowIndex).get("PIECES")));
                    	}else{
                    		writeData.append("GT").append(":::").append(Util.null2int(tempHblDataList.get(nRowIndex).get("PIECES")));
                    	}
                    	writeData.append("'");
                    	nSegmentCount++;
                    }
                    
                    // 품명
                    //KTNET은 대표품목이 아닌 세부품목을 송신한다. 2014.11.25
                    commodityItemDescStr = Util.null2str(tempHblDataList.get(nRowIndex).get("COMMODITY_ITEM_DESC")).trim(); // 중복 변수 지정 - ditobass!!@
                    if(commodityItemDescStr.length() > 0) 
                    {
                        //FTX+AAA+++품명50자리:품명50자리:품명50자리:품명50자리
                        writeData.append("FTX+AAA+++");
                    	writeData.append(ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(commodityItemDescStr, 0, 50)));

                        commodityItemDescStr50to100 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(commodityItemDescStr, 50, 100)); //중복 변수 지정 - ditobass!!@
                        commodityItemDescStr100to150 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(commodityItemDescStr, 100, 150)); //중복 변수 지정 - ditobass!!@
                        commodityItemDescStr150to200 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(commodityItemDescStr, 150, 200)); //중복 변수 지정 - ditobass!!@
                        
                    	if(!"".equals(commodityItemDescStr50to100)) { writeData.append(":").append(commodityItemDescStr50to100); }
                    	if(!"".equals(commodityItemDescStr100to150)){ writeData.append(":").append(commodityItemDescStr100to150); }
                    	if(!"".equals(commodityItemDescStr150to200)){ writeData.append(":").append(commodityItemDescStr150to200); }
                        writeData.append("'"); 
                        nSegmentCount++;
                    }
                    
                    if("AIR".equals(boundStr) && "IMP".equals(boundTypeStr)) 
                    {
                    	//HSCode >> 22.05.06 hscode삭제 요청 : 장순문
//                        if(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_HS_CODE")).trim().length() > 0) 
//                        {
//                            //FTX+6TH+++HS코드'
//                            writeData.append("FTX+6TH+++");
//                            writeData.append("");
//                            writeData.append("'");
//                            nSegmentCount++;
//                        }
                        
                        //UCR
//                        if(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_UCR_NO")).trim().length() > 0) 
//                        {
//                            //FTX+CUS+++:UCR번호'
//                            writeData.append("FTX+CUS+++:");
//                            writeData.append(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_UCR_NO")).trim());
//                            writeData.append("'");
//                            nSegmentCount++;
//                        }
                    }
                    
                    // 특수화물코드
                    if("SEA".equals(boundStr) && "IMP".equals(boundTypeStr)) 
                    {
                         if(!"".equals(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_SPECIAL_CODE")).trim()))
                         {
                        	writeData.append("DGS++++++++++");
                         	writeData.append(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_SPECIAL_CODE")).trim());
                         	
//                         	if("".equals(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_SPECIAL_CODE_A")).trim()))
//                         	{
                         		writeData.append(":").append(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_SPECIAL_CODE_A")).trim());
//                         	}
                         	
//                         	if("".equals(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_SPECIAL_CODE_B")).trim()))
//                         	{
                         		writeData.append(":").append(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_SPECIAL_CODE_B")).trim());
//                         	}
                         	writeData.append("'");
                         	nSegmentCount++;
                         }
                         else
                         {
                        	writeData.append("DGS++++++++++");
                         	writeData.append("NO");
                         	writeData.append("'");
                            nSegmentCount++;
                         }
                    }
                    
                    // 총중량
                    //'''@@@ 4세대 소수점 3자리 , 06. 추가일경우에만 추가
                    if(new BigDecimal(Util.null2str(tempHblDataList.get(nRowIndex).get("GWT"), "0")).doubleValue() > 0) {
                        //MEA+WT++KG:중량'     (항공은소숫정1자리, 해운은소숫점2자리)
                        writeData.append("MEA+WT++KG:");
                        if("SEA".equals(boundStr) && "IMP".equals(boundTypeStr)) {	//해운수입일 때 데이터 소수점3번째자리까지 그대로 표현
                        	writeData.append(ediCommonService.getNumberFormat(Util.null2str(tempHblDataList.get(nRowIndex).get("GWT"), "0"), "##0.000", 3));
                        }else {
                        	writeData.append(ediCommonService.getNumberFormat(Util.null2str(tempHblDataList.get(nRowIndex).get("GWT"), "0"), "##0.000", 3));
                        }
                        writeData.append("'"); nSegmentCount++;
                    }
                    
                    // 총용적
                    if("SEA".equals(boundStr) && "IMP".equals(boundTypeStr)) 
                    {
                        //'''@@@ 4세대 소수점 3자리, 06.추가일경우 추가
                        if(new BigDecimal(Util.null2str(tempHblDataList.get(nRowIndex).get("CBM"), "0")).doubleValue() > 0) {
                        	//MEA+VOL++MTQ:CBM'
                        	writeData.append("MEA+VOL++MTQ:");
                        	writeData.append(ediCommonService.getNumberFormat(Util.null2str(tempHblDataList.get(nRowIndex).get("CBM"), "0"), "##0.000", 3));	//해운수입일 때 데이터 소수점3번째자리까지 그대로 표현
                        	writeData.append("'"); nSegmentCount++;
                        }
                    }
                    
                    // "Shipper"
                    shipperNameStr = Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_NAME")).trim(); //중복 변수 지정 - ditobass!!@

                    if(shipperNameStr.length() > 0) 
                    {
                        shipperAddressStr = Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_ADDRESS")).trim(); //중복 변수 지정 - ditobass!!@
                        shipperCountryCodeStr = Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_COUNTRY_CODE")).trim(); //중복 변수 지정 - ditobass!!@
                        shipperPlaceCodeStr = Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_PLACE_CODE")).trim(); //중복 변수 지정 - ditobass!!@
                        origPortCodeStr = Util.null2str(tempHblDataList.get(nRowIndex).get("ORIG_CODE")).trim();
                        shipperAddressStr0to35 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(shipperAddressStr, 0, 35)); //중복 변수 지정 - ditobass!!@
                        shipperAddressStr35to70 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(shipperAddressStr, 35, 70)); //중복 변수 지정 - ditobass!!@
                        shipperAddressStr70to105 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(shipperAddressStr, 70, 105)); //중복 변수 지정 - ditobass!!@
                        shipperAddressStr105to140 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(shipperAddressStr, 105, 140)); //중복 변수 지정 - ditobass!!@
                        shipperAddressStr140to150 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(shipperAddressStr, 140, 150)); //중복 변수 지정 - ditobass!!@
                        shipperNameStr0to35 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(shipperNameStr, 0, 35)); //중복 변수 지정 - ditobass!!@
                        shipperNameStr35to70 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(shipperNameStr, 35, 70)); //중복 변수 지정 - ditobass!!@
                        shipperNameStr70to100 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(shipperNameStr, 70, 100)); //중복 변수 지정 - ditobass!!@
                        
                        //NAD+CZ++화주명:주소35자리:주소35자리:주소35자리'
                    	//'''@@@ 4세대 주소먼저 상호 도시코드 국가코드순으로 되어있음
                    	writeData.append("NAD+CZ++");

                    	if(!"".equals(shipperAddressStr0to35)) { writeData.append(shipperAddressStr0to35); }
                    	if(!"".equals(shipperAddressStr35to70)) { writeData.append(":").append(shipperAddressStr35to70); }
                    	if(!"".equals(shipperAddressStr70to105)) { writeData.append(":").append(shipperAddressStr70to105); }
                    	if(!"".equals(shipperAddressStr105to140)) { writeData.append(":").append(shipperAddressStr105to140); }
                    	if(!"".equals(shipperAddressStr140to150)) { writeData.append(":").append(shipperAddressStr140to150); }
                    	
                    	if(!"".equals(shipperNameStr0to35)) { writeData.append("+").append(shipperNameStr0to35); }
                    	if(!"".equals(shipperNameStr35to70)) { writeData.append(":").append(shipperNameStr35to70); }
                    	if(!"".equals(shipperNameStr70to100)) { writeData.append(":").append(shipperNameStr70to100); }
                    	
                    	// 빈값일 경우 구분자만 넣는다 스펙변경 - 20180904
                    	if("AIR".equals(boundStr) && "IMP".equals(boundTypeStr))
                    	{
                    		if(shipperCountryCodeStr.length() == 2 && shipperPlaceCodeStr.length() == 3) 
                    		{
                    			// g-one에서 입력받은 Country Code와 Place Code를 더하여 5자리로 만든다. - 20161221 윤현석
                    			writeData.append("+++");
                    			writeData.append(Util.getFileWriteDataWithRtrim(ediCommonService.getEdiModifyCharacterChk(shipperCountryCodeStr + shipperPlaceCodeStr), "String", "5"));
                    		}
                    		else
                    		{
                    			// 기존에 적용되어있던 방식
//                    			writeData.append("++" + Util.getFileWriteDataWithRtrim(ediCommonService.getEdiModifyCharacterChk(shipperPlaceCodeStr), "String", "5"));
                    		}
                    	}
                    	else
                    	{
//                    		writeData.append("+++").append(Util.getFileWriteDataWithRtrim(ediCommonService.getEdiModifyCharacterChk(shipperPlaceCodeStr), "String", "5"));
                    		writeData.append("+++").append(Util.getFileWriteDataWithRtrim(ediCommonService.getEdiModifyCharacterChk(origPortCodeStr), "String", "5"));	//해상수입-port코드 전송
                    	}
                    		
                    	// 빈값일 경우 구분자만 넣는다 스펙변경 - 20180904
                    	if("AIR".equals(boundStr) && "IMP".equals(boundTypeStr))	writeData.append("++").append(Util.getFileWriteDataWithRtrim(ediCommonService.getEdiModifyCharacterChk(shipperCountryCodeStr), "String", "2"));
                        writeData.append("'");
                        nSegmentCount++;
                    	
                        //COM+전화번호:TE'
                        if(Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_TEL")).trim().length() > 0) 
                        {
                            writeData.append("COM+");
                            writeData.append(Util.getFileWriteDataWithRtrim(Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_TEL")).trim(), "String", "25"));
                            writeData.append(":TE");
                            writeData.append("'"); 
                            nSegmentCount++;
                        }
                    }
                    
                    // "Consignee"
                    consigneeNameStr = Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_NAME")).trim(); //중복 변수 지정 - ditobass!!@

                    if(consigneeNameStr.length() > 0) {
                        consigneeBusinessIdStr = Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_BUSINESS_ID")).trim(); //중복 변수 지정 - ditobass!!@
                        consigneeAddressStr = Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_ADDRESS")).trim(); //중복 변수 지정 - ditobass!!@
                        consigneeCountryCodeStr = Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_COUNTRY_CODE")).trim(); //중복 변수 지정 - ditobass!!@
                        consigneePlaceCodeStr = Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_PLACE_CODE")).trim(); //중복 변수 지정 - ditobass!!@
                        destPortCodeStr = Util.null2str(tempHblDataList.get(nRowIndex).get("DEST_CODE")).trim();
                        consigneeAddressStr0to35 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(consigneeAddressStr, 0, 35)); //중복 변수 지정 - ditobass!!@
                        consigneeAddressStr35to70 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(consigneeAddressStr, 35, 70)); //중복 변수 지정 - ditobass!!@
                        consigneeAddressStr70to105 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(consigneeAddressStr, 70, 105)); //중복 변수 지정 - ditobass!!@
                        consigneeAddressStr105to140 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(consigneeAddressStr, 105, 140)); //중복 변수 지정 - ditobass!!@
                        consigneeAddressStr140to150 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(consigneeAddressStr, 140, 150)); //중복 변수 지정 - ditobass!!@
                        consigneeNameStr0to35 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(consigneeNameStr, 0, 35)); //중복 변수 지정 - ditobass!!@
                        consigneeNameStr35to70 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(consigneeNameStr, 35, 70)); //중복 변수 지정 - ditobass!!@
                        consigneeNameStr70to100 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(consigneeNameStr, 70, 100)); //중복 변수 지정 - ditobass!!@
                        
                        //NAD+CN+사업자번호10자리:58:KTX+수하인명:주소35자리:주소35자리:주소35자리'
                        //NAD+CN+주민번호6자리:174:KAS+수하인명:주소35자리:주소35자리:주소35자리'
                        writeData.append("NAD+CN+");

                        if(consigneeBusinessIdStr.length() > 0) {
                        	
                            //2013.06.25 권태완 변경 - 개인은 주민번호를 사용하므로 주민번호일시에는 앞6자리 년원일만 가져온다.
                            if (consigneeBusinessIdStr.length() == 13) {
                            	/** 생년월일    주민등록번호 7번째 자리가 1이나 2이면 19+주민번호앞6자리 / 3이나 4이면 20+주민번호앞6자리*/
            					if(Util.null2int(consigneeBusinessIdStr.substring(6, 7)) > 2){
            						consigneeBusinessIdStr = "20" + consigneeBusinessIdStr.substring(0, 6);
            					}else{
            						consigneeBusinessIdStr = "19" + consigneeBusinessIdStr.substring(0, 6);
            					}
            					writeData.append(consigneeBusinessIdStr);
            					writeData.append(":174");
                            }else{
                            	writeData.append(consigneeBusinessIdStr);
                            	writeData.append(":58");
                            }
                            writeData.append(":KTX");	//2021-10-13 스팩기준으로 수정  KAS => KTX
                        }

                        if(!"".equals(consigneeAddressStr0to35)) { writeData.append("+").append(consigneeAddressStr0to35); }
                    	if(!"".equals(consigneeAddressStr35to70)) { writeData.append(":").append(consigneeAddressStr35to70); }
                    	if(!"".equals(consigneeAddressStr70to105)) { writeData.append(":").append(consigneeAddressStr70to105); }
                    	if(!"".equals(consigneeAddressStr105to140)) { writeData.append(":").append(consigneeAddressStr105to140); }
                    	if(!"".equals(consigneeAddressStr140to150)) { writeData.append(":").append(consigneeAddressStr140to150); }
                    	
                    	if(!"".equals(consigneeNameStr0to35)) { writeData.append("+").append(consigneeNameStr0to35); }
                    	if(!"".equals(consigneeNameStr35to70)) { writeData.append(":").append(consigneeNameStr35to70); }
                    	if(!"".equals(consigneeNameStr70to100)) { writeData.append(":").append(consigneeNameStr70to100); }
                    	
                    	if("AIR".equals(boundStr) && "IMP".equals(boundTypeStr))
                    	{
                    		// 빈값일 경우 구분자만 넣는다 스펙변경 - 20180904
                    		//if(consigneePlaceCodeStr.length() > 0) {
                    		if(consigneeCountryCodeStr.length() == 2 && consigneePlaceCodeStr.length() == 3) 
                    		{
                    			// g-one에서 입력받은 Country Code와 Place Code를 더하여 5자리로 만든다. - 20161221 윤현석
                    			writeData.append("+++").append(Util.getFileWriteDataWithRtrim(ediCommonService.getEdiModifyCharacterChk(consigneeCountryCodeStr + consigneePlaceCodeStr), "String", "5"));
                    		}
                    		else
                    		{
                    			//writeData.append("+++" + Util.getFileWriteDataWithRtrim(ediCommonService.getEdiModifyCharacterChk(consigneePlaceCodeStr), "String", "5"));
                    		}
                    	}
                    	else
                    	{
//                    		writeData.append("+++").append(Util.getFileWriteDataWithRtrim(ediCommonService.getEdiModifyCharacterChk(consigneePlaceCodeStr), "String", "5"));
                    		writeData.append("+++").append(Util.getFileWriteDataWithRtrim(ediCommonService.getEdiModifyCharacterChk(destPortCodeStr), "String", "5"));	//해상수입-port코드 전송
                    	}
                    	
                      	if("AIR".equals(boundStr) && "IMP".equals(boundTypeStr))	writeData.append("++").append(Util.getFileWriteDataWithRtrim(ediCommonService.getEdiModifyCharacterChk(consigneeCountryCodeStr), "String", "2"));
                        writeData.append("'");
                        nSegmentCount++;
                        
                        //COM+전화번호:TE'
                        if(Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_TEL")).trim().length() > 0) {
                        	writeData.append("COM+").append(Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_TEL")).trim()).append(":TE");
                        	writeData.append("'"); 
                        	nSegmentCount++;
                        }
                        //통지처 >> 22.05.06 하드코딩 요청 : 장순문
                        if("AIR".equals(boundStr) && "IMP".equals(boundTypeStr)) {
                        	writeData.append("NAD+NI++SAME AS CONSIGNEE+SAME AS CONSIGNEE++'");
                        	nSegmentCount++;
                        }
                    }
                    
                    //통지처	>> 해상수입 - 수하인명 여부와 관계없이 통지처 항상 SAME AS CONSIGNEE
                    if("SEA".equals(boundStr) && "IMP".equals(boundTypeStr)) {
	                    writeData.append("NAD+NI++SAME AS CONSIGNEE+SAME AS CONSIGNEE+++++");
	                	writeData.append("'"); 
	                	nSegmentCount++;
                    }
                    
                } /** F:06:추가 - HOUSE END */
                
                // 4세대 스펙에서 삭제됨 배정장소 삭제
//                if("SEA".equals(boundStr) && "IMP".equals(boundTypeStr)) {
//                	if(ediCommonService.getLocation(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_ASSIGNMENT_WH_CODE")).trim(), docSend).trim().length() > 0) {
//                		//LOC+14+배정창고코드:129:KCS'
//                		writeData.append("LOC+14+" + Util.getFileWriteData(ediCommonService.getLocation(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_ASSIGNMENT_WH_CODE")).trim(), docSend).trim(), "String", "8") + ":129:KCS");
//	                    writeData.append("'"); 
//	                    nSegmentCount++;
//                	}
//                }
                
                // 4세대 스펙에서 삭제됨 배정장소 삭제
//                if("AIR".equals(boundStr) && "IMP".equals(boundTypeStr)) {
//                	//Destination
//                	if(Util.null2str(tempHblDataList.get(nRowIndex).get("DEST_CODE")).trim().length() > 0) {
//                		//LOC+20+도착지코드'
//                		writeData.append("LOC+20+" + Util.getFileWriteDataWithRtrim(Util.null2str(tempHblDataList.get(nRowIndex).get("DEST_CODE")).trim(), "String", "5"));
//                		writeData.append("'"); 
//                		nSegmentCount++;
//                	}
//                }
                //송하인, 수하인, 통지처 , 화물운송주선업자 정보
//              "AI_SAVE_SAM_DMS_GIS_NAD"

                
                /////////////////
                // 해운 수입
                /////////////////
                if("SEA".equals(boundStr) && "IMP".equals(boundTypeStr)) {
                	
                	//샘플에선 생성 안 되고 있어 샘플 기준으로 주석 처리함
//                	if(modType4th.equals("06")) {
                		// 공동혼재화물 국내화물 운송주선업자 부호 (edi_coloader_4code) - khr 20200227
                    	// coloader값이 없으면 아예 붙이지 않는다 
//                    	if(!"".equals(strFWDCode)){
//                    		writeData.append("NAD+");
//                    		writeData.append("FW+"); //국내화물운송주선업자 (고정)
//                    		writeData.append(strFWDCode);//4자리
//                    		writeData.append(":5CG"); //운송주선업자 (고정)
//                    		writeData.append(":KCS'"); //한국관세청 (고정)
//                    		
//                    		nSegmentCount++;
//                    	}
//                	}
                	
                	
//                	"Container 정정"
                    //Container 정정
//                	tempSearchData = new HashMap<String, Object>();
                	if(modType4th.equals("06") || modType4th.equals("99")) {
                		tempSearchData.clear(); //초기화 - ditobass!!@
                    	tempSearchData.put("odJobNo", Util.null2str(tempHblDataList.get(nRowIndex).get("OD_JOB_NO")).trim());
                        tempHblCntrList = (List<Map<String, Object>>)tempHblDataList.get(nRowIndex).get("containerList");
                        
//                        int nEQDCNT = 0; //사용 안함 주석 - ditobass!!@
                        for(int nContainerIndex = 0; nContainerIndex < tempHblCntrList.size(); ++nContainerIndex) {
//                            nEQDCNT++; //사용 안함 주석 - ditobass!!@
                            
                            //컨테이너 수정사항
                            if(Util.null2str(tempHblCntrList.get(nContainerIndex).get("CNTR_NO")).trim().length() > 0) {
                            	modTypeCntr = Util.null2str(tempHblCntrList.get(nContainerIndex).get("MOD_TYPE_CNTR"));
                                //컨테이너 번호 ※??? EQD+CN+TGHU3176829+22GP
                                writeData.append("EQD+CN+");
                                writeData.append(Util.null2str(tempHblCntrList.get(nContainerIndex).get("CNTR_NO")).trim());
                                if(modTypeCntr.equals("D")){
                                    writeData.append("'"); 
                                    nSegmentCount++;
                                    
                                  //Container 정정 구분  ※GIS+I:5DS:KCS'
                                    //OI_SAVE_SAM_DMS_IMD_EQD_GIS
                                    writeData.append("GIS+");
                                    writeData.append(modTypeCntr);
                                    writeData.append(":5DS:KCS");
                                    writeData.append("'"); 
                                    nSegmentCount++;
                                    correctionContents.append("삭제:" + Util.null2str(tempHblCntrList.get(nContainerIndex).get("CNTR_NO")));
                                }else {
                                	if(modTypeCntr.equals("A") || modTypeCntr.equals("I")){
                                		writeData.append("+");
                                		writeData.append(Util.null2str(tempHblCntrList.get(nContainerIndex).get("CNTR_EDI_CODE")).trim());
                                	}
                                	writeData.append("'");
                                    nSegmentCount++;
                                    
                                    //Container 정정 구분  ※GIS+I:5DS:KCS'
                                    //OI_SAVE_SAM_DMS_IMD_EQD_GIS
                                    writeData.append("GIS+");
                                    writeData.append(modTypeCntr);
                                    writeData.append(":5DS:KCS");
                                    writeData.append("'"); 
                                    nSegmentCount++;
                                    
                                    if(modTypeCntr.equals("U")){
//                                    	"Container_사유정보"
                                        //Container_Single 정보
//                                        tempSearchData = new HashMap<String, Object>();
                                        tempSearchData.clear(); //초기화 - ditobass!!@
                                        correctionContents.append("정정:" + Util.null2str(tempHblCntrList.get(nContainerIndex).get("CNTR_NO")) + "(");
                                        tempSearchData.put("modJobNo", Util.null2str(tempHblCntrList.get(nContainerIndex).get("MOD_JOB_NO")).trim());
                                        tempSearchData.put("cntrNo", Util.null2str(tempHblCntrList.get(nContainerIndex).get("CNTR_NO")).trim());
                                        tempHblCntrReasonList = (List<Map<String, Object>>)tempHblDataList.get(nRowIndex).get("containerModList");

//                                        nEQDCNT = 0; //사용 안함 주석- ditobass!!@
                                        //컨테이너 항목정정 관련 사항을 기재
                                        for(int nContainerSubIndex = 0; nContainerSubIndex < tempHblCntrReasonList.size(); ++nContainerSubIndex) {
//                                            nEQDCNT++; //사용 안함 주석 - ditobass!!@
                                            
                                            //Container 포장부호/내장개수 ※FTX+CHG+++정정항목:정정전내용:정정후내용
                                            if(Util.null2str(tempHblCntrReasonList.get(nContainerSubIndex).get("MOD_ITEM_CODE_CNTR")).trim().length() > 0) {
                                            	//writeData.append("FTX");
                                            	
                                            	// ※컨테이너 정정전/후 20자리 짤라야됨.
                                            	writeData.append("FTX+CHG+++");
                                            	writeData.append(Util.null2str(tempHblCntrReasonList.get(nContainerSubIndex).get("MOD_ITEM_CODE_CNTR")).trim());
                                            	writeData.append(":").append(Util.null2str(tempHblCntrReasonList.get(nContainerSubIndex).get("MOD_DESC_BEFORE")).trim());
                                            	writeData.append(":").append(Util.null2str(tempHblCntrReasonList.get(nContainerSubIndex).get("MOD_DESC_AFTER")).trim());
                                                writeData.append("'"); 
                                                nSegmentCount++;
                                                correctionContents.append(Util.null2str(tempHblCntrReasonList.get(nContainerSubIndex).get("MOD_ITEM_NAME_CNTR")) + ":");
                                                correctionContents.append(Util.null2str(tempHblCntrReasonList.get(nContainerSubIndex).get("MOD_DESC_BEFORE")) + "=>");
                                                correctionContents.append(Util.null2str(tempHblCntrReasonList.get(nContainerSubIndex).get("MOD_DESC_AFTER")));
                                                if(tempHblCntrReasonList.size() != nContainerSubIndex + 1){
                                                	correctionContents.append(", ");
                                                }else{
                                                	correctionContents.append(")");
                                                }
                                            }
                                        }
                                        
                                    }else {
                                    	//In Bulk 건의 경우 수량과 수량단위가 안들어 가도 아래 사항은 포함되어야 함 - 2011.03.21 이하양
                                        if(Util.null2int(tempHblCntrList.get(nContainerIndex).get("PIECES")) > 0
                                        		|| Util.null2str(tempHblCntrList.get(nContainerIndex).get("CNTR_NO")).trim().contains("IN BULK") == true) 
                                        {
                                            //Container 컨테이너 내장개수/내장단위 ※MEA+5AA++UNIT_PIECES:PIECES
                                        	//'''@@@ 4세대 포장부호 GT 고정값
                                        	writeData.append("MEA+5AA++GT");
//                                        			+ Util.null2str(tempHblCntrList.get(nContainerIndex).get("UNIT_PIECES")).trim()
                                        	writeData.append(":").append(Util.null2int(tempHblCntrList.get(nContainerIndex).get("PIECES")));
                                            writeData.append("'"); 
                                            nSegmentCount++;
                                        }

                                        //In Bulk 건의 경우 수량과 수량단위가 안들어 가도 아래 사항은 포함되어야 함 - 2011.03.21 이하양
                                        if(Util.null2str(tempHblCntrList.get(nContainerIndex).get("UNIT_PIECES")).trim().length() > 0
                                        		|| Util.null2str(tempHblCntrList.get(nContainerIndex).get("CNTR_NO")).trim().contains("IN BULK") == true) 
                                        {

                                            cntrSealNo1Str = Util.null2str(tempHblCntrList.get(nContainerIndex).get("CNTR_SEAL_NO_1")).trim(); //중복 변수 지정 - ditobass!!@
                                            cntrSealNo1Str0to10 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(cntrSealNo1Str, 0, 10)); //중복 변수 지정 - ditobass!!@
                                            cntrSealNo1Str10to20 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(cntrSealNo1Str, 10, 20)); //중복 변수 지정 - ditobass!!@
                                            
                                            //Container 봉인번호1/봉인번호2/봉인번호3  ※
                                            //OI_SAVE_SAM_DMS_IMD_EQD_SEL
                                            writeData.append("SEL+");
                    				    	if(!"".equals(cntrSealNo1Str0to10)) { writeData.append(cntrSealNo1Str0to10); }
            	                        	if(!"".equals(cntrSealNo1Str10to20)) { writeData.append("+:::").append(cntrSealNo1Str10to20); }
            	                            writeData.append("'");
            	                            nSegmentCount++;

                                            cntrSealNo2Str = Util.null2str(tempHblCntrList.get(nContainerIndex).get("CNTR_SEAL_NO_2")).trim(); //중복 변수 지정 - ditobass!!@
                                            if(cntrSealNo2Str.length() > 0) 
                                            {
                                                cntrSealNo2Str0to10 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(cntrSealNo2Str, 0, 10)); //중복 변수 지정 - ditobass!!@
                                                cntrSealNo2Str10to20 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(cntrSealNo2Str, 10, 20)); //중복 변수 지정 - ditobass!!@
                                                
                                                writeData.append("SEL+");
            	        				    	if(!"".equals(cntrSealNo2Str0to10)) { writeData.append(cntrSealNo2Str0to10); }
            		                        	if(!"".equals(cntrSealNo2Str10to20)) { writeData.append("+:::").append(cntrSealNo2Str10to20); }
            		                            writeData.append("'");
            		                            nSegmentCount++;

                                                cntrSealNo3Str = Util.null2str(tempHblCntrList.get(nContainerIndex).get("CNTR_SEAL_NO_3")).trim(); //중복 변수 지정 - ditobass!!@
                                                if(cntrSealNo3Str.length() > 0) 
                                                {
                                                    cntrSealNo3Str0to10 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(cntrSealNo3Str, 0, 10)); //중복 변수 지정 - ditobass!!@
                                                    cntrSealNo3Str10to20 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(cntrSealNo3Str, 10, 20)); //중복 변수 지정 - ditobass!!@
                                                    
                                                	writeData.append("SEL+");
                	        				    	if(!"".equals(cntrSealNo3Str0to10)) { writeData.append(cntrSealNo3Str0to10); }
                		                        	if(!"".equals(cntrSealNo3Str10to20)) { writeData.append("+:::").append(cntrSealNo3Str10to20); }
                		                            writeData.append("'");
                		                            nSegmentCount++;
                                                }
                                            }
                                        }
                                        correctionContents.append("추가:" + Util.null2str(tempHblCntrList.get(nContainerIndex).get("CNTR_NO")));
                                    }
                                }
                             }
                            if(tempHblCntrList.size() != nContainerIndex + 1){
                            	correctionContents.append(", ");
                            }else{
                            	correctionContents.append(")");
                            }
                        }
                	}
                	
                }
                if("05".equals(modType4th)){
	//              "Single 정정"
	//                tempSearchData = new HashMap<String, Object>();
	                tempSearchData.clear(); //초기화 - ditobass!!@
	                tempSearchData.put("odJobNo", Util.null2str(tempHblDataList.get(nRowIndex).get("OD_JOB_NO")));
	                tempSearchData.put("modJobNo", Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_JOB_NO")));
	                tempSearchData.put("entCode", docSend.get("entCode"));
	                tempHblSingleList = manifestMapper.getEdiMfModifyHblSingleList(tempSearchData);
	                shtFinishFlag = false;
	//                nLINCNT = 0; //사용 안함 주석 - ditobass!!@
	                for(int nSingleIndex = 0; nSingleIndex < tempHblSingleList.size(); ++nSingleIndex) {
	//                    nLINCNT++; //사용 안함 주석 - ditobass!!@
	                    
	                    //정정항목번호
	                    if(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_ITEM_CODE_HBL")).trim().length() > 0) {
	                    	if(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_ITEM_CODE_HBL")).equals("DA")){
	                    		shtFinishFlag = true;
	                    	}
	                        //LIN+++정정항목코드'
	                        writeData.append("LIN+++").append(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_ITEM_CODE_HBL")).trim());
	                        writeData.append("'"); 
	                        nSegmentCount++;
	                        
	                        //Single 정정사유부호| 정정전 내역| 정정후 내역
	                        if(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_REASON_CODE_HBL")).trim().length() > 0) {
	                            //GIS+정정사유코드:5FH:KCS'
	                            writeData.append("GIS+").append(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_REASON_CODE_HBL")).trim()).append(":5FH:KCS");
	                            writeData.append("'"); 
	                            nSegmentCount++;
	                        }
	
	                        //정정전 내역
	                        modDescBefore = Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_DESC_BEFORE")).trim(); //중복 변수 지정 - ditobass!!@
	                        if(modDescBefore.length() > 0) {
	
	                            modDescBefore0to50 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(modDescBefore,0,50)); //중복 변수 지정 - ditobass!!@
	                            modDescBefore50to100 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(modDescBefore,50,100)); //중복 변수 지정 - ditobass!!@
	                            modDescBefore100to150 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(modDescBefore,100,150)); //중복 변수 지정 - ditobass!!@
	                            modDescBefore150to200 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(modDescBefore,150,200)); //중복 변수 지정 - ditobass!!@
	                            
	                        	// ※ ':' 확인
	                            //FTX+AAO+++20143268'
	                            //FTX+AAO+++정정전내용50자리:정정전내용50자리:정정전내용50자리:정정전내용50자리:
	                            //FTX+AAO+++정정전내용200자리
	                            //상기 2개중 1나로 먼저 개발 후 오류시 바꾸기로 한다. 2014.11.24
	                            writeData.append("FTX+AAO+++");
	                            
		        				if(!"".equals(modDescBefore0to50)) { writeData.append(modDescBefore0to50); }
		        				writeData.append(":");
		        				if(!"".equals(modDescBefore50to100)) { writeData.append(modDescBefore50to100); }
		        				writeData.append(":");
		        				if(!"".equals(modDescBefore100to150)) { writeData.append(modDescBefore100to150); }
		        				writeData.append(":");
		        				if(!"".equals(modDescBefore150to200)) { writeData.append(modDescBefore150to200); }
		                        writeData.append("'");
		                        nSegmentCount++;
	                        }
	
	                        //Single 정정후 내역
	                        //AI_SAVE_SAM_DMS_IMD_LIN_FTX2"
	                        modDescAfter = Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_DESC_AFTER")).trim(); //중복 변수 지정 - ditobass!!@
	                        if(modDescAfter.length() > 0) {
	                            
	                            modDescAfter0to50 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(modDescAfter,0,50)); //중복 변수 지정 - ditobass!!@
	                            modDescAfter50to100 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(modDescAfter,50,100)); //중복 변수 지정 - ditobass!!@
	                            modDescAfter100to150 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(modDescAfter,100,150)); //중복 변수 지정 - ditobass!!@
	                            modDescAfter150to200 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(modDescAfter,150,200)); //중복 변수 지정 - ditobass!!@
	                            
	                            //FTX+CHG+++ICN20143268'
	                            //FTX+CHG+++정정전내용50자리:정정전내용50자리:정정전내용50자리:정정전내용50자리:
	                            //FTX+CHG+++정정전내용200자리
	                            //상기 2개중 1나로 먼저 개발 후 오류시 바꾸기로 한다. 2014.11.24
	                            writeData.append("FTX+CHG+++");
	
	                            if(!"".equals(modDescAfter0to50)) { writeData.append(modDescAfter0to50); }
	                            writeData.append(":");
		        				if(!"".equals(modDescAfter50to100)) { writeData.append(modDescAfter50to100); }
		        				writeData.append(":");
		        				if(!"".equals(modDescAfter100to150)) { writeData.append(modDescAfter100to150); }
		        				writeData.append(":");
		        				if(!"".equals(modDescAfter150to200)) { writeData.append(modDescAfter150to200); }
		                        writeData.append("'");
		                        nSegmentCount++;
	                        }
	                        correctionContents.append(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_ITEM_NAME_HBL")) + ":");
	                        correctionContents.append(modDescBefore + "=>");
	                        correctionContents.append(modDescAfter);
	                        if(nSingleIndex != tempHblSingleList.size() + 1){
	                        	correctionContents.append(", ");
	                        }else{
	                        	correctionContents.append(")");
	                        }
	                    }
	                }
	                if(!shtFinishFlag){
	                	tempHblDataList.get(nRowIndex).put("ABNORMAL_SEQ_NO_ID", null);
	                }
                }
                //UNT+16+1'
                if(correctionContents.length() > 1000){
                	tempHblDataList.get(nRowIndex).put("correctionContents", correctionContents.toString().substring(0, 1000));
                }else{
                	tempHblDataList.get(nRowIndex).put("correctionContents", correctionContents.toString());
                }
                tempHblDataList.get(nRowIndex).put("gmtTime", docSend.get("gmtTime"));
            	manifestMapper.updateMfModifyInfo(tempHblDataList.get(nRowIndex));
            	
            	/** MGN->적하정정삭제 or MFN->적하정정추가 or SHT->적하항목정정(DA-H B/L 포장갯수) 일경우 하기이상 완료여부 Y 업데이트 */
            	if(tempHblDataList.get(nRowIndex).get("ABNORMAL_SEQ_NO_ID") != null) {
            		abnormalMapper.updateAbnormalFinishYn(tempHblDataList.get(nRowIndex));
            	}
            }
            
            //UNT+세그먼트 갯수(UNH부터 UNT까지 세그먼트 갯수)+1'
            writeData.append("UNT+");
            writeData.append(String.valueOf(nSegmentCount)).append("+1");
            writeData.append("'");
            writeData.append("UNZ+1+1'");
            /*문서쓰기 변경으로인하여 주석처리
            bfw.write(writeData.toString());
			retWriteData.append(writeData.toString());	// DB에 저장할 파일 내용 담기
			*/
    		//파일 코딩타입
//    		logger.debug(System.getProperty("file.encoding"));
            
//    		Util.fileWrite(new String(writeData.getBytes("ISO-8859-1"), "KSC5601"), dirPath0, fileName, "euc-kr");
            FileUtil.fileWrite(writeData.toString(), dirPath0, fileName, "euc-kr");
			
		} catch(Exception e) {
			throw e;
		} finally {
		}
		
		retData[0] = fileName;	// 파일명
		retData[1] = writeData.toString();	// 파일 내용
		retData[2] = dirPath[0];	// 원본 로컬 경로
		retData[3] = dirPath[1];	// 백업 로컬 경로
		
		retData[4] = "IMFMOD5LI";
		retData[5] = modReqNo;
		
		return retData;
	}
	
	/**
     * 파일생성'''@@@
     * 관세청 4세대 전용
     * @param docSend
     * @param gridRow
     * @param tempMblData
     * @param tempHblDataList
     * @param fwSendAccInfo
     * @param ediHostSendInfo
     * @return
     * @throws java.lang.Exception
     */
	public String[] getSendDataFileCreate4thKCNET_old(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempHblDataList, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo) throws java.lang.Exception {
		
		List<Map<String, Object>> tempHblSingleList = null;
		List<Map<String, Object>> tempHblCntrList = null;
		List<Map<String, Object>> tempHblCntrReasonList = null;
		Map<String, Object> tempSearchData = new HashMap<>();
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(docSend, "gmtTime", false, null, null, null, null);
		
		/*주석처리 문서쓰기 변경
		StringBuffer retWriteData = new StringBuffer();	// 파일 용량이 클경우 내용 저장하면 안됨.
		*/
		String modTypeCntr = null;
		String writeData = null;
		String retData[] = new String[6];
		String fileName = new StringBuffer(Util.null2str(docSend.get("registerUserId"))).append(".H").append(Util.null2str(tempMblData.get("MBL_NO")).trim()).append(".txt").toString();	// 전송 파일명
        String ediVender = Util.null2str(docSend.get("ediVender"));
		String dirPath[] = ediCommonService.getSendFilePath(ediVender, "edi/manifestMod", docSend);	// 로컬폴더 경로 가져오기
		
		/*문서쓰기방식 변경하여 추가*/
		String dirPath0 = dirPath[0];
		
		String modReqNo = Util.null2str(tempMblData.get("MOD_DECLARE_NO"));
		String strDeclarerDate = Util.null2str(tempMblData.get("MOD_DECLARE_DATE")).trim().replace("-","").replace(":", "").replace(" ", "");
		boolean shtFinishFlag;
		
		// 파일 내용 쓰기
		try {
			ByteArrayOutputStream out = null;
			DOMSource source = null;
			StreamResult streamResult = null;
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			
			Document document = null;
			Element root = null;
			Element lv1 = null;
			Element lv2 = null;
			Element lv3 = null;
			Element lv4 = null;
			Element lv5 = null;
			Element lv6 = null;
			Map<String, String> attribute = new HashMap<String, String>();
			
			document = documentBuilder.newDocument();
			document.setXmlVersion("1.0");
			document.setXmlStandalone(true);
			
			Element soapenvBody = null;
			Element soapenv = document.createElement("soapenv:Envelope");
//			soapenv.setAttribute("xmlns:soapenv","http://schemas.xmlsoap.org/soap/envelope");
			soapenv.setAttribute("xmlns:soapenv","http://www.w3.org/2003/05/soap-envelope");
			soapenv.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema");
			soapenv.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			
			document.appendChild(soapenv);
			
			/** soapenvHeader */
			attribute.put("attributeName", "xmlns:kcn");
			attribute.put("attributeContent", "http://www.kcnet.co.kr/kcn");
			lv1 = this.createElement(document, soapenv, lv1, "soapenv:Header", null, attribute);
			
			lv2 = this.createElement(document, lv1, lv2, "kcn:Kcnet_Header", null);
			
			/** 송신아이디 */
			this.createElement(document, lv2, lv3, "kcn:SenderId", Util.getFileWriteDataWithRtrim(ediCommonService.getDeleteCharStringFwb(Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).trim()), "String", "4"));
			/** 수신아이디 */
			this.createElement(document, lv2, lv3, "kcn:ReceiverId", "KCNECP");
			/** 문서명 */
			this.createElement(document, lv2, lv3, "kcn:DocName", "5LI");
			/** 문서코드 */
			this.createElement(document, lv2, lv3, "kcn:DocCode", "5LI");
			/** 메세지아이디 */
			this.createElement(document, lv2, lv3, "kcn:MessageId", Util.null2str(tempMblData.get("MBL_NO")));
			/** 중개업체 */
			this.createElement(document, lv2, lv3, "kcn:VenderId", "YHT");
			/** KCNETID */
			this.createElement(document, lv1, lv2, "kcn:ID", Util.null2str(ediHostSendInfo.get("IF_WEB_ID")));
			/** KCNETPW */
			this.createElement(document, lv1, lv2, "kcn:Password", Util.null2str(ediHostSendInfo.get("IF_WEB_PWD")));
			
			
//			soapenvBody = this.createElement(document, lv1, soapenvBody, "soapenv:Body", null);
			soapenvBody = this.createElement(document, soapenv, soapenvBody, "soapenv:Body", null);
			
			root = document.createElement("wco:Declaration");
			root.setAttribute("xmlns:kcs", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5LISchemaModule:1:0");
			root.setAttribute("xmlns:wco", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5LISchemaModule:1:1");
			root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			root.setAttribute("xsi:schemaLocation", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5LISchemaModule:1:0 ./schema4G/kcs/data/standard/KCS_DeclarationOfCAG_5LISchemaModule_1.0_standard.xsd");
			soapenvBody.appendChild(root);
			
			/** 신고세관/신고과 */
			this.createElement(document, root, lv1, "wco:DeclarationOfficeID", Util.null2str(tempMblData.get("CUSTOMS_CODE")).trim() + Util.null2str(tempMblData.get("CUSTOMS_DPT_CODE")).trim());
			/** 제출번호 */
			this.createElement(document, root, lv1, "wco:ID", modReqNo);
			/** 제출일시 */
			this.createElement(document, root, lv1, "wco:IssueDateTime", strDeclarerDate);
			/** 문서형태구분 */
			this.createElement(document, root, lv1, "wco:TypeCode", "GOVCBR5LI");
			/** 적하목록문서 구분 */
			this.createElement(document, root, lv1, "kcs:SubTypeCode", "40");
			
//			/** wco:Consignment 시작 */
//			lv1 = this.createElement(document, root, lv1, "wco:Consignment", null);
//			/** AWB 번호(M-AWB) */
//			lv2 = this.createElement(document, lv1, lv2, "wco:TransportContractDocument", null);
//			this.createElement(document, lv2, lv3, "wco:ID", Util.null2str(tempMblData.get("MBL_NO")));
			
			String tmpStr = "";
            String modType4th = null;
            StringBuffer correctionContents = new StringBuffer();
            for(int nRowIndex = 0; nRowIndex < tempHblDataList.size(); nRowIndex++) {
            	correctionContents.setLength(0);
            	modType4th = Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_TYPE_4TH"));
            	
            	String oldCorrectionContents = Util.null2str(manifestMapper.selectOrderCorrectionContents(tempHblDataList.get(nRowIndex)));
            	if(!oldCorrectionContents.equals("")){
            		correctionContents.append(oldCorrectionContents + "|");
            	}
            	if(modType4th.equals("05")){
            		correctionContents.append("HBL정정:" + Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_REASON_NAME")) + "(");
            	}else if(modType4th.equals("06")){
            		correctionContents.append("HBL추가:" + Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_REASON_NAME")));
            	}else if(modType4th.equals("07")){
            		correctionContents.append("HBL삭제:" + Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_REASON_NAME")));
            	}
            	
            	/** wco:GeneralInformation 시작 */
    			lv1 = this.createElement(document, root, lv1, "wco:GeneralInformation", null);
    			/** 작업구분 부호 */
    			this.createElement(document, lv1, lv2, "kcs:StatementCode", modType4th.equals("99") ? "05" : modType4th);
    			/** 정정사유 */
    			this.createElement(document, lv1, lv2, "kcs:StatementDescription", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_REASON_NAME")), 0, 100));
    			
    			/** wco:HouseConsignment 시작 */
    			lv2 = this.createElement(document, lv1, lv2, "wco:HouseConsignment", null);
    			if (modType4th.equals("06")){	//추가
    				/** 화물구분 */
    				this.createElement(document, lv2, lv3, "kcs:CargoProcessTypeCode", Util.null2str(tempHblDataList.get(nRowIndex).get("BL_TYPE_IERT")));
    				/** 포장개수 */
    				this.createElement(document, lv2, lv3, "wco:TotalPackageQuantity", Util.null2str(tempHblDataList.get(nRowIndex).get("PIECES")));
    				/** 총용적 */
    				this.createElement(document, lv2, lv3, "wco:GrossVolumeMeasure", Util.null2str(tempHblDataList.get(nRowIndex).get("GWT")));
                }

    			/** wco:AssociatedTransportDocument 시작 */
    			lv3 = this.createElement(document, lv2, lv3, "wco:AssociatedTransportDocument", null);
    			/** House AWB번호 */
    			this.createElement(document, lv3, lv4, "wco:ID", ediCommonService.DeleteCharString_Loop(Util.null2str(tempHblDataList.get(nRowIndex).get("HBL_NO")).trim(), " |-", "|"));
    			/** House B/L 일련번호 */
    			this.createElement(document, lv3, lv4, "kcs:HouseBLSequenceID", Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_HSN")).trim());
    			/** wco:AssociatedTransportDocument 끝 */
    			
    			/** wco:TransportContractDocument 시작 */
    			lv3 = this.createElement(document, lv2, lv3, "wco:TransportContractDocument", null);
    			/** Master AWB번호 */
    			this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(tempMblData.get("MBL_NO")).trim());
    			/** Master B/L 일련번호 */
    			this.createElement(document, lv3, lv4, "kcs:MasterBLSequenceID", Util.null2str(tempMblData.get("EDI_MSN")).trim());
    			/** wco:TransportContractDocument 끝 */
    			
    			if (modType4th.equals("06")){	//추가
    				/** 최종 목적지 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:GoodsReceiptPlace", null);
        			this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("DEST_CODE")).trim());
        			/** 포장종류 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:Packaging", null);
//        			this.createElement(document, lv3, lv4, "wco:TypeCode", Util.null2str(tempHblDataList.get(nRowIndex).get("UNIT_PIECES")).trim());
    				if(Util.null2str(docSend.get("srchBound")).equals("AIR")){
    					this.createElement(document, lv3, lv4, "wco:TypeCode", "CT");
    				}else{
    					this.createElement(document, lv3, lv4, "wco:TypeCode", "GT");
    				}
        			
        			/** wco:Consignor 시작 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:Consignor", null);
    				/** 송하인 상호 또는 성명 */
    				this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_NAME")).trim(), 0, 50));
    				if(!Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_ADDRESS")).trim().equals("")){
    					/** wco:Address 시작 */
        				lv4 = this.createElement(document, lv3, lv4, "wco:Address", null);
        				/** 송하인 국가코드 */
        				this.createElement(document, lv4, lv5, "wco:CountryCode", Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_COUNTRY_CODE")).trim());
//        				if(!Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_COUNTRY_CODE")).trim().equals("") && !Util.null2str(tempHblDataList.get(nRowIndex).get("TEDAI_MOD_HBL.ShipperCity")).trim().equals("")){
//        					this.createElement(document, lv4, lv5, "wco:CountrySubDivisionID", Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_COUNTRY_CODE")).trim() + Util.null2str(tempHblDataList.get(nRowIndex).get("TEDAI_MOD_HBL.ShipperCity")).trim());
//        				}
        				//송하인 국가코드 -> 국가코드+ORIG_CODE
        				this.createElement(document, lv4, lv5, "wco:CountrySubDivisionID", Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_COUNTRY_CODE")).trim() + Util.null2str(tempHblDataList.get(nRowIndex).get("ORIG_CODE")).trim());
        				this.createElement(document, lv4, lv5, "wco:Line", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_ADDRESS")).trim(), 0, 150));
        				/** wco:Address 끝 */
    				}
    				if(!Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_TEL")).trim().equals("")){
    					/** wco:Communication 시작 */
        				lv4 = this.createElement(document, lv3, lv4, "wco:Communication", null);
        				/** 통신수단유형 */
        				this.createElement(document, lv4, lv5, "wco:TypeID", "TE");
        				/** 송하인 전화번호 */
        				this.createElement(document, lv4, lv5, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_TEL")).trim());
        				/** wco:Communication 끝 */
    				}
    				/** wco:Consignor 끝 */
    				
    				/** wco:Consignee 시작 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:Consignee", null);
    				/** 사업자등록번호/생년월일 */
    				attribute.put("attributeName", "schemeAgencyID");
    				tmpStr = Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_BUSINESS_ID"));
    				if(tmpStr.length() == 10){
    					attribute.put("attributeContent", "KTX");
    				}else{
    					attribute.put("attributeContent", "KAS");
    					if(tmpStr.length() == 13){
    						/** 생년월일    주민등록번호 7번째 자리가 1이나 2이면 19+주민번호앞6자리 / 3이나 4이면 20+주민번호앞6자리*/
    						if(Util.null2int(tmpStr.substring(6, 7)) > 2){
    							tmpStr = "20" + tmpStr.substring(0, 6);
    						}else{
    							tmpStr = "19" + tmpStr.substring(0, 6);
    						}
    					}
    				}
    				this.createElement(document, lv3, lv4, "wco:ID", tmpStr, attribute);
    				/** 수하인 상호 또는 성명 */
    				this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_NAME")).trim(), 0, 50));
    				if(!Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_ADDRESS")).trim().equals("")){
    					/** wco:Address 시작 */
        				lv4 = this.createElement(document, lv3, lv4, "wco:Address", null);
        				/** 수하인 국가코드 */
        				this.createElement(document, lv4, lv5, "wco:CountryCode", Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_COUNTRY_CODE")).trim());
//        				if(!Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_COUNTRY_CODE")).trim().equals("") && !Util.null2str(tempHblDataList.get(nRowIndex).get("TEDAI_MOD_HBL.ConsigneeCity")).trim().equals("")){
//        					this.createElement(document, lv4, lv5, "wco:CountrySubDivisionID", Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_COUNTRY_CODE")).trim() + Util.null2str(tempHblDataList.get(nRowIndex).get("TEDAI_MOD_HBL.ConsigneeCity")).trim());
//        				}
        				//수하인 국가코드 -> 국가코드+DEST_CODE
        				this.createElement(document, lv4, lv5, "wco:CountrySubDivisionID", Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_COUNTRY_CODE")).trim() + Util.null2str(tempHblDataList.get(nRowIndex).get("DEST_CODE")).trim());
        				this.createElement(document, lv4, lv5, "wco:Line", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_ADDRESS")).trim(), 0, 150));
        				/** wco:Address 끝 */
    				}
    				if(!Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_TEL")).trim().equals("")){
    					/** wco:Communication 시작 */
        				lv4 = this.createElement(document, lv3, lv4, "wco:Communication", null);
        				/** 통신수단유형 */
        				this.createElement(document, lv4, lv5, "wco:TypeID", "TE");
        				/** 수하인 전화번호 */
        				this.createElement(document, lv4, lv5, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_TEL")).trim());
        				/** wco:Communication 끝 */
    				}
    				/** wco:Consignee 끝 */

    				//220603 장부장님 : 통지처 ID(""), Name("SAME AS CONSIGNEE") 추가
    				/** wco:NotifyParty 시작 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:NotifyParty", null);
        			/** 사업자등록번호 */
        			this.createElement(document, lv3, lv4, "wco:ID", "");
        			/** 통지처 상호 또는 성명 */
        			this.createElement(document, lv3, lv4, "wco:Name", "SAME AS CONSIGNEE");
        			/** wco:NotifyParty 끝 */
    				
    				/** wco:ConsignmentItem 시작 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:ConsignmentItem", null);
    				
    				/** wco:Commodity 시작 */
    				lv4 = this.createElement(document, lv3, lv4, "wco:Commodity", null);
    				/** 품명 */
    				this.createElement(document, lv4, lv5, "wco:CargoDescription", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("COMMODITY_ITEM_DESC")).trim(), 0, 200));
    				
    				/** wco:Classification 시작 */
    				lv5 = this.createElement(document, lv4, lv5, "wco:Classification", null);
    				/** HSCode */
    				this.createElement(document, lv5, lv6, "wco:ID", "");
    				//특수화물코드(필수항목이라 'NO'로 함)[동물검역,식물검역,보온/보냉물품]
    				this.createElement(document, lv5, lv6, "wco:IdentificationTypeCode", "NO");
    				this.createElement(document, lv5, lv6, "wco:IdentificationTypeCode", "NO");
    				this.createElement(document, lv5, lv6, "wco:IdentificationTypeCode", "NO");
    				/** wco:Classification 끝 */
    				/** wco:Commodity 끝 */
    				
    				/** 중량 */
    				lv4 = this.createElement(document, lv3, lv4, "wco:GoodsMeasure", null);
    				this.createElement(document, lv4, lv5, "wco:GrossMassMeasure", Util.null2str(tempHblDataList.get(nRowIndex).get("GWT")));
    				/** wco:ConsignmentItem 끝 */
                }
    			if(modType4th.equals("05")){	//정정
    				tempSearchData.clear();
                    tempSearchData.put("odJobNo", Util.null2str(tempHblDataList.get(nRowIndex).get("OD_JOB_NO")));
                    tempSearchData.put("modJobNo", Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_JOB_NO")));
                    shtFinishFlag = false;
                    tempHblSingleList = manifestMapper.getEdiMfModifyHblSingleList(tempSearchData);
                    for(int nSingleIndex = 0; nSingleIndex < tempHblSingleList.size(); ++nSingleIndex) {
                    	if(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_ITEM_CODE_HBL")).equals("DA")){
                    		shtFinishFlag = true;
                    	}
                    	/** wco:AdditionalInformation 시작 */
                    	lv3 = this.createElement(document, lv2, lv3, "wco:AdditionalInformation", null);
                    	/** 단일항목정정항목번호 */
                    	this.createElement(document, lv3, lv4, "wco:Content", Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_ITEM_CODE_HBL")));
                    	/** 정정전 내역 */
                    	this.createElement(document, lv3, lv4, "wco:StatementDescription", ediCommonService.getSubStringK(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_DESC_BEFORE")), 0, 200));
                    	/** 단일항목정정사유코드 */
                    	this.createElement(document, lv3, lv4, "wco:ReconciliationReasonCode", Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_REASON_CODE_HBL")));
                    	/** 정정후 내역 */
                    	this.createElement(document, lv3, lv4, "kcs:AdjustmentDescription", ediCommonService.getSubStringK(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_DESC_AFTER")), 0, 200));
                        /** wco:AdditionalInformation 끝 */
                    	correctionContents.append(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_ITEM_NAME_HBL")) + ":");
        				correctionContents.append(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_DESC_BEFORE")) + "=>");
        				correctionContents.append(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_DESC_AFTER")));
        				if(nSingleIndex != tempHblSingleList.size() + 1){
        					correctionContents.append(", ");
        				}else{
        					correctionContents.append(")");
        				}
                    }
                    if(!shtFinishFlag){
	                	tempHblDataList.get(nRowIndex).put("ABNORMAL_SEQ_NO_ID", null);
	                }
                }
//                if (!Util.null2str(tempHblDataList.get(nRowIndex).get("TEDAO_MOD_HBL.ModiUCRNo")).trim().equals("")){
//                	/** UCR */
//                	lv3 = this.createElement(document, lv2, lv3, "wco:UCR", null);
//                	this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("TEDAO_MOD_HBL.ModiUCRNo")));
//                }
    			/** wco:HouseConsignment 끝 */
    			/** wco:GeneralInformation 끝 */
    			
    			if(correctionContents.length() > 1000){
                	tempHblDataList.get(nRowIndex).put("correctionContents", correctionContents.toString().substring(0, 1000));
                }else{
                	tempHblDataList.get(nRowIndex).put("correctionContents", correctionContents.toString());
                }
                tempHblDataList.get(nRowIndex).put("gmtTime", docSend.get("gmtTime"));
            	manifestMapper.updateMfModifyInfo(tempHblDataList.get(nRowIndex));
            	
            	/** MGN->적하정정삭제 or MFN->적하정정추가 or SHT->적하항목정정(DA-H B/L 포장갯수) 일경우 하기이상 완료여부 Y 업데이트 */
            	if(tempHblDataList.get(nRowIndex).get("ABNORMAL_SEQ_NO_ID") != null) {
            		abnormalMapper.updateAbnormalFinishYn(tempHblDataList.get(nRowIndex));
            	}
            }
            
            lv1 = this.createElement(document, root, lv1, "wco:Submitter", null);
            /** 신청자성명 */
            this.createElement(document, lv1, lv2, "wco:Name", Util.null2str(fwSendAccInfo.get("ENT_BUSINESS_NAME_ENG")));
            /** 신청인 구분구호 */
            this.createElement(document, lv1, lv2, "wco:RoleCode", "03");
            
            out = new ByteArrayOutputStream();
			source = new DOMSource(document);
			streamResult = new StreamResult(out);
			transformer.transform(source, streamResult);
			writeData = new String(out.toByteArray(), StandardCharsets.UTF_8);
            FileUtil.fileWrite(writeData, dirPath0, fileName, "euc-kr");
		} catch(Exception e) {
			throw e;
		} finally {
		}
		
		retData[0] = fileName;	// 파일명
		retData[1] = writeData;	// 파일 내용
		retData[2] = dirPath[0];	// 원본 로컬 경로
		retData[3] = dirPath[1];	// 백업 로컬 경로
		
		retData[4] = "GOVCBR5LI";
		retData[5] = modReqNo;
		
		return retData;
	}
	public String[] getSendDataFileCreate4thKCNET(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempHblDataList, Map<String, Object> fwSendAccInfo, 
			Map<String, Object> ediHostSendInfo) throws java.lang.Exception {
		
		List<Map<String, Object>> tempHblSingleList = null;
		List<Map<String, Object>> tempHblCntrList = null;
		List<Map<String, Object>> tempHblCntrReasonList = null;
		Map<String, Object> tempSearchData = new HashMap<>();
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(docSend, "gmtTime", false, null, null, null, null);
		
		/*주석처리 문서쓰기 변경
		StringBuffer retWriteData = new StringBuffer();	// 파일 용량이 클경우 내용 저장하면 안됨.
		*/
		String modTypeCntr = null;
		String writeData = null;
		String retData[] = new String[6];
		String fileName = new StringBuffer(Util.null2str(docSend.get("registerUserId"))).append(".H").append(Util.null2str(tempMblData.get("MBL_NO")).trim()).append(".txt").toString();	// 전송 파일명
        String ediVender = Util.null2str(docSend.get("ediVender"));
		String dirPath[] = ediCommonService.getSendFilePath(ediVender, "edi/manifestMod", docSend);	// 로컬폴더 경로 가져오기
		
		/*문서쓰기방식 변경하여 추가*/
		String dirPath0 = dirPath[0];
		
		String modReqNo = Util.null2str(tempMblData.get("MOD_DECLARE_NO"));
		String strDeclarerDate = Util.null2str(tempMblData.get("MOD_DECLARE_DATE")).trim().replace("-","").replace(":", "").replace(" ", "");
		boolean shtFinishFlag;
		
		// 파일 내용 쓰기
		try {
			ByteArrayOutputStream out = null;
			DOMSource source = null;
			StreamResult streamResult = null;
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			
			Document document = null;
			Element root = null;
			Element lv1 = null;
			Element lv2 = null;
			Element lv3 = null;
			Element lv4 = null;
			Element lv5 = null;
			Element lv6 = null;
			Map<String, String> attribute = new HashMap<String, String>();
			
			document = documentBuilder.newDocument();
			document.setXmlVersion("1.0");
			document.setXmlStandalone(true);
			
			root = document.createElement("wco:Declaration");
			root.setAttribute("xmlns:kcs", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5LISchemaModule:1:0");
			root.setAttribute("xmlns:wco", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5LISchemaModule:1:1");
			root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			root.setAttribute("xsi:schemaLocation", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_5LISchemaModule:1:0 ./schema4G/kcs/data/standard/KCS_DeclarationOfCAG_5LISchemaModule_1.0_standard.xsd");
			document.appendChild(root);
			
			/** 신고세관/신고과 */
			this.createElement(document, root, lv1, "wco:DeclarationOfficeID", Util.null2str(tempMblData.get("CUSTOMS_CODE")).trim() + Util.null2str(tempMblData.get("CUSTOMS_DPT_CODE")).trim());
			/** 제출번호 */
			this.createElement(document, root, lv1, "wco:ID", modReqNo);
			/** 제출일시 */
			this.createElement(document, root, lv1, "wco:IssueDateTime", strDeclarerDate);
			/** 문서형태구분 */
			this.createElement(document, root, lv1, "wco:TypeCode", "GOVCBR5LI");
			/** 적하목록문서 구분 */
			this.createElement(document, root, lv1, "kcs:SubTypeCode", "40");
			
//			/** wco:Consignment 시작 */
//			lv1 = this.createElement(document, root, lv1, "wco:Consignment", null);
//			/** AWB 번호(M-AWB) */
//			lv2 = this.createElement(document, lv1, lv2, "wco:TransportContractDocument", null);
//			this.createElement(document, lv2, lv3, "wco:ID", Util.null2str(tempMblData.get("MBL_NO")));
			
			String tmpStr = "";
            String modType4th = null;
            StringBuffer correctionContents = new StringBuffer();
            for(int nRowIndex = 0; nRowIndex < tempHblDataList.size(); nRowIndex++) {
            	correctionContents.setLength(0);
            	modType4th = Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_TYPE_4TH"));
            	
            	String oldCorrectionContents = Util.null2str(manifestMapper.selectOrderCorrectionContents(tempHblDataList.get(nRowIndex)));
            	if(!oldCorrectionContents.equals("")){
            		correctionContents.append(oldCorrectionContents + "|");
            	}
            	if(modType4th.equals("05")){
            		correctionContents.append("HBL정정:" + Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_REASON_NAME")) + "(");
            	}else if(modType4th.equals("06")){
            		correctionContents.append("HBL추가:" + Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_REASON_NAME")));
            	}else if(modType4th.equals("07")){
            		correctionContents.append("HBL삭제:" + Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_REASON_NAME")));
            	}
            	
            	/** wco:GeneralInformation 시작 */
    			lv1 = this.createElement(document, root, lv1, "wco:GeneralInformation", null);
    			/** 작업구분 부호 */
    			this.createElement(document, lv1, lv2, "kcs:StatementCode", modType4th.equals("99") ? "05" : modType4th);
    			/** 정정사유 */
    			this.createElement(document, lv1, lv2, "kcs:StatementDescription", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_REASON_NAME")), 0, 100));
    			
    			/** wco:HouseConsignment 시작 */
    			lv2 = this.createElement(document, lv1, lv2, "wco:HouseConsignment", null);
    			if (modType4th.equals("06")){	//추가
    				/** 화물구분 */
    				this.createElement(document, lv2, lv3, "kcs:CargoProcessTypeCode", Util.null2str(tempHblDataList.get(nRowIndex).get("BL_TYPE_IERT")));
    				/** 포장개수 */
    				this.createElement(document, lv2, lv3, "wco:TotalPackageQuantity", Util.null2str(tempHblDataList.get(nRowIndex).get("PIECES")));
    				/** 총용적 */
    				this.createElement(document, lv2, lv3, "wco:GrossVolumeMeasure", Util.null2str(tempHblDataList.get(nRowIndex).get("GWT")));
                }

    			/** wco:AssociatedTransportDocument 시작 */
    			lv3 = this.createElement(document, lv2, lv3, "wco:AssociatedTransportDocument", null);
    			/** House AWB번호 */
    			this.createElement(document, lv3, lv4, "wco:ID", ediCommonService.DeleteCharString_Loop(Util.null2str(tempHblDataList.get(nRowIndex).get("HBL_NO")).trim(), " |-", "|"));
    			/** House B/L 일련번호 */
    			this.createElement(document, lv3, lv4, "kcs:HouseBLSequenceID", Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_HSN")).trim());
    			/** wco:AssociatedTransportDocument 끝 */
    			
    			/** wco:TransportContractDocument 시작 */
    			lv3 = this.createElement(document, lv2, lv3, "wco:TransportContractDocument", null);
    			/** Master AWB번호 */
    			this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(tempMblData.get("MBL_NO")).trim());
    			/** Master B/L 일련번호 */
    			this.createElement(document, lv3, lv4, "kcs:MasterBLSequenceID", Util.null2str(tempMblData.get("EDI_MSN")).trim());
    			/** wco:TransportContractDocument 끝 */
    			
    			if (modType4th.equals("06")){	//추가
    				/** 최종 목적지 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:GoodsReceiptPlace", null);
        			this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("DEST_CODE")).trim());
        			/** 포장종류 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:Packaging", null);
//        			this.createElement(document, lv3, lv4, "wco:TypeCode", Util.null2str(tempHblDataList.get(nRowIndex).get("UNIT_PIECES")).trim());
    				if(Util.null2str(docSend.get("srchBound")).equals("AIR")){
    					this.createElement(document, lv3, lv4, "wco:TypeCode", "CT");
    				}else{
    					this.createElement(document, lv3, lv4, "wco:TypeCode", "GT");
    				}
        			
        			/** wco:Consignor 시작 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:Consignor", null);
    				/** 송하인 상호 또는 성명 */
    				this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_NAME")).trim(), 0, 50));
    				if(!Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_ADDRESS")).trim().equals("")){
    					/** wco:Address 시작 */
        				lv4 = this.createElement(document, lv3, lv4, "wco:Address", null);
        				/** 송하인 국가코드 */
        				this.createElement(document, lv4, lv5, "wco:CountryCode", Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_COUNTRY_CODE")).trim());
//        				if(!Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_COUNTRY_CODE")).trim().equals("") && !Util.null2str(tempHblDataList.get(nRowIndex).get("TEDAI_MOD_HBL.ShipperCity")).trim().equals("")){
//        					this.createElement(document, lv4, lv5, "wco:CountrySubDivisionID", Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_COUNTRY_CODE")).trim() + Util.null2str(tempHblDataList.get(nRowIndex).get("TEDAI_MOD_HBL.ShipperCity")).trim());
//        				}
        				//송하인 국가코드 -> 국가코드+ORIG_CODE
        				this.createElement(document, lv4, lv5, "wco:CountrySubDivisionID", Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_COUNTRY_CODE")).trim() + Util.null2str(tempHblDataList.get(nRowIndex).get("ORIG_CODE")).trim());
        				this.createElement(document, lv4, lv5, "wco:Line", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_ADDRESS")).trim(), 0, 150));
        				/** wco:Address 끝 */
    				}
    				if(!Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_TEL")).trim().equals("")){
    					/** wco:Communication 시작 */
        				lv4 = this.createElement(document, lv3, lv4, "wco:Communication", null);
        				/** 통신수단유형 */
        				this.createElement(document, lv4, lv5, "wco:TypeID", "TE");
        				/** 송하인 전화번호 */
        				this.createElement(document, lv4, lv5, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_TEL")).trim());
        				/** wco:Communication 끝 */
    				}
    				/** wco:Consignor 끝 */
    				
    				/** wco:Consignee 시작 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:Consignee", null);
    				/** 사업자등록번호/생년월일 */
    				attribute.put("attributeName", "schemeAgencyID");
    				tmpStr = Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_BUSINESS_ID"));
    				if(tmpStr.length() == 10){
    					attribute.put("attributeContent", "KTX");
    				}else{
    					attribute.put("attributeContent", "KAS");
    					if(tmpStr.length() == 13){
    						/** 생년월일    주민등록번호 7번째 자리가 1이나 2이면 19+주민번호앞6자리 / 3이나 4이면 20+주민번호앞6자리*/
    						if(Util.null2int(tmpStr.substring(6, 7)) > 2){
    							tmpStr = "20" + tmpStr.substring(0, 6);
    						}else{
    							tmpStr = "19" + tmpStr.substring(0, 6);
    						}
    					}
    				}
    				this.createElement(document, lv3, lv4, "wco:ID", tmpStr, attribute);
    				/** 수하인 상호 또는 성명 */
    				this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_NAME")).trim(), 0, 50));
    				if(!Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_ADDRESS")).trim().equals("")){
    					/** wco:Address 시작 */
        				lv4 = this.createElement(document, lv3, lv4, "wco:Address", null);
        				/** 수하인 국가코드 */
        				this.createElement(document, lv4, lv5, "wco:CountryCode", Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_COUNTRY_CODE")).trim());
//        				if(!Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_COUNTRY_CODE")).trim().equals("") && !Util.null2str(tempHblDataList.get(nRowIndex).get("TEDAI_MOD_HBL.ConsigneeCity")).trim().equals("")){
//        					this.createElement(document, lv4, lv5, "wco:CountrySubDivisionID", Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_COUNTRY_CODE")).trim() + Util.null2str(tempHblDataList.get(nRowIndex).get("TEDAI_MOD_HBL.ConsigneeCity")).trim());
//        				}
        				//수하인 국가코드 -> 국가코드+DEST_CODE
        				this.createElement(document, lv4, lv5, "wco:CountrySubDivisionID", Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_COUNTRY_CODE")).trim() + Util.null2str(tempHblDataList.get(nRowIndex).get("DEST_CODE")).trim());
        				this.createElement(document, lv4, lv5, "wco:Line", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_ADDRESS")).trim(), 0, 150));
        				/** wco:Address 끝 */
    				}
    				if(!Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_TEL")).trim().equals("")){
    					/** wco:Communication 시작 */
        				lv4 = this.createElement(document, lv3, lv4, "wco:Communication", null);
        				/** 통신수단유형 */
        				this.createElement(document, lv4, lv5, "wco:TypeID", "TE");
        				/** 수하인 전화번호 */
        				this.createElement(document, lv4, lv5, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_TEL")).trim());
        				/** wco:Communication 끝 */
    				}
    				/** wco:Consignee 끝 */

    				//220603 장부장님 : 통지처 ID(""), Name("SAME AS CONSIGNEE") 추가
    				/** wco:NotifyParty 시작 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:NotifyParty", null);
        			/** 사업자등록번호 */
        			this.createElement(document, lv3, lv4, "wco:ID", "");
        			/** 통지처 상호 또는 성명 */
        			this.createElement(document, lv3, lv4, "wco:Name", "SAME AS CONSIGNEE");
        			/** wco:NotifyParty 끝 */
    				
    				/** wco:ConsignmentItem 시작 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:ConsignmentItem", null);
    				
    				/** wco:Commodity 시작 */
    				lv4 = this.createElement(document, lv3, lv4, "wco:Commodity", null);
    				/** 품명 */
    				this.createElement(document, lv4, lv5, "wco:CargoDescription", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("COMMODITY_ITEM_DESC")).trim(), 0, 200));
    				/** 하기물품구분코드 */
    				this.createElement(document, lv4, lv5, "wco:CategoryCode", Util.null2str(tempHblDataList.get(nRowIndex).get("DISC_ITEM_CODE")));
    				
    				/** wco:Classification 시작 */
    				lv5 = this.createElement(document, lv4, lv5, "wco:Classification", null);
    				/** HSCode */
    				this.createElement(document, lv5, lv6, "wco:ID", "");
    				//특수화물코드(필수항목이라 'NO'로 함)[동물검역,식물검역,보온/보냉물품]
    				this.createElement(document, lv5, lv6, "wco:IdentificationTypeCode", "NO");
    				this.createElement(document, lv5, lv6, "wco:IdentificationTypeCode", "NO");
    				this.createElement(document, lv5, lv6, "wco:IdentificationTypeCode", "NO");
    				/** wco:Classification 끝 */
    				/** wco:Commodity 끝 */
    				
    				
    				/** 중량 */
    				lv4 = this.createElement(document, lv3, lv4, "wco:GoodsMeasure", null);
    				this.createElement(document, lv4, lv5, "wco:GrossMassMeasure", Util.null2str(tempHblDataList.get(nRowIndex).get("GWT")));
    				/** wco:ConsignmentItem 끝 */
    				
        			/** 전자상거래 여부 **/
        			if ("Y".equals(Util.null2str(tempHblDataList.get(nRowIndex).get("ECM_YN")))) {
        				lv3 = this.createElement(document, lv2, lv3, "wco:ElectronicCommerceTypeCode", null);
        				this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("ECM_YN")));
        			}
                }
    			if(modType4th.equals("05")){	//정정
    				tempSearchData.clear();
                    tempSearchData.put("odJobNo", Util.null2str(tempHblDataList.get(nRowIndex).get("OD_JOB_NO")));
                    tempSearchData.put("modJobNo", Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_JOB_NO")));
                    shtFinishFlag = false;
                    tempHblSingleList = manifestMapper.getEdiMfModifyHblSingleList(tempSearchData);
                    for(int nSingleIndex = 0; nSingleIndex < tempHblSingleList.size(); ++nSingleIndex) {
                    	if(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_ITEM_CODE_HBL")).equals("DA")){
                    		shtFinishFlag = true;
                    	}
                    	/** wco:AdditionalInformation 시작 */
                    	lv3 = this.createElement(document, lv2, lv3, "wco:AdditionalInformation", null);
                    	/** 단일항목정정항목번호 */
                    	this.createElement(document, lv3, lv4, "wco:Content", Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_ITEM_CODE_HBL")));
                    	/** 정정전 내역 */
                    	this.createElement(document, lv3, lv4, "wco:StatementDescription", ediCommonService.getSubStringK(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_DESC_BEFORE")), 0, 200));
                    	/** 단일항목정정사유코드 */
                    	this.createElement(document, lv3, lv4, "wco:ReconciliationReasonCode", Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_REASON_CODE_HBL")));
                    	/** 정정후 내역 */
                    	this.createElement(document, lv3, lv4, "kcs:AdjustmentDescription", ediCommonService.getSubStringK(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_DESC_AFTER")), 0, 200));
                        /** wco:AdditionalInformation 끝 */
                    	correctionContents.append(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_ITEM_NAME_HBL")) + ":");
        				correctionContents.append(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_DESC_BEFORE")) + "=>");
        				correctionContents.append(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_DESC_AFTER")));
        				if(nSingleIndex != tempHblSingleList.size() + 1){
        					correctionContents.append(", ");
        				}else{
        					correctionContents.append(")");
        				}
                    }
                    if(!shtFinishFlag){
	                	tempHblDataList.get(nRowIndex).put("ABNORMAL_SEQ_NO_ID", null);
	                }
                }
    			
//                if (!Util.null2str(tempHblDataList.get(nRowIndex).get("TEDAO_MOD_HBL.ModiUCRNo")).trim().equals("")){
//                	/** UCR */
//                	lv3 = this.createElement(document, lv2, lv3, "wco:UCR", null);
//                	this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("TEDAO_MOD_HBL.ModiUCRNo")));
//                }
    			if (!Util.null2str(tempHblDataList.get(nRowIndex).get("DISC_WH_CODE_ETC1")).equals("")){
                	/** 하선하기보세구역부호 */
                	lv3 = this.createElement(document, lv2, lv3, "wco:UnloadingLocation", null);
                	this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("DISC_WH_CODE_ETC1")));
    			}
    			
    			/** wco:HouseConsignment 끝 */
    			/** wco:GeneralInformation 끝 */
    			
    			if(correctionContents.length() > 1000){
                	tempHblDataList.get(nRowIndex).put("correctionContents", correctionContents.toString().substring(0, 1000));
                }else{
                	tempHblDataList.get(nRowIndex).put("correctionContents", correctionContents.toString());
                }
                tempHblDataList.get(nRowIndex).put("gmtTime", docSend.get("gmtTime"));
            	manifestMapper.updateMfModifyInfo(tempHblDataList.get(nRowIndex));
            	
            	/** MGN->적하정정삭제 or MFN->적하정정추가 or SHT->적하항목정정(DA-H B/L 포장갯수) 일경우 하기이상 완료여부 Y 업데이트 */
            	if(tempHblDataList.get(nRowIndex).get("ABNORMAL_SEQ_NO_ID") != null) {
            		abnormalMapper.updateAbnormalFinishYn(tempHblDataList.get(nRowIndex));
            	}
            }
            
            lv1 = this.createElement(document, root, lv1, "wco:Submitter", null);
            /** 신청자성명 */
            this.createElement(document, lv1, lv2, "wco:Name", Util.null2str(fwSendAccInfo.get("ENT_BUSINESS_NAME_ENG")));
            /** 신청인 구분구호 */
            this.createElement(document, lv1, lv2, "wco:RoleCode", "03");
            
            out = new ByteArrayOutputStream();
			source = new DOMSource(document);
			streamResult = new StreamResult(out);
			transformer.transform(source, streamResult);
			writeData = new String(out.toByteArray(), StandardCharsets.UTF_8);

			StringBuffer soapData = new StringBuffer();
			soapData.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			soapData.append("<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
			soapData.append("<soapenv:Header xmlns:kcn=\"http://www.kcnet.co.kr/kcn\" mustUnderstand=\"0\">");
			soapData.append("<kcn:Kcnet_Header mustUnderstand=\"0\">");
			soapData.append("<kcn:SenderId mustUnderstand=\"0\">").append(Util.getFileWriteDataWithRtrim(ediCommonService.getDeleteCharStringFwb(Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).trim()), "String", "4")).append("</kcn:SenderId>");
			soapData.append("<kcn:ReceiverId mustUnderstand=\"0\">KCNECP</kcn:ReceiverId>");
			soapData.append("<kcn:DocName mustUnderstand=\"0\">5LI</kcn:DocName>");
			soapData.append("<kcn:DocCode mustUnderstand=\"0\">5LI</kcn:DocCode>");
			soapData.append("<kcn:MessageId mustUnderstand=\"0\">").append(Util.null2str(tempMblData.get("MBL_NO"))).append("</kcn:MessageId>");
			soapData.append("<kcn:VenderId mustUnderstand=\"0\">YHT</kcn:VenderId>");
			soapData.append("</kcn:Kcnet_Header>");
			soapData.append("<kcn:ID mustUnderstand=\"0\">").append(Util.null2str(ediHostSendInfo.get("IF_WEB_ID"))).append("</kcn:ID>");
			soapData.append("<kcn:Password mustUnderstand=\"0\">").append(Util.null2str(ediHostSendInfo.get("IF_WEB_PWD"))).append("</kcn:Password>");
			soapData.append("</soapenv:Header>");
			soapData.append("<soapenv:Body>");
			
			writeData = soapData.toString() + writeData.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "") + "</soapenv:Body></soapenv:Envelope>";
			
            FileUtil.fileWrite(writeData, dirPath0, fileName, "euc-kr");
		} catch(Exception e) {
			throw e;
		} finally {
		}
		
		retData[0] = fileName;	// 파일명
		retData[1] = writeData;	// 파일 내용
		retData[2] = dirPath[0];	// 원본 로컬 경로
		retData[3] = dirPath[1];	// 백업 로컬 경로
		
		retData[4] = "GOVCBR5LI";
		retData[5] = modReqNo;
		
		return retData;
	}
	
	/**
	 * 오더 EDI 상태값 업데이트
	 * 
	 * @param docSend
	 * @param tempMblData
	 * @throws Exception
	 */
	public void setEdiModifyMblAfterSendUpdate(Map<String, Object> docSend, Map<String, Object> tempMblData) throws Exception {
		Map<String, Object> tempInsertOrUpdateData = new HashMap<String, Object>();
		
		tempInsertOrUpdateData.put("gmtTime", Util.null2str(docSend.get("gmtTime")));
		tempInsertOrUpdateData.put("modJobNo", Util.null2str(tempMblData.get("MOD_JOB_NO")));
		
//		송신시 'FAIL' or  'SEND'로 업데이트한다.
		if("Y".equals(Util.null2str(docSend.get("fileSendYn")))) {
//			tempInsertOrUpdateData.put("docSendType", "SEND");
			tempInsertOrUpdateData.put("docSendType", "Y");
		} else {
//			tempInsertOrUpdateData.put("docSendType", "FAIL");
			tempInsertOrUpdateData.put("docSendType", "E");
		}
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(tempInsertOrUpdateData, "gmtTime", false, null, null, null, null);
		manifestMapper.setModifyMblAfterSendUpdate(tempInsertOrUpdateData);
	}
	
	public Element createElement(Document document, Element parent, Element ele, String eleName, String eleContent) throws Exception {
		return createElement(document, parent, ele, eleName, eleContent, null);
	}
	
	public Element createElement(Document document, Element parent, Element ele, String eleName, String eleContent, Map<String, String> attribute) throws Exception {
		ele = document.createElement(eleName);
		if(eleContent != null){
			ele.setTextContent(eleContent);
		}
		ele.setTextContent(eleContent);
		if(attribute != null){
			ele.setAttribute(Util.null2str(attribute.get("attributeName")), Util.null2str(attribute.get("attributeContent")));
		}
		parent.appendChild(ele);
		return ele;
	}
}
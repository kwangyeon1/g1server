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
import org.yht.exps.express.mapper.edi.ManifestMapper;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.edi.ManifestCorrect;
import org.yht.exps.express.model.edi.ManifestCorrectMaster;
import org.yht.exps.express.model.edi.SendManifest;
import org.yht.exps.express.model.edi.search.SearchManifestCorrectList;
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
public class EdiMfModifyExpSendService {
		
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
	 * 수출 적하목록정정신고 문서송신(4세대)
	 * @param sendManifest
	 * @return
	 * @throws java.lang.Exception
	 */
	public OrderSaveResult sendManifestModifyExp4th(SendManifest sendManifest, HttpServletRequest request) throws java.lang.Exception {
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
			ifServiceCode = ediCommonService.getServiceCode(Util.null2str(docSend.get("solution")), Util.null2str(docSend.get("bound")), Util.null2str(docSend.get("boundType")), Util.null2str(docSend.get("docType")), Util.null2str(tempMblData.get("EDI_VENDER")));
			docSend.put("ifServiceCode", ifServiceCode);
			docSend.put("ediVender", Util.null2str(tempMblData.get("EDI_VENDER")).trim());
			docSend.put("mblJobNo", Util.null2str(tempMblData.get("MOD_JOB_NO")).trim());
			docSend.put("mblNo", Util.null2str(tempMblData.get("MBL_NO")).trim());
			
			// 사업장정보 가져오기( 포워더 송/수신 계정정보, TB_SMS_ENT_BUS_FMS )
			fwSendAccInfo = manifestMapper.getFwSendAccInfo(docSend);
			
			if(!this.setMfModifyAirSeaExpSend4th(docSend, tempMblData, fwSendAccInfo, errList, result, request)) {
				if(result.get("processCode") == null || "".equals(Util.null2str(result.get("processCode")))) {
					result.put("processCode", "A00001");
				}
			}
			processMessage.append("<br>");
			processMessage.append(Util.null2str(tempMblData.get("MBL_NO")));
		} else {
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
	public boolean setMfModifyAirSeaExpSend4th(Map<String, Object> docSend, Map<String, Object> tempMblData, Map<String, Object> fwSendAccInfo, List<Map<String, Object>> errList, Map<String, Object> result, HttpServletRequest request) throws java.lang.Exception {
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
		if(!this.getMfModifyAirSeaExpSend4th(docSend, tempMblData, tempHblDataList, fwSendAccInfo, ediHostSendInfo, errList, result, request)) {
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
	public boolean getMfModifyAirSeaExpSend4th(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempHblDataList, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo, List<Map<String, Object>> errList, Map<String, Object> result, HttpServletRequest request) throws java.lang.Exception {
		
//		tempMblData		// 정정신고 전송 오류검증 M.B/L 오더정보 조회
//		tempHblDataList	// 정정신고 전송 오류검증 H.B/L 오더정보 조회
//		fwSendAccInfo		// 포워더 송/수신 계정정보
//		ediHostSendInfo	// 중계사업자 송신 정보
		String fileCreateRetVal[] = null;									// 파일 생성후 결과값, [0]:파일명, [1]:로컬파일경로, [2]:파일내용
		String fileSendYn = "N";	// 파일 전송 여부( 'Y':전송, 'N':미전송 )
		
		//////////////////////////////////////////
		// 오류 검증
		//////////////////////////////////////////
		if(!this.getDoAirSeaExpSendDataValidate4th(docSend, tempMblData, tempHblDataList, fwSendAccInfo, ediHostSendInfo, errList, result)) {
			return false;
		}
		
		//////////////////////////////////////////
		// 파일 생성
		//////////////////////////////////////////
		if(Util.null2str(docSend.get("ediVender")).equals("KCNET")){
			fileCreateRetVal = this.getSendDataFileCreate4thKCNET(docSend, tempMblData, tempHblDataList, fwSendAccInfo, ediHostSendInfo);
			if ("Y".equals(customProperties.getEdiSendYn())) {
				try {
					
					log.info("getMfModifyAirSeaExpSend4th HttpClient start "+ docSend.get("entCode"));
					
					//테스트 http://121.138.113.7:12101 리얼 http://222.122.233.22:12101
					String httpPost = Util.null2str(ediHostSendInfo.get("IF_SVR_IP")) + ":" + Util.null2str(ediHostSendInfo.get("IF_SVR_IP_PORT")) + "/WebServices/services/XmlDataService";
	//				HttpPost blPost = new HttpPost(httpPost);
	//				blPost.setHeader("Content-type", "text/xml;charset=utf-8");
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
			        
			        log.info("getMfModifyAirSeaExpSend4th HttpClient end "+ docSend.get("entCode") + " responseCode :" + responseCode);
			        
				}catch(Exception e) {
					throw e;
				}
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
     * 수출 정정신고 전송내역 오류검증 '''@@@
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
	public boolean getDoAirSeaExpSendDataValidate4th(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempHblDataList, Map<String, Object> fwSendAccInfo, Map<String, Object> ediHostSendInfo, List<Map<String, Object>> errList, Map<String, Object> result) throws java.lang.Exception {
		
//		tempMblBaseData	// M.B/L 오더정보 조회
//		tempMblData		// 오류검증 M.B/L 오더정보 조회
//		tempHblDataList	// 오류검증 H.B/L 오더정보 조회
//		fwSendAccInfo		// 포워더 송/수신 계정정보
		
		Map<String, Object> tempUpdateData = null;
		Map<String, Object> tempSearchMapData = new HashMap<>();
		
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
        String boundStr = Util.null2str(docSend.get("bound")).trim(); // 중복 변수 지정 - ditobass!!@
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

//        if(Util.null2str(tempMblData.get("EDI_MSN")).trim().length() != 4) {
//        	tempSearchData.put("srchCode", "");
//			tempSearchData.put("message", "MSN번호가 기재되지 않았거나 4자 미만입니다.");
//			message = ediCommonService.getErrorCodeName(tempSearchData);
// 
//			errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
//        }

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
        String odJobNo = null;
        String modJobNo = null;
        String modType4thStr = null; //중복 변수 지정 - ditobass!!@
		
		// H.B/L 건수 체크
		if(tempHblDataList.size() <= 0) {
			tempSearchData.put("srchCode", "MF-43");	// 해당 M.B/L에 H.B/L이 한 건도 없습니다.
        	message = ediCommonService.getErrorCodeName(tempSearchData);
 
        	errList.add(ediCommonService.getErrorMessage(mblNo, "M.B/L : " + message));
		}
		
		for(int i = 0; i < tempHblDataList.size(); ++i) {
			
//          "House 검증"
			hblNo = Util.null2str(tempHblDataList.get(i).get("HBL_NO")).trim();
			odJobNo = Util.null2str(tempHblDataList.get(i).get("OD_JOB_NO")).trim();
			modJobNo = Util.null2str(tempHblDataList.get(i).get("MOD_JOB_NO")).trim();
            modType4thStr = Util.null2str(tempHblDataList.get(i).get("MOD_TYPE_4TH")).trim(); //중복 변수 지정 - ditobass!!@
			
            //하우스 기본정보 체크    
            if(Util.null2str(tempHblDataList.get(i).get("EDI_HSN")).trim().length() != 4) {
            	tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "HSN번호를 기재되지 않았거나 4자 미만입니다.");
				message = ediCommonService.getErrorCodeName(tempSearchData);
	 
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
            }

            if(Util.null2str(tempHblDataList.get(i).get("HBL_NO")).trim().length() == 0) {
            	tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "House BL번호를 기재 바랍니다");
				message = ediCommonService.getErrorCodeName(tempSearchData);
	 
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
            } else {
                if(!ediCommonService.getIsAlphaNumericCheck(Util.null2str(tempHblDataList.get(i).get("HBL_NO")).trim())) {
                	tempSearchData.put("srchCode", "");
    				tempSearchData.put("message", "House BL번호에 특수문자가 포함되어있습니다.");
    				message = ediCommonService.getErrorCodeName(tempSearchData);
    	 
    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                }
            }
            
//          "정정구분 체크"
            if(modType4thStr.length() == 0) {
            	tempSearchData.put("srchCode", "");
				tempSearchData.put("message", "의 정정구분를 기재 바랍니다");
				message = ediCommonService.getErrorCodeName(tempSearchData);
	 
				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
            }
            
//          "정정구분이 05일 경우(House 정정)"
            else if ("05".equals(modType4thStr)) {	//2021-09-02 항목정정만가능 이외내용 제거함
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
//                		if(Util.null2str(tempHblSingleList.get(nRowCount).get("MOD_DESC_BEFORE")).trim().length() == 0) {
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
//                        }
                		if(Util.null2str(tempHblSingleList.get(nRowCount).get("MOD_DESC_BEFORE")).trim().length() == 0 &&
                        	Util.null2str(tempHblSingleList.get(nRowCount).get("MOD_DESC_AFTER")).trim().length() == 0) {
                        	
                        	tempSearchData.put("srchCode", "");
                        	tempSearchData.put("message", "Single(" + Util.null2str(tempHblSingleList.get(nRowCount).get("MOD_ITEM_CODE_HBL")) + ")의 정정전 내역 또는 정정후 내역을 기재 바랍니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                        }
                	}
                }

            }else if("99".equals(modType4thStr)){
            	//container 부분 체크
                if("SEA".equals(boundStr) && "EXP".equals(boundTypeStr)) {
                	tempHblDataList.get(i).put("containerList", this.getModifyVerifyInContainer4th(odJobNo, modType4thStr, hblNo, boundStr, errList));
                }
            }else if ("06".equals(modType4thStr)) {	//House 추가
            	if(Util.null2str(tempHblDataList.get(i).get("MOD_REASON_NAME")).trim().length() == 0) {
                	tempSearchData.put("srchCode", "");
    				tempSearchData.put("message", "의 정정구분이 추가이면 정정사유를 기재 바랍니다.");
    				message = ediCommonService.getErrorCodeName(tempSearchData);
    	 
    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                }
            	
            	if(Util.null2str(tempHblDataList.get(i).get("BL_TYPE_IERT")).trim().length() == 0) {
            		tempSearchData.put("srchCode", "");
    				tempSearchData.put("message", "의 정정구분이 추가이면 화물구분을 기재 바랍니다.");
    				message = ediCommonService.getErrorCodeName(tempSearchData);
    	 
    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                }

                //Bulk 화물인경우 수량단위는 V로시적한다. 벌크화물인경우는 수량은 없기때문에 수량체크는 하지않는다.
            	if("SEA".equals(boundStr) && "EXP".equals(boundTypeStr)) {
	                if(Util.null2int(tempHblDataList.get(i).get("PIECES")) == 0 && !"V".equals(ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(i).get("UNIT_PIECES")).trim(), 0, 1))) {
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

                if(Util.null2str(tempHblDataList.get(i).get("COMMODITY_ITEM_DESC")).trim().length() == 0) {
                	tempSearchData.put("srchCode", "");
    				tempSearchData.put("message", "의 정정구분이 추가이면 품명을 기재 바랍니다.");
    				message = ediCommonService.getErrorCodeName(tempSearchData);
    	 
    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                }
               
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

                if(Util.null2str(tempHblDataList.get(i).get("SHIPPER_ADDRESS")).trim().length() == 0) {
                	tempSearchData.put("srchCode", "");
    				tempSearchData.put("message", "H.B/L 의 정정구분이 추가이면 Shipper 주소를 기재 바랍니다.");
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
                
                if(Util.null2str(tempHblDataList.get(i).get("CONSIGNEE_ADDRESS")).trim().length() == 0) {
                	tempSearchData.put("srchCode", "");
    				tempSearchData.put("message", "의 정정구분이 추가이면 Consignee 주소를 기재 바랍니다.");
    				message = ediCommonService.getErrorCodeName(tempSearchData);
    	 
    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                }

                if("SEA".equals(boundStr) && "EXP".equals(boundTypeStr)) {
                	if(new BigDecimal(Util.null2str(tempHblDataList.get(i).get("CBM"), "0")).doubleValue() == 0) {
                		tempSearchData.put("srchCode", "");
        				tempSearchData.put("message", "의 정정구분이 추가이면 용적을 기재 바랍니다.");
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

                //License 부분 체크
                this.getModifyVerifyLicense4th(odJobNo, modType4thStr, hblNo, boundStr, Util.null2str(tempHblDataList.get(i).get("BL_TYPE_IERT")).trim(), errList);

                //container 부분 체크
                if("SEA".equals(boundStr) && "EXP".equals(boundTypeStr)) {
	   				tempHblDataList.get(i).put("containerList", this.getModifyVerifyInContainer4th(odJobNo, modType4thStr, hblNo, boundStr, errList));
                }
            }

//          "정정구분이 07일 경우(House 삭제)"
            else if("07".equals(modType4thStr)) {
            	if(Util.null2str(tempHblDataList.get(i).get("MOD_REASON_NAME")).trim().length() == 0) {
            		tempSearchData.put("srchCode", "");
    				tempSearchData.put("message", "의 정정구분이 삭제이면 정정사유를 기재하기 바랍니다.");
    				message = ediCommonService.getErrorCodeName(tempSearchData);
    	 
    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
            	}
            }
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
	 * @param modJobNo
	 * @param modTypeHbl
	 * @param hblNo
	 * @param bound
	 * @param errList
	 * @throws Exception
	 */
	public List<Map<String, Object>> getModifyVerifyInContainer4th(String jobNo, String modTypeHbl, String hblNo, String bound, List<Map<String, Object>> errList) throws Exception {
        
		List<Map<String, Object>> tempHblCntrList = null;
		Map<String, Object> tempSearchMapData = new HashMap<String, Object>();
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		
		String strCntrNo = null;
		String modTypeCntrStr = null; //중복 변수 지정 - ditobass!!@
		String message = null;
		
		tempSearchMapData.put("odJobNo", Util.null2str(jobNo).trim());
		tempSearchMapData.put("modType", Util.null2str(modTypeHbl).trim());
        tempHblCntrList = manifestMapper.ediManifestContainerList(tempSearchMapData);	//ediMfModifyAirSeaSendDao.getEdiMfModifyHblCtnrList(tempSearchMapData);
        
        if(tempHblCntrList != null && tempHblCntrList.size() > 0) {
            for(int nRowCount = 0; nRowCount < tempHblCntrList.size(); ++nRowCount) {
                strCntrNo  = Util.null2str(tempHblCntrList.get(nRowCount).get("CNTR_NO")).trim();
                modTypeCntrStr = Util.null2str(tempHblCntrList.get(nRowCount).get("MOD_TYPE_CNTR")).trim(); //중복 변수 지정 - ditobass!!@
                
                if(Util.null2str(tempHblCntrList.get(nRowCount).get("CNTR_NO")).trim().length() == 0) {
                	tempSearchData.put("srchCode", "");
    				tempSearchData.put("message", "Container의 컨테이너번호을 기재 바랍니다.");
    				message = ediCommonService.getErrorCodeName(tempSearchData);
    	 
    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
    				
//                } else if ("IN BULK".equalsIgnoreCase(Util.null2str(tempHblCntrList.get(nRowCount).get("CNTR_NO")).trim())) {
//                    if (Util.null2str(tempHblCntrList.get(nRowCount).get("CNTR_SEAL_NO_1")).trim().length() == 0) {
//                    	tempSearchData.put("srchCode", "");
//        				tempSearchData.put("message", "[IN BULK] 건 이라도 Container의 봉인번호는 기재 바랍니다. 봉인번호가 없으면 봉인번호에 NO SEAL 기재");
//        				message = ediCommonService.getErrorCodeName(tempSearchData);
//        	 
//        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
//                    }
                }

//              "House 정정구분이 C:정정일경우 체크"
                if("99".equals(modTypeHbl)) {
                    if (modTypeCntrStr.length() == 0) {
                    	tempSearchData.put("srchCode", "");
        				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] Container의 정정구분를 기재 바랍니다.");
        				message = ediCommonService.getErrorCodeName(tempSearchData);
        	 
        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    }
                    //Container의 정정구분이 I:B/L추가 일경우 체크
//                    else if("I".equals(modTypeCntrStr)) {
//                    	tempSearchData.put("srchCode", "");
//        				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] 의 정정구분이 C이면 정정구분은 A,U,D,X만 기재 바랍니다.");
//        				message = ediCommonService.getErrorCodeName(tempSearchData);
//        	 
//        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
//                    }
                    //Container의 정정구분이 A:추가일경우 체크
                    else if("A".equals(modTypeCntrStr)) {
                    	if(Util.null2str(tempHblCntrList.get(nRowCount).get("MOD_REASON_NAME")).trim().length() == 0) {
                    		tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "Container의 정정사유를 기재 바랍니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    	}

                        if(Util.null2str(tempHblCntrList.get(nRowCount).get("CNTR_SEAL_NO_1")).trim().length() == 0) {
                        	tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + " Container의 봉인번호를 기재 바랍니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                        }
                    }else if("U".equals(modTypeCntrStr)) {	//Container의 정정구분이 U:정정 일경우 체크
                    	if(Util.null2str(tempHblCntrList.get(nRowCount).get("MOD_REASON_NAME")).trim().length() == 0) {
                    		tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + " 의 정정사유를 기재 바랍니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    	}
                    	
                    	if(Util.null2str(tempHblCntrList.get(nRowCount).get("BEFORE_VALUE")).trim().length() == 0) {
                    		tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + " 의 정정전 내용을 기재 바랍니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    	}
                    	
                    	if(Util.null2str(tempHblCntrList.get(nRowCount).get("AFTER_VALUE")).trim().length() == 0) {
                    		tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + " 의 정정후 내용을 기재 바랍니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    	}
                    }else if("D".equals(modTypeCntrStr)) {	//Container의 정정구분이 D:삭제 일경우 체크
                    	if(Util.null2str(tempHblCntrList.get(nRowCount).get("MOD_REASON_NAME")).trim().length() == 0) {
                    		tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + " 의 정정사유를 기재 바랍니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    	}
                    }
//                    else if("X".equals(modTypeCntrStr)) {	//Container의 정정구분이 X:신고번호변경 일경우 체크
//                    	if(Util.null2str(tempHblCntrList.get(nRowCount).get("MOD_REASON_NAME")).trim().length() == 0) {
//                    		tempSearchData.put("srchCode", "");
//            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + " 의 정정사유를 기재 바랍니다.");
//            				message = ediCommonService.getErrorCodeName(tempSearchData);
//            	 
//            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
//                    	}
//                        if(Util.null2str(tempHblCntrList.get(nRowCount).get("BEFORE_VALUE")).trim().length() != 0) {
//                        	tempSearchData.put("srchCode", "");
//            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + " 의 정정구분이 컨테이너번호정정일경우 정정전번호를 기재 바랍니다.");
//            				message = ediCommonService.getErrorCodeName(tempSearchData);
//            	 
//            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
//                        }
//                    }
                }else if("06".equalsIgnoreCase(modTypeHbl)) {	//"House 정정구분이 E:추가일경우 체크"
                	if(modTypeCntrStr.length() == 0) {
                		tempSearchData.put("srchCode", "");
        				tempSearchData.put("message", "[Cntr No:" + strCntrNo + " 의 정정구분를 기재 바랍니다.");
        				message = ediCommonService.getErrorCodeName(tempSearchData);
        	 
        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    }
                    ////Container의 정정구분이 I:B/L추가 일경우 체크
                    else if("I".equals(modTypeCntrStr)) {
                        if(Util.null2str(tempHblCntrList.get(nRowCount).get("MOD_REASON_NAME")).trim().length() != 0) {
                        	tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + " 의 정정구분이 I이면 정정사유를 기재하지 않습니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                        }

                        if(Util.null2str(tempHblCntrList.get(nRowCount).get("BEFORE_VALUE")).trim().length() != 0) {
                        	tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "[Cntr No:" + strCntrNo + " 의 정정구분이 I이면 정정전번호는 기재하지 않습니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                        }
                    }
                    //Container의 정정구분이 A:추가 일경우 체크
                    else if("A".equals(modTypeCntrStr)) {
                    	tempSearchData.put("srchCode", "");
        				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] House 정정구분이 E이면 Container의 정정구분은 I만 기재 바랍니다.");
        				message = ediCommonService.getErrorCodeName(tempSearchData);
        	 
        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    }
                    //Container의 정정구분이 U:정정 일경우 체크
                    else if("U".equals(modTypeCntrStr)) {
                    	tempSearchData.put("srchCode", "");
        				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] House 정정구분이 E이면 Container의 정정구분은 I만 기재 바랍니다.");
        				message = ediCommonService.getErrorCodeName(tempSearchData);
        	 
        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    }
                    //Container의 정정구분이 D:삭제 일경우 체크
                    else if("D".equals(modTypeCntrStr)) {
                    	tempSearchData.put("srchCode", "");
        				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] House 정정구분이 E이면 Container의 정정구분은 I만 기재 바랍니다.");
        				message = ediCommonService.getErrorCodeName(tempSearchData);
        	 
        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    }
                    //Container의 정정구분이 X:신고번호변경 일경우 체크
                    else if("X".equals(modTypeCntrStr)) {
                    	tempSearchData.put("srchCode", "");
        				tempSearchData.put("message", "[Cntr No:" + strCntrNo + "] House 정정구분이 C이면 Container의 정정구분은 I만 기재 바랍니다.");
        				message = ediCommonService.getErrorCodeName(tempSearchData);
        	 
        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    }
                }
            }	// end of for-loop.
        }	// end of if.
        
        return tempHblCntrList;
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
	public String[] getSendDataFileCreate4th(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempHblDataList, Map<String, Object> fwSendAccInfo, 
			Map<String, Object> ediHostSendInfo) throws java.lang.Exception {
		
		List<Map<String, Object>> tempHblCntrList = null;
		List<Map<String, Object>> tempHblSingleList = null;
		List<Map<String, Object>> tempHblLicenseList = null;
		
		Map<String, Object> tempSearchData = new HashMap<>();
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(docSend, "gmtTime", false, null, null, null, null);
		
		/*주석처리 문서쓰기 변경
		StringBuffer retWriteData = new StringBuffer();	// 파일 용량이 클경우 내용 저장하면 안됨.
		*/
		StringBuffer writeData = null;
		String retData[] = new String[6];
		String fileName = new StringBuffer(Util.null2str(docSend.get("registerUserId"))).append(".H").append(Util.null2str(tempMblData.get("MBL_NO")).trim()).append(".txt").toString();	// 전송 파일명
        String ediVender = Util.null2str(docSend.get("ediVender"));
		String dirPath[] = ediCommonService.getSendFilePath(ediVender, "edi/manifestMod", docSend);	// 로컬폴더 경로 가져오기
		
		/*문서쓰기방식 변경하여 추가*/
		String dirPath0 = dirPath[0];
		String docCode = null;
		
		String strDeclarerDate		= Util.null2str(tempMblData.get("MOD_DECLARE_DATE")).trim().replace("-","").replace(":", "");
        String strCustoms			= Util.null2str(tempMblData.get("CUSTOMS_CODE")).trim();
        String modReqNo = Util.null2str(tempMblData.get("MOD_DECLARE_NO"));
        String strCustomsDepart	= Util.null2str(tempMblData.get("CUSTOMS_DPT_CODE")).trim();
        
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
            String houseBlTypeIert = null;

            // 루프 밖 반복 변수 지정 - ditobass!!@
            String modReasonNameStr = null, modReasonNameStr50to100 = null;
            String commodityItemDesc = null, commodityItemDesc50to100 = null, commodityItemDesc100to150 = null, commodityItemDesc150to200 = null;
            String shipperNameStr = null, shipperNameStr0to35 = null, shipperNameStr35to70 = null, shipperNameStr70to100 = null;
            String shipperAddressStr = null, shipperAddressStr0to35 = null, shipperAddressStr35to70 = null, shipperAddressStr70to105 = null, shipperAddressStr105to140 = null, shipperAddressStr140to150 = null;
            String shipperCountryCodeStr = null;
            String shipperPlaceCodeStr = null;
            String consigneeNameStr = null, consigneeNameStr0to35 = null, consigneeNameStr35to70 = null, consigneeNameStr70to100 = null;
            String consigneeAddressStr = null, consigneeAddressStr0to35 = null, consigneeAddressStr35to70 = null, consigneeAddressStr70to105 = null, consigneeAddressStr105to140 = null, consigneeAddressStr140to150 = null;
            String consigneeCountryCodeStr = null;
            String consigneePlaceCodeStr = null;
            String licenseModReasonNameStr = null, licenseModReasonNameStr0to50 = null, licenseModReasonNameStr50to100 = null;
            String modDescAfterStr = null, modDescAfterStr0to50 = null, modDescAfterStr50to100 = null, modDescAfterStr100to150 = null, modDescAfterStr150to200 = null;
            String containerModReasonNameStr = null, containerModReasonNameStr0to50 = null, containerModReasonNameStr50to100 = null;
            String cntrSealNo1Str = null, cntrSealNo1Str0to10 = null, cntrSealNo1Str10to20 = null;
            String cntrSealNo2Str = null, cntrSealNo2Str0to10 = null, cntrSealNo2Str10to20 = null;
            String modReasonCodeHblStr = null, modReasonCodeHblStr0to50 = null, modReasonCodeHblStr50to100 = null;
            String singleModReasonNameStr = null, singleModReasonNameStr0to50 = null, singleModReasonNameStr50to100 = null;
            
            //사전적하목록의 경우 수신식별자가 변경된다. 항공수입 : KCS0004AX, 해운수입 :  KCS0004SX
            if("AIR".equals(boundStr)) {
            	if("IMP".equals(boundTypeStr)) {
            		strAirSeaReceiveId = Util.null2str(tempMblData.get("AIR_IMPORT_RECEIVE_ID")).trim();
            	} else {
            		//strAirSeaReceiveId = Util.null2str(tempMblData.get("AIR_EXPORT_RECEIVE_ID")).trim();
            		strAirSeaReceiveId = "KCS4G002";
            	}
            } else if("SEA".equals(boundStr)) {
            	if("KTNET".equals(ediVender)){
            		if("IMP".equals(boundTypeStr)) {
            			strAirSeaReceiveId = Util.null2str(tempMblData.get("SEA_IMPORT_RECEIVE_ID")).trim();
            		} else {
            			//strAirSeaReceiveId = Util.null2str(tempMblData.get("SEA_EXPORT_RECEIVE_ID")).trim();
            			strAirSeaReceiveId = "KCS4G002";
            		}
            	}else{//KLNET
            		if("IMP".equals(boundTypeStr)) {
            			//
            		} else {
            			strAirSeaReceiveId = "KCS0003XML";
            		}
            	}
            }
            
            //테스트 끝나고 삭제해야함 2016-02-16 삭제요망
//            if("IMFCK".equals(Util.null2str(docSend.get("entCode")))){
//            	strAirSeaReceiveId = "KCS4G002";
//            }
            
            // UNB 
            if("SEA".equals(boundStr) && "KLNET".equals(ediVender)) {
            	writeData.append("UNB+KECA:4+").append(strFwd_SenderID).append(":KL+").append(Util.null2str(strAirSeaReceiveId).trim()).append(":KC+");
            }else{
            	writeData.append("UNB+KECA:4+").append(strFwd_SenderID).append(":57+").append(Util.null2str(strAirSeaReceiveId).trim() + ":57+");
            }
            writeData.append(DateTime.getDate("yyyyMMdd:HHmm")).append("+1");
            writeData.append("'");
            nSegmentCount++;
            
            //UNH
            writeData.append("UNH+1+CUSMOD:S:93A:KE");
            writeData.append("'");
            nSegmentCount++;
            
            //정정신청 제출번호 | 적하목록문서 구분 | 제출일자 | 신청세관 | 신청인성명
//          "AO_SAVE_SAM_BGM | AO_SAVE_SAM_GIS | AO_SAVE_SAM_DTM | AO_SAVE_SAM_LOC"
            //정정신청 제출번호
            //AO_SAVE_SAM_BGM
            if("AIR".equals(boundStr) && "EXP".equals(boundTypeStr)) {
                //BGM+ACB+14PKYKW2126FILO0001'
                writeData.append("BGM+ACB+").append(modReqNo);
                writeData.append("'");
                nSegmentCount++;
                
                //적하목록문서 구분
                //AO_SAVE_SAM_GIS
                //GIS+SM2:5CH:KCS'
                //'''@@@ 4세대 SM2 ===> AM2로 변경  (항공적하목록 부분자전송 B Type)
                writeData.append("GIS+AM2:5CH:KCS");
                writeData.append("'"); 
                nSegmentCount++;
                docCode = "CUSMODACB";
            } else if("SEA".equals(boundStr) && "EXP".equals(boundTypeStr)) {
                //BGM+SCB+14PKYKW2126FILO0001'
            	writeData.append("BGM+SCB+").append(modReqNo);
                writeData.append("'"); 
                nSegmentCount++;

                //적하목록문서 구분
                //OO_SAVE_SAM_GIS"
                //GIS+SM2:5CH:KCS
                writeData.append("GIS+SM2:5CH:KCS");
                writeData.append("'"); 
                nSegmentCount++;
                docCode = "CUSMODSCB";
            }
            
            //제출일자
            //AO_SAVE_SAM_DTM
            //DTM+150:20141128161300:204'
            writeData.append("DTM+150:").append(strDeclarerDate).append(":204");
            writeData.append("'"); 
            nSegmentCount++;

            //신청세관
            //AO_SAVE_SAM_LOC
            //LOC+42+020:113:KCS+10:5AB:KCS'
            writeData.append("LOC+42+").append(strCustoms).append(":113:KCS+").append(strCustomsDepart).append(":5AB:KCS");
            writeData.append("'"); 
            nSegmentCount++;

            
            String entBusinessName = Util.null2str(fwSendAccInfo.get("ENT_BUSINESS_NAME")).trim();
            String modType4th = null;
            String modTypeCntr = null;
            		
            //신청인성명(사전제출)
            //AO_SAVE_SAM_NAD
            //NAD+MS++(주) 파인로직스'
            //'''@@@ 4세대 신청인의성명 35 ===> 100자리로 변경  체크!!
            if(entBusinessName.length() <= 35){
//            	writeData.append("NAD+MS++" + "IMAX");
            	writeData.append("NAD+MS++");
            	writeData.append(ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(entBusinessName, 0, 35)));
            	writeData.append("'");
            	nSegmentCount++;
            }else if(entBusinessName.length() <= 70){
            	writeData.append("NAD+MS++");
            	writeData.append(ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(entBusinessName, 0, 35)));
            	writeData.append(":").append(ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(entBusinessName, 35, 70)));
            	writeData.append("'");
            	nSegmentCount++;
            }else{
            	writeData.append("NAD+MS++"); 
            	writeData.append(ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(entBusinessName, 0, 35)));
            	writeData.append(":").append(ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(entBusinessName, 35, 70)));
            	writeData.append(":").append(ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(entBusinessName, 70, 100)));
            	writeData.append("'");
            	nSegmentCount++;
            }
            
            String mblNo = "";
            StringBuffer correctionContents = new StringBuffer();
            for(int nRowIndex = 0; nRowIndex < tempHblDataList.size(); nRowIndex++) {
            	correctionContents.setLength(0);
            	modType4th = Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_TYPE_4TH"));
            	houseBlTypeIert = Util.null2str(tempHblDataList.get(nRowIndex).get("BL_TYPE_IERT"));
            	
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
            	
            	if(mblNo.equals("") || !mblNo.equals(Util.null2str(tempHblDataList.get(nRowIndex).get("MBL_NO")))){
            		//마스터 번호
                    //DMS+PKYFW14212008+704'
                    //  '''@@@ 4세대 B/L 번호 16 ===> 20자리로 변경
                    writeData.append("DMS+");
                    writeData.append(Util.getFileWriteDataWithRtrim(Util.null2str(tempMblData.get("MBL_NO")), "String", "20"));
                    writeData.append("+704");
                    writeData.append("'");
                    nSegmentCount++;
            	}
            	
                //HOUSE B/L 정정사항 
//              "AO_SAVE_SAM_DMS_IMD"
                //IMD+++:::FILO1408012'
                // '''@@@ 4세대 B/L 번호 16 ===> 20자리로 변경
                writeData.append("IMD+++:::").append(Util.getFileWriteDataWithRtrim(Util.null2str(tempHblDataList.get(nRowIndex).get("HBL_NO")).trim(), "String", "20"));
                writeData.append("'");
                nSegmentCount++;

                //정정구분 (C: H B/L 정정 E: H B/L 추가 G:H B/L 삭제) | 화물구분 | House B/L 포장갯수, 포장종류 
//              "AO_SAVE_SAM_DMS_GIS1 | AO_SAVE_SAM_DMS_GIS2 |AO_SAVE_SAM_DMS_PAC"
                //정정구분 (C: H B/L 정정 E: H B/L 추가 G:H B/L 삭제)
                //AO_SAVE_SAM_DMS_GIS1
                //GIS+E:5CM:KCS'
                // '''@@@ 4세대 정정구분 변경 (05: H B/L 정정 06: H B/L 추가 07:H B/L 삭제)
                writeData.append("GIS+").append(modType4th.equals("99") ? "05" : modType4th).append(":5CM:KCS");
                writeData.append("'");
                nSegmentCount++;
                
                if(modType4th.equals("06")){
                	//화물구분
                    //AO_SAVE_SAM_DMS_GIS2, 06:추가일경우에만 추가 현석
                    //if("06" == Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_TYPE_4TH")).trim() && Util.null2str(tempHblDataList.get(nRowIndex).get("BL_TYPE_IERT")).trim().length() > 0) {
                    if(houseBlTypeIert.trim().length() > 0) {
                        //GIS+E:105:KCS'
                    	writeData.append("GIS+").append(houseBlTypeIert.trim()).append(":105:KCS");
                        writeData.append("'");
                        nSegmentCount++;
                    }
                    
                  //장치장소 2018.11.20 추가 지서희
                    if("AIR".equals(boundStr) && "EXP".equals(boundTypeStr)) {
    	                if(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_BONDED_SHED_CODE")).trim().length() > 0) {
    	                    //GIS+E:105:KCS'
    	                	writeData.append("LOC+107+").append(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_BONDED_SHED_CODE")).trim()).append(":156:KCS");
    	                    writeData.append("'");
    	                    nSegmentCount++;
    	                }
                    }
                    
                  //적재장소 2018.11.20 추가 지서희
                    if(Util.null2str(tempHblDataList.get(nRowIndex).get("")).trim().length() > 0) {
                        //GIS+E:105:KCS'
                    	writeData.append("LOC+14+").append(Util.null2str(tempHblDataList.get(nRowIndex).get("")).trim()).append(":129:KCS");
                        writeData.append("'");
                        nSegmentCount++;
                    }
                    
                  //항공해상 :개수가 다름 2018.11.20 지서희
                	if("AIR".equals(boundStr) && "EXP".equals(boundTypeStr)) {
                		if(Util.null2int(tempHblDataList.get(nRowIndex).get("PIECES")) > 0) {
//                			writeData.append("PAC+++").append(Util.null2str(tempHblDataList.get(nRowIndex).get("UNIT_PIECES")).trim()).append("::").append(Util.null2int(tempHblDataList.get(nRowIndex).get("PIECES")));
                			writeData.append("PAC+++").append("CT").append("::").append(Util.null2int(tempHblDataList.get(nRowIndex).get("PIECES")));
                			writeData.append("'"); 
                            nSegmentCount++;
                		}
                	}else if("SEA".equals(boundStr) && "EXP".equals(boundTypeStr)){
                		if(Util.null2str(tempHblDataList.get(nRowIndex).get("PIECES")).trim().length() > 0) {
//    	            		writeData.append("PAC+++").append(Util.null2str(tempHblDataList.get(nRowIndex).get("UNIT_PIECES")).trim()).append(":::").append(Util.null2int(tempHblDataList.get(nRowIndex).get("PIECES")));
    	            		writeData.append("PAC+++").append("GT").append(":::").append(Util.null2int(tempHblDataList.get(nRowIndex).get("PIECES")));
    	            		writeData.append("'"); 
    	                    nSegmentCount++;
                		}
                	}
                	
                }
                
                if(!modType4th.equals("99")){
                	// 정정사유 | 품명 | 화물고유식별번호
//                  "AO_SAVE_SAM_DMS_FTX1 | AO_SAVE_SAM_DMS_FTX2 "
                    // '''@@@ 4세대 정정사유 40 === > 100자리로 변경 체크!!
                    modReasonNameStr = Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_REASON_NAME")).trim(); //중복 변수 지정 - ditobass!!@
                    if(modReasonNameStr.length() > 0) {

                        modReasonNameStr50to100 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(modReasonNameStr, 50, 100)); //중복 변수 지정 - ditobass!!@
                        
                        //FTX+ACD+++BL 추가'
                    	writeData.append("FTX+ACD+++"); 
                    	writeData.append(ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(modReasonNameStr, 0, 50)));

                    	if(!"".equals(modReasonNameStr50to100)) { writeData.append(":").append(modReasonNameStr50to100); }
                    	writeData.append("'");
                    	nSegmentCount++;
                    }
                }
                   
                if(modType4th.equals("06")){
                	//품명
                    //AO_SAVE_SAM_DMS_FTX2
                    commodityItemDesc = Util.null2str(tempHblDataList.get(nRowIndex).get("COMMODITY_ITEM_DESC")).trim(); //중복 변수 지정 - ditobass!!@
                    if(commodityItemDesc.length() > 0) {

                        commodityItemDesc50to100 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(commodityItemDesc, 50, 100)); //중복 변수 지정 - ditobass!!@
                        commodityItemDesc100to150 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(commodityItemDesc, 100, 150)); //중복 변수 지정 - ditobass!!@
                        commodityItemDesc150to200 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(commodityItemDesc, 150, 200)); //중복 변수 지정 - ditobass!!@
                        
                        //FTX+AAA+++LIFT'
                    	writeData.append("FTX+AAA+++");
                    	writeData.append(ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(commodityItemDesc, 0, 50)));
                        
                    	if(!"".equals(commodityItemDesc50to100)) { writeData.append(":").append(commodityItemDesc50to100); }
                    	if(!"".equals(commodityItemDesc100to150)) { writeData.append(":").append(commodityItemDesc100to150); }
                    	if(!"".equals(commodityItemDesc150to200)) { writeData.append(":").append(commodityItemDesc150to200); }
                    	writeData.append("'");
                    	nSegmentCount++;
                    }
                    
                  //화물고유식별번호
                    //AO_SAVE_SAM_DMS_FTX3
//                    if(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_UCR_NO")).trim().length() > 0) {
//                        //FTX+CUS+++UCRNO'
//                    	writeData.append("FTX+CUS+++");
//                    	writeData.append(ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("EDI_UCR_NO")).trim(), 0, 35)));
//                        writeData.append("'");
//                        nSegmentCount++;
//                    }
                }

                //중량
//              "AO_SAVE_SAM_DMS_MEA", 06:추가 일경우에만 추가 현석
                //if("06" == Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_TYPE_4TH")).trim() && new BigDecimal(Util.null2str(tempHblDataList.get(nRowIndex).get("GWT"), "0")).doubleValue() > 0) {
                if("AIR".equals(boundStr) && "EXP".equals(boundTypeStr)) {
	                if(new BigDecimal(Util.null2str(tempHblDataList.get(nRowIndex).get("GWT"), "0")).doubleValue() > 0) {
	                    //MEA+WT++KG:12000'	(항공은소숫정1자리, 해운은소숫점2자리)
	                	//'''@@@ 4세대 소수점 3자리로 변경 중량 16,3 CBM 12,3
	                	writeData.append("MEA+WT++KG:").append(ediCommonService.getNumberFormat(Util.null2str(tempHblDataList.get(nRowIndex).get("GWT"), "0"), "##0.000", 3));
	                    writeData.append("'");
	                    nSegmentCount++;
	                }
                }else if("SEA".equals(boundStr) && "EXP".equals(boundTypeStr) && modType4th.equals("06")) {
                	if(Util.null2str(tempHblDataList.get(nRowIndex).get("GWT")).trim().length() > 0) {
	                    //MEA+WT++KG:12000'	(항공은소숫정1자리, 해운은소숫점2자리)
	                	//'''@@@ 4세대 소수점 3자리로 변경 중량 16,3 CBM 12,3
	                	writeData.append("MEA+WT++KG:").append(ediCommonService.getNumberFormat(Util.null2str(tempHblDataList.get(nRowIndex).get("GWT"), "0"), "##0.000", 3));
	                    writeData.append("'");
	                    nSegmentCount++;
	                }
                    //용적, 06:추가 일경우에만 추가 현석
                    //if("06" == Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_TYPE_4TH")).trim() && new BigDecimal(Util.null2str(tempHblDataList.get(nRowIndex).get("CBM"), "0")).doubleValue() > 0) {
                	if(Util.null2str(tempHblDataList.get(nRowIndex).get("CBM")).trim().length() > 0) {
                        //MEA+VOL++MTQ:12.00'
                    	writeData.append("MEA+VOL++MTQ:").append(ediCommonService.getNumberFormat(Util.null2str(tempHblDataList.get(nRowIndex).get("CBM"), "0"), "##0.000", 3));
                        writeData.append("'");
                        nSegmentCount++;
                    }

                    //컨테이너 갯수
                    if(Util.null2str(tempHblDataList.get(nRowIndex).get("CNTR_QTY")).trim().length() > 0) {
                        //MEA+AAE++NMB:1'
//                    	writeData.append("MEA+AAE++NMB:1" + Util.null2int(tempHblDataList.get(nRowIndex).get("CNTR_QTY"));
                    	//'''@@@ 4세대 스펙보고 변경 왜 1이 들어가 있는지???
                    	writeData.append("MEA+AAE++NMB:").append(Util.null2int(tempHblDataList.get(nRowIndex).get("CNTR_QTY")));
                        writeData.append("'");
                        nSegmentCount++;
                    }
                }
                
                if(modType4th.equals("06")){
                	//송하인, 수하인, 통지처 정보
//                  "AO_SAVE_SAM_DMS_NAD"
                    shipperNameStr = Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_NAME")).trim(); //중복 변수 지정 - ditobass!!@
                    if(shipperNameStr.length() > 0) {

                        shipperAddressStr = Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_ADDRESS")).trim(); //중복 변수 지정 - ditobass!!@
                        shipperCountryCodeStr = Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_COUNTRY_CODE")).trim(); //중복 변수 지정 - ditobass!!@
                        shipperPlaceCodeStr = Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_PLACE_CODE")).trim(); //중복 변수 지정 - ditobass!!@
                        shipperAddressStr0to35 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(shipperAddressStr, 0, 35)); //중복 변수 지정 - ditobass!!@
                        shipperAddressStr35to70 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(shipperAddressStr, 35, 70)); //중복 변수 지정 - ditobass!!@
                        shipperAddressStr70to105 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(shipperAddressStr, 70, 105)); //중복 변수 지정 - ditobass!!@
                        shipperAddressStr105to140 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(shipperAddressStr, 105, 140)); //중복 변수 지정 - ditobass!!@
                        shipperAddressStr140to150 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(shipperAddressStr, 140, 150)); //중복 변수 지정 - ditobass!!@
                        shipperNameStr0to35 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(shipperNameStr, 0, 35)); //중복 변수 지정 - ditobass!!@
                        shipperNameStr35to70 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(shipperNameStr, 35, 70)); //중복 변수 지정 - ditobass!!@
                        shipperNameStr70to100 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(shipperNameStr, 70, 100)); //중복 변수 지정 - ditobass!!@
                        
                        //NAD+CZ++HESHBON CO.,LTD.:673-52, GYEONGSEO-DONG, SEO-GUINCHE:ON 404-170, KOREATEL 82-32-585-3570:, 3581   FAX 82-32-585-3585'
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
                    	
                    	/*if(shipperPlaceCodeStr.length() > 0) {
                    		writeData.append("++").append(Util.getFileWriteDataWithRtrim(ediCommonService.getEdiModifyCharacterChk(shipperPlaceCodeStr), "String", "5"));
                    	}
                    	if(shipperCountryCodeStr.length() > 0) {
                    		writeData.append("+").append(Util.getFileWriteDataWithRtrim(ediCommonService.getEdiModifyCharacterChk(shipperCountryCodeStr), "String", "2"));
                    	}*/
                    	//CountryCode & City
                    	/** 2021-10-12 항공/해상 스팩 동일해서 주석처리 */
//                    	if("AIR".equals(boundStr) && "EXP".equals(boundTypeStr)) {
                    		if(shipperCountryCodeStr.length() > 0 && shipperPlaceCodeStr.length() > 0) {
                    			writeData.append("+++").append(shipperCountryCodeStr).append(shipperPlaceCodeStr);
                    		}else{
                    			if(shipperPlaceCodeStr.length() > 0){
                    				writeData.append("+++").append(Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_COUNTRY2")).trim()).append(shipperPlaceCodeStr);
                    			}
                    		}
//                    	}else if("SEA".equals(boundStr) && "EXP".equals(boundTypeStr)){
//                    		writeData.append("+++").append(shipperPlaceCodeStr);
//                    	}
                    	writeData.append("++").append(shipperCountryCodeStr);
                        writeData.append("'");
                        nSegmentCount++;

                        if(Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_TEL")).trim().length() > 0) {
                        	writeData.append("COM+").append(Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_TEL")).trim()).append(":TE");
                            writeData.append("'");
                            nSegmentCount++;
                        }
                    }

                    //CONSIGNEE
                    consigneeNameStr = Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_NAME")).trim(); //중복 변수 지정 - ditobass!!@
                    
                    if(consigneeNameStr.length() > 0) {
                        consigneeAddressStr = Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_ADDRESS")).trim(); //중복 변수 지정 - ditobass!!@
                        consigneeCountryCodeStr = Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_COUNTRY_CODE")).trim(); //중복 변수 지정 - ditobass!!@
                        consigneePlaceCodeStr = Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_PLACE_CODE")).trim(); //중복 변수 지정 - ditobass!!@
                        consigneeAddressStr0to35 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(consigneeAddressStr, 0, 35)); //중복 변수 지정 - ditobass!!@
                        consigneeAddressStr35to70 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(consigneeAddressStr, 35, 70)); //중복 변수 지정 - ditobass!!@
                        consigneeAddressStr70to105 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(consigneeAddressStr, 70, 105)); //중복 변수 지정 - ditobass!!@
                        consigneeAddressStr105to140 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(consigneeAddressStr, 105, 140)); //중복 변수 지정 - ditobass!!@
                        consigneeAddressStr140to150 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(consigneeAddressStr, 140, 150)); //중복 변수 지정 - ditobass!!@
                        consigneeNameStr0to35 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(consigneeNameStr, 0, 35)); //중복 변수 지정 - ditobass!!@
                        consigneeNameStr35to70 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(consigneeNameStr, 35, 70)); //중복 변수 지정 - ditobass!!@
                        consigneeNameStr70to100 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(consigneeNameStr, 70, 100)); //중복 변수 지정 - ditobass!!@
                        
                        //NAD+CN++YINGKOU LIDE ELECTRONIC TECHNOLOGY:NO.139 JINNIUSHAN STREET, YINGKOU C:ITYLIAONING PROVINCE, CHINACELL PHO:NE  86-186-4176-4075'
                    	writeData.append("NAD+CN++");
                    	if(!"".equals(consigneeAddressStr0to35)) { writeData.append(consigneeAddressStr0to35); }
                    	if(!"".equals(consigneeAddressStr35to70)) { writeData.append(":").append(consigneeAddressStr35to70); }
                    	if(!"".equals(consigneeAddressStr70to105)) { writeData.append(":").append(consigneeAddressStr70to105); }
                    	if(!"".equals(consigneeAddressStr105to140)) { writeData.append(":").append(consigneeAddressStr105to140); }
                    	if(!"".equals(consigneeAddressStr140to150)) { writeData.append(":").append(consigneeAddressStr140to150); }
                    	
                    	if(!"".equals(consigneeNameStr0to35)) { writeData.append("+").append(consigneeNameStr0to35); }
                    	if(!"".equals(consigneeNameStr35to70)) { writeData.append(":").append(consigneeNameStr35to70); }
                    	if(!"".equals(consigneeNameStr70to100)) { writeData.append(":").append(consigneeNameStr70to100); }
                    	
                    	/*if(consigneePlaceCodeStr.length() > 0) {
                    		writeData.append("++").append(Util.getFileWriteDataWithRtrim(ediCommonService.getEdiModifyCharacterChk(consigneePlaceCodeStr), "String", "5"));
                    	}
                    	if(consigneeCountryCodeStr.length() > 0) {
                    		writeData.append("+").append(Util.getFileWriteDataWithRtrim(ediCommonService.getEdiModifyCharacterChk(consigneeCountryCodeStr), "String", "2"));
                    	}*/
                    	//CountryCode & City
                    	/** 2021-10-12 항공/해상 스팩 동일해서 주석처리 */
//                    	if("AIR".equals(boundStr) && "EXP".equals(boundTypeStr)) {
                        	if(consigneeCountryCodeStr.length() > 0 && consigneePlaceCodeStr.length() > 0) {
                        		writeData.append("+++").append(consigneeCountryCodeStr).append(consigneePlaceCodeStr);
                        	}else{
                        		if(consigneePlaceCodeStr.length() > 0){
                        			writeData.append("+++").append(Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_COUNTRY2")).trim()).append(consigneePlaceCodeStr);
                        		}
                        	}
//                    	}else if("SEA".equals(boundStr) && "EXP".equals(boundTypeStr)){
//                    		writeData.append("+++").append(consigneePlaceCodeStr);
//                    	}
                    	writeData.append("++").append(consigneeCountryCodeStr);
                        writeData.append("'");
                        nSegmentCount++;

                        if(Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_TEL")).trim().length() > 0) {
                        	writeData.append("COM+").append(Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_TEL")).trim()).append(":TE");
                            writeData.append("'");
                            nSegmentCount++;
                        }
                    }
                    
                    //2021-09-03 NOTIFY제거
                    
//                  "License 정정"
//                  tempSearchData = new HashMap<String, Object>();
                  tempSearchData.clear(); //초기화 - ditobass!!@
                  tempSearchData.put("odJobNo", Util.null2str(tempHblDataList.get(nRowIndex).get("OD_JOB_NO")).trim());
                  tempHblLicenseList = manifestMapper.getEdiMfModifyHblLicenseList(tempSearchData);
                  
//                  int nDOCCNT = 0; //사용 안함 주석 - ditobass!!@
                  for(int nLicenseIndex = 0; nLicenseIndex < tempHblLicenseList.size(); nLicenseIndex++) {
//                      nDOCCNT++; //사용 안함 주석 - ditobass!!@
                      //License 수정사항
//                    "AO_SAVE_SAM_DMS_IMD_DOC"
                      //DOC+830:::N+020151403110378'
                      writeData.append("DOC+");

                      //(830: 수출신고, 85:적하목록, 916:UCR)
                      if("AIR".equals(boundStr) && "EXP".equals(boundTypeStr)) {
                          if("E".equals(houseBlTypeIert.trim())) {
                          	writeData.append("830");
                          } else {
                          	writeData.append("85");
                          }
                      } else if("SEA".equals(boundStr) && "EXP".equals(boundTypeStr)) {
  	                    if("E".equals(houseBlTypeIert.trim())) {
  	                    	writeData.append("830");
  	                    } else if("R".equals(houseBlTypeIert.trim())) {
  	                    	writeData.append("85");
  	                    }/* else { //스펙에 없음 2018.11.22 지서희
  	                    	writeData.append("916"); //UCR정정(사전제출);
  	                    }*/
                      }
                      
                      //License 번호, 분할선적여부, 수출근거번호, 동시포장 여부
                      //writeData.append(":::"+Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("PARTIAL_YN")).trim());
                      String partial_Tf =  "false";
                      if("Y".equals(Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("PARTIAL_YN")).trim())){
                      	partial_Tf = "true";
                      }
                      writeData.append(":::").append(partial_Tf);
                      writeData.append("+").append(Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("LICENSE_NO")).trim());

                      if(Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("COMBINE_TYPE_A_Z")).trim().length() > 0) {
                      	writeData.append("::").append(Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("COMBINE_TYPE_A_Z")).trim());
                      }
                      writeData.append("'");
                      nSegmentCount++;
                      
                      //License 정정구분 | License 정정전 수출신고번호 | License 정정 사유
//                    "AO_SAVE_SAM_DMS_IMD_DOC_GIS | AO_SAVE_SAM_DMS_IMD_DOC_RFF | AO_SAVE_SAM_DMS_IMD_DOC_FTX"
                      //License 정정구분(I:B/L추가 A:추가, U:정정, D:삭제, X:수출신고번호변경)    
                      //AO_SAVE_SAM_DMS_IMD_DOC_GIS
                      //GIS+I:5DS:KCS'
                      writeData.append("GIS+").append(Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("MOD_TYPE_LICENSE")).trim()).append(":5DS:KCS");
                      writeData.append("'");
                      nSegmentCount++;

                      /** 수출면장 정정 사용하지않아서 정정전 수출신고번호, 정정사유 제거함 */

                      //License 포장 갯수 | License 동시포장 갯수 | License 중량 | License 분할선적차수
//                    "AO_SAVE_SAM_DMS_IMD_DOC_MEA1 | AO_SAVE_SAM_DMS_IMD_DOC_MEA2 | AO_SAVE_SAM_DMS_IMD_DOC_MEA3 | AO_SAVE_SAM_DMS_IMD_DOC_MEA4"
                      
                      //License 동시포장 갯수
                      if(Util.null2int(tempHblLicenseList.get(nLicenseIndex).get("COMBINE_PIECES")) > 0) {
                      	//MEA+5AA++5AA:갯수'
                      	writeData.append("MEA+5AA++CT:").append(Util.null2int(tempHblLicenseList.get(nLicenseIndex).get("COMBINE_PIECES")));
                          writeData.append("'");
                          nSegmentCount++;
                      }
                      if(Util.null2int(tempHblLicenseList.get(nLicenseIndex).get("PIECES")) > 0) {
                      	//MEA+CT++CT:12'
                      	writeData.append("MEA+CT++CT:").append(Util.null2int(tempHblLicenseList.get(nLicenseIndex).get("PIECES")));
                          writeData.append("'");
                          nSegmentCount++;
                      }
                      //License 중량
                      if(new BigDecimal(Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("GWT"), "0")).doubleValue() > 0) {
                      	//MEA+WT++KG:12000.00'
                      	//MEA+WT++KG:100.0'
                      	writeData.append("MEA+WT++KG:").append(ediCommonService.getNumberFormat(Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("GWT"), "0"), "##0.000", 3));
                          writeData.append("'");
                          nSegmentCount++;
                      }
                      //License 분할선적차수
                      if(Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("PARTIAL_SEQ")).trim().length() > 0) {
                          //MEA+AAU++SCO:차수'
                      	writeData.append("MEA+AAU++SCO:").append(Util.null2int(tempHblLicenseList.get(nLicenseIndex).get("PARTIAL_SEQ")));
                          writeData.append("'");
                          nSegmentCount++;
                      }
                  }
                  
                }

                if(modType4th.equals("99") || modType4th.equals("06")){
//                  "Container 정정"
                    if("SEA".equals(boundStr) && "EXP".equals(boundTypeStr)) {
            			//Container 정정
                        tempHblCntrList = (List<Map<String, Object>>)tempHblDataList.get(nRowIndex).get("containerList");
                    	
                        for(int nContainerIndex = 0; nContainerIndex < tempHblCntrList.size(); ++nContainerIndex) {
  	          			    // 컨테이너번호 | Container 정정구분 | Container 정정전 컨테이너번호 | Container 정정사유/봉인번호1/봉인번호2
  	//          			    "OO_SAVE_SAM_DMS_IMD_EQD | OO_SAVE_SAM_DMS_IMD_EQD_GIS | OO_SAVE_SAM_DMS_IMD_EQD_RFF | OO_SAVE_SAM_DMS_IMD_EQD_FTX"
  	          			    if(Util.null2str(tempHblCntrList.get(nContainerIndex).get("CNTR_NO")).trim().length() > 0) {
  	          			    	modTypeCntr = Util.null2str(tempHblCntrList.get(nContainerIndex).get("MOD_TYPE_CNTR"));
  	          			        //EQD+CN+TGHU3176829+22GP'
  	          			    	writeData.append("EQD+CN+");
  	          			    	writeData.append(Util.null2str(tempHblCntrList.get(nContainerIndex).get("CNTR_NO")).trim());
  	          			    	if(modTypeCntr.equals("A")){
  	          			    		writeData.append("+").append(Util.null2str(tempHblCntrList.get(nContainerIndex).get("CNTR_EDI_CODE")).trim());
  	          			    	}
  	                              writeData.append("'");
  	                              nSegmentCount++;
  	  	
  	  	        				//OO_SAVE_SAM_DMS_IMD_EQD_GIS
  	  	        				//Container 정정구분(I:B/L 추가/A:추가/U:정정/D:삭제/X:컨테이너 번호변경)
  	  	        				//GIS+I:5DS:KCS'
  	                              writeData.append("GIS+").append(modTypeCntr);
  	                              writeData.append(":5DS:KCS");
  	                              writeData.append("'");
  	                              nSegmentCount++;
  	  	
  	                            if(modTypeCntr.equals("U")){
  	  	  	        				//OO_SAVE_SAM_DMS_IMD_EQD_RFF
  	  	  	        				//Container 정정전 컨테이너번호
  	  	  	        				    //RFF+CV:정정전번호'
  	  	        					writeData.append("RFF+CV:").append(Util.null2str(tempHblCntrList.get(nContainerIndex).get("BEFORE_VALUE")).trim());
  	  	                            writeData.append("'");
  	  	                            nSegmentCount++;
  	  	  	        				correctionContents.append("컨테이너번호정정:" + Util.null2str(tempHblCntrList.get(nContainerIndex).get("BEFORE_VALUE")) + "=>" + Util.null2str(tempHblCntrList.get(nContainerIndex).get("AFTER_VALUE")) + ")");
  	                            }else if(modTypeCntr.equals("A")){
  	                            	correctionContents.append("추가:" + Util.null2str(tempHblCntrList.get(nContainerIndex).get("CNTR_NO")) + ")");
  	                            }else if(modTypeCntr.equals("D")){
  	                            	correctionContents.append("삭제:" + Util.null2str(tempHblCntrList.get(nContainerIndex).get("CNTR_NO")) +")");
  	                            }
  	                            
  	  	        				//OO_SAVE_SAM_DMS_IMD_EQD_FTX
  	  	        				//Container 정정사유
  	                              containerModReasonNameStr = Util.null2str(tempHblCntrList.get(nContainerIndex).get("MOD_REASON_NAME")).trim(); //중복 변수 지정 - ditobass!!@
  	  	        				if(containerModReasonNameStr.length() > 0) {
  	                                  containerModReasonNameStr0to50 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(containerModReasonNameStr, 0, 50)); //중복 변수 지정 - ditobass!!@
  	                                  containerModReasonNameStr50to100 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(containerModReasonNameStr, 50, 100)); //중복 변수 지정 - ditobass!!@
  	  	        				    
  	  	        				    //FTX+ACD+++정정사유'
  	  	        					writeData.append("FTX+ACD+++");
  	  	        					if(!"".equals(containerModReasonNameStr0to50)) { writeData.append(containerModReasonNameStr0to50); }
  	  	                        	if(!"".equals(containerModReasonNameStr50to100)) {writeData.append(":").append(containerModReasonNameStr50to100); }
  	  	                            writeData.append("'");
  	  	                            nSegmentCount++;
  	  	        				}
  	  	        				
	  	  	        			if(modTypeCntr.equals("A")){
	  	  	        				
	  	  	        			  cntrSealNo1Str = Util.null2str(tempHblCntrList.get(nContainerIndex).get("CNTR_SEAL_NO_1")).trim(); //중복 변수 지정 - ditobass!!@
	  	        				    if(cntrSealNo1Str.length() > 0) {
	                                      cntrSealNo1Str0to10 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(cntrSealNo1Str, 0, 10)); //중복 변수 지정 - ditobass!!@
	                                      cntrSealNo1Str10to20 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(cntrSealNo1Str, 10, 20)); //중복 변수 지정 - ditobass!!@
	  	        				        
	  	        				        //SEL+봉인번호'
	  	        				    	writeData.append("SEL+");
	  	        				    	if(!"".equals(cntrSealNo1Str0to10)) { writeData.append(cntrSealNo1Str0to10); }
	  		                        	if(!"".equals(cntrSealNo1Str10to20)) { writeData.append(":::").append(cntrSealNo1Str10to20); }
	  		                            writeData.append("'");
	  		                            nSegmentCount++;
	  	        				    }
	
	                                  cntrSealNo2Str = Util.null2str(tempHblCntrList.get(nContainerIndex).get("CNTR_SEAL_NO_2")).trim(); //중복 변수 지정 - ditobass!!@
	  	        				    if(cntrSealNo2Str.length() > 0) {
	                                      cntrSealNo2Str0to10 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(cntrSealNo2Str, 0, 10)); //중복 변수 지정 - ditobass!!@
	                                      cntrSealNo2Str10to20 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(cntrSealNo2Str, 10, 20)); //중복 변수 지정 - ditobass!!@
	  	        				        
	  	        				        //SEL+봉인번호'
	  	        				    	writeData.append("SEL+");
	  	        				    	if(!"".equals(cntrSealNo2Str0to10)) { writeData.append(cntrSealNo2Str0to10); }
	  		                        	if(!"".equals(cntrSealNo2Str10to20)) { writeData.append(":::").append(cntrSealNo2Str10to20); }
	  		                            writeData.append("'");
	  		                            nSegmentCount++;
	  	        				    }
	  	  	        			}
  	          			    }
  	          			}	// end of for-loop.
  	          		}
                }
                if(modType4th.equals("05")){
//            		"단일항목 정정"
                    tempSearchData.clear(); //초기화 - ditobass!!@
                    tempSearchData.put("odJobNo", Util.null2str(tempHblDataList.get(nRowIndex).get("OD_JOB_NO")));
                    tempSearchData.put("modJobNo", Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_JOB_NO")));
                    tempSearchData.put("entCode", docSend.get("entCode"));
                    tempHblSingleList = manifestMapper.getEdiMfModifyHblSingleList(tempSearchData);
                    
//            		int nLINCNT = 0; //사용 안함 주석 - ditobass!!@
            		for(int nSingleIndex = 0; nSingleIndex < tempHblSingleList.size(); ++nSingleIndex) {
//            		    nLINCNT++; //사용 안함 주석 - ditobass!!@
                        
            		    //Single 정정번호 | 정정후 내역 | Single 정정사유
//            		    "AO_SAVE_SAM_DMS_IMD_LIN | AO_SAVE_SAM_DMS_IMD_LIN_FTX1 | AO_SAVE_SAM_DMS_IMD_LIN_FTX2"
            		    //Single 정정번호
            		    //LIN+0001++CA'
            		    if(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_ITEM_CODE_HBL")).trim().length() > 0) {
            		    	//정정항목번호??
            		    	writeData.append("LIN+++").append(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_ITEM_CODE_HBL")).trim());
                            writeData.append("'");
                            nSegmentCount++;

    	        			//Single 정정후내역
//    	        			"AO_SAVE_SAM_DMS_IMD_LIN_FTX1"
                            // FTX+ACB+++20'
                            modDescAfterStr = Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_DESC_AFTER")).trim(); //중복 변수 지정 - ditobass!!@
                                    
    	        			if(modDescAfterStr.length() > 0) {
                                modDescAfterStr0to50 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(modDescAfterStr,0,50)); //중복 변수 지정 - ditobass!!@
                                modDescAfterStr50to100 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(modDescAfterStr,50,100)); //중복 변수 지정 - ditobass!!@
                                modDescAfterStr100to150 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(modDescAfterStr,100,150)); //중복 변수 지정 - ditobass!!@
                                modDescAfterStr150to200 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(modDescAfterStr,150,200)); //중복 변수 지정 - ditobass!!@
    	        			    
    	        				writeData.append("FTX+ACB+++");
    	        				if("AIR".equals(boundStr) && "EXP".equals(boundTypeStr)) {
    	        					if(!"".equals(modDescAfterStr0to50)) { writeData.append(modDescAfterStr0to50); }
    		        				if(!"".equals(modDescAfterStr50to100)) { writeData.append(":").append(modDescAfterStr50to100); }
    		        				if(!"".equals(modDescAfterStr100to150)) { writeData.append(":").append(modDescAfterStr100to150); }
    		        				if(!"".equals(modDescAfterStr150to200)) { writeData.append(":").append(modDescAfterStr150to200); }
    	        				}else if("SEA".equals(boundStr) && "EXP".equals(boundTypeStr)) {
    	        					if(!"".equals(modDescAfterStr0to50)) { writeData.append(modDescAfterStr0to50); }
    		        				writeData.append(":");
    		        				if(!"".equals(modDescAfterStr50to100)) { writeData.append(modDescAfterStr50to100); }
    		        				writeData.append(":");
    		        				if(!"".equals(modDescAfterStr100to150)) { writeData.append(modDescAfterStr100to150); }
    		        				writeData.append(":");
    		        				if(!"".equals(modDescAfterStr150to200)) { writeData.append(modDescAfterStr150to200); }
    	        				}
    	        				
    	                        writeData.append("'");
    	                        nSegmentCount++;
    	        			}
    	
    	        			//Single 정정사유
//    	        			"AO_SAVE_SAM_DMS_IMD_LIN_FTX2"
    	        			// FTX+ACD+++담당자 실수로 포장갯수 정정'
	        				singleModReasonNameStr = Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_REASON_NAME")).trim(); //중복 변수 지정 - ditobass!!@
                            
	        				if(singleModReasonNameStr.length() > 0) {
	        					singleModReasonNameStr0to50 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(singleModReasonNameStr, 0, 50)); //중복 변수 지정 - ditobass!!@
	        					singleModReasonNameStr50to100 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(singleModReasonNameStr, 50, 100)); //중복 변수 지정 - ditobass!!@
	        				    
		        				writeData.append("FTX+ACD+++");
		        				if(!"".equals(singleModReasonNameStr0to50)) { writeData.append(singleModReasonNameStr0to50); }
		        				if(!"".equals(singleModReasonNameStr50to100)) { writeData.append(":").append(singleModReasonNameStr50to100); }
		                        writeData.append("'");
		                        nSegmentCount++;
	        				}
                            singleModReasonNameStr = Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_REASON_NAME")).trim(); //중복 변수 지정 - ditobass!!@
	        				if(singleModReasonNameStr.length() > 0) {
                                singleModReasonNameStr0to50 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(singleModReasonNameStr, 0, 50)); //중복 변수 지정 - ditobass!!@
                                singleModReasonNameStr50to100 = ediCommonService.getEdiModifyCharacterChk(ediCommonService.getSubStringK(singleModReasonNameStr, 50, 100)); //중복 변수 지정 - ditobass!!@
	        				    
	        					writeData.append("FTX+ACD+++");
		        				if(!"".equals(singleModReasonNameStr0to50)) { writeData.append(singleModReasonNameStr0to50); }
		        				if(!"".equals(singleModReasonNameStr50to100)) { writeData.append(":").append(singleModReasonNameStr50to100); }
		                        writeData.append("'");
		                        nSegmentCount++;	
	        				}
	        				correctionContents.append(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_ITEM_NAME_HBL")) + ":");
	        				correctionContents.append(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_DESC_BEFORE")) + "=>");
	        				correctionContents.append(modDescAfterStr);
	        				if(nSingleIndex != tempHblSingleList.size() + 1){
	        					correctionContents.append(", ");
	        				}else{
	        					correctionContents.append(")");
	        				}
            		    }
            		}
                }
                
                if(correctionContents.length() > 1000){
                	tempHblDataList.get(nRowIndex).put("correctionContents", correctionContents.toString().substring(0, 1000));
                }else{
                	tempHblDataList.get(nRowIndex).put("correctionContents", correctionContents.toString());
                }
                tempHblDataList.get(nRowIndex).put("gmtTime", docSend.get("gmtTime"));
            	manifestMapper.updateMfModifyInfo(tempHblDataList.get(nRowIndex));
            }	// end of for-loop.

            
            //UNT+세그먼트 갯수(UNH부터 UNT까지 세그먼트 갯수)+1'
            writeData.append("UNT+").append(String.valueOf(nSegmentCount)).append("+1");
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
		
		retData[4] = docCode;
		retData[5] = modReqNo;
		
		return retData;
	}
	
	/**
	 * 적하목록정정신고 이력삭제
	 * @param sendManifest
	 * @return
	 * @throws java.lang.Exception
	 */
	public OrderSaveResult modSendReset(ManifestCorrect manifestCorrect) throws java.lang.Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		Map<String, Object> tmpMap = manifestMapper.selectModMblInfo(manifestCorrect);
		if(tmpMap != null){
			/** 전송오류 or 미전송 or 수신상태오류 or 결과상태 = 3:기각   일 경우만 처리 */
			if("E".equals(tmpMap.get("SEND_STATUS")) || tmpMap.get("SEND_STATUS") == null || "E".equals(tmpMap.get("RECV_STATUS")) || "3".equals(tmpMap.get("RESULT_STATUS"))){
				manifestMapper.deleteModResult(manifestCorrect);
				
				SearchManifestCorrectList searchManifestCorrectList = new SearchManifestCorrectList();
				searchManifestCorrectList.setMrn(Util.null2str(tmpMap.get("MRN")));
				if("YHTXX".equals(manifestCorrect.getEntCode()) || "UPSXK".equals(manifestCorrect.getEntCode())) {
					if(!"".equals(Util.null2str(manifestCorrect.getModSerialNo()))) {
						tmpMap.put("modJobNo", manifestCorrect.getModJobNo());
						tmpMap.put("modSerialNo", manifestCorrect.getModSerialNo());
						manifestMapper.updateModSendReset(tmpMap);
						orderSaveResult.setResultCode(manifestCorrect.getModSerialNo());
					}else {
						ManifestCorrectMaster manifestCorrectMaster = manifestMapper.getSerialNo(searchManifestCorrectList);
							if(manifestCorrectMaster != null){
							tmpMap.clear();
							tmpMap.put("modJobNo", manifestCorrect.getModJobNo());
							tmpMap.put("modSerialNo", manifestCorrectMaster.getModSerialNo());
							manifestMapper.updateModSendReset(tmpMap);
							orderSaveResult.setResultCode(manifestCorrectMaster.getModSerialNo());
						}
					}
				}else {
					ManifestCorrectMaster manifestCorrectMaster = manifestMapper.getSerialNo(searchManifestCorrectList);
					if(manifestCorrectMaster != null){
						tmpMap.clear();
						tmpMap.put("modJobNo", manifestCorrect.getModJobNo());
						tmpMap.put("modSerialNo", manifestCorrectMaster.getModSerialNo());
						manifestMapper.updateModSendReset(tmpMap);
						orderSaveResult.setResultCode(manifestCorrectMaster.getModSerialNo());
					}
				}
			}else{
				orderSaveResult.setResultCode("A10028");	/** 전송된 자료는 수정할 수 없습니다. */
			}
		}
		return orderSaveResult;
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
		List<Map<String, Object>> tempHblLicenseList = null;
		
		Map<String, Object> tempSearchData = new HashMap<>();
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(docSend, "gmtTime", false, null, null, null, null);
		
		/*주석처리 문서쓰기 변경
		StringBuffer retWriteData = new StringBuffer();	// 파일 용량이 클경우 내용 저장하면 안됨.
		*/
		String writeData = null;
		String retData[] = new String[6];
		String fileName = new StringBuffer(Util.null2str(docSend.get("registerUserId"))).append(".H").append(Util.null2str(tempMblData.get("MBL_NO")).trim()).append(".txt").toString();	// 전송 파일명
        String ediVender = Util.null2str(docSend.get("ediVender"));
		String dirPath[] = ediCommonService.getSendFilePath(ediVender, "edi/manifestMod", docSend);	// 로컬폴더 경로 가져오기
		
		/*문서쓰기방식 변경하여 추가*/
		String dirPath0 = dirPath[0];
		
		String modReqNo = Util.null2str(tempMblData.get("MOD_DECLARE_NO"));
		String strDeclarerDate = Util.null2str(tempMblData.get("MOD_DECLARE_DATE")).trim().replace("-","").replace(":", "");
		
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
			this.createElement(document, lv1, lv2, "kcn:SenderId", Util.getFileWriteDataWithRtrim(ediCommonService.getDeleteCharStringFwb(Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).trim()), "String", "4"));
			/** 수신아이디 */
			this.createElement(document, lv1, lv2, "kcn:ReceiverId", "KCNECP");
			/** 문서명 */
			this.createElement(document, lv1, lv2, "kcn:DocName", "ACB");
			/** 문서코드 */
			this.createElement(document, lv1, lv2, "kcn:DocCode", "ACB");
			/** 메세지아이디 */
			this.createElement(document, lv1, lv2, "kcn:MessageId", Util.null2str(tempMblData.get("MBL_NO")));
			/** 중개업체 */
			this.createElement(document, lv1, lv2, "kcn:VenderId", "YHT");
			/** KCNETID */
			this.createElement(document, lv1, lv2, "kcn:ID", Util.null2str(ediHostSendInfo.get("IF_WEB_ID")));
			/** KCNETPW */
			this.createElement(document, lv1, lv2, "kcn:Password", Util.null2str(ediHostSendInfo.get("IF_WEB_PWD")));
			
			
//			soapenvBody = this.createElement(document, lv1, soapenvBody, "soapenv:Body", null);
			soapenvBody = this.createElement(document, soapenv, soapenvBody, "soapenv:Body", null);
			
			root = document.createElement("wco:Declaration");
			root.setAttribute("xmlns:kcs", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_ACBSchemaModule:1:0");
			root.setAttribute("xmlns:wco", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_ACBSchemaModule:1:1");
			root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			root.setAttribute("xsi:schemaLocation", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_ACBSchemaModule:1:0 ./schema4G/kcs/data/standard/KCS_DeclarationOfCAG_ACBSchemaModule_1.0_standard.xsd");
			soapenvBody.appendChild(root);
			
            
			/** 신고세관/신고과 */
			this.createElement(document, root, lv1, "wco:DeclarationOfficeID", Util.null2str(tempMblData.get("CUSTOMS_CODE")).trim() + Util.null2str(tempMblData.get("CUSTOMS_DPT_CODE")).trim());
			/** 제출번호 */
			this.createElement(document, root, lv1, "wco:ID", modReqNo);
			/** 제출일시 */
			this.createElement(document, root, lv1, "wco:IssueDateTime", strDeclarerDate);
			/** 문서형태구분 */
			this.createElement(document, root, lv1, "wco:TypeCode", "GOVCBRACB");
			/** 적하목록문서 구분 */
			this.createElement(document, root, lv1, "kcs:SubTypeCode", "AM2");
			
			/** wco:Consignment 시작 */
			lv1 = this.createElement(document, root, lv1, "wco:Consignment", null);

            String modType4th = null;
            String houseBlTypeIert = null;
            String mblNo = "";
            StringBuffer correctionContents = new StringBuffer();
            for(int nRowIndex = 0; nRowIndex < tempHblDataList.size(); nRowIndex++) {
            	correctionContents.setLength(0);
            	modType4th = Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_TYPE_4TH"));
            	houseBlTypeIert = Util.null2str(tempHblDataList.get(nRowIndex).get("BL_TYPE_IERT"));
            	
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
            	
            	if(mblNo.equals("") || !mblNo.equals(Util.null2str(tempHblDataList.get(nRowIndex).get("MBL_NO")))){
            		/** AWB 번호(M-AWB) */
        			lv2 = this.createElement(document, lv1, lv2, "wco:TransportContractDocument", null);
        			this.createElement(document, lv2, lv3, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("MBL_NO")));
            	}
            	
            	/** wco:HouseConsignment 시작 */
    			lv2 = this.createElement(document, lv1, lv2, "wco:HouseConsignment", null);
    			if (modType4th.equals("06")){
    				/** 화물구분 */
    				this.createElement(document, lv2, lv3, "kcs:CargoProcessTypeCode", houseBlTypeIert);	//(E:수출화물, R:제선적(환적)화물)
    				/** 포장개수 */
    				this.createElement(document, lv2, lv3, "wco:TotalPackageQuantity", Util.null2str(tempHblDataList.get(nRowIndex).get("PIECES")));
    			}
    			/** 정정구분 */
    			this.createElement(document, lv2, lv3, "kcs:RequestOverrideCode", modType4th.equals("99") ? "05" : modType4th);
    			/** 정정사유 */
    			this.createElement(document, lv2, lv3, "kcs:StatementDescription", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_REASON_NAME")), 0, 50));
    			/** AWB 번호(H-AWB) */
    			lv3 = this.createElement(document, lv2, lv3, "wco:TransportContractDocument", null);
    			this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("HBL_NO")));
    			
    			if (modType4th.equals("06")){
    				/** 적재장소 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:DepartureConveyanceFacility", null);
    				this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("")));
                    
    				/** 장치장소 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:GoodsLocation", null);
    				this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("")));
                    
                    /** 포장종류 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:Packaging", null);
    				this.createElement(document, lv3, lv4, "wco:TypeCode", "CT");
    				
    				/** wco:Consignor 시작 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:Consignor", null);
    				/** 송하인 상호 또는 성명 */
    				this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_NAME")), 0, 100));
    				/** wco:Address 시작 */
    				lv4 = this.createElement(document, lv3, lv4, "wco:Address", null);
    				/** 송하인 국가코드 */
    				this.createElement(document, lv4, lv5, "wco:CountryCode", Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_COUNTRY_CODE")));
//    				/** 송하인 도시코드 */
//    				this.createElement(document, lv4, lv5, "wco:CountrySubDivisionID", Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_COUNTRY_CODE")) + Util.null2str(tempHblDataList.get(nRowIndex).get("TEDAO_MOD_HBL.ShipperCity")));
    				/** 송하인 주소 */
    				this.createElement(document, lv4, lv5, "wco:Line", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_ADDRESS")), 0, 150));
    				/** wco:Address 끝 */
                    if (!Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_TEL")).equals("")){
                    	/** wco:Communication 시작 */
        				lv4 = this.createElement(document, lv3, lv4, "wco:Communication", null);
        				/** 통신수단유형 */
        				this.createElement(document, lv4, lv5, "wco:TypeID", "TE");
        				/** 전화번호 */
        				this.createElement(document, lv4, lv5, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_TEL")).trim());
        				/** wco:Communication 끝 */
                    }
                    /** wco:Consignor 끝 */

                    
                    /** wco:Consignee 시작 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:Consignee", null);
    				/** 수하인 상호 또는 성명 */
    				this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_NAME")), 0, 100));
    				/** wco:Address 시작 */
    				lv4 = this.createElement(document, lv3, lv4, "wco:Address", null);
    				/** 수하인 국가코드 */
    				this.createElement(document, lv4, lv5, "wco:CountryCode", Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_COUNTRY_CODE")));
//    				/** 수하인 도시코드 */
//    				this.createElement(document, lv4, lv5, "wco:CountrySubDivisionID", Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_COUNTRY_CODE")) + Util.null2str(tempHblDataList.get(nRowIndex).get("TEDAO_MOD_HBL.ConsigneeCity")));
    				/** 수하인 주소 */
    				this.createElement(document, lv4, lv5, "wco:Line", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_ADDRESS")), 0, 150));
    				/** wco:Address 끝 */
                    if (!Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_TEL")).equals("")){
                    	/** wco:Communication 시작 */
        				lv4 = this.createElement(document, lv3, lv4, "wco:Communication", null);
        				/** 통신수단유형 */
        				this.createElement(document, lv4, lv5, "wco:TypeID", "TE");
        				/** 전화번호 */
        				this.createElement(document, lv4, lv5, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_TEL")).trim());
        				/** wco:Communication 끝 */
                    }
                    /** wco:Consignee 끝 */


                    
                    /** wco:ConsignmentItem 시작 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:ConsignmentItem", null);
    				/** 품명 */
    				lv4 = this.createElement(document, lv3, lv4, "wco:Commodity", null);
    				this.createElement(document, lv4, lv5, "wco:CargoDescription", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("COMMODITY_ITEM_DESC")), 0, 200));
    				/** 중량 */
    				lv4 = this.createElement(document, lv3, lv4, "wco:GoodsMeasure", null);
    				this.createElement(document, lv4, lv5, "wco:GrossMassMeasure", Util.null2str(tempHblDataList.get(nRowIndex).get("GWT")));
                    /** wco:ConsignmentItem 끝 */
    			}
    			if (modType4th.equals("05")){
    				tempSearchData.clear();
                    tempSearchData.put("odJobNo", Util.null2str(tempHblDataList.get(nRowIndex).get("OD_JOB_NO")));
                    tempSearchData.put("modJobNo", Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_JOB_NO")));
                    tempHblSingleList = manifestMapper.getEdiMfModifyHblSingleList(tempSearchData);
                    for(int nSingleIndex = 0; nSingleIndex < tempHblSingleList.size(); ++nSingleIndex) {
                    	
                    	/** wco:AdditionalInformation 시작 */
                    	lv3 = this.createElement(document, lv2, lv3, "wco:AdditionalInformation", null);
                    	/** 정정사유 */
                    	this.createElement(document, lv3, lv4, "wco:StatementDescription", ediCommonService.getSubStringK(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_REASON_NAME")), 0, 100));
                    	/** wco:Amendment 시작 */
        				lv4 = this.createElement(document, lv3, lv4, "wco:Amendment", null);
        				/** 정정후 내역 */
        				this.createElement(document, lv4, lv5, "wco:StatementDescription", ediCommonService.getSubStringK(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_DESC_AFTER")), 0, 200));
        				/** 정정항목번호 */
            			lv5 = this.createElement(document, lv4, lv5, "wco:Pointer", null);
            			this.createElement(document, lv5, lv6, "wco:TagID", Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_ITEM_CODE_HBL")));
            			/** wco:Amendment 끝 */
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
    			}
    			/** UCR */
//    			lv3 = this.createElement(document, lv2, lv3, "wco:UCR", null);
//    			this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(tempMblData.get("TEDAO_MOD_HBL.ModiUCRNo")));
    			
    			if (modType4th.equals("06")){
    				tempSearchData.clear();
                    tempSearchData.put("odJobNo", Util.null2str(tempHblDataList.get(nRowIndex).get("OD_JOB_NO")).trim());
                    tempHblLicenseList = manifestMapper.getEdiMfModifyHblLicenseList(tempSearchData);
                    for(int nLicenseIndex = 0; nLicenseIndex < tempHblLicenseList.size(); nLicenseIndex++) {
                    	/** wco:PreviousDocument 시작 */
            			lv3 = this.createElement(document, lv2, lv3, "wco:PreviousDocument", null);
            			/** 수출신고/화물관리번호 구분 코드 */
                        if("E".equals(houseBlTypeIert.trim())) {
                        	this.createElement(document, lv3, lv4, "wco:TypeCode", "830");
                        } else {
                        	this.createElement(document, lv3, lv4, "wco:TypeCode", "85");
                        }
                        /** 수출신고번호 또는 화물관리번호  */
                        this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("LICENSE_NO")).trim());
                        /** 분할선적여부 */
                        if("Y".equals(Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("PARTIAL_YN")).trim())){
                        	this.createElement(document, lv3, lv4, "wco:TransportSplitIndicator", "true");
                        }else{
                        	this.createElement(document, lv3, lv4, "wco:TransportSplitIndicator", "false");
                        }
                        /** 분할선적차수 */
                        this.createElement(document, lv3, lv4, "kcs:TransportSplitNumeric", Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("PARTIAL_SEQ")));
                        
                        /** wco:Packaging 시작 */
            			lv4 = this.createElement(document, lv3, lv4, "wco:Packaging", null);
            			/** 선적포장종류코드 */
            			this.createElement(document, lv4, lv5, "wco:TypeCode", Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("UNIT_PIECES")));
            			/** 포장개수 */
            			this.createElement(document, lv4, lv5, "wco:TotalPackageQuantity", Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("PIECES")));
            			/** 동시포장그룹부호 */
            			this.createElement(document, lv4, lv5, "kcs:ConcurrentPackagingCode", Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("COMBINE_TYPE_A_Z")));
            			/** 동시포장개수 */
            			this.createElement(document, lv4, lv5, "kcs:ConcurrentPackagingNumeric", Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("COMBINE_PIECES")));
            			/** wco:Packaging 끝 */
            			
            			/** 중량 */
            			lv4 = this.createElement(document, lv3, lv4, "wco:GoodsMeasure", null);
            			this.createElement(document, lv4, lv5, "wco:GrossMassMeasure", ediCommonService.getNumberFormat(Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("GWT"), "0"), "##0.000", 3));
            			
            			/** wco:AdditionalInformation 시작 */
            			lv4 = this.createElement(document, lv3, lv4, "wco:AdditionalInformation", null);
            			/** 작업구분 */
            			this.createElement(document, lv4, lv5, "wco:StatementCode", Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("MOD_TYPE_LICENSE")));
            			/** 정정사유 */
            			this.createElement(document, lv4, lv5, "wco:StatementDescription", "");
            			/** 정정전 수출신고번호 또는 화물관리번호 */
            			lv5 = this.createElement(document, lv4, lv5, "wco:Amendment", null);
            			this.createElement(document, lv5, lv6, "wco:StatementDescription", "");
            			/** wco:AdditionalInformation 끝 */
            			/** wco:PreviousDocument 끝 */
            			/** wco:HouseConsignment 끝 */
                    }
    			}
    			
                /** wco:Consignment 끝 */
    			
    			if(correctionContents.length() > 1000){
                	tempHblDataList.get(nRowIndex).put("correctionContents", correctionContents.toString().substring(0, 1000));
                }else{
                	tempHblDataList.get(nRowIndex).put("correctionContents", correctionContents.toString());
                }
                tempHblDataList.get(nRowIndex).put("gmtTime", docSend.get("gmtTime"));
            	manifestMapper.updateMfModifyInfo(tempHblDataList.get(nRowIndex));
            }
            /** 신청자성명 */
            lv1 = this.createElement(document, root, lv1, "wco:Submitter", null);
            lv2 = this.createElement(document, lv1, lv2, "wco:Contact", null);
            this.createElement(document, lv2, lv3, "wco:Name", Util.null2str(fwSendAccInfo.get("ENT_BUSINESS_NAME_ENG")));
            
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
		
		retData[4] = "GOVCBRACB";
		retData[5] = modReqNo;
		
		return retData;
	}
	public String[] getSendDataFileCreate4thKCNET(Map<String, Object> docSend, Map<String, Object> tempMblData, List<Map<String, Object>> tempHblDataList, Map<String, Object> fwSendAccInfo, 
			Map<String, Object> ediHostSendInfo) throws java.lang.Exception {
		
		List<Map<String, Object>> tempHblSingleList = null;
		List<Map<String, Object>> tempHblLicenseList = null;
		
		Map<String, Object> tempSearchData = new HashMap<>();
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(docSend, "gmtTime", false, null, null, null, null);
		
		/*주석처리 문서쓰기 변경
		StringBuffer retWriteData = new StringBuffer();	// 파일 용량이 클경우 내용 저장하면 안됨.
		*/
		String writeData = null;
		String retData[] = new String[6];
		String fileName = new StringBuffer(Util.null2str(docSend.get("registerUserId"))).append(".H").append(Util.null2str(tempMblData.get("MBL_NO")).trim()).append(".txt").toString();	// 전송 파일명
        String ediVender = Util.null2str(docSend.get("ediVender"));
		String dirPath[] = ediCommonService.getSendFilePath(ediVender, "edi/manifestMod", docSend);	// 로컬폴더 경로 가져오기
		
		/*문서쓰기방식 변경하여 추가*/
		String dirPath0 = dirPath[0];
		
		String modReqNo = Util.null2str(tempMblData.get("MOD_DECLARE_NO"));
		String strDeclarerDate = Util.null2str(tempMblData.get("MOD_DECLARE_DATE")).trim().replace("-","").replace(":", "").replace(" ", "");
		
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
			root.setAttribute("xmlns:kcs", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_ACBSchemaModule:1:0");
			root.setAttribute("xmlns:wco", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_ACBSchemaModule:1:1");
			root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			root.setAttribute("xsi:schemaLocation", "urn:kr:gov:kcs:data:standard:KCS_DeclarationOfCAG_ACBSchemaModule:1:0 ./schema4G/kcs/data/standard/KCS_DeclarationOfCAG_ACBSchemaModule_1.0_standard.xsd");
			document.appendChild(root);
			
            
			/** 신고세관/신고과 */
			this.createElement(document, root, lv1, "wco:DeclarationOfficeID", Util.null2str(tempMblData.get("CUSTOMS_CODE")).trim() + Util.null2str(tempMblData.get("CUSTOMS_DPT_CODE")).trim());
			/** 제출번호 */
			this.createElement(document, root, lv1, "wco:ID", modReqNo);
			/** 제출일시 */
			this.createElement(document, root, lv1, "wco:IssueDateTime", strDeclarerDate);
			/** 문서형태구분 */
			this.createElement(document, root, lv1, "wco:TypeCode", "GOVCBRACB");
			/** 적하목록문서 구분 */
			this.createElement(document, root, lv1, "kcs:SubTypeCode", "AM2");
			
			/** wco:Consignment 시작 */
			lv1 = this.createElement(document, root, lv1, "wco:Consignment", null);

            String modType4th = null;
            String houseBlTypeIert = null;
            String mblNo = "";
            StringBuffer correctionContents = new StringBuffer();
            for(int nRowIndex = 0; nRowIndex < tempHblDataList.size(); nRowIndex++) {
            	correctionContents.setLength(0);
            	modType4th = Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_TYPE_4TH"));
            	houseBlTypeIert = Util.null2str(tempHblDataList.get(nRowIndex).get("BL_TYPE_IERT"));
            	
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
            	
            	if(mblNo.equals("") || !mblNo.equals(Util.null2str(tempHblDataList.get(nRowIndex).get("MBL_NO")))){
            		/** AWB 번호(M-AWB) */
        			lv2 = this.createElement(document, lv1, lv2, "wco:TransportContractDocument", null);
        			this.createElement(document, lv2, lv3, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("MBL_NO")));
            	}
            	
            	/** wco:HouseConsignment 시작 */
    			lv2 = this.createElement(document, lv1, lv2, "wco:HouseConsignment", null);
    			if (modType4th.equals("06")){
    				/** 화물구분 */
    				this.createElement(document, lv2, lv3, "kcs:CargoProcessTypeCode", houseBlTypeIert);	//(E:수출화물, R:제선적(환적)화물)
    				/** 포장개수 */
    				this.createElement(document, lv2, lv3, "wco:TotalPackageQuantity", Util.null2str(tempHblDataList.get(nRowIndex).get("PIECES")));
    			}
    			/** 정정구분 */
    			this.createElement(document, lv2, lv3, "kcs:RequestOverrideCode", modType4th.equals("99") ? "05" : modType4th);
    			/** 정정사유 */
    			this.createElement(document, lv2, lv3, "kcs:StatementDescription", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_REASON_NAME")), 0, 50));
    			/** AWB 번호(H-AWB) */
    			lv3 = this.createElement(document, lv2, lv3, "wco:TransportContractDocument", null);
    			this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("HBL_NO")));
    			
    			if (modType4th.equals("06")){
    				/** 적재장소 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:DepartureConveyanceFacility", null);
    				this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("")));
                    
    				/** 장치장소 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:GoodsLocation", null);
    				this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("")));
                    
                    /** 포장종류 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:Packaging", null);
    				this.createElement(document, lv3, lv4, "wco:TypeCode", "CT");
    				
    				/** wco:Consignor 시작 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:Consignor", null);
    				/** 송하인 상호 또는 성명 */
    				this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_NAME")), 0, 100));
    				/** wco:Address 시작 */
    				lv4 = this.createElement(document, lv3, lv4, "wco:Address", null);
    				/** 송하인 국가코드 */
    				this.createElement(document, lv4, lv5, "wco:CountryCode", Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_COUNTRY_CODE")));
//    				/** 송하인 도시코드 */
//    				this.createElement(document, lv4, lv5, "wco:CountrySubDivisionID", Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_COUNTRY_CODE")) + Util.null2str(tempHblDataList.get(nRowIndex).get("TEDAO_MOD_HBL.ShipperCity")));
    				/** 송하인 주소 */
    				this.createElement(document, lv4, lv5, "wco:Line", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_ADDRESS")), 0, 150));
    				/** wco:Address 끝 */
                    if (!Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_TEL")).equals("")){
                    	/** wco:Communication 시작 */
        				lv4 = this.createElement(document, lv3, lv4, "wco:Communication", null);
        				/** 통신수단유형 */
        				this.createElement(document, lv4, lv5, "wco:TypeID", "TE");
        				/** 전화번호 */
        				this.createElement(document, lv4, lv5, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("SHIPPER_TEL")).trim());
        				/** wco:Communication 끝 */
                    }
                    /** wco:Consignor 끝 */

                    
                    /** wco:Consignee 시작 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:Consignee", null);
    				/** 수하인 상호 또는 성명 */
    				this.createElement(document, lv3, lv4, "wco:Name", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_NAME")), 0, 100));
    				/** wco:Address 시작 */
    				lv4 = this.createElement(document, lv3, lv4, "wco:Address", null);
    				/** 수하인 국가코드 */
    				this.createElement(document, lv4, lv5, "wco:CountryCode", Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_COUNTRY_CODE")));
//    				/** 수하인 도시코드 */
//    				this.createElement(document, lv4, lv5, "wco:CountrySubDivisionID", Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_COUNTRY_CODE")) + Util.null2str(tempHblDataList.get(nRowIndex).get("TEDAO_MOD_HBL.ConsigneeCity")));
    				/** 수하인 주소 */
    				this.createElement(document, lv4, lv5, "wco:Line", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_ADDRESS")), 0, 150));
    				/** wco:Address 끝 */
                    if (!Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_TEL")).equals("")){
                    	/** wco:Communication 시작 */
        				lv4 = this.createElement(document, lv3, lv4, "wco:Communication", null);
        				/** 통신수단유형 */
        				this.createElement(document, lv4, lv5, "wco:TypeID", "TE");
        				/** 전화번호 */
        				this.createElement(document, lv4, lv5, "wco:ID", Util.null2str(tempHblDataList.get(nRowIndex).get("CONSIGNEE_TEL")).trim());
        				/** wco:Communication 끝 */
                    }
                    /** wco:Consignee 끝 */


                    
                    /** wco:ConsignmentItem 시작 */
    				lv3 = this.createElement(document, lv2, lv3, "wco:ConsignmentItem", null);
    				/** 품명 */
    				lv4 = this.createElement(document, lv3, lv4, "wco:Commodity", null);
    				this.createElement(document, lv4, lv5, "wco:CargoDescription", ediCommonService.getSubStringK(Util.null2str(tempHblDataList.get(nRowIndex).get("COMMODITY_ITEM_DESC")), 0, 200));
    				/** 중량 */
    				lv4 = this.createElement(document, lv3, lv4, "wco:GoodsMeasure", null);
    				this.createElement(document, lv4, lv5, "wco:GrossMassMeasure", Util.null2str(tempHblDataList.get(nRowIndex).get("GWT")));
                    /** wco:ConsignmentItem 끝 */
    			}
    			if (modType4th.equals("05")){
    				tempSearchData.clear();
                    tempSearchData.put("odJobNo", Util.null2str(tempHblDataList.get(nRowIndex).get("OD_JOB_NO")));
                    tempSearchData.put("modJobNo", Util.null2str(tempHblDataList.get(nRowIndex).get("MOD_JOB_NO")));
                    tempHblSingleList = manifestMapper.getEdiMfModifyHblSingleList(tempSearchData);
                    for(int nSingleIndex = 0; nSingleIndex < tempHblSingleList.size(); ++nSingleIndex) {
                    	
                    	/** wco:AdditionalInformation 시작 */
                    	lv3 = this.createElement(document, lv2, lv3, "wco:AdditionalInformation", null);
                    	/** 정정사유 */
                    	this.createElement(document, lv3, lv4, "wco:StatementDescription", ediCommonService.getSubStringK(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_REASON_NAME")), 0, 100));
                    	/** wco:Amendment 시작 */
        				lv4 = this.createElement(document, lv3, lv4, "wco:Amendment", null);
        				/** 정정후 내역 */
        				this.createElement(document, lv4, lv5, "wco:StatementDescription", ediCommonService.getSubStringK(Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_DESC_AFTER")), 0, 200));
        				/** 정정항목번호 */
            			lv5 = this.createElement(document, lv4, lv5, "wco:Pointer", null);
            			this.createElement(document, lv5, lv6, "wco:TagID", Util.null2str(tempHblSingleList.get(nSingleIndex).get("MOD_ITEM_CODE_HBL")));
            			/** wco:Amendment 끝 */
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
    			}
    			/** UCR */
//    			lv3 = this.createElement(document, lv2, lv3, "wco:UCR", null);
//    			this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(tempMblData.get("TEDAO_MOD_HBL.ModiUCRNo")));
    			
    			if (modType4th.equals("06")){
    				tempSearchData.clear();
                    tempSearchData.put("odJobNo", Util.null2str(tempHblDataList.get(nRowIndex).get("OD_JOB_NO")).trim());
                    tempHblLicenseList = manifestMapper.getEdiMfModifyHblLicenseList(tempSearchData);
                    for(int nLicenseIndex = 0; nLicenseIndex < tempHblLicenseList.size(); nLicenseIndex++) {
                    	/** wco:PreviousDocument 시작 */
            			lv3 = this.createElement(document, lv2, lv3, "wco:PreviousDocument", null);
            			/** 수출신고/화물관리번호 구분 코드 */
                        if("E".equals(houseBlTypeIert.trim())) {
                        	this.createElement(document, lv3, lv4, "wco:TypeCode", "830");
                        } else {
                        	this.createElement(document, lv3, lv4, "wco:TypeCode", "85");
                        }
                        /** 수출신고번호 또는 화물관리번호  */
                        this.createElement(document, lv3, lv4, "wco:ID", Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("LICENSE_NO")).trim());
                        /** 분할선적여부 */
                        if("Y".equals(Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("PARTIAL_YN")).trim())){
                        	this.createElement(document, lv3, lv4, "wco:TransportSplitIndicator", "true");
                        }else{
                        	this.createElement(document, lv3, lv4, "wco:TransportSplitIndicator", "false");
                        }
                        /** 분할선적차수 */
                        this.createElement(document, lv3, lv4, "kcs:TransportSplitNumeric", Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("PARTIAL_SEQ")));
                        
                        /** wco:Packaging 시작 */
            			lv4 = this.createElement(document, lv3, lv4, "wco:Packaging", null);
            			/** 선적포장종류코드 */
            			this.createElement(document, lv4, lv5, "wco:TypeCode", Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("UNIT_PIECES")));
            			/** 포장개수 */
            			this.createElement(document, lv4, lv5, "wco:TotalPackageQuantity", Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("PIECES")));
            			/** 동시포장그룹부호 */
            			this.createElement(document, lv4, lv5, "kcs:ConcurrentPackagingCode", Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("COMBINE_TYPE_A_Z")));
            			/** 동시포장개수 */
            			this.createElement(document, lv4, lv5, "kcs:ConcurrentPackagingNumeric", Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("COMBINE_PIECES")));
            			/** wco:Packaging 끝 */
            			
            			/** 중량 */
            			lv4 = this.createElement(document, lv3, lv4, "wco:GoodsMeasure", null);
            			this.createElement(document, lv4, lv5, "wco:GrossMassMeasure", ediCommonService.getNumberFormat(Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("GWT"), "0"), "##0.000", 3));
            			
            			/** wco:AdditionalInformation 시작 */
            			lv4 = this.createElement(document, lv3, lv4, "wco:AdditionalInformation", null);
            			/** 작업구분 */
            			this.createElement(document, lv4, lv5, "wco:StatementCode", Util.null2str(tempHblLicenseList.get(nLicenseIndex).get("MOD_TYPE_LICENSE")));
            			/** 정정사유 */
            			this.createElement(document, lv4, lv5, "wco:StatementDescription", "");
            			/** 정정전 수출신고번호 또는 화물관리번호 */
            			lv5 = this.createElement(document, lv4, lv5, "wco:Amendment", null);
            			this.createElement(document, lv5, lv6, "wco:StatementDescription", "");
            			/** wco:AdditionalInformation 끝 */
            			/** wco:PreviousDocument 끝 */
            			/** wco:HouseConsignment 끝 */
                    }
    			}
    			
                /** wco:Consignment 끝 */
    			
    			if(correctionContents.length() > 1000){
                	tempHblDataList.get(nRowIndex).put("correctionContents", correctionContents.toString().substring(0, 1000));
                }else{
                	tempHblDataList.get(nRowIndex).put("correctionContents", correctionContents.toString());
                }
                tempHblDataList.get(nRowIndex).put("gmtTime", docSend.get("gmtTime"));
            	manifestMapper.updateMfModifyInfo(tempHblDataList.get(nRowIndex));
            }
            /** 신청자성명 */
            lv1 = this.createElement(document, root, lv1, "wco:Submitter", null);
            lv2 = this.createElement(document, lv1, lv2, "wco:Contact", null);
            this.createElement(document, lv2, lv3, "wco:Name", Util.null2str(fwSendAccInfo.get("ENT_BUSINESS_NAME_ENG")));
            
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
			soapData.append("<kcn:DocName mustUnderstand=\"0\">ACB</kcn:DocName>");
			soapData.append("<kcn:DocCode mustUnderstand=\"0\">ACB</kcn:DocCode>");
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
		
		retData[4] = "GOVCBRACB";
		retData[5] = modReqNo;
		
		return retData;
	}
	
    /**
     * 오더 EDI 상태값 업데이트
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
	
	/**
     * License 오류검증 '''@@@
     * 관세청 4세대 전용
     * @param hblJobNo
     * @param sHouseModiCode
     * @param hblNo
     * @param bound
     * @param strBLTypeIE
     * @param errList
     * @throws Exception
     */
	public void getModifyVerifyLicense4th(String odJobNo, String sHouseModiCode, String hblNo, String bound, String strBLTypeIE, List<Map<String, Object>> errList) throws Exception {
        
		List<Map<String, Object>> tempHblLicenseList = null;
		
		Map<String, Object> tempSearchMapData = new HashMap<String, Object>();
		Map<String, Object> tempSearchData = new HashMap<String, Object>();
		
//		String licenseCheck = null;
		String strLicenseNo = null;
		String modTypeLicenseStr = null; //중복 변수 지정 - ditobass!!@
		String message = null;
		
        tempSearchMapData.put("odJobNo", Util.null2str(odJobNo).trim());
        tempHblLicenseList = manifestMapper.getEdiMfModifyHblLicenseList(tempSearchMapData);
        
        if(tempHblLicenseList != null && tempHblLicenseList.size() > 0) {
            for(int nRowCount = 0; nRowCount < tempHblLicenseList.size(); ++nRowCount) {
            	strLicenseNo = Util.null2str(tempHblLicenseList.get(nRowCount).get("LICENSE_NO")).trim();
                modTypeLicenseStr = Util.null2str(tempHblLicenseList.get(nRowCount).get("MOD_TYPE_LICENSE")).trim(); //중복 변수 지정 - ditobass!!@
            	
            	if(Util.null2str(tempHblLicenseList.get(nRowCount).get("BL_TYPE_IERT")).trim().length() == 0) {
//            		tempSearchData.put("srchCode", "");
//    				tempSearchData.put("message", "[License:" + strLicenseNo + "] 의 화물구분을 기재 바랍니다.");
//    				message = ediCommonService.getErrorCodeName(tempSearchData);
//    	 
//    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
            	}
            	
//              "Licese 화물구분이 E:수출화물일경우 체크"
                else if ("E".equals(strBLTypeIE)) {
                    if(strLicenseNo.length() > 5) {
                        if("E".equals(strLicenseNo.substring(5, 6)) || "R".equals(strLicenseNo.substring(5, 6))) {
                        	// 처리로직 없음.
                        } else {
                        	// 20161214 윤현석 - 4세대 14자리 체크부분 추가
                            if(strLicenseNo.length() != 15 && strLicenseNo.length() != 14) {
                                if(!"NCV".equals(strLicenseNo)) {
                                	tempSearchData.put("srchCode", "");
                    				tempSearchData.put("message", "[License:" + strLicenseNo + "] 의 신고번호를 입력하지 않았거나 자릿수가 틀립니다.");
                    				message = ediCommonService.getErrorCodeName(tempSearchData);
                    				
                    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                                }
                            } else{
                            	// 20161214 윤현석 - 4세대 15자리일때만 digit 체크부분 추가
                            	if(strLicenseNo.length() == 15){
                            		if(ediCommonService.getVerifyPermitCheckDigitOK(strLicenseNo) == false) {
                            			tempSearchData.put("srchCode", "");
                            			tempSearchData.put("message", "[License:" + strLicenseNo + "] 의 신고번호의 Check Digit을 확인 하십시오.");
                            			message = ediCommonService.getErrorCodeName(tempSearchData);
                            			
                            			errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                            		}
                            	}
                            }
                        }
                    } else {
                        if(!"NCV".equals(strLicenseNo)) {
                        	tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "[License:" + strLicenseNo + "] 의 신고번호를 입력하지 않았거나 자릿수가 틀립니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                        }
                    }
                }

//              "Licese 화물구분이 R:환적화물일경우 체크"
                else if("R".equals(strBLTypeIE)) {
                	// 20161214 윤현석 - 4세대 LICENSE_NO 19자리 체크 추가
                    if(strLicenseNo.length() != 18 && strLicenseNo.length() != 19) {
                        if(!"NCV".equals(strLicenseNo)) {
                        	if("E".equals(strLicenseNo.substring(5, 6)) || "R".equals(strLicenseNo.substring(5, 6))) {
                            	// 처리로직 없음.
                            } else {
                            	// 20161214 윤현석 - 4세대 기존 체크로직 사용X
                                /*if(strLicenseNo.length() == 15) {
                                	licenseCheck = strLicenseNo.substring(0, 11);
                                	
                                    if (EdiCommon.verifyMRNCheckDigitOK(licenseCheck) == false) {
                                    	tempSearchData.put("srchCode", "");
                        				tempSearchData.put("message", "[License:" + strLicenseNo + "] 의 신고번호의 Check Digit을 확인 하십시오.");
                        				message = ediCommonService.getErrorCodeName(tempSearchData);
                        	 
                        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                                    }
                                } else {
                                	tempSearchData.put("srchCode", "");
                    				tempSearchData.put("message", "[License:" + strLicenseNo + "] 의 신고번호를 입력하지 않았거나 자릿수가 틀립니다.");
                    				message = ediCommonService.getErrorCodeName(tempSearchData);
                    	 
                    				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                                }*/
                            	tempSearchData.put("srchCode", "");
                				tempSearchData.put("message", "[License:" + strLicenseNo + "] 환적건의 면장번호는 18자리(3세대) 또는 19자리(4세대)입니다.");
                				message = ediCommonService.getErrorCodeName(tempSearchData);
                	 
                				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                            	
                            }
                        }
                    } 
                    // 20161214 윤현석 - 4세대 LICENSE_NO 19자리 체크 추가
                    //사용 안함 주석 - ditobass!!@
//                    else if(strLicenseNo.length() == 18 || strLicenseNo.length() == 19) {
//                    	// 20161214 윤현석 - 4세대 기존 체크로직 사용X
//                    	/*licenseCheck = strLicenseNo.substring(0, 11);
//                    	
//                        if(EdiCommon.verifyMRNCheckDigitOK(licenseCheck) == false) {
//                        	tempSearchData.put("srchCode", "");
//            				tempSearchData.put("message", "[License:" + strLicenseNo + "] 의 신고번호의 Check Digit을 확인 하십시오.");
//            				message = ediCommonService.getErrorCodeName(tempSearchData);
//            	 
//            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
//                        }*/
//                    }
                }

                if("06".equals(sHouseModiCode)) {
                    if(modTypeLicenseStr.length() == 0) {
                    	tempSearchData.put("srchCode", "");
        				tempSearchData.put("message", "[License:" + strLicenseNo + "] 의 정정구분를 기재 바랍니다.");
        				message = ediCommonService.getErrorCodeName(tempSearchData);
        	 
        				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                    }
                    //License의 정정구분이 I:B/L추가 일경우 체크
                    else if("I".equals(modTypeLicenseStr)) {
                        if(Util.null2str(tempHblLicenseList.get(nRowCount).get("MOD_REASON_NAME")).trim().length() != 0) {
                        	tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "[License:" + strLicenseNo + "] 의 정정구분이 I이면 정정사유를 기재하지 않습니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                        }

                        if(Util.null2int(tempHblLicenseList.get(nRowCount).get("PIECES")) == 0) {
                        	tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "[License:" + strLicenseNo + "] 의 수량을 기재 바랍니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                        }

                        if(new BigDecimal(Util.null2str(tempHblLicenseList.get(nRowCount).get("GWT"), "0")).doubleValue() == 0) {
                        	tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "[License:" + strLicenseNo + "] 의 중량을 기재 바랍니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                        }

                        if("Y".equals(Util.null2str(tempHblLicenseList.get(nRowCount).get("PARTIAL_YN")).trim())) {
                        	if(Util.null2int(tempHblLicenseList.get(nRowCount).get("PARTIAL_SEQ")) == 0) {
                        		tempSearchData.put("srchCode", "");
                				tempSearchData.put("message", "[License:" + strLicenseNo + "] 의 분할선적유무가 Y이면 차수는 1이상이어야 합니다.");
                				message = ediCommonService.getErrorCodeName(tempSearchData);
                	 
                				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                        	}
                        }

                        if(Util.null2str(tempHblLicenseList.get(nRowCount).get("LICENSE_NO_BEFORE")).trim().length() != 0) {
                        	tempSearchData.put("srchCode", "");
            				tempSearchData.put("message", "[License:" + strLicenseNo + "] 의 정정구분이 A이면 정정전신고번호는 기재하지 않습니다.");
            				message = ediCommonService.getErrorCodeName(tempSearchData);
            	 
            				errList.add(ediCommonService.getErrorMessage(hblNo, "H.B/L : " + message));
                        }
                    }
                }
            }	// end of for-loop.
        }	// end of if.
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
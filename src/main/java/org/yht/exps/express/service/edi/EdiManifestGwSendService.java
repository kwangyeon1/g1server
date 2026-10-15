package org.yht.exps.express.service.edi;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.edi.ManifestMapper;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.edi.SendManifest;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.service.CommonService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

@Slf4j
@Service
@Transactional
public class EdiManifestGwSendService {
		
	@Autowired
	ManifestMapper manifestMapper;
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	@Autowired
	RestHighLevelClient client;
	@Autowired
	private CustomProperties customProperties;
	@Autowired
	StringRedisTemplate redisTemplate;
	
	@Resource(name="ediCommonService")
	EdiCommonService ediCommonService;
	@Resource(name = "commonService")
	CommonService commonService;
	
	
	/**
	 * 관우회 문서송신
	 * @param sendManifest
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult sendManifestGwSeqNo(SendManifest sendManifest) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> docSend = new HashMap<String, Object>();
		
		Map<String, Object> masterOrder = null;
		List<Map<String, Object>> masterOrderList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> houseOrderList = null;
		Map<String, Object> kctdiInfo = null;
		
		List<String> errList = new ArrayList<String>();
		List<String> confirmMsgList = new ArrayList<String>();
		int allHouseCnt = 0;
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(sendManifest, "gmtTime", false, null, null, null, null);
		
		docSend.put("entCode", Util.null2str(sendManifest.getEntCode()));
		docSend.put("entBusinessCode", Util.null2str(sendManifest.getEntBusinessCode()));
		docSend.put("solution", Util.null2str(sendManifest.getSolution()));
		docSend.put("bound", Util.null2str(sendManifest.getBound()));
		docSend.put("boundType", Util.null2str(sendManifest.getBoundType()));
		docSend.put("userLanguage", Util.null2str(sendManifest.getUserLanguage()));
		docSend.put("registerUserId", Util.null2str(sendManifest.getRegisterUserId()));
		docSend.put("registerUserName", sendManifest.getRegisterUserName());
		docSend.put("updateUserId", Util.null2str(sendManifest.getUpdateUserId()));
		docSend.put("gmtTime", Util.null2str(sendManifest.getGmtTime()));
		docSend.put("cancelYn", Util.null2str(sendManifest.getCancelYn()));
		docSend.put("cryptographyYn", sendManifest.getCryptographyYn());
		if("sendGwK002".equals(sendManifest.getAction()) || "sendGwCancelK002".equals(sendManifest.getAction())){
			docSend.put("version", "K002");
		}
		
		for(String masterOdJobNo : sendManifest.getModJobNoList()){
			docSend.put("odJobNo", masterOdJobNo);
			
			if(Util.null2str(sendManifest.getSubGridYn()).equals("Y")) {	//서브그리드인지 여부
				docSend.put("subGridYn", "Y");
				docSend.put("simpleDocNoList", sendManifest.getModSimpleDocList().get(masterOdJobNo));
				if(sendManifest.getModSimpleDocList().get(masterOdJobNo).contains(null)) {	//제출번호 리스트에 Null값이 있을 경우 쿼리에 조건 따로 줘야함.
					docSend.put("containsNullYn", "Y");
				};
			}
			
			if(sendManifest.getHouseOdJobNoList() != null) docSend.put("houseOdJobNoList", sendManifest.getHouseOdJobNoList());
			
			/** 마스터오더조회 */
			if("Y".equals(sendManifest.getConfirmYn())){
				docSend.put("srchTypeLog", "sendGwMaster");
			}else{
				docSend.put("srchTypeLog", "checkGwMaster");
			}
			masterOrder = manifestMapper.selectGwSendMasterOrder(docSend);
			if (masterOrder != null){
				if(Util.null2int(masterOrder.get("CANCELING_CNT")) > 0){
					errList.add("M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + "에 반입취소전송 처리중인 데이터가 있습니다. 잠시 후 다시 전송 바랍니다.<br />");
				}
				if(Util.null2str(masterOrder.get("BOUND")).equals("AIR")){
					if(Util.null2str(masterOrder.get("MRN")).equals("") || Util.null2str(masterOrder.get("MSN")).equals("") || Util.null2str(masterOrder.get("CUSTOMS_CODE")).equals("")){
						errList.add("M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + "의 MRN, MSN, 신고세관 정보가 없습니다.<br />");
					}
					if(Util.null2int(masterOrder.get("ITEM_ERR_CNT")) > 0){
						errList.add("M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + "에 품목코드가 1자리인 데이터가 있습니다.<br />");
					}
					if(Util.null2int(masterOrder.get("CD_USE_TYPE_ERR_CNT")) > 0){
						errList.add("M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + "에 용도구분(1.개인, 2.회사) 이외의 데이터가 있습니다.<br />");
					}
					if(!Util.null2str(masterOrder.get("DEST_PORT")).equals("ICN") && !Util.null2str(masterOrder.get("DEST_PORT")).equals("GMP")){
						errList.add("M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + "에 목적항 - ICN, GMP 이외의 데이터가 있습니다.<br />");
					}
				}else{
					if(Util.null2int(masterOrder.get("INSPECTION_ERR_CNT")) > 0){
						errList.add("M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + " 목록신고를 하지 않은 건은 개인통관고유부호에 데이터가 입력되면 삭제해야 하고 검사지정건도 취소해야 합니다.<br />");
					}
					if("K002".equals(docSend.get("version")) && !"KRINC".equals(masterOrder.get("DEST_PORT"))){
						errList.add("M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + " - 아암전송은 목적항이 KRINC일 경우에만 전송가능합니다.<br />");
					}else if(!Util.null2str(masterOrder.get("DEST_PORT")).equals("KRINC") && !Util.null2str(masterOrder.get("DEST_PORT")).equals("KRPUS") && !Util.null2str(masterOrder.get("DEST_PORT")).equals("KRPTK")
						&& !Util.null2str(masterOrder.get("DEST_PORT")).equals("KRKUV")){
						/** 해상은 KRINC,KRPUS,KRPTK,KRKUV만 전송중이어서 21.보세운송회사항목을 통관목록제출번호로 전송    해당 목적항 이외에 다른 목적항을 사용할경우 21번 항목 수정? */
						errList.add("M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + "에 목적항 - KRINC,KRPUS,KRPTK,KRKUV 이외의 데이터가 있습니다.<br />");
					}
				}
				
				//화면단에서 "~의 선별데이터가 없는데 전송하시겠습니까?"로 한 번 더 체크 -> 2023.01.02 장부장님 요청으로 삭제
//				if(!Util.null2str(masterOrder.get("SELECTION_YN")).equals("Y") && Util.null2int(masterOrder.get("CD_TYPE_Y_CNT")) > 0 && !Util.null2str(sendManifest.getUserConfirmYn()).equals("Y")){
//					confirmMsgList.add(Util.null2str(masterOrder.get("MBL_NO")));
//				}
				
				docSend.put("bound", masterOrder.get("BOUND"));
				//도착지 > 목적항으로 변경
				docSend.put("ediCityPort", masterOrder.get("DEST_PORT"));
				
				/** 지정장치장정보조회 */
				kctdiInfo = manifestMapper.selectKctdiInfo(docSend);
				if(kctdiInfo == null || kctdiInfo.size() == 0){
					errList.add("M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + "의 지정장치장정보가 없습니다.<br />");
				}else{
					
//					if("K002".equals(docSend.get("version"))){
//						kctdiInfo.put("EDI_FTP_IP", "119.196.128.35");
//						kctdiInfo.put("EDI_FTP_PORT", "10027");
//					}else if("KRKUV".equals(masterOrder.get("DEST_PORT"))){
//						kctdiInfo.put("EDI_FTP_PORT", "10027");
//					}
					
					/** EDI_FTP_ROOT필수값 체크하지 않도록 수정 2023-06-14 */
//					if(Util.null2str(kctdiInfo.get("EDI_FTP_IP")).equals("") || Util.null2str(kctdiInfo.get("EDI_FTP_ID")).equals("") || Util.null2str(kctdiInfo.get("EDI_FTP_PW")).equals("") || Util.null2str(kctdiInfo.get("EDI_FTP_ROOT")).equals("")){
					if(Util.null2str(kctdiInfo.get("EDI_FTP_IP")).equals("") || Util.null2str(kctdiInfo.get("EDI_FTP_ID")).equals("") || Util.null2str(kctdiInfo.get("EDI_FTP_PW")).equals("")){
						errList.add("M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + "의 지정장치장정보의 FTP 설정을 확인해 주시기 바랍니다.<br />");
					}
					if(Util.null2str(masterOrder.get("BOUND")).equals("SEA")) {
						if(Util.null2str(kctdiInfo.get("EDI_CUSTOMER_CODE")).equals("")){
							errList.add("M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + "의 지정장치장정보의 화주코드 설정을 확인 바랍니다.<br />");
						}
						if(Util.null2str(kctdiInfo.get("EDI_WH_LOCATION")).equals("")){
							errList.add("M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + "의 지정장치장정보의 장치위치 설정을 확인 바랍니다.<br />");
						}
					}
				}
				
				/** 하우스오더조회 */
				//기존소스 마스터구분없이 하우스모아서 처리
				//2021-11-26 지정장치장정보의 조회조건이 마스터마다 다를수 있어서 마스터별로 처리함
				docSend.put("masterOdJobNo", masterOrder.get("OD_JOB_NO"));
				houseOrderList = manifestMapper.selectGwSendHouseOrderList(docSend);
				if(houseOrderList == null || houseOrderList.size() == 0){
//					errList.add("M.B/L No : " + Util.null2str(masterOrder.get("MBL_NO")) + "에 전송할 하우스가 없습니다.<br />");
					
					if("Y".equals(docSend.get("cancelYn"))){
						errList.add("지정창고 반입전송 데이터가 없습니다.<br />");
					}else{
						errList.add(Util.null2str(masterOrder.get("HOUSE_CNT")) + "건 지정창고 반입전송 완료된 건입니다. 삭제 후 전송바랍니다.<br />");
					}
				}else{
					/** 2025-06-23 품명 필수값 추가 */
					String commodityItem;
					for(Map<String, Object> houseMap : houseOrderList){
						commodityItem = ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseMap.get("COMMODITY_ITEM")), "1");
						if(commodityItem.replace(" ", "").equals("")){
							errList.add(Util.null2str(houseMap.get("HBL_NO")) + "의 품명을 확인 바랍니다.<br />");
						}else{
							houseMap.put("COMMODITY_ITEM", commodityItem);
						}
						
					}
					allHouseCnt += houseOrderList.size();
				}
				
				masterOrder.put("houseOrderList", houseOrderList);
				masterOrder.put("kctdiInfo", kctdiInfo);
				if(!Util.null2str(masterOrder.get("DEST_CODE")).equals(Util.null2str(masterOrder.get("DEST_PORT")))){
					masterOrder.put("bondedCargoYn", "Y");
				}else{
					masterOrder.put("bondedCargoYn", "N");
				}
				masterOrderList.add(masterOrder);
			}
		}
		
		Map<String, Object>fwSendAccInfo = manifestMapper.getFwSendAccInfo(docSend);
		if(fwSendAccInfo == null || fwSendAccInfo.size() ==0 || Util.null2str(fwSendAccInfo.get("B2G_FORWARDER_SIGN")).length() != 4) {
			errList.add("업체부호(4자리)를 설정 후 전송하십시오.");
		}else{
			docSend.put("b2gForwarderSign", fwSendAccInfo.get("B2G_FORWARDER_SIGN"));
			docSend.put("entBusinessNameEng", fwSendAccInfo.get("ENT_BUSINESS_NAME_ENG"));
		}
		
		if(errList.size() > 0){
			result.put("errList", errList);
			orderSaveResult.setResultData(result);
			return orderSaveResult;
		}else if(confirmMsgList.size() > 0){
			result.put("confirmList", confirmMsgList);
			orderSaveResult.setResultData(result);
			return orderSaveResult;
		}
		
		//오류 없을 경우 전송여부 한 번 더 체크하도록 추가
		if(!Util.null2str(sendManifest.getConfirmYn()).equals("Y")){
			result.put("chkHouseCnt", allHouseCnt);
			orderSaveResult.setResultData(result);
			return orderSaveResult;
		}

		int masterCnt = 10001;
		
		/** 일련번호조회 */
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		tmpMap.put("entCode", docSend.get("entCode"));
		tmpMap.put("gmtTime", docSend.get("gmtTime"));
		tmpMap.put("entBusinessCode", "");
		tmpMap.put("updateUserId", docSend.get("updateUserId"));
		tmpMap.put("solution", "ECM");
		tmpMap.put("stockType", "OD");
		tmpMap.put("stockSeqLength", "8");
		tmpMap.put("dateLength", "4");
		tmpMap.put("signType", "noHyphen");
		tmpMap.put("stockDateFormat", "112");
		tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		tmpMap.put("stockRemark", "GW SEND SEQNO");
		if(Util.null2str(docSend.get("bound")).equals("AIR")){
			tmpMap.put("stockSign", "GWAIR");
		}else{
			tmpMap.put("stockSign", "GWSEA");
		}
		tmpMap.put("seqNo", masterCnt);
		tmpMap.put("subSeq", allHouseCnt);
		
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("docSend", docSend);
		returnMap.put("masterOrderList", masterOrderList);
		returnMap.put("stockStr", commonMapper.selectNextStockMaxNo(tmpMap).substring(5));	/** stock.substring(5) = 202300000030 */
		orderSaveResult.setReturnMap(returnMap);
		
		return orderSaveResult;
	}
	public OrderSaveResult sendManifestGw(SendManifest sendManifest, HttpServletRequest request, Map<String, Object> returnMap) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> docSend = (Map<String, Object>)returnMap.get("docSend");
		List<Map<String, Object>> masterOrderList = (List<Map<String, Object>>)returnMap.get("masterOrderList");
		List<Map<String, Object>> houseOrderList = null;
		List<String> errList = new ArrayList<String>();
		int houseCnt = 0;
		
		String stockStr = Util.null2str(returnMap.get("stockStr"));
		docSend.put("yyyy", stockStr.substring(0, 4));
		docSend.put("stockStartNo", Util.null2int(stockStr.substring(4)));
		int fileSeq = 1;
		int masterCnt = 10001;
		for(Map<String, Object> masterData : masterOrderList){
			houseOrderList = (List<Map<String, Object>>)masterData.get("houseOrderList");
			
			houseCnt = houseOrderList.size();
			for(int i = 0; i < houseCnt; i+=5000){
				if(i + 5000 < houseCnt) {
					this.getManifestGwSend(docSend, masterData, houseOrderList.subList(i, i + 5000), (Map<String, Object>)masterData.get("kctdiInfo"), errList, i, masterCnt++, fileSeq);
				}else{
					this.getManifestGwSend(docSend, masterData, houseOrderList.subList(i, houseCnt), (Map<String, Object>)masterData.get("kctdiInfo"), errList, i, masterCnt++, fileSeq);
				}
				fileSeq++;
			}
		}
		
		if(errList.size() > 0){
			result.put("errList", errList);
		}
		
		orderSaveResult.setResultData(result);
		return orderSaveResult;
	}
	
	/** 관우회전송 */
	public boolean getManifestGwSend(Map<String, Object> docSend, Map<String, Object> masterOrder, List<Map<String, Object>> houseOrderList, Map<String, Object> kctdiInfo, List<String> errList,
			int docSeqNo, int masterCnt, int fileSeq) throws Exception {
		String fileCreateRetVal[] = null;	// 파일 생성후 결과값, [0]:파일명, [1]:로컬파일경로, [2]:파일내용
		
		List<Map<String, Object>> insertPodList = new ArrayList<Map<String, Object>>();
		//////////////////////////////////////////
		// 파일 생성
		//////////////////////////////////////////
		if(Util.null2str(masterOrder.get("BOUND")).equals("AIR")){
			masterOrder.put("EDI_CUSTOMER_CODE", kctdiInfo.get("EDI_CUSTOMER_CODE"));
			fileCreateRetVal = this.getSendDataFileCreateAir(docSend, masterOrder, houseOrderList, docSeqNo, insertPodList, masterCnt, fileSeq);
		}else{
			fileCreateRetVal = this.getSendDataFileCreateSea(docSend, masterOrder, houseOrderList, kctdiInfo, docSeqNo, insertPodList, masterCnt, fileSeq);
		}
		
		IndexApi indexApi = new IndexApi(client);
		DocumentApi documentApi = new DocumentApi(client);
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
		String outFileName;
		
		//////////////////////////////////////////
		// 파일 전송
		//////////////////////////////////////////
		String logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + masterCnt;
		if ("Y".equals(customProperties.getEdiSendYn())) {
//			try {
				if(!ediCommonService.ftpFileSendGw(docSend, masterOrder, fileCreateRetVal, kctdiInfo, errList)){
					outFileName = ediCommonService.moveFile(fileCreateRetVal[0], fileCreateRetVal[0], fileCreateRetVal, false);
					
					/** jbs.g1에만 반영     전송실패해도 로그남기기위해 임시처리 */
//					tmpMap.put("ent_code", docSend.get("entCode"));
//					tmpMap.put("customer_code", "");
//					tmpMap.put("solution", "ECM");
//					tmpMap.put("bound", masterOrder.get("BOUND"));
//					tmpMap.put("bound_type", masterOrder.get("BOUND_TYPE"));
//					tmpMap.put("hbl_no", "");
//					tmpMap.put("if_service_code", "GW");
//					tmpMap.put("if_doc_code", "DESIGNATED_WAREHOUSE");
//					tmpMap.put("if_doc_name", "지정장치장");
//					tmpMap.put("if_doc_reference_no", fileCreateRetVal[5]);
//					tmpMap.put("if_vender", "GW");
//					tmpMap.put("if_send_recv_type", "S");
//					tmpMap.put("if_send_recv_method", "FTP");
//					tmpMap.put("if_file_path", fileCreateRetVal[3]);
//					tmpMap.put("if_file_name", outFileName);
//					tmpMap.put("if_status", "ERROR");
//					tmpMap.put("if_status_info", "");
//					tmpMap.put("if_sender_id", "");
//					tmpMap.put("if_receiver_id", "");
//					tmpMap.put("if_date", DateTimeUtil.getNow("yyyy-MM-dd"));
//					tmpMap.put("if_date_time", DateTimeUtil.getNow("HH:mm:ss"));
//					if(Util.null2str(docSend.get("cancelYn")).equals("Y")){
//						tmpMap.put("if_remark", "지정창고 반입취소전송");
//					}else{
//						tmpMap.put("if_remark", "지정창고 반입전송");
//					}
//					tmpMap.put("register_user_id", docSend.get("registerUserId"));
//					tmpMap.put("register_user_name", docSend.get("registerUserName"));
//					tmpMap.put("register_date_time", System.currentTimeMillis());
//					tmpMap.put("es_job_no", logId);
//						
//					if (!indexApi.existIndex(index))	indexApi.createIndex(index);
//					documentApi.createDocument(index, logId, tmpMap);
					
					return false;
				}
//			}catch(Exception e){
//				Map<String, Object> errorMap = new HashMap<String, Object>();
//				errorMap.putAll(docSend);
//				errorMap.putAll(kctdiInfo);
//				errorMap.put("fileName", fileCreateRetVal[0]);
//				ediCommonService.asyncErrorSendEmail("EdiManifestGwSendService.getManifestGwSend", errorMap, e);
//			}
			
//			/** FTP전송오류 발생해서 데몬에서 처리 */
//			Map<String, Object> tmpMap = new HashMap<String, Object>();
//			String index = "ecm-gw-send-" + Util.null2str(docSend.get("entCode")).toLowerCase();
//			tmpMap.put("user_id", docSend.get("registerUserId"));
//			tmpMap.put("user_name", docSend.get("registerUserName"));
//			tmpMap.put("cancel_yn", docSend.get("cancelYn"));
//			tmpMap.put("mbl_no", masterOrder.get("MBL_NO"));
//			tmpMap.put("master_od_job_no", masterOrder.get("MASTER_OD_JOB_NO"));
//			tmpMap.put("bound", masterOrder.get("BOUND"));
//			tmpMap.put("bound_type", masterOrder.get("BOUND_TYPE"));
//			tmpMap.put("file_name", fileCreateRetVal[0]);
//			tmpMap.put("file_path", fileCreateRetVal[2]);
//			tmpMap.put("file_path_backup", fileCreateRetVal[3]);
//			tmpMap.put("doc_reference_no", fileCreateRetVal[5]);
//			tmpMap.put("ftp_ip", kctdiInfo.get("EDI_FTP_IP"));
//			tmpMap.put("ftp_port", kctdiInfo.get("EDI_FTP_PORT"));
//			tmpMap.put("ftp_root", kctdiInfo.get("EDI_FTP_ROOT"));
//			tmpMap.put("ftp_id", kctdiInfo.get("EDI_FTP_ID"));
//			tmpMap.put("ftp_pw", kctdiInfo.get("EDI_FTP_PW"));
//			tmpMap.put("es_job_no", logId);
//			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
//			documentApi.createDocument(index, logId, tmpMap);
		}
		
		SqlSession sqlSession = null;
		
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			ManifestMapper manifestBatchMapper = sqlSession.getMapper(ManifestMapper.class);
			
			Map<String, Object> podMap = new HashMap<String, Object>();
			podMap.put("userLanguage", docSend.get("userLanguage"));
			podMap.put("srchGroupCode", "STATUS_ORDER_CARGO");
			podMap.put("srchCode", "ECM140");
			podMap.put("entCode", docSend.get("entCode"));
			podMap.put("entBusinessCode", "");
			podMap.put("gmtTime", docSend.get("gmtTime"));
			podMap.put("solution", "ECM");
			podMap.put("updateUserId", docSend.get("updateUserId"));
			podMap.put("stockType", "POD");
			podMap.put("stockSign", "POD");
			podMap.put("stockSeqLength", 8);
			podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			podMap.put("stockRemark", "ECM POD JOB NO");
			podMap.put("podStatus", "ECM140");
			podMap.put("podTableName", 	"TB_ECM_POD_02");
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(podMap, "podTableName|gmtTime", false, null, null, null, null);
			
			Map<String, Object> tmpInsertPodMap = new HashMap<String, Object>();
			tmpInsertPodMap.put("odJobNo", masterOrder.get("OD_JOB_NO"));
			tmpInsertPodMap.put("blNo", masterOrder.get("MBL_NO"));
			tmpInsertPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(masterCnt));
			tmpInsertPodMap.put("updateBlTypeMh", "M");
			insertPodList.add(tmpInsertPodMap);
			
			int cnt = 1;
			if("Y".equals(docSend.get("cancelYn"))){
				for(Map<String, Object> insertPodMap : insertPodList){
					podMap.put("odJobNo", insertPodMap.get("odJobNo"));
					orderBatchMapper.deleteOrderPod(podMap);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
			}else{
				/** 하우스선택 후 지정창고 전송시 해당 마스터 지정창고전송POD삭제     아래 반복문에서 다시 저장됨 */
				podMap.put("odJobNo", tmpInsertPodMap.get("odJobNo"));
				orderBatchMapper.deleteOrderPod(podMap);
				
				for(Map<String, Object> insertPodMap : insertPodList){
					podMap.put("odJobNo", insertPodMap.get("odJobNo"));
					podMap.put("blNo", insertPodMap.get("blNo"));
					podMap.put("podJobNo", insertPodMap.get("podJobNo"));
					orderBatchMapper.insertOrderPod(podMap);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
			}
			
			/** 관우회전송정보업데이트 */
			cnt = 1;
			for(Map<String, Object> insertPodMap : insertPodList){
				insertPodMap.put("cancelYn", docSend.get("cancelYn"));
				insertPodMap.put("gmtTime", docSend.get("gmtTime"));
				insertPodMap.put("updateUserId", docSend.get("updateUserId"));
				insertPodMap.put("masterOdJobNo", masterOrder.get("OD_JOB_NO"));
				insertPodMap.put("esJobNo", logId);
				manifestBatchMapper.updateManifestGwSend(insertPodMap);
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			if(!"Y".equals(docSend.get("cancelYn"))){
				List<Map<String, Object>> updateInspectionSeqNoList = manifestBatchMapper.selectInspectionSeqNo(podMap);
				if(updateInspectionSeqNoList != null && updateInspectionSeqNoList.size() > 0){
					cnt = 1;
					for(Map<String, Object> map : updateInspectionSeqNoList){
						manifestBatchMapper.updateInspectionSeqNo(map);
						if( (cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
				}
			}
			
			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
		} catch(Exception e) {
			throw e;
		} finally {
			sqlSession.close();
		}
		
		String key = "";
		if(Util.null2str(docSend.get("cancelYn")).equals("Y")){
			key = "E-COMMERCE_CARGO_PROGRESS_DELETE";
		}else{
			key = "E-COMMERCE_CARGO_PROGRESS_INSERT";
		}
		ListOperations<String, String> listOperations = redisTemplate.opsForList();
		JSONObject json = new JSONObject();
		for(Map<String, Object> houseOrder : houseOrderList){
			// 2024-11-18 통관타입이 목록이면서, 제출번호가 없는 경우에도 redis에 넣도록 수정
			// 2025-02-13 통관타입이 목록이면서, 제출번호가 있으면서 목록신고 이력이 없는 경우 redis에 넣도록 수정
			if(!"Y".equals(houseOrder.get("CD_TYPE")) || ("Y".equals(houseOrder.get("CD_TYPE")) && "".equals(Util.null2str(houseOrder.get("SIMPLE_DOC_NO"))))
						|| ("Y".equals(houseOrder.get("CD_TYPE")) && !"".equals(Util.null2str(houseOrder.get("SIMPLE_DOC_NO"))) && "".equals(Util.null2str(houseOrder.get("SIMPLE_EDI_STATUS"))))){
				json.put("entCode", houseOrder.get("ENT_CODE"));
				json.put("entBusinessCode", houseOrder.get("ENT_BUSINESS_CODE"));
				json.put("bound", houseOrder.get("BOUND"));
				json.put("boundType", houseOrder.get("BOUND_TYPE"));
				json.put("customs", houseOrder.get("CUSTOMS_CODE"));
				json.put("status", houseOrder.get("LAST_POD_STATUS"));
				json.put("odJobNo", houseOrder.get("OD_JOB_NO"));
				json.put("masterOdJobNo", houseOrder.get("MASTER_OD_JOB_NO"));
				json.put("mblNo", houseOrder.get("MBL_NO"));
				json.put("hblNo", houseOrder.get("HBL_NO"));
				json.put("arDate", Util.null2str(houseOrder.get("AR_DATE")));
				json.put("dropYn", houseOrder.get("DROP_YN"));
				json.put("cdType", houseOrder.get("CD_TYPE"));
				json.put("destCode", houseOrder.get("DEST_CODE"));
				json.put("destPort", houseOrder.get("DEST_PORT"));
				json.put("bondedCargoYn", masterOrder.get("bondedCargoYn"));
				json.put("action", "sendGw");
				json.put("mrn", masterOrder.get("MRN"));
				listOperations.rightPush(key, json.toString());
			}
		}
		
		outFileName = ediCommonService.moveFile(fileCreateRetVal[0], fileCreateRetVal[0], fileCreateRetVal, false);
		
		/** 처리 로그 기록 */
		tmpMap.put("ent_code", docSend.get("entCode"));
		tmpMap.put("customer_code", "");
		tmpMap.put("solution", "ECM");
		tmpMap.put("bound", masterOrder.get("BOUND"));
		tmpMap.put("bound_type", masterOrder.get("BOUND_TYPE"));
		tmpMap.put("hbl_no", "");
		tmpMap.put("if_service_code", "GW");
//		tmpMap.put("if_doc_code", fileCreateRetVal[4]);
		tmpMap.put("if_doc_code", "DESIGNATED_WAREHOUSE");
		tmpMap.put("if_doc_name", "지정장치장");
		tmpMap.put("if_doc_reference_no", fileCreateRetVal[5]);
		tmpMap.put("if_vender", "GW");
		tmpMap.put("if_send_recv_type", "S");
		tmpMap.put("if_send_recv_method", "FTP");
		tmpMap.put("if_file_path", fileCreateRetVal[3]);
		tmpMap.put("if_file_name", outFileName);
		tmpMap.put("if_status", "SEND");
		tmpMap.put("if_status_info", "");
		tmpMap.put("if_sender_id", "");
		tmpMap.put("if_receiver_id", "");
		tmpMap.put("if_date", DateTimeUtil.getNow("yyyy-MM-dd"));
		tmpMap.put("if_date_time", DateTimeUtil.getNow("HH:mm:ss"));
		if(Util.null2str(docSend.get("cancelYn")).equals("Y")){
			tmpMap.put("if_remark", "지정창고 반입취소전송");
		}else{
			tmpMap.put("if_remark", "지정창고 반입전송");
		}
		tmpMap.put("register_user_id", docSend.get("registerUserId"));
		tmpMap.put("register_user_name", docSend.get("registerUserName"));
		tmpMap.put("register_date_time", System.currentTimeMillis());
		tmpMap.put("es_job_no", logId);
			
		if (!indexApi.existIndex(index))	indexApi.createIndex(index);
		documentApi.createDocument(index, logId, tmpMap);
		
		return true;
	}
	
	/** 관우회 항공 파일생성 */
	public String[] getSendDataFileCreateAir(Map<String, Object> docSend, Map<String, Object> masterOrder, List<Map<String, Object>> houseOrderList, int docSeqNo,
			List<Map<String, Object>> insertPodList, int masterCnt, int fileSeq) throws java.lang.Exception {
		int seq = docSeqNo + 1;
		BufferedWriter bfw = null;
		StringBuffer writeData = null;
		String retData[] = new String[6];
		String nowDateTime = DateTimeUtil.addTimes(0, "yyyyMMddHHmmss");
		
		String fileName = "K002_" + Util.null2str(docSend.get("b2gForwarderSign")) + nowDateTime + "_" + fileSeq + ".txt";
		String dirPath[] = ediCommonService.getSendFilePath(null, "edi/gw", docSend);	// 로컬폴더 경로 가져오기
		
		// 파일 내용 쓰기
		try {
			
			bfw = ediCommonService.getFileWriterOpen(fileName, dirPath);	// 쓰기 파일 열기
			writeData = new StringBuffer();
			String strType = null;
			String arDate = Util.null2str(masterOrder.get("AR_DATE")).replace("-", "");
			String arTime = Util.null2str(masterOrder.get("AR_TIME")).replace(":", "");
			if(arTime.equals("")){	//기존소스 if (EtcLibrary.getSubStringK(strARTime,0,2) == "00")?
				arDate += nowDateTime.substring(8);
			}else{
				arDate += arTime + nowDateTime.substring(12);
			}
			
			
			/** 마스터 POD저장안함 */
//			podMap.put("odJobNo", masterOrder.get("OD_JOB_NO"));
//			podMap.put("blNo", masterOrder.get("MBL_NO"));
//			podMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5));
//			orderBatchMapper.insertOrderPod(podMap);
			
			Map<String, Object> tmpInsertPodMap = null;
			
			String yyyy = Util.null2str(docSend.get("yyyy"));
			int stockStartNo = Util.null2int(docSend.get("stockStartNo"));
			for(Map<String, Object> houseOrder : houseOrderList){
				writeData.setLength(0);
				if(Util.null2str(masterOrder.get("CUSTOMS_CODE")).equals("041")){
					writeData.append("10610");	//사무소코드
				}else{
					writeData.append("11110");
				}
				writeData.append(yyyy);	//반입번호(년도)
				writeData.append(Util.lpad(Util.null2str(stockStartNo++), 8, '0'));	//반입번호(일련번호)
				writeData.append(Util.rpad(ediCommonService.getSubStringK(Util.null2str(houseOrder.get("MRN")), 0, 11), 11));	//MRN
				writeData.append(Util.lpad(Util.null2str(houseOrder.get("MSN")), 4, '0'));	//MSN
				writeData.append(Util.lpad(Util.null2str(houseOrder.get("HSN")), 4, '0'));	//HSN
				writeData.append("14");	//화물구분코드
				writeData.append(nowDateTime);	//반입예정일자
				writeData.append(Util.rpad(ediCommonService.getSubStringK(Util.null2str(masterOrder.get("CUSTOMS_CODE")), 0, 3), 3));	//입항세관
				
				if(Util.null2str(masterOrder.get("CUSTOMS_CODE")).equals("041")){
					writeData.append(Util.rpad(ediCommonService.getSubStringK(Util.null2str(masterOrder.get("EDI_CUSTOMER_CODE")), 0, 4), 4));	//화주코드
				}else{
					writeData.append(Util.rpad(Util.null2str(docSend.get("b2gForwarderSign")), 4));
				}
				writeData.append(Util.rpad(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(docSend.get("entBusinessNameEng")), "1"), 100));
				if(Util.null2str(houseOrder.get("ALLOWED_ITEM_CODE")).equals("")){
					writeData.append("99");	//품목코드
				}else{
					writeData.append(Util.rpad(Util.null2str(houseOrder.get("ALLOWED_ITEM_CODE")), 2));
				}
				
				writeData.append(Util.rpad(ediCommonService.getSubStringK(Util.null2str(houseOrder.get("COMMODITY_ITEM")), 0, 200), 200));	//품명
				writeData.append(Util.rpad(ediCommonService.getDeleteAlphabetChar(ediCommonService.getSubStringK(Util.null2str(houseOrder.get("MBL_NO")), 0, 11)), 20));	//M B/L
				writeData.append(Util.rpad(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("HBL_NO")).replace("-",""), "1"), 20));	//H B/L
				
				/** LINE_CODE + VESSEL_FLIGHT에서 VESSEL_FLIGHT로 수정 */
//				if ((Util.null2str(houseOrder.get("LINE_CODE")) + Util.null2str(houseOrder.get("VESSEL_FLIGHT"))).length() > 35){
//					writeData.append(ediCommonService.GetReplaceETCCharToSpace((Util.null2str(houseOrder.get("LINE_CODE")) + Util.null2str(houseOrder.get("VESSEL_FLIGHT"))).substring(0, 35), "1"));	//선명(기명)
//				}else {
//					writeData.append(Util.rpad(ediCommonService.GetReplaceETCCharToSpace((Util.null2str(houseOrder.get("LINE_CODE")) + Util.null2str(houseOrder.get("VESSEL_FLIGHT"))), "1"), 35));
//				}
				if (Util.null2str(houseOrder.get("VESSEL_FLIGHT")).length() > 35){
					writeData.append(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("VESSEL_FLIGHT")).substring(0, 35), "1"));	//선명(기명)
				}else {
					writeData.append(Util.rpad(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("VESSEL_FLIGHT")), "1"), 35));
				}
				
				
				if (Util.null2str(houseOrder.get("BL_TYPE_IERT")).equals("T")){
					writeData.append("42");	//반입구분코드
				}else{
					writeData.append("10");
				}
				writeData.append(Util.rpad("", 8));		// 보세운송기간(From)
				writeData.append(Util.rpad("", 8));		// 보세운송기간(to)
                writeData.append(Util.rpad(Util.null2str(houseOrder.get("MUL_DELIVERY_NO")), 20));	// 보세운송 면허번호 멀티화물의 첫번째 DeliveryNo
				writeData.append(Util.rpad("", 20));	// 보세운송회사
				writeData.append(Util.rpad("CT", 3));	// 포장종류
				writeData.append(Util.rpad("OKY", 3));	// 반입사고구분코드
				writeData.append(Util.lpad(Util.null2str(houseOrder.get("PIECES")), 8, '0'));	//반입 수량
				writeData.append("00000000");	// 반출 수량
				writeData.append("00000000");	// 재고 수량
				if(Util.null2dbl(houseOrder.get("GWT")) > 999999.999){
					writeData.append("999999.999");	//반입 중량
				}else{
					writeData.append(Util.lpad(Util.null2str(houseOrder.get("GWT")), 10, '0'));
				}
				writeData.append("000000.000");	// 반출 중량
				writeData.append("000000.000");	// 재고 중량
				writeData.append("000000");	//반입 용적
				writeData.append("000000");	// 반출 용적
				writeData.append("000000");	// 재고 용적
				writeData.append(Util.rpad(Util.null2str(masterOrder.get("EDI_WH_LOCATION")), 10));	//장치위치
				writeData.append(Util.rpad("", 8));	// 장치확인일자
				writeData.append(Util.rpad("", 10));	// 장치확인자
				writeData.append("0");	// 체화여부(1:체화, 0:정상)
				writeData.append(Util.rpad("", 10));	// 체화번호
				writeData.append(Util.rpad("", 8));	// 체화일자
				if(Util.null2str(docSend.get("cancelYn")).equals("Y")){
					writeData.append("D");	//데이터 생성 구분
				}else{
					/** 생성/정정 구분하지않고 무조건 신규로 전송하기로함 2021-12 */
//					if(Util.null2str(houseOrder.get("CARRYIN_DATE")).equals("") || Util.null2str(houseOrder.get("CARRYIN_DATE")).equals("1900-01-01")) {	//생성/정정 구분체크  CARRYIN_DATE로 
						writeData.append("I");
//					}else {
//						writeData.append("U");
//					}
				}
				writeData.append(Util.rpad("", 10));	//사용자코드
				writeData.append(Util.rpad("", 10));	//비밀번호
				writeData.append(nowDateTime.substring(0, 8));	//작업일자
				writeData.append(Util.rpad("", 5));	//단말기번호
				if(Util.null2str(houseOrder.get("ECM_TYPE")).equals("A") || Util.null2str(houseOrder.get("ECM_TYPE")).equals("B")){	//2021-11-25현재 공통코드 B(기업) 사용안함
					writeData.append("1");	//전자상거래여부
				}else{
					writeData.append("0");
				}
				
				/**
				 * 1:목록 2: 검사(목록) 3:간이 4:일반 5:검사(일반/간이) 6:검역(사전검역 요청건) 7:환적 8: 검역(동물검역) 9: 검역(식물검역) 10: 검역(식품검역) 11:검역(서류), 91.거대화물
				 * 13: 개인통관부호 미제출
				 */
				if(Util.isEmpty(houseOrder.get("INSPECTION_TYPE"))){
					if(Util.null2str(houseOrder.get("CD_TYPE")).equals("B")){	/** 배제 */
						strType = "3";
					}else if(Util.null2str(houseOrder.get("CD_TYPE")).equals("N")){	/** 일반 */
						strType = "4";
					}else{	/** 목록 */
						if("Y".equals(houseOrder.get("INSPECTION_YN")) || "Y".equals(houseOrder.get("SE_INSPECTION_YN")) || "Y".equals(houseOrder.get("DROP_YN"))){	/** 검사 or 선별 or 취하 */
							strType = "2";
						}else if("A".equals(houseOrder.get("CUSTOMS_ID_TYPE")) && Util.isEmpty(houseOrder.get("CUSTOMS_ID"))){	/** 코드구분이 A.개인통관코드이고  코드값이 없을경우  */
							strType = "13";
						}else{
							strType = "1";
						}
					}
				}else{
					strType = Util.null2str(houseOrder.get("INSPECTION_TYPE"));
				}
				
				writeData.append(Util.rpad(strType, 2));	//목록구분(반입유형)
				writeData.append(" ");	//특수화물 구분
				writeData.append(nowDateTime);	//조업일자
				writeData.append(arDate);	// 입항일자
				writeData.append(Util.lpad(Util.null2str((int)Math.floor(Util.null2dbl(houseOrder.get("AMOUNT_USD")))), 18, '0'));	// 감가 정수표현
				writeData.append(Util.lpad("", 18, '0'));	//관세
				writeData.append(Util.rpad(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(masterOrder.get("ORIG_CODE")), "1"), 5));	// 적재항
				writeData.append(Util.rpad(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("SHIPPER_COUNTRY_CODE")), "1"), 2));	// 발송국
				writeData.append(Util.rpad(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("SHIPPER_NAME")), "1"), 100));	// 송하인명
				writeData.append(Util.rpad(ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("SHIPPER_ADDRESS")), "1"), 0, 50), 50));	// 송하인주소1
				writeData.append(Util.rpad(ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("SHIPPER_ADDRESS")), "1"), 50, 100), 50));	// 송하인주소2
				writeData.append(Util.rpad(ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("SHIPPER_ADDRESS")), "1"), 100, 150), 50));	// 송하인주소3
				writeData.append(Util.rpad(ediCommonService.getSubStringK(ediCommonService.getDeleteAlphabetChar(Util.null2str(houseOrder.get("CONSIGNEE_TEL"))), 0, 40), 40));	// 수하인 전화번호
				writeData.append(Util.rpad(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("CONSIGNEE_NAME")), "1"), 100));	// 수하인 명
				writeData.append(Util.rpad(ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS")), "1"), 0, 50), 50));	// 수하인주소1
				writeData.append(Util.rpad(ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS")), "1"), 50, 100), 50));	// 수하인주소2
				writeData.append(Util.rpad(ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS")), "1"), 100, 150), 50));	// 수하인주소3
				writeData.append(Util.lpad(ediCommonService.getSubStringK(ediCommonService.getDeleteAlphabetChar(Util.null2str(houseOrder.get("CONSIGNEE_ZIPCODE"))), 0, 5), 5,'0'));	// 수하인 우편번호
				writeData.append(Util.rpad(ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("URL")).replace("'", ""), "2"), 0 , 100), 100));	// 전자상거래 사이트주소
				writeData.append(Util.lpad(String.format("%.2f",Util.null2dbl(houseOrder.get("AMOUNT_USD"))), 18, '0'));	// 물품가격 소수점 아래 두자리까지 표현
				writeData.append(Util.rpad("US", 2));	// 물품가격 단위
				writeData.append(Util.rpad("", 10)); //특별통관대상업체 지정번호
				writeData.append(Util.rpad(Util.null2str(houseOrder.get("ECM_TYPE")), 1));	// 거래유형코드
				writeData.append(Util.rpad(Util.null2str(houseOrder.get("CD_USE_TYPE")), 1));	// 용도구분
				writeData.append(Util.rpad("", 11));	// 컨테이너 번호1
				writeData.append(Util.rpad("", 11));	// 컨테이너 번호2
				writeData.append(Util.rpad(Util.null2str(docSend.get("b2gForwarderSign")), 4));	//포더부호
				writeData.append(Util.rpad(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(docSend.get("entBusinessNameEng")), "1"), 100));	//포더명(특송업체명)
				switch (Util.null2str(houseOrder.get("SE_INSPECTION_CODE"))){
					case "01":
						writeData.append("1");	//UNI-PASS 검사지정사유(null:검사미지정 / 1:마약 / 2:총기 / 3:검역 / 4:식품 / 5:의약품 / 6:수입검사 / 7:AI 고위험)
						break;
					case "02":
						writeData.append("2");
						break;
					case "03":
						writeData.append("3");
						break;
					case "04":
						writeData.append("4");
						break;
					case "05":
						writeData.append("5");
						break;
					case "06":
					case "C":
						writeData.append("6");
						break;
					case "07":
						writeData.append("7");
						break;
					default:
						writeData.append(" ");
						break;
				}
				if(!"".equals(Util.null2str(houseOrder.get("CARRYIN_NO")))) {
					writeData.append(Util.rpad(Util.null2str(houseOrder.get("CARRYIN_NO")), 10));	//택배사번호
				}else {
					writeData.append(Util.rpad(Util.null2str(houseOrder.get("DELIVERY_COMPANY_NO")), 10));	//택배사번호
				}
				if("Y".equals(houseOrder.get("CFA_HOLD_YN"))){
					writeData.append(Util.rpad(Util.null2str(houseOrder.get("DELIVERY_LOC_NO")), 10));	//택배집하장번호
				}else{
					writeData.append(Util.rpad("", 10));	//택배집하장번호
				}
				writeData.append(Util.rpad("", 2));	//특이사항
				writeData.append("*");	//EOF
				writeData.append("\r\n");
				bfw.write(writeData.toString());
				
				tmpInsertPodMap = new HashMap<String, Object>();
				tmpInsertPodMap.put("odJobNo", houseOrder.get("OD_JOB_NO"));
				tmpInsertPodMap.put("blNo", houseOrder.get("HBL_NO"));
				tmpInsertPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				tmpInsertPodMap.put("updateBlTypeMh", "H");
				insertPodList.add(tmpInsertPodMap);
			}	//하우스반복 END
			docSend.put("stockStartNo", stockStartNo);
			
			
		} catch(Exception e) {
			throw e;
		} finally {
			if (bfw != null) {
				try{ bfw.close(); }catch(Exception e){log.error(Util.getPrintStacTraceString(e));}
			}
		}
		
		retData[0] = fileName;	// 파일명
		retData[2] = dirPath[0];	// 원본 로컬 경로
		retData[3] = dirPath[1];	// 백업 로컬 경로
		retData[4] = "MANIFEST_GW_AIR";
		retData[5] = Util.null2str(masterOrder.get("MBL_NO"));
		
		return retData;
	}
	
	/** 관우회 해상 파일생성 */
	public String[] getSendDataFileCreateSea(Map<String, Object> docSend, Map<String, Object> masterOrder, List<Map<String, Object>> houseOrderList, Map<String, Object> kctdiInfo, int docSeqNo,
			List<Map<String, Object>> insertPodList, int masterCnt, int fileSeq) throws Exception {
		int seq = docSeqNo + 1;
		BufferedWriter bfw = null;
		StringBuffer writeData = null;
		String retData[] = new String[6];
		String nowDateTime = DateTimeUtil.addTimes(0, "yyyyMMddHHmmss");
		
		/** destCode : DEST_CODE => DEST_PORT로 변경 */
		String destCode = Util.null2str(masterOrder.get("DEST_PORT"), "KRINC");
		String part, cusCd1;
		if(destCode.equals("KRPTK") || destCode.equals("KRPTH")){	/** 평택항 */
			part = "11210";
			cusCd1 = "016";
		}else if(destCode.equals("KRPUS") || destCode.equals("KRBUS")){	/** 부산항 */
			part = "10910";
			cusCd1 = "039";
		}else if(destCode.equals("KRKUV")){	/** 군산항 */
			part = "12250";
			cusCd1 = "080";
		}else{	/** 인천항 */
			part = "10710";
			cusCd1 = "020";
		}
		
//		String fileName = Util.null2str(masterOrder.get("MBL_NO")) + nowDateTime.substring(2) + docSeqNo + ".txt";
		String fileName = Util.null2str(docSend.get("b2gForwarderSign")) + Util.null2str(kctdiInfo.get("EDI_FTP_ID")) + nowDateTime.substring(2) + "_" + fileSeq + ".txt";
		
//		if(cusCd1.equals("020")){
		if("10710".equals(part)){
			fileName = "K002_" + fileName;
		}
		String dirPath[] = ediCommonService.getSendFilePath(null, "edi/gw", docSend);	// 로컬폴더 경로 가져오기
		
		// 파일 내용 쓰기
		try {
			bfw = ediCommonService.getFileWriterOpen(fileName, dirPath);	// 쓰기 파일 열기
			writeData = new StringBuffer();
			String strType = "";
			String cntrNo1 = null;
			String cntrNo2 = null;
			
			/** 마스터 POD저장안함 */
//			podMap.put("odJobNo", masterOrder.get("OD_JOB_NO"));
//			podMap.put("blNo", masterOrder.get("MBL_NO"));
//			podMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5));
//			orderBatchMapper.insertOrderPod(podMap);
			
			Map<String, Object> tmpInsertPodMap = null;
			
			double cbm = 0;
			
			String yyyy = Util.null2str(docSend.get("yyyy"));
			int stockStartNo = Util.null2int(docSend.get("stockStartNo"));
			
			for(Map<String, Object> houseOrder : houseOrderList){
				writeData.setLength(0);
				cntrNo1 = Util.null2str(houseOrder.get("CNTR_NO1"));
				cntrNo2 = Util.null2str(houseOrder.get("CNTR_NO2"));
				if(cntrNo1.equals(cntrNo2)){
					cntrNo2 = "";
				}
				
				writeData.append(part);	//사무소코드
				writeData.append(yyyy);	//반입번호(년도)
				writeData.append(Util.lpad(Util.null2str(stockStartNo++), 8, '0'));	//반입번호(일련번호)
				writeData.append(Util.rpad(ediCommonService.getSubStringK(Util.null2str(houseOrder.get("MRN")), 0, 11), 11));	//MRN
				writeData.append(Util.lpad(Util.null2str(houseOrder.get("MSN")), 4, '0'));	//MSN
				writeData.append(Util.lpad(Util.null2str(houseOrder.get("HSN")), 4, '0'));	//HSN
				writeData.append("14");	//화물구분 코드
				writeData.append(nowDateTime);	//반입일자
				writeData.append(cusCd1);	//입항세관
				writeData.append(Util.rpad(ediCommonService.getSubStringK(Util.null2str(kctdiInfo.get("EDI_CUSTOMER_CODE")), 0, 4), 4));	//화주코드
				writeData.append(Util.rpad(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("CONSIGNEE_NAME")), "1").replace("*", ""), 100));	//화주명
				if(Util.null2str(houseOrder.get("ALLOWED_ITEM_CODE")).length() != 2){
					writeData.append("99");	//품목코드
				}else{
					writeData.append(Util.rpad(Util.null2str(houseOrder.get("ALLOWED_ITEM_CODE")), 2));
				}
				
//				if(cusCd1.equals("020")){
				if("10710".equals(part)){
					writeData.append(Util.rpad(ediCommonService.getSubStringK(Util.null2str(houseOrder.get("COMMODITY_ITEM")), 0, 200), 200));	//품명
				}else{
					writeData.append(Util.rpad(ediCommonService.getSubStringK(Util.null2str(houseOrder.get("COMMODITY_ITEM")), 0, 20), 20));	//품명
				}
				writeData.append(Util.rpad(ediCommonService.GetBLNo_ZSeqDelete(Util.null2str(houseOrder.get("MBL_NO"))), 20));	//M B/L
				writeData.append(Util.rpad(Util.null2str(houseOrder.get("HBL_NO")).replace("-",""), 20));	//H B/L
				
				//선명(기명) = vessel_flight(선명) + "  " + voyage(항차) 표현 ex."HANSUNG INCHEON  2910E"
				if ((Util.null2str(houseOrder.get("VESSEL_FLIGHT")) + "  " + Util.null2str(masterOrder.get("VOYAGE"))).length() > 35){
					writeData.append((ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("VESSEL_FLIGHT")), "8") + "  " + ediCommonService.GetReplaceETCCharToSpace(Util.null2str(masterOrder.get("VOYAGE")), "8")).substring(0, 35));	//선명(기명)
				}else {
					writeData.append(Util.rpad(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("VESSEL_FLIGHT")), "8") + "  " + ediCommonService.GetReplaceETCCharToSpace(Util.null2str(masterOrder.get("VOYAGE")), "8"), 35));
				}
				
				if (Util.null2str(houseOrder.get("BL_TYPE_IERT")).equals("T")){
					writeData.append("42");	//반입구분코드
				}else if(!Util.null2str(houseOrder.get("CARRYIN_NO")).equals("")){
					writeData.append("20");
				}else{
					writeData.append("10");
				}
				writeData.append(Util.rpad("", 8));		// 보세운송기간(From)
				writeData.append(Util.rpad("", 8));		// 보세운송기간(to)
				writeData.append(Util.rpad(Util.null2str(houseOrder.get("CARRYIN_NO")), 20));	// 보세운송 면허번호
				writeData.append(Util.rpad(Util.null2str(houseOrder.get("SIMPLE_DOC_NO")), 20));// 보세운송회사(인천항, 평택항, 군산항, 부산항은 통관목록제출번호로 전송함)
				writeData.append(Util.rpad("CT", 3));	// 포장종류
				writeData.append(Util.rpad("OK", 3));	// 반입사고구분코드
				writeData.append(Util.lpad(Util.null2str(houseOrder.get("PIECES")), 8, '0'));	//반입 수량
				writeData.append("00000000");	// 반출 수량
				writeData.append("00000000");	// 재고 수량
				if(Util.null2dbl(houseOrder.get("GWT")) > 999999.999){
					writeData.append("999999.999");	//반입 중량
				}else{
					writeData.append(Util.lpad(Util.null2str(houseOrder.get("GWT")), 10, '0'));
				}
				writeData.append("000000.000");	// 반출 중량
				writeData.append("000000.000");	// 재고 중량
				cbm = Util.null2dbl(houseOrder.get("CBM"));
				if(cbm > 999999){
					writeData.append("999999");	//반입 용적
				}else{
					writeData.append(Util.lpad(Util.null2str(houseOrder.get("CBM")) != "" ? houseOrder.get("CBM").toString().split("\\.")[0]+houseOrder.get("CBM").toString().split("\\.")[1] : "", 6, '0'));
				}
				writeData.append("000000");	// 반출 용적
				writeData.append("000000");	// 재고 용적
				writeData.append(Util.rpad(ediCommonService.getSubStringK(Util.null2str(kctdiInfo.get("EDI_WH_LOCATION")), 0, 10), 10));	//장치위치
				writeData.append(Util.rpad("", 8));	// 장치확인일자
				writeData.append(Util.rpad("", 10));	// 장치확인자
				writeData.append("0");	// 체화여부(1:체화, 0:정상)
				writeData.append(Util.rpad("", 10));	// 체화번호
				writeData.append(Util.rpad("", 8));	// 체화일자
				if(Util.null2str(docSend.get("cancelYn")).equals("Y")){
					writeData.append("D");	//데이터 생성 구분
				}else{
					/** 생성/정정 구분하지않고 무조건 신규로 전송하기로함 2021-12 */
//					if(Util.null2str(houseOrder.get("CARRYIN_DATE")).equals("") || Util.null2str(houseOrder.get("CARRYIN_DATE")).equals("1900-01-01")) {	//생성/정정 구분체크  CARRYIN_DATE로 
						writeData.append("I");
//					}else {
//						writeData.append("U");
//					}
				}
				writeData.append(Util.rpad("", 10));	//사용자코드
				writeData.append(Util.rpad("", 10));	//비밀번호
				writeData.append(nowDateTime.substring(0, 8));	//작업일자
				writeData.append(Util.rpad("", 5));	//단말기 번호
				if(Util.null2str(houseOrder.get("ECM_TYPE")).equals("A")){
					writeData.append("1");	//전자상거래 여부
				}else{
					writeData.append("0");
				}
				
//				if(cusCd1.equals("020")){
				if("10710".equals(part)){
					/**
					 * 1:목록 2: 검사(목록) 3:간이 4:일반 5:검사(일반/간이) 6:검역(사전검역 요청건) 7:환적 8: 검역(동물검역) 9: 검역(식물검역) 10: 검역(식품검역) 11:검역(서류), 91.거대화물
					 * 13: 개인통관부호 미제출
					 */
					if(Util.isEmpty(houseOrder.get("INSPECTION_TYPE"))){
						if(Util.null2str(houseOrder.get("CD_TYPE")).equals("B")){	/** 배제 */
							strType = "3";
						}else if(Util.null2str(houseOrder.get("CD_TYPE")).equals("N")){	/** 일반 */
							strType = "4";
						}else{	/** 목록 */
							if("Y".equals(houseOrder.get("INSPECTION_YN")) || "Y".equals(houseOrder.get("SE_INSPECTION_YN")) || "Y".equals(houseOrder.get("DROP_YN"))){	/** 검사 or 선별 or 취하 */
								strType = "2";
							}else if("A".equals(houseOrder.get("CUSTOMS_ID_TYPE")) && Util.isEmpty(houseOrder.get("CUSTOMS_ID"))){	/** 코드구분이 A.개인통관코드이고  코드값이 없을경우  */
								strType = "13";
							}else{
								strType = "1";
							}
						}
					}else{
						strType = Util.null2str(houseOrder.get("INSPECTION_TYPE"));
					}
					writeData.append(Util.rpad(strType, 2));	//목록구분(반입유형)
				}else{
					// 목록구분 (반입유형) - 0:일반  1:목록  2:검사  4:간이  5:보류   7:개인통관부호 미제출
					// TEDOI_HBL 의 SelectGoods 에 값이 있거나 CheckReqYN 가 Y 이면 2. 검사
					if(Util.null2str(houseOrder.get("DROP_YN")).equals("Y")){
						strType = "5";
					}else if(Util.null2str(houseOrder.get("SE_INSPECTION_YN")).equals("Y") || Util.null2str(houseOrder.get("INSPECTION_YN")).equals("Y")){
						strType = "2";
					}else if(!Util.null2str(houseOrder.get("HOLD_REASON")).equals("")){
						strType = "N";
					}else{
						//목록건이면서, 용도구분이 개인이면서 개인통관고유부호가 없는 건 은 7(개인통관부호 미제출) 로 제출한다.
						//0:일반, 1:목록, 2:검사, 4:간이, 5:보류, 7(개인통관부호 미제출)
						if (Util.null2str(houseOrder.get("CD_USE_TYPE")).equals("1") && Util.null2str(houseOrder.get("CD_TYPE")).equals("Y") && Util.null2str(houseOrder.get("CUSTOMS_ID_TYPE")).equals("A") && Util.null2str(houseOrder.get("CUSTOMS_ID")).equals("")){
							strType = "7";
						}else{
							//목록 구분에서 목록(CustomsType == Y)인 경우면 목록으로 한다.
							//목록 구분에서 목록이 아닌 경우 일반이면 일반 배제이면 금액체크 (CustomsType != Y) 2000$초과 : 일반 2000$이하 : 간이
							if(Util.null2str(houseOrder.get("CD_TYPE")).equals("Y")){
								strType = "1";
							}else{
								if(Util.null2str(houseOrder.get("CD_TYPE")).equals("N")){
									strType = "0";
								}else{
									if(Util.null2dbl(houseOrder.get("AMOUNT_USD")) > 2000){
										strType = "0";
									}else{
										strType = "4";
									}
								}
							}
						}
					}
					writeData.append(Util.rpad(strType, 1));	//목록구분(반입유형)
				}
				
				
				if("10710".equals(part)){
					// 특송장 검역일 경우 4번 아닐 경우 공백을 넣는다.
					// 사전검역 : 6 동물검역 : 8 식물검역 : 9 식품검역 : 10  서류검역 : 11
					if(!Util.isEmpty(houseOrder.get("INSPECTION_TYPE"))) {
						String insType = Util.null2str(houseOrder.get("INSPECTION_TYPE"));
						if("6".equals(insType) || "8".equals(insType) || "9".equals(insType) || "10".equals(insType) || "11".equals(insType)) {
							writeData.append("4");	//특수화물 구분
						}else {
							writeData.append(" ");	//특수화물 구분
						}
					}else {
						writeData.append(" ");	//특수화물 구분
					}
				}else {
					// 특송장 검역일 경우 
					// 사전검역,서류검역,식품검역 : 특수화물 값 4번 
					// 동물검역 : 특수화물 값 5번 
					// 식물검역 : 특수화물 값 6번
					// 그외 공백
					// INSPECTION_TYPE 값 사전검역 : 6 동물검역 : 8 식물검역 : 9 식품검역 : 10  서류검역 : 11
					if(!Util.isEmpty(houseOrder.get("INSPECTION_TYPE"))) {
						String insType = Util.null2str(houseOrder.get("INSPECTION_TYPE"));
						if("6".equals(insType) || "10".equals(insType) || "11".equals(insType)) {
							writeData.append("4");	//특수화물 구분
						}else if("8".equals(insType)) {
							writeData.append("5");	//특수화물 구분
						}else if("9".equals(insType)) {
							writeData.append("6");	//특수화물 구분
						}else {
							writeData.append(" ");	//특수화물 구분
						}
					}else {
						writeData.append(" ");	//특수화물 구분
					}
				}
				
				
				writeData.append(Util.null2str(masterOrder.get("AR_DATE_TIME")));	//조업일자
				writeData.append(Util.null2str(masterOrder.get("AR_DATE_TIME")));	//입항일자
				writeData.append(Util.lpad(Util.null2str((int)Math.round(Util.null2dbl(houseOrder.get("AMOUNT_USD")))), 18, '0'));	// 감가 정수표현
				writeData.append(Util.lpad("", 18, '0'));	//관세
				writeData.append(Util.rpad(Util.null2str(masterOrder.get("ORIG_CODE")), 5));	// 적재항
				writeData.append(Util.rpad(Util.null2str(houseOrder.get("SHIPPER_COUNTRY_CODE")), 2));	// 발송국
				writeData.append(Util.rpad(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("SHIPPER_NAME")).replace("*", ""), "1"), 100));	// 송하인명
				writeData.append(Util.rpad(ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("SHIPPER_ADDRESS")), "1"), 0, 50), 50));	// 송하인주소1
				writeData.append(Util.rpad(ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("SHIPPER_ADDRESS")), "1"), 50, 100), 50));	// 송하인주소2
				writeData.append(Util.rpad(ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("SHIPPER_ADDRESS")), "1"), 100, 150), 50));	// 송하인주소3
				writeData.append(Util.rpad(ediCommonService.getSubStringK(ediCommonService.getDeleteAlphabetChar(Util.null2str(houseOrder.get("CONSIGNEE_TEL"))), 0, 40), 40));	// 수하인 전화번호
				writeData.append(Util.rpad(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("CONSIGNEE_NAME")).replace("*", ""), "1"), 100));	// 수하인 명
				writeData.append(Util.rpad(ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS")), "1"), 0, 50), 50));	// 수하인주소1
				writeData.append(Util.rpad(ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS")), "1"), 50, 100), 50));	// 수하인주소2
				writeData.append(Util.rpad(ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("CONSIGNEE_ADDRESS")), "1"), 100, 150), 50));	// 수하인주소3
				writeData.append(Util.lpad(ediCommonService.getSubStringK(ediCommonService.getDeleteAlphabetChar(Util.null2str(houseOrder.get("CONSIGNEE_ZIPCODE"))), 0, 5), 5,'0'));	// 수하인 우편번호
				writeData.append(Util.rpad(ediCommonService.getSubStringK(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(houseOrder.get("URL")).replace("*", ""), "1"), 0 , 100), 100));	// 전자상거래 사이트주소
				writeData.append(Util.lpad(String.format("%.2f",Util.null2dbl(houseOrder.get("AMOUNT_USD"))), 18, '0'));	// 물품가격 소수점 아래 두자리까지 표현
				writeData.append(Util.rpad("US", 2));	// 물품가격 단위
				writeData.append(Util.rpad("", 10)); //지정번호
				writeData.append(Util.rpad(Util.null2str(houseOrder.get("ECM_TYPE")), 1));	// 거래유형코드
				writeData.append(Util.rpad(Util.null2str(houseOrder.get("CD_USE_TYPE")), 1));	// 용도구분
				writeData.append(Util.rpad(cntrNo1, 11));	// 컨테이너 번호1
				writeData.append(Util.rpad(cntrNo2, 11));	// 컨테이너 번호2
				
//				if(cusCd1.equals("020")){
				if("10710".equals(part)){
					writeData.append(Util.rpad(Util.null2str(docSend.get("b2gForwarderSign")), 4));	//포더부호
					writeData.append(Util.rpad(ediCommonService.GetReplaceETCCharToSpace(Util.null2str(docSend.get("entBusinessNameEng")), "1"), 100));	//포더명(특송업체명)
					switch (Util.null2str(houseOrder.get("SE_INSPECTION_CODE"))){
						case "01":
							writeData.append("1");	//UNI-PASS 검사지정사유(null:검사미지정 / 1:마약 / 2:총기 / 3:검역 / 4:식품 / 5:의약품 / 6:수입검사 / 7:AI 고위험 )
							break;
						case "02":
							writeData.append("2");
							break;
						case "03":
							writeData.append("3");
							break;
						case "04":
							writeData.append("4");
							break;
						case "05":
							writeData.append("5");
							break;
						case "06":
						case "C":
							writeData.append("6");
							break;
						case "07":
							writeData.append("7");
							break;
						default:
							writeData.append(" ");
							break;
					}
					
					writeData.append(Util.rpad(Util.null2str(houseOrder.get("DELIVERY_COMPANY_NO")), 10));	//택배사번호
					if("Y".equals(houseOrder.get("CFA_HOLD_YN"))){
						writeData.append(Util.rpad(Util.null2str(houseOrder.get("DELIVERY_LOC_NO")), 10));	//택배집하장번호
					}else{
						writeData.append(Util.rpad("", 10));	//택배집하장번호
					}
					writeData.append(Util.rpad("", 30));	//택배 송장번호
					writeData.append(Util.rpad("", 2));	//특이사항
				}
				
				writeData.append("*");	//EOF
				writeData.append("\r\n");
				bfw.write(writeData.toString());
				
				tmpInsertPodMap = new HashMap<String, Object>();
				tmpInsertPodMap.put("odJobNo", houseOrder.get("OD_JOB_NO"));
				tmpInsertPodMap.put("blNo", houseOrder.get("HBL_NO"));
				tmpInsertPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				tmpInsertPodMap.put("updateBlTypeMh", "H");
				insertPodList.add(tmpInsertPodMap);
			}	//하우스반복 END
			docSend.put("stockStartNo", stockStartNo);
			
		} catch(Exception e) {
			throw e;
		} finally {
			if (bfw != null) {
				try{ bfw.close(); }catch(Exception e){log.error(Util.getPrintStacTraceString(e));}
			}
		}
		
		retData[0] = fileName;	// 파일명
		retData[2] = dirPath[0];	// 원본 로컬 경로
		retData[3] = dirPath[1];	// 백업 로컬 경로
		retData[4] = "MANIFEST_GW_SEA";
		retData[5] = Util.null2str(masterOrder.get("MBL_NO"));
		
		return retData;
	}
	public OrderSaveResult resetSendManifestGw(SendManifest sendManifest) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> tmpMap;
		Map<String, Object> podMap;
		for(String masterOdJobNo : sendManifest.getModJobNoList()){
			podMap = new HashMap<String, Object>();
			sendManifest.setOdJobNo(masterOdJobNo);
			
			if(Util.null2str(sendManifest.getSubGridYn()).equals("Y")){
				sendManifest.setSimpleDocNoList(sendManifest.getModSimpleDocList().get(masterOdJobNo));
				if(sendManifest.getModSimpleDocList().get(masterOdJobNo).contains(null)) {	//제출번호 리스트에 Null값이 있을 경우 쿼리에 조건 따로 줘야함.
					sendManifest.setAction("containsNull");
				}else{
					sendManifest.setAction(null);
				}
			}
			manifestMapper.deleteResetSendManifestGw(sendManifest);
			List<Map<String, Object>> resetDataList = manifestMapper.selectResetSendManifestGw(sendManifest);
			for(Map<String, Object> resetData : resetDataList){
				tmpMap = commonMapper.selectLastPodInfo(resetData);
				if(tmpMap != null && tmpMap.size() > 0){
					if("H".equals(resetData.get("BL_TYPE_MH"))){
						podMap.put("lastPodStatus", tmpMap.get("POD_STATUS"));
						podMap.put("lastPodDate", tmpMap.get("POD_DATE_TIME"));
					}else{
						podMap.put("lastPodStatusMaster", tmpMap.get("POD_STATUS"));
						podMap.put("lastPodDateMaster", tmpMap.get("POD_DATE_TIME"));
					}
				}
			}
			
			tmpMap = BeanUtils.describe(sendManifest);
			tmpMap.putAll(podMap);
			tmpMap.put("houseOdJobNoList", sendManifest.getHouseOdJobNoList());
			tmpMap.put("simpleDocNoList", sendManifest.getSimpleDocNoList());
			manifestMapper.updateResetSendManifestGw(tmpMap);
		}
		
		return orderSaveResult;
	}
	
	/**
	 * 수입화물진행정보 배치실행데이터 저장
	 * @param sendManifest
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult insertCargoProgress(SendManifest sendManifest, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		Map<String, Object> result = new HashMap<String, Object>();
		Map<String, Object> docSend = new HashMap<String, Object>();
		
		Map<String, Object> masterOrder = null;
		List<Map<String, Object>> houseOrderList = null;
		
		docSend.put("entCode", Util.null2str(sendManifest.getEntCode()));
		docSend.put("entBusinessCode", Util.null2str(sendManifest.getEntBusinessCode()));
		docSend.put("solution", Util.null2str(sendManifest.getSolution()));
		docSend.put("bound", Util.null2str(sendManifest.getBound()));
		docSend.put("boundType", Util.null2str(sendManifest.getBoundType()));
		docSend.put("userLanguage", Util.null2str(sendManifest.getUserLanguage()));
		docSend.put("registerUserId", Util.null2str(sendManifest.getRegisterUserId()));
		docSend.put("registerUserName", sendManifest.getRegisterUserName());
		docSend.put("updateUserId", Util.null2str(sendManifest.getUpdateUserId()));
		docSend.put("gmtTime", Util.null2str(sendManifest.getGmtTime()));
		docSend.put("cancelYn", Util.null2str(sendManifest.getCancelYn()));
		docSend.put("cryptographyYn", sendManifest.getCryptographyYn());
		
		String key = "E-COMMERCE_CARGO_PROGRESS_INSERT";
		ListOperations<String, String> listOperations = redisTemplate.opsForList();
		JSONObject json = new JSONObject();
		
		if(Util.null2str(sendManifest.getOrderDetYn()).equals("Y")) {	//오더조회 화면인지 여부, 오더조회에서는 마스터오더 조회하지 않고 화물진행정보 실행
			/** 하우스오더조회 */
			docSend.put("orderDetYn", "Y");
			docSend.put("houseOdJobNoList", sendManifest.getHouseOdJobNoList());
			docSend.put("srchType", "insertCargoProgress");
			houseOrderList = manifestMapper.selectGwSendHouseOrderList(docSend);
			
			for(Map<String, Object> houseOrder : houseOrderList){
				/** 2025-03-13 BMS는 일반/배제/취하만 수입화물진행정보실행 */
				if(!"Y".equals(houseOrder.get("CD_TYPE")) || "Y".equals(houseOrder.get("DROP_YN"))){
					json.put("entCode", houseOrder.get("ENT_CODE"));
					json.put("entBusinessCode", houseOrder.get("ENT_BUSINESS_CODE"));
					json.put("bound", houseOrder.get("BOUND"));
					json.put("boundType", houseOrder.get("BOUND_TYPE"));
					json.put("customs", houseOrder.get("CUSTOMS_CODE"));
					json.put("status", "none");
					json.put("odJobNo", houseOrder.get("OD_JOB_NO"));
					json.put("masterOdJobNo", houseOrder.get("MASTER_OD_JOB_NO"));
					json.put("mblNo", ((String)houseOrder.get("MBL_NO")).trim());
					json.put("hblNo", houseOrder.get("HBL_NO"));
					json.put("arDate", Util.null2str(houseOrder.get("AR_DATE")));
					json.put("dropYn", houseOrder.get("DROP_YN"));
					json.put("cdType", houseOrder.get("CD_TYPE"));
					json.put("destCode", houseOrder.get("DEST_CODE"));
					json.put("destPort", houseOrder.get("DEST_PORT"));
                    json.put("custCodeLv1", Util.null2str(houseOrder.get("CUST_CODE_LV1")));

					if(!Util.null2str(houseOrder.get("DEST_CODE")).equals(Util.null2str(houseOrder.get("DEST_PORT")))){
						json.put("bondedCargoYn", "Y");
					}else{
						json.put("bondedCargoYn", "N");
					}
					json.put("action", sendManifest.getAction());
					json.put("mrn", houseOrder.get("MRN"));
					listOperations.rightPush(key, json.toString());
				}
			}
		}else {
			for(String masterOdJobNo : sendManifest.getModJobNoList()){
				docSend.put("odJobNo", masterOdJobNo);
				
				if(Util.null2str(sendManifest.getSubGridYn()).equals("Y")) {	//서브그리드인지 여부
					docSend.put("subGridYn", "Y");
					docSend.put("simpleDocNoList", sendManifest.getModSimpleDocList().get(masterOdJobNo));
					if(sendManifest.getModSimpleDocList().get(masterOdJobNo).contains(null)) {	//제출번호 리스트에 Null값이 있을 경우 쿼리에 조건 따로 줘야함.
						docSend.put("containsNullYn", "Y");
					};
				}
				
				if(sendManifest.getHouseOdJobNoList() != null) docSend.put("houseOdJobNoList", sendManifest.getHouseOdJobNoList());
				
				/** 마스터오더조회 */
				masterOrder = manifestMapper.selectGwSendMasterOrder(docSend);
				if (masterOrder != null){
					
					docSend.put("bound", masterOrder.get("BOUND"));
					docSend.put("ediCityPort", masterOrder.get("DEST_PORT"));
					
					/** 하우스오더조회 */
					docSend.put("masterOdJobNo", masterOrder.get("OD_JOB_NO"));
					docSend.put("srchType", "insertCargoProgress");
					houseOrderList = manifestMapper.selectGwSendHouseOrderList(docSend);
					
					for(Map<String, Object> houseOrder : houseOrderList){
						if(!"Y".equals(houseOrder.get("CD_TYPE")) || "Y".equals(houseOrder.get("DROP_YN"))){
							json.put("entCode", houseOrder.get("ENT_CODE"));
							json.put("entBusinessCode", houseOrder.get("ENT_BUSINESS_CODE"));
							json.put("bound", houseOrder.get("BOUND"));
							json.put("boundType", houseOrder.get("BOUND_TYPE"));
							json.put("customs", houseOrder.get("CUSTOMS_CODE"));
							if("insertCargoProgressHouse".equals(sendManifest.getAction())){
								json.put("status", "none");
							}else{
								json.put("status", houseOrder.get("LAST_POD_STATUS"));
							}
							
							json.put("odJobNo", houseOrder.get("OD_JOB_NO"));
							json.put("masterOdJobNo", houseOrder.get("MASTER_OD_JOB_NO"));
							json.put("mblNo", ((String)houseOrder.get("MBL_NO")).trim());
							json.put("hblNo", houseOrder.get("HBL_NO"));
							json.put("arDate", Util.null2str(houseOrder.get("AR_DATE")));
							json.put("dropYn", houseOrder.get("DROP_YN"));
							json.put("cdType", houseOrder.get("CD_TYPE"));
							json.put("destCode", houseOrder.get("DEST_CODE"));
							json.put("destPort", houseOrder.get("DEST_PORT"));
	//					json.put("bondedCargoYn", masterOrder.get("bondedCargoYn"));
							if(!Util.null2str(masterOrder.get("DEST_CODE")).equals(Util.null2str(masterOrder.get("DEST_PORT")))){
								json.put("bondedCargoYn", "Y");
							}else{
								json.put("bondedCargoYn", "N");
							}
							json.put("action", sendManifest.getAction());
							json.put("mrn", masterOrder.get("MRN"));
							listOperations.rightPush(key, json.toString());
						}
					}
				}
			}
		}
		
		/** 처리 로그 기록 */
//		Map<String, Object> tmpMap = new HashMap<String, Object>();
//		String logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
//		String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
//		
//		tmpMap.clear();
//		tmpMap.put("ent_code", docSend.get("entCode"));
//		tmpMap.put("customer_code", "");
//		tmpMap.put("solution", "ECM");
//		tmpMap.put("bound", masterOrder.get("bound"));
//		tmpMap.put("bound_type", masterOrder.get("boundType"));
//		tmpMap.put("hbl_no", "");
//		tmpMap.put("if_service_code", "");
//		tmpMap.put("if_doc_code", sendManifest.getAction());
//		tmpMap.put("if_doc_name", sendManifest.getAction());
//		tmpMap.put("if_doc_reference_no", "");
//		tmpMap.put("if_vender", "");
//		tmpMap.put("if_send_recv_type", "");
//		tmpMap.put("if_send_recv_method", "");
//		tmpMap.put("if_file_path", "");
//		tmpMap.put("if_file_name", "");
//		tmpMap.put("if_status", "SEND");
//		tmpMap.put("if_status_info", "");
//		tmpMap.put("if_sender_id", "");
//		tmpMap.put("if_receiver_id", "");
//		tmpMap.put("if_date", DateTimeUtil.getNow("yyyy-MM-dd"));
//		tmpMap.put("if_date_time", DateTimeUtil.getNow("HH:mm:ss"));
//		tmpMap.put("if_remark", "");
//		tmpMap.put("register_user_id", docSend.get("registerUserId"));
//		tmpMap.put("register_user_name", docSend.get("registerUserName"));
//		tmpMap.put("register_date_time", System.currentTimeMillis());
//		tmpMap.put("es_job_no", logId);
//			
//		IndexApi indexApi = new IndexApi(client);
//		if (!indexApi.existIndex(index))	indexApi.createIndex(index);
//		
//		DocumentApi documentApi = new DocumentApi(client);
//		documentApi.createDocument(index, logId, tmpMap);
		
		orderSaveResult.setResultData(result);
		return orderSaveResult;
	}
}
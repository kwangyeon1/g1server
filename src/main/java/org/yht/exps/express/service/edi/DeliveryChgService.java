package org.yht.exps.express.service.edi;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.yht.elasticsearch.client.api.SearchApi;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.elasticsearch.client.data.SortData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.eCommerce.FunctionMapper;
import org.yht.exps.express.mapper.edi.DeliveryChgMapper;
import org.yht.exps.express.mapper.sms.EnterpriseMapper;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.edi.deliveryChg.DeliveryChg;
import org.yht.exps.express.model.edi.deliveryChg.DeliveryChgDoc;
import org.yht.exps.express.model.edi.deliveryChg.DeliveryChgList;
import org.yht.exps.express.model.edi.deliveryChg.DeliveryChgResult;
import org.yht.exps.express.model.edi.deliveryChg.DeliveryChgSaveResult;
import org.yht.exps.express.model.edi.deliveryChg.search.SearchDeliveryChgList;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.eCommerce.FunctionService;
import org.yht.exps.express.service.sms.SmsUserService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class DeliveryChgService {
		
	@Autowired
	DeliveryChgMapper deliveryChgMapper;
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	private CustomProperties customProperties;
	@Autowired
    EnterpriseMapper enterpriseMapper;
	@Autowired
	SmsUserService smsUserService;
	@Autowired
    CommonService commonService;
	@Autowired
	RestHighLevelClient client;
	@Value("${custom.elasticsearch-scroll-page}")
	private int elasticsearchScrollPage_;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	@Resource(name = "fileService")
	FileService fileService;
	@Autowired
	FunctionService functionService;
		
	/**
	 * ECM 배송지변경신고 제출번호 그룹 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public DeliveryChgList selectDeliveryDocList(SearchDeliveryChgList search) throws Exception {
		DeliveryChgList deliveryChgList = new DeliveryChgList();
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "edi.REGISTER_DATE_TIME|edi.EDI_SEND_DATE", null, null);
		List<DeliveryChg> list  = deliveryChgMapper.selectDeliveryDocList(search);
		deliveryChgList.setDeliveryChgDocList(list);
		if(list != null && list.size() > 0) {
			deliveryChgList.setTotal(list.get(0).getTotal());
		}else {
			deliveryChgList.setTotal(0);
		}
		
		return deliveryChgList;
	}
	
	/**
	 * ECM 배송지변경신고 리스트 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public DeliveryChgList selectDeliveryList(SearchDeliveryChgList search) throws Exception {
		DeliveryChgList deliveryChgList = new DeliveryChgList();
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!Util.isEmpty(search.getSrchNoHouse())){
			search.setSrchNo(search.getSrchNoHouse());
			search.setSrchNoType("od.HBL_NO");
			search.setSrchNoHouse(null);
		}
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}
		
		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		}else{
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat", true, "srchDateGbn", "|od.AR_DATE|od.DELIVERY_CHANGE_DATE_TIME|edi.REGISTER_DATE_TIME|odAdd.DELIVERY_DATE|edi.EDI_SEND_DATE", "srchNoType", "|od.MBL_NO|od.HBL_NO");
		List<DeliveryChg> list  = deliveryChgMapper.selectDeliveryList(search);
		deliveryChgList.setDeliveryChgList(list);
		if(list != null && list.size() > 0) {
			deliveryChgList.setTotal(list.get(0).getTotal());
		}else {
			deliveryChgList.setTotal(0);
		}
		
		return deliveryChgList;
	}
		
	/**
	 * 배송지변경신고 생성
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public DeliveryChgSaveResult insertDeliveryChg(SearchDeliveryChgList search) throws Exception {
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat|gmtTime", true, "srchDateGbn", "|od.AR_DATE|od.DELIVERY_CHANGE_DATE_TIME|edi.REGISTER_DATE_TIME|odAdd.DELIVERY_DATE|edi.EDI_SEND_DATE", "srchNoType", "|od.MBL_NO|od.HBL_NO");

		DeliveryChgDoc deliveryChgDoc = new DeliveryChgDoc();
		deliveryChgDoc.setEntCode(search.getSrchEntCode());
		deliveryChgDoc.setGmtTime(search.getGmtTime());
		deliveryChgDoc.setRegisterUserId(search.getUserId());
		deliveryChgDoc.setEntBusinessCode(search.getSrchEntBusinessCode());
		
		DeliveryChgSaveResult deliveryChgSaveResult = new DeliveryChgSaveResult();
		
		List<DeliveryChg> deliveryChgList = search.getDeliveryChgList();
		//검색건
		if(deliveryChgList == null || deliveryChgList.size() == 0) {
			List<DeliveryChg> list  = deliveryChgMapper.selectDeliveryListTargetCount(search);
			if(list != null) {
				
				for(DeliveryChg delChg : list) {
					if(delChg.getEdiJobNo() != null && !"".equals(Util.null2str(delChg.getEdiJobNo()))) {
						//이미 생성된 건이 존재합니다.
						deliveryChgSaveResult.setResultCode("A11253");
						return deliveryChgSaveResult;
					}
				}
				
				deliveryChgDoc.setDeliveryChgList(list);
				deliveryChgList = list;
			}
		//선택건
		}else {
			deliveryChgDoc.setDeliveryChgList(search.getDeliveryChgList());
		}
		
		int docNoCnt = 0;
		int deliChgListSize = deliveryChgDoc.getDeliveryChgList().size();
		List<DeliveryChg> deliChgList = deliveryChgDoc.getDeliveryChgList();
		for(int i=0; i<deliChgListSize; i+=1000) {
			Map<String, Object> chkMap = new HashMap<String, Object>();
			if(i+1000 < deliChgListSize) {
				deliveryChgDoc.setDeliveryChgList(deliChgList.subList(i, i+1000));
				chkMap = deliveryChgMapper.selectEdiCreateChk(deliveryChgDoc);
				docNoCnt += Util.null2int(chkMap.get("DOC_NO_CNT"));
			}else {
				deliveryChgDoc.setDeliveryChgList(deliChgList.subList(i, deliChgListSize));
				chkMap = deliveryChgMapper.selectEdiCreateChk(deliveryChgDoc);
				docNoCnt += Util.null2int(chkMap.get("DOC_NO_CNT"));
			}
		}
		
		if(docNoCnt > 0) {
			deliveryChgSaveResult.setResultCode("A11253");	/** 이미 생성된 건이 존재합니다. */
			return deliveryChgSaveResult;
		}
		/** SIMPLE_REPORT_DATE데이터 저장   2024-01-17 데몬 반영함   데이터 쌓일동안 기존소스로 전송 */
//		else if(Util.null2int(chkMap.get("DATE_NULL_CNT")) > 0){
//			deliveryChgSaveResult.setResultCode("A11304");	/** #1# #2# 항목이 존재하지 않습니다. */
//			return deliveryChgSaveResult;
//		}
		
		//목적항별 리스트 재조합 
		Map<String,List<DeliveryChg>> masMap = new HashMap<String,List<DeliveryChg>>();
		Map<String, String> mailboxIdMap = new HashMap<String, String>();
		for(int i=0;i<deliveryChgList.size();i++) {
			DeliveryChg chg = deliveryChgList.get(i);
			/** 2024-12-16 필수값체크 배송변경사유 제거 */
			if(Util.null2str(chg.getDeliveryNameChanged()).equals("") || Util.null2str(chg.getDeliveryAddressChanged()).equals("") /* || Util.null2str(chg.getDeliveryReasonChanged()).equals("") */){
				deliveryChgSaveResult.setResultCode("dataNull");
				return deliveryChgSaveResult;
			}
			String destPort = chg.getDestPort();
			List<DeliveryChg> hList = masMap.get(destPort);
			if(hList == null) {
				hList = new ArrayList<DeliveryChg>();
			}
			
			hList.add(chg);
			
			//목적항값으로 리스트 셋팅
			masMap.put(destPort, hList);
		}
		
		Map<String, Object> ediTempMap;
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("srchEntCode", deliveryChgDoc.getEntCode());
		paramMap.put("srchEntBusinessCode", deliveryChgDoc.getEntBusinessCode());
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(paramMap, "stockDateFormat", false, null, null, null, null);
		
		/** 업체코드,EDI_포털_사서함ID값 체크 */
		String errDestPort = "";
		for (String destPort : masMap.keySet()){
			List<DeliveryChg> list = masMap.get(destPort);
			paramMap.put("srchBound", list.get(0).getBound());
			paramMap.put("srchEdiCityPort", destPort);
			ediTempMap = commonMapper.selectBusEdiCodeInfo(paramMap);
			if(ediTempMap == null || Util.null2str(ediTempMap.get("EDI_EXPRESS_SIGN")).equals("")){
				errDestPort += ", " + destPort;
				log.error("ediTempMap null : " + deliveryChgDoc.getEntCode() + ", " + deliveryChgDoc.getEntBusinessCode() + ", " + list.get(0).getBound() + ", " + destPort);
			}else{
				for(DeliveryChg chg : list){
					chg.setEdiDocNo(Util.null2str(ediTempMap.get("EDI_EXPRESS_SIGN")));
					if(mailboxIdMap.get(destPort) == null && !Util.null2str(chg.getEdiPortalMailboxId()).equals("")){
						mailboxIdMap.put(destPort, chg.getEdiPortalMailboxId());
					}
				}
			}
		}
		for (String destPort : masMap.keySet()){
			if(mailboxIdMap.get(destPort) == null){
				errDestPort += ", " + destPort;
				log.error("mailboxIdMap null : " + deliveryChgDoc.getEntCode() + ", " + deliveryChgDoc.getEntBusinessCode() + ", " + destPort);
				List<DeliveryChg> list = masMap.get(destPort);
				for(DeliveryChg chg : list){
					if(Util.isEmpty(chg.getEdiPortalMailboxId())){
						log.error("EdiPortalMailboxId null : " + chg.getOdJobNo() + ", " + chg.getEdiPortalMailboxId() + ", " + destPort);
						break;
					}
				}
			}
		}
		
		if(errDestPort.length() > 0){
			paramMap.clear();
			paramMap.put("errDestPort", errDestPort.substring(2));
			deliveryChgSaveResult.setResultData(paramMap);
			deliveryChgSaveResult.setResultCode("ediInfoNull");
			return deliveryChgSaveResult;
		}
		
		SqlSession sqlSession = null;
		int cnt = 1;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			DeliveryChgMapper deliveryChgBatchMapper = sqlSession.getMapper(DeliveryChgMapper.class);
			
			//MAP의 KEY값을 이용하여 VALUE값 가져오기
			//목적항별로 제출번호 생성해야 함.
		    for (String destPort : masMap.keySet()){
		    	
		    	List<DeliveryChg> hList = masMap.get(destPort);
		    	
		    	//2021-10-18 EDI_DOC_NO STOCK_MAX로 생성하도록 수정
		    	//배송지변경신청서_제출번호 추출
//				DeliveryChg docMap = deliveryChgMapper.getDeliveryDocNo(deliveryChgDoc);
//				String ediDocNo = docMap.getEdiDocNo();
		    	Map<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("gmtTime", deliveryChgDoc.getGmtTime());
				tmpMap.put("entCode", deliveryChgDoc.getEntCode());
				tmpMap.put("entBusinessCode", "");
				tmpMap.put("registerUserId", deliveryChgDoc.getRegisterUserId());
				tmpMap.put("updateUserId", deliveryChgDoc.getRegisterUserId());
				tmpMap.put("solution", "ECM");
				tmpMap.put("stockType", "EDI");
				tmpMap.put("stockSign", hList.get(0).getEdiDocNo());	//업체코드(6) -> dest_port 기준으로 EDI_특송부호 가져오기
				tmpMap.put("stockSeqLength", "7");
				tmpMap.put("stockDateFormat", "112");	//yyyymmdd
				tmpMap.put("dateLength", "4");			//yyyy
				tmpMap.put("signType", "noHyphen");
				tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
				tmpMap.put("stockRemark", "EDI_CODE (GOVCBR109)");
				tmpMap.put("appendStockSign", "9");
				
				String ediDocNo = commonMapper.selectNextStockMaxNo(tmpMap);	/** 업체코드(6)+년도(4)+'9'+제출일련번호(7) */
				
				tmpMap = new HashMap<String, Object>();
				/** 관리번호 */
				tmpMap.put("gmtTime", deliveryChgDoc.getGmtTime());
				tmpMap.put("entCode", deliveryChgDoc.getEntCode());
				tmpMap.put("entBusinessCode", "");
				tmpMap.put("registerUserId", deliveryChgDoc.getRegisterUserId());
				tmpMap.put("updateUserId", deliveryChgDoc.getRegisterUserId());
				tmpMap.put("solution", "ECM");
				tmpMap.put("stockType", "EDI");
				tmpMap.put("stockSign", "EDI");
				tmpMap.put("stockSeqLength", 7);
				tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
				tmpMap.put("stockRemark", "EDI-JOB NO");
				String ediJobNo = commonMapper.selectNextStockMaxNo(tmpMap);
			
				for(int i=0;i<hList.size();i++) {
					DeliveryChg chg = hList.get(i);
					chg.setEdiDocNo(ediDocNo);
					if(i == 0) {
						chg.setEdiJobNo(ediJobNo);
						chg.setEntCode(deliveryChgDoc.getEntCode());
						chg.setEntBusinessCode(deliveryChgDoc.getEntBusinessCode());
						chg.setRegisterUserId(deliveryChgDoc.getRegisterUserId());
						chg.setGmtTime(deliveryChgDoc.getGmtTime());
						chg.setEdiPortalMailboxId(mailboxIdMap.get(destPort));
						//insert 처리
						deliveryChgMapper.insertDeliveryChg(chg);
					}
					
					//오더테이블 업데이트
					deliveryChgBatchMapper.updateDeliveryJob(chg);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
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
		
		
		return deliveryChgSaveResult;
	}
	
	/**
	 * 배송지변경신고 일괄변경
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public DeliveryChgSaveResult updateBatchDeliveryChg(DeliveryChgDoc deliveryChgDoc, HttpServletRequest request) throws Exception {
		DeliveryChgSaveResult deliveryChgSaveResult = new DeliveryChgSaveResult();
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(deliveryChgDoc, "gmtTime|dateFormat", false, null, null, null, null);
		
		List<DeliveryChg> deliveryChgList = deliveryChgMapper.selectDeliveryChgList(deliveryChgDoc);;
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		
		int batchSize = 200;
		int totalSize = deliveryChgList.size();
		int numBatches = (totalSize + batchSize - 1) / batchSize; // 전체 배치 수 계산
		if(deliveryChgList == null) {
			return deliveryChgSaveResult;
		}
		
		if(deliveryChgMapper.selectEdiSendChk(deliveryChgDoc) > 0) {
			deliveryChgSaveResult.setResultCode("A10028");	/** 전송된 자료는 수정할 수 없습니다. */
			return deliveryChgSaveResult;
		}
		// 매개변수 오류로 인하여 수정 2024.05.16
		for (int i = 0; i < numBatches; i++) {
		    int startIndex = i * batchSize;
		    int endIndex = Math.min(startIndex + batchSize, totalSize);
		    deliveryChgDoc.setDeliveryChgList(deliveryChgList.subList(startIndex, endIndex));
		    deliveryChgMapper.updateBatchDeliveryChg(deliveryChgDoc);
		}
		
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", deliveryChgDoc.getEntCode());
		tmpLogMap.put("ent_business_code", deliveryChgDoc.getEntBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("process_user_id", deliveryChgDoc.getUpdateUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		tmpLogMap.put("ent_business_print_name", deliveryChgDoc.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", deliveryChgDoc.getLogUserName());
		tmpLogMap.put("process_user_name_eng", deliveryChgDoc.getLogUserNameEng());
		
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		int seq = 1;
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			CommonMapper commonBatchMapper = sqlSession.getMapper(CommonMapper.class);
			
			Map<String, Object> podMap = null;
			if(!Util.null2str(deliveryChgDoc.getDeliveryDate()).equals("")){
				podMap = new HashMap<String, Object>();
				podMap.put("entCode", 		deliveryChgDoc.getEntCode());
				podMap.put("gmtTime", 		deliveryChgDoc.getGmtTime());
				podMap.put("updateUserId", 	deliveryChgDoc.getUpdateUserId());
				podMap.put("podStatus", 	"ECM420");
				podMap.put("podDate", 		deliveryChgDoc.getDeliveryDate());
				podMap.put("podTime", 		"00:00");
				podMap.put("dateFormat", 	deliveryChgDoc.getDateFormat());
				podMap.put("podTableName", 	"TB_ECM_POD_04");
				/** 취약성점검 SQL Injection 방지 */
				Util.accessControlDatabase(podMap, "podTableName|gmtTime|dateFormat", false, null, null, null, null);
			}
			
			List<DeliveryChg> updatePodList = new ArrayList<DeliveryChg>();
			for(DeliveryChg deliveryChg : deliveryChgList) {
				bulkData = new BulkData();
				bulkData.setActionType(BulkData.Type.CREATE);
				bulkData.setIndexName(index);
				bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
				logMap = new HashMap<String, Object>();
				logMap.putAll(tmpLogMap);
				logMap.put("reference_no_id", bulkData.getId());
				logMap.put("bound", deliveryChg.getBound());
				logMap.put("bound_type", deliveryChg.getBoundType());
				logMap.put("job_no", deliveryChg.getOdJobNo());
				logMap.put("description", "배송지변경신고 > 일괄변경" +" [MBL_NO : " + deliveryChg.getMblNo() + " HBL_NO : "+ deliveryChg.getHblNo() +"]");
	//			logMap.put("job_log", new Gson().toJson(deliveryChgDoc));
				logMap.put("job_log", "");
				bulkData.setJsonMap(logMap);
				bulkList.add(bulkData);
				
				if(podMap != null){
					if(!Util.null2str(deliveryChg.getPodJobNo()).equals("")){
						updatePodList.add(deliveryChg);
					}else{
						podMap.put("odJobNo", deliveryChg.getOdJobNo());
						podMap.put("blNo", deliveryChg.getHblNo());
						podMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
						commonBatchMapper.insertPod(podMap);
					}
				}
				seq++;
			}
			if(podMap != null){
				for(DeliveryChg deliveryChg : updatePodList) {
					podMap.put("odJobNo", deliveryChg.getOdJobNo());
					commonBatchMapper.updatePod(podMap);
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
		
		commonService.insertLogBulk(bulkList);
		
		return deliveryChgSaveResult;
	}
	
	/**
	 * 배송지변경신고 삭제
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public DeliveryChgSaveResult deleteBatchDeliveryChg(DeliveryChgDoc deliveryChgDoc) throws Exception {
		DeliveryChgSaveResult deliveryChgSaveResult = new DeliveryChgSaveResult();
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(deliveryChgDoc, "gmtTime|dateFormat", false, null, null, null, null);
		
		List<DeliveryChg> deliveryChgList = deliveryChgDoc.getDeliveryChgList();		
		if(deliveryChgList == null) {
			return deliveryChgSaveResult;
		}
		int batchSize = 300;
		int totalSize = deliveryChgList.size();
		int numBatches = (totalSize + batchSize - 1) / batchSize; // 전체 배치 수 계산
		
		if(Util.null2str(deliveryChgDoc.getAction()).equals("deliveryChgDel")){	/** 자료생성탭 삭제 */
			if(Util.null2int(deliveryChgMapper.selectEdiCreateChk(deliveryChgDoc).get("DOC_NO_CNT")) > 0) {
				deliveryChgSaveResult.setResultCode("A11253");	/** 이미 생성된 건이 존재합니다. */
				return deliveryChgSaveResult;
			}
			// 매개변수 오류로 인하여 수정 2024.05.16
			for (int i = 0; i < numBatches; i++) {
			    int startIndex = i * batchSize;
			    int endIndex = Math.min(startIndex + batchSize, totalSize);
			    deliveryChgDoc.setDeliveryChgList(deliveryChgList.subList(startIndex, endIndex));
			    deliveryChgMapper.updateBatchDeliveryChg(deliveryChgDoc);
			}

		}else{	/** 신고탭 삭제 */
			if(deliveryChgMapper.selectEdiSendChk(deliveryChgDoc) > 0) {
				deliveryChgSaveResult.setResultCode("A10028");	/** 전송된 자료는 수정할 수 없습니다. */
				return deliveryChgSaveResult;
			}
			deliveryChgMapper.deleteBatchDeliveryChg(deliveryChgDoc);
		}
		
		return deliveryChgSaveResult;
	}
	
	/**
	 * 엑셀업로드
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public DeliveryChgSaveResult excelUploadCheck(MultipartFile uploadExcel, String entCode) throws Exception{
		Map<String, Object> resultData = new HashMap<String, Object>();
		DeliveryChgSaveResult deliveryChgSaveResult = new DeliveryChgSaveResult();
		InputStream is = null;
		String errorField = "";
		try {
			// 파일 확장자 유효성 검사
			if(!fileService.checkExtension(uploadExcel, new String[]{"xlsx", "xls"})) {
				deliveryChgSaveResult.setResultCode("extError");
				return deliveryChgSaveResult;
			}
			
			FileUploadResponse result = fileService.storeFile(uploadExcel, entCode, true);
			if(Util.null2str(result.getResult()).equals("ok")){
				File file = fileService.loadFileAsResource(result.getFileName(), entCode, true).getFile();
				String extension = null;
				if(file.getName().toLowerCase().indexOf(".xlsx") > -1){
					extension = "xlsx";
				}else{
					extension = "xls";
				}
				
				String[] fieldName = null;
				String[] fieldTitle = null;
				String[] fieldLength = null;
				String[] fieldType = null;

				fieldName =		new String[]{"mblNo","hblNo",	"deliveryDate","deliveryNameChanged","deliveryAddressChanged",	"deliveryReasonChanged"};	
				fieldTitle =	new String[]{"MBLNO","HBLNo","배송일자","배송업체","실제배송 수하인주소",	"비고"};
				fieldLength =	new String[]{"20","20",	"10","50","300","200"};
				fieldType =		new String[]{"S","S","DATE","S","S","S"};
				
				List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
				
				//파일이 닫히지 않아서 수정
//				Workbook wb = WorkbookFactory.create(file);
				
				is = new FileInputStream(file);
				Workbook wb = WorkbookFactory.create(is);
				
				Sheet sheet = wb.getSheetAt(0);
				DataFormatter df = new DataFormatter();
				int fieldSize = fieldName.length;
				StringBuffer errMsg = new StringBuffer();
				Map<String, Object> dataMap = null;
				String fieldValue = "";
				// 데이트 포맷
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				
				if(extension.equals("xlsx")){
					XSSFCell myCell = null;
					for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
						if(sheet.getRow(i) == null){
							deliveryChgSaveResult.setResultCode("A11233");	/** #1#행이 비어있습니다. (파일 마지막행 : #2#) */
							resultData.put("nullLine", i + 1);
							resultData.put("lastLine", sheet.getLastRowNum() + 1);
							deliveryChgSaveResult.setResultData(resultData);
							return deliveryChgSaveResult;
						}
						
						dataMap = new HashMap<String, Object>();
						//셀 셋팅
						for(int j = 0; j < fieldSize; j++){
							myCell = (XSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							errorField = fieldTitle[j];
							if("DATE".equals(fieldType[j])) {
								String value = null;
								// 엑셀 일자 형식오류로 인하여 수정 24.07.09
								try {
									if(myCell!=null && myCell.getDateCellValue() !=null) {
										value = sdf.format(myCell.getDateCellValue());
									}
								}catch(Exception e){
									value = df.formatCellValue(myCell);
								}
								fieldValue = this.dataChk(value, fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i);
							}
							else {
								fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i);
							}
							if(!Util.null2str(fieldValue).equals("")){
								dataMap.put(fieldName[j], fieldValue);
							}
						}
						
						dataList.add(dataMap);
					}
				}else{
					HSSFCell myCell = null;
					for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
						if(sheet.getRow(i) == null){
							deliveryChgSaveResult.setResultCode("A11233");	/** #1#행이 비어있습니다. (파일 마지막행 : #2#) */
							resultData.put("nullLine", i + 1);
							resultData.put("lastLine", sheet.getLastRowNum() + 1);
							deliveryChgSaveResult.setResultData(resultData);
							return deliveryChgSaveResult;
						}
						
						dataMap = new HashMap<String, Object>();
						
						//셀 셋팅
						for(int j = 0; j < fieldSize; j++){
							errorField = fieldTitle[j];
							myCell = (HSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							if("DATE".equals(fieldType[j])) {
								String value = null;
								
								if(myCell!=null && myCell.getDateCellValue() !=null) {
									value = sdf.format(myCell.getDateCellValue());
								}
								
								fieldValue = this.dataChk(value, fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i);
							}
							else {
								fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i);
							}
							
							if(!Util.null2str(fieldValue).equals("")) {
								dataMap.put(fieldName[j], fieldValue);
							}
							
						}
						
						dataList.add(dataMap);				
						
					}
					
				}
				
				if(errMsg.length() != 0){
					deliveryChgSaveResult.setResultCode("dataErr");
					Map<String, Object> errMap = new HashMap<String, Object>();
					errMap.put("errStr", errMsg);
					deliveryChgSaveResult.setResultData(errMap);
					return deliveryChgSaveResult;
				}
				//TODO 파일삭제?
				
				resultData.put("dataList", dataList);
				resultData.put("entCode", entCode);
				resultData.put("excelUploadType", "deliveryChg");
				
				/** MBL_NO가 없을경우 HBL_NO만으로 조회   대상건이 2건이상일경우 오류 */
				int notBlCnt = 0;
				String notHblList = "";
				for(Map<String, Object> map : dataList){
					if(deliveryChgMapper.selectHblValidate3(map) != 1){
						notHblList += ", " + Util.null2str(map.get("hblNo"));
						notBlCnt++;
					}
				}
				
				
				resultData.put("dataSize", dataList.size());
				
				if(notBlCnt > 0){
					//유효하지않은 BL
					resultData.put("NOT_BL_CNT", notBlCnt);
					resultData.put("NOT_HBL_LIST", notHblList.substring(2));
				}
				else {
					resultData.put("NOT_BL_CNT", "0");
					resultData.put("NOT_HBL_LIST", "");				
				}
				
				deliveryChgSaveResult.setResultData(resultData);
				deliveryChgSaveResult.setResultCode("200");
				
				String backupPath = customProperties.getBackupDir() + "excel/" + entCode + "/" + DateTime.getDate("yyyy-MM");
				File dir = new File(backupPath);
				if (dir.exists() == false)	dir.mkdirs();
				String backupFilePath = backupPath + "/" + file.getName();
				file.renameTo(new File(backupFilePath));
			}else{
				deliveryChgSaveResult.setResultCode("ERROR");
			}
			
		}catch(Exception e) {
			if(e.getMessage().contains("Cannot get a ")) { 				// 데이터 형식오류 체크
				deliveryChgSaveResult.setResultCode("A11449");
				deliveryChgSaveResult.setErrorResult(errorField);
			} else {
				deliveryChgSaveResult.setResultCode("ERROR");
			}
			log.error("exception Message :: " + e.getMessage());
		}finally{
			if (is != null) is.close();
		}
		
		return deliveryChgSaveResult;
	}
	
	/**
	 * 배송지변경신고 Excel 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public DeliveryChgSaveResult excelUpload(DeliveryChgDoc deliveryChgDoc) throws Exception {
		DeliveryChgSaveResult deliveryChgSaveResult = new DeliveryChgSaveResult();
		List<DeliveryChg> dataList = deliveryChgDoc.getDeliveryChgList();
		List<DeliveryChg> deliveryChtDocTmpList = new ArrayList<DeliveryChg>();
		Map<String, Object> deliveryChtDocTmpMap = new HashMap<String, Object>();
		List<Map<String, Object>> errorHblList = new ArrayList<Map<String, Object>>();
		int errorHblCnt = 0;
		if(dataList == null) {
			deliveryChgSaveResult.setResultCode("NO DATA");
			return deliveryChgSaveResult;
		}
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(deliveryChgDoc, "gmtTime", false, null, null, null, null);
		deliveryChgDoc.setAction("excelUpload");
		// 매개변수개수 초과오류로 인하여 1000건씩 조회하도록 수정 - 2024.05.16
//		if(deliveryChgMapper.selectEdiSendChk(deliveryChgDoc) > 0) {
//			deliveryChgSaveResult.setResultCode("A10028");	/** 전송된 자료는 수정할 수 없습니다. */
//			return deliveryChgSaveResult;
//		}

		//셀렉 갯수 간격줄임 1000->35
		int	batchCount = 35;
		for(int i=0; i<dataList.size();) { 
			deliveryChtDocTmpList.add(dataList.get(i++));
			if(i%batchCount == 0 || i == dataList.size()) {
				deliveryChtDocTmpMap.put("deliveryChgList",deliveryChtDocTmpList);
				deliveryChtDocTmpMap.put("action", deliveryChgDoc.getAction());
				List<String> tmpList = deliveryChgMapper.selectEdiSendChk2(deliveryChtDocTmpMap);
				if(tmpList != null && tmpList.size() > 0) {
//					deliveryChgSaveResult.setResultCode("A10028");	/** 전송된 자료는 수정할 수 없습니다. */
//					return deliveryChgSaveResult;
					for(String blNo : tmpList) {
						errorHblCnt++;
						deliveryChtDocTmpMap = new HashMap<String, Object>();
						deliveryChtDocTmpMap.put("blNo", blNo);
						deliveryChtDocTmpMap.put("errorStr", "전송된 자료는 수정할 수 없습니다.");
						errorHblList.add(deliveryChtDocTmpMap);
					}
				}
				deliveryChtDocTmpList.clear();
			}
		}
		
		if(errorHblCnt > 0) {
			//이미 신고된 BL
			Map<String, Object> resultData = new HashMap<String, Object>();
			resultData.put("ERROR_HBL_LIST", errorHblList);
			deliveryChgSaveResult.setResultCode("A10028");	/** 전송된 자료는 수정할 수 없습니다. */
			deliveryChgSaveResult.setResultData(resultData);
			return deliveryChgSaveResult;
		}
		
		// 매개변수개수 초과오류로 인하여 수정 2024.01.16
		//deliveryChgMapper.updateExcelDeliveryChg(deliveryChgDoc);
		for(int i=0; i<dataList.size();){
			deliveryChtDocTmpList.add(dataList.get(i++));
			if(i%100 == 0 || i == dataList.size()){
				deliveryChtDocTmpMap.put("deliveryChgList",deliveryChtDocTmpList);
				deliveryChtDocTmpMap.put("gmtTime",deliveryChgDoc.getGmtTime());
				deliveryChtDocTmpMap.put("updateUserId", deliveryChgDoc.getUpdateUserId());
				deliveryChgMapper.updateExcelDeliveryChg(deliveryChtDocTmpMap);
				deliveryChtDocTmpList.clear();
			}
		}
		
		return deliveryChgSaveResult;
	}
	

	/**
	 * 배송지변경신고 결과조회
	 * @param searchDeliveryChgList
	 * @return
	 * @throws Exception
	 */
	public DeliveryChgList selectDeliveryChgResult(SearchDeliveryChgList searchDeliveryChgList) throws Exception {
		
		DeliveryChgList deliveryChgList = new DeliveryChgList();
		
		try {
			
			// 검색할 Indices
	        String[] indices = new String[1];
	        indices[0] = "ecm-interface-logs-*";
	        
	        // Sort 조건 처리
	        List<SortData> sortList = new ArrayList<SortData>();
	        SortData sortData1 = new SortData();
	        sortData1.setField("register_date_time");
	        sortData1.setType(SortOrder.DESC);
	        sortList.add(sortData1);
	        
	        // Parameter 처리
	        int size = 0;
	        
	        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
	        boolQueryBuilder.filter(QueryBuilders.termQuery("ent_code", searchDeliveryChgList.getSrchEntCode()));
	    	boolQueryBuilder.filter(QueryBuilders.termQuery("if_doc_reference_no", searchDeliveryChgList.getSrchIfDocReferenceNo()));
        	boolQueryBuilder.filter(QueryBuilders.termQuery("if_status", "ERROR"));
	    	
	    	SearchApi searchApi = new SearchApi(client);
	        SearchResponse response;
	        response = searchApi.searchScroll(boolQueryBuilder, elasticsearchScrollPage_, indices, searchDeliveryChgList.getSrchScrollId(), sortList);
		       
	        String scrollId = response.getScrollId();
	        deliveryChgList.setScrollId(scrollId);
	        
	        SearchHits hits = response.getHits();
	    	
			final ObjectMapper mapper = new ObjectMapper();
			mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			mapper.setSerializationInclusion(Include.NON_NULL);
	        
			SearchHit[] resultHits = hits.getHits();
			deliveryChgList.setTotal(resultHits.length);
	    	
			List<DeliveryChgResult> list = new ArrayList<DeliveryChgResult>();
			if (resultHits.length > 0) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		    	for (SearchHit hit : resultHits) {
		    		DeliveryChgResult processLogs = mapper.readValue(hit.getSourceAsString(), DeliveryChgResult.class);
		    		processLogs.setIndex(hit.getIndex());
		    		processLogs.setRegisterDateTimeS(sdf.format(Util.null2long(processLogs.getRegisterDateTime())));
		    		list.add(processLogs);
		        }
		    	
	    	} else {
	    		// 메모리 확보를 위해 ScrollId를 ElasticSearch에서 삭제한다.
	    		if (!"".equals(Util.null2str(searchDeliveryChgList.getSrchScrollId()))) {
	    			searchApi.clearScollRequest(searchDeliveryChgList.getSrchScrollId());
	    		}
	    	}
			
			deliveryChgList.setDeliveryChgResultList(list);
			
		} catch(Exception e) {
			log.error(Util.getPrintStacTraceString(e));
		}
		
		return deliveryChgList;
	}
	
	public int selectDeliveryListTargetCount(SearchDeliveryChgList search) throws Exception {
		int listSize = 0;
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}
		
		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		}else{
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
		}
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "|od.AR_DATE|od.DELIVERY_CHANGE_DATE_TIME|edi.REGISTER_DATE_TIME|odAdd.DELIVERY_DATE|edi.EDI_SEND_DATE", "srchNoType", "|od.MBL_NO|od.HBL_NO");
		List<DeliveryChg> list  = deliveryChgMapper.selectDeliveryListTargetCount(search);
		if(list != null) {
			listSize = list.size();
		}
		
		return listSize;
	}
	
	public String dataChk(String data, String name, String length, String type, StringBuffer errMsg, int row) throws Exception {
		//TODO 엑셀 셀타입 텍스트로 고정시킬수있는지?  없다면 추가개발필요
		if(type.equals("N")){
			if (Util.null2str(data).equals("")){
				data = "0";
			} else {
				try {
//					int idx = data.indexOf("E");	/** 8자리가 넘어가면 123456789.123 => 1.23456789123E8 로 표현됨 */
//					if(idx > -1){
//						int k = Util.null2int(data.substring(idx + 1)) + 1;
//						data = data.substring(0, idx).replace(".", "");
//						data = data.substring(0, k) + "." + data.substring(k);
//					}
					int dotIndex = data.indexOf(".");
					if (dotIndex > 0) {
						data = data.substring(0, dotIndex);
					}
					if(data.length() > Util.null2int(length)){
						errMsg.append("[" + name + "] DATA 길이 오류(행 : " + row + ", 입력값 : " + data + ", 제한 : " + length + "자리) <br />");
					}
					Integer.parseInt(data);	/** 숫자인지 체크 */
				}catch (Exception e){
					errMsg.append("[" + name + "] DATA TYPE 오류(행 : " + row + ", 입력값 : " + data + ") <br />");
				}
			}
		}else if(type.equals("DATE")){
			if (Util.null2str(data).equals("")) {
				data = null;
			}else{
				try{
//					if(!data.startsWith("2")){
//						String[] tmpData = data.split("/");
//						data = "20" + tmpData[2] + (tmpData[0].length() == 1 ? "0" + tmpData[0] : tmpData[0]) + tmpData[1];
//					}else{
						SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
						SimpleDateFormat sdFormat2 = new SimpleDateFormat("yyyyMMdd");
						data = data.replaceAll("[-./]", "").substring(0, 8);
						data = sdFormat.format(sdFormat2.parse(data));
//					}
				}catch (Exception e){
					errMsg.append("[" + name + "] DATA TYPE 오류(행 : " + row + ", 입력값 : " + data + ") <br />");
				}
			}
		}else if("DBL".equals(type)){
			if (Util.null2str(data).equals("")) {
				data = "0";
			} else {
				try {
					Double.parseDouble(data);
				} catch (Exception e) {
					errMsg.append("[" + name + "] DATA TYPE 오류(행 : " + row + ", 입력값 : " + data + ") <br />");
				}
			}
//			int idx = data.indexOf("E");	/** 8자리가 넘어가면 123456789.123 => 1.23456789123E8 로 표현됨 */
//			if(idx > -1){
//				int k = Util.null2int(data.substring(idx + 1)) + 1;
//				data = data.substring(0, idx).replace(".", "");
//				data = data.substring(0, k) + "." + data.substring(k);
//			}
			String[] lengthCnt = Util.null2str(length).split("\\.");
			String[] lengthData = data.split("\\.");
			int j = 0;
			if(lengthCnt.length > 1){
				j = Util.null2int(lengthCnt[1]);
			}
			if(lengthData[0].length() > Util.null2int(lengthCnt[0]) - j) {
				errMsg.append("[" + name + "] DATA 길이 오류(행 : " + row + ", 입력값 : " + data + ", 제한 : " + length + ") <br />");
			} else if(lengthData.length > 1 && lengthData[1].length() > j) {
				errMsg.append("[" + name + "] DATA 길이 오류(행 : " + row + ", 입력값 : " + data + ", 제한 : " + length + ") <br />");
			}
			
		}else if("S".equals(type)){
			if(Util.null2str(data).equals("")){
				data = null;
			}else{
				if(data.getBytes("euc-kr").length > Util.null2int(length)) {
					errMsg.append("[" + name + "] DATA 길이 오류(행 : " + row + ", 입력값 : " + data + ", 제한 : " + length + "자리) <br />");
				}
			}
		}else{
			if(Util.null2str(data).equals("")){
				data = null;
			}else{
				if(data.length() > Util.null2int(length)) {
					errMsg.append("[" + name + "] DATA 길이 오류(행 : " + row + ", 입력값 : " + data + ", 제한 : " + length + "자리) <br />");
				}
			}
		}
		return data;
	}
	
	/**
	 * 배송지변경신고 과태료검증
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public DeliveryChgSaveResult penaltyVali(SearchDeliveryChgList searchDeliveryChgList, HttpServletRequest request) throws Exception{
		DeliveryChgSaveResult deliveryChgSaveResult = new DeliveryChgSaveResult();
		
		Map<String, Object> resultData = new HashMap<String, Object>();
		List<Map<String, Object>> penaltyList = new ArrayList<Map<String, Object>>();

		//과태료검증타입
		String penaltyType = Util.null2str(searchDeliveryChgList.getPenaltyType());
		
		List<Map<String,Object>> hblNoList = new ArrayList<Map<String,Object>>();
		
		Map<String,Object> srchInfo = searchDeliveryChgList.getSrchInfo();
		if(srchInfo == null) {
			deliveryChgSaveResult.setResultCode("notOrder");
			return deliveryChgSaveResult;
		}
		srchInfo.put("cryptographyYn", searchDeliveryChgList.getCryptographyYn());
		srchInfo.put("func","penaltyVali");
		
		//검색조건으로 조회
		List<Map<String, Object>> list = selectOrderList(srchInfo);
		if(list == null || list.size() == 0) {
			deliveryChgSaveResult.setResultCode("notOrder");
			return deliveryChgSaveResult;
		}
		hblNoList.addAll(list);
		
		SqlSession sqlSession = null;
		sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
		FunctionMapper functionMapperBatch = sqlSession.getMapper(FunctionMapper.class);
		
		int houseCnt = hblNoList.size();
		int penaltyCnt = 0;
		StringBuffer penaltyCode = new StringBuffer();
		StringBuffer penaltyName = new StringBuffer();
		
		for(int i = 0; i < houseCnt; i++){	
			Map<String,Object> row = hblNoList.get(i);
			String tarStr = Util.null2str(row.get("deliveryAddressChanged")).trim();
			penaltyCode.setLength(0);
			penaltyName.setLength(0);
			if("".equals(tarStr)) {
				continue;
			}
			// 과태료 코드: B:영문주소,C:한글주소
			//수하인-영문주소 
			if("B".equals(penaltyType)) {
				if(!functionService.checkConsigneeAddress(tarStr)) {
					penaltyName.append(", 영문주소");
					penaltyCode.append("B");
				}
			}
			//수하인-한글주소 
			else if("C".equals(penaltyType)) {
				if(!functionService.checkConsigneeHanAddress(tarStr,"X")) {
					penaltyName.append(", 한글주소");
					penaltyCode.append("C");
				}
			}
			
			if(penaltyCode.length() != 0){
				penaltyCnt++;
				/** 오류내역 */
				Map<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("num", penaltyCnt);
				tmpMap.put("penaltyReason", penaltyName.substring(2) + "이(가) 세관기준에 맞지 않아 과태료 대상입니다.");
				tmpMap.put("hsn", row.get("hsn"));
				tmpMap.put("hblNo", row.get("hblNo"));
				tmpMap.put("deliveryAddressChanged", row.get("deliveryAddressChanged"));
				penaltyList.add(tmpMap);
			}
		}
		
		resultData.put("totalCnt", houseCnt);
		resultData.put("penaltyCnt", penaltyCnt);
		resultData.put("penaltyList", penaltyList);
		
		deliveryChgSaveResult.setResultData(resultData);
				
		return deliveryChgSaveResult;
	}
	
	/**
	 * 배송지변경신고 과태료검증할 bl list 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectOrderList(Map<String,Object> search) throws Exception {
		List<Map<String, Object>> list = null;
		
		
		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.get("srchNo")))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.get("srchNo"))).split(",")));
		}
		
		if(searchNoList.size() > 1){
			search.put("searchType", "multi");
			search.put("searchNoList", searchNoList);
		}else{
			search.put("searchType", "");
			search.put("srchNo", Util.null2str(search.get("srchNo")).trim().replace(",", ""));
		}
		
		Util.accessControlDatabase(search, "srchDateFormat", false, "srchDateGbn", "od.AR_DATE|od.DELIVERY_CHANGE_DATE_TIME|edi.REGISTER_DATE_TIME|odAdd.DELIVERY_DATE", "srchNoType", "od.MBL_NO|od.HBL_NO|od.OD_JOB_NO");
		
		list = deliveryChgMapper.selectOrderList(search);
		
		
		return list;
	}
	

}
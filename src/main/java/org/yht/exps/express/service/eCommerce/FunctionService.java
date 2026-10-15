package org.yht.exps.express.service.eCommerce;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.time.DateUtils;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.consumerService.OrderCustomerMapper;
import org.yht.exps.express.mapper.eCommerce.FunctionMapper;
import org.yht.exps.express.mapper.sms.EnterpriseMapper;
import org.yht.exps.express.model.eCommerce.function.FunctionOrder;
import org.yht.exps.express.model.eCommerce.function.FunctionSaveResult;
import org.yht.exps.express.model.eCommerce.function.OrderPenaltyList;
import org.yht.exps.express.model.eCommerce.function.PenaltyOrder;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.model.sms.user.User;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.consumerService.OrderCustomerService;
import org.yht.exps.express.service.sms.SmsUserService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

@Slf4j
@Service
@Transactional
public class FunctionService {

	@Autowired
	FunctionMapper functionMapper;
	@Autowired
    CommonMapper commonMapper;
	
	@Resource(name = "commonService")
	CommonService commonService;
	
	@Resource(name = "orderCustomerService")
	OrderCustomerService orderCustomerService;
	
	@Autowired
	OrderCustomerMapper orderCustomerMapper;
	
	@Autowired
    ThreadPoolTaskExecutor threadPool;
		
	@Value("${customId.thread.pati-size}")
	public int patiSize;
	
	@Autowired
	WebApplicationContext context;
		
	@Autowired
	DispatcherServlet servlet;
	
	@Autowired
	RequestContextFilter filter;
	
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	@Autowired
	RestHighLevelClient restClient;
	@Autowired
	StringRedisTemplate redisTemplate;
	@Autowired
    EnterpriseMapper enterpriseMapper;
	@Autowired
	SmsUserService smsUserService;
	/**
	 * Function - 반입적용/취소
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public FunctionSaveResult carryin(FunctionOrder functionOrder, HttpServletRequest request) throws Exception{
		Util.accessControlDatabase(functionOrder, "gmtTime", false, null, null, null, null);
		FunctionSaveResult functionSaveResult = new FunctionSaveResult();
//		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		ProcessLogs processLogs = new ProcessLogs();
		
		Map<String, Object> resultData = new HashMap<String, Object>();
		List<Map<String, Object>> tarList;
		//콘솔된 경우(상태가 ‘선적지반입(HTC)’ 이상일 경우 메시지 출력(rowData.BLSTATUSNM + “인 건 은 반입할수 없습니다”)
		List<Map<String, Object>> errList1 = new ArrayList<Map<String, Object>>();
		//콘솔 아닐 경우(상태가 ‘선적지반입(HTC)’ 이상일 경우 confirm (rowData.BLSTATUSNM + “이미 반입상태 처리상태입니다.다시 반입처리하시겠습니까?”)
		List<Map<String, Object>> errList2 = new ArrayList<Map<String, Object>>();
		List<List<Map<String, Object>>> spliceTarList = new ArrayList<List<Map<String, Object>>>();
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		
		//반입모드
		String carryinFunc = Util.null2str(functionOrder.getFuncMode());
		String chk = Util.null2str(functionOrder.getChkMode());
		boolean scanFlag = false;
		
		//호출메뉴
		String parentMenu = Util.null2str(functionOrder.getParentMenu());
		
		/** 반입적용 불가오더 체크 */
		List<Map<String, Object>> hblNoList = new ArrayList<Map<String, Object>>();
		//선택일때
		if("A".equals(chk)) {
			if((functionOrder.getOdList() == null || functionOrder.getOdList().size() == 0) && !Util.null2str(functionOrder.getHblNo()).equals("")){
				/** 스캔화면에서 호출 */
				scanFlag = true;
				tmpMap.put("func", "carryin");
				tmpMap.put("userLanguage", functionOrder.getUserLanguage());
				tmpMap.put("chkMode", "A");
				tmpMap.put("hblNo", functionOrder.getHblNo());
				/** 스캔은 비엘번호 직접입력하므로 권한체크함 */
				tmpMap.put("ecmCustomerCode", functionOrder.getEcmCustomerCode());
				tmpMap.put("ecmCustomerLevel", functionOrder.getEcmCustomerLevel());
				//반입적용(ECM030)
				if("S".equals(carryinFunc)) {
					if(!parentMenu.equals("regist")) {
						tmpMap.put("srchLastPodStatusList", new ArrayList<String>(Arrays.asList("ECM010","ECM020")));
					} else {
						tmpMap.put("srchLastPodStatusList", new ArrayList<String>(Arrays.asList("ECM003")));
					}
				//반입적용취소
				}else{
					if(!parentMenu.equals("regist")) {
						tmpMap.put("srchLastPodStatusList", new ArrayList<String>(Arrays.asList("ECM030")));
					} else {
						tmpMap.put("srchLastPodStatusList", new ArrayList<String>(Arrays.asList("ECM005")));
					}
				}
				tmpMap.put("parentMenu", parentMenu);
				tmpMap.put("cryptographyYn", functionOrder.getCryptographyYn());
				Util.accessControlDatabase(tmpMap, "userLanguage|ecmCustomerLevel|srchNoType|srchDateGbnsrchDateFormat|srchDateFormat|srchDateGbn", false, null, "", null, "");
				hblNoList.addAll(functionMapper.selectOrderListEnt(tmpMap));
				if(hblNoList.size() == 0){
					functionSaveResult.setResultCode("notOrder");
					return functionSaveResult;
				}
			}else{
//				hblNoList.addAll(functionOrder.getOdList());
				
				//2023.03.09 발송->반입/취소 (오더조회 화면에서는 현재 사용하지 않음)
				//선택 건인 경우에도 해당 odJobNo로 데이터 다시 조회해오는 것으로 수정됨
				List<String> srchOdJobNoList = new ArrayList<String>();
				for(Map<String, Object> od : functionOrder.getOdList()) {
					srchOdJobNoList.add(Util.null2str(od.get("odJobNo")));
				}
				
				Map<String,Object> srchInfo = functionOrder.getSrchInfo();
				if(srchInfo == null) {
					functionSaveResult.setResultCode("notOrder");
					return functionSaveResult;
				}
				srchInfo.put("cryptographyYn", functionOrder.getCryptographyYn());
				srchInfo.put("func","carryin");
				
				//반입적용(ECM030)
				if("S".equals(carryinFunc)) {
					//오더조회에서 반입/반입취소 기능 사용할 경우 lastPodStatus 주는 부분 수정해야 함(userType이 entUser일 경우 다른 쿼리 실행돼 변수명 다르기 때문)
					if(!parentMenu.equals("regist")) {
						srchInfo.put("srchStatusList", new ArrayList<String>(Arrays.asList("ECM010","ECM020")));
					} else {
						srchInfo.put("srchStatusList", new ArrayList<String>(Arrays.asList("ECM003")));
					}
				//반입적용취소
				}else{
					if(!parentMenu.equals("regist")) {
						srchInfo.put("srchStatusList", new ArrayList<String>(Arrays.asList("ECM030")));
					} else {
						srchInfo.put("srchStatusList", new ArrayList<String>(Arrays.asList("ECM005")));
					}
				}
				srchInfo.put("srchOdJobNoList", srchOdJobNoList);	//선택 건의 odJobNo list
				
				//검색조건으로 조회
				List<Map<String, Object>> list = selectOrderList(srchInfo);
				if(list == null || list.size() == 0) {
					functionSaveResult.setResultCode("notOrder");
					return functionSaveResult;
				}
				hblNoList.addAll(list);
			}
		}
		//검색일때 
		else {
			Map<String,Object> srchInfo = functionOrder.getSrchInfo();
			if(srchInfo == null) {
				functionSaveResult.setResultCode("notOrder");
				return functionSaveResult;
			}
			srchInfo.put("cryptographyYn", functionOrder.getCryptographyYn());
			srchInfo.put("func","carryin");
			//반입적용(ECM030)
			if("S".equals(carryinFunc)) {
				if(!parentMenu.equals("regist")) {
					srchInfo.put("srchStatusList", new ArrayList<String>(Arrays.asList("ECM010","ECM020")));
				} else {
					if(!parentMenu.equals("regist")) {
						srchInfo.put("srchStatusList", new ArrayList<String>(Arrays.asList("ECM030")));
					} else {
						srchInfo.put("srchStatusList", new ArrayList<String>(Arrays.asList("ECM005")));
					}
				}
			//반입적용취소
			}else{
				srchInfo.put("srchStatusList", new ArrayList<String>(Arrays.asList("ECM030")));
			}
			//검색조건으로 조회
			List<Map<String, Object>> list = selectOrderList(srchInfo);
			if(list == null || list.size() == 0) {
				functionSaveResult.setResultCode("notOrder");
				return functionSaveResult;
			}
			hblNoList.addAll(list);
		}
		
		int houseCnt = hblNoList.size();
		String bound = "";
		String boundType = "";
		if(houseCnt > 0) {
			bound = Util.null2str(hblNoList.get(0).get("bound"));
			boundType = Util.null2str(hblNoList.get(0).get("boundType"));
		}
		//1000 건씩 나눠서 처리함.
		for(int i = 0; i < houseCnt; i+=1000){
			if(i + 1000 < houseCnt) {
				functionOrder.setOdList(hblNoList.subList(i, i + 1000));
			}else{
				functionOrder.setOdList(hblNoList.subList(i, houseCnt));
			}
			
			tarList = functionOrder.getOdList();
			spliceTarList.add(tarList);
			
			//화면단에서 조회된 건수와 lastPodStatus 조건으로 줘 다시 조회해온 건수가 다를 경우 화면단에서 오류 반환
			//ex.검색 건인데 반입/오더생성 데이터 섞여 있을 경우 반입 상태인 데이터 때문에 반입 처리 할 수 없고 오더생성 상태인 데이터 때문에 반입취소 할 수 없음
			// 2023-12-14 불필요 주석
//			if(functionOrder.getHouseCnt() != houseCnt) {
//				functionSaveResult.setResultCode("carryinCntNotMatch");
//				return functionSaveResult;
//			}
			
			//반입적용 검색일때만 유효성 체크 선택일 경우는 화면단에서 유효성 체크했으므로 -> 선택건인 경우에도 다시 조회해오기로 했으므로 유효성도 다시 체크하도록 함
			if(tarList.size() > 0 && carryinFunc.equals("S") && scanFlag /*("B".equals(chk) || scanFlag)*/){
				for(Map<String, Object> errMap : tarList){
					if(Util.null2str(errMap.get("chk1")).equals("Y")){
						errList1.add(errMap);
					}
					if(Util.null2str(errMap.get("chk2")).equals("Y")){
						errList2.add(errMap);
					}				
				}
			}
		}
		
		if(errList1.size() > 0){
			functionSaveResult.setResultCode("carryinIncongruityOrder");
			resultData.put("errList1", errList1);
			
			Map<String, Object> err1MsgList = new HashMap<String, Object>();
			//상태값 중복제거 하고 추출
			for(int k = 0; k < errList1.size(); k++){	
				Map<String, Object> errRow = errList1.get(k);
				String nm = (String)errRow.get("lastPodStatusNm");
				err1MsgList.put(nm, nm);
			}
			
			//라스트 상태 컴마형태로 출력
			String errMsg1 = "";
			for(Map.Entry<String,Object> entry : err1MsgList.entrySet()){
				if(false == "".equals(errMsg1)) {
					errMsg1 = errMsg1 + ",";
				}

				errMsg1 = errMsg1 + entry.getKey();
			}
			resultData.put("errMsg1", errMsg1);
		}
		
		//다시 반입처리하는 경우 -> 이미 lastPodStatus가 ECM030인 경우 다시 반입하지 못하도록 일단 막아놓음
		if(false == Util.null2str(functionOrder.getReCarryYn()).equals("Y")) {
			if(errList2.size() > 0){
				functionSaveResult.setResultCode("carryinIncongruityOrder");
				resultData.put("errList2", errList2);
			}
		}
		
		//에러가 있으면 return
		if(!Util.null2str(functionSaveResult.getResultCode()).equals("")){
			if(Util.null2str(functionSaveResult.getResultCode()).equals("carryinIncongruityOrder")){
				functionSaveResult.setResultData(resultData);
			}
			return functionSaveResult;
		}
		
		SqlSession sqlSession = null;
		
//		BulkData bulkData = null;
//		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
//		Map<String, Object> logMap = null;
//		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
//		tmpLogMap.put("ent_code", functionOrder.getEntCode());
//		tmpLogMap.put("ent_business_code", functionOrder.getUserBusinessCode());
//		tmpLogMap.put("solution", "ECM");
//		tmpLogMap.put("process_user_id", functionOrder.getUpdateUserId());
//		tmpLogMap.put("log_job_type", "DATA");
//		tmpLogMap.put("job_func_type", "UPDATE");
//		tmpLogMap.put("result", "ok");
//		tmpLogMap.put("process_date_time", System.currentTimeMillis());
//		tmpLogMap.put("ent_business_print_name", functionOrder.getLogUserBusinessPrintName());
//		tmpLogMap.put("process_user_name", functionOrder.getLogUserName());
//		tmpLogMap.put("process_user_name_eng", functionOrder.getLogUserNameEng());
		
//		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			FunctionMapper functionBatchMapper = sqlSession.getMapper(FunctionMapper.class);
			
			Map<String, Object> podMap = new HashMap<String, Object>();
			podMap.put("entCode", functionOrder.getEntCode());
			podMap.put("entBusinessCode", "");
			podMap.put("gmtTime", functionOrder.getGmtTime());
			podMap.put("solution", "ECM");
			podMap.put("updateUserId", functionOrder.getUpdateUserId());
			podMap.put("stockType", "POD");
			podMap.put("stockSign", "POD");
			podMap.put("stockSeqLength", 8);
			podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			podMap.put("stockRemark", "ECM POD JOB NO");
			
			List<Map<String, Object>> insertPodList = new ArrayList<Map<String, Object>>();
			Map<String, Object> tmpInsertPodMap = null;
			
			//반입적용
			if("S".equals(carryinFunc)) {
				//1. 상태가 ‘HTA_H(발송보류, 홀드) 인 경우에는 업데이트 하지 않음.  
				//2. TB_ECM_OD_ADDITIONAL.LAST_POD_STATUS-> HTC(선적지반입) ECM030 로 업데이트 (등록 메뉴일 경우 ECM005)
				//3. POD 테이블에 UPDATE/INSERT
				if(!parentMenu.equals("regist")) {
					podMap.put("podStatus", "ECM030");
					podMap.put("POD_STATUS", "ECM030");
					functionOrder.setInsStatus("ECM030");
				}else {
					podMap.put("podStatus", "ECM005");
					podMap.put("POD_STATUS", "ECM005");
					functionOrder.setInsStatus("ECM005");
				}
				podMap.put("REMARKS", "반입적용");
				tmpMap = new HashMap<String,Object>();
				//1000*x 건씩 리스트 처리
				int seq = 1;
				for(int i = 0; i < spliceTarList.size(); i++){	
					
					List<Map<String, Object>> odList = spliceTarList.get(i);
					functionOrder.setOdNoList(odList);
					//last_pod_status ECM030 로 업데이트
					functionMapper.updateCarryin(functionOrder);
					
					//한건씩 돌면서 한건씩 pod 테이블 저장
					for(int j = 0; j < odList.size(); j++) {
						Map<String, Object> odRow = odList.get(j);
						//존재하면 update-> lastPodStatus기준으로 수정되어 반입 가능한 경우(ECM010,ECM020) POD 테이블에 ECM030 없어야 하므로 업데이트 부분 삭제함
//						if(!Util.null2str(odRow.get("podCarryIn")).equals("")) {
//							odRow.put("POD_STATUS", "ECM030");
//							odRow.put("REMARKS", "반입적용");
//							odRow.put("updateUserId", functionOrder.getUpdateUserId());
//							odRow.put("gmtTime", functionOrder.getGmtTime());
//							odRow.put("POD_JOB_NO", odRow.get("podCarryIn"));
//							//pod 테이블 UPDATE
//							functionBatchMapper.updatePodInfo(odRow);
//						}else{
							tmpInsertPodMap = new HashMap<String, Object>();
							tmpInsertPodMap.put("odJobNo", odRow.get("odJobNo"));
							tmpInsertPodMap.put("hblNo", odRow.get("hblNo"));
							tmpInsertPodMap.put("POD_JOB_NO", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
							insertPodList.add(tmpInsertPodMap);
//						}

//						bulkData = new BulkData();
//						bulkData.setActionType(BulkData.Type.CREATE);
//						bulkData.setIndexName(index);
//						bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
//						logMap = new HashMap<String, Object>();
//						logMap.putAll(tmpLogMap);
//						logMap.put("reference_no_id", bulkData.getId());
//						logMap.put("bound", Util.null2str(odRow.get("bound")));
//						logMap.put("bound_type",Util.null2str(odRow.get("boundType")));
//						logMap.put("job_no", Util.null2str(odRow.get("odJobNo")));
//						logMap.put("description","Carry In House Order [HBL_NO : "+ Util.null2str(odRow.get("hblNo"))+"]");
//						logMap.put("job_log", new Gson().toJson(odRow));
//						bulkData.setJsonMap(logMap);
//						bulkList.add(bulkData);
					}
					
					for(Map<String, Object> insertPodMap : insertPodList){
						podMap.put("odJobNo", insertPodMap.get("odJobNo"));
						podMap.put("hblNo", insertPodMap.get("hblNo"));
						podMap.put("POD_JOB_NO", insertPodMap.get("POD_JOB_NO"));
						functionBatchMapper.insertPodInfo(podMap);
					}
					
					sqlSession.flushStatements();
					sqlSession.clearCache();
					sqlSession.commit();
				}
				
				// 처리 로그 기록
				functionOrder.setOdList(null);
				functionOrder.setOdNoList(null);
				processLogs.setEntCode(functionOrder.getEntCode());
				processLogs.setEntBusinessCode(functionOrder.getUserBusinessCode());
				processLogs.setSolution("ECM");
				processLogs.setBound(bound);
				processLogs.setBoundType(boundType);
				processLogs.setProcessUserId(functionOrder.getUpdateUserId());
				processLogs.setJobNo("");
				processLogs.setLogJobType("DATA");
				processLogs.setDescription("CARRY IN");
				if(!"A".equals(chk)) {	//선택건이 아닐때(검색건)
					processLogs.setJobLog(new Gson().toJson(functionOrder));
				}else{
					processLogs.setJobLog("");
				}
				processLogs.setResult("ok");
				processLogs.setEntBusinessPrintName(functionOrder.getLogUserBusinessPrintName());
				processLogs.setProcessUserName(functionOrder.getLogUserName());
				processLogs.setProcessUserNameEng(functionOrder.getLogUserNameEng());
				processLogs.setJobFuncType("UPDATE");
			}	
			//반입취소
			else if("C".equals(Util.null2str(carryinFunc))) {
				//1. POD 히스토리에서 ECM030 삭제
				//3. POD 히스토리에서 MAX 값을 가져온 후 TB_ECM_OD_ADDITIONAL.LAST_POD_STATUS 업데이트
				
				tmpMap = new HashMap<String,Object>();
				//1000*x 건씩 리스트 처리
				int seq = 1;
				for(int i = 0; i < spliceTarList.size(); i++){	
					
					List<Map<String, Object>> odList = spliceTarList.get(i);
					functionOrder.setOdNoList(odList);
					if(!parentMenu.equals("regist")) {
						functionOrder.setDelStatus("ECM030");
					}else {
						functionOrder.setDelStatus("ECM005");
					}
					functionMapper.deletePodInfo(functionOrder);
					
					for(int j = 0; j < odList.size(); j++) {
						Map<String, Object> odRow = odList.get(j);
						functionOrder.setOdJobNo(Util.null2str(odRow.get("odJobNo")));
						functionBatchMapper.updateLastPodStatus2(functionOrder);
						
//						bulkData = new BulkData();
//						bulkData.setActionType(BulkData.Type.CREATE);
//						bulkData.setIndexName(index);
//						bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
//						logMap = new HashMap<String, Object>();
//						logMap.putAll(tmpLogMap);
//						logMap.put("reference_no_id", bulkData.getId());
//						logMap.put("bound", Util.null2str(odRow.get("bound")));
//						logMap.put("bound_type",Util.null2str(odRow.get("boundType")));
//						logMap.put("job_no", Util.null2str(odRow.get("odJobNo")));
//						logMap.put("description","Carry In Cancel House Order [HBL_NO : "+ Util.null2str(odRow.get("hblNo"))+"]");
//						logMap.put("job_log", new Gson().toJson(odRow));
//						bulkData.setJsonMap(logMap);
//						bulkList.add(bulkData);
					}
					sqlSession.flushStatements();
					sqlSession.clearCache();
					sqlSession.commit();
				}
				
//				commonService.insertLogBulk(bulkList);
				//처리 로그 기록
				functionOrder.setOdList(null);
				functionOrder.setOdNoList(null);
				processLogs.setEntCode(functionOrder.getEntCode());
				processLogs.setEntBusinessCode(functionOrder.getUserBusinessCode());
				processLogs.setSolution("ECM");
				processLogs.setBound(bound);
				processLogs.setBoundType(boundType);
				processLogs.setProcessUserId(functionOrder.getUpdateUserId());
				processLogs.setJobNo("");
				processLogs.setLogJobType("DATA");
				processLogs.setDescription("Carry In Cancel House Order");
				if(!"A".equals(chk)) {	//선택건이 아닐때(검색건)
					processLogs.setJobLog(new Gson().toJson(functionOrder));
				}else{
					processLogs.setJobLog("");
				}
				processLogs.setResult("ok");
				processLogs.setEntBusinessPrintName(functionOrder.getLogUserBusinessPrintName());
				processLogs.setProcessUserName(functionOrder.getLogUserName());
				processLogs.setProcessUserNameEng(functionOrder.getLogUserNameEng());
				processLogs.setJobFuncType("UPDATE");
				
			}
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}

		commonService.insertLog(processLogs, request);
		return functionSaveResult;
	}
	
	/**
	 * Function - 반입적용/취소 (등록 화면용)
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public FunctionSaveResult carryinRegistration(FunctionOrder functionOrder, HttpServletRequest request) throws Exception{
		Util.accessControlDatabase(functionOrder, "gmtTime", false, null, null, null, null);
		FunctionSaveResult functionSaveResult = new FunctionSaveResult();
//		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		ProcessLogs processLogs = new ProcessLogs();
		
		Map<String, Object> resultData = new HashMap<String, Object>();
		List<Map<String, Object>> tarList;
		//반입적용 에러 메시지 출력(rowData.BLSTATUSNM + “인 건 은 반입할수 없습니다”)
		List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();
		List<List<Map<String, Object>>> spliceTarList = new ArrayList<List<Map<String, Object>>>();
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		
		String jobFunc = null;
		//반입모드 ( S:반입적용, C:반입취소 )
		String carryinFunc = Util.null2str(functionOrder.getFuncMode());
		//A:선택체크, B:검색(전체)
		String chk = Util.null2str(functionOrder.getChkMode());
		
		/** 반입적용 불가오더 체크 */
		List<Map<String, Object>> hblNoList = new ArrayList<Map<String, Object>>();
		//선택일때
//		if("A".equals(chk)) {
//			hblNoList.addAll(functionOrder.getOdList());
//		}
		//검색일때 
//		else {
			Map<String,Object> srchInfo = functionOrder.getSrchInfo();
			if(srchInfo == null) {
				functionSaveResult.setResultCode("notOrder");	//화면에서 처리 - 검색건이 존재하지 않습니다.
				return functionSaveResult;
			}
			srchInfo.put("func","carryin");
			//등록->반입/취소
			//선택 건인 경우에도 해당 odJobNo로 데이터 다시 조회해오는 것으로 수정됨
			if("A".equals(chk)) {
				List<String> srchOdJobNoList = new ArrayList<String>();
				for(Map<String, Object> od : functionOrder.getOdList()) {
					srchOdJobNoList.add(Util.null2str(od.get("odJobNo")));
				}
				srchInfo.put("srchOdJobNoList", srchOdJobNoList);
			}
			//반입적용(ECM005)
			if("S".equals(carryinFunc)) {
				srchInfo.put("srchStatusList", new ArrayList<String>(Arrays.asList("ECM003")));
			//반입적용취소
			}else{
				srchInfo.put("srchStatusList", new ArrayList<String>(Arrays.asList("ECM005")));
			}
			//검색조건으로 조회
			srchInfo.put("cryptographyYn", functionOrder.getCryptographyYn());
			List<Map<String, Object>> list = selectOrderListRegistration(srchInfo);
			if(list == null || list.size() == 0) {
				functionSaveResult.setResultCode("notOrder");	//화면에서 처리 - 검색건이 존재하지 않습니다.
				return functionSaveResult;
			}
			hblNoList.addAll(list);
//		}
		
		int houseCnt = hblNoList.size();
		String bound = "";
		String boundType = "";
		if(houseCnt > 0) {
			bound = Util.null2str(hblNoList.get(0).get("bound"));
			boundType = Util.null2str(hblNoList.get(0).get("boundType"));
		}
		//1000 건씩 나눠서 처리함.
		for(int i = 0; i < houseCnt; i+=1000){
			if(i + 1000 < houseCnt) {
				functionOrder.setOdList(hblNoList.subList(i, i + 1000));
			}else{
				functionOrder.setOdList(hblNoList.subList(i, houseCnt));
			}
			
			tarList = functionOrder.getOdList();	//전달받은 그리드 리스트 전체
			spliceTarList.add(tarList);
			
			//화면단에서 조회된 건수와 lastPodStatus 조건으로 줘 다시 조회해온 건수가 다를 경우 화면단에서 오류 반환
			//ex.검색 건인데 반입/오더생성 데이터 섞여 있을 경우 반입 상태인 데이터 때문에 반입 처리 할 수 없고 오더생성 상태인 데이터 때문에 반입취소 할 수 없음
			if(functionOrder.getHouseCnt() != houseCnt) {
				functionSaveResult.setResultCode("carryinCntNotMatch");
				return functionSaveResult;
			}
			
			//반입적용 검색일때만 유효성 체크 (선택일 경우는 화면단에서 유효성 체크했으므로) -> 선택 건도 데이터 다시 조회해오기로 했으므로 유효성 체크 다시 해줌
			if (tarList.size() > 0 && carryinFunc.equals("S") /* && "B".equals(chk) */){
				for(Map<String, Object> errMap : tarList){
					if(!Util.null2str(errMap.get("lastPodStatus")).equals("ECM003")){
						errList.add(errMap);
					}		
				}
			}
		}
		
		if(errList.size() > 0){	//lastPodStatus가 ECM003아닌 데이터 있는 경우
			functionSaveResult.setResultCode("carryinIncongruityOrder");
			resultData.put("errList", errList);
			
			Map<String, Object> errMsgList = new HashMap<String, Object>();
			//상태값 중복제거 하고 추출
			for(int k = 0; k < errList.size(); k++){	
				Map<String, Object> errRow = errList.get(k);
				String nm = (String)errRow.get("lastPodStatusNm");
				errMsgList.put(nm, nm);
			}
			
			//라스트 상태 컴마형태로 출력
			String errMsg = "";
			for(Map.Entry<String,Object> entry : errMsgList.entrySet()){
				if(false == "".equals(errMsg)) {
					errMsg = errMsg + ",";
				}

				errMsg = errMsg + entry.getKey();
			}
			resultData.put("errMsg", errMsg);
		}
		
		//에러가 있으면 return
		if(!Util.null2str(functionSaveResult.getResultCode()).equals("")){
			if(Util.null2str(functionSaveResult.getResultCode()).equals("carryinIncongruityOrder")){
				functionSaveResult.setResultData(resultData);
			}
			return functionSaveResult;
		}

		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", functionOrder.getEntCode());
		tmpLogMap.put("ent_business_code", functionOrder.getUserBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("process_user_id", functionOrder.getUpdateUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		tmpLogMap.put("ent_business_print_name", functionOrder.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", functionOrder.getLogUserName());
		tmpLogMap.put("process_user_name_eng", functionOrder.getLogUserNameEng());
		
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			FunctionMapper functionBatchMapper = sqlSession.getMapper(FunctionMapper.class);
			
			Map<String, Object> podMap = new HashMap<String, Object>();
			podMap.put("entCode", functionOrder.getEntCode());
			podMap.put("entBusinessCode", "");
			podMap.put("gmtTime", functionOrder.getGmtTime());
			podMap.put("solution", "ECM");
			podMap.put("updateUserId", functionOrder.getUpdateUserId());
			podMap.put("stockType", "POD");
			podMap.put("stockSign", "POD");
			podMap.put("stockSeqLength", 8);
			podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			podMap.put("stockRemark", "ECM POD JOB NO");
			
			List<Map<String, Object>> insertPodList = new ArrayList<Map<String, Object>>();
			Map<String, Object> tmpInsertPodMap = null;
			
			//반입적용
			if("S".equals(carryinFunc)) {
				podMap.put("podStatus", "ECM005");
				podMap.put("POD_STATUS", "ECM005");
				podMap.put("REMARKS", "반입적용");
				//1. 상태가 'HTA_H(발송보류, 홀드)' 인 경우에는 업데이트 하지 않음.  
	        	//2. TB_ECM_OD_ADDITIONAL.LAST_POD_STATUS-> ECM005 로 업데이트
	            //3. POD 테이블에 UPDATE/INSERT
				functionOrder.setInsStatus("ECM005");
				tmpMap = new HashMap<String,Object>();
				int seq = 1;
				//1000*x 건씩 리스트 처리
				for(int i = 0; i < spliceTarList.size(); i++){	
					
					List<Map<String, Object>> odList = spliceTarList.get(i);
					functionOrder.setOdNoList(odList);
					//last_pod_status ECM005 로 업데이트
					functionMapper.updateCarryin(functionOrder);
					
					//한건씩 돌면서 한건씩 pod 테이블 저장, ES저장
					for(int j = 0; j < odList.size(); j++) {
						Map<String, Object> odRow = odList.get(j);
						//존재하면 update-> lastPodStatus기준으로 수정되어 반입 가능한 경우(ECM003) POD 테이블에 ECM005 없어야 하므로 업데이트 부분 삭제함
//						if(!Util.null2str(odRow.get("podCarryIn")).equals("")) {
//							odRow.put("POD_STATUS", "ECM005");
//							odRow.put("REMARKS", "반입적용");
//							odRow.put("updateUserId", functionOrder.getUpdateUserId());
//							odRow.put("gmtTime", functionOrder.getGmtTime());
//							odRow.put("POD_JOB_NO", odRow.get("podCarryIn"));
//							//pod 테이블 UPDATE
//							functionBatchMapper.updatePodInfo(odRow);
//						}
//						else {
							tmpInsertPodMap = new HashMap<String, Object>();
							tmpInsertPodMap.put("odJobNo", odRow.get("odJobNo"));
							tmpInsertPodMap.put("hblNo", odRow.get("hblNo"));
							tmpInsertPodMap.put("POD_JOB_NO", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
							insertPodList.add(tmpInsertPodMap);
//						}
						
//						bulkData = new BulkData();
//						bulkData.setActionType(BulkData.Type.CREATE);
//						bulkData.setIndexName(index);
//						bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
//						logMap = new HashMap<String, Object>();
//						logMap.putAll(tmpLogMap);
//						logMap.put("reference_no_id", bulkData.getId());
//						logMap.put("bound", Util.null2str(odRow.get("bound")));
//						logMap.put("bound_type", Util.null2str(odRow.get("boundType")));
//						logMap.put("job_no", Util.null2str(odRow.get("odJobNo")));
//						logMap.put("description","Carry In Registration [HBL_NO : "+ Util.null2str(odRow.get("hblNo"))+"]");
//						logMap.put("job_log", new Gson().toJson(odRow));
//						bulkData.setJsonMap(logMap);
//						bulkList.add(bulkData);
					}
					for(Map<String, Object> insertPodMap : insertPodList){
						podMap.put("odJobNo", insertPodMap.get("odJobNo"));
						podMap.put("hblNo", insertPodMap.get("hblNo"));
						podMap.put("POD_JOB_NO", insertPodMap.get("POD_JOB_NO"));
						functionBatchMapper.insertPodInfo(podMap);
					}
					sqlSession.flushStatements();
					sqlSession.clearCache();
					sqlSession.commit();
					insertPodList = new ArrayList<Map<String, Object>>();
				}
				// 처리 로그 기록
				processLogs.setEntCode(functionOrder.getEntCode());
				processLogs.setEntBusinessCode(functionOrder.getUserBusinessCode());
				processLogs.setSolution("ECM");
				processLogs.setBound(bound);
				processLogs.setBoundType(boundType);
				processLogs.setProcessUserId(functionOrder.getUpdateUserId());
				processLogs.setJobNo("");
				processLogs.setLogJobType("DATA");
				processLogs.setDescription("CARRYIN REGISTRATION");
//				if("A".equals(chk)) {
//					functionOrder.setOdList(hblNoList);
//				}else{
//					functionOrder.setOdList(null);
//				}
				if(!"A".equals(chk)) {
					functionOrder.setOdList(null);
					processLogs.setJobLog(new Gson().toJson(functionOrder));
				}else{
					processLogs.setJobLog("");
				}
				processLogs.setResult("ok");
				processLogs.setEntBusinessPrintName(functionOrder.getLogUserBusinessPrintName());
				processLogs.setProcessUserName(functionOrder.getLogUserName());
				processLogs.setProcessUserNameEng(functionOrder.getLogUserNameEng());
				processLogs.setJobFuncType("UPDATE");
			}	
			//반입취소
			else if("C".equals(Util.null2str(carryinFunc))) {
				//1. POD 히스토리에서 ECM005 삭제
				//3. POD 히스토리에서 MAX 값을 가져온 후 TB_ECM_OD_ADDITIONAL.LAST_POD_STATUS 업데이트
				
				tmpMap = new HashMap<String,Object>();
				//1000*x 건씩 리스트 처리
				int seq = 1;
				for(int i = 0; i < spliceTarList.size(); i++){	
					
					List<Map<String, Object>> odList = spliceTarList.get(i);
					functionOrder.setOdNoList(odList);
					functionOrder.setDelStatus("ECM005");
					functionMapper.deletePodInfo(functionOrder);
					
					//한건씩 돌면서 한건씩 pod 테이블 저장
					for(int j = 0; j < odList.size(); j++) {
						Map<String, Object> odRow = odList.get(j);
						functionOrder.setOdJobNo(Util.null2str(odRow.get("odJobNo")));
						functionBatchMapper.updateLastPodStatus2(functionOrder);
						
//						bulkData = new BulkData();
//						bulkData.setActionType(BulkData.Type.CREATE);
//						bulkData.setIndexName(index);
//						bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
//						logMap = new HashMap<String, Object>();
//						logMap.putAll(tmpLogMap);
//						logMap.put("reference_no_id", bulkData.getId());
//						logMap.put("bound", Util.null2str(odRow.get("bound")));
//						logMap.put("bound_type", Util.null2str(odRow.get("boundType")));
//						logMap.put("job_no", Util.null2str(odRow.get("odJobNo")));
//						logMap.put("description","Carry In Cancel [HBL_NO : "+ Util.null2str(odRow.get("hblNo"))+"]");
//						logMap.put("job_log", new Gson().toJson(odRow));
//						bulkData.setJsonMap(logMap);
//						bulkList.add(bulkData);
					}
					sqlSession.flushStatements();
					sqlSession.clearCache();
					sqlSession.commit();
				}
				
				// 처리 로그 기록
				processLogs.setEntCode(functionOrder.getEntCode());
				processLogs.setEntBusinessCode(functionOrder.getUserBusinessCode());
				processLogs.setSolution("ECM");
				processLogs.setBound(bound);
				processLogs.setBoundType(boundType);
				processLogs.setProcessUserId(functionOrder.getUpdateUserId());
				processLogs.setJobNo("");
				processLogs.setLogJobType("DATA");
				processLogs.setDescription("Carry In Cancel");
				if(!"A".equals(chk)) {
					functionOrder.setOdList(null);
					processLogs.setJobLog(new Gson().toJson(functionOrder));
				}else{
					processLogs.setJobLog("");
				}
				processLogs.setResult("ok");
				processLogs.setEntBusinessPrintName(functionOrder.getLogUserBusinessPrintName());
				processLogs.setProcessUserName(functionOrder.getLogUserName());
				processLogs.setProcessUserNameEng(functionOrder.getLogUserNameEng());
				processLogs.setJobFuncType("UPDATE");
//				commonService.insertLogBulk(bulkList);
			}
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
		
		commonService.insertLog(processLogs, request);
		return functionSaveResult;
	}
	
	/**
	 * function 에서 전자상거래Customer Order function 할 bl list 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectOrderListRegistration(Map<String,Object> search) throws Exception {
		List<Map<String, Object>> list = null;
		
		
		//등록 화면에서 호출하는 반입/취소일 경우이므로 userType이 entUser인 부분 주석처리
		//userType이 entUser일 때 selectOrderListEnt 쿼리에서는 lastPodStatus 조건을 줄 때 다른 파라미터 이름을 사용하므로
		//해당 부분 사용해야 할 경우 호출부에서 분기 처리해줘야 함
//		if(Util.null2str(search.get("userType")).equals("entUser")){
//			list = functionMapper.selectOrderListEnt(search);	/** 업데이트대상조회 */
//		}else{
			//유저의 거래처코드 추출
			Map<String,Object> param = new HashMap<String,Object>();
			param.put("entCode", search.get("srchEntCode"));
			param.put("userId", search.get("srchLoginId"));
			Map<String,Object> userInfo = orderCustomerMapper.selectUserInfo(param);
			if(userInfo != null) {
				search.put("loginCustomerCd", (String)userInfo.get("CUSTOMER_CODE"));
				search.put("loginCustomerLev", (String)userInfo.get("CUSTOMER_LEVEL"));
				search.put("loginCustomerUpperLev", (String)userInfo.get("UPPER_LEVEL"));
				search.put("loginCustomerLowLev", (String)userInfo.get("LOW_LEVEL"));
			}
			
			//전체 건 검색
			if("ORDER_CUSTOMER".equals(search.get("screen"))){
				Util.getCryptographyYn(search);
			}
			Util.accessControlDatabase(search, "userLanguage|srchCusType|srchDateFormat", false, "srchDateGbn", "od.PO_DATE", "srchNoType", "od.PO_NO");
			list = functionMapper.selectOrderList(search);
//		}
		
		return list;
	}
	
	/**
	 * 반출스켄
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public FunctionSaveResult carryout(FunctionOrder functionOrder, HttpServletRequest request) throws Exception{
		FunctionSaveResult functionSaveResult = new FunctionSaveResult();
		
		//호출메뉴
		String parentMenu = Util.null2str(functionOrder.getParentMenu());
		
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		tmpMap.put("userLanguage", functionOrder.getUserLanguage());
		tmpMap.put("chkMode", "A");
		tmpMap.put("hblNo", functionOrder.getHblNo());
		tmpMap.put("func", "carryout");
		/** 스캔은 비엘번호 직접입력하므로 권한체크함 */
		tmpMap.put("ecmCustomerCode", functionOrder.getEcmCustomerCode());
		tmpMap.put("ecmCustomerLevel", functionOrder.getEcmCustomerLevel());
		tmpMap.put("parentMenu", parentMenu);
		tmpMap.put("cryptographyYn", functionOrder.getCryptographyYn());
		Util.accessControlDatabase(tmpMap, "ecmCustomerLevel|userLanguage", false, null, "", null, "");
		functionOrder.setOdNoList(functionMapper.selectOrderListEnt(tmpMap));
		if(functionOrder.getOdNoList().size() == 0){
			functionSaveResult.setResultCode("notOrder");
			return functionSaveResult;
		}
		
		if(!parentMenu.equals("regist")) {
			functionOrder.setInsStatus("ECM050");
		} else {
			functionOrder.setInsStatus("ECM007");
		}
		functionMapper.updateLastPodStatus(functionOrder);
		
		Map<String, Object> odRow = functionOrder.getOdNoList().get(0);
		if(!parentMenu.equals("regist")) {
			odRow.put("POD_STATUS", "ECM050");
		} else {
			odRow.put("POD_STATUS", "ECM007");
		}
		odRow.put("REMARKS", "선적지반출");
		odRow.put("SOLUTION", "ECM");
		
		odRow.put("updateUserId", functionOrder.getUpdateUserId());
		odRow.put("gmtTime", functionOrder.getGmtTime());
		
		//존재하면 update
		if(!Util.null2str(odRow.get("podCarryout")).equals("")) {	
			Util.accessControlDatabase(odRow, "gmtTime", false, null, "", null, "");
			odRow.put("POD_JOB_NO", odRow.get("podCarryout"));
			functionMapper.updatePodInfo(odRow);
		}else{							
			tmpMap.put("gmtTime", functionOrder.getGmtTime());
			tmpMap.put("entCode", functionOrder.getEntCode());
			tmpMap.put("entBusinessCode", "");
			tmpMap.put("updateUserId", functionOrder.getUpdateUserId());
			tmpMap.put("solution", "ECM");
			tmpMap.put("stockType", "POD");
			tmpMap.put("stockSign", "POD");
			tmpMap.put("stockSeqLength", 8);
			tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			tmpMap.put("stockRemark", "ECM POD JOB NO");
			odRow.put("POD_JOB_NO",DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001");
			
			odRow.put("entCode", functionOrder.getEntCode());
			odRow.put("entBusinessCode", functionOrder.getEntBusinessCode());
			odRow.put("userLanguage", functionOrder.getUserLanguage());
			//pod 테이블 INSERT
			Util.accessControlDatabase(odRow, "gmtTime", false, null, "", null, "");
			functionMapper.insertPodInfo(odRow);
		}
		
		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(functionOrder.getEntCode());
		processLogs.setEntBusinessCode(functionOrder.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(Util.null2str(odRow.get("bound")));
		processLogs.setBoundType(Util.null2str(odRow.get("boundType")));
		processLogs.setProcessUserId(functionOrder.getUpdateUserId());
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType("UPDATE");
		processLogs.setDescription("HOUSE ORDER CARRYOUT SCAN [HBL_NO : "+ Util.null2str(odRow.get("hblNo"))+"]");
//		processLogs.setJobLog(new Gson().toJson(functionOrder));
		processLogs.setJobLog("");
		processLogs.setResult("ok");
		processLogs.setJobNo(Util.null2str(odRow.get("odJobNo")));
		processLogs.setEntBusinessPrintName(functionOrder.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(functionOrder.getLogUserName());
		processLogs.setProcessUserNameEng(functionOrder.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
				
		return functionSaveResult;
	}
	/**
	 * function 에서 전자상거래Customer Order function 할 bl list 조회
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
		
		
		if(Util.null2str(search.get("userType")).equals("entUser")){	//오더조회에서 반입 호출했을 때 -> 다시 사용할 경우 lastPodStatus 조회 조건 추가해야함
			if("ORDER".equals(search.get("srchQueryType"))){
				Util.getCryptographyYn(search);
			}
			Util.accessControlDatabase(search, "srchDateFormat|userLanguage", false, "srchDateGbn", "|noDateChk|od.PO_DATE|od.AR_DATE|od.OB_DATE|ad.MF_DATE|ad.SIMPLE_DOC_DATE", "srchNoType", "|od.PO_NO|od.HBL_NO|od.MBL_NO"
					+ "|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO|od.SHIPMENT_BOX_ID|ad.RETURN_REQUEST_ID");
			String func = Util.null2str(search.get("func"));
			if(func.equals("obDate")){
				search.put("functionConsoleChkYn", "Y");
				list = functionMapper.selectOrderListEnt(search);	/** 콘솔된건 있는지 체크 */
				if(list != null && list.size() > 0){
					search.put("errConsolData", "Y");
					return list;
				}
				search.put("functionConsoleChkYn", "N");
			}
			list = functionMapper.selectOrderListEnt(search);	/** 업데이트대상조회 */
		}else{
			//유저의 거래처코드 추출
			Map<String,Object> param = new HashMap<String,Object>();
			param.put("entCode", search.get("srchEntCode"));
			param.put("userId", search.get("srchLoginId"));
			Map<String,Object> userInfo = orderCustomerMapper.selectUserInfo(param);
			if(userInfo != null) {
				search.put("loginCustomerCd", (String)userInfo.get("CUSTOMER_CODE"));
				search.put("loginCustomerLev", (String)userInfo.get("CUSTOMER_LEVEL"));
				search.put("loginCustomerUpperLev", (String)userInfo.get("UPPER_LEVEL"));
				search.put("loginCustomerLowLev", (String)userInfo.get("LOW_LEVEL"));
			}
			
			
			if("penaltyVali".equals(search.get("func"))) {
				Util.accessControlDatabase(search, "userLanguage|srchDateFormat|srchCusType", false, "srchDateGbn", "od.PO_DATE|od.AR_DATE|od.OB_DATE|ad.MF_DATE|ad.SIMPLE_DOC_DATE|noDateChk", "srchNoType", "od.OD_JOB_NO|od.PO_NO|od.HBL_NO"
						+ "|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.CONSIGNEE_HP|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO|od.MASTER_OD_JOB_NO");
			}else if("ORDER_CUSTOMER".equals(Util.null2str(search.get("screen")))) {
				Util.getCryptographyYn(search);
				Util.accessControlDatabase(search, "userLanguage|srchDateFormat|srchCusType", false, "srchDateGbn", "od.PO_DATE|od.AR_DATE|od.OB_DATE", "srchNoType", "od.PO_NO|od.HBL_NO"
						+ "|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.CONSIGNEE_HP|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO");
			}else if("FHL".equals(Util.null2str(search.get("screen")))) {
				Util.accessControlDatabase(search, "srchDateFormat|srchCusType", false, "srchDateGbn", "od.AR_DATE|od.OB_DATE|ad.SIMPLE_DOC_DATE|noDateChk|ad.MF_DATE", "srchNoType", "od.MBL_NO|od.HBL_NO|od.MRN|ad.SIMPLE_DOC_NO");
			}else if("CONSOL".equals(Util.null2str(search.get("screen")))) {
				Util.accessControlDatabase(search, "srchDateFormat|srchCusType", false, null, "", "srchNoType", "od.MBL_NO|od.HBL_NO");
			}
			
			//전체 건 검색
			if(Util.null2str(search.get("func")).equals("obDate")){
				search.put("functionConsoleChkYn", "Y");
				list = functionMapper.selectOrderList(search);	/** 콘솔된건 있는지 체크 */
				if(list != null && list.size() > 0){
					search.put("errConsolData", "Y");
					return list;
				}
				search.put("functionConsoleChkYn", "N");
			}
			
			list = functionMapper.selectOrderList(search);
		}
		
		
		return list;
	}
		
	/**
	 * Function - 발송보류/취소
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public FunctionSaveResult sendHold(FunctionOrder functionOrder, HttpServletRequest request) throws Exception{
		Util.accessControlDatabase(functionOrder, "gmtTime|userLanguage|ecmCustomerLevel", false, null, null, null, null);
		FunctionSaveResult functionSaveResult = new FunctionSaveResult();
//		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		ProcessLogs processLogs = new ProcessLogs();
		
		Map<String, Object> resultData = new HashMap<String, Object>();
		List<Map<String, Object>> tarList;
		//콘솔된 경우 메시지 출력(rowData.HBLNo+ " 콘솔되어  등록할 수 없습니다.“)
		List<Map<String, Object>> errList1 = new ArrayList<Map<String, Object>>();
		List<List<Map<String, Object>>> spliceTarList = new ArrayList<List<Map<String, Object>>>();

		//발송모드
		String mode = Util.null2str(functionOrder.getFuncMode());
		String chk = Util.null2str(functionOrder.getChkMode());
		
		/** 발송보류 불가오더 체크 */
		List<Map<String, Object>> hblNoList = new ArrayList<Map<String, Object>>();
		Map<String,Object> srchInfo =  functionOrder.getSrchInfo();
		srchInfo.put("cryptographyYn", functionOrder.getCryptographyYn());
		String screen = Util.null2str(srchInfo.get("screen"));
		
		//선택일때
		if("A".equals(chk)) {
			//합산과세,일괄변경,발송보류/취소팝업에서 sendhold/update를 호출할 수 있는데 고객서비스에서 사용하는 팝업은 발송보류/취소팝업 뿐이므로
			//해당 경우에만 선택일 때도 선택한 odJobNo로 데이터 다시 조회해오도록 수정함
			//(다른 팝업에서도 적용해야 할 경우 팝업에서 선택건일 때 srchInfo 넘겨주는 부분 함께 추가해야함)
			if("ORDER_CUSTOMER".equals(screen)) {
				srchInfo.put("func","sendHold");
				if("A".equals(chk)) {
					List<String> srchOdJobNoList = new ArrayList<String>();
					for(Map<String, Object> od : functionOrder.getOdList()) {
						srchOdJobNoList.add(Util.null2str(od.get("odJobNo")));
					}
					srchInfo.put("srchOdJobNoList", srchOdJobNoList);
				}
				//ECM 메뉴에서 호출 시 다른 쿼리를 타기 때문에 lastPodStatus 조건을 주려면 다른 파라미터명으로 전달되어야 함
				//(lastPodStatus 조건 주는 파라미터 명이 달라서 전자상거래에서 호출 시 지금은 lastPodStatus 조건 타고 있지 않음)
				//발송보류(ECM040)
				if("S".equals(mode)) {
					srchInfo.put("srchStatusList", new ArrayList<String>(Arrays.asList("ECM010","ECM020","ECM030")));
				//발송보류취소
				}else{
					srchInfo.put("srchStatusList", new ArrayList<String>(Arrays.asList("ECM040")));
				}
				//검색조건으로 조회
				List<Map<String, Object>> list = selectOrderList(srchInfo);
				if(list == null || list.size() == 0) {
					functionSaveResult.setResultCode("notOrder");
					return functionSaveResult;
				}
				hblNoList.addAll(list);
			}else {
				hblNoList.addAll(functionOrder.getOdList());
			}
		}
		//검색일때 
		else {
			if(srchInfo == null) {
				functionSaveResult.setResultCode("notOrder");
				return functionSaveResult;
			}
			srchInfo.put("func","sendHold");
			//ECM 메뉴에서 호출 시 다른 쿼리를 타기 때문에 lastPodStatus 조건을 주려면 다른 파라미터명으로 전달되어야 함
			//(lastPodStatus 조건 주는 파라미터 명이 달라서 전자상거래에서 호출 시 지금은 lastPodStatus 조건 타고 있지 않음)
			//발송보류(ECM040)
			if("S".equals(mode)) {
				srchInfo.put("srchStatusList", new ArrayList<String>(Arrays.asList("ECM010","ECM020","ECM030")));
			//발송보류취소
			}else{
				srchInfo.put("srchStatusList", new ArrayList<String>(Arrays.asList("ECM040")));
			}
			//검색조건으로 조회
			List<Map<String, Object>> list = selectOrderList(srchInfo);
			if(list == null || list.size() == 0) {
				functionSaveResult.setResultCode("notOrder");
				return functionSaveResult;
			}
			hblNoList.addAll(list);
		}
				
		int houseCnt = hblNoList.size();
		//1000 건씩 나눠서 처리함.
		for(int i = 0; i < houseCnt; i+=1000){
			if(i + 1000 < houseCnt) {
				functionOrder.setOdList(hblNoList.subList(i, i + 1000));
			}else{
				functionOrder.setOdList(hblNoList.subList(i, houseCnt));
			}
			
			//hblNo 에 해당하는 od_jobNo 추출
			tarList = functionOrder.getOdList();
			spliceTarList.add(tarList);
			
			//화면단에서 조회된 건수와 lastPodStatus 조건으로 줘 다시 조회해온 건수가 다를 경우 화면단에서 오류 반환
			//ex.검색 건인데 반입/오더생성 데이터 섞여 있을 경우 반입 상태인 데이터 때문에 반입 처리 할 수 없고 오더생성 상태인 데이터 때문에 반입취소 할 수 없음
			if("ORDER_CUSTOMER".equals(screen)) {	
				if(functionOrder.getHouseCnt() != houseCnt) {
					functionSaveResult.setResultCode("carryinCntNotMatch");
					return functionSaveResult;
				}
			}
			//검색일때만 유효성 체크 선택일 경우는 화면단에서 유효성 체크했으므로 -> 발송에서 발송보류/취소한 경우 선택건도 데이터 다시 조회해오므로 유효성도 다시 체크해줌
			if(tarList.size() > 0 && (mode.equals("S") || mode.equals("C")) && ("B".equals(chk) || "ORDER_CUSTOMER".equals(screen))){
				for(Map<String, Object> errMap : tarList){
					if(Util.null2str(errMap.get("chk1")).equals("Y")){
						errList1.add(errMap);
					}				
				}
			}
		}
		
		if(errList1.size() > 0){
			functionSaveResult.setResultCode("sendHoldIncongruityOrder");
			resultData.put("errList1", errList1);
		}
				
		//에러가 있으면 return
		if(!Util.null2str(functionSaveResult.getResultCode()).equals("")){
			if(Util.null2str(functionSaveResult.getResultCode()).equals("sendHoldIncongruityOrder")){
				functionSaveResult.setResultData(resultData);
			}
			return functionSaveResult;
		}
		
		SqlSession sqlSession = null;
//		BulkData bulkData = null;
//		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
//		Map<String, Object> logMap = null;
//		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
//		tmpLogMap.put("ent_code", functionOrder.getEntCode());
//		tmpLogMap.put("ent_business_code", functionOrder.getUserBusinessCode());
//		tmpLogMap.put("solution", "ECM");
//		tmpLogMap.put("process_user_id", functionOrder.getUpdateUserId());
//		tmpLogMap.put("log_job_type", "DATA");
//		tmpLogMap.put("job_func_type", "UPDATE");
//		tmpLogMap.put("result", "ok");
//		tmpLogMap.put("process_date_time", System.currentTimeMillis());
//		
//		// 사업장명 가져오기
		if (!"".equals(Util.null2str(functionOrder.getUserBusinessCode()))) {
			String entBusinessPrintName = enterpriseMapper.getEntBusinessPrintName(functionOrder.getUserBusinessCode());
//			tmpLogMap.put("ent_business_print_name", entBusinessPrintName);
			processLogs.setEntBusinessPrintName(functionOrder.getLogUserBusinessPrintName());
		}
//		
//		// 사용자명 가져오기
		if (!"".equals(Util.null2str(functionOrder.getUpdateUserId()))) {
			User user = smsUserService.getUser(functionOrder.getUpdateUserId());
//			tmpLogMap.put("process_user_name", user.getUserName());
//			tmpLogMap.put("process_user_name_eng", user.getUserNameEng());
			processLogs.setProcessUserName(functionOrder.getLogUserName());
			processLogs.setProcessUserNameEng(functionOrder.getLogUserNameEng());
		}
//		
//		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			FunctionMapper functionBatchMapper = sqlSession.getMapper(FunctionMapper.class);
			
			Map<String, Object> podMap = new HashMap<String, Object>();
			podMap.put("entCode", functionOrder.getEntCode());
			podMap.put("entBusinessCode", "");
			podMap.put("gmtTime", functionOrder.getGmtTime());
			podMap.put("solution", "ECM");
			podMap.put("updateUserId", functionOrder.getUpdateUserId());
			podMap.put("stockType", "POD");
			podMap.put("stockSign", "POD");
			podMap.put("stockSeqLength", 8);
			podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			podMap.put("stockRemark", "ECM POD JOB NO");
			
			List<Map<String, Object>> insertPodList = new ArrayList<Map<String, Object>>();
			Map<String, Object> tmpInsertPodMap = null;
			
			//발송보류적용
			if("S".equals(mode)) {
				podMap.put("podStatus", "ECM040");
				podMap.put("POD_STATUS", "ECM040");
				podMap.put("REMARKS", "발송보류");
				//1. TB_ECM_OD_ADDITIONAL.LAST_POD_STATUS-> ECM040(발송보류)로 업데이트
	            //2. POD 테이블에 UPDATE/INSERT
				functionOrder.setInsStatus("ECM040");
				Map<String,Object> tmpMap = new HashMap<String,Object>();
				//1000*x 건씩 리스트 처리
				int seq = 1;
				for(int i = 0; i < spliceTarList.size(); i++){	
					
					List<Map<String, Object>> odList = spliceTarList.get(i);
					functionOrder.setOdNoList(odList);
					//last_pod_status ECM040 발송보류 로 업데이트
					functionMapper.updateSendHold(functionOrder);
					if(srchInfo != null && Util.null2str(srchInfo.get("userType")).equals("entUser")){
						/** 그리드 체크건 처리시 저장할 POD중복여부 조회 */
						srchInfo.put("odNoList", odList);
						if(!"A".equals(chk)) {
							if("ORDER".equals(srchInfo.get("srchQueryType"))){
								Util.getCryptographyYn(srchInfo);
							}
							Util.accessControlDatabase(srchInfo, "srchDateFormat", false, "srchDateGbn", "noDateChk|od.PO_DATE|od.AR_DATE|od.OB_DATE|ad.SIMPLE_DOC_DATE|ad.MF_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO"
									+ "|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO");
						}
						odList = functionMapper.selectOrderListEnt(srchInfo);
					}
					
					//한건씩 돌면서 한건씩 pod 테이블 저장
					for(int j = 0; j < odList.size(); j++) {
						Map<String, Object> odRow = odList.get(j);
						//존재하면 update
						if(!Util.null2str(odRow.get("podSendHold")).equals("")) {
							odRow.put("POD_STATUS", "ECM040");
							odRow.put("REMARKS", "발송보류");
							odRow.put("updateUserId", functionOrder.getUpdateUserId());
							odRow.put("gmtTime", functionOrder.getGmtTime());
							odRow.put("POD_JOB_NO", odRow.get("podSendHold"));
							//pod 테이블 UPDATE
							Util.accessControlDatabase(odRow, "gmtTime", false, null, "", null, "");
							functionBatchMapper.updatePodInfo(odRow);
						}
						else {
							tmpInsertPodMap = new HashMap<String, Object>();
							tmpInsertPodMap.put("odJobNo", odRow.get("odJobNo"));
							tmpInsertPodMap.put("hblNo", odRow.get("hblNo"));
							tmpInsertPodMap.put("POD_JOB_NO", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
							insertPodList.add(tmpInsertPodMap);
						}

//						bulkData = new BulkData();
//						bulkData.setActionType(BulkData.Type.CREATE);
//						bulkData.setIndexName(index);
//						bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
//						logMap = new HashMap<String, Object>();
//						logMap.putAll(tmpLogMap);
//						logMap.put("reference_no_id", bulkData.getId());
//						logMap.put("bound", Util.null2str(odRow.get("bound")));
//						logMap.put("bound_type", Util.null2str(odRow.get("boundType")));
//						logMap.put("job_no", Util.null2str(odRow.get("odJobNo")));
//						logMap.put("description", "HOUSE ORDER HOLD [HBL_NO : "+ Util.null2str(odRow.get("hblNo"))+"]");
//						logMap.put("job_log", new Gson().toJson(odRow));
//						bulkData.setJsonMap(logMap);
//						bulkList.add(bulkData);
					}
					for(Map<String, Object> insertPodMap : insertPodList){
						podMap.put("odJobNo", insertPodMap.get("odJobNo"));
						podMap.put("hblNo", insertPodMap.get("hblNo"));
						podMap.put("POD_JOB_NO", insertPodMap.get("POD_JOB_NO"));
						functionBatchMapper.insertPodInfo(podMap);
					}
					sqlSession.flushStatements();
					sqlSession.clearCache();
					sqlSession.commit();
					insertPodList = new ArrayList<Map<String, Object>>();
					
					functionOrder.setOdList(null);
					functionOrder.setOdNoList(null);
					processLogs.setJobFuncType("UPDATE");
					processLogs.setEntCode(functionOrder.getEntCode());
					processLogs.setEntBusinessCode(functionOrder.getUserBusinessCode());
					processLogs.setSolution("ECM");
					processLogs.setProcessUserId(functionOrder.getUpdateUserId());
					processLogs.setJobNo(functionOrder.getOdJobNo());
					processLogs.setLogJobType("DATA");
					processLogs.setDescription("HOUSE ORDER HOLD");
					if(!"A".equals(chk)) {
						processLogs.setJobLog(new Gson().toJson(functionOrder));
					}else{
						processLogs.setJobLog("");
					}
					processLogs.setResult("ok");
				}
			}	
			//발송보류취소
			else if("C".equals(Util.null2str(mode))) {
				//1. POD 히스토리에서 ECM030 삭제
				//3. POD 히스토리에서 MAX 값을 가져온 후 TB_ECM_OD_ADDITIONAL.LAST_POD_STATUS 업데이트
				
				Map<String,Object> tmpMap = new HashMap<String,Object>();
				//1000*x 건씩 리스트 처리
				int seq = 1;
				for(int i = 0; i < spliceTarList.size(); i++){	
					
					List<Map<String, Object>> odList = spliceTarList.get(i);
					functionOrder.setOdNoList(odList);
					functionOrder.setDelStatus("ECM040");
					functionMapper.deletePodInfo(functionOrder);

					if(srchInfo != null && Util.null2str(srchInfo.get("userType")).equals("entUser")){
						/** 그리드 체크건 처리시 저장할 POD중복여부 조회 */
						srchInfo.put("odNoList", odList);
						if(!"A".equals(chk)) {
							if("ORDER".equals(srchInfo.get("srchQueryType"))){
								Util.getCryptographyYn(srchInfo);
							}
							Util.accessControlDatabase(srchInfo, "srchDateFormat", false, "srchDateGbn", "noDateChk|od.PO_DATE|od.AR_DATE|od.OB_DATE|ad.SIMPLE_DOC_DATE|ad.MF_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO"
									+ "|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO");
						}
						odList = functionMapper.selectOrderListEnt(srchInfo);
					}
					
					//한건씩 돌면서 한건씩 pod 테이블 저장
					for(int j = 0; j < odList.size(); j++) {
						Map<String, Object> odRow = odList.get(j);
						functionOrder.setOdJobNo(Util.null2str(odRow.get("odJobNo")));
						functionBatchMapper.updateLastPodStatus2(functionOrder);

//						bulkData = new BulkData();
//						bulkData.setActionType(BulkData.Type.CREATE);
//						bulkData.setIndexName(index);
//						bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
//						logMap = new HashMap<String, Object>();
//						logMap.putAll(tmpLogMap);
//						logMap.put("reference_no_id", bulkData.getId());
//						logMap.put("bound", Util.null2str(odRow.get("bound")));
//						logMap.put("bound_type", Util.null2str(odRow.get("boundType")));
//						logMap.put("job_no", Util.null2str(odRow.get("odJobNo")));
//						logMap.put("description", "HOUSE ORDER HOLD CANCEL [HBL_NO : "+ Util.null2str(odRow.get("hblNo"))+"]");
//						logMap.put("job_log", new Gson().toJson(odRow));
//						bulkData.setJsonMap(logMap);
//						bulkList.add(bulkData);
						
					}
					sqlSession.flushStatements();
					sqlSession.clearCache();
					sqlSession.commit();
					
					functionOrder.setOdList(null);
					functionOrder.setOdNoList(null);
					processLogs.setJobFuncType("UPDATE");
					processLogs.setEntCode(functionOrder.getEntCode());
					processLogs.setEntBusinessCode(functionOrder.getUserBusinessCode());
					processLogs.setSolution("ECM");
					processLogs.setProcessUserId(functionOrder.getUpdateUserId());
					processLogs.setJobNo(functionOrder.getOdJobNo());
					processLogs.setLogJobType("DATA");
					processLogs.setDescription("HOUSE ORDER HOLD CANCEL");
					if(!"A".equals(chk)) {
						processLogs.setJobLog(new Gson().toJson(functionOrder));
					}else{
						processLogs.setJobLog("");
					}
					processLogs.setResult("ok");
				}
			}
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}

		commonService.insertLog(processLogs, request);
//		commonService.insertLogBulk(bulkList);
		return functionSaveResult;
	}
	
	/**
	 * Function - 발송
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public FunctionSaveResult send(FunctionOrder functionOrder, HttpServletRequest request) throws Exception{
		FunctionSaveResult functionSaveResult = new FunctionSaveResult();
		Util.accessControlDatabase(functionOrder, "gmtTime|userLanguage|ecmCustomerLevel", false, null, null, null, null);
		List<Map<String, Object>> tarList;
		List<List<Map<String, Object>>> spliceTarList = new ArrayList<List<Map<String, Object>>>();

		String chk = Util.null2str(functionOrder.getChkMode());
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		/** 발송보류 불가오더 체크 */
		List<Map<String, Object>> hblNoList = new ArrayList<Map<String, Object>>();
		
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		
		boolean scanFlag = false;
		
		//선택일때
		if("A".equals(chk)) {
			tmpMap.put("func", "send");
			tmpMap.put("userLanguage", functionOrder.getUserLanguage());
			tmpMap.put("chkMode", "A");
			if((functionOrder.getOdList() == null || functionOrder.getOdList().size() == 0) && !Util.null2str(functionOrder.getHblNo()).equals("")){
				/** 스캔화면에서 호출 */
				scanFlag = true;
				tmpMap.put("hblNo", functionOrder.getHblNo());
				/** 스캔은 비엘번호 직접입력하므로 권한체크함 */
				tmpMap.put("ecmCustomerCode", functionOrder.getEcmCustomerCode());
				tmpMap.put("ecmCustomerLevel", functionOrder.getEcmCustomerLevel());
				//발송스캔(ECM010)
				tmpMap.put("srchLastPodStatusList", new ArrayList<String>(Arrays.asList("ECM010")));
				tmpMap.put("cryptographyYn", functionOrder.getCryptographyYn());
				hblNoList.addAll(functionMapper.selectOrderListEnt(tmpMap));
				if(hblNoList.size() == 0){
					functionSaveResult.setResultCode("notOrder");
					return functionSaveResult;
				}
			} else {
				if(functionOrder.getOdList() != null && functionOrder.getOdList().size() > 0) {
					tmpMap.put("odNoList", functionOrder.getOdList());
					hblNoList.addAll(functionMapper.selectOrderListEnt(tmpMap));
				}else {
					functionSaveResult.setResultCode("notOrder");
					return functionSaveResult;
				}

			}
		}
		//검색일때 
		else {
			Map<String,Object> srchInfo = functionOrder.getSrchInfo();
			if(srchInfo == null) {
				functionSaveResult.setResultCode("notOrder");
				return functionSaveResult;
			}
			srchInfo.put("cryptographyYn", functionOrder.getCryptographyYn());
			srchInfo.put("func","send");
			
			//검색조건으로 조회
			List<Map<String, Object>> list = selectOrderList(srchInfo);
			if(list == null || list.size() == 0) {
				functionSaveResult.setResultCode("notOrder");
				return functionSaveResult;
			}
			hblNoList.addAll(list);
		}
				
		int houseCnt = hblNoList.size();
		//1000 건씩 나눠서 처리함.
		for(int i = 0; i < houseCnt; i+=1000){
			if(i + 1000 < houseCnt) {
				functionOrder.setOdList(hblNoList.subList(i, i + 1000));
			}else{
				functionOrder.setOdList(hblNoList.subList(i, houseCnt));
			}
			
			//hblNo 에 해당하는 od_jobNo 추출
			tarList = functionOrder.getOdList();
			spliceTarList.add(tarList);
		}		
		
		SqlSession sqlSession = null;
//		BulkData bulkData = null;
//		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", functionOrder.getEntCode());
		tmpLogMap.put("ent_business_code", functionOrder.getUserBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("process_user_id", functionOrder.getUpdateUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());

		// 사업장명 가져오기
		if (!"".equals(Util.null2str(functionOrder.getUserBusinessCode()))) {
			String entBusinessPrintName = enterpriseMapper.getEntBusinessPrintName(functionOrder.getUserBusinessCode());
			tmpLogMap.put("ent_business_print_name", entBusinessPrintName);
		}
		
		// 사용자명 가져오기
		if (!"".equals(Util.null2str(functionOrder.getUpdateUserId()))) {
			User user = smsUserService.getUser(functionOrder.getUpdateUserId());
			tmpLogMap.put("process_user_name", user.getUserName());
			tmpLogMap.put("process_user_name_eng", user.getUserNameEng());
		}
		
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			FunctionMapper functionBatchMapper = sqlSession.getMapper(FunctionMapper.class);
		
			Map<String, Object> podMap = new HashMap<String, Object>();
			podMap.put("entCode", functionOrder.getEntCode());
			podMap.put("entBusinessCode", "");
			podMap.put("gmtTime", functionOrder.getGmtTime());
			podMap.put("solution", "ECM");
			podMap.put("updateUserId", functionOrder.getUpdateUserId());
			podMap.put("stockType", "POD");
			podMap.put("stockSign", "POD");
			podMap.put("stockSeqLength", 8);
			podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			podMap.put("stockRemark", "ECM POD JOB NO");
			podMap.put("podStatus", "ECM010");
			
			List<Map<String, Object>> insertPodList = new ArrayList<Map<String, Object>>();
			Map<String, Object> tmpInsertPodMap = null;
			
			//1. TB_ECM_OD_ADDITIONAL.LAST_POD_STATUS-> ECM010(오더접수)로 업데이트
	        //2. POD 테이블에 UPDATE/INSERT
			functionOrder.setInsStatus("ECM010");
			//1000*x 건씩 리스트 처리
			int seq = 1;
			for(int i = 0; i < spliceTarList.size(); i++){	
				
				List<Map<String, Object>> odList = spliceTarList.get(i);
				functionOrder.setOdNoList(odList);
				//last_pod_status ECM010  오더접수 로 업데이트
				functionMapper.updateCarryin(functionOrder);
				
				//한건씩 돌면서 한건씩 pod 테이블 저장
				for(int j = 0; j < odList.size(); j++) {
					Map<String, Object> odRow = odList.get(j);
					//존재하면 update
					if(!Util.null2str(odRow.get("podRpt")).equals("")) {	
						odRow.put("POD_JOB_NO", odRow.get("podRpt"));
						odRow.put("POD_STATUS", "ECM010");
						odRow.put("updateUserId", functionOrder.getUpdateUserId());
						odRow.put("gmtTime", functionOrder.getGmtTime());
						//pod 테이블 UPDATE
						functionBatchMapper.updatePodInfo(odRow);
					}
					else {	
						tmpInsertPodMap = new HashMap<String, Object>();
						tmpInsertPodMap.put("odJobNo", odRow.get("odJobNo"));
						tmpInsertPodMap.put("hblNo", odRow.get("hblNo"));
						tmpInsertPodMap.put("POD_JOB_NO", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
						insertPodList.add(tmpInsertPodMap);
					}
					
//					bulkData = new BulkData();
//					bulkData.setActionType(BulkData.Type.CREATE);
//					bulkData.setIndexName(index);
//					bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
//					logMap = new HashMap<String, Object>();
//					logMap.putAll(tmpLogMap);
//					logMap.put("reference_no_id", bulkData.getId());
//					logMap.put("job_no", Util.null2str(odRow.get("odJobNo")));
//					logMap.put("bound", Util.null2str(odRow.get("bound")));
//					logMap.put("boundType", Util.null2str(odRow.get("boundType")));
//					logMap.put("description", "등록 > 발송 [HBL_NO : "+ Util.null2str(odRow.get("hblNo"))+"]");
//					logMap.put("job_log", new Gson().toJson(functionOrder));
//					bulkData.setJsonMap(logMap);
//					bulkList.add(bulkData);
				}
				for(Map<String, Object> insertPodMap : insertPodList){
					podMap.put("odJobNo", insertPodMap.get("odJobNo"));
					podMap.put("hblNo", insertPodMap.get("hblNo"));
					podMap.put("POD_JOB_NO", insertPodMap.get("POD_JOB_NO"));
					podMap.put("POD_STATUS", "ECM010");
					functionBatchMapper.insertPodInfo(podMap);
				}
				insertPodList = new ArrayList<Map<String, Object>>();
				sqlSession.flushStatements();
				sqlSession.clearCache();
				sqlSession.commit();
				
//				commonService.insertLogBulk(bulkList);
				bulkList = new ArrayList<BulkData>(); 
			}
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
		
		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
		functionOrder.setOdList(null);
		processLogs.setJobFuncType("UPDATE");
		processLogs.setEntCode(functionOrder.getEntCode());
		processLogs.setEntBusinessCode(functionOrder.getUserBusinessCode());
		processLogs.setSolution("ECM");
//		processLogs.setBound(functionOrder.getBound());
//		processLogs.setBoundType(functionOrder.getBoundType());
		processLogs.setProcessUserId(functionOrder.getUpdateUserId());
		processLogs.setJobNo(functionOrder.getOdJobNo());
		processLogs.setLogJobType("DATA");
		processLogs.setDescription("등록 > 발송");
		if(!"A".equals(chk))	processLogs.setJobLog(new Gson().toJson(functionOrder));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(functionOrder.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(functionOrder.getLogUserName());
		processLogs.setProcessUserNameEng(functionOrder.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
				
		return functionSaveResult;
	}	
	
	/**
	 * Function - 입항일자 적용
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public FunctionSaveResult obDate(FunctionOrder functionOrder, HttpServletRequest request) throws Exception{
		FunctionSaveResult functionSaveResult = new FunctionSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		Map<String, Object> tmpMap = null;
		
		List<Map<String, Object>> tarList;
		List<List<Map<String, Object>>> spliceTarList = new ArrayList<List<Map<String, Object>>>();
		String chk = Util.null2str(functionOrder.getChkMode());
		
		List<Map<String, Object>> hblNoList = new ArrayList<Map<String, Object>>();
		//선택일때
		if("A".equals(chk)) {
			tmpMap = new HashMap<String, Object>();
			tmpMap.put("odJobNoList", functionOrder.getOdList());
			tmpMap.put("functionConsoleChkYn", "Y");
			List<Map<String, Object>> list = functionMapper.selectOrderFunctionChk(tmpMap);
			if(list != null && list.size() > 0){
				functionSaveResult.setResultCode("errConsolData");
				tmpMap.put("errConsolData", list);
				functionSaveResult.setResultData(tmpMap);
				return functionSaveResult;
			}
			hblNoList.addAll(functionOrder.getOdList());
		}
		//검색일때 
		else {			
			Map<String,Object> srchInfo = functionOrder.getSrchInfo();
			if(srchInfo == null) {
				functionSaveResult.setResultCode("notOrder");
				return functionSaveResult;
			}
			srchInfo.put("cryptographyYn", functionOrder.getCryptographyYn());
			srchInfo.put("func","obDate");
			
			//검색조건으로 조회
			srchInfo.put("statusDefault", "");
			List<Map<String, Object>> list = selectOrderList(srchInfo);
			if(Util.null2str(srchInfo.get("errConsolData")).equals("Y")){
				functionSaveResult.setResultCode("errConsolData");
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("errConsolData", list);
				functionSaveResult.setResultData(tmpMap);
				return functionSaveResult;
			}
			if(list == null || list.size() == 0) {
				functionSaveResult.setResultCode("notOrder");
				return functionSaveResult;
			}
			hblNoList.addAll(list);
		}
		
		int houseCnt = hblNoList.size();
		//1000 건씩 나눠서 처리함.
		for(int i = 0; i < houseCnt; i+=1000){
			if(i + 1000 < houseCnt) {
				functionOrder.setOdList(hblNoList.subList(i, i + 1000));
			}else{
				functionOrder.setOdList(hblNoList.subList(i, houseCnt));
			}
			
			tarList = functionOrder.getOdList();
			spliceTarList.add(tarList);
		}
		
		//1. TB_ECM_OD 선적예정일자 업데이트
		//1000*x 건씩 리스트 처리
		for(int i = 0; i < spliceTarList.size(); i++){	
			
			List<Map<String, Object>> odList = spliceTarList.get(i);
			functionOrder.setOdNoList(odList);
			functionMapper.updateObDate(functionOrder);
			
		}
		
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", functionOrder.getEntCode());
		tmpLogMap.put("ent_business_code", functionOrder.getUserBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("process_user_id", functionOrder.getUpdateUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		
		// 사업장명 가져오기
		if (!"".equals(Util.null2str(functionOrder.getUserBusinessCode()))) {
			String entBusinessPrintName = enterpriseMapper.getEntBusinessPrintName(functionOrder.getUserBusinessCode());
			tmpLogMap.put("ent_business_print_name", entBusinessPrintName);
		}
		
		// 사용자명 가져오기
		if (!"".equals(Util.null2str(functionOrder.getUpdateUserId()))) {
			User user = smsUserService.getUser(functionOrder.getUpdateUserId());
			tmpLogMap.put("process_user_name", user.getUserName());
			tmpLogMap.put("process_user_name_eng", user.getUserNameEng());
		}
		
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		int seq = 1;
		for(Map<String,Object> od : hblNoList) {
			// 처리 로그 기록
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpLogMap);
			logMap.put("reference_no_id", bulkData.getId());
			logMap.put("job_no", Util.null2str(od.get("odJobNo")));
			logMap.put("bound", Util.null2str(od.get("bound")));
			logMap.put("bound_type", Util.null2str(od.get("boundType")));
			logMap.put("description", "Order > 입항일자 [HBL_NO : "+ Util.null2str(od.get("hblNo")) +"]");
			if(!"A".equals(chk)) {
				functionOrder.setOdList(null);
				functionOrder.setOdNoList(null);
				logMap.put("job_log", new Gson().toJson(functionOrder));
			}else {
				logMap.put("job_log", "");
			}
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
		}
		
		commonService.insertLogBulk(bulkList);
		return functionSaveResult;
	}
	
	/**
	 * Function - 개인통관부호검증
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public FunctionSaveResult customsIdVali(FunctionOrder functionOrder, HttpServletRequest request) throws Exception{
				
		FunctionSaveResult functionSaveResult = new FunctionSaveResult();
		
		String chk = Util.null2str(functionOrder.getChkMode());
		String srchYn = Util.null2str(functionOrder.getSrchYn());
		
		//검증할 개인통관부호 리스트 추출		
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		//선택일때
		if("A".equals(chk) && false == "Y".equals(srchYn)) {
			dataList.addAll(functionOrder.getOdList());
		}
		//검색일때 
		else {			
			Map<String,Object> srchInfo = functionOrder.getSrchInfo();
			if(srchInfo == null) {
				functionSaveResult.setResultCode("notOrder");
				return functionSaveResult;
			}
			srchInfo.put("cryptographyYn", functionOrder.getCryptographyYn());
			srchInfo.put("func","customsIdVali");
			//선택일때 master od_job_no 를 넘길때는 서버단에 매칭되는 hbl 을 다시 추출해야 함.
			if("A".equals(chk) && "Y".equals(srchYn)) {
				srchInfo.put("masterList", functionOrder.getOdList());
				srchInfo.put("checkMfSubGridYn", functionOrder.getCheckMfSubGridYn());
			}
			/* N개 검색(멀티타입) */
			List<String> searchNoList = new ArrayList<String>();
			if(!"".equals(Util.null2str(srchInfo.get("srchNo")))) {
				searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(srchInfo.get("srchNo"))).split(",")));
			}
			
			if(searchNoList.size() > 1){
				srchInfo.put("searchType","multi");
				srchInfo.put("searchNoList",searchNoList);
			}else{
				srchInfo.put("srchNo",Util.null2str(srchInfo.get("srchNo")).trim().replace(",", ""));
			}
			//검색조건으로 조회
			List<Map<String, Object>> list = selectOrderList(srchInfo);
			if(list == null || list.size() == 0) {
				functionSaveResult.setResultCode("notOrder");
				return functionSaveResult;
			}
			dataList.addAll(list);
		}
		
		Map<String, Object> returnMap = new HashMap<String, Object>();

		SqlSession sqlSession = null;
		try {
			
			int totCnt = dataList.size();	
			
			Map<String, Object> dataMap;
			List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();
			String customsId, consigneeHp, consigneeTel;
			
			for(int i = dataList.size() - 1; i >= 0; i--){
				dataMap = dataList.get(i);
				if(!Util.null2str(dataMap.get("customsIdType")).equals("A")){
					dataMap.put("consigneeIdCardNo_IF_YN", "O");
					dataMap.put("consigneeIdCardNoFailRemark", "A.개인통관코드만 검증 가능합니다.");
					errList.add(dataList.remove(i));
				}else{
					customsId = Util.null2str(dataMap.get("customsId"));
					consigneeHp = Util.null2str(dataMap.get("consigneeHp"));
					consigneeTel = Util.null2str(dataMap.get("consigneeTel"));
					if(customsId.equals("")){
						dataMap.put("consigneeIdCardNo_IF_YN", "O");
						dataMap.put("consigneeIdCardNoFailRemark", "개인통관부호 입력 값이 없습니다.");
						errList.add(dataList.remove(i));
					}else if(consigneeHp.equals("")){
						
						//수하인휴대전화 값이 없고 수하인전화번호 값이 있을 경우 전화번호로 검증, 둘 다 없을 경우에만 오류
						if(consigneeTel.equals("")) {
							dataMap.put("consigneeIdCardNo_IF_YN", "O");
							dataMap.put("consigneeIdCardNoFailRemark", "수하인휴대전화 입력 값이 없습니다.");
							errList.add(dataList.remove(i));
						//수하인휴대전화 값이 없고 수하인전화번호 값이 있을 경우 전화번호 길이 체크
						}else if(consigneeTel.length() < 4) {
							dataMap.put("consigneeIdCardNo_IF_YN", "O");
							dataMap.put("consigneeIdCardNoFailRemark", "전화번호가 정확하지 않습니다.");
							errList.add(dataList.remove(i));
						}
						
					}else if((functionOrder.getNameType().equals("ko") && Util.null2str(dataMap.get("consigneeNameLoc")).replace(" ", "").equals("")) || (functionOrder.getNameType().equals("en") && Util.null2str(dataMap.get("consigneeName")).replace(" ", "").equals(""))){
						dataMap.put("consigneeIdCardNo_IF_YN", "O");
						dataMap.put("consigneeIdCardNoFailRemark", "수하인명 입력 값이 없습니다.");
						errList.add(dataList.remove(i));
					}else if(customsId.length() != 13 || !customsId.matches("^(O|o|P|p)[0-9]{12}$")){
						dataMap.put("consigneeIdCardNo_IF_YN", "O");
						dataMap.put("consigneeIdCardNoFailRemark", "개인통관코드형식이 아닙니다.");
						errList.add(dataList.remove(i));
					}else if(consigneeHp.length() < 4){
						dataMap.put("consigneeIdCardNo_IF_YN", "O");
						dataMap.put("consigneeIdCardNoFailRemark", "전화번호가 정확하지 않습니다.");
						errList.add(dataList.remove(i));
					}
				} 
			}
			if(errList.size() > 0){
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				CommonMapper commonBatchMapper = sqlSession.getMapper(CommonMapper.class);
				int cnt = 1;
				for(Map<String, Object> errMap : errList){
					commonBatchMapper.updateCustoms(errMap);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
				sqlSession.flushStatements();
				sqlSession.clearCache();
				sqlSession.commit();
			}
			
			
    		
			returnMap.put("totalCnt", totCnt);
			returnMap.put("succCnt", dataList.size());
			returnMap.put("nonCnt", errList.size());
		}catch(Exception e) {
			throw e;
		}finally{
			if(sqlSession != null) sqlSession.close();
		}
		
		if(dataList.size() > 0){
			String index = "ecm-customs-verification-result-" + DateTime.getDate("yyyy.MM.dd");
			IndexApi indexApi = new IndexApi(restClient);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
		
			List<JSONObject> redisList = new ArrayList<JSONObject>();
			
			String id = DateTime.getDate("yyyyMMddhhmmssSSS") + "-" + functionOrder.getEntCode();
			DocumentApi documentApi = new DocumentApi(restClient);
			Map<String, Object> esMap = new HashMap<String, Object>();
			esMap.put("ent_code", functionOrder.getEntCode());
			esMap.put("id", id);
			esMap.put("order", "");
			esMap.put("status", "wait");
			esMap.put("total_count", dataList.size());
			esMap.put("fail_count", 0);
			esMap.put("register_date_time", DateUtils.nowWithMillisResolution());
			esMap.put("register_user_id", functionOrder.getUpdateUserId());
			esMap.put("register_user_name", functionOrder.getUserName());
			
			for (Map<String, Object> dataMap : dataList) {
				JSONObject json = new JSONObject();
				json.put("resultIndex", index);
				json.put("resultId", id);
				json.put("entCode", functionOrder.getEntCode());
				json.put("entBusinessCode", functionOrder.getEntBusinessCode());
				json.put("userId", functionOrder.getUpdateUserId());
				json.put("userName", functionOrder.getUserName());
				json.put("odJobNo", dataMap.get("odJobNo"));
				json.put("masterOdJobNo", dataMap.get("masterOdJobNo"));
				json.put("mablNo", dataMap.get("mblNo"));
				json.put("hblNo", dataMap.get("hblNo"));
				json.put("nameType", functionOrder.getNameType());
				redisList.add(json);
			}
			
			// 결과 조회를 위해 ES에 저장
			documentApi.createDocument(index, id, esMap);
			
			// Batch 작업을 위해 Redis에 저장
	    	String key = "E-COMMERCE_CUSTOMS_CHECK_" + functionOrder.getEntCode();
	        ListOperations<String, String> listOperations = redisTemplate.opsForList();
	        
	        listOperations.rightPush(key, redisList.toString());
		}
		//결과내역 셋팅
		functionSaveResult.setResultData(returnMap);
		
		return functionSaveResult;
	}
	
	/**
	 * Function - 과태료검증
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public FunctionSaveResult penaltyVali(FunctionOrder functionOrder, HttpServletRequest request) throws Exception{
		FunctionSaveResult functionSaveResult = new FunctionSaveResult();
		
		Map<String, Object> resultData = new HashMap<String, Object>();
		List<Map<String, Object>> penaltyList = new ArrayList<Map<String, Object>>();

		//과태료검증타입
		String penaltyType = Util.null2str(functionOrder.getPenaltyType());
		
		List<Map<String, Object>> hblNoList = new ArrayList<Map<String, Object>>();
		
		Map<String,Object> srchInfo = functionOrder.getSrchInfo();
		if(srchInfo == null) {
			functionSaveResult.setResultCode("notOrder");
			return functionSaveResult;
		}
		srchInfo.put("cryptographyYn", functionOrder.getCryptographyYn());
		srchInfo.put("func","penaltyVali");
		
		//검색조건으로 조회
		List<Map<String, Object>> list = selectOrderList(srchInfo);
		if(list == null || list.size() == 0) {
			functionSaveResult.setResultCode("notOrder");
			return functionSaveResult;
		}
		hblNoList.addAll(list);
		
		SqlSession sqlSession = null;
		sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
		FunctionMapper functionMapperBatch = sqlSession.getMapper(FunctionMapper.class);
		
		int houseCnt = hblNoList.size();
		int penaltyCnt = 0;
		StringBuffer penaltyCode = new StringBuffer();
		StringBuffer penaltyName = new StringBuffer();
		
		
		if(penaltyType.equals("sendEdi")){	/** EDI전송시 검증 */
			String nameChkType = "";
			String addressChkType = "";
			String addPlusChkType = "";
			srchInfo.clear();
			srchInfo.put("entCode", functionOrder.getEntCode());
			srchInfo.put("entBusinessCode", functionOrder.getEntBusinessCode());
			srchInfo.put("solution", "ECM");
			srchInfo.put("optionCode", "C04");
			nameChkType = Util.null2str(commonMapper.selectSystemOption(srchInfo));
			srchInfo.put("optionCode", "C05");
			addressChkType = Util.null2str(commonMapper.selectSystemOption(srchInfo));
			srchInfo.put("optionCode", "C16");
			addPlusChkType = Util.null2str(commonMapper.selectSystemOption(srchInfo));
			
			for(int i = 0; i < houseCnt; i++){
				Map<String, Object> row = hblNoList.get(i);
				row.put("entCode", functionOrder.getEntCode());
				String tarStr = "";
				penaltyCode.setLength(0);
				penaltyName.setLength(0);
				if(nameChkType.equals("ENG")){
					tarStr = Util.null2str(row.get("consigneeName"));
					if(!tarStr.equals("")){
						if(!checkConsigneeName(tarStr)){
							penaltyName.append(", 영문상호");
							penaltyCode.append("A");
						}
					}else{
						tarStr = Util.null2str(row.get("consigneeNameLoc"));
						if(!tarStr.equals("")){
//							if(!checkConsigneeHanName(tarStr)){
//								penaltyName.append(", 한글상호");
//								penaltyCode.append("A");
//							}
						}else{
							penaltyName.append(", 영문상호");
							penaltyCode.append("A");
						}
					}
				}else{
					tarStr = Util.null2str(row.get("consigneeNameLoc"));
					if(!tarStr.equals("")){
//						if(!checkConsigneeHanName(tarStr)){
//							penaltyName.append(", 한글상호");
//							penaltyCode.append("A");
//						}
					}else{
						tarStr = Util.null2str(row.get("consigneeName"));
						if(!tarStr.equals("")){
							if(!checkConsigneeName(tarStr)){
								penaltyName.append(", 영문상호");
								penaltyCode.append("A");
							}
						}else{
							penaltyName.append(", 한글상호");
							penaltyCode.append("A");
						}
					}
				}
				if(addressChkType.equals("ENG")){
					tarStr = Util.null2str(row.get("consigneeAddress"));
					if(!tarStr.equals("")){
						if(!checkConsigneeAddress(tarStr)){
							penaltyName.append(", 영문주소");
							if(penaltyCode.length() == 0){	/** PENALTY_YN이 2byte이기 때문에 상호,주소,품명에 모두 걸린상태에서 업데이트시 오류발생함 */
								penaltyCode.append("B");
							}
						}
					}else{
						tarStr = Util.null2str(row.get("consigneeAddressLoc"));
						if(!tarStr.equals("")){
							if(!checkConsigneeHanAddress(tarStr,"X")){
								penaltyName.append(", 한글주소");
								if(penaltyCode.length() == 0){
									penaltyCode.append("C");
								}
							}
						}else{
							penaltyName.append(", 영문주소");
							if(penaltyCode.length() == 0){
								penaltyCode.append("B");
							}
						}
					}
				}else {
					tarStr = Util.null2str(row.get("consigneeAddressLoc"));
					if(!tarStr.equals("")){
						if(!checkConsigneeHanAddress(tarStr,addPlusChkType)){
							penaltyName.append(", 한글주소");
							if(penaltyCode.length() == 0){
								penaltyCode.append("C");
							}
						}
					}else{
						tarStr = Util.null2str(row.get("consigneeAddress"));
						if(!tarStr.equals("")){
							if(!checkConsigneeAddress(tarStr)){
								penaltyName.append(", 영문주소");
								if(penaltyCode.length() == 0){
									penaltyCode.append("B");
								}
							}
						}else{
							penaltyName.append(", 한글주소");
							if(penaltyCode.length() == 0){
								penaltyCode.append("C");
							}
						}
					}
				}
				
				tarStr = Util.null2str(row.get("commodityItem"));
				if(!checkCommodityItem(tarStr)){
					penaltyName.append(", 품명");
					penaltyCode.append("D");
				}
				
				
				if(penaltyCode.length() == 0){
					row.put("penaltyYn", "N");
				}else{
					row.put("penaltyYn", Util.null2str(penaltyCode));
					penaltyCnt++;
				}
				
				functionMapperBatch.updatePenalty(row);
				if((i + 1 % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
				
				if(penaltyCode.length() != 0){
					/** 적하목록/통관목록 전송시 오류내역 */
					row.put("blNo", row.get("hblNo"));
					row.put("errorStr", penaltyName.substring(2) + "이(가) 세관기준에 맞지 않아 과태료 대상입니다.");
					row.put("messageType", "과태료");
					penaltyList.add(row);
				}
			}
			
			resultData.put("penaltyList", penaltyList);
		}else{
			for(int i = 0; i < houseCnt; i++){	
				
				Map<String, Object> row = hblNoList.get(i);
				row.put("entCode", functionOrder.getEntCode());
				String tarStr = "";
				penaltyCode.setLength(0);
				
				// 과태료 코드: A:상호,B:영문주소,C:한글주소,D:품명
				//수하인-영문상호 검증
				if("A".equals(penaltyType)) {
					tarStr = (String)row.get("consigneeName");
					if(!checkConsigneeName(tarStr)) penaltyCode.append("A");
				}			
				//수하인-영문주소 
				else if("B".equals(penaltyType)) {
					tarStr = (String)row.get("consigneeAddress");
					if(!checkConsigneeAddress(tarStr))penaltyCode.append("B");
				}			
				//수하인-한글주소 
				else if("C".equals(penaltyType)) {
					tarStr = (String)row.get("consigneeAddressLoc");
					if(!checkConsigneeHanAddress(tarStr,"X"))penaltyCode.append("C");
				}
				
				tarStr = (String)row.get("commodityItem");
				if(!checkCommodityItem(tarStr))penaltyCode.append("D");
				
				if(penaltyCode.length() == 0) row.put("penaltyYn", "N");
				else {
					row.put("penaltyYn", Util.null2str(penaltyCode));
					penaltyCnt++;
				}
				
				functionMapperBatch.updatePenalty(row);
				if((i + 1 % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
		}
		
			
		
		sqlSession.flushStatements();
		sqlSession.clearCache();
		sqlSession.commit();
		
		resultData.put("totalCnt", houseCnt);
		resultData.put("penaltyCnt", penaltyCnt);
		
		functionSaveResult.setResultData(resultData);
				
		return functionSaveResult;
	}	
	
	
	/**
	 * 수하인-상호 검증
	 * @param strData
	 * @return
	 */
	public boolean checkConsigneeName(String strData) throws Exception
    {
		boolean bReturnValue = true;
		strData = Util.null2str(strData);
		
        String strCheckData = strData.trim();
        List<String> arrReturnData = new ArrayList<String>();

        arrReturnData.add("ABC");
        arrReturnData.add("A Ri");
        arrReturnData.add("A 5218");
        arrReturnData.add("ABBA");
        arrReturnData.add("AHN");
        arrReturnData.add("AN");
        arrReturnData.add("APATEU");
        arrReturnData.add("BAEK");
        arrReturnData.add("BAG");
        arrReturnData.add("BH");
        arrReturnData.add("BOB");
        arrReturnData.add("BOO");
        arrReturnData.add("BP");
        arrReturnData.add("C C");
        arrReturnData.add("CHAN");
        arrReturnData.add("CHOI");
        arrReturnData.add("CHUU");
        arrReturnData.add("COM");
        arrReturnData.add("D C");
        arrReturnData.add("DAN");
        arrReturnData.add("HAKGYO");
        arrReturnData.add("H ome");
        arrReturnData.add("h ome");
        arrReturnData.add("HOME");
        arrReturnData.add("HAN");
        arrReturnData.add("HBL");
        arrReturnData.add("HOESA");
        arrReturnData.add("HONG");
        arrReturnData.add("HUANG");
        arrReturnData.add("HWANG");
        arrReturnData.add("I");
        arrReturnData.add("JATAEK");
        arrReturnData.add("JA TAEK");
        arrReturnData.add("JEON");
        arrReturnData.add("JEONG");
        arrReturnData.add("JHA");
        arrReturnData.add("JIKJANG");
        arrReturnData.add("JIM");
        arrReturnData.add("JIN");
        arrReturnData.add("JIP");
        arrReturnData.add("JOOO");
        arrReturnData.add("JP");
        arrReturnData.add("JU");
        arrReturnData.add("JUSO");
        arrReturnData.add("JUTAEK");
        arrReturnData.add("k im");
        arrReturnData.add("KAN");
        arrReturnData.add("KHAN");
        arrReturnData.add("KIM");
        arrReturnData.add("KM");
        arrReturnData.add("KOO");
        arrReturnData.add("KWANG");
        arrReturnData.add("KWON");
        arrReturnData.add("LEE");
        arrReturnData.add("LI");
        arrReturnData.add("LIU");
        arrReturnData.add("MI");
        arrReturnData.add("MIN");
        arrReturnData.add("MOON");
        arrReturnData.add("MR");
        arrReturnData.add("MR  IM");
        arrReturnData.add("MR IM");
        arrReturnData.add("MR  PU");
        arrReturnData.add("MR PU");
        arrReturnData.add("MR BAE");
        arrReturnData.add("MR BANG");
        arrReturnData.add("MR CEE");
        arrReturnData.add("MR CHOI");
        arrReturnData.add("MR CHUI");
        arrReturnData.add("MR CUI");
        arrReturnData.add("MR EM");
        arrReturnData.add("MR ERIC");
        arrReturnData.add("MR FWON");
        arrReturnData.add("MR GANG");
        arrReturnData.add("MR GU");
        arrReturnData.add("MR HAN");
        arrReturnData.add("MR HONG");
        arrReturnData.add("MR HUR");
        arrReturnData.add("MR IM");
        arrReturnData.add("MR J");
        arrReturnData.add("MR JACK");
        arrReturnData.add("MR JANG");
        arrReturnData.add("MR JD");
        arrReturnData.add("MR JENG");
        arrReturnData.add("MR JI");
        arrReturnData.add("MR JIN");
        arrReturnData.add("MR JING");
        arrReturnData.add("MR JO");
        arrReturnData.add("MR JONG");
        arrReturnData.add("MR KANG");
        arrReturnData.add("MR KEON");
        arrReturnData.add("MR KIM");
        arrReturnData.add("MR KO");
        arrReturnData.add("MR KOWN");
        arrReturnData.add("MR LEE");
        arrReturnData.add("MR LI");
        arrReturnData.add("MR LIM");
        arrReturnData.add("MR LIN");
        arrReturnData.add("MR LIU");
        arrReturnData.add("MR LV");
        arrReturnData.add("MR MOON");
        arrReturnData.add("MR MS");
        arrReturnData.add("MR NAM");
        arrReturnData.add("MR OK");
        arrReturnData.add("MR PARK");
        arrReturnData.add("MR PEI");
        arrReturnData.add("MR PIAO");
        arrReturnData.add("MR QUAN");
        arrReturnData.add("MR R");
        arrReturnData.add("MR RIM");
        arrReturnData.add("MR RYU");
        arrReturnData.add("MR SEAN");
        arrReturnData.add("MR SHI");
        arrReturnData.add("MR SHIN");
        arrReturnData.add("MR SMNA");
        arrReturnData.add("MR SONG");
        arrReturnData.add("MR TOAN");
        arrReturnData.add("MR WOO");
        arrReturnData.add("MR XU");
        arrReturnData.add("MR YANG");
        arrReturnData.add("MR YIN");
        arrReturnData.add("MR YOON");
        arrReturnData.add("MR YU");
        arrReturnData.add("MR  SON");
        arrReturnData.add("MR SON");
        arrReturnData.add("MR JIN");
        arrReturnData.add("MR PARK");
        arrReturnData.add("MR  DAL");
        arrReturnData.add("MR DAL");
        arrReturnData.add("MR  KIM");
        arrReturnData.add("MR KIM");
        arrReturnData.add("MR  LLO");
        arrReturnData.add("MR LLO");
        arrReturnData.add("MR  OH");
        arrReturnData.add("MR OH");
        arrReturnData.add("MR  SUN");
        arrReturnData.add("MR SUN");
        arrReturnData.add("MR BEA");
        arrReturnData.add("MR CHI");
        arrReturnData.add("MR CHO");
        arrReturnData.add("MR CUI");
        arrReturnData.add("MR HA");
        arrReturnData.add("MR HAN");
        arrReturnData.add("MR HONG");
        arrReturnData.add("MR JANG");
        arrReturnData.add("MR JAY");
        arrReturnData.add("MR JO");
        arrReturnData.add("MR JOO");
        arrReturnData.add("MR JUNG");
        arrReturnData.add("MR KAY");
        arrReturnData.add("MR KIM");
        arrReturnData.add("MR LEE");
        arrReturnData.add("MR LIHU");
        arrReturnData.add("MR OH");
        arrReturnData.add("MR PARK");
        arrReturnData.add("MR SEO");
        arrReturnData.add("MR SHEN");
        arrReturnData.add("MR SO");
        arrReturnData.add("MR SONG");
        arrReturnData.add("MR TOM");
        arrReturnData.add("MR WEN");
        arrReturnData.add("MR WON");
        arrReturnData.add("MR YANG");
        arrReturnData.add("MR YOO");
        arrReturnData.add("MR YOON");
        arrReturnData.add("MR YU");
        arrReturnData.add("MR ZHAO");
        arrReturnData.add("MRCHO");
        arrReturnData.add("MRDONG");
        arrReturnData.add("MRKO");
        arrReturnData.add("MRLEE");
        arrReturnData.add("MRMS");
        arrReturnData.add("MRS KIM");
        arrReturnData.add("MRS LIM");
        arrReturnData.add("MISS");
        arrReturnData.add("MS");
        arrReturnData.add("MS CHAU");
        arrReturnData.add("MS CHOI");
        arrReturnData.add("MS DIEM");
        arrReturnData.add("MS KIM");
        arrReturnData.add("MS MOON");
        arrReturnData.add("MS XU");
        arrReturnData.add("MS AN");
        arrReturnData.add("MS CHOI");
        arrReturnData.add("MS CUI");
        arrReturnData.add("MS HONG");
        arrReturnData.add("MS JIN");
        arrReturnData.add("MS KIM");
        arrReturnData.add("MS YOON");
        arrReturnData.add("MSJIN");
        arrReturnData.add("MSLEE");
        arrReturnData.add("MSZHU");
        arrReturnData.add("MY HOME");
        arrReturnData.add("MY HOUSE");
        arrReturnData.add("NA");
        arrReturnData.add("NS");
        arrReturnData.add("OFFICE");
        arrReturnData.add("OHI");
        arrReturnData.add("OHO");
        arrReturnData.add("PARK");
        arrReturnData.add("POO");
        arrReturnData.add("RYU");
        arrReturnData.add("RYUE");
        arrReturnData.add("RYUE ");
        arrReturnData.add("s eoul");
        arrReturnData.add("SAM");
        arrReturnData.add("SAMUSIL");
        arrReturnData.add("SIK");
        arrReturnData.add("SSEOL");
        arrReturnData.add("SUL");
        arrReturnData.add("SUN");
        arrReturnData.add("TAC");
        arrReturnData.add("TAI");
        arrReturnData.add("TAO");
        arrReturnData.add("TEACHER");
        arrReturnData.add("THUY");
        arrReturnData.add("TOP");
        arrReturnData.add("URIJIP");
        arrReturnData.add("VIP");
        arrReturnData.add("W");
        arrReturnData.add("WANDO");
        arrReturnData.add("WEN");
        arrReturnData.add("XIA");
        arrReturnData.add("XIE");
        arrReturnData.add("XIONG");
        arrReturnData.add("YANG");
        arrReturnData.add("YOON");
        arrReturnData.add("ZHANG");

        if (checkTwoByteChar(strCheckData)) //한글이 있는지 체크 => 한글이 있는 경우에는 영문자 1바이트 이하 체크를 하지 않는다.
        {
            for (int x = 0; x < arrReturnData.size(); x++)
            {
                //관세청에서 제공한 단어인 경우 과태료대상
            	strCheckData = getAlphabetRemain_Check2(strCheckData.toUpperCase()).trim();
            	String arrStr = (arrReturnData.get(x)).toUpperCase();            	
                if (strCheckData.equals(arrStr)) {
                    bReturnValue = false;
                }
            }
        }
        else
        {
        	//영문자가 1바이트 이하인 경우 과태료대상
            if (getAlphabetRemain(strCheckData).length() <= 1) {
            	bReturnValue = false;
            }                
            else
            {
                for (int x = 0; x < arrReturnData.size(); x++)
                {
                    //관세청에서 제공한 단어인 경우 과태료대상
                	strCheckData = getAlphabetRemain_Check2(strCheckData.toUpperCase()).trim();
                	String arrStr = (arrReturnData.get(x)).toUpperCase();
                    if (strCheckData.equals(arrStr)) {
                        bReturnValue = false;
                    }
                }
            }
        }
        return bReturnValue;
    }
	
	/**
	 * 수하인-영문주소 과태료 검증
	 * @param strData
	 * @return
	 */
	public boolean checkConsigneeAddress(String strData) throws Exception
    {
		boolean bReturnValue = true;
		strData = Util.null2str(strData);
		String strCheckData = strData.trim();

		//숫자가 없는 경우 과태료대상
        if (deleteAlphabetChar(strData).length() == 0)  {
            bReturnValue = false;
        }
        else
        {
            // ro, gil, myeon, dong, ri, eup 없는 경우 과태료대상
            if (!strData.toUpperCase().contains("RO")
                && !strData.toUpperCase().contains("GIL")
                && !strData.toUpperCase().contains("MYEON")
                && !strData.toUpperCase().contains("DONG")
                && !strData.toUpperCase().contains("RI")
                && !strData.toUpperCase().contains("EUP")) {
                	bReturnValue = false;
            	}

            //영문숫자가 20바이트 이하인 경우 과태료대상(2020.10.28 : 20바이트이하 과태료 대상 추가)
            if (getAlphabetNumericRemain(strCheckData).length() <= 20)  {
            	bReturnValue = false;
            }
        }

        return bReturnValue;
    }
	
	/**
	 * 수하인 한글주소 과태료 검증
	 * @param strData
	 * @return
	 */
	public boolean checkConsigneeHanAddress(String strData,String addPlusChkType )
    {
		boolean bReturnValue = true;
		strData = Util.null2str(strData);
		strData = strData.trim();

		//addPlusChkType이 Y일경우 영문이 있는 경우 과태료대상
		 if(addPlusChkType.equals("Y") && strData.matches(".*[a-zA-Z]+.*"))
		 {
			bReturnValue = false;	 
	     }
		
		//숫자가 없는 경우 과태료대상
        if (deleteAlphabetChar(strData).length() == 0)  {
            bReturnValue = false;
        }
        else
        {
            //2020.09.15 => 한글이 있는 경우 로, 길, 면, 동, 리, 읍 이 없는 경우 과태료대상
            if (!strData.contains("로")
                && !strData.contains("길")
                && !strData.contains("면")
                && !strData.contains("동")
                && !strData.contains("리")
                && !strData.contains("읍")
                ) {
                bReturnValue = false;
            }
        }
        
        return bReturnValue;
    }
	
	/**
	 * 품명 과태료 검증
	 * @param strData
	 * @return
	 */
	public boolean checkCommodityItem(String strData) throws Exception
    {
		boolean bReturnValue = true;
		strData = Util.null2str(strData);
		String strCheckData = strData.trim().toUpperCase();
		String strReCheckData = getAlphabetRemain_Check2(strData.trim()).toUpperCase();

        //"품명 배열데이터(소재, 재질)"
        List<String> arrReturnData_A = new ArrayList<String>();
        arrReturnData_A.add("ACRYLIC");
        arrReturnData_A.add("ALLOY");
        arrReturnData_A.add("ALUMINA");
        arrReturnData_A.add("ALUMINIUM");
        arrReturnData_A.add("ARTIFICIAL FUR");
        arrReturnData_A.add("ARTIFICIAL STONE");
        arrReturnData_A.add("ARTIFICIAL WOOL");
        arrReturnData_A.add("BAUXITE");
        arrReturnData_A.add("BRONZE");
        arrReturnData_A.add("CARBON");
        arrReturnData_A.add("CARDBOARD");
        arrReturnData_A.add("COPPER");
        arrReturnData_A.add("COTTON");
        arrReturnData_A.add("COW LEATHER");
        arrReturnData_A.add("COWHIDE");
        arrReturnData_A.add("DENIM");
        arrReturnData_A.add("DUCK DOWN");
        arrReturnData_A.add("DOWN");
        arrReturnData_A.add("FAKE FUR");
        arrReturnData_A.add("FLANNEL");
        arrReturnData_A.add("GLASS");
        arrReturnData_A.add("GOAT LEATHER");
        arrReturnData_A.add("GOAT SKIN");
        arrReturnData_A.add("IRON");
        arrReturnData_A.add("KNIT");
        arrReturnData_A.add("LACE");
        arrReturnData_A.add("LAMB SKIN");
        arrReturnData_A.add("LEATHER");
        arrReturnData_A.add("MESH");
        arrReturnData_A.add("METAL");
        arrReturnData_A.add("NYLON");
        arrReturnData_A.add("PE");
        arrReturnData_A.add("PIG LEATHER");
        arrReturnData_A.add("PLASTIC");
        arrReturnData_A.add("POLYESTER");
        arrReturnData_A.add("POLY");
        arrReturnData_A.add("PU");
        arrReturnData_A.add("PVC");
        arrReturnData_A.add("RAYON");
        arrReturnData_A.add("RESIN");
        arrReturnData_A.add("RUBBER");
        arrReturnData_A.add("SILICONE");
        arrReturnData_A.add("SILICON");
        arrReturnData_A.add("SILK");
        arrReturnData_A.add("STEEL");
        arrReturnData_A.add("VELVET");
        arrReturnData_A.add("VINYL");
        arrReturnData_A.add("WOOD");
        arrReturnData_A.add("WOOL");
        arrReturnData_A.add("WOVEN");
        arrReturnData_A.add("ACRYLATE");
        arrReturnData_A.add("ALN");
        arrReturnData_A.add("CALF LEATHER");
        arrReturnData_A.add("FIBER");
        arrReturnData_A.add("CASHMERE");
        arrReturnData_A.add("CRYSTAL");
        arrReturnData_A.add("ENAMEL");
        arrReturnData_A.add("EPOXY");
        arrReturnData_A.add("GLASTIC");
        arrReturnData_A.add("GOOSE DOWN");
        arrReturnData_A.add("LINEN");
        arrReturnData_A.add("NONWOVEN");
        arrReturnData_A.add("PET");
        arrReturnData_A.add("POLYMER");
        arrReturnData_A.add("RATTAN");
        arrReturnData_A.add("STAINLESS");
        arrReturnData_A.add("STAINNESS");
        arrReturnData_A.add("SYN");
        arrReturnData_A.add("SYNTHETIC");

        //"품명 배열데이터(단위, 광의어)"
        List<String> arrReturnData_B = new ArrayList<String>();
        arrReturnData_B.add("ABROAD");
        arrReturnData_B.add("ACC");
        arrReturnData_B.add("ACCERSSORY");
        arrReturnData_B.add("ACCESSORIES");
        arrReturnData_B.add("APPLIANCES");
        arrReturnData_B.add("ARTIFICIAL");
        arrReturnData_B.add("ARTWORK");
        arrReturnData_B.add("ASSEMBLING");
        arrReturnData_B.add("ASSEMBLY");
        arrReturnData_B.add("ASSY");
        arrReturnData_B.add("AUTO");
        arrReturnData_B.add("BACK");
        arrReturnData_B.add("BARK");
        arrReturnData_B.add("BASE");
        arrReturnData_B.add("BASEBALL");
        arrReturnData_B.add("BATH");
        arrReturnData_B.add("BEATIFUL");
        arrReturnData_B.add("BEAUTY");
        arrReturnData_B.add("BERTHA");
        arrReturnData_B.add("BIN");
        arrReturnData_B.add("BIT");
        arrReturnData_B.add("BITE");
        arrReturnData_B.add("BLUETOOTH");
        arrReturnData_B.add("BOARD");
        arrReturnData_B.add("BOARDS");
        arrReturnData_B.add("BODY");
        arrReturnData_B.add("BOTTOM");
        arrReturnData_B.add("BOW");
        arrReturnData_B.add("BUBBLE");
        arrReturnData_B.add("BUILT");
        arrReturnData_B.add("BULK");
        arrReturnData_B.add("BUSH");
        arrReturnData_B.add("CARE");
        arrReturnData_B.add("CARTON");
        arrReturnData_B.add("CASE");
        arrReturnData_B.add("CASES");
        arrReturnData_B.add("CERAMIC");
        arrReturnData_B.add("CERAMICS");
        arrReturnData_B.add("CHARGE");
        arrReturnData_B.add("CHEMICAL");
        arrReturnData_B.add("CHOKE");
        arrReturnData_B.add("CHOP");
        arrReturnData_B.add("CIVIL");
        arrReturnData_B.add("CLUB");
        arrReturnData_B.add("CLUBS");
        arrReturnData_B.add("CLUSTER");
        arrReturnData_B.add("COLUMN");
        arrReturnData_B.add("COMPONENT");
        arrReturnData_B.add("CONTROL");
        arrReturnData_B.add("CORE");
        arrReturnData_B.add("COURT");
        arrReturnData_B.add("COVER");
        arrReturnData_B.add("CRAFT");
        arrReturnData_B.add("CRAFTS");
        arrReturnData_B.add("DAMP");
        arrReturnData_B.add("DATA");
        arrReturnData_B.add("DENTAL");
        arrReturnData_B.add("DEVICE");
        arrReturnData_B.add("DIAGNOSTIC");
        arrReturnData_B.add("DIALOGUE");
        arrReturnData_B.add("DIALOGUES");
        arrReturnData_B.add("DIGIT");
        arrReturnData_B.add("DIRECTORY");
        arrReturnData_B.add("DIY");
        arrReturnData_B.add("DONATION");
        arrReturnData_B.add("EA");
        arrReturnData_B.add("EDGE");
        arrReturnData_B.add("ELEC");
        arrReturnData_B.add("ELECTRIC");
        arrReturnData_B.add("ELECTRONIC");
        arrReturnData_B.add("EXPANDED");
        arrReturnData_B.add("FABRIC");
        arrReturnData_B.add("FAKE");
        arrReturnData_B.add("FIN");
        arrReturnData_B.add("FINIAL");
        arrReturnData_B.add("FINISH");
        arrReturnData_B.add("FINISHED");
        arrReturnData_B.add("FLOAT");
        arrReturnData_B.add("FOAM");
        arrReturnData_B.add("FORESKIN");
        arrReturnData_B.add("FORM");
        arrReturnData_B.add("FORMS");
        arrReturnData_B.add("FOUNDATION");
        arrReturnData_B.add("FREE");
        arrReturnData_B.add("FRONT");
        arrReturnData_B.add("GALVANIC");
        arrReturnData_B.add("GAME");
        arrReturnData_B.add("GIFT");
        arrReturnData_B.add("GOODS");
        arrReturnData_B.add("GRIP");
        arrReturnData_B.add("GUARD");
        arrReturnData_B.add("HARD");
        arrReturnData_B.add("HARDWARE");
        arrReturnData_B.add("HOBBY");
        arrReturnData_B.add("HOME");
        arrReturnData_B.add("HOUSEHOLD");
        arrReturnData_B.add("HOUSELIVING");
        arrReturnData_B.add("HOUSING");
        arrReturnData_B.add("INCH");
        arrReturnData_B.add("INSERT");
        arrReturnData_B.add("INTERFACE");
        arrReturnData_B.add("KG");
        arrReturnData_B.add("KIT");
        arrReturnData_B.add("KRAFT");
        arrReturnData_B.add("LAST");
        arrReturnData_B.add("LIST");
        arrReturnData_B.add("LIVING");
        arrReturnData_B.add("LOAD");
        arrReturnData_B.add("LOT");
        arrReturnData_B.add("MACHINE");
        arrReturnData_B.add("MACHINERY");
        arrReturnData_B.add("MAGIC");
        arrReturnData_B.add("MATERIAL");
        arrReturnData_B.add("MOCKUP");
        arrReturnData_B.add("MODEL");
        arrReturnData_B.add("NAME");
        arrReturnData_B.add("OBJECT");
        arrReturnData_B.add("PACK");
        arrReturnData_B.add("PACKAGE");
        arrReturnData_B.add("PACKING");
        arrReturnData_B.add("PACKS");
        arrReturnData_B.add("PART");
        arrReturnData_B.add("PARTS");
        arrReturnData_B.add("PATTERN");
        arrReturnData_B.add("PCS");
        arrReturnData_B.add("PIECE");
        arrReturnData_B.add("PLAYTHING");
        arrReturnData_B.add("PRICE");
        arrReturnData_B.add("PRO");
        arrReturnData_B.add("PRODUCT");
        arrReturnData_B.add("PRODUCTS");
        arrReturnData_B.add("PROOF");
        arrReturnData_B.add("PROOFS");
        arrReturnData_B.add("REAR");
        arrReturnData_B.add("ROLL");
        arrReturnData_B.add("RUNNING");
        arrReturnData_B.add("SAFETY");
        arrReturnData_B.add("SAMPLE");
        arrReturnData_B.add("SAMPLE OF");
        arrReturnData_B.add("SAMPLES");
        arrReturnData_B.add("SET");
        arrReturnData_B.add("SIMPLE");
        arrReturnData_B.add("SKIN");
        arrReturnData_B.add("SOUND");
        arrReturnData_B.add("SPARE");
        arrReturnData_B.add("SPORT");
        arrReturnData_B.add("SPORTS");
        arrReturnData_B.add("SUB");
        arrReturnData_B.add("SUBSTRATE");
        arrReturnData_B.add("SUPPLIES");
        arrReturnData_B.add("SUPPLY");
        arrReturnData_B.add("SUPPORT");
        arrReturnData_B.add("SURFACE");
        arrReturnData_B.add("TOOL");
        arrReturnData_B.add("TOOLING");
        arrReturnData_B.add("TOOLS");
        arrReturnData_B.add("TOP");
        arrReturnData_B.add("TOY");
        arrReturnData_B.add("TRAINER");
        arrReturnData_B.add("TRAINERS");
        arrReturnData_B.add("TRAINING");
        arrReturnData_B.add("UNIT");
        arrReturnData_B.add("UPPER");
        arrReturnData_B.add("VERTICAL");
        arrReturnData_B.add("VISE");
        arrReturnData_B.add("WAITING");
        arrReturnData_B.add("WOMAN");
        arrReturnData_B.add("WORK");
        arrReturnData_B.add("WORKING");
        arrReturnData_B.add("YDS");
        arrReturnData_B.add("MAIN");
        arrReturnData_B.add("MANUAL");
        arrReturnData_B.add("MEETING");
        arrReturnData_B.add("MERCHANDISE");
        arrReturnData_B.add("METER");
        arrReturnData_B.add("PAIR");
        arrReturnData_B.add("CONNECT");
        arrReturnData_B.add("CONNECTION");
        arrReturnData_B.add("PERSONAL");
        arrReturnData_B.add("PICK");
        arrReturnData_B.add("POWDER");
        arrReturnData_B.add("POWER");
        arrReturnData_B.add("REFILL");
        arrReturnData_B.add("REPARING");
        arrReturnData_B.add("REPORT");
        arrReturnData_B.add("RIGHT");
        arrReturnData_B.add("ROUND");
        arrReturnData_B.add("SAFE");
        arrReturnData_B.add("SETUP");
        arrReturnData_B.add("SHOWER");
        arrReturnData_B.add("SOUVENIR");
        arrReturnData_B.add("SOUVENIRS");
        arrReturnData_B.add("SPECIFICATION");
        arrReturnData_B.add("SUPPORTER");
        arrReturnData_B.add("SWEAT");
        arrReturnData_B.add("TAIL");
        arrReturnData_B.add("TAP");
        arrReturnData_B.add("COLLECTION");
        arrReturnData_B.add("TIP");
        arrReturnData_B.add("TOUCH");
        arrReturnData_B.add("URINAL");
        arrReturnData_B.add("VAMP");
        arrReturnData_B.add("WATER");

        //"품명 배열데이터(브랜드)"
        List<String> arrReturnData_C = new ArrayList<String>();
        arrReturnData_C.add("CERAVE");
        arrReturnData_C.add("NERO");
        arrReturnData_C.add("ABERCROMBIE");
        arrReturnData_C.add("ADIDAS");
        arrReturnData_C.add("BIANKA");
        arrReturnData_C.add("CLOROX");
        arrReturnData_C.add("DEOXIT");
        arrReturnData_C.add("DIABOLO");
        arrReturnData_C.add("DEWALT");
        arrReturnData_C.add("DICKY");
        arrReturnData_C.add("JUUL");
        arrReturnData_C.add("JORDAN");
        arrReturnData_C.add("KOPPEL");
        arrReturnData_C.add("LEGO");
        arrReturnData_C.add("LILY ALLEN");
        arrReturnData_C.add("LOHAS");
        arrReturnData_C.add("MARSHAL");
        arrReturnData_C.add("MATTEL");
        arrReturnData_C.add("MEYER");
        arrReturnData_C.add("MINECRAFT");
        arrReturnData_C.add("NEW BALANCE");
        arrReturnData_C.add("NIKE");
        arrReturnData_C.add("POLO");
        arrReturnData_C.add("PARKA");
        arrReturnData_C.add("STYLUS");
        arrReturnData_C.add("TOMMY");

        //"품명 배열데이터(의미불명)"
        List<String> arrReturnData_D = new ArrayList<String>();
        arrReturnData_D.add("A FULL VIEW");
        arrReturnData_D.add("AF");
        arrReturnData_D.add("AS YOU LIKE IT");
        arrReturnData_D.add("ATTENTION");
        arrReturnData_D.add("BAD");
        arrReturnData_D.add("BEYOND");
        arrReturnData_D.add("REWORK");
        arrReturnData_D.add("BEST");
        arrReturnData_D.add("BITTER");
        arrReturnData_D.add("BLUE");
        arrReturnData_D.add("BOHR");
        arrReturnData_D.add("BRO");
        arrReturnData_D.add("BUY");
        arrReturnData_D.add("CAPU");
        arrReturnData_D.add("CAVITY");
        arrReturnData_D.add("CHAPTER");
        arrReturnData_D.add("CO");
        arrReturnData_D.add("COMBO");
        arrReturnData_D.add("COMFORT");
        arrReturnData_D.add("COS");
        arrReturnData_D.add("DIFFERENT WORLD");
        arrReturnData_D.add("ERUPTION");
        arrReturnData_D.add("ETERNITY");
        arrReturnData_D.add("GETTER");
        arrReturnData_D.add("GRAVITY");
        arrReturnData_D.add("HOMOGENEOUS");
        arrReturnData_D.add("NIGHTY");
        arrReturnData_D.add("MYSTERY");
        arrReturnData_D.add("NULL");
        arrReturnData_D.add("PLAYER");
        arrReturnData_D.add("PRECISION");
        arrReturnData_D.add("PROTECTIVE");
        arrReturnData_D.add("RAW");
        arrReturnData_D.add("SERENE");
        arrReturnData_D.add("SERIAL");
        arrReturnData_D.add("TARGET");
        arrReturnData_D.add("TENS");
        arrReturnData_D.add("TEST");
        arrReturnData_D.add("TOUS");
        arrReturnData_D.add("UNDERCOVER");
        arrReturnData_D.add("UNKNOWN");
        arrReturnData_D.add("WARP");
        arrReturnData_D.add("WHOOSH");
        
        //"품명 배열데이터(무의미한 접속어)"
        List<String> arrReturnData_E = new ArrayList<String>();
        arrReturnData_E.add("THE");
        arrReturnData_E.add("OF");
        arrReturnData_E.add("FOR");
        arrReturnData_E.add("A");
        arrReturnData_E.add("EITHER");
        arrReturnData_E.add("OR");
        arrReturnData_E.add("OTHER");
        arrReturnData_E.add("OPPOSITE");
        
        //영문자가 1바이트 이하인 경우 과태료대상
        if (getAlphabetRemain(strCheckData).length() <= 1) {
            bReturnValue = false;
        }
        else
        {
            //숫자만 있으면 과태료대상
            if ((deleteAlphabetChar(strCheckData)).equals(getAlphabetNumericRemain(strCheckData))) {
                bReturnValue = false;
            }
            else
            {
                //관세청에서 제공한 단어인 경우 과태료대상
                try
                {
                    strCheckData = getAlphabetRemain_Check2(strCheckData).trim();
                    for (int x = 0; x < arrReturnData_A.size(); x++)  //소재, 재질
                    {
                        if (strCheckData.equals(arrReturnData_A.get(x)))
                        {
                            return false;
                        }
                    }

                    for (int x = 0; x < arrReturnData_B.size(); x++)  //단위, 광의어
                    {
                        if (strCheckData.equals(arrReturnData_B.get(x)))
                        {
                            return false;
                        }
                    }

                    for (int x = 0; x < arrReturnData_C.size(); x++)  //브랜드
                    {
                        if (strCheckData.equals(arrReturnData_C.get(x)))
                        {
                            return false;
                        }
                    }

                    for (int x = 0; x < arrReturnData_D.size(); x++)  //의미불명
                    {
                        if (strCheckData.equals(arrReturnData_D.get(x)))
                        {
                            return false;
                        }
                    }

                    for (int x = 0; x < arrReturnData_E.size(); x++)  //무의미한 접속어
                    {
                        if (strCheckData.equals(arrReturnData_E.get(x)))
                        {
                            return false;
                        }
                    }


                    //광의어 + 광의어 체크
                    for (int x = 0; x < arrReturnData_B.size(); x++)
                    {
                        strReCheckData = strReCheckData.replaceAll(arrReturnData_B.get(x), "").trim();
                        for (int y = 0; y < arrReturnData_B.size(); y++)
                        {
                            if (strReCheckData.equals(arrReturnData_B.get(x)))
                            {
                                return false;
                            }
                        }
                        strReCheckData = getAlphabetRemain_Check2(strData.trim()).toUpperCase();
                    }
                    
                }catch(Exception e)
                {
                	log.error(e.getMessage());
                }
            }
        }
        
        return bReturnValue;
    }
	
	/**
	 * 알파벳, 숫자 제외한 나머지 문자 제거
	 * @param strSource
	 * @return
	 */
    public static String getAlphabetNumericRemain(String strSource) throws Exception
    {
    	String strResult = "";

        for(int i = 0; i < strSource.length(); i++)
        {
        	int iASCIICode = strSource.charAt(i);
        	String splitChar = Character.toString(iASCIICode);
        	
        	//한글 2byte로 계산하기 위해 EUC-KR 인코딩함. 디폴트 UTF-8 은 한글3byte로 처리됨.
            int iByteCount = splitChar.getBytes("EUC-KR").length;

            if (iByteCount < 2)
            {
                if ((iASCIICode >= 65 && iASCIICode <= 90) ||  // 알파벳대문자
                (iASCIICode >= 97 && iASCIICode <= 122) ||     // 알파벳소문자
                (iASCIICode >= 48 && iASCIICode <= 57))        // 숫자
                {
                    strResult += splitChar;
                }
            }
        }

        return strResult;
    }
	
	/**
	 * 숫자를 제외한 모든문자 제거.
	 * @param str
	 * @return
	 */
    public static String deleteAlphabetChar(String strSource)
    {        
        String strResult = "";        
        strSource = Util.null2str(strSource);
        for(int i = 0; i < strSource.length(); i++) {
            int iASCIICode = strSource.charAt(i);
            char chr = strSource.charAt(i);
            
            //숫자인 경우
            if ((iASCIICode >= 48 && iASCIICode <= 57))
            {
                strResult += chr;
            }
        }        

        return strResult;
    }
	
	/**
	 * 한글포함여부 체크
	 * @param strSource
	 * @return
	 */
    public static boolean checkTwoByteChar(String strSource)
    {
    	strSource = Util.null2str(strSource);
    	
    	boolean bReturnValue = strSource.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");

        return bReturnValue;
    }
    
    /**
     * 알파벳을 제외한 나머지 문자 제거
     * @param strSource
     * @return
     */
    public static String getAlphabetRemain(String strSource)
    {
        String strResult = "";        
        strSource = Util.null2str(strSource);
        for(int i = 0; i < strSource.length(); i++) {
            int iASCIICode = strSource.charAt(i);
            char chr = strSource.charAt(i);
            
            //알파벳인 경우
            if ((iASCIICode >= 65 && iASCIICode <= 90) ||  // 알파벳대문자
                (iASCIICode >= 97 && iASCIICode <= 122))   // 알파벳소문자
            {
                strResult += chr;
            }
        }        

        return strResult;
    }
    

    /**
     * 알파벳을 제외한 나머지 문자 제거 => 특수문자는 스페이스로 치환하고 숫자는 제거한다.
     * @param strSource
     * @return
     * @throws Exception
     */
    public static String getAlphabetRemain_Check2(String strSource) throws Exception
    {
    	String strResult = "";
    	strSource = strSource.replaceAll("1", "").replaceAll("2", "").replaceAll("3", "").replaceAll("4", "").replaceAll("5", "").replaceAll("6", "").replaceAll("7", "").replaceAll("8", "").replaceAll("9", "").replaceAll("0", "");
        
        for(int i = 0; i < strSource.length(); i++) 
        {
        	int iASCIICode = strSource.charAt(i);
        	String splitChar = Character.toString(iASCIICode);
        	
        	//한글 2byte로 계산하기 위해 EUC-KR 인코딩함. 디폴트 UTF-8 은 한글3byte로 처리됨.
            int iByteCount = splitChar.getBytes("EUC-KR").length;

            if (iByteCount < 2)
            {
                if ((iASCIICode >= 65 && iASCIICode <= 90) ||  // 알파벳대문자
                (iASCIICode >= 97 && iASCIICode <= 122))       // 알파벳소문자
                {
                    strResult += splitChar;
                }
                else
                {
                    //특수문자는 스페이스로 치환한다.
                    strResult += " ";
                }
            }
        }

        return strResult;
    }
		
    /**
	* 과태료 Order List
	* @param map
	* @return
	* @throws Exception
	*/
	public OrderPenaltyList selectPenaltyOrderList(FunctionOrder functionOrder) throws Exception {
	
		OrderPenaltyList orderPenaltyList = new OrderPenaltyList();
		if(functionOrder.getPage() != 0 && functionOrder.getPageSize() != 0){
			functionOrder.setStartNum(functionOrder.getPageSize() * (functionOrder.getPage() - 1));
		}
		if(Util.null2str(functionOrder.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(functionOrder);}
		
		Map<String,Object> srchInfo = functionOrder.getSrchInfo();
		
		List<PenaltyOrder> list;
		if(srchInfo == null) {
			list = new ArrayList<PenaltyOrder>();
		}else {
			/* N개 검색(멀티타입) */
			List<String> searchNoList = new ArrayList<String>();
			if(!"".equals(Util.null2str(srchInfo.get("srchNo")))) {
				searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(srchInfo.get("srchNo"))).split(",")));
			}
			
			if(searchNoList.size() > 1){
				srchInfo.put("searchType", "multi");
				srchInfo.put("searchNoList", searchNoList);
			}else{
				srchInfo.put("searchType", "");
				srchInfo.put("srchNo", Util.null2str(srchInfo.get("srchNo")).trim().replace(",", ""));
			}
			
			srchInfo.put("cryptographyYn", functionOrder.getCryptographyYn());
			Util.getCryptographyYn(srchInfo);
			Util.accessControlDatabase(srchInfo, "srchDateFormat", true, "srchDateGbn", "od.PO_DATE|od.AR_DATE|od.OB_DATE|od.REGISTER_DATE_TIME|ad.SIMPLE_DOC_DATE|ad.MF_DATE|noDateChk", "srchNoType", "od.OD_JOB_NO|od.PO_NO|od.HBL_NO|od.MBL_NO"
					+"|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO|od.MASTER_OD_JOB_NO");
			list = functionMapper.selectPenaltyOrderList(srchInfo);
		}
		
		orderPenaltyList.setPenaltyOrderList(list);
		if(list.size() > 0) {
			orderPenaltyList.setTotal(list.get(0).getTotal());
		}else {
			orderPenaltyList.setTotal(0);
		}
		return orderPenaltyList;
	}
	
	/**
	 * Function - 등록/등록 취소
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public FunctionSaveResult orderRegistration(FunctionOrder functionOrder, HttpServletRequest request) throws Exception{
		Util.accessControlDatabase(functionOrder, "gmtTime", false, null, null, null, null);
		FunctionSaveResult functionSaveResult = new FunctionSaveResult();
		ProcessLogs processLogs = new ProcessLogs();

		Map<String, Object> resultData = new HashMap<String, Object>();
		List<Map<String, Object>> tarList;
		//등록적용 에러 메시지 출력(rowData.BLSTATUSNM + “인 건 은 반입할수 없습니다”)
		List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();
		List<List<Map<String, Object>>> spliceTarList = new ArrayList<List<Map<String, Object>>>();

		// 모드 ( S:등록적용, C:등록취소 )
		String funcMode = Util.null2str(functionOrder.getFuncMode());
		// A:선택체크, B:검색(전체)
		String chk = Util.null2str(functionOrder.getChkMode());

		/** 반입적용 불가오더 체크 */
		List<Map<String, Object>> hblNoList = new ArrayList<Map<String, Object>>();

		Map<String,Object> srchInfo = functionOrder.getSrchInfo();
		if(srchInfo == null) {
			functionSaveResult.setResultCode("notOrder");	//화면에서 처리 - 검색건이 존재하지 않습니다.
			return functionSaveResult;
		}
		srchInfo.put("func","orderRegistration");

		//선택 건인 경우에도 해당 odJobNo로 데이터 다시 조회해오는 것으로 수정됨
		if("A".equals(chk)) {
			List<String> srchOdJobNoList = new ArrayList<String>();
			for(Map<String, Object> od : functionOrder.getOdList()) {
				srchOdJobNoList.add(Util.null2str(od.get("odJobNo")));
			}
			srchInfo.put("srchOdJobNoList", srchOdJobNoList);
		}

		if( "S".equals(funcMode) ) srchInfo.put("srchStatusList", new ArrayList<String>(Arrays.asList("ECM002")));
		else if( "C".equals(funcMode) ) srchInfo.put("srchStatusList", new ArrayList<String>(Arrays.asList("ECM003")));

		//검색조건으로 조회
		srchInfo.put("cryptographyYn", functionOrder.getCryptographyYn());
		List<Map<String, Object>> list = functionMapper.selectOrderRegistrationList(srchInfo);
		if(list == null || list.size() == 0) {
			functionSaveResult.setResultCode("notOrder");	//화면에서 처리 - 검색건이 존재하지 않습니다.
			return functionSaveResult;
		}
		hblNoList.addAll(list);

		int houseCnt = hblNoList.size();
		String bound = "";
		String boundType = "";

		if(houseCnt > 0) {
			bound = Util.null2str(hblNoList.get(0).get("bound"));
			boundType = Util.null2str(hblNoList.get(0).get("boundType"));
		}

		//1000 건씩 나눠서 처리함.
		for(int i = 0; i < houseCnt; i+=1000) {
			if(i + 1000 < houseCnt) {
				functionOrder.setOdList(hblNoList.subList(i, i + 1000));
			}else{
				functionOrder.setOdList(hblNoList.subList(i, houseCnt));
			}

			tarList = functionOrder.getOdList();	//전달받은 그리드 리스트 전체
			spliceTarList.add(tarList);

			//등록 적용 검색일때만 유효성 체크 (선택일 경우는 화면단에서 유효성 체크했으므로) -> 선택 건도 데이터 다시 조회해오기로 했으므로 유효성 체크 다시 해줌
			if (tarList.size() > 0 && funcMode.equals("S")) {
				for(Map<String, Object> errMap : tarList){
					if(!Util.null2str(errMap.get("lastPodStatus")).equals("ECM002")){
						errList.add(errMap);
					}
					if(!Util.null2str(errMap.get("ecmGovSendYn")).equals("Y")){
						errList.add(errMap);
					}
				}
			}
			if (tarList.size() > 0 && funcMode.equals("C")) {
				for(Map<String, Object> errMap : tarList){
					if(!Util.null2str(errMap.get("lastPodStatus")).equals("ECM003")){
						errList.add(errMap);
					}
					if(!Util.null2str(errMap.get("ecmGovSendYn")).equals("Y")){
						errList.add(errMap);
					}
				}
			}
		}

		// lastPodStatus가 ECM002가 아니거나 ecmGovSendYn가 Y가 아닌 경우
		if(errList.size() > 0) {
			functionSaveResult.setResultCode("orderIncongruityOrder");
			resultData.put("errList", errList);

			Map<String, Object> errMsgList = new HashMap<String, Object>();
			//상태값 중복제거 하고 추출
			for(int k = 0; k < errList.size(); k++){	
				Map<String, Object> errRow = errList.get(k);
				String nm = (String)errRow.get("lastPodStatusNm");
				errMsgList.put(nm, nm);
			}

			//라스트 상태 컴마형태로 출력
			String errMsg = "";
			for(Map.Entry<String,Object> entry : errMsgList.entrySet()){
				if(false == "".equals(errMsg)) {
					errMsg = errMsg + ",";
				}

				errMsg = errMsg + entry.getKey();
			}
			resultData.put("errMsg", errMsg);
		}

		//에러가 있으면 return
		if(!Util.null2str(functionSaveResult.getResultCode()).equals("")) {
			if(Util.null2str(functionSaveResult.getResultCode()).equals("orderIncongruityOrder")){
				functionSaveResult.setResultData(resultData);
			}
			return functionSaveResult;
		}

		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", functionOrder.getEntCode());
		tmpLogMap.put("ent_business_code", functionOrder.getUserBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("process_user_id", functionOrder.getUpdateUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		tmpLogMap.put("ent_business_print_name", functionOrder.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", functionOrder.getLogUserName());
		tmpLogMap.put("process_user_name_eng", functionOrder.getLogUserNameEng());

		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));

		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			FunctionMapper functionBatchMapper = sqlSession.getMapper(FunctionMapper.class);

			Map<String, Object> podMap = new HashMap<String, Object>();
			podMap.put("entCode", functionOrder.getEntCode());
			podMap.put("entBusinessCode", "");
			podMap.put("gmtTime", functionOrder.getGmtTime());
			podMap.put("solution", "ECM");
			podMap.put("updateUserId", functionOrder.getUpdateUserId());
			podMap.put("stockType", "POD");
			podMap.put("stockSign", "POD");
			podMap.put("stockSeqLength", 8);
			podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			podMap.put("stockRemark", "ECM POD JOB NO");

			List<Map<String, Object>> insertPodList = new ArrayList<Map<String, Object>>();
			Map<String, Object> tmpInsertPodMap = null;

			// 등록
			if("S".equals(funcMode)) {
				podMap.put("podStatus", "ECM003");
				podMap.put("POD_STATUS", "ECM003");
				podMap.put("REMARKS", "등록");
				//1. 상태가 'HTA_H(발송보류, 홀드)' 인 경우에는 업데이트 하지 않음.  
	        	//2. TB_ECM_OD_ADDITIONAL.LAST_POD_STATUS-> ECM003 로 업데이트
	            //3. POD 테이블에 UPDATE/INSERT
				functionOrder.setInsStatus("ECM003");
				int seq = 1;
				//1000*x 건씩 리스트 처리
				for(int i = 0; i < spliceTarList.size(); i++){	
					List<Map<String, Object>> odList = spliceTarList.get(i);
					functionOrder.setOdNoList(odList);
					//last_pod_status ECM003로 업데이트
					functionMapper.updateCarryin(functionOrder);

					//한건씩 돌면서 한건씩 pod 테이블 저장, ES저장
					for(int j = 0; j < odList.size(); j++) {
						Map<String, Object> odRow = odList.get(j);
						//존재하면 update-> lastPodStatus기준으로 수정되어 반입 가능한 경우(ECM003) POD 테이블에 ECM005 없어야 하므로 업데이트 부분 삭제함
						tmpInsertPodMap = new HashMap<String, Object>();
						tmpInsertPodMap.put("odJobNo", odRow.get("odJobNo"));
						tmpInsertPodMap.put("hblNo", odRow.get("hblNo"));
						tmpInsertPodMap.put("POD_JOB_NO", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
						insertPodList.add(tmpInsertPodMap);
					}

					for(Map<String, Object> insertPodMap : insertPodList){
						podMap.put("odJobNo", insertPodMap.get("odJobNo"));
						podMap.put("hblNo", insertPodMap.get("hblNo"));
						podMap.put("POD_JOB_NO", insertPodMap.get("POD_JOB_NO"));
						functionBatchMapper.insertPodInfo(podMap);
					}

					sqlSession.flushStatements();
					sqlSession.clearCache();
					sqlSession.commit();
					insertPodList = new ArrayList<Map<String, Object>>();
				}

				// 처리 로그 기록
				processLogs.setEntCode(functionOrder.getEntCode());
				processLogs.setEntBusinessCode(functionOrder.getUserBusinessCode());
				processLogs.setSolution("ECM");
				processLogs.setBound(bound);
				processLogs.setBoundType(boundType);
				processLogs.setProcessUserId(functionOrder.getUpdateUserId());
				processLogs.setJobNo("");
				processLogs.setLogJobType("DATA");
				processLogs.setDescription("ORDER REGISTRATION");

				if(!"A".equals(chk)) {
					functionOrder.setOdList(null);
					processLogs.setJobLog(new Gson().toJson(functionOrder));
				}else{
					processLogs.setJobLog("");
				}

				processLogs.setResult("ok");
				processLogs.setEntBusinessPrintName(functionOrder.getLogUserBusinessPrintName());
				processLogs.setProcessUserName(functionOrder.getLogUserName());
				processLogs.setProcessUserNameEng(functionOrder.getLogUserNameEng());
				processLogs.setJobFuncType("UPDATE");
			}
			// 등록 취소
			else if("C".equals(Util.null2str(funcMode))) {
				// 1. POD 히스토리에서 ECM003 삭제
				// 2. TB_ECM_OD_ADDITIONAL LAST_POD_STATUS = 'ECM003'

				//1000*x 건씩 리스트 처리
				int seq = 1;
				for(int i = 0; i < spliceTarList.size(); i++){	
					List<Map<String, Object>> odList = spliceTarList.get(i);
					functionOrder.setOdNoList(odList);
					functionOrder.setDelStatus("ECM003");
					functionMapper.deletePodInfo(functionOrder);

					//한건씩 돌면서 한건씩 pod 테이블 저장
					for(int j = 0; j < odList.size(); j++) {
						Map<String, Object> odRow = odList.get(j);
						functionOrder.setOdJobNo(Util.null2str(odRow.get("odJobNo")));
						functionBatchMapper.updateOdLastStatus(functionOrder);
					}
					sqlSession.flushStatements();
					sqlSession.clearCache();
					sqlSession.commit();
				}

				// 처리 로그 기록
				processLogs.setEntCode(functionOrder.getEntCode());
				processLogs.setEntBusinessCode(functionOrder.getUserBusinessCode());
				processLogs.setSolution("ECM");
				processLogs.setBound(bound);
				processLogs.setBoundType(boundType);
				processLogs.setProcessUserId(functionOrder.getUpdateUserId());
				processLogs.setJobNo("");
				processLogs.setLogJobType("DATA");
				processLogs.setDescription("Order Registratio Cancel");
				if(!"A".equals(chk)) {
					functionOrder.setOdList(null);
					processLogs.setJobLog(new Gson().toJson(functionOrder));
				}else{
					processLogs.setJobLog("");
				}
				processLogs.setResult("ok");
				processLogs.setEntBusinessPrintName(functionOrder.getLogUserBusinessPrintName());
				processLogs.setProcessUserName(functionOrder.getLogUserName());
				processLogs.setProcessUserNameEng(functionOrder.getLogUserNameEng());
				processLogs.setJobFuncType("UPDATE");
			}
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}

		commonService.insertLog(processLogs, request);
		return functionSaveResult;
	}
	
	/**
	 * 개인통관부호 인증번호 재발급
	 * @param functionOrder
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public FunctionSaveResult customsIdReissuance(FunctionOrder functionOrder, HttpServletRequest request) throws Exception {
		
		FunctionSaveResult functionSaveResult = new FunctionSaveResult();
		
		String chk = Util.null2str(functionOrder.getChkMode());
		String srchYn = Util.null2str(functionOrder.getSrchYn());
		
		//검증할 개인통관부호 리스트 추출		
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		//선택일때
		if("A".equals(chk) && false == "Y".equals(srchYn)) {
			dataList.addAll(functionOrder.getOdList());
		}
		//검색일때 
		else {			
			Map<String,Object> srchInfo = functionOrder.getSrchInfo();
			if(srchInfo == null) {
				functionSaveResult.setResultCode("notOrder");
				return functionSaveResult;
			}
			srchInfo.put("cryptographyYn", functionOrder.getCryptographyYn());
			srchInfo.put("func","customsIdVali");
			//선택일때 master od_job_no 를 넘길때는 서버단에 매칭되는 hbl 을 다시 추출해야 함.
			if("A".equals(chk) && "Y".equals(srchYn)) {
				srchInfo.put("masterList", functionOrder.getOdList());
				srchInfo.put("checkMfSubGridYn", functionOrder.getCheckMfSubGridYn());
			}
			/* N개 검색(멀티타입) */
			List<String> searchNoList = new ArrayList<String>();
			if(!"".equals(Util.null2str(srchInfo.get("srchNo")))) {
				searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(srchInfo.get("srchNo"))).split(",")));
			}
			
			if(searchNoList.size() > 1){
				srchInfo.put("searchType","multi");
				srchInfo.put("searchNoList",searchNoList);
			}else{
				srchInfo.put("srchNo",Util.null2str(srchInfo.get("srchNo")).trim().replace(",", ""));
			}
			//검색조건으로 조회
			List<Map<String, Object>> list = selectOrderList(srchInfo);
			if(list == null || list.size() == 0) {
				functionSaveResult.setResultCode("notOrder");
				return functionSaveResult;
			}
			dataList.addAll(list);
		}
		
		for(int i=0; i<dataList.size(); i++) {
			Map<String,Object> verifyMap = dataList.get(i);
			if("".equals(Util.null2str(verifyMap.get("ecmYn"))) || "N".equals(Util.null2str(verifyMap.get("ecmYn")))) {
				functionSaveResult.setResultCode("notEcmYn");
				return functionSaveResult;
			}
		}
		
		Map<String, Object> returnMap = new HashMap<String, Object>();
		
		SqlSession sqlSession = null;
		try {
			
			int totCnt = dataList.size();	
			
			Map<String, Object> dataMap;
			List<Map<String, Object>> errList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> errItemList = new ArrayList<Map<String, Object>>();
			String customsId, consigneeHp, consigneeTel, ecmBuyType, agentRegNo, forwardAgentCode, salesBrokerRegNo, consigneeZipcode, orderName;
			
			for(int i = dataList.size() - 1; i >= 0; i--){
				dataMap = dataList.get(i);
				if(!Util.null2str(dataMap.get("customsIdType")).equals("A")){
					dataMap.put("consigneeIdCardNo_IF_YN", "O");
					dataMap.put("consigneeIdCardNoFailRemark", "A.개인통관코드만 검증 가능합니다.");
					errList.add(dataList.remove(i));
				}else{
					customsId = Util.null2str(dataMap.get("customsId"));
					consigneeHp = Util.null2str(dataMap.get("consigneeHp"));
					consigneeTel = Util.null2str(dataMap.get("consigneeTel"));
					ecmBuyType = Util.null2str(dataMap.get("ecmBuyType"));
					agentRegNo = Util.null2str(dataMap.get("agentRegNo"));
					forwardAgentCode = Util.null2str(dataMap.get("forwardAgentCode"));
					salesBrokerRegNo = Util.null2str(dataMap.get("salesBrokerRegNo"));
					consigneeZipcode = Util.null2str(dataMap.get("consigneeZipcode"));
					orderName = Util.null2str(dataMap.get("orderName"));
					if(customsId.equals("")){
						dataMap.put("consigneeIdCardNo_IF_YN", "O");
						dataMap.put("consigneeIdCardNoFailRemark", "개인통관부호 입력 값이 없습니다.");
						errList.add(dataList.remove(i));
					}else if(consigneeHp.equals("")){
						
						//수하인휴대전화 값이 없고 수하인전화번호 값이 있을 경우 전화번호로 검증, 둘 다 없을 경우에만 오류
						if(consigneeTel.equals("")) {
							dataMap.put("consigneeIdCardNo_IF_YN", "O");
							dataMap.put("consigneeIdCardNoFailRemark", "수하인휴대전화 입력 값이 없습니다.");
							errList.add(dataList.remove(i));
						//수하인휴대전화 값이 없고 수하인전화번호 값이 있을 경우 전화번호 길이 체크
						}else if(consigneeTel.length() < 4) {
							dataMap.put("consigneeIdCardNo_IF_YN", "O");
							dataMap.put("consigneeIdCardNoFailRemark", "전화번호가 정확하지 않습니다.");
							errList.add(dataList.remove(i));
						}
						
					}else if((functionOrder.getNameType().equals("ko") && Util.null2str(dataMap.get("consigneeNameLoc")).replace(" ", "").equals("")) || (functionOrder.getNameType().equals("en") && Util.null2str(dataMap.get("consigneeName")).replace(" ", "").equals(""))){
						dataMap.put("consigneeIdCardNo_IF_YN", "O");
						dataMap.put("consigneeIdCardNoFailRemark", "수하인명 입력 값이 없습니다.");
						errList.add(dataList.remove(i));
					}else if(customsId.length() != 13 || !customsId.matches("^(P|p|O|o)[0-9]{12}$")){
						dataMap.put("consigneeIdCardNo_IF_YN", "O");
						dataMap.put("consigneeIdCardNoFailRemark", "개인통관코드형식이 아닙니다.");
						errList.add(dataList.remove(i));
					}else if(consigneeHp.length() < 4){
						dataMap.put("consigneeIdCardNo_IF_YN", "O");
						dataMap.put("consigneeIdCardNoFailRemark", "전화번호가 정확하지 않습니다.");
						errList.add(dataList.remove(i));
					}else if("".equals(consigneeZipcode)){
						dataMap.put("consigneeIdCardNo_IF_YN", "O");
						dataMap.put("consigneeIdCardNoFailRemark", "수하인 우편번호 값이 없습니다.");
						errList.add(dataList.remove(i));
					}else if("".equals(orderName)){
						dataMap.put("consigneeIdCardNo_IF_YN", "O");
						dataMap.put("consigneeIdCardNoFailRemark", "주문자 성명이 없습니다.");
						errList.add(dataList.remove(i));
					}else if("".equals(ecmBuyType)) {
						dataMap.put("consigneeIdCardNo_IF_YN", "O");
						dataMap.put("consigneeIdCardNoFailRemark", "거래유형이 없습니다.");
						errList.add(dataList.remove(i));
					}else if(!"".equals(ecmBuyType)) {
						if("A".equals(ecmBuyType)) {
							if("".equals(agentRegNo)) {
								dataMap.put("consigneeIdCardNo_IF_YN", "O");
								dataMap.put("consigneeIdCardNoFailRemark", "구매대행업자 부호가 없습니다.");
								errList.add(dataList.remove(i));
							}
						}else if("B".equals(ecmBuyType)) {
							if("".equals(forwardAgentCode)) {
								dataMap.put("consigneeIdCardNo_IF_YN", "O");
								dataMap.put("consigneeIdCardNoFailRemark", "배송대행업자 부호가 없습니다.");
								errList.add(dataList.remove(i));
							}
						}else if("C".equals(ecmBuyType)) {
							if("".equals(salesBrokerRegNo)) {
								dataMap.put("consigneeIdCardNo_IF_YN", "O");
								dataMap.put("consigneeIdCardNoFailRemark", "판매중개자 부호가 없습니다.");
								errList.add(dataList.remove(i));
							}
						}
					}
				}
				
				if(dataMap.get("consigneeIdCardNo_IF_YN") == null) {
					List<Map<String,Object>> itemList = functionMapper.selectCustomsRessiuItemList(dataMap);
					if(itemList != null && itemList.size() > 0) {
						boolean errorChk = false;
						for(int y=0; y<itemList.size(); y++) {
							Map<String,Object> itemMap = itemList.get(y);
							if("".equals(Util.null2str(itemMap.get("orderNo")))) {
								itemMap.put("consigneeIdCardNo_IF_YN", "O");
								itemMap.put("consigneeIdCardNoFailRemark", "주문번호가 없습니다.");
								errItemList.add(itemList.get(y));
								
								if(!errorChk) {
									errList.add(itemList.get(y));
									dataList.remove(i);
								};
								errorChk = true;
							}
						}
					}else {
						dataMap.put("consigneeIdCardNo_IF_YN", "O");
						dataMap.put("consigneeIdCardNoFailRemark", "상세상품이 없습니다.");
						errList.add(dataList.remove(i));
					}
				}
				
			}
			if(errList.size() > 0){
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				CommonMapper commonBatchMapper = sqlSession.getMapper(CommonMapper.class);
				int cnt = 1;
				for(Map<String, Object> errMap : errList){
					commonBatchMapper.updateCustoms(errMap);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
				sqlSession.flushStatements();
				sqlSession.clearCache();
				sqlSession.commit();
			}
			
			
			if(errItemList.size() > 0) {
				CommonMapper commonBatchMapper = sqlSession.getMapper(CommonMapper.class);
				int cnt = 1;
				for(Map<String, Object> errMap : errItemList){
					commonBatchMapper.updateItemCustoms(errMap);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
				sqlSession.flushStatements();
				sqlSession.clearCache();
				sqlSession.commit();
			}
			
			
			
			returnMap.put("totalCnt", totCnt);
			returnMap.put("succCnt", dataList.size());
			returnMap.put("nonCnt", errList.size());
		}catch(Exception e) {
			throw e;
		}finally{
			if(sqlSession != null) sqlSession.close();
		}
		
		if(dataList.size() > 0){
			String index = "ecm-customs-reissuance-result-" + DateTime.getDate("yyyy.MM.dd");
			IndexApi indexApi = new IndexApi(restClient);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
		
			List<JSONObject> redisList = new ArrayList<JSONObject>();
			
			String id = DateTime.getDate("yyyyMMddhhmmssSSS") + "-" + functionOrder.getEntCode();
			DocumentApi documentApi = new DocumentApi(restClient);
			Map<String, Object> esMap = new HashMap<String, Object>();
			esMap.put("ent_code", functionOrder.getEntCode());
			esMap.put("id", id);
			esMap.put("order", "");
			esMap.put("status", "wait");
			esMap.put("total_count", dataList.size());
			esMap.put("fail_count", 0);
			esMap.put("register_date_time", DateUtils.nowWithMillisResolution());
			esMap.put("register_user_id", functionOrder.getUpdateUserId());
			esMap.put("register_user_name", functionOrder.getUserName());
			
			for (Map<String, Object> dataMap : dataList) {
				JSONObject json = new JSONObject();
				json.put("resultIndex", index);
				json.put("resultId", id);
				json.put("entCode", functionOrder.getEntCode());
				json.put("entBusinessCode", functionOrder.getEntBusinessCode());
				json.put("userId", functionOrder.getUpdateUserId());
				json.put("userName", functionOrder.getUserName());
				json.put("odJobNo", dataMap.get("odJobNo"));
				json.put("masterOdJobNo", dataMap.get("masterOdJobNo"));
				json.put("mablNo", dataMap.get("mblNo"));
				json.put("hblNo", dataMap.get("hblNo"));
				json.put("nameType", functionOrder.getNameType());
				redisList.add(json);
			}
			
			// 결과 조회를 위해 ES에 저장
			documentApi.createDocument(index, id, esMap);
			
			// Batch 작업을 위해 Redis에 저장
			String key = "E-COMMERCE_CUSTOMS_REISSUE_" + functionOrder.getEntCode();
		    ListOperations<String, String> listOperations = redisTemplate.opsForList();
		    
		    listOperations.rightPush(key, redisList.toString());
		}
		//결과내역 셋팅
		functionSaveResult.setResultData(returnMap);
		
		return functionSaveResult;
	}
}
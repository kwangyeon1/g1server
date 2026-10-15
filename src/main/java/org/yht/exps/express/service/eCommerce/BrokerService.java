package org.yht.exps.express.service.eCommerce;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.eCommerce.BrokerMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.sms.EnterpriseMapper;
import org.yht.exps.express.model.eCommerce.broker.BrokerHouse;
import org.yht.exps.express.model.eCommerce.broker.BrokerHouseList;
import org.yht.exps.express.model.eCommerce.broker.BrokerHouseSub;
import org.yht.exps.express.model.eCommerce.broker.BrokerHouseSubList;
import org.yht.exps.express.model.eCommerce.broker.BrokerMaster;
import org.yht.exps.express.model.eCommerce.broker.BrokerMasterList;
import org.yht.exps.express.model.eCommerce.broker.BrokerMasterSub;
import org.yht.exps.express.model.eCommerce.broker.BrokerMasterSubList;
import org.yht.exps.express.model.eCommerce.broker.CustomsInfoRequest;
import org.yht.exps.express.model.eCommerce.broker.CustomsInfoResponse;
import org.yht.exps.express.model.eCommerce.broker.CustomsShipmentRequest;
import org.yht.exps.express.model.eCommerce.broker.CustomsShipmentResponse;
import org.yht.exps.express.model.eCommerce.broker.search.SearchBrokerBatchUpdate;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderList;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.sms.SmsUserService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.FileUtil;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@Transactional
public class BrokerService {

	@Autowired
	CommonMapper commonMapper;
	@Autowired
	BrokerMapper brokerMapper;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	
	@Autowired
    CommonService commonService;
	@Autowired
    EnterpriseMapper enterpriseMapper;
	@Autowired
    SmsUserService smsUserService;
	@Autowired
	RestHighLevelClient client;	
	@Autowired
	CustomProperties customProperties;
	
	/**
	 * broker-M 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BrokerMasterList selectBrokerMasterList(SearchOrderList search) throws Exception {
		BrokerMasterList brokerMasterList = new BrokerMasterList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
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
		//
		Util.accessControlDatabase(search, "srchDateFormat|srchCusType", true, "srchDateGbn", "AR_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO");
		List<BrokerMaster> list  = brokerMapper.selectBrokerMasterList(search);
		brokerMasterList.setBrokerMasterList(list);
		if(list.size() > 0) {
			brokerMasterList.setTotal(list.get(0).getTotal());
		}else {
			brokerMasterList.setTotal(0);
		}
		return brokerMasterList;
	}
	
	/**
	 * broker-M House 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BrokerMasterSubList selectBrokerMasterSubList(SearchOrderList search) throws Exception {
		BrokerMasterSubList brokerMasterSubList = new BrokerMasterSubList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
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
		//
		Util.accessControlDatabase(search, "userLanguage", true, "srchDateGbn", "AR_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO");
		List<BrokerMasterSub> list  = brokerMapper.selectBrokerMasterSubList(search);
		brokerMasterSubList.setBrokerMasterSubList(list);
		if(list.size() > 0) {
			brokerMasterSubList.setTotal(list.get(0).getTotal());
		}else {
			brokerMasterSubList.setTotal(0);
		}
		return brokerMasterSubList;
	}
	
	/**
	 * 관세사지정 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BrokerMasterList selectCustomsOfficerList(SearchOrderList search) throws Exception {
		BrokerMasterList brokerMasterList = new BrokerMasterList();
		search.setPage(0);
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
		//
		
		List<BrokerMaster> list  = brokerMapper.selectCustomsOfficerList(search);
		brokerMasterList.setBrokerMasterList(list);
		if(list.size() > 0) {
			brokerMasterList.setTotal(list.get(0).getTotal());
		}else {
			brokerMasterList.setTotal(0);
		}
		return brokerMasterList;
	}
	
	/**
	 * broker-H 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BrokerHouseList selectBrokerHouseList(SearchOrderList search) throws Exception {
		BrokerHouseList brokerHouseList = new BrokerHouseList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
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
		//
		Util.accessControlDatabase(search, "userLanguage", true, "srchDateGbn", "AR_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO");
		List<BrokerHouse> list  = brokerMapper.selectBrokerHouseList(search);
		brokerHouseList.setBrokerHouseList(list);
		if(list.size() > 0) {
			brokerHouseList.setTotal(list.get(0).getTotal());
		}else {
			brokerHouseList.setTotal(0);
		}
		return brokerHouseList;
	}
	
	/**
	 * broker-H 금액 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BrokerHouseSubList selectBrokerHouseSubList(SearchOrderList search) throws Exception {
		BrokerHouseSubList brokerHouseSubList = new BrokerHouseSubList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
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
		//
		Util.accessControlDatabase(search, "srchDateFormat|srchCusType", true, "srchDateGbn", "noDateChk", null, null);
		List<BrokerHouseSub> list  = brokerMapper.selectBrokerHouseSubList(search);
		brokerHouseSubList.setBrokerHouseSubList(list);
		if(list.size() > 0) {
			brokerHouseSubList.setTotal(list.get(0).getTotal());
		}else {
			brokerHouseSubList.setTotal(0);
		}
		return brokerHouseSubList;
	}
	
	/**
	 * broker - 일괄변경
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult batchUpdate(SearchBrokerBatchUpdate search, HttpServletRequest request) throws Exception{
		Util.accessControlDatabase(search, "gmtTime", false, null, null, null, null);
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		String updateField = Util.null2str(search.getUpdateField());
		String description = "";
		SearchOrderList schOrder = search.getSearchOrderList();
 
		Map<String, Object> returnMap = new HashMap<String, Object>();
		List<Order> logList = new ArrayList<Order>();
		
		/* N개 검색(멀티타입) */
		if("srchData".equals(Util.null2str(search.getTargetData()))) {
			List<String> searchNoList = new ArrayList<String>();
			if(!"".equals(Util.null2str(schOrder.getSrchNo()))) {
				searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(schOrder.getSrchNo())).split(",")));
			}
			
			if(searchNoList.size() > 1){
				schOrder.setSearchType("multi");
				schOrder.setSearchNoList(searchNoList);
			}else{
				schOrder.setSearchType("");
				schOrder.setSrchNo(Util.null2str(schOrder.getSrchNo()).trim().replace(",", ""));
			}
		}else if("M".equals(Util.null2str(search.getBlTypeMh()))) {
			//M 선택건인 경우 조회조건 + 선택한 MasterOdJobNo에 모두 해당해야 함.
			schOrder.setSrchMasterOdJobNo(search.getCheckDataList().get(0));
		}
		//
		
		
		//관세사지정
		if("CUSTOMS_OFFICER".equals(updateField)) {
			description = "관세사지정";
			int officerCntCbCode=0, officerCntClearanceStatus=0, tmpCnt=0;
			String msg="";
			if(Util.null2str(schOrder.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(schOrder);}
						
			List<Map<String,Object>> cbList = search.getCustomsOfficerList();
			//상위업체별로 업데이트 처리
			if(cbList!=null && cbList.size() > 0) {
				for(int i=0;i<cbList.size();i++) {	
					//view에서 관세사가 지정되지 않은 값은 null로 세팅하면 오류남, 띄어쓰기로 빈값 표현하여 오류 방지, 관세사를 선택하지 않을 경우 업데이트 하지 않도록 변경
//					if("".equals(((String)(cbList.get(i)).get("cbCode")).trim())) continue;
					schOrder.setCbCode((String)(cbList.get(i)).get("cbCode"));
					schOrder.setCustCodeLv1((String)(cbList.get(i)).get("custCodeLv1"));
					schOrder.setCustCodeLv2(Util.null2str((String)(cbList.get(i)).get("custCodeLv2")));
					schOrder.setUpdateField(updateField);
					//ES로그 위한 select
					logList.addAll(brokerMapper.selectBrokerBatchBySrchData(schOrder));
					//상위업체별로 업데이트
					tmpCnt = brokerMapper.updateBrokerBatchBySrchData(schOrder);
					msg += (tmpCnt == 0 ? "" : "<br/>" + tmpCnt + "건 " + schOrder.getCbCode() + " 관세사로 변경되었습니다.");
					officerCntCbCode += tmpCnt;
					//상위업체별로  통관상태 업데이트
					officerCntClearanceStatus += brokerMapper.updateBrokerBatchBySrchData2(schOrder);
				}				
			}
			
			returnMap.put("officerCnt", officerCntCbCode);
			returnMap.put("officerMsg", msg);
	
		}
		//통관완료
		else if("CLEARANCE_YN".equals(updateField)) {
			Util.accessControlDatabase(schOrder, "srchDateFormat|srchCusType", false, "srchDateGbn", "|AR_DATE", "srchNoType", "|od.PO_NO|od.HBL_NO|od.MBL_NO");
			
			description = "통관완료";
			int clearanceCnt;
			
			schOrder.setUpdateField(updateField);
			if(Util.null2str(search.getTargetData()).equals("srchData")){	//검색 건인 경우
				clearanceCnt = brokerMapper.updateBrokerBatchBySrchData(schOrder);
			}else if(Util.null2str(search.getBlTypeMh()).equals("M")){	//마스터 선택 건인 경우
				//M 선택건인 경우 조회조건 + 선택한 MasterOdJobNo에 모두 해당해야 함.
				clearanceCnt = brokerMapper.updateBrokerBatchBySrchData(schOrder);
			}else { 	//하우스 선택 건인 경우
				clearanceCnt = brokerMapper.updateBrokerBatch(search);
				schOrder.setDataList(search.getCheckDataList());
				schOrder.setBlTypeMh(search.getBlTypeMh());
			}
			logList.addAll(brokerMapper.selectBrokerBatchBySrchData(schOrder));
			returnMap.put("clearanceCnt", clearanceCnt);
			
		}
		//통관지연사유
		else if("DELAY".equals(updateField)) {
			Util.accessControlDatabase(schOrder, "srchDateFormat|srchCusType", false, "srchDateGbn", "|AR_DATE", "srchNoType", "|od.PO_NO|od.HBL_NO|od.MBL_NO");
			
			description = "통관지연사유";
			int delayCntOd, delayCntAd;
			schOrder.setUpdateField(updateField);
			
			if(!Util.null2str(search.getTargetData()).equals("srchData") && !Util.null2str(search.getBlTypeMh()).equals("M")){
				schOrder.setDataList(search.getCheckDataList());
				schOrder.setBlTypeMh(search.getBlTypeMh());
			}
			List<Order> delayList = brokerMapper.selectBrokerBatchBySrchData(schOrder);

			schOrder.setClearanceViolationCode(search.getClearanceViolationCode());
			schOrder.setClearanceViolationReason(search.getClearanceViolationReason());
			if(Util.null2str(search.getTargetData()).equals("srchData")){	//검색건인 경우
				delayCntOd = brokerMapper.updateBrokerBatchBySrchData(schOrder);
				delayCntAd = brokerMapper.updateBrokerBatchBySrchData2(schOrder);
			}else if(Util.null2str(search.getBlTypeMh()).equals("M")) {		//M 선택건인 경우
				//M선택건인 경우 조회조건 + 선택한 masterOdJobNo에 모두 해당해야 함.
				delayCntOd = brokerMapper.updateBrokerBatchBySrchData(schOrder);
				delayCntAd = brokerMapper.updateBrokerBatchBySrchData2(schOrder);
			}else{	//H 선택건인 경우
				delayCntOd = brokerMapper.updateBrokerBatch(search);
				delayCntAd = brokerMapper.updateBrokerBatch2(search);
			}
			
			/** ECM251(미통관)POD 저장 */
			Map<String, Object> podMap = new HashMap<String, Object>();
			podMap.put("podTableName", 	"TB_ECM_POD_03");
			podMap.put("podStatus", "ECM251");
			
			List<Order> insertPodList = new ArrayList<Order>();
			List<Order> updateLastPodList = new ArrayList<Order>();
			
			SqlSession sqlSession = null;
			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
				int cnt = 1;
				int seqNo = 1;
				for(Order order : delayList){
					if(!Util.null2str(order.getClearanceViolationReason()).equals(schOrder.getClearanceViolationReason())){
						podMap.put("odJobNo", order.getOdJobNo());
						orderBatchMapper.deleteOrderPod(podMap);
						if( (cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
						
						insertPodList.add(order);
						if(Util.isEmpty(order.getLastPodStatus()) || Util.null2int(order.getLastPodStatus().substring(3)) <= 251){
							updateLastPodList.add(order);
						}
					}
				}
				podMap.put("entCode", search.getEntCode());
				podMap.put("gmtTime", search.getGmtTime());
				podMap.put("updateUserId", search.getUpdateUserId());
				podMap.put("remarks", schOrder.getClearanceViolationReason());
				cnt = 1;
				for(Order order : insertPodList){
					podMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seqNo++));
					podMap.put("odJobNo", order.getOdJobNo());
					podMap.put("blNo", order.getHblNo());
					orderBatchMapper.insertOrderPod(podMap);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
				
				podMap.put("noCheckLastPodStatus", "Y");
				cnt = 1;
				for(Order order : updateLastPodList){
					podMap.put("odJobNo", order.getOdJobNo());
					orderBatchMapper.updateOrderLastPod(podMap);
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
			
			logList.addAll(delayList);
			returnMap.put("delayCnt", delayCntOd);
			
		}
		
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", search.getEntCode());
		tmpLogMap.put("ent_business_code", search.getUserBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("process_user_id", search.getUpdateUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		tmpLogMap.put("ent_business_print_name", search.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", search.getLogUserName());
		tmpLogMap.put("process_user_name_eng", search.getLogUserNameEng());
		
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		
		int seq = 1;
		for(Order od : logList) {
			search.setCustomsOfficerList(null);
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpLogMap);
			logMap.put("reference_no_id", bulkData.getId());
			logMap.put("bound",od.getBound());
			logMap.put("bound_type",od.getBoundType());
			logMap.put("job_no", od.getOdJobNo());
			logMap.put("description","Broker > "+ description +" [MBL_NO : " + od.getMblNo() + " HBL_NO : "+ od.getHblNo() +"]");
			if("srchData".equals(Util.null2str(search.getTargetData()))) {
				logMap.put("job_log", new Gson().toJson(search));	//검색건일때 조회조건 로그남김
			}else {
				logMap.put("job_log", "");
			}
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
			
		}
		
		commonService.insertLogBulk(bulkList);
		
		orderSaveResult.setReturnMap(returnMap);
		return orderSaveResult;
	}
	
	/**
	 * 로그인네트웍 LCSV2 API 전송 대상건 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult selectCustomsShipmentRequest(SearchOrderList search) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> map = new HashMap<String, Object>();
		List<BrokerHouse> tmpList = new ArrayList<BrokerHouse>();
		List<Map<String, Object>> errorList = new ArrayList<Map<String, Object>>();
		Map<String, Object> error = null;
		
		// 전송할 관세사 리스트 및 관세사별 하우스 건수 조회
		List<BrokerMaster> brList = brokerMapper.selectCustomsOfficerList2(search);
		if(brList.size() < 1) {
			orderSaveResult.setResultCode("A11316"); /** 처리할 하우스 리스트가 없습니다. */
			return orderSaveResult;
		}
		for(BrokerMaster br : brList) {
			String cbCode = Util.null2str(br.getCbCode());
			if(cbCode.equals("")) {
				error = new HashMap<String, Object>();
				error.put("errorStr", "관세사가 지정되지 않은 건이 있습니다.");
				error.put("messageType", "필수값오류");
				errorList.add(error);
			}
			else if(Util.null2int(br.getHblCnt()) > 500) {
				error = new HashMap<String, Object>();
				error.put("errorStr", "관세사별 최대 500건까지 전송 가능합니다. (건수 초과 관세사 : " + cbCode + ")");
				errorList.add(error);
			}
			
			if(errorList.size() > 0) {
				error = new HashMap<String, Object>();
				error.put("errorList", errorList);
				orderSaveResult.setResultCode("ERROR");
				orderSaveResult.setResultData(error);
				return orderSaveResult;
			}
			
			// 관세사별 하우스 조회
			search.setSrchNoType("od.CB_CODE");
			search.setSrchNo(cbCode);
			List<BrokerHouse> list = brokerMapper.selectCustomsShipmentRequestList(search);
			
			if(list.size() < 1) {
				orderSaveResult.setResultCode("A11316"); /** 처리할 하우스 리스트가 없습니다. */
				return orderSaveResult;
			}
			
			map.put(cbCode, list);
		}
		
		orderSaveResult.setResultData(map);
		return orderSaveResult;
	}
	
	/**
	 * 로그인네트웍 LCSV2 API - 수입 신고 요청 정보 전송
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult customsShipmentRequest(SearchOrderList search, OrderSaveResult orderSaveResult) throws Exception {
		List<Map<String, Object>> errorList = new ArrayList<Map<String, Object>>();
		Map<String, Object> error = null;
		Map<String, Object> resultData = orderSaveResult.getResultData();
		Map<String, Object> interfaceMap = new HashMap<String, Object>();
		Map<String, Object> tmpMap = null;
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		BulkData bulkData = new BulkData();
		IndexApi indexApi = null;
		DocumentApi documentApi = null;
		String logId = ""; // ES 로그용 아이디
		String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd"); // ES 로그용 index
		int seq = 1;
		
		// 검증
		for(String cbCode : resultData.keySet()) {
			List<BrokerHouse> list = (List<BrokerHouse>)resultData.get(cbCode);
			for(BrokerHouse data : list) {
				String errorStr = "";
				String hblNo = Util.null2str(data.getHblNo());
				if("A".equals(Util.null2str(data.getEcmBuyType())) && "".equals(Util.null2str(data.getUrl()))){
					error = new HashMap<String, Object>();
					error.put("errorStr", "전자상거래유형이 A이면 URL을 입력해야 합니다.");
					error.put("blNo", hblNo);
					errorList.add(error);
				}
				
				if("".equals(Util.null2str(data.getCdType()))) {
					errorStr += ", 통관구분";
				}
				if("".equals(Util.null2str(data.getIncoterms()))) {
					errorStr += ", 인도조건";
				}
				if(Util.null2dbl(data.getGwt()) == 0) {
					errorStr += ", 중량";
				}
				if(Util.null2int(data.getPieces()) == 0) {
					errorStr += ", 수량";
				}
				if("".equals(Util.null2str(data.getShipperName()))) {
					errorStr += ", 송하인영문명";
				}
				if("".equals(Util.null2str(data.getShipperAddress()))) {
					errorStr += ", 송하인영문주소";
				}
				if("".equals(Util.null2str(data.getShipperTel()))) {
					errorStr += ", 송하인전화번호";
				}
				if("".equals(Util.null2str(data.getEcmBuyType()))) {
					errorStr += ", 전자상거래유형코드";
				}
				if("".equals(Util.null2str(data.getConsigneeNameLoc()))) {
					errorStr += ", 수하인한글명";
				}
				if("".equals(Util.null2str(data.getConsigneeZipcode()))) {
					errorStr += ", 수하인우편번호";
				}
				if("".equals(Util.null2str(data.getConsigneeAddressLoc()))) {
					errorStr += ", 수하인주소";
				}
				
				List<Map<String, Object>> itemList = null;
				String itemsInfo = Util.null2str(data.getItemsInfo());
				if(!"".equals(itemsInfo)){
					org.json.JSONArray jArr = new org.json.JSONArray(itemsInfo.replace("\n", "").replace("&#x0D;", ""));
					itemList = (List<Map<String, Object>>)Util.JSONArrayToList(jArr);
				}else{
					itemList = null;
				}
				
				if(itemList != null) {
					for(Map<String, Object> item : itemList){
						if ("".equals(Util.null2str(item.get("DETAIL_ITEM_NAME")).trim())) {
							errorStr += ", 상세물품명";
						}
						if (Util.null2int(item.get("PIECES")) == 0) {
							errorStr += ", 상세물품수량";
						}
						if (Util.null2dbl(item.get("UNIT_PRICE")) == 0) {
							errorStr += ", 상세물품단가";
						}
						if ("".equals(Util.null2str(item.get("CURRENCY_CODE")))) {
							errorStr += ", 상세물품 화폐단위";
						}
					}
					data.setItemList(itemList);
				}
				
				if(!"".equals(errorStr)) {
					error = new HashMap<String, Object>();
					error.put("messageType", "필수값오류");
					error.put("errorStr", errorStr.substring(2) + "을(를) 기재하기 바랍니다.");
					error.put("blNo", hblNo);
					errorList.add(error);
				}
			}
			
			// 관세사별 인터페이스 정보 조회
			String cbName = list.get(0).getCbName();
			tmpMap = new HashMap<String, Object>();
			tmpMap.put("entCode", search.getSrchEntCode());
			tmpMap.put("ifServiceCode", "LOGINNETWORK-LCSV2");
			tmpMap.put("customerCode", cbCode);
			tmpMap.put("solution", "ECM");
			tmpMap = commonMapper.selectCustomerApiKeyInfo(tmpMap);
			
			if(tmpMap == null) {
				error = new HashMap<String, Object>();
				error.put("messageType", "인터페이스정보 오류");
				error.put("errorStr", "인터페이스 정보를 등록해 주세요. (관세사 : " + cbName + ")");
				errorList.add(error);
			} else if("".equals(Util.null2str(tmpMap.get("IF_RECEIVER_ID"))) || 
			   "".equals(Util.null2str(tmpMap.get("IF_LOGIN_ID"))) || 
			   "".equals(Util.null2str(tmpMap.get("IF_LOGIN_PW")))) {
				error = new HashMap<String, Object>();
				error.put("messageType", "인터페이스정보 오류");
				error.put("errorStr", "인터페이스 정보를 확인해 주세요. (관세사 : " + cbName + ")");
				errorList.add(error);
			}
			interfaceMap.put(cbCode, tmpMap);
		}
		
		if(errorList.size() > 0) {
			error = new HashMap<String, Object>();
			error.put("errorList", errorList);
			orderSaveResult.setResultCode("ERROR");
			orderSaveResult.setResultData(error);
			return orderSaveResult;
		}
		
		for(String cbCode : resultData.keySet()) {
			/*****************  API 전송 데이터 Parsing Start *****************/
			CustomsShipmentRequest customsShipmentRequest = new CustomsShipmentRequest();
			List<BrokerHouse> list = (List<BrokerHouse>)resultData.get(cbCode);
			Map<String, Object> ifMap = (Map<String, Object>) interfaceMap.get(cbCode); 
			
			/** RequestHeader */
			CustomsShipmentRequest.RequestHeader requestHeader = new CustomsShipmentRequest.RequestHeader();
			String requestNo = "TEST"+DateTimeUtil.getDate("yyyyMMdd") + Util.getRandomNum(5);
			requestHeader.setRequestNo(requestNo);
			requestHeader.setGroupId(Util.null2str(ifMap.get("IF_RECEIVER_ID")));
			customsShipmentRequest.setRequestHeader(requestHeader);
			
			/** Orders */
			CustomsShipmentRequest.Orders orders = new CustomsShipmentRequest.Orders();
			List<CustomsShipmentRequest.Order> orderList = new ArrayList<CustomsShipmentRequest.Order>();
			for(BrokerHouse data : list) {
				CustomsShipmentRequest.Order order = new CustomsShipmentRequest.Order();
				order.setDeliverNo(Util.null2str(data.getHblNo()));	// 운송장번호
				order.setCustomerId(data.getAgentCode());			// 거래처 코드
				order.setOrderRefNo(data.getPoNo());				// 고객사 주문번호
				order.setSiteUrl(data.getUrl());					// 사이트 URL (ElectronicCommerceCode : A 인 경우 필수)
				order.setCustomsTag("G");							// 통관구분 (일반통관 값 고정 : G)
				order.setIncotermsTag(data.getIncoterms());			// 인도조건 (FOB, CFR, DAP)
				
				// 청구지 (Duty paid, Collect, Third, Unpaid)
				String clearancePaymentType = Util.null2str(data.getClearancePaymentType());
				if(clearancePaymentType.equals("01")) {
					order.setBillingTag("Duty paid");
				}else if(clearancePaymentType.equals("02")) {
					order.setBillingTag("Collect");
				}else if(clearancePaymentType.equals("07")) {
					order.setBillingTag("Unpaid");
				}else {
					order.setBillingTag("Collect");
				}
				
				// 사업자 의심 (Y/N)
				String cdUseType = Util.null2str(data.getCdUseType());
				if(cdUseType.equals("1")) {
					order.setCompanyYN("N");
				}else if(cdUseType.equals("2")) {
					order.setCompanyYN("Y");
				}
				
				// 검역 의심 (Y/N)
				String inspectionType = Util.null2str(data.getInspectionType());
				if(!"".equals(inspectionType)) {
					order.setInspectionYN("Y");
				}else {
					order.setInspectionYN("N");
				}
				
				// 목록 취하 여부 (Y/N)
				String dropCode = Util.null2str(data.getDropCode());
				if(!"".equals(dropCode)) {
					order.setListCustomsCancelYN("Y");
				}else {
					order.setListCustomsCancelYN("N");
				}
				
				// 목록 취하 상세 사유
				if(dropCode.equals("21") || dropCode.equals("22")) { // 검역 취하
					order.setListCustomsCancelDetail("A");
				}else if(dropCode.equals("41")) { // 의약품 취하
					order.setListCustomsCancelDetail("B");
				}else if(dropCode.equals("23")) { // 식품 취하
					order.setListCustomsCancelDetail("C");
				}else if(dropCode.equals("29")) { // 요건 취하
					order.setListCustomsCancelDetail("D");
				}else if(dropCode.equals("12")) { // 가격 취하
					order.setListCustomsCancelDetail("E");
				}else if(dropCode.equals("39")) { // 사업자 취하
					order.setListCustomsCancelDetail("F");
				}else if(dropCode.equals("13") || dropCode.equals("51")) { // 수량 취하
					order.setListCustomsCancelDetail("G");
				}else if(dropCode.equals("11")) { // 합산 취하
					order.setListCustomsCancelDetail("H");
				}else if(dropCode.equals("42") || dropCode.equals("43") || dropCode.equals("44")) { // 총포도검류 취하
					order.setListCustomsCancelDetail("I");
				}else if(dropCode.equals("45")) { // 성인용품 취하
					order.setListCustomsCancelDetail("J");
				}else if(!dropCode.equals("")) { // 복합 취하
					order.setListCustomsCancelDetail("K");
				}
				
				order.setAgreementYN("N");									// 협정 의심 여부 (Y/N 고정 : N)
				order.setGrossWeight(data.getGwt());						// 총중량 (kg)	
				order.setNumberOfPack(data.getPieces());					// 패키지수량 (박스수량)
				order.setMblNo(data.getMblNo());							// Master B/L No
				order.setArrivalDate(data.getArDate());						// 입항일 (yyyyMMdd)
				order.setNationCode(data.getOrigCountry());					// 출발지 국가 코드 (2 자리)
				order.setFlightCurrencyCode("");							// 항공 운송비 화폐
				order.setFlightAmount(0);									// 항공 운송비
				
				/** Sender */
				CustomsShipmentRequest.Sender sender = new CustomsShipmentRequest.Sender();
				sender.setName(data.getShipperName());						// 보내는 사람 영문 이름
				sender.setAddress(data.getShipperAddress());				// 보내는 사람 영문 주소
				sender.setTelNo(data.getShipperTel());						// 보내는 사람 전화번호
				sender.setCode(data.getEcmBuyType());						// 전자상거래 유형 코드 (A:전자상거래,B:구매대행,C:배송대행,Z:파악불가)
				sender.setRegistrationNo(data.getSellerRegNo());			// 해외판매자 부호
				sender.setAgentName(data.getAgentName());					// 구매/배송대행업자 명
				sender.setAgentRegistrationNo(data.getAgentRegNo());		// 구매/배송대행업자 부호
				sender.setBuyerName(data.getSalesBrokerName());				// 판매중개자 명
				sender.setBuyerRegistrationNo(data.getSalesBrokerRegNo());	// 판매중개자 부호
				order.setSender(sender);
				
				/** Receiver */
				CustomsShipmentRequest.Receiver receiver = new CustomsShipmentRequest.Receiver();
				receiver.setKorName(this.encryptAES256(data.getConsigneeNameLoc()));			// 받는 사람 한글 이름
				receiver.setSocialSecurityNo(this.encryptAES256(data.getCustomsId()));			// 받는 사람 통관고유부호
				receiver.setZipNo(this.encryptAES256(data.getConsigneeZipcode()));				// 받는 사람 우편번호
				receiver.setAddress(this.encryptAES256(data.getConsigneeAddressLoc()));			// 받는 사람 주소
				receiver.setTelNo(this.encryptAES256(data.getConsigneeTel()));					// 받는 사람 전화 1
				receiver.setMobileNo(this.encryptAES256(data.getConsigneeHp()));				// 받는 사람 전화 2
				order.setReceiver(receiver);
				
				/** Items */
				CustomsShipmentRequest.Items items = new CustomsShipmentRequest.Items();
				List<CustomsShipmentRequest.Item> itemList = new ArrayList<CustomsShipmentRequest.Item>();
				for(Map<String, Object> itemMap : data.getItemList()) {
					CustomsShipmentRequest.Item item = new CustomsShipmentRequest.Item();
					if(!"".equals(Util.null2str(itemMap.get("HS_CODE")))) {
						item.setCode(Util.null2str(itemMap.get("HS_CODE")));			// 상품코드
						item.setHsCode(Util.null2str(itemMap.get("HS_CODE")));			// HsCode
					}
					item.setName(Util.null2str(itemMap.get("DETAIL_ITEM_NAME")));		// 상품명
					if(!"".equals(Util.null2str(itemMap.get("BRAND")))) {				// 브랜드명
						item.setBrandName(Util.null2str(itemMap.get("BRAND")));
					}
					item.setQuantity(Util.null2int(itemMap.get("PIECES")));				// 수량
					item.setWeight(Util.null2dbl(itemMap.get("GWT")));					// 라인별 무게
					item.setUnitPrice(Util.null2int(itemMap.get("PIECES")) * Util.null2dbl(itemMap.get("UNIT_PRICE")));	// 상품총가격 (수량 x 단가)
					item.setCurrencyUnit(Util.null2str(itemMap.get("CURRENCY_CODE")));	// 화폐 단위
					if(!"".equals(Util.null2str(itemMap.get("ORIGIN_COUNTRY")))) {		// 원산지 코드 (예. US, SE, MY, RU, ID)
						item.setCountryOrigin(Util.null2str(itemMap.get("ORIGIN_COUNTRY")));
					}
					itemList.add(item);
				}
				if(itemList.size() > 0) {
					items.setItem(itemList);
					order.setItems(items);
				}
				orderList.add(order);
			}
			orders.setOrder(orderList);
			customsShipmentRequest.setOrders(orders);
			/*****************  API 전송 데이터 Parsing End *****************/
			
			/****************** 전송 *******************/
			String body = "";
			try {
				StringWriter sw = new StringWriter();
				JAXBContext jaxbContext = JAXBContext.newInstance(CustomsShipmentRequest.class);
				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
				jaxbMarshaller.marshal(customsShipmentRequest, sw);
				body = sw.toString();
				
				log.info("******* CustomsShipmentRequest >>>> " + body);
				orderSaveResult.setResultCode("SUCCESS");
				
				String strUrl = "https://api.tlogin.net/";
				if("YHTXX".equals(search.getSrchEntCode())) {
					strUrl = "https://apitest.tlogin.net/";
				}
				URL url = new URL(strUrl+"v01/customs/customsShipmentRequest");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setReadTimeout(15000);
				conn.setRequestProperty("Content-Type", "application/xml");
				conn.setRequestProperty("X-API-KEY", Util.null2str(ifMap.get("IF_LOGIN_ID")));
				conn.setRequestProperty("X-API-SECRET", Util.null2str(ifMap.get("IF_LOGIN_PW")));
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.getOutputStream().write(body.getBytes("utf-8"));
				
				BufferedReader in = null;
				
				int responseCode = conn.getResponseCode();
				if (responseCode == 200) in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));	// HTTP OK
				else in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));						// HTTP ERROR
				
				String inputLine = "";
		        StringBuilder strBuild = new StringBuilder();
		        
				while((inputLine = in.readLine()) != null) { // response 출력
					strBuild.append(inputLine);
				}
				
				String response = strBuild.toString();
				System.out.println(response);
				if(responseCode == 200) {
					jaxbContext = JAXBContext.newInstance(CustomsShipmentResponse.class);
					Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
					CustomsShipmentResponse customsShipmentResponse = (CustomsShipmentResponse) unmarshaller.unmarshal(new StringReader(response));
					log.info("******* CustomsShipmentResponse >>>> " + customsShipmentResponse.toString());
					CustomsShipmentResponse.ResponseHeader responseHeader = customsShipmentResponse.getResponseHeader();
					if("success".equals(Util.null2str(responseHeader.getResult()))) {
						orderSaveResult.setResultCode("SUCCESS");
						List<BrokerHouse> brokerList = (List<BrokerHouse>)resultData.get(cbCode);
						brokerMapper.updateBrokerClearanceStatus(brokerList);
					}else {
						orderSaveResult.setResultCode("ERROR");
						if(customsShipmentResponse.getErrorOrders() != null) {
							List<CustomsShipmentResponse.ErrorOrder> errorOrders = customsShipmentResponse.getErrorOrders().getErrorOrder();
							for(CustomsShipmentResponse.ErrorOrder errorOrder : errorOrders) {
								error = new HashMap<String, Object>();
								error.put("messageType", "오류");
								error.put("errorStr", Util.null2str(errorOrder.getErrorMsg()));
								error.put("blNo", Util.null2str(errorOrder.getDeliverNo()));
								errorList.add(error);
							}
						}else {
							error = new HashMap<String, Object>();
							error.put("messageType", "오류");
							error.put("errorStr", Util.null2str(responseHeader.getMessage()));
							errorList.add(error);
						}
					}
				}else {
					orderSaveResult.setResultCode("ERROR");
					error = new HashMap<String, Object>();
					error.put("messageType", "오류");
					error.put("errorStr", response);
					errorList.add(error);
				}
			}catch(Exception e) {
				log.error(Util.getPrintStacTraceString(e));
				throw e;
			}
			/****************** 전송 End *******************/
			
			// history 등록 
			tmpMap = new HashMap<String, Object>();
			logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
			
			String dataId = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++);
			
			String filePath = customProperties.getBackupDir() + "api/customs/" + search.getSrchEntCode() + "/" + DateTime.getDate("yyyy-MM") + "/";
			String fileName = dataId + ".xml";
			FileUtil.fileWrite(body.toString(), filePath, fileName);
			
			// 처리결과에 따른 로그 기록
			tmpMap.clear();
			tmpMap.put("ent_code", search.getSrchEntCode()); 		 	// 도입사코드
			tmpMap.put("customer_code", ""); 		 					// 고객코드
			tmpMap.put("solution", "ECM");      		 				// SOLUTION
			tmpMap.put("bound", Util.null2str(search.getSrchBoundType()).equals("AI") ? "AIR" : "SEA");	 // 바운드 (AIR)
			tmpMap.put("bound_type", "IMP");	 						// 바운드타입
			tmpMap.put("hbl_no", "");				 					// HBL_NO
			tmpMap.put("if_service_code", "ECM");
			tmpMap.put("if_doc_code", "SEND_CUSTOMS_SHIPMENT");
			tmpMap.put("if_doc_name", "로그인네트웍 통관요청정보전송");
			tmpMap.put("if_doc_reference_no", "");	 					// 문서번호 
			tmpMap.put("if_vender", "");								// 중개사업자
			tmpMap.put("if_send_recv_type", "S");						// 송수신타입(S:SEND, R:RECV)
			tmpMap.put("if_send_recv_method", "");						// 송수신방법
			tmpMap.put("if_file_path", filePath);			 			// 파일경로
			tmpMap.put("if_file_name", fileName);			 			// 파일명
			tmpMap.put("if_status", "SEND");			 				// 처리상태
			tmpMap.put("if_status_info", "");		 					// 송수신상태_설명
			tmpMap.put("if_sender_id", "");								// 송신식별자
			tmpMap.put("if_sender_name", "");		 					// 송신자명
			tmpMap.put("if_receiver_id", "");		 					// 수신식별자
			tmpMap.put("if_receiver_name", "");	     					// 수신자명
			tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));		// DATE
			tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));	// DATE_TIME
			tmpMap.put("if_remark", "");			 					// 비고( 에러로그 등)
			tmpMap.put("register_user_id", "API");
			tmpMap.put("register_user_name", "API");
			tmpMap.put("register_date_time", System.currentTimeMillis());
			tmpMap.put("es_job_no", logId);
			
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(logId);
			bulkData.setJsonMap(tmpMap);
			bulkList.add(bulkData);
		}
		
		if(bulkList != null && bulkList.size() > 0) {
			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			documentApi = new DocumentApi(client);
			documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
		}
		if(errorList.size() > 0) {
			error = new HashMap<String, Object>();
			error.put("errorList", errorList);
			orderSaveResult.setResultData(error);
			orderSaveResult.setResultCode("ERROR");
		}
		return orderSaveResult;
	}
	
	public String encryptAES256(String text) throws Exception {
		if("".equals(Util.null2str(text))) return "";
		
		String alg = "AES/CBC/PKCS5Padding";
		String key = "y8jvsoqluzrgkyb4"; // 32byte
		String iv = "0123456789abcdef"; // 16byte
		
		Cipher cipher = Cipher.getInstance(alg);
		SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
		IvParameterSpec ivParamSpec = new IvParameterSpec(iv.getBytes());
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParamSpec);
		byte[] encrypted = cipher.doFinal(text.getBytes("UTF-8"));
		return Base64.getEncoder().encodeToString(encrypted);
	}
	
	/**
	 * 로그인네트웍 LCSV2 API - 통관 정보 수신 요청
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult customsInfoRequest(SearchOrderList search, OrderSaveResult orderSaveResult) throws Exception {
		List<Map<String, Object>> errorList = new ArrayList<Map<String, Object>>();
		Map<String, Object> error = null;
		Map<String, Object> resultData = orderSaveResult.getResultData();
		Map<String, Object> interfaceMap = new HashMap<String, Object>();
		Map<String, Object> tmpMap = null;
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		BulkData bulkData = new BulkData();
		IndexApi indexApi = null;
		DocumentApi documentApi = null;
		String logId = ""; // ES 로그용 아이디
		String index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd"); // ES 로그용 index
		
		for(String cbCode : resultData.keySet()) {
			List<BrokerHouse> list = (List<BrokerHouse>)resultData.get(cbCode);
			String cbName = list.get(0).getCbName();
			
			tmpMap = new HashMap<String, Object>();
			tmpMap.put("entCode", search.getSrchEntCode());
			tmpMap.put("ifServiceCode", "LOGINNETWORK-LCSV2");
			tmpMap.put("customerCode", cbCode);
			tmpMap.put("solution", "ECM");
			// 관세사별 인터페이스 정보 조회
			tmpMap = commonMapper.selectCustomerApiKeyInfo(tmpMap);
			if(tmpMap == null) {
				error = new HashMap<String, Object>();
				error.put("messageType", "인터페이스정보 오류");
				error.put("errorStr", "인터페이스 정보를 등록해 주세요. (관세사 : " + cbName + ")");
				errorList.add(error);
			}
			else if("".equals(Util.null2str(tmpMap.get("IF_RECEIVER_ID"))) || 
			   "".equals(Util.null2str(tmpMap.get("IF_LOGIN_ID"))) || 
			   "".equals(Util.null2str(tmpMap.get("IF_LOGIN_PW")))) {
				error = new HashMap<String, Object>();
				error.put("messageType", "인터페이스정보 오류");
				error.put("errorStr", "인터페이스 정보를 확인해 주세요. (관세사 : " + cbName + ")");
				errorList.add(error);
			}
			interfaceMap.put(cbCode, tmpMap);
		}
		
		if(errorList.size() > 0) {
			error = new HashMap<String, Object>();
			error.put("errorList", errorList);
			orderSaveResult.setResultCode("ERROR");
			orderSaveResult.setResultData(error);
			return orderSaveResult;
		}
		
		/****************** 전송 *******************/
		for(String cbCode : resultData.keySet()) {
			CustomsInfoRequest customsInfoRequest = new CustomsInfoRequest();
			List<CustomsInfoResponse.CustomsInfo> customsInfos = new ArrayList<CustomsInfoResponse.CustomsInfo>();
			
			List<BrokerHouse> list = (List<BrokerHouse>)resultData.get(cbCode);
			Map<String, Object> ifMap = (Map<String, Object>) interfaceMap.get(cbCode);
			
			/** RequestHeader */
			CustomsShipmentRequest.RequestHeader requestHeader = new CustomsShipmentRequest.RequestHeader();
			String requestNo = "TEST"+DateTimeUtil.getDate("yyyyMMdd") + Util.getRandomNum(5);
			requestHeader.setRequestNo(requestNo);
			requestHeader.setGroupId(Util.null2str(ifMap.get("IF_RECEIVER_ID")));
			customsInfoRequest.setRequestHeader(requestHeader);
			
			/** DeliveryNos */
			CustomsInfoRequest.DeliverNos deliverNos = new CustomsInfoRequest.DeliverNos();
			List<String> deliverNo = new ArrayList<String>();
			for(BrokerHouse data : list) {
				deliverNo.add(Util.null2str(data.getHblNo()));
			}
			deliverNos.setDeliverNo(deliverNo);
			customsInfoRequest.setDeliverNos(deliverNos);
			
			/****************** 전송 *******************/
			String body = "";
			try {
				StringWriter sw = new StringWriter();
				JAXBContext jaxbContext = JAXBContext.newInstance(CustomsInfoRequest.class);
				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
				jaxbMarshaller.marshal(customsInfoRequest, sw);
				body = sw.toString();
				
				log.info("******* CustomsInfoRequest >>>> " + body);
				String strUrl = "https://api.tlogin.net/";
				if("YHTXX".equals(search.getSrchEntCode())) {
					strUrl = "https://apitest.tlogin.net/";
				}
				URL url = new URL(strUrl+"v01/customs/customsInfoRequest");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setReadTimeout(15000);
				conn.setRequestProperty("Content-Type", "application/xml");
				conn.setRequestProperty("X-API-KEY", Util.null2str(ifMap.get("IF_LOGIN_ID")));
				conn.setRequestProperty("X-API-SECRET", Util.null2str(ifMap.get("IF_LOGIN_PW")));
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.getOutputStream().write(body.getBytes("utf-8"));
				
				BufferedReader in = null;
				in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
				
				String inputLine = "";
		        StringBuilder strBuild = new StringBuilder();
		        
				while((inputLine = in.readLine()) != null) { // response 출력
					strBuild.append(inputLine);
				}
				
				String response = strBuild.toString();
				jaxbContext = JAXBContext.newInstance(CustomsInfoResponse.class);
				Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
				CustomsInfoResponse customsInfoResponse = (CustomsInfoResponse) unmarshaller.unmarshal(new StringReader(response));
				log.info("******* CustomsInfoResponse >>>> " + customsInfoResponse.toString());
				CustomsShipmentResponse.ResponseHeader responseHeader = customsInfoResponse.getResponseHeader();
				customsInfos = customsInfoResponse.getCustomsInfos().getCustomsInfo();
				if("success".equals(Util.null2str(responseHeader.getResult()))) {
					orderSaveResult.setResultCode("SUCCESS");
				}else {
					error = new HashMap<String, Object>();
					error.put("errorStr", Util.null2str(responseHeader.getMessage()));
					orderSaveResult.setResultCode("ERROR");
					orderSaveResult.setResultData(error);
				}
			}catch(Exception e) {
				log.error(Util.getPrintStacTraceString(e));
				throw e;
			}
			
			/** 전송 성공 시 통관결과값 관세사 비고에 업데이트 */
			if(customsInfos.size() > 0) {
				SqlSession sqlSession = null;
				try {
					sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
					BrokerMapper brokerBatchMapper = sqlSession.getMapper(BrokerMapper.class);
					for(CustomsInfoResponse.CustomsInfo customsInfo : customsInfos) {
						for(BrokerHouse data : list){
							if(data.getHblNo().equals(customsInfo.getDeliverNo())) {
								tmpMap = new HashMap<String, Object>();
								tmpMap.put("odJobNo", data.getOdJobNo());
								tmpMap.put("tariffRemarks", customsInfo.getCustomsResult());
								brokerBatchMapper.updateTariffRemarks(tmpMap);
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
				
				// history 등록 
				tmpMap = new HashMap<String, Object>();
				logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
				
				// 처리결과에 따른 로그 기록
				tmpMap.clear();
				tmpMap.put("ent_code", search.getSrchEntCode()); 		 	// 도입사코드
				tmpMap.put("customer_code", ""); 		 					// 고객코드
				tmpMap.put("solution", "ECM");      		 				// SOLUTION
				tmpMap.put("bound", Util.null2str(search.getSrchBoundType()).equals("AI") ? "AIR" : "SEA");	 // 바운드 (AIR)
				tmpMap.put("bound_type", "IMP");	 						// 바운드타입
				tmpMap.put("hbl_no", "");				 					// HBL_NO
				tmpMap.put("if_service_code", "ECM");
				tmpMap.put("if_doc_code", "SEND_CUSTOMS_INFO");
				tmpMap.put("if_doc_name", "로그인네트웍 통관정보요청");
				tmpMap.put("if_doc_reference_no", "");	 					// 문서번호 
				tmpMap.put("if_vender", "");								// 중개사업자
				tmpMap.put("if_send_recv_type", "R");						// 송수신타입(S:SEND, R:RECV)
				tmpMap.put("if_send_recv_method", "");						// 송수신방법
				tmpMap.put("if_file_path", "");			 					// 파일경로
				tmpMap.put("if_file_name", "");			 					// 파일명
				tmpMap.put("if_status", "SEND");			 				// 처리상태
				tmpMap.put("if_status_info", "");		 					// 송수신상태_설명
				tmpMap.put("if_sender_id", "");								// 송신식별자
				tmpMap.put("if_sender_name", "");		 					// 송신자명
				tmpMap.put("if_receiver_id", "");		 					// 수신식별자
				tmpMap.put("if_receiver_name", "");	     					// 수신자명
				tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));		// DATE
				tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));	// DATE_TIME
				tmpMap.put("if_remark", "관세사 : " + cbCode);					// 비고( 에러로그 등)
				tmpMap.put("register_user_id", "API");
				tmpMap.put("register_user_name", "API");
				tmpMap.put("register_date_time", System.currentTimeMillis());
				tmpMap.put("es_job_no", logId);
				
				bulkData = new BulkData();
				bulkData.setActionType(BulkData.Type.CREATE);
				bulkData.setIndexName(index);
				bulkData.setId(logId);
				bulkData.setJsonMap(tmpMap);
				bulkList.add(bulkData);
			}
		}
		/****************** 전송 End *******************/
		
		if(bulkList != null && bulkList.size() > 0) {
			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			documentApi = new DocumentApi(client);
			documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
		}
		return orderSaveResult;
	}
	
}
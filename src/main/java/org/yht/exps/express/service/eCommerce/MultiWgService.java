package org.yht.exps.express.service.eCommerce;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.eCommerce.MultiWgMapper;
import org.yht.exps.express.mapper.sms.EnterpriseMapper;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgList;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgOrder;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgOrderList;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgSaveResult;
import org.yht.exps.express.model.eCommerce.multiWg.search.SearchMultiWg;
import org.yht.exps.express.model.eCommerce.multiWg.search.SearchMultiWgOrderList;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.model.sms.user.User;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.sms.SmsUserService;
import org.yht.util.DateTime;
import org.yht.util.Util;

import com.google.gson.Gson;

@Service
@Transactional
public class MultiWgService {

	@Autowired
	MultiWgMapper multiWgMapper;
	
	@Autowired
    CommonService commonService;
	
	@Autowired
    EnterpriseMapper enterpriseMapper;
	@Autowired
	SmsUserService smsUserService;
	/**
	 * 멀티(중량)Order List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public MultiWgOrderList selectMultiWgList(SearchMultiWgOrderList search) throws Exception {
		MultiWgOrderList multiWgOrderList = new MultiWgOrderList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/** N건 검색 */
		List<String> srchHblNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchHblNo()))) {
			srchHblNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchHblNo())).split(",")));
		}
		
		if(srchHblNoList.size() > 1) {
			search.setSearchType("multi");
			search.setSrchHblNoList(srchHblNoList);
		}else {
			search.setSearchType("");
			search.setSrchHblNo(Util.null2str(search.getSrchHblNo()).trim().replace(",",""));
		}
		
		Util.accessControlDatabase(search, "srchCusType|srchDateFormat", true, "srchDateGbn", "od.AR_DATE|od.OB_DATE|od.REGISTER_DATE_TIME", null, null);
		List<MultiWgOrder> list  = multiWgMapper.selectMultiWgList(search);
		multiWgOrderList.setMultiWgOrderList(list);
		if(list.size() > 0) {
			multiWgOrderList.setTotal(list.get(0).getTotal());
		}else {
			multiWgOrderList.setTotal(0);
		}
		return multiWgOrderList;
	}
	
	/**
	 * 멀티(중량) Sub Order List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public MultiWgOrderList selectSubMultiWgList(SearchMultiWgOrderList search) throws Exception {
		MultiWgOrderList multiWgOrderList = new MultiWgOrderList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		Util.accessControlDatabase(search, "userLanguage", false, null, null, null, null);
		List<MultiWgOrder> list  = multiWgMapper.selectSubMultiWgList(search);
		multiWgOrderList.setMultiWgOrderList(list);
		if(list.size() > 0) {
			multiWgOrderList.setTotal(list.get(0).getTotal());
		}else {
			multiWgOrderList.setTotal(0);
		}
		return multiWgOrderList;
	}
	
	/**
	 * Order Detail
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MultiWgOrder selectOrderDetail(SearchMultiWg search) throws Exception {
		MultiWgOrder order  = multiWgMapper.selectOrder(search);
		
		return order;
	}
	
	/**
	 * 전자상거래 멀티(중량) - 일괄 delete
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MultiWgSaveResult deleteMultiWgList(MultiWgList multiWgList, HttpServletRequest request) throws Exception {
		
		if(multiWgList == null) {
			return null;
		}
		
		int resultCnt = 0;
		List<MultiWgOrder> list = multiWgList.getMultiWgOrderList();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", multiWgList.getEntCode());
		tmpLogMap.put("ent_business_code", multiWgList.getUserBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("process_user_id", multiWgList.getRegisterUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		
		// 사업장명 가져오기
		if (!"".equals(Util.null2str(multiWgList.getUserBusinessCode()))) {
			String entBusinessPrintName = enterpriseMapper.getEntBusinessPrintName(multiWgList.getUserBusinessCode());
			tmpLogMap.put("ent_business_print_name", entBusinessPrintName);
		}
		
		// 사용자명 가져오기
		if (!"".equals(Util.null2str(multiWgList.getRegisterUserId()))) {
			User user = smsUserService.getUser(multiWgList.getRegisterUserId());
			tmpLogMap.put("process_user_name", user.getUserName());
			tmpLogMap.put("process_user_name_eng", user.getUserNameEng());
		}
		
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		
		
		for(int i=0;i<list.size();i++) {
			MultiWgOrder multiWgOrder = list.get(i);
			multiWgList.setOdJobNo(multiWgOrder.getOdJobNo());
			if( multiWgMapper.deleteMultiWgOrder(multiWgList) > 0) resultCnt ++;

			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(i));
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpLogMap);
			logMap.put("reference_no_id", bulkData.getId());
			logMap.put("bound", multiWgOrder.getBound());
			logMap.put("bound_type", multiWgOrder.getBoundType());
			logMap.put("job_no", multiWgOrder.getOdJobNo());
			logMap.put("description", "멀티(중량)화물 > delete [HBL_NO : "+ multiWgOrder.getHblNo() +"]");
//			logMap.put("job_log", new Gson().toJson(multiWgList));
			logMap.put("job_log", "");
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
		}
		
		commonService.insertLogBulk(bulkList);
		
		MultiWgSaveResult multiWgSaveResult = new MultiWgSaveResult();
		multiWgSaveResult.setResultCnt(resultCnt);
		return multiWgSaveResult;
	}
	
	/**
	 * 전자상거래 멀티(중량) - 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MultiWgSaveResult saveMultiWgList(MultiWgList multiWgList, HttpServletRequest request) throws Exception {
		
		if(multiWgList == null) {
			return null;
		}		
		Util.accessControlDatabase(multiWgList, "gmtTime", false, null, null, null, null);
		multiWgMapper.deleteMultiWgOrder(multiWgList);
		List<MultiWgOrder> list = multiWgList.getMultiWgOrderList();
		
		for(int i=0;i<list.size();i++) {
			MultiWgOrder multiWgOrder = list.get(i);
			multiWgOrder.setOdJobNo(multiWgList.getOdJobNo());
			multiWgOrder.setGmtTime(multiWgList.getGmtTime());
			multiWgOrder.setRegisterUserId(multiWgList.getRegisterUserId());
			multiWgMapper.insertMulti(multiWgOrder);
			
		}
		
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(multiWgList.getEntCode());
		processLogs.setEntBusinessCode(multiWgList.getUserBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(multiWgList.getBound());
		processLogs.setBoundType(multiWgList.getBoundType());
		processLogs.setProcessUserId(multiWgList.getRegisterUserId());
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType("UPDATE");
		processLogs.setDescription("멀티(중량)화물 > "+ multiWgList.getAction() +" [HBL_NO : "+ multiWgList.getHblNo() +"]");
//		processLogs.setJobLog(new Gson().toJson(multiWgList));
		processLogs.setJobLog("");
		processLogs.setResult("ok");
		processLogs.setJobNo(multiWgList.getOdJobNo());
		processLogs.setEntBusinessPrintName(multiWgList.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(multiWgList.getLogUserName());
		processLogs.setProcessUserNameEng(multiWgList.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		MultiWgSaveResult multiWgSaveResult = new MultiWgSaveResult();
		return multiWgSaveResult;
	}
	
	/**
	 * 전자상거래 멀티(중량) - 확정 or 취소
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MultiWgSaveResult updateMultiList(MultiWgList multiWgList , HttpServletRequest request) throws Exception {
		
		if(multiWgList == null) {
			return null;
		}		
		Util.accessControlDatabase(multiWgList, "dateFormat", false, null, null, null, null);
		List<MultiWgOrder> list = multiWgList.getMultiWgOrderList();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", multiWgList.getEntCode());
		tmpLogMap.put("ent_business_code", multiWgList.getUserBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("process_user_id", multiWgList.getRegisterUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		
		// 사업장명 가져오기
		if (!"".equals(Util.null2str(multiWgList.getUserBusinessCode()))) {
			String entBusinessPrintName = enterpriseMapper.getEntBusinessPrintName(multiWgList.getUserBusinessCode());
			tmpLogMap.put("ent_business_print_name", entBusinessPrintName);
		}
		
		// 사용자명 가져오기
		if (!"".equals(Util.null2str(multiWgList.getRegisterUserId()))) {
			User user = smsUserService.getUser(multiWgList.getRegisterUserId());
			tmpLogMap.put("process_user_name", user.getUserName());
			tmpLogMap.put("process_user_name_eng", user.getUserNameEng());
		}
		
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		
		for(int i=0;i<list.size();i++) {
			MultiWgOrder multiWgOrder = list.get(i);
			multiWgOrder.setGubun(multiWgList.getGubun());
			multiWgOrder.setRecipientName(multiWgList.getRecipientName());
			multiWgOrder.setReceiptDate(multiWgList.getReceiptDate());
			multiWgOrder.setGmtTime(multiWgList.getGmtTime());
			multiWgOrder.setDateFormat(multiWgList.getDateFormat());
			multiWgMapper.updateMulti(multiWgOrder);
			
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(i));
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpLogMap);
			logMap.put("reference_no_id", bulkData.getId());
			logMap.put("bound", multiWgOrder.getBound());
			logMap.put("bound_type", multiWgOrder.getBoundType());
			logMap.put("job_no", multiWgOrder.getOdJobNo());
			logMap.put("description", "멀티(중량)화물 > "+ multiWgList.getGubun() +" [HBL_NO : "+ multiWgOrder.getHblNo() +"]");
//			logMap.put("job_log", new Gson().toJson(multiWgList));
			logMap.put("job_log", "");
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
		}
		
		commonService.insertLogBulk(bulkList);
		MultiWgSaveResult multiWgSaveResult = new MultiWgSaveResult();
		return multiWgSaveResult;
	}
}
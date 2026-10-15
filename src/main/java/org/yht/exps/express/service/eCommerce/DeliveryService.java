package org.yht.exps.express.service.eCommerce;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.eCommerce.DeliveryMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.sms.EnterpriseMapper;
import org.yht.exps.express.model.api.ResultLotte;
import org.yht.exps.express.model.eCommerce.delivery.DeliveryH;
import org.yht.exps.express.model.eCommerce.delivery.DeliveryHList;
import org.yht.exps.express.model.eCommerce.delivery.DeliveryM;
import org.yht.exps.express.model.eCommerce.delivery.DeliveryMList;
import org.yht.exps.express.model.eCommerce.delivery.DeliverySaveResult;
import org.yht.exps.express.model.eCommerce.delivery.UpdateDelivery;
import org.yht.exps.express.model.eCommerce.delivery.search.SearchDeliveryMList;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.model.sms.user.User;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.sms.SmsUserService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jcraft.jsch.Session;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class DeliveryService {

	@Autowired
	CustomProperties customProperties;
	
	@Autowired
    CommonMapper commonMapper;
	@Autowired
	OrderMapper orderMapper;
	@Autowired
	DeliveryMapper deliveryMapper;
	
    @Autowired
    EnterpriseMapper enterpriseMapper;
    
    @Autowired
	SmsUserService smsUserService;

    @Autowired
    CommonService commonService;
    
	@Autowired
	RestHighLevelClient client;	
	
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;	
	
	@Value("${spring.profiles.active}")
	private String profile;
	
	/**
	 * Delivery-M List(main) 조회
	 * @param SearchDeliveryMList
	 * @return
	 * @throws Exception
	 */	
	
	public DeliveryMList selectDeliveryMList(SearchDeliveryMList search) throws Exception {
		DeliveryMList deliveryMList = new DeliveryMList();
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
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "srchCusType|srchMasterOdJobNoListStr", true, "srchDateGbn", "AR_DATE", "srchNoType", "od.MBL_NO|od.HBL_NO|multi.DELIVERY_NO|od.SHIPMENT_BOX_ID|RETURN_REQUEST_ID");
		List<DeliveryM> list = new ArrayList<>();
		if(profile.equals("local") || profile.equals("bms11") || profile.equals("hubn1")) {
			list  = deliveryMapper.selectDeliveryMListVE(search);
		}else {
			list  = deliveryMapper.selectDeliveryMList(search);
			
		}
		deliveryMList.setDeliveryMList(list);
		if(list.size() > 0) {
			deliveryMList.setTotal(list.get(0).getTotal());
		}else {
			deliveryMList.setTotal(0);
		}
		
		return deliveryMList; 
	}		
	
	
	/**
	 * Delivery-H List(sub) 조회
	 * @param SearchDeliveryMList
	 * @return
	 * @throws Exception
	 */	
	
	public DeliveryHList selectDeliveryHList(SearchDeliveryMList search) throws Exception {
		DeliveryHList deliveryHList = new DeliveryHList();
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
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "userLanguage|srchCusType", true, "srchDateGbn", "|AR_DATE", "srchNoType", "|od.MBL_NO|od.HBL_NO|multi.DELIVERY_NO|od.SHIPMENT_BOX_ID|RETURN_REQUEST_ID");
		search.setProfileName(profile);
		List<DeliveryH> list = new ArrayList<>();
		if(profile.equals("local") || profile.equals("bms11") || profile.equals("hubn1")) {
			list  = deliveryMapper.selectDeliveryHListVE(search);
		}else {
			list  = deliveryMapper.selectDeliveryHList(search);
		}
		deliveryHList.setDeliveryHList(list);
		if(list.size() > 0) {
			deliveryHList.setTotal(list.get(0).getTotal());
		}else {
			deliveryHList.setTotal(0);
		}
		
		return deliveryHList; 
	}	
	
	/**
	 * Delivery 배송사 지정
	 * @param UpdateDelivery
	 * @return
	 * @throws Exception
	 */
	public DeliverySaveResult assignDelivery(HttpServletRequest request, UpdateDelivery assign) throws Exception {
		DeliverySaveResult result = new DeliverySaveResult();
		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		Map<String, Object> dataMap = new HashMap<String, Object>();
		
//		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		
		SqlSession sqlSession = null;
//		String hblNoStr = "";
//		String odJobNoStr = "";
		if(!"".equals(Util.null2str(assign.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(assign.getSrchNo())).split(",")));
		}
		
		if(searchNoList.size() > 1){
			assign.setSearchType("multi");
			assign.setSearchNoList(searchNoList);
		}else{
			assign.setSearchType("");
			assign.setSrchNo(Util.null2str(assign.getSrchNo()).trim().replace(",", ""));
		}
		
		if("M".equals(assign.getBlTypeMh()) && "select".equals(assign.getAction())) {
			assign.setSrchMblNo(Util.null2str(assign.getDataList().get(0).get("mblNo")));
		}
		//리스트 SELECT 후 UPDATE 로 변경 > 두 테이블을 업데이트 해야함
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(assign, "srchCusType|gmtTime", false, "srchDateGbn", "|AR_DATE", "srchNoType", "|od.MBL_NO|od.HBL_NO|multi.DELIVERY_NO");
		List<Map<String, Object>> list  = deliveryMapper.selectAssignDeliveryList(assign);
		if(list != null && list.size() > 0){
			assign.setBound(Util.null2str(list.get(0).get("BOUND")));
			assign.setSrchBoundType(Util.null2str(list.get(0).get("BOUND_TYPE")));
		}
//		BulkData bulkData = null;
//		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
//		Map<String, Object> logMap = null;
//		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
//		tmpLogMap.put("ent_code", assign.getEntCode());
//		tmpLogMap.put("ent_business_code", assign.getEntBusinessCode());
//		tmpLogMap.put("solution", "ECM");
//		tmpLogMap.put("process_user_id", assign.getUpdateUserId());
//		tmpLogMap.put("log_job_type", "DATA");
//		tmpLogMap.put("job_func_type", "UPDATE");
//		tmpLogMap.put("result", "ok");
//		tmpLogMap.put("process_date_time", System.currentTimeMillis());
//		
//
//		// 사업장명 가져오기
//		if (!"".equals(Util.null2str(assign.getEntBusinessCode()))) {
//			String entBusinessPrintName = enterpriseMapper.getEntBusinessPrintName(assign.getEntBusinessCode());
//			tmpLogMap.put("ent_business_print_name", entBusinessPrintName);
//		}
//		
//		// 사용자명 가져오기
//		if (!"".equals(Util.null2str(assign.getUpdateUserId()))) {
//			User user = smsUserService.getUser(assign.getUpdateUserId());
//			tmpLogMap.put("process_user_name", user.getUserName());
//			tmpLogMap.put("process_user_name_eng", user.getUserNameEng());
//		}
		
//		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			DeliveryMapper deliveryBatchMapper = sqlSession.getMapper(DeliveryMapper.class);
			
			int updateCnt = 1;
			
			// POD_JOB_NO 채번 
			tmpMap.put("entCode", 		assign.getEntCode());
			tmpMap.put("gmtTime", 		assign.getGmtTime());
			tmpMap.put("registerUserId",assign.getRegisterUserId());
			tmpMap.put("updateUserId", 	assign.getUpdateUserId());
			tmpMap.put("entBusinessCode", "");
			tmpMap.put("solution", 		"ECM");
			tmpMap.put("stockType", 	"POD");
			tmpMap.put("stockSign", 	"POD");
			tmpMap.put("stockSeqLength", "8");
			tmpMap.put("resultCode", 	"FIXED_SIGN_AND_SEQ_NO");
			tmpMap.put("stockRemark", 	"ECM POD JOB NO");
			tmpMap.put("deliveryCode", assign.getDeliveryCode());
			tmpMap.put("podStatus", 	"ECM330");
			tmpMap.put("podTableName", 	"TB_ECM_POD_04");
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(tmpMap, "podTableName|gmtTime", false, null, null, null, null);
			
			List<Map<String, Object>> insertPodList = new ArrayList<Map<String, Object>>();
			Map<String, Object> tmpInsertPodMap = null;
			int seq = 1;
			for(Map<String, Object> data : list) {
//				hblNoStr += ", " + Util.null2str(data.get("HBL_NO"));
//				odJobNoStr += ", " + Util.null2str(data.get("OD_JOB_NO"));
				dataMap.clear();
				dataMap.putAll(tmpMap);
				dataMap.put("odJobNo", Util.null2str(data.get("OD_JOB_NO")));
				
				deliveryBatchMapper.assignDelivery(dataMap);
				
				tmpInsertPodMap = new HashMap<String, Object>();
				tmpInsertPodMap.put("odJobNo", data.get("OD_JOB_NO"));
				tmpInsertPodMap.put("blNo", data.get("HBL_NO"));
				if(data.get("POD_ECM330") != null){
					tmpInsertPodMap.put("POD_ECM330", "Y");
					tmpInsertPodMap.put("podJobNo", data.get("POD_ECM330"));
					
					if("".equals(Util.null2str(assign.getDeliveryCode()))) {
						tmpInsertPodMap.put("podTableName", "TB_ECM_POD_04");
						orderBatchMapper.deleteOrderPodDelivery(tmpInsertPodMap);
					}
				}else{
					tmpInsertPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				}
				insertPodList.add(tmpInsertPodMap);
				
				if( (updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
				
//				bulkData = new BulkData();
//				bulkData.setActionType(BulkData.Type.CREATE);
//				bulkData.setIndexName(index);
//				bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
//				logMap = new HashMap<String, Object>();
//				logMap.putAll(tmpLogMap);
//				logMap.put("reference_no_id", bulkData.getId());
//				logMap.put("bound", Util.null2str(data.get("BOUND")));
//				logMap.put("bound_type",Util.null2str(data.get("BOUND_TYPE")));
//				logMap.put("job_no",Util.null2str(data.get("OD_JOB_NO")));
//				logMap.put("description","DELIVERY_CODE assign to "+ assign.getDeliveryCode() +"[MBLNo : "+Util.null2str(data.get("MBL_NO")) +", HBLNo :  "+ Util.null2str(data.get("HBL_NO")) +"]");
//				logMap.put("job_log", new Gson().toJson(dataMap));
//				bulkData.setJsonMap(logMap);
//				bulkList.add(bulkData);
			}
//			if(hblNoStr.length() > 2) {
//				hblNoStr = hblNoStr.substring(2);
//			}
//			if(odJobNoStr.length() > 2) {
//				odJobNoStr = odJobNoStr.substring(2);
//			}
			
			updateCnt = 1;
			
			for(Map<String, Object> insertPodMap : insertPodList){
				tmpMap.put("odJobNo", insertPodMap.get("odJobNo"));
				if(!"".equals(Util.null2str(assign.getDeliveryCode()))) {
					orderBatchMapper.updateOrderLastPod(tmpMap);
				} else {
					orderBatchMapper.updateLastPodMax(tmpMap);
				}
				if( (updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			updateCnt = 1;
			List<Map<String, Object>> updatePodList = new ArrayList<Map<String, Object>>();
			for(Map<String, Object> insertPodMap : insertPodList){
				if(insertPodMap.get("POD_ECM330") != null){
					updatePodList.add(insertPodMap);
				}else {
					if(!"".equals(Util.null2str(assign.getDeliveryCode()))) {
						tmpMap.put("podJobNo", insertPodMap.get("podJobNo"));
						tmpMap.put("odJobNo", insertPodMap.get("odJobNo"));
						tmpMap.put("blNo", insertPodMap.get("blNo"));
						
						orderBatchMapper.insertOrderPod(tmpMap);
					}
				}
				
				if( (updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			updateCnt = 1;
			if(!"".equals(Util.null2str(assign.getDeliveryCode()))) {
				for(Map<String, Object> updatePodMap : updatePodList){
					tmpMap.put("podJobNo", updatePodMap.get("podJobNo"));
					orderBatchMapper.updateOrderPod(tmpMap);
					
					if( (updateCnt++ % 1000) == 0){
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
		
		result.setTotalCnt(list.size());
//		commonService.insertLogBulk(bulkList);
		
		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(assign.getEntCode());
		processLogs.setEntBusinessCode(assign.getEntBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(assign.getBound());
		processLogs.setBoundType(assign.getSrchBoundType());
		processLogs.setProcessUserId(assign.getUpdateUserId());
		processLogs.setJobNo("");
		processLogs.setLogJobType("DATA");
		processLogs.setDescription("ASSIGN DELIVERY");
		if("search".equals(assign.getAction()))	processLogs.setJobLog(new Gson().toJson(assign));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(assign.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(assign.getLogUserName());
		processLogs.setProcessUserNameEng(assign.getLogUserNameEng());
		processLogs.setJobFuncType("UPDATE");
		commonService.insertLog(processLogs, request);
		return result;
	}	
	
	/**
	 * Delivery 배송사 지정(엑셀)
	 * @param entCode
	 * @param deliveryCode
	 * @param updateUserId
	 * @param gmtTime
	 * @param file
	 * @param userBusinessCode
	 * @param req
	 * @return OrderSaveResult
	 * @throws Exception
	 */
	public OrderSaveResult assignDeliveryExcel(String entCode, String updateUserId, String gmtTime, File file, String userBusinessCode, HttpServletRequest request) throws Exception{
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		if(file != null){
			
			String[] fieldName = null;
			String[] fieldTitle = null;
			String[] fieldLength = null;
			String[] fieldType = null;

			fieldName = 	new String[]{"mblNo",		"hblNo",	"deliveryCode"}; // 컬럼명
			fieldTitle =	new String[]{"M.B/L No",	"H.B/L No",	"배송사"}; 		 // 타이틀
			fieldLength =	new String[]{"20",			"20",		"30"}; 			 // 길이
			fieldType =		new String[]{"S",			"S",		"S"}; 			 // 타입 
				
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				Workbook wb = WorkbookFactory.create(is);
				Sheet sheet = wb.getSheetAt(0);
				Row row;
				int lastRow = sheet.getLastRowNum();
				DataFormatter df = new DataFormatter();
				
				int fieldSize = fieldName.length;
				String extension;
				String fieldValue = "";
				
				if(file.getName().indexOf(".xlsx") > -1){
					extension = "xlsx";
				}else{
					extension = "xls";
				}
				
	
				Map<String, Object> dataMap = null;
				List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
				StringBuffer errMsg = new StringBuffer();
				
				if ("xls".equals(extension)) {
					HSSFCell myCell = null;
					for(int i = 1; i < lastRow + 1; i++){ // 0은 헤더이므로 1부터 시작 
						row = sheet.getRow(i);
						if(row == null){
							continue;
						}
						dataMap = new HashMap<String, Object>();
						for(int j = 0; j < fieldSize; j++){
							myCell = (HSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i);
							if(!Util.null2str(fieldValue).equals("")){
								dataMap.put(fieldName[j], fieldValue);
							}
						}
						
						
						// MBL_NO , HBL_NO가 있을때 (필수값)
						if(!"".equals(Util.null2str(dataMap.get("mblNo"))) && !"".equals(Util.null2str(dataMap.get("hblNo")))){
							dataMap.put("entCode", entCode);
							dataMap.put("updateUserId", updateUserId);
							dataMap.put("gmtTime", gmtTime);
							/** deliveryCode값 엑셀에서 입력받도록 수정 */
//							dataMap.put("deliveryCode", deliveryCode);
							dataList.add(dataMap);
						}
					}
				}else{
					XSSFCell myCell = null;
					for(int i = 1; i < lastRow + 1; i++){ // 0은 헤더이므로 1부터 시작
						row = sheet.getRow(i);
						if(row == null){
							continue;
						}
						dataMap = new HashMap<String, Object>();
						for(int j = 0; j < fieldSize; j++){
							myCell = (XSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i);
							if(!Util.null2str(fieldValue).equals("")){
								dataMap.put(fieldName[j], fieldValue);
							}
						}
						// MBL_NO , HBL_NO가 있을때 (필수값)
						if(!"".equals(Util.null2str(dataMap.get("mblNo"))) && !"".equals(Util.null2str(dataMap.get("hblNo")))){
							dataMap.put("entCode", entCode);
							dataMap.put("updateUserId", updateUserId);
							dataMap.put("gmtTime", gmtTime);
							/** deliveryCode값 엑셀에서 입력받도록 수정 */
//							dataMap.put("deliveryCode", deliveryCode);
							dataList.add(dataMap);
						}
					}
				}
				
				
				if(errMsg.length() != 0){
					orderSaveResult.setResultCode("dataErr");
					Map<String, Object> errMap = new HashMap<String, Object>();
					errMap.put("errStr", errMsg);
					orderSaveResult.setResultData(errMap);
					return orderSaveResult;
				}
				
				if(dataList.size() > 0){
						
					SqlSession sqlSession = null;
					
//					ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
//					BulkData bulkData = null;
//					String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
//					Map<String, Object> logMap = null;
//					Map<String, Object> tmpLogMap = new HashMap<String, Object>();
//					tmpLogMap.put("ent_code", entCode);
//					tmpLogMap.put("ent_business_code", userBusinessCode);
//					tmpLogMap.put("solution", "ECM");
//					tmpLogMap.put("process_user_id", updateUserId);
//					tmpLogMap.put("log_job_type", "DATA");
//					tmpLogMap.put("job_func_type", "UPDATE");
//					tmpLogMap.put("result", "ok");
//					tmpLogMap.put("process_date_time", System.currentTimeMillis());
//
//					// 사업장명 가져오기
//					if (!"".equals(Util.null2str(userBusinessCode))) {
//						String entBusinessPrintName = enterpriseMapper.getEntBusinessPrintName(userBusinessCode);
//						tmpLogMap.put("ent_business_print_name", entBusinessPrintName);
//					}
//					
//					// 사용자명 가져오기
//					if (!"".equals(Util.null2str(updateUserId))) {
//						User user = smsUserService.getUser(updateUserId);
//						tmpLogMap.put("process_user_name", user.getUserName());
//						tmpLogMap.put("process_user_name_eng", user.getUserNameEng());
//					}
//					
//					if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
					
					try {
						sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
						DeliveryMapper deliveryMapperBatch = sqlSession.getMapper(DeliveryMapper.class);
						OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);

						Map<String, Object> podMap = new HashMap<String, Object>();
						podMap.put("entCode", entCode);
						podMap.put("entBusinessCode", "");
						podMap.put("gmtTime", gmtTime);
						podMap.put("solution", "ECM");
						podMap.put("updateUserId", updateUserId);
						podMap.put("stockType", "POD");
						podMap.put("stockSign", "POD");
						podMap.put("stockSeqLength", "8");
						podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
						podMap.put("stockRemark", "ECM POD JOB NO");
						podMap.put("podStatus", "ECM330");
						podMap.put("podTableName", "TB_ECM_POD_04");
						/** 취약성점검 SQL Injection 방지 */
						Util.accessControlDatabase(podMap, "podTableName|gmtTime", false, null, null, null, null);
						
						List<Map<String, Object>> insertPodList = new ArrayList<Map<String, Object>>();
						List<Map<String, Object>> updatePodList = new ArrayList<Map<String, Object>>();
						Map<String, Object> tmpInsertPodMap = null;
						
						Map<String, Object> tmpMap;
						for(Map<String, Object> map : dataList){
							tmpMap = deliveryMapper.selectAssignDeliveryExcel(map);
							if(tmpMap != null){
								map.put("odJobNo", tmpMap.get("OD_JOB_NO"));
								map.put("podJobNo", tmpMap.get("ECM330_POD_JOB_NO"));
							}
						}
						int seq = 1;
						int cnt = 1;
						for(Map<String, Object> map : dataList){		
							String odJobNo = Util.null2str(map.get("odJobNo"));
							if(odJobNo.equals("")) continue;
							deliveryMapperBatch.assignDelivery(map);
							
							tmpInsertPodMap = new HashMap<String, Object>();
							tmpInsertPodMap.put("deliveryCode", map.get("deliveryCode"));
							tmpInsertPodMap.put("odJobNo", odJobNo);
							if(map.get("podJobNo") != null){
								tmpInsertPodMap.put("podJobNo", map.get("podJobNo"));
								updatePodList.add(tmpInsertPodMap);
							}else{
								tmpInsertPodMap.put("blNo", map.get("hblNo"));
								tmpInsertPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
								insertPodList.add(tmpInsertPodMap);
							}
							
							if((cnt++ % 1000) == 0){
								sqlSession.flushStatements();
								sqlSession.clearCache();
							}
							
							// 처리 로그 기록
							map.put("updateType", "assign");
							map.put("userBusinessCode", userBusinessCode);
							map.put("file", file.getPath());
							
//							bulkData = new BulkData();
//							bulkData.setActionType(BulkData.Type.CREATE);
//							bulkData.setIndexName(index);
//							bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
//							logMap = new HashMap<String, Object>();
//							logMap.putAll(tmpLogMap);
//							logMap.put("reference_no_id", bulkData.getId());
//							logMap.put("bound", "");
//							logMap.put("bound_type","IMP");
//							logMap.put("job_no", odJobNo);
//							logMap.put("description","DELIVERY_CODE assign to "+ Util.null2str(map.get("deliveryCode")) + " / EXCEL [MBL_NO : " + map.get("mblNo") + "], [HBL_NO : "+ map.get("hblNo") + "]");
//							logMap.put("job_log", new Gson().toJson(map));
//							bulkData.setJsonMap(logMap);
//							bulkList.add(bulkData);
						}
						
						cnt = 1;
						for(Map<String, Object> insertPodMap : insertPodList){
							podMap.put("odJobNo", insertPodMap.get("odJobNo"));
							orderBatchMapper.updateOrderLastPod(podMap);
							if( (cnt++ % 1000) == 0){
								sqlSession.flushStatements();
								sqlSession.clearCache();
							}
						}
						for(Map<String, Object> updatePodMap : updatePodList){
							podMap.put("odJobNo", updatePodMap.get("odJobNo"));
							orderBatchMapper.updateOrderLastPod(podMap);
							if( (cnt++ % 1000) == 0){
								sqlSession.flushStatements();
								sqlSession.clearCache();
							}
						}
						cnt = 1;
						for(Map<String, Object> insertPodMap : insertPodList){
							podMap.put("odJobNo", insertPodMap.get("odJobNo"));
							podMap.put("blNo", insertPodMap.get("blNo"));
							podMap.put("podJobNo", insertPodMap.get("podJobNo"));
							podMap.put("deliveryCode", insertPodMap.get("deliveryCode"));
							orderBatchMapper.insertOrderPod(podMap);
							if( (cnt++ % 1000) == 0){
								sqlSession.flushStatements();
								sqlSession.clearCache();
							}
						}
						cnt = 1;
						for(Map<String, Object> updatePodMap : updatePodList){
							podMap.put("podJobNo", updatePodMap.get("podJobNo"));
							podMap.put("deliveryCode", updatePodMap.get("deliveryCode"));
							orderBatchMapper.updateOrderPod(podMap);
							if( (cnt++ % 1000) == 0){
								sqlSession.flushStatements();
								sqlSession.clearCache();
							}
						}
						
						sqlSession.flushStatements();
						sqlSession.clearCache();
						sqlSession.commit();
//						commonService.insertLogBulk(bulkList);
					}catch(Exception e) {
						throw e;
					}finally{
						sqlSession.close();
						wb.close();
					}
				}
			
				String backupPath = customProperties.getBackupDir() + "excel/" + entCode + "/" + DateTime.getDate("yyyy-MM");
				File dir = new File(backupPath);
				if (dir.exists() == false)	dir.mkdirs();
				String backupFilePath = backupPath + "/" + file.getName();
				file.renameTo(new File(backupFilePath));
			}catch(Exception e) {
				throw e;
			}finally{
				if (is != null) is.close();
			}
		}else{
			orderSaveResult.setResultCode("ERROR");
		}
		
		
		// 처리 로그 기록
		ProcessLogs processLogs = new ProcessLogs();
		
		// 사업장명 가져오기
		if (!"".equals(Util.null2str(userBusinessCode))) {
			processLogs.setEntBusinessPrintName(enterpriseMapper.getEntBusinessPrintName(userBusinessCode));
		}
		// 사용자명 가져오기
		if (!"".equals(Util.null2str(updateUserId))) {
			User user = smsUserService.getUser(updateUserId);
			processLogs.setProcessUserName(user.getUserName());
			processLogs.setProcessUserNameEng(user.getUserNameEng());
		}
		
		processLogs.setEntCode(entCode);
		processLogs.setEntBusinessCode(userBusinessCode);
		processLogs.setSolution("ECM");
		processLogs.setBound("COM");
		processLogs.setBoundType("COM");
		processLogs.setProcessUserId(updateUserId);
		processLogs.setJobNo("");
		processLogs.setLogJobType("DATA");
		processLogs.setDescription("ASSIGN DELIVERY EXCEL");
		processLogs.setJobLog(new Gson().toJson(orderSaveResult));
		processLogs.setResult("ok");
		processLogs.setJobFuncType("UPDATE");
		commonService.insertLog(processLogs, request);
		return orderSaveResult;
	}	
	
	public String dataChk(String data, String name, String length, String type, StringBuffer errMsg, int row) throws Exception {
		return this.dataChk(data, name, length, type, errMsg, row, true);
	}
	public String dataChk(String data, String name, String length, String type, StringBuffer errMsg, int row, boolean null2zero) throws Exception {
		//TODO 엑셀 셀타입 텍스트로 고정시킬수있는지?  없다면 추가개발필요
		if(type.equals("N")){
			if (Util.null2str(data).equals("")){
				if(null2zero){
					data = "0";
				}else{
					data = null;
				}
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
				if(null2zero){
					data = "0";
				}else{
					data = null;
				}
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
			if(data != null) {
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
	 * Delivery 배송지 정보 변경
	 * @param UpdateDelivery
	 * @return
	 * @throws Exception
	 */
	public DeliverySaveResult updateDelivery(UpdateDelivery update, HttpServletRequest request) throws Exception {
		DeliverySaveResult result = new DeliverySaveResult();
		//null처리 하지 않으면 쿼리에서 오류 발생 
		if(update.getDeliveryAddressChanged() == null)update.setDeliveryAddressChanged("");
		
		deliveryMapper.updateDelivery(update);
		
		Map<String, Object> dataMap = update.getDataList().get(0);
		//로그 추가
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		tmpMap.put("entCode", update.getEntCode());
		tmpMap.put("userBusinessCode", update.getEntBusinessCode());
		tmpMap.put("updateUserId", update.getRegisterUserId());
		tmpMap.put("gmtTime", update.getGmtTime());
		tmpMap.put("consigneeName", update.getConsigneeName());
		tmpMap.put("consigneeZipcode", update.getConsigneeZipcode());
		tmpMap.put("deliveryAddressChanged", update.getDeliveryAddressChanged());
		tmpMap.put("consigneeTel", update.getConsigneeTel());
		tmpMap.put("deliveryPaymentType", update.getDeliveryPaymentType());
		tmpMap.put("odJobNo", update.getOdJobNo());
		tmpMap.put("dataList", update.getDataList());
//		tmpMap.put("updateType", "assign");
		
		ProcessLogs processLogs = new ProcessLogs();
		update.setDataList(null);
		processLogs.setEntCode(update.getEntCode());
		processLogs.setEntBusinessCode(update.getEntBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(Util.null2str(dataMap.get("bound")));
		processLogs.setBoundType("IMP");
		processLogs.setProcessUserId(update.getRegisterUserId());
		processLogs.setLogJobType("DATA");
		processLogs.setJobFuncType("UPDATE");
		processLogs.setDescription("DELIVERY INFO changed [MBL_NO : " + Util.null2str(dataMap.get("mblNo")) + "], [HBL_NO : " + Util.null2str(dataMap.get("hblNo")) + "]");
		processLogs.setJobLog(new Gson().toJson(tmpMap));
		processLogs.setJobNo(update.getOdJobNo());
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(update.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(update.getLogUserName());
		processLogs.setProcessUserNameEng(update.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		return result;
	}		
	
	/**
	 * Delivery 배송사 취소,보류
	 * @param UpdateDelivery
	 * @return
	 * @throws Exception
	 */
	public DeliverySaveResult cancelDelivery(UpdateDelivery cancel, HttpServletRequest request) throws Exception {
		DeliverySaveResult result = new DeliverySaveResult();
		
		List<Map<String, Object>> updateList = new ArrayList<Map<String, Object>>();
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		
		String bound = cancel.getSrchBoundType().equals("AI") ? "AIR" : "SEA";
		String action = cancel.getAction(); // select(선택건) OR search(검색건)
//		String hblNoStr = "";
//		String odJobNoStr = "";
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(cancel, "srchCusType|gmtTime", false, "srchDateGbn", "|AR_DATE", "srchNoType", "|od.MBL_NO|od.HBL_NO|multi.DELIVERY_NO");
		
		if("select".equals(action)){
			tmpMap.put("blTypeMh", cancel.getBlTypeMh());
			tmpMap.put("dataList", cancel.getDataList());
			List<Map<String, Object>> cancelDeliverylList = deliveryMapper.selectCancelDeliveryList(tmpMap);
			tmpMap.clear();
			int cnt = cancelDeliverylList.size();
			cancel.getDataList().clear();
				// 매개변수 오류로 인하여 1000건씩 처리
				for(int i=0; i  < cnt; i+=1000) {
					if(i + 1000 < cnt)cancel.setDataList(cancelDeliverylList.subList(i, (i+1000)));
					else cancel.setDataList(cancelDeliverylList.subList(i, cnt));
					deliveryMapper.cancelDelivery(cancel);
				}
		}else {
			deliveryMapper.cancelDelivery(cancel);  // LAST_POD_STATUS, DELIVERY_STATUS 업데이트 -> 2024.05.30 LAST_POD_STATUS는 업데이트하지 않게 변경
		}
		
		//POD저장을 ORDERMAPPER로 합치고 업데이트를 배치로 변경...2020/04/20.HYUK START
		SqlSession sqlSession = null;
		
//		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
//		BulkData bulkData = null;
//		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
//		Map<String, Object> logMap = null;
//		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
//		tmpLogMap.put("ent_code", cancel.getEntCode());
//		tmpLogMap.put("ent_business_code", cancel.getEntBusinessCode());
//		tmpLogMap.put("solution", "ECM");
//		tmpLogMap.put("process_user_id", cancel.getUpdateUserId());
//		tmpLogMap.put("log_job_type", "DATA");
//		tmpLogMap.put("job_func_type", "UPDATE");
//		tmpLogMap.put("bound", bound);
//		tmpLogMap.put("bound_type", "IMP");
//		tmpLogMap.put("result", "ok");
//		tmpLogMap.put("process_date_time", System.currentTimeMillis());
//		
//		// 사업장명 가져오기
//		if (!"".equals(Util.null2str(cancel.getEntBusinessCode()))) {
//			String entBusinessPrintName = enterpriseMapper.getEntBusinessPrintName(cancel.getEntBusinessCode());
//			tmpLogMap.put("ent_business_print_name", entBusinessPrintName);
//		}
//		
//		// 사용자명 가져오기
//		if (!"".equals(Util.null2str(cancel.getUpdateUserId()))) {
//			User user = smsUserService.getUser(cancel.getUpdateUserId());
//			tmpLogMap.put("process_user_name", user.getUserName());
//			tmpLogMap.put("process_user_name_eng", user.getUserNameEng());
//		}
//		
//		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		
		try {
			
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			
			int updateCnt = 1;
			
			tmpMap.put("entCode", 		cancel.getEntCode());
			tmpMap.put("gmtTime", 		cancel.getGmtTime());
			tmpMap.put("registerUserId",cancel.getRegisterUserId());
			tmpMap.put("updateUserId", 	cancel.getUpdateUserId());
			tmpMap.put("entBusinessCode", "");
			tmpMap.put("solution", 		"ECM");
			tmpMap.put("stockType", 	"POD");
			tmpMap.put("stockSign", 	"POD");
			tmpMap.put("stockSeqLength", "8");
			tmpMap.put("resultCode", 	"FIXED_SIGN_AND_SEQ_NO");
			tmpMap.put("stockRemark", 	"ECM POD JOB NO");
			tmpMap.put("podStatus", "ECM350");
			tmpMap.put("podTableName", 	"TB_ECM_POD_04");
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(tmpMap, "podTableName|gmtTime", false, null, null, null, null);
			
			if("select".equals(action)) {
				updateList = cancel.getDataList(); // 선택한 건들
				
			}else if("search".equals(action)) {
				
				/* N개 검색(멀티타입) */
				List<String> searchNoList = new ArrayList<String>();
				if(!"".equals(Util.null2str(cancel.getSrchNo()))) {
					searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(cancel.getSrchNo())).split(",")));
				}
				
				if(searchNoList.size() > 1){
					cancel.setSearchType("multi");
					cancel.setSearchNoList(searchNoList);
				}else{
					cancel.setSearchType("");
					cancel.setSrchNo(Util.null2str(cancel.getSrchNo()).trim().replace(",", ""));
				}
				
				// 검색조건으로 hblNo, odJobNo, deliveryCode 가져와서 처리.
				updateList = deliveryMapper.selectOdJobNos(cancel);
			}
			int seq = 1;
			
			List<Map<String, Object>> updatePodList = new ArrayList<Map<String, Object>>();
			for (Map<String, Object> updateMap : updateList) {
				if(updateMap.get("ECM350_POD_JOB_NO") != null){
					updatePodList.add(updateMap);
				}else{
					updateMap.putAll(tmpMap);
					updateMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(6) + Util.setSeqNo(seq++));
					orderBatchMapper.insertOrderPod(updateMap);
				}
				if( (updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			updateCnt = 1;
			for (Map<String, Object> updateMap : updatePodList) {
				updateMap.putAll(tmpMap);
				updateMap.put("podJobNo", updateMap.get("ECM350_POD_JOB_NO"));
				orderBatchMapper.updateOrderPod(updateMap);
				
				if( (updateCnt++ % 1000) == 0){
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
		
		//POD저장을 ORDERMAPPER로 합치고 업데이트를 배치로 변경...2020/04/20.HYUK END
//		commonService.insertLogBulk(bulkList);

		// 처리 로그 기록
//		if(hblNoStr.length() > 2) {
//			hblNoStr = hblNoStr.substring(2);
//		}
//		if(odJobNoStr.length() > 2) {
//			odJobNoStr = odJobNoStr.substring(2);
//		}
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(cancel.getEntCode());
		processLogs.setEntBusinessCode(cancel.getEntBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(bound);
		processLogs.setBoundType("IMP");
		processLogs.setProcessUserId(cancel.getUpdateUserId());
		processLogs.setJobNo("");
		processLogs.setLogJobType("DATA");
		processLogs.setDescription("CANCEL DELIVERY");
		if("search".equals(action))	processLogs.setJobLog(new Gson().toJson(cancel));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(cancel.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(cancel.getLogUserName());
		processLogs.setProcessUserNameEng(cancel.getLogUserNameEng());
		processLogs.setJobFuncType("UPDATE");
		commonService.insertLog(processLogs, request);
		return result;
	}

	/**
	 * Delivery 보류(Hold, ECM425) 처리
	 * @param UpdateDelivery
	 * @param HttpServletRequest
	 * @return DeliverySaveResult
	 * @throws Exception
	 */
	public DeliverySaveResult holdDelivery(UpdateDelivery hold, HttpServletRequest request) throws Exception {
		DeliverySaveResult result = new DeliverySaveResult();

		String action = hold.getAction(); // select(선택건) OR search(검색건)

		boolean veProfile = profile.equals("local") || profile.equals("bms11") || profile.equals("hubn1");

		List<Map<String, Object>> dataList;
		if("search".equals(action)) {
			/** 취약성점검 SQL Injection 방지 (검색조건 사용) */
			Util.accessControlDatabase(hold, "srchCusType|gmtTime", false, "srchDateGbn", "|AR_DATE", "srchNoType", "|od.MBL_NO|od.HBL_NO|multi.DELIVERY_NO");

			/* N개 검색(멀티타입) */
			List<String> searchNoList = new ArrayList<String>();
			if(!"".equals(Util.null2str(hold.getSrchNo()))) {
				searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(hold.getSrchNo())).split(",")));
			}
			if(searchNoList.size() > 1){
				hold.setSearchType("multi");
				hold.setSearchNoList(searchNoList);
			}else{
				hold.setSearchType("");
				hold.setSrchNo(Util.null2str(hold.getSrchNo()).trim().replace(",", ""));
			}

			// 검색조건으로 대상 조회 (멀티건은 seqNo 단위로 전개). hold 전용 쿼리는 hblNo/multiOdJobNo/seqNoId/deliveryNo를 직접 반환
			dataList = deliveryMapper.selectHoldOdJobNos(hold);
		}else {
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(hold, "gmtTime", false, null, null, null, null);

			if("M".equals(hold.getBlTypeMh())) {
				// 마스터 선택건: 마스터에 속한 하우스(H) 목록으로 전개 (멀티건은 seqNo 단위로 전개)
				Map<String, Object> listParam = new HashMap<String, Object>();
				listParam.put("blTypeMh", hold.getBlTypeMh());
				listParam.put("dataList", hold.getDataList());
				dataList = deliveryMapper.selectHoldDeliveryList(listParam);
			}else {
				dataList = hold.getDataList();
			}
		}

		if(dataList == null || dataList.size() == 0){
			return result;
		}

		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			DeliveryMapper deliveryBatchMapper = sqlSession.getMapper(DeliveryMapper.class);

			int updateCnt = 1;
			int seq = 1;
			for(Map<String, Object> holdData : dataList){
				String odJobNo = Util.null2str(holdData.get("odJobNo"));
				String multiOdJobNo = Util.null2str(holdData.get("multiOdJobNo"));
				boolean isMulti = veProfile && !"".equals(multiOdJobNo);

				Map<String, Object> statusParam = new HashMap<String, Object>();
				String currStatus;
				if(isMulti){
					statusParam.put("odJobNo", multiOdJobNo);
					statusParam.put("blNo", Util.null2str(holdData.get("deliveryNo")));
					currStatus = Util.null2str(deliveryMapper.selectHoldMultiStatus(statusParam));
				}else{
					statusParam.put("odJobNo", odJobNo);
					currStatus = Util.null2str(deliveryMapper.selectHoldSingleStatus(statusParam));
				}

				/** 배송요청대기가 아니면 건너뜀 */
				if(!("".equals(currStatus) || currStatus.compareTo("ECM340") < 0)){
					continue;
				}

				Map<String, Object> paramMap = new HashMap<String, Object>();
				paramMap.put("entCode", hold.getEntCode());
				paramMap.put("gmtTime", hold.getGmtTime());
				paramMap.put("registerUserId", hold.getRegisterUserId());
				paramMap.put("updateUserId", hold.getUpdateUserId());
				paramMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(6) + Util.setSeqNo(seq++));
				paramMap.put("deliveryCode", Util.null2str(holdData.get("deliveryCode")));

				if(isMulti){
					/** 멀티건 : TB_ECM_OD_MULTI.RETURN_DELIVERY_STATUS(seqNo 기준) 갱신 + TB_ECM_POD_04_M(OD_JOB_NO+BL_NO=DELIVERY_NO) INSERT */
					paramMap.put("odJobNo", multiOdJobNo);
					paramMap.put("seqNoId", holdData.get("seqNoId"));
					paramMap.put("blNo", Util.null2str(holdData.get("deliveryNo")));
					deliveryBatchMapper.holdDeliveryMulti(paramMap);
				}else{
					/** 단건 : TB_ECM_OD_ADDITIONAL.DELIVERY_STATUS 갱신 + TB_ECM_POD_04 INSERT */
					paramMap.put("odJobNo", odJobNo);
					paramMap.put("blNo", Util.null2str(holdData.get("hblNo")));
					deliveryBatchMapper.holdDeliverySingle(paramMap);
				}

				if((updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}

			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
		}catch(Exception e){
			throw e;
		}finally{
			if(sqlSession != null)	sqlSession.close();
		}

		// 처리 로그 기록
		String bound = "AI".equals(hold.getSrchBoundType()) ? "AIR" : "SEA";
		ProcessLogs processLogs = new ProcessLogs();
		processLogs.setEntCode(hold.getEntCode());
		processLogs.setEntBusinessCode(hold.getEntBusinessCode());
		processLogs.setSolution("ECM");
		processLogs.setBound(bound);
		processLogs.setBoundType("IMP");
		processLogs.setProcessUserId(hold.getUpdateUserId());
		processLogs.setJobNo("");
		processLogs.setLogJobType("DATA");
		processLogs.setDescription("HOLD DELIVERY");
		if("search".equals(action))	processLogs.setJobLog(new Gson().toJson(hold));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(hold.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(hold.getLogUserName());
		processLogs.setProcessUserNameEng(hold.getLogUserNameEng());
		processLogs.setJobFuncType("UPDATE");
		commonService.insertLog(processLogs, request);

		return result;
	}


	/**
	 * Delivery 각 배송사에 해당하는 데이터 조회 
	 * @param UpdateDelivery
	 * @return Map
	 * @throws Exception
	 */
	public Map<String, Object> selectSendDeliveryList(Map<String, Object> map, UpdateDelivery send) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> deliveryInterfaceInfo = null;
		Map<String, Object> commonDeliveryInterfaceInfo = null;
		List<Map<String,Object>> ems = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> korex = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> hanjin = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> lotte = new ArrayList<Map<String,Object>>();
		String customerCode = "";
		String logisticsCode = "";
		
		List<Map<String,Object>> errorList = new ArrayList<Map<String,Object>>();
		Map<String,Object> errorMap;
		String errorStr, errorResult, deliveryCode, consigneeAddress, shipperAddress;
		
		List<Map<String,Object>> deliveryList = deliveryMapper.selectSendDeliveryList(send);
		for(Map<String,Object> data : deliveryList) {
			if(!customerCode.equals(Util.null2str(data.get("AGENT_CODE"))) || !logisticsCode.equals(Util.null2str(data.get("DEST_PORT")))){
				customerCode = Util.null2str(data.get("AGENT_CODE"));
				logisticsCode = Util.null2str(data.get("DEST_PORT")); // 2025.01.03 목적항으로 집화점 조회
				map.put("srchCustomerCode", customerCode);
				map.put("srchLogisticsCode", logisticsCode);
				deliveryInterfaceInfo = deliveryMapper.selectDeliveryInterface(map);
				if(deliveryInterfaceInfo == null){
					/** 오더의 CUST_CODE_LV1코드로 등록된 TB_SMS_DELIVERY_COMP_INFO가 없을경우 COMMON으로 등록된 데이터 사용 */
					deliveryInterfaceInfo = new HashMap<String, Object>();
//					if(commonDeliveryInterfaceInfo == null){
						map.put("srchCustomerCode", "COMMON");
						commonDeliveryInterfaceInfo = deliveryMapper.selectDeliveryInterface(map);
						if(commonDeliveryInterfaceInfo == null){
							commonDeliveryInterfaceInfo = new HashMap<String, Object>();
						}
//					}
					deliveryInterfaceInfo.putAll(commonDeliveryInterfaceInfo);
				}
			}
			deliveryCode = Util.null2str(data.get("DELIVERY_CODE"));
			switch (deliveryCode) {
			case "EMS":
				data.put("EMS_ID", deliveryInterfaceInfo.get("EMS_ID"));
				ems.add(data);
				break;
			case "KOREX" :
				data.put("deliveryInterfaceInfo", deliveryInterfaceInfo);
				korex.add(data);
				break;
			case "HANJIN":
				data.put("deliveryInterfaceInfo", deliveryInterfaceInfo);
				hanjin.add(data);
				break;	
			case "LOTTE":
				data.put("deliveryInterfaceInfo", deliveryInterfaceInfo);
				lotte.add(data);
				break;
			}
			
			/** 필수값체크 */
			errorStr = "";
			errorResult = "";
			consigneeAddress = Util.null2str(data.get("consigneeAddressLocConvert")).replace("\r", " ").replace("\n", " ");
			shipperAddress = Util.null2str(deliveryInterfaceInfo.get("LOTTE_ADDR")).replace("\r", " ").replace("\n", " ");

			if(Util.null2str(deliveryCode).equals("KOREX") && Util.null2str(data.get("DELIVERY_NO")).length() > 20){
				errorStr += ", 멀티 운송장 번호";
				errorResult += ", " + Util.null2str(data.get("DELIVERY_NO"));
			}
			if(Util.null2str(data.get("consigneeNameLocConvert")).equals("")){
				errorStr += ", 수하인명";
			}
			if(Util.null2str(data.get("CONSIGNEE_ZIPCODE")).equals("")){
				errorStr += ", 수하인 우편번호";
			}
			if(Util.null2str(data.get("CONSIGNEE_TEL")).equals("")){
				errorStr += ", 수하인 전화번호";
			}
			if(deliveryCode.equals("EMS")){
				if(Util.null2str(data.get("commodityItem1")).equals("")){
					errorStr += ", 상품명";
				}
				if(consigneeAddress.equals("")){
					errorStr += ", 수하인 주소";
				}
			}else if(deliveryCode.equals("LOTTE")) {
				if(Util.null2str(data.get("CONSIGNEE_TEL")).equals("")){
					errorStr += ", 수하인 전화번호";
				}
				if(Util.null2str(data.get("commodityItemLocConvert")).equals("")){
					errorStr += ", 상품명";
				}
				if(consigneeAddress.equals("")){
					errorStr += ", 수하인 주소";
				}
//				if(Util.null2str(deliveryInterfaceInfo.get("LOTTE_NAME")).equals("")) {
				if(Util.null2str(data.get("shipperNameConvert")).equals("")){
					errorStr += ", 송하인명";
				}
				if(Util.null2str(deliveryInterfaceInfo.get("LOTTE_TEL")).equals("")){
					errorStr += ", 송하인 전화번호";
				}
				if(Util.null2str(deliveryInterfaceInfo.get("LOTTE_ZIPCODE")).equals("")){
					errorStr += ", 송하인 우편번호";
				}
				if(shipperAddress.equals("")){
					errorStr += ", 송하인 주소";
				}
			}else{
				if(Util.null2str(data.get("commodityItemLocConvert")).equals("")){
					errorStr += ", 상품명";
				}
				if(consigneeAddress.equals("") || (consigneeAddress.trim()).split(" ").length < 4){
					errorResult += ", " + consigneeAddress;
					errorStr += ", 수하인 주소";
				}
				if(deliveryCode.equals("KOREX")){
					if(Util.null2str(data.get("shipperNameConvert")).equals("")){
						errorStr += ", 송하인명";
					}
				}
			}
			
			if(!errorStr.equals("")){
				errorMap = new HashMap<String,Object>();
				errorMap.put("messageType", "필수값오류");
				errorMap.put("hsn", data.get("HSN"));
				errorMap.put("blNo", data.get("HBL_NO"));
				errorMap.put("errorStr", errorStr.substring(2));
				if(!errorResult.equals("")) errorMap.put("errorResult", errorResult.substring(2));
				errorList.add(errorMap);
				// 오류 건은 제외하고 전송하기 위해 리스트에서 삭제한다. - 2024.06.13
				data.clear();
				switch (deliveryCode) {
				case "EMS":
					ems.remove(ems.size()-1);
					break;
				case "KOREX" :
					korex.remove(korex.size()-1);
					break;
				case "HANJIN":
					hanjin.remove(hanjin.size()-1);
					break;	
				}
			}
		}
		
		returnMap.put("EMS", ems);
		returnMap.put("KOREX", korex);
		returnMap.put("HANJIN", hanjin);
		returnMap.put("LOTTE", lotte);
		
		if(errorList.size() > 0){
			returnMap.put("errorList", errorList);
		}
		
		return returnMap;
	}		
	
	/** 사용안함 */
	public void insertMasterPod(UpdateDelivery send) throws Exception {
		Map<String, Object> podMap = new HashMap<String, Object>();
		podMap.put("userLanguage", send.getUserLanguage());
		podMap.put("srchGroupCode", "STATUS_ORDER_CARGO");
		podMap.put("srchCode", "ECM340");
		podMap.put("entCode", send.getEntCode());
		podMap.put("entBusinessCode", "");
		podMap.put("gmtTime", send.getGmtTime());
		podMap.put("solution", "ECM");
		podMap.put("updateUserId", send.getUpdateUserId());
		podMap.put("stockType", "POD");
		podMap.put("stockSign", "POD");
		podMap.put("stockSeqLength", 8);
		podMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		podMap.put("stockRemark", "ECM POD JOB NO");
		podMap.put("podStatus", "ECM340");
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(podMap, "podTableName|gmtTime", false, null, null, null, null);
		List<Map<String, Object>> mblList = send.getDataList();
		for(Map<String, Object> mblData : mblList){
			podMap.put("odJobNo", mblData.get("OD_JOB_NO"));
			podMap.put("blNo", mblData.get("MBL_NO"));
			orderMapper.updateOrderLastPod(podMap);
			podMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5));
			orderMapper.insertOrderPod(podMap);
		}
	}
	
	/**
	 * Delivery 우체국 배송 요청
	 * @param UpdateDelivery
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> sendDeliveryEms(Map<String, Object> map, List<Map<String, Object>> deliveryListEms) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		Map<String, Object> errorMap = new HashMap<String, Object>();
		List<Map<String,Object>> errorList = new ArrayList<Map<String,Object>>();
		
		
		Pattern patternTel = Pattern.compile("^(01[016789]{1}|02|0[3-9]{1}[0-9]{1}|050[0-9])-?[0-9]{3,4}-?[0-9]{4}$");
		Matcher matcher = null;
		File folder = null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HHmmssSSS", Locale.KOREA);

		String dirEntCode =  Util.null2str(map.get("entCode")).toLowerCase();
		String sendYn = "N";
		String realPath = null;
		String hblno = null;
		String deliveryNo = "";
		String consigneeAddress = null;
		String failEmsHblNos = "";
		String failEmsTelHblNos = "";
		String failEmsAddrHblNos= "";
		String failEmsInterfaceHblNos = "";
		String resultStr = "D010";
		String todayTime = null;
		String emsId = "";
		String commonEmsId = null;
		String gwt = "0";
		String consigneeMobile = null;
		String consigneeTel = null;
		String dirPath = null;
		String fileName = null;
		String podJobNo = "";
		StringBuffer strBuffer = new StringBuffer();
		String logId = ""; 			// ES 로그용 아이디
		String index = ""; 			// ES 로그용 index
		String if_status = "";		// 처리상태값
		List<Map<String, Object>> logList = new ArrayList<Map<String, Object>>(); // 로그용 List
		int logIndex = 0;	// 로그용 index
		
		IndexApi indexApi = null;
		DocumentApi documentApi = null;
		
		String errorStr = "";
		String errorResult = "";

		int totalCnt = 0;
		int failedCnt = 0;
		int successedCnt = 0;
		int mailWght = 0;
		int mailVolm = 0;

		boolean checkMobile = false;
		boolean checkTel = false;

		String customerCode = "";
			
		// 우체국인 경우
		log.info("###### 우체국 파일 생성 시작##########");

		todayTime = formatter.format(new Date());

		//customProperties.getExAgentPath(); // C:/interface/exAgent/
		//customProperties.getExAgentSend(); // /send/ 
		// 로컬 경로 => C:/interface/exAgent/yhtxx/send/
		
		dirPath = customProperties.getExAgentPath() + dirEntCode + customProperties.getExAgentSend();
		fileName = "send_" + dirEntCode + "_" + todayTime + ".txt";

		
		// 파일생성 폴더 경로 지정
		folder = new File(dirPath);
		realPath = dirPath + fileName;

		// 파일생성 폴더 존재 유무 체크
		if (!folder.isDirectory()) {
			folder.mkdirs();
		}
		
		
		SqlSession sqlSession = null;

		// 파일 내용 쓰기
		try {
			
			int updateCnt = 1;
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			
			tmpMap.put("entCode", 		dirEntCode.toUpperCase());
			tmpMap.put("entBusinessCode", "");
			tmpMap.put("gmtTime", 		"0");
			tmpMap.put("solution", 		"ECM");
			tmpMap.put("stockType", 	"POD");
			tmpMap.put("stockSign", 	"POD");
			tmpMap.put("stockSeqLength","8");
			tmpMap.put("resultCode", 	"FIXED_SIGN_AND_SEQ_NO");
			tmpMap.put("stockRemark", 	"ECM POD JOB NO");
			tmpMap.put("podStatus", "ECM340");
			tmpMap.put("podTableName", 	"TB_ECM_POD_04");
			tmpMap.put("deliveryCode", 	"EMS");
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(tmpMap, "podTableName|gmtTime", false, null, null, null, null);
			
			strBuffer.setLength(0);
			totalCnt += deliveryListEms.size();
			
			List<Map<String, Object>> insertPodList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> updatePodList = new ArrayList<Map<String, Object>>();
			Map<String, Object> tmpInsertPodMap = null;
			int seq = 1;
			for (Map<String, Object> deliveryMapEms : deliveryListEms) {
				
				logIndex++;
				logList.add(deliveryMapEms);
				mailWght = 0;
				mailVolm = 0;
				errorStr = "";
				errorResult = "";

				hblno = Util.null2str(deliveryMapEms.get("HBL_NO"));
				gwt = Util.null2str(String.valueOf(deliveryMapEms.get("GWT")));
				consigneeMobile = Util.null2str(deliveryMapEms.get("CONSIGNEE_HP"));
				consigneeTel = Util.null2str(deliveryMapEms.get("CONSIGNEE_TEL"));
				consigneeAddress = Util.null2str(deliveryMapEms.get("consigneeAddressLocConvert")); //250바이트 이상일 때 오류 처리 
				
				// 멀티송장번호가 없다면 HBL NO를 넣는다.
				if(!"".equals(Util.null2str(deliveryMapEms.get("DELIVERY_NO")))) {
					deliveryNo = Util.null2str(deliveryMapEms.get("DELIVERY_NO"));
				}else {
					deliveryNo = hblno;
				}
				
				
				if ("".equals(gwt)) {
					gwt = "0";
				} else {
					mailWght = (int) (Float.parseFloat(gwt) * 1000);
				}

				if (mailWght <= 1000) {
					mailVolm = 50;
				} else if (mailWght <= 2000) {
					mailVolm = 60;
				} else if (mailWght <= 5000) {
					mailVolm = 80;
				} else if (mailWght <= 10000) {
					mailVolm = 120;
				} else if (mailWght <= 20000) {
					mailVolm = 140;
				} else if (mailWght <= 30000) {
					mailVolm = 160;
				}

				//값이 있을 때만 특수문자 체크! 없을 때는 true로 넘겨주기
				if (!"".equals(consigneeMobile)) {
					matcher = patternTel.matcher(consigneeMobile);

					if (matcher.find()) {
						checkMobile = true;
					} else {
						checkMobile = false;
					}
				} else {
					checkMobile = true;
				}

				if (!"".equals(consigneeTel)) {
					matcher = patternTel.matcher(consigneeTel);

					if (matcher.find()) {
						checkTel = true;
					} else {
						checkTel = false;
					}
				} else {
					checkTel = true;
				}
				
				//주소 250자 이상일 시 오류
				if(consigneeAddress.getBytes().length > 250) {
					logList.get(logIndex-1).put("sendStatus", "FAIL");
					failedCnt++;
					if ("".equals(failEmsAddrHblNos))
						failEmsAddrHblNos = hblno;
					else
						failEmsAddrHblNos += "," + hblno;
					
					errorMap = new HashMap<String, Object>();
					errorMap.put("messageType", "유효성오류");
					errorMap.put("hsn", deliveryMapEms.get("HSN"));
					errorMap.put("blNo", hblno);
					errorMap.put("errorStr", "[우체국]배송요청 주소 오류");
					errorMap.put("errorResult", consigneeAddress);
					errorList.add(errorMap);
					continue;
				}
				
				if(!"".equals(consigneeMobile) || !"".equals(consigneeTel)) { // 수하인전화번호, 핸드폰번호 유무 체크
					if (checkMobile && checkTel) {	
						//우체국 배송요청 각 항목 구분자 "|"
						successedCnt++;
						strBuffer.append("01").append("|"); // 1: 배송신청구분코드(01:신청,02:반품)
	//					strBuffer.append(emsId).append("|"); // 2: 업체구분코드
						strBuffer.append(Util.null2str(deliveryMapEms.get("EMS_ID"))).append("|"); // 2: 업체구분코드
						strBuffer.append(deliveryNo).append("|"); // 3.주문번호
						strBuffer.append(deliveryNo).append("|"); // 4.등기번호
						strBuffer.append(Util.fullWidthToHalfWidth(Util.null2str(deliveryMapEms.get("consigneeNameLocConvert")).trim().replaceAll("\\s", " "))).append("|"); // 5.수취인성명
						strBuffer.append(Util.null2str(deliveryMapEms.get("CONSIGNEE_TEL"))).append("|"); // 6.수취인전화번호
						strBuffer.append(Util.null2str(deliveryMapEms.get("CONSIGNEE_HP"))).append("|");  // 7.수취인기타전화번호
						strBuffer.append(Util.null2str(deliveryMapEms.get("CONSIGNEE_ZIPCODE"))).append("|"); // 8.수취인우편번호
						strBuffer.append(Util.null2str(deliveryMapEms.get("consigneeAddressLocConvert")).replaceAll("\\s", " ")).append("|");// 9.수취인주소
						strBuffer.append("|"); // 10.수취인상세주소
						strBuffer.append(Util.null2str(deliveryMapEms.get("consigneeNameLocConvert")).replaceAll("\\s", " ")).append("|"); // 11.주문자명
						strBuffer.append(Util.null2str(deliveryMapEms.get("CONSIGNEE_TEL"))).append("|"); // 12.주문자전화번호
						strBuffer.append(Util.null2str(deliveryMapEms.get("CONSIGNEE_HP"))).append("|"); // 13.주문자기타전화번호
						strBuffer.append(Util.null2str(deliveryMapEms.get("CONSIGNEE_ZIPCODE"))).append("|"); // 14.주문자우편번호
						strBuffer.append(Util.null2str(deliveryMapEms.get("consigneeAddressLocConvert")).replaceAll("\\s", " ")).append("|"); // 15.주문자주소
						strBuffer.append("|"); // 16.주문자상세주소
						strBuffer.append("|"); // 17.배송희망일자
						strBuffer.append("|"); // 18.배송메시지내역
						strBuffer.append("기타(일반)").append("|"); // 19.상품코드1
						strBuffer.append(Util.null2str(deliveryMapEms.get("commodityItem1")).replaceAll("\\s", " ")).append("|"); // 20.상품명1
						strBuffer.append("기타(일반)").append("|"); // 21.상품코드2
						strBuffer.append(Util.null2str(deliveryMapEms.get("commodityItem2")).replaceAll("\\s", " ")).append("|"); // 22.상품명2
						strBuffer.append("기타(일반)").append("|"); // 23.상품코드3
						strBuffer.append(Util.null2str(deliveryMapEms.get("commodityItem3")).replaceAll("\\s", " ")).append("|"); // 24.상품명3
						strBuffer.append("기타(일반)").append("|"); // 25.상품코드4
						strBuffer.append(Util.null2str(deliveryMapEms.get("commodityItem4")).replaceAll("\\s", " ")).append("|"); // 26.상품명4
						strBuffer.append("기타(일반)").append("|"); // 27.상품코드5
						strBuffer.append(Util.null2str(deliveryMapEms.get("commodityItem5")).replaceAll("\\s", " ")).append("|"); // 28.상품명5
						strBuffer.append(mailWght).append("|"); // 29.우편물중량
						strBuffer.append(mailVolm).append("|"); // 30.우편물크기
						strBuffer.append(Util.null2int(deliveryMapEms.get("PIECES"))).append("|"); // 31.상자수량
						strBuffer.append("N").append("|"); // 32.선불여부(N:선불, Y:착불, Default : N)
						strBuffer.append("|"); // 33.배송비
						strBuffer.append("|"); // 34.당일특급
						strBuffer.append("|"); // 35.자체운송장출력
						strBuffer.append("N").append("|"); // 36.초소형소포여부
						strBuffer.append("|"); // 37.맞교환여부
						strBuffer.append("|"); // 38.반품 원등기번호
						strBuffer.append("|"); // 39.맞교환대상등기번호
						strBuffer.append("|"); // 40.반품 원택배사명
						strBuffer.append("|"); // 41.안심소포 보험가액
						strBuffer.append("|"); // 42.포장재 코드
						strBuffer.append("\r\n");
	
						// 배송요청상태 변경
						deliveryMapEms.put("podStatus", "ECM340");
						deliveryMapEms.put("odJobNo",deliveryMapEms.get("OD_JOB_NO"));
						deliveryMapEms.put("noCheckLastPodStatus", "Y");
						
						
						orderBatchMapper.updateOrderLastPod(deliveryMapEms);
						
						tmpInsertPodMap = new HashMap<String, Object>();
						tmpInsertPodMap.put("odJobNo", deliveryMapEms.get("OD_JOB_NO"));
						tmpInsertPodMap.put("blNo", hblno);
						tmpInsertPodMap.put("updateUserId", deliveryMapEms.get("registerUserId"));
						if(deliveryMapEms.get("ECM340_POD_JOB_NO") != null){
							tmpInsertPodMap.put("podJobNo", deliveryMapEms.get("ECM340_POD_JOB_NO"));
							updatePodList.add(tmpInsertPodMap);
						}else{
							tmpInsertPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
							insertPodList.add(tmpInsertPodMap);
						}
						logList.get(logIndex-1).put("sendStatus", "SEND");
						
						//deliveryMapper.updateDeliveryStatus(deliveryMapEms); 
						
						if( (updateCnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
						
					} else {
						logList.get(logIndex-1).put("sendStatus", "FAIL");
						failedCnt++;
						if ("".equals(failEmsTelHblNos))
							failEmsTelHblNos = hblno;
						else
							failEmsTelHblNos += "," + hblno;
						
						errorStr = "[우체국]배송요청 모바일(전화) 특수문자 오류";
						errorResult = !"".equals(consigneeMobile) ? consigneeMobile : consigneeTel;
					}
				} else {
					logList.get(logIndex-1).put("sendStatus", "FAIL");
					failedCnt++;
					if ("".equals(failEmsHblNos))
						failEmsHblNos = hblno;
					else
						failEmsHblNos += "," + hblno;
					
					errorStr = "[우체국]배송요청 모바일(전화)오류";
				}
				
				if(!"".equals(errorStr)){
					errorMap = new HashMap<String, Object>();
					errorMap.put("messageType", "유효성오류");
					errorMap.put("hsn", deliveryMapEms.get("HSN"));
					errorMap.put("blNo", hblno);
					errorMap.put("errorStr", errorStr);
					errorMap.put("errorResult" , errorResult);
					errorList.add(errorMap);
				}
				
			} // for end
			
			updateCnt = 1;
			for(Map<String, Object> insertPodMap : insertPodList){
				tmpMap.put("odJobNo", insertPodMap.get("odJobNo"));
				tmpMap.put("blNo", insertPodMap.get("blNo"));
				tmpMap.put("updateUserId", insertPodMap.get("updateUserId"));
				tmpMap.put("podJobNo", insertPodMap.get("podJobNo"));
				orderBatchMapper.insertOrderPod(tmpMap);
				if( (updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			updateCnt = 1;
			for(Map<String, Object> updatePodMap : updatePodList){
				tmpMap.put("updateUserId", updatePodMap.get("updateUserId"));
				tmpMap.put("podJobNo", updatePodMap.get("podJobNo"));
				orderBatchMapper.updateOrderPod(tmpMap);
				if( (updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			/** 배송요청시 미집하 POD삭제 */
			tmpMap.put("podStatus", "ECM350");
			for(Map<String, Object> insertPodMap : insertPodList){
				tmpMap.put("odJobNo", insertPodMap.get("odJobNo"));
				orderBatchMapper.deleteOrderPod(tmpMap);
				if( (updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			for(Map<String, Object> updatePodMap : updatePodList){
				tmpMap.put("odJobNo", updatePodMap.get("odJobNo"));
				orderBatchMapper.deleteOrderPod(tmpMap);
				if( (updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}

			if (strBuffer.length() > 0) {
				FileWriter fw = new FileWriter(realPath);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(strBuffer.toString());
				bw.close();
				
				log.info("###### 우체국 파일 생성 완료 : " + realPath + "############");
			}
			
			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
			
		} catch (Exception e) {
			log.error(Util.getPrintStacTraceString(e));
			resultStr = "D030";
			logList.get(logIndex-1).put("sendStatus", "FAIL");
		} finally {
			sqlSession.close();
		}
			
		if ("D010".equals(resultStr)) { // 정상적으로 요청완료
			sendYn = "Y";
		} else {
			sendYn = resultStr;
		}
		
		//로그 insert
		for(Map<String,Object> logMap : logList) {
			
			// history 등록 
			tmpMap = new HashMap<String, Object>();
			logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
			index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
			
			// 처리결과에 따른 로그 기록 history
			tmpMap.clear();
			tmpMap.put("ent_code", dirEntCode.toUpperCase());
			tmpMap.put("customer_code", logMap.get("AGENT_CODE")); 		 // 고객코드
			tmpMap.put("solution", "ECM");      		 						 // SOLUTION
			tmpMap.put("bound", logMap.get("BOUND"));		 	 		// 바운드
			tmpMap.put("bound_type", logMap.get("BOUND_TYPE"));	 		// 바운드타입
			tmpMap.put("hbl_no", logMap.get("HBL_NO"));				 	 // HBL_NO
			tmpMap.put("if_service_code", "EMS");
			tmpMap.put("if_doc_code", "SEND_COURIER");
			tmpMap.put("if_doc_name", "택배사전송"); 			 			 // 문서분류명 (AIR ORDER, SEA ORDER)
			tmpMap.put("if_doc_reference_no", logMap.get("MBL_NO"));	 	 // 문서번호 
			tmpMap.put("if_vender", logMap.get("AGENT_CODE"));			// 중개사업자
			tmpMap.put("if_send_recv_type", "S");			 					 // 송수신타입(S:SEND, R:RECV)
			tmpMap.put("if_send_recv_method", "");		 				 		 // 송수신방법
			tmpMap.put("if_file_path", realPath);			 						 	 // 파일경로
			tmpMap.put("if_file_name", fileName);			 				 	 // 파일명
			tmpMap.put("if_status", logMap.get("sendStatus"));			 		 // 처리상태
			tmpMap.put("if_status_info", "");		 				 			 // 송수신상태_설명
			tmpMap.put("if_sender_id", logMap.get("AGENT_CODE"));									 // 송신식별자
			tmpMap.put("if_sender_name", "");		 							 // 송신자명
			tmpMap.put("if_receiver_id", "");		 							 // 수신식별자
			tmpMap.put("if_receiver_name", "");	     							 // 수신자명
			tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));				 // DATE
			tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));			 // DATE_TIME
			tmpMap.put("if_remark", "EMS");			 					 		 // 비고( 에러로그 등)
			tmpMap.put("register_user_id", logMap.get("registerUserId"));
			tmpMap.put("register_user_name", logMap.get("registerUserName"));
			tmpMap.put("register_date_time", System.currentTimeMillis());
			tmpMap.put("es_job_no", logId);

			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			
			documentApi = new DocumentApi(client);
			documentApi.createDocument(index, logId, tmpMap);	
			
		}
		
		returnMap.put("sendEmsYn", sendYn);
		returnMap.put("totalEmsCnt", totalCnt);
		returnMap.put("successedEmsCnt", successedCnt);
		returnMap.put("failEmsCnt", failedCnt);
		returnMap.put("failEmsHblNos", failEmsHblNos);
		returnMap.put("failEmsAddrHblNos", failEmsAddrHblNos);
		returnMap.put("failEmsInterfaceHblNos", failEmsInterfaceHblNos);
		returnMap.put("failEmsTelHblNos", failEmsTelHblNos);
		returnMap.put("errorListEms", errorList);
		return returnMap;
	}	
	

	

	/**
	 * Delivery 배송사 인터페이스 정보 조회
	 * @param UpdateDelivery
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> selectDeliveryInterface(Map<String, Object> map) throws Exception {
		return deliveryMapper.selectDeliveryInterface(map);
	}
	

	/**
	 * Delivery 배송 요청 후 상태값 업데이트 
	 * @param entCode
	 * @param deliveryMap
	 * @return 
	 * @throws Exception
	 */	
	public void updateDeliveryStatus(UpdateDelivery send, List<Map<String, Object>> odJobNoList) throws Exception {
		
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		Map<String, Object> dataMap = new HashMap<String, Object>();
		String podJobNo = "";
		
		SqlSession sqlSession = null;
		
		try {
		
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			
			int updateCnt = 1;
			
			// POD_JOB_NO 채번 
			tmpMap.put("entCode", 		send.getEntCode());
			tmpMap.put("gmtTime", 		send.getGmtTime());
			tmpMap.put("registerUserId",send.getRegisterUserId());
			tmpMap.put("updateUserId", 	send.getRegisterUserId());
			tmpMap.put("entBusinessCode", "");
			tmpMap.put("solution", 		"ECM");
			tmpMap.put("stockType", 	"POD");
			tmpMap.put("stockSign", 	"POD");
			tmpMap.put("stockSeqLength", "8");
			tmpMap.put("resultCode", 	"FIXED_SIGN_AND_SEQ_NO");
			tmpMap.put("stockRemark", 	"ECM POD JOB NO");
			tmpMap.put("podStatus", 	"ECM340");
			tmpMap.put("deliveryCode", send.getDeliveryCode());
			tmpMap.put("podTableName", 	"TB_ECM_POD_04");
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(tmpMap, "podTableName|gmtTime", false, null, null, null, null);
			
			List<Map<String, Object>> insertPodList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> updatePodList = new ArrayList<Map<String, Object>>();
			Map<String, Object> tmpInsertPodMap = null;
			int seq = 1;
			for (Map<String, Object> odJobNoMap : odJobNoList) {
				dataMap.put("odJobNo", odJobNoMap.get("odJobNo"));
				dataMap.put("podStatus", "ECM340");
				dataMap.put("gmtTime", send.getGmtTime());
				dataMap.put("noCheckLastPodStatus", "Y");
				orderBatchMapper.updateOrderLastPod(dataMap);
				
				tmpInsertPodMap = new HashMap<String, Object>();
				if(odJobNoMap.get("podJobNo") != null){
					tmpInsertPodMap.put("podJobNo", odJobNoMap.get("podJobNo"));
					updatePodList.add(tmpInsertPodMap);
				}else{
					tmpInsertPodMap.put("odJobNo", odJobNoMap.get("odJobNo"));
					tmpInsertPodMap.put("blNo", odJobNoMap.get("hblNo"));
					tmpInsertPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
					insertPodList.add(tmpInsertPodMap);
				}
				
				//deliveryMapper.updateDeliveryStatus(dataMap); 
				
				if( (updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			updateCnt = 1;
			for(Map<String, Object> insertPodMap : insertPodList){
				tmpMap.put("odJobNo", insertPodMap.get("odJobNo"));
				tmpMap.put("blNo", insertPodMap.get("blNo"));
				tmpMap.put("podJobNo", insertPodMap.get("podJobNo"));
				orderBatchMapper.insertOrderPod(tmpMap);
				if( (updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			updateCnt = 1;
			for(Map<String, Object> updatePodMap : updatePodList){
				tmpMap.put("podJobNo", updatePodMap.get("podJobNo"));
				orderBatchMapper.updateOrderPod(tmpMap);
				if( (updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			/** 배송요청시 미집하 POD삭제 */
			tmpMap.put("podStatus", "ECM350");
			for (Map<String, Object> odJobNoMap : odJobNoList) {
				tmpMap.put("odJobNo", odJobNoMap.get("odJobNo"));
				orderBatchMapper.deleteOrderPod(tmpMap);
				if( (updateCnt++ % 1000) == 0){
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
	}	
	
	/**
	 * Delivery 대한통운 배송요청(oracle) 
	 * @param entCode
	 * @param deliveryMap
	 * @return 
	 * @throws Exception
	 */	
	public Map<String, Object> sendDeliveryKorex(List<Map<String, Object>> deliveryListKorex) throws Exception {	
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> interfaceInfoMap;
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		Map<String, Object> errorMap = new HashMap<String, Object>();
		List<Map<String,Object>> errorList = new ArrayList<Map<String,Object>>();
		
		String sendYn = "N";   // 배송사 배송요청 완료 여부 
		String deliveryResult = null; // 배송사 배송요청 결과값
		String hblno = null; 
		String odJobNo = "";
		String consigneeAddress = null;
		String failKorexHblNos = "";  // 대한통운 배송요청 실패 하우스번호
		String failKorexAddrHblNos = ""; // 대한통운 배송요청 주소 오류로 인한 실패 하우스번호
		String failKorexInterfaceHblNos = "";  // 배송인터페이스정보오류 하우스번호
		String errorStr = "";
		String errorResult = "";
		
		String logId = ""; 			// ES 로그용 아이디
		String index = ""; 			// ES 로그용 index
		String if_status = "";		// 처리상태값
		
		IndexApi indexApi = null;
		DocumentApi documentApi = null;
		
		List<Map<String, Object>> successedKorexOdJobNos = new ArrayList<Map<String, Object>>(); // 대한 배송요청 성공 하우스번호
		Map<String, Object> successedKorexOdJobNo = null;
		
		int totalCnt = 0; 
		int successedCnt = 0;
		int failedCnt = 0;

		boolean checkKor = false;
		
		log.info("###### 대한통운 인터페이스 시작 ##########");
		totalCnt += deliveryListKorex.size();
		for (Map<String, Object> deliveryMapKorex : deliveryListKorex) {
			interfaceInfoMap = (Map<String, Object>)deliveryMapKorex.get("deliveryInterfaceInfo");
			checkKor = false;
			deliveryResult = "";
			errorStr = "";
			errorResult = "";

			hblno = Util.null2str(deliveryMapKorex.get("HBL_NO"));
			consigneeAddress = Util.null2str(deliveryMapKorex.get("consigneeAddressLocConvert")).replace("\r", " ").replace("\n", " ");
			odJobNo = Util.null2str(deliveryMapKorex.get("OD_JOB_NO"));

			// 한글 포함여부 체크
			checkKor = consigneeAddress.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");

			if (checkKor) {
				if (consigneeAddress.split(" ").length < 4) {
					if ("".equals(failKorexAddrHblNos))
						failKorexAddrHblNos = hblno;
					else
						failKorexAddrHblNos += "," + hblno;

					failedCnt++;
					deliveryResult = "N";
					if_status = "FAIL";
					errorStr = "[대한통운]배송요청 수하인주소 오류";
					errorResult = consigneeAddress;
				} else {
					deliveryResult = interfaceToDeliveryKorex(interfaceInfoMap, deliveryMapKorex);
					
					if ("D010".equals(deliveryResult)) { // 정상적으로 요청완료
						successedKorexOdJobNo = new HashMap<String, Object>();
						successedKorexOdJobNo.put("odJobNo", odJobNo);
						successedKorexOdJobNo.put("hblNo", hblno);
						successedKorexOdJobNo.put("podJobNo", deliveryMapKorex.get("ECM340_POD_JOB_NO"));
						successedKorexOdJobNos.add(successedKorexOdJobNo);
						successedCnt++;
						if_status = "SEND";
					} else if("D020".equals(deliveryResult)){	/** 배송사 인터페이스에 배송사 계정 정보 없음 */
						if ("".equals(failKorexInterfaceHblNos))
							failKorexInterfaceHblNos = hblno;
						else
							failKorexInterfaceHblNos += "," + hblno;

						failedCnt++;
						if_status = "FAIL";
						errorStr = "[대한통운]배송요청 interface정보 오류";
					} else if("D031".equals(deliveryResult)){	/** 수하인상세주소 200BYTE 초과 */
						if ("".equals(failKorexAddrHblNos))
							failKorexAddrHblNos = hblno;
						else
							failKorexAddrHblNos += "," + hblno;

						failedCnt++;
						if_status = "FAIL";
						errorStr = "[대한통운]배송요청 수하인주소 오류";
						errorResult = consigneeAddress;
					} else if("D035".equals(deliveryResult)){	/** 수하인 우편번호 6BYTE 초과 */
						if ("".equals(failKorexAddrHblNos))
							failKorexAddrHblNos = hblno;
						else
							failKorexAddrHblNos += "," + hblno;

						failedCnt++;
						if_status = "FAIL";
						errorStr = "[대한통운]배송요청 수하인 우편번호 오류";
						errorResult = consigneeAddress;
					} else if("D030".equals(deliveryResult)){	/** 대한통운 인터페이스 DB2DB Insert Error */
						if ("".equals(failKorexHblNos))
							failKorexHblNos = hblno;
						else
							failKorexHblNos += "," + hblno;

						failedCnt++;
						if_status = "FAIL";
						errorStr = "[대한통운]배송요청 오류";
					}
				}
			} else {
				if ("".equals(failKorexAddrHblNos))
					failKorexAddrHblNos = hblno;
				else
					failKorexAddrHblNos += "," + hblno;

				failedCnt++;
				deliveryResult = "D034";// 수하인 주소가 한글이 X
				errorStr = "[대한통운]배송요청 수하인주소 오류";
				errorResult = consigneeAddress;
			}
			
			if(!"".equals(errorStr)) {
				errorMap = new HashMap<String, Object>();
				errorMap.put("messageType", "유효성오류");
				errorMap.put("hsn", deliveryMapKorex.get("HSN"));
				errorMap.put("blNo", hblno);
				errorMap.put("errorStr", errorStr);
				errorMap.put("errorResult", errorResult);
				errorList.add(errorMap);
			}

			
			// history 등록 
			tmpMap = new HashMap<String, Object>();
			logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
			index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
			
			// 처리결과에 따른 로그 기록 history
			tmpMap.clear();
			tmpMap.put("ent_code", interfaceInfoMap.get("ENT_CODE"));
			tmpMap.put("customer_code", interfaceInfoMap.get("CUSTOMER_CODE")); 		 // 고객코드
			tmpMap.put("solution", "ECM");      		 						 // SOLUTION
			tmpMap.put("bound", deliveryMapKorex.get("BOUND"));		 	 		// 바운드
			tmpMap.put("bound_type", deliveryMapKorex.get("BOUND_TYPE"));	 		// 바운드타입
			tmpMap.put("hbl_no", deliveryMapKorex.get("HBL_NO"));				 	 // HBL_NO
			tmpMap.put("if_service_code", "KOREX");
			tmpMap.put("if_doc_code", "SEND_COURIER");
			tmpMap.put("if_doc_name", "택배사전송"); 			 			 // 문서분류명 (AIR ORDER, SEA ORDER)
			tmpMap.put("if_doc_reference_no", deliveryMapKorex.get("MBL_NO"));	 	 // 문서번호 
			tmpMap.put("if_vender", deliveryMapKorex.get("AGENT_CODE"));			// 중개사업자
			tmpMap.put("if_send_recv_type", "S");			 					 // 송수신타입(S:SEND, R:RECV)
			tmpMap.put("if_send_recv_method", "DB to DB");		 				 		 // 송수신방법
			tmpMap.put("if_file_path", "");			 						 	 // 파일경로
			tmpMap.put("if_file_name", "");			 				 	 // 파일명
			tmpMap.put("if_status", if_status);			 						 // 처리상태
			tmpMap.put("if_status_info", "");		 				 			 // 송수신상태_설명
			tmpMap.put("if_sender_id", deliveryMapKorex.get("AGENT_CODE"));									 // 송신식별자
			tmpMap.put("if_sender_name", "");		 							 // 송신자명
			tmpMap.put("if_receiver_id", "");		 							 // 수신식별자
			tmpMap.put("if_receiver_name", "");	     							 // 수신자명
			tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));				 // DATE
			tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));			 // DATE_TIME
			tmpMap.put("if_remark", "KOREX");			 					 		 // 비고( 에러로그 등)
			tmpMap.put("register_user_id", deliveryMapKorex.get("registerUserId"));
			tmpMap.put("register_user_name", deliveryMapKorex.get("registerUserName"));
			tmpMap.put("register_date_time", System.currentTimeMillis());
			tmpMap.put("es_job_no", logId);
			
			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			
			documentApi = new DocumentApi(client);
			documentApi.createDocument(index, logId, tmpMap);	
			
			if ("D010".equals(deliveryResult)) { // 정상적으로 요청완료
				//status = 1;
				sendYn = "Y";
			} else {
				sendYn = deliveryResult;
			}

		}
							

		log.info("###### 대한통운 인터페이스 종료 ##########");
		
		resultMap.put("sendKorexYn", sendYn);
		resultMap.put("totalKorexCnt", totalCnt);
		resultMap.put("successedKorexCnt", successedCnt);
		resultMap.put("successedKorexOdJobNos", successedKorexOdJobNos);
		resultMap.put("failKorexCnt", failedCnt);
		resultMap.put("failKorexHblNos", failKorexHblNos);
		resultMap.put("failKorexAddrHblNos", failKorexAddrHblNos);
		resultMap.put("failKorexInterfaceHblNos", failKorexInterfaceHblNos);
		resultMap.put("errorListKorex", errorList);
		
		return resultMap;
	}
	
	/**
	 * Delivery 한진택배 배송요청(oracle) 
	 * @param entCode
	 * @param deliveryMap
	 * @return 
	 * @throws Exception
	 */	
	public Map<String, Object> sendDeliveryHanjin(List<Map<String, Object>> deliveryListHanjin) throws Exception {	
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> interfaceInfoMap;
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		Map<String, Object> errorMap = new HashMap<String, Object>();
		List<Map<String,Object>> errorList = new ArrayList<Map<String,Object>>();
		
		String sendYn = "N";   // 배송사 배송요청 완료 여부 
		String deliveryResult = null; // 배송사 배송요청 결과값
		String hblno = null; 
		String odJobNo = "";
		String consigneeAddress = null;
		String failHanjinHblNos = "";	// 한진 배송요청 실패 하우스번호
		String failHanjinAddrHblNos = ""; // 한진 배송요청 주소 오류로 인한 실패 하우스번호
		String failHanjinInterfaceHblNos = ""; // 배송인터페이스정보오류 하우스번호
		String failHanjinNameHblNos = ""; // 수화인한글상호
		String failHanjinZipcodeHblNos = ""; // 수하인우편번호
		List<Map<String, Object>> successedHanjinOdJobNos = new ArrayList<Map<String, Object>>(); // 한진 배송요청 성공 하우스번호
		Map<String, Object> successedHanjinOdJobNo = null;
		
		int totalCnt = 0; 
		int successedCnt = 0;
		int failedCnt = 0;

		boolean checkKor = false;
		
		log.info("###### 한진택배 인터페이스 시작 ##########");
		sendYn = "N";
		totalCnt = 0;
		successedCnt = 0;
		failedCnt = 0;
		
		totalCnt += deliveryListHanjin.size();
		for (Map<String, Object> deliveryMapHanjin : deliveryListHanjin) {
			interfaceInfoMap = (Map<String, Object>)deliveryMapHanjin.get("deliveryInterfaceInfo");
			checkKor = false;
			deliveryResult = "";

			hblno = Util.null2str(deliveryMapHanjin.get("HBL_NO"));
			consigneeAddress = Util.null2str(deliveryMapHanjin.get("consigneeAddressLocConvert")).replace("\r", " ").replace("\n", " ");
			odJobNo = Util.null2str(deliveryMapHanjin.get("OD_JOB_NO"));
			
			String logId = ""; 			// ES 로그용 아이디
			String index = ""; 			// ES 로그용 index
			String if_status = "";		// 처리상태값
			String errorStr = "";
			String errorResult = "";
			
			IndexApi indexApi = null;
			DocumentApi documentApi = null;
			
			// 한글 포함여부 체크
			checkKor = consigneeAddress.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");

			if (checkKor) {
				if (consigneeAddress.split(" ").length < 4) {
					if ("".equals(failHanjinAddrHblNos))
						failHanjinAddrHblNos = hblno;
					else
						failHanjinAddrHblNos += "," + hblno;

					failedCnt++;
					deliveryResult = "N";
					if_status = "FAIL";
					errorStr = "[한진택배]배송요청 수하인주소 오류";
					errorResult = consigneeAddress;
				} else {
					deliveryResult = interfaceToDeliveryHanjin(interfaceInfoMap,deliveryMapHanjin);
					
					if ("Y".equals(deliveryResult)) {
						successedHanjinOdJobNo = new HashMap<String, Object>();
						successedHanjinOdJobNo.put("odJobNo", odJobNo);
						successedHanjinOdJobNo.put("hblNo", hblno);
						successedHanjinOdJobNo.put("podJobNo", deliveryMapHanjin.get("ECM340_POD_JOB_NO"));
						successedHanjinOdJobNos.add(successedHanjinOdJobNo);
						successedCnt++;
						if_status = "SEND";
					}else if("D020".equals(deliveryResult)){	/** 배송사 인터페이스에 배송사 계정 정보 없음 */
						if("".equals(failHanjinInterfaceHblNos)){
							failHanjinInterfaceHblNos = hblno;
						}else{
							failHanjinInterfaceHblNos += "," + hblno;
						}
						failedCnt++;
						if_status = "FAIL";
						errorStr = "[한진택배]배송요청 interface정보 오류";
					}else if("D032".equals(deliveryResult)){	/** 수하인 한글이름 존재X */
						if("".equals(failHanjinNameHblNos)){
							failHanjinNameHblNos = hblno;
						}else{
							failHanjinNameHblNos += "," + hblno;
						}
						failedCnt++;
						if_status = "FAIL";
						errorStr = "[한진택배]배송요청 수하인한글상호 오류";
						errorResult = Util.null2str(deliveryMapHanjin.get("consigneeNameLocConvert"));
					}else if("D033".equals(deliveryResult)){	/** 수하인 우편번호 존재X */
						if("".equals(failHanjinZipcodeHblNos)){
							failHanjinZipcodeHblNos = hblno;
						}else{
							failHanjinZipcodeHblNos += "," + hblno;
						}
						failedCnt++;
						if_status = "FAIL";
						errorStr = "[한진택배]배송요청 수하인우편번호 오류";
						errorResult = Util.null2str(deliveryMapHanjin.get("consigneeZipcodeLocConvert"));
					}else {
						if ("".equals(failHanjinHblNos))
							failHanjinHblNos = hblno;
						else
							failHanjinHblNos += "," + hblno;

						failedCnt++;
						if_status = "FAIL";
						errorStr = "[한진택배]배송요청 오류";
					}
				}
			} else {
				if ("".equals(failHanjinAddrHblNos))
					failHanjinAddrHblNos = hblno;
				else
					failHanjinAddrHblNos += "," + hblno;

				failedCnt++;
				// 한글 주소가 아님
				deliveryResult = "D034";// 수하인 주소가 한글이 X
				if_status = "FAIL";
				errorStr = "[한진택배]배송요청 수하인주소 오류";
				errorResult = consigneeAddress;
			}
			
			if(!"".equals(errorStr)) {
				errorMap = new HashMap<String, Object>();
				errorMap.put("messageType", "유효성오류");
				errorMap.put("hsn", deliveryMapHanjin.get("HSN"));
				errorMap.put("blNo", hblno);
				errorMap.put("errorStr", errorStr);
				errorMap.put("errorResult", errorResult);
				errorList.add(errorMap);
			}
			
			// history 등록 
			tmpMap = new HashMap<String, Object>();
			logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
			index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
			
			// 처리결과에 따른 로그 기록 history
			tmpMap.clear();
			tmpMap.put("ent_code", interfaceInfoMap.get("ENT_CODE"));
			tmpMap.put("customer_code", interfaceInfoMap.get("CUSTOMER_CODE")); 		 // 고객코드
			tmpMap.put("solution", "ECM");      		 						 // SOLUTION
			tmpMap.put("bound", deliveryMapHanjin.get("BOUND"));		 	 		// 바운드
			tmpMap.put("bound_type", deliveryMapHanjin.get("BOUND_TYPE"));	 		// 바운드타입
			tmpMap.put("hbl_no", deliveryMapHanjin.get("HBL_NO"));				 	 // HBL_NO
			tmpMap.put("if_service_code", "HANJIN");
			tmpMap.put("if_doc_code", "SEND_COURIER");
			tmpMap.put("if_doc_name", "택배사전송"); 			 			 // 문서분류명 (AIR ORDER, SEA ORDER)
			tmpMap.put("if_doc_reference_no", deliveryMapHanjin.get("MBL_NO"));	 	 // 문서번호 
			tmpMap.put("if_vender", deliveryMapHanjin.get("AGENT_CODE"));			// 중개사업자
			tmpMap.put("if_send_recv_type", "S");			 					 // 송수신타입(S:SEND, R:RECV)
			tmpMap.put("if_send_recv_method", "DB to DB");		 				 		 // 송수신방법
			tmpMap.put("if_file_path", "");			 						 	 // 파일경로
			tmpMap.put("if_file_name", "");			 				 	 // 파일명
			tmpMap.put("if_status", if_status);			 						 // 처리상태
			tmpMap.put("if_status_info", "");		 				 			 // 송수신상태_설명
			tmpMap.put("if_sender_id", deliveryMapHanjin.get("AGENT_CODE"));									 // 송신식별자
			tmpMap.put("if_sender_name", "");		 							 // 송신자명
			tmpMap.put("if_receiver_id", "");		 							 // 수신식별자
			tmpMap.put("if_receiver_name", "");	     							 // 수신자명
			tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));				 // DATE
			tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));			 // DATE_TIME
			tmpMap.put("if_remark", "HANJIN");			 					 		 // 비고( 에러로그 등)
			tmpMap.put("register_user_id", deliveryMapHanjin.get("registerUserId"));
			tmpMap.put("register_user_name", deliveryMapHanjin.get("registerUserName"));
			tmpMap.put("register_date_time", System.currentTimeMillis());
			tmpMap.put("es_job_no", logId);
			
			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			
			documentApi = new DocumentApi(client);
			documentApi.createDocument(index, logId, tmpMap);	

			if ("Y".equals(deliveryResult)) {
				sendYn = "Y";
			} else {
				sendYn = deliveryResult;
			}
	
		}

		
		log.info("###### 한진택배 인터페이스 종료 ##########");
		
		resultMap.put("sendHanjinYn", sendYn);
		resultMap.put("totalHanjinCnt", totalCnt);
		resultMap.put("successedHanjinCnt", successedCnt);
		resultMap.put("successedHanjinOdJobNos", successedHanjinOdJobNos);
		resultMap.put("failHanjinCnt", failedCnt);
		resultMap.put("failHanjinHblNos", failHanjinHblNos);
		resultMap.put("failHanjinAddrHblNos", failHanjinAddrHblNos);
		resultMap.put("failHanjinInterfaceHblNos", failHanjinInterfaceHblNos);
		resultMap.put("failHanjinNameHblNos", failHanjinNameHblNos);
		resultMap.put("failHanjinZipcodeHblNos", failHanjinZipcodeHblNos);
		resultMap.put("errorListHanjin", errorList);
		
		return resultMap;
	}
	
	/**
	 * 롯데택배 배송요청
	 * @param map
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> sendDeliveryLotte(Map<String, Object> map, List<Map<String, Object>> deliveryListLotte) throws Exception {

		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> interfaceInfoMap;
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		Map<String, Object> dataMap = new HashMap<String, Object>();
		Map<String, Object> sendMap = new HashMap<String, Object>();
		Map<String, Object> errorMap = new HashMap<String, Object>();
		List<Map<String,Object>> errorList = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> sndList = new ArrayList<Map<String, Object>>();

		String sendYn = "N";   // 배송사 배송요청 완료 여부
		String deliveryResult = null; // 배송사 배송요청 결과값
		String hblno = "";
		String odJobNo = "";
		String consigneeAddress = "";
		String failLotteHblNos = "";	// 롯데 배송요청 실패 하우스번호
		String failLotteAddrHblNos = ""; // 롯데 배송요청 주소 오류로 인한 실패 하우스번호
		String failLotteManHblNos = ""; // 롯데 배송요청 필수값 오류 하우스번호
		String failLotteInterfaceHblNos = ""; // 롯데 배송인터페이스정보오류 하우스번호
		String entCode = (String) map.get("entCode");
		String dirEntCode = entCode;

		String logId = ""; 			// ES 로그용 아이디
		String index = ""; 			// ES 로그용 index
		String if_status = "";		// 처리상태값
		List<Map<String, Object>> logList = new ArrayList<Map<String, Object>>(); // 로그용 List
		int logIndex = 0;	// 로그용 index
		
		IndexApi indexApi = null;
		DocumentApi documentApi = null;

		int status = 0;
		int totalCnt = 0;
		int failedCnt = 0;
		int successedCnt = 0;
		int sendCnt = 0;
		int oldSendCnt = 0;
		int tmpFailedCnt = 0;

		boolean checkKor = false;
		
		SqlSession sqlSession = null;
		
		try {
			
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			
			tmpMap.put("entCode", 		dirEntCode.toUpperCase());
			tmpMap.put("entBusinessCode", "");
			tmpMap.put("gmtTime", 		"0");
			tmpMap.put("solution", 		"ECM");
			tmpMap.put("stockType", 	"POD");
			tmpMap.put("stockSign", 	"POD");
			tmpMap.put("stockSeqLength","8");
			tmpMap.put("resultCode", 	"FIXED_SIGN_AND_SEQ_NO");
			tmpMap.put("stockRemark", 	"ECM POD JOB NO");
			tmpMap.put("podStatus", 	"ECM340");
			tmpMap.put("podTableName", 	"TB_ECM_POD_04");
			tmpMap.put("deliveryCode", 	"LOTTE");
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(tmpMap, "podTableName|gmtTime", false, null, null, null, null);
			
			log.info("###### 롯데택배 인터페이스 시작 ##########");
			sendYn = "N";
			totalCnt = 0;
			successedCnt = 0;
			failedCnt = 0;
			int executeCnt = 0;
			int updateCnt = 0;
			
			totalCnt += deliveryListLotte.size();
			
			List<Map<String, Object>> insertPodList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> updatePodList = new ArrayList<Map<String, Object>>();
			Set<String> donePodOdJobNoSet = new HashSet<String>();	// POD 처리 완료한 OD_JOB_NO (멀티 합포장건 중복 POD 방지)
			Map<String, Object> tmpInsertPodMap = null;
			int seq = 1;
			for (Map<String, Object> deliveryMapLotte : deliveryListLotte) {

				interfaceInfoMap = (Map<String, Object>)deliveryMapLotte.get("deliveryInterfaceInfo");
				checkKor = false;
				deliveryResult = "";

				// 멀티(합포장)건은 HBL_NO 대신 DELIVERY_NO(멀티 운송장번호)로 접수/처리
				// (맵의 HBL_NO값 자체를 치환하여 sendLotte 접수, POD blNo, 로그가 모두 멀티 송장번호를 쓰도록 함)
				if(!"".equals(Util.null2str(deliveryMapLotte.get("DELIVERY_NO")))) {
					deliveryMapLotte.put("HBL_NO", deliveryMapLotte.get("DELIVERY_NO"));
				}
				hblno = Util.null2str(deliveryMapLotte.get("HBL_NO"));

				consigneeAddress = Util.null2str(deliveryMapLotte.get("consigneeAddressLocConvert")).replace("\r", " ").replace("\n", " ");
				odJobNo = Util.null2str(deliveryMapLotte.get("OD_JOB_NO"));

				logIndex++;
				logList.add(deliveryMapLotte);
				String errorStr = "";
				String errorResult = "";
				
				// 한글 포함여부 체크
				checkKor = consigneeAddress.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
				status = 0;
//				result = "";
				
				if (checkKor) {// true 일때
					deliveryMapLotte.put("entCode", entCode);
					dataMap = sendLotte(deliveryMapLotte, interfaceInfoMap);
					
					deliveryResult = Util.null2str(dataMap.get("sendYn"));
					if ("Y".equals(deliveryResult)) { // 정상적으로 요청완료
						logList.get(logIndex-1).put("sendStatus", "SEND");
						sendCnt++;
						sndList.add(dataMap);
						tmpMap.put("MBL_NO", deliveryMapLotte.get("MBL_NO"));
					} else if (!"L20".equals(deliveryResult) && deliveryResult.startsWith("L")) { // 필수값 없음
						if ("".equals(failLotteManHblNos))
							failLotteManHblNos = hblno;
						else
							failLotteManHblNos += "," + hblno;
						
						logList.get(logIndex-1).put("sendStatus", "FAIL");
						failedCnt++;
						errorStr = "[롯데택배]배송요청 필수값 오류";
					} else if ("L20".equals(deliveryResult)) { // 배송사 인터페이스에 배송사 계정 정보 없음
						if ("".equals(failLotteInterfaceHblNos))
							failLotteInterfaceHblNos = hblno;
						else
							failLotteInterfaceHblNos += "," + hblno;

						logList.get(logIndex-1).put("sendStatus", "FAIL");
						failedCnt++;
						errorStr = "[롯데택배]배송요청 interface정보 오류";
					}
				} else {
					if ("".equals(failLotteAddrHblNos))
						failLotteAddrHblNos = hblno;
					else
						failLotteAddrHblNos += "," + hblno;

					logList.get(logIndex-1).put("sendStatus", "FAIL");
					failedCnt++;
					
//					result = "K50"; 		// E-WEB 결과값
					deliveryResult = "D034";// 수하인 주소가 한글이 X
					errorStr = "[롯데택배]배송요청 수하인주소 오류";
					errorResult = consigneeAddress;
				}
				

				if (!"Y".equals(deliveryResult)) {
					sendYn = deliveryResult;
				}
				
				if(!"".equals(errorStr)) {
					errorMap = new HashMap<String, Object>();
					errorMap.put("messageType", "유효성오류");
					errorMap.put("hsn", deliveryMapLotte.get("HSN"));
					errorMap.put("blNo", hblno);
					errorMap.put("errorStr", errorStr);
					errorMap.put("errorResult", errorResult);
					errorList.add(errorMap);
					continue;
				}
				
				if(sendCnt%2==0 || (sendCnt+failedCnt-tmpFailedCnt) == deliveryListLotte.size()) {
					log.info("################## API SEND : " + sendCnt + " ########################");
					String lotteUrl = customProperties.getLotteUrl();	// 롯데택배 API 택배신청 URL
					String IgtAk = (String) interfaceInfoMap.get("LOTTE_AUTH_KEY");
					String inputLine = "";
					
					HttpURLConnection conn = null;
					BufferedReader in = null;
					
					byte[] postDataBytes = null;
					
					URL url = null;
					try {
						url = new URL(lotteUrl);
					} catch (Exception e) {
						log.error(Util.getPrintStacTraceString(e));
						deliveryResult = "L00";
//						failLotteHblNos = hblno + " 롯데택배 API 연결 오류.";
//						failedCnt++;
						
						if ("".equals(failLotteHblNos))
							failLotteHblNos = hblno + (sndList.size()==1 ? "" : (" 외 " + (sndList.size()-1) + "건")) + " 롯데택배 API 연결 오류." + "\r\n";
						else
							failLotteHblNos += hblno + (sndList.size()==1 ? "" : (" 외 " + (sndList.size()-1) + "건")) + " 롯데택배 API 연결 오류." + "\r\n";
						
						failedCnt += sndList.size();
					}
					
					if(!deliveryResult.equals("L00")) {
						StringBuilder strBuild = new StringBuilder();
						
						try {
							sendMap.put("snd_list", sndList);
							
							String json = new Gson().toJson(sendMap);
							Charset charset = Charset.forName("UTF-8");
							postDataBytes = json.toString().getBytes(charset);
							
//							log.info("API >>>>>>>>>>>> " + json);
							
							conn = (HttpURLConnection)url.openConnection();
							conn.setConnectTimeout(20000);
							conn.setReadTimeout(20000);
							conn.setRequestMethod("POST");
							conn.setDoOutput(true); //항상 갱신된 내용 가져오도록 true로 설정
							conn.setRequestProperty("Content-Type", "application/json");
							conn.setRequestProperty("Authorization", "IgtAK " + IgtAk.replace("\r", "").replace("\n", ""));//인증키
							conn.getOutputStream().write(postDataBytes); // POST 호출
							
							in = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
				            
					        while((inputLine = in.readLine()) != null) { // response 출력
					        	strBuild.append(inputLine);
							}
					        
					        String response = strBuild.toString();
					        ObjectMapper objectMapper = new ObjectMapper();
					        ResultLotte rtnList = objectMapper.readValue(response, ResultLotte.class);
					        
					        for(int i=0; i<rtnList.getRtn_list().size(); i++){
					        	status = 0;
					        	deliveryResult = "";
					        	if("S".equals(Util.null2str(rtnList.getRtn_list().get(i).getRtnCd()))){
					        		successedCnt++;
					        		deliveryResult = "Y";
								}else {
									deliveryResult = Util.null2str(rtnList.getRtn_list().get(i).getRtnMsg());
									log.info("########## 롯데택배 배송 요청 실패! ##########");
							    	log.info("########## 롯데택배 HBLNo >>>> "+ Util.null2str(rtnList.getRtn_list().get(i).getInvNo()) + " ##########");
							    	log.info("########## 롯데택배 rtnMsg >>>> "+ Util.null2str(rtnList.getRtn_list().get(i).getRtnMsg()) + " ##########");
								}
					        	
					        	if ("Y".equals(deliveryResult)) { 
									status = 1;
								}
					        	// 배송요청상태 변경
								if (status > 0) {
									executeCnt++;
									// LAST POD 업데이트
									tmpMap.put("noCheckLastPodStatus", "Y");
									tmpMap.put("odJobNo", Util.null2str(rtnList.getRtn_list().get(i).getOrdNo()));

									orderBatchMapper.updateOrderLastPod(tmpMap);
									
									String podOdJobNo = Util.null2str(rtnList.getRtn_list().get(i).getOrdNo());
									// 멀티(합포장)건은 같은 OD_JOB_NO로 여러 건 접수되므로, POD는 오더당 1건만 처리 (중복 방지)
									if(!donePodOdJobNoSet.contains(podOdJobNo)) {
										tmpInsertPodMap = new HashMap<String, Object>();
										tmpInsertPodMap.put("odJobNo", podOdJobNo);
										tmpInsertPodMap.put("blNo", Util.null2str(rtnList.getRtn_list().get(i).getInvNo()));
										
										for(Map<String , Object> tmpDeliveryMapLotte : deliveryListLotte) {
											String tmpOdJobNo = Util.null2str(tmpDeliveryMapLotte.get("OD_JOB_NO"));
											if(tmpOdJobNo.equals(podOdJobNo)) {
												tmpInsertPodMap.put("updateUserId", tmpDeliveryMapLotte.get("registerUserId"));
												if(tmpDeliveryMapLotte.get("ECM340_POD_JOB_NO") != null){
													tmpInsertPodMap.put("podJobNo", tmpDeliveryMapLotte.get("ECM340_POD_JOB_NO"));
													updatePodList.add(tmpInsertPodMap);
												}else{
													tmpInsertPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
													insertPodList.add(tmpInsertPodMap);
												}
												donePodOdJobNoSet.add(podOdJobNo);	// 이 오더는 POD 처리 완료
												break;
											}
										}
									}
									if(executeCnt % 1000 == 0){
										sqlSession.flushStatements();
										sqlSession.clearCache();
									}
									sendYn = "Y";
									logList.get(oldSendCnt+i).put("sendStatus", "SEND");
								}else {
									if ("".equals(failLotteHblNos))
										failLotteHblNos = Util.null2str(rtnList.getRtn_list().get(i).getInvNo()) + " " + deliveryResult + "\r\n";
									else
										failLotteHblNos += Util.null2str(rtnList.getRtn_list().get(i).getInvNo()) + " " + deliveryResult + "\r\n";

									failedCnt++;
									tmpFailedCnt++;
							    	errorStr = "[롯데택배]배송요청 오류";
							    	logList.get(oldSendCnt+i).put("sendStatus", "FAIL");
								}
					        }
					        
					        if(in != null) in.close();
					        if(conn != null) conn.disconnect();
						}catch (SocketTimeoutException e) {
							log.info("########## 롯데택배 배송 요청 [ERROR]["+ e.getMessage()  +" Error[connect timed out-5 second over] ##########");
							log.error(Util.getPrintStacTraceString(e));
						} catch (Exception e) {
							log.error(Util.getPrintStacTraceString(e));
//							deliveryResult = "K84";	// E-WEB 결과값
							deliveryResult = "D030";
						}finally { 
							if(conn != null) conn.disconnect();
						}
					}
					sndList = new ArrayList<Map<String, Object>>();
					oldSendCnt = sendCnt;
				}
				
				
			} // for end
			
			updateCnt = 1;
			for(Map<String, Object> insertPodMap : insertPodList){
				tmpMap.put("odJobNo", insertPodMap.get("odJobNo"));
				tmpMap.put("blNo", insertPodMap.get("blNo"));
				tmpMap.put("updateUserId", insertPodMap.get("updateUserId"));
				tmpMap.put("podJobNo", insertPodMap.get("podJobNo"));
				orderBatchMapper.insertOrderPod(tmpMap);
				if( (updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			updateCnt = 1;
			for(Map<String, Object> updatePodMap : updatePodList){
				tmpMap.put("updateUserId", updatePodMap.get("updateUserId"));
				tmpMap.put("podJobNo", updatePodMap.get("podJobNo"));
				orderBatchMapper.updateOrderPod(tmpMap);
				if( (updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			/** 배송요청시 미집하 POD삭제 */
			tmpMap.put("podStatus", "ECM350");
			for(Map<String, Object> insertPodMap : insertPodList){
				tmpMap.put("odJobNo", insertPodMap.get("odJobNo"));
				orderBatchMapper.deleteOrderPod(tmpMap);
				if( (updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			for(Map<String, Object> updatePodMap : updatePodList){
				tmpMap.put("odJobNo", updatePodMap.get("odJobNo"));
				orderBatchMapper.deleteOrderPod(tmpMap);
				if( (updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
			
		} catch (Exception e) {
			log.error(Util.getPrintStacTraceString(e));
		} finally {
			sqlSession.close();
		}
		
		if ("Y".equals(deliveryResult)) { // 정상적으로 요청완료
			sendYn = "Y";
		} else {
			sendYn = deliveryResult;
		}
		
		//로그 insert
		for(Map<String,Object> logMap : logList) {
			
			// history 등록 
			tmpMap = new HashMap<String, Object>();
			logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
			index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
			
			// 처리결과에 따른 로그 기록 history
			tmpMap.clear();
			tmpMap.put("ent_code", dirEntCode.toUpperCase());
			tmpMap.put("customer_code", logMap.get("AGENT_CODE")); 		 // 고객코드
			tmpMap.put("solution", "ECM");      		 						 // SOLUTION
			tmpMap.put("bound", logMap.get("BOUND"));		 	 		// 바운드
			tmpMap.put("bound_type", logMap.get("BOUND_TYPE"));	 		// 바운드타입
			tmpMap.put("hbl_no", logMap.get("HBL_NO"));				 	 // HBL_NO
			tmpMap.put("if_service_code", "LOTTE");
			tmpMap.put("if_doc_code", "SEND_COURIER");
			tmpMap.put("if_doc_name", "택배사전송"); 			 			 // 문서분류명 (AIR ORDER, SEA ORDER)
			tmpMap.put("if_doc_reference_no", logMap.get("MBL_NO"));	 	 // 문서번호 
			tmpMap.put("if_vender", logMap.get("AGENT_CODE"));			// 중개사업자
			tmpMap.put("if_send_recv_type", "S");			 					 // 송수신타입(S:SEND, R:RECV)
			tmpMap.put("if_send_recv_method", "API");		 				 	 // 송수신방법
			tmpMap.put("if_file_path", "");			 						 	 // 파일경로
			tmpMap.put("if_file_name", "");			 				 	 // 파일명
			tmpMap.put("if_status", logMap.get("sendStatus"));			 		 // 처리상태
			tmpMap.put("if_status_info", "");		 				 			 // 송수신상태_설명
			tmpMap.put("if_sender_id", logMap.get("AGENT_CODE"));									 // 송신식별자
			tmpMap.put("if_sender_name", "");		 							 // 송신자명
			tmpMap.put("if_receiver_id", "");		 							 // 수신식별자
			tmpMap.put("if_receiver_name", "");	     							 // 수신자명
			tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));				 // DATE
			tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));			 // DATE_TIME
			tmpMap.put("if_remark", "LOTTE");			 					 		 // 비고( 에러로그 등)
			tmpMap.put("register_user_id", logMap.get("registerUserId"));
			tmpMap.put("register_user_name", logMap.get("registerUserName"));
			tmpMap.put("register_date_time", System.currentTimeMillis());
			tmpMap.put("es_job_no", logId);

			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			
			documentApi = new DocumentApi(client);
			documentApi.createDocument(index, logId, tmpMap);	
			
		}
		
		returnMap.put("sendLotteYn", sendYn);
		returnMap.put("totalLotteCnt", totalCnt);
		returnMap.put("successedLotteCnt", successedCnt);
		returnMap.put("failLotteCnt", failedCnt);
		returnMap.put("failLotteHblNos", failLotteHblNos);
		returnMap.put("failLotteAddrHblNos", failLotteAddrHblNos);
		returnMap.put("failLotteManHblNos", failLotteManHblNos);
		returnMap.put("failLotteInterfaceHblNos", failLotteInterfaceHblNos);
		returnMap.put("errorListLotte", errorList);
		return returnMap;
	}
	
	/**
	 * 롯데택배 매핑 및 필수값 검증
	 * @param map
	 * @param deliveryInfo
	 * @param req
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> sendLotte(Map<String, Object> map, Map<String, Object> deliveryInfo) throws Exception {
	
		Map<String, Object> deliveryMap = new HashMap<String, Object>();
		Map<String, Object> sendMap = new HashMap<String, Object>();
		List<Map<String, Object>> sndList = new ArrayList<Map<String, Object>>();
		
		String sendYn = "N";
		String result = "L10";
		
		//Request
		String jobCustCd = Util.null2str(deliveryInfo.get("LOTTE_CUST_ID"));  //거래처코드
		String ustRtgSctCd = "01"; //출고반품구분 (01:출고 02:반품)
		String ordSct = "1"; //오더구분 (1:일반 2:교환 3:AS)
		String fareSctCd = ""; //운임구분
		String ordNo = "";    //주문번호
		String invNo = "";    //운송장번호
		String orglInvNo = ""; //원송장번호
		String snperNm = "";   //송하인명      
		String snperTel = "";   //송하인 전화번호
		String snperCpno = ""; //송하인 휴대전화번호
		String snperZipcd = ""; //송하인 우편번호
		String snperAdr = ""; //송하인 주소
		String acperNm = "";   //수하인명                
		String acperTel = "";   //수하인 전화번호         
		String acperCpno = "";   //수하인 휴대전화번호
		String acperZipcd = ""; //수하인 우편번호
		String acperAdr = ""; //수하인 주소
		String boxTypCd = "A"; //박스크기  A, B, C, D, E
		int sumFare = 0; //현/착불 기본운임
		String gdsNm = ""; //상품명
		String dlvMsgCont = "";//배달메세지내용
		String cusMsgCont = "";//고객메세지내용
		String pickReqYmd = "";//집하요청일
		String bdpkSctCd = "N";//합포장여부 (Y/N)
		String bdpkkey = "";//합포장 KEY
		String bdpkRpnSeq = "";//합포장순번
		String midKey = DateTime.getDate("yyyyMMddHHmmss");
		
		StringBuilder strBuild = new StringBuilder();
		
		if ("".equals(jobCustCd)) {
			deliveryMap.put("sendYn", "L20"); 	// 거래처코드 없음
			return deliveryMap;
		}
		
		if (map != null && jobCustCd != null) {
			log.info("########## 롯데택배 인터페이스 Start ##########");
			
			//합포장KEY 대한통운 합포장KEY 동일 처리 요청
			bdpkkey = midKey.replaceAll("-", "") + "_" + jobCustCd + "_" + Util.null2str(map.get("OD_JOB_NO"))
			+ "_" + Util.null2str(map.get("HBL_NO"));
			
			//매핑 및 필수값 체크
			
			// 운임구분 (01:선불, 02:착불, 03:신용)
			if (!"".equals(Util.null2str(map.get("DELIVERY_PAYMENT_TYPE")))) {
				fareSctCd = Util.null2str(map.get("DELIVERY_PAYMENT_TYPE"));
				switch(fareSctCd) {
					case "01" : 				  break; // 현불(선불)
					case "08" : fareSctCd = "02"; break; // 착불
					case "07" :	fareSctCd = "03"; break; // 신용
				}
			} else {
				fareSctCd =  "03";
			}
			
			// 오더정보
			ordNo = Util.null2str(map.get("OD_JOB_NO"));
			invNo = Util.null2str(map.get("HBL_NO"));
			if("".equals(invNo)) {deliveryMap.put("sendYn", "L83 운송장번호"); return deliveryMap; }
			
			// 송하인 정보
//			snperNm = Util.null2str(deliveryInfo.get("LOTTE_NAME"));
			//롯데택배 배송접수 시 송하인명만 하우스의 shipperName으로 전송하도록 수정
			snperNm = Util.null2str(map.get("shipperNameConvert"));
			if("".equals(snperNm)) {deliveryMap.put("sendYn", "L84 송하인명"); return deliveryMap; }
			snperTel = Util.null2str(deliveryInfo.get("LOTTE_TEL"));
			if("".equals(snperTel)) {deliveryMap.put("sendYn", "L85 송하인전화번호"); return deliveryMap; }
			snperCpno = Util.null2str(deliveryInfo.get("LOTTE_MOBILE"));
			snperZipcd = Util.null2str(deliveryInfo.get("LOTTE_ZIPCODE"));
			if("".equals(snperZipcd)) {deliveryMap.put("sendYn", "L86 송하인우편번호"); return deliveryMap; }
			snperAdr = Util.null2str(deliveryInfo.get("LOTTE_ADDR"));
			if("".equals(snperAdr)) {deliveryMap.put("sendYn", "L87 송하인주소"); return deliveryMap; }
					
			// 수하인 정보
			acperNm = Util.null2str(map.get("consigneeNameLocConvert"));
			if("".equals(acperNm)) {deliveryMap.put("sendYn", "L88 수하인명"); return deliveryMap; }
			acperTel = Util.null2str(map.get("CONSIGNEE_TEL"));
			if("".equals(acperTel)) {deliveryMap.put("sendYn", "L89 수하인전화번호"); return deliveryMap; }
			acperCpno = Util.null2str(map.get("CONSIGNEE_HP"));
			acperZipcd = Util.null2str(map.get("CONSIGNEE_ZIPCODE"));
			if("".equals(acperZipcd)) {deliveryMap.put("sendYn", "L90 수하인 우편번호"); return deliveryMap; }
			acperAdr = Util.fullWidthToHalfWidth(Util.null2str(map.get("consigneeAddressLocConvert")));
			if("".equals(acperAdr)) {deliveryMap.put("sendYn", "L91 수하인주소"); return deliveryMap; }
			
			//박스타입
			if(!"".equals(Util.null2str(deliveryInfo.get("LOTTE_BOX_TYPE")))) {
				boxTypCd = Util.null2str(deliveryInfo.get("LOTTE_BOX_TYPE"));
			}else {
				boxTypCd = "A";
			}
			if("".equals(boxTypCd)) {deliveryMap.put("sendYn", "L92 박스타입"); return deliveryMap; }
			
			//상품명
			gdsNm = Util.null2str(map.get("commodityItemLocConvert"));
			if("".equals(gdsNm)) {deliveryMap.put("sendYn", "L93 상품명"); return deliveryMap; }
			
			//메세지내용
			dlvMsgCont = Util.null2str(map.get("DELIVERY_NOTE"));
//			cusMsgCont = Util.null2str(map.get("DeliveryRemark"));
			
			
			deliveryMap.put("jobCustCd", jobCustCd);  //거래처코드
			deliveryMap.put("ustRtgSctCd", ustRtgSctCd);  //출고반품구분 (01:출고 02:반품)
			deliveryMap.put("ordSct", ordSct);  //오더구분 (1:일반 2:교환 3:AS)
			deliveryMap.put("fareSctCd", fareSctCd);  //운임구분
			deliveryMap.put("ordNo", ordNo);  //주문번호
			deliveryMap.put("invNo", invNo);  //운송장번호 
			deliveryMap.put("orglInvNo", orglInvNo);  //원송장번호
			deliveryMap.put("snperNm", snperNm);  //송하인명
			deliveryMap.put("snperTel", snperTel);  //송하인 전화번호
			deliveryMap.put("snperCpno", snperCpno);  //송하인 휴대전화번호
			deliveryMap.put("snperZipcd", snperZipcd);  //송하인 우편번호
			deliveryMap.put("snperAdr", snperAdr);  //송하인 주소
			deliveryMap.put("acperNm", acperNm); // 수하인명
			deliveryMap.put("acperTel", acperTel);  //수하인 전화번호 
			deliveryMap.put("acperCpno", acperCpno);  //수하인 휴대전화번호
			deliveryMap.put("acperZipcd", acperZipcd);  //수하인 우편번호
			deliveryMap.put("acperAdr", acperAdr);  //수하인 주소
			deliveryMap.put("boxTypCd", boxTypCd);  //박스크기  A, B, C, D, E
			deliveryMap.put("sumFare", sumFare);  //현/착불 기본운임
			deliveryMap.put("gdsNm", gdsNm);  //상품명
			deliveryMap.put("dlvMsgCont", dlvMsgCont);  //배달메세지내용
			deliveryMap.put("cusMsgCont", cusMsgCont);  //고객메세지내용
			deliveryMap.put("pickReqYmd", pickReqYmd);  //집하요청일
			deliveryMap.put("bdpkSctCd", bdpkSctCd);  //합포장여부 (Y/N)
			deliveryMap.put("bdpkkey", bdpkkey);  //합포장 KEY
			deliveryMap.put("bdpkRpnSeq", bdpkRpnSeq);  //합포장순번
			deliveryMap.put("sendYn", "Y");
		}
		
		return deliveryMap;
	}
	
	/**
	 * Delivery 대한통운 배송요청 처리(DB To DB)
	 * @param infoMap
	 * @param dataMap
	 * @return 
	 * @throws Exception
	 */
	public String interfaceToDeliveryKorex(Map<String, Object> interfaceInfoMap, Map<String, Object> dataMap) throws Exception {
		Map<String, Object> deliveryMap = new HashMap<String, Object>();

		String mapckKey = "";
		String priKey = "";
		String resultStr = "D010";
		String today = DateTime.getDate("yyyyMMdd");
		String midKey = DateTime.getDate("yyyyMMddHHmmss");
		String custId = ""; // 대한통운 고객ID
		String subCustId = ""; // 대한통운 고객 관리 업체코드
		String senderAddress = "";
		String senderAddressDtl = "";
		String senderZipcode = "";
		String senderInfoUseYn = "N";
		String shipperZipCode = "";
		String rcvrNm = "";
		String rcvrTel = "";
		String rcvrCell = "";
		String rcvrZipCode = "";
		String rcvrAddressLocal = "";
		String consigneeAddr = "";
		String consigneeAddrDtl = "";
		String shipperTel = "";
		String shipperCell = "";
		String boxTypeCd = "01";
		
		String[] consigneeArr = null;
		String[] senderTel = new String[3];
		String[] telNo = new String[3];
		String[] cellNo = new String[3];

		// 대한통운 택사 이용 정보
		custId = Util.null2str(interfaceInfoMap.get("KOREX_ID"));
		subCustId = Util.null2str(interfaceInfoMap.get("KOREX_CUST_ID"));
		senderTel[0] = Util.null2str(interfaceInfoMap.get("SENDER_TELNO1"));
		senderTel[1] = Util.null2str(interfaceInfoMap.get("SENDER_TELNO2"));
		senderTel[2] = Util.null2str(interfaceInfoMap.get("SENDER_TELNO3"));
		senderAddress = Util.null2str(interfaceInfoMap.get("SENDER_ADDR"));
		senderAddressDtl = Util.null2str(interfaceInfoMap.get("SENDER_ADDR_DTL"));
		senderZipcode = Util.null2str(interfaceInfoMap.get("SENDER_ZIPCODE"));
		senderInfoUseYn = Util.null2str(interfaceInfoMap.get("SENDER_INFO_USEYN")); // 반송정보 유무
		
		if ("".equals(custId)) {
			return resultStr = "D020"; // 배송사 인터페이스에 배송사 계정 정보 없음
		}

		
		if (dataMap != null) {
			log.info("###### 대한통운 인터페이스 DB2DB Start ##########");
			
			if(Util.null2str(dataMap.get("DELIVERY_NO")).equals("")) {
				mapckKey = midKey.replaceAll("-", "") + "_" + custId + "_" + Util.null2str(dataMap.get("OD_JOB_NO"))+ "_" + Util.null2str(dataMap.get("HBL_NO"));
			} else {
				mapckKey = midKey.replaceAll("-", "") + "_" + custId + "_" + Util.null2str(dataMap.get("OD_JOB_NO"))+ "_" + Util.null2str(dataMap.get("DELIVERY_NO"));
			}

			// 등록확인
			priKey = Util.null2str(deliveryMapper.selectKorexDeliveryOrderPK(mapckKey));
			
			if (priKey == null || "".equals(priKey)) { // TB_RCPT_YHT010 테이블에 동일한 MPCK_KEY가 존재하지 않을때 접수 가능 
				
				// DeliveryBoxType 컬럼이 존재하지 않아서 기본 01로 고정 (박스타입구분 01:극소, 02:소, 03:중, 04:대, 05:특대)
				boxTypeCd = "01";

				deliveryMap.put("custId", custId); // 고객ID
				deliveryMap.put("rcptYmd", today.replaceAll("-", "")); // 접수일자
				deliveryMap.put("custUseNo", (Util.null2str(dataMap.get("BOUND")).equals("AIR") ? "A_" : "O_" ) + Util.null2str(dataMap.get("OD_JOB_NO"))); // 고객상용번호
				deliveryMap.put("rcptDv", "01"); // 접수구분 (01 : 일반)
				deliveryMap.put("workDvCd", "01"); // 작업구분 (01 : 일반)
				deliveryMap.put("reqDvCd", "01"); // 요청구분코드 (01:요청)
				deliveryMap.put("mpckKey", mapckKey); // YYYYMMDDHHmmss_고객ID_RefNo
				deliveryMap.put("mpckSeq", 1);
				deliveryMap.put("calDvCd", "01"); // 정산구분(01:계약운임, 02:자료운임)

				// 운임구분 (01:선불, 02:착불, 03:신용)
				if (!"".equals(Util.null2str(dataMap.get("DELIVERY_PAYMENT_TYPE")))) {
//					deliveryMap.put("deliveryPaymentType", dataMap.get("DELIVERY_PAYMENT_TYPE"));
					switch((String)dataMap.get("DELIVERY_PAYMENT_TYPE")) {
					case "01" : deliveryMap.put("deliveryPaymentType", "01"); break;	// 선불
					case "02" : deliveryMap.put("deliveryPaymentType", "02"); break;	// 후불
					case "08" : deliveryMap.put("deliveryPaymentType", "02"); break;	// 착불
					case "07" : deliveryMap.put("deliveryPaymentType", "03"); break;	// 신용
					}
				} else {
					deliveryMap.put("deliveryPaymentType", "03");
				}
				
				
				deliveryMap.put("cntrItemCd", "01"); // 계약품목 코드 01:일반품목
				deliveryMap.put("boxTypeCd", boxTypeCd); // 박스타입구분 01:극소, 02:소, 03:중, 04:대, 05:특대
				deliveryMap.put("boxQty", 1); // 박스수량 StringUtill.null2dbl(dataMap.get("Pieces"))
				deliveryMap.put("frt", 0); // 운임

				if (!"".equals(subCustId)) {
					deliveryMap.put("custMgmtDlcmCd", subCustId); // 고객관리 거래처코드
				} else {
					deliveryMap.put("custMgmtDlcmCd", custId); // 고객관리 거래처코드
				}

				// 송화인 
				deliveryMap.put("sendrNm", Util.null2str(dataMap.get("shipperNameConvert"))); // 송화인명
				shipperTel = Util.null2str(dataMap.get("shipperTelConvert"));
				shipperCell = Util.null2str(dataMap.get("shipperMobileConvert"));
				
				if ("Y".equals(senderInfoUseYn)) {
					deliveryMap.put("sendrTelNo1", senderTel[0]); // 송화인 전화번호1
					deliveryMap.put("sendrTelNo2", senderTel[1]); // 송화인 전화번호2
					deliveryMap.put("sendrTelNo3", senderTel[2]); // 송화인 전화번호3 *암호화
					deliveryMap.put("sendrCellNo1", senderTel[0]); // 송화인 휴대폰번호1
					deliveryMap.put("sendrCellNo2", senderTel[1]); // 송화인 휴대폰번호2
					deliveryMap.put("sendrCellNo3", senderTel[2]); // 송화인 휴대폰번호3 *대한통운 DB에서 암호화
					deliveryMap.put("sendrZipNo", senderZipcode); // 송화인 우편번호
					deliveryMap.put("sendrAddr", senderAddress); // 송화인 주소
					deliveryMap.put("sendrDetailAddr", senderAddressDtl); // 송화인 상세주소
				} else {
					shipperTel = shipperTel.replaceAll("\\/", "").replaceAll("\\+", "").replaceAll("-", "");
					shipperCell = shipperCell.replaceAll("\\/", "").replaceAll("\\+", "").replaceAll("-", "");

					if (!"".equals(shipperTel)) {
						if (shipperTel.length() == 10) {
							telNo[0] = shipperTel.substring(0, 3);
							telNo[1] = shipperTel.substring(3, 6);
							telNo[2] = shipperTel.substring(6, 10);
						} else if (shipperTel.length() == 11) {
							telNo[0] = shipperTel.substring(0, 3);
							telNo[1] = shipperTel.substring(3, 7);
							telNo[2] = shipperTel.substring(7, 11);
						} else if (shipperTel.length() == 9) {
							telNo[0] = shipperTel.substring(0, 2);
							telNo[1] = shipperTel.substring(2, 5);
							telNo[2] = shipperTel.substring(5, 9);
						} else {
							// 10,11 자리 외
							telNo[0] = "000";
							telNo[1] = "0000";
							telNo[2] = "0000";
						}
					} else if (!"".equals(shipperCell)) {
						if (shipperCell.length() == 10) {
							telNo[0] = shipperCell.substring(0, 3);
							telNo[1] = shipperCell.substring(3, 6);
							telNo[2] = shipperCell.substring(6, 10);
						} else if (shipperCell.length() == 11) {
							telNo[0] = shipperCell.substring(0, 3);
							telNo[1] = shipperCell.substring(3, 7);
							telNo[2] = shipperCell.substring(7, 11);
						} else {
							// 10,11 자리 외
							telNo[0] = "000";
							telNo[1] = "0000";
							telNo[2] = "0000";
						}
					} else {
						telNo[0] = "000";
						telNo[1] = "0000";
						telNo[2] = "0000";
					}

					
					
					deliveryMap.put("sendrTelNo1", telNo[0]); // 송화인 전화번호1
					deliveryMap.put("sendrTelNo2", telNo[1]); // 송화인 전화번호2
					deliveryMap.put("sendrTelNo3", telNo[2]); // 송화인 전화번호3 *대한통운 DB에서 암호화
					

					// 휴대폰번호 있으면
					if (!"".equals(shipperCell)) {
						if (shipperCell.length() == 10) {
							cellNo[0] = shipperCell.substring(0, 3);
							cellNo[1] = shipperCell.substring(3, 6);
							cellNo[2] = shipperCell.substring(6, 10);
						} else if (shipperCell.length() == 11) {
							cellNo[0] = shipperCell.substring(0, 3);
							cellNo[1] = shipperCell.substring(3, 7);
							cellNo[2] = shipperCell.substring(7, 11);
						} else {
							// 10,11 자리 외
							cellNo[0] = "000";
							cellNo[1] = "0000";
							cellNo[2] = "0000";
						}
					} else {
						cellNo[0] = "000";
						cellNo[1] = "0000";
						cellNo[2] = "0000";
					}

					deliveryMap.put("sendrCellNo1", cellNo[0]); // 송화인 휴대폰번호1
					deliveryMap.put("sendrCellNo2", cellNo[1]); // 송화인 휴대폰번호2
					deliveryMap.put("sendrCellNo3", cellNo[2]); // 송화인 휴대폰번호3 *대한통운 DB에서 암호화

					shipperZipCode = Util.null2str(dataMap.get("shipperZipcodeConvert"));
					shipperZipCode = "".equals(shipperZipCode) ? "00000" : shipperZipCode;
					
					deliveryMap.put("sendrZipNo", shipperZipCode.replaceAll("-", "")); // 송화인 우편번호
					deliveryMap.put("sendrAddr", Util.null2str(dataMap.get("shipperAddressLocConvert"))); // 송화인
					deliveryMap.put("sendrDetailAddr", "()"); // 송화인 상세주소 없음
				}

				// 수화인
				rcvrNm = Util.null2str(dataMap.get("consigneeNameLocConvert"));
				rcvrNm = "".equals(rcvrNm) ? " " : rcvrNm;
				deliveryMap.put("rcvrNm", rcvrNm); // 수화인명

				rcvrTel = Util.null2str(dataMap.get("consigneeTelConvert"));
				rcvrCell = Util.null2str(dataMap.get("consigneeHpConvert"));

				telNo = new String[3];
				cellNo = new String[3];

				rcvrTel = rcvrTel.replaceAll("\\/", "").replaceAll("\\+", "").replaceAll("-", "");
				rcvrCell = rcvrCell.replaceAll("\\/", "").replaceAll("\\+", "").replaceAll("-", "");

				if (!"".equals(rcvrTel)) {
					if (rcvrTel.length() == 9) {
						telNo[0] = rcvrTel.substring(0, 2);
						telNo[1] = rcvrTel.substring(2, 5);
						telNo[2] = rcvrTel.substring(5, 9);
					} else if (rcvrTel.length() == 10) {
						telNo[0] = rcvrTel.substring(0, 3);
						telNo[1] = rcvrTel.substring(3, 6);
						telNo[2] = rcvrTel.substring(6, 10);
					} else if (rcvrTel.length() == 11) {
						telNo[0] = rcvrTel.substring(0, 3);
						telNo[1] = rcvrTel.substring(3, 7);
						telNo[2] = rcvrTel.substring(7, 11);
					} else if (rcvrTel.length() == 12) {
						telNo[0] = rcvrTel.substring(0, 4);
						telNo[1] = rcvrTel.substring(4, 8);
						telNo[2] = rcvrTel.substring(8, 12);
					} else {
						// 9,10,11,12 자리 외
						telNo[0] = "000";
						telNo[1] = "0000";
						telNo[2] = "0000";
					}
				} else if (!"".equals(rcvrCell)) {
					if (rcvrCell.length() == 9) {
						telNo[0] = rcvrCell.substring(0, 2);
						telNo[1] = rcvrCell.substring(2, 5);
						telNo[2] = rcvrCell.substring(5, 9);
					} else if (rcvrCell.length() == 10) {
						telNo[0] = rcvrCell.substring(0, 3);
						telNo[1] = rcvrCell.substring(3, 6);
						telNo[2] = rcvrCell.substring(6, 10);
					} else if (rcvrCell.length() == 11) {
						telNo[0] = rcvrCell.substring(0, 3);
						telNo[1] = rcvrCell.substring(3, 7);
						telNo[2] = rcvrCell.substring(7, 11);
					} else if (rcvrCell.length() == 12) {
						telNo[0] = rcvrCell.substring(0, 4);
						telNo[1] = rcvrCell.substring(4, 8);
						telNo[2] = rcvrCell.substring(8, 12);
					} else {
						// 9,10,11,12 자리 외
						telNo[0] = "000";
						telNo[1] = "0000";
						telNo[2] = "0000";
					}
				} else {
					telNo[0] = "000";
					telNo[1] = "0000";
					telNo[2] = "0000";
				}
				deliveryMap.put("rcvrTelNo1", telNo[0]); // 수화인 전화번호1
				deliveryMap.put("rcvrTelNo2", telNo[1]); // 수화인 전화번호2
				deliveryMap.put("rcvrTelNo3", telNo[2]); // 수화인 전화번호3 *대한통운 DB에서 암호화

				// 휴대폰번호 있으면
				if (!"".equals(rcvrCell)) {
					if (rcvrCell.length() == 9) {
						cellNo[0] = rcvrCell.substring(0, 2);
						cellNo[1] = rcvrCell.substring(2, 5);
						cellNo[2] = rcvrCell.substring(5, 9);
					} else if (rcvrCell.length() == 10) {
						cellNo[0] = rcvrCell.substring(0, 3);
						cellNo[1] = rcvrCell.substring(3, 6);
						cellNo[2] = rcvrCell.substring(6, 10);
					} else if (rcvrCell.length() == 11) {
						cellNo[0] = rcvrCell.substring(0, 3);
						cellNo[1] = rcvrCell.substring(3, 7);
						cellNo[2] = rcvrCell.substring(7, 11);
					} else if (rcvrCell.length() == 12) {
						cellNo[0] = rcvrCell.substring(0, 4);
						cellNo[1] = rcvrCell.substring(4, 8);
						cellNo[2] = rcvrCell.substring(8, 12);
					} else {
						// 9,10,11,12 자리 외
						cellNo[0] = "000";
						cellNo[1] = "0000";
						cellNo[2] = "0000";
					}
				} else {
					cellNo[0] = "000";
					cellNo[1] = "0000";
					cellNo[2] = "0000";
				}

				deliveryMap.put("rcvrCellNo1", cellNo[0]); // 수화인 핸드폰번호1
				deliveryMap.put("rcvrCellNo2", cellNo[1]); // 수화인 핸드폰번호2
				deliveryMap.put("rcvrCellNo3", cellNo[2]); // 수화인 핸드폰번호3 *대한통운 DB에서 암호화

				rcvrZipCode = Util.null2str(dataMap.get("consigneeZipcodeLocConvert"));
				rcvrAddressLocal = Util.fullWidthToHalfWidth(Util.null2str(dataMap.get("consigneeAddressLocConvert")));
				rcvrZipCode = "".equals(rcvrZipCode) ? "00000" : rcvrZipCode;
				rcvrAddressLocal = "".equals(rcvrAddressLocal) ? "N/A" : rcvrAddressLocal;
				if(rcvrZipCode.replaceAll("-", "").length() > 6) return "D035"; // 수하인 우편번호 6BYTE 초과
				else deliveryMap.put("rcvrZipNo", rcvrZipCode.replaceAll("-", "")); // 수화인 우편번호


				consigneeArr = rcvrAddressLocal.split(" "); // 구 혹은 길 로 잘라 표시하기 위해
				if(consigneeArr.length > 2) {
					consigneeAddr = consigneeArr[0] + " " + consigneeArr[1] + " " + consigneeArr[2];
				}
				consigneeAddrDtl = "";

				// 나머지 주소를 합치며 " "각 배열마다 공백처리
				for (int i = 3; i < consigneeArr.length; i++) {
					consigneeAddrDtl += " " + consigneeArr[i] + " ";
				}
				// 오라클은 공백삽입 불가능 하여 상세주소가 빈값일 때, 주소 마지막 글자를 잘라 가공 후 삽입
				if(Util.null2str(consigneeAddrDtl) == null || ("").equals(Util.null2str(consigneeAddrDtl))) {
					consigneeAddrDtl += consigneeAddr.substring(consigneeAddr.length()-1, consigneeAddr.length());
					consigneeAddr = consigneeAddr.substring(0, consigneeAddr.length()-1);
				}else if (consigneeAddrDtl.getBytes().length > 200) {
					return "D031"; // 수하인상세주소 200BYTE 초과
				}else if(consigneeAddr.getBytes().length > 100) {
					return "D031"; // 수하인주소 100BYTE 초과 
				}
				deliveryMap.put("rcvrAddr", consigneeAddr); // 수화인 주소
				deliveryMap.put("rcvrDetailAddr", consigneeAddrDtl.trim()); // 수화인 상세주소
				
				// 운송장번호(송장번호 존재하면 DELIVERY_NO, 존재하지 않으면 HBL_NO)
				if(Util.null2str(dataMap.get("DELIVERY_NO")).equals("")) deliveryMap.put("invcNo", Util.null2str(dataMap.get("HBL_NO")));
				else deliveryMap.put("invcNo", Util.null2str(dataMap.get("DELIVERY_NO")));
				
				deliveryMap.put("prtSt", "02"); // 출력상태 01:미출력, 02:선출력, 03:선발번
				deliveryMap.put("remark1", Util.null2str(dataMap.get("DELIVERY_NOTE"))); // 배송요청사항

				String commodityItem = Util.null2str(dataMap.get("commodityItemLocConvert"));
				commodityItem = "".equals(commodityItem) ? " " : commodityItem;
				deliveryMap.put("gdsNm", commodityItem); // 상품명
				deliveryMap.put("gdsQty", Util.null2int(dataMap.get("PIECES")));
				deliveryMap.put("dlvDv", "01"); // 택배구분 01:택배, 02:중량물(설치물류), 03:중량물(비설치물류)
				deliveryMap.put("rcptErrYn", "N"); // 접수에러여부
				deliveryMap.put("eaiPrgsSt", "01"); // EAI 전송 상태 default: 01
				deliveryMap.put("regEmpId", "YHT"); // 등록ID 고정
				deliveryMap.put("modiEmpId", "YHT"); // 수정ID 고정

				log.info("###### 대한통운 인터페이스 DB2DB Insert ##########");
				try {
					deliveryMapper.insertKorexDeliveryOrderDB2DB(deliveryMap);
				} catch (Exception e) {
					log.error(Util.getPrintStacTraceString(e));
					log.info("###### 대한통운 인터페이스 DB2DB Insert Error ##########");
					resultStr = "D030";
					throw new Exception(e.getMessage());
				}
			}
		}
		
		log.info("###### 대한통운 인터페이스 DB2DB End ##########");
		
		return resultStr;
	
	}	

	/**
	 * Delivery 한진택배 배송요청 처리(DB To DB)
	 * @param infoMap
	 * @param dataMap
	 * @return 
	 * @throws Exception
	 */
	public String interfaceToDeliveryHanjin(Map<String, Object> interfaceInfoMap, Map<String, Object> dataMap) throws Exception {
		Map<String, Object> deliveryMap = new HashMap<String, Object>();
		
		String resultStr = "N";
		String ediCod = "ZENITH";  //EDI코드(DB접속계정) 고정, 추후 한진 사용 업체 추가되면 수정 필요
		String dlvDiv = "S";   //배송구분 S:자체출력 고정                
		String cusNum = Util.null2str(interfaceInfoMap.get("HANJIN_CUS_NUM"));   //신용번호 (테스트 신용번호 : 9117159) 고정 
		String picYmd = DateTime.getDate("yyyyMMdd");   //상품출고일(집하요청일자)
		String sndZip = Util.null2str(interfaceInfoMap.get("HANJIN_ZIPCODE"));   //송하인 우편번호         
		String sndAd1 = Util.null2str(interfaceInfoMap.get("HANJIN_ADDR1"));   //송하인 주소            
		String sndAd2 = Util.null2str(interfaceInfoMap.get("HANJIN_ADDR2"));   //송하인 상세주소            
		String sndNam = Util.null2str(interfaceInfoMap.get("HANJIN_NAME"));   //송하인명                
		String sndTel = Util.null2str(interfaceInfoMap.get("HANJIN_TEL"));   //송하인 전화번호         
		String sndHph = Util.null2str(interfaceInfoMap.get("HANJIN_MOBILE"));   //송하인 핸드폰번호
		String sndCrg = "";   //송하인 담당자명
		String rcvZip = "";   //수하인 우편번호         
		String rcvAd1 = "";   //수하인 주소            
		String rcvAd2 = "";   //수하인 상세주소            
		String rcvNam = "";   //수하인명                
		String rcvTel = "";   //수하인 전화번호         
		String rcvHph = "";   //수하인 핸드폰번호
		String rcvCrg = "";   //수하인 담당자
		String dlvMsg = "";   //배송메세지
		String godNam = "";   //상품명      
		int boxQty = Util.null2int(dataMap.get("Pieces"));
		String payCon = "CD";  //지불조건, CD(발지신용) CT(착지신용) PP(선불) CC(착불)                 
		String boxTyp = "S";   //박스타입, S(60cm이하), A(80cm 이하), B(100cm이하), C(120cm이하), D(160cm 이하) 
		String memNo1 = "";
		String memNo2 = "";
		String memNo3 = "";
		String memNo4 = "";
		String O_YN = "Y";    //성공여부 Y/N
		String O_SQLCODE = null;   //에러코드
		String O_SQLERRM = null;    //에러메시지
		String[] shipperArr = null;
		
		if(cusNum.equals("")){
			return "D020";	/** 배송사 인터페이스에 배송사 계정 정보 없음 */
		}
		
		if (dataMap != null) {
			log.info("###### 한진택배 인터페이스 DB2DB Start ##########");
			
			// 송하인 정보
			sndTel = sndTel.replaceAll("\\/", "").replaceAll("\\+", "").replaceAll("-", "");
			sndHph = sndHph.replaceAll("\\/", "").replaceAll("\\+", "").replaceAll("-", "");
			
			// 수하인 정보
			rcvNam = Util.null2str(dataMap.get("consigneeNameLocConvert"));
			if("".equals(rcvNam)) {
				return resultStr = "D032"; // 수하인 한글이름 존재X
			}
			
			
			rcvZip = Util.null2str(dataMap.get("consigneeZipcodeLocConvert"));
			if("".equals(rcvZip)) {
				return resultStr = "D033"; // 수하인 우편번호 존재 X
			}
			
			String rcvrAddressLocal = Util.fullWidthToHalfWidth(Util.null2str(dataMap.get("consigneeAddressLocConvert")));
			shipperArr = rcvrAddressLocal.split(" "); // 구 혹은 길 로 잘라 표시하기 위해
			if(shipperArr.length > 2) {
				rcvAd1 = shipperArr[0] + " " + shipperArr[1] + " " + shipperArr[2];
				rcvAd2 = "";
				
				// 나머지 주소를 합치며 " "각 배열마다 공백처리
				for (int i = 3; i < shipperArr.length; i++) {
					rcvAd2 += " " + shipperArr[i] + " ";
				}
			}
			
			rcvTel = Util.null2str(dataMap.get("consigneeTelConvert"));
			rcvHph = Util.null2str(dataMap.get("consigneeHpConvert"));
			
			//상품명
			godNam = Util.null2str(dataMap.get("commodityItemLocConvert"));
			/*
			 * if("".equals(godNam)) { return resultStr = "K83"; }
			 */
			godNam = "".equals(godNam) ? " " : godNam;
			
			String logVal = "godNam >>>>>>"+godNam;
			log.info(logVal);
			
			dlvMsg = Util.null2str(dataMap.get("DeliveryRemark")); // DeliveryRemark ? 
			memNo1 = Util.null2str(dataMap.get("DELIVERY_NOTE")); // DeliveryRequest ?  
			
			deliveryMap.put("ediCod", ediCod);  //EDI코드(DB접속계정) - not null
			deliveryMap.put("ordNum", Util.null2str(dataMap.get("OD_JOB_NO")));  //주문번호 - not null
			deliveryMap.put("wblNum", Util.null2str(dataMap.get("HBL_NO")));  //운송장번호 
			deliveryMap.put("dlvDiv", dlvDiv);  //배송구분, S:자체출력 - not null
			deliveryMap.put("cusNum", cusNum);  //신용번호 - not null
			deliveryMap.put("picYmd", picYmd);  //상품출고일(집하요청일자) - not null
			deliveryMap.put("sndZip", sndZip);  //송하인 우편번호 - not null 
			deliveryMap.put("sndAd1", sndAd1);  //송하인 주소1(동이상 주소)
			deliveryMap.put("sndAd2", sndAd2);  //송하인 주소2(동이하 주소) 
			deliveryMap.put("sndNam", sndNam);  //송하인명 - not null
			deliveryMap.put("sndTel", sndTel);  //송하인 전화번호
			deliveryMap.put("sndHph", sndHph);  //송하인 핸드폰번호
			deliveryMap.put("sndCrg", sndCrg);  //송하인 담당자명
			deliveryMap.put("rcvZip", rcvZip);  //수하인 우편번호 - not null
			deliveryMap.put("rcvAd1", rcvAd1);  //수하인 주소1(동이상 주소)
			deliveryMap.put("rcvAd2", rcvAd2.trim());  //수하인 주소2(동이하 주소)
			deliveryMap.put("rcvNam", rcvNam);  //수하인명 - not null
			deliveryMap.put("rcvTel", rcvTel);  //수하인 전화번호
			deliveryMap.put("rcvHph", rcvHph);  //수하인 핸드폰번호
			deliveryMap.put("rcvCrg", rcvCrg);  //수하인 담당자명
			deliveryMap.put("dlvMsg", dlvMsg);  //배송메세지
			deliveryMap.put("godNam", godNam);  //상품명 - not null
			deliveryMap.put("boxQty", boxQty);  //박스수량 - not null
			deliveryMap.put("payCon", payCon);  //지불조건, CD:발신지용   - not null              
			deliveryMap.put("boxTyp", boxTyp);  //박스타입, S:60cm 이하 - not null
			deliveryMap.put("memNo1", memNo1);  //배송 요청 사항
			deliveryMap.put("memNo2", memNo2);  //배송 요청 사항
			deliveryMap.put("memNo3", memNo3);  //배송 요청 사항
			deliveryMap.put("memNo4", memNo4);  //배송 요청 사항
			
			
			log.info("###### 한진택배 인터페이스 DB2DB Insert ##########");
			try {
				deliveryMapper.insertHanjinDeliveryOrderDB2DB(deliveryMap);
				
				O_YN = Util.null2str(deliveryMap.get("O_YN")); //insert 요청 성공 여부 (Y, N)
				O_SQLCODE =  Util.null2str(deliveryMap.get("O_SQLCODE")); // 에러코드
				O_SQLERRM =	 Util.null2str(deliveryMap.get("O_SQLERRM")); // 에러 메시지
				
			    if("Y".equals(O_YN)) {
			        resultStr = "Y"; 
			    }else{
			    	resultStr = "N";
			    	log.info("###### 한진택배 인터페이스 DB2DB Insert Error ##########");
			    	log.info("에러코드 >>>>> "+O_SQLCODE);
			    	log.info("에러메시지 >>>>>> "+O_SQLERRM);
			    }

			} catch (Exception e) {
				log.error(Util.getPrintStacTraceString(e));
				resultStr = "D030";
			}
		}
		
		return resultStr;

	}

	/**
	 * Delivery 우체국 반품 요청
	 * @param UpdateDelivery
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> sendDeliveryEmsReturn(Map<String, Object> map, List<Map<String, Object>> deliveryListEms) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		Map<String, Object> errorMap = new HashMap<String, Object>();
		List<Map<String,Object>> errorList = new ArrayList<Map<String,Object>>();


		Pattern patternTel = Pattern.compile("^(01[016789]{1}|02|0[3-9]{1}[0-9]{1}|050[0-9])-?[0-9]{3,4}-?[0-9]{4}$");
		Matcher matcher = null;
		File folder = null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HHmmssSSS", Locale.KOREA);

		String dirEntCode =  Util.null2str(map.get("entCode")).toLowerCase();
		String sendYn = "N";
		String realPath = null;
		String hblno = null;
		String deliveryNo = "";
		String consigneeAddress = null;
		String failEmsHblNos = "";
		String failEmsTelHblNos = "";
		String failEmsAddrHblNos= "";
		String failEmsInterfaceHblNos = "";
		String resultStr = "D010";
		String todayTime = null;
		String emsId = "";
		String seqNoId = "";
		String commonEmsId = null;
		String gwt = "0";
		String consigneeMobile = null;
		String consigneeTel = null;
		String dirPath = null;
		String fileName = null;
		String podJobNo = "";
		StringBuffer strBuffer = new StringBuffer();
		String logId = ""; 			// ES 로그용 아이디
		String index = ""; 			// ES 로그용 index
		String if_status = "";		// 처리상태값
		List<Map<String, Object>> logList = new ArrayList<Map<String, Object>>(); // 로그용 List
		int logIndex = 0;	// 로그용 index

		IndexApi indexApi = null;
		DocumentApi documentApi = null;

		String errorStr = "";
		String errorResult = "";

		int totalCnt = 0;
		int failedCnt = 0;
		int successedCnt = 0;
		int mailWght = 0;
		int mailVolm = 0;

		boolean checkMobile = false;
		boolean checkTel = false;

		String customerCode = "";

		// 우체국인 경우
		log.info("###### (반품)우체국 파일 생성 시작##########");

		todayTime = formatter.format(new Date());

		//customProperties.getExAgentPath(); // C:/interface/exAgent/
		//customProperties.getExAgentSend(); // /send/
		// 로컬 경로 => C:/interface/exAgent/yhtxx/send/

		dirPath = customProperties.getExAgentPath() + dirEntCode + customProperties.getExAgentSend();
		fileName = "send_" + dirEntCode + "_" + todayTime + ".txt";


		// 파일생성 폴더 경로 지정
		folder = new File(dirPath);
		realPath = dirPath + fileName;

		// 파일생성 폴더 존재 유무 체크
		if (!folder.isDirectory()) {
			folder.mkdirs();
		}


		SqlSession sqlSession = null;

		// 파일 내용 쓰기
		try {

			int updateCnt = 1;
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);

			tmpMap.put("entCode", 		dirEntCode.toUpperCase());
			tmpMap.put("entBusinessCode", "");
			tmpMap.put("gmtTime", 		"0");
			tmpMap.put("solution", 		"ECM");
			tmpMap.put("stockType", 	"POD");
			tmpMap.put("stockSign", 	"POD");
			tmpMap.put("stockSeqLength","8");
			tmpMap.put("resultCode", 	"FIXED_SIGN_AND_SEQ_NO");
			tmpMap.put("stockRemark", 	"ECM POD JOB NO");
			tmpMap.put("podStatus", "ECM504");
			tmpMap.put("podTableName", 	"TB_ECM_POD_04");
			tmpMap.put("deliveryCode", 	"EMS");
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(tmpMap, "podTableName|gmtTime", false, null, null, null, null);

			strBuffer.setLength(0);
			totalCnt += deliveryListEms.size();

			List<Map<String, Object>> insertPodList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> updatePodList = new ArrayList<Map<String, Object>>();
			Map<String, Object> tmpInsertPodMap = null;
			int seq = 1;
			for (Map<String, Object> deliveryMapEms : deliveryListEms) {

				logIndex++;
				logList.add(deliveryMapEms);
				mailWght = 0;
				mailVolm = 0;
				errorStr = "";
				errorResult = "";

				hblno = Util.null2str(deliveryMapEms.get("HBL_NO"));
				gwt = Util.null2str(String.valueOf(deliveryMapEms.get("GWT")));
				consigneeMobile = Util.null2str(deliveryMapEms.get("CONSIGNEE_HP"));
				consigneeTel = Util.null2str(deliveryMapEms.get("CONSIGNEE_TEL"));
				consigneeAddress = Util.null2str(deliveryMapEms.get("consigneeAddressLocConvert")); //250바이트 이상일 때 오류 처리

				// 멀티송장번호가 없다면 HBL NO를 넣는다.
				if(!"".equals(Util.null2str(deliveryMapEms.get("DELIVERY_NO")))) {
					deliveryNo = Util.null2str(deliveryMapEms.get("DELIVERY_NO"));
					seqNoId =  Util.null2str(deliveryMapEms.get("SEQ_NO_ID"));
					//반품송장번호 앞자리 7로 업데이트 및 각종정보 업데이트QWER
					Map<String,Object>chkMapEms = new HashMap<>();
					chkMapEms.put("deliveryNo",deliveryNo);
					chkMapEms.put("seqNoId",seqNoId);
					chkMapEms.put("odJobNo",Util.null2str(deliveryMapEms.get("OD_JOB_NO")));
					orderBatchMapper.updateReturnNo(chkMapEms);
				}else {
					deliveryNo = hblno;
					//반품송장번호 앞자리 7로 업데이트 및 각종정보 업데이트QWER
					Map<String,Object>chkMapEms = new HashMap<>();
					chkMapEms.put("deliveryNo",deliveryNo);
					chkMapEms.put("odJobNo",Util.null2str(deliveryMapEms.get("OD_JOB_NO")));
					orderBatchMapper.updateReturnNoMulti(chkMapEms);
				}

				if ("".equals(gwt)) {
					gwt = "0";
				} else {
					mailWght = (int) (Float.parseFloat(gwt) * 1000);
				}

				if (mailWght <= 1000) {
					mailVolm = 50;
				} else if (mailWght <= 2000) {
					mailVolm = 60;
				} else if (mailWght <= 5000) {
					mailVolm = 80;
				} else if (mailWght <= 10000) {
					mailVolm = 120;
				} else if (mailWght <= 20000) {
					mailVolm = 140;
				} else if (mailWght <= 30000) {
					mailVolm = 160;
				}

				//값이 있을 때만 특수문자 체크! 없을 때는 true로 넘겨주기
				if (!"".equals(consigneeMobile)) {
					matcher = patternTel.matcher(consigneeMobile);

					if (matcher.find()) {
						checkMobile = true;
					} else {
						checkMobile = false;
					}
				} else {
					checkMobile = true;
				}

				if (!"".equals(consigneeTel)) {
					matcher = patternTel.matcher(consigneeTel);

					if (matcher.find()) {
						checkTel = true;
					} else {
						checkTel = false;
					}
				} else {
					checkTel = true;
				}

				//주소 250자 이상일 시 오류
				if(consigneeAddress.getBytes().length > 250) {
					logList.get(logIndex-1).put("sendStatus", "FAIL");
					failedCnt++;
					if ("".equals(failEmsAddrHblNos))
						failEmsAddrHblNos = hblno;
					else
						failEmsAddrHblNos += "," + hblno;

					errorMap = new HashMap<String, Object>();
					errorMap.put("messageType", "유효성오류");
					errorMap.put("hsn", deliveryMapEms.get("HSN"));
					errorMap.put("blNo", hblno);
					errorMap.put("errorStr", "[우체국]반품요청 주소 오류");
					errorMap.put("errorResult", consigneeAddress);
					errorList.add(errorMap);
					continue;
				}

				if(!"".equals(consigneeMobile) || !"".equals(consigneeTel)) { // 수하인전화번호, 핸드폰번호 유무 체크
					if (checkMobile && checkTel) {
						//우체국 배송요청 각 항목 구분자 "|"
						successedCnt++;
						strBuffer.append("02").append("|"); // 1: 배송신청구분코드(01:신청,02:반품)
						//					strBuffer.append(emsId).append("|"); // 2: 업체구분코드
						strBuffer.append(Util.null2str(deliveryMapEms.get("EMS_ID"))).append("|"); // 2: 업체구분코드
						strBuffer.append(deliveryNo).append("|"); // 3.주문번호
						strBuffer.append(deliveryNo).append("|"); // 4.등기번호
						strBuffer.append(Util.fullWidthToHalfWidth(Util.null2str(deliveryMapEms.get("consigneeNameLocConvert")).trim().replaceAll("\\s", " "))).append("|"); // 5.수취인성명
						strBuffer.append(Util.null2str(deliveryMapEms.get("CONSIGNEE_TEL"))).append("|"); // 6.수취인전화번호
						strBuffer.append(Util.null2str(deliveryMapEms.get("CONSIGNEE_HP"))).append("|");  // 7.수취인기타전화번호
						strBuffer.append(Util.null2str(deliveryMapEms.get("CONSIGNEE_ZIPCODE"))).append("|"); // 8.수취인우편번호
						strBuffer.append(Util.null2str(deliveryMapEms.get("consigneeAddressLocConvert")).replaceAll("\\s", " ")).append("|");// 9.수취인주소
						strBuffer.append("|"); // 10.수취인상세주소
						strBuffer.append(Util.null2str(deliveryMapEms.get("consigneeNameLocConvert")).replaceAll("\\s", " ")).append("|"); // 11.주문자명
						strBuffer.append(Util.null2str(deliveryMapEms.get("CONSIGNEE_TEL"))).append("|"); // 12.주문자전화번호
						strBuffer.append(Util.null2str(deliveryMapEms.get("CONSIGNEE_HP"))).append("|"); // 13.주문자기타전화번호
						strBuffer.append(Util.null2str(deliveryMapEms.get("CONSIGNEE_ZIPCODE"))).append("|"); // 14.주문자우편번호
						strBuffer.append(Util.null2str(deliveryMapEms.get("consigneeAddressLocConvert")).replaceAll("\\s", " ")).append("|"); // 15.주문자주소
						strBuffer.append("|"); // 16.주문자상세주소
						strBuffer.append("|"); // 17.배송희망일자
						strBuffer.append("|"); // 18.배송메시지내역
						strBuffer.append("기타(일반)").append("|"); // 19.상품코드1
						strBuffer.append(Util.null2str(deliveryMapEms.get("commodityItem1")).replaceAll("\\s", " ")).append("|"); // 20.상품명1
						strBuffer.append("기타(일반)").append("|"); // 21.상품코드2
						strBuffer.append(Util.null2str(deliveryMapEms.get("commodityItem2")).replaceAll("\\s", " ")).append("|"); // 22.상품명2
						strBuffer.append("기타(일반)").append("|"); // 23.상품코드3
						strBuffer.append(Util.null2str(deliveryMapEms.get("commodityItem3")).replaceAll("\\s", " ")).append("|"); // 24.상품명3
						strBuffer.append("기타(일반)").append("|"); // 25.상품코드4
						strBuffer.append(Util.null2str(deliveryMapEms.get("commodityItem4")).replaceAll("\\s", " ")).append("|"); // 26.상품명4
						strBuffer.append("기타(일반)").append("|"); // 27.상품코드5
						strBuffer.append(Util.null2str(deliveryMapEms.get("commodityItem5")).replaceAll("\\s", " ")).append("|"); // 28.상품명5
						strBuffer.append(mailWght).append("|"); // 29.우편물중량
						strBuffer.append(mailVolm).append("|"); // 30.우편물크기
						strBuffer.append(Util.null2int(deliveryMapEms.get("PIECES"))).append("|"); // 31.상자수량
						strBuffer.append("N").append("|"); // 32.선불여부(N:선불, Y:착불, Default : N)
						strBuffer.append("|"); // 33.배송비
						strBuffer.append("|"); // 34.당일특급
						strBuffer.append("|"); // 35.자체운송장출력
						strBuffer.append("N").append("|"); // 36.초소형소포여부
						strBuffer.append("|"); // 37.맞교환여부
						strBuffer.append("|"); // 38.반품 원등기번호
						strBuffer.append("|"); // 39.맞교환대상등기번호
						strBuffer.append("|"); // 40.반품 원택배사명
						strBuffer.append("|"); // 41.안심소포 보험가액
						strBuffer.append("|"); // 42.포장재 코드
						strBuffer.append("\r\n");

						// 배송요청상태 변경
						deliveryMapEms.put("podStatus", "ECM504");
						deliveryMapEms.put("odJobNo",deliveryMapEms.get("OD_JOB_NO"));
						deliveryMapEms.put("noCheckLastPodStatus", "Y");


						orderBatchMapper.updateOrderLastPod(deliveryMapEms);

						tmpInsertPodMap = new HashMap<String, Object>();
						tmpInsertPodMap.put("odJobNo", deliveryMapEms.get("OD_JOB_NO"));
						tmpInsertPodMap.put("blNo", hblno);
						tmpInsertPodMap.put("updateUserId", deliveryMapEms.get("registerUserId"));
						if(deliveryMapEms.get("ECM340_POD_JOB_NO") != null){
							tmpInsertPodMap.put("podJobNo", deliveryMapEms.get("ECM504_POD_JOB_NO"));
							updatePodList.add(tmpInsertPodMap);
						}else{
							tmpInsertPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
							insertPodList.add(tmpInsertPodMap);
						}
						logList.get(logIndex-1).put("sendStatus", "SEND");

						//deliveryMapper.updateDeliveryStatus(deliveryMapEms);

						if( (updateCnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}

					} else {
						logList.get(logIndex-1).put("sendStatus", "FAIL");
						failedCnt++;
						if ("".equals(failEmsTelHblNos))
							failEmsTelHblNos = hblno;
						else
							failEmsTelHblNos += "," + hblno;

						errorStr = "[우체국]반품요청 모바일(전화) 특수문자 오류";
						errorResult = !"".equals(consigneeMobile) ? consigneeMobile : consigneeTel;
					}
				} else {
					logList.get(logIndex-1).put("sendStatus", "FAIL");
					failedCnt++;
					if ("".equals(failEmsHblNos))
						failEmsHblNos = hblno;
					else
						failEmsHblNos += "," + hblno;

					errorStr = "[우체국]반품요청 모바일(전화)오류";
				}

				if(!"".equals(errorStr)){
					errorMap = new HashMap<String, Object>();
					errorMap.put("messageType", "유효성오류");
					errorMap.put("hsn", deliveryMapEms.get("HSN"));
					errorMap.put("blNo", hblno);
					errorMap.put("errorStr", errorStr);
					errorMap.put("errorResult" , errorResult);
					errorList.add(errorMap);
				}

			} // for end

			updateCnt = 1;
			for(Map<String, Object> insertPodMap : insertPodList){
				tmpMap.put("odJobNo", insertPodMap.get("odJobNo"));
				tmpMap.put("blNo", insertPodMap.get("blNo"));
				tmpMap.put("updateUserId", insertPodMap.get("updateUserId"));
				tmpMap.put("podJobNo", insertPodMap.get("podJobNo"));
				orderBatchMapper.insertOrderPod(tmpMap);
				if( (updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			updateCnt = 1;
			for(Map<String, Object> updatePodMap : updatePodList){
				tmpMap.put("updateUserId", updatePodMap.get("updateUserId"));
				tmpMap.put("podJobNo", updatePodMap.get("podJobNo"));
				orderBatchMapper.updateOrderPod(tmpMap);
				if( (updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}

			/** 배송요청시 미집하 POD삭제 */
			tmpMap.put("podStatus", "ECM350");
			for(Map<String, Object> insertPodMap : insertPodList){
				tmpMap.put("odJobNo", insertPodMap.get("odJobNo"));
				orderBatchMapper.deleteOrderPod(tmpMap);
				if( (updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			for(Map<String, Object> updatePodMap : updatePodList){
				tmpMap.put("odJobNo", updatePodMap.get("odJobNo"));
				orderBatchMapper.deleteOrderPod(tmpMap);
				if( (updateCnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}

			if (strBuffer.length() > 0) {
				FileWriter fw = new FileWriter(realPath);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(strBuffer.toString());
				bw.close();

				log.info("###### 우체국 파일 생성 완료 : " + realPath + "############");
			}

			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();

		} catch (Exception e) {
			log.error(Util.getPrintStacTraceString(e));
			resultStr = "D030";
			logList.get(logIndex-1).put("sendStatus", "FAIL");
		} finally {
			sqlSession.close();
		}

		if ("D010".equals(resultStr)) { // 정상적으로 요청완료
			sendYn = "Y";
		} else {
			sendYn = resultStr;
		}

		//로그 insert
		for(Map<String,Object> logMap : logList) {

			// history 등록
			tmpMap = new HashMap<String, Object>();
			logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
			index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");

			// 처리결과에 따른 로그 기록 history
			tmpMap.clear();
			tmpMap.put("ent_code", dirEntCode.toUpperCase());
			tmpMap.put("customer_code", logMap.get("AGENT_CODE")); 		 // 고객코드
			tmpMap.put("solution", "ECM");      		 						 // SOLUTION
			tmpMap.put("bound", logMap.get("BOUND"));		 	 		// 바운드
			tmpMap.put("bound_type", logMap.get("BOUND_TYPE"));	 		// 바운드타입
			tmpMap.put("hbl_no", logMap.get("HBL_NO"));				 	 // HBL_NO
			tmpMap.put("if_service_code", "EMS");
			tmpMap.put("if_doc_code", "SEND_COURIER");
			tmpMap.put("if_doc_name", "택배사전송"); 			 			 // 문서분류명 (AIR ORDER, SEA ORDER)
			tmpMap.put("if_doc_reference_no", logMap.get("MBL_NO"));	 	 // 문서번호
			tmpMap.put("if_vender", logMap.get("AGENT_CODE"));			// 중개사업자
			tmpMap.put("if_send_recv_type", "S");			 					 // 송수신타입(S:SEND, R:RECV)
			tmpMap.put("if_send_recv_method", "");		 				 		 // 송수신방법
			tmpMap.put("if_file_path", realPath);			 						 	 // 파일경로
			tmpMap.put("if_file_name", fileName);			 				 	 // 파일명
			tmpMap.put("if_status", logMap.get("sendStatus"));			 		 // 처리상태
			tmpMap.put("if_status_info", "");		 				 			 // 송수신상태_설명
			tmpMap.put("if_sender_id", logMap.get("AGENT_CODE"));									 // 송신식별자
			tmpMap.put("if_sender_name", "");		 							 // 송신자명
			tmpMap.put("if_receiver_id", "");		 							 // 수신식별자
			tmpMap.put("if_receiver_name", "");	     							 // 수신자명
			tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));				 // DATE
			tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));			 // DATE_TIME
			tmpMap.put("if_remark", "EMS");			 					 		 // 비고( 에러로그 등)
			tmpMap.put("register_user_id", logMap.get("registerUserId"));
			tmpMap.put("register_user_name", logMap.get("registerUserName"));
			tmpMap.put("register_date_time", System.currentTimeMillis());
			tmpMap.put("es_job_no", logId);

			indexApi = new IndexApi(client);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);

			documentApi = new DocumentApi(client);
			documentApi.createDocument(index, logId, tmpMap);

		}

		returnMap.put("sendEmsYn", sendYn);
		returnMap.put("totalEmsCnt", totalCnt);
		returnMap.put("successedEmsCnt", successedCnt);
		returnMap.put("failEmsCnt", failedCnt);
		returnMap.put("failEmsHblNos", failEmsHblNos);
		returnMap.put("failEmsAddrHblNos", failEmsAddrHblNos);
		returnMap.put("failEmsInterfaceHblNos", failEmsInterfaceHblNos);
		returnMap.put("failEmsTelHblNos", failEmsTelHblNos);
		returnMap.put("errorListEms", errorList);
		return returnMap;
	}


}

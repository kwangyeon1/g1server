package org.yht.exps.express.service.eCommerce;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.eCommerce.LicenseMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.sms.EnterpriseMapper;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.eCommerce.license.License;
import org.yht.exps.express.model.eCommerce.license.LicenseCommodityItem;
import org.yht.exps.express.model.eCommerce.license.LicenseCommodityItemList;
import org.yht.exps.express.model.eCommerce.license.LicenseDelete;
import org.yht.exps.express.model.eCommerce.license.LicenseGoodsItem;
import org.yht.exps.express.model.eCommerce.license.LicenseGoodsItemList;
import org.yht.exps.express.model.eCommerce.license.LicenseList;
import org.yht.exps.express.model.eCommerce.license.LicenseSaveResult;
import org.yht.exps.express.model.eCommerce.license.search.SearchLicenseBatchUpdate;
import org.yht.exps.express.model.eCommerce.license.search.SearchLicenseList;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.model.sms.user.User;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.sms.SmsUserService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.FileUtil;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

@Slf4j
@Service
@Transactional
public class LicenseService {

	@Autowired
	CustomProperties customProperties;
	
	@Autowired
    CommonMapper commonMapper;
	@Autowired
	OrderMapper orderMapper;
	@Autowired
	LicenseMapper licenseMapper;
	
    @Autowired
    CommonService commonService;
    
	@Autowired
	RestHighLevelClient client;	
	
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;	
	
	@Autowired
	EnterpriseMapper enterpriseMapper;
	
	@Autowired
	SmsUserService smsUserService;
	
	@Resource(name = "fileService")
	FileService fileService;
	
	/**
	 * E면장 List(main) 조회
	 * @param SearchLicenseList
	 * @return
	 * @throws Exception
	 */	
	
	public LicenseList selectLicenseEList(SearchLicenseList search) throws Exception {
		LicenseList licenseList = new LicenseList();
 		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
 		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
 		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchLicenseText()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchLicenseText())).split(",")));
		}
		
		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSrchLicenseList(searchNoList);
		}else{
			search.setSearchType("");
			search.setSrchLicenseText(Util.null2str(search.getSrchLicenseText()).trim().replace(",", ""));
		}
 		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "PO_DATE|REGISTER_DATE_TIME|SEND_DATE_TIME|RESULT_DATE_TIME|RO_DATE", "srchLicenseType", "PO_NO|EFN_PO_NO");
		List<License> list  = licenseMapper.selectLicenseEList(search);
		licenseList.setLicenseList(list);
		if(list.size() > 0) {
			licenseList.setTotal(list.get(0).getTotal());
		}else {
			licenseList.setTotal(0);
		}
		
		return licenseList; 
	}		
	
	/**
	 * 면장 상세조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public License selectLicenseDetail(SearchLicenseList search) throws Exception {
		License detail  = licenseMapper.selectLicense(search);	
		return detail;
	}
	
	/**
	 * 면장 란 List
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public LicenseGoodsItemList selectLicenseGoodsItemList(SearchLicenseList search) throws Exception {
		
		LicenseGoodsItemList licenseGoodsItemList = new LicenseGoodsItemList();
		List<LicenseGoodsItem> list = licenseMapper.selectLicenseGoodsItemList(search);
		licenseGoodsItemList.setLicenseGoodsItemList(list);
		
		if(list != null && list.size() > 0) {
			licenseGoodsItemList.setTotal(list.size());
		}else {
			licenseGoodsItemList.setTotal(0);
		}
		
		return licenseGoodsItemList;
	}
	
	/**
	 * 면장 규격 List
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public LicenseCommodityItemList selectLicenseCommodityItemList(SearchLicenseList search) throws Exception {
		
		LicenseCommodityItemList licenseCommodityItemList = new LicenseCommodityItemList();
		List<LicenseCommodityItem> list = licenseMapper.selectLicenseCommodityItemList(search);
		licenseCommodityItemList.setLicenseCommodityItemList(list);
		
		if(list != null && list.size() > 0) {
			licenseCommodityItemList.setTotal(list.size());
		}else {
			licenseCommodityItemList.setTotal(0);
		}
		
		return licenseCommodityItemList;
	}
	
	/**
	 * 면장 란 - 오더아이템 조회
	 * @param SearchLicenseList
	 * @return
	 * @throws Exception
	 */	
	
	public LicenseGoodsItemList selectLicenseGoodsItemOdList(SearchLicenseList search) throws Exception {
		LicenseGoodsItemList licenseGoodsItemList = new LicenseGoodsItemList();
 		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
 		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		List<LicenseGoodsItem> list  = licenseMapper.selectLicenseGoodsItemOdList(search);
		licenseGoodsItemList.setLicenseGoodsItemList(list);
		if(list.size() > 0) {
			licenseGoodsItemList.setTotal(list.size());
		}else {
			licenseGoodsItemList.setTotal(0);
		}
		
		return licenseGoodsItemList; 
	}	
	
	/**
	 * 면장 란 - 오더아이템 조회
	 * @param SearchLicenseList
	 * @return
	 * @throws Exception
	 */	
	
	public LicenseCommodityItemList selectLicenseCommodityItemOdList(SearchLicenseList search) throws Exception {
		LicenseCommodityItemList licenseCommodityItemList = new LicenseCommodityItemList();
 		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
 		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		List<LicenseCommodityItem> list  = licenseMapper.selectLicenseCommodityItemOdList(search);
		licenseCommodityItemList.setLicenseCommodityItemList(list);
		if(list.size() > 0) {
			licenseCommodityItemList.setTotal(list.size());
		}else {
			licenseCommodityItemList.setTotal(0);
		}
		
		return licenseCommodityItemList; 
	}
	
	/**
	 * 면장 거래처 조회
	 * @param SearchLicenseList
	 * @return
	 * @throws Exception
	 */	
	
	public License selectLicenseCustomer(SearchLicenseList search) throws Exception {
		
		License licenseCustomer  = licenseMapper.selectLicenseCustomer(search);
		
		return licenseCustomer; 
	}
	
	/**
	 * 면장 EDI 특송부호 조회
	 * @param SearchLicenseList
	 * @return
	 * @throws Exception
	 */	
	
	public String selectLicenseEdi(SearchLicenseList search) throws Exception {
		String expressSign = "";
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		Map<String, Object> srchMap = new HashMap<String, Object>();
		
		srchMap.put("srchEntCode", search.getSrchEntCode());
		srchMap.put("srchEntBusinessCode", search.getSrchEntBusinessCode());
		srchMap.put("srchBound", search.getSrchBound());
		srchMap.put("srchBoundType", search.getSrchBoundType());
		srchMap.put("srchEdiCityPort", search.getSrchEdiCityPort());
		tmpMap = commonMapper.selectBusEdiEcmInfo(srchMap);
		
		if(tmpMap != null) {
			expressSign = Util.null2str(tmpMap.get("EDI_EXPRESS_SIGN"));
		}
		
		return StringEscapeUtils.escapeHtml4(expressSign); 
	}
	
	/**
	 * ECM 면장 - insert
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public LicenseSaveResult insertLicense(License license, HttpServletRequest request) throws Exception {
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(license, "dateFormat|gmtTime", false, null, null, null, null);
		/* 면장 - Master */
		
		/** LICENSE_JOB_NO 생성 */
		Map<String, Object> licenseSaveMap = new HashMap<String, Object>();
		licenseSaveMap.put("createOdJobNo", "Y");
		licenseSaveMap.put("seqNo", 1);
		licenseSaveMap.put("gmtTime", license.getGmtTime());
		licenseSaveMap.put("entCode", license.getEntCode());
		licenseSaveMap.put("entBusinessCode", "");
		licenseSaveMap.put("updateUserId", license.getUpdateUserId());
		licenseSaveMap.put("solution", "ECM");
		licenseSaveMap.put("stockType", "LIC");
		licenseSaveMap.put("stockSign", "CO"+license.getLicenseType());
		licenseSaveMap.put("stockSeqLength", 8);
		licenseSaveMap.put("dateLength", null);
		licenseSaveMap.put("signType", null);
		licenseSaveMap.put("stockDateFormat", null);
		licenseSaveMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		licenseSaveMap.put("stockRemark", "ORDER-JOB NO");
		
		String licenseJobNo = commonMapper.selectNextStockMaxNo(licenseSaveMap);
		int goodsItemTot = 0;
		if(license.getLicenseGoodsItemList() != null && !license.getLicenseGoodsItemList().isEmpty()) {
			goodsItemTot = license.getLicenseGoodsItemList().size();
		}
		license.setLicenseJobNo(licenseJobNo);
		license.setGoodsItemTot(goodsItemTot);
		
		licenseMapper.insertLicense(license);
		
		// Order License Type Update
		if(!Util.null2str(license.getOdJobNo()).equals("")) {
			License odLicense = new License();
			odLicense.setOdJobNo(license.getOdJobNo());
			odLicense.setLicenseType(license.getLicenseType());
			licenseMapper.updateOdLicenseType(odLicense);
		}
		
		/* 면장 - 란 */
		// 신고가격, 조정금액, 총란수
		
		if(license.getLicenseGoodsItemList() != null && !license.getLicenseGoodsItemList().isEmpty()) {
			for(LicenseGoodsItem licenseGoodsItem : license.getLicenseGoodsItemList()) {
				
				licenseGoodsItem.setLicenseJobNo(licenseJobNo);
				licenseMapper.insertLicenseGoodsItem(licenseGoodsItem);
			}
		}
		
		/* 면장 - 규격 */
		if(license.getLicenseCommodityItemList() != null && !license.getLicenseCommodityItemList().isEmpty()) {
			for(LicenseCommodityItem licenseCommodityItem : license.getLicenseCommodityItemList()) {
				// 란번호 추출
				SearchLicenseList search = new SearchLicenseList();
				search.setLicenseJobNo(license.getLicenseJobNo());
				search.setHsCode(licenseCommodityItem.getHsCode());
				List<LicenseGoodsItem> licenseGoodsItemList = licenseMapper.selectLicenseGoodsItemList(search);
				licenseCommodityItem.setSeqNoId(licenseGoodsItemList.get(0).getSeqNoId());
				
				licenseCommodityItem.setLicenseJobNo(licenseJobNo);
				licenseMapper.insertLicenseCommodityItem(licenseCommodityItem);
			}
		}
		
		ProcessLogs processLogs = new ProcessLogs();
		
		processLogs.setEntCode(license.getEntCode());
		processLogs.setEntBusinessCode(license.getEntBusinessCode());
		processLogs.setJobNo(license.getLicenseJobNo());
		processLogs.setJobFuncType("ADD");
		processLogs.setSolution("ECM");
		processLogs.setBound(license.getBound());
		processLogs.setBoundType("EXP");
		processLogs.setProcessUserId(license.getUpdateUserId());
		processLogs.setLogJobType("DATA");
		processLogs.setLogJobType("License");
		processLogs.setDescription("면장 등록 ");
		processLogs.setJobLog(new Gson().toJson(license));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(license.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(license.getLogUserName());
		processLogs.setProcessUserNameEng(license.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		LicenseSaveResult licenseSaveResult = new LicenseSaveResult();
		licenseSaveResult.setLicenseJobNo(license.getLicenseJobNo());
		
		return licenseSaveResult;
	}
	
	/**
	 * ECM 면장 - update
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public LicenseSaveResult updateLicense(License license, HttpServletRequest request) throws Exception {
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(license, "dateFormat", false, null, null, null, null);
		// 면장 수정
		licenseMapper.updateLicense(license);	
		
		// 면장 - 란
		// 삭제
		LicenseGoodsItem delLicenseGoodsItem = new LicenseGoodsItem();
		delLicenseGoodsItem.setLicenseJobNo(license.getLicenseJobNo());
		licenseMapper.deleteLicenseGoodsItem(delLicenseGoodsItem);
		if(license.getLicenseGoodsItemList() != null && !license.getLicenseGoodsItemList().isEmpty()) {
			
			// 저장
			for(LicenseGoodsItem licenseGoodsItem : license.getLicenseGoodsItemList()) {
				licenseGoodsItem.setLicenseJobNo(license.getLicenseJobNo());
				licenseMapper.insertLicenseGoodsItem(licenseGoodsItem);
			}
		}
		
		// 면장 - 규격
		// 삭제
		LicenseCommodityItem delLicenseCommodityItem = new LicenseCommodityItem();
		delLicenseCommodityItem.setLicenseJobNo(license.getLicenseJobNo());
		licenseMapper.deleteLicenseCommodityItem(delLicenseCommodityItem);
		if(license.getLicenseCommodityItemList() != null && !license.getLicenseCommodityItemList().isEmpty()) {
			
			// 저장
			for(LicenseCommodityItem licenseCommodityItem : license.getLicenseCommodityItemList()) {
				// 란번호 추출
				SearchLicenseList search = new SearchLicenseList();
				search.setLicenseJobNo(license.getLicenseJobNo());
				search.setHsCode(licenseCommodityItem.getHsCode());
				List<LicenseGoodsItem> licenseGoodsItemList = licenseMapper.selectLicenseGoodsItemList(search);
				if(licenseGoodsItemList!=null && !licenseGoodsItemList.isEmpty()) {
					licenseCommodityItem.setSeqNoId(licenseGoodsItemList.get(0).getSeqNoId());
				} else {
					licenseCommodityItem.setSeqNoId("999999");
				}
				
				licenseCommodityItem.setLicenseJobNo(license.getLicenseJobNo());
				licenseMapper.insertLicenseCommodityItem(licenseCommodityItem);
			}
		}

		ProcessLogs processLogs = new ProcessLogs();
		
		processLogs.setEntCode(license.getEntCode());
		processLogs.setEntBusinessCode(license.getEntBusinessCode());
		processLogs.setJobNo(license.getLicenseJobNo());
		processLogs.setJobFuncType("UPDATE");
		processLogs.setSolution("ECM");
		processLogs.setBound(license.getBound());
		processLogs.setBoundType("EXP");
		processLogs.setProcessUserId(license.getUpdateUserId());
		processLogs.setLogJobType("DATA");
		processLogs.setLogJobType("License");
		processLogs.setDescription("면장 수정 ");
		processLogs.setJobLog(new Gson().toJson(license));
		processLogs.setResult("ok");
		processLogs.setEntBusinessPrintName(license.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(license.getLogUserName());
		processLogs.setProcessUserNameEng(license.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		LicenseSaveResult licenseSaveResult = new LicenseSaveResult();
		licenseSaveResult.setLicenseJobNo(license.getLicenseJobNo());
		
		return licenseSaveResult;
	}
	
	/**
	 * ECM 면장 - delete
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public LicenseSaveResult deleteLicense(LicenseDelete licenseDelete, HttpServletRequest request) throws Exception {
		LicenseSaveResult licenseSaveResult = new LicenseSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		
		if(licenseDelete == null) {
			return null;
		}
		
		List<License> list = licenseDelete.getLicenseList();

		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", licenseDelete.getEntCode());
		tmpLogMap.put("ent_business_code", licenseDelete.getEntBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("process_user_id", licenseDelete.getUpdateUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("job_func_type", "DELETE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		
		// 사업장명 가져오기
		if (!"".equals(Util.null2str(licenseDelete.getUserBusinessCode()))) {
			String entBusinessPrintName = enterpriseMapper.getEntBusinessPrintName(licenseDelete.getUserBusinessCode());
			tmpLogMap.put("ent_business_print_name", entBusinessPrintName);
		}
		
		// 사용자명 가져오기
		if (!"".equals(Util.null2str(licenseDelete.getUpdateUserId()))) {
			User user = smsUserService.getUser(licenseDelete.getUpdateUserId());
			tmpLogMap.put("process_user_name", user.getUserName());
			tmpLogMap.put("process_user_name_eng", user.getUserNameEng());
		}
		
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		
		for(int i=0;i<list.size();i++) {
			License license = list.get(i);
			license.setEntCode(licenseDelete.getEntCode());
			licenseMapper.deleteLicense(license);

			LicenseGoodsItem licenseGoodsItem = new LicenseGoodsItem();
			licenseGoodsItem.setLicenseJobNo(license.getLicenseJobNo());
			licenseMapper.deleteLicenseGoodsItem(licenseGoodsItem);

			LicenseCommodityItem licenseCommodityItem = new LicenseCommodityItem();
			licenseCommodityItem.setLicenseJobNo(license.getLicenseJobNo());
			licenseMapper.deleteLicenseCommodityItem(licenseCommodityItem);
			
			// Order License Type Update
			if(!Util.null2str(license.getOdJobNo()).equals("")) {
				License odLicense = new License();
				odLicense.setOdJobNo(license.getOdJobNo());
				odLicense.setLicenseType("");
				licenseMapper.updateOdLicenseType(odLicense);
			}
			
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(i));
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpLogMap);
			logMap.put("reference_no_id", bulkData.getId());
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
		
		}
		
		commonService.insertLogBulk(bulkList);
		return licenseSaveResult;
	}
	
	/**
	 * X면장 엑셀업로드 유효성체크
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public LicenseSaveResult excelUploadCheckX(MultipartFile uploadExcel, String entCode) throws Exception{
		Map<String, Object> resultData = new HashMap<String, Object>();
		LicenseSaveResult licenseSaveResult = new LicenseSaveResult();
		
		try {
			// 파일 확장자 유효성 검사
			if(!fileService.checkExtension(uploadExcel, new String[]{"xlsx", "xls"})) {
				licenseSaveResult.setResultCode("extError");
				return licenseSaveResult;
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
				fieldName =		new String[]{"PO_NO", "ITEM_CODE", "DETAIL_ITEM_NAME","NWT", "UNIT_PRICE_CUR", "PIECES", "LICENSE_PAY_AMOUNT_CUR", "CURRENCY_CODE", "CONSIGNEE_NAME", "DEST_COUNTRY", "HS_CODE", "ORIGIN_COUNTRY", "DELIVERY_TYPE_CODE",
											"DEST_PORT", "CONSIGNEE_ZIPCODE", "CONSIGNEE_ADDRESS", "CONSIGNEE_TEL", "MAKER_NAME", "MAKER_BUSINESS_ID", "MAKER_CUSTOMS_ID", "MAKER_ZIPCODE", "INCOTERMS", "DELIVERY_FEE_LOC", "INSURANCE_LOC",
											"LICENSE_PAY_METHOD", "LICENSE_REMARK"};	
				fieldTitle =	new String[]{"주문번호","상품ID", "상품명(영문)","중량","가격","주문수량","결제금액","결제통화코드","구매자상호명","목적국 국가코드","HS코드","원산지 국가코드","운송수단",
											"적재항","구매자우편번호","구매자주소","구매자전화번호","제조자","제조자사업자번호","제조자통관고유부호","제조장소(우편번호)","인도조건","운임원화","보험료원화",
											"결제방법코드","신고인기재란"};
				fieldLength =	new String[]{"50", "30", "50", "9.3", "9.2", "4", "9.2", "3", "60", "2", "10", "2", "3",
											"5", "20", "560", "40", "28", "13", "15", "5", "3", "9", "9",
											"2", "400"};
				fieldType =		new String[]{"S", "S", "SN", "DBL", "DBL", "N", "DBL", "S", "SN", "S", "S", "S", "S",
											"S", "S", "SN", "S", "SN", "S", "S", "S", "S", "DBL", "DBL",
											"S", "SN"};
				
				List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
				
				InputStream is = null;
				try {
					is = new FileInputStream(file);
					Workbook wb = WorkbookFactory.create(is);
					Sheet sheet = wb.getSheetAt(0);
					DataFormatter df = new DataFormatter();
					int fieldSize = fieldName.length;
					StringBuffer errMsg = new StringBuffer();
					Map<String, Object> dataMap = null;
					String fieldValue = "";
					
					if(extension.equals("xlsx")){
						XSSFCell myCell = null;
						for (int i = 1; i < sheet.getLastRowNum(); i++) {
							if(sheet.getRow(i) == null){
								licenseSaveResult.setResultCode("A11216");
								resultData.put("nullLine", i + 1);
								resultData.put("lastLine", sheet.getLastRowNum() + 1);
								licenseSaveResult.setResultData(resultData);
								return licenseSaveResult;
							}
							
							dataMap = new HashMap<String, Object>();
							//셀 셋팅
							for(int j = 0; j < fieldSize; j++){
								myCell = (XSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
								fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i);
								
								dataMap.put(fieldName[j], fieldValue);
							}
							dataList.add(dataMap);
						}
					}else{
						HSSFCell myCell = null;
						for (int i = 1; i < sheet.getLastRowNum(); i++) {
							if(sheet.getRow(i) == null){
								licenseSaveResult.setResultCode("A11216");
								resultData.put("nullLine", i + 1);
								resultData.put("lastLine", sheet.getLastRowNum() + 1);
								licenseSaveResult.setResultData(resultData);
								return licenseSaveResult;
							}
							
							dataMap = new HashMap<String, Object>();
							//셀 셋팅
							for(int j = 0; j < fieldSize; j++){
								myCell = (HSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
								fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i);
		
								dataMap.put(fieldName[j], fieldValue);
							}
							dataList.add(dataMap);
						}
					}
					
					if(errMsg.length() != 0){
						licenseSaveResult.setResultCode("dataErr");
						Map<String, Object> errMap = new HashMap<String, Object>();
						errMap.put("errStr", errMsg);
						licenseSaveResult.setResultData(errMap);
						return licenseSaveResult;
					}
					
					for(Map<String, Object> data : dataList){
						if(Util.null2str(data.get("PO_NO")).equals("") || Util.null2str(data.get("DETAIL_ITEM_NAME")).equals("") || Util.null2str(data.get("NWT")).equals("") || Util.null2str(data.get("UNIT_PRICE_CUR")).equals("") 
								|| Util.null2str(data.get("PIECES")).equals("") || Util.null2str(data.get("LICENSE_PAY_AMOUNT_CUR")).equals("") || Util.null2str(data.get("CURRENCY_CODE")).equals("") || Util.null2str(data.get("CONSIGNEE_NAME")).equals("")
								|| Util.null2str(data.get("DEST_COUNTRY")).equals("") || Util.null2str(data.get("HS_CODE")).equals("")){
							licenseSaveResult.setResultCode("dataNull");
							return licenseSaveResult;
						}
					}
					
					resultData.put("dataList", dataList);
					
					licenseSaveResult.setResultData(resultData);
					licenseSaveResult.setResultCode("200");
					
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
				licenseSaveResult.setResultCode("ERROR");
			}
		
		}catch(Exception e) {
			licenseSaveResult.setResultCode("A00063");
			log.error("exception Message :: " + e.getMessage());
		}finally{
		}
		
		return licenseSaveResult;
	}
	
	/**
	 * X면장 엑셀업로드 유효성체크
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public LicenseSaveResult excelUploadCheckE(MultipartFile uploadExcel, String entCode) throws Exception{
		Map<String, Object> resultData = new HashMap<String, Object>();
		LicenseSaveResult licenseSaveResult = new LicenseSaveResult();
		
		try {
			// 파일 확장자 유효성 검사
			if(!fileService.checkExtension(uploadExcel, new String[]{"xlsx", "xls"})) {
				licenseSaveResult.setResultCode("extError");
				return licenseSaveResult;
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
				fieldName =		new String[]{"PO_NO", "HBL_NO", "DEST_COUNTRY","CONSIGNEE_NAME", "HS_CODE", "DETAIL_ITEM_NAME", "BRAND", "UNIT_PRICE_CUR", "PIECES", "NWT", "ADJUST_AMOUNT_LOC", "LICENSE_PAY_AMOUNT_CUR", "CURRENCY_CODE",
											"LICENSE_PAY_METHOD", "MAKER_NAME", "ITEM_CODE", "ITEM_URL"};	
				fieldTitle =	new String[]{"주문번호","주문배송관련번호", "목적국국가코드","구매자상호명","HS코드","상품명(영문)","모델규격","가격","주문수량","중량","조정금액","결제금액","결제통화코드"
											,"결제방법코드","제조자상호","상품ID","웹페이지주소"};
				fieldLength =	new String[]{"35", "35", "2", "60", "10", "50", "300", "18.6", "10", "16.3", "18.2", "18.2", "3",
											"10", "50", "30", "300"};
				fieldType =		new String[]{"S", "S", "S", "SN", "S", "SN", "SN", "DBL","N","DBL","DBL","DBL","S",
											"S", "SN", "S", "S"};
				
				List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
				
				InputStream is = null;
				try {
					is = new FileInputStream(file);
					Workbook wb = WorkbookFactory.create(is);
					Sheet sheet = wb.getSheetAt(0);
					DataFormatter df = new DataFormatter();
					int fieldSize = fieldName.length;
					StringBuffer errMsg = new StringBuffer();
					Map<String, Object> dataMap = null;
					String fieldValue = "";
					
					if(extension.equals("xlsx")){
						XSSFCell myCell = null;
						for (int i = 1; i < sheet.getLastRowNum(); i++) {
							if(sheet.getRow(i) == null){
								licenseSaveResult.setResultCode("A11216");
								resultData.put("nullLine", i + 1);
								resultData.put("lastLine", sheet.getLastRowNum() + 1);
								licenseSaveResult.setResultData(resultData);
								return licenseSaveResult;
							}
							
							dataMap = new HashMap<String, Object>();
							//셀 셋팅
							for(int j = 0; j < fieldSize; j++){
								myCell = (XSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
								fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i);
								
								dataMap.put(fieldName[j], fieldValue);
							}
							dataList.add(dataMap);
						}
					}else{
						HSSFCell myCell = null;
						for (int i = 1; i < sheet.getLastRowNum(); i++) {
							if(sheet.getRow(i) == null){
								licenseSaveResult.setResultCode("A11216");
								resultData.put("nullLine", i + 1);
								resultData.put("lastLine", sheet.getLastRowNum() + 1);
								licenseSaveResult.setResultData(resultData);
								return licenseSaveResult;
							}
							
							dataMap = new HashMap<String, Object>();
							//셀 셋팅
							for(int j = 0; j < fieldSize; j++){
								myCell = (HSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
								fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i);
		
								dataMap.put(fieldName[j], fieldValue);
							}
							dataList.add(dataMap);
						}
					}
					
					if(errMsg.length() != 0){
						licenseSaveResult.setResultCode("dataErr");
						Map<String, Object> errMap = new HashMap<String, Object>();
						errMap.put("errStr", errMsg);
						licenseSaveResult.setResultData(errMap);
						return licenseSaveResult;
					}
					
					for(Map<String, Object> data : dataList){
						if(Util.null2str(data.get("PO_NO")).equals("") || Util.null2str(data.get("DETAIL_ITEM_NAME")).equals("") || Util.null2str(data.get("NWT")).equals("") || Util.null2str(data.get("UNIT_PRICE_CUR")).equals("") 
								|| Util.null2str(data.get("PIECES")).equals("") || Util.null2str(data.get("LICENSE_PAY_AMOUNT_CUR")).equals("") || Util.null2str(data.get("CURRENCY_CODE")).equals("") || Util.null2str(data.get("CONSIGNEE_NAME")).equals("")
								|| Util.null2str(data.get("DEST_COUNTRY")).equals("") || Util.null2str(data.get("HS_CODE")).equals("")){
							licenseSaveResult.setResultCode("dataNull");
							return licenseSaveResult;
						}
					}
					
					resultData.put("dataList", dataList);
					
					licenseSaveResult.setResultData(resultData);
					licenseSaveResult.setResultCode("200");
					
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
				licenseSaveResult.setResultCode("ERROR");
			}
		
		}catch(Exception e) {
			licenseSaveResult.setResultCode("A00063");
			log.error("exception Message :: " + e.getMessage());
		}finally{
		}
		
		return licenseSaveResult;
	}
	
	public String dataChk(String data, String name, String length, String type, StringBuffer errMsg, int row) throws Exception {
		//TODO 엑셀 셀타입 텍스트로 고정시킬수있는지?  없다면 추가개발필요
		if(type.equals("N")){
			if (Util.null2str(data).equals("")){
				data = "0";
			} else {
				try {
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
					SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
					SimpleDateFormat sdFormat2 = new SimpleDateFormat("yyyyMMdd");
					data = data.replaceAll("[-./]", "").substring(0, 8);
					data = sdFormat.format(sdFormat2.parse(data));
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
	 * 면장 Excel 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public LicenseSaveResult excelUpload(LicenseList licenseList) throws Exception {
		LicenseSaveResult licenseSaveResult = new LicenseSaveResult();
		String licenseType = licenseList.getLicenseType();
		List<Map<String, Object>> dataList = licenseList.getDataList();	
		if(dataList == null) {
			return licenseSaveResult;
		}
		
		License license = null;
		LicenseGoodsItem licenseGoodsItem = null;
		LicenseCommodityItem licenseCommodityItem = null;
		List<License> licenseInsertList = new ArrayList<License>();
		
		List<Map<String, Object>> licenseInsertMapList = new ArrayList<Map<String, Object>>();
		
		
		for(Map<String, Object> data : dataList){
			if(licenseType.equals("X")) {
				if(Util.null2str(data.get("PO_NO")).equals("") || Util.null2str(data.get("DETAIL_ITEM_NAME")).equals("") || Util.null2str(data.get("NWT")).equals("") || Util.null2str(data.get("UNIT_PRICE_CUR")).equals("") 
						|| Util.null2str(data.get("PIECES")).equals("") || Util.null2str(data.get("LICENSE_PAY_AMOUNT_CUR")).equals("") || Util.null2str(data.get("CURRENCY_CODE")).equals("") || Util.null2str(data.get("CONSIGNEE_NAME")).equals("")
						|| Util.null2str(data.get("DEST_COUNTRY")).equals("") || Util.null2str(data.get("HS_CODE")).equals("")){
					licenseSaveResult.setResultCode("dataNull");
					return licenseSaveResult;
				}
			} else {
				if(Util.null2str(data.get("PO_NO")).equals("") || Util.null2str(data.get("HBL_NO")).equals("") || Util.null2str(data.get("DEST_COUNTRY")).equals("") || Util.null2str(data.get("CONSIGNEE_NAME")).equals("")
						|| Util.null2str(data.get("HS_CODE")).equals("") || Util.null2str(data.get("DETAIL_ITEM_NAME")).equals("") || Util.null2str(data.get("BRAND")).equals("") || Util.null2str(data.get("UNIT_PRICE_CUR")).equals("") 
						|| Util.null2str(data.get("PIECES")).equals("") || Util.null2str(data.get("NWT")).equals("") || Util.null2str(data.get("LICENSE_PAY_AMOUNT_CUR")).equals("") || Util.null2str(data.get("LICENSE_PAY_METHOD")).equals("")
						|| Util.null2str(data.get("LICENSE_PAY_METHOD")).equals("") || Util.null2str(data.get("MAKER_NAME")).equals("")){
					licenseSaveResult.setResultCode("dataNull");
					return licenseSaveResult;
				}
			}
			
			String poNo = data.get("PO_NO").toString();
			String hsCode = data.get("HS_CODE").toString();
			
			// 동일 오더번호가 기존 데이터에 존재할 경우 Error Return
			SearchLicenseList searchLicenseList = new SearchLicenseList();
			searchLicenseList.setEntCode(licenseList.getEntCode());
			searchLicenseList.setPoNo(poNo);
			License oldLicense = licenseMapper.selectLicense(searchLicenseList);
			
			if(oldLicense != null) {
				licenseSaveResult.setResultCode("poNoExist");
				return licenseSaveResult;
			}
			
			// 동일 HS CODE, PO NO 존재 여부 확인
			Boolean dupHsCodePoNo = false;
			int dupHsCodePoNoIndex = 0;
			Boolean dupPoNo = false;
			int dupPoNoIndex = 0;
			int index = 0;
			
			license = new License();
			licenseGoodsItem = new LicenseGoodsItem();
			licenseCommodityItem = new LicenseCommodityItem();
			Map<String, Object> licenseInsertMap = new HashMap<String, Object>();
			
			if(licenseInsertMapList != null && !licenseInsertMapList.isEmpty()) {
				for(Map<String, Object> licenseInsertMapListData : licenseInsertMapList) {
					if(licenseInsertMapListData.get("poNo").equals(poNo)) {
						dupPoNo = true;
						dupPoNoIndex = index;
						
						List<LicenseGoodsItem> licenseGoodsItemDataList = (List<LicenseGoodsItem>) licenseInsertMapListData.get("LicenseGoodsItem");
						int dupHsCodePoNoforIndex = 0;
						for(LicenseGoodsItem licenseGoodsItemData : licenseGoodsItemDataList) {
							if(licenseGoodsItemData.getHsCode().equals(hsCode)) {
								dupHsCodePoNo = true;
								dupHsCodePoNoIndex = dupHsCodePoNoforIndex;
							}
							dupHsCodePoNoforIndex++;
						}
						
					}
					index++;
				}
				
				Map<String,Object> licenseInsertMapData = licenseInsertMapList.get(dupPoNoIndex);
				License licenseItem = (License) licenseInsertMapData.get("License");
				List<LicenseCommodityItem> licenseCommodityItemList = (List<LicenseCommodityItem>) licenseInsertMapData.get("LicenseCommodityItem");
				List<LicenseGoodsItem> licenseGoodsItemList = (List<LicenseGoodsItem>) licenseInsertMapData.get("LicenseGoodsItem");
				
				if(dupPoNo) {
					
					// License
					licenseItem.setLicensePayAmountCur(licenseItem.getLicensePayAmountCur().add(new BigDecimal(data.get("LICENSE_PAY_AMOUNT_CUR").toString())));
					
					// Commodity
					licenseCommodityItem = new LicenseCommodityItem();
					licenseCommodityItem.setItemCode(Util.null2str(data.get("ITEM_CODE")));
					licenseCommodityItem.setDetailItemName(Util.null2str(data.get("DETAIL_ITEM_NAME")));
					licenseCommodityItem.setBrand(Util.null2str(data.get("BRAND")));
					licenseCommodityItem.setItemUrl(Util.null2str(data.get("ITEM_URL")));
					if(data.get("NWT") != null) {
						licenseCommodityItem.setNwt(new BigDecimal(data.get("NWT").toString()));
					}
					if(data.get("UNIT_PRICE_CUR") != null) {
						licenseCommodityItem.setUnitPriceCur(new BigDecimal(data.get("UNIT_PRICE_CUR").toString()));
					}
					if(data.get("PIECES") != null) {
						licenseCommodityItem.setPieces(Integer.parseInt(data.get("PIECES").toString()));
					}
					
					licenseCommodityItemList.add(licenseCommodityItem);
					
					// Goods
					if(!dupHsCodePoNo) {
						licenseGoodsItem.setItemCode(Util.null2str(data.get("ITEM_CODE")));
						licenseGoodsItem.setDetailItemName(Util.null2str(data.get("DETAIL_ITEM_NAME")));
						licenseGoodsItem.setBrand(Util.null2str(data.get("BRAND")));
						if(data.get("NWT") != null) {
							licenseGoodsItem.setNwt(new BigDecimal(data.get("NWT").toString()));
						}
						if(data.get("PIECES") != null) {
							licenseGoodsItem.setPieces(Integer.parseInt(data.get("PIECES").toString()));
						}
						licenseGoodsItem.setHsCode(Util.null2str(data.get("HS_CODE")));
						licenseGoodsItem.setOriginCountry(Util.null2str(data.get("ORIGIN_COUNTRY")));
						
						licenseGoodsItemList.add(licenseGoodsItem);
						
					} else {
						licenseGoodsItemList.get(dupHsCodePoNoIndex).setNwt(licenseGoodsItemList.get(dupHsCodePoNoIndex).getNwt().add(new BigDecimal(data.get("NWT").toString())));
						licenseGoodsItemList.get(dupHsCodePoNoIndex).setPieces(licenseGoodsItemList.get(dupHsCodePoNoIndex).getPieces() + Util.null2int(data.get("PIECES")));
					}

					Map<String,Object> updateLicenseInsertMapData = new  HashMap<>();
					updateLicenseInsertMapData.put("poNo", poNo);
					updateLicenseInsertMapData.put("License", licenseItem);
					updateLicenseInsertMapData.put("LicenseGoodsItem", licenseGoodsItemList);
					updateLicenseInsertMapData.put("LicenseCommodityItem", licenseCommodityItemList);
					
					licenseInsertMapList.set(dupPoNoIndex, updateLicenseInsertMapData);
					
					
				} else {
					/*** Commodity ***/
					licenseCommodityItem = new LicenseCommodityItem();
					licenseCommodityItem.setItemCode(Util.null2str(data.get("ITEM_CODE")));
					licenseCommodityItem.setDetailItemName(Util.null2str(data.get("DETAIL_ITEM_NAME")));
					licenseCommodityItem.setBrand(Util.null2str(data.get("BRAND")));
					licenseCommodityItem.setItemUrl(Util.null2str(data.get("ITEM_URL")));
					if(data.get("NWT") != null) {
						licenseCommodityItem.setNwt(new BigDecimal(data.get("NWT").toString()));
					}
					if(data.get("UNIT_PRICE_CUR") != null) {
						licenseCommodityItem.setUnitPriceCur(new BigDecimal(data.get("UNIT_PRICE_CUR").toString()));
					}
					if(data.get("PIECES") != null) {
						licenseCommodityItem.setPieces(Integer.parseInt(data.get("PIECES").toString()));
					}
					
					/*** Goods ***/
					licenseGoodsItem.setItemCode(Util.null2str(data.get("ITEM_CODE")));
					licenseGoodsItem.setDetailItemName(Util.null2str(data.get("DETAIL_ITEM_NAME")));
					licenseGoodsItem.setBrand(Util.null2str(data.get("BRAND")));
					if(data.get("NWT") != null) {
						licenseGoodsItem.setNwt(new BigDecimal(data.get("NWT").toString()));
					}
					if(data.get("PIECES") != null) {
						licenseGoodsItem.setPieces(Integer.parseInt(data.get("PIECES").toString()));
					}
					licenseGoodsItem.setHsCode(Util.null2str(data.get("HS_CODE")));
					licenseGoodsItem.setOriginCountry(Util.null2str(data.get("ORIGIN_COUNTRY")));
					
					/*** License ***/
					license.setPoNo(Util.null2str(data.get("PO_NO")));
					license.setHblNo(Util.null2str(data.get("HBL_NO")));
					if(data.get("LICENSE_PAY_AMOUNT_CUR") != null) {
						license.setLicensePayAmountCur(new BigDecimal(data.get("LICENSE_PAY_AMOUNT_CUR").toString()));
					}
					license.setCurrencyCode(Util.null2str(data.get("CURRENCY_CODE")));
					license.setConsigneeName(Util.null2str(data.get("CONSIGNEE_NAME")));
					if(data.get("LICENSE_PAY_AMOUNT_CUR") != null) {
						license.setLicensePayAmountCur(new BigDecimal(data.get("LICENSE_PAY_AMOUNT_CUR").toString()));
					}
					license.setDestCountry(Util.null2str(data.get("DEST_COUNTRY")));
					license.setDeliveryTypeCode(Util.null2str(data.get("DELIVERY_TYPE_CODE")));
					license.setDestPort(Util.null2str(data.get("DEST_PORT")));
					license.setConsigneeZipcode(Util.null2str(data.get("CONSIGNEE_ZIPCODE")));
					license.setConsigneeAddress(Util.null2str(data.get("CONSIGNEE_ADDRESS")));
					license.setConsigneeTel(Util.null2str(data.get("CONSIGNEE_TEL")));
					license.setMakerName(Util.null2str(data.get("MAKER_NAME")));
					license.setMakerBusinessId(Util.null2str(data.get("MAKER_BUSINESS_ID")));
					license.setMakerCustomsId(Util.null2str(data.get("MAKER_CUSTOMS_ID")));
					license.setMakerZipcode(Util.null2str(data.get("MAKER_ZIPCODE")));
					license.setIncoterms(Util.null2str(data.get("INCOTERMS")));
					if(data.get("DELIVERY_FEE_LOC") != null) {
						license.setDeliveryFeeLoc(new BigDecimal(data.get("DELIVERY_FEE_LOC").toString()));
					}
					if(data.get("INSURANCE_LOC") != null) {
						license.setInsuranceLoc(new BigDecimal(data.get("INSURANCE_LOC").toString()));
					}
					license.setLicensePayMethod(Util.null2str(data.get("LICENSE_PAY_METHOD")));
					license.setLicenseRemark(Util.null2str(data.get("LICENSE_REMARK")));
					
					/* 행 별, Data 별 SaveMap Set */
					
					List<LicenseCommodityItem> licenseCommodityItemListInsert = new ArrayList<>();
					licenseCommodityItemListInsert.add(licenseCommodityItem);
					licenseInsertMap.put("LicenseCommodityItem", licenseCommodityItemListInsert);
					
					List<LicenseGoodsItem> licenseGoodsItemListInsert = new ArrayList<>();
					licenseGoodsItemListInsert.add(licenseGoodsItem);
					licenseInsertMap.put("LicenseGoodsItem", licenseGoodsItemListInsert);
					
					licenseInsertMap.put("License", license);
					
					licenseInsertMap.put("poNo", poNo);
					
					// 행 별 SaveMapList Set
					licenseInsertMapList.add(licenseInsertMap);
				}
				
			} else {
				/*** Commodity ***/
				licenseCommodityItem = new LicenseCommodityItem();
				licenseCommodityItem.setItemCode(Util.null2str(data.get("ITEM_CODE")));
				licenseCommodityItem.setDetailItemName(Util.null2str(data.get("DETAIL_ITEM_NAME")));
				licenseCommodityItem.setBrand(Util.null2str(data.get("BRAND")));
				licenseCommodityItem.setItemUrl(Util.null2str(data.get("ITEM_URL")));
				if(data.get("NWT") != null) {
					licenseCommodityItem.setNwt(new BigDecimal(data.get("NWT").toString()));
				}
				if(data.get("UNIT_PRICE_CUR") != null) {
					licenseCommodityItem.setUnitPriceCur(new BigDecimal(data.get("UNIT_PRICE_CUR").toString()));
				}
				if(data.get("PIECES") != null) {
					licenseCommodityItem.setPieces(Integer.parseInt(data.get("PIECES").toString()));
				}
				
				/*** Goods ***/
				licenseGoodsItem.setItemCode(Util.null2str(data.get("ITEM_CODE")));
				licenseGoodsItem.setDetailItemName(Util.null2str(data.get("DETAIL_ITEM_NAME")));
				licenseGoodsItem.setBrand(Util.null2str(data.get("BRAND")));
				if(data.get("NWT") != null) {
					licenseGoodsItem.setNwt(new BigDecimal(data.get("NWT").toString()));
				}
				if(data.get("PIECES") != null) {
					licenseGoodsItem.setPieces(Integer.parseInt(data.get("PIECES").toString()));
				}
				licenseGoodsItem.setHsCode(Util.null2str(data.get("HS_CODE")));
				licenseGoodsItem.setOriginCountry(Util.null2str(data.get("ORIGIN_COUNTRY")));
				
				/*** License ***/
				license.setPoNo(Util.null2str(data.get("PO_NO")));
				license.setHblNo(Util.null2str(data.get("HBL_NO")));
				if(data.get("LICENSE_PAY_AMOUNT_CUR") != null) {
					license.setLicensePayAmountCur(new BigDecimal(data.get("LICENSE_PAY_AMOUNT_CUR").toString()));
				}
				license.setCurrencyCode(Util.null2str(data.get("CURRENCY_CODE")));
				license.setConsigneeName(Util.null2str(data.get("CONSIGNEE_NAME")));
				license.setDestCountry(Util.null2str(data.get("DEST_COUNTRY")));
				license.setDeliveryTypeCode(Util.null2str(data.get("DELIVERY_TYPE_CODE")));
				license.setDestPort(Util.null2str(data.get("DEST_PORT")));
				license.setConsigneeZipcode(Util.null2str(data.get("CONSIGNEE_ZIPCODE")));
				license.setConsigneeAddress(Util.null2str(data.get("CONSIGNEE_ADDRESS")));
				license.setConsigneeTel(Util.null2str(data.get("CONSIGNEE_TEL")));
				license.setMakerName(Util.null2str(data.get("MAKER_NAME")));
				license.setMakerBusinessId(Util.null2str(data.get("MAKER_BUSINESS_ID")));
				license.setMakerCustomsId(Util.null2str(data.get("MAKER_CUSTOMS_ID")));
				license.setMakerZipcode(Util.null2str(data.get("MAKER_ZIPCODE")));
				license.setIncoterms(Util.null2str(data.get("INCOTERMS")));
				if(data.get("DELIVERY_FEE_LOC") != null) {
					license.setDeliveryFeeLoc(new BigDecimal(data.get("DELIVERY_FEE_LOC").toString()));
				}
				if(data.get("INSURANCE_LOC") != null) {
					license.setInsuranceLoc(new BigDecimal(data.get("INSURANCE_LOC").toString()));
				}
				license.setLicensePayMethod(Util.null2str(data.get("LICENSE_PAY_METHOD")));
				license.setLicenseRemark(Util.null2str(data.get("LICENSE_REMARK")));
				
				/* 행 별, Data 별 SaveMap Set */
				
				List<LicenseCommodityItem> licenseCommodityItemList = new ArrayList<>();
				licenseCommodityItemList.add(licenseCommodityItem);
				licenseInsertMap.put("LicenseCommodityItem", licenseCommodityItemList);
				
				List<LicenseGoodsItem> licenseGoodsItemList = new ArrayList<>();
				licenseGoodsItemList.add(licenseGoodsItem);
				licenseInsertMap.put("LicenseGoodsItem", licenseGoodsItemList);
				
				licenseInsertMap.put("License", license);
				
				licenseInsertMap.put("poNo", poNo);
				
				// 행 별 SaveMapList Set
				licenseInsertMapList.add(licenseInsertMap);
			}
		}
		
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		
		for(Map<String, Object> licenseInsertMap : licenseInsertMapList) {
			License licenseInsert = (License) licenseInsertMap.get("License");
			List<LicenseGoodsItem> licenseGoodsInsertList = (List<LicenseGoodsItem>) licenseInsertMap.get("LicenseGoodsItem");
			List<LicenseCommodityItem> licenseCommodityInsertList = (List<LicenseCommodityItem>) licenseInsertMap.get("LicenseCommodityItem");
			
			/** License Set **/
			licenseInsert.setEntCode(licenseList.getEntCode());
			licenseInsert.setLicenseType(licenseList.getLicenseType());
			licenseInsert.setGmtTime(licenseList.getGmtTime());
			licenseInsert.setUpdateUserId(licenseList.getRegisterUserId());
			licenseInsert.setDateFormat(licenseList.getDateFormat());
			
			/** LicenseGoods Set **/
			if(licenseGoodsInsertList != null && !licenseGoodsInsertList.isEmpty()) {
				licenseInsert.setLicenseGoodsItemList(licenseGoodsInsertList);
			}
			
			/** LicenseCommodity Set **/
			if(licenseCommodityInsertList != null && !licenseCommodityInsertList.isEmpty()) {
				licenseInsert.setLicenseCommodityItemList(licenseCommodityInsertList);
			}
			
			this.insertLicense(licenseInsert, request);
		}
		
		return licenseSaveResult;
	}
	
	/**
	 * E면장 List(main) 조회
	 * @param SearchLicenseList
	 * @return
	 * @throws Exception
	 */	
	
	public LicenseList selectLicenseExcelList(SearchLicenseList search) throws Exception {
		LicenseList licenseList = new LicenseList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "PO_DATE|REGISTER_DATE_TIME|SEND_DATE_TIME|RESULT_DATE_TIME|RO_DATE", "srchLicenseType", "PO_NO|EFN_PO_NO");
		List<License> list  = licenseMapper.selectLicenseExcelList(search);
		licenseList.setLicenseList(list);
		
		if(list.size() > 0) {
			licenseList.setTotal(list.get(0).getTotal());
		}else {
			licenseList.setTotal(0);
		}
		
		return licenseList; 
	}

	/**
	 * X면장 KTNET 전송
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public LicenseSaveResult sendXLicense(SearchLicenseBatchUpdate search) throws Exception {
		LicenseSaveResult licenseSaveResult = new LicenseSaveResult();
		String targetData = Util.null2str(search.getTargetData());
		List<Map<String, Object>> sendList = new ArrayList<Map<String,Object>>();
		
		Map<String, Object> tmpMap = null;
		IndexApi indexApi = null;
		DocumentApi documentApi = null;
		String logId = ""; // ES 로그용 아이디
		String index = ""; // ES 로그용 index
		int seq = 1;
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "srchDateFormat|gmtTime", true, "srchDateGbn", "PO_DATE|REGISTER_DATE_TIME|SEND_DATE_TIME|RESULT_DATE_TIME|RO_DATE", "srchLicenseType", "PO_NO|EFN_PO_NO");
		//검색건
		if(targetData.equals("srchData")){
			sendList = licenseMapper.selectLicenseInfoList(search);
		//선택건	
		}else {
			sendList = licenseMapper.selectLicenseInfoList(search);
		}
		
		tmpMap = new HashMap<String, Object>();
		tmpMap.put("ifServiceCode", "KTNET-X-LICENSE");
		tmpMap.put("entCode", search.getEntCode());
		tmpMap.put("entBusinessCode", search.getSrchEntBusinessCode());
		tmpMap.put("customerCode",search.getSrchCustCodeLv1());
		// 사업장 API 키값 정보 조회
		Map<String,Object> apiMap = licenseMapper.selectBusinessApiKeyInfo(tmpMap);
		if(apiMap == null) {
			licenseSaveResult.setResultCode("notBusinessKey");
			return licenseSaveResult;
		}
		// 상위업체 API 키값 정보 조회
		Map<String,Object> apiCustomerMap = licenseMapper.selectCustomerApiKeyInfo(tmpMap);
		if(apiCustomerMap == null) {
			licenseSaveResult.setResultCode("notCustomerKey");
			return licenseSaveResult;
		}
		String sellerId = Util.null2str(apiCustomerMap.get("IF_LOGIN_ID"));
		String apiConsumerKey = Util.null2str(apiCustomerMap.get("IF_LOGIN_PW")).replace("\r", "").replace("\n", "");
		String apiAgentKey = Util.null2str(apiMap.get("IF_WEB_PWD")).replace("\r", "").replace("\n", "");
		
		// 결과를 담을 List<JSONObject>를 생성합니다.
        List<JSONObject> jsonObjectList = new ArrayList<>();
        
		int batchSize = 50;
		int totalSize = sendList.size();
		
		for(int i=0; i < totalSize; i+= batchSize) {
			
			JSONArray jsonArray = new JSONArray();
			
			for (int j = i; j < i + batchSize && j < totalSize; j++) {
				Map<String, Object> dataMap = sendList.get(j);
				
				/*****************  API 전송 데이터 Parsing Start *****************/
				
				JSONObject jsonObj = new JSONObject();
				
				jsonObj.put("MallId", sellerId);
				jsonObj.put("SellerPartyId", sellerId);
				jsonObj.put("OrderNo", dataMap.get("PO_NO"));
				jsonObj.put("AutoDeclaration", "Y");
				jsonObj.put("DeliveryStatus", "BD");
				jsonObj.put("DeliveryMeans", "KTNT");
				jsonObj.put("InvoiceNo", dataMap.get("DEST_DELIVERY_NO"));
				jsonObj.put("RequestType", "N");
				jsonObj.put("PaymentAmount", dataMap.get("LICENSE_PAY_AMOUNT_CUR"));
				jsonObj.put("PaymentCurrencyCode", dataMap.get("CURRENCY_CODE"));
				jsonObj.put("DestinationCountryCode", dataMap.get("DEST_COUNTRY"));
				String consigneeName = transStr(Util.null2str(dataMap.get("CONSIGNEE_NAME")));
				jsonObj.put("BuyerPartyEngName", consigneeName);
				jsonObj.put("BuyerPartyAddress", dataMap.get("CONSIGNEE_ADDRESS"));
				jsonObj.put("TotalPackageQuantity", dataMap.get("PIECES"));
				jsonObj.put("TotalPackageWeight", dataMap.get("GWT"));
				jsonObj.put("SellerPartyCompanyName", dataMap.get("SHIPPER_BUSINESS_NAME"));
				jsonObj.put("SellerPartyRegistNo", dataMap.get("SHIPPER_BUSINESS_ID"));
				jsonObj.put("SellerPartyAddress", Util.null2str(dataMap.get("SHIPPER_BUSINESS_ADDRESS"))+ Util.null2str(dataMap.get("SHIPPER_BUSINESS_ADDRESSA")));
				jsonObj.put("SellerPartyCeoName", dataMap.get("SHIPPER_BUSINESS_PRESIDENT"));
				jsonObj.put("SellerPartyZipCode", dataMap.get("SHIPPER_BUSINESS_ZIPCODE"));
				jsonObj.put("SellerPartyCustomsId", dataMap.get("SHIPPER_BUSINESS_CUSTOMS_ID"));
				jsonObj.put("SellerPartyDeclarationId", dataMap.get("DECLARATION_REG_NO"));
				jsonObj.put("GoodsLocZipCode", dataMap.get("GOODS_LOC_ZIPCODE"));
				jsonObj.put("GoodsLocAddress", dataMap.get("GOODS_LOC_ADDRESS"));
				jsonObj.put("GoodsLocAddress2", dataMap.get("BWH_NAME"));
				jsonObj.put("LoadingLocPortCode", "ICN");

				if("AIR".equals(dataMap.get("BOUND"))) {
					jsonObj.put("TransportMeansCode", "40");
				}else if("SEA".equals(dataMap.get("BOUND"))) {
					jsonObj.put("TransportMeansCode", "10");
				}
				
				jsonObj.put("CustomsCode", dataMap.get("LICENSE_CUSTOMS_CODE"));
				jsonObj.put("CustomsDeptCode", dataMap.get("LICENSE_CUSTOMS_DPT_CODE"));
				jsonObj.put("ManufactureLocCode", "999");
				jsonObj.put("PaymentTypeCode", dataMap.get("LICENSE_PAY_METHOD"));
				jsonObj.put("ExportPartyTypeCode", "C");
				jsonObj.put("CustomsTaxRefundRequest", "NO");
				jsonObj.put("InspectionCode", "A");
				jsonObj.put("MakerCustomsId", dataMap.get("MAKER_CUSTOMS_ID"));
				jsonObj.put("MakerName", dataMap.get("MAKER_NAME"));
				jsonObj.put("MakerZipCode", dataMap.get("MAKER_ZIPCODE"));
				jsonObj.put("DeliveryTermsCode", dataMap.get("INCOTERMS"));
				//2024-10-14 운임,보험료 0으로 전송 
//				if(Util.null2dbl(dataMap.get("DELIVERY_FEE_LOC")) != 0) {
//					jsonObj.put("FreightAmount", Util.null2int(dataMap.get("DELIVERY_FEE_LOC")));
//				}
//				if(Util.null2dbl(dataMap.get("INSURANCE_LOC")) != 0) {
//					jsonObj.put("InsuranceAmount", Util.null2int(dataMap.get("INSURANCE_LOC")));
//				}
				jsonObj.put("FreightAmount", 0);
				jsonObj.put("InsuranceAmount", 0);
				
				jsonObj.put("ApplicationNotice", dataMap.get("LICENSE_REMARK"));
				
				SearchLicenseList searchList = new SearchLicenseList();
				List<LicenseCommodityItem> goodsItemList = new ArrayList<LicenseCommodityItem>();
				searchList.setLicenseJobNo((String)dataMap.get("LICENSE_JOB_NO"));
				
				//란 상세정보 조회
				goodsItemList = licenseMapper.selectLicenseCommodityItemList(searchList);
				
				if(goodsItemList != null) {
					
					JSONArray itemArray = new JSONArray();
					
					for(int y=0; y<goodsItemList.size(); y++) {
						JSONObject itemObj = new JSONObject();
						
						itemObj.put("ItemNo", goodsItemList.get(y).getItemCode());
						itemObj.put("OriginLocCode", goodsItemList.get(y).getOriginCountry());
						// 첫번째 란 규격에 면장 중량을 넣고 나머지는 0만 넣는다.
						if(y == 0) {
							itemObj.put("NetWeight", dataMap.get("GWT"));
						}else {
							itemObj.put("NetWeight", 0);
						}
						itemObj.put("Quantity", goodsItemList.get(y).getPieces());
						itemObj.put("QuantityUnitCode", goodsItemList.get(y).getUnitPieces());
						itemObj.put("BrandName", goodsItemList.get(y).getBrand());
						itemObj.put("GoodsDesc", goodsItemList.get(y).getDetailItemName());
						itemObj.put("GoodsName", goodsItemList.get(y).getItemSize());
						itemObj.put("HSCode", goodsItemList.get(y).getHsCode());
						itemObj.put("DeclarationPrice", goodsItemList.get(y).getAmountCur());
						itemObj.put("UnitPrice", goodsItemList.get(y).getUnitPriceCur());
						itemObj.put("DeclPriceCurrencyCode", goodsItemList.get(y).getCurrencyCode());
						
						itemArray.add(itemObj);
					}
					
					jsonObj.put("Items", itemArray);
				}
				jsonArray.add(jsonObj);
            }
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("DocCount", sendList.size());
			jsonObject.put("DocList", jsonArray);
			
			jsonObjectList.add(jsonObject);
			
			/*****************  API 전송 데이터 Parsing End *****************/
		}
		
		if(jsonObjectList != null && jsonObjectList.size() > 0) {
			for(int i=0; i<jsonObjectList.size(); i++) {
				
				JSONObject jsonObject = jsonObjectList.get(i);
				JSONArray jsonArr = (JSONArray) jsonObject.get("DocList");
				
				
				/******************  KT NET 전송 *******************/
				// 운영 Url
				String ApiUrl = "https://cbtapi.utradehub.or.kr/api/export/declaration.do";
				// Test Url
//				String ApiUrl = "https://testcbtapi.utradehub.or.kr/api/export/declaration.do";
				URL url = new URL(ApiUrl);
				BufferedReader in = null;
				String sb = "";
				
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setReadTimeout(15000);
		        conn.setRequestProperty("Content-Type", "application/json");
		        conn.setRequestProperty("api_agent_key", apiAgentKey);
		        conn.setRequestProperty("api_consumer_key", apiConsumerKey);
		        conn.setDoOutput(true);
				
		        try (OutputStream os = conn.getOutputStream()) {
		            os.write(jsonObject.toString().getBytes());
		            os.flush();
		        }
		        
		        // 응답 코드 확인
		        int responseCode = conn.getResponseCode();
		        log.info("Response Code: " + responseCode);
		        
		        if (responseCode == 200) { // HTTP OK
		        	in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		        }else { // 오류 발생 시
		            in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
		        }
		        
				while(in.ready()) {
					sb = in.readLine();
				}
				
				//결과 값을 파싱
				JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
				JSONObject responseInfo = (JSONObject)jsonParser.parse(sb);
				log.info("************ Receipt Process responseInfo*************"+responseInfo);
				
				JSONArray responseArr = (JSONArray) responseInfo.get("ResultList");
				if(responseArr != null && responseArr.size() > 0) {
					for(int x=0; x<responseArr.size(); x++) {
						JSONObject obj = (JSONObject) responseArr.get(x);
						String resultCode = "";
						String resultTypeCode = Util.null2str(obj.get("ResultTypeCode"));
						String errorDesc = "";
						
						if("220".equals(resultTypeCode)) {
							resultCode = "Y";
							errorDesc = "";
						}else if("422".equals(resultTypeCode)){
							errorDesc = Util.null2str(obj.get("ErrorDetail").toString());
							resultCode = "N";
						}else {
							errorDesc = Util.null2str(obj.get("ErrorDescription"));
							resultCode = "N";
						}
						
						// 상태값을 업데이트 해야할 PONO를 찾아야함. API에서 PONO를 리턴해주지않고 SEQ 번호를 리턴해줌
						JSONObject poObj = (JSONObject) jsonArr.get(Util.null2int((obj.get("OrgSeq"))));
						String poNo = Util.null2str(poObj.get("OrderNo"));
						
						
						Map<String,Object> updateMap = new HashMap<String, Object>();
						updateMap.put("sendResultCode", resultCode);
						updateMap.put("sendResultErrorDesc", errorDesc);
						updateMap.put("poNo", poNo);
						updateMap.put("gmtTime",search.getGmtTime());
						updateMap.put("updateUserId", search.getUpdateUserId());
						updateMap.put("licenseType", "X");
						licenseMapper.updateLicenseSendResult(updateMap);
						
					}
				}
				
				/******************  KT NET 전송 End *******************/
				
		        // history 등록 
				tmpMap = new HashMap<String, Object>();
				logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
				index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
				
				String dataId = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++);
				
				String filePath = customProperties.getBackupDir() + "api/xLicense/" + search.getEntCode() + "/" + DateTime.getDate("yyyy-MM") + "/";
				String fileName = dataId + ".json";
				FileUtil.fileWrite(jsonObject.toString(), filePath, fileName);
				
				// 처리결과에 따른 로그 기록
		        tmpMap.clear();
				tmpMap.put("ent_code", search.getEntCode()); 		 					// 도입사코드
				tmpMap.put("customer_code", ""); 		 				// 고객코드
				tmpMap.put("solution", "ECM");      		 				// SOLUTION
				tmpMap.put("bound", "");		 	 					// 바운드 (AIR)
				tmpMap.put("bound_type", "EXP");	 					// 바운드타입
				tmpMap.put("hbl_no", "");				 				// HBL_NO
				tmpMap.put("if_service_code", "ECM");
				tmpMap.put("if_doc_code", "SEND_XLICENSE");
				tmpMap.put("if_doc_name", "X면장전송");
				tmpMap.put("if_doc_reference_no", "");	 					// 문서번호 
				tmpMap.put("if_vender", "");								// 중개사업자
				tmpMap.put("if_send_recv_type", "S");						// 송수신타입(S:SEND, R:RECV)
				tmpMap.put("if_send_recv_method", "");						// 송수신방법
				tmpMap.put("if_file_path", filePath);			 				// 파일경로
				tmpMap.put("if_file_name", fileName);			 			// 파일명
				tmpMap.put("if_status", "S");			 				// 처리상태
				tmpMap.put("if_status_info", "");		 				// 송수신상태_설명
				tmpMap.put("if_sender_id", "");							// 송신식별자
				tmpMap.put("if_sender_name", "");		 					// 송신자명
				tmpMap.put("if_receiver_id", "");		 					// 수신식별자
				tmpMap.put("if_receiver_name", "KTNET");	     					// 수신자명
				tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));		// DATE
				tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));	// DATE_TIME
				tmpMap.put("if_remark", "");			 			// 비고( 에러로그 등)
				tmpMap.put("register_user_id", "API");
				tmpMap.put("register_date_time", System.currentTimeMillis());
				tmpMap.put("es_job_no", logId);
				
				indexApi = new IndexApi(client);
				if (!indexApi.existIndex(index))	indexApi.createIndex(index);
				documentApi = new DocumentApi(client);
				documentApi.createDocument(index, logId, tmpMap);
			}
		}
		return licenseSaveResult;
	}

	/**
	 * 면장 건수 조회
	 * @param search
	 * @return
	 * @throws Exception 
	 */
	public LicenseSaveResult selectLienseCount(SearchLicenseBatchUpdate search) throws Exception {
		LicenseSaveResult licenseSaveResult = new LicenseSaveResult();
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "PO_DATE|REGISTER_DATE_TIME|SEND_DATE_TIME|RESULT_DATE_TIME|RO_DATE", "srchLicenseType", "PO_NO|EFN_PO_NO");
		licenseSaveResult.setResultCode(licenseMapper.selectLienseCount(search));
		
		return licenseSaveResult;
	}

	/**
	 * E면장 KTNET 등록 API 
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public LicenseSaveResult sendELicenseCreate(SearchLicenseBatchUpdate search) throws Exception {
		LicenseSaveResult licenseSaveResult = new LicenseSaveResult();
		String targetData = Util.null2str(search.getTargetData());
		List<Map<String, Object>> sendList = new ArrayList<Map<String,Object>>();
		
		Map<String, Object> tmpMap = null;
		IndexApi indexApi = null;
		DocumentApi documentApi = null;
		String logId = ""; // ES 로그용 아이디
		String index = ""; // ES 로그용 index
		int seq = 1;
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "srchDateFormat|gmtTime", true, "srchDateGbn", "PO_DATE|REGISTER_DATE_TIME|SEND_DATE_TIME|RESULT_DATE_TIME|RO_DATE", "srchLicenseType", "PO_NO|EFN_PO_NO");
		//검색건
		if(targetData.equals("srchData")){
			sendList = licenseMapper.selectLicenseInfoList(search);
		//선택건	
		}else {
			sendList = licenseMapper.selectLicenseInfoList(search);
		}
		
		tmpMap = new HashMap<String, Object>();
		tmpMap.put("ifServiceCode", "KTNET-E-LICENSE");
		tmpMap.put("entCode", search.getEntCode());
		tmpMap.put("entBusinessCode", search.getSrchEntBusinessCode());
		tmpMap.put("customerCode",search.getSrchCustCodeLv1());
		// 사업장 API 키값 정보 조회
		Map<String,Object> apiMap = licenseMapper.selectBusinessApiKeyInfo(tmpMap);
		if(apiMap == null) {
			licenseSaveResult.setResultCode("notBusinessKey");
			return licenseSaveResult;
		}
		
		// 상위업체 API 키값 정보 조회
		Map<String,Object> apiCustomerMap = licenseMapper.selectCustomerApiKeyInfo(tmpMap);
		if(apiCustomerMap == null) {
			licenseSaveResult.setResultCode("notCustomerKey");
			return licenseSaveResult;
		}
		String sellerId = Util.null2str(apiCustomerMap.get("IF_LOGIN_ID"));
		String apiConsumerKey = Util.null2str(apiCustomerMap.get("IF_LOGIN_PW")).replace("\r", "").replace("\n", "");
		String apiAgentKey = Util.null2str(apiMap.get("IF_WEB_PWD")).replace("\r", "").replace("\n", "");
		
		// 결과를 담을 List<JSONObject>를 생성합니다.
        List<JSONObject> jsonObjectList = new ArrayList<>();
        
		int batchSize = 50;
		int totalSize = sendList.size();
		
		for(int i=0; i < totalSize; i+= batchSize) {
			
			JSONArray jsonArray = new JSONArray();
			
			for (int j = i; j < i + batchSize && j < totalSize; j++) {
				Map<String, Object> dataMap = sendList.get(j);
				
				/*****************  API 전송 데이터 Parsing Start *****************/
				
				JSONObject jsonObj = new JSONObject();
				
				jsonObj.put("RequestId", sellerId);
				jsonObj.put("CustomsOfficerBusinessNumber", dataMap.get("SHIPPER_BUSINESS_ID"));
				jsonObj.put("OrderNumber", dataMap.get("PO_NO"));
				jsonObj.put("ValueAmount", dataMap.get("LICENSE_PAY_AMOUNT_CUR"));
				jsonObj.put("ValueAmountCurrencyCode", dataMap.get("CURRENCY_CODE"));
				jsonObj.put("SettlementConditionCode", dataMap.get("LICENSE_PAY_METHOD"));
				jsonObj.put("DestinationCountryCode", dataMap.get("DEST_COUNTRY"));
				String consigneeName = transStr(Util.null2str(dataMap.get("CONSIGNEE_NAME")));
				jsonObj.put("BuyerName", consigneeName);
				jsonObj.put("ExporterName", dataMap.get("SHIPPER_BUSINESS_NAME"));
				jsonObj.put("ExporterRepresentativeName", dataMap.get("SHIPPER_BUSINESS_PRESIDENT"));
				jsonObj.put("ExporterBusinessNumber", dataMap.get("LICENSE_SEND_BUSINESS_ID"));
				jsonObj.put("ExporterCustomsClearanceCode", dataMap.get("AGENT_CUSTOMS_ID"));
				jsonObj.put("ExporterPostcodeID", dataMap.get("SHIPPER_BUSINESS_ZIPCODE"));
				jsonObj.put("ExporterDescription", Util.null2str(dataMap.get("SHIPPER_BUSINESS_ADDRESS")));
				jsonObj.put("ExporterLine", Util.null2str(dataMap.get("SHIPPER_BUSINESS_ADDRESSA")));
				jsonObj.put("StatementDescription", Util.null2str(dataMap.get("LICENSE_REMARK")));
				jsonObj.put("DeliveryTermsCode", Util.null2str(dataMap.get("INCOTERMS")));
				jsonObj.put("FreightChargeAmount", "0");
				jsonObj.put("WaybillNo", Util.null2str(dataMap.get("HBL_NO")));
				jsonObj.put("LoadingLocPortCode", "ICN");
				if("AIR".equals(dataMap.get("BOUND"))) {
					jsonObj.put("TransportationMethodCode", "40");
				}else if("SEA".equals(dataMap.get("BOUND"))) {
					jsonObj.put("TransportationMethodCode", "10");
				}
				jsonObj.put("ExpressCompanyCode", Util.null2str(dataMap.get("EXPRESS_SIGN")));
				jsonObj.put("CustomOrganizationID", Util.null2str(dataMap.get("LICENSE_CUSTOMS_CODE")));
				jsonObj.put("CustomDepartmentID", Util.null2str(dataMap.get("LICENSE_CUSTOMS_DPT_CODE")));
				jsonObj.put("GoodsLocZipCode", Util.null2str(dataMap.get("GOODS_LOC_ZIPCODE")));
				jsonObj.put("GoodsLocAddress", Util.null2str(dataMap.get("GOODS_LOC_ADDRESS")));
				
				//아래 TB_ECM_OD_LICENSE INSERT 작업에서 odJobNo가 필요해서 넣었음
				jsonObj.put("odJobNo", Util.null2str(dataMap.get("OD_JOB_NO")));
				
				SearchLicenseList searchList = new SearchLicenseList();
				List<LicenseCommodityItem> goodsItemList = new ArrayList<LicenseCommodityItem>();
				searchList.setLicenseJobNo((String)dataMap.get("LICENSE_JOB_NO"));
				
				//란 규격 상세정보 조회
				goodsItemList = licenseMapper.selectLicenseCommodityItemList(searchList);
				
				if(goodsItemList != null) {
					
					JSONArray itemArray = new JSONArray();
					
					for(int y=0; y<goodsItemList.size(); y++) {
						JSONObject itemObj = new JSONObject();
						itemObj.put("ClassificationID", goodsItemList.get(y).getHsCode());
						itemObj.put("CommodityDescription", goodsItemList.get(y).getDetailItemName());
						itemObj.put("CargoDescription", goodsItemList.get(y).getItemSize());
						itemObj.put("UnitPriceAmount", goodsItemList.get(y).getUnitPriceCur());
						itemObj.put("CountQuantity", goodsItemList.get(y).getPieces());
						itemObj.put("Weight", goodsItemList.get(y).getNwt());
						itemObj.put("CommodityValueAmount", goodsItemList.get(y).getAdjustAmountLoc());
						itemObj.put("ManufacturerName", dataMap.get("MAKER_NAME"));
						itemObj.put("ManufacturerBusinessPlaceSerialNumber", "1");
						itemObj.put("ExporterTypeCode", "C");
						itemObj.put("MallGoodsID", goodsItemList.get(y).getItemCode());
						
						itemArray.add(itemObj);
					}
					
					jsonObj.put("GoodsList", itemArray);
				}
				jsonArray.add(jsonObj);
            }
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("DocCount", jsonArray.size());
			jsonObject.put("DocList", jsonArray);
			
			jsonObjectList.add(jsonObject);
			
			/*****************  API 전송 데이터 Parsing End *****************/
		}
		
		if(jsonObjectList != null && jsonObjectList.size() > 0) {
			for(int i=0; i<jsonObjectList.size(); i++) {
				
				JSONObject jsonObject = jsonObjectList.get(i);
				JSONArray jsonArr = (JSONArray) jsonObject.get("DocList");
				
				
				/******************  KT NET 전송 *******************/
				// 운영 Url
				String ApiUrl = "https://cbtapi.utradehub.or.kr/api/smed/export/declaration.do";
				// Test Url
//				String ApiUrl = "https://testcbtapi.utradehub.or.kr/api/smed/export/declaration.do";
				URL url = new URL(ApiUrl);
				BufferedReader in = null;
				String sb = "";
				
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setReadTimeout(15000);
		        conn.setRequestProperty("Content-Type", "application/json");
		        conn.setRequestProperty("api_agent_key", apiAgentKey);
		        conn.setRequestProperty("api_consumer_key", apiConsumerKey);
		        conn.setDoOutput(true);
				
		        try (OutputStream os = conn.getOutputStream()) {
		            os.write(jsonObject.toString().getBytes());
		            os.flush();
		        }
		        
		        // 응답 코드 확인
		        int responseCode = conn.getResponseCode();
		        log.info("Response Code: " + responseCode);
		        
		        if (responseCode == 200) { // HTTP OK
		        	in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		        }else { // 오류 발생 시
		            in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
		        }
		        
				while(in.ready()) {
					sb = in.readLine();
				}
				
				//결과 값을 파싱
				JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
				JSONObject responseInfo = (JSONObject)jsonParser.parse(sb);
				log.info("************ Receipt Process responseInfo*************"+responseInfo);
				
				JSONArray responseArr = (JSONArray) responseInfo.get("ResultList");
				if(responseArr != null && responseArr.size() > 0) {
					for(int x=0; x<responseArr.size(); x++) {
						JSONObject obj = (JSONObject) responseArr.get(x);
						String resultCode = "";
						String resultTypeCode = Util.null2str(obj.get("ResultTypeCode"));
						String errorDesc = "";
						String efnPoNo = "";
						if("220".equals(resultTypeCode)) {
							resultCode = "Y";
							errorDesc = "";
							efnPoNo = Util.null2str(obj.get("DeclarationNo"));
						}else if("422".equals(resultTypeCode)){
							errorDesc = Util.null2str(obj.get("ErrorDetail").toString());
							resultCode = "N";
						}else {
							errorDesc = Util.null2str(obj.get("ErrorDescription"));
							resultCode = "N";
						}
						
						// 상태값을 업데이트 해야할 PONO를 찾아야함. API에서 PONO를 리턴해주지않고 SEQ 번호를 리턴해줌
						JSONObject poObj = (JSONObject) jsonArr.get(Util.null2int((obj.get("OrgSeq"))));
						String poNo = Util.null2str(poObj.get("OrderNumber"));
						String odJobNo = Util.null2str(poObj.get("odJobNo"));
						
						
						Map<String,Object> updateMap = new HashMap<String, Object>();
						updateMap.put("sendResultCode", resultCode);
						updateMap.put("sendResultErrorDesc", errorDesc);
						updateMap.put("poNo", poNo);
						updateMap.put("gmtTime",search.getGmtTime());
						updateMap.put("efnPoNo", efnPoNo);
						updateMap.put("updateUserId", search.getUpdateUserId());
						updateMap.put("licenseType", "E");
						licenseMapper.updateLicenseSendResult(updateMap);
						
						
						// TB_ECM_OD_LICENSE 에도 면장 INSERT
						if("220".equals(resultTypeCode)) {
							
							SearchLicenseList searchLicenseList = new SearchLicenseList();
							searchLicenseList.setPoNo(poNo);
							searchLicenseList.setEntCode(search.getEntCode());
							Map<String,Object> orderMap = new HashMap<String, Object>();
							License licenseVo = licenseMapper.selectLicense(searchLicenseList);
							if(licenseVo != null) {
								
								orderMap.put("gwt", licenseVo.getGwt());
								orderMap.put("pieces", licenseVo.getPieces());
								orderMap.put("unitPieces", licenseVo.getUnitPieces());
								orderMap.put("poNo", licenseVo.getPoNo());
								orderMap.put("hblNo", licenseVo.getHblNo());
								orderMap.put("odJobNo", odJobNo);
								orderMap.put("efnSenderCode", licenseVo.getDeclarationRegNo());
								
								orderMap.put("licenseNo", efnPoNo);
								orderMap.put("updateUserId", search.getUpdateUserId());
								orderMap.put("gmtTime", search.getGmtTime());
								licenseMapper.deleteLicenseOd(orderMap);
								licenseMapper.insertLicenseOd(orderMap);
							}
						}
						
					}
				}
				
				/******************  KT NET 전송 End *******************/
				
		        // history 등록 
				tmpMap = new HashMap<String, Object>();
				logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
				index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
				
				String dataId = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++);
				
				String filePath = customProperties.getBackupDir() + "api/xLicense/" + search.getEntCode() + "/" + DateTime.getDate("yyyy-MM") + "/";
				String fileName = dataId + ".json";
				FileUtil.fileWrite(jsonObject.toString(), filePath, fileName);
				
				// 처리결과에 따른 로그 기록
		        tmpMap.clear();
				tmpMap.put("ent_code", search.getEntCode()); 		 					// 도입사코드
				tmpMap.put("customer_code", ""); 		 				// 고객코드
				tmpMap.put("solution", "ECM");      		 				// SOLUTION
				tmpMap.put("bound", "");		 	 					// 바운드 (AIR)
				tmpMap.put("bound_type", "EXP");	 					// 바운드타입
				tmpMap.put("hbl_no", "");				 				// HBL_NO
				tmpMap.put("if_service_code", "ECM");
				tmpMap.put("if_doc_code", "SEND_ELICENSE");
				tmpMap.put("if_doc_name", "E면장등록");
				tmpMap.put("if_doc_reference_no", "");	 					// 문서번호 
				tmpMap.put("if_vender", "");								// 중개사업자
				tmpMap.put("if_send_recv_type", "S");						// 송수신타입(S:SEND, R:RECV)
				tmpMap.put("if_send_recv_method", "");						// 송수신방법
				tmpMap.put("if_file_path", filePath);			 				// 파일경로
				tmpMap.put("if_file_name", fileName);			 			// 파일명
				tmpMap.put("if_status", "S");			 				// 처리상태
				tmpMap.put("if_status_info", "");		 				// 송수신상태_설명
				tmpMap.put("if_sender_id", "");							// 송신식별자
				tmpMap.put("if_sender_name", "");		 					// 송신자명
				tmpMap.put("if_receiver_id", "");		 					// 수신식별자
				tmpMap.put("if_receiver_name", "KTNET");	     					// 수신자명
				tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));		// DATE
				tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));	// DATE_TIME
				tmpMap.put("if_remark", "");			 			// 비고( 에러로그 등)
				tmpMap.put("register_user_id", "API");
				tmpMap.put("register_date_time", System.currentTimeMillis());
				tmpMap.put("es_job_no", logId);
				
				indexApi = new IndexApi(client);
				if (!indexApi.existIndex(index))	indexApi.createIndex(index);
				documentApi = new DocumentApi(client);
				documentApi.createDocument(index, logId, tmpMap);
			}
		}
		
		search.setApiConsumerId(sellerId);
		search.setApiConsumerKey(apiConsumerKey);
		
		/** E면장 KT-NET 전송 API */
		sendELicenseSend(search);
		
		return licenseSaveResult;
	}
	
	/**
	 * E면장 KTNET 전송 API
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public LicenseSaveResult sendELicenseSend(SearchLicenseBatchUpdate search) throws Exception {
		
		LicenseSaveResult licenseSaveResult = new LicenseSaveResult();
		Map<String,Object> srchMap = new HashMap<String, Object>();
		List<Map<String,Object>> licenselist = new ArrayList<Map<String,Object>>();
		Map<String, Object> tmpMap = null;
		IndexApi indexApi = null;
		DocumentApi documentApi = null;
		String logId = ""; // ES 로그용 아이디
		String index = ""; // ES 로그용 index
		int seq = 1;
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "gmtTime", false, null, null, null, null);
		
		srchMap.put("licenseType", "E");
		srchMap.put("sendResultCode", "Y");
		
		licenselist = licenseMapper.selectLicenseSendList(srchMap);
		
		if(licenselist != null && licenselist.size() > 0) {
			
			// 결과를 담을 List<JSONObject>를 생성합니다.
	        List<JSONObject> jsonObjectList = new ArrayList<>();
	        
			int batchSize = 100;
			int totalSize = licenselist.size();
			
			for(int i=0; i < totalSize; i+= batchSize) {
				
				JSONArray jsonArray = new JSONArray();
				
				for (int j = i; j < i + batchSize && j < totalSize; j++) {
					Map<String, Object> dataMap = licenselist.get(j);
					
					/*****************  API 전송 데이터 Parsing Start *****************/
					
					JSONObject jsonObj = new JSONObject();
					
					jsonObj.put("RequestId", search.getApiConsumerId());
					jsonObj.put("DeclarationNo", dataMap.get("EFN_PO_NO"));
					jsonObj.put("poNo",dataMap.get("PO_NO"));
					
					jsonArray.add(jsonObj);
				}
				
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("DocCount", jsonArray.size());
				jsonObject.put("DocList", jsonArray);
				
				jsonObjectList.add(jsonObject);
			}	
			
			/*****************  API 전송 데이터 Parsing End *****************/

			if(jsonObjectList != null && jsonObjectList.size() > 0) {
				for(int i=0; i<jsonObjectList.size(); i++) {
					
					
					JSONObject jsonObject = jsonObjectList.get(i);
					JSONArray jsonArr = (JSONArray) jsonObject.get("DocList");
					
					
					/******************  KT NET 전송 *******************/
					// 운영 Url
					String ApiUrl = "https://cbtapi.utradehub.or.kr/api/smed/export/declarationSend.do";
					// Test Url
//					String ApiUrl = "https://testcbtapi.utradehub.or.kr/api/smed/export/declarationSend.do";
					URL url = new URL(ApiUrl);
					BufferedReader in = null;
					String sb = "";
					
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("POST");
					conn.setReadTimeout(15000);
			        conn.setRequestProperty("Content-Type", "application/json");
			        conn.setRequestProperty("api_consumer_key", search.getApiConsumerKey());
			        conn.setDoOutput(true);
					
			        try (OutputStream os = conn.getOutputStream()) {
			            os.write(jsonObject.toString().getBytes());
			            os.flush();
			        }
			        
			        // 응답 코드 확인
			        int responseCode = conn.getResponseCode();
			        log.info("Response Code: " + responseCode);
			        
			        if (responseCode == 200) { // HTTP OK
			        	in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			        }else { // 오류 발생 시
			            in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
			        }
			        
					while(in.ready()) {
						sb = in.readLine();
					}
					
					//결과 값을 파싱
					JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
					JSONObject responseInfo = (JSONObject)jsonParser.parse(sb);
					log.info("************ Receipt Process responseInfo*************"+responseInfo);
					
					JSONArray responseArr = (JSONArray) responseInfo.get("ResultList");
					if(responseArr != null && responseArr.size() > 0) {
						for(int x=0; x<responseArr.size(); x++) {
							JSONObject obj = (JSONObject) responseArr.get(x);
							String resultCode = "";
							String resultTypeCode = Util.null2str(obj.get("ResultTypeCode"));
							String errorDesc = "";
							String efnPoNo = "";
							if("220".equals(resultTypeCode)) {
								resultCode = "S";
								errorDesc = "";
								efnPoNo = Util.null2str(obj.get("DeclarationNo"));
							}else if("422".equals(resultTypeCode)){
								errorDesc = Util.null2str(obj.get("ErrorDetail").toString());
								resultCode = "F";
							}else {
								errorDesc = Util.null2str(obj.get("ErrorDescription"));
								resultCode = "F";
							}
							
							// 상태값을 업데이트 해야할 PONO를 찾아야함. API에서 PONO를 리턴해주지않고 SEQ 번호를 리턴해줌
							JSONObject poObj = (JSONObject) jsonArr.get(Util.null2int((obj.get("OrgSeq"))));
							String poNo = Util.null2str(poObj.get("poNo"));
							
							
							Map<String,Object> updateMap = new HashMap<String, Object>();
							updateMap.put("sendResultCode", resultCode);
							updateMap.put("sendResultErrorDesc", errorDesc);
							updateMap.put("poNo", poNo);
							updateMap.put("licenseType", "E");
							updateMap.put("gmtTime",search.getGmtTime());
							updateMap.put("updateUserId", search.getUpdateUserId());
							licenseMapper.updateLicenseSendResult(updateMap);
						}
					}	
					/******************  KT NET 전송 End *******************/
					// history 등록 
					tmpMap = new HashMap<String, Object>();
					logId = DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
					index = "ecm-interface-logs-" + DateTime.getDate("yyyyMMdd");
					
					String dataId = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++);
					
					String filePath = customProperties.getBackupDir() + "api/eLicense/" + search.getEntCode() + "/" + DateTime.getDate("yyyy-MM") + "/";
					String fileName = dataId + ".json";
					FileUtil.fileWrite(jsonObject.toString(), filePath, fileName);
					
					// 처리결과에 따른 로그 기록
					tmpMap.clear();
					tmpMap.put("ent_code", search.getEntCode()); 		 					// 도입사코드
					tmpMap.put("customer_code", ""); 		 				// 고객코드
					tmpMap.put("solution", "ECM");      		 				// SOLUTION
					tmpMap.put("bound", "");		 	 					// 바운드 (AIR)
					tmpMap.put("bound_type", "EXP");	 					// 바운드타입
					tmpMap.put("hbl_no", "");				 				// HBL_NO
					tmpMap.put("if_service_code", "ECM");
					tmpMap.put("if_doc_code", "SEND_ELICENSE");
					tmpMap.put("if_doc_name", "E면장전송");
					tmpMap.put("if_doc_reference_no", "");	 					// 문서번호 
					tmpMap.put("if_vender", "");								// 중개사업자
					tmpMap.put("if_send_recv_type", "S");						// 송수신타입(S:SEND, R:RECV)
					tmpMap.put("if_send_recv_method", "");						// 송수신방법
					tmpMap.put("if_file_path", filePath);			 				// 파일경로
					tmpMap.put("if_file_name", fileName);			 			// 파일명
					tmpMap.put("if_status", "S");			 				// 처리상태
					tmpMap.put("if_status_info", "");		 				// 송수신상태_설명
					tmpMap.put("if_sender_id", "");							// 송신식별자
					tmpMap.put("if_sender_name", "");		 					// 송신자명
					tmpMap.put("if_receiver_id", "");		 					// 수신식별자
					tmpMap.put("if_receiver_name", "KTNET");	     					// 수신자명
					tmpMap.put("if_date", DateTime.getDate("yyyy-MM-dd"));		// DATE
					tmpMap.put("if_date_time", DateTime.getDate("hh:mm:ss"));	// DATE_TIME
					tmpMap.put("if_remark", "");			 			// 비고( 에러로그 등)
					tmpMap.put("register_user_id", "API");
					tmpMap.put("register_date_time", System.currentTimeMillis());
					tmpMap.put("es_job_no", logId);
					
					indexApi = new IndexApi(client);
					if (!indexApi.existIndex(index))	indexApi.createIndex(index);
					documentApi = new DocumentApi(client);
					documentApi.createDocument(index, logId, tmpMap);
				}
			}
		}
		return licenseSaveResult;
	}
	
	/**
	 * X/E면장 수신결과 수동전송
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public LicenseSaveResult updateResultSend(SearchLicenseBatchUpdate search) throws Exception {
		
		LicenseSaveResult licenseSaveResult = new LicenseSaveResult();

		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchLicenseText()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchLicenseText())).split(",")));
		}
		
		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSrchLicenseList(searchNoList);
		}else{
			search.setSearchType("");
			search.setSrchLicenseText(Util.null2str(search.getSrchLicenseText()).trim().replace(",", ""));
		}
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "srchDateFormat|gmtTime", true, "srchDateGbn", "PO_DATE|REGISTER_DATE_TIME|SEND_DATE_TIME|RESULT_DATE_TIME|RO_DATE", "srchLicenseType", "PO_NO|EFN_PO_NO");
		licenseMapper.updateLicenseResultStatus(search);
		licenseSaveResult.setResultCode("A11494");
		
		return licenseSaveResult;
	}
	
	/**
	 * 특수문자 알파벳 치환
	 * @param str
	 * @return
	 */
	public String transStr(String str) {
		
		if(str == null && "".equals(str)) {
			return str;
		}
		
		String replaceStr = str.replace("Ä", "A")
				                .replace("Å", "A")
				                .replace("Ă", "A")
				                .replace("Ə", "A")
				                .replace("ä", "a")
				                .replace("å", "a")
				                .replace("ă", "a")
								.replace("ə", "a")
								.replace("Ç", "C")
								.replace("Ć", "C")
								.replace("ç", "c")
								.replace("ć", "c")
								.replace("ć", "c")
								.replace("Đ", "D")
								.replace("đ", "đ")
								.replace("É", "E")
								.replace("é", "e")
								.replace("Ğ", "g")
								.replace("Ñ", "N")
								.replace("ñ", "n")
								.replace("Ö", "O")
								.replace("ö", "o")
								.replace("Ø", "O")
								.replace("ø", "o")
								.replace("Ü", "U")
								.replace("ü", "u")
								.replace("Ś", "S")
								.replace("ś", "s")
								.replace("Ș", "S")
								.replace("ș", "s")
								.replace("Ź", "Z")
								.replace("ź", "z")
								.replace("Ż", "Z")
								.replace("ż", "z")
								.replace("Î", "I")
								.replace("î", "i")
								.replace("Ț", "T")
								.replace("ț", "t")
								.replace("Þ", "T")
								.replace("þ", "t")
								.replace("Ð", "T")
								.replace("ð", "t")
								.replace("Ł", "L")
								.replace("ł", "l")
								;
		return replaceStr;
	}
}

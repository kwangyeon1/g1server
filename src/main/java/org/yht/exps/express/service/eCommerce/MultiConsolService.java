package org.yht.exps.express.service.eCommerce;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.consumerService.OrderCustomerMapper;
import org.yht.exps.express.mapper.eCommerce.ConsolMapper;
import org.yht.exps.express.mapper.eCommerce.MasterOrderMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.sms.EnterpriseMapper;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.eCommerce.consol.ConsolClose;
import org.yht.exps.express.model.eCommerce.consol.ConsolHouseOrder;
import org.yht.exps.express.model.eCommerce.consol.ConsolHouseOrderList;
import org.yht.exps.express.model.eCommerce.consol.ConsolMasterOrder;
import org.yht.exps.express.model.eCommerce.consol.ConsolMasterOrderList;
import org.yht.exps.express.model.eCommerce.consol.ConsolOrder;
import org.yht.exps.express.model.eCommerce.consol.ConsolRelease;
import org.yht.exps.express.model.eCommerce.consol.search.SearchConsolOrderList;
import org.yht.exps.express.model.eCommerce.masterOrder.MasterOrder;
import org.yht.exps.express.model.eCommerce.masterOrder.MasterOrderDelete;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.edi.SendManifest;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.OrderCommonService;
import org.yht.exps.express.service.edi.EdiCustomsManifestSendService;
import org.yht.exps.express.service.edi.EdiManifestGwSendService;
import org.yht.exps.express.service.sms.SmsUserService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class MultiConsolService {

	@Autowired
	CommonMapper commonMapper;
	@Autowired
	OrderMapper orderMapper;
	@Autowired
	ConsolMapper consolMapper;
	@Autowired
	OrderCustomerMapper orderCustomerMapper;
	@Autowired
    EnterpriseMapper enterpriseMapper;
	@Autowired
	MasterOrderMapper masterOrderMapper;
	@Autowired
    SmsUserService smsUserService;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	@Autowired
	CustomProperties customProperties;
	@Resource(name = "fileService")
	FileService fileService;
	@Resource(name = "commonService")
	CommonService commonService;
	@Resource(name = "orderCommonService")
	OrderCommonService orderCommonService;
	@Resource(name = "masterOrderService")
	MasterOrderService masterOrderService;
	@Resource(name = "ediManifestGwSendService")
	EdiManifestGwSendService ediManifestGwSendService;
	@Resource(name = "ediCustomsManifestSendService")
	EdiCustomsManifestSendService ediCustomsManifestSendService;
	
	//로컬,테스트,운영 구분
	@Value("${spring.profiles.active}")
	private String profile;
	
	/**
	 * Consol MasterOrder List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ConsolMasterOrderList selectMasterOrderList(SearchConsolOrderList search) throws Exception {
		ConsolMasterOrderList consolMasterOrderList = new ConsolMasterOrderList();
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
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "userLanguage|level|srchCusType|srchDateFormat", true, null, null, "srchNoType", "|od.HBL_NO|mod.MBL_NO");
		List<ConsolMasterOrder> list  = consolMapper.selectMasterOrderList(search);
		consolMasterOrderList.setConsolMasterOrderList(list);
		if(list.size() > 0) {
			consolMasterOrderList.setTotal(list.get(0).getTotal());
		}else {
			consolMasterOrderList.setTotal(0);
		}
		return consolMasterOrderList;
	}
	
	/**
	 * Consol HouseOrder List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ConsolHouseOrderList selectHouseOrderList(SearchConsolOrderList search) throws Exception {
		ConsolHouseOrderList consolHouseOrderList = new ConsolHouseOrderList();
		
		if(search.getSrchMasterOdJobNo() == null && search.getSrchBoundType() == null){
			consolHouseOrderList.setResultCode("A00001");	/** 시스템 오류가 발생하였습니다. 관리자에게 문의해주세요. */
			return consolHouseOrderList;
		}
		
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/* N개 검색(멀티타입) */
		List<String> searchNoHouseList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNoHouse()))) {
			searchNoHouseList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNoHouse())).split(",")));
		}
		if(searchNoHouseList.size() > 1){
			search.setSearchTypeHouse("multi");
			search.setSearchNoHouseList(searchNoHouseList);
		}else{
			search.setSearchTypeHouse("");
			search.setSrchNoHouse(Util.null2str(search.getSrchNoHouse()).trim().replace(",", ""));
		}
		
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
		Util.accessControlDatabase(search, "level|srchCusType|srchDateFormat", true, null, null, "srchNoTypeHouse", "|od.HBL_NO");
		List<ConsolHouseOrder> list  = consolMapper.selectHouseOrderList(search);
		consolHouseOrderList.setConsolHouseOrderList(list);
		if(list.size() > 0) {
			consolHouseOrderList.setTotal(list.get(0).getTotal());
		}else {
			consolHouseOrderList.setTotal(0);
		}
		return consolHouseOrderList;
		
	}
	
	/**
	 * Consol 수입 Master List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ConsolMasterOrderList selectImpMasterList(SearchConsolOrderList search) throws Exception {
		ConsolMasterOrderList consolMasterOrderList = new ConsolMasterOrderList();
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
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "srchDateFormat", true, null, null, "srchNoType", "mod.MBL_NO");
		List<ConsolMasterOrder> list  = consolMapper.selectImpMasterList(search);
		consolMasterOrderList.setConsolMasterOrderList(list);
		if(list.size() > 0) {
			consolMasterOrderList.setTotal(list.get(0).getTotal());
		}else {
			consolMasterOrderList.setTotal(0);
		}
		return consolMasterOrderList;
	}
	
	/**
	 * Consol 수입 House List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ConsolHouseOrderList selectImpHouseList(SearchConsolOrderList search) throws Exception {
		ConsolHouseOrderList consolHouseOrderList = new ConsolHouseOrderList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"od.COMMODITY_ITEM".equals(search.getSrchNoType()) && !"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
			
			if(searchNoList.size() == 1){
				search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
			}
		}
		
		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		}else{
			search.setSearchType("");
		}
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "srchDateFormat", true, null, null, "srchNoType", "od.HBL_NO|od.ALLOWED_ITEM_CODE|od.COMMODITY_ITEM");
		List<ConsolHouseOrder> list  = consolMapper.selectImpHouseList(search);
		consolHouseOrderList.setConsolHouseOrderList(list);
		if(list.size() > 0) {
			consolHouseOrderList.setTotal(list.get(0).getTotal());
		}else {
			consolHouseOrderList.setTotal(0);
		}
		return consolHouseOrderList;
	}
	
	/**
	 * Consol 수입 House 콘솔건 List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ConsolHouseOrderList selectImpHouseConsolList(SearchConsolOrderList search) throws Exception {
		ConsolHouseOrderList consolHouseOrderList = new ConsolHouseOrderList();
		search.setPage(0);
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, null, true, null, null, null, null);
		consolHouseOrderList.setConsolHouseOrderList(consolMapper.selectImpHouseConsolList(search));
		return consolHouseOrderList;
	}
	
	/**
	 * Consol 수출 Master List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ConsolMasterOrderList selectExpMasterList(SearchConsolOrderList search) throws Exception {
		ConsolMasterOrderList consolMasterOrderList = new ConsolMasterOrderList();
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
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "srchDateFormat", true, null, null, "srchNoType", "mod.MBL_NO");
		List<ConsolMasterOrder> list  = consolMapper.selectExpMasterList(search);
		consolMasterOrderList.setConsolMasterOrderList(list);
		if(list.size() > 0) {
			consolMasterOrderList.setTotal(list.get(0).getTotal());
		}else {
			consolMasterOrderList.setTotal(0);
		}
		return consolMasterOrderList;
	}
	
	/**
	 * Consol 수출 House List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ConsolHouseOrderList selectExpHouseList(SearchConsolOrderList search) throws Exception {
		ConsolHouseOrderList consolHouseOrderList = new ConsolHouseOrderList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if("od.HBL_NO".equals(search.getSrchNoType()) && !"".equals(Util.null2str(search.getSrchNo()))) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
			
			if(searchNoList.size() == 1){
				search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",", ""));
			}
		}
		
		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		}else{
			search.setSearchType("");
		}
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "srchDateFormat", true, null, null, "srchNoType", "od.HBL_NO|od.SHIPPER_NAME|od.SHIPPER_ADDRESS|od.CONSIGNEE_NAME|od.CONSIGNEE_ADDRESS");
		List<ConsolHouseOrder> list  = consolMapper.selectExpHouseList(search);
		consolHouseOrderList.setConsolHouseOrderList(list);
		if(list.size() > 0) {
			consolHouseOrderList.setTotal(list.get(0).getTotal());
		}else {
			consolHouseOrderList.setTotal(0);
		}
		return consolHouseOrderList;
	}
	
	/**
	 * Consol 수출 House 콘솔건 List
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ConsolHouseOrderList selectExpHouseConsolList(SearchConsolOrderList search) throws Exception {
		ConsolHouseOrderList consolHouseOrderList = new ConsolHouseOrderList();
		search.setPage(0);
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, null, true, null, null, null, null);
		consolHouseOrderList.setConsolHouseOrderList(consolMapper.selectExpHouseConsolList(search));
		return consolHouseOrderList;
	}
	
	/**
	 * Consolidation - Consol
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult consol(ConsolOrder consol, HttpServletRequest request) throws Exception{
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		String consolType = Util.null2str(consol.getAction());
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(consol, "gmtTime", false, null, null, null, null);
		
		/** 콘솔할 마스터정보 조회 */
		if(!"addConsol".equals(consolType)){
			consol.setAction("consol");
		}
		
		/** 바운드가 없는 데이터를 콘솔 할때 **/
		/**
		 * 도입사/거래처(한국) : IMP->IMP엔트리, EXP->EXP엔트리
		 * 거래처(!한국)	: IMP->EXP엔트리, EXP->IMP엔트리
		 */
		if("Y".equals(Util.null2str(consol.getReverseBoundType()))) {
			if("IMP".equals(consol.getBoundType()))consol.setBoundType("EXP");
			else if("EXP".equals(consol.getBoundType()))consol.setBoundType("IMP");
		}
		
		ConsolOrder consolOrder = consolMapper.selectConsolMasterInfo(consol);
		if(consolOrder == null){
			Map<String, Object> errMap = new HashMap<String, Object>();
			errMap.put("mblNoStr", consol.getMblNo());
			orderSaveResult.setResultCode("A00039");	/** 마스터 정보가 존재하지 않아 처리할 수 없습니다. */
			orderSaveResult.setResultData(errMap);
			return orderSaveResult;
		}else if("addConsol".equals(consolType)){
			consol.setOdJobNo(consolOrder.getOdJobNo());
			consol.setBound(consolOrder.getBound());
			consol.setBoundType(consolOrder.getBoundType());
			consol.setOrigCode(consolOrder.getOrigCode());
			consol.setDestPort(consolOrder.getDestPort());
		}
		
		if(Util.null2str(consolOrder.getFinishYn()).equals("Y")/* && !Util.null2str(consol.getEcmCustomerCode()).equals("")*/){
			Map<String, Object> errMap = new HashMap<String, Object>();
			errMap.put("mblNoStr", consolOrder.getMblNo());
			orderSaveResult.setResultCode("A11237");	/** 마감된 오더는 수정 할 수 없습니다. */
			orderSaveResult.setResultData(errMap);
			return orderSaveResult;
		}
		
		if(consolOrder.getHblCnt() + consol.getHblNoList().size() > 9999) {
			Map<String, Object> errMap = new HashMap<String, Object>();
			errMap.put("mblNoStr", consolOrder.getMblNo());
			orderSaveResult.setResultCode("A11488");	/** 최대 콘솔 가능 건수(9,999)를 초과하였습니다. */
			orderSaveResult.setResultData(errMap);
			return orderSaveResult;
		}
		
		consolOrder.setGmtTime(consol.getGmtTime());
		consolOrder.setUpdateUserId(consol.getUpdateUserId());
		
		//마스터에 들어갈 상위업체 체크 
		//여러개의 상위업체가 섞인 하우스를 마스터와 콘솔하게 되면 마스터에는 상위업체값은 도입사를 넣게된다.
		Map<String,Object> consolMap = new HashMap<>();
		consolMap.put("odJobNo", consol.getOdJobNo());
		List<String> custList = consolMapper.selectCustCodeList(consolMap);
		if(custList.size() > 0) {
			consol.setCustLv1List(custList);
		}
		
		Map<String, Object> resultData = new HashMap<String, Object>();
		List<List<Map<String, Object>>> spliceHblNoList = new ArrayList<List<Map<String, Object>>>();
		int count = 0;
		
		consolErrorCheck(consol, consolOrder, spliceHblNoList, orderSaveResult, resultData);	/** 콘솔불가오더체크 */
		if(!Util.null2str(orderSaveResult.getResultCode()).equals("")){
			if(Util.null2str(orderSaveResult.getResultCode()).equals("consolIncongruityOrder")){
				orderSaveResult.setResultData(resultData);
			}
			return orderSaveResult;
		}
		
		/** 환율정보조회 */
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		tmpMap.put("srchEntCode", consol.getEntCode());
		tmpMap.put("entBusinessCode", consol.getEntBusinessCode());
		tmpMap.put("srchDateFormat", "23");
		
		if(Util.null2str(consol.getBoundType()).equals("EXP")){
			tmpMap.put("exrateType", "EXRATE_SELLING_CUSTOMS");	/** 항공해상구분없음 */
			tmpMap.put("srchDate", Util.null2str(consolOrder.getObDate()));
		}else{
			tmpMap.put("exrateType", "EXRATE_BUYING_CUSTOMS");
			tmpMap.put("srchDate", Util.null2str(consolOrder.getArDate()));
		}
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(tmpMap, "exrateType|srchDateFormat", false, null, null, null, null);
		Map<String, Object> exchangeRateMap = commonMapper.selectExchangeRate(tmpMap);
		Map<String, Object> houseData = null;
//		Map<String, Object> simpleDocNoMap = new HashMap<String, Object>();
//		simpleDocNoMap.put("entCode", consol.getEntCode());
//		simpleDocNoMap.put("gmtTime", consol.getGmtTime());
//		simpleDocNoMap.put("entBusinessCode", "");
//		simpleDocNoMap.put("updateUserId", consol.getUpdateUserId());
//		simpleDocNoMap.put("solution", "ECM");
//		simpleDocNoMap.put("stockType", "OD");
//		simpleDocNoMap.put("stockSeqLength", "8");
//		simpleDocNoMap.put("signType", "noHyphen");
//		simpleDocNoMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
//		simpleDocNoMap.put("stockRemark", "ORDER SIMPLE DOC NO");
//		if(consol.getBoundType().equals("EXP")){
//			simpleDocNoMap.put("dateLength", "4");
//			simpleDocNoMap.put("stockDateFormat", "112");
//			tmpMap.put("srchEdiCityPort", consolOrder.getOrigCode());
//		}else{
//			simpleDocNoMap.put("dateLength", "2");
//			simpleDocNoMap.put("stockDateFormat", "12");
//			tmpMap.put("srchEdiCityPort", consolOrder.getDestPort());
//		}
//		/** 제출번호에 사용되는 특송부호 조회 */
//		String ediExpressSign = null;
//		tmpMap.put("srchEntBusinessCode", consol.getEntBusinessCode());
//		tmpMap.put("srchBound", consol.getBound());
//		Map<String, Object> busEdiEcmMap = commonMapper.selectBusEdiCodeInfo(tmpMap);
//		if(busEdiEcmMap != null && busEdiEcmMap.size() > 0){
//			ediExpressSign = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN"));
//		}
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			ConsolMapper consolBatchMapper = sqlSession.getMapper(ConsolMapper.class);
			consolOrder.setMasterOdJobNo(consolOrder.getOdJobNo());
			
			Map<String, Object> orderSaveMap = new HashMap<String, Object>();
			Order order = new Order();
			order.setEntCode(consolOrder.getEntCode());
			order.setMblNo(consolOrder.getMblNo());
			order.setMasterOdJobNo(consolOrder.getMasterOdJobNo());
			order.setLineCode(consolOrder.getLineCode());
			order.setVesselFlight(consolOrder.getVesselFlight());
			order.setOrigCode(consolOrder.getOrigCode());
			order.setOrigName(consolOrder.getOrigName());
			order.setObDate(consolOrder.getObDate());
			order.setObTime(consolOrder.getObTime());
			order.setDestCode(consolOrder.getDestCode());
			order.setDestName(consolOrder.getDestName());
			order.setArDate(consolOrder.getArDate());
			order.setArTime(consolOrder.getArTime());
			order.setOrigCountry(consolOrder.getOrigCountry());
			order.setDestCountry(consolOrder.getDestCountry());
			order.setCustomsCode(consolOrder.getCustomsCode());
			order.setCustomsDptCode(consolOrder.getCustomsDptCode());
			order.setDiscWhCode(consolOrder.getDiscWhCode());
			order.setBound(consolOrder.getBound());
			order.setBoundType(consolOrder.getBoundType());
			order.setDestPort(consolOrder.getDestPort());
			order.setMrn(consolOrder.getMrn());
			order.setMsn(consolOrder.getMsn());
			order.setUpdateUserId(consolOrder.getUpdateUserId());
			order.setGmtTime(consolOrder.getGmtTime());
			order.setEdiMailboxSeqNo(consolOrder.getEdiMailboxSeqNo());
			order.setVenderId1(consolOrder.getVenderId1());
			order.setCarryoutCode(consolOrder.getCarryoutCode());
			if("AIR".equals(consol.getBound()) && "EXP".equals(consol.getBoundType())){
				if("CN".equals(consolOrder.getDestCodeCountry())){
					order.setShipperFax("VAT NUMBER");
					order.setConsigneeFax("USCI");
				}else if("ID".equals(consolOrder.getDestCodeCountry())){
					order.setConsigneeFax("TIN");
				}else if("210".equals(consolOrder.getDestCodeArea())){
					order.setConsigneeFax("T");
				}
			}
//			if(consol.getBoundType().equals("IMP")){
//				simpleDocNoMap.put("stockSign", ediExpressSign);
//				order.setSimpleDocNo(commonMapper.selectNextStockMaxNo(simpleDocNoMap));
//			}
			
			BulkData bulkData = null;
			String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
			Map<String, Object> logMap = null;
			Map<String, Object> tmpLogMap = new HashMap<String, Object>();
			
			tmpLogMap.put("ent_code", consol.getEntCode());
			tmpLogMap.put("ent_business_code", consol.getUserBusinessCode());
			tmpLogMap.put("solution", "ECM");
			tmpLogMap.put("process_user_id", consol.getUpdateUserId());
			tmpLogMap.put("log_job_type", "CONSOL");
			tmpLogMap.put("job_func_type", "UPDATE");
			tmpLogMap.put("result", "ok");
			tmpLogMap.put("process_date_time", System.currentTimeMillis());
			tmpLogMap.put("ent_business_print_name", consol.getLogUserBusinessPrintName());
			tmpLogMap.put("process_user_name", consol.getLogUserName());
			tmpLogMap.put("process_user_name_eng", consol.getLogUserNameEng());
			
			if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
			int seq = 1;
			for(int i = 0; i < spliceHblNoList.size(); i++){
				for(int j = 0; j < spliceHblNoList.get(i).size(); j++){
					houseData = spliceHblNoList.get(i).get(j);
					order.setOdJobNo(Util.null2str(houseData.get("OD_JOB_NO")));
					order.setCurrencyCode(Util.null2str(houseData.get("CURRENCY_CODE")));
					order.setAmountCur(Util.null2dbl(houseData.get("AMOUNT_CUR")));
					order.setDestCountryH(Util.null2str(houseData.get("DEST_COUNTRY_H")));
					order.setShipperCountryCode(Util.null2str(houseData.get("SHIPPER_COUNTRY_CODE")));
					order.setConsigneeCountryCode(Util.null2str(houseData.get("CONSIGNEE_COUNTRY_CODE")));
					/** 2025-03-24 하우스의 보세창고값이 없을경우만 마스터 정보로 업데이트 */
					if(Util.isEmpty(houseData.get("ASSIGNMENT_WH_CODE"))){
						order.setAssignmentWhCode(consolOrder.getAssignmentWhCode());
					}else{
						order.setAssignmentWhCode(null);
					}
					/** 2025-06-20 하우스의 반출사유값이 없을경우만 마스터 정보로 업데이트 */
					if(Util.isEmpty(houseData.get("CARRYOUT_CODE"))){
						order.setCarryoutCode(consolOrder.getCarryoutCode());
					}else{
						order.setCarryoutCode(null);
					}
					
					orderSaveMap.clear();
					/** 공통 - 환율처리 */
					orderSaveMap.put("exChangeYn", "Y");
					orderSaveMap.put("exrateMap", exchangeRateMap);
					
					// 마스터의 상위업체 코드 값이 도입사면 마스터 상위업체 정보를 업데이트 해야 한다.
					if(consol.getEntCode().equals(consolOrder.getCustCodeLv1())) {
						consolBatchMapper.updateMasterCustLvInfo(consolOrder);
					}
					
					orderCommonService.orderSave(order, "consol", orderSaveMap, seq);
					order.setProfileName(profile);
					consolBatchMapper.updateConsolOrderBatch(order);
					count++;
					
					bulkData = new BulkData();
					bulkData.setActionType(BulkData.Type.CREATE);
					bulkData.setIndexName(index);
					bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
					
					logMap = new HashMap<String, Object>();
					logMap.putAll(tmpLogMap);
					logMap.put("bound", Util.null2str(houseData.get("BOUND")));
					logMap.put("bound_type", Util.null2str(houseData.get("BOUND_TYPE")));
					logMap.put("reference_no_id", bulkData.getId());
					logMap.put("job_no", Util.null2str(houseData.get("OD_JOB_NO")));
					logMap.put("description", "Consol [MBL_NO : " + consolOrder.getMblNo() + ", HBL_NO : "+ houseData.get("HBL_NO") +"]");
//					logMap.put("job_log","{\"order\":" +new Gson().toJson(houseData)+", \"requestConsol\":" +new Gson().toJson(consol)+"}");
					logMap.put("job_log","");
					bulkData.setJsonMap(logMap);
					bulkList.add(bulkData);
				}
				sqlSession.flushStatements();
				sqlSession.clearCache();
				sqlSession.commit();
			}
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
			
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpLogMap);
			logMap.put("bound", consolOrder.getBound());
			logMap.put("bound_type", consolOrder.getBoundType());
			logMap.put("reference_no_id", bulkData.getId());
			logMap.put("job_no", consolOrder.getMasterOdJobNo());
			logMap.put("description", "Consol [MBL_NO : " + consolOrder.getMblNo() + "]");
			logMap.put("job_log","");
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
		
		orderSaveResult.setResultCode("A11283"); // 성공 
		resultData.put("count", count);
		if("addConsol".equals(consolType)){
			resultData.put("arDate", consolOrder.getArDate());
			if("AIR".equals(consolOrder.getBound())){if("EXP".equals(consolOrder.getBoundType())){resultData.put("boundType", "AO");}else{resultData.put("boundType", "AI");}}
			else if("SEA".equals(consolOrder.getBound())){if("EXP".equals(consolOrder.getBoundType())){resultData.put("boundType", "SO");}else{resultData.put("boundType", "SI");}}
		}
		orderSaveResult.setResultData(resultData);
		
		commonService.insertLogBulk(bulkList);
		
		log.info("처리 : "+count);
		return orderSaveResult;
	}
	
	/**
	 * Consolidation - Consol
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult autoConsol(ConsolOrder consol, HttpServletRequest request) throws Exception{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("odJobNo", consol.getMasterOdJobNo());
		map.put("action", "autoConsol");
		map.put("simpleDocNo", consol.getSimpleDocNo());
		map = consolMapper.selectFinishYnChk(map);
		if(map != null && "Y".equals(map.get("FINISH_YN"))){
			if(Util.null2int(map.get("SIMPLE_ERROR_SEND_CNT")) > 0){
				OrderSaveResult orderSaveResult = new OrderSaveResult();
				orderSaveResult.setResultCode("A11133");	/** 을(를) 확인하여 주시기 바랍니다. */
				Map<String, Object> errMap = new HashMap<String, Object>();
				errMap.put("prependMsgId", "EXPS004660");	/** 오류내역 */
				errMap.put("appendStr", "<br />" + consol.getSimpleDocNo());
				orderSaveResult.setResultData(errMap);
				return orderSaveResult;
			}
			return this.closeMasterConsol(consol, request);
		}else{
			return this.consol(consol, request);
		}
	}
	
	/**
	 * Consolidation - Deconsol
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult autoDeconsol(ConsolRelease consol, HttpServletRequest request) throws Exception{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("odJobNo", consol.getMasterOdJobNo());
		map = consolMapper.selectFinishYnChk(map);
		if(map != null && "Y".equals(map.get("FINISH_YN"))){
			ConsolOrder consolOrder = new ConsolOrder();
			consolOrder.setEntCode(consol.getEntCode());
			consolOrder.setGmtTime(consol.getGmtTime());
			consolOrder.setBound(consol.getBound());
			consolOrder.setBoundType(consol.getBoundType());
			consolOrder.setUpdateUserId(consol.getUpdateUserId());
			consolOrder.setEntBusinessCode(consol.getHeadBusinessCode());
			consolOrder.setUserBusinessCode(consol.getUserBusinessCode());
			List<String> odJobNoList = new ArrayList<String>();
			for(Map<String, Object> od : consol.getOdJobNoList()){
				odJobNoList.add(Util.null2str(od.get("odJobNo")));
			}
			consolOrder.setOdJobNoList(odJobNoList);
			return this.closeMasterDeconsol(consolOrder, request);
		}else{
			return this.deconsol(consol, request);
		}
	}
	
	/**
	 * Consolidation - closeMasterConsol
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult closeMasterConsol(ConsolOrder consol, HttpServletRequest request) throws Exception{
		String origAction = Util.null2str(consol.getAction());
		String simpleDocNo = null;
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(consol, "gmtTime", false, null, null, null, null);
		
		if("autoConsol".equals(origAction)){	/** 신규 수입콘솔화면에서 콘솔할때는 선택한 마스터의 제출번호로 하우스 제출번호를 업데이트 */
			simpleDocNo = Util.null2str(consol.getSimpleDocNo());
			consol.setSimpleDocNo(null);
		}
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		
		/** 콘솔할 마스터정보 조회 */
		consol.setAction("closeMasterConsol");
		ConsolOrder consolOrder = consolMapper.selectConsolMasterInfo(consol);
		if(!Util.null2str(consolOrder.getFinishYn()).equals("Y")){
			orderSaveResult.setResultCode("A11334");	/** 마감된 마스터가 아닙니다. */
			return orderSaveResult;
		}
		
		if(consolOrder.getHblCnt() + consol.getHblNoList().size() > 9999) {
			Map<String, Object> errMap = new HashMap<String, Object>();
			errMap.put("mblNoStr", consolOrder.getMblNo());
			orderSaveResult.setResultCode("A11488");	/** 최대 콘솔 가능 건수(9,999)를 초과하였습니다. */
			orderSaveResult.setResultData(errMap);
			return orderSaveResult;
		}
		
		consolOrder.setGmtTime(consol.getGmtTime());
		consolOrder.setUpdateUserId(consol.getUpdateUserId());
		
		//마스터에 들어갈 상위업체 체크 
		//여러개의 상위업체가 섞인 하우스를 마스터와 콘솔하게 되면 마스터에는 상위업체값은 도입사를 넣게된다.
		Map<String,Object> consolMap = new HashMap<>();
		consolMap.put("odJobNo", consolOrder.getOdJobNo());
		List<String> custList = consolMapper.selectCustCodeList(consolMap);
		if(custList.size() > 0) {
			consol.setCustLv1List(custList);
		}
		
		Map<String, Object> resultData = new HashMap<String, Object>();
		List<List<Map<String, Object>>> spliceHblNoList = new ArrayList<List<Map<String, Object>>>();
		int count = 0;
		
		consolErrorCheck(consol, consolOrder, spliceHblNoList, orderSaveResult, resultData); /** 콘솔불가오더체크 */
		if(!Util.null2str(orderSaveResult.getResultCode()).equals("")){
			if(Util.null2str(orderSaveResult.getResultCode()).equals("consolIncongruityOrder")){
				orderSaveResult.setResultData(resultData);
			}
			return orderSaveResult;
		}
		
		Map<String, Object> houseData = null;
		if(consol.getBoundType().equals("IMP") && "autoConsol".equals(origAction) && !"".equals(simpleDocNo)){
			String documentYn = null;
			StringBuffer errorStr = new StringBuffer();
			if(simpleDocNo.startsWith("AD")){	/** 서류여부Y의 제출번호는 무조건 AD로 시작   서류여부N의 제출번호는 무조건 AE로 시작 */
				documentYn = "Y";
			}else{
				documentYn = "N";
			}
			for(int i = 0; i < spliceHblNoList.size(); i++){
				for(int j = 0; j < spliceHblNoList.get(i).size(); j++){
					houseData = spliceHblNoList.get(i).get(j);
					if(!Util.null2str(houseData.get("DOCUMENT_YN")).equals(documentYn)){
						errorStr.append(houseData.get("HBL_NO") + "<br />");
					}
				}
			}
			
			if(errorStr.length() > 0){
				orderSaveResult.setResultCode("A11133");	/** 을(를) 확인하여 주시기 바랍니다. */
				Map<String, Object> errMap = new HashMap<String, Object>();
				errMap.put("prependMsgId", "EXPS003879");	/** 서류여부 */
				errMap.put("appendStr", "<br />" + errorStr);
				orderSaveResult.setResultData(errMap);
				return orderSaveResult;
			}
		}
		
		
		
		/** 환율정보조회 */
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		tmpMap.put("srchEntCode", consol.getEntCode());
		tmpMap.put("entBusinessCode", consol.getEntBusinessCode());
		tmpMap.put("srchDateFormat", "23");
		
		if(Util.null2str(consol.getBoundType()).equals("EXP")){
			tmpMap.put("exrateType", "EXRATE_SELLING_CUSTOMS");	/** 항공해상구분없음 */
			tmpMap.put("srchDate", Util.null2str(consolOrder.getObDate()));
		}else{
			tmpMap.put("exrateType", "EXRATE_BUYING_CUSTOMS");
			tmpMap.put("srchDate", Util.null2str(consolOrder.getArDate()));
		}
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(tmpMap, "exrateType|srchDateFormat", false, null, null, null, null);
		Map<String, Object> exchangeRateMap = commonMapper.selectExchangeRate(tmpMap);
		
		Map<String, Object> simpleDocNoMap = new HashMap<String, Object>();
		simpleDocNoMap.put("entCode", consol.getEntCode());
		simpleDocNoMap.put("gmtTime", consol.getGmtTime());
		simpleDocNoMap.put("entBusinessCode", "");
		simpleDocNoMap.put("updateUserId", consol.getUpdateUserId());
		simpleDocNoMap.put("solution", "ECM");
		simpleDocNoMap.put("stockType", "OD");
		simpleDocNoMap.put("stockSeqLength", "8");
		simpleDocNoMap.put("signType", "noHyphen");
		simpleDocNoMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		simpleDocNoMap.put("stockRemark", "ORDER SIMPLE DOC NO");
		if(consol.getBoundType().equals("EXP")){
//			simpleDocNoMap.put("dateLength", "4");
//			simpleDocNoMap.put("stockDateFormat", "112");
			tmpMap.put("srchEdiCityPort", consolOrder.getOrigCode());
		}else{
			simpleDocNoMap.put("dateLength", "2");
			simpleDocNoMap.put("stockDateFormat", "12");
			tmpMap.put("srchEdiCityPort", consolOrder.getDestPort());
		}
		/** 제출번호에 사용되는 특송부호 조회 */
		String ediExpressSign = null, ediExpressSignDocumentY = "", ediExpressSignDocumentN = "";
		tmpMap.put("srchEntBusinessCode", consol.getEntBusinessCode());
		tmpMap.put("srchBound", consol.getBound());
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(tmpMap, "stockDateFormat", false, null, null, null, null);
		Map<String, Object> busEdiEcmMap = commonMapper.selectBusEdiCodeInfo(tmpMap);
		if(busEdiEcmMap != null && busEdiEcmMap.size() > 0){
			ediExpressSign = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN"));
			ediExpressSignDocumentY = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y"));
			ediExpressSignDocumentN = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N"));
		}
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			ConsolMapper consolBatchMapper = sqlSession.getMapper(ConsolMapper.class);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			consolOrder.setMasterOdJobNo(consolOrder.getOdJobNo());
			
			Map<String, Object> orderSaveMap = new HashMap<String, Object>();
			Order order = new Order();
			order.setEntCode(consolOrder.getEntCode());
			order.setMblNo(consolOrder.getMblNo());
			order.setMasterOdJobNo(consolOrder.getMasterOdJobNo());
			order.setLineCode(consolOrder.getLineCode());
			order.setVesselFlight(consolOrder.getVesselFlight());
			order.setOrigCode(consolOrder.getOrigCode());
			order.setOrigName(consolOrder.getOrigName());
			order.setObDate(consolOrder.getObDate());
			order.setObTime(consolOrder.getObTime());
			order.setDestCode(consolOrder.getDestCode());
			order.setDestName(consolOrder.getDestName());
			order.setArDate(consolOrder.getArDate());
			order.setArTime(consolOrder.getArTime());
			order.setOrigCountry(consolOrder.getOrigCountry());
			order.setDestCountry(consolOrder.getDestCountry());
			order.setCustomsCode(consolOrder.getCustomsCode());
			order.setCustomsDptCode(consolOrder.getCustomsDptCode());
			order.setDiscWhCode(consolOrder.getDiscWhCode());
			order.setBound(consolOrder.getBound());
			order.setBoundType(consolOrder.getBoundType());
			order.setDestPort(consolOrder.getDestPort());
			order.setMrn(consolOrder.getMrn());
			order.setMsn(consolOrder.getMsn());
			order.setUpdateUserId(consolOrder.getUpdateUserId());
			order.setGmtTime(consolOrder.getGmtTime());
			order.setEdiMailboxSeqNo(consolOrder.getEdiMailboxSeqNo());
			order.setVenderId1(consolOrder.getVenderId1());
			order.setCarryoutCode(consolOrder.getCarryoutCode());
			/* 2025-04-16 마감후콘솔시 마스터 중게사업자 참조해서 하우스에 값 적용 요청 */
			order.setConsolAction("closeMasterConsol");
			order.setVenderId1(consolOrder.getVenderId1());
			if("AIR".equals(consol.getBound()) && "EXP".equals(consol.getBoundType())){
				if("CN".equals(consolOrder.getDestCodeCountry())){
					order.setShipperFax("VAT NUMBER");
					order.setConsigneeFax("USCI");
				}else if("ID".equals(consolOrder.getDestCodeCountry())){
					order.setConsigneeFax("TIN");
				}else if("210".equals(consolOrder.getDestCodeArea())){
					order.setConsigneeFax("T");
				}
			}
			
			boolean documentY = false, documentN = false;
			if(consol.getBoundType().equals("IMP") && "autoConsol".equals(origAction)) {
				order.setSimpleDocNo(Util.isEmpty(simpleDocNo) ? "null" : simpleDocNo);
			}else{
				if(ediExpressSign != null){
					if(consol.getBoundType().equals("IMP")){
						/** 서류YN값에 따라 제출번호 처리 */
						for(int i = 0; i < spliceHblNoList.size(); i++){
							for(int j = 0; j < spliceHblNoList.get(i).size(); j++){
								houseData = spliceHblNoList.get(i).get(j);
								if("Y".equals(houseData.get("DOCUMENT_YN"))){
									documentY = true;
								}else{
									documentN = true;
								}
								if(documentY && documentN){
									break;
								}
							}
							if(documentY && documentN){
								break;
							}
						}
						
						/** 목록신고 전송여부에 따라 제출번호 결정 */
//						if(consolOrder.getHblCnt() == Integer.parseInt(consolOrder.getSimpleEdiSendCnt()) && consolOrder.getMasterSimpleDocNoHblCnt() != 0) {
//						// 1) 모두 전송된 건일 경우(마스터 제출번호가 있고 마스터 제출번호와 같은 하우스가 없을경우 제외) : 새 제출번호 생성
//							if(documentY){
//								this.createSimpleDocNoDocumentYn(ediExpressSignDocumentY, order, simpleDocNoMap, true);
//							}
//							if(documentN){
//								this.createSimpleDocNoDocumentYn(ediExpressSignDocumentN, order, simpleDocNoMap, false);
//							}
//						}else{
//						// 2) 전송되지않은건의 기존 제출번호 사용     기존 제출번호 없으면 신규 제출번호 생성
//							if(documentY){
//								tmpMap = new HashMap<String, Object>();
//								tmpMap.put("masterOdJobNo", order.getMasterOdJobNo());
//								tmpMap.put("documentYn", "Y");
//								if(consolOrder.getMasterSimpleDocNoHblCnt() == 0 && !Util.isEmpty(consolOrder.getSimpleDocNo()) && ediExpressSignDocumentY.equals(consolOrder.getSimpleDocNo().substring(0, 6))){
//									/** 마스터제출번호 포함해서 조회 */
//									tmpMap.put("chkMasterSimpleDocNo", "Y");
//								}
//								tmpMap = consolMapper.selectMinSimpleDocNo(tmpMap);
//								if(tmpMap == null){
//									this.createSimpleDocNoDocumentYn(ediExpressSignDocumentY, order, simpleDocNoMap, true);
//								}else{
//									if(Util.isEmpty(tmpMap.get("MIN_SIMPLE_DOC_NO"))){
//										order.setSimpleDocNoDocumentY("null");
//									}else{
//										order.setSimpleDocNoDocumentY(Util.null2str(tmpMap.get("MIN_SIMPLE_DOC_NO")));
//									}
//								}
//							}
//							if(documentN){
//								tmpMap = new HashMap<String, Object>();
//								tmpMap.put("masterOdJobNo", order.getMasterOdJobNo());
//								tmpMap.put("documentYn", "N");
//								if(consolOrder.getMasterSimpleDocNoHblCnt() == 0 && !Util.isEmpty(consolOrder.getSimpleDocNo()) && ediExpressSignDocumentN.equals(consolOrder.getSimpleDocNo().substring(0, 6))){
//									tmpMap.put("chkMasterSimpleDocNo", "Y");
//								}
//								tmpMap = consolMapper.selectMinSimpleDocNo(tmpMap);
//								if(tmpMap == null){
//									this.createSimpleDocNoDocumentYn(ediExpressSignDocumentN, order, simpleDocNoMap, false);
//								}else{
//									if(Util.isEmpty(tmpMap.get("MIN_SIMPLE_DOC_NO"))){
//										order.setSimpleDocNoDocumentN("null");
//									}else{
//										order.setSimpleDocNoDocumentN(Util.null2str(tmpMap.get("MIN_SIMPLE_DOC_NO")));
//									}
//								}
//							}
//						}
					}
				}else{
					order.setSimpleDocNo("null");
					order.setSimpleDocNoDocumentY("null");
					order.setSimpleDocNoDocumentN("null");
				}
			}
			
			order.setFinishYn("Y");
			
			if(consol.getBoundType().equals("IMP")){
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("entCode", consolOrder.getEntCode());
				tmpMap.put("entBusinessCode", consol.getEntBusinessCode());
				tmpMap.put("solution", "ECM");
				tmpMap.put("optionCode", "C09");
				if("Y".equals(commonMapper.selectSystemOption(tmpMap))){
					order.setAction("updateSimpleDocDate");
				}
			}
			
			BulkData bulkData = null;
			String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
			Map<String, Object> logMap = null;
			Map<String, Object> tmpLogMap = new HashMap<String, Object>();
			
			tmpLogMap.put("ent_code", consol.getEntCode());
			tmpLogMap.put("ent_business_code", consol.getUserBusinessCode());
			tmpLogMap.put("solution", "ECM");
			tmpLogMap.put("process_user_id", consol.getUpdateUserId());
			tmpLogMap.put("log_job_type", "CONSOL");
			tmpLogMap.put("job_func_type", "UPDATE");
			tmpLogMap.put("result", "ok");
			tmpLogMap.put("process_date_time", System.currentTimeMillis());
			tmpLogMap.put("ent_business_print_name", consol.getLogUserBusinessPrintName());
			tmpLogMap.put("process_user_name", consol.getLogUserName());
			tmpLogMap.put("process_user_name_eng", consol.getLogUserNameEng());
			
			if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
			
			List<Map<String, Object>> insertPodList = new ArrayList<Map<String, Object>>();
			Map<String, Object> tmpInsertPodMap = null;
			int seq = 1;
			for(int i = 0; i < spliceHblNoList.size(); i++){
				for(int j = 0; j < spliceHblNoList.get(i).size(); j++){
					houseData = spliceHblNoList.get(i).get(j);
					order.setOdJobNo(Util.null2str(houseData.get("OD_JOB_NO")));
					order.setCurrencyCode(Util.null2str(houseData.get("CURRENCY_CODE")));
					order.setAmountCur(Util.null2dbl(houseData.get("AMOUNT_CUR")));
					order.setCdType(Util.null2str(houseData.get("CD_TYPE")));
					
					// 수출일때 마감후 콘솔 하우스의 출발지 코드가 없으면 마스터 출발지 코드를 따라가도록 수정 2025-06-30 장순문부장님 요청
					if("".equals(Util.null2str(houseData.get("ORIG_CODE")))) {
						order.setExpOrigYn("Y");
					}
					
					orderSaveMap.clear();
					/** 공통 - 환율처리 */
					orderSaveMap.put("exChangeYn", "Y");
					orderSaveMap.put("exrateMap", exchangeRateMap);
					if(consol.getBoundType().equals("EXP")){
						order.setSimpleDocNo("null");
						if(ediExpressSign != null && ("E".equals(order.getCdType()) || "Y".equals(order.getCdType()))){
							if("Y".equals(houseData.get("DOCUMENT_YN")) && !"".equals(ediExpressSignDocumentY)){
								orderSaveMap.put("createSimpleDocNo", "Y");
								orderSaveMap.put("ediExpressSign", ediExpressSignDocumentY);
							}else if("N".equals(houseData.get("DOCUMENT_YN")) && !"".equals(ediExpressSignDocumentN)){
								orderSaveMap.put("createSimpleDocNo", "Y");
								orderSaveMap.put("ediExpressSign", ediExpressSignDocumentN);
							}
						}
					}else{
						if(!"autoConsol".equals(origAction)) {
							
							// EDI 전송 여부 체크 및 전자상거래 Y/N을 체크해서 제출번호를 신규생성해야할지 기존에 제출번호를 사용할지 조회한다.
							houseData.put("MASTER_OD_JOB_NO", consol.getOdJobNo());
							if("".equals(Util.null2str(houseData.get("ECM_YN")))) {
								houseData.put("ECM_YN", "N");
							}
							Map<String , Object> simpleMap = consolMapper.selectEcmYnSimpleDocNo(houseData);
							
							// 신규 제출번호 생성 
							if(simpleMap == null) {

								if(documentY){ 
									this.createSimpleDocNoDocumentYn(ediExpressSignDocumentY, order, simpleDocNoMap, true);
								}
								if(documentN){
									this.createSimpleDocNoDocumentYn(ediExpressSignDocumentN, order, simpleDocNoMap, false);
								}
								
								if("Y".equals(houseData.get("DOCUMENT_YN"))){
									order.setSimpleDocNo(order.getSimpleDocNoDocumentY());
								}else{
									order.setSimpleDocNo(order.getSimpleDocNoDocumentN());
								}
								
							// 기존 제출번호 사용 
							}else {
								if(Util.isEmpty(simpleMap.get("SIMPLE_DOC_NO"))){
									order.setSimpleDocNo("null");
								}else{
									order.setSimpleDocNo(simpleMap.get("SIMPLE_DOC_NO").toString());
								}
							}
						}
					}
					orderCommonService.orderSave(order, "consol", orderSaveMap, seq);
					
					if(Util.isEmpty(houseData.get("ASSIGNMENT_WH_CODE"))){
						order.setAssignmentWhCode(consolOrder.getAssignmentWhCode());
					}else{
						order.setAssignmentWhCode(null);
					}
					/** 2025-06-20 하우스의 반출사유값이 없을경우만 마스터 정보로 업데이트 */
					if(Util.isEmpty(houseData.get("CARRYOUT_CODE"))){
						order.setCarryoutCode(consolOrder.getCarryoutCode());
					}else{
						order.setCarryoutCode(null);
					}
					order.setProfileName(profile);
//					consolMapper.updateConsolOrderBatch(order);
					consolBatchMapper.updateConsolOrderBatch(order);
					
					tmpInsertPodMap = new HashMap<String, Object>();
					tmpInsertPodMap.put("masterOdJobNo", order.getMasterOdJobNo());
					tmpInsertPodMap.put("odJobNo", order.getOdJobNo());
					tmpInsertPodMap.put("blNo", houseData.get("HBL_NO"));
					tmpInsertPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
					insertPodList.add(tmpInsertPodMap);
					
					count++;
					
					bulkData = new BulkData();
					bulkData.setActionType(BulkData.Type.CREATE);
					bulkData.setIndexName(index);
					bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
					
					logMap = new HashMap<String, Object>();
					logMap.putAll(tmpLogMap);
					logMap.put("bound", Util.null2str(houseData.get("BOUND")));
					logMap.put("bound_type", Util.null2str(houseData.get("BOUND_TYPE")));
					logMap.put("reference_no_id", bulkData.getId());
					logMap.put("job_no", Util.null2str(houseData.get("OD_JOB_NO")));
					logMap.put("description", "closeMasterConsol [MBL_NO : " + consolOrder.getMblNo() + ", HBL_NO : "+ houseData.get("HBL_NO") +"]");
//					logMap.put("job_log","{\"order\":" +new Gson().toJson(houseData)+", \"requestConsol\":" +new Gson().toJson(consol)+"}");
					logMap.put("job_log","");
					bulkData.setJsonMap(logMap);
					bulkList.add(bulkData);
				}
//				sqlSession.flushStatements();
//				sqlSession.clearCache();
//				sqlSession.commit();
			}
			
			
			// 마스터 멀티 콘솔 관련 업데이트 
			// 마스터의 상위업체 코드 값이 도입사면 마스터 상위업체 정보를 업데이트 해야 한다.
			if(consol.getEntCode().equals(consolOrder.getCustCodeLv1())) {
				consolMapper.updateMasterCustLvInfo(consolOrder);
			}
			
			/** 마스터제출번호가 없을경우 하우스에 생성된 제출번호로 업데이트 */
			if(Util.isEmpty(consolOrder.getSimpleDocNo())){
				if(!"null".equals(order.getSimpleDocNoDocumentN()) && !Util.isEmpty(order.getSimpleDocNoDocumentN())){
					Order masterOrder = new Order();
					masterOrder.setOdJobNo(consolOrder.getOdJobNo());
					masterOrder.setSimpleDocNo(order.getSimpleDocNoDocumentN());
					consolMapper.updateSimpleDocNo(masterOrder);
				}else if(!"null".equals(order.getSimpleDocNoDocumentY()) && !Util.isEmpty(order.getSimpleDocNoDocumentY())){
					Order masterOrder = new Order();
					masterOrder.setOdJobNo(consolOrder.getOdJobNo());
					masterOrder.setSimpleDocNo(order.getSimpleDocNoDocumentY());
					consolMapper.updateSimpleDocNo(masterOrder);
				}
			}
			
			Map<String, Object> podMap = new HashMap<String, Object>();
			podMap.put("entCode", 		consolOrder.getEntCode());
			podMap.put("entBusinessCode", "");
			podMap.put("gmtTime", 		consolOrder.getGmtTime());
			podMap.put("solution", 		"ECM");
			podMap.put("updateUserId", 	consol.getUpdateUserId());
			podMap.put("stockType", 	"POD");
			podMap.put("stockSign", 	"POD");
			podMap.put("stockSeqLength",8);
			podMap.put("resultCode", 	"FIXED_SIGN_AND_SEQ_NO");
			podMap.put("stockRemark", 	"ECM POD JOB NO");
			podMap.put("podStatus", 	"ECM055");
			podMap.put("podChkeckYn", 	"N");
			podMap.put("podTableName", 	"TB_ECM_POD_01");
			
			for(Map<String, Object> insertPodMap : insertPodList){
				podMap.put("odJobNo", insertPodMap.get("odJobNo"));
				orderBatchMapper.updateOrderLastPod(podMap);
			}
			for(Map<String, Object> insertPodMap : insertPodList){
				podMap.put("odJobNo", insertPodMap.get("odJobNo"));
				podMap.put("blNo", insertPodMap.get("blNo"));
				podMap.put("podJobNo", insertPodMap.get("podJobNo"));
				orderBatchMapper.insertOrderPod(podMap);
				
				//마감 후 콘솔시 마스터에 적하 전송 POD가 있는 경우 동일하게 넣어준다.
				Map<String,Object> consolPodMap = new HashMap<>();
				String masterOdJobNo = Util.null2str(insertPodMap.get("masterOdJobNo"));
				consolPodMap.put("odJobNo", masterOdJobNo);
				consolPodMap.put("podStatus","ECM100");
				consolPodMap.put("podTableName","TB_ECM_POD_02");
				consolPodMap = consolMapper.selectPodInfo(consolPodMap);
				if(consolPodMap != null) {
					consolPodMap.put("podTableName", 	"TB_ECM_POD_02");
					consolPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
					consolPodMap.put("entCode", 		consolOrder.getEntCode());
					consolPodMap.put("gmtTime", 		consolOrder.getGmtTime());
					consolPodMap.put("updateUserId", 	consol.getUpdateUserId());
					consolPodMap.put("odJobNo", insertPodMap.get("odJobNo"));
					consolPodMap.put("blNo", insertPodMap.get("blNo"));
					// pod insert
					orderBatchMapper.insertOrderPodInfo(consolPodMap);
				}
			}
			
			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
			
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(++seq));
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpLogMap);
			logMap.put("bound", consolOrder.getBound());
			logMap.put("bound_type", consolOrder.getBoundType());
			logMap.put("reference_no_id", bulkData.getId());
			logMap.put("job_no", consolOrder.getMasterOdJobNo());
			logMap.put("description", "closeMasterConsol [MBL_NO : " + consolOrder.getMblNo() + "]");
			logMap.put("job_log","");
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
		
		orderSaveResult.setResultCode("A11283"); // 성공 
		resultData.put("count", count);
		orderSaveResult.setResultData(resultData);
		
		commonService.insertLogBulk(bulkList);
		
		log.info("처리 : "+count);
		return orderSaveResult;
	}
	public void createSimpleDocNoDocumentYn(String ediExpressSign, Order order, Map<String, Object> simpleDocNoMap, boolean documentY) throws Exception{
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(simpleDocNoMap, "gmtTime", false, null, null, null, null);
		if(documentY){
			if(Util.isEmpty(ediExpressSign)){
				order.setSimpleDocNoDocumentY("null");
			}else{
				simpleDocNoMap.put("stockSign", ediExpressSign);
				order.setSimpleDocNoDocumentY(commonMapper.selectNextStockMaxNo(simpleDocNoMap));
			}
		}else{
			if(Util.isEmpty(ediExpressSign)){
				order.setSimpleDocNoDocumentN("null");
			}else{
				simpleDocNoMap.put("stockSign", ediExpressSign);
				order.setSimpleDocNoDocumentN(commonMapper.selectNextStockMaxNo(simpleDocNoMap));
			}
		}
	}
	
	/**
	 * Consolidation - closeMasterDeconsol
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult closeMasterDeconsol(ConsolOrder consol, HttpServletRequest request) throws Exception{
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		// 개발원 전송이력 삭제
		SendManifest sendManifest = new SendManifest();
		sendManifest.setModJobNoList(consol.getMasterOdJobNoList());
		sendManifest.setHouseOdJobNoList(consol.getOdJobNoList());
		ediManifestGwSendService.resetSendManifestGw(sendManifest);
		
		// 통관목록 전송이력 삭제
		sendManifest.setAction("resetSendCustomsManifest");
		ediCustomsManifestSendService.resetSendCustomsManifest(sendManifest);
		
		for(String odJobNo : consol.getOdJobNoList()){
			Map<String,Object> podMap = new HashMap<String, Object>();
			podMap = consolMapper.selectLastConsolPodInfo(odJobNo);
			if(podMap != null) {
				consolMapper.updateCloseOrderDeconsol(podMap);
				// ECM055(콘솔마감) 보다 큰 POD 삭제한다. 2024-10-29
				consolMapper.deletePodInfo(odJobNo);
			}
		}
		
		//마스터에 들어갈 상위업체 체크 
		//여러개의 상위업체가 섞인 하우스를 마스터와 콘솔하게 되면 마스터에는 상위업체값은 도입사를 넣게된다.
		Map<String,Object> consolMap = new HashMap<>();
		consolMap.put("odJobNo", consol.getMasterOdJobNoList().get(0));
		List<String> custList = consolMapper.selectCustCodeList(consolMap);
		if(custList != null && custList.size() > 0) {
			if(custList.size() == 1) {
				// 멀티 콘솔 이였던 마스터가 멀티가 아닌 단일 콘솔이 되어버린 경우에는 마스터 상위업체를 하우스 상위업체와 똑같이 맞춰준다.
				consol.setCustCodeLv1(custList.get(0).toString());
			}else {
				// 멀티 콘솔 이였던 마스터에서 특정 상위업체가 빠져 버리면 remark 에도 빠진 상위업체를 없애야한다.
				String custLv1Remark = String.join(",", custList);
				consol.setCustLv1Remark(custLv1Remark);
				consol.setCustCodeLv1(consol.getEntCode());
			}
			consol.setOdJobNo(consol.getMasterOdJobNoList().get(0));
			consolMapper.updateMasterCustLvInfo(consol);
		}
				
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", consol.getEntCode());
		tmpLogMap.put("ent_business_code", consol.getUserBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("bound", consol.getBound());
		tmpLogMap.put("bound_type", consol.getBoundType());
		tmpLogMap.put("process_user_id", consol.getUpdateUserId());
		tmpLogMap.put("log_job_type", "DECONSOL");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		tmpLogMap.put("ent_business_print_name", consol.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", consol.getLogUserName());
		tmpLogMap.put("process_user_name_eng", consol.getLogUserNameEng());
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		
		int seq = 1;
		for(String odJobNo : consol.getOdJobNoList()){
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
			
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpLogMap);
			logMap.put("reference_no_id", bulkData.getId());
			logMap.put("job_no", odJobNo);
			logMap.put("description", "closeMasterDeconsol [OD_JOB_NO : " + odJobNo +"]");
			logMap.put("job_log","");
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
		}
		commonService.insertLogBulk(bulkList);
		
		return orderSaveResult;
	}
	
	/**
	 * Consolidation - 콘솔불가오더체크
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public void consolErrorCheck(ConsolOrder consol, ConsolOrder consolOrder, List<List<Map<String, Object>>> spliceHblNoList, OrderSaveResult orderSaveResult, Map<String, Object> resultData) throws Exception{
		//MASTER_OD_JOB_NO 없는건만 조회도도록 쿼리 변경
//		List<Map<String, Object>> consolErrList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> errListChkList;
		List<Map<String, Object>> boundTypeErrList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> custCodeErrList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> pendingErrList = new ArrayList<Map<String, Object>>();
		List<String> nullOrderErrList = new ArrayList<String>();
		Set<String> custSet = new LinkedHashSet<>(); // 순서 유지 + 중복 제거
		String custLv1Remark = "";
		consolOrder.setProfileName(profile);
		
		//기존에 있는 하우스 상위업체 멀티 리스트 체크
		if(consol.getCustLv1List() != null && consol.getCustLv1List().size() > 0) {
			for(int i=0; i< consol.getCustLv1List().size(); i++) {
				custSet.add(consol.getCustLv1List().get(i));
			}
		}
		
		/** 콘솔불가하우스 체크 */
		List<String> hblNoList = new ArrayList<String>();
		hblNoList.addAll(consol.getHblNoList());
		int houseCnt = hblNoList.size();
		for(int i = 0; i < houseCnt; i+=500){
			if(i + 500 < houseCnt) {
				consolOrder.setHblNoList(hblNoList.subList(i, i + 500));
			}else{
				consolOrder.setHblNoList(hblNoList.subList(i, houseCnt));
			}
			errListChkList = consolMapper.selectConsolIncongruityOrder(consolOrder);
			if(errListChkList.size() > 0){
				
				Map<String,Object> custMap = errListChkList.get(0);
				String custRemark = Util.null2str(custMap.get("CUST_LV1_REMARK"));
				String[] custArr = custRemark.split(",");
				for(String cust : custArr) {
					custSet.add(cust);
				}
				
				for(Map<String, Object> errMap : errListChkList){
//					if(Util.null2str(errMap.get("CONSOL_YN")).equals("Y")){
//						consolErrList.add(errMap);
//					}
					if("".equals(Util.null2str(consol.getNoBoundConsol()))) {
						if(!Util.null2str(errMap.get("BOUND")).equals(consolOrder.getBound()) || !Util.null2str(errMap.get("BOUND_TYPE")).equals(consolOrder.getBoundType())){
							boundTypeErrList.add(errMap);
						}
					}else {
						if(!Util.null2str(errMap.get("BOUND_TYPE")).equals(consolOrder.getBoundType())){
							boundTypeErrList.add(errMap);
						}
					}
					if(!Util.null2str(errMap.get("CUST_CODE_LV1")).equals(consolOrder.getCustCodeLv1())){
//						custCodeErrList.add(errMap);
					}
					if(Util.null2str(errMap.get("LAST_POD_STATUS")).equals("ECM040")){
						pendingErrList.add(errMap);
					}
				}
				spliceHblNoList.add(errListChkList);
			}
			
			/** 선택한 건수와 조회된 건수가 다를경우 */
			if(errListChkList.size() != consolOrder.getHblNoList().size()){
				String dataHblNo;
				boolean hblChkFlag;
				int chkHblNoSize = consolOrder.getHblNoList().size();
				int dataHblNoSize = errListChkList.size();
				for(int j = chkHblNoSize - 1; j >= 0; j--){
					hblChkFlag = false;
					for(int k = dataHblNoSize - 1; k >= 0; k--){
						dataHblNo = Util.null2str(errListChkList.get(k).get("HBL_NO"));
						if(dataHblNo.equals(consolOrder.getHblNoList().get(j))){
							errListChkList.remove(k);
							dataHblNoSize--;
							hblChkFlag = true;
							continue;
						}
					}
					if(!hblChkFlag){
						nullOrderErrList.add(consolOrder.getHblNoList().get(j));
					}
				}
			}
			
		}
		
		// 콘솔한 하우스들의 상위업체가 여러개면 마스터 상위업체에 도입사를 넣는다. remark에는 상위업체들을 ,로 나열한다.
		if(!consolOrder.getCustCodeLv1().equals(consolOrder.getEntCode()) && !custSet.contains(consolOrder.getCustCodeLv1())) {
			orderSaveResult.setResultCode("consolCustCodeError");
		}else {
			custLv1Remark = String.join(",", custSet);
			if(custLv1Remark.contains(",")) {
				consolOrder.setCustCodeLv1(consolOrder.getEntCode());
				consolOrder.setCustLv1Remark(custLv1Remark);
			}
		}
		
//		if(consolErrList.size() > 0){
//			orderSaveResult.setResultCode("consolIncongruityOrder");
//			resultData.put("consolErrList", consolErrList);
//		}
		if(boundTypeErrList.size() > 0){
			orderSaveResult.setResultCode("consolIncongruityOrder");
			resultData.put("boundTypeErrList", boundTypeErrList);
			resultData.put("bound", consolOrder.getBound());
			resultData.put("boundType", consolOrder.getBoundType());
		}
		if(custCodeErrList.size() > 0){
			orderSaveResult.setResultCode("consolIncongruityOrder");
			resultData.put("custCodeErrList", custCodeErrList);
			resultData.put("custCode", consolOrder.getCustCodeLv1());
		}
		if(pendingErrList.size() > 0){
			orderSaveResult.setResultCode("consolIncongruityOrder");
			resultData.put("pendingErrList", pendingErrList);
		}
		if(nullOrderErrList.size() > 0){
			orderSaveResult.setResultCode("consolIncongruityOrder");
			resultData.put("nullOrderErrList", nullOrderErrList);
		}
	}
	/**
	 * Consolidation - 마스터저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult insertMasterOrder(ConsolOrder consolOrder, HttpServletRequest request) throws Exception{

		Map<String, Object> busEdiEcmMap = null;
		ConsolOrder oldConsolOrder = null;
		
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		List<Map<String, Object>> errList;
//		List<Map<String, Object>> consolErrList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> boundTypeErrList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> custCodeErrList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> pendingErrList = new ArrayList<Map<String, Object>>();
		List<String> nullOrderErrList = new ArrayList<String>();
		List<Map<String, Object>> origCodeErrList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> destCodeErrList = new ArrayList<Map<String, Object>>();
		
		List<Map<String, Object>> consolTarget = new ArrayList<Map<String, Object>>();
		List<String> masterOdJobNoList = consolOrder.getMasterOdJobNoList();
		String consolYn = Util.null2str(consolOrder.getConsolYn());
		String action = Util.null2str(consolOrder.getAction());
		String masterJobFunc = null;
		String description = null;
		consolOrder.setProfileName(profile);
		
		MasterOrder masterOrder = null;
		List<MasterOrder> deleteList = null;
		MasterOrderDelete masterOrderDelete = null;
		
		String custLv1Remark = "";
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(consolOrder, "gmtTime|dateFormat", false, null, null, null, null);
		
		/** 바운드가 없는 데이터를 콘솔 할때 **/
		/**
		 * 도입사/거래처(한국) : IMP->IMP엔트리, EXP->EXP엔트리
		 * 거래처(!한국)	: IMP->EXP엔트리, EXP->IMP엔트리
		 */
		if("Y".equals(Util.null2str(consolOrder.getReverseBoundType()))) {
			if("IMP".equals(consolOrder.getBoundType()))consolOrder.setBoundType("EXP");
			else if("EXP".equals(consolOrder.getBoundType()))consolOrder.setBoundType("IMP");
		}
		
		/** 비엘번호 중복체크 */
		tmpMap.put("entCode", consolOrder.getEntCode());
		tmpMap.put("blNo", consolOrder.getMblNo());
		tmpMap.put("blTypeMh", "M");
		tmpMap.put("masterOdJobNoList", masterOdJobNoList);	/** 마스터콘솔용 */
		tmpMap.put("odJobNo", consolOrder.getOdJobNo());	/** 마스터수정용 */
		Map<String, Object> blNoChkMap = orderMapper.selectOrderByBlNo(tmpMap);
		if(blNoChkMap != null && blNoChkMap.size() > 0){
			orderSaveResult.setResultCode("duplicateBlNo");
			return orderSaveResult;
		}
		
		if(masterOdJobNoList != null){	/** 마스터콘솔 */
			/** 마감체크 */
			this.closeChkList(orderSaveResult, masterOdJobNoList, "odJobNoList");
			if(!Util.null2str(orderSaveResult.getResultCode()).equals("")){
				return orderSaveResult;
			}
			
			Set<String> custSet = new LinkedHashSet<>(); // 순서 유지 + 중복 제거
			
			masterOrder = new MasterOrder();
			masterOrder.setOdJobNoList(masterOdJobNoList);
			deleteList = masterOrderMapper.selectEcmOrderList(masterOrder);
			masterOrderDelete = new MasterOrderDelete();
			masterOrderDelete.setAction("check");
			/** 최대 콘솔 가능 건수 초과 여부 체크 */
			int houseCnt = 0;
			for(MasterOrder data : deleteList){
				houseCnt += data.getHouseCnt();
				custSet.add(data.getCustCodeLv1());
			}
			if(houseCnt > 9999) {
				orderSaveResult.setResultCode("A11488");
				return orderSaveResult;
			}
			for(MasterOrder data : deleteList){
				masterOrderDelete.setOblJobNo(data.getOblJobNo());
				orderSaveResult.setResultCode(masterOrderService.deleteMasterOrder(masterOrderDelete, request).getResultCode());
				if(!Util.isEmpty(orderSaveResult.getResultCode())){
					return orderSaveResult;
				}
			}
			
			/** 멀티콘솔 로직 **/
			custLv1Remark = String.join(",", custSet);
			if(custLv1Remark.contains(",")) {
				consolOrder.setCustCodeLv1(consolOrder.getEntCode());
				consolOrder.setCustLv1Remark(custLv1Remark);
			}else {
				if(!custLv1Remark.equals(consolOrder.getCustCodeLv1())){
					orderSaveResult.setResultCode("consolCustCodeError");
				}
			}
			
			errList = consolMapper.selectConsolIncongruityOrder(consolOrder);
			if(errList != null && errList.size() > 0){
				if(errList.size() > 0){
					for(Map<String, Object> errMap : errList){
						if(!Util.null2str(errMap.get("CUST_CODE_LV1")).equals(consolOrder.getCustCodeLv1())){
//							custCodeErrList.add(errMap);
						}
						if(!Util.null2str(errMap.get("ORIG_CODE")).equals(consolOrder.getOrigCode())){
							origCodeErrList.add(errMap);
						}
						if(!Util.null2str(errMap.get("DEST_CODE")).equals(consolOrder.getDestCode())){
							destCodeErrList.add(errMap);
						}
					}
					orderSaveResult.setResultCode("consolIncongruityOrder");
					if(custCodeErrList.size() > 0){
						resultData.put("custCodeErrList", custCodeErrList);
						resultData.put("custCodeErrList", errList);
					}
					if(origCodeErrList.size() > 0){
						resultData.put("origCodeErrList", origCodeErrList);
						resultData.put("origCodeErrList", errList);
					}
					if(destCodeErrList.size() > 0){
						resultData.put("destCodeErrList", destCodeErrList);
						resultData.put("destCodeErrList", errList);
					}
					orderSaveResult.setResultData(resultData);
					return orderSaveResult;
				}
			}
		}else{	/** 마스터신규 or 마스터수정 */
			/** 콘솔불가하우스 체크 */
			if(consolYn.equals("Y")){
				List<String> hblNoList = new ArrayList<String>();
				hblNoList.addAll(consolOrder.getHblNoList());
				int houseCnt = hblNoList.size();
				
				if(!action.equals("update") && houseCnt > 9999) {
					Map<String, Object> errMap = new HashMap<String, Object>();
					errMap.put("mblNoStr", consolOrder.getMblNo());
					orderSaveResult.setResultCode("A11488");	/** 최대 콘솔 가능 건수(9,999)를 초과하였습니다. */
					orderSaveResult.setResultData(errMap);
					return orderSaveResult;
				}
				
				Set<String> custSet = new LinkedHashSet<>(); // 순서 유지 + 중복 제거
				
				for(int i = 0; i < houseCnt; i+=500){
					if(i + 500 < houseCnt) {
						consolOrder.setHblNoList(hblNoList.subList(i, i + 500));
					}else{
						consolOrder.setHblNoList(hblNoList.subList(i, houseCnt));
					}
					errList = consolMapper.selectConsolIncongruityOrder(consolOrder);
					
					if(errList.size() > 0){
						
						Map<String,Object> custMap = errList.get(0);
						String custRemark = Util.null2str(custMap.get("CUST_LV1_REMARK"));
						String[] custArr = custRemark.split(",");
						for(String cust : custArr) {
							custSet.add(cust);
						}
						
						for(Map<String, Object> errMap : errList){
//							if(Util.null2str(errMap.get("CONSOL_YN")).equals("Y")){
//								consolErrList.add(errMap);
//							}
							if("".equals(Util.null2str(consolOrder.getNoBoundConsol()))) {
								if(!Util.null2str(errMap.get("BOUND")).equals(consolOrder.getBound()) || !Util.null2str(errMap.get("BOUND_TYPE")).equals(consolOrder.getBoundType())){
									boundTypeErrList.add(errMap);
								}
							}else {
								if(!Util.null2str(errMap.get("BOUND_TYPE")).equals(consolOrder.getBoundType())){
									boundTypeErrList.add(errMap);
								}
							}
							if(!Util.null2str(errMap.get("CUST_CODE_LV1")).equals(consolOrder.getCustCodeLv1())){
//								custCodeErrList.add(errMap);
							}
							if(Util.null2str(errMap.get("LAST_POD_STATUS")).equals("ECM040")){
								pendingErrList.add(errMap);
							}
						}
						consolTarget.addAll(errList);	/** 신규콘솔될 하우스 */
					}
					/** 선택한 건수와 조회된 건수가 다를경우 */
					if(errList.size() != consolOrder.getHblNoList().size()){
						String dataHblNo;
						boolean hblChkFlag;
						int chkHblNoSize = consolOrder.getHblNoList().size();
						int dataHblNoSize = errList.size();
						for(int j = chkHblNoSize - 1; j >= 0; j--){
							hblChkFlag = false;
							for(int k = dataHblNoSize - 1; k >= 0; k--){
								dataHblNo = Util.null2str(errList.get(k).get("HBL_NO"));
								if(dataHblNo.equals(consolOrder.getHblNoList().get(j))){
									errList.remove(k);
									dataHblNoSize--;
									hblChkFlag = true;
									continue;
								}
							}
							if(!hblChkFlag){
								nullOrderErrList.add(consolOrder.getHblNoList().get(j));
							}
						}
					}
				}
				
				// 콘솔한 하우스들의 상위업체가 여러개면 마스터 상위업체에 도입사를 넣는다. remark에는 상위업체들을 ,로 나열한다.
				custLv1Remark = String.join(",", custSet);
				if(custLv1Remark.contains(",")) {
					consolOrder.setCustCodeLv1(consolOrder.getEntCode());
					consolOrder.setCustLv1Remark(custLv1Remark);
				}else {
					if(!custLv1Remark.equals(consolOrder.getCustCodeLv1())){
						orderSaveResult.setResultCode("consolCustCodeError");
					}
				}
				
//				if(consolErrList.size() > 0){
//					orderSaveResult.setResultCode("consolIncongruityOrder");
//					resultData.put("consolErrList", consolErrList);
//				}
				if(boundTypeErrList.size() > 0){
					orderSaveResult.setResultCode("consolIncongruityOrder");
					resultData.put("boundTypeErrList", boundTypeErrList);
				}
				if(custCodeErrList.size() > 0){
					orderSaveResult.setResultCode("consolIncongruityOrder");
					resultData.put("custCodeErrList", custCodeErrList);
				}
				if(pendingErrList.size() > 0){
					orderSaveResult.setResultCode("consolIncongruityOrder");
					resultData.put("pendingErrList", pendingErrList);
				}
				if(nullOrderErrList.size() > 0){
					orderSaveResult.setResultCode("consolIncongruityOrder");
					resultData.put("nullOrderErrList", nullOrderErrList);
				}
			}
		}
		
		if(!Util.null2str(orderSaveResult.getResultCode()).equals("")){
			if(Util.null2str(orderSaveResult.getResultCode()).equals("consolIncongruityOrder")){
				orderSaveResult.setResultData(resultData);
			}
			return orderSaveResult;
		}
		
		String ediExpressSign = null, ediExpressSignDocumentY = null, ediExpressSignDocumentN = null, simpleDocNoDocumentY = null, simpleDocNoDocumentN = null;
		boolean updateSimpleDocNo = false, documentY = false, documentN = false;
		
		if(action.equals("update")){
			
			tmpMap.put("masterOdJobNo", consolOrder.getOdJobNo());
			Map<String, Object> saveChkMap = commonMapper.selectSimpleDocSendYn(tmpMap);
			if("1".equals(consolOrder.getRegisterCustomerLevel())){
				if("Y".equals(saveChkMap.get("FINISH_YN"))){
					orderSaveResult.setResultCode("A11237");
					return orderSaveResult;
				}
			}else if(!"AD".equals(consolOrder.getUserAuth())){
				if(Util.null2str(saveChkMap.get("SEND_YN")).equals("Y")){
					orderSaveResult.setResultCode("simpleDocSend");
					return orderSaveResult;
				}
			}
			
			tmpMap.put("masterOdJobNo", null);
			
			oldConsolOrder = consolMapper.selectConsolMasterInfo(consolOrder);
			if(oldConsolOrder != null){
				consolOrder.setMrn(oldConsolOrder.getMrn());
				consolOrder.setMsn(oldConsolOrder.getMsn());
//				consolOrder.setSimpleDocNo(oldConsolOrder.getSimpleDocNo());
				consolOrder.setOldSimpleDocNo(oldConsolOrder.getSimpleDocNo());
				consolOrder.setCarryoutCode(oldConsolOrder.getCarryoutCode());
			}
			
			/** 기존 콘솔된 하우스 */
			consolTarget.addAll(consolMapper.selectConsolTarget(consolOrder));
			
			if("Y".equals(Util.null2str(oldConsolOrder.getFinishYn()))){
				if(Util.null2str((consolOrder.getBoundType())).equals("EXP") && !Util.null2str(oldConsolOrder.getOrigCode()).equals(consolOrder.getOrigCode()) || Util.null2str(consolOrder.getBoundType()).equals("IMP") && !Util.null2str(oldConsolOrder.getDestPort()).equals(consolOrder.getDestPort())) {
					updateSimpleDocNo = true;
					/** 통관목록제출번호   수입은 마스터, 수출은 하우스별로 제출번호 생성 */
					tmpMap.put("srchEntCode", consolOrder.getEntCode());
					tmpMap.put("srchEntBusinessCode", consolOrder.getEntBusinessCode());
					tmpMap.put("srchBound", consolOrder.getBound());
					if(consolOrder.getBoundType().equals("EXP")){
						tmpMap.put("srchEdiCityPort", consolOrder.getOrigCode());
					}else{
						tmpMap.put("srchEdiCityPort", consolOrder.getDestPort());
					}
					
					if(consolOrder.getBoundType().equals("IMP")){
						for(Map<String, Object> consol : consolTarget){
							if("Y".equals(consol.get("DOCUMENT_YN"))){
								documentY = true;
							}else{
								documentN = true;
							}
							if(documentY && documentN){
								break;
							}
						}
					}
					
					/** 취약성점검 SQL Injection 방지 */
					Util.accessControlDatabase(tmpMap, "stockDateFormat", false, null, null, null, null);
					/** 제출번호에 사용되는 특송부호 조회 */
					busEdiEcmMap = commonMapper.selectBusEdiCodeInfo(tmpMap);
					if(busEdiEcmMap != null && busEdiEcmMap.size() > 0){
						ediExpressSign = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN"));
						ediExpressSignDocumentY = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y"));
						ediExpressSignDocumentN = Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N"));
						if(consolOrder.getBoundType().equals("IMP")){
							tmpMap.put("entCode", consolOrder.getEntCode());
							tmpMap.put("gmtTime", consolOrder.getGmtTime());
							tmpMap.put("entBusinessCode", "");
							tmpMap.put("updateUserId", consolOrder.getUpdateUserId());
							tmpMap.put("solution", "ECM");
							tmpMap.put("stockType", "OD");
							tmpMap.put("stockSeqLength", "8");
							tmpMap.put("dateLength", "2");
							tmpMap.put("signType", "noHyphen");
							tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
							tmpMap.put("stockRemark", "ORDER SIMPLE DOC NO");
							if(documentY){
								if(Util.isEmpty(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y"))){
									simpleDocNoDocumentY = "null";
								}else{
									tmpMap.put("stockSign", busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y"));
									simpleDocNoDocumentY = commonMapper.selectNextStockMaxNo(tmpMap);
								}
							}
							if(documentN || (!documentY && !documentN)){
								if(Util.isEmpty(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N"))){
									simpleDocNoDocumentN = "null";
								}else{
									tmpMap.put("stockSign", busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N"));
									simpleDocNoDocumentN = commonMapper.selectNextStockMaxNo(tmpMap);
								}
							}
						}
					}else{
						simpleDocNoDocumentY = "null";
						simpleDocNoDocumentN = "null";
					}
				}
			}
		}else if(consolOrder.getBound().equals("AIR") && consolOrder.getBoundType().equals("EXP")){
			consolOrder.setCarryoutCode("GG");
		}
		
		Map<String,Object> paramMap = new HashMap<String,Object>();
		/** 관세청사서함 */
		paramMap.put("srchEntCode", consolOrder.getEntCode());
		paramMap.put("srchEntBusinessCode", consolOrder.getEntBusinessCode());
		paramMap.put("srchBound", consolOrder.getBound());
		paramMap.put("srchBoundType", consolOrder.getBoundType());
		if(consolOrder.getBoundType().equals("EXP")){
			paramMap.put("srchEdiCityPort", consolOrder.getOrigCode());
		}else{
			paramMap.put("srchEdiCityPort", consolOrder.getDestPort());
		}
		paramMap = commonMapper.selectBusEdiEcmInfo(paramMap);
		if(paramMap != null && paramMap.size() > 0){
			consolOrder.setEdiMailboxSeqNo(Util.null2str(paramMap.get("SEQ_NO")));
		}else{
			paramMap = new HashMap<String,Object>();
		}
		
		if(action.equals("insert")){
			if(!Util.null2str(consolOrder.getRegisterCustomerLevel()).equals("")) {
				//LVL1~7 추출
				paramMap.put("entCode", consolOrder.getEntCode());
				paramMap.put("userId", consolOrder.getRegisterUserId());
				Map<String,Object> lvlMap = orderCustomerMapper.selectCusLvl(paramMap);
				
				if(lvlMap == null) {
					//로그인유저의 거래처코드가 없는 경우 에러 
					orderSaveResult.setResultCode("NOT_CUS_CODE");
					return orderSaveResult;
				}
				
				String userCusCd = (String)lvlMap.get("USER_CUSTOMER_CODE");
				//로그인유저의 거래처코드가 없는 경우 에러 
				if(StringUtils.isEmpty(userCusCd)) {
					orderSaveResult.setResultCode("NOT_CUS_CODE");
					return orderSaveResult;
				}
				
				//로그인유저의 거래처코드의 레벨로 상위계층레벨 거래처코드 셋팅 
				consolOrder.setCustCodeLv1(Util.null2str(lvlMap.get("CUST_CODE_LV1")));
				consolOrder.setCustCodeLv2(Util.null2str(lvlMap.get("CUST_CODE_LV2")));
				consolOrder.setCustCodeLv3(Util.null2str(lvlMap.get("CUST_CODE_LV3")));
				consolOrder.setCustCodeLv4(Util.null2str(lvlMap.get("CUST_CODE_LV4")));
				consolOrder.setCustCodeLv5(Util.null2str(lvlMap.get("CUST_CODE_LV5")));
				consolOrder.setCustCodeLv6(Util.null2str(lvlMap.get("CUST_CODE_LV6")));
				consolOrder.setCustCodeLv7(Util.null2str(lvlMap.get("CUST_CODE_LV7")));
			}
			
			/** 중개사업자 */
			paramMap.put("entCode", consolOrder.getEntCode());
			paramMap.put("entBusinessCode", consolOrder.getEntBusinessCode());
			paramMap.put("solution", "ECM");
			if(consolOrder.getBound().equals("AIR")){
				paramMap.put("optionCode", "C02");
				Map<String, Object> map = commonMapper.selectSystemOption2(paramMap);
				/** 항공일경우 중계사업자 설정값이 auto이면 편명의 중계사업자로 저장 */
				if("AUTO".equals(Util.null2str(map.get("SET_VALUE")).toUpperCase())){
					String venderId = "";
					if(!Util.isEmpty(consolOrder.getLineCode())){
						paramMap.put("groupCode", "AIRLINE_CODE");
						paramMap.put("srchEtcFieldOnly", "Y");
						paramMap.put("srchEtcField", "ETC2");
						paramMap.put("srchEtcFieldValue", consolOrder.getLineCode());
						paramMap = commonMapper.getCodeDetail(paramMap);
						if(paramMap != null){
							if(consolOrder.getBoundType().equals("EXP")){
								venderId = Util.null2str(paramMap.get("ETC10"));
							}else{
								venderId = Util.null2str(paramMap.get("ETC11"));
							}
						}
					}
					if(venderId.equals("")){
						consolOrder.setVenderId1(Util.null2str(map.get("DEFAULT_VALUE")));
					}else{
						consolOrder.setVenderId1(venderId);
					}
				}else if(Util.isEmpty(map.get("SET_VALUE"))){
					consolOrder.setVenderId1(Util.null2str(map.get("DEFAULT_VALUE")));
				}else{
					consolOrder.setVenderId1(Util.null2str(map.get("SET_VALUE")));
				}
			}else{
				paramMap.put("optionCode", "C03");
				consolOrder.setVenderId1(commonMapper.selectSystemOption(paramMap));
			}
			
			/** 마스터오더 insert(신규 or 마스터콘솔) */
			tmpMap.put("entCode", consolOrder.getEntCode());
			tmpMap.put("gmtTime", consolOrder.getGmtTime());
			tmpMap.put("entBusinessCode", "");
			tmpMap.put("updateUserId", consolOrder.getUpdateUserId());
			tmpMap.put("solution", "ECM");
			tmpMap.put("stockType", "OD");
			tmpMap.put("stockSign", "COD");
			tmpMap.put("stockSeqLength", 8);
			tmpMap.put("dateLength", null);
			tmpMap.put("signType", null);
			tmpMap.put("stockDateFormat", null);
			tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			tmpMap.put("stockRemark", "ORDER-JOB NO");
			consolOrder.setOdJobNo(commonMapper.selectNextStockMaxNo(tmpMap));
			consolOrder.setProfileName(profile);
			consolMapper.insertMasterOrder(consolOrder);
			if(masterOdJobNoList == null){
				description = "insert consol";
				masterJobFunc = "ADD";
			}
		}else{
			description = "update consol";
			masterJobFunc = "UPDATE";
		}
		
		SqlSession sqlSession = null;
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		Map<String, Object> logMap = null;
		
		String masterJoblog = new Gson().toJson(consolOrder);
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
	
		tmpLogMap.put("ent_code", consolOrder.getEntCode());
		tmpLogMap.put("ent_business_code", consolOrder.getUserBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("process_user_id", consolOrder.getRegisterUserId());
		tmpLogMap.put("log_job_type", "CONSOL");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		tmpLogMap.put("ent_business_print_name", consolOrder.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", consolOrder.getLogUserName());
		tmpLogMap.put("process_user_name_eng", consolOrder.getLogUserNameEng());
		
		
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
				
		int cnt = 1;
		int seq = 1;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			ConsolMapper consolBatchMapper = sqlSession.getMapper(ConsolMapper.class);
			
			if(consolYn.equals("Y")){
				if(masterOdJobNoList != null){	/** 마스터콘솔 */
					/** 기존마스터삭제 */
					ConsolClose consolClose = new ConsolClose();
					consolClose.setMasterOdJobNoList(masterOdJobNoList);
					consolMapper.deleteMasterOrder(consolClose);
					consolMapper.deleteMasterOrderAdditional(consolClose);
					
					/** TB_COM_OD삭제 */
					masterOrderDelete.setBound(consolOrder.getBound());
					masterOrderDelete.setEntCode(consolOrder.getEntCode());
					masterOrderDelete.setUserBusinessCode(consolOrder.getUserBusinessCode());
					masterOrderDelete.setBoundType(consolOrder.getBoundType());
					masterOrderDelete.setUpdateUserId(consolOrder.getUpdateUserId());
					masterOrderDelete.setLogUserBusinessPrintName(consolOrder.getLogUserBusinessPrintName());
					masterOrderDelete.setLogUserName(consolOrder.getLogUserName());
					masterOrderDelete.setLogUserNameEng(consolOrder.getLogUserNameEng());
					masterOrderDelete.setAction(null);
					for(MasterOrder data : deleteList){
						if(!Util.isEmpty(data.getOblJobNo())){
							masterOrderDelete.setOdJobNo(data.getOdJobNo());
							masterOrderDelete.setOblJobNo(data.getOblJobNo());
							masterOrderService.deleteMasterOrder(masterOrderDelete, request);
						}
					}
					
					description = "maseter consol";
					masterJobFunc = "ADD";
					/** 하우스 조회 */
					consolTarget = consolMapper.selectConsolTarget(consolOrder);
				}else{	/** 마스터신규(하우스선택 or 엑셀있는경우) or 마스터수정(엑셀있는경우)  */
					consolOrder.setConsolYn("Y");
				}
			}else if(action.equals("update")){	/** 마스터수정(엑셀없는경우) */
				consolOrder.setConsolYn(null);
			}
			
			if(action.equals("update")){	/** 마스터수정 */
				if(updateSimpleDocNo && consolOrder.getBoundType().equals("IMP")){
					if(!Util.isEmpty(simpleDocNoDocumentN) && !"null".equals(simpleDocNoDocumentN)){
						consolOrder.setSimpleDocNo(simpleDocNoDocumentN);
					}else if(!Util.isEmpty(simpleDocNoDocumentY) && !"null".equals(simpleDocNoDocumentY)){
						consolOrder.setSimpleDocNo(simpleDocNoDocumentY);
					}else{
						consolOrder.setSimpleDocNo("null");
					}
				}
				consolMapper.updateConsolOrder(consolOrder);
				
				/** TB_COM_OD수정 */
				if(!Util.isEmpty(oldConsolOrder.getOblJobNo())){
					consolOrder.setOblJobNo(oldConsolOrder.getOblJobNo());
					consolOrder.setIssuingCarriers(oldConsolOrder.getIssuingCarriers());
					String tmpLineCode = "", tmpLineName = "";
					if(consolOrder.getBound().equals("AIR")){
						tmpLineCode = consolOrder.getLineCode();
						tmpLineName = consolOrder.getLineName();
						if(!oldConsolOrder.getMblNo().equals(consolOrder.getMblNo())){
							consolOrder.setLineCode(null);
							consolOrder.setLineName(null);
							if(consolOrder.getMblNo().length() > 2){
								Map<String, Object> lineCode = new HashMap<String, Object>();
								lineCode.put("entCode", consolOrder.getEntCode());
								lineCode.put("entBusinessCode", consolOrder.getEntBusinessCode());
								lineCode.put("solution", "FMS");
								lineCode.put("optionCode", "A34");
								lineCode.put("customerCode", consolOrder.getMblNo().substring(0, 3));
								lineCode = commonMapper.selectCustomerInfo(lineCode);
								if(lineCode!= null && lineCode.size() > 0){
									consolOrder.setLineCode(Util.null2str(lineCode.get("CUSTOMER_CODE")));
									consolOrder.setLineName(Util.null2str(lineCode.get("CUSTOMER_NAME_ENG")));
//									if(Util.isEmpty(oldConsolOrder.getIssuingCarriers())){
//										consolOrder.setIssuingCarriers(consolOrder.getLineName());
//									}
									
									consolOrder.setFwrdCode(consolOrder.getLineCode());
									consolOrder.setFwrdName(consolOrder.getLineName());
									
									String addressEng = Util.setAddress(Util.null2str(lineCode.get("ADDRESS_ENG")));
									String tmpStr = Util.null2str(lineCode.get("ADDRESS_A_ENG")).trim();
									if(!tmpStr.equals(""))	addressEng += "\n" + Util.setAddress(tmpStr);
									
									tmpStr = Util.null2str(lineCode.get("ADDRESS_B_ENG")).trim();
									if(!tmpStr.equals(""))	addressEng += "\n" + Util.setAddress(tmpStr);
									
									tmpStr = Util.null2str(lineCode.get("ATTN_ENG")).trim();
									if(!tmpStr.equals(""))	addressEng += "\nATTN: " + Util.setAddress(tmpStr);
									
									tmpStr = Util.null2str(lineCode.get("TEL_ENG")).trim();
									if(!tmpStr.equals("")){
										addressEng += "\nTEL: " + Util.setAddress(tmpStr);
										tmpStr = Util.null2str(lineCode.get("FAX_ENG")).trim();
										if(!tmpStr.equals(""))	addressEng += "   FAX: " + Util.setAddress(tmpStr);
									}else{
										tmpStr = Util.null2str(lineCode.get("FAX_ENG")).trim();
										if(!tmpStr.equals(""))	addressEng += "\nFAX: " + Util.setAddress(tmpStr);
									}
									
									consolOrder.setFwrdAddress(addressEng);
									
									consolOrder.setSignActingAs("AS AGENT FOR THE CARRIER : " + consolOrder.getLineName());
									lineCode.clear();
									lineCode.put("entCode", consolOrder.getEntCode());
									lineCode.put("entBusinessCode", consolOrder.getEntBusinessCode());
									lineCode.put("solution", "FMS");
									lineCode.put("optionCode", "A34");
									if(!"1".equals(commonMapper.selectSystemOption(lineCode))){
										consolOrder.setSignHisAgent(consolOrder.getSignActingAs());
									}
								}
							}
						}else{
							consolOrder.setLineCode("noUpdate");
						}
					}
					consolMapper.updateComObl(consolOrder);
					consolMapper.updateComOrder(consolOrder);
					consolMapper.updateComOrderBus(consolOrder);
					if(consolOrder.getBound().equals("AIR")){
						consolOrder.setLineCode(tmpLineCode);
						consolOrder.setLineName(tmpLineName);
					}
					
					ProcessLogs processLogs = new ProcessLogs();
					processLogs.setJobFuncType("UPDATE");
					processLogs.setEntCode(consolOrder.getEntCode());
					processLogs.setEntBusinessCode(consolOrder.getUserBusinessCode());
					processLogs.setSolution("ECM");
					processLogs.setBound(consolOrder.getBound());
					processLogs.setBoundType(consolOrder.getBoundType());
					processLogs.setProcessUserId(consolOrder.getUpdateUserId());
					processLogs.setJobNo(consolOrder.getOblJobNo());
					processLogs.setLogJobType("DATA");
					processLogs.setDescription("CONSOL UPDATE[MBL_NO : " + consolOrder.getMblNo()+"]");
					processLogs.setJobLog(new Gson().toJson(consolOrder));
					processLogs.setResult("ok");
					processLogs.setEntBusinessPrintName(consolOrder.getLogUserBusinessPrintName());
					processLogs.setProcessUserName(consolOrder.getLogUserName());
					processLogs.setProcessUserNameEng(consolOrder.getLogUserNameEng());
					commonService.insertLog(processLogs, request);
				}
			}
			
			/** 환율계산 */
			if(consolTarget != null && consolTarget.size() > 0){
				tmpMap.put("srchEntCode", consolOrder.getEntCode());
				tmpMap.put("entBusinessCode", consolOrder.getEntBusinessCode());
				tmpMap.put("srchDateFormat", "23");
				if(Util.null2str(consolOrder.getBoundType()).equals("EXP")){
					tmpMap.put("exrateType", "EXRATE_SELLING_CUSTOMS");
					tmpMap.put("srchDate", Util.null2str(consolOrder.getObDate()));
				}else{
					tmpMap.put("exrateType", "EXRATE_BUYING_CUSTOMS");
					tmpMap.put("srchDate", Util.null2str(consolOrder.getArDate()));
				}
				/** 취약성점검 SQL Injection 방지 */
				Util.accessControlDatabase(tmpMap, "exrateType|srchDateFormat", false, null, null, null, null);
				Map<String, Object> exchangeRateMap = commonMapper.selectExchangeRate(tmpMap);
				
				consolOrder.setMasterOdJobNo(consolOrder.getOdJobNo());
				
				Map<String, Object> orderSaveMap = new HashMap<String, Object>();
				Order order = new Order();
				order.setEntCode(consolOrder.getEntCode());
				order.setMblNo(consolOrder.getMblNo());
				order.setMasterOdJobNo(consolOrder.getMasterOdJobNo());
				order.setLineCode(consolOrder.getLineCode());
				order.setVesselFlight(consolOrder.getVesselFlight());
				order.setOrigCode(consolOrder.getOrigCode());
				order.setOrigName(consolOrder.getOrigName());
				order.setObDate(consolOrder.getObDate());
				order.setObTime(consolOrder.getObTime());
				order.setDestCode(consolOrder.getDestCode());
				order.setDestName(consolOrder.getDestName());
				order.setArDate(consolOrder.getArDate());
				order.setArTime(consolOrder.getArTime());
				order.setOrigCountry(consolOrder.getOrigCountry());
				order.setDestCountry(consolOrder.getDestCountry());
				order.setCustomsCode(consolOrder.getCustomsCode());
				order.setCustomsDptCode(consolOrder.getCustomsDptCode());
				order.setDiscWhCode(consolOrder.getDiscWhCode());
				order.setAssignmentWhCode(consolOrder.getAssignmentWhCode());
				order.setBound(consolOrder.getBound());
				order.setBoundType(consolOrder.getBoundType());
				order.setDestPort(consolOrder.getDestPort());
				order.setMrn(consolOrder.getMrn());
				order.setMsn(consolOrder.getMsn());
				order.setVenderId1(consolOrder.getVenderId1());
				order.setUpdateUserId(consolOrder.getUpdateUserId());
				order.setGmtTime(consolOrder.getGmtTime());
				order.setEdiMailboxSeqNo(consolOrder.getEdiMailboxSeqNo());
				order.setVenderId1(consolOrder.getVenderId1());
				/** 2025-06-20 반출사유 하우스별로 관리 */
//				order.setCarryoutCode(consolOrder.getCarryoutCode());
				
				/** 항공수출일경우 DEST_CODE값에 따라 하우스의 송하인/수하인 팩스번호 변경 */
				if(masterOdJobNoList == null && "AIR".equals(order.getBound()) && "EXP".equals(order.getBoundType()) && (action.equals("insert") || !Util.null2str(oldConsolOrder.getDestCode()).equals(order.getDestCode()))){
					if(!Util.isEmpty(order.getDestCode())){
						tmpMap.clear();
						tmpMap.put("groupCode", "CITY_PORT");
						tmpMap.put("entCode", order.getEntCode());
						tmpMap.put("srchCode", order.getDestCode());
						tmpMap = commonMapper.getCodeDetail(tmpMap);
						if(tmpMap != null && tmpMap.size() > 0){
							if("CN".equals(tmpMap.get("ETC2"))){
								order.setShipperFax("VAT NUMBER");
								order.setConsigneeFax("USCI");
							}else if("ID".equals(tmpMap.get("ETC2"))){
								order.setConsigneeFax("TIN");
							}else if("210".equals(tmpMap.get("ETC3"))){
								order.setConsigneeFax("T");
							}
						}
					}
				}else{
					order.setShipperFax("noUpdate");
				}
				
				
				List<Order> updateOrderList = new ArrayList<Order>();
				Order tmpUpdateOrder = null;
				
				for(Map<String, Object> consol : consolTarget){
					order.setOdJobNo(Util.null2str(consol.get("OD_JOB_NO")));
					order.setCurrencyCode(Util.null2str(consol.get("CURRENCY_CODE")));
					order.setAmountCur(Util.null2dbl(consol.get("AMOUNT_CUR")));
					order.setCdType(Util.null2str(consol.get("CD_TYPE")));
					order.setShipperCountryCode(Util.null2str(consol.get("SHIPPER_COUNTRY_CODE")));
					order.setConsigneeCountryCode(Util.null2str(consol.get("CONSIGNEE_COUNTRY_CODE")));
					order.setDestCountryH(Util.null2str(consol.get("DEST_COUNTRY_H")));
					orderSaveMap.clear();
					/** 공통 - 환율처리 */
					orderSaveMap.put("exChangeYn", "Y");
					orderSaveMap.put("exrateMap", exchangeRateMap);
					
					if(order.getBoundType().equals("EXP") && updateSimpleDocNo){
						if("E".equals(order.getCdType()) || "Y".equals(order.getCdType())){
							if("Y".equals(consol.get("DOCUMENT_YN")) && !Util.isEmpty(ediExpressSignDocumentY)){
								orderSaveMap.put("createSimpleDocNo", "Y");
								orderSaveMap.put("ediExpressSign", ediExpressSignDocumentY);
							}else if("N".equals(consol.get("DOCUMENT_YN")) && !Util.isEmpty(ediExpressSignDocumentN)){
								orderSaveMap.put("createSimpleDocNo", "Y");
								orderSaveMap.put("ediExpressSign", ediExpressSignDocumentN);
							}else{
								order.setSimpleDocNo("null");
							}
							orderCommonService.orderSave(order, "consol", orderSaveMap, seq);
						}else{
							orderCommonService.orderSave(order, "consol", orderSaveMap, seq);
							order.setSimpleDocNo("null");
						}
						tmpUpdateOrder = order.clone();
						updateOrderList.add(tmpUpdateOrder);
					}else{
						orderCommonService.orderSave(order, "consol", orderSaveMap, seq);
						if(updateSimpleDocNo){
							if("Y".equals(consol.get("DOCUMENT_YN"))){
								order.setSimpleDocNo(simpleDocNoDocumentY);
							}else if(documentN){
								order.setSimpleDocNo(simpleDocNoDocumentN);
							}
						}
						
						if(Util.isEmpty(consol.get("ASSIGNMENT_WH_CODE"))){
							order.setAssignmentWhCode(consolOrder.getAssignmentWhCode());
						}else{
							order.setAssignmentWhCode(null);
						}
						order.setProfileName(profile);
						consolBatchMapper.updateConsolOrderBatch(order);
						if( (cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
					
					bulkData = new BulkData();
					bulkData.setActionType(BulkData.Type.CREATE);
					bulkData.setIndexName(index);
					bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
					
					logMap = new HashMap<String, Object>();
					logMap.putAll(tmpLogMap);
					logMap.put("bound", Util.null2str(consol.get("BOUND")));
					logMap.put("bound_type", Util.null2str(consol.get("BOUND_TYPE")));
					logMap.put("reference_no_id", bulkData.getId());
					logMap.put("job_no", Util.null2str(consol.get("OD_JOB_NO")));
					logMap.put("job_log","{\"order\":" +new Gson().toJson(order)+", \"consolOrder\":" +new Gson().toJson(consolOrder)+"}");
					logMap.put("description", "Consol( "+ description +" ) [MBL_NO : " + consolOrder.getMblNo() + " HBL_NO : "+ consol.get("HBL_NO") +"]");
					bulkData.setJsonMap(logMap);
					bulkList.add(bulkData);
				}
				if(order.getBoundType().equals("EXP") && updateSimpleDocNo){
					cnt = 1;
					for(Order updateOrder : updateOrderList){
						updateOrder.setProfileName(profile);
						consolBatchMapper.updateConsolOrderBatch(updateOrder);
						if( (cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
				}
				
				sqlSession.flushStatements();
				sqlSession.clearCache();
				sqlSession.commit();
			}
		
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
		
		// 처리 로그 기록
		bulkData = new BulkData();
		bulkData.setActionType(BulkData.Type.CREATE);
		bulkData.setIndexName(index);
		bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
		
		logMap = new HashMap<String, Object>();
		logMap.putAll(tmpLogMap);
		logMap.put("bound", consolOrder.getBound());
		logMap.put("bound_type", consolOrder.getBoundType());
		logMap.put("reference_no_id", bulkData.getId());
		logMap.put("job_no", consolOrder.getOdJobNo());
		logMap.put("job_log", masterJoblog);
		logMap.put("job_func_type", masterJobFunc);
		if(description.equals("master consol")){
			logMap.put("description", description + "=> masterOdJobNo : " + masterOdJobNoList.toString());
		}else{
			logMap.put("description", description + "=> MBLNO(" + consolOrder.getMblNo() + ")");
		}
		bulkData.setJsonMap(logMap);
		bulkList.add(bulkData);
		
		commonService.insertLogBulk(bulkList);
		
		return orderSaveResult;
	}
	
	/**
	 * Consolidation - 엑셀업로드
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult excelUpload(MultipartFile consolHouseExcel, String entCode) throws Exception{
		Map<String, Object> resultData = new HashMap<String, Object>();
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		// 파일 확장자 유효성 검사
		if(!fileService.checkExtension(consolHouseExcel, new String[]{"xlsx", "xls"})) {
			orderSaveResult.setResultCode("extError");
			return orderSaveResult;
		}
		
		FileUploadResponse result = fileService.storeFile(consolHouseExcel, entCode, true);
		if(Util.null2str(result.getResult()).equals("ok")){
			File file = fileService.loadFileAsResource(result.getFileName(), entCode, true).getFile();
			String extension = null;
			if(file.getName().toLowerCase().indexOf(".xlsx") > -1){
				extension = "xlsx";
			}else{
				extension = "xls";
			}
			
			String hblNo;
			List<String> dataList = new ArrayList<String>();

			InputStream is = null;
			try {
				is = new FileInputStream(file);
				Workbook wb = WorkbookFactory.create(is);
				Sheet sheet = wb.getSheetAt(0);
				DataFormatter df = new DataFormatter();
				
				if(extension.equals("xlsx")){
					XSSFCell myCell = null;
					for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
						if(sheet.getRow(i) == null){
							orderSaveResult.setResultCode("A11216");
							resultData.put("nullLine", i + 1);
							resultData.put("lastLine", sheet.getLastRowNum() + 1);
							orderSaveResult.setResultData(resultData);
							return orderSaveResult;
						}
						myCell = (XSSFCell)sheet.getRow(i).getCell(0, MissingCellPolicy.CREATE_NULL_AS_BLANK);
						hblNo = Util.null2str(df.formatCellValue(myCell));
						if(!hblNo.equals("")){
							dataList.add(hblNo);
						}
					}
				}else{
					HSSFCell myCell = null;
					for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
						if(sheet.getRow(i) == null){
							orderSaveResult.setResultCode("A11216");
							resultData.put("nullLine", i + 1);
							resultData.put("lastLine", sheet.getLastRowNum() + 1);
							orderSaveResult.setResultData(resultData);
							return orderSaveResult;
						}
						myCell = (HSSFCell)sheet.getRow(i).getCell(0, MissingCellPolicy.CREATE_NULL_AS_BLANK);
						hblNo = Util.null2str(df.formatCellValue(myCell));
						if(!hblNo.equals("")){
							dataList.add(hblNo);
						}
					}
				}
				//TODO 파일삭제?
				
				resultData.put("dataList", dataList);
				resultData.put("entCode", entCode);
				//hbl 유효성 체크
				//저장시 체크함
	//			Map<String, Object> rstHbl = consolMapper.selectHblValidate(resultData);
				
				resultData.put("dataSize", dataList.size());
				
	//			if(rstHbl!=null) {
	//				//유효하지않은 BL
	//				resultData.put("NOT_BL_CNT", rstHbl.get("NOT_BL_CNT"));
	//				resultData.put("NOT_HBL_LIST", rstHbl.get("NOT_HBL_LIST"));
	//			}
	//			else {
					resultData.put("NOT_BL_CNT", "0");
					resultData.put("NOT_HBL_LIST", "");
	//			}
				
				orderSaveResult.setResultData(resultData);
				orderSaveResult.setResultCode("200");
				
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
		return orderSaveResult;
	}
	
	/**
	 * Consolidation - Deconsol
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult deconsol(ConsolRelease consolRelease, HttpServletRequest request) throws Exception{
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		// 단건 >>  현재 단건처리 기능 없음 
		if(!Util.null2str(consolRelease.getOdJobNo()).equals("")) {
			this.closeChk(orderSaveResult, consolRelease.getOdJobNo());
			
		}
		//복수건
		else {
			/** 마감체크 > 같은 마스터안에서 deconsol 하기에 첫번째 하우스만 검증한다*/
			this.closeChk(orderSaveResult, Util.null2str(consolRelease.getOdJobNoList().get(0).get("odJobNo")));
		}
		
		if(!Util.null2str(orderSaveResult.getResultCode()).equals("")){
			return orderSaveResult;
		}
		
		// 도입사 직원이 아닌 상위업체lv1 직원인 경우 
		if(!"".equals(Util.null2str(consolRelease.getEcmCustomerCode()))) {
			ConsolOrder consol = new ConsolOrder();
			consol.setOdJobNo(consolRelease.getMasterOdJobNo());
			consol = consolMapper.selectConsolMasterInfo(consol);
			if(consol.getCustCodeLv1().equals(consolRelease.getEntCode())) {
				orderSaveResult.setResultCode("A11621");
				return orderSaveResult;
			}
		}
		
		Map<String, Object> map	= new HashMap<String, Object>();
		map.put("odJobNoList", consolRelease.getOdJobNoList());
		List<Map<String, Object>> list = consolMapper.selectOrderListModNo(map);
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(consolRelease, "gmtTime", false, null, null, null, null);
		consolMapper.updateDeConsol(consolRelease);
		
		//마스터에 들어갈 상위업체 체크 
		//여러개의 상위업체가 섞인 하우스를 마스터와 콘솔하게 되면 마스터에는 상위업체값은 도입사를 넣게된다.
		Map<String,Object> consolMap = new HashMap<>();
		consolMap.put("odJobNo", consolRelease.getMasterOdJobNo());
		List<String> custList = consolMapper.selectCustCodeList(consolMap);
		if(custList != null && custList.size() > 0) {
			ConsolOrder consol = new ConsolOrder();
			if(custList.size() == 1) {
				// 멀티 콘솔 이였던 마스터가 멀티가 아닌 단일 콘솔이 되어버린 경우에는 마스터 상위업체를 하우스 상위업체와 똑같이 맞춰준다.
				consol.setCustCodeLv1(custList.get(0).toString());
			}else {
				// 멀티 콘솔 이였던 마스터에서 특정 상위업체가 빠져 버리면 remark 에도 빠진 상위업체를 없애야한다.
				String custLv1Remark = String.join(",", custList);
				consol.setCustLv1Remark(custLv1Remark);
				consol.setCustCodeLv1(consolRelease.getEntCode());
			}
			consol.setOdJobNo(consolRelease.getMasterOdJobNo());
			consolMapper.updateMasterCustLvInfo(consol);
		}
		
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		
		tmpLogMap.put("ent_code", consolRelease.getEntCode());
		tmpLogMap.put("ent_business_code", consolRelease.getUserBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("process_user_id", consolRelease.getUpdateUserId());
		tmpLogMap.put("log_job_type", "DECONSOL");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		tmpLogMap.put("bound", consolRelease.getBound());
		tmpLogMap.put("bound_type", consolRelease.getBoundType());
		
		
		tmpLogMap.put("ent_business_print_name", consolRelease.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", consolRelease.getLogUserName());
		tmpLogMap.put("process_user_name_eng", consolRelease.getLogUserNameEng());
		
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		int seq = 1;
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			ConsolMapper consolBatchMapper = sqlSession.getMapper(ConsolMapper.class);
			int cnt = 1;
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			Map<String, Object> exrateData;
			Map<String, Map<String, Object>> exrateMap = new HashMap<String, Map<String, Object>>();
			tmpMap.put("srchEntCode", consolRelease.getEntCode());
			tmpMap.put("entBusinessCode", consolRelease.getHeadBusinessCode());
			tmpMap.put("srchDateFormat", "23");
			if("EXP".equals(consolRelease.getBoundType())){
				tmpMap.put("exrateType", "EXRATE_SELLING_CUSTOMS");
			}else{
				tmpMap.put("exrateType", "EXRATE_BUYING_CUSTOMS");
			}
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(tmpMap, "exrateType|srchDateFormat", false, null, null, null, null);
			
			double exrateUsd = 0, exrate = 0, amountCur = 0, amountLoc = 0;
			for(Map<String, Object> data : list) {
				bulkData = new BulkData();
				bulkData.setActionType(BulkData.Type.CREATE);
				bulkData.setIndexName(index);
				bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				
				logMap = new HashMap<String, Object>();
				logMap.putAll(tmpLogMap);
				logMap.put("reference_no_id", bulkData.getId());
				logMap.put("job_no", Util.null2str(data.get("OD_JOB_NO")));
				logMap.put("description","house deconsole [MBL_NO : " + Util.null2str(data.get("MBL_NO")) + ", HBL_NO : "+ Util.null2str(data.get("HBL_NO")) +"]");
				logMap.put("job_log", "");
				bulkData.setJsonMap(logMap);
				bulkList.add(bulkData);
				
				
				/** 환율처리 */
				if(!Util.isEmpty(data.get("CURRENCY_CODE")) && Util.null2dbl(data.get("AMOUNT_CUR")) != 0 && !Util.null2str(data.get("PO_DATE")).equals(Util.null2str(data.get("EXRATE_DATE")))){
					exrateData = exrateMap.get(Util.null2str(data.get("PO_DATE")));
					if(exrateData == null){
						tmpMap.put("srchDate", Util.null2str(data.get("PO_DATE")));
						exrateData = commonMapper.selectExchangeRate(tmpMap);
						exrateMap.put(Util.null2str(data.get("PO_DATE")), exrateData);
					}
					if(Util.null2int(exrateData.get("CNT")) != 0){
						exrateUsd = Util.null2dbl(exrateData.get("EXCHANGE_RATE_USD"));
						exrate = Util.null2dbl(exrateData.get("EXCHANGE_RATE_" + Util.null2str(data.get("CURRENCY_CODE"))));
						amountCur = Util.null2dbl(data.get("AMOUNT_CUR"));
						amountLoc = amountCur * exrate;
						data.put("EXCHANGE_RATE", exrate);
						data.put("AMOUNT_LOC", amountLoc);
						
						
						if("USD".equals(data.get("CURRENCY_CODE"))){
							data.put("AMOUNT_USD", amountCur);
						}else if(exrateUsd!= 0){
							data.put("AMOUNT_USD", Util.null2dbl(amountLoc / exrateUsd, 2, "U"));
						}else{
							data.put("AMOUNT_USD", 0);
						}
					}else{
						data.put("EXCHANGE_RATE", 0);
						data.put("AMOUNT_LOC", 0);
						data.put("AMOUNT_USD", 0);
					}
					
					consolBatchMapper.updateDeconsolExrate(data);
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
		
		commonService.insertLogBulk(bulkList);
	
		return orderSaveResult;
	}
	
	/**
	 * Consolidation - close
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult close(ConsolClose consolClose, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		
		ConsolOrder consolOrder = null;
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		Map<String, Object> simpleDocNoMap = new HashMap<String, Object>();
		List<Map<String, Object>> updateSimpleDocNoList = new ArrayList<Map<String, Object>>();
		Map<String, Object> updateSimpleDocNoMap;
		String ecmYnChk = "";
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(consolClose, "gmtTime", false, null, null, null, null);
		
		if(Util.null2str(consolClose.getFinishYn()).equals("Y")){
			
			tmpMap.put("srchEntCode", consolClose.getEntCode());
			tmpMap.put("srchEntBusinessCode", consolClose.getHeadBusinessCode());
			tmpMap.put("srchBound", consolClose.getBound());
			
			simpleDocNoMap.put("entCode", consolClose.getEntCode());
			simpleDocNoMap.put("gmtTime", consolClose.getGmtTime());
			simpleDocNoMap.put("entBusinessCode", "");
			simpleDocNoMap.put("updateUserId", consolClose.getUpdateUserId());
			simpleDocNoMap.put("solution", "ECM");
			simpleDocNoMap.put("stockType", "OD");
			simpleDocNoMap.put("stockSeqLength", "8");
			simpleDocNoMap.put("signType", "noHyphen");
			simpleDocNoMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			simpleDocNoMap.put("stockRemark", "ORDER SIMPLE DOC NO");
			simpleDocNoMap.put("dateLength", "2");
			simpleDocNoMap.put("stockDateFormat", "12");
			
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(tmpMap, "stockDateFormat", false, null, null, null, null);
			
			/** 통관목록제출번호   수입은 마스터, 수출은 하우스별로 제출번호 생성 */
			consolOrder = new ConsolOrder();
			consolOrder.setAction("saveManifest");	/** DOCUMENT_YN조회하기위해 추가 */
			for(String masterOdJobNo : consolClose.getMasterOdJobNoList()){
				updateSimpleDocNoMap = new HashMap<String, Object>();
				consolOrder.setOdJobNo(masterOdJobNo);
				consolOrder = consolMapper.selectConsolMasterInfo(consolOrder);
				if(Util.null2str(consolOrder.getFinishYn()).equals("Y")){
					orderSaveResult.setResultCode("A00112");	/** 마감이 완료된 자료 입니다. */
					return orderSaveResult;
				}
				
				
				// 수입일경우 하우스에 화물구분값이 있는지 체크 없는 하우스가 있다면 return 시킨다.
				if(consolClose.getBoundType().equals("IMP")){
					List<ConsolOrder> iertList = consolMapper.selectConsolBltypeiertCheck(consolOrder);
					if(iertList != null && iertList.size() > 0) {
						Map<String,Object> returnMap = new HashMap<String, Object>();
						String iertHblNo = "";
						
						for(int i=0; i<iertList.size(); i++) {
							iertHblNo += iertList.get(i).getHblNo() + "</br>";
						}
						
						returnMap.put("returnMessage", iertHblNo);
						orderSaveResult.setResultData(returnMap);
						orderSaveResult.setResultCode("blTypeIertNull");
						return orderSaveResult;
					}
					
					// 하우스들중에 전자상거래 여부가 Y,N 둘다 있는지 하나만 있는지 체크한다.
					// 둘다 있으면 Y, 둘중 하나만 있으면 N
					ecmYnChk = consolMapper.selectEcmYnCheck(consolOrder);
				}
				
				if(consolClose.getBoundType().equals("IMP")){
					tmpMap.put("srchEdiCityPort", consolOrder.getDestPort());
				}else {
					tmpMap.put("srchEdiCityPort", consolOrder.getOrigCode());
				}
				/** 제출번호에 사용되는 특송부호 조회 */
				Map<String, Object> busEdiEcmMap = commonMapper.selectBusEdiCodeInfo(tmpMap);
				if(busEdiEcmMap != null && busEdiEcmMap.size() > 0){
					updateSimpleDocNoMap.put("ediExpressSign", Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN")));
					if("YN".equals(consolOrder.getDocumentYn())){
						updateSimpleDocNoMap.put("ediExpressSignDocumentY", Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y")));
						updateSimpleDocNoMap.put("ediExpressSignDocumentN", Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N")));
					}else if("Y".equals(consolOrder.getDocumentYn())){
						updateSimpleDocNoMap.put("ediExpressSignDocumentY", Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y")));
					}else{
						updateSimpleDocNoMap.put("ediExpressSignDocumentN", Util.null2str(busEdiEcmMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N")));
					}
				}
				updateSimpleDocNoMap.put("masterOdJobNo", masterOdJobNo);
				updateSimpleDocNoList.add(updateSimpleDocNoMap);
			}
			
			if(consolClose.getBoundType().equals("IMP")){
				for(Map<String, Object> dataMap : updateSimpleDocNoList){
					if(!Util.null2str(dataMap.get("ediExpressSignDocumentY")).equals("")){
						simpleDocNoMap.put("stockSign", dataMap.get("ediExpressSignDocumentY"));
						dataMap.put("simpleDocNo", commonMapper.selectNextStockMaxNo(simpleDocNoMap));
						dataMap.put("documentYn", "Y");
						if(Util.isEmpty(dataMap.get("ediExpressSignDocumentN"))){
							dataMap.put("masterUpdateYn", "Y");
						}
						consolMapper.updateSimpleDocNoImp(dataMap);
						

						if("Y".equals(ecmYnChk)) {
							dataMap.put("simpleDocNo", commonMapper.selectNextStockMaxNo(simpleDocNoMap));
							dataMap.put("ecmYnChk",ecmYnChk);
							consolMapper.updateSimpleDocNoImp(dataMap);
						}
					}
					if(!Util.null2str(dataMap.get("ediExpressSignDocumentN")).equals("")){
						simpleDocNoMap.put("stockSign", dataMap.get("ediExpressSignDocumentN"));
						dataMap.put("simpleDocNo", commonMapper.selectNextStockMaxNo(simpleDocNoMap));
						dataMap.put("documentYn", "N");
						dataMap.put("masterUpdateYn", "Y");
						consolMapper.updateSimpleDocNoImp(dataMap);
						
						if("Y".equals(ecmYnChk)) {
							dataMap.put("simpleDocNo", commonMapper.selectNextStockMaxNo(simpleDocNoMap));
							dataMap.put("ecmYnChk",ecmYnChk);
							consolMapper.updateSimpleDocNoImp(dataMap);
						}
					}
				}
			}
		}else{
			for(String masterOdJobNo : consolClose.getMasterOdJobNoList()){
				consolOrder = new ConsolOrder();
				consolOrder.setOdJobNo(masterOdJobNo);
				consolOrder = consolMapper.selectConsolMasterInfo(consolOrder);
				if("Y".equals(consolOrder.getSendMfYn()) && !"".equals(consolClose.getEcmCustomerCode())){
					orderSaveResult.setResultCode("A11332");	/** 적하목록 전송이 되었습니다. Agnet 문의 바랍니다. */
					return orderSaveResult;
				}
				if(Util.null2int(consolOrder.getSimpleEdiSendCnt()) > 0){
					orderSaveResult.setResultCode("A11333");	/** 통관목록이 전송되어서 처리할 수 없습니다. */
					return orderSaveResult;
				}
			}
		}
		
		
		if(Util.null2str(consolClose.getFinishYn()).equals("Y") && consolClose.getBoundType().equals("IMP")){
			tmpMap.clear();
			tmpMap.put("entCode", consolOrder.getEntCode());
			tmpMap.put("entBusinessCode", consolClose.getHeadBusinessCode());
			tmpMap.put("solution", "ECM");
			tmpMap.put("optionCode", "C09");
			if("Y".equals(commonMapper.selectSystemOption(tmpMap))){
				consolClose.setAction("updateSimpleDocDate");
			}
		}
		consolMapper.updateClose(consolClose);
		
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		
		tmpLogMap.put("ent_code", consolClose.getEntCode());
		tmpLogMap.put("ent_business_code", consolClose.getUserBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("process_user_id", consolClose.getUpdateUserId());
		tmpLogMap.put("log_job_type", "FINISH");
		tmpLogMap.put("job_func_type", "UPDATE");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		tmpLogMap.put("ent_business_print_name", consolClose.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", consolClose.getLogUserName());
		tmpLogMap.put("process_user_name_eng", consolClose.getLogUserNameEng());
		
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		int cnt = 1;
		if(Util.null2str(consolClose.getFinishYn()).equals("Y")){
			String masterOdJobNoStr = "";
			List<String> masterOdJobNoList = consolClose.getMasterOdJobNoList();
			
			SqlSession sqlSession = null;
			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
				ConsolMapper consolBatchMapper = sqlSession.getMapper(ConsolMapper.class);
				
				for(String masterOdJobNo : masterOdJobNoList){
					if(Util.null2int(consolMapper.selectHsnChk(masterOdJobNo)) > 0){
						List<Map<String, Object>> updateHsnList = consolMapper.selectUpdateHsnList(masterOdJobNo);
						for(Map<String, Object> updateHsn : updateHsnList){
							consolBatchMapper.updateHsnMax(updateHsn);
							if( (cnt++ % 1000) == 0){
								sqlSession.flushStatements();
								sqlSession.clearCache();
							}
						}
					}else{
						consolMapper.updateHsnOrderClose(masterOdJobNo);
					}
					masterOdJobNoStr += ", " + masterOdJobNo;
				}
				if(masterOdJobNoStr.length() > 2){
					masterOdJobNoStr = masterOdJobNoStr.substring(2);
				}
				cnt = 1;
				
				Map<String, Object> podMap = new HashMap<String, Object>();
				
				/* 상태명 고정으로 해서 주석처리 2020/04/21.hyuk
				podMap.put("entCode", consolClose.getEntCode());
				podMap.put("userLanguage", consolClose.getUserLanguage());
				podMap.put("srchGroupCode", "STATUS_ORDER_CARGO");
				podMap.put("srchCode", "ECM055");
				podMap = commonMapper.selectCodeName(podMap);
				*/
				podMap.put("entCode", 		consolClose.getEntCode());
				podMap.put("entBusinessCode", "");
				podMap.put("gmtTime", 		consolClose.getGmtTime());
				podMap.put("solution", 		"ECM");
				podMap.put("updateUserId", 	consolClose.getUpdateUserId());
				podMap.put("stockType", 	"POD");
				podMap.put("stockSign", 	"POD");
				podMap.put("stockSeqLength",8);
				podMap.put("resultCode", 	"FIXED_SIGN_AND_SEQ_NO");
				podMap.put("stockRemark", 	"ECM POD JOB NO");
				podMap.put("podStatus", 	"ECM055");
				podMap.put("podChkeckYn", 	"N");
				podMap.put("podTableName", 	"TB_ECM_POD_01");
				
				/** 취약성점검 SQL Injection 방지 */
				Util.accessControlDatabase(podMap, "podTableName|gmtTime", false, null, null, null, null);
				
				List<Map<String, Object>> updateLoadingLocationList = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> insertPodList = new ArrayList<Map<String, Object>>();
				Map<String, Object> tmpInsertPodMap = null;
				int seq = 1;
				for(Map<String, Object> dataMap : updateSimpleDocNoList){
					dataMap.put("srchType", "close");
					List<Map<String, Object>> closeOrderList = consolMapper.selectCloseOrderList(dataMap);
					for(Map<String, Object> closeOrder : closeOrderList){
						tmpInsertPodMap = new HashMap<String, Object>();
						tmpInsertPodMap.put("odJobNo", closeOrder.get("OD_JOB_NO"));
						tmpInsertPodMap.put("blNo", closeOrder.get("BL_NO"));
						tmpInsertPodMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
						insertPodList.add(tmpInsertPodMap);
						
						if(consolClose.getBoundType().equals("EXP") && "H".equals(closeOrder.get("BL_TYPE_MH"))){
							if(!Util.null2str(dataMap.get("ediExpressSignDocument" + Util.null2str(closeOrder.get("DOCUMENT_YN"), "N"))).equals("")){
								simpleDocNoMap.put("odJobNo", closeOrder.get("OD_JOB_NO"));
								simpleDocNoMap.put("stockSign", dataMap.get("ediExpressSignDocument" + Util.null2str(closeOrder.get("DOCUMENT_YN"), "N")));
								simpleDocNoMap.put("seqNo", seq);
								if(Util.null2str(closeOrder.get("CD_TYPE")).equals("Y")){
									simpleDocNoMap.put("dateLength", "4");
									simpleDocNoMap.put("stockDateFormat", "112");
								}else if(Util.null2str(closeOrder.get("CD_TYPE")).equals("E")){
									simpleDocNoMap.put("dateLength", "2");
									simpleDocNoMap.put("stockDateFormat", "12");
								}
								consolBatchMapper.updateSimpleDocNoExp(simpleDocNoMap);
							}
						}
						
						if("Y".equals(closeOrder.get("UPDATE_ORIG_CODE_YN")) || "Y".equals(closeOrder.get("UPDATE_CUSTOMS_CODE_YN"))){
							updateLoadingLocationList.add(closeOrder);
						}
						
//						bulkData = new BulkData();
//						bulkData.setActionType(BulkData.Type.CREATE);
//						bulkData.setIndexName(index);
//						bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
//						
//						logMap = new HashMap<String, Object>();
//						logMap.putAll(tmpLogMap);
//						logMap.put("bound", Util.null2str(closeOrder.get("BOUND")));
//						logMap.put("bound_type", Util.null2str(closeOrder.get("BOUND_TYPE")));
//						logMap.put("reference_no_id", bulkData.getId());
//						logMap.put("job_no", Util.null2str(closeOrder.get("OD_JOB_NO")));
//						logMap.put("job_log","{\"consolClose\":" +new Gson().toJson(consolClose)+", \"simpleDocNo\":" +new Gson().toJson(dataMap)+"}");
//						if("M".equals(Util.null2str(closeOrder.get("BL_TYPE_MH"))))
//							logMap.put("description", "FINISH [MBL_NO: "+ Util.null2str(closeOrder.get("MBL_NO")) +"]");
//						else
//							logMap.put("description", "FINISH [MBL_NO: "+ Util.null2str(closeOrder.get("MBL_NO")) + ", HBL_NO: "+ Util.null2str(closeOrder.get("BL_NO"))+"]");
//						bulkData.setJsonMap(logMap);
//						bulkList.add(bulkData);
						
						if( (cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
				}
				
				cnt = 1;
				for(Map<String, Object> updateLoadingLocationMap : updateLoadingLocationList){
					consolBatchMapper.updateLoadingLocation(updateLoadingLocationMap);
					if( (cnt++ % 1000) == 0){
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
				}
				cnt = 1;
				for(Map<String, Object> insertPodMap : insertPodList){
					podMap.put("odJobNo", insertPodMap.get("odJobNo"));
					//last pod update 추가..2022/04/20.hyuk
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
					orderBatchMapper.insertOrderPod(podMap);
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
			
			// 처리 로그 기록
			ProcessLogs processLogs = new ProcessLogs();
			consolClose.setMasterOdJobNoList(null);
			processLogs.setEntCode(consolClose.getEntCode());
			processLogs.setEntBusinessCode(consolClose.getUserBusinessCode());
			processLogs.setSolution("ECM");
			processLogs.setBound(consolClose.getBound());
			processLogs.setBoundType(consolClose.getBoundType());
			processLogs.setProcessUserId(consolClose.getUpdateUserId());
			processLogs.setJobNo(masterOdJobNoStr);
			processLogs.setLogJobType("DATA");
			processLogs.setDescription("Close Order[MASTER_OD_JOB_NO : " + masterOdJobNoStr +"]");
//			processLogs.setJobLog(new Gson().toJson(consolClose));
			processLogs.setJobLog("");
			processLogs.setResult("ok");
			processLogs.setEntBusinessPrintName(consolClose.getLogUserBusinessPrintName());
			processLogs.setProcessUserName(consolClose.getLogUserName());
			processLogs.setProcessUserNameEng(consolClose.getLogUserNameEng());
			processLogs.setJobFuncType("UPDATE");
			commonService.insertLog(processLogs, request);
		}else{
			consolMapper.updateHsnNull(consolClose);
			consolClose.setPodStatus("ECM055");
			consolMapper.deletePod(consolClose);
			HashMap<String,Object> map = new HashMap<String,Object>();
			SqlSession sqlSession = null;
			try {
				sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
				ConsolMapper consolBatchMapper = sqlSession.getMapper(ConsolMapper.class);
				
				cnt = 1;
				int seq = 1;
				for(String masterOdJobNo : consolClose.getMasterOdJobNoList()){
					map.put("masterOdJobNo",masterOdJobNo);
					List<Map<String, Object>> odList = consolMapper.selectCloseOrderList(map);
					for(Map<String, Object> od : odList){
						od.put("odJobNo", od.get("OD_JOB_NO"));
						consolBatchMapper.updateLastPodStatus(od);
						
						if( (cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
				}
				sqlSession.flushStatements();
				sqlSession.clearCache();
				sqlSession.commit();
				
				for(String masterOdJobNo : consolClose.getMasterOdJobNoList()){
					map.put("masterOdJobNo",masterOdJobNo);
					List<Map<String, Object>> odList = consolMapper.selectCloseOrderList(map);
					for(Map<String, Object> od : odList){
						
						bulkData = new BulkData();
						bulkData.setActionType(BulkData.Type.CREATE);
						bulkData.setIndexName(index);
						bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
						
						logMap = new HashMap<String, Object>();
						logMap.putAll(tmpLogMap);
						consolClose.setMasterOdJobNoList(null);
						logMap.put("bound", Util.null2str(od.get("BOUND")));
						logMap.put("bound_type", Util.null2str(od.get("BOUND_TYPE")));
						logMap.put("reference_no_id", bulkData.getId());
						logMap.put("job_no", Util.null2str(od.get("OD_JOB_NO")));
						logMap.put("job_log",new Gson().toJson(consolClose));
						if("M".equals(Util.null2str(od.get("BL_TYPE_MH"))))
							logMap.put("description", "Cancel FINISH [MBL_NO: "+ Util.null2str(od.get("MBL_NO")) +"]");
						else
							logMap.put("description", "Cancel FINISH [MBL_NO: "+ Util.null2str(od.get("MBL_NO")) + ", HBL_NO: "+ Util.null2str(od.get("BL_NO"))+"]");
						bulkData.setJsonMap(logMap);
						bulkList.add(bulkData);
						
					}
				}
			}catch(Exception e) {
				throw e;
			}finally{
				sqlSession.close();
			}
			commonService.insertLogBulk(bulkList);
		}
		
		return orderSaveResult;
	}
	
	/**
	 * Consolidation - delete
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult delete(ConsolClose consolClose, HttpServletRequest request) throws Exception{
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		Map<String, Object> map	= new HashMap<String, Object>();
		
		map.put("masterOdJobNoList", consolClose.getMasterOdJobNoList());
		/** 마감체크 */
		this.closeChkList(orderSaveResult, consolClose.getMasterOdJobNoList(), "odJobNoList");
		if(!Util.null2str(orderSaveResult.getResultCode()).equals("")){
			return orderSaveResult;
		}
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(consolClose, "gmtTime", false, null, null, null, null);
		
		List<Map<String, Object>> list = consolMapper.selectOrderListModNo(map);
		
		consolMapper.deleteMasterOrder(consolClose);
		consolMapper.deleteMasterOrderAdditional(consolClose);
		consolMapper.updateDeconsolAll(consolClose);
		
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		tmpLogMap.put("ent_code", consolClose.getEntCode());
		tmpLogMap.put("ent_business_code", consolClose.getUserBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("bound", consolClose.getBound());
		tmpLogMap.put("bound_type", consolClose.getBoundType());
		tmpLogMap.put("process_user_id", consolClose.getUpdateUserId());
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		tmpLogMap.put("ent_business_print_name", consolClose.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", consolClose.getLogUserName());
		tmpLogMap.put("process_user_name_eng", consolClose.getLogUserNameEng());
		
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		int seq = 1;
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			ConsolMapper consolBatchMapper = sqlSession.getMapper(ConsolMapper.class);
			int cnt = 1;
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			Map<String, Object> exrateData;
			Map<String, Map<String, Object>> exrateMap = new HashMap<String, Map<String, Object>>();
			tmpMap.put("srchEntCode", consolClose.getEntCode());
			tmpMap.put("entBusinessCode", consolClose.getHeadBusinessCode());
			tmpMap.put("srchDateFormat", "23");
			if("EXP".equals(consolClose.getBoundType())){
				tmpMap.put("exrateType", "EXRATE_SELLING_CUSTOMS");
			}else{
				tmpMap.put("exrateType", "EXRATE_BUYING_CUSTOMS");
			}
			
			/** 취약성점검 SQL Injection 방지 */
			Util.accessControlDatabase(tmpMap, "exrateType|srchDateFormat", false, null, null, null, null);
			
			double exrateUsd = 0, exrate = 0, amountCur = 0, amountLoc = 0;
			for(Map<String, Object> data : list) {
				bulkData = new BulkData();
				bulkData.setActionType(BulkData.Type.CREATE);
				bulkData.setIndexName(index);
				bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				
				logMap = new HashMap<String, Object>();
				logMap.putAll(tmpLogMap);
				logMap.put("reference_no_id", bulkData.getId());
				logMap.put("job_no", Util.null2str(data.get("OD_JOB_NO")));
				logMap.put("job_log", "");
				
				if("M".equals(Util.null2str(data.get("BL_TYPE_MH")))) {
					logMap.put("job_func_type","DELETE");
					logMap.put("log_job_type", "DATA");
					logMap.put("description", "master delete [MBL_NO : "+ Util.null2str(data.get("MBL_NO"))+"]");
				}else {
					logMap.put("job_func_type","UPDATE");
					logMap.put("log_job_type", "DECONSOL");
					logMap.put("description", "master delete > house deconsol [MBL_NO : "+ Util.null2str(data.get("MBL_NO")) +", HBL_NO : "+ Util.null2str(data.get("HBL_NO")) +"]");
					
					/** 환율처리 */
					if(!Util.isEmpty(data.get("CURRENCY_CODE")) && Util.null2dbl(data.get("AMOUNT_CUR")) != 0 && !Util.null2str(data.get("PO_DATE")).equals(Util.null2str(data.get("EXRATE_DATE")))){
						exrateData = exrateMap.get(Util.null2str(data.get("PO_DATE")));
						if(exrateData == null){
							tmpMap.put("srchDate", Util.null2str(data.get("PO_DATE")));
							exrateData = commonMapper.selectExchangeRate(tmpMap);
							exrateMap.put(Util.null2str(data.get("PO_DATE")), exrateData);
						}
						if(Util.null2int(exrateData.get("CNT")) != 0){
							exrateUsd = Util.null2dbl(exrateData.get("EXCHANGE_RATE_USD"));
							exrate = Util.null2dbl(exrateData.get("EXCHANGE_RATE_" + Util.null2str(data.get("CURRENCY_CODE"))));
							amountCur = Util.null2dbl(data.get("AMOUNT_CUR"));
							amountLoc = amountCur * exrate;
							data.put("EXCHANGE_RATE", exrate);
							data.put("AMOUNT_LOC", amountLoc);
							
							
							if("USD".equals(data.get("CURRENCY_CODE"))){
								data.put("AMOUNT_USD", amountCur);
							}else if(exrateUsd!= 0){
								data.put("AMOUNT_USD", Util.null2dbl(amountLoc / exrateUsd, 2, "U"));
							}else{
								data.put("AMOUNT_USD", 0);
							}
						}else{
							data.put("EXCHANGE_RATE", 0);
							data.put("AMOUNT_LOC", 0);
							data.put("AMOUNT_USD", 0);
						}
						
						consolBatchMapper.updateDeconsolExrate(data);
						if( (cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
				}
				bulkData.setJsonMap(logMap);
				bulkList.add(bulkData);
				
				
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
		
		return orderSaveResult;
	}
	
	
	/**
	 * 마감체크
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public void closeChk(OrderSaveResult orderSaveResult, String odJobNo) throws Exception{
		Map<String, Object> chkMap = new HashMap<String, Object>();
		chkMap.put("odJobNo", odJobNo);
		chkMap = consolMapper.selectFinishYnChk(chkMap);
		if(chkMap != null && Util.null2str(chkMap.get("FINISH_YN")).equals("Y")){
			orderSaveResult.setResultCode("A11237");
		}
	}
	public void closeChkList(OrderSaveResult orderSaveResult, List<?> noList, String noType) throws Exception{
		Map<String, Object> tmpMap = null;
		Map<String, Object> chkMap = new HashMap<String, Object>();
		Map<String, Object> errMap = null;
		int listCnt = noList.size();
		if(listCnt > 500){
			StringBuffer hblNo = new StringBuffer();
			StringBuffer mblNo = new StringBuffer();
			for(int i = 0; i < listCnt; i+=500){
				if(i + 500 < listCnt) {
					chkMap.put(noType, noList.subList(i, i + 500));
				}else{
					chkMap.put(noType, noList.subList(i, listCnt));
				}
				tmpMap = consolMapper.selectFinishYnChkList(chkMap);
				if(tmpMap != null){
					if(tmpMap.get("mblNoStr") != null){
						mblNo.append(Util.null2str(tmpMap.get("mblNoStr")) + ", ");
					}
					if(tmpMap.get("hblNoStr") != null){
						hblNo.append(Util.null2str(tmpMap.get("hblNoStr")) + ", ");
					}
				}
			}
			if(mblNo.length() > 0){
				errMap = new HashMap<String, Object>();
				errMap.put("mblNoStr", mblNo.toString().substring(0, mblNo.length() - 2));
			}
			if(hblNo.length() > 0){
				if(errMap == null){
					errMap = new HashMap<String, Object>();
				}
				errMap.put("hblNoStr", hblNo.toString().substring(0, hblNo.length() - 2));
			}
		}else{
			chkMap.put(noType, noList);
			errMap = consolMapper.selectFinishYnChkList(chkMap);
		}
		
		if(errMap != null){
			orderSaveResult.setResultCode("A11237");
			orderSaveResult.setResultData(errMap);
		}
	}
	
	public String convertDate(String date, String dateFormat) throws Exception{
		SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdFormat2 = null;
		if(dateFormat.equals("23")){
			return date;
		}else if(dateFormat.equals("111")){
			sdFormat2 = new SimpleDateFormat("yyyyMMdd");
		}else if(dateFormat.equals("110") || dateFormat.equals("101")){
			sdFormat2 = new SimpleDateFormat("MMddyyyy");
		}else if(dateFormat.equals("105") || dateFormat.equals("103")){
			sdFormat2 = new SimpleDateFormat("ddMMyyyy");
		}
		date.replaceAll("[-./]", "").substring(0, 8);
		if(date.length() != 8){
			return date;
		}
		return sdFormat.format(sdFormat2.parse(date));
	}

	public ConsolHouseOrderList selectCargoManifest(SearchConsolOrderList search) {
		ConsolHouseOrderList consolHouseOrderList = new ConsolHouseOrderList();
		List<ConsolHouseOrder> list  = consolMapper.selectCargoManifest(search);
		consolHouseOrderList.setConsolHouseOrderList(list);
		if(list.size() > 0) {
			consolHouseOrderList.setTotal(list.get(0).getTotal());
		}else {
			consolHouseOrderList.setTotal(0);
		}
		return consolHouseOrderList;
	}
	
	/**
	 * Consolidation - 픽업완료처리
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult pickUpComplete(ConsolOrder consol, HttpServletRequest request) throws Exception{
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		SearchConsolOrderList search = new SearchConsolOrderList(); 
		int seq = 1;
		for(String odJobNo : consol.getOdJobNoList()){
			search.setSrchMasterOdJobNo(odJobNo);
			search.setSrchBoundType(Util.null2str(consol.getBoundType()));
			search.setDefaultSort(Util.null2str(consol.getDefaultSort()));
			List<ConsolHouseOrder> consolHouseOrderList = consolMapper.selectHouseOrderList(search);
			if(consolHouseOrderList != null) {
				for(ConsolHouseOrder consolHouseOrder : consolHouseOrderList) {
					Map<String, Object> podMap = new HashMap<>();
					podMap.put("entCode", Util.null2str(consol.getEntCode()));
					podMap.put("podJobNo", 	DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
					podMap.put("odJobNo", Util.null2str(consolHouseOrder.getOdJobNo()));
					podMap.put("blNo", Util.null2str(consolHouseOrder.getHblNo()));
					podMap.put("podStatus", "ECM030"); // 출발지반입
					podMap.put("updateUserId", Util.null2str(consol.getUpdateUserId()));
					podMap.put("gmtTime", Util.null2str(consol.getGmtTime()));
					consolMapper.insertPickUpComplete(podMap);
				}
			}else {
				orderSaveResult.setResultCode("A11316");
			}
		}
		return orderSaveResult;
	}
}
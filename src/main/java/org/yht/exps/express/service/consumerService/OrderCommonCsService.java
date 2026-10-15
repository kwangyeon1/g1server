package org.yht.exps.express.service.consumerService;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.consumerService.OrderCommonCsMapper;
import org.yht.exps.express.mapper.eCommerce.FunctionMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.edi.AbnormalMapper;
import org.yht.exps.express.mapper.edi.DeliveryChgMapper;
import org.yht.exps.express.mapper.sms.EnterpriseMapper;
import org.yht.exps.express.mapper.systemMng.SystemMngMapperVE;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.com.fileshare.FileShare;
import org.yht.exps.express.model.com.fileshare.FileShareFile;
import org.yht.exps.express.model.consumerService.order.OrderCustomerList;
import org.yht.exps.express.model.consumerService.order.search.SearchOrderCustomerList;
import org.yht.exps.express.model.eCommerce.bondedTrans.BondedTrans;
import org.yht.exps.express.model.eCommerce.bondedTrans.BondedTransDelete;
import org.yht.exps.express.model.eCommerce.bondedTrans.BondedTransList;
import org.yht.exps.express.model.eCommerce.bondedTrans.BondedTransSaveResult;
import org.yht.exps.express.model.eCommerce.bondedTrans.search.SearchBondedTrans;
import org.yht.exps.express.model.eCommerce.bondedTrans.search.SearchBondedTransList;
import org.yht.exps.express.model.eCommerce.broker.BrokerMasterSub;
import org.yht.exps.express.model.eCommerce.broker.BrokerMasterSubList;
import org.yht.exps.express.model.eCommerce.changeDelivery.ChangeDelivery;
import org.yht.exps.express.model.eCommerce.changeDelivery.ChangeDeliveryList;
import org.yht.exps.express.model.eCommerce.changeDelivery.ChangeDeliverySaveResult;
import org.yht.exps.express.model.eCommerce.changeDelivery.search.SearchChangeDelivery;
import org.yht.exps.express.model.eCommerce.changeDelivery.search.SearchChangeDeliveryList;
import org.yht.exps.express.model.eCommerce.consol.ConsolOrder;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgList;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgOrder;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgOrderList;
import org.yht.exps.express.model.eCommerce.multiWg.MultiWgSaveResult;
import org.yht.exps.express.model.eCommerce.multiWg.search.SearchMultiWg;
import org.yht.exps.express.model.eCommerce.multiWg.search.SearchMultiWgOrderList;
import org.yht.exps.express.model.eCommerce.order.ExcelUploadOdChg;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.OrderCost;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderList;
import org.yht.exps.express.model.eCommerce.quickRequest.QuickRequest;
import org.yht.exps.express.model.eCommerce.quickRequest.QuickRequestDeleteList;
import org.yht.exps.express.model.eCommerce.quickRequest.QuickRequestList;
import org.yht.exps.express.model.eCommerce.quickRequest.QuickRequestSaveResult;
import org.yht.exps.express.model.eCommerce.quickRequest.search.SearchQuickRequest;
import org.yht.exps.express.model.eCommerce.quickRequest.search.SearchQuickRequestList;
import org.yht.exps.express.model.eCommerce.returnMng.ReturnMng;
import org.yht.exps.express.model.eCommerce.returnMng.ReturnMngDeleteList;
import org.yht.exps.express.model.eCommerce.returnMng.ReturnMngList;
import org.yht.exps.express.model.eCommerce.returnMng.ReturnMngSaveResult;
import org.yht.exps.express.model.eCommerce.returnMng.search.SearchReturnMngList;
import org.yht.exps.express.model.eCommerce.unusualMng.UnusualMng;
import org.yht.exps.express.model.eCommerce.unusualMng.UnusualMngList;
import org.yht.exps.express.model.eCommerce.unusualMng.search.SearchUnusualMngList;
import org.yht.exps.express.model.edi.abnormal.Abnormal;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.OrderCommonService;
import org.yht.exps.express.service.eCommerce.ConsolService;
import org.yht.exps.express.service.sms.SmsUserService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.FileUtil;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class OrderCommonCsService {

	@Autowired
	CustomProperties customProperties;
	@Autowired
    CommonMapper commonMapper;
	@Autowired
	OrderMapper orderMapper;
	@Autowired
	OrderCommonCsMapper orderCommonCsMapper;
	@Autowired
	EnterpriseMapper enterpriseMapper;
	@Autowired
	AbnormalMapper abnormalMapper;
	@Autowired
	DeliveryChgMapper deliveryChgMapper;

	@Autowired
	SystemMngMapperVE systemMngMapperVE;

	@Autowired
	SmsUserService smsUserService;
	@Autowired
	ConsolService consolService;
	
	@Resource(name = "fileService")
	FileService fileService;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	
	@Autowired
    FunctionMapper functionMapper;
	
	@Resource(name = "orderCommonService")
	OrderCommonService orderCommonService;
	
	@Resource(name = "commonService")
	CommonService commonService;
	
	
	@Value("${spring.profiles.active}")
	private String profile;
	
	private Logger orderParamsLog = LoggerFactory.getLogger("orderParams");
	
	/**
	 * Order List
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderCustomerList selectOrderList(SearchOrderCustomerList search) throws Exception {
		String logVal = ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> OrderCustomerController.selectOrderList!! " + search.getSrchEntCode() + " / "
			+ search.getLoginId() + "(" + search.getUserName() + ") / " + search.getSrchDateGbn() + " / " + search.getSrchStartDate() + " ~ " + search.getSrchEndDate();
		orderParamsLog.info(logVal);
		search.setProfileName(profile);
		
		//유저의 거래처코드 추출
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("entCode", search.getSrchEntCode());
		param.put("userId", search.getSrchLoginId());
		
		OrderCustomerList orderCustomerList = new OrderCustomerList();
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
		//
		
		/* 로그인 유저의 거래처 레벨이 없으면 조회하지 않고 오류 표현 */
		List<Order> list = null;
		if(Util.null2str(search.getLoginCustomerLevNan()).equals("Y")) {
			list = new ArrayList<Order>();
			orderCustomerList.setResultCode("loginCustomerLevNan");
		}else {
			Util.getCryptographyYn(search);
			Util.accessControlDatabase(search, "loginCustomerUpperLev|loginCustomerLowLev|userLanguage|loginCustomerLev|srchDateFormat", true, "srchDateGbn", "od.PO_DATE|od.AR_DATE|od.OB_DATE|od.REGISTER_DATE_TIME", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.CONSIGNEE_HP|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO");
			list  = orderCommonCsMapper.selectOrderList(search);
		}
		
		orderCustomerList.setOrderCustomerList(list);
		if(list.size() > 0) {
			orderCustomerList.setTotal(list.get(0).getTotal());
		}else {
			orderCustomerList.setTotal(0);
		}
		return orderCustomerList;
	}
	
	/**
	 * Inspection Detail
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public Order selectInspectionDetail(SearchOrderCustomerList search) throws Exception {
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "userLanguage", false, null, null, null, null);
		Order inspectionDetail = orderCommonCsMapper.selectInspectionDetail(search);
		return inspectionDetail;
	}
	
	/**
	 * Inspection Detail 팝업저장
	 * @param broker
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult saveInspectionDetailUpdate(Order order){
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		try {
			if(order == null) {
				orderSaveResult.setResultCode("A00001");
				return orderSaveResult;
			}
			//오더 업데이트
			orderCommonCsMapper.saveInspectionDetailUpdate(order);
		}catch(Exception e) {
			log.error(e.getMessage());
			orderSaveResult.setResultCode("A00001");
		}
		return orderSaveResult;
	}
	
	/**
	 * 엑셀업로드 check
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult excelUploadCheck(MultipartFile uploadExcel, String entCode, String excelUploadType) throws Exception{
		Map<String, Object> resultData = new HashMap<String, Object>();
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		// 파일 확장자 유효성 검사
		if(!fileService.checkExtension(uploadExcel, new String[]{"xlsx", "xls"})) {
			orderSaveResult.setResultCode("extError");
			return orderSaveResult;
		}
		
		FileUploadResponse result = fileService.storeFile(uploadExcel, entCode, true);
		
		Map<String, Object> rstHbl = null;
		
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
			
			String[] fieldNameItem = null;
			String[] fieldTitleItem = null;
			String[] fieldLengthItem = null;
			String[] fieldTypeItem = null;
			
			String[] fieldNameCntr = null;
			String[] fieldTitleCntr = null;
			String[] fieldLengthCntr = null;
			String[] fieldTypeCntr = null;
			
			if(excelUploadType.equals("insertMgn")){
				fieldName =		new String[]{"NO","MBL_NO"	,"HBL_NO",		"UNMANAGEMENT_DATE"};
				fieldTitle =	new String[]{"No","M.B/L No"	,"H.B/L No",	"반입이상 일자"};
				fieldLength =	new String[]{"4","20",		"20",		"16",};
				fieldType =		new String[]{"S","S","S","S"};
			}else if(excelUploadType.equals("insertMfn")){
				fieldName =		new String[]{"NO","MBL_NO"	,"HBL_NO"};
				fieldTitle =	new String[]{"No","M.B/L No"	,"H.B/L No"};
				fieldLength =	new String[]{"4","20",		"20"};
				fieldType =		new String[]{"S","S","S"};
			}else if(excelUploadType.equals("insertBondedTrans")){
				fieldName =		new String[]{"AR_DATE", "SHIPPER_NAME", "HBL_NO", "PIECES", "BONDED_TRANS_DATE", "BONDED_TRANS_TYPE", "INSPECTION_DROP", "TRANSPORT_DATE","CARRYOUT_DATE","CARRYOUT_REMARK"};
				fieldTitle =	new String[]{"입항일자","송화인"	,"BL No", "CT", "보세운송신고일","보세유형","검사취하","운송일","반출일","반출내역비고"};
				fieldLength =	new String[]{"8","100","20","4","8","50","50","8","8","100"};
				fieldType =		new String[]{"S","S","S","N","S","S","S","S","S","S"};
			}else if(excelUploadType.equals("insertChangeDelivery")){
				fieldName =		new String[]{"REQUEST_DATE", "SHIPPER_NAME", "HBL_NO", "DELIVERY_NAME_CHANGED", "DELIVERY_ZIPCODE_CHANGED", "DELIVERY_ADDRESS_CHANGED", "DELIVERY_TEL_CHANGED", "DELIVERY_CODE_CHANGED","DELIVERY_NO_CHANGED","DELIVERY_CHANGED_DATE"};
				fieldTitle =	new String[]{"요청일자","송화인"	,"원송장번호", "이름","우편번호", "재발송주소","연락처","택배사","변경송장번호","교체일자"};
				fieldLength =	new String[]{"8","100","20","100","10","300","50","30","20","8"};
				fieldType =		new String[]{"S","S","S","S","S","S","S","S","S","S"};
			}else if(excelUploadType.equals("insertQuickRequest")){
				fieldName =		new String[]{"REQUEST_DATE", "CUST_CODE_LV1", "HBL_NO", "PIECES", "INCOTERMS", "QUICK_FEE", "TAKE_OVER_DATE", "DRIVER_NAME", "REMARK"};
				fieldTitle =	new String[]{"등록일","상(하)위업체"	,"송장번호", "수량","운임조건", "운임","퀵인계일자","배송기사","비고"};
				fieldLength =	new String[]{"8","20","20","4","2","10","8","100","100"};
				fieldType =		new String[]{"S","S","S","N","S","S","S","S","S"};
			}else if(excelUploadType.equals("insertReturn")){
				fieldName =		new String[]{"HBL_NO","RETURN_REQ_DATE","RETURN_REQUEST_ID","RETURN_DELIVERY_CODE","RETURN_DELIVERY_NO","RETURN_REG_DATE","DELIVERY_STATUS"};
				fieldTitle =	new String[]{"H.B/L No","반품요청일","반품번호","반품배송사","반품송장번호","반품접수일","반품배송상태"};
				fieldLength =	new String[]{"20","8","20","30","30","8","6"};
				fieldType =		new String[]{"S","S","S","S","S","S","S"};

			}
			
			List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> dataListSub = null;
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				Workbook wb = WorkbookFactory.create(is);
				Sheet sheet = wb.getSheetAt(0);
				DataFormatter df = new DataFormatter();
				int fieldSize = fieldName.length;
				int fieldSizeItem = 0;
				int fieldSizeCntr = 0;
				if(fieldNameItem != null) fieldSizeItem = fieldNameItem.length;
				if(fieldNameCntr != null) fieldSizeCntr = fieldNameCntr.length;
				StringBuffer errMsg = new StringBuffer();
				Map<String, Object> dataMap = null;
				Map<String, Object> dataMapSub = null;
				String fieldValue = "";
				String firstMblNo = null;
				boolean chkMblNo = false;
				// 데이트 포맷
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				
				if(extension.equals("xlsx")){
					XSSFCell myCell = null;
					for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
						if(sheet.getRow(i) == null){
							orderSaveResult.setResultCode("A11233");	/** #1#행이 비어있습니다. (파일 마지막행 : #2#) */
							resultData.put("nullLine", i + 1);
							resultData.put("lastLine", sheet.getLastRowNum() + 1);
							orderSaveResult.setResultData(resultData);
							return orderSaveResult;
						}
						
						dataMap = new HashMap<String, Object>();
						//셀 셋팅
						for(int j = 0; j < fieldSize; j++){
							myCell = (XSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i, false);
							if(!Util.null2str(fieldValue).equals("")){
								dataMap.put(fieldName[j], fieldValue);
							}
						}
						
						if(excelUploadType.equals("insertMgn")) {
							if(!Util.null2str(dataMap.get("MBL_NO")).equals("") && !Util.null2str(dataMap.get("HBL_NO")).equals("") && !Util.null2str(dataMap.get("UNMANAGEMENT_DATE")).equals("")){
								dataList.add(dataMap);
							}else{
								orderSaveResult.setResultCode("A00034");	/** 필수입력 항목을 입력하거나 입력한 자료를 다시 확인 바랍니다. */
								resultData.put("errorLine", i + 1);
								orderSaveResult.setResultData(resultData);
								return orderSaveResult;
							}
						}else if(excelUploadType.equals("insertMfn")) {
							if(!Util.null2str(dataMap.get("MBL_NO")).equals("") && !Util.null2str(dataMap.get("HBL_NO")).equals("")){
								dataList.add(dataMap);
							}else{
								orderSaveResult.setResultCode("A00034");	/** 필수입력 항목을 입력하거나 입력한 자료를 다시 확인 바랍니다. */
								resultData.put("errorLine", i + 1);
								orderSaveResult.setResultData(resultData);
								return orderSaveResult;
							}
						}else if(excelUploadType.equals("insertBondedTrans")) {
							if(!Util.null2str(dataMap.get("HBL_NO")).equals("") && !Util.null2str(dataMap.get("BONDED_TRANS_DATE")).equals("")){
								dataList.add(dataMap);
							}else{
								orderSaveResult.setResultCode("A00034");	/** 필수입력 항목을 입력하거나 입력한 자료를 다시 확인 바랍니다. */
								resultData.put("errorLine", i + 1);
								orderSaveResult.setResultData(resultData);
								return orderSaveResult;
							}
						}else if(excelUploadType.equals("insertChangeDelivery")) {
							if(!Util.null2str(dataMap.get("HBL_NO")).equals("")  && !Util.null2str(dataMap.get("REQUEST_DATE")).equals("")){
								dataList.add(dataMap);
							}else{
								orderSaveResult.setResultCode("A00034");	/** 필수입력 항목을 입력하거나 입력한 자료를 다시 확인 바랍니다. */
								resultData.put("errorLine", i + 1);
								orderSaveResult.setResultData(resultData);
								return orderSaveResult;
							}
						}else if(excelUploadType.equals("insertQuickRequest")) {
							if(!Util.null2str(dataMap.get("HBL_NO")).equals("")  && !Util.null2str(dataMap.get("REQUEST_DATE")).equals("")){
								dataList.add(dataMap);
							}else{
								orderSaveResult.setResultCode("A00034");	/** 필수입력 항목을 입력하거나 입력한 자료를 다시 확인 바랍니다. */
								resultData.put("errorLine", i + 1);
								orderSaveResult.setResultData(resultData);
								return orderSaveResult;
							}
						}else{
							dataList.add(dataMap);
						}
					}
				}else{
					HSSFCell myCell = null;
					for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
						if(sheet.getRow(i) == null){
							orderSaveResult.setResultCode("A11233");	/** #1#행이 비어있습니다. (파일 마지막행 : #2#) */
							resultData.put("nullLine", i + 1);
							resultData.put("lastLine", sheet.getLastRowNum() + 1);
							orderSaveResult.setResultData(resultData);
							return orderSaveResult;
						}
						
						dataMap = new HashMap<String, Object>();
						//셀 셋팅
						for(int j = 0; j < fieldSize; j++){
							myCell = (HSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i, false);
							if(!Util.null2str(fieldValue).equals("")){
								dataMap.put(fieldName[j], fieldValue);
							}
						}
						
						if(excelUploadType.equals("insertMgn")) {
							if(!Util.null2str(dataMap.get("MBL_NO")).equals("") && !Util.null2str(dataMap.get("HBL_NO")).equals("") && !Util.null2str(dataMap.get("UNMANAGEMENT_DATE")).equals("")){
								dataList.add(dataMap);
							}else{
								orderSaveResult.setResultCode("A00034");	/** 필수입력 항목을 입력하거나 입력한 자료를 다시 확인 바랍니다. */
								resultData.put("errorLine", i + 1);
								orderSaveResult.setResultData(resultData);
								return orderSaveResult;
							}
						}else if(excelUploadType.equals("insertMfn")) {
							if(!Util.null2str(dataMap.get("MBL_NO")).equals("") && !Util.null2str(dataMap.get("HBL_NO")).equals("")){
								dataList.add(dataMap);
							}else{
								orderSaveResult.setResultCode("A00034");	/** 필수입력 항목을 입력하거나 입력한 자료를 다시 확인 바랍니다. */
								resultData.put("errorLine", i + 1);
								orderSaveResult.setResultData(resultData);
								return orderSaveResult;
							}
						}else if(excelUploadType.equals("insertBondedTrans")) {
							if(!Util.null2str(dataMap.get("HBL_NO")).equals("")){
								dataList.add(dataMap);
							}else{
								orderSaveResult.setResultCode("A00034");	/** 필수입력 항목을 입력하거나 입력한 자료를 다시 확인 바랍니다. */
								resultData.put("errorLine", i + 1);
								orderSaveResult.setResultData(resultData);
								return orderSaveResult;
							}
						}else if(excelUploadType.equals("insertChangeDelivery")) {
							if(!Util.null2str(dataMap.get("HBL_NO")).equals("")  && !Util.null2str(dataMap.get("REQUEST_DATE")).equals("")){
								dataList.add(dataMap);
							}else{
								orderSaveResult.setResultCode("A00034");	/** 필수입력 항목을 입력하거나 입력한 자료를 다시 확인 바랍니다. */
								resultData.put("errorLine", i + 1);
								orderSaveResult.setResultData(resultData);
								return orderSaveResult;
							}
						}else if(excelUploadType.equals("insertQuickRequest")) {
							if(!Util.null2str(dataMap.get("HBL_NO")).equals("")  && !Util.null2str(dataMap.get("REQUEST_DATE")).equals("")){
								dataList.add(dataMap);
							}else{
								orderSaveResult.setResultCode("A00034");	/** 필수입력 항목을 입력하거나 입력한 자료를 다시 확인 바랍니다. */
								resultData.put("errorLine", i + 1);
								orderSaveResult.setResultData(resultData);
								return orderSaveResult;
							}
						}
						else{
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
				Set<String> hblNoList = new HashSet<>();
				Set<String> dupNoList = new HashSet<>();

				for (Map<String, Object> row : dataList) {
				    String hblNo = (String) row.get("HBL_NO");
				    if (!hblNoList.add(hblNo)) {
				    	dupNoList.add(hblNo);
				    }
				}
				if(!dupNoList.isEmpty()) {
					resultData.put("dupHbl", dupNoList);
					orderSaveResult.setResultData(resultData);
					return orderSaveResult;
				}
				resultData.put("entCode", entCode);
				resultData.put("excelUploadType", excelUploadType);
				
				//hbl 유효성 체크
				resultData.put("dataList", dataList);
				rstHbl = deliveryChgMapper.selectHblValidate(resultData);
				
				if(rstHbl!=null) {
					//유효하지않은 BL
					resultData.put("NOT_BL_CNT", rstHbl.get("NOT_BL_CNT"));
					resultData.put("NOT_HBL_LIST", rstHbl.get("NOT_HBL_LIST"));
					resultData.remove("dataList");
				}else {
					resultData.put("NOT_BL_CNT", "0");
					resultData.put("NOT_HBL_LIST", "");
				}
				
				resultData.put("dataSize", dataList.size());
				
				
				
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
	 * 엑셀업로드 미착/무적관리
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult excelUploadInsertMgnMfn(ExcelUploadOdChg excelUploadOdChg, String excelUploadType, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		List<String> odJobNoList = new ArrayList<>();
		Map<String, Object> odJobNo = new HashMap<String, Object>();
		List<String> odJobNoDataList = new ArrayList<>();
		
		if(excelUploadOdChg.getDataList() == null) {
			return orderSaveResult;
		}
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Util.accessControlDatabase(excelUploadOdChg, "gmtTime", false, null, null, null, null);
		SqlSession sqlSession = null;
		int cnt = 1;
		int seq = 1;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderCommonCsMapper orderBatchMapper = sqlSession.getMapper(OrderCommonCsMapper.class);
			Map<String, Object> tmpLogMap = new HashMap<String, Object>();
			tmpLogMap.put("ent_code", excelUploadOdChg.getEntCode());
			tmpLogMap.put("ent_business_code", excelUploadOdChg.getEntBusinessCode());
			tmpLogMap.put("solution", "ECM");
			tmpLogMap.put("process_user_id", excelUploadOdChg.getUpdateUserId());
			tmpLogMap.put("log_job_type", "DATA");
			tmpLogMap.put("job_func_type", "EXCEL");
			tmpLogMap.put("result", "ok");
			tmpLogMap.put("process_date_time", System.currentTimeMillis());
			tmpLogMap.put("ent_business_print_name", excelUploadOdChg.getLogUserBusinessPrintName());
			tmpLogMap.put("process_user_name", excelUploadOdChg.getLogUserName());
			tmpLogMap.put("process_user_name_eng", excelUploadOdChg.getLogUserNameEng());
			if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
			
			for(Map<String, Object> excelOdChg : excelUploadOdChg.getDataList()){
				Map<String, Object> selectOdJobNo = new HashMap<String, Object>();
				selectOdJobNo.put("entCode", excelUploadOdChg.getEntCode());
				selectOdJobNo.put("hblNo", Util.null2str(excelOdChg.get("HBL_NO")));
				selectOdJobNo.put("mblNo", Util.null2str(excelOdChg.get("MBL_NO")));
				selectOdJobNo.put("bound", "SEA");
				selectOdJobNo.put("boundType", "IMP");
				odJobNo = orderBatchMapper.selectOdJobNoByHblNo(selectOdJobNo);
				if(odJobNo != null) {
					odJobNoList.add(Util.null2str(odJobNo.get("OD_JOB_NO")));
					excelOdChg.put("odJobNo", Util.null2str(odJobNo.get("OD_JOB_NO")));
				}
			}
			
			if(odJobNoList != null && odJobNoList.size() > 0) {
				odJobNoDataList = odJobNoList.stream().distinct().collect(Collectors.toList());
				Abnormal abnormal = new Abnormal();
				abnormal.setOdJobNoList(odJobNoDataList);
				Map<String, Object> map = abnormalMapper.getNoRegisterCount(abnormal);
				int mgnCnt = Util.null2int(map.get("MGN_CNT"));
				int otherCnt = Util.null2int(map.get("OTHER_CNT"));
				int totalCnt = Util.null2int(map.get("TOTAL_CNT"));
				if("insertMgn".equals(excelUploadType)) {
					if(mgnCnt == totalCnt) {
						orderSaveResult.setResultCode("A11388");/** 하기코드가 있는 건이 포함되어 있습니다. */
						return orderSaveResult;
					}
				}else {
					if(otherCnt == totalCnt) {
						orderSaveResult.setResultCode("A11388");/** 하기코드가 있는 건이 포함되어 있습니다. */
						return orderSaveResult;
					}
				}
				
			}
			
			cnt = 1;
			for(Map<String, Object> excelOdChg : excelUploadOdChg.getDataList()){
				excelOdChg.put("entCode", excelUploadOdChg.getEntCode());
				excelOdChg.put("hblNo", Util.null2str(excelOdChg.get("HBL_NO")));
				excelOdChg.put("odJobNoList", odJobNoDataList);
				if("insertMgn".equals(excelUploadType)) {
					excelOdChg.put("odJobNo", Util.null2str(excelOdChg.get("odJobNo")));
					excelOdChg.put("unmanagementDate", excelOdChg.get("UNMANAGEMENT_DATE"));
					excelOdChg.put("abnormalCode", "MGN");
				}else {
					if(!"".equals(Util.null2str(excelOdChg.get("odJobNo")))) {
						excelOdChg.put("odJobNo", Util.null2str(excelOdChg.get("odJobNo")));
					}else { //hblNo가 존재하지않으면 hbl데이터 생성
						//마스터 정보 조회
						Map<String, Object> mblInfo = orderCommonCsMapper.selectMblInfo(excelOdChg);
						if(mblInfo != null && mblInfo.size() > 0) {
							// OD_JOB_NO 생성을 위해 세팅  
							Map<String, Object> stockMap = new HashMap<String,Object>();
							stockMap.put("gmtTime", "0");
							stockMap.put("entCode", excelUploadOdChg.getEntCode());
							stockMap.put("entBusinessCode", "");
							stockMap.put("solution", "ECM");
							stockMap.put("stockType", "OD");
							stockMap.put("stockSign", "COD");
							stockMap.put("stockSeqLength", 8);
							stockMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
							stockMap.put("stockRemark", "ORDER-JOB NO");
							stockMap.put("updateUserId", excelUploadOdChg.getUpdateUserId());
							stockMap.put("registerUserId", excelUploadOdChg.getUpdateUserId());
							String hblOdJobNo = commonMapper.selectNextStockMaxNo(stockMap);
							//HBL생성
							Map<String, Object> createHbl = new HashMap<String, Object>();
							createHbl.put("entCode", excelUploadOdChg.getEntCode());
							createHbl.put("entBusinessCode", excelUploadOdChg.getEntBusinessCode());
							createHbl.put("odJobNo", hblOdJobNo);
							createHbl.put("bound", Util.null2str(mblInfo.get("BOUND")));
							createHbl.put("boundType", Util.null2str(mblInfo.get("BOUND_TYPE")));
							createHbl.put("custCodeLv1", Util.null2str(mblInfo.get("CUST_CODE_LV1")));
							createHbl.put("hblNo", Util.null2str(excelOdChg.get("HBL_NO")));
							createHbl.put("registerUserId", excelUploadOdChg.getUpdateUserId());
							createHbl.put("gmtTime", excelUploadOdChg.getGmtTime());
							createHbl.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001");
							orderCommonCsMapper.createHbl(createHbl);
							//마감 후 콘솔처리
							ConsolOrder consolOrder = new ConsolOrder();
							consolOrder.setAction("closeMasterConsol");
							consolOrder.setEntCode(excelUploadOdChg.getEntCode());
							consolOrder.setEntBusinessCode(excelUploadOdChg.getEntBusinessCode());
							consolOrder.setGmtTime(excelUploadOdChg.getGmtTime());
							consolOrder.setUpdateUserId(excelUploadOdChg.getUpdateUserId());
							consolOrder.setOdJobNo(Util.null2str(mblInfo.get("OD_JOB_NO")));
							consolOrder.setBound(Util.null2str(mblInfo.get("BOUND")));
							consolOrder.setBoundType(Util.null2str(mblInfo.get("BOUND_TYPE")));
							List<String> hblNoList = new ArrayList<>();
							hblNoList.add(Util.null2str(excelOdChg.get("HBL_NO")));
							consolOrder.setHblNoList(hblNoList);
							orderSaveResult = consolService.closeMasterConsol(consolOrder, request);
							if(!"A11283".equals(orderSaveResult.getResultCode())){
								return orderSaveResult;
							}else {
								//콘솔 완료 후 무적데이터 생성
								excelOdChg.put("odJobNo", hblOdJobNo);
							}
						}else {
							orderSaveResult.setResultCode("A01027");/** 사용되지 않은 M.B/L번호 입니다. */
							return orderSaveResult;
						}
					}

					excelOdChg.put("abnormalCode", "MFN");
				}
				excelOdChg.put("cnt", "1");
				excelOdChg.put("updateUserId", excelUploadOdChg.getUpdateUserId());
				excelOdChg.put("gmtTime", excelUploadOdChg.getGmtTime());
				
				orderBatchMapper.insertMgnMfn(excelOdChg);
				
				// POD insert
				Map<String, Object> podMap = new HashMap<>();
				podMap.put("entCode", excelUploadOdChg.getEntCode());
				podMap.put("gmtTime", excelUploadOdChg.getGmtTime());
				podMap.put("updateUserId", excelUploadOdChg.getUpdateUserId());
				podMap.put("odJobNo", excelOdChg.get("odJobNo"));
				podMap.put("blNo", excelOdChg.get("HBL_NO"));
				podMap.put("podStatus", "ECM160");
				podMap.put("podJobNo", DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001");
				podMap.put("podTableName", "TB_ECM_POD_02");
				orderBatchMapper.deleteOrderPod(podMap);
				orderBatchMapper.insertOrderPod(podMap);
				
				// 처리 로그 기록
				bulkData = new BulkData();
				bulkData.setActionType(BulkData.Type.CREATE);
				bulkData.setIndexName(index);
				bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				
				logMap = new HashMap<String, Object>();
				logMap.putAll(tmpLogMap);
				logMap.put("bound", excelUploadOdChg.getBound());
				logMap.put("bound_type", "IMP");
				logMap.put("reference_no_id", bulkData.getId());
				logMap.put("job_no", "");
				logMap.put("job_log", "");
				logMap.put("description", "엑셀 미착/무적 수정 [HBL NO : " + Util.null2str(excelOdChg.get("HBL_NO")) + "]");
				bulkData.setJsonMap(logMap);
				bulkList.add(bulkData);
				
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
			
			// 처리 로그 기록
			commonService.insertLogBulk(bulkList);
			
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
		if("A11283".equals(orderSaveResult.getResultCode())) {
			orderSaveResult.setResultCode(null);
		}
		return orderSaveResult;
		
	}
	
	/**
	 * 엑셀업로드 데이터 check
	 * @param map
	 * @return
	 * @throws Exception
	 */
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
				if(!"pass".equals(length) && Util.getKor2ByteLength(data) > Util.null2int(length)) {
					errMsg.append("[" + name + "] DATA 길이 오류(행 : " + row + ", 입력값 : " + data + ", 제한 : " + length + "자리) <br />");
				}
			}
		}else if("SN".equals(type)){
			if(Util.null2str(data).equals("")){
				data = null;
			}else{
				if(data.length() > Util.null2int(length)) {
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
		Util.accessControlDatabase(search, "userLanguage", true, "srchDateGbn", "od.PO_DATE|od.AR_DATE|od.OB_DATE", "srchNoType", "od.PO_NO|od.HBL_NO|od.MBL_NO");
		List<BrokerMasterSub> list  = orderCommonCsMapper.selectBrokerMasterSubList(search);
		brokerMasterSubList.setBrokerMasterSubList(list);
		if(list.size() > 0) {
			brokerMasterSubList.setTotal(list.get(0).getTotal());
		}else {
			brokerMasterSubList.setTotal(0);
		}
		return brokerMasterSubList;
	}
	
	/**
	 * Broker Detail
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BrokerMasterSub selectBrokerDetail(SearchOrderList search) throws Exception {
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "userLanguage", false, null, null, null, null);
		BrokerMasterSub brokerMasterSub = orderCommonCsMapper.selectBrokerDetail(search);
//		if(brokerMasterSub != null && !"".equals(Util.null2str(brokerMasterSub.getReferenceNoId()))) {
//			List<FileShareFile> fileShareFile = orderCommonCsMapper.selectFileShareList(brokerMasterSub);
//			if(fileShareFile != null && fileShareFile.size() > 0) {
//				brokerMasterSub.setFileShareFile(fileShareFile);
//			}
//		}
		
		return brokerMasterSub;
	}
	
	/**
	 * BrokerMasterSub 팝업저장
	 * @param broker
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult saveBrokerUpdate(BrokerMasterSub broker){
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		try {
			if(broker == null) {
				orderSaveResult.setResultCode("A00001");
				return orderSaveResult;
			}
			//오더 업데이트
			orderCommonCsMapper.saveBrokerUpdate(broker);
			
			//세금수정
			OrderCost odCost = null;
			List<OrderCost> odCostList = new ArrayList<>();
			//관세
		    if (Util.null2dbl(broker.getTariff()) > 0) {
		        odCost = new OrderCost();
		        odCost.setOdJobNo(broker.getOdJobNo());
		        odCost.setChargeCode("DTY");
		        odCost.setApAmount(Util.null2dbl(broker.getTariff()));
		        odCost.setPaymentGubn(Util.null2str(broker.getClearancePaymentType()));
		        odCost.setRegisterUserId(Util.null2str(broker.getRegisterUserId()));
		        odCost.setEntCode(Util.null2str(broker.getEntCode()));
		        odCost.setGmtTime(Util.null2str(broker.getGmtTime()));
		        odCostList.add(odCost);
		    }
		    //부가세
		    if (broker.getSurtax() > 0) {
		    	odCost = new OrderCost();
		        odCost.setOdJobNo(broker.getOdJobNo());
		        odCost.setChargeCode("TAX");
		        odCost.setApAmount(Util.null2dbl(broker.getSurtax()));
		        odCost.setPaymentGubn(Util.null2str(broker.getClearancePaymentType()));
		        odCost.setRegisterUserId(Util.null2str(broker.getRegisterUserId()));
		        odCost.setEntCode(Util.null2str(broker.getEntCode()));
		        odCost.setGmtTime(Util.null2str(broker.getGmtTime()));
		        odCostList.add(odCost);
		    }
		    //통관수수료
		    if (broker.getClearanceFee() > 0) {
		    	odCost = new OrderCost();
		        odCost.setOdJobNo(broker.getOdJobNo());
		        odCost.setChargeCode("CFC");
		        odCost.setApAmount(Util.null2dbl(broker.getClearanceFee()));
		        odCost.setPaymentGubn(Util.null2str(broker.getClearancePaymentType()));
		        odCost.setRegisterUserId(Util.null2str(broker.getRegisterUserId()));
		        odCost.setEntCode(Util.null2str(broker.getEntCode()));
		        odCost.setGmtTime(Util.null2str(broker.getGmtTime()));
		        odCostList.add(odCost);
		    }
		    //검역수수료
		    if (broker.getQurantineFee() > 0) {
		    	odCost = new OrderCost();
		        odCost.setOdJobNo(broker.getOdJobNo());
		        odCost.setChargeCode("QUA");
		        odCost.setApAmount(Util.null2dbl(broker.getQurantineFee()));
		        odCost.setPaymentGubn(Util.null2str(broker.getClearancePaymentType()));
		        odCost.setRegisterUserId(Util.null2str(broker.getRegisterUserId()));
		        odCost.setEntCode(Util.null2str(broker.getEntCode()));
		        odCost.setGmtTime(Util.null2str(broker.getGmtTime()));
		        odCostList.add(odCost);
		    }
		    //기타수수료
		    if (broker.getEtcFee() > 0) {
		    	odCost = new OrderCost();
		        odCost.setOdJobNo(broker.getOdJobNo());
		        odCost.setChargeCode("SPTAX");
		        odCost.setApAmount(Util.null2dbl(broker.getEtcFee()));
		        odCost.setPaymentGubn(Util.null2str(broker.getClearancePaymentType()));
		        odCost.setRegisterUserId(Util.null2str(broker.getRegisterUserId()));
		        odCost.setEntCode(Util.null2str(broker.getEntCode()));
		        odCost.setGmtTime(Util.null2str(broker.getGmtTime()));
		        odCostList.add(odCost);
		    }
			
			if(odCostList != null && odCostList.size() > 0) {
				for(OrderCost orderCost : odCostList) {
					orderCommonCsMapper.insertOdCost(orderCost);
				}
			}
		
		}catch(Exception e) {
			log.error(e.getMessage());
			orderSaveResult.setResultCode("A00001");
		}
		return orderSaveResult;
	}
	
	/**
	 * 이영(중량) 화물관리 리스트
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public MultiWgOrderList selectSubMultiWgList(SearchMultiWgOrderList search) throws Exception {
		MultiWgOrderList multiWgOrderList = new MultiWgOrderList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/** N건 검색 */
		List<String> srchblNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			srchblNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}
		
		if(srchblNoList.size() > 1) {
			search.setSearchType("multi");
			search.setSrchBlNoList(srchblNoList);
		}else {
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",",""));
		}
		Util.accessControlDatabase(search, "srchCusType|srchDateFormat", true, "srchDateGbn", "od.AR_DATE|od.OB_DATE|od.REGISTER_DATE_TIME|noDateChk", null, null);
		List<MultiWgOrder> list  = orderCommonCsMapper.selectSubMultiWgList(search);
		multiWgOrderList.setMultiWgOrderList(list);
		if(list.size() > 0) {
			multiWgOrderList.setTotal(list.get(0).getTotal());
		}else {
			multiWgOrderList.setTotal(0);
		}
		return multiWgOrderList;
	}
	
	/**
	 * Mutil Order Detail
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MultiWgOrder selectMultiOrderDetail(SearchMultiWg search) throws Exception {
		MultiWgOrder order  = orderCommonCsMapper.selectMultiOrderDetail(search);
		return order;
	}
	
	/**
	 * 전자상거래 이영(중량) - 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MultiWgSaveResult saveMultiWgList(MultiWgList multiWgList, HttpServletRequest request) throws Exception {
		
		MultiWgSaveResult multiWgSaveResult = new MultiWgSaveResult();
		if(multiWgList == null) {
			multiWgSaveResult.setResultCode("A11337");
			return multiWgSaveResult;
		}		
		Util.accessControlDatabase(multiWgList, "gmtTime", false, null, null, null, null);
		orderCommonCsMapper.deleteMultiWgOrder(multiWgList);
		List<MultiWgOrder> list = multiWgList.getMultiWgOrderList();
		
		for(int i=0;i<list.size();i++) {
			MultiWgOrder multiWgOrder = list.get(i);
			multiWgOrder.setOdJobNo(multiWgList.getOdJobNo());
			multiWgOrder.setGmtTime(multiWgList.getGmtTime());
			multiWgOrder.setRegisterUserId(multiWgList.getRegisterUserId());
			orderCommonCsMapper.insertMulti(multiWgOrder);
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
		processLogs.setDescription("이영(중량)화물 > "+ multiWgList.getAction() +" [HBL_NO : "+ multiWgList.getHblNo() +"]");
		processLogs.setJobLog("");
		processLogs.setResult("ok");
		processLogs.setJobNo(multiWgList.getOdJobNo());
		processLogs.setEntBusinessPrintName(multiWgList.getLogUserBusinessPrintName());
		processLogs.setProcessUserName(multiWgList.getLogUserName());
		processLogs.setProcessUserNameEng(multiWgList.getLogUserNameEng());
		commonService.insertLog(processLogs, request);
		
		return multiWgSaveResult;
	}
	
	/**
	 * 전자상거래 이영(중량) - 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public MultiWgSaveResult insertMultiWg(MultiWgOrder multiWgOrder, HttpServletRequest request) throws Exception {
		
		MultiWgSaveResult multiWgSaveResult = new MultiWgSaveResult();
		if("".equals(Util.null2str(multiWgOrder.getOdJobNo()))) {
			multiWgSaveResult.setResultCode("A11337");
			return multiWgSaveResult;
		}else {
			Util.accessControlDatabase(multiWgOrder, "gmtTime", false, null, null, null, null);
			Map<String, Object> seqMap =   new HashMap<String, Object>();
			seqMap.put("odJobNo", multiWgOrder.getOdJobNo());
			// 멀티데이터 존재 여부확인 존재하면 insert X
			int seqNoId  = orderCommonCsMapper.selectMaxMultiSeqNo(seqMap);
			if(seqNoId > 0) {
				multiWgSaveResult.setResultCode("A11643");
				return multiWgSaveResult;
			}else {
				multiWgOrder.setSeqNoId("1");
				multiWgOrder.setDeliveryNo(Util.null2str(multiWgOrder.getHblNo()));
				multiWgOrder.setPieces(1);
				orderCommonCsMapper.insertMulti(multiWgOrder);
				multiWgSaveResult.setOdJobNo(multiWgOrder.getOdJobNo());
				
				//처리로그
				ProcessLogs processLogs = new ProcessLogs();
				processLogs.setEntCode(multiWgOrder.getEntCode());
				processLogs.setEntBusinessCode(multiWgOrder.getEntBusinessCode());
				processLogs.setSolution("ECM");
				processLogs.setBound(multiWgOrder.getBound());
				processLogs.setBoundType(multiWgOrder.getBoundType());
				processLogs.setProcessUserId(multiWgOrder.getRegisterUserId());
				processLogs.setLogJobType("DATA");
				processLogs.setJobFuncType("ADD");
				processLogs.setDescription("이영(중량)화물 > INSERT "+" [HBL_NO : "+ multiWgOrder.getHblNo() +"]");
				processLogs.setJobLog("");
				processLogs.setResult("ok");
				processLogs.setJobNo(multiWgOrder.getOdJobNo());
				processLogs.setEntBusinessPrintName(multiWgOrder.getLogUserBusinessPrintName());
				processLogs.setProcessUserName(multiWgOrder.getLogUserName());
				processLogs.setProcessUserNameEng(multiWgOrder.getLogUserNameEng());
				commonService.insertLog(processLogs, request);
				return multiWgSaveResult;
			}
		}
	}
	
	/**
	 * 고객서비스 보세운송 리스트
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public BondedTransList selectBondedTransList(SearchBondedTransList search) throws Exception {
		BondedTransList bondedTransList = new BondedTransList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/** N건 검색 */
		List<String> srchblNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			srchblNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}
		
		if(srchblNoList.size() > 1) {
			search.setSearchType("multi");
			search.setSrchBlNoList(srchblNoList);
		}else {
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",",""));
		}
		Util.accessControlDatabase(search, "srchCusType|srchDateFormat", true, "srchDateGbn", "od.AR_DATE|od.OB_DATE|bond.REGISTER_DATE_TIME", null, null);
		List<BondedTrans> list  = orderCommonCsMapper.selectBondedTransList(search);
		bondedTransList.setBondedTransList(list);
		if(list.size() > 0) {
			bondedTransList.setTotal(list.get(0).getTotal());
		}else {
			bondedTransList.setTotal(0);
		}
		return bondedTransList;
	}
	
	/**
	 * 고객서비스 보세운송 Detail
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public BondedTrans selectBondedTransDetail(SearchBondedTrans search) throws Exception {
		BondedTrans order  = orderCommonCsMapper.selectBondedTransDetail(search);
		return order;
	}
	
	/**
	 * 고객서비스 보세운송 - 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public BondedTransSaveResult saveBondedTrans(BondedTrans bondedTrans, HttpServletRequest request) throws Exception {
		BondedTransSaveResult bondedTransSaveResult = new BondedTransSaveResult();
		if(bondedTrans == null || ("".equals(Util.null2str(bondedTrans.getOdJobNo())) && (bondedTrans.getOdJobNoList() == null || bondedTrans.getOdJobNoList().size() < 1))) {
		    return null;
		}
		Map<String, Object> bondedTransMap = null;
		int resultCnt = 0;
		if(!"".equals(Util.null2str(bondedTrans.getOdJobNo()))) {
			Util.accessControlDatabase(bondedTrans, "gmtTime", false, null, null, null, null);
			bondedTransMap = new HashMap<String, Object>();
			bondedTransMap.put("odJobNo",Util.null2str(bondedTrans.getOdJobNo()));
			bondedTransMap.put("bondedTransDate",Util.null2str(bondedTrans.getBondedTransDate()));
			bondedTransMap.put("bondedTransType",Util.null2str(bondedTrans.getBondedTransType()));
			bondedTransMap.put("inspectionDrop",Util.null2str(bondedTrans.getInspectionDrop()));
			bondedTransMap.put("transportDate",Util.null2str(bondedTrans.getTransportDate()));
			bondedTransMap.put("carryoutDate",Util.null2str(bondedTrans.getCarryoutDate()));
			bondedTransMap.put("carryoutRemark",Util.null2str(bondedTrans.getCarryoutRemark()));
			bondedTransMap.put("inputMethod","Manual");
			bondedTransMap.put("gmtTime",Util.null2str(bondedTrans.getGmtTime()));
			bondedTransMap.put("updateUserId",Util.null2str(bondedTrans.getRegisterUserId()));
			orderCommonCsMapper.insertBondedTrans(bondedTransMap);
			
			//CS Histroy Insert
			String csRemark  = "보세운송신고일 : " + Util.null2str(bondedTrans.getBondedTransDate()) + " | 보세운송유형 : " 
					+	Util.null2str(bondedTrans.getBondedTransType()) + " | 담당자 : " + Util.null2str(bondedTrans.getLogUserName());
			bondedTransMap.put("csRemark", csRemark);
			bondedTransMap.put("csType", "보세운송");
			bondedTransMap.put("seqNoId" , orderCommonCsMapper.selectMaxCsSeqNo(bondedTransMap) + 1);
			orderCommonCsMapper.insertOdCsHistory(bondedTransMap);
			resultCnt = 1;
			bondedTransSaveResult.setResultCnt(resultCnt);
		}else {
			if(bondedTrans.getOdJobNoList() != null && bondedTrans.getOdJobNoList().size() > 0) {
			    for(String odJobNo : bondedTrans.getOdJobNoList()) {
			    	bondedTransMap = new HashMap<String, Object>();
			    	bondedTransMap.put("odJobNo", odJobNo);
					bondedTransMap.put("bondedTransDate",Util.null2str(bondedTrans.getBondedTransDate()));
					bondedTransMap.put("bondedTransType",Util.null2str(bondedTrans.getBondedTransType()));
					bondedTransMap.put("transportDate",Util.null2str(bondedTrans.getTransportDate()));
					bondedTransMap.put("carryoutDate",Util.null2str(bondedTrans.getCarryoutDate()));
					bondedTransMap.put("inputMethod","Manual");
					bondedTransMap.put("gmtTime",Util.null2str(bondedTrans.getGmtTime()));
					bondedTransMap.put("updateUserId",Util.null2str(bondedTrans.getRegisterUserId()));
					orderCommonCsMapper.insertBondedTrans(bondedTransMap);
					
					//CS Histroy Insert
					String csRemark  = "보세운송신고일 : " + Util.null2str(bondedTrans.getBondedTransDate()) + " | 보세운송유형 : " 
							+	Util.null2str(bondedTrans.getBondedTransType()) + " | 담당자 : " + Util.null2str(bondedTrans.getLogUserName());
					bondedTransMap.put("csRemark", csRemark);
					bondedTransMap.put("csType", "보세운송");
					bondedTransMap.put("seqNoId" , orderCommonCsMapper.selectMaxCsSeqNo(bondedTransMap) + 1);
					orderCommonCsMapper.insertOdCsHistory(bondedTransMap);
					resultCnt++;
			    }
			    bondedTransSaveResult.setResultCnt(resultCnt);
			}
		}
		return bondedTransSaveResult;
	}
	
	/**
	 * 엑셀업로드 고객서비스 보세운송
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult excelUploadInsertBondedTrans(ExcelUploadOdChg excelUploadOdChg, String excelUploadType, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		Map<String, Object> odJobNo = new HashMap<String, Object>();
		
		if(excelUploadOdChg.getDataList() == null) {
			return orderSaveResult;
		}
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Util.accessControlDatabase(excelUploadOdChg, "gmtTime", false, null, null, null, null);
		SqlSession sqlSession = null;
		int cnt = 1;
		int seq = 1;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderCommonCsMapper orderBatchMapper = sqlSession.getMapper(OrderCommonCsMapper.class);
			Map<String, Object> tmpLogMap = new HashMap<String, Object>();
			tmpLogMap.put("ent_code", excelUploadOdChg.getEntCode());
			tmpLogMap.put("ent_business_code", excelUploadOdChg.getEntBusinessCode());
			tmpLogMap.put("solution", "ECM");
			tmpLogMap.put("process_user_id", excelUploadOdChg.getUpdateUserId());
			tmpLogMap.put("log_job_type", "DATA");
			tmpLogMap.put("job_func_type", "EXCEL");
			tmpLogMap.put("result", "ok");
			tmpLogMap.put("process_date_time", System.currentTimeMillis());
			tmpLogMap.put("ent_business_print_name", excelUploadOdChg.getLogUserBusinessPrintName());
			tmpLogMap.put("process_user_name", excelUploadOdChg.getLogUserName());
			tmpLogMap.put("process_user_name_eng", excelUploadOdChg.getLogUserNameEng());
			if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
			
			for(Map<String, Object> excelOdChg : excelUploadOdChg.getDataList()){
				Map<String, Object> selectOdJobNo = new HashMap<String, Object>();
				selectOdJobNo.put("entCode", excelUploadOdChg.getEntCode());
				selectOdJobNo.put("hblNo", excelOdChg.get("HBL_NO"));
				selectOdJobNo.put("bound", excelUploadOdChg.getBound());
				selectOdJobNo.put("boundType", "IMP");
				odJobNo = orderBatchMapper.selectOdJobNoByHblNo(selectOdJobNo);
				excelOdChg.put("odJobNo", Util.null2str(odJobNo.get("OD_JOB_NO")));
				excelOdChg.put("seqNoId" , orderBatchMapper.selectMaxCsSeqNo(excelOdChg) + 1);
			}
			
			cnt = 1;
			for(Map<String, Object> excelOdChg : excelUploadOdChg.getDataList()){
				excelOdChg.put("odJobNo", Util.null2str(excelOdChg.get("odJobNo")));
				excelOdChg.put("bondedTransDate", Util.null2str(excelOdChg.get("BONDED_TRANS_DATE")));
				excelOdChg.put("bondedTransType", Util.null2str(excelOdChg.get("BONDED_TRANS_TYPE")));
				excelOdChg.put("inspectionDrop", Util.null2str(excelOdChg.get("INSPECTION_DROP")));
				excelOdChg.put("transportDate", Util.null2str(excelOdChg.get("TRANSPORT_DATE")));
				excelOdChg.put("carryoutDate", Util.null2str(excelOdChg.get("CARRYOUT_DATE")));
				excelOdChg.put("carryoutRemark", Util.null2str(excelOdChg.get("CARRYOUT_REMARK")));
				excelOdChg.put("inputMethod", "EXCEL");
				excelOdChg.put("updateUserId", excelUploadOdChg.getUpdateUserId());
				excelOdChg.put("gmtTime", excelUploadOdChg.getGmtTime());
				orderBatchMapper.insertBondedTrans(excelOdChg);
				
				// CS_HISTORY  insert
				Map<String, Object> historyMap = new HashMap<>();
				String bondedTransDate = Util.null2str(excelOdChg.get("bondedTransDate"));
				String formattedDate = bondedTransDate.substring(0, 4) + "-" + bondedTransDate.substring(4, 6) + "-" + bondedTransDate.substring(6, 8);
				String csRemark = "보세운송신고일 : " + formattedDate + " | 보세운송유형 : " 
				+	Util.null2str(excelOdChg.get("bondedTransType")) + " | 담당자 : " + excelUploadOdChg.getLogUserName();
				historyMap.put("gmtTime", excelUploadOdChg.getGmtTime());
				historyMap.put("updateUserId", excelUploadOdChg.getUpdateUserId());
				historyMap.put("odJobNo", excelOdChg.get("odJobNo"));
				historyMap.put("seqNoId", excelOdChg.get("seqNoId"));
				historyMap.put("csType", "보세운송");
				historyMap.put("csRemark", csRemark);
				orderBatchMapper.insertOdCsHistory(historyMap);
				
				// 처리 로그 기록
				bulkData = new BulkData();
				bulkData.setActionType(BulkData.Type.CREATE);
				bulkData.setIndexName(index);
				bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				
				logMap = new HashMap<String, Object>();
				logMap.putAll(tmpLogMap);
				logMap.put("bound", excelUploadOdChg.getBound());
				logMap.put("bound_type", "IMP");
				logMap.put("reference_no_id", bulkData.getId());
				logMap.put("job_no", "");
				logMap.put("job_log", "");
				logMap.put("description", "엑셀 보세운송관리 [HBL NO : " + excelOdChg.get("HBL_NO") + "]");
				bulkData.setJsonMap(logMap);
				bulkList.add(bulkData);
				
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
			
			// 처리 로그 기록
			commonService.insertLogBulk(bulkList);
			
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
			
		return orderSaveResult;
		
	}
	
	/**
	 * 고객서비스 보세운송 - delete
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public BondedTransSaveResult deleteBondedTransList(SearchBondedTransList searchBondedTransList, HttpServletRequest request) throws Exception {
		BondedTransSaveResult bondedTransSaveResult = new BondedTransSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderCommonCsMapper orderCommonCsBatchMapper = sqlSession.getMapper(OrderCommonCsMapper.class);
			
			List<BondedTransDelete> bondedTransDeleteList = searchBondedTransList.getBondedTransDeleteList();
			
			BulkData bulkData = null;
			String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
			Map<String, Object> logMap = null;
			Map<String, Object> tmpLogMap = new HashMap<String, Object>();
			tmpLogMap.put("ent_code", searchBondedTransList.getSrchEntCode());
			tmpLogMap.put("ent_business_code", "");
			tmpLogMap.put("solution", "ECM");
			tmpLogMap.put("bound", "SEA");
			tmpLogMap.put("bound_type", "IMP");
			tmpLogMap.put("process_user_id", searchBondedTransList.getUserId());
			tmpLogMap.put("log_job_type", "DATA");
			tmpLogMap.put("job_func_type", "DELETE");
			tmpLogMap.put("result", "ok");
			tmpLogMap.put("process_date_time", System.currentTimeMillis());
			
			tmpLogMap.put("process_user_name", searchBondedTransList.getLogUserName());
			tmpLogMap.put("process_user_name_eng", searchBondedTransList.getLogUserNameEng());
			if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
			
			int cnt = 1;
			int seq = 1;
			for(BondedTransDelete bondedTransDelete : bondedTransDeleteList) {
				orderCommonCsBatchMapper.deleteBondedTrans(bondedTransDelete);
				
				bulkData = new BulkData();
				bulkData.setActionType(BulkData.Type.CREATE);
				bulkData.setIndexName(index);
				bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				logMap = new HashMap<String, Object>();
				logMap.putAll(tmpLogMap);
				logMap.put("reference_no_id", bulkData.getId());
				logMap.put("job_no", bondedTransDelete.getOdJobNo());
				logMap.put("description", "BONDED TRANS DELETE [HBL_NO : "+bondedTransDelete.getHblNo() +"]");
				bulkData.setJsonMap(logMap);
				bulkList.add(bulkData);
				cnt++;
				if(cnt > 1000){
					sqlSession.flushStatements();
					sqlSession.clearCache();
					cnt = 1;
				}
			}
			
			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
			
			bondedTransSaveResult.setResultCnt(seq-1);
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
		
		commonService.insertLogBulk(bulkList);
		
		return bondedTransSaveResult;
	}
	
	/**
	 * 고객서비스 송장호출(주소변경) 리스트
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ChangeDeliveryList selectChangeDeliveryList(SearchChangeDeliveryList search) throws Exception {
		ChangeDeliveryList changeDeliveryList = new ChangeDeliveryList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/** N건 검색 */
		List<String> srchblNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			srchblNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}
		
		if(srchblNoList.size() > 1) {
			search.setSearchType("multi");
			search.setSrchBlNoList(srchblNoList);
		}else {
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",",""));
		}
		Util.accessControlDatabase(search, "srchCusType|srchDateFormat", true, "srchDateGbn", "od.AR_DATE|od.OB_DATE|dlv.REGISTER_DATE_TIME", null, null);
		List<ChangeDelivery> list  = orderCommonCsMapper.selectChangeDeliveryList(search);
		changeDeliveryList.setChangeDeliveryList(list);;
		if(list.size() > 0) {
			changeDeliveryList.setTotal(list.get(0).getTotal());
		}else {
			changeDeliveryList.setTotal(0);
		}
		return changeDeliveryList;
	}
	
	/**
	 * 고객서비스 송장교체(주소변경) Detail
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ChangeDelivery selectChangeDeliveryDetail(SearchChangeDelivery search) throws Exception {
		ChangeDelivery order  = orderCommonCsMapper.selectChangeDeliveryDetail(search);
		return order;
	}
	
	/**
	 * 고객서비스 송장교체(주소변경) - 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ChangeDeliverySaveResult saveChangeDelivery(ChangeDelivery changeDelivery, HttpServletRequest request) throws Exception {
		ChangeDeliverySaveResult changeDeliverySaveResult = new ChangeDeliverySaveResult();
		if(changeDelivery == null ||  "".equals(Util.null2str(changeDelivery.getOdJobNo()))) {
			return null;
		}
		
		if("".equals(Util.null2str(changeDelivery.getSeqNoId()))) {
			Map<String, Object> multiStockMap = new HashMap<String, Object>();
			multiStockMap.put("gmtTime", Util.null2str(changeDelivery.getGmtTime()));
			multiStockMap.put("updateUserId", Util.null2str(changeDelivery.getRegisterUserId()));
			multiStockMap.put("hblNo", Util.null2str(changeDelivery.getHblNo()));
			multiStockMap.put("deliveryCode", Util.null2str(changeDelivery.getDeliveryCodeChanged()));
			multiStockMap.put("stockMutliNo", Util.null2str(changeDelivery.getDeliveryNoChanged()));
			multiStockMap.put("odJobNo", Util.null2str(changeDelivery.getOdJobNo()));
			multiStockMap.put("seqNoId", "1");
			orderCommonCsMapper.insertMultiDelivery(multiStockMap);
			changeDelivery.setSeqNoId("1");
		}
		
		Util.accessControlDatabase(changeDelivery, "gmtTime", false, null, null, null, null);
		Map<String, Object> changeDeliveryMap = new HashMap<String, Object>();
		changeDeliveryMap.put("odJobNo",Util.null2str(changeDelivery.getOdJobNo()));
		changeDeliveryMap.put("odJobNoSeqNoId",Util.null2str(changeDelivery.getSeqNoId()));
		changeDeliveryMap.put("deliveryNameChanged",Util.null2str(changeDelivery.getDeliveryNameChanged()));
		changeDeliveryMap.put("deliveryZipcodeChanged",Util.null2str(changeDelivery.getDeliveryZipcodeChanged()));
		changeDeliveryMap.put("deliveryAddressChanged",Util.null2str(changeDelivery.getDeliveryAddressChanged()));
		changeDeliveryMap.put("deliveryTelChanged",Util.null2str(changeDelivery.getDeliveryTelChanged()));
		changeDeliveryMap.put("deliveryCodeChanged",Util.null2str(changeDelivery.getDeliveryCodeChanged()));
		changeDeliveryMap.put("deliveryNoChanged",Util.null2str(changeDelivery.getDeliveryNoChanged()));
		changeDeliveryMap.put("deliveryChangedDate",Util.null2str(changeDelivery.getDeliveryChangedDate()));
		changeDeliveryMap.put("inputMethod","Manual");
		changeDeliveryMap.put("gmtTime",Util.null2str(changeDelivery.getGmtTime()));
		changeDeliveryMap.put("updateUserId",Util.null2str(changeDelivery.getRegisterUserId()));
		orderCommonCsMapper.insertChangeDelivery(changeDeliveryMap);
		//기존 오더 업데이트 처리
		orderCommonCsMapper.updateOrigMultiOrder(changeDeliveryMap);
		
		//CS Histroy Insert
		String csRemark  = "변경배송사 : " + Util.null2str(changeDelivery.getDeliveryCodeChanged()) + " | 변경송장번호 : " 
				+	Util.null2str(changeDelivery.getDeliveryNoChanged()) + " | 교체일 : " + Util.null2str(changeDelivery.getLogUserName()) + " | 등록담당자 : " 
				+ Util.null2str(changeDelivery.getLogUserName()) + "\n 수하인 / 전화번호 : " + Util.null2str(changeDelivery.getDeliveryNameChanged()) + " | " + Util.null2str(changeDelivery.getDeliveryTelChanged())
				+ "\n 우편번호 / 수하인주소 : " + Util.null2str(changeDelivery.getDeliveryZipcodeChanged()) + " | " +  Util.null2str(changeDelivery.getDeliveryAddressChanged());
		changeDeliveryMap.put("csRemark", csRemark);
		changeDeliveryMap.put("csType", "송장교체");
		changeDeliveryMap.put("seqNoId" , orderCommonCsMapper.selectMaxCsSeqNo(changeDeliveryMap) + 1);
		orderCommonCsMapper.insertOdCsHistory(changeDeliveryMap);
		
		changeDeliverySaveResult.setOdJobNo(Util.null2str(changeDelivery.getOdJobNo()));
		return changeDeliverySaveResult;
		
		
	}
	
	/**
	 * 고객서비스 송장교체(주소변경) - Confirm
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ChangeDeliverySaveResult confirmDelivery(ChangeDelivery changeDelivery, HttpServletRequest request) throws Exception {
		
		
		ChangeDeliverySaveResult changeDeliverySaveResult = new ChangeDeliverySaveResult();
		if(changeDelivery == null ||  changeDelivery.getDataList().size() < 1 ) {
			return null;
		}
		Util.accessControlDatabase(changeDelivery, "gmtTime", false, null, null, null, null);
		orderCommonCsMapper.confirmDelivery(changeDelivery);
		
		changeDeliverySaveResult.setResultCnt(changeDelivery.getDataList().size());
		return changeDeliverySaveResult;
		
		
	}
	
	/**
	 * 엑셀업로드 고객서비스 송장교체
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult excelUploadInsertChangeDelivery(ExcelUploadOdChg excelUploadOdChg, String excelUploadType, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		Map<String, Object> odJobNo = new HashMap<String, Object>();
		
		if(excelUploadOdChg.getDataList() == null) {
			return orderSaveResult;
		}
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Util.accessControlDatabase(excelUploadOdChg, "gmtTime", false, null, null, null, null);
		SqlSession sqlSession = null;
		int cnt = 1;
		int seq = 1;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderCommonCsMapper orderBatchMapper = sqlSession.getMapper(OrderCommonCsMapper.class);
			Map<String, Object> tmpLogMap = new HashMap<String, Object>();
			tmpLogMap.put("ent_code", excelUploadOdChg.getEntCode());
			tmpLogMap.put("ent_business_code", excelUploadOdChg.getEntBusinessCode());
			tmpLogMap.put("solution", "ECM");
			tmpLogMap.put("process_user_id", excelUploadOdChg.getUpdateUserId());
			tmpLogMap.put("log_job_type", "DATA");
			tmpLogMap.put("job_func_type", "EXCEL");
			tmpLogMap.put("result", "ok");
			tmpLogMap.put("process_date_time", System.currentTimeMillis());
			tmpLogMap.put("ent_business_print_name", excelUploadOdChg.getLogUserBusinessPrintName());
			tmpLogMap.put("process_user_name", excelUploadOdChg.getLogUserName());
			tmpLogMap.put("process_user_name_eng", excelUploadOdChg.getLogUserNameEng());
			if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
			
			for(Map<String, Object> excelOdChg : excelUploadOdChg.getDataList()){
				Map<String, Object> selectOdJobNo = new HashMap<String, Object>();
				selectOdJobNo.put("entCode", excelUploadOdChg.getEntCode());
				selectOdJobNo.put("hblNo", excelOdChg.get("HBL_NO"));
				selectOdJobNo.put("bound", excelUploadOdChg.getBound());
				selectOdJobNo.put("boundType", "IMP");
				odJobNo = orderBatchMapper.selectMultiOdJobNoByBlNo(selectOdJobNo);
				excelOdChg.put("odJobNo", Util.null2str(odJobNo.get("OD_JOB_NO")));
				if(!"".equals(Util.null2str(odJobNo.get("SEQ_NO_ID")))) {
					excelOdChg.put("odJobNoSeqNoId", Util.null2str(odJobNo.get("SEQ_NO_ID")));
				}else {
					Map<String, Object> multiStockMap = new HashMap<String, Object>();
					multiStockMap.put("gmtTime", Util.null2str(excelUploadOdChg.getGmtTime()));
					multiStockMap.put("updateUserId", Util.null2str(excelUploadOdChg.getUpdateUserId()));
					multiStockMap.put("hblNo", Util.null2str(excelOdChg.get("HBL_NO")));
					multiStockMap.put("deliveryCode", Util.null2str(excelOdChg.get("DELIVERY_CODE_CHANGED")));
					multiStockMap.put("stockMutliNo", Util.null2str(excelOdChg.get("DELIVERY_NO_CHANGED")));
					multiStockMap.put("odJobNo", Util.null2str(excelOdChg.get("odJobNo")));
					multiStockMap.put("seqNoId", "1");
					orderCommonCsMapper.insertMultiDelivery(multiStockMap);
					excelOdChg.put("odJobNoSeqNoId", "1");
				}
				excelOdChg.put("seqNoId" , orderBatchMapper.selectMaxCsSeqNo(excelOdChg) + 1);
			}
			
			cnt = 1;
			for(Map<String, Object> excelOdChg : excelUploadOdChg.getDataList()){
				excelOdChg.put("odJobNo", Util.null2str(excelOdChg.get("odJobNo")));
				excelOdChg.put("requestDate", Util.null2str(excelOdChg.get("REQUEST_DATE")));
				excelOdChg.put("deliveryNameChanged", Util.null2str(excelOdChg.get("DELIVERY_NAME_CHANGED")));
				excelOdChg.put("deliveryZipcodeChanged", Util.null2str(excelOdChg.get("DELIVERY_ZIPCODE_CHANGED")));
				excelOdChg.put("deliveryAddressChanged", Util.null2str(excelOdChg.get("DELIVERY_ADDRESS_CHANGED")));
				excelOdChg.put("deliveryTelChanged", Util.null2str(excelOdChg.get("DELIVERY_TEL_CHANGED")));
				excelOdChg.put("deliveryCodeChanged", Util.null2str(excelOdChg.get("DELIVERY_CODE_CHANGED")));
				excelOdChg.put("deliveryNoChanged", Util.null2str(excelOdChg.get("DELIVERY_NO_CHANGED")));
				excelOdChg.put("deliveryChangedDate", Util.null2str(excelOdChg.get("DELIVERY_CHANGED_DATE")));
				excelOdChg.put("inputMethod", "EXCEL");
				excelOdChg.put("updateUserId", excelUploadOdChg.getUpdateUserId());
				excelOdChg.put("gmtTime", excelUploadOdChg.getGmtTime());
				orderBatchMapper.insertChangeDelivery(excelOdChg);
				
				//기존 오더 업데이트 처리
				orderBatchMapper.updateOrigMultiOrder(excelOdChg);
				
				// CS_HISTORY  insert
				Map<String, Object> historyMap = new HashMap<>();
				String deliveryChangedDate = Util.null2str(excelOdChg.get("deliveryChangedDate"));
				String formattedDate = deliveryChangedDate.substring(0, 4) + "-" + deliveryChangedDate.substring(4, 6) + "-" + deliveryChangedDate.substring(6, 8);
				String csRemark  = "변경배송사 : " + Util.null2str(excelOdChg.get("deliveryCodeChanged")) + " | 변경송장번호 : " 
						+	Util.null2str(excelOdChg.get("deliveryNoChanged")) + " | 교체일 : " + formattedDate + " | 등록담당자 : " 
						+ Util.null2str(excelUploadOdChg.getLogUserName()) + "\n 수하인 / 전화번호 : " + Util.null2str(excelOdChg.get("deliveryNameChanged")) + " | " + Util.null2str(excelOdChg.get("deliveryTelChanged"))
						+ "\n 우편번호 / 수하인주소 : " + Util.null2str(excelOdChg.get("deliveryZipcodeChanged")) + " | " +  Util.null2str(excelOdChg.get("deliveryAddressChanged"));
				
				historyMap.put("gmtTime", excelUploadOdChg.getGmtTime());
				historyMap.put("updateUserId", excelUploadOdChg.getUpdateUserId());
				historyMap.put("odJobNo", excelOdChg.get("odJobNo"));
				historyMap.put("seqNoId", excelOdChg.get("seqNoId"));
				historyMap.put("odJobNoSeqNoId", excelOdChg.get("odJobNoSeqNoId"));
				historyMap.put("csType", "송장교체");
				historyMap.put("csRemark", csRemark);
				orderBatchMapper.insertOdCsHistory(historyMap);
				
				// 처리 로그 기록
				bulkData = new BulkData();
				bulkData.setActionType(BulkData.Type.CREATE);
				bulkData.setIndexName(index);
				bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				
				logMap = new HashMap<String, Object>();
				logMap.putAll(tmpLogMap);
				logMap.put("bound", excelUploadOdChg.getBound());
				logMap.put("bound_type", "IMP");
				logMap.put("reference_no_id", bulkData.getId());
				logMap.put("job_no", "");
				logMap.put("job_log", "");
				logMap.put("description", "엑셀 송장교체(주소변경)관리 [HBL NO : " + excelOdChg.get("HBL_NO") + "]");
				bulkData.setJsonMap(logMap);
				bulkList.add(bulkData);
				
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
			
			// 처리 로그 기록
			commonService.insertLogBulk(bulkList);
			
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
			
		return orderSaveResult;
		
	}
	
	/**
	 * 고객서비스 퀵요청 리스트
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public QuickRequestList selectQuickRequestList(SearchQuickRequestList search) throws Exception {
		QuickRequestList quickRequestList = new QuickRequestList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/** N건 검색 */
		List<String> srchblNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			srchblNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}
		
		if(srchblNoList.size() > 1) {
			search.setSearchType("multi");
			search.setSrchBlNoList(srchblNoList);
		}else {
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",",""));
		}
		Util.accessControlDatabase(search, "srchCusType|srchDateFormat", true, "srchDateGbn", "od.AR_DATE|od.OB_DATE|quick.REGISTER_DATE_TIME", null, null);
		List<QuickRequest> list  = orderCommonCsMapper.selectQuickRequestList(search);
		quickRequestList.setQuickRequestList(list);;
		if(list.size() > 0) {
			quickRequestList.setTotal(list.get(0).getTotal());
		}else {
			quickRequestList.setTotal(0);
		}
		return quickRequestList;
	}
	
	/**
	 * 고객서비스 퀵요청 Detail
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public QuickRequest selectQuickRequestDetail(SearchQuickRequest search) throws Exception {
		QuickRequest order  = orderCommonCsMapper.selectQuickRequestDetail(search);
		return order;
	}
	
	/**
	 * 고객서비스 퀵요청 - 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public QuickRequestSaveResult saveQuickRequest(QuickRequest quickRequest, HttpServletRequest request) throws Exception {
		QuickRequestSaveResult quickRequestSaveResult = new QuickRequestSaveResult();
		if(quickRequest == null || "".equals(Util.null2str(quickRequest.getOdJobNo()))) {
			return null;
		}
		
		Util.accessControlDatabase(quickRequest, "gmtTime", false, null, null, null, null);
		Map<String, Object> quickRequestMap = new HashMap<String, Object>();
		quickRequestMap.put("odJobNo",Util.null2str(quickRequest.getOdJobNo()));
		quickRequestMap.put("odJobNoSeqNoId",Util.null2str(quickRequest.getSeqNoId()));
		quickRequestMap.put("takeOverDate",Util.null2str(quickRequest.getTakeOverDate()));
		quickRequestMap.put("driverName",Util.null2str(quickRequest.getDriverName()));
		quickRequestMap.put("incoterms",Util.null2str(quickRequest.getIncoterms()));
		quickRequestMap.put("quickFee",Util.null2str(quickRequest.getQuickFee()));
		quickRequestMap.put("remark",Util.null2str(quickRequest.getRemark()));
		quickRequestMap.put("inputMethod","Manual");
		quickRequestMap.put("gmtTime",Util.null2str(quickRequest.getGmtTime()));
		quickRequestMap.put("updateUserId",Util.null2str(quickRequest.getRegisterUserId()));
		orderCommonCsMapper.insertQuickRequest(quickRequestMap);
		
		//CS Histroy Insert
		String csRemark  = "퀵인계일자 : " + Util.null2str(quickRequest.getTakeOverDate()) + " | 배송기사 : " 
				+	Util.null2str(quickRequest.getDriverName()) + " | 결제조건 : " + Util.null2str(quickRequest.getIncoterms()) + " | 결제운임 : " 
				+ Util.null2str(quickRequest.getQuickFee())+ " | 비고 : " + Util.null2str(quickRequest.getRemark()) ;
		quickRequestMap.put("csRemark", csRemark);
		quickRequestMap.put("csType", "퀵요청");
		quickRequestMap.put("seqNoId" , orderCommonCsMapper.selectMaxCsSeqNo(quickRequestMap) + 1);
		orderCommonCsMapper.insertOdCsHistory(quickRequestMap);
		
		quickRequestSaveResult.setOdJobNo(Util.null2str(quickRequest.getOdJobNo()));
		return quickRequestSaveResult;
	}
	
	/**
	 * 고객서비스 퀵요청 - 삭제
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public QuickRequestSaveResult deleteQuickRequest(QuickRequestDeleteList quickRequestDeleteList, HttpServletRequest request) throws Exception {
		QuickRequestSaveResult quickRequestSaveResult = new QuickRequestSaveResult();
		if(quickRequestDeleteList.getDataList() == null || quickRequestDeleteList.getDataList().size() < 1) {
			quickRequestSaveResult.setResultCode("A11337");
			return quickRequestSaveResult;
		}
		
		orderCommonCsMapper.deleteQuickRequest(quickRequestDeleteList);
		
		for(Map<String, Object> returnMng : quickRequestDeleteList.getDataList()) {
			//히스토리 등록
			Map<String, Object> historyMap = new HashMap<String, Object>();
			historyMap.put("odJobNo", returnMng.get("odJobNo"));
			historyMap.put("odJobNoSeqNoId", returnMng.get("seqNoId"));
			historyMap.put("seqNoId", orderCommonCsMapper.selectMaxCsSeqNo(historyMap) + 1);
			historyMap.put("gmtTime", quickRequestDeleteList.getGmtTime());
			historyMap.put("updateUserId", quickRequestDeleteList.getUpdateUserId());
			historyMap.put("csType", "퀵요청");
			historyMap.put("csRemark", "퀵요청 삭제");
			orderCommonCsMapper.insertOdCsHistory(historyMap);
		}
		
		return quickRequestSaveResult;
	}
	
	/**
	 * 엑셀업로드 고객서비스 퀵요청
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult excelUploadInsertQuickRequest(ExcelUploadOdChg excelUploadOdChg, String excelUploadType, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		Map<String, Object> odJobNo = new HashMap<String, Object>();
		
		if(excelUploadOdChg.getDataList() == null) {
			return orderSaveResult;
		}
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Util.accessControlDatabase(excelUploadOdChg, "gmtTime", false, null, null, null, null);
		SqlSession sqlSession = null;
		int cnt = 1;
		int seq = 1;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderCommonCsMapper orderBatchMapper = sqlSession.getMapper(OrderCommonCsMapper.class);
			Map<String, Object> tmpLogMap = new HashMap<String, Object>();
			tmpLogMap.put("ent_code", excelUploadOdChg.getEntCode());
			tmpLogMap.put("ent_business_code", excelUploadOdChg.getEntBusinessCode());
			tmpLogMap.put("solution", "ECM");
			tmpLogMap.put("process_user_id", excelUploadOdChg.getUpdateUserId());
			tmpLogMap.put("log_job_type", "DATA");
			tmpLogMap.put("job_func_type", "EXCEL");
			tmpLogMap.put("result", "ok");
			tmpLogMap.put("process_date_time", System.currentTimeMillis());
			tmpLogMap.put("ent_business_print_name", excelUploadOdChg.getLogUserBusinessPrintName());
			tmpLogMap.put("process_user_name", excelUploadOdChg.getLogUserName());
			tmpLogMap.put("process_user_name_eng", excelUploadOdChg.getLogUserNameEng());
			if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
			
			for(Map<String, Object> excelOdChg : excelUploadOdChg.getDataList()){
				Map<String, Object> selectOdJobNo = new HashMap<String, Object>();
				selectOdJobNo.put("entCode", excelUploadOdChg.getEntCode());
				selectOdJobNo.put("hblNo", excelOdChg.get("HBL_NO"));
				selectOdJobNo.put("bound", excelUploadOdChg.getBound());
				selectOdJobNo.put("boundType", "IMP");
				odJobNo = orderBatchMapper.selectMultiOdJobNoByBlNo(selectOdJobNo);
				excelOdChg.put("odJobNo", Util.null2str(odJobNo.get("OD_JOB_NO")));
				if(!"".equals(Util.null2str(odJobNo.get("SEQ_NO_ID")))) {
					excelOdChg.put("odJobNoSeqNoId", Util.null2str(odJobNo.get("SEQ_NO_ID")));
				}
				excelOdChg.put("seqNoId" , orderBatchMapper.selectMaxCsSeqNo(excelOdChg) + 1);
			}
			
			cnt = 1;
			for(Map<String, Object> excelOdChg : excelUploadOdChg.getDataList()){
				excelOdChg.put("odJobNo", Util.null2str(excelOdChg.get("odJobNo")));
				excelOdChg.put("requestDate", Util.null2str(excelOdChg.get("REQUEST_DATE")));
				excelOdChg.put("incoterms", Util.null2str(excelOdChg.get("INCOTERMS")));
				excelOdChg.put("quickFee", Util.null2str(excelOdChg.get("QUICK_FEE")));
				excelOdChg.put("remark", Util.null2str(excelOdChg.get("REMARK")));
				excelOdChg.put("takeOverDate", Util.null2str(excelOdChg.get("TAKE_OVER_DATE")));
				excelOdChg.put("driverName", Util.null2str(excelOdChg.get("DRIVER_NAME")));
				excelOdChg.put("inputMethod", "EXCEL");
				excelOdChg.put("updateUserId", excelUploadOdChg.getUpdateUserId());
				excelOdChg.put("gmtTime", excelUploadOdChg.getGmtTime());
				orderBatchMapper.insertQuickRequest(excelOdChg);
				
				// CS_HISTORY  insert
				Map<String, Object> historyMap = new HashMap<>();
				String takeOverDate = Util.null2str(excelOdChg.get("takeOverDate"));
				String formattedDate = takeOverDate.substring(0, 4) + "-" + takeOverDate.substring(4, 6) + "-" + takeOverDate.substring(6, 8);
				
				String csRemark  = "퀵인계일자 : " + formattedDate + " | 배송기사 : " 
						+	Util.null2str(excelOdChg.get("driverName")) + " | 결제조건 : " + Util.null2str(excelOdChg.get("incoterms")) + " | 결제운임 : " 
						+ Util.null2str(excelOdChg.get("quickFee")) + " | 비고 : " + Util.null2str(excelOdChg.get("remark"));
				
				historyMap.put("gmtTime", excelUploadOdChg.getGmtTime());
				historyMap.put("updateUserId", excelUploadOdChg.getUpdateUserId());
				historyMap.put("odJobNo", excelOdChg.get("odJobNo"));
				historyMap.put("seqNoId", excelOdChg.get("seqNoId"));
				historyMap.put("odJobNoSeqNoId", excelOdChg.get("odJobNoSeqNoId"));
				historyMap.put("csType", "퀵요청");
				historyMap.put("csRemark", csRemark);
				orderBatchMapper.insertOdCsHistory(historyMap);
				
				// 처리 로그 기록
				bulkData = new BulkData();
				bulkData.setActionType(BulkData.Type.CREATE);
				bulkData.setIndexName(index);
				bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				
				logMap = new HashMap<String, Object>();
				logMap.putAll(tmpLogMap);
				logMap.put("bound", excelUploadOdChg.getBound());
				logMap.put("bound_type", "IMP");
				logMap.put("reference_no_id", bulkData.getId());
				logMap.put("job_no", "");
				logMap.put("job_log", "");
				logMap.put("description", "엑셀 퀵요청 [HBL NO : " + excelOdChg.get("HBL_NO") + "]");
				bulkData.setJsonMap(logMap);
				bulkList.add(bulkData);
				
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
			
			// 처리 로그 기록
			commonService.insertLogBulk(bulkList);
			
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
			
		return orderSaveResult;
		
	}
	
	/**
	 * 고객서비스 반품관리 리스트
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ReturnMngList selectReturnMngList(SearchReturnMngList search) throws Exception {
		ReturnMngList returnMngList = new ReturnMngList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/** N건 검색 */
		List<String> srchblNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			srchblNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}
		
		if(srchblNoList.size() > 1) {
			search.setSearchType("multi");
			search.setSrchBlNoList(srchblNoList);
		}else {
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",",""));
		}
		Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "od.AR_DATE|od.OB_DATE|rsn.REGISTER_DATE_TIME", null, null);
		List<ReturnMng> list  = orderCommonCsMapper.selectReturnMngList(search);
		returnMngList.setReturnMngList(list);;
		if(list.size() > 0) {
			returnMngList.setTotal(list.get(0).getTotal());
		}else {
			returnMngList.setTotal(0);
		}
		return returnMngList;
	}
	
	/**
	 * 고객서비스 반품관리 - Function
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ReturnMngSaveResult saveReturnMngFunction(SearchReturnMngList returnMngList, HttpServletRequest request) throws Exception {
		ReturnMngSaveResult returnMngSaveResult = new ReturnMngSaveResult();
		String code = Util.null2str(returnMngList.getProcessCode());
		if(returnMngList.getDataList() == null || returnMngList.getDataList().size() < 1) {
			return null;
		}
		orderCommonCsMapper.saveReturnMngFunction(returnMngList);
		
		return returnMngSaveResult;
	}
	
	/**
	 * 고객서비스 반품관리 - 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ReturnMngSaveResult saveReturnMng(ReturnMng returnMng, HttpServletRequest request) throws Exception {
		ReturnMngSaveResult returnMngSaveResult = new ReturnMngSaveResult();
		if(returnMng == null) {
			return null;
		}
		
		Util.accessControlDatabase(returnMng, "gmtTime", false, null, null, null, null);
		int cnt = orderCommonCsMapper.selectReturnBl(returnMng);  
		if(cnt > 0) {
			returnMngSaveResult.setResultCode("A11253");
			return returnMngSaveResult;
		}else {
			if("".equals(Util.null2str(returnMng.getSeqNoId()))) {
				Map<String, Object> multi = new HashMap<>();
				multi.put("odJobNo", Util.null2str(returnMng.getOdJobNo()));
				multi.put("seqNoId", "1");
				multi.put("hblNo", Util.null2str(returnMng.getHblNo()));
				multi.put("gmtTime", Util.null2str(returnMng.getGmtTime()));
				multi.put("updateUserId", Util.null2str(returnMng.getRegisterUserId()));
				orderCommonCsMapper.insertMultiDelivery(multi);
				returnMng.setSeqNoId("1");
			}
			orderCommonCsMapper.saveReturnMng(returnMng);
		}
		//CS Histroy Insert
//			String csRemark  = "퀵인계일자 : " + Util.null2str(quickRequest.getTakeOverDate()) + " | 배송기사 : " 
//					+	Util.null2str(quickRequest.getDriverName()) + " | 결제조건 : " + Util.null2str(quickRequest.getIncoterms()) + " | 결제운임 : " 
//					+ Util.null2str(quickRequest.getQuickFee())+ " | 비고 : " + Util.null2str(quickRequest.getRemark()) ;
//			quickRequestMap.put("csRemark", csRemark);
//			quickRequestMap.put("csType", "퀵요청");
//			quickRequestMap.put("seqNoId" , orderCommonCsMapper.selectMaxCsSeqNo(quickRequestMap) + 1);
//			orderCommonCsMapper.insertOdCsHistory(quickRequestMap);
		
		returnMngSaveResult.setOdJobNo(Util.null2str(returnMng.getOdJobNo()));
		
		return returnMngSaveResult;
	}
	
	/**
	 * 고객서비스 반품관리 - 삭제
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ReturnMngSaveResult deleteReturnMng(ReturnMngDeleteList returnMngDeleteList, HttpServletRequest request) throws Exception {
		ReturnMngSaveResult returnMngSaveResult = new ReturnMngSaveResult();
		if(returnMngDeleteList.getDataList() == null || returnMngDeleteList.getDataList().size() < 1) {
			return null;
		}
		
		orderCommonCsMapper.deleteReturnMng(returnMngDeleteList);
		
		for(Map<String, Object> returnMng : returnMngDeleteList.getDataList()) {
			//히스토리 등록
			Map<String, Object> historyMap = new HashMap<String, Object>();
			historyMap.put("odJobNo", returnMng.get("odJobNo"));
			historyMap.put("odJobNoSeqNoId", returnMng.get("seqNoId"));
			historyMap.put("seqNoId", orderCommonCsMapper.selectMaxCsSeqNo(historyMap) + 1);
			historyMap.put("gmtTime", returnMngDeleteList.getGmtTime());
			historyMap.put("updateUserId", returnMngDeleteList.getUpdateUserId());
			historyMap.put("csType", "Q3");
			historyMap.put("csRemark", "반품요청 삭제");
			orderCommonCsMapper.insertOdCsHistory(historyMap);
		}
		
		return returnMngSaveResult;
	}
	
	/**
	 * 고객서비스 이례화물관리 리스트
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public UnusualMngList selectUnusualMngList(SearchUnusualMngList search) throws Exception {
		UnusualMngList unusualMngList = new UnusualMngList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/** N건 검색 */
		List<String> srchblNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))) {
			srchblNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(Util.null2str(search.getSrchNo())).split(",")));
		}
		
		if(srchblNoList.size() > 1) {
			search.setSearchType("multi");
			search.setSrchBlNoList(srchblNoList);
		}else {
			search.setSearchType("");
			search.setSrchNo(Util.null2str(search.getSrchNo()).trim().replace(",",""));
		}
		Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "od.AR_DATE|od.OB_DATE|od.PO_DATE|ad.UNUSUAL_DATE_TIME", null, null);
		List<UnusualMng> list  = orderCommonCsMapper.selectUnusualMngList(search);
		unusualMngList.setUnusualMngList(list);;
		if(list.size() > 0) {
			unusualMngList.setTotal(list.get(0).getTotal());
		}else {
			unusualMngList.setTotal(0);
		}
		return unusualMngList;
	}
	
	/**
	 * BrokerMasterSub 파일저장
	 * @param broker
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult saveBrokerFileShareUpdate(BrokerMasterSub broker, HttpServletRequest request){
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		MultipartHttpServletRequest multirequest = (MultipartHttpServletRequest) request;
	    List<MultipartFile> uploadFileList = null;
	    Iterator<String> fileNames = multirequest.getFileNames();
	    FileShare fileShare = new FileShare();
	    List<FileShareFile> fileShareList = new ArrayList<FileShareFile>();
	    
	    File uploadFile = null;
	    File deleteFile = null;
	    
	    String filePath = customProperties.getFileQnADir(); 
	    String fileName 	= null;
	    String tempFileName = null;
	    
		try {
			// 파일 업로드 START
			if (!FileUtil.isExists(filePath)) {
				FileUtil.createDirectorys(filePath);
			}
			
			// 파일 삭제 여부
			if("Y".equals(Util.null2str(broker.getFileDeleteYn()))) {
				for(FileShareFile fileTemp : broker.getFileShareFile()) {
					String fileNameRemote = Util.null2str(fileTemp.getFileNameRemote());
					deleteFile = new File(filePath + Util.null2str(fileNameRemote));
					if(deleteFile.isFile()) {
						deleteFile.delete();
					}
					
					orderCommonCsMapper.deleteFileShareFile(fileTemp);
				}
			}
			
			String referenceNoId = Util.null2str(broker.getReferenceNoId());
			System.out.println(referenceNoId);
			String fileShareYn = "N";
			int maxFileSeqNo = 0;
			if(!"".equals(referenceNoId)) {
				Map<String, Object> fileShareInfo = orderCommonCsMapper.selectFileShare(broker);
				if(fileShareInfo != null) {
					if(Util.null2int(fileShareInfo.get("FILE_SHARE_COUNT")) == 0) {
						orderCommonCsMapper.deleteFileShare(broker);
					}else {
						maxFileSeqNo = Util.null2int(fileShareInfo.get("MAX_SEQ_NO"));
						fileShareYn = "Y";
					}
				}
			}
			
			while (fileNames.hasNext()) {
				fileName 		= fileNames.next();
				uploadFileList 	= multirequest.getFiles(fileName);
				
				for (int i = 0, size = uploadFileList.size(); i < size; i++) {
					FileShareFile fileShareFile = new FileShareFile();
					// 파일 확장자 유효성 검사: 문서 + 이미지
					String extension = FilenameUtils.getExtension(uploadFileList.get(i).getOriginalFilename()).toLowerCase();
					if (!extension.matches("xls|xlsx|doc|docx|hwp|hwpx|pdf xlsx|xls|doc|docx|hwp|hwpx|pdf|jpg|jpeg|bmp|gif|png")) {
			        	orderSaveResult.setResultCode("extErrorYn");
			            return orderSaveResult;
			        }
					
					tempFileName = "QnA_" + DateTimeUtil.getNow("yyyyMMddHHmmssSSS");
					
					uploadFile = new File(filePath + tempFileName);
					uploadFileList.get(i).transferTo(uploadFile);
					String origName = Util.null2str(uploadFileList.get(i).getOriginalFilename());
					int dotIdx = origName.indexOf(".");
					
					if(i==0) {
						fileShare.setEntCode(Util.null2str(broker.getEntCode()));
						fileShare.setEntBusinessCode(Util.null2str(broker.getEntBusinessCode()));
						fileShare.setFileName(uploadFileList.get(i).getOriginalFilename());
						fileShare.setFileExt(origName.substring(dotIdx));
						fileShare.setFileSize(uploadFileList.get(i).getSize());
						fileShare.setFileNameRemote(tempFileName);
						fileShare.setFilePathRemote(filePath);
						fileShare.setGmtTime(broker.getGmtTime());
						fileShare.setRegisterUserId(Util.null2str(broker.getRegisterUserId()));
						
						if("Y".equals(fileShareYn)) fileShare.setReferenceNoId(Util.null2int(referenceNoId));
					}
					
					fileShareFile.setSeqNo(++maxFileSeqNo);
					fileShareFile.setFileDocType("OT");
					fileShareFile.setFileName(uploadFileList.get(i).getOriginalFilename());
					fileShareFile.setFileSize(uploadFileList.get(i).getSize());
					fileShareFile.setFileNameRemote(tempFileName);
					fileShareFile.setFilePathRemote(filePath);

					log.debug("saveQnA filePath : " 		+ uploadFile.getAbsolutePath());
					log.debug("saveQnA fileContentType : " 	+ uploadFileList.get(i).getContentType());
					log.debug("saveQnA fileTempName : " 	+ tempFileName);
					log.debug("saveQnA fileOriginalName : " + uploadFileList.get(i).getOriginalFilename());

					fileShareList.add(fileShareFile);
				}
			}
			
			if(fileShareList != null && fileShareList.size() > 0) {
				fileShare.setFileShareList(fileShareList);
				
				// 파일 데이터 저장
				fileShare.setFuncJobNo(broker.getOdJobNo());
				fileShare.setFuncReferenceNo(broker.getOdJobNo());
				
				if("N".equals(fileShareYn)) orderCommonCsMapper.insertFileShare(fileShare);
				else orderCommonCsMapper.insertFileShareFile(fileShare);
			}
		}catch(Exception e) {
			log.error(e.getMessage());
			orderSaveResult.setResultCode("A00001");
		}
		return orderSaveResult;
	}

	/**
	 * 엑셀업로드 고객서비스 반품관리
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult excelUploadInsertReturn(ExcelUploadOdChg excelUploadOdChg, String excelUploadType, HttpServletRequest request) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		Map<String, Object> odJobNo = new HashMap<String, Object>();
		
		if(excelUploadOdChg.getDataList() == null) {
			return orderSaveResult;
		}
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		Map<String, Object> logMap = null;
		Util.accessControlDatabase(excelUploadOdChg, "gmtTime", false, null, null, null, null);
		SqlSession sqlSession = null;
		int cnt = 1;
		int seq = 1;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			OrderCommonCsMapper orderBatchMapper = sqlSession.getMapper(OrderCommonCsMapper.class);
			Map<String, Object> tmpLogMap = new HashMap<String, Object>();
			tmpLogMap.put("ent_code", excelUploadOdChg.getEntCode());
			tmpLogMap.put("ent_business_code", excelUploadOdChg.getEntBusinessCode());
			tmpLogMap.put("solution", "ECM");
			tmpLogMap.put("process_user_id", excelUploadOdChg.getUpdateUserId());
			tmpLogMap.put("log_job_type", "DATA");
			tmpLogMap.put("job_func_type", "EXCEL");
			tmpLogMap.put("result", "ok");
			tmpLogMap.put("process_date_time", System.currentTimeMillis());
			tmpLogMap.put("ent_business_print_name", excelUploadOdChg.getLogUserBusinessPrintName());
			tmpLogMap.put("process_user_name", excelUploadOdChg.getLogUserName());
			tmpLogMap.put("process_user_name_eng", excelUploadOdChg.getLogUserNameEng());
			if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
			
			for(Map<String, Object> excelOdChg : excelUploadOdChg.getDataList()){
				Map<String, Object> selectOdJobNo = new HashMap<String, Object>();
				selectOdJobNo.put("entCode", excelUploadOdChg.getEntCode());
				selectOdJobNo.put("hblNo", excelOdChg.get("HBL_NO"));
				odJobNo = orderBatchMapper.selectMultiOdJobNoByBlNo(selectOdJobNo);
				excelOdChg.put("odJobNo", Util.null2str(odJobNo.get("OD_JOB_NO")));
				if(!"".equals(Util.null2str(odJobNo.get("SEQ_NO_ID")))) {
					excelOdChg.put("odJobNoSeqNoId", Util.null2str(odJobNo.get("SEQ_NO_ID")));
				}else {
					excelOdChg.put("odJobNoSeqNoId", "1");
				}
				excelOdChg.put("seqNoId" , orderBatchMapper.selectMaxCsSeqNo(excelOdChg) + 1);
			}
			
			cnt = 1;
			for(Map<String, Object> excelOdChg : excelUploadOdChg.getDataList()){
				excelOdChg.put("odJobNo", Util.null2str(excelOdChg.get("odJobNo")));
			
				excelOdChg.put("returnReqDate", Util.null2str(excelOdChg.get("RETURN_REQ_DATE")));
				excelOdChg.put("returnRequestId", Util.null2str(excelOdChg.get("RETURN_REQUEST_ID")));
				excelOdChg.put("returnDeliveryCode", Util.null2str(excelOdChg.get("RETURN_DELIVERY_CODE")));
				excelOdChg.put("returnDeliveryNo", Util.null2str(excelOdChg.get("RETURN_DELIVERY_NO")));
				excelOdChg.put("returnRegDate", Util.null2str(excelOdChg.get("RETURN_REG_DATE")));
				excelOdChg.put("deliveryStatus", Util.null2str(excelOdChg.get("DELIVERY_STATUS")));
				
				excelOdChg.put("inputMethod", "EXCEL");
				excelOdChg.put("updateUserId", excelUploadOdChg.getUpdateUserId());
				excelOdChg.put("gmtTime", excelUploadOdChg.getGmtTime());
				
				orderBatchMapper.insertResendRequest(excelOdChg);
				orderBatchMapper.updateMultiDelivery(excelOdChg);
				if(!"".equals(Util.null2str(excelOdChg.get("DELIVERY_STATUS")))) orderBatchMapper.updateDeliveryStatus(excelOdChg);
				
				// CS_HISTORY  insert
				Map<String, Object> historyMap = new HashMap<>();
				historyMap.put("gmtTime", excelUploadOdChg.getGmtTime());
				historyMap.put("updateUserId", excelUploadOdChg.getUpdateUserId());
				historyMap.put("odJobNo", excelOdChg.get("odJobNo"));
				historyMap.put("seqNoId", excelOdChg.get("seqNoId"));
				historyMap.put("odJobNoSeqNoId", excelOdChg.get("odJobNoSeqNoId"));
				historyMap.put("csType", "Q3");
				historyMap.put("csRemark", "엑셀 반품관리");
				orderBatchMapper.insertOdCsHistory(historyMap);
				
				// 처리 로그 기록
				bulkData = new BulkData();
				bulkData.setActionType(BulkData.Type.CREATE);
				bulkData.setIndexName(index);
				bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				
				logMap = new HashMap<String, Object>();
				logMap.putAll(tmpLogMap);
				logMap.put("bound", excelUploadOdChg.getBound());
				logMap.put("bound_type", "IMP");
				logMap.put("reference_no_id", bulkData.getId());
				logMap.put("job_no", "");
				logMap.put("job_log", "");
				logMap.put("description", "엑셀 반품관리 [HBL NO : " + excelOdChg.get("HBL_NO") + "]");
				bulkData.setJsonMap(logMap);
				bulkList.add(bulkData);
				
				if( (cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
			
			// 처리 로그 기록
			commonService.insertLogBulk(bulkList);
			
		}catch(Exception e) {
			throw e;
		}finally{
			sqlSession.close();
		}
			
		return orderSaveResult;
	}
}
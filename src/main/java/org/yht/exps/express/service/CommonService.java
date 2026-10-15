package org.yht.exps.express.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.elasticsearch.client.RestHighLevelClient;
import org.json.JSONArray;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yht.MailSmtpSender;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.yht.elasticsearch.client.api.DocumentApi;
import org.yht.elasticsearch.client.api.IndexApi;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.com.Common;
import org.yht.exps.express.com.excel.CreateExcelFile;
import org.yht.exps.express.com.excel.ExcelStyles;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.sms.EnterpriseMapper;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.SearchExcelList;
import org.yht.exps.express.model.SearchOdJobNo;
import org.yht.exps.express.model.common.AddressInfo;
import org.yht.exps.express.model.common.CustomsClearanceInfo;
import org.yht.exps.express.model.common.search.SearchAddressInfo;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.logs.ProcessLogs;
import org.yht.exps.express.model.sms.message.Message;
import org.yht.exps.express.service.edi.CodeDefinition;
import org.yht.exps.express.service.edi.EdiCommonService;
import org.yht.exps.express.service.sms.SmsUserService;
import org.yht.util.Converter;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.EncryptionUtils;
import org.yht.util.FileUtil;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubireport.viewer.report.preview.UbiViewer;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

@Slf4j
@Service
public class CommonService {
    
	@Resource(name = "commonService")
	CommonService commonService;
	@Resource(name="ediCommonService")
	EdiCommonService ediCommonService;
	
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
    @Autowired
    CommonMapper commonMapper;

    @Autowired
    OrderMapper orderMapper;
    
    @Autowired
    EnterpriseMapper enterpriseMapper;
    
    @Autowired
    SmsUserService smsUserService;
    //@Autowired
	//SqlSessionTemplate sqlSessionTemplate;
    @Autowired
	StringRedisTemplate redisTemplate;
	@Autowired
	RestHighLevelClient restClient;
	
	@Autowired
	private CustomProperties customProperties;
	
	@Value("${spring.mail.job.host}")
	private String host;
	@Value("${spring.mail.job.port}")
	private int port;
	@Value("${spring.mail.job.username}")
	private String username;
	@Value("${spring.mail.job.password}")
	private String password;
	@Value("${spring.mail.job.rawKey}")
	private String rawKey;
	@Value("${spring.profiles.active}")
	private String profile;

	
	/**
	 * BL 데이터 신규, 수정, 삭제 전 체크
	 * @param workMode 
	 * @param mf
	 * @param client
	 * @return
	 * @throws java.lang.Exception
	 */
//	public String checkPreSaveBl(String workMode, Order od, RestHighLevelClient client) throws Exception {
//		String messageId = "";
//		
//		// 마감된 경우 리턴할 메시지
//		Map<String, Object> search = new HashMap<String, Object>();
//		search.put("workMode", workMode);
//		search.put("jobType", "OBAR");
//		search.put("srchEntCode", od.getEntCode());
//		search.put("srchEntBusinessCode", od.getEntBusinessCode());
//		search.put("srchSolution", "ECM");
//		search.put("srchBound", od.getBound());
//		search.put("srchJobNo", od.getOdJobNo());
//		search.put("checkNewDate", "IMP".equals(Util.null2str(od.getBoundType())) ? od.getArDate() : od.getObDate());
//		search.put("format", Util.null2str(od.getCommon().getFormat()));
//		
//		messageId += checkClosedBl(search);
//		
//		// 중복 체크
//		if ("insert".equalsIgnoreCase(workMode)) {
//			int count = 0;
//			SearchOrder srchOrder = new SearchOrder();
//			srchOrder.setSrchEntCode(od.getEntCode());
//			srchOrder.setSrchEntBusinessCode(od.getEntBusinessCode());
//			
//			// MBL NO 체크
//			if ("M".equals(od.getBlTypeMh())) {
//				srchOrder.setSrchMblNo(od.getMblNo());
//				count = orderMapper.countOrder(srchOrder);
//		    	if (count > 0)	messageId += ",'A00155'";
//
//			// HBL NO 체크
//			} else {
//				srchOrder.setSrchHblNo(od.getHblNo());
//				count = orderMapper.countOrder(srchOrder);
//		    	if (count > 0)	messageId += ",'A00156'";
//			}
//		}
//		
//		return messageId;
//	}
	
	/**
	 * 마감여부 체크 (사용자마감, 정산마감, 관리자마감)
	 * @param search
	 * @param session
	 * @return
	 * @throws java.lang.Exception
	 */
	public String checkClosedBl(Map<String, Object> search) throws Exception {
		String workMode = Util.null2str(search.get("workMode"));
		String messageId = "";
		
		// 관리자 마감 체크
		Map<String, Object> adminClsSearch = new HashMap<String, Object>();
		adminClsSearch.put("srchEntCode", search.get("srchEntCode"));
		adminClsSearch.put("srchEntBusinessCode", search.get("srchEntBusinessCode"));
		adminClsSearch.put("srchSolution", search.get("srchSolution"));
		adminClsSearch.put("srchClosingType", "BL_ECM");
		adminClsSearch.put("srchOblJobNo", search.get("srchJobNo"));
		adminClsSearch.put("srchDate", search.get("checkNewDate"));
		adminClsSearch.put("format", search.get("format"));
		Map<String, Object> orderMap = enterpriseMapper.getBusinessClosingInfo(adminClsSearch);
		
		if (orderMap != null && 
				"Y".equals(Util.null2str(orderMap.get("ADMIN_FINISH_YN"))) 
				&& !"Y".equals(Util.null2str(orderMap.get("BL_EXCEPTION_YN")))
				&& !"Y".equals(Util.null2str(orderMap.get("DATE_EXCEPTION_YN")))) {
			//관리자 마감된 경우 메세지 세팅
			if ("insert".equalsIgnoreCase(workMode))	messageId += ",'A00219'";
			else										messageId += ",'A00028'";
			
		} else if (orderMap == null || (!"Y".equals(Util.null2str(orderMap.get("BL_EXCEPTION_YN"))) && !"Y".equals(Util.null2str(orderMap.get("DATE_EXCEPTION_YN"))))) {
			if (orderMap != null)	orderMap.clear();
			
			// jobType (매출정산:REV, 매입정산:EXP, 해외정산:DRCR, 회계:ACCT, ORDER:OBAR, B/L:OBAR)
			String jobType = Util.null2str(search.get("jobType"));
			String checkFinishYn = Util.null2str(search.get("checkFinishYn"));				// 사용자 마감 체크 여부
			String checkProfitFinishYn = Util.null2str(search.get("checkProfitFinishYn"));	// 실적 마감 체크 여부

			switch (jobType.toUpperCase()) {
				case "OBAR" : 
					if(Util.null2str(search.get("srchJobNo")).length() > 0) {
						orderMap = commonMapper.checkClosedOrderInfo(search);
						if (orderMap != null) {
							// 사용자 마감 체크
							if ("".equals(checkFinishYn) || "Y".equals(checkFinishYn)) {
								if(Util.null2str(orderMap.get("FINISH_YN")).equals("Y")) {
									//사용자 마감된 경우 메세지 세팅
									if ("insert".equalsIgnoreCase(workMode))	messageId += ",'A00219'";
									else										messageId += ",'A00027'";
								}
							} else if ("".equals(checkProfitFinishYn) || "Y".equals(checkProfitFinishYn)) { // 실적 마감 체크
								if(Util.null2str(orderMap.get("PROFIT_FINISH_YN")).equals("Y")) {
									//정산 마감된 경우 메세지 세팅
									if ("insert".equalsIgnoreCase(workMode))	messageId += ",'A00219'";
									else										messageId += ",'A10013'";
								}
							}
						}
					}
					
					break;
			}
		}

		return messageId;
	}
	
	public List<Message> selectMessage(String messageIds, String langType) throws Exception {
		List<Message> messageList = null;
		
		if(messageIds.length() > 0) {
			if(messageIds.startsWith(",")) messageIds = messageIds.replaceFirst(",", "");
			
			Map<String, Object> search = new HashMap<String, Object>();
			search.put("lang", "".equals(Util.null2str(langType)) ? "KOR" : langType);
			search.put("messageIdList", messageIds);
			
			messageList = commonMapper.selectMessage(search);
		}
		
		return messageList;
	}
	
	/**
	 * 리스트 객체 생성
	 * @param list
	 * @param Map
	 */
	public static Map<String, Object> responseMapData(List<Map<String, Object>> list) {
		
		Map<String, Object> responseMap = new HashMap<String, Object>();
		
        if(list != null && list.size() > 0)	{
        	// Total Count 세팅
        	Map<String, Object> map = list.get(0);
        	responseMap.put("total", map.get("TOTAL"));
        	responseMap.put("data", list);
        } else {
        	responseMap.put("total", 0);
        	responseMap.put("data", new ArrayList<Map<String, Object>>());
        }
        
        return responseMap;
	}
	
	public static Map<String, Object> responseMapData(Map<String, Object> map) {
		
		Map<String, Object> responseMap = new HashMap<String, Object>();
		
        if(map != null && map.size() > 0){
        	responseMap.put("data", map);
        } else {
        	responseMap.put("data", new HashMap<String, Object>());
        }
        
        return responseMap;
	}
	
	
	/**
	 * 그리드조회에 필요한 항목 추가
	 * @param list
	 * @param Map
	 */
	public static Map setGridParams(HttpServletRequest request) throws Exception {
		
		Map map = new ObjectMapper().readValue(JSONObject.fromObject(request.getParameter("params")).toString(), HashMap.class);
		int startNum = 0;
		if(map.get("pageSize") != null && map.get("page") != null) {
			startNum = Util.null2int(map.get("pageSize")) * (Util.null2int(map.get("page")) - 1);
		}
		map.put("startNum", startNum);
        return map;
	}
	
	/**
	 * 엑셀관리 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectExcelItemList(Map<String, Object> map) throws Exception {
		return commonMapper.selectExcelItemList(map);
	}
	
	/**
	 * 마스터번호 리스트 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public List selectMblNoList(Map map) throws Exception {
		return commonMapper.selectMblNoList(map);
	}
	
	public String selectEntCode(String entCode) throws Exception {
		String code = commonMapper.selectEntCode(entCode);
		return StringEscapeUtils.escapeHtml4(code);
	}
	
	/**
	 * 프로세스 처리 로그 기록
	 * @param processLogs
	 * @param request
	 * @throws Exception
	 */
	public void insertLog(ProcessLogs processLogs, HttpServletRequest request) throws Exception {
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		String id = DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + "00001";
		
		processLogs.setReferenceNoId(id);
		processLogs.setProcessDateTime(System.currentTimeMillis());
		
		// 사업장명 가져오기
//		if (!"".equals(Util.null2str(processLogs.getEntBusinessCode()))) {
//			String entBusinessPrintName = enterpriseMapper.getEntBusinessPrintName(processLogs.getEntBusinessCode());
//			processLogs.setEntBusinessPrintName(entBusinessPrintName);
//		}
		
		// 사용자명 가져오기
//		if (!"".equals(Util.null2str(processLogs.getProcessUserId()))) {
//			User user = smsUserService.getUser(processLogs.getProcessUserId());
//			processLogs.setProcessUserName(user.getUserName());
//			processLogs.setProcessUserNameEng(user.getUserNameEng());
//		}
		
		if (request != null)	processLogs.setAccessIp(Util.getClientIP(request));
		
		try {
			IndexApi indexApi = new IndexApi(restClient);
			if (!indexApi.existIndex(index))	indexApi.createIndex(index);
			
			DocumentApi documentApi = new DocumentApi(restClient);
			documentApi.createDocument(index, id, Converter.domainToMap(processLogs));
		} catch (Exception e) {
			log.error("오류가 발생 했습니다.", e);
		}
	}
	
	/**
	 * 프로세스 처리 로그 기록
	 * @param bulkList
	 * @throws Exception
	 */
	public void insertLogBulk(ArrayList<BulkData> bulkList) throws Exception {
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		
		try {
			if (bulkList != null && bulkList.size() > 0) {
				// Index 확인 후 Index 생성
				IndexApi indexApi = new IndexApi(restClient);
				if (!indexApi.existIndex(index))	indexApi.createIndex(index);
				
				DocumentApi documentApi = new DocumentApi(restClient);
				documentApi.bulkDocumentWithBulkProcessor(bulkList, 10000);
			}
		} catch (Exception e) {
			log.error("오류가 발생 했습니다.", e);
		}
	}
	
	/**
	 * 포멧별 엑셀다운로드시 필요한 데이터 count
	 * @param map
	 * @throws Exception
	 */	
	public int selectExcelOrderCount(SearchExcelList search) throws Exception {
		
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
		
		if("DELIVERY".equals(search.getSrchQueryType())) {
			Util.accessControlDatabase(search, "srchCusType", false, "srchDateGbn", "|noDateChk|AR_DATE", "srchNoType", "|od.MBL_NO|od.HBL_NO|multi.DELIVERY_NO");
		}else if("BROKER_SUB".equals(search.getSrchQueryType())) {
			Util.accessControlDatabase(search, "srchDateFormat|srchCusType", false, "srchDateGbn", "|noDateChk|AR_DATE", "srchNoType", "|od.MBL_NO|od.HBL_NO|od.PO_NO");
		}else if("BROKER".equals(search.getSrchQueryType())) {
			Util.accessControlDatabase(search, "srchDateFormat|srchCusType", false, "srchDateGbn", "|noDateChk|AR_DATE", "srchNoType", "|od.MBL_NO|od.HBL_NO|od.PO_NO");
		}else if("ORDER".equals(search.getSrchQueryType())) {
			Util.getCryptographyYn(search);
			Util.accessControlDatabase(search, "srchDateFormat|srchCusType", false, "srchDateGbn", "|noDateChk|od.PO_DATE|od.AR_DATE|od.OB_DATE|ad.SIMPLE_DOC_DATE", "srchNoType", "|od.MBL_NO|od.HBL_NO|od.PO_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO");
		}else if("ORDER_CUSTOMER".equals(search.getSrchQueryType())) {
			Util.getCryptographyYn(search);
			Util.accessControlDatabase(search, "loginCustomerLev|srchDateFormat|loginCustomerLowLev|loginCustomerUpperLev", false, "srchDateGbn", "|noDateChk|od.PO_DATE|od.AR_DATE|od.OB_DATE", "srchNoType", "|od.MBL_NO|od.HBL_NO|od.PO_NO|od.CONSIGNEE_NAME|od.CONSIGNEE_NAME_LOC|od.CONSIGNEE_TEL|od.CONSIGNEE_HP|od.SHIPPER_NAME|od.SHIPPER_TEL|od.COMMODITY_ITEM|multi.DELIVERY_NO");
		}else if("CONSOL".equals(search.getSrchQueryType())) {
			Util.accessControlDatabase(search, "srchDateFormat|srchCusType", false, "srchDateGbn", "|noDateChk|od.AR_DATE|od.OB_DATE", "srchNoType", "|od.HBL_NO|mod.MBL_NO|od.MASTER_OD_JOB_NO");
		}
		
		return commonMapper.selectExcelOrderCount(search);
	}
	
	/**
	 * 포멧별 엑셀다운로드시 필요한 데이터 count
	 * @param map
	 * @throws Exception
	 */	
	public List<Map<String, Object>> selectExcelDownFormatItem(Map<String, Object> map) throws Exception {
		return commonMapper.selectExcelDownFormatItem(map);
	}
	
	/**
	 * 포멧별 엑셀다운로드시 필요한 데이터 조회
	 * @param map
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectMakeExcelDownload(SearchExcelList search) throws Exception {
		
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

		Util.accessControlDatabase(search, "userLanguage|srchCusType|srchDateFormat|loginCustomerLev|loginCustomerLowLev|loginCustomerUpperLev", true, null, null, null, null);
		return commonMapper.selectMakeExcelDownload(search);
	}
	
	/**
	 * 포멧별 엑셀다운로드
	 * @param map
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> exceldownloadFormat(Map<String, Object> map, HttpServletRequest request) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		List<Map<String, Object>> formItemList = (List<Map<String, Object>>)map.get("formItemList");
		List<Map<String, Object>> masterItemList = (List<Map<String, Object>>)map.get("masterItemList");

		// TB_SMS_FORMAT에서 가져오기 
		if(formItemList.size() == 0) {
			returnMap.put("errCode", "A11198");	/** Excel download 설정 데이터가 없습니다. */
			return returnMap;
		}else if(formItemList.size() == 0 && masterItemList.size() == 0){
			returnMap.put("errCode", "A11198");	/** Excel download 설정 데이터가 없습니다. */
			return returnMap;
		}
		
		// ITEM, CONTAINER 테이블 데이터 문자열로 합쳐서 조회할때 필드명을 축약해서 용량을 줄이기 위해 변환한 값을 받아서 사용
		Map<String, Object> itemConvertMap = CreateExcelFile.convertExcelDbField("item");
		Map<String, Object> contConvertMap = CreateExcelFile.convertExcelDbField("cont");
		Map<String, Object> multiConvertMap = CreateExcelFile.convertExcelDbField("multi");
		Map<String, Object> licenseConvertMap = CreateExcelFile.convertExcelDbField("license");
		Map<String, Object> verticalRepeatConvertMap = null;


		String fileName = Util.null2str(map.get("documentName"));
		String vhType = Util.null2str(formItemList.get(0).get("V_H_TYPE"));
		String verticalDownType = Util.null2str(formItemList.get(0).get("VERTICAL_DOWN_TYPE"));
		String menuId = Util.null2str(formItemList.get(0).get("MENU_ID"));
		String verticalRepeat = null;
		if(menuId.equals("DELIVERY")){
			verticalRepeat = "MULTI_REPEAT";
			verticalRepeatConvertMap = multiConvertMap;
		}else{
			verticalRepeat = "ITEM_REPEAT";
			verticalRepeatConvertMap = itemConvertMap;
		}
		
		List<Map<String, Object>> orderFormItemList = new ArrayList<Map<String, Object>>();
		
		Workbook wb; 
		Sheet sheet;
		JSONArray jArr = null;
		
		///
		String saveFile = Util.null2str(map.get("saveFile"));
		String sheetNo = Util.null2str(map.get("sheetNo"));
		
		if(!"".equals(saveFile)){  // 엑셀파일이 있는경우 다음 sheet 생성하여 데이터 적재
			FileInputStream file = new FileInputStream(saveFile);
			wb = new SXSSFWorkbook(new XSSFWorkbook(file), 100);
			sheet = wb.createSheet(fileName + "(" + sheetNo + ")");
			sheet = wb.getSheetAt(Integer.parseInt(sheetNo));
		}else {
			wb = new SXSSFWorkbook(100); // rowAccessWindowSize: Flush 갯수
			sheet = wb.createSheet(fileName + "(" + sheetNo + ")"); // 시트생성
		}


		Cell cell;
		Row headerRow = null;
		Row etcRow = null;
		
		Map<String, CellStyle> comStyle = ExcelStyles.createStyles(wb);	//Column별 Style은 동일하므로 Column별 Style을 생성한다.
		List<Map<String, Object>> verticalRepeatFormat = new ArrayList<>();
		
		int titleRow = 0;
		int cnt = 1;
		int lastColNum = 0;
		int colNum = 0;
		int fieldLength = 0;
		int masterRow = 0;
		
		////////////////////////
		//// 마스터정보 타이틀 생성
		String masterYn = Util.null2str(map.get("masterYn")); 
		if("Y".equals(masterYn)){
			for(Map<String, Object> masterMap : masterItemList){
				colNum = Util.excelEng2int(masterMap.get("TITLE_COL"));
				fieldLength = Util.getInt(Util.null2str(masterMap.get("FIELD_LENGTH")));
				masterRow = Util.getInt(Util.null2str(masterMap.get("TITLE_ROW"))) - 1;
				headerRow = this.getRow(sheet, masterRow);
			
				cell = headerRow.createCell(colNum);
				cell.setCellValue(Util.null2str(masterMap.get("DB_FIELD_NAME")).replaceAll("<br/>", " ").replaceAll("<br>", " ").replaceAll("<br />", " "));
				cell.setCellStyle(comStyle.get("header"));
				
				if(titleRow < masterRow) titleRow = masterRow;
				
				sheet.setColumnWidth(colNum, fieldLength == 0 ? 4000 : fieldLength * 36);
			}
		}

		for(int i = 0; i < formItemList.size(); i++){
			if(!"ETC".equals(formItemList.get(i).get("ITEM_TYPE"))){
				titleRow = Util.null2int(formItemList.get(i).get("TITLE_ROW")) - 1;
				break;
			}
		}
		
		////////////////////////////		
		
		//the header row: centered text
		headerRow = this.getRow(sheet, titleRow);
		// 헤더 타이틀 생성 부분 
		for(Map<String, Object> itemMap : formItemList){ 
			
			if("Y".equals(masterYn) && "MASTER".equals(Util.null2str(itemMap.get("ITEM_TYPE")))) continue;
			
			if("ETC".equals(itemMap.get("ITEM_TYPE"))){
				/*if(titleRow > Util.null2int(itemMap.get("TITLE_ROW")) - 1){*/
					colNum = Util.excelEng2int(itemMap.get("TITLE_COL"));
					fieldLength = Util.getInt(Util.null2str(itemMap.get("FIELD_LENGTH")));
					masterRow = Util.getInt(Util.null2str(itemMap.get("TITLE_ROW"))) - 1;
					etcRow = this.getRow(sheet, masterRow);
				
					cell = etcRow.createCell(colNum);
					cell.setCellValue(Util.null2str(itemMap.get("DB_FIELD_NAME")).replaceAll("<br/>", " ").replaceAll("<br>", " ").replaceAll("<br />", " "));
					cell.setCellStyle(comStyle.get("header"));
					sheet.setColumnWidth(colNum, fieldLength == 0 ? 4000 : fieldLength * 36);
				/* } */
				continue;
			}
			
			colNum = Util.excelEng2int(itemMap.get("TITLE_COL"));
			fieldLength = Util.getInt(Util.null2str(itemMap.get("FIELD_LENGTH")));
			
			if(headerRow.getCell(colNum) == null) {
				cell = headerRow.createCell(colNum);
				cell.setCellValue(Util.null2str(itemMap.get("DB_FIELD_NAME")));
			}else {
				cell =  headerRow.getCell(colNum);
				String value= cell.getStringCellValue();
				cell.setCellValue(value + "\n" + Util.null2str(itemMap.get("DB_FIELD_NAME")));
			}
			
			if(cnt == 1){
				cell.setCellStyle(comStyle.get("header_left"));
			}else{
				cell.setCellStyle(comStyle.get("header"));
			}
			
			if(lastColNum < colNum) lastColNum = colNum;
			
			
			sheet.setColumnWidth(colNum, fieldLength == 0 ? 4000 : fieldLength * 36);
			
			if(Util.null2str(itemMap.get("DB_FIELD")).indexOf(verticalRepeat) > -1){
				verticalRepeatFormat.add(itemMap);	/** ITEM or MULTI(menuId = DELIVERY) */
			}
			
			cnt++;
		}
		
		
		cell = headerRow.getCell(lastColNum);
		cell.setCellStyle(comStyle.get("header_right"));
		
		//freeze the first row
		sheet.createFreezePane(0, titleRow + 1);
		
		
		////////////////////////////////////////////////////////
		
		List<Object> datas = (List<Object>)map.get("data"); // 엑셀에 들어갈 데이터
		List<Map<String, Object>> contRepeatList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> itemRepeatList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> multiRepeatList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> verticalRepeatData = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> licenseRepeatList = new ArrayList<Map<String, Object>>();
		
		
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> masterData = new HashMap<String, Object>();
		Map<String, Object> repeatMap = null;
		
		
		String dbField = "";
		String mValue = "";
		
		// elastic search 로그 데이터 간소화를 위해 변수에 담아준 후 제거 
		map.remove("formItemList");
		map.remove("masterItemList");
		map.remove("data");
		
		
		///////////////////////////////////////////////////
		// 마스터정보 값 입력
		if("Y".equals(masterYn)){
			if(datas != null && datas.size() > 0){
				masterData = (Map<String, Object>)datas.get(0);
			}else{
				masterData = new HashMap<String, Object>();
			}
			
			for(Map<String, Object> masterMap : masterItemList){
				masterRow = Util.getInt(Util.null2str(masterMap.get("VALUE_ROW"))) - 1;
				headerRow = this.getRow(sheet, masterRow);
				cell = headerRow.createCell(Util.excelEng2int(masterMap.get("VALUE_COL")));
				dbField = (String)masterMap.get("ETC1");
				mValue = Util.null2str(masterData.get(dbField));
				cell.setCellValue(mValue);
			}
		}
		/////////////////////////////////////////////////////
		
		for (int i = 0; i < datas.size(); i++) {
			verticalRepeatData.clear();
			itemRepeatList.clear();
			multiRepeatList.clear();
    		contRepeatList.clear();
    		licenseRepeatList.clear();
			if(titleRow!=sheet.getLastRowNum()) titleRow = sheet.getLastRowNum();
			headerRow = this.getRow(sheet, ++titleRow); 
    		data = (Map<String, Object>)datas.get(i);
    		
    		if(!"".equals(Util.null2str(data.get("multiInfo")))) {
				jArr = new JSONArray(Util.null2str(data.get("multiInfo")).replace("\n", "").replace("&#x0D;", ""));
				multiRepeatList = (List<Map<String, Object>>)Util.JSONArrayToList(jArr);
				if(menuId.equals("DELIVERY")){
	    			verticalRepeatData = multiRepeatList;
	    		}
			}
    		if(!"".equals(Util.null2str(data.get("itemsInfo")))) {
				jArr = new JSONArray(Util.null2str(data.get("itemsInfo")).replace("\n", "").replace("&#x0D;", ""));
				itemRepeatList = (List<Map<String, Object>>)Util.JSONArrayToList(jArr);
				if(!menuId.equals("DELIVERY")){
	    			verticalRepeatData = itemRepeatList;
	    		}
			}
			// itemsInfo2 추가(DKK용)
			List<Map<String, Object>> itemsInfo2List = new ArrayList<>();
			if(!"".equals(Util.null2str(data.get("itemsInfo2")))) {
				jArr = new JSONArray(Util.null2str(data.get("itemsInfo2")).replace("\n", "").replace("&#x0D;", ""));
				itemsInfo2List = (List<Map<String, Object>>)Util.JSONArrayToList(jArr);
				// itemRepeatList에 itemsInfo2 데이터 병합 (SEQ 기준)
				for(Map<String, Object> item2 : itemsInfo2List) {
					String seq2 = Util.null2str(item2.get("SEQ"));
					for(Map<String, Object> item : itemRepeatList) {
						if(seq2.equals(Util.null2str(item.get("SEQ")))) {
							item.putAll(item2); // ← itemsInfo에 itemsInfo2 필드 합치기
							break;
						}
					}
				}
			}
			// CONTAINER 데이터
			if(!"".equals(Util.null2str(data.get("containerInfo")))) {
				jArr = new JSONArray(Util.null2str(data.get("containerInfo")).replace("\n", "").replace("&#x0D;", ""));
				contRepeatList = (List<Map<String, Object>>)Util.JSONArrayToList(jArr);
			}
			
			// LICENSE 데이터
			if(!"".equals(Util.null2str(data.get("licenseInfo")))) {
				jArr = new JSONArray(Util.null2str(data.get("licenseInfo")).replace("\n", "").replace("&#x0D;", ""));
				licenseRepeatList = (List<Map<String, Object>>)Util.JSONArrayToList(jArr);
			}
			
    		int num = 0;
    		int z = 0;
    		for(Map<String, Object> itemMap : formItemList){
    			
    			if("Y".equals(masterYn) && "MASTER".equals(Util.null2str(itemMap.get("ITEM_TYPE")))) continue;

    			repeatMap = null;
    			/** 세로(L)일경우 DELIVERY는 멀티, 나머지는 ITEM만 세로반복됨 */
    			if("L".equals(Util.null2str(vhType))){
    				String etc1Field = Util.null2str(itemMap.get("ETC1"));
    				
    				if(etc1Field.indexOf("__CONTAINER__") > -1) { // ex) GWT'__CONTAINER__'1
						etc1Field = etc1Field.replace("__CONTAINER__", "");
						num = Util.null2int(etc1Field.charAt(etc1Field.length() - 1)); //ex) GWT__CONTAINER__'1' => blfiled number 값
    					etc1Field = etc1Field.substring(0, etc1Field.length() - 1); //ex) 'GWT'__CONTAINER__1
    					etc1Field = Util.null2str(contConvertMap.get(etc1Field));
    					
    					if(contRepeatList != null && contRepeatList.size() >= num) {
    						repeatMap = contRepeatList.get(num-1);
    						dbField = Util.null2str(repeatMap.get(etc1Field));
							cell = headerRow.createCell(Util.excelEng2int(itemMap.get("TITLE_COL")));
							cell.setCellValue(dbField);
    					}
					}else if(Util.null2str(itemMap.get("DB_FIELD")).indexOf("_REPEAT") == -1) { // 포함 x (반복부분이 아님)
    					cell = headerRow.createCell(Util.excelEng2int(itemMap.get("TITLE_COL")));
    					if("ETC".equals(itemMap.get("ITEM_TYPE"))){
    						cell.setCellValue(Util.null2str(itemMap.get("DESC_MESSAGE")));
    					}else{
    						dbField = (String)itemMap.get("ETC1");
        					if(dbField == null && "NUM".equals(itemMap.get("DB_FIELD"))){
    							dbField = "NUM";
    						}
        					if(dbField != null && !dbField.equals("") && dbField.indexOf(" AS ") > -1) { // ex) "CONSIGNEE_NAME_LOC AS CONSIGNEE_NAME_LOC2"
        						String dbFields[] = dbField.split(" AS ");	// ex) ["CONSIGNEE_NAME_LOC", "CONSIGNEE_NAME_LOC2"]
    							dbField = dbFields[dbFields.length - 1];	// ex) "CONSIGNEE_NAME_LOC2"
    						}
        					cell.setCellValue(Util.null2str(data.get(dbField)));
        					
        					/** VERTICAL_DOWN_TYPE가 2일경우 아이템로우에 오더내용 채우기 위해 추가 */
        					if(i == 0 && verticalDownType.equals("2")){
        						orderFormItemList.add(itemMap);
        					}
    					}
    					
    					
    				}else if(Util.null2str(itemMap.get("DB_FIELD")).indexOf(verticalRepeat) > -1){
    					if(verticalRepeatData != null && verticalRepeatData.size() > 0) {
							repeatMap = verticalRepeatData.get(0);
							cell = headerRow.createCell(Util.excelEng2int(itemMap.get("TITLE_COL")));
							dbField = (String)itemMap.get("ETC1");
							if(dbField != null && !dbField.equals("") && dbField.indexOf(" AS ") > -1) { // ex) "CONSIGNEE_NAME_LOC AS CONSIGNEE_NAME_LOC2"
        						String dbFields[] = dbField.split(" AS ");	// ex) ["CONSIGNEE_NAME_LOC", "CONSIGNEE_NAME_LOC2"]
    							dbField = dbFields[dbFields.length - 1];	// ex) "CONSIGNEE_NAME_LOC2"
    						}
							cell.setCellValue(Util.null2str(repeatMap.get(verticalRepeatConvertMap.get(dbField))));   
    					}
    				}
    				if(z == formItemList.size() - 1 && verticalRepeatData != null && verticalRepeatData.size() > 1) {
    					for (int j = 1; j < verticalRepeatData.size(); j++) {
							repeatMap = verticalRepeatData.get(j);
							headerRow = this.getRow(sheet, titleRow + j); 
							
							for (Map<String, Object> itemInfo : verticalRepeatFormat) {
								cell = headerRow.createCell(Util.excelEng2int(itemInfo.get("TITLE_COL")));
								dbField = (String)itemInfo.get("ETC1");
								if(dbField != null && !dbField.equals("") && dbField.indexOf(" AS ") > -1) { // ex) "CONSIGNEE_NAME_LOC AS CONSIGNEE_NAME_LOC2"
	        						String dbFields[] = dbField.split(" AS ");	// ex) ["CONSIGNEE_NAME_LOC", "CONSIGNEE_NAME_LOC2"]
	    							dbField = dbFields[dbFields.length - 1];	// ex) "CONSIGNEE_NAME_LOC2"
	    						}
								cell.setCellValue(Util.null2str(repeatMap.get(verticalRepeatConvertMap.get(dbField))));
							}
							
							/** VERTICAL_DOWN_TYPE가 2일경우 아이템로우에 오더내용 채우기 위해 추가 */
							if(verticalDownType.equals("2")){
								for(Map<String, Object> orderFormItem : orderFormItemList){
									cell = headerRow.createCell(Util.excelEng2int(orderFormItem.get("TITLE_COL")));
			    					dbField = (String)orderFormItem.get("ETC1");
			    					if(dbField == null && "NUM".equals(orderFormItem.get("DB_FIELD"))){
										dbField = "NUM";
									}
			    					if(dbField != null && !dbField.equals("") && dbField.indexOf(" AS ") > -1) { // ex) "CONSIGNEE_NAME_LOC AS CONSIGNEE_NAME_LOC2"
			    						String dbFields[] = dbField.split(" AS ");	// ex) ["CONSIGNEE_NAME_LOC", "CONSIGNEE_NAME_LOC2"]
										dbField = dbFields[dbFields.length - 1];	// ex) "CONSIGNEE_NAME_LOC2"
									}
			    					cell.setCellValue(Util.null2str(data.get(dbField)));
								}
							}
						}
    				}
    				
    			}else if("H".equals(Util.null2str(vhType))) { //H: COL(세로) => 기존 가로 양식 
    					String etc1Field = Util.null2str(itemMap.get("ETC1"));
    					if(etc1Field.indexOf("__ITEM__") > -1) { // ex) DETAIL'_ITEM_'NAME__ITEM__2
	    					//num = Util.null2int(etc1Field.replace("__ITEM__", "").replaceAll("[^0-9]", ""));  // ex)DETAIL_ITEM_NAME__ITEM__'2'
	    					//etc1Field = etc1Field.replace("__ITEM__", "").replaceAll("[0-9]", "");     // ex) 'DETAIL_ITEM_NAME'__ITEM__2
							if(etc1Field.contains("CATEGORY_NAME")) {
								String[] parts = etc1Field.split("__ITEM__");   // ["CATEGORY_NAME_1", "1"]
								num = Util.null2int(parts[1]);                    // 반복번호 1
								etc1Field = parts[0];                             // "CATEGORY_NAME_1" (숫자 보존)
							} else {
								num = Util.null2int(etc1Field.replace("__ITEM__", "").replaceAll("[^0-9]", ""));  // ex)DETAIL_ITEM_NAME__ITEM__'2'
								etc1Field = etc1Field.replace("__ITEM__", "").replaceAll("[0-9]", "");// ex) 'DETAIL_ITEM_NAME'__ITEM__2
							}
							etc1Field = Util.null2str(itemConvertMap.get(etc1Field));
	    					
	    					if(itemRepeatList != null && itemRepeatList.size() > 0) {
	    						if(num <= Integer.parseInt((String) itemRepeatList.get(itemRepeatList.size()-1).get("SEQ"))) {
		    						for (int j = 0; j < itemRepeatList.size(); j++) {
				    					repeatMap = itemRepeatList.get(j); 
		    							if(num == Util.null2int(repeatMap.get("NUM"))) {
			    							dbField = Util.null2str(repeatMap.get(etc1Field));
			    							cell = headerRow.createCell(Util.excelEng2int(itemMap.get("TITLE_COL")));
			    							cell.setCellValue(dbField);
			    							break;
		    							}
		    							// 엑셀 아이템 다운로드 오류로 인하여 주석 2024.06.11
		    							// repeatMap.clear();
		    							dbField = "";
		    						}
		    					}
	    					}
    					}else if(etc1Field.indexOf("__CONTAINER__") > -1) { // ex) GWT'__CONTAINER__'1
    						etc1Field = etc1Field.replace("__CONTAINER__", "");
    						num = Util.null2int(etc1Field.charAt(etc1Field.length() - 1)); //ex) GWT__CONTAINER__'1' => blfiled number 값
	    					etc1Field = etc1Field.substring(0, etc1Field.length() - 1); //ex) 'GWT'__CONTAINER__1
	    					etc1Field = Util.null2str(contConvertMap.get(etc1Field));
	    					if(contRepeatList != null && contRepeatList.size() > 0) {
	    						for (int j = 0; j < contRepeatList.size(); j++) {
	    							repeatMap = contRepeatList.get(j);
	    							if(num == (Util.null2int(repeatMap.get("NUM")))) {  // "NUM" 값과 blfiled number 값이 같은것만 값 세팅 
	    								dbField = Util.null2str(repeatMap.get(etc1Field));
	    								cell = headerRow.createCell(Util.excelEng2int(itemMap.get("TITLE_COL")));
	    								cell.setCellValue(dbField);
	    								break;
	    							}
	    							repeatMap.clear();
	    							dbField = "";
	    						}
	    					}
    					}else if(etc1Field.indexOf("__MULTI__") > -1) {
    						etc1Field = etc1Field.replace("__MULTI__", "");
    						num = Util.null2int(etc1Field.charAt(etc1Field.length() - 1));
	    					etc1Field = etc1Field.substring(0, etc1Field.length() - 1);
	    					etc1Field = Util.null2str(multiConvertMap.get(etc1Field));
	    					if(multiRepeatList != null && multiRepeatList.size() > 0) {
	    						for (int j = 0; j < multiRepeatList.size(); j++) {
	    							repeatMap = multiRepeatList.get(j);
	    							if(num == (Util.null2int(repeatMap.get("NUM")))) { 
	    								dbField = Util.null2str(repeatMap.get(etc1Field));
	    								cell = headerRow.createCell(Util.excelEng2int(itemMap.get("TITLE_COL")));
	    								cell.setCellValue(dbField);
	    								break;
	    							}
	    							repeatMap.clear();
	    							dbField = "";
	    						}
	    					}
    					}else if(etc1Field.indexOf("__LICENSE__") > -1) { // ex) GWT'__LICENSE__'1
    						etc1Field = etc1Field.replace("__LICENSE__", "");
    						num = Util.null2int(etc1Field.charAt(etc1Field.length() - 1)); //ex) GWT__LICENSE__'1' => blfiled number 값
	    					etc1Field = etc1Field.substring(0, etc1Field.length() - 1); //ex) 'GWT'__LICENSE__1
	    					etc1Field = Util.null2str(licenseConvertMap.get(etc1Field));
	    					if(licenseRepeatList != null && licenseRepeatList.size() > 0) {
	    						for (int j = 0; j < licenseRepeatList.size(); j++) {
	    							repeatMap = licenseRepeatList.get(j);
	    							if(num == (Util.null2int(repeatMap.get("NUM")))) {  // "NUM" 값과 blfiled number 값이 같은것만 값 세팅 
	    								dbField = Util.null2str(repeatMap.get(etc1Field));
	    								cell = headerRow.createCell(Util.excelEng2int(itemMap.get("TITLE_COL")));
	    								cell.setCellValue(dbField);
	    								break;
	    							}
	    							repeatMap.clear();
	    							dbField = "";
	    						}
	    					}
    					}else {
							cell = headerRow.createCell(Util.excelEng2int(itemMap.get("TITLE_COL")));
							if("ETC".equals(itemMap.get("ITEM_TYPE"))){
	    						cell.setCellValue(Util.null2str(itemMap.get("DESC_MESSAGE")));
	    					}else{
	    						dbField = (String)itemMap.get("ETC1");
	    						if(dbField == null && "NUM".equals(itemMap.get("DB_FIELD"))){
	    							dbField = "NUM";
	    						}
	    						if(dbField != null && !dbField.equals("") && dbField.indexOf(" AS ") > -1) { // ex) "CONSIGNEE_NAME_LOC AS CONSIGNEE_NAME_LOC2"
	    							String dbFields[] = dbField.split(" AS ");	// ex) ["CONSIGNEE_NAME_LOC", "CONSIGNEE_NAME_LOC2"]
	    							dbField = dbFields[dbFields.length - 1];	// ex) "CONSIGNEE_NAME_LOC2"
	    						}
	    						cell.setCellValue(Util.null2str(data.get(dbField)));
	    					}
    					}
    			}// vhType END
    			z++;
			}
    		
    		// 100개마다 flushRows 메소드를 통해 메모리에 있는 데이터를 디스크(임시파일)로 옮기고 메모리를 비워준다.
    		if (i % 100 == 0) ((SXSSFSheet) sheet).flushRows(100); 
    		 
		}// datas.size()


		String dir = customProperties.getExcelDownload() + "/";
		String currDtm = "";
		String originFile = fileName + ".xlsx";
		
		if("".equals(saveFile)){ // 엑셀파일 생성
			currDtm = DateTimeUtil.getDate("yyyyMMddhhmmss");
			saveFile = fileName + "_" + currDtm + ".xlsx";
			if(Common.checkSaveFilenameForExcel(fileName)) {
    			String uuid = UUID.randomUUID().toString();
                saveFile = uuid + "_" + currDtm + ".xlsx";
    		}
			//workbook파일 출력
			FileUtil.fileWrite(wb, dir, saveFile);
			((SXSSFWorkbook) wb).dispose(); // 임시파일 제거 
			returnMap.put("saveFile",  dir + saveFile);
			returnMap.put("originFile", originFile);
		}else {
			saveFile = saveFile.replaceAll(dir, "");
			FileUtil.fileWrite(wb, dir, saveFile);
			((SXSSFWorkbook) wb).dispose(); // 임시파일 제거 
			returnMap.put("saveFile",  dir + saveFile);
			returnMap.put("originFile", originFile);
		}
		returnMap.put("saveFile", StringEscapeUtils.escapeHtml4(returnMap.get("saveFile").toString()));
		returnMap.put("originFile", StringEscapeUtils.escapeHtml4(returnMap.get("originFile").toString()));
		
		return returnMap;
	}
	
	public DownloadFileInfo makeExcelDownload(Map<String, Object> map,  Object params, Object mapper, String queryId, String parameterClass) throws Exception {
		return makeExcelDownload(map, params, mapper, queryId, parameterClass, true);
	}
	public DownloadFileInfo makeExcelDownload(Map<String, Object> map,  Object params, Object mapper, String queryId, String parameterClass, boolean extendsTotal) throws Exception {
		DownloadFileInfo downloadFileInfo = new DownloadFileInfo();
		List<Object> excelDataList;
		int page = 1; 
		int pageSize = 10000;
		int seq = 0; // 고객서비스 등록, 발송 - 일별로 엑셀 다운로드시 NUM 항목값이 날짜별 ROW NUMBER로 들어가서 시퀀스로 대체
		map.put("parameterClass", parameterClass);
		
		Method selectData = mapper.getClass().getMethod(queryId, Class.forName(parameterClass));
		Object listCount = ((List<Object>)selectData.invoke(mapper, params)).get(0);
		Class<?> cls;
		if(extendsTotal){
			cls = listCount.getClass().getSuperclass();
		}else{
			cls = listCount.getClass();
		}
		
		Field field = cls.getDeclaredField("total");
		field.setAccessible(true);
		int totalCount = Util.null2int(field.get(listCount));
		
		cls = params.getClass().getSuperclass();
		field = cls.getDeclaredField("pageSize");
		field.setAccessible(true);
		field.set(params, pageSize);
		
		field = cls.getDeclaredField("srchMode");
		field.setAccessible(true);
		field.set(params, "selectCodeName");
		
		field = cls.getDeclaredField("startNum");
		field.setAccessible(true);
		
		if(totalCount == 0) totalCount = 1;
		for (int startNum = 0; startNum < totalCount; startNum += pageSize, page++) {
			field.set(params, startNum);
			excelDataList = (List<Object>)selectData.invoke(mapper, params);
			
			map.put("data", excelDataList);
            map.put("sheetNo", page);
			
            // 2024.06.05 - 오더조회 listCount에 필터가 적용되지 않음 > listCount와 실제 건수가 달라서 불필요한 시트가 생성될 가능성 있음 > 데이터가 없으면 생성 중지
            /** 조회데이터가 한건도 없을경우 빈 엑셀파일 만들기위해 조건 추가 */
            if(excelDataList.size() < 1 && startNum != 0) {
            	break;
            }
            
            if(parameterClass.equals("org.yht.exps.express.model.consumerService.order.search.SearchOrderCustomerList") ||
            		parameterClass.equals("org.yht.exps.express.model.eCommerce.order.search.SearchOrderList")) {
            	exceldownloadCsv(map);	// 2024.08.12 - 오더조회 & 고객서비스 등록, 발송은 건수 관계없이 무조건 csv로 생성
            	seq = Util.null2int(map.get("seq"));
				seq += excelDataList.size();
				map.put("seq", seq);
            }else if(totalCount < 30000) {
				exceldownload(map);
			}else{
				exceldownloadCsv(map);
			}
		}
		
		downloadFileInfo.setSaveFile(Util.null2str(map.get("saveFile")).replace(customProperties.getExcelDownload() + "/", ""));
		downloadFileInfo.setOriginFile(Util.null2str(map.get("originFile")));
		if(seq > 0) {
			downloadFileInfo.setSeq(seq);
		}
		return downloadFileInfo;
	}
	
	/** 정렬/포멧 등 기능 제거함 */
	public void exceldownload(Map<String, Object> map) throws Exception {
		String fileName = Util.null2str(map.get("mainTitle"));
		
		Workbook wb; 
		Sheet sheet;
		
		String saveFile = Util.null2str(map.get("saveFile"));
		String sheetNo = Util.null2str(map.get("sheetNo"));
		FileInputStream file= null;
		
		if(!"".equals(saveFile)){	/** 엑셀파일이 있는경우 다음 sheet 생성하여 데이터 적재 */
			file = new FileInputStream(saveFile);
			wb = new SXSSFWorkbook(new XSSFWorkbook(file), 100);
			sheet = wb.createSheet(fileName + "(" + sheetNo + ")");
		}else {
			wb = new SXSSFWorkbook(100);	/** rowAccessWindowSize: Flush 갯수 */
			sheet = wb.createSheet(fileName + "(" + sheetNo + ")");
		}
		
		Map<String, CellStyle> comStyle = ExcelStyles.createStyles(wb, true);
		
		List<String> columns = (List<String>)map.get("columns");
		List<String> titles = (List<String>)map.get("titles");
		List<Object> datas = (List<Object>)map.get("data");
		List<Integer> columnsWidth = (List<Integer>)map.get("columnsWidth");
		Map<String, Object> data;
		
		Row row;
		Cell cell;
		int rownum = 0;
		int titleSize = titles.size();
		Row headerRow = sheet.createRow(rownum++);
		headerRow.setHeight((short) 300);
		headerRow.setHeightInPoints(15.00f);
		
		for (int i = 0; i < titleSize; i++) {
			cell = headerRow.createCell(i);
			cell.setCellValue(Util.null2str(titles.get(i)).replaceAll("<br/>", " ").replaceAll("<br>", " ").replaceAll("<br />", " "));
			if((i+1) == titleSize) {
				cell.setCellStyle(comStyle.get("header_right"));
			} else if(i == 0) {
				cell.setCellStyle(comStyle.get("header_left"));
			} else {
				cell.setCellStyle(comStyle.get("header"));
			}
			sheet.setColumnWidth(i, Util.null2int(columnsWidth.get(i)) * 36);
		}
		
		sheet.createFreezePane(0, 1);
		
		ObjectMapper objectMapper = new ObjectMapper();
		for (int i = 0, n = datas.size(); i < n; i++, rownum++) {
			row = sheet.createRow(rownum);
        	row.setHeight((short) 300);
        	
    		data = (Map<String, Object>)objectMapper.convertValue(datas.get(i), Map.class);
        	for(int j = 0; j < titleSize; j++) {
        		cell = row.createCell(j);
        		cell.setCellValue(Util.null2str(data.get(Util.null2str(columns.get(j)))));
        	}
			
			/** 100개마다 flushRows 메소드를 통해 메모리에 있는 데이터를 디스크(임시파일)로 옮기고 메모리를 비워준다. */
			if (i % 100 == 0) ((SXSSFSheet) sheet).flushRows(100); 
		}

		String dir = customProperties.getExcelDownload() + "/";
		String currDtm = "";
		String originFile = fileName + ".xlsx";
		
		if("".equals(saveFile)){	/** 엑셀파일 생성 */
			currDtm = DateTimeUtil.getDate("yyyyMMddhhmmss");
			saveFile = fileName + "_" + currDtm + ".xlsx";
			if(Common.checkSaveFilenameForExcel(fileName)) {
    			String uuid = UUID.randomUUID().toString();
    			saveFile = uuid + "_" + currDtm + ".xlsx";
    		}
			FileUtil.fileWrite(wb, dir, saveFile);	/** workbook파일 출력 */
			((SXSSFWorkbook) wb).dispose();	/** 임시파일 제거 */ 
			map.put("saveFile",  dir + saveFile);
			map.put("originFile", originFile);
		}else{
			saveFile = saveFile.replaceAll(dir, "");
			FileUtil.fileWrite(wb, dir, saveFile);
			((SXSSFWorkbook) wb).dispose(); 
			map.put("saveFile",  dir + saveFile);
			map.put("originFile", originFile);
			file.close();
		}
	}
	public void exceldownloadCsv(Map<String, Object> map) throws Exception {

		List<String> headerCsvList = null;
		
		String saveFile = Util.null2str(map.get("saveFile"));
		String dir = customProperties.getExcelDownload() + "/";
		
		List<String> columns = (List<String>)map.get("columns");
		List<String> titles = (List<String>)map.get("titles");
		List<Object> datas = (List<Object>)map.get("data");
		
		if("".equals(saveFile)){
			String fileName = Util.null2str(map.get("mainTitle"));
			saveFile = dir + fileName + "_" + DateTimeUtil.getDate("yyyyMMddhhmmss") + ".csv";
    		if(Common.checkSaveFilenameForExcel(fileName)) {
    			String uuid = UUID.randomUUID().toString();
                saveFile = dir + uuid + "_" + DateTimeUtil.getDate("yyyyMMddhhmmss") + ".csv";
    		}
			map.put("originFile", fileName + ".csv");
			map.put("saveFile",  saveFile);
			
			headerCsvList = new ArrayList<>();
			for(String title : titles){
				headerCsvList.add(title);
			}
		}
		

		File file = null;
		BufferedWriter bw = null;
        OutputStreamWriter osw = null;
        FileOutputStream fos = null;
        
		String newLine = System.lineSeparator();
		StringBuffer headSb = new StringBuffer();
		StringBuffer dataSb = new StringBuffer();
		
		Map<String, Object> data = new HashMap<String, Object>();
		int seq = Util.null2int(map.get("seq"));
		
		try {
			
			file = new File(saveFile);
			fos = new FileOutputStream(file,true); //  true로 지정되면 해당 파일에 이어쓰기
			osw = new OutputStreamWriter(fos, "MS949"); // "euc-kr" , "MS949"
			bw = new BufferedWriter(osw);

			if(headerCsvList != null){
				headSb.append(Util.null2str(headerCsvList));
				headSb.deleteCharAt(0).deleteCharAt(headSb.length()-1);
				bw.write(String.valueOf(headSb));
				bw.write(newLine);
			}
			
			ObjectMapper objectMapper = new ObjectMapper();
			for (int i = 0; i < datas.size(); i++) {
				data = (Map<String, Object>)objectMapper.convertValue(datas.get(i), Map.class);
	    		dataSb.setLength(0);
	    		
	    		for(String column : columns){
	    			if(column.equals("num") && seq > 0) {
	    				data.put(column, ++seq);
	    			}
	    			// 2024.08.19 csv 파일로 다운받을 때 문자의 오류로 인하여 문자열로 강제 인식시킴
	    			if(column.equals("poNo") 			 || column.equals("hblNo") 					|| column.equals("mblNo") 				  || column.equals("consigneeZipcode")
	    				|| column.equals("consigneeTel") || column.equals("consigneeHp")  			|| column.equals("shipperHp")    		  || column.equals("shipperTel")
	    				|| column.equals("clearanceDate")|| column.equals("clearanceReceiptDate")	|| column.equals("clearanceViolationDate")|| column.equals("deliveryRecvDate")
	    				|| column.equals("pickupDate")   || column.equals("deliveryDate")           || column.equals("undeliveryDate")  	  || column.equals("mfCorrectionDate")) {
	    				data.put(column, "=\"" + Util.null2str(data.get(column)) + "\"");
	    			}
	    			
	    			//2024-10-14 오더조회, 고객서비스(등록,발송) 메뉴에서 엑셀 다운로드시 진행상태 값 코드명이 보이도록 수정
	    			if("org.yht.exps.express.model.consumerService.order.search.SearchOrderCustomerList".equals(Util.null2str(map.get("parameterClass"))) 
	    					|| "org.yht.exps.express.model.eCommerce.order.search.SearchOrderList".equals(Util.null2str(map.get("parameterClass")))) {
	    				if(column.equals("lastPodStatus")) {
	    					data.put(column, Util.null2str(data.get("lastPodStatusNm")));
	    				}
	    			}
	    			
	    			dataSb.append("," + CreateExcelFile.convertCsvFormat(Util.null2str(data.get(column))));
				}
				dataSb.append(newLine);
				bw.write(String.valueOf(dataSb.deleteCharAt(0)));
				
				if (i % 100 == 0) bw.flush();
			}

			bw.flush();
			bw.close();
		} catch (Exception e) {
			log.error(Util.getPrintStacTraceString(e));
			bw.close();
		}
	}
	
	/**
	 * 포멧별 csv다운로드
	 * @param map
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> exceldownloadFormatCsv(Map<String, Object> map, HttpServletRequest request) throws Exception {
		Map<String, Object> returnMap = new HashMap<>();
		List<Map<String, Object>> formItemList = (List<Map<String, Object>>)map.get("formItemList");
		List<Map<String, Object>> masterItemList = (List<Map<String, Object>>)map.get("masterItemList");

		// TB_SMS_FORMAT에서 가져오기 
		if(formItemList.size() == 0){
			returnMap.put("errCode", "A11198");	/** Excel download 설정 데이터가 없습니다. */
			return returnMap;
		}
		
		JSONArray jArr = null;

		List<String> headerCsvList = new ArrayList<>(); // CSV파일의 헤더라인 생성을 위한 헤더리스트
		
		// ITEM, CONTAINER 테이블 데이터 문자열로 합쳐서 조회할때 필드명을 축약해서 용량을 줄이기 위해 변환한 값을 받아서 사용
		Map<String, Object> itemConvertMap = CreateExcelFile.convertExcelDbField("item");
		Map<String, Object> contConvertMap = CreateExcelFile.convertExcelDbField("cont");
		Map<String, Object> multiConvertMap = CreateExcelFile.convertExcelDbField("multi");
		Map<String, Object> verticalRepeatConvertMap = null;
		
		int colNum = 0;
		int columnCount = 0; //nullLine = new String[columnCount];
		
		String fileName = Util.null2str(map.get("documentName"));
		String vhType = Util.null2str(formItemList.get(0).get("V_H_TYPE"));
		String verticalDownType = Util.null2str(formItemList.get(0).get("VERTICAL_DOWN_TYPE"));
		String menuId = Util.null2str(formItemList.get(0).get("MENU_ID"));
		String verticalRepeat = null;
		if(menuId.equals("DELIVERY")){
			verticalRepeat = "MULTI_REPEAT";
			verticalRepeatConvertMap = multiConvertMap;
		}else{
			verticalRepeat = "ITEM_REPEAT";
			verticalRepeatConvertMap = itemConvertMap;
		}
		List<Map<String, Object>> verticalRepeatFormat = new ArrayList<>();
		
		List<Map<String, Object>> orderFormItemList = new ArrayList<Map<String, Object>>();
		String saveFile = Util.null2str(map.get("saveFile"));
		String dir = customProperties.getExcelDownload() + "/";
		String currDtm = DateTimeUtil.getDate("yyyyMMddhhmmss");
		String originFile = fileName + ".csv";
		String makeMasterYn = "N"; 
		String masterYn = Util.null2str(map.get("masterYn"));
		int headerCheckSize;
		
		if("".equals(saveFile)){ // 기존에 생성된 CSV파일이 없는경우 새로운 CSV파일 생성  
			saveFile = fileName + "_" + currDtm + ".csv";
			if(Common.checkSaveFilenameForExcel(fileName)) {
				String uuid = UUID.randomUUID().toString();
				saveFile = uuid + "_" + currDtm + ".csv";
			}
			
			for(Map<String, Object> itemMap : formItemList){ // 헤더 타이틀 생성 부분 
				
				if("Y".equals(masterYn) && "MASTER".equals(Util.null2str(itemMap.get("ITEM_TYPE")))) continue;
				
				if(!"".equals(Util.null2str(itemMap.get("ETC6"))) || "NUM".equals(itemMap.get("DB_FIELD")) || "ETC".equals(itemMap.get("ITEM_TYPE"))){
					colNum = Util.excelEng2int(itemMap.get("TITLE_COL")); // 엑셀 열 알파벳을 숫자로 변환 
					if(headerCsvList.size() > colNum && !"".equals(Util.null2str(headerCsvList.get(colNum)))) {
						String header = Util.null2str(headerCsvList.get(colNum));
						headerCsvList.remove(colNum);
						headerCsvList.add(colNum, header+"/"+ CreateExcelFile.convertCsvFormat(Util.null2str(itemMap.get("DB_FIELD_NAME"))));
					}else{
						headerCheckSize = colNum - headerCsvList.size();
						if(headerCheckSize != 0){
							for(int i = headerCheckSize; i > 0; i--){
								headerCsvList.add(colNum - i, "");
							}
						}
						headerCsvList.add(colNum,  CreateExcelFile.convertCsvFormat(Util.null2str(itemMap.get("DB_FIELD_NAME"))));
					}
					
					if(Util.null2str(itemMap.get("DB_FIELD")).indexOf(verticalRepeat) > -1){
						verticalRepeatFormat.add(itemMap);	/** ITEM or MULTI(menuId = DELIVERY) */
					}
				}
			}
			makeMasterYn = "Y"; // 파일 처음 생성시에만 마스터정보 출력 
		}else {
			saveFile = saveFile.replaceAll(dir, "");
			if(!"H".equals(Util.null2str(vhType))) {
				columnCount = Util.null2int(map.get("columnCount"));
			}
		}
		

		File file = null;
		BufferedWriter bw = null;
        OutputStreamWriter osw = null;
        FileOutputStream fos = null;
        
		String newLine = System.lineSeparator(); // 줄바꿈(\n)
		StringBuffer headSb = new StringBuffer(); // 헤더 라인 
		StringBuffer dataSb = new StringBuffer(); // 데이터 라인
		String[] nullLine = null;
		String[] resetLine = null;
		
		////////////////////////////////////////////////////////////////////////////////////////////
		List<Object> datas = (List<Object>)map.get("data"); // 엑셀에 들어갈 데이터
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> repeatMap = null;
		// ITEM, CONTAINER 테이블 데이터 문자열로 합쳐서 조회할때 필드명을 축약해서 용량을 줄이기 위해 변환한 값을 받아서 사용
		List<Map<String, Object>> itemRepeatList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> contRepeatList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> multiRepeatList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> verticalRepeatData = new ArrayList<Map<String, Object>>();
		
		String dbField = "";
		
 		// elastic search 로그 데이터 간소화를 위해 변수에 담아준 후 제거 
		map.remove("formItemList");
		map.remove("masterItemList");
		map.remove("data");
		
		//////////////////////////////////////////////////////////////////////////////////////////////
		
		
		try {
			
			file = new File(dir + saveFile);
			fos = new FileOutputStream(file,true); //  true로 지정되면 해당 파일에 이어쓰기
			osw = new OutputStreamWriter(fos, "MS949"); // "euc-kr" , "MS949"
			bw = new BufferedWriter(osw);
			
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 마스터 타이틀, 값 생성 
			String mTitle = "";
			String mValue = "";
			String mRow = "";
			String mvRow = "";
			int mTitleCol = 0;
			int mValueCol = 0;
			Map<String, Object> masterData = new HashMap<String, Object>();
			Map<String, Object> masterCsvMap = new HashMap<String, Object>();
			if("Y".equals(masterYn) && "Y".equals(makeMasterYn)){ 
				masterData = (Map<String, Object>)datas.get(0);

				String[] masterLine = new String[10]; // 마스터정보 5개셀로 고정, 추후 조정
				String[] masterValueLine;
				Arrays.fill(masterLine, ""); // 배열을 빈값으로 채워줌  
				resetLine = masterLine.clone(); // 배열 nullLine 초기화를 위함
				for(Map<String, Object> masterMap : masterItemList){
					mTitle = Util.null2str(masterMap.get("DB_FIELD_NAME"));
					mTitleCol = Util.null2int(Util.excelEng2int(masterMap.get("TITLE_COL")));
					dbField = (String)masterMap.get("ETC1");
					mValue = Util.null2str(masterData.get(dbField));
					mValueCol = Util.null2int(Util.excelEng2int(masterMap.get("VALUE_COL")));
					mRow = Util.null2str(masterMap.get("TITLE_ROW"));
					mvRow = Util.null2str(masterMap.get("VALUE_ROW"));

					if(masterCsvMap.get(mRow) != null){
						masterLine = (String[]) masterCsvMap.get(mRow);
					}else {
						masterLine = resetLine.clone();
					}
					if(mRow.equals(mvRow)){
						masterValueLine = masterLine;
					}else{
						if(masterCsvMap.get(mvRow) != null){
							masterValueLine = (String[]) masterCsvMap.get(mvRow);
						}else {
							masterValueLine = resetLine.clone();
						}
					}
					
					masterLine[mTitleCol] = mTitle;
					masterValueLine[mValueCol] = CreateExcelFile.convertCsvFormat(mValue);
					masterCsvMap.put(mRow,masterLine);
					
					if(!mRow.equals(mvRow)){
						masterCsvMap.put(mvRow,masterValueLine);
					}
				}

				masterCsvMap.forEach((key, value) -> {
					headSb.append(String.join(",", (String[])value));
					headSb.append(newLine);
				});
				if(!"".equals(Util.null2str(headSb))) {
					bw.write(String.valueOf(headSb));
					headSb.setLength(0);
				}
			}

			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 헤더 
			headSb.append(Util.null2str(headerCsvList));
			headSb.deleteCharAt(0).deleteCharAt(headSb.length()-1);
			
			if(!"".equals(Util.null2str(headSb))) {
				bw.write(String.valueOf(headSb));
				bw.write(newLine);
				// V: ROW(가로) 양식 빈 CSV라인(,,,,,,,,,,,) 만들기 위해 헤더 사이즈 변수에 담아줌
				if(!"H".equals(Util.null2str(vhType))) {
					columnCount = headerCsvList.size();  
				}
				headSb.setLength(0);
			}

			// V: ROW(가로) 양식 헤더컬럼 사이즈의 빈 배열을 만듬 
			if(!"H".equals(Util.null2str(vhType))) {
				nullLine = new String[columnCount];
				Arrays.fill(nullLine, ""); // 배열을 빈값으로 채워줌  
				resetLine = nullLine.clone(); // 배열nullLine 초기화를 위함
			}
			//

			
			for (int i = 0; i < datas.size(); i++) {
				verticalRepeatData.clear();
				itemRepeatList.clear();
	    		contRepeatList.clear();
	    		multiRepeatList.clear();
	    		data = (Map<String, Object>)datas.get(i);
	    		dataSb.setLength(0);
	    		if("L".equals(Util.null2str(vhType)))nullLine = resetLine.clone();
	    		
				if(!"".equals(Util.null2str(data.get("multiInfo")))) {
					jArr = new JSONArray(Util.null2str(data.get("multiInfo")).replace("\n", "").replace("&#x0D;", ""));
					multiRepeatList = (List<Map<String, Object>>)Util.JSONArrayToList(jArr);
					if(menuId.equals("DELIVERY")){
		    			verticalRepeatData = multiRepeatList;
		    		}
				}
	    		if(!"".equals(Util.null2str(data.get("itemsInfo")))) {
					jArr = new JSONArray(Util.null2str(data.get("itemsInfo")).replace("\n", "").replace("&#x0D;", ""));
					itemRepeatList = (List<Map<String, Object>>)Util.JSONArrayToList(jArr);
					if(!menuId.equals("DELIVERY")){
		    			verticalRepeatData = itemRepeatList;
		    		}
				}
				
				 
				// CONTAINER 테이블
				if(!"".equals(Util.null2str(data.get("containerInfo")))) {	
					jArr = new JSONArray(Util.null2str(data.get("containerInfo")).replace("\n", "").replace("&#x0D;", ""));
					contRepeatList = (List<Map<String, Object>>)Util.JSONArrayToList(jArr);
				}
				
	    		int num = 0;
	    		int vIndex = 0; // formItemList의 사이즈 비교를 하기 위한 변수 
	    		String etc1Field = "";
	    		String dbValue = "";
	    		Map<String, Object> itemColnumInfo = new HashMap<String, Object>();
	    		 
	    		
	    		for(Map<String, Object> itemMap : formItemList){
	    			
	    			if("Y".equals(masterYn) && "MASTER".equals(Util.null2str(itemMap.get("ITEM_TYPE")))) continue;
	    			
	    			colNum = Util.excelEng2int(itemMap.get("TITLE_COL")); // 번호(0 ~ ?) 
	    			etc1Field = Util.null2str(itemMap.get("ETC1")); // 필드명
	    			/** 세로(L)일경우 DELIVERY는 멀티, 나머지는 ITEM만 세로반복됨 */
	    			if("L".equals(Util.null2str(vhType))){
	    				if(etc1Field.indexOf("__CONTAINER__") > -1) { // ex) GWT'__CONTAINER__'1
	    					etc1Field = etc1Field.replace("__CONTAINER__", "");
	    					num = Util.null2int(etc1Field.charAt(etc1Field.length() - 1));
	    					etc1Field = etc1Field.substring(0, etc1Field.length() - 1); //ex) 'GWT'__CONTAINER__1
	    					etc1Field = Util.null2str(contConvertMap.get(etc1Field));
	    					
	    					if(contRepeatList != null && contRepeatList.size() >= num) {
	    						repeatMap = contRepeatList.get(num-1);
	    						dbField = Util.null2str(repeatMap.get(etc1Field));
	    						nullLine[colNum] = CreateExcelFile.convertCsvFormat(dbField);
	    					}
	    					
	    				}else if(Util.null2str(itemMap.get("DB_FIELD")).indexOf("_REPEAT") == -1) { // 포함 x (반복부분이 아님)
	    					if("ETC".equals(itemMap.get("ITEM_TYPE"))){
	    						nullLine[colNum] = Util.null2str(itemMap.get("DESC_MESSAGE"));
	    					}else{
	    						if("".equals(etc1Field) && "NUM".equals(itemMap.get("DB_FIELD"))){
		    						etc1Field = "NUM";
		    					}
		    					if(dbField != null && !etc1Field.equals("") && etc1Field.indexOf(" AS ") > -1) { // ex) "CONSIGNEE_NAME_LOC AS CONSIGNEE_NAME_LOC2"
		    						String etc1Fields[] = etc1Field.split(" AS ");	// ex) ["CONSIGNEE_NAME_LOC", "CONSIGNEE_NAME_LOC2"]
		    						etc1Field = etc1Fields[etc1Fields.length - 1];	// ex) "CONSIGNEE_NAME_LOC2"
								}
		    					nullLine[colNum] = CreateExcelFile.convertCsvFormat(Util.null2str(data.get(etc1Field))); // ?
		    					
		    					/** VERTICAL_DOWN_TYPE가 2일경우 아이템로우에 오더내용 채우기 위해 추가 */
		    					if(i == 0 && verticalDownType.equals("2")){
		    						orderFormItemList.add(itemMap);
		    					}
	    					}
	    					
	    				}else if(Util.null2str(itemMap.get("DB_FIELD")).indexOf(verticalRepeat) > -1){
    						etc1Field = Util.null2str(verticalRepeatConvertMap.get(etc1Field));
    						if(verticalRepeatData != null && verticalRepeatData.size() > 0) {
    							repeatMap = verticalRepeatData.get(0); // hbl 정보 + 첫번째 아이템으로 csv라인 생성  
    							nullLine[colNum] = CreateExcelFile.convertCsvFormat(Util.null2str(repeatMap.get(etc1Field)));
    							itemColnumInfo.put(etc1Field, colNum); // colnumInfo에 (컬럼명,colNum) 값을 넣어준다
    						}
	    				}
	    				
	    				
	    				// 엑셀다운로드 컬럼항목이 마지막일 경우에는 csv한줄이 생성 되었으므로 newLine으로 개행 후 남은 아이템, 컨테이너 항목 csv생성 후 개행 반복
	    				if(vIndex  == formItemList.size() - 1 && verticalRepeatData != null) { 
	    					dataSb.append(String.join(",", nullLine)); // hbl정보 + 첫번째 컨테이너 + 첫번째 아이템라인 append 
	    					dataSb.append(newLine); 
	    					
	    					String itemContStr = "";
	    					
	    					// hbl정보를 제외한 n개 컨테이너 , n개 아이템 라인 생성
	    					for (int j = 1; j < verticalRepeatData.size(); j++) {
	    						nullLine = resetLine.clone(); // nullLine 초기화 
	    						itemContStr = "";
    							repeatMap = verticalRepeatData.get(j);
    							for (Map<String, Object> itemInfo : verticalRepeatFormat) {
    								nullLine[Util.excelEng2int(itemInfo.get("TITLE_COL"))] = CreateExcelFile.convertCsvFormat(Util.null2str(repeatMap.get(verticalRepeatConvertMap.get(Util.null2str(itemInfo.get("ETC1"))))));
								}
    							/** VERTICAL_DOWN_TYPE가 2일경우 아이템로우에 오더내용 채우기 위해 추가 */
								if(verticalDownType.equals("2")){
									for(Map<String, Object> orderFormItem : orderFormItemList){
										etc1Field = Util.null2str(orderFormItem.get("ETC1"));
										colNum = Util.excelEng2int(orderFormItem.get("TITLE_COL"));
										if("".equals(etc1Field) && "NUM".equals(orderFormItem.get("DB_FIELD"))){
				    						etc1Field = "NUM";
				    					}
										if(dbField != null && !etc1Field.equals("") && etc1Field.indexOf(" AS ") > -1) { // ex) "CONSIGNEE_NAME_LOC AS CONSIGNEE_NAME_LOC2"
				    						String etc1Fields[] = etc1Field.split(" AS ");	// ex) ["CONSIGNEE_NAME_LOC", "CONSIGNEE_NAME_LOC2"]
				    						etc1Field = etc1Fields[etc1Fields.length - 1];	// ex) "CONSIGNEE_NAME_LOC2"
										}
				    					nullLine[colNum] = CreateExcelFile.convertCsvFormat(Util.null2str(data.get(etc1Field)));
									}
								}
	    						itemContStr = String.join(",", nullLine); // nullLine 배열을  ,로 구분한 String 값으로 만든후 변수에 담아준다.
	    						if(!"".equals(Util.null2str(itemContStr).replaceAll(",", ""))) { // ,를 제거한 값이 null이 아니면 dataSb에 append후 newLine으로 개행 
	    							dataSb.append(itemContStr);
	    							dataSb.append(newLine);
	    						}
	    						
							}
	    					
	    				}
		    			vIndex ++;
	    				
	    				
	    			}else if("H".equals(Util.null2str(vhType))) { //H: COL(세로) => 기존 가로 양식 	
	    				if(etc1Field.indexOf("__ITEM__") > -1) { // ex) DETAIL'_ITEM_'NAME__ITEM__2
	    					//num = Util.null2int(etc1Field.replace("__ITEM__", "").replaceAll("[^0-9]", ""));  // ex)DETAIL_ITEM_NAME__ITEM__'2'
	    					//dbField = etc1Field.replace("__ITEM__", "").replaceAll("[0-9]", "");     // ex) 'DETAIL_ITEM_NAME'__ITEM__2
							if(etc1Field.contains("CATEGORY_NAME")) {
								String[] parts = etc1Field.split("__ITEM__");   // ["CATEGORY_NAME_1", "1"]
								num = Util.null2int(parts[1]);                    // 반복번호만
								dbField = parts[0];                               // "CATEGORY_NAME_1" (숫자 보존)
							} else {
								num = Util.null2int(etc1Field.replace("__ITEM__", "").replaceAll("[^0-9]", ""));  // ex)DETAIL_ITEM_NAME__ITEM__'2'
								dbField = etc1Field.replace("__ITEM__", "").replaceAll("[0-9]", "");     // ex) 'DETAIL_ITEM_NAME'__ITEM__2
							}

							dbField = Util.null2str(itemConvertMap.get(dbField));
	    					
	    					if(itemRepeatList != null && itemRepeatList.size() > 0) {
	    						for (int j = 0; j < itemRepeatList.size(); j++) {
	    							repeatMap = itemRepeatList.get(j);
	    							if(num == Util.null2int(itemRepeatList.get(j).get("NUM"))) {
	    								dbValue = Util.null2str(repeatMap.get(dbField));
		    							break;
	    							}
	    							repeatMap.clear();
	    							dbValue = "";
	    						}
	    					}else {
	    						dbValue = "";
	    					}
	    				}else if(etc1Field.indexOf("__CONTAINER__") > -1) { // ex) GWT'__CONTAINER__'1
    						etc1Field = etc1Field.replace("__CONTAINER__", "");
    						num = Util.null2int(etc1Field.charAt(etc1Field.length() - 1)); //ex) GWT__CONTAINER__'1' => blfiled number 값
    						dbField = etc1Field.substring(0, etc1Field.length() - 1); //ex) 'GWT'__CONTAINER__1
    						dbField = Util.null2str(contConvertMap.get(dbField));
    						
	    					if(contRepeatList != null && contRepeatList.size() > 0) {
	    						for (int j = 0; j < contRepeatList.size(); j++) {
	    							repeatMap = contRepeatList.get(j);
	    							if(num == (Util.null2int(contRepeatList.get(j).get("NUM")))) {  // "NUM" 값과 blfiled number 값이 같은것만 값 세팅 
	    								dbValue = Util.null2str(repeatMap.get(dbField));
	    								break;
	    							}
	    							repeatMap.clear();
	    							dbValue = "";
	    						}
	    					}else {
	    						dbValue = "";
	    					}
	    				}else if(etc1Field.indexOf("__MULTI__") > -1) {
    						etc1Field = etc1Field.replace("__MULTI__", "");
    						num = Util.null2int(etc1Field.charAt(etc1Field.length() - 1));
    						dbField = etc1Field.substring(0, etc1Field.length() - 1);
    						dbField = Util.null2str(multiConvertMap.get(dbField));
    						
	    					if(multiRepeatList != null && multiRepeatList.size() > 0) {
	    						for (int j = 0; j < multiRepeatList.size(); j++) {
	    							repeatMap = multiRepeatList.get(j);
	    							if(num == (Util.null2int(multiRepeatList.get(j).get("NUM")))) {  // "NUM" 값과 blfiled number 값이 같은것만 값 세팅 
	    								dbValue = Util.null2str(repeatMap.get(dbField));
	    								break;
	    							}
	    							repeatMap.clear();
	    							dbValue = "";
	    						}
	    					}else {
	    						dbValue = "";
	    					}
	    				}else {
	    					if("ETC".equals(itemMap.get("ITEM_TYPE"))){
	    						dbValue = Util.null2str(itemMap.get("DESC_MESSAGE"));
	    					}else{
	    						if("".equals(etc1Field) && "NUM".equals(itemMap.get("DB_FIELD"))){
		    						etc1Field = "NUM";
		    					}
		    					if(dbField != null && !etc1Field.equals("") && etc1Field.indexOf(" AS ") > -1) { // ex) "CONSIGNEE_NAME_LOC AS CONSIGNEE_NAME_LOC2"
		    						String etc1Fields[] = etc1Field.split(" AS ");	// ex) ["CONSIGNEE_NAME_LOC", "CONSIGNEE_NAME_LOC2"]
		    						etc1Field = etc1Fields[etc1Fields.length - 1];	// ex) "CONSIGNEE_NAME_LOC2"
								}
		    					dbValue = Util.null2str(data.get(etc1Field));
		    					if("".equals(Util.null2str(dbValue))) dbValue = "";
	    					}
	    					
    					}
	    				
	    				dbValue = CreateExcelFile.convertCsvFormat(dbValue);

	    				if(colNum == 0) {
	    					dataSb.append(dbValue);
	    				}else {
	    					dataSb.append("," + dbValue);
	    				}		
	    				
	    			}// vhType END
	    			
				}//itemInfoList	    		
	    		
				bw.write(String.valueOf(dataSb));
				if("H".equals(Util.null2str(vhType))) { //H: COL(세로)만 해당 , V: ROW(가로)양식은 따로 newLine 작성 	
					bw.write(newLine);	
					dataSb.append(newLine);
				}
				if (i % 100 == 0) bw.flush();
	    		
			}// datas.size()		

			bw.flush();
			bw.close();
		} catch (Exception e) {
			log.error(Util.getPrintStacTraceString(e));
			bw.close();
		}
		

		
		if("".equals(saveFile)){ // 엑셀파일 생성
			currDtm = DateTimeUtil.getDate("yyyyMMddhhmmss");
			saveFile = fileName + "_" + currDtm + ".xlsx";
			if(Common.checkSaveFilenameForExcel(fileName)) {
				String uuid = UUID.randomUUID().toString();
				saveFile = uuid + "_" + currDtm + ".xlsx";
			}
			returnMap.put("saveFile",  dir + saveFile);
			returnMap.put("originFile", originFile);
		}else {
			returnMap.put("saveFile",  dir + saveFile);
			returnMap.put("originFile", originFile);
			if("V".equals(Util.null2str(vhType))) {
				 returnMap.put("columnCount", columnCount);
			}
		}
		returnMap.put("saveFile", StringEscapeUtils.escapeHtml4(returnMap.get("saveFile").toString()));
		returnMap.put("originFile", StringEscapeUtils.escapeHtml4(returnMap.get("originFile").toString()));
		
		return returnMap;
		
	}		

	/** BMS통관구분 변경 */
	public void updateBmsCdType(List<Map<String, Object>> updateCdTypeList) throws Exception{
		SqlSession sqlSession = null;
		int seq = 1;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			CommonMapper commonBatchMapper = sqlSession.getMapper(CommonMapper.class);
			
			for(Map<String, Object> updateCdTypeMap : updateCdTypeList) {
				commonBatchMapper.updateBmsCdType(updateCdTypeMap);
				if( (seq++ % 1000) == 0){
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
	/** BMS 통관목록 변경시 수입화물진행정보 실행 */
	public void insertBmsCargoProgress(Map<String, Object> updateCdTypeMap) throws Exception{
		List<Map<String, Object>> updateCdTypeList = new ArrayList<Map<String, Object>>();
		updateCdTypeList.add(updateCdTypeMap);
		this.insertBmsCargoProgress(updateCdTypeList);
	}
	public void insertBmsCargoProgress(List<Map<String, Object>> updateCdTypeList) throws Exception{
		String key = "E-COMMERCE_CARGO_PROGRESS_INSERT";
		ListOperations<String, String> listOperations = redisTemplate.opsForList();
		JSONObject json = new JSONObject();
		/** CD_TYPE : 변경전 / cdType : 변경후 */
		for(Map<String, Object> updateCdTypeMap : updateCdTypeList) {
			if("Y".equals(updateCdTypeMap.get("CD_TYPE")) && "Y".equals(updateCdTypeMap.get("BMS_IN_PLAN_YN"))){
				json.put("entCode", updateCdTypeMap.get("ENT_CODE"));
				json.put("entBusinessCode", updateCdTypeMap.get("ENT_BUSINESS_CODE"));
				json.put("bound", updateCdTypeMap.get("BOUND"));
				json.put("boundType", updateCdTypeMap.get("BOUND_TYPE"));
				json.put("customs", updateCdTypeMap.get("CUSTOMS_CODE"));
				json.put("status", updateCdTypeMap.get("LAST_POD_STATUS"));
				json.put("odJobNo", updateCdTypeMap.get("OD_JOB_NO"));
				json.put("masterOdJobNo", updateCdTypeMap.get("MASTER_OD_JOB_NO"));
				json.put("mblNo", updateCdTypeMap.get("MBL_NO"));
				json.put("hblNo", updateCdTypeMap.get("HBL_NO"));
				json.put("arDate", Util.null2str(updateCdTypeMap.get("AR_DATE")));
				json.put("dropYn", updateCdTypeMap.get("DROP_YN"));
				json.put("cdType", updateCdTypeMap.get("cdType"));
				json.put("destCode", updateCdTypeMap.get("DEST_CODE"));
				json.put("destPort", updateCdTypeMap.get("DEST_PORT"));
				if(!Util.null2str(updateCdTypeMap.get("DEST_CODE")).equals(Util.null2str(updateCdTypeMap.get("DEST_PORT")))){
					json.put("bondedCargoYn", "Y");
				}else{
					json.put("bondedCargoYn", "N");
				}
				json.put("action", "sendGw");
				json.put("mrn", updateCdTypeMap.get("MRN"));
				listOperations.rightPush(key, json.toString());
			}
		}
	}
	
	/**
	 * 통관정보 url 호출을 위한 데이터 암호화정보 조회
	 * @param info
	 * @throws Exception
	 */
	public CustomsClearanceInfo getCustomsClearanceInfo(CustomsClearanceInfo info) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String key = "tO/SdUc9hkSKc9rt+drUGA==";
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

		byte[] keyBytes= new byte[16];
		byte[] b= key.getBytes("UTF-8");

		int len= b.length;
		String mblNo = info.getMblNo();
		String hblNo = info.getHblNo();
		
		if (len > keyBytes.length) len = keyBytes.length;
		System.arraycopy(b, 0, keyBytes, 0, len);
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
		cipher.init(Cipher.ENCRYPT_MODE,keySpec,ivSpec);

//		BASE64Encoder encoder = new BASE64Encoder();
		Base64 encoder = new Base64();
		
		info.setMblNo(new String(encoder.encode(cipher.doFinal(mblNo.getBytes("UTF-8")))));
		info.setHblNo(new String(encoder.encode(cipher.doFinal(hblNo.getBytes("UTF-8")))));
		
		return info;
	}
	
	/**
	 * 하우스번호, 오더번호로 OD_JOB_NO 조회
	 * @param map
	 * @throws Exception
	 */
	public Map<String, Object> getOdJobNo(SearchOdJobNo search) throws Exception {
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "srchScanNoType", false, null, null, null, null);
		tmpMap = commonMapper.getOdJobNo(search);
		
		if(tmpMap != null && tmpMap.size() > 0){ // 스캔 데이터 존재 O
			resultMap.putAll(tmpMap);
			resultMap.put("scanNo", search.getSrchScanNo()); 
			resultMap.put("scanNoType", search.getSrchScanNoType());
			resultMap.put("registerYn", "Y");
			resultMap.put("PRINT_YN", tmpMap.get("PRINT_YN"));
			resultMap.put("PRINT_DATE", tmpMap.get("PRINT_DATE"));
		}else { // 스캔 데이터 존재 X
			resultMap.put("scanNo", search.getSrchScanNo());
			resultMap.put("scanNoType", search.getSrchScanNoType());
			resultMap.put("registerYn", "N");
			resultMap.put("PRINT_YN", "");
			resultMap.put("PRINT_DATE", "");
		}
		
		return resultMap;
	}
	

	/**
	 * 카카오톡 전송
	 * @param map
	 * @return
	 * @throws java.lang.Exception
	 */
	public void sendKakaoAlarmGroup(Map<String, Object> map, String groupId) throws Exception {
		sendKakaoAlarmGroup(map, groupId, false);
	}
	public void sendKakaoAlarmGroup(Map<String, Object> map, String groupId, boolean customer) throws Exception {
		//map data => entCode, entBusinessCode, gmtTime, userId, odJobNo, bound, boundType, 전송시 변환할 데이터
		try {
			Util.accessControlDatabase(map, "gmtTime", false, null, null, null, null);
			String entCode = Util.null2str(map.get("entCode"));
			String userId = Util.null2str(map.get("userId"));
			
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			tmpMap.put("entCode", entCode);
			tmpMap.put("entBusinessCode", map.get("entBusinessCode"));
			if(customer){
				tmpMap.put("solution", "EMS");
				tmpMap.put("optionCode", "E12");
			}else{
				tmpMap.put("solution", "ECM");
				tmpMap.put("optionCode", "C15");
			}
			
			if("Y".equals(commonMapper.selectSystemOption(tmpMap))){
				tmpMap.put("groupId", groupId);
				List<Map<String, Object>> mailLogList = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> userList = commonMapper.selectAlarmGroupUser(tmpMap);
				Map<String, Object> kakaoInfo = commonMapper.selectAlarmGroupKakaoInfo(tmpMap);
				if(kakaoInfo == null || userList == null || userList.size() == 0){
					//TODO 미처리 로그?
					return;
				}
				
				boolean sendMailFlag = false;
				StringBuffer receiver = new StringBuffer();
				String subject = Util.null2str(kakaoInfo.get("SUBJECT"));
				String sendMsg = Util.null2str(kakaoInfo.get("TEMPLATE"));
				String templateCode = Util.null2str(kakaoInfo.get("TEMPLATE_CODE"));
				
				//TODO 테스트
				subject = subject.replace("#{B/L No}", Util.null2str(map.get("blNo")));
				sendMsg = sendMsg.replace("#{B/L No}", Util.null2str(map.get("blNo")));
				if(customer){
					sendMsg = sendMsg.replace("#{송하인}", Util.null2str(map.get("SHIPPER_NAME")));
					sendMsg = sendMsg.replace("#{픽업주소}", Util.null2str(map.get("SHIPPER_ADDRESS")));
					sendMsg = sendMsg.replace("#{상품명}", Util.null2str(map.get("ITEM_NAME")));
					sendMsg = sendMsg.replace("#{담당자}", Util.null2str(map.get("SHIPPER_ATTN")));
					sendMsg = sendMsg.replace("#{전화번호}", Util.null2str(map.get("SHIPPER_TEL")));
				}
				
				/** mail전송(ALARM_TYPE:E) */
				for(int i = userList.size() - 1; i >= 0; i--){
					if("S".equals(userList.get(i).get("ALARM_TYPE"))){
						break;
					}
					receiver.append("@@" + Util.null2str(userList.get(i).get("EMAIL")));
					sendMailFlag = true;
					userList.get(i).put("seqNoId", commonMapper.selectMailSeqNoId(i));
					mailLogList.add(userList.remove(i));
				}
				if(sendMailFlag){
					MailSmtpSender sender = new MailSmtpSender(host, port, username, EncryptionUtils.decryptAES(password, rawKey));
					String senderName = profile.toUpperCase();
					if(senderName.contains("PROD")){
						senderName = "G1Express";
					}
					try {
						//TODO 테스트중 주석
//						sender.sendSmtpMail("GONE@YHT.CO.KR", "G1Express", receiver.toString().substring(2).split("@@"), subject, sendMsg);
						map.put("sendYn", "Y");
						map.put("sendStatus", "SEND");
					}catch(Exception e){
						String sendStatusRemark = e.getMessage();
						if (sendStatusRemark.length() > 500) {
							sendStatusRemark = sendStatusRemark.substring(0, 480);
						}
						map.put("sendYn", "N");
						map.put("sendStatus", "FAIL");
						map.put("sendStatusRemark", sendStatusRemark);
					}
				}
				
				/** 알림톡전송 */
				if(userList.size() > 0){
					kakaoInfo.putAll(commonMapper.selectEnterpriseInfo(entCode));
					if(Util.isEmpty(kakaoInfo.get("SMS_USER_ID")) || Util.isEmpty(kakaoInfo.get("SMS_CID_NUM")) || Util.isEmpty(kakaoInfo.get("SMS_SEND_TEL_NO")) || Util.isEmpty(kakaoInfo.get("TEMPLATE_CODE"))){
						//TODO 미처리 로그?
						return;
					}
					
					tmpMap.clear();
					tmpMap.put("gmtTime", map.get("gmtTime"));
					tmpMap.put("entCode", entCode);
					tmpMap.put("entBusinessCode", "");
					tmpMap.put("updateUserId", userId);
					tmpMap.put("solution", "ECM");
					tmpMap.put("stockType", "SMS");
					tmpMap.put("stockSign", "SMS");
					tmpMap.put("stockSeqLength", 8);
					tmpMap.put("dateLength", null);
					tmpMap.put("signType", null);
					tmpMap.put("stockDateFormat", null);
					tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
					tmpMap.put("stockRemark", "ALARM-JOB NO");
					int cnt = 1;
					String msgType = Util.null2str(kakaoInfo.get("MSG_TYPE"));
					String resendMsg = Util.null2str(kakaoInfo.get("RESEND_TEMPLATE"));
					String KakaoNextType = Util.null2str(kakaoInfo.get("RESEND_MSG_TYPE"));
					StringBuffer sendData = new StringBuffer();
					
					//TODO 테스트
					resendMsg = resendMsg.replace("#{B/L No}", Util.null2str(map.get("blNo")));
					if(customer){
						resendMsg = resendMsg.replace("#{송하인}", Util.null2str(map.get("SHIPPER_NAME")));
						resendMsg = resendMsg.replace("#{픽업주소}", Util.null2str(map.get("SHIPPER_ADDRESS")));
						resendMsg = resendMsg.replace("#{상품명}", Util.null2str(map.get("ITEM_NAME")));
						resendMsg = resendMsg.replace("#{담당자}", Util.null2str(map.get("SHIPPER_ATTN")));
						resendMsg = resendMsg.replace("#{전화번호}", Util.null2str(map.get("SHIPPER_TEL")));
					}
					
					sendData.append("<?xml version='1.0' encoding='UTF-8' standalone='yes'?><root><AccessIdentifier><UserID>" + Util.null2str(kakaoInfo.get("SMS_USER_ID")) + "</UserID><CidNum>" + Util.null2str(kakaoInfo.get("SMS_CID_NUM")) + "</CidNum></AccessIdentifier>");
					for(Map<String, Object> userMap : userList){
						tmpMap.put("seqNo", cnt);
						userMap.put("seqNoId", commonMapper.selectNextStockMaxNo(tmpMap));
						sendData.append("<SendData><Seq>" + cnt++ + "</Seq><MsgType>" + msgType + "</MsgType><SendType>1</SendType><DstAddr>" + Util.null2str(userMap.get("TEL")) + "</DstAddr><CallBack>" + Util.null2str(kakaoInfo.get("SMS_SEND_TEL_NO")) + "</CallBack>");
						sendData.append("<Subject>" + subject + "</Subject><TextType>0</TextType><Message>" + sendMsg + "</Message><Message2>" + resendMsg + "</Message2>");
						sendData.append("<KakaoTemplate>" + templateCode + "</KakaoTemplate><KakaoNextType>" + KakaoNextType + "</KakaoNextType><KakaoAdFlag>N</KakaoAdFlag>");
						sendData.append("<FileCnt>0</FileCnt></SendData>");
					}
					sendData.append("</root>");
					
					//TODO 테스트중 주석
//					URL url = new URL("http://sms.mcube.ne.kr/if/sms/registSmsMsgInfo.do");
//					HttpURLConnection con = (HttpURLConnection) url.openConnection();
//					con.setConnectTimeout(5000);
//					con.setReadTimeout(5000);
//					con.setRequestMethod("POST");
//					con.setRequestProperty("Content-Type", "text/plain");
//					con.setDoInput(true);
//					con.setDoOutput(true);
//					con.setUseCaches(false);
//					con.setDefaultUseCaches(false);
//					
//					String AESPWD = "tO/SdUc9hkSKc9rt+drUGA==";
//					String rtn = EncryptionUtils.encryptAES(sendData.toString(), AESPWD, "");
//					
//					OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
//					wr.write(rtn);
//					wr.flush();
//					
//					int responseCode = con.getResponseCode();
//					if (responseCode == HttpURLConnection.HTTP_OK) {
//						BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
//						StringBuilder sb = new StringBuilder();
//						String line = "";
//						while ((line = br.readLine()) != null) {
//							sb.append(line);
//						}
//						String responseStr = EncryptionUtils.decryptAES(sb.toString(), AESPWD, "");
//						
//						int seq = 1;
//						String msgKey = "";
//						String returnCodeDetail = "";
//						String returnMessageDetail = "";
//						
//						DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//						DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//						Document doc = dBuilder.parse(new InputSource(new StringReader(responseStr)));
//						doc.getDocumentElement().normalize();
//						
//						NodeList returnCode = doc.getElementsByTagName("ReturnCode");
//						if(returnCode.item(0).getTextContent().equals("F")){	/** T : 성공, F : 실패 */
//							//TODO 미처리 로그?
////							NodeList returnMessage = doc.getElementsByTagName("ReturnMessage");
////							returnMap.put("errStr", returnMessage.item(0).getTextContent());
//						}else{
//							NodeList messageList = doc.getElementsByTagName("Message");
//							if(messageList.getLength() > 0){
//								for (int i = 0; i < messageList.getLength(); i++){
//									Node messageNode = messageList.item(i);
//									NodeList ChildList = messageNode.getChildNodes();
//									for (int j = 0; j < ChildList.getLength(); j++){
//										Node messageChild = ChildList.item(j);
//										if(messageChild.getNodeName().equals("Seq")){
//											seq = Util.null2int(messageChild.getTextContent());
//										}else if(messageChild.getNodeName().equals("ReturnCodeDetail")){
//											returnCodeDetail = messageChild.getTextContent();
//										}else if(messageChild.getNodeName().equals("ReturnMessageDetail")){
//											returnMessageDetail = messageChild.getTextContent();
//										}else if(messageChild.getNodeName().equals("MsgKey")){
//											msgKey = messageChild.getTextContent();
//										}
//									}
//									Map<String, Object> dataMap = userList.get(seq - 1);
//									if(returnCodeDetail.equals("T")){	/** T : 성공, F : 실패 */
//										dataMap.put("msgKey", msgKey);
//									}else{
//										dataMap.put("returnMessageDetail", returnMessageDetail);
//									}
//								}
//							}
//						}
//					}else{
//						//TODO 미처리 로그?
////						returnMap.put("errStr", "송신 에러코드 : " + responseCode);
//					}
				}
				
				SqlSession sqlSession = null;
				try {
					sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
					CommonMapper commonBatchMapper = sqlSession.getMapper(CommonMapper.class);
					if(customer){
						map.put("solution", "EMS");
					}else {
						map.put("solution", "ECM");
					}
					
					if(sendMailFlag){
						map.put("sendType", "EMAIL");
						map.put("mailType", groupId);
						map.put("mailCodeRef", map.get("odJobNo"));
						map.put("MAIL_SENDER_ID", "GONE@YHT.CO.KR");
						map.put("MAIL_SENDER_NAME", userId);
						map.put("MAIL_SUBJECT", subject);
						map.put("MAIL_CONTENTS", sendMsg);
						
						for(Map<String, Object> dataMap : mailLogList){
							map.put("seqNoId", dataMap.get("seqNoId"));
							map.put("MAIL_RECEIVER_ID", dataMap.get("EMAIL"));
							commonBatchMapper.insertMailLog(map);
						}
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
					
					if(userList.size() > 0){
						map.put("templateType", kakaoInfo.get("TEMPLATE_TYPE"));
						map.put("templateCode", templateCode);
						
						for(Map<String, Object> dataMap : userList){
							map.put("attnId", dataMap.get("NAME"));
							map.put("attnHp", dataMap.get("TEL"));
							map.put("seqNoId", dataMap.get("seqNoId"));
							commonBatchMapper.insertAlarmGroupKakaoResult(map);
						}
						
						sqlSession.flushStatements();
						sqlSession.clearCache();
					}
					sqlSession.commit();
				} catch(Exception e){
					throw e;
				}finally{
					sqlSession.close();
				}
			}
		} catch(Exception e){
			log.error(Util.getPrintStacTraceString(e));
			ediCommonService.asyncErrorSendEmail(groupId + " Send Error", BeanUtils.describe(map), e);
		}
	}

	/**
	 * 주소에서 기타정보 조회
	 * @param search
	 * @throws Exception
	 */
	public AddressInfo getAddressInfo(SearchAddressInfo search) throws Exception {
		/*
		YUHAN TECHNOS CO.,LTD.15TH FLOOR. SAEHAN BLDG.1
		GDUK-DONG,MAPO-KU, SEOUL 121-710. KOREA A
		ADDR 33
		ATTN: 담당자
		TEL: 02-6277-8080
		*/
		AddressInfo addressInfo = new AddressInfo();
		
		String addressStr = Util.null2str(search.getSrchAddress());
		if(!addressStr.equals("")){
			String strSeparator = "\n";
			String strTempAddressTelFax = addressStr.replace(strSeparator, "%%").replace("TEL:", "%%TEL:");
			String[] arrAddressTelFax = strTempAddressTelFax.split("%%");
			String strTempTelFaxAttn = "";  //원본 주소에서 전화번호, 팩스번호, 담당자가 들어있는 문자열을 더한 하나의 문자열.
			String strTempTel = "";
			String strTempFax = "";
			String strFixedStreetAddress = "";
			String strFixedTelNo = "";
			String strFixedFaxNo = "";
			String strFixedAttn = "";
			String strFixedAMS_AMS_Place = "";
			String strFixedAMS_AMS_Street = "";
			String strFixedAMS_AMS_PostCode = "";
			String strFixedAMS_AMS_StateCode = "";
			String strFixedAMS_AMS_StateName = "";
			String strFixedAMS_AMS_CountryCode = "";
			String strFixedAMS_AMS_CountryName = "";
			
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			tmpMap.put("entCode", search.getSrchEntCode());
			
			//각각의 주소배열중 전화번호, 팩스번호, 담당자 검색여부
			boolean bFindTelFaxAttn = false;
			//배열중 전화번호와 팩스번호 분리 보통 "TEL:0000-0000 FAX:0000-0000"형식임
			for(String strTempString : arrAddressTelFax){
				if("".equals(strTempTel) && strTempString.toUpperCase().contains("TEL")){
					strTempTel = strTempString;
					bFindTelFaxAttn = true;
				}
				if("".equals(strTempFax) && strTempString.toUpperCase().contains("FAX")){
					strTempFax = strTempString;
					bFindTelFaxAttn = true;
				}
				if("".equals(strFixedAttn) && (strTempString.toUpperCase().contains("ATTN:") || strTempString.toUpperCase().contains("ATTN."))){
					strFixedAttn = strTempString;
					bFindTelFaxAttn = true;
				}
				if(!bFindTelFaxAttn){
					if(!"".equals(strTempString.trim())) {
						strFixedStreetAddress += strTempString + " ";
					}
				}
				bFindTelFaxAttn = false;
			}

			//조회된 전화번호, 팩스번호, 담당자가 포함된 문자열을 합한다.
			if (strTempTel == strTempFax){
				strTempTelFaxAttn = strTempTel + " " + strFixedAttn;
			}else{
				strTempTelFaxAttn = strTempTel + " " + strTempFax + " " + strFixedAttn;
			}

			//확인된 정보를 할당하기 위하여 초기화한다.
			strTempTel = ""; strTempFax = ""; strFixedAttn = "";
			if (strTempTelFaxAttn.contains("TEL") && !strTempTelFaxAttn.contains("TEL:")){
				strTempTelFaxAttn = strTempTelFaxAttn.replace("TEL", "TEL:");
			}
			if (strTempTelFaxAttn.contains("FAX") && !strTempTelFaxAttn.contains("FAX:")){
				strTempTelFaxAttn = strTempTelFaxAttn.replace("FAX", "FAX:");
			}
			
			//팩스번호가 전화번호와 분리되어 있는 경우
			if(!"".equals(strTempTelFaxAttn)){
				strTempTelFaxAttn = strTempTelFaxAttn.toUpperCase().replace("TEL:", "%%TEL:").replace("FAX:", "%%FAX:").replace("ATTN:", "%%ATTN:").replace("ATTN.", "%%ATTN:").replace("+"," ");
				arrAddressTelFax = strTempTelFaxAttn.split("%%");
				for (String strTempString : arrAddressTelFax){
					if (strTempString.length() >= 4 && "TEL:".equals(strTempString.substring(0, 4))){
						if ("".equals(strFixedTelNo)) {
							strFixedTelNo = strTempString.replace("TEL:", "").replace("//","");
						}
					}
					if(strTempString.length() >= 4 && "FAX:".equals(strTempString.substring(0, 4))){
						if("".equals(strFixedFaxNo)){
							strFixedFaxNo = strTempString.replace("FAX:", "");
						}
					}
					if (strTempString.length() >= 5 && "ATTN:".equals(strTempString.substring(0, 5))){
						if ("".equals(strFixedAttn)){
							strFixedAttn = strTempString.replace("ATTN:", "");
						}
					}
				}
			}
			
			//전화번호/팩스번호/담당자를 제회한 문자열에서 "999-999, 9999-9999" 형태의 우편번호 분리
			Pattern p = Pattern.compile("(\\d{3,4}(-)\\d{3,4})"); 
			Matcher m = p.matcher(strFixedStreetAddress); 
			int matchCount=0;
			while (m.find()) {
				if(matchCount == 0 ){
					strFixedAMS_AMS_PostCode = m.group().trim();
					strFixedStreetAddress = strFixedStreetAddress.replace(strFixedAMS_AMS_PostCode, " ");
					matchCount++;
				}else{
					break;
				}
			}

			//선행으로 먼저 찾아서 불필요한 쿼리 안타도록 수정 2016-10-11 HIT 
			if (strFixedStreetAddress.toUpperCase().contains("KOREA")){// 한국
				strFixedAMS_AMS_CountryCode = "KR";
				strFixedStreetAddress = strFixedStreetAddress.toUpperCase().replace("KOREA", " ");
				strFixedAMS_AMS_CountryName = "KOREA";
			}else if(strFixedStreetAddress.toUpperCase().contains("UNITED STATES OF AMERICA")){ // 미국추가
				strFixedAMS_AMS_CountryCode = "US";
				strFixedStreetAddress = strFixedStreetAddress.toUpperCase().replace("UNITED STATES OF AMERICA", " ");
				strFixedAMS_AMS_CountryName = "U.S.A";
			}else if(strFixedStreetAddress.toUpperCase().contains("USA")){// 미국추가
				strFixedAMS_AMS_CountryCode = "US";
				strFixedStreetAddress = strFixedStreetAddress.toUpperCase().replace("USA", " ");
				strFixedAMS_AMS_CountryName = "U.S.A";
			} else if(strFixedStreetAddress.toUpperCase().contains("U.S.A")){// 미국추가
				strFixedAMS_AMS_CountryCode = "US";
				strFixedStreetAddress = strFixedStreetAddress.toUpperCase().replace("U.S.A", " ");
				strFixedAMS_AMS_CountryName = "U.S.A";
			}
	            
			//국가코드조회
			if("".equals(strFixedAMS_AMS_CountryCode)){
				tmpMap.put("groupCode", "COUNTRY");
				List<Map<String, Object>> tempDomesticCodeList = commonMapper.getDomesticCodeList(tmpMap); // 
				if(tempDomesticCodeList != null && !tempDomesticCodeList.isEmpty()){
					for(int i = 0; i<tempDomesticCodeList.size(); i++){
						if (strFixedStreetAddress.toUpperCase().contains(Util.null2str(tempDomesticCodeList.get(i).get("NAME")).toUpperCase())){
							strFixedAMS_AMS_CountryCode = Util.null2str(tempDomesticCodeList.get(i).get("CODE"));
							strFixedStreetAddress = strFixedStreetAddress.toUpperCase().replace(Util.null2str(tempDomesticCodeList.get(i).get("NAME")).toUpperCase(), " ");
							strFixedAMS_AMS_CountryName = Util.null2str(tempDomesticCodeList.get(i).get("NAME")).toUpperCase();
							break;
						}
					}
				}
			}
			
			//국가코드를 못 찾은 경우 주요도시명으로 한 번 더 검색한다.
			if("".equals(strFixedAMS_AMS_CountryCode)){
				for(int i=0; i < CodeDefinition.amsAddressCountryCode.length; i++){
					if (strFixedStreetAddress.toUpperCase().contains(Util.null2str(CodeDefinition.amsAddressCountryCode[i][0]).toUpperCase())){
						strFixedAMS_AMS_CountryCode = Util.null2str(CodeDefinition.amsAddressCountryCode[i][1]);
						strFixedAMS_AMS_Place = Util.null2str(CodeDefinition.amsAddressCountryCode[i][0]).toUpperCase();
						break;
					}
				}
			}
			
			tmpMap.put("groupCode", "EDI_STATE");
			//국가를 찾은 경우 해당 국가의 주코드 자료만 조회한다.
			if(strFixedAMS_AMS_CountryCode.length() == 2){
				tmpMap.put("strFixedAMS_AMS_CountryCode", strFixedAMS_AMS_CountryCode);
			}
			List<Map<String, Object>> tempDomesticCodeStateList = commonMapper.getDomesticCodeList(tmpMap); 
			String strTempCode = "";
			if (tempDomesticCodeStateList != null && !tempDomesticCodeStateList.isEmpty()){
				for(int i = 0; i<tempDomesticCodeStateList.size(); i++){
					strTempCode = Util.null2str(tempDomesticCodeStateList.get(i).get("CODE"));
					if (strFixedStreetAddress.toUpperCase().contains(Util.null2str(tempDomesticCodeStateList.get(i).get("NAME")).toUpperCase())){
						strFixedAMS_AMS_StateCode = strTempCode;
						strFixedStreetAddress = strFixedStreetAddress.toUpperCase().replace(Util.null2str(tempDomesticCodeStateList.get(i).get("NAME")).toUpperCase(), " ");
						strFixedAMS_AMS_StateName = Util.null2str(tempDomesticCodeStateList.get(i).get("NAME")).toUpperCase();
						break;
					}
					if(strFixedStreetAddress.toUpperCase().contains(" " + strTempCode + " " )){
						strFixedAMS_AMS_StateCode = strTempCode;
						strFixedStreetAddress = strFixedStreetAddress.toUpperCase().replace(" " + strTempCode + " ", " ");
						strFixedAMS_AMS_StateName = Util.null2str(tempDomesticCodeStateList.get(i).get("NAME")).toUpperCase();
						break;
					}
				}
			}
	            
			//AMS Street에 전화번호, 팩스번호, 담당자를 제외한 주소를 할당한다.
			strFixedAMS_AMS_Street = strFixedStreetAddress;
			//분리한 결과값 할당
			//전화번호 및 팩스번호에서 부정확한 문자 제거. [허용문자=>,.-()+ ]
			strFixedTelNo = this.GetStandardTelFaxNumber(strFixedTelNo, "STANDARD");
			strFixedFaxNo = this.GetStandardTelFaxNumber(strFixedFaxNo, "STANDARD");
			if("".equals(strFixedAMS_AMS_Place)){
				if (strFixedAMS_AMS_Street.length() > 35){
					strFixedAMS_AMS_Place = strFixedAMS_AMS_Street.substring(35, strFixedAMS_AMS_Street.length()).trim();
					strFixedAMS_AMS_Street = strFixedAMS_AMS_Street.substring(0, 35).trim();
				}
			}else{
				/** TB_ECM_OD.CONSIGNEE_STREET(nvarchar(35)) 오류 발생해서 35자리 까지만 처리되도록 수정 */
				if (strFixedAMS_AMS_Street.length() > 35){
					strFixedAMS_AMS_Street = strFixedAMS_AMS_Street.substring(0, 35).trim();
				}
			}

			addressInfo.setFax(Util.null2str(strFixedFaxNo));
			addressInfo.setAttn(Util.null2str(strFixedAttn));
			addressInfo.setZipcode(Util.null2str(strFixedAMS_AMS_PostCode));
			addressInfo.setCountryCode(Util.null2str(strFixedAMS_AMS_CountryCode));
			addressInfo.setCountryName(Util.null2str(strFixedAMS_AMS_CountryName));
			addressInfo.setStateCode(Util.null2str(strFixedAMS_AMS_StateCode));
			addressInfo.setStateName(Util.null2str(strFixedAMS_AMS_StateName));
			addressInfo.setStreet(Util.null2str(strFixedAMS_AMS_Street));
			addressInfo.setTel(Util.null2str(strFixedTelNo));
			if(Util.null2str(strFixedAMS_AMS_Place).length() > 60){
				addressInfo.setPlace(Util.null2str(strFixedAMS_AMS_Place.substring(0, 60)));
			}else{
				addressInfo.setPlace(Util.null2str(strFixedAMS_AMS_Place));
			}
			
		}
		
		return addressInfo;
	}
	
	/** 
	 * 송화인/수화인주소에서 기타정보추출
	 * 주소원문  ex)SANGJIN MACTECH CO.,LTD.\n378-5, 1F SAMSAN-DONG, BUPYUNG-GU,  INCHEON,KOREA\n(TEL.82-32-522-9720, FAX.82-32-522-9677)
	 * 분할된 주소의 구분자 "\n"
	 */
//	public void getAmsAddressInfo(Order order, String addressType, String saveType) throws java.lang.Exception {
//		
//	}
	
	/** 전화번호 및 팩스번호를 확인하여 부정확한 문자를 제거하고 표준형태로 변환한다. */
	public String GetStandardTelFaxNumber(String sTelFaxNo, String sStyle){
		String strReturnValue = "";
		char[] chArray = sTelFaxNo.toCharArray();
		try{
			for(char cTemp : chArray){
				switch (sStyle.toUpperCase()){
					case "NUMBER":
						if (cTemp == ' ' || Character.isDigit(cTemp)) {
							strReturnValue += cTemp;
						}
						break;
					case "NUMBER_STYLE":    // " - 허용
						if (cTemp == ' ' || cTemp=='-' || Character.isDigit(cTemp)) {
							strReturnValue += cTemp;
						}
						break;
					default:
						if (cTemp == '+' || cTemp == '-' || cTemp == ')' || cTemp == '(' || cTemp == '-' || cTemp == '.' || cTemp == ',' || cTemp == ' ' || Character.isDigit(cTemp)) {
							strReturnValue += cTemp;
						}
						break;
				}
			}

			//표준 형태로 변형을 원한경우. 추후 작업한다. 2013.03.26
			if (sStyle=="STANDARD"){
				//국내 : 지역번호 + 전화번호  ex) 02-9999-9999
				//해외 : +국가번호 + 0을제외한지역번호 + 전화번호 ex) +82-2-9999-9999
			}
		}catch (Exception ex){
			strReturnValue = sTelFaxNo;
		}
		return strReturnValue;
	}
	
	public Row getRow(Sheet sh, int idx) {
		if(sh.getRow(idx) != null) {
			return sh.getRow(idx);
		}else {
			Row row = sh.createRow(idx);
			row.setHeightInPoints(15.00f);
			row.setHeight((short) 300);
			return row;
		}
	}
	
	/**
	 * email 로그 저장
	 * @param request
	 * @return 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void saveMailLogs(HttpServletRequest request, Map reqMap) throws java.lang.Exception {
		List<Map<String, Object>> refNoList 	= (List<Map<String, Object>>) reqMap.get("refNoList");
		List<Map<String, Object>> emailList 		= (List<Map<String, Object>>) reqMap.get("emailList");
		List<Map<String, Object>> solutionList 		= (List<Map<String, Object>>) reqMap.get("solutionList");
		List<Map<String, Object>> boundTypeList 	= (List<Map<String, Object>>) reqMap.get("boundTypeList");
		List<Map<String, Object>> boundList 		= (List<Map<String, Object>>) reqMap.get("boundList");
		List<Map<String, Object>> argList 			= (List<Map<String, Object>>) reqMap.get("argList");
		List<Map<String, Object>> attFilenameList 	= (List<Map<String, Object>>) reqMap.get("attFilenameList");
		List<Map<String, Object>> rptSubPathList 	= (List<Map<String, Object>>) reqMap.get("rptSubPathList");
		List<Map<String, Object>> rptFilenameList 	= (List<Map<String, Object>>) reqMap.get("rptFilenameList");
		//List<Map<String, Object>> rptArgList2 	= (List<Map<String, Object>>) map.get("rptArg2List");
		String[] rptArgList2 = request.getParameterValues("rptArg2List[]");  // 파라미터로 직접 전달 
		if(rptArgList2 == null) rptArgList2 = request.getParameterValues("rptArg2List");
		
		reqMap.put("mailContents", Util.null2str(reqMap.get("MAIL_CONTENTS")).replaceAll("\n", "<BR>"));
		List<Map<String, Object>> mailLogList = new ArrayList<Map<String, Object>>();
		Map<String, Object> mailLog = null;
		
		if (emailList != null) {
			for (int i = 0; i < emailList.size(); i++) {
				mailLog = new ConcurrentHashMap<String, Object>();
				mailLog.put("mailCodeRef", refNoList.get(i));

				mailLog.put("MAIL_SENDER_ID", reqMap.get("MAIL_SENDER_ID"));
				mailLog.put("MAIL_SENDER_NAME", reqMap.get("MAIL_SENDER_NAME"));
				mailLog.put("MAIL_RECEIVER_ID", emailList.get(i));
				mailLog.put("mailCarbonCopyId", reqMap.get("mailCarbonCopyId"));
				mailLog.put("MAIL_SUBJECT", reqMap.get("MAIL_SUBJECT"));
				mailLog.put("MAIL_CONTENTS", Util.null2str(reqMap.get("MAIL_CONTENTS")).replaceAll("\n", "<BR>"));
				//mailLog.put("mailContentsPrefix", Util.null2str(reqMap.get("mailContentsPrefix")).replaceAll("\n", "<BR>"));
				//mailLog.put("mailContentsSuffix", Util.null2str(reqMap.get("mailContentsSuffix")).replaceAll("\n", "<BR>"));

				mailLog.put("solution", solutionList.get(i));
				mailLog.put("bound", boundList.get(i));
				mailLog.put("boundType", boundTypeList.get(i));
				mailLog.put("sendType", reqMap.get("sendType"));
				mailLog.put("mailType", reqMap.get("mailType"));
				mailLog.put("userId", reqMap.get("userId"));
				mailLog.put("gmtTime", reqMap.get("gmtTime"));
				mailLog.put("workMode", reqMap.get("workMode"));
				mailLog.put("entCode", reqMap.get("entCode"));
				mailLog.put("entBusinessCode", reqMap.get("entBusinessCode"));
				mailLog.put("rptSubPath", rptSubPathList.get(i));
				mailLog.put("attFilename", attFilenameList.get(i));
				mailLog.put("rptFilename", rptFilenameList.get(i));
				mailLog.put("rptArg1", argList.get(i));
				//mailLog.put("rptArg2", Util.null2str(reqMap.get("rptArg2")));
				if(rptArgList2 != null && rptArgList2.length > 0) mailLog.put("rptArg2", 	rptArgList2[i]);
				mailLog.put("rptType", reqMap.get("rptType"));
				mailLog.put("sendYn", "N");
				mailLog.put("sendStatus", "STANDBY");
				mailLog.put("seqNoId", commonMapper.selectMailSeqNoId(i));

				mailLogList.add(mailLog);
			}
		}
		// 메일 저장
		for (Map<String, Object> map : mailLogList) {
			List<Map<String, Object>> atchList = new ArrayList<Map<String, Object>>();
			atchList.add(map);
			
			commonMapper.insertMailLog(map);

			for (Map<String, Object> atchFile : atchList) {
				atchFile.put("seqNoId", map.get("seqNoId"));
				commonMapper.insertMailLogDetail(atchFile);
			}
		}
				
	}
	
	/**
	 * Mail Log 에 있는 SEND_TYPE == 'EMAIL' && SEND_YN == 'N' && SEND_STATUS == 'STANDBY' 인 목록을 가져와 메일을
	 * 보낸다.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> sendEmail(Map<String, Object> map) throws Exception {
		Map<String, Object> returnMap = new HashMap<String, Object>();
		MailSmtpSender sender = new MailSmtpSender(host, port, username, EncryptionUtils.decryptAES(password, rawKey));
		String devLive 	= profile.toUpperCase();
		byte[] rawKey = null;
		List<Map<String, Object>> mailList = null;
		List<Map<String, Object>> result = new ArrayList<>();
		
		Map<String, Object> searchMap = new HashMap<>();
		searchMap.put("sendYn", 		"N");
		searchMap.put("sendStatus", 	"STANDBY");
		searchMap.put("sendType", 		"EMAIL");

		mailList = commonMapper.selectMailLogList(searchMap);

		for (Map<String, Object> mailMap : mailList) {
			
			mailMap.put("seqNoId", mailMap.get("SEQ_NO_ID"));
			
			List<Map<String, Object>> atachList = commonMapper.selectMailAttchList(mailMap);
			List<File> fileList = new ArrayList<File>();
			Map<String, Object> rptArg2Map = new HashMap<String, Object>();

			for (Map<String, Object> attchMent : atachList) {
				String rptType = Util.null2str(attchMent.get("RPT_TYPE"));

							
				if ("CLREARANCE_PRINT".equals(rptType)) {
					
				} else if ("ATCH".equals(rptType)) { // 첨부파일인 경우

					log.debug("ATCH --- "+ (String) attchMent.get("RPT_SUB_PATH"));

					File atchFile = new File((String) attchMent.get("RPT_SUB_PATH"));
					if (atchFile.exists()) {
						String originFileName = atchFile.getParent() + "/" + (String) attchMent.get("RPT_FILENAME");

						originFileName = getRename(originFileName);

						File dest = new File(originFileName);
						atchFile.renameTo(dest);
						fileList.add(dest);
					}
				}

				if (!"ATCH".equals(rptType)) {
					
					attchMent.put("ubi_ent_code", 	(String) map.get("entCode"));
					attchMent.put("ubi_arg", 		attchMent.get("RPT_ARG1"));
					attchMent.put("reportFileDir", 	attchMent.get("RPT_SUB_PATH"));		//rpt 파일 sub 경로..ems,pnd...
					attchMent.put("ubi_jrf", 		attchMent.get("RPT_FILENAME"));
					
					log.debug("==========================ATT_FILENAME ========================"+(String)attchMent.get("ATT_FILENAME"));
					
					//G1 소스.사용안함  String attFileName = (String)attchMent.get("ATT_FILENAME") +"_"+Util.getNow("yyyyMMddhhmmssSSS")  +".pdf";
					String attFileName = (String)attchMent.get("ATT_FILENAME") +"_"+Util.getNow("yyMMddHHmm")  +".pdf";// 저장시 파일명.
					log.debug("==========================ATT_FILENAME ========================"+attFileName);

					Map<String, Object> downFile = getReportResultFile(attchMent);
					
					File file = new File((String) downFile.get("fileDir") + (String) downFile.get("fileName"));
					
					if (file.exists()) {
						File renameFile = new File((String) downFile.get("fileDir") + attFileName);
						file.renameTo(renameFile);
						file.delete();
						fileList.add(renameFile);
					}
				}
			}

			String mailType = Util.null2str(mailMap.get("MAIL_TYPE"));

			File[] atchFileList = new File[fileList.size()];

			int i = 0;
			for (File file : fileList) {
				atchFileList[i++] = file;
			}
			
			String sendYn = "Y";
			String sendStatus = "SEND";
			String sendStatusRemark = "";
			String mailSenderId 		= Util.null2str(mailMap.get("MAIL_SENDER_ID"));
			String mailSenderName 		= Util.null2str(mailMap.get("MAIL_SENDER_NAME"));
			String[] mailReceiverIds 	= Util.null2str(mailMap.get("MAIL_RECEIVER_ID")).split(";");
			String[] mailCarbonCopyIds 	= Util.null2str(mailMap.get("MAIL_CARBON_COPY_ID")).split(";");
			String mailSubject 			= Util.null2str(mailMap.get("MAIL_SUBJECT"));
			String mailContents			= Util.null2str(mailMap.get("MAIL_CONTENTS"));
			String entCode 				= Util.null2str(mailMap.get("ENT_CODE"));
			String entBusinessCode 		= Util.null2str(mailMap.get("ENT_BUSINESS_CODE"));
			String entOfficeCode 		= Util.null2str(mailMap.get("ENT_OFFICE_CODE"));
			String registerUserId 		= Util.null2str(mailMap.get("REGISTER_USER_ID"));
			
			mailMap.put("mailSubject", 			mailSubject);
			mailMap.put("mailContentsPrefix", 	Util.null2str(mailMap.get("MAIL_CONTENTS_PREFIX")).replaceAll("\n", "<br/>").replaceAll(" ", "&nbsp;"));
			if ("STATUS".equals(mailType)) {
				mailMap.put("mailContents", 		Util.null2str(mailMap.get("MAIL_CONTENTS")).replaceAll("\n", "<br/>"));
			} else {
				mailMap.put("mailContents", 		Util.null2str(mailMap.get("MAIL_CONTENTS")).replaceAll("\n", "<br/>").replaceAll(" ", "&nbsp;"));
			}
			mailMap.put("mailContentsSuffix", 	Util.null2str(mailMap.get("MAIL_CONTENTS_SUFFIX")).replaceAll("\n", "<br/>").replaceAll(" ", "&nbsp;"));
			mailMap.put("entCode", 				entCode);
			mailMap.put("entBusinessCode", 		entBusinessCode);
			mailMap.put("entOfficeCode", 		entOfficeCode);
			mailMap.put("gmtTime", 				Util.null2str(map.get("gmtTime")));
			String domain  =  customProperties.getPreUrl();
			//logger.debug("==========================domain ========================" + domain);
			mailMap.put("domain", 				domain);  
			mailMap.put("mailSendDate", 		DateTime.getToday());
			
//			rawKey = EncryptionUtils.getRawKey(Util.null2str(mailMap.get("mailSendDate")));
			mailMap.put("encMailCodeRef", 		EncryptionUtils.encryptAES(Util.null2str(mailMap.get("MAIL_CODE_REF")), Util.null2str(mailMap.get("mailSendDate"))));
			mailMap.put("encSolution", 			EncryptionUtils.encryptAES(Util.null2str(mailMap.get("SOLUTION")), Util.null2str(mailMap.get("mailSendDate"))));
			mailMap.put("encEntCode", 			EncryptionUtils.encryptAES(entCode, Util.null2str(mailMap.get("mailSendDate"))));
			mailMap.put("encEntBusinessCode", 	EncryptionUtils.encryptAES(entBusinessCode, Util.null2str(mailMap.get("mailSendDate"))));
			mailMap.put("encRegisterUserId", 	EncryptionUtils.encryptAES(registerUserId, Util.null2str(mailMap.get("mailSendDate"))));
			mailMap.put("lang",mailMap.get("LANGUAGE_CODE"));
			
			if (mailReceiverIds.length == 0) {
				mailReceiverIds = null;
			}
			if (mailCarbonCopyIds.length == 0) {
				mailCarbonCopyIds = null;
			}
			if (atchFileList.length == 0) {
				atchFileList = null;
			}
			String cache = UUID.randomUUID().toString();
			mailMap.put("cache", cache);
			
			try {
				sender.sendSmtpMail(mailSenderId, mailSenderName, mailReceiverIds, mailCarbonCopyIds, null, mailSubject, mailContents, atchFileList);
			} catch (Exception e) {
				sendYn = "N";
				sendStatus = "FAIL";
				sendStatusRemark = e.getMessage();
				if (sendStatusRemark.length() > 500) {
					sendStatusRemark = sendStatusRemark.substring(0, 480);
				}
			} finally {
				if (atchFileList != null) {
					int len = atchFileList.length;
					for (int k = 0; k < len; k++) {
						if (atchFileList[k].exists()) {
							atchFileList[k].delete();
						}
					}
				}
			}

			// 전송결과 update;
			mailMap.put("sendYn", 			sendYn);
			mailMap.put("sendStatus", 		sendStatus);
			mailMap.put("sendStatusRemark", sendStatusRemark);
			
			result.add(mailMap);
		}
		
		boolean sendOk = true;
		for (Map<String, Object> resultMap : result) {
			if ("FAIL".equals(resultMap.get("sendStatus"))) {
				sendOk = false;
				break;
			}
		}
		
		returnMap.put("sendStatus", sendOk);
		returnMap.put("result", result);
		
		return returnMap;
	}
	
	public String getRename(String originFileName) {
		if (FileUtil.isExists(originFileName)) {
			int dotIndex = originFileName.lastIndexOf(".");
			String extName = "";
			String fileName = "";
			if (dotIndex != -1) {
				fileName = originFileName.substring(0, originFileName.lastIndexOf("."));
				extName = originFileName.substring(originFileName.lastIndexOf("."), originFileName.length());
			}
			originFileName = fileName + "(1)" + extName;
			originFileName = getRename(originFileName);
		}

		return originFileName;
	}
	
	/**
	 * UBI 출력물 생성
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	public Map<String,Object> getReportResultFile(Map<String,Object> params){
		Map<String,Object> result = new HashMap<String,Object>();
//		String jrf_dir = customProperties.getReportJrfTopLoc() + Util.null2str(params.get("report_FileDir")) + "/" ; 			// 리포트 파일 위치.
		String jrf_dir = customProperties.getReportJrfTopLoc(); 			// 리포트 파일 위치.
		
		log.info("reportFileDir========================"+Util.null2str(params.get("RPT_SUB_PATH")));
		
		String entCode = Util.null2str(params.get("ubi_ent_code"));
		String jrfFileName = Util.null2str(params.get("RPT_FILENAME")) + ".jrf";												// 리포트 파일명.
		//String ds	= "dataSource_yhtxx"; // 테스트 
		//String ds = "jdbc/exps_yhtxx"; //  운영
		String ds = customProperties.getReportDs() + entCode.toLowerCase();														// WAS에 설정된 DataSource 명. ubl/UbiService/ ubiservice.xml
		String file_url = customProperties.getReportUrl();																		// 리포트에서 사용되는 이미지 또는 공통 아이템 정보를 가져오기위한 정보.
		String ubi_serverUrl = customProperties.getReportDomain() 	+ "/UbiServer";												// 리포트에서 사용되는 이미지 또는 공통 아이템 정보를 가져오기위한 정보.
		String servlet_url1 = customProperties.getReportDomain() 	+ "/UbiForm";												//"/UbiForm";							// Form 서블릿 URL.
		String servlet_url2 = customProperties.getReportDomain() 	+ "/UbiData";												//"/UbiData";							// Data 서블릿 URL.
		String args = Util.null2str(params.get("RPT_ARG1"));					    												// 아규먼트 정보.
//		String today = DateTime.getToday();
//		String datePath = "/"+today.substring(0,4)+"/"+today.substring(5,7)+"/"+today.substring(8,10)+"/";
		String export_dir = customProperties.getBackupDir()+"ubi/"; // TODO 경로 미정, 임시로 백업 폴더 경로로 잡음
		String export_file = Util.null2str(params.get("ATT_FILENAME"))+DateTimeUtil.getNow("yyyyMMddhhmmssSSS")+".pdf";
		String ext = "ecm";
		boolean isSuccess = false;
		
		log.info("FileUtil.isExists========================"+FileUtil.isExists(export_dir));
		if(!FileUtil.isExists(export_dir)){
			FileUtil.createDirectorys(export_dir);
		}
		
		try {
			UbiViewer ubiviewer = new UbiViewer(false, false);	// 객체 생성
			
			log.info("file_url========================"+file_url);
			log.info("ubiServerURL===================="+ubi_serverUrl);
			log.info("jrf_dir========================="+jrf_dir);
			log.info("jrfFileName====================="+jrfFileName);
			log.info("args============================"+args);
			log.info("ds=============================="+ds);
			log.info("export_dir======================"+export_dir);
			log.info("export_file====================="+export_file);
			
			ubiviewer.fileURL 		= file_url;
			ubiviewer.ubiServerURL 	= ubi_serverUrl;
			ubiviewer.isLocalFile	= true;
			ubiviewer.jrfFileDir 	= jrf_dir;
			ubiviewer.jrfFileName 	= jrfFileName;
			ubiviewer.arg 			= args;
			ubiviewer.dataSource 	= ds;
			ubiviewer.setExportParams("PDF", export_dir + export_file);
			ubiviewer.exectype 		= "TYPE6";
			ubiviewer.fontRevision 	= true;
			ubiviewer.setFontPath(customProperties.getReportFontLoc());		//WAS가 설치된 서버의 OS가 Non-Windows인 경우 폰트 정보를 가져오는 부분을 설정.
			ubiviewer.isDebug 		= true;
			log.info("["+export_file+"] loadReport Start");
			isSuccess = ubiviewer.loadReport(); // 파일 저장 수행.
			log.info("["+export_file+"] loadReport End / isSuccess: " + isSuccess);
			
			result.put("isSuccess", isSuccess);
			result.put("fileName", export_file);
			result.put("fileDir", export_dir);
		}catch(Exception e) {
			result.put("isSuccess", false);
			e.printStackTrace();
		}
		
		return result;
	}
	public void updateEmailData(List<Map> result) throws Exception {
		String  mailType;
		for (Map<String, Object> mailMap : result) {
			mailType = Util.null2str(mailMap.get("MAIL_TYPE"));
			commonMapper.updateMailLog(mailMap);
		}
	}
}
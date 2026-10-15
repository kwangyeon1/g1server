package org.yht.exps.express.service.eCommerce;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
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
import org.yht.exps.express.mapper.eCommerce.ConsolMapper;
import org.yht.exps.express.mapper.eCommerce.ManifestFileMapper;
import org.yht.exps.express.mapper.eCommerce.OrderMapper;
import org.yht.exps.express.mapper.eCommerce.UpsMngMapper;
import org.yht.exps.express.mapper.edi.ManifestMapper;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.eCommerce.order.ManifestFile;
import org.yht.exps.express.model.eCommerce.order.ManifestFileList;
import org.yht.exps.express.model.eCommerce.order.Order;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.eCommerce.order.search.SearchManifestFileList;
import org.yht.exps.express.model.edi.SendManifest;
import org.yht.exps.express.model.edi.search.SearchManifestCustoms;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.edi.EdiManifestGwSendService;
import org.yht.util.DateTime;
import org.yht.util.DateTimeUtil;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class ManifestFileService {

	@Autowired
	ManifestFileMapper manifestFileMapper;
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	ManifestMapper manifestMapper;
	@Autowired
	OrderMapper orderMapper;
	@Autowired
	ConsolMapper consolMapper;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	@Autowired
	UpsMngMapper upsMngMapper;
	
	@Resource(name = "fileService")
	FileService fileService;
	@Resource(name = "orderService")
	OrderService orderService;
	@Resource(name = "commonService")
	CommonService commonService;
	@Resource(name = "ediManifestGwSendService")
	EdiManifestGwSendService ediManifestGwSendService;
	@Value("${spring.profiles.active}")
	private String profile;
	
	/**
	 * 적하목록 파일업로드 배제리스트
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ManifestFileList selectExclusionList(SearchManifestFileList search) throws Exception {
		ManifestFileList manifestFileList = new ManifestFileList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		if(search.getSort() != null && search.getSort().size() > 0 && "FCODE".equals(search.getSort().get(0).getField())){
			search.getSort().get(0).setField("F_CODE");
		}
		
		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))){
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
		
		Util.accessControlDatabase(search, "srchDateFormat", false, "srchDateGbn", "cdb.REGISTER_DATE_TIME","srchNoType", "cdb.HBL_NO");
		List<ManifestFile> list  = manifestFileMapper.selectExclusionList(search);
		manifestFileList.setManifestFileList(list);
		if(list.size() > 0){
			manifestFileList.setTotal(list.get(0).getTotal());
		}else {
			manifestFileList.setTotal(0);
		}
		return manifestFileList;
	}
	
	/**
	 * 적하목록 배제 파일업로드
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public FileUploadResponse uploadCdBInfo(String entCode, String userId, String gmtTime, String uploadType, MultipartFile multipartFile) throws Exception {
		FileUploadResponse result = new FileUploadResponse(FileUploadResponse.RESULT_OK);
		if(multipartFile.getSize() > 1024 * 1024 * 8){
			result.setResult(FileUploadResponse.RESULT_SIZE_OVER);
			result.setResultText("The file size exceeds the upload size.");
		}else if(!fileService.checkExtension(multipartFile, new String[]{"xlsx", "xls"})){ // 파일 확장자 유효성 검사
			result.setResult("extError");
		}else{
			if(!multipartFile.isEmpty()){
				result = fileService.storeFile(multipartFile, entCode, false, true);
				if(FileUploadResponse.RESULT_OK.equals(result.getResult())){
					
					StringBuffer errMsg = new StringBuffer();
					List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
					File file = fileService.loadFileAsResource(result.getFileName(), entCode, false).getFile();
					InputStream is = null;
					try {
						is = new FileInputStream(file);
						Workbook wb = WorkbookFactory.create(is);
						Sheet sheet = wb.getSheetAt(0);
						Row row;
						int lastRow = sheet.getLastRowNum();
						DataFormatter df = new DataFormatter();
						String extension;
						String fieldValue = "";
						if(file.getName().indexOf(".xlsx") > -1){
							extension = "xlsx";
						}else{
							extension = "xls";
						}
						
						
						String[] fieldName = null;
						String[] fieldTitle = null;
						String[] fieldLength = null;
						String[] fieldType = null;
						int[] cellidx = null;
						if("exclusion".equals(uploadType)){
							fieldName =		new String[]	{"F_CODE",	"HBL_NO",			"OLD_COMMODITY_ITEM",	"NEW_COMMODITY_ITEM",	"CONSIGNEE_NAME",	"CONSIGNEE_HP",	"CONSIGNEE_ADDRESS",	"ALLOWED_ITEM_CODE",	"URL",		"AMOUNT_CUR",	"CURRENCY_CODE",	"CUSTOMS_ID",		"CD_USE_TYPE"};
							fieldTitle =	new String[]	{"Reason",	"ShipmentNumber",	"GoodsName",			"품명",					"Sname",			"Sphone",		"Sbuilding",			"품명코드",				"SiteURL",	"Amount",		"CurrencyCode",		"개인통관고유부호",	"용도구분"};
							fieldLength =	new String[]	{"pass",	"20",				"300",					"pass",					"100",				"50",			"300",					"10",					"300",		"17.4",			"3",				"50",				"20"};
							fieldType =		new String[]	{"S",		"S",				"SN",					"SN",					"SN",				"S",			"SN",					"S",					"S",		"DBL",			"S",				"S",				"S"};
							cellidx =		new int[]		{1,			2,					3,						4,						5,					6,				12,						15,						17,			20,				21,					23,					24};
						}else{
							fieldName =		new String[]	{"HBL_NO",	"CUSTOMS_ID",		"CD_USE_TYPE",		"CONSIGNEE_ADDRESS_LOC",	"CONSIGNEE_NAME_LOC",	"CONSENT",	"CONSIGNEE_HP",		"CONSIGNEE_ZIPCODE"};
							fieldTitle =	new String[]	{"HBLNo",	"pCustomsNumber",	"CustomsUseType",	"ConsingeeAddressLocal",	"ConsingeeNameLocal",	"Consent",	"ConsingeeTel",		"ConsigneeZipcode"};
							fieldLength =	new String[]	{"20",		"50",				"20",				"300",						"100",					"1",		"50",				"10"};
							fieldType =		new String[]	{"S",		"S",				"S",				"SN",						"SN",					"S",		"S",				"S"};
							cellidx =		new int[]		{0,			1,					2,					3,							4,						5,			6,					7};
						}
						
						int fieldSize = fieldName.length;
						Map<String, Object> dataMap = null;
						
						if("xls".equals(extension)){
							HSSFCell myCell = null;
							for(int i = 1; i < lastRow + 1; i++){
								row = sheet.getRow(i);
								if(row == null){
									continue;
								}
								dataMap = new HashMap<String, Object>();
								for(int j = 0; j < fieldSize; j++){
									myCell = (HSSFCell)sheet.getRow(i).getCell(cellidx[j], MissingCellPolicy.CREATE_NULL_AS_BLANK);
									if(myCell.getCellType() == CellType.FORMULA) {
										fieldValue = Util.null2str(orderService.dataChk(myCell.getStringCellValue(), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i + 1, false)).trim().replace("'", "");
									}else{
										fieldValue = Util.null2str(orderService.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i + 1, false)).trim().replace("'", "");
									}
									if(!fieldValue.equals("")){
										dataMap.put(fieldName[j], fieldValue);
									}
								}
								if(!Util.null2str(dataMap.get("HBL_NO")).equals("")){
									dataMap.put("entCode", entCode);
									dataMap.put("userId", userId);
									dataMap.put("gmtTime", gmtTime);
									dataMap.put("uploadType", uploadType);
									dataList.add(dataMap);
								}
							}
						}else{
							XSSFCell myCell = null;
							for(int i = 1; i < lastRow + 1; i++){
								row = sheet.getRow(i);
								if(row == null){
									continue;
								}
								dataMap = new HashMap<String, Object>();
								for(int j = 0; j < fieldSize; j++){
									myCell = (XSSFCell)sheet.getRow(i).getCell(cellidx[j], MissingCellPolicy.CREATE_NULL_AS_BLANK);
									if(myCell.getCellType() == CellType.FORMULA) {
										fieldValue = Util.null2str(orderService.dataChk(myCell.getStringCellValue(), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i + 1, false)).trim().replace("'", "");
									}else{
										fieldValue = Util.null2str(orderService.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i + 1, false)).trim().replace("'", "");
									}
									
									if(!Util.null2str(fieldValue).equals("")){
										dataMap.put(fieldName[j], fieldValue);
									}
								}
								if(!Util.null2str(dataMap.get("HBL_NO")).equals("")){
									dataMap.put("entCode", entCode);
									dataMap.put("userId", userId);
									dataMap.put("gmtTime", gmtTime);
									dataMap.put("uploadType", uploadType);
									dataList.add(dataMap);
								}
							}
						}
					}catch(Exception e){
						throw e;
					}finally{
						if(is != null) is.close();
					}
					
					if(errMsg.length() != 0){
						result.setResult("dataErr");
						result.setResultText(errMsg.toString());
						return result;
					}
					
					//TODO 파일삭제?
					if(dataList.size() > 0){
						List<Map<String, Object>> updateOrderList = new ArrayList<Map<String, Object>>();
						Map<String, Object> updateOrderMap = new HashMap<String, Object>();
						String cdUseType, tmpStr;
						
						for(Map<String, Object> tmpMap : dataList){
							if(!"1".equals(tmpMap.get("F_CODE")) && !"A".equals(tmpMap.get("F_CODE"))){
								tmpMap.remove("F_CODE");
							}
							
							tmpStr = Util.null2str(tmpMap.get("NEW_COMMODITY_ITEM"));
							if(tmpStr.length() > 300){
								tmpMap.put("NEW_COMMODITY_ITEM", tmpStr.substring(0, 300));
							}
							
							if("개인".equals(tmpMap.get("CD_USE_TYPE"))){
								tmpMap.put("CD_USE_TYPE", "1");
							}else if("회사".equals(tmpMap.get("CD_USE_TYPE"))){
								tmpMap.put("CD_USE_TYPE", "2");
							}else{
								tmpMap.remove("CD_USE_TYPE");
							}
							
							if("customsId".equals(uploadType)){
								updateOrderMap = manifestFileMapper.selectExclusionOrderUpdate(tmpMap);
								if(updateOrderMap != null && updateOrderMap.size() > 0){
									updateOrderMap.putAll(tmpMap);
									cdUseType = Util.null2str(updateOrderMap.get("CD_USE_TYPE"), Util.null2str(updateOrderMap.get("CD_USE_TYPE_OLD")));
									if(cdUseType.equals("1")){
										if(Util.isEmpty(updateOrderMap.get("URL"))){
											updateOrderMap.put("ECM_TYPE", "D");
										}else{
											updateOrderMap.put("ECM_TYPE", "A");
										}
									}else{
										if("Y".equals(Util.null2str(updateOrderMap.get("DOCUMENT_YN")))){
											updateOrderMap.put("ECM_TYPE", "F");
										}else{
											updateOrderMap.put("ECM_TYPE", "E");
										}
										updateOrderMap.put("removeUrlYn", "Y");
									}
									updateOrderList.add(updateOrderMap);
								}
							}
						}
						
						SqlSession sqlSession = null;
						try {
							sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
							ManifestFileMapper manifestFileBatchMapper = sqlSession.getMapper(ManifestFileMapper.class);
							int cnt = 1;
							for(Map<String, Object> tmpMap : dataList){
								manifestFileBatchMapper.insertExclusion(tmpMap);
								if((cnt++ % 1000) == 0){
									sqlSession.flushStatements();
									sqlSession.clearCache();
								}
							}
							
							sqlSession.flushStatements();
							sqlSession.clearCache();
							/** 개인통관부호 엑셀업로드일경우 오더 데이터 업데이트 */
							if("customsId".equals(uploadType)){
								cnt = 1;
								for(Map<String, Object> tmpMap : updateOrderList){
									manifestFileBatchMapper.updateOrderExclusion(tmpMap);
									if((cnt++ % 1000) == 0){
										sqlSession.flushStatements();
										sqlSession.clearCache();
									}
								}
							}
							sqlSession.flushStatements();
							sqlSession.clearCache();
							sqlSession.commit();
						}catch(Exception e){
							throw e;
						}finally{
							sqlSession.close();
						}
					}
				}
			} else {
				result.setResult(FileUploadResponse.RESULT_NOT_FILE);
				result.setResultText("The file does not exist.");
			}
		}
		return result;
	}
	
	/**
	 * UPS 기초코드 파일업로드
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public FileUploadResponse uploadUpsBasic(String entCode, String userId, String gmtTime, String uploadType, MultipartFile multipartFile) throws Exception {
		FileUploadResponse result = new FileUploadResponse(FileUploadResponse.RESULT_OK);
		if(multipartFile.getSize() > 1024 * 1024 * 8){
			result.setResult(FileUploadResponse.RESULT_SIZE_OVER);
			result.setResultText("The file size exceeds the upload size.");
		}else{
			if(!multipartFile.isEmpty()){
				Map<String, Object> dataMap = null;
				result = fileService.storeFile(multipartFile, entCode, false, true);
				if(FileUploadResponse.RESULT_OK.equals(result.getResult())){
					
					StringBuffer errMsg = new StringBuffer();
					List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
					File file = fileService.loadFileAsResource(result.getFileName(), entCode, false).getFile();
					InputStream is = null;
					try {
						is = new FileInputStream(file);
						Workbook wb = WorkbookFactory.create(is);
						Sheet sheet = wb.getSheetAt(0);
						Row row;
						int lastRow = sheet.getLastRowNum();
						DataFormatter df = new DataFormatter();
						String extension;
						String fieldValue = "";
						if(file.getName().indexOf(".xlsx") > -1){
							extension = "xlsx";
						}else{
							extension = "xls";
						}
						
						String[] fieldName = null;
						String[] fieldTitle = null;
						String[] fieldLength = null;
						String[] fieldType = null;
						String dataChkField = "";
						String boundType = "IMP";
						int[] cellidx = null;
						if("A".contains(uploadType)){
							fieldName =		new String[]	{"ETC2"};
							fieldTitle =	new String[]	{"화주명"};
							fieldLength =	new String[]	{"100"};
							fieldType =		new String[]	{"SN"};
							cellidx =		new int[]		{0};
							dataChkField = "ETC2";
						}else if("B".contains(uploadType)){
							fieldName =		new String[]	{"ETC1",		"ETC2"};
							fieldTitle =	new String[]	{"거래구분",	"화주명"};
							fieldLength =	new String[]	{"2",			"100"};
							fieldType =		new String[]	{"S",			"SN"};
							cellidx =		new int[]		{0,				1};
							dataChkField = "ETC2";
							boundType = "EXP";
						}else if("E".contains(uploadType)){
							fieldName =		new String[]	{"HBL_NO",		"ETC1",			"ETC2",				"ETC3"};
							fieldTitle =	new String[]	{"HAWB No.",	"판매자구분",	"전자상거래주소",	"거래유형"};
							fieldLength =	new String[]	{"6",			"100",			"100",				"1"};
							fieldType =		new String[]	{"S",			"SN",			"SN",				"S"};
							cellidx =		new int[]		{0,				1,				2,					3};
							dataChkField = "HBL_NO";
						}else if("C".contains(uploadType) || "D".contains(uploadType)){
							fieldName =		new String[]	{"ETC1",		"ETC2"};
							fieldTitle =	new String[]	{"값",			"타입"};
							fieldLength =	new String[]	{"100",			"100"};
							fieldType =		new String[]	{"SN",			"SN"};
							cellidx =		new int[]		{0,				1};
							dataChkField = "ETC1";
						}else{
							fieldName =		new String[]	{"ETC1"};
							fieldTitle =	new String[]	{"화주명"};
							fieldLength =	new String[]	{"100"};
							fieldType =		new String[]	{"SN"};
							cellidx =		new int[]		{0};
							dataChkField = "ETC1";
						}
						
						int fieldSize = fieldName.length;
						if("xls".equals(extension)){
							HSSFCell myCell = null;
							for(int i = 1; i < lastRow + 1; i++){
								row = sheet.getRow(i);
								if(row == null){
									continue;
								}
								dataMap = new HashMap<String, Object>();
								for(int j = 0; j < fieldSize; j++){
									myCell = (HSSFCell)sheet.getRow(i).getCell(cellidx[j], MissingCellPolicy.CREATE_NULL_AS_BLANK);
									fieldValue = Util.null2str(orderService.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i, false)).trim().replace("'", "");
									if(!fieldValue.equals("")){
										dataMap.put(fieldName[j], fieldValue);
									}
								}
								if(!Util.isEmpty(dataMap.get(dataChkField))){
									dataMap.put("bound", "AIR");
									dataMap.put("boundType", boundType);
									dataMap.put("basicType", uploadType);
									dataMap.put("userId", userId);
									dataMap.put("gmtTime", gmtTime);
									if("A".equals(uploadType)){
										dataMap.put("ETC1", "H");
									}
									dataList.add(dataMap);
								}
							}
						}else{
							XSSFCell myCell = null;
							for(int i = 1; i < lastRow + 1; i++){
								row = sheet.getRow(i);
								if(row == null){
									continue;
								}
								dataMap = new HashMap<String, Object>();
								for(int j = 0; j < fieldSize; j++){
									myCell = (XSSFCell)sheet.getRow(i).getCell(cellidx[j], MissingCellPolicy.CREATE_NULL_AS_BLANK);
									fieldValue = Util.null2str(orderService.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i, false)).trim().replace("'", "");
									if(!Util.null2str(fieldValue).equals("")){
										dataMap.put(fieldName[j], fieldValue);
									}
								}
								if(!Util.isEmpty(dataMap.get(dataChkField))){
									dataMap.put("bound", "AIR");
									dataMap.put("boundType", boundType);
									dataMap.put("basicType", uploadType);
									dataMap.put("userId", userId);
									dataMap.put("gmtTime", gmtTime);
									if("A".equals(uploadType)){
										dataMap.put("ETC1", "H");
									}
									dataList.add(dataMap);
								}
							}
						}
					}catch(Exception e){
						throw e;
					}finally{
						if(is != null) is.close();
					}
					
					if("E".contains(uploadType)){
						dataMap = new HashMap<String, Object>();
						String hblNo;
						for(Map<String, Object> tmpMap : dataList){
							hblNo = Util.null2str(tmpMap.get("HBL_NO"));
							if(dataMap.get(hblNo) == null){
								dataMap.put(hblNo, "Y");
							}else{
								errMsg.append(hblNo + " : 중복되는값이 있습니다.<br />");
							}
						}
						
						if(errMsg.length() != 0){
							result.setResult("dataErr");
							result.setResultText(errMsg.toString());
							return result;
						}
						
						dataMap = new HashMap<String, Object>();
						dataMap.put("basicType", "E");
						for(int i = 0; i < dataList.size(); i+=1000){
							if(i + 1000 < dataList.size()) {
								dataMap.put("hblNoList", dataList.subList(i, i + 1000));
							}else{
								dataMap.put("hblNoList", dataList.subList(i, dataList.size()));
							}
							List<String> dupList = manifestFileMapper.selectUpsBasicDupCheck(dataMap);
							for(String hblNo2 : dupList){
								errMsg.append(hblNo2 + " : 이미 등록되어있습니다.<br />");
							}
						}
					}
					
					if(errMsg.length() != 0){
						result.setResult("dataErr");
						result.setResultText(errMsg.toString());
						return result;
					}
					
					//TODO 파일삭제?
					if(dataList.size() > 0){
						SqlSession sqlSession = null;
						try {
							sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
							ManifestFileMapper manifestFileBatchMapper = sqlSession.getMapper(ManifestFileMapper.class);
							int cnt = 1;
							for(Map<String, Object> tmpMap : dataList){
								manifestFileBatchMapper.insertUpsBasicMap(tmpMap);
								if((cnt++ % 1000) == 0){
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
							sqlSession.close();
						}
					}
				}
			} else {
				result.setResult(FileUploadResponse.RESULT_NOT_FILE);
				result.setResultText("The file does not exist.");
			}
		}
		return result;
	}
	
	/**
	 * 적하목록 배제 삭제
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult deleteExclusion(SearchManifestFileList search) throws Exception {
		manifestFileMapper.deleteExclusion(search);
		return new OrderSaveResult();
	}
	
	/**
	 * 수입적하목록 파일업로드 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult saveIFile(String entCode, SearchManifestFileList search, HttpServletRequest req) throws Exception {
		OrderSaveResult result = new OrderSaveResult();
		
		int maxHsn = 1;
		StringBuffer errorStr = new StringBuffer();
		Map<String, String> createHblData = new HashMap<String, String>();
		List<String> checkList = new ArrayList<String>();
		List<Map<String, Object>> selectHouseList = new ArrayList<Map<String, Object>>();
		Map<String, Object> ediMap;
		Map<String, String> checkMap = new HashMap<String, String>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		int stockSeqNo = 1;
		Map<String, Object> stockMap = new HashMap<String, Object>();
		stockMap.put("entCode", entCode);
		stockMap.put("gmtTime", search.getGmtTime());
		stockMap.put("entBusinessCode", "");
		stockMap.put("updateUserId", search.getUserId());
		stockMap.put("solution", "ECM");
		stockMap.put("stockType", "OD");
		stockMap.put("stockSeqLength", "8");
		stockMap.put("dateLength", "2");
		stockMap.put("signType", "noHyphen");
		stockMap.put("stockDateFormat", "12");
		stockMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		stockMap.put("stockRemark", "ORDER SIMPLE DOC NO");
		
		tmpMap.put("mblNo", search.getMblNo());
		tmpMap.put("bound", search.getBound());
		tmpMap.put("boundType", search.getBoundType());
		tmpMap.put("selectSimpleDocNo", "Y");
		tmpMap.put("selectMaxHsn", "Y");
		Map<String, Object> mblMap = manifestFileMapper.selectMasterInfo(tmpMap);
		List<ManifestFile> list = search.getManifestFileList();
		List<ManifestFile> insertList = new ArrayList<ManifestFile>();
		String simpleDocNoDocumentY = "", simpleDocNoDocumentN = "", consigneeName, consigneeTel;
		int strIdx;
		boolean selectExpressSignDocumentY = false, selectExpressSignDocumentN = false;
		
		
		if(mblMap != null){
			if(!"Y".equals(mblMap.get("FINISH_YN"))){
				resultMap.put("createCount", 0);
				resultMap.put("createHblData", createHblData);
				resultMap.put("errorStr", "마감되지않은 마스터번호 입니다. 마감 후 업로드 하십시오.");
				result.setResultData(resultMap);
				return result;
			}
			simpleDocNoDocumentY = Util.null2str(mblMap.get("SIMPLE_DOC_NO_DOCUMENT_Y"));
			simpleDocNoDocumentN = Util.null2str(mblMap.get("SIMPLE_DOC_NO_DOCUMENT_N"));
			maxHsn = Util.null2int(mblMap.get("MAX_HSN")) + 1;
			search.setMrn(Util.null2str(mblMap.get("MRN")));
			search.setMsn(Util.null2str(mblMap.get("MSN")));
			search.setLineCode(Util.null2str(mblMap.get("LINE_CODE")));
			search.setVesselFlight(Util.null2str(mblMap.get("VESSEL_FLIGHT")));
			search.setDestCode(Util.null2str(mblMap.get("DEST_CODE")));
			search.setOrigCode(Util.null2str(mblMap.get("ORIG_CODE")));
			search.setArDate(Util.null2str(mblMap.get("AR_DATE")));
			search.setObDate(Util.null2str(mblMap.get("OB_DATE")));
			search.setOrigName(Util.null2str(mblMap.get("ORIG_NAME")));
			search.setOrigCountry(Util.null2str(mblMap.get("ORIG_COUNTRY")));
			search.setDestName(Util.null2str(mblMap.get("DEST_NAME")));
			search.setDestPort(Util.null2str(mblMap.get("DEST_PORT")));
			search.setDestCountry(Util.null2str(mblMap.get("DEST_COUNTRY")));
			search.setCustomsCode(Util.null2str(mblMap.get("CUSTOMS_CODE")));
			search.setCustomsDptCode(Util.null2str(mblMap.get("CUSTOMS_DPT_CODE")));
			search.setDiscWhCode(Util.null2str(mblMap.get("DISC_WH_CODE")));
			search.setAssignmentWhCode(Util.null2str(mblMap.get("ASSIGNMENT_WH_CODE")));
			search.setEdiMailboxSeqNo(Util.null2str(mblMap.get("EDI_MAILBOX_SEQ_NO")));
			search.setOdJobNo(Util.null2str(mblMap.get("OD_JOB_NO")));
			search.setSelectorUserId(Util.null2str(mblMap.get("SELECTOR_USER_ID")));
			search.setConfirmerUserId(Util.null2str(mblMap.get("CONFIRMER_USER_ID")));
		}else{
			tmpMap.clear();
			tmpMap.put("entCode", entCode);
			tmpMap.put("groupCode", "CITY_PORT");
			tmpMap.put("srchCode", search.getOrigCode());
			Map<String, Object> portMap = commonMapper.getCodeDetail(tmpMap);
			if(portMap != null){
				search.setOrigName(Util.null2str(portMap.get("CODE_NAME")));
				search.setOrigCountry(Util.null2str(portMap.get("ETC2")));
			}
			tmpMap.put("srchCode", search.getDestCode());
			portMap = commonMapper.getCodeDetail(tmpMap);
			if(portMap != null){
				search.setDestName(Util.null2str(portMap.get("CODE_NAME")));
				search.setDestCountry(Util.null2str(portMap.get("ETC2")));
			}
		}
		
		if(simpleDocNoDocumentY.equals("")){
			selectExpressSignDocumentY = true;
		}
		if(simpleDocNoDocumentN.equals("")){
			selectExpressSignDocumentN = true;
		}
		
		tmpMap.clear();
		tmpMap.put("srchEntCode", entCode);
		tmpMap.put("srchEntBusinessCode", search.getEntBusinessCode());
		tmpMap.put("srchBound", search.getBound());
		tmpMap.put("srchEdiCityPort", search.getDestPort());
		ediMap = manifestFileMapper.selectBusEdiCodeInfo(tmpMap);
		
		double dblCustomsValue = 150;
		
		for(ManifestFile data : list){
			
			if(data.getOrigCountry().equals("US") || data.getOrigCountry().equals("PR")){
				dblCustomsValue = 200;
			}else{
				dblCustomsValue = 150;
			}
			
			if("Y".equals(data.getDocumentYn())){
				if(selectExpressSignDocumentY) {
					if(ediMap != null && ediMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y") != null){
						stockMap.put("stockSign", ediMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y"));
						stockMap.put("seqNo", stockSeqNo++);
						simpleDocNoDocumentY = Util.null2str(commonMapper.selectNextStockMaxNo(stockMap));
					}
					selectExpressSignDocumentY = false;
				}
			}else{
				if(selectExpressSignDocumentN) {
					if(ediMap != null && ediMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N") != null){
						stockMap.put("stockSign", ediMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N"));
						stockMap.put("seqNo", stockSeqNo++);
						simpleDocNoDocumentN = Util.null2str(commonMapper.selectNextStockMaxNo(stockMap));
					}
					selectExpressSignDocumentN = false;
				}
			}
			
			if(checkMap.get(data.getHblNo()) != null){
				errorStr.append(data.getHblNo() + " : 동일한 H.B/L이 존재합니다.<br />");
				checkMap.put(data.getHblNo(), "error");
			}else{
				checkMap.put(data.getHblNo(), "Y");
				checkList.add(data.getHblNo());
			}
			
			if(!Util.isEmpty(data.getCdTypeTemp())){
				if(data.getAmountCur() > dblCustomsValue){
					data.setCdType("N");
				}else{
					data.setCdType(data.getCdTypeTemp());
				}
			}
			
//			if(!selectExpressSignDocumentY && !selectExpressSignDocumentN){
//				break;
//			}
			
		}
		
		if(simpleDocNoDocumentY.equals("")){
			simpleDocNoDocumentY = null;
		}
		if(simpleDocNoDocumentN.equals("")){
			simpleDocNoDocumentN = null;
		}
		
		stockMap.put("stockSign", "COD");
		stockMap.put("stockSeqLength", 8);
		stockMap.put("dateLength", null);
		stockMap.put("signType", null);
		stockMap.put("stockDateFormat", null);
		stockMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		stockMap.put("stockRemark", "ORDER-JOB NO");
		if(mblMap == null){
			stockMap.put("seqNo", stockSeqNo++);
			search.setOdJobNo(commonMapper.selectNextStockMaxNo(stockMap));
			
			tmpMap.clear();
			tmpMap.put("srchEntCode", entCode);
			tmpMap.put("srchEntBusinessCode", search.getEntBusinessCode());
			tmpMap.put("srchBound", search.getBound());
			tmpMap.put("srchBoundType", search.getBoundType());
			tmpMap.put("srchEdiCityPort", search.getDestPort());
			tmpMap = commonMapper.selectBusEdiEcmInfo(tmpMap);
			if(tmpMap != null && tmpMap.size() > 0){
				search.setEdiMailboxSeqNo(Util.null2str(tmpMap.get("SEQ_NO")));
			}
			
			SearchManifestCustoms searchCustoms = new SearchManifestCustoms();
			searchCustoms.setBoundType(search.getBoundType());
			searchCustoms.setSrchBusEdiCodeYn("Y");
			searchCustoms.setEntBusinessCode(search.getEntBusinessCode());
			searchCustoms.setBound(search.getBound());
			searchCustoms.setEntCode(search.getSrchEntCode());
			searchCustoms.setPortCode(search.getDestCode());
			tmpMap = manifestMapper.getCustomsCode(searchCustoms);
			if(tmpMap != null){
				search.setCustomsCode(Util.null2str(tmpMap.get("CUSTOMS_CODE")));
				search.setCustomsDptCode(Util.null2str(tmpMap.get("CUSTOMS_DPT_CODE")));
				search.setDiscWhCode(Util.null2str(tmpMap.get("EDI_DISC_WH_CODE")));
				search.setAssignmentWhCode(Util.null2str(tmpMap.get("EDI_ASSIGNMENT_WH_CODE")));
			}
			
			if(ediMap != null){
				search.setSelectorUserId(Util.null2str(ediMap.get("EDI_SELECTOR_USER_ID")));
				search.setConfirmerUserId(Util.null2str(ediMap.get("EDI_CONFIRMER_USER_ID")));
			}
			
			tmpMap = new HashMap<String, Object>();
			tmpMap.put("entCode", entCode);
			tmpMap.put("entBusinessCode", search.getEntBusinessCode());
			tmpMap.put("solution", "ECM");
			tmpMap.put("optionCode", "C02");
			Map<String, Object> optionMap = commonMapper.selectSystemOption2(tmpMap);
			/** 항공일경우 중계사업자 설정값이 auto이면 편명의 중계사업자로 저장 */
			if("auto".equals(optionMap.get("SET_VALUE"))){
				String venderId = "";
				if(!Util.isEmpty(search.getLineCode())){
					tmpMap.put("groupCode", "AIRLINE_CODE");
					tmpMap.put("srchEtcFieldOnly", "Y");
					tmpMap.put("srchEtcField", "ETC2");
					tmpMap.put("srchEtcFieldValue", search.getLineCode());
					tmpMap = commonMapper.getCodeDetail(tmpMap);
					if(tmpMap != null){
						venderId = Util.null2str(tmpMap.get("ETC11"));
					}else{
						tmpMap = new HashMap<String, Object>();
					}
				}
				if(venderId.equals("")){
					search.setVenderId(Util.null2str(optionMap.get("DEFAULT_VALUE")));
				}else{
					search.setVenderId(venderId);
				}
			}else if(Util.isEmpty(optionMap.get("SET_VALUE"))){
				search.setVenderId(Util.null2str(optionMap.get("DEFAULT_VALUE")));
			}else{
				search.setVenderId(Util.null2str(optionMap.get("SET_VALUE")));
			}
			
			search.setSimpleDocNo(Util.null2str(simpleDocNoDocumentN, simpleDocNoDocumentY));
			search.setCustCodeLv1("UPS-IMP");
			search.setFinishYn("Y");
			manifestFileMapper.insertMasterOrder(search);
		}else{
			int houseCnt = checkList.size();
			tmpMap.clear();
			tmpMap.put("masterOdJobNo", mblMap.get("OD_JOB_NO"));
			for(int i = 0; i < houseCnt; i+=1000){
				if(i + 1000 < houseCnt) {
					tmpMap.put("hblNoList", checkList.subList(i, i + 1000));
				}else{
					tmpMap.put("hblNoList", checkList.subList(i, houseCnt));
				}
				selectHouseList.addAll(manifestFileMapper.selectHouseInfoList(tmpMap));
			}
			if(selectHouseList.size() != 0){
				for(Map<String, Object> selectHouse : selectHouseList){
					errorStr.append(Util.null2str(selectHouse.get("HBL_NO")) + " : 동일한 H.B/L이 존재합니다.<br />");
					checkMap.put(Util.null2str(selectHouse.get("HBL_NO")), "error");
				}
			}
		}
		
		List<Map<String, Object>> packingList;
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		String description = "NEW ORDER";
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		Map<String, Object> logMap;
		tmpLogMap.put("job_func_type", "ADD");
		tmpLogMap.put("ent_code", entCode);
		tmpLogMap.put("ent_business_code", search.getEntBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("bound", search.getBound());
		tmpLogMap.put("bound_type", search.getBoundType());
		tmpLogMap.put("process_user_id", search.getUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("ent_business_print_name", search.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", search.getLogUserName());
		tmpLogMap.put("process_user_name_eng", search.getLogUserNameEng());
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		if (req != null)	tmpLogMap.put("access_ip", Util.getClientIP(req));
		
		int seq = 1;
		tmpMap.clear();
		tmpMap.put("beforeOneYear", ediMap.get("BEFORE_ONE_YEAR"));
		tmpMap.put("nowDate", ediMap.get("NOW_DATE"));
		for (ManifestFile data : list){
			if("error".equals(checkMap.get(data.getHblNo()))){
				continue;
			}
			if(maxHsn > 9999){
				errorStr.append(data.getHblNo() + " : HSN 범위를 초과 하여 생성 하지 않았습니다.<br />");
				continue;
			}
			if("T".equals(search.getBlTypeIert()) && "KR".equals(data.getDestCountry())){
				errorStr.append(data.getHblNo() + " : T/S화물인 경우는 도착국가가 KR이 아니어야 합니다.<br />");
				continue;
			}
			if("I".equals(search.getBlTypeIert()) && !"KR".equals(data.getDestCountry())){
				errorStr.append(data.getHblNo() + " : T/S화물이 아닌 경우에는 도착국가가 KR 이어야 합니다<br />");
				continue;
			}
			
			data.setEntCode(entCode);
			data.setHeadEntBusinessCode(search.getEntBusinessCode());
			data.setSolution("ECM");
			data.setGmtTime(search.getGmtTime());
			data.setDateFormat(search.getDateFormat());
			data.setCustCodeLv1("UPS-IMP");
			data.setBlTypeMh("H");
			data.setInspectionYn("N");
			data.setGeInspectionYn("N");
			data.setMultiYn("N");
			data.setHoldYn("N");
			data.setFtaYn("Y");
			data.setFinishYn("Y");
			data.setUserId(search.getUserId());
			data.setMasterOdJobNo(search.getOdJobNo());
			data.setMblNo(search.getMblNo());
			data.setMrn(search.getMrn());
			data.setMsn(search.getMsn());
			data.setHsn(Util.lpad(Util.null2str(maxHsn), 4, '0'));
			data.setLineCode(search.getLineCode());
			data.setVesselFlight(search.getVesselFlight());
			data.setBlTypeIert(search.getBlTypeIert());
			data.setDestCode(search.getDestCode());
			data.setOrigCode(search.getOrigCode());
			data.setArDate(search.getArDate());
			data.setArTime(search.getArTime());
			data.setObDate(search.getObDate());
			data.setObTime(search.getObTime());
			data.setBound(search.getBound());
			data.setBoundType(search.getBoundType());
			data.setConsolYn("Y");
			data.setOrigName(search.getOrigName());
//			data.setOrigCountry(search.getOrigCountry());
			data.setDestName(search.getDestName());
			data.setDestPort(search.getDestPort());
			data.setDestCountry(search.getDestCountry());
			data.setCustomsCode(search.getCustomsCode());
			data.setCustomsDptCode(search.getCustomsDptCode());
			data.setDiscWhCode(search.getDiscWhCode());
			/** UPS는 위험물여부에 따라 고정값 적용 */
//			data.setAssignmentWhCode(search.getAssignmentWhCode());
			data.setEdiMailboxSeqNo(search.getEdiMailboxSeqNo());
			data.setVenderId(search.getVenderId());
			data.setSelectorUserId(search.getSelectorUserId());
			data.setConfirmerUserId(search.getConfirmerUserId());
			
			String customsId = Util.null2str(data.getCustomsId());
			if(customsId.length() == 8 && StringUtils.isNumeric(customsId)){
				data.setCustomsIdType("B");	/** 생년월일 */
			}else if(customsId.length() == 13){
				if((customsId.startsWith("P") || customsId.startsWith("p")) && StringUtils.isNumeric(customsId.substring(1))){
					data.setCustomsIdType("A");	/** 개인통관고유부호 */
				}else if(StringUtils.isNumeric(customsId) && "5678".contains(customsId.substring(6,7))){
					data.setCustomsIdType("E");	/** 외국인등록번호 */
				}
			}else if(customsId.length() == 10 && StringUtils.isNumeric(customsId)){
				data.setCustomsIdType("D");	/** 사업자번호 */
			}else if(customsId.length() == 11 && StringUtils.isNumeric(customsId.substring(4)) && customsId.substring(0, 4).matches("[ㄱ-ㅎ가-힣*]*$")){
				data.setCustomsIdType("C");	/** 통관고유부호 */
			}else if(!Util.isEmpty(customsId)) {
				data.setCustomsIdType("F");	/** 위의 조건에 해당하지 않고 개인통관고유부호 공란이 아닐 경우 F.여권 번호 으로 분류 */
			}else {
				data.setCustomsIdType("Z");	/** 개인통관고유부호 공란일 경우 Z.데이터 없음 으로 분류 */
			}
			
			/** 통관구분값이 "일반", "배제" 일 경우 개인통관부호 값이 없는 상태이면-->  검증결과: NULL, 검증결과내역: Null 처리 */
			if((!Util.null2str(data.getCdType()).equals("B") && !Util.null2str(data.getCdType()).equals("N")) || !customsId.equals("")){
				if(Util.null2str(data.getCustomsIdType()).equals("A")){
					/** 개인통관식별자가 A일경우 검증(N) or 미입력 */
					if(!Util.isEmpty(customsId)){
						data.setCustomsIdVerify("NV");
					}else{
						data.setCustomsIdVerify("X");
					}
				}else{
					/** 개인통관식별자가 A가 아닐경우 검증제외 */
					data.setCustomsIdVerify("O");
				}
			}
			
			/** 2025.10.01 Verify에서 셋팅&속도문제로 주석처리(HSM)
			if("1".equals(data.getCdUseType()) && "Y".equals(data.getCdType()) && Util.isEmpty(data.getCustomsId())){
				consigneeName = Util.null2str(data.getConsigneeName()).replaceAll("[^a-zA-Z]", "");
				consigneeTel = Util.null2str(data.getConsigneeTel()).replaceAll("[^0-9]", "");
				if(!"".equals(consigneeName) && !"".equals(consigneeTel)){
					strIdx = consigneeTel.length() - 8;
					if(strIdx > 0){
						consigneeTel = consigneeTel.substring(strIdx);
					}
					tmpMap.put("consigneeName", consigneeName);
					tmpMap.put("consigneeTel", consigneeTel);
					ediMap = manifestFileMapper.selectOrderCustomsId(tmpMap);
					if(ediMap != null && ediMap.size() > 0){
						data.setCustomsId(Util.null2str(ediMap.get("CUSTOMS_ID")));
						data.setCustomsIdType(Util.null2str(ediMap.get("CUSTOMS_ID_TYPE")));
					}
				}
			}
			*/
			
			/** 용도구분 = 1.개인 이고 거래유형 = A.전자상거래개인 or D.개인물품 이고 개인통관부호가 없는 목록건인 경우는 제출번호가 생성하지 않음 */
			if(!("1".equals(data.getCdUseType()) && ("A".equals(data.getEcmType()) || "D".equals(data.getEcmType())) && Util.isEmpty(data.getCustomsId()) && "Y".equals(data.getCdType()))){
				if("Y".equals(data.getDocumentYn())){
					data.setSimpleDocNo(simpleDocNoDocumentY);
				}else{
					data.setSimpleDocNo(simpleDocNoDocumentN);
				}
			}
			/** 2025-02-05 거래유형이 A.전자상거래개인이고 전자상거래유형 값이 없을경우 Z.파악불가 저장 */
			if("A".equals(data.getEcmType()) && Util.isEmpty(data.getEcmBuyType())){
				data.setEcmBuyType("Z");
			}
			
			data.setLastPodStatus("ECM055");	/** 콘솔마감 */
			data.setPodJobNo(DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
			data.setPodJobNo2(DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
			
			stockMap.put("seqNo", stockSeqNo++);
			data.setOdJobNo(commonMapper.selectNextStockMaxNo(stockMap));
			
//			packingList = data.getPackingList();
//			int packingCnt = 0;
			
//			Order order = new Order();
//			order.setOdJobNo(data.getOdJobNo());
//			order.setHblNo(data.getHblNo().trim().replace("\t", ""));
//			if(Util.null2int(orderMapper.updatePackingOdJobNo(order)) > 0){
//				Map<String, Object> packingMap = new HashMap<String, Object>();
//				packingMap.put("odJobNo", order.getOdJobNo());
//				packingMap.put("hblNo", order.getHblNo());
//				packingCnt = orderMapper.selectPackingCount(packingMap);
//				data.setAction("select");
//			}
//			
//			String packingNo;
//			for(Map<String, Object> packing : packingList){
//				packingNo = Util.null2str(packing.get("packingNo"));
//				if(packingNo.length() == 18 || packingNo.equals(order.getHblNo())){
//					packingCnt++;
//				}
//			}
//			if(packingCnt != Util.null2int(data.getPieces())){
//				data.setPackingErrorYn("Y");
//			}else{
//				data.setPackingErrorYn("N");
//			}
			
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpLogMap);
			logMap.put("job_no", data.getOdJobNo());
			logMap.put("description", description +"[HBL_NO : " + data.getHblNo()+"]");
			logMap.put("job_log", new Gson().toJson(data));
			logMap.put("reference_no_id", bulkData.getId());
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
			
			createHblData.put(data.getHblNo(), "Y");
			insertList.add(data);
			maxHsn++;
		}

		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			ManifestFileMapper manifestFileBatchMapper = sqlSession.getMapper(ManifestFileMapper.class);
			OrderMapper orderBatchMapper = sqlSession.getMapper(OrderMapper.class);
			int cnt = 1;
			for(ManifestFile data : insertList){
				manifestFileBatchMapper.insertHouseOrder(data);
				if((cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			cnt = 1;
			List<Map<String, Object>> itemList;
			for(ManifestFile data : insertList){
				itemList = data.getItemList();
				if(itemList != null){
					for(Map<String, Object> item : itemList){
						item.put("odJobNo", data.getOdJobNo());
						manifestFileBatchMapper.insertHouseItem(item);
						if((cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
				}
			}
			
			cnt = 1;
			for(ManifestFile data : insertList){
				packingList = data.getPackingList();
				if(packingList != null){
					for(Map<String, Object> packing : packingList){
						packing.put("odJobNo", data.getOdJobNo());
						manifestFileBatchMapper.insertHousePacking(packing);
						//hblNo와 packingNo가 같으면 데이터 저장 X -> 주석처리
//						if(!Util.null2str(data.getHblNo()).equals(Util.null2str(packing.get("packingNo")))) {
//							manifestFileBatchMapper.insertHousePacking(packing);
//						}
						
						if((cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
				}
			}
			
			cnt = 1;
			Order order = new Order();
			order.setAction("updatePackingErrorYn");
			for(ManifestFile data : insertList){
				order.setOdJobNo(data.getOdJobNo());
				order.setHblNo(data.getHblNo());
				orderBatchMapper.updatePackingOdJobNo(order);
				
				if((cnt++ % 1000) == 0){
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
			sqlSession.close();
		}
		commonService.insertLogBulk(bulkList);
		
		resultMap.put("createCount", insertList.size());
		resultMap.put("createHblData", createHblData);
		if(errorStr.length() > 0){
			resultMap.put("errorStr", errorStr.toString());
		}
		result.setResultData(resultMap);
		return result;
	}
	
	/**
	 * 수입적하목록 파일업로드
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public FileUploadResponse uploadIFile(String entCode, SearchManifestFileList search, MultipartFile multipartFile) throws Exception {
		FileUploadResponse result = new FileUploadResponse(FileUploadResponse.RESULT_OK);
		if(!"ups1".equals(profile) && !"bms1".equals(profile) && !"bms3".equals(profile)) {
			result.setResult(FileUploadResponse.RESULT_FAIL);
			return result;
		}
		// 확장자 체크 (.001 ~ .365)
		try {
			String chkExt = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
			if(chkExt.length() != 3 || Util.null2int(chkExt) < 1 || Util.null2int(chkExt) > 365){
				result.setResult(FileUploadResponse.RESULT_FAIL);
				result.setResultText("Invalid file format.");
				return result;
			}
		}catch(NumberFormatException e) {
			result.setResult(FileUploadResponse.RESULT_FAIL);
			result.setResultText("Invalid file format.");
			return result;
		}
		
		if(multipartFile.getSize() > 1024 * 1024 * 20){
			result.setResult(FileUploadResponse.RESULT_SIZE_OVER);
			result.setResultText("The file size exceeds the upload size.");
		}else{
			if(!multipartFile.isEmpty()){
				result = fileService.storeFile(multipartFile, entCode, false, true);
				if(FileUploadResponse.RESULT_OK.equals(result.getResult())){
					File file = fileService.loadFileAsResource(result.getFileName(), entCode, false).getFile();
					
					FileReader fr = null;
					BufferedReader br = null;
					try{
						NumberFormat f = NumberFormat.getInstance();
						f.setGroupingUsed(false);
						
						search.setSrchEntCode(entCode);
						fr = new FileReader(file);
						br = new BufferedReader(fr);
						Map<String, Map<String, Object>> eTradeInfoData = new HashMap<String, Map<String, Object>>();
						List<String> updateList = new ArrayList<String>();
						Map<String, String> arrHBLInfo = new HashMap<String, String>();
						Map<String, Object> tmpMap = new HashMap<String, Object>();
						List<String> arrHBLInv = new ArrayList<String>();
						List<String> arrHBLPackage = new ArrayList<String>();
						List<ManifestFile> dataList = new ArrayList<ManifestFile>();
						StringBuffer errorStr = new StringBuffer();
						String preHblNo = "", tmpHblNo;
						String strTextData = null;
						
						if(!Util.isEmpty(search.getMblNo())){
							Map<String, Object> mblMap = new HashMap<String, Object>();
							mblMap.put("mblNo", search.getMblNo());
							mblMap.put("bound", "AIR");
							mblMap.put("boundType", "IMP");
							mblMap = manifestFileMapper.selectMasterInfo(mblMap);
							if(mblMap != null){
								search.setArDate(Util.null2str(mblMap.get("AR_DATE")));
								search.setOrigCountry(Util.null2str(mblMap.get("ORIG_COUNTRY")));
							}
						}
						
						Map<String, Object> exchangeRateMap = new HashMap<String, Object>();
						exchangeRateMap.put("srchEntCode", entCode);
						exchangeRateMap.put("entBusinessCode", search.getEntBusinessCode());
						exchangeRateMap.put("srchDateFormat", search.getSrchDateFormat());
						exchangeRateMap.put("exrateType", "EXRATE_BUYING_CUSTOMS");
						exchangeRateMap.put("srchDate", search.getArDate());
						exchangeRateMap.put("srchCurrencyCode", "USD");
						tmpMap = commonMapper.selectExchangeRate2(exchangeRateMap);
						if(tmpMap == null){
							search.setUsdExchangeRate(0);
							exchangeRateMap.put("USD_YN", "N");
						}else{
							search.setUsdExchangeRate(Util.null2dbl(tmpMap.get("EXCHANGE_RATE")));
							exchangeRateMap.put("USD", search.getUsdExchangeRate());
							exchangeRateMap.put("USD_YN", "Y");
						}
						
						tmpMap = new HashMap<String, Object>();
						tmpMap.put("srchBound", "AIR");
						tmpMap.put("srchBoundType", "IMP");
						tmpMap.put("srchBasicType", "A");
						List<Map<String, Object>> upsBasicList = manifestFileMapper.selectUpsBasic(tmpMap);
						if(upsBasicList.size() > 0){
							tmpMap.put("srchEtc1", "H");
							upsBasicList = manifestFileMapper.selectUpsBasic(tmpMap);
						}else{
							upsBasicList = null;
						}
						
						while((strTextData = br.readLine()) != null){
							if(strTextData.length() > 40){
								if(!this.iFileSubstr(strTextData, 35, 6).equals("000000") && !this.iFileSubstr(strTextData, 44, 1).trim().equals("")){
									tmpHblNo = this.iFileInsertGroupFile(strTextData, preHblNo, arrHBLInfo, arrHBLInv, arrHBLPackage, errorStr, search, exchangeRateMap, dataList, updateList, upsBasicList, eTradeInfoData, f);
									if(tmpHblNo != null) {
										preHblNo = tmpHblNo;
									}
								}
							}
						}
						
						if(arrHBLInfo.get("200000") != null) {
							if("C".equals(this.iFileSubstr(arrHBLInfo.get("200000"), 32, 1))){
								String consolidatedClearanceFlag = this.iFileSubstr(arrHBLInfo.get("200000"), 181, 1);
								if("L".equals(consolidatedClearanceFlag) || "V".equals(consolidatedClearanceFlag)){
									this.iFileDataSetting(arrHBLInfo, arrHBLInv, arrHBLPackage, errorStr, search, exchangeRateMap, dataList, updateList, upsBasicList, eTradeInfoData, f);
								}
							}else{
								this.iFileDataSetting(arrHBLInfo, arrHBLInv, arrHBLPackage, errorStr, search, exchangeRateMap, dataList, updateList, upsBasicList, eTradeInfoData, f);
							}
						}
						
						if(updateList.size() > 0){
							SqlSession sqlSession = null;
							try {
								sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
								ManifestFileMapper manifestFileBatchMapper = sqlSession.getMapper(ManifestFileMapper.class);
								int cnt = 1;
								tmpMap.clear();
								tmpMap.put("gmtTime", search.getGmtTime());
								for(String hblNo : updateList){
									tmpMap.put("hblNo", hblNo);
									manifestFileBatchMapper.updateExclusion(tmpMap);
									if((cnt++ % 1000) == 0){
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
								sqlSession.close();
							}
						}
						
						if(errorStr.length() > 0){
							result.setResult("dataErr");
							result.setResultText(errorStr.toString());
						}
						
						result.setResultList(dataList);
						
					}catch(Exception e){
						throw e;
					}finally{
						if(br != null){try{br.close();}catch(Exception e){log.error(e.getMessage());}}
						if(fr != null){try{fr.close();}catch(Exception e){log.error(e.getMessage());}}
					}
				}
			} else {
				result.setResult(FileUploadResponse.RESULT_NOT_FILE);
				result.setResultText("The file does not exist.");
			}
		}
		return result;
	}
	public String iFileInsertGroupFile(String strTextData, String preHblNo, Map<String, String> arrHBLInfo, List<String> arrHBLInv, List<String> arrHBLPackage, StringBuffer errorStr, SearchManifestFileList search, Map<String, Object> exchangeRateMap
			, List<ManifestFile> dataList, List<String> updateList, List<Map<String, Object>> upsBasicList, Map<String, Map<String, Object>> eTradeInfoData, NumberFormat f) throws Exception {
		String strTextType = "";
		String hblNo = null;
		if(!this.iFileSubstr(strTextData, 0, 10).trim().equals("")){
			strTextType = this.iFileSubstr(strTextData, 44, 6);
			if(!strTextType.equals("100000")){
				if(strTextType.equals("200000")){
					hblNo = this.iFileSubstr(strTextData, 33, 11);
					if(!hblNo.equals(preHblNo) && !preHblNo.equals("")){
						if("C".equals(this.iFileSubstr(arrHBLInfo.get("200000"), 32, 1))){
							String consolidatedClearanceFlag = this.iFileSubstr(arrHBLInfo.get("200000"), 181, 1);
							if("L".equals(consolidatedClearanceFlag) || "V".equals(consolidatedClearanceFlag)){
								this.iFileDataSetting(arrHBLInfo, arrHBLInv, arrHBLPackage, errorStr, search, exchangeRateMap, dataList, updateList, upsBasicList, eTradeInfoData, f);
							}
						}else{
							this.iFileDataSetting(arrHBLInfo, arrHBLInv, arrHBLPackage, errorStr, search, exchangeRateMap, dataList, updateList, upsBasicList, eTradeInfoData, f);
						}
						arrHBLInfo.clear();
						arrHBLInv.clear();
						arrHBLPackage.clear();
					}
				}
				
				if("200000".equals(strTextType) || "202000".equals(strTextType) || "300000".equals(strTextType) || "400000".equals(strTextType) || "401000".equals(strTextType) || "402000".equals(strTextType) || "900000".equals(strTextType)){
					arrHBLInfo.put(strTextType, this.iFileGetEdiCharacter(strTextData, true));
				}else if(Util.null2int(strTextType) >= 500000 && Util.null2int(strTextType) <= 599000){
					arrHBLInv.add(this.iFileGetEdiCharacter(strTextData, true));
				}else if(Util.null2int(strTextType) >= 600000 && Util.null2int(strTextType) <= 699997){
					arrHBLPackage.add(this.iFileGetEdiCharacter(strTextData, true));
				}
			}else{
				search.setOrig(this.iFileSubstr(strTextData, 2, 4).trim());
			}
		}
		
		return hblNo;
	}
	public String iFileGetEdiCharacter(String strTextData, boolean impFlag) throws Exception {
		String strReturnValue = "";
		int chr;
		for(int i = 0; i < strTextData.length(); i++){
			chr = strTextData.charAt(i);
			if((chr >= 48 && chr <= 57) || (chr >= 65 && chr <= 90) || (chr >= 97 && chr <= 122) || (chr == 38 && impFlag) || chr == 44 || chr == 45 || chr == 46 || chr == 32 || chr < 0 || chr > 255){
				strReturnValue += strTextData.substring(i, i + 1);
			}else{
				strReturnValue += " ";
			}
		}
		return strReturnValue.replace("\r", "").replace("\n", "");
	}
	public String iFileSubstr(String str, int startIdx, int length) throws Exception {
		String returnStr = "";
		int strLength = str.length();
		if(startIdx >= strLength){
			return returnStr;
		}
		if(startIdx + length > strLength){
			length = strLength - startIdx;
		}
		returnStr = str.substring(startIdx, startIdx + length);
		return returnStr;
	}
	public String iFileGetClearanceCheck(String strBasicType, String strCustomerName, List<Map<String, Object>> upsBasicList) throws Exception {
		String strReturnValue = "";
		boolean bWorkType = false;
//		String ePerson = "C";
//		String eFor = "C";
		switch (strBasicType){
			case "A":	 //용도구분
				if(strCustomerName != ""){
					for(Map<String, Object> upsBasicMap : upsBasicList){
						if(strCustomerName.contains(Util.null2str(upsBasicMap.get("ETC2")).trim())){
							bWorkType = true;
							break;
						}
					}
					if(bWorkType){
						strReturnValue = "2";	//회사
					}else {
						strReturnValue = "1";	//개인
					}
				}else{
					strReturnValue = "2";	//회사
				}
				break;
//			case "B":   //전자상거래
//				if(strCustomerName != ""){
//					for (int i = 0; i < dsEtrade.Tables[0].Rows.Count; i++){
//						if((dsEtrade.Tables[0].Rows[i]["Etc1"].ToString().Trim() == "A" && strCustomerName.Trim().ToUpper() == dsEtrade.Tables[0].Rows[i]["Etc2"].ToString().Trim().ToUpper())
//							|| (dsEtrade.Tables[0].Rows[i]["Etc1"].ToString().Trim() == "B" && strCustomerName.Contains(dsWorkType.Tables[0].Rows[i]["Etc2"].ToString().Trim()))){
//							strReturnValue = "A";
//							ePerson = "A";   //한번만 걸리면 - 전자 상거래
//						}else if((dsEtrade.Tables[0].Rows[i]["Etc1"].ToString().Trim() == "G" && strCustomerName.Trim().ToUpper() == dsEtrade.Tables[0].Rows[i]["Etc2"].ToString().Trim().ToUpper())
//							|| (dsEtrade.Tables[0].Rows[i]["Etc1"].ToString().Trim() == "H" && strCustomerName.Contains(dsWorkType.Tables[0].Rows[i]["Etc2"].ToString().Trim()))){
//							strReturnValue = "B";
//							eFor = "B";   //한번만 걸리면 - 수입대행
//						}else{
//							strReturnValue = "C";
//						}
//					}
//				}else{
//					strReturnValue = "C";	//일반
//				}
//				if(ePerson == "A"){
//					strReturnValue = "A";
//				}
//				if(eFor == "B"){
//					strReturnValue = "B";
//				}
//				break;
		}
		return strReturnValue;
	}
	
	public String GetDetailDescription(double dblUSDExRate, String strHBLNo, List<String> arrHBLInv, Map<String, Object> exchangeRateMap, StringBuffer errorStr, NumberFormat f) throws Exception {
		String strPCSUnit = "";
		String strReturnValue = "";
		String strCurrency = "";
		double dblAmount_ifile = 0;
		double dblAmountKRW = 0;
		double dblAmount = 0;	 //통관금액
		double dblValue = 0;
		double dblExRate = 0;
		Map<String, Object> tmpMap;

		for(int i = 0; i < arrHBLInv.size(); i++){
			if(!"".equals(arrHBLInv.get(i))){
				if(i == 0){	//500000 레코드 저장
					dblValue = 0; dblAmount = 0;
					strCurrency = this.iFileSubstr(arrHBLInv.get(0), 171, 3);
					dblAmount_ifile = Util.null2dbl(Util.null2dbl(this.iFileSubstr(arrHBLInv.get(0), 161, 10).trim()) / 100, 2, "U");
					if(!strCurrency.equals("")){
						if(exchangeRateMap.get(strCurrency + "_YN") == null){
							exchangeRateMap.put("srchCurrencyCode", strCurrency);
							tmpMap = commonMapper.selectExchangeRate2(exchangeRateMap);
							if(tmpMap == null){
								exchangeRateMap.put(strCurrency + "_YN", "N");
							}else{
								exchangeRateMap.put(strCurrency + "_YN", "Y");
								exchangeRateMap.put(strCurrency, Util.null2dbl(tmpMap.get("EXCHANGE_RATE")));
							}
						}
						
						if("Y".equals(exchangeRateMap.get(strCurrency + "_YN"))){
							if(strCurrency.equals("KRW")){
								if(dblUSDExRate > 0){
									dblAmount = dblAmount_ifile / dblUSDExRate;
								}
							}else if(!strCurrency.equals("USD")){
								dblExRate = Util.null2dbl(exchangeRateMap.get(strCurrency));
								if(dblExRate > 0 && dblUSDExRate > 0){
									dblAmountKRW = Util.null2dbl(dblExRate * dblAmount_ifile, 0, "U");	//원화로 환산
									dblAmount = dblAmountKRW / dblUSDExRate;	//USD 환율로 나눈다.
								}
							}else{
								dblAmount = dblAmount_ifile;
							}
						}else{
							strCurrency = "USD";
							dblValue = (Util.null2dbl(this.iFileSubstr(arrHBLInv.get(0), 161, 10).trim()) / 100) + 0.999999;
							dblAmount = Util.null2dbl(dblValue, 0, "U");
						}
					}
					if(this.iFileSubstr(arrHBLInv.get(0), 57, 100).trim().equals("")){
						errorStr.append(strHBLNo + " : 상세품목명이 없습니다.<br />");
					}else{
						strReturnValue = "01^";	//일련번호
						strReturnValue += this.iFileSubstr(arrHBLInv.get(0), 57, 100).trim() + "^";	//품명
						if(!this.iFileSubstr(arrHBLInv.get(0), 233, 35).trim().equals("")){
							strReturnValue += this.iFileSubstr(arrHBLInv.get(0), 233, 35).trim() + "^";	//규격
						}else{
							strReturnValue += this.iFileSubstr(arrHBLInv.get(0), 57, 100).trim() + "^";	//규격
						}
						strReturnValue += this.iFileSubstr(arrHBLInv.get(0), 54, 3).trim() + "^";	//수량 단위
						strPCSUnit = this.iFileSubstr(arrHBLInv.get(0), 54, 3).trim();
						strReturnValue += this.iFileSubstr(arrHBLInv.get(0), 50, 4).trim() + "^";	//수량
						
						/** 2025-02-11 숫자가 커질경우 지수표현되서 수정함 */
						strReturnValue += f.format(Util.null2dbl(dblAmount, 2, "U")) + "|";	//금액
					}
				}else{	//501000 부터 511000 까지 저장
					dblValue = 0; dblAmount = 0;
					strCurrency = this.iFileSubstr(arrHBLInv.get(i), 171, 3);
					dblAmount_ifile = Util.null2dbl(this.iFileSubstr(arrHBLInv.get(i), 161, 10).trim()) / 100;

					if(!strCurrency.equals("")){
						if(exchangeRateMap.get(strCurrency + "_YN") == null){
							exchangeRateMap.put("srchCurrencyCode", strCurrency);
							tmpMap = commonMapper.selectExchangeRate2(exchangeRateMap);
							if(tmpMap == null){
								exchangeRateMap.put(strCurrency + "_YN", "N");
							}else{
								exchangeRateMap.put(strCurrency + "_YN", "Y");
								exchangeRateMap.put(strCurrency, Util.null2dbl(tmpMap.get("EXCHANGE_RATE")));
							}
						}
						
						if("Y".equals(exchangeRateMap.get(strCurrency + "_YN"))){
							if(strCurrency.equals("KRW")){
								if(dblUSDExRate > 0){
									dblAmount = dblAmount_ifile / dblUSDExRate;
								}
							}else if(!strCurrency.equals("USD")){
								dblExRate = Util.null2dbl(exchangeRateMap.get(strCurrency));
								if(dblExRate > 0 && dblUSDExRate > 0){
									dblAmountKRW = Util.null2dbl(dblExRate * dblAmount_ifile, 0, "U");	//원화로 환산
									dblAmount = dblAmountKRW / dblUSDExRate;	//USD 환율로 나눈다.
								}
							}else {
								dblAmount = dblAmount_ifile;
							}
						}else{
							strCurrency = "USD";
							dblValue = (Util.null2dbl(this.iFileSubstr(arrHBLInv.get(i), 161, 10).trim()) / 100) + 0.999999;
							dblAmount = Util.null2dbl(dblValue, 0, "U");
						}
					}

					if(dblAmount == 0){
						dblAmount = 1;
					}
					if(!this.iFileSubstr(arrHBLInv.get(i), 57, 100).trim().equals("")){
						strReturnValue += Util.lpad(Util.null2str(i + 1), 2, '0') + "^";	//일련번호
						strReturnValue += this.iFileSubstr(arrHBLInv.get(i), 57, 100).trim() + "^";	//품명
						if(!this.iFileSubstr(arrHBLInv.get(i), 233, 35).trim().equals("")){
							strReturnValue += this.iFileSubstr(arrHBLInv.get(i), 233, 35).trim() + "^";	//규격
						}else{
							strReturnValue += this.iFileSubstr(arrHBLInv.get(i), 57, 100).trim() + "^";	//규격
						}
						if(this.iFileSubstr(arrHBLInv.get(i), 54, 3).trim().equals("")){	//단위가 있어야함.
							strReturnValue += strPCSUnit + "^";	//수량 단위
						}else{
							strReturnValue += this.iFileSubstr(arrHBLInv.get(i), 54, 3).trim() + "^";	//수량 단위
						}
						strReturnValue += this.iFileSubstr(arrHBLInv.get(i), 50, 4).trim() + "^";   //수량
						/** 2025-02-11 숫자가 커질경우 지수표현되서 수정함 */
						strReturnValue += f.format(Util.null2dbl(dblAmount, 2, "U")) + "|";
					}
				}
			}
		}

		return this.iFileSubstr(strReturnValue, 0, strReturnValue.length() - 1);
	}
	
	public boolean iFileDataSetting(Map<String, String> arrHBLInfo, List<String> arrHBLInv, List<String> arrHBLPackage, StringBuffer errorStr, SearchManifestFileList search, Map<String, Object> exchangeRateMap
			, List<ManifestFile> dataList, List<String> updateList, List<Map<String, Object>> upsBasicList, Map<String, Map<String, Object>> eTradeInfoData, NumberFormat f) throws Exception {
		double dblAmountKRW = 0;
		double dblAmount = 0;	 //통관금액
		double dblValue = 0;
		String strHBLNo = "";
		String strHBLNo6 = "";
		String strCommodity = "";
		String strCurrency = "";
		String strGoods = "";
		String strConsigneeName = "";
		String strRejectCode = "";
		String strGoodsType = "";
		String strWorkType = "";
		String strCustomsYN = "";
		String strCustomsYN_Sub = "";
		String strCustomsYN_LD = "";
		String strEtradeURL = "";
		String strItemCode = "";
		String strConsigneeAddress = "";
		String strConsigneeTel = "";
		double nRealPCS = 0;
		double dblGWT = 0;
		double dblCustomsValue = 0;
		String strPlanYN = "";
		String strNewBillCurrencyCode = "";
		double dblNewBillAmount = 0;
//		String strConsigneeContactName = "";
		String strP_CustomsNumberA = "";
		String strP_CustomsNumberB = "";
		String strP_CustomsNumberC = "";
		String strP_CustomsNumberD = "";
		String strP_CustomsNumber_Plan = "";   //개인통관고유부호(플랜데이터)
		String strCustomsUseType_Plan = "";   //용도구분(플랜데이터)
		int nP_Pos = 0;
		String strThirdParty = "";
		String strBillingTerm = "";
		String strTrans = "";
		String strDuties = "";
		String strShipper_CountryCode = "";
		String strShipper_AC_3rd = "";
		String strConsignee_Number = "";
		String strConsignee_AC_3rd = "";
		boolean bCustoms_zj = false;
		String strEtradeURL_zj = "";
		String streTradeType_zj = "";
		String streTradeCompanyName_zj = "";
		String strConsigneeNameLocal = "";
		String strConsigneeAddressLocal = "";
		String strConsigneeZipcode = "";
		String documentYn = "";
		String documentType = "";
		
		try{
			if(arrHBLInfo.get("200000") == null){arrHBLInfo.put("200000", "");}
			if(arrHBLInfo.get("202000") == null){arrHBLInfo.put("202000", "");}
			if(arrHBLInfo.get("300000") == null){arrHBLInfo.put("300000", "");}
			if(arrHBLInfo.get("400000") == null){arrHBLInfo.put("400000", "");}
			if(arrHBLInfo.get("401000") == null){arrHBLInfo.put("401000", "");}
			if(arrHBLInfo.get("402000") == null){arrHBLInfo.put("402000", "");}
			if(arrHBLInfo.get("900000") == null){arrHBLInfo.put("900000", "");}
			
//			strConsigneeContactName = this.iFileSubstr(arrHBLInfo.get("400000"), 103, 25).trim();

			dblCustomsValue = 150;
			if(search.getOrigCountry().equals("US") || search.getOrigCountry().equals("PR")){
				dblCustomsValue = 200;
			}
			
			strCustomsYN = "Y";
			String strCustomerCode = this.iFileSubstr(arrHBLInfo.get("300000"), 58, 10).trim();
			String strCustomerCode_cne = this.iFileSubstr(arrHBLInfo.get("400000"), 58, 10).trim();
			if(!"".equals(strCustomerCode) || !"".equals(strCustomerCode_cne)) {
				List<String> srchEtc1InList = new ArrayList<String>();
				Map<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("srchBound", "AIR");
				tmpMap.put("srchBoundType", "IMP");
				if(!"".equals(strCustomerCode)){
					srchEtc1InList.add(strCustomerCode);
				}
				if(!"".equals(strCustomerCode_cne)){
					srchEtc1InList.add(strCustomerCode_cne);
				}
				tmpMap.put("srchEtc1InList", srchEtc1InList);
				List<String> srchBasicTypeInList = new ArrayList<String>();
				srchBasicTypeInList.add("C");
				srchBasicTypeInList.add("D");
				tmpMap.put("srchBasicTypeInList", srchBasicTypeInList);
				List<Map<String, Object>> upsBlackList = manifestFileMapper.selectUpsBasic(tmpMap);
				for(Map<String, Object> upsBasicMap : upsBlackList){
					if("D".equals(upsBasicMap.get("BASIC_TYPE"))){
						strCustomsYN_LD = "Z";
					}else{
						strCustomsYN = "N";
					}
					if("Z".equals(strCustomsYN_LD) && "N".equals(strCustomsYN)){
						break;
					}
				}
			}
			
			strHBLNo = this.iFileSubstr(arrHBLInfo.get("200000"), 33, 11);
			strHBLNo6 = this.iFileSubstr(strHBLNo, 0, 6);
			documentType = this.iFileSubstr(arrHBLInfo.get("200000"), 72, 1);
			if("D".equals(documentType) || "L".equals(documentType)){
				documentYn = "Y";
			}else{
				documentYn = "N";
			}
			if("Y".equals(search.getUploadSearch())){
				if(!search.getSrchDestCode().equals("") && !search.getSrchDestCode().equals(this.iFileSubstr(arrHBLInfo.get("200000"), 8, 4))){
					return false;
				}else if(!search.getSrchDestCountry().equals("") && !search.getSrchDestCountry().equals(this.iFileSubstr(arrHBLInfo.get("200000"), 6, 2))){
					return false;
				}else if(!search.getSrchDocumentYn().equals("") && !search.getSrchDocumentYn().equals(documentYn)){
					return false;
				}
			}
			
			if(this.iFileSubstr(arrHBLInfo.get("200000"), 32, 1).equals("C") && this.iFileSubstr(arrHBLInfo.get("200000"), 181, 1).equals("H")){
				return false;
			}
			
			//L Or D 건은 목록으로 하고, 실제수량은1 품명은 Document, 통관금액은 0, 품명코드는 49로 한다.
			//if(EtcLibrary.getSubStringK(_arrHBLInfo[0], 72, 1) != "L" && EtcLibrary.getSubStringK(_arrHBLInfo[0], 72, 1) != "D")
			//{
			//#region - "목록배제"
			Map<String, Object> exclusionMap = new HashMap<String, Object>();
			exclusionMap.put("hblNo", strHBLNo);
			exclusionMap = manifestFileMapper.selectExclusion(exclusionMap);
			if(exclusionMap != null && exclusionMap.size() > 0){
				strGoods = Util.null2str(exclusionMap.get("NEW_COMMODITY_ITEM"));
				strConsigneeName = Util.null2str(exclusionMap.get("CONSIGNEE_NAME"));
				strRejectCode = Util.null2str(exclusionMap.get("F_CODE"));
				strGoodsType = Util.null2str(exclusionMap.get("ALLOWED_ITEM_CODE"));
				strEtradeURL = Util.null2str(exclusionMap.get("URL")).trim();
				
				strNewBillCurrencyCode = Util.null2str(exclusionMap.get("CURRENCY_CODE"));
				dblNewBillAmount = Util.null2dbl(exclusionMap.get("AMOUNT_CUR"));
				strConsigneeAddress = Util.null2str(exclusionMap.get("CONSIGNEE_ADDRESS"));
				strConsigneeTel = Util.null2str(exclusionMap.get("CONSIGNEE_HP"));
				strP_CustomsNumber_Plan = Util.null2str(exclusionMap.get("CUSTOMS_ID"));
				strCustomsUseType_Plan = Util.null2str(exclusionMap.get("CD_USE_TYPE"));
				strConsigneeNameLocal = Util.null2str(exclusionMap.get("CONSIGNEE_NAME_LOC"));
				strConsigneeAddressLocal = Util.null2str(exclusionMap.get("CONSIGNEE_ADDRESS_LOC"));
				strConsigneeZipcode = Util.null2str(exclusionMap.get("CONSIGNEE_ZIPCODE"));
				
				//19.07.23 적하목록생성시 Low_V_Rjt.OldGoods=null인지 추가로 체크하여 null인 경우 HAWB에 PLAN = N, null인 아닌 경우 HAWB에 PLAN = Y
				if(Util.isEmpty(Util.null2str(exclusionMap.get("OLD_COMMODITY_ITEM")))){
					strPlanYN = "N";
				}else{
					strPlanYN = "Y";
				}
				
				updateList.add(strHBLNo);
			}else{
				strPlanYN = "N";
			}
			//}
			
			Map<String, Object> eTradeInfo = null;
			if(eTradeInfoData.get(strHBLNo6) == null){
				eTradeInfo = new HashMap<String, Object>();
				eTradeInfo.put("srchBound", "AIR");
				eTradeInfo.put("srchBoundType", "IMP");
				eTradeInfo.put("srchBasicType", "E");
				eTradeInfo.put("srchHblNo", strHBLNo6);
				eTradeInfo = manifestFileMapper.selectUpsBasicMap(eTradeInfo);
				if(eTradeInfo == null){
					eTradeInfo = new HashMap<String, Object>();
				}
				eTradeInfoData.put(strHBLNo6, eTradeInfo);
			}else{
				eTradeInfo = eTradeInfoData.get(strHBLNo6);
			}
			if(eTradeInfo.get("BOUND") != null){
				bCustoms_zj = true;
				strEtradeURL_zj = Util.null2str(eTradeInfo.get("ETC2")).trim();	//전자상거래 주소
				streTradeType_zj = this.iFileSubstr(Util.null2str(eTradeInfo.get("ETC3")).trim(), 0 , 1);	//전자상거래유형
				streTradeCompanyName_zj = this.iFileSubstr(Util.null2str(eTradeInfo.get("ETC1")).trim(), 0 , 150);	//전자상거래유형에 따른 업체상호
			}

			//#region - "중량"
			if(this.iFileSubstr(arrHBLInfo.get("200000"), 32, 1).equals("C")){
				if(this.iFileSubstr(arrHBLInfo.get("200000"), 181, 1).equals("L") || this.iFileSubstr(arrHBLInfo.get("200000"), 181, 1).equals("V")){
					if(this.iFileSubstr(arrHBLInfo.get("401000"), 326, 3).equals("KGS") || this.iFileSubstr(arrHBLInfo.get("401000"), 326, 3).trim().equals("")){
//						strGWT = Convert.ToDouble(double.Parse(EtcLibrary.getSubStringK(_arrHBLInfo[4], 329, 14).Trim()) / 10).ToString(zCompanyInfo.glvWeightType_4);
						dblGWT = Util.null2dbl(Util.null2dbl(this.iFileSubstr(arrHBLInfo.get("401000"), 329, 14).trim()) / 10, 3, "U");
					}else if(this.iFileSubstr(arrHBLInfo.get("401000"), 326, 3).equals("LBS")){
						//서보국 과장 요청 ==> LBS 일때 Local Billing System 과 UFMS 시스템의 중량 계산값이 상이 하다고 하여 uFMS 시스템이  Local Billing 시스템에 맞춘다.
						//소숫점 두째자리는 무조건 버린다.
						//dblGWT = double.Parse(EtcLibrary.getSubStringK(_arrHBLInfo[4], 329, 14).Trim()) / 2.205 * 10 + 0.5;
						//16.04.26 파운드 계산공식변경
						dblGWT = Util.null2dbl(this.iFileSubstr(arrHBLInfo.get("401000"), 329, 14).trim()) * 0.453592;
						//dblGWT = dblGWT / 10;
						//if(zCompanyInfo.glvCustomsType == "3")
						//	dblGWT = MyRound(dblGWT, 1);
						//else
						//{
							//dblGWT = MyRound(dblGWT, 3);
//							dblGWT = Math.Round(dblGWT, 3);
							dblGWT = Util.null2dbl(dblGWT, 3, "U");
						//}
						//strGWT = Convert.ToDouble((double.Parse(EtcLibrary.getSubStringK(_arrHBLInfo[4], 329, 14).Trim()) / 2.205 * 10 + 0.5) / 10).ToString("0.0");
					}
				}
			}else{
				if(this.iFileSubstr(arrHBLInfo.get("200000"), 81, 3).equals("KGS") || this.iFileSubstr(arrHBLInfo.get("200000"), 81, 3).trim().equals("")){
//					strGWT = Convert.ToDouble(double.Parse(EtcLibrary.getSubStringK(_arrHBLInfo[1], 85, 7).Trim()) / 10).ToString(zCompanyInfo.glvWeightType_4);
					dblGWT = Util.null2dbl(Util.null2dbl(this.iFileSubstr(arrHBLInfo.get("202000"), 85, 7).trim()) / 10, 3, "U");
				}else if(this.iFileSubstr(arrHBLInfo.get("200000"), 81, 3).equals("LBS")){
					//서보국 과장 요청 ==> LBS 일때 Local Billing System 과 UFMS 시스템의 중량 계산값이 상이 하다고 하여 uFMS 시스템이  Local Billing 시스템에 맞춘다.
					//소숫점 두째자리는 무조건 버린다.
					//dblGWT = double.Parse(EtcLibrary.getSubStringK(_arrHBLInfo[1], 85, 7).Trim()) / 2.205 * 10 + 0.5;
					//16.04.26 파운드 계산공식변경
					dblGWT = Util.null2dbl(this.iFileSubstr(arrHBLInfo.get("202000"), 85, 7).trim()) * 0.453592;
					//dblGWT = dblGWT / 10;
					//if(zCompanyInfo.glvCustomsType == "3")
					//	dblGWT = MyRound(dblGWT, 1);
					//else
					//{

						//dblGWT = MyRound(dblGWT, 3);
//						dblGWT = Math.Round(dblGWT, 3);
						dblGWT = Util.null2dbl(dblGWT, 3, "U");
					//}
					//strGWT = Convert.ToDouble((double.Parse(EtcLibrary.getSubStringK(_arrHBLInfo[1], 85, 7).Trim()) / 2.205 * 10 + 0.5) / 10).ToString("0.0");
				}
			}
			//#endregion
			//#region - "품명"
			
			if(strGoods.equals("")){
				if(arrHBLInv.size() == 0 || this.iFileSubstr(arrHBLInv.get(0), 57, 100).trim().equals("")){
					strCommodity = "Sample";
					if(!documentType.equals("L") && !documentType.equals("D")){
						errorStr.append(strHBLNo + " : 품명이 없습니다.<br />");
					}
				}else{
					for (int nX = 0; nX < arrHBLInv.size(); nX++){
						if(nX == 0){
							strCommodity = this.iFileSubstr(arrHBLInv.get(nX), 57, 100).trim();
						}else{
							strCommodity += " / " + this.iFileSubstr(arrHBLInv.get(nX), 57, 100).trim();
						}
					}
					if(strCommodity.equals("")){
						if(!documentType.equals("L") && !documentType.equals("D")){
							errorStr.append(strHBLNo + " : 품명이 없습니다.<br />");
						}
					}
				}
			}else{
				strCommodity = strGoods;
			}
			//#endregion

			strCurrency = this.iFileSubstr(arrHBLInfo.get("200000"), 175, 3);
			if(!strNewBillCurrencyCode.equals("") && dblNewBillAmount != 0){
				////if(DupliCheck_MBL(txtMBLNo.Text, ref strMRefNo, ref strARDate))
				////{
				////	strARDate = dtpARDate.Text;
				////}
				////if(strCurrencyCode == "USD")
				////	dblUSDAmount = dblAmount;
				////else
				////	dblUSDAmount = GetExRate(strCurrencyCode, strARDate, dblAmount);
				dblValue = dblNewBillAmount;
				strCurrency = this.iFileSubstr(strNewBillCurrencyCode.replace(" ", "").replace("\r", "").replace("\n", ""), 0, 3);
			}

			//#region - "통관금액"
			if(dblValue == 0){
				dblValue = (Util.null2dbl(this.iFileSubstr(arrHBLInfo.get("200000"), 307, 11).trim()) - Util.null2dbl(this.iFileSubstr(arrHBLInfo.get("200000"), 153, 9).trim()) - Util.null2dbl(this.iFileSubstr(arrHBLInfo.get("200000"), 141, 9).trim()) - Util.null2dbl(this.iFileSubstr(arrHBLInfo.get("200000"), 241, 11).trim()) + Util.null2dbl(this.iFileSubstr(arrHBLInfo.get("200000"), 106, 9).trim())) / 100;
			}
			if(dblValue <= 0){
				if(!documentType.equals("L") && !documentType.equals("D")){
					errorStr.append(strHBLNo + " : 통관금액이 0 이거나 음수 입니다.<br />");
				}
			}
			//#endregion

			//#region - "환율"
			if(exchangeRateMap.get(strCurrency + "_YN") == null){
				exchangeRateMap.put("srchCurrencyCode", strCurrency);
				Map<String, Object> tmpMap = commonMapper.selectExchangeRate2(exchangeRateMap);
				if(tmpMap == null){
					exchangeRateMap.put(strCurrency + "_YN", "N");
				}else{
					exchangeRateMap.put(strCurrency + "_YN", "Y");
					exchangeRateMap.put(strCurrency, Util.null2dbl(tmpMap.get("EXCHANGE_RATE")));
				}
			}
			if("Y".equals(exchangeRateMap.get(strCurrency + "_YN"))){
				search.setExchangeRate(Util.null2dbl(exchangeRateMap.get(strCurrency)));
				if(strCurrency.equals("KRW")){
					if(search.getUsdExchangeRate() > 0){
						dblAmount = dblValue / search.getUsdExchangeRate();
						dblAmountKRW = dblAmount;
					}
				}else if(!strCurrency.equals("USD")){
					if(search.getExchangeRate() > 0 && search.getUsdExchangeRate() > 0){
						dblAmountKRW = Util.null2dbl(search.getExchangeRate() * dblValue, 0, "U");	//원화로 환산
						dblAmount = dblAmountKRW / search.getUsdExchangeRate();	//USD 환율로 나눈다.
					}
				}else{
					dblAmountKRW = Util.null2dbl(search.getExchangeRate() * dblValue, 0, "U");	//원화로 환산
					dblAmount = dblValue;
				}
				
			}else{
				if(!documentType.equals("L") && !documentType.equals("D")){
					errorStr.append(this.iFileSubstr(arrHBLInfo.get("200000"), 33, 11) + " : " + strCurrency + " 에 환율이 없습니다.<br />");
				}
				strCurrency = "USD";

				//기초환율에 환율정보가 없는 경우는 금액을 0으로 생성한다.(17.12.26)
				dblAmount = 0;
				//dblValue = dblValue + 0.999999;
				//dblAmount = Convert.ToInt32(dblValue);
				//if(dblAmount == 0) dblAmount = 1;
			}
			//#endregion

			//#region - "목록 금액"
			if(dblAmount < dblCustomsValue + 1 && strRejectCode.equals("1")){
				strCustomsYN_Sub = "B";	//일반건 
			}
			//#endregion

			//#region - "개인/회사"
			//18.08.31 플랜데이터(Low_V_Rjt)에 값이 있으면 그 데이터를 우선으로 한다.
			if(!strCustomsUseType_Plan.equals("")){
				strWorkType = strCustomsUseType_Plan;
			}else{
				if(upsBasicList != null){
					strWorkType = this.iFileGetClearanceCheck("A", this.iFileSubstr(arrHBLInfo.get("400000"), 68, 35).trim(), upsBasicList);
				}
				if(strWorkType.equals("")){
					if(strRejectCode.equals("A")){
						strWorkType = "1";
					}else{
						strWorkType = "2";
					}
				}
			}
			//#endregion

			//#region - "전자상거래"
			/** strETrade값 사용하지 않아서 주석처리함 */
//			if(_bETrade){
//				strETrade = this.GetClearanceCheck("B", this.iFileSubstr(arrHBLInfo.get("300000"), 68, 35).trim());
//			}
			//#endregion

			//#region - "실제수량"
			for(int i = 0; i < arrHBLInv.size(); i++) {
				if(!arrHBLInv.get(i).equals("")){
					nRealPCS = nRealPCS + Util.null2dbl(this.iFileSubstr(arrHBLInv.get(i), 50, 4).trim());
				}
			}
			if(nRealPCS == 0){
				if(!documentType.equals("L") && !documentType.equals("D")){
					errorStr.append(strHBLNo + " : Total 인보이스 수량 ERROR - 9999개 로 SETTING합니다..꼭!!! ISPS를 확인하세요. TR# : <br />");
					nRealPCS = 9999;
				}
			}
			//#endregion

			if(arrHBLInv.size() > 0) {
//				strItemCode = EtcLibrary.DeleteAlphabetChar(EtcLibrary.getSubStringK(_arrHBLInv[0], 196, 6));
				strItemCode = this.iFileSubstr(arrHBLInv.get(0), 196, 6).replaceAll("[^0-9]", "");
				if(strItemCode.trim().equals("")){
//					strItemCode = EtcLibrary.getSubStringK(EtcLibrary.DeleteAlphabetChar(EtcLibrary.getSubStringK(_arrHBLInv[0], 233, 35)), 0, 2);
					strItemCode = this.iFileSubstr(this.iFileSubstr(arrHBLInv.get(0), 233, 35).replaceAll("[^0-9]", ""), 0, 2);
				}
			}

			//if(DateTime.Parse(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss")) < DateTime.Parse("2022-10-04 19:00:00"))
			//{
			//	if(strItemCode.Length != 2)
			//		strItemCode = "";
			//}

			if(!this.iFileSubstr(arrHBLInfo.get("400000"), 255, 9).trim().equals("")){
//				if(EtcLibrary.DeleteAlphabetChar(EtcLibrary.getSubStringK(_arrHBLInfo[3], 255, 9).Trim()).Length != 5){
				if(this.iFileSubstr(arrHBLInfo.get("400000"), 255, 9).trim().replaceAll("[^0-9]", "").length() != 5){
					errorStr.append(strHBLNo + " : 수하인 우편번호 길이는 5자리입니다.<br />");
				}
			}

			//L Or D 건은 목록으로 하고, 실제수량은1 품명은 Document, 통관금액은 0, 품명코드는 49로 한다.
			if(documentType.equals("L") || documentType.equals("D")){
				strCommodity = "DOCUMENTS";
				nRealPCS = 1;
				strItemCode = "49"; strGoodsType = "";
				//dblAmount = 0; strCustomsYN = "Y";   DOX건은 1불로 생성한다.(16.04.25 UPS 요청)
				dblAmount = 1;
				strCustomsYN = "Y";	
			}

			//#region - "18.04.18 NewBilling DB에 접근이 안되기 때문에 BillingTerm, Trans, Duties 항목은 iFile에서 생성한다."
			strBillingTerm = this.iFileSubstr(arrHBLInfo.get("200000"), 318, 3).trim();  //BillingTerm
			strThirdParty = this.iFileSubstr(arrHBLInfo.get("200000"), 179, 1).trim();  //ThirdParty
			strShipper_AC_3rd = this.iFileSubstr(arrHBLInfo.get("300000"), 270, 10).trim();  //SHIPPER 3RD PARTY
			strShipper_CountryCode = this.iFileSubstr(arrHBLInfo.get("300000"), 315, 2).trim();  //SHIPPER 3RD PARTY CountryCode
			strConsignee_Number = this.iFileSubstr(arrHBLInfo.get("400000"), 58, 10).trim();  //CONSIGNEE NUMBER
			strConsignee_AC_3rd = this.iFileSubstr(arrHBLInfo.get("402000"), 50, 10).trim();  //CONSIGNEE 3RD PARTY
			strTrans = "";
			strDuties = "";
			switch(strBillingTerm){
				case "P P":
					strBillingTerm = "P/P";
					if(strThirdParty.equals("0")){
						strTrans = "S"; strDuties = "R";
					}else if(strThirdParty.equals("1")){
						strTrans = "S"; strDuties = "T";
					}else if(strThirdParty.equals("2")){
						strTrans = "S"; strDuties = "T";
					}else if(strThirdParty.equals("3")){
						strTrans = "T"; strDuties = "R";
					}else if(strThirdParty.equals("6")){
						strTrans = "T"; strDuties = "R";
					}else if(strThirdParty.equals("7")){
						strTrans = "R"; strDuties = "T";
					}else if(strThirdParty.equals("8")){
						strTrans = "T"; strDuties = "S";
						if(strShipper_AC_3rd.equals(strConsignee_Number)){
							strTrans = "R"; strDuties = "S";
						}
						if(!strShipper_AC_3rd.equals(strConsignee_AC_3rd)){
							strTrans = "T"; strDuties = "T";
						}
						if(strShipper_CountryCode.equals("KR")){
							strTrans = "T"; strDuties = "T";
						}
					}
					break;
				case "F D":
					strBillingTerm = "F/D";
					if(strThirdParty.equals("0")){
						strTrans = "S"; strDuties = "S";
					}else if(strThirdParty.equals("6") || strThirdParty.equals("8")){
						strTrans = "T"; strDuties = "T";
					}
					break;
				case "F C":
					strBillingTerm = "F/C";
					if(strThirdParty.equals("0")){
						strTrans = "R"; strDuties = "R";
					}else if(strThirdParty.equals("1")){
						strTrans = "T"; strDuties = "T";
					}else if(strThirdParty.equals("2")){
						strTrans = "T"; strDuties = "T";
					}
					break;
				case "FOB":
					if(strThirdParty.equals("0")){
						strTrans = "SR"; strDuties = "R";
					}else if(strThirdParty.equals("6")){
						strTrans = "RT"; strDuties = "R";
					}
					break;
			}
			//#endregion

			//#region - "UPS 요청사항 16.08.18"
			//1. 적하목록 생성시 (개인통관고유부호 가지고옴) - 테스트 I-FILE로 적하목록 생성시 첨부해드린 개인고유통관번호가 입력되면 됩니다.
			//2. 통관목록 생성시 1. 전자상거래구분 ”A” + 2. 통관용도구분 “1”(개인) 인 경우만 개인통관고유부호가 생성되어 제출될 수 있도록 부탁드립니다. 
			//3. 통관목록 오류체크 (“P”+”숫자12자리”)

			//POSITON
			//400000 RECORD FORMAT 343~356(14) - IMPTR ID (CONSIGNEE TAX ID NUMBER) 
			//400000 RECORD FORMAT 308~342(35) - REF NUM 2
			//900000 RECORD FORMAT 118~186(69) - SPECIAL INSTRUCTION FIELD
			//300000 RECORD FORMAT 333~367(35) - 추가(18.04.23)

			//위 POSITION에서 필요한 데이터는 “P”+”숫자12자리” 인경우(사업자등록번호는 제외)입니다.
			//전체 들어오는 문자값중 P로시작하는 문자열이 있으면 P문자부터13자리 잘라서 가져오고 가져온값을 숫자만 체크해서 12자리인지 체크해서 가져옴(중간에 “-“, “공란”을 제외하고 체크)
			//#endregion

			nP_Pos = this.iFileSubstr(arrHBLInfo.get("400000"), 307, 35).trim().toUpperCase().indexOf("P");

			if(nP_Pos >= 0){
				strP_CustomsNumberA = this.iFileSubstr(arrHBLInfo.get("400000"), 307, 35).trim();
				strP_CustomsNumberA = this.iFileSubstr(strP_CustomsNumberA, nP_Pos + 1, 35 - (nP_Pos + 1));
//				strP_CustomsNumberA = EtcLibrary.getSubStringK(EtcLibrary.DeleteAlphabetChar(strP_CustomsNumberA),0,12);
				strP_CustomsNumberA = this.iFileSubstr(strP_CustomsNumberA.replaceAll("[^0-9]", ""), 0, 12);

				if(strP_CustomsNumberA.length() == 12) {
					strP_CustomsNumberA = "P" + strP_CustomsNumberA;
				}else {
					strP_CustomsNumberA = "";
				}
			}

			if(strP_CustomsNumberA.equals("")){
				nP_Pos = this.iFileSubstr(arrHBLInfo.get("400000"), 342, 14).trim().toUpperCase().indexOf("P");
				if(nP_Pos >= 0){
					strP_CustomsNumberB = this.iFileSubstr(arrHBLInfo.get("400000"), 342, 14).trim();
					strP_CustomsNumberB = this.iFileSubstr(strP_CustomsNumberB, nP_Pos + 1, 14 - (nP_Pos + 1));
//					strP_CustomsNumberB = EtcLibrary.getSubStringK(EtcLibrary.DeleteAlphabetChar(strP_CustomsNumberB),0,12);
					strP_CustomsNumberB = this.iFileSubstr(strP_CustomsNumberB.replaceAll("[^0-9]", ""),0,12);

					if(strP_CustomsNumberB.length() == 12){
						strP_CustomsNumberB = "P" + strP_CustomsNumberB;
					}else{
						strP_CustomsNumberB = "";
					}
				}
			}

			if(strP_CustomsNumberB.equals("")){
				nP_Pos = this.iFileSubstr(arrHBLInfo.get("900000"), 117, 69).trim().toUpperCase().indexOf("P");
				if(nP_Pos >= 0){
					strP_CustomsNumberC = this.iFileSubstr(arrHBLInfo.get("900000"), 117, 69).trim();
					strP_CustomsNumberC = this.iFileSubstr(strP_CustomsNumberC, nP_Pos + 1, 69 - (nP_Pos + 1));
//					strP_CustomsNumberC = EtcLibrary.getSubStringK(EtcLibrary.DeleteAlphabetChar(strP_CustomsNumberC),0,12);
					strP_CustomsNumberC = this.iFileSubstr(strP_CustomsNumberC.replaceAll("[^0-9]", ""),0,12);

					if(strP_CustomsNumberC.length() == 12){
						strP_CustomsNumberC = "P" + strP_CustomsNumberC;
					}else{
						strP_CustomsNumberC = "";
					}
				}
			}

			if(strP_CustomsNumberC.equals("")){
				nP_Pos = this.iFileSubstr(arrHBLInfo.get("300000"), 332, 35).trim().toUpperCase().indexOf("P");
				if(nP_Pos >= 0){
					strP_CustomsNumberD = this.iFileSubstr(arrHBLInfo.get("300000"), 332, 35).trim();
					strP_CustomsNumberD = this.iFileSubstr(strP_CustomsNumberD, nP_Pos + 1, 35 - (nP_Pos + 1));
//					strP_CustomsNumberD = EtcLibrary.getSubStringK(EtcLibrary.DeleteAlphabetChar(strP_CustomsNumberD), 0, 12);
					strP_CustomsNumberD = this.iFileSubstr(strP_CustomsNumberD.replaceAll("[^0-9]", ""), 0, 12);

					if(strP_CustomsNumberD.length() == 12){
						strP_CustomsNumberD = "P" + strP_CustomsNumberD;
					}else{
						strP_CustomsNumberD = "";
					}
				}
			}
			
			ManifestFile data = new ManifestFile();
			data.setNum(dataList.size() + 1);
			data.setCreateYn("N");
			data.setHblNo(strHBLNo);
			data.setPlanYn(strPlanYN);
			
			if(this.iFileSubstr(arrHBLInfo.get("200000"), 32, 1).equals("C")){
				data.setPieces(Util.null2dbl(this.iFileSubstr(arrHBLInfo.get("401000"), 319, 7)));
			}else{
				data.setPieces(Util.null2dbl(this.iFileSubstr(arrHBLInfo.get("202000"), 99, 5)));
			}
			data.setPiecesReal(nRealPCS);
			data.setGwt(dblGWT);
			data.setGwtLbs(dblGWT * 2.20462262);
			data.setCommodityItem(this.iFileSubstr(strCommodity.trim(), 0, 200).replace("'", "`"));

			if(this.iFileSubstr(arrHBLInfo.get("202000"), 155, 1).trim().equals("1")){
//				data.setAssignmentWhCode("SJD");
				data.setAssignmentWhCode("SID");	//2025.05.08 SJD=>SID 변경
			}else{
				data.setAssignmentWhCode("UX");
			}
			
			data.setDoSendYn("N");
			if(documentType.equals("L") || documentType.equals("D")){
				//L/D로 생성시 - document
				if(strWorkType.equals("1"))
					data.setEcmType("D");
				else
					data.setEcmType("F");
			}else{
				//N로 생성시 - Non DOC
				if(strRejectCode.equals("A") || bCustoms_zj){
					//플랜데이터 DB의 Filed의 fCode=A인 경우에 생성시 용도구분이 개인이면 거래코드 "A"로 진행되고 플랜데이터 DB의 Filed WebAddr를 사이트주소에 입력
					if(strWorkType.equals("1")){
						data.setEcmType("A");
						data.setEcmBuyType(streTradeType_zj);   //2022.09.27 전자상거래유형 추가

						if(streTradeType_zj.equals("A")){
							data.setSellerName(streTradeCompanyName_zj);   //2022.09.27 해외판매업자
						}else if(streTradeType_zj.equals("B")){
							data.setSellerName(streTradeCompanyName_zj);   //2022.09.27 해외판매업자
							data.setAgentRegNo(this.iFileSubstr(streTradeCompanyName_zj, 0 , 9));   //2022.09.27 구매대행업자
						}else if(streTradeType_zj.equals("C")){
							data.setSellerName(streTradeCompanyName_zj);   //2022.09.27 해외판매업자
							data.setAgentRegNo(this.iFileSubstr(streTradeCompanyName_zj, 0 , 9));   //2022.09.27 구매대행업자
							data.setAgentName(streTradeCompanyName_zj);   //2022.09.27 배송대행업자
						}
					}else{  //플랜데이터 DB의 Filed의  fCode=A인 경우에 생성시 용도구분이 회사이면 거래코드 “E”로 진행
						data.setEcmType("E");
					}

					//전자상거래 주소는 적하목록 신고까지는 생성되어야 향 후 로컬시스템에서 집계가 가능 (*목록통관 선별시 거래코드 “E”인 경우에는 사이트주소를 삭제한다.*)
					if(!strEtradeURL.equals("")){
						data.setUrl(strEtradeURL);
					}else{
						data.setUrl(strEtradeURL_zj);
					}
				}else{
					if(strWorkType.equals("1"))
						data.setEcmType("D");   ////플랜데이터 DB의 Filed의 fCode 값이 없고 용도구분이 개인인 경우 : D 로 생성
					else
						data.setEcmType("E");   ////플랜데이터 DB의 Filed의 fCode 값이 없고 용도구분이 회사인 경우 : E 로 생성
				}
			}
			data.setCdUseType(strWorkType);
			data.setCurrencyCode("USD");
			data.setAmountCur(Util.null2dbl(dblAmount, 2, "U"));
			data.setExchangeRate(search.getUsdExchangeRate());
			data.setAmountLoc(dblAmountKRW);
			data.setAmountUsd(data.getAmountCur());

			if(documentType.equals("L") || documentType.equals("D")){
				if(strRejectCode.equals("1")){	//(17.11.23) L 또는 D 건 이면서 Local DB에 fcode가 1인 경우는 목록배제처리한다.
					data.setCdType("B");
				}else{
					if(strCustomsYN_LD.equals("Z")){
//						data.setCdType("Z");
						data.setCdType("N");	/** Z = 일반으로 대체 */
					}else{
						data.setCdType("Y");
					}
				}
			}else{
				if(strRejectCode.equals("1")){
					data.setCdType("B");
				}else{
					/** 2025-03-19 출발지 입력하지않고 Verify 실행할경우 출발국가별 USD금액으로 목록/일반 처리 불가함    USD금액이 150 ~ 200 사이일경우 저장시점에 체크하도록 수정 */
					if(dblAmount > 200){
						data.setCdType("N");
					}else{
						if(strCustomsYN.equals("Y")){
							if(strCustomsYN_Sub.equals("N")){
								data.setCdType("N");
							}else{
								data.setCdType("Y");
							}
						}else{
							if(strCustomsYN.equals("N")){
//								data.setCdType("Z");
								data.setCdType("N");	/** Z = 일반으로 대체 */
							}else{
								data.setCdType(strCustomsYN);
							}
						}
						
						if(dblAmount > 150){
							data.setCdTypeTemp(data.getCdType());
							data.setCdType(null);
						}
					}
				}
			}
			data.setFwrdCode("UPSX");
			data.setShipperName(this.iFileSubstr(arrHBLInfo.get("300000"), 68, 35).trim().replace("'", "`"));
//			spMainList.SetData(_nHouseCount, "ShipperAddress", "[" + this.iFileSubstr(arrHBLInfo.get("300000"), 0, 2).trim() + "] " + this.iFileSubstr(arrHBLInfo.get("300000"), 103, 35).trim());
//			spMainList.SetData(_nHouseCount, "ShipperAddressA", this.iFileSubstr(arrHBLInfo.get("300000"), 138, 35).trim());
//			spMainList.SetData(_nHouseCount, "ShipperAddressB", this.iFileSubstr(arrHBLInfo.get("300000"), 173, 35).trim());
			data.setShipperAddress("[" + this.iFileSubstr(arrHBLInfo.get("300000"), 0, 2).trim() + "] " + this.iFileSubstr(arrHBLInfo.get("300000"), 103, 35).trim() + this.iFileSubstr(arrHBLInfo.get("300000"), 138, 35).trim() + this.iFileSubstr(arrHBLInfo.get("300000"), 173, 35).trim());
			data.setShipperAddress(data.getShipperAddress().replace("'", "`"));
			
			data.setShipperBusinessId(this.iFileSubstr(arrHBLInfo.get("300000"), 58, 10).trim());
			data.setShipperCountryCode(this.iFileSubstr(arrHBLInfo.get("300000"), 239, 2).trim());
			/** 2025-05-20 origCountry 최초 출발지로 저장 */
			data.setOrigCountry(data.getShipperCountryCode());

			//#region - "송하인 추가항목 2012.08.14"
			data.setShipperZipcode(this.iFileSubstr(arrHBLInfo.get("300000"), 230, 9).trim());
			data.setShipperStateCode(this.iFileSubstr(arrHBLInfo.get("300000"), 228, 2).trim());
			data.setShipperStreet(this.iFileSubstr(arrHBLInfo.get("300000"), 138, 35).trim());
			data.setShipperPlace(this.iFileSubstr(arrHBLInfo.get("300000"), 103, 17).trim());
			data.setShipperTel(this.iFileSubstr(arrHBLInfo.get("300000"), 242, 14).trim().replaceAll("[^0-9]", ""));
			//#endregion

			//if(EtcLibrary.getSubStringK(_arrHBLInfo[1], 59, 2).Trim() == "85" && EtcLibrary.getSubStringK(_arrHBLInfo[1], 50, 18).Trim().Length == 18)
			//{
			//	if(strConsigneeName != "")
			//	{
			//		if(strConsigneeName.Replace(" ", "") != strConsigneeContactName.Replace(" ", ""))
			//			spMainList.SetData(_nHouseCount, "ConsigneeName", EtcLibrary.getSubStringK(strConsigneeName, 0, 35) + "/" + strConsigneeContactName);
			//		else
			//			spMainList.SetData(_nHouseCount, "ConsigneeName", EtcLibrary.getSubStringK(strConsigneeName, 0, 35));
			//	}
			//	else
			//	{
			//		if(EtcLibrary.getSubStringK(_arrHBLInfo[4], 68, 35).Replace(" ", "") != strConsigneeContactName.Replace(" ", ""))
			//			spMainList.SetData(_nHouseCount, "ConsigneeName", EtcLibrary.getSubStringK(_arrHBLInfo[4], 68, 35).Trim() + "/" + strConsigneeContactName);
			//		else
			//			spMainList.SetData(_nHouseCount, "ConsigneeName", EtcLibrary.getSubStringK(_arrHBLInfo[4], 68, 35).Trim());
			//	}

			//	if(strConsigneeAddress != "")
			//	{
			//		spMainList.SetData(_nHouseCount, "ConsigneeAddress",  EtcLibrary.getSubStringK(strConsigneeAddress, 0, 60).Trim());
			//		spMainList.SetData(_nHouseCount, "ConsigneeAddressA", EtcLibrary.getSubStringK(strConsigneeAddress, 60, 60).Trim());
			//		spMainList.SetData(_nHouseCount, "ConsigneeAddressB", EtcLibrary.getSubStringK(strConsigneeAddress, 120, 60).Trim());
			//		spMainList.SetData(_nHouseCount, "ConsigneeAddressC", EtcLibrary.getSubStringK(strConsigneeAddress, 180, 60).Trim());
			//	}
			//	else
			//	{
			//		spMainList.SetData(_nHouseCount, "ConsigneeAddress", EtcLibrary.getSubStringK(_arrHBLInfo[4], 128, 35).Trim());
			//		spMainList.SetData(_nHouseCount, "ConsigneeAddressA", EtcLibrary.getSubStringK(_arrHBLInfo[4], 163, 35).Trim());
			//		spMainList.SetData(_nHouseCount, "ConsigneeAddressB", EtcLibrary.getSubStringK(_arrHBLInfo[4], 233, 20).Trim());
			//		spMainList.SetData(_nHouseCount, "ConsigneeAddressC", EtcLibrary.getSubStringK(_arrHBLInfo[4], 198, 35).Trim());
			//	}

			//	if(strConsigneeTel.Replace(" ", "") != "")
			//	{
			//		spMainList.SetData(_nHouseCount, "ConsigneeTel", strConsigneeTel.Replace(" ", ""));
			//	}
			//	else
			//	{
			//		if(EtcLibrary.getSubStringK(_arrHBLInfo[4], 267, 1) == "(" || SetDataVal(EtcLibrary.getSubStringK(_arrHBLInfo[4], 267, 14)) != "0")
			//			spMainList.SetData(_nHouseCount, "ConsigneeTel", EtcLibrary.getSubStringK(_arrHBLInfo[4], 267, 14).Trim());
			//		else
			//			spMainList.SetData(_nHouseCount, "ConsigneeTel", "");
			//	}
			//}
			//else
			//{
				if(!strConsigneeName.equals("")){
					//18.10.18 UPS 요청 => 예전에 수하인상호이름 다음에 '/' + ContactName 을 붙여달라는 요청이 있었으나, 관세청에서 수하인 이름이 이상하다고 지적을 받았다고 한다.
					//따라서 '/' + ContactName 를 붙이는 로직은 제거한다.
					//if(strConsigneeName.Replace(" ", "") != strConsigneeContactName.Replace(" ", ""))
					//	spMainList.SetData(_nHouseCount, "ConsigneeName", EtcLibrary.getSubStringK(strConsigneeName, 0, 35) + "/" + strConsigneeContactName);
					//else

					//2021.03.04 UPS 요청(이메일로 요청) Low_V_Rjt 의 RcvName 항목에 값이 있어도 iFile 상의 수하인 상호 길이보다 작은 경우에는 iFile 상의 수하인 상호로 생성한다.
					//if(strConsigneeName.Trim().Length >= EtcLibrary.getSubStringK(_arrHBLInfo[3], 68, 35).Trim().Length)
					//	spMainList.SetData(_nHouseCount, "ConsigneeName", EtcLibrary.getSubStringK(strConsigneeName, 0, 100));
					//else
					//	spMainList.SetData(_nHouseCount, "ConsigneeName", EtcLibrary.getSubStringK(_arrHBLInfo[3], 68, 35).Trim());

					//2021.05.07 Low_V_Rjt 의 RcvName 항목에 값이 있으면 해당 값으로 수하인명에 적용한다.(유명종 대리 요청)
					data.setConsigneeName(this.iFileSubstr(strConsigneeName, 0, 100).replace("'", "`"));
				}else{
					//if(EtcLibrary.getSubStringK(_arrHBLInfo[3], 68, 35).Replace(" ", "") != strConsigneeContactName.Replace(" ", ""))
					//	spMainList.SetData(_nHouseCount, "ConsigneeName", EtcLibrary.getSubStringK(_arrHBLInfo[3], 68, 35).Trim() + "/" + strConsigneeContactName);
					//else
					data.setConsigneeName(this.iFileSubstr(arrHBLInfo.get("400000"), 68, 35).trim().replace("'", "`"));
				}

				if(!strConsigneeAddress.equals("")){
					data.setConsigneeAddress(strConsigneeAddress.replace("'", "`"));
				}else{
					if(this.iFileSubstr(arrHBLInfo.get("400000"), 128, 35).trim().equals("") && this.iFileSubstr(arrHBLInfo.get("400000"), 163, 35).trim().equals("")
						&&this.iFileSubstr(arrHBLInfo.get("400000"), 233, 20).trim().equals("") && this.iFileSubstr(arrHBLInfo.get("400000"), 198, 35).trim().equals("")){
						errorStr.append(strHBLNo + " : 수하인 주소가 없습니다.<br />");
					}else{
//						spMainList.SetData(_nHouseCount, "ConsigneeAddress", this.iFileSubstr(arrHBLInfo.get("400000"), 128, 35).trim());
//						spMainList.SetData(_nHouseCount, "ConsigneeAddressA", this.iFileSubstr(arrHBLInfo.get("400000"), 163, 35).trim());
//						spMainList.SetData(_nHouseCount, "ConsigneeAddressB", this.iFileSubstr(arrHBLInfo.get("400000"), 233, 20).trim());
//						spMainList.SetData(_nHouseCount, "ConsigneeAddressC", this.iFileSubstr(arrHBLInfo.get("400000"), 198, 35).trim());
						data.setConsigneeAddress(this.iFileSubstr(arrHBLInfo.get("400000"), 128, 35).trim() + this.iFileSubstr(arrHBLInfo.get("400000"), 163, 35).trim() + this.iFileSubstr(arrHBLInfo.get("400000"), 233, 20).trim() + this.iFileSubstr(arrHBLInfo.get("400000"), 198, 35).trim());
						data.setConsigneeAddress(data.getConsigneeAddress().replace("'", "`"));
					}
				}

				if(!strConsigneeTel.replace(" ", "").equals("")){
					data.setConsigneeTel(strConsigneeTel.replace(" ", "").replaceAll("[^0-9]", ""));
				}else{
					if(this.iFileSubstr(arrHBLInfo.get("400000"), 267, 1).equals("(") || !"".equals(this.iFileSubstr(arrHBLInfo.get("400000"), 267, 14).replaceAll("[^0-9]", ""))){
//						if(!this.iFileSubstr(arrHBLInfo.get("400000"), 267, 14).trim().equals("")){
							data.setConsigneeTel(this.iFileSubstr(arrHBLInfo.get("400000"), 267, 14).replaceAll("[^0-9]", ""));
//						}else{
//							data.setConsigneeTel("0");
//						}
					}else{
						data.setConsigneeTel("0");
					}
				}
			//}
			data.setBankName(this.iFileSubstr(arrHBLInfo.get("400000"), 58, 10).trim());

			//2026-01-27 수하인우편번호는 목록배제 테이블에서 조회된 데이터가 있을 경우가 먼저. 없는 경우에 iFile에서 추출한다.
			if(!strConsigneeZipcode.equals("")){
				data.setConsigneeZipcode(strConsigneeZipcode);
			}else {
				data.setConsigneeZipcode(this.iFileSubstr(arrHBLInfo.get("400000"), 255, 9).trim());
			}
			
			//#region - "수하인 추가항목 2012.08.14"
			data.setConsigneeCountryCode(this.iFileSubstr(arrHBLInfo.get("400000"), 264, 2).trim());
			data.setConsigneeStateCode(this.iFileSubstr(arrHBLInfo.get("400000"), 253, 2).trim());
			data.setConsigneeStreet(this.iFileSubstr(arrHBLInfo.get("400000"), 163, 35).trim());
			data.setConsigneePlace(this.iFileSubstr(arrHBLInfo.get("400000"), 128, 17).trim());
			//#endregion

			//if(Convert.ToDateTime(Variable.glvCurrentDate) < Convert.ToDateTime("2018-09-05"))
			//	spMainList.SetData(_nHouseCount, "ElectronicURL", strEtradeURL);

			if(!strGoodsType.trim().equals("")){
				data.setAllowedItemCode(strGoodsType.trim());
			}else{
				//if(DateTime.Parse(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss")) < DateTime.Parse("2022-10-04 19:00:00"))
				//	spMainList.SetData(_nHouseCount, "ItemCode", EtcLibrary.getSubStringK(strItemCode.Trim(), 0, 2));
				//else
				//{
					if(data.getEcmType().equals("A")){
						data.setAllowedItemCode(this.iFileSubstr(strItemCode.trim(), 0, 6));
					}else{
						data.setAllowedItemCode(this.iFileSubstr(strItemCode.trim(), 0, 2));
					}
				//}
			}

			//#region - "상세품목"
			String detailDescription = null;
			if(documentType.equals("L") || documentType.equals("D")){
				detailDescription = "DOCUMENTS";
			}else{
//				data.setDetailDescription(GetDetailDescription(search.getUsdExchangeRate(), strHBLNo, arrHBLInv, exchangeRateMap, errorStr));
				detailDescription = this.GetDetailDescription(search.getUsdExchangeRate(), strHBLNo, arrHBLInv, exchangeRateMap, errorStr, f);
			}
			//#endregion

			//spMainList.SetData(_nHouseCount, "Remark", EtcLibrary.getSubStringK(_arrHBLInfo[2], 58, 10).Trim());   //ShipperCode
			data.setRemarks(this.iFileSubstr(arrHBLInfo.get("300000"), 58, 10).trim()
				+ "|" + this.iFileSubstr(arrHBLInfo.get("200000"), 8, 4).trim()
				+ "|" + this.iFileSubstr(arrHBLInfo.get("200000"), 219, 1).trim()   //ShipperCode, Port 4Code, Service 1Code 2012.07.18 추가(수출 TS건 처리관련작업)
				+ "|" + this.iFileSubstr(arrHBLInfo.get("200000"), 6, 2).trim());	  //Destination CountryCode

//			spMainList.SetData(_nHouseCount, "Log", "");
			data.setDestCountry(this.iFileSubstr(arrHBLInfo.get("200000"), 6, 2));
			data.setDocumentYn(documentYn);
			data.setTsSendType(this.iFileSubstr(arrHBLInfo.get("200000"), 219, 1).trim());
			

			//수하인 개인통관고유부호
			//18.08.31 플랜데이터(Low_V_Rjt)에 값이 있으면 그 데이터를 우선으로 한다.
			if(!strP_CustomsNumber_Plan.equals("")){
				data.setCustomsId(strP_CustomsNumber_Plan.toUpperCase());
			}else{
				//2021.09.07 : UPS 유명종대리 요청 개인통관고유부호 앞4자리가 P004인 경우에는 빈문자열로 생성요청
				if(!strP_CustomsNumberA.equals("")){
					if(this.iFileSubstr(strP_CustomsNumberA,0,4).equals("P004")){
						data.setCustomsId("");
					}else{
						data.setCustomsId(strP_CustomsNumberA.toUpperCase());
					}
				}else if(!strP_CustomsNumberB.equals("")){
					if(this.iFileSubstr(strP_CustomsNumberB, 0, 4).equals("P004")){
						data.setCustomsId("");
					}else{
						data.setCustomsId(strP_CustomsNumberB.toUpperCase());
					}
				}else if(!strP_CustomsNumberC.equals("")){
					if(this.iFileSubstr(strP_CustomsNumberC, 0, 4).equals("P004")){
						data.setCustomsId("");
					}else{
						data.setCustomsId(strP_CustomsNumberC.toUpperCase());
					}
				}else if(!strP_CustomsNumberD.equals("")){
					if(this.iFileSubstr(strP_CustomsNumberD, 0, 4).equals("P004")){
						data.setCustomsId("");
					}else{
						data.setCustomsId(strP_CustomsNumberD.toUpperCase());
					}
				}else{
					data.setCustomsId("");
				}
			}

			//18.09.14 UPS 요청 : 적하목록생성시 전자상거래건이며 + 개인통관고유부호( P +12자리)가 있는 경우에는 용도구분 : 개인
			if(data.getEcmType().equals("A") && this.iFileSubstr(data.getCustomsId(), 0, 1).equals("P") && data.getCustomsId().length() == 13){
				data.setCdUseType("1");
			}

			//적하목록생성시 용도구분이 회사건인 경우에 개인통관고유부호를 삭제
			if(data.getCdUseType().equals("2")){
				data.setCustomsId("");
			}

			data.setBillingTerm(strBillingTerm);
			data.setTransType(strTrans);
			data.setDuties(strDuties);
			data.setOrig(search.getOrig());

			//19.09.26 수하인한글성호, 주소 추가(Plan DB에서 가져온다.)
			data.setConsigneeNameLoc(strConsigneeNameLocal);
			data.setConsigneeAddressLoc(strConsigneeAddressLocal);
			data.setActualCstName(this.iFileSubstr(arrHBLInfo.get("401000"), 68, 35).trim());
			
			List<Map<String, Object>> itemList = new ArrayList<Map<String, Object>>();
			Map<String, Object> itemMap;
			String[] strDetailDesc_Count = detailDescription.split("\\|");
			String[] strDetailDesc = null;
			String commodityItem2 = "";
			for(int i = 0; i < strDetailDesc_Count.length; i++){
				if(i == 99){	//상세품목 갯수 제한  최대 99개
					break;
				}
				strDetailDesc = strDetailDesc_Count[i].split("\\^");
				if(strDetailDesc.length > 1){
					itemMap = new HashMap<String, Object>();
//					htlHBLSubData.Add("RefNo", strHRefNo);
					itemMap.put("seqNoId", i + 1);
					itemMap.put("detailItemName", strDetailDesc[1].replace("'", "`"));
					itemMap.put("brand", strDetailDesc[2].replace("'", "`"));
//					htlHBLSubData.Add("NumberID", strDetailDesc[0]);
					itemMap.put("amount", strDetailDesc[5]);
					itemMap.put("pieces", strDetailDesc[4]);
					itemMap.put("unitPieces", strDetailDesc[3]);
//					htlHBLSubData.Add("ItemCode", strDetailDesc[3]);
					
//					itemMap.put("currencyCode", this.iFileSubstr(arrHBLInv.get(i), 171, 3).trim());
					itemMap.put("currencyCode", "USD");
					if(Util.null2dbl(itemMap.get("pieces")) > 0){
						itemMap.put("unitPrice", Util.null2dbl(itemMap.get("amount")) / Util.null2dbl(itemMap.get("pieces")));
					}
					itemMap.put("gwt", 0);
					commodityItem2 += " / " + Util.null2str(itemMap.get("detailItemName"));
					itemList.add(itemMap);
				}
			}
			if(commodityItem2.length() > 3) {
				data.setCommodityItem2(commodityItem2.substring(3));
			}
			
			if(itemList.size() == 0){
				itemMap = new HashMap<String, Object>();
				itemMap.put("seqNoId", 1);
				itemMap.put("hsCode", data.getAllowedItemCode());
				itemMap.put("detailItemName", Util.null2str(data.getCommodityItem()).replace("'", "`"));
				itemMap.put("pieces", data.getPiecesReal());
				itemMap.put("gwt", data.getGwt());
				itemMap.put("amount", data.getAmountCur());
				itemMap.put("currencyCode", "USD");
				if(Util.null2int(data.getPiecesReal()) != 0 && Util.null2dbl(data.getAmountCur()) != 0){
					itemMap.put("unitPrice", Util.null2dbl(data.getAmountCur()) / Util.null2int(data.getPiecesReal()));
				}
				itemList.add(itemMap);
			}
			
			/** 2025-02-07 packing추가 */
			List<Map<String, Object>> packingList = new ArrayList<Map<String, Object>>();
			Map<String, Object> packingMap = new HashMap<String, Object>();
			packingMap.put("packingNo", this.iFileSubstr(arrHBLInfo.get("202000"), 50, 35).trim());
//			packingMap.put("gwt", Util.null2dbl(this.iFileSubstr(arrHBLInfo.get("202000"), 92, 7).trim()));
			packingMap.put("seqNoId", 1);
			packingMap.put("userId", search.getUserId());
			packingMap.put("gmtTime", search.getGmtTime());
			packingList.add(packingMap);
			
			int seqNoId = 2;
			for(String packageStr : arrHBLPackage){
				packingMap = new HashMap<String, Object>();
				packingMap.put("packingNo", this.iFileSubstr(packageStr, 166, 35).trim());
//				packingMap.put("gwt", Util.null2dbl(this.iFileSubstr(packageStr, 201, 7).trim()));
				packingMap.put("seqNoId", seqNoId++);
				packingMap.put("userId", search.getUserId());
				packingMap.put("gmtTime", search.getGmtTime());
				packingList.add(packingMap);
			}
			
			data.setItemList(itemList);
			data.setPackingList(packingList);
			dataList.add(data);
		}catch (Exception e){
			throw e;
		}
		return true;
	}
	
	/**
	 * 수출적하목록 파일업로드
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public FileUploadResponse uploadEFile(String entCode, SearchManifestFileList search, MultipartFile multipartFile) throws Exception {
		FileUploadResponse result = new FileUploadResponse(FileUploadResponse.RESULT_OK);
		if(!"ups1".equals(profile) && !"bms1".equals(profile) && !"bms3".equals(profile)) {
			result.setResult(FileUploadResponse.RESULT_FAIL);
			result.setResultText("It does not support uploading.");
			return result;
		}
		// 확장자 체크 (.001 ~ .365)
		try {
			String chkExt = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
			if(chkExt.length() != 3 || Util.null2int(chkExt) < 1 || Util.null2int(chkExt) > 365){
				result.setResult(FileUploadResponse.RESULT_FAIL);
				result.setResultText("Invalid file format.");
				return result;
			}
		}catch(NumberFormatException e) {
			result.setResult(FileUploadResponse.RESULT_FAIL);
			result.setResultText("Invalid file format.");
			return result;
		}
		
		if(multipartFile.getSize() > 1024 * 1024 * 8){
			result.setResult(FileUploadResponse.RESULT_SIZE_OVER);
			result.setResultText("The file size exceeds the upload size.");
		}else{
			if(!multipartFile.isEmpty()){
				result = fileService.storeFile(multipartFile, entCode, false, true);
				if(FileUploadResponse.RESULT_OK.equals(result.getResult())){
					File file = fileService.loadFileAsResource(result.getFileName(), entCode, false).getFile();
					
					FileReader fr = null;
					BufferedReader br = null;
					try{
						search.setSrchEntCode(entCode);
						fr = new FileReader(file);
						br = new BufferedReader(fr);
						
						Map<String, Object> tmpMap = new HashMap<String, Object>();
						Map<String, String> arrHBLInfo = new HashMap<String, String>();
						List<String> arrHBLInv = new ArrayList<String>();
						List<String> arrHBLChild = new ArrayList<String>();
						List<String> arrTargetText = new ArrayList<String>();
						List<ManifestFile> dataList = new ArrayList<ManifestFile>();
						StringBuffer errorStr = new StringBuffer();
						String preHblNo = "", tmpHblNo;
						String strTextData = null;
//						
						tmpMap.clear();
						tmpMap.put("srchBound", "AIR");
						tmpMap.put("srchBoundType", "EXP");
						tmpMap.put("srchBasicType", "B");
						List<Map<String, Object>> upsBasicList = manifestFileMapper.selectUpsBasic(tmpMap);
						
						while((strTextData = br.readLine()) != null){
							if(strTextData.length() > 40){
								if(Util.null2int(this.iFileSubstr(strTextData, 44, 1)) > 1 && Util.null2int(this.iFileSubstr(strTextData, 44, 1)) < 10) {
									tmpHblNo = eFileInsertGroupFile(strTextData, preHblNo, arrHBLInfo, arrHBLInv, arrHBLChild, arrTargetText, errorStr, search, dataList, upsBasicList);
									if(tmpHblNo != null) {
										preHblNo = tmpHblNo;
										
										//bl번호 중복체크
										Map<String, Object> odMap = new HashMap<String, Object>();
										odMap.put("hblNo", tmpHblNo);
										odMap.put("entCode", entCode);
										int blCnt = manifestFileMapper.selectDuplicateHblNo(odMap);
										if(blCnt > 0) {
											result.setResult(FileUploadResponse.RESULT_FAIL);
											result.setResultText("Duplicate HBL NO found.");
											return result;
										}
									}
								}
							}
						}
						
						if(arrHBLInfo.get("200000") != null) {
							if("C".equals(this.iFileSubstr(arrHBLInfo.get("200000"), 32, 1))){
								String consolidatedClearanceFlag = this.iFileSubstr(arrHBLInfo.get("200000"), 181, 1);
								if("L".equals(consolidatedClearanceFlag) || "V".equals(consolidatedClearanceFlag)){
									eFileDataSetting(arrHBLInfo, arrHBLInv, arrHBLChild, errorStr, search, dataList, upsBasicList);
								}
							}else{
								eFileDataSetting(arrHBLInfo, arrHBLInv, arrHBLChild, errorStr, search, dataList, upsBasicList);
							}
						}
						
						if(errorStr.length() > 0){
							result.setResult("dataErr");
							result.setResultText(errorStr.toString());
						}
						
						result.setResultList(dataList);
						
					}catch(Exception e){
						throw e;
					}finally{
						if(br != null){try{br.close();}catch(Exception e){log.error(e.getMessage());}}
						if(fr != null){try{fr.close();}catch(Exception e){log.error(e.getMessage());}}
					}
				}
			} else {
				result.setResult(FileUploadResponse.RESULT_NOT_FILE);
				result.setResultText("The file does not exist.");
			}
		}
		return result;
	}
	public String eFileInsertGroupFile(String strTextData, String preHblNo, Map<String, String> arrHBLInfo, List<String> arrHBLInv, List<String> arrHBLChild, List<String> arrTargetText, StringBuffer errorStr
			, SearchManifestFileList search, List<ManifestFile> dataList, List<Map<String, Object>> upsBasicList) throws Exception {
		String strTextType = "";
		String hblNo = null;
		strTextType = this.iFileSubstr(strTextData, 44, 6);
		
		if(strTextType.equals("200000")){
			hblNo = this.iFileSubstr(strTextData, 33, 11);
			if(!hblNo.equals(preHblNo) && !preHblNo.equals("")){
				if("C".equals(this.iFileSubstr(arrHBLInfo.get("200000"), 32, 1))){
					String consolidatedClearanceFlag = this.iFileSubstr(arrHBLInfo.get("200000"), 181, 1);
					if("L".equals(consolidatedClearanceFlag) || "V".equals(consolidatedClearanceFlag)){
						eFileDataSetting(arrHBLInfo, arrHBLInv, arrHBLChild, errorStr, search, dataList, upsBasicList);
					}
				}else{
					eFileDataSetting(arrHBLInfo, arrHBLInv, arrHBLChild, errorStr, search, dataList, upsBasicList);
				}
				arrHBLInfo.clear();
				arrHBLInv.clear();
				arrHBLChild.clear();
				arrTargetText.clear();
			}
		}
		
		if("200000".equals(strTextType) || "202000".equals(strTextType) || "300000".equals(strTextType) || "400000".equals(strTextType) || "401000".equals(strTextType)){
			arrHBLInfo.put(strTextType, this.iFileGetEdiCharacter(strTextData, false));
		}else if(Util.null2int(strTextType) >= 500000 && Util.null2int(strTextType) <= 599000){
			arrHBLInv.add(this.iFileGetEdiCharacter(strTextData, false));
		}else if("60".equals(this.iFileSubstr(strTextType, 0, 2))){
			arrHBLChild.add(this.iFileGetEdiCharacter(strTextData, false));
		}
		arrTargetText.add(strTextData);
		
		return hblNo;
	}
	public boolean eFileDataSetting(Map<String, String> arrHBLInfo, List<String> arrHBLInv, List<String> arrHBLChild, StringBuffer errorStr, SearchManifestFileList search
			, List<ManifestFile> dataList, List<Map<String, Object>> upsBasicList) throws Exception {
		String strGroupName = "";
		String hblNo = this.iFileSubstr(arrHBLInfo.get("200000"), 33, 11).trim();
		String countryCode = this.iFileSubstr(arrHBLInfo.get("200000"), 6, 2);
		String portCode = this.iFileSubstr(arrHBLInfo.get("200000"), 8, 4);
				
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		if("Y".equals(search.getUploadSearch())){	/** 추가건 선택 */
			tmpMap.put("seqNoId", search.getOdJobNo());
		}else{
			tmpMap.put("srchBound", "AIR");
			tmpMap.put("srchBoundType", "EXP");
			tmpMap.put("srchCountryCode", countryCode);
			List<String> srchCityPortIn = new ArrayList<String>();
			srchCityPortIn.add(portCode);
			srchCityPortIn.add("");
			tmpMap.put("srchCityPortIn", srchCityPortIn);
			tmpMap.put("sortStr", "ORDER BY CITY_PORT DESC");
			tmpMap.put("top1Yn", "Y");
			List<Map<String, Object>> upsBoundList = manifestFileMapper.selectUpsBound(tmpMap);
			if(upsBoundList.size() > 0){
				strGroupName = Util.null2str(upsBoundList.get(0).get("GROUP_NAME"));
			}else{
				errorStr.append(hblNo + " : 국가 - " + countryCode + ",Port - " + portCode + " 해당 건이 누락 되었습니다.<br />");
			}
			
			tmpMap.clear();
			tmpMap.put("srchBound", "AIR");
			tmpMap.put("srchBoundType", "EXP");
			tmpMap.put("srchGroupName", strGroupName);
			tmpMap.put("srchRunType", "Y");
			List<String> srchServiceCodeInList = new ArrayList<String>();
			srchServiceCodeInList.add(this.iFileSubstr(arrHBLInfo.get("200000"), 219, 1).trim());
			if(!srchServiceCodeInList.get(0).equals("")){
				srchServiceCodeInList.add("");
			}
			tmpMap.put("srchEntCode", search.getSrchEntCode());
			tmpMap.put("srchServiceCodeInList", srchServiceCodeInList);
			tmpMap.put("sortStr", "ORDER BY ups.SERVICE_CODE DESC, ups.SEQ_NO_ID DESC");
		}
		
		tmpMap.put("top1Yn", "Y");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String todayFormattedDateTime = now.format(formatter);

        tmpMap.put("currentDate", todayFormattedDateTime);
		List<Map<String, Object>> upsGroupList = manifestFileMapper.selectUpsGroup(tmpMap);
//		if(upsGroupList.size() > 0){
			double gwt = 0;
			String strCommodity = "";
			String strAMSAddress = "";
			String strItemCode = "";
			String strETrade = "";
			String strChildNumber = "";
//			double dblValue = 0;
			
//			#region - "파일생성"
//			if (txtTargetFileName.Text.Length > 0)
//			{
//				if (EtcLibrary.getSubStringK(_arrHBLInfo[0], 219, 1) == "5")
//				{
//					file = new FileStream(txtTargetFileName.Text + "Weps.TXT", FileMode.Append, FileAccess.Write);
//					sw = new StreamWriter(file);
//					for (int i = 0; i < _nTargetText; i++)
//					{
//						if (_arrTargetText[i] != "")
//							sw.WriteLine(_arrTargetText[i]);
//					}
//					sw.Close();
//				}
//				else if (EtcLibrary.getSubStringK(_arrHBLInfo[0], 72, 1) == "N" && EtcLibrary.getSubStringK(_arrHBLInfo[0], 219, 1) != "5")
//				{
//					file = new FileStream(txtTargetFileName.Text + ds.Tables[0].Rows[0]["GroupName"].ToString() + ".TXT", FileMode.Append, FileAccess.Write);
//					sw = new StreamWriter(file);
//					for (int i = 0; i < _nTargetText; i++)
//					{
//						if (_arrTargetText[i] != "")
//							sw.WriteLine(_arrTargetText[i]);
//					}
//					sw.Close();
//				}
//				else if (EtcLibrary.getSubStringK(_arrHBLInfo[0], 72, 1) == "D" || EtcLibrary.getSubStringK(_arrHBLInfo[0], 72, 1) == "L")
//				{
//					file = new FileStream(txtTargetFileName.Text + ds.Tables[0].Rows[0]["GroupName"].ToString() + ".DOC", FileMode.Append, FileAccess.Write);
//					sw = new StreamWriter(file);
//					for (int i = 0; i < _nTargetText; i++)
//					{
//						if (_arrTargetText[i] != "")
//							sw.WriteLine(_arrTargetText[i]);
//					}
//					sw.Close();
//				}
//			}
//			#endregion
//
//			if (chkLD.Checked == false && EtcLibrary.getSubStringK(_arrHBLInfo[0], 72, 1) == "P")   //Pallet 건은 생성 가능하도록 한다.(2013.01.04 박재남)
//			{
//			}
//			//else
//			//	if (chkLD.Checked == false && EtcLibrary.getSubStringK(_arrHBLInfo[0], 72, 1) != "N") return;
//
//			if (chkDoc.Checked == false && EtcLibrary.getSubStringK(_arrHBLInfo[0], 72, 1) != "N") return;

			//#region - "중량"
			String shipmentType = this.iFileSubstr(arrHBLInfo.get("200000"), 32, 1);
			String consolidatedClearanceFlag = this.iFileSubstr(arrHBLInfo.get("200000"), 181, 1);
			String documentType = this.iFileSubstr(arrHBLInfo.get("200000"), 72, 1);
			if(shipmentType.equals("C")){
				// 중량단위가 없을 경우에는 무조건 KGS로 처리한다. 2011.12.16
				if(consolidatedClearanceFlag.equals("L") || consolidatedClearanceFlag.equals("V")){
					if(this.iFileSubstr(arrHBLInfo.get("401000"), 326, 3).equals("LBS")){
						gwt = Util.null2dbl((Util.null2dbl(this.iFileSubstr(arrHBLInfo.get("401000"), 329, 14).trim()) / 2.205 * 10 + 0.5) / 10, 1, "U");
					}else{
						gwt = Util.null2dbl(Util.null2dbl(this.iFileSubstr(arrHBLInfo.get("401000"), 329, 14).trim()) / 10, 1, "U");
					}
					//if (EtcLibrary.getSubStringK(_arrHBLInfo[4], 326, 3).Trim() == "KGS")
					//	strGWT = Convert.ToDouble(double.Parse(SetDataVal(EtcLibrary.getSubStringK(_arrHBLInfo[4], 329, 14).Trim())) / 10).ToString("0.0");
					//else if (EtcLibrary.getSubStringK(_arrHBLInfo[4], 326, 3).Trim() == "LBS")
					//	strGWT = Convert.ToDouble((double.Parse(SetDataVal(EtcLibrary.getSubStringK(_arrHBLInfo[4], 329, 14).Trim())) / 2.205 * 10 + 0.5) / 10).ToString("0.0");
				}
			}else{
				// 중량단위가 없을 경우에는 무조건 KGS로 처리한다. 2011.12.16
				if(this.iFileSubstr(arrHBLInfo.get("200000"), 81, 3).trim().equals("LBS")){
					gwt = Util.null2dbl((Util.null2dbl(this.iFileSubstr(arrHBLInfo.get("202000"), 85, 7).trim()) / 2.205 * 10 + 0.5) / 10, 1, "U");
				}else{
					gwt = Util.null2dbl(Util.null2dbl(this.iFileSubstr(arrHBLInfo.get("202000"), 85, 7).trim()) / 10, 1, "U");
				}

				//if (EtcLibrary.getSubStringK(_arrHBLInfo[0], 81, 3).Trim() == "KGS")
				//	strGWT = Convert.ToDouble(double.Parse(SetDataVal(EtcLibrary.getSubStringK(_arrHBLInfo[1], 85, 7).Trim())) / 10).ToString("0.0");
				//else if (EtcLibrary.getSubStringK(_arrHBLInfo[0], 81, 3).Trim() == "LBS")
				//	strGWT = Convert.ToDouble((double.Parse(SetDataVal(EtcLibrary.getSubStringK(_arrHBLInfo[1], 85, 7).Trim())) / 2.205 * 10 + 0.5) / 10).ToString("0.0");
			}
			//#endregion

			//#region - "품명"
			double itemAmountCur = 0;
			for(int nX = 0; nX < arrHBLInv.size(); nX++){
				if(nX == 0){
					strCommodity = this.iFileSubstr(arrHBLInv.get(0), 57, 100).trim();
				}else{
					if(this.iFileSubstr(arrHBLInv.get(nX), 57, 100).trim().equals("")){
						break;
					}else{
						strCommodity += " / " + this.iFileSubstr(arrHBLInv.get(nX), 57, 100).trim();
					}
				}
				itemAmountCur += Util.null2dbl(this.iFileSubstr(arrHBLInv.get(nX), 161, 10).trim());
			}

			if(!documentType.equals("N") && !documentType.equals("P")){
				strCommodity = "DOCUMENT";
			}
			//#endregion

			//#region - "Child Tracking Number"
			for (int nX = 0; nX < arrHBLChild.size(); nX++){
				if(!this.iFileSubstr(arrHBLChild.get(nX), 166, 35).trim().equals("")){
					strChildNumber += this.iFileSubstr(arrHBLChild.get(nX), 166, 35).trim() + "|";
				}
			}

			if(!strChildNumber.equals("")){
				strChildNumber = strChildNumber.substring(0,strChildNumber.length() - 1);
			}
			//#endregion

			//#region - "통관금액"
			//dblValue = (double.Parse(SetDataVal(EtcLibrary.getSubStringK(_arrHBLInfo[0], 307, 11).Trim())) - double.Parse(SetDataVal(EtcLibrary.getSubStringK(_arrHBLInfo[0], 153, 9).Trim())) - double.Parse(SetDataVal(EtcLibrary.getSubStringK(_arrHBLInfo[0], 141, 9).Trim())) - double.Parse(SetDataVal(EtcLibrary.getSubStringK(_arrHBLInfo[0], 241, 11).Trim())) + double.Parse(SetDataVal(EtcLibrary.getSubStringK(_arrHBLInfo[0], 106, 9).Trim()))) / 100;
			//if (dblValue <= 0)
			//	htError_Message.Add(htError_Message.Count.ToString().Trim(), EtcLibrary.getSubStringK(_arrHBLInfo[0], 33, 11).Trim() + "||통관금액이 0 이거나 음수 입니다.");
			//#endregion

			//세번부호(2014-09-24)
			strItemCode = this.iFileSubstr(arrHBLInv.get(0), 196, 10).replaceAll("[^0-9]", "");

			if(strItemCode.length() != 10){
				strItemCode = "";
			}

			if(strCommodity.equals("")){
				errorStr.append(hblNo + " : 품명이 없습니다.");
			}

			if (shipmentType.equals("C")){
				if (Util.null2int(this.iFileSubstr(arrHBLInfo.get("401000"), 319, 7)) > 99) {
					errorStr.append(hblNo + " : 국가코드: " + countryCode.trim() + " 수량 OVER");
				}
			}else{
				if(Util.null2int(this.iFileSubstr(arrHBLInfo.get("202000"), 99, 5)) > 99) {
					errorStr.append(hblNo + " : 국가코드: " + countryCode.trim() + " 수량 OVER");
				}
			}
			
			String shipperName = this.iFileSubstr(arrHBLInfo.get("300000"), 68, 35).trim();
			strETrade = "99";
			if(!shipperName.equals("")){
				for(Map<String, Object> upsBasicMap : upsBasicList){
					if(strETrade.equals("01")){
						break;
					}
					for(String shipperNamsSplit : shipperName.replace(" ", "|").split("\\|")){
						if(shipperNamsSplit.equals(Util.null2str(upsBasicMap.get("ETC2")).trim())){
							strETrade = Util.null2str(upsBasicMap.get("ETC1")).trim();
						}
					}
				}
			}

			ManifestFile data = new ManifestFile();
			data.setNum(dataList.size() + 1);
			data.setCreateYn("N");
			data.setOrigCode("ICN");
			data.setOrigName("INCHEON AIRPORT,KOREA"); 
			if(!upsGroupList.isEmpty()){
				String obDate = Util.null2str(upsGroupList.get(0).get("OB_DATE"));
				data.setMblNo(Util.null2str(upsGroupList.get(0).get("MBL_NO")).trim());
				data.setDestCode(Util.null2str(upsGroupList.get(0).get("DEST_CODE")).trim());
				data.setDestName(Util.null2str(upsGroupList.get(0).get("DEST_NAME")).trim());
				data.setDestCountry(Util.null2str(upsGroupList.get(0).get("DEST_COUNTRY")).trim());
				
				data.setObDate(obDate);
				data.setObTime(Util.null2str(upsGroupList.get(0).get("OB_TIME")).trim());
				data.setArDate(obDate);
	//			spMainList.SetData(_nHouseCount, "FlightNo", ds.Tables[0].Rows[0]["AirlineCode"].ToString().Trim() + ds.Tables[0].Rows[0]["FlightNo"].ToString().Trim());
				data.setLineCode(Util.null2str(upsGroupList.get(0).get("LINE_CODE")).trim());
				data.setVesselFlight(Util.null2str(upsGroupList.get(0).get("VESSEL_FLIGHT")).trim());
			}
			data.setHblNo(hblNo);

			data.setTrackingNo(this.iFileSubstr(arrHBLInfo.get("202000"), 50, 35).trim());
			if(!strChildNumber.equals("")){
				data.setTrackingNoStr(data.getTrackingNo() + "|" + strChildNumber);
			}else{
				data.setTrackingNoStr(data.getTrackingNo());
			}
			data.setCommodityItem(this.iFileSubstr(strCommodity.trim(), 0, 200).replace("'", "`").replace(",", "."));

			if(documentType.equals("L") || documentType.equals("D")){
				//spMainList.SetData(_nHouseCount, "CustomsTypeYN", "D");
//				data.setCdType("Y");   //2020.09.17 : DOC건은 목록으로 생성한다. 2026-02-19 통관타입은 화면에서 선택한 값으로 한다.
				data.setCurrencyCode("KRW");
				data.setAmountCur(1);
			}else{
//				data.setCdType("N");   //서보국과장 요청 생성될때는 무조건 일반건으로 생성한다.(2011-12-19). 2026-02-19 통관타입은 화면에서 선택한 값으로 한다.
				data.setCurrencyCode(this.iFileSubstr(arrHBLInfo.get("200000"), 175, 3).trim());
				double amountCur = Util.null2dbl(this.iFileSubstr(arrHBLInfo.get("200000"), 307, 11).trim());
				if(amountCur == 0){
					amountCur = itemAmountCur;
				}
				if(amountCur <= 0){
					errorStr.append(hblNo + " : Invoice Amount 가 0입니다.");
					data.setAmountCur(0);
				}else{
					data.setAmountCur(amountCur / 100);
				}
			}
			
			if (data.getCurrencyCode().equals("")){
				errorStr.append(hblNo + " : 통화단위가 없습니다.");
			}

			//spMainList.SetData(_nHouseCount, "AmountTotal", dblValue);

			if(shipmentType.equals("C")){
				data.setPieces(Util.null2int(this.iFileSubstr(arrHBLInfo.get("401000"), 319, 7).trim()));
			}else{
				data.setPieces(Util.null2int(this.iFileSubstr(arrHBLInfo.get("202000"), 99, 5).trim()));
			}
			data.setGwt(gwt);
			data.setShipperName(this.iFileSubstr(arrHBLInfo.get("300000"), 68, 35).trim().replace("'", "`"));
//			spMainList.SetData(_nHouseCount, "ShipperAddress", "ADDR " + EtcLibrary.getSubStringK(_arrHBLInfo[2], 103, 35).trim());
//			spMainList.SetData(_nHouseCount, "ShipperAddressA", EtcLibrary.getSubStringK(_arrHBLInfo[2], 138, 35).trim());
//			spMainList.SetData(_nHouseCount, "ShipperAddressB", EtcLibrary.getSubStringK(_arrHBLInfo[2], 173, 35).trim());
			data.setShipperAddress("ADDR " + this.iFileSubstr(arrHBLInfo.get("300000"), 103, 35).trim() + this.iFileSubstr(arrHBLInfo.get("300000"), 138, 35).trim() + this.iFileSubstr(arrHBLInfo.get("300000"), 173, 35).trim());
			data.setShipperAddress(data.getShipperAddress().replace("'", "`"));

			data.setConsigneeName(this.iFileSubstr(arrHBLInfo.get("400000"), 68, 35).trim().replace("'", "`"));
//			spMainList.SetData(_nHouseCount, "ConsigneeAddress", "ADDR " + EtcLibrary.getSubStringK(_arrHBLInfo[3], 128, 35).trim());
//			spMainList.SetData(_nHouseCount, "ConsigneeAddressA", EtcLibrary.getSubStringK(_arrHBLInfo[3], 163, 35).trim());
//			spMainList.SetData(_nHouseCount, "ConsigneeAddressB", EtcLibrary.getSubStringK(_arrHBLInfo[3], 198, 35).trim());
			data.setConsigneeAddress("ADDR " + this.iFileSubstr(arrHBLInfo.get("400000"), 128, 35).trim() + this.iFileSubstr(arrHBLInfo.get("400000"), 163, 35).trim() + this.iFileSubstr(arrHBLInfo.get("400000"), 198, 35).trim());
			data.setConsigneeAddress(data.getConsigneeAddress().replace("'", "`"));
			data.setAmsYn("N");
			data.setShipperCountryCode(this.iFileSubstr(arrHBLInfo.get("200000"), 0, 2));

			/** SHIPPER BUILDING(35=>17) + SHIPPER STREET(35) + SHIPPER CITY(35)  =>  ShipperPlace(17) + ShipperStreet(35)  ? */
			strAMSAddress = this.iFileSubstr(arrHBLInfo.get("300000"), 103, 17).trim() + this.iFileSubstr(arrHBLInfo.get("300000"), 138, 35).trim() + this.iFileSubstr(arrHBLInfo.get("300000"), 173, 35).trim();
			if(!this.iFileSubstr(strAMSAddress, 0, 17).trim().equals("")){
				data.setShipperPlace(this.iFileSubstr(strAMSAddress, 0, 17).trim());
			}else{
				data.setShipperPlace("N/A");
			}
			if(!this.iFileSubstr(strAMSAddress, 17, 35).trim().equals("")){
				data.setShipperStreet(this.iFileSubstr(strAMSAddress, 17, 35).trim());
			}else{
				data.setShipperStreet("N/A");
			}
			data.setConsigneeCountryCode(countryCode);

			//strAMSAddress = EtcLibrary.getSubStringK(_arrHBLInfo[3], 103, 17).Trim() + EtcLibrary.getSubStringK(_arrHBLInfo[3], 138, 35).Trim() + EtcLibrary.getSubStringK(_arrHBLInfo[3], 173, 35).Trim();
			strAMSAddress = this.iFileSubstr(arrHBLInfo.get("400000"), 128, 17).trim() + this.iFileSubstr(arrHBLInfo.get("400000"), 163, 35).trim() + this.iFileSubstr(arrHBLInfo.get("400000"), 198, 35).trim();
			if(!this.iFileSubstr(strAMSAddress, 0, 17).trim().equals("")){
				data.setConsigneePlace(this.iFileSubstr(strAMSAddress, 0, 17).trim().replace("'", "`"));
			}else{
				data.setConsigneePlace("N/A");
			}
			if(!this.iFileSubstr(strAMSAddress, 17, 35).trim().equals("")){
				data.setConsigneeStreet(this.iFileSubstr(strAMSAddress, 17, 35).trim().replace("'", "`"));
			}else{
				data.setConsigneeStreet("N/A");
			}
			if(countryCode.trim().equals("MX")){	   // 멕시코는 주코드를 "MX"로 고정한다.  AMS신고 안하며 FHL4필수항목이라. 2012.04-06
				data.setConsigneeStateCode("MX");
			}else{
				data.setConsigneeStateCode(this.iFileSubstr(arrHBLInfo.get("400000"), 253, 2).trim());
			}
			
			data.setShipperTel(this.iFileSubstr(arrHBLInfo.get("300000"), 242, 14).replaceAll("[^0-9]", ""));
			data.setShipperFax(this.iFileSubstr(arrHBLInfo.get("300000"), 256, 14).replaceAll("[^0-9]", ""));
			data.setShipperZipcode(this.iFileSubstr(arrHBLInfo.get("300000"), 230, 9).replaceAll("[^0-9]", ""));

			data.setConsigneeTel(this.iFileSubstr(arrHBLInfo.get("400000"), 267, 14).replaceAll("[^0-9]", ""));
			data.setConsigneeFax(this.iFileSubstr(arrHBLInfo.get("400000"), 281, 14).replaceAll("[^0-9]", ""));

			if(countryCode.trim().equals("CA") || countryCode.trim().equals("GB")){	// 케나다 || 영국 은 우편번호에 문자가 포함된다.
				data.setConsigneeZipcode(this.iFileSubstr(arrHBLInfo.get("400000"), 255, 9).trim());
			}else{
				data.setConsigneeZipcode(this.iFileSubstr(arrHBLInfo.get("400000"), 255, 9).replaceAll("[^0-9]", ""));
			}

			data.setEcmType(strETrade);	//거래구분
			data.setAllowedItemCode(strItemCode);	//세번부호
			//spMainList.SetData(_nHouseCount, "ItemCode", "");		//세번부호

			if(documentType.equals("D") || documentType.equals("L")){
				data.setDocumentYn("Y");
			}else{
				data.setDocumentYn("N");
			}
			
			//비고
			data.setRemarks(this.iFileSubstr(arrHBLInfo.get("300000"), 58, 10).trim()
					+ "|" + this.iFileSubstr(arrHBLInfo.get("200000"), 8, 4).trim()
					+ "|" + this.iFileSubstr(arrHBLInfo.get("200000"), 219, 1).trim()   //ShipperCode, Port 4Code, Service 1Code 2012.07.18 추가(수출 TS건 처리관련작업)
					+ "|" + this.iFileSubstr(arrHBLInfo.get("200000"), 6, 2).trim());	  //Destination CountryCode
			
			dataList.add(data);
//		}
		return true;
	}
	
	/**
	 * 수출적하목록 파일업로드 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult saveEFile(String entCode, SearchManifestFileList search, HttpServletRequest req) throws Exception {
		OrderSaveResult result = new OrderSaveResult();
		Util.accessControlDatabase(search, "gmtTime", false, null, null, null, null);
		
		search.setCustCodeLv1("UPS-EXP");
		search.setFinishYn("Y");
		
		int maxHsn = 1;
		StringBuffer errorStr = new StringBuffer();
		Map<String, String> createHblData = new HashMap<String, String>();
		Map<String, Object> ediMap;
		Map<String, String> checkMap = new HashMap<String, String>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		int stockSeqNo = 1;
		Map<String, Object> stockMap = new HashMap<String, Object>();
		stockMap.put("entCode", entCode);
		stockMap.put("gmtTime", search.getGmtTime());
		stockMap.put("entBusinessCode", "");
		stockMap.put("updateUserId", search.getUserId());
		stockMap.put("solution", "ECM");
		stockMap.put("stockType", "OD");
		stockMap.put("stockSign", "COD");
		stockMap.put("stockSeqLength", 8);
		stockMap.put("dateLength", null);
		stockMap.put("signType", null);
		stockMap.put("stockDateFormat", null);
		stockMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		stockMap.put("stockRemark", "ORDER-JOB NO");
		
		
		Map<String, SearchManifestFileList> mblCheckMap = new HashMap<String, SearchManifestFileList>();
		SearchManifestFileList mbl;
		Map<String, Object> mblParamMap = new HashMap<String, Object>();
		mblParamMap.put("bound", search.getBound());
		mblParamMap.put("boundType", search.getBoundType());
		mblParamMap.put("selectMaxHsn", "Y");
		
		/** 2024-11-11_2 정상처리되지 않은건도 그리드 표현하지 않도록 변경함 */
//		List<ManifestFile> errorList = new ArrayList<ManifestFile>();
		List<ManifestFile> list = search.getManifestFileList();
		List<ManifestFile> insertList = new ArrayList<ManifestFile>();
		List<SearchManifestFileList> insertMasterList = new ArrayList<SearchManifestFileList>();
		
		for(ManifestFile data : list){
			if(!Util.isEmpty(data.getMblNo()) && Util.isEmpty(data.getObTime())){
				resultMap.put("errorStr", "출항시간을 입력바랍니다.");
				result.setResultData(resultMap);
				return result;
			}
			
			if(checkMap.get(data.getHblNo()) != null){
				errorStr.append(data.getHblNo() + " : 동일한 H.B/L이 존재합니다.<br />");
				checkMap.put(data.getHblNo(), "error");
			}else{
				checkMap.put(data.getHblNo(), "Y");
			}
		}
		
		tmpMap.clear();
		tmpMap.put("srchEntCode", entCode);
		tmpMap.put("srchEntBusinessCode", search.getEntBusinessCode());
		tmpMap.put("srchBound", search.getBound());
		tmpMap.put("srchEdiCityPort", "ICN");
		ediMap = manifestFileMapper.selectBusEdiCodeInfo(tmpMap);
		
		tmpMap.clear();
		tmpMap.put("srchEntCode", entCode);
		tmpMap.put("srchEntBusinessCode", search.getEntBusinessCode());
		tmpMap.put("srchBound", search.getBound());
		tmpMap.put("srchBoundType", search.getBoundType());
		tmpMap.put("srchEdiCityPort", "ICN");
		tmpMap = commonMapper.selectBusEdiEcmInfo(tmpMap);
		if(tmpMap != null && tmpMap.size() > 0){
			search.setEdiMailboxSeqNo(Util.null2str(tmpMap.get("SEQ_NO")));
		}
		
		tmpMap = new HashMap<String, Object>();
		tmpMap.put("entCode", entCode);
		tmpMap.put("entBusinessCode", search.getEntBusinessCode());
		tmpMap.put("solution", "ECM");
		tmpMap.put("optionCode", "C02");
		Map<String, Object> optionMap = commonMapper.selectSystemOption2(tmpMap);
		/** 항공일경우 중계사업자 설정값이 auto이면 편명의 중계사업자로 저장 */
		if("auto".equals(optionMap.get("SET_VALUE"))){
			String venderId = "";
			if(!Util.isEmpty(search.getLineCode())){
				tmpMap.put("groupCode", "AIRLINE_CODE");
				tmpMap.put("srchEtcFieldOnly", "Y");
				tmpMap.put("srchEtcField", "ETC2");
				tmpMap.put("srchEtcFieldValue", search.getLineCode());
				tmpMap = commonMapper.getCodeDetail(tmpMap);
				if(tmpMap != null){
					venderId = Util.null2str(tmpMap.get("ETC11"));
				}else{
					tmpMap = new HashMap<String, Object>();
				}
			}
			if(venderId.equals("")){
				search.setVenderId(Util.null2str(optionMap.get("DEFAULT_VALUE")));
			}else{
				search.setVenderId(venderId);
			}
		}else if(Util.isEmpty(optionMap.get("SET_VALUE"))){
			search.setVenderId(Util.null2str(optionMap.get("DEFAULT_VALUE")));
		}else{
			search.setVenderId(Util.null2str(optionMap.get("SET_VALUE")));
		}
		
		SearchManifestCustoms searchCustoms = new SearchManifestCustoms();
		searchCustoms.setBoundType(search.getBoundType());
		searchCustoms.setSrchBusEdiCodeYn("Y");
		searchCustoms.setEntBusinessCode(search.getEntBusinessCode());
		searchCustoms.setBound(search.getBound());
		searchCustoms.setEntCode(search.getSrchEntCode());
		searchCustoms.setPortCode("ICN");
		Map<String, Object> customsCodeMap = manifestMapper.getCustomsCode(searchCustoms);
		
		
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		String description = "NEW ORDER";
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		Map<String, Object> logMap;
		tmpLogMap.put("job_func_type", "ADD");
		tmpLogMap.put("ent_code", entCode);
		tmpLogMap.put("ent_business_code", search.getEntBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("bound", search.getBound());
		tmpLogMap.put("bound_type", search.getBoundType());
		tmpLogMap.put("process_user_id", search.getUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("ent_business_print_name", search.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", search.getLogUserName());
		tmpLogMap.put("process_user_name_eng", search.getLogUserNameEng());
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		if (req != null)	tmpLogMap.put("access_ip", Util.getClientIP(req));
		
		Map<String, Map<String, Object>> exchangeRateMap = new HashMap<String, Map<String, Object>>();
		
		int seq = 1;
		
		tmpMap.clear();
		for (ManifestFile data : list){
			if("error".equals(checkMap.get(data.getHblNo()))){
//				errorList.add(data);
				continue;
			}
			mbl = mblCheckMap.get(data.getMblNo());
			if(mbl == null && data.getMblNo() != null){
				mblParamMap.put("mblNo", data.getMblNo());
				mbl = manifestFileMapper.selectMasterInfo2(mblParamMap);
				if(mbl == null){
					mbl = new SearchManifestFileList(search);
					stockMap.put("seqNo", stockSeqNo++);
					mbl.setOdJobNo(commonMapper.selectNextStockMaxNo(stockMap));
					mbl.setMblNo(data.getMblNo());
					mbl.setLineCode(data.getLineCode());
					mbl.setVesselFlight(data.getVesselFlight());
					mbl.setOrigCode("ICN");
					mbl.setOrigName("INCHEON AIRPORT,KOREA");
					mbl.setOrigCountry("KR");
					mbl.setObDate(data.getObDate());
					mbl.setObTime(data.getObTime());
					mbl.setArDate(data.getObDate());
					mbl.setDestCode(data.getDestCode());
					mbl.setDestName(data.getDestName());
					mbl.setDestCountry(data.getDestCountry());
//					mbl.setArTime();
					if(customsCodeMap != null){
						mbl.setCustomsCode(Util.null2str(customsCodeMap.get("CUSTOMS_CODE")));
						mbl.setCustomsDptCode(Util.null2str(customsCodeMap.get("CUSTOMS_DPT_CODE")));
					}
					mbl.setEdiMailboxSeqNo(search.getEdiMailboxSeqNo());
					mbl.setAction("insert");
					insertMasterList.add(mbl);
				}else{
					mbl.setSrchEntCode(search.getSrchEntCode());
					mbl.setEntBusinessCode(search.getEntBusinessCode());
					mbl.setBound(search.getBound());
					mbl.setBoundType(search.getBoundType());
					mbl.setGmtTime(search.getGmtTime());
					mbl.setConsolYn(search.getConsolYn());
					mbl.setCustCodeLv1(search.getCustCodeLv1());
					mbl.setDateFormat(search.getDateFormat());
					mbl.setUserId(search.getUserId());
				}
				mblCheckMap.put(data.getMblNo(), mbl);
				
			}
			
			if(mbl != null && !"Y".equals(mbl.getFinishYn())){
				errorStr.append(data.getMblNo() + " - " + data.getHblNo() + " : 마감되지않은 마스터번호 입니다.<br />");
//				errorList.add(data);
				continue;
			}
			
			if(mbl != null && mbl.getMblNo() != null){
				maxHsn = Util.null2int(mbl.getHsn()) + 1;
				if(maxHsn > 9999){
					errorStr.append(data.getHblNo() + " : HSN 범위를 초과 하여 생성 하지 않았습니다.<br />");
	//				errorList.add(data);
					continue;
				}
			}
			
			tmpMap.put("hblNo", data.getHblNo());
			if(mbl != null){
				tmpMap.put("masterOdJobNo", mbl.getOdJobNo());
				//ICNHUB 계정으로 업로드하는 경우에는 반입계의 테이블을 한번더 검색하여 반입계(반입계의 목록통관(F)건은 예외)에 하우스빌번호를 체크한다.(16.05.12)
				if((!"insert".equals(mbl.getAction()) && manifestFileMapper.selectHouseInfoList(tmpMap).size() > 0)){
					errorStr.append(data.getHblNo() + " : 동일한 H.B/L이 존재합니다.<br />");
	//				errorList.add(data);
					continue;
				}
			}
			
			if("ICNHUB".equals(search.getUserName()) && manifestFileMapper.selectEpnList(tmpMap).size() > 0) {
				errorStr.append(data.getHblNo() + " : 동일한 H.B/L이 존재합니다.<br />");
//				errorList.add(data);
				continue;
			}
			
            if(mbl != null){
                mbl.setHsn(Util.null2str(maxHsn));
            }
			
            data.setCdType(search.getSrchCdType());
			
			if(data.getCdType().equals("Y") && ediMap != null){
				data.setEntBusinessCode("");
				data.setUpdateUserId(search.getUserId());
				data.setStockType("OD");
				data.setStockSeqLength("8");
				data.setDateLength("4");
				data.setStockDateFormat("112");
				data.setSignType("noHyphen");
				data.setResultCode("FIXED_SIGN_AND_SEQ_NO");
				data.setStockRemark("ORDER SIMPLE DOC NO");
				data.setSeqNo(stockSeqNo++);
				
				if("Y".equals(data.getDocumentYn())){
					data.setStockSign(Util.null2str(ediMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y")));
				}else{
					data.setStockSign(Util.null2str(ediMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N")));
				}
				data.setSimpleDocNo("create");
			}
			
			data.setEntCode(entCode);
			data.setHeadEntBusinessCode(search.getEntBusinessCode());
			data.setSolution("ECM");
			data.setGmtTime(search.getGmtTime());
			data.setDateFormat(search.getDateFormat());
			data.setCustCodeLv1("UPS-EXP");
			data.setBlTypeMh("H");
			data.setCustomsIdType("01");
			data.setInspectionYn("N");
			data.setMultiYn("N");
			data.setHoldYn("N");
			data.setFtaYn("Y");
			data.setFinishYn("Y");
			data.setUserId(search.getUserId());
            data.setBound(search.getBound());
            data.setBoundType(search.getBoundType());
			
			if(mbl != null && mbl.getMblNo() != null){
				data.setMasterOdJobNo(mbl.getOdJobNo());
				data.setMblNo(mbl.getMblNo());
				data.setMrn(mbl.getMrn());
				data.setMsn(mbl.getMsn());
				data.setHsn(Util.lpad(Util.null2str(maxHsn), 4, '0'));
				data.setLineCode(mbl.getLineCode());
				data.setVesselFlight(mbl.getVesselFlight());
				data.setBlTypeIert("create");
				data.setDestCode(mbl.getDestCode());
				data.setOrigCode(mbl.getOrigCode());
				data.setArDate(mbl.getArDate());
				data.setArTime(mbl.getArTime());
				data.setObDate(mbl.getObDate());
				data.setObTime(mbl.getObTime());
				data.setBound(mbl.getBound());
				data.setBoundType(mbl.getBoundType());
				data.setConsolYn("Y");
				data.setOrigName(mbl.getOrigName());
				data.setOrigCountry(mbl.getOrigCountry());
				data.setDestName(mbl.getDestName());
				data.setDestPort(mbl.getDestPort());
				data.setDestCountry(mbl.getDestCountry());
				data.setCustomsCode(mbl.getCustomsCode());
				data.setCustomsDptCode(mbl.getCustomsDptCode());
			}else {
                data.setConsolYn("N");
                if(customsCodeMap != null) {
                    data.setCustomsCode(Util.null2str(customsCodeMap.get("CUSTOMS_CODE")));
                    data.setCustomsDptCode(Util.null2str(customsCodeMap.get("CUSTOMS_DPT_CODE")));
                }
	        }
			data.setEdiMailboxSeqNo(search.getEdiMailboxSeqNo());
			data.setVenderId(search.getVenderId());
            if(data.getMblNo() != null){
                data.setLastPodStatus("ECM055");	/** 콘솔마감 */
            }else{
                data.setLastPodStatus("ECM010");	/** 오더접수 */
            }
			data.setPodJobNo(DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
			data.setPodJobNo2(DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
			stockMap.put("seqNo", stockSeqNo++);
			data.setOdJobNo(commonMapper.selectNextStockMaxNo(stockMap));
			
			Map<String, Object> exchangeRate = exchangeRateMap.get(data.getObDate());
			if(exchangeRate == null){
				tmpMap.put("srchEntCode", search.getSrchEntCode());
				tmpMap.put("entBusinessCode", search.getEntBusinessCode());
				tmpMap.put("srchDateFormat", search.getSrchDateFormat());
				tmpMap.put("exrateType", "EXRATE_BUYING_CUSTOMS");
				tmpMap.put("srchDate", data.getObDate());
				exchangeRate = commonMapper.selectExchangeRate(tmpMap);
				if(Util.null2int(exchangeRate.get("CNT")) == 0){
					exchangeRate = new HashMap<String, Object>();
				}
				exchangeRateMap.put(data.getObDate(), exchangeRate);
			}
			
			
			data.setExchangeRate(Util.null2dbl(exchangeRate.get("EXCHANGE_RATE_" + data.getCurrencyCode())));
			if("KRW".equals(data.getCurrencyCode())){
				data.setAmountLoc(data.getAmountCur());
			}else{
				data.setAmountLoc(data.getAmountCur() * data.getExchangeRate());
			}
			if("USD".equals(data.getCurrencyCode())){
				data.setAmountUsd(data.getAmountCur());
			}else{
				data.setAmountUsd(data.getAmountLoc() * Util.null2dbl(exchangeRate.get("EXCHANGE_RATE_USD")));
			}
			
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpLogMap);
			logMap.put("job_no", data.getOdJobNo());
			logMap.put("description", description +"[HBL_NO : " + data.getHblNo()+"]");
			logMap.put("job_log", new Gson().toJson(data));
			logMap.put("reference_no_id", bulkData.getId());
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
			
			createHblData.put(data.getHblNo(), "Y");
			insertList.add(data);
		}

		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			ManifestFileMapper manifestFileBatchMapper = sqlSession.getMapper(ManifestFileMapper.class);
			int cnt = 1;
			for(SearchManifestFileList data : insertMasterList){
				manifestFileBatchMapper.insertMasterOrder(data);
				if((cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			cnt = 1;
			for(ManifestFile data : insertList){
				manifestFileBatchMapper.insertHouseOrder(data);
				if((cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			cnt = 1;
			int seqNoId;
			String trackingNoStr;
			tmpMap.clear();
			for(ManifestFile data : insertList){
				trackingNoStr = data.getTrackingNoStr();
				if(!Util.isEmpty(trackingNoStr)){
					seqNoId = 1;
					for(String trackingNo : trackingNoStr.split("\\|")) {
						tmpMap.put("odJobNo", data.getOdJobNo());
						tmpMap.put("seqNoId", seqNoId++);
						tmpMap.put("trackingNo", trackingNo);
						manifestFileBatchMapper.insertOdChildNo(tmpMap);
						if((cnt++ % 1000) == 0){
							sqlSession.flushStatements();
							sqlSession.clearCache();
						}
					}
				}
			}
			
			sqlSession.flushStatements();
			sqlSession.clearCache();
			sqlSession.commit();
		}catch(Exception e){
			throw e;
		}finally{
			sqlSession.close();
		}
		commonService.insertLogBulk(bulkList);
		
		resultMap.put("createCount", insertList.size());
		resultMap.put("createHblData", createHblData);
		if(errorStr.length() > 0){
			resultMap.put("errorStr", errorStr.toString());
//			resultMap.put("errorList", errorList);
		}
		result.setResultData(resultMap);
		return result;
	}
	
	/**
	 * 임시마스터 리스트
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ManifestFileList selectMasterTempList(SearchManifestFileList search) throws Exception {
		ManifestFileList manifestFileList = new ManifestFileList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/* N개 검색(멀티타입) */
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(Util.null2str(search.getSrchNo()))){
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
		Util.accessControlDatabase(search, "srchDateFormat", true, "srchDateGbn", "mod.OB_DATE", null, null);
		List<ManifestFile> list  = manifestFileMapper.selectMasterTempList(search);
		manifestFileList.setManifestFileList(list);
		if(list.size() > 0){
			manifestFileList.setTotal(list.get(0).getTotal());
		}else {
			manifestFileList.setTotal(0);
		}
		return manifestFileList;
	}
	
	/**
	 * 전자상거래 임시마스터 - insert/update
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult saveMasterTemp(ManifestFile manifestFile) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		if("insert".equals(manifestFile.getAction())){
			Util.accessControlDatabase(manifestFile, "gmtTime", false, null, null, null, null);
			if(manifestFile.getServiceCode() == null) {
				manifestFile.setServiceCode("");
			}
			manifestFileMapper.insertMasterTemp(manifestFile);
		}else if("update".equals(manifestFile.getAction())){
			Util.accessControlDatabase(manifestFile, "gmtTime", false, null, null, null, null);
			manifestFileMapper.updateMasterTemp(manifestFile);
		}else if("delete".equals(manifestFile.getAction())){
			manifestFileMapper.deleteMasterTemp(manifestFile);
		}
		
		return orderSaveResult;
	}
	
	/**
	 * 임시마스터 그룹명
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public List<String> selectMasterTempGroupName(SearchManifestFileList search) throws Exception {
		return manifestFileMapper.selectMasterTempGroupName(search);
	}
	
	/**
	 * UPS기초코드 리스트
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ManifestFileList selectUpsBasicList(SearchManifestFileList search) throws Exception {
		ManifestFileList manifestFileList = new ManifestFileList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		
		List<ManifestFile> list  = manifestFileMapper.selectUpsBasicList(search);
		manifestFileList.setManifestFileList(list);
		if(list.size() > 0){
			manifestFileList.setTotal(list.get(0).getTotal());
		}else {
			manifestFileList.setTotal(0);
		}
		return manifestFileList;
	}
	
	/**
	 * UPS기초코드 - insert/update
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult saveUpsBasic(ManifestFile manifestFile) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		if("insert".equals(manifestFile.getAction())){
			if("E".equals(manifestFile.getBasicType()) || "F".equals(manifestFile.getBasicType())){
				Map<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("basicType", manifestFile.getBasicType());
				tmpMap.put("hblNo", manifestFile.getHblNo());
				if(manifestFileMapper.selectUpsBasicDupCheck(tmpMap).size() > 0){
					orderSaveResult.setResultCode("A00623");	/** 중복되는 번호가 있습니다. */
					return orderSaveResult;
				}
			}else if("C".equals(manifestFile.getBasicType()) || "D".equals(manifestFile.getBasicType())) {
				manifestFile.setEtc2(manifestFile.getCdEtc2());
			}
			Util.accessControlDatabase(manifestFile, "gmtTime", false, null, null, null, null);
			manifestFileMapper.insertUpsBasic(manifestFile);
		}else if("update".equals(manifestFile.getAction())){
			manifestFileMapper.updateUpsBasic(manifestFile);
		}else if("delete".equals(manifestFile.getAction())){
			manifestFileMapper.deleteUpsBasic(manifestFile);
		}
		orderSaveResult.setOdJobNo(manifestFile.getSeqNoId());
		return orderSaveResult;
	}
	
	/**
	 * UPS기초코드 - select
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ManifestFile selectUpsBasicEntry(ManifestFile manifestFile) throws Exception {
		return manifestFileMapper.selectUpsBasicEntry(manifestFile);
	}
	
	/**
	 * UPS그룹코드 리스트
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ManifestFileList selectUpsGroupList(SearchManifestFileList search) throws Exception {
		ManifestFileList manifestFileList = new ManifestFileList();
		if(search.getPage() != 0 && search.getPageSize() != 0){search.setStartNum(search.getPageSize() * (search.getPage() - 1));}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		
		Util.accessControlDatabase(search, "srchDateFormat", false, null, null, null, null);
		List<ManifestFile> list  = manifestFileMapper.selectUpsGroupList(search);
		manifestFileList.setManifestFileList(list);
		if(list.size() > 0){
			manifestFileList.setTotal(list.get(0).getTotal());
		}else {
			manifestFileList.setTotal(0);
		}
		return manifestFileList;
	}
	
	/**
	 * UPS그룹코드 - insert/update
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult saveUpsGroup(ManifestFile manifestFile) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		if("insert".equals(manifestFile.getAction()) || "update".equals(manifestFile.getAction())){

			if(Util.null2str(manifestFile.getGroupName()) == "" || (Util.null2str(manifestFile.getCountryCode()) == "" && Util.null2str(manifestFile.getCityPort()) == "")) {
				orderSaveResult.setResultCode("A00026");	/** 코드를 입력바랍니다. */
				return orderSaveResult;
			} else if(upsMngMapper.selectUpsGroupDup(manifestFile) > 0){
				orderSaveResult.setResultCode("A00623");	/** 중복되는 번호가 있습니다. */
				return orderSaveResult;
			}
			Util.accessControlDatabase(manifestFile, "gmtTime", false, null, null, null, null);

			if("insert".equals(manifestFile.getAction())) {
				manifestFileMapper.insertUpsGroup(manifestFile);
			} else {
				manifestFileMapper.updateUpsGroup(manifestFile);
			}
		}else if("delete".equals(manifestFile.getAction())){
			manifestFileMapper.deleteUpsGroup(manifestFile);
		}
		orderSaveResult.setOdJobNo(manifestFile.getSeqNoId());
		orderSaveResult.setResultCode("A11135"); /** 저장(업로드)이 완료 되었습니다. */
		return orderSaveResult;
	}
	
	/**
	 * UPS그룹코드 - select
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public ManifestFile selectUpsGroupEntry(ManifestFile manifestFile) throws Exception {
		return manifestFileMapper.selectUpsGroupEntry(manifestFile);
	}
	
	/**
	 * UPS : 항공수입 T/S HAWB 가져오기 - 항공수입 T/S HAWB 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public ManifestFileList selectTsHouseInList(SearchManifestFileList search) throws Exception {
		ManifestFileList manifestFileList = new ManifestFileList();
		if(search.getPage() != 0 && search.getPageSize() != 0){
			search.setStartNum(search.getPageSize() * (search.getPage() - 1));
		}
		if(Util.null2str(search.getChangeField()).equals("Y")){Util.camelCaseToUnderscore(search);}
		
		/* N개 검색(멀티타입) */
		String srchNo = null;
		srchNo = Util.null2str(search.getSrchNo());
		
		List<String> searchNoList = new ArrayList<String>();
		if(!"".equals(srchNo)) {
			searchNoList.addAll(Arrays.asList(StringUtils.deleteWhitespace(srchNo).split(",")));
		}
		
		if(searchNoList.size() > 1){
			search.setSearchType("multi");
			search.setSearchNoList(searchNoList);
		}else{
			srchNo = srchNo.trim().replace(",", "");
			search.setSearchType("");
			search.setSrchNo(srchNo);
		}
		Util.accessControlDatabase(search, "userLanguage|srchDateFormat|srchNoType", true, "srchDateGbn", "|od.AR_DATE|noDateChk", "srchNoType", "od.HBL_NO|od.MBL_NO");
		List<ManifestFile> list  = manifestFileMapper.selectTsHouseInList(search);
		
		for(ManifestFile data : list) {
			String[] remarksArr= Util.null2str(data.getRemarks()).split("\\|");
			if(remarksArr.length == 4) {
				data.setCityPort(remarksArr[1]);
				data.setServiceCode(remarksArr[2]); 
				data.setCountryCode(remarksArr[3]);
			}
		}
		
		/** 1.항공수출 MAWB 검색 : 각 하우스의 비고에 입력된 그룹코드, Country Code, Port Code에 매칭되는 항공수출 MAWB 입력 */
		if("getMbl".equals(Util.null2str(search.getAction()))) {
			this.getTsMaster(search.getSrchEntCode(), list);
		}
		
		manifestFileList.setManifestFileList(list);
		if(list != null && list.size() > 0) {
			manifestFileList.setTotal(list.get(0).getTotal());
		}else {
			manifestFileList.setTotal(0);
		}
		
		return manifestFileList;
	}
	
	public void getTsMaster(String entCode, List<ManifestFile> list) throws Exception {
		for(ManifestFile data : list) {
			String strGroupName = "";
			String portCode = Util.null2str(data.getCityPort());
			String serviceCode =  Util.null2str(data.getServiceCode());
			String countryCode =  Util.null2str(data.getCountryCode());
			if("".equals(portCode) || "".equals(serviceCode) || "".equals(countryCode)) continue;
			if("Y".equals(Util.null2str(data.getExpYn()))) continue;
			
			Map<String, Object> tmpMap = new HashMap<String, Object>();
			tmpMap.put("srchBound", "AIR");
			tmpMap.put("srchBoundType", "EXP");
			tmpMap.put("srchCountryCode", countryCode);
			List<String> srchCityPortIn = new ArrayList<String>();
			srchCityPortIn.add(portCode);
			srchCityPortIn.add("");
			tmpMap.put("srchCityPortIn", srchCityPortIn);
			tmpMap.put("sortStr", "ORDER BY CITY_PORT DESC");
			tmpMap.put("top1Yn", "Y");
			List<Map<String, Object>> upsBoundList = manifestFileMapper.selectUpsBound(tmpMap);
			if(upsBoundList.size() > 0){
				strGroupName = Util.null2str(upsBoundList.get(0).get("GROUP_NAME"));
			}
//			else{
//				errorStr.append(hblNo + " : 국가 - " + countryCode + ",Port - " + portCode + " 해당 건이 누락 되었습니다.<br />");
//			}
			
			if(!"".equals(strGroupName)) {
				tmpMap.clear();
				tmpMap.put("srchBound", "AIR");
				tmpMap.put("srchBoundType", "EXP");
				tmpMap.put("srchGroupName", strGroupName);
				tmpMap.put("srchRunType", "Y");
				List<String> srchServiceCodeInList = new ArrayList<String>();
				srchServiceCodeInList.add(Util.null2str(data.getServiceCode()));
				if(!srchServiceCodeInList.get(0).equals("")){
					srchServiceCodeInList.add("");
				}
				tmpMap.put("srchEntCode", entCode);
				tmpMap.put("srchServiceCodeInList", srchServiceCodeInList);
				tmpMap.put("sortStr", "ORDER BY ups.SERVICE_CODE DESC, ups.SEQ_NO_ID DESC");
				tmpMap.put("top1Yn", "Y");
				List<Map<String, Object>> upsGroupList = manifestFileMapper.selectUpsGroup(tmpMap);
				if(upsGroupList.size() > 0){
					String obDate = Util.null2str(upsGroupList.get(0).get("OB_DATE"));
					data.setExpMblNo(Util.null2str(upsGroupList.get(0).get("MBL_NO")).trim());
				}
			}
			
		}
	}
	
	/**
	 * UPS 항공수출 T/S HAWB 저장
	 * @param entCode
	 * @param search
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult saveTsHouse(String entCode, SearchManifestFileList search, HttpServletRequest request) throws Exception {
		OrderSaveResult result = new OrderSaveResult();
		/** 취약성점검 SQL Injection 방지 */
		Util.accessControlDatabase(search, "gmtTime", false, null, null, null, null);
		
		int maxHsn = 1;
		StringBuffer errorStr = new StringBuffer();
		Map<String, String> createHblData = new HashMap<String, String>();
		Map<String, Object> ediMap;
		Map<String, String> checkMap = new HashMap<String, String>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		List<String> tmpList = new ArrayList<String>();
		int stockSeqNo = 1;
		Map<String, Object> stockMap = new HashMap<String, Object>();
		stockMap.put("entCode", entCode);
		stockMap.put("gmtTime", search.getGmtTime());
		stockMap.put("entBusinessCode", "");
		stockMap.put("updateUserId", search.getUserId());
		stockMap.put("solution", "ECM");
		stockMap.put("stockType", "OD");
		stockMap.put("stockSign", "COD");
		stockMap.put("stockSeqLength", 8);
		stockMap.put("dateLength", null);
		stockMap.put("signType", null);
		stockMap.put("stockDateFormat", null);
		stockMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
		stockMap.put("stockRemark", "ORDER-JOB NO");
		
		
		Map<String, SearchManifestFileList> mblCheckMap = new HashMap<String, SearchManifestFileList>();
		SearchManifestFileList mbl;
		Map<String, Object> mblParamMap = new HashMap<String, Object>();
		mblParamMap.put("bound", search.getBound());
		mblParamMap.put("boundType", search.getBoundType());
		mblParamMap.put("selectMaxHsn", "Y");
		
		List<ManifestFile> list = search.getManifestFileList();
		List<ManifestFile> insertList = new ArrayList<ManifestFile>();
		List<ManifestFile> updateList = new ArrayList<ManifestFile>();
		
		ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
		BulkData bulkData = null;
		String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
		String description = "insert".equals(search.getAction()) ? "NEW ORDER" : "UPDATE ORDER";
		Map<String, Object> tmpLogMap = new HashMap<String, Object>();
		Map<String, Object> logMap;
		tmpLogMap.put("job_func_type", "ADD");
		tmpLogMap.put("ent_code", entCode);
		tmpLogMap.put("ent_business_code", search.getEntBusinessCode());
		tmpLogMap.put("solution", "ECM");
		tmpLogMap.put("bound", search.getBound());
		tmpLogMap.put("bound_type", search.getBoundType());
		tmpLogMap.put("process_user_id", search.getUserId());
		tmpLogMap.put("log_job_type", "DATA");
		tmpLogMap.put("result", "ok");
		tmpLogMap.put("ent_business_print_name", search.getLogUserBusinessPrintName());
		tmpLogMap.put("process_user_name", search.getLogUserName());
		tmpLogMap.put("process_user_name_eng", search.getLogUserNameEng());
		tmpLogMap.put("process_date_time", System.currentTimeMillis());
		if (request != null)	tmpLogMap.put("access_ip", Util.getClientIP(request));
		
		Map<String, Map<String, Object>> exchangeRateMap = new HashMap<String, Map<String, Object>>();
		
		int seq = 1;
		
		tmpMap = new HashMap<String, Object>();
		tmpMap.put("entCode", entCode);
		tmpMap.put("entBusinessCode", search.getEntBusinessCode());
		tmpMap.put("solution", "ECM");
		tmpMap.put("optionCode", "C02");
		Map<String, Object> optionMap = commonMapper.selectSystemOption2(tmpMap);
		
		for(ManifestFile data : list){
			if("".equals(Util.null2str(data.getExpMblNo()))) {
				continue;
			}
			
			mbl = mblCheckMap.get(data.getExpMblNo());
			if(mbl == null){
				mblParamMap.put("mblNo", data.getExpMblNo());
				mbl = manifestFileMapper.selectMasterInfo2(mblParamMap);
				if(mbl == null){
					continue;
				}
				mbl.setSrchEntCode(search.getSrchEntCode());
				mbl.setEntBusinessCode(search.getEntBusinessCode());
				mbl.setBound(search.getBound());
				mbl.setBoundType(search.getBoundType());
				mbl.setGmtTime(search.getGmtTime());
				mbl.setConsolYn(search.getConsolYn());
				mbl.setDateFormat(search.getDateFormat());
				mbl.setUserId(search.getUserId());
				
				tmpMap = new HashMap<String, Object>();
				tmpMap.put("srchEntCode", entCode);
				tmpMap.put("srchEntBusinessCode", search.getEntBusinessCode());
				tmpMap.put("srchBound", search.getBound());
				tmpMap.put("srchBoundType", search.getBoundType());
				tmpMap.put("srchEdiCityPort", mbl.getOrigCode());
				tmpMap = commonMapper.selectBusEdiEcmInfo(tmpMap);
				if(tmpMap != null && tmpMap.size() > 0){
					mbl.setEdiMailboxSeqNo(Util.null2str(tmpMap.get("SEQ_NO")));
				}
				
				/** 항공일경우 중계사업자 설정값이 auto이면 편명의 중계사업자로 저장 */
				if("auto".equals(optionMap.get("SET_VALUE"))){
					String venderId = "";
					if(!Util.isEmpty(mbl.getLineCode())){
						tmpMap.put("groupCode", "AIRLINE_CODE");
						tmpMap.put("srchEtcFieldOnly", "Y");
						tmpMap.put("srchEtcField", "ETC2");
						tmpMap.put("srchEtcFieldValue", mbl.getLineCode());
						tmpMap = commonMapper.getCodeDetail(tmpMap);
						if(tmpMap != null){
							venderId = Util.null2str(tmpMap.get("ETC11"));
						}else{
							tmpMap = new HashMap<String, Object>();
						}
					}
					if(venderId.equals("")){
						mbl.setVenderId(Util.null2str(optionMap.get("DEFAULT_VALUE")));
					}else{
						mbl.setVenderId(venderId);
					}
				}else if(Util.isEmpty(optionMap.get("SET_VALUE"))){
					mbl.setVenderId(Util.null2str(optionMap.get("DEFAULT_VALUE")));
				}else{
					mbl.setVenderId(Util.null2str(optionMap.get("SET_VALUE")));
				}
				
				mblCheckMap.put(data.getExpMblNo(), mbl);
			}
			
			// 마스터가 변경되지 않은 건은 미처리 
			if("update".equals(search.getAction()) && Util.null2str(mbl.getOdJobNo()).equals(Util.null2str(data.getExpMasterOdJobNo()))) {
				continue;
			}
			
			if(Util.isEmpty(mbl.getObTime())){
				errorStr.append(data.getMblNo() + " - " + data.getHblNo() + " : 출항시간을 입력바랍니다.<br />");
				continue;
			}
			
			if(!"Y".equals(mbl.getFinishYn())){
				errorStr.append(data.getMblNo() + " - " + data.getHblNo() + " : 마감되지않은 마스터번호 입니다.<br />");
				continue;
			}
			
			maxHsn = Util.null2int(mbl.getHsn()) + 1;
			if(maxHsn > 9999){
				errorStr.append(data.getHblNo() + " : HSN 범위를 초과 하여 생성 하지 않았습니다.<br />");
				continue;
			}
			
			tmpMap = new HashMap<String, Object>();
			tmpMap.put("masterOdJobNo", mbl.getOdJobNo());
			tmpMap.put("hblNo", data.getHblNo());
			//ICNHUB 계정으로 업로드하는 경우에는 반입계의 테이블을 한번더 검색하여 반입계(반입계의 목록통관(F)건은 예외)에 하우스빌번호를 체크한다.(16.05.12)
			if((!"insert".equals(mbl.getAction()) && manifestFileMapper.selectHouseInfoList(tmpMap).size() > 0) || ("ICNHUB".equals(search.getUserName()) && manifestFileMapper.selectEpnList(tmpMap).size() > 0)){
				errorStr.append(data.getHblNo() + " : 동일한 H.B/L이 존재합니다.<br />");
				continue;
			}
			
			mbl.setHsn(Util.null2str(maxHsn));
			
			if("update".equals(search.getAction())) {
				// 개발원 전송이력 삭제
				tmpList = new ArrayList<String>();
				tmpList.add(data.getExpMasterOdJobNo());
				SendManifest sendManifest = new SendManifest();
				sendManifest.setModJobNoList(tmpList);
				tmpList.clear();
				tmpList.add(data.getExpOdJobNo());
				sendManifest.setHouseOdJobNoList(tmpList);
				ediManifestGwSendService.resetSendManifestGw(sendManifest);
				
				Map<String,Object> podMap = new HashMap<String, Object>();
				podMap = consolMapper.selectLastConsolPodInfo(data.getExpOdJobNo());
				if(podMap != null) {
					consolMapper.updateCloseOrderDeconsol(podMap);
					// ECM055(콘솔마감) 보다 큰 POD 삭제한다. 2024-10-29
					consolMapper.deletePodInfo(data.getExpOdJobNo());
				}
				
				bulkData = new BulkData();
				bulkData.setActionType(BulkData.Type.CREATE);
				bulkData.setIndexName(index);
				bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
				logMap = new HashMap<String, Object>();
				logMap.putAll(tmpLogMap);
				logMap.put("log_job_type", "DECONSOL");
				logMap.put("job_func_type", "UPDATE");
				logMap.put("reference_no_id", bulkData.getId());
				logMap.put("job_no", data.getExpOdJobNo());
				logMap.put("description", "closeMasterDeconsol [OD_JOB_NO : " + data.getExpOdJobNo() +"]");
				logMap.put("job_log","");
				bulkData.setJsonMap(logMap);
				bulkList.add(bulkData);
			}
			
			tmpMap = new HashMap<String, Object>();
			tmpMap.put("srchEntCode", entCode);
			tmpMap.put("srchEntBusinessCode", search.getEntBusinessCode());
			tmpMap.put("srchBound", search.getBound());
			tmpMap.put("srchEdiCityPort", mbl.getOrigCode());
			ediMap = manifestFileMapper.selectBusEdiCodeInfo(tmpMap);
			
			if(data.getCdType().equals("Y") && ediMap != null){
				data.setEntBusinessCode("");
				data.setUpdateUserId(search.getUserId());
				data.setStockType("OD");
				data.setStockSeqLength("8");
				data.setDateLength("4");
				data.setStockDateFormat("112");
				data.setSignType("noHyphen");
				data.setResultCode("FIXED_SIGN_AND_SEQ_NO");
				data.setStockRemark("ORDER SIMPLE DOC NO");
				data.setSeqNo(stockSeqNo++);
				
				if("Y".equals(data.getDocumentYn())){
					data.setStockSign(Util.null2str(ediMap.get("EDI_EXPRESS_SIGN_DOCUMENT_Y")));
				}else{
					data.setStockSign(Util.null2str(ediMap.get("EDI_EXPRESS_SIGN_DOCUMENT_N")));
				}
				data.setSimpleDocNo("create");
			}
			
			data.setEntCode(entCode);
			data.setHeadEntBusinessCode(search.getEntBusinessCode());
			data.setSolution("ECM");
			data.setGmtTime(search.getGmtTime());
			data.setDateFormat(search.getDateFormat());
			data.setCustCodeLv1(mbl.getCustCodeLv1());
			data.setBlTypeMh("H");
			data.setCustomsIdType("01");
			data.setEcmType("01");
			data.setInspectionYn("N");
			data.setMultiYn("N");
			data.setHoldYn("N");
			data.setFtaYn("Y");
			data.setFinishYn("Y");
			data.setUserId(search.getUserId());
			data.setMasterOdJobNo(mbl.getOdJobNo());
			data.setMblNo(mbl.getMblNo());
			data.setLicenseNo(Util.null2str(data.getMrn()) + Util.null2str(data.getMsn()) + Util.null2str(data.getHsn()));
			data.setMrn(mbl.getMrn());
			data.setMsn(mbl.getMsn());
			data.setHsn(Util.lpad(Util.null2str(maxHsn), 4, '0'));
			data.setLineCode(mbl.getLineCode());
			data.setVesselFlight(mbl.getVesselFlight());
			data.setBlTypeIert("R");
			data.setDestCode(mbl.getDestCode());
			data.setOrigCode(mbl.getOrigCode());
			data.setArDate(mbl.getArDate());
			data.setArTime(mbl.getArTime());
			data.setObDate(mbl.getObDate());
			data.setObTime(mbl.getObTime());
			data.setBound(search.getBound());
			data.setBoundType(search.getBoundType());
			data.setConsolYn("Y");
			data.setOrigName(mbl.getOrigName());
			data.setOrigCountry(mbl.getOrigCountry());
			data.setDestName(mbl.getDestName());
			data.setDestPort(mbl.getDestPort());
			data.setDestCountry(mbl.getDestCountry());
			data.setShipperCountryCode(mbl.getShipperCountryCode());
			data.setConsigneeCountryCode(mbl.getConsigneeCountryCode());
			data.setCustomsCode(mbl.getCustomsCode());
			data.setCustomsDptCode(mbl.getCustomsDptCode());
			data.setEdiMailboxSeqNo(mbl.getEdiMailboxSeqNo());
			data.setVenderId(mbl.getVenderId());
			data.setLastPodStatus("ECM055");	/** 콘솔마감 */
			data.setPodJobNo(DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
			stockMap.put("seqNo", stockSeqNo++);
			if("insert".equals(search.getAction())) {
				data.setOdJobNo(commonMapper.selectNextStockMaxNo(stockMap));
				data.setPodJobNo2(DateTimeUtil.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
			}else {
				data.setOdJobNo(data.getExpOdJobNo());
			}
			
			Map<String, Object> exchangeRate = exchangeRateMap.get(data.getObDate());
			if(exchangeRate == null){
				tmpMap.put("srchEntCode", search.getSrchEntCode());
				tmpMap.put("entBusinessCode", search.getEntBusinessCode());
				tmpMap.put("srchDateFormat", search.getSrchDateFormat());
				tmpMap.put("exrateType", "EXRATE_SELLING_CUSTOMS");
				tmpMap.put("srchDate", data.getObDate());
				exchangeRate = commonMapper.selectExchangeRate(tmpMap);
				if(Util.null2int(exchangeRate.get("CNT")) == 0){
					exchangeRate = new HashMap<String, Object>();
				}
				exchangeRateMap.put(data.getObDate(), exchangeRate);
			}
			
			data.setExchangeRate(Util.null2dbl(exchangeRate.get("EXCHANGE_RATE_" + data.getCurrencyCode())));
			if("KRW".equals(data.getCurrencyCode())){
				data.setAmountLoc(data.getAmountCur());
			}else{
				data.setAmountLoc(data.getAmountCur() * data.getExchangeRate());
			}
			if("USD".equals(data.getCurrencyCode())){
				data.setAmountUsd(data.getAmountCur());
			}else{
				data.setAmountUsd(data.getAmountLoc() * Util.null2dbl(exchangeRate.get("EXCHANGE_RATE_USD")));
			}
			
			bulkData = new BulkData();
			bulkData.setActionType(BulkData.Type.CREATE);
			bulkData.setIndexName(index);
			bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq));
			logMap = new HashMap<String, Object>();
			logMap.putAll(tmpLogMap);
			logMap.put("job_no", data.getOdJobNo());
			logMap.put("description", description +"[HBL_NO : " + data.getHblNo()+"]");
			logMap.put("job_log", new Gson().toJson(data));
			logMap.put("reference_no_id", bulkData.getId());
			bulkData.setJsonMap(logMap);
			bulkList.add(bulkData);
			
			createHblData.put(data.getHblNo(), "Y");
			if("insert".equals(search.getAction())) {
				insertList.add(data);
			}else {
				updateList.add(data);
			}
		}
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			ManifestFileMapper manifestFileBatchMapper = sqlSession.getMapper(ManifestFileMapper.class);
			int cnt = 1;
			for(ManifestFile data : insertList){
				manifestFileBatchMapper.insertTsHouseOrder(data);
				if((cnt++ % 1000) == 0){
					sqlSession.flushStatements();
					sqlSession.clearCache();
				}
			}
			
			cnt = 1;
			for(ManifestFile data : updateList){
				manifestFileBatchMapper.updateTsHouseOrder(data);
				if((cnt++ % 1000) == 0){
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
			sqlSession.close();
		}
		commonService.insertLogBulk(bulkList);
		
		int createCount = "insert".equals(search.getAction()) ? insertList.size() : updateList.size();
		resultMap.put("createCount", createCount);
		resultMap.put("createHblData", createHblData);
		if(errorStr.length() > 0){
			resultMap.put("errorStr", errorStr.toString());
//			resultMap.put("errorList", errorList);
		}
		result.setResultData(resultMap);
		return result;
	}
}

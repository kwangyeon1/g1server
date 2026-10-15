package org.yht.exps.express.service.eCommerce;



import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.yht.elasticsearch.client.data.BulkData;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.eCommerce.EpnMapper;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.eCommerce.epn.Epn;
import org.yht.exps.express.model.eCommerce.epn.EpnList;
import org.yht.exps.express.model.eCommerce.epn.search.SearchEpnBatchUpdate;
import org.yht.exps.express.model.eCommerce.epn.search.SearchEpnList;
import org.yht.exps.express.model.eCommerce.order.ExcelUploadList;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.service.CommonService;
import org.yht.exps.express.service.FileService;
import org.yht.exps.express.service.edi.EdiCommonService;
import org.yht.util.DateTime;
import org.yht.util.Util;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@Transactional
public class EpnService {

	@Resource(name = "fileService")
	FileService fileService;
	@Resource(name="ediCommonService")
	EdiCommonService ediCommonService;
    @Resource(name = "commonService")
    CommonService commonService;
	
	@Autowired
	EpnMapper epnMapper;
	@Autowired
	CommonMapper commonMapper;
	@Autowired
	CustomProperties customProperties;
	@Autowired
	SqlSessionTemplate sqlSessionTemplate;

	/**
	 * EPN 조회
	 * @param search
	 * @return
	 * @throws Exception
	 */
	public EpnList selectEpnList(SearchEpnList search) throws Exception {
		EpnList epnList = new EpnList();
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
		
		Util.accessControlDatabase(search, "srchDateFormat", true, null, null, null, null);
		List<Epn> list  = epnMapper.selectEpnList(search);
		epnList.setEpnList(list);
		if(list.size() > 0) {
			epnList.setTotal(list.get(0).getTotal());
		}else {
			epnList.setTotal(0);
		}
		return epnList;
	}
	
	/**
	 * 전자상거래 EPN 저장
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult saveEpn(Epn epn) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		String action = epn.getAction();
		if("insert".equals(action)){
			Map<String, Object> stockMap = new HashMap<String, Object>();
			stockMap.put("gmtTime", epn.getGmtTime());
			stockMap.put("entCode", epn.getEntCode());
			stockMap.put("entBusinessCode", "");
			stockMap.put("updateUserId", epn.getUserId());
			stockMap.put("solution", "ECM");
			stockMap.put("stockType", "EPN");
			stockMap.put("stockSign", "EPN");
			stockMap.put("stockSeqLength", 8);
			stockMap.put("dateLength", null);
			stockMap.put("signType", null);
			stockMap.put("stockDateFormat", null);
			stockMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
			stockMap.put("stockRemark", "EPN JOB NO");
			epn.setEpnJobNo(commonMapper.selectNextStockMaxNo(stockMap));
			Util.accessControlDatabase(epn, "gmtTime", false, null, null, null, null);
			epnMapper.insertEpn(epn);
		}else if("update".equals(action)){
			epnMapper.updateEpn(epn);
		}
		
		orderSaveResult.setResultCode(epn.getEpnJobNo());
		return orderSaveResult;
	}
	public OrderSaveResult insertEpnBatch(EpnList epnList) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		
		SqlSession sqlSession = null;
		try {
			sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH, false);
			EpnMapper epnBatchMapper = sqlSession.getMapper(EpnMapper.class);
			
			int cnt = 1;
			for(Epn epn : epnList.getEpnList()){
				epn.setEntBusinessCode("");
				epn.setSolution("ECM");
				epn.setStockType("EPN");
				epn.setStockSign("EPN");
				epn.setStockSeqLength("8");
				epn.setResultCode("FIXED_SIGN_AND_SEQ_NO");
				epn.setStockRemark("EPN JOB NO");
				Util.accessControlDatabase(epn, "dateFormat|gmtTime", false, null, null, null, null);
				epnBatchMapper.insertEpnBatch(epn);
				if((cnt++ % 1000) == 0){
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
		
		return orderSaveResult;
	}
	
	/**
	 * 전자상거래 EPN 삭제
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult deleteEpnList(Epn epn) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		epnMapper.deleteEpn(epn);
		return orderSaveResult;
	}
	
	/**
	 * 전자상거래 EPN 조회
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public Epn selectEpn(Epn epn) throws Exception {
		return epnMapper.selectEpn(epn);
	}
	
	/**
	 * 전자상거래 EPN 엑셀 파일 READ
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult readExcelEpn(MultipartFile uploadExcel, String entCode, String type) throws Exception{
		Map<String, Object> resultData = new HashMap<String, Object>();
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		FileUploadResponse result = fileService.storeFile(uploadExcel, entCode, true);
		if(Util.null2str(result.getResult()).equals("ok")){
			File file = fileService.loadFileAsResource(result.getFileName(), entCode, true).getFile();
			String extension = null;
			if(file.getName().toLowerCase().indexOf(".xlsx") > -1){
				extension = "xlsx";
			}else if(file.getName().toLowerCase().indexOf(".xlsm") > -1) {
				extension = "xlsm";
			}else{
				extension = "xls";
			}
			
			String[] fieldName = null;
			String[] fieldTitle = null;
			String[] fieldLength = null;
			String[] fieldType = null;
			
			/** 고객사 EPN (EXCEL), T/S EPN (EXCEL), 내륙지통관 EPN (EXCEL) - 동일한 Excel 스펙 사용 */
			if(type.equals("CS") || type.equals("TS") || type.equals("IN")) {
				fieldName =		new String[]{"sendDateTime", "destCountry",	"hblNo",	"licenseNo",	"pieces",	"gwt",		"packingCode",	"packingPieces",	"splitYn",	"splitSeq",	"shipperName"};
				fieldTitle =	new String[]{"신고일자",		 "국가코드", 		"HAWB No", 	"수출신고번호",		"수량",		"중량",		"동시포장코드",		"동시포장갯수",			"분할여부",	"분할차수",	"수출자명"};
				fieldLength =	new String[]{"10",			 "3", 			"20", 		"20", 			"4",		"12.3", 	"2", 			"4",		 		"1", 		"4",		"100"};
				fieldType =		new String[]{"DATE",		 "S",			"S",		"S",			"N",		"D",		"S",			"N",				"S",		"N",		"SN"};
			}else {
			/** 목록 EPN (EXCEL) */
				fieldName =		new String[]{"sendDateTime", 	"hblNo",	"shipperName",	"shipperBusinessId",	"hsCode",			"clearanceType",		"registerUserId",	"entBusinessCode"};
				fieldTitle =	new String[]{"신고일자",			"HAWB No", 	"수출자명", 		"사업자번호",				"세번부호(HSCode)",	"통관구분(A,B,C,D,E,F)",	"입력자코드",			"본지사코드"};
				fieldLength =	new String[]{"10",				"20", 		"100", 			"32", 					"10",				"10", 					"10", 				"8"};
				fieldType =		new String[]{"DATE",			"S",		"SN",			"SN",					"S",				"S",					"S",				"X"};
			}
			
			
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
				
				// 데이트 포맷
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				
				if(extension.equals("xlsx") || extension.equals("xlsm")){
					XSSFCell myCell = null;
					for (int i = 1; i <= sheet.getLastRowNum(); i++) {
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
							if("DATE".equals(fieldType[j])) {
								String value = null;
								try {
									if(myCell!=null && myCell.getDateCellValue() !=null) {
										value = sdf.format(myCell.getDateCellValue());
									}
								}catch(Exception e) {
									value = df.formatCellValue(myCell);
								}
								fieldValue = this.dataChk(value, fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i);
							}
							else {
								fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i);
							}
							
							//필수값 체크
							if(j == 0 || j == 2) {
								if(StringUtils.isEmpty(fieldValue) || Util.null2str(fieldValue).trim().equals("")) {
									errMsg.append("[" + fieldTitle[j] + "] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							dataMap.put(fieldName[j], fieldValue);
						}
						
						dataList.add(dataMap);
					}
				}else{
					HSSFCell myCell = null;
					for (int i = 1; i <= sheet.getLastRowNum(); i++) {
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
							if("DATE".equals(fieldType[j])) {
								String value = null;
								try {
									if(myCell!=null && myCell.getDateCellValue() !=null) {
										value = sdf.format(myCell.getDateCellValue());
									}
								}catch(Exception e) {
									value = df.formatCellValue(myCell);
								}
								fieldValue = this.dataChk(value, fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i);
							}
							else {
								fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j],fieldLength[j], fieldType[j], errMsg, i);
							}
							
							//필수값 체크
							if(j == 0 || j == 2) {
								if(StringUtils.isEmpty(fieldValue) || Util.null2str(fieldValue).trim().equals("")) {
									errMsg.append("[" + fieldTitle[j] + "] 필수값 비어있습니다.(행 : " + (i+1) + ") <br />");
								}
							}
							
							dataMap.put(fieldName[j], fieldValue);
						}
						
						
						dataList.add(dataMap);				
						
					}
				}
				
				if(dataList.size() == 0){
					orderSaveResult.setResultCode("A00059");	/** 데이터를 찾을 수 없습니다. */
					return orderSaveResult;
				}
				
				if(errMsg.length() != 0){
					orderSaveResult.setResultCode("dataErr");
					Map<String, Object> errMap = new HashMap<String, Object>();
					errMap.put("errStr", errMsg);
					orderSaveResult.setResultData(errMap);
					return orderSaveResult;
				}
				//TODO 파일삭제?
				
				int nNo = 0;
				for(Map<String, Object> data : dataList) {
					
	                String strErrorMessage = "";
	                String strHBLNo = "";
	                String strLicenseNo = "";
					
					/** 고객사 EPN (EXCEL), T/S EPN (EXCEL), 내륙지통관 EPN (EXCEL) */
					if(type.equals("CS") || type.equals("TS") || type.equals("IN")) {
						//변수 초기화
                        strErrorMessage = "";
                        nNo++;
                        strHBLNo = "";
                        strLicenseNo = "";
                        
                        if(!Util.null2str(data.get("sendDateTime")).trim().equals("")) {
                        	data.put("sendDateTime", Util.null2str(data.get("sendDateTime")).trim().substring(0, 10));
                        }else {
                        	data.put("sendDateTime", Util.getNow("yyyy-MM-dd"));
                        }
                        
                        //국가코드가 3Code인 경우는 City를 나타냄. 따라서 그에 해당하는 국가코드를 가져온다(3Code->2Code)
                        if(Util.null2str(data.get("destCountry")).trim().length() >= 3) {
                        	data.put("destCountry", getCountryCode(Util.null2str(data.get("destCountry"))));
                        }else {
                        	data.put("destCountry", Util.null2str(data.get("destCountry")).trim());
                        }
                        
                        strHBLNo = Util.null2str(data.get("hblNo")).trim();
                        
                        //하우스 비엘번호 자릿수가 18인 경우 11자리로 변환한다.
                        if(strHBLNo.length() == 18) {
                        	data.put("hblNo", getLenChangeHBLNo(strHBLNo));
                        }
                        ///////////////////////////////////////////////////
                    	strLicenseNo = Util.null2str(data.get("licenseNo")).replace("'", "").trim();
                    	data.put("licenseNo", strLicenseNo);
                    	data.put("pieces", Util.null2int(Util.null2str(data.get("pieces")).trim()));
						
                        // error
                    	data.put("gwt", Util.null2dbl(setDataVal(Util.null2str(data.get("gwt")).trim())));
                    	data.put("packingCode", Util.null2str(data.get("packingCode")).trim());
                    	data.put("packingPieces", Util.null2int(Util.null2str(data.get("packingPieces")).trim()));
                    	
                    	if(!Util.null2str(data.get("splitYn")).trim().equals("")) {
                    		data.put("splitYn", Util.null2str(data.get("splitYn")).trim());
                    	}else {
                    		data.put("splitYn", "N");
                    	}
                    	data.put("splitSeq", Util.null2int(Util.null2str(data.get("splitSeq")).trim()));
                    	data.put("shipperName", Util.null2str(data.get("shipperName")).trim());
                        
                        if (strLicenseNo.toUpperCase() != "NCV" && strLicenseNo.length() < 14){
                            strErrorMessage += "면장번호가 등록되지 않았습니다.,";
                        }
                        
                        if (strLicenseNo.length() > 19){
                            strErrorMessage += "면장번호 자릿수를 초과하였습니다.(19자).,";
                        }
                        
                        if (Util.null2str(data.get("hblNo")).length() > 11){
                            strErrorMessage += "H/B.L No. 자릿수를 초과하였습니다.(11자).,";
                        }
                        
                        data.put("num", nNo);
                        data.put("errorMessage", strErrorMessage);
                        
                    /** 목록 EPN (EXCEL) */
					}else if(type.equals("LI")) {
						// 변수 초기화                      
                        strErrorMessage = "";
                        nNo++;
                        strHBLNo = "";
                        
                        if(!Util.null2str(data.get("sendDateTime")).trim().equals("")) {
                        	data.put("sendDateTime", Util.null2str(data.get("sendDateTime")).trim().substring(0, 10));
                        }else {
                        	data.put("sendDateTime", Util.getNow("yyyy-MM-dd"));
                        }
                        
                        strHBLNo = Util.null2str(data.get("hblNo")).trim();
                    	data.put("hblNo", 			  getLenChangeHBLNo(strHBLNo));
                    	data.put("shipperName", 	  Util.null2str(data.get("shipperName")).trim());
                    	data.put("shipperBusinessId", Util.null2str(data.get("shipperBusinessId")).replace("'", "").trim().replace("-", ""));
//                    	data.put("clearanceType", ediCommonService.getSubStringK(Util.null2str(data.get("clearanceType")), 0, 1));
                    	data.put("clearanceType", Util.null2str(data.get("clearanceType")));

                    	data.put("registerUserId", Util.null2str(data.get("registerUserId")).trim());
                    	data.put("entBusinessCode", Util.null2str(data.get("entBusinessCode")).trim());
                    	data.put("num", nNo);
					}
				}
				
				resultData.put("entCode", entCode);
				resultData.put("dataList", dataList);
				resultData.put("dataSize", dataList.size());
				
				orderSaveResult.setResultData(resultData);
				orderSaveResult.setResultCode("200");
				
				String backupPath = customProperties.getBackupDir() + "excel/" + entCode + "/" + DateTime.getDate("yyyy-MM");
				File dir = new File(backupPath);
				if (dir.exists() == false)	dir.mkdirs();
				String backupFilePath = backupPath + "/" + file.getName();
				file.renameTo(new File(backupFilePath));
				
			}catch(Exception e) {
				orderSaveResult.setResultCode("A00063");
				log.error("exception Message :: " + e.getMessage());
			}finally{
				if (is != null) is.close();
			}
		}else{
			orderSaveResult.setResultCode("ERROR");
		}
		return orderSaveResult;
	}
	
	/**
	 * 전자상거래 EPN 엑셀 업로드
	 * @param excelUploadList
	 * @param req
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult insertExcelEpn(ExcelUploadList excelUploadList, String type, HttpServletRequest req) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();
		Map<String, Object> resultData = new HashMap<String, Object>();
		boolean returnValue = false;
		
		try {
			int cnt = 0;
			List<Map<String, Object>> dataList = excelUploadList.getDataList();
			
			for(Map<String, Object> data : dataList) {
				/** EPN 저장 전 데이터 유무 체크 */
				cnt++;
				Map<String, Object> tmpMap = new HashMap<String, Object>();
				tmpMap.put("hblNo", data.get("hblNo"));
				tmpMap.put("licenseNo", data.get("licenseNo"));
				tmpMap.put("clearanceType", type);
				List<Map<String, Object>> chkList = epnMapper.selectEpnChk(tmpMap);
				
				data.put("type", type);
				data.put("updateUserId", excelUploadList.getUpdateUserId());
				data.put("gmtTime", excelUploadList.getGmtTime());
				data.put("dateFormat", excelUploadList.getDateFormat());
				
				switch(type) {
				case "CS": 
					data.put("clearanceType", "D"); 
					break;
				case "TS": 
					data.put("clearanceType", "E"); 
					break;
				case "IN":
					data.put("clearanceType", "A"); 
					break;
				}
				
				data.put("clearanceType", ediCommonService.getSubStringK(Util.null2str(data.get("clearanceType")), 0, 1));
				
				/** UPDATE */
				if(chkList.size() > 0) {
					epnMapper.updateEpnExcelUpload(data);
				/** INSERT */
				}else {
					Map<String, Object> stockMap = new HashMap<String, Object>();
					stockMap.put("gmtTime", excelUploadList.getGmtTime());
					stockMap.put("entCode", excelUploadList.getEntCode());
					stockMap.put("entBusinessCode", "");
					stockMap.put("updateUserId", excelUploadList.getUpdateUserId());
					stockMap.put("solution", "ECM");
					stockMap.put("stockType", "EPN");
					stockMap.put("stockSign", "EPN");
					stockMap.put("stockSeqLength", 8);
					stockMap.put("dateLength", null);
					stockMap.put("signType", null);
					stockMap.put("stockDateFormat", null);
					stockMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
					stockMap.put("stockRemark", "EPN JOB NO");
					data.put("epnJobNo",commonMapper.selectNextStockMaxNo(stockMap));
					epnMapper.insertEpnExcelUpload(data);
				}
				
				resultData.put("cnt", cnt);
				orderSaveResult.setResultData(resultData);
				orderSaveResult.setResultCode("SUCCESS");
			}
			
		}catch(Exception e) {
			Util.getPrintStacTraceString(e);
			orderSaveResult.setResultCode("ERROR");
			return orderSaveResult;
		}
		
		return orderSaveResult;
	}
	
	/**
	 * 엑셀 타입 체크
	 * @param data
	 * @param name
	 * @param length
	 * @param type
	 * @param errMsg
	 * @param row
	 * @return
	 * @throws Exception
	 */
	public String dataChk(String data, String name, String length, String type, StringBuffer errMsg, int row) throws Exception {
		//TODO 엑셀 셀타입 텍스트로 고정시킬수있는지?  없다면 추가개발필요
		if(type.equals("N")){
			if (Util.null2str(data).equals("")){
				data = "0";
			} else {
				try {
	//				int idx = data.indexOf("E");	/** 8자리가 넘어가면 123456789.123 => 1.23456789123E8 로 표현됨 */
	//				if(idx > -1){
	//					int k = Util.null2int(data.substring(idx + 1)) + 1;
	//					data = data.substring(0, idx).replace(".", "");
	//					data = data.substring(0, k) + "." + data.substring(k);
	//				}
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
	//				if(!data.startsWith("2")){
	//					String[] tmpData = data.split("/");
	//					data = "20" + tmpData[2] + (tmpData[0].length() == 1 ? "0" + tmpData[0] : tmpData[0]) + tmpData[1];
	//				}else{
						SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
						SimpleDateFormat sdFormat2 = new SimpleDateFormat("yyyyMMdd");
						data = data.replaceAll("[-./]", "").substring(0, 8);
						data = sdFormat.format(sdFormat2.parse(data));
	//				}
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
	//		int idx = data.indexOf("E");	/** 8자리가 넘어가면 123456789.123 => 1.23456789123E8 로 표현됨 */
	//		if(idx > -1){
	//			int k = Util.null2int(data.substring(idx + 1)) + 1;
	//			data = data.substring(0, idx).replace(".", "");
	//			data = data.substring(0, k) + "." + data.substring(k);
	//		}
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
	
	private String getCountryCode(String portCode) {
		String countryCode = "";
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		tmpMap.put("portCode", portCode);
		List<String> countryCodeList = epnMapper.selectCountryCode(tmpMap);
		if(countryCodeList.size() > 0) {
			if(!Util.null2str(countryCodeList.get(0)).equals("")) {
				countryCode = Util.null2str(countryCodeList.get(0)).trim();
			}
		}
		return countryCode;
	}
	
	private String getLenChangeHBLNo(String strHBLNo) {
		String strReturnValue = "";
		String strFinalBL = "";
		long nBase = 0;
		long[] arrNResult = new long[5];
		int nStep = 4;
	
	    for (int i = 0; i < arrNResult.length; i++)
	    	arrNResult[i] = 0;
	    
	    try {
	    	strFinalBL = ediCommonService.getSubStringK(strHBLNo, 2, 8);
	    	nBase = Util.null2long(ediCommonService.getSubStringK(strHBLNo, 10, 7));
	    }catch(Exception e) {
	    	log.error(Util.getPrintStacTraceString(e));
	    }
	    
	    for (int i = 0; i < 5; i++) {
	    	switch (nStep) {
		    	case 4:
	                arrNResult[nStep] = Util.null2long((nBase - (arrNResult[4] * 456976) - (arrNResult[3] * 17576) - (arrNResult[2] * 676) - (arrNResult[1] * 26)) / 456976);
	                break;
	            case 3:
	                arrNResult[nStep] = Util.null2long((nBase - (arrNResult[4] * 456976) - (arrNResult[3] * 17576) - (arrNResult[2] * 676) - (arrNResult[1] * 26)) / 17576);
	                break;
	            case 2:
	                arrNResult[nStep] = Util.null2long((nBase - (arrNResult[4] * 456976) - (arrNResult[3] * 17576) - (arrNResult[2] * 676) - (arrNResult[1] * 26)) / 676);
	                break;
	            case 1:
	                arrNResult[nStep] = Util.null2long((nBase - (arrNResult[4] * 456976) - (arrNResult[3] * 17576) - (arrNResult[2] * 676) - (arrNResult[1] * 26)) / 26);
	                break;
	            case 0:
	                arrNResult[nStep] = Util.null2long((nBase - (arrNResult[4] * 456976) - (arrNResult[3] * 17576) - (arrNResult[2] * 676) - (arrNResult[1] * 26)) / 1);
	                break;
	    	}
	    	strFinalBL = strFinalBL + trackingChar(arrNResult[nStep]);
            nStep--;
	    }
	    strReturnValue = strFinalBL;
	    return strReturnValue;
	}
	
	private String trackingChar(long nResult) {
		String strReturnValue = "";
		if(nResult == 0 || nResult == 1) {
			strReturnValue = Util.null2str(nResult + 3);
		}else if(nResult == 2 || nResult == 3 || nResult == 4) {
			strReturnValue = Util.null2str(nResult + 5);
		}else if(nResult == 5) {
			strReturnValue = "B";
		}else if(nResult == 6) {
			strReturnValue = "C";
		}else if(nResult == 7) {
			strReturnValue = "D";
		}else if(nResult == 8) {
			strReturnValue = "F";
		}else if(nResult == 9) {
			strReturnValue = "G";
		}else if(nResult == 10) {
			strReturnValue = "H";
		}else if(nResult == 11) {
			strReturnValue = "J";
		}else if(nResult == 12) {
			strReturnValue = "K";
		}else if(nResult == 13) {
			strReturnValue = "L";
		}else if(nResult == 14) {
			strReturnValue = "M";
		}else if(nResult == 15) {
			strReturnValue = "N";
		}else if(nResult == 16) {
			strReturnValue = "P";
		}else if(nResult == 17) {
			strReturnValue = "Q";
		}else if(nResult == 18) {
			strReturnValue = "R";
		}else if(nResult == 19) {
			strReturnValue = "S";
		}else if(nResult == 20) {
			strReturnValue = "T";
		}else if(nResult == 21) {
			strReturnValue = "V";
		}else if(nResult == 22) {
			strReturnValue = "W";
		}else if(nResult == 23) {
			strReturnValue = "X";
		}else if(nResult == 24) {
			strReturnValue = "Y";
		}else if(nResult == 25) {
			strReturnValue = "Z";
		}
		return strReturnValue;
	}
	
	/**
	 * 데이터가 공백인 경우 "0" 값 리턴
	 * @param strData
	 * @return
	 */
    private String setDataVal(String strData)
    {
        String strReturnValue = "0";
        String strCheckData = deleteAlphabetChar_DotPass(strData);

        if (strCheckData != "")
            strReturnValue = strCheckData;

        return strReturnValue;
    }
    
    /**
	 * 숫자, 온점(.)을 제외한 모든문자 제거
	 * @param str
	 * @return
	 */
    public static String deleteAlphabetChar_DotPass(String strSource)
    {        
        String strResult = "";        
        strSource = Util.null2str(strSource);
        for(int i = 0; i < strSource.length(); i++) {
            int iASCIICode = strSource.charAt(i);
            char chr = strSource.charAt(i);
            
            //숫자, 온점(.)인 경우
            if ((iASCIICode >= 48 && iASCIICode <= 57) || iASCIICode == 46)
            {
                strResult += chr;
            }
        }        

        return strResult;
    }
	
    /**
     * 통관정보(EPN/반입계) List Count 조회
     * @param search
     * @return
     */
    public OrderSaveResult manifestLicenseTargetCount(SearchEpnBatchUpdate search) {
        OrderSaveResult orderSaveResult = new OrderSaveResult();

        SearchEpnList searchEpnList = search.getSearchEpnList();
        orderSaveResult.setResultCode(epnMapper.selectManifestLicenseTargetCount(searchEpnList));

        return orderSaveResult;
    }

    /**
     * 적하목록 통관정보 이관 Update
     * @param search
     * @return
     */
    public OrderSaveResult manifestLicenseUpdate(SearchEpnBatchUpdate search) throws Exception{
        OrderSaveResult orderSaveResult = new OrderSaveResult();
        String chk = Util.null2str(search.getChkMode());

        BulkData bulkData = null;
        ArrayList<BulkData> bulkList = new ArrayList<BulkData>();
        String index = "ecm-process-logs-" + DateTime.getDate("yyyyMMdd");
        Map<String, Object> logMap = null;
        Map<String, Object> tmpLogMap = new HashMap<String, Object>();
        int seq = 1;

        tmpLogMap.put("ent_code", search.getEntCode());
        tmpLogMap.put("ent_business_code", search.getUserBusinessCode());
        tmpLogMap.put("solution", "ECM");
        tmpLogMap.put("bound", "AIR");
        tmpLogMap.put("bound_type", "EXP");
        tmpLogMap.put("process_user_id", search.getUpdateUserId());
        tmpLogMap.put("log_job_type", "DATA");
        tmpLogMap.put("job_func_type", "UPDATE");
        tmpLogMap.put("result", "ok");
        tmpLogMap.put("process_date_time", System.currentTimeMillis());

        tmpLogMap.put("ent_business_print_name", search.getLogUserBusinessPrintName());
        tmpLogMap.put("process_user_name", search.getLogUserName());
        tmpLogMap.put("process_user_name_eng", search.getLogUserNameEng());

        //검색건 List 쿼리 조회
        if("B".equals(chk)){
            SearchEpnList searchEpnList = search.getSearchEpnList();
            List<Map<String, Object>> updateList = new ArrayList<Map<String, Object>>();
            updateList = epnMapper.selectManifestLicenseList(searchEpnList);
            search.setEpnList(updateList);
        }
        //조회건, 검색건 List for문으로 적하여부 체크
        for(int i = 0; i < search.getEpnList().size(); i++) {
            Map<String,Object> paramMap = search.getEpnList().get(i);
            //적하목록 이관 여부 체크
            if("Y".equals(Util.null2str(paramMap.get("mfTsYn")))){
                orderSaveResult.setResultCode("mfTsYnError");
                return orderSaveResult;
            }
            //신고번호 체크
            if("".equals(Util.null2str(paramMap.get("licenseNo"))) && !"F".equals(Util.null2str(paramMap.get("clearanceType")))){
                orderSaveResult.setResultCode("licenseNoError");
                return orderSaveResult;
            }
            //HBL_NO 체크
            if("".equals(Util.null2str(paramMap.get("hblNo")))){
                orderSaveResult.setResultCode("hblNoError");
                return orderSaveResult;
            }
            if("".equals(Util.null2str(paramMap.get("odJobNo")))) {
            	orderSaveResult.setResultCode("odJobNoError");
            	return orderSaveResult;
            }
            //오더생성일자 체크
            String odRegistDate = Util.null2str(paramMap.get("odRegisterDateTime"));
            LocalDate targetDate = LocalDate.parse(odRegistDate);
            LocalDate today = LocalDate.now();

            if(targetDate.isBefore(today)){
                orderSaveResult.setResultCode("registerDateError");
                return orderSaveResult;
            }
        }

        for (int y = 0; y < search.getEpnList().size(); y++) {
            Map<String, Object> paramMap = search.getEpnList().get(y);
            paramMap.put("updateUserId", search.getUpdateUserId());
            paramMap.put("gmtTime", search.getGmtTime());
            Map<String, Object> licenseMap = new HashMap<>();

            if (!"".equals(Util.null2str(paramMap.get("licenseNo")))) {
                // TB_ECM_OD_LICENSE SELECT
                licenseMap = epnMapper.selectOdLicenseInfo(paramMap);

                // UPDATE
                if (licenseMap != null) {
                    epnMapper.updateOdLicenseInfo(paramMap);

                    // INSERT
                } else {
                    epnMapper.insertOdLicenseInfo(paramMap);
                }
            }
            // TS건인 경우 환적으로 업데이트
            if("E".equals(Util.null2str(paramMap.get("clearanceType")))){
               epnMapper.updateOdIert(paramMap);
            }
            // 목록통관이면서 LicenseNo가 없는 경우
            if ("F".equals(Util.null2str(paramMap.get("clearanceType"))) && "".equals(Util.null2str(paramMap.get("licenseNo")))) {
                if(!"".equals(Util.null2str(paramMap.get("shipperBusinessId"))) || !"".equals(Util.null2str(paramMap.get("hsCode"))) || !"".equals(Util.null2str(paramMap.get("shipperName")))){
                    epnMapper.updateOdShipperInfo(paramMap);
                }
            }
            //EPN 이관여부 상태값 변경
            epnMapper.updateEpnMfTsYn(paramMap);

            bulkData = new BulkData();
            bulkData.setActionType(BulkData.Type.CREATE);
            bulkData.setIndexName(index);
            bulkData.setId(DateTime.getDate("yyyyMMddHHmmssSSS") + Util.getRandomNum(5) + Util.setSeqNo(seq++));
            logMap = new HashMap<String, Object>();
            logMap.putAll(tmpLogMap);
            logMap.put("reference_no_id", bulkData.getId());
            logMap.put("job_no", paramMap.get("odJobNo"));
            logMap.put("job_log",new Gson().toJson(paramMap));
            logMap.put("description", "적하목록 통관정보 이관 UPDATE ORDER [HBL_NO : " + Util.null2str(paramMap.get("hblNo")) +"]");
            bulkData.setJsonMap(logMap);
            bulkList.add(bulkData);
        }
        commonService.insertLogBulk(bulkList);	/** 처리 로그 기록 */

        return orderSaveResult;
    }
}
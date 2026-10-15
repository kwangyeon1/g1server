package org.yht.exps.express.service.sms.holy;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.yht.exps.express.mapper.CommonMapper;
import org.yht.exps.express.mapper.sms.holy.HolydayMapper;
import org.yht.exps.express.model.FileUploadResponse;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.exps.express.model.sms.holy.Holyday;
import org.yht.exps.express.model.sms.holy.HolydayList;
import org.yht.exps.express.model.sms.holy.HolydayResult;
import org.yht.exps.express.model.sms.holy.search.SearchHolydayList;
import org.yht.exps.express.service.FileService;
import org.yht.util.DateTime;
import org.yht.util.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class HolydayService {
	@Autowired
	HolydayMapper holydayMapper;
	
	@Autowired
	CommonMapper commonMapper;
	
	@Resource(name = "fileService")
	FileService fileService;
	
	@Autowired
	CustomProperties customProperties;
	
	/**
	 * HolydayList
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public HolydayList selectHolydayList(SearchHolydayList search) throws Exception {
		HolydayList holydayList = new HolydayList();
		List<Holyday> list = holydayMapper.selectHolyDayList(search);
		
		holydayList.setDataList(list);
		if(list.size() > 0) {
			holydayList.setTotal(list.get(0).getTotal());
		}else {
			holydayList.setTotal(0);
		}
		return holydayList;
	}
	
	/**
	 * 공휴일관리 - insert
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public HolydayResult saveHolyday(HttpServletRequest request, HolydayList holydayList) throws Exception {
		Util.accessControlDatabase(holydayList, "dateFormat|gmtTime", false, null, null, null, null);
		
		HolydayResult holydayResult = new HolydayResult();
		String action = holydayList.getAction();
		
		Map<String, Object> tmpMap = new HashMap<String, Object>();
		
		if(holydayList.getDataList() != null || holydayList.getExcelDataList() != null) {
			if("insert".equals(action)) {
				List<Holyday> tempList = new ArrayList<Holyday>();
				if(holydayList.getDataList() != null) tempList.addAll(holydayList.getDataList());
				if(holydayList.getExcelDataList() != null) tempList.addAll(holydayList.getExcelDataList());
				
				for(Holyday holyday : tempList) {
					holyday.setEntCode(Util.null2str(holydayList.getEntCode()));
					holyday.setDateFormat(Util.null2str(holydayList.getDateFormat()));
					holyday.setRegisterUserId(Util.null2str(holydayList.getRegisterUserId()));
					holyday.setGmtTime(Util.null2str(holydayList.getGmtTime()));
					
					tmpMap.clear();
					tmpMap.put("gmtTime", Util.null2str(holydayList.getGmtTime()));
					tmpMap.put("entCode", Util.null2str(holydayList.getEntCode()));
					tmpMap.put("entBusinessCode", "");
					tmpMap.put("updateUserId", Util.null2str(holydayList.getRegisterUserId()));
					tmpMap.put("solution", "ECM");
					tmpMap.put("stockType", "HOL");
					tmpMap.put("stockSign", "HOL");
					tmpMap.put("stockSeqLength", 8);
					tmpMap.put("dateLength", null);
					tmpMap.put("signType", null);
					tmpMap.put("stockDateFormat", null);
					tmpMap.put("resultCode", "FIXED_SIGN_AND_SEQ_NO");
					tmpMap.put("stockRemark", "HOLYDAY-SEQ NO");
					holyday.setSeqNoId(commonMapper.selectNextStockMaxNo(tmpMap));
					
					holydayMapper.insertHolyDay(holyday);
				}
			}else if("delete".equals(action)) {
				holydayMapper.deleteHolyDay(holydayList);
			}
		}
		
		return holydayResult;
	}

	public HolydayResult excelUpload(MultipartFile holydayExcel, String entCode) throws Exception{
		Map<String, Object> resultData = new HashMap<String, Object>();
		HolydayResult holydayResult = new HolydayResult();
		
		// 파일 확장자 유효성 검사
		if(!fileService.checkExtension(holydayExcel, new String[]{"xlsx", "xls"})) {
			holydayResult.setResultCode("extError");
			return holydayResult;
		}
		
		FileUploadResponse result = fileService.storeFile(holydayExcel, entCode, true);
		if(Util.null2str(result.getResult()).equals("ok")){
			File file = fileService.loadFileAsResource(result.getFileName(), entCode, true).getFile();
			String extension = null;
			if(file.getName().toLowerCase().indexOf(".xlsx") > -1){
				extension = "xlsx";
			}else{
				extension = "xls";
			}
			
			List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
			
			String[] fieldName		= new String[] {"국가",		"공휴일 타입",	"시작일자",		"종료일자",	"공휴일명",		"비고"};
			String[] fieldTitle		= new String[] {"country",	"holydayType",	"startDate",	"endDate",	"holydayName",	"remarks"};
			String[] fieldLength	= new String[] {"2",		"1",			"",				"",			"50",			"200"};
			String[] fieldType		= new String[] {"S",		"S",			"c",			"DATE",		"S",			"SN"};

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
					for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
						if(sheet.getRow(i) == null){
							holydayResult.setResultCode("A11233");	/** #1#행이 비어있습니다. (파일 마지막행 : #2#) */
							resultData.put("nullLine", i + 1);
							resultData.put("lastLine", sheet.getLastRowNum() + 1);
							holydayResult.setResultData(resultData);
							return holydayResult;
						}

						dataMap = new HashMap<String, Object>();
						for(int j = 0; j < fieldSize; j++){
							myCell = (XSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i, false);
							if(!Util.null2str(fieldValue).equals("")){
								dataMap.put(fieldTitle[j], fieldValue);
							}
							
							//소문자로 들어왔을경우를 감안하여 처리
							if(fieldName[j].equals("country")) {
								String country = fieldName[j].toUpperCase();
								dataMap.put("country", country);
							}
							
							if(dataMap.size() == 0) {
								holydayResult.setResultCode("A11233");	/** #1#행이 비어있습니다. (파일 마지막행 : #2#) */
								resultData.put("nullLine", i + 1);
								resultData.put("lastLine", sheet.getLastRowNum() + 1);
								holydayResult.setResultData(resultData);
								return holydayResult;
							}
						}
						dataList.add(dataMap);
					}
				}else{
					HSSFCell myCell = null;
					for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
						dataMap = new HashMap<String, Object>();
						
						for(int j = 0; j < fieldSize; j++){
							myCell = (HSSFCell)sheet.getRow(i).getCell(j, MissingCellPolicy.CREATE_NULL_AS_BLANK);
							fieldValue = this.dataChk(df.formatCellValue(myCell), fieldTitle[j], fieldLength[j], fieldType[j], errMsg, i, false);
							if(!Util.null2str(fieldValue).equals("")){
								dataMap.put(fieldTitle[j], fieldValue);
							}
							
							//소문자로 들어왔을경우를 감안하여 처리
							if(fieldName[j].equals("country")) {
								String country = fieldName[j].toUpperCase();
								dataMap.put("country", country);
							}
							
							if(sheet.getRow(i) == null){
								holydayResult.setResultCode("A11233");	/** #1#행이 비어있습니다. (파일 마지막행 : #2#) */
								resultData.put("nullLine", i + 1);
								resultData.put("lastLine", sheet.getLastRowNum() + 1);
								holydayResult.setResultData(resultData);
								return holydayResult;
							}
							
							if(dataMap.size() == 0) {
								holydayResult.setResultCode("A11233");	/** #1#행이 비어있습니다. (파일 마지막행 : #2#) */
								resultData.put("nullLine", i + 1);
								resultData.put("lastLine", sheet.getLastRowNum() + 1);
								holydayResult.setResultData(resultData);
								return holydayResult;
							}
						}
						dataList.add(dataMap);
					}
				}
				//TODO 파일삭제?
				
				resultData.put("dataList", dataList);
				resultData.put("entCode", entCode);
				resultData.put("dataSize", dataList.size());
				
				holydayResult.setResultData(resultData);
				holydayResult.setResultCode("200");
				
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
			holydayResult.setResultCode("ERROR");
		}
		
		return holydayResult;
	}
	
	public String dataChk(String data, String name, String length, String type, StringBuffer errMsg, int row, boolean null2zero) throws Exception {
		if(type.equals("DATE")){
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
}

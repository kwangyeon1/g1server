package org.yht.exps.express.service.eCommerce;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yht.exps.express.mapper.eCommerce.ManifestFileMapper;
import org.yht.exps.express.mapper.eCommerce.UpsMngMapper;
import org.yht.exps.express.model.eCommerce.order.ManifestFile;
import org.yht.exps.express.model.eCommerce.order.OrderSaveResult;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.util.DateTime;
import org.yht.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class UpsMngService {

	@Autowired
	CustomProperties customProperties;
	@Autowired
	UpsMngMapper upsMngMapper;
	@Autowired
	ManifestFileMapper manifestFileMapper;

	/**
	 * UPS 그룹관리 파일업로드
	 * @param
	 * @return
	 * @throws Exception
	 */
	public OrderSaveResult insertUpsGroupCodeExcel(String entCode, String userId, String gmtTime, File file) throws Exception {
		OrderSaveResult orderSaveResult = new OrderSaveResult();

		if(file != null) {

			String[] fieldName = null;
			String[] fieldTitle = null;
			String[] fieldLength = null;
			String[] fieldType = null;

			fieldName = 	new String[]{"groupName","countryCode","cityPort"}; // 컬럼명
			fieldTitle =	new String[]{"그룹명","국가코드","도시코드"}; 		 // 타이틀
			fieldLength =	new String[]{"10","2","10"}; 	// 길이
			fieldType =		new String[]{"S","S","S"}; 		// 타입

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

				if("xls".equals(extension)) {
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

							dataMap.put(fieldName[j], fieldValue);

						}
						dataMap.put("bound", "AIR");
						dataMap.put("boundType", "EXP");
						dataMap.put("userId", userId);
						dataMap.put("updateUserId", userId);
						dataMap.put("gmtTime", gmtTime);
						dataList.add(dataMap);
					}
				} else {
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

							dataMap.put(fieldName[j], fieldValue);

						}
						dataMap.put("bound", "AIR");
						dataMap.put("boundType", "EXP");
						dataMap.put("userId", userId);
						dataMap.put("updateUserId", userId);
						dataMap.put("gmtTime", gmtTime);
						dataList.add(dataMap);
					}
				}

				if(errMsg.length() != 0){
					orderSaveResult.setResultCode("dataErr");
					Map<String, Object> errMap = new HashMap<String, Object>();
					errMap.put("errStr", errMsg);
					orderSaveResult.setResultData(errMap);
					return orderSaveResult;
				}

				ObjectMapper objectMapper = new ObjectMapper();

				List<ManifestFile> excelDataList = dataList.stream()
						.map(map -> objectMapper.convertValue(map, ManifestFile.class))
						.collect(Collectors.toList());

				if(!excelDataList.isEmpty()) {
					try {
						for(ManifestFile data : excelDataList) {
							//중복체크후 생략
							int dupCheck = upsMngMapper.selectUpsGroupDup(data);
							if(dupCheck == 0 && Util.null2str(data.getGroupName()) != "" && (Util.null2str(data.getCountryCode()) != "" || Util.null2str(data.getCityPort()) != "")) {
								manifestFileMapper.insertUpsGroup(data);
							}
						}
					}catch(Exception e) {
						throw e;
					}finally{
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

		return orderSaveResult;
	}

	public String dataChk(String data, String name, String length, String type, StringBuffer errMsg, int row) throws Exception {
		return this.dataChk(data, name, length, type, errMsg, row, true);
	}
	public String dataChk(String data, String name, String length, String type, StringBuffer errMsg, int row, boolean null2zero) throws Exception {
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
				if(!"pass".equals(length) && data.length() > Util.null2int(length)) {
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

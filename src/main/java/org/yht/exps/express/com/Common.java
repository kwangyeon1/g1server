package org.yht.exps.express.com;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Component;
import org.yht.exps.express.com.excel.CreateExcelFile;
import org.yht.exps.express.com.excel.ExcelStyles;
import org.yht.exps.express.model.DownloadFileInfo;
import org.yht.exps.express.model.properties.CustomProperties;
import org.yht.util.DateTimeUtil;
import org.yht.util.FileUtil;
import org.yht.util.Util;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Slf4j
@Component
public class Common {
	@Autowired
	private CustomProperties customProperties;
	private static CustomProperties staticCustomProperties;
	
	@PostConstruct     
	private void initStaticCustomProperties () {
		staticCustomProperties = this.customProperties;
	}
	
	/**
	 * 메뉴 리스트를 트리형태에 맞게 재구성
	 * @param menuArray
	 * @param menuObj
	 * @param upper
	 * @return
	 * @throws java.lang.Exception
	 */
	public static boolean setMenuArray(JSONArray menuArray, JSONObject menuObj, String upper) throws java.lang.Exception {
		boolean result = false;
		
		for(int i = 0; i < menuArray.size(); i++) {
			JSONObject tmpMenuObj = menuArray.getJSONObject(i);
			
			String id = tmpMenuObj.getString("id");
			if(upper.equals(id)) {
				tmpMenuObj.getJSONArray("items").add(menuObj);
				result = true;
				break;
			} else {
				result = setMenuArray(tmpMenuObj.getJSONArray("items"), menuObj, upper);
				if(result)	break;
			}
		}
		
		return result;
	}
	
	/**
	 * 사용자정의 엑셀다운로드 출력용 Json을 리턴
	 * @param query
	 * @param srchMode
	 * @param order
	 * @param where
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static DownloadFileInfo getCustomizeExcelList(Map<String, Object> searchMap) throws Exception {
		DownloadFileInfo downloadFileInfo = new DownloadFileInfo();
		
		List<Map<String, Object>> itemList = (List<Map<String, Object>>)searchMap.get("excelItemList");
		if(itemList == null || itemList.size() == 0){
			downloadFileInfo.setResultCode("A11198");	/** Excel download 설정 데이터가 없습니다. */
			return downloadFileInfo;
		}
		
		String fileName = Util.null2str(searchMap.get("documentName"));
		Workbook wb;
		Sheet sheet;
		
		/** xls 타입이면서 컬럼이 개수가 88초과인 경우 에러 발생 2016.12.05 blue */
		int itemColMax = 0;
		if(itemList.size() > 0){
			itemColMax = Util.excelEng2int(itemList.get(itemList.size() - 1).get("TITLE_COL"));
		}
		if(itemColMax > 88) {
			wb = new XSSFWorkbook();
		}else{
			wb = new HSSFWorkbook();
		}
		sheet = wb.createSheet(fileName);	//시트생성
		Cell cell;
		Map<String, CellStyle> comStyle = ExcelStyles.createStyles(wb);	//Column별 Style은 동일하므로 Column별 Style을 생성한다.
		
		int titleRow = 0;
		Row headerRow;
		
		titleRow = Util.null2int(itemList.get(0).get("TITLE_ROW")) - 1;
		
		//the header row: centered text
		headerRow = sheet.createRow(titleRow);
		headerRow.setHeightInPoints(15.00f);
		headerRow.setHeight((short) 300);

		int cnt = 1;
		List<Map<String, Object>> hItemList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> iItemList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> lItemList = new ArrayList<Map<String, Object>>();
		int dataTypeCol = 0;
		String oblOdGubn;
		int lastColNum = 0;
		int colNum = 0;
		int fieldLength = 0;
		for(Map<String, Object> itemMap : itemList){
			colNum = Util.excelEng2int(itemMap.get("TITLE_COL"));
			fieldLength = Util.null2int(itemMap.get("FIELD_LENGTH"));
			cell = headerRow.createCell(colNum);
			cell.setCellValue(Util.null2str(itemMap.get("DB_FIELD_NAME")).replaceAll("<br/>", " ").replaceAll("<br>", " ").replaceAll("<br />", " "));
			if(cnt == 1){
				cell.setCellStyle(comStyle.get("header_left"));
			}else{
				cell.setCellStyle(comStyle.get("header"));
			}
			if(lastColNum < colNum){
				lastColNum = colNum;
			}
			sheet.setColumnWidth(colNum, fieldLength == 0 ? 4000 : fieldLength * 36);
			itemMap.put("DB_FIELD", JdbcUtils.convertUnderscoreNameToPropertyName(Util.null2str(itemMap.get("DB_FIELD"))));
			cnt++;
		}
		cell = headerRow.getCell(lastColNum);
		cell.setCellStyle(comStyle.get("header_right"));
		
		//freeze the first row
		sheet.createFreezePane(0, titleRow + 1);
		
		List<Map<String, Object>> dataList = (List<Map<String, Object>>)searchMap.get("data");
		ObjectMapper objectMapper = new ObjectMapper();
    	Map<String, Object> dataMap = new HashMap<String, Object>();
		for(Object data : dataList){
			dataMap = (Map<String, Object>)objectMapper.convertValue(data, Map.class);
			headerRow = sheet.createRow(++titleRow);
			headerRow.setHeightInPoints(15.00f);
			headerRow.setHeight((short) 300);
			for(Map<String, Object> itemMap : itemList){
				cell = headerRow.createCell(Util.excelEng2int(itemMap.get("TITLE_COL")));
				cell.setCellValue(Util.null2str(dataMap.get(itemMap.get("DB_FIELD"))));
			}
		}
		

		String dir = staticCustomProperties.getExcelDownload();
		// Write the output to a file
		String currDtm = DateTimeUtil.getDate("yyyyMMddHHmmss");
		
		String saveFile = fileName + "_" + currDtm + ".xls";
		String originFile = fileName + ".xls";
		if(wb instanceof XSSFWorkbook) {
			saveFile += "x";
			originFile += "x";
		}
		
		//workbook파일 출력
		FileUtil.fileWrite(wb, dir, saveFile);
		
		downloadFileInfo.setSaveFile(saveFile);
		downloadFileInfo.setOriginFile(originFile);
		
		return downloadFileInfo;
	}
	
	public static DownloadFileInfo getJsonForExcelList(String fileName, Map<String, Object> searchMap) throws Exception {
		return getJsonForExcelList(fileName, searchMap, "xls");
	}
	
	/**
	 * 리스트 json을 받아서 엑셀 출력용 Json을 리턴
	 * @param query
	 * @param srchMode
	 * @param order
	 * @param where
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static DownloadFileInfo getJsonForExcelList(String fileName, Map<String, Object> searchMap, String excelType) throws Exception {
		if("".equals(Util.null2str(excelType)))		excelType = "xls";
		
		List<String> columns = (List<String>)searchMap.get("columns");
		List<String> excelTypes = (List<String>)searchMap.get("excelTypes");
		List<Integer> numFormats = (List<Integer>)searchMap.get("numFormats");
		List<String> titles = (List<String>)searchMap.get("titles");
		
		String userName = Util.null2str(searchMap.get("userName"));
		String dateFormat = Util.null2str(searchMap.get("dateFormat"));
		String mainTitle = Util.null2str(searchMap.get("mainTitle"));
		
		
		if( (excelType.equalsIgnoreCase("xls")) && (titles !=null) && (titles.size() > 80) ) {
			excelType = "xlsx";
		}
		
		CreateExcelFile createExcelFile = new CreateExcelFile();
		String[] fileUrl = null;
		if(mainTitle.contains("Cargo Manifest")) {
			fileUrl = createExcelFile.makeCargoManifestExcel(excelType, searchMap, mainTitle, fileName, userName, dateFormat);
		}else if(mainTitle.contains("brosLicense")) {
			fileUrl = createExcelFile.makeLicenseBrosExcel(excelType, searchMap, mainTitle, fileName, userName, dateFormat);
		}else if("dailyReport".equals(searchMap.get("menuName"))){
			fileUrl = createExcelFile.makeDailyReportExcel(excelType, searchMap, columns, excelTypes, numFormats, titles, mainTitle, fileName, userName, dateFormat);
		}else if(mainTitle.contains("selection")){
			fileUrl = createExcelFile.makeSelectionExcel(excelType, searchMap, columns, excelTypes, numFormats, titles, mainTitle, fileName, userName, dateFormat);
		}else if(mainTitle.contains("UpsNewbilling")){
			fileUrl = createExcelFile.makeUpsNewbillingExcel(excelType, searchMap, mainTitle, fileName, userName, dateFormat);
		}else if(mainTitle.contains("Delivery")){
			fileUrl = createExcelFile.makeDeliveryExcel(excelType, searchMap, columns, excelTypes, numFormats, titles, mainTitle, fileName, userName, "yyyy-MM-dd HH:mm:ss", true);
		}else if("gridJp".equals(Util.null2str(searchMap.get("documentCode"))) || "gridCn".equals(Util.null2str(searchMap.get("documentCode")))){
			fileUrl = createExcelFile.makeExcelCustomsExpExcel(excelType, searchMap, columns, excelTypes, numFormats, titles, mainTitle, fileName, userName, dateFormat);
		}else {
			if("PANTK".equals(Util.null2str(searchMap.get("srchEntCode"))) || "YHTXX".equals(Util.null2str(searchMap.get("srchEntCode")))) {
				//if( "Y".equals(Util.null2str(searchMap.get("dayByDayDownloadYn"))) ){ //순차적_분할다운로드이고 해당 구간은 데이터가 없을때 다운 X
					List<Object> dataRows = (List<Object>) searchMap.get("data");
					if (dataRows == null || (dataRows != null && dataRows.size() == 0)) { // 해당 구간은 데이터가 없을때 다운 X
						DownloadFileInfo downloadFileInfo = new DownloadFileInfo();
						downloadFileInfo.setSaveFile("");
						downloadFileInfo.setOriginFile("");
						return downloadFileInfo;
					}
				//}
				fileUrl = createExcelFile.makeExcelNoStyleNoHeader(excelType, searchMap, columns, excelTypes, numFormats, titles, mainTitle, fileName, userName, dateFormat);
			}else if("UPSXK".equals(Util.null2str(searchMap.get("srchEntCode"))) && "Y".equals(Util.null2str(searchMap.get("dayByDayDownloadYn")))) {
				List<Object> dataRows = (List<Object>) searchMap.get("data");
				if (dataRows == null || (dataRows != null && dataRows.size() == 0)) { // 해당 구간은 데이터가 없을때 다운 X
					DownloadFileInfo downloadFileInfo = new DownloadFileInfo();
					downloadFileInfo.setSaveFile("");
					downloadFileInfo.setOriginFile("");
					return downloadFileInfo;
				}
				
				fileUrl = createExcelFile.makeExcelNoStyleNoHeader(excelType, searchMap, columns, excelTypes, numFormats, titles, mainTitle, fileName, userName, dateFormat);
			}else {
				fileUrl = createExcelFile.makeExcel(excelType, searchMap, columns, excelTypes, numFormats, titles, mainTitle, fileName, userName, dateFormat);
			}
		}

		// 파일 생성이 완료되었을 경우
		String saveFile = fileUrl[0];
		String originFile = fileUrl[1];

		DownloadFileInfo downloadFileInfo = new DownloadFileInfo();
		downloadFileInfo.setSaveFile(saveFile);
		downloadFileInfo.setOriginFile(originFile);
		
		return downloadFileInfo;
	}
	
	/**
	 * 한글이 포함되어 있는지 여부(포함되있으면: true, 아니면: false)
	 * @param str
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean checkSaveFilenameForExcel(String str) {
        // 정규 표현식을 이용하여 한글이 포함되어 있는지 검사
        return str.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
    }
	
}
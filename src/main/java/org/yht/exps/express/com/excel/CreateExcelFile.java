package org.yht.exps.express.com.excel;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HeaderFooter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yht.exps.express.com.Common;
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
public class CreateExcelFile {
	private static final int FAST_EXCEL_ROW_WINDOW_SIZE = 1000;
	private static final int FAST_EXCEL_FLUSH_INTERVAL = 2000;
	private static final int FAST_EXCEL_FLUSH_RETAINED_ROWS = 200;

	@Autowired
	private CustomProperties customProperties;
	private static CustomProperties staticCustomProperties;
	
	@Value("${spring.profiles.active}")
	private String profile;
	private static String staticProfile;
	
	@PostConstruct     
	private void initStaticCustomProperties () {
		staticCustomProperties = this.customProperties;
		staticProfile = this.profile;
	}
	
	
	/**
	 * 엑셀 파일 생성
	 * @param excelType
	 * @param dataListObj
	 * @param columns
	 * @param titles
	 * @param sheetTitle
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String[] makeExcel(
			String excelType, Map<String, Object> searchMap, List<String> columns, List<String> excelTypes, 
			List<Integer> numFormats, List<String> titles, String sheetTitle, String frontFileName, 
			String userName, String dateFormat) throws Exception {
		
		String excelDir = staticCustomProperties.getExcelDownload();
		
		// Return값 [0]: 실제 파일 저장경로, [1]: 다운로드받을 파일명
        String[] files = new String[2];
        FileInputStream file= null;
		Workbook wb;
		Sheet sheet;
		if(!Util.null2str(searchMap.get("saveFile")).equals("") ){  // 엑셀파일이 있는경우 다음 sheet 생성하여 데이터 적재 
			file = new FileInputStream(searchMap.get("saveFile").toString()); 
						
			if("xls".equals(Util.null2str(excelType))) 	wb = new HSSFWorkbook(file);
	        else 										wb = new SXSSFWorkbook(new XSSFWorkbook(file), 100);
			
			sheet = wb.createSheet(sheetTitle.replaceAll("/", "") +"("+searchMap.get("sheetNo").toString()+")");
			sheet = wb.getSheetAt(Integer.parseInt(searchMap.get("sheetNo").toString())); // 시트정보읽어온다
		}
		else{ 
			if("xls".equals(Util.null2str(excelType))) 	wb = new HSSFWorkbook();
	        else 										wb = new SXSSFWorkbook(100);
			
			sheet = wb.createSheet(sheetTitle.replaceAll("/", "")); // 시트생성
		}
		
		DataFormat df = wb.createDataFormat();		
		
		
		Font headerFont = wb.createFont();
		headerFont.setBold(true);
		headerFont.setFontName("Arial");
		headerFont.setFontHeightInPoints((short)10);
		headerFont.setColor(IndexedColors.WHITE.getIndex());
		Font titleFont = wb.createFont();
		titleFont.setBold(true);
		titleFont.setUnderline(HSSFFont.U_SINGLE); 
		titleFont.setFontName("Arial");
		titleFont.setFontHeightInPoints((short)16);
		Font font4 = wb.createFont();
		font4.setFontName("Arial");
		font4.setFontHeightInPoints((short)10);
		
		
		Map<String, CellStyle> comStyle = new HashMap<String, CellStyle>();
		String[] styleTitleArr = {"system","system_value","title","header_right","header_left","header"};
		for(String styleTitle : styleTitleArr){
			ExcelStyles.createStyles(wb, comStyle, styleTitle, headerFont, titleFont, font4, df);
		}
		
		List<Map<String, CellStyle>> styleList = new ArrayList<Map<String, CellStyle>>();
		
		for(int i = 0; i < titles.size(); i++) {
			Map<String, CellStyle> style = new HashMap<String, CellStyle>();
			styleList.add(style);
		}
		
		PrintSetup printSetup = sheet.getPrintSetup();
		setPaperSize(printSetup, PrintSetup.A4_PAPERSIZE, true);
		sheet.setAutobreaks(true); // Option Button Move
		
		boolean headerYn = true;
		//요율관리에서는 해더안나오게 표현하기 위함, UPS 적하목록정보 엑셀파일생성도 헤더안나오게 수정
		if("agent_charge_list".equals(frontFileName) || "delivery_chg_List".equals(frontFileName) || "Correction_Upload_List".equals(frontFileName) || "Ups_Manifest_List".equals(frontFileName) || "xray판독".equals(frontFileName)) {
			headerYn = false;
		}
		
		int rownum = 0;
		//지정창고반출요청(해상)일때 헤더 다르게 주기 위해 분기시킴
		if(frontFileName.equals("Manifest_List")) {
			if(headerYn) {
				for(int i = 0; i < 3; i++) {
					Row sysInfoRow = sheet.createRow(i);
					rownum = i;
					sysInfoRow.setHeight((short) 400);
					Cell sysInfoTitleCell = sysInfoRow.createCell(0);
					Cell sysInfoValueCell = null;
					switch(i) {
					case 0 :
						sysInfoTitleCell.setCellValue("System");
						sysInfoValueCell = sysInfoRow.createCell(1);
						sysInfoValueCell.setCellValue("특송적하/목록자료조회(OI)");
						break;
					case 1 :
						sysInfoTitleCell.setCellValue("Writer");
						sysInfoValueCell = sysInfoRow.createCell(1);
						sysInfoValueCell.setCellValue(userName);
						break;
					case 2 :
						sysInfoTitleCell.setCellValue("Date");
						sysInfoValueCell = sysInfoRow.createCell(1);
						sysInfoValueCell.setCellValue(DateTimeUtil.getDate(dateFormat));
						break;
					default :
						sysInfoTitleCell.setCellValue("");
						sysInfoValueCell = sysInfoRow.createCell(1);
						sysInfoValueCell.setCellValue("");
					}
		
					sysInfoTitleCell.setCellStyle(comStyle.get("system"));
					sysInfoValueCell.setCellStyle(comStyle.get("system_value"));
					
					for (int j = 2, n = titles.size(); j < n; j++) {
						sysInfoValueCell = sysInfoRow.createCell(j);
						sysInfoValueCell.setCellValue("");
						sysInfoValueCell.setCellStyle(comStyle.get("system_value"));
					}
		
					sheet.addMergedRegion(new CellRangeAddress(i, i, 1, titles.size()-1));
				}
				
				rownum++;

				Row titleRow = sheet.createRow(rownum);
				titleRow.setHeightInPoints(29.25f);
				Cell titleCell = titleRow.createCell(0);
				titleCell.setCellValue(sheetTitle);
				titleCell.setCellStyle(comStyle.get("title"));
				sheet.addMergedRegion(new CellRangeAddress(rownum, rownum, 0, titles.size()-1));
				
				rownum = rownum+2;
			}
		}else {
			if(headerYn) {
				for(int i = 0; i < 4; i++) {
					Row sysInfoRow = sheet.createRow(i);
					rownum = i;
					sysInfoRow.setHeight((short) 400);
					Cell sysInfoTitleCell = sysInfoRow.createCell(0);
					Cell sysInfoValueCell = null;
					switch(i) {
					case 0 :
						sysInfoTitleCell.setCellValue("Program");
						sysInfoValueCell = sysInfoRow.createCell(1);
						sysInfoValueCell.setCellValue("Express System");
						break;
					case 1 :
						sysInfoTitleCell.setCellValue("System");
						sysInfoValueCell = sysInfoRow.createCell(1);
						sysInfoValueCell.setCellValue(sheetTitle);
						break;
					case 2 :
						sysInfoTitleCell.setCellValue("Writer");
						sysInfoValueCell = sysInfoRow.createCell(1);
						sysInfoValueCell.setCellValue(userName);
						break;
					case 3 :
						sysInfoTitleCell.setCellValue("Date");
						sysInfoValueCell = sysInfoRow.createCell(1);
						sysInfoValueCell.setCellValue(DateTimeUtil.getDate(dateFormat));
						break;
					default :
						sysInfoTitleCell.setCellValue("");
						sysInfoValueCell = sysInfoRow.createCell(1);
						sysInfoValueCell.setCellValue("");
					}
		
					sysInfoTitleCell.setCellStyle(comStyle.get("system"));
					sysInfoValueCell.setCellStyle(comStyle.get("system_value"));
					
					for (int j = 2, n = titles.size(); j < n; j++) {
						sysInfoValueCell = sysInfoRow.createCell(j);
						sysInfoValueCell.setCellValue("");
						sysInfoValueCell.setCellStyle(comStyle.get("system_value"));
					}
		
					sheet.addMergedRegion(new CellRangeAddress(i, i, 1, titles.size()-1));
				}
				
				rownum++;
	
				Row titleRow = sheet.createRow(rownum);
				titleRow.setHeightInPoints(29.25f);
				Cell titleCell = titleRow.createCell(0);
				titleCell.setCellValue(sheetTitle);
				titleCell.setCellStyle(comStyle.get("title"));
				sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, titles.size()-1));
				
				rownum = rownum+2;
			}
		}
	
        //the header row: centered text
        Row headerRow = sheet.createRow(rownum);
        headerRow.setHeight((short) 300);
        headerRow.setHeightInPoints(15.00f);

        Cell cell;
        boolean isTotalStyleType = false; // 합계 표현용 ROW 추가 여부
        boolean wrapText = true; // 자동줄바꿈 여부
        CreationHelper factory = wb.getCreationHelper();
		Drawing<?> drawing = sheet.createDrawingPatriarch();
		// When the comment box is visible, have it show in a 1x3 space
		ClientAnchor anchor = factory.createClientAnchor();
		Comment comment;
		RichTextString memoStr;
        
		//해더셋팅
		List<Map<String, Object>> memosList = (List<Map<String, Object>>)searchMap.get("memos");
        for (int i = 0, n = titles.size(); i < n; i++) {
            cell = headerRow.createCell(i);
            cell.setCellValue(Util.null2str(titles.get(i)).replaceAll("<br/>", " ").replaceAll("<br>", " ").replaceAll("<br />", " "));
		                
            if(memosList !=null && memosList.size() > i) {
            	String memo = Util.null2str(memosList.get(i));
            	//해더에 메모추가 시작
                if(!memo.equals("")){
    				anchor.setCol1(cell.getColumnIndex());
    				anchor.setCol2(cell.getColumnIndex()+2);
    				anchor.setRow1(headerRow.getRowNum());
    				anchor.setRow2(headerRow.getRowNum()+12);
    				comment = drawing.createCellComment(anchor);    				
    				memoStr = factory.createRichTextString(memo);
    				comment.setString(memoStr);
    				cell.setCellComment(comment);
    			}     
                //해더에 메모추가 끝
            }            
            
            if((i+1) == n) {
            	cell.setCellStyle(comStyle.get("header_right"));
            } else if(i == 0) {
            	cell.setCellStyle(comStyle.get("header_left"));
            } else {
            	cell.setCellStyle(comStyle.get("header"));
            }
            if(!Util.null2str(columns.get(i)).equals("num") && ("number".equals(Util.null2str(excelTypes.get(i), "left")) || "double".equals(Util.null2str(excelTypes.get(i), "left")))) isTotalStyleType = true;			// 하단의 합계 부분 추가 여부
        }
		if(frontFileName.equals("Daily performance") || frontFileName.equals("CollectPaymentAccount_List") || frontFileName.equals("CollectPaymentBalance_List") 
				|| frontFileName.equals("agent_charge_list") || frontFileName.equals("Duplicate_House_List") ){
			isTotalStyleType = false;
		}
		
		//요율관리일때는 해더고정안함, UPS 적하목록정보 엑셀파일생성일때도 헤더고정안함
		if(!frontFileName.equals("agent_charge_list") && !frontFileName.equals("Correction_Upload_List") && !frontFileName.equals("delivery_chg_List") && !frontFileName.equals("Ups_Manifest_List") && !frontFileName.equals("xray판독")) {
			//freeze the first row
			sheet.createFreezePane(0, 7);
		}
		
		//지정창고반출요청일때는 6행(헤더) 고정
		if(frontFileName.equals("Manifest_List")) {
			//freeze the first row
			sheet.createFreezePane(0, 6);
		}
		
		// UPS는 자동줄바꿈 적용하지 않음
		if(staticProfile.equals("ups1")) {
			wrapText = false;
		}
        
        Row row;
        rownum++;
        List<Object> datas = (List<Object>)searchMap.get("data");

        Map<String, CellStyle> styles;
        String styleType = "";
		String styleName = "";
        int numFormat = 0;
    	int titleSize = titles.size();
    	int totalAddRow = isTotalStyleType ? 1 : 0;
		ObjectMapper objectMapper = new ObjectMapper();
    	Map<String, Object> data = new HashMap<String, Object>();
        for (int i = 0, n = datas.size() + totalAddRow; i < n; i++, rownum++) {
        	row = sheet.createRow(rownum);
        	row.setHeight((short) 300);
        	
        	if(isTotalStyleType && (i+1) == n){
        		// 합계 표현용 마지막 ROW 추가
        		data.clear();
        		data.put("num", "TOTAL");
        	}else{
    			data = (Map<String, Object>)objectMapper.convertValue(datas.get(i), Map.class);
    			data.put("num", i +1);
        	}
        	
        	//POD관리 다운로드 시 줄 높이 수정
        	if("podList".equals(frontFileName)) {
        		int maxLine = 1;
        		for(Object val : data.values()) {
        			if (val != null && val.toString().contains("\n")) {
    	                int valueLines = val.toString().split("\n", -1).length;
    	                if (valueLines > maxLine) maxLine = valueLines;
        	        }
        		}
        		row.setHeight((short) (300 * maxLine));
        	}
        	
        	String columnName = "";
        	for(int j = 0; j < titleSize; j++) {
        		cell = row.createCell(j);
        		columnName = Util.null2str(columns.get(j));
        		
        		// Cell Style Setting
        		styles = styleList.get(j);
        		styleType = Util.null2str(excelTypes.get(j), "left");
        		numFormat = Util.null2int(numFormats.get(j));
        		/** NUM컬럼의 순서를 변경 후 엑셀 다운로드시 오류 발생해서 수정 */
//        		if(isTotalStyleType && (i+1) == n && j == 0){
        		if(isTotalStyleType && (i+1) == n && "num".equals(columnName)){
        			styleType = "center";
        		}
        		String defaultFormat = "#,##0";
        		switch(styleType) {
        			case "left" :
        				if((i+1) == n) {
        					if(j == 0)					styleName = "cell_normal_left_bottom";
        					else if((j+1) == titleSize)	styleName = "cell_normal_right_bottom";
        					else						styleName = "cell_normal_bottom";
        				} else {
        					if(j == 0)					styleName = "cell_normal_left";
        					else if((j+1) == titleSize)	styleName = "cell_normal_right";
        					else						styleName = "cell_normal";
        				}
        				
        				if(styles.get(styleName) == null){
        					ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df, wrapText);
        				}
        				cell.setCellStyle(styles.get(styleName));
        				// UPS 적하목록정보 엑셀파일생성 : 값 없으면 비워둠
        				if(frontFileName.equals("Ups_Manifest_List")) {
        					if(Util.null2str(data.get(columnName)).equals("")) {
        						break;
        					}
        				}
    					cell.setCellValue(Util.null2str(data.get(columnName)));
    					
        				break;
        			case "center" :
        				if((i+1) == n) {
        					if(j == 0)					styleName = "cell_normal_centered_left_bottom";
        					else if((j+1) == titleSize)	styleName = "cell_normal_centered_right_bottom";
        					else						styleName = "cell_normal_centered_bottom";
        				} else {
        					if(j == 0)					styleName = "cell_normal_centered_left";
        					else if((j+1) == titleSize)	styleName = "cell_normal_centered_right";
        					else						styleName = "cell_normal_centered";
        				}

        				if(styles.get(styleName) == null){
        					ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df, wrapText);
        				}
        				cell.setCellStyle(styles.get(styleName));
        				// UPS 적하목록정보 엑셀파일생성 : 값 없으면 비워둠
        				if(frontFileName.equals("Ups_Manifest_List")) {
        					if(Util.null2str(data.get(columnName)).equals("")) {
        						break;
        					}
        				}
    					cell.setCellValue(Util.null2str(data.get(columnName)));
                		
        				break;
        			case "right" :
        				if((i+1) == n) {
        					if(j == 0)					styleName = "cell_right_left_bottom";
        					else if((j+1) == titleSize)	styleName = "cell_right_right_bottom";
        					else						styleName = "cell_right_bottom";
        				} else {
        					if(j == 0)					styleName = "cell_right_left";
        					else if((j+1) == titleSize)	styleName = "cell_right_right";
        					else						styleName = "cell_right";
        				}
        				
        				if(styles.get(styleName) == null){
        					ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df, wrapText);
        				}
        				cell.setCellStyle(styles.get(styleName));
        				// UPS 적하목록정보 엑셀파일생성 : 값 없으면 비워둠
        				if(frontFileName.equals("Ups_Manifest_List")) {
        					if(Util.null2str(data.get(columnName)).equals("")) {
        						break;
        					}
        				}
                		cell.setCellValue(Util.null2str(data.get(columnName)));
                		
        				break;
        			case "number" :
        				if((i+1) == n) {
        					if(j == 0)					styleName = "cell_normal_number_left_bottom";
        					else if((j+1) == titleSize)	styleName = "cell_normal_number_right_bottom";
        					else						styleName = "cell_normal_number_bottom";
        				} else {
        					if(j == 0)					styleName = "cell_normal_number_left";
        					else if((j+1) == titleSize)	styleName = "cell_normal_number_right";
        					else						styleName = "cell_normal_number";
        				}
        				
        				if(styles.get(styleName) == null){
        					ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df, wrapText);
        					
        					if(numFormat > 0) {
        			        	defaultFormat += ".";
        			        	for(int k = 0; k < numFormat; k++) {
        			        		defaultFormat += "0";
        			        	}
            				}else if(numFormat != 0){
            					String[] digits = Util.null2str(data.get(columnName)).split("\\.");
            					if(digits.length > 1) {
            			        	defaultFormat += ".";
            			        	for(int k = 0; k < digits[1].length(); k++) {
            			        		defaultFormat += "0";
            			        	}        						
            					}
            				}
        					styles.get(styleName).setDataFormat(df.getFormat(defaultFormat));
        				}
        				cell.setCellStyle(styles.get(styleName));
        				
        				// 경영정보 > collect payment 엑셀 셀병합 처리 2020.06.08 sophie
        				if( frontFileName.equals("CollectPaymentAccount_List") ) {
        					cell.setCellValue((Util.null2int(data.get(columnName)) / 4) + 1);
        					
        				}else {
        					cell.setCellValue(Util.null2int(data.get(columnName)));
        				}
        				
                		// NUMBER 마지막 ROW 합계 수식 추가
                		if(isTotalStyleType && (i+1) == n) {
                			//cell.setCellType(CellType.FORMULA);
                			cell.setCellFormula("SUM(" 
                					+ CellReference.convertNumToColString(cell.getColumnIndex()).concat(Integer.toString(8))  
                					+ ":"
                					+ CellReference.convertNumToColString(cell.getColumnIndex()).concat(Integer.toString((cell.getRow().getRowNum()))) 
                					+ ")");
                		}
                		
        				break;
        			case "double" :
        				if((i+1) == n) {
        					if(j == 0)					styleName = "cell_normal_double_left_bottom";
        					else if((j+1) == titleSize)	styleName = "cell_normal_double_right_bottom";
        					else						styleName = "cell_normal_double_bottom";
        				} else {
        					if(j == 0)					styleName = "cell_normal_double_left";
        					else if((j+1) == titleSize)	styleName = "cell_normal_double_right";
        					else						styleName = "cell_normal_double";
        				}
        				
        				if(styles.get(styleName) == null){
        					ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df, wrapText);
        					
        					if(numFormat > 0) {
        			        	defaultFormat += ".";
        			        	for(int k = 0; k < numFormat; k++) {
        			        		defaultFormat += "0";
        			        	}
            				}else if(numFormat != 0){
            					String[] digits = Util.null2str(data.get(columnName)).split("\\.");
            					if(digits.length > 1) {
            			        	defaultFormat += ".";
            			        	for(int k = 0; k < digits[1].length(); k++) {
            			        		defaultFormat += "0";
            			        	}        						
            					}
            				}
        					styles.get(styleName).setDataFormat(df.getFormat(defaultFormat));
        				}
        				cell.setCellStyle(styleList.get(j).get(styleName));
                		cell.setCellValue(Util.null2dbl(data.get(columnName)));
                		
                		// DOUBLE 마지막 ROW 합계 수식 추가
                		if(isTotalStyleType && (i+1) == n){
                			//cell.setCellType(CellType.FORMULA);
                			cell.setCellFormula("SUM(" 
                					+ CellReference.convertNumToColString(cell.getColumnIndex()).concat(Integer.toString(8))  
                					+ ":"
                					+ CellReference.convertNumToColString(cell.getColumnIndex()).concat(Integer.toString((cell.getRow().getRowNum()))) 
                					+ ")");
                		}
                		
        				break;
        			case "date" :
        				if((i+1) == n) {
        					if(j == 0)					styleName = "cell_normal_date_left_bottom";
        					else if((j+1) == titleSize)	styleName = "cell_normal_date_right_bottom";
        					else						styleName = "cell_normal_date_bottom";
        				} else {
        					if(j == 0)					styleName = "cell_normal_date_left";
        					else if((j+1) == titleSize)	styleName = "cell_normal_date_right";
        					else						styleName = "cell_normal_date";
        				}
        				if(styles.get(styleName) == null){
        					ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df, wrapText);
        				}
        				cell.setCellStyle(styles.get(styleName));
    					try{
	        		        long time = (long)data.get(columnName);
	        		        cell.setCellValue(DateTimeUtil.ConvertDate2String(new Date(time), new SimpleDateFormat(dateFormat)));
    					}catch(Exception e){
    						cell.setCellValue("");
    					}
        				
        				break;
        			case "percent" :
        				if((i+1) == n) {
        					if(j == 0)					styleName = "cell_normal_left_bottom";
        					else if((j+1) == titleSize)	styleName = "cell_normal_right_bottom";
        					else						styleName = "cell_normal_bottom";
        				} else {
        					if(j == 0)					styleName = "cell_normal_left";
        					else if((j+1) == titleSize)	styleName = "cell_normal_right";
        					else						styleName = "cell_normal";
        				}
    					try{
    						if(styles.get(styleName) == null){
            					ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df, wrapText);
            				}
    						styles.get(styleName).setDataFormat(df.getFormat("0.00%"));
    						cell.setCellStyle(styles.get(styleName));
            				cell.setCellValue(Util.null2dbl(data.get(columnName)));
    					}catch(Exception e){
    						cell.setCellValue("");
    					}
        				
        				break;
        			default :
        				if((i+1) == n) {
        					if(j == 0)					styleName = "cell_normal_left_bottom";
        					else if((j+1) == titleSize)	styleName = "cell_normal_right_bottom";
        					else						styleName = "cell_normal_bottom";
        				} else {
        					if(j == 0)					styleName = "cell_normal_left";
        					else if((j+1) == titleSize)	styleName = "cell_normal_right";
        					else						styleName = "cell_normal";
        				}
        				if(styles.get(styleName) == null){
        					ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df, wrapText);
        				}
        				cell.setCellStyle(styles.get(styleName));
                		cell.setCellValue(Util.null2str(data.get(columnName)));
        		}
        		
        		 
        	}
        	
        	/** 100개마다 flushRows 메소드를 통해 메모리에 있는 데이터를 디스크(임시파일)로 옮기고 메모리를 비워준다. */
    		if("xlsx".equals(Util.null2str(excelType))) {
    			if (i % 100 == 0) ((SXSSFSheet) sheet).flushRows(100); 
    		}
        }
        
        // 경영정보 > collect payment 엑셀 셀병합 처리 2020.06.08 sophie
        if( frontFileName.equals("CollectPaymentAccount_List") ) {
            int listTotal = datas.size()/4;
            for (int k = 1; k <= listTotal; k++) {
            	sheet.addMergedRegion(new CellRangeAddress( k*4 + 3, k*4 + 6, 0,0)); //number (4)
            	
            	if(k == listTotal) {
            		sheet.addMergedRegion(new CellRangeAddress( k*4 + 3, k*4 + 6, 1,4)); // Total 일 때
            	}else {
            		sheet.addMergedRegion(new CellRangeAddress( k*4 + 3, k*4 + 6, 1,1)); //solution (4)
            		sheet.addMergedRegion(new CellRangeAddress( k*4 + 3, k*4 + 6, 2,2)); //account code (4)
            		sheet.addMergedRegion(new CellRangeAddress( k*4 + 3, k*4 + 6, 3,3)); //account name (4)
                	sheet.addMergedRegion(new CellRangeAddress( k*4 + 3, k*4 + 6, 4,4)); //invoice period (4)
            	}
    		}
        }else if( frontFileName.equals("CollectPaymentBalance_List") ) {
        	int solutionTotal = datas.size()/11;
        	for (int p = 1; p <= solutionTotal; p++) {
        		sheet.addMergedRegion(new CellRangeAddress(p*11 - 4, p*11 + 6, 0,0)); //solution (11)
        		sheet.addMergedRegion(new CellRangeAddress(p*11 - 4, p*11 - 1, 1,1)); //total - collect sum (4)
        		sheet.addMergedRegion(new CellRangeAddress(p*11, p*11 + 3, 1,1)); 	  //total - payment sum (4)
        		sheet.addMergedRegion(new CellRangeAddress(p*11 + 4, p*11 + 6, 1,1)); //total - balance (3)
			}
        }
        
        List<Integer> columnsWidth = (List<Integer>)searchMap.get("columnsWidth");
        int columnsSize;
        for(int i = 0; i < titleSize; i++) {
        	columnsSize = Util.null2int(columnsWidth.get(i)) * 36;
        	if(columnsSize > 65280){
        		sheet.setColumnWidth(i, 65280);
        	}else{
        		sheet.setColumnWidth(i, columnsSize);
        	}
        }
        
        if(Util.null2str(searchMap.get("saveFile")).equals("") ){ // 엑셀파일 생성
        	
        	// Write the output to a file
    		String currDtm = DateTimeUtil.getDate("yyyyMMddHHmmss");
            
            String saveFile = frontFileName + "_" + currDtm + ".xls";													// 임시파일명
        	if(Common.checkSaveFilenameForExcel(frontFileName)) {
    			String uuid = UUID.randomUUID().toString();
                saveFile = uuid + "_" + currDtm + ".xls";
    		}
            String originFile = frontFileName + "_" + currDtm.substring(0,4) + "-" + currDtm.substring(4,6) + "-" + currDtm.substring(6,8) + ".xls";	// 원본파일명
            
            if("xlsx".equals(Util.null2str(excelType))) {
            	saveFile += "x";
            	originFile += "x";
            }
            
            //workbook파일 출력
            FileUtil.fileWrite(wb, excelDir, saveFile);
            
        	// Return값 [0]: 저장된 파일명, [1]: 다운로드받을 파일명
            files[0] = saveFile;
            files[1] = originFile;
		}	
        else{
        	String logVal = searchMap.get("saveFile").toString() +  "  " + searchMap.get("originFile").toString();
        	log.debug(logVal);
        	FileUtil.fileWrite(wb, "", searchMap.get("saveFile").toString());
	        
	        files[0] = searchMap.get("saveFile").toString();
	        files[1] = searchMap.get("originFile").toString();
	        
	        file.close();
        }
        
        return files;
	}

	@SuppressWarnings("unchecked")
	public String[] makeExcel2(
			String excelType, Map<String, Object> searchMap, List<String> columns, List<String> excelTypes,
			List<Integer> numFormats, List<String> titles, String sheetTitle, String frontFileName,
			String userName, String dateFormat) throws Exception {

		String excelDir = staticCustomProperties.getExcelDownload();
		String[] files = new String[2];
		FileInputStream file = null;
		Workbook wb = null;

		List<?> columnsWidth = (List<?>) searchMap.get("columnsWidth");
		List<?> memos = (List<?>) searchMap.get("memos");
		List<Object> dataRows = (List<Object>) searchMap.get("data");
		if (dataRows == null) {
			dataRows = new ArrayList<Object>();
		}
		try {
			Sheet sheet;
			if(!Util.null2str(searchMap.get("saveFile")).equals("") ){
				file = new FileInputStream(searchMap.get("saveFile").toString());

				if("xls".equals(Util.null2str(excelType))) {
					wb = new HSSFWorkbook(file);
				}else {
					wb = new SXSSFWorkbook(new XSSFWorkbook(file), FAST_EXCEL_ROW_WINDOW_SIZE);
				}

				sheet = wb.createSheet(sheetTitle.replaceAll("/", "") +"("+searchMap.get("sheetNo").toString()+")");
				sheet = wb.getSheetAt(Integer.parseInt(searchMap.get("sheetNo").toString()));
			}
			else{
				if("xls".equals(Util.null2str(excelType))) {
					wb = new HSSFWorkbook();
				}else {
					wb = new SXSSFWorkbook(FAST_EXCEL_ROW_WINDOW_SIZE);
				}

				sheet = wb.createSheet(sheetTitle.replaceAll("/", ""));
			}

			DataFormat df = wb.createDataFormat();

			Font headerFont = wb.createFont();
			headerFont.setBold(true);
			headerFont.setFontName("Arial");
			headerFont.setFontHeightInPoints((short)10);
			headerFont.setColor(IndexedColors.WHITE.getIndex());

			Font titleFont = wb.createFont();
			titleFont.setBold(true);
			titleFont.setUnderline(HSSFFont.U_SINGLE);
			titleFont.setFontName("Arial");
			titleFont.setFontHeightInPoints((short)16);

			Font font4 = wb.createFont();
			font4.setFontName("Arial");
			font4.setFontHeightInPoints((short)10);

			Map<String, CellStyle> comStyle = new HashMap<String, CellStyle>();
			String[] styleTitleArr = {"system","system_value","title","header_right","header_left","header"};
			for(String styleTitle : styleTitleArr){
				ExcelStyles.createStyles(wb, comStyle, styleTitle, headerFont, titleFont, font4, df);
			}

			List<Map<String, CellStyle>> styleList = new ArrayList<Map<String, CellStyle>>();
			for (int i = 0; i < titles.size(); i++) {
				styleList.add(new HashMap<String, CellStyle>());
			}

			PrintSetup printSetup = sheet.getPrintSetup();
			setPaperSize(printSetup, PrintSetup.A4_PAPERSIZE, true);
			sheet.setAutobreaks(true);

			boolean headerYn = true;
			if("agent_charge_list".equals(frontFileName) || "Correction_Upload_List".equals(frontFileName) || "Ups_Manifest_List".equals(frontFileName) || "xray판독".equals(frontFileName)) {
				headerYn = false;
			}

			int rownum = 0;
			if(frontFileName.equals("Manifest_List")) {
				if(headerYn) {
					for(int i = 0; i < 3; i++) {
						Row sysInfoRow = sheet.createRow(i);
						rownum = i;
						sysInfoRow.setHeight((short) 400);
						Cell sysInfoTitleCell = sysInfoRow.createCell(0);
						Cell sysInfoValueCell = sysInfoRow.createCell(1);
						switch(i) {
						case 0 :
							sysInfoTitleCell.setCellValue("System");
							sysInfoValueCell.setCellValue("특송적하/목록자료조회(OI)");
							break;
						case 1 :
							sysInfoTitleCell.setCellValue("Writer");
							sysInfoValueCell.setCellValue(userName);
							break;
						case 2 :
							sysInfoTitleCell.setCellValue("Date");
							sysInfoValueCell.setCellValue(DateTimeUtil.getDate(dateFormat));
							break;
						default :
							sysInfoTitleCell.setCellValue("");
							sysInfoValueCell.setCellValue("");
						}

						sysInfoTitleCell.setCellStyle(comStyle.get("system"));
						sysInfoValueCell.setCellStyle(comStyle.get("system_value"));

						for (int j = 2, n = titles.size(); j < n; j++) {
							Cell emptyCell = sysInfoRow.createCell(j);
							emptyCell.setCellValue("");
							emptyCell.setCellStyle(comStyle.get("system_value"));
						}

						sheet.addMergedRegion(new CellRangeAddress(i, i, 1, titles.size()-1));
					}

					rownum++;

					Row titleRow = sheet.createRow(rownum);
					titleRow.setHeightInPoints(29.25f);
					Cell titleCell = titleRow.createCell(0);
					titleCell.setCellValue(sheetTitle);
					titleCell.setCellStyle(comStyle.get("title"));
					sheet.addMergedRegion(new CellRangeAddress(rownum, rownum, 0, titles.size()-1));

					rownum = rownum+2;
				}
			}else {
				if(headerYn) {
					for(int i = 0; i < 4; i++) {
						Row sysInfoRow = sheet.createRow(i);
						rownum = i;
						sysInfoRow.setHeight((short) 400);
						Cell sysInfoTitleCell = sysInfoRow.createCell(0);
						Cell sysInfoValueCell = sysInfoRow.createCell(1);
						switch(i) {
						case 0 :
							sysInfoTitleCell.setCellValue("Program");
							sysInfoValueCell.setCellValue("Express System");
							break;
						case 1 :
							sysInfoTitleCell.setCellValue("System");
							sysInfoValueCell.setCellValue(sheetTitle);
							break;
						case 2 :
							sysInfoTitleCell.setCellValue("Writer");
							sysInfoValueCell.setCellValue(userName);
							break;
						case 3 :
							sysInfoTitleCell.setCellValue("Date");
							sysInfoValueCell.setCellValue(DateTimeUtil.getDate(dateFormat));
							break;
						default :
							sysInfoTitleCell.setCellValue("");
							sysInfoValueCell.setCellValue("");
						}

						sysInfoTitleCell.setCellStyle(comStyle.get("system"));
						sysInfoValueCell.setCellStyle(comStyle.get("system_value"));

						for (int j = 2, n = titles.size(); j < n; j++) {
							Cell emptyCell = sysInfoRow.createCell(j);
							emptyCell.setCellValue("");
							emptyCell.setCellStyle(comStyle.get("system_value"));
						}

						sheet.addMergedRegion(new CellRangeAddress(i, i, 1, titles.size()-1));
					}

					rownum++;

					Row titleRow = sheet.createRow(rownum);
					titleRow.setHeightInPoints(29.25f);
					Cell titleCell = titleRow.createCell(0);
					titleCell.setCellValue(sheetTitle);
					titleCell.setCellStyle(comStyle.get("title"));
					sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, titles.size()-1));

					rownum = rownum+2;
				}
			}

			Row headerRow = sheet.createRow(rownum);
			headerRow.setHeight((short) 300);
			headerRow.setHeightInPoints(15.00f);

			boolean isTotalStyleType = false;
			boolean wrapText = !staticProfile.equals("ups1");
			CreationHelper factory = null;
			Drawing<?> drawing = null;
			ClientAnchor anchor = null;
			if(memos != null && !memos.isEmpty()) {
				factory = wb.getCreationHelper();
				drawing = sheet.createDrawingPatriarch();
				anchor = factory.createClientAnchor();
			}

			for (int i = 0, n = titles.size(); i < n; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(Util.null2str(titles.get(i)).replaceAll("<br/>", " ").replaceAll("<br>", " ").replaceAll("<br />", " "));

				if(memos != null && memos.size() > i) {
					String memo = Util.null2str(memos.get(i));
					if(!memo.equals("") && anchor != null) {
						anchor.setCol1(cell.getColumnIndex());
						anchor.setCol2(cell.getColumnIndex()+2);
						anchor.setRow1(headerRow.getRowNum());
						anchor.setRow2(headerRow.getRowNum()+12);
						Comment comment = drawing.createCellComment(anchor);
						RichTextString memoStr = factory.createRichTextString(memo);
						comment.setString(memoStr);
						cell.setCellComment(comment);
					}
				}

				if((i+1) == n) {
					cell.setCellStyle(comStyle.get("header_right"));
				} else if(i == 0) {
					cell.setCellStyle(comStyle.get("header_left"));
				} else {
					cell.setCellStyle(comStyle.get("header"));
				}
				if(!Util.null2str(columns.get(i)).equals("num") && ("number".equals(Util.null2str(excelTypes.get(i), "left")) || "double".equals(Util.null2str(excelTypes.get(i), "left")))) {
					isTotalStyleType = true;
				}
			}

			if(frontFileName.equals("Daily performance") || frontFileName.equals("CollectPaymentAccount_List") || frontFileName.equals("CollectPaymentBalance_List")
					|| frontFileName.equals("agent_charge_list") || frontFileName.equals("Duplicate_House_List") ){
				isTotalStyleType = false;
			}

			if(!frontFileName.equals("agent_charge_list") && !frontFileName.equals("Correction_Upload_List") && !frontFileName.equals("Ups_Manifest_List") && !frontFileName.equals("xray판독")) {
				sheet.createFreezePane(0, 7);
			}
			if(frontFileName.equals("Manifest_List")) {
				sheet.createFreezePane(0, 6);
			}

			int titleSize = titles.size();
			int totalAddRow = isTotalStyleType ? 1 : 0;
			int dataStartExcelRow = rownum + 2;
			sheet.setDefaultRowHeight((short) 300);
			Object firstRowData = dataRows.isEmpty() ? null : dataRows.get(0);
			boolean mapRowData = firstRowData instanceof Map;
			boolean upsManifest = frontFileName.equals("Ups_Manifest_List");
			boolean collectPaymentAccount = frontFileName.equals("CollectPaymentAccount_List");
			Method[] getterMethods = buildGetterMethods(dataRows, columns);
			String[] columnNames = new String[titleSize];
			boolean[] numberColumns = new boolean[titleSize];
			boolean[] doubleColumns = new boolean[titleSize];
			boolean[] dateColumns = new boolean[titleSize];
			boolean[] percentColumns = new boolean[titleSize];
			CellStyle[] rowStyles = new CellStyle[titleSize];
			CellStyle[] totalStyles = new CellStyle[titleSize];
			SimpleDateFormat fastDateFormatter = new SimpleDateFormat(dateFormat);

			for (int j = 0; j < titleSize; j++) {
				String columnName = Util.null2str(columns.get(j));
				String styleType = Util.null2str(excelTypes.get(j), "left");
				columnNames[j] = columnName;
				numberColumns[j] = "number".equals(styleType);
				doubleColumns[j] = "double".equals(styleType);
				dateColumns[j] = "date".equals(styleType);
				percentColumns[j] = "percent".equals(styleType);

				Map<String, CellStyle> styles = styleList.get(j);
				String rowStyleName = resolveStyleName(styleType, j, titleSize, false);
				CellStyle rowStyle = styles.get(rowStyleName);
				if (rowStyle == null) {
					ExcelStyles.createStyles(wb, styles, rowStyleName, headerFont, titleFont, font4, df, wrapText);
					rowStyle = styles.get(rowStyleName);
					applyNumberFormat(styleType, rowStyle, df, parseNumFormat(numFormats, j), columnName, firstRowData, getterMethods[j], mapRowData);
				}
				rowStyles[j] = rowStyle;

				String totalStyleType = "num".equals(columnName) ? "center" : styleType;
				String totalStyleName = resolveStyleName(totalStyleType, j, titleSize, true);
				CellStyle totalStyle = styles.get(totalStyleName);
				if (totalStyle == null) {
					ExcelStyles.createStyles(wb, styles, totalStyleName, headerFont, titleFont, font4, df, wrapText);
					totalStyle = styles.get(totalStyleName);
					applyNumberFormat(totalStyleType, totalStyle, df, parseNumFormat(numFormats, j), columnName, firstRowData, getterMethods[j], mapRowData);
				}
				totalStyles[j] = totalStyle;
				sheet.setDefaultColumnStyle(j, rowStyle);
			}

			rownum++;
			for (int i = 0, n = dataRows.size() + totalAddRow; i < n; i++, rownum++) {
				Row row = sheet.createRow(rownum);
				boolean totalRow = isTotalStyleType && (i + 1) == n;
				Object rowData = totalRow ? null : dataRows.get(i);

				for(int j = 0; j < titleSize; j++) {
					Cell cell = row.createCell(j);
					cell.setCellStyle(totalRow ? totalStyles[j] : rowStyles[j]);

					String columnName = columnNames[j];
					if(totalRow) {
						if("num".equals(columnName)) {
							cell.setCellValue("TOTAL");
						} else if(numberColumns[j] || doubleColumns[j]) {
							cell.setCellFormula("SUM("
									+ CellReference.convertNumToColString(j) + dataStartExcelRow
									+ ":"
									+ CellReference.convertNumToColString(j) + row.getRowNum()
									+ ")");
						}
						continue;
					}

					Object value = "num".equals(columnName) ? i + 1 : extractValue(rowData, columnName, getterMethods[j], mapRowData);
					if(upsManifest && Util.null2str(value).equals("")) {
						continue;
					}

					if(numberColumns[j]) {
						if(collectPaymentAccount) {
							cell.setCellValue((Util.null2int(value) / 4) + 1);
						}else {
							cell.setCellValue(Util.null2int(value));
						}
					}else if(doubleColumns[j]) {
						cell.setCellValue(Util.null2dbl(value));
					}else if(dateColumns[j]) {
						writeDateCellFast(cell, value, fastDateFormatter);
					}else if(percentColumns[j]) {
						cell.setCellValue(Util.null2dbl(value));
					}else {
						cell.setCellValue(Util.null2str(value));
					}
				}

				if(sheet instanceof SXSSFSheet && shouldManualFlush(excelType, i + 1, dataRows.size())) {
					((SXSSFSheet) sheet).flushRows(FAST_EXCEL_FLUSH_RETAINED_ROWS);
				}
			}

			if(frontFileName.equals("CollectPaymentAccount_List")) {
				int listTotal = dataRows.size()/4;
				for (int k = 1; k <= listTotal; k++) {
					sheet.addMergedRegion(new CellRangeAddress( k*4 + 3, k*4 + 6, 0,0));
					if(k == listTotal) {
						sheet.addMergedRegion(new CellRangeAddress( k*4 + 3, k*4 + 6, 1,4));
					}else {
						sheet.addMergedRegion(new CellRangeAddress( k*4 + 3, k*4 + 6, 1,1));
						sheet.addMergedRegion(new CellRangeAddress( k*4 + 3, k*4 + 6, 2,2));
						sheet.addMergedRegion(new CellRangeAddress( k*4 + 3, k*4 + 6, 3,3));
						sheet.addMergedRegion(new CellRangeAddress( k*4 + 3, k*4 + 6, 4,4));
					}
				}
			}else if(frontFileName.equals("CollectPaymentBalance_List")) {
				int solutionTotal = dataRows.size()/11;
				for (int p = 1; p <= solutionTotal; p++) {
					sheet.addMergedRegion(new CellRangeAddress(p*11 - 4, p*11 + 6, 0,0));
					sheet.addMergedRegion(new CellRangeAddress(p*11 - 4, p*11 - 1, 1,1));
					sheet.addMergedRegion(new CellRangeAddress(p*11, p*11 + 3, 1,1));
					sheet.addMergedRegion(new CellRangeAddress(p*11 + 4, p*11 + 6, 1,1));
				}
			}

			if(columnsWidth != null) {
				for(int i = 0; i < titleSize && i < columnsWidth.size(); i++) {
					int columnsSize = Util.null2int(columnsWidth.get(i)) * 36;
					sheet.setColumnWidth(i, Math.min(columnsSize, 65280));
				}
			}

			if(Util.null2str(searchMap.get("saveFile")).equals("")) {
				String currDtm = DateTimeUtil.getDate("yyyyMMddHHmmss");

				String saveFile = frontFileName + "_" + currDtm + ".xls";
				if(Common.checkSaveFilenameForExcel(frontFileName)) {
					saveFile = UUID.randomUUID().toString() + "_" + currDtm + ".xls";
				}
				String originFile = frontFileName + "_" + currDtm.substring(0,4) + "-" + currDtm.substring(4,6) + "-" + currDtm.substring(6,8) + ".xls";

				if("xlsx".equals(Util.null2str(excelType))) {
					saveFile += "x";
					originFile += "x";
				}

				FileUtil.fileWrite(wb, excelDir, saveFile);

				files[0] = saveFile;
				files[1] = originFile;
			}else{
				String logVal = searchMap.get("saveFile").toString() +  "  " + searchMap.get("originFile").toString();
				log.debug(logVal);
				FileUtil.fileWrite(wb, "", searchMap.get("saveFile").toString());

				files[0] = searchMap.get("saveFile").toString();
				files[1] = searchMap.get("originFile").toString();
			}

			return files;
		} finally {
			if (file != null) {
				file.close();
			}
			if (wb instanceof SXSSFWorkbook) {
				((SXSSFWorkbook) wb).dispose();
			}
			if (wb != null) {
				wb.close();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public String[] makeExcelNoStyleNoHeader(
			String excelType, Map<String, Object> searchMap, List<String> columns, List<String> excelTypes,
			List<Integer> numFormats, List<String> titles, String sheetTitle, String frontFileName,
			String userName, String dateFormat) throws Exception {

		String excelDir = staticCustomProperties.getExcelDownload();
		String[] files = new String[2];
		FileInputStream file = null;
		List<Object> dataRows = (List<Object>) searchMap.get("data");
		if (dataRows == null) {
			dataRows = new ArrayList<Object>();
		}
		Workbook wb;
		Sheet sheet;
		if(!Util.null2str(searchMap.get("saveFile")).equals("") ){
			file = new FileInputStream(searchMap.get("saveFile").toString());
			if("xls".equals(Util.null2str(excelType))) {
				wb = new HSSFWorkbook(file);
			}else {
				wb = new SXSSFWorkbook(new XSSFWorkbook(file), FAST_EXCEL_ROW_WINDOW_SIZE);
			}
			sheet = wb.createSheet(sheetTitle.replaceAll("/", "") +"("+searchMap.get("sheetNo").toString()+")");
			sheet = wb.getSheetAt(Integer.parseInt(searchMap.get("sheetNo").toString()));
		}else{
			if("xls".equals(Util.null2str(excelType))) {
				wb = new HSSFWorkbook();
			}else {
				wb = new SXSSFWorkbook(FAST_EXCEL_ROW_WINDOW_SIZE);
			}
			sheet = wb.createSheet(sheetTitle.replaceAll("/", ""));
		}

		PrintSetup printSetup = sheet.getPrintSetup();
		setPaperSize(printSetup, PrintSetup.A4_PAPERSIZE, true);
		sheet.setAutobreaks(true);

		int rownum = 0;
		int titleSize = titles.size();
		Row headerRow = sheet.createRow(rownum);
		boolean isTotalStyleType = false;
		for (int i = 0; i < titleSize; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(Util.null2str(titles.get(i)).replaceAll("<br/>", " ").replaceAll("<br>", " ").replaceAll("<br />", " "));
			if(!Util.null2str(columns.get(i)).equals("num") && ("number".equals(Util.null2str(excelTypes.get(i), "left")) || "double".equals(Util.null2str(excelTypes.get(i), "left")))) {
				isTotalStyleType = true;
			}
		}

		if(frontFileName.equals("Daily performance") || frontFileName.equals("CollectPaymentAccount_List") || frontFileName.equals("CollectPaymentBalance_List")
				|| frontFileName.equals("agent_charge_list") || frontFileName.equals("Duplicate_House_List") ){
			isTotalStyleType = false;
		}

		rownum++;
		int totalAddRow = isTotalStyleType ? 1 : 0;
		int firstDataRowIndex = rownum;
		int dataStartExcelRow = firstDataRowIndex + 1;
		Object firstRowData = dataRows.isEmpty() ? null : dataRows.get(0);
		boolean mapRowData = firstRowData instanceof Map;
		boolean upsManifest = frontFileName.equals("Ups_Manifest_List");
		boolean collectPaymentAccount = frontFileName.equals("CollectPaymentAccount_List");
		Method[] getterMethods = buildGetterMethods(dataRows, columns);
		SimpleDateFormat fastDateFormatter = new SimpleDateFormat(dateFormat);
		String[] columnNames = new String[titleSize];
		boolean[] numberColumns = new boolean[titleSize];
		boolean[] doubleColumns = new boolean[titleSize];
		boolean[] dateColumns = new boolean[titleSize];
		boolean[] percentColumns = new boolean[titleSize];

		for (int j = 0; j < titleSize; j++) {
			String columnName = Util.null2str(columns.get(j));
			String styleType = Util.null2str(excelTypes.get(j), "left");
			columnNames[j] = columnName;
			numberColumns[j] = "number".equals(styleType);
			doubleColumns[j] = "double".equals(styleType);
			dateColumns[j] = "date".equals(styleType);
			percentColumns[j] = "percent".equals(styleType);
		}

		for (int i = 0, n = dataRows.size() + totalAddRow; i < n; i++, rownum++) {
			Row row = sheet.createRow(rownum);
			boolean totalRow = isTotalStyleType && (i + 1) == n;
			Object rowData = totalRow ? null : dataRows.get(i);

			for(int j = 0; j < titleSize; j++) {
				Cell cell = row.createCell(j);
				String columnName = columnNames[j];

				if(totalRow) {
					if("num".equals(columnName)) {
						cell.setCellValue("TOTAL");
					} else if(numberColumns[j] || doubleColumns[j]) {
						cell.setCellFormula("SUM("
								+ CellReference.convertNumToColString(j) + dataStartExcelRow
								+ ":"
								+ CellReference.convertNumToColString(j) + row.getRowNum()
								+ ")");
					}
					continue;
				}

				Object value = "num".equals(columnName) ? i + 1 : extractValue(rowData, columnName, getterMethods[j], mapRowData);
				if(upsManifest && Util.null2str(value).equals("")) {
					continue;
				}

				if(numberColumns[j]) {
					if(collectPaymentAccount) {
						cell.setCellValue((Util.null2int(value) / 4) + 1);
					}else {
						cell.setCellValue(Util.null2int(value));
					}
				}else if(doubleColumns[j]) {
					cell.setCellValue(Util.null2dbl(value));
				}else if(dateColumns[j]) {
					writeDateCellFast(cell, value, fastDateFormatter);
				}else if(percentColumns[j]) {
					cell.setCellValue(Util.null2dbl(value));
				}else {
					cell.setCellValue(Util.null2str(value));
				}
			}

			if(shouldManualFlush(excelType, i + 1, dataRows.size())) {
				((SXSSFSheet) sheet).flushRows(FAST_EXCEL_FLUSH_RETAINED_ROWS);
			}
		}

		if(frontFileName.equals("CollectPaymentAccount_List")) {
			int listTotal = dataRows.size()/4;
			for (int k = 1; k <= listTotal; k++) {
				int startRow = firstDataRowIndex + ((k - 1) * 4);
				int endRow = startRow + 3;
				sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 0,0));
				if(k == listTotal) {
					sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 1,4));
				}else {
					sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 1,1));
					sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 2,2));
					sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 3,3));
					sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 4,4));
				}
			}
		}else if(frontFileName.equals("CollectPaymentBalance_List")) {
			int solutionTotal = dataRows.size()/11;
			for (int p = 1; p <= solutionTotal; p++) {
				int startRow = firstDataRowIndex + ((p - 1) * 11);
				int endRow = startRow + 10;
				sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 0,0));
				sheet.addMergedRegion(new CellRangeAddress(startRow, startRow + 3, 1,1));
				sheet.addMergedRegion(new CellRangeAddress(startRow + 4, startRow + 7, 1,1));
				sheet.addMergedRegion(new CellRangeAddress(startRow + 8, endRow, 1,1));
			}
		}
		List<?> columnsWidth = (List<?>) searchMap.get("columnsWidth");
		if(columnsWidth != null) {
			for(int i = 0; i < titleSize && i < columnsWidth.size(); i++) {
				int columnsSize = Util.null2int(columnsWidth.get(i)) * 36;
				sheet.setColumnWidth(i, Math.min(columnsSize, 65280));
			}
		}

		if(Util.null2str(searchMap.get("saveFile")).equals("") ){
			String currDtm = DateTimeUtil.getDate("yyyyMMddHHmmss");
			String saveFile = frontFileName + "_" + currDtm + ".xls";
			if(Common.checkSaveFilenameForExcel(frontFileName)) {
				saveFile = UUID.randomUUID().toString() + "_" + currDtm + ".xls";
			}
			String originFile = frontFileName + "_" + currDtm.substring(0,4) + "-" + currDtm.substring(4,6) + "-" + currDtm.substring(6,8) + ".xls";

			if("xlsx".equals(Util.null2str(excelType))) {
				saveFile += "x";
				originFile += "x";
			}

			FileUtil.fileWrite(wb, excelDir, saveFile);
			files[0] = saveFile;
			files[1] = originFile;
		}else{
			FileUtil.fileWrite(wb, "", searchMap.get("saveFile").toString());
			files[0] = searchMap.get("saveFile").toString();
			files[1] = searchMap.get("originFile").toString();
			file.close();
		}

		if (wb instanceof SXSSFWorkbook) {
			((SXSSFWorkbook) wb).dispose();
		}
		return files;
	}

	private Method[] buildGetterMethods(List<Object> dataRows, List<String> columns) throws IntrospectionException {
		Method[] getterMethods = new Method[columns.size()];
		if(dataRows == null || dataRows.isEmpty()) {
			return getterMethods;
		}

		Object firstRow = dataRows.get(0);
		if(firstRow instanceof Map) {
			return getterMethods;
		}

		Map<String, Method> getterCache = new HashMap<String, Method>();
		for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(firstRow.getClass()).getPropertyDescriptors()) {
			Method readMethod = propertyDescriptor.getReadMethod();
			if (readMethod != null) {
				getterCache.put(propertyDescriptor.getName(), readMethod);
			}
		}

		for (int i = 0; i < columns.size(); i++) {
			getterMethods[i] = getterCache.get(Util.null2str(columns.get(i)));
		}
		return getterMethods;
	}

	private Object extractValue(Object rowData, String columnName, Method getterMethod, boolean mapRowData) throws Exception {
		if (rowData == null) {
			return null;
		}
		if (mapRowData) {
			return ((Map<?, ?>) rowData).get(columnName);
		}
		return getterMethod == null ? null : getterMethod.invoke(rowData);
	}

	private int parseNumFormat(List<?> numFormats, int index) {
		if (numFormats == null || numFormats.size() <= index) {
			return 0;
		}
		return Util.null2int(numFormats.get(index));
	}

	private void applyNumberFormat(String styleType, CellStyle style, DataFormat dataFormat, int numFormat, String columnName,
			Object rowData, Method getterMethod, boolean mapRowData) throws Exception {
		if ("percent".equals(styleType)) {
			style.setDataFormat(dataFormat.getFormat("0.00%"));
			return;
		}
		if (!"number".equals(styleType) && !"double".equals(styleType)) {
			return;
		}

		String format = "#,##0";
		if (numFormat > 0) {
			StringBuilder builder = new StringBuilder(format).append('.');
			for (int i = 0; i < numFormat; i++) {
				builder.append('0');
			}
			format = builder.toString();
		} else if (numFormat != 0 && rowData != null) {
			Object value = extractValue(rowData, columnName, getterMethod, mapRowData);
			String[] digits = Util.null2str(value).split("\\.");
			if (digits.length > 1) {
				StringBuilder builder = new StringBuilder(format).append('.');
				for (int i = 0; i < digits[1].length(); i++) {
					builder.append('0');
				}
				format = builder.toString();
			}
		}
		style.setDataFormat(dataFormat.getFormat(format));
	}

	private String resolveStyleName(String styleType, int columnIndex, int titleSize, boolean totalRow) {
		boolean firstColumn = columnIndex == 0;
		boolean lastColumn = (columnIndex + 1) == titleSize;

		switch (styleType) {
			case "center":
				if (totalRow) {
					return firstColumn ? "cell_normal_centered_left_bottom"
							: lastColumn ? "cell_normal_centered_right_bottom" : "cell_normal_centered_bottom";
				}
				return firstColumn ? "cell_normal_centered_left"
						: lastColumn ? "cell_normal_centered_right" : "cell_normal_centered";
			case "right":
				if (totalRow) {
					return firstColumn ? "cell_right_left_bottom"
							: lastColumn ? "cell_right_right_bottom" : "cell_right_bottom";
				}
				return firstColumn ? "cell_right_left"
						: lastColumn ? "cell_right_right" : "cell_right";
			case "number":
				if (totalRow) {
					return firstColumn ? "cell_normal_number_left_bottom"
							: lastColumn ? "cell_normal_number_right_bottom" : "cell_normal_number_bottom";
				}
				return firstColumn ? "cell_normal_number_left"
						: lastColumn ? "cell_normal_number_right" : "cell_normal_number";
			case "double":
				if (totalRow) {
					return firstColumn ? "cell_normal_double_left_bottom"
							: lastColumn ? "cell_normal_double_right_bottom" : "cell_normal_double_bottom";
				}
				return firstColumn ? "cell_normal_double_left"
						: lastColumn ? "cell_normal_double_right" : "cell_normal_double";
			case "date":
				if (totalRow) {
					return firstColumn ? "cell_normal_date_left_bottom"
							: lastColumn ? "cell_normal_date_right_bottom" : "cell_normal_date_bottom";
				}
				return firstColumn ? "cell_normal_date_left"
						: lastColumn ? "cell_normal_date_right" : "cell_normal_date";
			default:
				if (totalRow) {
					return firstColumn ? "cell_normal_left_bottom"
							: lastColumn ? "cell_normal_right_bottom" : "cell_normal_bottom";
				}
				return firstColumn ? "cell_normal_left"
						: lastColumn ? "cell_normal_right" : "cell_normal";
		}
	}

	private void writeDateCellFast(Cell cell, Object value, String dateFormat) {
		try {
			long time = value instanceof Number ? ((Number) value).longValue() : Long.parseLong(Util.null2str(value));
			cell.setCellValue(DateTimeUtil.ConvertDate2String(new Date(time), new SimpleDateFormat(dateFormat)));
		}catch(Exception e){
			cell.setCellValue("");
		}
	}

	private void writeDateCellFast(Cell cell, Object value, SimpleDateFormat dateFormatter) {
		try {
			long time = value instanceof Number ? ((Number) value).longValue() : Long.parseLong(Util.null2str(value));
			cell.setCellValue(DateTimeUtil.ConvertDate2String(new Date(time), dateFormatter));
		}catch(Exception e){
			cell.setCellValue("");
		}
	}

	private boolean shouldManualFlush(String excelType, int writtenRows, int totalRows) {
		return "xlsx".equals(Util.null2str(excelType))
				&& totalRows > FAST_EXCEL_FLUSH_INTERVAL
				&& writtenRows % FAST_EXCEL_FLUSH_INTERVAL == 0;
	}

	/**
	 * 엑셀 파일 생성
	 * @param excelType
	 * @param dataListObj
	 * @param columns
	 * @param titles
	 * @param sheetTitle
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String[] makeDeliveryExcel(
			String excelType, Map<String, Object> searchMap, List<String> columns, List<String> excelTypes, 
			List<Integer> numFormats, List<String> titles, String sheetTitle, String frontFileName, 
			String userName, String dateFormat, boolean isSaveCsv) throws Exception {
		
		String excelDir = staticCustomProperties.getExcelDownload();
		
		// Return값 [0]: 실제 파일 저장경로, [1]: 다운로드받을 파일명
		String[] files = new String[2];
		FileInputStream file= null;
		Workbook wb;
		Sheet sheet;
		if(!Util.null2str(searchMap.get("saveFile")).equals("") ){  // 엑셀파일이 있는경우 다음 sheet 생성하여 데이터 적재 
			file = new FileInputStream(searchMap.get("saveFile").toString()); 
			
			if("xls".equals(Util.null2str(excelType))) 	wb = new HSSFWorkbook(file);
			else 										wb = new XSSFWorkbook(file);
			
			sheet = wb.createSheet(sheetTitle.replaceAll("/", "") +"("+searchMap.get("sheetNo").toString()+")");
			sheet = wb.getSheetAt(Integer.parseInt(searchMap.get("sheetNo").toString())); // 시트정보읽어온다
		}
		else{ 
			if("xls".equals(Util.null2str(excelType))) 	wb = new HSSFWorkbook();
			else 										wb = new XSSFWorkbook();
			
			sheet = wb.createSheet(sheetTitle.replaceAll("/", "")); // 시트생성
		}
		
		DataFormat df = wb.createDataFormat();		
		
		
		Font headerFont = wb.createFont();
		headerFont.setBold(true);
		headerFont.setFontName("Arial");
		headerFont.setFontHeightInPoints((short)10);
		headerFont.setColor(IndexedColors.WHITE.getIndex());
		Font titleFont = wb.createFont();
		titleFont.setBold(true);
		titleFont.setUnderline(HSSFFont.U_SINGLE); 
		titleFont.setFontName("Arial");
		titleFont.setFontHeightInPoints((short)16);
		Font font4 = wb.createFont();
		font4.setFontName("Arial");
		font4.setFontHeightInPoints((short)10);
		
		
		Map<String, CellStyle> comStyle = new HashMap<String, CellStyle>();
		String[] styleTitleArr = {"system","system_value","title","header_right","header_left","header"};
		for(String styleTitle : styleTitleArr){
			ExcelStyles.createStyles(wb, comStyle, styleTitle, headerFont, titleFont, font4, df);
		}
		
		List<Map<String, CellStyle>> styleList = new ArrayList<Map<String, CellStyle>>();
		
		for(int i = 0; i < titles.size(); i++) {
			Map<String, CellStyle> style = new HashMap<String, CellStyle>();
			styleList.add(style);
		}
		
		PrintSetup printSetup = sheet.getPrintSetup();
		setPaperSize(printSetup, PrintSetup.A4_PAPERSIZE, true);
		sheet.setAutobreaks(true); // Option Button Move
		
		boolean headerYn = true;
		//요율관리에서는 해더안나오게 표현하기 위함
		if("agent_charge_list".equals(frontFileName) || "Correction_Upload_List".equals(frontFileName)) {
			headerYn = false;
		}
		
		int rownum = 0;

		//the header row: centered text
		Row headerRow = sheet.createRow(rownum);
		headerRow.setHeight((short) 300);
		headerRow.setHeightInPoints(15.00f);
		
		Cell cell;
		boolean isTotalStyleType = false; // 합계 표현용 ROW 추가 여부
		boolean wrapText = true; // 자동줄바꿈 여부
		CreationHelper factory = wb.getCreationHelper();
		Drawing<?> drawing = sheet.createDrawingPatriarch();
		// When the comment box is visible, have it show in a 1x3 space
		ClientAnchor anchor = factory.createClientAnchor();
		Comment comment;
		RichTextString memoStr;
		
		//해더셋팅
		List<Map<String, Object>> memosList = (List<Map<String, Object>>)searchMap.get("memos");
		
		
		for (int i = 0, n = titles.size(); i < n; i++) {
			cell = headerRow.createCell(i);
			cell.setCellValue(Util.null2str(titles.get(i)).replaceAll("<br/>", " ").replaceAll("<br>", " ").replaceAll("<br />", " "));
			
			if(memosList !=null && memosList.size() > i) {
				String memo = Util.null2str(memosList.get(i));
				//해더에 메모추가 시작
				if(!memo.equals("")){
					anchor.setCol1(cell.getColumnIndex());
					anchor.setCol2(cell.getColumnIndex()+2);
					anchor.setRow1(headerRow.getRowNum());
					anchor.setRow2(headerRow.getRowNum()+12);
					comment = drawing.createCellComment(anchor);    				
					memoStr = factory.createRichTextString(memo);
					comment.setString(memoStr);
					cell.setCellComment(comment);
				}     
				//해더에 메모추가 끝
			}            
		}
		
		if(frontFileName.equals("Daily performance") || frontFileName.equals("CollectPaymentAccount_List") || frontFileName.equals("CollectPaymentBalance_List") 
				|| frontFileName.equals("agent_charge_list") || frontFileName.equals("Duplicate_House_List") ){
			isTotalStyleType = false;
		}
		
		// UPS는 자동줄바꿈 적용하지 않음
		if(staticProfile.equals("ups1")) {
			wrapText = false;
		}
		
		Row row;
		rownum++;
		List<Object> datas = (List<Object>)searchMap.get("data");
		
		Map<String, CellStyle> styles;
		String styleType = "";
		String styleName = "";
		int numFormat = 0;
		int titleSize = titles.size();
		int totalAddRow = isTotalStyleType ? 1 : 0;
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> data = new HashMap<String, Object>();
		for (int i = 0, n = datas.size() + totalAddRow; i < n; i++, rownum++) {
			row = sheet.createRow(rownum);
			row.setHeight((short) 300);
			
			if(isTotalStyleType && (i+1) == n){
				// 합계 표현용 마지막 ROW 추가
				data.clear();
				data.put("num", "TOTAL");
			}else{
				data = (Map<String, Object>)objectMapper.convertValue(datas.get(i), Map.class);
			}
			
			String columnName = "";

			for(int j = 0; j < titleSize; j++) {
				cell = row.createCell(j);
				columnName = Util.null2str(columns.get(j));

				// Cell Style Setting
				styles = styleList.get(j);
				styleType = Util.null2str(excelTypes.get(j), "left");
				numFormat = Util.null2int(numFormats.get(j));
				/** NUM컬럼의 순서를 변경 후 엑셀 다운로드시 오류 발생해서 수정 */
				if(isTotalStyleType && (i+1) == n && "num".equals(columnName)){
					styleType = "center";
				}
				String defaultFormat = "#,##0";
				switch(styleType) {
				case "left" :
					styleName = "cell_normal";
					if(styles.get(styleName) == null){
						ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df, wrapText);
					}
					cell.setCellStyle(styles.get(styleName));
					cell.setCellValue(Util.null2str(data.get(columnName)));
					break;
				case "center" :
					styleName = "cell_normal_centered";
					if(styles.get(styleName) == null){
						ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df, wrapText);
					}
					cell.setCellStyle(styles.get(styleName));
					cell.setCellValue(Util.null2str(data.get(columnName)));
					
					break;
				case "right" :
					styleName = "cell_right";
					if(styles.get(styleName) == null){
						ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df, wrapText);
					}
					cell.setCellStyle(styles.get(styleName));
					cell.setCellValue(Util.null2str(data.get(columnName)));
					
					break;
				case "number" :
					styleName = "cell_normal_number";
					if(styles.get(styleName) == null){
						ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df, wrapText);
						
						if(numFormat > 0) {
							defaultFormat += ".";
							for(int k = 0; k < numFormat; k++) {
								defaultFormat += "0";
							}
						}else if(numFormat != 0){
							String[] digits = Util.null2str(data.get(columnName)).split("\\.");
							if(digits.length > 1) {
								defaultFormat += ".";
								for(int k = 0; k < digits[1].length(); k++) {
									defaultFormat += "0";
								}        						
							}
						}
						styles.get(styleName).setDataFormat(df.getFormat(defaultFormat));
					}
					cell.setCellStyle(styles.get(styleName));
					
					// 경영정보 > collect payment 엑셀 셀병합 처리 2020.06.08 sophie
					if( frontFileName.equals("CollectPaymentAccount_List") ) {
						cell.setCellValue((Util.null2int(data.get(columnName)) / 4) + 1);
						
					}else {
						cell.setCellValue(Util.null2int(data.get(columnName)));
					}
					
					// NUMBER 마지막 ROW 합계 수식 추가
					if(isTotalStyleType && (i+1) == n) {
						//cell.setCellType(CellType.FORMULA);
						cell.setCellFormula("SUM(" 
								+ CellReference.convertNumToColString(cell.getColumnIndex()).concat(Integer.toString(8))  
								+ ":"
								+ CellReference.convertNumToColString(cell.getColumnIndex()).concat(Integer.toString((cell.getRow().getRowNum()))) 
								+ ")");
					}
					
					break;
				case "double" :
					styleName = "cell_normal_double";
					if(styles.get(styleName) == null){
						ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df, wrapText);
						
						if(numFormat > 0) {
							defaultFormat += ".";
							for(int k = 0; k < numFormat; k++) {
								defaultFormat += "0";
							}
						}else if(numFormat != 0){
							String[] digits = Util.null2str(data.get(columnName)).split("\\.");
							if(digits.length > 1) {
								defaultFormat += ".";
								for(int k = 0; k < digits[1].length(); k++) {
									defaultFormat += "0";
								}        						
							}
						}
						styles.get(styleName).setDataFormat(df.getFormat(defaultFormat));
					}
					cell.setCellStyle(styleList.get(j).get(styleName));
					cell.setCellValue(Util.null2dbl(data.get(columnName)));
					
					// DOUBLE 마지막 ROW 합계 수식 추가
					if(isTotalStyleType && (i+1) == n){
						//cell.setCellType(CellType.FORMULA);
						cell.setCellFormula("SUM(" 
								+ CellReference.convertNumToColString(cell.getColumnIndex()).concat(Integer.toString(8))  
								+ ":"
								+ CellReference.convertNumToColString(cell.getColumnIndex()).concat(Integer.toString((cell.getRow().getRowNum()))) 
								+ ")");
					}
					
					break;
				case "date" :
					styleName = "cell_normal_date";
					if(styles.get(styleName) == null){
						ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df, wrapText);
					}
					cell.setCellStyle(styles.get(styleName));
					try{
						OffsetDateTime odt = OffsetDateTime.parse(String.valueOf(data.get(columnName)));
				        String scanDate = odt.format(DateTimeFormatter.ofPattern(dateFormat));
						cell.setCellValue(scanDate);
					}catch(Exception e){
						cell.setCellValue("");
					}
					
					break;
				case "percent" :
					styleName = "cell_normal";
					try{
						if(styles.get(styleName) == null){
							ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df, wrapText);
						}
						styles.get(styleName).setDataFormat(df.getFormat("0.00%"));
						cell.setCellStyle(styles.get(styleName));
						cell.setCellValue(Util.null2dbl(data.get(columnName)));
					}catch(Exception e){
						cell.setCellValue("");
					}
					
					break;
				default :
					styleName = "cell_normal";
					if(styles.get(styleName) == null){
						ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df, wrapText);
					}
					cell.setCellStyle(styles.get(styleName));
					cell.setCellValue(Util.null2str(data.get(columnName)));
				}
			}
		}
		
		List<Integer> columnsWidth = (List<Integer>)searchMap.get("columnsWidth");
		int columnsSize;
		for(int i = 0; i < titleSize; i++) {
			columnsSize = Util.null2int(columnsWidth.get(i)) * 36;
			if(columnsSize > 65280){
				sheet.setColumnWidth(i, 65280);
			}else{
				sheet.setColumnWidth(i, columnsSize);
			}
		}

		String currDtm = DateTimeUtil.getDate("yyyyMMddHHmmss");
		String excelTryCnt = String.valueOf(searchMap.get("excelTryCnt"));

		if ( isSaveCsv && Util.null2str(searchMap.get("saveFile")).equals("")) { // CSV 저장
		    String saveFile = frontFileName + excelTryCnt == "0" ? "_" : ("_" + searchMap.get("excelTryCnt") + "_") + currDtm + ".csv";	// 임시파일명
		    String originFile = frontFileName + "_" + currDtm.substring(0,4) + "-" + currDtm.substring(4,6) + "-" + currDtm.substring(6,8) + ".csv";

		    // UTF-8 + BOM 인코딩으로 CSV 저장
		    String csvPath = excelDir + File.separator + saveFile;
		    try (BufferedWriter bw = new BufferedWriter(
		            new OutputStreamWriter(new FileOutputStream(csvPath), StandardCharsets.UTF_8))) {

		        bw.write("\uFEFF");
		        
		        if(!"cjDelivery".equals(sheetTitle)) {	// CJ택배일경우 타이틀 없이.
		        for (int i = 0; i < titles.size(); i++) {
		            bw.write("\"" + titles.get(i).replaceAll("\"", "\"\"") + "\"");
		            if (i < titles.size() - 1) bw.write(",");
		        }
		        bw.newLine();
		        }
		        for (Object obj : datas) {
		            Map<String, Object> dataObj = objectMapper.convertValue(obj, Map.class);
		            for (int i = 0; i < columns.size(); i++) {
		                Object val = dataObj.get(columns.get(i));
		                String strVal = val == null ? "" : val.toString();

		                if (excelTypes.size() > i && "date".equals(excelTypes.get(i))) {
		                    try {
		                        OffsetDateTime odt = OffsetDateTime.parse(strVal);
		                        strVal = odt.format(DateTimeFormatter.ofPattern(dateFormat));
		                    } catch (Exception e) {
		                    }
		                }

		                // CSV 특수문자 처리
		                strVal = strVal.replace("\"", "\"\"");
		                if (strVal.contains(",") || strVal.contains("\"") || strVal.contains("\n")) {
		                    strVal = "\"" + strVal + "\"";
		                }

		                bw.write(strVal);
		                if (i < columns.size() - 1) bw.write(",");
		            }
		            bw.newLine();
		        }
		    }

		    // 반환값 설정
		    files[0] = saveFile;
		    files[1] = originFile;
		} else if( !isSaveCsv && Util.null2str(searchMap.get("saveFile")).equals("") ) { // Excel 저장
			// Write the output to a file
			String saveFile = frontFileName + excelTryCnt == "0" ? "_" : ("_" + searchMap.get("excelTryCnt") + "_") + currDtm + ".xls";	// 임시파일명
			if(Common.checkSaveFilenameForExcel(frontFileName)) {
				String uuid = UUID.randomUUID().toString();
				saveFile = uuid + "_" + currDtm + "xls";
			}
			String originFile = frontFileName + "_" + currDtm.substring(0,4) + "-" + currDtm.substring(4,6) + "-" + currDtm.substring(6,8) + ".xls";	// 원본파일명
			
			if(wb instanceof XSSFWorkbook) {
				saveFile += "x";
				originFile += "x";
			}
			
			//workbook파일 출력
			FileUtil.fileWrite(wb, excelDir, saveFile);

			// Return값 [0]: 저장된 파일명, [1]: 다운로드받을 파일명
			files[0] = saveFile;
			files[1] = originFile;
		} else{
			String logVal = searchMap.get("saveFile").toString() +  "  " + searchMap.get("originFile").toString();
			log.debug(logVal);
			FileUtil.fileWrite(wb, "", searchMap.get("saveFile").toString());
			
			files[0] = searchMap.get("saveFile").toString();
			files[1] = searchMap.get("originFile").toString();
			
			file.close();
		}
		
		return files;
	}

	/**
	 * 엑셀 파일 생성
	 * 작업 중 파일
	 */
	public String[] makeExcel(JSONObject setting, ExcelData excelData, JSONObject dataListObj, String userName, String dateFormat) throws Exception {
		String excelDir = customProperties.getExcelDownload();
		
		setting.put("userName", userName);
		setting.put("dateFormat", dateFormat);

		// Return값 [0]: 실제 파일 저장경로, [1]: 다운로드받을 파일명
		String[] files = new String[2];
		Workbook wb;
		Sheet sheet;

		// excelFile
		String saveFile = Util.null2str(dataListObj.get("saveFile"));
		String sheetNo = Util.null2str(dataListObj.get("sheetNo"));

		// setting
		String frontFileName = Util.null2str(setting.get("fileName"));
		String sheetTitle = Util.null2str(setting.get("mainTitle")).replaceAll("/", "");

		if(!"".equals(saveFile)){  // 엑셀파일이 있는경우 다음 sheet 생성하여 데이터 적재
			FileInputStream file = new FileInputStream(saveFile);
			wb = new SXSSFWorkbook(new XSSFWorkbook(file), 100);

			sheet = wb.createSheet(sheetTitle + "(" + sheetNo + ")");
			sheet = wb.getSheetAt(Integer.parseInt(sheetNo));
		}
		else{
			// rowAccessWindowSize: Flush 갯수
			wb = new SXSSFWorkbook(100);

			sheet = wb.createSheet(sheetTitle); // 시트생성
		}

		// header 입력
		sheet = makeHeader(wb, sheet, setting);
		// data list 입력
		wb = makeDataList(wb, sheet, setting, excelData);

		// 엑셀 파일 생성
		if("".equals(saveFile)){ // 엑셀파일 생성
			// Write the output to a file
			String currDtm = DateTimeUtil.getDate("yyyyMMddHHmmss");

			// 임시파일명
			String _saveFile = frontFileName + "_" + currDtm + ".xlsx";
			// 원본파일명
			String _originFile = frontFileName + "_" + currDtm.substring(0,4) + "-" + currDtm.substring(4,6) + "-" + currDtm.substring(6,8) + ".xlsx";

			//workbook파일 출력
			FileUtil.fileWrite(wb, excelDir, _saveFile);

			// Return값 [0]: 실제 파일 저장경로, [1]: 다운로드받을 파일명
			files[0] = excelDir + "/" + _saveFile;
			files[1] = _originFile;
		}
		else{
			log.debug(dataListObj.get("saveFile").toString() +  "  " + dataListObj.get("originFile").toString());
			FileUtil.fileWrite(wb, "", dataListObj.get("saveFile").toString());

			files[0] = dataListObj.get("saveFile").toString();
			files[1] = dataListObj.get("originFile").toString();
		}

		((SXSSFWorkbook) wb).dispose();

		return files;
	}
  
	/**
	 * 엑셀 헤더 입력
	 * @param wb
	 * @param sheet
	 * @param setting
	 * @return
	 * @throws Exception
	 */
	private Sheet makeHeader(Workbook wb, Sheet sheet, JSONObject setting) throws Exception {
		// setting
		String sheetTitle = Util.null2str(setting.get("mainTitle")).replaceAll("/", "");
		String userName = Util.null2str(setting.get("userName"));
		String dateFormat = Util.null2str(setting.get("dateFormat"));
		JSONArray titles = (JSONArray) setting.get("titles");

		//Column별 Style은 동일하므로 Column별 Style을 생성한다.
		Map<String, CellStyle> comStyle = ExcelStyles.createStyles(wb);

		PrintSetup printSetup = sheet.getPrintSetup();
		setPaperSize(printSetup, PrintSetup.A4_PAPERSIZE, true);
		sheet.setAutobreaks(true); // Option Button Move

		for(int i = 0; i < 4; i++) {
			Row sysInfoRow = sheet.createRow(i);
			sysInfoRow.setHeight((short) 400);
			Cell sysInfoTitleCell = sysInfoRow.createCell(0);
			Cell sysInfoValueCell = null;

			switch(i) {
				case 0 :
					sysInfoTitleCell.setCellValue("Program");
					sysInfoValueCell = sysInfoRow.createCell(1);
					sysInfoValueCell.setCellValue("Global One System");
					break;
				case 1 :
					sysInfoTitleCell.setCellValue("System");
					sysInfoValueCell = sysInfoRow.createCell(1);
					sysInfoValueCell.setCellValue(sheetTitle);
					break;
				case 2 :
					sysInfoTitleCell.setCellValue("Writer");
					sysInfoValueCell = sysInfoRow.createCell(1);
					sysInfoValueCell.setCellValue(userName);
					break;
				case 3 :
					sysInfoTitleCell.setCellValue("Date");
					sysInfoValueCell = sysInfoRow.createCell(1);
					sysInfoValueCell.setCellValue(DateTimeUtil.getDate(dateFormat));
					break;
				default :
					sysInfoTitleCell.setCellValue("");
					sysInfoValueCell = sysInfoRow.createCell(1);
					sysInfoValueCell.setCellValue("");
			}

			sysInfoTitleCell.setCellStyle(comStyle.get("system"));
			sysInfoValueCell.setCellStyle(comStyle.get("system_value"));

			for (int j = 2, n = titles.size(); j < n; j++) {
				sysInfoValueCell = sysInfoRow.createCell(j);
				sysInfoValueCell.setCellValue("");
				sysInfoValueCell.setCellStyle(comStyle.get("system_value"));
			}

			sheet.addMergedRegion(new CellRangeAddress(i, i, 1, titles.size()-1));
		}

		return sheet;
	}

	/**
	 * 데이터 리스트 입력
	 * @param wb
	 * @param sheet
	 * @param setting
	 * @return
	 * @throws Exception
	 */
	Workbook makeDataList(Workbook wb, Sheet sheet, JSONObject setting, ExcelData excelData) throws Exception {
		String sheetTitle = Util.null2str(setting.get("mainTitle")).replaceAll("/", "");
		String pageType = Util.null2str(setting.get("pageType"));
		String dateFormat = Util.null2str(setting.get("dateFormat"));
		JSONArray titles = (JSONArray) setting.get("titles");
		JSONArray columns = (JSONArray) setting.get("columns");
		JSONArray excelTypes = (JSONArray) setting.get("excelTypes");
		JSONArray numFormats = (JSONArray) setting.get("numFormats");

		//Column별 Style은 동일하므로 Column별 Style을 생성한다.
		Map<String, CellStyle> comStyle = ExcelStyles.createStyles(wb);
		DataFormat df = wb.createDataFormat();

		List<Map<String, CellStyle>> styleList = new ArrayList<Map<String, CellStyle>>();
		for(int i = 0; i < titles.size(); i++) {
			styleList.add(comStyle);
		}

		Row titleRow = sheet.createRow(4);
		titleRow.setHeightInPoints(29.25f);
		Cell titleCell = titleRow.createCell(0);
		titleCell.setCellValue(sheetTitle);
		titleCell.setCellStyle(comStyle.get("title"));
		sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, titles.size()-1));

		//the header row: centered text
		Row headerRow = sheet.createRow(6);
		headerRow.setHeight((short) 300);
		headerRow.setHeightInPoints(15.00f);

		Cell cell;

		for (int i = 0, n = titles.size(); i < n; i++) {
			cell = headerRow.createCell(i);
			cell.setCellValue(titles.getString(i).replaceAll("<br/>", " ").replaceAll("<br>", " ").replaceAll("<br />", " "));

			if((i+1) == n) {
				cell.setCellStyle(comStyle.get("header_right"));
			} else if(i == 0) {
				cell.setCellStyle(comStyle.get("header_left"));
			} else {
				cell.setCellStyle(comStyle.get("header"));
			}
		}

		//freeze the first row
		sheet.createFreezePane(0, 7);

		Row row;
		int rownum = 7;

		String styleType = "";
		String styleName = "";
		int numFormat = 0;
		int titleSize = titles.size();
		Map<String, Object> data = null;

		int readCnt = excelData.getDataSize();
		int minCnt = 1;
		int dataSize = 0;
		boolean nextData = true;

		while(nextData){
			List<Map<String, Object>> dataList = null;

			// DB에서 조회를 여러번 처리 시
			if("Y".equals(Util.null2str(pageType))){
				Map<String, Object> search = new HashMap<String, Object>();
				search.put("minCnt", minCnt);
				search.put("maxCnt", minCnt + readCnt);

				dataList = excelData.doSearch(search);

				minCnt += readCnt;

				if(dataList.size() <= readCnt){
					nextData = false;
					dataSize = dataList.size();
				} else {
					dataSize = readCnt;
				}
			}
			// DB에서 조회를 1회 처리 시
			else {
				nextData = false;
				dataList = excelData.doSearch(new HashMap<String, Object>());
				dataSize = dataList.size();
			}

			for (int i = 0, n = dataSize; i < n; i++, rownum++) {
				row = sheet.createRow(rownum);
				row.setHeight((short) 300);

				data = dataList.get(i);
				String columnName = "";

				for(int j = 0; j < titleSize; j++) {
					cell = row.createCell(j);
					columnName = Util.null2str(columns.getString(j));

					// Cell Style Setting
					styleType = Util.null2str(excelTypes.getString(j), "left");
					numFormat = Util.null2int(numFormats.getString(j));
					String defaultFormat = "#,##0";

					switch(styleType) {
						case "left" :
							if((i+1) == n) {
								if(j == 0)
									cell.setCellStyle(styleList.get(j).get("cell_normal_left_bottom"));
								else if((j+1) == titleSize)
									cell.setCellStyle(styleList.get(j).get("cell_normal_right_bottom"));
								else
									cell.setCellStyle(styleList.get(j).get("cell_normal_bottom"));
							} else {
								if(j == 0)
									cell.setCellStyle(styleList.get(j).get("cell_normal_left"));
								else if((j+1) == titleSize)
									cell.setCellStyle(styleList.get(j).get("cell_normal_right"));
								else
									cell.setCellStyle(styleList.get(j).get("cell_normal"));
							}

							cell.setCellValue(Util.null2str(data.get(columnName)));

							break;
						case "center" :
							if((i+1) == n) {
								if(j == 0)
									cell.setCellStyle(styleList.get(j).get("cell_normal_centered_left_bottom"));
								else if((j+1) == titleSize)
									cell.setCellStyle(styleList.get(j).get("cell_normal_centered_right_bottom"));
								else
									cell.setCellStyle(styleList.get(j).get("cell_normal_centered_bottom"));
							} else {
								if(j == 0)
									cell.setCellStyle(styleList.get(j).get("cell_normal_centered_left"));
								else if((j+1) == titleSize)
									cell.setCellStyle(styleList.get(j).get("cell_normal_centered_right"));
								else
									cell.setCellStyle(styleList.get(j).get("cell_normal_centered"));
							}

							cell.setCellValue(Util.null2str(data.get(columnName)));

							break;
						case "right" :
							if((i+1) == n) {
								if(j == 0)
									cell.setCellStyle(styleList.get(j).get("cell_right_left_bottom"));
								else if((j+1) == titleSize)
									cell.setCellStyle(styleList.get(j).get("cell_right_right_bottom"));
								else
									cell.setCellStyle(styleList.get(j).get("cell_right_bottom"));
							} else {
								if(j == 0)
									cell.setCellStyle(styleList.get(j).get("cell_right_left"));
								else if((j+1) == titleSize)
									cell.setCellStyle(styleList.get(j).get("cell_right_right"));
								else
									cell.setCellStyle(styleList.get(j).get("cell_right"));
							}

							cell.setCellValue(Util.null2str(data.get(columnName)));

							break;
						case "number" :
							if((i+1) == n) {
								if(j == 0)					styleName = "cell_normal_number_left_bottom";
								else if((j+1) == titleSize)	styleName = "cell_normal_number_right_bottom";
								else						styleName = "cell_normal_number_bottom";
							} else {
								if(j == 0)					styleName = "cell_normal_number_left";
								else if((j+1) == titleSize)	styleName = "cell_normal_number_right";
								else						styleName = "cell_normal_number";
							}

							if(numFormat > 0) {
								defaultFormat += ".";
								for(int k = 0; k < numFormat; k++) {
									defaultFormat += "0";
								}
							} else {
								String[] digits = Util.null2str(data.get(columnName)).split("\\.");
								if(digits.length > 1) {
									defaultFormat += ".";
									for(int k = 0; k < digits[1].length(); k++) {
										defaultFormat += "0";
									}
								}
							}

							styleList.get(j).get(styleName).setDataFormat(df.getFormat(defaultFormat));

							cell.setCellStyle(styleList.get(j).get(styleName));

							cell.setCellValue(Util.null2int(data.get(columnName)));

							break;
						case "double" :
							if((i+1) == n) {
								if(j == 0)					styleName = "cell_normal_double_left_bottom";
								else if((j+1) == titleSize)	styleName = "cell_normal_double_right_bottom";
								else						styleName = "cell_normal_double_bottom";
							} else {
								if(j == 0)					styleName = "cell_normal_double_left";
								else if((j+1) == titleSize)	styleName = "cell_normal_double_right";
								else						styleName = "cell_normal_double";
							}

							if(numFormat > 0) {
								defaultFormat += ".";
								for(int k = 0; k < numFormat; k++) {
									defaultFormat += "0";
								}
							} else {
								String[] digits = Util.null2str(data.get(columnName)).split("\\.");
								if(digits.length > 1) {
									defaultFormat += ".";
									for(int k = 0; k < digits[1].length(); k++) {
										defaultFormat += "0";
									}
								}
							}

							styleList.get(j).get(styleName).setDataFormat(df.getFormat(defaultFormat));

							cell.setCellStyle(styleList.get(j).get(styleName));

							cell.setCellValue(Util.null2dbl(data.get(columnName)));

							break;
						case "date" :
							if((i+1) == n) {
								if(j == 0)
									cell.setCellStyle(styleList.get(j).get("cell_normal_date_left_bottom"));
								else if((j+1) == titleSize)
									cell.setCellStyle(styleList.get(j).get("cell_normal_date_right_bottom"));
								else
									cell.setCellStyle(styleList.get(j).get("cell_normal_date_bottom"));
							} else {
								if(j == 0)
									cell.setCellStyle(styleList.get(j).get("cell_normal_date_left"));
								else if((j+1) == titleSize)
									cell.setCellStyle(styleList.get(j).get("cell_normal_date_right"));
								else
									cell.setCellStyle(styleList.get(j).get("cell_normal_date"));
							}

							try{
								Date date = DateTimeUtil.str2date(Util.null2str(data.get(columnName)), "yyyy-MM-dd"); //2019.01.25 해외엑셀다운로드 날짜형식 오류로 수정 / 지서희
								cell.setCellValue(DateTimeUtil.ConvertDate2String(date, new SimpleDateFormat(dateFormat)));
							}catch(Exception e){
								cell.setCellValue("");
							}

							break;
						default :
							if((i+1) == n) {
								if(j == 0)
									cell.setCellStyle(styleList.get(j).get("cell_normal_left_bottom"));
								else if((j+1) == titleSize)
									cell.setCellStyle(styleList.get(j).get("cell_normal_right_bottom"));
								else
									cell.setCellStyle(styleList.get(j).get("cell_normal_bottom"));
							} else {
								if(j == 0)
									cell.setCellStyle(styleList.get(j).get("cell_normal_left"));
								else if((j+1) == titleSize)
									cell.setCellStyle(styleList.get(j).get("cell_normal_right"));
								else
									cell.setCellStyle(styleList.get(j).get("cell_normal"));
							}

							cell.setCellValue(Util.null2str(data.get(columnName)));
					}
				}
			}
		}

		for(int i = 0; i < titleSize; i++) {
			if(sheet.getColumnWidth(i) <= 60000)
				sheet.setColumnWidth(i, (sheet.getColumnWidth(i))+500 );
		}

		return wb;
	}
	
	@SuppressWarnings("unchecked")
	public String[] makeCargoManifestExcel(String excelType, Map<String, Object> searchMap, String sheetTitle, String frontFileName, 
			String userName, String dateFormat) throws Exception{
		
		ObjectMapper objectMapper = new ObjectMapper();
		String bound = Util.null2str(searchMap.get("srchBoundType"));
		String excelDir = staticCustomProperties.getExcelDownload();
		String headerTitle;
		String[] files = new String[2];
        FileInputStream file= null;
		Workbook wb;
		Sheet sheet;
		Row row;
		Cell cell;
		
		List<String> headerNameList = null;
		List<String> columnNameList = null;
		List<Integer> alignTypeList = null;
		List<Integer> colSizeList = null;
		
		int rownum = 0;
		// Return값 [0]: 실제 파일 저장경로, [1]: 다운로드받을 파일명
		if("xls".equals(Util.null2str(excelType))) 	wb = new HSSFWorkbook();
	    else 										wb = new XSSFWorkbook();
			
		sheet = wb.createSheet(sheetTitle.replaceAll("/", "")); // 시트생
		PrintSetup print = sheet.getPrintSetup();
		print.setLandscape(true);// 가로인쇄
		print.setPaperSize(PrintSetup.A4_PAPERSIZE);// A4 인쇄
		print.setScale((short) 75);// 75%배율
		
		Map<String, CellStyle> comStyle = ExcelStyles.createCargoManifestStyles(wb);
		List<Object> datas = (List<Object>)searchMap.get("data");
		Map<String, Object> data = (Map<String, Object>)objectMapper.convertValue(datas.get(0), Map.class);

		if(bound.startsWith("A")) {
			headerNameList = Arrays.asList("BL NO", "", "", "Shipper", "", "", "Consignee", "", "", "", "PCS","Value($)", "Weight(kg)", "Item Description", "", "", "", "", "NAR/NDR");
			columnNameList = Arrays.asList("hblNo", "", "", "shipperAddress", "", "", "consigneeAddress", "","", "", "pieces", "amountLoc", "gwt", "commodityItem", "", "", "", "", "cdType");
			alignTypeList = Arrays.asList(1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 3, 3, 3, 1, 0, 0, 0, 0, 2); // 1.왼쪽,2.가운데,3.오른쪽정렬
			colSizeList = Arrays.asList(4500, 1000, 1000, 1000, 1000, 5000, 2000, 1000, 2000, 2000, 2000,3000, 3000, 2000, 2000, 2000, 2000, 2000, 2500);
			headerTitle = "Air Consolidated "+ sheetTitle;
			
			row = sheet.createRow(0);
			cell = row.createCell(0);
			cell.setCellValue("TEL : / FAX : ");
			cell.setCellStyle(comStyle.get("label_style"));
			cell = row.createCell(1);
			cell.setCellValue(Util.null2str(data.get("userTel"))+" / "+Util.null2str(data.get("userFax")));
			cell.setCellStyle(comStyle.get("normal_style"));
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 3));

			row = sheet.createRow(1);
			cell = row.createCell(0);
			cell.setCellValue(headerTitle);
			cell.setCellStyle(comStyle.get("title_style"));
			sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 18));

			row = sheet.createRow(2);
			cell = row.createCell(0);
			cell.setCellValue("Operator : ");
			cell.setCellStyle(comStyle.get("label_style"));
			cell = row.createCell(1);
			cell.setCellValue(Util.null2str(data.get("shipperCountryCode")));
			cell.setCellStyle(comStyle.get("normal_style"));
			sheet.addMergedRegion(new CellRangeAddress(2, 2, 1, 3));
			cell = row.createCell(5);
			cell.setCellValue("Flight No : ");
			cell.setCellStyle(comStyle.get("label_style"));
			cell = row.createCell(6);
			cell.setCellValue(Util.null2str(data.get("vesselFlight")));
			cell.setCellStyle(comStyle.get("normal_style"));
			sheet.addMergedRegion(new CellRangeAddress(2, 2, 6, 9));
			cell = row.createCell(10);
			cell.setCellValue("E.T.D :");
			cell.setCellStyle(comStyle.get("label_style"));
			cell = row.createCell(11);
			cell.setCellValue(Util.null2str(data.get("obDate")));
			cell.setCellStyle(comStyle.get("normal_style"));
			cell = row.createCell(13);
			cell.setCellValue("E.T.A :");
			cell.setCellStyle(comStyle.get("label_style"));
			cell = row.createCell(14);
			cell.setCellValue(Util.null2str(data.get("arDate")));
			cell.setCellStyle(comStyle.get("normal_style"));

			row = sheet.createRow(3);
			cell = row.createCell(5);
			cell.setCellValue("M/BL# : ");
			cell.setCellStyle(comStyle.get("label_style"));
			cell = row.createCell(6);
			cell.setCellValue(Util.null2str(data.get("mblNo")));
			cell.setCellStyle(comStyle.get("normal_style"));
			sheet.addMergedRegion(new CellRangeAddress(3, 3, 6, 8));

			row = sheet.createRow(4);
			cell = row.createCell(0);
			cell.setCellValue("Consolidator : ");
			cell.setCellStyle(comStyle.get("label_style"));
			cell = row.createCell(1);
			cell.setCellValue(Util.null2str(data.get("userName")));
			cell.setCellStyle(comStyle.get("normal_style"));
			sheet.addMergedRegion(new CellRangeAddress(4, 4, 1, 3));
			cell = row.createCell(5);
			cell.setCellValue("DE-Consolidator : ");
			cell.setCellStyle(comStyle.get("label_style"));
			cell = row.createCell(6);
			cell.setCellValue(Util.null2str(data.get("customerName")));
			cell.setCellStyle(comStyle.get("normal_style"));
			sheet.addMergedRegion(new CellRangeAddress(4, 4, 6, 9));
			cell = row.createCell(10);
			cell.setCellValue("Tel : ");
			cell.setCellStyle(comStyle.get("label_style"));
			cell = row.createCell(11);
			cell.setCellValue(Util.null2str(data.get("userTel")));
			cell.setCellStyle(comStyle.get("normal_style"));
			
			rownum = 6;
		}else {

			headerNameList = Arrays.asList("BL NO","","","Shipper","","","Consignee","","","","PCS","Value($)","Weight(kg)","CBM","Item Description","","","","NAR/NDR","CONTAINER NO");
			columnNameList = Arrays.asList("hblNo","","","shipperAddress","","","consigneeAddress","","","","pieces","amountLoc","gwt","cbm","commodityItem","","","","cdType","containerInfo");
			alignTypeList = Arrays.asList(1,0,0,1,0,0,1,0,0,0,3,3,3,3,1,0,0,0,2,1); // 1.왼쪽, 2.가운데, 3.오른쪽 정렬
			colSizeList = Arrays.asList(4500,1000,1000,1000,1000,5000,2000,1000,2000,2000,2000,3000,3000,3000,2000,2000,2000,2000,2500,4000);
			headerTitle = "Sea Consolidated "+ sheetTitle;
			
			row = sheet.createRow(1);
			cell = row.createCell(0);
			cell.setCellValue("Sea Consolidated Cargo Manifest");
			cell.setCellStyle(comStyle.get("title_style"));
			sheet.addMergedRegion(new CellRangeAddress(1,1,0,19));
		
			row = sheet.createRow(2);
			cell = row.createCell(0);
			cell.setCellValue("TO : ");
			cell.setCellStyle(comStyle.get("label_style"));
			cell = row.createCell(1);
			cell.setCellValue(Util.null2str(data.get("userName")));
			cell.setCellStyle(comStyle.get("normal_style"));
			sheet.addMergedRegion(new CellRangeAddress(2,2,1,3));
			cell = row.createCell(5);
			cell.setCellValue("ATTN : ");
			cell.setCellStyle(comStyle.get("label_style"));
			cell = row.createCell(6);
			cell.setCellValue(Util.null2str(data.get("customerName")));
			cell.setCellStyle(comStyle.get("normal_style"));
			sheet.addMergedRegion(new CellRangeAddress(2,2,6,9));
			cell = row.createCell(10);
			cell.setCellValue("TEL :");
			cell.setCellStyle(comStyle.get("label_style"));
			cell = row.createCell(11);
			cell.setCellValue(Util.null2str(data.get("userTel")));
			cell.setCellStyle(comStyle.get("normal_style"));
			cell = row.createCell(13);
			cell.setCellValue("FAX :");
			cell.setCellStyle(comStyle.get("label_style"));
			cell = row.createCell(14);
			cell.setCellValue(Util.null2str(data.get("userFax")));
			cell.setCellStyle(comStyle.get("normal_style"));
			cell = row.createCell(17);
			cell.setCellValue("E.T.D : ");
			cell.setCellStyle(comStyle.get("label_style"));
			cell = row.createCell(18);
			cell.setCellValue(Util.null2str(data.get("obDate")));
			cell.setCellStyle(comStyle.get("normal_style"));
			
			row = sheet.createRow(3);
			cell = row.createCell(0);
			cell.setCellValue("M/BL# : ");
			cell.setCellStyle(comStyle.get("label_style"));
			cell = row.createCell(1);
			cell.setCellValue(Util.null2str(data.get("mblNo")));
			cell.setCellStyle(comStyle.get("normal_style"));
			sheet.addMergedRegion(new CellRangeAddress(3,3,1,3));
			cell = row.createCell(5);
			cell.setCellValue("VESSEL/VOY : ");
			cell.setCellStyle(comStyle.get("label_style"));
			cell = row.createCell(6);
			cell.setCellValue(Util.null2str(data.get("vesselFlight"))+" / "+Util.null2str(data.get("voyage")));
			cell.setCellStyle(comStyle.get("normal_style"));
			sheet.addMergedRegion(new CellRangeAddress(3,3,6,9));
			cell = row.createCell(10);
			cell.setCellValue("P.O.L :");
			cell.setCellStyle(comStyle.get("label_style"));
			cell = row.createCell(11);
			cell.setCellValue(Util.null2str(data.get("origName")));
			cell.setCellStyle(comStyle.get("normal_style"));
			cell = row.createCell(13);
			cell.setCellValue("P.O.D :");
			cell.setCellStyle(comStyle.get("label_style"));
			cell = row.createCell(14);
			cell.setCellValue(Util.null2str(data.get("destName")));
			cell.setCellStyle(comStyle.get("normal_style"));
			cell = row.createCell(17);
			cell.setCellValue("E.T.A : ");
			cell.setCellStyle(comStyle.get("label_style"));
			cell = row.createCell(18);
			cell.setCellValue(Util.null2str(data.get("arDate")));
			cell.setCellStyle(comStyle.get("normal_style"));
			
			rownum = 5;
		}
		
		sheet.getHeader().setLeft(headerTitle);
		sheet.getHeader().setRight("Page : " + HeaderFooter.page() + " / " + HeaderFooter.numPages());
		
		//헤더 세팅 
		int cellnum = 0;
		row = sheet.createRow(rownum);
		for(String headerName:headerNameList) {
			cell = row.createCell(cellnum);
			cell.setCellValue(headerName);
			cell.setCellStyle(comStyle.get("header_style"));
			cellnum++;
		}
		
		sheet.addMergedRegion(new CellRangeAddress(rownum,rownum,0,2));
		sheet.addMergedRegion(new CellRangeAddress(rownum,rownum,3,5));
		sheet.addMergedRegion(new CellRangeAddress(rownum,rownum,6,9));
		if(bound.startsWith("A")) {
			sheet.addMergedRegion(new CellRangeAddress(rownum, rownum, 13, 17));
		}else {
			sheet.addMergedRegion(new CellRangeAddress(rownum,rownum, 14, 17));
		}
		rownum ++;
		// 셀 길이 셋팅
		cellnum = 0;
		for(Integer colSize : colSizeList) {
			sheet.setColumnWidth(cellnum, colSize);
			cellnum++;
		}
		
		int ConsigneeAddressHeight = 0;
		for (int i = 0; i < datas.size(); i++, rownum++) {
			data = (Map<String, Object>)objectMapper.convertValue(datas.get(i), Map.class);
			row = sheet.createRow(rownum);
			cellnum = 0;
			for(String colName : columnNameList) {
				cell = row.createCell(cellnum);
				String value;
				if(colName.equals("shipperAddress")) value = Util.null2str(data.get("shipperName"))+ "\n" + Util.null2str(data.get(colName));
				else  value = Util.null2str(data.get(colName));
				if(colName.equals("consigneeAddress")) ConsigneeAddressHeight = Util.null2str(data.get(colName)).length();
				switch(alignTypeList.get(cellnum)){
					case 0 :
						cell.setCellStyle(comStyle.get("text_left_style"));
						break;
					case 1 :
						cell.setCellValue(value);
						cell.setCellStyle(comStyle.get("text_left_style"));
						break;
					case 2 :
						cell.setCellValue(value);
						cell.setCellStyle(comStyle.get("text_center_style"));
						break;
					case 3 :
						cell.setCellValue(value);
						cell.setCellStyle(comStyle.get("text_right_style"));
						break;
				}
				
				cellnum++;
			}
			
			sheet.addMergedRegion(new CellRangeAddress(rownum,rownum,0,2));
			sheet.addMergedRegion(new CellRangeAddress(rownum,rownum,3,5));
			sheet.addMergedRegion(new CellRangeAddress(rownum,rownum,6,9));
			if(bound.startsWith("A")) {
				sheet.addMergedRegion(new CellRangeAddress(rownum, rownum, 13, 17));
			}else {
				sheet.addMergedRegion(new CellRangeAddress(rownum,rownum, 14, 17));
			}
			
			row.setHeight((short) Math.max(ConsigneeAddressHeight*20 , 1000));
		}
		row = sheet.createRow(rownum);
		cell = row.createCell(0);
		cell.setCellValue("TOTAL");
		cell.setCellStyle(comStyle.get("text_center_style"));
		cell = row.createCell(1);
		cell.setCellStyle(comStyle.get("text_center_style"));
		cell = row.createCell(2);
		cell.setCellStyle(comStyle.get("text_center_style"));
		sheet.addMergedRegion(new CellRangeAddress(rownum, rownum, 0, 2));
		cell = row.createCell(3);
		cell.setCellValue(Util.null2str(data.get("sumCount")));
		cell.setCellStyle(comStyle.get("text_right_style"));
		cell = row.createCell(4);
		cell.setCellStyle(comStyle.get("text_center_style"));
		cell = row.createCell(5);
		cell.setCellStyle(comStyle.get("text_center_style"));
		sheet.addMergedRegion(new CellRangeAddress(rownum, rownum, 3, 5));
		cell = row.createCell(6);
		cell.setCellStyle(comStyle.get("text_center_style"));
		cell = row.createCell(7);
		cell.setCellStyle(comStyle.get("text_center_style"));
		cell = row.createCell(8);
		cell.setCellStyle(comStyle.get("text_center_style"));
		cell = row.createCell(9);
		cell.setCellStyle(comStyle.get("text_center_style"));
		sheet.addMergedRegion(new CellRangeAddress(rownum, rownum, 6, 9));
		cell = row.createCell(10);
		cell.setCellValue(Util.null2str(data.get("sumPieces")));
		cell.setCellStyle(comStyle.get("text_right_style"));
		cell = row.createCell(11);
		cell.setCellStyle(comStyle.get("text_right_style"));
		cell.setCellValue(Util.null2str(data.get("sumAmount")));
		cell = row.createCell(12);
		cell.setCellValue(Util.null2str(data.get("sumGwt")) + " KG");
		cell.setCellStyle(comStyle.get("text_right_style"));
		cell = row.createCell(13);
		cell.setCellStyle(comStyle.get("text_center_style"));
		cell = row.createCell(14);
		cell.setCellStyle(comStyle.get("text_center_style"));
		cell = row.createCell(15);
		cell.setCellStyle(comStyle.get("text_center_style"));
		cell = row.createCell(16);
		cell.setCellStyle(comStyle.get("text_center_style"));
		cell = row.createCell(17);
		cell.setCellStyle(comStyle.get("text_center_style"));
		cell = row.createCell(18);
		cell.setCellStyle(comStyle.get("text_center_style"));
		
		if(bound.startsWith("A")) {
			sheet.addMergedRegion(new CellRangeAddress(rownum, rownum, 13, 17));
		}else {
			sheet.addMergedRegion(new CellRangeAddress(rownum,rownum,14,17));
			cell = row.createCell(19);
			cell.setCellStyle(comStyle.get("text_center_style"));
		}
			
		// Write the output to a file
		String currDtm = DateTimeUtil.getDate("yyyyMMddHHmmss");
        
        String saveFile = frontFileName + "_" + currDtm + ".xls";													// 임시파일명
    	if(Common.checkSaveFilenameForExcel(frontFileName)) {
			String uuid = UUID.randomUUID().toString();
            saveFile = uuid + "_" + currDtm + ".xls";
		}
        String originFile = frontFileName + "_" + currDtm.substring(0,4) + "-" + currDtm.substring(4,6) + "-" + currDtm.substring(6,8) + ".xls";	// 원본파일명
        
        if(wb instanceof XSSFWorkbook) {
        	saveFile += "x";
        	originFile += "x";
        }
        
        //workbook파일 출력
        FileUtil.fileWrite(wb, excelDir, saveFile);
        
    	// Return값 [0]: 저장된 파일명, [1]: 다운로드받을 파일명
        files[0] = saveFile;
        files[1] = originFile;
        
		return files;
	}
	
	/**
	 * 엑셀 인쇄 용지 크기 설정
	 * @param printSetup
	 * @param isHorizontal
	 */
	public void setPaperSize(PrintSetup printSetup, short size, Boolean isHorizontal) {
		printSetup.setPaperSize(size);
		printSetup.setLandscape(isHorizontal);
	}

	/**
	 * CSV파일 생성시 CSV파일 구분 값인 ,(comma) 와 개행값 제거  
	 * @param src
	 * @return
	 */    
	public static String convertCsvFormat(String str){

		if(!"".equals(str)){
	        str = str.replaceAll(","," ");
	        str = str.replaceAll("\n"," ");
	        str = str.replaceAll("\r"," ");
	        str = str.replaceAll("\r\n"," ");
			str = str.replaceAll("\\\\s", "''s");
			str = str.replaceAll("(\\r\\n|\\r|\\n|\\n\\r|\\\\)", " ");
		}

		return str;		
	}	
	
	/**
	 * 엑셀 양식 ITEM, CONTAINER 테이블 컬럼 축약된 이름으로 변환 
	 * @param type 
	 * @param str
	 * @return
	 */    
	public static Map<String, Object> convertExcelDbField(String type){
		Map<String, Object> convertMap = new HashMap<String, Object>();
		if("item".equals(Util.null2str(type))){ // 아이템
			convertMap.put("DETAIL_ITEM_NAME","DIN");
			convertMap.put("DETAIL_ITEM_NAME2","DIN");
			convertMap.put("DETAIL_ITEM_NAME_LOC","DINL");
			convertMap.put("DETAIL_ITEM_NAME_LOC2","DINL");
			convertMap.put("CURRENCY_CODE","CC");
			convertMap.put("CURRENCY_CODE2","CC");
			convertMap.put("ECM_URL","EU");
			convertMap.put("ECM_URL2","EU");
			convertMap.put("GWT","G");
			convertMap.put("GWT2","G");
			convertMap.put("VWT","V");
			convertMap.put("VWT2","V");
			convertMap.put("DIM_WIDTH","DW");
			convertMap.put("DIM_WIDTH2","DW");
			convertMap.put("DIM_LENGTH","DL");
			convertMap.put("DIM_LENGTH2","DL");
			convertMap.put("DIM_HEIGHT","DH");
			convertMap.put("DIM_HEIGHT2","DH");
			convertMap.put("DELIVERY_NO","DN");
			convertMap.put("DELIVERY_NO2","DN");
			convertMap.put("ORIGIN_COUNTRY","OC");
			convertMap.put("ORIGIN_COUNTRY2","OC");
			convertMap.put("BRAND","BR");
			convertMap.put("BRAND2","BR");
			convertMap.put("REMARKS","RE");
			convertMap.put("REMARKS2","RE");
			convertMap.put("UNIT_PIECES","UPI");
			convertMap.put("UNIT_PIECES2","UPI");
			convertMap.put("UNIT_PRICE","UPR");
			convertMap.put("UNIT_PRICE2","UPR");
			convertMap.put("AMOUNT","AM");
			convertMap.put("AMOUNT2","AM");
			convertMap.put("PIECES","PI");
			convertMap.put("PIECES2","PI");
			convertMap.put("HS_CODE","HC");
			convertMap.put("HS_CODE2","HC");
			convertMap.put("SEQ_NO_ID","SEQ");
			convertMap.put("SEQ_NO_ID2","SEQ");
			convertMap.put("NUM","N");
			convertMap.put("NUM2","N");
			// 신규 추가(DKK용)
			convertMap.put("CUSTOMS_ID","CI");
			convertMap.put("CUSTOMS_ID","CI");
			convertMap.put("CUSTOMS_TEMP_NO","CT");
			convertMap.put("CUSTOMS_TEMP_NO2","CT");
			convertMap.put("ORDER_NO","ON2");
			convertMap.put("ORDER_NO2","ON2");
			convertMap.put("CATEGORY_NAME_1","CN1");
			convertMap.put("CATEGORY_NAME_1_2","CN1");
			convertMap.put("CATEGORY_NAME_2","CN2");
			convertMap.put("CATEGORY_NAME_2_2","CN2");
			convertMap.put("CATEGORY_NAME_3","CN3");
			convertMap.put("CATEGORY_NAME_3_2","CN3");
			convertMap.put("CATEGORY_NAME_4","CN4");
			convertMap.put("CATEGORY_NAME_4_2","CN4");
			convertMap.put("CATEGORY_NAME_5","CN5");
			convertMap.put("CATEGORY_NAME_5_2","CN5");
			convertMap.put("CATEGORY_NAME_6","CN6");
			convertMap.put("CATEGORY_NAME_6_2","CN6");
			convertMap.put("CATEGORY_NAME_7","CN7");
			convertMap.put("CATEGORY_NAME_7_2","CN7");
			convertMap.put("CATEGORY_NAME_8","CN8");
			convertMap.put("CATEGORY_NAME_8_2","CN8");
			convertMap.put("CATEGORY_NAME_9","CN9");
			convertMap.put("CATEGORY_NAME_9_2","CN9");
			convertMap.put("OPTION_NAME","OPN");
			convertMap.put("OPTION_NAME2","OPN");
			convertMap.put("SELLER_NAME","SN");
			convertMap.put("SELLER_NAME2","SN");
			convertMap.put("SELLER_ID","SI");
			convertMap.put("SELLER_ID2","SI");
			convertMap.put("SELLER_CODE","SC");
			convertMap.put("SELLER_CODE2","SC");
			convertMap.put("ITEM_ID","IID");
			convertMap.put("ITEM_ID2","IID");
			convertMap.put("ITEM_MAF_NAME","IMN");
			convertMap.put("ITEM_MAF_NAME2","IMN");
			convertMap.put("ITEM_MAF_MODEL_NO","IMM");
			convertMap.put("ITEM_MAF_MODEL_NO2","IMM");
			convertMap.put("ITEM_BRAND_NAME","IBN");
			convertMap.put("ITEM_BRAND_NAME2","IBN");
			convertMap.put("ORDER_END_DATETIME","OED");
			convertMap.put("ORDER_END_DATETIME2","OED");
			convertMap.put("ITEM_NO","INO");
			convertMap.put("ITEM_NO2","INO");
			convertMap.put("ECM_YN","EYN");
			convertMap.put("ECM_YN2","EYN");
		}else if("cont".equals(Util.null2str(type))){ // 컨테이너
			convertMap.put("CNTR_NO","CN");
			convertMap.put("CNTR_SEAL_NO_1","CSN1");
			convertMap.put("CNTR_SEAL_NO_2","CSN2");
			convertMap.put("CNTR_SEAL_NO_3","CSN3");
			convertMap.put("CNTR_CODE","CC");
			convertMap.put("CNTR_EDI_CODE","CEC");
			convertMap.put("CNTR_SIZE","CS");
			convertMap.put("CNTR_SUB_CODE","CSC");
			convertMap.put("CNTR_LOAD_TYPE","CLT");
			convertMap.put("PIECES","PI");
			convertMap.put("UNIT_PIECES","UPI");
			convertMap.put("GWT","G");
			convertMap.put("UNIT_WEIGHT","UWE");
			convertMap.put("CBM","C");			
			convertMap.put("GWT_LBS","GWTL");
			convertMap.put("CBM_CFT","CBMC");
			convertMap.put("CNTR_PART_YN","CPY");		
			convertMap.put("NUM","N");
		}else if("multi".equals(Util.null2str(type))){
			convertMap.put("CUSTOMER_CODE","A");
			convertMap.put("DELIVERY_CODE","B");
			convertMap.put("DELIVERY_FEE","C");
			convertMap.put("DELIVERY_NO","D");
			convertMap.put("DELIVERY_NO_CHANGED","E");
			convertMap.put("DIM_HEIGHT","F");
			convertMap.put("DIM_LENGTH","G");
			convertMap.put("DIM_WIDTH","H");
			convertMap.put("PAYMENT_GUBN","I");
			convertMap.put("PIECES","J");
			convertMap.put("REMARKS","K");
			convertMap.put("SEQ_NO_ID","NUM");
		}else if("license".equals(Util.null2str(type))){ // 면장정보
			convertMap.put("LICENSE_NO","LN");
			convertMap.put("PIECES","PI");
			convertMap.put("UNIT_PIECES","UPI");
			convertMap.put("GWT","GWT");
			convertMap.put("PARTIAL_YN","PY");
			convertMap.put("PARTIAL_SEQ","PS");
			convertMap.put("COMBINE_YN","CY");
			convertMap.put("COMBINE_TYPE_A_Z","CTA");
			convertMap.put("COMBINE_PIECES","CP");
			convertMap.put("EFN_SENDER_CODE","ESC");
			convertMap.put("EFN_PO_NO","EPN");
			convertMap.put("EFN_DELIVERY_NO","EDN");
			convertMap.put("NUM","N");
		}

		return convertMap;		
	}

	@SuppressWarnings("unchecked")
	public String[] makeLicenseBrosExcel(String excelType, Map<String, Object> searchMap, String sheetTitle, String frontFileName, 
			String userName, String dateFormat) {
		ObjectMapper objectMapper = new ObjectMapper();
		String bound = Util.null2str(searchMap.get("srchBoundType"));
		String excelDir = staticCustomProperties.getExcelDownload();
		String headerTitle;
		String[] files = new String[2];
        FileInputStream file= null;
		Workbook wb;
		Sheet sheet;
		Row row;
		Cell cell;
		
		List<String> headerNameList = null;
		List<String> columnNameList = null;
		List<Integer> alignTypeList = null;
		List<Integer> colSizeList = null;
		
		int rownum = 0;
		// Return값 [0]: 실제 파일 저장경로, [1]: 다운로드받을 파일명
		if("xls".equals(Util.null2str(excelType))) 	wb = new HSSFWorkbook();
	    else 										wb = new XSSFWorkbook();
			
		sheet = wb.createSheet(sheetTitle.replaceAll("/", "")); // 시트생
		PrintSetup print = sheet.getPrintSetup();
		print.setLandscape(true);// 가로인쇄
		print.setPaperSize(PrintSetup.A4_PAPERSIZE);// A4 인쇄
		print.setScale((short) 75);// 75%배율
		
		Map<String, CellStyle> comStyle = ExcelStyles.createCargoManifestStyles(wb);
		List<Object> datas = (List<Object>) searchMap.get("data");
		Map<String, Object> data = (Map<String, Object>)objectMapper.convertValue(datas.get(0), Map.class);

		if("brosLicenseX".equals(sheetTitle)) {
			headerNameList = Arrays.asList("주문번호","상품ID", "상품명(영문)","중량","가격","주문수량","결제금액","결제통화코드","구매자상호명","목적국 국가코드"
											,"HS코드","원산지 국가코드","운송수단","적재항","구매자우편번호","구매자주소","구매자전화번호","제조자","제조자사업자번호","제조자통관고유부호","제조장소(우편번호)"
											,"인도조건","운임원화","보험료원화","결제방법코드","신고인기재란");

			columnNameList = Arrays.asList("poNo", "itemCode", "detailItemName","nwt", "unitPriceCur", "pieces", "licensePayAmountCur", "currencyCode", "consigneeName", "destCountry"
											, "hsCode", "originCountry", "deliveryTypeCode", "destPort", "consigneeZipcode", "consigneeAddress", "consigneeTel", "makerName", "makerBusinessId", "makerCustomsId", "makerZipcode"
											, "incoterms", "deliveryFeeLoc", "insuranceLoc","licensePayMethod", "licenseRemark");
			
			alignTypeList = Arrays.asList(3,1,1,1,1,1,1,1,1,1
											,1,1,1,1,1,1,1,1,1,1,1
											,1,1,1,1,1);
			
			colSizeList = Arrays.asList(4500,3500,5000,4500,3000,3000,3000,4500,4500,5000
							,4500,5000,3500,3500,5000,8000,4000,3500,5000,5000,6000
							,3500,3500,4000,4000,15000);
		} else if("brosLicenseEUltrade".equals(sheetTitle)){
			
			headerNameList = Arrays.asList("주문번호","주문배송관련번호","목적국 국가코드","구매자상호명","HS코드","상품명(영문)","모델규격","가격","주문수량"
											,"중량","조정금액","결제금액","결제통화코드","결제방법코드","제조자 상호","상품ID","제조자통관고유부호","제조자사업자번호","제조자사업장일련번호"
											,"신고인 사서함부호","신고인기재란","인도조건","운임","특송업체부호","웹페이지주소","신고세관","신고과"
											,"운송수단","적재항","물품소재지 우편번호","물품소재지 주소","장치장소명");
			
			columnNameList = Arrays.asList("poNo","hblNo", "destCountry",  "consigneeName","hsCode","detailItemName", "itemSize", "unitPriceCur","pieces"
											,"nwt","adjustAmountLoc","licensePayAmountCur","currencyCode", "licensePayMethod","makerName","itemCode","makerCustomsId","makerBusinessId","null"
											,"null","licenseRemark","incoterms","deliveryFeeCur","expressSign","itemUrl","licenseCustomsCode","licenseCustomsDptCode"
											,"deliveryTypeCode","destPort","goodsLocZipcode","goodsLocAddress","bwhName");
			
			alignTypeList = Arrays.asList(1,1,1,1,1,1,1,3,3
											,3,1,3,1,1,1,1,1,1,1
											,1,1,1,1,1,1,1,1
											,1,1,1,1,1);
			
			colSizeList = Arrays.asList(4500,5000,4500,4500,5000,5000,4000,4000,4000
										,3500,4000,4000,3500,3500,4500,4500,6000,4000,4000
										,4000,6000,4000,4000,4000,5000,4000,4000
										,4000,4000,3500,5000,5000);
		} else if("brosLicenseEHanyoung".equals(sheetTitle)) {
			
			headerNameList = Arrays.asList("건수일련번호","고객명","주문번호","주문일자","송장번호","수출대행자 상호","수출대행자 통관고유부호","수출자 상호(한글)","수출자 대표자명(한글)"
										,"수출자 주소(한글)","수출자 우편번호","수출자 사업자등록번호","수출자 통관고유부호","건/중량(kg)","한국 HS CODE","품목/모델"
										,"목적국","구매자(영문)","포장갯수","판매수량","판매화폐","판매단가","판매소계","상품할인","판매합계","품목별 사은품여부","건별 무상거래여부"
										,"쿠폰&장바구니할인","운임","결제금액", "인도조건","결제방법","제조자 상호(한글)","제조사 사업자등록번호","제조사 통관고유부호");
			
			
			columnNameList = Arrays.asList("seq","declarationName","poNo","poDate","hblNo","agentName","agentCustomsId","shipperBusinessName","shipperBusinessPresident"
										,"shipperBusinessAddress","shipperBusinessZipcode","shipperBusinessId","shipperBusinessCustomsId","nwt","hsCode","detailItemName"
										,"destCountry","consigneeName","packingPieces","pieces","currencyCode","unitPriceCur","amountTotal","null","licensePayAmountCur","freegiftYn","freeTransYn"
										,"null","deliveryFeeLoc","licensePayAmount","incoterms","licensePayMethod","makerNameNone","makerBusinessId","makerCustomsId");
			
			alignTypeList = Arrays.asList(1,1,1,1,1,1,1,1,1
										,1,1,1,1,1,1,1
										,1,1,1,1,1,1,1,1,1,1,1
										,1,1,1,1,1,1,1,1);
			
			colSizeList = Arrays.asList(3500,3500,5000,4500,4500,4500,6500,5000,6500
										,10000,5500,6000,6000,4500,4500,4500
										,3500,4500,3500,3500,3500,3500,4000,5000,4000,4000,3500,3500
										,4500,3000,3000,5500,4000,4000,4000,4000);
		}
		
		rownum = 0;
		
		//헤더 세팅 
		int cellnum = 0;
		row = sheet.createRow(rownum);
		for(String headerName:headerNameList) {
			cell = row.createCell(cellnum);
			cell.setCellValue(headerName);
			cell.setCellStyle(comStyle.get("header_style"));
			cellnum++;
		}
		
		rownum ++;
		// 셀 길이 셋팅
		cellnum = 0;
		for(Integer colSize : colSizeList) {
			sheet.setColumnWidth(cellnum, colSize);
			cellnum++;
		}
		
		int ConsigneeAddressHeight = 0;
		for (int i = 0; i < datas.size(); i++, rownum++) {
			data = (Map<String, Object>)objectMapper.convertValue(datas.get(i), Map.class);
			row = sheet.createRow(rownum);
			cellnum = 0;
			for(String colName : columnNameList) {
				cell = row.createCell(cellnum);
				String value;
				value = Util.null2str(data.get(colName));
				switch(alignTypeList.get(cellnum)){
					case 0 :
						cell.setCellStyle(comStyle.get("text_left_style"));
						break;
					case 1 :
						cell.setCellValue(value);
						cell.setCellValue(value);
						cell.setCellStyle(comStyle.get("text_left_style"));
						break;
					case 2 :
						cell.setCellValue(value);
						cell.setCellStyle(comStyle.get("text_center_style"));
						break;
					case 3 :
						cell.setCellValue(value);
						cell.setCellStyle(comStyle.get("text_right_style"));
						break;
				}
				
				cellnum++;
			}
			
			row.setHeight((short) Math.max(ConsigneeAddressHeight*20 , 300));
		}
		
		// Write the output to a file
		String currDtm = DateTimeUtil.getDate("yyyyMMddHHmmss");
        
        String saveFile = frontFileName + "_" + currDtm + ".xls";													// 임시파일명
    	if(Common.checkSaveFilenameForExcel(frontFileName)) {
			String uuid = UUID.randomUUID().toString();
            saveFile = uuid + "_" + currDtm + ".xls";
		}
        String originFile = frontFileName + "_" + currDtm.substring(0,4) + "-" + currDtm.substring(4,6) + "-" + currDtm.substring(6,8) + ".xls";	// 원본파일명
        
        if(wb instanceof XSSFWorkbook) {
        	saveFile += "x";
        	originFile += "x";
        }
        
        //workbook파일 출력
        FileUtil.fileWrite(wb, excelDir, saveFile);
        
    	// Return값 [0]: 저장된 파일명, [1]: 다운로드받을 파일명
        files[0] = saveFile;
        files[1] = originFile;
        
		return files;
	}

	/**
	 * 엑셀 파일 생성
	 * @param excelType
	 * @param dataListObj
	 * @param columns
	 * @param titles
	 * @param sheetTitle
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String[] makeDailyReportExcel(
			String excelType, Map<String, Object> searchMap, List<String> columns, List<String> excelTypes,
			List<Integer> numFormats, List<String> titles, String sheetTitle, String frontFileName,
			String userName, String dateFormat) throws Exception {

		String excelDir = staticCustomProperties.getExcelDownload();

		// Return값 [0]: 실제 파일 저장경로, [1]: 다운로드받을 파일명
		String[] files = new String[2];
		FileInputStream file= null;
		Workbook wb;
		Sheet sheet;
		if(!Util.null2str(searchMap.get("saveFile")).equals("") ){  // 엑셀파일이 있는경우 다음 sheet 생성하여 데이터 적재
			file = new FileInputStream(searchMap.get("saveFile").toString());

			if("xls".equals(Util.null2str(excelType))) 	wb = new HSSFWorkbook(file);
			else 										wb = new XSSFWorkbook(file);

			sheet = wb.createSheet(sheetTitle.replaceAll("/", "") +"("+searchMap.get("sheetNo").toString()+")");
			sheet = wb.getSheetAt(Integer.parseInt(searchMap.get("sheetNo").toString())); // 시트정보읽어온다
		}
		else{
			if("xls".equals(Util.null2str(excelType))) 	wb = new HSSFWorkbook();
			else 										wb = new XSSFWorkbook();

			sheet = wb.createSheet(sheetTitle.replaceAll("/", "")); // 시트생성
		}

		DataFormat df = wb.createDataFormat();


		Font headerFont = wb.createFont();
		headerFont.setBold(true);
		headerFont.setFontName("Arial");
		headerFont.setFontHeightInPoints((short)10);
		headerFont.setColor(IndexedColors.WHITE.getIndex());
		Font titleFont = wb.createFont();
		titleFont.setBold(true);
		titleFont.setUnderline(HSSFFont.U_SINGLE);
		titleFont.setFontName("Arial");
		titleFont.setFontHeightInPoints((short)16);
		Font font4 = wb.createFont();
		font4.setFontName("Arial");
		font4.setFontHeightInPoints((short)10);


		Map<String, CellStyle> comStyle = new HashMap<String, CellStyle>();
		String[] styleTitleArr = {"system","system_value","title","header_right","header_left","header"};
		for(String styleTitle : styleTitleArr){
			ExcelStyles.createStyles(wb, comStyle, styleTitle, headerFont, titleFont, font4, df);
		}

		List<Map<String, CellStyle>> styleList = new ArrayList<Map<String, CellStyle>>();

		for(int i = 0; i < titles.size(); i++) {
			Map<String, CellStyle> style = new HashMap<String, CellStyle>();
			styleList.add(style);
		}

		PrintSetup printSetup = sheet.getPrintSetup();
		setPaperSize(printSetup, PrintSetup.A4_PAPERSIZE, true);
		sheet.setAutobreaks(true); // Option Button Move

		boolean headerYn = true;
		//요율관리에서는 해더안나오게 표현하기 위함
		if("agent_charge_list".equals(frontFileName) || "Correction_Upload_List".equals(frontFileName)) {
			headerYn = false;
		}

		int rownum = 0;
		//지정창고반출요청(해상)일때 헤더 다르게 주기 위해 분기시킴
		if(frontFileName.equals("Manifest_List")) {
			if(headerYn) {
				for(int i = 0; i < 3; i++) {
					Row sysInfoRow = sheet.createRow(i);
					rownum = i;
					sysInfoRow.setHeight((short) 400);
					Cell sysInfoTitleCell = sysInfoRow.createCell(0);
					Cell sysInfoValueCell = null;
					switch(i) {
						case 0 :
							sysInfoTitleCell.setCellValue("System");
							sysInfoValueCell = sysInfoRow.createCell(1);
							sysInfoValueCell.setCellValue("특송적하/목록자료조회(OI)");
							break;
						case 1 :
							sysInfoTitleCell.setCellValue("Writer");
							sysInfoValueCell = sysInfoRow.createCell(1);
							sysInfoValueCell.setCellValue(userName);
							break;
						case 2 :
							sysInfoTitleCell.setCellValue("Date");
							sysInfoValueCell = sysInfoRow.createCell(1);
							sysInfoValueCell.setCellValue(DateTimeUtil.getDate(dateFormat));
							break;
						default :
							sysInfoTitleCell.setCellValue("");
							sysInfoValueCell = sysInfoRow.createCell(1);
							sysInfoValueCell.setCellValue("");
					}

					sysInfoTitleCell.setCellStyle(comStyle.get("system"));
					sysInfoValueCell.setCellStyle(comStyle.get("system_value"));

					for (int j = 2, n = titles.size(); j < n; j++) {
						sysInfoValueCell = sysInfoRow.createCell(j);
						sysInfoValueCell.setCellValue("");
						sysInfoValueCell.setCellStyle(comStyle.get("system_value"));
					}

					sheet.addMergedRegion(new CellRangeAddress(i, i, 1, titles.size()-1));
				}

				rownum++;

				Row titleRow = sheet.createRow(rownum);
				titleRow.setHeightInPoints(29.25f);
				Cell titleCell = titleRow.createCell(0);
				titleCell.setCellValue(sheetTitle);
				titleCell.setCellStyle(comStyle.get("title"));
				sheet.addMergedRegion(new CellRangeAddress(rownum, rownum, 0, titles.size()-1));

				rownum = rownum+2;
			}
		}else {
			if(headerYn) {
				for(int i = 0; i < 4; i++) {
					Row sysInfoRow = sheet.createRow(i);
					rownum = i;
					sysInfoRow.setHeight((short) 400);
					Cell sysInfoTitleCell = sysInfoRow.createCell(0);
					Cell sysInfoValueCell = null;
					switch(i) {
						case 0 :
							sysInfoTitleCell.setCellValue("Program");
							sysInfoValueCell = sysInfoRow.createCell(1);
							sysInfoValueCell.setCellValue("Express System");
							break;
						case 1 :
							sysInfoTitleCell.setCellValue("System");
							sysInfoValueCell = sysInfoRow.createCell(1);
							sysInfoValueCell.setCellValue(sheetTitle);
							break;
						case 2 :
							sysInfoTitleCell.setCellValue("Writer");
							sysInfoValueCell = sysInfoRow.createCell(1);
							sysInfoValueCell.setCellValue(userName);
							break;
						case 3 :
							sysInfoTitleCell.setCellValue("Date");
							sysInfoValueCell = sysInfoRow.createCell(1);
							sysInfoValueCell.setCellValue(DateTimeUtil.getDate(dateFormat));
							break;
						default :
							sysInfoTitleCell.setCellValue("");
							sysInfoValueCell = sysInfoRow.createCell(1);
							sysInfoValueCell.setCellValue("");
					}

					sysInfoTitleCell.setCellStyle(comStyle.get("system"));
					sysInfoValueCell.setCellStyle(comStyle.get("system_value"));

					for (int j = 2, n = titles.size(); j < n; j++) {
						sysInfoValueCell = sysInfoRow.createCell(j);
						sysInfoValueCell.setCellValue("");
						sysInfoValueCell.setCellStyle(comStyle.get("system_value"));
					}

					sheet.addMergedRegion(new CellRangeAddress(i, i, 1, titles.size()-1));
				}

				rownum++;

				Row titleRow = sheet.createRow(rownum);
				titleRow.setHeightInPoints(29.25f);
				Cell titleCell = titleRow.createCell(0);
				titleCell.setCellValue(sheetTitle);
				titleCell.setCellStyle(comStyle.get("title"));
				sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, titles.size()-1));

				rownum = rownum+2;
			}
		}

		//the header row: centered text
		Row headerRow = sheet.createRow(rownum);
		headerRow.setHeight((short) 300);
		headerRow.setHeightInPoints(15.00f);

		Cell cell;
		boolean isTotalStyleType = false; // 합계 표현용 ROW 추가 여부
		CreationHelper factory = wb.getCreationHelper();
		Drawing<?> drawing = sheet.createDrawingPatriarch();
		// When the comment box is visible, have it show in a 1x3 space
		ClientAnchor anchor = factory.createClientAnchor();
		Comment comment;
		RichTextString memoStr;

		//해더셋팅
		List<Map<String, Object>> memosList = (List<Map<String, Object>>)searchMap.get("memos");
		for (int i = 0, n = titles.size(); i < n; i++) {
			cell = headerRow.createCell(i);
			cell.setCellValue(Util.null2str(titles.get(i)).replaceAll("<br/>", " ").replaceAll("<br>", " ").replaceAll("<br />", " "));

			if(memosList !=null && memosList.size() > i) {
				String memo = Util.null2str(memosList.get(i));
				//해더에 메모추가 시작
				if(!memo.equals("")){
					anchor.setCol1(cell.getColumnIndex());
					anchor.setCol2(cell.getColumnIndex()+2);
					anchor.setRow1(headerRow.getRowNum());
					anchor.setRow2(headerRow.getRowNum()+12);
					comment = drawing.createCellComment(anchor);
					memoStr = factory.createRichTextString(memo);
					comment.setString(memoStr);
					cell.setCellComment(comment);
				}
				//해더에 메모추가 끝
			}

			if((i+1) == n) {
				cell.setCellStyle(comStyle.get("header_right"));
			} else if(i == 0) {
				cell.setCellStyle(comStyle.get("header_left"));
			} else {
				cell.setCellStyle(comStyle.get("header"));
			}
			if(!Util.null2str(columns.get(i)).equals("num") && ("number".equals(Util.null2str(excelTypes.get(i), "left")) || "double".equals(Util.null2str(excelTypes.get(i), "left")))) isTotalStyleType = true;			// 하단의 합계 부분 추가 여부
		}
		if(frontFileName.equals("Daily performance") || frontFileName.equals("CollectPaymentAccount_List") || frontFileName.equals("CollectPaymentBalance_List")
				|| frontFileName.equals("agent_charge_list") ){
			isTotalStyleType = false;
		}

		if("WORK_REPORT".equals(searchMap.get("reportType")) || "CARRYIN_REPORT".equals(searchMap.get("reportType")) || "CARRYOUT_REPORT".equals(searchMap.get("reportType")) || "PERIOD_REPORT".equals(searchMap.get("reportType")) ) {
			isTotalStyleType = false;
		}
		
		//요율관리일때는 해더고정안함
		if(!frontFileName.equals("agent_charge_list") && !frontFileName.equals("Correction_Upload_List")) {
			//freeze the first row
			sheet.createFreezePane(0, 7);
		}

		//지정창고반출요청일때는 6행(헤더) 고정
		if(frontFileName.equals("Manifest_List")) {
			//freeze the first row
			sheet.createFreezePane(0, 6);
		}

		Row row;
		rownum++;
		List<Object> datas = (List<Object>)searchMap.get("data");

		Map<String, CellStyle> styles;
		String styleType = "";
		String styleName = "";
		int numFormat = 0;
		int titleSize = titles.size();
		int totalAddRow = isTotalStyleType ? 1 : 0;
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> data = new HashMap<String, Object>();
		for (int i = 0, n = datas.size() + totalAddRow; i < n; i++, rownum++) {
			row = sheet.createRow(rownum);
			row.setHeight((short) 300);

			if(isTotalStyleType && (i+1) == n){
				// 합계 표현용 마지막 ROW 추가
				data.clear();
				data.put("num", "TOTAL");
			}else{
				data = (Map<String, Object>)objectMapper.convertValue(datas.get(i), Map.class);
			}

			String columnName = "";
			for(int j = 0; j < titleSize; j++) {
				cell = row.createCell(j);
				columnName = Util.null2str(columns.get(j));

				// Cell Style Setting
				styles = styleList.get(j);
				styleType = Util.null2str(excelTypes.get(j), "left");
				numFormat = Util.null2int(numFormats.get(j));
				/** NUM컬럼의 순서를 변경 후 엑셀 다운로드시 오류 발생해서 수정 */
//        		if(isTotalStyleType && (i+1) == n && j == 0){
				if(isTotalStyleType && (i+1) == n && "num".equals(columnName)){
					styleType = "center";
				}
				String defaultFormat = "#,##0";
				switch(styleType) {
					case "left" :
						if((i+1) == n) {
							if(j == 0)					styleName = "cell_normal_left_bottom";
							else if((j+1) == titleSize)	styleName = "cell_normal_right_bottom";
							else						styleName = "cell_normal_bottom";
						} else {
							if(j == 0)					styleName = "cell_normal_left";
							else if((j+1) == titleSize)	styleName = "cell_normal_right";
							else						styleName = "cell_normal";
						}

						if(styles.get(styleName) == null){
							ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df);
						}
						cell.setCellStyle(styles.get(styleName));
						cell.setCellValue(Util.null2str(data.get(columnName)));
						break;
					case "center" :
						if((i+1) == n) {
							if(j == 0)					styleName = "cell_normal_centered_left_bottom";
							else if((j+1) == titleSize)	styleName = "cell_normal_centered_right_bottom";
							else						styleName = "cell_normal_centered_bottom";
						} else {
							if(j == 0)					styleName = "cell_normal_centered_left";
							else if((j+1) == titleSize)	styleName = "cell_normal_centered_right";
							else						styleName = "cell_normal_centered";
						}

						if(styles.get(styleName) == null){
							ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df);
						}
						cell.setCellStyle(styles.get(styleName));
						cell.setCellValue(Util.null2str(data.get(columnName)));

						break;
					case "right" :
						if((i+1) == n) {
							if(j == 0)					styleName = "cell_right_left_bottom";
							else if((j+1) == titleSize)	styleName = "cell_right_right_bottom";
							else						styleName = "cell_right_bottom";
						} else {
							if(j == 0)					styleName = "cell_right_left";
							else if((j+1) == titleSize)	styleName = "cell_right_right";
							else						styleName = "cell_right";
						}

						if(styles.get(styleName) == null){
							ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df);
						}
						cell.setCellStyle(styles.get(styleName));
						cell.setCellValue(Util.null2str(data.get(columnName)));

						break;
					case "number" :
						if((i+1) == n) {
							if(j == 0)					styleName = "cell_normal_number_left_bottom";
							else if((j+1) == titleSize)	styleName = "cell_normal_number_right_bottom";
							else						styleName = "cell_normal_number_bottom";
						} else {
							if(j == 0)					styleName = "cell_normal_number_left";
							else if((j+1) == titleSize)	styleName = "cell_normal_number_right";
							else						styleName = "cell_normal_number";
						}

						if(styles.get(styleName) == null){
							ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df);

							if(numFormat > 0) {
								defaultFormat += ".";
								for(int k = 0; k < numFormat; k++) {
									defaultFormat += "0";
								}
							}else if(numFormat != 0){
								String[] digits = Util.null2str(data.get(columnName)).split("\\.");
								if(digits.length > 1) {
									defaultFormat += ".";
									for(int k = 0; k < digits[1].length(); k++) {
										defaultFormat += "0";
									}
								}
							}
							styles.get(styleName).setDataFormat(df.getFormat(defaultFormat));
						}
						cell.setCellStyle(styles.get(styleName));

						// 경영정보 > collect payment 엑셀 셀병합 처리 2020.06.08 sophie
						if( frontFileName.equals("CollectPaymentAccount_List") ) {
							cell.setCellValue((Util.null2int(data.get(columnName)) / 4) + 1);

						}else {
							cell.setCellValue(Util.null2int(data.get(columnName)));
						}

						// NUMBER 마지막 ROW 합계 수식 추가
						if(isTotalStyleType && (i+1) == n) {
							//cell.setCellType(CellType.FORMULA);
							cell.setCellFormula("SUM("
									+ CellReference.convertNumToColString(cell.getColumnIndex()).concat(Integer.toString(8))
									+ ":"
									+ CellReference.convertNumToColString(cell.getColumnIndex()).concat(Integer.toString((cell.getRow().getRowNum())))
									+ ")");
						}

						break;
					case "double" :
						if((i+1) == n) {
							if(j == 0)					styleName = "cell_normal_double_left_bottom";
							else if((j+1) == titleSize)	styleName = "cell_normal_double_right_bottom";
							else						styleName = "cell_normal_double_bottom";
						} else {
							if(j == 0)					styleName = "cell_normal_double_left";
							else if((j+1) == titleSize)	styleName = "cell_normal_double_right";
							else						styleName = "cell_normal_double";
						}

						if(styles.get(styleName) == null){
							ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df);

							if(numFormat > 0) {
								defaultFormat += ".";
								for(int k = 0; k < numFormat; k++) {
									defaultFormat += "0";
								}
							}else if(numFormat != 0){
								String[] digits = Util.null2str(data.get(columnName)).split("\\.");
								if(digits.length > 1) {
									defaultFormat += ".";
									for(int k = 0; k < digits[1].length(); k++) {
										defaultFormat += "0";
									}
								}
							}
							styles.get(styleName).setDataFormat(df.getFormat(defaultFormat));
						}
						cell.setCellStyle(styleList.get(j).get(styleName));
						cell.setCellValue(Util.null2dbl(data.get(columnName)));

						// DOUBLE 마지막 ROW 합계 수식 추가
						if(isTotalStyleType && (i+1) == n){
							//cell.setCellType(CellType.FORMULA);
							cell.setCellFormula("SUM("
									+ CellReference.convertNumToColString(cell.getColumnIndex()).concat(Integer.toString(8))
									+ ":"
									+ CellReference.convertNumToColString(cell.getColumnIndex()).concat(Integer.toString((cell.getRow().getRowNum())))
									+ ")");
						}

						break;
					case "date" :
						if((i+1) == n) {
							if(j == 0)					styleName = "cell_normal_date_left_bottom";
							else if((j+1) == titleSize)	styleName = "cell_normal_date_right_bottom";
							else						styleName = "cell_normal_date_bottom";
						} else {
							if(j == 0)					styleName = "cell_normal_date_left";
							else if((j+1) == titleSize)	styleName = "cell_normal_date_right";
							else						styleName = "cell_normal_date";
						}
						if(styles.get(styleName) == null){
							ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df);
						}
						cell.setCellStyle(styles.get(styleName));
						try{
							long time = (long)data.get(columnName);
							cell.setCellValue(DateTimeUtil.ConvertDate2String(new Date(time), new SimpleDateFormat(dateFormat)));
						}catch(Exception e){
							cell.setCellValue("");
						}

						break;
					case "percent" :
						if((i+1) == n) {
							if(j == 0)					styleName = "cell_normal_left_bottom";
							else if((j+1) == titleSize)	styleName = "cell_normal_right_bottom";
							else						styleName = "cell_normal_bottom";
						} else {
							if(j == 0)					styleName = "cell_normal_left";
							else if((j+1) == titleSize)	styleName = "cell_normal_right";
							else						styleName = "cell_normal";
						}
						try{
							if(styles.get(styleName) == null){
								ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df);
							}
							styles.get(styleName).setDataFormat(df.getFormat("0.00%"));
							cell.setCellStyle(styles.get(styleName));
							cell.setCellValue(Util.null2dbl(data.get(columnName)));
						}catch(Exception e){
							cell.setCellValue("");
						}

						break;
					default :
						if((i+1) == n) {
							if(j == 0)					styleName = "cell_normal_left_bottom";
							else if((j+1) == titleSize)	styleName = "cell_normal_right_bottom";
							else						styleName = "cell_normal_bottom";
						} else {
							if(j == 0)					styleName = "cell_normal_left";
							else if((j+1) == titleSize)	styleName = "cell_normal_right";
							else						styleName = "cell_normal";
						}
						if(styles.get(styleName) == null){
							ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df);
						}
						cell.setCellStyle(styles.get(styleName));
						cell.setCellValue(Util.null2str(data.get(columnName)));
				}
			}
		}

		//일일보고 - 근무일지
		if(searchMap.get("reportType").equals("WORK_REPORT")) {
			//헤더병합
			sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 1));
			sheet.addMergedRegion(new CellRangeAddress(5, 5, 2, 4));
			sheet.addMergedRegion(new CellRangeAddress(5, 5, 5, 7));
			sheet.addMergedRegion(new CellRangeAddress(5, 5, 8, 10));
			sheet.addMergedRegion(new CellRangeAddress(5, 5, 11, 13));

			Row headerRow1 = sheet.createRow(5);
			// 첫 번째 줄 헤더 값 설정
			String[] headers1 = {"구분","", "전일재고","","", "금일반입","","", "금일반출","","", "금일재고","",""};
			for (int i = 0; i < headers1.length; i++) {
				Cell tmpCell = headerRow1.createCell(i);
				tmpCell.setCellValue(headers1[i]);
				tmpCell.setCellStyle(comStyle.get("header"));
			}

			//1행병합
			sheet.addMergedRegion(new CellRangeAddress(7, 9, 0, 0));
			sheet.addMergedRegion(new CellRangeAddress(11, 14, 0, 0));

		//일일보고 - 반입보고/반출보고
		}else if(searchMap.get("reportType").equals("CARRYIN_REPORT") || searchMap.get("reportType").equals("CARRYOUT_REPORT")) {

			//1행병합
			sheet.addMergedRegion(new CellRangeAddress(7, 9, 0, 0));
			sheet.addMergedRegion(new CellRangeAddress(10, 13, 0, 0));
			sheet.addMergedRegion(new CellRangeAddress(14, 21, 0, 0));
			sheet.addMergedRegion(new CellRangeAddress(22, 29, 0, 0));
			sheet.addMergedRegion(new CellRangeAddress(30, 36, 0, 0));
			sheet.addMergedRegion(new CellRangeAddress(37, 40, 0, 0));
			sheet.addMergedRegion(new CellRangeAddress(41, 50, 0, 0));

		//일일보고 - 기간별반출입
		}else if(searchMap.get("reportType").equals("PERIOD_REPORT")) {
			sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 1));
			sheet.addMergedRegion(new CellRangeAddress(5, 5, 3, 8));
			sheet.addMergedRegion(new CellRangeAddress(5, 5, 9, 14));

			int listTotal = datas.size()/3;
			for (int i = 1; i <= listTotal; i++) {
				sheet.addMergedRegion(new CellRangeAddress(i*3 + 4, i*3 + 6, 0,0));
			}

			Row headerRow1 = sheet.createRow(5);
			// 첫 번째 줄 헤더 값 설정
			String[] headers1 = {"구분","","전일재고","금일반입","","","","","", "금일반출","","","","","", "금일재고"};
			for (int i = 0; i < headers1.length; i++) {
				Cell tmpCell = headerRow1.createCell(i);
				tmpCell.setCellValue(headers1[i]);
				tmpCell.setCellStyle(comStyle.get("header"));
			}
		}

		List<Integer> columnsWidth = (List<Integer>)searchMap.get("columnsWidth");
		for(int i = 0; i < titleSize; i++) {
			sheet.setColumnWidth(i, Util.null2int(columnsWidth.get(i)) * 36);
		}

		if(Util.null2str(searchMap.get("saveFile")).equals("") ){ // 엑셀파일 생성

			// Write the output to a file
			String currDtm = DateTimeUtil.getDate("yyyyMMddHHmmss");

			String saveFile = frontFileName + "_" + currDtm + ".xls";													// 임시파일명
			if(Common.checkSaveFilenameForExcel(frontFileName)) {
				String uuid = UUID.randomUUID().toString();
				saveFile = uuid + "_" + currDtm + ".xls";
			}
			String originFile = frontFileName + "_" + currDtm.substring(0,4) + "-" + currDtm.substring(4,6) + "-" + currDtm.substring(6,8) + ".xls";	// 원본파일명

			if(wb instanceof XSSFWorkbook) {
				saveFile += "x";
				originFile += "x";
			}

			//workbook파일 출력
			FileUtil.fileWrite(wb, excelDir, saveFile);

			// Return값 [0]: 저장된 파일명, [1]: 다운로드받을 파일명
			files[0] = saveFile;
			files[1] = originFile;
		}
		else{
			String logVal = searchMap.get("saveFile").toString() +  "  " + searchMap.get("originFile").toString();
			log.debug(logVal);
			FileUtil.fileWrite(wb, "", searchMap.get("saveFile").toString());

			files[0] = searchMap.get("saveFile").toString();
			files[1] = searchMap.get("originFile").toString();

			file.close();
		}

		return files;
	}

	/**
	 * 엑셀 파일 생성
	 * @param excelType
	 * @param dataListObj
	 * @param columns
	 * @param titles
	 * @param sheetTitle
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String[] makeSelectionExcel(
			String excelType, Map<String, Object> searchMap, List<String> columns, List<String> excelTypes, 
			List<Integer> numFormats, List<String> titles, String sheetTitle, String frontFileName, 
			String userName, String dateFormat) throws Exception {
		
		String excelDir = staticCustomProperties.getExcelDownload();
		
		// Return값 [0]: 실제 파일 저장경로, [1]: 다운로드받을 파일명
        String[] files = new String[2];
        FileInputStream file= null;
		Workbook wb;
		Sheet sheet;
		if(!Util.null2str(searchMap.get("saveFile")).equals("") ){  // 엑셀파일이 있는경우 다음 sheet 생성하여 데이터 적재 
			file = new FileInputStream(searchMap.get("saveFile").toString()); 
						
			if("xls".equals(Util.null2str(excelType))) 	wb = new HSSFWorkbook(file);
	        else 										wb = new XSSFWorkbook(file);
			
			sheet = wb.createSheet(sheetTitle.replaceAll("/", "") +"("+searchMap.get("sheetNo").toString()+")");
			sheet = wb.getSheetAt(Integer.parseInt(searchMap.get("sheetNo").toString())); // 시트정보읽어온다
		}
		else{ 
			if("xls".equals(Util.null2str(excelType))) 	wb = new HSSFWorkbook();
	        else 										wb = new XSSFWorkbook();
			
			sheet = wb.createSheet(sheetTitle.replaceAll("/", "")); // 시트생성
		}
		
		DataFormat df = wb.createDataFormat();		
		
		
		Font headerFont = wb.createFont();
		headerFont.setBold(true);
		headerFont.setFontName("Arial");
		headerFont.setFontHeightInPoints((short)10);
		headerFont.setColor(IndexedColors.WHITE.getIndex());
		Font titleFont = wb.createFont();
		titleFont.setBold(true);
		titleFont.setUnderline(HSSFFont.U_SINGLE); 
		titleFont.setFontName("Arial");
		titleFont.setFontHeightInPoints((short)16);
		Font font4 = wb.createFont();
		font4.setFontName("Arial");
		font4.setFontHeightInPoints((short)10);
		
		
		Map<String, CellStyle> comStyle = new HashMap<String, CellStyle>();
		String[] styleTitleArr = {"system","system_value","title","header_right","header_left","header"};
		for(String styleTitle : styleTitleArr){
			ExcelStyles.createStyles(wb, comStyle, styleTitle, headerFont, titleFont, font4, df);
		}
		
		List<Map<String, CellStyle>> styleList = new ArrayList<Map<String, CellStyle>>();
		
		for(int i = 0; i < titles.size(); i++) {
			Map<String, CellStyle> style = new HashMap<String, CellStyle>();
			styleList.add(style);
		}
		
		PrintSetup printSetup = sheet.getPrintSetup();
		setPaperSize(printSetup, PrintSetup.A4_PAPERSIZE, true);
		sheet.setAutobreaks(true); // Option Button Move
		
		boolean headerYn = true;
		
		int rownum = 0;
		if(headerYn) {
			for(int i = 0; i < 4; i++) {
				Row sysInfoRow = sheet.createRow(i);
				rownum = i;
				sysInfoRow.setHeight((short) 400);
				Cell sysInfoTitleCell = sysInfoRow.createCell(0);
				Cell sysInfoValueCell = null;
				switch(i) {
				case 0 :
					sysInfoTitleCell.setCellValue("Program");
					sysInfoValueCell = sysInfoRow.createCell(1);
					sysInfoValueCell.setCellValue("Express System");
					break;
				case 1 :
					sysInfoTitleCell.setCellValue("System");
					sysInfoValueCell = sysInfoRow.createCell(1);
					sysInfoValueCell.setCellValue("통관목록 자체선별");
					break;
				case 2 :
					sysInfoTitleCell.setCellValue("Writer");
					sysInfoValueCell = sysInfoRow.createCell(1);
					sysInfoValueCell.setCellValue(userName);
					break;
				case 3 :
					sysInfoTitleCell.setCellValue("Date");
					sysInfoValueCell = sysInfoRow.createCell(1);
					sysInfoValueCell.setCellValue(DateTimeUtil.getDate(dateFormat));
					break;
				default :
					sysInfoTitleCell.setCellValue("");
					sysInfoValueCell = sysInfoRow.createCell(1);
					sysInfoValueCell.setCellValue("");
				}
	
				sysInfoTitleCell.setCellStyle(comStyle.get("system"));
				sysInfoValueCell.setCellStyle(comStyle.get("system_value"));
				
				for (int j = 2, n = titles.size(); j < n; j++) {
					sysInfoValueCell = sysInfoRow.createCell(j);
					sysInfoValueCell.setCellValue("");
					sysInfoValueCell.setCellStyle(comStyle.get("system_value"));
				}
	
				sheet.addMergedRegion(new CellRangeAddress(i, i, 1, titles.size()-1));
			}
			
			rownum++;

			Row titleRow = sheet.createRow(rownum);
			titleRow.setHeightInPoints(29.25f);
			Cell titleCell = titleRow.createCell(0);
			titleCell.setCellValue("통관목록 자체선별");
			titleCell.setCellStyle(comStyle.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, titles.size()-1));
			
			rownum = rownum+2;
		}
	
        //the header row: centered text
        Row headerRow = sheet.createRow(rownum);
        headerRow.setHeight((short) 300);
        headerRow.setHeightInPoints(15.00f);

        Cell cell;
        boolean isTotalStyleType = false; // 합계 표현용 ROW 추가 여부
        CreationHelper factory = wb.getCreationHelper();
		Drawing<?> drawing = sheet.createDrawingPatriarch();
		// When the comment box is visible, have it show in a 1x3 space
		ClientAnchor anchor = factory.createClientAnchor();
		Comment comment;
		RichTextString memoStr;
        
		//해더셋팅
		List<Map<String, Object>> memosList = (List<Map<String, Object>>)searchMap.get("memos");
        for (int i = 0, n = titles.size(); i < n; i++) {
            cell = headerRow.createCell(i);
            cell.setCellValue(Util.null2str(titles.get(i)).replaceAll("<br/>", " ").replaceAll("<br>", " ").replaceAll("<br />", " "));
		                
            if(memosList !=null && memosList.size() > i) {
            	String memo = Util.null2str(memosList.get(i));
            	//해더에 메모추가 시작
                if(!memo.equals("")){
    				anchor.setCol1(cell.getColumnIndex());
    				anchor.setCol2(cell.getColumnIndex()+2);
    				anchor.setRow1(headerRow.getRowNum());
    				anchor.setRow2(headerRow.getRowNum()+12);
    				comment = drawing.createCellComment(anchor);    				
    				memoStr = factory.createRichTextString(memo);
    				comment.setString(memoStr);
    				cell.setCellComment(comment);
    			}     
                //해더에 메모추가 끝
            }            
            
            if((i+1) == n) {
            	cell.setCellStyle(comStyle.get("header_right"));
            } else if(i == 0) {
            	cell.setCellStyle(comStyle.get("header_left"));
            } else {
            	cell.setCellStyle(comStyle.get("header"));
            }
            if(!Util.null2str(columns.get(i)).equals("num") && ("number".equals(Util.null2str(excelTypes.get(i), "left")) || "double".equals(Util.null2str(excelTypes.get(i), "left")))) isTotalStyleType = true;			// 하단의 합계 부분 추가 여부
        }
		isTotalStyleType = false;
        
        Row row;
        rownum++;
        List<Object> datas = (List<Object>)searchMap.get("data");

        Map<String, CellStyle> styles;
        String styleType = "";
		String styleName = "";
        int numFormat = 0;
    	int titleSize = titles.size();
    	int totalAddRow = isTotalStyleType ? 1 : 0;
		ObjectMapper objectMapper = new ObjectMapper();
    	Map<String, Object> data = new HashMap<String, Object>();
    	Map<String, Object> itemData = new HashMap<String, Object>();
        for (int i = 0, n = datas.size() + totalAddRow; i < n; i++, rownum++) {
        	row = sheet.createRow(rownum);
        	row.setHeight((short) 300);
        	
        	if(isTotalStyleType && (i+1) == n){
        		// 합계 표현용 마지막 ROW 추가
        		data.clear();
        		data.put("num", "TOTAL");
        	}else{
    			data = (Map<String, Object>)objectMapper.convertValue(datas.get(i), Map.class);
    			itemData = (Map<String, Object>)objectMapper.convertValue(data.get("selectionItem"), Map.class); 
        	}
        	
        	String columnName = "";
        	boolean itemFlag = false; // 세부품명 표현 시작점
        	for(int j = 0; j < titleSize; j++) {
        		cell = row.createCell(j);
        		columnName = Util.null2str(columns.get(j));
        		
        		// Cell Style Setting
        		styles = styleList.get(j);
        		styleType = Util.null2str(excelTypes.get(j), "left");
        		numFormat = Util.null2int(numFormats.get(j));
        		/** NUM컬럼의 순서를 변경 후 엑셀 다운로드시 오류 발생해서 수정 */
//        		if(isTotalStyleType && (i+1) == n && j == 0){
        		if(isTotalStyleType && (i+1) == n && "num".equals(columnName)){
        			styleType = "center";
        		}
        		/** hsCode ~ 끝까지 세부품명 항목 */
        		if("hsCode".equals(columnName)) itemFlag = true;
        		if(itemFlag && itemData == null) break;
        		
        		String defaultFormat = "#,##0";
        		switch(styleType) {
        			case "left" :
        				if((i+1) == n) {
        					if(j == 0)					styleName = "cell_normal_left_bottom";
        					else if((j+1) == titleSize)	styleName = "cell_normal_right_bottom";
        					else						styleName = "cell_normal_bottom";
        				} else {
        					if(j == 0)					styleName = "cell_normal_left";
        					else if((j+1) == titleSize)	styleName = "cell_normal_right";
        					else						styleName = "cell_normal";
        				}
        				if(styles.get(styleName) == null){
        					ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df);
        				}
        				cell.setCellStyle(styles.get(styleName));
                		cell.setCellValue(itemFlag ? Util.null2str(itemData.get(columnName)) : Util.null2str(data.get(columnName)));
        				break;
        			case "center" :
        				if((i+1) == n) {
        					if(j == 0)					styleName = "cell_normal_centered_left_bottom";
        					else if((j+1) == titleSize)	styleName = "cell_normal_centered_right_bottom";
        					else						styleName = "cell_normal_centered_bottom";
        				} else {
        					if(j == 0)					styleName = "cell_normal_centered_left";
        					else if((j+1) == titleSize)	styleName = "cell_normal_centered_right";
        					else						styleName = "cell_normal_centered";
        				}

        				if(styles.get(styleName) == null){
        					ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df);
        				}
        				cell.setCellStyle(styles.get(styleName));
                		cell.setCellValue(itemFlag ? Util.null2str(itemData.get(columnName)) : Util.null2str(data.get(columnName)));
                		
        				break;
        			case "right" :
        				if((i+1) == n) {
        					if(j == 0)					styleName = "cell_right_left_bottom";
        					else if((j+1) == titleSize)	styleName = "cell_right_right_bottom";
        					else						styleName = "cell_right_bottom";
        				} else {
        					if(j == 0)					styleName = "cell_right_left";
        					else if((j+1) == titleSize)	styleName = "cell_right_right";
        					else						styleName = "cell_right";
        				}
        				
        				if(styles.get(styleName) == null){
        					ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df);
        				}
        				cell.setCellStyle(styles.get(styleName));
                		cell.setCellValue(itemFlag ? Util.null2str(itemData.get(columnName)) : Util.null2str(data.get(columnName)));
                		
        				break;
        			case "number" :
        				if((i+1) == n) {
        					if(j == 0)					styleName = "cell_normal_number_left_bottom";
        					else if((j+1) == titleSize)	styleName = "cell_normal_number_right_bottom";
        					else						styleName = "cell_normal_number_bottom";
        				} else {
        					if(j == 0)					styleName = "cell_normal_number_left";
        					else if((j+1) == titleSize)	styleName = "cell_normal_number_right";
        					else						styleName = "cell_normal_number";
        				}
        				
        				if(styles.get(styleName) == null){
        					ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df);
        					
        					if(numFormat > 0) {
        			        	defaultFormat += ".";
        			        	for(int k = 0; k < numFormat; k++) {
        			        		defaultFormat += "0";
        			        	}
            				}else if(numFormat != 0){
            					String[] digits = Util.null2str(data.get(columnName)).split("\\.");
            					if(digits.length > 1) {
            			        	defaultFormat += ".";
            			        	for(int k = 0; k < digits[1].length(); k++) {
            			        		defaultFormat += "0";
            			        	}        						
            					}
            				}
        					styles.get(styleName).setDataFormat(df.getFormat(defaultFormat));
        				}
        				cell.setCellStyle(styles.get(styleName));
    					cell.setCellValue(itemFlag ? Util.null2int(itemData.get(columnName)) : Util.null2int(data.get(columnName)));
        				
                		// NUMBER 마지막 ROW 합계 수식 추가
                		if(isTotalStyleType && (i+1) == n) {
                			//cell.setCellType(CellType.FORMULA);
                			cell.setCellFormula("SUM(" 
                					+ CellReference.convertNumToColString(cell.getColumnIndex()).concat(Integer.toString(8))  
                					+ ":"
                					+ CellReference.convertNumToColString(cell.getColumnIndex()).concat(Integer.toString((cell.getRow().getRowNum()))) 
                					+ ")");
                		}
                		
        				break;
        			case "double" :
        				if((i+1) == n) {
        					if(j == 0)					styleName = "cell_normal_double_left_bottom";
        					else if((j+1) == titleSize)	styleName = "cell_normal_double_right_bottom";
        					else						styleName = "cell_normal_double_bottom";
        				} else {
        					if(j == 0)					styleName = "cell_normal_double_left";
        					else if((j+1) == titleSize)	styleName = "cell_normal_double_right";
        					else						styleName = "cell_normal_double";
        				}
        				
        				if(styles.get(styleName) == null){
        					ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df);
        					
        					if(numFormat > 0) {
        			        	defaultFormat += ".";
        			        	for(int k = 0; k < numFormat; k++) {
        			        		defaultFormat += "0";
        			        	}
            				}else if(numFormat != 0){
            					String[] digits = Util.null2str(data.get(columnName)).split("\\.");
            					if(digits.length > 1) {
            			        	defaultFormat += ".";
            			        	for(int k = 0; k < digits[1].length(); k++) {
            			        		defaultFormat += "0";
            			        	}        						
            					}
            				}
        					styles.get(styleName).setDataFormat(df.getFormat(defaultFormat));
        				}
        				cell.setCellStyle(styleList.get(j).get(styleName));
                		cell.setCellValue(itemFlag ? Util.null2dbl(itemData.get(columnName)) : Util.null2dbl(data.get(columnName)));
                		
                		// DOUBLE 마지막 ROW 합계 수식 추가
                		if(isTotalStyleType && (i+1) == n){
                			//cell.setCellType(CellType.FORMULA);
                			cell.setCellFormula("SUM(" 
                					+ CellReference.convertNumToColString(cell.getColumnIndex()).concat(Integer.toString(8))  
                					+ ":"
                					+ CellReference.convertNumToColString(cell.getColumnIndex()).concat(Integer.toString((cell.getRow().getRowNum()))) 
                					+ ")");
                		}
                		
        				break;
        			case "date" :
        				if((i+1) == n) {
        					if(j == 0)					styleName = "cell_normal_date_left_bottom";
        					else if((j+1) == titleSize)	styleName = "cell_normal_date_right_bottom";
        					else						styleName = "cell_normal_date_bottom";
        				} else {
        					if(j == 0)					styleName = "cell_normal_date_left";
        					else if((j+1) == titleSize)	styleName = "cell_normal_date_right";
        					else						styleName = "cell_normal_date";
        				}
        				if(styles.get(styleName) == null){
        					ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df);
        				}
        				cell.setCellStyle(styles.get(styleName));
    					try{
    						long time = 0;
	        		        if(itemFlag) {
	        		        	time = (long)itemData.get(columnName);
	        		        }else {
	        		        	time = (long)data.get(columnName);
	        		        }
	        		        cell.setCellValue(DateTimeUtil.ConvertDate2String(new Date(time), new SimpleDateFormat(dateFormat)));
    					}catch(Exception e){
    						cell.setCellValue("");
    					}
        				
        				break;
        			case "percent" :
        				if((i+1) == n) {
        					if(j == 0)					styleName = "cell_normal_left_bottom";
        					else if((j+1) == titleSize)	styleName = "cell_normal_right_bottom";
        					else						styleName = "cell_normal_bottom";
        				} else {
        					if(j == 0)					styleName = "cell_normal_left";
        					else if((j+1) == titleSize)	styleName = "cell_normal_right";
        					else						styleName = "cell_normal";
        				}
    					try{
    						if(styles.get(styleName) == null){
            					ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df);
            				}
    						styles.get(styleName).setDataFormat(df.getFormat("0.00%"));
    						cell.setCellStyle(styles.get(styleName));
            				cell.setCellValue(itemFlag ? Util.null2dbl(itemData.get(columnName)) : Util.null2dbl(data.get(columnName)));
    					}catch(Exception e){
    						cell.setCellValue("");
    					}
        				
        				break;
        			default :
        				if((i+1) == n) {
        					if(j == 0)					styleName = "cell_normal_left_bottom";
        					else if((j+1) == titleSize)	styleName = "cell_normal_right_bottom";
        					else						styleName = "cell_normal_bottom";
        				} else {
        					if(j == 0)					styleName = "cell_normal_left";
        					else if((j+1) == titleSize)	styleName = "cell_normal_right";
        					else						styleName = "cell_normal";
        				}
        				if(styles.get(styleName) == null){
        					ExcelStyles.createStyles(wb, styles, styleName, headerFont, titleFont, font4, df);
        				}
        				cell.setCellStyle(styles.get(styleName));
                		cell.setCellValue(itemFlag ? Util.null2str(itemData.get(columnName)) : Util.null2str(data.get(columnName)));
        		}
        	}
        }
        
        List<Integer> columnsWidth = (List<Integer>)searchMap.get("columnsWidth");
        for(int i = 0; i < titleSize; i++) {
        	sheet.setColumnWidth(i, Util.null2int(columnsWidth.get(i)) * 36);
        }
        
        if(Util.null2str(searchMap.get("saveFile")).equals("") ){ // 엑셀파일 생성
        	
        	// Write the output to a file
    		String currDtm = DateTimeUtil.getDate("yyyyMMddHHmmss");
            
            String saveFile = frontFileName + "_" + currDtm + ".xls";													// 임시파일명
        	if(Common.checkSaveFilenameForExcel(frontFileName)) {
    			String uuid = UUID.randomUUID().toString();
                saveFile = uuid + "_" + currDtm + ".xls";
    		}
            String originFile = frontFileName + "_" + currDtm.substring(0,4) + "-" + currDtm.substring(4,6) + "-" + currDtm.substring(6,8) + ".xls";	// 원본파일명
            
            if(wb instanceof XSSFWorkbook) {
            	saveFile += "x";
            	originFile += "x";
            }
            
            //workbook파일 출력
            FileUtil.fileWrite(wb, excelDir, saveFile);
            
        	// Return값 [0]: 저장된 파일명, [1]: 다운로드받을 파일명
            files[0] = saveFile;
            files[1] = originFile;
		}	
        else{
        	log.debug(searchMap.get("saveFile").toString() +  "  " + searchMap.get("originFile").toString());
        	FileUtil.fileWrite(wb, "", searchMap.get("saveFile").toString());
	        
	        files[0] = searchMap.get("saveFile").toString();
	        files[1] = searchMap.get("originFile").toString();
	        
	        file.close();
        }
        
        return files;
	}
	
	@SuppressWarnings("unchecked")
	public String[] makeUpsNewbillingExcel(String excelType, Map<String, Object> searchMap, String sheetTitle, String frontFileName, 
			String userName, String dateFormat) throws Exception{
		ObjectMapper objectMapper = new ObjectMapper();
		String excelDir = staticCustomProperties.getExcelDownload();
		String headerTitle;
		String[] files = new String[2];
        FileInputStream file= null;
		Workbook wb;
		Sheet sheet;
		Row row;
		Cell cell;
		
		List<String> headerNameList = null;
		List<String> columnNameList = null;
		List<Integer> alignTypeList = null;
		List<Integer> colSizeList = null;
		
		int rownum = 0;
		// Return값 [0]: 실제 파일 저장경로, [1]: 다운로드받을 파일명
		if("xls".equals(Util.null2str(excelType))) 	wb = new HSSFWorkbook();
	    else 										wb = new XSSFWorkbook();
			
		sheet = wb.createSheet(sheetTitle.replaceAll("/", "")); // 시트생성
		
		DataFormat df = wb.createDataFormat();
		
		PrintSetup print = sheet.getPrintSetup();
		print.setLandscape(true);// 가로인쇄
		print.setPaperSize(PrintSetup.A4_PAPERSIZE);// A4 인쇄
		
		Map<String, CellStyle> comStyle = ExcelStyles.createStyles(wb);
		List<Object> datas = (List<Object>)searchMap.get("data");
//		Map<String, Object> data = (Map<String, Object>)objectMapper.convertValue(datas.get(0), Map.class);
		Map<String, Object> data;

		headerNameList = Arrays.asList("업무구분","하우스빌번호","개인통관부호","항공사코드","편명","MRN","MSN","HSN","수하인주소_한글","수하인명_한글"
										,"용도구분","외화금액","마스터빌번호","입항일자","수량","중량","실제수량","은행","통관타입","거래구분"
										,"허용품목코드","URL","수하인명","수하인전화번호","대표품목","수하인주소","보세창고코드","화주명칭","화주국가코드","플랜여부"
										,"LC번호","TS_SEND_TYPE","개인통관부호 플랜여부","도착지코드","다운로드 일시","다운로드 사용자");
		
		columnNameList = Arrays.asList("jobType","hblNo","customsId","lineCode","vesselFlight","mrn","msn","hsn","consigneeAddressLoc","consigneeNameLoc"
										,"cdUseType","amountCur","mblNo","arDate","pieces","gwt","piecesReal","bankName","cdType","ecmType"
										,"allowedItemCode","url","consigneeName","consigneeTel","commodityItem", "consigneeAddress", "assignmentWhCode","shipperName","shipperCountryCode","planYn"
										,"lcNo","tsSendType","customsUpdateYn","destCode","logDateTime","logUserName");
		
		alignTypeList = Arrays.asList(2, 1, 1, 1, 1, 1, 1, 1, 1, 1
									 ,1, 1, 1, 2, 3, 3, 3, 1, 2, 2
									 ,1, 1, 1, 1, 1, 1, 1, 1, 1, 2
									 ,1, 1, 2, 1, 2, 2); // 1.왼쪽,2.가운데,3.오른쪽정렬
		
		colSizeList = Arrays.asList(2700, 4500, 4500, 3000, 3000, 4000, 2700, 2700, 6000, 4000
								   ,2700, 2700, 4500, 3000, 2700, 2700, 4000, 4000, 3600, 3600
								   ,4500, 4500, 4000, 4500, 3000, 6000, 4500, 4000, 4500, 2700
								   ,2700, 4500, 6000, 4500, 5000, 4500);
		
		//헤더 세팅 
		Font headerFont = wb.createFont();
		headerFont.setBold(true);
		headerFont.setFontName("Arial");
		headerFont.setFontHeightInPoints((short)10);
		headerFont.setColor(IndexedColors.WHITE.getIndex());
		Font titleFont = wb.createFont();
		titleFont.setBold(true);
		titleFont.setUnderline(HSSFFont.U_SINGLE); 
		titleFont.setFontName("Arial");
		titleFont.setFontHeightInPoints((short)16);
		Font font4 = wb.createFont();
		font4.setFontName("Arial");
		font4.setFontHeightInPoints((short)10);
		
		ExcelStyles.createStyles(wb, comStyle, "header", headerFont, titleFont, font4, df);
		
		int cellnum = 0;
		row = sheet.createRow(rownum);
		for(String headerName : headerNameList) {
			cell = row.createCell(cellnum);
			cell.setCellValue(headerName);
			cell.setCellStyle(comStyle.get("header"));
			cellnum++;
		}
		
		rownum ++;
		// 셀 길이 셋팅
		cellnum = 0;
		for(Integer colSize : colSizeList) {
			sheet.setColumnWidth(cellnum, colSize);
			cellnum++;
		}
		
		for (int i = 0; i < datas.size(); i++, rownum++) {
			data = (Map<String, Object>)objectMapper.convertValue(datas.get(i), Map.class);
			row = sheet.createRow(rownum);
			cellnum = 0;
			for(String colName : columnNameList) {
				cell = row.createCell(cellnum);
				String value= Util.null2str(data.get(colName));
				switch(alignTypeList.get(cellnum)){
					case 1 :
						cell.setCellValue(value);
						cell.setCellStyle(comStyle.get("cell_normal_left"));
						break;
					case 2 :
						cell.setCellValue(value);
						cell.setCellStyle(comStyle.get("cell_normal_centered"));
						break;
					case 3 :
						cell.setCellValue(value);
						cell.setCellStyle(comStyle.get("cell_normal_right"));
						break;
				}
				
				cellnum++;
			}
		}
			
		// Write the output to a file
		String currDtm = DateTimeUtil.getDate("yyyyMMddhhmmss");
        
        String saveFile = frontFileName + "_" + currDtm + ".xls";													// 임시파일명
    	if(Common.checkSaveFilenameForExcel(frontFileName)) {
			String uuid = UUID.randomUUID().toString();
            saveFile = uuid + "_" + currDtm + ".xls";
		}
        String originFile = frontFileName + "_" + currDtm.substring(0,4) + "-" + currDtm.substring(4,6) + "-" + currDtm.substring(6,8) + ".xls";	// 원본파일명
        
        if(wb instanceof XSSFWorkbook) {
        	saveFile += "x";
        	originFile += "x";
        }
        
        //workbook파일 출력
        FileUtil.fileWrite(wb, excelDir, saveFile);
        
    	// Return값 [0]: 저장된 파일명, [1]: 다운로드받을 파일명
        files[0] = saveFile;
        files[1] = originFile;
        
		return files;
	}
	
	@SuppressWarnings("unchecked")
	public String[] makeExcelCustomsExpExcel(
			String excelType, Map<String, Object> searchMap, List<String> columns, List<String> excelTypes,
			List<Integer> numFormats, List<String> titles, String sheetTitle, String frontFileName,
			String userName, String dateFormat) throws Exception {

		
		String excelDir = staticCustomProperties.getExcelDownload();
		
		// Return값 [0]: 실제 파일 저장경로, [1]: 다운로드받을 파일명
        String[] files = new String[2];
        FileInputStream file= null;
		Workbook wb;
		Sheet sheet;
		if(!Util.null2str(searchMap.get("saveFile")).equals("") ){  // 엑셀파일이 있는경우 다음 sheet 생성하여 데이터 적재 
			file = new FileInputStream(searchMap.get("saveFile").toString()); 
						
			if("xls".equals(Util.null2str(excelType))) 	wb = new HSSFWorkbook(file);
	        else 										wb = new SXSSFWorkbook(new XSSFWorkbook(file), 100);
			
			sheet = wb.createSheet(sheetTitle.replaceAll("/", "") +"("+searchMap.get("sheetNo").toString()+")");
			sheet = wb.getSheetAt(Integer.parseInt(searchMap.get("sheetNo").toString())); // 시트정보읽어온다
		}
		else{ 
			if("xls".equals(Util.null2str(excelType))) 	wb = new HSSFWorkbook();
	        else 										wb = new SXSSFWorkbook(100);
			
			sheet = wb.createSheet(sheetTitle.replaceAll("/", "")); // 시트생성
		}
		
		DataFormat df = wb.createDataFormat();		
		
		
		Font headerFont = wb.createFont();
		if("Japan_Export".equals(frontFileName)) headerFont.setBold(false);
		else headerFont.setBold(true);
		
		headerFont.setFontName("맑은 고딕");
		headerFont.setFontHeightInPoints((short)11);
		Font titleFont = wb.createFont();
		titleFont.setFontName("맑은 고딕");
		titleFont.setFontHeightInPoints((short)11);
		Font font4 = wb.createFont();
		font4.setFontName("맑은 고딕");
		font4.setFontHeightInPoints((short)11);
		
		Map<String, CellStyle> comStyle = new HashMap<String, CellStyle>();
		String[] styleTitleArr = {"system","system_value","title","header_right","header_left","header"};
		for(String styleTitle : styleTitleArr){
			ExcelStyles.createStyles(wb, comStyle, styleTitle, headerFont, titleFont, font4, df);
		}
		
		List<Map<String, CellStyle>> styleList = new ArrayList<Map<String, CellStyle>>();
		
		for(int i = 0; i < titles.size(); i++) {
			Map<String, CellStyle> style = new HashMap<String, CellStyle>();
			styleList.add(style);
		}
		
		PrintSetup printSetup = sheet.getPrintSetup();
		setPaperSize(printSetup, PrintSetup.A4_PAPERSIZE, true);
		sheet.setAutobreaks(true); // Option Button Move
		
		int rownum = 0;
		if("Japan_Export".equals(frontFileName)) {
			comStyle.get("title").setAlignment(HorizontalAlignment.LEFT);
			
			CellStyle yStyle = wb.createCellStyle();
			yStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
			yStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			yStyle.setAlignment(HorizontalAlignment.CENTER);
			
			for(int i = 0; i < 3; i++) {
				Row sysInfoRow = sheet.createRow(i);
				rownum = i;
				sysInfoRow.setHeight((short) 400);
				Cell sysInfoValueCell = null;
				switch(i) {
					case 0 :
						sysInfoValueCell = sysInfoRow.createCell(1);
						sysInfoValueCell.setCellValue("PRE ALERT");
						sysInfoValueCell.setCellStyle(comStyle.get("title"));
						break;
					case 1 :
						sysInfoValueCell = sysInfoRow.createCell(3);
						sysInfoValueCell.setCellValue(DateTimeUtil.getDate("yyyy-MM-dd"));
						sysInfoValueCell.setCellStyle(yStyle);
						break;
					case 2 :
						sysInfoValueCell = sysInfoRow.createCell(1);
						sysInfoValueCell.setCellValue("M.AWB");
						sysInfoValueCell.setCellStyle(comStyle.get("title"));
						
						sysInfoValueCell = sysInfoRow.createCell(3);
						sysInfoValueCell.setCellValue("");
						sysInfoValueCell.setCellStyle(yStyle);
						break;
					default :
						sysInfoValueCell = sysInfoRow.createCell(1);
						sysInfoValueCell.setCellValue("");
				}
			}
			
			rownum++;
			rownum = rownum + 2;
		}
	
        //the header row: centered text
        Row headerRow = sheet.createRow(rownum);
        headerRow.setHeight((short) 300);
        headerRow.setHeightInPoints(15.00f);

        Cell cell;
        boolean isTotalStyleType = false; // 합계 표현용 ROW 추가 여부
        boolean wrapText = true; // 자동줄바꿈 여부
        CreationHelper factory = wb.getCreationHelper();
		Drawing<?> drawing = sheet.createDrawingPatriarch();
		// When the comment box is visible, have it show in a 1x3 space
		ClientAnchor anchor = factory.createClientAnchor();
		Comment comment;
		RichTextString memoStr;
        
		//해더셋팅
		List<Map<String, Object>> memosList = (List<Map<String, Object>>)searchMap.get("memos");
        for (int i = 0, n = titles.size(); i < n; i++) {
            cell = headerRow.createCell(i);
            cell.setCellValue(Util.null2str(titles.get(i)).replaceAll("<br/>", " ").replaceAll("<br>", " ").replaceAll("<br />", " "));
		                
            if(memosList !=null && memosList.size() > i) {
            	String memo = Util.null2str(memosList.get(i));
            	//해더에 메모추가 시작
                if(!memo.equals("")){
    				anchor.setCol1(cell.getColumnIndex());
    				anchor.setCol2(cell.getColumnIndex()+2);
    				anchor.setRow1(headerRow.getRowNum());
    				anchor.setRow2(headerRow.getRowNum()+12);
    				comment = drawing.createCellComment(anchor);    				
    				memoStr = factory.createRichTextString(memo);
    				comment.setString(memoStr);
    				cell.setCellComment(comment);
    			}     
            }         
            if("중국수출통관용".equals(frontFileName)) comStyle.get("system").setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            else comStyle.get("system").setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());//#00B0F0
            
            cell.setCellStyle(comStyle.get("system"));
        }
		
        Row row;
        rownum++;
        List<Object> datas = (List<Object>)searchMap.get("data");

        Map<String, CellStyle> styles;
        String styleType = "";
        int numFormat = 0;
    	int titleSize = titles.size();
    	int totalAddRow = isTotalStyleType ? 1 : 0;
		ObjectMapper objectMapper = new ObjectMapper();
    	Map<String, Object> data = new HashMap<String, Object>();
        for (int i = 0, n = datas.size() + totalAddRow; i < n; i++, rownum++) {
        	row = sheet.createRow(rownum);
        	row.setHeight((short) 300);
        	
        	data = (Map<String, Object>)objectMapper.convertValue(datas.get(i), Map.class);
			data.put("num", i +1);
        	
        	String columnName = "";
        	for(int j = 0; j < titleSize; j++) {
        		cell = row.createCell(j);
        		columnName = Util.null2str(columns.get(j));
        		
        		// Cell Style Setting
        		styles = styleList.get(j);
        		styleType = Util.null2str(excelTypes.get(j), "left");
        		numFormat = Util.null2int(numFormats.get(j));
        		String defaultFormat = "#,##0";
        		switch(styleType) {
        			case "left" :
        				if(styles.get("system_value") == null){
        					ExcelStyles.createStyles(wb, styles, "system_value", headerFont, titleFont, font4, df, wrapText);
        					
        					if("Japan_Export".equals(frontFileName)) styles.get("system_value").setAlignment(HorizontalAlignment.CENTER);
        					else styles.get("system_value").setAlignment(HorizontalAlignment.LEFT);
        				}
        				cell.setCellStyle(styles.get("system_value"));
    					cell.setCellValue(Util.null2str(data.get(columnName)));
    					
        				break;
        			case "center" :
        				if(styles.get("system_value") == null){
        					ExcelStyles.createStyles(wb, styles, "system_value", headerFont, titleFont, font4, df, wrapText);
        					
        					styles.get("system_value").setAlignment(HorizontalAlignment.CENTER);
        				}
        				cell.setCellStyle(styles.get("system_value"));
    					cell.setCellValue(Util.null2str(data.get(columnName)));
                		
        				break;
        			case "right" :
        				if(styles.get("system_value") == null){
        					ExcelStyles.createStyles(wb, styles, "system_value", headerFont, titleFont, font4, df, wrapText);
        					
        					if("Japan_Export".equals(frontFileName)) styles.get("system_value").setAlignment(HorizontalAlignment.CENTER);
        					else styles.get("system_value").setAlignment(HorizontalAlignment.RIGHT);
        				}
        				cell.setCellStyle(styles.get("system_value"));
                		cell.setCellValue(Util.null2str(data.get(columnName)));
                		
        				break;
        			case "number" :
        				if(styles.get("system_value") == null){
        					ExcelStyles.createStyles(wb, styles, "system_value", headerFont, titleFont, font4, df, wrapText);
        					
        					if(numFormat > 0) {
        			        	defaultFormat += ".";
        			        	for(int k = 0; k < numFormat; k++) {
        			        		defaultFormat += "0";
        			        	}
            				}else if(numFormat != 0){
            					String[] digits = Util.null2str(data.get(columnName)).split("\\.");
            					if(digits.length > 1) {
            			        	defaultFormat += ".";
            			        	for(int k = 0; k < digits[1].length(); k++) {
            			        		defaultFormat += "0";
            			        	}        						
            					}
            				}
        					styles.get("system_value").setDataFormat(df.getFormat(defaultFormat));
        					
        					if("Japan_Export".equals(frontFileName)) styles.get("system_value").setAlignment(HorizontalAlignment.CENTER);
        					else styles.get("system_value").setAlignment(HorizontalAlignment.RIGHT);
        				}
        				cell.setCellStyle(styles.get("system_value"));
        				cell.setCellValue(Util.null2int(data.get(columnName)));
        				
        				break;
        			case "double" :
        				if(styles.get("system_value") == null){
        					ExcelStyles.createStyles(wb, styles, "system_value", headerFont, titleFont, font4, df, wrapText);
        					
        					if(numFormat > 0) {
        			        	defaultFormat += ".";
        			        	for(int k = 0; k < numFormat; k++) {
        			        		defaultFormat += "0";
        			        	}
            				}else if(numFormat != 0){
            					String[] digits = Util.null2str(data.get(columnName)).split("\\.");
            					if(digits.length > 1) {
            			        	defaultFormat += ".";
            			        	for(int k = 0; k < digits[1].length(); k++) {
            			        		defaultFormat += "0";
            			        	}        						
            					}
            				}
        					styles.get("system_value").setDataFormat(df.getFormat(defaultFormat));
        					
        					if("Japan_Export".equals(frontFileName)) styles.get("system_value").setAlignment(HorizontalAlignment.CENTER);
        					else styles.get("system_value").setAlignment(HorizontalAlignment.RIGHT);
        				}
        				cell.setCellStyle(styleList.get(j).get("system_value"));
                		cell.setCellValue(Util.null2dbl(data.get(columnName)));
                		
        				break;
        			default :
        				if(styles.get("system_value") == null){
        					ExcelStyles.createStyles(wb, styles, "system_value", headerFont, titleFont, font4, df, wrapText);
        				}
        				cell.setCellStyle(styles.get("system_value"));
                		cell.setCellValue(Util.null2str(data.get(columnName)));
        		}
        	}
        	
        	/** 100개마다 flushRows 메소드를 통해 메모리에 있는 데이터를 디스크(임시파일)로 옮기고 메모리를 비워준다. */
    		if("xlsx".equals(Util.null2str(excelType))) {
    			if (i % 100 == 0) ((SXSSFSheet) sheet).flushRows(100); 
    		}
        }
        
        List<Integer> columnsWidth = (List<Integer>)searchMap.get("columnsWidth");
        int columnsSize;
        for(int i = 0; i < titleSize; i++) {
        	columnsSize = Util.null2int(columnsWidth.get(i)) * 36;
        	if(columnsSize > 65280){
        		sheet.setColumnWidth(i, 65280);
        	}else{
        		sheet.setColumnWidth(i, columnsSize);
        	}
        }
        
        if(Util.null2str(searchMap.get("saveFile")).equals("") ){ // 엑셀파일 생성
        	// Write the output to a file
    		String currDtm = DateTimeUtil.getDate("yyyyMMddHHmmss");
            
            String saveFile = frontFileName + "_" + currDtm + ".xls";													// 임시파일명
        	if(Common.checkSaveFilenameForExcel(frontFileName)) {
    			String uuid = UUID.randomUUID().toString();
                saveFile = uuid + "_" + currDtm + ".xls";
    		}
            String originFile = frontFileName + "_" + currDtm.substring(0,4) + "-" + currDtm.substring(4,6) + "-" + currDtm.substring(6,8) + ".xls";	// 원본파일명
            
            if("xlsx".equals(Util.null2str(excelType))) {
            	saveFile += "x";
            	originFile += "x";
            }
            
            //workbook파일 출력
            FileUtil.fileWrite(wb, excelDir, saveFile);
            
        	// Return값 [0]: 저장된 파일명, [1]: 다운로드받을 파일명
            files[0] = saveFile;
            files[1] = originFile;
		}	
        else{
        	String logVal = searchMap.get("saveFile").toString() +  "  " + searchMap.get("originFile").toString();
        	log.debug(logVal);
        	FileUtil.fileWrite(wb, "", searchMap.get("saveFile").toString());
	        
	        files[0] = searchMap.get("saveFile").toString();
	        files[1] = searchMap.get("originFile").toString();
	        
	        file.close();
        }
        
        return files;
	}
		
}

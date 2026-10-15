package org.yht.exps.express.com.excel;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelStyles {
	
    /**
     * create a library of cell styles
     */
	public static Map<String, CellStyle> createStyles(Workbook wb) {
		return createStyles(wb, false);
	}
    public static Map<String, CellStyle> createStyles(Workbook wb, boolean headOnly) {
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
        DataFormat df = wb.createDataFormat();

        CellStyle style;
        
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerFont.setFontName("맑은 고딕");
        headerFont.setFontHeightInPoints((short)10);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        
        style = createBorderedStyle(wb);
        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderLeft(BorderStyle.NONE);
        style.setFont(headerFont);
        styles.put("header", style);
        
        style = createBorderedStyle(wb);
        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setFont(headerFont);
        styles.put("header_left", style); 
        
        style = createBorderedStyle(wb);
        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.NONE);
        style.setFont(headerFont);
        styles.put("header_right", style);
        
        if(headOnly){
        	return styles;
        }
        
        Font titleFont = wb.createFont();
        titleFont.setBold(true);
        titleFont.setUnderline(HSSFFont.U_SINGLE); 
        titleFont.setFontName("맑은 고딕");
        titleFont.setFontHeightInPoints((short)16);
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setFont(titleFont);
        styles.put("title", style); 
        
        style = createBorderedStyle(wb);
        style.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFont(headerFont);
        styles.put("system", style); 
        
        
        Font headerFont1 = wb.createFont();
        headerFont1.setBold(true);
        headerFont1.setFontName("맑은 고딕");
        headerFont1.setFontHeightInPoints((short)10);
        headerFont1.setColor(IndexedColors.BLACK.getIndex());
        
        style = createBorderedStyle(wb);
        style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFont(headerFont1);
        styles.put("header1", style); 
        
        style = createBorderedStyle(wb);
        style.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFont(headerFont1);
        styles.put("header2", style); 
        
        style = createBorderedStyle(wb);
        style.setFillForegroundColor(IndexedColors.LIME.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFont(headerFont1);
        styles.put("header3", style); 
        
        style = createBorderedStyle(wb);
        style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setFont(headerFont);
        styles.put("header_left1", style); 
        
        style = createBorderedStyle(wb);
        style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.NONE);
        style.setFont(headerFont);
        styles.put("header_right1", style); 
         
        Font font4 = wb.createFont();
        font4.setFontName("맑은 고딕");
        font4.setFontHeightInPoints((short)10);
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFont(font4);
        styles.put("system_value", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFont(font4);
        styles.put("system_value1", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE); 
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setFont(font4);
        styles.put("cell_normal", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setFont(font4);
        styles.put("cell_normal_left", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setFont(font4);
        styles.put("cell_normal_right", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setFont(font4);
        styles.put("cell_normal_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setFont(font4);
        styles.put("cell_normal_left_bottom", style); 

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setFont(font4);
        styles.put("cell_normal_right_bottom", style); 
        
        
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setFont(font4);
        styles.put("cell_right", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setFont(font4);
        styles.put("cell_right_left", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setFont(font4);
        styles.put("cell_right_right", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setFont(font4);
        styles.put("cell_right_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setFont(font4);
        styles.put("cell_right_left_bottom", style); 

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setFont(font4);
        styles.put("cell_right_right_bottom", style); 
        
        
        
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setDataFormat(df.getFormat("#,##0.00"));
        style.setFont(font4);
        styles.put("cell_normal_double", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setDataFormat(df.getFormat("#,##0.00"));
        style.setFont(font4);
        styles.put("cell_normal_double_left", style); 

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setDataFormat(df.getFormat("#,##0.00"));
        style.setFont(font4);
        styles.put("cell_normal_double_right", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setDataFormat(df.getFormat("#,##0.00"));
        style.setFont(font4);
        styles.put("cell_normal_double_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setDataFormat(df.getFormat("#,##0.00"));
        style.setFont(font4);
        styles.put("cell_normal_double_left_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setDataFormat(df.getFormat("#,##0.00"));
        style.setFont(font4);
        styles.put("cell_normal_double_right_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setDataFormat(df.getFormat("#,##0"));
        style.setFont(font4);
        styles.put("cell_normal_number", style); 

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setDataFormat(df.getFormat("#,##0"));
        style.setFont(font4);
        styles.put("cell_normal_number_left", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setDataFormat(df.getFormat("#,##0"));
        style.setFont(font4);
        styles.put("cell_normal_number_right", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setDataFormat(df.getFormat("#,##0"));
        style.setFont(font4);
        styles.put("cell_normal_number_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setDataFormat(df.getFormat("#,##0"));
        style.setFont(font4);
        styles.put("cell_normal_number_left_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setDataFormat(df.getFormat("#,##0"));
        style.setFont(font4);
        styles.put("cell_normal_number_right_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setFont(font4);
        styles.put("cell_normal_centered", style); 

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setFont(font4);
        styles.put("cell_normal_centered_left", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setFont(font4);
        styles.put("cell_normal_centered_right", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setFont(font4);
        styles.put("cell_normal_centered_bottom", style); 

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setFont(font4);
        styles.put("cell_normal_centered_left_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setFont(font4);
        styles.put("cell_normal_centered_right_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setFont(font4);
        style.setDataFormat(df.getFormat("m/d"));
        styles.put("cell_normal_date", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setFont(font4);
        style.setDataFormat(df.getFormat("m/d"));
        styles.put("cell_normal_date_left", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setFont(font4);
        style.setDataFormat(df.getFormat("m/d"));
        styles.put("cell_normal_date_right", style); 

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setFont(font4);
        style.setDataFormat(df.getFormat("m/d"));
        styles.put("cell_normal_date_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setFont(font4);
        style.setDataFormat(df.getFormat("m/d"));
        styles.put("cell_normal_date_left_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setFont(font4);
        style.setDataFormat(df.getFormat("m/d"));
        styles.put("cell_normal_date_right_bottom", style); 
        
        /*
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setIndention((short)1);
        style.setFont(font4);
        styles.put("cell_indented", style);

        style = createBorderedStyle(wb);
        style.setFont(font4);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("cell_blue", style);
        
        style = createBorderedStyle(wb);
        style.setFont(font4);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("cell_yellow", style);

        style = createBorderedStyle(wb);
        style.setFont(font4);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styles.put("cell_yellow_center", style);
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setFont(font4);
        styles.put("cell_normal_last", style);
        
        
        Font totalFont = wb.createFont();
        totalFont.setBold(true);
        totalFont.setFontName("맑은 고딕");
        totalFont.setFontHeightInPoints((short)13);
        style = createBorderedStyle(wb);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setFont(totalFont);
        styles.put("cell_total_normal", style);

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setFont(totalFont);
        styles.put("cell_total_normal_center", style);
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setFont(totalFont);
        styles.put("cell_total_normal_right", style);
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setFont(totalFont);
        styles.put("cell_total_normal_left", style);
        */
        
        style = createBorderedStyle(wb);
        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setFont(headerFont);
        styles.put("headerBorder", style); 
        return styles;
    }
    
    
    public static Map<String, CellStyle> createS1EXPStyles(Workbook wb) {
    	 Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
         DataFormat df = wb.createDataFormat();

         CellStyle style;
         
         Font titleFont = wb.createFont();
         titleFont.setBold(true);
         titleFont.setUnderline(HSSFFont.U_SINGLE); 
         titleFont.setFontName("맑은 고딕");
         titleFont.setFontHeightInPoints((short)16);
         
         style = createBorderedStyle(wb);
         style.setAlignment(HorizontalAlignment.CENTER);
         style.setVerticalAlignment(VerticalAlignment.CENTER);
         style.setBorderTop(BorderStyle.NONE);
         style.setBorderBottom(BorderStyle.NONE);
         style.setBorderLeft(BorderStyle.NONE);
         style.setBorderRight(BorderStyle.NONE);
         style.setFont(titleFont);
         styles.put("title", style); 
         
         Font headerFont = wb.createFont();
         headerFont.setBold(true);
         headerFont.setFontName("맑은 고딕");
         headerFont.setFontHeightInPoints((short)10);
         headerFont.setColor(IndexedColors.WHITE.getIndex());

         style = createBorderedStyle(wb);
         style.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
         style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
         style.setAlignment(HorizontalAlignment.CENTER);
         style.setVerticalAlignment(VerticalAlignment.CENTER);
         style.setFont(headerFont);
         styles.put("system", style); 
         
       
         
         style = createBorderedStyle(wb);
         style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
         style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
         style.setAlignment(HorizontalAlignment.CENTER);
         style.setVerticalAlignment(VerticalAlignment.CENTER);
         style.setBorderTop(BorderStyle.MEDIUM);
         style.setBorderBottom(BorderStyle.MEDIUM);
         style.setBorderRight(BorderStyle.NONE);
         style.setBorderLeft(BorderStyle.NONE);
         style.setFont(headerFont);
         styles.put("header", style); 
         
         
         Font headerFont1 = wb.createFont();
         headerFont1.setBold(true);
         headerFont1.setFontName("맑은 고딕");
         headerFont1.setFontHeightInPoints((short)10);
         headerFont1.setColor(IndexedColors.BLACK.getIndex());
         
         style = createBorderedStyle(wb);
         style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
         style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
         style.setAlignment(HorizontalAlignment.CENTER);
         style.setVerticalAlignment(VerticalAlignment.CENTER);
         style.setFont(headerFont1);
         styles.put("header1", style); 
         
         style = createBorderedStyle(wb);
         style.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
         style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
         style.setAlignment(HorizontalAlignment.CENTER);
         style.setVerticalAlignment(VerticalAlignment.CENTER);
         style.setFont(headerFont1);
         styles.put("header2", style); 
         
         style = createBorderedStyle(wb);
         style.setFillForegroundColor(IndexedColors.LIME.getIndex());
         style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
         style.setAlignment(HorizontalAlignment.CENTER);
         style.setVerticalAlignment(VerticalAlignment.CENTER);
         style.setFont(headerFont1);
         styles.put("header3", style); 
         
         style = createBorderedStyle(wb);
         style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
         style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
         style.setAlignment(HorizontalAlignment.CENTER);
         style.setVerticalAlignment(VerticalAlignment.CENTER);
         style.setBorderTop(BorderStyle.MEDIUM);
         style.setBorderBottom(BorderStyle.MEDIUM);
         style.setBorderRight(BorderStyle.NONE);
         style.setBorderLeft(BorderStyle.MEDIUM);
         style.setFont(headerFont);
         styles.put("header_left", style); 
         
         style = createBorderedStyle(wb);
         style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
         style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
         style.setAlignment(HorizontalAlignment.CENTER);
         style.setVerticalAlignment(VerticalAlignment.CENTER);
         style.setBorderTop(BorderStyle.MEDIUM);
         style.setBorderBottom(BorderStyle.MEDIUM);
         style.setBorderRight(BorderStyle.MEDIUM);
         style.setBorderLeft(BorderStyle.NONE);
         style.setFont(headerFont);
         styles.put("header_right", style); 
         
         
         style = createBorderedStyle(wb);
         style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
         style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
         style.setAlignment(HorizontalAlignment.CENTER);
         style.setVerticalAlignment(VerticalAlignment.CENTER);
         style.setBorderTop(BorderStyle.MEDIUM);
         style.setBorderBottom(BorderStyle.MEDIUM);
         style.setBorderRight(BorderStyle.NONE);
         style.setBorderLeft(BorderStyle.MEDIUM);
         style.setFont(headerFont);
         styles.put("header_left1", style); 
         
         style = createBorderedStyle(wb);
         style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
         style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
         style.setAlignment(HorizontalAlignment.CENTER);
         style.setVerticalAlignment(VerticalAlignment.CENTER);
         style.setBorderTop(BorderStyle.MEDIUM);
         style.setBorderBottom(BorderStyle.MEDIUM);
         style.setBorderRight(BorderStyle.MEDIUM);
         style.setBorderLeft(BorderStyle.NONE);
         style.setFont(headerFont);
         styles.put("header_right1", style); 
         
                 
         Font font4 = wb.createFont();
         font4.setFontName("맑은 고딕");
         font4.setFontHeightInPoints((short)10);
         
         style = createBorderedStyle(wb);
         style.setAlignment(HorizontalAlignment.LEFT);
         style.setVerticalAlignment(VerticalAlignment.CENTER);
         style.setFont(font4);
         styles.put("system_value", style); 
         
         style = createBorderedStyle(wb);
         style.setAlignment(HorizontalAlignment.CENTER);
         style.setVerticalAlignment(VerticalAlignment.CENTER);
         style.setFont(font4);
         styles.put("system_value1", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
     /*   style.setBorderLeft(BorderStyle.NONE); 
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);*/
        style.setFont(font4);
        styles.put("cell_normal", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
      /*  style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);*/
        style.setFont(font4);
        styles.put("cell_normal_left", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
      /*  style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);*/
        style.setFont(font4);
        styles.put("cell_normal_right", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);*/
        style.setFont(font4);
        styles.put("cell_normal_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
      /*  style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);*/
        style.setFont(font4);
        styles.put("cell_normal_left_bottom", style); 

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
      /*  style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);*/
        style.setFont(font4);
        styles.put("cell_normal_right_bottom", style); 
        
        
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);*/
        style.setFont(font4);
        styles.put("cell_right", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);*/
        style.setFont(font4);
        styles.put("cell_right_left", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderBottom(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);*/
        style.setFont(font4);
        styles.put("cell_right_right", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);*/
        style.setFont(font4);
        styles.put("cell_right_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);*/
        style.setFont(font4);
        styles.put("cell_right_left_bottom", style); 

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);*/
        style.setFont(font4);
        styles.put("cell_right_right_bottom", style); 
        
        
        
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);*/
        style.setDataFormat(df.getFormat("#,##0.00"));
        style.setFont(font4);
        styles.put("cell_normal_double", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        /*style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);*/
        style.setDataFormat(df.getFormat("#,##0.00"));
        style.setFont(font4);
        styles.put("cell_normal_double_left", style); 

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
      /*  style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);*/
        style.setDataFormat(df.getFormat("#,##0.00"));
        style.setFont(font4);
        styles.put("cell_normal_double_right", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        /*style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);*/
        style.setDataFormat(df.getFormat("#,##0.00"));
        style.setFont(font4);
        styles.put("cell_normal_double_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);*/
        style.setDataFormat(df.getFormat("#,##0.00"));
        style.setFont(font4);
        styles.put("cell_normal_double_left_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);*/
        style.setDataFormat(df.getFormat("#,##0.00"));
        style.setFont(font4);
        styles.put("cell_normal_double_right_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);*/
        style.setDataFormat(df.getFormat("#,##0"));
        style.setFont(font4);
        styles.put("cell_normal_number", style); 

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        /*style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);*/
        style.setDataFormat(df.getFormat("#,##0"));
        style.setFont(font4);
        styles.put("cell_normal_number_left", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);*/
        style.setDataFormat(df.getFormat("#,##0"));
        style.setFont(font4);
        styles.put("cell_normal_number_right", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        /*style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);*/
        style.setDataFormat(df.getFormat("#,##0"));
        style.setFont(font4);
        styles.put("cell_normal_number_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);*/
        style.setDataFormat(df.getFormat("#,##0"));
        style.setFont(font4);
        styles.put("cell_normal_number_left_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);*/
        style.setDataFormat(df.getFormat("#,##0"));
        style.setFont(font4);
        styles.put("cell_normal_number_right_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderRight(BorderStyle.NONE);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);*/
        style.setFont(font4);
        styles.put("cell_normal_centered", style); 

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);*/
        style.setFont(font4);
        styles.put("cell_normal_centered_left", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        /*style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);*/
        style.setFont(font4);
        styles.put("cell_normal_centered_right", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
      /*  style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);*/
        style.setFont(font4);
        styles.put("cell_normal_centered_bottom", style); 

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        /*style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);*/
        style.setFont(font4);
        styles.put("cell_normal_centered_left_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);*/
        style.setFont(font4);
        styles.put("cell_normal_centered_right_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
      /*  style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);*/
        style.setFont(font4);
        style.setDataFormat(df.getFormat("m/d"));
        styles.put("cell_normal_date", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);*/
        style.setFont(font4);
        style.setDataFormat(df.getFormat("m/d"));
        styles.put("cell_normal_date_left", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);*/
        style.setFont(font4);
        style.setDataFormat(df.getFormat("m/d"));
        styles.put("cell_normal_date_right", style); 

        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);*/
        style.setFont(font4);
        style.setDataFormat(df.getFormat("m/d"));
        styles.put("cell_normal_date_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);*/
        style.setFont(font4);
        style.setDataFormat(df.getFormat("m/d"));
        styles.put("cell_normal_date_left_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
       /* style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);*/
        style.setFont(font4);
        style.setDataFormat(df.getFormat("m/d"));
        styles.put("cell_normal_date_right_bottom", style); 
        
        
        return styles;
    }

    /**
     * 2016.07.25 박현익 : PNS 스타일 추가(무조건 PNS는 아니지만 현재 PNS 엑셀 스타일을 적용 차후에 변경될수도있음)
     * @since 2016.08.05
     * @author parkhi
     */
    public static Map<String, CellStyle> createPnsStyles(Workbook wb) {
        Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
        DataFormat df = wb.createDataFormat();

        CellStyle style;
        
        Font font = wb.createFont();
        font.setFontName("맑은 고딕");
        font.setBold(true);
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setFont(font);
        styles.put("cell_bold_center", style);
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setFont(font);
        styles.put("cell_bold_left", style);
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.NONE);
        style.setFont(font);
        styles.put("cell_bold_right", style);
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setDataFormat(df.getFormat("#,##0.00"));
        style.setFont(font);
        styles.put("cell_bold_double_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.NONE);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setDataFormat(df.getFormat("#,##0.00"));
        style.setFont(font);
        styles.put("cell_bold_double_left_bottom", style); 
        
        style = createBorderedStyle(wb);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderLeft(BorderStyle.NONE);
        style.setBorderRight(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.NONE);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setDataFormat(df.getFormat("#,##0.00"));
        style.setFont(font);
        styles.put("cell_bold_double_right_bottom", style);
        
        return styles;
    }
    
    /**
     * 2022.02.15 cargo Manifest용 스타일 추가 
     * @since 2022.02.15
     * @author shkim
     */
    public static Map<String, CellStyle> createCargoManifestStyles(Workbook wb) {
    	Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
    	CellStyle style;
    	
    	Font font1 = wb.createFont();
    	Font font2 = wb.createFont();
    	Font font3 = wb.createFont();
    	
    	font1.setBold(true);
		font1.setFontName("Courier New");
		font1.setFontHeightInPoints((short) 11);
		
		font2.setBold(true);
		font2.setFontName("Courier New");
		font2.setFontHeightInPoints((short) 20);

		font3.setBold(false);
		font3.setFontName("Courier New");
		font3.setFontHeightInPoints((short) 10);
		
		style = createBorderedStyle(wb);
		style.setFont(font1);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setAlignment(HorizontalAlignment.CENTER);
		styles.put("header_style", style);
		
		style = wb.createCellStyle();
		style.setFont(font1);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setAlignment(HorizontalAlignment.LEFT);
		styles.put("label_style", style);
		
		style = wb.createCellStyle();
		style.setFont(font2);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setAlignment(HorizontalAlignment.CENTER);
		styles.put("title_style", style);
		
		style = wb.createCellStyle();
		style.setFont(font3);
		styles.put("normal_style", style);
		
		style = createBorderedStyle(wb);
		style.setFont(font3);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setWrapText(true); //자동줄바꿈
		styles.put("text_left_style", style);
		
		style = createBorderedStyle(wb);
		style.setFont(font3);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setWrapText(true); //자동줄바꿈
		styles.put("text_center_style", style);
		
		style = createBorderedStyle(wb);
		style.setFont(font3);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setAlignment(HorizontalAlignment.RIGHT);
		style.setWrapText(true); //자동줄바꿈
		styles.put("text_right_style", style);
		
    	return styles;
    }
    
    public static void createStyles(Workbook wb, Map<String, CellStyle> styles, String title, Font headerFont, Font titleFont, Font font4, DataFormat df) {
    	createStyles(wb, styles, title, headerFont, titleFont, font4, df, true);
    }
    
    public static void createStyles(Workbook wb, Map<String, CellStyle> styles, String title, Font headerFont, Font titleFont, Font font4, DataFormat df, boolean wrapText) {
    	CellStyle style = null;
		switch(title) {
			case "system" :
		        style = createBorderedStyle(wb);
		        style.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
		        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setFont(headerFont);
				break;
			case "system_value" :
		        style = createBorderedStyle(wb);
		        style.setAlignment(HorizontalAlignment.LEFT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setFont(font4);
				break;
			case "title" :
		        style = createBorderedStyle(wb);
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.NONE);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setFont(titleFont);
				break;
			case "header_right" :
				style = createBorderedStyle(wb);
		        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
		        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderTop(BorderStyle.MEDIUM);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setBorderRight(BorderStyle.MEDIUM);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setFont(headerFont);
				break;
			case "header_left" :
				style = createBorderedStyle(wb);
		        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
		        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderTop(BorderStyle.MEDIUM);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderLeft(BorderStyle.MEDIUM);
		        style.setFont(headerFont);
				break;
			case "header" :
				style = createBorderedStyle(wb);
		        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
		        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderTop(BorderStyle.MEDIUM);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setFont(headerFont);
				break;
			case "cell_normal_left_bottom" :
				style = createBorderedStyle(wb);
		        style.setAlignment(HorizontalAlignment.LEFT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.MEDIUM);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setFont(font4);
				break;
			case "cell_normal_right_bottom" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.LEFT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.MEDIUM);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setFont(font4);
				break;
			case "cell_normal_bottom" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.LEFT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setFont(font4);
				break;
			case "cell_normal_left" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.LEFT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.MEDIUM);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.NONE);
		        style.setFont(font4);
				break;
			case "cell_normal_right" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.LEFT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.MEDIUM);
		        style.setBorderBottom(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setFont(font4);
				break;
			case "cell_normal" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.LEFT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.NONE); 
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.NONE);
		        style.setFont(font4);
				break;
			case "cell_normal_centered_left_bottom" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.MEDIUM);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setFont(font4);
				break;
			case "cell_normal_centered_right_bottom" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.MEDIUM);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setFont(font4);
				break;
			case "cell_normal_centered_bottom" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setFont(font4);
				break;
			case "cell_normal_centered_left" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.MEDIUM);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.NONE);
		        style.setFont(font4);
				break;
			case "cell_normal_centered_right" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.MEDIUM);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.NONE);
		        style.setFont(font4);
				break;
			case "cell_normal_centered" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.NONE);
		        style.setFont(font4);
				break;
			case "cell_right_left_bottom" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.RIGHT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.MEDIUM);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setFont(font4);
				break;
			case "cell_right_right_bottom" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.RIGHT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.MEDIUM);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setFont(font4);
				break;
			case "cell_right_bottom" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.RIGHT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setFont(font4);
				break;
			case "cell_right_left" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.RIGHT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.MEDIUM);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.NONE);
		        style.setFont(font4);
				break;
			case "cell_right_right" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.RIGHT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.MEDIUM);
		        style.setBorderBottom(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setFont(font4);
				break;
			case "cell_right" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.RIGHT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.NONE);
		        style.setFont(font4);
				break;
			case "cell_normal_number_left_bottom" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.RIGHT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.MEDIUM);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setDataFormat(df.getFormat("#,##0"));
		        style.setFont(font4);
				break;
			case "cell_normal_number_right_bottom" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.RIGHT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.MEDIUM);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setDataFormat(df.getFormat("#,##0"));
		        style.setFont(font4);
				break;
			case "cell_normal_number_bottom" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.RIGHT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setDataFormat(df.getFormat("#,##0"));
		        style.setFont(font4);
				break;
			case "cell_normal_number_left" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.RIGHT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.MEDIUM);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.NONE);
		        style.setDataFormat(df.getFormat("#,##0"));
		        style.setFont(font4);
				break;
			case "cell_normal_number_right" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.RIGHT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.MEDIUM);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.NONE);
		        style.setDataFormat(df.getFormat("#,##0"));
		        style.setFont(font4);
				break;
			case "cell_normal_number" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.RIGHT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.NONE);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setDataFormat(df.getFormat("#,##0"));
		        style.setFont(font4);
				break;
			case "cell_normal_double_left_bottom" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.RIGHT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.MEDIUM);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setDataFormat(df.getFormat("#,##0.00"));
		        style.setFont(font4);
				break;
			case "cell_normal_double_right_bottom" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.RIGHT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.MEDIUM);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setDataFormat(df.getFormat("#,##0.00"));
		        style.setFont(font4);
				break;
			case "cell_normal_double_bottom" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.RIGHT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setDataFormat(df.getFormat("#,##0.00"));
		        style.setFont(font4);
				break;
			case "cell_normal_double_left" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.RIGHT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.MEDIUM);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.NONE);
		        style.setDataFormat(df.getFormat("#,##0.00"));
		        style.setFont(font4);
				break;
			case "cell_normal_double_right" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.RIGHT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderRight(BorderStyle.MEDIUM);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.NONE);
		        style.setDataFormat(df.getFormat("#,##0.00"));
		        style.setFont(font4);
				break;
			case "cell_normal_double" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.RIGHT);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.NONE);
		        style.setDataFormat(df.getFormat("#,##0.00"));
		        style.setFont(font4);
				break;
			case "cell_normal_date_left_bottom" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.MEDIUM);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setFont(font4);
		        style.setDataFormat(df.getFormat("m/d"));
				break;
			case "cell_normal_date_right_bottom" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderRight(BorderStyle.MEDIUM);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setFont(font4);
		        style.setDataFormat(df.getFormat("m/d"));
				break;
			case "cell_normal_date_bottom" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.MEDIUM);
		        style.setFont(font4);
		        style.setDataFormat(df.getFormat("m/d"));
				break;
			case "cell_normal_date_left" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.MEDIUM);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.NONE);
		        style.setFont(font4);
		        style.setDataFormat(df.getFormat("m/d"));
				break;
			case "cell_normal_date_right" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderRight(BorderStyle.MEDIUM);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.NONE);
		        style.setFont(font4);
		        style.setDataFormat(df.getFormat("m/d"));
				break;
			case "cell_normal_date" :
				style = createBorderedStyle(wb, wrapText);
		        style.setAlignment(HorizontalAlignment.CENTER);
		        style.setVerticalAlignment(VerticalAlignment.CENTER);
		        style.setBorderLeft(BorderStyle.NONE);
		        style.setBorderRight(BorderStyle.NONE);
		        style.setBorderTop(BorderStyle.NONE);
		        style.setBorderBottom(BorderStyle.NONE);
		        style.setFont(font4);
		        style.setDataFormat(df.getFormat("m/d"));
				break;
		}
		styles.put(title, style);
    }
    
    /**
     * create a library of border styles
     */
    public static CellStyle createBorderedStyle(Workbook wb){
        return createBorderedStyle(wb, true);
    }
    
    /**
     * create a library of border styles
     */
    public static CellStyle createBorderedStyle(Workbook wb, boolean wrapText){
        CellStyle style = wb.createCellStyle();
        style.setWrapText(wrapText);
        style.setBorderRight(BorderStyle.THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        return style;
    }
   
}
package org.yht.exps.express.model.search;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@Alias("searchList")
public class SearchList {
	@ApiModelProperty(value="조회타입", example="excel")
	private String srchMode;
	@ApiModelProperty(value="그리드기본정렬", example="HBL_NO DESC")
	private String defaultSort;
	@ApiModelProperty(value="그리드기본정렬2", example="HBL_NO DESC")
	private String defaultSort2;
	@ApiModelProperty(value="그리드정렬")
	private List<Sort> sort;
	@ApiModelProperty(value="그리드페이지사이즈", example="50")
	private int pageSize;
	@ApiModelProperty(value="그리드페이지", example="2")
	private int page;
	@ApiModelProperty(value="그리드시작번호", example="51")
	private int startNum;
	@ApiModelProperty(value="그리드필터")
	private Filter filter;
	@ApiModelProperty(value="정렬/필터 필드 변경여부(CamelCase To Underscore)", example="Y or N")
	private String changeField;
	@ApiModelProperty(value="조회일자타입", example="23")
	private String srchDateFormat;
	@ApiModelProperty(value="조회일자구분", example="OB_DATE")
	private String srchDateGbn;
	@ApiModelProperty(value="조회시작일", example="2021-01-01")
	private String srchStartDate;
	@ApiModelProperty(value="조회종료일", example="2021-01-01")
	private String srchEndDate;
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	
	
	// Excel Download 관련 Parameter
	@ApiModelProperty(value="Excel Sheet Name")
	private String mainTitle;
	@ApiModelProperty(value="사용자 아이디")
	private String userId;
	@ApiModelProperty(value="사용자 이름")
	private String userName;
	@ApiModelProperty(value="Excel File Name")
	private String excelFileName;
	@ApiModelProperty(value="날짜포맷")
	private String dateFormat;
	@ApiModelProperty(value="그리드 컬럼")
	private List<String> columns;
	@ApiModelProperty(value="그리드 컬럼 타이틀")
	private List<String> titles;
	@ApiModelProperty(value="그리드 컬럼 메모")
	private List<String> memos;
	@ApiModelProperty(value="그리드 엑셀 타입")
	private List<String> excelTypes;
	@ApiModelProperty(value="그리드 Number 포맷")
	private List<String> numFormats;
	@ApiModelProperty(value="그리드 컬럼 width")
	//private List<Integer> columnsWidth; 
	private List<Double> columnsWidth; // 그리드 너비 조정 후 엑셀다운로드시 타입오류 발생으로 인한 변경 2026.01.09 2sowell
	
	@ApiModelProperty(value="암호화여부")
	private String cryptographyYn;
	@ApiModelProperty(value="암호화여부(동적쿼리)")
	private String dynamicCryptographyYn;
}

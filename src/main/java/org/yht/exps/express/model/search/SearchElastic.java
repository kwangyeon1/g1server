package org.yht.exps.express.model.search;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchElastic", description = "ElasticSearch 기본 검색 조건")
@Alias("searchElasitc")
public class SearchElastic {
	@ApiModelProperty(value="페이지 리스트 개수", example="100")
	private int pageSize;
	@ApiModelProperty(value="페이지", example="0")
	private int page;
	@ApiModelProperty(value="조회타입", example="excel")
	private String srchMode;
	@ApiModelProperty(value="날짜포맷", example="yyyy-MM-dd")
	private String srchDateFormat;
	@ApiModelProperty(value="검색일자사용여부", example="Y")
	private String srchDateCheck;
	@ApiModelProperty(value="검색일자구분", example="ar_date")
	private String srchDateGbn;
	@ApiModelProperty(value="검색시작일자", example="1551884400000")
	private long srchStartDate;
	@ApiModelProperty(value="검색종료일자", example="1551970799000")
	private long srchEndDate;
	@ApiModelProperty(value="Scroll ID", example="FGluY2x1ZGVfY29udG....")
	private String srchScrollId;
	@ApiModelProperty(value="그리드 정렬")
	private List<Sort> sort;
	@ApiModelProperty(value="그리드 필터")
	private List<Filter> filter;
	
	// Excel Download 관련 Parameter
	@ApiModelProperty(value="Excel Sheet Name")
	private String mainTitle;
	@ApiModelProperty(value="사용자 이름")
	private String userName;
	@ApiModelProperty(value="날짜포맷")
	private String dateFormat;
	@ApiModelProperty(value="참조번호")
	private String referenceNoId;
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
	private List<Integer> columnsWidth;
}
package org.yht.exps.express.model.systemMng.search;

import java.util.List;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.Filter;
import org.yht.exps.express.model.search.SearchList;
import org.yht.exps.express.model.search.Sort;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchHubQnAList", description = "eHub문의사항 리스트 검색 조건")
@Alias("searchHubQnAList")
public class SearchHubQnAList extends SearchList {

	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="검색조건", example="제목,거래처")
	private String srchQnA;
	@ApiModelProperty(value="검색 거래처코드")
	private String srchCustomerCode;
	private List<String> srchCustomerCodeList;
	@ApiModelProperty(value="검색 주문번호")
	private String srchPoNo;
	@ApiModelProperty(value="검색 제목")
	private String srchTitle;
	@ApiModelProperty(value="검색 H.B/L No")
	private String srchHblNo;
	@ApiModelProperty(value="검색 BusinessType")
	private String srchBusinessType;
	@ApiModelProperty(value="검색 문의유형")
	private String srchQuestionType;
	@ApiModelProperty(value="검색 완료여부")
	private String srchCompleteYnh;
	@ApiModelProperty(value="거래처코드", example="aaaa")
	private String ecmCustomerCode;
	@ApiModelProperty(value="대리점 구분", example="HC, SC")
	private String ecmCustomerType;
	@ApiModelProperty(value="관리번호")
	private String referenceNo;
	@ApiModelProperty(value="일련번호")
	private int seqNo;
	@ApiModelProperty(value="제목")
	private String title;
	
	@ApiModelProperty(value="조회타입", example="excel")
	private String srchMode;
	@ApiModelProperty(value="그리드기본정렬", example="HBL_NO DESC")
	private String defaultSort;
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
}

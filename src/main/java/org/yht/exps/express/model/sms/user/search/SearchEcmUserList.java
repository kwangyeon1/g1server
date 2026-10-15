package org.yht.exps.express.model.sms.user.search;

import java.util.List;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.Filter;
import org.yht.exps.express.model.search.Sort;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchEcmUserList", description = "ECM 웹 사용자  리스트 검색 조건")
@Alias("searchEcmUserList")
public class SearchEcmUserList {

	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="검색거래처코드", example="거래처코드")
	private String srchCustomerCode;
	
	@ApiModelProperty(value="부모Id", example="부모Id")
	private String srchParentIds;
	
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
}

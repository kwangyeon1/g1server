package org.yht.exps.express.model.selectMng.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchSelfSelectionList", description = "통관목록 자체선별(기초) 리스트 검색 조건")
@Alias("searchSelfSelectionList")
public class SearchSelfSelectionList extends SearchList {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="사용여부")
	private String srchUseYn;
	@ApiModelProperty(value="선별종류")	
	private String srchSelectionTarget;
	@ApiModelProperty(value="대상항목")
	private String srchDataField;
	@ApiModelProperty(value="적용내용")
	private String srchKeyword;
	@ApiModelProperty(value="서류여부")
	private String srchDocumentYn;
	@ApiModelProperty(value="검색방법")
	private String srchOperator;
	
}

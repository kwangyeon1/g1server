package org.yht.exps.express.model.selectMng.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchSelectionItemList", description = "목록선별 세부품목 리스트 검색 조건")
@Alias("searchSelectionItemList")
public class SearchSelectionItemList extends SearchList {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="오더관리번호")
	private String srchOdJobNo;
}

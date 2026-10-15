package org.yht.exps.express.model.selectMng.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchGoodsSelectionInfo", description = "통관목록 자동선별(기초) 리스트 검색 조건")
@Alias("searchGoodsSelectionInfo")
public class SearchGoodsSelectionInfo extends SearchList {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="품명")
	private String srchItemName;
	@ApiModelProperty(value="품명 like 검색 여부")
	private String srchItemNameLikeOption;
	
}

package org.yht.exps.express.model.selectMng.search;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchSelfSelection", description = "통관목록 자체선별(기초) 조회")
@Alias("searchSelfSelection")
public class SearchSelfSelection{
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="관리번호")
	private String jobNo;
	
}

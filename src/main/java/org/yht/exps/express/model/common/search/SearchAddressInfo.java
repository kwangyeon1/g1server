package org.yht.exps.express.model.common.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchAddressInfo", description = "주소에서 기타정보 조회")
@Alias("searchAddressInfo")
public class SearchAddressInfo extends SearchList{
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="주소")
	private String srchAddress;
}

package org.yht.exps.express.model.systemMng.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchInterfaceKeyCountList", description = "InterfaceKey건수조회")
@Alias("searchInterfaceKeyCountList")
public class SearchInterfaceKeyCountList extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="거래처")
	private String srchCustomerCode;
	@ApiModelProperty(value="조회년도")
	private String srchYear;
}
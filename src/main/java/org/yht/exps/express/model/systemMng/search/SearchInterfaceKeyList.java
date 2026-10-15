package org.yht.exps.express.model.systemMng.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchInterfaceKeyList", description = "InterfaceKey조회")
@Alias("searchInterfaceKeyList")
public class SearchInterfaceKeyList extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="사용여부", example="Y or N")
	private String srchUseYn;
	@ApiModelProperty(value="거래처")
	private String srchCustomerCode;
	
}
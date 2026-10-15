package org.yht.exps.express.model.systemMng.search;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchInterfaceKey", description = "InterfaceKey 조회")
@Alias("searchInterfaceKey")
public class SearchInterfaceKey{
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="거래처코드")
	private String srchCustomerCode;
}

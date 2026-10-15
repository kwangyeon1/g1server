package org.yht.exps.express.model.systemMng.search;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchZipCode", description = "ZipCode 조회")
@Alias("searchZipCode")
public class SearchZipCode {

	@ApiModelProperty(value="우편번호")
	private String zipCode;
}

package org.yht.exps.express.model.eCommerce.changeDelivery.search;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchChangeDelivery", description = "송장교체(주소변경)")
@Alias("searchChangeDelivery")
public class SearchChangeDelivery{
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="일련번호")
	private String seqNoId;
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	
}

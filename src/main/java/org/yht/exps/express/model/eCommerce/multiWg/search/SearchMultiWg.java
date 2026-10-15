package org.yht.exps.express.model.eCommerce.multiWg.search;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchMultiWg", description = "오더조회")
@Alias("searchMultiWg")
public class SearchMultiWg{
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	
}

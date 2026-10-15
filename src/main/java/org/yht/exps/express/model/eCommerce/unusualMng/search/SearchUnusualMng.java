package org.yht.exps.express.model.eCommerce.unusualMng.search;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchUnusualMng", description = "반품관리")
@Alias("searchUnusualMng")
public class SearchUnusualMng{
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="일련번호")
	private String seqNoId;
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	
}

package org.yht.exps.express.model.edi.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchManifest", description = "전자상거래 적하목록 오더 검색 ")
@Alias("searchManifest")
public class SearchManifest extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="목록신고제출번호")
	private String simpleDocNo;
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	
	
}

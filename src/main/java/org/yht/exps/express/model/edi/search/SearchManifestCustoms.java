package org.yht.exps.express.model.edi.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchManifestCustoms", description = "전자상거래 적하목록 신고세관/과 검색 ")
@Alias("SearchManifestCustoms")
public class SearchManifestCustoms extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="항공/해상")
	private String bound;
	@ApiModelProperty(value="수입/수출")
	private String boundType;
	@ApiModelProperty(value="port")
	private String portCode;
	@ApiModelProperty(value="특송부호관리조회여부")
	private String srchBusEdiCodeYn;
	@ApiModelProperty(value="특송부호관리코드명조회여부")
	private String srchBusEdiCodeNameYn;
	@ApiModelProperty(value="사용자 언어")
	private String userLanguage;
	@ApiModelProperty(value="대표사업장")
	private String entBusinessCode;
}

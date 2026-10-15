package org.yht.exps.express.model.eCommerce.order.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchElastic;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchCustomsVerificationResult", description = "전자상거래 개인통관부호 검증 결과 리스트 검색 조건")
@Alias("searchCustomsVerificationResult")
public class SearchCustomsVerification extends SearchElastic {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="사업장코드", example="YHTXX-KR")
	private String srchEntBusinessCode;
	@ApiModelProperty(value="솔루션", example="ECM")
	private String srchSolution;
	@ApiModelProperty(value="바운드", example="AIR")
	private String srchBound;
	@ApiModelProperty(value="수출입 구분", example="EXP")
	private String srchBoundType;
}

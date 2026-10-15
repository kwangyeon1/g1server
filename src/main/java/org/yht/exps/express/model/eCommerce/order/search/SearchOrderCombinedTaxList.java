package org.yht.exps.express.model.eCommerce.order.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchOrderCombinedTaxList", description = "전자상거래 합산과세자료 리스트 검색 조건")
@Alias("searchOrderCombinedTaxList")
public class SearchOrderCombinedTaxList extends SearchList {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="합산과세조회타입")
	private String srchTaxType;
	@ApiModelProperty(value="항공/해상구분")
	private String srchBound;
	@ApiModelProperty(value="콘솔여부")
	private String srchConsolYn;
	@ApiModelProperty(value="환율사업장")
	private String entBusinessCode;
	@ApiModelProperty(value="계정 레벨")
	private String level;
	@ApiModelProperty(value="거래처코드")
	private String srchEcmCustomerCode;
	
}

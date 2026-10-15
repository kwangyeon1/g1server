package org.yht.exps.express.model.sms.search;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchEcmCustomerTreeList", description = "ECM 거래처 하위자식 리스트 검색 조건")
@Alias("searchEcmCustomerTreeList")
public class SearchEcmCustomerTreeList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	
	@ApiModelProperty(value="상위사용자Id", example="2010-10")
	private String srchParentId;	
	
}

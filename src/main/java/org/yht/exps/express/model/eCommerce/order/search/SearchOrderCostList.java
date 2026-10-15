package org.yht.exps.express.model.eCommerce.order.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchOrderCostList", description = "추가비용조회조회")
@Alias("searchOrderCostList")
public class SearchOrderCostList extends SearchList {
	
	@ApiModelProperty(value="5자리 도입사코드")
	private String srchEntCode;
	@ApiModelProperty(value="관리번호")
	private String srchOdJobNo;
	@ApiModelProperty(value="비용타입(배송비(DLV)/관세(BRK)/전체(ALL)")
	private String srchChgType;
	
}

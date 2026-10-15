package org.yht.exps.express.model.eCommerce.houseOrder.search;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchHouseOrder", description = "마스터오더조회")
@Alias("searchHouseOrder")
public class SearchHouseOrder{
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="오더관리번호")
	private String srchOdJobNo;
	@ApiModelProperty(value="비엘관리번호")
	private String srchOblJobNo;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String srchBound;
	@ApiModelProperty(value="EMS BL여부")
	private String srchEmsBlYn;
}

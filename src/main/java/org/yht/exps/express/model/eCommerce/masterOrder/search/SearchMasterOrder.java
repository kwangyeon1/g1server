package org.yht.exps.express.model.eCommerce.masterOrder.search;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchMasterOrder", description = "마스터오더조회")
@Alias("searchMasterOrder")
public class SearchMasterOrder{
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
	
//	@ApiModelProperty(value="언어", example="KOR")
//	private String userLanguage;
//	@ApiModelProperty(value="gmtTime")
//	private String gmtTime;
//	@ApiModelProperty(value="userId")
//	private String userId;
//	@ApiModelProperty(value="도입사계약일자")
//	private String contractDate;
//	@ApiModelProperty(value="날짜형식")
//	private String dateFormat;
}

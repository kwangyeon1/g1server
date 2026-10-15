package org.yht.exps.express.model.eCommerce.order.search;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchOrder", description = "오더조회")
@Alias("searchOrder")
public class SearchOrder{
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="암호화여부")
	private String cryptographyYn;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String srchBound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String srchBoundType;
	@ApiModelProperty(value="관리번호")
	private String srchOdJobNo;
	@ApiModelProperty(value="통관구분")
	private String srchCdType;
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="userId")
	private String userId;
	@ApiModelProperty(value="도입사계약일자")
	private String contractDate;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="ems환적여부")
	private String emsYn;
	@ApiModelProperty(value="profile명")
	private String profileName;
}

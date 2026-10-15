package org.yht.exps.express.model.consumerService.order.search;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchOrderCustomer", description = "오더조회")
@Alias("searchOrderCustomer")
public class SearchOrderCustomer{
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="암호화여부")
	private String cryptographyYn;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	
	@ApiModelProperty(value="로그인id")
	private String srchLoginId;
	@ApiModelProperty(value="로그인거래처코드id")
	private String loginCustomerCd;
	@ApiModelProperty(value="로그인거래처Lev")
	private String loginCustomerLev;
	@ApiModelProperty(value="로그인사용자상위거래처Lev")
	private String loginCustomerUpperLev;
	@ApiModelProperty(value="로그인사용자하위거래처Lev")
	private String loginCustomerLowLev;
	@ApiModelProperty(value="profile명")
	private String profileName;
	
	
}

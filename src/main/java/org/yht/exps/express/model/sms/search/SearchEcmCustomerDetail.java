package org.yht.exps.express.model.sms.search;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchEcmCustomerDetail", description = "ECM 거래처  상세조회 검색 조건")
@Alias("searchEcmCustomerDetail")
public class SearchEcmCustomerDetail {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="거래처코드")
	private String customerCode;
	@ApiModelProperty(value="날짜포맷")
	private String dateFormat;
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	@ApiModelProperty(value="서비스코드")
	private String ifServiceCode;
}

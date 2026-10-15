package org.yht.exps.express.model.sms.user.search;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchEcmUserDetail", description = "ECM 웹 사용자  상세조회 검색 조건")
@Alias("searchEcmUserDetail")
public class SearchEcmUserDetail {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="사용자아이디")
	private String userId;
	@ApiModelProperty(value="날짜포맷")
	private String dateFormat;	
}

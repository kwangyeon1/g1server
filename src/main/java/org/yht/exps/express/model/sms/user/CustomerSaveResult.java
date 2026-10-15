package org.yht.exps.express.model.sms.user;

import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "CustomerSaveResult", description = "전자상거래 사용자 Save 결과")
@Alias("customerSaveResult")
public class CustomerSaveResult {
	
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	
	@ApiModelProperty(value="거래처코드")
	private String customerCode;
	
	@ApiModelProperty(value="결과내역")
	private Map<String, Object> resultData;
	
}

package org.yht.exps.express.model.sms.user;

import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "UserSaveResult", description = "전자상거래 사용자 Save 결과")
@Alias("userSaveResult")
public class UserSaveResult {
	
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	
	@ApiModelProperty(value="사용자아이디")
	private String userId;
	
	@ApiModelProperty(value="결과내역")
	private Map<String, Object> resultData;
}

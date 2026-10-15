package org.yht.exps.express.model.api.hms;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "HmsTokenResult", description = "HMS TokenResult")
@Alias("hmsTokenResult")
public class HmsTokenResult{
	@ApiModelProperty(value="access_token")
	private String access_token;
	@ApiModelProperty(value="token_type")
	private String token_type;
	@ApiModelProperty(value="expires_in")
	private Long expires_in;
}

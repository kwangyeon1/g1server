package org.yht.exps.express.model.api.hms;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "HmsToken", description = "HMS Token")
@Alias("hmsToken")
public class HmsToken{
	@ApiModelProperty(value="grant_type")
	private String grant_type;
	@ApiModelProperty(value="client_id")
	private String client_id;
	@ApiModelProperty(value="client_secret")
	private String client_secret;
}

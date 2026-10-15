package org.yht.exps.express.model.api.hms;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "HmsResponse", description = "HMS Response")
@Alias("hmsResponse")
public class HmsResponse{
	@ApiModelProperty(value="ReturnCode")
	private String returnCode;
	@ApiModelProperty(value="ReturnMessage")
	private String returnMessage;
}

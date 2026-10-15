package org.yht.exps.express.model.api.hms;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "HmsResult", description = "HMS Result")
@Alias("hmsResult")
public class HmsResult{
	@ApiModelProperty(value="Response")
	private HmsResponse response;
}

package org.yht.exps.express.model.api.hms;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "Hms", description = "HMS")
@Alias("hms")
public class Hms{
	@ApiModelProperty(value="Meta")
	private HmsMeta meta;
	@ApiModelProperty(value="Payload")
	private HmsPayload payload;
}

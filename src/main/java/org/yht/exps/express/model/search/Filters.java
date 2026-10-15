package org.yht.exps.express.model.search;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "Filters", description = "그리드필터리스트")
@Alias("filters")
public class Filters{
	@ApiModelProperty(value="필터타입")
	private String operator;
	@ApiModelProperty(value="필터필드")
	private String field;
	@ApiModelProperty(value="필터값")
	private String value;
}

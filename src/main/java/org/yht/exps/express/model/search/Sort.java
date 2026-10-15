package org.yht.exps.express.model.search;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "Sort", description = "그리드정렬")
@Alias("sort")
public class Sort {
	@ApiModelProperty(value="정렬필드")
	private String field;
	@ApiModelProperty(value="정렬타입")
	private String dir;
}

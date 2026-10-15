package org.yht.exps.express.model.search;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "Filter", description = "그리드필터")
@Alias("filter")
public class Filter{
	@ApiModelProperty(value="'AND' or 'OR'")
	private String logic;
	@ApiModelProperty(value="필터리스트")
	private List<Filters> filters;
}

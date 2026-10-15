package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ExcelError", description = "전자상거래 B/L 오류")
@Alias("excelError")
public class ExcelError {
	@ApiModelProperty(value="Job No")
	private String id;
	@ApiModelProperty(value="Error Condition")
	private ExcelErrorCondition errorCondition;
	@ApiModelProperty(value="Error Line")
	private int line;
	@ApiModelProperty(value="number")
	private int num;
	@ApiModelProperty(value="Error Type")
	private String errorType;
	@ApiModelProperty(value="Error Reason")
	private String errorReason;
	@ApiModelProperty(value="Error Line Data")
	private String lineData;
	@ApiModelProperty(value="Error Table")
	private String table;
	@ApiModelProperty(value="Error Field")
	private String field;
	@ApiModelProperty(value="Error Field Name")
	private String fieldName;
	@ApiModelProperty(value="Error Value")
	private String fieldValue;
	@ApiModelProperty(value="Error Message")
	private String errorMessage;
}

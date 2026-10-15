package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ExcelErrorCondition", description = "엑셀 업로드 에러 조건")
@Alias("excelErrorCondition")
public class ExcelErrorCondition {
	@ApiModelProperty(value="데이터 길이")
	private double itemLength;
	@ApiModelProperty(value="데이터 타입")
	private String itemType;
	@ApiModelProperty(value="데이터 필수 여부")
	private String itemRequired;
	@ApiModelProperty(value="허용문자")
	private String itemPossible;
}

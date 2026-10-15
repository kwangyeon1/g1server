package org.yht.exps.express.model.bms.bmsEc;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import org.apache.ibatis.type.Alias;

@Data
@ApiModel(value = "BmsEcData", description = "EC 데이터")
@Alias("bmsEcData")
public class BmsEcData {
	 @ApiModelProperty(value="원본 인터페이스ID")
	 private String orgInterfaceId;
	 @ApiModelProperty(value="인터페이스ID")
	 private String interfaceId;
	 @ApiModelProperty(value="인터페이스ID-체번")
	 private String interfaceIdNew;
	 @ApiModelProperty(value="인터페이스SEQ")
	 private Integer interfaceSeq;
	 @ApiModelProperty(value="CUD")
	 private String isCud;
}
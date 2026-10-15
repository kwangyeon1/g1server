package org.yht.exps.express.model.api.hms;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "HmsPackage", description = "HMS Package")
@Alias("hmsPackage")
public class HmsPackage{
	@ApiModelProperty(value="Package No")
	private String packageNo;
	@ApiModelProperty(value="Hold")
	private String hold;
	@ApiModelProperty(value="scan result")
	private String scanResult;
	@ApiModelProperty(value="inspection details")
	private HmsInspection inspection;
}

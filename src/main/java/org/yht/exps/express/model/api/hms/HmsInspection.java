package org.yht.exps.express.model.api.hms;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "HmsInspection", description = "HMS Package")
@Alias("hmsInspection")
public class HmsInspection{
	@ApiModelProperty(value="whether the pacakge need inspection")
	private boolean needInspection;
	@ApiModelProperty(value="inspectionType")
	private String inspectionType;
}

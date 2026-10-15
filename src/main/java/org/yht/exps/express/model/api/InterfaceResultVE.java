package org.yht.exps.express.model.api;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class InterfaceResultVE {
	private InterfaceReturnDataVE returnData;
	
	@ApiModelProperty(name = "Return")
	public InterfaceReturnDataVE getReturnData() {
		return returnData;
	}
	public void setReturnData(InterfaceReturnDataVE returnData) {
		this.returnData = returnData;
	}
}

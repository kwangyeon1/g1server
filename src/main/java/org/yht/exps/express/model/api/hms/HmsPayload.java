package org.yht.exps.express.model.api.hms;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "HmsPayload", description = "HMS Payload")
@Alias("hmsPayload")
public class HmsPayload{
	@ApiModelProperty(value="Shipment No")
	private String shipmentNo;
	@ApiModelProperty(value="scanDate")
	private String scanDate;
	@ApiModelProperty(value="releaseApprovalCode")
	private String releaseApprovalCode;
	@ApiModelProperty(value="releaseApprovalNumber")
	private String releaseApprovalNumber;
	@ApiModelProperty(value="Packages")
	private List<HmsPackage> packages;
}

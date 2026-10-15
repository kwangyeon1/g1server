package org.yht.exps.express.model.common;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "CustomsConsigNameAddrInfo", description = "수하인 이름, 주소 정보")
@Alias("customsConsigNameAddrInfo")
public class CustomsConsigNameAddrInfo {
	
	@ApiModelProperty(value="수하인이름")
	private String consigneeNameLoc;
	@ApiModelProperty(value="수하인주소")
	private String consigneeAddressLoc;
	
	@Override
	public String toString() {
		return "CustomsConsigNameAddrInfo [consigneeNameLoc=" + consigneeNameLoc + ", consigneeAddressLoc="
				+ consigneeAddressLoc + "]";
	}
	
	
}

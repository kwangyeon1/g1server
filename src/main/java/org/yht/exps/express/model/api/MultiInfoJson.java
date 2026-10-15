package org.yht.exps.express.model.api;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Alias("MultiInfoJson")
public class MultiInfoJson {
	private String DeliveryNo;
	private String ChangeDeliveryCode;
	private String ChangeDeliveryNo;
	
	@JsonProperty("DeliveryNo")
	public String getDeliveryNo() {
		return DeliveryNo;
	}
	public void setDeliveryNo(String deliveryNo) {
		DeliveryNo = deliveryNo;
	}
	@JsonProperty("ChangeDeliveryCode")
	public String getChangeDeliveryCode() {
		return ChangeDeliveryCode;
	}
	public void setChangeDeliveryCode(String changeDeliveryCode) {
		ChangeDeliveryCode = changeDeliveryCode;
	}
	@JsonProperty("ChangeDeliveryNo")
	public String getChangeDeliveryNo() {
		return ChangeDeliveryNo;
	}
	public void setChangeDeliveryNo(String changeDeliveryNo) {
		ChangeDeliveryNo = changeDeliveryNo;
	}
	
}

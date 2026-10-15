package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CustomsInfoJsonVE{
	private String OrdId;
	private String HBLNo;
	private String CustomsClearance;
	private String Inspection;
	private String WithdrawalCode;
	private String WithdrawalReason;
	private String DeliveryCode;
	
	@JsonProperty("OrdId")
	public String getOrdId() {
		return OrdId;
	}
	public void setOrdId(String ordId) {
		OrdId = ordId;
	}
	@JsonProperty("HBLNo")
	public String getHBLNo() {
		return HBLNo;
	}
	public void setHBLNo(String hBLNo) {
		HBLNo = hBLNo;
	}
	@JsonProperty("CustomsClearance")
	public String getCustomsClearance() {
		return CustomsClearance;
	}
	public void setCustomsClearance(String customsClearance) {
		CustomsClearance = customsClearance;
	}
	@JsonProperty("Inspection")
	public String getInspection() {
		return Inspection;
	}
	public void setInspection(String inspection) {
		Inspection = inspection;
	}
	@JsonProperty("WithdrawalCode")
	public String getWithdrawalCode() {
		return WithdrawalCode;
	}
	public void setWithdrawalCode(String withdrawalCode) {
		WithdrawalCode = withdrawalCode;
	}
	@JsonProperty("WithdrawalReason")
	public String getWithdrawalReason() {
		return WithdrawalReason;
	}
	public void setWithdrawalReason(String withdrawalReason) {
		WithdrawalReason = withdrawalReason;
	}
	@JsonProperty("DeliveryCode")
	public String getDeliveryCode() {
		return DeliveryCode;
	}
	public void setDeliveryCode(String deliveryCode) {
		DeliveryCode = deliveryCode;
	}
}
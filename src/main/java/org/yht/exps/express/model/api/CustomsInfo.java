package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="CustomsInfo", propOrder = {"ordId", "HBLNo", "customsClearance", "inspection", "withdrawalCode", "withdrawalReason", "deliveryCode"})
public class CustomsInfo{
	private String OrdId;
	private String HBLNo;
	private String CustomsClearance;
	private String Inspection;
	private String WithdrawalCode;
	private String WithdrawalReason;
	private String DeliveryCode;
	
	@XmlElement(name="OrdId")
	public String getOrdId() {
		return OrdId;
	}
	public void setOrdId(String ordId) {
		OrdId = ordId;
	}
	@XmlElement(name="HBLNo")
	public String getHBLNo() {
		return HBLNo;
	}
	public void setHBLNo(String hBLNo) {
		HBLNo = hBLNo;
	}
	@XmlElement(name = "CustomsClearance")
	public String getCustomsClearance() {
		return CustomsClearance;
	}
	public void setCustomsClearance(String customsClearance) {
		CustomsClearance = customsClearance;
	}
	@XmlElement(name = "Inspection")
	public String getInspection() {
		return Inspection;
	}
	public void setInspection(String inspection) {
		Inspection = inspection;
	}
	@XmlElement(name = "WithdrawalCode")
	public String getWithdrawalCode() {
		return WithdrawalCode;
	}
	public void setWithdrawalCode(String withdrawalCode) {
		WithdrawalCode = withdrawalCode;
	}
	@XmlElement(name = "WithdrawalReason")
	public String getWithdrawalReason() {
		return WithdrawalReason;
	}
	public void setWithdrawalReason(String withdrawalReason) {
		WithdrawalReason = withdrawalReason;
	}
	@XmlElement(name = "DeliveryCode")
	public String getDeliveryCode() {
		return DeliveryCode;
	}
	public void setDeliveryCode(String deliveryCode) {
		DeliveryCode = deliveryCode;
	}
}
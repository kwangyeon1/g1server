package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="MultiInfo", propOrder = {"deliveryNo", "changeDeliveryCode", "changeDeliveryNo"})
public class MultiInfo {
	private String DeliveryNo;
	private String ChangeDeliveryCode;
	private String ChangeDeliveryNo;
	
	@XmlElement(name="DeliveryNo")
	public String getDeliveryNo() {
		return DeliveryNo;
	}
	public void setDeliveryNo(String deliveryNo) {
		DeliveryNo = deliveryNo;
	}
	@XmlElement(name="ChangeDeliveryCode")
	public String getChangeDeliveryCode() {
		return ChangeDeliveryCode;
	}
	public void setChangeDeliveryCode(String changeDeliveryCode) {
		ChangeDeliveryCode = changeDeliveryCode;
	}
	@XmlElement(name="ChangeDeliveryNo")
	public String getChangeDeliveryNo() {
		return ChangeDeliveryNo;
	}
	public void setChangeDeliveryNo(String changeDeliveryNo) {
		ChangeDeliveryNo = changeDeliveryNo;
	}
	
}

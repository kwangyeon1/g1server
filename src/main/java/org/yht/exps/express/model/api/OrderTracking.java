package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="OrderTracking", propOrder = {"language", "ordId", "HBLNo"})

public class OrderTracking{
	private String Language;
	private String OrdId;
	private String HBLNo;
	
	@XmlElement(name="Language")
	public String getLanguage() {
		return Language;
	}
	public void setLanguage(String language) {
		Language = language;
	}
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
}
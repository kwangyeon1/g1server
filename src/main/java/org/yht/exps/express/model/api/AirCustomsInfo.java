package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="customsInfo", propOrder = {"customNm"})

public class AirCustomsInfo {
	private String CustomNm;
	
	@XmlElement(name="CustomNm")
	public String getCustomNm() {
		return CustomNm;
	}
	public void setCustomNm(String customNm) {
		CustomNm = customNm;
	}
}

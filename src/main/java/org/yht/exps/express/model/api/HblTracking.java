package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="BlTracking", propOrder = {"hblNo"})

public class HblTracking{
	private String HblNo;
	
	@XmlElement(name="HBLNo")
	public String getHblNo() {
		return HblNo;
	}
	public void setHblNo(String hblNo) {
		HblNo = hblNo;
	}
}
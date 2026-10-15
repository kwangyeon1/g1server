package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ItemOrder", propOrder = {"ordId"})

public class ItemOrder{
	private String OrdId;

	
	@XmlElement(name="OrdId")
	public String getOrdId() {
		return OrdId;
	}
	public void setOrdId(String ordId) {
		this.OrdId = ordId;
	}
	
}
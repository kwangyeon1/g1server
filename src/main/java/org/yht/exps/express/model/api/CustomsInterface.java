package org.yht.exps.express.model.api;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "root")
@XmlType(name="root", propOrder = {"accessIdentifier","customsInfo"})
public class CustomsInterface{

	private AccessIdentifier accessIdentifier;
	private List<CustomsInfoList> customsInfo;
	
	@XmlElement(name = "AccessIdentifier")
	public AccessIdentifier getAccessIdentifier() {
		return accessIdentifier;
	}
	public void setAccessIdentifier(AccessIdentifier accessIdentifier) {
		this.accessIdentifier = accessIdentifier;
	}
	
	@XmlElement(name = "CustomsInfo")
	public List<CustomsInfoList> getCustomsInfo() {
		return customsInfo;
	}
	public void setCustomsInfo(List<CustomsInfoList> customsInfo) {
		this.customsInfo = customsInfo;
	}
}
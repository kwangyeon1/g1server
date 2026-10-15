package org.yht.exps.express.model.api;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "root")
@XmlType(name="root", propOrder = {"accessIdentifier","orderManifest"})
public class AirInterface{

	private AccessIdentifier accessIdentifier;
	private List<AirOrderManifest> orderManifest;
	
	@XmlElement(name = "AccessIdentifier")
	public AccessIdentifier getAccessIdentifier() {
		return accessIdentifier;
	}
	public void setAccessIdentifier(AccessIdentifier accessIdentifier) {
		this.accessIdentifier = accessIdentifier;
	}
	
	@XmlElement(name = "OrderManifest")
	public List<AirOrderManifest> getOrderManifest() {
		return orderManifest;
	}
	public void setOrderManifest(List<AirOrderManifest> orderManifest) {
		this.orderManifest = orderManifest;
	}
}
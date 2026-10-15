package org.yht.exps.express.model.api;


import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "root")
@XmlType(name="root", propOrder = {"accessIdentifier", "orderManifest_ocean"})
public class OceanInterface{

	private AccessIdentifier accessIdentifier;
	private List<OceanOrderManifest> orderManifest_ocean;
	
	@XmlElement(name = "AccessIdentifier")
	public AccessIdentifier getAccessIdentifier() {
		return accessIdentifier;
	}
	public void setAccessIdentifier(AccessIdentifier accessIdentifier) {
		this.accessIdentifier = accessIdentifier;
	}
	
	@XmlElement(name = "OrderManifest")
	public List<OceanOrderManifest> getOrderManifest_ocean() {
		return orderManifest_ocean;
	}
	public void setOrderManifest_ocean(List<OceanOrderManifest> orderManifest_ocean) {
		this.orderManifest_ocean = orderManifest_ocean;
	}
}
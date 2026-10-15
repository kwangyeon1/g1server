package org.yht.exps.express.model.api;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Data;

@XmlRootElement(name = "root")
@XmlType(name="root", propOrder = {"accessIdentifier", "orderTracking"})
public class Tracking_interface{

	private AccessIdentifier accessIdentifier;
	private List<OrderTracking> orderTracking; 
	
	@XmlElement(name = "AccessIdentifier")
	public AccessIdentifier getAccessIdentifier() {
		return accessIdentifier;
	}
	public void setAccessIdentifier(AccessIdentifier accessIdentifier) {
		this.accessIdentifier = accessIdentifier;
	}
	@XmlElement(name = "OrderTracking")
	public List<OrderTracking> getOrderTracking() {
		return orderTracking;
	}
	public void setOrderTracking(List<OrderTracking> orderTracking) {
		this.orderTracking = orderTracking;
	}
	
}
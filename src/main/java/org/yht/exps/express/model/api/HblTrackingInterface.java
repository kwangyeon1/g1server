package org.yht.exps.express.model.api;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "root")
@XmlType(name="root", propOrder = {"accessIdentifier", "blTracking"})
public class HblTrackingInterface{

	private AccessIdentifier accessIdentifier;
	private List<HblTracking> blTracking; 
	
	@XmlElement(name = "AccessIdentifier")
	public AccessIdentifier getAccessIdentifier() {
		return accessIdentifier;
	}
	public void setAccessIdentifier(AccessIdentifier accessIdentifier) {
		this.accessIdentifier = accessIdentifier;
	}
	@XmlElement(name = "BlTracking")
	public List<HblTracking> getBlTracking() {
		return blTracking;
	}
	public void setBlTracking(List<HblTracking> blTracking) {
		this.blTracking = blTracking;
	}
	
}
package org.yht.exps.express.model.api;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "root")
@XmlType(name="root", propOrder = {"accessIdentifier","hblManifest"})
public class HblInterface{

	private AccessIdentifier accessIdentifier;
	private List<HblManifest> hblManifest;
	
	@XmlElement(name = "AccessIdentifier")
	public AccessIdentifier getAccessIdentifier() {
		return accessIdentifier;
	}
	public void setAccessIdentifier(AccessIdentifier accessIdentifier) {
		this.accessIdentifier = accessIdentifier;
	}
	
	@XmlElement(name = "HblManifest")
	public List<HblManifest> getHblManifest() {
		return hblManifest;
	}
	public void setHblManifest(List<HblManifest> hblManifest) {
		this.hblManifest = hblManifest;
	}
}
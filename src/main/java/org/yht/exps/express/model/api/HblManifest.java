package org.yht.exps.express.model.api;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "HblManifest", propOrder = {"hblInfo","itemInfo"})

public class HblManifest{
	private HblInfo hblInfo;
	private List<HblItemInfo> itemInfo;
	
	@XmlElement(name="HblInfo")
	public HblInfo getHblInfo() {
		return hblInfo;
	}
	public void setHblInfo(HblInfo hblInfo) {
		this.hblInfo = hblInfo;
	}
	
	@XmlElement(name="ItemInfo")
	public List<HblItemInfo> getItemInfo() {
		return itemInfo;
	}
	public void setItemInfo(List<HblItemInfo> itemInfo) {
		this.itemInfo = itemInfo;
	}
}
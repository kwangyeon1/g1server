package org.yht.exps.express.model.api;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="OrderManifest", propOrder = {"orderInfo_ocean", "itemInfo_ocean", "containerInfo_ocean"})

public class OceanOrderManifest{
	
	private OceanOrderInfo orderInfo_ocean;
	private List<OceanItemInfo> itemInfo_ocean;
	private List<OceanContainerInfo> containerInfo_ocean;
	
	@XmlElement(name="OrderInfo")
	public OceanOrderInfo getOrderInfo_ocean() {
		return orderInfo_ocean;
	}
	public void setOrderInfo_ocean(OceanOrderInfo orderInfo_ocean) {
		this.orderInfo_ocean = orderInfo_ocean;
	}
	
	@XmlElement(name="ItemInfo")
	public List<OceanItemInfo> getItemInfo_ocean() {
		return itemInfo_ocean;
	}
	public void setItemInfo_ocean(List<OceanItemInfo> itemInfo_ocean) {
		this.itemInfo_ocean = itemInfo_ocean;
	}
	
	@XmlElement(name="ContainerInfo")
	public List<OceanContainerInfo> getContainerInfo_ocean() {
		return containerInfo_ocean;
	}
	public void setContainerInfo_ocean(List<OceanContainerInfo> containerInfo_ocean) {
		this.containerInfo_ocean = containerInfo_ocean;
	}
}
package org.yht.exps.express.model.api;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "OrderManifest", propOrder = {"orderInfo","itemInfo","masterInfo","customsInfo"})

public class AirOrderManifest{
	private AirOrderInfo orderInfo;
	private List<AirItemInfo> itemInfo;
	private List<AirMasterInfo> masterInfo;
	private List<AirCustomsInfo> customsInfo;
	
	@XmlElement(name="OrderInfo")
	public AirOrderInfo getOrderInfo() {
		return orderInfo;
	}
	public void setOrderInfo(AirOrderInfo orderInfo) {
		this.orderInfo = orderInfo;
	}
	
	@XmlElement(name="ItemInfo")
	public List<AirItemInfo> getItemInfo() {
		return itemInfo;
	}
	public void setItemInfo(List<AirItemInfo> itemInfo) {
		this.itemInfo = itemInfo;
	}
	
	@XmlElement(name="MasterInfo")
	public List<AirMasterInfo> getMasterInfo() {
		return masterInfo;
	}
	public void setMasterInfo(List<AirMasterInfo> masterInfo) {
		this.masterInfo = masterInfo;
	}
	
	// UPS 항공수출 오더 API 항목
	@XmlElement(name="CustomsInfo")
	public List<AirCustomsInfo> getCustomsInfo() {
		return customsInfo;
	}
	public void setCustomsInfo(List<AirCustomsInfo> customsInfo) {
		this.customsInfo = customsInfo;
	}
}
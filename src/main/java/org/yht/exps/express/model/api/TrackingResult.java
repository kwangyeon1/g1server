package org.yht.exps.express.model.api;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "root")
@XmlType(name="root", propOrder = {"returnData", "returnTrackingData", "customsInfo", "multiInfo", "inventoryInfo"})
public class TrackingResult{

	private InterfaceReturnData returnData;
	private List<ReturnTrackingData> returnTrackingData;
	private List<CustomsInfo> customsInfo;
	private List<MultiInfo> multiInfo;
	private List<InventoryInfo> inventoryInfo;
	
	@XmlElement(name = "Return")
	public InterfaceReturnData getReturnData() {
		return returnData;
	}
	public void setReturnData(InterfaceReturnData returnData) {
		this.returnData = returnData;
	}
	
	@XmlElement(name = "OrderTracking")
	public List<ReturnTrackingData> getReturnTrackingData() {
		return returnTrackingData;
	}
	public void setReturnTrackingData(List<ReturnTrackingData> returnTrackingData) {
		this.returnTrackingData = returnTrackingData;
	}
	
	@XmlElement(name = "CustomsInfo")
	public List<CustomsInfo> getCustomsInfo() {
		return customsInfo;
	}
	public void setCustomsInfo(List<CustomsInfo> customsInfo) {
		this.customsInfo = customsInfo;
	}
	
	@XmlElement(name = "MultiInfo")
	public List<MultiInfo> getMultiInfo() {
		return multiInfo;
	}
	public void setMultiInfo(List<MultiInfo> multiInfo) {
		this.multiInfo = multiInfo;
	}
	
	@XmlElement(name = "InventoryInfo")
	public List<InventoryInfo> getInventoryInfo() {
		return inventoryInfo;
	}
	public void setInventoryInfo(List<InventoryInfo> inventoryInfo) {
		this.inventoryInfo = inventoryInfo;
	}
	
}
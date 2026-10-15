package org.yht.exps.express.model.api;

import java.util.List;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Alias("trackingResultJson")
public class TrackingResultJson {
	private InterfaceReturnDataJson returnData;
	private List<ReturnTrackingDataJson> returnTrackingData;
	private List<CustomsInfoJsonVE> customsInfo;
	private List<MultiInfoJson> multiInfo;
	private List<InventoryInfoJson> inventoryInfo;
	
	@JsonProperty("Return")
	public InterfaceReturnDataJson getReturnData() {
		return returnData;
	}
	public void setReturnData(InterfaceReturnDataJson returnData) {
		this.returnData = returnData;
	}
	
	@JsonProperty("OrderTracking")
	public List<ReturnTrackingDataJson> getReturnTrackingData() {
		return returnTrackingData;
	}
	public void setReturnTrackingData(List<ReturnTrackingDataJson> returnTrackingData) {
		this.returnTrackingData = returnTrackingData;
	}
	
	@JsonProperty("CustomsInfo")
	public List<CustomsInfoJsonVE> getCustomsInfo() {
		return customsInfo;
	}
	public void setCustomsInfo(List<CustomsInfoJsonVE> customsInfo) {
		this.customsInfo = customsInfo;
	}
	
	@JsonProperty("MultiInfo")
	public List<MultiInfoJson> getMultiInfo() {
		return multiInfo;
	}
	public void setMultiInfo(List<MultiInfoJson> multiInfo) {
		this.multiInfo = multiInfo;
	}
	
	@JsonProperty("InventoryInfo")
	public List<InventoryInfoJson> getInventoryInfo() {
		return inventoryInfo;
	}
	public void setInventoryInfo(List<InventoryInfoJson> inventoryInfo) {
		this.inventoryInfo = inventoryInfo;
	}
}

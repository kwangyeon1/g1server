package org.yht.exps.express.model.api;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Alias("InventoryInfoJson")
public class InventoryInfoJson {
	private String HoldYN;
	private String Location;
	private String StockDateTime;
	private String StockUserName;
	private String CarryoutDate;
	private String CarryoutUser;
	
	@JsonProperty("HoldYN")
	public String getHoldYN() {
		return HoldYN;
	}
	public void setHoldYN(String holdYN) {
		HoldYN = holdYN;
	}
	@JsonProperty("Location")
	public String getLocation() {
		return Location;
	}
	public void setLocation(String location) {
		Location = location;
	}
	@JsonProperty("StockDateTime")
	public String getStockDateTime() {
		return StockDateTime;
	}
	public void setStockDateTime(String stockDateTime) {
		StockDateTime = stockDateTime;
	}
	@JsonProperty("StockUserName")
	public String getStockUserName() {
		return StockUserName;
	}
	public void setStockUserName(String stockUserName) {
		StockUserName = stockUserName;
	}
	@JsonProperty("CarryoutDate")
	public String getCarryoutDate() {
		return CarryoutDate;
	}
	public void setCarryoutDate(String carryoutDate) {
		CarryoutDate = carryoutDate;
	}
	@JsonProperty("CarryoutUser")
	public String getCarryoutUser() {
		return CarryoutUser;
	}
	public void setCarryoutUser(String carryoutUser) {
		CarryoutUser = carryoutUser;
	}
	
}

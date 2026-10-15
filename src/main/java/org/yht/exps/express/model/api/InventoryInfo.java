package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="InventoryInfo", propOrder= {"holdYN", "location", "stockDateTime", "stockUserName", "carryoutDate", "carryoutUser"})
public class InventoryInfo {
	private String HoldYN;
	private String Location;
	private String StockDateTime;
	private String StockUserName;
	private String CarryoutDate;
	private String CarryoutUser;
	
	@XmlElement(name="HoldYN")
	public String getHoldYN() {
		return HoldYN;
	}
	public void setHoldYN(String holdYN) {
		HoldYN = holdYN;
	}
	@XmlElement(name="Location")
	public String getLocation() {
		return Location;
	}
	public void setLocation(String location) {
		Location = location;
	}
	@XmlElement(name="StockDateTime")
	public String getStockDateTime() {
		return StockDateTime;
	}
	public void setStockDateTime(String stockDateTime) {
		StockDateTime = stockDateTime;
	}
	@XmlElement(name="StockUserName")
	public String getStockUserName() {
		return StockUserName;
	}
	public void setStockUserName(String stockUserName) {
		StockUserName = stockUserName;
	}
	@XmlElement(name="CarryoutDate")
	public String getCarryoutDate() {
		return CarryoutDate;
	}
	public void setCarryoutDate(String carryoutDate) {
		CarryoutDate = carryoutDate;
	}
	@XmlElement(name="CarryoutUser")
	public String getCarryoutUser() {
		return CarryoutUser;
	}
	public void setCarryoutUser(String carryoutUser) {
		CarryoutUser = carryoutUser;
	}
	
}

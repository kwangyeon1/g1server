package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;;

@XmlType(name="MasterInfo", propOrder = {"MBLNo", "flightNo", "ETD", "ETA", "depPort"
		, "arrPort", "HBLCount"})

public class AirMasterInfo {
	private String MBLNo;
	private String FlightNo;
	private String ETD;
	private String ETA;
	private String DepPort;
	private String ArrPort;
	private String HBLCount;
	
	@XmlElement(name="MBLNo")
	public String getMBLNo() {
		return MBLNo;
	}
	public void setMBLNo(String mBLNo) {
		MBLNo = mBLNo;
	}
	@XmlElement(name="FlightNo")
	public String getFlightNo() {
		return FlightNo;
	}
	public void setFlightNo(String flightNo) {
		FlightNo = flightNo;
	}
	@XmlElement(name="ETD")
	public String getETD() {
		return ETD;
	}
	public void setETD(String eTD) {
		ETD = eTD;
	}
	@XmlElement(name="ETA")
	public String getETA() {
		return ETA;
	}
	public void setETA(String eTA) {
		ETA = eTA;
	}
	@XmlElement(name="DepPort")
	public String getDepPort() {
		return DepPort;
	}
	public void setDepPort(String depPort) {
		DepPort = depPort;
	}
	@XmlElement(name="ArrPort")
	public String getArrPort() {
		return ArrPort;
	}
	public void setArrPort(String arrPort) {
		ArrPort = arrPort;
	}
	@XmlElement(name="HBLCount")
	public String getHBLCount() {
		return HBLCount;
	}
	public void setHBLCount(String hBLCount) {
		HBLCount = hBLCount;
	}

}


package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="OrderTracking", propOrder = {"hblNo", "trackingCode", "trackingDateTime", "trackingRemark"})
public class HblReturnTracking{
	private String HblNo;
	private String TrackingCode;
	private String TrackingDateTime;
	private String TrackingRemark;
	
	@XmlElement(name="HBLNo")
	public String getHblNo() {
		return HblNo;
	}
	public void setHblNo(String hblNo) {
		HblNo = hblNo;
	}
	@XmlElement(name="TrackingCode")
	public String getTrackingCode() {
		return TrackingCode;
	}
	public void setTrackingCode(String trackingCode) {
		TrackingCode = trackingCode;
	}
	@XmlElement(name="TrackingDateTime")
	public String getTrackingDateTime() {
		return TrackingDateTime;
	}
	public void setTrackingDateTime(String trackingDateTime) {
		TrackingDateTime = trackingDateTime;
	}
	@XmlElement(name="TrackingRemark")
	public String getTrackingRemark() {
		return TrackingRemark;
	}
	public void setTrackingRemark(String trackingRemark) {
		TrackingRemark = trackingRemark;
	}
}
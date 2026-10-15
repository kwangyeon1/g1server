package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="OrderTracking", propOrder = {"ordId", "HBLNo", "trackingCode", "trackingDateTime", "trackingCodeName", "trackingRemark"})
public class ReturnTrackingData{
	private String OrdId;
	private String HBLNo;
	private String TrackingCode;
	private String TrackingDateTime;
	private String TrackingCodeName;
	private String TrackingRemark;
	
	@XmlElement(name="OrdId")
	public String getOrdId() {
		return OrdId;
	}
	public void setOrdId(String ordId) {
		OrdId = ordId;
	}
	@XmlElement(name="HBLNo")
	public String getHBLNo() {
		return HBLNo;
	}
	public void setHBLNo(String hBLNo) {
		HBLNo = hBLNo;
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
	@XmlElement(name="TrackingCodeName")
	public String getTrackingCodeName() {
		return TrackingCodeName;
	}
	public void setTrackingCodeName(String trackingCodeName) {
		TrackingCodeName = trackingCodeName;
	}
	@XmlElement(name="TrackingRemark")
	public String getTrackingRemark() {
		return TrackingRemark;
	}
	public void setTrackingRemark(String trackingRemark) {
		TrackingRemark = trackingRemark;
	}
}
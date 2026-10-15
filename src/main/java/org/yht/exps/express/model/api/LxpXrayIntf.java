package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import io.swagger.annotations.ApiModelProperty;

@XmlRootElement(name = "root")
@XmlType(name="root", propOrder = {"bagId","bagAlias","workStationName", "machineName", "event", "timeStamp"})
public class LxpXrayIntf {
	
	@ApiModelProperty(value="Xray분배 고유번호", example="012345")
	private String bagId;
	@ApiModelProperty(value="판토스 투입화물의 고유번호", example="012345")
	private String bagAlias;
	@ApiModelProperty(value="Xray Work Station 번호", example="012345")
	private String workStationName;
	@ApiModelProperty(value="Xray 기기번호", example="012345")
	private String machineName;
	@ApiModelProperty(value="", example="showing")
	private String event;
	
	@ApiModelProperty(value="분배일시", example="2025-05-23 11:15:22")
	private String timeStamp;

	private String entCode;
	
	public String getBagId() {
		return bagId;
	}

	public void setBagId(String bagId) {
		this.bagId = bagId;
	}

	public String getBagAlias() {
		return bagAlias;
	}

	public void setBagAlias(String bagAlias) {
		this.bagAlias = bagAlias;
	}

	public String getWorkStationName() {
		return workStationName;
	}

	public void setWorkStationName(String workStationName) {
		this.workStationName = workStationName;
	}

	public String getMachineName() {
		return machineName;
	}

	public void setMachineName(String machineName) {
		this.machineName = machineName;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getEntCode() {
		return entCode;
	}

	public void setEntCode(String entCode) {
		this.entCode = entCode;
	}
	
	
	
}

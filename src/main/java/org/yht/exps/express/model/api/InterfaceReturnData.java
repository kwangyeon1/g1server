package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;

public class InterfaceReturnData{

	private String ReturnCode;
	private String ReturnMessage;
	
	@XmlElement(name = "ReturnCode")
	public String getReturnCode() {
		return ReturnCode;
	}
	public void setReturnCode(String returnCode) {
		ReturnCode = returnCode;
	}
	@XmlElement(name = "ReturnMessage")
	public String getReturnMessage() {
		return ReturnMessage;
	}
	public void setReturnMessage(String returnMessage) {
		ReturnMessage = returnMessage;
	}
}
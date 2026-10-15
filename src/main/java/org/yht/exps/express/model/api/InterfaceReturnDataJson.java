package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InterfaceReturnDataJson{

	private String ReturnCode;
	private String ReturnMessage;
	
	@JsonProperty("ReturnCode")
	public String getReturnCode() {
		return ReturnCode;
	}
	public void setReturnCode(String returnCode) {
		ReturnCode = returnCode;
	}
	@JsonProperty("ReturnMessage")
	public String getReturnMessage() {
		return ReturnMessage;
	}
	public void setReturnMessage(String returnMessage) {
		ReturnMessage = returnMessage;
	}
}
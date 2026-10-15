package org.yht.exps.express.model.api;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Alias("interfaceResultJson")
public class InterfaceResultJson {
	@JsonProperty("Return")
	private InterfaceResultDataJson returnData;
	
	@Data
	public static class InterfaceResultDataJson{
		@JsonProperty("ReturnCode")
		private String returnCode;
		@JsonProperty("ReturnMessage")
		private String returnMessage;
	}
}
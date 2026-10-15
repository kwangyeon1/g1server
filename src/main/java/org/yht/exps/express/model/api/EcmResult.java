package org.yht.exps.express.model.api;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value = "EcmResult", description = "EcmResult")
@Alias("ecmResult")
public class EcmResult{
	
	@JsonProperty("Return")
	private EcmReturn Return;
	@JsonProperty("OrderInvoice")
	private OrderInvoice OrderInvoice;
	
	@Data
	public static class EcmReturn {
		@JsonProperty("ReturnCode")
		private String ReturnCode;
		@JsonProperty("ReturnMessage")
		private String ReturnMessage;
	}
	
}
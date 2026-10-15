package org.yht.exps.express.model.api;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value = "EmsResult", description = "EmsResult")
@Alias("emsResult")
public class EmsResult{
	
	@JsonProperty("Return")
	private EmsReturn Return;
	@JsonProperty("OrderInvoice")
	private OrderInvoice OrderInvoice;
	
	@Data
	public static class EmsReturn {
		@JsonProperty("ReturnCode")
		private String ReturnCode;
		@JsonProperty("ReturnMessage")
		private String ReturnMessage;
	}
	
}
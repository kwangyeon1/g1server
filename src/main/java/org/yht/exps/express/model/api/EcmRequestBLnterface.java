package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class EcmRequestBLnterface {

	@JsonProperty("AccessIdentifier")
	private AccessIdentifier accessIdentifier;
	@JsonProperty("OrderInvoice")
	private OrderInvoice orderInvoice;
	
}

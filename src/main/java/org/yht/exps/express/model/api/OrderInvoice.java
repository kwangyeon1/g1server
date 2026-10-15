package org.yht.exps.express.model.api;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value = "OrderInvoice", description = "OrderInvoice")
@Alias("OrderInvoice")
public class OrderInvoice {
	@JsonProperty("OrdId")
	private String OrdId;
	
	@JsonProperty("HblNo")
	private String HblNo;
	
	@JsonProperty("Invoice")
	private String Invoice;
	
	@JsonProperty("InvoiceList")
	private List<Map<String, Object>> invoiceList;
}

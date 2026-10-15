package org.yht.exps.express.model.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class CoupangReturn {
	@JsonAlias({"OrderType", "orderType"})
	private String orderType;
	@JsonAlias({"RequestId", "requestId"})
	private String requestId;
	@JsonAlias({"OrderId", "orderId"})
	private String orderId;
	@JsonAlias({"OrderInvoiceNumber", "orderInvoiceNumber"})
	private String orderInvoiceNumber;
	@JsonAlias({"VendorType", "vendorType"})
	private String vendorType;
	@JsonAlias({"ShippingCompanyCode", "shippingCompanyCode"})
	private String shippingCompanyCode;
	@JsonAlias({"SenderAddressInfo", "senderAddressInfo"})
	private CoupangAddressInfo senderAddressInfo;
	@JsonAlias({"ReceiverAddressInfo", "receiverAddressInfo"})
	private CoupangAddressInfo receiverAddressInfo;
	@JsonAlias({"paidType", "paidType"})
	private String paidType;
	@JsonAlias({"PromisedPickupDate", "promisedPickupDate"})
	private String promisedPickupDate;
	@JsonAlias({"Comments", "comments"})
	private List<String> comments;
	@JsonAlias({"ItemList", "itemList"})
	private List<ItemInfoVE> itemList;
}

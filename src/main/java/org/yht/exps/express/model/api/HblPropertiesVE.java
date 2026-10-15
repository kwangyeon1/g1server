package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class HblPropertiesVE {
	@JsonAlias({"BoxCode", "boxCode"})
	private String boxCode;
	@JsonAlias({"ActualOrderNumber", "actualOrderNumber"})
	private String actualOrderNumber;
	@JsonAlias({"ShippingMethod", "shippingMethod"})
	private String shippingMethod;
	@JsonAlias({"CoupangVendorId", "coupangVendorId"})
	private String coupangVendorId;
	@JsonAlias({"ShippingType", "shippingType"})
	private String shippingType;
	@JsonAlias({"BoxLength", "boxLength"})
	private double dimLength;
	@JsonAlias({"BoxWidth", "boxWidth"})
	private double dimWidth;
	@JsonAlias({"BoxHeight", "boxHeight"})
	private double dimHeight;
	@JsonAlias({"ShipmentBoxId", "shipmentBoxId"})
	private String shipmentBoxId;
}
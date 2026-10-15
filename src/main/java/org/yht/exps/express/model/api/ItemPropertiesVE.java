package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class ItemPropertiesVE {
	@JsonAlias({"InvoiceItemNameLocal", "invoiceItemNameLocal"})
	private String invoiceItemNameLocal;
	@JsonAlias({"GrossWeight", "grossWeight"})
	private String grossWeight;
	@JsonAlias({"NetWeight", "netWeight"})
	private double netWeight;
	@JsonAlias({"BarCode", "barCode"})
	private String barCode;
	@JsonAlias({"VendorItemID", "vendorItemID"})
	private String vendorItemID;
	@JsonAlias({"CoupangRegDate", "coupangRegDate"})
	private String coupangRegDate;
	@JsonAlias({"CoupangURL", "coupangURL"})
	private String coupangURL;
	@JsonAlias({"Battery", "battery"})
	private boolean battery;
	@JsonAlias({"BatteryType", "batteryType"})
	private String batteryType;
	@JsonAlias({"Glass", "glass"})
	private boolean glass;
	@JsonAlias({"Fragile", "fragile"})
	private boolean fragile;
	@JsonAlias({"MakerName", "makerName"})
	private String makerName;
	@JsonAlias({"ModelNumber", "modelNumber"})
	private String modelNumber;
	@JsonAlias({"BrandName", "brandName"})
	private String brandName;
	
}

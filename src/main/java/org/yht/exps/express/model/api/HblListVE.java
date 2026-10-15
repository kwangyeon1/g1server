package org.yht.exps.express.model.api;

import java.util.List;

import org.yht.util.Util;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class HblListVE {
	@JsonAlias({"HblNumber", "hblNumber"})
	private String hblNumber;
	@JsonAlias({"BillingTerm", "billingTerm"})
	private String billingTerm;
	@JsonAlias({"ClearanceType", "clearanceType"})
	private String clearanceType;
	@JsonAlias({"CustomerInfo", "customerInfo"})
	private CustomerInfoVE customerInfo;
	@JsonAlias({"Comments", "comments"})
	private List<String> comments;
	@JsonAlias({"Currency", "currency"})
	private String currency;
	@JsonAlias({"CustomsBrokerCode", "customsBrokerCode"})
	private String customsBrokerCode;
	@JsonAlias({"CustomsRequest", "customsRequest"})
	private String customsRequest;
	@JsonAlias({"DeliveryRequest", "deliveryRequest"})
	private String deliveryRequest;
	@JsonAlias({"EcommerceType", "ecommerceType"})
	private String ecommerceType;
//	@JsonAlias({"ShippingType", "shippingType"})
//	private String shippingType;
	@JsonAlias({"HsCodeOfMainItem", "hsCodeOfMainItem"})
	private String hsCodeOfMainItem;
	@JsonAlias({"ItemList", "itemList"})
	private List<ItemListVE> itemList;
	@JsonAlias({"NumberOfPackages", "numberOfPackages"})
	private int numberOfPackages;
	@JsonAlias({"OrderNumber", "orderNumber"})
	private long orderNumber;
	@JsonAlias({"Origin", "origin"})
	private String origin;
	@JsonAlias({"OriginCountry", "originCountry"})
	private String originCountry;
	@JsonAlias({"ShipmentDescription", "shipmentDescription"})
	private String shipmentDescription;
	@JsonAlias({"SpecialClearanceCompanyNumber", "specialClearanceCompanyNumber"})
	private String specialClearanceCompanyNumber;
	@JsonAlias({"UsageType", "usageType"})
	private String usageType;
	@JsonAlias({"Weight", "weight"})
	private double weight;
	@JsonAlias({"ShipperName", "shipperName"})
	private String shipperName;
	@JsonAlias({"ShipperAddress", "shipperAddress"})
	private String shipperAddress;
	@JsonAlias({"ShipperContactNumber", "shipperContactNumber"})
	private String shipperContactNumber;
	@JsonAlias({"HblProperties", "hblProperties"})
	private HblPropertiesVE hblProperties;
	
	public HblListVE clone() throws CloneNotSupportedException{
		HblListVE vo = null;
		try {
			vo = (HblListVE) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error(Util.getPrintStacTraceString(e));
			throw e;
		}
		return vo;
	}
}

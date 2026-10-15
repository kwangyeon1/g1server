package org.yht.exps.express.model.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class EmsInterface{
	@JsonProperty("AccessIdentifier")
	private AccessIdentifier accessIdentifier;
	@JsonProperty("OrderManifest")
	private List<OrderManifest> orderManifest;
	
	@Data
	public static class OrderManifest {
		@JsonProperty("OrderInfo")
		private OrderInfo orderInfo;
		
		@JsonProperty("ItemInfo")
		private List<ItemInfo> itemInfo;
		
		@JsonProperty("ContainerInfo")
		private List<ContainerInfo> containerInfo;
	}
	
	@Data
	public static class OrderInfo {
		@JsonProperty("Operation")
		private String operation;

		@JsonProperty("OrdId")
		private String ordId;

		@JsonProperty("OrdDt")
		private String ordDt;

		@JsonProperty("AgentCode")
		private String agentCode;

		@JsonProperty("HBLNo")
		private String hblNo;

		@JsonProperty("MBLNo")
		private String mblNo;

		@JsonProperty("BLTypeIE")
		private String blTypeIE;

		@JsonProperty("ETD")
		private String etd;

		@JsonProperty("ETA")
		private String eta;

		@JsonProperty("EmsKpacketNo")
		private String emsKpacketNo;

		@JsonProperty("EmsMailKindCd")
		private String emsMailKindCd;

		@JsonProperty("EmsMailTypeCd")
		private String emsMailTypeCd;

		@JsonProperty("EmsDetailDivCd")
		private String emsDetailDivCd;

		@JsonProperty("EmsContractClass")
		private String emsContractClass;

		@JsonProperty("EmsConsigneeZipcode")
		private String emsConsigneeZipcode;

		@JsonProperty("EmsCustomsDeclareRemark")
		private String emsCustomsDeclareRemark;

		@JsonProperty("EmsWidth")
		private String emsWidth;

		@JsonProperty("EmsLength")
		private String emsLength;

		@JsonProperty("EmsHeight")
		private String emsHeight;

		@JsonProperty("EmsVolumnExcludedYN")
		private String emsVolumnExcludedYN;

		@JsonProperty("GroupNo_ShippingDate")
		private String groupNo_ShippingDate;

		@JsonProperty("GroupNo_AgentCode")
		private String groupNo_AgentCode;

		@JsonProperty("GroupNo_ExchangeOffice")
		private String groupNo_ExchangeOffice;

		@JsonProperty("GroupNo_BagNumber")
		private String groupNo_BagNumber;

		@JsonProperty("GroupNo_BagWeight")
		private String groupNo_BagWeight;

		@JsonProperty("GroupNo_Amount")
		private String groupNo_Amount;

		@JsonProperty("InsuranceYN")
		private String insuranceYN;

		@JsonProperty("InsuranceValue")
		private String insuranceValue;

		@JsonProperty("ShipperName")
		private String shipperName;

		@JsonProperty("ShipperAddress")
		private String shipperAddress;

		@JsonProperty("ShipperAddressA")
		private String shipperAddressA;

		@JsonProperty("ShipperAddressB")
		private String shipperAddressB;

		@JsonProperty("ShipperAddressC")
		private String shipperAddressC;

		@JsonProperty("ShipperTel")
		private String shipperTel;

		@JsonProperty("ShipperTelA")
		private String shipperTelA;

		@JsonProperty("ShipperTelB")
		private String shipperTelB;

		@JsonProperty("ShipperTelC")
		private String shipperTelC;

		@JsonProperty("ShipperTelD")
		private String shipperTelD;

		@JsonProperty("ShipperZipcode")
		private String shipperZipcode;

		@JsonProperty("ShipperCountryCode")
		private String shipperCountryCode;

		@JsonProperty("ConsigneeName")
		private String consigneeName;

		@JsonProperty("ConsigneeAddress")
		private String consigneeAddress;

		@JsonProperty("ConsigneeAddressA")
		private String consigneeAddressA;

		@JsonProperty("ConsigneeAddressB")
		private String consigneeAddressB;

		@JsonProperty("ConsigneeAddressC")
		private String consigneeAddressC;

		@JsonProperty("ConsigneeTel")
		private String consigneeTel;

		@JsonProperty("ConsigneeTelA")
		private String consigneeTelA;

		@JsonProperty("ConsigneeTelB")
		private String consigneeTelB;

		@JsonProperty("ConsigneeTelC")
		private String consigneeTelC;

		@JsonProperty("ConsigneeTelD")
		private String consigneeTelD;

		@JsonProperty("ConsigneeMobile")
		private String consigneeMobile;

		@JsonProperty("ConsigneeZipcode")
		private String consigneeZipcode;

		@JsonProperty("TaxIDNo")
		private String taxIDNo;

		@JsonProperty("DestinationCountryCode")
		private String destinationCountryCode;

		@JsonProperty("CommodityItem")
		private String commodityItem;

		@JsonProperty("TotalQty")
		private String totalQty;

		@JsonProperty("TotalWeight")
		private String totalWeight;

		@JsonProperty("CBM")
		private String cbm;

		@JsonProperty("CurrencyCode")
		private String currencyCode;

		@JsonProperty("AmountTotal")
		private String amountTotal;
		
		@JsonProperty("DeliveryCenterCode")
		private String deliveryCenterCode;
		
		@JsonProperty("DeliveryPayment")
		private String deliveryPayment;
	}
	
	@Data
	public static class ItemInfo {
		@JsonProperty("ItemNm")
		private String itemNm;
		
		@JsonProperty("ItemProductId")
		private String itemProductId;
		
		@JsonProperty("ItemQty")
		private String itemQty;
		
		@JsonProperty("ItemQtyUnit")
		private String itemQtyUnit;
		
		@JsonProperty("ItemGrossWeight")
		private String itemGrossWeight;
		
		@JsonProperty("ItemNetWeight")
		private String itemNetWeight;
		
		@JsonProperty("ItemAmount")
		private String itemAmount;
		
		@JsonProperty("ItemCurrencyCode")
		private String itemCurrencyCode;
		
		@JsonProperty("ItemHsCode")
		private String itemHsCode;
		
		@JsonProperty("ItemOrigin")
		private String itemOrigin;
		
		@JsonProperty("ItemRemark")
		private String itemRemark;
	}
	
	@Data
	public static class ContainerInfo {
		@JsonProperty("ContainerNo")
		private String containerNo;
		
		@JsonProperty("ContainerType")
		private String containerType;
		
		@JsonProperty("SealNo1")
		private String sealNo1;
		
		@JsonProperty("SealNo2")
		private String sealNo2;
		
		@JsonProperty("SealNo3")
		private String sealNo3;
		
		@JsonProperty("Qty")
		private String qty;
		
		@JsonProperty("QtyUnit")
		private String qtyUnit;
		
		@JsonProperty("GrossWeight")
		private String grossWeight;
		
		@JsonProperty("CBM")
		private String cbm;
	}
}
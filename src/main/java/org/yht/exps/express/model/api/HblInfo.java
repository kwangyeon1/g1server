package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlType(name="HblInfo", propOrder = {
		"operation","boundType","obDate","HBLNo","destPort","itemGubn","CType","ETradeType","shipmentType","paymentGubn","totalQty","innerPices","totalWeight","dimWidth","dimLength","dimHeight","dimPieces","commodityItem"
		,"licenseNo","currencyCode","invoiceAmount","arAccountCode","arAccountName","shipperName","shipperAddress","shipperTel","shipperMobile","shipperAttn","shipperDept","consigneeName","consigneeAddress","consigneeTel"
		,"consigneeMobile","consigneeAttn","consigneeClearanceNo","consigneeNameLoc","consigneeAddressLoc","zipcode","consigneeDept","BLRemark","pickupType","pickupAddress","deliverName","deliveryNo","pickupDate"
		,"pickupRemark"
		})

public class HblInfo{
	private String Operation;
	private String BoundType;
	private String ObDate;
	private String HBLNo;
	private String DestPort;
	private String ItemGubn;
	private String CType;
	private String ETradeType;
	private String ShipmentType;
	private String PaymentGubn;
	private String TotalQty;
	private String InnerPices;
	private String TotalWeight;
	private String DimWidth;
	private String DimLength;
	private String DimHeight;
	private String DimPieces;
	private String CommodityItem;
	private String LicenseNo;
	private String CurrencyCode;
	private String InvoiceAmount;
	private String ArAccountCode;
	private String ArAccountName;
	private String ShipperName;
	private String ShipperAddress;
	private String ShipperTel;
	private String ShipperMobile;
	private String ShipperAttn;
	private String ShipperDept;
	private String ConsigneeName;
	private String ConsigneeAddress;
	private String ConsigneeTel;
	private String ConsigneeMobile;
	private String ConsigneeAttn;
	private String ConsigneeClearanceNo;
	private String ConsigneeNameLoc;
	private String ConsigneeAddressLoc;
	private String Zipcode;
	private String ConsigneeDept;
	private String BLRemark;
	private String PickupType;
	private String PickupAddress;
	private String DeliverName;
	private String DeliveryNo;
	private String PickupDate;
	private String PickupRemark;
	
	
	@XmlElement(name="Operation")
	public String getOperation() {
		return Operation;
	}
	public void setOperation(String operation) {
		Operation = operation;
	}
	@XmlElement(name="HBLNo")
	public String getHBLNo() {
		return HBLNo;
	}
	public void setHBLNo(String hBLNo) {
		HBLNo = hBLNo;
	}
	@XmlElement(name="ShipperName")
	public String getShipperName() {
		return ShipperName;
	}
	public void setShipperName(String shipperName) {
		ShipperName = shipperName;
	}
	@XmlElement(name="ShipperAddress")
	public String getShipperAddress() {
		return ShipperAddress;
	}
	public void setShipperAddress(String shipperAddress) {
		ShipperAddress = shipperAddress;
	}
	@XmlElement(name="ShipperTel")
	public String getShipperTel() {
		return ShipperTel;
	}
	public void setShipperTel(String shipperTel) {
		ShipperTel = shipperTel;
	}
	@XmlElement(name="ShipperMobile")
	public String getShipperMobile() {
		return ShipperMobile;
	}
	public void setShipperMobile(String shipperMobile) {
		ShipperMobile = shipperMobile;
	}
	@XmlElement(name="ConsigneeName")
	public String getConsigneeName() {
		return ConsigneeName;
	}
	public void setConsigneeName(String consigneeName) {
		ConsigneeName = consigneeName;
	}
	@XmlElement(name="ConsigneeAddress")
	public String getConsigneeAddress() {
		return ConsigneeAddress;
	}
	public void setConsigneeAddress(String consigneeAddress) {
		ConsigneeAddress = consigneeAddress;
	}
	@XmlElement(name="ConsigneeTel")
	public String getConsigneeTel() {
		return ConsigneeTel;
	}
	public void setConsigneeTel(String consigneeTel) {
		ConsigneeTel = consigneeTel;
	}
	@XmlElement(name="ConsigneeMobile")
	public String getConsigneeMobile() {
		return ConsigneeMobile;
	}
	public void setConsigneeMobile(String consigneeMobile) {
		ConsigneeMobile = consigneeMobile;
	}
	@XmlElement(name="CType")
	public String getCType() {
		return CType;
	}
	public void setCType(String cType) {
		CType = cType;
	}
	@XmlElement(name="ETradeType")
	public String getETradeType() {
		return ETradeType;
	}
	public void setETradeType(String eTradeType) {
		ETradeType = eTradeType;
	}
	@XmlElement(name="CommodityItem")
	public String getCommodityItem() {
		return CommodityItem;
	}
	public void setCommodityItem(String commodityItem) {
		CommodityItem = commodityItem;
	}
	@XmlElement(name="TotalQty")
	public String getTotalQty() {
		return TotalQty;
	}
	public void setTotalQty(String totalQty) {
		TotalQty = totalQty;
	}
	@XmlElement(name="TotalWeight")
	public String getTotalWeight() {
		return TotalWeight;
	}
	public void setTotalWeight(String totalWeight) {
		TotalWeight = totalWeight;
	}
	@XmlElement(name="CurrencyCode")
	public String getCurrencyCode() {
		return CurrencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		CurrencyCode = currencyCode;
	}
	@XmlElement(name="DeliveryNo")
	public String getDeliveryNo() {
		return DeliveryNo;
	}
	public void setDeliveryNo(String deliveryNo) {
		DeliveryNo = deliveryNo;
	}
	@XmlElement(name="DimWidth")
	public String getDimWidth() {
		return DimWidth;
	}
	public void setDimWidth(String dimWidth) {
		DimWidth = dimWidth;
	}
	@XmlElement(name="DimLength")
	public String getDimLength() {
		return DimLength;
	}
	public void setDimLength(String dimLength) {
		DimLength = dimLength;
	}
	@XmlElement(name="DimHeight")
	public String getDimHeight() {
		return DimHeight;
	}
	public void setDimHeight(String dimHeight) {
		DimHeight = dimHeight;
	}
	@XmlElement(name="DimPieces")
	public String getDimPieces() {
		return DimPieces;
	}
	public void setDimPieces(String dimPieces) {
		DimPieces = dimPieces;
	}
	@XmlElement(name="BoundType")
	public String getBoundType() {
		return BoundType;
	}
	public void setBoundType(String boundType) {
		BoundType = boundType;
	}
	@XmlElement(name="ObDate")
	public String getObDate() {
		return ObDate;
	}
	public void setObDate(String obDate) {
		ObDate = obDate;
	}
	@XmlElement(name="DestPort")
	public String getDestPort() {
		return DestPort;
	}
	public void setDestPort(String destPort) {
		DestPort = destPort;
	}
	@XmlElement(name="ItemGubn")
	public String getItemGubn() {
		return ItemGubn;
	}
	public void setItemGubn(String itemGubn) {
		ItemGubn = itemGubn;
	}
	@XmlElement(name="ShipmentType")
	public String getShipmentType() {
		return ShipmentType;
	}
	public void setShipmentType(String shipmentType) {
		ShipmentType = shipmentType;
	}
	@XmlElement(name="PaymentGubn")
	public String getPaymentGubn() {
		return PaymentGubn;
	}
	public void setPaymentGubn(String paymentGubn) {
		PaymentGubn = paymentGubn;
	}
	@XmlElement(name="InnerPices")
	public String getInnerPices() {
		return InnerPices;
	}
	public void setInnerPices(String innerPices) {
		InnerPices = innerPices;
	}
	@XmlElement(name="LicenseNo")
	public String getLicenseNo() {
		return LicenseNo;
	}
	public void setLicenseNo(String licenseNo) {
		LicenseNo = licenseNo;
	}
	@XmlElement(name="InvoiceAmount")
	public String getInvoiceAmount() {
		return InvoiceAmount;
	}
	public void setInvoiceAmount(String invoiceAmount) {
		InvoiceAmount = invoiceAmount;
	}
	@XmlElement(name="ArAccountCode")
	public String getArAccountCode() {
		return ArAccountCode;
	}
	public void setArAccountCode(String arAccountCode) {
		ArAccountCode = arAccountCode;
	}
	@XmlElement(name="ArAccountName")
	public String getArAccountName() {
		return ArAccountName;
	}
	public void setArAccountName(String arAccountName) {
		ArAccountName = arAccountName;
	}
	@XmlElement(name="ShipperAttn")
	public String getShipperAttn() {
		return ShipperAttn;
	}
	public void setShipperAttn(String shipperAttn) {
		ShipperAttn = shipperAttn;
	}
	@XmlElement(name="ShipperDept")
	public String getShipperDept() {
		return ShipperDept;
	}
	public void setShipperDept(String shipperDept) {
		ShipperDept = shipperDept;
	}
	@XmlElement(name="ConsigneeAttn")
	public String getConsigneeAttn() {
		return ConsigneeAttn;
	}
	public void setConsigneeAttn(String consigneeAttn) {
		ConsigneeAttn = consigneeAttn;
	}
	@XmlElement(name="ConsigneeClearanceNo")
	public String getConsigneeClearanceNo() {
		return ConsigneeClearanceNo;
	}
	public void setConsigneeClearanceNo(String consigneeClearanceNo) {
		ConsigneeClearanceNo = consigneeClearanceNo;
	}
	@XmlElement(name="ConsigneeNameLoc")
	public String getConsigneeNameLoc() {
		return ConsigneeNameLoc;
	}
	public void setConsigneeNameLoc(String consigneeNameLoc) {
		ConsigneeNameLoc = consigneeNameLoc;
	}
	@XmlElement(name="ConsigneeAddressLoc")
	public String getConsigneeAddressLoc() {
		return ConsigneeAddressLoc;
	}
	public void setConsigneeAddressLoc(String consigneeAddressLoc) {
		ConsigneeAddressLoc = consigneeAddressLoc;
	}
	@XmlElement(name="Zipcode")
	public String getZipcode() {
		return Zipcode;
	}
	public void setZipcode(String zipcode) {
		Zipcode = zipcode;
	}
	@XmlElement(name="ConsigneeDept")
	public String getConsigneeDept() {
		return ConsigneeDept;
	}
	public void setConsigneeDept(String consigneeDept) {
		ConsigneeDept = consigneeDept;
	}
	@XmlElement(name="BLRemark")
	public String getBLRemark() {
		return BLRemark;
	}
	public void setBLRemark(String bLRemark) {
		BLRemark = bLRemark;
	}
	@XmlElement(name="PickupType")
	public String getPickupType() {
		return PickupType;
	}
	public void setPickupType(String pickupType) {
		PickupType = pickupType;
	}
	@XmlElement(name="PickupAddress")
	public String getPickupAddress() {
		return PickupAddress;
	}
	public void setPickupAddress(String pickupAddress) {
		PickupAddress = pickupAddress;
	}
	@XmlElement(name="DeliverName")
	public String getDeliverName() {
		return DeliverName;
	}
	public void setDeliverName(String deliverName) {
		DeliverName = deliverName;
	}
	@XmlElement(name="PickupDate")
	public String getPickupDate() {
		return PickupDate;
	}
	public void setPickupDate(String pickupDate) {
		PickupDate = pickupDate;
	}
	@XmlElement(name="PickupRemark")
	public String getPickupRemark() {
		return PickupRemark;
	}
	public void setPickupRemark(String pickupRemark) {
		PickupRemark = pickupRemark;
	}
}
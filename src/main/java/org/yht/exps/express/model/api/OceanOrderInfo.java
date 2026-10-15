package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="OrderInfo", propOrder = {"HBLNo", "operation","ordId", "ordDt", "customerCode", "agentCode", "emsYn",
		"MBLNo", "BLTypeIE", "lineCode", "vesselName", "vesselCountryCode",
		"voyage", "callSign", "ETD", "ETA", "depPort",
		"arrPort","emsOfficeCd","emsOfficeSeq","emsLinkDivCd","emsCustNo","emsApprNo","emsKpacketNo","emsMailKindCd","emsMailTypeCd","emsDetailDivCd",
		"emsContractClass","emsCustomsDeclarRemark","emsWidth","emsLength","emsHeight","emsVolumnExcludedYN",
		"insuranceYN","insuranceValue"
		,"shipperName", "shipperAddress","shipperAddressA","shipperAddressB"
		, "shipperState", "shipperPlace",
		"shipperStreet", "shipperTel","shipperTelA","shipperTelB","shipperTelC","shipperTelD"
		, "shipperMobile","shipperMobileA","shipperMobileB","shipperMobileC"
		, "shipperZipcode", "shipperCountryCode",
		"shipperUrl", "shipperBusinessId", "shipperEmail"
		, "shipperNameLocal", "shipperAddressLocal",
		"consigneeName", "consigneeAddress", "consigneeState", "consigneePlace", "consigneeStreet"
		, "consigneeTel","consigneeTelA","consigneeTelB","consigneeTelC","consigneeTelD"
		, "consigneeMobile", "consigneeZipcode",
		"consigneeBusinessId", "consigneeCountryCode",
		"consigneeIdCardType"
		, "consigneeIdCardNo", "consigneeEmail"
		, "consigneeNameLocal", "consigneeAddressLocal",
		"consigneeRegionLocal", "consigneeCityLocal", "consigneeStateLocal", "consigneeZipcodeLocal"
		, "consigneeTelLocal","destinationCountryCode"
		,"specialShipperCode", "CType", "ETradeType","ETradeBuyType","sellerRegNo","sellerName","agentRegNo",
		"agentName","salesBrokerRegNo","salesBrokerName","customsType", "url",
		"commodityItem", "commodityItemLocal", "permitItemCode", "hsCode", "totalQty", "totalWeight",
		"CBM", "currencyCode", "amountTotal", "deliveryNo", "dimWidth",
		"dimLength", "dimHeight", "remark", "deliveryRequest", "brokerRequest", "brokerPayment", "brokerAmount", "deliveryCenterCode", "deliveryCode", "frtDvCd", "bagNumber", "deliveryPayment"
		,"sellerNo", "ftaYn", "documentYn", "carryoutCode", "consigneeFirstName", "consigneeLastName"
})

public class OceanOrderInfo{
	private String Operation;
	private String OrdId;
	private String OrdDt;
	private String CustomerCode;
	private String AgentCode;
	private String HBLNo;
	private String MBLNo;
	private String BLTypeIE;
	private String LineCode;
	private String VesselName;
	private String VesselCountryCode;
	private String Voyage;
	private String CallSign;
	private String ETD;
	private String ETA;
	private String DepPort;
	private String ArrPort;
	private String ShipperName;
	private String ShipperAddress;
	private String ShipperState;
	private String ShipperPlace;
	private String ShipperStreet;
	private String ShipperTel;
	private String ShipperMobile;
	private String ShipperZipcode;
	private String ShipperCountryCode;
	private String ShipperUrl;
	private String ShipperBusinessId;
	private String ShipperEmail;
	private String ShipperNameLocal;
	private String ShipperAddressLocal;
	private String ConsigneeName;
	private String ConsigneeAddress;
	private String ConsigneeTel;
	private String ConsigneeMobile;
	private String ConsigneeZipcode;
	private String ConsigneeBusinessId;
	private String ConsigneeState;
	private String ConsigneePlace;
	private String ConsigneeStreet;
	private String ConsigneeCountryCode;
	private String ConsigneeIdCardType;
	private String ConsigneeIdCardNo;
	private String ConsigneeEmail;
	private String ConsigneeNameLocal;
	private String ConsigneeAddressLocal;
	private String ConsigneeRegionLocal;
	private String ConsigneeCityLocal;
	private String ConsigneeStateLocal;
	private String ConsigneeZipcodeLocal;
	private String ConsigneeTelLocal;
	private String SpecialShipperCode;
	private String CType;
	private String ETradeType;
	private String ETradeBuyType;
	private String SellerRegNo;
	private String SellerName;
	private String AgentRegNo;
	private String AgentName;
	private String SalesBrokerRegNo;
	private String SalesBrokerName;
	private String CustomsType;
	private String Url;
	private String CommodityItem;
	private String CommodityItemLocal;
	private String PermitItemCode;
	private String HSCode;
	private String TotalQty;
	private String TotalWeight;
	private String CBM;
	private String CurrencyCode;
	private String AmountTotal;
	private String DeliveryNo;
	private String DimWidth;
	private String DimLength;
	private String DimHeight;
	private String Remark;
	private String DeliveryRequest;
	private String BrokerRequest;
	private String BrokerPayment;
	private String BrokerAmount;
	private String DeliveryCenterCode;
	private String BagNumber;
	private String DeliveryPayment;
	private String DeliveryCode;
	private String FrtDvCd;
	
	private String EmsYn;
	private String EmsOfficeCd;
	private String EmsOfficeSeq;
	private String EmsLinkDivCd;
	private String EmsCustNo;
	private String EmsApprNo;
	private String EmsKpacketNo;
	private String EmsMailKindCd;
	private String EmsMailTypeCd;
	private String EmsDetailDivCd;
	private String EmsContractClass;
	private String EmsCustomsDeclarRemark;
	private String EmsWidth;
	private String EmsLength;
	private String EmsHeight;
	private String EmsVolumnExcludedYN;
	private String InsuranceYN;
	private String InsuranceValue;
	private String ShipperAddressA;
	private String ShipperAddressB;
	private String ShipperTelA;
	private String ShipperTelB;
	private String ShipperTelC;
	private String ShipperTelD;
	private String ShipperMobileA;
	private String ShipperMobileB;
	private String ShipperMobileC;
	private String ConsigneeTelA;
	private String ConsigneeTelB;
	private String ConsigneeTelC;
	private String ConsigneeTelD;
	private String DestinationCountryCode;
	
	private String SellerNo;
	private String FtaYn;
	private String DocumentYn;
	private String CarryoutCode;
	private String ConsigneeFirstName;
	private String ConsigneeLastName;
	
	@XmlElement(name="Operation")
	public String getOperation() {
		return Operation;
	}
	public void setOperation(String operation) {
		Operation = operation;
	}
	@XmlElement(name="OrdId")
	public String getOrdId() {
		return OrdId;
	}
	public void setOrdId(String ordId) {
		OrdId = ordId;
	}
	@XmlElement(name="OrdDt")
	public String getOrdDt() {
		return OrdDt;
	}
	public void setOrdDt(String ordDt) {
		OrdDt = ordDt;
	}
	@XmlElement(name="CustomerCode")
	public String getCustomerCode() {
		return CustomerCode;
	}
	public void setCustomerCode(String customerCode) {
		CustomerCode = customerCode;
	}
	@XmlElement(name="AgentCode")
	public String getAgentCode() {
		return AgentCode;
	}
	public void setAgentCode(String agentCode) {
		AgentCode = agentCode;
	}
	@XmlElement(name="HBLNo")
	public String getHBLNo() {
		return HBLNo;
	}
	public void setHBLNo(String hBLNo) {
		HBLNo = hBLNo;
	}
	@XmlElement(name="MBLNo")
	public String getMBLNo() {
		return MBLNo;
	}
	public void setMBLNo(String mBLNo) {
		MBLNo = mBLNo;
	}
	@XmlElement(name="BLTypeIE")
	public String getBLTypeIE() {
		return BLTypeIE;
	}
	public void setBLTypeIE(String bLTypeIE) {
		BLTypeIE = bLTypeIE;
	}
	@XmlElement(name="LineCode")
	public String getLineCode() {
		return LineCode;
	}
	public void setLineCode(String lineCode) {
		LineCode = lineCode;
	}
	@XmlElement(name="VesselName")
	public String getVesselName() {
		return VesselName;
	}
	public void setVesselName(String vesselName) {
		VesselName = vesselName;
	}
	@XmlElement(name="VesselCountryCode")
	public String getVesselCountryCode() {
		return VesselCountryCode;
	}
	public void setVesselCountryCode(String vesselCountryCode) {
		VesselCountryCode = vesselCountryCode;
	}
	@XmlElement(name="Voyage")
	public String getVoyage() {
		return Voyage;
	}
	public void setVoyage(String voyage) {
		Voyage = voyage;
	}
	@XmlElement(name="CallSign")
	public String getCallSign() {
		return CallSign;
	}
	public void setCallSign(String callSign) {
		CallSign = callSign;
	}
	@XmlElement(name="ETD")
	public String getETD() {
		return ETD;
	}
	public void setETD(String eTD) {
		ETD = eTD;
	}
	@XmlElement(name="ETA")
	public String getETA() {
		return ETA;
	}
	public void setETA(String eTA) {
		ETA = eTA;
	}
	@XmlElement(name="DepPort")
	public String getDepPort() {
		return DepPort;
	}
	public void setDepPort(String depPort) {
		DepPort = depPort;
	}
	@XmlElement(name="ArrPort")
	public String getArrPort() {
		return ArrPort;
	}
	public void setArrPort(String arrPort) {
		ArrPort = arrPort;
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
	@XmlElement(name="ShipperState")
	public String getShipperState() {
		return ShipperState;
	}
	public void setShipperState(String shipperState) {
		ShipperState = shipperState;
	}
	@XmlElement(name="ShipperPlace")
	public String getShipperPlace() {
		return ShipperPlace;
	}
	public void setShipperPlace(String shipperPlace) {
		ShipperPlace = shipperPlace;
	}
	@XmlElement(name="ShipperStreet")
	public String getShipperStreet() {
		return ShipperStreet;
	}
	public void setShipperStreet(String shipperStreet) {
		ShipperStreet = shipperStreet;
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
	@XmlElement(name="ShipperZipcode")
	public String getShipperZipcode() {
		return ShipperZipcode;
	}
	public void setShipperZipcode(String shipperZipcode) {
		ShipperZipcode = shipperZipcode;
	}
	@XmlElement(name="ShipperCountryCode")
	public String getShipperCountryCode() {
		return ShipperCountryCode;
	}
	public void setShipperCountryCode(String shipperCountryCode) {
		ShipperCountryCode = shipperCountryCode;
	}
	@XmlElement(name="ShipperUrl")
	public String getShipperUrl() {
		return ShipperUrl;
	}
	public void setShipperUrl(String shipperUrl) {
		ShipperUrl = shipperUrl;
	}
	@XmlElement(name="ShipperBusinessId")
	public String getShipperBusinessId() {
		return ShipperBusinessId;
	}
	public void setShipperBusinessId(String shipperBusinessId) {
		ShipperBusinessId = shipperBusinessId;
	}
	@XmlElement(name="ShipperEmail")
	public String getShipperEmail() {
		return ShipperEmail;
	}
	public void setShipperEmail(String shipperEmail) {
		ShipperEmail = shipperEmail;
	}
	@XmlElement(name="ShipperNameLocal")
	public String getShipperNameLocal() {
		return ShipperNameLocal;
	}
	public void setShipperNameLocal(String shipperNameLocal) {
		ShipperNameLocal = shipperNameLocal;
	}
	@XmlElement(name="ShipperAddressLocal")
	public String getShipperAddressLocal() {
		return ShipperAddressLocal;
	}
	public void setShipperAddressLocal(String shipperAddressLocal) {
		ShipperAddressLocal = shipperAddressLocal;
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
	@XmlElement(name="ConsigneeZipcode")
	public String getConsigneeZipcode() {
		return ConsigneeZipcode;
	}
	public void setConsigneeZipcode(String consigneeZipcode) {
		ConsigneeZipcode = consigneeZipcode;
	}
	@XmlElement(name="ConsigneeBusinessId")
	public String getConsigneeBusinessId() {
		return ConsigneeBusinessId;
	}
	public void setConsigneeBusinessId(String consigneeBusinessId) {
		ConsigneeBusinessId = consigneeBusinessId;
	}
	@XmlElement(name="ConsigneeState")
	public String getConsigneeState() {
		return ConsigneeState;
	}
	public void setConsigneeState(String consigneeState) {
		ConsigneeState = consigneeState;
	}
	@XmlElement(name="ConsigneePlace")
	public String getConsigneePlace() {
		return ConsigneePlace;
	}
	public void setConsigneePlace(String consigneePlace) {
		ConsigneePlace = consigneePlace;
	}
	@XmlElement(name="ConsigneeStreet")
	public String getConsigneeStreet() {
		return ConsigneeStreet;
	}
	public void setConsigneeStreet(String consigneeStreet) {
		ConsigneeStreet = consigneeStreet;
	}
	@XmlElement(name="ConsigneeCountryCode")
	public String getConsigneeCountryCode() {
		return ConsigneeCountryCode;
	}
	public void setConsigneeCountryCode(String consigneeCountryCode) {
		ConsigneeCountryCode = consigneeCountryCode;
	}
	@XmlElement(name="ConsigneeIdCardType")
	public String getConsigneeIdCardType() {
		return ConsigneeIdCardType;
	}
	public void setConsigneeIdCardType(String consigneeIdCardType) {
		ConsigneeIdCardType = consigneeIdCardType;
	}
	@XmlElement(name="ConsigneeIdCardNo")
	public String getConsigneeIdCardNo() {
		return ConsigneeIdCardNo;
	}
	public void setConsigneeIdCardNo(String consigneeIdCardNo) {
		ConsigneeIdCardNo = consigneeIdCardNo;
	}
	@XmlElement(name="ConsigneeEmail")
	public String getConsigneeEmail() {
		return ConsigneeEmail;
	}
	public void setConsigneeEmail(String consigneeEmail) {
		ConsigneeEmail = consigneeEmail;
	}
	@XmlElement(name="ConsigneeNameLocal")
	public String getConsigneeNameLocal() {
		return ConsigneeNameLocal;
	}
	public void setConsigneeNameLocal(String consigneeNameLocal) {
		ConsigneeNameLocal = consigneeNameLocal;
	}
	@XmlElement(name="ConsigneeAddressLocal")
	public String getConsigneeAddressLocal() {
		return ConsigneeAddressLocal;
	}
	public void setConsigneeAddressLocal(String consigneeAddressLocal) {
		ConsigneeAddressLocal = consigneeAddressLocal;
	}
	@XmlElement(name="ConsigneeRegionLocal")
	public String getConsigneeRegionLocal() {
		return ConsigneeRegionLocal;
	}
	public void setConsigneeRegionLocal(String consigneeRegionLocal) {
		ConsigneeRegionLocal = consigneeRegionLocal;
	}
	@XmlElement(name="ConsigneeCityLocal")
	public String getConsigneeCityLocal() {
		return ConsigneeCityLocal;
	}
	public void setConsigneeCityLocal(String consigneeCityLocal) {
		ConsigneeCityLocal = consigneeCityLocal;
	}
	@XmlElement(name="ConsigneeStateLocal")
	public String getConsigneeStateLocal() {
		return ConsigneeStateLocal;
	}
	public void setConsigneeStateLocal(String consigneeStateLocal) {
		ConsigneeStateLocal = consigneeStateLocal;
	}
	@XmlElement(name="ConsigneeZipcodeLocal")
	public String getConsigneeZipcodeLocal() {
		return ConsigneeZipcodeLocal;
	}
	public void setConsigneeZipcodeLocal(String consigneeZipcodeLocal) {
		ConsigneeZipcodeLocal = consigneeZipcodeLocal;
	}
	@XmlElement(name="ConsigneeTelLocal")
	public String getConsigneeTelLocal() {
		return ConsigneeTelLocal;
	}
	public void setConsigneeTelLocal(String consigneeTelLocal) {
		ConsigneeTelLocal = consigneeTelLocal;
	}
	@XmlElement(name="SpecialShipperCode")
	public String getSpecialShipperCode() {
		return SpecialShipperCode;
	}
	public void setSpecialShipperCode(String specialShipperCode) {
		SpecialShipperCode = specialShipperCode;
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
	@XmlElement(name="ETradeBuyType")
	public String getETradeBuyType() {
		return ETradeBuyType;
	}
	public void setETradeBuyType(String eTradeBuyType) {
		ETradeBuyType = eTradeBuyType;
	}
	@XmlElement(name="SellerRegNo")
	public String getSellerRegNo() {
		return SellerRegNo;
	}
	public void setSellerRegNo(String sellerRegNo) {
		SellerRegNo = sellerRegNo;
	}
	@XmlElement(name="SellerName")
	public String getSellerName() {
		return SellerName;
	}
	public void setSellerName(String sellerName) {
		SellerName = sellerName;
	}
	@XmlElement(name="AgentRegNo")
	public String getAgentRegNo() {
		return AgentRegNo;
	}
	public void setAgentRegNo(String agentRegNo) {
		AgentRegNo = agentRegNo;
	}
	@XmlElement(name="AgentName")
	public String getAgentName() {
		return AgentName;
	}
	public void setAgentName(String agentName) {
		AgentName = agentName;
	}
	@XmlElement(name="SalesBrokerRegNo")
	public String getSalesBrokerRegNo() {
		return SalesBrokerRegNo;
	}
	public void setSalesBrokerRegNo(String salesBrokerRegNo) {
		SalesBrokerRegNo = salesBrokerRegNo;
	}
	@XmlElement(name="SalesBrokerName")
	public String getSalesBrokerName() {
		return SalesBrokerName;
	}
	public void setSalesBrokerName(String salesBrokerName) {
		SalesBrokerName = salesBrokerName;
	}
	@XmlElement(name="SellerNo")
	public String getSellerNo() {
		return SellerNo;
	}
	public void setSellerNo(String sellerNo) {
		SellerNo = sellerNo;
	}
	@XmlElement(name="CustomsType")
	public String getCustomsType() {
		return CustomsType;
	}
	public void setCustomsType(String customsType) {
		CustomsType = customsType;
	}
	@XmlElement(name="Url")
	public String getUrl() {
		return Url;
	}
	public void setUrl(String url) {
		Url = url;
	}
	@XmlElement(name="CommodityItem")
	public String getCommodityItem() {
		return CommodityItem;
	}
	public void setCommodityItem(String commodityItem) {
		CommodityItem = commodityItem;
	}
	@XmlElement(name="CommodityItemLocal")
	public String getCommodityItemLocal() {
		return CommodityItemLocal;
	}
	public void setCommodityItemLocal(String commodityItemLocal) {
		CommodityItemLocal = commodityItemLocal;
	}
	@XmlElement(name="PermitItemCode")
	public String getPermitItemCode() {
		return PermitItemCode;
	}
	public void setPermitItemCode(String permitItemCode) {
		PermitItemCode = permitItemCode;
	}
	@XmlElement(name="HSCode")
	public String getHsCode() {
		return HSCode;
	}
	public void setHsCode(String hsCode) {
		HSCode = hsCode;
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
	@XmlElement(name="CBM")
	public String getCBM() {
		return CBM;
	}
	public void setCBM(String cbm) {
		CBM = cbm;
	}
	@XmlElement(name="CurrencyCode")
	public String getCurrencyCode() {
		return CurrencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		CurrencyCode = currencyCode;
	}
	@XmlElement(name="AmountTotal")
	public String getAmountTotal() {
		return AmountTotal;
	}
	public void setAmountTotal(String amountTotal) {
		AmountTotal = amountTotal;
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
	@XmlElement(name="Remark")
	public String getRemark() {
		return Remark;
	}
	public void setRemark(String remark) {
		Remark = remark;
	}
	@XmlElement(name="DeliveryRequest")
	public String getDeliveryRequest() {
		return DeliveryRequest;
	}
	public void setDeliveryRequest(String deliveryRequest) {
		DeliveryRequest = deliveryRequest;
	}
	@XmlElement(name="BrokerRequest")
	public String getBrokerRequest() {
		return BrokerRequest;
	}
	public void setBrokerRequest(String brokerRequest) {
		BrokerRequest = brokerRequest;
	}
	@XmlElement(name="BrokerPayment")
	public String getBrokerPayment() {
		return BrokerPayment;
	}
	public void setBrokerPayment(String brokerPayment) {
		BrokerPayment = brokerPayment;
	}
	@XmlElement(name="BrokerAmount")
	public String getBrokerAmount() {
		return BrokerAmount;
	}
	public void setBrokerAmount(String brokerAmount) {
		BrokerAmount = brokerAmount;
	}
	@XmlElement(name="DeliveryCenterCode")
	public String getDeliveryCenterCode() {
		return DeliveryCenterCode;
	}
	public void setDeliveryCenterCode(String deliveryCenterCode) {
		DeliveryCenterCode = deliveryCenterCode;
	}
	@XmlElement(name="BagNumber")
	public String getBagNumber() {
		return BagNumber;
	}
	public void setBagNumber(String bagNumber) {
		BagNumber = bagNumber;
	}
	@XmlElement(name="DeliveryPayment")
	public String getDeliveryPayment() {
		return DeliveryPayment;
	}
	public void setDeliveryPayment(String deliveryPayment) {
		DeliveryPayment = deliveryPayment;
	}
	@XmlElement(name="DeliveryCode")
	public String getDeliveryCode() {
		return DeliveryCode;
	}
	public void setDeliveryCode(String deliveryCode) {
		DeliveryCode = deliveryCode;
	}
	@XmlElement(name="FrtDvCd")
	public String getFrtDvCd() {
		return FrtDvCd;
	}
	public void setFrtDvCd(String frtDvCd) {
		FrtDvCd = frtDvCd;
	}
	@XmlElement(name="FtaYn")
	public String getFtaYn() {
		return FtaYn;
	}
	public void setFtaYn(String ftaYn) {
		FtaYn = ftaYn;
	}
	@XmlElement(name="DocumentYn")
	public String getDocumentYn() {
		return DocumentYn;
	}
	public void setDocumentYn(String documentYn) {
		DocumentYn = documentYn;
	}
	
	
	//EmsYn
	@XmlElement(name="EmsYn")
	public String getEmsYn() {
		return EmsYn;
	}
	public void setEmsYn(String emsYn) {
		EmsYn = emsYn;
	}
	@XmlElement(name="EmsOfficeCd")
	public String getEmsOfficeCd() {
		return EmsOfficeCd;
	}
	public void setEmsOfficeCd(String emsOfficeCd) {
		EmsOfficeCd = emsOfficeCd;
	}
	@XmlElement(name="EmsOfficeSeq")
	public String getEmsOfficeSeq() {
		return EmsOfficeSeq;
	}
	public void setEmsOfficeSeq(String emsOfficeSeq) {
		EmsOfficeSeq = emsOfficeSeq;
	}
	@XmlElement(name="EmsLinkDivCd")
	public String getEmsLinkDivCd() {
		return EmsLinkDivCd;
	}
	public void setEmsLinkDivCd(String emsLinkDivCd) {
		EmsLinkDivCd = emsLinkDivCd;
	}
	@XmlElement(name="EmsCustNo")
	public String getEmsCustNo() {
		return EmsCustNo;
	}
	public void setEmsCustNo(String emsCustNo) {
		EmsCustNo = emsCustNo;
	}
	@XmlElement(name="EmsApprNo")
	public String getEmsApprNo() {
		return EmsApprNo;
	}
	public void setEmsApprNo(String emsApprNo) {
		EmsApprNo = emsApprNo;
	}
	@XmlElement(name="EmsKpacketNo")
	public String getEmsKpacketNo() {
		return EmsKpacketNo;
	}
	public void setEmsKpacketNo(String emsKpacketNo) {
		EmsKpacketNo = emsKpacketNo;
	}
	@XmlElement(name="EmsMailKindCd")
	public String getEmsMailKindCd() {
		return EmsMailKindCd;
	}
	public void setEmsMailKindCd(String emsMailKindCd) {
		EmsMailKindCd = emsMailKindCd;
	}
	@XmlElement(name="EmsMailTypeCd")
	public String getEmsMailTypeCd() {
		return EmsMailTypeCd;
	}
	public void setEmsMailTypeCd(String emsMailTypeCd) {
		EmsMailTypeCd = emsMailTypeCd;
	}
	@XmlElement(name="EmsDetailDivCd")
	public String getEmsDetailDivCd() {
		return EmsDetailDivCd;
	}
	public void setEmsDetailDivCd(String emsDetailDivCd) {
		EmsDetailDivCd = emsDetailDivCd;
	}
	@XmlElement(name="EmsContractClass")
	public String getEmsContractClass() {
		return EmsContractClass;
	}
	public void setEmsContractClass(String emsContractClass) {
		EmsContractClass = emsContractClass;
	}
	@XmlElement(name="EmsCustomsDeclarRemark")
	public String getEmsCustomsDeclarRemark() {
		return EmsCustomsDeclarRemark;
	}
	public void setEmsCustomsDeclarRemark(String emsCustomsDeclarRemark) {
		EmsCustomsDeclarRemark = emsCustomsDeclarRemark;
	}
	@XmlElement(name="EmsWidth")
	public String getEmsWidth() {
		return EmsWidth;
	}
	public void setEmsWidth(String emsWidth) {
		EmsWidth = emsWidth;
	}
	@XmlElement(name="EmsLength")
	public String getEmsLength() {
		return EmsLength;
	}
	public void setEmsLength(String emsLength) {
		EmsLength = emsLength;
	}
	@XmlElement(name="EmsHeight")
	public String getEmsHeight() {
		return EmsHeight;
	}
	public void setEmsHeight(String emsHeight) {
		EmsHeight = emsHeight;
	}
	@XmlElement(name="EmsVolumnExcludedYN")
	public String getEmsVolumnExcludedYN() {
		return EmsVolumnExcludedYN;
	}
	public void setEmsVolumnExcludedYN(String emsVolumnExcludedYN) {
		EmsVolumnExcludedYN = emsVolumnExcludedYN;
	}
	@XmlElement(name="InsuranceYN")
	public String getInsuranceYN() {
		return InsuranceYN;
	}
	public void setInsuranceYN(String insuranceYN) {
		InsuranceYN = insuranceYN;
	}
	@XmlElement(name="InsuranceValue")
	public String getInsuranceValue() {
		return InsuranceValue;
	}
	public void setInsuranceValue(String insuranceValue) {
		InsuranceValue = insuranceValue;
	}
	@XmlElement(name="ShipperAddressA")
	public String getShipperAddressA() {
		return ShipperAddressA;
	}
	public void setShipperAddressA(String shipperAddressA) {
		ShipperAddressA = shipperAddressA;
	}
	@XmlElement(name="ShipperAddressB")
	public String getShipperAddressB() {
		return ShipperAddressB;
	}
	public void setShipperAddressB(String shipperAddressB) {
		ShipperAddressB = shipperAddressB;
	}
	@XmlElement(name="ShipperTelA")
	public String getShipperTelA() {
		return ShipperTelA;
	}
	public void setShipperTelA(String shipperTelA) {
		ShipperTelA = shipperTelA;
	}
	@XmlElement(name="ShipperTelB")
	public String getShipperTelB() {
		return ShipperTelB;
	}
	public void setShipperTelB(String shipperTelB) {
		ShipperTelB = shipperTelB;
	}
	@XmlElement(name="ShipperTelC")
	public String getShipperTelC() {
		return ShipperTelC;
	}
	public void setShipperTelC(String shipperTelC) {
		ShipperTelC = shipperTelC;
	}
	@XmlElement(name="ShipperTelD")
	public String getShipperTelD() {
		return ShipperTelD;
	}
	public void setShipperTelD(String shipperTelD) {
		ShipperTelD = shipperTelD;
	}
	@XmlElement(name="ShipperMobileA")
	public String getShipperMobileA() {
		return ShipperMobileA;
	}
	public void setShipperMobileA(String shipperMobileA) {
		ShipperMobileA = shipperMobileA;
	}
	@XmlElement(name="ShipperMobileB")
	public String getShipperMobileB() {
		return ShipperMobileB;
	}
	public void setShipperMobileB(String shipperMobileB) {
		ShipperMobileB = shipperMobileB;
	}
	@XmlElement(name="ShipperMobileC")
	public String getShipperMobileC() {
		return ShipperMobileC;
	}
	public void setShipperMobileC(String shipperMobileC) {
		ShipperMobileC = shipperMobileC;
	}
	@XmlElement(name="ConsigneeTelA")
	public String getConsigneeTelA() {
		return ConsigneeTelA;
	}
	public void setConsigneeTelA(String consigneeTelA) {
		ConsigneeTelA = consigneeTelA;
	}
	@XmlElement(name="ConsigneeTelB")
	public String getConsigneeTelB() {
		return ConsigneeTelB;
	}
	public void setConsigneeTelB(String consigneeTelB) {
		ConsigneeTelB = consigneeTelB;
	}
	@XmlElement(name="ConsigneeTelC")
	public String getConsigneeTelC() {
		return ConsigneeTelC;
	}
	public void setConsigneeTelC(String consigneeTelC) {
		ConsigneeTelC = consigneeTelC;
	}
	@XmlElement(name="ConsigneeTelD")
	public String getConsigneeTelD() {
		return ConsigneeTelD;
	}
	public void setConsigneeTelD(String consigneeTelD) {
		ConsigneeTelD = consigneeTelD;
	}
	@XmlElement(name="DestinationCountryCode")
	public String getDestinationCountryCode() {
		return DestinationCountryCode;
	}
	public void setDestinationCountryCode(String destinationCountryCode) {
		DestinationCountryCode = destinationCountryCode;
	}
	@XmlElement(name="CarryoutCode")
	public String getCarryoutCode() {
		return CarryoutCode;
	}
	public void setCarryoutCode(String carryoutCode) {
		CarryoutCode = carryoutCode;
	}
	@XmlElement(name="ConsigneeFirstName")
	public String getConsigneeFirstName() {
		return ConsigneeFirstName;
	}
	public void setConsigneeFirstName(String consigneeFirstName) {
		ConsigneeFirstName = consigneeFirstName;
	}
	@XmlElement(name="ConsigneeLastName")
	public String getConsigneeLastName() {
		return ConsigneeLastName;
	}
	public void setConsigneeLastName(String consigneeLastName) {
		ConsigneeLastName = consigneeLastName;
	}
}
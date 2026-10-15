package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OrderInfoJson {
    @JsonAlias({"Operation", "operation"})
    private String operation;
    @JsonAlias({"OrdId", "ordId"})
    private String ordId;
    @JsonAlias({"OrdDt", "ordDt"})
    private String ordDt;
    @JsonAlias({"CustomerCode", "customerCode"})
    private String customerCode;
    @JsonAlias({"AgentCode", "agentCode"})
    private String agentCode;
    @JsonAlias({"EmsYn", "emsYn"})
    private String emsYn;
    @JsonAlias({"HblNo", "hblNo"})
    private String hblNo;
    @JsonAlias({"MblNo", "mblNo"})
    private String mblNo;
    @JsonProperty("BLTypeIE")
    @JsonAlias({"BLTypeIE", "blTypeIE"})
    private String blTypeIE;
    @JsonAlias({"LineCode", "lineCode"})
    private String lineCode;
    @JsonAlias({"VesselName", "vesselName"})
    private String vesselName;
    @JsonAlias({"VesselCountryCode", "vesselCountryCode"})
    private String vesselCountryCode;
    @JsonAlias({"Voyage", "voyage"})
    private String voyage;
    @JsonAlias({"CallSign", "callSign"})
    private String callSign;
    @JsonAlias({"FlightNo", "flightNo"})
    private String flightNo;
    @JsonProperty("ETD")
    @JsonAlias({"ETD", "etd"})
    private String etd;
    @JsonProperty("ETA")
    @JsonAlias({"ETA", "eta"})
    private String eta;
    @JsonAlias({"DepPort", "depPort"})
    private String depPort;
    @JsonAlias({"ArrPort", "arrPort"})
    private String arrPort;
    @JsonAlias({"ShipperName", "shipperName"})
    private String shipperName;
    @JsonAlias({"ShipperAddress", "shipperAddress"})
    private String shipperAddress;
    @JsonAlias({"ShipperState", "shipperState"})
    private String shipperState;
    @JsonAlias({"ShipperPlace", "shipperPlace"})
    private String shipperPlace;
    @JsonAlias({"ShipperStreet", "shipperStreet"})
    private String shipperStreet;
    @JsonAlias({"ShipperTel", "shipperTel"})
    private String shipperTel;
    @JsonAlias({"ShipperMobile", "shipperMobile"})
    private String shipperMobile;
    @JsonAlias({"ShipperZipcode", "shipperZipcode"})
    private String shipperZipcode;
    @JsonAlias({"ShipperCountryCode", "shipperCountryCode"})
    private String shipperCountryCode;
    @JsonAlias({"ShipperUrl", "shipperUrl"})
    private String shipperUrl;
    @JsonAlias({"ShipperBusinessId", "shipperBusinessId"})
    private String shipperBusinessId;
    @JsonAlias({"ShipperEmail", "shipperEmail"})
    private String shipperEmail;
    @JsonAlias({"ShipperNameLocal", "shipperNameLocal"})
    private String shipperNameLocal;
    @JsonAlias({"ShipperAddressLocal", "shipperAddressLocal"})
    private String shipperAddressLocal;
    @JsonAlias({"ConsigneeName", "consigneeName"})
    private String consigneeName;
    @JsonAlias({"ConsigneeAddress", "consigneeAddress"})
    private String consigneeAddress;
    @JsonAlias({"ConsigneeState", "consigneeState"})
    private String consigneeState;
    @JsonAlias({"ConsigneePlace", "consigneePlace"})
    private String consigneePlace;
    @JsonAlias({"ConsigneeStreet", "consigneeStreet"})
    private String consigneeStreet;
    @JsonAlias({"ConsigneeTel", "consigneeTel"})
    private String consigneeTel;
    @JsonAlias({"ConsigneeMobile", "consigneeMobile"})
    private String consigneeMobile;
    @JsonAlias({"ConsigneeZipcode", "consigneeZipcode"})
    private String consigneeZipcode;
    @JsonAlias({"ConsigneeCountryCode", "consigneeCountryCode"})
    private String consigneeCountryCode;
    @JsonAlias({"ConsigneeCountryName", "consigneeCountryName"})
    private String consigneeCountryName;
    @JsonAlias({"ConsigneeBusinessId", "consigneeBusinessId"})
    private String consigneeBusinessId;
    @JsonAlias({"ConsigneeIdCardType", "consigneeIdCardType"})
    private String consigneeIdCardType;
    @JsonAlias({"ConsigneeIdCardNo", "consigneeIdCardNo"})
    private String consigneeIdCardNo;
    @JsonAlias({"ConsigneeEmail", "consigneeEmail"})
    private String consigneeEmail;
    @JsonAlias({"ConsigneeNameLocal", "consigneeNameLocal"})
    private String consigneeNameLocal;
    @JsonAlias({"ConsigneeAddressLocal", "consigneeAddressLocal"})
    private String consigneeAddressLocal;
    @JsonAlias({"ConsigneeRegionLocal", "consigneeRegionLocal"})
    private String consigneeRegionLocal;
    @JsonAlias({"ConsigneeCityLocal", "consigneeCityLocal"})
    private String consigneeCityLocal;
    @JsonAlias({"ConsigneeStateLocal", "consigneeStateLocal"})
    private String consigneeStateLocal;
    @JsonAlias({"ConsigneeZipcodeLocal", "consigneeZipcodeLocal"})
    private String consigneeZipcodeLocal;
    @JsonAlias({"ConsigneeTelLocal", "consigneeTelLocal"})
    private String consigneeTelLocal;
    @JsonAlias({"DestinationCountryCode", "destinationCountryCode"})
    private String destinationCountryCode;
    @JsonAlias({"SpecialShipperCode", "specialShipperCode"})
    private String specialShipperCode;
    @JsonProperty("CType")
    @JsonAlias({"CType", "cType", "ctype"})
    private String cType;
    @JsonProperty("ETradeType")
    @JsonAlias({"ETradeType", "eTradeType", "etradeType"})
    private String eTradeType;
    @JsonProperty("ETradeBuyType")
    @JsonAlias({"ETradeBuyType", "eTradeBuyType", "etradeBuyType"})
    private String eTradeBuyType;
    @JsonAlias({"SellerRegNo", "sellerRegNo"})
    private String sellerRegNo;
    @JsonAlias({"SellerName", "sellerName"})
    private String sellerName;
    @JsonAlias({"AgentRegNo", "agentRegNo"})
    private String agentRegNo;
    @JsonAlias({"AgentName", "agentName"})
    private String agentName;
    @JsonAlias({"SalesBrokerRegNo", "salesBrokerRegNo"})
    private String salesBrokerRegNo;
    @JsonAlias({"SalesBrokerName", "salesBrokerName"})
    private String salesBrokerName;
    @JsonAlias({"SellerNo", "sellerNo"})
    private String sellerNo;
    @JsonAlias({"CustomsType", "customsType"})
    private String customsType;
    @JsonAlias({"Url", "url"})
    private String url;
    @JsonAlias({"CommodityItem", "commodityItem"})
    private String commodityItem;
    @JsonAlias({"CommodityItemLocal", "commodityItemLocal"})
    private String commodityItemLocal;
    @JsonAlias({"PermitItemCode", "permitItemCode"})
    private String permitItemCode;
    @JsonAlias({"HSCode", "hsCode"})
    private String hsCode;
    @JsonAlias({"TotalQty", "totalQty"})
    private int totalQty;
    @JsonAlias({"TotalWeight", "totalWeight"})
    private String totalWeight;
    @JsonProperty("CBM")
    @JsonAlias({"CBM", "cbm", "Cbm"})
    private String cbm;
    @JsonAlias({"CurrencyCode", "currencyCode"})
    private String currencyCode;
    @JsonAlias({"AmountTotal", "amountTotal"})
    private String amountTotal;
    @JsonAlias({"DeliveryNo", "deliveryNo"})
    private String deliveryNo;
    @JsonAlias({"DimWidth", "dimWidth"})
    private Integer dimWidth;
    @JsonAlias({"DimLength", "dimLength"})
    private Integer dimLength;
    @JsonAlias({"DimHeight", "dimHeight"})
    private Integer dimHeight;
    @JsonAlias({"Remark", "remark"})
    private String remark;
    @JsonAlias({"DeliveryRequest", "deliveryRequest"})
    private String deliveryRequest;
    @JsonAlias({"BrokerRequest", "brokerRequest"})
    private String brokerRequest;
    @JsonAlias({"BrokerPayment", "brokerPayment"})
    private String brokerPayment;
    @JsonAlias({"BrokerAmount", "brokerAmount"})
    private String brokerAmount;
    @JsonAlias({"DeliveryCenterCode", "deliveryCenterCode"})
    private String deliveryCenterCode;
    @JsonAlias({"BagNumber", "bagNumber"})
    private String bagNumber;
    @JsonAlias({"DeliveryPayment", "deliveryPayment"})
    private String deliveryPayment;
    @JsonAlias({"DeliveryCode", "deliveryCode"})
    private String deliveryCode;
    @JsonProperty("FrtDvCd")
    @JsonAlias({"FrtDvCd", "frtDvCd"})
    private String frtDvCd;
    @JsonAlias({"FtaYn", "ftaYn"})
    private String ftaYn;
    @JsonAlias({"DocumentYn", "documentYn"})
    private String documentYn;
    @JsonAlias({"CarryoutCode", "carryoutCode"})
    private String carryoutCode;
    @JsonAlias({"ConsigneeFirstName", "consigneeFirstName"})
    private String consigneeFirstName;
    @JsonAlias({"ConsigneeLastName", "consigneeLastName"})
    private String consigneeLastName;
}
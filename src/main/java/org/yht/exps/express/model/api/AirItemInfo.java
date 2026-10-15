package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ItemInfo", propOrder = {"itemNo", "itemNm","itemNmLocal", "itemModelNm", "itemModelNmLocal", "itemProductId", 
		   "itemQty", "itemQtyUnit", "itemGrossWeight", "itemRate", "itemNetWeight", "itemAmount",
		   "itemCurrencyCode", "itemHsCode", "itemOrigin", "itemUrl", "itemRemark"})

public class AirItemInfo{
	private String ItemNo;
	private String ItemNm;
	private String ItemNmLocal;
	private String ItemModelNm;
	private String ItemModelNmLocal;
	private String ItemProductId;
	private String ItemQty;
	private String ItemQtyUnit;
	private String ItemGrossWeight;
	private String ItemRate;
	private String ItemNetWeight;
	private String ItemAmount;
	private String ItemCurrencyCode;
	private String ItemHsCode;
	private String ItemOrigin;
	private String ItemUrl;
	private String ItemRemark;
	
	@XmlElement(name="ItemNo")
	public String getItemNo() {
		return ItemNo;
	}
	public void setItemNo(String itemNo) {
		ItemNo = itemNo;
	}
	@XmlElement(name="ItemNm")
	public String getItemNm() {
		return ItemNm;
	}
	public void setItemNm(String itemNm) {
		ItemNm = itemNm;
	}
	@XmlElement(name="ItemNmLocal")
	public String getItemNmLocal() {
		return ItemNmLocal;
	}
	public void setItemNmLocal(String itemNmLocal) {
		ItemNmLocal = itemNmLocal;
	}
	@XmlElement(name="ItemModelNm")
	public String getItemModelNm() {
		return ItemModelNm;
	}
	public void setItemModelNm(String itemModelNm) {
		ItemModelNm = itemModelNm;
	}
	@XmlElement(name="ItemModelNmLocal")
	public String getItemModelNmLocal() {
		return ItemModelNmLocal;
	}
	public void setItemModelNmLocal(String itemModelNmLocal) {
		ItemModelNmLocal = itemModelNmLocal;
	}
	@XmlElement(name="ItemProductId")
	public String getItemProductId() {
		return ItemProductId;
	}
	public void setItemProductId(String itemProductId) {
		ItemProductId = itemProductId;
	}
	@XmlElement(name="ItemQty")
	public String getItemQty() {
		return ItemQty;
	}
	public void setItemQty(String itemQty) {
		this.ItemQty = itemQty;
	}
	@XmlElement(name="ItemQtyUnit")
	public String getItemQtyUnit() {
		return ItemQtyUnit;
	}
	public void setItemQtyUnit(String itemQtyUnit) {
		ItemQtyUnit = itemQtyUnit;
	}
	@XmlElement(name="ItemGrossWeight")
	public String getItemGrossWeight() {
		return ItemGrossWeight;
	}
	public void setItemGrossWeight(String itemGrossWeight) {
		ItemGrossWeight = itemGrossWeight;
	}
	@XmlElement(name="ItemRate")
	public String getItemRate() {
		return ItemRate;
	}
	public void setItemRate(String itemRate) {
		ItemRate = itemRate;
	}
	@XmlElement(name="ItemNetWeight")
	public String getItemNetWeight() {
		return ItemNetWeight;
	}
	public void setItemNetWeight(String itemNetWeight) {
		ItemNetWeight = itemNetWeight;
	}
	@XmlElement(name="ItemAmount")
	public String getItemAmount() {
		return ItemAmount;
	}
	public void setItemAmount(String itemAmount) {
		ItemAmount = itemAmount;
	}
	@XmlElement(name="ItemCurrencyCode")
	public String getItemCurrencyCode() {
		return ItemCurrencyCode;
	}
	public void setItemCurrencyCode(String itemCurrencyCode) {
		ItemCurrencyCode = itemCurrencyCode;
	}
	@XmlElement(name="ItemHsCode")
	public String getItemHsCode() {
		return ItemHsCode;
	}
	public void setItemHsCode(String itemHsCode) {
		ItemHsCode = itemHsCode;
	}
	@XmlElement(name="ItemOrigin")
	public String getItemOrigin() {
		return ItemOrigin;
	}
	public void setItemOrigin(String itemOrigin) {
		ItemOrigin = itemOrigin;
	}
	@XmlElement(name="ItemUrl")
	public String getItemUrl() {
		return ItemUrl;
	}
	public void setItemUrl(String itemUrl) {
		ItemUrl = itemUrl;
	}
	@XmlElement(name="ItemRemark")
	public String getItemRemark() {
		return ItemRemark;
	}
	public void setItemRemark(String itemRemark) {
		ItemRemark = itemRemark;
	}
}
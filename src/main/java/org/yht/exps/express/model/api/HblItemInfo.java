package org.yht.exps.express.model.api;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ItemInfo", propOrder = {"itemHsCode", "itemName","itemDetail", "itemQty", "itemQtyUnit", "itemGrossWeight", "itemRate", "itemCurrencyCode", "itemAmount"})

public class HblItemInfo{
	
	private String ItemHsCode;
	private String ItemName;
	private String ItemDetail;
	private String ItemQty;
	private String ItemQtyUnit;
	private String ItemGrossWeight;
	private String ItemRate;
	private String ItemCurrencyCode;
	private String ItemAmount;
	
	@XmlElement(name="ItemName")
	public String getItemName() {
		return ItemName;
	}
	public void setItemName(String itemName) {
		ItemName = itemName;
	}
	@XmlElement(name="ItemDetail")
	public String getItemDetail() {
		return ItemDetail;
	}
	public void setItemDetail(String itemDetail) {
		ItemDetail = itemDetail;
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
}
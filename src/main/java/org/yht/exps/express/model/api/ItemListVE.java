package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class ItemListVE {
	@JsonAlias({"HsCode", "hsCode"})
	private String hsCode;
	@JsonAlias({"Name", "name"})
	private String name;
	@JsonAlias({"Amount", "amount"})
	private double amount;
	@JsonAlias({"Quantity", "quantity"})
	private int quantity;
	@JsonAlias({"UnitPrice", "unitPrice"})
	private double unitPrice;
	@JsonAlias({"SerialNumber", "serialNumber"})
	private String serialNumber;
	@JsonAlias({"Category1", "category1"})
	private String category1;
	@JsonAlias({"Category2", "category2"})
	private String category2;
	@JsonAlias({"ItemProperties", "itemProperties"})
	private ItemPropertiesVE itemProperties;
}

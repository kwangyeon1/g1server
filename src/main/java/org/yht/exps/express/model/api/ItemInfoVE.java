package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class ItemInfoVE {
	@JsonAlias({"Id", "id"})
	private long id;
	@JsonAlias({"Name", "name"})
	private String name;
	@JsonAlias({"Quantity", "quantity"})
	private int quantity;
	@JsonAlias({"Price", "price"})
	private int price;
}

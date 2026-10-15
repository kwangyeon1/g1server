package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class OrderTrackingJson {
	@JsonAlias({"Language", "language"})
	private String language;
	@JsonAlias({"OrdId", "ordId"})
	private String ordId;
	@JsonAlias({"HBLNo", "HblNo", "hblNo"})
	private String hblNo;
}

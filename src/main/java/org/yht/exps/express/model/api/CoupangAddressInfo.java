package org.yht.exps.express.model.api;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class CoupangAddressInfo {
	@JsonAlias({"Name", "name"})
	private String name;
	@JsonAlias({"zipCode", "zipCode"})
	private String zipCode;
	@JsonAlias({"address", "address"})
	private String address;
	@JsonAlias({"addressDetail", "addressDetail"})
	private String addressDetail;
	@JsonAlias({"contactNumber", "contactNumber"})
	private String contactNumber;
	@JsonAlias({"cellPhoneNumber", "cellPhoneNumber"})
	private String cellPhoneNumber;
}

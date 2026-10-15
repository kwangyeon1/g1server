package org.yht.exps.express.model.common;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AddressInfo", description = "주소정보")
@Alias("addressInfo")
public class AddressInfo {
	@ApiModelProperty(value="팩스")
	private String fax;
	@ApiModelProperty(value="담당자")
	private String attn;
	@ApiModelProperty(value="우편번호")
	private String zipcode;
	@ApiModelProperty(value="국가코드")
	private String countryCode;
	@ApiModelProperty(value="국가명")
	private String countryName;
	@ApiModelProperty(value="state코드")
	private String stateCode;
	@ApiModelProperty(value="state명")
	private String stateName;
	@ApiModelProperty(value="street")
	private String street;
	@ApiModelProperty(value="전화번호")
	private String tel;
	@ApiModelProperty(value="place")
	private String place;
}

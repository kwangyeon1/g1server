package org.yht.exps.express.model.systemMng.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchAddressZipCodeList", description = "AddressZipCode 조회")
@Alias("SearchAddressZipCodeList")
public class SearchAddressZipCodeList extends SearchList {
	
	@ApiModelProperty(value="우편번호", example="10338")
	private String srchZipCode;
	@ApiModelProperty(value="시도", example="경기도")
	private String srchSido;
	@ApiModelProperty(value="집중국번호, 집중국명, 배달국번호, 배달국명, 집배팀번호, 집배구번호" ,
			example="CentralStationNo, CentralStation, DeliveryNo, Delivery, DeliveryTeamNo, DeliveryPeopleNo") 
	private String srchType;
	@ApiModelProperty(value="검색어")
	private String srchString;		
	
}
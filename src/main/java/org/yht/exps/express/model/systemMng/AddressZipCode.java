package org.yht.exps.express.model.systemMng;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "AddressZipCode", description = "AddressZipCode")
@Alias("AddressZipCode")
@EqualsAndHashCode(callSuper=false)
public class AddressZipCode extends ResultList {
	
	@ApiModelProperty(value="우편번호")
	private String zipcode;
	@ApiModelProperty(value="시도")
	private String sido;
	@ApiModelProperty(value="시군구")
	private String sigungu;
	@ApiModelProperty(value="집중국번호")
	private String centralStationNo;
	@ApiModelProperty(value="집중국명")
	private String centralStation;
	@ApiModelProperty(value="배달국번호")
	private String deliveryNo;
	@ApiModelProperty(value="배달국명")
	private String delivery;
	@ApiModelProperty(value="집배팀번호")
	private String deliveryTeamNo;
	@ApiModelProperty(value="집배구번호")
	private String deliveryPeopleNo;
	
	@ApiModelProperty(value="읍면")
	private String eupmyeon;
	@ApiModelProperty(value="도로명")
	private String road;
	@ApiModelProperty(value="도로명본번")
	private String buildingMainNo;
	@ApiModelProperty(value="도로명부번")
	private String buildingSubNo;
	@ApiModelProperty(value="건물명")
	private String buildingName;
	@ApiModelProperty(value="법정동명")
	private String courtDong;
	@ApiModelProperty(value="리명")
	private String li;
	@ApiModelProperty(value="행정동명")
	private String strokeDong;
	@ApiModelProperty(value="지번본번")
	private String addrMainNo;
	@ApiModelProperty(value="지번부번")
	private String addrSubNo;
	@ApiModelProperty(value="지하여부")
	private String underYn;
	@ApiModelProperty(value="산여부")
	private String mountYn;
	@ApiModelProperty(value="구분코드")
	private String gubun;
	
	
	@ApiModelProperty(value="등록일자")
	private String registerDateTime;
	@ApiModelProperty(value="수정일자")
	private String updateDateTime;

}
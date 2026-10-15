package org.yht.exps.express.model.eCommerce.houseOrder;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "HouseOrderCommon", description = "전자상거래 HouseOrde 공통 리스트")
@Alias("houseOrderCommon")
@EqualsAndHashCode(callSuper=false)
public class HouseOrderCommon extends ResultList{
	@ApiModelProperty(value="오더관리번호")
	private String odJobNo;
	@ApiModelProperty(value="비엘관리번호")
	private String oblJobNo;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="출발지코드")
	private String origCode;
	@ApiModelProperty(value="출발지명")
	private String origName;
	@ApiModelProperty(value="도착지코드")
	private String destCode;
	@ApiModelProperty(value="도착지명")
	private String destName;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="오더번호")
	private String poNo;
	@ApiModelProperty(value="편(선)명")
	private String vesselFlight;
	@ApiModelProperty(value="송하인명")
	private String shipperName;
	@ApiModelProperty(value="수하인명")
	private String consigneeName;
	@ApiModelProperty(value="수하인명(L)")
	private String consigneeNameLoc;
}


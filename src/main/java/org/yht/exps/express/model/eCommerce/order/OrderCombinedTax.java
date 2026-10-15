package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "OrderCombinedTax", description = "전자상거래 합산과세자료 리스트")
@Alias("orderCombinedTax")
@EqualsAndHashCode(callSuper=false)
public class OrderCombinedTax extends ResultList{
	
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="편/선명")
	private String vesselFlight;
	@ApiModelProperty(value="발송국가")
	private String origCountry;
	@ApiModelProperty(value="발송국가코드")
	private String origCode;
	@ApiModelProperty(value="수화인")
	private String consigneeName;
	@ApiModelProperty(value="수하인전화번호")
	private String consigneeTel;
	@ApiModelProperty(value="수하인휴대전화")
	private String consigneeHp;
	@ApiModelProperty(value="외화금액")
	private double amountCur;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="대표품목")
	private String commodityItem;
	@ApiModelProperty(value="통관_ID")
	private String customsId;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="수하인 로컬이름")
	private String consigneeNameLoc;
	@ApiModelProperty(value="수하인 로컬주소")
	private String consigneeAddressLoc;
	@ApiModelProperty(value="수화인주소")
	private String consigneeAddress;
	
	@ApiModelProperty(value="odJobNo")
	private String odJobNo;
}

package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "OrderDayHouse", description = "전자상거래 Order 일일보고 하우스 리스트")
@Alias("orderDayHouse")
@EqualsAndHashCode(callSuper=false)
public class OrderDayHouse extends ResultList{
	
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="중량")
	private double gwt;
	@ApiModelProperty(value="통관_ID_구분")
	private String customsIdType;
	@ApiModelProperty(value="검사_여부")
	private String inspectionYn;
	@ApiModelProperty(value="선별_여부")
	private String seInspectionYn;
	@ApiModelProperty(value="X-RAY_여부")
	private String xrayYn;
	@ApiModelProperty(value="취하_여부")
	private String dropYn;
	@ApiModelProperty(value="이례_여부")
	private String unusualYn;
	@ApiModelProperty(value="반입여부")
	private String carryinYn;
	@ApiModelProperty(value="반입일자")
	private String carryinDate;
	@ApiModelProperty(value="반출여부")
	private String carryoutYn;
	@ApiModelProperty(value="반출일자")
	private String carryoutDate;
	@ApiModelProperty(value="통관_여부")
	private String clearanceYn;
	@ApiModelProperty(value="통관완료일")
	private String clearanceDate;
	@ApiModelProperty(value="배송접수여부")
	private String deliveryRecvYn;
	@ApiModelProperty(value="배송접수일자")
	private String deliveryRecvDate;
	@ApiModelProperty(value="미통관여부")
	private String clearanceViolationYn;
	@ApiModelProperty(value="외화금액_AMOUNT")
	private double amountCur;
	@ApiModelProperty(value="대표품목")
	private String commodityItem;
	@ApiModelProperty(value="화주_명칭")
	private String shipperName;
	@ApiModelProperty(value="수하인_명칭")
	private String consigneeName;
	@ApiModelProperty(value="수하인_명칭_한글")
	private String consigneeNameLoc;
	@ApiModelProperty(value="수하인_핸드폰")
	private String consigneeHp;
	@ApiModelProperty(value="수하인_전화번호")
	private String consigneeTel;
	@ApiModelProperty(value="비고사항")
	private String remarks;
}

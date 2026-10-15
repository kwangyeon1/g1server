package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "OrderSumMaster", description = "전자상거래 Order Summary Master 리스트")
@Alias("orderSumMaster")
@EqualsAndHashCode(callSuper=false)
public class OrderSumMaster extends ResultList{
	
	@ApiModelProperty(value="관리번호")
	private String masterOdJobNo;
	@ApiModelProperty(value="AGENT_코드")
	private String custCodeLv1;
	@ApiModelProperty(value="화주코드")
	private String custCodeLv2;
	@ApiModelProperty(value="출발지코드")
	private String origCode;
	@ApiModelProperty(value="도착지코드")
	private String destCode;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="관세사_명칭")
	private String cbName;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="하우스건수")
	private int hblCnt;
	@ApiModelProperty(value="목록건수")
	private int simpleCnt;
	@ApiModelProperty(value="일반건수")
	private int generalCnt;
	@ApiModelProperty(value="이행건수")
	private int implementationCnt;
	@ApiModelProperty(value="배제건수")
	private int excludeCnt;
	@ApiModelProperty(value="검사건수")
	private int inspectionCnt;
	@ApiModelProperty(value="선별건수")
	private int seInspectionCnt;
	@ApiModelProperty(value="X-RAY건수")
	private int xrayCnt;
	@ApiModelProperty(value="취하건수")
	private int dropCnt;
	@ApiModelProperty(value="홀드건수")
	private int holdCnt;
	@ApiModelProperty(value="이례건수")
	private int abnormalCnt;
	@ApiModelProperty(value="미반입건수")
	private int noCarryinCnt;
	@ApiModelProperty(value="반입건수")
	private int carryinCnt;
	@ApiModelProperty(value="반입건수")
	private int stock;
	@ApiModelProperty(value="반출건수")
	private int carryoutCnt;
	@ApiModelProperty(value="통관접수건수")
	private int clearanceReceiptCnt;
	@ApiModelProperty(value="미통관건수")
	private int clearanceViolationCnt;
	@ApiModelProperty(value="통관완료건수")
	private int clearanceCnt;
	@ApiModelProperty(value="배송완료건수")
	private int deliveryCnt;
	@ApiModelProperty(value="미배송건수")
	private int undeliveredCnt;
	@ApiModelProperty(value="배송사변경건수")
	private int deliveryChangedCnt;
	@ApiModelProperty(value="배송지변경건수")
	private int deliveryAddressChangedCnt;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="하우스건수")
	private int totalCnt;
	@ApiModelProperty(value="목록건수")
	private int totalSimpleCnt;
	@ApiModelProperty(value="일반건수")
	private int totalGeneralCnt;
	@ApiModelProperty(value="전체이행건수")
	private int totalImplementationCnt;
	@ApiModelProperty(value="배제건수")
	private int totalExcludeCnt;
	@ApiModelProperty(value="검사건수")
	private int totalInspectionCnt;
	@ApiModelProperty(value="선별건수")
	private int totalSeInspectionCnt;
	@ApiModelProperty(value="X-RAY건수")
	private int totalXrayCnt;
	@ApiModelProperty(value="취하건수")
	private int totalDropCnt;
	@ApiModelProperty(value="홀드건수")
	private int totalHoldCnt;
	@ApiModelProperty(value="이례건수")
	private int totalAbnormalCnt;
	@ApiModelProperty(value="미반입건수")
	private int totalNoCarryinCnt;
	@ApiModelProperty(value="반입건수")
	private int totalCarryinCnt;
	@ApiModelProperty(value="반입건수")
	private int totalStock;
	@ApiModelProperty(value="반출건수")
	private int totalCarryoutCnt;
	@ApiModelProperty(value="통관접수건수")
	private int totalClearanceReceiptCnt;
	@ApiModelProperty(value="미통관건수")
	private int totalClearanceViolationCnt;
	@ApiModelProperty(value="통관완료건수")
	private int totalClearanceCnt;
	@ApiModelProperty(value="배송완료건수")
	private int totalDeliveryCnt;
	@ApiModelProperty(value="미배송건수")
	private int totalUndeliveredCnt;
	@ApiModelProperty(value="배송사변경건수")
	private int totalDeliveryChangedCnt;
	@ApiModelProperty(value="배송지변경건수")
	private int totalDeliveryAddressChangedCnt;
	@ApiModelProperty(value="전자상거래여부Y카운트")
	private String ecmYCnt;
	@ApiModelProperty(value="전자상거래여부N카운트")
	private String ecmNCnt;
}

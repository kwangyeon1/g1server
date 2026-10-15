package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "OrderDay", description = "전자상거래 Order 일일보고 리스트")
@Alias("orderDay")
@EqualsAndHashCode(callSuper=false)
public class OrderDay extends ResultList{
	
	@ApiModelProperty(value="대리점")
	private String custCodeLv1;
	@ApiModelProperty(value="화주")
	private String custCodeLv2;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="마스터빌관리번호")
	private String masterOdJobNo;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="선명_편명")
	private String vesselFlight;
	@ApiModelProperty(value="출발지코드")
	private String origCode;
	@ApiModelProperty(value="도착지코드")
	private String destCode;
	@ApiModelProperty(value="관세사_명칭")
	private String cbName;
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
	@ApiModelProperty(value="이례건수")
	private int unusualCnt;
	@ApiModelProperty(value="미반입건수")
	private int noCarryinCnt;
	@ApiModelProperty(value="반입건수")
	private int carryinCnt;
	@ApiModelProperty(value="반출건수")
	private int carryoutCnt;
	@ApiModelProperty(value="통관완료건수")
	private int clearanceCnt;
	@ApiModelProperty(value="배송접수건수")
	private int deliveryRecvCnt;
	@ApiModelProperty(value="통관진행율")
	private int clearancePercent;
	@ApiModelProperty(value="배송진행율")
	private int deliveryPercent;
	@ApiModelProperty(value="재고")
	private int stock;
	@ApiModelProperty(value="미통관건수")
	private int clearanceViolationCnt;
	@ApiModelProperty(value="배송완료건수")
	private int deliveryCnt;
	
	@ApiModelProperty(value="전체건수")
	private int totalCnt;
	@ApiModelProperty(value="전체목록건수")
	private int totalSimpleCnt;
	@ApiModelProperty(value="전체일반건수")
	private int totalGeneralCnt;
	@ApiModelProperty(value="전체이행건수")
	private int totalImplementationCnt;
	@ApiModelProperty(value="전체배제건수")
	private int totalExcludeCnt;
	@ApiModelProperty(value="전체검사건수")
	private int totalInspectionCnt;
	@ApiModelProperty(value="전체선별건수")
	private int totalSeInspectionCnt;
	@ApiModelProperty(value="전체X-RAY건수")
	private int totalXrayCnt;
	@ApiModelProperty(value="전체취하건수")
	private int totalDropCnt;
	@ApiModelProperty(value="전체이례건수")
	private int totalUnusualCnt;
	@ApiModelProperty(value="전체미반입건수")
	private int totalNoCarryinCnt;
	@ApiModelProperty(value="전체반입건수")
	private int totalCarryinCnt;
	@ApiModelProperty(value="전체반출건수")
	private int totalCarryoutCnt;
	@ApiModelProperty(value="전체통관완료건수")
	private int totalClearanceCnt;
	@ApiModelProperty(value="전체배송접수건수")
	private int totalDeliveryRecvCnt;
	@ApiModelProperty(value="전체미통관건수")
	private int totalClearanceViolationCnt;
	@ApiModelProperty(value="전자상거래여부Y카운트")
	private String ecmYCnt;
	@ApiModelProperty(value="전자상거래여부N카운트")
	private String ecmNCnt;
	
	@ApiModelProperty(value="컨테이너번호")
	private String cntrNo;
	@ApiModelProperty(value="컨테이너종류")
	private String cntrCode;
	@ApiModelProperty(value="중량")
	private double gwt;
	@ApiModelProperty(value="볼륨중량")
	private double cbm;
}

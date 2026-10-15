package org.yht.exps.express.model.invoice;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "PartnerSummaryCharge", description = "전자상거래 정산관리 파트너집계")
@Alias("partnerSummaryCharge")
@EqualsAndHashCode(callSuper=false)
public class PartnerSummaryCharge extends ResultList{
	
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="선명_편명")
	private String vesselFlight;
	@ApiModelProperty(value="출발지코드")
	private String origCode;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="청구지")
	private String customerCode;
	@ApiModelProperty(value="정산일자")
	private String invoiceDate;
	@ApiModelProperty(value="통화단위")
	private String currencyCode;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="하우스건수")
	private int hblCnt;
	@ApiModelProperty(value="목록건수")
	private int simpleCnt;
	@ApiModelProperty(value="일반건수")
	private int generalCnt;
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
	@ApiModelProperty(value="작업료")
	private double whfCost;
	@ApiModelProperty(value="작업료(CUR)")
	private double whfCostCur;
	@ApiModelProperty(value="기타")
	private double otcCost;
	@ApiModelProperty(value="계산금액")
	private int wahaCost;
	@ApiModelProperty(value="부가세")
	private int aaaaCost;
	@ApiModelProperty(value="보수금액")
	private int wmainCost;
	@ApiModelProperty(value="경비료")
	private int securCost;
	@ApiModelProperty(value="작업료")
	private int wahoCost;
	@ApiModelProperty(value="보험료")
	private int insCost;
	@ApiModelProperty(value="폐포장비")
	private int wpackCost;
	@ApiModelProperty(value="배송료")
	private int dlcCost;
	@ApiModelProperty(value="도선료")
	private int dislaCost;
	@ApiModelProperty(value="제주운임")
	private int djuCost;
	@ApiModelProperty(value="기타운임")
	private int dtcCost;
	@ApiModelProperty(value="관세")
	private int dtyCost;
	@ApiModelProperty(value="부가세")
	private int taxCost;
	@ApiModelProperty(value="통관수수료")
	private int cfcCost;
	@ApiModelProperty(value="검역료")
	private int quaCost;
	@ApiModelProperty(value="무적")
	private int execCost;
	@ApiModelProperty(value="보수작업")
	private int maintCost;
	@ApiModelProperty(value="협정")
	private int agreCost;
	@ApiModelProperty(value="수출")
	private int expCost;
	@ApiModelProperty(value="기타")
	private int botCost;
	@ApiModelProperty(value="기타(정산외운임코드)")
	private double etcCost;
	@ApiModelProperty(value="합계")
	private int arAmount;
	@ApiModelProperty(value="전체합계")
	private int totalArAmount;
	
}

package org.yht.exps.express.model.eCommerce.coio;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "CoioSum", description = "전자상거래 반입/반출 summury tab")
@Alias("coioSum")
public class CoioSum{

	@ApiModelProperty(value="반입_일자")
	private String carryinDate;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;		
	@ApiModelProperty(value="출발지코드")
	private String origCode;
	@ApiModelProperty(value="출발지명")
	private String origName;
	@ApiModelProperty(value="도착지코드")
	private String destCode;
	@ApiModelProperty(value="도착지명")
	private String destName;	
	@ApiModelProperty(value="AGENT_코드")
	private String agentCode;		
	@ApiModelProperty(value="화주_명칭")
	private String shipperName;		
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="하우스건수")
	private int hblCnt;	
	@ApiModelProperty(value="목록건수")
	private int cdTypeYyCnt;		
	@ApiModelProperty(value="일반건수")
	private int cdTypeNnCnt;		
	@ApiModelProperty(value="배제건수")
	private int cdTypeBbCnt;
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
	private String unusualCnt;
	@ApiModelProperty(value="무적건수")
	private String mfnCnt;
	@ApiModelProperty(value="반입건수")
	private int carryinCnt;		
	@ApiModelProperty(value="미반입건수")
	private String noCarryinCnt;
	@ApiModelProperty(value="반출건수")
	private int carryoutCnt;			
	@ApiModelProperty(value="미출고건수")
	private int noReleaseCnt;
	@ApiModelProperty(value="통관완료건수")
	private int clearanceCnt;
	@ApiModelProperty(value="통관미완료건수")
	private int beforeClearanceCnt;
	@ApiModelProperty(value="미통관건수")
	private int clearanceViolationCnt;	
	@ApiModelProperty(value="배송접수건수")
	private int deliveryRecvCnt;	
	@ApiModelProperty(value="배송완료건수")
	private int deliveryCnt;	
	@ApiModelProperty(value="재고건수")
	private String stockCnt;
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	@ApiModelProperty(value="도입사코드 5자리 + 국가 2자리")
	private String entBusinessCode;
	@ApiModelProperty(value="SOLUTION")
	private String solution;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="오더일자")
	private String poDate;
	@ApiModelProperty(value="P/O번호")
	private String poNo;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	
	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;	
	
}

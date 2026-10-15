package org.yht.exps.express.model.eCommerce.delivery;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "DeliveryM", description = "전자상거래 Delivery-M")
@Alias("deliveryM")
public class DeliveryM {
	@ApiModelProperty(value="진행단계")
	private String lastPodStatus;	 // 추후 수정 
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="입항일자")
	private String arDate;	
	@ApiModelProperty(value="선명_편명")
	private String vesselFlight;
	@ApiModelProperty(value="출발지코드")
	private String origCode;
	@ApiModelProperty(value="도착지코드")
	private String destCode;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;	
	@ApiModelProperty(value="마스터오더관리번호")
	private String masterOdJobNo;
	@ApiModelProperty(value="하우스건수")
	private int hblCnt;	
	@ApiModelProperty(value="배송사미지정건수")
	private int deliveryUnspecifyCnt;		
	@ApiModelProperty(value="배송사지정건수")
	private int deliveryDesignateCnt;	
	@ApiModelProperty(value="배송요청건수")
	private int deliveryReqCnt;		
	@ApiModelProperty(value="배송접수건수")
	private int deliveryRecvCnt;		
	@ApiModelProperty(value="미배송건수")
	private int undeliveryCnt;		
	@ApiModelProperty(value="배송완료건수")
	private int deliveryCnt;	
	@ApiModelProperty(value="배송요청오류건수")
	private int deliveryReqErrCnt;
	@ApiModelProperty(value="배송요청대기건수")
	private int deliveryWaitingCnt;
	@ApiModelProperty(value="배송집하건수")
	private int deliveryPickupCnt;
	@ApiModelProperty(value="배송출발건수")
	private int deliveryStartCnt;
	@ApiModelProperty(value="AGENT_코드")
	private String agentCode;	
	@ApiModelProperty(value="BL구분")
	private String blTypeMh;	
	@ApiModelProperty(value="바운드", example="COMMON, AI, AO, OI, OO")
	private String bound;		

	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;
	/* 마스터기준 합계목록 */
	@ApiModelProperty(value="마스터sum")
	private int sumCount;
	@ApiModelProperty(value="하우스sum")
	private int sumHblNo;
	@ApiModelProperty(value="수량sum")
	private int sumPieces;
	@ApiModelProperty(value="중량sum")
	private double sumGwt;		
	/*마스터 내 순수 하우스 수량, 조회조건을 타지않음*/
	@ApiModelProperty(value="마스터 내 총 하우스 수")
	private int totalHblCnt;	
	@ApiModelProperty(value="배송상태")
	private String deliveryStatus;
	@ApiModelProperty(value="배송사지정유무")
	private String deliveryCodeYn;
}

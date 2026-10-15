package org.yht.exps.express.model.eCommerce.broker;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "BrokerMaster", description = "전자상거래 broker-M 리스트")
@Alias("brokerMaster")
public class BrokerMaster extends ResultList{
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="출발항")
	private String origCode;
	@ApiModelProperty(value="도착항")
	private String destCode;
	@ApiModelProperty(value="편명")
	private String vesselFlight;
	@ApiModelProperty(value="MAWB No")
	private String mblNo;
	@ApiModelProperty(value="H.건수")
	private int hblCnt;
	@ApiModelProperty(value="신고전")
	private int clearanceViolationCnt1;
	@ApiModelProperty(value="신고의뢰")
	private int clearanceViolationCnt2;
	@ApiModelProperty(value="미통관")
	private int clearanceViolationCnt3;
	@ApiModelProperty(value="신고완료")
	private int clearanceViolationCnt4;
	@ApiModelProperty(value="미통관")
	private int clearanceViolationCnt;
	@ApiModelProperty(value="목록")
	private int cdTypeYCnt;
	@ApiModelProperty(value="일반")
	private int cdTypeNCnt;
	@ApiModelProperty(value="배제")
	private int cdTypeBCnt;
	@ApiModelProperty(value="취하")
	private int dropCnt;
	@ApiModelProperty(value="검사")
	private int inspectionCnt;
	@ApiModelProperty(value="선별")
	private int seInspectionCnt;
	@ApiModelProperty(value="X-Ray")
	private int xrayCnt;
	@ApiModelProperty(value="일검")
	private int geInspectionCnt;
	@ApiModelProperty(value="검역")
	private String inspectionType;
	@ApiModelProperty(value="안내")
	//private int smsInformCnt;
	private int clearanceNoticeCnt;
	@ApiModelProperty(value="진행단계")
	private String lastPodStatus;
	@ApiModelProperty(value="마스터오더번호")
	private String masterOdJobNo;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="상위업체이름")
	private String custNameLv1;
	@ApiModelProperty(value="하위업체")
	private String custCodeLv2;
	@ApiModelProperty(value="하위업체이름")
	private String custNameLv2;
	@ApiModelProperty(value="관세사코드")
	private String cbCode;
	@ApiModelProperty(value="관세사_명칭")
	private String cbName;	
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="수출입")
	private String boundType;
	@ApiModelProperty(value="관세사 I/F")
	private String cbIfStatus;
	@ApiModelProperty(value="통관신고구분")
	private String cclType;
	@ApiModelProperty(value="전자상거래여부Y카운트")
	private String ecmYCnt;
	@ApiModelProperty(value="전자상거래여부N카운트")
	private String ecmNCnt;
	
	/* 마스터기준 합계목록 */
	@ApiModelProperty(value="마스터sum")
	private int sumCount;
	@ApiModelProperty(value="하우스sum")
	private int sumHblNo;
	@ApiModelProperty(value="수량sum")
	private int sumPieces;
	@ApiModelProperty(value="중량sum")
	private double sumGwt;	
	
}

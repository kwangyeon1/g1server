package org.yht.exps.express.model.eCommerce.coio;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "CoioLocSub", description = "전자상거래 반입/반출 location sub")
@Alias("coioLocSub")
public class CoioLocSub{

	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;	
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="반입_일자")
	private String carryinDate;
	@ApiModelProperty(value="반출_일자")
	private String carryoutDate;
	@ApiModelProperty(value="통관완료일")
	private String clearanceFinishDate;
	@ApiModelProperty(value="재고일")
	private String stockDateTime;
	@ApiModelProperty(value="재고등록유저")
	private String stockUserId;
	@ApiModelProperty(value="수하인_명칭")
	private String consigneeName;	
	@ApiModelProperty(value="수하인_한글이름")
	private String consigneeNameLoc;
	@ApiModelProperty(value="수하인 전화번호")
	private String consigneeTel;
	@ApiModelProperty(value="수하인 휴대전화")
	private String consigneeHp;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="중량")
	private double gwt;		
	@ApiModelProperty(value="검사여부", example="Y or N")
	private String inspectionResultYn;
	@ApiModelProperty(value="홀드_여부")
	private String holdReleaseYn;
	@ApiModelProperty(value="홀드_사유")
	private String holdReason;
	@ApiModelProperty(value="대표품목")
	private String commodityItem;	
	@ApiModelProperty(value="출발지코드")
	private String origCode;
	@ApiModelProperty(value="AGENT_코드")
	private String agentCode;		
	@ApiModelProperty(value="화주_명칭")
	private String shipperName;		
	@ApiModelProperty(value="로케이션 코드")
	private String whLocation;	
	@ApiModelProperty(value="로케이션명")
	private String whLocationName;
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
	
	@ApiModelProperty(value="진행상태")
	private String lastPodStatus;
	@ApiModelProperty(value="배송사")
	private String deliveryName;
	@ApiModelProperty(value="관세사")
	private String cbName;
	@ApiModelProperty(value="하위업체")
	private String custCodeLv2;
	@ApiModelProperty(value="통관구분")
	private String cdType;
	
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;	
	
}

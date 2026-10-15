package org.yht.exps.express.model.eCommerce.coio;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "CoioDet", description = "전자상거래 반입/반출")
@Alias("coioDet")
public class CoioDet{

	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="HSN")
	private String hsn;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="Inner Pieces (실제수량, EDI신고용)")
	private int piecesReal;	
	@ApiModelProperty(value="중량")
	private double gwt;
	@ApiModelProperty(value="수하인_명칭")
	private String consigneeNameLoc;
	@ApiModelProperty(value="수하인주소")
	private String consigneeAddressLoc;
	@ApiModelProperty(value="수하인_전화번호")
	private String consigneeTel;	
	@ApiModelProperty(value="수하인_휴대전화")
	private String consigneeHp;	
	@ApiModelProperty(value="목록")
	private String cdTypeY;		
	@ApiModelProperty(value="일반")
	private String cdTypeN;		
	@ApiModelProperty(value="배제")
	private String cdTypeB;
	@ApiModelProperty(value="통관구분")
	private String cdType;
	@ApiModelProperty(value="통관구분명")
	private String cdTypeName;
	@ApiModelProperty(value="검사여부")
	private String inspectionYn;
	@ApiModelProperty(value="선별")
	private String seInspectionYn;
	@ApiModelProperty(value="검사결과_여부")
	private String inspectionResultYn;	
	@ApiModelProperty(value="X-RAY_여부")
	private String xrayYn;
	@ApiModelProperty(value="취하_여부")
	private String dropYn;
	@ApiModelProperty(value="취하_사유")
	private String dropReason;
	@ApiModelProperty(value="홀드_여부")
	private String holdYn;
	@ApiModelProperty(value="홀드_사유")
	private String holdReason;	
	@ApiModelProperty(value="무적")
	private String mfn;	
	@ApiModelProperty(value="반입_일자")
	private String carryinDate;
	@ApiModelProperty(value="통관접수일")
	private String clearanceReceiptDate;	
	@ApiModelProperty(value="통관완료일")
	private String clearanceFinishDate;
	@ApiModelProperty(value="미통관코드")
	private String clearanceViolationCode;
	@ApiModelProperty(value="미통관사유")
	private String clearanceViolationReason;
	@ApiModelProperty(value="미통관일시")
	private String clearanceViolationDate;
	@ApiModelProperty(value="반출_일자")
	private String carryoutDate;	
	@ApiModelProperty(value="출고_일자")
	private String releaseDate;	
	@ApiModelProperty(value="출고_담당자")
	private String carryoutUserId;
	@ApiModelProperty(value="로케이션")
	private String whLocation;
	@ApiModelProperty(value="재고일(시간)")
	private String stockDateTime;	
	@ApiModelProperty(value="재고_담당자")
	private String stockUserId;		
	@ApiModelProperty(value="배송접수일")
	private String deliveryRecvDate;
	@ApiModelProperty(value="집하처리")
	private String pickupDate;
	@ApiModelProperty(value="배송일자")
	private String deliveryDate;	
	@ApiModelProperty(value="미배송사유")
	private String undeliveryReason;
	@ApiModelProperty(value="미배송일시")
	private String undeliveryDate;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;	
	@ApiModelProperty(value="선명_편명")
	private String vesselFlight;
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
    @ApiModelProperty(value="하위업체")
    private String custCodeLv2;
    @ApiModelProperty(value="화주_명칭")
	private String shipperName;
    @ApiModelProperty(value="배송비고")
    private String deliveryRemarks;
    @ApiModelProperty(value="파일공유 유무")
	private String fileShare;	
	@ApiModelProperty(value="배송사명")
	private String deliveryName;	
	@ApiModelProperty(value="관세사명")
	private String cbName;	
	@ApiModelProperty(value="진행상태")
	private String lastPodStatus;
	
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
	
	@ApiModelProperty(value="M/H")
	private String blTypeMh;
	@ApiModelProperty(value="이례")
	private String unusual;
	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;	
	
}

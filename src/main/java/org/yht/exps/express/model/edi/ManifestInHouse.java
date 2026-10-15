package org.yht.exps.express.model.edi;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ManifestInHouse", description = "전자상거래  적하목록수입 하우스 리스트")
@Alias("manifestInHouse")
public class ManifestInHouse {
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="마스터관리번호")
	private String masterOdJobNo;
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	@ApiModelProperty(value="오더일자")
	private String poDate;
	@ApiModelProperty(value="서류여부")
	private String documentYn;
	@ApiModelProperty(value="진행상태")
	private String lastPodStatus;
	@ApiModelProperty(value="진행상태명")
	private String lastPodStatusName;
	@ApiModelProperty(value="상위업체_이름")
	private String upperCusName;
	@ApiModelProperty(value="오더입력방법")
	private String inputMethod;
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="과태료")
	private String penaltyYn;
	@ApiModelProperty(value="제출번호")
	private String simpleDocNo;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="EXP : 수출, IMP:수입")
	private String boundType;
	@ApiModelProperty(value="HSN")
	private String hsn;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="통관구분")
	private String cdType;
	@ApiModelProperty(value="통관구분명")
	private String cdTypeName;
	@ApiModelProperty(value="용도구분")
	private String cdUseType;
	@ApiModelProperty(value="용도구분명")
	private String cdUseTypeName;
	@ApiModelProperty(value="거래구분")
	private String ecmType;
	@ApiModelProperty(value="거래구분명")
	private String ecmTypeName;
	@ApiModelProperty(value="검사_여부")
	private String inspectionYn;
	@ApiModelProperty(value="선별_여부")
	private String seInspectionYn;
	@ApiModelProperty(value="X-RAY_여부")
	private String xrayYn;
	@ApiModelProperty(value="취하_여부")
	private String dropYn;
	@ApiModelProperty(value="일검_여부")
	private String geInspectionYn;
	@ApiModelProperty(value="홀드처리_여부")
	private String holdReleaseYn;
	@ApiModelProperty(value="홀드_여부")
	private String holdYn;
	@ApiModelProperty(value="하기이상")
	private String abnormalityCode;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="Inner Pieces (실제수량, EDI신고용)")
	private int piecesReal;
	@ApiModelProperty(value="단위_수량")
	private int unitPieces;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="CBM")
	private double cbm;
	@ApiModelProperty(value="통화단위")
	private String currencyCode;
	@ApiModelProperty(value="외화금액_AMOUNT")
	private double amountCur;
	@ApiModelProperty(value="원화금액_AMOUNT")
	private double amountLoc;
	@ApiModelProperty(value="허용품목코드")
	private String allowedItemCode;
	@ApiModelProperty(value="대표품목")
	private String commodityItem;
	@ApiModelProperty(value="수하인_명칭")
	private String consigneeName;
	@ApiModelProperty(value="수하인_주소")
	private String consigneeAddress;
	@ApiModelProperty(value="수하인_주소_A")
	private String consigneeAddressA;
	@ApiModelProperty(value="수하인_주소_B")
	private String consigneeAddressB;
	@ApiModelProperty(value="수하인_주소_C")
	private String consigneeAddressC;
	@ApiModelProperty(value="수하인_주소_D")
	private String consigneeAddressD;
	@ApiModelProperty(value="수하인_핸드폰")
	private String consigneeHp;
	@ApiModelProperty(value="수하인_전화번호")
	private String consigneeTel;
	@ApiModelProperty(value="수하인_우편번호")
	private String consigneeZipcode;
	@ApiModelProperty(value="우편번호_오류")
	private String zipcodeErrorYn;
	@ApiModelProperty(value="수하인_명칭_한글")
	private String consigneeNameLoc;
	@ApiModelProperty(value="수하인_주소_한글")
	private String consigneeAddressLoc;
	@ApiModelProperty(value="수하인_사업자번호")
	private String consigneeBusinessId;
	@ApiModelProperty(value="통관_ID_구분")
	private String customsIdType;
	@ApiModelProperty(value="통관_ID_구분명")
	private String customsIdTypeName;
	@ApiModelProperty(value="통관_ID")
	private String customsId;
	@ApiModelProperty(value="통관부호_검증상태")
	private String customsIdVerify;
	@ApiModelProperty(value="통관부호_오류내역")
	private String customsIdError;
	@ApiModelProperty(value="출발지코드")
	private String origCode;
	@ApiModelProperty(value="출발지명")
	private String origName;
	@ApiModelProperty(value="출발국")
	private String origCountry;
	@ApiModelProperty(value="화주_명칭")
	private String shipperName;
	@ApiModelProperty(value="화주_주소")
	private String shipperAddress;
	@ApiModelProperty(value="화주_주소_A")
	private String shipperAddressA;
	@ApiModelProperty(value="화주_주소_B")
	private String shipperAddressB;
	@ApiModelProperty(value="화주_주소_C")
	private String shipperAddressC;
	@ApiModelProperty(value="화주_주소_D")
	private String shipperAddressD;
	@ApiModelProperty(value="화주_전화번호")
	private String shipperTel;
	@ApiModelProperty(value="홈페이지")
	private String url;
	@ApiModelProperty(value="오류통보")
	private String simpleErrorDesc;
	@ApiModelProperty(value="검사결과")
	private String inspectionResultYn;
	@ApiModelProperty(value="취하코드")
	private String dropCode;
	@ApiModelProperty(value="취하사유(내용)")
	private String dropReason;
	@ApiModelProperty(value="비고")
	private String remarks;
	@ApiModelProperty(value="운송사코드")
	private String trCode;
	@ApiModelProperty(value="운송사_명칭")
	private String trName;
	@ApiModelProperty(value="배송사_코드")
	private String deliveryCode;
	@ApiModelProperty(value="배송사_명")
	private String deliveryName;
    @ApiModelProperty(value="배송사_상태")
    private String deliveryStatusName;
	@ApiModelProperty(value="관세사_코드")
	private String cbCode;
	@ApiModelProperty(value="관세사_명칭")
	private String cbName;
	@ApiModelProperty(value="FileShare유무")
	private String fileShare;
	@ApiModelProperty(value="SMS안내_여부")
	private String smsInformYn;
	@ApiModelProperty(value="SMS처리_여부")
	private String smsInformResYn;
	@ApiModelProperty(value="목록신고 일시")
	private String simpleReportDate;
	@ApiModelProperty(value="목록신고 결과통보 수입신고불가여부")
	private String simpleStatementYn;
	@ApiModelProperty(value="목록신고 결과통보 수입신고불가사유")
	private String simpleStatementReason;
	@ApiModelProperty(value="반입근거번호")
	private String carryinNo;
	@ApiModelProperty(value="반입위치")
	private String whInPos;
	@ApiModelProperty(value="반입일자")
	private String carryinDate;
	@ApiModelProperty(value="반출일자")
	private String carryoutDate;
	@ApiModelProperty(value="반입여부")
	private String carryinYn;
	@ApiModelProperty(value="반출여부")
	private String carryoutYn;
	@ApiModelProperty(value="최초통관구분")
	private String cdTypeOld;
	@ApiModelProperty(value="검역(특송장)")
	private String inspectionType;
	@ApiModelProperty(value="목록취하세관담당자")
	private String simpleAuthName;	
	@ApiModelProperty(value="우범화물여부")
	private String crimeSelectionYn;
	@ApiModelProperty(value="우범화물통보구분")
	private String inspectionAppGubn;
	@ApiModelProperty(value="검사지정사유")
	private String inspectionAppCode;
	@ApiModelProperty(value="하기이상코드")
	private String abnormalCode;
	@ApiModelProperty(value="도착일자")
	private String arDate;
	@ApiModelProperty(value="지정창고홀드여부")
	private String cfaHoldYn;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="MSN")
	private String msn;
	@ApiModelProperty(value="도착지코드")
	private String destCode;
	@ApiModelProperty(value="편(선)명")
	private String vesselFlight;
	@ApiModelProperty(value="검사결과")
	private String seInspectionCodeName;
	@ApiModelProperty(value="도착항")
	private String destPort;
	@ApiModelProperty(value="보세창고")
	private String assignmentWhCode;
	@ApiModelProperty(value="하기장소")
	private String discWhCode;
	@ApiModelProperty(value="유효기간시작일")
	private String customsIdStartDate;
	@ApiModelProperty(value="유효기간만료일")
	private String customsIdEndDate;
	
	@ApiModelProperty(value="전자상거래유형")
	private String ecmBuyType;
	@ApiModelProperty(value="구매대행등록번호")
	private String agentRegNo;
	@ApiModelProperty(value="구매대행 업자명")
	private String agentName;
	@ApiModelProperty(value="해외판매자 부호")
	private String sellerRegNo;
	@ApiModelProperty(value="해외판매자 명")
	private String sellerName;
	@ApiModelProperty(value="판매중개자 부호")
	private String salesBrokerRegNo;
	@ApiModelProperty(value="판매중개자 명")
	private String salesBrokerName;
	@ApiModelProperty(value="주문번호리스트")
	private String poNoList;
	@ApiModelProperty(value="목록취하결과통보")
	private String simpleDropEdiStatus;
	@ApiModelProperty(value="목록취하오류내역")
	private String simpleDropErrorDesc;
	@ApiModelProperty(value="전자상거래여부")
	private String ecmYn;

	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;
	
	@ApiModelProperty(value="별도관리물품 중량")
	private double abnormalGwt;
	@ApiModelProperty(value="별도관리물품 수량")
	private int abnormalPieces;
	
	/** 지적재산권 침해 관련 */
	@ApiModelProperty(value="상표권 침해의심물품")
	private String suspectedTrademark;
	@ApiModelProperty(value="상표권침해물품통관보류")
	private String trademarkInfringing;
	
	@ApiModelProperty(value="배송대행업체부호")
	private String forwardAgentCode;
	
	/* UPS 엑셀 다운로드 */
	@ApiModelProperty(value="오류검증 (UPS)")
	private String errorCheck;
	@ApiModelProperty(value="발송국가")
	private String shipperCountryCode;
	@ApiModelProperty(value="USD_AMOUNT")
	private double amountUsd;
	@ApiModelProperty(value="수하인과 다른 경우")
	private String diffYn;
	@ApiModelProperty(value="PLAN_YN")
	private String planYn;
	@ApiModelProperty(value="품명오류내역")
	private String ccrErrorDesc;
	@ApiModelProperty(value="IMPORTER")
	private String actualCstName;
	@ApiModelProperty(value="개인통관부호")
	private String deliveryNote;
}


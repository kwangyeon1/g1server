package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "OrderCommon", description = "전자상거래 Order 공통 리스트")
@Alias("orderCommon")
@EqualsAndHashCode(callSuper=false)
public class OrderCommon extends ResultList{
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="오더일자")
	private String poDate;
	@ApiModelProperty(value="오더번호")
	private String poNo;
	@ApiModelProperty(value="중개사업자1")
	private String venderId1;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="출항시간")
	private String obTime;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="입항시간")
	private String arTime;
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
	@ApiModelProperty(value="도착지국가")
	private String destCountry;
	@ApiModelProperty(value="도착지코드_국가")
	private String destCodeCountry;
	@ApiModelProperty(value="도착지코드_지역")
	private String destCodeArea;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="배송사_코드")
	private String deliveryCode;
	@ApiModelProperty(value="배송요청")
	private String deliveryNote;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="발송국가")
	private String origCountry;
	@ApiModelProperty(value="품명")
	private String commodityItem;
	@ApiModelProperty(value="마감여부")
	private String finishYn;
	@ApiModelProperty(value="수량")
	private Integer pieces;
	@ApiModelProperty(value="수량")
	private Integer odPieces;
	@ApiModelProperty(value="단위_수량")
	private String unitPieces;
	@ApiModelProperty(value="단위_수량")
	private String odUnitPieces;
	@ApiModelProperty(value="통화단위")
	private String currencyCode;
	@ApiModelProperty(value="단가")
	private Double unitPrice;
	@ApiModelProperty(value="금액_AMOUNT")
	private Double amountLoc;
	@ApiModelProperty(value="총중량")
	private Double gwt;
	@ApiModelProperty(value="볼륨중량")
	private Double vwt;
	@ApiModelProperty(value="cbm")
	private Double cbm;
	@ApiModelProperty(value="홈페이지")
	private String ecmUrl;	
	@ApiModelProperty(value="브랜드")
	private String brand;
	@ApiModelProperty(value="item갯수")
	private String itemCnt;
	@ApiModelProperty(value="rowNum")
	private String rowNum;
	@ApiModelProperty(value="공통코드조회결과")
	private String code;
	@ApiModelProperty(value="마스터오더관리번호")
	private String masterOdJobNo;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="HSN")
	private String hsn;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="MSN")
	private String msn;
	@ApiModelProperty(value="세관코드")
	private String customsCode;
	@ApiModelProperty(value="세관과코드")
	private String customsDptCode;
	@ApiModelProperty(value="세관코드(정정)")
	private String customsModifyCode;
	@ApiModelProperty(value="세관과코드(정정)")
	private String customsModifyDptCode;
	@ApiModelProperty(value="blTypeIert")
	private String blTypeIert;
	@ApiModelProperty(value="목적항")
	private String destPort;
	@ApiModelProperty(value="목적항명")
	private String destPortName;
	@ApiModelProperty(value="배정코드")
	private String assignmentCode;
	@ApiModelProperty(value="배정코드명")
	private String assignmentName;
	@ApiModelProperty(value="화주_명칭")
	private String shipperName;
	@ApiModelProperty(value="화주_주소")
	private String shipperAddress;
	@ApiModelProperty(value="화주_전화번호")
	private String shipperTel;
	@ApiModelProperty(value="화주_핸드폰번호")
	private String shipperHp;
	@ApiModelProperty(value="화주_우편번호")
	private String shipperZipcode;
	@ApiModelProperty(value="화주_국가코드")
	private String shipperCountryCode;
	@ApiModelProperty(value="화주STATE_코드")
	private String shipperStateCode;
	@ApiModelProperty(value="화주_사업자번호")
	private String shipperBusinessId;
	@ApiModelProperty(value="수하인_명칭")
	private String consigneeName;
	@ApiModelProperty(value="수하인_주소")
	private String consigneeAddress;
	@ApiModelProperty(value="수하인_전화번호")
	private String consigneeTel;
	@ApiModelProperty(value="수하인_우편번호")
	private String consigneeZipcode;
	@ApiModelProperty(value="수하인_국가코드")
	private String consigneeCountryCode;
	@ApiModelProperty(value="수하인STATE_코드")
	private String consigneeStateCode;
	@ApiModelProperty(value="수하인_사업자번호")
	private String consigneeBusinessId;
	@ApiModelProperty(value="변경주소")
	private String deliveryAddressChanged;
	@ApiModelProperty(value="변경사유")
	private String deliveryReasonChanged;
	@ApiModelProperty(value="관세청정보관리번호")
	private String ediMailboxSeqNo;
	@ApiModelProperty(value="통관목록제출번호")
	private String simpleDocNo;
	
	@ApiModelProperty(value="수하인(한)")
	private String consigneeNameLoc;
	@ApiModelProperty(value="수하인주소(한)")
	private String consigneeAddressLoc;
	@ApiModelProperty(value="수하인휴대전화")
	private String consigneeHp;
	
	@ApiModelProperty(value="적하정정-배정코드")
	private String ediAssignmentWhCode;
	@ApiModelProperty(value="적하정정-배정코드명")
	private String ediAssignmentWhName;
	
	@ApiModelProperty(value="변경후 배송사")
	private String deliveryNameChanged;
	
	@ApiModelProperty(value="선별마감여부")
	private String selectionYn;
	
	@ApiModelProperty(value="컨테이너번호")
	private String cntrNo;
	@ApiModelProperty(value="컨테이너정정구분_코드(I: B/L 추가, A:추가,U:정정, D:삭제)")
	private String cntrModType;
	@ApiModelProperty(value="컨테이너정정_코드")
	private String cntrModCode;
	@ApiModelProperty(value="컨테이너EDI코드")
	private String cntrEdiCode;
	@ApiModelProperty(value="컨테이너타입")
	private String cntrCode;
	@ApiModelProperty(value="봉인번호")
	private String cntrSealNo1;
	@ApiModelProperty(value="봉인번호")
	private String cntrSealNo2;
	@ApiModelProperty(value="봉인번호")
	private String cntrSealNo3;
	@ApiModelProperty(value="컨테이너수량")
	private String cntrPieces;
	@ApiModelProperty(value="컨테이너수량단위")
	private String cntrUnitPieces;
	@ApiModelProperty(value="컨테이너중량")
	private String cntrGwt;
	@ApiModelProperty(value="컨테이너CBM")
	private String cntrCbm;
	
	@ApiModelProperty(value="과태료여부")
	private String penaltyYn;
	@ApiModelProperty(value="통관접수일")
	private String clearanceReceiptDate;
	@ApiModelProperty(value="자체선별임시데이터건수")
	private int selfSelectionCnt;
	@ApiModelProperty(value="자동선별임시데이터건수")
	private int autoSelectionCnt;
	@ApiModelProperty(value="선별종류")
	private String selectionType;
	@ApiModelProperty(value="통관타입")
	private String cdType;
	@ApiModelProperty(value="배송일자")
	private String deliveryDate;
	@ApiModelProperty(value="배송사")
	private String deliveryName;
	@ApiModelProperty(value="목록신고일자")
	private String simpleReportDate;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="대리점")
	private String custCodeLv1Name;
	@ApiModelProperty(value="항공/해운수입 하기(하선)장소코드 (장치장 또는 보세창고코드)")
	private String discWhCode;
	@ApiModelProperty(value="하기장소명")
	private String discWhName;
	@ApiModelProperty(value="항공/해운수입 하기(하선)장소코드 (장치장 또는 보세창고코드)")
	private String odDiscWhCode;
	@ApiModelProperty(value="하기장소명")
	private String odDiscWhName;
	@ApiModelProperty(value="하기물품코드")
	private String discItemCode;
	@ApiModelProperty(value="하기물품코드명")
	private String discItemName;
	@ApiModelProperty(value="수입 전용. 배정코드")
	private String assignmentWhCode;
	@ApiModelProperty(value="배정코드명")
	private String assignmentWhName;
	@ApiModelProperty(value="항공사_선사코드")
	private String lineCode;
	@ApiModelProperty(value="항차")
	private String voyage;
	@ApiModelProperty(value="적하정정사유")
	private String modReason;
	@ApiModelProperty(value="적하정정관리번호")
	private String modJobNo;
	@ApiModelProperty(value="반출사유코드")
	private String carryoutCode;
	@ApiModelProperty(value="정산여부 업체코드")
	private String invoiceYnCustCode;
	@ApiModelProperty(value="정산여부 업체명")
	private String invoiceYnCustName;
	
	@ApiModelProperty(value="운임조건")
	private String incoterms;
	@ApiModelProperty(value="면장(X,E) 결재방법(수출-BROS)")
	private String licensePayMethod;
	@ApiModelProperty(value="면장(X,E)(수출-BROS)")
	private String licenseType;
	@ApiModelProperty(value="X-면장 여부")
	private String xLicenseYn;
	@ApiModelProperty(value="E-면장 여부")
	private String eLicenseYn;
	
	@ApiModelProperty(value="환율")
	private String exchangeRate;
	@ApiModelProperty(value="상품금액(수출-BROS)")
	private Double itemAmountTot;
	@ApiModelProperty(value="주문자운임(수출-BROS)")
	private Double itemDeliveryFeeTot;
	@ApiModelProperty(value="보험금액(수출-BROS)")
	private Double itemInsuranceTot;
	@ApiModelProperty(value="신고금액(수출-BROS)")
	private Double itemDeclarationTot;
	@ApiModelProperty(value="조정금액(수출-BROS)")
	private Double itemAdjustTot;
	
	@ApiModelProperty(value="배송ID")
	private String deliveryId;
	@ApiModelProperty(value="건별 무상거래 여부")
	private String freeTransYn;
	@ApiModelProperty(value="일련번호")
	private String seqNoId;
	@ApiModelProperty(value="전자상거래Y/N")
	private String ecmYn;
}

package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "OrderDet", description = "전자상거래 Order Detail 리스트")
@Alias("orderDet")
@EqualsAndHashCode(callSuper=false)
public class OrderDet extends ResultList{
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;
	@ApiModelProperty(value="CONSOL_여부")
	private String consolYn;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="하위업체")
	private String custCodeLv2;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="항공/해상")
	private String bound;
	@ApiModelProperty(value="수출/수입")
	private String boundType;
	@ApiModelProperty(value="수입 전용. 배정코드")
	private String assignmentWhCode;
	@ApiModelProperty(value="오더일자")
	private String poDate;
	@ApiModelProperty(value="오더번호")
	private String poNo;
	@ApiModelProperty(value="마스터관리번호")
	private String masterOdJobNo;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="MSN")
	private String msn;
	@ApiModelProperty(value="HSN")
	private String hsn;
	@ApiModelProperty(value="목록신고제출번호")
	private String simpleDocNo;
	@ApiModelProperty(value="배송사_코드")
	private String deliveryCode;
	@ApiModelProperty(value="배송결재_타입")
	private String deliveryPaymentType;
	@ApiModelProperty(value="배송상태")
	private String deliveryStatus;
	@ApiModelProperty(value="배송상태명")
	private String deliveryStatusName;
	@ApiModelProperty(value="배송접수일")
	private String deliveryRecvDate;
	@ApiModelProperty(value="변경배송사")
	private String deliveryNameChanged;
	@ApiModelProperty(value="멀티송장번호")
	private String multiDeliveryNo;
	@ApiModelProperty(value="멀티송장여부")
	private String multiYn;
	@ApiModelProperty(value="비고")
	private String remarks;
	@ApiModelProperty(value="배송일자")
	private String deliveryDate;
	@ApiModelProperty(value="배송비고")
	private String deliveryRemarks;
	@ApiModelProperty(value="과태료_여부")
	private String penaltyYn;
	@ApiModelProperty(value="관세사_명칭")
	private String cbName;
	@ApiModelProperty(value="통관타입")
	private String cdType;
	@ApiModelProperty(value="통관_용도구분")
	private String cdUseType;
	@ApiModelProperty(value="거래구분")
	private String ecmType;	
	@ApiModelProperty(value="통관_ID_구분")
	private String customsIdType;
	@ApiModelProperty(value="통관_ID")
	private String customsId;
	@ApiModelProperty(value="통관부호_검증상태")
	private String customsIdVerify;
	@ApiModelProperty(value="통관부호_오류내역")
	private String customsIdError;
	@ApiModelProperty(value="검사_여부")
	private String inspectionYn;
	@ApiModelProperty(value="선별_여부")
	private String seInspectionYn;
	@ApiModelProperty(value="X-RAY_여부")
	private String xrayYn;
	@ApiModelProperty(value="취하_여부")
	private String dropYn;
	@ApiModelProperty(value="취하사유코드")
	private String dropCode;
	@ApiModelProperty(value="취하사유내용")
	private String dropReason;
	@ApiModelProperty(value="일검_여부")
	private String geInspectionYn;
	@ApiModelProperty(value="검역_타입")
	private String inspectionType;
	@ApiModelProperty(value="반입_일자")
	private String carryinDate;
	@ApiModelProperty(value="반출_일자")
	private String carryoutDate;
	@ApiModelProperty(value="통관여부")
	private String clearanceYn;
	@ApiModelProperty(value="통관완료일")
	private String clearanceDate;
	@ApiModelProperty(value="홀드_여부")
	private String holdYn;
	@ApiModelProperty(value="홀드_사유")
	private String holdReason;
	@ApiModelProperty(value="홀드_처리여부")
	private String holdReleaseYn;
	@ApiModelProperty(value="검사결과_여부")
	private String inspectionResultYn;
	@ApiModelProperty(value="검사결과 통보구분")
	private String inspectionAppGubn;
	@ApiModelProperty(value="검사지정사유")
	private String inspectionAppCode;
	@ApiModelProperty(value="검사지정일시")
	private String inspectionAppDate;
	@ApiModelProperty(value="통관결제_타입(선불후불)")
	private String clearancePaymentType;
	@ApiModelProperty(value="통관접수일")
	private String clearanceReceiptDate;
	@ApiModelProperty(value="미통관코드")
	private String clearanceViolationCode;
	@ApiModelProperty(value="미통관사유")
	private String clearanceViolationReason;
	@ApiModelProperty(value="분할폐기여부")
	private String splitWaistYn;
	@ApiModelProperty(value="지사청구여부")
	private String billToBranchYn;
	@ApiModelProperty(value="홈페이지")
	private String url;
	@ApiModelProperty(value="SMS안내_여부")
	private String smsInformYn;
	@ApiModelProperty(value="SMS처리_여부")
	private String smsInformResYn;
	@ApiModelProperty(value="SMS안내_일시")
	private String smsInformDate;
	@ApiModelProperty(value="SMS처리_일시")
	private String smsInformResDate;
	@ApiModelProperty(value="배송사변경안내여부")
	private String deliveryFeeNoticeYn;
	@ApiModelProperty(value="관부가세안내여부")
	private String clearanceNoticeYn;
	@ApiModelProperty(value="자유무역협정_여부")
	private String ftaYn;
	@ApiModelProperty(value="자유무역협정_비고")
	private String ftaRemarks;
	@ApiModelProperty(value="선명_편명")
	private String vesselFlight;
	@ApiModelProperty(value="출발지코드")
	private String origCode;
	@ApiModelProperty(value="도착지코드")
	private String destCode;
	@ApiModelProperty(value="발송국가")
	private String origCountry;
	@ApiModelProperty(value="목적국가")
	private String destCountry;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="Inner Pieces (실제수량, EDI신고용)")
	private int piecesReal;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="볼륨중량")
	private double cbm;
	@ApiModelProperty(value="허용품목코드")
	private String allowedItemCode;
	@ApiModelProperty(value="품명")
	private String commodityItem;
	@ApiModelProperty(value="통화단위")
	private String currencyCode;
	@ApiModelProperty(value="환율")
	private double exchangeRate;
	@ApiModelProperty(value="원화금액_AMOUNT")
	private double amountLoc;
	@ApiModelProperty(value="외화금액_AMOUNT")
	private double amountCur;
	@ApiModelProperty(value="화주_명칭")
	private String shipperName;
	@ApiModelProperty(value="화주_주소")
	private String shipperAddress;
	@ApiModelProperty(value="화주_전화번호")
	private String shipperTel;
	@ApiModelProperty(value="화주_사업자번호")
	private String shipperBusinessId;
	@ApiModelProperty(value="수하인_명칭")
	private String consigneeName;
	@ApiModelProperty(value="수하인_주소")
	private String consigneeAddress;
	@ApiModelProperty(value="수하인_주소_한글")
	private String consigneeAddressLoc;
	@ApiModelProperty(value="수하인_전화번호")
	private String consigneeTel;
	@ApiModelProperty(value="수하인_우편번호")
	private String consigneeZipcode;
	@ApiModelProperty(value="우편번호_오류")
	private String zipcodeErrorYn;
	@ApiModelProperty(value="수하인_사업자번호")
	private String consigneeBusinessId;
	@ApiModelProperty(value="수하인_명칭_한글")
	private String consigneeNameLoc;
	@ApiModelProperty(value="하기신고_일시")
	private String unloadingDate;
	@ApiModelProperty(value="하기이상_코드")
	private String abnormalCode;
	@ApiModelProperty(value="하기이상_명")
	private String abnormalName;
	@ApiModelProperty(value="하기이상건수")
	private String abnormalityCnt;
	@ApiModelProperty(value="별도관리해제신고_일자")
	private String unmanagementDate;
	@ApiModelProperty(value="적하목록정정_일자")
	private String mfCorrectionDate;
	@ApiModelProperty(value="적하목록정정_내용")
	private String mfCorrectionContents;
	@ApiModelProperty(value="배송지변경여부")
	private String deliveryChangeYn;
	@ApiModelProperty(value="관세")
	private double tariff;	
	@ApiModelProperty(value="관세비고")
	private String tariffRemarks;	
	@ApiModelProperty(value="진행상태")
	private String lastPodStatus;
	@ApiModelProperty(value="면장수량")
	private int licensePieces;	
	@ApiModelProperty(value="면장중량")
	private double licenseGwt;	
	@ApiModelProperty(value="수화인핸드폰번호")
	private String consigneeHp;
	@ApiModelProperty(value="FileShare유무")
	private String fileShare;
	@ApiModelProperty(value="오더입력방법")
	private String inputMethod;
	@ApiModelProperty(value="적하정정구분")
	private String modType;
	@ApiModelProperty(value="적하정정구분_명")
	private String modTypeName;
	@ApiModelProperty(value="반입근거번호")
	private String carryinNo;
	@ApiModelProperty(value="하기이상신고일자")
	private String abnormalDate;
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="입력일자")
	private String registerDateTime;
	@ApiModelProperty(value="EMS환적 여부")
	private String emsYn;
	
	@ApiModelProperty(value="전체건수")
	private int cnt;
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
	@ApiModelProperty(value="목록신고일")
	private String simpleReportDate;
	@ApiModelProperty(value="목록접수일")
	private String simpleResponseDate;
	@ApiModelProperty(value="이행신고일")
	private String simpleDocDateLicense;
	@ApiModelProperty(value="이행신고일시")
	private String simpleReportDateLicense;
	@ApiModelProperty(value="이행신고수신일")
	private String simpleResponseDateLicense;
	@ApiModelProperty(value="이행신고상태")
	private String simpleEdiStatusLicense;
	@ApiModelProperty(value="미반입건수")
	private int noCarryinCnt;
	@ApiModelProperty(value="반입건수")
	private int carryinCnt;
	@ApiModelProperty(value="반출건수")
	private int carryoutCnt;
	@ApiModelProperty(value="통관완료건수")
	private int clearanceCnt;
	@ApiModelProperty(value="미통관건수")
	private int clearanceViolationCnt;
	@ApiModelProperty(value="미통관일")
	private String clearanceViolationDate;
	@ApiModelProperty(value="미배송일")
	private String undeliveryDate;
	@ApiModelProperty(value="미배송일")
	private String undeliveryRemarks;
	@ApiModelProperty(value="집하처리일")
	private String pickupDate;
	@ApiModelProperty(value="유효기간시작일")
	private String customsIdStartDate;
	@ApiModelProperty(value="유효기간시작일")
	private String customsIdEndDate;
	@ApiModelProperty(value="개인통관부호 성(영문)")
	private String consigneeLastName;
	@ApiModelProperty(value="개인통관부호 이름(영문)")
	private String consigneeFirstName;
	
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
	@ApiModelProperty(value="주분번호리스트")
	private String poNoList;
	@ApiModelProperty(value="shipmentBoxId")
	private String shipmentBoxId;
	@ApiModelProperty(value="쿠팡box번호")
	private String returnRequestId;
	@ApiModelProperty(value="반품요청일")
	private String returnReqDate;
	@ApiModelProperty(value="반품배송사")
	private String returnDeliveryCode;
	@ApiModelProperty(value="반품Y/N")
	private String returnYn;
	@ApiModelProperty(value="반품접수일")
	private String returnRegDate;
	@ApiModelProperty(value="반품송장번호")
	private String returnDeliveryNo;
	@ApiModelProperty(value="반품배송출발일")
	private String returnStartDate;
	@ApiModelProperty(value="반품배송완료일")
	private String returnEndDate;
	@ApiModelProperty(value="변경송장번호")
	private String deliveryNoChanged;
	@ApiModelProperty(value="배송비")
	private double deliveryFee;
	@ApiModelProperty(value="멀티화물카운트")
	private int multiCnt;
	@ApiModelProperty(value="멀티화물송장번호")
	private String deliveryNo;
	@ApiModelProperty(value="배송완료일")
	private String deliveryComplDate;
	@ApiModelProperty(value="전자상거래여부")
	private String ecmYn;
	
	/** 오더조회 엑셀 다운로드 시 필터 적용되지 않는 항목들 코드명 따로 조회 */
	@ApiModelProperty(value="통관타입명")
	private String cdTypeName;
	@ApiModelProperty(value="통관_용도구분명")
	private String cdUseTypeName;
	@ApiModelProperty(value="거래구분명")
	private String ecmTypeName;	
	@ApiModelProperty(value="통관_ID_구분명")
	private String customsIdTypeName;
	@ApiModelProperty(value="과태료_여부명")
	private String penaltyYnName;
	@ApiModelProperty(value="전자상거래유형명")
	private String ecmBuyTypeName;

	/** 고객서비스오더관리공통기능 */
	@ApiModelProperty(value="오더접수 POD관리번호")
	private String podRpt;
	@ApiModelProperty(value="발송보류 POD관리번호")
	private String podSendHold;
	@ApiModelProperty(value="반입 POD관리번호")
	private String podCarryIn;
	@ApiModelProperty(value="반입/취소 체크(반입/콘솔)")
	private String chk1;
	@ApiModelProperty(value="반입/취소 체크(반입)")
	private String chk2;
	@ApiModelProperty(value="마지막POD상태명")
	private String lastPodStatusNm;
	@ApiModelProperty(value="주문자명")
	private String orderName;
	@ApiModelProperty(value="배송대행업체부호")
	private String forwardAgentCode;
	
	/** 지적재산권 침해 관련 */
	@ApiModelProperty(value="상표권 침해의심물품")
	private String suspectedTrademark;
	@ApiModelProperty(value="상표권침해물품통관보류")
	private String trademarkInfringing;
	
	/** 쿠팡정산관련*/ 
	@ApiModelProperty(value="SHIPPING METHOD")
	private String shippingMethod;
	@ApiModelProperty(value="CP ORIGIN")
	private String cpOrigin;
	@ApiModelProperty(value="이례여부")
	private String unusualYn;
	@ApiModelProperty(value="이례코드명")
	private String unusualCodeName;
	@ApiModelProperty(value="송장번호")
	private String dsetDeliveryNo;
	
	@ApiModelProperty(value="일련번호")
	private String seqNoId;
	
	/** 통관엑셀용 */
	@ApiModelProperty(value="hsCode")
	private String hsCode;
	@ApiModelProperty(value="단가")
	private double unitPrice;
	@ApiModelProperty(value="총금액")
	private double totalAmount;
	@ApiModelProperty(value="INV VALUE")
	private double invValue;
	@ApiModelProperty(value="DUTY")
	private String duty;
	
}

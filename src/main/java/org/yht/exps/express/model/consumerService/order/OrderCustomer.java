package org.yht.exps.express.model.consumerService.order;

import org.apache.ibatis.type.Alias;
import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "OrderCustomer", description = "전자상거래  대리점/화주 Order Detail 리스트")
@Alias("orderCustomer")
public class OrderCustomer {
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	@ApiModelProperty(value="도입사코드 5자리 + 국가 2자리")
	private String entBusinessCode;
	@ApiModelProperty(value="상위업체_코드")
	private String upperCusCode;
	@ApiModelProperty(value="상위업체_이름")
	private String upperCusName;
	@ApiModelProperty(value="하위업체_코드")
	private String lowCusCode;
	@ApiModelProperty(value="하위업체_이름")
	private String lowCusName;
	
	@ApiModelProperty(value="SOLUTION")
	private String solution;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="오더일자")
	private String poDate;
	@ApiModelProperty(value="진행상태")
	private String lastPodStatus;
	@ApiModelProperty(value="진행상태")
	private String status;
	@ApiModelProperty(value="P/O번호")
	private String poNo;
	@ApiModelProperty(value="중개사업자1")
	private String venderId1;
	@ApiModelProperty(value="중개사업자2")
	private String venderId2;
	@ApiModelProperty(value="BL구분_MH")
	private String blTypeMh;
	@ApiModelProperty(value="E:Export, R:Re-Shippment(환적화물) I:Import, T:Trans-Shippment(통과화물)")
	private String blTypeIert;
	@ApiModelProperty(value="CONSOL_여부")
	private String consolYn;
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
	@ApiModelProperty(value="운송사코드")
	private String trCode;
	@ApiModelProperty(value="운송사_명칭")
	private String trName;
	@ApiModelProperty(value="배송사_코드")
	private String deliveryCode;
	@ApiModelProperty(value="배송사_명")
	private String deliveryName;
	@ApiModelProperty(value="택배번호")
	private String deliveryNo;
	@ApiModelProperty(value="배송요청")
	private String deliveryNote;
	@ApiModelProperty(value="배송결재_타입")
	private String deliveryPaymentType;
	@ApiModelProperty(value="배송비")
	private double deliveryFee;
	@ApiModelProperty(value="배송비_도서")
	private double deliveryAddedFee;
	@ApiModelProperty(value="배송상태")
	private String deliveryStatus;
	@ApiModelProperty(value="배송접수일")
	private String deliveryRecvDate;
	@ApiModelProperty(value="변경배송사_코드")
	private String deliveryCodeChanged;
	@ApiModelProperty(value="변경배송번호")
	private String deliveryNoChanged;
	@ApiModelProperty(value="변경배송사이름")
	private String deliveryNameChanged;
	@ApiModelProperty(value="변경배송주소")
	private String deliveryAddressChanged;
	@ApiModelProperty(value="변경배송사유")
	private String deliveryReasonChanged;
	@ApiModelProperty(value="구매대행등록부호")
	private String agentRegNo;
	@ApiModelProperty(value="멀티송장번호")
	private String multiDeliveryNo;
	@ApiModelProperty(value="배송일자")
	private String deliveryDate;
	@ApiModelProperty(value="배송비고")
	private String deliveryRemarks;
	@ApiModelProperty(value="배송비착불안내_여부")
	private String deliveryFeeNoticeYn;
	@ApiModelProperty(value="배송비착불안내_일시")
	private String deliveryFeeNoticeDate;
	@ApiModelProperty(value="과태료_여부")
	private String penaltyYn;
	@ApiModelProperty(value="관세사_코드")
	private String cbCode;
	@ApiModelProperty(value="관세사_명칭")
	private String cbName;
	@ApiModelProperty(value="Delivery To : 창고 업체")
	private String whCode;
	@ApiModelProperty(value="창고업체_명칭")
	private String whName;
	@ApiModelProperty(value="통관타입")
	private String cdType;
	@ApiModelProperty(value="통관_용도구분")
	private String cdUseType;
	@ApiModelProperty(value="거래구분")
	private String ecmType;
	@ApiModelProperty(value="전자상거래_용도구분")
	private String ecmUseType;
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
	@ApiModelProperty(value="검역_여부")
	private String quarantineYn;
	@ApiModelProperty(value="통관_여부")
	private String clearanceYn;
	@ApiModelProperty(value="멀티_여부")
	private String multiYn;
	@ApiModelProperty(value="반입_일자")
	private String carryinDate;
	@ApiModelProperty(value="반출_일자")
	private String carryoutDate;
	@ApiModelProperty(value="통관완료일")
	private String clearanceDate;
	@ApiModelProperty(value="배송완료_여부")
	private String deliveryYn;
	@ApiModelProperty(value="홀드_여부")
	private String holdYn;
	@ApiModelProperty(value="홀드_코드")
	private String holdCode;
	@ApiModelProperty(value="홀드_사유")
	private String holdReason;
	@ApiModelProperty(value="홀드_비고")
	private String holdRemarks;
	@ApiModelProperty(value="홀드처리_여부")
	private String holdReleaseYn;
	@ApiModelProperty(value="홀드처리_담당자")
	private String holdReleaseAttn;
	@ApiModelProperty(value="홀드처리_일자")
	private String holdReleaseDate;
	@ApiModelProperty(value="검사결과_여부")
	private String inspectionResultYn;
	@ApiModelProperty(value="통관결제_타입(선불후불)")
	private String clearancePaymentType;
	@ApiModelProperty(value="통관접수일")
	private String clearanceReceiptDate;
	@ApiModelProperty(value="미통관코드")
	private String clearanceViolationCode;
	@ApiModelProperty(value="미통관사유")
	private String clearanceViolationReason;
	@ApiModelProperty(value="식품신고여부")
	private String foodYn;
	@ApiModelProperty(value="과세환율")
	private double taxExchangeRate;
	@ApiModelProperty(value="과세운임")
	private double taxFreight;
	@ApiModelProperty(value="CIF_원화")
	private double cifLocal;
	@ApiModelProperty(value="관세_비고")
	private String tariffRemarks;
	@ApiModelProperty(value="특소세")
	private double specialConsumtionTax;
	@ApiModelProperty(value="주세")
	private double liquorTax;
	@ApiModelProperty(value="교육세")
	private double educationTax;
	@ApiModelProperty(value="농특세")
	private double ruralSpecialTax;
	@ApiModelProperty(value="식품신고수수료")
	private double foodFee;
	@ApiModelProperty(value="세금대납여부")
	private String taxPayForYn;
	@ApiModelProperty(value="청구여부")
	private String invoiceYn;
	@ApiModelProperty(value="분할폐기여부")
	private String splitWaistYn;
	@ApiModelProperty(value="지사청구여부")
	private String billToBranchYn;
	@ApiModelProperty(value="홈페이지")
	private String url;
	@ApiModelProperty(value="SMS안내_여부")
	private String smsInformYn;
	@ApiModelProperty(value="SMS안내_일시")
	private String smsInformDate;
	@ApiModelProperty(value="자유무역협정_여부")
	private String ftaYn;
	@ApiModelProperty(value="자유무역협정_비고")
	private String ftaRemarks;
	@ApiModelProperty(value="항공사_선사코드")
	private String lineCode;
	@ApiModelProperty(value="항공사_선사명칭")
	private String lineName;
	@ApiModelProperty(value="선명_편명")
	private String vesselFlight;
	@ApiModelProperty(value="항차")
	private String voyage;
	@ApiModelProperty(value="출발지코드")
	private String origCode;
	@ApiModelProperty(value="출발지명")
	private String origName;
	@ApiModelProperty(value="도착지코드")
	private String destCode;
	@ApiModelProperty(value="도착지명")
	private String destName;
	@ApiModelProperty(value="발송국가")
	private String origCountry;
	@ApiModelProperty(value="목적국가")
	private String destCountry;
	@ApiModelProperty(value="목적항")
	private String destPort;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="출항시간")
	private String obTime;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="입항시간")
	private String arTime;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="Inner Pieces (실제수량, EDI신고용)")
	private int piecesReal;
	@ApiModelProperty(value="단위_수량")
	private int unitPieces;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="총중량Lbs")
	private double gwtLbs;
	@ApiModelProperty(value="볼륨중량")
	private double cbm;
	@ApiModelProperty(value="화물규격_가로")
	private int dimWidth;
	@ApiModelProperty(value="화물규격_길이")
	private int dimLength;
	@ApiModelProperty(value="화물규격_높이")
	private int dimHeight;
	@ApiModelProperty(value="화물규격_볼륨")
	private double dimVolumn;
	@ApiModelProperty(value="중량")
	private double vwt;
	@ApiModelProperty(value="대표품목")
	private String commodityItem;
	@ApiModelProperty(value="허용품목코드")
	private String allowedItemCode;
	@ApiModelProperty(value="통화단위")
	private String currencyCode;
	@ApiModelProperty(value="환율")
	private double exchangeRate;
	@ApiModelProperty(value="원화금액_AMOUNT")
	private double amountLoc;
	@ApiModelProperty(value="외화금액_AMOUNT")
	private double amountCur;
	@ApiModelProperty(value="보험_여부")
	private String insuranceYn;
	@ApiModelProperty(value="보험료")
	private double insuranceAmount;
	@ApiModelProperty(value="액체_여부")
	private String liquidYn;
	@ApiModelProperty(value="세관코드")
	private String customsCode;
	@ApiModelProperty(value="세관과코드")
	private String customsDptCode;
	@ApiModelProperty(value="포워더_코드")
	private String fwrdCode;
	@ApiModelProperty(value="포워더_명칭")
	private String fwrdName;
	@ApiModelProperty(value="비고사항")
	private String remarks;
	@ApiModelProperty(value="화주코드")
	private String shipperCode;
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
	@ApiModelProperty(value="화주_우편번호")
	private String shipperZipcode;
	@ApiModelProperty(value="화주_국가코드")
	private String shipperCountryCode;
	@ApiModelProperty(value="화주STATE_코드")
	private String shipperStateCode;
	@ApiModelProperty(value="화주PLACE_코드")
	private String shipperPlaceCode;
	@ApiModelProperty(value="화주PLACE")
	private String shipperPlace;
	@ApiModelProperty(value="화주STREET")
	private String shipperStreet;
	@ApiModelProperty(value="화주_사업자번호")
	private String shipperBusinessId;
	@ApiModelProperty(value="화주_담당자_ID")
	private String shipperAttnId;
	@ApiModelProperty(value="화주_담당자")
	private String shipperAttn;
	@ApiModelProperty(value="화주_팩스번호")
	private String shipperFax;
	@ApiModelProperty(value="수하인_코드")
	private String consigneeCode;
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
	@ApiModelProperty(value="수하인_국가코드")
	private String consigneeCountryCode;
	@ApiModelProperty(value="수하인STATE_코드")
	private String consigneeStateCode;
	@ApiModelProperty(value="수하인PLACE_코드")
	private String consigneePlaceCode;
	@ApiModelProperty(value="수하인PLACE")
	private String consigneePlace;
	@ApiModelProperty(value="수하인STREET")
	private String consigneeStreet;
	@ApiModelProperty(value="수하인_사업자번호")
	private String consigneeBusinessId;
	@ApiModelProperty(value="수하인_담당자_ID")
	private String consigneeAttnId;
	@ApiModelProperty(value="수하인_담당자")
	private String consigneeAttn;
	@ApiModelProperty(value="수하인_팩스번호")
	private String consigneeFax;
	@ApiModelProperty(value="수하인_명칭_한글")
	private String consigneeNameLoc;
	@ApiModelProperty(value="수하인_주소_한글")
	private String consigneeAddressLoc;
	@ApiModelProperty(value="항공/해운수입 하기(하선)장소코드 (장치장 또는 보세창고코드)")
	private String discWhCode;
	@ApiModelProperty(value="항공수입 기재한다. SD, ST, GD,GT : 항공은 기초의 하기물품코드를 사용한다. 해운수입 기재한다. 해운은 기초의 하선물품코드를 사용한다.")
	private String discItemCode;
	@ApiModelProperty(value="항공수입 전용. 배정창고코드	@ApiModelProperty(value='배정장소 하기신고시 사용된다. 항공수입:보세창고 중 ETC2='AIR' 해운수입:보세창고 중 ETC2<>'AIR'")
	private String assignmentWhCode;
	@ApiModelProperty(value="검역구분_코드")
	private String inspectionType;
	@ApiModelProperty(value="통관목록_신고일시")
	private String simpleDocDate;
	@ApiModelProperty(value="적하목록_신고일시")
	private String mfDate;
	@ApiModelProperty(value="적하목록_송신자")
	private String mfUserId;
	@ApiModelProperty(value="하기신고_일시")
	private String unloadingDate;
	@ApiModelProperty(value="목록신고_일시")
	private String simpleReportDate;
	@ApiModelProperty(value="목록신고_송신자")
	private String simpleReportUserId;
	@ApiModelProperty(value="목록수신_일시")
	private String simpleResponseDate;
	@ApiModelProperty(value="결과통보_일시")
	private String simpleResultDate;
	@ApiModelProperty(value="하기이상보고_신고일시")
	private String abnormalitiesReportDate;
	@ApiModelProperty(value="하기이상_코드")
	private String abnormalityCode;
	@ApiModelProperty(value="하기이상_수량")
	private int abnormalityPieces;
	@ApiModelProperty(value="하기이상_중량")
	private double abnormalityGwt;
	@ApiModelProperty(value="하기이상_사고개수")
	private int abnormalityCnt;
	@ApiModelProperty(value="별도관리해제신고_일자")
	private String unmanagementDate;
	@ApiModelProperty(value="적하목록정정_일자")
	private String mfCorrectionDate;
	@ApiModelProperty(value="적하목록정정_내용")
	private String mfCorrectionContents;
	@ApiModelProperty(value="관우회_송신일자")
	private String cfaDate;
	@ApiModelProperty(value="관우회_송신자")
	private String cfaUserId;
	@ApiModelProperty(value="목록선별자_아이디")
	private String selectorUserId;
	@ApiModelProperty(value="선별물품확인자_아이디")
	private String confirmerUserId;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력자명")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="수정자명")
	private String updateUserName;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	@ApiModelProperty(value="insert/update구분")
	private String action;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="wtDecimalAir")
	private String wtDecimalAir;
	
	@ApiModelProperty(value="출력")
	private String printYn;
	@ApiModelProperty(value="출력일자")
	private String printDate;
	
	@ApiModelProperty(value="거래처코드1")
	private String custCodeLv1;
	@ApiModelProperty(value="거래처코드2")
	private String custCodeLv2;
	@ApiModelProperty(value="거래처코드3")
	private String custCodeLv3;
	@ApiModelProperty(value="거래처코드4")
	private String custCodeLv4;
	@ApiModelProperty(value="거래처코드5")
	private String custCodeLv5;
	@ApiModelProperty(value="거래처코드6")
	private String custCodeLv6;
	@ApiModelProperty(value="거래처코드7")
	private String custCodeLv7;
	
	@ApiModelProperty(value="카운트sum")
	private String sumCount;
	@ApiModelProperty(value="수량sum")
	private String sumPieces;
	@ApiModelProperty(value="중량sum")
	private String sumGwt;
	@ApiModelProperty(value="볼륨sum")
	private String sumCbm;
	@ApiModelProperty(value="usdsum")
	private String sumUsd;
	@ApiModelProperty(value="chk1")
	private String chk1;
	@ApiModelProperty(value="chk2")
	private String chk2;
	@ApiModelProperty(value="lastPodStatusNm")
	private String lastPodStatusNm;
	@ApiModelProperty(value="오더접수 POD관리번호")
	private String podRpt;
	@ApiModelProperty(value="발송보류 POD관리번호")
	private String podSendHold;
	@ApiModelProperty(value="반입 POD관리번호")
	private String podCarryIn;
	
	@ApiModelProperty(value="Order Item List")
	private List<OrderCustomerItem> itemList;
	
	@ApiModelProperty(value="Order Sub List")
	private List<OrderCustomerItem> subList;
	
	@ApiModelProperty(value="Order Sub Delete List")
	private List<OrderCustomerItem> subDelList;	
	/**
	 * TODO 진행상태, 면장수량, 면장중량, 검증결과(개인), SMS처리
	 */
	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;
	@ApiModelProperty(value="파일공유여부")
	private String fileShare;
}

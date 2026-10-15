package org.yht.exps.express.model.eCommerce.order;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.CommonModel;
import org.yht.exps.express.model.consumerService.order.OrderCustomerItem;
import org.yht.exps.express.model.systemMng.Alarm;
import org.yht.util.Util;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@ApiModel(value = "Order", description = "전자상거래 Order")
@Alias("order")
public class Order implements Cloneable{
	/** 
	 * 엑셀업로드 처리내역에서 저장시 오류로 int => double로 변경    문제발생시 해당부분 체크
	 *  엑셀에 수량(int)을 1.0으로 입력하고 해당오더의 다른부분에서 검증에 걸렸을경우 처리내역데이터는 수량(String) 1.0으로 처리되고
	 *  저장시 JSON parse error: Cannot deserialize value of type `int` from String "1.0": not a valid `int` value 오류발생
	 */
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="COPY시 원본 하우스 진행상태(LAST_POD_STATUS)")
	private String copyLastPodStatus;
	@ApiModelProperty(value="암호화여부")
	private String cryptographyYn;
	@ApiModelProperty(value="비엘관리번호")
	private String oblJobNo;
	@ApiModelProperty(value="ISSUING_CARRIER")
	private String issuingCarriers;
	@ApiModelProperty(value="ISSUING_CARRIER")
	private String bmsInPlanYn;
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	@ApiModelProperty(value="도입사코드 5자리 + 국가 2자리")
	private String entBusinessCode;
	@ApiModelProperty(value="AGENT_코드")
	private String agentCode;
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
	@ApiModelProperty(value="주문번호")
	private String orderNo;
	@ApiModelProperty(value="목록신고제출번호")
	private String simpleDocNo;
	@ApiModelProperty(value="목록신고제출일련번호")
	private String simpleDocNoSeqNo;
	@ApiModelProperty(value="통관목록전송여부")
	private String simpleEdiSendYn;
	@ApiModelProperty(value="목록신고제출STOCK생성번호")
	private String SimpleDocNoStockNo;
	@ApiModelProperty(value="목록신고제출STOCK생성번호-서류Y")
	private String simpleDocNoDocumentY;
	@ApiModelProperty(value="목록신고제출STOCK생성번호-서류N")
	private String simpleDocNoDocumentN;
	@ApiModelProperty(value="수정전 목록신고제출번호")
	private String oldSimpleDocNo;
	@ApiModelProperty(value="목록신고전송상태")
	private String simpleEdiStatus;
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
	@ApiModelProperty(value="발송수정여부")
	private String sendModifyYn;
	@ApiModelProperty(value="운송사코드")
	private String trCode;
	@ApiModelProperty(value="운송사_명칭")
	private String trName;
	@ApiModelProperty(value="배송사코드")
	private String deliveryCode;
	@ApiModelProperty(value="배송사명")
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
	@ApiModelProperty(value="통관상태")
	private String clearanceStatus;
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
	@ApiModelProperty(value="AMS_여부")
	private String amsYn;
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
	@ApiModelProperty(value="통관_여부")
	private String clearanceYn;
	@ApiModelProperty(value="멀티_여부")
	private String multiYn;
	@ApiModelProperty(value="반입근거번호")
	private String carryinNo;
	@ApiModelProperty(value="반입_일자")
	private String carryinDate;
	@ApiModelProperty(value="반출_일자")
	private String carryoutDate;
	@ApiModelProperty(value="통관완료일")
	private String clearanceDate;
	@ApiModelProperty(value="배송완료_여부")
	private String deliveryYn;
	@ApiModelProperty(value="홀드_코드")
	private String holdCode;
	@ApiModelProperty(value="홀드_사유")
	private String holdReason;
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
	@ApiModelProperty(value="미통관일시")
	private String clearanceViolationDate;
	@ApiModelProperty(value="식품신고여부")
	private String foodYn;
	@ApiModelProperty(value="과세환율")
	private double taxExchangeRate;
	@ApiModelProperty(value="과세운임")
	private double taxFreight;
	@ApiModelProperty(value="CIF_원화")
	private double cifLocal;
	@ApiModelProperty(value="관세")
	private double tariff;
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
	@ApiModelProperty(value="SMS처리_여부")
	private String smsInformResYn;
	@ApiModelProperty(value="SMS안내_일시")
	private String smsInformDate;
	@ApiModelProperty(value="SMS처리_일시")
	private String smsInformResDate;
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
	@ApiModelProperty(value="선박호출부호")
	private String vesselSign;
	@ApiModelProperty(value="항차")
	private String voyage;
	@ApiModelProperty(value="항차(마스터사용컬럼명)")
	private String vesselVoyage;
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
//	private int pieces;
	private double pieces;
	@ApiModelProperty(value="Inner Pieces (실제수량, EDI신고용)")
//	private int piecesReal;
	private double piecesReal;
	@ApiModelProperty(value="단위_수량")
	private String unitPieces;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="총중량Lbs")
	private double gwtLbs;
	@ApiModelProperty(value="용적")
	private double cbm;
	@ApiModelProperty(value="볼륨중량")
	private double vwt;
	@ApiModelProperty(value="화물규격_가로")
	private double dimWidth;
	@ApiModelProperty(value="화물규격_길이")
	private double dimLength;
	@ApiModelProperty(value="화물규격_높이")
	private double dimHeight;
	@ApiModelProperty(value="화물규격_볼륨")
	private double dimVolumn;
	@ApiModelProperty(value="대표품목")
	private String commodityItem;
	@ApiModelProperty(value="대표품목(Local)")
	private String commodityItemLoc;
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
	@ApiModelProperty(value="USD금액_AMOUNT")
	private double amountUsd;
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
	@ApiModelProperty(value="화주_핸드폰번호")
	private String shipperHp;
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
	@ApiModelProperty(value="수하인_주소_추가")
	private String consigneeAddressAdd;
	@ApiModelProperty(value="수하인_핸드폰")
	private String consigneeHp;
	@ApiModelProperty(value="수하인_전화번호")
	private String consigneeTel;
	@ApiModelProperty(value="수하인_우편번호")
	private String consigneeZipcode;
	@ApiModelProperty(value="우편번호_오류")
	private String zipcodeErrorYn;
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
	@ApiModelProperty(value="수하인_주소_한글_추가")
	private String consigneeAddressLocAdd;
	@ApiModelProperty(value="항공/해운수입 하기(하선)장소코드 (장치장 또는 보세창고코드)")
	private String discWhCode;
	@ApiModelProperty(value="항공수입 기재한다. SD, ST, GD,GT : 항공은 기초의 하기물품코드를 사용한다. 해운수입 기재한다. 해운은 기초의 하선물품코드를 사용한다.")
	private String discItemCode;
	@ApiModelProperty(value="수입 전용. 배정코드")
	private String assignmentWhCode;
	@ApiModelProperty(value="수입 전용. 배정부호")
	private String assignmentCode;
	@ApiModelProperty(value="검역구분_코드")
	private String inspectionType;
	@ApiModelProperty(value="통관목록_신고일시")
	private String simpleDocDate;
	@ApiModelProperty(value="통관목록_신고시간")
	private String simpleDocTime;
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
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	@ApiModelProperty(value="대리점코드")
	private String custCodeLv1;
	@ApiModelProperty(value="대리점명")
	private String custNameLv1;
	@ApiModelProperty(value="화주코드2")
	private String custCodeLv2;
	@ApiModelProperty(value="화주코드3")
	private String custCodeLv3;
	@ApiModelProperty(value="화주코드4")
	private String custCodeLv4;
	@ApiModelProperty(value="화주코드5")
	private String custCodeLv5;
	@ApiModelProperty(value="화주코드6")
	private String custCodeLv6;
	@ApiModelProperty(value="화주코드7")
	private String custCodeLv7;
	@ApiModelProperty(value="마감여부")
	private String finishYn;
	@ApiModelProperty(value="마감일자")
	private String finishDate;
	@ApiModelProperty(value="마감자코드")
	private String finishUserId;
	@ApiModelProperty(value="관세청정보관리번호")
	private String ediMailboxSeqNo;
	@ApiModelProperty(value="특송부호")
	private String ediExpressSign;
	@ApiModelProperty(value="마스터오더관리번호")
	private String masterOdJobNo;
	@ApiModelProperty(value="관세사정보알림톡여부")
	private String clearanceNoticeYn;
	@ApiModelProperty(value="하기이상보고_신고일시")
	private String sendDate;
	@ApiModelProperty(value="현재진행상태코드")
	private String lastPodStatus;
	@ApiModelProperty(value="현재진행상태일시")
	private String lastPodDate;
	@ApiModelProperty(value="현재진행상태명")
	private String lastPodStatusName;
	@ApiModelProperty(value="운임조건")
	private String incoterms;
	@ApiModelProperty(value="반품여부")
	private String returnYn;
	@ApiModelProperty(value="반품송장번호")
	private String returnDeliveryNo;
	@ApiModelProperty(value="전자상거래유형")
	private String ecmBuyType;
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
	@ApiModelProperty(value="물품소재지부호(우편번호)")
	private String goodsLocCode;
	@ApiModelProperty(value="물품소재지주소")
	private String goodsLocAddr;
	@ApiModelProperty(value="장치장소명")
	private String bwhName;
	@ApiModelProperty(value="물품소재지검사여부")
	private String goodsLocInspectYn;
	@ApiModelProperty(value="물품소재지검사사유")
	private String goodsLocInspectReason;
	@ApiModelProperty(value="주문번호 리스트")
	private String poNoList;
	@ApiModelProperty(value="집하처리일")
	private String pickupDate;
	@ApiModelProperty(value="미배송일")
	private String undeliveryDate;
	@ApiModelProperty(value="미배송사유")
	private String undeliveryRemarks;
	@ApiModelProperty(value="EMS환적여부")
	private String emsYn;
	@ApiModelProperty(value="유효기간시작일")
	private String customsIdStartDate;
	@ApiModelProperty(value="유효기간만료일")
	private String customsIdEndDate;
	@ApiModelProperty(value="개인통관부호 성(영문)")
	private String consigneeLastName;
	@ApiModelProperty(value="개인통관부호 이름(영문)")
	private String consigneeFirstName;
	@ApiModelProperty(value="멀티화물카운트")
	private int multiCnt;

	/** EMS 환적 전용 **/
	@ApiModelProperty(value="오더번호")
	private String emsPoNo;
	@ApiModelProperty(value="우편물구분")
	private String mailType;
	@ApiModelProperty(value="상품구분")
	private String itemType;
	@ApiModelProperty(value="보험이용여부")
	private String emsInsuranceYn;
	@ApiModelProperty(value="환적계약구분코드")
	private String contractCode;
	@ApiModelProperty(value="우편물 포장상자 가로")
	private double emsDimWidth;
	@ApiModelProperty(value="우편물 포장상자 세로")
	private double emsDimLength;
	@ApiModelProperty(value="우편물 포장상자 높이")
	private double emsDimHeight;
	@ApiModelProperty(value="보험 가입금액")
	private double emsInsuranceAmount;
	@ApiModelProperty(value="총중량")
	private double emsGwt;
	@ApiModelProperty(value="우편요금")
	private double emsDeliveryAmount;
	@ApiModelProperty(value="부피중량 적용제외")
	private String volumnExcludeYn;
	@ApiModelProperty(value="받는사람 상호")
	private String emsConsigneeName;
	@ApiModelProperty(value="주소 상세")
	private String emsConsigneeAddressA;
	@ApiModelProperty(value="시/군")
	private String emsConsigneeAddressB;
	@ApiModelProperty(value="주/도")
	private String emsConsigneeAddressC;
	@ApiModelProperty(value="전화번호 국가")
	private String consigneeTelA;
	@ApiModelProperty(value="전화번호 지역")
	private String consigneeTelB;
	@ApiModelProperty(value="전화번호 국")
	private String consigneeTelC;
	@ApiModelProperty(value="전화번호 번호")
	private String consigneeTelD;
	@ApiModelProperty(value="전화번호")
	private String emsConsigneTel;
	@ApiModelProperty(value="받는사람 국가")
	private String emsDestCountry;
	@ApiModelProperty(value="받는사람 국가명")
	private String emsDestCountryName;
	@ApiModelProperty(value="받는사람 이메일")
	private String emsConsigneeEmail;
	@ApiModelProperty(value="보내는사람 이름")
	private String emsShipperName;
	@ApiModelProperty(value="보내는사람 주소 상세")
	private String emsShipperAddressA;
	@ApiModelProperty(value="보내는사람 주소 시/군")
	private String emsShipperAddressB;
	@ApiModelProperty(value="보내는사람 주소 주/도")
	private String emsShipperAddressC;
	@ApiModelProperty(value="보내는사람 전화번호 국가")
	private String shipperTelA;
	@ApiModelProperty(value="보내는사람 전화번호 지역")
	private String shipperTelB;
	@ApiModelProperty(value="보내는사람 전화번호 국")
	private String shipperTelC;
	@ApiModelProperty(value="보내는사람 전화번호 번호")
	private String shipperTelD;
	@ApiModelProperty(value="보내는사람 국가")
	private String emsOrigCountry;
	@ApiModelProperty(value="보내는사람 국가명")
	private String emsOrigCountryName;
	@ApiModelProperty(value="보내는사람 이메일")
	private String emsShipperEmail;
	@ApiModelProperty(value="부가세 식별번호")
	private String vatIdtNo;
	@ApiModelProperty(value="묶음번호")
	private String emsBundleNo;
	@ApiModelProperty(value="국제우편물종류")
	private String interEmailType;
	@ApiModelProperty(value="요금납부구분")
	private String payChargeType;
	@ApiModelProperty(value="승인번호")
	private String emsApprovalNo;
	@ApiModelProperty(value="보내는사람 전화번호")
	private String emsShipperTel;
	@ApiModelProperty(value="받는사람 전화번호")
	private String emsConsigneeTel;
	@ApiModelProperty(value="받는사람 우편번호")
	private String emsConsigneeZipcode;
	@ApiModelProperty(value="보내는사람 우편번호")
	private String emsShipperZipcode;
	@ApiModelProperty(value="delivery center code")
	private String deliveryCenterCode;
	

	// DKX관련 추가
    @ApiModelProperty(value="배송대행업자 부호")
	private String forwardAgentCode;
    @ApiModelProperty(value="배송대행업자 명")
    private String forwardAgentName;
    @ApiModelProperty(value="주문완료일자")
    private String orderEndDate;
    @ApiModelProperty(value="주문완료시간")
    private String orderEndtime;
    @ApiModelProperty(value="주문자명")
	private String orderName;
    @ApiModelProperty(value="주문자 전화번호")
	private String orderTel;
    @ApiModelProperty(value="주문자 계정")
	private String orderAccount;
    @ApiModelProperty(value="수하인주소2")
    private String consigneeAddress2;
    @ApiModelProperty(value="수하인주소LOC_2")
    private String consigneeAddressLoc2;
    @ApiModelProperty(value="전자상거래여부")
    private String ecmYn;
    @ApiModelProperty(value="전자상거래여부체크")
    private String ecmYnChk;

	
	
	/** 엑셀업로드용 */
	@ApiModelProperty(value="대리점코드")
	private String customerCode;
	@ApiModelProperty(value="중량단위")
	private String gwtType;
	
	
	@ApiModelProperty(value="로그인 거래처 코드")
	private String loginCustomerCode;

	@ApiModelProperty(value="Order 세부품목 List")
	private List<OrderItem> itemList;
	@ApiModelProperty(value="Order 별도관리물품내역 List")
	private List<OrderSpecialItem> specialItemList;
	@ApiModelProperty(value="Order 하기이상결과보고 List")
	private List<OrderAbnormal> abnormalList;
	@ApiModelProperty(value="Order 멀티(중량)화물 List")
	private List<OrderMulti> multiList;
	@ApiModelProperty(value="Order Packing List")
	private List<OrderPacking> packingList;
	@ApiModelProperty(value="Order 홀드 List")
	private List<OrderHold> holdList;
	@ApiModelProperty(value="Order 주문번호 List")
	private List<String> poInfoList;
	@ApiModelProperty(value="Order 알림톡 List")
	private List<Alarm> alarmList;
	@ApiModelProperty(value="Order 추가비용 List")
	private List<OrderCost> costList;
	@ApiModelProperty(value="Order POD List")
	private List<OrderPod> podList;
	@ApiModelProperty(value="Order 고객관리 List")
	private List<OrderCs> csList;
	@ApiModelProperty(value="Order 컨테이너정보 List")
	private List<OrderContainer> containerList;
	@ApiModelProperty(value="Order 면장정보 List")
	private List<OrderLicense> licenseList;
	
	/** 저장 */
	private CommonModel common;
	@ApiModelProperty(value="insert/update구분")
	private String action;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="작업자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="도입사계약일자")
	private String contractDate;
	@ApiModelProperty(value="사용자권한")
	private String userAuth;
	
	/** 조회 */
	@ApiModelProperty(value="하기장소명")
	private String discWhName;
	@ApiModelProperty(value="배정코드명")
	private String assignmentWhName;
	@ApiModelProperty(value="입력자레벨")
	private String registerUserLevel;
	@ApiModelProperty(value="반입여부")
	private String carryinYn;
	@ApiModelProperty(value="반출여부")
	private String carryoutYn;
	@ApiModelProperty(value="정산여부 업체코드")
	private String invoiceYnCustCode;
	@ApiModelProperty(value="정산여부 업체명")
	private String invoiceYnCustName;
	@ApiModelProperty(value="하우스 항공/해운수입 하기(하선)장소코드 (장치장 또는 보세창고코드)")
	private String hblDiscWhCode;
	@ApiModelProperty(value="하우스 항공/해운수입 하기(하선)장소코드 (장치장 또는 보세창고코드)")
	private String hblDiscWhCodeName;
	
	public Order(){
		this.consolYn = "N";
		this.penaltyYn = "N";
		this.deliveryFeeNoticeYn = "N";
		this.inspectionYn = "N";
		this.seInspectionYn = "N";
		this.xrayYn = "N";
		this.dropYn = "N";
		this.geInspectionYn = "N";
//		this.inspectionType = "N";
		this.clearanceYn = "N";
		this.multiYn = "N";
		this.deliveryYn = "N";
		this.inspectionResultYn = "N";
		this.foodYn = "N";
		this.taxPayForYn = "N";
		this.invoiceYn = "N";
		this.splitWaistYn = "N";
		this.billToBranchYn = "N";
		this.smsInformYn = "N";
		this.ftaYn = "N";
		this.insuranceYn = "N";
		this.liquidYn = "N";
		this.finishYn = "N";
	}
	
	public Order clone() throws CloneNotSupportedException{
		Order vo = null;
		try {
			vo = (Order) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error(Util.getPrintStacTraceString(e));
			throw e;
		}
		return vo;
	}
	
//	//깊은 복사를 위한 메소드
//	public Mf clone() {
//		Mf vo = null;
//		try {
//			vo = (Mf) super.clone();
//			// 데이터 저장과 상관 없는 항목은 null 처리 한다.
//			vo.setContainerList(null);
//			vo.setItemList(null);
//			vo.setLicenseList(null);
//			vo.setSpecialItemList(null);
//			vo.setAggregation(null);
//			vo.setCommon(null);
//		} catch (CloneNotSupportedException e) {
//			log.error(Util.getPrintStacTraceString(e));
//		}
//		return vo;		
//	}

	
	
	/** 오더공통저장에 사용하기위해 org.yht.exps.express.model.ecm.order.OrderCustomer.java에만 있는 내용 추가 */
	@ApiModelProperty(value="상위업체_코드")
	private String upperCusCode;
	@ApiModelProperty(value="상위업체_이름")
	private String upperCusName;
	@ApiModelProperty(value="하위업체_코드")
	private String lowCusCode;
	@ApiModelProperty(value="하위업체_이름")
	private String lowCusName;
	@ApiModelProperty(value="진행상태")
	private String status;
	@ApiModelProperty(value="검역_여부")
	private String quarantineYn;
	@ApiModelProperty(value="홀드_여부")
	private String holdYn;
	@ApiModelProperty(value="홀드_비고")
	private String holdRemarks;
	@ApiModelProperty(value="입력자명")
	private String registerUserName;
	@ApiModelProperty(value="수정자명")
	private String updateUserName;
	@ApiModelProperty(value="wtDecimalAir")
	private String wtDecimalAir;
	@ApiModelProperty(value="출력")
	private String printYn;
	@ApiModelProperty(value="출력일자")
	private String printDate;
	@ApiModelProperty(value="카운트sum")
	private int sumCount;
	@ApiModelProperty(value="수량sum")
	private int sumPieces;
	@ApiModelProperty(value="중량sum")
	private double sumGwt;
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
	@ApiModelProperty(value="Order Sub List")
	private List<OrderCustomerItem> subList;
	@ApiModelProperty(value="Order Sub Delete List")
	private List<OrderCustomerItem> subDelList;	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;
	@ApiModelProperty(value="STOCK조회시작LV")
	private int stockLv;
	@ApiModelProperty(value="생성해야할스탁수")
	private double createStockNoCnt;
	@ApiModelProperty(value="파일공유여부")
	private String fileShare;
	
	/** 오더공통저장에 사용하기위해 org.yht.exps.express.model.edi.ManifestOrder.java에만 있는 내용 추가 */
	@ApiModelProperty(value="목적국가명")
	private String destCountryName;
	@ApiModelProperty(value="세관명")
	private String customsName;
	@ApiModelProperty(value="세관과명")
	private String customsDptName;
	@ApiModelProperty(value="목록선별자_아이디")
	private String selectorUserName;
	@ApiModelProperty(value="선별물품확인자_아이디")
	private String confirmerUserName;
	@ApiModelProperty(value="반출사유코드")
	private String carryoutCode;
	@ApiModelProperty(value="적하목록전송여부")
	private String sendManifestYn;
	@ApiModelProperty(value="정정구분")
	private String modType;
	@ApiModelProperty(value="정정사유")
	private String modReason;
	@ApiModelProperty(value="단일항목정정_코드")
	private String singleModCode;
	@ApiModelProperty(value="단일항목정정_사유코드")
	private String singleModReasonCode;
	@ApiModelProperty(value="정정전내역")
	private String beforeValue;
	@ApiModelProperty(value="정정후내역")
	private String afterValue;
	
	@ApiModelProperty(value="툴바 - 목록cnt")
	private String simpleCnt;
	@ApiModelProperty(value="툴바 - 일반cnt")
	private String generalCnt;
	@ApiModelProperty(value="툴바 - 이행cnt")
	private String implementationCnt;
	@ApiModelProperty(value="툴바 - 배제cnt")
	private String excludeCnt;
	
	@ApiModelProperty(value="목록")
	private int sumSimple;
	@ApiModelProperty(value="일반")
	private int sumGeneral;
	@ApiModelProperty(value="배제")
	private int sumExclude;
	@ApiModelProperty(value="검사")
	private int sumInspection;
	@ApiModelProperty(value="취하")
	private int sumDrop;
	
	
	@ApiModelProperty(value="사용자 언어")
	private String userLanguage;
	
	@ApiModelProperty(value="콘솔 action")
	private String consolAction;
	
	@ApiModelProperty(value="콘솔 action")
	private String expOrigYn;
	
	@ApiModelProperty(value="profile명")
	private String profileName;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
	
	//bros
	@ApiModelProperty(value="운송방법")
	private String deliveryTypeCode;
	@ApiModelProperty(value="구분번호")
	private String separationNo;
	@ApiModelProperty(value="agent 총중량")
	private double agentGwt;
	@ApiModelProperty(value="agent 운임적용중량")
	private double agentCwt;
	@ApiModelProperty(value="agent 볼륨중량")
	private double agentVwt;
	@ApiModelProperty(value="상품금액")
	private double itemAmountTot;
	@ApiModelProperty(value="주문자운임")
	private double itemDeliveryFeeTot;
	@ApiModelProperty(value="보험금액")
	private double itemInsuranceTot;
	@ApiModelProperty(value="신고금액")
	private double itemDeclarationTot;
	@ApiModelProperty(value="신고구분코드")
	private String declarationCode;
	@ApiModelProperty(value="DOC 여부")
	private String documentYn;
	@ApiModelProperty(value="결재방법")
	private String licensePayMethod;
	@ApiModelProperty(value="수출자상호")
	private String shipperBusinessName;
	@ApiModelProperty(value="수출자 대표자")
	private String shipperBusinessPresident;
	@ApiModelProperty(value="수출자주소")
	private String shipperBusinessAddress;
	@ApiModelProperty(value="VAT(GST)번호")
	private String shipperGstNo;
	@ApiModelProperty(value="송화인명ENG")
	private String shipperNameEng;
	@ApiModelProperty(value="도시Local")
	private String shipperPlaceChi;
	@ApiModelProperty(value="도시Eng")
	private String shipperPlaceEng;
	@ApiModelProperty(value="중국+영문 주소")
	private String shipperAddressSum;
	@ApiModelProperty(value="발송인 주소(US)")
	private String shipperAddressEng;
	@ApiModelProperty(value="발송인 주소(CN)")
	private String shipperAddressChi;
	@ApiModelProperty(value="받는사람 이메일")
	private String consigneeEmailLoc;
	@ApiModelProperty(value="CN:성")
	private String consigneeState;
	@ApiModelProperty(value="CN:구")
	private String consigneeLocalDist;
	@ApiModelProperty(value="CN:전체 주소")
	private String consigneeAddressSum;
	@ApiModelProperty(value="받는사람 이니셜")
	private String consigneeInitial;
	@ApiModelProperty(value="받는사람 TEL")
	private String consigneeTelLoc;
	@ApiModelProperty(value="받는사람 HP")
	private String consigneeHpLoc;
	@ApiModelProperty(value="도착지 송장번호")
	private String destDeliveryNo;
	@ApiModelProperty(value="배송사정보 환종")
	private String deliveryCurrencyCode;
	@ApiModelProperty(value="관세사정보 환종")
	private String tariffCurrencyCode;
	@ApiModelProperty(value="배차번호")
	private String assignmentNo;
	@ApiModelProperty(value="멀티화물 insert 수동여부")
	private String multiInsertYn;
	@ApiModelProperty(value="운송방법DIM계산")
	private int divideValue;
	@ApiModelProperty(value="발송보류")
	private String sendHoldYn;
	@ApiModelProperty(value="운송방법 검증결과")
	private String deliveryVerifyResult;
	@ApiModelProperty(value="운송방법 검증여부")
	private String deliveryVerifyYn;
	@ApiModelProperty(value="패킹건수불일치여부")
	private String packingErrorYn;
	@ApiModelProperty(value="packing No")
	private String packingNo;
	@ApiModelProperty(value="shipmentBoxId")
	private String shipmentBoxId;
	@ApiModelProperty(value="쿠팡box번호")
	private String returnRequestId;
	@ApiModelProperty(value="반품요청일")
	private String returnReqDate;
	@ApiModelProperty(value="반품배송사")
	private String returnDeliveryCode;
	@ApiModelProperty(value="반품Y/N")
	private String returnRegDate;
	@ApiModelProperty(value="반품배송출발일")
	private String returnStartDate;
	@ApiModelProperty(value="반품배송완료일")
	private String returnEndDate;
	@ApiModelProperty(value="배송요청일")
	private String podDateChk;
	
	/** bms */
	@ApiModelProperty(value="XRAY판독자1(UPS등 자가장치장)")
	private String xrayReader1Name;
	@ApiModelProperty(value="XRAY판독자2(UPS등 자가장치장)")
	private String xrayReader2Name;
	
	@ApiModelProperty(value="검사구분")
	private String inspectionGubn;
	@ApiModelProperty(value="검사")
	private String seInspectionCodeName;
	@ApiModelProperty(value="수입신고불가")
	private String simpleStatementYn;
	@ApiModelProperty(value="수입신고불가사유")
	private String simpleStatementReason;
	@ApiModelProperty(value="검사지정사유")
	private String inspectionAppCode;
	@ApiModelProperty(value="취하사유수기")
	private String dropRemarks;
	@ApiModelProperty(value="목적국 HOUSE")
	private String destCountryH;
}

package org.yht.exps.express.model.eCommerce.license;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "License", description = "전자상거래 면장")
@Alias("license")
@EqualsAndHashCode(callSuper=false)
public class License extends ResultList{
	@ApiModelProperty(value="관리번호")
	private String licenseJobNo;

	@ApiModelProperty(value="도입사 코드")
	private String entCode;

	@ApiModelProperty(value="면장구분 , X:일반면장, E:이행면장")
	private String licenseType;

	@ApiModelProperty(value="OD_JOB_NO")
	private String odJobNo;

	@ApiModelProperty(value="신고인 신고자 상호")
	private String declarationName;

	@ApiModelProperty(value="신고자부호")
	private String declarationRegNo;

	@ApiModelProperty(value="대표자명")
	private String declarationPresident;

	@ApiModelProperty(value="신고일자")
	private String licenseSendDate;
	
	@ApiModelProperty(value="신고일자")
	private String licenseSendTime;
	
	@ApiModelProperty(value="신고일자")
	private String licenseSendDateTime;

	@ApiModelProperty(value="수리일자")
	private String licenseRecvDate;
	
	@ApiModelProperty(value="수리일자")
	private String licenseRecvTime;
	
	@ApiModelProperty(value="수리일자")
	private String licenseRecvDateTime;

	@ApiModelProperty(value="적재의무기간")
	private String roDate;

	@ApiModelProperty(value="주문번호(Order No)")
	private String poNo;
	
	@ApiModelProperty(value="오더일자")
	private String poDate;

	@ApiModelProperty(value="H.B/L No")
	private String hblNo;

	@ApiModelProperty(value="수출신고번호")
	private String efnPoNo;

	@ApiModelProperty(value="세관")
	private String licenseCustomsCode;

	@ApiModelProperty(value="과")
	private String licenseCustomsDptCode;

	@ApiModelProperty(value="신고인 기재란")
	private String licenseRemark;

	@ApiModelProperty(value="재출자상호")
	private String licenseSendName;

	@ApiModelProperty(value="사업자번호")
	private String licenseSendBusinessId;

	@ApiModelProperty(value="수출대행자 수술자상호")
	private String agentName;

	@ApiModelProperty(value="통관고유부호")
	private String agentCustomsId;

	@ApiModelProperty(value="수출자구분")
	private String agentType;

	@ApiModelProperty(value="사업자 구분")
	private String agentCustomsIdType;

	@ApiModelProperty(value="사업자 일련번호")
	private String agentBusinessId;

	@ApiModelProperty(value="상품정보URL")
	private String itemUrl;

	@ApiModelProperty(value="구매자 상호")
	private String consigneeName;

	@ApiModelProperty(value="우편번호")
	private String consigneeZipcode;

	@ApiModelProperty(value="전화번호")
	private String consigneeTel;

	@ApiModelProperty(value="주소")
	private String consigneeAddress;

	@ApiModelProperty(value="물품소재지 우편번호")
	private String goodsLocZipcode;

	@ApiModelProperty(value="주소")
	private String goodsLocAddress;

	@ApiModelProperty(value="장치장소명")
	private String bwhName;

	@ApiModelProperty(value="운송 목적국")
	private String destCountry;

	@ApiModelProperty(value="운송수단")
	private String deliveryTypeCode;

	@ApiModelProperty(value="적재항")
	private String destPort;

	@ApiModelProperty(value="주문배송번호")
	private String destDeliveryNo;

	@ApiModelProperty(value="특송업체부호")
	private String expressSign;

	@ApiModelProperty(value="수출 화주 수출자상호")
	private String shipperBusinessName;

	@ApiModelProperty(value="통관고유부호")
	private String shipperBusinessCustomsId;

	@ApiModelProperty(value="대표자")
	private String shipperBusinessPresident;

	@ApiModelProperty(value="사업자번호")
	private String shipperBusinessId;

	@ApiModelProperty(value="우편번호")
	private String shipperBusinessZipcode;

	@ApiModelProperty(value="주소")
	private String shipperBusinessAddress;

	@ApiModelProperty(value="주소상세")
	private String shipperBusinessAddressa;

	@ApiModelProperty(value="건물관리번호")
	private String shipperOfficeMngNo;

	@ApiModelProperty(value="제조사 상호")
	private String makerName;
	
	@ApiModelProperty(value="환급신청인")
	private String refundType;

	@ApiModelProperty(value="통관고유부호")
	private String makerCustomsId;
	
	@ApiModelProperty(value="제조사사업자번호")
	private String makerBusinessId;

	@ApiModelProperty(value="산업단지부호")
	private String makerComplexCode;

	@ApiModelProperty(value="우편번호")
	private String makerZipcode;

	@ApiModelProperty(value="결제 인도조건")
	private String incoterms;

	@ApiModelProperty(value="통화단위")
	private String currencyCode;

	@ApiModelProperty(value="환율")
	private BigDecimal exchangeRate;
	
	@ApiModelProperty(value="상품금액 외화")
	private BigDecimal licensePayAmountCur;

	@ApiModelProperty(value="운임 외화")
	private BigDecimal deliveryFeeCur;

	@ApiModelProperty(value="보험료 원화")
	private BigDecimal insuranceLoc;

	@ApiModelProperty(value="보험료 외화")
	private BigDecimal insuranceCur;
	
	@ApiModelProperty(value="운임 통화단위")
	private String deliveryFeeCurCode;

	@ApiModelProperty(value="운임 원화")
	private BigDecimal deliveryFeeLoc;

	@ApiModelProperty(value="조정금액 통화단위")
	private String adjustAmountCurCode;

	@ApiModelProperty(value="조정금액 원화")
	private BigDecimal adjustAmountLoc;
	
	@ApiModelProperty(value="조정금액 외화")
	private BigDecimal adjustAmountCur;

	@ApiModelProperty(value="신고가격 원화")
	private BigDecimal declarationAmountLoc;

	@ApiModelProperty(value="신고가격 USD")
	private BigDecimal declarationAmountUsd;

	@ApiModelProperty(value="수량")
	private int pieces;

	@ApiModelProperty(value="수량 단위")
	private String unitPieces;

	@ApiModelProperty(value="중량")
	private BigDecimal gwt;

	@ApiModelProperty(value="중량 단위")
	private String unitWeight;

	@ApiModelProperty(value="결제방법")
	private String licensePayMethod;

	@ApiModelProperty(value="총란수")
	private int goodsItemTot;
	
	@ApiModelProperty(value="BOUND")
	private String bound;
	
	@ApiModelProperty(value="배송ID")
	private String deliveryId;
	
	@ApiModelProperty(value="전송일자")
	private String sendDate;
	
	@ApiModelProperty(value="전송시간")
	private String sendTime;
	
	@ApiModelProperty(value="처리결과")
	private String sendResultCode;
	
	@ApiModelProperty(value="결과조회")
	private String resultStatus;
	
	@ApiModelProperty(value="수리일자")
	private String resultDateTime;
	
	@ApiModelProperty(value="수리일")
	private String resultDate;
	
	@ApiModelProperty(value="수리시간")
	private String resultTime;
	
	// GOODS / COMMODITY
	@ApiModelProperty(value="HS CODE")
	private String hsCode;
	@ApiModelProperty(value="품명")
	private String detailItemName;
	@ApiModelProperty(value="상품ID")
	private String itemCode;
	@ApiModelProperty(value="순중량")
	private BigDecimal nwt;
	@ApiModelProperty(value="단가(외화)")
	private BigDecimal unitPriceCur;
	@ApiModelProperty(value="원산지국가")
	private String originCountry;
	@ApiModelProperty(value="브랜드")
	private String brand;
	@ApiModelProperty(value="일련번호")
	private String seq;
	@ApiModelProperty(value="포장개수")
	private int packingPieces;
	@ApiModelProperty(value="판매소계")
	private BigDecimal amountTotal;
	@ApiModelProperty(value="결제금액")
	private BigDecimal licensePayAmount;
	@ApiModelProperty(value="제조자-미상")
	private String makerNameNone;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="건별무상거래여부")
	private String freeTransYn;
	@ApiModelProperty(value="규격")
	private String itemSize;
	@ApiModelProperty(value="전송오류내역")
	private String sendResultErrorDesc;
	@ApiModelProperty(value="결과오류내역")
	private String resultErrorReason;
	
	// 면장 - 란
	private List<LicenseGoodsItem> licenseGoodsItemList;
	
	// 면장 - 규격
	private List<LicenseCommodityItem> licenseCommodityItemList;
	
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="수정자")
	private String updateUserName;
	
	@ApiModelProperty(value="저장타입")
	private String 	action;
	@ApiModelProperty(value="수정자")
	private String 	updateUserId;
	@ApiModelProperty(value="시간")
	private String 	gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	
	@ApiModelProperty(value="Count")
	private int count;
	
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
	@ApiModelProperty(value="수정자사업장코드")
	private String entBusinessCode;
}

package org.yht.exps.express.model.eCommerce.masterOrder;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.CommonModel;
import org.yht.util.Util;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@ApiModel(value = "MasterOrder", description = "전자상거래 MasterOrder")
@Alias("masterOrder")
public class MasterOrder implements Cloneable{
	@ApiModelProperty(value="오더관리번호")
	private String odJobNo;
	@ApiModelProperty(value="비엘관리번호")
	private String oblJobNo;
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	@ApiModelProperty(value="도입사코드 5자리 + 국가 2자리")
	private String entBusinessCode;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="SR번호")
	private String srNo;
	@ApiModelProperty(value="SOLUTION")
	private String solution;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="BL구분_MH")
	private String blTypeMh;
	@ApiModelProperty(value="SS:SINGLE, CS:CONSOLE")
	private String blTypeSc;
	@ApiModelProperty(value="카고비엘번호")
	private String cargoBlNo;
	@ApiModelProperty(value="송하인_코드")
	private String shipperCode;
	@ApiModelProperty(value="송하인_명칭")
	private String shipperName;
	@ApiModelProperty(value="송하인_주소")
	private String shipperAddress;
	@ApiModelProperty(value="송하인_전화번호")
	private String shipperTel;
	@ApiModelProperty(value="송하인_우편번호")
	private String shipperZipcode;
	@ApiModelProperty(value="송하인_국가코드")
	private String shipperCountryCode;
	@ApiModelProperty(value="송하인STATE_코드")
	private String shipperStateCode;
	@ApiModelProperty(value="송하인PLACE")
	private String shipperPlace;
	@ApiModelProperty(value="송하인STREET")
	private String shipperStreet;
	@ApiModelProperty(value="송하인_사업자번호")
	private String shipperBusinessId;
	@ApiModelProperty(value="송하인_담당자")
	private String shipperAttn;
	@ApiModelProperty(value="송하인_팩스번호")
	private String shipperFax;
	@ApiModelProperty(value="수하인_코드")
	private String consigneeCode;
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
	@ApiModelProperty(value="수하인PLACE")
	private String consigneePlace;
	@ApiModelProperty(value="수하인STREET")
	private String consigneeStreet;
	@ApiModelProperty(value="수하인_사업자번호")
	private String consigneeBusinessId;
	@ApiModelProperty(value="수하인_담당자")
	private String consigneeAttn;
	@ApiModelProperty(value="수하인_팩스번호")
	private String consigneeFax;
	@ApiModelProperty(value="실화주_코드")
	private String notifyCode;
	@ApiModelProperty(value="실화주_명칭")
	private String notifyName;
	@ApiModelProperty(value="실화주_주소")
	private String notifyAddress;
	@ApiModelProperty(value="실화주_전화번호")
	private String notifyTel;
	@ApiModelProperty(value="실화주_우편번호")
	private String notifyZipcode;
	@ApiModelProperty(value="실화주_국가코드")
	private String notifyCountryCode;
	@ApiModelProperty(value="실화주STATE_코드")
	private String notifyStateCode;
	@ApiModelProperty(value="실화주PLACE")
	private String notifyPlace;
	@ApiModelProperty(value="실화주STREET")
	private String notifyStreet;
	@ApiModelProperty(value="실화주_사업자번호")
	private String notifyBusinessId;
	@ApiModelProperty(value="실화주_담당자")
	private String notifyAttn;
	@ApiModelProperty(value="실화주_팩스번호")
	private String notifyFax;
	@ApiModelProperty(value="포워더_코드")
	private String fwrdCode;
	@ApiModelProperty(value="포워더_명칭")
	private String fwrdName;
	@ApiModelProperty(value="포워더_주소")
	private String fwrdAddress;
	@ApiModelProperty(value="SIGN_HIS_AGENT")
	private String signHisAgent;
	@ApiModelProperty(value="SIGN_ACTING_AS")
	private String signActingAs;
	@ApiModelProperty(value="BL발행일자")
	private String issueDate;
	@ApiModelProperty(value="BL발행지코드")
	private String issuePlaceCode;
	@ApiModelProperty(value="SIGN_ATTN")
	private String signAttn;
	@ApiModelProperty(value="항공사_선사코드")
	private String lineCode;
	@ApiModelProperty(value="항공사_선사명칭")
	private String lineName;
	@ApiModelProperty(value="ISSUING_CARRIER")
	private String issuingCarriers;
	@ApiModelProperty(value="IATA코드")
	private String iataCode;
	@ApiModelProperty(value="출발지코드")
	private String origCode;
	@ApiModelProperty(value="출발지명")
	private String origName;
	@ApiModelProperty(value="도착지코드")
	private String destCode;
	@ApiModelProperty(value="도착지명")
	private String destName;
	@ApiModelProperty(value="목적항")
	private String destPort;
	@ApiModelProperty(value="경유지코드_A")
	private String viaToA;
	@ApiModelProperty(value="경유지편명_A")
	private String viaByA;
	@ApiModelProperty(value="경유지코드_B")
	private String viaToB;
	@ApiModelProperty(value="경유지편명_B")
	private String viaByB;
	@ApiModelProperty(value="경유지코드_C")
	private String viaToC;
	@ApiModelProperty(value="경유지편명_C")
	private String viaByC;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="출항시간")
	private String obTime;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="입항시간")
	private String arTime;
	@ApiModelProperty(value="BL_HANDLING_INFO")
	private String handlingInfo;
	@ApiModelProperty(value="BL_HANDLING_SPECIAL")
	private String handlingSpecial;
	@ApiModelProperty(value="BL_ACCOUNT_INFO")
	private String accountInfo;
	@ApiModelProperty(value="운임조건")
	private String incoterms;
	@ApiModelProperty(value="통화단위")
	private String currencyCode;
	@ApiModelProperty(value="운송신고가격")
	private String dvCarriage;
	@ApiModelProperty(value="통관신고가격")
	private String dvCustoms;
	@ApiModelProperty(value="보험신고가격")
	private String dvInsurance;
	@ApiModelProperty(value="품명")
	private String ediCommodityItem;
	@ApiModelProperty(value="blHandlingSpecialName")
	private String handlingSpecialName;
	@ApiModelProperty(value="수량")
	private double pieces;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="단가_AS_UP여부")
	private String rateAsUpYn;
	@ApiModelProperty(value="단가_CLASS")
	private String rateClass;
	@ApiModelProperty(value="단가_품목")
	private String rateItem;
	@ApiModelProperty(value="단가_RATE")
	private double rate;
	@ApiModelProperty(value="FREIGHT_CHARGE")
	private double freightCharge;
	@ApiModelProperty(value="BL_WEIGHT_PPD")
	private double weightChgPp;
	@ApiModelProperty(value="BL_WEIGHT_CCT")
	private double weightChgCc;
	@ApiModelProperty(value="BL_VALUATION_PPD")
	private double valuationChgPp;
	@ApiModelProperty(value="BL_VALUATION_CCT")
	private double valuationChgCc;
	@ApiModelProperty(value="BL_TAX_PPD")
	private double taxChgPp;
	@ApiModelProperty(value="BL_TAX_CCT")
	private double taxChgCc;
	@ApiModelProperty(value="BL_DUE_AGENT_PPD")
	private double dueAgentPp;
	@ApiModelProperty(value="BL_DUE_AGENT_CCT")
	private double dueAgentCc;
	@ApiModelProperty(value="BL_DUE_CARRIER_PPD")
	private double dueCarrierPp;
	@ApiModelProperty(value="BL_DUE_CARRIER_CCT")
	private double dueCarrierCc;
	@ApiModelProperty(value="BL_TOTAL_PPD")
	private double prepaidChgTotal;
	@ApiModelProperty(value="BL_TOTAL_CCT")
	private double collectChgTotal;
	@ApiModelProperty(value="운임중량용적")
	private double cwtCbm;
	@ApiModelProperty(value="입력자아이디")
	private String registerUserId;
	@ApiModelProperty(value="입력자명")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정자아이디")
	private String updateUserId;
	@ApiModelProperty(value="수정자명")
	private String updateUserName;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	@ApiModelProperty(value="중개사업자1")
	private String venderId1;	
	@ApiModelProperty(value="대리점코드")
	private String custCodeLv1;
	@ApiModelProperty(value="발송국가")
	private String origCountry;
	@ApiModelProperty(value="목적국가")
	private String destCountry;
	@ApiModelProperty(value="description1")
	private String marksGoodsLeft;
	@ApiModelProperty(value="description2")
	private String marksGoodsRight;
	@ApiModelProperty(value="콘솔하우스건수")
	private double houseCnt;
	@ApiModelProperty(value="관세청정보관리번호")
	private String ediMailboxSeqNo;
	@ApiModelProperty(value="세관코드")
	private String customsCode;
	@ApiModelProperty(value="세관과코드")
	private String customsDptCode;
	@ApiModelProperty(value="보세창고")
	private String assignmentWhCode;
	@ApiModelProperty(value="하기장소")
	private String discWhCode;
	@ApiModelProperty(value="선명_편명")
	private String vesselFlight;
	@ApiModelProperty(value="전자상거래항공사명")
	private String ecmLineName;
	@ApiModelProperty(value="해운_항차")
	private String vesselVoyage;
	@ApiModelProperty(value="RECEIPT코드")
	private String receiptCode;
	@ApiModelProperty(value="RECEIPT명")
	private String receiptName;
	@ApiModelProperty(value="DISCHARGE코드")
	private String discCode;
	@ApiModelProperty(value="DISCHARGE명")
	private String discName;
	@ApiModelProperty(value="DELIVERY코드")
	private String deliCode;
	@ApiModelProperty(value="DELIVERY명")
	private String deliName;
	@ApiModelProperty(value="BOOKING일자")
	private String bookingDate;
	@ApiModelProperty(value="BOOKING번호")
	private String bookingNo;
	@ApiModelProperty(value="단위_수량")
	private String unitPieces;
	@ApiModelProperty(value="BL구분_LFB")
	private String blTypeLfb;
	@ApiModelProperty(value="BL_OBL여부")
	private String originalBlYn;
	@ApiModelProperty(value="BL_OBL매수")
	private String noOfOriginal;
	@ApiModelProperty(value="BL구분_CY_CFS")
	private String blCargoCyCfs;
	@ApiModelProperty(value="BL_SAY")
	private String blSay;
	@ApiModelProperty(value="BL_PAYABLE_AT")
	private String blPayableAt;
	@ApiModelProperty(value="FREIGHT_TERM")
	private String freightTerm;
	@ApiModelProperty(value="OTHER_TERM")
	private String otherTerm;
	@ApiModelProperty(value="비고사항")
	private String remarks;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="MSN")
	private String msn;
	
	@ApiModelProperty(value="관리번호 List")
	private List<String> odJobNoList;
	@ApiModelProperty(value="MasterOrderOtherCharge List")
	private List<MasterOrderOtherCharge> otherChargeList;
	@ApiModelProperty(value="MasterOrderDimension List")
	private List<MasterOrderDimension> dimensionList;
	@ApiModelProperty(value="MasterOrderLicense List")
	private List<MasterOrderLicense> licenseList;
	@ApiModelProperty(value="MasterOrderHts List")
	private List<MasterOrderHts> htsList;
	@ApiModelProperty(value="MasterOrderContainerList List")
	private List<MasterOrderContainer> containerList;
	@ApiModelProperty(value="MasterOrderCntrType List")
	private List<MasterOrderContainer> cntrTypeList;
	
	
//	/** 저장 */
	private CommonModel common;
	@ApiModelProperty(value="insert/update구분")
	private String action;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="작업자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="사용자권한")
	private String userAuth;
	@ApiModelProperty(value="입력자레벨")
	private String registerUserLevel;
	
	@ApiModelProperty(value="원BL 관리번호")
	private String origOdJobNo;
	@ApiModelProperty(value="원BL 관리번호")
	private String origOblJobNo;
	@ApiModelProperty(value="특송데이터판별")
	private String emsBl;
//	@ApiModelProperty(value="도입사계약일자")
//	private String contractDate;
//	@ApiModelProperty(value="사용자권한")
//	private String userAuth;
//	
	public MasterOrder clone() throws CloneNotSupportedException{
		MasterOrder vo = null;
		try {
			vo = (MasterOrder) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error(Util.getPrintStacTraceString(e));
			throw e;
		}
		return vo;
	}
//	@ApiModelProperty(value="사용자 언어")
//	private String userLanguage;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}

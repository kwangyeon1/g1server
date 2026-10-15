package org.yht.exps.express.model.eCommerce.consol;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ConsolOrder", description = "전자상거래 Consol Order")
@Alias("consolOrder")
public class ConsolOrder {
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="obl관리번호")
	private String oblJobNo;
	@ApiModelProperty(value="ISSUING_CARRIER")
	private String issuingCarriers;
	@ApiModelProperty(value="SIGN_HIS_AGENT")
	private String signHisAgent;
	@ApiModelProperty(value="SIGN_ACTING_AS")
	private String signActingAs;
	@ApiModelProperty(value="포워더_코드")
	private String fwrdCode;
	@ApiModelProperty(value="포워더_명칭")
	private String fwrdName;
	@ApiModelProperty(value="포워더_주소")
	private String fwrdAddress;
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;
	@ApiModelProperty(value="솔루션")
	private String solution;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
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
	@ApiModelProperty(value="중개사업자1")
	private String venderId1;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="voyage")
	private String voyage;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="MSN")
	private String msn;
	@ApiModelProperty(value="항공사_선사")
	private String lineCode;
	@ApiModelProperty(value="항공사_선사명")
	private String lineName;
	@ApiModelProperty(value="선명_편명")
	private String vesselFlight;
	@ApiModelProperty(value="출발지코드")
	private String origCode;
	@ApiModelProperty(value="출발지명")
	private String origName;
	@ApiModelProperty(value="발송국가")
	private String origCountry;
	@ApiModelProperty(value="도착지코드")
	private String destCode;
	@ApiModelProperty(value="도착지명")
	private String destName;
	@ApiModelProperty(value="도착국가")
	private String destCountry;
	@ApiModelProperty(value="도착지코드_국가")
	private String destCodeCountry;
	@ApiModelProperty(value="도착지코드_지역")
	private String destCodeArea;
	@ApiModelProperty(value="목적지코드")
	private String destPort;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="출항시간")
	private String obTime;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="입항시간")
	private String arTime;
	@ApiModelProperty(value="콘솔여부")
	private String consolYn;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="마감여부")
	private String finishYn;
	@ApiModelProperty(value="세관")
	private String customsCode;
	@ApiModelProperty(value="과(세관)")
	private String customsDptCode;
	@ApiModelProperty(value="보세창고")
	private String assignmentWhCode;
	@ApiModelProperty(value="하기장소")
	private String discWhCode;
	@ApiModelProperty(value="관세청정보관리번호")
	private String ediMailboxSeqNo;
	@ApiModelProperty(value="하우스번호리스트")
	private List<String> hblNoList;
	@ApiModelProperty(value="관리번호리스트")
	private List<String> odJobNoList;
	@ApiModelProperty(value="마스터관리번호리스트")
	private List<String> masterOdJobNoList;
	@ApiModelProperty(value="상위업체멀티리스트")
	private List<String> custLv1List;
	@ApiModelProperty(value="입력자업체레벨")
	private String registerCustomerLevel;
	@ApiModelProperty(value="사용자권한")
	private String userAuth;
	@ApiModelProperty(value="거래처코드")
	private String ecmCustomerCode;
	@ApiModelProperty(value="defaultSort")
	private String defaultSort;
	
	/** 저장 */
	@ApiModelProperty(value="insert/update구분")
	private String action;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="작업자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="H.B/L No")
	private String hblNo;
	@ApiModelProperty(value="통화단위")
	private String currencyCode;
	@ApiModelProperty(value="반출사유코드")
	private String carryoutCode;
	@ApiModelProperty(value="제출번호")
	private String simpleDocNo;
	@ApiModelProperty(value="수정전제출번호")
	private String oldSimpleDocNo;
	@ApiModelProperty(value="목록신고_송신자")
	private String simpleReportUserId;
	@ApiModelProperty(value="마스터오더관리번호")
	private String masterOdJobNo;
	@ApiModelProperty(value="적하목록전송여부")
	private String sendMfYn;
	@ApiModelProperty(value="통관목록신고상태")
	private String simpleEdiStatus;
	@ApiModelProperty(value="통관목록신고오더건수")
	private String simpleEdiSendCnt;
	@ApiModelProperty(value="외화금액")
	private double amountCur;
	@ApiModelProperty(value="로컬금액")
	private double amountLoc;
	@ApiModelProperty(value="미화금액")
	private double amountUsd;
	@ApiModelProperty(value="환율")
	private double exchangeRate;
	@ApiModelProperty(value="상위업체리스트멀티콘솔")
	private String custLv1Remark;
	@ApiModelProperty(value="상위업체리스트멀티콘솔")
	private String noBoundConsol;
	@ApiModelProperty(value="거래처용BoundType")
	private String reverseBoundType;
	
	/** 체크 */
	@ApiModelProperty(value="DOC 여부")
	private String documentYn;
	@ApiModelProperty(value="마스터제출번호하우스건수")
	private int masterSimpleDocNoHblCnt;
	@ApiModelProperty(value="콘솔하우스건수")
	private int hblCnt;
	@ApiModelProperty(value="profile명")
	private String profileName;
	
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}
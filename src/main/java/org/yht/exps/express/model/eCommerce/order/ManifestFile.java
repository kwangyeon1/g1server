package org.yht.exps.express.model.eCommerce.order;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "ManifestFile", description = "전자상거래 적하목록 파일업로드 리스트")
@Alias("manifestFile")
@EqualsAndHashCode(callSuper=false)
public class ManifestFile extends ResultList{
	
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="대표품목_OLD")
	private String oldCommodityItem;
	@ApiModelProperty(value="대표품목_NEW")
	private String newCommodityItem;
	@ApiModelProperty(value="CreateYn")
	private String createYn;
	@ApiModelProperty(value="PlanYn")
	private String planYn;
	@ApiModelProperty(value="DoSendYn")
	private String doSendYn;
	@ApiModelProperty(value="대리점코드")
	private String custCodeLv1;
	@ApiModelProperty(value="수량")
	private double pieces;
	@ApiModelProperty(value="Inner Pieces (실제수량, EDI신고용)")
	private double piecesReal;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="총중량Lbs")
	private double gwtLbs;
	@ApiModelProperty(value="대표품목")
	private String commodityItem;
	@ApiModelProperty(value="대표품목2")
	private String commodityItem2;
	@ApiModelProperty(value="거래구분")
	private String ecmType;
	@ApiModelProperty(value="전자상거래유형")
	private String ecmBuyType;
	@ApiModelProperty(value="해외판매자 명")
	private String sellerName;
	@ApiModelProperty(value="구매대행등록부호")
	private String agentRegNo;
	@ApiModelProperty(value="구매대행 업자명")
	private String agentName;
	@ApiModelProperty(value="홈페이지")
	private String url;
	@ApiModelProperty(value="통관_용도구분")
	private String cdUseType;
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
	@ApiModelProperty(value="통관타입")
	private String cdType;
	@ApiModelProperty(value="통관타입")
	private String cdTypeTemp;
	@ApiModelProperty(value="화주_명칭")
	private String shipperName;
	@ApiModelProperty(value="화주_주소")
	private String shipperAddress;
	@ApiModelProperty(value="화주_전화번호")
	private String shipperTel;
	@ApiModelProperty(value="화주_우편번호")
	private String shipperZipcode;
	@ApiModelProperty(value="화주_국가코드")
	private String shipperCountryCode;
	@ApiModelProperty(value="화주STATE_코드")
	private String shipperStateCode;
	@ApiModelProperty(value="화주PLACE")
	private String shipperPlace;
	@ApiModelProperty(value="화주STREET")
	private String shipperStreet;
	@ApiModelProperty(value="화주_사업자번호")
	private String shipperBusinessId;
	@ApiModelProperty(value="화주_팩스번호")
	private String shipperFax;
	@ApiModelProperty(value="수하인_명칭")
	private String consigneeName;
	@ApiModelProperty(value="수하인_주소")
	private String consigneeAddress;
	@ApiModelProperty(value="수하인_전화번호")
	private String consigneeTel;
	@ApiModelProperty(value="수하인_휴대폰")
	private String consigneeHp;
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
	private String bankName;
	@ApiModelProperty(value="수하인_명칭_한글")
	private String consigneeNameLoc;
	@ApiModelProperty(value="수하인_주소_한글")
	private String consigneeAddressLoc;
	@ApiModelProperty(value="수하인_팩스번호")
	private String consigneeFax;
	@ApiModelProperty(value="허용품목코드")
	private String allowedItemCode;
	@ApiModelProperty(value="비고사항")
	private String remarks;
	@ApiModelProperty(value="목적국가")
	private String destCountry;
	@ApiModelProperty(value="DOC 여부")
	private String documentYn;
	@ApiModelProperty(value="통관_ID_구분")
	private String customsIdType;
	@ApiModelProperty(value="통관_ID")
	private String customsId;
	@ApiModelProperty(value="목록신고제출번호")
	private String simpleDocNo;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="MSN")
	private String msn;
	@ApiModelProperty(value="HSN")
	private String hsn;
	@ApiModelProperty(value="항공사_선사코드")
	private String lineCode;
	@ApiModelProperty(value="선명_편명")
	private String vesselFlight;
	@ApiModelProperty(value="E:Export, R:Re-Shippment(환적화물) I:Import, T:Trans-Shippment(통과화물)")
	private String blTypeIert;
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
	@ApiModelProperty(value="목적항")
	private String destPort;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="CONSOL_여부")
	private String consolYn;
	@ApiModelProperty(value="오더관리번호")
	private String odJobNo;
	@ApiModelProperty(value="마스터오더관리번호")
	private String masterOdJobNo;
	@ApiModelProperty(value="출항시간")
	private String obTime;
	@ApiModelProperty(value="입항시간")
	private String arTime;
	@ApiModelProperty(value="세관코드")
	private String customsCode;
	@ApiModelProperty(value="세관과코드")
	private String customsDptCode;
	@ApiModelProperty(value="항공/해운수입 하기(하선)장소코드 (장치장 또는 보세창고코드)")
	private String discWhCode;
	@ApiModelProperty(value="수입 전용. 배정코드")
	private String assignmentWhCode;
	@ApiModelProperty(value="관세청정보관리번호")
	private String ediMailboxSeqNo;
	@ApiModelProperty(value="중개사업자")
	private String venderId;
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;
	@ApiModelProperty(value="대표사업장코드")
	private String headEntBusinessCode;
	@ApiModelProperty(value="SOLUTION")
	private String solution;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="BL구분_MH")
	private String blTypeMh;
	@ApiModelProperty(value="검사_여부")
	private String inspectionYn;
	@ApiModelProperty(value="일검_여부")
	private String geInspectionYn;
	@ApiModelProperty(value="멀티_여부")
	private String multiYn;
	@ApiModelProperty(value="홀드_여부")
	private String holdYn;
	@ApiModelProperty(value="자유무역협정_여부")
	private String ftaYn;
	@ApiModelProperty(value="포워더_코드")
	private String fwrdCode;
	@ApiModelProperty(value="마감여부")
	private String finishYn;
	@ApiModelProperty(value="입력자아이디")
	private String userId;
	@ApiModelProperty(value="수정자아이디")
	private String updateUserId;
	@ApiModelProperty(value="통관부호_검증상태")
	private String customsIdVerify;
	@ApiModelProperty(value="통관부호_오류내역")
	private String customsIdError;
	@ApiModelProperty(value="현재진행상태코드")
	private String lastPodStatus;
	@ApiModelProperty(value="POD관리번호")
	private String podJobNo;
	@ApiModelProperty(value="POD관리번호2")
	private String podJobNo2;
	@ApiModelProperty(value="Order 세부품목 List")
	private List<Map<String, Object>> itemList;
	@ApiModelProperty(value="Order packing List")
	private List<Map<String, Object>> packingList;
	@ApiModelProperty(value="Order ChildNumber List")
	private List<Map<String, Object>> childNumber;
	@ApiModelProperty(value="목록선별자_아이디")
	private String selectorUserId;
	@ApiModelProperty(value="선별물품확인자_아이디")
	private String confirmerUserId;
	@ApiModelProperty(value="AMS_여부")
	private String amsYn;
	@ApiModelProperty(value="TrackingNo리스트문자열")
	private String trackingNoStr;
	@ApiModelProperty(value="TrackingNo")
	private String trackingNo;
	@ApiModelProperty(value="insert/update구분")
	private String action;
	@ApiModelProperty(value="패킹건수불일치여부")
	private String packingErrorYn;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	@ApiModelProperty(value="입력자명")
	private String registerUserName;
	@ApiModelProperty(value="수정자명")
	private String updateUserName;
	@ApiModelProperty(value="UPS_iFile")
	private String tsSendType;
	@ApiModelProperty(value="UPS_iFile")
	private String transType;
	@ApiModelProperty(value="UPS_iFile")
	private String duties;
	@ApiModelProperty(value="UPS_iFile")
	private String billingTerm;
	@ApiModelProperty(value="UPS_iFile")
	private String orig;
	@ApiModelProperty(value="UPS_iFile")
	private String actualCstName;
	@ApiModelProperty(value="UPS_eFile")
	private String groupName;
	@ApiModelProperty(value="UPS_eFile")
	private String countryCode;
	@ApiModelProperty(value="UPS_eFile")
	private String cityPort;
	@ApiModelProperty(value="UPS_eFile")
	private String serviceCode;
	@ApiModelProperty(value="UPS_eFile")
	private String runType;
	@ApiModelProperty(value="UPS_eFile")
	private String seqNoId;
	
	private List<String> seqNoIdList;
	private String basicType;
	private String etc1;
	private String etc2;
	private String etc3;
	private String cdEtc2;
	
	private String StockSign;
	private String stockType;
	private String stockSeqLength;
	private String dateLength;
	private String signType;
	private String stockDateFormat;
	private String resultCode;
	private String stockRemark;
	private String appendStockSign;
	private String subSeq;
	private int seqNo;
	
	// 목록배제조회 그리드
	private String fCode;
	private String importDate;
	private String remove;
	
	// UPS : 항공수입 T/S HAWB 가져오기
	private String impMblNo;
	private String impOdJobNo;
	private String expMblNo;
	private String expOdJobNo;
	private String expMasterOdJobNo;
	private String expYn;
	private String licenseNo;
}

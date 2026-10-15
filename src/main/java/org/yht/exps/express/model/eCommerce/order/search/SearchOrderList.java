package org.yht.exps.express.model.eCommerce.order.search;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=false)
@ToString(callSuper = true)
@ApiModel(value = "SearchOrderList", description = "오더조회")
@Alias("searchOrderList")
public class SearchOrderList extends SearchList {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="SOLUTION", example="ECM")
	private String srchSolution;
	@ApiModelProperty(value="Master/House구분", example="H")
	private String srchBlTypeMh;
	@ApiModelProperty(value="조회일자타입", example="dateTime")
	private String srchDateType;
	@ApiModelProperty(value="항공/해상,수출/수입구분", example="AO")
	private String srchBoundType;
	@ApiModelProperty(value="수출/수입구분", example="EXP or IMP")
	private String srchBoundType2;
	@ApiModelProperty(value="콘솔여부", example="Y or N")
	private String srchConsolYn;
	@ApiModelProperty(value="반입여부", example="Y or N")
	private String srchCarryinYn;
	@ApiModelProperty(value="반출여부", example="Y or N")
	private String srchCarryoutYn;
	@ApiModelProperty(value="통관구분")
	private String srchCdType;
	@ApiModelProperty(value="멀티운송장여부", example="Y or N")
	private String srchMultiYn;
	@ApiModelProperty(value="홀드여부", example="Y or N")
	private String srchHoldYn;
	@ApiModelProperty(value="홀드코드")
	private String srchHoldCode;
	@ApiModelProperty(value="홀드(처리)")
	private String srchHoldReleaseYn;
	@ApiModelProperty(value="통관부호 검증상태")
	private String srchCustomsIdVerify;
	@ApiModelProperty(value="SMS안내여부", example="Y or N")
	private String srchSmsInformYn;
	@ApiModelProperty(value="관세요금안내여부", example="Y or N")
	private String srchClearanceNoticeYn;
	@ApiModelProperty(value="마스터번호")
	private String srchMblNo;
	@ApiModelProperty(value="하우스번호")
	private String srchHblNo;
	@ApiModelProperty(value="주문번호")
	private String srchPoNo;
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	@ApiModelProperty(value="마감여부")
	private String srchFinishYn;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;	
	@ApiModelProperty(value="선택된 odJobNo 리스트")
	private List<String> srchOdJobNoList;	
	@ApiModelProperty(value="제출번호리스트")
	private List<String> srchSimpleDocNoList;
	@ApiModelProperty(value="제출번호")
	private String simpleDocNo;
	@ApiModelProperty(value="조회번호 Like검색 여부", example="Y or N")
	private String srchNoLikeYn;
	@ApiModelProperty(value="조회업체")
	private String srchCus;
	@ApiModelProperty(value="조회업체타입")
	private String srchCusType;
	@ApiModelProperty(value="선별대상여부", example="Y or N")
	private String srchSeInspectionYn;
	@ApiModelProperty(value="검사여부", example="Y or N")
	private String srchInspectionYn;
	@ApiModelProperty(value="X-Ray여부", example="Y or N")
	private String srchXrayYn;
	@ApiModelProperty(value="취하(보류통보)여부", example="Y or N")
	private String srchDropYn;
	@ApiModelProperty(value="미통관여부", example="Y or N")
	private String srchClearanceViolationYn;
	@ApiModelProperty(value="통관완료여부", example="Y or N")
	private String srchClearanceYn;
	@ApiModelProperty(value="관세입력여부", example="Y or N")
	private String srchTariffYn;
	@ApiModelProperty(value="편명")
	private String srchVesselFlight;
	@ApiModelProperty(value="로케이션")
	private String srchWhLocation;
	@ApiModelProperty(value="출발지")
	private String srchOrigCode;
	@ApiModelProperty(value="도착지")
	private String srchDestCode;
	@ApiModelProperty(value="마스터관리번호")
	private String srchMasterOdJobNo;
	@ApiModelProperty(value="마스터관리번호리스트")
	private String srchMasterOdJobNoListStr;
	@ApiModelProperty(value="하우스관리번호")
	private String srchHouseOdJobNo;
	@ApiModelProperty(value="관리번호")
	private String srchOdJobNo;
	@ApiModelProperty(value="이례사항여부", example="Y or N")
	private String srchAbnormalityYn;
	@ApiModelProperty(value="송장출력여부", example="Y or N")
	private String printYn;
	@ApiModelProperty(value="바운드타입reverse여부", example="Y or N")
	private String srchRvsBoundType;
	@ApiModelProperty(value="관세사코드")
	private String srchCbCode;
	@ApiModelProperty(value="관세사코드")
	private String cbCode;	
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;	
	@ApiModelProperty(value="하위업체")
	private String custCodeLv2;	
	@ApiModelProperty(value="송하인명")
	private String srchShipperName;
	@ApiModelProperty(value="수하인명")
	private String srchConsigneeName;
	@ApiModelProperty(value="통관상태")
	private String srchClearanceStatus;			
	@ApiModelProperty(value="변경배송사여부")
	private String srchChanged;	
	@ApiModelProperty(value="상담유형")
	private String srchCsType;
	@ApiModelProperty(value="배송사")
	private String srchDeliveryCode;
	@ApiModelProperty(value="배송사여부")
	private String srchDeliveryCodeYn;
	@ApiModelProperty(value="변경배송사여부")
	private String srchDeliveryNameChangedYn;
	@ApiModelProperty(value="변경배송주소여부")
	private String srchDeliveryAddressChangedYn;
	@ApiModelProperty(value="진행사항")
	private String srchLastPodStatus;
	@ApiModelProperty(value="진행사항(해당코드이후부터조회)")
	private String srchLastPodStatusMin;
	@ApiModelProperty(value="적하목록전송여부")
	private String srchMfSendYn;
	@ApiModelProperty(value="수입신고불가여부")
	private String srchSimpleStatementYn;
	@ApiModelProperty(value="우범화물여부")
	private String srchCriminalYn;
	@ApiModelProperty(value="통관목록신고상태")
	private String srchSimpleEdiStatus;
	@ApiModelProperty(value="이례사항여부")
	private String srchAbnormalYn;
	@ApiModelProperty(value="컨테이너조회여부")
	private String srchContainerYn;
	@ApiModelProperty(value="적하정정관리번호")
	private String srchModJobNo;
	@ApiModelProperty(value="적하정정관리번호")
	private String srchSeqNoId;
	@ApiModelProperty(value="거래구분")
	private String srchEcmType;
	@ApiModelProperty(value="거래유형")
	private String srchEcmBuyType;
	@ApiModelProperty(value="조회타입")
	private String srchType;
	@ApiModelProperty(value="조회타입")
	private String srchOption;
	@ApiModelProperty(value="데이터조회하지않음")
	private String srchNoRecords;
	@ApiModelProperty(value="검사,선별등 중복조회여부")
	private String srchMultiStatusYn;
	@ApiModelProperty(value="적하정정관리번호조회여부")
	private String srchModJobNoYn;
	@ApiModelProperty(value="적하정정관리번호조회타입")
	private String srchModType;
	@ApiModelProperty(value="화물구분")
	private String srchBlTypeIert;
	
	/** org.yht.exps.express.model.SearchExcelList 내용 추가 */
	@ApiModelProperty(value="화면 메뉴 아이디", example="OD, DELIVERY, BROKER")
	private String srchMenuId;	
	@ApiModelProperty(value="배송상태")
	private String srchStatus;
	@ApiModelProperty(value="배송사 코드", example="우체국, 대한통운")
	private String srchTrCode;	
	@ApiModelProperty(value="비용구분")
	private String srchDeliveryPaymentType; 
	@ApiModelProperty(value="")
	private String srchGroupCode;
	@ApiModelProperty(value="쿼리타입")
	private String srchQueryType;
	@ApiModelProperty(value="조회대상 Like검색 여부", example="Y or N")
	private String srchCusLikeYn;
	@ApiModelProperty(value="사업장코드", example="YHTXX-KR")
	private String srchEntBusinessCode;
	@ApiModelProperty(value="등록자")
	private String registerUserId;	
	@ApiModelProperty(value="배송사지정유무")
	private String deliveryCodeYn;
	@ApiModelProperty(value="배송상태")
	private String deliveryStatus;
	@ApiModelProperty(value="아이템 테이블 조회 유무")
	private String srchItemTableYn;
	@ApiModelProperty(value="컨테이너 테이블 조회 유무")
	private String srchContTableYn;
	@ApiModelProperty(value="멀티 테이블 조회 유무")
	private String srchMultiTableYn;
	@ApiModelProperty(value="마스터정보 조회 유무")
	private String srchMasterYn;	
	@ApiModelProperty(value="cost 테이블 조회 유무")
	private String srchCostTableYn;
	@ApiModelProperty(value="cost 테이블 조회 리스트")
	private List<Map<String, Object>> srchCostTableList;
	@ApiModelProperty(value="코드명컬럼 리스트")
	private List<Map<String, Object>> codeNameColumnList;
	@ApiModelProperty(value="집계하우스그리드조회조건")
	private List<Map<String, Object>> srchHouseSumList;
	@ApiModelProperty(value="오더 테이블 컬럼 리스트 ")
	private List<String> odColumnList;	
	@ApiModelProperty(value="additional 테이블 컬럼 리스트 ")
	private List<String> adColumnList;
	@ApiModelProperty(value="오더, additional 테이블에 없는 컬럼 리스트 ")
	private List<String> notOdAdColumnList;	
	@ApiModelProperty(value="로그인거래처코드id")
	private String loginCustomerCd;
	@ApiModelProperty(value="로그인거래처Lev")
	private String loginCustomerLev;
	@ApiModelProperty(value="출력여부")
	private String srchPrintYn;
	@ApiModelProperty(value="상태 리스트(멀티체크)")
	private List<String> srchStatusList;
	@ApiModelProperty(value="상태 제외 리스트")
	private List<String> srchStatusNotList;
	@ApiModelProperty(value="통관부호 검증상태")
	private String srchCusIdVerify;
	@ApiModelProperty(value="통관부호 리스트(멀티체크)")
	private List<String> srchCusIdVerifyList;
	@ApiModelProperty(value="검색거래처코드", example="거래처코드")
	private String srchCustomerCode;
	@ApiModelProperty(value="검색거래처명", example="거래처명")
	private String srchCustomerName;
	@ApiModelProperty(value="검색입력자명", example="입력자명")
	private String srchRegisterUserName;
	@ApiModelProperty(value="통관목록제출번호(서류Y)")
	private String simpleDocNoY;
	@ApiModelProperty(value="통관목록제출번호(서류N)")
	private String simpleDocNoN;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="로그인사용자상위거래처Lev")
	private String loginCustomerUpperLev;
	@ApiModelProperty(value="로그인사용자하위거래처Lev")
	private String loginCustomerLowLev;
	@ApiModelProperty(value="상태디폴트")
	private String statusDefault;

	@ApiModelProperty(value="조회 출/도착지 타입")
	private String srchOrigDestType;
	@ApiModelProperty(value="조회 출/도착지")
	private String srchOrigDest;
	@ApiModelProperty(value="출발지,도착지 Like검색 여부", example="Y or N")
	private String srchOrigDestLikeYn;
	@ApiModelProperty(value="EMS환적 여부 ")
	private String srchEmsYn;
	
	@ApiModelProperty(value="전자상거래 여부 ")
	private String srchEcmYn;
	
	
	@ApiModelProperty(value="통관구분 리스트(멀티체크)")
	private List<String> srchCdTypeList;
	@ApiModelProperty(value="신고상태 리스트(멀티체크)")
	private List<String> srchClearanceStatusList;
	@ApiModelProperty(value="진행사항 리스트(멀티체크)")
	private List<String> srchLastPodStatusList;
	@ApiModelProperty(value="상담유형 리스트(멀티체크)")
	private List<String> srchCsTypeList;
	@ApiModelProperty(value="배송사 리스트(멀티체크)")
	private List<String> srchDeliveryCodeList;
	@ApiModelProperty(value="개인통관부호 리스트(멀티체크)")
	private List<String> srchCustomsIdVerifyList;
	@ApiModelProperty(value="반출입구분(멀티체크)")
	private List<String> srchCarryList;
	@ApiModelProperty(value="거래구분 리스트(멀티체크)")
	private List<String> srchEcmTypeList;
	@ApiModelProperty(value="거래유형 리스트(멀티체크)")
	private List<String> srchEcmBuyTypeList;
	@ApiModelProperty(value="검사 리스트(멀티체크)")
	private List<String> srchMultiStatusList;
	
	
	@ApiModelProperty(value="도입사계약일자")
	private String contractDate;
	
	@ApiModelProperty(value="업데이트컬럼")
	private String updateField;
	@ApiModelProperty(value="업데이트값")
	private String updateValue;
	@ApiModelProperty(value="통관지연사유코드")
	private String clearanceViolationCode;
	@ApiModelProperty(value="통관지연사유")
	private String clearanceViolationReason;
	@ApiModelProperty(value="B/L넘버 멀티조회 비사용여부")
	private String notUseMultiYn;
	
	/** bms관련인지 여부 */
	@ApiModelProperty(value="bmsYn = 'Y','N' ")
	private String bmsYn;
	
	/** 그리드 검색조건 */
	@ApiModelProperty(value="")
	private String clearanceStatus;
	@ApiModelProperty(value="")
	private String cdType;
	@ApiModelProperty(value="")
	private String dropYN;
	@ApiModelProperty(value="")
	private String inspectionYN;
	@ApiModelProperty(value="")
	private String xray;
	@ApiModelProperty(value="")
	private String inspectionType;
	@ApiModelProperty(value="")
	private String seInspectionYN;
	@ApiModelProperty(value="")
	private String clearanceNoticeYN;
	@ApiModelProperty(value="필수값 타입")
	private String requiredType;
	@ApiModelProperty(value="신규여부 (bms 중복HAWB조회)")
	private String newYN;
	@ApiModelProperty(value="오류통보내역여부 (bms 통관목록 오류건 재전송)", example="Y or N")
	private String srchSimpleErrorDescYn;
	@ApiModelProperty(value="통관부호입력여부 (bms 통관목록 오류건 재전송)", example="Y or N")
	private String srchCustomsIdYn;
	@ApiModelProperty(value="통관부호구분 (bms 통관목록 오류건 재전송)")
	private String srchCustomsIdType;
	@ApiModelProperty(value="용도구분 (bms 통관목록 오류건 재전송)")
	private String srchCdUseType;
	
	
	/** 엑셀다운 */
	@ApiModelProperty(value="문서양식코드(엑셀다운로드)")
	private String documentCode;
	@ApiModelProperty(value="문서명(엑셀다운로드)")
	private String documentName;
	
	@ApiModelProperty(value="체크로 선택된 건 리스트")
	private List<String> dataList;
	@ApiModelProperty(value="쿼리에서 Master(MASTER_OD_JOB_NO) /House구분(OD_JOB_NO)", example="H")
	private String blTypeMh;	
	
	@ApiModelProperty(value="이례사항 페이지 조건 추가하기 위함")
	private String unusualYn;
	@ApiModelProperty(value="통관완료 페이지 조건 추가하기 위함")
	private String customCompleteYn;
	
	@ApiModelProperty(value="발송보류여부")
	private String srchSendYn;
	@ApiModelProperty(value="배송상태")
	private String srchDeliveryStatus;
	@ApiModelProperty(value="사용자거래처코드")
	private String srchEcmCustomerCode;
	@ApiModelProperty(value="반품Y/N")
	private String srchReturnYn;
	@ApiModelProperty(value="반송장유무")
	private String srchReturnDelNoYn;
	
	/** 로그 기록용 */
	@ApiModelProperty(value="사용자 로그인ID")
	private String loginId;
	
	@ApiModelProperty(value="profile명")
	private String profileName;
	
	/** 수출통관 엑셀용 */
	@ApiModelProperty(value="환율_USD")
	private double exchangeRateUsd;
	@ApiModelProperty(value="환율_JPY")
	private double exchangeRateJpy;
	
	/** 관세사 I/F */
	@ApiModelProperty(value="관세사 I/F")
	private String srchCbIfStatus;
	/** 출발 국가 */
	@ApiModelProperty(value="출발국가")
	private String srchOrigCountry;
}

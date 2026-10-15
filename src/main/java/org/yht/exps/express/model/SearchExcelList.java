package org.yht.exps.express.model;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchExcelList", description = "엑셀 다운로드할 데이터")
@Alias("searchExcelList")
public class SearchExcelList extends SearchList{
	
	@ApiModelProperty(value="화면 메뉴 아이디", example="OD, DELIVERY, BROKER")
	private String srchMenuId;	
	
	@ApiModelProperty(value="배송상태")
	private String srchStatus;
	@ApiModelProperty(value="배송사 코드", example="우체국, 대한통운")
	private String srchTrCode;	
	@ApiModelProperty(value="비용구분")
	private String srchDeliveryPaymentType; 
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;		
	@ApiModelProperty(value="조회업체타입")
	private String srchCusType;
	@ApiModelProperty(value="조회업체")
	private String srchCus;
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="MBL No")
	private String srchMblNo;
	@ApiModelProperty(value="조회번호 Like검색 여부", example="Y or N")
	private String srchNoLikeYn;
	@ApiModelProperty(value="")
	private String srchGroupCode;
	@ApiModelProperty(value="쿼리타입")
	private String srchQueryType;
	@ApiModelProperty(value="마스터관리번호")
	private String srchMasterOdJobNo;
	@ApiModelProperty(value="조회대상 Like검색 여부", example="Y or N")
	private String srchCusLikeYn;
	@ApiModelProperty(value="사업장코드", example="YHTXX-KR")
	private String srchEntBusinessCode;
	@ApiModelProperty(value="항공/해상,수출/수입구분", example="AO")
	private String srchBoundType;	
	
	@ApiModelProperty(value="SOLUTION", example="ECM")
	private String srchSolution;
	@ApiModelProperty(value="Master/House구분", example="H")
	private String srchBlTypeMh;
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
	@ApiModelProperty(value="출발지")
	private String srchOrigCode;
	@ApiModelProperty(value="도착지")
	private String srchDestCode;
	@ApiModelProperty(value="마스터관리번호")
	private String srchModJobNo;
	@ApiModelProperty(value="관리번호")
	private String srchOdJobNo;
	@ApiModelProperty(value="이례사항여부", example="Y or N")
	private String srchAbnormalityYn;
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	@ApiModelProperty(value="조회일자타입", example="dateTime")
	private String srchDateType;
	@ApiModelProperty(value="바운드타입reverse여부", example="Y or N")
	private String srchRvsBoundType;
	@ApiModelProperty(value="수출/수입구분", example="EXP or IMP")
	private String srchBoundType2;
	@ApiModelProperty(value="통관상태")
	private String srchClearanceStatus;	
	@ApiModelProperty(value="신고상태 리스트(멀티체크)")
	private List<String> srchClearanceStatusList;
	@ApiModelProperty(value="진행사항 리스트(멀티체크)")
	private List<String> srchLastPodStatusList;
	@ApiModelProperty(value="통관구분 리스트(멀티체크)")
	private List<String> srchCdTypeList;
	
	@ApiModelProperty(value="마스터관리번호리스트")
	private String srchMasterOdJobNoListStr;
	@ApiModelProperty(value="하우스관리번호")
	private String srchHouseOdJobNo;
	@ApiModelProperty(value="송장출력여부", example="Y or N")
	private String printYn;
	@ApiModelProperty(value="관세사코드")
	private String srchCbCode;
	@ApiModelProperty(value="관세사코드")
	private String cbCode;	
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;	
	@ApiModelProperty(value="변경배송사여부")
	private String srchChanged;	
	@ApiModelProperty(value="상담유형")
	private String srchCsType;
	@ApiModelProperty(value="배송사")
	private String srchDeliveryCode;
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
	@ApiModelProperty(value="상담유형 리스트(멀티체크)")
	private List<String> srchCsTypeList;
	@ApiModelProperty(value="배송사 리스트(멀티체크)")
	private List<String> srchDeliveryCodeList;
	@ApiModelProperty(value="개인통관부호 리스트(멀티체크)")
	private List<String> srchCustomsIdVerifyList;	
	@ApiModelProperty(value="도입사계약일자")
	private String contractDate;
	@ApiModelProperty(value="업데이트컬럼")
	private String updateField;
	@ApiModelProperty(value="업데이트값")
	private String updateValue;
	@ApiModelProperty(value="등록자")
	private String registerUserId;
	@ApiModelProperty(value="코드.명 출력여부")
	private String printCodeAndNameYn;
	@ApiModelProperty(value="코드명조회시 중복컬럼 Alias")
	private String codeAlias;
	@ApiModelProperty(value="통관지연사유코드")
	private String clearanceViolationCode;
	@ApiModelProperty(value="통관지연사유")
	private String clearanceViolationReason;
	@ApiModelProperty(value="엑셀 템플릿 코드")
	private String documentCode;
	@ApiModelProperty(value="엑셀 문서명")
	private String documentName;
	@ApiModelProperty(value="체크로 선택된 건 리스트")
	private List<String> dataList;	
	@ApiModelProperty(value="쿼리에서 Master(MASTER_OD_JOB_NO) /House구분(OD_JOB_NO)", example="H")
	private String blTypeMh;
	@ApiModelProperty(value="마감여부")
	private String srchFinishYn;
	@ApiModelProperty(value="관세요금안내여부", example="Y or N")
	private String srchClearanceNoticeYn;
	@ApiModelProperty(value="배송사여부")
	private String srchDeliveryCodeYn;
	@ApiModelProperty(value="배송사지정유무")
	private String deliveryCodeYn;
	@ApiModelProperty(value="배송상태")
	private String deliveryStatus;
	@ApiModelProperty(value="데이터조회하지않음")
	private String srchNoRecords;
	@ApiModelProperty(value="변경배송사여부")
	private String srchDeliveryNameChangedYn;
	@ApiModelProperty(value="변경배송주소여부")
	private String srchDeliveryAddressChangedYn;
	@ApiModelProperty(value="적하정정관리번호")
	private String srchSeqNoId;
	@ApiModelProperty(value="검사,선별등 중복조회여부")
	private String srchMultiStatusYn;
	@ApiModelProperty(value="적하정정관리번호조회여부")
	private String srchModJobNoYn;
	@ApiModelProperty(value="적하정정관리번호조회타입")
	private String srchModType;
	
	@ApiModelProperty(value="아이템 테이블 조회 유무")
	private String srchItemTableYn;
	@ApiModelProperty(value="아이템 원산지 코드명 조회 여부")
	private String srchNameItemOriginCountryYn;
	@ApiModelProperty(value="아이템 원산지 코드.코드명 조회 여부")
	private String srchCodeNameItemOriginCountryYn;
	
	@ApiModelProperty(value="컨테이너 테이블 조회 유무")
	private String srchContTableYn;
	@ApiModelProperty(value="컨테이너 컨테이너종류 코드명 조회 여부")
	private String srchNameCntrCodeYn;
	@ApiModelProperty(value="컨테이너 컨테이너종류 코드.코드명 조회 여부")
	private String srchCodeNameCntrCodeYn;
	
	@ApiModelProperty(value="멀티 테이블 조회 유무")
	private String srchMultiTableYn;
	@ApiModelProperty(value="멀티 변경배송사 코드명 조회 여부")
	private String srchNameMultiDeliveryCodeYn;
	@ApiModelProperty(value="멀티 변경배송사 코드.코드명 조회 여부")
	private String srchCodeNameMultiDeliveryCodeYn;
	@ApiModelProperty(value="멀티 결제조건 코드명 조회 여부")
	private String srchNameMultiPaymentGubnYn;
	@ApiModelProperty(value="멀티 결제조건 코드.코드명 조회 여부")
	private String srchCodeNameMultiPaymentGubnYn;
	@ApiModelProperty(value="멀티 거래처 코드명 조회 여부")
	private String srchNameMultiCustomerCodeYn;
	@ApiModelProperty(value="멀티 거래처 코드.코드명 조회 여부")
	private String srchCodeNameMultiCustomerCodeYn;
	
	@ApiModelProperty(value="마스터정보 조회 유무")
	private String srchMasterYn;	
	@ApiModelProperty(value="cost 테이블 조회 유무")
	private String srchCostTableYn;
	@ApiModelProperty(value="cost 테이블 조회 리스트")
	private List<Map<String, Object>> srchCostTableList;
	@ApiModelProperty(value="코드명컬럼 리스트")
	private List<Map<String, Object>> codeNameColumnList;
	@ApiModelProperty(value="홀드 테이블 조회 유무")
	private String srchHoldTableYn;
	
	@ApiModelProperty(value="면장정보 테이블 조회 유무")
	private String srchLicenseTableYn;
	
	@ApiModelProperty(value="거래구분")
	private String srchEcmType;
	@ApiModelProperty(value="거래유형")
	private String srchEcmBuyType;
	@ApiModelProperty(value="조회타입")
	private String srchType;
	
	@ApiModelProperty(value="오더 테이블 컬럼 리스트 ")
	private List<String> odColumnList;	
	@ApiModelProperty(value="additional 테이블 컬럼 리스트 ")
	private List<String> adColumnList;
	@ApiModelProperty(value="오더, additional 테이블에 없는 컬럼 리스트 ")
	private List<String> notOdAdColumnList;	
	@ApiModelProperty(value="선택된 odJobNo 리스트")
	private List<String> srchOdJobNoList;	
	@ApiModelProperty(value="반출입구분(멀티체크)")
	private List<String> srchCarryList;
	
	
	@ApiModelProperty(value="이례사항 페이지 조건 추가하기 위함")
	private String unusualYn;	
	@ApiModelProperty(value="통관완료 페이지 조건 추가하기 위함")
	private String customCompleteYn;
	@ApiModelProperty(value="로케이션")
	private String srchWhLocation;
	
	/** OrderMapper에 없고 OrderCustomerMapper에 있는내용추가 */
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
	@ApiModelProperty(value="발송보류여부")
	private String srchSendYn;
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
	@ApiModelProperty(value="로그인사용자상위거래처Lev")
	private String loginCustomerUpperLev;
	@ApiModelProperty(value="로그인사용자하위거래처Lev")
	private String loginCustomerLowLev;
	@ApiModelProperty(value="상태디폴트")
	private String statusDefault;
	
	@ApiModelProperty(value="통관상태")
	private String clearanceStatus;
	@ApiModelProperty(value="통관타입")
	private String cdType;
	@ApiModelProperty(value="취하_여부")
	private String dropYN;
	@ApiModelProperty(value="검사_여부")
	private String inspectionYN;
	@ApiModelProperty(value="X-RAY_여부")
	private String xray;
	@ApiModelProperty(value="검역구분_코드")
	private String inspectionType;
	@ApiModelProperty(value="선별_여부")
	private String seInspectionYN;
	@ApiModelProperty(value="관부가세안내여부")
	private String clearanceNoticeYN;

	@ApiModelProperty(value="송하인명")
	private String srchShipperName;
	@ApiModelProperty(value="수하인명")
	private String srchConsigneeName;
	@ApiModelProperty(value="제출번호리스트")
	private List<String> srchSimpleDocNoList;
	
	@ApiModelProperty(value="EMS환적여부")
	private String srchEmsYn;
	@ApiModelProperty(value="사용자거래처코드")
	private String srchEcmCustomerCode;
	@ApiModelProperty(value="반품Y/N")
	private String srchReturnYn;
	@ApiModelProperty(value="반송장유무")
	private String srchReturnDelNoYn;
	@ApiModelProperty(value="검색HblNo")
	private String srchHblNo;
	@ApiModelProperty(value="검색PoNo")
	private String srchPoNo;
	@ApiModelProperty(value="관세사 I/F")
	private String srchCbIfStatus;
	@ApiModelProperty(value="배송상태")
	private String srchDeliveryStatus;
	@ApiModelProperty(value="전자상거래 여부 ")
	private String srchEcmYn;
	@ApiModelProperty(value="화물구분")
	private String srchBlTypeIert;
}

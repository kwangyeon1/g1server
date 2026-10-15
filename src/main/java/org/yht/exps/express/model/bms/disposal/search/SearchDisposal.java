package org.yht.exps.express.model.bms.disposal.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=true)
@ApiModel(value = "SearchDisposal", description = "재고 조회 조건")
@Alias("searchDisposal")
public class SearchDisposal extends SearchList{
	
	@ApiModelProperty(value="조회일자 구분")
	private String srchDateGbn;
	@ApiModelProperty(value="조회 시작일자")
	private String srchStartDate;
	@ApiModelProperty(value="조회 종료일자")
	private String srchEndDate;

	@ApiModelProperty(value="로그인사용자", example="201808-001")
	private String srchUserId;
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="도입사사업장코드", example="YHTXX-KR")
	private String srchEntBusinessCode;

	@ApiModelProperty(value="재고 상태") // STOCK, DISPOSAL
	private String srchCarryInState;

	@ApiModelProperty(value="재고 조회 번호")
	private Integer srchInNo;
	@ApiModelProperty(value="화물관리번호")
	private String cargoReferenceNo;

	@ApiModelProperty(value="폐기신정 조회 번호")
	private String srchDisposalNo;
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	@ApiModelProperty(value="조회번호 실제 번호", example="Bl No")
	private String srchContent;
	@ApiModelProperty(value="수출입구분", example="1.해상,3.항공")
	private String srchImportExportClass;

	@ApiModelProperty(value="외국어구분(KOR,ENG)")
	private String userLanguage;
////


	@ApiModelProperty(value="반입등록여부", example="반입전 or 반입후")
	private String srchInYn;
	@ApiModelProperty(value="용도구분", example="1 or 2")
	private String srchCdUseType;
	@ApiModelProperty(value="통관구분", example="일반 or 목록")
	private String srchCdType;
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

	@ApiModelProperty(value="멀티운송장여부", example="Y or N")
	private String srchMultiYn;
	@ApiModelProperty(value="홀드여부", example="Y or N")
	private String srchHoldYn;
	@ApiModelProperty(value="통관부호 검증상태")
	private String srchCustomsIdVerify;
	@ApiModelProperty(value="SMS안내여부", example="Y or N")
	private String srchSmsInformYn;
	@ApiModelProperty(value="관세요금안내여부", example="Y or N")
	private String srchClearanceNoticeYn;
	@ApiModelProperty(value="마스터번호")
	private String srchMblNo;
	@ApiModelProperty(value="조회번호")
	private String srchNo;

	@ApiModelProperty(value="마감여부")
	private String srchFinishYn;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;	
	@ApiModelProperty(value="선택된 odJobNo 리스트")
	private List<String> srchOdJobNoList;	
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
	@ApiModelProperty(value="데이터조회하지않음")
	private String srchNoRecords;
	@ApiModelProperty(value="검사,선별등 중복조회여부")
	private String srchMultiStatusYn;
	@ApiModelProperty(value="적하정정관리번호조회여부")
	private String srchModJobNoYn;
	@ApiModelProperty(value="적하정정관리번호조회타입")
	private String srchModType;
	
	
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

	@ApiModelProperty(value="반입구분", example="입항반입 or 무적화물반입 or ...")
	private String srchCarryInType;
	//거래처 검색
	@ApiModelProperty(value="거래처구분", example="화주 or 송하인 or 수하인...")
	private String srchCustomerType;
	@ApiModelProperty(value="검색거래처코드(화주)", example="거래처코드(화주)")
	private String srchCustomerCode;
	@ApiModelProperty(value="검색거래처명(화주)", example="거래처명(화주)")
	private String srchCustomerName;
	@ApiModelProperty(value="검색거래처코드(송화인)", example="거래처코드(송화인)")
	private String srchShipperCode;
	@ApiModelProperty(value="검색거래처명(송화인)", example="거래처명(송화인)")
	private String srchShipperName;
	@ApiModelProperty(value="검색거래처코드(수화인)", example="거래처코드(수화인)")
	private String srchConsigneeCode;
	@ApiModelProperty(value="검색거래처명(수화인)", example="거래처명(수화인)")
	private String srchConsigneeName;
	@ApiModelProperty(value="검색거래처코드(통지처)", example="거래처코드(통지처)")
	private String srchNotifyCode;
	@ApiModelProperty(value="검색거래처명(통지처)", example="거래처명(통지처)")
	private String srchNotifyName;
	@ApiModelProperty(value="검색거래처코드(포워더)", example="거래처코드(포워더)")
	private String srchFwdCode;
	@ApiModelProperty(value="검색거래처명(포워더)", example="거래처명(포워더)")
	private String srchFwdName;
	@ApiModelProperty(value="검색거래처명(배송사)", example="거래처명(포워더)")
	private String srchDeliveryName;
	@ApiModelProperty(value="검색거래처명(장치위치)", example="거래처명(장치위치)")
	private String srchLocationPlaceName;
	@ApiModelProperty(value="검색거래처명(비고1)", example="거래처명(비고1)")
	private String srchRemarks1Name;
	@ApiModelProperty(value="검색거래처명(비고2)", example="거래처명(비고2)")
	private String srchRemarks2Name;
	@ApiModelProperty(value="검색거래처명(비고3)", example="거래처명(비고3)")
	private String srchRemarks3Name;
	@ApiModelProperty(value="검색거래처명(품명)", example="거래처명(품명)")
	private String srchCommodityItemLoc;

	/** 엑셀다운 */
	@ApiModelProperty(value="문서양식코드(엑셀다운로드)")
	private String documentCode;
	@ApiModelProperty(value="문서명(엑셀다운로드)")
	private String documentName;

	@ApiModelProperty(value="엑셀타이틀명")
	private String excelTitle;
	@ApiModelProperty(value="엑셀다운여부")
	private String excelDownYn;

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

	@ApiModelProperty(value="다국어 관련 코드")
	private String srchUserLanguage;
	
}

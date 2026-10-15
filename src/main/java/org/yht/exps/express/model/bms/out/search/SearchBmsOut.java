package org.yht.exps.express.model.bms.out.search;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchBmsOut", description = "반출 조회 조건")
@Alias("searchBmsOut")
public class SearchBmsOut extends SearchList{

	@ApiModelProperty(value="조회일자 구분")
	private String srchDateGbn;
	@ApiModelProperty(value="조회 시작일자")
	private String srchStartDate;
	@ApiModelProperty(value="조회 종료일자")
	private String srchEndDate;

	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
    @ApiModelProperty(value="사업장 보세 구역번호 8자리")
    private String srchBmsWhCode;

	@ApiModelProperty(value="대표사업장코드", example="YHTXX")
	private String srchEntBusinessCode;

	@ApiModelProperty(value="재고 상태") // STOCK, OUT, OUT_APPROVAL(반출승인), CANCEL
	private String srchCarryInState;

	@ApiModelProperty(value="재고 조회 번호")
	private Integer srchInNo;

	@ApiModelProperty(value="예상창고료번호")
	private  Integer   srchEstimateNo ;

	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;

	@ApiModelProperty(value="반출 조회 번호")
	private Integer srchOutNo;
	@ApiModelProperty(value="반출 승인내역 번호")
	private Integer srchOutPlanNo;
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	@ApiModelProperty(value="조회번호 실제 번호", example="Bl No")
	private String srchContent;
	@ApiModelProperty(value="수출입구분", example="1.항공,3.해상")
	private String srchImportExportClass;

	@ApiModelProperty(value="외국어구분(KOR,ENG)")
	private String userLanguage;
////
	@ApiModelProperty(value="Master/House구분", example="H")
	private String srchBlTypeMh;
	@ApiModelProperty(value="조회일자타입", example="dateTime")
	private String srchDateType;

	@ApiModelProperty(value="반입등록여부", example="반입전 or 반입후")
	private String srchInYn;
	@ApiModelProperty(value="용도구분", example="1 or 2")
	private String srchCdUseType;
	@ApiModelProperty(value="통관구분", example="일반 or 목록")
	private String srchCdType;
	@ApiModelProperty(value="SOLUTION", example="ECM")
	private String srchSolution;
	
	@ApiModelProperty(value="납세의무자사업자번호")
	private String srchCompanyBusinessNo;
	@ApiModelProperty(value="정정전원본 버튼을 통한 열람여부")
	private String srchCarryInOpenType;

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
	
	/** 반출관련 항목 */
	@ApiModelProperty(value="화물관리번호")
	private String cargoReferenceNo;

	@ApiModelProperty(value="세관제출번호")
	private String srchDeclarationNo;
	
	/** 견품반출 항목 */
	@ApiModelProperty(value="화물관리번호")
	private Integer sampleNo;
	
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


	@ApiModelProperty(value="다국어 관련 코드")
	private String srchUserLanguage;

	@ApiModelProperty(value="BlNo 번호")
	private String srchBlNo;
	
    @ApiModelProperty(value="일자별 다운로드여부")
    private String dayByDayDownloadYn;

	/** 반출신고 조회 */
	@ApiModelProperty(value="반출 조회 구분") // PLAN(반출승인), OUT(반출), CANCEL(승인취하), STOCK(재고)
	private String srchOutApprovalStatus;
	@ApiModelProperty(value="반출 진행상태") // OUT(반출)일 경우에만 사용
	private String srchLastPodStatus;
	@ApiModelProperty(value="반출등록여부", example="반출전 or 반출후")
	private String srchOutYn;
	@ApiModelProperty(value="기타구분(멀티,현도)" , example="multi or assign")
    private String srchEtcGubn;
	@ApiModelProperty(value="거대화물여부", example="'' or Y or N")
	private String srchBigSizeYn;
	@ApiModelProperty(value="거대멀티이고 멀티체크 리스트")
	private List<String> srchBigSizeYnList;
	@ApiModelProperty(value="검역구분 멀티체크 리스트")
	private List<String> srchInspectionTypeList;
	@ApiModelProperty(value="선별검사 멀티체크 리스트")
	private List<String> srchSeInspectionCdList;
	@ApiModelProperty(value="XRAY검사 멀티체크 리스트")
	private List<String> srchXrayInspectionList;
	@ApiModelProperty(value="XRAY호기 멀티체크 리스트")
	private List<String> srchXrayNoList;
	@ApiModelProperty(value="홀드이상 멀티체크 리스트")
	private List<String> srchHoldCodeList;
	@ApiModelProperty(value="반출구분")
	private String srchCarryOutType;

	@ApiModelProperty(value="반입구분")
	private String srchCarryInType;
	@ApiModelProperty(value="거래처구분")
	private String srchCustomerGuBun;
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
	@ApiModelProperty(value="검색거래처코드(배송사)", example="거래처코드(배송사)")
	private String srchDeliveryCode;
	@ApiModelProperty(value="검색거래처명(배송사)", example="거래처명(배송사)")
	private String srchDeliveryName;
	@ApiModelProperty(value="검색거래처코드(포워더)", example="거래처코드(포워더)")
	private String srchFwdCode;
	@ApiModelProperty(value="검색거래처명(포워더)", example="거래처명(포워더)")
	private String srchFwdName;
	@ApiModelProperty(value="검색거래처코드(관세사)", example="거래처코드(관세사)")
	private String srchBrokerCode;
	@ApiModelProperty(value="검색거래처명(관세사)", example="거래처명(관세사)")
	private String srchBrokerName;
	@ApiModelProperty(value="검색거래처명(장치위치)", example="거래처명(장치위치)")
	private String srchLocationPlaceName;
	@ApiModelProperty(value="검색거래처명(비고1)", example="거래처명(비고1)")
	private String srchRemarks1Name;
	@ApiModelProperty(value="검색거래처명(비고2)", example="거래처명(비고2)")
	private String srchRemarks2Name;
	@ApiModelProperty(value="검색거래처명(비고3)", example="거래처명(비고3)")
	private String srchRemarks3Name;
	@ApiModelProperty(value="거래처명(반입비고)", example="거래처명(반입비고)")
	private String srchRemarksInName;
	@ApiModelProperty(value="거래처명(반출비고)", example="거래처명(반출비고)")
	private String srchRemarksOutName;

	@ApiModelProperty(value="검색거래처명(품명)", example="거래처명(품명)")
	private String srchCommodityItemLoc;
	@ApiModelProperty(value="검색거래처명(품명)", example="거래처명(품명)")
	private String srchCommodityItemLocName;
	
	@ApiModelProperty(value="검색참조번호", example="검색참조번호")
    private String srchReferenceNo;

	@ApiModelProperty(value="BL번호, 신고번호등 Like 검색 유무", example="YN")
	private String srchNoLikeYn;
	
	@ApiModelProperty(value="OUT/RETURN", example="견본반출상태")
	private String srchSampleGubn;
	
	@ApiModelProperty(value="이고차수")
	private Integer srchTicOutSeq;
	
	@ApiModelProperty(value="PDA 스캔여부", example="공백, Y")
	private String srchPdaScanYn;
	
	@ApiModelProperty(value="택배구분")
	private String srchDeliveryTypeGbn;
	
	@ApiModelProperty(value="택배전송상태")
	private String srchSendTypeGbn;
	
	@ApiModelProperty(value="택배 구분")
	private String srchDeliveryType;
	
	@ApiModelProperty(value="전송상태 구분")
	private String srchSendType;

    /** Web XML Send */
	@ApiModelProperty(value="outNo 리스트")
	private List<Integer> outNoList;
}

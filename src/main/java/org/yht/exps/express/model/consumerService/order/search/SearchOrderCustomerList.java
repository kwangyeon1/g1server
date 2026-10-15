package org.yht.exps.express.model.consumerService.order.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchOrderCustomerList", description = "오더조회")
@Alias("searchOrderCustomerList")
public class SearchOrderCustomerList extends SearchList { 
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	
	@ApiModelProperty(value="항공/해상", example="AE")
	private String srchBound;
	@ApiModelProperty(value="수출/수입구분", example="AE")
	private String srchBoundType;
	@ApiModelProperty(value="콘솔여부", example="Y or N")
	private String srchConsolYn;
	@ApiModelProperty(value="통관구분")
	private String srchCdType;
	@ApiModelProperty(value="출력여부")
	private String srchPrintYn;
	@ApiModelProperty(value="상태")
	private String srchStatus;
	@ApiModelProperty(value="상태 리스트(멀티체크)")
	private List<String> srchStatusList;
	@ApiModelProperty(value="상태 제외 리스트")
	private List<String> srchStatusNotList;
	@ApiModelProperty(value="발송보류여부")
	private String srchSendYn;
	@ApiModelProperty(value="상태디폴트")
	private String statusDefault;
	@ApiModelProperty(value="검사,선별등 중복조회여부")
	private String srchMultiStatusYn;
	@ApiModelProperty(value="선별대상여부", example="Y or N")
	private String srchSeInspectionYn;
	@ApiModelProperty(value="검사여부", example="Y or N")
	private String srchInspectionYn;
	@ApiModelProperty(value="X-Ray여부", example="Y or N")
	private String srchXrayYn;
	@ApiModelProperty(value="취하(보류통보)여부", example="Y or N")
	private String srchDropYn;
	@ApiModelProperty(value="통관목록신고상태")
	private String srchSimpleEdiStatus;
	@ApiModelProperty(value="수입신고불가여부")
	private String srchSimpleStatementYn;
	@ApiModelProperty(value="이례사항여부")
	private String srchAbnormalYn;
	@ApiModelProperty(value="데이터조회하지않음")
	private String srchNoRecords;
	@ApiModelProperty(value="조회번호 Like검색 여부", example="Y or N")
	private String srchNoLikeYn;	
	@ApiModelProperty(value="SMS처리안내여부")
	private String srchSmsInformYn;
	@ApiModelProperty(value="멀티운송장여부", example="Y or N")
	private String srchMultiYn;
	@ApiModelProperty(value="반품Y/N")
	private String srchReturnYn;
	@ApiModelProperty(value="반송장유무")
	private String srchReturnDelNoYn;
	@ApiModelProperty(value="배송사코드")
	private String srchDeliveryCode;
		
//	@ApiModelProperty(value="언어", example="KOR")
//	private String userLanguage;
	
	@ApiModelProperty(value="로그인id")
	private String srchLoginId;
	@ApiModelProperty(value="로그인거래처코드id")
	private String loginCustomerCd;
	@ApiModelProperty(value="로그인거래처Lev")
	private String loginCustomerLev;
	@ApiModelProperty(value="로그인사용자상위거래처Lev")
	private String loginCustomerUpperLev;
	@ApiModelProperty(value="로그인사용자하위거래처Lev")
	private String loginCustomerLowLev;
	@ApiModelProperty(value="로그인거래처Lev 존재여부")
	private String loginCustomerLevNan;
	
	@ApiModelProperty(value="검색거래처코드", example="거래처코드")
	private String srchCustomerCode;
	@ApiModelProperty(value="검색거래처명", example="거래처명")
	private String srchCustomerName;
	@ApiModelProperty(value="검색입력자명", example="입력자명")
	private String srchRegisterUserName;
	
	@ApiModelProperty(value="통관부호 검증상태")
	private String srchCusIdVerify;
	@ApiModelProperty(value="통관부호 리스트(멀티체크)")
	private List<String> srchCusIdVerifyList;
	@ApiModelProperty(value="반출입구분(멀티체크)")
	private List<String> srchCarryList;
	
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;		
	@ApiModelProperty(value="odJobNo")
	private String srchOdJobNo;
	@ApiModelProperty(value="odJobNo리스트")
	private List<String> srchOdJobNoList;		
	
	@ApiModelProperty(value="조회업체타입")
	private String srchCusType;
	
	@ApiModelProperty(value="셋팅번호")
	private String putNo;
	
	@ApiModelProperty(value="func")
	private String func;
			
	@ApiModelProperty(value="로그인거래처 국가코드")
	private String loginCountryCode;
	
	@ApiModelProperty(value="통관구분 리스트 (멀티체크)")
	private List<String> srchCdTypeList;
	
	
//	@ApiModelProperty(value="조회데이터 사용용도")
//	private String srchMode;	
	
	
	@ApiModelProperty(value="호출화면 (등록,발송 화면에서 호출시 ORDER_CUSTOMER")
	private String screen;
	@ApiModelProperty(value="이례사항 페이지 조건 추가하기 위함")
	private String unusualYn;	
	@ApiModelProperty(value="통관완료 페이지 조건 추가하기 위함")
	private String customCompleteYn;
	
	@ApiModelProperty(value="사용자로그인ID - 로그 기록")
	private String loginId;
	
	@ApiModelProperty(value="profile명")
	private String profileName;
	
	@ApiModelProperty(value="검사구분")
	private String srchInputMethod;
	@ApiModelProperty(value="취하사유")
	private String srchDropReason;
	@ApiModelProperty(value="취하사유")
	private String srchDropReasonStr;
}

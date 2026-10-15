package org.yht.exps.express.model.eCommerce.transaction.search;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchTransactionList", description = "전자상거래 거래정보 제출 검색조건")
@Alias("searchTransactionList")
public class SearchTransactionList extends SearchList {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="거래처코드")
	private String srchEntBusinessCode;
	@ApiModelProperty(value="검색타입")
	private String srchLicenseType;
	@ApiModelProperty(value="검색텍스트")
	private String srchLicenseText;
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;
	@ApiModelProperty(value="바운드타입")
	private String srchBoundType;
	@ApiModelProperty(value="조회업체타입")
	private String srchCusType;
	@ApiModelProperty(value="HBL NO")
	private String srchHblNo;
	@ApiModelProperty(value="오더관리번호")
	private String srchOdJobNo;
	@ApiModelProperty(value="거래처코드")
	private String srchCustomerCode;
	@ApiModelProperty(value="상위업체")
	private String srchCuslv1;
	@ApiModelProperty(value="하위업체")
	private String srchCuslv2;
	@ApiModelProperty(value="수신상태")
	private String srchResultStatus;
	@ApiModelProperty(value="송신상태")
	private String srchSendResultCode;
	@ApiModelProperty(value="신고여부")
	private String srchSendStatus;

	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="오더번호")
	private String poNo;
	@ApiModelProperty(value="오더번호 리스트")
	private List<String> srchOdJobNoList;
	@ApiModelProperty(value="검색 리스트")
	private List<String> srchTransactionList;
	@ApiModelProperty(value="삭제 리스트")
	private List<Map<String,Object>> transNoList;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="개인통관부호재검증 여부")
	private String reCheckYn;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
	@ApiModelProperty(value="로그인한 업체코드")
	private String loginCustomerCd;
	@ApiModelProperty(value="로그인한 업체레벨")
	private String loginCustomerLev;
	@ApiModelProperty(value="로그인사용자상위거래처Lev")
	private String loginCustomerUpperLev;
	@ApiModelProperty(value="로그인사용자하위거래처Lev")
	private String loginCustomerLowLev;
}

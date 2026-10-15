package org.yht.exps.express.model.edi.search;

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
@ApiModel(value = "SearchManifestInList", description = "전자상거래 적하목록 수입 리스트 검색 조건")
@Alias("searchManifestInList")
public class SearchManifestInList extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="바운드")
	private String srchBoundType;
	@ApiModelProperty(value="검색타입")
	private String srchNoType;
	@ApiModelProperty(value="검색text")
	private String srchNo;
	@ApiModelProperty(value="조회일자타입", example="aMonthChk")
	private String srchDateType;
	
	@ApiModelProperty(value="로그인id")
	private String srchLoginId;
	@ApiModelProperty(value="로그인거래처코드id")
	private String srchLoginCustomerCd;
	@ApiModelProperty(value="로그인거래처Lev")
	private String srchLoginCustomerLev;
	@ApiModelProperty(value="로그인사용자상위거래처Lev")
	private String srchLoginCustomerUpperLev;
	@ApiModelProperty(value="로그인사용자하위거래처Lev")
	private String srchLoginCustomerLowLev;
	
	@ApiModelProperty(value="마스터오더번호", example="COD210713-0000002")
	private String masterOdJobNo;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;
	@ApiModelProperty(value="통관구분 리스트(멀티체크)")
	private List<String> srchCdTypeList;
	@ApiModelProperty(value="반출입구분 리스트(멀티체크)")
	private List<String> srchCarryList;
	@ApiModelProperty(value="개인통관부호 리스트(멀티체크)")
	private List<String> srchCustomsIdVerifyList;
	@ApiModelProperty(value="거래구분 리스트(멀티체크)")
	private List<String> srchEcmTypeList;
	@ApiModelProperty(value="거래유형 리스트(멀티체크)")
	private List<String> srchEcmBuyTypeList;
	@ApiModelProperty(value="검사 리스트(멀티체크)")
	private List<String> srchMultiStatusList;
	
	@ApiModelProperty(value="통관구분")
	private String srchCdType;
	@ApiModelProperty(value="srchMenuId")
	private String srchMenuId;
	@ApiModelProperty(value="SMS안내여부", example="Y or N")
	private String srchSmsInformYn;
	@ApiModelProperty(value="검색타입")
	private String srchHNoType;
	@ApiModelProperty(value="검색text")
	private String srchHNo;
	@ApiModelProperty(value="개인통관부호상태")
	private String srchCustomsIdVerify;
	@ApiModelProperty(value="반출입구분")
	private String srchCarryDet;
	
	@ApiModelProperty(value="서브 그리드 여부")
	private String subGridYn;
	@ApiModelProperty(value="제출번호리스트")
	private List<String> srchSimpleDocNoList;
	@ApiModelProperty(value="제출번호")
	private String simpleDocNo;
	
	@ApiModelProperty(value="선별대상여부", example="Y or N")
	private String srchSeInspectionYn;
	@ApiModelProperty(value="검사여부", example="Y or N")
	private String srchInspectionYn;
	@ApiModelProperty(value="X-Ray여부", example="Y or N")
	private String srchXrayYn;
	@ApiModelProperty(value="취하(보류통보)여부", example="Y or N")
	private String srchDropYn;
	@ApiModelProperty(value="홀드여부", example="Y or N")
	private String srchHoldYn;
	@ApiModelProperty(value="하기이상여부", example="Y or N")
	private String srchAbnormalYn;
	@ApiModelProperty(value="오류통보", example="Y or N")
	private String srchSimpleEdiStatus;
	@ApiModelProperty(value="오류통보내역여부", example="Y or N")
	private String srchSimpleErrorDescYn;
	@ApiModelProperty(value="통관부호입력여부", example="Y or N")
	private String srchCustomsIdYn;
	@ApiModelProperty(value="통관부호구분")
	private String srchCustomsIdType;
	@ApiModelProperty(value="수입신고불가", example="Y or N")
	private String srchSimpleStatementYn;
	@ApiModelProperty(value="우범화물여부", example="Y or N")
	private String srchCriminalYn;
	@ApiModelProperty(value="Master/House구분", example="H")
	private String srchBlTypeMh;
	@ApiModelProperty(value="검사종류", example="Y or N")
	private String srchInspectionType;
	@ApiModelProperty(value="반입/미반입", example="Y or N")
	private String srchCarryin;
	@ApiModelProperty(value="반출/미반출", example="Y or N")
	private String srchCarryout;
	@ApiModelProperty(value="Packing No (UPS)", example="N")
	private String srchPackingYn;
	@ApiModelProperty(value="용도구분(UPS 통관목록 오류건 재전송 메뉴 조회조건)")
	private String srchCdUseType;
	
	@ApiModelProperty(value="마스트 리스트")
	private String srchMasterOdJobNoListStr;
	@ApiModelProperty(value="거래유형 리스트(멀티체크)")
	private List<String> srchMasterOdJobNoList;
	@ApiModelProperty(value="검색리턴여부")
	private String srchResultYn;
	@ApiModelProperty(value="거래유형 리스트(멀티체크)")
	private List<String> srchOdJobNoAndDocNoList;
	@ApiModelProperty(value="거래유형 리스트(멀티체크)")
	private List<Map<String, Object>> srchOdJobNoAndDocNoListSql;
	
	@ApiModelProperty(value="마스터수입페이지체크")
	private String masterImpChk;
	
	@ApiModelProperty(value="마스터관리번호")
	private String srchMasterOdJobNo;
}

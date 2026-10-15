package org.yht.exps.express.model.edi.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchManifestOutList", description = "전자상거래 적하목록 수출 리스트 검색 조건")
@Alias("searchManifestOutList")
public class SearchManifestOutList extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="바운드")
	private String srchBoundType;
	@ApiModelProperty(value="검색타입")
	private String srchNoType;
	@ApiModelProperty(value="검색text")
	private String srchNo;
	
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
	@ApiModelProperty(value="수신구분 리스트(멀티체크)")
	private List<String> srchEdiStatusList;
	@ApiModelProperty(value="신고처리상태(멀티체크)")
	private List<String> srchSimpleDeclarationStatusList;
	
	@ApiModelProperty(value="통관구분")
	private String srchCdType;
	@ApiModelProperty(value="검색타입")
	private String srchHNoType;
	@ApiModelProperty(value="검색text")
	private String srchHNo;
	
	@ApiModelProperty(value="서브 그리드 여부")
	private String subGridYn;
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
	@ApiModelProperty(value="하기이상여부", example="Y or N")
	private String srchAbnormalYn;
	@ApiModelProperty(value="오류통보", example="Y or N")
	private String srchSimpleEdiStatus;
	@ApiModelProperty(value="수입신고불가", example="Y or N")
	private String srchSimpleStatementYn;
	
	@ApiModelProperty(value="마스트 리스트")
	private String srchMasterOdJobNoListStr;	
	
	@ApiModelProperty(value="profile 명")
	private String profileName;
	
}

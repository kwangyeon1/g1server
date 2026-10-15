package org.yht.exps.express.model.edi.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchCarryoutInList", description = "전자상거래 적하목록 수입 리스트 검색 조건")
@Alias("searchCarryoutInList")
public class SearchCarryoutInList extends SearchList {
	
	// 적하목록(in)
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="마스터 리스트")
	private String srchMasterOdJobNoListStr;
	
	@ApiModelProperty(value="마스터관리번호")
	private String srchMasterOdJobNo;
	
	// 오더조회
	@ApiModelProperty(value="SOLUTION", example="ECM")
	private String srchSolution;
	@ApiModelProperty(value="Master/House구분", example="H")
	private String srchBlTypeMh;
	
	@ApiModelProperty(value="조회일자 구분")
	private String srchDateGbn;
	@ApiModelProperty(value="조회 시작일자")
	private String srchStartDate;
	@ApiModelProperty(value="조회 종료일자")
	private String srchEndDate;
	
	@ApiModelProperty(value="항공/해상,수출/수입구분", example="AO")
	private String srchBoundType;
	@ApiModelProperty(value="콘솔여부", example="Y or N")
	private String srchConsolYn;
	@ApiModelProperty(value="진행사항")
	private String srchLastPodStatus;
	@ApiModelProperty(value="진행사항(해당코드이후부터조회)")
	private String srchLastPodStatusMin;
	@ApiModelProperty(value="반입여부", example="Y or N")
	private String srchCarryinYn;
	@ApiModelProperty(value="반출여부", example="Y or N")
	private String srchCarryoutYn;
	@ApiModelProperty(value="통관구분")
	private String srchCdType;
	
	@ApiModelProperty(value="선별대상여부", example="Y or N")
	private String srchSeInspectionYn;
	@ApiModelProperty(value="검사여부", example="Y or N")
	private String srchInspectionYn;
	@ApiModelProperty(value="X-Ray여부", example="Y or N")
	private String srchXrayYn;
	@ApiModelProperty(value="취하(보류통보)여부", example="Y or N")
	private String srchDropYn;
	@ApiModelProperty(value="수입신고불가여부")
	private String srchSimpleStatementYn;
	@ApiModelProperty(value="통관목록신고상태")
	private String srchSimpleEdiStatus;
	@ApiModelProperty(value="제출번호")
	private String srchSimpleDocNo;
	@ApiModelProperty(value="srchMenuId")
	private String srchMenuId;
	@ApiModelProperty(value="이례사항여부")
	private String srchAbnormalYn;
	
	@ApiModelProperty(value="멀티운송장여부", example="Y or N")
	private String srchMultiYn;
	@ApiModelProperty(value="홀드여부", example="Y or N")
	private String srchHoldYn;
	@ApiModelProperty(value="통관부호 검증상태")
	private String srchCustomsIdVerify;
	@ApiModelProperty(value="SMS안내여부", example="Y or N")
	private String srchSmsInformYn;
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회업체타입")
	private String srchCusType;
	@ApiModelProperty(value="조회번호 Like검색 여부", example="Y or N")
	private String srchNoLikeYn;
	@ApiModelProperty(value="조회업체")
	private String srchCus;
	
	/** 엑셀다운 */
	@ApiModelProperty(value="문서양식코드(엑셀다운로드)")
	private String documentCode;
	@ApiModelProperty(value="문서명(엑셀다운로드)")
	private String documentName;
	
}

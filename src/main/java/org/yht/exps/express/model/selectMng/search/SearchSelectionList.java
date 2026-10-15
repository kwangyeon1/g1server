package org.yht.exps.express.model.selectMng.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchSelectionList", description = "목록선별 리스트 검색 조건")
@Alias("searchSelectionList")
public class SearchSelectionList extends SearchList {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="마스터오더관리번호")
	private String srchMasterOdJobNo;
	@ApiModelProperty(value="하우스오더관리번호")
	private String srchOdJobNo;
	@ApiModelProperty(value="하우스오더관리번호리스트")
	private List<String> srchOdJobNoList;
	@ApiModelProperty(value="조회타입")
	private String srchType;
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;
	@ApiModelProperty(value="선별옵션")
	private List<String> srchSelectionOption;
	@ApiModelProperty(value="검사옵션")
	private List<String> srchInspectionOption;
	@ApiModelProperty(value="선별타입")
	private String srchSelectionType;
	
	@ApiModelProperty(value="목록구분")
	private String srchCdType;
	@ApiModelProperty(value="용도구분")
	private String srchCdUseType;
	@ApiModelProperty(value="거래구분")
	private String srchEcmType;
	@ApiModelProperty(value="자동선별비율")
	private String srchRatio;
	@ApiModelProperty(value="검사")
	private String srchInspectionYn;
	@ApiModelProperty(value="우범")
	private String srchCrimeAreaYn;
	
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="작업자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}

package org.yht.exps.express.model.edi.abnormal.search;

import java.util.List;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.edi.abnormal.Abnormal;
import org.yht.exps.express.model.search.SearchList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchAbnormalList", description = "전자상거래 하기이상결과보고신고 리스트 검색 조건")
@Alias("searchAbnormalList")
public class SearchAbnormalList extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="MFN 검색타입")
	private String srchMfnType;
	@ApiModelProperty(value="검색타입")
	private String srchNoType;
	@ApiModelProperty(value="검색text")
	private String srchNo;
	@ApiModelProperty(value="자료생성구분")
	private String srchDataGen;
	@ApiModelProperty(value="별도관리해제")
	private String srchUnmng;
	@ApiModelProperty(value="적하목록정정")
	private String srchCorrection;	
	@ApiModelProperty(value="하기이상코드")
	private String srchAbnormalCode;
	@ApiModelProperty(value="MFN신고완료여부")
	private String srchAbnormalMfnYn;
	@ApiModelProperty(value="마스터오더관리번호")
	private String srchMasterOdJobNo;
	@ApiModelProperty(value="마스터비엘번호")
	private String srchMblNo;
	@ApiModelProperty(value="조회번호 Like검색 여부", example="Y or N")
	private String srchNoLikeYn;
	@ApiModelProperty(value="반입여부", example="ALL or Y or N")
	private String srchCarryInYn;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="항공/해상")
	private String srchBound;
	@ApiModelProperty(value="적하삭제여부")
	private String srchManifestDelYn;
	@ApiModelProperty(value="진행상태구분")
	private String srchLastPodStatus;
	@ApiModelProperty(value="신고여부")
	private String srchSendStatus;
	@ApiModelProperty(value="반입및MGN,MFN신고여부")
	private String srchInMgnMfnRaqYn;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;
			
	@ApiModelProperty(value="오더 리스트")
	private List<Abnormal> abnormalList;
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
	
}


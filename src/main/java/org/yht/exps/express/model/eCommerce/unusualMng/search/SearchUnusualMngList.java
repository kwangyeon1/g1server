package org.yht.exps.express.model.eCommerce.unusualMng.search;

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
@ApiModel(value = "SearchUnusualMngList", description = "이례화물관리 조회")
@Alias("searchUnusualMngList")
public class SearchUnusualMngList extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="바운드")
	private String srchBound;
	@ApiModelProperty(value="바운드타입")
	private String srchBoundType;
	@ApiModelProperty(value="hblNo")
	private String srchHblNo;
	@ApiModelProperty(value="반품상태")
	private String srchUnusual;
	@ApiModelProperty(value="조회업체타입")
	private String srchCusType;
	@ApiModelProperty(value="조회업체코드")
	private String srchCustomerCode;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="검색번호")
	private String srchNo;
	@ApiModelProperty(value="검색번호타입")
	private String srchNoType;
	@ApiModelProperty(value="로그인거래처코드id")
	private String loginCustomerCd;
	@ApiModelProperty(value="로그인거래처Lev")
	private String loginCustomerLev;
	@ApiModelProperty(value="blNo 리스트 (blNo 다건 조회시)")
	private List<String> srchBlNoList;
	
	@ApiModelProperty(value="도입사")
	private String entCode;
	@ApiModelProperty(value="userLanguage")
	private String userLanguage;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="입력자")
	private String registerUserId;
	@ApiModelProperty(value="리스트")
	private List<Map<String, Object>> dataList;
	
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자명")
	private String logUserNameEng;
}
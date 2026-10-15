package org.yht.exps.express.model.mobile.bms.search;


import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchMobCargo", description = "보세구역관리 모바일 화물 검색 ")
@Alias("searchMobCargo")
public class SearchMobCargo extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="하우스번호")
	private String hblNo;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="홀드여부")
	private String holdYn;
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	@ApiModelProperty(value="조회여부")
	private String outSrchYn;
	@ApiModelProperty(value="창고 LOCATION")
	private String whLocation;
	@ApiModelProperty(value="반출일자여부")
	private String carrydateYn;
	@ApiModelProperty(value="반출상태", example="반출_상태(P : 출고예정,C : 출고취소,F : 출고완료)")
	private String carryoutStatus;
	@ApiModelProperty(value="출고리스트")
	private String carryoutList;
	@ApiModelProperty(value="도착항")
	private String destCode;
	@ApiModelProperty(value="수출입 구분")
	private String srchBoundType;
	
	@ApiModelProperty(value="옵션코드")
	private String optionCode;
	@ApiModelProperty(value="솔루션")
	private String solution;
	@ApiModelProperty(value="도입사_사업장코드")
	private String entBusinessCode;
	@ApiModelProperty(value="출고처리")
	private String selectType;
	@ApiModelProperty(value="데이터구분")
	private String dGbn;
	@ApiModelProperty(value="검색번호")
	private String srchNo;
	@ApiModelProperty(value="BL중복 반입 검색구분")
	private String srchInGbn;
	@ApiModelProperty(value="BL중복 반입예정 검색구분")
	private String srchInPlanGbn;
	@ApiModelProperty(value="BL중복 오더 검색구분")
	private String srchOrderGbn;
}

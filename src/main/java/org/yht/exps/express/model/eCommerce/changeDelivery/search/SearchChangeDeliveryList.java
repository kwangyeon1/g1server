package org.yht.exps.express.model.eCommerce.changeDelivery.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchChangeDeliveryList", description = "송장교체(주소변경) 조회")
@Alias("searchChangeDeliveryList")
public class SearchChangeDeliveryList extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="바운드")
	private String srchBound;
	@ApiModelProperty(value="바운드타입")
	private String srchBoundType;
	@ApiModelProperty(value="hblNo")
	private String srchHblNo;
	@ApiModelProperty(value="조회업체타입")
	private String srchCusType;
	@ApiModelProperty(value="조회업체코드")
	private String srchCustomerCode;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="보세유형조건")
	private String srchChangeDeliveryType;
	@ApiModelProperty(value="보세유형내용")
	private String srchChangeDelivery;
	@ApiModelProperty(value="검색번호")
	private String srchNo;
	@ApiModelProperty(value="검색번호타입")
	private String srchNoType;
	@ApiModelProperty(value="로그인거래처코드id")
	private String loginCustomerCd;
	@ApiModelProperty(value="로그인거래처Lev")
	private String loginCustomerLev;
	@ApiModelProperty(value="일련번호")
	private String seqNoId;
	@ApiModelProperty(value="blNo 리스트 (blNo 다건 조회시)")
	private List<String> srchBlNoList;
	@ApiModelProperty(value="SMS안내여부", example="Y or N")
	private String srchSmsInformYn;
	
}
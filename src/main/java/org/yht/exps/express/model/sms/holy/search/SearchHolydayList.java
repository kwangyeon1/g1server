package org.yht.exps.express.model.sms.holy.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchHolydayList", description = "공휴일 조회")
@Alias("SearchHolydayList")
public class SearchHolydayList extends SearchList { 
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	
	@ApiModelProperty(value="국가", example="KR")
	private String srchCountry;
	@ApiModelProperty(value="공휴일타입", example="H")
	private String srchHolydayType;
	@ApiModelProperty(value="공휴일명", example="")
	private String srchHolydayName;
	
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;
}

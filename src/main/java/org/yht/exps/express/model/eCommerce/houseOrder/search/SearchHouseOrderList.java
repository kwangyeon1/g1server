package org.yht.exps.express.model.eCommerce.houseOrder.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchHouseOrderList", description = "오더조회")
@Alias("searchHouseOrderList")
public class SearchHouseOrderList extends SearchList {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="SOLUTION", example="ECM")
	private String srchSolution;
	@ApiModelProperty(value="Master/House구분", example="H")
	private String srchBlTypeMh;
	@ApiModelProperty(value="조회일자타입", example="dateTime")
	private String srchDateType;
	@ApiModelProperty(value="바운드타입reverse여부", example="Y or N")
	private String srchRvsBoundType;
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="항공/해상,수출/수입구분", example="AO")
	private String srchBoundType;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="조회번호 Like검색 여부", example="Y or N")
	private String srchNoLikeYn;
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;
	@ApiModelProperty(value="EmsBL여부")
	private String srchEmsBlYn;
}

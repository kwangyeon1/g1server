package org.yht.exps.express.model.eCommerce.epn.search;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchEpnList", description = "전자상거래 EPN조회")
@Alias("searchEpnList")
public class SearchEpnList extends SearchList {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	@ApiModelProperty(value="통관구분")
	private String srchClearanceType;
	@ApiModelProperty(value="수출자")
	private String shipperName;
	@ApiModelProperty(value="신고인기재란")
	private String licenseRemark;
	@ApiModelProperty(value="적하목록이관여부")
	private String mfTsYn;
	@ApiModelProperty(value="조회일자타입", example="dateTime")
	private String srchDateType;
    @ApiModelProperty(value="오더잡번호리스트")
    private List<Map<String, Object>> epnList;
    @ApiModelProperty(value="조회번호 Like검색 여부", example="Y or N")
    private String srchNoLikeYn;
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;	
	
}
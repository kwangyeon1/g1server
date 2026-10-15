package org.yht.exps.express.model;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchOdJobNo", description = "하우스번호, 오더번호로 OdJobNo 검색")
@Alias("searchOdJobNo")
public class SearchOdJobNo extends SearchList{
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="사업장코드", example="YHTXX-KR")
	private String srchEntBusinessCode;
	@ApiModelProperty(value="스캔 번호")
	private String srchScanNo;	
	@ApiModelProperty(value="스캔 타입", example="HBL_NO, PO_NO")
	private String srchScanNoType;	
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;	
	@ApiModelProperty(value="항공/해상,수출/수입구분", example="AO, AI, OO, OI")
	private String srchBoundType;	

}

package org.yht.exps.express.model.bms.bmsPkgNo.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchBmsPkgNoList", description = "보세구역관리(BMS) 박스번호조회 공통 파라미터")
@Alias("SearchBmsPkgNoList")
public class SearchBmsPkgNoList extends SearchList {

	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회반입예정번호")
	private String srchInPlanNo;
	@ApiModelProperty(value="조회BL번호")
	private String srchBlNo;
	@ApiModelProperty(value="반입반출구분", example="반입 : I, 반출 : O")
	private String srchInOutClass;
	
	@ApiModelProperty(value="마스터번호")
	private String mblNo;
	@ApiModelProperty(value="하우스빌번호")
	private String blNo;
	@ApiModelProperty(value="화물관리번호")
	private String cargoReferenceNo;
	@ApiModelProperty(value="반입유형")
	private String inClass;

}
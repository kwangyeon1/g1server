package org.yht.exps.express.model.eCommerce.order.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchElastic;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchExcelUploadResult", description = "전자상거래 엑셀 업로드 결과 리스트 검색 조건")
@Alias("searchExcelUploadResult")
public class SearchExcelUploadResult extends SearchElastic {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="사업장코드", example="YHTXX-KR")
	private String srchEntBusinessCode;
	@ApiModelProperty(value="문서유형", example="ORDER")
	private String srchExcelType;
	@ApiModelProperty(value="결과", example="wait")
	private String srchStatus;
	@ApiModelProperty(value="입력자")
	private String srchRegisterUserId;
	@ApiModelProperty(value="솔루션", example="ECM")
	private String srchSolution;
	@ApiModelProperty(value="바운드", example="AIR")
	private String srchBound;
	@ApiModelProperty(value="수출입 구분", example="EXP")
	private String srchBoundType;
	@ApiModelProperty(value="조회타입")
	private String srchBusinessType;
}

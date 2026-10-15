package org.yht.exps.express.model.invoice.search;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchBatchDeleteCost", description = "전자상거래 Cost 일괄삭제")
@Alias("searchBatchDeleteCost")
public class SearchBatchDeleteCost {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="삭제대상COST구분")
	private String invoiceType;
	@ApiModelProperty(value="삭제대상COST정산처구분")
	private String invoiceAccountType;
	@ApiModelProperty(value="삭제대상구분")
	private String targetData;
	@ApiModelProperty(value="삭제대상 관리번호리스트")
	private List<Map<String, Object>> checkDataList;
	@ApiModelProperty(value="삭제대상 조회조건")
	private SearchInvoiceList searchInvoiceList;
	
	/** 저장 */
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="작업자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="GMT_TIME")
	private String gmtTime;
	
	/** 시스템 로그 */
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}

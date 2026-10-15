package org.yht.exps.express.model.invoice.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchInvoiceList", description = "정산서조회")
@Alias("searchInvoiceList")
public class SearchInvoiceList extends SearchList {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="조회일자타입", example="dateTime")
	private String srchDateType;
	@ApiModelProperty(value="항공/해상,수출/수입구분", example="AO")
	private String srchBoundType;
	@ApiModelProperty(value="정산여부", example="Y or N")
	private String srchInvoiceYn;
	@ApiModelProperty(value="비용구분")
	private String srchChgCodeSystem;
	@ApiModelProperty(value="비용구분")
	private String srchChgCodeSystemIn;
	@ApiModelProperty(value="통화단위")
	private String srchCurrencyCode;
	@ApiModelProperty(value="정산서타입")
	private String invoiceType;
	@ApiModelProperty(value="비용업체코드")
	private String srchAccountCode;
	@ApiModelProperty(value="청구지코드")
	private String srchCustomerCode;
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="작업료입력여부")
	private String srchWhfChargeYn;
	@ApiModelProperty(value="정산서 청구/지급구분")
	private String srchArAp;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;
	
	@ApiModelProperty(value="정산서관리번호")
	private String invoiceJobNo;
	@ApiModelProperty(value="정산일자")
	private String invoiceDate;
	@ApiModelProperty(value="삭제대상COST정산처구분")
	private String invoiceAccountType;
	
	
	//시스템 로그
	@ApiModelProperty(value="작업자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}

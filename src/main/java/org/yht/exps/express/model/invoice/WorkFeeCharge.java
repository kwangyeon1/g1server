package org.yht.exps.express.model.invoice;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "WorkFeeCharge", description = "전자상거래 정산관리 작업료")
@Alias("workFeeCharge")
@EqualsAndHashCode(callSuper=false)
public class WorkFeeCharge extends ResultList{
	
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="오더관리번호")
	private String odJobNo;
	@ApiModelProperty(value="AR정산관리번호")
	private String arInvoiceJobNo;
	@ApiModelProperty(value="AP정산관리번호")
	private String apInvoiceJobNo;
	@ApiModelProperty(value="정산일자")
	private String invoiceDate;
	@ApiModelProperty(value="정산번호")
	private String invoiceNo;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="상위업체코드")
	private String custCodeLv1;
	@ApiModelProperty(value="하위업체코드")
	private String custCodeLv2;
	@ApiModelProperty(value="관세_비고")
	private String tariffRemarks;
	@ApiModelProperty(value="비고사항")
	private String remarks;
	@ApiModelProperty(value="배송요청")
	private String deliveryNote;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="작업료")
	private double whfCost;
	@ApiModelProperty(value="작업료(CUR)")
	private double whfCostCur;
	@ApiModelProperty(value="기타")
	private double otcCost;
	@ApiModelProperty(value="합계")
	private double arAmount;
	@ApiModelProperty(value="통화단위")
	private String currencyCode;
	@ApiModelProperty(value="청구지")
	private String customerCode;
	@ApiModelProperty(value="청구지명")
	private String customerName;
	@ApiModelProperty(value="통관타입")
	private String cdType;
	@ApiModelProperty(value="마감여부")
	private String closeYn;
	@ApiModelProperty(value="입력자명")
	private String registerUserName;
	@ApiModelProperty(value="전체합계")
	private int totalArAmount;
}

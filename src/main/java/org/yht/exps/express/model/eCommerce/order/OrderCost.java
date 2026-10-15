package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "OrderCost", description = "전자상거래 Order 추가비용")
@Alias("orderCost")
@EqualsAndHashCode(callSuper=false)
public class OrderCost extends ResultList{
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="일련번호")
	private String seqNoId;
	@ApiModelProperty(value="정산일자(청구)")
	private String arInvoiceDate;
	@ApiModelProperty(value="정산일자(지급)")
	private String apInvoiceDate;
	@ApiModelProperty(value="운임코드")
	private String chargeCode;
	@ApiModelProperty(value="운임명")
	private String chargeName;
	@ApiModelProperty(value="정산처코드")
	private String accountCode;
	@ApiModelProperty(value="정산처명")
	private String accountName;
	@ApiModelProperty(value="청구금액")
	private double arAmount;
	@ApiModelProperty(value="청구금액(CUR)")
	private double arAmountCur;
	@ApiModelProperty(value="청구금액부가세")
	private double arAmountVat;
	@ApiModelProperty(value="지급금액")
	private double apAmount;
	@ApiModelProperty(value="지급금액부가세")
	private double apAmountVat;
	@ApiModelProperty(value="통화단위")
	private String currencyCode;
	@ApiModelProperty(value="비고사항")
	private String remarks;
	@ApiModelProperty(value="결제구분")
	private String paymentGubn;
	@ApiModelProperty(value="거래처")
	private String customerCode;
	@ApiModelProperty(value="거래처명")
	private String customerName;
	@ApiModelProperty(value="계산금액_창고료")
	private double wahaCost;
	@ApiModelProperty(value="보수금액_창고료")
	private double wmainCost;
	@ApiModelProperty(value="작업료_창고료")
	private double wahoCost;
	@ApiModelProperty(value="경비료_창고료")
	private double securCost;
	@ApiModelProperty(value="보험료_창고료")
	private double insCost;
	@ApiModelProperty(value="폐포장비_창고료")
	private double wpackCost;
	@ApiModelProperty(value="기본운임(배송료)_택배사")
	private double dlcCost;
	@ApiModelProperty(value="도선료_택배사")
	private double dislaCost;
	@ApiModelProperty(value="제주운임_택배사")
	private double djuCost;
	@ApiModelProperty(value="기타운임_택배사")
	private double dtcCost;
	@ApiModelProperty(value="관세_관세사")
	private double dtyCost;
	@ApiModelProperty(value="통관수수료_관세사")
	private double cfcCost;
	@ApiModelProperty(value="검역료_관세사")
	private double quaCost;
	@ApiModelProperty(value="기타금액_관세사")
	private double bctcCost;
	@ApiModelProperty(value="입출금일자")
	private String paymentDate;
	@ApiModelProperty(value="운임코드구분")
	private String chgCodeSystem;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력자명")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="정산서관리번호(청구)")
	private String arInvoiceJobNo;
	@ApiModelProperty(value="정산서관리번호(지급)")
	private String apInvoiceJobNo;
	@ApiModelProperty(value="그리드수정여부")
	private String mode;
	@ApiModelProperty(value="비용타입(배송비(DLV)/관세(BRK)/전체(ALL)")
	private String chgType;
	@ApiModelProperty(value="도입사")
	private String entCode;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
}
package org.yht.exps.express.model.invoice;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "CourierCharge", description = "전자상거래 정산관리 택배사")
@Alias("courierCharge")
@EqualsAndHashCode(callSuper=false)
public class CourierCharge extends ResultList{
	
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
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
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="AR정산관리번호")
	private String arInvoiceJobNo;
	@ApiModelProperty(value="AP정산관리번호")
	private String apInvoiceJobNo;
	@ApiModelProperty(value="정산일자")
	private String invoiceDate;
	@ApiModelProperty(value="정산번호")
	private String invoiceNo;
	@ApiModelProperty(value="택배사")
	private String deliveryName;
	@ApiModelProperty(value="운임구분")
	private String deliveryPaymentType;
	@ApiModelProperty(value="수하인_명칭")
	private String consigneeName;
	@ApiModelProperty(value="배송료")
	private int dlcCost;
	@ApiModelProperty(value="도선료")
	private int dislaCost;
	@ApiModelProperty(value="제주운임")
	private int djuCost;
	@ApiModelProperty(value="기타운임")
	private int dtcCost;
	@ApiModelProperty(value="합계")
	private int apAmount;
	@ApiModelProperty(value="전체합계")
	private int totalApAmount;
	@ApiModelProperty(value="대표품목")
	private String commodityItem;
	@ApiModelProperty(value="비용업체코드")
	private String accountCode;
	@ApiModelProperty(value="비용업체명")
	private String accountName;
	@ApiModelProperty(value="오더관리번호")
	private String odJobNo;
	@ApiModelProperty(value="추가비용일련번호")
	private String seqNoId;
	@ApiModelProperty(value="입력자명")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="변경운송장")
	private String deliveryNoChanged;
}

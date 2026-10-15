package org.yht.exps.express.model.invoice;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "WarehouseCharge", description = "전자상거래 정산관리 창고료")
@Alias("warehouseCharge")
@EqualsAndHashCode(callSuper=false)
public class WarehouseCharge extends ResultList{
	
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
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="MSN")
	private String msn;
	@ApiModelProperty(value="HSN")
	private String hsn;
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
	@ApiModelProperty(value="검사_여부")
	private String inspectionYn;
	@ApiModelProperty(value="X-RAY_여부")
	private String xrayYn;
	@ApiModelProperty(value="취하_여부")
	private String dropYn;
	@ApiModelProperty(value="선별_여부")
	private String seInspectionYn;
	@ApiModelProperty(value="수하인_명칭")
	private String consigneeName;
	@ApiModelProperty(value="계산금액")
	private int wahaCost;
	@ApiModelProperty(value="부가세")
	private int aaaaCost;
	@ApiModelProperty(value="보수금액")
	private int wmainCost;
	@ApiModelProperty(value="경비료")
	private int securCost;
	@ApiModelProperty(value="작업료")
	private int wahoCost;
	@ApiModelProperty(value="보험료")
	private int insCost;
	@ApiModelProperty(value="폐포장비")
	private int wpackCost;
	@ApiModelProperty(value="기타(정산외운임코드)")
	private double etcCost;
	@ApiModelProperty(value="합계")
	private int apAmount;
	@ApiModelProperty(value="반입_일자")
	private String carryinDate;
	@ApiModelProperty(value="반출_일자")
	private String carryoutDate;
	@ApiModelProperty(value="통관타입")
	private String cdType;
	@ApiModelProperty(value="전체합계")
	private int totalApAmount;
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
}

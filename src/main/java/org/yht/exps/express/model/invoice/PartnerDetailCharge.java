package org.yht.exps.express.model.invoice;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "PartnerDetailCharge", description = "전자상거래 정산관리 파트너세부")
@Alias("partnerDetailCharge")
@EqualsAndHashCode(callSuper=false)
public class PartnerDetailCharge extends ResultList{
	
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="하위업체")
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
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="오더관리번호")
	private String odJobNo;
	@ApiModelProperty(value="정산번호")
	private String invoiceNo;
	@ApiModelProperty(value="정산여부")
	private String invoiceYn;
	@ApiModelProperty(value="통화단위")
	private String currencyCode;
	@ApiModelProperty(value="청구지")
	private String customerCode;
	@ApiModelProperty(value="거래처명")
	private String customerName;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="선명_편명")
	private String vesselFlight;
	@ApiModelProperty(value="출발지코드")
	private String origCode;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="통관타입")
	private String cdType;
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
	@ApiModelProperty(value="작업료")
	private double whfCost;
	@ApiModelProperty(value="작업료(CUR)")
	private double whfCostCur;
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
	@ApiModelProperty(value="배송료")
	private int dlcCost;
	@ApiModelProperty(value="도선료")
	private int dislaCost;
	@ApiModelProperty(value="제주운임")
	private int djuCost;
	@ApiModelProperty(value="관세")
	private int dtyCost;
	@ApiModelProperty(value="부가세")
	private int taxCost;
	@ApiModelProperty(value="통관수수료")
	private int cfcCost;
	@ApiModelProperty(value="검역료")
	private int quaCost;
	@ApiModelProperty(value="무적")
	private int execCost;
	@ApiModelProperty(value="보수작업")
	private int maintCost;
	@ApiModelProperty(value="협정")
	private int agreCost;
	@ApiModelProperty(value="수출")
	private int expCost;
	@ApiModelProperty(value="기타(정산외운임코드)")
	private double etcCost;
	@ApiModelProperty(value="합계")
	private int arAmount;
	@ApiModelProperty(value="전체합계")
	private int totalArAmount;
	@ApiModelProperty(value="입력자명")
	private String registerUserName;
	
}

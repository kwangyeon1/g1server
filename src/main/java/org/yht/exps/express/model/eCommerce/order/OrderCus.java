package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "OrderCus", description = "전자상거래 Order 고객상담관리 리스트")
@Alias("orderCus")
@EqualsAndHashCode(callSuper=false)
public class OrderCus extends ResultList{

	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력자이름")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="상담유형")
	private String csType;
	@ApiModelProperty(value="상담내용")
	private String csContents;
	@ApiModelProperty(value="송하인_명칭")
	private String shipperName;
	@ApiModelProperty(value="수하인_명칭")
	private String consigneeName;
	@ApiModelProperty(value="수하인_명칭_한글")
	private String consigneeNameLoc;
	@ApiModelProperty(value="청구금액")
	private double arAmountSum;
	@ApiModelProperty(value="지급금액")
	private double apAmountSum;
	@ApiModelProperty(value="입항일자")
	private String arDate;	
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="선명_편명")
	private String vesselFlight;	
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="총중량")
	private double gwt;	
	@ApiModelProperty(value="원화금액_AMOUNT")
	private double amountLoc;
	@ApiModelProperty(value="대표품목")
	private String commodityItem;
	@ApiModelProperty(value="검사_여부")
	private String inspectionYn;
	@ApiModelProperty(value="선별_여부")
	private String seInspectionYn;
	@ApiModelProperty(value="취하_여부")
	private String dropYn;
	@ApiModelProperty(value="홀드_여부")
	private String holdYn;
	@ApiModelProperty(value="통관타입")
	private String cdType;
	@ApiModelProperty(value="통관_용도구분")
	private String cdUseType;
	@ApiModelProperty(value="거래구분")
	private String ecmType;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="하위업체")
	private String custCodeLv2;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="수화인핸드폰번호")
	private String consigneeHp;
	@ApiModelProperty(value="수하인전화번호")
	private String consigneeTel;
	@ApiModelProperty(value="FileShare유무")
	private String fileShare;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	
	@ApiModelProperty(value="클레임건수")
	private String csType1Cnt;
	@ApiModelProperty(value="출고보류건수")
	private String csType2Cnt;
	@ApiModelProperty(value="일반상담건수")
	private String csType3Cnt;
	@ApiModelProperty(value="오버사이즈건수")
	private String csType4Cnt;
	@ApiModelProperty(value="고객전화건수")
	private String csType5Cnt;
	@ApiModelProperty(value="고객메일건수")
	private String csType6Cnt;
}

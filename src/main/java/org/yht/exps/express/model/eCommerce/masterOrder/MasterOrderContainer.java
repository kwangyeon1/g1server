package org.yht.exps.express.model.eCommerce.masterOrder;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MasterOrderContainer", description = "전자상거래 MasterOrderContainer")
@Alias("masterOrderContainer")
public class MasterOrderContainer{
	@ApiModelProperty(value="비엘관리번호")
	private String oblJobNo;
	@ApiModelProperty(value="컨테이너번호")
	private String cntrNo;
	@ApiModelProperty(value="컨테이너_봉인번호_1")
	private String cntrSealNo1;
	@ApiModelProperty(value="컨테이너코드")
	private String cntrCode;
	@ApiModelProperty(value="수량")
	private double pieces;
	@ApiModelProperty(value="단위_수량")
	private String unitPieces;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="단위_중량")
	private String unitWeight;
	@ApiModelProperty(value="용적")
	private double cbm;
	@ApiModelProperty(value="컨테이너_봉인번호_2")
	private String cntrSealNo2;
	@ApiModelProperty(value="컨테이너_봉인번호_3")
	private String cntrSealNo3;
	@ApiModelProperty(value="컨테이너소유구분")
	private String cntrOwnType;
	@ApiModelProperty(value="컨테이너EDI코드")
	private String cntrEdiCode;
	@ApiModelProperty(value="컨테이너상세코드")
	private String cntrSubCode;
	@ApiModelProperty(value="컨테이너사용구분")
	private String cntrLoadTypeEl;
	@ApiModelProperty(value="컨테이너정보_인쇄용")
	private String cntrPrintInfo;
}

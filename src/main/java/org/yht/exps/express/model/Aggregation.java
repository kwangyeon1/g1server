package org.yht.exps.express.model;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "Aggregations", description = "통계성 데이터")
@Alias("aggregations")
public class Aggregation {
	@ApiModelProperty(value="하우스 개수")
	private long houseCount;
	@ApiModelProperty(value="하우스 수량 합계")
	private double houseSumPieces;
	@ApiModelProperty(value="하우스 중량 합계")
	private double houseSumGwt;
	@ApiModelProperty(value="개인통관부호가 입력된 하우스 개수")
	private long houseCustomsIdCount;
	@ApiModelProperty(value="목록통관 하우스 개수")
	private long houseListClearanceCount;
	@ApiModelProperty(value="일반통관 하우스 개수")
	private long houseGeneralClearanceCount;
	@ApiModelProperty(value="통관배제 하우스 개수")
	private long houseExclusionClearanceCount;
	@ApiModelProperty(value="검사 하우스 개수")
	private long houseInspectionCount;
	@ApiModelProperty(value="선별 하우스 개수")
	private long houseSeInspectionCount;
	@ApiModelProperty(value="취하 하우스 개수")
	private long houseDropCount;
	@ApiModelProperty(value="X-Ray 하우스 개수")
	private long houseXrayCount;
	@ApiModelProperty(value="일검 하우스 개수")
	private long houseGeInspectionCount;
	@ApiModelProperty(value="검역 하우스 개수")
	private long houseQuarantineCount;
	@ApiModelProperty(value="홀드 하우스 개수")
	private long houseHoldCount;
	@ApiModelProperty(value="반입 하우스 개수")
	private long houseCarryinCount;
	@ApiModelProperty(value="미반입 하우스 개수")
	private long houseNotCarryinCount;
	@ApiModelProperty(value="반입율")
	private double houseCarryinRatio;
	@ApiModelProperty(value="미통관 하우스 개수")
	private long houseNotClearanceCount;
	@ApiModelProperty(value="송장교체 하우스 개수")
	private long houseDeliveryNoChangedCount;
	@ApiModelProperty(value="면장 수량")
	private long licenseCount;
	@ApiModelProperty(value="면장 중량 합계")
	private double sumLicenseGwt;
}

package org.yht.exps.express.model.eCommerce.houseOrder;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "HouseOrderLicense", description = "전자상거래 HouseOrderLicense")
@Alias("houseOrderLicense")
public class HouseOrderLicense{
	@ApiModelProperty(value="비엘관리번호")
	private String oblJobNo;
	@ApiModelProperty(value="면장번호")
	private String licenseNo;
	@ApiModelProperty(value="수량")
	private double pieces;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="동시포장구분")
	private String combineTypeAZ;
	@ApiModelProperty(value="동시포장수량")
	private double combinePieces;
	@ApiModelProperty(value="분할선적여부")
	private String partialYn;
	@ApiModelProperty(value="분할선적차수")
	private double partialSeq;
}

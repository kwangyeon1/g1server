package org.yht.exps.express.model.eCommerce.masterOrder;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MasterOrderDimension", description = "전자상거래 MasterOrderDimension")
@Alias("masterOrderDimension")
public class MasterOrderDimension{
	@ApiModelProperty(value="비엘관리번호")
	private String oblJobNo;
	@ApiModelProperty(value="순번")
	private String seqNo;
	@ApiModelProperty(value="화물규격_가로")
	private double dimWidth;
	@ApiModelProperty(value="화물규격_길이")
	private double dimLength;
	@ApiModelProperty(value="화물규격_높이")
	private double dimHeight;
	@ApiModelProperty(value="화물규격_수량")
	private double dimPieces;
	@ApiModelProperty(value="화물규격_볼륨")
	private double dimVolumn;
}

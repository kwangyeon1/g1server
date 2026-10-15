package org.yht.exps.express.model.eCommerce.masterOrder;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MasterOrderHts", description = "전자상거래 MasterOrderHts")
@Alias("masterOrderHts")
public class MasterOrderHts{
	@ApiModelProperty(value="비엘관리번호")
	private String mfJobNo;
	@ApiModelProperty(value="면장번호")
	private String htsCode;
}

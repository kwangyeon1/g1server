package org.yht.exps.express.model.eCommerce.houseOrder;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "HouseOrderHts", description = "전자상거래 HouseOrderHts")
@Alias("houseOrderHts")
public class HouseOrderHts{
	@ApiModelProperty(value="비엘관리번호")
	private String mfJobNo;
	@ApiModelProperty(value="면장번호")
	private String htsCode;
}

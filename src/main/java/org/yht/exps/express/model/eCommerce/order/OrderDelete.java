package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "OrderDelete", description = "전자상거래 Order 삭제")
@Alias("orderDelete")
public class OrderDelete {
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="H.B/L No")
	private String hblNo;
}

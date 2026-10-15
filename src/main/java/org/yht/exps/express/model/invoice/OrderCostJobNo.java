package org.yht.exps.express.model.invoice;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "OrderCostJobNo", description = "전자상거래 추가비용 관리번호")
@Alias("orderCostJobNo")
@EqualsAndHashCode(callSuper=false)
public class OrderCostJobNo{
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="일련번호")
	private String seqNoId;
}

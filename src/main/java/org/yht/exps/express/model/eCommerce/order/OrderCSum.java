package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "OrderCSum", description = "전자상거래 Order 통관목록현황집계")
@Alias("orderCSum")
@EqualsAndHashCode(callSuper=false)
public class OrderCSum extends ResultList {
	@ApiModelProperty(value="H.B/L 앞 6자리")
	private String hblNo;
	@ApiModelProperty(value="전자상거래업체")
	private String code;
	@ApiModelProperty(value="목록건수")
	private int totCnt;
	@ApiModelProperty(value="목록배제건수")
	private int cnt;
	@ApiModelProperty(value="일반건수")
	private int totCntN;
}

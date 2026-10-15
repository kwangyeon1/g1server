package org.yht.exps.express.model.consumerService.order;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "OrderCustomerSaveResult", description = "전자상거래 Order Save 결과")
@Alias("orderCustomerSaveResult")
public class OrderCustomerSaveResult {
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	
}

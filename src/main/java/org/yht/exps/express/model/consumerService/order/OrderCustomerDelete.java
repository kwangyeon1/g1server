package org.yht.exps.express.model.consumerService.order;

import java.util.List;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "OrderCustomerDelete", description = "Order 일괄삭제")
@Alias("orderCustomerDelete")
public class OrderCustomerDelete {
	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	
	@ApiModelProperty(value="사업장코드")
	private String userBusinessCode;
	
	@ApiModelProperty(value="리스트")
	private List<org.yht.exps.express.model.eCommerce.order.Order> orderCustomerList;
	@ApiModelProperty(value="사용자아이디")
	private String userId;
	
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
}

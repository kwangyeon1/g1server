package org.yht.exps.express.model.eCommerce.order;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "OrderListDelete", description = "전자상거래 OrderList 삭제")
@Alias("orderListDelete")
public class OrderListDelete {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="오더삭제리스트")
	private List<OrderDelete> orderDeleteList;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="삭제자아이디")
	private String userId;
	@ApiModelProperty(value="삭제자명")
	private String userName;
	@ApiModelProperty(value="GMT")
	private int gmtTime;
	@ApiModelProperty(value="작업자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="action")
	private String action;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}

package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "OrderSpecialItem", description = "전자상거래 Order 별도관리물품")
@Alias("orderSpecialItem")
public class OrderSpecialItem {
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="일련번호")
	private String seqNoId;
	@ApiModelProperty(value="별도관리물품_구분")
	private String manageType;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="편명")
	private String flightNo;
	@ApiModelProperty(value="적재항")
	private String destPort;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="품목_상세")
	private String itemDesc;
	@ApiModelProperty(value="총중량")
	private double gwt;
}

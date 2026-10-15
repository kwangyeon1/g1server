package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "TrademarkInfringement", description = "전자상거래 Order 상표권침해")
@Alias("TrademarkInfringement")
public class TrademarkInfringement {
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="물품 품명")
	private String itemName;
	@ApiModelProperty(value="물품 상표명")
	private String trademarkName;
	@ApiModelProperty(value="규격")
	private String itemSize;
	@ApiModelProperty(value="통보세관")
	private String customsCode;
	@ApiModelProperty(value="통보세관 과")
	private String customsDptCode;
	@ApiModelProperty(value="통보자 성명")
	private String customsAuthName;
	@ApiModelProperty(value="전화번호")
	private String customsAuthTel;
	@ApiModelProperty(value="팩스번호")
	private String customsAuthFax;
	@ApiModelProperty(value="발행일자")
	private String customsIssueDate;
}

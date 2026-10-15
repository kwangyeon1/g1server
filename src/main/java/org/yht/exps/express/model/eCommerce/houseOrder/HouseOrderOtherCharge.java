package org.yht.exps.express.model.eCommerce.houseOrder;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "HouseOrderOtherCharge", description = "전자상거래 HouseOrderOtherCharge")
@Alias("houseOrderOtherCharge")
public class HouseOrderOtherCharge{
	@ApiModelProperty(value="비엘관리번호")
	private String oblJobNo;
	@ApiModelProperty(value="순번")
	private String seqNo;
	@ApiModelProperty(value="운임코드")
	private String chargeCode;
	@ApiModelProperty(value="운임명칭")
	private String chargeName;
	@ApiModelProperty(value="통화단위")
	private String currencyCode;
	@ApiModelProperty(value="FREIGHT_TERM")
	private String freightTerm;
	@ApiModelProperty(value="단가_계산방법")
	private String rateMultiply;
	@ApiModelProperty(value="단가_RATE")
	private double rate;
	@ApiModelProperty(value="금액_AMOUNT")
	private double amount;
	@ApiModelProperty(value="운임코드_IATA")
	private String chgCodeIata;
	@ApiModelProperty(value="운임코드_IATA")
	private String chargeTypeAc;
}

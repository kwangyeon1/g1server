package org.yht.exps.express.model.eCommerce.broker;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "BrokerHouseSub", description = "전자상거래 broker-H 금액 리스트")
@Alias("brokerHouseSub")
@EqualsAndHashCode(callSuper=false)
public class BrokerHouseSub extends ResultList{
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	
	@ApiModelProperty(value="과세환율")
	private double taxExchangeRate;
	@ApiModelProperty(value="과세운임")
	private double taxFreight;
	@ApiModelProperty(value="CIF원화")
	private double cifLocal;
	@ApiModelProperty(value="부가세")
	private double surtax;
	@ApiModelProperty(value="특소세")
	private double specialConsumtionTax;
	@ApiModelProperty(value="주세")
	private double liquorTax;
	@ApiModelProperty(value="교육세")
	private double educationTax;
	@ApiModelProperty(value="농특세")
	private double ruralSpecialTax;
	@ApiModelProperty(value="통관수수료")
	private double clearanceFee;
	@ApiModelProperty(value="검역수수료")
	private double qurantineFee;
	@ApiModelProperty(value="창고료")
	private double warehouseFee;
	@ApiModelProperty(value="관세")
	private double tariff;
	@ApiModelProperty(value="총세액")
	private double totFee;
	@ApiModelProperty(value="총합계")
	private double totAmt;
	
	@ApiModelProperty(value="관세사 I/F")
	private String cbIfStatus;
	@ApiModelProperty(value="통관신고구분")
	private String cclType;

	/**
	 * TODO 총세액,총합계
	 */
	
}

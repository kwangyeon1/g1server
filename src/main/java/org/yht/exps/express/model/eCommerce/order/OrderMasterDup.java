package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import org.yht.exps.express.model.eCommerce.consol.ConsolMasterOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "OrderMasterDup", description = "전자상거래 OrderMasterDup")
@Alias("orderMasterDup")
@EqualsAndHashCode(callSuper=false)
public class OrderMasterDup extends ResultList {

	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="출항일자")
	private String arDate;
	@ApiModelProperty(value="MAWB No")
	private String mblNo;
	@ApiModelProperty(value="H.Cnt")
	private int hblCnt;
	@ApiModelProperty(value="편명(선명)")
	private String vesselFlight;
	@ApiModelProperty(value="출발지")
	private String origCode;
	@ApiModelProperty(value="도착지")
	private String destCode;
	
}

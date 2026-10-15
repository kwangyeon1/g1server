package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "OrderHold", description = "전자상거래 Order 홀드")
@Alias("orderHold")
public class OrderHold {
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty("홀드_코드")
	private String holdCode;
	@ApiModelProperty(value="홀드_사유")
	private String holdReason;
	@ApiModelProperty(value="홀드_담당자")
	private String holdAttn;
	@ApiModelProperty(value="홀드_담당자명")
	private String holdAttnName;
	@ApiModelProperty(value="홀드_일자")
	private String holdDate;
	@ApiModelProperty(value="반입금지여부")
	private String carryinStopYn;
	@ApiModelProperty(value="반출금지여부")
	private String carryoutStopYn;
	@ApiModelProperty(value="홀드처리_여부")
	private String holdReleaseYn;
	@ApiModelProperty(value="홀드처리_사유")
	private String holdReleaseReason;
	@ApiModelProperty(value="홀드처리_담당자")
	private String holdReleaseAttn;
	@ApiModelProperty(value="홀드처리_담당자명")
	private String holdReleaseAttnName;
    @ApiModelProperty(value="홀드처리_일자")
    private String holdReleaseDate;
    @ApiModelProperty(value="홀드처리구분")
    private String holdGubn;
}

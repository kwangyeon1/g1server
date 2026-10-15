package org.yht.exps.express.model.commonMng.stock;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "StockSaveResult", description = "전자상거래 Stock Save 결과")
@Alias("stockSaveResult")
public class StockSaveResult {
	@ApiModelProperty(value="관리번호")
	private String seqNo;
	@ApiModelProperty(value="결과코드")
	private String resultCode;

}

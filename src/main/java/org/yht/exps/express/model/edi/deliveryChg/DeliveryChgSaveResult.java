package org.yht.exps.express.model.edi.deliveryChg;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "DeliveryChgSaveResult", description = "DeliveryChg Save 결과")
@Alias("deliveryChgSaveResult")
public class DeliveryChgSaveResult {
	
	@ApiModelProperty(value="관리번호")
	private String ediJobNo;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	@ApiModelProperty(value="에러결과")
	private String errorResult;
	
	@ApiModelProperty(value="결과내역")
	private Map<String, Object> resultData;
	
	@ApiModelProperty(value="엑셀joblist")
	private List<DeliveryChg> odList;
	
}

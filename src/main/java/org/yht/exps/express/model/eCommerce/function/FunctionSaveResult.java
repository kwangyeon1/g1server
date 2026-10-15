package org.yht.exps.express.model.eCommerce.function;

import java.util.Map;
import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "FunctionSaveResult", description = "전자상거래 Function Save 결과")
@Alias("FunctionSaveResult")
public class FunctionSaveResult {
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	@ApiModelProperty(value="결과내역")
	private Map<String, Object> resultData;
	
}
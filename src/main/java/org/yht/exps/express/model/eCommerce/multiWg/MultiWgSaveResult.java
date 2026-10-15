package org.yht.exps.express.model.eCommerce.multiWg;

import java.util.Map;
import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MultiWgSaveResult", description = "전자상거래 멀티(중량) Save 결과")
@Alias("multiWgSaveResult")
public class MultiWgSaveResult {
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	@ApiModelProperty(value="결과내역")
	private Map<String, Object> resultData;
	@ApiModelProperty(value="처리건수")
	private int resultCnt;
	
}
package org.yht.exps.express.model.eCommerce.unusualMng;

import java.util.Map;
import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "UnusualMngSaveResult", description = "고객서비스 이례화물관리 save 결과")
@Alias("unusualMngSaveResult")
public class UnusualMngSaveResult {
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	@ApiModelProperty(value="결과내역")
	private Map<String, Object> resultData;
	@ApiModelProperty(value="처리건수")
	private int resultCnt;
	
}
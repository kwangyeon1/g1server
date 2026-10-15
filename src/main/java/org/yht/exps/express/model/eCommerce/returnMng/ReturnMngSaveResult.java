package org.yht.exps.express.model.eCommerce.returnMng;

import java.util.Map;
import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ReturnMngSaveResult", description = "고객서비스 반품관리 save 결과")
@Alias("returnMngSaveResult")
public class ReturnMngSaveResult {
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	@ApiModelProperty(value="결과내역")
	private Map<String, Object> resultData;
	@ApiModelProperty(value="처리건수")
	private int resultCnt;
	
}
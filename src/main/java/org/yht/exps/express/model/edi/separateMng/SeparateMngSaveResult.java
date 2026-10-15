package org.yht.exps.express.model.edi.separateMng;

import java.util.Map;
import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SeparateMngSaveResult", description = "SeparateMng Save 결과")
@Alias("separateMngSaveResult")
public class SeparateMngSaveResult {
	
	@ApiModelProperty(value="관리번호")
	private String ediJobNo;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	
	@ApiModelProperty(value="결과내역")
	private Map<String, Object> resultData;
		
}

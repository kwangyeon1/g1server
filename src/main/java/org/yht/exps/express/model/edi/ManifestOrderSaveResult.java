package org.yht.exps.express.model.edi;

import java.util.Map;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ManifestOrderSaveResult", description = "Master Save 결과")
@Alias("manifestOrderSaveResult")
public class ManifestOrderSaveResult {
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	@ApiModelProperty(value="결과내역")
	private Map<String, Object> resultData;
	
}

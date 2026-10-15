package org.yht.exps.express.model.eCommerce.pod;

import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "PodSaveResult", description = "전자상거래 PodSave 결과")
@Alias("PodSaveResult")
public class PodSaveResult {
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	@ApiModelProperty(value="결과내역")
	private Map<String, Object> resultData;
	@ApiModelProperty(value="결과출력")
	private Map<String, Object> returnMap;
}

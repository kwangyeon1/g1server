package org.yht.exps.express.model.edi;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ManifestSeparationResult", description = "제출번호 분리 결과")
@Alias("manifestSeparationResult")
public class ManifestSeparationResult {
	
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	
	@ApiModelProperty(value="결과내역")
	private Map<String, Object> resultData;
	
}


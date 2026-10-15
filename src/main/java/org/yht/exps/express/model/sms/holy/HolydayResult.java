package org.yht.exps.express.model.sms.holy;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "HolydayResult", description = "공휴일관리 Save 결과")
@Alias("jolydayResult")
public class HolydayResult {
	@ApiModelProperty(value="관리번호")
	private String seqNoId;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	@ApiModelProperty(value="결과내역")
	private Map<String, Object> resultData;
	@ApiModelProperty(value="결과리스트 (다건)")
	private List<Map<String, Object>> resultList;
}

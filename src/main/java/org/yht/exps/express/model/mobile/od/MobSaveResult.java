package org.yht.exps.express.model.mobile.od;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

@Data
@ApiModel(value = "MobSaveResult", description = "전자상거래  모바일 Order Save 결과")
@Alias("mobSaveResult")
public class MobSaveResult {
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	@ApiModelProperty(value="order")
	private MobEcod mobEcod;
	@ApiModelProperty(value="기타상세네용")
	private String chkContent;


	@ApiModelProperty(value="프린트정보")
	Map<String, Object> printParamMap;
}

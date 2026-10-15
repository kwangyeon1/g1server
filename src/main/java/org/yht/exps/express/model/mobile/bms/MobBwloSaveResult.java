package org.yht.exps.express.model.mobile.bms;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MobBwloSaveResult", description = "보세구역관리  모바일 로케이션적치 Save 결과")
@Alias("mobBwloSaveResult")
public class MobBwloSaveResult {
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	@ApiModelProperty(value="order")
	private MobBwlo mobBwlo;
	
}

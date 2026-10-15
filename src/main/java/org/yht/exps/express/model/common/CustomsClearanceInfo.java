package org.yht.exps.express.model.common;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "CustomsClearanceInfo", description = "통관정보 url 호출을 위한 데이터 암호화정보")
@Alias("customsClearanceInfo")
public class CustomsClearanceInfo {
	
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
}

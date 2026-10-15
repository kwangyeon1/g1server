package org.yht.exps.express.model.api;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "InterfaceTracking", description = "전자상거래 수입(항공&해상) 트레이킹 조회")
@Alias("interfaceTracking")
public class InterfaceTracking{

	@ApiModelProperty(value="도입사코드", example="YHTXX")
//	private String entCode;
	private String fwdCode;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="바운드", example="AI OR OI")
	private String bound;		
	
}

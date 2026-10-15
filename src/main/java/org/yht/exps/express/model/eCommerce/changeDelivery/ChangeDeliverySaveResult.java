package org.yht.exps.express.model.eCommerce.changeDelivery;

import java.util.Map;
import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ChangeDeliverySaveResult", description = "고객서비스 송장교체(주소변경) Save 결과")
@Alias("changeDeliverySaveResult")
public class ChangeDeliverySaveResult {
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	@ApiModelProperty(value="결과내역")
	private Map<String, Object> resultData;
	@ApiModelProperty(value="처리건수")
	private int resultCnt;
	
}
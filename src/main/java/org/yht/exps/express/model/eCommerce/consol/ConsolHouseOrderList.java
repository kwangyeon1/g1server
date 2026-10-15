package org.yht.exps.express.model.eCommerce.consol;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ConsolHouseOrderList", description = "전자상거래 Consol HouseOrder 리스트")
@Alias("consolHouseOrderList")
public class ConsolHouseOrderList{
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="리스트")
	private List<ConsolHouseOrder> ConsolHouseOrderList;
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
}

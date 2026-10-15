package org.yht.exps.express.model.invoice;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "WarehouseChargeList", description = "전자상거래 정산관리 창고료 리스트")
@Alias("warehousearehouseChargeList")
public class WarehouseChargeList {
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="리스트")
	private List<WarehouseCharge> warehouseChargeList;
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
}

package org.yht.exps.express.model.bms.bmsEc;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

@Data
@ApiModel(value = "BmsEcOutPlanList", description = "EC반출승인")
@Alias("bmsEcOutPlanList")
public class BmsEcOutPlanList {
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="반출승인 리스트")
	private List<BmsEcOutPlan> ecOutPlanList;
	
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
}
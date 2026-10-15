package org.yht.exps.express.model.bms.carryin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import java.util.List;

/** 반입예정 리스트*/

@Data
@ApiModel(value = "InPlanList", description = "반입예정 리스트")
@Alias("inPlanList")
public class InPlanList {
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="리스트")
	private List<InPlan> inPlanList;
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
}

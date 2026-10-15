package org.yht.exps.express.model.systemMng;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MonthlyCountList", description = "전자상거래 월별건수 리스트")
@Alias("monthlyCountList")
public class MonthlyCountList{
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="리스트")
	private List<MonthlyCount> monthlyCountList;
}

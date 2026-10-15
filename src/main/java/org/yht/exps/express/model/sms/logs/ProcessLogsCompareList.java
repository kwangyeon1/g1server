package org.yht.exps.express.model.sms.logs;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ProcessLogsCompareList", description = "프로세스 처리 로그 비교 리스트")
@Alias("processLogsCompareList")
public class ProcessLogsCompareList {
	@ApiModelProperty(value="비교리스트1")
	private List<ProcessLogsCompare> processLogsList1;
	@ApiModelProperty(value="비교리스트2")
	private List<ProcessLogsCompare> processLogsList2;
}

package org.yht.exps.express.model.sms.logs;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "BatchDataLogsList", description = "배치 처리 로그 리스트")
@Alias("batchDataLogsList")
public class BatchDataLogsList {
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="리스트")
	private List<BatchDataLogs> batchDataLogsList;
}

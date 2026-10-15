package org.yht.exps.express.model.sms.logs;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ProcessLogsCompare", description = "프로세스 처리 로그 비교")
@Alias("processLogsCompare")
public class ProcessLogsCompare {
	@ApiModelProperty(value="key")
	private String key;
	@ApiModelProperty(value="value")
	private Object value;
	@ApiModelProperty(value="변경유무")
	private String isSame;
	@ApiModelProperty(value="gb")
	private String gb;
}

package org.yht.exps.express.model.bms.bmsXraySend;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data	
@ApiModel(value = "xraySendLogList", description = "xray 재전송 리스트")
@Alias("xraySendLogList")
public class XraySendLogList {
    @ApiModelProperty(value="리스트 개수")
    private long total;
	@ApiModelProperty(value="리스트")
	private List<XraySendLog> xraySendLogList;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
}

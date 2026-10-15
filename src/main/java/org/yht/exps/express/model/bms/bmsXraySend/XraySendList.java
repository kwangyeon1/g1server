package org.yht.exps.express.model.bms.bmsXraySend;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data	
@ApiModel(value = "xraySendList", description = "xray 재전송 리스트")
@Alias("xraySendList")
public class XraySendList {
    @ApiModelProperty(value="리스트 개수")
    private long total;
	@ApiModelProperty(value="리스트")
	private List<XraySend> xraySendList;
}

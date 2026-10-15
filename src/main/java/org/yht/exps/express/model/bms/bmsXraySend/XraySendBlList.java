package org.yht.exps.express.model.bms.bmsXraySend;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data	
@ApiModel(value = "xraySendBlList", description = "xray 재전송 > BL ADD 리스트")
@Alias("xraySendBlList")
public class XraySendBlList {
    @ApiModelProperty(value="리스트 개수")
    private long total;
	@ApiModelProperty(value="리스트")
	private List<XraySendBl> xraySendBlList;
}

package org.yht.exps.express.model.bms.bmsXraySend;

import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@ApiModel(value = "XraySendSaveResult", description = "xray hms전송 결과")
@EqualsAndHashCode(callSuper=false)
@Alias("XraySendSaveResult")
@NoArgsConstructor
@AllArgsConstructor
public class XraySendSaveResult {
	
    @ApiModelProperty(value="결과코드")
    private String resultCode;
    @ApiModelProperty(value="결과내역")
    private Map<String, Object> resultData;
    
}

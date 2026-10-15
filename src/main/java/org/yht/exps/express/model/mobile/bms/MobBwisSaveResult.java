package org.yht.exps.express.model.mobile.bms;

import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "MobBwisSaveResult", description = "검사대상확인 save 결과")
@EqualsAndHashCode(callSuper=false)
@Alias("mobBwisSaveResult")
public class MobBwisSaveResult {
	@ApiModelProperty(value="반입번호")
    private String inNo;
    @ApiModelProperty(value="결과코드")
    private String resultCode;
    @ApiModelProperty(value="결과내역")
    private Map<String, Object> resultData;
    @ApiModelProperty(value="결과출력")
    private Map<String, Object> returnMap;

}

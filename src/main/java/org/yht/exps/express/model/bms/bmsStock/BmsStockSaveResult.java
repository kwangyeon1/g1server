package org.yht.exps.express.model.bms.bmsStock;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.ibatis.type.Alias;

import java.util.Map;

@Data
@ApiModel(value = "BmsStockSaveResult", description = "재고실사 excel upload Save 결과")
@Alias("bmsStockSaveResult")
public class BmsStockSaveResult {
    @ApiModelProperty(value="관리번호")
    private String odJobNo;
    @ApiModelProperty(value="결과코드")
    private String resultCode;
    @ApiModelProperty(value="결과내역")
    private Map<String, Object> resultData;
    @ApiModelProperty(value="결과출력")
    private Map<String, Object> returnMap;
}

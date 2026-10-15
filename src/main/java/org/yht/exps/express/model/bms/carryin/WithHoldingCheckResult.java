package org.yht.exps.express.model.bms.carryin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import org.apache.ibatis.type.Alias;

@Data
@ApiModel(value = "WithHoldingCheckResult", description = "보류리스트 체크사항")
@EqualsAndHashCode(callSuper=false)
@Alias("withHoldingCheckResult")
public class WithHoldingCheckResult {
    @ApiModelProperty(value = "보류리스트 사용여부")
    private String withHoldingYn;
    @ApiModelProperty(value="보류리스트")
    private List<WithHolding> withHoldingList;
}

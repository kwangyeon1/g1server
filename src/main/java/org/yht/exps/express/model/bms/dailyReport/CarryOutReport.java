package org.yht.exps.express.model.bms.dailyReport;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;

@Data
@Builder
@ApiModel(value = "CarryOutReport", description = "반출보고")
@Alias("carryOutReport")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class CarryOutReport {
    @ApiModelProperty(value="장치구분")
    private String deviceClassification;

    @ApiModelProperty(value="구분")
    private String classificationType;

    @ApiModelProperty(value="건수")
    private Integer carryoutCount;
    @ApiModelProperty(value="수량")
    private Integer carryoutSumCt;
    @ApiModelProperty(value="중량")
    private Double carryoutSumWt;
}

package org.yht.exps.express.model.bms.dailyReport;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;

@Data
@Builder
@ApiModel(value = "CarryInReport", description = "반입보고")
@Alias("carryInReport")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class CarryInReport {

    @ApiModelProperty(value="장치구분")
    private String deviceClassification;

    @ApiModelProperty(value="구분")
    private String classificationType;

    @ApiModelProperty(value="건수")
    private Integer carryinCount;
    @ApiModelProperty(value="수량")
    private Integer carryinSumCt;
    @ApiModelProperty(value="중량")
    private Double carryinSumWt;
}

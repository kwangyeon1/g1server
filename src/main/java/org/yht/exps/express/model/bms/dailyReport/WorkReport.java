package org.yht.exps.express.model.bms.dailyReport;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;

@Data
@Builder
@ApiModel(value = "WorkReport", description = "일일보고")
@Alias("workReport")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class WorkReport {

    @ApiModelProperty(value="전일재고건수")
    private Integer yesterdayStockCount;
    @ApiModelProperty(value="전일재고수량")
    private Integer yesterdayStockCt;
    @ApiModelProperty(value="전일재고중량")
    private Double yesterdayStockWt;
    @ApiModelProperty(value="금일반입건수")
    private Integer todayCarryinCount;
    @ApiModelProperty(value="금일반입수량")
    private Integer todayCarryinCt;
    @ApiModelProperty(value="금일반입중량")
    private Double todayCarryinWt;
    @ApiModelProperty(value="금일반출건수")
    private Integer todayCarryoutCount;
    @ApiModelProperty(value="금일반출수량")
    private Integer todayCarryoutCt;
    @ApiModelProperty(value="금일반출중량")
    private Double todayCarryoutWt;
    @ApiModelProperty(value="금일재고건수")
    private Integer todayStockCount;
    @ApiModelProperty(value="금일재고수량")
    private Integer todayStockCt;
    @ApiModelProperty(value="금일재고중량")
    private Double todayStockWt;

    @ApiModelProperty(value="해상수입")
    private Integer importSea;
    @ApiModelProperty(value="항공수입")
    private Integer importAir;
    @ApiModelProperty(value="수입합계")
    private Integer importTotal;

    @ApiModelProperty(value="장치구분")
    private String deviceClassification;

    @ApiModelProperty(value="구분")
    private String classificationType;

    @ApiModelProperty(value="체화")
    private String delay;

}

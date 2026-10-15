package org.yht.exps.express.model.bms.dailyReport;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@Builder
@ApiModel(value = "PeriodReport", description = "기간별반출입")
@Alias("periodReport")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class PeriodReport extends ResultList {

    /** 그리드 카운트*/
    @ApiModelProperty(value="총건수")
    private int cnt;
    @ApiModelProperty(value="목록건수")
    private int simpleCnt;
    @ApiModelProperty(value="일반건수")
    private int generalCnt;
    @ApiModelProperty(value="총건수")
    private int totalCnt;
    @ApiModelProperty(value="목록건수")
    private int totalSimpleCnt;
    @ApiModelProperty(value="일반건수")
    private int totalGeneralCnt;
    @ApiModelProperty(value="번호")
    private int num;

    @ApiModelProperty(value="장치구분")
    private String deviceClassification;
    @ApiModelProperty(value="구분")
    private String classificationType;
    @ApiModelProperty(value="날짜")
    private String divDate;
    @ApiModelProperty(value="전일재고")
    private Integer yesterdayStockCount;
    @ApiModelProperty(value="목록(금일반입)")
    private Integer carryinInventory;
    @ApiModelProperty(value="배제(금일반입)")
    private Integer carryinBan;
    @ApiModelProperty(value="일반(금일반입)")
    private Integer carryinNormal;
    @ApiModelProperty(value="수기(금일반입)")
    private Integer carryinManual;
    @ApiModelProperty(value="검사(금일반입)")
    private Integer carryinInspection;
    @ApiModelProperty(value="합계(금일반입)")
    private Integer carryinTotal;
    @ApiModelProperty(value="목록(금일반출)")
    private Integer carryoutInventory;
    @ApiModelProperty(value="보세운송(금일반출)")
    private Integer carryoutBondedTransport;
    @ApiModelProperty(value="일반(금일반출)")
    private Integer carryoutNormal;
    @ApiModelProperty(value="분할(금일반출)")
    private Integer carryoutPartial;
    @ApiModelProperty(value="기타(금일반출)")
    private Integer carryoutEtc;
    @ApiModelProperty(value="계(금일반출)")
    private Integer carryoutTotal;
    @ApiModelProperty(value="금일재고")
    private Integer todayStockCount;


}

package org.yht.exps.express.model.bms.dailyReport;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import java.util.List;

@Data
@ApiModel(value = "PeriodReportList", description = "기간별반출입 리스트")
@Alias("periodReportList")
public class PeriodReportList {
    @ApiModelProperty(value="리스트 개수")
    private long total;
    @ApiModelProperty(value="리스트")
    private List<PeriodReport> periodReportList;
    @ApiModelProperty(value="에러 타입")
    private CustomErrorType errorType;
    @ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
    private String scrollId;

    @ApiModelProperty(value="분할반출로 인한 차이")
    private Integer inOutDiff;
}

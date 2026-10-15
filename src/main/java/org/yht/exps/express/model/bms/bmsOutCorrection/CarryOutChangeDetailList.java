package org.yht.exps.express.model.bms.bmsOutCorrection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import java.util.List;

@Data
@ApiModel(value = "CarryOutChangeDetailList", description = "반출정정리스트, 반출 리스트")
@EqualsAndHashCode(callSuper=false)
@Alias("carryOutChangeDetailList")
public class CarryOutChangeDetailList {
    @ApiModelProperty(value="반출신청 리스트 개수")
    private long total;
    @ApiModelProperty(value="반출 리스트")
    private List<CarryOut> carryOutList;

    @ApiModelProperty(value="반출정정 리스트 개수")
    private long totalCarryOutChange;
    @ApiModelProperty(value="반출정정 리스트")
    private List<CarryOutChange> carryOutChangeList;

    @ApiModelProperty(value="에러 타입")
    private CustomErrorType errorType;
    @ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
    private String scrollId;
}

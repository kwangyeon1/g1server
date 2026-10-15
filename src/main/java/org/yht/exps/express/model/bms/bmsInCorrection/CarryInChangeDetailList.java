package org.yht.exps.express.model.bms.bmsInCorrection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.util.CustomErrorType;

import java.util.List;

@Data
@ApiModel(value = "CarryInChangeDetailList", description = "반입정정리스트, 반입 리스트")
@EqualsAndHashCode(callSuper=false)
@Alias("carryInChangeDetailList")
public class CarryInChangeDetailList {
    @ApiModelProperty(value="반입신청 리스트 개수")
    private long total;
    @ApiModelProperty(value="반입 리스트")
    private List<CarryIn> carryInList;

    @ApiModelProperty(value="반입정정 리스트 개수")
    private long totalCarryInChange;
    @ApiModelProperty(value="반입정정 리스트")
    private List<CarryInChange> carryInChangeList;

    @ApiModelProperty(value="에러 타입")
    private CustomErrorType errorType;
    @ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
    private String scrollId;
}

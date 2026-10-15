package org.yht.exps.express.model.bms.bmsInCorrection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.bms.bmsOutCorrection.CarryOutChange;
import org.yht.util.CustomErrorType;

import java.util.List;

@Data
@ApiModel(value = "CarryInChangeList", description = "반입 리스트")
@Alias("carryInChangeList")
public class CarryInChangeList {
    @ApiModelProperty(value = "리스트갯수")
    private long total;
    @ApiModelProperty(value = "리스트")
    private List<CarryInChange> carryInChangeList;
    @ApiModelProperty(value = "에러 타입")
    private CustomErrorType errorType;
    @ApiModelProperty(value = "스크롤ID, 페이징기능 사용안할 시 필요함")
    private String scrollId;
}

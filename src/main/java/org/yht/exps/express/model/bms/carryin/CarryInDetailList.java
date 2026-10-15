package org.yht.exps.express.model.bms.carryin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.bms.carryin.CarryIn;
import org.yht.exps.express.model.bms.carryin.InPlan;
import org.yht.util.CustomErrorType;

import java.util.List;

/** BL NO 로 조회시 리스트 객체 리턴*/

@Data
@ApiModel(value = "CarryInDetailList", description = "반입 리스트")
@EqualsAndHashCode(callSuper=false)
@Alias("carryInDetailList")
public class CarryInDetailList {
    @ApiModelProperty(value="반입신청 리스트 개수")
    private long total;
    @ApiModelProperty(value="반입 리스트")
    private List<CarryIn> carryInList;

    @ApiModelProperty(value="반입예정 리스트 개수")
    private long totalInPlan;
    @ApiModelProperty(value="반입예정 리스트")
    private List<InPlan> inPlanList;

    @ApiModelProperty(value="에러 타입")
    private CustomErrorType errorType;
    @ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
    private String scrollId;
}

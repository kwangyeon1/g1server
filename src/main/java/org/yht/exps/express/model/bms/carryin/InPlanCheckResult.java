package org.yht.exps.express.model.bms.carryin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

@Data
@ApiModel(value = "InPlanCheckResult", description = "반입예정 데이터 삭제전 체크사항")
@EqualsAndHashCode(callSuper=false)
@Alias("inPlanCheckResult")
public class InPlanCheckResult {
    @ApiModelProperty(value = "반입등록여부")
    private String inYn;
    @ApiModelProperty(value = "xray판독수량 or xray이미지수량이 0보다 큰 경우의 수")
    private int xrayResultCount;
    @ApiModelProperty(value = "반출승인내역통보를 반입번호로 조회시 데이터가 있는 경우")
    private int existsOutPlanCount;
}

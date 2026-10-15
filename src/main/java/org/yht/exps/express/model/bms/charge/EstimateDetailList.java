package org.yht.exps.express.model.bms.charge;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.bms.disposal.InventoryIn;
import org.yht.exps.express.model.bms.out.BmsOut;
import org.yht.exps.express.model.bms.out.BmsOutPlan;
import org.yht.util.CustomErrorType;

import java.util.List;

/** BL NO 로 조회시 리스트 객체 리턴*/

@Data
@ApiModel(value = "EstimateDetailList", description = "정산료계산 상세 조회 리스트")
@EqualsAndHashCode(callSuper=false)
@Alias("estimateDetailList")
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class EstimateDetailList {
    @ApiModelProperty(value="폐기 신청 리스트 개수")
    private long total;
    @ApiModelProperty(value="반출조회 리스트")
    private List<BmsOut> bmsOutList;

    @ApiModelProperty(value="반출승인통고 리스트 개수")
    private long totalPlan;
    @ApiModelProperty(value="반출승인통고조회 리스트")
    private List<BmsOutPlan> bmsOutPlanList;

    @ApiModelProperty(value="재고 리스트 개수")
    private long totalIn;
    @ApiModelProperty(value="재고 리스트")
    private List<InventoryIn> inventoryInList;

    @ApiModelProperty(value="창고료 리스트 개수")
    private long totalEstimate;
    @ApiModelProperty(value="창고료 리스트")
    private List<BmsEstimate> estimateList;

    @ApiModelProperty(value="에러 타입")
    private CustomErrorType errorType;
    @ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
    private String scrollId;
}

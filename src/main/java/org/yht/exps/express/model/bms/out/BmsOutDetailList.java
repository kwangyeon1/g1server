package org.yht.exps.express.model.bms.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.bms.disposal.InventoryIn;
import org.yht.util.CustomErrorType;

import java.util.List;

/** BL NO 로 조회시 리스트 객체 리턴*/

@Data
@ApiModel(value = "BmsOutDetailList", description = "반출 상세 리스트")
@EqualsAndHashCode(callSuper=false)
@Alias("bmsOutDetailList")
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsOutDetailList {
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
    
    @ApiModelProperty(value="견품반출조회 리스트")
    private List<BmsOutSample> bmsOutSampleList;
    
    @ApiModelProperty(value="에러 타입")
    private CustomErrorType errorType;
    @ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
    private String scrollId;
}

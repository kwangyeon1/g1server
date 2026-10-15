package org.yht.exps.express.model.bms.charge;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.bms.disposal.InventoryIn;
import org.yht.exps.express.model.bms.out.BmsDelivery;
import org.yht.exps.express.model.bms.out.BmsOut;
import org.yht.exps.express.model.bms.out.BmsOutPlan;

import java.util.List;

@Data
@Builder
@ApiModel(value = "EstimateDetail", description = "정산료계산 팝업 상세")
@Alias("estimateDetail")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class EstimateDetail {

    @ApiModelProperty(value="재고 상태")
    private String srchCarryInState;

    @ApiModelProperty(value="재고 조회 번호")
    private Integer srchInNo;

    @ApiModelProperty(value="반출 조회 번호")
    private Integer srchOutNo;

    @ApiModelProperty(value="반출승인 조회 번호")
    private Integer srchOutPlanNo;

    @ApiModelProperty(value="예상창고료번호")
    private  Integer   srchEstimateNo ;


    @ApiModelProperty(value="조회번호타입")
    private String srchNoType;
    @ApiModelProperty(value="조회번호 실제 번호", example="Bl No")
    private String srchContent;

    /** out_no로 조회한 객체 */
    @ApiModelProperty(value="반출 상세 조회")
    BmsOut bmsOut;

    /** out_plan_no로 조회한 객체 */
    @ApiModelProperty(value="반출 상세 조회")
    BmsOutPlan bmsOutPlan;

    /** in_no로 조회한 객체 */
    @ApiModelProperty(value="재고(BMS_IN) 상세 조회")
    InventoryIn inventoryIn;

    @ApiModelProperty(value="창고료 상세 조회")
    BmsEstimate bmsEstimate;

    @ApiModelProperty(value="D/O BlNo 리스트 조회")
    List<BmsDelivery> bmsDelivery;


    @ApiModelProperty(value="반출,반출승인,재고 그리드에서 이미 등록 여부 표시용 0보다 크면 팝업 표시 ")
    @Builder.Default
    private  Integer   dupEstimateCnt = 0 ;


    @ApiModelProperty(value="계산서 정보를 위한 객체 Estimate 객체에는 Customer코드만 있음")
    BmsCustomerInfo customerInfo;


}

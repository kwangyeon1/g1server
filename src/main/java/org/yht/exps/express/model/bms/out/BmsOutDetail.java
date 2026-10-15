package org.yht.exps.express.model.bms.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.bms.disposal.InventoryIn;
import org.yht.exps.express.model.sms.user.EcmCustomer;

import java.util.List;

@Data
@Builder
@ApiModel(value = "BmsOutDetail", description = "반출 팝업 상세")
@Alias("bmsOutDetail")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsOutDetail {

    @ApiModelProperty(value="재고 상태")
    private String srchCarryInState;

    @ApiModelProperty(value="재고 조회 번호")
    private Integer srchInNo;

    @ApiModelProperty(value="반출 조회 번호")
    private Integer srchOutNo;

    @ApiModelProperty(value="반출승인 조회 번호")
    private Integer srchOutPlanNo;

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
    
    @ApiModelProperty(value="견품반출 상세 조회")
    BmsOutSample bmsOutSample;

    @ApiModelProperty(value="컨테이너 리스트 조회")
    List<BmsContainer> bmsContainer;
    /** in_no로 조회한 객체 */
    @ApiModelProperty(value="재고(BMS_IN) 상세 조회")
    InventoryIn inventoryIn;

    @ApiModelProperty(value="D/O BlNo 리스트 조회")
    List<BmsDelivery> bmsDelivery;
    
    @ApiModelProperty(value="거래처 정보 조회")
    EcmCustomer customerInfo;




}

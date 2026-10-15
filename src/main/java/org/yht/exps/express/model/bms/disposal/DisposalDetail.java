package org.yht.exps.express.model.bms.disposal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;

@Data
@Builder
@ApiModel(value = "Disposal detail", description = "disposal 팝업 상세")
@Alias("disposalDetail")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class DisposalDetail {

    @ApiModelProperty(value="재고 상태")
    private String srchCarryInState;

    @ApiModelProperty(value="재고 조회 번호")
    private Integer srchInNo;

    @ApiModelProperty(value="폐기신정 조회 번호")
    private String srchDisposalNo;

    @ApiModelProperty(value="조회번호타입")
    private String srchNoType;
    @ApiModelProperty(value="조회번호 실제 번호", example="Bl No")
    private String srchContent;

    /** disposal_no로 조회한 객체 */
    @ApiModelProperty(value="폐기신청 상세 조회")
    Disposal disposal;

    /** in_no로 조회한 객체 */
    @ApiModelProperty(value="재고(BMS_IN) 상세 조회")
    InventoryIn inventoryIn;


    /** disposal_no로 조회한 객체 */
    @ApiModelProperty(value="폐기 업체 상세 조회")
    Disposal dispoCustomer;



}

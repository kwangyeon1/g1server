package org.yht.exps.express.model.bms.bmsOutCorrection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

@Data
@ApiModel(value = "CarryOutChangeDetail")
@Builder
@Alias("carryOutChangeDetail")
@AllArgsConstructor
@NoArgsConstructor
public class CarryOutChangeDetail {
    @ApiModelProperty(value="반출정정 상태")
    private String srchCarryOutState;

    @ApiModelProperty(value="반출 조회 번호")
    private Integer srchOutNo;

    @ApiModelProperty(value="반출정정신청 조회 번호")
    private Integer srchChangeNo;

    @ApiModelProperty(value="조회번호타입")
    private String srchNoType;
    @ApiModelProperty(value="조회번호 실제 번호", example="Bl No")
    private String srchContent;

    @ApiModelProperty(value = "반출정정항목 리스트 갯수")
    private Integer changeItemCount;

    /** CHANGE_NO로 조회한 객체 */
    @ApiModelProperty(value="반출정정신청 상세 조회")
    CarryOutChange carryOutChange;

    /** OUT_NO로 조회한 객체 */
    @ApiModelProperty(value="반출(BMS_OUT) 상세 조회")
    CarryOut carryOut;
}

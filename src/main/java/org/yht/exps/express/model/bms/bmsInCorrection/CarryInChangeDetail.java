package org.yht.exps.express.model.bms.bmsInCorrection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.bms.carryin.CarryIn;


@Data
@Builder
@ApiModel(value = "CarryInChangeDetail", description = "반입정정 상세")
@Alias("carryInChangeDetail")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class CarryInChangeDetail {

    @ApiModelProperty(value="반입정정 상태")
    private String srchCarryInState;

    @ApiModelProperty(value="반입 조회 번호")
    private Integer srchInNo;

    @ApiModelProperty(value="반입정정신청 조회 번호")
    private Integer srchChangeNo;

    @ApiModelProperty(value="조회번호타입")
    private String srchNoType;
    @ApiModelProperty(value="조회번호 실제 번호", example="Bl No")
    private String srchContent;

    @ApiModelProperty(value = "반입정정항목 리스트 갯수")
    private Integer changeItemCount;

    /** CHANGE_NO로 조회한 객체 */
    @ApiModelProperty(value="반입정정신청 상세 조회")
    CarryInChange carryInChange;

    /** IN_NO로 조회한 객체 */
    @ApiModelProperty(value="반입(BMS_IN) 상세 조회")
    CarryIn carryIn;
}

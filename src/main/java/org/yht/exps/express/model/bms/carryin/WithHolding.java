package org.yht.exps.express.model.bms.carryin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;

@Data
@Builder
@ApiModel(value = "WithHolding", description = "보류")
@Alias("withHolding")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class WithHolding {
    @ApiModelProperty(value="mrn 11자리")
    private String mrn;
    @ApiModelProperty(value="입항일자")
    private String arDate;
    @ApiModelProperty(value="선기명")
    private String vesselFlight;
}

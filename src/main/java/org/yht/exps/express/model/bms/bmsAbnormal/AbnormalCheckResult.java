package org.yht.exps.express.model.bms.bmsAbnormal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

@Data
@ApiModel(value = "AbnormalCheckResult", description = "반입물품이상 데이터 삭제전 체크사항")
@EqualsAndHashCode(callSuper=false)
@Alias("abnormalCheckResult")
public class AbnormalCheckResult {
    @ApiModelProperty(value="신고상태")
    private String lastPodStatus;
}

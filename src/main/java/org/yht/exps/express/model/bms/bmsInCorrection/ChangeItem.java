package org.yht.exps.express.model.bms.bmsInCorrection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@Builder
@ApiModel(value = "ChangeItem", description = "정정항목")
@Alias("changeItem")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class ChangeItem extends ResultList {
    @ApiModelProperty(value="반출입정정신청번호")
    private Integer changeNo;
    @ApiModelProperty(value="일련번호")
    private Integer seqNo;
    @ApiModelProperty(value = "항목정정구분")
    private String changeCode;
    @ApiModelProperty(value = "정정전내용")
    private String changeBefore;
    @ApiModelProperty(value = "정정후내용")
    private String changeAfter;
}

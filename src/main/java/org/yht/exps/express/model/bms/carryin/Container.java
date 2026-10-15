package org.yht.exps.express.model.bms.carryin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@Builder
@ApiModel(value = "Container", description = "콘테이너 정보")
@Alias("container")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class Container extends ResultList {

    @ApiModelProperty(value="반입신고번호")
    private String customsReportNo;
    @ApiModelProperty(value="반입번호")
    private Integer inNo;
    @ApiModelProperty(value="컨테이너번호")
    @Builder.Default
    private String cntrNo = "";
    @ApiModelProperty(value="컨테이너SIZE")
    @Builder.Default
    private String cntrEdiCode = "";
    @ApiModelProperty(value="봉인번호1")
    @Builder.Default
    private String sealNo1 = "";
    @ApiModelProperty(value="봉인번호2")
    @Builder.Default
    private String sealNo2 = "";
    @ApiModelProperty(value="봉인번호3")
    @Builder.Default
    private String sealNo3 = "";
}

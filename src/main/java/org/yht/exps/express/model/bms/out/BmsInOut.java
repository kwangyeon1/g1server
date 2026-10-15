package org.yht.exps.express.model.bms.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;


/***
 *  @Builder.Default 를 쓰는 이유는 디비 컬럼 모두가 NOT NULL로 되어 있어서 입력되지 않은 항목을 모두 입력 시키기 위해 선언함
 */
@Data
@Builder
@ApiModel(value = "BmsInOut", description = "반입, 반출 VO 객체")
@Alias("bmsInOut")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsInOut {
    @ApiModelProperty(value="결과코드")
    private String resultCode;

    @ApiModelProperty(value="반출신고번호")
    @Builder.Default private String customsReportNo632 = "";

    @ApiModelProperty(value="화물관리번호")
    @Builder.Default private String customsReportNo6nb = "";

    @ApiModelProperty(value="화물관리번호")
    @Builder.Default private String cargoReferenceNo632	= "";

    @ApiModelProperty(value="화물관리번호")
    @Builder.Default private String cargoReferenceNo6nb = "";
}

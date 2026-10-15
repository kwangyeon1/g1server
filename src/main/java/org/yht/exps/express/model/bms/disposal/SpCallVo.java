package org.yht.exps.express.model.bms.disposal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;

@Data
@Builder
@ApiModel(value = "SpCallVo", description = "저장 프로시저 호출  VO 객체")
@Alias("spCallVo")
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class SpCallVo {
    @ApiModelProperty(value="A(Auto), M(Manual)")
    String mode ;
    @ApiModelProperty(value="사업장 보세 구역번호 8자리 ")
    String bmsWhCode;
    @ApiModelProperty(value="I.반입, O.반출")
    String inOut;
    @ApiModelProperty(value=" 반입/반출 신고 번호(CUSTOMS_REPORT_NO) 채번")
    String retCustomsReportNo;
}

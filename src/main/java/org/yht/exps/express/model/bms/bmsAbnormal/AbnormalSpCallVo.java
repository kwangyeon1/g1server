package org.yht.exps.express.model.bms.bmsAbnormal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

@Data
@Builder
@ApiModel(value = "AbnormalSpCallVo", description = "반입이상제출번호 저장 프로시저 호출  VO 객체")
@Alias("abnormalSpCallVo")
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class AbnormalSpCallVo {
    @ApiModelProperty(value="A(Auto), M(Manual)")
    String mode ;
    @ApiModelProperty(value="사업장 보세 구역번호 8자리 ")
    String bmsWhCode;
    @ApiModelProperty(value=" 반입/반출 신고 번호(CUSTOMS_REPORT_NO) 채번")
    String retCustomsReportNo;
}

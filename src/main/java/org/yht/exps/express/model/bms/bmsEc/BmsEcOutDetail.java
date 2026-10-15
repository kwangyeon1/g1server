package org.yht.exps.express.model.bms.bmsEc;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@Builder
@ApiModel(value = "BmsEcOutDetail", description = "EC 반출 상세")
@Alias("bmsEcOutDetail")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsEcOutDetail extends ResultList {
	@ApiModelProperty(value="인터페이스ID")
    private String interfaceId;
    @ApiModelProperty(value="인터페이스SEQ")
    private Integer interfaceSeq;
    @ApiModelProperty(value="인터페이스LINE번호")
    private Integer interfaceLineNo;
    @ApiModelProperty(value="CUD")
    private String isCud;
    @ApiModelProperty(value="택배번호")
    private String takbaeLblid;
    @ApiModelProperty(value="스캔일시")
    private String scanYmdhms;
    
}

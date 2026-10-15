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
@ApiModel(value = "BmsEcInPlanRsgDetail", description = "EC 반입예정 상세 RSG")
@Alias("bmsEcInPlanRsgDetail")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsEcInPlanRsgDetail extends ResultList {
	@ApiModelProperty(value="인터페이스ID")
    private String interfaceId;
    @ApiModelProperty(value="인터페이스SEQ")
    private Integer interfaceSeq;
    @ApiModelProperty(value="STATCD")
    private String statcd;
    @ApiModelProperty(value="STATMSG")
    private String statmsg;
    @ApiModelProperty(value="XDATETIME")
    private String xdatetime;
    @ApiModelProperty(value="송신시스템명")
    private String sndSysCd;
    @ApiModelProperty(value="수신시스템명")
    private String recvSysCd;
    @ApiModelProperty(value="인터페이스 진행상태")
    private String sndStatCd;
    @ApiModelProperty(value="송신에러메세지")
    private String errMsg;
    @ApiModelProperty(value="송신일자")
    private String sndYmd;
    @ApiModelProperty(value="송신시각")
    private String sndHms;
    @ApiModelProperty(value="등록일시")
    private String dtReg;
    
}

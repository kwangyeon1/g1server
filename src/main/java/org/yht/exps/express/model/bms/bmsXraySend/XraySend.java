package org.yht.exps.express.model.bms.bmsXraySend;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "xraySend", description = "xray 재전송 리스트")
@Alias("xraySend")
@EqualsAndHashCode(callSuper=false)
public class XraySend extends ResultList{
	@ApiModelProperty(value="seq")
	private String seq;
	@ApiModelProperty(value="packingNo")
	private String packingNo;
	@ApiModelProperty(value="전송상태")
	private String sendStatus;
	@ApiModelProperty(value="참조번호(패킹번호,반출승인번호)")
	private String referenceNo;
	@ApiModelProperty(value="blNo")
	private String blNo;
    @ApiModelProperty(value="등록일시")
    private String registerDateTime;
    @ApiModelProperty(value="재전송일시")
    private String updateDateTime;
    @ApiModelProperty(value="전송타입")
    private String sendType;
    @ApiModelProperty(value="전송타입명")
    private String sendTypeName;
}

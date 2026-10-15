package org.yht.exps.express.model.bms.bmsXraySend;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "xraySendBl", description = "xray 재전송 > ADD BL 리스트")
@Alias("xraySendBl")
@EqualsAndHashCode(callSuper=false)
public class XraySendBl extends ResultList{
	@ApiModelProperty(value="blNo")
	private String blNo;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="mblNo")
	private String mblNo;
	@ApiModelProperty(value="화물관리번호")
	private String cargoReferenceNo;
	
	@ApiModelProperty(value="packingNo")
	private String packingNo;
	@ApiModelProperty(value="X-RAY검사")
	private String xrayInspectionCode;
	@ApiModelProperty(value="선별")
	private String seInspectionCode;
	@ApiModelProperty(value="집중검사")
	private String inspectionCustomsYn;
	
	@ApiModelProperty(value="승인일자")
	private String approvalDate;
	@ApiModelProperty(value="반출승인여부")
	private String approvalClass;
	@ApiModelProperty(value="통보일시")
	private String notifyDateTime;
	@ApiModelProperty(value="승인근거번호")
	private String outApprovalNo;
	@ApiModelProperty(value="반출구분")
	private String outClass;
}

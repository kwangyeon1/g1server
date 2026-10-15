package org.yht.exps.express.model.edi;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ManifestCorrectRecvResult", description = "전자상거래  적하목록정정 수신결과내역")
@Alias("manifestCorrectRecvResult")
public class ManifestCorrectRecvResult {
	
	@ApiModelProperty(value="수신일시")
	private String dateTime;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="MSN")
	private String msn;
	@ApiModelProperty(value="HSN")
	private String hsn;
	@ApiModelProperty(value="수신결과")
	private String statementCode;
	@ApiModelProperty(value="오류정보")
	private String statementReason;	
}


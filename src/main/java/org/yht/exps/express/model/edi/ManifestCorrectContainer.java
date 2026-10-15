package org.yht.exps.express.model.edi;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ManifestCorrectContainer", description = "전자상거래  적하목록수출 컨테이너 리스트")
@Alias("manifestCorrectContainer")
public class ManifestCorrectContainer {
	
	@ApiModelProperty(value="HSN")
	private String hsn;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="정정구분")
	private String modType;
	@ApiModelProperty(value="정정사유")
	private String modReason;
	@ApiModelProperty(value="단일항목정정_코드")
	private String singleModCode;
	@ApiModelProperty(value="단일항목정정_사유코드")
	private String singleModReasonCode;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="MSN")
	private String msn;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="정정번호")
	private String modNo;
	@ApiModelProperty(value="신청일자")
	private String modReqDate;
	@ApiModelProperty(value="전송일자/시간")
	private String sendDateTime;
	@ApiModelProperty(value="결과일자/시간")
	private String resultDateTime;
	@ApiModelProperty(value="적하목록정정_관리번호")
	private String modJobNo;
	@ApiModelProperty(value="일련번호")
	private int seqIdNo;
	@ApiModelProperty(value="컨테이너일련번호")
	private int seqNoId;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="정정전내역")
	private String beforeValue;
	@ApiModelProperty(value="정정후내역")
	private String afterValue;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;

	@ApiModelProperty(value="컨테이너번호")
	private String cntrNo;
	@ApiModelProperty(value="컨테이너정정코드")
	private String cntrModCode;
	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;	
	
}


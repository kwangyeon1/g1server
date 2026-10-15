package org.yht.exps.express.model.edi;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "CustomsManifestRes", description = "전자상거래  적하목록수입 하우스 리스트")
@Alias("customsManifestRes")
public class CustomsManifestRes extends ResultList{
	
	@ApiModelProperty(value="제출번호")
	private String simpleDocNo;
	@ApiModelProperty(value="세관")
	private String customsName;
	@ApiModelProperty(value="과부호")
	private String customsDptName;
	@ApiModelProperty(value="수신일시")
	private String resDateTime;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="MSN")
	private String msn;
	@ApiModelProperty(value="HSN")
	private String hsn;
	@ApiModelProperty(value="문서유형")
	private String simpleEdiStatus;
	@ApiModelProperty(value="H.B/L번호")
	private String hblNo;
	@ApiModelProperty(value="오류내용")
	private String simpleErrorDesc;
	@ApiModelProperty(value="검사결과")
	private String seInspectionCodeName;
	@ApiModelProperty(value="취하사유")
	private String dropCodeName;
	@ApiModelProperty(value="통관여부")
	private String clearanceYn;
	
}


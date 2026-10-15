package org.yht.exps.express.model.edi.search;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchManifestSimpleDocNo", description = "적하목록 목록신고제출번호 조회 ")
@Alias("searchManifestSimpleDocNo")
public class SearchManifestSimpleDocNo{
	
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;
	@ApiModelProperty(value="항공/해상")
	private String bound;
	@ApiModelProperty(value="수입/수출")
	private String boundType;
	@ApiModelProperty(value="목적항")
	private String destPort;
	@ApiModelProperty(value="출발지")
	private String origCode;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="목록신고제출번호")
	private String simpleDocNo;
	@ApiModelProperty(value="목록신고제출일련번호")
	private String simpleDocNoSeqNo;
	@ApiModelProperty(value="사용자아이디")
	private String userId;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	
}

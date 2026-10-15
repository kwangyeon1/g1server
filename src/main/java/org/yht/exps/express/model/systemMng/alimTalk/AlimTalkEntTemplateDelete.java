package org.yht.exps.express.model.systemMng.alimTalk;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AlimTalkEntTemplateDelete", description = "시스템관리 알림톡 업체 - 템플릿 삭제")
@Alias("alimTalkEntTemplateDelete")
public class AlimTalkEntTemplateDelete {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="바운드", example="COMMON, AI, AO, OI, OO")
	private String bound;
	@ApiModelProperty(value="템플릿타입", example="E01")
	private String templateType;
	@ApiModelProperty(value="템플릿 시퀀스 넘버", example="1")
	private String templateSeqNo;	
	@ApiModelProperty(value="템플릿코드(세종)", example="SJ_039501")
	private String templateCode;	
}

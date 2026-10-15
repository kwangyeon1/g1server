package org.yht.exps.express.model.systemMng.alimTalk;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AlimTalkTemplateDelete", description = "시스템관리 알림톡 템플릿정보 삭제")
@Alias("alimTalkTemplateDelete")
public class AlimTalkTemplateDelete {
	@ApiModelProperty(value="템플릿타입", example="E01")
	private String templateType;		
	@ApiModelProperty(value="템플릿 시퀀스 넘버", example="1")
	private String templateSeqNo;	
	@ApiModelProperty(value="템플릿이름")
	private String templateName;	

	
}

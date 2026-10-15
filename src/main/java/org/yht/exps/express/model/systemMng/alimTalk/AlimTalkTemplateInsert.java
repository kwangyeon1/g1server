package org.yht.exps.express.model.systemMng.alimTalk;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AlimTalkTemplateInsert", description = "시스템관리 알림톡 템플릿 등록")
@Alias("alimTalkTemplateInsert")
public class AlimTalkTemplateInsert {
	@ApiModelProperty(value="템플릿타입", example="E01")
	private String templateType;
	@ApiModelProperty(value="템플릿 시퀀스 넘버", example="1")
	private String templateSeqNo;	
	@ApiModelProperty(value="템플릿이름")
	private String templateName;
	@ApiModelProperty(value="템플릿코드")
	private String templateCode;	
	@ApiModelProperty(value="제목")
	private String subject;		
	@ApiModelProperty(value="템플릿내용")
	private String template;	
	@ApiModelProperty(value="대체 템플릿내용")
	private String resendTemplate;
	@ApiModelProperty(value="버튼1 타입")
	private String button1Type;
	@ApiModelProperty(value="버튼1 이름")
	private String button1Name;
	@ApiModelProperty(value="버튼1 URL")
	private String button1Url;
	@ApiModelProperty(value="버튼2 타입")	
	private String button2Type;
	@ApiModelProperty(value="버튼2 이름")
	private String button2Name;
	@ApiModelProperty(value="버튼2 URL")
	private String button2Url;	
	@ApiModelProperty(value="버튼3 타입")	
	private String button3Type;
	@ApiModelProperty(value="버튼3 이름")
	private String button3Name;
	@ApiModelProperty(value="버튼3 URL")
	private String button3Url;	
	@ApiModelProperty(value="버튼4 타입")		
	private String button4Type;
	@ApiModelProperty(value="버튼4 이름")
	private String button4Name;
	@ApiModelProperty(value="버튼4 URL")
	private String button4Url;	
	@ApiModelProperty(value="버튼5 타입")		
	private String button5Type;
	@ApiModelProperty(value="버튼5 이름")
	private String button5Name;
	@ApiModelProperty(value="버튼5 URL")
	private String button5Url;		
	@ApiModelProperty(value="등록아이디")
	private String registerUserId;	
	@ApiModelProperty(value="등록일자")
	private String registerDateTime;	
	@ApiModelProperty(value="수정아이디")
	private String updateUserId;	
	@ApiModelProperty(value="수정일자")
	private String updateDateTime;	
	@ApiModelProperty(value="GMT_TIME")
	private String gmtTime;
	
}

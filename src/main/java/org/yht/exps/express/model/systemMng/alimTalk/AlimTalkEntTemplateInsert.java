package org.yht.exps.express.model.systemMng.alimTalk;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AlimTalkEntTemplateInsert", description = "시스템관리 알림톡 업체-템플릿 연결 등록")
@Alias("alimTalkEntTemplateInsert")
public class AlimTalkEntTemplateInsert {

	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="바운드", example="COMMON, AI, AO, OI, OO")
	private String bound;	
	@ApiModelProperty(value="템플릿 시퀀스 넘버", example="1")
	private String templateSeqNo;	
	@ApiModelProperty(value="템플릿타입", example="E01")
	private String templateType;		
	@ApiModelProperty(value="템플릿코드(세종)", example="SJ_039501")
	private String templateCode;		
	@ApiModelProperty(value="템플릿이름")
	private String templateName;		
	@ApiModelProperty(value="웹 화면에서 보여지는 알림톡 라벨 이름")
	private String labelName;		
	@ApiModelProperty(value="재전송 메시지 타입", example="0:카카오, 4:SMS로 에이전트에서 분할 재전송, 5:LMS로 에이전트에서 분할 재전송, 6:MMS로 에이전트에서 분할 재전송, 7:SMS로 에이전트에서 텍스트 대체 재전송, 8:LMS로 에이전트에서 텍스트 대체 재전송(기본), 9:MMS로 에이전트에서 텍스트 대체 재전송" )
	private String resendMsgType;		
	@ApiModelProperty(value="오더 생성시 자동전송 여부", example="Y, N")
	private String createAutoYn;	
	@ApiModelProperty(value="마감(Finish)시 자동전송 여부", example="Y, N")
	private String finishAutoYn;	
	@ApiModelProperty(value="메시지 타입" , example="1:SMS, 2:SMS_URL, 3:MMS, 4:MMS URL, 5: 국제 SMS text, 6:알림톡, 7:친구톡")
	private String msgType;	
	@ApiModelProperty(value="템플릿 내용")
	private String template;	
	@ApiModelProperty(value="재전송용 템플릿 내용")
	private String resendTemplate;	
	@ApiModelProperty(value="등록아이디")
	private String registerUserId;	
	@ApiModelProperty(value="등록일자")
	private String updateUserId;
	@ApiModelProperty(value="GMT_TIME")
	private String gmtTime;
	
	
}

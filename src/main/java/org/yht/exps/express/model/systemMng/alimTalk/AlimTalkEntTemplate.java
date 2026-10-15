package org.yht.exps.express.model.systemMng.alimTalk;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AlimTalkEntTemplate", description = "시스템관리 알림톡 업체등록정보")
@Alias("alimTalkEntTemplate")
public class AlimTalkEntTemplate {

	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
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
	@ApiModelProperty(value="MMS 제목")
	private String subject;
	@ApiModelProperty(value="버튼1 타입")
	private String button1Type;
	@ApiModelProperty(value="버튼1 명")
	private String button1Name;
	@ApiModelProperty(value="버튼1 URL")
	private String button1Url;
	@ApiModelProperty(value="버튼2 타입")
	private String button2Type;
	@ApiModelProperty(value="버튼2 명")
	private String button2Name;
	@ApiModelProperty(value="버튼2 URL")
	private String button2Url;
	@ApiModelProperty(value="버튼3 타입")
	private String button3Type;
	@ApiModelProperty(value="버튼3 명")
	private String button3Name;
	@ApiModelProperty(value="버튼3 URL")
	private String button3Url;
	@ApiModelProperty(value="버튼4 타입")
	private String button4Type;
	@ApiModelProperty(value="버튼4 명")
	private String button4Name;
	@ApiModelProperty(value="버튼4 URL")
	private String button4Url;
	@ApiModelProperty(value="버튼5 타입")
	private String button5Type;
	@ApiModelProperty(value="버튼5 명")
	private String button5Name;
	@ApiModelProperty(value="버튼5 URL")
	private String button5Url;
	

	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;		
	
	
}

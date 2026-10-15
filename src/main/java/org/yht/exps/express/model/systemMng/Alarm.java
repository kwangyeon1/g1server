package org.yht.exps.express.model.systemMng;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "Alarm", description = "알림톡")
@Alias("alarm")
public class Alarm {
//	@ApiModelProperty(value="일련번호")
//	private String seqNoId;
//	@ApiModelProperty(value="도입사코드")
//	private String entCode;
//	@ApiModelProperty(value="사업장코드")
//	private String entBusinessCode;
//	@ApiModelProperty(value="솔루션")
//	private String solution;
//	@ApiModelProperty(value="관리번호")
//	private String refJobNo;
	@ApiModelProperty(value="알림톡_구분")
	private String jobType;
	@ApiModelProperty(value="알림톡_구분(명)")
	private String jobTypeName;
//	@ApiModelProperty(value="결과메세지_KEY")
//	private String msgKey;
	@ApiModelProperty(value="알림톡_담당자")
	private String alarmAttnId;
	@ApiModelProperty(value="송신일시")
	private String sendDateTime;
	@ApiModelProperty(value="수신일시")
	private String recvDateTime;
	@ApiModelProperty(value="변경전")
	private String beforeValue;
	@ApiModelProperty(value="변경후")
	private String afterValue;
//	@ApiModelProperty(value="전송상태")
//	private String status;
//	@ApiModelProperty(value="전송결과")
//	private String resultType;
//	@ApiModelProperty(value="전송결과값")
//	private String resultCode;
//	@ApiModelProperty(value="결과로그")
//	private String resultLog;
//	@ApiModelProperty(value="입력자코드")
//	private String registerUserId;
//	@ApiModelProperty(value="입력일시")
//	private String registerDateTime;
//	@ApiModelProperty(value="수정자코드")
//	private String updateUserId;
//	@ApiModelProperty(value="수정일시")
//	private String updateDateTime;
	@ApiModelProperty(value="에러내용")
	private String errorDesc;
}
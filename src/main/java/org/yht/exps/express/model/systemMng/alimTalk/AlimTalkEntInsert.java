package org.yht.exps.express.model.systemMng.alimTalk;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AlimTalkEntInsert", description = "시스템관리 알림톡 업체등록")
@Alias("alimTalkEntInsert")
public class AlimTalkEntInsert {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="발신번호")
	private String smsSendTelNo;	
	@ApiModelProperty(value="세종텔레콤 ID")
	private String smsUserId;	
	@ApiModelProperty(value="세종텔레콤 CID")
	private String smsCidNum;	
	
}

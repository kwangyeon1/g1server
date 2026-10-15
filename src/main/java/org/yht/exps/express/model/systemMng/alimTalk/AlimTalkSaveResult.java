package org.yht.exps.express.model.systemMng.alimTalk;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AlimTalkSaveResult", description = "시스템관리 알림톡 Save 결과")
@Alias("alimTalkSaveResult")
public class AlimTalkSaveResult {
	@ApiModelProperty(value="관리번호")
	private String seqNo;
	@ApiModelProperty(value="결과코드")
	private String resultCode;

}

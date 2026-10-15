package org.yht.exps.express.model.systemMng.alimTalk;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AlimTalkEntDelete", description = "시스템관리 알림톡 업체등록정보 삭제")
@Alias("alimTalkEntDelete")
public class AlimTalkEntDelete {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="바운드", example="COMMON, AI, AO, OI, OO")
	private String bound;
	@ApiModelProperty(value="카카오채널 ID")
	private String kakaoPlusId;
	@ApiModelProperty(value="카카오사용 하는 업체명")
	private String kakaoPlusName;
	
	
}

package org.yht.exps.express.model.sms.message;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "Message", description = "다국어 메세지")
@Alias("message")
public class Message {
	@ApiModelProperty(value="메세지 ID")
	private String messageId;
	@ApiModelProperty(value="메세지")
	private String messageName;
}

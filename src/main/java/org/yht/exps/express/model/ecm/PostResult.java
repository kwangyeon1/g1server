package org.yht.exps.express.model.ecm;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.sms.message.Message;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "PostResult", description = "데이터 입력/수정/삭제 결과")
@Alias("postResult")
public class PostResult {
	@ApiModelProperty(value="결과", example="ok or fail")
	private String result;
	@ApiModelProperty(value="Index", example="ecm-process-logs-2010.11.27")
	private String index;
	@ApiModelProperty(value="id", example="324234234234")
	private String id;
	@ApiModelProperty(value="업데이트 결과 메세지 리스트")
	private List<Message> messageList;
}

package org.yht.exps.express.model;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "Notification", description = "알람(Notification)")
@Alias("notification")
public class Notification {
	@ApiModelProperty(value="Batch 처리 결과", example="success, error")
	private String result;
	@ApiModelProperty(value="메세지명")
	private String messageDefault;
	@ApiModelProperty(value="메세지명_한글")
	private String messageKor;
	@ApiModelProperty(value="메세지명_영문")
	private String messageEng;
	@ApiModelProperty(value="메세지명_일어")
	private String messageJpn;
	@ApiModelProperty(value="메세지명_중문")
	private String messageChi;
	@ApiModelProperty(value="메세지명_스페인어")
	private String messagespa;
	@ApiModelProperty(value="메세지명_베트남어")
	private String messageVie;
	@ApiModelProperty(value="Batch명")
	private String batchTypeDefault;
	@ApiModelProperty(value="Batch명_한글")
	private String batchTypeKor;
	@ApiModelProperty(value="Batch명_영문")
	private String batchTypeEng;
	@ApiModelProperty(value="Batch명_일어")
	private String batchTypeJpn;
	@ApiModelProperty(value="Batch명_중문")
	private String batchTypeChi;
	@ApiModelProperty(value="Batch명_스페인어")
	private String batchTypeSpa;
	@ApiModelProperty(value="Batch명_베트남어")
	private String messagebatchTypeVieVie;
	@ApiModelProperty(value="Total Count")
	private int totalCount;
	@ApiModelProperty(value="Fail Count")
	private int failCount;
	@ApiModelProperty(value="메세지 발생시간")
	private long messageDatetime;
}

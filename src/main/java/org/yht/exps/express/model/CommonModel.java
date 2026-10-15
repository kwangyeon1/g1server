package org.yht.exps.express.model;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "Common", description = "공통항목")
@Alias("common")
public class CommonModel {
	@ApiModelProperty(value="ElasticSearch Index명")
	private String index;
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="House Routing정보(Master와 Console되어 있을 경우 Master Job No가 된다)")
	private String routing;
	@ApiModelProperty(value="날짜 포맷")
	private String format;
	@ApiModelProperty(value="GMT 기준시간 차이")
	private int gmtTime;
	@ApiModelProperty(value="Job No Stock 발행 연월")
	private String stockIssueYymm;
	@ApiModelProperty(value="Job No Stock 발행 일자")
	private String stockInputDate;
	@ApiModelProperty(value="등록자 ID")
	private String userId;
	@ApiModelProperty(value="등록자명")
	private String userName;
	@ApiModelProperty(value="사업장별 국가코드")
	private String userCountryCode;
	@ApiModelProperty(value="실적일자적용시점")
	private String userSetProfitDateType;
	@ApiModelProperty(value="다국어타입")
	private String langType;
}

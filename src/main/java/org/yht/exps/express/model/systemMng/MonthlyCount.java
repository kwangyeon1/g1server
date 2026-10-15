package org.yht.exps.express.model.systemMng;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "MonthlyCount", description = "전자상거래 월별건수")
@Alias("monthlyCount")
@EqualsAndHashCode(callSuper=false)
public class MonthlyCount extends ResultList{

	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="기준년월")
	private String odYymm;
	@ApiModelProperty(value="Total")
	private int totCnt;
	@ApiModelProperty(value="Air Outbound")
	private int aoCnt;
	@ApiModelProperty(value="Air Inbound")
	private int aiCnt;
	@ApiModelProperty(value="Sea Outbound")
	private int ooCnt;
	@ApiModelProperty(value="Sea Inbound")
	private int oiCnt;
	@ApiModelProperty(value="KTNET")
	private int ktnetCnt;
	@ApiModelProperty(value="KCNET")
	private int kcnetCnt;
	@ApiModelProperty(value="KLNET")
	private int klnetCnt;
	@ApiModelProperty(value="API Total")
	private int apiTotCnt;
	@ApiModelProperty(value="API Air Outbound")
	private int apiAoCnt;
	@ApiModelProperty(value="API Air Inbound")
	private int apiAiCnt;
	@ApiModelProperty(value="API Sea Outbound")
	private int apiOoCnt;
	@ApiModelProperty(value="API Sea Inbound")
	private int apiOiCnt;
	@ApiModelProperty(value="한진택배")
	private int apiHanjinCnt;
	@ApiModelProperty(value="대한통운")
	private int apiKorexCnt;
	@ApiModelProperty(value="API Tracking Cnt")
	private int apiTrackingCnt;
	
}

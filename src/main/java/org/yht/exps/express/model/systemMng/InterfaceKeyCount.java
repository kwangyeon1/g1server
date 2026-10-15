package org.yht.exps.express.model.systemMng;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "InterfaceKeyCount", description = "전자상거래 Interface Key건수")
@Alias("interfaceKeyCount")
@EqualsAndHashCode(callSuper=false)
public class InterfaceKeyCount extends ResultList{

	@ApiModelProperty(value="기준년월")
	private String interfaceYymm;
	@ApiModelProperty(value="항공수출건수")
	private int apiAoCnt;
	@ApiModelProperty(value="항공수입건수")
	private int apiAiCnt;
	@ApiModelProperty(value="해상수출건수")
	private int apiOoCnt;
	@ApiModelProperty(value="해상수입건수")
	private int apiOiCnt;
	@ApiModelProperty(value="한진건수")
	private int apiHanjinCnt;
	@ApiModelProperty(value="대한통운건수")
	private int apiKorexCnt;
	@ApiModelProperty(value="트래킹건수")
	private int apiTrackingCnt;
	
}

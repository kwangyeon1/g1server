package org.yht.exps.express.model.edi;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ManifestAggregate", description = "전자상거래  적하목록수입 통관목록집계 리스트")
@Alias("manifestAggregate")
public class ManifestAggregate {

	@ApiModelProperty(value="관리번호")
	private String hblNo;
	@ApiModelProperty(value="전자상거래업체")
	private String url;
	@ApiModelProperty(value="목로건수")
	private String cdTypeYCnt;
	@ApiModelProperty(value="목록배제건수")
	private String cdTypeBCnt;
	@ApiModelProperty(value="일반건수")
	private String cdTypeNCnt;
	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;
	
}

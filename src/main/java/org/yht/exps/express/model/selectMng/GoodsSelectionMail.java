package org.yht.exps.express.model.selectMng;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "GoodsSelectionMail", description = "통관목록 자동선별(기초) 메일")
@Alias("goodsSelectionMail")
//@EqualsAndHashCode(callSuper=false)
public class GoodsSelectionMail {
	@ApiModelProperty(value="마스터번호")	
	private String mblNo;
	@ApiModelProperty(value="입항일자")	
	private String arDate;
	@ApiModelProperty(value="편명")	
	private String vesselFlight;
	@ApiModelProperty(value="Agent")	
	private String custCode;
	@ApiModelProperty(value="관세사")	
	private String cbCode;
	@ApiModelProperty(value="관세사명")	
	private String cbName;
	@ApiModelProperty(value="관세사이메일")	
	private String cbEmail;
	@ApiModelProperty(value="일반건수")	
	private int cdNtypeCnt;
	@ApiModelProperty(value="배제건수")	
	private int cdBtypeCnt;
	@ApiModelProperty(value="일반+배제건수")
	private int cdNbTypeCnt;
}

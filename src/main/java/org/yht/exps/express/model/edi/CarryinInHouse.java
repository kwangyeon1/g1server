package org.yht.exps.express.model.edi;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "CarryinInHouse", description = "전자상거래  적하목록수입 하우스 리스트")
@Alias("carryinInHouse")
public class CarryinInHouse {
	
	@ApiModelProperty(value="번호")
	public int num;
	@ApiModelProperty(value="업체명:도입사명")
	public String entName;
	@ApiModelProperty(value="입항일")
	public String arDate;
	@ApiModelProperty(value="적재국")
	public String origCountry;
	@ApiModelProperty(value="적재항")
	public String origName;
	@ApiModelProperty(value="양륙항")
	public String destName;
	@ApiModelProperty(value="컨테이너번호")
	public String cntrNo;
	@ApiModelProperty(value="컨테이너규격")
	public String cbm;

	@ApiModelProperty(value="반입건수")
	public String cnt;
	@ApiModelProperty(value="반입개수")
	public String pieces;
	@ApiModelProperty(value="반입중량")
	public String gwt;
	@ApiModelProperty(value="M.B/L No")
	public String mblNo;
	@ApiModelProperty(value="비고")
	public String remarks;
	
	@ApiModelProperty(value="조회건수")
	private int total;
}

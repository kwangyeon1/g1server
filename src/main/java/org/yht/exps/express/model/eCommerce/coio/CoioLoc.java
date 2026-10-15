package org.yht.exps.express.model.eCommerce.coio;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "CoioLoc", description = "전자상거래 반입/반출 location tab")
@Alias("CoioLoc")
public class CoioLoc{

	@ApiModelProperty(value="로케이션 코드")
	private String whLocation;	
	@ApiModelProperty(value="로케이션명")
	private String whLocationName;
	@ApiModelProperty(value="AGENT_코드")
	private String agentCode;		
	@ApiModelProperty(value="하우스건수")
	private int hblCnt;	
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="총중량")
	private double gwt;	
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	@ApiModelProperty(value="도입사코드 5자리 + 국가 2자리")
	private String entBusinessCode;
	@ApiModelProperty(value="SOLUTION")
	private String solution;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	
	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;	
	
}

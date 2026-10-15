package org.yht.exps.express.model.commonMng.stock;


import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(value = "VoidStock", description = "전자상거래 Stock")
@Alias("voidStock")
public class VoidStock {
	
	
	@ApiModelProperty(value="배송사")
	private String trCode;	
	
	@ApiModelProperty(value="바운드")
	private String bound;		

	@ApiModelProperty(value="바운드타입")
	private String boundType;	
	
	@ApiModelProperty(value="대리점")
	private String agentCode;
	
	@ApiModelProperty(value="SEQ_NO")
	private int seqNo;		
	
	@ApiModelProperty(value="")
	private String voidStockNo;	
	
	@ApiModelProperty(value="")
	private String voidType;		
	
	@ApiModelProperty(value="등록자")
	private String registerUserId;		
	
	@ApiModelProperty(value="등록일자")
	private String registerDateTime;	
	
	@ApiModelProperty(value="수정자")
	private String updateUserID;	

	@ApiModelProperty(value="수정일자")
	private String updateDateTime;		
	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;		

}




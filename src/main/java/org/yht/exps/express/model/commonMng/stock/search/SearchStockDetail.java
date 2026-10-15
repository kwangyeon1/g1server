package org.yht.exps.express.model.commonMng.stock.search;


import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(value = "SearchStockDetail", description = "전자상거래 Stock")
@Alias("searchStockDetail")
public class SearchStockDetail {
	
	@ApiModelProperty(value="도입사코드", example = "YHTXX")
	private String entCode;	
	@ApiModelProperty(value="배송사", example = "KOREX OR EMS")
	private String srchTrCode;	
	@ApiModelProperty(value="바운드", example = "AIR OR SEA")
	private String srchBound;		
	@ApiModelProperty(value="바운드타입", example = "EXP OR IMP")
	private String srchBoundType;	
	@ApiModelProperty(value="대리점", example = "AAAA")
	private String srchAgentCode;
	@ApiModelProperty(value="스탁 시작 번호", example = "346681511686")
	private long srchStartNo;		
	@ApiModelProperty(value="스탁 끝 번호", example = "346681512725")
	private long srchEndNo;		
	@ApiModelProperty(value="SEQ_NO", example = "1")
	private int srchSeqNo;		
	
}




package org.yht.exps.express.model.commonMng.stock.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchVoidStockList", description = "전자상거래 Void Stock 리스트 검색 조건")
@Alias("searchVoidStockList")
public class SearchVoidStockList extends SearchList {
	@ApiModelProperty(value="바운드", example="AIR OR SEA")
	private String srchBound;	
	@ApiModelProperty(value="바운드타입", example="EXP OR IMP")
	private String srchBoundType;	
	@ApiModelProperty(value="배송사 코드", example="우체국, 대한통운")
	private String srchTrCode;	
	@ApiModelProperty(value="대리점 코드", example="aaaa")
	private String srchAgentCode;		
	@ApiModelProperty(value="대리점 이름", example="유한")
	private String srchAgentName;	
	@ApiModelProperty(value="진행구분", example="")
	private String srchStatus;		
	@ApiModelProperty(value="재고구분", example="")
	private String srchStockBalanceYn;			
	@ApiModelProperty(value="Stock번호", example="")
	private String srchStockNo;	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;		
	@ApiModelProperty(value="스탁시작번호", example="6323456789101")
	private String srchStartNo;	
	@ApiModelProperty(value="스탁끝번호", example="6323456790101")
	private String srchEndNo;		
}

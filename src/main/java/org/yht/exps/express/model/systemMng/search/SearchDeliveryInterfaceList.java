package org.yht.exps.express.model.systemMng.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchDeliveryInterfaceList", description = "배송사 Interface 조회")
@Alias("searchDeliveryInterfaceList")
public class SearchDeliveryInterfaceList extends SearchList {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="사용여부", example="Y or N")
	private String srchUseYn;
	@ApiModelProperty(value="거래처")
	private String srchCustomerCode;
	@ApiModelProperty(value="공통, 항공, 해상 , 반송", example="C")
	private String srchBoundType;	
	@ApiModelProperty(value="우체국, 우체국API, 대한통운, 로젠택배, 코파스, 스마트로", example="EMS, EPOST_API, KOREX, LOGEN, COPAS, SMART")
	private String srchCompany;		
	
	
}
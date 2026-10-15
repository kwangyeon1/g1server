package org.yht.exps.express.model.systemMng.search;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchDeliveryInterface", description = "DeliveryInterface 조회")
@Alias("searchDeliveryInterface")
public class SearchDeliveryInterface{
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="바운드", example="AI, AO, OI, OO")
	private String srchBound;	
	@ApiModelProperty(value="거래처")
	private String srchCustomerCode;	
	@ApiModelProperty(value="집하점")
	private String srchLogisticsCode;
	@ApiModelProperty(value="배송사코드")
	private String srchDeliveryCode;
	@ApiModelProperty(value="profile명")
	private String profileName;
	
	@ApiModelProperty(value="리스트")
	private List<Map<String, Object>> dataList;	
}

package org.yht.exps.express.model.eCommerce.order;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "orderDashboard", description = "전자상거래 Order 대시보드")
@Alias("orderDashboard")
@EqualsAndHashCode(callSuper=false)
public class OrderDashboard {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="대시보드 타입")
	private String dashboardType;
	@ApiModelProperty(value="대시보드용 코드")
	private String code;
	@ApiModelProperty(value="대시보드용 코드명")
	private String name;
	@ApiModelProperty(value="대시보드 건수")
	private Double count;
	@ApiModelProperty(value="대시보드 건수2")
	private long count2;

	@ApiModelProperty(value="대시보드 결과 리스트")
	List<OrderDashboard> dashboardList;
	
	@ApiModelProperty(value="환율결과")
	List<Map<String, Object>> rateList;
	
	@ApiModelProperty(value="C/S결과")
	List<Map<String, Object>> csQanda;
	
	@ApiModelProperty(value="반품(미처리)결과")
	Map<String, Object> resendCount; 
}

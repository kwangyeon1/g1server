package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "OrderPod", description = "전자상거래 Order POD정보")
@Alias("orderPod")
public class OrderPod {
	@ApiModelProperty(value="POD발생일시")
	private String podDateTime;
	@ApiModelProperty(value="진행구분코드명")
	private String podStatusName;
	@ApiModelProperty(value="진행구분코드")
	private String podStatusCode;
	@ApiModelProperty(value="담당자")
	private String courierName;
	
	//진행상태 팝업 상단부에 추가
	@ApiModelProperty(value="hbl 번호")
	private String hblNo;
	@ApiModelProperty(value="mbl 번호")
	private String mblNo;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="실수량")
	private int piecesReal;
	@ApiModelProperty(value="중량")
	private double gwt;
	@ApiModelProperty(value="수하인명")
	private String consigneeNameLoc;
	@ApiModelProperty(value="품명")
	private String commodityItem;
	
	@ApiModelProperty(value="담당자명")
	private String registerUserName;
	@ApiModelProperty(value="담당자id")
	private String registerUserId;
	@ApiModelProperty(value="로그인id")
	private String userLoginId;
	
	//2024-07-11 추가
	@ApiModelProperty(value="화물관리번호")
	private String shipmentNo;
	@ApiModelProperty(value="bound")
	private String bound;
	@ApiModelProperty(value="bound type")
	private String boundType;
	@ApiModelProperty(value="비고")
	private String remarks;
}

package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "OrderPacking", description = "전자상거래 Order Packing 목록")
@Alias("orderPacking")
public class OrderPacking {
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="일련번호")
	private String seqNoId;
	@ApiModelProperty(value="배송번호")
	private String deliveryNo;
	@ApiModelProperty(value="packingNo")
	private String packingNo;
	@ApiModelProperty(value="화물규격_가로")
	private double dimWidth;
	@ApiModelProperty(value="화물규격_길이")
	private double dimLength;
	@ApiModelProperty(value="화물규격_높이")
	private double dimHeight;
	@ApiModelProperty(value="화물규격_볼륨")
	private double vwt;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="NET(K)")
	private double nwt;
	@ApiModelProperty(value="CBM")
	private double cbm;
	
	
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력자명")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
}

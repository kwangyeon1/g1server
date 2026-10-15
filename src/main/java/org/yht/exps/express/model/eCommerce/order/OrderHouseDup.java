package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "OrderHouseDup", description = "전자상거래 OrderHouseDup")
@Alias("orderHouseDup")
@EqualsAndHashCode(callSuper=false)
public class OrderHouseDup extends ResultList {

	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="신규여부")
	private String newYn;
	@ApiModelProperty(value="출항일자")
	private String arDate;
	@ApiModelProperty(value="MAWB No")
	private String mblNo;
	@ApiModelProperty(value="HAWB No")
	private String hblNo;
	@ApiModelProperty(value="HSN")
	private String hsn;
	@ApiModelProperty(value="pieces")
	private int pieces;
	@ApiModelProperty(value="gwt")
	private double gwt;
	@ApiModelProperty(value="수하인명")
	private String consigneeName;
	@ApiModelProperty(value="등록일자")
	private String registerDateTime;
	
}

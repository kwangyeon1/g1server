package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "HblNo", description = "전자상거래 H.B/L No 리스트")
@Alias("hblNo")
@EqualsAndHashCode(callSuper=false)
public class HblNo extends ResultList{
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="HSN")
	private String hsn;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="수하인_명칭")
	private String consigneeName;
	@ApiModelProperty(value="통관타입")
	private String cdType;
	@ApiModelProperty(value="통관_용도구분")
	private String cdUseType;
	@ApiModelProperty(value="검사_여부")
	private String inspectionYn;
	@ApiModelProperty(value="선별_여부")
	private String seInspectionYn;
}

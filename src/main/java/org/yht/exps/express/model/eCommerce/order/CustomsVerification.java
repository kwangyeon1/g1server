package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "CustomsVerification", description = "개인통관부호 검증")
@Alias("customsVerification")
public class CustomsVerification {
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;
	@ApiModelProperty(value="사용자ID")
	private String userId;
	@ApiModelProperty(value="사용자명")
	private String userName;
	@ApiModelProperty(value="Order Job No")
	private String odJobNo;
	@ApiModelProperty(value="Master Order Job No")
	private String masterOdJobNo;
	@ApiModelProperty(value="MBL No")
	private String mblNo;
	@ApiModelProperty(value="HBL No")
	private String hblNo;
	@ApiModelProperty(value="검증이름구분")
	private String nameType;
}

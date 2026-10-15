package org.yht.exps.express.model.eCommerce.bondedTrans;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "BondedTransDelete", description = "고객서비스 보세운송 삭제")
@Alias("bondedTransDelete")
public class BondedTransDelete {
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="H.B/L No")
	private String hblNo;
}

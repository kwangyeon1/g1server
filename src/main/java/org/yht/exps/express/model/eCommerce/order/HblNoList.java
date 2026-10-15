package org.yht.exps.express.model.eCommerce.order;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "HblNoList", description = "전자상거래 H.B/L No 리스트")
@Alias("hblNoList")
public class HblNoList {
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="리스트")
	private List<HblNo> hblNoList;
}

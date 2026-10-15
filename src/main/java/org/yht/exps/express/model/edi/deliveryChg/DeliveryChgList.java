package org.yht.exps.express.model.edi.deliveryChg;

import java.util.List;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "DeliveryChgList", description = "전자상거래  배송지변경신고 리스트")
@Alias("deliveryChgList")
public class DeliveryChgList {
	
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="제출번호 그룹 리스트")
	private List<DeliveryChg> deliveryChgDocList;
	@ApiModelProperty(value="배송지변경신고 리스트")
	private List<DeliveryChg> deliveryChgList;
	@ApiModelProperty(value="배송지변경신고 결과조회 리스트")
	private List<DeliveryChgResult> deliveryChgResultList;
	
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
}

package org.yht.exps.express.model.bms.bmsOutNotice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.bms.disposal.InventoryIn;
import org.yht.util.CustomErrorType;

import java.util.List;

@Data
@ApiModel(value = "BmsOutNoticeDetailList", description = "반출통고신고 - ENTRY 상세")
@EqualsAndHashCode(callSuper=false)
@Alias("bmsOutNoticeDetailList")
public class BmsOutNoticeDetailList {

	@ApiModelProperty(value="반출통고 리스트 개수")
	private long total;
	@ApiModelProperty(value="반출통고 리스트")
	private List<BmsOutNotice> bmsOutNoticeList;

    @ApiModelProperty(value="미승인재고 리스트 개수")
    private long totalIn;
    @ApiModelProperty(value="미승인재고 리스트")
    private List<InventoryIn> inventoryInList;

    @ApiModelProperty(value="누락통보 리스트 개수")
    private long totalNoticePlan;
    @ApiModelProperty(value="누락통보 리스트")
    private List<BmsOutNoticePlan> bmsOutNoticePlanList;

    @ApiModelProperty(value="에러 타입")
    private CustomErrorType errorType;
    @ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
    private String scrollId;

}

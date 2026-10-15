package org.yht.exps.express.model.bms.bmsOutNotice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.bms.disposal.InventoryIn;

@Data
@Builder
@ApiModel(value = "BmsOutNoticeDetail", description = "반출통고신고 - ENTRY 상세")
@Alias("bmsOutNoticeDetail")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class BmsOutNoticeDetail {

    @ApiModelProperty(value="조회번호")
    private String srchNo;
    @ApiModelProperty(value="조회번호 구분")
    private String srchNoType;
	@ApiModelProperty(value="조회 구분") /** STOCK(미승인재고), NOTICE(반출통고), OMITTED(누락통보) */
	private String srchGbn;
	@ApiModelProperty(value="화물관리번호(ENTRY 상세 조회조건)")
	private String srchCargoReferenceNo;

    @ApiModelProperty(value="미승인재고 상세")
    InventoryIn inventoryIn;
    @ApiModelProperty(value="반출통고 상세")
    BmsOutNotice bmsOutNotice;
    @ApiModelProperty(value="누락통보 상세")
    BmsOutNoticePlan bmsOutNoticePlan;

}

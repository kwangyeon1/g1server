package org.yht.exps.express.model.bms.disposal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import java.util.List;

/** 폐기신청 재고 리스트*/

@Data
@ApiModel(value = "InventoryInList", description = "재고 리스트")
@EqualsAndHashCode(callSuper=false)
@Alias("inventoryInList")
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class InventoryInList {
    @ApiModelProperty(value="리스트 개수")
    private long total;
    @ApiModelProperty(value="리스트")
    private List<InventoryIn> inventoryInList;
    @ApiModelProperty(value="에러 타입")
    private CustomErrorType errorType;
    @ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
    private String scrollId;
}

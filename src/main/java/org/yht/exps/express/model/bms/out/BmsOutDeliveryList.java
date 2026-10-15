package org.yht.exps.express.model.bms.out;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "BmsOutDeliveryList", description = "택배사연계 리스트")
@EqualsAndHashCode(callSuper=false)
@Alias("BmsOutDeliveryList")
public class BmsOutDeliveryList {
    @ApiModelProperty(value="리스트 개수")
    private long total;
    @ApiModelProperty(value="리스트")
    private List<BmsOutDelivery> bmsOutDeliveryList;
    @ApiModelProperty(value="에러 타입")
    private CustomErrorType errorType;
    @ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
    private String scrollId;
}

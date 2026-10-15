package org.yht.exps.express.model.bms.charge;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@ApiModel(value = "BmsEstimateGroupingList", description = "반출 리스트")
@EqualsAndHashCode(callSuper=false)
@Alias("bmsEstimateGroupingList")
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsEstimateGroupingList {
	
    @ApiModelProperty(value="리스트")
    private Map<String, List<BmsEstimate>> groupedByApplyClass;
    @ApiModelProperty(value="에러 타입")
    private CustomErrorType errorType;
    
}

package org.yht.exps.express.model.bms.charge;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import java.util.List;

/** 작업료 폐기 그리드 리스트*/

@Data
@ApiModel(value = "BmsWorkRateList", description = "작업료 리스트")
@EqualsAndHashCode(callSuper=false)
@Alias("bmsWorkRateList")
public class BmsWorkRateList {
    @ApiModelProperty(value="리스트 개수")
    private long total;
    @ApiModelProperty(value="리스트")
    private List<BmsWorkRate> bmsWorkRateList;
    @ApiModelProperty(value="에러 타입")
    private CustomErrorType errorType;
    @ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
    private String scrollId;


    @ApiModelProperty(value="공휴일 리스트")
    private List<BmsHoliday> holidayList;
}

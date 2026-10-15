package org.yht.exps.express.model.bms.bmsAbnormalCorrection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.bms.bmsAbnormal.CarryInAbnormal;
import org.yht.util.CustomErrorType;

import java.util.List;

@Data
@ApiModel(value = "CarryInAbnormalCorrDetailList", description = "반입이상물품신고정정 리스트")
@EqualsAndHashCode(callSuper=false)
@Alias("carryInAbnormalCorrDetailList")
public class CarryInAbnormalCorrDetailList {
    @ApiModelProperty(value="반입이상 리스트 개수")
    private long totalAbnormal;
    @ApiModelProperty(value="반입이상 리스트")
    private List<CarryInAbnormal> carryInAbnormalList;

    @ApiModelProperty(value="반입이상정정(Master 입항정보) 리스트 개수")
    private long totalAbnormalCorr;
    @ApiModelProperty(value="반입이상정정(Master 입항정보) 리스트")
    private List<CarryInAbnormalCorr> carryInAbnormalCorrList;

    @ApiModelProperty(value="반입이상정정 House/Master Single 리스트")
    private List<CarryInAbnormalHblCorr> carryInAbnormalHblCorrList;

    @ApiModelProperty(value = "정정된내역 존재여부(0: 정정내역 없음 / 1:정정내역 존재)")
    private int isCorrected = 0;

    @ApiModelProperty(value="에러 타입")
    private CustomErrorType errorType;
    @ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
    private String scrollId;
}

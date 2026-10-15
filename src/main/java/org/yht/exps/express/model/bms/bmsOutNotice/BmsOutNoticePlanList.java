package org.yht.exps.express.model.bms.bmsOutNotice;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import java.util.List;

@Data
@ApiModel(value = "BmsOutNoticeList", description = "반출통고신고 - 누락통보 리스트")
@EqualsAndHashCode(callSuper=false)
@Alias("bmsOutNoticePlanList")
public class BmsOutNoticePlanList {

    @ApiModelProperty(value="리스트 개수")
    private long total;
    @ApiModelProperty(value="리스트")
    private List<BmsOutNoticePlan> bmsOutNoticePlanList;
    @ApiModelProperty(value="에러 타입")
    private CustomErrorType errorType;
    @ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
    private String scrollId;

}

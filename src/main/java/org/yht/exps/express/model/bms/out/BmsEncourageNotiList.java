package org.yht.exps.express.model.bms.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import java.util.List;

/** 폐기신청 폐기 그리드 리스트*/

@Data
@ApiModel(value = "BmsEncourageNotiList", description = "선반입반출신고독려통보 리스트")
@EqualsAndHashCode(callSuper=false)
@Alias("bmsEncourageNotiList")
public class BmsEncourageNotiList {
    @ApiModelProperty(value="리스트 개수")
    private long total;
    @ApiModelProperty(value="리스트")
    private List<BmsEncourageNoti> bmsEncourageNotiList;
    @ApiModelProperty(value="에러 타입")
    private CustomErrorType errorType;
    @ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
    private String scrollId;
}

package org.yht.exps.express.model.bms.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import java.util.List;

/** 폐기신청 폐기 그리드 리스트*/

@Data
@ApiModel(value = "BmsOutSampleList", description = "견품반출 리스트")
@EqualsAndHashCode(callSuper=false)
@Alias("bmsOutSampleList")
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsOutSampleList {
    @ApiModelProperty(value="리스트 개수")
    private long total;
    @ApiModelProperty(value="리스트")
    private List<BmsOutSample> bmsOutSampleList;
    @ApiModelProperty(value="에러 타입")
    private CustomErrorType errorType;
    @ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
    private String scrollId;
}

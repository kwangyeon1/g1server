package org.yht.exps.express.model.bms.bmsEc;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.util.List;
import java.util.Map;

@Data
@ApiModel(value = "BmsEcSendResult", description = "EC 재송신 결과")
@EqualsAndHashCode(callSuper=false)
@Alias("bmsEcSendResult")
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsEcSendResult {
    @ApiModelProperty(value="결과코드")
    private String resultCode;
    @ApiModelProperty(value="결과내역")
    private Map<String, Object> resultData;
    @ApiModelProperty(value="결과출력")
    private Map<String, Object> returnMap;
    @ApiModelProperty(value="결과리스트")
    private List<String> returnList;
    @ApiModelProperty(value="결과에러리스트")
    private List<String> errList;

    @ApiModelProperty(value="처리 건수(성공)")
    private Integer resultCnt;
    @ApiModelProperty(value="실패 건수")
    private Integer failCnt;

}

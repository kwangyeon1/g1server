package org.yht.exps.express.model.bms.disposal;

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
@ApiModel(value = "DisposalSaveResult", description = "폐기정보 Disposal Save 결과")
@EqualsAndHashCode(callSuper=false)
@Alias("disposalSaveResult")
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class DisposalSaveResult {
    @ApiModelProperty(value="관리번호")
    private String odJobNo;
    @ApiModelProperty(value="결과코드")
    private String resultCode;
    @ApiModelProperty(value="결과내역")
    private Map<String, Object> resultData;
    @ApiModelProperty(value="결과출력")
    private Map<String, Object> returnMap;

    @ApiModelProperty(value="결과리스트")
    private List<String> returnList;
    @ApiModelProperty(value="처리 건수")
    private Integer resultCnt;
    @ApiModelProperty(value="조회 건수")
    private Integer selectCnt;
    
    // 반/출입처리가 거절된 홀드사유
    @ApiModelProperty(value="홀드사유")
	private String holdReason;

}

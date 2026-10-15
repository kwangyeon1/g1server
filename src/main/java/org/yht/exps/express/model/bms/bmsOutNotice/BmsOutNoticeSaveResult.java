package org.yht.exps.express.model.bms.bmsOutNotice;

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
@ApiModel(value = "BmsOutNoticeSaveResult", description = "반출통고 Save 결과")
@EqualsAndHashCode(callSuper=false)
@Alias("BmsOutNoticeSaveResult")
@NoArgsConstructor
@AllArgsConstructor
public class BmsOutNoticeSaveResult {

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
    @ApiModelProperty(value="결과에러리스트")
    private List<String> errList;
    @ApiModelProperty(value="처리 건수(성공)")
    private Integer succCnt;
    @ApiModelProperty(value="실패 건수")
    private Integer failCnt;
    @ApiModelProperty(value="예외메일전송 또는 EDI포털접속오류용 에러메시지")
    private String errorMsg;
    @ApiModelProperty(value="MRS 전송실패 에러코드 및 내용")
    private String mrsSendErrorMsg;

}

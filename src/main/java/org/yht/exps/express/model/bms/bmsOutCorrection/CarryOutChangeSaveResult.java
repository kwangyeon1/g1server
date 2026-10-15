package org.yht.exps.express.model.bms.bmsOutCorrection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.util.Map;

@Data
@ApiModel(value = "CarryOutChangeSaveResult", description = "반출정정신청 Save 결과")
@EqualsAndHashCode(callSuper = false)
@Alias("carryOutChangeSaveResult")
@AllArgsConstructor
@NoArgsConstructor
public class CarryOutChangeSaveResult {
    @ApiModelProperty(value="반출입정정번호")
    private Integer changeNo;
    @ApiModelProperty(value="반출번호")
    private Integer outNo;
    @ApiModelProperty(value="결과코드")
    private String resultCode;
    @ApiModelProperty(value="결과내역")
    private Map<String, Object> resultData;
    @ApiModelProperty(value="결과출력")
    private Map<String, Object> returnMap;

    //삭제시 처리선수(변수삭제한건수, 변수삭제하지못한건수)
    @ApiModelProperty(value="변수삭제못함 처리건수")
    private Integer notDeletedCnt;
    @ApiModelProperty(value="변수삭제함 처리건수")
    private Integer deletedCnt;

	/** Web XML Send */
    @ApiModelProperty(value="성공 건수")
    private Integer succCnt;
    @ApiModelProperty(value="실패 건수")
    private Integer failCnt;
    @ApiModelProperty(value="예외메일전송 또는 EDI포털접속오류용 에러메시지")
    private String errorMsg;
    @ApiModelProperty(value="MRS 전송실패 에러코드 및 내용")
    private String mrsSendErrorMsg;

}

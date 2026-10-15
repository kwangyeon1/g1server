package org.yht.exps.express.model.bms.out;

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
@ApiModel(value = "BmsOutSaveResult", description = "반출 Save 결과")
@EqualsAndHashCode(callSuper=false)
@Alias("bmsOutSaveResult")
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsOutSaveResult {
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
    private Integer resultCnt;

    @ApiModelProperty(value="실패 건수")
    private Integer failCnt;
    
    @ApiModelProperty(value="재입고 건수")
    private Integer returnCnt;

    @ApiModelProperty(value="작업료 번호")
    private Integer workRateNo;

    @ApiModelProperty(value="휴일일자")
    private String holidayDate;


    @ApiModelProperty(value="입력 건수")
    private Integer InCnt;

    @ApiModelProperty(value="수정 건수")
    private Integer upCnt;
    
    // 반/출입처리가 거절된 홀드사유
    @ApiModelProperty(value="홀드사유")
	private String holdReason;

	/** Web XML Send */
    @ApiModelProperty(value="예외메일전송 또는 EDI포털접속오류용 에러메시지")
    private String errorMsg;
    @ApiModelProperty(value="MRS 전송실패 에러코드 및 내용")
    private String mrsSendErrorMsg;

    @ApiModelProperty(value="이고반출 SCAN 에러 실패 건수")
    private Integer moveFailCnt;
    @ApiModelProperty(value="이고반출 SCAN 에러 플래그")
    private String moveOutFailYn;

}

package org.yht.exps.express.model.bms.carryin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import java.util.List;
import java.util.Map;

@Data
@ApiModel(value = "CarryInSaveResult", description = "반입정보 Save 결과")
@EqualsAndHashCode(callSuper=false)
@Alias("carryInSaveResult")
public class CarryInSaveResult {
    @ApiModelProperty(value="반입번호")
    private String inNo;
    @ApiModelProperty(value="결과코드")
    private String resultCode;
    @ApiModelProperty(value="결과내역")
    private Map<String, Object> resultData;
    @ApiModelProperty(value="결과출력")
    private Map<String, Object> returnMap;

    //삭제시 처리선수(변수삭제한건수, 변수삭제하지못한건수)
    @ApiModelProperty(value="변수삭제못함 처리건수")
    private int notDeletedCnt;
    @ApiModelProperty(value="변수삭제함 처리건수")
    private int deletedCnt;
    @ApiModelProperty(value="삭제불가 사유 리스트(박스삭제: 사유별 박스번호)")
    private List<String> notDeletedReasonList;
    
    //반입신고 반입생성시
    @ApiModelProperty(value="참조할 반입예정번호")
    private Integer referenceInPlanNo;

    // 배치 처리를 위한 변수 추가
    @ApiModelProperty(value="결과리스트_접수 아닌 항목")
    private List<String> returnList;
    @ApiModelProperty(value="결과리스트_이고반출자료 아닌 항목")
    private List<String> returnOutList;
    @ApiModelProperty(value="결과리스트_재고 아닌 항목")
    private List<String> returnStockList;
    @ApiModelProperty(value="처리 건수")
    private Integer resultCnt;
    @ApiModelProperty(value="사업장 보세 구역번호 8자리")
    private String bmsWhCode;
    @ApiModelProperty(value="다음 이고차수")
    private Integer nextTicOutSeq;
    
    // 반/출입처리가 거절된 홀드사유
    @ApiModelProperty(value="홀드사유")
	private String holdReason;
    
	/** Web XML Send */
    @ApiModelProperty(value="성공 건수")
    private Integer succCnt;
    @ApiModelProperty(value="실패 건수")
    private Integer failCnt;
    @ApiModelProperty(value="예외메일전송 또는 EDI포털접속오류용 에러메시지")
    private String errorMsg;
    @ApiModelProperty(value="MRS 전송실패 에러코드 및 내용")
    private String mrsSendErrorMsg;

    /** MFN, MGN 자료생성 */
    @ApiModelProperty(value="MFN, MGN 자료생성 성공후 리턴 메시지ID")
    private String retSuccMsgId;
    @ApiModelProperty(value="MFN, MGN 자료생성 실패후 리턴 메시지ID")
    private String retFailMsgId;
    @ApiModelProperty(value="MFN, MGN 자료생성 후 생성실패 화물관리번호 리스트")
    private List<String> existCargoNoList;
    @ApiModelProperty(value="성공 건수")
    private Integer insertSuccCnt;
    @ApiModelProperty(value="실패 건수")
    private Integer insertFailCnt;
}

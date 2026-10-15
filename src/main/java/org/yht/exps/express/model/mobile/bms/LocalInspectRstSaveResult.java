package org.yht.exps.express.model.mobile.bms;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import java.util.Map;

@Data
@ApiModel(value = "LocalInspectRstSaveResult", description = "특송창고 검사결과 Save 결과")
@EqualsAndHashCode(callSuper=false)
@Alias("localInspectRstSaveResult")
public class LocalInspectRstSaveResult {
    @ApiModelProperty(value="반입번호")
    private String inNo;
    @ApiModelProperty(value="결과코드")
    private String carryinDate;
    @ApiModelProperty(value="BL/NO(하우스번호)")
	private String blNo;
    @ApiModelProperty(value="반출일자")
    private String carryoutDate;
    @ApiModelProperty(value="반출수량")
    private String outCt;
    @ApiModelProperty(value="이고반출체크일시")
    private String TicOutDate;
    /* 반입TB_BMS_IN 항목 */	
	@ApiModelProperty(value = "반입 수량")
    private Integer ct;
	@ApiModelProperty(value = "배송사명")
	private String deliveryName;
	/* --------------- */
	
	/* 반출승인TB_BMS_OUT_PLAN 항목 */	
	@ApiModelProperty(value = "반출승인수량")
    private Integer approvalCt;
	/* --------------- */
    
    @ApiModelProperty(value="이고반출확인후, 업데이트가 안된 이유")
    private String reason;
    @ApiModelProperty(value="업데이트 성공여부")
    private String updateYn;
    
    @ApiModelProperty(value="결과코드")
    private String resultCode;
    @ApiModelProperty(value="결과내역")
    private Map<String, Object> resultData;
    @ApiModelProperty(value="결과출력")
    private Map<String, Object> returnMap;
    @ApiModelProperty(value="결과메세지")
    private String resultMsg;
    
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

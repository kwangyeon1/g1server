package org.yht.exps.express.model.bms.bmsAllocateCar;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.util.Map;

@Data
@ApiModel(value = "BmsAllocateCarSaveResult", description = "일괄배차등록 팝업 Save 결과")
@EqualsAndHashCode(callSuper = false)
@Alias("BmsAllocateCarSaveResult")
@AllArgsConstructor
@NoArgsConstructor
public class BmsAllocateCarSaveResult {

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
    @ApiModelProperty(value="처리 건수(성공)")
    private Integer succCnt;
    @ApiModelProperty(value="실패 건수")
    private Integer failCnt;
    @ApiModelProperty(value="예외메일전송 또는 EDI포털접속오류용 에러메시지")
    private String errorMsg;
    @ApiModelProperty(value="MRS 전송실패 에러코드 및 내용")
    private String mrsSendErrorMsg;

    @ApiModelProperty(value="배차예정번호")
	private int allocateCarNotiNo;
}

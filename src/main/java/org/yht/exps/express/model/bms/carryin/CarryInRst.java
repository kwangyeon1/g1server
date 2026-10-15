package org.yht.exps.express.model.bms.carryin;

import org.yht.exps.express.model.mobile.bms.BmsOutReleaseResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;

import java.util.List;
import java.util.Map;

@Data
@Builder
@ApiModel(value = "CarryInRst", description = "반입관련 응답결과")
@Alias("carryInRst")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class CarryInRst{

	/* --------------- */
	@ApiModelProperty(value="박스")
	private InPlanPkg inPlanPkg;
	/* --------------- */
	
	/* --------------- */
	@ApiModelProperty(value="박스리스트")
	private List<InPlanPkg> inPlanPkgList;

	@ApiModelProperty(value="이고대상리스트")
	private List<Map<String, Object>> inspectResultList;
	
	@ApiModelProperty(value="반출출고결과리스트")
	private List<BmsOutReleaseResult> outReleaseResultList;
	/* --------------- */
	
	/* --------------- */
	/* --------------- */
    
    @ApiModelProperty(value="결과이유")
    private String reason;
    
    @ApiModelProperty(value="결과코드")
    private String resultCode;

    @ApiModelProperty(value="홀드사유")
    private String holdReason;

    @ApiModelProperty(value="성공 건수")
    private Integer succCnt;
    
    @ApiModelProperty(value="실패 건수")
    private Integer failCnt;
    
    /** 이고반출 FTP 전송 */
    @ApiModelProperty(value="결과에러리스트")
    private List<String> errList;

    @ApiModelProperty(value="처리 건수(성공)")
    private Integer resultCnt;

    @ApiModelProperty(value="행 삭제 여부")
    private String deleteRowFlag;
}

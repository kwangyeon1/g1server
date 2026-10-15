package org.yht.exps.express.model.bms.carryin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;

@Data
@Builder
@ApiModel(value = "BmsInspectResultRst", description = "검사결과관련 result")
@Alias("bmsInspectResultRst")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsInspectResultRst {

	/* --------------- */
	@ApiModelProperty(value="검사")
	private BmsInspectResult bmsInspectResult;
	/* --------------- */
	
	/* --------------- */
	@ApiModelProperty(value="검사리스트")
	private List<BmsInspectResult> bmsInspectResultList;
	@ApiModelProperty(value="박스리스트")
	private List<InPlanPkg> inPlanPkgList;
	/* --------------- */
	
	/* --------------- */
	@ApiModelProperty(value="반입 HashMap데이터")
	private Map<String, Object> bmsInMap;
	/* --------------- */
    
    @ApiModelProperty(value="이고반출확인후, 업데이트가 안된 이유")
    private String reason;
    @ApiModelProperty(value="업데이트 성공여부")
    private String updateYn;
    
    @ApiModelProperty(value="결과코드")
    private String resultCode;
    
    @ApiModelProperty(value="결과 action")
    private String resultAction;
    
	/** Web XML Send */
    @ApiModelProperty(value="성공 건수")
    private Integer succCnt;
    @ApiModelProperty(value="실패 건수")
    private Integer failCnt;
    
   
}

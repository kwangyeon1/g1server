package org.yht.exps.express.model.bms.carryin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import java.util.List;
import java.util.Map;

@Data
@ApiModel(value = "BmsInPlanPkgResult", description = "박스조회결과")
@EqualsAndHashCode(callSuper=false)
@Alias("bmsInPlanPkgResult")
public class BmsInPlanPkgResult {

	/* --------------- */
	@ApiModelProperty(value="박스")
	private InPlanPkg inPlanPkg;
	/* --------------- */
	
	/* --------------- */
	@ApiModelProperty(value="박스리스트")
	private List<InPlanPkg> inPlanPkgList;
	/* --------------- */
	
	/* --------------- */
	@ApiModelProperty(value="로케이션 정보")
	private Map<String, Object> locationInfo;
	/* --------------- */
    
    @ApiModelProperty(value="이고반출확인후, 업데이트가 안된 이유")
    private String reason;
    
    @ApiModelProperty(value="결과코드")
    private String resultCode;


    @ApiModelProperty(value="성공 건수")
    private Integer succCnt;
    
}

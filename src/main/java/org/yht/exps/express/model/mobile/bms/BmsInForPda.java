package org.yht.exps.express.model.mobile.bms;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import org.yht.exps.express.model.bms.carryin.CarryIn;

@Data
@ApiModel(value = "bmsInForPda", description = "반입 상세 (반출,홀드 포함)")
@Alias("bmsInForPda")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsInForPda extends CarryIn{

	@ApiModelProperty(value = "반입금지")
    private String carryinStopYn;
	
	@ApiModelProperty(value = "반출금지")
    private String carryoutStopYn;
	
    @ApiModelProperty(value="홀드처리일자")
    private String holdDate;
    
}

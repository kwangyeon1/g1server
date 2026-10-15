package org.yht.exps.express.model.bms.charge;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@ApiModel(value = "TcrNoSettingResult", description = "tcr 번호 정보")
@Alias("tcrNoSettingResult")
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class TcrNoSettingResult {
	
    @ApiModelProperty(value="리스트")
    private String tcrNo;
    
    @ApiModelProperty(value="결과코드")
    private String resultCode;
    
}

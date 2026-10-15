package org.yht.exps.express.model.mobile.bms;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@ApiModel(value = "bmsBigSizeScan", description = "거대화물스캔로그")
@Alias("bmsBigSizeScan")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsBigSizeScan extends ResultList{

	@ApiModelProperty(value = "박스번호")
    private String packingNo;
	
	@ApiModelProperty(value = "BL번호")
    private String blNo;
	
	@ApiModelProperty(value="스캔번호")
    private String scanNo;
	
	@ApiModelProperty(value="스캔날짜")
    private String scanDate;
	
    @ApiModelProperty(value="스캔결과")
    private String scanResult;

    @ApiModelProperty(value="거대여부")
    private String bigSizeYnInput;
    
    @ApiModelProperty(value="입력자코드")
    private String registerUserId;
    
    @ApiModelProperty(value="입력자이름")
    private String registerUserName;
    
    @ApiModelProperty(value="입력일시")
    private String registerDateTime;
    
}

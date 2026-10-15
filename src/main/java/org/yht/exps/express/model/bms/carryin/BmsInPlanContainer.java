package org.yht.exps.express.model.bms.carryin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.apache.ibatis.type.Alias;

/**
 * 반출컨테이너(BmsInContainer)Entity
 */
@Data
@Builder
@ApiModel(value = "bmsInPlanContainer", description = "반입예정 컨테이너 VO 객체")
@Alias("bmsInPlanContainer")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class BmsInPlanContainer   {

    @ApiModelProperty(value="반입예정번호")
    private  Integer  inPlanNo ;
    
    @ApiModelProperty(value="컨테이너번호")
    @Builder.Default  private  String   cntrNo    =  ""      ;

    @ApiModelProperty(value="컨테이너SIZE")
    @Builder.Default  private  String   cntrEdiCode    =  ""      ;

    @ApiModelProperty(value="봉인번호1")
    @Builder.Default  private  String   sealNo1    =  ""      ;

    @ApiModelProperty(value="봉인번호2")
    @Builder.Default  private  String   sealNo2    =  ""      ;

    @ApiModelProperty(value="봉인번호3")
    @Builder.Default  private  String   sealNo3    =  ""      ;


}


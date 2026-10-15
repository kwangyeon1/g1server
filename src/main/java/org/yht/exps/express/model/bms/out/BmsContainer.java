package org.yht.exps.express.model.bms.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;

/**
 * 반출컨테이너(BmsInContainer)Entity
 *
 * @author Woong Jang
 * @since 2024-03-24 22:01:15
 */
@Data
@Builder
@ApiModel(value = "BmsContainer", description = "반입/반출컨테이너 VO 객체")
@Alias("bmsContainer")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class BmsContainer   {
    private static final long serialVersionUID = 377689775501330700L;

    @ApiModelProperty(value="반입번호")
    private  Integer   inNo ;
    @ApiModelProperty(value="반출번호")
    private  Integer   outNo ;
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


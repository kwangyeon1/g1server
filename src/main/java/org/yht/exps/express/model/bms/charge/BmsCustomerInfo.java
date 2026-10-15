package org.yht.exps.express.model.bms.charge;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;

/**
 *            CUSTOMER_CODE
 * 			 , CUSTOMER_NAME
 * 			 , ATTN
 * 			 , ATTN_HP
 * 			 , TEL
 * 			 , EMAIL
 * 			 , USA_BUSINESS_NO AS  BUSINESS_NO
 */
@Data
@Builder
@ApiModel(value = "BmsCustomerInfo", description = "계산서 정보 표시용 VO 객체")
@Alias("bmsCustomerInfo")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsCustomerInfo {

    @ApiModelProperty(value="납세의무자사업자번호")
    @Builder.Default private String companyBusinessNo=  ""   ;

    @ApiModelProperty(value="화주코드")
    @Builder.Default  private  String   customerCode    =  ""      ;

    @ApiModelProperty(value="화주명")
    @Builder.Default  private  String   customerName    =  ""      ;

    @ApiModelProperty(value="담당자")
    @Builder.Default  private  String   attn    =  ""      ;

    @ApiModelProperty(value="담당자 HP")
    @Builder.Default  private  String   attnHp    =  ""      ;
    @ApiModelProperty(value="전화번호")
    @Builder.Default  private  String   tel    =  ""      ;

    @ApiModelProperty(value="이메일")
    @Builder.Default  private  String   email    =  ""      ;

    @ApiModelProperty(value="사업자번호")
    @Builder.Default  private  String   businessNo    =  ""      ;

    @ApiModelProperty(value="정산번호( 계산서 발행후)")
    @Builder.Default  private  String   invoiceNo    =  ""      ;

}

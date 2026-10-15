package org.yht.exps.express.model.bms.charge;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;

/**
 *             ENT_CODE
 *           , ENT_BUSINESS_CODE
 *           , CUSTOMER_CODE
 *           , BUSINESS_ID
 * 			 , CUSTOMER_NAME
 * 			 , TEL
 * 			 , ATTN_ID
 * 			 , ATTN
 * 			 , TEL AS EBILL_TEL
 * 			 , EMAIL
 * 			 , ADDRESS
 * 			 , ADDRESS_A
 * 			 , BUSINESS_CONDITION
 * 			 , BUSINESS_TYPE
 * 			 , BUSINESS_PRESIDENT
 * 			 , REGISTRATION_CODE
 */
@Data
@Builder
@ApiModel(value = "BmsCustomerChargeInfo", description = "창고료정산팝업 화주등록용 VO 객체")
@Alias("bmsCustomerChargeInfo")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsCustomerChargeInfo {

	@ApiModelProperty(value="도입사코드")
	@Builder.Default private String entCode = "";
	
	@ApiModelProperty(value="도입사사업장코드")
	@Builder.Default private String entBusinessCode = "";
	
	@ApiModelProperty(value="화주코드")
    @Builder.Default private String customerCode = "";
	
	@ApiModelProperty(value="사업자번호")
    @Builder.Default private String businessNo = "";
	
    @ApiModelProperty(value="화주명")
    @Builder.Default private String customerName = "";

    @ApiModelProperty(value="전화번호")
    @Builder.Default private String tel = "";
    
    @ApiModelProperty(value="담당자코드")
    @Builder.Default private String attnId = "";
    
    @ApiModelProperty(value="담당자")
    @Builder.Default private String attn = "";
    
    @ApiModelProperty(value="계산서담당자")
    @Builder.Default private String ebillTel = "";
    
    @ApiModelProperty(value="계산서이메일담당자")
    @Builder.Default private String email = "";
    
    @ApiModelProperty(value="주소")
    @Builder.Default private String address = "";
    
    @ApiModelProperty(value="상세주소")
    @Builder.Default private String addressA = "";

    @ApiModelProperty(value="업태")
    @Builder.Default private String businessCondition = "";
    
    @ApiModelProperty(value="종목")
    @Builder.Default private String businessType = "";
    
    @ApiModelProperty(value="대표자")
    @Builder.Default private String businessPresident = "";
    
    @ApiModelProperty(value="법인관리코드")
    @Builder.Default private String registrationCode = "";
    
    @ApiModelProperty(value="입력자")
    @Builder.Default private String userId = "";
}

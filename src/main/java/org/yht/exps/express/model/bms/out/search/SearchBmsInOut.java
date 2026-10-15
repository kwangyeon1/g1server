package org.yht.exps.express.model.bms.out.search;



import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;


/***
 *  @Builder.Default 를 쓰는 이유는 디비 컬럼 모두가 NOT NULL로 되어 있어서 입력되지 않은 항목을 모두 입력 시키기 위해 선언함
 */
@Data
@Builder
@ApiModel(value = "SearchBmsInOut", description = "반입, 반출 VO 객체")
@Alias("searchBmsInOut")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class SearchBmsInOut {

    @ApiModelProperty(value="inNo")
    private int inNo;

    @ApiModelProperty(value="inPlanNo")
    private int inPlanNo;

    @ApiModelProperty(value="outNo")
    private int outNo;

    @ApiModelProperty(value="outPlanNo")
    private int outPlanNo;
    
    @ApiModelProperty(value="docType")
    @Builder.Default private String docType = "";
    
    @ApiModelProperty(value="cargoReferenceNo")
    @Builder.Default private String cargoReferenceNo = "";
    
    @ApiModelProperty(value="도입사코드", example="YHTXX")
    private String entCode;
    @ApiModelProperty(value="대표사업장코드", example="YHTXX")
    private String entBusinessCode;
    
}

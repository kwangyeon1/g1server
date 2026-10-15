package org.yht.exps.express.model.bms.bmsComm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

/**
 * (EcmPod01)Entity
 *
 * @author Woong Jang
 * @since 2024-07-12 11:04:56
 */
@Data
@Builder
@ApiModel(value = "Disposal", description = "폐기 VO 객체")
@Alias("bmsEcmPod")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsEcmPod extends ResultList {
    private static final long serialVersionUID = 440789245849573979L;

    @ApiModelProperty(value="POD_JOB번호")
    private  String   podJobNo ="" ;

    @ApiModelProperty(value="도입사코드")
    @Builder.Default  private  String   entCode =""   ;

    @ApiModelProperty(value="OD_JOB번호")
    @Builder.Default  private  String   odJobNo    ="" ;

    @ApiModelProperty(value="진행구분코드")
    @Builder.Default  private  String   podStatus    ="" ;

    @ApiModelProperty(value="진행구분명")
    @Builder.Default  private  String   podName    ="" ;

    @ApiModelProperty(value="POD발생일자")
    @Builder.Default  private String podDate ="" ;

    @ApiModelProperty(value="POD발생시간")
    @Builder.Default  private  String   podTime    ="" ;

    @ApiModelProperty(value="BL_번호")
    @Builder.Default  private  String   blNo  =""   ;

    @ApiModelProperty(value="비고")
    @Builder.Default  private  String   remarks  =""   ;

    @ApiModelProperty(value="입력자코드")
    @Builder.Default  private  String   registerUserId   =""  ;

    @ApiModelProperty(value="입력일시")
    @Builder.Default  private  String   registerDateTime  =""   ;

    @ApiModelProperty(value="수정자코드")
    @Builder.Default  private  String   updateUserId   =""  ;

    @ApiModelProperty(value="수정일시")
    @Builder.Default  private  String   updateDateTime  =""   ;

  /*  @ApiModelProperty(value="테이블 No : 기본은 01")
    @Builder.Default  private  String   podTableNo  ="01"   ;*/
/*
    @ApiModelProperty(value="대표사업장코드", example="YHTXX")
    @Builder.Default  private  String   entBusinessCode =""   ;*/


}
package org.yht.exps.express.model.bms.charge;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import java.math.BigDecimal;

/**
 * 특송창고_작업료율(BmsStorageRate)Entity
 *
 * @author Woong Jang
 * @since 2024-04-19 08:59:33
 */
@Data
@Builder
@ApiModel(value = "BmsWorkRate", description = "작업료율 VO 객체")
@Alias("bmsWorkRate")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class BmsWorkRate extends ResultList {

    @ApiModelProperty(value="작업료요율번호")
    private  Integer   workRateNo ;

    @ApiModelProperty(value="적용일자")
    @Builder.Default  private  String   applyDate    =  ""      ;

    @ApiModelProperty(value="요율구분(보관료와같음)")
    @Builder.Default  private  String   applyClass    =  ""      ;

    @ApiModelProperty(value="기본금액")
    @Builder.Default  private  BigDecimal   payBasic    =     BigDecimal.ZERO       ;

    @ApiModelProperty(value="최소금액")
    @Builder.Default  private  BigDecimal   payMin    =     BigDecimal.ZERO       ;

    @ApiModelProperty(value="초과중량")
    @Builder.Default  private  BigDecimal   overWeight    =     BigDecimal.ZERO       ;

    @ApiModelProperty(value="초과단가")
    @Builder.Default  private  BigDecimal   overPrice    =     BigDecimal.ZERO       ;

    @ApiModelProperty(value="최저적용중량")
    @Builder.Default  private  BigDecimal   wtMin    =    BigDecimal.ZERO       ;

    @ApiModelProperty(value="현도초과중량단가")
    @Builder.Default  private  BigDecimal   assignOverPrice    =    BigDecimal.ZERO       ;



    @ApiModelProperty(value="입력자")
    @Builder.Default  private  String   registerUserId    =  ""      ;

    @ApiModelProperty(value="입력일시")
    @Builder.Default  private  String   registerDateTime    =   ""       ;

    @ApiModelProperty(value="수정자")
    @Builder.Default  private  String   updateUserId    =  ""      ;

    @ApiModelProperty(value="수정일시")
    @Builder.Default  private  String   updateDateTime    =   ""       ;

    @ApiModelProperty(value="입력자명")
    private  String   registerUserName;
    @ApiModelProperty(value="수정자명")
    private  String   updateUserName;
//
    /** 정산료신고 조회 */
    @ApiModelProperty(value="총건수")
    private int totalCnt;
    @ApiModelProperty(value="목록건수")
    private int totalSimpleCnt;
    @ApiModelProperty(value="일반건수")
    private int totalGeneralCnt;
    @ApiModelProperty(value="총건수")
    private int cnt;
    @ApiModelProperty(value="목록건수")
    private int simpleCnt;
    @ApiModelProperty(value="일반건수")
    private int generalCnt;

    @ApiModelProperty(value="적용일자명")
    @Builder.Default  private  String   applyName    =  ""      ;
}


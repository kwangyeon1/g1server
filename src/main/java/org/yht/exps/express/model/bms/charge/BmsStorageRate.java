package org.yht.exps.express.model.bms.charge;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import java.math.BigDecimal;

/**
 * 특송창고_보관료요율(BmsStorageRate)Entity
 *
 * @author Woong Jang
 * @since 2024-04-19 08:59:33
 */
@Data
@Builder
@ApiModel(value = "BmsStorageRate", description = "특송창고_보관료요율 VO 객체")
@Alias("bmsStorageRate")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class BmsStorageRate extends ResultList {

    @ApiModelProperty(value="보관요율번호")
    private  String   storageRateNo ;

    @ApiModelProperty(value="적용일자")
    @Builder.Default  private  String   applyDate    =  ""      ;

    @ApiModelProperty(value="보관료구분(1.검역특수,2.검역일반,3.수입화물,4.수출화물)")
    @Builder.Default  private  String   applyClass    =  ""      ;

    @ApiModelProperty(value="시작일")
    @Builder.Default  private  Integer   storageStartDays    =   0       ;

    @ApiModelProperty(value="종료일")
    @Builder.Default  private  Integer   storageEndDays    =   0       ;

    @ApiModelProperty(value="종가기본료")
    @Builder.Default  private  BigDecimal   amountBasic    =    BigDecimal.ZERO         ;

    @ApiModelProperty(value="종가할인료")
    @Builder.Default  private  BigDecimal   amountOver    =    BigDecimal.ZERO         ;

    @ApiModelProperty(value="종량기본료")
    @Builder.Default  private  BigDecimal   weightBasic    =    BigDecimal.ZERO        ;

    @ApiModelProperty(value="종량할인료")
    @Builder.Default  private  BigDecimal   weightOver    =    BigDecimal.ZERO         ;

    @ApiModelProperty(value="최소금액")
    @Builder.Default  private  BigDecimal   payMin    =    BigDecimal.ZERO         ;

    @ApiModelProperty(value="최저적용중량")
    @Builder.Default  private  BigDecimal   wtMin    =   BigDecimal.ZERO       ;

    @ApiModelProperty(value="입력자")
    @Builder.Default  private  String   registerUserId    =  ""      ;

    @ApiModelProperty(value="입력일시")
    @Builder.Default  private  String   registerDateTime    =   ""       ;

    @ApiModelProperty(value="수정자")
    @Builder.Default  private  String   updateUserId    =  ""      ;

    @ApiModelProperty(value="수정일시")
    @Builder.Default  private  String   updateDateTime    =   ""       ;

//
    @ApiModelProperty(value="입력자명")
    private  String   registerUserName;
    @ApiModelProperty(value="수정자명")
    private  String   updateUserName;
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


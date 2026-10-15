package org.yht.exps.express.model.bms.charge;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import java.math.BigDecimal;
/**
 * 창고료계산(BmsEstimate)Entity
 *
 * @author Woong Jang
 * @since 2024-04-18 16:53:45
 */
@Data
@Builder
@ApiModel(value = "BmsEstimate", description = "창고료계산 VO 객체")
@Alias("bmsEstimate")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class BmsEstimate extends ResultList {
    @ApiModelProperty(value="창고료번호(매출번호)")
    private  Integer   estimateNo ;

    @ApiModelProperty(value="반출승인일자")
    @Builder.Default  private  String   approvalDate    =  ""      ;

    @ApiModelProperty(value="반출구분")
    @Builder.Default  private  String   outClass    =  ""      ;

    @ApiModelProperty(value="반출근거번호")
    @Builder.Default  private  String   outApprovalNo    =  ""      ;

    @ApiModelProperty(value="오더관리번호")
    @Builder.Default  private  String   odJobNo    =  ""      ;
    
    @ApiModelProperty(value="COM_OD_JOB_NO")
    @Builder.Default  private  String   comOdJobNo    =  ""      ;

    @ApiModelProperty(value="반출번호")
    @Builder.Default  private  Integer   outNo    =   0       ;

    @ApiModelProperty(value="반입번호")
    @Builder.Default  private  Integer   inNo    =   0       ;

    @ApiModelProperty(value="반출승인통보번호")
    @Builder.Default  private  Integer   outPlanNo    =   0       ;

    @ApiModelProperty(value="화물관리번호")
    @Builder.Default  private  String   cargoReferenceNo    =  ""      ;

    @ApiModelProperty(value="BL번호")
    @Builder.Default  private  String   blNo    =  ""      ;

    @ApiModelProperty(value="MBL번호")
    @Builder.Default  private  String   mblNo    =  ""      ;

    @ApiModelProperty(value="D/O번호")
    @Builder.Default  private  String   doNo    =  ""      ;

    @ApiModelProperty(value="반입일자")
    @Builder.Default  private  String   carryinDate    =  ""      ;

    @ApiModelProperty(value="반입시간")
    @Builder.Default  private  String   carryinTime    =  ""      ;

    @ApiModelProperty(value="반출일자")
    @Builder.Default  private  String   carryoutDate    =  ""      ;

    @ApiModelProperty(value="반출시간")
    @Builder.Default  private  String   carryoutTime    =  ""      ;

    @ApiModelProperty(value="창고료계산일자")
    @Builder.Default  private  String   estimateDate   ="" ;

    @ApiModelProperty(value="반출수량")
    @Builder.Default  private  Integer   outCt    =   0       ;

    @ApiModelProperty(value="반출중량")
    @Builder.Default  private  BigDecimal   outWt    =    BigDecimal.ZERO       ;

    @ApiModelProperty(value="반출용적")
    @Builder.Default  private  BigDecimal   outCbm    =    BigDecimal.ZERO        ;

    @ApiModelProperty(value="화주코드")
    @Builder.Default  private  String   customerCode    =  ""      ;

    @ApiModelProperty(value="화주명")
    @Builder.Default  private  String   customerName    =  ""      ;

    @ApiModelProperty(value="관세사코드")
    @Builder.Default  private  String   brokerCode    =  ""      ;

    @ApiModelProperty(value="관세사명")
    @Builder.Default  private  String   brokerName    =  ""      ;

    @ApiModelProperty(value="비고")
    @Builder.Default  private  String   remarks    =  ""      ;

    @ApiModelProperty(value="요율구분(창고료요율구분)")
    @Builder.Default  private  String   applyClass    =  ""      ;

    @ApiModelProperty(value="발행번호")
    @Builder.Default  private  String   applyNo    =   ""       ;

    @ApiModelProperty(value="현도구분")
    @Builder.Default  private  String   assignYn    =   "N"       ;

    @ApiModelProperty(value="면제여부(N.아님,Y.면제,C.4일면제)")
    @Builder.Default  private  String   freeYnc    =   "N"       ;

    @ApiModelProperty(value="관세")
    @Builder.Default  private  BigDecimal   approvalDuty    =    BigDecimal.ZERO        ;

    @ApiModelProperty(value="감정가액")
    @Builder.Default  private  BigDecimal   approvalAppraiseValue    =   BigDecimal.ZERO        ;

    @ApiModelProperty(value="보관료적용일자")
    @Builder.Default  private  String   storageApplyDate    =  ""      ;

    @ApiModelProperty(value="보관중량")
    @Builder.Default  private  BigDecimal   storageWt    =    BigDecimal.ZERO         ;

    @ApiModelProperty(value="보관일수")
    @Builder.Default  private  Integer   storageDays    =   0       ;

    @ApiModelProperty(value="종가기본")
    @Builder.Default  private  BigDecimal   storageDayBasic    =    BigDecimal.ZERO         ;

    @ApiModelProperty(value="종가기본료")
    @Builder.Default  private  BigDecimal   storageDayBasicAmount    =    BigDecimal.ZERO        ;

    @ApiModelProperty(value="종가일일할증")
    @Builder.Default  private  BigDecimal   storageDayOver    =    BigDecimal.ZERO         ;

    @ApiModelProperty(value="종가일일할증료")
    @Builder.Default  private  BigDecimal   storageDayOverAmount    =    BigDecimal.ZERO       ;

    @ApiModelProperty(value="종가율")
    @Builder.Default  private  BigDecimal   storageDayRate    =    BigDecimal.ZERO         ;

    @ApiModelProperty(value="종가료")
    @Builder.Default  private  BigDecimal   storageDayRateAmount    =    BigDecimal.ZERO       ;

    @ApiModelProperty(value="종량기본")
    @Builder.Default  private  BigDecimal   storageWeightBasic   = BigDecimal.ZERO   ;

    @ApiModelProperty(value="종량기본료")
    @Builder.Default  private  BigDecimal   storageWeightBasicAmount    =    BigDecimal.ZERO         ;

    @ApiModelProperty(value="종량일일할증")
    @Builder.Default  private  BigDecimal   storageWeightOver    =    BigDecimal.ZERO         ;

    @ApiModelProperty(value="종량일일할증료")
    @Builder.Default  private  BigDecimal   storageWeightOverAmount    =    BigDecimal.ZERO        ;

    @ApiModelProperty(value="종량율")
    @Builder.Default  private  BigDecimal   storageWeightRate    =    BigDecimal.ZERO        ;

    @ApiModelProperty(value="종량료")
    @Builder.Default  private  BigDecimal   storageWeightRateAmount    =    BigDecimal.ZERO      ;

    @ApiModelProperty(value="보관할증")
    @Builder.Default  private  Integer   storageOverPer    =   0       ;

    @ApiModelProperty(value="보관할증료")
    @Builder.Default  private  BigDecimal   storageOverAmount    =   BigDecimal.ZERO       ;

    @ApiModelProperty(value="보관할인")
    @Builder.Default  private  Integer   storageDiscountPer    =   0       ;

    @ApiModelProperty(value="보관할인료")
    @Builder.Default  private  BigDecimal   storageDiscountAmount    =   BigDecimal.ZERO       ;

    @ApiModelProperty(value="보관료")
    @Builder.Default  private  BigDecimal   storageAmount    =    BigDecimal.ZERO       ;

    @ApiModelProperty(value="작업적용일자")
    @Builder.Default  private  String   workApplyDate    =  ""      ;

    @ApiModelProperty(value="작업중량")
    @Builder.Default  private  BigDecimal   workWt    =   BigDecimal.ZERO       ;

    @ApiModelProperty(value="작업기본")
    @Builder.Default  private  BigDecimal   workPayBasic    =   BigDecimal.ZERO       ;

    @ApiModelProperty(value="작업할증")
    @Builder.Default  private  BigDecimal   workPayRate    =   BigDecimal.ZERO       ;

    @ApiModelProperty(value="작업료")
    @Builder.Default  private  BigDecimal   workAmount    =    BigDecimal.ZERO       ;

    @ApiModelProperty(value="부가세여부")
    @Builder.Default  private  String   vatYn    =   "Y"       ;

    @ApiModelProperty(value="공급가액_자국")
    @Builder.Default  private  BigDecimal   amountLoc    =  BigDecimal.ZERO       ;

    @ApiModelProperty(value="부가세액_자국")
    @Builder.Default  private  BigDecimal   amountVatLoc    =   BigDecimal.ZERO      ;

    @ApiModelProperty(value="합계금액_자국")
    @Builder.Default  private  BigDecimal   amountTotalLoc    =   BigDecimal.ZERO       ;

    @ApiModelProperty(value="입금액")
    @Builder.Default  private  BigDecimal   amountRcv    =   BigDecimal.ZERO     ;

    @ApiModelProperty(value="입금여부")
    @Builder.Default  private  String   rcvYn    =   "N"       ;

    @ApiModelProperty(value="계산여부")
    @Builder.Default  private  String   invoiceYn    =   "N"       ;
    
    @ApiModelProperty(value="ULD여부")
    private String uldYn;

    @ApiModelProperty(value="입력자")
    @Builder.Default  private  String   registerUserId    =  ""      ;

    @ApiModelProperty(value="입력일시")
    @Builder.Default  private  String   registerDateTime    =  ""     ;

    @ApiModelProperty(value="수정자")
    @Builder.Default  private  String   updateUserId    =  ""      ;

    @ApiModelProperty(value="수정일시")
    @Builder.Default  private  String   updateDateTime    =   ""     ;

//
    @ApiModelProperty(value="입력자명")
    private  String   registerUserName;
    @ApiModelProperty(value="수정자명")
    private  String   updateUserName;

    @ApiModelProperty(value="재고,반출,반출승인")
    private String sdType ; //STOCK, OUT,PLAN , ESTIMATE
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

    @ApiModelProperty(value="파일공유여부")
    private String fileShare;
    @ApiModelProperty(value="관세청")
    private String blStatus;
    
    /** 요율구분 이름 */
    @ApiModelProperty(value="특수화물")
    private String applyClassName;

    /** 요율구분수정여부 (pod용) */
    @ApiModelProperty(value="요율구분수정여부 (pod용)")
    @Builder.Default
    private String applyClassChangedYn = "N";
    
    /** 매출 그리드 위한 다른 필드 BMS_OUT START **/

    @ApiModelProperty(value = "반입구분(그리드공통코드매핑 빈값)")
    @Builder.Default
    private String inClass = "";

    @ApiModelProperty(value="포장종류")
    @Builder.Default
    private  String   pkgType  = "";

    @ApiModelProperty(value="gmtTime")
    private String gmtTime;
    @ApiModelProperty(value="날짜형식")
    private String dateFormat;
    @ApiModelProperty(value="도입사코드", example="YHTXX")
    private String entCode;
    @ApiModelProperty(value="대표사업장코드", example="YHTXX")
    private String entBusinessCode;

    //시스템 로그
    @ApiModelProperty(value="처리자")
    private String logUserName;
    @ApiModelProperty(value="처리자(영)")
    private String logUserNameEng;
    @ApiModelProperty(value="출력용 사업장명")
    private String logUserBusinessPrintName;

    @ApiModelProperty(value="도입사_사업장코드(8)+\"-INV\"+년도(4)+월(2)+\"-\"+순번(5) 24자리", example="")
    private String invoiceJobNo;
    
    /** 세금계산서 발행시 필요한 필드 **/
    @ApiModelProperty(value="담당자")
    private String attn;
    @ApiModelProperty(value="전화번호")
    private String tel;
    @ApiModelProperty(value="담당자 이메일")
    private String email;
    
    @ApiModelProperty(value="정산번호를 입력하지 않은 경우에는 사업장(8) 코드를", example="")
    private String invoiceNo;
    /** 매출 그리드 위한 다른 필드 BMS_OUT END **/
    /**
     * 11-4-4 표시 그리드에서 사용
     * @return
     */
    @ApiModelProperty(value="화물관리번호_표시")
    public String getCargoReferenceNoDisp() {
        String ret = getCargoReferenceNoDisp(this.cargoReferenceNo);
        return ret;
    }

    private String getCargoReferenceNoDisp(String _cargoReferenceNo) {
        String ret ="";
        if ( ! _cargoReferenceNo.equals("") ){
            if (_cargoReferenceNo.length() > 10) {
                if (_cargoReferenceNo.length() == 19) {
                    ret = _cargoReferenceNo.substring(0, 11) + "-" + _cargoReferenceNo.substring(11, 15) + "-" + _cargoReferenceNo.substring(15, 19);
                }else if (_cargoReferenceNo.length() == 18) { // 마이그레이션 값은 18이다
                    ret = _cargoReferenceNo.substring(0, 11) + "-" + _cargoReferenceNo.substring(11, 15) + "-" + _cargoReferenceNo.substring(15);
                } else {
                    ret = _cargoReferenceNo.substring(0, 11) + "-" + _cargoReferenceNo.substring(11);
                }
            }else {
                ret = _cargoReferenceNo;
            }
        }
        return ret;
    }
}


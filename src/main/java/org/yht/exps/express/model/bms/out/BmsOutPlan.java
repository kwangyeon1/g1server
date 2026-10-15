package org.yht.exps.express.model.bms.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

/***
 *  @Builder.Default 를 쓰는 이유는 디비 컬럼 모두가 NOT NULL로 되어 있어서 입력되지 않은 항목을 모두 입력 시키기 위해 선언함
 */
/**
 * 반출승인내역통보(BmsOutPlan)Entity
 *
 * @author Woong Jang
 * @since 2024-03-24 22:16:39
 */
@Data
@Builder
@ApiModel(value = "BmsOutPlan", description = "반출승인내역통보 VO 객체")
@Alias("bmsOutPlan")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsOutPlan extends ResultList {
    private static final long serialVersionUID = -45698942923977011L;

    @ApiModelProperty(value="승인통보번호")
    private  Integer   outPlanNo ;

    @ApiModelProperty(value="보세운송관세청통보일시")
    @Builder.Default  private   String  notifyDateTime    =  ""      ;

    @ApiModelProperty(value="승인통보(31.승인,1:취하)")
    @Builder.Default  private  String   approvalClass    =  ""      ;

    @ApiModelProperty(value="승인구분")
    @Builder.Default  private  String   outClass    =  ""      ;

    @ApiModelProperty(value="승인근거번호")
    @Builder.Default  private  String   outApprovalNo    =  ""      ;

    @ApiModelProperty(value="반출승인일자")
    @Builder.Default  private  String   approvalDate    =  ""      ;

    @ApiModelProperty(value="승인취하일자")
    @Builder.Default  private  String   approvalCancelDate    =  ""      ;

    @ApiModelProperty(value="보세구역부호")
    @Builder.Default  private  String   bondedWarehouseNo    =  ""      ;

    @ApiModelProperty(value="장치확인번호")
    @Builder.Default  private  Object   locationConfirmNo    =  ""      ;

    @ApiModelProperty(value="반입번호")
    @Builder.Default  private  Integer   inNo    =  0      ;

    @ApiModelProperty(value="화물관리번호")
    @Builder.Default  private  String   cargoReferenceNo=""    ;

    @ApiModelProperty(value="BL번호")
    @Builder.Default  private  String   blNo    =  ""      ;

    @ApiModelProperty(value="MBL번호")
    @Builder.Default  private  String   mblNo    =  ""      ;

    @ApiModelProperty(value="관세")
    @Builder.Default  private  Object   duty    =  0      ;

    @ApiModelProperty(value="감정가액")
    @Builder.Default  private  Object   appraiseValue    =  0      ;

    @ApiModelProperty(value="인도조건")
    @Builder.Default  private  String   deliveryTerms    =  ""      ;

    @ApiModelProperty(value="선기명")
    @Builder.Default  private  String   vesselFlight    =  ""      ;

    @ApiModelProperty(value="항차")
    @Builder.Default  private  String   voyage    =  ""      ;

    @ApiModelProperty(value="입항일자")
    @Builder.Default  private  String   arDate    =  ""      ;

    @ApiModelProperty(value="영문품명")
    @Builder.Default  private  String   commodityItemLoc=""    ;

    @ApiModelProperty(value="납세의무자사업자번호")
    @Builder.Default  private  String   companyBusinessNo    =  ""      ;

    @ApiModelProperty(value="납세의무자상호")
    @Builder.Default  private  String   companyName    =  ""      ;

    @ApiModelProperty(value="납세의무자성명")
    @Builder.Default  private  String   companyPresident    =  ""      ;

    @ApiModelProperty(value="포장종류")
    @Builder.Default  private  String   pkgType    =  ""      ;

    @ApiModelProperty(value="반출승인수량")
    @Builder.Default  private  Integer   approvalCt    =  0      ;

    @ApiModelProperty(value="반출승인중량")
    @Builder.Default  private  Double   approvalWt    =  0.00     ;

    @ApiModelProperty(value="반출승인용적")
    @Builder.Default  private  Double   approvalCbm    = 0.00      ;

    @ApiModelProperty(value="반입수량")
    @Builder.Default  private  Integer   ct    =  0      ;

    @ApiModelProperty(value="반입중량")
    @Builder.Default  private  Double   wt    =  0.00     ;

    @ApiModelProperty(value="반입용적")
    @Builder.Default  private  Double   cbm    = 0.00      ;

    @ApiModelProperty(value="결제방법")
    @Builder.Default  private  String   insPaymentType    =  ""      ;

    @ApiModelProperty(value="결제통화")
    @Builder.Default  private  String   currencyCode    =  ""      ;

    @ApiModelProperty(value="적하보험료납부여부")
    @Builder.Default  private  String   insPaymentYn    = "N";

    @ApiModelProperty(value="컨테이너번호반출승인")
    @Builder.Default  private  String   cntrNoApp    =  ""      ;

    @ApiModelProperty(value="반출처리여부")
    @Builder.Default  private  String   outYn    =  "N"      ;

    @ApiModelProperty(value="승인검사대상")
    @Builder.Default  private  String   inspectYn    = "N";

    @ApiModelProperty(value="HS코드")
    @Builder.Default  private  String   hsCode    =  ""      ;

    @ApiModelProperty(value="수입요건법령코드")
    @Builder.Default  private  String   criteriaCode    =  ""      ;

    @ApiModelProperty(value="승인~취소간격")
    @Builder.Default  private  Object   appcancelDateDiff    =  ""      ;

    @ApiModelProperty(value="반입~승인간격")
    @Builder.Default  private  Object   inappDateDiff    =  ""      ;

    @ApiModelProperty(value="출고SCAN일시")
    @Builder.Default  private   String  outScanDateTime    =  ""      ;

    @ApiModelProperty(value="입력자코드")
    @Builder.Default  private  String   registerUserId    =  ""      ;

    @ApiModelProperty(value="입력일시")
    @Builder.Default  private   String  registerDateTime    =  ""     ;

    @ApiModelProperty(value="수정자코드")
    @Builder.Default  private  String   updateUserId    =  ""      ;

    @ApiModelProperty(value="수정일시")
    @Builder.Default  private   String  updateDateTime    =  ""      ;

    @ApiModelProperty(value="입력자명")
    private String registerUserName;

    @ApiModelProperty(value="수정자명")
    private String updateUserName;

    @ApiModelProperty(value="재고,반출,반출승인")
    private String sdType ; //STOCK, OUT,PLAN 유형

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

	/** 반출신고 조회 */
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
	@ApiModelProperty(value="진행상태")
	private String lastPodStatus;
	@ApiModelProperty(value="관세청")
	private String blStatus;
	@ApiModelProperty(value="반입일자")
	private String carryinDate;
	@ApiModelProperty(value="반입시간")
	private String carryinTime;
	@ApiModelProperty(value="화물구분")
	private String cdType;
	@ApiModelProperty(value="물품가격")
	private String amountUsd;
	@ApiModelProperty(value="용도구분")
	private String cdUseType;
	@ApiModelProperty(value="거래구분")
	private String ecmType;
	@ApiModelProperty(value="거대화물여부")
	private String bigSizeYn;
	@ApiModelProperty(value="선별")
	private String seInspectionCode;
	@ApiModelProperty(value="현도여부")
    private String assignYn;
    @ApiModelProperty(value="우범화물여부")
    private String crimeSelectionYn;
    @ApiModelProperty(value="지재권")
    private String intellectualPropYn;
    @ApiModelProperty(value="집중검사")
    private String inspectionCustomsYn;
	@ApiModelProperty(value="검역1")
	private String inspectionType;
	@ApiModelProperty(value="검역2")
	private String inspectionTypeA;
	@ApiModelProperty(value="검역3")
	private String inspectionTypeB;
	@ApiModelProperty(value="XRAY판독")
	private String xrayInspectionCode;
	@ApiModelProperty(value="XRAY판독수량")
	private String xrayScanCt;
	@ApiModelProperty(value="XRAY이미지수량")
	private String xrayImageCt;
	@ApiModelProperty(value="입항시간")
	private String arTime;
	@ApiModelProperty(value="XRAY호기")
	private String xrayLineNo;
	@ApiModelProperty(value="오더관리번호")
	private String odJobNo;
	@ApiModelProperty(value="홀드여부")
	private String holdYn;
	@ApiModelProperty(value="홀드코드")
	private String holdCode;
	@ApiModelProperty(value="홀드사유")
	private String holdReason;
	@ApiModelProperty(value="홀드처리여부")
	private String holdReleaseYn;
	@ApiModelProperty(value="홀드처리담당자")
	private String holdReleaseAttn;
	@ApiModelProperty(value="홀드처리일자")
	private String holdReleaseDate;
	@ApiModelProperty(value="화주코드")
	private String customerCode;
	@ApiModelProperty(value="화주명")
	private String customerName;
    @ApiModelProperty(value="송하인코드")
    private String shipperCode;
    @ApiModelProperty(value="송하인명")
    private String shipperName;
    @ApiModelProperty(value="수하인코드")
    private String consigneeCode;
    @ApiModelProperty(value="수하인명")
    private String consigneeName;
    @ApiModelProperty(value="통지처코드")
    private String notifyCode;
    @ApiModelProperty(value="통지처명")
    private String notifyName;
    @ApiModelProperty(value="배송사코드")
    private String deliveryCode;
    @ApiModelProperty(value="배송사명")
    private String deliveryName;
    @ApiModelProperty(value="포워더코드")
    private String fwdCode;
    @ApiModelProperty(value="포워더명")
    private String fwdName;
    @ApiModelProperty(value="장치위치")
    private String locationPlace;
    @ApiModelProperty(value="비고1")
    private String remarks1;
    @ApiModelProperty(value="비고2")
    private String remarks2;
    @ApiModelProperty(value="비고3")
    private String remarks3;
	@ApiModelProperty(value="특수화물코드1")
	private String specialCode1;
	@ApiModelProperty(value="특수화물코드2")
	private String specialCode2;
	@ApiModelProperty(value="특수화물코드3")
	private String specialCode3;
	@ApiModelProperty(value="발송국가")
	private String origCountry;
	@ApiModelProperty(value="참조번호")
	private String referenceNo;

}

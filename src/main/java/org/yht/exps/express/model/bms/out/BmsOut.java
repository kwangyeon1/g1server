package org.yht.exps.express.model.bms.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import java.math.BigDecimal;
import java.util.List;

/***
 *  @Builder.Default 를 쓰는 이유는 디비 컬럼 모두가 NOT NULL로 되어 있어서 입력되지 않은 항목을 모두 입력 시키기 위해 선언함
 */
@Data
@Builder
@ApiModel(value = "BmsOut", description = "반출 VO 객체")
@Alias("bmsOut")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsOut extends ResultList{

    @ApiModelProperty(value="반출번호")
    private  Integer   outNo ;

    @ApiModelProperty(value="반출신고번호")
    @Builder.Default  private  String   customsReportNo    =  ""      ;


    @ApiModelProperty(value="현재진행상태일시")
    @Builder.Default  private   String  lastPodDate    =  ""      ;

    @ApiModelProperty(value="현재진행상태코드")
    @Builder.Default  private  String   lastPodStatus    =  ""      ;

    @ApiModelProperty(value="접수일시")
    @Builder.Default  private   String  lastPodRaqDate   =  ""      ;

    @ApiModelProperty(value="반출승인일자")
    @Builder.Default  private  String   approvalDate    =  ""      ;

    @ApiModelProperty(value="반출구분")
    @Builder.Default  private  String   outClass    =  ""      ;

    @ApiModelProperty(value="반출근거번호")
    @Builder.Default  private  String   outApprovalNo    =  ""      ;

    @ApiModelProperty(value="오더관리번호")
    @Builder.Default  private  String   odJobNo    =  ""      ;

    @ApiModelProperty(value="반입번호")
    @Builder.Default  private  Integer   inNo    =  0      ;

    @ApiModelProperty(value="반출승인통보번호")
    @Builder.Default  private  Integer   outPlanNo    =  0      ;

    @ApiModelProperty(value="화물관리번호")
    @Builder.Default  private  String   cargoReferenceNo    =  ""      ;

    @ApiModelProperty(value="BL번호")
    @Builder.Default  private  String   blNo    =  ""      ;

    @ApiModelProperty(value="MBL번호")
    @Builder.Default  private  String   mblNo    =  ""      ;

    @ApiModelProperty(value="DO번호")
    @Builder.Default  private  String   doNo    =  ""      ;

    @ApiModelProperty(value="DO일자")
    @Builder.Default  private  String   doDate    =  ""      ;

    @ApiModelProperty(value="세관코드")
    @Builder.Default  private  String   customsCode    =  ""      ;

    @ApiModelProperty(value="세관과코드")
    @Builder.Default  private  String   customsDptCode    =  ""      ;

    @ApiModelProperty(value="화주코드")
    @Builder.Default  private  String   customerCode    =  ""      ;

    @ApiModelProperty(value="화주명")
    @Builder.Default  private  String   customerName    =  ""      ;

    @ApiModelProperty(value="송하인코드")
    @Builder.Default  private String shipperCode = "";

    @ApiModelProperty(value="송하인명")
    @Builder.Default  private String shipperName = "";

    @ApiModelProperty(value="수하인코드")
    @Builder.Default  private String consigneeCode = "";

    @ApiModelProperty(value="수하인명")
    @Builder.Default  private String consigneeName = "";

    @ApiModelProperty(value="통지처코드")
    @Builder.Default  private String notifyCode = "";

    @ApiModelProperty(value="통지처명")
    @Builder.Default  private String notifyName = "";

    @ApiModelProperty(value="배송사코드")
    @Builder.Default  private String deliveryCode = "";

    @ApiModelProperty(value="배송사명")
    @Builder.Default  private String deliveryName = "";

    @ApiModelProperty(value="포워더코드")
    @Builder.Default  private String fwdCode = "";

    @ApiModelProperty(value="포워더명")
    @Builder.Default  private String fwdName = "";

    @ApiModelProperty(value="장치위치")
    @Builder.Default  private String locationPlace = "";

    @ApiModelProperty(value="관세사코드")
    @Builder.Default  private  String   brokerCode    =  ""      ;

    @ApiModelProperty(value="관세사명")
    @Builder.Default  private  String   brokerName    =  ""      ;

    @ApiModelProperty(value="포장종류")
    @Builder.Default  private  String   pkgType    =  ""      ;

    @ApiModelProperty(value="승인수량")
    @Builder.Default  private  Integer   approvalCt    =  0      ;

    @ApiModelProperty(value="승인중량")
    @Builder.Default  private  Double   approvalWt    =  0.00      ;

    @ApiModelProperty(value="승인용적")
    @Builder.Default  private  Double   approvalCbm    =  0.00      ;

    @ApiModelProperty(value="반출수량")
    @Builder.Default  private  Integer   outCt    =  0      ;

    @ApiModelProperty(value="반출중량")
    @Builder.Default  private  Double   outWt    =  0.00      ;

    @ApiModelProperty(value="반출용적")
    @Builder.Default  private  Double   outCbm    =  0.00      ;

    @ApiModelProperty(value="반입수량")
    @Builder.Default  private  Integer   ct    =  0      ;

    @ApiModelProperty(value="반입중량")
    @Builder.Default  private  Double   wt    =  0.00      ;

    @ApiModelProperty(value="반입용적")
    @Builder.Default  private  Double   cbm    =  0.00      ;

    @ApiModelProperty(value="반출일자")
    @Builder.Default  private  String   carryoutDate    =  ""      ;

    @ApiModelProperty(value="반출시간")
    @Builder.Default  private  String   carryoutTime    =  ""      ;

    @ApiModelProperty(value="관세")
    @Builder.Default  private  BigDecimal   approvalDuty   = BigDecimal.ZERO  ;

    @ApiModelProperty(value="감정가액")
    @Builder.Default  private BigDecimal approvalAppraiseValue    =  BigDecimal.ZERO  ;

    @ApiModelProperty(value="결제방법")
    @Builder.Default  private  String   insPaymentType    =  ""      ;

    @ApiModelProperty(value="결제통화")
    @Builder.Default  private  String   currencyCode    =  ""      ;

    @ApiModelProperty(value="적하보험료납부")
    @Builder.Default  private  String   insPaymentYn    =  "N"      ;

    @ApiModelProperty(value="검사대상여부")
    @Builder.Default  private  String   inspectYn    =  "N"      ;

    @ApiModelProperty(value="HS코드")
    @Builder.Default  private  String   hsCode    =  ""      ;

    @ApiModelProperty(value="수입요건법령코드")
    @Builder.Default  private  String   criteriaCode    =  ""      ;

    @ApiModelProperty(value="반입반출간격")
    @Builder.Default  private  String   inoutDateDiff    =  ""      ;

    @ApiModelProperty(value="비고1")
    @Builder.Default  private  String   remarks1    =  ""      ;

    @ApiModelProperty(value="비고2")
    @Builder.Default  private  String   remarks2    =  ""      ;

    @ApiModelProperty(value="비고3")
    @Builder.Default  private  String   remarks3    =  ""      ;

    @ApiModelProperty(value="참조번호")
    @Builder.Default  private  String   referenceNo  = ""  ;

    @ApiModelProperty(value="입력자명")
    private  String   registerUserName;
    @ApiModelProperty(value="수정자명")
    private  String   updateUserName;

    @ApiModelProperty(value="입력자코드")
    @Builder.Default  private  String   registerUserId  = ""  ;
    @ApiModelProperty(value="입력일시")
    @Builder.Default  private   String  registerDateTime  = ""  ;
    @ApiModelProperty(value="수정자코드")
    @Builder.Default  private  String   updateUserId  = ""  ;
    @ApiModelProperty(value="수정일시")
    @Builder.Default  private   String  updateDateTime  = ""  ;

    @ApiModelProperty(value="반출여부(IN,PLAN) 업데이트용")
    @Builder.Default  private   String  outYn  = "N"  ;
    @ApiModelProperty(value="TIC이고반출FTP전송여부")
    @Builder.Default  private   String  ticOutFtpSendYn  = "N"  ;
    @ApiModelProperty(value="TIC이고반출차수")
    private   String  ticOutSeq;
    @ApiModelProperty(value="출고SCAN일시")
    @Builder.Default  private   String  outScanDateTime    =  ""      ;
    @ApiModelProperty(value="반출승인내역통보 출고SCAN일시")
    @Builder.Default  private   String  outPlanScanDateTime    =  ""      ;
    @ApiModelProperty(value="이고반출 스캔여부")
    private   String  ticOutScanYn;
    @ApiModelProperty(value="이고반출 수입신고번호")
    @Builder.Default  private   String  ticOutApprovalNo50 =  "";

    @ApiModelProperty(value="컨테이너 리스트")
    private List<BmsContainer> containerList  ;

    @ApiModelProperty(value="재고,반출,반출승인")
    private String sdType ; //STOCK, OUT,PLAN 유형

    @ApiModelProperty(value="사업장 보세 구역번호 8자리")
    private String bmsWhCode;
    
    @ApiModelProperty(value="특송 총괄과 여부")
    private String ecHoldYn;
    
    @ApiModelProperty(value="새로생성된반입번호")
    private  Integer newInNo;
    
    /**
     *  이고반출확인 사유 POD용
     */
    
    @ApiModelProperty(value="이고반출확인 사유 POD용")
    private String reason;
    /**
     * 11-4-4 표시 그리드에서 사용
     * @return
     */
    @ApiModelProperty(value="화물관리번호_표시")
    public String getCargoReferenceNoDisp() {
        String ret = getCargoReferenceNoDisp(this.cargoReferenceNo);
        return ret;
    }
    //carryinDate, carryinTime  => inoutDateDiff 이값 계산용으로 처리
    @ApiModelProperty(value="반입일자")
    @Builder.Default  private  String   carryinDate  = ""  ;
    @ApiModelProperty(value="반입시간")
    @Builder.Default  private  String   carryinTime  = ""  ;


    @ApiModelProperty(value = "반입구분(그리드공통코드매핑 빈값)")
    @Builder.Default
    private String inClass = "";



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

    @ApiModelProperty(value="반출신고번호표시(8-2-8)")
    public String getCustomsReportNoDisp() {
        String ret = getCustomsReportNoDisp(this.customsReportNo);
        return ret;
    }
    private String getCustomsReportNoDisp(String _customsReportNo) {
        String ret ="";
        if ( ! _customsReportNo.equals("") ){
            if (_customsReportNo.length() > 8) {
                if (_customsReportNo.length() == 18) {
                    ret = _customsReportNo.substring(0, 8) + "-" + _customsReportNo.substring(8, 10) + "-" + _customsReportNo.substring(10, 18);
                }
            }else {
                ret = _customsReportNo;
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
	@ApiModelProperty(value="반출신고한 건수")
	private int totalTry6nbCnt;
	@ApiModelProperty(value="접수 건수")
	private int totalSuccess6nbCnt;
	@ApiModelProperty(value="오류 건수")
	private int totalError6nbCnt;
	
	
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
	@ApiModelProperty(value="인도조건")
	private String deliveryTerms;
	@ApiModelProperty(value="납세의무자사업자번호")
	private String companyBusinessNo;
	@ApiModelProperty(value="납세의무자상호")
	private String companyName;
	@ApiModelProperty(value="납세의무자성명")
	private String companyPresident;
	@ApiModelProperty(value="화물구분")
	private String cdType;
	@ApiModelProperty(value="영문품명")
	private String commodityItemLoc;
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
    @ApiModelProperty(value="원본반입예정번호")
    private Integer origInPlanNo;
	@ApiModelProperty(value="검역1")
	private String inspectionType;
	@ApiModelProperty(value="검역2")
	private String inspectionTypeA;
	@ApiModelProperty(value="검역3")
	private String inspectionTypeB;
	@ApiModelProperty(value="XRAY판독")
	private String xrayInspectionCode;
	@ApiModelProperty(value="통관검사계획코드")
    private String paln5emInspectionCode;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="입항시간")
	private String arTime;
	@ApiModelProperty(value="선기명")
	private String vesselFlight;
	@ApiModelProperty(value="항차")
	private String voyage;
	@ApiModelProperty(value="XRAY호기")
	private String xrayLineNo;
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
	@ApiModelProperty(value="특수화물코드1")
	private String specialCode1;
	@ApiModelProperty(value="특수화물코드2")
	private String specialCode2;
	@ApiModelProperty(value="특수화물코드3")
	private String specialCode3;
	@ApiModelProperty(value="컨테이너번호")
	private String cntrNoApp;
	@ApiModelProperty(value="발송국가")
	private String origCountry;
	
	//TB_ECM_OD_ADDITIONAL
    @ApiModelProperty(value="목록취하여부")
    private String dropYn;

    @ApiModelProperty(value="도입사코드", example="YHTXX")
    private String entCode;
    @ApiModelProperty(value="대표사업장코드", example="YHTXX")
    private String entBusinessCode;

    /** Web XML Send */
    private String typeCode; 		 /** 문서형태구분: 전송될 전자문서의 유형을 기재(ex. GOVCBR6NB) */
    private String functionCode; 	 /** 전자문서기능(9: 원본, 35: 재전송) */
    private String bunhal; 			 /** 분할반출구분(A: 전량, P: 분할, L: 최종) */
    private String carryoutDateTime; /** 반출일시(CCYYMMDDHHMMSS) */
    //시스템 로그
    @ApiModelProperty(value="처리자")
    private String logUserName;
    @ApiModelProperty(value="처리자(영)")
    private String logUserNameEng;
    @ApiModelProperty(value="출력용 사업장명")
    private String logUserBusinessPrintName;
    
    @ApiModelProperty(value="진행상태변경여부")
    private String changeStatusYn;
}

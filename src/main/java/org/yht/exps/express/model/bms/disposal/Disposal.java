package org.yht.exps.express.model.bms.disposal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

/***
 *  @Builder.Default 를 쓰는 이유는 디비 컬럼 모두가 NOT NULL로 되어 있어서 입력되지 않은 항목을 모두 입력 시키기 위해 선언함
 */
@Data
@Builder
@ApiModel(value = "Disposal", description = "폐기 VO 객체")
@Alias("disposal")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class Disposal extends ResultList{

    @ApiModelProperty(value="폐기번호")
    private  Integer   disposalNo  ;
    @ApiModelProperty(value="신청일자")
    @Builder.Default
    private  String   disposalIssueDate  = ""  ;

    @ApiModelProperty(value="반입번호")
    private  Integer   inNo  ;

    @ApiModelProperty(value="화물관리번호")
    @Builder.Default  private  String   cargoReferenceNo  = ""  ;
    @ApiModelProperty(value="BL번호")
    @Builder.Default private  String   blNo  = ""  ;
    @ApiModelProperty(value="BOX번호")
    @Builder.Default private  String   packingNo  = ""  ;
    @ApiModelProperty(value="스캔결과")
    @Builder.Default private  String   scanResult  = ""  ;
    @ApiModelProperty(value="MBL번호")
    @Builder.Default private  String   mblNo  = ""  ;
    @ApiModelProperty(value="폐기사유")

    @Builder.Default  private  String   disposalReason  = ""  ;
    @ApiModelProperty(value="폐기업체코드")
    @Builder.Default  private  String   disposalCode  = ""  ;
    @ApiModelProperty(value="폐기업체명")
    @Builder.Default  private  String   disposalName  = ""  ;
    @ApiModelProperty(value="화주코드")
    @Builder.Default  private  String   customerCode  = ""  ;
    @ApiModelProperty(value="화주명")
    @Builder.Default  private  String   customerName  = ""  ;
    @ApiModelProperty(value="화주전화번호")
    @Builder.Default  private  Object   customerTel  = ""  ;
    @ApiModelProperty(value="화주기본주소")
    @Builder.Default  private  Object   customerAddress1  = ""  ;
    @ApiModelProperty(value="화주상세주소")
    @Builder.Default  private  Object   customerAddress2  = ""  ;
    @ApiModelProperty(value="물품가격")
    @Builder.Default  private  Double   amountUsd   = 0.00   ;
    @ApiModelProperty(value="폐기장소")
    @Builder.Default  private  String   disposalLoc  = ""  ;
    @ApiModelProperty(value="폐기예정일자")
    @Builder.Default  private  String   disposalExpDate  = ""  ;
    @ApiModelProperty(value="폐기방법")
    @Builder.Default  private  String   disposalMethod  = ""  ;
    @ApiModelProperty(value="폐기완료일자")
    @Builder.Default  private  String   disposalFinishDate  = ""  ;
    @ApiModelProperty(value="폐기근거번호")
    @Builder.Default  private  String   disposalApprovalNo  = ""  ;
    @ApiModelProperty(value="포장종류")
    @Builder.Default  private  String   pkgType  = ""  ;

    @ApiModelProperty(value="폐기수량")
    @Builder.Default private  Integer   disposalCt = 0  ;
    @ApiModelProperty(value="폐기중량")
    @Builder.Default private  Double   disposalWt  = 0.00  ;

    @ApiModelProperty(value="반출처리여부")
    @Builder.Default  private  String   outYn  = "N"  ; /* 기본값은 N */
    @ApiModelProperty(value="입력자코드")
    @Builder.Default  private  String   registerUserId  = ""  ;
    @ApiModelProperty(value="입력일시")
    @Builder.Default  private   String  registerDateTime  = ""  ;
    @ApiModelProperty(value="수정자코드")
    @Builder.Default  private  String   updateUserId  = ""  ;
    @ApiModelProperty(value="수정일시")
    @Builder.Default  private   String  updateDateTime  = ""  ;

    @ApiModelProperty(value="gmtTime_기본값 ")
    @Builder.Default  private String gmtTime ="9";
    @ApiModelProperty(value="날짜형식_현재 날짜 입력시 DATEADD(HOUR, 9, GETDATE()) ")
    @Builder.Default  private String dateFormat ="23";

    @ApiModelProperty(value="재고,폐기유형")
    private String sdType ; //STOCK, DISPOSAL 유형

    @ApiModelProperty(value="외국어구분(KOR,ENG)")
    private String userLanguage;

    // BMS_IN 데이터 조회 데이터 조회용
    @ApiModelProperty(value="반입일자")
    @Builder.Default  private  String   carryinDate  = ""  ;
    @ApiModelProperty(value="반입시간")
    @Builder.Default  private  String   carryinTime  = ""  ;
    @ApiModelProperty(value="화물구분")
    @Builder.Default  private  String   cdType  = ""  ;
    @ApiModelProperty(value="거래구분")
    @Builder.Default  private  String   ecmType  = ""  ;
    @ApiModelProperty(value="용도구분")
    @Builder.Default  private  String   cdUseType  = ""  ;
    @ApiModelProperty(value="장치위치")
    @Builder.Default  private  Object   locationPlace  = ""  ;
    @ApiModelProperty(value="수출입구분")
    @Builder.Default  private  String   importExportClass  = ""  ;
    @ApiModelProperty(value="선기명")
    @Builder.Default  private  String   vesselFlight  = ""  ;
    @ApiModelProperty(value="항차")
    @Builder.Default  private  String   voyage  = ""  ;
    @ApiModelProperty(value="입항일자")
    @Builder.Default  private  String   arDate  = ""  ;
    @ApiModelProperty(value="입항시간")
    @Builder.Default  private  String   arTime  = ""  ;
    @ApiModelProperty(value="영문품명")
    @Builder.Default  private  String   commodityItemLoc  = ""  ;
    @ApiModelProperty(value="비고1")
    @Builder.Default  private  String   remarks1  = ""  ;
    @ApiModelProperty(value="비고2")
    @Builder.Default  private  String   remarks2  = ""  ;
    @ApiModelProperty(value="비고3")
    @Builder.Default  private  String   remarks3  = ""  ;
    @ApiModelProperty(value="참조번호")
    @Builder.Default  private  String   referenceNo  = ""  ;

    @ApiModelProperty(value="입력자명")
    private  String   registerUserName;
    @ApiModelProperty(value="수정자명")
    private  String   updateUserName;

    @ApiModelProperty(value="반출신고번호(OUT)")
    private  String   customsReportNo;
 
    @ApiModelProperty(value="총건수")
    private int totalCnt;
    @ApiModelProperty(value="목록건수")
    private int totalSimpleCnt;
    @ApiModelProperty(value="일반건수")
    private int totalGeneralCnt; 

    @ApiModelProperty(value = "반입구분(그리드공통코드매핑 빈값)")
    @Builder.Default
    private String inClass = "";

    @ApiModelProperty(value="파일공유여부")
    private String fileShare;
    @ApiModelProperty(value="오더관리번호")
    private String odJobNo;

    //시스템 로그
    @ApiModelProperty(value="처리자")
    private String logUserName;
    @ApiModelProperty(value="처리자(영)")
    private String logUserNameEng;
    @ApiModelProperty(value="출력용 사업장명")
    private String logUserBusinessPrintName;
    @ApiModelProperty(value="도입사코드", example="YHTXX")
    private String entCode;
    @ApiModelProperty(value="대표사업장코드", example="YHTXX")
    private String entBusinessCode;

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
    
    @ApiModelProperty(value="pod용 pod이름")
    private String podName;
    
    @ApiModelProperty(value="pod용 비교")
    private String podRemarks;
    
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

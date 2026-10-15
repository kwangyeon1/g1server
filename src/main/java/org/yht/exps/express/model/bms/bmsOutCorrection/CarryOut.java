package org.yht.exps.express.model.bms.bmsOutCorrection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import java.util.Date;

@Data
@Builder
@ApiModel(value = "CarryOut", description = "반출")
@Alias("carryOut")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class CarryOut extends ResultList {

    /** 반출 그리드 카운트*/
    @ApiModelProperty(value="총건수")
    private int cnt;
    @ApiModelProperty(value="목록건수")
    private int simpleCnt;
    @ApiModelProperty(value="일반건수")
    private int generalCnt;
    @ApiModelProperty(value="총건수")
    private int totalCnt;
    @ApiModelProperty(value="목록건수")
    private int totalSimpleCnt;
    @ApiModelProperty(value="일반건수")
    private int totalGeneralCnt;
    @ApiModelProperty(value="번호")
    private int num;
    @ApiModelProperty(value="파일공유여부")
    private String fileShare;

    /** 반출 */
    @ApiModelProperty(value="반출번호")
    private Integer outNo;
    @ApiModelProperty(value="반출신고번호")
    private String customsReportNo;
    @ApiModelProperty(value="현재진행상태일시")
    private String lastPodDate;
    @ApiModelProperty(value="현재진행상태코드")
    private String lastPodStatus;
    @ApiModelProperty(value="접수일시")
    private String lastPodRaqDate;
    @ApiModelProperty(value="반출승인일자")
    private String approvalDate;
    @ApiModelProperty(value="반출구분")
    private String outClass;
    @ApiModelProperty(value="반출근거번호")
    private String outApprovalNo;
    @ApiModelProperty(value="오더관리번호")
    private String odJobNo;
    @ApiModelProperty(value="반입번호")
    private Integer inNo;
    @ApiModelProperty(value="반출승인통보번호")
    private Integer outPlanNo;
    @ApiModelProperty(value="화물관리번호")
    private String cargoReferenceNo;
    @ApiModelProperty(value="화물관리번호")
    @Builder.Default
    private String cargoReferenceNoDisp = "";
    @ApiModelProperty(value="BL번호")
    private String blNo;
    @ApiModelProperty(value="MBL번호")
    private String mblNo;
    @ApiModelProperty(value="DO번호")
    private String doNo;
    @ApiModelProperty(value="DO일자")
    private String doDate;
    @ApiModelProperty(value="세관코드")
    private String customsCode;
    @ApiModelProperty(value="세관과코드")
    private String customsDptCode;
    @ApiModelProperty(value="화주코드")
    private String customerCode;
    @ApiModelProperty(value="화주명")
    private Object customerName;
    @ApiModelProperty(value="관세사코드")
    private String brokerCode;
    @ApiModelProperty(value="관세사명")
    private Object brokerName;
    @ApiModelProperty(value="포장종류")
    private String pkgType;
    @ApiModelProperty(value="승인수량")
    private Integer approvalCt;
    @ApiModelProperty(value="승인중량")
    private Double approvalWt;
    @ApiModelProperty(value="승인용적")
    private Double approvalCbm;
    @ApiModelProperty(value="반출수량")
    private Integer outCt;
    @ApiModelProperty(value="반출중량")
    private Double outWt;
    @ApiModelProperty(value="반출용적")
    private Double outCbm;
    @ApiModelProperty(value="반출일자")
    private String carryoutDate;
    @ApiModelProperty(value="반출시간")
    private String carryoutTime;
    @ApiModelProperty(value="관세")
    private Object approvalDuty;
    @ApiModelProperty(value="감정가액")
    private Object approvalAppraiseValue;
    @ApiModelProperty(value="결제방법")
    private String insPaymentType;
    @ApiModelProperty(value="결제통화")
    private String currencyCode;
    @ApiModelProperty(value="적하보험료납부")
    private String insPaymentYn;
    @ApiModelProperty(value="검사대상여부")
    private String inspectYn;
    @ApiModelProperty(value="HS코드")
    private String hsCode;
    @ApiModelProperty(value="수입요건법령코드")
    private String criteriaCode;
    @ApiModelProperty(value="반입반출간격")
    private Object inoutDateDiff;
    @ApiModelProperty(value="비고1")
    private Object remarks1;
    @ApiModelProperty(value="비고2")
    private Object remarks2;
    @ApiModelProperty(value="비고3")
    private Object remarks3;
    @ApiModelProperty(value="참조번호")
    private Object referenceNo;
    @ApiModelProperty(value="입력자코드")
    private String registerUserId;
    @ApiModelProperty(value="입력자이름")
    private String registerUserName;
    @ApiModelProperty(value="입력일시")
    private String registerDateTime;
    @ApiModelProperty(value="수정자코드")
    private String updateUserId;
    @ApiModelProperty(value="수정자이름")
    private String updateUserName;
    @ApiModelProperty(value="수정일시")
    private String updateDateTime;

    /*화물관련정보*/
    @ApiModelProperty(value="물품가격")
    @Builder.Default
    private Double amountUsd = 0.0;
    @ApiModelProperty(value="화물구분(통관구분)")
    @Builder.Default
    private String cdType = "";
    @ApiModelProperty(value="거래구분")
    @Builder.Default
    private String ecmType = "";
    @ApiModelProperty(value="용도구분")
    @Builder.Default
    private String cdUseType = "";
    @ApiModelProperty(value="장치위치")
    private String locationPlace;
    @ApiModelProperty(value="수출입구분(AIR&SEA구분)")
    @Builder.Default
    private String importExportClass = "";
    @ApiModelProperty(value="선기명")
    @Builder.Default
    private String vesselFlight = "";
    @ApiModelProperty(value="항차")
    private String voyage;
    @ApiModelProperty(value="입항일자")
    private String arDate;
    @ApiModelProperty(value="입항시간")
    private String arTime;
    @ApiModelProperty(value="반입수량")
    private Integer ct;
    @ApiModelProperty(value="반입중량")
    private Double wt;
    @ApiModelProperty(value="반입용적")
    private Double cbm;
    @ApiModelProperty(value="영문품명")
    @Builder.Default
    private String commodityItemLoc = "";
    @ApiModelProperty(value="XRAY호기")
    @Builder.Default
    private String xrayLineNo = "";

    /** 반입 */
    @ApiModelProperty(value="운송기한")
    @Builder.Default
    private String transPeriodDate = "";
    @ApiModelProperty(value="반출통고일자")
    @Builder.Default
    private String outNoticeDate = "";
    @ApiModelProperty(value="체화일자")
    @Builder.Default
    private String periodOverDate ="";
    @ApiModelProperty(value="포더코드")
    @Builder.Default
    private String fwdCode = "";
    @ApiModelProperty(value="포더명")
    @Builder.Default
    private String fwdName = "";
    @ApiModelProperty(value="수하인코드")
    @Builder.Default
    private String consigneeCode = "";
    @ApiModelProperty(value="수하인상호")
    @Builder.Default
    private String consigneeName = "";
    @ApiModelProperty(value="수하인기본주소")
    @Builder.Default
    private String consigneeAddress1 = "";
    @ApiModelProperty(value="수하인상세주소")
    @Builder.Default
    private String consigneeAddress2 = "";
    @ApiModelProperty(value="수하인사업자")
    private String consigneeBusinessId;
    @ApiModelProperty(value="수하인전화번호")
    private String consigneeTel;
    @ApiModelProperty(value="통지처코드")
    @Builder.Default
    private String notifyCode = "";
    @ApiModelProperty(value="통지처상호")
    @Builder.Default
    private String notifyName = "";
    @ApiModelProperty(value="통지처기본주소")
    @Builder.Default
    private String notifyAddress1 = "";
    @ApiModelProperty(value="통지처상세주소")
    @Builder.Default
    private String notifyAddress2 = "";
    @ApiModelProperty(value="통지처전화번호")
    @Builder.Default
    private String notifyTel = "";
    @ApiModelProperty(value="배송사코드")
    @Builder.Default
    private String deliveryCode = "";
    @ApiModelProperty(value="배송사상호")
    @Builder.Default
    private String deliveryName = "";
    @ApiModelProperty(value="송하인코드")
    @Builder.Default
    private String shipperCode = "";
    @ApiModelProperty(value="송하인상호")
    @Builder.Default
    private String shipperName ="";
    @ApiModelProperty(value="송하인기본주소")
    @Builder.Default
    private String shipperAddress1 = "";
    @ApiModelProperty(value="송하인상세주소")
    @Builder.Default
    private String shipperAddress2 = "";
    @ApiModelProperty(value="송하인전화번호")
    @Builder.Default
    private String shipperTel = "";
    @ApiModelProperty(value="선사부호")
    private String carrierCode;
    @ApiModelProperty(value="특수화물코드1")
    private String specialCode1;
    @ApiModelProperty(value="특수화물코드2")
    private String specialCode2;
    @ApiModelProperty(value="특수화물코드3")
    private String specialCode3;
    @ApiModelProperty(value="컨테이너번호")
    @Builder.Default
    private String cntrNo = "";
    @ApiModelProperty(value="발송국가")
    @Builder.Default
    private String origCountry = "";

//    /**
//     * 11-4-4 표시 그리드에서 사용
//     * @return
//     */
//    @ApiModelProperty(value="화물관리번호_표시")
//    public String getCargoReferenceNoDisp() {
//        String ret = getCargoReferenceNoDisp(this.cargoReferenceNo);
//        return ret;
//    }
//    //carryinDate, carryinTime  => inoutDateDiff 이값 계산용으로 처리
//    @ApiModelProperty(value="반입일자")
//    @Builder.Default  private  String   carryinDate  = ""  ;
//    @ApiModelProperty(value="반입시간")
//    @Builder.Default  private  String   carryinTime  = ""  ;
//
//
//    @ApiModelProperty(value = "반입구분(그리드공통코드매핑 빈값)")
//    @Builder.Default
//    private String inClass = "";
//
//    private String getCargoReferenceNoDisp(String _cargoReferenceNo) {
//        String ret ="";
//        if ( ! _cargoReferenceNo.equals("") ){
//            if (_cargoReferenceNo.length() > 10) {
//                if (_cargoReferenceNo.length() == 19) {
//                    ret = _cargoReferenceNo.substring(0, 11) + "-" + _cargoReferenceNo.substring(11, 15) + "-" + _cargoReferenceNo.substring(15, 19);
//                }else if (_cargoReferenceNo.length() == 18) { // 마이그레이션 값은 18이다
//                    ret = _cargoReferenceNo.substring(0, 11) + "-" + _cargoReferenceNo.substring(11, 15) + "-" + _cargoReferenceNo.substring(15);
//                } else {
//                    ret = _cargoReferenceNo.substring(0, 11) + "-" + _cargoReferenceNo.substring(11);
//                }
//            }else {
//                ret = _cargoReferenceNo;
//            }
//        }
//        return ret;
//    }
}

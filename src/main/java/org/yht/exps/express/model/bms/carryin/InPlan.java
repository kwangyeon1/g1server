package org.yht.exps.express.model.bms.carryin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import java.util.List;

@Data
@Builder
@ApiModel(value = "InPlan", description = "반입예정 리스트")
@Alias("inPlan")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class InPlan extends ResultList{

    @ApiModelProperty(value = "반입예정 컨테이너 리스트")
    private List<Container> containerList;

    @ApiModelProperty(value="사업장코드", example="YHTXX-KR")
    private String entBusinessCode;

    @ApiModelProperty(value="gmtTime_기본값 ")
    @Builder.Default
    private String gmtTime ="9";

    @ApiModelProperty(value="날짜형식_현재 날짜 입력시 DATEADD(HOUR, 9, GETDATE()) ")
    @Builder.Default
    private String dateFormat ="23";

    /** 반입예정 그리드 카운트*/
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
    /*@ApiModelProperty(value="번호")
    private int num;*/
    @ApiModelProperty(value="파일공유여부")
    private String fileShare;

    /** 반입예정 */
    @ApiModelProperty(value="반입상태", example="PRE_CARRY_IN(반입예정) or CARRY_IN(반입) or STOCK(재고)")
    private String carryInState;
    @ApiModelProperty(value="반입예정번호")
    private Integer inPlanNo;
    @ApiModelProperty(value="보세운송관세청통보일시")
    private String notifyDateTime;
    @ApiModelProperty(value="반입근거번호")
    private String inApprovalNo;
    @ApiModelProperty(value="운송기한")
    private String transPeriodDate;
    @ApiModelProperty(value="발생지보세구역")
    private String departureLocation;
    @ApiModelProperty(value="최종보세구역")
    private String lastLocation;
    @ApiModelProperty(value="신청인전화번호")
    private String reportEmployeeTel;
    @ApiModelProperty(value="오더관리번호")
    private String odJobNo;
    @ApiModelProperty(value="화물관리번호")
    @Builder.Default
    private String cargoReferenceNo = "";
    @ApiModelProperty(value="화물관리번호")
    @Builder.Default
    private String cargoReferenceNoDisp = "";
    @ApiModelProperty(value="BL번호")
    private String blNo;
    @ApiModelProperty(value="MBL번호")
    private String mblNo;
    @ApiModelProperty(value="검사대상여부")
    private String inspectYn;
    @ApiModelProperty(value="반입후검사사전통보대상여부")
    private String noticeInspectYn;
    @ApiModelProperty(value="포장종류")
    private String pkgType;
    @ApiModelProperty(value="예정수량")
    private Integer exCt;
    @ApiModelProperty(value="예정중량")
    private Double exWt;
    @ApiModelProperty(value="예정용적")
    private Double exCbm;
    @ApiModelProperty(value="세관코드")
    private String customsCode;
    @ApiModelProperty(value="세관과")
    private String customsDptCode;
    @ApiModelProperty(value="선기명")
    private String vesselFlight;
    @ApiModelProperty(value="항차")
    private String voyage;
    @ApiModelProperty(value="입항일자")
    private String arDate;
    @ApiModelProperty(value="입항시간")
    private String arTime;
    @ApiModelProperty(value="영문품명")
    private String commodityItemLoc;
    @ApiModelProperty(value="송하인코드")
    private String shipperCode;
    @ApiModelProperty(value="송하인상호")
    private String shipperName;
    @ApiModelProperty(value="수하인기본주소")
    private String shipperAddress1;
    @ApiModelProperty(value="수하인상세주소")
    private String shipperAddress2;
    @ApiModelProperty(value="송하인전화번호")
    private String shipperTel;
    @ApiModelProperty(value="수하인코드")
    private String consigneeCode;
    @ApiModelProperty(value="수하인상호")
    private String consigneeName;
    @ApiModelProperty(value="수하인기본주소")
    private String consigneeAddress1;
    @ApiModelProperty(value="수하인상세주소")
    private String consigneeAddress2;
    @ApiModelProperty(value="수하인사업자")
    private String consigneeBusinessId;
    @ApiModelProperty(value="수하인전화번호")
    private String consigneeTel;
    @ApiModelProperty(value="통지처코드")
    private String notifyCode;
    @ApiModelProperty(value="통지처상호")
    private String notifyName;
    @ApiModelProperty(value="통지처기본주소")
    private String notifyAddress1;
    @ApiModelProperty(value="통지처상세주소")
    private String notifyAddress2;
    @ApiModelProperty(value="통지처전화번호")
    private String notifyTel;
    @ApiModelProperty(value="특수화물코드1")
    private String specialCode1;
    @ApiModelProperty(value="특수화물코드2")
    private String specialCode2;
    @ApiModelProperty(value="특수화물코드3")
    private String specialCode3;
    @ApiModelProperty(value="선사부호")
    private String carrierCode;
    @ApiModelProperty(value="컨테이너번호")
    private String cntrNo;
    @ApiModelProperty(value="발송국가")
    private String origCountry;
    @ApiModelProperty(value="선별결과코드")
    private String seInspectionCode;
    @ApiModelProperty(value="목록신고_결과통보_세관담당자명")
    private String simpleAuthName;
    @ApiModelProperty(value="검역구분코드")
    private String inspectionType;
    @ApiModelProperty(value="검역구분코드2")
    private String inspectionTypeA;
    @ApiModelProperty(value="검역구분코드3")
    private String inspectionTypeB;
    @ApiModelProperty(value="통관검사계획코드")
    private String paln5emInspectionCode;
    @ApiModelProperty(value="화물구분")
    private String cdType;
    @ApiModelProperty(value="거래구분")
    private String ecmType;
    @ApiModelProperty(value="전자상거래 사용여부")
    private String ecmYn;
    @ApiModelProperty(value="용도구분")
    private String cdUseType;
    @ApiModelProperty(value="거대화물여부")
    private String bigSizeYn;
    @ApiModelProperty(value="물품가격")
    private Double amountUsd;
    @ApiModelProperty(value="입고SCAN일시")
    private String inScanDateTime;
    @ApiModelProperty(value="참조번호")
    private String referenceNo;
    @ApiModelProperty(value="반입등록여부")
    private String inYn;
    @ApiModelProperty(value="입력자코드")
    private String registerUserId;
    @ApiModelProperty(value="입력자이름")
    @Builder.Default
    private String registerUserName = "";
    @ApiModelProperty(value="입력일시")
    private String registerDateTime;
    @ApiModelProperty(value="수정자코드")
    private String updateUserId;
    @ApiModelProperty(value="수정자이름")
    @Builder.Default
    private String updateUserName = "";
    @ApiModelProperty(value="수정일시")
    private String updateDateTime;
    @ApiModelProperty(value="진행상태")
    private String lastPodStatus;
    @ApiModelProperty(value="배송사코드")
    @Builder.Default
    private String deliveryCode = "";
    @ApiModelProperty(value="배송사상호")
    @Builder.Default
    private String deliveryName = "";
    @ApiModelProperty(value="우범화물여부")
    private String crimeSelectionYn;
    @ApiModelProperty(value="지재권")
    private String intellectualPropYn;
    @ApiModelProperty(value="집중검사")
    private String inspectionCustomsYn;
    @ApiModelProperty(value="박스총합")
    private int packingCtSum;
    @ApiModelProperty(value="박스입고상태")
    private String boxInStatus;
    @ApiModelProperty(value="MGN대상여부")
    private String mgnIssueYn;

    //반입정보
    @ApiModelProperty(value="반입번호")
    private Integer inNo;
    @ApiModelProperty(value="장치위치")
    private String locationPlace;
    @ApiModelProperty(value="비고1")
    private String remarks1;
    @ApiModelProperty(value="비고2")
    private String remarks2;
    @ApiModelProperty(value="비고3")
    private String remarks3;
    
    
    //홀드정보
    @ApiModelProperty(value="홀드여부")
    @Builder.Default
    private String holdYn = "N";
    @ApiModelProperty(value="홀드코드")
    private String holdCode;
    @ApiModelProperty(value="홀드사유")
    private String holdReason;
    @ApiModelProperty(value="홀드처리여부")
    private String holdReleaseYn;
    @ApiModelProperty(value="홀드처리담당자코드")
    private String holdReleaseAttn;
    @ApiModelProperty(value="홀드처리담당자이름")
    private String holdReleaseAttnName;
    @ApiModelProperty(value="홀드처리일자")
    private String holdReleaseDate;

    //XRAY정보
    @ApiModelProperty(value="XRAY판독구분코드")
    private String xrayInspectionCode;
    @ApiModelProperty(value="XRAY호기")
    private String xrayLineNo;
    @ApiModelProperty(value="XRAY판독수량")
    private Integer xrayScanCt;
    @ApiModelProperty(value="XRAY이미지수량")
    private Integer xrayImageCt;
    @ApiModelProperty(value="재전송여부")
    private String resendYn;
    
    @ApiModelProperty(value="반입구분")
    private String inClass;
    
    public static InPlan initializeInPlan() {
        InPlan inPlan = new InPlan();

        // 빈 문자열로 초기화
        inPlan.setEntBusinessCode("");
        inPlan.setGmtTime("");
        inPlan.setDateFormat("");
        inPlan.setFileShare("");
        inPlan.setCarryInState("");
        inPlan.setNotifyDateTime("");
        inPlan.setInApprovalNo("");
        inPlan.setTransPeriodDate("");
        inPlan.setDepartureLocation("");
        inPlan.setLastLocation("");
        inPlan.setReportEmployeeTel("");
        inPlan.setOdJobNo("");
        inPlan.setCargoReferenceNo("");
        inPlan.setCargoReferenceNoDisp("");
        inPlan.setBlNo("");
        inPlan.setMblNo("");
        inPlan.setInspectYn("");
        inPlan.setNoticeInspectYn("");
        inPlan.setPkgType("");
        inPlan.setCustomsCode("");
        inPlan.setCustomsDptCode("");
        inPlan.setVesselFlight("");
        inPlan.setVoyage("");
        inPlan.setArDate("");
        inPlan.setArTime("");
        inPlan.setCommodityItemLoc("");
        inPlan.setShipperCode("");
        inPlan.setShipperName("");
        inPlan.setShipperAddress1("");
        inPlan.setShipperAddress2("");
        inPlan.setShipperTel("");
        inPlan.setConsigneeCode("");
        inPlan.setConsigneeName("");
        inPlan.setConsigneeAddress1("");
        inPlan.setConsigneeAddress2("");
        inPlan.setConsigneeBusinessId("");
        inPlan.setConsigneeTel("");
        inPlan.setNotifyCode("");
        inPlan.setNotifyName("");
        inPlan.setNotifyAddress1("");
        inPlan.setNotifyAddress2("");
        inPlan.setNotifyTel("");
        inPlan.setSpecialCode1("");
        inPlan.setSpecialCode2("");
        inPlan.setSpecialCode3("");
        inPlan.setCarrierCode("");
        inPlan.setCntrNo("");
        inPlan.setOrigCountry("");
        inPlan.setSeInspectionCode("");
        inPlan.setSimpleAuthName("");
        inPlan.setInspectionType("");
        inPlan.setInspectionTypeA("");
        inPlan.setInspectionTypeB("");
        inPlan.setPaln5emInspectionCode("");
        inPlan.setCdType("");
        inPlan.setEcmType("");
        inPlan.setCdUseType("");
        inPlan.setBigSizeYn("");
        inPlan.setInScanDateTime("");
        inPlan.setReferenceNo("");
        inPlan.setInYn("");
        inPlan.setRegisterUserId("");
        inPlan.setRegisterUserName("");
        inPlan.setRegisterDateTime("");
        inPlan.setUpdateUserId("");
        inPlan.setUpdateUserName("");
        inPlan.setUpdateDateTime("");
        inPlan.setLastPodStatus("");
        inPlan.setDeliveryCode("");
        inPlan.setDeliveryName("");
        inPlan.setMgnIssueYn("");
        inPlan.setHoldCode("");
        inPlan.setHoldReason("");
        inPlan.setHoldReleaseYn("");
        inPlan.setHoldReleaseAttn("");
        inPlan.setHoldReleaseAttnName("");
        inPlan.setHoldReleaseDate("");
        inPlan.setXrayInspectionCode("");
        inPlan.setXrayLineNo("");
        inPlan.setResendYn("");
        inPlan.setInClass("");
        
        // 정수 및 더블 필드는 기본값으로 초기화
        inPlan.setCnt(0);
        inPlan.setSimpleCnt(0);
        inPlan.setGeneralCnt(0);
        inPlan.setTotalCnt(0);
        inPlan.setTotalSimpleCnt(0);
        inPlan.setTotalGeneralCnt(0);
        inPlan.setExCt(0);
        inPlan.setExWt(0.0);
        inPlan.setExCbm(0.0);
        inPlan.setXrayScanCt(0);
        inPlan.setXrayImageCt(0);

        return inPlan;
    }
//    /**
//     * 화물관리번호 표시형태 변환
//     * @return
//     */
//    @ApiModelProperty(value="화물관리번호_표시")
//    public String getCargoReferenceNo() {
//        String ret ="";
//        if ( ! cargoReferenceNo.equals("") ){
//            if (cargoReferenceNo.length() > 10) {
//                if (cargoReferenceNo.length() == 19) {
//                    ret = cargoReferenceNo.substring(0, 11) + "-" + cargoReferenceNo.substring(11, 15) + "-" + cargoReferenceNo.substring(15, 19);
//                }else if (cargoReferenceNo.length() == 18) { // 마이그레이션 값은 18이다
//                    ret = cargoReferenceNo.substring(0, 11) + "-" + cargoReferenceNo.substring(11, 15) + "-" + cargoReferenceNo.substring(15);
//                } else {
//                    ret = cargoReferenceNo.substring(0, 11) + "-" + cargoReferenceNo.substring(11);
//                }
//            }else {
//                ret  = cargoReferenceNo;
//            }
//        }
//        return ret;
//    }
}

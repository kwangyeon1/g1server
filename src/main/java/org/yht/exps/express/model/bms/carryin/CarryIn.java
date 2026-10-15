package org.yht.exps.express.model.bms.carryin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import java.util.List;
import java.util.Map;

@Data
@Builder
@ApiModel(value = "CarryIn", description = "반입")
@Alias("carryIn")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class CarryIn extends ResultList{

    @ApiModelProperty(value = "반입컨테이너 리스트")
    private List<Container> containerList;

    @ApiModelProperty(value="조회 그리드에서 가져온 리스트")
    private List<Map<String, Object>> dataList;

    @ApiModelProperty(value="사업장 보세 구역번호 8자리")
    private String bmsWhCode;

    @ApiModelProperty(value = "작업모드")
    private String workMode;

    @ApiModelProperty(value = "등록모드", example="M(매뉴얼 - 70000001) OR A(자동 - 00000001)")
    private String insertMode;

    @ApiModelProperty(value="데이터행 갯수")
    private Integer rowCount;

    @ApiModelProperty(value="반입상태", example="PRE_CARRY_IN(반입예정) or CARRY_IN(반입) or STOCK(재고)")
    private String carryInState;

    @ApiModelProperty(value="도입사코드", example="YHTXX")
    private String entCode;

    @ApiModelProperty(value="사업장코드", example="YHTXX-KR")
    private String entBusinessCode;

    @ApiModelProperty(value="gmtTime_기본값 ")
    @Builder.Default
    private String gmtTime ="9";

    @ApiModelProperty(value="날짜형식_현재 날짜 입력시 DATEADD(HOUR, 9, GETDATE()) ")
    @Builder.Default
    private String dateFormat ="23";

    @ApiModelProperty(value = "체화일자 구하는 기준값(월)")
    private int divideValueData;


    /** 반입 그리드 카운트*/
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
    
    @ApiModelProperty(value="반입신고한 건수")
    private int totalTry632Cnt;
    @ApiModelProperty(value="접수 반입신고 건수")
    private int totalSuccess632Cnt;
    @ApiModelProperty(value="오류 반입신고 건수")
    private int totalError632Cnt;
    @ApiModelProperty(value="속성신고한 건수")
    private int totalTry5tlCnt;
    @ApiModelProperty(value="접수 속성신고 건수")
    private int totalSuccess5tlCnt;
    @ApiModelProperty(value="오류 속성신고 건수")
    private int totalError5tlCnt;
    /*@ApiModelProperty(value="번호")
    private int num;*/
    @ApiModelProperty(value="파일공유여부")
    private String fileShare;

    /** 반입 */
    @ApiModelProperty(value="반입번호")
    private Integer inNo;
    @ApiModelProperty(value="반입신고번호")
    private String customsReportNo;
    @ApiModelProperty(value="신고일자")
    private String lastPodDate;
    @ApiModelProperty(value="신고상태")
    @Builder.Default
    private String lastPodStatus = "BMS01";
    @ApiModelProperty(value="속성신고상태")
    @Builder.Default
    private String lastPod5tlStatus = "";
    @ApiModelProperty(value="접수일시")
    private String lastPodRaqDate;
    @ApiModelProperty(value="5TL접수일시")
    private String lastPodR48Date;
    @ApiModelProperty(value="반입구분")
    private String inClass;

    @ApiModelProperty(value="반입근거번호")
    @Builder.Default private String inApprovalNo = "";

    @ApiModelProperty(value="운송기한")
    @Builder.Default
    private String transPeriodDate = "";
    @ApiModelProperty(value="오더관리번호")
    @Builder.Default
    private String odJobNo = "";
    @ApiModelProperty(value="반입예정번호")
    @Builder.Default
    private Integer inPlanNo = 0;
    @ApiModelProperty(value="원반입예정번호")
    @Builder.Default
    private Integer origInPlanNo = 0;
    @ApiModelProperty(value="화물관리번호")
    @Builder.Default
    private String cargoReferenceNo = "";
    @ApiModelProperty(value="화물관리번호")
    @Builder.Default
    private String cargoReferenceNoDisp = "";
    @ApiModelProperty(value="BL번호")
    private String blNo;
    @ApiModelProperty(value="MBL번호")
    @Builder.Default
    private String mblNo = "";
    @ApiModelProperty(value="세관코드")
    private String customsCode;
    @ApiModelProperty(value="세관과")
    private String customsDptCode;
    @ApiModelProperty(value="검사대상여부")
    private String inspectYn;
    @ApiModelProperty(value="반입후검사사전통보대상여부")
    private String noticeInspectYn;
    @ApiModelProperty(value="장치위치")
    private String locationPlace;
    @ApiModelProperty(value="최초로케이션")
    private String origLocationPlace;
    
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
    @ApiModelProperty(value="영문품명")
    @Builder.Default
    private String commodityItemLoc = "";
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
    @ApiModelProperty(value="특수화물코드1")
    private String specialCode1;
    @ApiModelProperty(value="특수화물코드2")
    private String specialCode2;
    @ApiModelProperty(value="특수화물코드3")
    private String specialCode3;
    @ApiModelProperty(value="선사부호")
    private String carrierCode;
    @ApiModelProperty(value="발송국가")
    @Builder.Default
    private String origCountry = "";
    @ApiModelProperty(value="선별결과코드(목록선별)")
    @Builder.Default
    private String seInspectionCode = "";
    @ApiModelProperty(value="목록신고_결과통보_세관담당자명")
    private String simpleAuthName;
    @ApiModelProperty(value="검역구분코드")
    @Builder.Default
    private String inspectionType = "";
    @ApiModelProperty(value="검역구분코드2")
    @Builder.Default
    private String inspectionTypeA = "";
    @ApiModelProperty(value="검역구분코드3")
    @Builder.Default
    private String inspectionTypeB = "";
    @ApiModelProperty(value="XRAY판독구분코드")
    @Builder.Default
    private String xrayInspectionCode = "";
    @ApiModelProperty(value="XRAY호기")
    @Builder.Default
    private String xrayLineNo = "";
    @ApiModelProperty(value="XRAY검사형태")
    @Builder.Default
    private String xrayCheck ="";
    @ApiModelProperty(value="통관검사계획코드")
    @Builder.Default
    private String paln5emInspectionCode = "";
    @ApiModelProperty(value="화물구분(통관구분)")
    @Builder.Default
    private String cdType = "";
    @ApiModelProperty(value="거래구분")
    @Builder.Default
    private String ecmType = "";
    @ApiModelProperty(value="용도구분")
    @Builder.Default
    private String cdUseType = "";
    @ApiModelProperty(value="거대화물여부")
    private String bigSizeYn;
    @ApiModelProperty(value="판독자ID")
    private String inspectorId;

    @ApiModelProperty(value="물품가격")
    @Builder.Default
    private Double amountUsd = 0.0;
    @ApiModelProperty(value="XRAY판독수량")
    @Builder.Default
    private Integer xrayScanCt = 0;
    @ApiModelProperty(value="XRAY이미지수량")
    @Builder.Default
    private Integer xrayImageCt = 0;
    @ApiModelProperty(value="반입일자")
    private String carryinDate;

    @ApiModelProperty(value="반입시간")
    @Builder.Default
    private String carryinTime = "";

    @ApiModelProperty(value="반출통고일자")
    @Builder.Default
    private String outNoticeDate = "";
    @ApiModelProperty(value="체화일자")
    @Builder.Default
    private String periodOverDate ="";
    @ApiModelProperty(value="화주코드")
    @Builder.Default
    private String customerCode = "";
    @ApiModelProperty(value="화주명")
    @Builder.Default
    private String customerName = "";
    @ApiModelProperty(value="포더코드")
    @Builder.Default
    private String fwdCode = "";
    @ApiModelProperty(value="포더명")
    @Builder.Default
    private String fwdName = "";
    @ApiModelProperty(value="포장종류")
    private String pkgType;
    @ApiModelProperty(value="예정수량")
    private Integer exCt;
    @ApiModelProperty(value="예정중량")
    private Double exWt;
    @ApiModelProperty(value="예정용적")
    private Double exCbm;
    @ApiModelProperty(value="반입수량")
    private Integer ct;
    @ApiModelProperty(value="반입중량")
    private Double wt;
    @ApiModelProperty(value="반입용적")
    private Double cbm;
    @ApiModelProperty(value="사고구분")
    private String accidentClass;
    @ApiModelProperty(value="사고수량")
    private Integer accidentCt;
    @ApiModelProperty(value="사고중량")
    private Double accidentWt;
    @ApiModelProperty(value="사고용적")
    private Double accidentCbm;
    @ApiModelProperty(value="재고수량")
    private Integer stockCt;
    @ApiModelProperty(value="재고중량")
    private Double stockWt;
    @ApiModelProperty(value="재고용적")
    private Double stockCbm;
    @ApiModelProperty(value="반출일자")
    @Builder.Default
    private String carryoutDate = "";
    @ApiModelProperty(value="반출시간")
    @Builder.Default
    private String carryoutTime = "";
    @ApiModelProperty(value="비고1")
    @Builder.Default
    private String remarks1 = "";
    @ApiModelProperty(value="비고2")
    @Builder.Default
    private String remarks2 = "";
    @ApiModelProperty(value="비고3")
    @Builder.Default
    private String remarks3 = "";
    @ApiModelProperty(value="참조번호")
    @Builder.Default
    private String referenceNo ="";
    @ApiModelProperty(value="MGN대상여부")
    @Builder.Default
    private String mgnIssueYn = "N";
    @ApiModelProperty(value="현도여부")
    private String assignYn;
    @ApiModelProperty(value="우범화물여부")
    private String crimeSelectionYn;
    @ApiModelProperty(value="지재권")
    private String intellectualPropYn;
    @ApiModelProperty(value="집중검사")
    private String inspectionCustomsYn;
    @ApiModelProperty(value="박스총합")
    private int packingCtSum;
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
    @ApiModelProperty(value="반출관리번호")
    private Integer outNo;

    @ApiModelProperty(value="배송사코드")
    @Builder.Default
    private String deliveryCode = "";
    @ApiModelProperty(value="배송사상호")
    @Builder.Default
    private String deliveryName = "";
    @ApiModelProperty(value = "폐기여부(최초N, 폐기 처리후 Y)")
    @Builder.Default
    private String disposalYn = "N";
    
    // TB_BMS_OUT의 정보
    @ApiModelProperty(value="다음 이고차수")
    private Integer nextTicOutSeq;
    @ApiModelProperty(value="반출예정번호")
    private Integer outPlanNo;   
    @ApiModelProperty(value="PREC 업데이트 여부")
    private Boolean isPrecUpdated = false;
    
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

    //컨테이너 정보
    @ApiModelProperty(value="컨테이너번호")
    @Builder.Default
    private String cntrNo = "";
    @ApiModelProperty(value="컨테이너번호1")
    @Builder.Default
    private String cntrNo1 = "";
    @ApiModelProperty(value="컨테이너번호2")
    @Builder.Default
    private String cntrNo2 = "";
    @ApiModelProperty(value="컨테이너번호3")
    @Builder.Default
    private String cntrNo3 = "";
    @ApiModelProperty(value="컨테이너SIZE1")
    @Builder.Default
    private String cntrEdiCode1 = "";
    @ApiModelProperty(value="컨테이너SIZE2")
    @Builder.Default
    private String cntrEdiCode2 = "";
    @ApiModelProperty(value="컨테이너SIZE3")
    @Builder.Default
    private String cntrEdiCode3 = "";
    @ApiModelProperty(value="봉인번호1_1")
    @Builder.Default
    private String sealNo1A = "";
    @ApiModelProperty(value="봉인번호1_2")
    @Builder.Default
    private String sealNo1B = "";
    @ApiModelProperty(value="봉인번호2_1")
    @Builder.Default
    private String sealNo2A = "";
    @ApiModelProperty(value="봉인번호2_2")
    @Builder.Default
    private String sealNo2B = "";
    @ApiModelProperty(value="봉인번호3_1")
    @Builder.Default
    private String sealNo3A = "";
    @ApiModelProperty(value="봉인번호3_2")
    @Builder.Default
    private String sealNo3B = "";

    //시스템 로그
    @ApiModelProperty(value="처리자")
    private String logUserName;
    @ApiModelProperty(value="처리자(영)")
    private String logUserNameEng;
    @ApiModelProperty(value="출력용 사업장명")
    private String logUserBusinessPrintName;
    @ApiModelProperty(value="PDA 작업구분")
    private String pdaYn;

    //TB_ECM_OD_ADDITIONAL
    @ApiModelProperty(value="목록취하여부")
    private String dropYn;

    /**
     * 화물관리번호 표시형태 변환
     * @return
     */
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
    
    @ApiModelProperty(value="진행상태변경여부")
    private String changeStatusYn;
    @ApiModelProperty(value="진행상태(전송 BMS03 ) 변경여부")
    private String changeStatus03Yn;
}

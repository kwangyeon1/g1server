package org.yht.exps.express.model.bms.statistics;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@Builder
@ApiModel(value = "BmsStatistics", description = "통계조회")
@Alias("bmsStatistics")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsStatistics extends ResultList {

    @ApiModelProperty(value="사업장코드", example="YHTXX-KR")
    private String entBusinessCode;
    @ApiModelProperty(value="gmtTime_기본값 ")
    
    private String gmtTime ="9";
    @ApiModelProperty(value="날짜형식_현재 날짜 입력시 DATEADD(HOUR, 9, GETDATE()) ")
    
    private String dateFormat ="23";

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
    @ApiModelProperty(value="번호")
    private int num;

    /** 반입 */
    @ApiModelProperty(value="반입번호")
    private Integer inNo;
    @ApiModelProperty(value="반입신고번호")
    private String customsReportNo;
    @ApiModelProperty(value="신고일자")
    private String lastPodDate;
    @ApiModelProperty(value="신고상태")
    private String lastPodStatus;
    @ApiModelProperty(value="속성신고상태")
    private String lastPod5tlStatus;
    @ApiModelProperty(value="접수일시")
    private String lastPodRaqDate;
    @ApiModelProperty(value="5TL접수일시")
    private String lastPodR48Date;
    @ApiModelProperty(value="반입구분")
    private String inClass;
    @ApiModelProperty(value="반입근거번호")
     private String inApprovalNo;
    @ApiModelProperty(value="운송기한")
    private String transPeriodDate;
    @ApiModelProperty(value="오더관리번호")
    private String odJobNo;
    @ApiModelProperty(value="반입예정번호")
    private Integer inPlanNo;
    @ApiModelProperty(value="화물관리번호")
    private String cargoReferenceNo;
    @ApiModelProperty(value="화물관리번호")
    private String cargoReferenceNoDisp;
    @ApiModelProperty(value="BL번호")
    private String blNo;
    @ApiModelProperty(value="MBL번호")
    private String mblNo;
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
    @ApiModelProperty(value="수출입구분(AIR&SEA구분)")
    private String importExportClass;
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
    @ApiModelProperty(value="송하인기본주소")
    private String shipperAddress1;
    @ApiModelProperty(value="송하인상세주소")
    private String shipperAddress2;
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
    @ApiModelProperty(value="발송국가")
    private String origCountry;
    @ApiModelProperty(value="선별결과코드(목록선별)")
    private String seInspectionCode;
    @ApiModelProperty(value="목록신고_결과통보_세관담당자명")
    private String simpleAuthName;
    @ApiModelProperty(value="검역구분코드")
    private String inspectionType;
    @ApiModelProperty(value="검역구분코드2")
    private String inspectionTypeA;
    @ApiModelProperty(value="검역구분코드3")
    private String inspectionTypeB;
    @ApiModelProperty(value="XRAY판독구분코드")
    private String xrayInspectionCode;
    @ApiModelProperty(value="XRAY호기")
    private String xrayLineNo;
    @ApiModelProperty(value="XRAY검사형태")
    private String xrayCheck;
    @ApiModelProperty(value="통관검사계획코드")
    private String paln5emInspectionCode;
    @ApiModelProperty(value="화물구분(통관구분)")
    private String cdType;
    @ApiModelProperty(value="거래구분")
    private String ecmType;
    @ApiModelProperty(value="용도구분")
    private String cdUseType;
    @ApiModelProperty(value="거대화물여부")
    private String bigSizeYn;
    @ApiModelProperty(value="물품가격")
    private Double amountUsd;
    @ApiModelProperty(value="XRAY판독수량")
    private Integer xrayScanCt;
    @ApiModelProperty(value="XRAY이미지수량")
    private Integer xrayImageCt;
    @ApiModelProperty(value="반입일자")
    private String carryinDate;
    @ApiModelProperty(value="반입시간")
    private String carryinTime;
    @ApiModelProperty(value="반출통고일자")
    private String outNoticeDate;
    @ApiModelProperty(value="체화일자")
    private String periodOverDate;
    @ApiModelProperty(value="화주코드")
    private String customerCode;
    @ApiModelProperty(value="화주명")
    private String customerName;
    @ApiModelProperty(value="포더코드")
    private String fwdCode;
    @ApiModelProperty(value="포더명")
    private String fwdName;
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
    private String carryoutDate;
    @ApiModelProperty(value="반출시간")
    private String carryoutTime;
    @ApiModelProperty(value="비고1")
    private String remarks1;
    @ApiModelProperty(value="비고2")
    private String remarks2;
    @ApiModelProperty(value="비고3")
    private String remarks3;
    @ApiModelProperty(value="참조번호")
    private String referenceNo;
    @ApiModelProperty(value="MGN대상여부")
    private String mgnIssueYn;
    @ApiModelProperty(value="MFN대상여부")
    private String mfnIssueYn;
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
    @ApiModelProperty(value="배송사코드")
    private String deliveryCode;
    @ApiModelProperty(value="배송사상호")
    private String deliveryName;
    @ApiModelProperty(value = "폐기여부(최초N, 폐기 처리후 Y)")
    private String disposalYn;
    
    /** 집계(건수,수량,중량)*/
    @ApiModelProperty(value="반입건수합계")
    private int totalInCnt;
    @ApiModelProperty(value="반입수량합계")
    private int totalInSumCt;
    @ApiModelProperty(value="반입중량합계")
    private int totalInSumWt;
    @ApiModelProperty(value="반출건수합계")
    private int totalOutCnt;
    @ApiModelProperty(value="반출수량합계")
    private int totalOutSumCt;
    @ApiModelProperty(value="반출중량합계")
    private int totalOutSumWt;
    @ApiModelProperty(value="재고건수합계")
    private int totalStockCnt;
    @ApiModelProperty(value="재고수량합계")
    private int totalStockSumCt;
    @ApiModelProperty(value="재고중량합계")
    private int totalStockSumWt;

    /** 반출 */
    @ApiModelProperty(value="반출구분")
    private String outClass;
    @ApiModelProperty(value="관세사코드")
    private String brokerCode;
    @ApiModelProperty(value="관세사명")
    private String brokerName;
    @ApiModelProperty(value="반입비고1")
    private String carryinRemarks1;
    @ApiModelProperty(value="반입비고2")
    private String carryinRemarks2;
    @ApiModelProperty(value="반입비고3")
    private String carryinRemarks3;
    
    /** 반입시간(시간단위) */
    @ApiModelProperty(value="반입시간(시간단위)")
    private String carryinHour;
    
    /** 판독시간 */
    @ApiModelProperty(value="판독시간")
    private String xrayTime;
    @ApiModelProperty(value="판독건수")
    private int xrayCnt;
    @ApiModelProperty(value="판독원수")
    private int xrayIdCnt;
    @ApiModelProperty(value="판독자동건수")
    private int xrayAcnt;
    @ApiModelProperty(value="판독수동건수")
    private int xrayMcnt;

}

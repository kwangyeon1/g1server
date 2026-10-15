package org.yht.exps.express.model.bms.bmsAbnormal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@Builder
@ApiModel(value = "CarryInAbnormalHbl", description = "반입물품이상신고HBL")
@Alias("carryInAbnormalHbl")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class CarryInAbnormalHbl extends ResultList {

    @ApiModelProperty(value="사업장 보세 구역번호 8자리")
    private String bmsWhCode;

    @ApiModelProperty(value="gmtTime_기본값 ")
    @Builder.Default
    private String gmtTime ="9";

    @ApiModelProperty(value="날짜형식_현재 날짜 입력시 DATEADD(HOUR, 9, GETDATE()) ")
    @Builder.Default
    private String dateFormat ="23";

    /** 반입이상 그리드 카운트*/
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

    /** 반입물품이상신고*/
    @ApiModelProperty(value="반입이상관리번호")
    private Integer abnNo;
    @ApiModelProperty(value="반입이상제출번호")
    private String customsReportNo;
    @ApiModelProperty(value="적하목록관리번호(Manifest Reference Number)")
    private String mrn;
    @ApiModelProperty(value="Master B/L 일련번호(Mater B/L Sequence Number)")
    private String msn;
    @ApiModelProperty(value="MBL번호")
    private String mblNo;
    @ApiModelProperty(value="신고일자(송신일시)")
    private String lastPodDate;
    @ApiModelProperty(value="신고상태")
    private String lastPodStatus;
    @ApiModelProperty(value="접수일시(수신일시)")
    private String lastPodRaqDate;
    @ApiModelProperty(value="선기명")
    private String vesselFlight;
    @ApiModelProperty(value="항차")
    private String voyage;
    @ApiModelProperty(value="입항일자")
    private String arDate;
    @ApiModelProperty(value="입항시간")
    private String arTime;
    @ApiModelProperty(value="제출일자")
    private String abnReqDate;
    @ApiModelProperty(value="포장종류")
    private String pkgType;
    @ApiModelProperty(value="예정수량")
    private Integer exCt;
    @ApiModelProperty(value="예정중량")
    private Double exWt;
    @ApiModelProperty(value="반입수량")
    private Integer ct;
    @ApiModelProperty(value="반입중량")
    private Double wt;
    @ApiModelProperty(value="사고구분")
    private String accidentClass;
    @ApiModelProperty(value="사고수량")
    private Integer accidentCt;
    @ApiModelProperty(value="사고중량")
    private Double accidentWt;
    @ApiModelProperty(value="사유")
    private String rejectReason;
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

    /** HOUSE BL - 포장종류,수량,중량,사고구분*/
    @ApiModelProperty(value="반입이상관리번호순번")
    private Integer seqNo;
    @ApiModelProperty(value="BL번호")
    private String blNo;
    @ApiModelProperty(value="House BL 일련번호")
    private String hsn;
    @ApiModelProperty(value="House 화물관리번호")
    @Builder.Default
    private String cargoReferenceNo = "";
    @ApiModelProperty(value="House 화물관리번호")
    @Builder.Default
    private String cargoReferenceNoDisp = "";

    /** 반입예정테이블에 있는 정보 */
    @ApiModelProperty(value="반입예정번호")
    private String inPlanNo;
    @ApiModelProperty(value="오더관리번호")
    private String odJobNo;
    @ApiModelProperty(value="수하인상호")
    @Builder.Default
    private String consigneeName = "";
    @ApiModelProperty(value="영문품명")
    @Builder.Default
    private String commodityItemLoc = "";

    @ApiModelProperty(value="반입이상등록여부") //House / Master Single 영역의 그리드에서 기존에 존재하는 정보와 +버튼으로 추가한 정보를 구분하기 위해 추가
    private String abnYn;
}

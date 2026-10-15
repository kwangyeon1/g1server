package org.yht.exps.express.model.bms.bmsAbnormalCorrection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import java.util.List;
import java.util.Map;

@Data
@Builder
@ApiModel(value = "CarryInAbnormalCorr", description = "반입물품이상신고정정(MASTER 입항정보)")
@Alias("carryInAbnormalCorr")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class CarryInAbnormalCorr extends ResultList {

    @ApiModelProperty(value = "화면에서 입력받은 하우스정보 리스트")
    List<Map<String,String>> gridData;

    @ApiModelProperty(value="사업장 보세 구역번호 8자리")
    private String bmsWhCode;

    @ApiModelProperty(value="gmtTime_기본값 ")
    @Builder.Default
    private String gmtTime ="9";

    @ApiModelProperty(value="날짜형식_현재 날짜 입력시 DATEADD(HOUR, 9, GETDATE()) ")
    @Builder.Default
    private String dateFormat ="23";

    /** 반입이상정정 그리드 카운트*/
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

    /**반입이상정보*/
    @ApiModelProperty(value="반입이상관리번호")
    private Integer abnNo;

    /** 반입물품이상신고정정_BL (TB_BMS_ABNORMAL_CHANGE_BL)*/
    @ApiModelProperty(value="반입물품이상정정번호")
    private Integer abnChangeNo;
    @ApiModelProperty(value="순번")
    private Integer seqNo;
    @ApiModelProperty(value="화물관리번호")
    private String cargoReferenceNo;
    @ApiModelProperty(value="화물관리번호")
    @Builder.Default
    private String cargoReferenceNoDisp = "";
    @ApiModelProperty(value="House B/L 일련번호(House B/L Sequence Number)")
    private String hsn;
    @ApiModelProperty(value="BL번호")
    private String blNo;
    @ApiModelProperty(value="오더관리번호")
    @Builder.Default
    private String odJobNo = "";
    @ApiModelProperty(value="포장종류")
    private String pkgType;
    @ApiModelProperty(value="예정수량")
    private Integer exCt;
    @ApiModelProperty(value="예정중량")
    private Double exWt;
    @ApiModelProperty(value="수량")
    private Integer ct;
    @ApiModelProperty(value="중량")
    private Double wt;
    @ApiModelProperty(value="사고구분")
    private String accidentClass;
    @ApiModelProperty(value="사고수량")
    private Integer accidentCt;
    @ApiModelProperty(value="사고중량")
    private Double accidentWt;
    @ApiModelProperty(value="사유")
    private Object rejectReason;
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

    /** 반입물품이상신고정정(TB_BMS_ABNORMAL_CHANGE) */
    @ApiModelProperty(value="반입이상제출번호")
    private String customsReportNo;
    @ApiModelProperty(value="적하목록관리번호(Manifest Reference Number)")
    private String mrn;
    @ApiModelProperty(value="Master B/L 일련번호(Mater B/L Sequence Number)")
    private String msn;
    @ApiModelProperty(value="MBL번호")
    private String mblNo;
    @ApiModelProperty(value="신고일자(송신일시)")
    @Builder.Default
    private String lastPodDate = "";
    @ApiModelProperty(value="신고상태")
    private String lastPodStatus;
    @Builder.Default
    @ApiModelProperty(value="접수일시(수신일시)")
    private String lastPodRaqDate = "";
    @ApiModelProperty(value="선기명")
    private String vesselFlight;
    @Builder.Default
    @ApiModelProperty(value="항차")
    private String voyage = "";
    @ApiModelProperty(value="입항일자")
    private String arDate;
    @Builder.Default
    @ApiModelProperty(value="입항시간")
    private String arTime = "";
    @ApiModelProperty(value="정정일자")
    private String changeDate;
    @ApiModelProperty(value="정정차수")
    private String changeCount;
    @ApiModelProperty(value="정정구분")
    private String changeClass;
    @ApiModelProperty(value="정정사유")
    private Object changeReason;

    @ApiModelProperty(value="세관")
    private String customsCode;
    @ApiModelProperty(value="세관과")
    private String customsDptCode;

    /** Web XML Send */
    private String typeCode; 		 	/** 문서형태구분: 전송될 전자문서의 유형을 기재(ex. GOVCBR5II) */
    private String officeId;			/** 신고세관/과 */
    private String bmsCustomsCode;   	/** 세관코드 */
    private String bmsCustomsDptCode;	/** 세관과코드 */
    private String airseaGbn;			/** 해상/항공 전송구분 */

    //시스템 로그
    @ApiModelProperty(value="처리자")
    private String logUserName;
    @ApiModelProperty(value="처리자(영)")
    private String logUserNameEng;
    @ApiModelProperty(value="출력용 사업장명")
    private String logUserBusinessPrintName;

    @ApiModelProperty(value="도입사코드", example="YHTXX")
    private String entCode;
    @ApiModelProperty(value="사업장코드", example="YHTXX-KR")
    private String entBusinessCode;

}

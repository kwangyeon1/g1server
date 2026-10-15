package org.yht.exps.express.model.bms.bmsInCorrection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import org.yht.exps.express.model.bms.carryin.Container;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@ApiModel(value = "CarryInChange", description = "반입정정")
@Alias("carryInChange")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class CarryInChange extends ResultList {
    @ApiModelProperty(value = "변경항목정보 리스트")
    private List<ChangeItem> changeItemList;
    @ApiModelProperty(value = "화면에서 입력받은 변경항목정보 리스트")
    private List<Map<String,String>> changeInfoList;
    @ApiModelProperty(value="gmtTime_기본값 ")
    @Builder.Default
    private String gmtTime ="9";
    @ApiModelProperty(value="날짜형식_현재 날짜 입력시 DATEADD(HOUR, 9, GETDATE()) ")
    @Builder.Default
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
    /*@ApiModelProperty(value="번호")
    private int num;*/
    @ApiModelProperty(value="파일공유여부")
    private String fileShare;

    /** 반입정정 */
    @ApiModelProperty(value="반출입정정신청번호")
    private Integer changeNo;
    @ApiModelProperty(value="반출입구분(5LC.반입,5LD.반출)")
    @Builder.Default
    private String inoutClass = "5LC";
    @ApiModelProperty(value="반출입번호")
    private Integer inoutNo;
    @ApiModelProperty(value="반출입신고번호")
    private String customsReportNo;
    @ApiModelProperty(value="현재진행상태일시")
    private String lastPodDate;
    @ApiModelProperty(value="현재진행상태코드")
    private String lastPodStatus;
    @ApiModelProperty(value="접수일시")
    private String lastPodRaqDate;
    @ApiModelProperty(value="정정일자")
    private String changeDate;
    @ApiModelProperty(value="정정차수(001,002)")
    private String changeCount;
    @ApiModelProperty(value="정정구분(01.항목정정,03.내역삭제)")
    private String changeClass;
    @ApiModelProperty(value="신청인")
    private Object changeEmployeeName;
    @ApiModelProperty(value="정정사유")
    private Object changeReason;
    @ApiModelProperty(value="화물관리번호")
    private String cargoReferenceNo;
    @ApiModelProperty(value="화물관리번호")
    @Builder.Default
    private String cargoReferenceNoDisp = "";
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

    @ApiModelProperty(value="BL번호")
    private String blNo;
    @ApiModelProperty(value="MBL번호")
    @Builder.Default
    private String mblNo = "";
    @ApiModelProperty(value="오더관리번호")
    @Builder.Default
    private String odJobNo = "";

    @ApiModelProperty(value = "항목정정구분")
    private String changeCode;
    @ApiModelProperty(value = "정정전내용")
    private String changeBefore;
    @ApiModelProperty(value = "정정후내용")
    private String changeAfter;

    /**반입정정엔트리 - 정정항목(ITEM) */
    @ApiModelProperty(value="정정반입구분")
    private String inClassChanged;
    @ApiModelProperty(value="정정반입일자")
    private String carryinDateChanged;
    @ApiModelProperty(value="정정반입시간")
    @Builder.Default
    private String carryinTimeChanged = "";
    @ApiModelProperty(value="정정반입근거번호")
    @Builder.Default private
    String inApprovalNoChanged = "";
    @ApiModelProperty(value="정정포장종류(단위)")
    private String pkgTypeChanged;
    @ApiModelProperty(value="정정반입수량")
    private Integer ctChanged;
    @ApiModelProperty(value="정정반입중량")
    private Double wtChanged;
    @ApiModelProperty(value="정정사고구분")
    private String accidentClassChanged;
    @ApiModelProperty(value="정정사고수량")
    private Integer accidentCtChanged;
    @ApiModelProperty(value="정정사고중량")
    private Double accidentWtChanged;

    /** Web XML Send */
    private String typeCode; /** 문서형태구분: 전송될 전자문서의 유형을 기재(ex. GOVCBR5LC, GOVCBR5LD) */

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

package org.yht.exps.express.model.bms.carryin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;

@Data
@Builder
@ApiModel(value = "BmsInspectResult", description = "박스 리스트")
@Alias("bmsInspectResult")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsInspectResult {

    @ApiModelProperty(value="사업장코드", example="YHTXX-KR")
    private String entBusinessCode;

    @ApiModelProperty(value="gmtTime_기본값 ")

    private String gmtTime ="9";

    @ApiModelProperty(value="날짜형식_현재 날짜 입력시 DATEADD(HOUR, 9, GETDATE()) ")

    private String dateFormat ="23";
    
    /** 검사결과 */
    @ApiModelProperty(value="반입번호")
    private Integer inNo;
    @ApiModelProperty(value="검사종류")
    private String bmsInspectionType;
    @ApiModelProperty(value="검사종류 코드")
    private String inspectionResultType;
    @ApiModelProperty(value="HSN")
    private String hsn;
    @ApiModelProperty(value="검사피킹입력자")
    private String inspectionStartUserId;
    @ApiModelProperty(value="검사피킹일시")
    private String inspectionStartDateTime;
    @ApiModelProperty(value="검사완료여부")
    private String inspectionFinishYn;
    @ApiModelProperty(value="검사완료비고")
    private String inspectionFinishYnRemarks;
    @ApiModelProperty(value="검사완료입력자")
    private String inspectionFinishUserId;
    @ApiModelProperty(value="검사완료일시")
    private String inspectionFinishDateTime;
    
    // 검사결과완료비고 column
    @ApiModelProperty(value="검사완료비고")
    private String inspectionFinishRemarks;
    
    /** 검사결과 추가정보 */
    @ApiModelProperty(value="박스 총수량")
    private Integer packingCnt;
    @ApiModelProperty(value="TIC반출(Grid용)")
    private String ticOutStatus;
    @ApiModelProperty(value="TIC반출(Grid용)")
    private String otherOutPlan;
   
    /** 박스 */
    @ApiModelProperty(value="번호")
    private Integer seq;
    @ApiModelProperty(value="반입예정번호")
    private Integer inPlanNo;
    @ApiModelProperty(value="BL번호")
    private String blNo;
    @ApiModelProperty(value="Packing번호")
    private String packingNo;
    @ApiModelProperty(value="Package수량")
    private Integer packingCt;
    @ApiModelProperty(value="가로")
    private Integer dimWidth;
    @ApiModelProperty(value="세로")
    private Integer dimLength;
    @ApiModelProperty(value="높이")
    private Integer dimHeight;
    @ApiModelProperty(value="계근중량")
    private Double weighingWt;
    @ApiModelProperty(value="입고수량")
    private Integer inCt;
    @ApiModelProperty(value="출고수량")
    private Integer outCt;
    @ApiModelProperty(value="견품입고수량")
    private Integer sampleInCt;
    @ApiModelProperty(value="견품출고수량")
    private Integer sampleOutCt;
    @ApiModelProperty(value="이고입고수량")
    private Integer moveInCt;
    @ApiModelProperty(value="이고출고수량")
    private Integer moveOutCt;
    @ApiModelProperty(value="재입고수량")
    private Integer returnInCt;
    @ApiModelProperty(value="재출고수량")
    private Integer returnOutCt;
    @ApiModelProperty(value="로케이션위치")
    private String locationPlacePkg;
    @ApiModelProperty(value="로케이션상태")
    private String locationStatus;
    
    /** 반입 */
    
    @ApiModelProperty(value="MBL번호")
    private String mblNo;
    @ApiModelProperty(value="오더JOB번호")
	private String odJobNo;
    @ApiModelProperty(value="화물관리번호")
    private String cargoReferenceNo;
    @ApiModelProperty(value="선별결과코드(목록선별)")
    private String seInspectionCode;
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
    
    /** 반출 */
    @ApiModelProperty(value="반출번호")
    private Integer outNo;
    @ApiModelProperty(value="반출수량")
    private Integer bmsOutCt;

    /** 검사사유관리 */
    @ApiModelProperty(value="사유")
    private String inspectRemark;
    @ApiModelProperty(value="선별결과비고")
    private String seInspectionRemark;
    @ApiModelProperty(value="검역구분비고")
    private String inspectionTypeRemark;
    @ApiModelProperty(value="XRAY판독구분비고")
    private String xrayInspectionRemark;
    @ApiModelProperty(value="통관검사계획비고")
    private String paln5emInspectionRemark;
    
    @ApiModelProperty(value="선별검사 (코드 이름)")
    private String seInspectionCodeName;
    @ApiModelProperty(value="xray검사 (코드 이름)")
    private String xrayInspectionCodeName;
    @ApiModelProperty(value="통관검사 (코드 이름)")
    private String paln5emInspectionCodeName;
    @ApiModelProperty(value="검역검사,A,B (코드 이름)")
    private String inspectionTypeName;
    
    @ApiModelProperty(value="선별검사 검사종류 코드")
    private String seInspectionCodeType;
    @ApiModelProperty(value="xray검사 검사종류 코드")
    private String xrayInspectionCodeType;
    @ApiModelProperty(value="통관검사 검사종류 코드")
    private String paln5emInspectionCodeType;
    @ApiModelProperty(value="검역검사,A,B 검사종류 코드")
    private String inspectionTypeType;
    
    @ApiModelProperty(value="거대 (이름)")
    private String bigSizeYnName;
    @ApiModelProperty(value="지재권 (이름)")
    private String intellectualPropYnName;
    @ApiModelProperty(value="집중 (이름)")
    private String inspectionCustomsYnName;
    
    @ApiModelProperty(value="선별검사완료여부")
    private String seInspectionFinishYn;
    @ApiModelProperty(value="XRAY검사완료여부")
    private String xrayInspectionFinishYn;
    @ApiModelProperty(value="통관검사계획검사완료여부")
    private String paln5emInspectionFinishYn;
    
}

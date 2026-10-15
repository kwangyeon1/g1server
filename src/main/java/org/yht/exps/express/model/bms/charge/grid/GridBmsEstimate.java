package org.yht.exps.express.model.bms.charge.grid;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.bms.charge.BmsEstimate;

@Data
//@Builder
@ApiModel(value = "gridBmsEstimate", description = "창고료계산 Grid 에서 사용 VO 객체")
@Alias("gridBmsEstimate")
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class GridBmsEstimate extends BmsEstimate {
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


    @ApiModelProperty(value="화물구분")
    private String cdType;
    @ApiModelProperty(value="영문품명")
    private String commodityItemLoc;

    @ApiModelProperty(value="DO일자")
    private  String   doDate       ;

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
    @ApiModelProperty(value="검역1")
    private String inspectionType;
    @ApiModelProperty(value="검역2")
    private String inspectionTypeA;
    @ApiModelProperty(value="검역3")
    private String inspectionTypeB;
    @ApiModelProperty(value="XRAY판독")
    private String xrayInspectionCode;

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
    @ApiModelProperty(value="참조번호")
    private  String   referenceNo  ;
    @ApiModelProperty(value="승인검사 대상")
    private String inspectYn;

    @ApiModelProperty(value="선기명")
    private String vesselFlight;
    @ApiModelProperty(value="항차")
    private String voyage;
    @ApiModelProperty(value="입항일자")
    private String arDate;
    @ApiModelProperty(value="입항시간")
    private String arTime;
    
    // 필터적용 후 엑셀 다운로드 오류로 인한 추가 2026.02.26 2sowell
    @ApiModelProperty(value="반출구분명")
    private String outClassName;
    @ApiModelProperty(value="요율구분명")
    private String applyClassName;
    @ApiModelProperty(value="화물구분명")
    private String cdTypeName;
}

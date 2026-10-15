package org.yht.exps.express.model.mobile.bms;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@ApiModel(value = "MobPalo", description = "보세구역관리  모바일  로케이션적치 Detail")
@Alias("mobPalo")
public class MobPalo {
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	
	@ApiModelProperty(value="오더JOB번호")
	private String odJobNo;
	@ApiModelProperty(value="데이터구분")
	private String dGbn;
	@ApiModelProperty(value="하우스빌번호")
	private String blNo;
	@ApiModelProperty(value="화물관리번호")
    private String cargoReferenceNo;
	@ApiModelProperty(value="Packing번호")
    private String packingNo;
	@ApiModelProperty(value="예정수량")
    private Integer exCt;
    @ApiModelProperty(value="예정중량")
    private Double exWt;
    @ApiModelProperty(value="반입수량")
    private Integer ct;
    @ApiModelProperty(value="반입중량")
    private Double wt;
    @ApiModelProperty(value="반입예정번호")
    private Integer inPlanNo;
    @ApiModelProperty(value="반입번호")
    private Integer inNo;
    @ApiModelProperty(value="장치위치코드")
	private String locationPlaceCode;
	@ApiModelProperty(value="장치위치")
	private String locationPlace;
	@ApiModelProperty(value="장치위치명")
	private String locationPlaceName;
	@ApiModelProperty(value="원본 장치위치")
	private String origLocationPlace;
	@ApiModelProperty(value="반출금지여부")
	private String carryOutStopYn;
	@ApiModelProperty(value="검사")
	private String inspection;
	@ApiModelProperty(value="검사완료여부")
	private String inspectionFinishYn;
	@ApiModelProperty(value="홀드history")
	private String holdHistory;
	@ApiModelProperty(value="반출승인일자")
	private String approvalDate;
	@ApiModelProperty(value=" 보관일 (현재일자 - 반입일자)")
	private String storageDays;
	
	@ApiModelProperty(value="박스삭제여부")
    private String deleteYn;
	@ApiModelProperty(value="출고여부")
	private int outCt;
	@ApiModelProperty(value="박스Identity")
	private int seq;
	
	/** 반입예정및 XRAY 검사정보 */
    @ApiModelProperty(value="선별결과코드(목록선별)")
    private String seInspectionCode;
    
	@ApiModelProperty(value="XRAY판독구분코드")
    private String xrayInspectionCode;
	
	@ApiModelProperty(value="통관검사계획코드")
    private String paln5emInspectionCode;
    
    @ApiModelProperty(value="검역구분코드")
    private String inspectionType;
    
    @ApiModelProperty(value="검역구분코드2")
    private String inspectionTypeA;
    
    @ApiModelProperty(value="검역구분코드3")
    private String inspectionTypeB;
	    
	@ApiModelProperty(value="거대화물여부")
    private String bigSizeYn;
	
	@ApiModelProperty(value="우범화물여부")
    private String crimeSelectionYn;
	
    @ApiModelProperty(value="지재권")
    private String intellectualPropYn;
    
    @ApiModelProperty(value="집중검사")
    private String inspectionCustomsYn;
	
	/** 반입 */
	@ApiModelProperty(value="모든검사완료여부")
	private String allInspectionFinishYn;
	
	/** PDA 적치화면 */
	@ApiModelProperty(value="결과")
	private String scanResult;
	
	/** 저장 */
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	
}

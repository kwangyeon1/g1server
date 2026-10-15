package org.yht.exps.express.model.mobile.bms;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "BmsOutReleaseResult", description = "보세구역관리 PDA 반출출고 조회")
@Alias("bmsOutReleaseResult")
public class BmsOutReleaseResult {
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	
	@ApiModelProperty(value="데이터구분")
	private String dGbn;
	@ApiModelProperty(value="하우스빌번호")
	private String blNo;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="화물관리번호")
    private String cargoReferenceNo;
	@ApiModelProperty(value="오더JOB번호")
	private String odJobNo;
	
	@ApiModelProperty(value="배송사상호")
    private String deliveryName;
	@ApiModelProperty(value="반입신고상태")
    private String inLastPodStatus;
	@ApiModelProperty(value="반입구분")
    private String inClass;
	@ApiModelProperty(value="검사완료여부")
    private String allInspectionFinishYn;
	@ApiModelProperty(value="XRAY판독구분코드")
	private String xrayInspectionCode;
	@ApiModelProperty(value="XRAY판독구분코드명")
	private String xrayInspectionCodeName;
	@ApiModelProperty(value="통관검사계획코드")
	private String paln5emInspectionCode;
	@ApiModelProperty(value="검역구분코드")
    private String inspectionType;
    @ApiModelProperty(value="검역구분코드2")
    private String inspectionTypeA;
    @ApiModelProperty(value="검역구분코드3")
    private String inspectionTypeB;
	@ApiModelProperty(value="선별결과코드(목록선별)")
    private String seInspectionCode;
	@ApiModelProperty(value="우범화물여부")
    private String crimeSelectionYn;
    @ApiModelProperty(value="지재권")
    private String intellectualPropYn;
    @ApiModelProperty(value="집중검사")
    private String inspectionCustomsYn;
    @ApiModelProperty(value="거대화물여부")
    private String bigSizeYn;
    
    @ApiModelProperty(value="반출구분")
    private String outClass;
    @ApiModelProperty(value="반출근거번호")
    private String outApprovalNo;
    @ApiModelProperty(value="반출신고상태")
    private String outLastPodStatus;
    @ApiModelProperty(value="출고SCAN일시")
    private String outScanDateTime;
	
	@ApiModelProperty(value="Packing번호")
    private String packingNo;
    @ApiModelProperty(value="수량")
    private Integer ct;
    @ApiModelProperty(value="중량")
    private Double wt;
    @ApiModelProperty(value="반출수량")
    private Integer outCt;
    @ApiModelProperty(value="반입예정번호")
    private Integer inPlanNo;
    @ApiModelProperty(value="반입번호")
    private Integer inNo;
	@ApiModelProperty(value="홀드사유")
	private String holdReason;
	@ApiModelProperty(value="승인통보(31.승인,1:취하)")
	private String approvalClass;
	@ApiModelProperty(value="키번호")
    private Integer keyNo;
	
	@ApiModelProperty(value="관세청통보일시")
	private String notifyDateTime;
	
	/** 저장 */
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	
}

package org.yht.exps.express.model.bms.bmsCargo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@Builder
@ApiModel(value = "BmsCargo", description = "화물")
@Alias("bmsCargo")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsCargo extends ResultList {

	@ApiModelProperty(value="데이터 타입")
	private String dataType;
	@ApiModelProperty(value="신고일자")
	private String lastPodDate;
	@ApiModelProperty(value="신고상태")
	@Builder.Default
	private String lastPodStatus = "";
	@ApiModelProperty(value="반입 신고상태")
	@Builder.Default
	private String inLastPodStatus = "";
	@ApiModelProperty(value="반출 신고상태")
	@Builder.Default
	private String outLastPodStatus = "";
	@ApiModelProperty(value="속성신고상태")
	@Builder.Default
	private String lastPod5tlStatus = "";
	@ApiModelProperty(value="오더관리번호")
	@Builder.Default
	private String odJobNo = "";
	@ApiModelProperty(value="반입예정번호")
	@Builder.Default
	private Integer inPlanNo = 0;
	@ApiModelProperty(value="화물관리번호")
	@Builder.Default
	private String cargoReferenceNo = "";
	@ApiModelProperty(value="BL번호")
	private String blNo;
	@ApiModelProperty(value="MBL번호")
	@Builder.Default
	private String mblNo = "";
	@ApiModelProperty(value="장치위치")
	private String locationPlace;
	@ApiModelProperty(value="선기명")
	@Builder.Default
	private String vesselFlight = "";
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="입항시간")
	private String arTime;
	@ApiModelProperty(value="영문품명")
	@Builder.Default
	private String commodityItemLoc = "";
	@ApiModelProperty(value="수하인코드")
	@Builder.Default
	private String consigneeCode = "";
	@ApiModelProperty(value="수하인상호")
	@Builder.Default
	private String consigneeName = "";
	@ApiModelProperty(value="선별결과코드(목록선별)")
	@Builder.Default
	private String seInspectionCode = "";
	@ApiModelProperty(value="검역구분코드")
	@Builder.Default
	private String inspectionType = "";
	@ApiModelProperty(value="XRAY판독구분코드")
	@Builder.Default
	private String xrayInspectionCode = "";
	@ApiModelProperty(value="검사완료여부")
	private String allInspectionFinishYn;
	@ApiModelProperty(value="화물구분(통관구분)")
	@Builder.Default
	private String cdType = "";
	@ApiModelProperty(value="통관검사계획코드")
	private String paln5emInspectionCode;
	@ApiModelProperty(value="거대화물여부")
	private String bigSizeYn;
	@ApiModelProperty(value="집중검사")
	private String inspectionCustomsYn;
	@ApiModelProperty(value="지재권")
	private String intellectualPropYn;
	@ApiModelProperty(value="우범화물여부")
	private String crimeSelectionYn;
	@ApiModelProperty(value="검역구분코드2")
	private String inspectionTypeA;
	@ApiModelProperty(value="검역구분코드3")
	private String inspectionTypeB;
	@ApiModelProperty(value="MGN대상여부")
	private String mgnIssueYn;
	@ApiModelProperty(value="반입일자")
	private String carryInDate;
	@ApiModelProperty(value="반입시간")
	@Builder.Default
	private String carryInTime = "";
	@ApiModelProperty(value="반입수량")
	private Integer ct;
	@ApiModelProperty(value="수량표시")
	private String ctPrint;
	@ApiModelProperty(value="반입중량")
	private Double wt;
	@ApiModelProperty(value="반출일자")
	@Builder.Default
	private String carryOutDate = "";
	@ApiModelProperty(value="반출시간")
	@Builder.Default
	private String carryOutTime = "";
	@ApiModelProperty(value="이고반입일시")
	@Builder.Default
	private String moveCarryInDate = "";
	@ApiModelProperty(value="이고반출일시")
	@Builder.Default
	private String moveCarryOutDate = "";
	@ApiModelProperty(value="반출승인일자")
	private String approvalDate;
	@ApiModelProperty(value="승인취하일자")
	@Builder.Default  
	private String approvalCancelDate = "";
	@ApiModelProperty(value="출고SCAN일시")
	@Builder.Default
	private String outScanDateTime = "";
	@ApiModelProperty(value="목록번호")
	@Builder.Default
	private String referenceNo = "";
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
	@Builder.Default
	private String deliveryCode = "";
	@ApiModelProperty(value="배송사상호")
	@Builder.Default
	private String deliveryName = "";
	
	@ApiModelProperty(value="미검사완료")
	private String inspectionResultType;
	
	/* 홀드정보 */
	@ApiModelProperty(value="홀드여부")
	@Builder.Default
	private String holdYn = "N";
	@ApiModelProperty(value="홀드코드")
	private String holdCode;
	@ApiModelProperty(value="홀드사유")
	private String holdReason;
	@ApiModelProperty(value="홀드처리여부")
	private String holdReleaseYn;
	
	/* BL분할 */
	@ApiModelProperty(value="분할구분코드")
	private String divideClass;
	
	/* 순번 */
	@ApiModelProperty(value="순번")
	private String printNo;

	/* 분류 */
	@ApiModelProperty(value="분류 메세지")
	private String rsltMsg;

	/* BCR 일시 */
	@ApiModelProperty(value="BCR일시")
	private String bcrTime;

	/* 판독 일시 */
	@ApiModelProperty(value="판독일시")
	private String xrayPassTime;

	/* 미제출 여부 */
	@ApiModelProperty(value="미제출여부")
	@Builder.Default
	private String notSubmitYn = "N";

	/* 업무 권고 */
	@ApiModelProperty(value="업무권고")
	@Builder.Default
	private String adviceWork = "";
}

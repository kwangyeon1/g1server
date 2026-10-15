package org.yht.exps.express.model.bms.bmsCargo;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import org.yht.exps.express.model.bms.carryin.InPlanPkg;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@ApiModel(
	value = "bmsCargoInfo",
	description = "화물 상세 정보"
)
@Alias("bmsCargoInfo")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class BmsCargoInfo extends ResultList {

	@ApiModelProperty(value="반입번호")
	private Integer inNo;
 
	@ApiModelProperty(value="반입예정번호")
	private Integer inPlanNo;
	
	@ApiModelProperty(value = "반입구분")
	private String inClass;
	
	@ApiModelProperty(value="반출번호")
	private Integer outNo;
 
	@ApiModelProperty(value="반출승인번호")
	private Integer outPlanNo;
	
	@ApiModelProperty(value = "승인근거번호")
	private String outApprovalNo;
	
	@ApiModelProperty(value = "승인구분")
	private String approvalClass;

	@ApiModelProperty(value = "반출구분")
	private String outClass;
	
	@ApiModelProperty(value = "반출화주코드")
	private String customerCode;
	
	@ApiModelProperty(value = "반출화주이름")
	private String customerName;
	
	@ApiModelProperty(value = "납세의무자사업자번호")
	private String companyBusinessNo;
	
	@ApiModelProperty(value = "납세의무자상호")
	private String companyName;
	
	@ApiModelProperty(value = "오더번호")
	private String odJobNo;
	
	@ApiModelProperty(value = "BL 번호")
	private String blNo;

	@ApiModelProperty(value = "화물관리번호")
	private String cargoReferenceNo;

	@ApiModelProperty(value = "MBL 번호")
	private String mblNo;

	@ApiModelProperty(value = "선기명 또는 항공편명")
	private String vesselFlight;

	@ApiModelProperty(value = "품명")
	private String commodityItemLoc;

	@ApiModelProperty(value = "화물구분 또는 통관구분")
	private String cdType;

	@ApiModelProperty(value = "미화 금액")
	private Double amountUsd;

	@ApiModelProperty(value = "장치위치")
	private String locationPlace;

	@ApiModelProperty(value = "최종 정보 구분")
	private String infoLastClass;

	@ApiModelProperty(value = "송하인 상호")
	private String shipperName;

	@ApiModelProperty(value = "송하인 주소")
	private String shipperAddress1;

	@ApiModelProperty(value = "송하인 전화번호")
	private String shipperTel;

	@ApiModelProperty(value = "수하인 상호")
	private String consigneeName;

	@ApiModelProperty(value = "수하인 주소")
	private String consigneeAddress1;

	@ApiModelProperty(value = "수하인 전화번호")
	private String consigneeTel;

	@ApiModelProperty(value = "입항일시")
	private String arDateTime;

	@ApiModelProperty(value = "반입일시")
	private String carryInDateTime;

	@ApiModelProperty(value = "반출일시")
	private String carryOutDateTime;

	@ApiModelProperty(value = "포워더 상호")
	private String fwdName;

	@ApiModelProperty(value = "관세사 상호")
	private String brokerName;

	@ApiModelProperty(value = "배송사 상호")
	private String deliveryName;

	@ApiModelProperty(value = "예정 수량")
	private Integer exCt;

	@ApiModelProperty(value = "반입 수량")
	private Integer ct;

	@ApiModelProperty(value = "반출 수량")
	private Integer outCt;

	@ApiModelProperty(value = "재고 수량")
	private Integer stockCt;

	@ApiModelProperty(value = "예정 중량")
	private Double exWt;

	@ApiModelProperty(value = "반입 중량")
	private Double wt;

	@ApiModelProperty(value = "반출 중량")
	private Double outWt;

	@ApiModelProperty(value = "재고 중량")
	private Double stockWt;

	@ApiModelProperty(value = "X-RAY 라인 번호")
	private String xrayLineNo;

	@ApiModelProperty(value = "X-RAY 판독 구분 코드")
	private String xrayInspectionCode;
	
	@ApiModelProperty(value="선별결과코드(목록선별)")
	private String seInspectionCode;
	
	@ApiModelProperty(value="통관검사계획코드")
	private String paln5emInspectionCode;
	
	@ApiModelProperty(value="검역구분코드")
	private String inspectionType;
	
	@ApiModelProperty(value="거대화물여부")
    private String bigSizeYn;
	
    @ApiModelProperty(value="지재권")
    private String intellectualPropYn;
    
    @ApiModelProperty(value="집중검사")
    private String inspectionCustomsYn;
    
    @ApiModelProperty(value = "(이름)X-RAY 판독 구분 코드")
	private String xrayInspectionCodeStr;
	
	@ApiModelProperty(value="(이름)선별결과코드(목록선별)")
	private String seInspectionCodeStr;
	
	@ApiModelProperty(value="(이름)통관검사계획코드")
	private String paln5emInspectionCodeStr;
	
	@ApiModelProperty(value="(이름)검역구분코드")
	private String inspectionTypeStr;

	@ApiModelProperty(value = "X-RAY 스캔 수량")
	private Integer xrayScanCt;

	@ApiModelProperty(value = "X-RAY 이미지 수량")
	private Integer xrayImageCt;

	@ApiModelProperty(value = "홀드 코드")
	private String holdCode;

	@ApiModelProperty(value = "홀드 사유")
	private String holdReason;

	@ApiModelProperty(value = "홀드 해제 여부")
	private String holdReleaseYn;
	
	/** xray history */
	@ApiModelProperty(value = "X-RAY 판독일시")
	private String xrayPassTime;
	
	private List<Map<String, Object>> inspectionList;
	
	private List<InPlanPkg> pkgList;
	
}

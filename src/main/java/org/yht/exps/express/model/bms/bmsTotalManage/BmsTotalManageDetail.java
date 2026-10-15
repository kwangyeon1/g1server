package org.yht.exps.express.model.bms.bmsTotalManage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

@Data
@Builder
@ApiModel(value = "BmsTotalManageDetail", description = "통합관리 상세 정보")
@Alias("bmsTotalManageDetail")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsTotalManageDetail extends ResultList {
	@ApiModelProperty(value="진행상태")
	@Builder.Default
	private String lastPodStatus = "";
	@ApiModelProperty(value="관세청")
	@Builder.Default
	private String blStatus = "조회";
	@ApiModelProperty(value="B/L번호")
	@Builder.Default
	private String hblNo = "";
	@ApiModelProperty(value="M.B/L번호")
	@Builder.Default
	private String mblNo = "";
	@ApiModelProperty(value="화물관리번호")
	@Builder.Default
	private String cargoReferenceNo = "";
//	@ApiModelProperty(value="화물관리번호")
//	@Builder.Default
//	private String cargoReferenceNoDisp = "";
	@ApiModelProperty(value="박스번호")
	@Builder.Default
	private String packingNo = "";
	@ApiModelProperty(value="화물구분")
	@Builder.Default
	private String cdType = "";
	@ApiModelProperty(value="입항일자")
	@Builder.Default
	private String arDate = "";
	@ApiModelProperty(value="입항시간")
	@Builder.Default
	private String arTime = "";
	@ApiModelProperty(value="편명(선기명)")
	@Builder.Default
	private String vesselFlight = "";
	@ApiModelProperty(value="반입상태")
	@Builder.Default
	private String carryInStatus = "";
	@ApiModelProperty(value="속성상태")
	@Builder.Default
	private String lastPod5tlStatus = "";
	@ApiModelProperty(value="반입구분")
	@Builder.Default
	private String inClass = "";
	@ApiModelProperty(value="반입일자")
	@Builder.Default
	private String carryinDate = "";
	@ApiModelProperty(value="반입시간")
	@Builder.Default
	private String carryinTime = "";
	@ApiModelProperty(value="반입신고번호")
	@Builder.Default
	private String customsReportNo = "";
	@ApiModelProperty(value="장치위치")
	@Builder.Default
	private String locationPlace = "";
	@ApiModelProperty(value="승인통보(31.승인,1:취하)")
	@Builder.Default
	private String approvalClass = "";
	@ApiModelProperty(value="승인구분")
	@Builder.Default
	private String approvalOutClass = "";
	@ApiModelProperty(value="승인근거번호")
	@Builder.Default
	private String approvalOutNo = "";
	@ApiModelProperty(value="인도조건")
	@Builder.Default
	private String deliveryTerms = "";
	@ApiModelProperty(value="관세사코드")
	@Builder.Default
	private String brokerCode = "";
	@ApiModelProperty(value="납세의무자상호")
	@Builder.Default
	private String companyName = "";
	@ApiModelProperty(value="승인~취소간격")
	@Builder.Default
	private String appcancelDateDiff = "";
	@ApiModelProperty(value="반입~승인간격")
	@Builder.Default
	private String inappDateDiff = "";
	@ApiModelProperty(value="반출승인일자")
	@Builder.Default
	private String approvalDate = "";
	@ApiModelProperty(value="승인취하일자")
	@Builder.Default
	private String approvalCancelDate = "";
	@ApiModelProperty(value="반출구분")
	@Builder.Default
	private String outClass = "";
	@ApiModelProperty(value="반출근거번호")
	@Builder.Default
	private String outApprovalNo = "";
	@ApiModelProperty(value="반출일자")
	@Builder.Default
	private String carryoutDate = "";
	@ApiModelProperty(value="반출시간")
	@Builder.Default
	private String carryoutTime = "";
	@ApiModelProperty(value="반출상태")
	@Builder.Default
	private String carryOutStatus = "";
	@ApiModelProperty(value="관세")
	@Builder.Default
	private Double duty = 0.0;
	@ApiModelProperty(value="감정가액")
	@Builder.Default
	private Double appraiseValue = 0.0;
	@ApiModelProperty(value="결제방법")
	@Builder.Default
	private String insPaymentType = "";
	@ApiModelProperty(value="결제통화")
	@Builder.Default
	private String currencyCode = "";
	@ApiModelProperty(value="적하보험료납부")
	@Builder.Default
	private String insPaymentYn = "N";
	@ApiModelProperty(value="수입요건법령코드")
	@Builder.Default
	private String criteriaCode = "";
	@ApiModelProperty(value="반입반출간격")
	@Builder.Default
	private String inoutDateDiff = "";
	@ApiModelProperty(value="반출비고1")
	@Builder.Default
	private String remarks1 = "";
	@ApiModelProperty(value="반출비고2")
	@Builder.Default
	private String remarks2 = "";
	@ApiModelProperty(value="출고슈트번호")
	@Builder.Default
	private String chuteNo = "";
	@ApiModelProperty(value="참조번호")
	@Builder.Default
	private String referenceNo = "";
	@ApiModelProperty(value="출고SCAN일시")
	@Builder.Default
	private String outCmptDt = "";
	@ApiModelProperty(value="TIC이고반출차수")
	@Builder.Default
	private Integer ticOutSeq = 0;
	@ApiModelProperty(value="이고FTP전송")
	@Builder.Default
	private String ticOutFtpSendYn = "N";
	@ApiModelProperty(value="이고반출SCAN")
	@Builder.Default
	private String ticOutScanYn = "N";
	@ApiModelProperty(value="예정수량")
	@Builder.Default
	private Integer exCt = 0;
	@ApiModelProperty(value="예정중량")
	@Builder.Default
	private Double exWt = 0.0;
	@ApiModelProperty(value="XRAY판독수량")
	@Builder.Default
	private Integer xrayScanCt = 0;
	@ApiModelProperty(value="XRAY이미지수량")
	@Builder.Default
	private Integer xrayImageCt = 0;
	@ApiModelProperty(value="반입수량")
	@Builder.Default
	private Integer ct = 0;
	@ApiModelProperty(value="반입중량")
	@Builder.Default
	private Double wt = 0.0;
	@ApiModelProperty(value="승인수량")
	@Builder.Default
	private Integer approvalCt = 0;
	@ApiModelProperty(value="승인중량")
	@Builder.Default
	private Double approvalWt = 0.0;
	@ApiModelProperty(value="반출수량")
	@Builder.Default
	private Integer outCt = 0;
	@ApiModelProperty(value="반출중량")
	@Builder.Default
	private Double outWt = 0.0;
	@ApiModelProperty(value="재고수량")
	@Builder.Default
	private Integer stockCt = 0;
	@ApiModelProperty(value="재고중량")
	@Builder.Default
	private Double stockWt = 0.0;
	@ApiModelProperty(value="견품낱개수량")
	@Builder.Default
	private Integer samplePieces = 0;
	@ApiModelProperty(value="물품가격")
	@Builder.Default
	private Double amountUsd = 0.0;
	@ApiModelProperty(value="용도구분")
	@Builder.Default
	private String cdUseType = "";
	@ApiModelProperty(value="거래구분")
	@Builder.Default
	private String ecmType = "";
	@ApiModelProperty(value="거대화물여부")
	@Builder.Default
	private String bigSizeYn = "N";
	@ApiModelProperty(value="선별결과코드(목록선별)")
	@Builder.Default
	private String seInspectionCode = "";
	@ApiModelProperty(value="지재권")
	@Builder.Default
	private String intellectualPropYn = "N";
	@ApiModelProperty(value="집중검사")
	@Builder.Default
	private String inspectionCustomsYn = "N";
	@ApiModelProperty(value="검역구분코드")
	@Builder.Default
	private String inspectionType = "";
	@ApiModelProperty(value="XRAY판독")
	@Builder.Default
	private String xrayInspectionCode = "";
	@ApiModelProperty(value="통관검사계획코드")
	@Builder.Default
	private String paln5emInspectionCode = "";
	@ApiModelProperty(value="영문품명")
	@Builder.Default
	private String commodityItemLoc = "";
	@ApiModelProperty(value="수하인코드")
	@Builder.Default
	private String consigneeCode = "";
	@ApiModelProperty(value="수하인상호")
	@Builder.Default
	private String consigneeName = "";
	@ApiModelProperty(value="수하인기본주소")
	@Builder.Default
	private String consigneeAddress1 = "";
	@ApiModelProperty(value="수하인전화번호")
	@Builder.Default
	private String consigneeTel = "";
	@ApiModelProperty(value="송하인코드")
	@Builder.Default
	private String shipperCode = "";
	@ApiModelProperty(value="송하인상호")
	@Builder.Default
	private String shipperName ="";
	@ApiModelProperty(value="송하인기본주소")
	@Builder.Default
	private String shipperAddress1 = "";
	@ApiModelProperty(value="송하인전화번호")
	@Builder.Default
	private String shipperTel = "";
	@ApiModelProperty(value="통지처상호")
	@Builder.Default
	private String notifyName = "";
	@ApiModelProperty(value="배송사코드")
	@Builder.Default
	private String deliveryCode = "";
	@ApiModelProperty(value="배송사상호")
	@Builder.Default
	private String deliveryName = "";
	@ApiModelProperty(value="반입근거번호")
	@Builder.Default
	private String inApprovalNo = "";
	@ApiModelProperty(value="운송기한")
	@Builder.Default
	private String transPeriodDate = "";
	@ApiModelProperty(value="오더관리번호")
	@Builder.Default
	private String odJobNo = "";
	@ApiModelProperty(value="반입예정번호")
	@Builder.Default
	private Integer inPlanNo = 0;
	@ApiModelProperty(value="반입번호")
	@Builder.Default
	private Integer inNo = 0;
	@ApiModelProperty(value="반출승인통보번호")
	@Builder.Default
	private Integer outPlanNo = 0;
	@ApiModelProperty(value="반출번호")
	@Builder.Default
	private Integer outNo = 0;
	@ApiModelProperty(value="원본반입예정번호")
	@Builder.Default
	private Integer origInPlanNo = 0;
	@ApiModelProperty(value="반입박스번호")
	@Builder.Default
	private String inPkgNo = "";
	@ApiModelProperty(value="반출박스번호")
	@Builder.Default
	private String outPkgNo = "";
	@ApiModelProperty(value="견품박스번호")
	@Builder.Default
	private String samplePkgNo = "";
	@ApiModelProperty(value="TIC박스번호")
	@Builder.Default
	private String ticOutPkgNo = "";

	@ApiModelProperty(value="견품반출여부")
	@Builder.Default
	private String sampleOutYn = "N";
	@ApiModelProperty(value="견품재입고여부")
	@Builder.Default
	private String sampleReturnYn = "N";
	@ApiModelProperty(value="이고반출여부")
	@Builder.Default
	private String ticOutYn = "N";
	@ApiModelProperty(value="이고반입여부")
	@Builder.Default
	private String ticInYn = "N";
	@ApiModelProperty(value="이고대상")
	@Builder.Default
	private String ticYn = "";
	
	@ApiModelProperty(value="이고구분")
	@Builder.Default
	private String houseCdTypeGbnName = "";
	
    @ApiModelProperty(value="이고대상")
    private String checkTypeTypesStr;
	

	@ApiModelProperty(value="화물관리번호_표시")
	public String getCargoReferenceNoDisp() {
		String ret ="";
		if ( ! cargoReferenceNo.equals("") ) {
			if (cargoReferenceNo.length() > 10) {
				if (cargoReferenceNo.length() == 19) {
					ret = cargoReferenceNo.substring(0, 11) + "-" + cargoReferenceNo.substring(11, 15) + "-" + cargoReferenceNo.substring(15, 19);
				}else if (cargoReferenceNo.length() == 18) { // 마이그레이션 값은 18이다
					ret = cargoReferenceNo.substring(0, 11) + "-" + cargoReferenceNo.substring(11, 15) + "-" + cargoReferenceNo.substring(15);
				} else {
					ret = cargoReferenceNo.substring(0, 11) + "-" + cargoReferenceNo.substring(11);
				}
			} else {
				ret  = cargoReferenceNo;
			}
		}
		return ret;
	}
	
	@ApiModelProperty(value="진행상태")
	@Builder.Default
	private String cargoStep = "";
	@ApiModelProperty(value="이고구분")
	@Builder.Default
	private String transStep = "";
	@ApiModelProperty(value="이고구분 반입/반출")
	@Builder.Default
	private String transSel = "";
	@ApiModelProperty(value="견품구분")
	@Builder.Default
	private String sampleSel = "";
	@ApiModelProperty(value="검사항목명")
	@Builder.Default
	private String inspect = "";
	@ApiModelProperty(value="B/L번호")
	@Builder.Default
	private String blNo = "";
}

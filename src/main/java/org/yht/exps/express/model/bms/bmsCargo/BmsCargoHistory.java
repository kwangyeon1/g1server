package org.yht.exps.express.model.bms.bmsCargo;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

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
	value = "bmsCargoHistory",
	description = "화물 내역"
)
@Alias("bmsCargoHistory")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class BmsCargoHistory extends ResultList {

	@ApiModelProperty(value="데이터 출처(IN_PLAN, IN, OUT_PLAN, OUT)")
	private String sourceType;

	@ApiModelProperty(value="일자")
	private String statusDateTime;

	@ApiModelProperty(value="반입/반출 구분코드")
	private String statusClass;

	@ApiModelProperty(value="수량")
	private Integer ct;

	@ApiModelProperty(value="중량(kg)")
	private Double wt;

	@ApiModelProperty(value="신고번호")
	private String customsReportNo;
	
	@ApiModelProperty(value="근거번호")
	private String approvalNo;

	@ApiModelProperty(value="화물상태")
	private String lastPodStatus;

	/** 참고용필드 */
	@ApiModelProperty(value="화물관리번호")
	private String cargoReferenceNo;

	@ApiModelProperty(value="반입예정번호")
	private Integer inPlanNo;

	@ApiModelProperty(value="반입번호")
	private Integer inNo;
	
	@ApiModelProperty(value="반출승번호")
	private Integer outPlanNo;
	
	@ApiModelProperty(value="반출번호")
	private Integer outNo;
}

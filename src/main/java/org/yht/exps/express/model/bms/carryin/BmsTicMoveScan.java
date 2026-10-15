package org.yht.exps.express.model.bms.carryin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.util.Util;

@Data
@Builder
@ApiModel(value = "BmsTicMoveScan", description = "이고 반출 대상")
@Alias("bmsTicMoveScan")
@EqualsAndHashCode(callSuper=false)
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BmsTicMoveScan{

	@ApiModelProperty(value="반출 번호")
	private Integer outNo;

	@ApiModelProperty(value="Scan 번호")
	private String scanNo;

	@ApiModelProperty(value="BL 번호")
	private String blNo;

	@ApiModelProperty(value="In 번호")
	private Integer inNo;

	@ApiModelProperty(value="반출 CT")
	private Integer outCt;

	@ApiModelProperty(value="이고 반출 상태")
	private String outBackStatus;

	@ApiModelProperty(value="이고 반출 사용자 ID")
	private String outBackUserId;

	@ApiModelProperty(value="이고 반출 일자 및 시간")
	private String outBackDateTime;

	@ApiModelProperty(value="Scan 결과")
	private String scanResult;

	@ApiModelProperty(value="Scan 일자")
	private String scanDate;

	@ApiModelProperty(value="이고 반출입 종료 여부")
	private String outBackInFinishYn;

	@ApiModelProperty(value="입력자코드")
	private String registerUserId;

	@ApiModelProperty(value="입력자이름")
	private String registerUserName;

	@ApiModelProperty(value="입력일시")
	private String registerDateTime;

	public void setScanInfo(Map<String, Object> srchInspectResult, Map<String, Object> search, String reason) {
		inNo = srchInspectResult.isEmpty() ? 0 : (Integer) srchInspectResult.get("IN_NO");
		outNo = srchInspectResult.isEmpty() ? 0 : (Integer) srchInspectResult.get("OUT_NO");
		scanNo =  srchInspectResult.isEmpty() ? Util.null2str(search.get("packingNo")) : (String) srchInspectResult.get("PACKING_NO");
		blNo = srchInspectResult.isEmpty() ? Util.null2str(search.get("blNo")) : (String) srchInspectResult.get("BL_NO");
		outCt = srchInspectResult.isEmpty() ? 0 : (Integer) srchInspectResult.get("OUT_CT");
		outBackStatus = srchInspectResult.isEmpty() ? "" : (Integer) srchInspectResult.get("CT") > 1 ? "O" : "P";
		outBackUserId = Util.null2str(search.get("updateUserId"));
		scanResult = reason;
		outBackInFinishYn = "N";
		registerUserId = Util.null2str(search.get("updateUserId"));
	}
}

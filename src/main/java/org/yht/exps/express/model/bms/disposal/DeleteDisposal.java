package org.yht.exps.express.model.bms.disposal;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.util.List;
import java.util.Map;


@Builder
@Data
@ApiModel(value = "DeleteDisposal", description = "BMS  폐기신청 삭제 ")
@Alias("deleteDisposal")
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class DeleteDisposal {
	

	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="대표사업장코드", example="YHTXX")
	private String entBusinessCode;
	@ApiModelProperty(value="등록자")
	private String inputUserId;		
	@ApiModelProperty(value="리스트")
	private List<Map<String, Object>> dataList;

	@ApiModelProperty(value="gmtTime_기본값 ")
	@Builder.Default  private String gmtTime ="9";
	@ApiModelProperty(value="날짜형식_현재 날짜 입력시 DATEADD(HOUR, 9, GETDATE()) ")
	@Builder.Default  private String dateFormat ="23";

	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;


	@ApiModelProperty(value="사유")
	private String reason;

}


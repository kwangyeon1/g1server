package org.yht.exps.express.model.bms.bmsAbnormal;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor //생성자로 기본값 적용
@AllArgsConstructor
@Alias("batchBmsAbnormal")
@ApiModel(value = "BatchBmsAbnormal", description = "반입물품이상신고 Batch용 객체")
public class BatchBmsAbnormal {

	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="대표사업장코드", example="YHTXX")
	private String entBusinessCode;

	@ApiModelProperty(value="gmtTime_기본값 ")
	@Builder.Default private String gmtTime ="9";
	@ApiModelProperty(value="날짜형식_현재 날짜 입력시 DATEADD(HOUR, 9, GETDATE()) ")
	@Builder.Default private String dateFormat ="23";
	@ApiModelProperty(value="외국어구분(KOR,ENG)")
	private String userLanguage;

	@ApiModelProperty(value="등록자")
	private String inputUserId;
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;

	@ApiModelProperty(value="배치 처리구분(OUT)") // 추후 확장 고려 하여 작업
	private String batchExecType;

	/** Web XML Send */
	@ApiModelProperty(value="반입물품이상신고 데이터 리스트")
	private List<CarryInAbnormal> carryInAbnormalList;

}

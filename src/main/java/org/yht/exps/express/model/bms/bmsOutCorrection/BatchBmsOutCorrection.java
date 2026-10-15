package org.yht.exps.express.model.bms.bmsOutCorrection;

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
@Alias("batchBmsOutCorrection")
@ApiModel(value = "BatchBmsOutCorrection", description = "반출정정 Batch용 객체")
public class BatchBmsOutCorrection {

	/** 관세청 XML 송신(Send) - GOVCBR5LD(반출신고정정신청서) */
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
	@ApiModelProperty(value="반출정정 데이터 리스트")
	private List<CarryOutChange> bmsCarryOutChangeList;

	/** 시스템 로그 - ES */
    @ApiModelProperty(value="BL번호")
    private String blNo;
	@ApiModelProperty(value="오더관리번호")
    private String odJobNo;
    @ApiModelProperty(value="출력용 사업장명")
    private String logUserBusinessPrintName;

}

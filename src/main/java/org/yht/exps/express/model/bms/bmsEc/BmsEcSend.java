package org.yht.exps.express.model.bms.bmsEc;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Alias("bmsEcSend")
@ApiModel(value = "BmsEcSend", description = "BMS EC 재전송 객체 ")
public class BmsEcSend {

	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="대표사업장코드", example="YHTXX")
	private String entBusinessCode;
	@ApiModelProperty(value="등록자")
	private String inputUserId;
	
	@ApiModelProperty(value="EC 데이터 테이블")
	private String ecTable;
	@ApiModelProperty(value="EC 재송신 데이터")
	private List<BmsEcData> ecList;

	
	@ApiModelProperty(value="gmtTime_기본값 ")
	@Builder.Default  private String gmtTime ="9";
	@ApiModelProperty(value="날짜형식_현재 날짜 입력시 DATEADD(HOUR, 9, GETDATE()) ")
	@Builder.Default  private String dateFormat ="23";

	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;

	@ApiModelProperty(value="외국어구분(KOR,ENG)")
	private String userLanguage;

	@ApiModelProperty(value="배치 처리구분(DISP,OUT)")
	private String batchExecType;

	@ApiModelProperty(value="사업장 보세 구역번호 8자리")
	private String bmsWhCode;
	
}


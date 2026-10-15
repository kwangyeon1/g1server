package org.yht.exps.express.model.bms.disposal;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;

import java.util.List;


@Builder
@Data
@ApiModel(value = "BatchDisposal", description = "BMS  폐기신청 다건 서버 전송용 객체 ")
@Alias("batchDisposal")
// 생성자로 기본값 적용
@NoArgsConstructor
@AllArgsConstructor
public class BatchDisposal {
	

	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="대표사업장코드", example="YHTXX")
	private String entBusinessCode;
	@ApiModelProperty(value="등록자")
	private String inputUserId;
	//
	@ApiModelProperty(value="재고 리스트( 폐기 처리용 )")
	private List<InventoryIn> inList;

	@ApiModelProperty(value="폐기 리스트 (반출용)")
	private List<Disposal> dispList;

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


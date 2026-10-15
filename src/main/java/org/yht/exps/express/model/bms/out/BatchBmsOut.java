package org.yht.exps.express.model.bms.out;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.bms.disposal.InventoryIn;

import java.util.List;

@Data
@Builder
@NoArgsConstructor // 생성자로 기본값 적용
@AllArgsConstructor
@Alias("batchBmsOut")
@ApiModel(value = "BatchBmsOut", description = "BMS  반출 다건 서버 전송용 객체 ")
public class BatchBmsOut {

	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="대표사업장코드", example="YHTXX")
	private String entBusinessCode;
	@ApiModelProperty(value="등록자")
	private String inputUserId;
	//
	@ApiModelProperty(value="재고 리스트( 폐기 처리용 )")
	private List<InventoryIn> inList;

	/** (+) Web XML Send */
	@ApiModelProperty(value="폐기 리스트 (반출용)")
	private List<BmsOut> bmsOutList;

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

	/** 시스템 로그 - ES */
    @ApiModelProperty(value="BL번호")
    private String blNo;
	@ApiModelProperty(value="오더관리번호")
    private String odJobNo;
    @ApiModelProperty(value="출력용 사업장명")
    private String logUserBusinessPrintName;

    /** 세관이고반출확정 - 수입통관이고  */
    @ApiModelProperty(value="이고반출 데이터 생성 구분")
    @Builder.Default
    private String ticOutDataCreateGbn = "";
}


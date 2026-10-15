package org.yht.exps.express.model.bms.carryin;

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
@NoArgsConstructor //생성자로 기본값 적용
@AllArgsConstructor
@Alias("batchCarryIn")
@ApiModel(value = "BatchCarryIn", description = "BMS  특송장이고반출입  다건 서버 전송용 객체 ")
public class BatchCarryIn {

	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="대표사업장코드", example="YHTXX")
	private String entBusinessCode;
	@ApiModelProperty(value="등록자")
	private String inputUserId;
	//
	@ApiModelProperty(value="재고 리스트( 이고 반출)")
	private List<CarryIn> inList;


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

	@ApiModelProperty(value="배치 처리구분(OUT)") // 추후 확장 고려 하여 작업
	private String batchExecType;
	@ApiModelProperty(value="사업장 보세 구역번호 8자리")
	private String bmsWhCode;

	/** Web XML Send */
	@ApiModelProperty(value="반입 데이터 리스트")
	private List<InventoryIn> bmsCarryInList;

	/** 시스템 로그 - ES */
    @ApiModelProperty(value="BL번호")
    private String blNo;
	@ApiModelProperty(value="오더관리번호")
    private String odJobNo;
    @ApiModelProperty(value="출력용 사업장명")
    private String logUserBusinessPrintName;
    
    /** 일괄반입자료생성 */
    @ApiModelProperty(value="반입일자")
    private String carryinDate;
    @ApiModelProperty(value="반입시간")
    private String carryinTime;
    @ApiModelProperty(value="체화일자")
    private String periodOverDate;
    @ApiModelProperty(value="반출통고일자")
    private String outNoticeDate;
    @ApiModelProperty(value="장치위치")
    private String locationPlace;

}

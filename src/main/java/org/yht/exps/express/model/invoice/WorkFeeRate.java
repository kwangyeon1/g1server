package org.yht.exps.express.model.invoice;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "WorkFeeRate", description = "전자상거래 작업료 요율")
@Alias("workFeeRate")
@EqualsAndHashCode(callSuper=false)
public class WorkFeeRate{
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	@ApiModelProperty(value="대표사업장코드")
	private String entBusinessCode;
	@ApiModelProperty(value="작업자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="정산처")
	private String customerCode;
	@ApiModelProperty(value="정산처Lv")
	private String customerLv;
	@ApiModelProperty(value="정산처하위Lv")
	private String customerLowLv;
	@ApiModelProperty(value="요율시작일자")
	private String startDate;
	@ApiModelProperty(value="요율종료일자")
	private String endDate;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="사용자아이디")
	private String userId;
	@ApiModelProperty(value="정산여부")
	private String ecmInvoiceYn;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}

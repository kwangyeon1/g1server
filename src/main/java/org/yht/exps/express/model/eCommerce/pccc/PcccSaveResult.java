package org.yht.exps.express.model.eCommerce.pccc;

import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "PcccSaveResult", description = "전자상거래 Pccc Save 결과")
@Alias("PcccSaveResult")
public class PcccSaveResult {
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	@ApiModelProperty(value="결과내역")
	private Map<String, Object> resultData;
	@ApiModelProperty(value="결과출력")
	private Map<String, Object> returnMap;
	
	/* 통관부호_검증상태 */
	private String customsIdVerify;
	/* 통관부호_검증오류내역 */
	private String customsIdError;
	/* 통관부호_검증결과 */
	private Boolean result;
	/* 유효기간시작일자 */
	private String customsIdStartDate;
	/* 유효기간만료일자 */
	private String customsIdEndDate;
}

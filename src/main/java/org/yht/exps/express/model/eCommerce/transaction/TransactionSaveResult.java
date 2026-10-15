package org.yht.exps.express.model.eCommerce.transaction;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "transactionSaveResult", description = "Transaction Save 결과")
@Alias("transactionSaveResult")
public class TransactionSaveResult {
	@ApiModelProperty(value="관리번호")
	private String seqNo;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	@ApiModelProperty(value="필수값오류리스트")
	private List<Map<String, Object>> errorList;

	@ApiModelProperty(value="면장관리번호")
	private String transactionJobNo;

	@ApiModelProperty(value="배송요청 전체 카운트")
	private int totalCnt;

	@ApiModelProperty(value="결과내역")
	private Map<String, Object> resultData;
	@ApiModelProperty(value="결과출력")
	private Map<String, Object> returnMap;
}

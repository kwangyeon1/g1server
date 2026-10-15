package org.yht.exps.express.model.systemMng;

import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "qnASaveResultVE", description = "문의사항 Save 결과")
@Alias("qnASaveResultVE")
public class QnASaveResultVE {
	
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	
	@ApiModelProperty(value="관리번호")
	private String referenceNo;

	@ApiModelProperty(value="일련번호")
	private int seqNo;

	@ApiModelProperty(value="관리자 미답변건수")
	private int sUnansweredCnt;
	@ApiModelProperty(value="거래처 미답변건수")
	private int cUnansweredCnt;
	
	@ApiModelProperty(value="결과내역")
	private Map<String, Object> resultData;
	
}
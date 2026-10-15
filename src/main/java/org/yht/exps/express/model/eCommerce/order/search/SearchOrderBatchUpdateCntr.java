package org.yht.exps.express.model.eCommerce.order.search;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SearchOrderBatchUpdateCntr", description = "전자상거래 일괄변경 컨테이너정보")
@Alias("searchOrderBatchUpdateCntr")
public class SearchOrderBatchUpdateCntr{
	@ApiModelProperty(value="컨테이너번호")
	private String cntrNo;
	@ApiModelProperty(value="컨테이너코드")
	private String cntrCode;
	@ApiModelProperty(value="컨테이너EDI코드")
	private String cntrEdiCode;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="수량단위")
	private String unitPieces;
	@ApiModelProperty(value="봉인번호1")
	private String cntrSealNo1;
	@ApiModelProperty(value="봉인번호2")
	private String cntrSealNo2;
	@ApiModelProperty(value="봉인번호3")
	private String cntrSealNo3;
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
}

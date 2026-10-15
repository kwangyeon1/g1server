package org.yht.exps.express.model.eCommerce.transaction;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "TransactionOdList", description = "거래정보 Detail 리스트")
@Alias("transactionOdList")
public class TransactionOdList {
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="거래정보 오더 리스트")
	private List<TransactionDet> transDetList;
	@ApiModelProperty(value="거래정보 그룹 상세 리스트")
	private List<TransactionOdItem> transOdItemList;
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
}

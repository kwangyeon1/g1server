package org.yht.exps.express.model.systemMng.search;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "searchQnADetailVE", description = "문의사항 상세조회 검색 조건")
@Alias("searchQnADetailVE")
public class SearchQnADetailVE {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="관리번호")
	private String referenceNo;
	@ApiModelProperty(value="일련번호")
	private int seqNo;
	@ApiModelProperty(value="거래처코드")
	private String customerCode;
	@ApiModelProperty(value="사용자아이디")
	private String userId;
	@ApiModelProperty(value="날짜포맷")
	private String dateFormat;
}
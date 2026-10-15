package org.yht.exps.express.model.bms.bmsErrorDesc.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchBmsErrorDescList", description = "보세구역관리(BMS) 오류내역 공통 파라미터")
@Alias("SearchBmsErrorDescList")
public class SearchBmsErrorDescList extends SearchList {

	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="진행상태")
	private String lastPodStatus;
	@ApiModelProperty(value="전송타입") 
	private String docType;
	@ApiModelProperty(value="세관제출번호(신고번호 or 화물관리번호)")
	private String declarationNo;
	@ApiModelProperty(value="정정차수")
	private String changeSeqNo;

}
package org.yht.exps.express.model.sms.logs.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchBatchDataLogs", description = "배치 처리 로그 검색 조건")
@Alias("searchBatchDataLogs")
public class SearchBatchDataLogs extends SearchList {
	private String srchEntCode;
	private String srchHblNo;
}

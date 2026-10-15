package org.yht.exps.express.model.mobile.bms.search;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchMobBwis", description = "PDA 검사대상확인 검색")
@Alias("searchMobBwis")
public class SearchMobBwis {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
	@ApiModelProperty(value="검색 번호")
	private String srchNo;
	@ApiModelProperty(value="사용자 아이디")
	private String userId;
	
}

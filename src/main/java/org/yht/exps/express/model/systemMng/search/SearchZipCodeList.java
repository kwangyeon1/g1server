package org.yht.exps.express.model.systemMng.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchZipCodeList", description = "ZipCode조회")
@Alias("SearchZipCodeList")
public class SearchZipCodeList extends SearchList{
	
	@ApiModelProperty(value="우편번호")
	private String srchZipCode;
	@ApiModelProperty(value="브랜치이름코드", example="BranchName1 or BranchName2 or BranchName3 or BranchName4 or BranchName5 or BranchName6")
	private String srchBranchNameCode;
	@ApiModelProperty(value="브랜치이름")
	private String srchBranchName;
}

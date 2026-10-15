package org.yht.exps.express.model.systemMng;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "ZipCode", description = "전자상거래 ZipCode")
@Alias("zipcode")
@EqualsAndHashCode(callSuper=false)
public class ZipCode extends ResultList{
	
	@ApiModelProperty(value="ZipCode")
	private String zipcode;
	@ApiModelProperty(value="BranchName1")
	private String branchName1;
	@ApiModelProperty(value="BranchName2")
	private String branchName2;
	@ApiModelProperty(value="BranchName3")
	private String branchName3;
	@ApiModelProperty(value="BranchName4")
	private String branchName4;
	@ApiModelProperty(value="BranchName5")
	private String branchName5;
	@ApiModelProperty(value="BranchName6")
	private String branchName6;
	@ApiModelProperty(value="CreateDate")
	private String registerDateTime;
	@ApiModelProperty(value="ModifyDate")
	private String updateDateTime;
	
	/** 저장 */
	@ApiModelProperty(value="insert/update구분")
	private String action;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
}

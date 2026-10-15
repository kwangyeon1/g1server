package org.yht.exps.express.model.eCommerce.print;


import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "printInfo", description = "출력물 정보")
@Alias("printInfo")
public class PrintInfo{
	@ApiModelProperty(value="인쇄양식코드")
	private String printFormCode;
	@ApiModelProperty(value="인쇄양식명")
	private String printFormName;
	@ApiModelProperty(value="인쇄양식 문서파일명")
	private String rptFileName;
}

package org.yht.exps.express.model.eCommerce.print;


import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "searchPrintInfo", description = "출력물 정보 조회")
@Alias("searchPrintInfo")
public class SearchPrintInfo{
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="암호화여부")
	private String cryptographyYn;
	@ApiModelProperty(value="사업장코드", example="YHTXX-KR")
	private String userEntBusinessCode;
	@ApiModelProperty(value="인쇄양식 문서파일명")
	private List<String> srchRptFileName;
	@ApiModelProperty(value="인쇄양식 문서코드")
	private String srchPrintFormCode;
	@ApiModelProperty(value="solution")
	private String solution;
	
	@ApiModelProperty(value="")
	private String pageMode;
	@ApiModelProperty(value="")
	private String title;
	@ApiModelProperty(value="")
	private String bound;
	@ApiModelProperty(value="")
	private String dateFormat;
	@ApiModelProperty(value="")
	private String dbDateFormat;
	@ApiModelProperty(value="")
	private String userId;
	@ApiModelProperty(value="")
	private String userName;
	@ApiModelProperty(value="")
	private String gmtTime;
	@ApiModelProperty(value="")
	private String srchLangType;
	@ApiModelProperty(value="")
	private String printFormCode;
	@ApiModelProperty(value="")
	private List<Map<String,Object>> grid;
	@ApiModelProperty(value="")
	private String printOnePage;
	
	@ApiModelProperty(value="")
	private String boundType;
	@ApiModelProperty(value="")
	private int referenceNo;
	@ApiModelProperty(value="")
	private int printNo;
	@ApiModelProperty(value="")
	private String docPrintCount;
}

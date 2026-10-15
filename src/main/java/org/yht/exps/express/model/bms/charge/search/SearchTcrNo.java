package org.yht.exps.express.model.bms.charge.search;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "searchTcrNo", description = "창고료및 작업료 조회 조건")
@Alias("searchTcrNo")
public class SearchTcrNo {
	
    @ApiModelProperty(value="1231 123")
    private String tcrNo;

    @ApiModelProperty(value="도입사코드", example="YHTXX")
	private String srchEntCode;
    @ApiModelProperty(value="도입사_사업장코드")
	private String srchEntBusinessCode;
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	
	@ApiModelProperty(value="옵션코드")
	private String optionCode;
	@ApiModelProperty(value="솔루션")
	private String solution;
	
    @ApiModelProperty(value="gmtTime_기본값 ")
    private String gmtTime;
    @ApiModelProperty(value="날짜형식_현재 날짜 입력시 DATEADD(HOUR, 9, GETDATE()) ")
    private String dateFormat;
    
    @ApiModelProperty(value="수정자코드")
    private  String   updateUserId;
    @ApiModelProperty(value="수정일시")
    private   String  updateDateTime;
    
	
	//시스템 로그
	@ApiModelProperty(value="작업자사업장코드")
	private String userEntBusinessCode;
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}

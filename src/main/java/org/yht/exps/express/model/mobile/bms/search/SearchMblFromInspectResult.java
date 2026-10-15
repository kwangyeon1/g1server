package org.yht.exps.express.model.mobile.bms.search;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.mobile.bms.BmsLocalInspectResult;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchMblFromInspectResult", description = "BMS pda 특송창고검사결과 MBL검색 ")
@Alias("searchMblFromInspectResult")
public class SearchMblFromInspectResult {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	
	@ApiModelProperty(value="옵션코드")
	private String optionCode;
	@ApiModelProperty(value="솔루션")
	private String solution;
	@ApiModelProperty(value="도입사_사업장코드")
	private String entBusinessCode;
	
    @ApiModelProperty(value="gmtTime_기본값 ")
    private String gmtTime;
    @ApiModelProperty(value="날짜형식_현재 날짜 입력시 DATEADD(HOUR, 9, GETDATE()) ")
    private String dateFormat;
    
    @ApiModelProperty(value="수정자코드")
    private String   updateUserId;
    @ApiModelProperty(value="수정일시")
    private String  updateDateTime;

	@ApiModelProperty(value="BL/NO(하우스번호)")
	private String blNo;
	@ApiModelProperty(value="MBL/NO(마스터번호)")
	private String mblNo;
	
	@ApiModelProperty(value="반입번호")
	private String inNo;
	
	@ApiModelProperty(value="검사코드 그룹")
	private String codeGroup;
	
	@ApiModelProperty(value="update할 정보객체 배열")
	private List<BmsLocalInspectResult> updateInfoList;
	
	//시스템 로그
	@ApiModelProperty(value="작업자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
	
}

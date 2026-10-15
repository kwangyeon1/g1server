package org.yht.exps.express.model.mobile.bms.search;


import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "SearchMobBigSizeScan", description = "PDA 거대화물스캔로그 검색 ")
@Alias("searchMobBigSizeScan")
public class SearchMobBigSizeScan extends SearchList {
	
	@ApiModelProperty(value="오더 번호")
	private String odJobNo;
	@ApiModelProperty(value="검색 번호")
	private String srchNo;
	@ApiModelProperty(value="장치 위치")
	private String whLocation;
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="옵션코드")
	private String optionCode;
	@ApiModelProperty(value="솔루션")
	private String solution;
	@ApiModelProperty(value="도입사_사업장코드")
	private String entBusinessCode;
	
    @ApiModelProperty(value="gmtTime_기본값 ")
    private String gmtTime;
    
    @ApiModelProperty(value="수정자코드")
    private  String   updateUserId;
    @ApiModelProperty(value="수정일시")
    private   String  updateDateTime;
    
	
	@ApiModelProperty(value="홀드여부")
	private String holdYn;
	@ApiModelProperty(value="BL/NO(하우스번호)")
	private String blNo;
	@ApiModelProperty(value="스캔결과검색")
	private String srchScanResult;
	@ApiModelProperty(value="스캔날짜검색")
	private String srchScanDate;
	@ApiModelProperty(value="거대여부검색")
	private String srchBigSizeYnInput;
	
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

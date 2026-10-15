package org.yht.exps.express.model.bms;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "UpsNewbilling", description = "전자상거래 UPS Newbilling")
@Alias("upsNewbilling")
public class UpsNewbilling{
	@ApiModelProperty(value="오더관리번호")
	private String odJobNo;
	@ApiModelProperty(value="오더관리번호리스트")
	private List<String> odJobNoList;
	@ApiModelProperty(value="암호화여부")
	private String cryptographyYn;
	@ApiModelProperty(value="마스터오더관리번호")
	private String masterOdJobNo;
	@ApiModelProperty(value="목록신고제출번호")
	private String simpleDocNo;
	@ApiModelProperty(value="목록신고제출번호리스트")
	private List<String> simpleDocNoList;
	@ApiModelProperty(value="통관타입")
	private String cdType;
	@ApiModelProperty(value="처리타입")
	private String action;
	@ApiModelProperty(value="반입일자")
	private String carryinDate;
	@ApiModelProperty(value="리스트")
	private List<Map<String, Object>> dataList;
	@ApiModelProperty(value="리스트")
	private List<Map<String, Object>> dataList2;
	private String masterImpChk;
	@ApiModelProperty(value="메뉴ID")
	private String menuId;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
	
	
	
	
	
	

    
    
    
    
    
    
    
//  김일호 =====================================================================================================
    
    
    
    
    
    
    
    
//  이슬기 =====================================================================================================
    @ApiModelProperty(value="사업장")
    private String srchEntBusinessCode;
    
    
    
    
    
    
    
   
}

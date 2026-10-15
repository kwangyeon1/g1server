package org.yht.exps.express.model.commonMng.stock;



import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel(value = "RegrantStock", description = "전자상거래  Stock 재부여 ")
@Alias("regrantStock")
public class RegrantStock {

	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;	
	@ApiModelProperty(value="도입사_사업장코드")
	private String entBusinessCode;		
	@ApiModelProperty(value="도입사_사업장코드")
	private String userBusinessCode;		
	@ApiModelProperty(value="seaNo", example="1")
	private int seqNo;		
	@ApiModelProperty(value="배송사", example="KOREX OR EMS")
	private String trCode;	
	@ApiModelProperty(value="바운드", example="AIR OR SEA")
	private String bound;		
	@ApiModelProperty(value="바운드타입", example="EXP OR IMP")
	private String boundType;	
	@ApiModelProperty(value="대리점", example="aaaa")
	private String srchAgentCode;		
	@ApiModelProperty(value="상위 스탁의 스탁시작번호", example="346681511686")
	private String origStartNo;		
	@ApiModelProperty(value="상위 스탁의 스탁종료번호", example="346681512725")
	private String origEndNo;		
	@ApiModelProperty(value="등록자")
	private String inputUserId;		
	@ApiModelProperty(value="수정유저")
	private String updateUserId;		
	@ApiModelProperty(value="등록일자", example="2021-06-25")
	private String registerDateTime;	
	@ApiModelProperty(value="대리점", example="YHTXX")
	private String agentCode;	
	@ApiModelProperty(value="대리점이름", example="유한")
	private String agentName;		
	@ApiModelProperty(value="재부여할 건수", example="100")
	private String regrantCount;	
	@ApiModelProperty(value="GMT", example="9")
	private int gmtTime;
	@ApiModelProperty(value="스탁시작번호", example="346681511686")
	private String startNo;		
	@ApiModelProperty(value="스탁끝번호", example="346681512725")
	private String endNo;	
	@ApiModelProperty(value="스탁사용여부", example="Y OR N")
	private String stockUseYn;
	@ApiModelProperty(value="EMS용 Stock 번호", example="Y OR N")
	private String emsStockNo;
	

	@ApiModelProperty(value="스탁 일자")
	private String stockDate;
	@ApiModelProperty(value="스탁 재부여 일자")
	private String reStockDate;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;	
		
}


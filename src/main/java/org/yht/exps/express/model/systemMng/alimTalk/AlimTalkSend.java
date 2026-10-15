package org.yht.exps.express.model.systemMng.alimTalk;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "AlimTalkSend", description = "시스템관리 알림톡 전송")
@Alias("alimTalkSend")
public class AlimTalkSend{
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="암호화여부")
	private String cryptographyYn;
	@ApiModelProperty(value="조회일자구분", example="OB_DATE")
	private String srchDateGbn;
	@ApiModelProperty(value="조회시작일", example="2021-01-01")
	private String srchStartDate;
	@ApiModelProperty(value="조회종료일", example="2021-01-01")
	private String srchEndDate;
	@ApiModelProperty(value="조회일자타입", example="23")
	private String srchDateFormat;
	@ApiModelProperty(value="항공/해상,수출/수입구분", example="AE")
	private String srchBoundType;
	@ApiModelProperty(value="콘솔여부", example="Y or N")
	private String srchConsolYn;
	@ApiModelProperty(value="출력여부")
	private String srchPrintYn;	
	@ApiModelProperty(value="진행구분", example="")
	private String srchStatus;		
	@ApiModelProperty(value="통관구분")
	private String srchCdType;	
	@ApiModelProperty(value="발송보류여부")
	private String srchSendYn;
	@ApiModelProperty(value="통관부호 검증상태")
	private String srchCusIdVerify;
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;	
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회업체타입")
	private String srchCusType;
	@ApiModelProperty(value="검색거래처코드", example="거래처코드")
	private String srchCustomerCode;	
	@ApiModelProperty(value="검색거래처명", example="거래처명")
	private String srchCustomerName;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;	
	@ApiModelProperty(value="mode", example="check, search")
	private String mode;	
	@ApiModelProperty(value="mode", example="E01")
	private String templateType;
	
	@ApiModelProperty(value="mode", example="E01")
	private String alreadySendYn;
	
	@ApiModelProperty(value="하우스배열")
	String hblNoArr;
	@ApiModelProperty(value="관리번호배열")
	String odJobNoArr;
	
	@ApiModelProperty(value="하우스리스트")
	private List<String> hblNoList;
	

}

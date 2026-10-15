package org.yht.exps.express.model.edi;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SendDelivery", description = "적하,통관목록신고")
@Alias("sendDelivery")
public class SendDelivery{
	
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	@ApiModelProperty(value="작성자아이디")
	private String registerUserId;
	@ApiModelProperty(value="작성자명")
	private String registerUserName;
	@ApiModelProperty(value="GMT")
	private String gmtTime;
	@ApiModelProperty(value="도입사코드 5자리 + 국가 2자리")
	private String entBusinessCode;
	@ApiModelProperty(value="SOLUTION")
	private String solution;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="중계업체")
	private String ediVender;
	@ApiModelProperty(value="전송구분")
	private String sendType;
	
//	@ApiModelProperty(value="마스터오더관리번호")
//	private String masterOdJobNo;
//	@ApiModelProperty(value="적하목록정정관리번호리스트")
//	private List<String> modJobNoList;
	@ApiModelProperty(value="수정자아이디")
	private String updateUserId;
	@ApiModelProperty(value="실행타입")
	private String action;
//	@ApiModelProperty(value="도입사계약일자")
//	private String contractDate;
	
	@ApiModelProperty(value="edi리스트")
	private List<Map<String, Object>> ediList;
}


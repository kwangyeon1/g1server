package org.yht.exps.express.model.eCommerce.consol;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ConsolClose", description = "전자상거래 마감")
@Alias("consolClose")
public class ConsolClose {
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="마스터관리번호리스트")
	private List<String> masterOdJobNoList;
	@ApiModelProperty(value="사용자 언어")
	private String userLanguage;
	@ApiModelProperty(value="마감여부")
	private String finishYn;
	@ApiModelProperty(value="action")
	private String action;
	@ApiModelProperty(value="거래처코드")
	private String ecmCustomerCode;
	
	/** 저장 */
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="작업자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="대표사업장코드")
	private String headBusinessCode;
	@ApiModelProperty(value="출발지코드")
	private String origCode;
	@ApiModelProperty(value="목적항")
	private String destPort;
	@ApiModelProperty(value="진행상태코드")
	private String podStatus;
	@ApiModelProperty(value="목록선별자_아이디")
	private String selectorUserId;
	@ApiModelProperty(value="선별물품확인자_아이디")
	private String confirmerUserId;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}
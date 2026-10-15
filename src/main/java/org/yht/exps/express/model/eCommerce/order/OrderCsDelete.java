package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "OrderCsDelete", description = "전자상거래 고객관리 삭제")
@Alias("orderCsDelete")
public class OrderCsDelete {
	@ApiModelProperty(value="일련번호")
	private String seqNoId;
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="관리번호")
	private String refJobNo;
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="작업자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}
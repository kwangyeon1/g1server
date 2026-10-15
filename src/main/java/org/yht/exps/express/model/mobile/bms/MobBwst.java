package org.yht.exps.express.model.mobile.bms;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MobBwst", description = "보세구역관리  모바일  재고실사처리 Detail")
@Alias("mobBwst")
public class MobBwst {
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	@ApiModelProperty(value="하우스빌번호")
	private String blNo;
	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;
	
	@ApiModelProperty(value="반입일자")
	private String carryinDateTime;
	@ApiModelProperty(value="적용여부")
	private String applyYn;
	@ApiModelProperty(value="SCAN 수량")
	private int scanCt;
	
	/** 저장 */
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	
}

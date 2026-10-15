package org.yht.exps.express.model.mobile.od;


import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MobEcodCarryout", description = "전자상거래  모바일  Order Carryout 리스트")
@Alias("mobEcodCarryout")
public class MobEcodCarryout {
	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="5자리 도입사코드")
	private String entCode;
	
	
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="수량")
	private int pieces;	
	@ApiModelProperty(value="통관타입")
	private String cdType;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="반출_상태(P : 출고예정,C : 출고취소,F : 출고완료)")
	private String carryoutStatus;
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	@ApiModelProperty(value="반입일자")
	private String carryinDate;
	@ApiModelProperty(value="반출여부")
	private String carryoutYn;
	
	/** 저장 */
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력자코드")
	private String carryoutUserId;


}

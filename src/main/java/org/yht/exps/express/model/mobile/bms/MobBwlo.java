package org.yht.exps.express.model.mobile.bms;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MobBwlo", description = "보세구역관리  모바일  로케이션적치 Detail")
@Alias("mobBwlo")
public class MobBwlo {
	
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
	
	@ApiModelProperty(value="장치위치")
	private String locationPlace;
	@ApiModelProperty(value="장치위치명")
	private String locationPlaceName;
	@ApiModelProperty(value="반입일자")
	private String carryinDateTime;
	@ApiModelProperty(value="선별결과코드명")
	private String seInspectionCodeName;
	@ApiModelProperty(value="XRAY검사코드명")
	private String xrayInspectionCodeName;
	@ApiModelProperty(value="통관검사계획코드명")
	private String paln5emInspectionCodeName;
	@ApiModelProperty(value="스캔여부")
	private String scanYn;
	
	@ApiModelProperty(value="재고수량")
	private int stockCt;
	@ApiModelProperty(value="재고중량")
	private double stockWt;
	
	/** 저장 */
	@ApiModelProperty(value="언어", example="KOR")
	private String userLanguage;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	
}

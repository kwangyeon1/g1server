package org.yht.exps.express.model.sms.holy;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "Holyday", description = "공휴일관리")
@Alias("Holyday")
public class Holyday {
	@ApiModelProperty(value="조회건수")
	private long total;
	@ApiModelProperty(value="번호")
	private int num;
	
	@ApiModelProperty(value="관리번호")
	private String seqNoId;
	@ApiModelProperty(value="도입사")
	private String entCode;
	@ApiModelProperty(value="국가")
	private String country;
	@ApiModelProperty(value="국가")
	private String countryName;
	@ApiModelProperty(value="공휴일타입")
	private String holydayType;
	@ApiModelProperty(value="공휴일타입")
	private String holydayTypeName; 
	@ApiModelProperty(value="공휴일명")
	private String holydayName;
	@ApiModelProperty(value="공휴일 시작일자")
	private String startDate;
	@ApiModelProperty(value="공휴일 종료일자")
	private String endDate;
	@ApiModelProperty(value="비고")
	private String remarks;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력자명")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="수정자명")
	private String updateUserName;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
}

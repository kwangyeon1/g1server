package org.yht.exps.express.model.edi;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "CarryoutInHouse", description = "전자상거래  적하목록수입 하우스 리스트")
@Alias("carryoutInHouse")
public class CarryoutInHouse {
	
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="MSN")
	private String msn;
	@ApiModelProperty(value="HSN")
	private String hsn;
	@ApiModelProperty(value="수신결과")
	private String result;
	@ApiModelProperty(value="수리일자(통관완료일)")
	private String clearanceFinishDate;
	@ApiModelProperty(value="반출일자")
	private String carryoutDate;
	@ApiModelProperty(value="반입일자")
	private String carryinDate;
	@ApiModelProperty(value="코드")
	private String ediCustomerCode;
	@ApiModelProperty(value="창고부호")
	private String assignmentWhCode;
	@ApiModelProperty(value="업체코드")
	private String b2gForwarderSign;
	@ApiModelProperty(value="면허개수")
	private String pieces;
	@ApiModelProperty(value="면허중량")
	private String gwt;
	@ApiModelProperty(value="입항일")
	private String arDate;
	@ApiModelProperty(value="업체명(수하인 이름)")
	private String consigneeNameLoc;
	@ApiModelProperty(value="홀드사유")
	private String holdReason;
	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;
	
}


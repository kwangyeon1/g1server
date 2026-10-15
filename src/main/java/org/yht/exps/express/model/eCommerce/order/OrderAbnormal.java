package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "OrderAbnormal", description = "전자상거래 Order 하기이상결과보고")
@Alias("orderAbnormal")
public class OrderAbnormal {
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="일련번호")
	private String seqNoId;
	@ApiModelProperty(value="이례사항")
	private String abnormalCode;
	@ApiModelProperty(value="MRN")
	private String mrn;
	@ApiModelProperty(value="MSN")
	private String msn;
	@ApiModelProperty(value="편명")
	private String vesselFlight;
	@ApiModelProperty(value="도착항")
	private String discWhCode;
	@ApiModelProperty(value="입항일")
	private String arDate;
	@ApiModelProperty(value="하기수량")
	private String pieces;
	@ApiModelProperty(value="하기중량")
	private String gwt;
	@ApiModelProperty(value="하기이상_사고개수")
	private String cnt;
	@ApiModelProperty(value="하기이상보고_신고일시")
	private String sendDate;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
}

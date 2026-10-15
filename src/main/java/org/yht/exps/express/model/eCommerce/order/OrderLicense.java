package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;
import org.yht.util.Util;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@ApiModel(value = "OrderLicense", description = "전자상거래 Order 면장정보")
@Alias("orderLicense")
public class OrderLicense implements Cloneable{
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="면장번호")
	private String licenseNo;
	@ApiModelProperty(value="수량")
	private double pieces;
	@ApiModelProperty(value="수량단위")
	private String unitPieces;
	@ApiModelProperty(value="중량")
	private double gwt;
	@ApiModelProperty(value="중량단위")
	private String unitWeight;
	@ApiModelProperty(value="동시포장여부")
	private String combineYn;
	@ApiModelProperty(value="동시포장구분")
	private String combineTypeAZ;
	@ApiModelProperty(value="동사포장갯수")
//	private int combinePieces;
	private double combinePieces;
	@ApiModelProperty(value="분할선적여부")
	private String partialYn;
	@ApiModelProperty(value="분할선척차수")
//	private int partialSeq;
	private double partialSeq;
	@ApiModelProperty(value="수출화주신고인부호")
	private String efnSenderCode;
	@ApiModelProperty(value="주문번호")
	private String efnPoNo;
	@ApiModelProperty(value="운송장번호")
	private String efnDeliveryNo;
	@ApiModelProperty(value="수출이행신고신고번호")
	private String efnDocNo;
	@ApiModelProperty(value="수출이행신고전송구분(01:등록,02:수정,03:삭제)")
	private String efnType;
	@ApiModelProperty(value="EDI전송상태")
	private String ediStatus;
	@ApiModelProperty(value="송수신일시")
	private String ediDateTime;
	@ApiModelProperty(value="입력자아이디")
	private String registerUserId;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	
	public OrderLicense clone() throws CloneNotSupportedException{
		OrderLicense vo = null;
		try {
			vo = (OrderLicense) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error(Util.getPrintStacTraceString(e));
			throw e;
		}
		return vo;
	}
}

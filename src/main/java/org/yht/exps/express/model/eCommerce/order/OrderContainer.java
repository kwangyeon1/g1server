package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;
import org.yht.util.Util;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@ApiModel(value = "OrderContainer", description = "전자상거래 Order 컨테이너")
@Alias("orderContainer")
public class OrderContainer implements Cloneable{
	@ApiModelProperty(value="Job No")
	private String odJobNo;
	@ApiModelProperty(value="컨테이너번호")
	private String cntrNo;
	@ApiModelProperty(value="컨테이너봉인번호 1")
	private String cntrSealNo1;
	@ApiModelProperty(value="컨테이너봉인번호 2")
	private String cntrSealNo2;
	@ApiModelProperty(value="컨테이너봉인번호 3")
	private String cntrSealNo3;
	@ApiModelProperty(value="컨테이너코드")
	private String cntrCode;
	@ApiModelProperty(value="컨테이너EDI코드")
	private String cntrEdiCode;
	@ApiModelProperty(value="컨테이너 코드기분 크기분류 20, 40, 45")
	private String cntrSize;
	@ApiModelProperty(value="CSI 신고시 필수항목으로 사용된다.")
	private String cntrSubCode;
	@ApiModelProperty(value="CSI 신고시 필수항목으로 사용된다. L : LOAD, E:EMPTY")
	private String cntrLoadTypeEl;
	@ApiModelProperty(value="수량")
//	private Integer pieces;
	private double pieces;
	@ApiModelProperty(value="단위수량")
	private String unitPieces;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="단위중량")
	private String unitWeight;
	@ApiModelProperty(value="용적")
	private double cbm;
	@ApiModelProperty(value="LBS")
	private double gwtLbs;
	@ApiModelProperty(value="CFT")
	private double cbmCft;
	@ApiModelProperty(value="마스터빌의 FCL 컨테이너 중 하우스에 분할(PART) 되어있는지 여부를 기재한다.")
	private String cntrPartYn;
	
	public OrderContainer clone() throws CloneNotSupportedException{
		OrderContainer vo = null;
		try {
			vo = (OrderContainer) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error(Util.getPrintStacTraceString(e));
			throw e;
		}
		return vo;
	}
}

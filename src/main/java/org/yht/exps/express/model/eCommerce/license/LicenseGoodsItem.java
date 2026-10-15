package org.yht.exps.express.model.eCommerce.license;

import java.math.BigDecimal;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "LicenseGoodsItem", description = "전자상거래 면장 - 란 상세정보")
@Alias("licenseGoodsItem")
public class LicenseGoodsItem extends ResultList{
	@ApiModelProperty(value="관리번호")
	private String licenseJobNo;

	@ApiModelProperty(value="란번호")
	private String seqNoId;

	@ApiModelProperty(value="HS CODE")
	private String hsCode;

	@ApiModelProperty(value="브랜드")
	private String brand;

	@ApiModelProperty(value="품명")
	private String detailItemName;

	@ApiModelProperty(value="상세수량")
	private int pieces;

	@ApiModelProperty(value="상세수량 단위")
	private String unitPieces;

	@ApiModelProperty(value="순중량")
	private BigDecimal nwt;

	@ApiModelProperty(value="순중량 단위")
	private String unitWeight;

	@ApiModelProperty(value="포장개수")
	private int packingPieces;
	
	@ApiModelProperty(value="포장개수단위")
	private String unitPacking;
	
	@ApiModelProperty(value="신고가격(원화)")
	private BigDecimal declarationAmountLoc;
	
	@ApiModelProperty(value="신고가격(외화)")
	private BigDecimal declarationAmountCur;
	
	@ApiModelProperty(value="신고가격(USD)")
	private BigDecimal declarationAmountUsd;
	
	@ApiModelProperty(value="결제금액")
	private BigDecimal licensePayAmountLoc;

	@ApiModelProperty(value="H.B/L No")
	private String hblNo;
	
	@ApiModelProperty(value="원산지국가")
	private String originCountry;
	
	@ApiModelProperty(value="조정금액")
	private BigDecimal adjustAmountLoc;
	
	@ApiModelProperty(value="제조자구분")
	private String makerType;
	
	@ApiModelProperty(value="환급")
	private String autoRefundType;
	
	@ApiModelProperty(value="통화")
	private String  currencyCode;
	
	@ApiModelProperty(value="단가")
	private String unitPrice;
	
	@ApiModelProperty(value="금액")
	private String unitAmount;
	
	@ApiModelProperty(value="상품코드")
	private String itemCode;

	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="수정자")
	private String updateUserName;
	
	@ApiModelProperty(value="저장타입")
	private String 	action;
	@ApiModelProperty(value="수정자")
	private String 	updateUserId;
	@ApiModelProperty(value="시간")
	private String 	gmtTime;
	
	@ApiModelProperty(value="Count")
	private int count;
}

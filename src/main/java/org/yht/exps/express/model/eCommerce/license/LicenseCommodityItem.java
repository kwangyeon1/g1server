package org.yht.exps.express.model.eCommerce.license;

import java.math.BigDecimal;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "LicenseCommodityItem", description = "전자상거래 면장 - 규격 상세정보")
@Alias("licenseCommodityItem")
public class LicenseCommodityItem extends ResultList{
	@ApiModelProperty(value="관리번호")
	private String licenseJobNo;

	@ApiModelProperty(value="란번호")
	private String seqNoId;

	@ApiModelProperty(value="순번")
	private String cmditySeqNoId;

	@ApiModelProperty(value="브랜드")
	private String brand;

	@ApiModelProperty(value="품명")
	private String detailItemName;

	@ApiModelProperty(value="상세수량")
	private int pieces;

	@ApiModelProperty(value="상세수량 단위")
	private String unitPieces;

	@ApiModelProperty(value="상품ID")
	private String itemCode;
	
	@ApiModelProperty(value="단가")
	private BigDecimal unitPrice;
	
	@ApiModelProperty(value="단가(외화)")
	private BigDecimal unitPriceCur;
	
	@ApiModelProperty(value="단가(외화)")
	private BigDecimal unitPriceLoc;
	
	@ApiModelProperty(value="금액(외화)")
	private BigDecimal amountCur;
	
	@ApiModelProperty(value="금액(외화)")
	private BigDecimal declarationAmountCur;
	
	@ApiModelProperty(value="금액(외화)")
	private BigDecimal amountLoc;
	
	@ApiModelProperty(value="조정금액")
	private BigDecimal adjustAmountLoc;
	
	@ApiModelProperty(value="순중량")
	private BigDecimal nwt;
	
	@ApiModelProperty(value="순중량단위")
	private String unitWeight;
	
	@ApiModelProperty(value="URL")
	private String itemUrl;
	
	@ApiModelProperty(value="HS CODE")
	private String hsCode;
	
	@ApiModelProperty(value="통화단위")
	private String 	currencyCode;
	
	@ApiModelProperty(value="규격")
	private String 	itemSize;
	
	@ApiModelProperty(value="원산지국가")
	private String originCountry;
	

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

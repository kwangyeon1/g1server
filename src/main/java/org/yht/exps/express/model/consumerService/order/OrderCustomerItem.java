package org.yht.exps.express.model.consumerService.order;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "OrderCustomerItem", description = "전자상거래 Order Item")
@Alias("orderCustomerItem")
public class OrderCustomerItem {
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="일련번호")
	private String seqNoId;
	@ApiModelProperty(value="품명")
	private String detailItemName;
	@ApiModelProperty(value="브랜드")
	private String brand;
	@ApiModelProperty(value="원산지국가코드")
	private String originCountry;
	@ApiModelProperty(value="수량")
	private Integer pieces;
	@ApiModelProperty(value="단위_수량")
	private String unitPieces;
	@ApiModelProperty(value="총중량")
	private Double gwt;
	@ApiModelProperty(value="볼륨중량")
	private Double vwt;
	@ApiModelProperty(value="HS코드")
	private String hsCode;
	@ApiModelProperty(value="이미지파일")
	private Object imageFile;
	@ApiModelProperty(value="홈페이지")
	private String ecmUrl;
	@ApiModelProperty(value="택배번호")
	private String deliveryNo;
	@ApiModelProperty(value="단가")
	private Double unitPrice;
	@ApiModelProperty(value="금액_AMOUNT")
	private Double amount;
	@ApiModelProperty(value="통화단위")
	private String currencyCode;
	@ApiModelProperty(value="비고")
	private String remarks;
	
	@ApiModelProperty(value="가로")
	private Integer dimWidth;
	@ApiModelProperty(value="세로")
	private Integer dimLength;
	@ApiModelProperty(value="높이")
	private Integer dimHeight;
	
	private String customsId;
    private String customsTempNo;
    private String categoryName1;
    private String categoryName2;
    private String categoryName3;
    private String categoryName4;
    private String categoryName5;
    private String categoryName6;
    private String categoryName7;
    private String categoryName8;
    private String categoryName9;
    private String optionName;
    private String sellerName;
    private String sellerId;
    private String sellerCode;
    private String itemMafName;
    private String itemMafModelNo;
    private String itemBrandName;
    private String itemId;
    private String orderNo;
    private String orderEndDatetime;
	
}

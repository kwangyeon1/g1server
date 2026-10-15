package org.yht.exps.express.model.eCommerce.transaction;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "TransactionOdItem", description = "거래정보 그룹 상세 정보")
@Alias("transactionOdItem")
public class TransactionOdItem extends ResultList {
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="주문번호")
	private String orderNo;
	@ApiModelProperty(value="수하인 개인통관고유부호")
	private String customsId;
	@ApiModelProperty(value="일회용인증번호")
	private String customsTempNo;
	@ApiModelProperty(value="검증(개인통관부호)")
	private String customsIdVerify;
	@ApiModelProperty(value="일회용인증번호 재요청여부")
	private String customsTempNoYn;
	@ApiModelProperty(value="상품일련번호")
	private String itemNo;
	@ApiModelProperty(value="상품분류명1단계")
	private String categoryName1;
	@ApiModelProperty(value="상품분류명2단계")
	private String categoryName2;
	@ApiModelProperty(value="HS Code")
	private String hsCode;
	@ApiModelProperty(value="상품명")
	private String detailItemName;
	@ApiModelProperty(value="상품판매페이지 URL")
	private String ecmUrl;
	@ApiModelProperty(value="상품 주문수량")
	private String pieces;
	@ApiModelProperty(value="단가")
	private String unitPrice;
	@ApiModelProperty(value="상품단가 통화 단위")
	private String currencyCode;
	@ApiModelProperty(value="주문금액")
	private String amountCur;
	@ApiModelProperty(value="주문금액 통화 단위")
	private String amountCurrencyCode;
}
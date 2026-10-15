package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import org.yht.util.Util;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@ApiModel(value = "OrderItem", description = "전자상거래 Order Item")
@Alias("orderItem")
@EqualsAndHashCode(callSuper=false)
public class OrderItem extends ResultList implements Cloneable{
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="일련번호")
//	private int seqNoId;
	private double seqNoId;
	@ApiModelProperty(value="품명")
	private String detailItemName;
	@ApiModelProperty(value="품명(Local)")
	private String detailItemNameLoc;
	@ApiModelProperty(value="브랜드")
	private String brand;
	@ApiModelProperty(value="원산지국가코드")
	private String originCountry;
	@ApiModelProperty(value="수량")
//	private Integer pieces;
	private double pieces;
	@ApiModelProperty(value="단위_수량")
	private String unitPieces;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="화물규격_가로")
	private double dimWidth;
	@ApiModelProperty(value="화물규격_길이")
	private double dimLength;
	@ApiModelProperty(value="화물규격_높이")
	private double dimHeight;
	@ApiModelProperty(value="볼륨중량")
	private double vwt;
	@ApiModelProperty(value="HS코드")
	private String hsCode;
	@ApiModelProperty(value="이미지파일")
	private Object imageFile;
	@ApiModelProperty(value="홈페이지")
	private String ecmUrl;
	@ApiModelProperty(value="택배번호")
	private String deliveryNo;
	@ApiModelProperty(value="단가")
	private double unitPrice;
	@ApiModelProperty(value="금액_AMOUNT")
	private double amount;
	@ApiModelProperty(value="통화단위")
	private String currencyCode;
	@ApiModelProperty(value="비고")
	private String remarks;
	
	// DKX용
	@ApiModelProperty(value="개인통관부호")
	private String customsId;
    @ApiModelProperty(value="일회용인증번호")
    private String customsTempNo;
    @ApiModelProperty(value="상품 분류명 1단계")
    private String categoryName1;
    @ApiModelProperty(value="상품 분류명 2단계")
    private String categoryName2;
    @ApiModelProperty(value="상품 분류명 3단계")
    private String categoryName3;
    @ApiModelProperty(value="상품 분류명 4단계")
    private String categoryName4;
    @ApiModelProperty(value="상품 분류명 5단계")
    private String categoryName5;
    @ApiModelProperty(value="상품 분류명 6단계")
    private String categoryName6;
    @ApiModelProperty(value="상품 분류명 7단계")
    private String categoryName7;
    @ApiModelProperty(value="상품 분류명 8단계")
    private String categoryName8;
    @ApiModelProperty(value="상품 분류명 9단계")
    private String categoryName9;
    @ApiModelProperty(value="선택옵션명")
    private String optionName;
    @ApiModelProperty(value="판매업체명")
    private String sellerName;
    @ApiModelProperty(value="판매업체아이디")
    private String sellerId;
    @ApiModelProperty(value="판매업체부호")
    private String sellerCode;
    @ApiModelProperty(value="상품제조사명")
    private String itemMafName;
    @ApiModelProperty(value="상품 제조사 모델번호")
    private String itemMafModelNo;
    @ApiModelProperty(value="상품 브랜드명")
    private String itemBrandName;
    @ApiModelProperty(value="상품 아이디")
    private String itemId;
    @ApiModelProperty(value="주문번호")
    private String orderNo;
    @ApiModelProperty(value="주문완료일자")
    private String orderEndDatetime;
    @ApiModelProperty(value="상품일련번호")
    private String itemNo;

    @ApiModelProperty(value="개인통관부호 검증결과")
    private String customsIdVerify;
    @ApiModelProperty(value="개인통관부호 검증내용")
    private String customsIdError;
    @ApiModelProperty(value="거래정보 오류내역ㄱ")
	private String ecmGovError;
    @ApiModelProperty(value="요청번호(인증번호발급)")
    private String ecmGovReqNo;
	
    @ApiModelProperty(value="profile명")
    private String profileName;
    
	/** 엑셀업로드용 */
	@ApiModelProperty(value="중량단위")
	private String gwtType;
	
	public OrderItem clone() throws CloneNotSupportedException{
		OrderItem vo = null;
		try {
			vo = (OrderItem) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error(Util.getPrintStacTraceString(e));
			throw e;
		}
		return vo;
	}
}

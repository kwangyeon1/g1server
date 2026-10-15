package org.yht.exps.express.model.invoice.search;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "Easypay", description = "이지페이")
@Alias("easypay")
public class Easypay extends SearchList {
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="결제상태")
	private String paymentStatus;
	@ApiModelProperty(value="청구서관리번호")
	private String invoiceJobNo;
	@ApiModelProperty(value="청구서번호")
	private String invoiceNo;
	@ApiModelProperty(value="청구일자")
	private String invoiceDate;
	@ApiModelProperty(value="청구처")
	private String accountCode;
	@ApiModelProperty(value="조회비엘번호")
	private String srchBlNo;
	@ApiModelProperty(value="송하인명")
	private String shipperName;
	@ApiModelProperty(value="송하인주소")
	private String shipperAddress;
	@ApiModelProperty(value="수하인명")
	private String consigneeName;
	@ApiModelProperty(value="수하인주소")
	private String consigneeAddress;
	@ApiModelProperty(value="품명")
	private String itemName;
	@ApiModelProperty(value="수량")
	private double pieces;
	@ApiModelProperty(value="중량")
	private double gwt;
	
	
	
	@ApiModelProperty(value="결제요청일자")
	private String approvalReqDate;
	@ApiModelProperty(value="가맹점 트랜잭션 ID")
	private String shopTransactionId;
	@ApiModelProperty(value="PG 승인 거래번호")
	private String pgCno;
	@ApiModelProperty(value="결제금액")
	private double amount;
	@ApiModelProperty(value="상품명")
	private String goodsName;
	@ApiModelProperty(value="상점 주문번호")
	private String shopOrderNo;
	@ApiModelProperty(value="상점 주문번호(암호화)")
	private String encryptShopOrderNo;
	@ApiModelProperty(value="결과코드")
	private String resCd;
	@ApiModelProperty(value="결과메시지")
	private String resMsg;
	@ApiModelProperty(value="인증응답 URL")
	private String returnUrl;
	@ApiModelProperty(value="결제수단 코드")
	private String payMethodTypeCode;
	@ApiModelProperty(value="고객 결제 단말")
	private String deviceTypeCode;
	@ApiModelProperty(value="은행명")
	private String bankName;
	@ApiModelProperty(value="계좌번호")
	private String accountNo;
	@ApiModelProperty(value="예금주")
	private String depositName;
	
	
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}

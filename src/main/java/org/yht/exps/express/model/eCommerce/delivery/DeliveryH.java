package org.yht.exps.express.model.eCommerce.delivery;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "DeliveryH", description = "전자상거래 Delivery-H")
@Alias("deliveryH")
public class DeliveryH {
	
	@ApiModelProperty(value="배송상태")
	private String deliveryStatus;	
	@ApiModelProperty(value="배송상태명")
	private String deliveryStatusName;	
	@ApiModelProperty(value="목록구분")
	private String cdType;	
	@ApiModelProperty(value="운송사코드")
	private String trCode;
	@ApiModelProperty(value="배송사코드")
	private String deliveryCode;
	@ApiModelProperty(value="오더일자")
	private String poDate;		
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="출발지코드")
	private String origCode;
	@ApiModelProperty(value="도착지코드")
	private String destCode;
	@ApiModelProperty(value="오더번호")
	private String poNo;		
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;	
	@ApiModelProperty(value="수하인명")
	private String consigneeName;
	@ApiModelProperty(value="수하인_주소")
	private String consigneeAddress;
	@ApiModelProperty(value="수하인명(L)")
	private String consigneeNameLoc;
	@ApiModelProperty(value="수하인_주소(L)")
	private String consigneeAddressLoc;
	@ApiModelProperty(value="수하인_우편번호")
	private String consigneeZipcode;	
	@ApiModelProperty(value="수하인_전화번호")
	private String consigneeTel;	
	@ApiModelProperty(value="수하인_휴대전화")
	private String consigneeHp;	
	@ApiModelProperty(value="품명")
	private String commodityItem;	
	@ApiModelProperty(value="변경주소")
	private String deliveryAddressChanged;
	@ApiModelProperty(value="변경배송사이름")
	private String deliveryNameChanged;
	@ApiModelProperty(value="변경운송장번호")
	private String deliveryNoChanged;	
	@ApiModelProperty(value="결제구분")
	private String deliveryPaymentType;	
	@ApiModelProperty(value="배송비")
	private double deliveryFee;
	@ApiModelProperty(value="화주명")
	private String shipperName;	
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;	
	@ApiModelProperty(value="관리번호")
	private String odJobNo;	
	@ApiModelProperty(value="미배송코드")
	private String deliveryIrreCode;
	@ApiModelProperty(value="미배송사유")
	private String remarks;
	@ApiModelProperty(value="AGENT_코드")
	private String agentCode;		
	@ApiModelProperty(value="BL구분")
	private String blTypeMh;	
	@ApiModelProperty(value="바운드", example="COMMON, AI, AO, OI, OO")
	private String bound;	
	@ApiModelProperty(value="파일공유 유무")
	private String fileShare;		
	@ApiModelProperty(value="도입사코드 5자리 + 국가 2자리")
	private String entBusinessCode;	
	@ApiModelProperty(value="배송사변경안내여부")
	private String deliveryFeeNoticeYn;
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;	
	
	@ApiModelProperty(value="관세사 코드")
	private String cbCode;
	@ApiModelProperty(value="미통관코드")
	private String clearanceViolationCode;
	@ApiModelProperty(value="미통관사유")
	private String clearanceViolationReason;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="하위업체")
	private String custCodeLv2;
	@ApiModelProperty(value="배송사변경안내 전송일자")
	private String deliveryFeeNoticeDate;
	@ApiModelProperty(value="배송일자")
	private String deliveryDate;
	
	/* 하우스기준 합계목록 */
	@ApiModelProperty(value="하우스sum")
	private int sumCount;
	@ApiModelProperty(value="수량sum")
	private int sumPieces;
	@ApiModelProperty(value="중량sum")
	private double sumGwt;		
	@ApiModelProperty(value="배송사지정")
	private int sumDesignate;
	@ApiModelProperty(value="배송요청")
	private int sumReq;
	@ApiModelProperty(value="배송요청요류")
	private double sumRecv;		
	@ApiModelProperty(value="배송접수")
	private int sumUndelivery;
	@ApiModelProperty(value="미배달")
	private int sumReqErr;
	@ApiModelProperty(value="배송완료")
	private double sumDelivery;		
	@ApiModelProperty(value="집하처리")
	private int sumPickup;
	@ApiModelProperty(value="배송출발")
	private int sumStart;
	@ApiModelProperty(value="멀티송장번호")
	private String deliveryNo;
	@ApiModelProperty(value="멀티odJobNo")
	private String multiOdJobNo;
	@ApiModelProperty(value="shimpentBoxId")
	private String shipmentBoxId;
	@ApiModelProperty(value="반품번호")
	private String returnRequestId;
	@ApiModelProperty(value="반품운송장번호")
	private String returnDeliveryNo;
	@ApiModelProperty(value="반품배송접수일")
	private String returnRegDate;
	@ApiModelProperty(value="반품배송완료일")
	private String returnEndDate;
	@ApiModelProperty(value="반품배송사")
	private String returnDeliveryCode;
	@ApiModelProperty(value="배송접수일")
	private String deliveryRecvDate;
	@ApiModelProperty(value="집하처리")
	private String pickupDate;
	@ApiModelProperty(value="배송비고")
	private String deliveryNote;
	@ApiModelProperty(value="멀티일련번호자동")
	private String seqNoId;
}
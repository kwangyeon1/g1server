package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "OrderMulti", description = "전자상거래 Order 별도관리물품")
@Alias("orderMulti")
public class OrderMulti {
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="일련번호")
	private String seqNoId;
	@ApiModelProperty(value="배송번호")
	private String deliveryNo;
	@ApiModelProperty(value="배송사_코드")
	private String deliveryCode;
	@ApiModelProperty(value="거래처코드")
	private String customerCode;
	@ApiModelProperty(value="거래처명")
	private String customerName;
	@ApiModelProperty(value="변경배송번호")
	private String deliveryNoChanged;
	@ApiModelProperty(value="결제구분")
	private String paymentGubn;
	@ApiModelProperty(value="배송비")
	private double deliveryFee;
	@ApiModelProperty(value="수량")
	private double pieces;
	@ApiModelProperty(value="화물규격_가로")
	private double dimWidth;
	@ApiModelProperty(value="화물규격_길이")
	private double dimLength;
	@ApiModelProperty(value="화물규격_높이")
	private double dimHeight;
	@ApiModelProperty(value="화물규격_볼륨")
	private double vwt;
	@ApiModelProperty(value="배송비고")
	private String remarks;
	@ApiModelProperty(value="인계자명")
	private String delivererUserName;	
	@ApiModelProperty(value="인계일자")
	private String deliveryDate;
	@ApiModelProperty(value="인계자아이디")
	private String delivererUserId;
	@ApiModelProperty(value="인수자")
	private String recipientName;	
	@ApiModelProperty(value="인수일자")
	private String receiptDate;
	@ApiModelProperty(value="반품송장번호")
	private String returnDeliveryNo;
	@ApiModelProperty(value="반품번호")
	private String returnRequestId;
	@ApiModelProperty(value="반품요청일")
	private String returnReqDate;
	@ApiModelProperty(value="반품완료일")
	private String returnEndDate;
	@ApiModelProperty(value="반품배송사코드")
	private String returnDeliveryCode;
	@ApiModelProperty(value="반품접수일")
	private String returnRegDate;
	@ApiModelProperty(value="배송접수일")
	private String deliveryRegDate;
	@ApiModelProperty(value="배송완료일")
	private String deliveryEndDate;
    @ApiModelProperty(value="수하인명")
    private String consigneeNameLocM;
    @ApiModelProperty(value="수하인주소")
    private String consigneeAddressLocM;
    @ApiModelProperty(value="수하인전화번호")
    private String consigneeTelM;
    @ApiModelProperty(value="수하인우편번호")
    private String consigneeZipcodeM;
    @ApiModelProperty(value="배송상태")
	private String returnDeliveryStatus;
    
    @ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력자명")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;

}
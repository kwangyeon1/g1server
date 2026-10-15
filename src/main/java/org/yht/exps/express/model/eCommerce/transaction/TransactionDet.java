package org.yht.exps.express.model.eCommerce.transaction;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "TransactionDet", description = "거래정보 Order 리스트")
@Alias("transactionDet")
@EqualsAndHashCode(callSuper=false)
public class TransactionDet extends ResultList{
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="진행상태코드")
	private String lastPodStatus;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="하위업체")
	private String custCodeLv2;
	@ApiModelProperty(value="오더일자")
	private String poDate;
	@ApiModelProperty(value="오더번호")
	private String poNo;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="전자상거래 유형코드")
	private String ecmBuyType;
	@ApiModelProperty(value="전자상거래 국가코드")
	private String origCountry;
	@ApiModelProperty(value="구매대행업체부호")
	private String agentRegNo;
	@ApiModelProperty(value="구매대행업체명")
	private String agentName;
	@ApiModelProperty(value="배송대행업체부호")
	private String forwardAgentCode;
	@ApiModelProperty(value="배송대행업체명")
	private String forwardAgentName;
	@ApiModelProperty(value="화주_명칭")
	private String shipperName;
	@ApiModelProperty(value="화주_주소")
	private String shipperAddress;
	@ApiModelProperty(value="화주_전화번호")
	private String shipperTel;
	@ApiModelProperty(value="주문완료일시")
	private String orderEndDatetime;
	@ApiModelProperty(value="주문자명")
	private String orderName;
	@ApiModelProperty(value="주문자 전화번호")
	private String orderTel;
	@ApiModelProperty(value="주문자 아이디")
	private String orderAccount;
	@ApiModelProperty(value="수하인_명칭")
	private String consigneeName;
	@ApiModelProperty(value="수하인_주소")
	private String consigneeAddress;
	@ApiModelProperty(value="수하인_주소_한글")
	private String consigneeAddressLoc;
	@ApiModelProperty(value="수하인_우편번호")
	private String consigneeZipcode;
	@ApiModelProperty(value="수하인_전화번호")
	private String consigneeTel;
	@ApiModelProperty(value="수화인핸드폰번호")
	private String consigneeHp;
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="입력일자")
	private String registerDateTime;

	@ApiModelProperty(value="신고일자")
	private String sendDateTime;
	@ApiModelProperty(value="거래정보제출번호")
	private String ecIfSbmtNo;
	@ApiModelProperty(value="제출자 전자상거래업자부호")
	private String test2;
	@ApiModelProperty(value="제출자 전자상거래업체명")
	private String test3;
	@ApiModelProperty(value="거래정보 에러")
	private String recvErrCn;
}

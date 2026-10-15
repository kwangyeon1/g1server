package org.yht.exps.express.model.eCommerce.multiWg;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "MultiWgOrder", description = "전자상거래멀티중량 Order 리스트")
@Alias("multiWgOrder")
@EqualsAndHashCode(callSuper=false)
public class MultiWgOrder extends ResultList{

	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="도입사")
	private String entCode;
	@ApiModelProperty(value="사업장")
	private String entBusinessCode;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="진행상태")
	private String lastPodStatus;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="수출입")
	private String boundType;
	@ApiModelProperty(value="타입")
	private String cdType;
	@ApiModelProperty(value="입력자명")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정자명")
	private String updateUserName;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="배송지코드")
	private String deliveryCode;
	@ApiModelProperty(value="배송지명")
	private String deliveryName;
	@ApiModelProperty(value="변경배송지명")
	private String deliveryNameChanged;
	@ApiModelProperty(value="정산처명")
	private String accountName;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="하위업체")
	private String custCodeLv2;
	@ApiModelProperty(value="수하인_명칭")
	private String consigneeName;
	@ApiModelProperty(value="수하인_명칭")
	private String consigneeNameLoc;
	@ApiModelProperty(value="수하인_전화번호")
	private String consigneeHp;
	@ApiModelProperty(value="수량")
	private int pieces;
	@ApiModelProperty(value="총중량")
	private double gwt;
	@ApiModelProperty(value="대리점")
	private String custCodeLv1Name;
	@ApiModelProperty(value="화주_명칭")
	private String shipperName;
	@ApiModelProperty(value="통관결제_타입(선불후불)")
	private String clearancePaymentType;
	@ApiModelProperty(value="원화금액_AMOUNT")
	private double amountLoc;
	@ApiModelProperty(value="인계자")
	private String delivererUserName;	
	@ApiModelProperty(value="인계일자")
	private String deliveryDate;
	@ApiModelProperty(value="인수자")
	private String recipientName;	
	@ApiModelProperty(value="인수일자")
	private String receiptDate;
	@ApiModelProperty(value="배송요청일")
	private String podDateChk;

	@ApiModelProperty(value="일련번호")
	private String seqNoId;
	@ApiModelProperty(value="배송번호")
	private String deliveryNo;
	@ApiModelProperty(value="정산처코드")
	private String customerCode;
	@ApiModelProperty(value="정산처명")
	private String customerName;
	@ApiModelProperty(value="변경배송지명")
	private String deliveryNoChanged;
	@ApiModelProperty(value="결제조건")
	private String paymentGubn;
	@ApiModelProperty(value="배송비")
	private double deliveryFee;
	@ApiModelProperty(value="화물규격_가로")
	private int dimWidth;
	@ApiModelProperty(value="화물규격_길이")
	private int dimLength;
	@ApiModelProperty(value="화물규격_높이")
	private int dimHeight;
	@ApiModelProperty(value="화물규격_볼륨")
	private double vwt;
	@ApiModelProperty(value="배송비고")
	private String remarks;
	
	@ApiModelProperty(value="확정취소구분")
	private String gubun;	
	@ApiModelProperty(value="인계자id")
	private String recipientUserId;	
	
	@ApiModelProperty(value="file share")
	private String fileShare;
	
	/** 저장 */
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="로그유저명")
	private String logUserName;
	@ApiModelProperty(value="로그유저영문명")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
	
	//이영(중량)화물관리 추가
	@ApiModelProperty(value="오더일자")
	private String poDate;
	@ApiModelProperty(value="상위업체명")
	private String custNameLv1;
	@ApiModelProperty(value="하위업체명")
	private String custNameLv2;
	@ApiModelProperty(value="금액(CUR)")
	private String amountCur;
	@ApiModelProperty(value="오더배송사")
	private String deliveryNameOd;
	@ApiModelProperty(value="멀티배송사")
	private String deliveryNameMulti;
	@ApiModelProperty(value="결제조건")
	private String paymenGubn;
	@ApiModelProperty(value="배송_특별운임")
	private double deliverySpecialFee;
	@ApiModelProperty(value="배송_보험운임")
	private double deliveryInsuranceFee;
	@ApiModelProperty(value="배송료합계")
	private double totalFee;
	@ApiModelProperty(value="등록자")
	private String inputUserName;
	@ApiModelProperty(value="등록구분")
	private String inputMethod;
	@ApiModelProperty(value="등록일자")
	private String inputDateTime;
	@ApiModelProperty(value="하우스_볼륨")
	private double houseVwt;
	@ApiModelProperty(value="멀티_볼륨")
	private double multiVwt;
	@ApiModelProperty(value="하우스_수량")
	private int housePieces;
	@ApiModelProperty(value="멀티_수량")
	private int multiPieces;
	@ApiModelProperty(value="결제조건명")
	private String paymentGubnName;
	
	@ApiModelProperty(value="SMS안내_여부")
	private String smsInformYn;
	@ApiModelProperty(value="SMS처리_여부")
	private String smsInformResYn;
	@ApiModelProperty(value="SMS안내_일시")
	private String smsInformDate;
	@ApiModelProperty(value="SMS처리_일시")
	private String smsInformResDate;
}

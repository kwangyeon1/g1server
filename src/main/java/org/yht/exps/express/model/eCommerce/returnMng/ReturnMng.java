package org.yht.exps.express.model.eCommerce.returnMng;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "ReturnMng", description = "고객서비스 반품관리")
@Alias("returnMng")
@EqualsAndHashCode(callSuper=false)
public class ReturnMng extends ResultList{

	@ApiModelProperty(value="도입사")
	private String entCode;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="출항일자")
	private String obDate;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="수출입")
	private String boundType;
	@ApiModelProperty(value="입항일자")
	private String arDate;
	@ApiModelProperty(value="오더일자")
	private String poDate;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="하위업체")
	private String custCodeLv2;
	@ApiModelProperty(value="상위업체명")
	private String custNameLv1;
	@ApiModelProperty(value="하위업체명")
	private String custNameLv2;
	@ApiModelProperty(value="Order No.")
	private String poNo;
	@ApiModelProperty(value="H.B/L No.")
	private String hblNo;
	@ApiModelProperty(value="수량")
	private String pieces;
	@ApiModelProperty(value="금액")
	private String totalFee;
	@ApiModelProperty(value="품명")
	private String itemName;
	@ApiModelProperty(value="생성구분")
	private String inputMethod;
	@ApiModelProperty(value="반품번호")
	private String returnRequestId;
	@ApiModelProperty(value="반품배송사")
	private String returnDeliveryCode;
	@ApiModelProperty(value="반품배송사명")
	private String returnDeliveryName;
	@ApiModelProperty(value="반품송장번호")
	private String returnDeliveryNo;
	@ApiModelProperty(value="반품요청일")
	private String returnReqDate;
	@ApiModelProperty(value="반품접수일")
	private String returnRegDate;
	@ApiModelProperty(value="반품배송상태")
	private String returnDeliveryStatus;
	@ApiModelProperty(value="반품배송상태명")
	private String returnDeliveryStatusName;
	@ApiModelProperty(value="반품인")
	private String returnCustomer;
	@ApiModelProperty(value="반품주소")
	private String returnAddress;
	@ApiModelProperty(value="반품우편번호")
	private String returnZipcode;
	@ApiModelProperty(value="반품전화번호")
	private String returnTel;
	@ApiModelProperty(value="반입지")
	private String carryin;
	@ApiModelProperty(value="반입지주소")
	private String carryinAddress;
	@ApiModelProperty(value="반입지우편번호")
	private String carryinZipcode;
	@ApiModelProperty(value="반입지전화번호")
	private String carryinTel;
	@ApiModelProperty(value="입고일자")
	private String carryinDateTime;
	@ApiModelProperty(value="입고담당자")
	private String carryinUserName;
	@ApiModelProperty(value="입고위치")
	private String carryinWhLocation;
	@ApiModelProperty(value="반품처리유형")
	private String returnProcessType;
	@ApiModelProperty(value="출고일자")
	private String carryoutDateTime;
	@ApiModelProperty(value="출고담당자")
	private String carryoutUserName;
	@ApiModelProperty(value="시스템배송요청여부")
	private String sendChk;
	@ApiModelProperty(value="재발송배송사")
	private String resendDeliveryCode;
	@ApiModelProperty(value="재발송배송사명")
	private String resendDeliveryName;
	@ApiModelProperty(value="재발송접수일")
	private String resendDateTime;
	@ApiModelProperty(value="재발송송장번호")
	private String resendDeliveryNo;
	@ApiModelProperty(value="결제조건")
	private String resendIncoterms;
	@ApiModelProperty(value="결제조건이름")
	private String resendIncotermsName;
	@ApiModelProperty(value="재발송운임")
	private String resendFee;
	@ApiModelProperty(value="재발송주소")
	private String resendAddress;
	@ApiModelProperty(value="재발송우편번호")
	private String resendZipcode;
	@ApiModelProperty(value="재발송전화번호")
	private String resendTel;
	@ApiModelProperty(value="재발송배송상태")
	private String resendStatus;
	@ApiModelProperty(value="비고사항")
	private String resendRemark;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="일련번호")
	private String seqNoId;

	@ApiModelProperty(value="입력자명")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정자명")
	private String updateUserName;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자명")
	private String logUserNameEng;

	
}

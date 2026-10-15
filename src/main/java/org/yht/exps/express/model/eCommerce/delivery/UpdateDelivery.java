package org.yht.exps.express.model.eCommerce.delivery;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.search.SearchList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "UpdateDelivery", description = "전자상거래 Delivery 업데이트")
@Alias("updateDelivery")
public class UpdateDelivery extends SearchList {
	@ApiModelProperty(value="MBL No")
	private String srchMblNo;	
	@ApiModelProperty(value="HBL No")
	private String srchHblNo;	
	@ApiModelProperty(value="조회업체")
	private String srchCus;
	@ApiModelProperty(value="cus Like검색 여부", example="Y or N")
	private String srchCusLikeYn;
	@ApiModelProperty(value="조회업체타입")
	private String srchCusType;
	@ApiModelProperty(value="배송상태")
	private String srchStatus;	
	@ApiModelProperty(value="조회번호타입")
	private String srchNoType;	
	@ApiModelProperty(value="조회번호")
	private String srchNo;
	@ApiModelProperty(value="조회타입 멀티/싱글")
	private String searchType;
	@ApiModelProperty(value="srchNo N개씩 조회시 리스트")
	private List<String> searchNoList;		
	@ApiModelProperty(value="운송사 코드")
	private String trCode;	
	@ApiModelProperty(value="배송사 코드", example="우체국, 대한통운")
	private String deliveryCode;
	@ApiModelProperty(value="등록자")
	private String registerUserId;
	@ApiModelProperty(value="등록자명")
	private String registerUserName;
	@ApiModelProperty(value="수정자")
	private String updateUserId;		
	@ApiModelProperty(value="업데이트 방법")
	private String action;	
	@ApiModelProperty(value="GMT", example="9")
	private int gmtTime;
	@ApiModelProperty(value="도입사코드", example = "YHTXX")
	private String entCode;	
	@ApiModelProperty(value="도입사_사업장코드")
	private String entBusinessCode;	
	@ApiModelProperty(value="변경배송번호")
	private String deliveryNoChanged;	
	@ApiModelProperty(value="변경배송사유")
	private String deliveryReasonChanged;	
	@ApiModelProperty(value="결제구분")
	private String deliveryPaymentType;		
	@ApiModelProperty(value="리스트")
	private List<Map<String, Object>> dataList;
	@ApiModelProperty(value="수하인명")
	private String consigneeName;
	@ApiModelProperty(value="수하인_전화번호")
	private String consigneeTel;	
	@ApiModelProperty(value="수하인_우편번호")
	private String consigneeZipcode;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;	
	@ApiModelProperty(value="변경배송주소")
	private String deliveryAddressChanged;
	@ApiModelProperty(value="조회번호 Like검색 여부", example="Y or N")
	private String srchNoLikeYn;	
	@ApiModelProperty(value="BL구분")
	private String blTypeMh;		
	@ApiModelProperty(value="거래처")
	private String srchCustomerCode;
	@ApiModelProperty(value="집하점")
	private String srchLogisticsCode;	
	@ApiModelProperty(value="항공/해상,수출/수입구분", example="AO")
	private String srchBoundType;
	@ApiModelProperty(value="마스터관리번호리스트")
	private String srchMasterOdJobNoListStr;
	@ApiModelProperty(value="바운드", example="AIR/SEA")
	private String bound;
	@ApiModelProperty(value="고객사코드", example="")
	private String srchEcmCustomerCode;
	
	@ApiModelProperty(value="배송사 검색조건")
	private String srchDeliveryCode;
	@ApiModelProperty(value="비용구분")
	private String srchDeliveryPaymentType;
	@ApiModelProperty(value="관리번호리스트")
	private String[] srchOdJobNoList;
	@ApiModelProperty(value="멀티운송장여부", example="Y or N")
	private String srchMultiYn;
	@ApiModelProperty(value="반품여부", example="Y or N")
	private String srchReturnYn;
	@ApiModelProperty(value="카운터확인여부")
	private String multiChk;
	@ApiModelProperty(value="반품배송사체크여부")
	private String returnDeliveryCodeChk;
	@ApiModelProperty(value="경동처리를위한시퀀스")
	private String seq;
	@ApiModelProperty(value="화면체크")
	private String chkPage;

	@ApiModelProperty(value="리스트")
	private List<Map<String, Object>> multiOdList;
	private List<Map<String, Object>> hblOdList;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;	
	@ApiModelProperty(value="profile명")
	private String profileName;
	@ApiModelProperty(value="배송상태")
	private String srchDeliveryStatus;

}

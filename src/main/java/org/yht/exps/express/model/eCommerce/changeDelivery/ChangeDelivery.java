package org.yht.exps.express.model.eCommerce.changeDelivery;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "ChangeDelivery", description = "고객서비스 송장교체(주소변경)")
@Alias("changeDelivery")
@EqualsAndHashCode(callSuper=false)
public class ChangeDelivery extends ResultList{

	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="일련번호")
	private String seqNoId;
	@ApiModelProperty(value="진행상태")
	private String lastPodStatus;
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
	@ApiModelProperty(value="H.B/L No.")
	private String hblNo;
	@ApiModelProperty(value="변경이름")
	private String deliveryNameChanged;
	@ApiModelProperty(value="변경우편번호")
	private String deliveryZipcodeChanged;
	@ApiModelProperty(value="변경주소")
	private String deliveryAddressChanged;
	@ApiModelProperty(value="변경연락처")
	private String deliveryTelChanged;
	@ApiModelProperty(value="변경배송사")
	private String deliveryCodeChanged;
	@ApiModelProperty(value="변경배송사명")
	private String deliveryName;
	@ApiModelProperty(value="변경송장번호")
	private String deliveryNoChanged;
	@ApiModelProperty(value="등록구분")
	private String inputMethod;
	@ApiModelProperty(value="교체일자")
	private String deliveryChangedDate;
	@ApiModelProperty(value="출력일자")
	private String printDate;
	@ApiModelProperty(value="확인")
	private String confirmYn;
	@ApiModelProperty(value="확인자명")
	private String confirmUserName;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="갯수")
	private String pieces;
	@ApiModelProperty(value="배송요청일")
	private String podDateChk;
	@ApiModelProperty(value="멀티여부")
	private String multiYn;
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
	
	@ApiModelProperty(value="리스트")
	private List<Map<String, Object>> dataList;
	
	@ApiModelProperty(value="SMS안내_여부")
	private String smsInformYn;
	@ApiModelProperty(value="SMS처리_여부")
	private String smsInformResYn;
	@ApiModelProperty(value="SMS안내_일시")
	private String smsInformDate;
	@ApiModelProperty(value="SMS처리_일시")
	private String smsInformResDate;
	
}

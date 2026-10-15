package org.yht.exps.express.model.eCommerce.pccc;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;
import org.yht.exps.express.model.systemMng.Alarm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "PodDetail", description = "전자상거래 PodDetail")
@Alias("podDetail")
@EqualsAndHashCode(callSuper=false)
public class PodDetail extends ResultList{

	@ApiModelProperty(value="관리번호")
	private String customsIdJobNo;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="요청ID")
	private String requestId;
	@ApiModelProperty(value="진행상태")
	private String status;
	@ApiModelProperty(value="진행상태")
	private String statusName;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="주문번호")
	private String poNo;
	@ApiModelProperty(value="주문일")
	private String poDate;
	@ApiModelProperty(value="운송장번호")
	private String hblNo;
	@ApiModelProperty(value="mrn")
	private String mrn;
	@ApiModelProperty(value="소유자명")
	private String origConsigneeName;
	@ApiModelProperty(value="대표물품")
	private String commodityItem;
	@ApiModelProperty(value="배송비고")
	private String deliveryNote;
	@ApiModelProperty(value="출발지")
	private String origName;
	@ApiModelProperty(value="도착지")
	private String destName;
	@ApiModelProperty(value="출,도착지")
	private String origDest;
	@ApiModelProperty(value="M.B/L No")
	private String mblNo;
	@ApiModelProperty(value="FLT No / Vessel Name")
	private String vesselFlight;
	@ApiModelProperty(value="DepDate / Time")
	private String obDate;
	@ApiModelProperty(value="ArrDate / Time")
	private String arDate;
	@ApiModelProperty(value="반품여부")
	private String returnYn;
	@ApiModelProperty(value="멀티운송여부")
	private String multiYn;
	@ApiModelProperty(value="PCCC성공여부")
	private String pcccYn;
	@ApiModelProperty(value="PCCC에러내용")
	private String errorDesc;
	@ApiModelProperty(value="POD상태")
	private String podStatus;
	@ApiModelProperty(value="POD상태명")
	private String podStatusNm;
	@ApiModelProperty(value="통관목록사유")
	private String podReason;
	@ApiModelProperty(value="입력자")
	private String registerUserId;	
	@ApiModelProperty(value="입력시간")
	private String registerDateTime;
	@ApiModelProperty(value="갱신자")
	private String updateUserId;
	@ApiModelProperty(value="갱신시간")
	private String updateDateTime;
	@ApiModelProperty(value="날짜형식")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String srchDateFormat;
	@ApiModelProperty(value="배송사코드")
	private String deliveryCode;
	@ApiModelProperty(value="배송사코드")
	private String lastPodStatus;
	@ApiModelProperty(value="관세사코드")
	private String cbCode;
	@ApiModelProperty(value="관세사명")
	private String cbName;
	@ApiModelProperty(value="관세사전화번호")
	private String cbTel;
	
	@ApiModelProperty(value="발송국가")
	private String origCountry;
	@ApiModelProperty(value="화주명칭")
	private String shipperName;
	
	@ApiModelProperty(value="알림톡 List")
	private List<Alarm> alarmList;
	@ApiModelProperty(value="POD List")
	private List<Map<String,Object>> podList;
	@ApiModelProperty(value="delivery상태 List")
	private List<Map<String,Object>> deliveryList;
	
	@ApiModelProperty(value="멀티운송장번호 List")
	private List<Map<String,Object>> multiNoList;
}

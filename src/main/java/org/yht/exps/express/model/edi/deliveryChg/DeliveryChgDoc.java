package org.yht.exps.express.model.edi.deliveryChg;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "DeliveryChgDoc", description = "전자상거래  배송지변경신고")
@Alias("deliveryChgDoc")
public class DeliveryChgDoc {
	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;	
	@ApiModelProperty(value="관리번호")
	private String ediJobNo;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	
	@ApiModelProperty(value="배송지변경신고 리스트")
	private List<DeliveryChg> deliveryChgList;	
	
	@ApiModelProperty(value="엑셀 업로드 리스트")
	private List<Map<String, Object>> dataList;
	
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;	
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력자코드")
	private String registerUserName;
	@ApiModelProperty(value="insert/update구분")
	private String action;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	
	@ApiModelProperty(value="변경배송사_주소")
	private String deliveryAddressChanged;	
	@ApiModelProperty(value="변경배송_사유")
	private String deliveryReasonChanged;	
	
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	
	@ApiModelProperty(value="변경배송사")
	private String deliveryNameChanged;
	@ApiModelProperty(value="변경배송_일자")
	private String deliveryDate;
	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}


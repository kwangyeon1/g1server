package org.yht.exps.express.model.edi.deliveryChg;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "DeliveryChg", description = "전자상거래  배송지변경신고")
@Alias("deliveryChg")
public class DeliveryChg {
	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;
	
	@ApiModelProperty(value="관리번호")
	private String ediJobNo;
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="목적항")
	private String destPort;
	
	@ApiModelProperty(value="그룹")
	private String docYn;
	@ApiModelProperty(value="배송지변경신청서_제출번호")
	private String ediDocNo;
	@ApiModelProperty(value="제출일자")
	private String issueDate;
	
	@ApiModelProperty(value="송신상태")
	private String ediStatus;
	@ApiModelProperty(value="배송지변경입력일자")
	private String deliveryChangeDateTime;
	@ApiModelProperty(value="전송일자/시간")
	private String ediSendDate;

	@ApiModelProperty(value="접수일자/시간")
	private String ediReceiptDate;
	@ApiModelProperty(value="결과통보일")
	private String ediResultDate;
	@ApiModelProperty(value="결과일자/시간")
	private String resultDateTime;
	@ApiModelProperty(value="마스터odJobNo")
	private String masterOdJobNo;
	
	@ApiModelProperty(value="건수")
	private String docCnt;
	@ApiModelProperty(value="전송건수")
	private String sendCnt;
	@ApiModelProperty(value="관리번호")
	private String odJobNo;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="EDI입력일시")
	private String ediRegisterDateTime;
	
	@ApiModelProperty(value="EDI_포털_사서함_ID")
	private String ediPortalMailboxId;
		
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력자코드")
	private String registerUserName;
	@ApiModelProperty(value="insert/update구분")
	private String action;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;	
	@ApiModelProperty(value="결과통보상태")
	private String resultStatus;
	@ApiModelProperty(value="과태료여부")
	private String penaltyYn;
	@ApiModelProperty(value="통관접수일")
	private String clearanceReceiptDate;
	@ApiModelProperty(value="제출번호")
	private String poNo;
	@ApiModelProperty(value="통관타입")
	private String cdType;
	@ApiModelProperty(value="마스터빌번호")
	private String mblNo;
	@ApiModelProperty(value="하우스빌번호")
	private String hblNo;
	@ApiModelProperty(value="배송일자")
	private String deliveryDate;
	@ApiModelProperty(value="배송일자_엑셀용")
	private String deliveryDateExcel;
	@ApiModelProperty(value="배송사")
	private String deliveryCode;
	@ApiModelProperty(value="배송사")
	private String deliveryName;
	@ApiModelProperty(value="배송주소")
	private String consigneeAddress;
	@ApiModelProperty(value="수하인주소(L)")
	private String consigneeAddressLoc;
	@ApiModelProperty(value="변경후배송주소")
	private String deliveryAddressChanged;
	@ApiModelProperty(value="변경사유")
	private String deliveryReasonChanged;
	@ApiModelProperty(value="수하인_명칭")
	private String consigneeName;
	@ApiModelProperty(value="대표품목")
	private String commodityItem;
	@ApiModelProperty(value="세관코드")
	private String customsCode;
	@ApiModelProperty(value="출발항")
	private String origCode;
	@ApiModelProperty(value="편명")
	private String vesselFlight;
	@ApiModelProperty(value="POD관리번호")
	private String podJobNo;
	@ApiModelProperty(value="배송내역일련번호")
	private String deliveryChangedSeqNo;
	@ApiModelProperty(value="목록신고일자")
	private String simpleReportDate;
	@ApiModelProperty(value="목록신고제출번호")
	private String simpleDocNo;
	
	@ApiModelProperty(value="변경후 배송사")
	private String deliveryNameChanged;
	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;
	
	
}


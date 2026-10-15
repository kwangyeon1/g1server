package org.yht.exps.express.model.systemMng;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "DeliveryInterface", description = "전자상거래 배송사 Interface")
@Alias("deliveryInterface")
@EqualsAndHashCode(callSuper=false)
public class DeliveryInterface extends ResultList{

	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="공통/항공/해상/반송", example="C, A, O, R")
	private String bound;	
	@ApiModelProperty(value="공통/항공/해상/반송", example="공통")
	private String boundEng;		
	@ApiModelProperty(value="거래처코드")
	private String customerCode;
	@ApiModelProperty(value="거래처명")
	private String customerName;	
	@ApiModelProperty(value="집화점코드")
	private String logisticsCode;	
	@ApiModelProperty(value="집화점이름")
	private String logisticsName;	
	@ApiModelProperty(value="대한통운트래킹수집 여부", example="Y, N")
	private String trackingKorexYn;	
	@ApiModelProperty(value="대한통운요청방식")
	private String korexMethod;
	@ApiModelProperty(value="대한통운 아이디")
	private String korexId;		
	@ApiModelProperty(value="대한통운 대리점아이디")
	private String korexCustId;		
	@ApiModelProperty(value="반송전화번호1")
	private String senderTelNo1;		
	@ApiModelProperty(value="반송전화번호2")
	private String senderTelNo2;
	@ApiModelProperty(value="반송전화번호3")
	private String senderTelNo3;	
	@ApiModelProperty(value="반송우편번호")
	private String senderZipcode;	
	@ApiModelProperty(value="반송주소")
	private String senderAddr;	
	@ApiModelProperty(value="반송상세주소")
	private String senderAddrDtl;		
	@ApiModelProperty(value="반송정보유무", example="Y, N")
	private String senderInfoUseYn;		
	@ApiModelProperty(value="우체국기업연계 트래킹수집 여부", example="Y, N")
	private String trackingEmsYn;	
	@ApiModelProperty(value="우체국API 트래킹수집 여부", example="Y, N")
	private String trackingEpostYn;	
	@ApiModelProperty(value="우체국 기업연계 아이디")
	private String emsId;		
	@ApiModelProperty(value="우체국 API 인증키")
	private String epostAuthKey;	
	@ApiModelProperty(value="우체국 API OFFICE_SER")
	private String epostOfficeSer;		
	@ApiModelProperty(value="우체국 API APPR_NO")
	private String epostApprNo;		
	@ApiModelProperty(value="우체국 API EPOST_SECURITY_KEY")
	private String epostSecurityKey;	
	@ApiModelProperty(value="우체국 API EPOST_CUST_NO")
	private String epostCustNo;
	@ApiModelProperty(value="우체국 API 집배코드 인증키")
	private String epostDelivAreaKey;
	@ApiModelProperty(value="코파스 사용 유무", example="Y, N")
	private String trackingCopasYn;
	@ApiModelProperty(value="스마트로 사용 유무", example="Y, N")
	private String trackingSmartYn;		
	@ApiModelProperty(value="FTP 타입")
	private String ftpType;			
	@ApiModelProperty(value="FTP 아이피")
	private String ftpIp;		
	@ApiModelProperty(value="FTP 포트")
	private String ftpPort;	
	@ApiModelProperty(value="FTP 아이디")
	private String ftpId;	
	@ApiModelProperty(value="FTP 패스워드")
	private String ftpPw;	
	@ApiModelProperty(value="FTP PASSIVE MODE")
	private String ftpPassivemode;		
	@ApiModelProperty(value="FTP 송신경로")
	private String ftpSenderDir;		
	@ApiModelProperty(value="FTP 수신경로")
	private String ftpReceiverDir;	
	@ApiModelProperty(value="로젠택배 트레킹수집 여부" , example="Y, N")
	private String trackingLogenYn;		
	@ApiModelProperty(value="로젠택배 아이디")
	private String logenId;		
	@ApiModelProperty(value="로젠택배 패스워드")
	private String logenPw;		
	@ApiModelProperty(value="로젠택배 FREIGHT ")
	private String logenFreight;	
	@ApiModelProperty(value="로젠택배 가격")
	private String logenPrice;	
	@ApiModelProperty(value="천일택배 트래킹수집여부")
	private String trackingChunilYn;	
	@ApiModelProperty(value="한진택배 트래킹수집여부")
	private String trackingHanjinYn;	
	@ApiModelProperty(value="한진택배 고객번호")
	private String hanjinCusNum;
	@ApiModelProperty(value="한진택배 우편번호")
	private String hanjinZipcode;
	@ApiModelProperty(value="한진택배 주소1")
	private String hanjinAddr1;
	@ApiModelProperty(value="한진택배 주소2")
	private String hanjinAddr2;	
	@ApiModelProperty(value="한진택배 이름")
	private String hanjinName;	
	@ApiModelProperty(value="한진택배 전화번호")
	private String hanjinTel;			
	@ApiModelProperty(value="한진택배 HP")
	private String hanjinMobile;	
	@ApiModelProperty(value="한진택배 client id")
	private String hanjinClientId;	
	@ApiModelProperty(value="한진택배 auth key")
	private String hanjinApiCode;	
	@ApiModelProperty(value="롯데택배 사용여부")
	private String trackingLotteYn;
	@ApiModelProperty(value="롯데택배 거래처코드")
	private String lotteAuthKey;
	@ApiModelProperty(value="롯데택배 인증키")
	private String lotteCustId;
	@ApiModelProperty(value="롯데택배 우편번호")
	private String lotteZipcode;
	@ApiModelProperty(value="롯데택배 주소")
	private String lotteAddr;
	@ApiModelProperty(value="롯데택배 박스타입")
	private String lotteBoxType;
	@ApiModelProperty(value="롯데택배 핸드폰번호")
	private String lotteMobile;
	@ApiModelProperty(value="롯데택배 송하인명")
	private String lotteName;
	@ApiModelProperty(value="롯데택배 전화번호")
	private String lotteTel;
	@ApiModelProperty(value="경동택배 사용여부")
	private String trackingKdYn;
	@ApiModelProperty(value="경동택배 인증키")
	private String kdAuthKey;
	@ApiModelProperty(value="경동택배 고객사 등록 관리번호")
	private String kdCustSendNo;
	@ApiModelProperty(value="경동택배 보내는분 우편번호")
	private String kdZipcode;
	@ApiModelProperty(value="경동택배 보내는분 전화번호")
	private String kdTel1;
	@ApiModelProperty(value="경동택배 보내는분 기타 전화번호")
	private String kdTel2;
	@ApiModelProperty(value="경동택배 보내는분 성함")
	private String kdName;
	@ApiModelProperty(value="경동택배 보내는분 주소")
	private String kdAddr1;
	@ApiModelProperty(value="경동택배 보내는분 상세주소")
	private String kdAddr2;
	@ApiModelProperty(value="크림 사용여부")
	private String trackingKreamYn;
	
		
	@ApiModelProperty(value="배송 Interface 사용여부" , example="Y, N")
	private String useYn;	
	@ApiModelProperty(value="등록아이디")
	private String regId;			
	@ApiModelProperty(value="등록날짜")
	private String regDate;	
	@ApiModelProperty(value="수정아이디")
	private String updId;			
	@ApiModelProperty(value="수정날짜")
	private String updDate;	
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="수정자")
	private String updateUserName;
	@ApiModelProperty(value="프로파일명")
	private String profileName;
	
	@ApiModelProperty(value="API탭 그리드")
	private List<Map<String, Object>> gridData;

	
	/** 저장 */
	@ApiModelProperty(value="insert/update구분")
	private String action;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
}

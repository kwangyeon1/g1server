package org.yht.exps.express.model.sms.user;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "EcmUserDetail", description = "Ecm사용자")
@Alias("ecmUserDetail")
public class EcmUserDetail {
	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="사용자아이디")
	private String userId;
	@ApiModelProperty(value="사용자로그인아이디")
	private String userLoginId;
	@ApiModelProperty(value="사용자이름")
	private String userName;
	@ApiModelProperty(value="사용자구분")
	private String userType;
	@ApiModelProperty(value="사업장")
	private String entBusinessCode;
	@ApiModelProperty(value="패쓰워드")
	private String userLoginPw;
	@ApiModelProperty(value="주소1")
	private String userAddress;
	@ApiModelProperty(value="주소2")
	private String userAddressA;
	@ApiModelProperty(value="주소3")
	private String userAddressB;
	@ApiModelProperty(value="핸드폰번호")
	private String userCellphone;
	@ApiModelProperty(value="내선번호")
	private String userTel;
	@ApiModelProperty(value="이메일")
	private String userEmail;
	@ApiModelProperty(value="중량타입")
	private String lbCftType;
	@ApiModelProperty(value="통화단위")
	private String currencyCode;
	@ApiModelProperty(value="엑셀타입")
	private String 	excelType;
	@ApiModelProperty(value="관세사")
	private String 	cbCode;
	@ApiModelProperty(value="관세사명")
	private String 	cbName;
	@ApiModelProperty(value="배송사")
	private String 	trCode;
	@ApiModelProperty(value="배송사명")
	private String 	trName;
	@ApiModelProperty(value="거래처정보코드")
	private String 	customerCode;
	@ApiModelProperty(value="거래처명")
	private String 	customerName;
	@ApiModelProperty(value="저장타입")
	private String 	action;	
	@ApiModelProperty(value="수정자")
	private String 	updateUserId;		
	@ApiModelProperty(value="시간")
	private String 	gmtTime;
	private String 	defaultSolutionCode;
	@ApiModelProperty(value="기본메뉴_바운드")
	private String 	defaultBound;
	@ApiModelProperty(value="기본메뉴_바운드타입")
	private String 	defaultBoundType;
	
	//조회옵션
	@ApiModelProperty(value="조회일수 이전")
	private String searchStartTerm;	
	@ApiModelProperty(value="조회일수 이후")
	private String searchEndTerm;	
	@ApiModelProperty(value="사용자조회옵션")
	private String searchAllUserYn;	
	@ApiModelProperty(value="사용자조회옵션")
	private String searchDefaultUserYn;	
	@ApiModelProperty(value="조회상세옵션")
	private String searchOptionHideYn;	
	@ApiModelProperty(value="사용자메뉴펼침여부")
	private String spreadYn;	
	@ApiModelProperty(value="날짜포맷")
	private String dateFormat;	
	
	//로그인옵션
	@ApiModelProperty(value="암호사용시작일")
	private String pwUpdateDate;	
	@ApiModelProperty(value="암호유효기간")
	private String pwUpdateDays;	
	@ApiModelProperty(value="암호오류제한횟수")
	private String pwErrorCount;	
	@ApiModelProperty(value="암호만료안내")
	private String pwUpdateAlertDays;	
	@ApiModelProperty(value="유효사용일수")
	private String validUseDays;	
	@ApiModelProperty(value="등록일자")
	private String registerDate;	
	@ApiModelProperty(value="사용자권한")
	private String userClass;	
	
	@ApiModelProperty(value="승인여부")
	private String useYn;	
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="수정자")
	private String updateUserName;

	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
}

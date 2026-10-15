package org.yht.exps.express.model.sms.user;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "EcmUserSave", description = "Ecm사용자 저장용")
@Alias("ecmUserSave")
public class EcmUserSave {
	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="사용자아이디")
	private String userId;
	@ApiModelProperty(value="사용자로그인아이디")
	private String userLoginId;
	@ApiModelProperty(value="사용자이름")
	private String userName;
	@ApiModelProperty(value="직급코드")
	private String positionCode;
	@ApiModelProperty(value="부서코드")
	private String departmentCode;
	@ApiModelProperty(value="상위_사용자ID")
	private String parentId;
	@ApiModelProperty(value="사용자구분")
	private String userType;
	@ApiModelProperty(value="사업장")
	private String entBusinessCode;
	@ApiModelProperty(value="사업장조회권한")
	private String srchServices;
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
	@ApiModelProperty(value="배송사")
	private String 	trCode;
	@ApiModelProperty(value="거래처정보코드")
	private String 	customerCode;
	@ApiModelProperty(value="저장타입")
	private String 	action;	
	@ApiModelProperty(value="수정자")
	private String 	updateUserId;		
	@ApiModelProperty(value="시간")
	private String 	gmtTime;
	@ApiModelProperty(value="기본메뉴_솔루션")
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

	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}

package org.yht.exps.express.model.sms.user;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "User", description = "사용자")
@Alias("user")
public class User {
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="사용자아이디")
	private String userId;
	@ApiModelProperty(value="사용자_로그인아이디")
	private String userLoginId;
	@ApiModelProperty(value="사용자_로그인암호")
	private String userLoginPw;
	@ApiModelProperty(value="상위_사용자ID")
	private String parentId;
	@ApiModelProperty(value="사용자구분")
	private String userType;
	@ApiModelProperty(value="사용자업무구분")
	private String userJobType;
	@ApiModelProperty(value="사용자명")
	private String userName;
	@ApiModelProperty(value="사용자명_영문")
	private String userNameEng;
	@ApiModelProperty(value="사용자명_약어")
	private String userNameAbbreviation;
	@ApiModelProperty(value="사용자_전화번호")
	private String userTel;
	@ApiModelProperty(value="사용자_내선번호")
	private String userTelExtension;
	@ApiModelProperty(value="사용자_팩스번호")
	private String userFax;
	@ApiModelProperty(value="사용자_핸드폰번호")
	private String userCellphone;
	@ApiModelProperty(value="사용자_이메일")
	private String userEmail;
	@ApiModelProperty(value="사용자_이메일암호")
	private String userEmailPw;
	@ApiModelProperty(value="직급코드")
	private String positionCode;
	@ApiModelProperty(value="부서코드")
	private String departmentCode;
	@ApiModelProperty(value="사용여부")
	private String useYn;
	@ApiModelProperty(value="비고사항")
	private String remarks;
	@ApiModelProperty(value="관리용_팀코드")
	private String teamCode;
	@ApiModelProperty(value="도입사_사업장코드")
	private String entBusinessCode;
	@ApiModelProperty(value="도입사_본지사코드")
	private String entOfficeCode;
	@ApiModelProperty(value="고객코드")
	private String customerCode;
	@ApiModelProperty(value="메인타입_A,B,C...")
	private String mainType;
	@ApiModelProperty(value="입사일")
	private Object joinDate;
	@ApiModelProperty(value="퇴사일")
	private Object resignDate;
	@ApiModelProperty(value="베트남 BKAV 세금계산서 사용자 GUID")
	private String etc1;
	@ApiModelProperty(value="베트남 BKAV 세금계산서 사용자 TOKEN")
	private String etc2;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="수정일시")
	private String updateDateTime;
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
}

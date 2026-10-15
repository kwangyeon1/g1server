package org.yht.exps.express.model.sms.user;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "EcmUser", description = "Ecm사용자")
@Alias("ecmUser")
public class EcmUser {
	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="사용자아이디")
	private String userId;
	@ApiModelProperty(value="사용자이름")
	private String userName;
	@ApiModelProperty(value="사용자이름(영문)")
	private String userNameEng;
	@ApiModelProperty(value="세션아이디")
	private String userSessionId;
	@ApiModelProperty(value="사용자_로그인아이디")
	private String userLoginId;
	@ApiModelProperty(value="사업장")
	private String entBusinessCode;
	@ApiModelProperty(value="상위_사용자ID")
	private String parentId;
	@ApiModelProperty(value="사용자구분")
	private String userType;	
	@ApiModelProperty(value="승인여부")
	private String useYn;
	@ApiModelProperty(value="거래처코드")
	private String customerCode;
	@ApiModelProperty(value="거래처명")
	private String customerName;
	
	@ApiModelProperty(value="이메일")
	private String userEmail;
	@ApiModelProperty(value="HP")
	private String userCellphone;
	@ApiModelProperty(value="내선번호")
	private String userTel;
	
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="Count")
	private int count;
	
	@ApiModelProperty(value="조회건수")
	private int total;
	@ApiModelProperty(value="번호")
	private int num;

	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
}

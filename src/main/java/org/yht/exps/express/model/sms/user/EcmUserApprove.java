package org.yht.exps.express.model.sms.user;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "EcmUserApprove", description = "Ecm승인여부 업데이트할 사용자")
@Alias("ecmUserApprove")
public class EcmUserApprove {
	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	
	@ApiModelProperty(value="리스트")
	private List<EcmUser> ecmUserList;

	@ApiModelProperty(value="승인여부")
	private String useYn;
	
	@ApiModelProperty(value="날짜포맷")
	private String dateFormat;	
	@ApiModelProperty(value="수정자")
	private String 	updateUserId;	
	@ApiModelProperty(value="시간")
	private String 	gmtTime;	
	
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
}

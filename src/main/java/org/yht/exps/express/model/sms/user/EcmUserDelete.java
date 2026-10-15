package org.yht.exps.express.model.sms.user;

import java.util.List;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "EcmUserDelete", description = "Ecm사용자 삭제할 사용자")
@Alias("ecmUserDelete")
public class EcmUserDelete {
	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	
	@ApiModelProperty(value="리스트")
	private List<EcmUser> ecmUserList;
	@ApiModelProperty(value="사용자아이디")
	private String userId;
	
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
}

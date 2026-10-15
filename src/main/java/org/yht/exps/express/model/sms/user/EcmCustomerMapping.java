package org.yht.exps.express.model.sms.user;

import java.util.List;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "EcmCustomerMapping", description = "Ecm 삭제할 거래처")
@Alias("ecmCustomerMapping")
public class EcmCustomerMapping {
	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	
	@ApiModelProperty(value="리스트")
	private List<EcmCustomer> ecmCustomerList;
	@ApiModelProperty(value="맵핑할 부모 거래처코드")
	private String parentCustomerCode;
	@ApiModelProperty(value="맵핑타입")
	private String mappingYn;
	
	@ApiModelProperty(value="수정자")
	private String 	updateUserId;	
	@ApiModelProperty(value="시간")
	private String 	gmtTime;	
	
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
}

package org.yht.exps.express.model.sms.user;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "EcmCustomerInterface", description = "Ecm거래처정보 저장용")
@Alias("ecmCustomerInterface")
public class EcmCustomerInterface {
	@ApiModelProperty(value="솔루션")
	private String 	solution;
	@ApiModelProperty(value="IF서비스코드")
	private String 	ifServiceCode;
	@ApiModelProperty(value="수신자ID")
	private String 	ifReceiverId;
	@ApiModelProperty(value="로그인ID")
	private String 	ifLoginId;
	@ApiModelProperty(value="로그인암호")
	private String 	ifLoginPw;
	@ApiModelProperty(value="기타1")
	private String 	ifEtc1;
	@ApiModelProperty(value="기타2")
	private String 	ifEtc2;
	@ApiModelProperty(value="Etc-3")
	private String 	ifEtc3;
	@ApiModelProperty(value="Etc-4")
	private String 	ifEtc4;
	@ApiModelProperty(value="Etc-5")
	private String 	ifEtc5;
	@ApiModelProperty(value="Etc-6")
	private String 	ifEtc6;
}

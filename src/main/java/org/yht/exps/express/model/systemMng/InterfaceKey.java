package org.yht.exps.express.model.systemMng;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "InterfaceKey", description = "전자상거래 Interface Key")
@Alias("interfaceKey")
@EqualsAndHashCode(callSuper=false)
public class InterfaceKey extends ResultList{

	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="거래처코드")
	private String customerCode;
	@ApiModelProperty(value="거래처명")
	private String customerName;
	@ApiModelProperty(value="accessKey")
	private String accessKey;
	@ApiModelProperty(value="사용유무")
	private String useYn;
	@ApiModelProperty(value="입력일자")
	private String registerDateTime;
	@ApiModelProperty(value="입력자ID")
	private String registerUserId;
	@ApiModelProperty(value="입력자")
	private String registerUserName;
	@ApiModelProperty(value="수정일자")
	private String updateDateTime;
	@ApiModelProperty(value="수정자")
	private String updateUserName;
	@ApiModelProperty(value="수정자ID")
	private String updateUserId;
	@ApiModelProperty(value="API 사용하여 생성한 오더 카운트")
	private String orderCnt;	
	@ApiModelProperty(value="API 사용하여 대한통운 주소정제 서비스 사용 카운트")
	private String deliveryCodeCnt;	
	
	
	/** 저장 */
	@ApiModelProperty(value="insert/update구분")
	private String action;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
}

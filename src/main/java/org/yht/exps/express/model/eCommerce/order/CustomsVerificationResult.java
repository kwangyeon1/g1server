package org.yht.exps.express.model.eCommerce.order;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "CustomsVerificationResult", description = "개인통관부호 검증 결과")
@Alias("customsVerificationResult")
public class CustomsVerificationResult {
	@ApiModelProperty(value="Index Name")
	private String indexName;
	@ApiModelProperty(value="ES ID")
	private String id;
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;
	@ApiModelProperty(value="사용자ID")
	private String userId;
	@ApiModelProperty(value="Order Job No")
	private String odJobNo;
	@ApiModelProperty(value="Master Order Job No")
	private String masterOdJobNo;
	@ApiModelProperty(value="MBL No")
	private String mblNo;
	@ApiModelProperty(value="HBL No")
	private String hblNo;
	@ApiModelProperty(value="검증 처리 상태")
	private String status;
	@ApiModelProperty(value="총 검증 개수")
	private int totalCount;
	@ApiModelProperty(value="검증 실패 개수")
	private int failCount;
	@ApiModelProperty(value="입력자 ID")
	private String registerUserId;
	@ApiModelProperty(value="입력자명")
	private String registerUserName;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	@ApiModelProperty(value="대기순번")
	private int order;
}

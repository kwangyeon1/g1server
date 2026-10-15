package org.yht.exps.express.model.sms.user;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "EcmCustomerBank", description = "Ecm거래처 계좌정보 저장용")
@Alias("ecmCustomerBank")
public class EcmCustomerBank {
	
	@ApiModelProperty(value="은행계좌코드")
	private String bankAccountsCode;
	@ApiModelProperty(value="출금계좌여부")
	private char paymentAccountsYn;
	@ApiModelProperty(value="바운드")
	private String bound;
	@ApiModelProperty(value="바운드구분")
	private String boundType;
	@ApiModelProperty(value="은행코드")
	private String bankCode;
	@ApiModelProperty(value="은행명")
	private String bankName;
	@ApiModelProperty(value="은행지점명칭")
	private String bankOfficeName;
	@ApiModelProperty(value="계좌번호")
	private String bankAccountNo;
	@ApiModelProperty(value="은행예금주")
	private String bankDepositor;
	@ApiModelProperty(value="은행계좌개설일자")
	private String bankAssignDate;
	@ApiModelProperty(value="비고")
	private String remarks;
	@ApiModelProperty(value="출력여부")
	private String printYn;
	@ApiModelProperty(value="표시순서")
	private int displayIndex;
	@ApiModelProperty(value="통화단위")
	private String currencyCode;
	@ApiModelProperty(value="사용여부")
	private String useYn;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;
	
}

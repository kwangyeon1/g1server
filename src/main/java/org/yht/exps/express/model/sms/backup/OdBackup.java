package org.yht.exps.express.model.sms.backup;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.eCommerce.order.search.SearchOrderList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@ApiModel(value = "OdBackup", description = "오더 백업시 필요한 값")
@Alias("odBackup")
public class OdBackup {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	@ApiModelProperty(value="거래처코드")
	private String userBusinessCode;
	@ApiModelProperty(value="유저아이디")
	private String userId;
	@ApiModelProperty(value="검색조건")
	private SearchOrderList searchOrderList;
	@ApiModelProperty(value="odJobNo List")
	private List<String> odJobNoList;
	@ApiModelProperty(value="(검색/선택) 백업타입")
	private String backupType;
}

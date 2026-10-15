package org.yht.exps.express.model.eCommerce.license;

import java.util.List;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "LicenseDelete", description = "면장 삭제")
@Alias("licenseDelete")
public class LicenseDelete {
	
	@ApiModelProperty(value="도입사코드", example="YHTXX")
	private String entCode;
	
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;
	
	@ApiModelProperty(value="리스트")
	private List<License> licenseList;
	
	@ApiModelProperty(value="운송방법코드")
	private String licenseJobNo;
	
	@ApiModelProperty(value="란번호")
	private String seqNoId;
	
	@ApiModelProperty(value="순번")
	private String cmdtSeqNoId;
	
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	
	@ApiModelProperty(value="작업자사업장코드")
	private String userBusinessCode;
	@ApiModelProperty(value="삭제자명")
	private String userName;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}

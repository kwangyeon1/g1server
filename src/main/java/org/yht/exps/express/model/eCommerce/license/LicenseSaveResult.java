package org.yht.exps.express.model.eCommerce.license;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "LicenseSaveResult", description = "License  Save 결과")
@Alias("licenseSaveResult")
public class LicenseSaveResult {
	@ApiModelProperty(value="관리번호")
	private String seqNo;
	@ApiModelProperty(value="결과코드")
	private String resultCode;
	@ApiModelProperty(value="필수값오류리스트")
	private List<Map<String, Object>> errorList;
	
	@ApiModelProperty(value="면장관리번호")
	private String licenseJobNo;
	
	@ApiModelProperty(value="배송요청 전체 카운트")
	private int totalCnt;
	
	@ApiModelProperty(value="결과내역")
	private Map<String, Object> resultData;
	
}

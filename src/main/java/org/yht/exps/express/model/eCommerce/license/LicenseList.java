package org.yht.exps.express.model.eCommerce.license;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "LicenseList", description = "전자상거래 면장 리스트")
@Alias("licenseList")
public class LicenseList {
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;	
	@ApiModelProperty(value="면장구분")
	private String licenseType;	
	
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="리스트")
	private List<License> licenseList;
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
	
	@ApiModelProperty(value="엑셀 업로드 리스트")
	private List<Map<String, Object>> dataList;
	@ApiModelProperty(value="입력일시")
	private String registerDateTime;	
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="입력자코드")
	private String registerUserName;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
}

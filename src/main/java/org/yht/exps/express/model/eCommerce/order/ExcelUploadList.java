package org.yht.exps.express.model.eCommerce.order;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ExcelUploadList", description = "UPS 적하목록정보 엑셀 업로드 리스트")
@Alias("excelUploadList")
public class ExcelUploadList {
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="사용자사업장코드")
	private String userEntBusinessCode;
	
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	
	@ApiModelProperty(value="엑셀 업로드 리스트")
	private List<Map<String, Object>> dataList;
	@ApiModelProperty(value="입력일시")
	private String updateDateTime;	
	@ApiModelProperty(value="입력자코드")
	private String updateUserId;
	@ApiModelProperty(value="입력자코드")
	private String updateUserName;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}


package org.yht.exps.express.model.eCommerce.order;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ExcelUploadOdChg", description = "엑셀오더변경")
@Alias("excelUploadOdChg")
public class ExcelUploadOdChg {
	
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="암호화여부")
	private String cryptographyYn;
	@ApiModelProperty(value="사업장코드")
	private String entBusinessCode;	
	@ApiModelProperty(value="AIR : 항공, SEA:해운")
	private String bound;
	@ApiModelProperty(value="IMP:수입, EXP:수출")
	private String boundType;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	
	@ApiModelProperty(value="엑셀 업로드 리스트")
	private List<Map<String, Object>> dataList;
	@ApiModelProperty(value="엑셀 업로드 정보")
	private Map<String, Object> dataMap;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
	
}


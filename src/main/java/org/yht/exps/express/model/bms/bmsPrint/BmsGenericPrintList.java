package org.yht.exps.express.model.bms.bmsPrint;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.apache.poi.ss.formula.functions.T;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "BmsGenericPrintList", description = "보세구역관리(BMS) 출력 공통 리스트")
@Alias("bmsGenericPrintList")
public class BmsGenericPrintList{
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="리스트")
	private List<?> bmsPrintList;
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
}

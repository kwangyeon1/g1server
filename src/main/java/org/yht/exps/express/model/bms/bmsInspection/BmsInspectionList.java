package org.yht.exps.express.model.bms.bmsInspection;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "BmsInspectionList", description = "검사계획통보 조회 리스트")
@Alias("BmsInspectionList")
public class BmsInspectionList{
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="리스트")
	private List<BmsInspection> bmsInspectionList;
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
}

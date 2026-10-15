package org.yht.exps.express.model.bms.bmsErrorDesc;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "BmsErrorDescList", description = "보세구역관리(BMS) 오류내역 공통 리스트")
@Alias("BmsErrorDescList")
public class BmsErrorDescList{
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="리스트")
	private List<BmsErrorDesc> bmsErrorDescList;
	@ApiModelProperty(value="에러 타입") 
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
}

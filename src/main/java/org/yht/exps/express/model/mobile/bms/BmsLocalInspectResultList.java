package org.yht.exps.express.model.mobile.bms;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import java.util.List;

/** 반입 리스트*/

@Data
@ApiModel(value = "BmsLocalInspectResultList", description = "특송창고 `검사결과 리스트")
@Alias("bmsLocalInspectResultList")
public class BmsLocalInspectResultList {
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="리스트")
	private List<BmsLocalInspectResult> bmsLocalInspectResultList;
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
}

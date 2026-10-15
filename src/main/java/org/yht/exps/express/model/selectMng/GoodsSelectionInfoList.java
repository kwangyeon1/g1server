package org.yht.exps.express.model.selectMng;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "GoodsSelectionInfoList", description = "전자상거래 통관목록 자동선별(기초) 리스트")
@Alias("goodsSelectionInfoList")
public class GoodsSelectionInfoList {
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="리스트")
	private List<GoodsSelectionInfo> goodsSelectionInfoList;
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함 ")
	private String scrollId;
}

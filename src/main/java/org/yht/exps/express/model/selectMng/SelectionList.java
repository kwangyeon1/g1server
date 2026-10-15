package org.yht.exps.express.model.selectMng;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SelectionList", description = "전자상거래 목록선별 리스트")
@Alias("selectionList")
public class SelectionList {
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="리스트")
	private List<Selection> selectionList;
	@ApiModelProperty(value="선별되지않은 아이템 리스트")
	private List<GoodsSelectionInfo> unSelectionItemList;
//	@ApiModelProperty(value="hsCode 업데이트 할 아이템 리스트")
//	private List<GoodsSelectionInfo> selectionHsList;
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함 ")
	private String scrollId;
}

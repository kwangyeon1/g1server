package org.yht.exps.express.model.systemMng;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "HubSubQnAList", description = "eHub 서브 Q&A 리스트")
@Alias("hubSubQnAList")
public class HubSubQnAList {
	
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="리스트")
	private HubQnA hubQnA;
	@ApiModelProperty(value="서브리스트")
	private List<HubQnA> hubSubQnAList;
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
}

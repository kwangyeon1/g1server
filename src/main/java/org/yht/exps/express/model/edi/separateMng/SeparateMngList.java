package org.yht.exps.express.model.edi.separateMng;

import java.util.List;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SeparateMngList", description = "전자상거래  별도관리물품해제신고 리스트")
@Alias("separateMngList")
public class SeparateMngList {
	
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="제출번호 그룹 리스트")
	private List<SeparateMng> separateMngDocList;
	@ApiModelProperty(value="별도관리물품해제신고 리스트")
	private List<SeparateMng> separateMngList;
	
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
}

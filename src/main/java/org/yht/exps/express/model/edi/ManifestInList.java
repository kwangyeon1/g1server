package org.yht.exps.express.model.edi;

import java.util.List;
import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ManifestInList", description = "전자상거래  적하목록수입 리스트")
@Alias("manifestInList")
public class ManifestInList {
	
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="마스터 리스트")
	private List<ManifestInMaster> manifestInMasterList;
	@ApiModelProperty(value="하우스 리스트")
	private List<ManifestInHouse> manifestInHouseList;
	
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
}

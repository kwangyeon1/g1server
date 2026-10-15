package org.yht.exps.express.model.bms.bmsTotalManage;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

@Data
@ApiModel(value = "BmsTotalManageList", description = "통합관리")
@Alias("bmsTotalManageList")
public class BmsTotalManageList {
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="마스터 리스트")
	private List<BmsTotalManage> totalManageMasterList;
	@ApiModelProperty(value="하우스 리스트")
	private List<BmsTotalManageDetail> totalManageHouseList;
	
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
}
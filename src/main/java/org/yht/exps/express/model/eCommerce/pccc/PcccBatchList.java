package org.yht.exps.express.model.eCommerce.pccc;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "PcccBatchList", description = "전자상거래 Pccc Batch 리스트")
@Alias("PcccBatchList")
public class PcccBatchList{
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="리스트")
	private List<PcccBatch> pcccBatchList;
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="userId")
	private String userId;
	@ApiModelProperty(value="userName")
	private String userName;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
	
	//시스템 로그
	@ApiModelProperty(value="처리자")
	private String logUserName;
	@ApiModelProperty(value="처리자(영)")
	private String logUserNameEng;
	@ApiModelProperty(value="출력용 사업장명")
	private String logUserBusinessPrintName;
}

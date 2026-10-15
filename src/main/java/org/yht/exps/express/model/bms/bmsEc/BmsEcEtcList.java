package org.yht.exps.express.model.bms.bmsEc;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

@Data
@ApiModel(value = "BmsEcEtcList", description = "EC통관")
@Alias("bmsEcEtcList")
public class BmsEcEtcList {
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="통관 마스터 리스트")
	private List<BmsEcEtc> ecEtcMasterList;
	@ApiModelProperty(value="통관 상세 리스트")
	private List<BmsEcEtcDetail> ecEtcDetailList;
	
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
}
package org.yht.exps.express.model.mobile.bms;

import java.util.List;

import org.apache.ibatis.type.Alias;
import org.yht.util.CustomErrorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "MobPaloList", description = "로케이션적치 리스트")
@Alias("mobPaloList")
public class MobPaloList {
	@ApiModelProperty(value="리스트 개수")
	private long total;
	@ApiModelProperty(value="리스트")
	private List<MobPalo> mobPaloList;
	@ApiModelProperty(value="결과")
	private String scanResult;
	@ApiModelProperty(value="결과메세지코드")
	private String resultCode;
	@ApiModelProperty(value="에러 타입")
	private CustomErrorType errorType;
	@ApiModelProperty(value="스크롤 ID, 페이징 기능 사용 안할 시 필요함")
	private String scrollId;
	
	@ApiModelProperty(value="성공 개수")
	private long succCnt;
	@ApiModelProperty(value="실패 개수")
	private long failCnt;
}

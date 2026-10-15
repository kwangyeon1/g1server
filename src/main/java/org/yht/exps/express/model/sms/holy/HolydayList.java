package org.yht.exps.express.model.sms.holy;

import java.util.List;

import org.apache.ibatis.type.Alias;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "HolydayList", description = "공휴일관리 리스트")
@Alias("holydayList")
public class HolydayList {
	@ApiModelProperty(value="리스트 개수")
	private long total;
	
	/** 저장 */
	@ApiModelProperty(value="insert/delete구분")
	private String action;
	@ApiModelProperty(value="도입사")
	private String entCode;
	@ApiModelProperty(value="gmtTime")
	private String gmtTime;
	@ApiModelProperty(value="날짜형식")
	private String dateFormat;
	@ApiModelProperty(value="입력자코드")
	private String registerUserId;
	@ApiModelProperty(value="수정자코드")
	private String updateUserId;
	
	/** 조회 */
	@ApiModelProperty(value="리스트")
	private List<Holyday> dataList;
	
	@ApiModelProperty(value="리스트")
	private List<Holyday> excelDataList;
}

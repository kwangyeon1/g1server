package org.yht.exps.express.model.eCommerce.pccc;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.type.Alias;
import org.yht.exps.express.model.ResultList;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@ApiModel(value = "PcccBatch", description = "전자상거래 PcccBatch")
@Alias("PcccBatch")
@EqualsAndHashCode(callSuper=false)
public class PcccBatch extends ResultList{

	@ApiModelProperty(value="관리번호")
	private String pcccSetJobNo;
	@ApiModelProperty(value="도입사코드")
	private String entCode;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1;
	@ApiModelProperty(value="상위업체")
	private String custCodeLv1Name;
	@ApiModelProperty(value="반복횟수")
	private String repeatCount;
	@ApiModelProperty(value="전송시간대구분")
	private String sendTimeType;
	@ApiModelProperty(value="전송시간")
	private String sendTime;
	@ApiModelProperty(value="전송시간")
	private String sendTimeConvert;
	@ApiModelProperty(value="요청ID")
	private String requestId;
	@ApiModelProperty(value="입력자")
	private String registerUserId;	
	@ApiModelProperty(value="입력시간")
	private String registerDateTime;
	@ApiModelProperty(value="갱신자")
	private String updateUserId;
	@ApiModelProperty(value="갱신시간")
	private String updateDateTime;
	@ApiModelProperty(value="날짜형식")
	private String gmtTime;
	@ApiModelProperty(value="insert/update구분")
	private String action;
	
	// 알람톡 설정
	@ApiModelProperty(value="템플릿타입")
	private String templateType;
	@ApiModelProperty(value="템플릿번호")
	private String templateSeqNo;
	@ApiModelProperty(value="리스트")
	private List<Map<String,Object>> dataList;
}
